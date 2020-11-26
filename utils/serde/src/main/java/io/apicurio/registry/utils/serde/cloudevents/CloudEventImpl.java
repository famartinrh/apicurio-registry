package io.apicurio.registry.utils.serde.cloudevents;


import java.time.OffsetDateTime;

/**
 * @author Fabian Martinez
 */
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

    public void setId(String id) {
        this.id = id;
    }

    public void setSpecVersion(String specVersion) {
        this.specVersion = specVersion;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTime(OffsetDateTime time) {
        this.time = time;
    }

    public void setDatacontenttype(String datacontenttype) {
        this.datacontenttype = datacontenttype;
    }

    public void setDataschema(String dataschema) {
        this.dataschema = dataschema;
    }

    public void setData(T data) {
        this.data = data;
    }

}

