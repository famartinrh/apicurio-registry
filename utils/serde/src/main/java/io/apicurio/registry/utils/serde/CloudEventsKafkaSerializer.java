package io.apicurio.registry.utils.serde;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Optional;

import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.worldturner.medeia.api.jackson.MedeiaJacksonApi;
import com.worldturner.medeia.schema.validation.SchemaValidator;

import io.apicurio.registry.utils.serde.cloudevents.CloudEventsApicurioArtifactProvider;

public class CloudEventsKafkaSerializer<T> extends AbstractKafkaSerDe<CloudEventsKafkaSerializer<T>> implements Serializer<T> {

    public static final String CONTENT_TYPE = "content-type";


    private CloudEventsApicurioArtifactProvider artifacProvider;
    private MedeiaJacksonApi api;
    private ObjectMapper mapper;

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        super.configure(configs, isKey);
        artifacProvider = new CloudEventsApicurioArtifactProvider(getClient());
    }

    @Override
    public byte[] serialize(String topic, T data) {
        // Headers are required when sending data using this serde impl
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] serialize(String topic, Headers headers, T data) {
        // just return null
        if (data == null) {
            return null;
        }
        try {

            String contentType = Optional.ofNullable(headers.headers(CONTENT_TYPE))
                    .map(Iterable::iterator)
                    .map(it -> {
                        return it.hasNext() ? it.next() : null;
                    })
                    .map(Header::value)
                    .map(String::new)
                    .orElse(null);

            if (contentType == null || contentType.isEmpty()) {
                throw new IllegalArgumentException("missing content-type header");
            }

            if ("application/json".equals(contentType)) {

            } else if (contentType.contains("avro")) {

            } else if (contentType.contains("protobuf")) {

            } else {
                throw new IllegalArgumentException("Unknown content-type "+contentType);
            }

            String artifactId = getArtifactIdStrategy().artifactId(topic, isKey(), schema);
            long id = getGlobalIdStrategy().findId(getClient(), artifactId, artifactType(), schema);

            return null;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private byte[] serializeJson() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            JsonGenerator generator = getMapper().getFactory().createGenerator(baos);
//            if (isValidationEnabled()) {
                String artifactId = getArtifactId(topic, data);
                long globalId = getGlobalId(artifactId, topic, data);
                headerUtils.addSchemaHeaders(headers, artifactId, globalId);

                SchemaValidator schemaValidator = getSchemaCache().getSchema(globalId);
                generator = getMedeiaJacksonApi().decorateJsonGenerator(schemaValidator, generator);
//            }
            headerUtils.addMessageTypeHeader(headers, data.getClass().getName());

            getMapper().writeValue(generator, data);

            return baos.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private synchronized ObjectMapper getMapper() {
        if (mapper == null) {
            mapper = new ObjectMapper();
        }
        return mapper;
    }

    private synchronized MedeiaJacksonApi getMedeiaJacksonApi() {
        if (api == null) {
            api = new MedeiaJacksonApi();
        }
        return api;
    }
}
