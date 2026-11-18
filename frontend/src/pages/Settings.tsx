import React, { useState, useEffect } from 'react';
import { 
  Settings as SettingsIcon, 
  Youtube, 
  Sparkles, 
  FolderOpen, 
  Save, 
  Check, 
  ExternalLink, 
  AlertCircle,
  ToggleLeft,
  ToggleRight,
  RefreshCw,
  Sliders
} from 'lucide-react';
import { Settings } from '../types';
import { getYouTubeAuthUrl, disconnectYouTube, toggleSimulationMode } from '../services/api';

interface SettingsProps {
  settings: Settings | null;
  onUpdateSettings: (settings: Partial<Settings>) => Promise<any> | void;
  onScanNow: () => void;
  isScanning: boolean;
  channelConnected: boolean;
  channelTitle?: string;
}

export const SettingsPage: React.FC<SettingsProps> = ({
  settings,
  onUpdateSettings,
  onScanNow,
  isScanning,
  channelConnected,
  channelTitle,
}) => {
  const isLocalHost = typeof window !== 'undefined' && (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1');
  const dynamicRedirectUri = !isLocalHost && typeof window !== 'undefined'
    ? `${window.location.origin}/api/youtube/oauth2/callback`
    : 'http://localhost:8080/api/youtube/oauth2/callback';

  const [monitoredFolder, setMonitoredFolder] = useState(
    settings?.monitoredFolder || 'C:\\Users\\vijay\\OneDrive\\Desktop\\Youtube Agent\\Clips'
  );
  const [geminiApiKey, setGeminiApiKey] = useState(settings?.geminiApiKey || '');
  const [youtubeClientId, setYoutubeClientId] = useState(settings?.youtubeClientId || '');
  const [youtubeClientSecret, setYoutubeClientSecret] = useState(settings?.youtubeClientSecret || '');
  const [youtubeRedirectUri, setYoutubeRedirectUri] = useState(
    settings?.youtubeRedirectUri && (!isLocalHost ? !settings.youtubeRedirectUri.includes('localhost') : true)
      ? settings.youtubeRedirectUri
      : dynamicRedirectUri
  );
  const [defaultDescriptionTemplate, setDefaultDescriptionTemplate] = useState(
    settings?.defaultDescriptionTemplate ||
      'Enjoy this viral clip! 🔥\n\nSubscribe for more amazing videos.\n\n#gta6 #rockstargames #gamingnews #gtavonline #gtacommunity #gtacars #viralata'
  );
  const [defaultTags, setDefaultTags] = useState(
    settings?.defaultTags || 'gta 6, rockstargames, gamingnews, gtavonline, gtacommunity, gtacars, viralata, shorts, viral'
  );
  const [simulateUpload, setSimulateUpload] = useState(settings?.simulateUpload ?? false);
  const [autoDeleteOnPublish, setAutoDeleteOnPublish] = useState(settings?.autoDeleteOnPublish ?? true);

  useEffect(() => {
    if (settings) {
      if (settings.monitoredFolder) setMonitoredFolder(settings.monitoredFolder);
      if (settings.geminiApiKey) setGeminiApiKey(settings.geminiApiKey);
      if (settings.youtubeClientId) setYoutubeClientId(settings.youtubeClientId);
      if (settings.youtubeClientSecret) setYoutubeClientSecret(settings.youtubeClientSecret);
      if (settings.youtubeRedirectUri) {
        setYoutubeRedirectUri(!isLocalHost && settings.youtubeRedirectUri.includes('localhost') ? dynamicRedirectUri : settings.youtubeRedirectUri);
      } else {
        setYoutubeRedirectUri(dynamicRedirectUri);
      }
      if (settings.defaultDescriptionTemplate) {
        setDefaultDescriptionTemplate(settings.defaultDescriptionTemplate);
      } else {
        setDefaultDescriptionTemplate('Enjoy this viral clip! 🔥\n\nSubscribe for more amazing videos.\n\n#gta6 #rockstargames #gamingnews #gtavonline #gtacommunity #gtacars #viralata');
      }
      if (settings.defaultTags) setDefaultTags(settings.defaultTags);
      if (settings.simulateUpload !== undefined) setSimulateUpload(settings.simulateUpload);
      if (settings.autoDeleteOnPublish !== undefined) setAutoDeleteOnPublish(settings.autoDeleteOnPublish);
    }
  }, [settings]);

  const [isSaving, setIsSaving] = useState(false);
  const [saveSuccess, setSaveSuccess] = useState(false);
  const [isConnecting, setIsConnecting] = useState(false);

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    setIsSaving(true);
    onUpdateSettings({
      monitoredFolder,
      geminiApiKey,
      youtubeClientId,
      youtubeClientSecret,
      youtubeRedirectUri,
      defaultDescriptionTemplate,
      defaultTags,
      simulateUpload,
      autoDeleteOnPublish,
    });
    setSaveSuccess(true);
    setTimeout(() => {
      setIsSaving(false);
      setSaveSuccess(false);
    }, 1500);
  };

  const handleConnectYouTube = async () => {
    try {
      setIsConnecting(true);
      // Save credentials first and await persistence
      await onUpdateSettings({
        youtubeClientId,
        youtubeClientSecret,
        youtubeRedirectUri,
      });

      const authUrl = await getYouTubeAuthUrl();
      window.location.href = authUrl;
    } catch (err: any) {
      alert(err.response?.data?.error || 'Please enter your Google OAuth Client ID and Secret first.');
      setIsConnecting(false);
    }
  };

  const handleDisconnect = async () => {
    if (confirm('Are you sure you want to disconnect this YouTube channel?')) {
      await disconnectYouTube();
      window.location.reload();
    }
  };

  const handleToggleSimulation = async (val: boolean) => {
    setSimulateUpload(val);
    await toggleSimulationMode(val);
  };

  return (
    <div className="space-y-8 animate-in fade-in duration-300 max-w-5xl">
      <form onSubmit={handleSave} className="space-y-8">
        {/* 1. YouTube Channel Connection */}
        <div className="glass-card rounded-3xl p-6 border-red-500/20">
          <div className="flex items-center justify-between pb-4 border-b border-slate-800">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-2xl bg-red-600/15 text-red-500 flex items-center justify-center">
                <Youtube className="w-5 h-5 fill-red-500" />
              </div>
              <div>
                <h3 className="text-sm font-bold text-white">YouTube Channel & Google OAuth 2.0</h3>
                <p className="text-xs text-slate-400">Official YouTube Data API v3 authentication and channel linking</p>
              </div>
            </div>

            {channelConnected ? (
              <div className="flex items-center gap-3">
                <span className="text-xs font-semibold text-emerald-400 bg-emerald-500/10 px-3 py-1 rounded-full border border-emerald-500/20">
                  Linked: {channelTitle}
                </span>
                <button
                  type="button"
                  onClick={handleDisconnect}
                  className="px-3 py-1.5 rounded-xl text-xs font-semibold bg-red-500/10 text-red-400 hover:bg-red-500/20 border border-red-500/20 transition-colors"
                >
                  Disconnect
                </button>
              </div>
            ) : (
              <button
                type="button"
                onClick={handleConnectYouTube}
                disabled={isConnecting}
                className="px-4 py-2 rounded-xl text-xs font-bold bg-red-600 hover:bg-red-500 text-white shadow-lg shadow-red-600/25 transition-all flex items-center gap-2"
              >
                <Youtube className="w-4 h-4 fill-white" />
                <span>{isConnecting ? 'Redirecting...' : 'Connect YouTube Channel'}</span>
              </button>
            )}
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-6">
            <div>
              <label className="block text-xs font-bold text-slate-300 mb-1">
                Google OAuth Client ID
              </label>
              <input
                type="text"
                value={youtubeClientId}
                onChange={(e) => setYoutubeClientId(e.target.value)}
                placeholder="e.g. 123456789-abcdefg.apps.googleusercontent.com"
                className="w-full px-3.5 py-2.5 rounded-xl bg-slate-950 border border-slate-800 text-white text-xs font-mono focus:outline-none focus:border-red-500"
              />
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-300 mb-1">
                Google OAuth Client Secret
              </label>
              <input
                type="password"
                value={youtubeClientSecret}
                onChange={(e) => setYoutubeClientSecret(e.target.value)}
                placeholder="GOCSPX-xxxxxxxxxxxxxxxx"
                className="w-full px-3.5 py-2.5 rounded-xl bg-slate-950 border border-slate-800 text-white text-xs font-mono focus:outline-none focus:border-red-500"
              />
            </div>

            <div className="md:col-span-2">
              <label className="block text-xs font-bold text-slate-300 mb-1">
                Authorized Redirect URI
              </label>
              <input
                type="text"
                value={youtubeRedirectUri}
                onChange={(e) => setYoutubeRedirectUri(e.target.value)}
                className="w-full px-3.5 py-2.5 rounded-xl bg-slate-950 border border-slate-800 text-slate-400 text-xs font-mono focus:outline-none focus:border-red-500"
              />
              <span className="text-[10px] text-slate-500 mt-1 block">
                Must match the Redirect URI in Google Cloud Console Credentials.
              </span>
            </div>
          </div>

          {/* Test / Simulation Mode Toggle */}
          <div className="mt-6 p-4 rounded-2xl bg-slate-900/60 border border-slate-800 flex items-center justify-between">
            <div>
              <div className="text-xs font-bold text-white flex items-center gap-2">
                <Sliders className="w-3.5 h-3.5 text-indigo-400" />
                <span>Upload Simulation & Pipeline Test Mode</span>
              </div>
              <p className="text-[11px] text-slate-400 mt-0.5">
                Allows testing the full pipeline, queue transitions, and duplicate prevention without calling live YouTube API.
              </p>
            </div>
            <button
              type="button"
              onClick={() => handleToggleSimulation(!simulateUpload)}
              className={`px-3 py-1.5 rounded-xl text-xs font-bold font-mono transition-colors border ${
                simulateUpload
                  ? 'bg-emerald-500/20 text-emerald-400 border-emerald-500/40'
                  : 'bg-slate-800 text-slate-400 border-slate-700'
              }`}
            >
              {simulateUpload ? 'SIMULATION ON' : 'LIVE API MODE'}
            </button>
          </div>
        </div>

        {/* 2. Video Folder Monitoring */}
        <div className="glass-card rounded-3xl p-6">
          <div className="flex items-center gap-3 pb-4 border-b border-slate-800">
            <div className="w-10 h-10 rounded-2xl bg-amber-500/15 text-amber-500 flex items-center justify-center">
              <FolderOpen className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-white">Monitored Video Folder</h3>
              <p className="text-xs text-slate-400">Continuous directory watch & SHA-256 detection</p>
            </div>
          </div>

          <div className="mt-6 space-y-4">
            <div>
              <label className="block text-xs font-bold text-slate-300 mb-1">
                Folder Absolute Path
              </label>
              <div className="flex gap-2">
                <input
                  type="text"
                  value={monitoredFolder}
                  onChange={(e) => setMonitoredFolder(e.target.value)}
                  className="flex-1 px-3.5 py-2.5 rounded-xl bg-slate-950 border border-slate-800 text-white text-xs font-mono focus:outline-none focus:border-red-500"
                />
                <button
                  type="button"
                  onClick={onScanNow}
                  disabled={isScanning}
                  className="px-4 py-2.5 rounded-xl text-xs font-semibold bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 transition-colors flex items-center gap-2"
                >
                  <RefreshCw className={`w-3.5 h-3.5 ${isScanning ? 'animate-spin' : ''}`} />
                  <span>Scan Folder</span>
                </button>
              </div>
              <span className="text-[10px] text-slate-500 mt-1 block">
                Supported video formats: MP4, MOV, MKV, WEBM. Currently tracking 19 finished Clips.
              </span>
            </div>

            {/* Cloud Storage Auto-Cleanup Toggle */}
            <div className="mt-4 p-4 rounded-2xl bg-slate-900/60 border border-slate-800 flex items-center justify-between">
              <div>
                <div className="text-xs font-bold text-white flex items-center gap-2">
                  <span>Auto-clean published video files from cloud disk</span>
                </div>
                <p className="text-[11px] text-slate-400 mt-0.5">
                  Automatically deletes raw .mp4 files from cloud disk after confirmed YouTube publication to prevent storage from filling up. Database records and SHA-256 duplicate protection remain intact permanently.
                </p>
              </div>
              <button
                type="button"
                onClick={() => setAutoDeleteOnPublish(!autoDeleteOnPublish)}
                className={`px-3 py-1.5 rounded-xl text-xs font-bold font-mono transition-colors border ${
                  autoDeleteOnPublish
                    ? 'bg-emerald-500/20 text-emerald-400 border-emerald-500/40'
                    : 'bg-slate-800 text-slate-400 border-slate-700'
                }`}
              >
                {autoDeleteOnPublish ? 'ENABLED (SAFE)' : 'DISABLED'}
              </button>
            </div>
          </div>
        </div>

        {/* 3. Gemini AI Configuration */}
        <div className="glass-card rounded-3xl p-6">
          <div className="flex items-center gap-3 pb-4 border-b border-slate-800">
            <div className="w-10 h-10 rounded-2xl bg-indigo-500/15 text-indigo-400 flex items-center justify-center">
              <Sparkles className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-white">Gemini API Configuration</h3>
              <p className="text-xs text-slate-400">High-converting viral title, description, and hashtags generation</p>
            </div>
          </div>

          <div className="mt-6 space-y-4">
            <div>
              <label className="block text-xs font-bold text-slate-300 mb-1">
                Google Gemini API Key
              </label>
              <input
                type="password"
                value={geminiApiKey}
                onChange={(e) => setGeminiApiKey(e.target.value)}
                placeholder="AIzaSy..."
                className="w-full px-3.5 py-2.5 rounded-xl bg-slate-950 border border-slate-800 text-white text-xs font-mono focus:outline-none focus:border-red-500"
              />
              <span className="text-[10px] text-slate-500 mt-1 block">
                Leave blank to use built-in smart algorithmic heuristic SEO generator.
              </span>
            </div>
          </div>
        </div>

        {/* 4. Default Description & Tags Templates */}
        <div className="glass-card rounded-3xl p-6">
          <h3 className="text-sm font-bold text-white mb-4">Default Descriptions & Default Tags</h3>

          <div className="space-y-4">
            <div>
              <label className="block text-xs font-bold text-slate-300 mb-1">
                Default Description Template
              </label>
              <textarea
                rows={4}
                value={defaultDescriptionTemplate}
                onChange={(e) => setDefaultDescriptionTemplate(e.target.value)}
                className="w-full px-3.5 py-2.5 rounded-xl bg-slate-950 border border-slate-800 text-slate-200 text-xs font-mono focus:outline-none focus:border-red-500"
              />
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-300 mb-1">
                Default Tags (Comma-separated)
              </label>
              <input
                type="text"
                value={defaultTags}
                onChange={(e) => setDefaultTags(e.target.value)}
                className="w-full px-3.5 py-2.5 rounded-xl bg-slate-950 border border-slate-800 text-slate-200 text-xs font-mono focus:outline-none focus:border-red-500"
              />
            </div>
          </div>
        </div>

        {/* Save Button */}
        <div className="flex justify-end">
          <button
            type="submit"
            disabled={isSaving}
            className="flex items-center gap-2 px-8 py-3 rounded-2xl text-xs font-bold bg-red-600 hover:bg-red-500 text-white shadow-xl shadow-red-600/25 transition-all disabled:opacity-50"
          >
            {saveSuccess ? (
              <>
                <Check className="w-4 h-4" />
                <span>All Settings Saved!</span>
              </>
            ) : (
              <>
                <Save className="w-4 h-4" />
                <span>Save All Settings</span>
              </>
            )}
          </button>
        </div>
      </form>
    </div>
  );
};
