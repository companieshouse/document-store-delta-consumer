package uk.gov.companieshouse.documentstore.consumer.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.delta.DocumentStoreDelta;
import uk.gov.companieshouse.api.delta.DocumentStoreDelta.CategoryEnum;
import uk.gov.companieshouse.api.delta.DocumentStoreDelta.SignificantDateTypeEnum;
import uk.gov.companieshouse.api.model.document.CreateDocumentApi;
import uk.gov.companieshouse.documentstore.consumer.exception.NonRetryableException;

@ExtendWith(MockitoExtension.class)
class DocumentApiMapperTest {
    private DocumentApiMapper documentApiMapper;

    @BeforeEach
    void setup() {
        documentApiMapper = new DocumentApiMapper();
    }

    @Test
    void mapDocumentStoreDeltaToFullCreateDocumentRequest() {
        // given
        DocumentStoreDelta delta = getDocumentStoreDelta(true, "D12345678", 5, "pdf");

        // when
        CreateDocumentApi actual = documentApiMapper.documentStoreDeltaToApi(delta);

        // then
        assertEquals("CHIPS:D12345678", actual.getTransactionId());
        assertEquals("accounts", actual.getCategory());
        assertEquals("X1234567", actual.getBarcode());
        assertEquals("00006400", actual.getCompanyNumber());
        assertEquals("2014-09-24T00:00:00.00Z", actual.getSignificantDate());
        assertEquals("made-up-date", actual.getSignificantDateType());
        assertEquals("testurl.com/filename.pdf", actual.getStoredImageUrl());
        assertEquals("filename", actual.getFilename());
        assertEquals(5, actual.getPages());
    }

    @Test
    void mapDocumentStoreDeltaToPartialCreateDocumentRequest() {
        // given
        DocumentStoreDelta delta = getDocumentStoreDelta(false, "CHIPS:D12345678", 0, null);

        // when
        CreateDocumentApi actual = documentApiMapper.documentStoreDeltaToApi(delta);

        // then
        assertEquals("CHIPS:D12345678", actual.getTransactionId());
        assertEquals("accounts", actual.getCategory());
        assertEquals("X1234567", actual.getBarcode());
        assertEquals("00006400", actual.getCompanyNumber());
        assertNull(actual.getSignificantDate());
        assertNull(actual.getSignificantDateType());
        assertNull( actual.getStoredImageUrl());
        assertEquals("filename", actual.getFilename());
        assertNull(actual.getPages());
    }

    @Test
    void mapDocumentStoreDeltaToMinimumCreateDocumentRequest() {
        // given
        DocumentStoreDelta delta = getMinimumDocumentStoreDelta();

        // when
        CreateDocumentApi actual = documentApiMapper.documentStoreDeltaToApi(delta);

        // then
        assertEquals("CHIPS:D12345678", actual.getTransactionId());
        assertEquals("accounts", actual.getCategory());
        assertEquals("X1234567", actual.getBarcode());
        assertEquals("00006400", actual.getCompanyNumber());
    }

    @Test
    void mapDocumentStoreDeltaCreateDocumentRequestInvalidContentType() {
        // given
        DocumentStoreDelta delta = getDocumentStoreDelta(true, "CHIPS:D12345678", 3, "rar");

        // when
        CreateDocumentApi actual = documentApiMapper.documentStoreDeltaToApi(delta);

        // then
        assertEquals("CHIPS:D12345678", actual.getTransactionId());
        assertEquals("accounts", actual.getCategory());
        assertEquals("X1234567", actual.getBarcode());
        assertEquals("00006400", actual.getCompanyNumber());
        assertEquals("2014-09-24T00:00:00.00Z", actual.getSignificantDate());
        assertEquals("made-up-date", actual.getSignificantDateType());
        assertEquals("testurl.com/filename.rar", actual.getStoredImageUrl());
        assertEquals("filename", actual.getFilename());
        assertNull(actual.getPages());
    }

    @Test
    void mapDocumentStoreDeltaCreateDocumentRequestNegativePageCount() {
        // given
        DocumentStoreDelta delta = getDocumentStoreDelta(true, "CHIPS:D12345678", -1, "pdf");

        // when
        Executable actual = () -> documentApiMapper.documentStoreDeltaToApi(delta);

        // then
        assertThrows(NonRetryableException.class, actual);
    }

    private DocumentStoreDelta getDocumentStoreDelta(boolean fullDelta, String transactionId,
            int pageCount, String contentType) {
        DocumentStoreDelta delta = new DocumentStoreDelta();
        delta.setTransactionId(transactionId);
        delta.setCategory(CategoryEnum.ACCOUNTS);
        delta.setBarcode("X1234567");
        delta.setCompanyNumber("00006400");
        delta.setFilename("filename");
        delta.setPageCount(new BigDecimal(pageCount));
        if (fullDelta) {
            delta.setStoredImageUrl("testurl.com/filename." + contentType);
            delta.setSignificantDate("2014-09-24'T'00:00:00.000Z");
            delta.setSignificantDateType(SignificantDateTypeEnum.MADE_UP_DATE);
        }
        return delta;
    }

    private DocumentStoreDelta getMinimumDocumentStoreDelta() {
        DocumentStoreDelta delta = new DocumentStoreDelta();
        delta.setTransactionId("CHIPS:D12345678");
        delta.setCategory(CategoryEnum.ACCOUNTS);
        delta.setBarcode("X1234567");
        delta.setCompanyNumber("00006400");
        return delta;
    }
}
