package com.gb.app.VCS.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.couchbase.core.mapping.Document;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document
public class Difference implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("contentRemoved")
    private String contentRemoved;

    @JsonProperty("contentAdded")
    private String contentAdded;
}
