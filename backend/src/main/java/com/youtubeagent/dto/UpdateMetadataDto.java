package com.youtubeagent.dto;

public class UpdateMetadataDto {
    private String title;
    private String description;
    private String tags;
    private String hashtags;
    private String categoryId;
    private String privacyStatus;
    private Boolean isMadeForKids;

    public UpdateMetadataDto() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public String getHashtags() { return hashtags; }
    public void setHashtags(String hashtags) { this.hashtags = hashtags; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getPrivacyStatus() { return privacyStatus; }
    public void setPrivacyStatus(String privacyStatus) { this.privacyStatus = privacyStatus; }

    public Boolean getIsMadeForKids() { return isMadeForKids; }
    public void setIsMadeForKids(Boolean isMadeForKids) { this.isMadeForKids = isMadeForKids; }
}
