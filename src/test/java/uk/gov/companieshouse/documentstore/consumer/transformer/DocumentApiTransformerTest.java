package uk.gov.companieshouse.documentstore.consumer.transformer;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.delta.DocumentStoreDelta;
import uk.gov.companieshouse.api.model.document.CreateDocumentApi;
import uk.gov.companieshouse.documentstore.consumer.mapper.DocumentApiMapper;

@ExtendWith(MockitoExtension.class)
class DocumentApiTransformerTest {

    @Mock
    DocumentApiMapper documentApiMapper;
    @InjectMocks
    DocumentApiTransformer documentApiTransformer;
    @Mock
    DocumentStoreDelta documentStoreDelta;
    @Mock
    CreateDocumentApi createDocumentApi;

    @Test
    void transformDocumentStoreDeltaToCreateDocumentApi() {
        // given
        when(documentApiMapper.documentStoreDeltaToApi(documentStoreDelta)).thenReturn(createDocumentApi);

        // when
        documentApiTransformer.transform(documentStoreDelta);

        // then
        verify(documentApiMapper).documentStoreDeltaToApi(documentStoreDelta);
    }
}
