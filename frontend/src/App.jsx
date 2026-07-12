import React, { useState } from 'react';
import { Routes, Route } from 'react-router-dom';
import Sidebar from './components/Sidebar';
import ChatEngine from './components/ChatEngine';
import ContextPanel from './components/ContextPanel';
import Landing from './components/Landing';
import AnalyticsDashboard from './components/AnalyticsDashboard';

function App() {
  const [activeTab, setActiveTab] = useState('chat');
  const [hasStarted, setHasStarted] = useState(false);

  if (!hasStarted) {
    return (
      <div className="flex h-screen bg-surface-muted overflow-hidden">
        <Landing onStart={() => setHasStarted(true)} />
      </div>
    );
  }

  return (
    <div className="flex h-screen bg-surface-muted overflow-hidden">
      {/* Left Navigation Sidebar */}
      <Sidebar activeTab={activeTab} setActiveTab={setActiveTab} />
      
      {/* Main Interface Area */}
      <main className="flex-grow flex flex-col h-full bg-white shadow-neomorphic z-10 m-2 rounded-2xl overflow-hidden relative">
        {activeTab === 'analytics' ? <AnalyticsDashboard /> : <ChatEngine />}
      </main>

      {/* Right Contextual Dashboard Panel (Hidden on mobile, slides up as bottom sheet in future) */}
      <aside className="hidden lg:flex w-96 flex-col bg-surface h-full shadow-sm m-2 ml-0 rounded-2xl overflow-hidden">
        <ContextPanel />
      </aside>
    </div>
  );
}

export default App;
