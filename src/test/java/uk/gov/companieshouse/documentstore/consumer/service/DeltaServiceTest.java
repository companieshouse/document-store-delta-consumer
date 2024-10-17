package uk.gov.companieshouse.documentstore.consumer.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.delta.DocumentStoreDelta;
import uk.gov.companieshouse.api.model.document.CreateDocumentResponseApi;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.documentstore.consumer.serdes.DocumentStoreDeltaDeserialiser;

@ExtendWith(MockitoExtension.class)
class DeltaServiceTest {

    @Mock
    private DocumentStoreDeltaDeserialiser documentStoreDeltaDeserialiser;
    @Mock
    private DocumentStoreDeltaProcessor documentStoreDeltaProcessor;
    @Mock
    private FilingHistoryDocumentMetadataUpdateProcessor filingHistoryDocumentMetadataUpdateProcessor;
    @InjectMocks
    private DeltaService deltaService;
    @Mock
    private ChsDelta chsDelta;
    @Mock
    private DocumentStoreDelta documentStoreDelta;
    @Mock
    private CreateDocumentResponseApi createDocumentResponseApi;

    @Test
    void processDocumentStoreDelta() {
        // given
        when(chsDelta.getData()).thenReturn("data");
        when(documentStoreDeltaDeserialiser.deserialiseDocumentStoreDelta(anyString())).thenReturn(documentStoreDelta);
        when(documentStoreDeltaProcessor.processDelta(documentStoreDelta)).thenReturn(createDocumentResponseApi);

        // when
        deltaService.process(chsDelta);

        // then
        verify(documentStoreDeltaDeserialiser).deserialiseDocumentStoreDelta("data");
        verify(documentStoreDeltaProcessor).processDelta(documentStoreDelta);
        verify(filingHistoryDocumentMetadataUpdateProcessor).process(documentStoreDelta, createDocumentResponseApi);
    }

}
