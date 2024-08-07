package uk.gov.companieshouse.documentstore.consumer.mapper;

import com.google.common.base.Strings;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.DocumentStoreDelta;
import uk.gov.companieshouse.api.model.document.CreateDocumentApi;
import uk.gov.companieshouse.documentstore.consumer.exception.NonRetryableException;
import uk.gov.companieshouse.documentstore.consumer.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import static uk.gov.companieshouse.documentstore.consumer.Application.NAMESPACE;
import static uk.gov.companieshouse.documentstore.consumer.mapper.DocumentMapperConstants.TRANSACTION_ID_CHIPS_PREFIX;
import static uk.gov.companieshouse.documentstore.consumer.mapper.DocumentMapperConstants.TRANSACTION_ID_CHIPS_PREFIX_REGEX;

@Component
public class DocumentApiMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private static final String INVALID_PAGES_MESSAGE = "Invalid pages value in delta, pages=[%d]";
    private static final String INVALID_PAGES_FORMAT_MESSAGE = "Invalid format for pages value in delta, pages=[%s]";

    // Ensures filename ends with ".tif", ".tiff", ."pdf" or ".zip" to be allowed to have a value for "pages" field
    private static final String VALID_PAGES_FILENAME_ENDING_REGEX = ".*\\.(tif|tiff|pdf|zip)$";

    // date format from CHIPS begins with "yyyy-MM-dd" which is 10 characters to use from start of date string
    private static final int DATE_FORMAT_SUBSTRING_LENGTH = 10;
    // parsing from date requires adding time element to expecting "yyyy-MM-dd" string
    private static final String TIME_MIDNIGHT_UTC = "T00:00:00.00Z";


    public DocumentApiMapper() {}

    public CreateDocumentApi documentStoreDeltaToApi(DocumentStoreDelta delta) {
        LOGGER.trace(String.format("Mapping delta [%s]", delta), DataMapHolder.getLogMap());
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
        LOGGER.trace(String.format("Mapped delta to CreateDocumentApi [%s]", api), DataMapHolder.getLogMap());
        return api;
    }

    private String getMappedCategory(DocumentStoreDelta delta) {
        return delta.getCategory().toString();
    }

    private Integer getMappedPages(DocumentStoreDelta delta) {
        if (delta.getPages() == null) {
            LOGGER.trace("Mapping null pages to null", DataMapHolder.getLogMap());
            return null; // no pages value is valid so just return null
        }

        if (!canStoredImageUrlHavePages(delta.getStoredImageUrl())) {
            LOGGER.trace(String.format("Mapping pages to null due to filetype [%s]", delta.getStoredImageUrl()), DataMapHolder.getLogMap());
            return null; // only certain file types can have page count, others cannot have a page count even if it is set in the delta
        }

        try {
            int parsedPages = Integer.parseInt(delta.getPages());

            if (parsedPages > 0) {
                LOGGER.trace(String.format("Mapping valid pages [%d]", parsedPages), DataMapHolder.getLogMap());
                return parsedPages; // positive page count is valid
            }
            if (parsedPages == 0) {
                LOGGER.trace("Mapping 0 pages to null", DataMapHolder.getLogMap());
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

    private String getMappedSignificantDate(DocumentStoreDelta delta) {
        if (Strings.isNullOrEmpty(delta.getSignificantDate())) {
            LOGGER.trace(String.format("Mapping null or empty significant date [%s]", delta.getSignificantDate()), DataMapHolder.getLogMap());
            return null;
        }

        // date format from CHIPS begins with "yyyy-MM-dd" which is 10 characters to use from start of date string
        // the date is all we care about and time format is inconsistent so ignore any time format provided
        final String sigDateSubstring = delta.getSignificantDate().substring(0, DATE_FORMAT_SUBSTRING_LENGTH);
        LOGGER.trace(String.format("Mapping appended time onto significant date substring [%s]", sigDateSubstring), DataMapHolder.getLogMap());

        // significant date substring format "yyyy-MM-dd" appended with midnight utc time to send to document API
        return sigDateSubstring + TIME_MIDNIGHT_UTC;
    }

    private String getMappedSignificantDateType(DocumentStoreDelta delta) {
        if (delta.getSignificantDateType() == null) {
            LOGGER.trace(String.format("Mapping null significant date type [%s]", delta.getSignificantDateType()), DataMapHolder.getLogMap());
            return null;
        }
        LOGGER.trace(String.format("Mapping present significant date type [%s]", delta.getSignificantDateType()), DataMapHolder.getLogMap());
        return delta.getSignificantDateType().toString();
    }

    private String getMappedTransactionId(DocumentStoreDelta delta) {
        String transactionId = delta.getTransactionId();

        // ensure "CHIPS:" prefix exists without duplicating it
        if (!transactionId.matches(TRANSACTION_ID_CHIPS_PREFIX_REGEX)) {
            LOGGER.trace(String.format("Mapping prefixed transaction id [%s]", transactionId), DataMapHolder.getLogMap());
            return TRANSACTION_ID_CHIPS_PREFIX + transactionId;
        }

        LOGGER.trace(String.format("Mapping direct transaction id [%s]", transactionId), DataMapHolder.getLogMap());
        return transactionId;
    }
}
