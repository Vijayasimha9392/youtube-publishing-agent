package com.youtubeagent.repository;

import com.youtubeagent.model.AutomationSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AutomationSettingRepository extends JpaRepository<AutomationSetting, Long> {
    Optional<AutomationSetting> findBySettingKey(String settingKey);
}
