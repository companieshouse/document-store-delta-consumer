package uk.gov.companieshouse.documentstore.consumer.mapper;

import java.util.regex.Pattern;

final class DocumentMapperConstants {

    static final Pattern TRANSACTION_ID_CHIPS_PREFIX_REGEX = Pattern.compile("^CHIPS:");
    static final String TRANSACTION_ID_CHIPS_PREFIX = "CHIPS:";

    private DocumentMapperConstants() {
    }

}
