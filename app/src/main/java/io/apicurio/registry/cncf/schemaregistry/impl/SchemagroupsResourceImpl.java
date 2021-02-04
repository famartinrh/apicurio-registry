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

package io.apicurio.registry.cncf.schemaregistry.impl;

import static io.apicurio.registry.metrics.MetricIDs.REST_CONCURRENT_REQUEST_COUNT;
import static io.apicurio.registry.metrics.MetricIDs.REST_CONCURRENT_REQUEST_COUNT_DESC;
import static io.apicurio.registry.metrics.MetricIDs.REST_GROUP_TAG;
import static io.apicurio.registry.metrics.MetricIDs.REST_REQUEST_COUNT;
import static io.apicurio.registry.metrics.MetricIDs.REST_REQUEST_COUNT_DESC;
import static io.apicurio.registry.metrics.MetricIDs.REST_REQUEST_RESPONSE_TIME;
import static io.apicurio.registry.metrics.MetricIDs.REST_REQUEST_RESPONSE_TIME_DESC;
import static org.eclipse.microprofile.metrics.MetricUnits.MILLISECONDS;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.eclipse.microprofile.metrics.annotation.ConcurrentGauge;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;

import io.apicurio.registry.cncf.schemaregistry.SchemagroupsResource;
import io.apicurio.registry.cncf.schemaregistry.beans.SchemaGroup;
import io.apicurio.registry.logging.Logged;
import io.apicurio.registry.metrics.ResponseErrorLivenessCheck;
import io.apicurio.registry.metrics.ResponseTimeoutReadinessCheck;
import io.apicurio.registry.metrics.RestMetricsApply;
import io.apicurio.registry.storage.RegistryStorage;
import io.apicurio.registry.storage.dto.ArtifactSearchResultsDto;
import io.apicurio.registry.storage.dto.OrderBy;
import io.apicurio.registry.storage.dto.OrderDirection;
import io.apicurio.registry.storage.dto.SearchFilter;
import io.apicurio.registry.storage.dto.SearchFilterType;
import io.apicurio.registry.types.Current;

/**
 * @author Fabian Martinez
 */
@ApplicationScoped
@Interceptors({ResponseErrorLivenessCheck.class, ResponseTimeoutReadinessCheck.class})
@RestMetricsApply
@Counted(name = REST_REQUEST_COUNT, description = REST_REQUEST_COUNT_DESC, tags = {"group=" + REST_GROUP_TAG, "metric=" + REST_REQUEST_COUNT})
@ConcurrentGauge(name = REST_CONCURRENT_REQUEST_COUNT, description = REST_CONCURRENT_REQUEST_COUNT_DESC, tags = {"group=" + REST_GROUP_TAG, "metric=" + REST_CONCURRENT_REQUEST_COUNT})
@Timed(name = REST_REQUEST_RESPONSE_TIME, description = REST_REQUEST_RESPONSE_TIME_DESC, tags = {"group=" + REST_GROUP_TAG, "metric=" + REST_REQUEST_RESPONSE_TIME}, unit = MILLISECONDS)
@Logged
public class SchemagroupsResourceImpl implements SchemagroupsResource {

    @Inject
    @Current
    RegistryStorage storage;

    /**
     * @see io.apicurio.registry.cncf.schemaregistry.SchemagroupsResource#getGroups()
     */
    @Override
    public List<String> getGroups() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see io.apicurio.registry.cncf.schemaregistry.SchemagroupsResource#getGroup(java.lang.String)
     */
    @Override
    public SchemaGroup getGroup(String groupId) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see io.apicurio.registry.cncf.schemaregistry.SchemagroupsResource#createGroup(java.lang.String, io.apicurio.registry.cncf.schemaregistry.beans.SchemaGroup)
     */
    @Override
    public void createGroup(String groupId, SchemaGroup data) {
        // TODO Auto-generated method stub

    }

    /**
     * @see io.apicurio.registry.cncf.schemaregistry.SchemagroupsResource#deleteGroup(java.lang.String)
     */
    @Override
    public void deleteGroup(String groupId) {
        // TODO Auto-generated method stub

    }

    /**
     * @see io.apicurio.registry.cncf.schemaregistry.SchemagroupsResource#getSchemasByGroup(java.lang.String)
     */
    @Override
    public List<String> getSchemasByGroup(String groupId) {
        Set<SearchFilter> filters = new HashSet<>();
        filters.add(new SearchFilter(SearchFilterType.group, groupId));

        ArtifactSearchResultsDto resultsDto = storage.searchArtifacts(filters, OrderBy.name, OrderDirection.asc, 0, 1000);

        return resultsDto.getArtifacts()
                .stream()
                .map(dto -> dto.getId())
                .collect(Collectors.toList());
    }

    /**
     * @see io.apicurio.registry.cncf.schemaregistry.SchemagroupsResource#deleteSchemasByGroup(java.lang.String)
     */
    @Override
    public void deleteSchemasByGroup(String groupId) {
        storage.deleteArtifacts(groupId);
    }

    /**
     * @see io.apicurio.registry.cncf.schemaregistry.SchemagroupsResource#getLatestSchema(java.lang.String, java.lang.String)
     */
    @Override
    public void getLatestSchema(String groupId, String schemaId) {
        // TODO Auto-generated method stub

    }

    /**
     * @see io.apicurio.registry.cncf.schemaregistry.SchemagroupsResource#createSchema(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void createSchema(String groupId, String schemaId, String data) {
        // TODO Auto-generated method stub

    }

    /**
     * @see io.apicurio.registry.cncf.schemaregistry.SchemagroupsResource#deleteSchema(java.lang.String, java.lang.String)
     */
    @Override
    public void deleteSchema(String groupId, String schemaId) {
        // TODO Auto-generated method stub

    }

    /**
     * @see io.apicurio.registry.cncf.schemaregistry.SchemagroupsResource#getSchemaVersions(java.lang.String, java.lang.String)
     */
    @Override
    public List<Integer> getSchemaVersions(String groupId, String schemaId) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see io.apicurio.registry.cncf.schemaregistry.SchemagroupsResource#getSchemaVersion(java.lang.String, java.lang.String, java.lang.Integer)
     */
    @Override
    public void getSchemaVersion(String groupId, String schemaId, Integer versionNumber) {
        // TODO Auto-generated method stub

    }

    /**
     * @see io.apicurio.registry.cncf.schemaregistry.SchemagroupsResource#deleteSchemaVersion(java.lang.String, java.lang.String, java.lang.Integer)
     */
    @Override
    public void deleteSchemaVersion(String groupId, String schemaId, Integer versionNumber) {
        // TODO Auto-generated method stub

    }

}
