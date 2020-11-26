package io.apicurio.registry.utils.serde.cloudevents;

import java.nio.charset.StandardCharsets;

import org.apache.kafka.common.header.Headers;

public class KafkaEditableCloudEventsAttributes extends KafkaCloudEventAttributes implements EditableCloudEventsAttributes {

    public KafkaEditableCloudEventsAttributes(Headers headers) {
        super(headers);
    }

    @Override
    public void setDataschema(String dataschema) {
        headers.add(CE_PREFIX + "dataschema", dataschema.getBytes(StandardCharsets.UTF_8));
    }

}
