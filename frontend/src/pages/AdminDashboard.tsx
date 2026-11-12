import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { dashboardApi, adminApi } from '../services/api';
import { AdminDashboard, Donation, User } from '../types';
import { StatusBadge } from '../components/StatusBadge';
import { SafetyBadge } from '../components/SafetyBadge';
import {
  ShieldAlert,
  Users,
  CheckCircle2,
  Clock,
  TrendingUp,
  AlertTriangle,
  FileCheck,
  Package,
  Sparkles,
  ArrowRight,
  Loader2,
  Building2,
  Utensils,
} from 'lucide-react';
import { ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Tooltip } from 'recharts';

export const AdminDashboardPage: React.FC = () => {
  const [data, setData] = useState<AdminDashboard | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadDashboard();
  }, []);

  const loadDashboard = async () => {
    try {
      const res = await dashboardApi.getAdmin();
      if (res.success && res.data) setData(res.data);
    } catch (err) {
      console.error('Failed to load admin dashboard:', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-[70vh] flex items-center justify-center">
        <Loader2 className="w-8 h-8 animate-spin text-emerald-600" />
      </div>
    );
  }

  const categoryChartData = data?.foodCategoryDistribution
    ? Object.entries(data.foodCategoryDistribution).map(([name, count]) => ({
        name,
        count,
      }))
    : [];

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-8">
      {/* Title */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 border-b border-slate-200 pb-5">
        <div>
          <div className="flex items-center space-x-2">
            <span className="p-2 bg-purple-100 text-purple-700 rounded-xl">
              <ShieldAlert className="w-5 h-5" />
            </span>
            <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">Platform Administration Portal</h1>
          </div>
          <p className="text-xs sm:text-sm text-slate-500 mt-1">Ecosystem governance, NGO verification queues, food safety screening, and analytics.</p>
        </div>

        <div className="flex flex-wrap items-center gap-2.5">
          <Link
            to="/rescue-relay"
            className="inline-flex items-center space-x-1.5 px-3.5 py-2.5 bg-white text-slate-700 font-bold text-xs rounded-xl border border-slate-200 shadow-sm hover:bg-orange-50 hover:text-orange-700 transition"
          >
            <AlertTriangle className="w-4 h-4 text-orange-500" />
            <span>Rescue Relay ({data?.activeRescueRelaysCount || 0})</span>
          </Link>
          <Link
            to="/heatmap"
            className="inline-flex items-center space-x-1.5 px-3.5 py-2.5 bg-white text-slate-700 font-bold text-xs rounded-xl border border-slate-200 shadow-sm hover:bg-slate-50 transition"
          >
            <span>Demand Heatmap</span>
          </Link>
          <Link
            to="/admin/analytics"
            className="inline-flex items-center space-x-2 px-4 py-2.5 bg-gradient-to-r from-emerald-600 to-teal-600 text-white font-bold text-xs rounded-xl shadow-md shadow-emerald-500/20 hover:from-emerald-700 hover:to-teal-700 transition"
          >
            <Sparkles className="w-4 h-4" />
            <span>AI Strategic Report</span>
          </Link>
        </div>
      </div>

      {/* Primary KPI Grid */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 sm:gap-6">
        <div className="p-5 bg-white rounded-3xl border border-slate-200 shadow-sm space-y-1">
          <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">Total Users</span>
          <div className="text-3xl font-extrabold text-slate-900">{data?.totalUsers || 0}</div>
          <p className="text-[11px] text-slate-400">
            {data?.totalDonors} Donors &bull; {data?.totalReceivers} NGOs &bull; {data?.totalVolunteers || 0} Volunteers
          </p>
        </div>

        <div className="p-5 bg-white rounded-3xl border border-slate-200 shadow-sm space-y-1">
          <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">Pending Verifications</span>
          <div className={`text-3xl font-extrabold ${data && data.pendingVerificationRequests > 0 ? 'text-amber-600' : 'text-slate-900'}`}>
            {data?.pendingVerificationRequests || 0}
          </div>
          <p className="text-[11px] text-slate-400">
            {(data?.activeRescueRelaysCount || 0) > 0 ? (
              <Link to="/rescue-relay" className="text-orange-600 font-semibold hover:underline">
                {data?.activeRescueRelaysCount} Rescue Relay alert(s)
              </Link>
            ) : (
              'Charities awaiting review'
            )}
          </p>
        </div>

        <div className="p-5 bg-white rounded-3xl border border-slate-200 shadow-sm space-y-1">
          <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">Total Redistributed</span>
          <div className="text-3xl font-extrabold text-emerald-600">
            {data ? data.totalFoodRedistributedKg.toFixed(1) : '0.0'} <span className="text-base text-slate-500 font-bold">kg</span>
          </div>
          <p className="text-[11px] text-slate-400">~{data ? Math.round(data.estimatedMealsSaved) : 0} meals</p>
        </div>

        <div className="p-5 bg-white rounded-3xl border border-slate-200 shadow-sm space-y-1">
          <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">Redistribution Efficiency</span>
          <div className="text-3xl font-extrabold text-teal-600">
            {data ? data.redistributionSuccessRate.toFixed(1) : '0.0'}%
          </div>
          <p className="text-[11px] text-slate-400">Completion rate</p>
        </div>
      </div>

      {/* Verification Queue & Flagged Safety Review */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Pending Organisation Verifications */}
        <div className="p-6 bg-white rounded-3xl border border-slate-200 shadow-sm space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="font-bold text-slate-900 text-sm flex items-center space-x-2">
              <Building2 className="w-4 h-4 text-emerald-600" />
              <span>Pending Charity Verifications ({data?.pendingVerifications?.length || 0})</span>
            </h3>
            <Link to="/admin/verifications" className="text-xs font-semibold text-emerald-600 hover:text-emerald-700">
              Manage Queue &rarr;
            </Link>
          </div>

          {data?.pendingVerifications && data.pendingVerifications.length > 0 ? (
            <div className="divide-y divide-slate-100">
              {data.pendingVerifications.slice(0, 4).map((u) => (
                <div key={u.id} className="py-3 flex items-center justify-between">
                  <div>
                    <h4 className="font-bold text-slate-900 text-xs">{u.organizationName || u.name}</h4>
                    <p className="text-[11px] text-slate-500">Reg: {u.registrationNumber || 'Pending documentation'}</p>
                    <p className="text-[10px] text-slate-400">{u.address}</p>
                  </div>
                  <Link
                    to="/admin/verifications"
                    className="px-3 py-1 bg-emerald-50 text-emerald-700 rounded-lg text-xs font-bold hover:bg-emerald-100 transition"
                  >
                    Review
                  </Link>
                </div>
              ))}
            </div>
          ) : (
            <div className="text-center py-10 text-xs text-slate-400">
              <CheckCircle2 className="w-8 h-8 text-emerald-500 mx-auto mb-2 opacity-80" />
              <span>All organization verifications are up to date!</span>
            </div>
          )}
        </div>

        {/* Food Safety Flagged Listings */}
        <div className="p-6 bg-white rounded-3xl border border-slate-200 shadow-sm space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="font-bold text-slate-900 text-sm flex items-center space-x-2">
              <AlertTriangle className="w-4 h-4 text-amber-500" />
              <span>Safety Screened Listings ({data?.flaggedSafetyDonations?.length || 0})</span>
            </h3>
            <Link to="/admin/moderation" className="text-xs font-semibold text-emerald-600 hover:text-emerald-700">
              Inspect all &rarr;
            </Link>
          </div>

          {data?.flaggedSafetyDonations && data.flaggedSafetyDonations.length > 0 ? (
            <div className="divide-y divide-slate-100">
              {data.flaggedSafetyDonations.slice(0, 4).map((d) => (
                <div key={d.id} className="py-3 flex items-center justify-between">
                  <div>
                    <div className="flex items-center space-x-1.5 mb-1">
                      <SafetyBadge indicator={d.safetyIndicator} />
                      <span className="text-[10px] text-slate-400">{d.foodCategory}</span>
                    </div>
                    <h4 className="font-bold text-slate-900 text-xs">{d.foodName}</h4>
                    <p className="text-[10px] text-slate-500 italic truncate max-w-xs">{d.safetyNotes}</p>
                  </div>
                  <span className="text-xs font-bold text-slate-700">{d.quantity} {d.measurementUnit}</span>
                </div>
              ))}
            </div>
          ) : (
            <div className="text-center py-10 text-xs text-slate-400">
              <CheckCircle2 className="w-8 h-8 text-emerald-500 mx-auto mb-2 opacity-80" />
              <span>No critical food safety warnings flagged.</span>
            </div>
          )}
        </div>
      </div>

      {/* Category Breakdown Recharts */}
      {categoryChartData.length > 0 && (
        <div className="p-6 bg-white rounded-3xl border border-slate-200 shadow-sm space-y-4">
          <h3 className="font-bold text-slate-900 text-sm">Platform Food Category Breakdown</h3>
          <div className="h-60 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={categoryChartData}>
                <XAxis dataKey="name" stroke="#94a3b8" fontSize={11} />
                <YAxis stroke="#94a3b8" fontSize={11} />
                <Tooltip
                  contentStyle={{ backgroundColor: '#ffffff', borderRadius: '12px', borderColor: '#e2e8f0', fontSize: '12px' }}
                />
                <Bar dataKey="count" name="Listings" fill="#10b981" radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      )}
    </div>
  );
};
