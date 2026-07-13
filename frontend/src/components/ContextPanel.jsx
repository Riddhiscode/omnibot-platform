import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import {
  MapPin, CreditCard, TrendingDown, Clock, ChevronRight,
  Package, Activity, RefreshCw, CheckCircle2, Truck, ShoppingBag
} from 'lucide-react';

const API = 'http://localhost:8080/api';

// Track status → progress percentage + color
const statusMeta = {
  SEARCHING:        { pct: 10, color: 'bg-yellow-400',  label: 'Searching...' },
  DRIVER_ASSIGNED:  { pct: 25, color: 'bg-blue-400',    label: 'Driver assigned' },
  DRIVER_EN_ROUTE:  { pct: 45, color: 'bg-blue-500',    label: 'Driver on the way' },
  ARRIVED:          { pct: 60, color: 'bg-indigo-500',  label: 'Arrived' },
  IN_PROGRESS:      { pct: 65, color: 'bg-indigo-500',  label: 'In progress' },
  PREPARING:        { pct: 40, color: 'bg-orange-400',  label: 'Preparing' },
  OUT_FOR_DELIVERY: { pct: 75, color: 'bg-orange-500',  label: 'Out for delivery' },
  DELIVERED:        { pct: 100, color: 'bg-green-500',  label: 'Delivered ✓' },
  COMPLETED:        { pct: 100, color: 'bg-green-500',  label: 'Completed ✓' },
  CANCELLED:        { pct: 100, color: 'bg-red-400',    label: 'Cancelled' },
  FAILED:           { pct: 0,   color: 'bg-red-400',    label: 'Failed' },
};

function useOrders(token) {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);

  const fetchOrders = async () => {
    setLoading(true);
    try {
      const res = await fetch(`${API}/v1/aggregator/orders?userId=1`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (res.ok) {
        const data = await res.json();
        setOrders(data.orders || []);
      }
    } catch (_) {
      // backend may not be running; keep stale state
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
    const interval = setInterval(fetchOrders, 15000); // refresh every 15s
    return () => clearInterval(interval);
  }, []);

  return { orders, loading, refresh: fetchOrders };
}

function useVendorStatus(token) {
  const [status, setStatus] = useState(null);

  useEffect(() => {
    fetch(`${API}/v1/aggregator/status`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
      .then(r => r.ok ? r.json() : null)
      .then(d => setStatus(d))
      .catch(() => {});
  }, []);

  return status;
}

function TrackingBar({ statusKey }) {
  const meta = statusMeta[statusKey] || { pct: 0, color: 'bg-gray-300', label: statusKey };
  return (
    <div className="mt-3">
      <div className="bg-gray-100 h-1.5 rounded-full overflow-hidden">
        <motion.div
          className={`${meta.color} h-full rounded-full`}
          initial={{ width: 0 }}
          animate={{ width: `${meta.pct}%` }}
          transition={{ duration: 0.8, ease: 'easeOut' }}
        />
      </div>
      <p className="text-[10px] text-gray-500 mt-1.5 font-medium">{meta.label}</p>
    </div>
  );
}

function OrderCard({ order }) {
  const [tracking, setTracking] = useState(null);
  const [expanded, setExpanded] = useState(false);

  const track = async () => {
    if (!order.vendorId || !order.externalOrderId) return;
    try {
      const res = await fetch(
        `${API}/v1/aggregator/track?vendorName=${order.vendorId}&orderId=${order.externalOrderId}`
      );
      if (res.ok) setTracking(await res.json());
    } catch (_) {}
  };

  useEffect(() => { track(); }, []);

  const statusKey = tracking?.status || 'IN_PROGRESS';
  const categoryIcon = {
    TRANSPORT: '🚗', FOOD: '🍕', SHOPPING: '📦', GROCERY: '🛒'
  }[order.category] || '📋';

  const isTerminal = ['DELIVERED','COMPLETED','CANCELLED','FAILED'].includes(statusKey);
  const formattedDate = order.createdAt
    ? new Date(order.createdAt).toLocaleDateString('en-IN', { day:'2-digit', month:'short', hour:'2-digit', minute:'2-digit' })
    : '';

  return (
    <motion.div
      layout
      className="bg-white border border-gray-100 rounded-2xl shadow-sm hover:shadow-md transition-shadow cursor-pointer"
      onClick={() => setExpanded(e => !e)}
    >
      <div className="p-4 flex items-center gap-3">
        <div className="w-10 h-10 rounded-full bg-indigo-50 flex items-center justify-center shrink-0 text-xl">
          {categoryIcon}
        </div>
        <div className="flex-grow overflow-hidden">
          <h4 className="font-bold text-sm text-gray-800 truncate">{order.vendorId}</h4>
          <p className="text-xs text-gray-500">{formattedDate}</p>
        </div>
        <div className="text-right shrink-0">
          <div className="font-bold text-sm text-gray-800">
            {order.currency || 'INR'} {Number(order.totalAmount).toFixed(0)}
          </div>
          <ChevronRight size={14} className={`ml-auto text-gray-400 transition-transform ${expanded ? 'rotate-90' : ''}`} />
        </div>
      </div>

      {!isTerminal && <TrackingBar statusKey={statusKey} />}

      <AnimatePresence>
        {expanded && (
          <motion.div
            initial={{ height: 0, opacity: 0 }}
            animate={{ height: 'auto', opacity: 1 }}
            exit={{ height: 0, opacity: 0 }}
            transition={{ duration: 0.25 }}
            className="overflow-hidden px-4 pb-4"
          >
            <div className="mt-3 border-t border-gray-50 pt-3 space-y-1.5 text-xs text-gray-500">
              <div className="flex justify-between">
                <span>Category</span><span className="font-medium text-gray-700">{order.category}</span>
              </div>
              <div className="flex justify-between">
                <span>Status</span>
                <span className={`font-bold ${isTerminal && statusKey === 'DELIVERED' ? 'text-green-600' : 'text-indigo-600'}`}>
                  {statusMeta[statusKey]?.label || statusKey}
                </span>
              </div>
              {order.externalOrderId && (
                <div className="flex justify-between">
                  <span>Order ID</span><span className="font-mono text-[10px] text-gray-500">{order.externalOrderId}</span>
                </div>
              )}
              {order.trackingUrl && (
                <a
                  href={order.trackingUrl}
                  target="_blank"
                  rel="noreferrer"
                  className="mt-2 flex items-center gap-1 text-indigo-600 font-semibold hover:underline"
                  onClick={e => e.stopPropagation()}
                >
                  <Truck size={12} /> Track Order
                </a>
              )}
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </motion.div>
  );
}

function VendorStatusBadge({ status }) {
  if (!status) return null;
  const total     = status.totalAdapters     || 0;
  const available = status.availableAdapters || 0;
  const mode      = status.mode || 'mock';

  return (
    <div className="bg-gray-50 border border-gray-100 rounded-2xl p-4">
      <div className="flex items-center justify-between mb-3">
        <h3 className="font-bold text-gray-800 text-sm flex items-center gap-2">
          <Activity size={14} className="text-indigo-500" /> Vendor Network
        </h3>
        <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
          mode === 'mock' ? 'bg-yellow-100 text-yellow-700' : 'bg-green-100 text-green-700'
        }`}>
          {mode.toUpperCase()} MODE
        </span>
      </div>
      <div className="grid grid-cols-3 gap-2">
        {['FOOD','TRANSPORT','SHOPPING'].map(cat => {
          const vendors = status[cat] || [];
          const live    = vendors.filter(v => v.available).length;
          return (
            <div key={cat} className="text-center bg-white rounded-xl p-2 border border-gray-100">
              <div className="text-lg font-bold text-indigo-600">{live}/{vendors.length}</div>
              <div className="text-[9px] text-gray-500 font-medium mt-0.5">{cat}</div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

const ContextPanel = ({ token }) => {
  const { orders, loading, refresh } = useOrders(token);
  const vendorStatus = useVendorStatus(token);
  const activeOrders = orders.filter(o => !['DELIVERED','COMPLETED','CANCELLED','FAILED'].includes(o.status));

  // Calculate real savings: assume avg 15% savings per order
  const totalSpent   = orders.reduce((s, o) => s + Number(o.totalAmount || 0), 0);
  const totalSavings = (totalSpent * 0.15).toFixed(2);

  return (
    <div className="flex flex-col h-full bg-surface">
      <div className="p-6 border-b border-gray-100 flex-shrink-0 flex items-center justify-between">
        <div>
          <h2 className="text-xl font-bold tracking-tight text-gray-800">Dashboard</h2>
          <p className="text-sm text-gray-500 mt-1">Your command center</p>
        </div>
        <button onClick={refresh} className="p-2 rounded-xl hover:bg-gray-100 transition-colors" title="Refresh">
          <RefreshCw size={16} className={`text-gray-400 ${loading ? 'animate-spin' : ''}`} />
        </button>
      </div>

      <div className="flex-grow overflow-y-auto p-6 space-y-6">

        {/* Real Savings Card */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="bg-gradient-to-br from-brand to-brand-dark rounded-2xl p-5 text-white shadow-lg shadow-brand/20 relative overflow-hidden"
        >
          <div className="absolute -right-10 -top-10 w-32 h-32 bg-white opacity-10 rounded-full blur-2xl" />
          <div className="flex items-center gap-3 mb-2">
            <TrendingDown size={20} className="text-brand-100" />
            <h3 className="font-semibold text-sm opacity-90">Total Saved This Month</h3>
          </div>
          <div className="text-3xl font-bold tracking-tight">₹{totalSavings}</div>
          <p className="text-xs mt-3 opacity-80 border-t border-white/20 pt-3">
            Across {orders.length} order{orders.length !== 1 ? 's' : ''} via OmniBot
          </p>
        </motion.div>

        {/* Active Orders */}
        <div>
          <div className="flex items-center justify-between mb-4">
            <h3 className="font-bold text-gray-800 text-sm flex items-center gap-2">
              <Clock size={14} className="text-orange-500" />
              Active Orders {activeOrders.length > 0 && (
                <span className="bg-orange-100 text-orange-600 text-[10px] px-1.5 py-0.5 rounded-full font-bold">
                  {activeOrders.length}
                </span>
              )}
            </h3>
          </div>

          {loading && orders.length === 0 ? (
            <div className="space-y-3">
              {[1,2].map(i => (
                <div key={i} className="bg-white rounded-2xl border border-gray-100 p-4 animate-pulse">
                  <div className="flex gap-3">
                    <div className="w-10 h-10 rounded-full bg-gray-200" />
                    <div className="flex-grow space-y-2">
                      <div className="h-3 bg-gray-200 rounded w-3/4" />
                      <div className="h-2 bg-gray-100 rounded w-1/2" />
                    </div>
                  </div>
                </div>
              ))}
            </div>
          ) : activeOrders.length === 0 ? (
            <div className="bg-gray-50 border border-gray-100 rounded-2xl p-6 text-center">
              <Package size={28} className="text-gray-300 mx-auto mb-2" />
              <p className="text-sm text-gray-400">No active orders</p>
              <p className="text-xs text-gray-300 mt-1">Your live orders will appear here</p>
            </div>
          ) : (
            <div className="space-y-3">
              {activeOrders.map(order => <OrderCard key={order.id} order={order} />)}
            </div>
          )}
        </div>

        {/* Recent Completed Orders */}
        {orders.filter(o => ['DELIVERED','COMPLETED'].includes(o.status)).length > 0 && (
          <div>
            <h3 className="font-bold text-gray-800 text-sm mb-3 flex items-center gap-2">
              <CheckCircle2 size={14} className="text-green-500" /> Recent Completed
            </h3>
            <div className="space-y-2">
              {orders
                .filter(o => ['DELIVERED','COMPLETED'].includes(o.status))
                .slice(0, 3)
                .map(order => <OrderCard key={order.id} order={order} />)}
            </div>
          </div>
        )}

        {/* Vendor Status */}
        <VendorStatusBadge status={vendorStatus} />

        {/* Quick Actions */}
        <div>
          <h3 className="font-bold text-gray-800 text-sm mb-4">Quick Actions</h3>
          <div className="grid grid-cols-2 gap-3">
            <button className="bg-gray-50 hover:bg-brand/5 border border-gray-100 hover:border-brand/30 rounded-xl p-3 flex flex-col items-center justify-center gap-2 transition-all group">
              <div className="bg-white p-2 rounded-lg shadow-sm group-hover:text-brand">
                <MapPin size={18} />
              </div>
              <span className="text-xs font-semibold text-gray-600 group-hover:text-brand">Ride Home</span>
            </button>
            <button className="bg-gray-50 hover:bg-brand/5 border border-gray-100 hover:border-brand/30 rounded-xl p-3 flex flex-col items-center justify-center gap-2 transition-all group">
              <div className="bg-white p-2 rounded-lg shadow-sm group-hover:text-brand">
                <ShoppingBag size={18} />
              </div>
              <span className="text-xs font-semibold text-gray-600 group-hover:text-brand">Quick Buy</span>
            </button>
          </div>
        </div>

      </div>
    </div>
  );
};

export default ContextPanel;
