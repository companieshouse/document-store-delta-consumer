package uk.gov.companieshouse.documentstore.consumer.mapper;

import org.springframework.beans.factory.annotation.Value;
import uk.gov.companieshouse.api.delta.DocumentStoreDelta;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryDocumentMetadataUpdateApi;
import uk.gov.companieshouse.api.filinghistory.utils.TransactionKindCriteria;
import uk.gov.companieshouse.api.filinghistory.utils.TransactionKindResult;
import uk.gov.companieshouse.api.filinghistory.utils.TransactionKindService;
import uk.gov.companieshouse.api.model.document.CreateDocumentResponseApi;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import static uk.gov.companieshouse.documentstore.consumer.Application.NAMESPACE;

public class FilingHistoryDocumentMetadataMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    protected static final String DOCUMENT_METADATA_LINK_PREFIX = "/document/";

    private final TransactionKindService transactionKindService;

    public FilingHistoryDocumentMetadataMapper(@Value("${transaction-id-salt}") String transactionIdSalt) {
        this.transactionKindService = new TransactionKindService(transactionIdSalt);
    }

    public FilingHistoryDocumentMetadataUpdateApi createDocumentResponseToLinksUpdate(CreateDocumentResponseApi response) {
        FilingHistoryDocumentMetadataUpdateApi api = new FilingHistoryDocumentMetadataUpdateApi();
        api.setDocumentMetadata(DOCUMENT_METADATA_LINK_PREFIX + response.getDocumentId());
        if (response.getPages() > 0) {
            api.setPages(response.getPages());
        }
        return api;
    }

    public String mapFilingHistoryId(DocumentStoreDelta delta, CreateDocumentResponseApi response) {
        TransactionKindCriteria criteria = new TransactionKindCriteria(
                delta.getTransactionId(),
                delta.getParentTransactionId(),
                delta.getDocumentType(),
                delta.getParentDocumentType(),
                delta.getBarcode()
        );

        TransactionKindResult result = transactionKindService.encodeIdByTransactionKind(criteria);
        return result.getEncodedId();
    }
}
