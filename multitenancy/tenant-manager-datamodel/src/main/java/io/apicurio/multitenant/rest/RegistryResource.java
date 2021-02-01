package io.apicurio.multitenant.rest;

import io.apicurio.multitenant.rest.datamodel.RegistryDeploymentInfo;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * A JAX-RS interface.  An implementation of this interface must be provided.
 */
@Path("/registry")
public interface RegistryResource {
  @GET
  @Produces("application/json")
  RegistryDeploymentInfo getRegistryInfo();
}
