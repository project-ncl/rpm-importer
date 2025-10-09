package org.jboss.pnc.rpm.importer.model.brew;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "maven"
})
public class Typeinfo {

    @JsonProperty("maven")
    private Maven maven;

    @JsonProperty("maven")
    public Maven getMaven() {
        return maven;
    }

    @JsonProperty("maven")
    public void setMaven(Maven maven) {
        this.maven = maven;
    }

}
