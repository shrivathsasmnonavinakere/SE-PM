import React, { useState, useEffect } from 'react';
import { adminApi } from '../services/api';
import { User } from '../types';
import { TrustBadge } from '../components/TrustBadge';
import {
  Users,
  Search,
  CheckCircle,
  XCircle,
  Shield,
  Loader2,
  Lock,
  Unlock,
} from 'lucide-react';

export const UserManagementPage: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [roleFilter, setRoleFilter] = useState<string>('ALL');
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadUsers();
  }, []);

  const loadUsers = async () => {
    try {
      const res = await adminApi.getUsers();
      if (res.success && res.data) setUsers(res.data);
    } catch (err) {
      console.error('Failed to load users:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleToggleActive = async (userId: string, currentActive: boolean) => {
    try {
      await adminApi.toggleActive(userId, !currentActive);
      loadUsers();
    } catch (err: any) {
      alert('Failed to update account status.');
    }
  };

  const filtered = users.filter((u) => {
    if (roleFilter !== 'ALL' && u.role !== roleFilter) return false;
    if (!searchTerm.trim()) return true;
    const term = searchTerm.toLowerCase();
    return (
      u.name.toLowerCase().includes(term) ||
      u.email.toLowerCase().includes(term) ||
      (u.organizationName && u.organizationName.toLowerCase().includes(term))
    );
  });

  if (loading) {
    return (
      <div className="min-h-[70vh] flex items-center justify-center">
        <Loader2 className="w-8 h-8 animate-spin text-emerald-600" />
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-8">
      {/* Title */}
      <div className="border-b border-slate-200 pb-5">
        <div className="flex items-center space-x-2">
          <span className="p-2 bg-purple-100 text-purple-700 rounded-xl">
            <Users className="w-5 h-5" />
          </span>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
            User Account Management
          </h1>
        </div>
        <p className="text-xs sm:text-sm text-slate-500 mt-1">
          Monitor all registered donors, receivers, and administrative staff accounts.
        </p>
      </div>

      {/* Filters */}
      <div className="bg-white p-5 rounded-3xl border border-slate-200 shadow-sm flex flex-col sm:flex-row items-center justify-between gap-4 text-xs">
        <div className="relative w-full sm:w-80">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Search by name, email, or org..."
            className="w-full pl-9 pr-3 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-emerald-500"
          />
        </div>

        <div className="flex items-center space-x-2 w-full sm:w-auto overflow-x-auto">
          {['ALL', 'ROLE_DONOR', 'ROLE_RECEIVER', 'ROLE_VOLUNTEER', 'ROLE_ADMIN'].map((r) => (
            <button
              key={r}
              onClick={() => setRoleFilter(r)}
              className={`px-3 py-1.5 rounded-xl font-bold transition whitespace-nowrap ${
                roleFilter === r
                  ? 'bg-purple-600 text-white shadow-sm'
                  : 'bg-slate-50 text-slate-600 hover:bg-slate-100'
              }`}
            >
              {r.replace('ROLE_', '')}
            </button>
          ))}
        </div>
      </div>

      {/* Users Table */}
      <div className="bg-white rounded-3xl border border-slate-200 overflow-hidden shadow-sm">
        <div className="divide-y divide-slate-100">
          {filtered.map((u) => (
            <div key={u.id} className="p-5 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 text-xs">
              <div className="space-y-1">
                <div className="flex flex-wrap items-center gap-2">
                  <h4 className="font-bold text-slate-900 text-sm">{u.name}</h4>
                  <span
                    className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                      u.role === 'ROLE_ADMIN'
                        ? 'bg-purple-100 text-purple-800'
                        : u.role === 'ROLE_DONOR'
                        ? 'bg-emerald-100 text-emerald-800'
                        : u.role === 'ROLE_VOLUNTEER'
                        ? 'bg-indigo-100 text-indigo-800'
                        : 'bg-teal-100 text-teal-800'
                    }`}
                  >
                    {u.role.replace('ROLE_', '')}
                  </span>
                  <TrustBadge
                    score={u.trustScore}
                    tier={u.trustTier}
                    userId={u.id}
                    label={u.organizationName || u.name}
                    compact
                  />
                  {!u.active && (
                    <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-rose-100 text-rose-800">
                      SUSPENDED
                    </span>
                  )}
                </div>
                <p className="text-slate-500">{u.email} &bull; {u.organizationName || 'Individual'}</p>
                <p className="text-slate-400 text-[11px]">{u.address || 'No address set'}</p>
              </div>

              <div className="flex items-center space-x-2">
                <button
                  onClick={() => handleToggleActive(u.id, u.active)}
                  className={`px-3 py-1.5 rounded-xl font-bold transition flex items-center space-x-1 ${
                    u.active
                      ? 'bg-slate-100 text-slate-700 hover:bg-rose-50 hover:text-rose-700'
                      : 'bg-emerald-600 text-white hover:bg-emerald-700'
                  }`}
                >
                  {u.active ? <Lock className="w-3.5 h-3.5" /> : <Unlock className="w-3.5 h-3.5" />}
                  <span>{u.active ? 'Suspend' : 'Activate'}</span>
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
