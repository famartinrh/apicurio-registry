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
package io.apicurio.registry.utils.cloudevents;

import java.time.OffsetDateTime;

import lombok.Builder;

/**
 * @author Fabian Martinez
 */
@Builder(builderClassName = "CloudEventBuilder", builderMethodName = "", toBuilder = true, setterPrefix = "with")
public class CloudEventImpl<T> implements CloudEvent<T> {

    private String id;
    private String specVersion;
    private String source;
    private String subject;
    private String type;
    private OffsetDateTime time;
    private String datacontenttype;
    private String dataschema;
    private T data;

    public static <T> CloudEvent<T> from(CloudEventAttributes attrs) {
        return new CloudEventBuilder<T>()
                .withId(attrs.id())
                .withSpecVersion(attrs.specVersion())
                .withSource(attrs.source())
                .withSubject(attrs.subject())
                .withType(attrs.type())
                .withTime(attrs.time())
                .withDatacontenttype(attrs.datacontenttype())
                .withDataschema(attrs.dataschema())
                .build();
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String specVersion() {
        return specVersion;
    }

    @Override
    public String source() {
        return source;
    }

    @Override
    public String subject() {
        return subject;
    }

    @Override
    public OffsetDateTime time() {
        return time;
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public String datacontenttype() {
        return datacontenttype;
    }

    @Override
    public String dataschema() {
        return dataschema;
    }

    @Override
    public T data() {
        return data;
    }

}
