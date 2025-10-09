package org.jboss.pnc.rpm.importer.model.brew;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "build_id",
        "cg_id",
        "completion_time",
        "completion_ts",
        "creation_event_id",
        "creation_time",
        "creation_ts",
        "draft",
        "epoch",
        "extra",
        "id",
        "name",
        "nvr",
        "owner_id",
        "owner_name",
        "package_id",
        "package_name",
        "promoter_id",
        "promoter_name",
        "promotion_time",
        "promotion_ts",
        "release",
        "source",
        "start_time",
        "start_ts",
        "state",
        "task_id",
        "version",
        "volume_id",
        "volume_name",
        "cg_name"
})
public class BuildInfo {

    @JsonProperty("build_id")
    private Integer buildId;
    @JsonProperty("cg_id")
    private Integer cgId;
    @JsonProperty("completion_time")
    private String completionTime;
    @JsonProperty("completion_ts")
    private Float completionTs;
    @JsonProperty("creation_event_id")
    private Integer creationEventId;
    @JsonProperty("creation_time")
    private String creationTime;
    @JsonProperty("creation_ts")
    private Float creationTs;
    @JsonProperty("draft")
    private Boolean draft;
    @JsonProperty("epoch")
    private Object epoch;
    @JsonProperty("extra")
    private Extra extra;
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("nvr")
    private String nvr;
    @JsonProperty("owner_id")
    private Integer ownerId;
    @JsonProperty("owner_name")
    private String ownerName;
    @JsonProperty("package_id")
    private Integer packageId;
    @JsonProperty("package_name")
    private String packageName;
    @JsonProperty("promoter_id")
    private Object promoterId;
    @JsonProperty("promoter_name")
    private Object promoterName;
    @JsonProperty("promotion_time")
    private Object promotionTime;
    @JsonProperty("promotion_ts")
    private Object promotionTs;
    @JsonProperty("release")
    private String release;
    @JsonProperty("source")
    private String source;
    @JsonProperty("start_time")
    private String startTime;
    @JsonProperty("start_ts")
    private Float startTs;
    @JsonProperty("state")
    private Integer state;
    @JsonProperty("task_id")
    private Integer taskId;
    @JsonProperty("version")
    private String version;
    @JsonProperty("volume_id")
    private Integer volumeId;
    @JsonProperty("volume_name")
    private String volumeName;
    @JsonProperty("cg_name")
    private String cgName;

    @JsonProperty("build_id")
    public Integer getBuildId() {
        return buildId;
    }

    @JsonProperty("build_id")
    public void setBuildId(Integer buildId) {
        this.buildId = buildId;
    }

    @JsonProperty("cg_id")
    public Integer getCgId() {
        return cgId;
    }

    @JsonProperty("cg_id")
    public void setCgId(Integer cgId) {
        this.cgId = cgId;
    }

    @JsonProperty("completion_time")
    public String getCompletionTime() {
        return completionTime;
    }

    @JsonProperty("completion_time")
    public void setCompletionTime(String completionTime) {
        this.completionTime = completionTime;
    }

    @JsonProperty("completion_ts")
    public Float getCompletionTs() {
        return completionTs;
    }

    @JsonProperty("completion_ts")
    public void setCompletionTs(Float completionTs) {
        this.completionTs = completionTs;
    }

    @JsonProperty("creation_event_id")
    public Integer getCreationEventId() {
        return creationEventId;
    }

    @JsonProperty("creation_event_id")
    public void setCreationEventId(Integer creationEventId) {
        this.creationEventId = creationEventId;
    }

    @JsonProperty("creation_time")
    public String getCreationTime() {
        return creationTime;
    }

    @JsonProperty("creation_time")
    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    @JsonProperty("creation_ts")
    public Float getCreationTs() {
        return creationTs;
    }

    @JsonProperty("creation_ts")
    public void setCreationTs(Float creationTs) {
        this.creationTs = creationTs;
    }

    @JsonProperty("draft")
    public Boolean getDraft() {
        return draft;
    }

    @JsonProperty("draft")
    public void setDraft(Boolean draft) {
        this.draft = draft;
    }

    @JsonProperty("epoch")
    public Object getEpoch() {
        return epoch;
    }

    @JsonProperty("epoch")
    public void setEpoch(Object epoch) {
        this.epoch = epoch;
    }

    @JsonProperty("extra")
    public Extra getExtra() {
        return extra;
    }

    @JsonProperty("extra")
    public void setExtra(Extra extra) {
        this.extra = extra;
    }

    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(Integer id) {
        this.id = id;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("nvr")
    public String getNvr() {
        return nvr;
    }

    @JsonProperty("nvr")
    public void setNvr(String nvr) {
        this.nvr = nvr;
    }

    @JsonProperty("owner_id")
    public Integer getOwnerId() {
        return ownerId;
    }

    @JsonProperty("owner_id")
    public void setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
    }

    @JsonProperty("owner_name")
    public String getOwnerName() {
        return ownerName;
    }

    @JsonProperty("owner_name")
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    @JsonProperty("package_id")
    public Integer getPackageId() {
        return packageId;
    }

    @JsonProperty("package_id")
    public void setPackageId(Integer packageId) {
        this.packageId = packageId;
    }

    @JsonProperty("package_name")
    public String getPackageName() {
        return packageName;
    }

    @JsonProperty("package_name")
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    @JsonProperty("promoter_id")
    public Object getPromoterId() {
        return promoterId;
    }

    @JsonProperty("promoter_id")
    public void setPromoterId(Object promoterId) {
        this.promoterId = promoterId;
    }

    @JsonProperty("promoter_name")
    public Object getPromoterName() {
        return promoterName;
    }

    @JsonProperty("promoter_name")
    public void setPromoterName(Object promoterName) {
        this.promoterName = promoterName;
    }

    @JsonProperty("promotion_time")
    public Object getPromotionTime() {
        return promotionTime;
    }

    @JsonProperty("promotion_time")
    public void setPromotionTime(Object promotionTime) {
        this.promotionTime = promotionTime;
    }

    @JsonProperty("promotion_ts")
    public Object getPromotionTs() {
        return promotionTs;
    }

    @JsonProperty("promotion_ts")
    public void setPromotionTs(Object promotionTs) {
        this.promotionTs = promotionTs;
    }

    @JsonProperty("release")
    public String getRelease() {
        return release;
    }

    @JsonProperty("release")
    public void setRelease(String release) {
        this.release = release;
    }

    @JsonProperty("source")
    public String getSource() {
        return source;
    }

    @JsonProperty("source")
    public void setSource(String source) {
        this.source = source;
    }

    @JsonProperty("start_time")
    public String getStartTime() {
        return startTime;
    }

    @JsonProperty("start_time")
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    @JsonProperty("start_ts")
    public Float getStartTs() {
        return startTs;
    }

    @JsonProperty("start_ts")
    public void setStartTs(Float startTs) {
        this.startTs = startTs;
    }

    @JsonProperty("state")
    public Integer getState() {
        return state;
    }

    @JsonProperty("state")
    public void setState(Integer state) {
        this.state = state;
    }

    @JsonProperty("task_id")
    public Integer getTaskId() {
        return taskId;
    }

    @JsonProperty("task_id")
    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    @JsonProperty("volume_id")
    public Integer getVolumeId() {
        return volumeId;
    }

    @JsonProperty("volume_id")
    public void setVolumeId(Integer volumeId) {
        this.volumeId = volumeId;
    }

    @JsonProperty("volume_name")
    public String getVolumeName() {
        return volumeName;
    }

    @JsonProperty("volume_name")
    public void setVolumeName(String volumeName) {
        this.volumeName = volumeName;
    }

    @JsonProperty("cg_name")
    public String getCgName() {
        return cgName;
    }

    @JsonProperty("cg_name")
    public void setCgName(String cgName) {
        this.cgName = cgName;
    }

}
