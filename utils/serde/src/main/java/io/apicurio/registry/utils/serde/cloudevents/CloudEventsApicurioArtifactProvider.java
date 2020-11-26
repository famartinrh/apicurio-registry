package io.apicurio.registry.utils.serde.cloudevents;

import java.io.InputStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.apicurio.registry.client.RegistryRestClient;

public class CloudEventsApicurioArtifactProvider {

    private RegistryRestClient client;

    public CloudEventsApicurioArtifactProvider(RegistryRestClient client) {
        this.client = client;
    }

    //TODO investigate to do this with only one request to registry api
    public InputStream getArtifact(CloudEventAttributes cloudEvent) {
        Long globalId = lookupGlobalId(cloudEvent);
        try {
            return client.getArtifactByGlobalId(globalId);
        } catch (Exception e) {
            throw new IllegalStateException(
                String.format(
                    "Error retrieving schema: %s",
                    cloudEvent.dataschema()
                ),
                e
            );
        }
    }

    private Long lookupGlobalId(CloudEventAttributes event) {
        if (event.dataschema() != null) {
            return getGlobalIdByDataSchema(event.dataschema());
        } else {
            String artifactId = event.type();
            return client.getArtifactMetaData(artifactId).getGlobalId();
        }
    }

    private Long getGlobalIdByDataSchema(String dataschema) {
        if (dataschema.startsWith("/apicurio")) {
            String[] apicurioArtifactTokens = Stream.of(dataschema.split("/"))
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.toList()).toArray(new String[0]);
            String artifactId = null;
            if (apicurioArtifactTokens.length > 1) {
                artifactId = apicurioArtifactTokens[1];
            }
            String version = null;
            if (apicurioArtifactTokens.length > 2) {
                version = apicurioArtifactTokens[2];
            }
            if (artifactId == null) {
                throw new IllegalStateException("Bad apicurio dataschema URI");
            }
            if (version == null) {
                //this case should not be cached
                return client.getArtifactMetaData(artifactId).getGlobalId();
            } else {
                if (version.length() > 1 && version.toLowerCase().startsWith("v")) {
                    version = version.substring(1);
                }
                Integer artifactVersion = Integer.parseInt(version);
                try {
                    return client.getArtifactVersionMetaData(artifactId, artifactVersion).getGlobalId();
                } catch (Exception e) {
                    throw new IllegalStateException("Artifact not found", e);
                }
            }
        } else if (dataschema.startsWith("apicurio-global-id-")) {
            String apicurioGlobalId = dataschema.substring("apicurio-global-id-".length());
            return Long.parseLong(apicurioGlobalId);
        }
        throw new IllegalArgumentException("Unable to find schema "+dataschema);
    }

}
