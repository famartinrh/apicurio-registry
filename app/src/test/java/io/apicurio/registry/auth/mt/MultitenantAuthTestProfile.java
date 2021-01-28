package io.apicurio.registry.auth.mt;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;

public class MultitenantAuthTestProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Collections.emptyMap();
    }

    @Override
    public List<TestResourceEntry> testResources() {
        return Arrays.asList(
                new TestResourceEntry(MultitenantKeycloakTestResource.class));
    }

}