package com.youtubeagent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youtubeagent.model.ApiCredential;
import com.youtubeagent.model.VideoMetadata;
import com.youtubeagent.model.YouTubeAccount;
import com.youtubeagent.repository.ApiCredentialRepository;
import com.youtubeagent.repository.YouTubeAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class YouTubeApiService {

    private final YouTubeAccountRepository accountRepository;
    private final ApiCredentialRepository credentialRepository;
    private final AutomationLogService logService;
    private final ObjectMapper objectMapper;

    @Value("${agent.youtube.client-id:}")
    private String configuredClientId;

    @Value("${agent.youtube.client-secret:}")
    private String configuredClientSecret;

    @Value("${agent.youtube.redirect-uri:http://localhost:8080/api/youtube/oauth2/callback}")
    private String redirectUri;

    public static class UploadResult {
        private final boolean success;
        private final String youtubeVideoId;
        private final String youtubeUrl;
        private final String errorMessage;

        public UploadResult(boolean success, String youtubeVideoId, String youtubeUrl, String errorMessage) {
            this.success = success;
            this.youtubeVideoId = youtubeVideoId;
            this.youtubeUrl = youtubeUrl;
            this.errorMessage = errorMessage;
        }

        public boolean isSuccess() { return success; }
        public String getYoutubeVideoId() { return youtubeVideoId; }
        public String getYoutubeUrl() { return youtubeUrl; }
        public String getErrorMessage() { return errorMessage; }
    }

    public YouTubeApiService(YouTubeAccountRepository accountRepository,
                             ApiCredentialRepository credentialRepository,
                             AutomationLogService logService,
                             ObjectMapper objectMapper) {
        this.accountRepository = accountRepository;
        this.credentialRepository = credentialRepository;
        this.logService = logService;
        this.objectMapper = objectMapper;
    }

    public String getEffectiveClientId() {
        if (configuredClientId != null && !configuredClientId.isBlank()) {
            return configuredClientId;
        }
        return credentialRepository.findByProvider("YOUTUBE_OAUTH")
                .map(ApiCredential::getClientId)
                .orElse("");
    }

    public String getEffectiveClientSecret() {
        if (configuredClientSecret != null && !configuredClientSecret.isBlank()) {
            return configuredClientSecret;
        }
        return credentialRepository.findByProvider("YOUTUBE_OAUTH")
                .map(ApiCredential::getClientSecret)
                .orElse("");
    }

    public String buildAuthorizationUrl() {
        String clientId = getEffectiveClientId();
        if (clientId.isBlank()) {
            throw new IllegalStateException("Google OAuth Client ID is not configured. Please enter it in Settings.");
        }

        String scope = "https://www.googleapis.com/auth/youtube.upload https://www.googleapis.com/auth/youtube.readonly https://www.googleapis.com/auth/userinfo.profile";
        return "https://accounts.google.com/o/oauth2/v2/auth?" +
                "client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                "&response_type=code" +
                "&scope=" + URLEncoder.encode(scope, StandardCharsets.UTF_8) +
                "&access_type=offline" +
                "&prompt=consent";
    }

    public YouTubeAccount exchangeAuthCode(String code) throws Exception {
        String clientId = getEffectiveClientId();
        String clientSecret = getEffectiveClientSecret();

        String requestBody = "code=" + URLEncoder.encode(code, StandardCharsets.UTF_8) +
                "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
                "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8) +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                "&grant_type=authorization_code";

        URL tokenUrl = new URL("https://oauth2.googleapis.com/token");
        HttpURLConnection conn = (HttpURLConnection) tokenUrl.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(requestBody.getBytes(StandardCharsets.UTF_8));
        }

        if (conn.getResponseCode() != 200) {
            String err = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            throw new RuntimeException("Failed to exchange auth code: " + err);
        }

        JsonNode tokenNode;
        try (InputStream is = conn.getInputStream()) {
            tokenNode = objectMapper.readTree(is);
        }

        String accessToken = tokenNode.get("access_token").asText();
        String refreshToken = tokenNode.has("refresh_token") ? tokenNode.get("refresh_token").asText() : null;
        int expiresIn = tokenNode.path("expires_in").asInt(3600);
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expiresIn - 60);

        // Fetch channel info
        URL chUrl = new URL("https://www.googleapis.com/youtube/v3/channels?part=snippet,statistics&mine=true");
        HttpURLConnection chConn = (HttpURLConnection) chUrl.openConnection();
        chConn.setRequestMethod("GET");
        chConn.setRequestProperty("Authorization", "Bearer " + accessToken);
        chConn.setConnectTimeout(15000);
        chConn.setReadTimeout(15000);

        String channelId = "CH-" + UUID.randomUUID().toString().substring(0, 8);
        String channelTitle = "My YouTube Channel";
        String channelHandle = "@mychannel";
        String avatarUrl = "";
        long subscriberCount = 0L;

        if (chConn.getResponseCode() == 200) {
            try (InputStream is = chConn.getInputStream()) {
                JsonNode chNode = objectMapper.readTree(is);
                JsonNode items = chNode.path("items");
                if (items.isArray() && items.size() > 0) {
                    JsonNode chItem = items.get(0);
                    channelId = chItem.path("id").asText(channelId);
                    JsonNode snippet = chItem.path("snippet");
                    channelTitle = snippet.path("title").asText(channelTitle);
                    channelHandle = snippet.path("customUrl").asText(channelHandle);
                    avatarUrl = snippet.path("thumbnails").path("default").path("url").asText("");
                    subscriberCount = chItem.path("statistics").path("subscriberCount").asLong(0L);
                }
            }
        }

        YouTubeAccount account = accountRepository.findByChannelId(channelId)
                .orElse(new YouTubeAccount());

        account.setChannelId(channelId);
        account.setChannelTitle(channelTitle);
        account.setChannelHandle(channelHandle);
        account.setAvatarUrl(avatarUrl);
        account.setSubscriberCount(subscriberCount);
        account.setAccessToken(accessToken);
        if (refreshToken != null) {
            account.setRefreshToken(refreshToken);
        }
        account.setExpiresAt(expiresAt);
        account.setIsActive(true);
        account.setConnectedAt(LocalDateTime.now());

        YouTubeAccount saved = accountRepository.save(account);
        logService.info("YOUTUBE_AUTH", "Successfully connected YouTube channel: " + channelTitle + " (" + channelHandle + ")");
        return saved;
    }

    public synchronized String getValidAccessToken() {
        Optional<YouTubeAccount> activeAccOpt = accountRepository.findTopByIsActiveTrueOrderByIdDesc();
        if (activeAccOpt.isEmpty()) {
            return null;
        }

        YouTubeAccount account = activeAccOpt.get();
        if (account.getExpiresAt() != null && account.getExpiresAt().isAfter(LocalDateTime.now().plusMinutes(2))) {
            return account.getAccessToken();
        }

        if (account.getRefreshToken() == null || account.getRefreshToken().isBlank()) {
            return account.getAccessToken();
        }

        try {
            String clientId = getEffectiveClientId();
            String clientSecret = getEffectiveClientSecret();

            String body = "client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
                    "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8) +
                    "&refresh_token=" + URLEncoder.encode(account.getRefreshToken(), StandardCharsets.UTF_8) +
                    "&grant_type=refresh_token";

            URL url = new URL("https://oauth2.googleapis.com/token");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            if (conn.getResponseCode() == 200) {
                try (InputStream is = conn.getInputStream()) {
                    JsonNode node = objectMapper.readTree(is);
                    String newAccess = node.get("access_token").asText();
                    int expiresIn = node.path("expires_in").asInt(3600);
                    account.setAccessToken(newAccess);
                    account.setExpiresAt(LocalDateTime.now().plusSeconds(expiresIn - 60));
                    accountRepository.save(account);
                    return newAccess;
                }
            }
        } catch (Exception e) {
            logService.error("YOUTUBE_AUTH", "Failed to refresh YouTube access token: " + e.getMessage());
        }

        return account.getAccessToken();
    }

    public UploadResult uploadVideo(File videoFile, VideoMetadata metadata, Long videoFileId, String fileHash) {
        String token = getValidAccessToken();

        if (token == null || token.isBlank()) {
            String sim = System.getProperty("SIMULATE_YOUTUBE_UPLOAD", "false");
            if ("true".equalsIgnoreCase(sim)) {
                String simId = "sim_" + UUID.randomUUID().toString().substring(0, 11);
                String simUrl = "https://www.youtube.com/shorts/" + simId;
                logService.info("UPLOAD", "[SIMULATION MODE] Video uploaded successfully: " + simUrl, videoFileId, fileHash);
                return new UploadResult(true, simId, simUrl, null);
            }
            return new UploadResult(false, null, null,
                    "YouTube channel not connected. Please connect your YouTube channel in Settings.");
        }

        try {
            String uploadUrl = initiateResumableUpload(token, videoFile, metadata);
            String videoId = streamVideoFile(uploadUrl, videoFile);

            String youtubeUrl = "https://www.youtube.com/shorts/" + videoId;
            logService.info("UPLOAD", "YouTube upload complete! Video ID: " + videoId + " -> " + youtubeUrl, videoFileId, fileHash);
            return new UploadResult(true, videoId, youtubeUrl, null);

        } catch (Exception e) {
            logService.error("UPLOAD", "YouTube upload failed: " + e.getMessage(), videoFileId, fileHash);
            return new UploadResult(false, null, null, e.getMessage());
        }
    }

    private String initiateResumableUpload(String token, File videoFile, VideoMetadata metadata) throws Exception {
        Map<String, Object> snippet = new HashMap<>();
        snippet.put("title", metadata.getTitle());
        snippet.put("description", metadata.getDescription());
        if (metadata.getTags() != null && !metadata.getTags().isBlank()) {
            snippet.put("tags", Arrays.asList(metadata.getTags().split("\\s*,\\s*")));
        }
        snippet.put("categoryId", metadata.getCategoryId() != null ? metadata.getCategoryId() : "20");

        Map<String, Object> status = new HashMap<>();
        status.put("privacyStatus", metadata.getPrivacyStatus() != null ? metadata.getPrivacyStatus() : "public");
        status.put("selfDeclaredMadeForKids", metadata.getIsMadeForKids() != null && metadata.getIsMadeForKids());

        Map<String, Object> resource = Map.of(
                "snippet", snippet,
                "status", status
        );

        String jsonPayload = objectMapper.writeValueAsString(resource);

        URL url = new URL("https://www.googleapis.com/upload/youtube/v3/videos?uploadType=resumable&part=snippet,status");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(20000);
        conn.setReadTimeout(30000);
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("X-Upload-Content-Length", String.valueOf(videoFile.length()));
        conn.setRequestProperty("X-Upload-Content-Type", "video/mp4");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
        }

        int respCode = conn.getResponseCode();
        if (respCode != 200 && respCode != 201) {
            String err = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            throw new RuntimeException("Resumable upload initiation failed (HTTP " + respCode + "): " + err);
        }

        String location = conn.getHeaderField("Location");
        if (location == null || location.isBlank()) {
            throw new RuntimeException("YouTube did not return a resumable upload Location URL");
        }

        return location;
    }

    private String streamVideoFile(String uploadUrl, File videoFile) throws Exception {
        long fileSize = videoFile.length();
        URL url = new URL(uploadUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setDoOutput(true);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(120000);
        conn.setRequestProperty("Content-Type", "video/mp4");
        conn.setRequestProperty("Content-Length", String.valueOf(fileSize));

        try (FileInputStream fis = new FileInputStream(videoFile);
             OutputStream os = conn.getOutputStream()) {

            byte[] buffer = new byte[262144]; // 256KB buffer
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
        }

        int respCode = conn.getResponseCode();
        if (respCode == 200 || respCode == 201) {
            String respBody = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            JsonNode root = objectMapper.readTree(respBody);
            String videoId = root.path("id").asText();
            if (videoId == null || videoId.isBlank()) {
                throw new RuntimeException("Uploaded video has no ID in response: " + respBody);
            }
            return videoId;
        } else {
            InputStream errStream = conn.getErrorStream();
            String err = (errStream != null) ? new String(errStream.readAllBytes(), StandardCharsets.UTF_8) : "HTTP " + respCode;
            throw new RuntimeException("Video upload chunk transfer failed (HTTP " + respCode + "): " + err);
        }
    }
}
