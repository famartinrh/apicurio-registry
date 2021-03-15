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

package io.apicurio.registry.storage.impl;

import static io.apicurio.registry.metrics.MetricIDs.STORAGE_CONCURRENT_OPERATION_COUNT;
import static io.apicurio.registry.metrics.MetricIDs.STORAGE_CONCURRENT_OPERATION_COUNT_DESC;
import static io.apicurio.registry.metrics.MetricIDs.STORAGE_GROUP_TAG;
import static io.apicurio.registry.metrics.MetricIDs.STORAGE_OPERATION_COUNT;
import static io.apicurio.registry.metrics.MetricIDs.STORAGE_OPERATION_COUNT_DESC;
import static io.apicurio.registry.metrics.MetricIDs.STORAGE_OPERATION_TIME;
import static io.apicurio.registry.metrics.MetricIDs.STORAGE_OPERATION_TIME_DESC;
import static org.eclipse.microprofile.metrics.MetricUnits.MILLISECONDS;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.metrics.annotation.ConcurrentGauge;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;

import io.apicurio.registry.content.ContentHandle;
import io.apicurio.registry.logging.Logged;
import io.apicurio.registry.metrics.PersistenceExceptionLivenessApply;
import io.apicurio.registry.metrics.PersistenceTimeoutReadinessApply;
import io.apicurio.registry.mt.TenantContext;
import io.apicurio.registry.mt.metadata.TenantMetadataDto;
import io.apicurio.registry.storage.ArtifactAlreadyExistsException;
import io.apicurio.registry.storage.ArtifactNotFoundException;
import io.apicurio.registry.storage.ContentNotFoundException;
import io.apicurio.registry.storage.GroupAlreadyExistsException;
import io.apicurio.registry.storage.GroupNotFoundException;
import io.apicurio.registry.storage.LogConfigurationNotFoundException;
import io.apicurio.registry.storage.RegistryStorage;
import io.apicurio.registry.storage.RegistryStorageException;
import io.apicurio.registry.storage.RuleAlreadyExistsException;
import io.apicurio.registry.storage.RuleNotFoundException;
import io.apicurio.registry.storage.VersionNotFoundException;
import io.apicurio.registry.storage.dto.ArtifactMetaDataDto;
import io.apicurio.registry.storage.dto.ArtifactSearchResultsDto;
import io.apicurio.registry.storage.dto.ArtifactVersionMetaDataDto;
import io.apicurio.registry.storage.dto.EditableArtifactMetaDataDto;
import io.apicurio.registry.storage.dto.GroupMetaDataDto;
import io.apicurio.registry.storage.dto.LogConfigurationDto;
import io.apicurio.registry.storage.dto.OrderBy;
import io.apicurio.registry.storage.dto.OrderDirection;
import io.apicurio.registry.storage.dto.RuleConfigurationDto;
import io.apicurio.registry.storage.dto.SearchFilter;
import io.apicurio.registry.storage.dto.StoredArtifactDto;
import io.apicurio.registry.storage.dto.VersionSearchResultsDto;
import io.apicurio.registry.types.ArtifactState;
import io.apicurio.registry.types.ArtifactType;
import io.apicurio.registry.types.RuleType;
import io.apicurio.registry.types.provider.ArtifactTypeUtilProviderFactory;
import io.quarkus.security.identity.SecurityIdentity;

/**
 * @author Fabian Martinez
 */
@ApplicationScoped
@PersistenceExceptionLivenessApply
@PersistenceTimeoutReadinessApply
@Counted(name = STORAGE_OPERATION_COUNT + "_InMemoryRegistryStorage", description = STORAGE_OPERATION_COUNT_DESC, tags = {"group=" + STORAGE_GROUP_TAG, "metric=" + STORAGE_OPERATION_COUNT}, reusable = true)
@ConcurrentGauge(name = STORAGE_CONCURRENT_OPERATION_COUNT + "_InMemoryRegistryStorage", description = STORAGE_CONCURRENT_OPERATION_COUNT_DESC, tags = {"group=" + STORAGE_GROUP_TAG, "metric=" + STORAGE_CONCURRENT_OPERATION_COUNT}, reusable = true)
@Timed(name = STORAGE_OPERATION_TIME + "_InMemoryRegistryStorage", description = STORAGE_OPERATION_TIME_DESC, tags = {"group=" + STORAGE_GROUP_TAG, "metric=" + STORAGE_OPERATION_TIME}, unit = MILLISECONDS, reusable = true)
@Logged
public class MultitenantInMemoryRegistryStorage implements RegistryStorage {

    @Inject
    ArtifactTypeUtilProviderFactory factory;
    @Inject
    SecurityIdentity securityIdentity;

    private Map<String, RegistryStorage> tenantStorages = new ConcurrentHashMap<>();

    @Inject
    TenantContext tenantContext;

    private RegistryStorage tenantStorage() {
        return tenantStorages.computeIfAbsent(tenantContext.tenantId(), (k) -> new SimpleInMemoryRegistryStorage(factory, securityIdentity));
    }

    /**
     * @return
     * @see io.apicurio.registry.storage.RegistryStorage#isReady()
     */
    @Override
    public boolean isReady() {
        return tenantStorage().isReady();
    }


    /**
     * @return
     * @see io.apicurio.registry.storage.RegistryStorage#isAlive()
     */
    @Override
    public boolean isAlive() {
        return tenantStorage().isAlive();
    }


    /**
     * @param groupId
     * @param artifactId
     * @param state
     * @throws ArtifactNotFoundException
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#updateArtifactState(java.lang.String, java.lang.String, io.apicurio.registry.types.ArtifactState)
     */
    @Override
    public void updateArtifactState(String groupId, String artifactId, ArtifactState state)
            throws ArtifactNotFoundException, RegistryStorageException {
        tenantStorage().updateArtifactState(groupId, artifactId, state);
    }


    /**
     * @param groupId
     * @param artifactId
     * @param version
     * @param state
     * @throws ArtifactNotFoundException
     * @throws VersionNotFoundException
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#updateArtifactState(java.lang.String, java.lang.String, java.lang.Long, io.apicurio.registry.types.ArtifactState)
     */
    @Override
    public void updateArtifactState(String groupId, String artifactId, Long version, ArtifactState state)
            throws ArtifactNotFoundException, VersionNotFoundException, RegistryStorageException {
        tenantStorage().updateArtifactState(groupId, artifactId, version, state);
    }


    /**
     * @param groupId
     * @param artifactId
     * @param artifactType
     * @param content
     * @return
     * @throws ArtifactAlreadyExistsException
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#createArtifact(java.lang.String, java.lang.String, io.apicurio.registry.types.ArtifactType, io.apicurio.registry.content.ContentHandle)
     */
    @Override
    public CompletionStage<ArtifactMetaDataDto> createArtifact(String groupId, String artifactId,
            ArtifactType artifactType, ContentHandle content)
            throws ArtifactAlreadyExistsException, RegistryStorageException {
        return tenantStorage().createArtifact(groupId, artifactId, artifactType, content);
    }


    /**
     * @param groupId
     * @param artifactId
     * @param artifactType
     * @param content
     * @param metaData
     * @return
     * @throws ArtifactAlreadyExistsException
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#createArtifactWithMetadata(java.lang.String, java.lang.String, io.apicurio.registry.types.ArtifactType, io.apicurio.registry.content.ContentHandle, io.apicurio.registry.storage.dto.EditableArtifactMetaDataDto)
     */
    @Override
    public CompletionStage<ArtifactMetaDataDto> createArtifactWithMetadata(String groupId, String artifactId,
            ArtifactType artifactType, ContentHandle content, EditableArtifactMetaDataDto metaData)
            throws ArtifactAlreadyExistsException, RegistryStorageException {
        return tenantStorage().createArtifactWithMetadata(groupId, artifactId, artifactType, content, metaData);
    }


    /**
     * @param groupId
     * @param artifactId
     * @return
     * @throws ArtifactNotFoundException
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#deleteArtifact(java.lang.String, java.lang.String)
     */
    @Override
    public SortedSet<Long> deleteArtifact(String groupId, String artifactId)
            throws ArtifactNotFoundException, RegistryStorageException {
        return tenantStorage().deleteArtifact(groupId, artifactId);
    }


    /**
     * @param groupId
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#deleteArtifacts(java.lang.String)
     */
    @Override
    public void deleteArtifacts(String groupId) throws RegistryStorageException {
        tenantStorage().deleteArtifacts(groupId);
    }


    /**
     * @param groupId
     * @param artifactId
     * @return
     * @throws ArtifactNotFoundException
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#getArtifact(java.lang.String, java.lang.String)
     */
    @Override
    public StoredArtifactDto getArtifact(String groupId, String artifactId)
            throws ArtifactNotFoundException, RegistryStorageException {
        return tenantStorage().getArtifact(groupId, artifactId);
    }


    /**
     * @param contentId
     * @return
     * @throws ContentNotFoundException
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#getArtifactByContentId(long)
     */
    @Override
    public ContentHandle getArtifactByContentId(long contentId)
            throws ContentNotFoundException, RegistryStorageException {
        return tenantStorage().getArtifactByContentId(contentId);
    }


    /**
     * @param contentHash
     * @return
     * @throws ContentNotFoundException
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#getArtifactByContentHash(java.lang.String)
     */
    @Override
    public ContentHandle getArtifactByContentHash(String contentHash)
            throws ContentNotFoundException, RegistryStorageException {
        return tenantStorage().getArtifactByContentHash(contentHash);
    }


    /**
     * @param contentId
     * @return
     * @see io.apicurio.registry.storage.RegistryStorage#getArtifactVersionsByContentId(long)
     */
    @Override
    public List<ArtifactMetaDataDto> getArtifactVersionsByContentId(long contentId) {
        return tenantStorage().getArtifactVersionsByContentId(contentId);
    }


    /**
     * @param groupId
     * @param artifactId
     * @param artifactType
     * @param content
     * @return
     * @throws ArtifactNotFoundException
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#updateArtifact(java.lang.String, java.lang.String, io.apicurio.registry.types.ArtifactType, io.apicurio.registry.content.ContentHandle)
     */
    @Override
    public CompletionStage<ArtifactMetaDataDto> updateArtifact(String groupId, String artifactId,
            ArtifactType artifactType, ContentHandle content)
            throws ArtifactNotFoundException, RegistryStorageException {
        return tenantStorage().updateArtifact(groupId, artifactId, artifactType, content);
    }


    /**
     * @param groupId
     * @param artifactId
     * @param artifactType
     * @param content
     * @param metaData
     * @return
     * @throws ArtifactNotFoundException
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#updateArtifactWithMetadata(java.lang.String, java.lang.String, io.apicurio.registry.types.ArtifactType, io.apicurio.registry.content.ContentHandle, io.apicurio.registry.storage.dto.EditableArtifactMetaDataDto)
     */
    @Override
    public CompletionStage<ArtifactMetaDataDto> updateArtifactWithMetadata(String groupId, String artifactId,
            ArtifactType artifactType, ContentHandle content, EditableArtifactMetaDataDto metaData)
            throws ArtifactNotFoundException, RegistryStorageException {
        return tenantStorage().updateArtifactWithMetadata(groupId, artifactId, artifactType, content, metaData);
    }


    /**
     * @param limit
     * @return
     * @see io.apicurio.registry.storage.RegistryStorage#getArtifactIds(java.lang.Integer)
     */
    @Override
    public Set<String> getArtifactIds(Integer limit) {
        return tenantStorage().getArtifactIds(limit);
    }


    /**
     * @param filters
     * @param orderBy
     * @param orderDirection
     * @param offset
     * @param limit
     * @return
     * @see io.apicurio.registry.storage.RegistryStorage#searchArtifacts(java.util.Set, io.apicurio.registry.storage.dto.OrderBy, io.apicurio.registry.storage.dto.OrderDirection, int, int)
     */
    @Override
    public ArtifactSearchResultsDto searchArtifacts(Set<SearchFilter> filters, OrderBy orderBy,
            OrderDirection orderDirection, int offset, int limit) {
        return tenantStorage().searchArtifacts(filters, orderBy, orderDirection, offset, limit);
    }


    /**
     * @param groupId
     * @param artifactId
     * @return
     * @throws ArtifactNotFoundException
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#getArtifactMetaData(java.lang.String, java.lang.String)
     */
    @Override
    public ArtifactMetaDataDto getArtifactMetaData(String groupId, String artifactId)
            throws ArtifactNotFoundException, RegistryStorageException {
        return tenantStorage().getArtifactMetaData(groupId, artifactId);
    }


    /**
     * @param groupId
     * @param artifactId
     * @param canonical
     * @param content
     * @return
     * @throws ArtifactNotFoundException
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#getArtifactVersionMetaData(java.lang.String, java.lang.String, boolean, io.apicurio.registry.content.ContentHandle)
     */
    @Override
    public ArtifactVersionMetaDataDto getArtifactVersionMetaData(String groupId, String artifactId,
            boolean canonical, ContentHandle content)
            throws ArtifactNotFoundException, RegistryStorageException {
        return tenantStorage().getArtifactVersionMetaData(groupId, artifactId, canonical, content);
    }


    /**
     * @param globalId
     * @return
     * @throws ArtifactNotFoundException
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#getArtifactMetaData(long)
     */
    @Override
    public ArtifactMetaDataDto getArtifactMetaData(long globalId)
            throws ArtifactNotFoundException, RegistryStorageException {
        return tenantStorage().getArtifactMetaData(globalId);
    }


    /**
     * @param groupId
     * @param artifactId
     * @param metaData
     * @throws ArtifactNotFoundException
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#updateArtifactMetaData(java.lang.String, java.lang.String, io.apicurio.registry.storage.dto.EditableArtifactMetaDataDto)
     */
    @Override
    public void updateArtifactMetaData(String groupId, String artifactId,
            EditableArtifactMetaDataDto metaData) throws ArtifactNotFoundException, RegistryStorageException {
        tenantStorage().updateArtifactMetaData(groupId, artifactId, metaData);
    }


    /**
     * @param groupId
     * @param artifactId
     * @return
     * @throws ArtifactNotFoundException
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#getArtifactRules(java.lang.String, java.lang.String)
     */
    @Override
    public List<RuleType> getArtifactRules(String groupId, String artifactId)
            throws ArtifactNotFoundException, RegistryStorageException {
        return tenantStorage().getArtifactRules(groupId, artifactId);
    }


    /**
     * @param groupId
     * @param artifactId
     * @param rule
     * @param config
     * @throws ArtifactNotFoundException
     * @throws RuleAlreadyExistsException
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#createArtifactRule(java.lang.String, java.lang.String, io.apicurio.registry.types.RuleType, io.apicurio.registry.storage.dto.RuleConfigurationDto)
     */
    @Override
    public void createArtifactRule(String groupId, String artifactId, RuleType rule,
            RuleConfigurationDto config)
            throws ArtifactNotFoundException, RuleAlreadyExistsException, RegistryStorageException {
        tenantStorage().createArtifactRule(groupId, artifactId, rule, config);
    }


    /**
     * @param groupId
     * @param artifactId
     * @param rule
     * @param config
     * @return
     * @throws ArtifactNotFoundException
     * @throws RuleAlreadyExistsException
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#createArtifactRuleAsync(java.lang.String, java.lang.String, io.apicurio.registry.types.RuleType, io.apicurio.registry.storage.dto.RuleConfigurationDto)
     */
    @Override
    public CompletionStage<Void> createArtifactRuleAsync(String groupId, String artifactId, RuleType rule,
            RuleConfigurationDto config)
            throws ArtifactNotFoundException, RuleAlreadyExistsException, RegistryStorageException {
        return tenantStorage().createArtifactRuleAsync(groupId, artifactId, rule, config);
    }


    /**
     * @param groupId
     * @param artifactId
     * @throws ArtifactNotFoundException
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#deleteArtifactRules(java.lang.String, java.lang.String)
     */
    @Override
    public void deleteArtifactRules(String groupId, String artifactId)
            throws ArtifactNotFoundException, RegistryStorageException {
        tenantStorage().deleteArtifactRules(groupId, artifactId);
    }


    /**
     * @param groupId
     * @param artifactId
     * @param rule
     * @return
     * @throws ArtifactNotFoundException
     * @throws RuleNotFoundException
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#getArtifactRule(java.lang.String, java.lang.String, io.apicurio.registry.types.RuleType)
     */
    @Override
    public RuleConfigurationDto getArtifactRule(String groupId, String artifactId, RuleType rule)
            throws ArtifactNotFoundException, RuleNotFoundException, RegistryStorageException {
        return tenantStorage().getArtifactRule(groupId, artifactId, rule);
    }


    /**
     * @param groupId
     * @param artifactId
     * @param rule
     * @param config
     * @throws ArtifactNotFoundException
     * @throws RuleNotFoundException
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#updateArtifactRule(java.lang.String, java.lang.String, io.apicurio.registry.types.RuleType, io.apicurio.registry.storage.dto.RuleConfigurationDto)
     */
    @Override
    public void updateArtifactRule(String groupId, String artifactId, RuleType rule,
            RuleConfigurationDto config)
            throws ArtifactNotFoundException, RuleNotFoundException, RegistryStorageException {
        tenantStorage().updateArtifactRule(groupId, artifactId, rule, config);
    }


    /**
     * @param groupId
     * @param artifactId
     * @param rule
     * @throws ArtifactNotFoundException
     * @throws RuleNotFoundException
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#deleteArtifactRule(java.lang.String, java.lang.String, io.apicurio.registry.types.RuleType)
     */
    @Override
    public void deleteArtifactRule(String groupId, String artifactId, RuleType rule)
            throws ArtifactNotFoundException, RuleNotFoundException, RegistryStorageException {
        tenantStorage().deleteArtifactRule(groupId, artifactId, rule);
    }


    /**
     * @param groupId
     * @param artifactId
     * @return
     * @throws ArtifactNotFoundException
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#getArtifactVersions(java.lang.String, java.lang.String)
     */
    @Override
    public SortedSet<Long> getArtifactVersions(String groupId, String artifactId)
            throws ArtifactNotFoundException, RegistryStorageException {
        return tenantStorage().getArtifactVersions(groupId, artifactId);
    }


    /**
     * @param groupId
     * @param artifactId
     * @param offset
     * @param limit
     * @return
     * @throws ArtifactNotFoundException
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#searchVersions(java.lang.String, java.lang.String, int, int)
     */
    @Override
    public VersionSearchResultsDto searchVersions(String groupId, String artifactId, int offset, int limit)
            throws ArtifactNotFoundException, RegistryStorageException {
        return tenantStorage().searchVersions(groupId, artifactId, offset, limit);
    }


    /**
     * @param id
     * @return
     * @throws ArtifactNotFoundException
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#getArtifactVersion(long)
     */
    @Override
    public StoredArtifactDto getArtifactVersion(long id)
            throws ArtifactNotFoundException, RegistryStorageException {
        return tenantStorage().getArtifactVersion(id);
    }


    /**
     * @param groupId
     * @param artifactId
     * @param version
     * @return
     * @throws ArtifactNotFoundException
     * @throws VersionNotFoundException
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#getArtifactVersion(java.lang.String, java.lang.String, long)
     */
    @Override
    public StoredArtifactDto getArtifactVersion(String groupId, String artifactId, long version)
            throws ArtifactNotFoundException, VersionNotFoundException, RegistryStorageException {
        return tenantStorage().getArtifactVersion(groupId, artifactId, version);
    }


    /**
     * @param groupId
     * @param artifactId
     * @param version
     * @throws ArtifactNotFoundException
     * @throws VersionNotFoundException
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#deleteArtifactVersion(java.lang.String, java.lang.String, long)
     */
    @Override
    public void deleteArtifactVersion(String groupId, String artifactId, long version)
            throws ArtifactNotFoundException, VersionNotFoundException, RegistryStorageException {
        tenantStorage().deleteArtifactVersion(groupId, artifactId, version);
    }


    /**
     * @param groupId
     * @param artifactId
     * @param version
     * @return
     * @throws ArtifactNotFoundException
     * @throws VersionNotFoundException
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#getArtifactVersionMetaData(java.lang.String, java.lang.String, long)
     */
    @Override
    public ArtifactVersionMetaDataDto getArtifactVersionMetaData(String groupId, String artifactId,
            long version)
            throws ArtifactNotFoundException, VersionNotFoundException, RegistryStorageException {
        return tenantStorage().getArtifactVersionMetaData(groupId, artifactId, version);
    }


    /**
     * @param groupId
     * @param artifactId
     * @param version
     * @param metaData
     * @throws ArtifactNotFoundException
     * @throws VersionNotFoundException
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#updateArtifactVersionMetaData(java.lang.String, java.lang.String, long, io.apicurio.registry.storage.dto.EditableArtifactMetaDataDto)
     */
    @Override
    public void updateArtifactVersionMetaData(String groupId, String artifactId, long version,
            EditableArtifactMetaDataDto metaData)
            throws ArtifactNotFoundException, VersionNotFoundException, RegistryStorageException {
        tenantStorage().updateArtifactVersionMetaData(groupId, artifactId, version, metaData);
    }


    /**
     * @param groupId
     * @param artifactId
     * @param version
     * @throws ArtifactNotFoundException
     * @throws VersionNotFoundException
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#deleteArtifactVersionMetaData(java.lang.String, java.lang.String, long)
     */
    @Override
    public void deleteArtifactVersionMetaData(String groupId, String artifactId, long version)
            throws ArtifactNotFoundException, VersionNotFoundException, RegistryStorageException {
        tenantStorage().deleteArtifactVersionMetaData(groupId, artifactId, version);
    }


    /**
     * @return
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#getGlobalRules()
     */
    @Override
    public List<RuleType> getGlobalRules() throws RegistryStorageException {
        return tenantStorage().getGlobalRules();
    }


    /**
     * @param rule
     * @param config
     * @throws RuleAlreadyExistsException
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#createGlobalRule(io.apicurio.registry.types.RuleType, io.apicurio.registry.storage.dto.RuleConfigurationDto)
     */
    @Override
    public void createGlobalRule(RuleType rule, RuleConfigurationDto config)
            throws RuleAlreadyExistsException, RegistryStorageException {
        tenantStorage().createGlobalRule(rule, config);
    }


    /**
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#deleteGlobalRules()
     */
    @Override
    public void deleteGlobalRules() throws RegistryStorageException {
        tenantStorage().deleteGlobalRules();
    }


    /**
     * @param rule
     * @return
     * @throws RuleNotFoundException
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#getGlobalRule(io.apicurio.registry.types.RuleType)
     */
    @Override
    public RuleConfigurationDto getGlobalRule(RuleType rule)
            throws RuleNotFoundException, RegistryStorageException {
        return tenantStorage().getGlobalRule(rule);
    }


    /**
     * @param rule
     * @param config
     * @throws RuleNotFoundException
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#updateGlobalRule(io.apicurio.registry.types.RuleType, io.apicurio.registry.storage.dto.RuleConfigurationDto)
     */
    @Override
    public void updateGlobalRule(RuleType rule, RuleConfigurationDto config)
            throws RuleNotFoundException, RegistryStorageException {
        tenantStorage().updateGlobalRule(rule, config);
    }


    /**
     * @param rule
     * @throws RuleNotFoundException
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#deleteGlobalRule(io.apicurio.registry.types.RuleType)
     */
    @Override
    public void deleteGlobalRule(RuleType rule) throws RuleNotFoundException, RegistryStorageException {
        tenantStorage().deleteGlobalRule(rule);
    }


    /**
     * @param tenantId
     * @return
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#getTenantMetadata(java.lang.String)
     */
    @Override
    public TenantMetadataDto getTenantMetadata(String tenantId) throws RegistryStorageException {
        TenantMetadataDto dto = new TenantMetadataDto();
        dto.setTenantId(tenantId);
        return dto;
    }


    /**
     * @param logger
     * @return
     * @throws RegistryStorageException
     * @throws LogConfigurationNotFoundException
     * @see io.apicurio.registry.storage.RegistryStorage#getLogConfiguration(java.lang.String)
     */
    @Override
    public LogConfigurationDto getLogConfiguration(String logger)
            throws RegistryStorageException, LogConfigurationNotFoundException {
        return tenantStorage().getLogConfiguration(logger);
    }


    /**
     * @param logConfiguration
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#setLogConfiguration(io.apicurio.registry.storage.dto.LogConfigurationDto)
     */
    @Override
    public void setLogConfiguration(LogConfigurationDto logConfiguration) throws RegistryStorageException {
        tenantStorage().setLogConfiguration(logConfiguration);
    }


    /**
     * @param logger
     * @throws RegistryStorageException
     * @throws LogConfigurationNotFoundException
     * @see io.apicurio.registry.storage.RegistryStorage#removeLogConfiguration(java.lang.String)
     */
    @Override
    public void removeLogConfiguration(String logger)
            throws RegistryStorageException, LogConfigurationNotFoundException {
        tenantStorage().removeLogConfiguration(logger);
    }


    /**
     * @return
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#listLogConfigurations()
     */
    @Override
    public List<LogConfigurationDto> listLogConfigurations() throws RegistryStorageException {
        return tenantStorage().listLogConfigurations();
    }


    /**
     * @param group
     * @throws GroupAlreadyExistsException
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#createGroup(io.apicurio.registry.storage.dto.GroupMetaDataDto)
     */
    @Override
    public void createGroup(GroupMetaDataDto group)
            throws GroupAlreadyExistsException, RegistryStorageException {
        tenantStorage().createGroup(group);
    }


    /**
     * @param group
     * @throws GroupNotFoundException
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#updateGroupMetaData(io.apicurio.registry.storage.dto.GroupMetaDataDto)
     */
    @Override
    public void updateGroupMetaData(GroupMetaDataDto group)
            throws GroupNotFoundException, RegistryStorageException {
        tenantStorage().updateGroupMetaData(group);
    }


    /**
     * @param groupId
     * @throws GroupNotFoundException
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#deleteGroup(java.lang.String)
     */
    @Override
    public void deleteGroup(String groupId) throws GroupNotFoundException, RegistryStorageException {
        tenantStorage().deleteGroup(groupId);
    }


    /**
     * @param limit
     * @return
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#getGroupIds(java.lang.Integer)
     */
    @Override
    public List<String> getGroupIds(Integer limit) throws RegistryStorageException {
        return tenantStorage().getGroupIds(limit);
    }


    /**
     * @param groupId
     * @return
     * @throws GroupNotFoundException
     * @throws RegistryStorageException
     * @see io.apicurio.registry.storage.RegistryStorage#getGroupMetaData(java.lang.String)
     */
    @Override
    public GroupMetaDataDto getGroupMetaData(String groupId)
            throws GroupNotFoundException, RegistryStorageException {
        return tenantStorage().getGroupMetaData(groupId);
    }

    private class SimpleInMemoryRegistryStorage extends SimpleMapRegistryStorage {

        private AtomicLong globalIdCounter = new AtomicLong(1);
        private AtomicLong contentIdCounter = new AtomicLong(1);

        /**
         * Constructor.
         * @param factory
         * @param securityIdentity
         */
        public SimpleInMemoryRegistryStorage(ArtifactTypeUtilProviderFactory factory, SecurityIdentity securityIdentity) {
            super(factory, securityIdentity);
        }

        @Override
        protected long nextGlobalId() {
            return globalIdCounter.getAndIncrement();
        }

        @Override
        protected long nextContentId() {
            return contentIdCounter.getAndIncrement();
        }
    }

}
