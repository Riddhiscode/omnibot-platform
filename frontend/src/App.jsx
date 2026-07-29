import React, { useState } from 'react';
import Header from './components/Header';
import ChatEngine from './components/ChatEngine';
import ServiceSidebar from './components/ServiceSidebar';
import AuthPage from './components/AuthPage';
import AnalyticsDashboard from './components/AnalyticsDashboard';
import OrderHistory from './components/OrderHistory';
import ConnectedAccounts from './components/ConnectedAccounts';

function App() {
  const [activeTab, setActiveTab] = useState('chat');
  const [quickInput, setQuickInput] = useState('');
  const [auth, setAuth] = useState(() => {
    const token = localStorage.getItem('omnibot_token');
    const user = JSON.parse(localStorage.getItem('omnibot_user') || 'null');
    return token ? { token, user: user || { name: 'angel', email: 'angel@omnibot.ai' } } : null;
  });

  const handleLogin = (user, token) => {
    localStorage.setItem('omnibot_token', token);
    localStorage.setItem('omnibot_user', JSON.stringify(user));
    setAuth({ token, user });
  };

  const handleLogout = () => {
    localStorage.removeItem('omnibot_token');
    localStorage.removeItem('omnibot_user');
    setAuth(null);
    setActiveTab('chat');
  };

  const handleQuickSend = (text) => {
    setActiveTab('chat');
    setQuickInput(text);
  };

  if (!auth) {
    return (
      <div className="h-screen w-screen bg-[#0b0e14]">
        <AuthPage onLogin={handleLogin} />
      </div>
    );
  }

  return (
    <div className="flex flex-col h-screen w-screen bg-[#0b0e14] overflow-hidden">
      {/* Top Fixed Header */}
      <Header 
        user={auth.user} 
        onLogout={handleLogout} 
        onQuickSend={handleQuickSend} 
        activeTab={activeTab}
        setActiveTab={setActiveTab}
      />

      {/* Main Content Area */}
      <div className="flex flex-grow h-[calc(100vh-3.5rem)] overflow-hidden relative">
        {activeTab === 'chat' && (
          <>
            {/* 80% Chat Column */}
            <main className="flex-grow h-full overflow-hidden">
              <ChatEngine 
                serverOk={true} 
                token={auth.token} 
                quickInput={quickInput}
                onClearQuickInput={() => setQuickInput('')}
              />
            </main>
            {/* 20% Service Directory Sidebar */}
            <ServiceSidebar onQuickSend={handleQuickSend} />
          </>
        )}

        {activeTab === 'analytics' && (
          <div className="w-full h-full overflow-y-auto bg-white">
            <AnalyticsDashboard token={auth.token} />
          </div>
        )}

        {activeTab === 'history' && (
          <div className="w-full h-full overflow-y-auto bg-white">
            <OrderHistory token={auth.token} />
          </div>
        )}

        {activeTab === 'profile' && (
          <div className="w-full h-full overflow-y-auto bg-white">
            <ConnectedAccounts user={auth.user} token={auth.token} />
          </div>
        )}
      </div>
    </div>
  );
}

export default App;
