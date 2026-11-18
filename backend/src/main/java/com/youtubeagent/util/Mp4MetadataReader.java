package com.youtubeagent.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class Mp4MetadataReader {

    public static class VideoMetadataResult {
        private final double durationSeconds;
        private final int width;
        private final int height;
        private final String resolution;

        public VideoMetadataResult(double durationSeconds, int width, int height) {
            this.durationSeconds = durationSeconds;
            this.width = width;
            this.height = height;
            this.resolution = (width > 0 && height > 0) ? (width + "x" + height) : "1080x1920";
        }

        public double getDurationSeconds() { return durationSeconds; }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
        public String getResolution() { return resolution; }
    }

    public static VideoMetadataResult extractMetadata(File file) {
        if (!file.exists() || file.length() < 16) {
            return new VideoMetadataResult(0.0, 1080, 1920);
        }

        double duration = 0.0;
        int width = 0;
        int height = 0;

        try (FileInputStream fis = new FileInputStream(file);
             FileChannel channel = fis.getChannel()) {

            ByteBuffer header = ByteBuffer.allocate(8);
            long position = 0;
            long fileSize = channel.size();

            while (position + 8 <= fileSize) {
                channel.position(position);
                header.clear();
                if (channel.read(header) < 8) break;
                header.flip();

                long boxSize = Integer.toUnsignedLong(header.getInt());
                byte[] typeBytes = new byte[4];
                header.get(typeBytes);
                String boxType = new String(typeBytes);

                if (boxSize == 1) { // 64-bit size
                    ByteBuffer largeSizeBuf = ByteBuffer.allocate(8);
                    if (channel.read(largeSizeBuf) < 8) break;
                    largeSizeBuf.flip();
                    boxSize = largeSizeBuf.getLong();
                } else if (boxSize == 0) {
                    boxSize = fileSize - position;
                }

                if (boxSize < 8) break;

                if ("moov".equals(boxType)) {
                    // Parse inside moov container
                    long moovEnd = position + boxSize;
                    long innerPos = position + 8;

                    while (innerPos + 8 <= moovEnd) {
                        channel.position(innerPos);
                        header.clear();
                        if (channel.read(header) < 8) break;
                        header.flip();

                        long innerBoxSize = Integer.toUnsignedLong(header.getInt());
                        byte[] innerTypeBytes = new byte[4];
                        header.get(innerTypeBytes);
                        String innerBoxType = new String(innerTypeBytes);

                        if (innerBoxSize == 1) {
                            ByteBuffer largeSizeBuf = ByteBuffer.allocate(8);
                            if (channel.read(largeSizeBuf) < 8) break;
                            largeSizeBuf.flip();
                            innerBoxSize = largeSizeBuf.getLong();
                        } else if (innerBoxSize == 0) {
                            innerBoxSize = moovEnd - innerPos;
                        }

                        if (innerBoxSize < 8) break;

                        if ("mvhd".equals(innerBoxType)) {
                            // Movie header box
                            int mvhdPayloadSize = (int) Math.min(innerBoxSize - 8, 128);
                            ByteBuffer mvhdBuf = ByteBuffer.allocate(mvhdPayloadSize);
                            channel.read(mvhdBuf);
                            mvhdBuf.flip();

                            if (mvhdBuf.remaining() >= 20) {
                                int version = mvhdBuf.get() & 0xFF;
                                mvhdBuf.position(mvhdBuf.position() + 3); // flags

                                long timescale;
                                long rawDuration;
                                if (version == 1) {
                                    mvhdBuf.position(mvhdBuf.position() + 16); // skip creation & mod times (8+8)
                                    timescale = Integer.toUnsignedLong(mvhdBuf.getInt());
                                    rawDuration = mvhdBuf.getLong();
                                } else {
                                    mvhdBuf.position(mvhdBuf.position() + 8); // skip creation & mod times (4+4)
                                    timescale = Integer.toUnsignedLong(mvhdBuf.getInt());
                                    rawDuration = Integer.toUnsignedLong(mvhdBuf.getInt());
                                }

                                if (timescale > 0) {
                                    duration = Math.round((rawDuration / (double) timescale) * 100.0) / 100.0;
                                }
                            }
                        } else if ("trak".equals(innerBoxType)) {
                            // Inspect track header
                            long trakEnd = innerPos + innerBoxSize;
                            long trakInnerPos = innerPos + 8;

                            while (trakInnerPos + 8 <= trakEnd) {
                                channel.position(trakInnerPos);
                                header.clear();
                                if (channel.read(header) < 8) break;
                                header.flip();

                                long trakBoxSize = Integer.toUnsignedLong(header.getInt());
                                byte[] tBytes = new byte[4];
                                header.get(tBytes);
                                String tType = new String(tBytes);

                                if ("tkhd".equals(tType)) {
                                    int tkhdPayloadSize = (int) Math.min(trakBoxSize - 8, 120);
                                    ByteBuffer tkhdBuf = ByteBuffer.allocate(tkhdPayloadSize);
                                    channel.read(tkhdBuf);
                                    tkhdBuf.flip();

                                    if (tkhdBuf.remaining() >= 80) {
                                        int version = tkhdBuf.get() & 0xFF;
                                        // Position of width and height in tkhd:
                                        // version 0: 76 bytes before width
                                        // version 1: 88 bytes before width
                                        int skipBytes = (version == 1) ? 88 : 76;
                                        if (tkhdBuf.remaining() >= skipBytes + 8) {
                                            tkhdBuf.position(tkhdBuf.position() + skipBytes);
                                            int w = (tkhdBuf.getInt() >> 16) & 0xFFFF;
                                            int h = (tkhdBuf.getInt() >> 16) & 0xFFFF;
                                            if (w > 0 && h > 0 && width == 0) {
                                                width = w;
                                                height = h;
                                            }
                                        }
                                    }
                                }
                                trakInnerPos += trakBoxSize;
                            }
                        }

                        innerPos += innerBoxSize;
                    }
                    break; // parsed moov box
                }

                position += boxSize;
            }

        } catch (Exception e) {
            // Graceful fallback to typical shorts format
        }

        if (width == 0 || height == 0) {
            width = 1080;
            height = 1920;
        }
        if (duration <= 0.0) {
            // Rough estimation from file size if duration wasn't in moov
            duration = Math.max(15.0, Math.min(60.0, (file.length() / (1024.0 * 1024.0)) * 4.0));
            duration = Math.round(duration * 10.0) / 10.0;
        }

        return new VideoMetadataResult(duration, width, height);
    }
}
