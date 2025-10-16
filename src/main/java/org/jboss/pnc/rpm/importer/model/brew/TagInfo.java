package org.jboss.pnc.rpm.importer.model.brew;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "arches",
        "id",
        "locked",
        "maven_include_all",
        "maven_support",
        "name",
        "perm",
        "perm_id",
        "extra"
})
public class TagInfo {

    @JsonProperty("arches")
    private String arches;
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("locked")
    private Boolean locked;
    @JsonProperty("maven_include_all")
    private Boolean mavenIncludeAll;
    @JsonProperty("maven_support")
    private Boolean mavenSupport;
    @JsonProperty("name")
    private String name;
    @JsonProperty("perm")
    private Object perm;
    @JsonProperty("perm_id")
    private Object permId;
    @JsonProperty("extra")
    private Extra extra;

    @JsonProperty("arches")
    public String getArches() {
        return arches;
    }

    @JsonProperty("arches")
    public void setArches(String arches) {
        this.arches = arches;
    }

    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(Integer id) {
        this.id = id;
    }

    @JsonProperty("locked")
    public Boolean getLocked() {
        return locked;
    }

    @JsonProperty("locked")
    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    @JsonProperty("maven_include_all")
    public Boolean getMavenIncludeAll() {
        return mavenIncludeAll;
    }

    @JsonProperty("maven_include_all")
    public void setMavenIncludeAll(Boolean mavenIncludeAll) {
        this.mavenIncludeAll = mavenIncludeAll;
    }

    @JsonProperty("maven_support")
    public Boolean getMavenSupport() {
        return mavenSupport;
    }

    @JsonProperty("maven_support")
    public void setMavenSupport(Boolean mavenSupport) {
        this.mavenSupport = mavenSupport;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("perm")
    public Object getPerm() {
        return perm;
    }

    @JsonProperty("perm")
    public void setPerm(Object perm) {
        this.perm = perm;
    }

    @JsonProperty("perm_id")
    public Object getPermId() {
        return permId;
    }

    @JsonProperty("perm_id")
    public void setPermId(Object permId) {
        this.permId = permId;
    }

    @JsonProperty("extra")
    public Extra getExtra() {
        return extra;
    }

    @JsonProperty("extra")
    public void setExtra(Extra extra) {
        this.extra = extra;
    }

}
