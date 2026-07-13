import React, { useState, useRef, useEffect, useCallback } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Send, Mic, ExternalLink, Zap, Image as ImageIcon, X, RotateCcw } from 'lucide-react';

const ChatEngine = ({ serverOk = true, token }) => {
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
  const [selectedImage, setSelectedImage] = useState(null);
  const [imagePreview, setImagePreview] = useState(null);
  const messagesEndRef = useRef(null);
  const fileInputRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, isTyping]);

  const handleImageSelect = (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    if (!file.type.startsWith('image/')) {
      alert('Only image files are supported.');
      return;
    }
    if (file.size > 10 * 1024 * 1024) {
      alert('Image must be under 10MB.');
      return;
    }
    setSelectedImage(file);
    const reader = new FileReader();
    reader.onload = (ev) => setImagePreview(ev.target.result);
    reader.readAsDataURL(file);
  };

  const removeImage = () => {
    setSelectedImage(null);
    setImagePreview(null);
    if (fileInputRef.current) fileInputRef.current.value = '';
  };

  const sendMessage = useCallback(async (text, imageDataUrl = null) => {
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
      if (imageDataUrl) {
        body.imageData = imageDataUrl;
      }

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
        content: data.reply || 'Sorry, I encountered an error parsing that.',
        timestamp: new Date().toISOString()
      };
      setMessages(prev => [...prev, botReply]);

      if (data.services && data.services.length > 0) {
        const parseNum = (s) => parseInt((s || '').replace(/[^0-9]/g, '') || '9999');
        const options = data.services.map((svc, idx) => ({
          id: String(idx),
          vendor: svc.name,
          logoUrl: svc.logo,
          price: svc.price,
          eta: svc.estimate,
          action: svc.action,
          rating: svc.rating,
          isCheapest: false,
          isFastest: false
        }));
        const cheapIdx = options.reduce((mi, o, i) => parseNum(o.price) < parseNum(options[mi].price) ? i : mi, 0);
        const fastIdx = options.reduce((mi, o, i) => parseNum(o.eta) < parseNum(options[mi].eta) ? i : mi, 0);
        options[cheapIdx].isCheapest = true;
        options[fastIdx].isFastest = true;

        setMessages(prev => [...prev, {
          id: Date.now() + 2,
          role: 'bot',
          type: 'deal_card',
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
        content: 'Server unreachable. Check if backend is running.',
        failedMessage: text,
        failedImage: imageDataUrl,
        timestamp: new Date().toISOString()
      }]);
    }
  }, []);

  const handleSend = async (e) => {
    e.preventDefault();
    const text = inputValue.trim();
    if (!text && !selectedImage) return;

    const imageData = imagePreview;
    setInputValue('');
    removeImage();

    await sendMessage(text, imageData);
  };

  const handleRetry = async (msg) => {
    setMessages(prev => prev.filter(m => m.id !== msg.id));
    await sendMessage(msg.failedMessage, msg.failedImage);
  };

  const handlePlaceOrder = async (vendor, price) => {
    try {
      const response = await fetch('http://localhost:8080/api/v1/orders/place', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
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
        <div className={`w-2 h-2 rounded-full animate-pulse mr-3 ${serverOk ? 'bg-green-500' : 'bg-red-500'}`}></div>
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

              {msg.type === 'image' && (
                <div className="max-w-[80%] md:max-w-[70%] rounded-2xl shadow-sm overflow-hidden bg-brand text-white rounded-br-none">
                  {msg.imageUrl && (
                    <img src={msg.imageUrl} alt="Uploaded" className="w-full max-h-60 object-cover" />
                  )}
                  {msg.content && (
                    <div className="p-4 text-sm">{msg.content}</div>
                  )}
                </div>
              )}

              {msg.type === 'error' && (
                <div className="max-w-[80%] p-4 rounded-2xl shadow-sm text-sm bg-red-50 text-red-700 rounded-bl-none border border-red-200 flex items-start gap-3">
                  <div className="flex-1">
                    <p className="font-medium">Connection Error</p>
                    <p className="text-red-500 text-xs mt-1">{msg.content}</p>
                  </div>
                  <button
                    onClick={() => handleRetry(msg)}
                    className="p-1.5 rounded-lg bg-red-100 hover:bg-red-200 text-red-600 transition-colors flex-shrink-0"
                    title="Retry"
                  >
                    <RotateCcw size={14} />
                  </button>
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
                            {opt.isCheapest && <span className="bg-green-100 text-green-700 text-[10px] px-2 py-0.5 rounded-full font-bold">Cheapest</span>}
                            {opt.isFastest && <span className="bg-blue-100 text-blue-600 text-[10px] px-2 py-0.5 rounded-full font-bold">Fastest</span>}
                            {opt.rating && <span className="text-[10px] text-gray-400">&#9733; {parseFloat(opt.rating).toFixed(1)}</span>}
                          </div>
                          <p className="text-xs text-gray-500 mt-1">{opt.eta}</p>
                        </div>
                        <div className="flex items-center gap-4">
                          <span className="font-bold text-lg">{typeof opt.price === 'number' ? `₹${opt.price.toFixed(0)}` : opt.price}</span>
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

      {/* Image Preview */}
      {imagePreview && (
        <div className="px-4 pb-2">
          <div className="relative inline-block">
            <img src={imagePreview} alt="Preview" className="h-20 rounded-lg border border-gray-200 object-cover" />
            <button
              onClick={removeImage}
              className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full p-0.5 hover:bg-red-600 transition-colors"
            >
              <X size={12} />
            </button>
          </div>
        </div>
      )}

      {/* Input Area */}
      <div className="p-4 bg-white border-t border-gray-100">
        <form onSubmit={handleSend} className="relative flex items-center">
          <input
            ref={fileInputRef}
            type="file"
            accept="image/*"
            onChange={handleImageSelect}
            className="hidden"
          />
          <button
            type="button"
            onClick={() => fileInputRef.current?.click()}
            className="absolute left-4 text-gray-400 hover:text-brand transition-colors"
            title="Attach image"
          >
            <ImageIcon size={20} />
          </button>
          <input
            type="text"
            value={inputValue}
            onChange={(e) => setInputValue(e.target.value)}
            placeholder={serverOk ? "Type a message or attach an image..." : "Server is offline..."}
            disabled={!serverOk}
            className="w-full bg-gray-50 border border-gray-200 text-gray-800 rounded-full py-4 pl-12 pr-14 focus:outline-none focus:ring-2 focus:ring-brand/30 focus:bg-white transition-all shadow-inner disabled:opacity-50 disabled:cursor-not-allowed"
          />
          <button 
            type="submit" 
            disabled={(!inputValue.trim() && !selectedImage) || !serverOk}
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
