package uk.gov.companieshouse.documentstore.consumer.service;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.DocumentStoreDelta;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryDocumentMetadataUpdateApi;
import uk.gov.companieshouse.api.model.document.CreateDocumentResponseApi;
import uk.gov.companieshouse.documentstore.consumer.apiclient.FilingHistoryDocumentMetadataApiClient;
import uk.gov.companieshouse.documentstore.consumer.logging.DataMapHolder;
import uk.gov.companieshouse.documentstore.consumer.transformer.FilingHistoryDocumentMetadataTransformer;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import static uk.gov.companieshouse.documentstore.consumer.Application.NAMESPACE;

@Component
public class FilingHistoryDocumentMetadataUpdateProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    private static final String CONTENT_TYPE_PDF = "application/pdf";
    private static final String CONTENT_TYPE_ZIP = "application/zip";

    private final FilingHistoryDocumentMetadataTransformer transformer;
    private final FilingHistoryDocumentMetadataApiClient fhApiClient;

    public FilingHistoryDocumentMetadataUpdateProcessor(FilingHistoryDocumentMetadataTransformer transformer, FilingHistoryDocumentMetadataApiClient fhApiClient) {
        this.transformer = transformer;
        this.fhApiClient = fhApiClient;
    }

    public void process(DocumentStoreDelta documentStore, CreateDocumentResponseApi createDocumentResponseApi) {
        final String contentType = createDocumentResponseApi.getContentType();

        // only update filing history with doc metadata link for PDF and ZIP files
        if (CONTENT_TYPE_PDF.equals(contentType) || CONTENT_TYPE_ZIP.equals(contentType)) {
            LOGGER.info("Updating filing history document metadata", DataMapHolder.getLogMap());

            FilingHistoryDocumentMetadataUpdateApi apiRequest = transformer.transform(createDocumentResponseApi);
            String transactionId = transformer.transformFilingHistoryId(documentStore);
            DataMapHolder.get().transactionId(transactionId);
            DataMapHolder.get().filingHistoryDocumentMetadata(apiRequest.getDocumentMetadata());

            fhApiClient.updateDocumentMetadataLink(apiRequest, documentStore.getCompanyNumber(), transactionId);

            LOGGER.info("Updated filing history document metadata successfully", DataMapHolder.getLogMap());
            return;
        }
        LOGGER.info("Skipping filing history document metadata", DataMapHolder.getLogMap());
    }
}
