package com.youtubeagent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youtubeagent.model.ApiCredential;
import com.youtubeagent.model.AutomationSetting;
import com.youtubeagent.repository.ApiCredentialRepository;
import com.youtubeagent.repository.AutomationSettingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class GeminiMetadataService {

    private final ApiCredentialRepository apiCredentialRepository;
    private final AutomationSettingRepository settingRepository;
    private final AutomationLogService logService;
    private final ObjectMapper objectMapper;

    @Value("${agent.gemini.api-key:}")
    private String configuredApiKey;

    @Value("${agent.gemini.model:gemini-2.5-flash}")
    private String geminiModel;

    public static class GeneratedMetadata {
        private final String title;
        private final String description;
        private final String tags;
        private final String hashtags;
        private final boolean aiGenerated;

        public GeneratedMetadata(String title, String description, String tags, String hashtags, boolean aiGenerated) {
            this.title = title;
            this.description = description;
            this.tags = tags;
            this.hashtags = hashtags;
            this.aiGenerated = aiGenerated;
        }

        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getTags() { return tags; }
        public String getHashtags() { return hashtags; }
        public boolean isAiGenerated() { return aiGenerated; }
    }

    public GeminiMetadataService(ApiCredentialRepository apiCredentialRepository,
                                 AutomationSettingRepository settingRepository,
                                 AutomationLogService logService,
                                 ObjectMapper objectMapper) {
        this.apiCredentialRepository = apiCredentialRepository;
        this.settingRepository = settingRepository;
        this.logService = logService;
        this.objectMapper = objectMapper;
    }

    public GeneratedMetadata generateMetadata(String filename, Long videoFileId, String fileHash) {
        String apiKey = getEffectiveApiKey();

        if (apiKey != null && !apiKey.isBlank()) {
            try {
                GeneratedMetadata aiMeta = callGeminiApi(apiKey, filename);
                if (aiMeta != null) {
                    logService.info("AI_METADATA", "Successfully generated AI metadata via Gemini for: " + filename, videoFileId, fileHash);
                    return aiMeta;
                }
            } catch (Exception e) {
                logService.warn("AI_METADATA", "Gemini API call failed (" + e.getMessage() + "), falling back to smart heuristic metadata for: " + filename, videoFileId, fileHash);
            }
        }

        // Smart Heuristic Fallback
        GeneratedMetadata fallback = generateHeuristicMetadata(filename);
        logService.info("AI_METADATA", "Generated heuristic metadata for: " + filename, videoFileId, fileHash);
        return fallback;
    }

    private GeneratedMetadata callGeminiApi(String apiKey, String filename) throws Exception {
        String cleanName = cleanFilename(filename);

        String prompt = "You are a master YouTube Shorts viral strategist. Given this video filename: \"" + cleanName + "\", generate optimized YouTube Shorts metadata.\n" +
                "Respond ONLY with valid JSON in this exact structure without markdown backticks:\n" +
                "{\n" +
                "  \"title\": \"Hook Title with 1-2 emojis under 95 chars #Shorts\",\n" +
                "  \"description\": \"Engaging 2-sentence description of the video. Subscribe for more crazy gaming moments!\",\n" +
                "  \"tags\": \"shorts, gta 6, gaming, viral, trending, easter egg\",\n" +
                "  \"hashtags\": \"#Shorts #GTA6 #Gaming #Viral #Trending\"\n" +
                "}";

        Map<String, Object> part = Map.of("text", prompt);
        Map<String, Object> content = Map.of("parts", List.of(part));
        Map<String, Object> requestBodyMap = Map.of("contents", List.of(content));

        String requestBody = objectMapper.writeValueAsString(requestBodyMap);
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/" + geminiModel + ":generateContent?key=" + apiKey;

        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(15000);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(requestBody.getBytes(StandardCharsets.UTF_8));
        }

        if (conn.getResponseCode() == 200) {
            try (InputStream is = conn.getInputStream()) {
                JsonNode root = objectMapper.readTree(is);
                JsonNode textNode = root.at("/candidates/0/content/parts/0/text");
                if (!textNode.isMissingNode()) {
                    String rawText = textNode.asText().trim();
                    if (rawText.startsWith("```")) {
                        rawText = rawText.replaceAll("^```[a-zA-Z]*\\s*", "").replaceAll("```$", "").trim();
                    }

                    JsonNode parsed = objectMapper.readTree(rawText);
                    String title = parsed.path("title").asText(cleanName);
                    String desc = parsed.path("description").asText();
                    String tags = parsed.path("tags").asText("shorts, gaming, viral, trending");
                    String hashtags = parsed.path("hashtags").asText("#Shorts #Viral #Gaming");

                    String fullDesc = desc + "\n\n" + hashtags + "\n\nEnjoy this viral clip! 🔥\nSubscribe for more amazing videos.";
                    return new GeneratedMetadata(title, fullDesc, tags, hashtags, true);
                }
            }
        }
        return null;
    }

    private GeneratedMetadata generateHeuristicMetadata(String filename) {
        String clean = cleanFilename(filename);

        String emoji = "🔥🎮";
        if (clean.toLowerCase().contains("cartel") || clean.toLowerCase().contains("kidnapped") || clean.toLowerCase().contains("hacker")) {
            emoji = "😱🚨";
        } else if (clean.toLowerCase().contains("scam") || clean.toLowerCase().contains("spent") || clean.toLowerCase().contains("paid")) {
            emoji = "💸🤯";
        } else if (clean.toLowerCase().contains("hardest") || clean.toLowerCase().contains("broken") || clean.toLowerCase().contains("easter egg")) {
            emoji = "🏆✨";
        }

        String title = clean + " " + emoji + " #Shorts";
        if (title.length() > 95) {
            title = title.substring(0, 90).trim() + "... #Shorts";
        }

        String defaultTemplate = getDefaultDescriptionTemplate();
        String description = clean + "!\n\n" + defaultTemplate;

        String tags = "shorts, viral, gaming, gta 6, rockstar games, trending, gta clips, gta online, gameplay, shorts feed";
        String hashtags = "#Shorts #Viral #GTA6 #Gaming #Trending";

        return new GeneratedMetadata(title, description, tags, hashtags, false);
    }

    private String cleanFilename(String filename) {
        String name = filename;
        int dot = name.lastIndexOf('.');
        if (dot != -1) {
            name = name.substring(0, dot);
        }
        name = name.replace('_', ' ')
                .replaceAll("\\s+", " ")
                .trim();
        return name;
    }

    private String getDefaultDescriptionTemplate() {
        return settingRepository.findBySettingKey("DEFAULT_DESCRIPTION_TEMPLATE")
                .map(AutomationSetting::getSettingValue)
                .orElse("Enjoy this viral clip! 🔥\n\nSubscribe for more amazing videos.\n\n#Shorts #ViralVideos #Trending #Gaming");
    }

    private String getEffectiveApiKey() {
        if (configuredApiKey != null && !configuredApiKey.isBlank()) {
            return configuredApiKey;
        }
        return apiCredentialRepository.findByProvider("GEMINI_API")
                .map(ApiCredential::getApiKey)
                .orElse(null);
    }
}
