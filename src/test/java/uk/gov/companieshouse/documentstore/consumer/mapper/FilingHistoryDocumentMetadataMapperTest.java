package uk.gov.companieshouse.documentstore.consumer.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.delta.DocumentStoreDelta;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryDocumentMetadataUpdateApi;
import uk.gov.companieshouse.api.model.document.CreateDocumentResponseApi;

@ExtendWith(MockitoExtension.class)
class FilingHistoryDocumentMetadataMapperTest {

    private static final String DOCUMENT_METADATA = "/document/%s";
    private static final String DOCUMENT_ID = "DOC1234";
    private static final String FILING_HISTORY_ID = "QkNERTIzNDVudWxs";

    @InjectMocks
    FilingHistoryDocumentMetadataMapper filingHistoryDocumentMetadataMapper;

    @Test
    void testFilingHistoryMetadataUpdateFromCreateDocumentResponse() {
        // given
        CreateDocumentResponseApi response = new CreateDocumentResponseApi();
        response.setPages(2);
        response.setDocumentId(DOCUMENT_ID);

        // when
        FilingHistoryDocumentMetadataUpdateApi actual = filingHistoryDocumentMetadataMapper.mapCreateDocumentResponseToLinksUpdate(response);

        // then
        assertEquals(DOCUMENT_METADATA.formatted(DOCUMENT_ID), actual.getDocumentMetadata());
        assertEquals(2, actual.getPages());
    }

    @Test
    void testFilingHistoryMetadataUpdateFromCreateDocumentResponseZeroPages() {
        // given
        CreateDocumentResponseApi response = new CreateDocumentResponseApi();
        response.setDocumentId(DOCUMENT_ID);
        response.setPages(0);

        // when
        FilingHistoryDocumentMetadataUpdateApi actual = filingHistoryDocumentMetadataMapper.mapCreateDocumentResponseToLinksUpdate(response);

        // then
        assertEquals(DOCUMENT_METADATA.formatted(DOCUMENT_ID), actual.getDocumentMetadata());
    }

    @Test
    void testFilingHistoryMapsFilingHistoryId() {
        // given
        DocumentStoreDelta delta = new DocumentStoreDelta();
        delta.setTransactionId("ABCD1234");
        delta.setParentTransactionId("BCDE2345");
        delta.setDocumentType("ANNOTATION");
        delta.setDocumentType("ANNOTATION");
        delta.setBarcode("XBC1234");

        // when
        String actual = filingHistoryDocumentMetadataMapper.mapFilingHistoryId(delta);

        // then
        assertEquals(FILING_HISTORY_ID, actual);
    }

}
