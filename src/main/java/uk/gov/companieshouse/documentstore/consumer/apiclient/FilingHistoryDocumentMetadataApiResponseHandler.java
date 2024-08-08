package uk.gov.companieshouse.documentstore.consumer.apiclient;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.documentstore.consumer.exception.NonRetryableException;
import uk.gov.companieshouse.documentstore.consumer.exception.RetryableException;
import uk.gov.companieshouse.documentstore.consumer.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import static uk.gov.companieshouse.documentstore.consumer.Application.NAMESPACE;

@Component
public class FilingHistoryDocumentMetadataApiResponseHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private static final String API_ERROR_RESPONSE_MESSAGE = "HTTP response code %d when setting document metadata link";
    private static final String URI_VALIDATION_EXCEPTION_MESSAGE = "Failed to set document metadata link due to invalid URI";

    public void handle(ApiErrorResponseException ex) {
        final int statusCode = ex.getStatusCode();
        if (isRetryableStatusCode(statusCode)) {
            LOGGER.info(API_ERROR_RESPONSE_MESSAGE.formatted(statusCode), DataMapHolder.getLogMap());
            throw new RetryableException(API_ERROR_RESPONSE_MESSAGE.formatted(statusCode), ex);
        } else {
            LOGGER.error(API_ERROR_RESPONSE_MESSAGE.formatted(statusCode), DataMapHolder.getLogMap());
            throw new NonRetryableException(API_ERROR_RESPONSE_MESSAGE.formatted(statusCode), ex);
        }
    }

    private boolean isRetryableStatusCode(int statusCode) {
        HttpStatus httpStatus = HttpStatus.valueOf(statusCode);
        // retry 5XX errors and 404 errors as 404 could be down to FH delta processing times
        return httpStatus.is5xxServerError() || httpStatus == HttpStatus.NOT_FOUND;
    }

    public void handle(URIValidationException ex) {
        LOGGER.error(URI_VALIDATION_EXCEPTION_MESSAGE, DataMapHolder.getLogMap());
        throw new NonRetryableException(URI_VALIDATION_EXCEPTION_MESSAGE, ex);
    }
}
