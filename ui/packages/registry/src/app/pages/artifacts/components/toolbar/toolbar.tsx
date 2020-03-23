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
import React from 'react';
import {
    Button,
    ButtonVariant,
    DataToolbar,
    DataToolbarContent,
    DataToolbarItem,
    Dropdown,
    DropdownItem,
    DropdownToggle, Form,
    InputGroup,
    Text,
    TextContent,
    TextInput
} from '@patternfly/react-core';
import {SearchIcon, SortAlphaDownAltIcon, SortAlphaDownIcon} from "@patternfly/react-icons";
import "./toolbar.css";
import {PureComponent} from "../../../../components";
import {GetArtifactsCriteria, Services} from "@apicurio/registry-services";

/**
 * Properties
 */
export interface ArtifactsToolbarProps {
    artifactsCount: number|null;
    onChange: (criteria: GetArtifactsCriteria) => void
}

/**
 * State
 */
export interface ArtifactsToolbarState {
    filterIsExpanded: boolean;
    filterSelection: string;
    filterValue: string;
    ascending: boolean;
}

/**
 * Models the toolbar for the Artifacts page.
 */
export class ArtifactsToolbar extends PureComponent<ArtifactsToolbarProps, ArtifactsToolbarState> {

    constructor(props: Readonly<ArtifactsToolbarProps>) {
        super(props);
        this.state = {
            ascending: true,
            filterIsExpanded: false,
            filterSelection: "",
            filterValue: ""
        };
    }

    public render(): React.ReactElement {
        return (
            <DataToolbar id="artifacts-toolbar-1" className="artifacts-toolbar">
                <DataToolbarContent>
                    <DataToolbarItem className="filter-item">
                        <Form onSubmit={this.onFilterSubmit}>
                            <InputGroup>
                                <Dropdown
                                    onSelect={this.onFilterSelect}
                                    toggle={
                                        <DropdownToggle onToggle={this.onFilterToggle}>
                                            {this.state.filterSelection ? this.state.filterSelection : 'Everything'}
                                        </DropdownToggle>
                                    }
                                    isOpen={this.state.filterIsExpanded}
                                    dropdownItems={[
                                        <DropdownItem key="everything" component="button">Everything</DropdownItem>,
                                        <DropdownItem key="name" component="button">Name</DropdownItem>,
                                        <DropdownItem key="description" component="button">Description</DropdownItem>,
                                        <DropdownItem key="labels" component="button">Labels</DropdownItem>
                                    ]}
                                />
                                <TextInput name="filterValue" id="filterValue" type="search"
                                           onChange={this.onFilterValueChange}
                                           aria-label="search input example"/>
                                <Button variant={ButtonVariant.control}
                                        onClick={this.onFilterSubmit}
                                        aria-label="search button for search input">
                                    <SearchIcon/>
                                </Button>
                            </InputGroup>
                        </Form>
                    </DataToolbarItem>
                    <DataToolbarItem className="sort-icon-item">
                        <Button variant="plain" aria-label="edit" onClick={this.onToggleAscending}>
                            {
                                this.state.ascending ? <SortAlphaDownIcon/> : <SortAlphaDownAltIcon/>
                            }
                        </Button>
                    </DataToolbarItem>
                    <DataToolbarItem className="artifact-count-item">
                        <TextContent>
                        {
                            this.props.artifactsCount != null ?
                                <Text>{ this.props.artifactsCount } Artifacts Found</Text>
                            :
                                <Text/>
                        }
                        </TextContent>
                    </DataToolbarItem>
                </DataToolbarContent>
            </DataToolbar>
        );
    }

    private onFilterToggle = (isExpanded: boolean): void => {
        Services.getLoggerService().debug("[ArtifactsToolbar] Toggling filter dropdown.");
        this.setSingleState("filterIsExpanded", isExpanded);
    };

    private onFilterSelect = (event: React.SyntheticEvent<HTMLDivElement>|undefined): void => {
        const value: string|null = event ? event.currentTarget.textContent : null;
        Services.getLoggerService().debug("[ArtifactsToolbar] Setting filter type to: %s", value);
        this.setMultiState({
            filterIsExpanded: false,
            filterSelection: value
        });
        this.change();
    };

    private onFilterValueChange = (value: any): void => {
        Services.getLoggerService().debug("[ArtifactsToolbar] Setting filter value: %o", value);
        this.setSingleState("filterValue", value);
    };

    private onFilterSubmit = (event: any|undefined): void => {
        Services.getLoggerService().debug("[ArtifactsToolbar] Filter SUBMIT!");
        this.change();
        if (event) {
            event.preventDefault();
        }
    };

    private onToggleAscending = (): void => {
        Services.getLoggerService().debug("[ArtifactsToolbar] Toggle the ascending flag.");
        this.setSingleState("ascending", !this.state.ascending);
        this.change();
    };

    private change(): void {
        if (this.props.onChange) {
            const criteria: GetArtifactsCriteria = {
                sortAscending: this.state.ascending,
                type: this.state.filterSelection,
                value: this.state.filterValue
            };
            this.props.onChange(criteria);
        }
    }
}
