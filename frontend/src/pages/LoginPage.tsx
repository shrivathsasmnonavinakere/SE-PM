import React, { useState } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { HeartHandshake, Lock, Mail, ArrowRight, Loader2, Sparkles, Building, Utensils, ShieldAlert, Truck } from 'lucide-react';

export const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e?: React.FormEvent, customEmail?: string, customPass?: string) => {
    if (e) e.preventDefault();
    const targetEmail = customEmail || email;
    const targetPass = customPass || password;

    if (!targetEmail || !targetPass) {
      setError('Please enter both email and password');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const auth = await login({ email: targetEmail, password: targetPass });
      const from = (location.state as any)?.from?.pathname;

      if (from) {
        navigate(from, { replace: true });
      } else if (auth.role === 'ROLE_DONOR') {
        navigate('/donor/dashboard', { replace: true });
      } else if (auth.role === 'ROLE_RECEIVER') {
        navigate('/receiver/dashboard', { replace: true });
      } else if (auth.role === 'ROLE_VOLUNTEER') {
        navigate('/volunteer/dashboard', { replace: true });
      } else if (auth.role === 'ROLE_ADMIN') {
        navigate('/admin/dashboard', { replace: true });
      } else {
        navigate('/', { replace: true });
      }
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Login failed. Please check credentials.');
    } finally {
      setLoading(false);
    }
  };

  const handleQuickLogin = (demoEmail: string, demoPass: string) => {
    setEmail(demoEmail);
    setPassword(demoPass);
    handleSubmit(undefined, demoEmail, demoPass);
  };

  return (
    <div className="min-h-[85vh] flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8 bg-slate-50/70">
      <div className="max-w-md w-full space-y-6">
        <div className="text-center space-y-2">
          <div className="w-12 h-12 rounded-2xl bg-gradient-to-tr from-emerald-600 to-teal-500 text-white flex items-center justify-center mx-auto shadow-md shadow-emerald-500/20">
            <HeartHandshake className="w-7 h-7" />
          </div>
          <h2 className="text-2xl font-extrabold text-slate-900 tracking-tight">Welcome Back to AnnaDhata</h2>
          <p className="text-xs text-slate-500">Sign in to coordinate food redistribution</p>
        </div>

        {/* Demo Quick-Login Pills */}
        <div className="bg-emerald-50/60 p-4 rounded-2xl border border-emerald-100 space-y-2.5">
          <div className="flex items-center space-x-1.5 text-xs font-bold text-emerald-800">
            <Sparkles className="w-4 h-4 text-emerald-600" />
            <span>Instant Demo Logins (Click to autofill & sign in)</span>
          </div>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
            <button
              type="button"
              onClick={() => handleQuickLogin('spicegarden@restaurant.com', 'donor123')}
              className="px-2.5 py-2 bg-white rounded-xl text-left border border-emerald-200/80 hover:border-emerald-500 hover:shadow-sm transition"
            >
              <div className="flex items-center space-x-1 text-slate-800 font-bold text-[11px]">
                <Utensils className="w-3 h-3 text-emerald-600" />
                <span>Donor</span>
              </div>
              <p className="text-[10px] text-slate-500 truncate">Spice Garden</p>
            </button>

            <button
              type="button"
              onClick={() => handleQuickLogin('hope@shelter.org', 'receiver123')}
              className="px-2.5 py-2 bg-white rounded-xl text-left border border-emerald-200/80 hover:border-emerald-500 hover:shadow-sm transition"
            >
              <div className="flex items-center space-x-1 text-slate-800 font-bold text-[11px]">
                <Building className="w-3 h-3 text-teal-600" />
                <span>Receiver</span>
              </div>
              <p className="text-[10px] text-slate-500 truncate">Hope Care NGO</p>
            </button>

            <button
              type="button"
              onClick={() => handleQuickLogin('volunteer@annadhata.org', 'volunteer123')}
              className="px-2.5 py-2 bg-white rounded-xl text-left border border-emerald-200/80 hover:border-indigo-500 hover:shadow-sm transition"
            >
              <div className="flex items-center space-x-1 text-slate-800 font-bold text-[11px]">
                <Truck className="w-3 h-3 text-indigo-600" />
                <span>Volunteer</span>
              </div>
              <p className="text-[10px] text-slate-500 truncate">Relay Courier</p>
            </button>

            <button
              type="button"
              onClick={() => handleQuickLogin('admin@annadhata.org', 'admin123')}
              className="px-2.5 py-2 bg-white rounded-xl text-left border border-emerald-200/80 hover:border-emerald-500 hover:shadow-sm transition"
            >
              <div className="flex items-center space-x-1 text-slate-800 font-bold text-[11px]">
                <ShieldAlert className="w-3 h-3 text-purple-600" />
                <span>Admin</span>
              </div>
              <p className="text-[10px] text-slate-500 truncate">Governance</p>
            </button>
          </div>
        </div>

        {/* Login Form */}
        <div className="bg-white p-8 rounded-3xl border border-slate-200 shadow-xl shadow-slate-200/40">
          {error && (
            <div className="mb-5 p-3.5 bg-rose-50 border border-rose-200 text-rose-700 rounded-xl text-xs flex items-center space-x-2">
              <span className="font-semibold">{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">Email Address</label>
              <div className="relative">
                <Mail className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
                <input
                  type="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="name@organization.com"
                  className="w-full pl-9 pr-3 py-2 text-xs bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">Password</label>
              <div className="relative">
                <Lock className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
                <input
                  type="password"
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  className="w-full pl-9 pr-3 py-2 text-xs bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-2.5 px-4 bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-700 hover:to-teal-700 text-white font-bold text-xs rounded-xl shadow-md shadow-emerald-500/20 transition disabled:opacity-50 flex items-center justify-center space-x-2"
            >
              {loading ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" />
                  <span>Signing In...</span>
                </>
              ) : (
                <>
                  <span>Sign In</span>
                  <ArrowRight className="w-4 h-4" />
                </>
              )}
            </button>
          </form>

          <div className="mt-6 text-center text-xs text-slate-500">
            Don't have an account yet?{' '}
            <Link to="/register" className="font-bold text-emerald-600 hover:text-emerald-700">
              Register Here
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};
