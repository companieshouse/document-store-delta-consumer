package uk.gov.companieshouse.documentstore.consumer.transformer;

import consumer.exception.RetryableErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.DocumentStoreDelta;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryDocumentMetadataUpdateApi;
import uk.gov.companieshouse.api.model.document.CreateDocumentResponseApi;
import uk.gov.companieshouse.documentstore.consumer.mapper.FilingHistoryDocumentMetadataMapper;

@Component
public class FilingHistoryDocumentMetadataTransformer {

    private final FilingHistoryDocumentMetadataMapper mapper;

    @Autowired
    public FilingHistoryDocumentMetadataTransformer(FilingHistoryDocumentMetadataMapper mapper) {
        this.mapper = mapper;
    }

    public FilingHistoryDocumentMetadataUpdateApi transform(CreateDocumentResponseApi response) throws RetryableErrorException {
        try {
            return mapper.mapCreateDocumentResponseToLinksUpdate(response);
        } catch (Exception exception) {
            throw new RetryableErrorException("Unable to map to Filing History Document Metadata object", exception);
        }
    }

    public String transformFilingHistoryId(DocumentStoreDelta delta) throws RetryableErrorException {
        try {
            return mapper.mapFilingHistoryId(delta);
        } catch (Exception exception) {
            throw new RetryableErrorException("Unable to map to Filing History Id", exception);
        }
    }
}
