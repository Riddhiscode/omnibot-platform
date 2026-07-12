import React, { useState, useRef, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Send, Mic, ExternalLink, Zap } from 'lucide-react';

const ChatEngine = () => {
  const [messages, setMessages] = useState([
    {
      id: 1,
      role: 'bot',
      type: 'text',
      content: 'Hi Riddhi! What can I help you with today? I can book rides, order food, or find grocery deals across all your favorite platforms.',
      timestamp: new Date().toISOString()
    }
  ]);
  const [inputValue, setInputValue] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, isTyping]);

  const handleSend = async (e) => {
    e.preventDefault();
    if (!inputValue.trim()) return;

    const userMsg = {
      id: Date.now(),
      role: 'user',
      type: 'text',
      content: inputValue,
      timestamp: new Date().toISOString()
    };

    setMessages(prev => [...prev, userMsg]);
    setInputValue('');
    setIsTyping(true);

    try {
      const response = await fetch('http://localhost:8080/api/v1/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message: userMsg.content, sessionId: 'session-123' })
      });
      
      const data = await response.json();
      setIsTyping(false);

      const botReply = {
        id: Date.now() + 1,
        role: 'bot',
        type: 'text',
        content: data.reply || 'Sorry, I encountered an error parsing that.',
        timestamp: new Date().toISOString()
      };
      
      setMessages(prev => [...prev, botReply]);

      if (data.services && data.services.length > 0) {
        // Map backend ServiceCard to frontend deal_card options
        const options = data.services.map((svc, idx) => ({
          id: String(idx),
          vendor: svc.vendorName,
          price: svc.price,
          eta: svc.eta,
          isCheapest: idx === 0, // Mock logic assuming sorted
          isFastest: svc.eta && svc.eta.includes('5') // Mock logic
        }));
        
        const dealCard = {
          id: Date.now() + 2,
          role: 'bot',
          type: 'deal_card',
          data: {
            category: data.intent.includes('TRANSPORT') ? 'Transport' : 'Food/Shopping',
            options: options
          },
          timestamp: new Date().toISOString()
        };
        setMessages(prev => [...prev, dealCard]);
      }
    } catch (error) {
      console.error("Error communicating with backend:", error);
      setIsTyping(false);
      setMessages(prev => [...prev, {
        id: Date.now() + 1,
        role: 'bot',
        type: 'text',
        content: 'Error connecting to server. Is the backend running?',
        timestamp: new Date().toISOString()
      }]);
    }
  };

  const handlePlaceOrder = async (vendor, price) => {
    try {
      const response = await fetch('http://localhost:8080/api/v1/orders/place', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ vendor, price, service_class: 'STANDARD' })
      });
      const result = await response.json();
      
      setMessages(prev => [...prev, {
        id: Date.now(),
        role: 'bot',
        type: 'text',
        content: `Success! I've placed the order with ${vendor}. Tracking URL: ${result.tracking_url}`,
        timestamp: new Date().toISOString()
      }]);
    } catch (error) {
      console.error("Order failed:", error);
    }
  };

  return (
    <div className="flex flex-col h-full bg-white relative">
      {/* Header */}
      <div className="h-16 border-b border-gray-100 flex items-center px-6 flex-shrink-0 bg-white/80 backdrop-blur-md z-10 sticky top-0">
        <div className="w-2 h-2 rounded-full bg-accent animate-pulse mr-3"></div>
        <h2 className="font-bold text-gray-800">OmniBot Assistant</h2>
      </div>

      {/* Messages Area */}
      <div className="flex-grow overflow-y-auto p-6 space-y-6">
        <AnimatePresence initial={false}>
          {messages.map((msg) => (
            <motion.div
              key={msg.id}
              initial={{ opacity: 0, y: 10, scale: 0.95 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}
            >
              {msg.type === 'text' && (
                <div
                  className={`max-w-[80%] md:max-w-[70%] p-4 rounded-2xl shadow-sm text-sm ${
                    msg.role === 'user'
                      ? 'bg-brand text-white rounded-br-none'
                      : 'bg-gray-50 text-gray-800 rounded-bl-none border border-gray-100'
                  }`}
                >
                  {msg.content}
                </div>
              )}
              
              {msg.type === 'deal_card' && (
                <div className="w-full max-w-md bg-white border border-gray-200 rounded-2xl shadow-sm overflow-hidden mt-2">
                  <div className="bg-gray-50 px-4 py-3 border-b border-gray-100 flex justify-between items-center">
                    <span className="font-bold text-gray-700 text-sm">{msg.data.category} Options</span>
                    <span className="text-xs text-gray-500 flex items-center gap-1">
                      <Zap size={12} className="text-accent" /> Live Rates
                    </span>
                  </div>
                  <div className="divide-y divide-gray-100">
                    {msg.data.options.map(opt => (
                      <div key={opt.id} className="p-4 flex items-center justify-between hover:bg-gray-50 transition-colors cursor-pointer group">
                        <div>
                          <div className="flex items-center gap-2">
                            <h4 className="font-bold text-gray-800">{opt.vendor}</h4>
                            {opt.isCheapest && <span className="bg-accent/10 text-accent text-[10px] px-2 py-0.5 rounded-full font-bold">Cheapest</span>}
                            {opt.isFastest && <span className="bg-blue-100 text-blue-600 text-[10px] px-2 py-0.5 rounded-full font-bold">Fastest</span>}
                          </div>
                          <p className="text-xs text-gray-500 mt-1">{opt.eta} away</p>
                        </div>
                        <div className="flex items-center gap-4">
                          <span className="font-bold text-lg">${opt.price.toFixed(2)}</span>
                          <button 
                            onClick={() => handlePlaceOrder(opt.vendor, opt.price)}
                            className="bg-brand text-white p-2 rounded-xl opacity-0 group-hover:opacity-100 transition-opacity"
                            title="Place Order"
                          >
                            <ExternalLink size={16} />
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </motion.div>
          ))}
          
          {isTyping && (
            <motion.div
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              className="flex justify-start"
            >
              <div className="bg-gray-50 border border-gray-100 p-4 rounded-2xl rounded-bl-none flex gap-2 items-center h-12">
                <div className="w-2 h-2 rounded-full bg-brand/40 animate-bounce" style={{ animationDelay: '0ms' }}></div>
                <div className="w-2 h-2 rounded-full bg-brand/60 animate-bounce" style={{ animationDelay: '150ms' }}></div>
                <div className="w-2 h-2 rounded-full bg-brand/80 animate-bounce" style={{ animationDelay: '300ms' }}></div>
              </div>
            </motion.div>
          )}
        </AnimatePresence>
        <div ref={messagesEndRef} />
      </div>

      {/* Input Area */}
      <div className="p-4 bg-white border-t border-gray-100">
        <form onSubmit={handleSend} className="relative flex items-center">
          <button type="button" className="absolute left-4 text-gray-400 hover:text-brand transition-colors">
            <Mic size={20} />
          </button>
          <input
            type="text"
            value={inputValue}
            onChange={(e) => setInputValue(e.target.value)}
            placeholder="E.g., Book me a cab to the airport..."
            className="w-full bg-gray-50 border border-gray-200 text-gray-800 rounded-full py-4 pl-12 pr-14 focus:outline-none focus:ring-2 focus:ring-brand/30 focus:bg-white transition-all shadow-inner"
          />
          <button 
            type="submit" 
            disabled={!inputValue.trim()}
            className="absolute right-2 bg-brand text-white p-2.5 rounded-full hover:bg-brand-dark transition-colors disabled:opacity-50 disabled:hover:bg-brand"
          >
            <Send size={18} className="ml-0.5" />
          </button>
        </form>
      </div>
    </div>
  );
};

export default ChatEngine;
