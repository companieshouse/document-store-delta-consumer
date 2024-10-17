package uk.gov.companieshouse.documentstore.consumer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.delta.DocumentStoreDelta;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryDocumentMetadataUpdateApi;
import uk.gov.companieshouse.api.model.document.CreateDocumentResponseApi;
import uk.gov.companieshouse.documentstore.consumer.apiclient.FilingHistoryDocumentMetadataApiClient;
import uk.gov.companieshouse.documentstore.consumer.transformer.FilingHistoryDocumentMetadataTransformer;

@ExtendWith(MockitoExtension.class)
class FilingHistoryDocumentMetadataUpdateProcessorTest {
    private static final String COMPANY_NUMBER = "12345678";
    private static final String FILING_HISTORY_ID = "ABCD1234";
    private static final String CONTENT_TYPE_PDF = "application/pdf";
    private static final String CONTENT_TYPE_ZIP = "application/zip";
    private static final String CONTENT_TYPE_XML = "application/xml";

    @Mock
    private FilingHistoryDocumentMetadataTransformer filingHistoryDocumentMetadataTransformer;
    @Mock
    private FilingHistoryDocumentMetadataApiClient filingHistoryDocumentMetadataApiClient;
    @InjectMocks
    private FilingHistoryDocumentMetadataUpdateProcessor filingHistoryDocumentMetadataUpdateProcessor;
    @Mock
    private FilingHistoryDocumentMetadataUpdateApi filingHistoryDocumentMetadataUpdateApi;
    @Mock
    private DocumentStoreDelta documentStoreDelta;
    @Mock
    private CreateDocumentResponseApi createDocumentResponseApi;

    @ParameterizedTest
    @CsvSource({CONTENT_TYPE_PDF, CONTENT_TYPE_ZIP})
    void processDocumentStoreDeltaAndUpdateFilingHistoryMetadataForPDF(String contentType) {
        // given
        when(createDocumentResponseApi.getContentType()).thenReturn(contentType);
        when(filingHistoryDocumentMetadataTransformer.transform(createDocumentResponseApi))
                .thenReturn(filingHistoryDocumentMetadataUpdateApi);
        when(filingHistoryDocumentMetadataTransformer.transformFilingHistoryId(documentStoreDelta))
                .thenReturn(FILING_HISTORY_ID);
        when(documentStoreDelta.getCompanyNumber()).thenReturn(COMPANY_NUMBER);

        // when
        filingHistoryDocumentMetadataUpdateProcessor.process(documentStoreDelta, createDocumentResponseApi);

        // then
        assertEquals(contentType, createDocumentResponseApi.getContentType());
        assertEquals(COMPANY_NUMBER, documentStoreDelta.getCompanyNumber());
        verify(filingHistoryDocumentMetadataTransformer).transform(createDocumentResponseApi);
        verify(filingHistoryDocumentMetadataTransformer).transformFilingHistoryId(documentStoreDelta);
        verify(filingHistoryDocumentMetadataApiClient).updateDocumentMetadataLink(
                filingHistoryDocumentMetadataUpdateApi, COMPANY_NUMBER, FILING_HISTORY_ID);
    }

    @Test
    void shouldNotProcessDocumentStoreDeltaIfInvalidContentType() {
        // given
        when(createDocumentResponseApi.getContentType()).thenReturn(CONTENT_TYPE_XML);

        /// when
        filingHistoryDocumentMetadataUpdateProcessor.process(documentStoreDelta, createDocumentResponseApi);

        // then
        verifyNoInteractions(filingHistoryDocumentMetadataTransformer);
        verifyNoInteractions(filingHistoryDocumentMetadataApiClient);
    }}
