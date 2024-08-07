package uk.gov.companieshouse.documentstore.consumer.serdes;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.api.filinghistory.AltCapitalDescriptionValue;
import uk.gov.companieshouse.api.filinghistory.CapitalDescriptionValue;

@Configuration
public class SerdesConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .registerModule(new JavaTimeModule());
    }

    @Bean
    public ArrayNodeDeserialiser<String> stringArrayNodeDeserialiser(ObjectMapper objectMapper) {
        return new ArrayNodeDeserialiser<>(objectMapper, String.class);
    }

    @Bean
    public ArrayNodeDeserialiser<CapitalDescriptionValue> capitalArrayNodeDeserialiser(ObjectMapper objectMapper) {
        return new ArrayNodeDeserialiser<>(objectMapper, CapitalDescriptionValue.class);
    }

    @Bean
    public ArrayNodeDeserialiser<AltCapitalDescriptionValue> altCapitalArrayNodeDeserialiser(
            ObjectMapper objectMapper) {
        return new ArrayNodeDeserialiser<>(objectMapper, AltCapitalDescriptionValue.class);
    }
}
