package uk.gov.companieshouse.documentstore.consumer.service;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.DocumentStoreDelta;
import uk.gov.companieshouse.api.model.document.CreateDocumentApi;
import uk.gov.companieshouse.api.model.document.CreateDocumentResponseApi;
import uk.gov.companieshouse.documentstore.consumer.apiclient.DocumentApiClient;
import uk.gov.companieshouse.documentstore.consumer.transformer.DocumentApiTransformer;
import uk.gov.companieshouse.environment.EnvironmentReader;

@Component
public class DocumentStoreDeltaProcessor {

    private static final String EMPTY_RESPONSE_EXCEPTION_MESSAGE = "Empty response when creating document";
    private static final String NO_DELETION_HEADER_ENV_VAR = "DOCUMENT_STORE_NO_DELETION";

    private final DocumentApiTransformer transformerService;
    private final DocumentApiClient docApiClient;
    private final boolean setNoDeletionHeader;

    public DocumentStoreDeltaProcessor(DocumentApiTransformer transformerService, DocumentApiClient docApiClient,
                                       EnvironmentReader environmentReader) {
        this.transformerService = transformerService;
        this.docApiClient = docApiClient;
        this.setNoDeletionHeader = environmentReader.getOptionalBoolean(NO_DELETION_HEADER_ENV_VAR);
    }

    public CreateDocumentResponseApi processDelta(DocumentStoreDelta delta) {
        CreateDocumentApi createDocumentApiRequest = this.transformerService.transform(delta);
        return docApiClient.createDocument(createDocumentApiRequest, setNoDeletionHeader);
    }
}
