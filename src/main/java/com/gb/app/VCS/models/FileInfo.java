package com.gb.app.VCS.models;

import com.couchbase.client.java.repository.annotation.Id;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gb.app.VCS.exception.CustomErrorMessage;
import lombok.Data;
import org.springframework.data.couchbase.core.mapping.Document;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document
public class FileInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private transient String id;

    @JsonProperty("filePath")
    private String path;

    @JsonProperty("fileName")
    private String name;

    @JsonProperty("_id")
    private String _id;

    @JsonProperty("_ver")
    private String _ver;

    @JsonProperty("error")
    private CustomErrorMessage error;

    @JsonProperty("content")
    private String content;

    @JsonProperty("revision")
    private int revision;

    @JsonProperty("difference")
    private Difference difference;

}
