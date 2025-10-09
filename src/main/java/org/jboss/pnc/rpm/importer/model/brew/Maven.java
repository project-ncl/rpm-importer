package org.jboss.pnc.rpm.importer.model.brew;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "group_id",
        "artifact_id",
        "version"
})
public class Maven {

    @JsonProperty("group_id")
    private String groupId;
    @JsonProperty("artifact_id")
    private String artifactId;
    @JsonProperty("version")
    private String version;

    @JsonProperty("group_id")
    public String getGroupId() {
        return groupId;
    }

    @JsonProperty("group_id")
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @JsonProperty("artifact_id")
    public String getArtifactId() {
        return artifactId;
    }

    @JsonProperty("artifact_id")
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return groupId + ":" + artifactId + ":" + version;
    }
}
