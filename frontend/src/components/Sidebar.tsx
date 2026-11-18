import React from 'react';
import { 
  LayoutDashboard, 
  Film, 
  ListOrdered, 
  CheckCircle2, 
  CalendarClock, 
  FileText, 
  Settings as SettingsIcon,
  Youtube,
  Radio,
  FolderOpen
} from 'lucide-react';

interface SidebarProps {
  currentTab: string;
  setCurrentTab: (tab: string) => void;
  queueCount: number;
  publishedCount: number;
  isAutomationActive: boolean;
}

export const Sidebar: React.FC<SidebarProps> = ({
  currentTab,
  setCurrentTab,
  queueCount,
  publishedCount,
  isAutomationActive,
}) => {
  const navItems = [
    { id: 'overview', label: 'Overview', icon: LayoutDashboard },
    { id: 'library', label: 'Video Library', icon: Film },
    { id: 'queue', label: 'Publishing Queue', icon: ListOrdered, badge: queueCount },
    { id: 'published', label: 'Published Videos', icon: CheckCircle2, badge: publishedCount },
    { id: 'scheduler', label: 'Scheduler', icon: CalendarClock },
    { id: 'logs', label: 'Automation Logs', icon: FileText },
    { id: 'settings', label: 'Settings', icon: SettingsIcon },
  ];

  return (
    <aside className="w-64 bg-dark-900 border-r border-slate-800/80 flex flex-col h-screen fixed left-0 top-0 z-30 select-none">
      {/* Brand Header */}
      <div className="h-16 px-6 border-b border-slate-800/80 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-red-600 to-red-500 flex items-center justify-center shadow-lg shadow-red-600/20">
            <Youtube className="w-5 h-5 text-white fill-white" />
          </div>
          <div>
            <h1 className="text-sm font-bold tracking-tight text-white flex items-center gap-1.5">
              AUTOSHORT
              <span className="text-[10px] font-extrabold uppercase px-1.5 py-0.2 rounded bg-red-500/20 text-red-400 border border-red-500/30">
                AI 2.0
              </span>
            </h1>
            <p className="text-[11px] text-slate-400 font-medium">Publishing Agent</p>
          </div>
        </div>
      </div>

      {/* Live System Indicator */}
      <div className="mx-4 my-3 px-3.5 py-2.5 rounded-xl bg-slate-900/90 border border-slate-800 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <span className="relative flex h-2.5 w-2.5">
            <span className={`animate-ping absolute inline-flex h-full w-full rounded-full ${isAutomationActive ? 'bg-emerald-400' : 'bg-amber-400'} opacity-75`}></span>
            <span className={`relative inline-flex rounded-full h-2.5 w-2.5 ${isAutomationActive ? 'bg-emerald-500' : 'bg-amber-500'}`}></span>
          </span>
          <span className="text-xs font-semibold text-slate-300">
            {isAutomationActive ? 'Autonomous Mode' : 'Engine Paused'}
          </span>
        </div>
        <span className="text-[10px] uppercase font-mono px-1.5 py-0.5 rounded bg-slate-800 text-slate-400 border border-slate-700">
          2/hr
        </span>
      </div>

      {/* Navigation List */}
      <nav className="flex-1 px-3 py-2 space-y-1 overflow-y-auto">
        {navItems.map((item) => {
          const Icon = item.icon;
          const isActive = currentTab === item.id;
          return (
            <button
              key={item.id}
              onClick={() => setCurrentTab(item.id)}
              className={`w-full flex items-center justify-between px-3.5 py-2.5 rounded-xl text-xs font-semibold transition-all ${
                isActive
                  ? 'bg-red-600 text-white shadow-lg shadow-red-600/25'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
              }`}
            >
              <div className="flex items-center gap-3">
                <Icon className={`w-4 h-4 ${isActive ? 'text-white' : 'text-slate-400'}`} />
                <span>{item.label}</span>
              </div>
              {item.badge !== undefined && item.badge > 0 && (
                <span
                  className={`text-[10px] font-mono font-bold px-1.5 py-0.5 rounded-full ${
                    isActive
                      ? 'bg-white text-red-600'
                      : 'bg-slate-800 text-slate-300 border border-slate-700'
                  }`}
                >
                  {item.badge}
                </span>
              )}
            </button>
          );
        })}
      </nav>

      {/* Footer Info */}
      <div className="p-4 border-t border-slate-800/80 bg-slate-900/40">
        <div className="flex items-center gap-2 text-[11px] text-slate-400 font-mono">
          <FolderOpen className="w-3.5 h-3.5 text-slate-400 shrink-0" />
          <span className="truncate" title="Clips">
            Watching: \Clips
          </span>
        </div>
      </div>
    </aside>
  );
};
