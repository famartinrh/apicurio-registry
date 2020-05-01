/**
 * @license
 * Copyright 2020 JBoss Inc
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
import React from "react";
import {
    Badge,
    DataList,
    DataListAction,
    DataListCell,
    DataListItemCells,
    DataListItemRow
} from '@patternfly/react-core';
import {Artifact} from "@apicurio/registry-models";
import "./artifactList.css";
import {ArtifactTypeIcon} from "./artifactTypeIcon";
import {Link} from "react-router-dom";
import {PureComponent, PureComponentProps, PureComponentState} from "../../../../components";

/**
 * Properties
 */
export interface ArtifactListProps extends PureComponentProps {
    artifacts: Artifact[];
}

/**
 * State
 */
// tslint:disable-next-line:no-empty-interface
export interface ArtifactListState extends PureComponentState {
}


/**
 * Models the list of artifacts.
 */
export class ArtifactList extends PureComponent<ArtifactListProps, ArtifactListState> {

    constructor(props: Readonly<ArtifactListProps>) {
        super(props);
    }

    public render(): React.ReactElement {
        return (
            <DataList aria-label="List of artifacts" className="artifact-list">
                {
                    this.props.artifacts.map( artifact =>
                            <DataListItemRow className="artifact-list-item" key={artifact.id}>
                                <DataListItemCells
                                    dataListCells={[
                                        <DataListCell key="type icon" className="type-icon-cell">
                                            <ArtifactTypeIcon type={artifact.type}/>
                                        </DataListCell>,
                                        <DataListCell key="main content" className="content-cell">
                                            <div className="artifact-title">{artifact.name}</div>
                                            <div className="artifact-description">{artifact.description}</div>
                                            <div className="artifact-tags">
                                                {
                                                    this.labels(artifact).map( label =>
                                                        <Badge key={label} isRead={true}>{label}</Badge>
                                                    )
                                                }
                                            </div>
                                        </DataListCell>
                                    ]}
                                />
                                <DataListAction
                                    id="artifact-actions"
                                    aria-labelledby="artifact-actions"
                                    aria-label="Actions"
                                >
                                    <Link className="pf-c-button pf-m-secondary" to={ `/artifacts/${artifact.id}` }>View Artifact</Link>
                                </DataListAction>
                            </DataListItemRow>
                    )
                }
            </DataList>
        );
    }

    protected initializeState(): ArtifactListState {
        return {};
    }

    private labels(artifact: Artifact): string[] {
        return artifact.labels ? artifact.labels : [];
    }

}
