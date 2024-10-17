package uk.gov.companieshouse.documentstore.consumer.apiclient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.document.PrivateDocumentResourceHandler;
import uk.gov.companieshouse.api.handler.document.request.PrivateCreateDocument;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.http.HttpClient;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.document.CreateDocumentApi;
import uk.gov.companieshouse.api.model.document.CreateDocumentResponseApi;
import uk.gov.companieshouse.documentstore.consumer.logging.DataMapHolder;

@ExtendWith(MockitoExtension.class)
class DocumentApiClientTest {
    private static final String REQUEST_URI = "/document";
    private static final String REQUEST_ID = "request_id";

    @InjectMocks
    private DocumentApiClient documentApiClient;
    @Mock
    private Supplier<InternalApiClient> internalApiClientFactory;
    @Mock
    private DocumentApiResponseHandler responseHandler;
    @Mock
    private CreateDocumentApi requestBody;
    @Mock
    private InternalApiClient internalApiClient;
    @Mock
    private HttpClient apiClient;
    @Mock
    private PrivateDocumentResourceHandler privateDocumentResourceHandler;
    @Mock
    private PrivateCreateDocument privateCreateDocument;
    @Mock
    private ApiResponse<CreateDocumentResponseApi> response;

    @Test
    void shouldSendSuccessfulPutRequest() throws Exception {
        // given
        when(internalApiClientFactory.get()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(apiClient);
        when(internalApiClient.privateDocumentResourceHandler()).thenReturn(privateDocumentResourceHandler);
        when(privateDocumentResourceHandler.createDocument(any(), any(), anyBoolean())).thenReturn(privateCreateDocument);
        when(privateCreateDocument.execute()).thenReturn(response);
        when(response.getStatusCode()).thenReturn(200);

        DataMapHolder.get().requestId(REQUEST_ID);

        // when
        documentApiClient.createDocument(requestBody, true);

        // then
        verify(apiClient).setRequestId(REQUEST_ID);
        verify(privateDocumentResourceHandler).createDocument(REQUEST_URI, requestBody, true);
        verify(privateCreateDocument).execute();
        verifyNoInteractions(responseHandler);
    }

    @Test
    void shouldHandleApiErrorExceptionWhenSendingPutRequest() throws Exception {
        // given
        Class<ApiErrorResponseException> exceptionClass = ApiErrorResponseException.class;

        when(internalApiClientFactory.get()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(apiClient);
        when(internalApiClient.privateDocumentResourceHandler()).thenReturn(privateDocumentResourceHandler);
        when(privateDocumentResourceHandler.createDocument(any(), any(), anyBoolean())).thenReturn(privateCreateDocument);
        when(privateCreateDocument.execute()).thenThrow(exceptionClass);

        DataMapHolder.get().requestId(REQUEST_ID);

        // when
        documentApiClient.createDocument(requestBody, true);

        // then
        verify(apiClient).setRequestId(REQUEST_ID);
        verify(privateDocumentResourceHandler).createDocument(REQUEST_URI, requestBody, true);
        verify(privateCreateDocument).execute();
        verify(responseHandler).handle(any(exceptionClass));
    }

    @Test
    void shouldHandleURIValidationExceptionWhenSendingPutRequest() throws Exception {
        // given
        Class<URIValidationException> exceptionClass = URIValidationException.class;

        when(internalApiClientFactory.get()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(apiClient);
        when(internalApiClient.privateDocumentResourceHandler()).thenReturn(privateDocumentResourceHandler);
        when(privateDocumentResourceHandler.createDocument(any(), any(), anyBoolean())).thenReturn(privateCreateDocument);
        when(privateCreateDocument.execute()).thenThrow(exceptionClass);

        DataMapHolder.get().requestId(REQUEST_ID);

        // when
        documentApiClient.createDocument(requestBody, true);

        // then
        verify(apiClient).setRequestId(REQUEST_ID);
        verify(privateDocumentResourceHandler).createDocument(REQUEST_URI, requestBody, true);
        verify(privateCreateDocument).execute();
        verify(responseHandler).handle(any(exceptionClass));
    }

    @Test
    void shouldHandleWhenResponseComesBackAsNull() throws Exception {
        // given
        when(internalApiClientFactory.get()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(apiClient);
        when(internalApiClient.privateDocumentResourceHandler()).thenReturn(privateDocumentResourceHandler);
        when(privateDocumentResourceHandler.createDocument(any(), any(), anyBoolean())).thenReturn(privateCreateDocument);
        when(privateCreateDocument.execute()).thenReturn(null);

        DataMapHolder.get().requestId(REQUEST_ID);

        // when
        documentApiClient.createDocument(requestBody, true);

        // then
        verify(apiClient).setRequestId(REQUEST_ID);
        verify(privateDocumentResourceHandler).createDocument(REQUEST_URI, requestBody, true);
        verify(privateCreateDocument).execute();
        verify(responseHandler).handleNullResponse();
    }

    @Test
    void shouldHandleWhenResponseComesBackNon200OK() throws Exception {
        // given
        when(internalApiClientFactory.get()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(apiClient);
        when(internalApiClient.privateDocumentResourceHandler()).thenReturn(privateDocumentResourceHandler);
        when(privateDocumentResourceHandler.createDocument(any(), any(), anyBoolean())).thenReturn(privateCreateDocument);
        when(privateCreateDocument.execute()).thenReturn(response);
        when(response.getStatusCode()).thenReturn(503);


        DataMapHolder.get().requestId(REQUEST_ID);

        // when
        documentApiClient.createDocument(requestBody, true);

        // then
        verify(apiClient).setRequestId(REQUEST_ID);
        verify(privateDocumentResourceHandler).createDocument(REQUEST_URI, requestBody, true);
        verify(privateCreateDocument).execute();
        verify(responseHandler).handleUnexpectedStatusCode(503);
    }
}
