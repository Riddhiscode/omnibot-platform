import React, { useState, useRef, useEffect, useCallback } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Send, Image as ImageIcon, X, RotateCcw } from 'lucide-react';
import PaymentModal from './PaymentModal';

const ChatEngine = ({ serverOk = true, token, quickInput, onClearQuickInput }) => {
  const [messages, setMessages] = useState([
    {
      id: 1,
      role: 'bot',
      type: 'text',
      content: '🍕 Hungry? I can find the best food, rides, or grocery deals for you across all connected apps.',
      timestamp: new Date().toISOString()
    }
  ]);
  const [inputValue, setInputValue] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const [selectedImage, setSelectedImage] = useState(null);
  const [imagePreview, setImagePreview] = useState(null);
  const [activeCheckout, setActiveCheckout] = useState(null);
  const messagesEndRef = useRef(null);
  const fileInputRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, isTyping]);

  const sendMessage = useCallback(async (text, imageDataUrl = null) => {
    if (!text && !imageDataUrl) return;

    const userMsg = {
      id: Date.now(),
      role: 'user',
      type: imageDataUrl ? 'image' : 'text',
      content: text,
      imageUrl: imageDataUrl,
      timestamp: new Date().toISOString()
    };

    setMessages(prev => [...prev, userMsg]);
    setIsTyping(true);

    try {
      const body = { message: text, sessionId: 'session-123' };
      if (imageDataUrl) body.imageData = imageDataUrl;

      const response = await fetch('http://localhost:8080/api/v1/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
        body: JSON.stringify(body)
      });

      if (!response.ok) throw new Error(`Server returned ${response.status}`);

      const data = await response.json();
      setIsTyping(false);

      const botReply = {
        id: Date.now() + 1,
        role: 'bot',
        type: 'text',
        content: data.reply || 'Here are the options I found for you:',
        timestamp: new Date().toISOString()
      };
      setMessages(prev => [...prev, botReply]);

      if (data.services && data.services.length > 0) {
        const options = data.services.map((svc, idx) => ({
          id: String(idx),
          vendor: svc.name,
          logoUrl: svc.logo,
          price: svc.price,
          eta: svc.estimate,
          action: svc.action,
          rating: svc.rating || '4.2',
        }));

        setMessages(prev => [...prev, {
          id: Date.now() + 2,
          role: 'bot',
          type: 'deal_cards',
          data: { category: data.intent || 'Options', options },
          timestamp: new Date().toISOString()
        }]);
      }
    } catch (error) {
      console.error("Error communicating with backend:", error);
      setIsTyping(false);
      setMessages(prev => [...prev, {
        id: Date.now() + 1,
        role: 'bot',
        type: 'error',
        content: 'Server unreachable. Check if backend is running on 8080.',
        failedMessage: text,
        failedImage: imageDataUrl,
        timestamp: new Date().toISOString()
      }]);
    }
  }, [token]);

  // Handle external quick inputs (from Header pills or Sidebar clicks)
  useEffect(() => {
    if (quickInput) {
      sendMessage(quickInput);
      onClearQuickInput?.();
    }
  }, [quickInput, sendMessage, onClearQuickInput]);

  const handleSend = async (e) => {
    e.preventDefault();
    const text = inputValue.trim();
    if (!text && !selectedImage) return;

    const imageData = imagePreview;
    setInputValue('');
    setSelectedImage(null);
    setImagePreview(null);
    if (fileInputRef.current) fileInputRef.current.value = '';

    await sendMessage(text, imageData);
  };

  const handleSelectVendorCard = (vendor, priceStr) => {
    const numericPrice = typeof priceStr === 'number' 
      ? priceStr 
      : parseFloat((priceStr || '').replace(/[^0-9.]/g, '') || '150');
    
    setActiveCheckout({ vendor, price: numericPrice });
  };

  const confirmPayment = async (vendor, price) => {
    try {
      const response = await fetch('http://localhost:8080/api/v1/aggregator/order', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
        body: JSON.stringify({ vendorName: vendor, amount: price, category: 'FOOD' })
      });
      const result = await response.json();
      setMessages(prev => [...prev, {
        id: Date.now(),
        role: 'bot',
        type: 'text',
        content: `🎉 Order placed successfully with ${vendor}! Tracking ID: ${result.orderId || 'OMNI-7892'}`,
        timestamp: new Date().toISOString()
      }]);
    } catch (error) {
      console.error("Order failed:", error);
    }
  };

  const formatTime = (isoString) => {
    const d = isoString ? new Date(isoString) : new Date();
    return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: true }).toLowerCase();
  };

  return (
    <div className="flex flex-col h-full bg-[#0b0e14] text-gray-100 relative select-none">
      {/* Messages Feed */}
      <div className="flex-grow overflow-y-auto px-6 py-6 space-y-6">
        <AnimatePresence initial={false}>
          {messages.map((msg) => (
            <motion.div
              key={msg.id}
              initial={{ opacity: 0, y: 8 }}
              animate={{ opacity: 1, y: 0 }}
              className={`flex gap-3 max-w-4xl ${msg.role === 'user' ? 'ml-auto flex-row-reverse' : 'mr-auto'}`}
            >
              {/* Avatar */}
              <div className={`w-8 h-8 rounded-full flex items-center justify-center font-bold text-xs shrink-0 ${
                msg.role === 'user' ? 'bg-indigo-600 text-white' : 'bg-cyan-500/20 text-cyan-400 border border-cyan-500/30'
              }`}>
                {msg.role === 'user' ? 'A' : 'O'}
              </div>

              {/* Message Content */}
              <div className="flex flex-col space-y-1 max-w-2xl">
                {msg.type === 'text' && (
                  <div className={`px-4 py-3 rounded-2xl text-sm leading-relaxed ${
                    msg.role === 'user'
                      ? 'bg-indigo-600 text-white rounded-tr-none'
                      : 'bg-[#161c28] border border-white/5 text-gray-200 rounded-tl-none'
                  }`}>
                    {msg.content}
                  </div>
                )}

                {msg.type === 'image' && (
                  <div className="rounded-2xl overflow-hidden bg-indigo-600 text-white rounded-tr-none border border-white/10">
                    {msg.imageUrl && (
                      <img src={msg.imageUrl} alt="Attachment" className="max-h-60 w-full object-cover" />
                    )}
                    {msg.content && <div className="p-3 text-sm">{msg.content}</div>}
                  </div>
                )}

                {msg.type === 'error' && (
                  <div className="p-3 rounded-xl bg-red-950/40 border border-red-500/30 text-red-300 text-xs flex items-center gap-2">
                    <span>⚠️ {msg.content}</span>
                  </div>
                )}

                {/* Bot Deal Cards (Horizontal Row) */}
                {msg.type === 'deal_cards' && (
                  <div className="flex gap-3 overflow-x-auto py-2 no-scrollbar max-w-full">
                    {msg.data.options.map((opt) => (
                      <div 
                        key={opt.id}
                        className="bg-[#141b26] border border-white/10 hover:border-indigo-500/40 rounded-xl p-3.5 w-44 shrink-0 flex flex-col justify-between shadow-lg transition-all"
                      >
                        <div>
                          {/* Vendor Name & Icon */}
                          <div className="flex items-center gap-2 mb-2">
                            <span className="text-base">{opt.logoUrl || '⚡'}</span>
                            <span className="font-bold text-white text-sm truncate">{opt.vendor}</span>
                          </div>

                          {/* Meta: ETA & Price */}
                          <div className="text-xs space-y-1 mb-2">
                            <div className="text-cyan-400 font-medium">{opt.eta}</div>
                            <div className="text-gray-300 font-semibold">{opt.price}</div>
                            <div className="text-amber-400 text-[11px] font-bold">★ {opt.rating}</div>
                          </div>
                        </div>

                        {/* Select Button */}
                        <button
                          onClick={() => handleSelectVendorCard(opt.vendor, opt.price)}
                          className="w-full mt-2 py-1.5 px-3 rounded-lg bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold transition-colors text-center"
                        >
                          Select {opt.vendor}
                        </button>
                      </div>
                    ))}
                  </div>
                )}

                {/* Timestamp */}
                <span className={`text-[10px] text-gray-500 px-1 ${msg.role === 'user' ? 'text-right' : 'text-left'}`}>
                  {formatTime(msg.timestamp)}
                </span>
              </div>
            </motion.div>
          ))}

          {isTyping && (
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="flex items-center gap-3">
              <div className="w-8 h-8 rounded-full bg-cyan-500/20 text-cyan-400 border border-cyan-500/30 flex items-center justify-center font-bold text-xs">
                O
              </div>
              <div className="bg-[#161c28] border border-white/5 px-4 py-3 rounded-2xl rounded-tl-none flex gap-1.5 items-center">
                <div className="w-2 h-2 rounded-full bg-cyan-400 animate-bounce" style={{ animationDelay: '0ms' }}></div>
                <div className="w-2 h-2 rounded-full bg-cyan-400 animate-bounce" style={{ animationDelay: '150ms' }}></div>
                <div className="w-2 h-2 rounded-full bg-cyan-400 animate-bounce" style={{ animationDelay: '300ms' }}></div>
              </div>
            </motion.div>
          )}
        </AnimatePresence>
        <div ref={messagesEndRef} />
      </div>

      {/* Input Field (Fixed Bottom) */}
      <div className="p-4 bg-[#0a0d14] border-t border-white/5 shrink-0">
        <form onSubmit={handleSend} className="max-w-4xl mx-auto flex items-center gap-2 bg-[#141a24] border border-white/10 rounded-2xl px-4 py-2 focus-within:border-indigo-500/50 transition-all">
          <input
            type="text"
            value={inputValue}
            onChange={(e) => setInputValue(e.target.value)}
            placeholder="Ask OmniBot anything — food, rides, shopping..."
            className="flex-grow bg-transparent text-sm text-gray-100 placeholder-gray-500 focus:outline-none"
          />
          <button
            type="submit"
            disabled={!inputValue.trim() && !selectedImage}
            className="w-9 h-9 rounded-xl bg-indigo-600 hover:bg-indigo-500 disabled:opacity-40 disabled:hover:bg-indigo-600 text-white flex items-center justify-center transition-colors shrink-0"
          >
            <Send size={16} />
          </button>
        </form>
      </div>

      {/* Payment Modal */}
      <PaymentModal 
        isOpen={!!activeCheckout}
        onClose={() => setActiveCheckout(null)}
        orderDetails={activeCheckout}
        onConfirmPayment={confirmPayment}
      />
    </div>
  );
};

export default ChatEngine;
