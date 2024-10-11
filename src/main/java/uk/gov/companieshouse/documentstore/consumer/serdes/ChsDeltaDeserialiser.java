package uk.gov.companieshouse.documentstore.consumer.serdes;

import static uk.gov.companieshouse.documentstore.consumer.Application.NAMESPACE;

import java.io.IOException;
import org.apache.avro.AvroRuntimeException;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.reflect.ReflectDatumReader;
import org.apache.kafka.common.serialization.Deserializer;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.documentstore.consumer.exception.InvalidPayloadException;
import uk.gov.companieshouse.documentstore.consumer.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

public class ChsDeltaDeserialiser implements Deserializer<ChsDelta> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    @Override
    public ChsDelta deserialize(String topic, byte[] data) {
        try {
            Decoder decoder = DecoderFactory.get().binaryDecoder(data, null);
            DatumReader<ChsDelta> reader = new ReflectDatumReader<>(ChsDelta.class);
            return reader.read(null, decoder);
        } catch (IOException | AvroRuntimeException ex) {
            String payload = new String(data);
            LOGGER.error("Error deserialising message payload: [%s]".formatted(payload), ex, DataMapHolder.getLogMap());
            throw new InvalidPayloadException("Invalid payload: [%s]".formatted(payload), ex);
        }
    }
}
