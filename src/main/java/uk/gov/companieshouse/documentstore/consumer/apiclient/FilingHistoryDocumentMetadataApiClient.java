package uk.gov.companieshouse.documentstore.consumer.apiclient;

import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryDocumentMetadataUpdateApi;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.documentstore.consumer.logging.DataMapHolder;

@Component
public class FilingHistoryDocumentMetadataApiClient {

    private static final String PATCH_REQUEST_URI = "/company/%s/filing-history/%s/document-metadata";

    private final Supplier<InternalApiClient> internalApiClientFactory;
    private final FilingHistoryDocumentMetadataApiResponseHandler filingHistoryDocumentMetadataApiResponseHandler;

    public FilingHistoryDocumentMetadataApiClient(Supplier<InternalApiClient> internalApiClientFactory, FilingHistoryDocumentMetadataApiResponseHandler filingHistoryDocumentMetadataApiResponseHandler) {
        this.internalApiClientFactory = internalApiClientFactory;
        this.filingHistoryDocumentMetadataApiResponseHandler = filingHistoryDocumentMetadataApiResponseHandler;
    }

    public void updateDocumentMetadataLink(FilingHistoryDocumentMetadataUpdateApi requestBody, String companyNumber, String filingHistoryId) {
        InternalApiClient client = internalApiClientFactory.get();
        client.getHttpClient().setRequestId(DataMapHolder.getRequestId());

        final String formattedUri = PATCH_REQUEST_URI.formatted(companyNumber, filingHistoryId);
        DataMapHolder.get().resourceUri(formattedUri);
        try {
            client.privateFilingHistoryDocumentMetadataResourceHandler()
                    .patchFilingHistoryLinks(formattedUri, requestBody)
                    .execute();
        } catch (ApiErrorResponseException ex) {
            filingHistoryDocumentMetadataApiResponseHandler.handle(ex);
        } catch (URIValidationException ex) {
            filingHistoryDocumentMetadataApiResponseHandler.handle(ex);
        }
    }
}
