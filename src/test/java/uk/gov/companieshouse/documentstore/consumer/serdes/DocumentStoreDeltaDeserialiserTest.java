package uk.gov.companieshouse.documentstore.consumer.serdes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.delta.DocumentStoreDelta;
import uk.gov.companieshouse.documentstore.consumer.exception.NonRetryableException;

@ExtendWith(MockitoExtension.class)
public class DocumentStoreDeltaDeserialiserTest {
    public static final String DOCUMENT_STORE_DELTA = "document store delta json string";
    @InjectMocks
    private DocumentStoreDeltaDeserialiser deserialiser;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private DocumentStoreDelta expectedDelta;

    @Test
    void shouldDeserialiseRegistersDelta() throws JsonProcessingException {
        // given
        when(objectMapper.readValue(anyString(), eq(DocumentStoreDelta.class))).thenReturn(expectedDelta);

        // when
        DocumentStoreDelta actual = deserialiser.deserialiseDocumentStoreDelta(DOCUMENT_STORE_DELTA);

        // then
        assertEquals(expectedDelta, actual);
        verify(objectMapper).readValue(DOCUMENT_STORE_DELTA, DocumentStoreDelta.class);
    }

    @Test
    void shouldThrowNonRetryableExceptionWhenJsonProcessingExceptionThrown() throws JsonProcessingException {
        // given
        when(objectMapper.readValue(anyString(), eq(DocumentStoreDelta.class))).thenThrow(
                JsonProcessingException.class);

        // when
        Executable executable = () -> deserialiser.deserialiseDocumentStoreDelta(DOCUMENT_STORE_DELTA);

        // then
        NonRetryableException actual = assertThrows(NonRetryableException.class, executable);
        assertEquals("Unable to deserialise delta", actual.getMessage());
        verify(objectMapper).readValue(DOCUMENT_STORE_DELTA, DocumentStoreDelta.class);
    }
}
