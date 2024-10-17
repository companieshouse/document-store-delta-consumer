package uk.gov.companieshouse.documentstore.consumer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.DocumentStoreDelta;
import uk.gov.companieshouse.api.model.document.CreateDocumentApi;
import uk.gov.companieshouse.api.model.document.CreateDocumentResponseApi;
import uk.gov.companieshouse.documentstore.consumer.apiclient.DocumentApiClient;
import uk.gov.companieshouse.documentstore.consumer.transformer.DocumentApiTransformer;

@Component
public class DocumentStoreDeltaProcessor {

    private final DocumentApiTransformer transformerService;
    private final DocumentApiClient docApiClient;
    private final boolean setNoDeletionHeader;

    public DocumentStoreDeltaProcessor(@Value("${set-no-deletion-header}") Boolean setNoDeletionHeader,
            DocumentApiTransformer transformerService, DocumentApiClient docApiClient) {
        this.transformerService = transformerService;
        this.docApiClient = docApiClient;
        this.setNoDeletionHeader = setNoDeletionHeader;
    }

    public CreateDocumentResponseApi processDelta(DocumentStoreDelta delta) {
        CreateDocumentApi createDocumentApiRequest = this.transformerService.transform(delta);
        return docApiClient.createDocument(createDocumentApiRequest, setNoDeletionHeader);
    }
}
