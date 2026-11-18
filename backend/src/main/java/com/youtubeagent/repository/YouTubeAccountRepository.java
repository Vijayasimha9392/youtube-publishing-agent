package com.youtubeagent.repository;

import com.youtubeagent.model.YouTubeAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface YouTubeAccountRepository extends JpaRepository<YouTubeAccount, Long> {
    Optional<YouTubeAccount> findByChannelId(String channelId);
    Optional<YouTubeAccount> findTopByIsActiveTrueOrderByIdDesc();
}
