package uk.gov.companieshouse.documentstore.consumer.mapper;

import uk.gov.companieshouse.api.delta.DocumentStoreDelta;
import uk.gov.companieshouse.api.model.document.CreateDocumentApi;
import uk.gov.companieshouse.api.model.document.CreateDocumentResponseApi;
import uk.gov.companieshouse.documentstore.consumer.exception.NonRetryableException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.time.Instant;

import static uk.gov.companieshouse.documentstore.consumer.Application.NAMESPACE;
import static uk.gov.companieshouse.documentstore.consumer.mapper.DocumentMapperConstants.TRANSACTION_ID_CHIPS_PREFIX;
import static uk.gov.companieshouse.documentstore.consumer.mapper.DocumentMapperConstants.TRANSACTION_ID_CHIPS_PREFIX_REGEX;

public class DocumentApiMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    private static final String INVALID_PAGES_MESSAGE = "Invalid pages value in delta, pages=[%d]";
    private static final String INVALID_PAGES_FORMAT_MESSAGE = "Invalid format for pages value in delta, pages=[%s]";

    // Regex copied directly from perl equivalent, ensures filename ends with ".tif", ".tiff", ."pdf" or ".zip" to be
    // allowed to have a value for "pages" field
    private static final String VALID_PAGES_FILENAME_ENDING_REGEX = "\\.(?:tif(f)?|pdf|zip)$";

    public DocumentApiMapper() {}

    public CreateDocumentApi documentStoreDeltaToApi(DocumentStoreDelta delta) {
        CreateDocumentApi api = new CreateDocumentApi();
        api.setBarcode(delta.getBarcode());
        api.setCategory(getMappedCategory(delta));
        api.setFilename(delta.getFilename());
        api.setPages(getMappedPages(delta));
        api.setCompanyNumber(delta.getCompanyNumber());
        api.setSignificantDate(getMappedSignificantDate(delta));
        api.setSignificantDateType(getMappedSignificantDateType(delta));
        api.setStoredImageUrl(delta.getStoredImageUrl());
        api.setTransactionId(getMappedTransactionId(delta));
        return api;
    }

    private String getMappedCategory(DocumentStoreDelta delta) {
        return delta.getCategory().toString();
    }

    private Integer getMappedPages(DocumentStoreDelta delta) {
        if (delta.getPages() == null) {
            return null; // no pages value is valid so just return null
        }

        if (!canStoredImageUrlHavePages(delta.getStoredImageUrl())) {
            return null; // only certain file types can have page count, others cannot have a page count even if it is set in the delta
        }

        try {
            int parsedPages = Integer.parseInt(delta.getPages());

            if (parsedPages > 0) {
                return parsedPages; // positive page count is valid
            }
            if (parsedPages == 0) {
                return null; // page count of 0 is invalid in document API but expected in delta so return null if 0 is the parsed value
            }

            // negative page count is invalid and non-retryable
            throw new NonRetryableException(String.format(INVALID_PAGES_MESSAGE, parsedPages));
        } catch (NumberFormatException nfe) {
            // invalid format in pages value is non-retryable
            throw new NonRetryableException(String.format(INVALID_PAGES_FORMAT_MESSAGE, delta.getPages()), nfe);
        }
    }

    private boolean canStoredImageUrlHavePages(String storedImageUrl) {
        if (storedImageUrl == null) {
            return false;
        }
        return storedImageUrl.matches(VALID_PAGES_FILENAME_ENDING_REGEX);
    }

    private Instant getMappedSignificantDate(DocumentStoreDelta delta) {
        return Instant.parse(delta.getSignificantDate());
    }

    private String getMappedSignificantDateType(DocumentStoreDelta delta) {
        return delta.getSignificantDateType().toString();
    }

    private String getMappedTransactionId(DocumentStoreDelta delta) {
        String transactionId = delta.getTransactionId();

        // ensure "CHIPS:" prefix exists without duplicating it
        if (!transactionId.matches(TRANSACTION_ID_CHIPS_PREFIX_REGEX)) {
            transactionId = TRANSACTION_ID_CHIPS_PREFIX + transactionId;
        }

        return transactionId;
    }
}
