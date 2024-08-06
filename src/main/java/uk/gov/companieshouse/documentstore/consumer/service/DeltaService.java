package uk.gov.companieshouse.documentstore.consumer.service;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.DocumentStoreDelta;
import uk.gov.companieshouse.api.model.document.CreateDocumentResponseApi;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.documentstore.consumer.serdes.DocumentStoreDeltaDeserialiser;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import static uk.gov.companieshouse.documentstore.consumer.Application.NAMESPACE;

@Component
public class DeltaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

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
        CreateDocumentResponseApi createDocumentResponseApi = documentStoreDeltaProcessor.processDelta(documentStore);
        filingHistoryDocumentMetadataUpdateProcessor.process(documentStore, createDocumentResponseApi);
    }

}
