import React, { useState } from 'react';
import { 
  CalendarClock, 
  Play, 
  Pause, 
  Save, 
  Clock, 
  Globe, 
  ShieldCheck,
  Check
} from 'lucide-react';
import { PublishingSchedule } from '../types';

interface SchedulerProps {
  schedule: PublishingSchedule | null;
  onUpdateSchedule: (schedule: Partial<PublishingSchedule>) => void;
  onToggleAutomation: (active: boolean) => void;
}

export const Scheduler: React.FC<SchedulerProps> = ({
  schedule,
  onUpdateSchedule,
  onToggleAutomation,
}) => {
  const [videosPerInterval, setVideosPerInterval] = useState(schedule?.videosPerInterval ?? 2);
  const [intervalMinutes, setIntervalMinutes] = useState(schedule?.intervalMinutes ?? 60);
  const [startTime, setStartTime] = useState(schedule?.startTime ?? '09:00');
  const [endTime, setEndTime] = useState(schedule?.endTime ?? '23:59');
  const [timezone, setTimezone] = useState(schedule?.timezone ?? 'Asia/Kolkata');
  const [maxDailyUploads, setMaxDailyUploads] = useState(schedule?.maxDailyUploads ?? 20);
  const [isSaving, setIsSaving] = useState(false);
  const [saveSuccess, setSaveSuccess] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setIsSaving(true);
    onUpdateSchedule({
      videosPerInterval,
      intervalMinutes,
      startTime,
      endTime,
      timezone,
      maxDailyUploads,
    });
    setSaveSuccess(true);
    setTimeout(() => {
      setIsSaving(false);
      setSaveSuccess(false);
    }, 1500);
  };

  const isActive = schedule?.isActive ?? true;

  // Generate example 24h schedule preview table (like prompt Section 3)
  const scheduleSlots = [
    { time: '09:00 AM', batch: 'Video 1 and Video 2', status: 'Slot 1' },
    { time: '10:00 AM', batch: 'Video 3 and Video 4', status: 'Slot 2' },
    { time: '11:00 AM', batch: 'Video 5 and Video 6', status: 'Slot 3' },
    { time: '12:00 PM', batch: 'Video 7 and Video 8', status: 'Slot 4' },
    { time: '01:00 PM', batch: 'Video 9 and Video 10', status: 'Slot 5' },
    { time: '02:00 PM', batch: 'Video 11 and Video 12', status: 'Slot 6' },
    { time: '03:00 PM', batch: 'Video 13 and Video 14', status: 'Slot 7' },
    { time: '04:00 PM', batch: 'Video 15 and Video 16', status: 'Slot 8' },
    { time: '05:00 PM', batch: 'Video 17 and Video 18', status: 'Slot 9' },
    { time: '06:00 PM', batch: 'Video 19 and next clip', status: 'Slot 10' },
  ];

  return (
    <div className="space-y-8 animate-in fade-in duration-300">
      {/* Engine Status & Toggle Banner */}
      <div className="glass-card p-6 rounded-3xl flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            <span className={`w-3 h-3 rounded-full ${isActive ? 'bg-emerald-500 animate-pulse' : 'bg-amber-500'}`} />
            <h2 className="text-xl font-bold text-white">
              Automation Engine: {isActive ? 'Running' : 'Paused'}
            </h2>
          </div>
          <p className="text-xs text-slate-400">
            {isActive
              ? 'Autonomous scheduler is actively monitoring intervals and executing 2 videos every hour.'
              : 'Scheduler is paused. Videos will remain queued until automation is resumed.'}
          </p>
        </div>

        <button
          onClick={() => onToggleAutomation(!isActive)}
          className={`px-6 py-3 rounded-2xl font-bold text-xs shadow-xl transition-all flex items-center gap-2 shrink-0 ${
            isActive
              ? 'bg-amber-500 hover:bg-amber-600 text-dark-950 shadow-amber-500/20'
              : 'bg-emerald-500 hover:bg-emerald-600 text-dark-950 shadow-emerald-500/20'
          }`}
        >
          {isActive ? (
            <>
              <Pause className="w-4 h-4 fill-current" />
              <span>Pause Automation</span>
            </>
          ) : (
            <>
              <Play className="w-4 h-4 fill-current" />
              <span>Resume Automation</span>
            </>
          )}
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-5 gap-8">
        {/* Settings Form */}
        <div className="lg:col-span-3 glass-card rounded-3xl p-6">
          <h3 className="text-sm font-bold text-white flex items-center gap-2 mb-6">
            <CalendarClock className="w-4 h-4 text-red-500" />
            <span>Publishing Rules & Rate Limits</span>
          </h3>

          <form onSubmit={handleSubmit} className="space-y-5">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-bold text-slate-300 mb-1">
                  Videos Per Interval
                </label>
                <input
                  type="number"
                  min={1}
                  max={10}
                  value={videosPerInterval}
                  onChange={(e) => setVideosPerInterval(parseInt(e.target.value) || 2)}
                  className="w-full px-3.5 py-2.5 rounded-xl bg-slate-950 border border-slate-800 text-white text-xs font-mono focus:outline-none focus:border-red-500"
                />
                <span className="text-[10px] text-slate-500 mt-1 block">Default: 2 videos</span>
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-300 mb-1">
                  Interval Frequency (Minutes)
                </label>
                <input
                  type="number"
                  min={5}
                  max={1440}
                  value={intervalMinutes}
                  onChange={(e) => setIntervalMinutes(parseInt(e.target.value) || 60)}
                  className="w-full px-3.5 py-2.5 rounded-xl bg-slate-950 border border-slate-800 text-white text-xs font-mono focus:outline-none focus:border-red-500"
                />
                <span className="text-[10px] text-slate-500 mt-1 block">Default: 60 minutes (1 hour)</span>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-bold text-slate-300 mb-1">
                  Active Start Time
                </label>
                <input
                  type="time"
                  value={startTime}
                  onChange={(e) => setStartTime(e.target.value)}
                  className="w-full px-3.5 py-2.5 rounded-xl bg-slate-950 border border-slate-800 text-white text-xs font-mono focus:outline-none focus:border-red-500"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-300 mb-1">
                  Active End Time
                </label>
                <input
                  type="time"
                  value={endTime}
                  onChange={(e) => setEndTime(e.target.value)}
                  className="w-full px-3.5 py-2.5 rounded-xl bg-slate-950 border border-slate-800 text-white text-xs font-mono focus:outline-none focus:border-red-500"
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-bold text-slate-300 mb-1">
                  Scheduler Timezone
                </label>
                <select
                  value={timezone}
                  onChange={(e) => setTimezone(e.target.value)}
                  className="w-full px-3.5 py-2.5 rounded-xl bg-slate-950 border border-slate-800 text-white text-xs focus:outline-none focus:border-red-500 font-mono"
                >
                  <option value="Asia/Kolkata">Asia/Kolkata (IST - UTC+5:30)</option>
                  <option value="America/New_York">America/New_York (EST)</option>
                  <option value="America/Los_Angeles">America/Los_Angeles (PST)</option>
                  <option value="UTC">UTC</option>
                  <option value="Europe/London">Europe/London (GMT)</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-300 mb-1">
                  Max Daily Uploads Limit
                </label>
                <input
                  type="number"
                  min={1}
                  max={100}
                  value={maxDailyUploads}
                  onChange={(e) => setMaxDailyUploads(parseInt(e.target.value) || 20)}
                  className="w-full px-3.5 py-2.5 rounded-xl bg-slate-950 border border-slate-800 text-white text-xs font-mono focus:outline-none focus:border-red-500"
                />
                <span className="text-[10px] text-slate-500 mt-1 block">Default: 20 per day</span>
              </div>
            </div>

            <div className="pt-4 border-t border-slate-800 flex justify-end">
              <button
                type="submit"
                disabled={isSaving}
                className="flex items-center gap-2 px-6 py-2.5 rounded-xl text-xs font-bold bg-red-600 hover:bg-red-500 text-white shadow-lg shadow-red-600/20 transition-all disabled:opacity-50"
              >
                {saveSuccess ? (
                  <>
                    <Check className="w-4 h-4" />
                    <span>Saved Successfully!</span>
                  </>
                ) : (
                  <>
                    <Save className="w-4 h-4" />
                    <span>Save Schedule Policy</span>
                  </>
                )}
              </button>
            </div>
          </form>
        </div>

        {/* Visual Daily Publishing Slots Preview */}
        <div className="lg:col-span-2 glass-card rounded-3xl p-6 flex flex-col justify-between">
          <div>
            <h3 className="text-sm font-bold text-white flex items-center gap-2 mb-2">
              <Clock className="w-4 h-4 text-indigo-400" />
              <span>Daily Publishing Slots Simulation</span>
            </h3>
            <p className="text-xs text-slate-400 mb-4">
              Visual example of your 2-video-per-hour publishing flow (Asia/Kolkata):
            </p>

            <div className="space-y-2 max-h-[360px] overflow-y-auto pr-1">
              {scheduleSlots.map((slot, i) => (
                <div
                  key={i}
                  className="p-2.5 rounded-xl bg-slate-900/60 border border-slate-800/80 flex items-center justify-between text-xs"
                >
                  <div className="flex items-center gap-2.5">
                    <span className="font-mono font-bold text-white text-[11px] w-16">
                      {slot.time}
                    </span>
                    <span className="text-slate-300 font-medium text-[11px]">
                      {slot.batch}
                    </span>
                  </div>
                  <span className="text-[10px] font-mono px-2 py-0.5 rounded-full bg-slate-800 text-slate-400 border border-slate-700">
                    {slot.status}
                  </span>
                </div>
              ))}
            </div>
          </div>

          <div className="mt-4 pt-3 border-t border-slate-800 text-[11px] text-slate-400 flex items-center gap-2">
            <ShieldCheck className="w-4 h-4 text-emerald-400 shrink-0" />
            <span>Publishing pauses automatically when queue is empty or daily limit is reached.</span>
          </div>
        </div>
      </div>
    </div>
  );
};
