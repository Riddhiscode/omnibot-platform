import React from 'react';
import { motion } from 'framer-motion';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, Legend } from 'recharts';
import { TrendingUp, ArrowDownRight, ArrowUpRight, DollarSign, Activity, FileSpreadsheet, FileText } from 'lucide-react';

const savingsData = [
  { name: 'Week 1', youPaid: 120, avoidedCost: 155 },
  { name: 'Week 2', youPaid: 95, avoidedCost: 120 },
  { name: 'Week 3', youPaid: 150, avoidedCost: 195 },
  { name: 'Week 4', youPaid: 110, avoidedCost: 145 },
];

const categoryData = [
  { name: 'Food', value: 45, color: '#f97316' },
  { name: 'Transport', value: 35, color: '#3b82f6' },
  { name: 'Grocery', value: 20, color: '#10b981' },
];

const AnalyticsDashboard = ({ token }) => {

  const handleExport = async (format) => {
    try {
      const response = await fetch(`http://localhost:8080/api/v1/export/${format}?userId=1`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `omnibot_report.${format === 'excel' ? 'xlsx' : 'pdf'}`;
      a.click();
      window.URL.revokeObjectURL(url);
    } catch (e) {
      console.error(`Export ${format} failed:`, e);
    }
  };

  return (
    <div className="flex flex-col h-full bg-surface-muted overflow-y-auto">
      <div className="p-8 border-b border-gray-100 bg-white">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-800 tracking-tight">Savings & Analytics</h1>
            <p className="text-gray-500 mt-2">Track your spending and see how much OmniBot is saving you.</p>
          </div>
          <div className="flex gap-3">
            <button
              onClick={() => handleExport('excel')}
              className="flex items-center gap-2 bg-emerald-600 hover:bg-emerald-700 text-white px-4 py-2.5 rounded-xl font-semibold text-sm transition-all shadow-sm hover:shadow-md"
            >
              <FileSpreadsheet size={16} /> Export Excel
            </button>
            <button
              onClick={() => handleExport('pdf')}
              className="flex items-center gap-2 bg-rose-600 hover:bg-rose-700 text-white px-4 py-2.5 rounded-xl font-semibold text-sm transition-all shadow-sm hover:shadow-md"
            >
              <FileText size={16} /> Export PDF
            </button>
          </div>
        </div>
      </div>

      <div className="p-8 space-y-8 flex-grow">
        
        {/* KPI Cards */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.1 }} className="bg-white p-6 rounded-3xl shadow-sm border border-gray-100">
            <div className="flex items-center justify-between mb-4">
              <div className="w-12 h-12 bg-green-50 text-green-600 rounded-2xl flex items-center justify-center">
                <TrendingUp size={24} />
              </div>
              <span className="flex items-center gap-1 text-green-600 font-bold text-sm bg-green-50 px-2 py-1 rounded-full">
                <ArrowUpRight size={14} /> 12%
              </span>
            </div>
            <p className="text-gray-500 text-sm font-semibold uppercase tracking-wider mb-1">Total Saved (YTD)</p>
            <h2 className="text-3xl font-bold text-gray-800">$485.50</h2>
          </motion.div>

          <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.2 }} className="bg-white p-6 rounded-3xl shadow-sm border border-gray-100">
            <div className="flex items-center justify-between mb-4">
              <div className="w-12 h-12 bg-blue-50 text-blue-600 rounded-2xl flex items-center justify-center">
                <DollarSign size={24} />
              </div>
              <span className="flex items-center gap-1 text-red-500 font-bold text-sm bg-red-50 px-2 py-1 rounded-full">
                <ArrowDownRight size={14} /> 4%
              </span>
            </div>
            <p className="text-gray-500 text-sm font-semibold uppercase tracking-wider mb-1">Total Spent</p>
            <h2 className="text-3xl font-bold text-gray-800">$1,250.00</h2>
          </motion.div>

          <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.3 }} className="bg-white p-6 rounded-3xl shadow-sm border border-gray-100 bg-gradient-to-br from-brand to-brand-dark text-white border-none relative overflow-hidden">
             <div className="absolute -right-10 -top-10 w-32 h-32 bg-white opacity-10 rounded-full blur-2xl"></div>
             <div className="flex items-center justify-between mb-4 relative z-10">
              <div className="w-12 h-12 bg-white/20 text-white rounded-2xl flex items-center justify-center">
                <Activity size={24} />
              </div>
            </div>
            <p className="text-white/80 text-sm font-semibold uppercase tracking-wider mb-1 relative z-10">Avg Delta per Order</p>
            <h2 className="text-3xl font-bold text-white relative z-10">$4.20</h2>
          </motion.div>
        </div>

        {/* Charts Row */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 h-[400px]">
          
          <motion.div initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }} transition={{ delay: 0.4 }} className="lg:col-span-2 bg-white p-6 rounded-3xl shadow-sm border border-gray-100 flex flex-col">
            <h3 className="font-bold text-gray-800 mb-6 tracking-tight text-lg">Cross-Platform Price Delta</h3>
            <div className="flex-grow w-full h-full">
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={savingsData} margin={{ top: 5, right: 30, left: 0, bottom: 5 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" vertical={false} />
                  <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{fill: '#94a3b8', fontSize: 12}} />
                  <YAxis axisLine={false} tickLine={false} tick={{fill: '#94a3b8', fontSize: 12}} tickFormatter={(value) => `$${value}`} />
                  <Tooltip 
                    contentStyle={{ borderRadius: '16px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }}
                  />
                  <Legend iconType="circle" wrapperStyle={{ fontSize: '12px', paddingTop: '20px' }} />
                  <Line type="monotone" name="Highest Price Avoided" dataKey="avoidedCost" stroke="#ef4444" strokeWidth={3} dot={{ r: 4, strokeWidth: 2 }} activeDot={{ r: 6 }} />
                  <Line type="monotone" name="What You Paid" dataKey="youPaid" stroke="#10b981" strokeWidth={3} dot={{ r: 4, strokeWidth: 2 }} activeDot={{ r: 6 }} />
                </LineChart>
              </ResponsiveContainer>
            </div>
          </motion.div>

          <motion.div initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }} transition={{ delay: 0.5 }} className="bg-white p-6 rounded-3xl shadow-sm border border-gray-100 flex flex-col">
             <h3 className="font-bold text-gray-800 mb-2 tracking-tight text-lg">Spending Distribution</h3>
             <div className="flex-grow flex items-center justify-center">
               <ResponsiveContainer width="100%" height={250}>
                  <PieChart>
                    <Pie
                      data={categoryData}
                      cx="50%"
                      cy="50%"
                      innerRadius={60}
                      outerRadius={80}
                      paddingAngle={5}
                      dataKey="value"
                      stroke="none"
                    >
                      {categoryData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={entry.color} />
                      ))}
                    </Pie>
                    <Tooltip contentStyle={{ borderRadius: '12px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }} />
                  </PieChart>
                </ResponsiveContainer>
             </div>
             <div className="grid grid-cols-1 gap-3 mt-4">
               {categoryData.map(cat => (
                 <div key={cat.name} className="flex justify-between items-center text-sm">
                   <div className="flex items-center gap-2">
                     <div className="w-3 h-3 rounded-full" style={{backgroundColor: cat.color}}></div>
                     <span className="text-gray-600 font-medium">{cat.name}</span>
                   </div>
                   <span className="font-bold text-gray-800">{cat.value}%</span>
                 </div>
               ))}
             </div>
          </motion.div>

        </div>

      </div>
    </div>
  );
};

export default AnalyticsDashboard;
