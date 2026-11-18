import React, { useState, useEffect } from 'react';
import { Sidebar } from './components/Sidebar';
import { Header } from './components/Header';
import { Overview } from './pages/Overview';
import { Library } from './pages/Library';
import { Queue } from './pages/Queue';
import { Published } from './pages/Published';
import { Scheduler } from './pages/Scheduler';
import { Logs } from './pages/Logs';
import { SettingsPage } from './pages/Settings';
import { VideoPlayerModal } from './components/VideoPlayerModal';
import { EditMetadataModal } from './components/EditMetadataModal';
import { 
  getDashboardStats, 
  getVideos, 
  getQueue, 
  getPublishedVideos, 
  getPublishingSchedule, 
  getAutomationLogs, 
  getSettings,
  updatePublishingSchedule,
  toggleAutomation,
  triggerFolderScan,
  publishBatchNow,
  reorderQueueItem,
  retryQueueItem,
  skipQueueItem,
  updateSettings,
  getNotifications,
  markNotificationsAsRead
} from './services/api';
import { 
  DashboardStats, 
  VideoDetail, 
  PublishedVideo, 
  PublishingSchedule, 
  AutomationLog, 
  Settings,
  NotificationItem
} from './types';

export function App() {
  const [currentTab, setCurrentTab] = useState<string>('overview');
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [videos, setVideos] = useState<VideoDetail[]>([]);
  const [queueVideos, setQueueVideos] = useState<VideoDetail[]>([]);
  const [publishedVideos, setPublishedVideos] = useState<PublishedVideo[]>([]);
  const [schedule, setSchedule] = useState<PublishingSchedule | null>(null);
  const [logs, setLogs] = useState<AutomationLog[]>([]);
  const [settings, setSettings] = useState<Settings | null>(null);
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);

  const [previewVideo, setPreviewVideo] = useState<VideoDetail | null>(null);
  const [editingVideo, setEditingVideo] = useState<VideoDetail | null>(null);
  const [isScanning, setIsScanning] = useState(false);
  const [isPublishing, setIsPublishing] = useState(false);
  const [isRefreshingLogs, setIsRefreshingLogs] = useState(false);

  // Load all initial data
  const loadData = async () => {
    try {
      const [sData, vData, qData, pData, schData, lData, settsData, notifData] = await Promise.all([
        getDashboardStats().catch(() => null),
        getVideos().catch(() => []),
        getQueue().catch(() => []),
        getPublishedVideos().catch(() => []),
        getPublishingSchedule().catch(() => null),
        getAutomationLogs(undefined, 100).catch(() => []),
        getSettings().catch(() => null),
        getNotifications().catch(() => ({ notifications: [], unreadCount: 0 })),
      ]);

      if (sData) setStats(sData);
      setVideos(vData);
      setQueueVideos(qData);
      setPublishedVideos(pData);
      if (schData) setSchedule(schData);
      setLogs(lData);
      if (settsData) setSettings(settsData);
      setNotifications(notifData.notifications);
      setUnreadCount(notifData.unreadCount);
    } catch (e) {
      console.error('Error fetching dashboard data:', e);
    }
  };

  useEffect(() => {
    loadData();
    // Live polling every 5 seconds for status & stats
    const interval = setInterval(loadData, 5000);
    return () => clearInterval(interval);
  }, []);

  const handleScanNow = async () => {
    setIsScanning(true);
    try {
      await triggerFolderScan();
      await loadData();
    } catch (err) {
      console.error('Folder scan failed', err);
    } finally {
      setIsScanning(false);
    }
  };

  const handlePublishBatch = async () => {
    setIsPublishing(true);
    try {
      const res = await publishBatchNow(2);
      await loadData();
      alert(res.message);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Publishing batch failed.');
    } finally {
      setIsPublishing(false);
    }
  };

  const handleReorder = async (videoFileId: number, newPos: number) => {
    await reorderQueueItem(videoFileId, newPos);
    await loadData();
  };

  const handleRetry = async (videoFileId: number) => {
    await retryQueueItem(videoFileId);
    await loadData();
  };

  const handleSkip = async (videoFileId: number) => {
    await skipQueueItem(videoFileId);
    await loadData();
  };

  const handleUpdateSchedule = async (newSchedule: Partial<PublishingSchedule>) => {
    const updated = await updatePublishingSchedule(newSchedule);
    setSchedule(updated);
    await loadData();
  };

  const handleToggleAutomation = async (active: boolean) => {
    const updated = await toggleAutomation(active);
    setSchedule(updated);
    await loadData();
  };

  const handleUpdateSettings = async (newSettings: Partial<Settings>) => {
    await updateSettings(newSettings);
    await loadData();
  };

  const handleRefreshLogs = async () => {
    setIsRefreshingLogs(true);
    try {
      const l = await getAutomationLogs(undefined, 100);
      setLogs(l);
    } finally {
      setIsRefreshingLogs(false);
    }
  };

  const handleClearNotifications = async () => {
    await markNotificationsAsRead();
    setUnreadCount(0);
    setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
  };

  return (
    <div className="flex min-h-screen bg-dark-950 text-slate-100">
      {/* Sidebar Navigation */}
      <Sidebar
        currentTab={currentTab}
        setCurrentTab={setCurrentTab}
        queueCount={stats?.totalInQueue ?? queueVideos.length}
        publishedCount={stats?.totalPublished ?? publishedVideos.length}
        isAutomationActive={stats?.automationEnabled ?? schedule?.isActive ?? true}
      />

      {/* Main Content Area */}
      <div className="flex-1 ml-64 flex flex-col min-h-screen">
        <Header
          currentTab={currentTab}
          stats={stats}
          onScanNow={handleScanNow}
          onPublishBatch={handlePublishBatch}
          isScanning={isScanning}
          isPublishing={isPublishing}
          notifications={notifications}
          unreadCount={unreadCount}
          onClearNotifications={handleClearNotifications}
        />

        <main className="flex-1 p-8 overflow-y-auto">
          {currentTab === 'overview' && (
            <Overview
              stats={stats}
              logs={logs}
              queueVideos={queueVideos}
              onNavigate={setCurrentTab}
              onPublishBatch={handlePublishBatch}
              isPublishing={isPublishing}
            />
          )}

          {currentTab === 'library' && (
            <Library
              videos={videos}
              onPreview={(v) => setPreviewVideo(v)}
              onEditMetadata={(v) => setEditingVideo(v)}
              onRefresh={loadData}
            />
          )}

          {currentTab === 'queue' && (
            <Queue
              queueVideos={queueVideos}
              onReorder={handleReorder}
              onRetry={handleRetry}
              onSkip={handleSkip}
              onPublishBatch={handlePublishBatch}
              isPublishing={isPublishing}
              intervalMinutes={stats?.intervalMinutes ?? 60}
              videosPerInterval={stats?.videosPerInterval ?? 2}
            />
          )}

          {currentTab === 'published' && (
            <Published publishedVideos={publishedVideos} />
          )}

          {currentTab === 'scheduler' && (
            <Scheduler
              schedule={schedule}
              onUpdateSchedule={handleUpdateSchedule}
              onToggleAutomation={handleToggleAutomation}
            />
          )}

          {currentTab === 'logs' && (
            <Logs
              logs={logs}
              onRefresh={handleRefreshLogs}
              isRefreshing={isRefreshingLogs}
            />
          )}

          {currentTab === 'settings' && (
            <SettingsPage
              settings={settings}
              onUpdateSettings={handleUpdateSettings}
              onScanNow={handleScanNow}
              isScanning={isScanning}
              channelConnected={stats?.channelConnected ?? false}
              channelTitle={stats?.channelTitle}
            />
          )}
        </main>
      </div>

      {/* Video Preview Player Modal */}
      <VideoPlayerModal
        video={previewVideo}
        onClose={() => setPreviewVideo(null)}
      />

      {/* Edit Metadata SEO Modal */}
      <EditMetadataModal
        video={editingVideo}
        onClose={() => setEditingVideo(null)}
        onSaved={loadData}
      />
    </div>
  );
}

export default App;
