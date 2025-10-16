package org.jboss.pnc.rpm.importer.model.brew;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({
        "external_build_system",
        "external_build_id",
        "maven",
        "build_system",
        "import_initiator",
        "rhpkg_scl_prefix",
        "rpm.macro.dist",
        "rpm.macro.scl",
        "scmTag",
        "typeinfo"
})
public class Extra {

    @JsonProperty("external_build_system")
    private String externalBuildSystem;
    @JsonProperty("external_build_id")
    private String externalBuildId;
    @JsonProperty("maven")
    private Maven maven;
    @JsonProperty("build_system")
    private String buildSystem;
    @JsonProperty("import_initiator")
    private String importInitiator;
    @JsonProperty("rhpkg_scl_prefix")
    private String rhpkgSclPrefix;
    @JsonProperty("rpm.macro.dist")
    private String rpmMacroDist;
    @JsonProperty("rpm.macro.scl")
    private String rpmMacroScl;
    @JsonProperty("scmTag")
    private String scmTag;
    @JsonProperty("typeinfo")
    private Typeinfo typeinfo;

    @JsonProperty("external_build_system")
    public String getExternalBuildSystem() {
        return externalBuildSystem;
    }

    @JsonProperty("external_build_system")
    public void setExternalBuildSystem(String externalBuildSystem) {
        this.externalBuildSystem = externalBuildSystem;
    }

    @JsonProperty("external_build_id")
    public String getExternalBuildId() {
        return externalBuildId;
    }

    @JsonProperty("external_build_id")
    public void setExternalBuildId(String externalBuildId) {
        this.externalBuildId = externalBuildId;
    }

    @JsonProperty("maven")
    public Maven getMaven() {
        return maven;
    }

    @JsonProperty("maven")
    public void setMaven(Maven maven) {
        this.maven = maven;
    }

    @JsonProperty("build_system")
    public String getBuildSystem() {
        return buildSystem;
    }

    @JsonProperty("build_system")
    public void setBuildSystem(String buildSystem) {
        this.buildSystem = buildSystem;
    }

    @JsonProperty("import_initiator")
    public String getImportInitiator() {
        return importInitiator;
    }

    @JsonProperty("import_initiator")
    public void setImportInitiator(String importInitiator) {
        this.importInitiator = importInitiator;
    }

    @JsonProperty("rpm.macro.dist")
    public String getRpmMacroDist() {
        return rpmMacroDist;
    }

    @JsonProperty("rpm.macro.dist")
    public void setRpmMacroDist(String rpmMacroDist) {
        this.rpmMacroDist = rpmMacroDist;
    }

    @JsonProperty("rpm.macro.scl")
    public String getRpmMacroScl() {
        return rpmMacroScl;
    }

    @JsonProperty("rpm.macro.scl")
    public void setRpmMacroScl(String rpmMacroScl) {
        this.rpmMacroScl = rpmMacroScl;
    }

    @JsonProperty("rhpkg_scl_prefix")
    public String getRhpkgSclPrefix() {
        return rhpkgSclPrefix;
    }

    @JsonProperty("rhpkg_scl_prefix")
    public void setRhpkgSclPrefix(String rhpkgSclPrefix) {
        this.rhpkgSclPrefix = rhpkgSclPrefix;
    }

    @JsonProperty("scmTag")
    public String getScmTag() {
        return scmTag;
    }

    @JsonProperty("scmTag")
    public void setScmTag(String scmTag) {
        this.scmTag = scmTag;
    }

    @JsonProperty("typeinfo")
    public Typeinfo getTypeinfo() {
        return typeinfo;
    }

    @JsonProperty("typeinfo")
    public void setTypeinfo(Typeinfo typeinfo) {
        this.typeinfo = typeinfo;
    }
}
