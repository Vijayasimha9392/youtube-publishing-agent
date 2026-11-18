import axios from 'axios';
import {
  DashboardStats,
  VideoDetail,
  PublishedVideo,
  PublishingSchedule,
  AutomationLog,
  Settings,
  NotificationItem,
} from '../types';

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

export const getDashboardStats = async (): Promise<DashboardStats> => {
  const res = await api.get<DashboardStats>('/dashboard/stats');
  return res.data;
};

export const getVideos = async (): Promise<VideoDetail[]> => {
  const res = await api.get<VideoDetail[]>('/videos');
  return res.data;
};

export const getVideo = async (id: number): Promise<VideoDetail> => {
  const res = await api.get<VideoDetail>(`/videos/${id}`);
  return res.data;
};

export const updateVideoMetadata = async (
  id: number,
  metadata: {
    title: string;
    description: string;
    tags: string;
    hashtags: string;
    categoryId?: string;
    privacyStatus?: string;
    isMadeForKids?: boolean;
  }
): Promise<VideoDetail> => {
  const res = await api.put<VideoDetail>(`/videos/${id}/metadata`, metadata);
  return res.data;
};

export const getVideoMediaUrl = (id: number): string => {
  return `/api/videos/${id}/stream`;
};

export const getQueue = async (): Promise<VideoDetail[]> => {
  const res = await api.get<VideoDetail[]>('/queue');
  return res.data;
};

export const publishBatchNow = async (batchSize = 2): Promise<{ success: boolean; publishedCount: number; message: string }> => {
  const res = await api.post(`/queue/publish-batch?batchSize=${batchSize}`);
  return res.data;
};

export const reorderQueueItem = async (videoFileId: number, newPosition: number) => {
  const res = await api.post(`/queue/${videoFileId}/reorder?newPosition=${newPosition}`);
  return res.data;
};

export const retryQueueItem = async (videoFileId: number) => {
  const res = await api.post(`/queue/${videoFileId}/retry`);
  return res.data;
};

export const skipQueueItem = async (videoFileId: number) => {
  const res = await api.delete(`/queue/${videoFileId}`);
  return res.data;
};

export const getPublishedVideos = async (): Promise<PublishedVideo[]> => {
  const res = await api.get<PublishedVideo[]>('/published');
  return res.data;
};

export const getPublishingSchedule = async (): Promise<PublishingSchedule> => {
  const res = await api.get<PublishingSchedule>('/schedule');
  return res.data;
};

export const updatePublishingSchedule = async (schedule: Partial<PublishingSchedule>): Promise<PublishingSchedule> => {
  const res = await api.put<PublishingSchedule>('/schedule', schedule);
  return res.data;
};

export const toggleAutomation = async (active: boolean): Promise<PublishingSchedule> => {
  const res = await api.post<PublishingSchedule>(`/schedule/toggle?active=${active}`);
  return res.data;
};

export const getAutomationLogs = async (category?: string, limit = 100): Promise<AutomationLog[]> => {
  const url = category ? `/logs?category=${encodeURIComponent(category)}&limit=${limit}` : `/logs?limit=${limit}`;
  const res = await api.get<AutomationLog[]>(url);
  return res.data;
};

export const getSettings = async (): Promise<Settings> => {
  const res = await api.get<Settings>('/settings');
  return res.data;
};

export const updateSettings = async (settings: Partial<Settings>) => {
  const res = await api.put('/settings', settings);
  return res.data;
};

export const triggerFolderScan = async (): Promise<{ success: boolean; newVideosDetected: number; message: string }> => {
  const res = await api.post('/settings/scan-folder');
  return res.data;
};

export const getYouTubeAuthUrl = async (): Promise<string> => {
  const res = await api.get<{ authUrl: string }>('/youtube/auth-url');
  return res.data.authUrl;
};

export const getYouTubeStatus = async (): Promise<{
  connected: boolean;
  channelTitle?: string;
  channelHandle?: string;
  avatarUrl?: string;
  subscriberCount?: number;
  simulationMode?: boolean;
}> => {
  const res = await api.get('/youtube/status');
  return res.data;
};

export const disconnectYouTube = async () => {
  const res = await api.post('/youtube/disconnect');
  return res.data;
};

export const toggleSimulationMode = async (enabled: boolean) => {
  const res = await api.post(`/youtube/toggle-simulation?enabled=${enabled}`);
  return res.data;
};

export const getNotifications = async (): Promise<{ notifications: NotificationItem[]; unreadCount: number }> => {
  const res = await api.get('/notifications');
  return res.data;
};

export const markNotificationsAsRead = async () => {
  const res = await api.post('/notifications/read-all');
  return res.data;
};

export const uploadVideosApi = async (files: File[]): Promise<{ success: boolean; savedFiles: number; message: string }> => {
  const formData = new FormData();
  files.forEach(f => formData.append('files', f));
  const res = await api.post('/videos/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
  return res.data;
};
