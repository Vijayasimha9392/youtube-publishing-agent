# Autonomous YouTube Shorts Publishing Agent (Google Antigravity 2.0)

A production-ready, fully autonomous AI-powered YouTube Shorts publishing automation platform built with **Java Spring Boot 3.4.2**, **Java 25**, **MySQL / H2 Database**, **React.js**, **TypeScript**, and **Tailwind CSS**.

---

## 🎯 System Objective

The agent eliminates all manual publishing tasks. You simply drop your finished video clips (`.mp4`, `.mov`, `.mkv`, `.webm`) into your designated video folder (`C:\Users\vijay\OneDrive\Desktop\Youtube Agent\Clips`). The AI agent autonomously manages everything that follows:

1. **Continuous Folder Monitoring**: Scans the clips folder automatically and detects newly added files.
2. **File Validation & Stability Guard**: Verifies video format, file integrity, resolution (e.g. 1080x1920), duration, and ignores incomplete files while they are still being copied.
3. **Permanent Duplicate Prevention**: Computes SHA-256 cryptographic hashes and checks against database records (`published_videos`). Published content is **permanently barred from being republished**, even across restarts, renames, or file relocations.
4. **AI Viral Metadata Generation**: Uses Google Gemini API (with heuristic fallback) to generate engaging SEO titles with emojis, descriptions with CTAs, tags, and viral hashtags (`#Shorts #Viral #Gaming`).
5. **Publishing Queue Engine**: Dispatches **exactly 2 videos every hour** according to your configured schedule and timezone (`Asia/Kolkata` IST).
6. **Official YouTube API Integration**: Uploads via official YouTube Data API v3 resumable upload protocol using OAuth 2.0.
7. **Premium Web Dashboard**: Manage and inspect the entire pipeline at `http://localhost:5173`.

---

## 🏗️ Architecture & Tech Stack

### Backend
- **Framework**: Spring Boot 3.4.2
- **Language**: Java 25 (LTS)
- **Database Support**: Dual-mode Spring Data JPA:
  - **Embedded H2 (MySQL Mode)**: Runs out-of-the-box with zero database installation required (`data/youtube_agent.mv.db`).
  - **MySQL Server 8.0**: Activate with `spring.profiles.active=mysql` for production MySQL deployments.
- **Video Inspection**: Pure Java ISO-BMFF (ISO/IEC 14496-12) box parser for instant, native extraction of duration, width, and height without external binary dependencies.
- **Video Streaming**: HTTP 206 Partial Content range streaming endpoint for instant browser video previews.

### Frontend
- **Framework**: React 18 + Vite
- **Language**: TypeScript
- **Styling**: Tailwind CSS (Dark Glassmorphism UI)
- **Icons**: Lucide React
- **HTTP Client**: Axios with reactive 5-second polling

---

## 📊 Database Schema (Normalized)

1. `video_files`: File paths, SHA-256 hashes, sizes, durations, resolutions, formats, statuses.
2. `video_metadata`: AI/custom titles, descriptions, tags, hashtags, categories, visibility.
3. `publishing_queue`: Priority queue, queue positions, scheduled slot timestamps.
4. `publishing_jobs`: Execution tracking, attempt counts, retry policies, error messages.
5. `published_videos`: **Permanent duplicate prevention registry** storing unique `file_hash`, `youtube_video_id`, and `youtube_url`.
6. `publishing_schedules`: Videos per interval (default 2), interval minutes (default 60), active hours (09:00 - 23:59), timezone (`Asia/Kolkata`), daily limits.
7. `automation_settings`: Dynamic key-value store for monitored folder, description templates, default tags.
8. `automation_logs`: Full audit trail (`SCAN`, `DETECT`, `VALIDATION`, `DUPLICATE`, `AI_METADATA`, `QUEUE`, `UPLOAD`, `PUBLISH`, `RETRY`, `FAILED`, `RESTART`).
9. `youtube_accounts`: Channel tokens, handle, title, subscriber counts.
10. `api_credentials`: Encrypted client IDs, secrets, and Gemini keys.
11. `notifications`: Real-time alerts for published videos, detection events, and failures.

---

## ⚡ Quick Start Guide

### 1. One-Click Launch
Double-click `start.bat` in the root directory:
```bat
start.bat
```
This automatically starts:
- **Backend API**: `http://localhost:8080`
- **Dashboard UI**: `http://localhost:5173`

### 2. Manual Startup

**Start Backend**:
```powershell
cd backend
$env:JAVA_HOME="C:\Program Files\Java\jdk-25.0.2"
& "C:\Program Files\Apache\apache-maven-3.10.0\bin\mvn.cmd" package -DskipTests
java -jar target\youtube-publishing-agent-1.0.0.jar
```

**Start Frontend**:
```powershell
cd frontend
npm run dev -- --host 127.0.0.1 --port 5173
```

---

## 🔑 YouTube API & Gemini Setup

### Connecting Your YouTube Channel
1. Go to the [Google Cloud Console](https://console.cloud.google.com/).
2. Create or select a project and enable the **YouTube Data API v3**.
3. In **Credentials**, create an **OAuth 2.0 Client ID** (Web application).
4. Add the Authorized Redirect URI:
   ```
   http://localhost:8080/api/youtube/oauth2/callback
   ```
5. Open the Dashboard Settings (`http://localhost:5173`) -> **Settings** tab.
6. Paste your **Client ID** and **Client Secret**.
7. Click **Connect YouTube Channel** to authenticate your channel.

### Gemini API Key (Optional but Recommended)
In the **Settings** tab, enter your Gemini API key to enable generative viral hook titles and context-aware descriptions. If omitted, the agent uses its smart algorithmic heuristic generator.

---

## 🚀 Publishing Engine Rules

| Setting | Default Value | Description |
|---|---|---|
| **Videos Per Slot** | `2` | Number of videos uploaded per batch |
| **Interval Frequency** | `60 minutes` | Hourly schedule |
| **Active Time Window**| `09:00 - 23:59`| Active hours (prevents late-night publishing) |
| **Timezone** | `Asia/Kolkata` | Standard Indian Standard Time (IST) |
| **Daily Upload Limit**| `20 videos` | Daily quota guardrail |
| **Duplicate Check** | `Permanent SHA-256` | Never republishes existing content |
| **Interrupted Jobs** | `Auto Recovery` | Incomplete uploads resume on restart without duplicates |

---

## 📁 Monitored Folder Details

- Default path: `C:\Users\vijay\OneDrive\Desktop\Youtube Agent\Clips`
- Initially loaded with 19 viral clips.
- When new clips are dropped into this folder, the agent automatically validates them, creates the metadata, and adds them to the end of the queue.
