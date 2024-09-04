package uk.gov.companieshouse.documentstore.consumer.apiclient;

import static uk.gov.companieshouse.documentstore.consumer.Application.NAMESPACE;

import java.util.Arrays;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.documentstore.consumer.exception.NonRetryableException;
import uk.gov.companieshouse.documentstore.consumer.exception.RetryableException;
import uk.gov.companieshouse.documentstore.consumer.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class DocumentApiResponseHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private static final String API_ERROR_RESPONSE_MESSAGE = "HTTP response code %d when creating document";
    private static final String URI_VALIDATION_EXCEPTION_MESSAGE = "Failed to create document due to invalid URI";
    private static final String NULL_RESPONSE_EXCEPTION_MESSAGE = "Null response when creating document";
    private static final String UNEXPECTED_RESPONSE_CODE_MESSAGE = "Unexpected HTTP response code %d when creating document";

    public void handle(ApiErrorResponseException ex) {

        final int statusCode = ex.getStatusCode();
        String message = API_ERROR_RESPONSE_MESSAGE.formatted(statusCode);

        if (HttpStatus.BAD_REQUEST.value() == ex.getStatusCode() || HttpStatus.CONFLICT.value() == ex.getStatusCode()) {
            LOGGER.error(message, ex, DataMapHolder.getLogMap());
            throw new NonRetryableException(message, ex);
        } else {
            LOGGER.info(String.format("%s. %s", message, Arrays.toString(ex.getStackTrace())),
                    DataMapHolder.getLogMap());
            throw new RetryableException(message, ex);
        }
    }

    public void handle(URIValidationException ex) {
        LOGGER.error(URI_VALIDATION_EXCEPTION_MESSAGE, ex, DataMapHolder.getLogMap());
        throw new NonRetryableException(URI_VALIDATION_EXCEPTION_MESSAGE, ex);
    }

    public void handleNullResponse() {
        LOGGER.info(NULL_RESPONSE_EXCEPTION_MESSAGE, DataMapHolder.getLogMap());
        throw new RetryableException(NULL_RESPONSE_EXCEPTION_MESSAGE);
    }

    public void handleUnexpectedStatusCode(int statusCode) {
        LOGGER.info(UNEXPECTED_RESPONSE_CODE_MESSAGE.formatted(statusCode), DataMapHolder.getLogMap());
        throw new NonRetryableException(UNEXPECTED_RESPONSE_CODE_MESSAGE.formatted(statusCode));
    }
}
