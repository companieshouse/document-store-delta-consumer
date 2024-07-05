package uk.gov.companieshouse.documentstore.consumer.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.documentstore.consumer.exception.RetryableException;
import uk.gov.companieshouse.documentstore.consumer.service.DeltaService;

@Component
public class Consumer {

    private final DeltaService deltaService;
    private final MessageFlags messageFlags;

    public Consumer(DeltaService deltaService, MessageFlags messageFlags) {
        this.deltaService = deltaService;
        this.messageFlags = messageFlags;
    }

    @KafkaListener(
            id = "${consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory",
            topics = {"${consumer.topic}"},
            groupId = "${consumer.group-id}"
    )
    public void consume(Message<ChsDelta> message) {
        try {
            deltaService.process(message.getPayload());
        } catch (RetryableException ex) {
            messageFlags.setRetryable(true);
            throw ex;
        }
    }
}
