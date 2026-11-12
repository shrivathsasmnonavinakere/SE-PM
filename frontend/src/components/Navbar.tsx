import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { notificationApi, seedApi } from '../services/api';
import { NotificationDrawer } from './NotificationDrawer';
import { NotificationItem } from '../types';
import {
  Sparkles,
  Bell,
  LogOut,
  User as UserIcon,
  Menu,
  X,
  Layers,
  HeartHandshake,
  Database,
  CheckCircle,
} from 'lucide-react';

export const Navbar: React.FC = () => {
  const { user, role, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [notificationDrawerOpen, setNotificationDrawerOpen] = useState(false);
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [unreadCount, setUnreadCount] = useState<number>(0);
  const [isSeeding, setIsSeeding] = useState(false);
  const [seedSuccess, setSeedSuccess] = useState(false);

  useEffect(() => {
    if (isAuthenticated) {
      loadNotifications();
      const interval = setInterval(loadNotifications, 30000);
      return () => clearInterval(interval);
    }
  }, [isAuthenticated]);

  const loadNotifications = async () => {
    try {
      const [listRes, countRes] = await Promise.all([
        notificationApi.getAll(),
        notificationApi.getUnreadCount(),
      ]);
      if (listRes.success && listRes.data) setNotifications(listRes.data);
      if (countRes.success && countRes.data) setUnreadCount(countRes.data.unreadCount);
    } catch (err) {
      // quiet poll
    }
  };

  const handleMarkRead = async (id: string) => {
    try {
      await notificationApi.markRead(id);
      loadNotifications();
    } catch {}
  };

  const handleMarkAllRead = async () => {
    try {
      await notificationApi.markAllRead();
      loadNotifications();
    } catch {}
  };

  const handleQuickSeed = async () => {
    setIsSeeding(true);
    try {
      await seedApi.seed();
      setSeedSuccess(true);
      setTimeout(() => setSeedSuccess(false), 3000);
      loadNotifications();
    } catch (err) {
      console.error('Seed error:', err);
    } finally {
      setIsSeeding(false);
    }
  };

  const isActive = (path: string) => location.pathname === path;

  return (
    <>
      <header className="sticky top-0 z-30 bg-white/95 backdrop-blur-md border-b border-slate-100 shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          {/* Logo */}
          <Link to="/" className="flex items-center space-x-2.5 group">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-emerald-600 to-teal-500 flex items-center justify-center text-white shadow-md shadow-emerald-500/20 group-hover:scale-105 transition">
              <HeartHandshake className="w-6 h-6" />
            </div>
            <div>
              <div className="flex items-center space-x-1.5">
                <span className="font-extrabold text-xl tracking-tight text-slate-900">Anna<span className="text-emerald-600">Dhata</span></span>
                <span className="text-[10px] font-semibold bg-emerald-100 text-emerald-800 px-1.5 py-0.5 rounded-full uppercase tracking-wider">AI</span>
              </div>
              <p className="text-[10px] text-slate-400 font-medium hidden sm:block">Surplus Food to Meals, Not Waste</p>
            </div>
          </Link>

          {/* Navigation Links based on role */}
          <nav className="hidden md:flex items-center space-x-1 text-sm font-medium text-slate-600">
            {!isAuthenticated && (
              <>
                <Link to="/" className={`px-3 py-1.5 rounded-lg transition ${isActive('/') ? 'text-emerald-700 bg-emerald-50 font-semibold' : 'hover:text-emerald-600 hover:bg-slate-50'}`}>Home</Link>
                <Link to="/about" className={`px-3 py-1.5 rounded-lg transition ${isActive('/about') ? 'text-emerald-700 bg-emerald-50 font-semibold' : 'hover:text-emerald-600 hover:bg-slate-50'}`}>About</Link>
                <Link to="/how-it-works" className={`px-3 py-1.5 rounded-lg transition ${isActive('/how-it-works') ? 'text-emerald-700 bg-emerald-50 font-semibold' : 'hover:text-emerald-600 hover:bg-slate-50'}`}>How It Works</Link>
                <Link to="/features" className={`px-3 py-1.5 rounded-lg transition ${isActive('/features') ? 'text-emerald-700 bg-emerald-50 font-semibold' : 'hover:text-emerald-600 hover:bg-slate-50'}`}>Features</Link>
                <Link to="/impact" className={`px-3 py-1.5 rounded-lg transition ${isActive('/impact') ? 'text-emerald-700 bg-emerald-50 font-semibold' : 'hover:text-emerald-600 hover:bg-slate-50'}`}>Impact</Link>
              </>
            )}

            {isAuthenticated && role === 'ROLE_DONOR' && (
              <>
                <Link to="/donor/dashboard" className={`px-2.5 py-1.5 rounded-lg transition ${isActive('/donor/dashboard') ? 'text-emerald-700 bg-emerald-50 font-semibold' : 'hover:text-emerald-600 hover:bg-slate-50'}`}>Dashboard</Link>
                <Link to="/donor/create-donation" className={`px-2.5 py-1.5 rounded-lg transition ${isActive('/donor/create-donation') ? 'text-emerald-700 bg-emerald-50 font-semibold' : 'hover:text-emerald-600 hover:bg-slate-50'}`}>+ Donate</Link>
                <Link to="/donor/my-donations" className={`px-2.5 py-1.5 rounded-lg transition ${isActive('/donor/my-donations') ? 'text-emerald-700 bg-emerald-50 font-semibold' : 'hover:text-emerald-600 hover:bg-slate-50'}`}>Listings</Link>
                <Link to="/donor/claims" className={`px-2.5 py-1.5 rounded-lg transition ${isActive('/donor/claims') ? 'text-emerald-700 bg-emerald-50 font-semibold' : 'hover:text-emerald-600 hover:bg-slate-50'}`}>Claims</Link>
                <Link to="/rescue-relay" className={`px-2.5 py-1.5 rounded-lg transition ${isActive('/rescue-relay') ? 'text-orange-700 bg-orange-50 font-semibold' : 'hover:text-orange-600 hover:bg-slate-50'}`}>Rescue Relay</Link>
                <Link to="/verify-passport" className={`px-2.5 py-1.5 rounded-lg transition ${isActive('/verify-passport') ? 'text-emerald-700 bg-emerald-50 font-semibold' : 'hover:text-emerald-600 hover:bg-slate-50'}`}>Verify QR</Link>
                <Link to="/heatmap" className={`px-2.5 py-1.5 rounded-lg transition ${isActive('/heatmap') ? 'text-emerald-700 bg-emerald-50 font-semibold' : 'hover:text-emerald-600 hover:bg-slate-50'}`}>Heatmap</Link>
              </>
            )}

            {isAuthenticated && role === 'ROLE_RECEIVER' && (
              <>
                <Link to="/receiver/dashboard" className={`px-2.5 py-1.5 rounded-lg transition ${isActive('/receiver/dashboard') ? 'text-emerald-700 bg-emerald-50 font-semibold' : 'hover:text-emerald-600 hover:bg-slate-50'}`}>Dashboard</Link>
                <Link to="/receiver/donations" className={`px-2.5 py-1.5 rounded-lg transition ${isActive('/receiver/donations') ? 'text-emerald-700 bg-emerald-50 font-semibold' : 'hover:text-emerald-600 hover:bg-slate-50'}`}>Available Food</Link>
                <Link to="/receiver/map" className={`px-2.5 py-1.5 rounded-lg transition ${isActive('/receiver/map') ? 'text-emerald-700 bg-emerald-50 font-semibold' : 'hover:text-emerald-600 hover:bg-slate-50'}`}>Map</Link>
                <Link to="/receiver/claims" className={`px-2.5 py-1.5 rounded-lg transition ${isActive('/receiver/claims') ? 'text-emerald-700 bg-emerald-50 font-semibold' : 'hover:text-emerald-600 hover:bg-slate-50'}`}>My Claims</Link>
                <Link to="/receiver/requirements" className={`px-2.5 py-1.5 rounded-lg transition ${isActive('/receiver/requirements') ? 'text-emerald-700 bg-emerald-50 font-semibold' : 'hover:text-emerald-600 hover:bg-slate-50'}`}>Needs</Link>
                <Link to="/heatmap" className={`px-2.5 py-1.5 rounded-lg transition ${isActive('/heatmap') ? 'text-emerald-700 bg-emerald-50 font-semibold' : 'hover:text-emerald-600 hover:bg-slate-50'}`}>Heatmap</Link>
              </>
            )}

            {isAuthenticated && role === 'ROLE_VOLUNTEER' && (
              <>
                <Link to="/volunteer/dashboard" className={`px-2.5 py-1.5 rounded-lg transition ${isActive('/volunteer/dashboard') ? 'text-indigo-700 bg-indigo-50 font-semibold' : 'hover:text-indigo-600 hover:bg-slate-50'}`}>Volunteer Portal</Link>
                <Link to="/verify-passport" className={`px-2.5 py-1.5 rounded-lg transition ${isActive('/verify-passport') ? 'text-emerald-700 bg-emerald-50 font-semibold' : 'hover:text-emerald-600 hover:bg-slate-50'}`}>Verify QR</Link>
                <Link to="/heatmap" className={`px-2.5 py-1.5 rounded-lg transition ${isActive('/heatmap') ? 'text-emerald-700 bg-emerald-50 font-semibold' : 'hover:text-emerald-600 hover:bg-slate-50'}`}>Demand Heatmap</Link>
                <Link to="/impact" className={`px-2.5 py-1.5 rounded-lg transition ${isActive('/impact') ? 'text-emerald-700 bg-emerald-50 font-semibold' : 'hover:text-emerald-600 hover:bg-slate-50'}`}>Impact</Link>
              </>
            )}

            {isAuthenticated && role === 'ROLE_ADMIN' && (
              <>
                <Link to="/admin/dashboard" className={`px-2.5 py-1.5 rounded-lg transition ${isActive('/admin/dashboard') ? 'text-emerald-700 bg-emerald-50 font-semibold' : 'hover:text-emerald-600 hover:bg-slate-50'}`}>Admin Portal</Link>
                <Link to="/admin/users" className={`px-2.5 py-1.5 rounded-lg transition ${isActive('/admin/users') ? 'text-emerald-700 bg-emerald-50 font-semibold' : 'hover:text-emerald-600 hover:bg-slate-50'}`}>Users</Link>
                <Link to="/admin/verifications" className={`px-2.5 py-1.5 rounded-lg transition ${isActive('/admin/verifications') ? 'text-emerald-700 bg-emerald-50 font-semibold' : 'hover:text-emerald-600 hover:bg-slate-50'}`}>Verifications</Link>
                <Link to="/admin/moderation" className={`px-2.5 py-1.5 rounded-lg transition ${isActive('/admin/moderation') ? 'text-emerald-700 bg-emerald-50 font-semibold' : 'hover:text-emerald-600 hover:bg-slate-50'}`}>Safety</Link>
                <Link to="/rescue-relay" className={`px-2.5 py-1.5 rounded-lg transition ${isActive('/rescue-relay') ? 'text-orange-700 bg-orange-50 font-semibold' : 'hover:text-orange-600 hover:bg-slate-50'}`}>Relay</Link>
                <Link to="/heatmap" className={`px-2.5 py-1.5 rounded-lg transition ${isActive('/heatmap') ? 'text-emerald-700 bg-emerald-50 font-semibold' : 'hover:text-emerald-600 hover:bg-slate-50'}`}>Heatmap</Link>
                <Link to="/admin/analytics" className={`px-2.5 py-1.5 rounded-lg transition ${isActive('/admin/analytics') ? 'text-emerald-700 bg-emerald-50 font-semibold' : 'hover:text-emerald-600 hover:bg-slate-50'}`}>AI Analytics</Link>
              </>
            )}
          </nav>

          {/* Action buttons & User State */}
          <div className="flex items-center space-x-2.5">
            {/* Quick Demo Seed Button */}
            <button
              onClick={handleQuickSeed}
              disabled={isSeeding}
              title="Seed demo dataset (Restaurants, NGOs, surplus biryani, impact)"
              className="hidden lg:flex items-center space-x-1.5 px-2.5 py-1.5 text-xs font-medium text-slate-600 bg-slate-100 hover:bg-emerald-50 hover:text-emerald-700 rounded-lg border border-slate-200 transition"
            >
              {seedSuccess ? (
                <>
                  <CheckCircle className="w-3.5 h-3.5 text-emerald-600" />
                  <span className="text-emerald-700">Seeded!</span>
                </>
              ) : (
                <>
                  <Database className="w-3.5 h-3.5 text-slate-500" />
                  <span>{isSeeding ? 'Seeding...' : 'Seed Demo Data'}</span>
                </>
              )}
            </button>

            {isAuthenticated ? (
              <>
                {/* Notification Bell */}
                <button
                  onClick={() => setNotificationDrawerOpen(true)}
                  className="relative p-2 text-slate-600 hover:text-emerald-600 hover:bg-slate-50 rounded-xl transition"
                  title="Notifications"
                >
                  <Bell className="w-5 h-5" />
                  {unreadCount > 0 && (
                    <span className="absolute top-1 right-1 w-4 h-4 rounded-full bg-rose-500 text-white text-[10px] font-bold flex items-center justify-center animate-pulse">
                      {unreadCount > 9 ? '9+' : unreadCount}
                    </span>
                  )}
                </button>

                {/* Profile Pill */}
                <Link
                  to="/profile"
                  className="flex items-center space-x-2 pl-2 pr-3 py-1.5 bg-slate-50 hover:bg-slate-100 rounded-xl border border-slate-200/80 transition"
                >
                  <div className="w-7 h-7 rounded-lg bg-emerald-600 text-white flex items-center justify-center text-xs font-bold">
                    {user?.name?.charAt(0) || 'U'}
                  </div>
                  <div className="text-left hidden sm:block">
                    <p className="text-xs font-semibold text-slate-800 leading-tight truncate max-w-[120px]">{user?.name}</p>
                    <p className="text-[10px] text-emerald-700 font-medium">
                      {role === 'ROLE_DONOR'
                        ? 'Donor'
                        : role === 'ROLE_RECEIVER'
                        ? 'Receiver NGO'
                        : role === 'ROLE_VOLUNTEER'
                        ? 'Volunteer'
                        : 'Administrator'}
                    </p>
                  </div>
                </Link>

                {/* Logout Button */}
                <button
                  onClick={() => {
                    logout();
                    navigate('/login');
                  }}
                  title="Sign out"
                  className="p-2 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-xl transition"
                >
                  <LogOut className="w-5 h-5" />
                </button>
              </>
            ) : (
              <div className="flex items-center space-x-2">
                <Link
                  to="/login"
                  className="px-4 py-2 text-xs font-semibold text-slate-700 hover:text-emerald-600 transition"
                >
                  Log in
                </Link>
                <Link
                  to="/register"
                  className="px-4 py-2 text-xs font-semibold text-white bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-700 hover:to-teal-700 rounded-xl shadow-sm hover:shadow transition"
                >
                  Register
                </Link>
              </div>
            )}

            {/* Mobile Hamburger */}
            <button
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="md:hidden p-2 text-slate-600 rounded-lg hover:bg-slate-100 transition"
            >
              {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
            </button>
          </div>
        </div>

        {/* Mobile dropdown */}
        {mobileMenuOpen && (
          <div className="md:hidden border-t border-slate-100 bg-white px-4 py-3 space-y-2">
            {!isAuthenticated && (
              <>
                <Link to="/" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-sm text-slate-700 font-medium">Home</Link>
                <Link to="/about" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-sm text-slate-700 font-medium">About</Link>
                <Link to="/how-it-works" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-sm text-slate-700 font-medium">How It Works</Link>
                <Link to="/features" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-sm text-slate-700 font-medium">Features</Link>
                <Link to="/impact" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-sm text-slate-700 font-medium">Impact</Link>
              </>
            )}

            {isAuthenticated && role === 'ROLE_DONOR' && (
              <>
                <Link to="/donor/dashboard" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-sm text-slate-700 font-medium">Dashboard</Link>
                <Link to="/donor/create-donation" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-sm text-emerald-700 font-semibold">+ Donate Food</Link>
                <Link to="/donor/my-donations" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-sm text-slate-700 font-medium">My Listings</Link>
                <Link to="/donor/claims" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-sm text-slate-700 font-medium">Claims</Link>
                <Link to="/rescue-relay" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-sm text-orange-700 font-medium">Rescue Relay</Link>
                <Link to="/verify-passport" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-sm text-slate-700 font-medium">Verify QR</Link>
                <Link to="/heatmap" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-sm text-slate-700 font-medium">Demand Heatmap</Link>
                <Link to="/impact" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-sm text-slate-700 font-medium">Impact</Link>
                <Link to="/profile" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-sm text-slate-700 font-medium">Profile</Link>
              </>
            )}

            {isAuthenticated && role === 'ROLE_RECEIVER' && (
              <>
                <Link to="/receiver/dashboard" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-sm text-slate-700 font-medium">Dashboard</Link>
                <Link to="/receiver/donations" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-sm text-slate-700 font-medium">Available Food</Link>
                <Link to="/receiver/map" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-sm text-slate-700 font-medium">Live Map</Link>
                <Link to="/receiver/claims" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-sm text-slate-700 font-medium">My Claims</Link>
                <Link to="/receiver/requirements" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-sm text-slate-700 font-medium">Food Needs</Link>
                <Link to="/rescue-relay" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-sm text-orange-700 font-medium">Rescue Relay</Link>
                <Link to="/heatmap" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-sm text-slate-700 font-medium">Demand Heatmap</Link>
                <Link to="/impact" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-sm text-slate-700 font-medium">Impact</Link>
                <Link to="/profile" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-sm text-slate-700 font-medium">Profile</Link>
              </>
            )}

            {isAuthenticated && role === 'ROLE_VOLUNTEER' && (
              <>
                <Link to="/volunteer/dashboard" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-sm text-indigo-700 font-semibold">Volunteer Portal</Link>
                <Link to="/verify-passport" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-sm text-slate-700 font-medium">Verify QR</Link>
                <Link to="/heatmap" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-sm text-slate-700 font-medium">Demand Heatmap</Link>
                <Link to="/impact" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-sm text-slate-700 font-medium">Impact</Link>
                <Link to="/profile" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-sm text-slate-700 font-medium">Profile</Link>
              </>
            )}

            {isAuthenticated && role === 'ROLE_ADMIN' && (
              <>
                <Link to="/admin/dashboard" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-sm text-slate-700 font-medium">Admin Dashboard</Link>
                <Link to="/admin/users" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-sm text-slate-700 font-medium">Users</Link>
                <Link to="/admin/verifications" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-sm text-slate-700 font-medium">Verifications</Link>
                <Link to="/admin/moderation" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-sm text-slate-700 font-medium">Donations & Safety</Link>
                <Link to="/rescue-relay" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-sm text-orange-700 font-medium">Rescue Relay</Link>
                <Link to="/verify-passport" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-sm text-slate-700 font-medium">Verify QR</Link>
                <Link to="/heatmap" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-sm text-slate-700 font-medium">Demand Heatmap</Link>
                <Link to="/admin/analytics" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-sm text-slate-700 font-medium">AI Analytics</Link>
              </>
            )}

            <button
              onClick={handleQuickSeed}
              className="w-full text-left py-2 text-sm text-emerald-700 font-medium border-t border-slate-100 mt-2 pt-2"
            >
              Seed Demo Data
            </button>
          </div>
        )}
      </header>

      {/* Notification Drawer Modal */}
      <NotificationDrawer
        isOpen={notificationDrawerOpen}
        onClose={() => setNotificationDrawerOpen(false)}
        notifications={notifications}
        onMarkRead={handleMarkRead}
        onMarkAllRead={handleMarkAllRead}
      />
    </>
  );
};
