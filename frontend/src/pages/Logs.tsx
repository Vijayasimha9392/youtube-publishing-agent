import React, { useState } from 'react';
import { 
  FileText, 
  RefreshCw, 
  Filter, 
  AlertCircle, 
  CheckCircle, 
  Info, 
  Terminal,
  Hash,
  Clock
} from 'lucide-react';
import { AutomationLog } from '../types';

interface LogsProps {
  logs: AutomationLog[];
  onRefresh: () => void;
  isRefreshing: boolean;
}

export const Logs: React.FC<LogsProps> = ({ logs, onRefresh, isRefreshing }) => {
  const [selectedCategory, setSelectedCategory] = useState<string>('ALL');

  const categories = [
    'ALL',
    'SCAN',
    'DETECT',
    'VALIDATION',
    'DUPLICATE',
    'AI_METADATA',
    'QUEUE',
    'UPLOAD',
    'PUBLISH',
    'RETRY',
    'FAILED',
    'RESTART',
    'SETTINGS',
  ];

  const filteredLogs = logs.filter(
    (l) => selectedCategory === 'ALL' || l.category.toUpperCase() === selectedCategory.toUpperCase()
  );

  const getLevelBadge = (level: string) => {
    switch (level) {
      case 'ERROR':
        return (
          <span className="flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-bold bg-red-500/10 text-red-400 border border-red-500/20">
            <AlertCircle className="w-2.5 h-2.5" />
            ERROR
          </span>
        );
      case 'WARN':
        return (
          <span className="flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-500/10 text-amber-400 border border-amber-500/20">
            <AlertCircle className="w-2.5 h-2.5" />
            WARN
          </span>
        );
      default:
        return (
          <span className="flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-bold bg-blue-500/10 text-blue-400 border border-blue-500/20">
            <Info className="w-2.5 h-2.5" />
            INFO
          </span>
        );
    }
  };

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* Category Filter Pills & Refresh */}
      <div className="glass-card p-4 rounded-2xl flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div className="flex flex-wrap items-center gap-1.5">
          {categories.map((cat) => (
            <button
              key={cat}
              onClick={() => setSelectedCategory(cat)}
              className={`px-3 py-1.5 rounded-xl text-xs font-semibold transition-all ${
                selectedCategory === cat
                  ? 'bg-red-600 text-white shadow-md shadow-red-600/20'
                  : 'bg-slate-900/60 hover:bg-slate-800 text-slate-400 hover:text-slate-200 border border-slate-800'
              }`}
            >
              {cat}
            </button>
          ))}
        </div>

        <button
          onClick={onRefresh}
          disabled={isRefreshing}
          className="flex items-center gap-2 px-3.5 py-1.5 rounded-xl text-xs font-semibold bg-slate-800 hover:bg-slate-700 text-slate-300 border border-slate-700 transition-colors shrink-0 disabled:opacity-50"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${isRefreshing ? 'animate-spin text-red-400' : ''}`} />
          <span>Refresh</span>
        </button>
      </div>

      {/* Terminal-like Logs Viewer */}
      <div className="glass-card rounded-3xl overflow-hidden border border-slate-800 shadow-2xl">
        <div className="px-6 py-3 border-b border-slate-800 bg-slate-950 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Terminal className="w-4 h-4 text-red-500" />
            <span className="text-xs font-bold text-white font-mono uppercase tracking-wider">
              Real-Time Automation Event Stream
            </span>
          </div>
          <span className="text-[11px] font-mono text-slate-500">
            Showing {filteredLogs.length} events
          </span>
        </div>

        <div className="divide-y divide-slate-800/40 max-h-[650px] overflow-y-auto font-mono text-xs">
          {filteredLogs.map((log) => (
            <div
              key={log.id}
              className="px-6 py-3 hover:bg-slate-900/40 transition-colors flex flex-col md:flex-row md:items-start justify-between gap-2"
            >
              <div className="flex items-start gap-3">
                <span className="text-slate-500 text-[11px] shrink-0 pt-0.5">
                  {new Date(log.createdAt).toLocaleTimeString()}
                </span>

                <div className="shrink-0">{getLevelBadge(log.level)}</div>

                <span className="px-2 py-0.5 rounded bg-slate-900 text-slate-300 border border-slate-800 text-[10px] font-bold shrink-0">
                  {log.category}
                </span>

                <span className="text-slate-200 leading-relaxed break-all">
                  {log.message}
                </span>
              </div>

              {log.fileHash && (
                <span className="text-[10px] text-slate-500 shrink-0 truncate max-w-[120px]" title={log.fileHash}>
                  hash: {log.fileHash.substring(0, 10)}...
                </span>
              )}
            </div>
          ))}

          {filteredLogs.length === 0 && (
            <div className="py-20 text-center text-slate-500 text-xs">
              No logs found for selected category.
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
