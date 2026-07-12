import React from 'react';
import { motion } from 'framer-motion';
import { MapPin, CreditCard, TrendingDown, Clock, ChevronRight } from 'lucide-react';

const ContextPanel = () => {
  return (
    <div className="flex flex-col h-full bg-surface">
      <div className="p-6 border-b border-gray-100 flex-shrink-0">
        <h2 className="text-xl font-bold tracking-tight text-gray-800">Dashboard</h2>
        <p className="text-sm text-gray-500 mt-1">Your command center</p>
      </div>

      <div className="flex-grow overflow-y-auto p-6 space-y-6">
        {/* Savings Card */}
        <motion.div 
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="bg-gradient-to-br from-brand to-brand-dark rounded-2xl p-5 text-white shadow-lg shadow-brand/20 relative overflow-hidden"
        >
          <div className="absolute -right-10 -top-10 w-32 h-32 bg-white opacity-10 rounded-full blur-2xl"></div>
          <div className="flex items-center gap-3 mb-2">
            <TrendingDown size={20} className="text-brand-100" />
            <h3 className="font-semibold text-sm opacity-90">Total Saved This Month</h3>
          </div>
          <div className="text-3xl font-bold tracking-tight">$142.50</div>
          <p className="text-xs mt-3 opacity-80 border-t border-white/20 pt-3">
            Across 12 rides and 4 food orders
          </p>
        </motion.div>

        {/* Active Orders Tracker */}
        <div>
          <div className="flex items-center justify-between mb-4">
            <h3 className="font-bold text-gray-800 text-sm">Active Orders</h3>
            <button className="text-brand text-xs font-semibold hover:underline">View All</button>
          </div>
          
          <div className="bg-white border border-gray-100 rounded-2xl p-4 shadow-sm hover:shadow-md transition-shadow cursor-pointer">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-full bg-orange-100 flex items-center justify-center text-orange-500 shrink-0">
                <Clock size={20} />
              </div>
              <div className="flex-grow overflow-hidden">
                <h4 className="font-bold text-sm text-gray-800 truncate">Spicy Chicken Burger</h4>
                <p className="text-xs text-gray-500">Arriving in 12 mins via DoorDash</p>
              </div>
              <ChevronRight size={18} className="text-gray-400" />
            </div>
            
            <div className="mt-4 bg-gray-100 h-1.5 rounded-full overflow-hidden">
              <div className="bg-orange-500 w-2/3 h-full rounded-full"></div>
            </div>
            <div className="flex justify-between mt-2 text-[10px] text-gray-500 font-medium px-1">
              <span>Preparing</span>
              <span className="text-orange-600">On the way</span>
              <span>Delivered</span>
            </div>
          </div>
        </div>

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
                <CreditCard size={18} />
              </div>
              <span className="text-xs font-semibold text-gray-600 group-hover:text-brand">Add Card</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ContextPanel;
