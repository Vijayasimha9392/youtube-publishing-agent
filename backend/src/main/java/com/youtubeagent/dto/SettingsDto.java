package com.youtubeagent.dto;

public class SettingsDto {
    private String monitoredFolder;
    private String geminiApiKey;
    private String youtubeClientId;
    private String youtubeClientSecret;
    private String youtubeRedirectUri;
    private String defaultDescriptionTemplate;
    private String defaultTags;
    private boolean simulateUpload;
    private boolean autoDeleteOnPublish;

    public SettingsDto() {}

    public String getMonitoredFolder() { return monitoredFolder; }
    public void setMonitoredFolder(String monitoredFolder) { this.monitoredFolder = monitoredFolder; }

    public String getGeminiApiKey() { return geminiApiKey; }
    public void setGeminiApiKey(String geminiApiKey) { this.geminiApiKey = geminiApiKey; }

    public String getYoutubeClientId() { return youtubeClientId; }
    public void setYoutubeClientId(String youtubeClientId) { this.youtubeClientId = youtubeClientId; }

    public String getYoutubeClientSecret() { return youtubeClientSecret; }
    public void setYoutubeClientSecret(String youtubeClientSecret) { this.youtubeClientSecret = youtubeClientSecret; }

    public String getYoutubeRedirectUri() { return youtubeRedirectUri; }
    public void setYoutubeRedirectUri(String youtubeRedirectUri) { this.youtubeRedirectUri = youtubeRedirectUri; }

    public String getDefaultDescriptionTemplate() { return defaultDescriptionTemplate; }
    public void setDefaultDescriptionTemplate(String defaultDescriptionTemplate) { this.defaultDescriptionTemplate = defaultDescriptionTemplate; }

    public String getDefaultTags() { return defaultTags; }
    public void setDefaultTags(String defaultTags) { this.defaultTags = defaultTags; }

    public boolean isSimulateUpload() { return simulateUpload; }
    public void setSimulateUpload(boolean simulateUpload) { this.simulateUpload = simulateUpload; }

    public boolean isAutoDeleteOnPublish() { return autoDeleteOnPublish; }
    public void setAutoDeleteOnPublish(boolean autoDeleteOnPublish) { this.autoDeleteOnPublish = autoDeleteOnPublish; }
}
