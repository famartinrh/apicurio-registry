package io.apicurio.registry.utils.serde.cloudevents;

/**
 * @author Fabian Martinez
 */
public interface CloudEvent<T> extends CloudEventAttributes {

    T data();

}

