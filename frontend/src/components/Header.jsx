import React, { useState, useEffect } from 'react';
import { User, LogOut, ChevronDown, BarChart2, History, CreditCard, Settings, Star } from 'lucide-react';

const Header = ({ user, onLogout, onQuickSend, activeTab, setActiveTab, token }) => {
  const [menuOpen, setMenuOpen] = useState(false);
  const [omniPoints, setOmniPoints] = useState(null);

  const userName = user?.name || user?.email?.split('@')[0] || 'angel';
  const initial = userName.charAt(0).toUpperCase();

  useEffect(() => {
    if (!token) return;
    fetch('http://localhost:8080/api/v1/rewards/balance', {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then(res => (res.ok ? res.json() : null))
      .then(data => { if (data) setOmniPoints(data.points); })
      .catch(() => {});
  }, [token]);

  const quickActions = [
    { label: '🍕 Order Food', prompt: 'I want to use Zomato for order food' },
    { label: '🚕 Book Cab', prompt: 'Book a cab for me' },
    { label: '🏍 Bike Ride', prompt: 'Book a bike ride' },
    { label: '📦 Shop Now', prompt: 'Buy something on Amazon' },
    { label: '⚖ Compare', prompt: 'Compare prices for me' },
    { label: '📍 Track Order', prompt: 'Track my order' },
    { label: '❓ Help', prompt: 'Help' },
  ];

  return (
    <header className="h-14 bg-[#0a0d14] border-b border-white/5 px-4 flex items-center justify-between shrink-0 select-none z-20">
      {/* Left Logo */}
      <div className="flex items-center gap-2 cursor-pointer" onClick={() => setActiveTab?.('chat')}>
        <div className="w-7 h-7 rounded-lg bg-cyan-500/20 border border-cyan-500/30 flex items-center justify-center text-cyan-400 font-bold text-sm">
          O
        </div>
        <span className="font-bold text-cyan-400 text-base tracking-wide">OmniBot</span>
      </div>

      {/* Quick Actions Pills */}
      <div className="hidden md:flex items-center gap-1.5 overflow-x-auto no-scrollbar max-w-2xl py-1">
        {quickActions.map((qa, i) => (
          <button
            key={i}
            onClick={() => onQuickSend?.(qa.prompt)}
            className="px-3 py-1 rounded-full bg-white/[0.04] hover:bg-white/[0.08] border border-white/10 text-gray-300 hover:text-white text-xs font-medium whitespace-nowrap transition-all"
          >
            {qa.label}
          </button>
        ))}
      </div>

      {/* Right User Profile */}
      <div className="relative">
        <div className="flex items-center gap-3">
          {omniPoints !== null && (
            <div
              className="hidden sm:flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-amber-500/10 border border-amber-500/25 text-amber-300 text-xs font-medium"
              title="OmniPoints — earn on any order, redeem on any vendor"
            >
              <Star size={12} className="fill-amber-400 text-amber-400" />
              <span>{omniPoints.toLocaleString()} OmniPoints</span>
            </div>
          )}
          <button 
            onClick={() => setMenuOpen(!menuOpen)}
            className="flex items-center gap-2 px-2.5 py-1 rounded-xl bg-white/[0.03] hover:bg-white/[0.08] border border-white/5 transition-all text-xs"
          >
            <div className="w-6 h-6 rounded-full bg-indigo-600 text-white font-bold flex items-center justify-center text-xs">
              {initial}
            </div>
            <span className="text-gray-200 font-medium">{userName}</span>
            <ChevronDown size={14} className="text-gray-400" />
          </button>

          <button
            onClick={onLogout}
            className="px-2.5 py-1 rounded-lg border border-white/10 hover:border-red-500/50 hover:bg-red-500/10 text-gray-400 hover:text-red-400 text-xs transition-all flex items-center gap-1"
          >
            <LogOut size={12} />
            <span>Sign out</span>
          </button>
        </div>

        {/* User Dropdown for auxiliary views */}
        {menuOpen && (
          <div className="absolute right-0 mt-2 w-48 bg-[#121820] border border-white/10 rounded-xl shadow-xl py-1 z-50 text-xs text-gray-300">
            <button
              onClick={() => { setActiveTab?.('chat'); setMenuOpen(false); }}
              className={`w-full px-3 py-2 text-left flex items-center gap-2 hover:bg-white/5 ${activeTab === 'chat' ? 'text-cyan-400 font-bold' : ''}`}
            >
              💬 Chat Assistant
            </button>
            <button
              onClick={() => { setActiveTab?.('analytics'); setMenuOpen(false); }}
              className={`w-full px-3 py-2 text-left flex items-center gap-2 hover:bg-white/5 ${activeTab === 'analytics' ? 'text-cyan-400 font-bold' : ''}`}
            >
              <BarChart2 size={14} /> Analytics & Savings
            </button>
            <button
              onClick={() => { setActiveTab?.('history'); setMenuOpen(false); }}
              className={`w-full px-3 py-2 text-left flex items-center gap-2 hover:bg-white/5 ${activeTab === 'history' ? 'text-cyan-400 font-bold' : ''}`}
            >
              <History size={14} /> Order History
            </button>
            <button
              onClick={() => { setActiveTab?.('profile'); setMenuOpen(false); }}
              className={`w-full px-3 py-2 text-left flex items-center gap-2 hover:bg-white/5 ${activeTab === 'profile' ? 'text-cyan-400 font-bold' : ''}`}
            >
              <CreditCard size={14} /> Connected Accounts
            </button>
          </div>
        )}
      </div>
    </header>
  );
};

export default Header;
