/*
 * Copyright 2020 Red Hat
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apicurio.registry.utils.cloudevents;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.worldturner.medeia.api.StringSchemaSource;
import com.worldturner.medeia.api.jackson.MedeiaJacksonApi;
import com.worldturner.medeia.schema.validation.SchemaValidator;

import io.apicurio.registry.client.RegistryRestClient;
import io.apicurio.registry.utils.IoUtil;

/**
 * @author Fabian Martinez
 */
public class JsonSchemaCloudEventsSerde {

        protected static MedeiaJacksonApi api = new MedeiaJacksonApi();
        protected static ObjectMapper mapper = new ObjectMapper();

        private RegistryRestClient registryClient;
        private DataSchemaCache<SchemaValidator> schemaValidatorCache;

        public JsonSchemaCloudEventsSerde(RegistryRestClient registryClient) {
            this.registryClient = registryClient;
        }

        public <T> T readData(CloudEvent<byte[]> cloudevent, Class<T> clazz) {
            if (cloudevent.data() == null) {
                return null;
            }

            try {
                DataSchemaEntry<SchemaValidator> dataschema = getSchemaValidatorCache().getSchema(cloudevent);
                SchemaValidator schemaValidator = dataschema.getSchema();


                JsonParser parser = mapper.getFactory().createParser(cloudevent.data());
                parser = api.decorateJsonParser(schemaValidator, parser);

                return mapper.readValue(parser, clazz);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        public <T> ParsedData<T> readParsedData(CloudEvent<byte[]> cloudevent, Class<T> clazz) {
            if (cloudevent.data() == null) {
                return null;
            }

            try {
                DataSchemaEntry<SchemaValidator> dataschema = getSchemaValidatorCache().getSchema(cloudevent);
                SchemaValidator schemaValidator = dataschema.getSchema();


                JsonParser parser = mapper.getFactory().createParser(cloudevent.data());
                parser = api.decorateJsonParser(schemaValidator, parser);

                T data = mapper.readValue(parser, clazz);

                return new ParsedData<T>(dataschema.getDataSchema(), data);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        public <T> CloudEvent<byte[]> writeData(CloudEvent<T> cloudevent, T data) {
            if (data == null) {
                return CloudEventImpl.<byte[]>from(cloudevent);
            }

            try {
                DataSchemaEntry<SchemaValidator> dataschema = getSchemaValidatorCache().getSchema(cloudevent);
                SchemaValidator schemaValidator = dataschema.getSchema();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                JsonGenerator generator = mapper.getFactory().createGenerator(baos);
                generator = api.decorateJsonGenerator(schemaValidator, generator);

                mapper.writeValue(generator, data);

                return CloudEventImpl.<byte[]>from(cloudevent)
                        .toBuilder()
                            .withDatacontenttype("application/json")
                            .withDataschema(dataschema.getDataSchema())
                            .withData(baos.toByteArray())
                            .build();

            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        private synchronized DataSchemaCache<SchemaValidator> getSchemaValidatorCache() {
            if (schemaValidatorCache == null) {
                schemaValidatorCache = new DataSchemaCache<SchemaValidator>(registryClient) {
                    @Override
                    protected SchemaValidator toSchema(InputStream rawSchema) {
                        return api.loadSchema(new StringSchemaSource(IoUtil.toString(rawSchema)));
                    }
                };
            }
            return schemaValidatorCache;
        }

}
