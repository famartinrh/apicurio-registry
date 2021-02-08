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

package io.apicurio.registry.rest.client.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.apicurio.registry.rest.Headers;
import io.apicurio.registry.rest.client.RegistryClient;
import io.apicurio.registry.rest.client.exception.RestClientException;
import io.apicurio.registry.rest.client.request.JsonBodyHandler;
import io.apicurio.registry.rest.client.request.RequestHandler;
import io.apicurio.registry.rest.v1.beans.Error;
import io.apicurio.registry.rest.v2.beans.*;
import io.apicurio.registry.types.ArtifactType;
import io.apicurio.registry.types.RuleType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.apicurio.registry.rest.client.impl.Routes.*;
import static java.net.http.HttpResponse.BodyHandlers;

/**
 * @author Carles Arnal <carnalca@redhat.com>
 */
public class RegistryClientImpl implements RegistryClient {

	private final RequestHandler requestHandler;
	private final ObjectMapper mapper;

	public RegistryClientImpl(String endpoint) {
		requestHandler = new RequestHandler(endpoint);
		mapper = new ObjectMapper();
	}

	@Override
	public InputStream getLatestArtifact(String groupId, String artifactId) {

		return requestHandler.sendGetRequest(ARTIFACT_BASE_PATH, Collections.emptyMap(), BodyHandlers.ofInputStream(), groupId, artifactId);
	}

	@Override
	public ArtifactMetaData updateArtifact(String groupId, String artifactId, InputStream data) {

		return requestHandler.sendPutRequest(ARTIFACT_BASE_PATH, Collections.emptyMap(), new JsonBodyHandler<>(ArtifactMetaData.class), data, groupId, artifactId)
				.get();
	}

	@Override
	public void deleteArtifact(String groupId, String artifactId) {

		requestHandler.sendDeleteRequest(ARTIFACT_BASE_PATH, Collections.emptyMap(), new JsonBodyHandler<>(Void.class), groupId, artifactId);
	}

	@Override
	public ArtifactMetaData getArtifactMetaData(String groupId, String artifactId) {

		return requestHandler.sendGetRequest(ARTIFACT_METADATA, Collections.emptyMap(), new JsonBodyHandler<>(ArtifactMetaData.class), groupId, artifactId)
				.get();
	}

	@Override
	public void updateArtifactMetaData(String groupId, String artifactId, EditableMetaData data) {

		try {

			requestHandler.sendPutRequest(ARTIFACT_METADATA, Collections.emptyMap(), new JsonBodyHandler<>(Void.class), new ByteArrayInputStream(mapper.writeValueAsBytes(data)), groupId, artifactId);

		} catch (IOException e) {
			throw parseError(e);
		}
	}

	@Override
	public VersionMetaData getArtifactVersionMetaDataByContent(String groupId, String artifactId, Boolean canonical, InputStream data) {

		return requestHandler.sendPostRequest(VERSION_METADATA, Collections.emptyMap(), Map.of(Parameters.CANONICAL, String.valueOf(canonical)), new JsonBodyHandler<>(VersionMetaData.class), data, groupId, artifactId)
				.get();
	}

	@Override
	public List<RuleType> listArtifactRules(String groupId, String artifactId) {

		//FIXME proper handling of list results
		return requestHandler.sendGetRequest(ARTIFACT_RULES, Collections.emptyMap(), new JsonBodyHandler<>(List.class), groupId, artifactId)
				.get();
	}

	@Override
	public void createArtifactRule(String groupId, String artifactId, Rule data) {

		try {
			requestHandler.sendPostRequest(ARTIFACT_RULES, Collections.emptyMap(), Collections.emptyMap(), new JsonBodyHandler<>(Void.class), new ByteArrayInputStream(mapper.writeValueAsBytes(data)), groupId, artifactId);
		} catch (JsonProcessingException e) {
			throw parseError(e);
		}
	}

	@Override
	public void deleteArtifactRules(String groupId, String artifactId) {

		requestHandler.sendDeleteRequest(ARTIFACT_RULES, Collections.emptyMap(), new JsonBodyHandler<>(Void.class), groupId, artifactId);
	}

	@Override
	public Rule getArtifactRuleConfig(String groupId, String artifactId, RuleType rule) {

		return requestHandler.sendGetRequest(ARTIFACT_RULE, Collections.emptyMap(), new JsonBodyHandler<>(Rule.class), groupId, rule.value())
				.get();
	}

	@Override
	public Rule updateArtifactRuleConfig(String groupId, String artifactId, RuleType rule, Rule data) {

		try {
			return requestHandler.sendPutRequest(ARTIFACT_RULE, Collections.emptyMap(), new JsonBodyHandler<>(Rule.class), new ByteArrayInputStream(mapper.writeValueAsBytes(data)), groupId, artifactId, rule.value())
					.get();
		} catch (JsonProcessingException e) {
			throw parseError(e);
		}
	}

	@Override
	public void deleteArtifactRule(String groupId, String artifactId, RuleType rule) {

		requestHandler.sendDeleteRequest(ARTIFACT_RULE, Collections.emptyMap(), new JsonBodyHandler<>(Void.class), groupId, artifactId, rule.value());
	}

	@Override
	public void updateArtifactState(String groupId, String artifactId, UpdateState data) {

		try {
			requestHandler.sendPutRequest(ARTIFACT_STATE, Collections.emptyMap(), new JsonBodyHandler<>(Void.class), new ByteArrayInputStream(mapper.writeValueAsBytes(data)), groupId, artifactId);
		} catch (JsonProcessingException e) {
			throw parseError(e);
		}
	}

	@Override
	public void testUpdateArtifact(String groupId, String artifactId, InputStream data) {

		try {
			requestHandler.sendPutRequest(ARTIFACT_TEST, Collections.emptyMap(), new JsonBodyHandler<>(Void.class), new ByteArrayInputStream(mapper.writeValueAsBytes(data)), groupId, artifactId);
		} catch (JsonProcessingException e) {
			throw parseError(e);
		}
	}

	@Override
	public InputStream getArtifactVersion(String groupId, String artifactId, String version) {

		return requestHandler.sendGetRequest(ARTIFACT_VERSION, Collections.emptyMap(), BodyHandlers.ofInputStream(), groupId, version);
	}

	@Override
	public VersionMetaData getArtifactVersionMetaData(String groupId, String artifactId, String version) {

		return requestHandler.sendGetRequest(VERSION_METADATA, Collections.emptyMap(), new JsonBodyHandler<>(VersionMetaData.class), groupId, version)
				.get();
	}

	@Override
	public void updateArtifactVersionMetaData(String groupId, String artifactId, String version, EditableMetaData data) {

		try {
			requestHandler.sendPutRequest(VERSION_METADATA, Collections.emptyMap(), new JsonBodyHandler<>(Void.class), new ByteArrayInputStream(mapper.writeValueAsBytes(data)), groupId, artifactId);
		} catch (JsonProcessingException e) {
			throw parseError(e);
		}
	}

	@Override
	public void deleteArtifactVersionMetaData(String groupId, String artifactId, String version) {

		requestHandler.sendDeleteRequest(VERSION_METADATA, Collections.emptyMap(), new JsonBodyHandler<>(Void.class), groupId, artifactId, version);
	}

	@Override
	public void updateArtifactVersionState(String groupId, String artifactId, String version, UpdateState data) {

		try {
			requestHandler.sendPutRequest(VERSION_STATE, Collections.emptyMap(), new JsonBodyHandler<>(Void.class), new ByteArrayInputStream(mapper.writeValueAsBytes(data)), groupId, artifactId, version);
		} catch (JsonProcessingException e) {
			throw parseError(e);
		}
	}

	@Override
	public VersionSearchResults listArtifactVersions(String groupId, String artifactId, Integer offset, Integer limit) {

		return requestHandler.sendGetRequest(ARTIFACT_VERSIONS, Map.of(Parameters.LIMIT, String.valueOf(limit), Parameters.OFFSET, String.valueOf(offset)), new JsonBodyHandler<>(VersionSearchResults.class), groupId, artifactId)
				.get();
	}

	@Override
	public VersionMetaData createArtifactVersion(String groupId, String artifactId, String xRegistryVersion, InputStream data) {

		return requestHandler.sendPostRequest(ARTIFACT_VERSION, Map.of(Headers.VERSION, xRegistryVersion), Collections.emptyMap(), new JsonBodyHandler<>(VersionMetaData.class), data, groupId, artifactId)
				.get();
	}

	@Override
	public ArtifactSearchResults listArtifactsInGroup(String groupId, Integer limit, Integer offset, SortOrder order, SortBy orderby) {

		return requestHandler.sendGetRequest(GROUP_BASE_PATH, Map.of(Parameters.LIMIT, String.valueOf(limit), Parameters.OFFSET, String.valueOf(offset), Parameters.SORT_ORDER, order.value(), Parameters.ORDER_BY, orderby.value()), new JsonBodyHandler<>(ArtifactSearchResults.class), groupId)
				.get();

	}

	@Override
	public ArtifactMetaData createArtifact(String groupId, ArtifactType xRegistryArtifactType, String xRegistryArtifactId, String xRegistryVersion, IfExists ifExists, Boolean canonical, InputStream data) {

		return requestHandler.sendPostRequest(GROUP_BASE_PATH, Map.of(Headers.ARTIFACT_ID, xRegistryArtifactId, Headers.ARTIFACT_TYPE, xRegistryArtifactType.value(), Headers.VERSION, xRegistryVersion), Map.of(Parameters.CANONICAL, String.valueOf(canonical)), new JsonBodyHandler<>(ArtifactMetaData.class), data, groupId)
				.get();
	}

	@Override
	public void deleteArtifactsInGroup(String groupId) {

		requestHandler.sendDeleteRequest(GROUP_BASE_PATH, Collections.emptyMap(), new JsonBodyHandler<>(Void.class), groupId);

	}

	@Override
	public InputStream getContentById(int contentId) {
		return null;
	}

	@Override
	public InputStream getContentByGlobalId(int globalId) {
		return null;
	}

	@Override
	public InputStream getContentByHash(int contentHash, Boolean canonical) {
		return null;
	}

	@Override
	public ArtifactSearchResults searchArtifacts(String name, Integer offset, Integer limit, SortOrder order, SortBy orderby, List<String> labels, List<String> properties, String description, String artifactgroup) {
		return null;
	}

	@Override
	public ArtifactSearchResults searchArtifactsByContent(Integer offset, Integer limit, SortOrder order, SortBy orderby, InputStream data) {
		return null;
	}

	private RestClientException parseError(Exception ex) {

		//FIXME proper error handling
		return new RestClientException(new Error());
	}

}