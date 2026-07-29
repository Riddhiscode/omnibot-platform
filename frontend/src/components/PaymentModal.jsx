import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, CreditCard, Smartphone, CheckCircle, Loader2 } from 'lucide-react';

const PaymentModal = ({ isOpen, onClose, orderDetails, onConfirmPayment }) => {
  const [selectedMethod, setSelectedMethod] = useState('card');
  const [isProcessing, setIsProcessing] = useState(false);
  const [isSuccess, setIsSuccess] = useState(false);

  if (!isOpen || !orderDetails) return null;

  const handlePay = () => {
    setIsProcessing(true);
    
    // Simulate network delay for payment gateway
    setTimeout(() => {
      setIsProcessing(false);
      setIsSuccess(true);
      
      // Keep success screen for 1.5s then trigger parent callback
      setTimeout(() => {
        setIsSuccess(false);
        onConfirmPayment(orderDetails.vendor, orderDetails.price);
        onClose();
      }, 1500);
      
    }, 2000);
  };

  return (
    <AnimatePresence>
      <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-gray-900/40 backdrop-blur-sm">
        <motion.div
          initial={{ opacity: 0, scale: 0.95, y: 20 }}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          exit={{ opacity: 0, scale: 0.95, y: 20 }}
          className="bg-white rounded-3xl shadow-xl w-full max-w-md overflow-hidden relative"
        >
          {/* Header */}
          <div className="px-6 py-4 border-b border-gray-100 flex justify-between items-center bg-gray-50/50">
            <h3 className="font-bold text-gray-800">Secure Checkout</h3>
            {!isProcessing && !isSuccess && (
              <button onClick={onClose} className="p-1 hover:bg-gray-200 rounded-full text-gray-500 transition-colors">
                <X size={20} />
              </button>
            )}
          </div>

          <div className="p-6">
            {isSuccess ? (
              <div className="py-12 flex flex-col items-center justify-center text-center">
                <motion.div
                  initial={{ scale: 0 }}
                  animate={{ scale: 1 }}
                  transition={{ type: "spring", stiffness: 200, damping: 15 }}
                >
                  <CheckCircle size={64} className="text-green-500 mb-4" />
                </motion.div>
                <h2 className="text-2xl font-bold text-gray-800 mb-2">Payment Successful!</h2>
                <p className="text-gray-500">Redirecting to chat...</p>
              </div>
            ) : isProcessing ? (
              <div className="py-12 flex flex-col items-center justify-center text-center">
                <Loader2 size={48} className="text-brand animate-spin mb-6" />
                <h2 className="text-xl font-bold text-gray-800 mb-2">Processing Payment...</h2>
                <p className="text-gray-500">Please do not close this window.</p>
              </div>
            ) : (
              <>
                {/* Order Summary */}
                <div className="bg-gray-50 rounded-2xl p-4 mb-6 border border-gray-100">
                  <p className="text-sm text-gray-500 mb-1">Paying to</p>
                  <h4 className="font-bold text-lg text-gray-800 mb-3">{orderDetails.vendor}</h4>
                  <div className="flex justify-between items-end">
                    <span className="text-gray-600">Total Amount</span>
                    <span className="text-3xl font-black text-gray-900">
                      ₹{orderDetails.price}
                    </span>
                  </div>
                </div>

                {/* Payment Methods */}
                <h4 className="font-semibold text-gray-700 mb-3 text-sm uppercase tracking-wider">Payment Method</h4>
                <div className="space-y-3 mb-8">
                  <label className={`flex items-center p-4 border rounded-xl cursor-pointer transition-all ${
                    selectedMethod === 'card' ? 'border-brand bg-brand/5 ring-1 ring-brand' : 'border-gray-200 hover:border-gray-300'
                  }`}>
                    <input 
                      type="radio" 
                      name="payment_method" 
                      className="hidden" 
                      checked={selectedMethod === 'card'} 
                      onChange={() => setSelectedMethod('card')} 
                    />
                    <CreditCard className={`mr-4 ${selectedMethod === 'card' ? 'text-brand' : 'text-gray-400'}`} />
                    <div className="flex-grow">
                      <p className="font-bold text-gray-800">Credit / Debit Card</p>
                      <p className="text-xs text-gray-500">Visa, Mastercard, RuPay</p>
                    </div>
                    <div className={`w-5 h-5 rounded-full border-2 flex items-center justify-center ${selectedMethod === 'card' ? 'border-brand' : 'border-gray-300'}`}>
                      {selectedMethod === 'card' && <div className="w-2.5 h-2.5 rounded-full bg-brand" />}
                    </div>
                  </label>

                  <label className={`flex items-center p-4 border rounded-xl cursor-pointer transition-all ${
                    selectedMethod === 'upi' ? 'border-brand bg-brand/5 ring-1 ring-brand' : 'border-gray-200 hover:border-gray-300'
                  }`}>
                    <input 
                      type="radio" 
                      name="payment_method" 
                      className="hidden" 
                      checked={selectedMethod === 'upi'} 
                      onChange={() => setSelectedMethod('upi')} 
                    />
                    <Smartphone className={`mr-4 ${selectedMethod === 'upi' ? 'text-brand' : 'text-gray-400'}`} />
                    <div className="flex-grow">
                      <p className="font-bold text-gray-800">UPI</p>
                      <p className="text-xs text-gray-500">Google Pay, PhonePe, Paytm</p>
                    </div>
                    <div className={`w-5 h-5 rounded-full border-2 flex items-center justify-center ${selectedMethod === 'upi' ? 'border-brand' : 'border-gray-300'}`}>
                      {selectedMethod === 'upi' && <div className="w-2.5 h-2.5 rounded-full bg-brand" />}
                    </div>
                  </label>
                </div>

                {/* Pay Button */}
                <button 
                  onClick={handlePay}
                  className="w-full bg-gray-900 text-white font-bold py-4 rounded-xl hover:bg-gray-800 transition-colors shadow-md flex justify-center items-center gap-2"
                >
                  Pay ₹{orderDetails.price} Securely
                </button>
                <div className="text-center mt-4 text-xs text-gray-400 flex items-center justify-center gap-1">
                  <span>🔒 Secured by OmniBot Payments</span>
                </div>
              </>
            )}
          </div>
        </motion.div>
      </div>
    </AnimatePresence>
  );
};

export default PaymentModal;
