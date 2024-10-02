package uk.gov.companieshouse.documentstore.consumer.apiclient;

import static org.mockito.ArgumentMatchers.any;
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
import uk.gov.companieshouse.api.filinghistory.FilingHistoryDocumentMetadataUpdateApi;
import uk.gov.companieshouse.api.handler.delta.PrivateDeltaResourceHandler;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.filinghistory.PrivateFilingHistoryDocumentMetadataResourceHandler;
import uk.gov.companieshouse.api.handler.filinghistory.documentmetadata.request.PrivateFilingHistoryDocumentMetadataPatch;
import uk.gov.companieshouse.api.http.HttpClient;
import uk.gov.companieshouse.documentstore.consumer.logging.DataMapHolder;

@ExtendWith(MockitoExtension.class)
class FilingHistoryDocumentMetadataApiClientTest {
    private static final String COMPANY_NUMBER = "12345678";
    private static final String FILING_HISTORY_ID = "ABCD1234";
    private static final String REQUEST_URI = "/company/%s/filing-history/%s/document-metadata".
            formatted(COMPANY_NUMBER, FILING_HISTORY_ID);
    private static final String REQUEST_ID = "request_id";

    @InjectMocks
    private FilingHistoryDocumentMetadataApiClient metadataApiClient;
    @Mock
    private Supplier<InternalApiClient> internalApiClientFactory;
    @Mock
    private FilingHistoryDocumentMetadataApiResponseHandler responseHandler;
    @Mock
    private FilingHistoryDocumentMetadataUpdateApi requestBody;
    @Mock
    private InternalApiClient internalApiClient;
    @Mock
    private HttpClient apiClient;
    @Mock
    private PrivateFilingHistoryDocumentMetadataResourceHandler privateFilingHistoryDocumentMetadataResourceHandler;
    @Mock
    private PrivateFilingHistoryDocumentMetadataPatch privateFilingHistoryDocumentMetadataPatch;

    @Test
    void shouldSendSuccessfulPutRequest() throws Exception {
        // given
        when(internalApiClientFactory.get()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(apiClient);
        when(internalApiClient.privateFilingHistoryDocumentMetadataResourceHandler()).thenReturn(privateFilingHistoryDocumentMetadataResourceHandler);
        when(privateFilingHistoryDocumentMetadataResourceHandler.patchFilingHistoryLinks(any(), any())).thenReturn(privateFilingHistoryDocumentMetadataPatch);

        DataMapHolder.get().requestId(REQUEST_ID);

        // when
        metadataApiClient.updateDocumentMetadataLink(requestBody, COMPANY_NUMBER, FILING_HISTORY_ID);

        // then
        verify(apiClient).setRequestId(REQUEST_ID);
        verify(privateFilingHistoryDocumentMetadataResourceHandler).patchFilingHistoryLinks(REQUEST_URI, requestBody);
        verify(privateFilingHistoryDocumentMetadataPatch).execute();
        verifyNoInteractions(responseHandler);
    }

    @Test
    void shouldHandleApiErrorExceptionWhenSendingPutRequest() throws Exception {
        // given
        Class<ApiErrorResponseException> exceptionClass = ApiErrorResponseException.class;

        when(internalApiClientFactory.get()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(apiClient);
        when(internalApiClient.privateFilingHistoryDocumentMetadataResourceHandler()).thenReturn(privateFilingHistoryDocumentMetadataResourceHandler);
        when(privateFilingHistoryDocumentMetadataResourceHandler.patchFilingHistoryLinks(any(), any())).thenReturn(privateFilingHistoryDocumentMetadataPatch);
        when(privateFilingHistoryDocumentMetadataPatch.execute()).thenThrow(exceptionClass);

        DataMapHolder.get().requestId(REQUEST_ID);

        // when
        metadataApiClient.updateDocumentMetadataLink(requestBody, COMPANY_NUMBER, FILING_HISTORY_ID);

        // then
        verify(apiClient).setRequestId(REQUEST_ID);
        verify(privateFilingHistoryDocumentMetadataResourceHandler).patchFilingHistoryLinks(REQUEST_URI, requestBody);
        verify(privateFilingHistoryDocumentMetadataPatch).execute();
        verify(responseHandler).handle(any(exceptionClass));
    }

    @Test
    void shouldHandleURIValidationExceptionWhenSendingPutRequest() throws Exception {
        // given
        Class<URIValidationException> exceptionClass = URIValidationException.class;

        when(internalApiClientFactory.get()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(apiClient);
        when(internalApiClient.privateFilingHistoryDocumentMetadataResourceHandler()).thenReturn(privateFilingHistoryDocumentMetadataResourceHandler);
        when(privateFilingHistoryDocumentMetadataResourceHandler.patchFilingHistoryLinks(any(), any())).thenReturn(privateFilingHistoryDocumentMetadataPatch);
        when(privateFilingHistoryDocumentMetadataPatch.execute()).thenThrow(exceptionClass);

        DataMapHolder.get().requestId(REQUEST_ID);

        // when
        metadataApiClient.updateDocumentMetadataLink(requestBody, COMPANY_NUMBER, FILING_HISTORY_ID);

        // then
        verify(apiClient).setRequestId(REQUEST_ID);
        verify(privateFilingHistoryDocumentMetadataResourceHandler).patchFilingHistoryLinks(REQUEST_URI, requestBody);
        verify(privateFilingHistoryDocumentMetadataPatch).execute();
        verify(responseHandler).handle(any(exceptionClass));
    }
}
