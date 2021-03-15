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

package io.apicurio.registry.mt;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.DeploymentException;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.apicurio.registry.storage.RegistryStorage;
import io.apicurio.registry.types.Current;
import io.quarkus.runtime.StartupEvent;

/**
 * @author Fabian Martinez
 */
@ApplicationScoped
public class MultitenantRegistryConfig {

    @Inject
    @Current
    RegistryStorage storage;

    @Inject
    @ConfigProperty(name = "registry.enable.multitenancy")
    boolean multitenancyEnabled;

    public void init(@Observes StartupEvent ev) {
        if (multitenancyEnabled) {
            if (!storage.supportsMultiTenancy()) {
                throw new DeploymentException("Unsupported configuration, \"registry.enable.multitenancy\" is enabled "
                        + "but the storage implementation being used (" + storage.storageName() + ") does not support multitenancy");
            }
        }
    }

}
