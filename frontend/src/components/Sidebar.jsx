import React from 'react';
import { Home, MessageSquare, History, Wallet, User, Settings, Compass } from 'lucide-react';

const Sidebar = ({ activeTab, setActiveTab }) => {
  const menuItems = [
    { id: 'chat', icon: MessageSquare, label: 'Smart Assistant' },
    { id: 'history', icon: History, label: 'Order History' },
    { id: 'analytics', icon: Compass, label: 'Savings & Analytics' },
    { id: 'wallet', icon: Wallet, label: 'Payment Vault' },
    { id: 'profile', icon: User, label: 'Connected Accounts' },
    { id: 'settings', icon: Settings, label: 'Settings' },
  ];

  return (
    <aside className="w-20 lg:w-64 flex flex-col bg-surface h-full m-2 mr-0 rounded-2xl shadow-sm transition-all duration-300">
      <div className="flex items-center justify-center lg:justify-start lg:px-6 h-20 border-b border-gray-100">
        <div className="bg-brand text-white p-2 rounded-xl flex items-center justify-center shadow-lg shadow-brand/30">
          <Home size={24} />
        </div>
        <span className="hidden lg:block ml-3 font-bold text-xl tracking-tight text-gray-800">
          OmniBot
        </span>
      </div>

      <nav className="flex-grow py-6 flex flex-col gap-2 px-3">
        {menuItems.map((item) => (
          <button
            key={item.id}
            onClick={() => setActiveTab(item.id)}
            className={`flex items-center px-3 py-3 rounded-xl transition-all group ${
              activeTab === item.id
                ? 'bg-brand/10 text-brand font-medium'
                : 'text-gray-500 hover:bg-gray-50 hover:text-gray-800'
            }`}
          >
            <item.icon
              size={22}
              className={`${
                activeTab === item.id ? 'text-brand' : 'text-gray-400 group-hover:text-gray-600'
              } transition-colors`}
            />
            <span className="hidden lg:block ml-4 text-sm font-medium">{item.label}</span>
          </button>
        ))}
      </nav>

      <div className="p-4 border-t border-gray-100">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-full bg-gradient-to-tr from-brand to-accent flex items-center justify-center text-white font-bold shadow-md">
            R
          </div>
          <div className="hidden lg:block flex-grow overflow-hidden">
            <h4 className="text-sm font-bold text-gray-800 truncate">Riddhi</h4>
            <p className="text-xs text-gray-500 truncate">Premium Member</p>
          </div>
        </div>
      </div>
    </aside>
  );
};

export default Sidebar;
