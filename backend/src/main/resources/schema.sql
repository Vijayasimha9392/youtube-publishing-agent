-- ========================================================
-- AUTONOMOUS YOUTUBE SHORTS PUBLISHING AGENT DATABASE SCHEMA
-- Normalized MySQL Schema Definition
-- ========================================================

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(50) DEFAULT 'ADMIN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. YouTube Accounts Table
CREATE TABLE IF NOT EXISTS youtube_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    channel_id VARCHAR(100) NOT NULL UNIQUE,
    channel_title VARCHAR(255) NOT NULL,
    channel_handle VARCHAR(100),
    avatar_url TEXT,
    subscriber_count BIGINT DEFAULT 0,
    access_token TEXT,
    refresh_token TEXT,
    token_type VARCHAR(50) DEFAULT 'Bearer',
    expires_at TIMESTAMP NULL,
    connected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- 3. Video Files Table
CREATE TABLE IF NOT EXISTS video_files (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(1000) NOT NULL,
    file_hash VARCHAR(64) NOT NULL UNIQUE,
    file_size BIGINT NOT NULL,
    duration DOUBLE PRECISION DEFAULT 0.0,
    resolution VARCHAR(32) DEFAULT '1080x1920',
    format VARCHAR(16) DEFAULT 'mp4',
    detected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(32) NOT NULL DEFAULT 'DETECTED',
    INDEX idx_video_files_hash (file_hash),
    INDEX idx_video_files_status (status)
);

-- 4. Video Metadata Table
CREATE TABLE IF NOT EXISTS video_metadata (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    video_file_id BIGINT NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    tags TEXT,
    hashtags TEXT,
    category_id VARCHAR(32) DEFAULT '20',
    privacy_status VARCHAR(32) DEFAULT 'public',
    is_made_for_kids BOOLEAN DEFAULT FALSE,
    ai_generated BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_video_metadata_file FOREIGN KEY (video_file_id) REFERENCES video_files(id) ON DELETE CASCADE
);

-- 5. Publishing Queue Table
CREATE TABLE IF NOT EXISTS publishing_queue (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    video_file_id BIGINT NOT NULL UNIQUE,
    queue_position INT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'QUEUED',
    scheduled_time TIMESTAMP NULL,
    priority INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_publishing_queue_file FOREIGN KEY (video_file_id) REFERENCES video_files(id) ON DELETE CASCADE,
    INDEX idx_queue_pos (queue_position),
    INDEX idx_queue_status (status)
);

-- 6. Publishing Jobs Table
CREATE TABLE IF NOT EXISTS publishing_jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    queue_id BIGINT NULL,
    video_file_id BIGINT NOT NULL,
    file_hash VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    attempt_count INT DEFAULT 0,
    max_retries INT DEFAULT 3,
    error_message TEXT,
    started_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_jobs_file_hash (file_hash),
    INDEX idx_jobs_status (status)
);

-- 7. Published Videos Table (PERMANENT DUPLICATE PREVENTION RECORD)
CREATE TABLE IF NOT EXISTS published_videos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    video_file_id BIGINT NOT NULL UNIQUE,
    file_hash VARCHAR(64) NOT NULL UNIQUE,
    youtube_video_id VARCHAR(64) NOT NULL UNIQUE,
    youtube_url VARCHAR(255) NOT NULL,
    title VARCHAR(255),
    published_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    publishing_status VARCHAR(32) NOT NULL DEFAULT 'PUBLISHED',
    INDEX idx_published_hash (file_hash),
    INDEX idx_published_yt_id (youtube_video_id)
);

-- 8. Publishing Schedules Table
CREATE TABLE IF NOT EXISTS publishing_schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    videos_per_interval INT DEFAULT 2,
    interval_minutes INT DEFAULT 60,
    start_time VARCHAR(8) DEFAULT '09:00',
    end_time VARCHAR(8) DEFAULT '23:59',
    timezone VARCHAR(64) DEFAULT 'Asia/Kolkata',
    max_daily_uploads INT DEFAULT 20,
    is_active BOOLEAN DEFAULT TRUE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 9. Automation Settings Table
CREATE TABLE IF NOT EXISTS automation_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(64) NOT NULL UNIQUE,
    setting_value TEXT,
    description VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 10. Automation Logs Table
CREATE TABLE IF NOT EXISTS automation_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    level VARCHAR(16) NOT NULL DEFAULT 'INFO',
    category VARCHAR(32) NOT NULL,
    message TEXT NOT NULL,
    video_file_id BIGINT NULL,
    file_hash VARCHAR(64) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_logs_category (category),
    INDEX idx_logs_created_at (created_at)
);

-- 11. API Credentials Table
CREATE TABLE IF NOT EXISTS api_credentials (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    provider VARCHAR(64) NOT NULL UNIQUE,
    client_id TEXT,
    client_secret TEXT,
    api_key TEXT,
    redirect_uri TEXT,
    is_configured BOOLEAN DEFAULT FALSE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 12. Notifications Table
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(32) DEFAULT 'INFO',
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
