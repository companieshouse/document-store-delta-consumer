package uk.gov.companieshouse.documentstore.consumer.transformer;

import consumer.exception.RetryableErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.companieshouse.api.delta.DocumentStoreDelta;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryDocumentMetadataUpdateApi;
import uk.gov.companieshouse.api.model.document.CreateDocumentResponseApi;
import uk.gov.companieshouse.documentstore.consumer.mapper.FilingHistoryDocumentMetadataMapper;

public class FilingHistoryDocumentMetadataTransformer {

    private final FilingHistoryDocumentMetadataMapper mapper;

    @Autowired
    public FilingHistoryDocumentMetadataTransformer(FilingHistoryDocumentMetadataMapper mapper) {
        this.mapper = mapper;
    }

    public FilingHistoryDocumentMetadataUpdateApi transform(CreateDocumentResponseApi response) throws RetryableErrorException {
        try {
            return mapper.createDocumentResponseToLinksUpdate(response);
        } catch (Exception exception) {
            throw new RetryableErrorException("Unable to map to Filing History Document Metadata object", exception);
        }
    }

    public String transformFilingHistoryId(DocumentStoreDelta delta, CreateDocumentResponseApi response) throws RetryableErrorException {
        try {
            return mapper.mapFilingHistoryId(delta, response);
        } catch (Exception exception) {
            throw new RetryableErrorException("Unable to map to Filing History Id", exception);
        }
    }
}
