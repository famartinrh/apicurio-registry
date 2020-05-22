/*
 * Copyright 2020 Red Hat
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
package io.apicurio.tests.utils;

import java.util.concurrent.TimeoutException;

import io.apicurio.registry.utils.tests.TestUtils;
import io.apicurio.tests.Constants;

public class RegistryUtils {

    private RegistryUtils() {
        //utils class
    }

    public static void waitForRegistry() throws TimeoutException {
        TestUtils.waitFor("Cannot connect to registries on " + TestUtils.getRegistryApiUrl() + " in timeout!",
                Constants.POLL_INTERVAL, Constants.TIMEOUT_FOR_REGISTRY_START_UP, TestUtils::isReachable);

        TestUtils.waitFor("Registry reports is ready",
                Constants.POLL_INTERVAL, Constants.TIMEOUT_FOR_REGISTRY_READY, () -> TestUtils.isReady(false), () -> TestUtils.isReady(true));
    }

}
