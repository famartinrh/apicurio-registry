package io.apicurio.registry.auth.mt;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.apicurio.registry.auth.KeycloakTestResource;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class MultitenantKeycloakTestResource implements QuarkusTestResourceLifecycleManager {

    private static final Logger log = LoggerFactory.getLogger(KeycloakTestResource.class);

    private KeycloakContainer container;

    @Override
    public Map<String, String> start() {
        log.info("Starting Keycloak Test Container");

        container = new KeycloakContainer();
//                .withRealmImportFile("test-realm.json");
        container.start();

        Map<String, String> props = new HashMap<>();
        props.put("registry.keycloak.url", container.getAuthServerUrl());
        props.put("registry.keycloak.realm", "master");
        props.put("registry.auth.enabled", "true");

        props.put("registry.enable.multitenancy", "true");

        props.put("keycloak.clientId", "admin-cli");
        props.put("keycloak.grantType", "password");
        props.put("keycloak.username", container.getAdminUsername());
        props.put("keycloak.password", container.getAdminPassword());

        return props;
    }

    @Override
    public void stop() {
        log.info("Stopping Keycloak Test Container");
        container.stop();
    }
}

