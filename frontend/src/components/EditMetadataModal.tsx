import React, { useState } from 'react';
import { X, Sparkles, Save, Check } from 'lucide-react';
import { VideoDetail } from '../types';
import { updateVideoMetadata } from '../services/api';

interface EditMetadataModalProps {
  video: VideoDetail | null;
  onClose: () => void;
  onSaved: () => void;
}

export const EditMetadataModal: React.FC<EditMetadataModalProps> = ({ video, onClose, onSaved }) => {
  if (!video) return null;

  const [title, setTitle] = useState(video.title || video.filename);
  const [description, setDescription] = useState(video.description || '');
  const [tags, setTags] = useState(video.tags || '');
  const [hashtags, setHashtags] = useState(video.hashtags || '');
  const [privacyStatus, setPrivacyStatus] = useState(video.privacyStatus || 'public');
  const [isSaving, setIsSaving] = useState(false);
  const [savedSuccess, setSavedSuccess] = useState(false);

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSaving(true);
    try {
      await updateVideoMetadata(video.id, {
        title,
        description,
        tags,
        hashtags,
        privacyStatus,
      });
      setSavedSuccess(true);
      setTimeout(() => {
        onSaved();
        onClose();
      }, 700);
    } catch (err) {
      alert('Failed to update video metadata.');
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="bg-dark-900 border border-slate-800 rounded-3xl max-w-2xl w-full overflow-hidden shadow-2xl flex flex-col">
        {/* Header */}
        <div className="px-6 py-4 border-b border-slate-800 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-lg bg-indigo-500/20 text-indigo-400 flex items-center justify-center">
              <Sparkles className="w-4 h-4" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-white">Edit YouTube Publishing Metadata</h3>
              <p className="text-[11px] text-slate-400 font-mono truncate max-w-sm">{video.filename}</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="w-8 h-8 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-400 hover:text-white flex items-center justify-center transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSave} className="p-6 space-y-4">
          <div>
            <div className="flex justify-between items-center mb-1">
              <label className="text-xs font-bold text-slate-300">Shorts Title</label>
              <span className={`text-[10px] font-mono ${title.length > 95 ? 'text-red-400' : 'text-slate-500'}`}>
                {title.length}/100 chars
              </span>
            </div>
            <input
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              required
              maxLength={100}
              className="w-full px-3.5 py-2.5 rounded-xl bg-slate-950 border border-slate-800 text-white text-xs focus:outline-none focus:border-red-500 font-medium"
              placeholder="Catchy YouTube Shorts title with emoji #Shorts"
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-300 mb-1">Description</label>
            <textarea
              rows={5}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              className="w-full px-3.5 py-2.5 rounded-xl bg-slate-950 border border-slate-800 text-slate-200 text-xs focus:outline-none focus:border-red-500 font-mono"
              placeholder="Detailed description, engagement CTA, and credits..."
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-bold text-slate-300 mb-1">Hashtags</label>
              <input
                type="text"
                value={hashtags}
                onChange={(e) => setHashtags(e.target.value)}
                className="w-full px-3.5 py-2.5 rounded-xl bg-slate-950 border border-slate-800 text-slate-200 text-xs focus:outline-none focus:border-red-500 font-mono"
                placeholder="#Shorts #GTA6 #Gaming #Viral"
              />
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-300 mb-1">Privacy Visibility</label>
              <select
                value={privacyStatus}
                onChange={(e) => setPrivacyStatus(e.target.value)}
                className="w-full px-3.5 py-2.5 rounded-xl bg-slate-950 border border-slate-800 text-slate-200 text-xs focus:outline-none focus:border-red-500"
              >
                <option value="public">Public (Immediate reach)</option>
                <option value="unlisted">Unlisted (Shareable link)</option>
                <option value="private">Private (Channel owner only)</option>
              </select>
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-300 mb-1">Tags (Comma-separated)</label>
            <input
              type="text"
              value={tags}
              onChange={(e) => setTags(e.target.value)}
              className="w-full px-3.5 py-2.5 rounded-xl bg-slate-950 border border-slate-800 text-slate-200 text-xs focus:outline-none focus:border-red-500 font-mono"
              placeholder="shorts, gaming, viral, trending, gta 6"
            />
          </div>

          {/* Footer Actions */}
          <div className="pt-4 border-t border-slate-800 flex items-center justify-end gap-3">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 rounded-xl text-xs font-semibold bg-slate-800 hover:bg-slate-700 text-slate-300 transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSaving}
              className="flex items-center gap-2 px-5 py-2 rounded-xl text-xs font-bold bg-red-600 hover:bg-red-500 text-white shadow-lg shadow-red-600/20 transition-all disabled:opacity-50"
            >
              {savedSuccess ? (
                <>
                  <Check className="w-4 h-4" />
                  <span>Saved!</span>
                </>
              ) : (
                <>
                  <Save className="w-4 h-4" />
                  <span>{isSaving ? 'Saving...' : 'Save Metadata'}</span>
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
