import React from 'react';
import { LucideIcon } from 'lucide-react';

interface StatCardProps {
  label: string;
  value: string | number;
  subValue?: string;
  icon: LucideIcon;
  color?: 'red' | 'blue' | 'emerald' | 'amber' | 'purple';
  trend?: string;
}

export const StatCard: React.FC<StatCardProps> = ({
  label,
  value,
  subValue,
  icon: Icon,
  color = 'red',
  trend,
}) => {
  const colorMap = {
    red: {
      bg: 'bg-red-500/10 border-red-500/20 text-red-400',
      iconBg: 'bg-red-500/15 text-red-500',
    },
    blue: {
      bg: 'bg-blue-500/10 border-blue-500/20 text-blue-400',
      iconBg: 'bg-blue-500/15 text-blue-500',
    },
    emerald: {
      bg: 'bg-emerald-500/10 border-emerald-500/20 text-emerald-400',
      iconBg: 'bg-emerald-500/15 text-emerald-500',
    },
    amber: {
      bg: 'bg-amber-500/10 border-amber-500/20 text-amber-400',
      iconBg: 'bg-amber-500/15 text-amber-500',
    },
    purple: {
      bg: 'bg-purple-500/10 border-purple-500/20 text-purple-400',
      iconBg: 'bg-purple-500/15 text-purple-500',
    },
  };

  const scheme = colorMap[color];

  return (
    <div className="glass-card p-5 rounded-2xl flex flex-col justify-between hover:border-slate-700 transition-all">
      <div className="flex items-center justify-between">
        <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">{label}</span>
        <div className={`w-9 h-9 rounded-xl flex items-center justify-center ${scheme.iconBg}`}>
          <Icon className="w-4 h-4" />
        </div>
      </div>

      <div className="mt-4 flex items-baseline justify-between">
        <div>
          <span className="text-2xl font-black tracking-tight text-white">{value}</span>
          {subValue && (
            <span className="text-xs text-slate-400 ml-2 font-medium">{subValue}</span>
          )}
        </div>
        {trend && (
          <span className="text-[11px] font-semibold text-emerald-400 bg-emerald-500/10 px-2 py-0.5 rounded-full border border-emerald-500/20">
            {trend}
          </span>
        )}
      </div>
    </div>
  );
};
