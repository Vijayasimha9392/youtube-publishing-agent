import React from 'react';
import { 
  CheckCircle2, 
  ExternalLink, 
  Youtube, 
  Lock, 
  Calendar, 
  Hash, 
  Copy, 
  Check
} from 'lucide-react';
import { PublishedVideo } from '../types';

interface PublishedProps {
  publishedVideos: PublishedVideo[];
}

export const Published: React.FC<PublishedProps> = ({ publishedVideos }) => {
  const [copiedId, setCopiedId] = React.useState<string | null>(null);

  const copyToClipboard = (text: string, id: string) => {
    navigator.clipboard.writeText(text);
    setCopiedId(id);
    setTimeout(() => setCopiedId(null), 2000);
  };

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* Permanent Duplicate Prevention Header Banner */}
      <div className="glass-card p-6 rounded-3xl flex flex-col md:flex-row md:items-center justify-between gap-4 border-emerald-500/20 bg-gradient-to-r from-emerald-950/20 via-dark-900 to-dark-900">
        <div>
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-xs font-bold uppercase tracking-wider mb-2">
            <Lock className="w-3.5 h-3.5" />
            <span>Permanent Duplicate Protection Active</span>
          </div>
          <h2 className="text-xl font-bold text-white">Published YouTube Shorts</h2>
          <p className="text-xs text-slate-400 mt-1 max-w-2xl">
            Each published video is permanently locked in the database with its SHA-256 hash. Even if computer restarts, filenames change, or videos are recopied into folders, they will never be republished.
          </p>
        </div>

        <div className="px-5 py-3 rounded-2xl bg-slate-900/80 border border-slate-800 flex items-center gap-3 shrink-0">
          <CheckCircle2 className="w-8 h-8 text-emerald-400" />
          <div>
            <div className="text-[10px] font-bold uppercase text-slate-500">Total Published</div>
            <div className="text-xl font-black font-mono text-white">{publishedVideos.length} Shorts</div>
          </div>
        </div>
      </div>

      {/* Published List */}
      <div className="glass-card rounded-3xl overflow-hidden border border-slate-800">
        <div className="divide-y divide-slate-800/60">
          {publishedVideos.map((item) => (
            <div
              key={item.id}
              className="px-6 py-5 flex flex-col md:flex-row md:items-center justify-between gap-4 hover:bg-slate-900/40 transition-colors"
            >
              <div className="flex items-start gap-4">
                <div className="w-12 h-12 rounded-2xl bg-red-600/15 text-red-500 flex items-center justify-center shrink-0 shadow-lg shadow-red-600/10 mt-0.5">
                  <Youtube className="w-6 h-6 fill-red-500" />
                </div>

                <div className="space-y-1">
                  <h4 className="text-sm font-bold text-white line-clamp-1">
                    {item.title || 'YouTube Short #' + item.youtubeVideoId}
                  </h4>

                  <div className="flex flex-wrap items-center gap-3 text-xs text-slate-400 font-mono">
                    <span className="flex items-center gap-1 text-slate-300">
                      <Hash className="w-3.5 h-3.5 text-slate-500" />
                      ID: <span className="font-bold text-white">{item.youtubeVideoId}</span>
                    </span>
                    <span>•</span>
                    <span className="flex items-center gap-1 text-slate-400">
                      <Calendar className="w-3.5 h-3.5 text-slate-500" />
                      {new Date(item.publishedAt).toLocaleString()}
                    </span>
                  </div>

                  <div className="text-[10px] font-mono text-slate-500 flex items-center gap-1.5 pt-1">
                    <Lock className="w-3 h-3 text-emerald-400" />
                    <span>Locked Hash:</span>
                    <span className="truncate max-w-xs">{item.fileHash}</span>
                  </div>
                </div>
              </div>

              {/* Actions & Link */}
              <div className="flex items-center gap-3 shrink-0">
                <button
                  onClick={() => copyToClipboard(item.youtubeUrl, item.youtubeVideoId)}
                  className="px-3 py-1.5 rounded-xl text-xs font-semibold bg-slate-800 hover:bg-slate-700 text-slate-300 border border-slate-700 transition-colors flex items-center gap-1.5"
                  title="Copy YouTube URL"
                >
                  {copiedId === item.youtubeVideoId ? (
                    <>
                      <Check className="w-3.5 h-3.5 text-emerald-400" />
                      <span className="text-emerald-400">Copied</span>
                    </>
                  ) : (
                    <>
                      <Copy className="w-3.5 h-3.5 text-slate-400" />
                      <span>Copy Link</span>
                    </>
                  )}
                </button>

                <a
                  href={item.youtubeUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="px-4 py-2 rounded-xl text-xs font-bold bg-gradient-to-r from-red-600 to-red-500 hover:from-red-500 hover:to-red-600 text-white shadow-lg shadow-red-600/20 transition-all flex items-center gap-1.5"
                >
                  <span>Watch on YouTube</span>
                  <ExternalLink className="w-3.5 h-3.5" />
                </a>
              </div>
            </div>
          ))}

          {publishedVideos.length === 0 && (
            <div className="py-20 text-center text-slate-500 text-xs">
              No videos published yet. When the scheduled batch runs or you click "Publish Next Batch", published videos will appear here with permanent duplicate locks.
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
