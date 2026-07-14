import React, { useState } from 'react';
import { motion } from 'framer-motion';
import {
  Bell, Moon, Sun, Globe, Zap, Shield,
  Sliders, ChevronRight, Volume2, Smartphone,
  RefreshCw, Trash2, LogOut
} from 'lucide-react';

/* ── Toggle component ──────────────────────────────────────── */
function Toggle({ enabled, onChange }) {
  return (
    <button onClick={() => onChange(!enabled)}
      className={`relative w-11 h-6 rounded-full transition-colors duration-200 focus:outline-none
        ${enabled ? 'bg-indigo-600' : 'bg-gray-200'}`}>
      <motion.span layout
        className="absolute top-0.5 left-0.5 w-5 h-5 rounded-full bg-white shadow-sm"
        animate={{ x: enabled ? 20 : 0 }}
        transition={{ type: 'spring', stiffness: 500, damping: 35 }}
      />
    </button>
  );
}

/* ── Setting row ───────────────────────────────────────────── */
function Row({ icon: Icon, color, label, sub, right }) {
  return (
    <div className="flex items-center gap-3 py-3 px-1">
      <div className={`w-9 h-9 rounded-xl flex items-center justify-center shrink-0 ${color}`}>
        <Icon size={16} className="text-white" />
      </div>
      <div className="flex-grow min-w-0">
        <p className="text-sm font-medium text-gray-800">{label}</p>
        {sub && <p className="text-xs text-gray-400 mt-0.5">{sub}</p>}
      </div>
      <div className="shrink-0">{right}</div>
    </div>
  );
}

/* ── Section card ──────────────────────────────────────────── */
function Card({ title, children }) {
  return (
    <section className="bg-white rounded-2xl shadow-sm border border-gray-100 p-5">
      <h2 className="text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">{title}</h2>
      <div className="divide-y divide-gray-50">{children}</div>
    </section>
  );
}

/* ── Main ──────────────────────────────────────────────────── */
export default function AppSettings({ onLogout }) {
  const [cfg, setCfg] = useState({
    darkMode:          false,
    notifications:     true,
    priceAlerts:       true,
    soundEffects:      false,
    biometricAuth:     true,
    autoRefresh:       true,
    shareAnalytics:    false,
  });

  const [currency, setCurrency] = useState('INR ₹');
  const [lang,     setLang]     = useState('English');
  const [vendor,   setVendor]   = useState('mock');

  const set = key => val => setCfg(c => ({ ...c, [key]: val }));

  return (
    <div className="flex flex-col h-full bg-gray-50 overflow-y-auto">

      {/* ── Header ──────────────────────────────────────────── */}
      <div className="bg-white border-b border-gray-100 px-8 py-7">
        <h1 className="text-2xl font-bold text-gray-800">Settings</h1>
        <p className="text-sm text-gray-400 mt-1">Customise your OmniBot experience.</p>
      </div>

      <div className="px-6 py-6 space-y-4">

        {/* Appearance */}
        <Card title="Appearance">
          <Row icon={cfg.darkMode ? Moon : Sun}
            color={cfg.darkMode ? 'bg-slate-700' : 'bg-amber-400'}
            label="Dark Mode" sub="Switch between light and dark themes"
            right={<Toggle enabled={cfg.darkMode} onChange={set('darkMode')} />}
          />
          <Row icon={Globe} color="bg-indigo-500"
            label="Language" sub="App interface language"
            right={
              <select value={lang} onChange={e => setLang(e.target.value)}
                className="text-sm text-gray-700 bg-gray-50 border border-gray-200 rounded-xl px-3 py-1.5 focus:outline-none focus:ring-2 focus:ring-indigo-300">
                {['English','हिंदी','Tamil','Telugu','Kannada'].map(l =>
                  <option key={l}>{l}</option>)}
              </select>
            }
          />
          <Row icon={Sliders} color="bg-purple-500"
            label="Currency" sub="Display prices in your preferred currency"
            right={
              <select value={currency} onChange={e => setCurrency(e.target.value)}
                className="text-sm text-gray-700 bg-gray-50 border border-gray-200 rounded-xl px-3 py-1.5 focus:outline-none focus:ring-2 focus:ring-indigo-300">
                {['INR ₹','USD $','EUR €','GBP £'].map(c =>
                  <option key={c}>{c}</option>)}
              </select>
            }
          />
        </Card>

        {/* Notifications */}
        <Card title="Notifications">
          <Row icon={Bell} color="bg-rose-500"
            label="Push Notifications" sub="Order updates, deals, and alerts"
            right={<Toggle enabled={cfg.notifications} onChange={set('notifications')} />}
          />
          <Row icon={Zap} color="bg-yellow-500"
            label="Price Drop Alerts" sub="Get notified when prices fall across vendors"
            right={<Toggle enabled={cfg.priceAlerts} onChange={set('priceAlerts')} />}
          />
          <Row icon={Volume2} color="bg-green-500"
            label="Sound Effects" sub="Play sounds for chat messages and order events"
            right={<Toggle enabled={cfg.soundEffects} onChange={set('soundEffects')} />}
          />
        </Card>

        {/* Privacy & Security */}
        <Card title="Privacy & Security">
          <Row icon={Shield} color="bg-emerald-600"
            label="Biometric Auth" sub="Use fingerprint or Face ID to unlock"
            right={<Toggle enabled={cfg.biometricAuth} onChange={set('biometricAuth')} />}
          />
          <Row icon={RefreshCw} color="bg-blue-500"
            label="Auto-refresh Data" sub="Keep vendor prices & availability live"
            right={<Toggle enabled={cfg.autoRefresh} onChange={set('autoRefresh')} />}
          />
          <Row icon={Smartphone} color="bg-gray-600"
            label="Share Analytics" sub="Help us improve with anonymous usage data"
            right={<Toggle enabled={cfg.shareAnalytics} onChange={set('shareAnalytics')} />}
          />
        </Card>

        {/* Vendor Mode */}
        <Card title="Vendor API Mode">
          <div className="py-3 px-1">
            <p className="text-sm font-medium text-gray-800 mb-1">Data Source</p>
            <p className="text-xs text-gray-400 mb-3">
              Mock mode uses simulated data. Live mode requires valid API keys in application.properties.
            </p>
            <div className="flex gap-2">
              {['mock','live'].map(m => (
                <button key={m} onClick={() => setVendor(m)}
                  className={`flex-1 py-2.5 rounded-xl text-sm font-bold border transition-all ${
                    vendor === m
                      ? m === 'live'
                        ? 'bg-emerald-600 text-white border-emerald-600 shadow'
                        : 'bg-indigo-600 text-white border-indigo-600 shadow'
                      : 'bg-gray-50 text-gray-400 border-gray-200 hover:bg-gray-100'
                  }`}>
                  {m === 'mock' ? '🧪 Mock' : '⚡ Live'}
                </button>
              ))}
            </div>
            {vendor === 'live' && (
              <p className="text-[11px] text-orange-500 mt-2 flex items-center gap-1">
                ⚠ Live mode requires API keys set in <code className="font-mono bg-orange-50 px-1 rounded">application.properties</code>
              </p>
            )}
          </div>
        </Card>

        {/* Danger Zone */}
        <Card title="Account">
          <button className="w-full flex items-center gap-3 py-3 px-1 text-left group">
            <div className="w-9 h-9 rounded-xl bg-gray-100 flex items-center justify-center shrink-0 group-hover:bg-red-50 transition-colors">
              <Trash2 size={16} className="text-gray-400 group-hover:text-red-500 transition-colors" />
            </div>
            <div className="flex-grow">
              <p className="text-sm font-medium text-gray-700 group-hover:text-red-600 transition-colors">Clear Chat History</p>
              <p className="text-xs text-gray-400">Remove all conversations from this device</p>
            </div>
          </button>

          <button onClick={onLogout}
            className="w-full flex items-center gap-3 py-3 px-1 text-left group">
            <div className="w-9 h-9 rounded-xl bg-gray-100 flex items-center justify-center shrink-0 group-hover:bg-red-50 transition-colors">
              <LogOut size={16} className="text-gray-400 group-hover:text-red-500 transition-colors" />
            </div>
            <div className="flex-grow">
              <p className="text-sm font-medium text-gray-700 group-hover:text-red-600 transition-colors">Sign Out</p>
              <p className="text-xs text-gray-400">Securely end your current session</p>
            </div>
          </button>
        </Card>

        <p className="text-center text-xs text-gray-300 pb-2">OmniBot Platform v1.0.0 · Phase 3</p>

      </div>
    </div>
  );
}
