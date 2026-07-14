import React, { useState } from 'react';
import { AnimatePresence, motion } from 'framer-motion';
import Sidebar            from './components/Sidebar';
import ChatEngine         from './components/ChatEngine';
import ContextPanel       from './components/ContextPanel';
import Landing            from './components/Landing';
import AnalyticsDashboard from './components/AnalyticsDashboard';
import AuthPage           from './components/AuthPage';
import OrderHistory       from './components/OrderHistory';
import PaymentVault       from './components/PaymentVault';
import ConnectedAccounts  from './components/ConnectedAccounts';
import AppSettings        from './components/AppSettings';

/* Slide-in animation for tab panels */
const tabVariants = {
  initial: { opacity: 0, y: 12 },
  animate: { opacity: 1, y: 0, transition: { duration: 0.22, ease: 'easeOut' } },
  exit:    { opacity: 0, y: -8, transition: { duration: 0.15 } },
};

function MainPanel({ activeTab, user, onLogout }) {
  const panels = {
    chat:      <ChatEngine />,
    history:   <OrderHistory />,
    analytics: <AnalyticsDashboard />,
    wallet:    <PaymentVault />,
    profile:   <ConnectedAccounts user={user} />,
    settings:  <AppSettings onLogout={onLogout} />,
  };

  return (
    <AnimatePresence mode="wait">
      <motion.div
        key={activeTab}
        variants={tabVariants}
        initial="initial"
        animate="animate"
        exit="exit"
        className="flex flex-col h-full w-full overflow-hidden"
      >
        {panels[activeTab] ?? <ChatEngine />}
      </motion.div>
    </AnimatePresence>
  );
}

function App() {
  const [activeTab,  setActiveTab]  = useState('chat');
  const [hasStarted, setHasStarted] = useState(false);
  const [auth,       setAuth]       = useState(null);

  const handleLogin  = (user, token) => setAuth({ token, user });
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

  /* Show ContextPanel only on chat tab (it's chat-specific) */
  const showContext = activeTab === 'chat';

  return (
    <div className="flex h-screen bg-surface-muted overflow-hidden">
      <Sidebar activeTab={activeTab} setActiveTab={setActiveTab}
               user={auth.user} onLogout={handleLogout} />

      <main className="flex-grow flex flex-col h-full bg-white shadow-neomorphic z-10 m-2 rounded-2xl overflow-hidden relative">
        <MainPanel activeTab={activeTab} user={auth.user} onLogout={handleLogout} />
      </main>

      {showContext && (
        <aside className="hidden lg:flex w-96 flex-col bg-surface h-full shadow-sm m-2 ml-0 rounded-2xl overflow-hidden">
          <ContextPanel />
        </aside>
      )}
    </div>
  );
}

export default App;
