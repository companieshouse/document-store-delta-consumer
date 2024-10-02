package uk.gov.companieshouse.documentstore.consumer.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.delta.DocumentStoreDelta;
import uk.gov.companieshouse.api.model.document.CreateDocumentApi;
import uk.gov.companieshouse.api.model.document.CreateDocumentResponseApi;
import uk.gov.companieshouse.documentstore.consumer.apiclient.DocumentApiClient;
import uk.gov.companieshouse.documentstore.consumer.transformer.DocumentApiTransformer;

@ExtendWith(MockitoExtension.class)
public class DocumentStoreDeltaProcessorTest {
    @Mock
    private DocumentApiTransformer documentApiTransformer;
    @Mock
    private DocumentApiClient documentApiClient;
    private DocumentStoreDeltaProcessor documentStoreDeltaProcessor;
    @Mock
    private DocumentStoreDelta documentStoreDelta;
    @Mock
    private CreateDocumentApi createDocumentApi;
    @Mock
    private CreateDocumentResponseApi createDocumentResponseApi;

    @BeforeEach
    void setUp() {
        documentStoreDeltaProcessor = new DocumentStoreDeltaProcessor(true,
                documentApiTransformer, documentApiClient);
    }

    @Test
    void processDocumentStoreDeltaAndReturnResponse() {
        // given
        when(documentApiTransformer.transform(documentStoreDelta)).thenReturn(createDocumentApi);

        // when
        documentStoreDeltaProcessor.processDelta(documentStoreDelta);

        // then
        verify(documentApiTransformer).transform(documentStoreDelta);
        verify(documentApiClient).createDocument(createDocumentApi, true);
    }
}
