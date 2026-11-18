import React from 'react';
import { 
  Film, 
  ListOrdered, 
  CheckCircle2, 
  Clock, 
  AlertTriangle, 
  Layers, 
  Calendar,
  Radio,
  ArrowUpRight,
  TrendingUp,
  Cpu,
  Youtube,
  FolderCheck
} from 'lucide-react';
import { DashboardStats, AutomationLog, VideoDetail } from '../types';
import { StatCard } from '../components/StatCard';

interface OverviewProps {
  stats: DashboardStats | null;
  logs: AutomationLog[];
  queueVideos: VideoDetail[];
  onNavigate: (tab: string) => void;
  onPublishBatch: () => void;
  isPublishing: boolean;
}

export const Overview: React.FC<OverviewProps> = ({
  stats,
  logs,
  queueVideos,
  onNavigate,
  onPublishBatch,
  isPublishing,
}) => {
  return (
    <div className="space-y-8 animate-in fade-in duration-300">
      {/* Hero Automation Status Banner */}
      <div className="relative overflow-hidden rounded-3xl bg-gradient-to-r from-dark-900 via-dark-850 to-dark-900 border border-slate-800 p-8 shadow-2xl">
        <div className="absolute right-0 top-0 bottom-0 w-1/3 bg-gradient-to-l from-red-600/10 to-transparent pointer-events-none" />
        
        <div className="relative z-10 flex flex-col lg:flex-row lg:items-center lg:justify-between gap-6">
          <div className="space-y-2">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-red-500/10 border border-red-500/20 text-red-400 text-xs font-bold uppercase tracking-wider">
              <Radio className="w-3.5 h-3.5 animate-pulse" />
              <span>Autonomous Publishing Active</span>
            </div>
            <h1 className="text-2xl lg:text-3xl font-black tracking-tight text-white">
              Publishing <span className="text-red-500">2 Shorts</span> Every Hour
            </h1>
            <p className="text-sm text-slate-400 max-w-xl">
              Watching monitored folder, generating Gemini AI viral metadata, and automatically uploading scheduled clips with strict duplicate prevention.
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-4">
            <div className="bg-slate-900/80 border border-slate-800 px-5 py-3.5 rounded-2xl flex items-center gap-4">
              <div className="w-10 h-10 rounded-xl bg-indigo-500/15 text-indigo-400 flex items-center justify-center">
                <Clock className="w-5 h-5" />
              </div>
              <div>
                <div className="text-[10px] font-bold uppercase tracking-wider text-slate-500">Next Scheduled Slot</div>
                <div className="text-sm font-black font-mono text-white">
                  {stats?.nextPublishTime || 'Calculating...'}
                </div>
              </div>
            </div>

            <button
              onClick={onPublishBatch}
              disabled={isPublishing || (stats?.totalInQueue ?? 0) === 0}
              className="px-6 py-3.5 rounded-2xl bg-gradient-to-r from-red-600 to-red-500 hover:from-red-500 hover:to-red-600 text-white font-bold text-xs shadow-xl shadow-red-600/25 transition-all flex items-center gap-2 disabled:opacity-40"
            >
              <span>{isPublishing ? 'Uploading Batch...' : 'Publish Next Batch (2 Videos)'}</span>
              <ArrowUpRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>

      {/* Primary Metrics Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          label="Total Detected"
          value={stats?.totalDetected ?? 0}
          subValue="videos in folder"
          icon={Film}
          color="blue"
        />
        <StatCard
          label="In Publishing Queue"
          value={stats?.totalInQueue ?? 0}
          subValue="ready to publish"
          icon={ListOrdered}
          color="amber"
        />
        <StatCard
          label="Published to YouTube"
          value={stats?.totalPublished ?? 0}
          subValue="live shorts"
          icon={CheckCircle2}
          color="emerald"
        />
        <StatCard
          label="Published Today"
          value={stats?.todayPublishCount ?? 0}
          subValue="in IST timezone"
          icon={Calendar}
          color="purple"
          trend="2/hr target"
        />
      </div>

      {/* Secondary Metrics Row */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <StatCard
          label="Videos Remaining"
          value={stats?.videosRemaining ?? 0}
          subValue="in pipeline"
          icon={Layers}
          color="blue"
        />
        <StatCard
          label="Pending Retries"
          value={stats?.totalPending ?? 0}
          subValue="queue & retries"
          icon={Clock}
          color="purple"
        />
        <StatCard
          label="Failed Uploads"
          value={stats?.totalFailed ?? 0}
          subValue="after max retries"
          icon={AlertTriangle}
          color="red"
        />
      </div>

      {/* Pipeline Status Cards & Live Queue Preview */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Next Up in Queue (2 Videos) */}
        <div className="lg:col-span-2 glass-card rounded-3xl p-6 flex flex-col justify-between">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="text-sm font-bold text-white flex items-center gap-2">
                <ListOrdered className="w-4 h-4 text-red-500" />
                <span>Next 2 Videos Scheduled to Publish</span>
              </h3>
              <p className="text-xs text-slate-400 mt-0.5">Top of the queue for the next hourly release window</p>
            </div>
            <button
              onClick={() => onNavigate('queue')}
              className="text-xs font-semibold text-red-400 hover:text-red-300 flex items-center gap-1"
            >
              <span>View Full Queue</span>
              <ArrowUpRight className="w-3.5 h-3.5" />
            </button>
          </div>

          <div className="space-y-3">
            {queueVideos.slice(0, 2).map((item, idx) => (
              <div
                key={item.id}
                className="p-4 rounded-2xl bg-slate-900/60 border border-slate-800/80 flex items-center justify-between hover:border-slate-700 transition-colors"
              >
                <div className="flex items-center gap-4">
                  <div className="w-10 h-10 rounded-xl bg-red-600/10 border border-red-500/20 text-red-500 font-mono font-black text-sm flex items-center justify-center">
                    #{idx + 1}
                  </div>
                  <div>
                    <h4 className="text-xs font-bold text-white line-clamp-1">
                      {item.title || item.filename}
                    </h4>
                    <div className="flex items-center gap-3 text-[11px] text-slate-500 font-mono mt-0.5">
                      <span>{item.filename}</span>
                      <span>•</span>
                      <span>{item.duration}s</span>
                      <span>•</span>
                      <span className="uppercase">{item.resolution}</span>
                    </div>
                  </div>
                </div>

                <div className="flex items-center gap-3">
                  <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                    {item.status}
                  </span>
                </div>
              </div>
            ))}

            {queueVideos.length === 0 && (
              <div className="py-8 text-center text-slate-500 text-xs">
                No videos currently in queue. Add clips to your monitored folder to automatically queue them.
              </div>
            )}
          </div>

          <div className="mt-4 pt-4 border-t border-slate-800/80 flex items-center justify-between text-xs text-slate-400">
            <span>Configured slot capacity: <strong>2 videos / hour</strong></span>
            <span>Timezone: <strong>{stats?.timezone || 'Asia/Kolkata (IST)'}</strong></span>
          </div>
        </div>

        {/* Live System Guard Status */}
        <div className="glass-card rounded-3xl p-6 flex flex-col justify-between">
          <div>
            <h3 className="text-sm font-bold text-white flex items-center gap-2 mb-4">
              <Cpu className="w-4 h-4 text-emerald-400" />
              <span>Autonomous Guard Rails</span>
            </h3>

            <div className="space-y-3.5">
              <div className="flex items-start gap-3">
                <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
                <div>
                  <div className="text-xs font-bold text-slate-200">Permanent Duplicate Prevention</div>
                  <div className="text-[11px] text-slate-400">SHA-256 hash verified against database. Published videos will never republish.</div>
                </div>
              </div>

              <div className="flex items-start gap-3">
                <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
                <div>
                  <div className="text-xs font-bold text-slate-200">File Copy Stability Guard</div>
                  <div className="text-[11px] text-slate-400">Incomplete copying files are automatically detected and held until write completes.</div>
                </div>
              </div>

              <div className="flex items-start gap-3">
                <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
                <div>
                  <div className="text-xs font-bold text-slate-200">Auto Restart Recovery</div>
                  <div className="text-[11px] text-slate-400">Application restarts resume interrupted tasks with zero duplicate uploads.</div>
                </div>
              </div>

              <div className="flex items-start gap-3">
                <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
                <div>
                  <div className="text-xs font-bold text-slate-200">Gemini AI SEO Generation</div>
                  <div className="text-[11px] text-slate-400">Generates high-converting titles, descriptions, hashtags, and gaming tags.</div>
                </div>
              </div>
            </div>
          </div>

          <div className="mt-6 pt-4 border-t border-slate-800 text-[11px] text-slate-500 font-mono flex items-center justify-between">
            <span>Spring Boot + MySQL</span>
            <span>YouTube Data API v3</span>
          </div>
        </div>
      </div>
    </div>
  );
};
