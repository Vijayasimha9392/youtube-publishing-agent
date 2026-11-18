package com.youtubeagent.service;

import com.youtubeagent.util.FileHashUtil;
import com.youtubeagent.util.Mp4MetadataReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
public class VideoValidationService {

    private static final Set<String> SUPPORTED_EXTENSIONS = new HashSet<>(
            Arrays.asList("mp4", "mov", "mkv", "webm")
    );

    @Value("${agent.folder.stability-wait-seconds:3}")
    private int stabilityWaitSeconds;

    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        private final String fileHash;
        private final long fileSize;
        private final double duration;
        private final String resolution;
        private final String format;

        public ValidationResult(boolean valid, String errorMessage, String fileHash, long fileSize,
                                double duration, String resolution, String format) {
            this.valid = valid;
            this.errorMessage = errorMessage;
            this.fileHash = fileHash;
            this.fileSize = fileSize;
            this.duration = duration;
            this.resolution = resolution;
            this.format = format;
        }

        public static ValidationResult invalid(String message) {
            return new ValidationResult(false, message, null, 0, 0, null, null);
        }

        public boolean isValid() { return valid; }
        public String getErrorMessage() { return errorMessage; }
        public String getFileHash() { return fileHash; }
        public long getFileSize() { return fileSize; }
        public double getDuration() { return duration; }
        public String getResolution() { return resolution; }
        public String getFormat() { return format; }
    }

    public ValidationResult validate(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return ValidationResult.invalid("File does not exist or is not a standard file");
        }

        String filename = file.getName();
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex == -1) {
            return ValidationResult.invalid("File has no extension");
        }

        String ext = filename.substring(dotIndex + 1).toLowerCase();
        if (!SUPPORTED_EXTENSIONS.contains(ext)) {
            return ValidationResult.invalid("Unsupported video format: " + ext);
        }

        // Verify file stability (avoid copying in progress)
        long initialSize = file.length();
        if (initialSize <= 0) {
            return ValidationResult.invalid("File is empty (0 bytes)");
        }

        try {
            Thread.sleep(Math.min(1000L, stabilityWaitSeconds * 500L));
        } catch (InterruptedException ignored) {}

        long secondSize = file.length();
        if (initialSize != secondSize) {
            return ValidationResult.invalid("File size is still changing (file copy in progress)");
        }

        // Verify readable
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] testBuf = new byte[16];
            int read = fis.read(testBuf);
            if (read < 8) {
                return ValidationResult.invalid("File cannot be read or is corrupted");
            }
        } catch (Exception e) {
            return ValidationResult.invalid("Cannot open file for reading: " + e.getMessage());
        }

        // Compute SHA-256
        String fileHash;
        try {
            fileHash = FileHashUtil.calculateSha256(file);
        } catch (Exception e) {
            return ValidationResult.invalid("Failed to compute SHA-256: " + e.getMessage());
        }

        // Extract metadata
        Mp4MetadataReader.VideoMetadataResult meta = Mp4MetadataReader.extractMetadata(file);

        return new ValidationResult(
                true,
                null,
                fileHash,
                secondSize,
                meta.getDurationSeconds(),
                meta.getResolution(),
                ext
        );
    }
}
