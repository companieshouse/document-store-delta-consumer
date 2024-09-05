package uk.gov.companieshouse.documentstore.consumer.apiclient;

import java.util.function.Supplier;

import com.google.api.client.http.HttpStatusCodes;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.document.CreateDocumentApi;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.document.CreateDocumentResponseApi;
import uk.gov.companieshouse.documentstore.consumer.logging.DataMapHolder;

@Component
public class DocumentApiClient {

    private static final String POST_REQUEST_URI = "/document";

    private final Supplier<InternalApiClient> internalApiClientFactory;
    private final DocumentApiResponseHandler documentApiResponseHandler;

    public DocumentApiClient(Supplier<InternalApiClient> internalApiClientFactory,
                             DocumentApiResponseHandler documentApiResponseHandler) {
        this.internalApiClientFactory = internalApiClientFactory;
        this.documentApiResponseHandler = documentApiResponseHandler;
    }

    public CreateDocumentResponseApi createDocument(CreateDocumentApi requestBody, boolean setNoDeletionHeader) {
        InternalApiClient client = internalApiClientFactory.get();
        client.getHttpClient().setRequestId(DataMapHolder.getRequestId());

        ApiResponse<CreateDocumentResponseApi> response = null;
        try {
            response = client.privateDocumentResourceHandler().createDocument(POST_REQUEST_URI, requestBody, setNoDeletionHeader).execute();
        } catch (ApiErrorResponseException ex) {
            documentApiResponseHandler.handle(ex);
            return null;
        } catch (URIValidationException ex) {
            documentApiResponseHandler.handle(ex);
            return null;
        }

        if (response == null) {
            documentApiResponseHandler.handleNullResponse();
            return null;
        } else if (!HttpStatusCodes.isSuccess(response.getStatusCode())) {

            documentApiResponseHandler.handleUnexpectedStatusCode(response.getStatusCode());
            return null;
        }

        return response.getData();
    }
}
