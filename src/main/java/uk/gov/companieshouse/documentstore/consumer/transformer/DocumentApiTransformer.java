package uk.gov.companieshouse.documentstore.consumer.transformer;

import consumer.exception.RetryableErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.DocumentStoreDelta;
import uk.gov.companieshouse.api.model.document.CreateDocumentApi;
import uk.gov.companieshouse.documentstore.consumer.mapper.DocumentApiMapper;

@Component
public class DocumentApiTransformer {

    private final DocumentApiMapper mapper;

    @Autowired
    public DocumentApiTransformer(DocumentApiMapper mapper) {
        this.mapper = mapper;
    }

    public CreateDocumentApi transform(DocumentStoreDelta delta) throws RetryableErrorException {
        try {
            return mapper.documentStoreDeltaToApi(delta);
        } catch (Exception exception) {
            throw new RetryableErrorException("Unable to map to Document API object", exception);
        }
    }
}
