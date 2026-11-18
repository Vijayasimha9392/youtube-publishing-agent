package com.youtubeagent.repository;

import com.youtubeagent.model.AutomationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutomationLogRepository extends JpaRepository<AutomationLog, Long> {
    List<AutomationLog> findTop100ByOrderByCreatedAtDesc();
    Page<AutomationLog> findByCategoryOrderByCreatedAtDesc(String category, Pageable pageable);
    Page<AutomationLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
