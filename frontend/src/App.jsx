import React, { useState, useEffect, useCallback } from 'react';
import Sidebar from './components/Sidebar';
import ChatEngine from './components/ChatEngine';
import ContextPanel from './components/ContextPanel';
import Landing from './components/Landing';
import AnalyticsDashboard from './components/AnalyticsDashboard';
import AuthPage from './components/AuthPage';
import OrderHistory from './components/OrderHistory';
import ConnectedAccounts from './components/ConnectedAccounts';
import PaymentVault from './components/PaymentVault';
import AppSettings from './components/AppSettings';

function App() {
  const [activeTab, setActiveTab] = useState('chat');
  const [hasStarted, setHasStarted] = useState(false);
  const [auth, setAuth] = useState(null);

  const handleLogin = (user, token) => {
    setAuth({ token, user });
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setAuth(null);
    setHasStarted(false);
    setActiveTab('chat');
  };

  if (!auth) {
    return (
      <div className="flex h-screen bg-surface-muted overflow-hidden">
        <AuthPage onLogin={handleLogin} />
      </div>
    );
  }

  if (!hasStarted) {
    return (
      <div className="flex h-screen bg-surface-muted overflow-hidden">
        <Landing onStart={() => setHasStarted(true)} user={auth.user} onLogout={handleLogout} />
      </div>
    );
  }

  return (
    <div className="flex h-screen bg-surface-muted overflow-hidden">
      <Sidebar activeTab={activeTab} setActiveTab={setActiveTab} user={auth.user} onLogout={handleLogout} />
      <main className="flex-grow flex flex-col h-full bg-white shadow-neomorphic z-10 m-2 rounded-2xl overflow-hidden relative">
        {activeTab === 'analytics' && <AnalyticsDashboard />}
        {activeTab === 'history' && <OrderHistory />}
        {activeTab === 'wallet' && <PaymentVault />}
        {activeTab === 'profile' && <ConnectedAccounts user={auth.user} />}
        {activeTab === 'settings' && <AppSettings user={auth.user} />}
        {activeTab === 'chat' && <ChatEngine serverOk={true} token={auth.token} />}
      </main>
      <aside className="hidden lg:flex w-96 flex-col bg-surface h-full shadow-sm m-2 ml-0 rounded-2xl overflow-hidden">
        <ContextPanel />
      </aside>
    </div>
  );
}

export default App;
