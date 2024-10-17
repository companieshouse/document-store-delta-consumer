package uk.gov.companieshouse.documentstore.consumer.kafka;

import com.google.common.collect.Iterables;
import org.apache.kafka.clients.consumer.ConsumerRecords;

final class KafkaUtils {

    static final String MAIN_TOPIC = "document-store-delta";
    static final String RETRY_TOPIC = "document-store-delta-document-store-delta-consumer-retry";
    static final String ERROR_TOPIC = "document-store-delta-document-store-delta-consumer-error";
    static final String INVALID_TOPIC = "document-store-delta-document-store-delta-consumer-invalid";

    private KafkaUtils() {
    }

    static int noOfRecordsForTopic(ConsumerRecords<?, ?> records, String topic) {
        return Iterables.size(records.records(topic));
    }
}
