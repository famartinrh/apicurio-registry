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

package io.apicurio.registry;

import static io.apicurio.registry.utils.tests.TestUtils.assertWebError;
import static io.apicurio.registry.utils.tests.TestUtils.retry;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.apicurio.registry.rest.beans.ArtifactMetaData;
import io.apicurio.registry.rest.beans.EditableMetaData;
import io.apicurio.registry.rest.beans.UpdateState;
import io.apicurio.registry.rest.beans.VersionMetaData;
import io.apicurio.registry.types.ArtifactState;
import io.apicurio.registry.types.ArtifactType;
import io.quarkus.test.junit.QuarkusTest;

/**
 * @author Ales Justin
 */
@QuarkusTest
public class ArtifactStateTest extends AbstractResourceTestBase {

    private static final UpdateState toUpdateState(ArtifactState state) {
        UpdateState us = new UpdateState();
        us.setState(state);
        return us;
    }

    @Test
    public void testSmoke() throws Exception {
        String artifactId = generateArtifactId();

        ArtifactMetaData amd1 = client.createArtifact(artifactId, ArtifactType.JSON, new ByteArrayInputStream("{\"type\": \"string\"}".getBytes(StandardCharsets.UTF_8)));
        this.waitForGlobalId(amd1.getGlobalId());

        ArtifactMetaData amd2 = client.updateArtifact(
                artifactId,
                ArtifactType.JSON,
                new ByteArrayInputStream("\"type\": \"int\"".getBytes(StandardCharsets.UTF_8))
        );
        this.waitForGlobalId(amd2.getGlobalId());

        ArtifactMetaData amd3 = client.updateArtifact(
                artifactId,
                ArtifactType.JSON,
                new ByteArrayInputStream("\"type\": \"float\"".getBytes(StandardCharsets.UTF_8))
        );
        this.waitForGlobalId(amd3.getGlobalId());

        ArtifactMetaData amd = client.getArtifactMetaData(artifactId);
        Assertions.assertEquals(3, amd.getVersion());

        // disable latest
        client.updateArtifactState(artifactId, toUpdateState(ArtifactState.DISABLED));
        this.waitForVersionState(artifactId, 3, ArtifactState.DISABLED);

        VersionMetaData tvmd = client.getArtifactVersionMetaData(artifactId, 3);
        Assertions.assertEquals(3, tvmd.getVersion());
        Assertions.assertEquals(ArtifactState.DISABLED, tvmd.getState());

        ArtifactMetaData tamd = client.getArtifactMetaData(artifactId);
        Assertions.assertEquals(3, tamd.getVersion());
        Assertions.assertEquals(ArtifactState.DISABLED, tamd.getState());
        Assertions.assertNull(tamd.getDescription());

        EditableMetaData emd = new EditableMetaData();
        String description = "Testing artifact state";
        emd.setDescription(description);

        // cannot get a disabled artifact
        assertWebError(404, () -> client.getLatestArtifact(artifactId));
        assertWebError(404, () -> client.getArtifactVersion(artifactId, 3));

        // can update and get metadata for a disabled artifact
        client.updateArtifactVersionMetaData(artifactId, 3, emd);
        client.updateArtifactMetaData(artifactId, emd);

        retry(() -> {
            ArtifactMetaData innerAmd = client.getArtifactMetaData(artifactId);
            Assertions.assertEquals(3, innerAmd.getVersion());
            Assertions.assertEquals(description, innerAmd.getDescription());
            return null;
        });

        client.updateArtifactVersionState(artifactId, 3, toUpdateState(ArtifactState.DEPRECATED));
        this.waitForVersionState(artifactId, 3, ArtifactState.DEPRECATED);

        tamd = client.getArtifactMetaData(artifactId);
        Assertions.assertEquals(3, tamd.getVersion()); // should be back to v3
        Assertions.assertEquals(ArtifactState.DEPRECATED, tamd.getState());
        Assertions.assertEquals(tamd.getDescription(), description);

        InputStream latestArtifact = client.getLatestArtifact(artifactId);
        Assertions.assertNotNull(latestArtifact);
        latestArtifact.close();
        InputStream version = client.getArtifactVersion(artifactId, 2);
        Assertions.assertNotNull(version);
        version.close();

        client.updateArtifactMetaData(artifactId, emd); // should be allowed for deprecated

        retry(() -> {
            ArtifactMetaData innerAmd = client.getArtifactMetaData(artifactId);
            Assertions.assertEquals(3, innerAmd.getVersion());
            Assertions.assertEquals(description, innerAmd.getDescription());
            Assertions.assertEquals(ArtifactState.DEPRECATED, innerAmd.getState());
            return null;
        });

        // can revert back to enabled from deprecated
        client.updateArtifactVersionState(artifactId, 3, toUpdateState(ArtifactState.ENABLED));
        this.waitForVersionState(artifactId, 3, ArtifactState.ENABLED);

        retry(() -> {
            ArtifactMetaData innerAmd = client.getArtifactMetaData(artifactId);
            Assertions.assertEquals(3, innerAmd.getVersion()); // should still be latest (aka 3)
            Assertions.assertEquals(description, innerAmd.getDescription());

            VersionMetaData innerVmd = client.getArtifactVersionMetaData(artifactId, 1);
            Assertions.assertNull(innerVmd.getDescription());

            return null;
        });
    }

    @Test
    void testEnableDisableArtifact() throws Exception {
        String artifactId = generateArtifactId();

        // Create the artifact
        ArtifactMetaData md = client.createArtifact(
                artifactId,
                ArtifactType.JSON,
                new ByteArrayInputStream("{\"type\": \"string\"}".getBytes(StandardCharsets.UTF_8))
        );

        retry(() -> {
            // Get the meta-data
            ArtifactMetaData actualMD = client.getArtifactMetaData(artifactId);
            assertEquals(md.getGlobalId(), actualMD.getGlobalId());
        });

        // Set to disabled
        UpdateState state = new UpdateState();
        state.setState(ArtifactState.DISABLED);
        client.updateArtifactState(artifactId, state);
        this.waitForArtifactState(artifactId, ArtifactState.DISABLED);

        retry(() -> {
            // Get the meta-data again - should be DISABLED
            ArtifactMetaData actualMD = client.getArtifactMetaData(artifactId);
            assertEquals(md.getGlobalId(), actualMD.getGlobalId());
            Assertions.assertEquals(ArtifactState.DISABLED, actualMD.getState());

            // Get the version meta-data - should also be disabled
            VersionMetaData vmd = client.getArtifactVersionMetaData(artifactId, md.getVersion());
            Assertions.assertEquals(ArtifactState.DISABLED, vmd.getState());
        });

        // Now re-enable the artifact
        state.setState(ArtifactState.ENABLED);
        client.updateArtifactState(artifactId, state);
        this.waitForArtifactState(artifactId, ArtifactState.ENABLED);

        // Get the meta-data
        ArtifactMetaData amd = client.getArtifactMetaData(artifactId);
        Assertions.assertEquals(ArtifactState.ENABLED, amd.getState());
        VersionMetaData vmd = client.getArtifactVersionMetaData(artifactId, md.getVersion());
        Assertions.assertEquals(ArtifactState.ENABLED, vmd.getState());
    }

    @Test
    void testDeprecateDisableArtifact() throws Exception {
        String artifactId = generateArtifactId();

        // Create the artifact
        ArtifactMetaData md = client.createArtifact(
            artifactId,
            ArtifactType.JSON,
            new ByteArrayInputStream("{\"type\": \"string\"}".getBytes(StandardCharsets.UTF_8))
        );

        retry(() -> {
            // Get the meta-data
            ArtifactMetaData actualMD = client.getArtifactMetaData(artifactId);
            assertEquals(md.getGlobalId(), actualMD.getGlobalId());
        });

        // Set to deprecated
        UpdateState state = new UpdateState();
        state.setState(ArtifactState.DEPRECATED);
        client.updateArtifactState(artifactId, state);
        this.waitForArtifactState(artifactId, ArtifactState.DEPRECATED);

        retry(() -> {
            // Get the meta-data again - should be DEPRECATED
            ArtifactMetaData actualMD = client.getArtifactMetaData(artifactId);
            assertEquals(md.getGlobalId(), actualMD.getGlobalId());
            Assertions.assertEquals(ArtifactState.DEPRECATED, actualMD.getState());
        });

        // Set to disabled
        state.setState(ArtifactState.DISABLED);
        client.updateArtifactState(artifactId, state);
        this.waitForArtifactState(artifactId, ArtifactState.DISABLED);

        retry(() -> {
            // Get the meta-data again - should be DISABLED
            ArtifactMetaData actualMD = client.getArtifactMetaData(artifactId);
            assertEquals(md.getGlobalId(), actualMD.getGlobalId());
            Assertions.assertEquals(ArtifactState.DISABLED, actualMD.getState());

            // Get the version meta-data - should also be disabled
            VersionMetaData vmd = client.getArtifactVersionMetaData(artifactId, md.getVersion());
            Assertions.assertEquals(ArtifactState.DISABLED, vmd.getState());
        });
    }

}