package com.youtubeagent.repository;

import com.youtubeagent.model.PublishingSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PublishingScheduleRepository extends JpaRepository<PublishingSchedule, Long> {
    Optional<PublishingSchedule> findTopByOrderByIdAsc();
}
