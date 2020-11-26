package io.apicurio.registry.utils.serde.cloudevents;

import java.time.OffsetDateTime;

import org.apache.kafka.common.header.Headers;

public class KafkaCloudEventAttributes implements CloudEventAttributes {

    public static final String CE_PREFIX = "ce_";

    protected Headers headers;

    public KafkaCloudEventAttributes(Headers headers) {
        this.headers = headers;
    }

    @Override
    public String id() {
        return new String(headers.lastHeader(CE_PREFIX + "id").value());
    }

    @Override
    public String specVersion() {
        return new String(headers.lastHeader(CE_PREFIX + "specversion").value());
    }

    @Override
    public String source() {
        return new String(headers.lastHeader(CE_PREFIX + "source").value());
    }

    @Override
    public String subject() {
        return new String(headers.lastHeader(CE_PREFIX + "subject").value());
    }

    @Override
    public OffsetDateTime time() {
        return OffsetDateTime.parse(new String(headers.lastHeader(CE_PREFIX + "time").value()));
    }

    @Override
    public String type() {
        return new String(headers.lastHeader(CE_PREFIX + "type").value());
    }

    @Override
    public String datacontenttype() {
        return new String(headers.lastHeader("content-type").value());
    }

    @Override
    public String dataschema() {
        return new String(headers.lastHeader(CE_PREFIX + "dataschema").value());
    }


}
