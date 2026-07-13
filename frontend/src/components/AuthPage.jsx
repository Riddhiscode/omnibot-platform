import React, { useState } from 'react';
import { motion } from 'framer-motion';
import { Mail, Lock, User, Phone, Eye, EyeOff } from 'lucide-react';

const API = 'http://localhost:8080/api';

const AuthPage = ({ onLogin }) => {
  const [isLogin, setIsLogin] = useState(true);
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [form, setForm] = useState({
    fullName: '',
    email: '',
    password: '',
    phone: ''
  });

  const update = (field, val) => {
    setForm(prev => ({ ...prev, [field]: val }));
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      if (isLogin) {
        const res = await fetch(`${API}/auth/login`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ email: form.email, password: form.password })
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.message || 'Login failed');
        localStorage.setItem('token', data.accessToken);
        localStorage.setItem('refreshToken', data.refreshToken);
        localStorage.setItem('user', JSON.stringify(data.user));
        onLogin(data.user, data.accessToken);
      } else {
        const res = await fetch(`${API}/auth/register`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            fullName: form.fullName,
            email: form.email,
            password: form.password,
            phone: form.phone
          })
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.message || 'Registration failed');
        setIsLogin(true);
        setError('');
        alert('Account created! Please login.');
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex items-center justify-center min-h-screen bg-surface-muted p-4">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="w-full max-w-md"
      >
        {/* Logo */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center gap-2 mb-4">
            <div className="w-10 h-10 bg-brand rounded-xl flex items-center justify-center">
              <span className="text-white font-bold text-lg">O</span>
            </div>
            <span className="text-2xl font-bold text-gray-800">OmniBot</span>
          </div>
          <p className="text-gray-500 text-sm">
            {isLogin ? 'Welcome back! Sign in to continue.' : 'Create your account to get started.'}
          </p>
        </div>

        {/* Card */}
        <div className="bg-white rounded-3xl shadow-lg border border-gray-100 p-8">
          <h2 className="text-xl font-bold text-gray-800 mb-6">
            {isLogin ? 'Sign In' : 'Create Account'}
          </h2>

          {error && (
            <div className="bg-red-50 text-red-600 text-sm px-4 py-3 rounded-xl mb-4 border border-red-100">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            {!isLogin && (
              <div className="relative">
                <User size={16} className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" />
                <input
                  type="text"
                  value={form.fullName}
                  onChange={(e) => update('fullName', e.target.value)}
                  placeholder="Full Name"
                  required
                  className="w-full bg-gray-50 border border-gray-200 rounded-xl py-3 pl-11 pr-4 text-sm focus:outline-none focus:ring-2 focus:ring-brand/30 focus:bg-white transition-all"
                />
              </div>
            )}

            <div className="relative">
              <Mail size={16} className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" />
              <input
                type="email"
                value={form.email}
                onChange={(e) => update('email', e.target.value)}
                placeholder="Email address"
                required
                className="w-full bg-gray-50 border border-gray-200 rounded-xl py-3 pl-11 pr-4 text-sm focus:outline-none focus:ring-2 focus:ring-brand/30 focus:bg-white transition-all"
              />
            </div>

            <div className="relative">
              <Lock size={16} className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" />
              <input
                type={showPassword ? 'text' : 'password'}
                value={form.password}
                onChange={(e) => update('password', e.target.value)}
                placeholder="Password (min 8 characters)"
                required
                minLength={8}
                className="w-full bg-gray-50 border border-gray-200 rounded-xl py-3 pl-11 pr-11 text-sm focus:outline-none focus:ring-2 focus:ring-brand/30 focus:bg-white transition-all"
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
              >
                {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
              </button>
            </div>

            {!isLogin && (
              <div className="relative">
                <Phone size={16} className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" />
                <input
                  type="tel"
                  value={form.phone}
                  onChange={(e) => update('phone', e.target.value)}
                  placeholder="Phone (10-digit Indian number)"
                  pattern="^[6-9]\d{9}$"
                  className="w-full bg-gray-50 border border-gray-200 rounded-xl py-3 pl-11 pr-4 text-sm focus:outline-none focus:ring-2 focus:ring-brand/30 focus:bg-white transition-all"
                />
              </div>
            )}

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-brand text-white py-3 rounded-xl font-bold hover:bg-brand-dark transition-colors disabled:opacity-50"
            >
              {loading ? 'Please wait...' : isLogin ? 'Sign In' : 'Create Account'}
            </button>
          </form>

          <div className="mt-6 text-center">
            <button
              onClick={() => { setIsLogin(!isLogin); setError(''); }}
              className="text-sm text-brand hover:text-brand-dark font-medium transition-colors"
            >
              {isLogin ? "Don't have an account? Sign up" : 'Already have an account? Sign in'}
            </button>
          </div>

          {isLogin && (
            <div className="mt-4 p-3 bg-blue-50 rounded-xl border border-blue-100">
              <p className="text-xs text-blue-600 font-medium">Demo credentials:</p>
              <p className="text-xs text-blue-500 mt-1">admin@omnibot.in / Admin@12345</p>
            </div>
          )}
        </div>
      </motion.div>
    </div>
  );
};

export default AuthPage;
