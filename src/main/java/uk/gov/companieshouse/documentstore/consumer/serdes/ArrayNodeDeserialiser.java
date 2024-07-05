package uk.gov.companieshouse.documentstore.consumer.serdes;

import static uk.gov.companieshouse.documentstore.consumer.Application.NAMESPACE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.util.List;
import uk.gov.companieshouse.documentstore.consumer.exception.NonRetryableException;
import uk.gov.companieshouse.documentstore.consumer.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

public class ArrayNodeDeserialiser<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private final ObjectMapper mapper;
    private final Class<T> type;

    protected ArrayNodeDeserialiser(ObjectMapper mapper, Class<T> type) {
        this.mapper = mapper;
        this.type = type;
    }

    public List<T> deserialise(ArrayNode arrayNode) {
        try {
            return mapper.readerForListOf(type).readValue(arrayNode);
        } catch (IOException ex) {
            LOGGER.error("Unable to deserialise array node: [%s]".formatted(arrayNode.toPrettyString()),
                    ex,
                    DataMapHolder.getLogMap());
            throw new NonRetryableException(
                    "Unable to deserialise array node: [%s]".formatted(arrayNode.toPrettyString()), ex);
        }
    }
}
