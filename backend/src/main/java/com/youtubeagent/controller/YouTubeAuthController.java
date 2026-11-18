package com.youtubeagent.controller;

import com.youtubeagent.model.YouTubeAccount;
import com.youtubeagent.repository.YouTubeAccountRepository;
import com.youtubeagent.service.YouTubeApiService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/youtube")
public class YouTubeAuthController {

    private final YouTubeApiService youTubeApiService;
    private final YouTubeAccountRepository accountRepository;

    @Value("${agent.youtube.frontend-redirect:http://localhost:5173/settings}")
    private String frontendRedirect;

    public YouTubeAuthController(YouTubeApiService youTubeApiService,
                                 YouTubeAccountRepository accountRepository) {
        this.youTubeApiService = youTubeApiService;
        this.accountRepository = accountRepository;
    }

    @GetMapping("/auth-url")
    public ResponseEntity<Map<String, String>> getAuthUrl() {
        try {
            String url = youTubeApiService.buildAuthorizationUrl();
            return ResponseEntity.ok(Map.of("authUrl", url));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/oauth2/callback")
    public void oauth2Callback(@RequestParam(required = false) String code,
                               @RequestParam(required = false) String error,
                               HttpServletRequest request,
                               HttpServletResponse response) throws IOException {
        String target = resolveRedirectTarget(request);

        if (error != null) {
            response.sendRedirect(target + "?tab=settings&oauth_error=" + error);
            return;
        }

        if (code != null) {
            try {
                youTubeApiService.exchangeAuthCode(code);
                response.sendRedirect(target + "?tab=settings&connected=true");
                return;
            } catch (Exception e) {
                response.sendRedirect(target + "?tab=settings&oauth_error=" + e.getMessage());
                return;
            }
        }

        response.sendRedirect(target + "?tab=settings");
    }

    private String resolveRedirectTarget(HttpServletRequest request) {
        String host = request.getHeader("Host");
        String proto = request.getHeader("X-Forwarded-Proto");
        if (proto == null || proto.isBlank()) {
            proto = request.isSecure() ? "https" : "http";
        }
        if (host != null && !host.isBlank() && !host.contains("localhost:8080")) {
            return proto + "://" + host + "/";
        }
        String envRedirect = System.getenv("FRONTEND_REDIRECT_URL");
        if (envRedirect != null && !envRedirect.isBlank()) {
            return envRedirect;
        }
        return frontendRedirect;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Optional<YouTubeAccount> active = accountRepository.findTopByIsActiveTrueOrderByIdDesc();
        if (active.isPresent()) {
            YouTubeAccount acc = active.get();
            return ResponseEntity.ok(Map.of(
                    "connected", true,
                    "channelId", acc.getChannelId(),
                    "channelTitle", acc.getChannelTitle(),
                    "channelHandle", acc.getChannelHandle() != null ? acc.getChannelHandle() : "",
                    "avatarUrl", acc.getAvatarUrl() != null ? acc.getAvatarUrl() : "",
                    "subscriberCount", acc.getSubscriberCount() != null ? acc.getSubscriberCount() : 0,
                    "simulationMode", "true".equalsIgnoreCase(System.getProperty("SIMULATE_YOUTUBE_UPLOAD", "false"))
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                    "connected", false,
                    "channelTitle", "Not connected",
                    "simulationMode", "true".equalsIgnoreCase(System.getProperty("SIMULATE_YOUTUBE_UPLOAD", "false"))
            ));
        }
    }

    @PostMapping("/disconnect")
    public ResponseEntity<Map<String, Object>> disconnect() {
        accountRepository.findAll().forEach(a -> {
            a.setIsActive(false);
            accountRepository.save(a);
        });
        return ResponseEntity.ok(Map.of("success", true, "message", "YouTube channel disconnected."));
    }

    @PostMapping("/toggle-simulation")
    public ResponseEntity<Map<String, Object>> toggleSimulation(@RequestParam boolean enabled) {
        System.setProperty("SIMULATE_YOUTUBE_UPLOAD", String.valueOf(enabled));
        return ResponseEntity.ok(Map.of("simulationMode", enabled, "message", "Simulation mode updated."));
    }
}
