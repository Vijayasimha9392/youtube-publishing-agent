import React from 'react';
import { X, Film, Hash, Clock, HardDrive, Maximize2 } from 'lucide-react';
import { VideoDetail } from '../types';
import { getVideoMediaUrl } from '../services/api';

interface VideoPlayerModalProps {
  video: VideoDetail | null;
  onClose: () => void;
}

export const VideoPlayerModal: React.FC<VideoPlayerModalProps> = ({ video, onClose }) => {
  if (!video) return null;

  const streamUrl = getVideoMediaUrl(video.id);
  const sizeMb = (video.fileSize / (1024 * 1024)).toFixed(2);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="bg-dark-900 border border-slate-800 rounded-3xl max-w-4xl w-full overflow-hidden shadow-2xl flex flex-col max-h-[90vh]">
        {/* Modal Header */}
        <div className="px-6 py-4 border-b border-slate-800 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-lg bg-red-600/20 text-red-500 flex items-center justify-center">
              <Film className="w-4 h-4" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-white truncate max-w-md">
                {video.title || video.filename}
              </h3>
              <p className="text-[11px] text-slate-400 font-mono">{video.filename}</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="w-8 h-8 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-400 hover:text-white flex items-center justify-center transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Modal Body: Player + Info */}
        <div className="grid grid-cols-1 md:grid-cols-5 gap-0 overflow-y-auto">
          {/* Video Player */}
          <div className="md:col-span-3 bg-black flex items-center justify-center p-4 min-h-[350px]">
            <video
              controls
              autoPlay
              playsInline
              className="max-h-[500px] w-auto max-w-full rounded-xl shadow-lg border border-slate-800"
              src={streamUrl}
            >
              Your browser does not support video playback.
            </video>
          </div>

          {/* Video Details */}
          <div className="md:col-span-2 p-6 flex flex-col justify-between border-t md:border-t-0 md:border-l border-slate-800 bg-dark-950/40">
            <div className="space-y-4">
              <div>
                <span className="text-[10px] font-bold uppercase text-slate-500 tracking-wider">Publishing Title</span>
                <p className="text-xs font-semibold text-slate-200 mt-1 leading-relaxed">
                  {video.title || video.filename}
                </p>
              </div>

              <div>
                <span className="text-[10px] font-bold uppercase text-slate-500 tracking-wider">Description</span>
                <p className="text-[11px] text-slate-300 mt-1 bg-slate-900/60 p-2.5 rounded-xl border border-slate-800 font-mono whitespace-pre-wrap max-h-36 overflow-y-auto">
                  {video.description || 'No description generated'}
                </p>
              </div>

              <div className="grid grid-cols-2 gap-3 pt-2">
                <div className="bg-slate-900/60 p-2.5 rounded-xl border border-slate-800 flex items-center gap-2">
                  <Clock className="w-3.5 h-3.5 text-slate-400" />
                  <div>
                    <div className="text-[10px] text-slate-500 uppercase">Duration</div>
                    <div className="text-xs font-bold text-slate-200 font-mono">{video.duration}s</div>
                  </div>
                </div>

                <div className="bg-slate-900/60 p-2.5 rounded-xl border border-slate-800 flex items-center gap-2">
                  <Maximize2 className="w-3.5 h-3.5 text-slate-400" />
                  <div>
                    <div className="text-[10px] text-slate-500 uppercase">Resolution</div>
                    <div className="text-xs font-bold text-slate-200 font-mono">{video.resolution}</div>
                  </div>
                </div>

                <div className="bg-slate-900/60 p-2.5 rounded-xl border border-slate-800 flex items-center gap-2">
                  <HardDrive className="w-3.5 h-3.5 text-slate-400" />
                  <div>
                    <div className="text-[10px] text-slate-500 uppercase">File Size</div>
                    <div className="text-xs font-bold text-slate-200 font-mono">{sizeMb} MB</div>
                  </div>
                </div>

                <div className="bg-slate-900/60 p-2.5 rounded-xl border border-slate-800 flex items-center gap-2">
                  <Hash className="w-3.5 h-3.5 text-slate-400" />
                  <div>
                    <div className="text-[10px] text-slate-500 uppercase">Format</div>
                    <div className="text-xs font-bold text-slate-200 uppercase font-mono">{video.format}</div>
                  </div>
                </div>
              </div>

              <div>
                <span className="text-[10px] font-bold uppercase text-slate-500 tracking-wider">SHA-256 Hash</span>
                <p className="text-[10px] font-mono text-slate-400 break-all bg-slate-950 p-2 rounded-lg border border-slate-800/80 mt-1">
                  {video.fileHash}
                </p>
              </div>
            </div>

            <div className="pt-4 border-t border-slate-800/80 flex items-center justify-between text-xs">
              <span className="text-slate-500">Status:</span>
              <span className="font-bold text-emerald-400 bg-emerald-500/10 px-2 py-0.5 rounded-full border border-emerald-500/20">
                {video.status}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
