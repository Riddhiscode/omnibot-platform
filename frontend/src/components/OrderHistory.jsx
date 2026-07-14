import React, { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { Package, Search, Filter, RotateCcw, ChevronDown } from 'lucide-react';

const API = 'http://localhost:8080/api';

const statusColors = {
  DELIVERED:  'bg-green-100 text-green-700',
  COMPLETED:  'bg-green-100 text-green-700',
  IN_PROGRESS:'bg-blue-100 text-blue-700',
  PREPARING:  'bg-orange-100 text-orange-700',
  OUT_FOR_DELIVERY: 'bg-orange-100 text-orange-700',
  CANCELLED:  'bg-red-100 text-red-700',
  FAILED:     'bg-red-100 text-red-700',
  SEARCHING:  'bg-yellow-100 text-yellow-700',
};

const categoryIcons = { TRANSPORT:'🚗', FOOD:'🍕', SHOPPING:'📦', GROCERY:'🛒' };

export default function OrderHistory() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [filter, setFilter] = useState('ALL');

  useEffect(() => {
    fetch(`${API}/v1/aggregator/orders?userId=1`)
      .then(r => r.ok ? r.json() : { orders: [] })
      .then(d => setOrders(d.orders || []))
      .catch(() => setOrders([]))
      .finally(() => setLoading(false));
  }, []);

  const categories = ['ALL', 'FOOD', 'TRANSPORT', 'SHOPPING', 'GROCERY'];

  const filtered = orders.filter(o => {
    const matchCat  = filter === 'ALL' || o.category === filter;
    const matchSearch = !search || o.vendorId?.toLowerCase().includes(search.toLowerCase())
                                || o.externalOrderId?.toLowerCase().includes(search.toLowerCase());
    return matchCat && matchSearch;
  });

  const totalSpent = filtered.reduce((s, o) => s + Number(o.totalAmount || 0), 0);

  return (
    <div className="flex flex-col h-full bg-surface-muted overflow-hidden">
      {/* Header */}
      <div className="p-8 border-b border-gray-100 bg-white">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-800 tracking-tight">Order History</h1>
            <p className="text-gray-500 mt-2">All {orders.length} orders across every platform.</p>
          </div>
          <div className="bg-indigo-50 border border-indigo-100 rounded-2xl px-5 py-3 text-center">
            <div className="text-2xl font-bold text-indigo-600">₹{totalSpent.toFixed(0)}</div>
            <div className="text-xs text-indigo-400 mt-0.5">Total spent</div>
          </div>
        </div>

        {/* Search & Filter */}
        <div className="mt-5 flex gap-3">
          <div className="relative flex-grow">
            <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
            <input
              type="text"
              value={search}
              onChange={e => setSearch(e.target.value)}
              placeholder="Search by vendor or order ID..."
              className="w-full bg-gray-50 border border-gray-200 rounded-xl pl-9 pr-4 py-2.5 text-sm text-gray-700 focus:outline-none focus:ring-2 focus:ring-indigo-300 focus:bg-white transition-all"
            />
          </div>
          <div className="flex gap-2 overflow-x-auto">
            {categories.map(c => (
              <button
                key={c}
                onClick={() => setFilter(c)}
                className={`shrink-0 px-3 py-2 rounded-xl text-xs font-bold transition-all ${
                  filter === c ? 'bg-indigo-600 text-white shadow-md' : 'bg-gray-100 text-gray-500 hover:bg-gray-200'
                }`}
              >
                {categoryIcons[c] || ''} {c}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Orders List */}
      <div className="flex-grow overflow-y-auto p-8">
        {loading ? (
          <div className="space-y-4">
            {[1,2,3,4].map(i => (
              <div key={i} className="bg-white rounded-2xl border border-gray-100 p-5 animate-pulse flex gap-4">
                <div className="w-12 h-12 rounded-full bg-gray-200" />
                <div className="flex-grow space-y-2">
                  <div className="h-4 bg-gray-200 rounded w-1/3" />
                  <div className="h-3 bg-gray-100 rounded w-1/2" />
                </div>
                <div className="h-4 bg-gray-200 rounded w-16" />
              </div>
            ))}
          </div>
        ) : filtered.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-64">
            <Package size={48} className="text-gray-200 mb-4" />
            <h3 className="text-lg font-bold text-gray-400">No orders found</h3>
            <p className="text-sm text-gray-300 mt-1">
              {orders.length === 0 ? 'Place your first order via the chat!' : 'Try adjusting your filters.'}
            </p>
          </div>
        ) : (
          <div className="space-y-3">
            {filtered.map((order, idx) => (
              <motion.div
                key={order.id}
                initial={{ opacity: 0, y: 12 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: idx * 0.04 }}
                className="bg-white border border-gray-100 rounded-2xl p-5 flex items-center gap-4 hover:shadow-md transition-shadow"
              >
                <div className="w-12 h-12 rounded-2xl bg-indigo-50 flex items-center justify-center text-2xl shrink-0">
                  {categoryIcons[order.category] || '📋'}
                </div>
                <div className="flex-grow min-w-0">
                  <div className="flex items-center gap-2">
                    <h4 className="font-bold text-gray-800">{order.vendorId}</h4>
                    <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${statusColors[order.status] || 'bg-gray-100 text-gray-600'}`}>
                      {order.status?.replace(/_/g, ' ')}
                    </span>
                  </div>
                  <p className="text-xs text-gray-400 mt-0.5 truncate">
                    {order.externalOrderId || `Order #${order.id}`} &middot; {
                      order.createdAt
                        ? new Date(order.createdAt).toLocaleDateString('en-IN', { day:'2-digit', month:'short', year:'numeric' })
                        : 'Recently'
                    }
                  </p>
                </div>
                <div className="text-right shrink-0">
                  <div className="font-bold text-lg text-gray-800">
                    ₹{Number(order.totalAmount || 0).toFixed(0)}
                  </div>
                  <div className="text-[10px] text-gray-400">{order.currency || 'INR'}</div>
                </div>
              </motion.div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
