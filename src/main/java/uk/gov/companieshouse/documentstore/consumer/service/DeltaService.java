package uk.gov.companieshouse.documentstore.consumer.service;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.DocumentStoreDelta;
import uk.gov.companieshouse.api.model.document.CreateDocumentResponseApi;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.documentstore.consumer.logging.DataMapHolder;
import uk.gov.companieshouse.documentstore.consumer.serdes.DocumentStoreDeltaDeserialiser;

@Component
public class DeltaService {

    private final DocumentStoreDeltaDeserialiser deserialiser;
    private final DocumentStoreDeltaProcessor documentStoreDeltaProcessor;
    private final FilingHistoryDocumentMetadataUpdateProcessor filingHistoryDocumentMetadataUpdateProcessor;

    public DeltaService(DocumentStoreDeltaDeserialiser deserialiser, DocumentStoreDeltaProcessor documentStoreDeltaProcessor,
                        FilingHistoryDocumentMetadataUpdateProcessor filingHistoryDocumentMetadataUpdateProcessor) {
        this.deserialiser = deserialiser;
        this.documentStoreDeltaProcessor = documentStoreDeltaProcessor;
        this.filingHistoryDocumentMetadataUpdateProcessor = filingHistoryDocumentMetadataUpdateProcessor;
    }

    public void process(ChsDelta delta) {
        DocumentStoreDelta documentStore = deserialiser.deserialiseDocumentStoreDelta(delta.getData());
        DataMapHolder.get().companyNumber(documentStore.getCompanyNumber());
        DataMapHolder.get().entityId(documentStore.getTransactionId());
        CreateDocumentResponseApi createDocumentResponseApi = documentStoreDeltaProcessor.processDelta(documentStore);
        filingHistoryDocumentMetadataUpdateProcessor.process(documentStore, createDocumentResponseApi);
    }

}
