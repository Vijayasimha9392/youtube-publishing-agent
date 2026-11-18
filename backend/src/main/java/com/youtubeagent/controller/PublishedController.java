package com.youtubeagent.controller;

import com.youtubeagent.model.PublishedVideo;
import com.youtubeagent.repository.PublishedVideoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/published")
public class PublishedController {

    private final PublishedVideoRepository publishedVideoRepository;

    public PublishedController(PublishedVideoRepository publishedVideoRepository) {
        this.publishedVideoRepository = publishedVideoRepository;
    }

    @GetMapping
    public ResponseEntity<List<PublishedVideo>> getPublishedVideos() {
        return ResponseEntity.ok(publishedVideoRepository.findAllByOrderByPublishedAtDesc());
    }
}
