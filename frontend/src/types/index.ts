export type VideoStatus =
  | 'DETECTED'
  | 'VALIDATING'
  | 'READY'
  | 'QUEUED'
  | 'UPLOADING'
  | 'PROCESSING'
  | 'SCHEDULED'
  | 'PUBLISHED'
  | 'FAILED'
  | 'RETRY_PENDING'
  | 'SKIPPED';

export interface DashboardStats {
  totalDetected: number;
  totalInQueue: number;
  totalPublished: number;
  totalPending: number;
  totalFailed: number;
  videosRemaining: number;
  todayPublishCount: number;
  nextPublishTime: string;
  channelConnected: boolean;
  channelTitle: string;
  channelHandle: string;
  channelAvatar: string;
  subscriberCount: number;
  monitoringActive: boolean;
  monitoredFolder: string;
  automationEnabled: boolean;
  videosPerInterval: number;
  intervalMinutes: number;
  timezone: string;
}

export interface VideoDetail {
  id: number;
  filename: string;
  filePath: string;
  fileHash: string;
  fileSize: number;
  duration: number;
  resolution: string;
  format: string;
  detectedAt: string;
  status: VideoStatus;

  // Metadata
  title?: string;
  description?: string;
  tags?: string;
  hashtags?: string;
  categoryId?: string;
  privacyStatus?: string;
  isMadeForKids?: boolean;
  aiGenerated?: boolean;

  // Queue info
  queuePosition?: number;
  scheduledTime?: string;
  priority?: number;

  // Published info
  youtubeVideoId?: string;
  youtubeUrl?: string;
  publishedAt?: string;
}

export interface PublishedVideo {
  id: number;
  videoFileId: number;
  fileHash: string;
  youtubeVideoId: string;
  youtubeUrl: string;
  title: string;
  publishedAt: string;
  publishingStatus: string;
}

export interface PublishingSchedule {
  id: number;
  videosPerInterval: number;
  intervalMinutes: number;
  startTime: string;
  endTime: string;
  timezone: string;
  maxDailyUploads: number;
  isActive: boolean;
  updatedAt: string;
}

export interface AutomationLog {
  id: number;
  level: 'INFO' | 'WARN' | 'ERROR';
  category: string;
  message: string;
  videoFileId?: number;
  fileHash?: string;
  createdAt: string;
}

export interface Settings {
  monitoredFolder: string;
  geminiApiKey: string;
  youtubeClientId: string;
  youtubeClientSecret: string;
  youtubeRedirectUri: string;
  defaultDescriptionTemplate: string;
  defaultTags: string;
  simulateUpload: boolean;
}

export interface NotificationItem {
  id: number;
  title: string;
  message: string;
  type: 'INFO' | 'SUCCESS' | 'WARNING' | 'ERROR';
  isRead: boolean;
  createdAt: string;
}
