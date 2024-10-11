package uk.gov.companieshouse.documentstore.consumer.kafka;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.patchRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.requestMadeFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.reflect.ReflectDatumWriter;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.codehaus.plexus.util.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import uk.gov.companieshouse.api.model.document.CreateDocumentResponseApi;
import uk.gov.companieshouse.delta.ChsDelta;

@SpringBootTest
@WireMockTest(httpPort = 8888)
class ConsumerPositiveIT extends AbstractKafkaIT {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String TRANSACTION_ID = "3043972675";
    private static final String SALT = "salt";

    @Autowired
    private KafkaConsumer<String, byte[]> testConsumer;
    @Autowired
    private KafkaProducer<String, byte[]> testProducer;
    @Autowired
    private TestConsumerAspect testConsumerAspect;
    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("steps", () -> 1);
    }

    @BeforeEach
    public void setup() {
        testConsumerAspect.resetLatch();
        testConsumer.poll(Duration.ofMillis(1000));
    }

    @Test
    void shouldConsumeDocumentStoreDeltaTopicAndProcessDelta() throws Exception {
        // given
        final String delta = IOUtils.resourceToString("/data/upsert/document-store-delta.json", StandardCharsets.UTF_8);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Encoder encoder = EncoderFactory.get().directBinaryEncoder(outputStream, null);
        DatumWriter<ChsDelta> writer = new ReflectDatumWriter<>(ChsDelta.class);
        writer.write(new ChsDelta(delta, 0, "context_id", false), encoder);

        final String expectedDocuStoreRequestBody = IOUtils.resourceToString("/data/upsert/document-store-request-body.json",
                StandardCharsets.UTF_8);
        final String expectedFilingHistoryRequestBody = IOUtils.resourceToString("/data/upsert/filing-history-request-body.json",
                StandardCharsets.UTF_8);

        final String encodedTransactionId =
                Base64.encodeBase64URLSafeString((StringUtils.trim(TRANSACTION_ID) + SALT).getBytes(StandardCharsets.UTF_8));
        final String expectedFilingHistoryRequestUri = "/company/%s/filing-history/%s/document-metadata"
                .formatted(COMPANY_NUMBER, encodedTransactionId);
        final String expectedDocumentStoreRequestURI = "/document";

        CreateDocumentResponseApi createDocumentResponseApi = new CreateDocumentResponseApi();
        createDocumentResponseApi.setDocumentId("docId");
        createDocumentResponseApi.setPages(3);
        createDocumentResponseApi.setContentType("application/pdf");
        final String res = objectMapper.writeValueAsString(createDocumentResponseApi);

        stubFor(
                post(urlEqualTo(expectedDocumentStoreRequestURI))
                        .willReturn(aResponse()
                                .withBody(res)
                                .withStatus(200))
        );

        stubFor(patch(urlEqualTo(expectedFilingHistoryRequestUri))
                .willReturn(aResponse()
                        .withStatus(200)));

        // when
        testProducer.send(new ProducerRecord<>(KafkaUtils.MAIN_TOPIC, 0, System.currentTimeMillis(),
                "key", outputStream.toByteArray()));
        if (!testConsumerAspect.getLatch().await(5L, TimeUnit.SECONDS)) {
            fail("Timed out waiting for latch");
        }

        // then
        ConsumerRecords<?, ?> consumerRecords = KafkaTestUtils.getRecords(testConsumer, Duration.ofMillis(10000L), 1);
        assertThat(KafkaUtils.noOfRecordsForTopic(consumerRecords, KafkaUtils.MAIN_TOPIC)).isOne();
        assertThat(KafkaUtils.noOfRecordsForTopic(consumerRecords, KafkaUtils.RETRY_TOPIC)).isZero();
        assertThat(KafkaUtils.noOfRecordsForTopic(consumerRecords, KafkaUtils.ERROR_TOPIC)).isZero();
        assertThat(KafkaUtils.noOfRecordsForTopic(consumerRecords, KafkaUtils.INVALID_TOPIC)).isZero();

        verify(1, postRequestedFor(urlMatching(expectedDocumentStoreRequestURI))
                .withRequestBody(equalToJson(expectedDocuStoreRequestBody)));

        verify(1, patchRequestedFor(urlMatching(expectedFilingHistoryRequestUri)));

        verify(requestMadeFor(new PostRequestMatcher(expectedDocumentStoreRequestURI, expectedDocuStoreRequestBody)));
        verify(requestMadeFor(new PatchRequestMatcher(expectedFilingHistoryRequestUri, expectedFilingHistoryRequestBody)));
    }
}
