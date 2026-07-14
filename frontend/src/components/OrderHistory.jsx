import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import {
  Package, Search, Filter, ChevronRight,
  Clock, CheckCircle, XCircle, Truck
} from 'lucide-react';

const API = 'http://localhost:8080/api';

const STATUS_META = {
  DELIVERED:        { color: 'text-emerald-600', bg: 'bg-emerald-50', icon: CheckCircle },
  COMPLETED:        { color: 'text-emerald-600', bg: 'bg-emerald-50', icon: CheckCircle },
  OUT_FOR_DELIVERY: { color: 'text-blue-600',    bg: 'bg-blue-50',    icon: Truck },
  PREPARING:        { color: 'text-orange-500',   bg: 'bg-orange-50',  icon: Clock },
  IN_PROGRESS:      { color: 'text-blue-600',    bg: 'bg-blue-50',    icon: Clock },
  CANCELLED:        { color: 'text-red-500',      bg: 'bg-red-50',     icon: XCircle },
  FAILED:           { color: 'text-red-500',      bg: 'bg-red-50',     icon: XCircle },
};
const CAT_EMOJI = { TRANSPORT: '🚗', FOOD: '🍕', SHOPPING: '📦', GROCERY: '🛒' };
const CATS = ['ALL', 'FOOD', 'TRANSPORT', 'SHOPPING', 'GROCERY'];

/* ── Skeleton row ─────────────────────────────────────────── */
const Skeleton = () => (
  <div className="bg-white rounded-2xl border border-gray-100 p-5 flex gap-4 animate-pulse">
    <div className="w-12 h-12 rounded-2xl bg-gray-200 shrink-0" />
    <div className="flex-grow space-y-2">
      <div className="h-4 bg-gray-200 rounded w-1/3" />
      <div className="h-3 bg-gray-100 rounded w-1/2" />
    </div>
    <div className="h-6 bg-gray-200 rounded w-16 self-center" />
  </div>
);

export default function OrderHistory() {
  const [orders,  setOrders]  = useState([]);
  const [loading, setLoading] = useState(true);
  const [search,  setSearch]  = useState('');
  const [cat,     setCat]     = useState('ALL');

  useEffect(() => {
    fetch(`${API}/v1/aggregator/orders?userId=1`)
      .then(r => r.ok ? r.json() : { orders: [] })
      .then(d => setOrders(Array.isArray(d.orders) ? d.orders : []))
      .catch(() => setOrders([]))
      .finally(() => setLoading(false));
  }, []);

  const filtered = orders.filter(o => {
    const okCat = cat === 'ALL' || o.category === cat;
    const q     = search.toLowerCase();
    const okQ   = !q
      || o.vendorId?.toLowerCase().includes(q)
      || o.externalOrderId?.toLowerCase().includes(q);
    return okCat && okQ;
  });

  const totalSpent = filtered.reduce((s, o) => s + Number(o.totalAmount || 0), 0);

  return (
    <div className="flex flex-col h-full bg-gray-50">
      {/* ── Header ───────────────────────────────────────────── */}
      <div className="bg-white border-b border-gray-100 px-8 pt-8 pb-6">
        <div className="flex items-start justify-between mb-5">
          <div>
            <h1 className="text-2xl font-bold text-gray-800">Order History</h1>
            <p className="text-sm text-gray-400 mt-1">
              {orders.length} orders across all platforms
            </p>
          </div>
          <div className="bg-indigo-50 border border-indigo-100 rounded-2xl px-5 py-3 text-center">
            <div className="text-xl font-bold text-indigo-600">₹{totalSpent.toFixed(0)}</div>
            <div className="text-[11px] text-indigo-400 mt-0.5">Total spent</div>
          </div>
        </div>

        {/* Search */}
        <div className="relative mb-3">
          <Search size={14} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400" />
          <input
            value={search} onChange={e => setSearch(e.target.value)}
            placeholder="Search vendor or order ID…"
            className="w-full bg-gray-50 border border-gray-200 rounded-xl pl-9 pr-4 py-2.5 text-sm text-gray-700 focus:outline-none focus:ring-2 focus:ring-indigo-300 focus:bg-white transition-all"
          />
        </div>

        {/* Category pills */}
        <div className="flex gap-2 overflow-x-auto pb-1 scrollbar-none">
          {CATS.map(c => (
            <button key={c} onClick={() => setCat(c)}
              className={`shrink-0 px-3 py-1.5 rounded-xl text-xs font-bold transition-all ${
                cat === c
                  ? 'bg-indigo-600 text-white shadow'
                  : 'bg-gray-100 text-gray-500 hover:bg-gray-200'
              }`}>
              {CAT_EMOJI[c] || ''} {c}
            </button>
          ))}
        </div>
      </div>

      {/* ── List ─────────────────────────────────────────────── */}
      <div className="flex-grow overflow-y-auto p-6 space-y-3">
        {loading
          ? [1,2,3].map(i => <Skeleton key={i} />)
          : filtered.length === 0
            ? (
              <div className="flex flex-col items-center justify-center h-56">
                <Package size={44} className="text-gray-200 mb-3" />
                <p className="text-gray-400 font-medium">
                  {orders.length === 0
                    ? 'No orders yet — place your first order via chat!'
                    : 'No orders match your filter.'}
                </p>
              </div>
            )
            : filtered.map((order, idx) => {
                const meta   = STATUS_META[order.status] || { color:'text-gray-500', bg:'bg-gray-100', icon: Package };
                const Icon   = meta.icon;
                return (
                  <motion.div key={order.id || idx}
                    initial={{ opacity:0, y:10 }} animate={{ opacity:1, y:0 }}
                    transition={{ delay: idx * 0.035 }}
                    className="bg-white border border-gray-100 rounded-2xl p-4 flex items-center gap-4 hover:shadow-md transition-shadow cursor-pointer group"
                  >
                    <div className="w-12 h-12 rounded-2xl bg-indigo-50 flex items-center justify-center text-2xl shrink-0">
                      {CAT_EMOJI[order.category] || '📋'}
                    </div>
                    <div className="flex-grow min-w-0">
                      <div className="flex items-center gap-2 flex-wrap">
                        <span className="font-semibold text-gray-800">{order.vendorId || 'Unknown'}</span>
                        <span className={`inline-flex items-center gap-1 text-[10px] font-bold px-2 py-0.5 rounded-full ${meta.bg} ${meta.color}`}>
                          <Icon size={10} />
                          {order.status?.replace(/_/g,' ')}
                        </span>
                      </div>
                      <p className="text-xs text-gray-400 mt-0.5 truncate">
                        {order.externalOrderId || `#${order.id}`}
                        {order.createdAt && (
                          <> · {new Date(order.createdAt).toLocaleDateString('en-IN',{ day:'2-digit', month:'short', year:'numeric' })}</>
                        )}
                      </p>
                    </div>
                    <div className="text-right shrink-0">
                      <div className="font-bold text-gray-800">₹{Number(order.totalAmount||0).toFixed(0)}</div>
                      <div className="text-[10px] text-gray-400">{order.currency || 'INR'}</div>
                    </div>
                    <ChevronRight size={16} className="text-gray-300 group-hover:text-indigo-400 transition-colors shrink-0" />
                  </motion.div>
                );
              })
        }
      </div>
    </div>
  );
}
