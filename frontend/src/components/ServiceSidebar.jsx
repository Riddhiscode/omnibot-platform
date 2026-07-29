import React from 'react';

const ServiceSidebar = ({ onQuickSend }) => {
  return (
    <div className="w-64 bg-[#0e131b] border-l border-white/5 p-4 overflow-y-auto flex-shrink-0 flex flex-col gap-6 text-sm select-none">
      {/* Food Section */}
      <div>
        <div className="text-[11px] font-bold text-orange-400 uppercase tracking-wider mb-2.5 flex items-center gap-1.5">
          <span>🍕</span> FOOD
        </div>
        <div className="space-y-1">
          <div 
            onClick={() => onQuickSend?.('I want to use Zomato for order food')}
            className="flex items-center gap-2.5 px-3 py-2 rounded-xl bg-white/[0.02] hover:bg-white/[0.06] border border-white/5 text-gray-300 hover:text-white cursor-pointer transition-all"
          >
            <span className="w-2 h-2 rounded-full bg-emerald-500 shrink-0"></span>
            <span>Zomato</span>
          </div>
          <div 
            onClick={() => onQuickSend?.('I want to use Swiggy for order food')}
            className="flex items-center gap-2.5 px-3 py-2 rounded-xl bg-white/[0.02] hover:bg-white/[0.06] border border-white/5 text-gray-300 hover:text-white cursor-pointer transition-all"
          >
            <span className="w-2 h-2 rounded-full bg-emerald-500 shrink-0"></span>
            <span>Swiggy</span>
          </div>
          <div 
            onClick={() => onQuickSend?.('I want to use Blinkit for order grocery')}
            className="flex items-center gap-2.5 px-3 py-2 rounded-xl bg-white/[0.02] hover:bg-white/[0.06] border border-white/5 text-gray-300 hover:text-white cursor-pointer transition-all"
          >
            <span className="w-2 h-2 rounded-full bg-emerald-500 shrink-0"></span>
            <span>Blinkit</span>
          </div>
          <div 
            onClick={() => onQuickSend?.('I want to use Zepto for order grocery')}
            className="flex items-center gap-2.5 px-3 py-2 rounded-xl bg-white/[0.02] hover:bg-white/[0.06] border border-white/5 text-gray-300 hover:text-white cursor-pointer transition-all"
          >
            <span className="w-2 h-2 rounded-full bg-emerald-500 shrink-0"></span>
            <span>Zepto</span>
          </div>
        </div>
      </div>

      {/* Transport Section */}
      <div>
        <div className="text-[11px] font-bold text-orange-400 uppercase tracking-wider mb-2.5 flex items-center gap-1.5">
          <span>🚕</span> TRANSPORT
        </div>
        <div className="space-y-1">
          <div 
            onClick={() => onQuickSend?.('I want to use Uber for book ride')}
            className="flex items-center gap-2.5 px-3 py-2 rounded-xl bg-white/[0.02] hover:bg-white/[0.06] border border-white/5 text-gray-300 hover:text-white cursor-pointer transition-all"
          >
            <span className="w-2 h-2 rounded-full bg-white shrink-0"></span>
            <span>Uber</span>
          </div>
          <div 
            onClick={() => onQuickSend?.('I want to use Ola for book ride')}
            className="flex items-center gap-2.5 px-3 py-2 rounded-xl bg-white/[0.02] hover:bg-white/[0.06] border border-white/5 text-gray-300 hover:text-white cursor-pointer transition-all"
          >
            <span className="w-2 h-2 rounded-full bg-amber-400 shrink-0"></span>
            <span>Ola</span>
          </div>
          <div 
            onClick={() => onQuickSend?.('I want to use Rapido for book ride')}
            className="flex items-center gap-2.5 px-3 py-2 rounded-xl bg-white/[0.02] hover:bg-white/[0.06] border border-white/5 text-gray-300 hover:text-white cursor-pointer transition-all"
          >
            <span className="w-2 h-2 rounded-full bg-amber-500 shrink-0"></span>
            <span>Rapido</span>
          </div>
          <div 
            onClick={() => onQuickSend?.('I want to use Yulu for book ride')}
            className="flex items-center gap-2.5 px-3 py-2 rounded-xl bg-white/[0.02] hover:bg-white/[0.06] border border-white/5 text-gray-300 hover:text-white cursor-pointer transition-all"
          >
            <span className="w-2 h-2 rounded-full bg-cyan-400 shrink-0"></span>
            <span>Yulu</span>
          </div>
        </div>
      </div>

      {/* Shopping Section */}
      <div>
        <div className="text-[11px] font-bold text-orange-400 uppercase tracking-wider mb-2.5 flex items-center gap-1.5">
          <span>📦</span> SHOPPING
        </div>
        <div className="space-y-1">
          <div 
            onClick={() => onQuickSend?.('Buy something on Amazon')}
            className="flex items-center gap-2.5 px-3 py-2 rounded-xl bg-white/[0.02] hover:bg-white/[0.06] border border-white/5 text-gray-300 hover:text-white cursor-pointer transition-all"
          >
            <span className="w-2 h-2 rounded-full bg-amber-500 shrink-0"></span>
            <span>Amazon</span>
          </div>
          <div 
            onClick={() => onQuickSend?.('Buy something on Flipkart')}
            className="flex items-center gap-2.5 px-3 py-2 rounded-xl bg-white/[0.02] hover:bg-white/[0.06] border border-white/5 text-gray-300 hover:text-white cursor-pointer transition-all"
          >
            <span className="w-2 h-2 rounded-full bg-blue-500 shrink-0"></span>
            <span>Flipkart</span>
          </div>
          <div 
            onClick={() => onQuickSend?.('Buy something on Meesho')}
            className="flex items-center gap-2.5 px-3 py-2 rounded-xl bg-white/[0.02] hover:bg-white/[0.06] border border-white/5 text-gray-300 hover:text-white cursor-pointer transition-all"
          >
            <span className="w-2 h-2 rounded-full bg-pink-500 shrink-0"></span>
            <span>Meesho</span>
          </div>
          <div 
            onClick={() => onQuickSend?.('Buy something on Myntra')}
            className="flex items-center gap-2.5 px-3 py-2 rounded-xl bg-white/[0.02] hover:bg-white/[0.06] border border-white/5 text-gray-300 hover:text-white cursor-pointer transition-all"
          >
            <span className="w-2 h-2 rounded-full bg-rose-500 shrink-0"></span>
            <span>Myntra</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ServiceSidebar;
