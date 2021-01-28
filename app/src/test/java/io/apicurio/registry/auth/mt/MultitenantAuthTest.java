package io.apicurio.registry.auth.mt;

import org.junit.jupiter.api.Test;
import io.apicurio.registry.RegistryTestBase;

//@QuarkusTest
//@TestProfile(MultitenantAuthTestProfile.class)
public class MultitenantAuthTest extends RegistryTestBase {

//    @ConfigProperty(name = "registry.keycloak.url")
//    String authServerUrl;
//
//    @ConfigProperty(name = "registry.keycloak.realm")
//    String realm;
//
//    @ConfigProperty(name = "keycloak.clientId")
//    String clientId;
//
//    @ConfigProperty(name = "keycloak.grantType")
//    String grantType;
//
//    @ConfigProperty(name = "keycloak.username")
//    String username;
//
//    @ConfigProperty(name = "keycloak.password")
//    String password;
//
//    private Keycloak keycloak;
//
//    private String tenantClientId = "tenant-client-id";
//    private String[] tenantRoles = new String[] {"sr-admin", "sr-developer", "sr-readonly"};
//
//    @BeforeAll
//    public void init() {
//        keycloak = KeycloakBuilder.builder()
//              .serverUrl(authServerUrl)
//              .realm(realm)
//              .clientId(clientId)
//              .grantType(grantType)
//              .username(username)
//              .password(password)
//              .build();
//
//    }

    @Test
    public void testDevRole() throws Exception {

//        Auth auth = new KeycloakAuth(authServerUrl, realm, developerClientId, "test1");

//        RegistryRestClient client = RegistryRestClientFactory.create(registryUrl, Collections.emptyMap(), auth);
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


//    public AuthResource createTenantAuthResources(String tenantId, String registryAppUrl) {
//
//        final RealmRepresentation realmRepresentation = new RealmRepresentation();
//        final String realmTenantId = "tenant-realm".concat("-").concat(tenantId);
//
//        realmRepresentation.setDisplayName(realmTenantId);
//        realmRepresentation.setRealm(realmTenantId);
//        realmRepresentation.setEnabled(true);
//
//        realmRepresentation.setRoles(buildRealmRoles());
//        realmRepresentation.setClients(buildRealmClients(registryAppUrl));
//
//        keycloak.realms()
//                .create(realmRepresentation);
//
//        return AuthResource.builder()
//                .clientId(authConfig.getApiClientId())
//                .serverUrl(buildAuthServerUrl(realmTenantId))
//                .build();
//    }
//
//    private String buildAuthServerUrl(String realm) {
//
//        return String.format("%s/realms/%s", authServerUrl, realm);
//    }
//
//    private List<ClientRepresentation> buildRealmClients(String registryAppUrl) {
//
////        final ClientRepresentation uiClient = new ClientRepresentation();
////        uiClient.setClientId(authConfig.getUiClientId());
////        uiClient.setName(authConfig.getUiClientId());
////        uiClient.setRedirectUris(List.of(String.format(REDIRECT_URI_PLACEHOLDER, registryAppUrl)));
////        uiClient.setPublicClient(true);
//
//        final ClientRepresentation apiClient = new ClientRepresentation();
//        apiClient.setClientId(tenantClientId);
//        apiClient.setName(tenantClientId);
//        apiClient.setBearerOnly(true);
//
//        return List.of(apiClient);
//    }
//
//    private RolesRepresentation buildRealmRoles() {
//
//        final RolesRepresentation rolesRepresentation = new RolesRepresentation();
//
//        final List<RoleRepresentation> newRealmRoles = Stream.of(tenantRoles)
//                .map(r -> {
//                    RoleRepresentation rp = new RoleRepresentation();
//                    rp.setName(r);
//                    return rp;
//                })
//                .collect(Collectors.toList());
//
//        rolesRepresentation.setRealm(newRealmRoles);
//
//        return rolesRepresentation;
//    }

}

