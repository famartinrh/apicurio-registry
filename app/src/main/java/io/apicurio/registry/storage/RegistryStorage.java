/*
 * Copyright 2020 Red Hat
 * Copyright 2020 IBM
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

package io.apicurio.registry.storage;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.CompletionStage;

import io.apicurio.registry.content.ContentHandle;
import io.apicurio.registry.storage.dto.ArtifactMetaDataDto;
import io.apicurio.registry.storage.dto.ArtifactSearchResultsDto;
import io.apicurio.registry.storage.dto.ArtifactVersionMetaDataDto;
import io.apicurio.registry.storage.dto.EditableArtifactMetaDataDto;
import io.apicurio.registry.storage.dto.OrderBy;
import io.apicurio.registry.storage.dto.OrderDirection;
import io.apicurio.registry.storage.dto.RuleConfigurationDto;
import io.apicurio.registry.storage.dto.SearchFilter;
import io.apicurio.registry.storage.dto.StoredArtifactDto;
import io.apicurio.registry.storage.dto.VersionSearchResultsDto;
import io.apicurio.registry.types.ArtifactState;
import io.apicurio.registry.types.ArtifactType;
import io.apicurio.registry.types.RuleType;
import io.apicurio.registry.utils.ConcurrentUtil;


/**
 * The artifactStore layer for the registry.
 *
 * @author eric.wittmann@gmail.com
 * @author Ales Justin
 */
public interface RegistryStorage {

    /**
     * Is the artifactStore ready?
     * <p>
     * By default we check if it can access list of globalIdStore rules.
     *
     * @return true if yes, false if no
     */
    default boolean isReady() {
        return (getGlobalRules() != null);
    }

    /**
     * Is the artifactStore alive?
     * <p>
     * By default it's true.
     *
     * @return true if yes, false if no
     */
    default boolean isAlive() {
        return true;
    }

    /**
     * Update artifact state.
     * @param groupId
     * @param artifactId
     * @param state
     * @throws ArtifactNotFoundException
     * @throws RegistryStorageException
     */
    public void updateArtifactState(String groupId, String artifactId, ArtifactState state) 
            throws ArtifactNotFoundException, RegistryStorageException;

    /**
     * Update artifact state.
     * @param groupId
     * @param artifactId
     * @param version
     * @param state
     * @throws ArtifactNotFoundException
     * @throws VersionNotFoundException
     * @throws RegistryStorageException
     */
    public void updateArtifactState(String groupId, String artifactId, Integer version, ArtifactState state)
            throws ArtifactNotFoundException, VersionNotFoundException, RegistryStorageException;

    /**
     * Creates a new artifact (from the given value) in the artifactStore.  The artifactId must be unique
     * within the given artifact group.  Returns a map of meta-data generated by the artifactStore layer, such as the 
     * generated, globally unique versionId of the new value.
     * @param groupId
     * @param artifactId
     * @param artifactType
     * @param content
     * @throws ArtifactAlreadyExistsException
     * @throws RegistryStorageException
     */
    public CompletionStage<ArtifactMetaDataDto> createArtifact(String groupId, String artifactId, ArtifactType artifactType, ContentHandle content)
            throws ArtifactAlreadyExistsException, RegistryStorageException;

    /**
     * Creates a new artifact (from the given value including metadata) in the artifactStore.  The artifactId must be unique
     * within the given artifact group. Returns a map of meta-data generated by the artifactStore layer, such as the
     * generated, globally unique versionId of the new value.
     * @param groupId
     * @param artifactId
     * @param artifactType
     * @param content
     * @param metaData
     * @throws ArtifactAlreadyExistsException
     * @throws RegistryStorageException
     */
    public CompletionStage<ArtifactMetaDataDto> createArtifactWithMetadata(String groupId, String artifactId, ArtifactType artifactType, ContentHandle content, EditableArtifactMetaDataDto metaData)
            throws ArtifactAlreadyExistsException, RegistryStorageException;

    /**
     * Deletes an artifact by its group and unique id. Returns list of artifact versions.
     * @param groupId
     * @param artifactId
     * @throws ArtifactNotFoundException
     * @throws RegistryStorageException
     */
    public SortedSet<Long> deleteArtifact(String groupId, String artifactId) throws ArtifactNotFoundException, RegistryStorageException;

    /**
     * Deletes all artifacts in the given group.
     * @param groupId
     * @throws RegistryStorageException
     */
    public void deleteArtifacts(String groupId) throws RegistryStorageException;

    /**
     * Gets the most recent version of the value of the artifact with the given group and ID.
     * @param groupId
     * @param artifactId
     * @throws ArtifactNotFoundException
     * @throws RegistryStorageException
     */
    public StoredArtifactDto getArtifact(String groupId, String artifactId) throws ArtifactNotFoundException, RegistryStorageException;

    /**
     * Updates the artifact value by storing the given value as a new version of the artifact.  Previous value
     * is NOT overwitten.  Returns a map of meta-data generated by the artifactStore layer, such as the generated,
     * globally unique versionId of the new value.
     * @param groupId
     * @param artifactId
     * @param artifactType
     * @param content
     * @throws ArtifactNotFoundException
     * @throws RegistryStorageException
     */
    public CompletionStage<ArtifactMetaDataDto> updateArtifact(String groupId, String artifactId, ArtifactType artifactType, ContentHandle content) throws ArtifactNotFoundException, RegistryStorageException;

    /**
     * Updates the artifact value by storing the given value and metadata as a new version of the artifact.  Previous value
     * is NOT overwitten.  Returns a map of meta-data generated by the artifactStore layer, such as the generated,
     * globally unique versionId of the new value.
     * @param groupId
     * @param artifactId
     * @param artifactType
     * @param content
     * @param metaData
     * @throws ArtifactNotFoundException
     * @throws RegistryStorageException
     */
    public CompletionStage<ArtifactMetaDataDto> updateArtifactWithMetadata(String groupId, String artifactId, ArtifactType artifactType, 
            ContentHandle content, EditableArtifactMetaDataDto metaData) throws ArtifactNotFoundException, RegistryStorageException;

    /**
     * Get all artifact ids.  
     * ---
     * Note: This should only be used in older APIs such as the registry V1 REST API and the Confluent API
     * ---
     * @return all artifact ids
     * @param limit the limit of artifacts
     */
    public Set<String> getArtifactIds(Integer limit);

    /**
     * Search artifacts by given criteria
     * @param filters the set of filters to apply when searching
     * @param orderBy the field to order by
     * @param orderDirection the direction to order the results
     * @param offset the number of artifacts to skip
     * @param limit the result size limit
     */
    public ArtifactSearchResultsDto searchArtifacts(Set<SearchFilter> filters, OrderBy orderBy, OrderDirection orderDirection, 
            int offset, int limit);

    /**
     * Gets the stored meta-data for an artifact by group and ID.  This will include client-editable meta-data such as 
     * name and description, but also generated meta-data such as "modifedOn" and "versionId".
     * @param groupId
     * @param artifactId
     * @throws ArtifactNotFoundException
     * @throws RegistryStorageException
     */
    public ArtifactMetaDataDto getArtifactMetaData(String groupId, String artifactId) throws ArtifactNotFoundException, RegistryStorageException;

    /**
     * Gets the metadata of the version that matches content.
     * @param groupId
     * @param artifactId
     * @param canonical
     * @param content
     * @throws ArtifactNotFoundException
     * @throws RegistryStorageException
     */
    public ArtifactVersionMetaDataDto getArtifactVersionMetaData(String groupId, String artifactId, boolean canonical,
            ContentHandle content) throws ArtifactNotFoundException, RegistryStorageException;

    /**
     * Gets the stored meta-data for an artifact by globalIdStore ID.  This will include client-editable meta-data such as
     * name and description, but also generated meta-data such as "modifedOn" and "versionId".
     * @param globalId
     * @throws ArtifactNotFoundException
     * @throws RegistryStorageException
     */
    public ArtifactMetaDataDto getArtifactMetaData(long globalId) throws ArtifactNotFoundException, RegistryStorageException;

    /**
     * Updates the stored meta-data for an artifact by group and ID.  Only the client-editable meta-data can be updated.  Client
     * editable meta-data includes e.g. name and description. TODO what if set to null?
     * @param groupId
     * @param artifactId
     * @param metaData
     * @throws ArtifactNotFoundException
     * @throws RegistryStorageException
     */
    public void updateArtifactMetaData(String groupId, String artifactId, EditableArtifactMetaDataDto metaData) throws ArtifactNotFoundException, RegistryStorageException;
    
    /**
     * Gets a list of rules configured for a specific Artifact (by group and ID).  This will return only the names of the
     * rules.
     * @param groupId
     * @param artifactId
     * @throws ArtifactNotFoundException
     * @throws RegistryStorageException
     */
    public List<RuleType> getArtifactRules(String groupId, String artifactId) throws ArtifactNotFoundException, RegistryStorageException;

    /**
     * Creates an artifact rule for a specific Artifact.  If the named rule already exists for the artifact, then
     * this should fail.
     * @param groupId
     * @param artifactId
     * @param rule
     * @param config
     * @throws ArtifactNotFoundException
     * @throws RuleAlreadyExistsException
     * @throws RegistryStorageException
     */
    public default void createArtifactRule(String groupId, String artifactId, RuleType rule, RuleConfigurationDto config)
            throws ArtifactNotFoundException, RuleAlreadyExistsException, RegistryStorageException {
        ConcurrentUtil.result(createArtifactRuleAsync(groupId, artifactId, rule, config));
    }
    public CompletionStage<Void> createArtifactRuleAsync(String groupId, String artifactId, RuleType rule, RuleConfigurationDto config) 
            throws ArtifactNotFoundException, RuleAlreadyExistsException, RegistryStorageException;

    /**
     * Deletes all rules stored/configured for the artifact.
     * @param groupId
     * @param artifactId
     * @throws ArtifactNotFoundException
     * @throws RegistryStorageException
     */
    public void deleteArtifactRules(String groupId, String artifactId) throws ArtifactNotFoundException, RegistryStorageException;

    /**
     * Gets all of the information for a single rule configured on a given artifact.
     * @param groupId
     * @param artifactId
     * @param rule
     * @throws ArtifactNotFoundException
     * @throws RuleNotFoundException
     * @throws RegistryStorageException
     */
    public RuleConfigurationDto getArtifactRule(String groupId, String artifactId, RuleType rule)
            throws ArtifactNotFoundException, RuleNotFoundException, RegistryStorageException;

    /**
     * Updates the configuration information for a single rule on a given artifact.
     * @param groupId
     * @param artifactId
     * @param rule
     * @param config
     * @throws ArtifactNotFoundException
     * @throws RuleNotFoundException
     * @throws RegistryStorageException
     */
    public void updateArtifactRule(String groupId, String artifactId, RuleType rule, RuleConfigurationDto config)
            throws ArtifactNotFoundException, RuleNotFoundException, RegistryStorageException;
    
    /**
     * Deletes a single stored/configured rule for a given artifact.
     * @param groupId
     * @param artifactId
     * @param rule
     * @throws ArtifactNotFoundException
     * @throws RuleNotFoundException
     * @throws RegistryStorageException
     */
    public void deleteArtifactRule(String groupId, String artifactId, RuleType rule) throws ArtifactNotFoundException, RuleNotFoundException, RegistryStorageException;
    
    /**
     * Gets a sorted set of all artifact versions that exist for a given artifact.
     * @param groupId
     * @param artifactId
     * @throws ArtifactNotFoundException
     * @throws RegistryStorageException
     */
    public SortedSet<Long> getArtifactVersions(String groupId, String artifactId) throws ArtifactNotFoundException, RegistryStorageException;

    /**
     * Fetch the versions of the given artifact
     * @param groupId
     * @param artifactId the artifact used to fetch versions
     * @param limit the result size limit
     * @param offset the number of versions to skip
     * @return the artifact versions, limited
     * @throws ArtifactNotFoundException
     * @throws RegistryStorageException
     */
    public VersionSearchResultsDto searchVersions(String groupId, String artifactId, int offset, int limit) throws ArtifactNotFoundException, RegistryStorageException;

    /**
     * Gets the stored artifact content for the artifact version with the given unique globalIdStore ID.
     * @param id
     * @throws ArtifactNotFoundException
     * @throws RegistryStorageException
     */
    public StoredArtifactDto getArtifactVersion(long id) throws ArtifactNotFoundException, RegistryStorageException;

    /**
     * Gets the stored value for a single version of a given artifact.
     * @param groupId
     * @param artifactId
     * @param version
     * @throws ArtifactNotFoundException
     * @throws VersionNotFoundException
     * @throws RegistryStorageException
     */
    public StoredArtifactDto getArtifactVersion(String groupId, String artifactId, long version) throws ArtifactNotFoundException, VersionNotFoundException, RegistryStorageException;

    /**
     * Deletes a single version of a given artifact.
     * @param groupId
     * @param artifactId
     * @param version
     * @throws ArtifactNotFoundException
     * @throws VersionNotFoundException
     * @throws RegistryStorageException
     */
    public void deleteArtifactVersion(String groupId, String artifactId, long version) throws ArtifactNotFoundException, VersionNotFoundException, RegistryStorageException;

    /**
     * Gets the stored meta-data for a single version of an artifact.  This will return all meta-data for the
     * version, including any user edited meta-data along with anything generated by the artifactStore.
     * @param groupId
     * @param artifactId
     * @param version
     * @throws ArtifactNotFoundException
     * @throws VersionNotFoundException
     * @throws RegistryStorageException
     */
    public ArtifactVersionMetaDataDto getArtifactVersionMetaData(String groupId, String artifactId, long version) throws ArtifactNotFoundException, VersionNotFoundException, RegistryStorageException;
    
    /**
     * Updates the user-editable meta-data for a single version of a given artifact.  Only the client-editable 
     * meta-data can be updated.  Client editable meta-data includes e.g. name and description.
     * @param groupId
     * @param artifactId
     * @param version
     * @param metaData
     * @throws ArtifactNotFoundException
     * @throws VersionNotFoundException
     * @throws RegistryStorageException
     */
    public void updateArtifactVersionMetaData(String groupId, String artifactId, long version, EditableArtifactMetaDataDto metaData) throws ArtifactNotFoundException, VersionNotFoundException, RegistryStorageException;
    
    /**
     * Deletes the user-editable meta-data for a singel version of a given artifact.  Only the client-editable
     * meta-data is deleted.  Any meta-data generated by the artifactStore is preserved.
     * @param groupId
     * @param artifactId
     * @param version
     * @throws ArtifactNotFoundException
     * @throws VersionNotFoundException
     * @throws RegistryStorageException
     */
    public void deleteArtifactVersionMetaData(String groupId, String artifactId, long version) throws ArtifactNotFoundException, VersionNotFoundException, RegistryStorageException;
    
    /**
     * Gets a list of all globalIdStore rule names.
     * @throws RegistryStorageException
     */
    public List<RuleType> getGlobalRules() throws RegistryStorageException;

    /**
     * Creates a single globalIdStore rule.  Duplicates (by name) are not allowed.  Stores the rule name and configuration.
     * @param rule
     * @param config
     * @throws RuleAlreadyExistsException
     * @throws RegistryStorageException
     */
    public void createGlobalRule(RuleType rule, RuleConfigurationDto config) throws RuleAlreadyExistsException, RegistryStorageException;
    
    /**
     * Deletes all of the globally configured rules.
     * @throws RegistryStorageException
     */
    public void deleteGlobalRules() throws RegistryStorageException;
    
    /**
     * Gets all information about a single globalIdStore rule.
     * @param rule
     * @throws RuleNotFoundException
     * @throws RegistryStorageException
     */
    public RuleConfigurationDto getGlobalRule(RuleType rule) throws RuleNotFoundException, RegistryStorageException;

    /**
     * Updates the configuration settings for a single globalIdStore rule.
     * @param rule
     * @param config
     * @throws RuleNotFoundException
     * @throws RegistryStorageException
     */
    public void updateGlobalRule(RuleType rule, RuleConfigurationDto config) throws RuleNotFoundException, RegistryStorageException;

    /**
     * Deletes a single globalIdStore rule.
     * @param rule
     * @throws RuleNotFoundException
     * @throws RegistryStorageException
     */
    public void deleteGlobalRule(RuleType rule) throws RuleNotFoundException, RegistryStorageException;
}
