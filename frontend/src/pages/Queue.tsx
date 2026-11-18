import React from 'react';
import { 
  ListOrdered, 
  Play, 
  ArrowUp, 
  ArrowDown, 
  RotateCcw, 
  Trash2, 
  CalendarClock, 
  CheckCircle2, 
  Clock,
  Film
} from 'lucide-react';
import { VideoDetail } from '../types';

interface QueueProps {
  queueVideos: VideoDetail[];
  onReorder: (videoFileId: number, newPosition: number) => void;
  onRetry: (videoFileId: number) => void;
  onSkip: (videoFileId: number) => void;
  onPublishBatch: () => void;
  isPublishing: boolean;
  intervalMinutes: number;
  videosPerInterval: number;
}

export const Queue: React.FC<QueueProps> = ({
  queueVideos,
  onReorder,
  onRetry,
  onSkip,
  onPublishBatch,
  isPublishing,
  intervalMinutes = 60,
  videosPerInterval = 2,
}) => {
  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* Header Info Banner */}
      <div className="glass-card p-6 rounded-3xl flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-indigo-500/10 border border-indigo-500/20 text-indigo-400 text-xs font-bold uppercase tracking-wider mb-2">
            <CalendarClock className="w-3.5 h-3.5" />
            <span>2-Videos-Per-Hour Publishing Schedule</span>
          </div>
          <h2 className="text-xl font-bold text-white">Active Publishing Queue</h2>
          <p className="text-xs text-slate-400 mt-1">
            Videos are strictly dispatched in batches of {videosPerInterval} every {intervalMinutes} minutes. Reorder queue positions to prioritize viral clips.
          </p>
        </div>

        <button
          onClick={onPublishBatch}
          disabled={isPublishing || queueVideos.length === 0}
          className="px-5 py-3 rounded-2xl bg-gradient-to-r from-red-600 to-red-500 hover:from-red-500 hover:to-red-600 text-white font-bold text-xs shadow-xl shadow-red-600/25 transition-all flex items-center gap-2 shrink-0 disabled:opacity-40"
        >
          <Play className={`w-4 h-4 fill-current ${isPublishing ? 'animate-pulse' : ''}`} />
          <span>{isPublishing ? 'Publishing Next 2...' : 'Publish Next Batch (2 Videos)'}</span>
        </button>
      </div>

      {/* Queue List Table */}
      <div className="glass-card rounded-3xl overflow-hidden border border-slate-800">
        <div className="px-6 py-4 border-b border-slate-800/80 bg-slate-900/60 flex items-center justify-between">
          <span className="text-xs font-bold text-slate-300 uppercase tracking-wider">
            Total In Queue: {queueVideos.length} Videos
          </span>
          <span className="text-xs font-mono text-slate-400">
            Estimated batches: {Math.ceil(queueVideos.length / videosPerInterval)} hourly release slots
          </span>
        </div>

        <div className="divide-y divide-slate-800/60">
          {queueVideos.map((item, index) => {
            const batchNum = Math.floor(index / videosPerInterval) + 1;
            const isFirstInBatch = index % videosPerInterval === 0;

            return (
              <React.Fragment key={item.id}>
                {isFirstInBatch && (
                  <div className="px-6 py-2 bg-dark-950/80 border-y border-slate-800 text-[11px] font-bold text-slate-400 font-mono flex items-center justify-between">
                    <span className="flex items-center gap-2 text-indigo-400">
                      <Clock className="w-3.5 h-3.5" />
                      Hourly Batch #{batchNum} (Slot: {batchNum} hr from now)
                    </span>
                    <span className="text-slate-500">Target: 2 videos</span>
                  </div>
                )}

                <div className="px-6 py-4 flex items-center justify-between hover:bg-slate-900/40 transition-colors">
                  <div className="flex items-center gap-4">
                    <div className="w-8 h-8 rounded-xl bg-slate-800 border border-slate-700 text-slate-300 font-mono font-bold text-xs flex items-center justify-center">
                      #{index + 1}
                    </div>

                    <div className="w-10 h-10 rounded-xl bg-red-600/10 border border-red-500/20 text-red-500 flex items-center justify-center shrink-0">
                      <Film className="w-5 h-5" />
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
                        <span>•</span>
                        <span className="text-slate-400">
                          {((item.fileSize) / (1024 * 1024)).toFixed(1)} MB
                        </span>
                      </div>
                    </div>
                  </div>

                  <div className="flex items-center gap-3">
                    {/* Status Badge */}
                    <span
                      className={`text-[10px] font-bold px-2.5 py-1 rounded-full border ${
                        item.status === 'UPLOADING'
                          ? 'bg-amber-500/10 text-amber-400 border-amber-500/20 animate-pulse'
                          : item.status === 'RETRY_PENDING'
                          ? 'bg-purple-500/10 text-purple-400 border-purple-500/20'
                          : 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
                      }`}
                    >
                      {item.status}
                    </span>

                    {/* Reorder Buttons */}
                    <div className="flex items-center gap-1 border border-slate-800 rounded-xl p-1 bg-slate-900/60">
                      <button
                        onClick={() => onReorder(item.id, Math.max(1, (item.queuePosition || index + 1) - 1))}
                        disabled={index === 0}
                        className="w-7 h-7 rounded-lg hover:bg-slate-800 text-slate-400 hover:text-white flex items-center justify-center transition-colors disabled:opacity-30"
                        title="Move Up"
                      >
                        <ArrowUp className="w-3.5 h-3.5" />
                      </button>
                      <button
                        onClick={() => onReorder(item.id, (item.queuePosition || index + 1) + 1)}
                        disabled={index === queueVideos.length - 1}
                        className="w-7 h-7 rounded-lg hover:bg-slate-800 text-slate-400 hover:text-white flex items-center justify-center transition-colors disabled:opacity-30"
                        title="Move Down"
                      >
                        <ArrowDown className="w-3.5 h-3.5" />
                      </button>
                    </div>

                    {/* Retry Button */}
                    {(item.status === 'FAILED' || item.status === 'RETRY_PENDING') && (
                      <button
                        onClick={() => onRetry(item.id)}
                        className="p-2 rounded-xl bg-purple-500/10 hover:bg-purple-500/20 text-purple-400 border border-purple-500/20 transition-colors"
                        title="Retry Upload"
                      >
                        <RotateCcw className="w-3.5 h-3.5" />
                      </button>
                    )}

                    {/* Skip / Remove */}
                    <button
                      onClick={() => onSkip(item.id)}
                      className="p-2 rounded-xl bg-slate-800 hover:bg-red-500/20 text-slate-400 hover:text-red-400 border border-slate-700 hover:border-red-500/30 transition-colors"
                      title="Skip / Remove from Queue"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>
              </React.Fragment>
            );
          })}

          {queueVideos.length === 0 && (
            <div className="py-16 text-center text-slate-500 text-xs">
              No videos currently in publishing queue. Add MP4 clips to your folder to queue them automatically.
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
