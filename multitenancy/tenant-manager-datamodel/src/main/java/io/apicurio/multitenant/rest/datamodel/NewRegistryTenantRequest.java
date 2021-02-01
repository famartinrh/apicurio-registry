
package io.apicurio.multitenant.rest.datamodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Root Type for NewTenant
 * <p>
 * The information required when creating a new tenant.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "tenantId",
    "organizationId",
    "authServerUrl",
    "clientId"
})
public class NewRegistryTenantRequest {

    /**
     * Root Type for TenantId
     * <p>
     * The unique ID of a tenant managed by this API, bound to only one registry deployment.
     * 
     */
    @JsonProperty("tenantId")
    @JsonPropertyDescription("The unique ID of a tenant managed by this API, bound to only one registry deployment.")
    private String tenantId;
    /**
     * ID of the organization the tenant belongs to
     * (Required)
     * 
     */
    @JsonProperty("organizationId")
    @JsonPropertyDescription("ID of the organization the tenant belongs to")
    private String organizationId;
    /**
     * Http endpoint for the auth server (including realm) to be used for this tenant to authenticate against the registry
     * 
     */
    @JsonProperty("authServerUrl")
    @JsonPropertyDescription("Http endpoint for the auth server (including realm) to be used for this tenant to authenticate against the registry")
    private String authServerUrl;
    /**
     * ClientId in the authentication server to be used by the registry to authenticate incoming requests made by the tenant
     * (Required)
     * 
     */
    @JsonProperty("clientId")
    @JsonPropertyDescription("ClientId in the authentication server to be used by the registry to authenticate incoming requests made by the tenant")
    private String clientId;

    /**
     * Root Type for TenantId
     * <p>
     * The unique ID of a tenant managed by this API, bound to only one registry deployment.
     * 
     */
    @JsonProperty("tenantId")
    public String getTenantId() {
        return tenantId;
    }

    /**
     * Root Type for TenantId
     * <p>
     * The unique ID of a tenant managed by this API, bound to only one registry deployment.
     * 
     */
    @JsonProperty("tenantId")
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    /**
     * ID of the organization the tenant belongs to
     * (Required)
     * 
     */
    @JsonProperty("organizationId")
    public String getOrganizationId() {
        return organizationId;
    }

    /**
     * ID of the organization the tenant belongs to
     * (Required)
     * 
     */
    @JsonProperty("organizationId")
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    /**
     * Http endpoint for the auth server (including realm) to be used for this tenant to authenticate against the registry
     * 
     */
    @JsonProperty("authServerUrl")
    public String getAuthServerUrl() {
        return authServerUrl;
    }

    /**
     * Http endpoint for the auth server (including realm) to be used for this tenant to authenticate against the registry
     * 
     */
    @JsonProperty("authServerUrl")
    public void setAuthServerUrl(String authServerUrl) {
        this.authServerUrl = authServerUrl;
    }

    /**
     * ClientId in the authentication server to be used by the registry to authenticate incoming requests made by the tenant
     * (Required)
     * 
     */
    @JsonProperty("clientId")
    public String getClientId() {
        return clientId;
    }

    /**
     * ClientId in the authentication server to be used by the registry to authenticate incoming requests made by the tenant
     * (Required)
     * 
     */
    @JsonProperty("clientId")
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

}
