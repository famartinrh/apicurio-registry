package io.apicurio.registry.types;

/**
 * @author Ales Justin
 */
public interface SchemaTypeAdapter {
    SchemaWrapper wrapper(String schema);
}