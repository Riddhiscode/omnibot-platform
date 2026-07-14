import React, { useState } from 'react';
import { motion } from 'framer-motion';
import {
  CreditCard, ShieldCheck, Smartphone, Plus, Zap, Lock,
  Wallet as WalletIcon, TrendingUp, Gift, ChevronRight
} from 'lucide-react';

/* ── Static demo data (replace with real API when payment gateway is wired) */
const CARDS = [
  { id: 1, type: 'visa',       last4: '4242', expiry: '09/27', name: 'Riddhi S.',   primary: true  },
  { id: 2, type: 'mastercard', last4: '8810', expiry: '03/26', name: 'Riddhi S.',   primary: false },
];

const SAVED_UPI = [
  { id: 1, vpa: 'riddhi@okaxis',  app: 'Google Pay',  color: 'from-blue-500 to-indigo-600'    },
  { id: 2, vpa: 'riddhi@ybl',    app: 'PhonePe',      color: 'from-purple-500 to-purple-700'  },
];

const RECENT_TXN = [
  { id: 1, desc: 'Zomato — Butter Chicken',   amt: -349,  date: 'Today, 1:12 PM',    status: 'paid'    },
  { id: 2, desc: 'Uber — Airport drop',       amt: -520,  date: 'Yesterday, 8:40 AM',status: 'paid'    },
  { id: 3, desc: 'OmniBot Cashback',          amt:  +50,  date: '12 Jul',            status: 'credit'  },
  { id: 4, desc: 'Flipkart — Headphones',     amt: -1349, date: '11 Jul',            status: 'paid'    },
  { id: 5, desc: 'BigBasket — Grocery',       amt: -680,  date: '10 Jul',            status: 'paid'    },
];

const CARD_BG = {
  visa:       'from-indigo-600 via-indigo-500 to-purple-600',
  mastercard: 'from-gray-700  via-gray-600   to-gray-800',
};

/* ── Sub-components ──────────────────────────────────────────────── */

function CreditCardUI({ card, active, onClick }) {
  return (
    <motion.div whileHover={{ scale: 1.02 }} whileTap={{ scale: 0.98 }}
      onClick={onClick}
      className={`relative cursor-pointer rounded-2xl p-5 bg-gradient-to-br ${CARD_BG[card.type]} text-white shadow-lg
        ring-2 transition-all ${active ? 'ring-indigo-400 shadow-indigo-300/40' : 'ring-transparent'}`}
      style={{ minHeight: 150 }}
    >
      <div className="flex items-start justify-between mb-6">
        <WalletIcon size={22} className="opacity-80" />
        <span className="text-xs font-bold tracking-widest uppercase opacity-70">{card.type}</span>
      </div>
      <p className="text-lg font-mono tracking-widest mb-4">
        •••• •••• •••• {card.last4}
      </p>
      <div className="flex items-end justify-between">
        <div>
          <p className="text-[10px] opacity-60 uppercase tracking-wider">Card holder</p>
          <p className="text-sm font-semibold">{card.name}</p>
        </div>
        <div className="text-right">
          <p className="text-[10px] opacity-60 uppercase tracking-wider">Expires</p>
          <p className="text-sm font-semibold">{card.expiry}</p>
        </div>
      </div>
      {card.primary && (
        <span className="absolute top-4 right-14 text-[9px] bg-white/20 rounded-full px-2 py-0.5 font-bold uppercase tracking-wide">
          Primary
        </span>
      )}
    </motion.div>
  );
}

function UpiRow({ upi }) {
  return (
    <div className="flex items-center gap-3 p-3 rounded-xl hover:bg-gray-50 transition-colors cursor-pointer group">
      <div className={`w-10 h-10 rounded-xl bg-gradient-to-br ${upi.color} flex items-center justify-center`}>
        <Smartphone size={18} className="text-white" />
      </div>
      <div className="flex-grow">
        <p className="text-sm font-semibold text-gray-800">{upi.app}</p>
        <p className="text-xs text-gray-400">{upi.vpa}</p>
      </div>
      <ChevronRight size={16} className="text-gray-300 group-hover:text-indigo-400 transition-colors" />
    </div>
  );
}

/* ── Main Panel ──────────────────────────────────────────────────── */

export default function PaymentVault() {
  const [activeCard, setActiveCard] = useState(CARDS[0].id);

  const totalSpent = RECENT_TXN.filter(t => t.amt < 0).reduce((s,t) => s + Math.abs(t.amt), 0);
  const cashback   = RECENT_TXN.filter(t => t.amt > 0).reduce((s,t) => s + t.amt, 0);

  return (
    <div className="flex flex-col h-full bg-gray-50 overflow-y-auto">

      {/* ── Top stats bar ──────────────────────────────────── */}
      <div className="bg-gradient-to-r from-indigo-600 to-purple-600 px-8 pt-8 pb-10 text-white">
        <h1 className="text-2xl font-bold mb-1">Payment Vault</h1>
        <p className="text-indigo-200 text-sm mb-6">All your payment methods in one secure place.</p>
        <div className="grid grid-cols-3 gap-3">
          {[
            { label: 'Cards saved',    value: CARDS.length,               icon: CreditCard  },
            { label: 'UPI linked',     value: SAVED_UPI.length,           icon: Zap         },
            { label: 'OmniBot Coins',  value: `₹${cashback}`,            icon: Gift        },
          ].map(s => (
            <div key={s.label} className="bg-white/10 rounded-2xl p-3 flex flex-col gap-1 backdrop-blur">
              <s.icon size={16} className="text-indigo-200" />
              <span className="text-xl font-bold">{s.value}</span>
              <span className="text-[11px] text-indigo-200">{s.label}</span>
            </div>
          ))}
        </div>
      </div>

      <div className="px-6 -mt-4 space-y-6 pb-8">

        {/* ── Cards ──────────────────────────────────────────── */}
        <section className="bg-white rounded-2xl shadow-sm border border-gray-100 p-5">
          <div className="flex items-center justify-between mb-4">
            <h2 className="font-bold text-gray-800 flex items-center gap-2">
              <CreditCard size={17} className="text-indigo-500" /> Saved Cards
            </h2>
            <button className="flex items-center gap-1 text-xs text-indigo-600 font-semibold hover:text-indigo-800 transition-colors">
              <Plus size={13} /> Add Card
            </button>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            {CARDS.map(c => (
              <CreditCardUI key={c.id} card={c} active={activeCard === c.id}
                onClick={() => setActiveCard(c.id)} />
            ))}
          </div>
          <div className="mt-3 flex items-center gap-2 text-xs text-gray-400">
            <Lock size={12} /> <span>256-bit encrypted · PCI DSS compliant</span>
          </div>
        </section>

        {/* ── UPI ────────────────────────────────────────────── */}
        <section className="bg-white rounded-2xl shadow-sm border border-gray-100 p-5">
          <div className="flex items-center justify-between mb-3">
            <h2 className="font-bold text-gray-800 flex items-center gap-2">
              <Smartphone size={17} className="text-purple-500" /> UPI Accounts
            </h2>
            <button className="flex items-center gap-1 text-xs text-purple-600 font-semibold hover:text-purple-800 transition-colors">
              <Plus size={13} /> Link UPI
            </button>
          </div>
          <div className="divide-y divide-gray-50">
            {SAVED_UPI.map(u => <UpiRow key={u.id} upi={u} />)}
          </div>
        </section>

        {/* ── Recent Transactions ──────────────────────────── */}
        <section className="bg-white rounded-2xl shadow-sm border border-gray-100 p-5">
          <div className="flex items-center justify-between mb-4">
            <h2 className="font-bold text-gray-800 flex items-center gap-2">
              <TrendingUp size={17} className="text-emerald-500" /> Recent Transactions
            </h2>
            <span className="text-xs text-gray-400">This month: <span className="font-bold text-gray-700">₹{totalSpent}</span></span>
          </div>
          <div className="space-y-1">
            {RECENT_TXN.map((t, i) => (
              <motion.div key={t.id}
                initial={{ opacity:0, x:-8 }} animate={{ opacity:1, x:0 }}
                transition={{ delay: i * 0.04 }}
                className="flex items-center gap-3 py-2.5 px-2 rounded-xl hover:bg-gray-50 transition-colors"
              >
                <div className={`w-8 h-8 rounded-xl flex items-center justify-center text-sm
                  ${t.status === 'credit' ? 'bg-emerald-50 text-emerald-600' : 'bg-gray-100 text-gray-500'}`}>
                  {t.status === 'credit' ? '+' : '−'}
                </div>
                <div className="flex-grow min-w-0">
                  <p className="text-sm font-medium text-gray-800 truncate">{t.desc}</p>
                  <p className="text-xs text-gray-400">{t.date}</p>
                </div>
                <span className={`text-sm font-bold shrink-0 ${t.amt > 0 ? 'text-emerald-600' : 'text-gray-700'}`}>
                  {t.amt > 0 ? '+' : ''}₹{Math.abs(t.amt)}
                </span>
              </motion.div>
            ))}
          </div>
        </section>

      </div>
    </div>
  );
}
