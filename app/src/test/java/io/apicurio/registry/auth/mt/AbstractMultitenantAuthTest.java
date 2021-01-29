/*
 * Copyright 2021 Red Hat
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
package io.apicurio.registry.auth.mt;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.RolesRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import io.apicurio.registry.RegistryTestBase;

/**
 * @author Fabian Martinez
 */
//@QuarkusTest
//@TestProfile(MultitenantAuthTestProfile.class)
public class AbstractMultitenantAuthTest extends RegistryTestBase {

    @ConfigProperty(name = "registry.keycloak.url")
    String authServerUrl;

    @ConfigProperty(name = "registry.keycloak.realm")
    String realm;

    @ConfigProperty(name = "keycloak.clientId")
    String clientId;

    @ConfigProperty(name = "keycloak.grantType")
    String grantType;

    @ConfigProperty(name = "keycloak.username")
    String adminUsername;

    @ConfigProperty(name = "keycloak.password")
    String adminPassword;

//    registry.ui.config.auth.keycloak.clientId  apicurio-registry
    @ConfigProperty(name = "registry.ui.config.auth.keycloak.clientId")
    String directAccessClientId;
//    quarkus.oidc.client-id registry-api
    @ConfigProperty(name = "quarkus.oidc.client-id")
    String apiClientId;

    private Keycloak keycloak;

    private String[] tenantRoles = new String[] {"sr-admin", "sr-developer", "sr-readonly"};

    @BeforeAll
    public void init() {
        keycloak = KeycloakBuilder.builder()
              .serverUrl(authServerUrl)
              .realm(realm)
              .clientId(clientId)
              .grantType(grantType)
              .username(adminUsername)
              .password(adminPassword)
              .build();

    }

    @Test
    public void testDevRole() throws Exception {

        String tenantId = "foo";
        String registryAppUrl = "http://localhost:8081/t/" + tenantId;

        TenantAuthInfo tenantInfo = createTenantAuthResources(tenantId, registryAppUrl);

        //TODO missing client api for direct access using clientId , username and password

//        Auth auth = new KeycloakAuth(authServerUrl, tenantInfo.getRealm(), tenantInfo.getDirectAccessClientId(), "test1");

//        RegistryRestClient client = RegistryRestClientFactory.create(registryAppUrl + "/api", Collections.emptyMap(), auth);
//
//        String artifactId = TestUtils.generateArtifactId();
//        try {
//            client.listArtifacts();
//
//            ArtifactMetaData meta = client.createArtifact(artifactId, ArtifactType.JSON, new ByteArrayInputStream("{}".getBytes()));
//            TestUtils.retry(() -> client.getArtifactMetaDataByGlobalId(meta.getGlobalId()));
//
//            assertNotNull(client.getLatestArtifact(meta.getId()));
//
//            Rule ruleConfig = new Rule();
//            ruleConfig.setType(RuleType.VALIDITY);
//            ruleConfig.setConfig(ValidityLevel.NONE.name());
//            client.createArtifactRule(meta.getId(), ruleConfig);
//
//            Assertions.assertThrows(ForbiddenException.class, () -> {
//                client.createGlobalRule(ruleConfig);
//            });
//        } finally {
//            client.deleteArtifact(artifactId);
//        }



    }

    public TenantAuthInfo createTenantAuthResources(String tenantId, String registryAppUrl) {

        final RealmRepresentation realmRepresentation = new RealmRepresentation();
        final String realmTenantId = "realm".concat("-").concat(tenantId);

        realmRepresentation.setDisplayName(realmTenantId);
        realmRepresentation.setRealm(realmTenantId);
        realmRepresentation.setEnabled(true);

        realmRepresentation.setRoles(buildRealmRoles());
        realmRepresentation.setClients(buildRealmClients(registryAppUrl));

        UserRepresentation user = new UserRepresentation();
        user.setUsername("sr-admin-tenant-" + tenantId);

        String password = "password";
        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(password);
        user.setCredentials(Collections.singletonList(passwordCred));

        user.setRealmRoles(Collections.singletonList("sr-admin"));

        realmRepresentation.setUsers(Collections.singletonList(user));

        keycloak.realms()
                .create(realmRepresentation);

        TenantAuthInfo info = new TenantAuthInfo();
        info.setRealm(realmTenantId);
        info.setAuthServerUrl(buildAuthServerUrl(realmTenantId));
        info.setApiClientId(apiClientId);
        info.setDirectAccessClientId(directAccessClientId);
        info.setUsername(user.getUsername());
        info.setPassword(password);
        return info;
    }

    private String buildAuthServerUrl(String realm) {

        return String.format("%s/realms/%s", authServerUrl, realm);
    }

    private List<ClientRepresentation> buildRealmClients(String registryAppUrl) {

        final ClientRepresentation uiClient = new ClientRepresentation();
        uiClient.setClientId(directAccessClientId);
        uiClient.setName(directAccessClientId);
        uiClient.setRedirectUris(List.of(String.format("%s/*", registryAppUrl)));
        uiClient.setPublicClient(true);
        uiClient.setDirectAccessGrantsEnabled(true);

        final ClientRepresentation apiClient = new ClientRepresentation();
        apiClient.setClientId(apiClientId);
        apiClient.setName(apiClientId);
        apiClient.setBearerOnly(true);

        return List.of(uiClient, apiClient);
    }

    private RolesRepresentation buildRealmRoles() {

        final RolesRepresentation rolesRepresentation = new RolesRepresentation();

        final List<RoleRepresentation> newRealmRoles = Stream.of(tenantRoles)
                .map(r -> {
                    RoleRepresentation rp = new RoleRepresentation();
                    rp.setName(r);
                    return rp;
                })
                .collect(Collectors.toList());

        rolesRepresentation.setRealm(newRealmRoles);

        return rolesRepresentation;
    }

}

