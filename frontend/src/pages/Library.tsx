import React, { useState, useRef } from 'react';
import { 
  Film, 
  Search, 
  Play, 
  Edit3, 
  Clock, 
  HardDrive, 
  Maximize2, 
  Sparkles, 
  CheckCircle2, 
  AlertCircle,
  Filter,
  ArrowUpDown,
  UploadCloud,
  Loader2
} from 'lucide-react';
import { VideoDetail } from '../types';
import { uploadVideosApi } from '../services/api';

interface LibraryProps {
  videos: VideoDetail[];
  onPreview: (video: VideoDetail) => void;
  onEditMetadata: (video: VideoDetail) => void;
  onRefresh?: () => void;
}

export const Library: React.FC<LibraryProps> = ({ videos, onPreview, onEditMetadata, onRefresh }) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [sortBy, setSortBy] = useState<'date' | 'title' | 'duration' | 'size'>('date');
  const [isUploading, setIsUploading] = useState(false);
  const [uploadMessage, setUploadMessage] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (!files || files.length === 0) return;
    setIsUploading(true);
    setUploadMessage(null);
    try {
      const res = await uploadVideosApi(Array.from(files));
      setUploadMessage(res.message || 'Videos uploaded successfully!');
      if (onRefresh) onRefresh();
    } catch (err: any) {
      setUploadMessage('Upload failed: ' + (err.response?.data?.message || err.message));
    } finally {
      setIsUploading(false);
      if (fileInputRef.current) fileInputRef.current.value = '';
      setTimeout(() => setUploadMessage(null), 5000);
    }
  };

  const filtered = videos
    .filter((v) => {
      const matchSearch =
        v.filename.toLowerCase().includes(searchTerm.toLowerCase()) ||
        (v.title && v.title.toLowerCase().includes(searchTerm.toLowerCase()));
      const matchStatus = statusFilter === 'ALL' || v.status === statusFilter;
      return matchSearch && matchStatus;
    })
    .sort((a, b) => {
      if (sortBy === 'title') {
        return (a.title || a.filename).localeCompare(b.title || b.filename);
      }
      if (sortBy === 'duration') {
        return b.duration - a.duration;
      }
      if (sortBy === 'size') {
        return b.fileSize - a.fileSize;
      }
      return new Date(b.detectedAt).getTime() - new Date(a.detectedAt).getTime();
    });

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'PUBLISHED':
        return <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">PUBLISHED</span>;
      case 'QUEUED':
        return <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">QUEUED</span>;
      case 'UPLOADING':
        return <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-500/10 text-amber-400 border border-amber-500/20 animate-pulse">UPLOADING</span>;
      case 'FAILED':
        return <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-red-500/10 text-red-400 border border-red-500/20">FAILED</span>;
      case 'RETRY_PENDING':
        return <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-purple-500/10 text-purple-400 border border-purple-500/20">RETRY PENDING</span>;
      default:
        return <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-slate-800 text-slate-400 border border-slate-700">{status}</span>;
    }
  };

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* Control Toolbar */}
      <div className="glass-card p-4 rounded-2xl flex flex-col md:flex-row md:items-center justify-between gap-4">
        {/* Search */}
        <div className="relative flex-1 max-w-md">
          <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Search by title or filename..."
            className="w-full pl-10 pr-4 py-2 rounded-xl bg-slate-950 border border-slate-800 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-red-500"
          />
        </div>

        {/* Filters */}
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-2">
            <Filter className="w-3.5 h-3.5 text-slate-400" />
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="px-3 py-2 rounded-xl bg-slate-950 border border-slate-800 text-xs text-slate-300 focus:outline-none focus:border-red-500 font-medium"
            >
              <option value="ALL">All Statuses</option>
              <option value="QUEUED">Queued</option>
              <option value="PUBLISHED">Published</option>
              <option value="FAILED">Failed</option>
              <option value="RETRY_PENDING">Retry Pending</option>
              <option value="SKIPPED">Skipped</option>
            </select>
          </div>

          <div className="flex items-center gap-2">
            <ArrowUpDown className="w-3.5 h-3.5 text-slate-400" />
            <select
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value as any)}
              className="px-3 py-2 rounded-xl bg-slate-950 border border-slate-800 text-xs text-slate-300 focus:outline-none focus:border-red-500 font-medium"
            >
              <option value="date">Sort by Detected Date</option>
              <option value="title">Sort by Title</option>
              <option value="duration">Sort by Duration</option>
              <option value="size">Sort by File Size</option>
            </select>
          </div>

          <input
            type="file"
            ref={fileInputRef}
            onChange={handleFileChange}
            multiple
            accept="video/mp4,video/mov,video/quicktime,video/webm,video/x-matroska"
            className="hidden"
          />

          <button
            onClick={() => fileInputRef.current?.click()}
            disabled={isUploading}
            className="flex items-center gap-2 px-4 py-2 rounded-xl bg-gradient-to-r from-red-600 to-rose-600 hover:from-red-500 hover:to-rose-500 text-white text-xs font-semibold shadow-lg shadow-red-600/20 active:scale-95 transition-all disabled:opacity-50"
          >
            {isUploading ? (
              <>
                <Loader2 className="w-4 h-4 animate-spin" />
                <span>Uploading...</span>
              </>
            ) : (
              <>
                <UploadCloud className="w-4 h-4" />
                <span>Upload Clips</span>
              </>
            )}
          </button>
        </div>
      </div>

      {uploadMessage && (
        <div className="p-3.5 rounded-xl bg-slate-900/90 border border-slate-800 text-xs text-slate-200 flex items-center justify-between animate-in fade-in">
          <span>{uploadMessage}</span>
          <button onClick={() => setUploadMessage(null)} className="text-slate-400 hover:text-white text-xs">✕</button>
        </div>
      )}

      {/* Video Cards Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {filtered.map((video) => {
          const sizeMb = (video.fileSize / (1024 * 1024)).toFixed(2);
          return (
            <div
              key={video.id}
              className="glass-card rounded-2xl overflow-hidden border border-slate-800/80 hover:border-slate-700 transition-all flex flex-col justify-between group"
            >
              {/* Card Header & Preview Box */}
              <div>
                <div className="relative bg-slate-950 h-40 flex items-center justify-center border-b border-slate-800/80 group-hover:bg-slate-900/60 transition-colors">
                  <div className="w-12 h-12 rounded-2xl bg-red-600/20 text-red-500 flex items-center justify-center shadow-lg group-hover:scale-110 transition-transform">
                    <Film className="w-6 h-6" />
                  </div>
                  
                  {/* Play Overlay Button */}
                  <button
                    onClick={() => onPreview(video)}
                    className="absolute inset-0 flex items-center justify-center bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity backdrop-blur-xs"
                    title="Play Preview"
                  >
                    <div className="w-12 h-12 rounded-full bg-red-600 text-white flex items-center justify-center shadow-xl shadow-red-600/40">
                      <Play className="w-5 h-5 fill-white ml-0.5" />
                    </div>
                  </button>

                  {/* Top Badges */}
                  <div className="absolute top-3 left-3 flex items-center gap-1.5">
                    {getStatusBadge(video.status)}
                    {video.aiGenerated && (
                      <span className="flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-bold bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
                        <Sparkles className="w-2.5 h-2.5" />
                        AI SEO
                      </span>
                    )}
                  </div>

                  <div className="absolute bottom-3 right-3 px-2 py-0.5 rounded-md bg-black/70 backdrop-blur-sm text-[10px] font-mono text-slate-300 border border-slate-800">
                    {video.duration}s • {video.resolution}
                  </div>
                </div>

                {/* Content */}
                <div className="p-4 space-y-2">
                  <h4 className="text-xs font-bold text-white line-clamp-2 leading-relaxed">
                    {video.title || video.filename}
                  </h4>
                  <p className="text-[11px] font-mono text-slate-400 truncate">
                    {video.filename}
                  </p>

                  <div className="flex items-center gap-3 pt-2 text-[11px] text-slate-500 font-mono">
                    <span className="flex items-center gap-1">
                      <HardDrive className="w-3 h-3" />
                      {sizeMb} MB
                    </span>
                    <span>•</span>
                    <span className="uppercase">{video.format}</span>
                    <span>•</span>
                    <span>{new Date(video.detectedAt).toLocaleDateString()}</span>
                  </div>
                </div>
              </div>

              {/* Action Buttons */}
              <div className="p-3 border-t border-slate-800/80 bg-slate-900/40 flex items-center justify-between gap-2">
                <button
                  onClick={() => onPreview(video)}
                  className="flex-1 py-1.5 px-3 rounded-xl text-xs font-semibold bg-slate-800 hover:bg-slate-700 text-slate-200 transition-colors flex items-center justify-center gap-1.5"
                >
                  <Play className="w-3 h-3" />
                  <span>Preview</span>
                </button>
                <button
                  onClick={() => onEditMetadata(video)}
                  className="flex-1 py-1.5 px-3 rounded-xl text-xs font-semibold bg-slate-800 hover:bg-slate-700 text-slate-200 transition-colors flex items-center justify-center gap-1.5"
                >
                  <Edit3 className="w-3 h-3" />
                  <span>Edit SEO</span>
                </button>
              </div>
            </div>
          );
        })}
      </div>

      {filtered.length === 0 && (
        <div className="glass-card rounded-2xl p-12 text-center text-slate-500 text-xs">
          No video files matching your criteria.
        </div>
      )}
    </div>
  );
};
