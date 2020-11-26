package io.apicurio.registry.utils.serde.cloudevents;

public interface EditableCloudEventsAttributes extends CloudEventAttributes {

    void setDataschema(String dataschema);

}
