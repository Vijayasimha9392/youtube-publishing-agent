import React, { useState } from 'react';
import { 
  Bell, 
  RefreshCw, 
  Play, 
  CheckCircle, 
  AlertCircle, 
  Youtube, 
  Sparkles,
  ExternalLink
} from 'lucide-react';
import { DashboardStats, NotificationItem } from '../types';

interface HeaderProps {
  currentTab: string;
  stats: DashboardStats | null;
  onScanNow: () => void;
  onPublishBatch: () => void;
  isScanning: boolean;
  isPublishing: boolean;
  notifications: NotificationItem[];
  unreadCount: number;
  onClearNotifications: () => void;
}

export const Header: React.FC<HeaderProps> = ({
  currentTab,
  stats,
  onScanNow,
  onPublishBatch,
  isScanning,
  isPublishing,
  notifications,
  unreadCount,
  onClearNotifications,
}) => {
  const [showNotifications, setShowNotifications] = useState(false);

  const getPageTitle = (tab: string) => {
    switch (tab) {
      case 'overview': return { title: 'Automation Command Center', desc: 'Real-time overview of your YouTube Shorts publishing pipeline' };
      case 'library': return { title: 'Video Library', desc: 'Detected video clips with AI-generated titles, tags, and metadata' };
      case 'queue': return { title: 'Publishing Queue', desc: 'Scheduled publishing queue with hourly batch processing' };
      case 'published': return { title: 'Published Shorts', desc: 'Permanently registered videos uploaded to your YouTube channel' };
      case 'scheduler': return { title: 'Publishing Scheduler', desc: 'Configure hourly intervals, active hours, and timezone policies' };
      case 'logs': return { title: 'Automation Audit Logs', desc: 'Live event stream of folder scans, validation, AI generation, and uploads' };
      case 'settings': return { title: 'System Settings', desc: 'Google OAuth, Gemini API key, monitored folder path, and templates' };
      default: return { title: 'Dashboard', desc: 'Autonomous YouTube Shorts Publishing Engine' };
    }
  };

  const { title, desc } = getPageTitle(currentTab);

  return (
    <header className="h-20 bg-dark-900/80 backdrop-blur-md border-b border-slate-800/80 sticky top-0 z-20 px-8 flex items-center justify-between">
      <div>
        <h2 className="text-xl font-bold tracking-tight text-white flex items-center gap-2">
          {title}
        </h2>
        <p className="text-xs text-slate-400 mt-0.5">{desc}</p>
      </div>

      <div className="flex items-center gap-3.5">
        {/* YouTube Channel Status Pill */}
        {stats && (
          <div className="flex items-center gap-2.5 px-3 py-1.5 rounded-full bg-slate-800/80 border border-slate-700/80 text-xs">
            {stats.channelConnected ? (
              <>
                {stats.channelAvatar ? (
                  <img src={stats.channelAvatar} alt="Channel" className="w-5 h-5 rounded-full object-cover" />
                ) : (
                  <Youtube className="w-4 h-4 text-red-500 fill-red-500" />
                )}
                <div className="flex flex-col">
                  <span className="font-semibold text-slate-200 leading-none truncate max-w-[130px]">
                    {stats.channelTitle}
                  </span>
                  <span className="text-[10px] text-emerald-400 font-mono flex items-center gap-1">
                    <span className="w-1.5 h-1.5 rounded-full bg-emerald-400"></span>
                    Connected
                  </span>
                </div>
              </>
            ) : (
              <div className="flex items-center gap-1.5 text-amber-400">
                <AlertCircle className="w-4 h-4" />
                <span className="font-medium text-[11px]">Channel Not Connected</span>
              </div>
            )}
          </div>
        )}

        {/* Scan Folder Button */}
        <button
          onClick={onScanNow}
          disabled={isScanning}
          className="flex items-center gap-2 px-3.5 py-2 rounded-xl text-xs font-semibold bg-slate-800 hover:bg-slate-700 border border-slate-700 text-slate-200 transition-all disabled:opacity-50"
          title="Scan monitored folder for newly added video clips"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${isScanning ? 'animate-spin text-red-400' : 'text-slate-400'}`} />
          <span>{isScanning ? 'Scanning...' : 'Scan Folder'}</span>
        </button>

        {/* Publish Next Batch Button */}
        <button
          onClick={onPublishBatch}
          disabled={isPublishing || (stats?.totalInQueue ?? 0) === 0}
          className="flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold bg-gradient-to-r from-red-600 to-red-500 hover:from-red-500 hover:to-red-600 text-white shadow-lg shadow-red-600/25 transition-all disabled:opacity-40 disabled:cursor-not-allowed"
          title="Instantly publish next batch of 2 videos from queue"
        >
          <Play className={`w-3.5 h-3.5 fill-current ${isPublishing ? 'animate-pulse' : ''}`} />
          <span>{isPublishing ? 'Publishing...' : 'Publish Next Batch (2)'}</span>
        </button>

        {/* Notification Bell */}
        <div className="relative">
          <button
            onClick={() => setShowNotifications(!showNotifications)}
            className="w-9 h-9 rounded-xl bg-slate-800 hover:bg-slate-700 border border-slate-700 flex items-center justify-center text-slate-300 relative transition-colors"
          >
            <Bell className="w-4 h-4" />
            {unreadCount > 0 && (
              <span className="absolute -top-1 -right-1 w-4 h-4 bg-red-500 text-white text-[10px] font-bold rounded-full flex items-center justify-center shadow-md animate-pulse">
                {unreadCount > 9 ? '9+' : unreadCount}
              </span>
            )}
          </button>

          {/* Notification Dropdown */}
          {showNotifications && (
            <div className="absolute right-0 mt-2 w-80 bg-dark-900 border border-slate-800 rounded-2xl shadow-2xl p-4 z-50">
              <div className="flex items-center justify-between pb-3 border-b border-slate-800">
                <span className="text-xs font-bold text-white uppercase tracking-wider">Notifications</span>
                {unreadCount > 0 && (
                  <button
                    onClick={onClearNotifications}
                    className="text-[11px] text-red-400 hover:text-red-300 font-semibold"
                  >
                    Mark all read
                  </button>
                )}
              </div>
              <div className="mt-3 space-y-2 max-h-72 overflow-y-auto">
                {notifications.length === 0 ? (
                  <p className="text-xs text-slate-500 text-center py-4">No new notifications</p>
                ) : (
                  notifications.map((n) => (
                    <div
                      key={n.id}
                      className={`p-2.5 rounded-xl border text-xs transition-colors ${
                        n.isRead ? 'bg-slate-900/40 border-slate-800/60 text-slate-400' : 'bg-slate-800/60 border-slate-700 text-slate-200'
                      }`}
                    >
                      <div className="font-semibold text-white flex items-center justify-between">
                        <span>{n.title}</span>
                        <span className="text-[10px] text-slate-500 font-mono">
                          {new Date(n.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                        </span>
                      </div>
                      <p className="text-[11px] text-slate-400 mt-1 line-clamp-2">{n.message}</p>
                    </div>
                  ))
                )}
              </div>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};
