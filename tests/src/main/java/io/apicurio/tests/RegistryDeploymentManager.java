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
package io.apicurio.tests;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.TimeoutException;

import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.apicurio.registry.utils.tests.TestUtils;
import io.apicurio.tests.utils.RegistryUtils;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;

public class RegistryDeploymentManager implements TestExecutionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistryDeploymentManager.class);

    private static RegistryFacade registry = RegistryFacade.getInstance();

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        if (!TestUtils.isExternalRegistry() && !registry.isRunning()) {
            LOGGER.info("Starting registry");
            try {
                registry.start();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        } else {
            LOGGER.info("Going to use already running registries on {}", TestUtils.getRegistryApiUrl());
        }
        try {
            RegistryUtils.waitForRegistry();
        } catch (TimeoutException e) {
            if (!TestUtils.isExternalRegistry()) {
                try {
                    registry.stop(); //TODO collect logs
                } catch (IOException e1) {
                    e.addSuppressed(e1);
                }
            }
            throw new IllegalStateException(e);
        }
        RestAssured.baseURI = TestUtils.getRegistryApiUrl();
        LOGGER.info("Registry app is running on {}", RestAssured.baseURI);
        RestAssured.defaultParser = Parser.JSON;
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        if (!TestUtils.isExternalRegistry()) {
            LOGGER.info("Tear down registry deployment");
            try {
                registry.stop();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

}
