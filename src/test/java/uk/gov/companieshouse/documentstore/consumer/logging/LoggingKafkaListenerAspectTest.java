package uk.gov.companieshouse.documentstore.consumer.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.messaging.Message;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.documentstore.consumer.exception.NonRetryableException;
import uk.gov.companieshouse.documentstore.consumer.exception.RetryableException;


@ExtendWith(MockitoExtension.class)
class LoggingKafkaListenerAspectTest {

    private static final String CONTEXT_ID = "context_id";
    private static final String TOPIC = "document-store-delta";

    private LoggingKafkaListenerAspect aspect;

    @Mock
    private ProceedingJoinPoint joinPoint;
    @Mock
    private Message<ChsDelta> message;
    @Mock
    private ChsDelta delta;
    @Mock
    private Message<String> invalidMessage;

    @BeforeEach
    void setUp() {
        aspect = new LoggingKafkaListenerAspect(5);
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void shouldManageStructuredLogging(CapturedOutput capture) throws Throwable {
        // given
        Object[] args = new Object[]{message, null, TOPIC, 0, 0L};
        Object expected = "result";
        when(joinPoint.getArgs()).thenReturn(args);
        when(message.getPayload()).thenReturn(delta);
        when(delta.getContextId()).thenReturn(CONTEXT_ID);
        when(joinPoint.proceed()).thenReturn(expected);

        // when
        Object actual = aspect.manageStructuredLogging(joinPoint);

        //then
        assertEquals(expected, actual);
        assertTrue(capture.getOut().contains("Processed delta"));
        verifyInfoLogMap(capture);
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void shouldLogInfoWhenRetryableException(CapturedOutput capture) throws Throwable {
        // given
        Object[] args = new Object[]{message, null, TOPIC, 0, 0L};
        when(joinPoint.getArgs()).thenReturn(args);
        when(message.getPayload()).thenReturn(delta);
        when(delta.getContextId()).thenReturn(CONTEXT_ID);
        when(joinPoint.proceed()).thenThrow(RetryableException.class);

        // when
        Executable actual = () -> aspect.manageStructuredLogging(joinPoint);

        //then
        assertThrows(RetryableException.class, actual);
        assertTrue(capture.getOut().contains("RetryableException exception thrown"));
        verifyInfoLogMap(capture);
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void shouldLogInfoWhenRetryableExceptionMaxAttempts(CapturedOutput capture) throws Throwable {
        // given
        Object[] args = new Object[]{message, 5, TOPIC, 0, 0L};
        when(joinPoint.getArgs()).thenReturn(args);
        when(message.getPayload()).thenReturn(delta);
        when(delta.getContextId()).thenReturn(CONTEXT_ID);
        when(joinPoint.proceed()).thenThrow(RetryableException.class);

        // when
        Executable actual = () -> aspect.manageStructuredLogging(joinPoint);

        //then
        assertThrows(RetryableException.class, actual);
        assertTrue(capture.getOut().contains("\"event\":\"error\""));
        assertTrue(capture.getOut().contains("\"message\":\"Max retry attempts reached\""));
        assertTrue(capture.getOut().contains("\"request_id\":\"%s\"".formatted(CONTEXT_ID)));
        assertTrue(capture.getOut().contains("\"retry_count\":4"));
        assertTrue(capture.getOut().contains("\"topic\":\"%s\"".formatted(TOPIC)));
        assertTrue(capture.getOut().contains("\"partition\":0"));
        assertTrue(capture.getOut().contains("\"offset\":0"));
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void shouldLogInfoWhenInvalidPayload(CapturedOutput capture) {
        // given
        Object[] args = new Object[]{invalidMessage, null, TOPIC, 0, 0L};
        when(joinPoint.getArgs()).thenReturn(args);
        when(invalidMessage.getPayload()).thenReturn("message payload");

        // when
        Executable actual = () -> aspect.manageStructuredLogging(joinPoint);

        //then
        assertThrows(NonRetryableException.class, actual);
        assertTrue(capture.getOut().contains("\"event\":\"error\""));
        assertTrue(capture.getOut().contains("\"message\":\"Invalid payload type, payload: [message payload]\""));
        assertTrue(capture.getOut().contains("\"request_id\":\"uninitialised\""));
        assertFalse(capture.getOut().contains("retry_count"));
        assertFalse(capture.getOut().contains("topic"));
        assertFalse(capture.getOut().contains("partition"));
        assertFalse(capture.getOut().contains("offset"));
    }

    private static void verifyInfoLogMap(CapturedOutput capture) {
        assertTrue(capture.getOut().contains("\"event\":\"info\""));
        assertTrue(capture.getOut().contains("\"request_id\":\"%s\"".formatted(CONTEXT_ID)));
        assertTrue(capture.getOut().contains("\"retry_count\":0"));
        assertTrue(capture.getOut().contains("\"topic\":\"%s\"".formatted(TOPIC)));
        assertTrue(capture.getOut().contains("\"partition\":0"));
        assertTrue(capture.getOut().contains("\"offset\":0"));
    }
}