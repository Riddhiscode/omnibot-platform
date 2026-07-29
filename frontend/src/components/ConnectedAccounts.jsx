import React, { useState } from 'react';
import { motion } from 'framer-motion';
import {
  User, Mail, Phone, MapPin, Edit3, Check,
  Link2, Zap, ShoppingBag, Car, Utensils,
  Shield, Award, Star, Camera
} from 'lucide-react';

/* ── Base vendors blueprint ───────────────────────────────── */
const BASE_VENDORS = [
  { id: 'zomato',     name: 'Zomato',     emoji: '🍕' },
  { id: 'swiggy',     name: 'Swiggy',     emoji: '🛵' },
  { id: 'uber',       name: 'Uber',       emoji: '🚗' },
  { id: 'ola',        name: 'Ola',        emoji: '🚕' },
  { id: 'amazon',     name: 'Amazon',     emoji: '📦' },
  { id: 'flipkart',   name: 'Flipkart',   emoji: '🛒' },
  { id: 'bigbasket',  name: 'BigBasket',  emoji: '🥦' },
  { id: 'jiomart',    name: 'JioMart',    emoji: '🏪' },
];

const BADGES = [
  { label: 'Power User',        emoji: '⚡', desc: '50+ orders placed'           },
  { label: 'Smart Saver',       emoji: '💰', desc: 'Saved ₹2,000+ via OmniBot'  },
  { label: 'Multi-Platform Pro',emoji: '🌐', desc: '5+ vendors connected'        },
];

export default function ConnectedAccounts({ user, token }) {
  const [editing, setEditing] = useState(false);
  const [vendors, setVendors] = useState(BASE_VENDORS.map(v => ({...v, status: 'not_linked', orders: 0})));
  const [profile, setProfile] = useState({
    name:    user?.fullName || 'Riddhi S.',
    email:   user?.email    || 'demo@omnibot.in',
    phone:   '+91 98765 43210',
    city:    'Bengaluru, Karnataka',
  });
  const [draft, setDraft] = useState({ ...profile });

  React.useEffect(() => {
    fetchAccounts();
  }, [token]);

  const fetchAccounts = async () => {
    try {
      const res = await fetch('http://localhost:8080/api/v1/accounts', {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      const data = await res.json();
      const mapped = BASE_VENDORS.map(base => {
        const found = data.find(d => d.vendorId === base.id);
        if (found) {
          return { ...base, status: found.status.toLowerCase(), orders: found.orderCount };
        }
        return { ...base, status: 'not_linked', orders: 0 };
      });
      setVendors(mapped);
    } catch (e) {
      console.error('Failed to fetch accounts:', e);
    }
  };

  const handleToggle = async (vendor) => {
    try {
      if (vendor.status === 'connected' || vendor.status === 'pending') {
        if (window.confirm(`Are you sure you want to disconnect ${vendor.name}?`)) {
          await fetch(`http://localhost:8080/api/v1/accounts/unlink/${vendor.id}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${token}` }
          });
          fetchAccounts();
        }
      } else {
        await fetch('http://localhost:8080/api/v1/accounts/link', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
          body: JSON.stringify({ vendorId: vendor.id })
        });
        fetchAccounts();
      }
    } catch (e) {
      console.error('Failed to toggle account:', e);
    }
  };

  const totalOrders = vendors.reduce((s, v) => s + v.orders, 0);
  const connected   = vendors.filter(v => v.status === 'connected').length;

  const save = () => { setProfile({ ...draft }); setEditing(false); };

  return (
    <div className="flex flex-col h-full bg-gray-50 overflow-y-auto">

      {/* ── Hero banner ────────────────────────────────────── */}
      <div className="bg-gradient-to-br from-slate-800 to-slate-900 px-8 pt-8 pb-12 text-white relative overflow-hidden">
        <div className="absolute inset-0 opacity-10"
          style={{ backgroundImage: 'radial-gradient(circle at 80% 20%, #6366f1 0%, transparent 50%)' }} />

        <div className="relative flex items-start gap-5">
          {/* Avatar */}
          <div className="relative shrink-0">
            <div className="w-20 h-20 rounded-2xl bg-gradient-to-tr from-indigo-500 to-purple-600 flex items-center justify-center text-3xl font-bold shadow-xl">
              {profile.name.charAt(0)}
            </div>
            <button className="absolute -bottom-1 -right-1 bg-white rounded-full p-1.5 shadow-md hover:bg-indigo-50 transition-colors">
              <Camera size={12} className="text-indigo-600" />
            </button>
          </div>

          <div className="flex-grow min-w-0 pt-1">
            <h1 className="text-xl font-bold truncate">{profile.name}</h1>
            <p className="text-slate-400 text-sm">{profile.email}</p>
            <div className="flex gap-4 mt-3">
              {[
                { label: 'Orders', value: totalOrders },
                { label: 'Linked', value: `${connected} apps` },
                { label: 'Since',  value: 'Jul 2025' },
              ].map(s => (
                <div key={s.label}>
                  <div className="text-base font-bold text-white">{s.value}</div>
                  <div className="text-[11px] text-slate-400">{s.label}</div>
                </div>
              ))}
            </div>
          </div>

          <button onClick={() => { setEditing(!editing); setDraft({...profile}); }}
            className="shrink-0 mt-1 p-2 bg-white/10 hover:bg-white/20 rounded-xl transition-colors">
            {editing ? <Check size={16} className="text-emerald-400" onClick={save} />
                     : <Edit3 size={16} className="text-white" />}
          </button>
        </div>

        {/* Badges */}
        <div className="relative flex gap-2 mt-5 overflow-x-auto pb-1">
          {BADGES.map(b => (
            <div key={b.label}
              className="shrink-0 bg-white/10 backdrop-blur rounded-xl px-3 py-2 flex items-center gap-2">
              <span className="text-base">{b.emoji}</span>
              <div>
                <p className="text-[11px] font-bold text-white leading-tight">{b.label}</p>
                <p className="text-[10px] text-slate-400 leading-tight">{b.desc}</p>
              </div>
            </div>
          ))}
        </div>
      </div>

      <div className="px-6 -mt-4 space-y-5 pb-8">

        {/* ── Personal Info ─────────────────────────────────── */}
        <section className="bg-white rounded-2xl shadow-sm border border-gray-100 p-5">
          <h2 className="font-bold text-gray-800 flex items-center gap-2 mb-4">
            <User size={16} className="text-indigo-500" /> Personal Information
          </h2>
          {[
            { icon: Mail,    key: 'email', label: 'Email',  placeholder: 'your@email.com' },
            { icon: Phone,   key: 'phone', label: 'Phone',  placeholder: '+91 00000 00000' },
            { icon: MapPin,  key: 'city',  label: 'City',   placeholder: 'Your city' },
          ].map(f => (
            <div key={f.key} className="flex items-center gap-3 py-3 border-b border-gray-50 last:border-0">
              <f.icon size={15} className="text-gray-400 shrink-0" />
              <div className="flex-grow">
                <p className="text-[10px] text-gray-400 uppercase tracking-wide">{f.label}</p>
                {editing
                  ? <input value={draft[f.key]}
                      onChange={e => setDraft(d => ({...d, [f.key]: e.target.value}))}
                      className="text-sm text-gray-800 bg-indigo-50 rounded-lg px-2 py-1 w-full mt-0.5 focus:outline-none focus:ring-1 focus:ring-indigo-300"
                    />
                  : <p className="text-sm font-medium text-gray-800">{profile[f.key]}</p>
                }
              </div>
            </div>
          ))}
          {editing && (
            <div className="flex gap-2 mt-3">
              <button onClick={save}
                className="flex-1 bg-indigo-600 text-white text-sm py-2 rounded-xl font-semibold hover:bg-indigo-700 transition-colors">
                Save Changes
              </button>
              <button onClick={() => setEditing(false)}
                className="px-4 bg-gray-100 text-gray-600 text-sm py-2 rounded-xl font-semibold hover:bg-gray-200 transition-colors">
                Cancel
              </button>
            </div>
          )}
        </section>

        {/* ── Connected Vendors ─────────────────────────────── */}
        <section className="bg-white rounded-2xl shadow-sm border border-gray-100 p-5">
          <h2 className="font-bold text-gray-800 flex items-center gap-2 mb-4">
            <Link2 size={16} className="text-purple-500" /> Connected Accounts
          </h2>
          <div className="grid grid-cols-2 gap-2">
            {vendors.map(v => (
              <motion.div key={v.id} whileHover={{ scale: 1.02 }} onClick={() => handleToggle(v)}
                className={`flex items-center gap-3 p-3 rounded-xl border transition-all cursor-pointer
                  ${v.status === 'connected'
                    ? 'border-emerald-100 bg-emerald-50/50 hover:bg-emerald-50'
                    : v.status === 'pending'
                    ? 'border-orange-100 bg-orange-50/50 hover:bg-orange-50'
                    : 'border-gray-100 bg-gray-50 hover:bg-gray-100'}`}>
                <span className="text-xl">{v.emoji}</span>
                <div className="min-w-0 flex-grow">
                  <p className="text-xs font-bold text-gray-800 truncate">{v.name}</p>
                  <p className={`text-[10px] font-medium ${
                    v.status === 'connected' ? 'text-emerald-600'
                    : v.status === 'pending'  ? 'text-orange-500'
                    : 'text-gray-400'}`}>
                    {v.status === 'connected' ? `${v.orders} orders`
                     : v.status === 'pending'  ? 'Verifying…'
                     : 'Tap to link'}
                  </p>
                </div>
              </motion.div>
            ))}
          </div>
        </section>

        {/* ── Security ──────────────────────────────────────── */}
        <section className="bg-white rounded-2xl shadow-sm border border-gray-100 p-5">
          <h2 className="font-bold text-gray-800 flex items-center gap-2 mb-3">
            <Shield size={16} className="text-rose-500" /> Security
          </h2>
          {[
            { label: 'Change Password',       sub: 'Last changed 30 days ago' },
            { label: 'Two-Factor Auth (2FA)', sub: 'Enabled via Google Authenticator' },
            { label: 'Active Sessions',       sub: '2 devices logged in' },
          ].map(s => (
            <div key={s.label}
              className="flex items-center justify-between py-3 border-b border-gray-50 last:border-0 cursor-pointer group hover:bg-gray-50 -mx-2 px-2 rounded-xl transition-colors">
              <div>
                <p className="text-sm font-medium text-gray-800">{s.label}</p>
                <p className="text-xs text-gray-400">{s.sub}</p>
              </div>
              <motion.span whileHover={{ x: 3 }} className="text-gray-300 group-hover:text-indigo-400 transition-colors text-lg">›</motion.span>
            </div>
          ))}
        </section>

      </div>
    </div>
  );
}
