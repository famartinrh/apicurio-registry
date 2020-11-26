package io.apicurio.registry.utils.serde.cloudevents;

import java.time.OffsetDateTime;

/**
 * @author Fabian Martinez
 */
public interface CloudEventAttributes {

    String id();

    String specVersion();

    String source();

    String subject();

    OffsetDateTime time();

    String type();

    String datacontenttype();

    String dataschema();

}

