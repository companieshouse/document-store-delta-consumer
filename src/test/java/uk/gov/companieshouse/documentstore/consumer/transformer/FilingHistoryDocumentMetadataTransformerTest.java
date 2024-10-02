package uk.gov.companieshouse.documentstore.consumer.transformer;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.delta.DocumentStoreDelta;
import uk.gov.companieshouse.api.model.document.CreateDocumentResponseApi;
import uk.gov.companieshouse.documentstore.consumer.mapper.FilingHistoryDocumentMetadataMapper;

@ExtendWith(MockitoExtension.class)
public class FilingHistoryDocumentMetadataTransformerTest {

    @Mock
    FilingHistoryDocumentMetadataMapper filingHistoryDocumentMetadataMapper;
    @InjectMocks FilingHistoryDocumentMetadataTransformer filingHistoryDocumentMetadataTransformer;
    @Mock
    CreateDocumentResponseApi createDocumentResponseApi;
    @Mock
    DocumentStoreDelta documentStoreDelta;

    @Test
    void createFilingHistoryMetadataUpdateRequestFromResponse() {
        // given

        // when
        filingHistoryDocumentMetadataTransformer.transform(createDocumentResponseApi);

        // then
        verify(filingHistoryDocumentMetadataMapper).mapCreateDocumentResponseToLinksUpdate(createDocumentResponseApi);
    }

    @Test
    void createFilingHistoryIdFromDocumentStoreDElta() {
        // given

        // when
        filingHistoryDocumentMetadataTransformer.transformFilingHistoryId(documentStoreDelta);

        // then
        verify(filingHistoryDocumentMetadataMapper).mapFilingHistoryId(documentStoreDelta);
    }
}
