import React, { useState } from 'react';
import { motion } from 'framer-motion';
import { Terminal, Car, Pizza, ShoppingBag, ShieldCheck } from 'lucide-react';

const Landing = ({ onStart }) => {
  const [savingsSlider, setSavingsSlider] = useState(5); // 5 orders a week

  return (
    <div className="flex-grow flex flex-col h-full bg-surface-muted overflow-y-auto w-full relative pb-10">
      
      {/* Hero Section */}
      <section className="bg-gradient-to-br from-brand-dark via-brand to-brand-light text-white pt-24 pb-32 px-6 lg:px-20 text-center relative overflow-hidden rounded-b-[3rem]">
        <div className="absolute inset-0 opacity-10 bg-[url('https://www.transparenttextures.com/patterns/cubes.png')]"></div>
        <motion.div 
          initial={{ opacity: 0, y: 30 }} 
          animate={{ opacity: 1, y: 0 }} 
          transition={{ duration: 0.8 }}
          className="relative z-10 max-w-4xl mx-auto"
        >
          <div className="inline-block px-4 py-1.5 rounded-full bg-white/20 backdrop-blur-md border border-white/30 text-sm font-semibold mb-6">
            Meet the Future of Commerce 🚀
          </div>
          <h1 className="text-5xl lg:text-7xl font-bold tracking-tight mb-6">
            One Chat to Rule Them All.
          </h1>
          <p className="text-xl lg:text-2xl text-white/80 mb-10 max-w-2xl mx-auto font-light">
            Don't waste time hopping between Swiggy, Uber, and Amazon. Tell OmniBot what you want, and we'll instantly find the cheapest and fastest option.
          </p>
          <button 
            onClick={onStart}
            className="bg-accent hover:bg-accent-dark text-white px-8 py-4 rounded-full text-lg font-bold shadow-[0_0_40px_rgba(16,185,129,0.5)] hover:scale-105 transition-all"
          >
            Start Chatting Now
          </button>
        </motion.div>
      </section>

      {/* Mock Chat Terminal */}
      <section className="max-w-4xl mx-auto -mt-20 relative z-20 px-4 w-full">
        <div className="bg-surface-dark rounded-2xl shadow-2xl border border-gray-700 overflow-hidden">
          <div className="bg-gray-800 px-4 py-3 flex items-center gap-2 border-b border-gray-700">
            <div className="w-3 h-3 rounded-full bg-red-500"></div>
            <div className="w-3 h-3 rounded-full bg-yellow-500"></div>
            <div className="w-3 h-3 rounded-full bg-green-500"></div>
            <span className="text-gray-400 text-xs ml-4 flex items-center gap-1 font-mono">
              <Terminal size={14} /> OmniBot Terminal
            </span>
          </div>
          <div className="p-6 font-mono text-sm space-y-4">
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ delay: 0.5 }}>
              <span className="text-brand-light mr-2">You:</span>
              <span className="text-gray-300">Get me a spicy chicken burger ASAP.</span>
            </motion.div>
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ delay: 1.5 }}>
              <span className="text-accent mr-2">OmniBot:</span>
              <span className="text-gray-300">Scanning DoorDash, UberEats, and Zomato...</span>
            </motion.div>
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ delay: 2.5 }} className="bg-gray-800 p-4 rounded-xl border border-gray-700 mt-2">
              <div className="flex justify-between items-center mb-2">
                <span className="text-white font-bold">Best Deal Found!</span>
                <span className="text-accent font-bold">$12.50 (Zomato)</span>
              </div>
              <p className="text-gray-400 text-xs">Saves you $4.00 compared to DoorDash.</p>
            </motion.div>
          </div>
        </div>
      </section>

      {/* Global Capabilities Grid */}
      <section className="max-w-5xl mx-auto py-20 px-6">
        <h2 className="text-3xl font-bold text-center text-gray-800 mb-12 tracking-tight">Everything You Need, In One Place</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="bg-white p-6 rounded-3xl shadow-sm border border-gray-100 hover:shadow-md transition-shadow">
            <div className="w-14 h-14 bg-orange-100 text-orange-600 rounded-2xl flex items-center justify-center mb-6">
              <Pizza size={28} />
            </div>
            <h3 className="text-xl font-bold text-gray-800 mb-2">Food Delivery</h3>
            <p className="text-gray-500 text-sm leading-relaxed">Aggregating Swiggy, Zomato, UberEats, and DoorDash for the lowest delivery fees and fastest ETAs.</p>
          </div>
          <div className="bg-white p-6 rounded-3xl shadow-sm border border-gray-100 hover:shadow-md transition-shadow">
            <div className="w-14 h-14 bg-blue-100 text-blue-600 rounded-2xl flex items-center justify-center mb-6">
              <Car size={28} />
            </div>
            <h3 className="text-xl font-bold text-gray-800 mb-2">Urban Transport</h3>
            <p className="text-gray-500 text-sm leading-relaxed">Compare prices across Uber, Lyft, Bolt, and Ola instantly. Never pay surge pricing again.</p>
          </div>
          <div className="bg-white p-6 rounded-3xl shadow-sm border border-gray-100 hover:shadow-md transition-shadow">
            <div className="w-14 h-14 bg-purple-100 text-purple-600 rounded-2xl flex items-center justify-center mb-6">
              <ShoppingBag size={28} />
            </div>
            <h3 className="text-xl font-bold text-gray-800 mb-2">E-Commerce</h3>
            <p className="text-gray-500 text-sm leading-relaxed">Amazon, Walmart, and local vendors integrated. Let the AI find the exact SKU for cheaper.</p>
          </div>
        </div>
      </section>

      {/* Savings Calculator */}
      <section className="bg-white py-20 border-y border-gray-100">
        <div className="max-w-3xl mx-auto px-6 text-center">
          <h2 className="text-3xl font-bold text-gray-800 mb-4 tracking-tight">Interactive Savings Calculator</h2>
          <p className="text-gray-500 mb-10">See how much you save monthly by letting the AI pick the cheapest vendor.</p>
          
          <div className="bg-surface-muted p-8 rounded-3xl border border-gray-200 shadow-inner">
            <h3 className="text-xl font-bold text-gray-800 mb-8">How many rides/orders do you make a week?</h3>
            <input 
              type="range" 
              min="1" 
              max="20" 
              value={savingsSlider}
              onChange={(e) => setSavingsSlider(e.target.value)}
              className="w-full h-2 bg-gray-300 rounded-lg appearance-none cursor-pointer accent-accent"
            />
            <div className="text-center mt-6">
              <span className="text-4xl font-bold text-brand">{savingsSlider}</span>
              <span className="text-gray-500 ml-2">orders/week</span>
            </div>
            
            <div className="mt-10 p-6 bg-white rounded-2xl shadow-sm border border-gray-100">
              <p className="text-gray-500 text-sm font-semibold uppercase tracking-wider mb-2">Estimated Monthly Savings</p>
              <div className="text-5xl font-bold text-accent tracking-tight">
                ${(savingsSlider * 4.5 * 4).toFixed(0)}
              </div>
              <p className="text-xs text-gray-400 mt-2">Based on an average delta of $4.50 per multi-vendor comparison.</p>
            </div>
          </div>
        </div>
      </section>

      {/* Trust & Security */}
      <section className="max-w-4xl mx-auto py-16 px-6 text-center">
         <div className="inline-flex items-center gap-2 bg-green-50 text-green-700 px-4 py-2 rounded-full text-sm font-bold mb-4">
            <ShieldCheck size={18} /> Enterprise-Grade Security
         </div>
         <p className="text-gray-500 max-w-xl mx-auto text-sm leading-relaxed">
           Your payment credentials and connected accounts are secured with AES-256 end-to-end encryption. OmniBot never stores your raw CVV or passwords.
         </p>
      </section>

    </div>
  );
};

export default Landing;
