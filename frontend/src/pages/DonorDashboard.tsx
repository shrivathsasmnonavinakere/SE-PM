import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { dashboardApi, claimApi } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { DonorDashboard, Claim } from '../types';
import { StatusBadge } from '../components/StatusBadge';
import { SafetyBadge } from '../components/SafetyBadge';
import { TrustBadge } from '../components/TrustBadge';
import {
  Package,
  TrendingUp,
  HeartHandshake,
  Clock,
  PlusCircle,
  Sparkles,
  ArrowRight,
  Check,
  X,
  MapPin,
  Loader2,
  Calendar,
  AlertCircle,
  AlertTriangle,
} from 'lucide-react';

export const DonorDashboardPage: React.FC = () => {
  const { user } = useAuth();
  const [data, setData] = useState<DonorDashboard | null>(null);
  const [pendingClaims, setPendingClaims] = useState<Claim[]>([]);
  const [loading, setLoading] = useState(true);
  const [actionLoadingId, setActionLoadingId] = useState<string | null>(null);

  useEffect(() => {
    loadDashboard();
  }, []);

  const loadDashboard = async () => {
    try {
      const [dashRes, claimsRes] = await Promise.all([
        dashboardApi.getDonor(),
        claimApi.getMy(),
      ]);
      if (dashRes.success && dashRes.data) setData(dashRes.data);
      if (claimsRes.success && claimsRes.data) {
        setPendingClaims(claimsRes.data.filter((c) => c.status === 'PENDING'));
      }
    } catch (err) {
      console.error('Failed to load donor dashboard:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleApproveClaim = async (claimId: string) => {
    setActionLoadingId(claimId);
    try {
      await claimApi.approve(claimId);
      await loadDashboard();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to approve claim');
    } finally {
      setActionLoadingId(null);
    }
  };

  const handleRejectClaim = async (claimId: string) => {
    setActionLoadingId(claimId);
    try {
      await claimApi.reject(claimId, 'Schedule incompatibility');
      await loadDashboard();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to reject claim');
    } finally {
      setActionLoadingId(null);
    }
  };

  if (loading) {
    return (
      <div className="min-h-[70vh] flex items-center justify-center">
        <Loader2 className="w-8 h-8 animate-spin text-emerald-600" />
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 border-b border-slate-200 pb-6">
        <div>
          <div className="flex flex-wrap items-center gap-3">
            <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">Donor Kitchen Dashboard</h1>
            {data?.trustScore !== undefined && (
              <TrustBadge
                score={data.trustScore}
                tier={data.trustTier}
                userId={user?.id}
                label={user?.organizationName || user?.name}
              />
            )}
          </div>
          <p className="text-xs sm:text-sm text-slate-500 mt-1">Manage surplus food inventory, review NGO claims, and monitor your meals saved.</p>
        </div>

        <div className="flex flex-wrap items-center gap-2.5">
          <Link
            to="/rescue-relay"
            className="inline-flex items-center space-x-1.5 px-3.5 py-2.5 bg-white text-slate-700 font-semibold text-xs rounded-xl border border-slate-200 shadow-sm hover:bg-orange-50 hover:text-orange-700 transition"
          >
            <AlertTriangle className="w-3.5 h-3.5 text-orange-500" />
            <span>Rescue Relay</span>
          </Link>
          <Link
            to="/donor/create-donation"
            className="inline-flex items-center space-x-2 px-4 py-2.5 bg-gradient-to-r from-emerald-600 to-teal-600 text-white font-bold text-xs rounded-xl shadow-md shadow-emerald-500/20 hover:from-emerald-700 hover:to-teal-700 transition"
          >
            <PlusCircle className="w-4 h-4" />
            <span>Donate Surplus Food</span>
          </Link>
        </div>
      </div>

      {/* Rescue Relay Missed Pickup Alert Banner */}
      {(data?.activeRescueRelaysCount || 0) > 0 && (
        <div className="p-4 bg-orange-50 border border-orange-200 rounded-2xl flex flex-col sm:flex-row sm:items-center justify-between gap-3 text-xs text-orange-950">
          <div className="flex items-start space-x-2.5">
            <AlertTriangle className="w-5 h-5 text-orange-600 shrink-0 mt-0.5" />
            <div>
              <strong className="block">
                {data?.activeRescueRelaysCount} Rescue Relay Alert{(data?.activeRescueRelaysCount || 0) > 1 ? 's' : ''} Pending Approval
              </strong>
              <p className="text-orange-800 mt-0.5">
                An approved pickup exceeded its grace window. Release the portion to re-match alternative nearby NGOs before expiry.
              </p>
            </div>
          </div>
          <Link
            to="/rescue-relay"
            className="px-4 py-2 bg-orange-600 hover:bg-orange-700 text-white font-bold rounded-xl transition whitespace-nowrap text-center"
          >
            Review & Release
          </Link>
        </div>
      )}

      {/* KPI Stats Grid */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 sm:gap-6">
        <div className="p-5 bg-white rounded-3xl border border-slate-200 shadow-sm space-y-1">
          <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">Active Listings</span>
          <div className="text-3xl font-extrabold text-slate-900">{data?.activeDonations || 0}</div>
          <p className="text-[11px] text-slate-400">Available or matched</p>
        </div>

        <div className="p-5 bg-white rounded-3xl border border-slate-200 shadow-sm space-y-1">
          <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">Pending Claims</span>
          <div className={`text-3xl font-extrabold ${pendingClaims.length > 0 ? 'text-amber-600' : 'text-slate-900'}`}>
            {pendingClaims.length}
          </div>
          <p className="text-[11px] text-slate-400">Awaiting your approval</p>
        </div>

        <div className="p-5 bg-white rounded-3xl border border-slate-200 shadow-sm space-y-1">
          <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">Food Redistributed</span>
          <div className="text-3xl font-extrabold text-emerald-600">
            {data ? data.totalFoodRedistributedKg.toFixed(1) : '0.0'} <span className="text-base text-slate-500 font-bold">kg</span>
          </div>
          <p className="text-[11px] text-slate-400">Completed pickups</p>
        </div>

        <div className="p-5 bg-white rounded-3xl border border-slate-200 shadow-sm space-y-1">
          <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">Meals Provided</span>
          <div className="text-3xl font-extrabold text-teal-600">
            {data ? Math.round(data.estimatedMealsSaved).toLocaleString() : '0'}
          </div>
          <p className="text-[11px] text-slate-400">0.4 kg / meal factor</p>
        </div>
      </div>

      {/* Pending Claims Requiring Action */}
      {pendingClaims.length > 0 && (
        <div className="p-6 bg-amber-50/60 rounded-3xl border border-amber-200/80 shadow-sm space-y-4">
          <div className="flex items-center space-x-2 text-amber-900 font-bold text-sm">
            <AlertCircle className="w-5 h-5 text-amber-600" />
            <span>Pending Claim Requests Requiring Your Approval ({pendingClaims.length})</span>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {pendingClaims.map((c) => (
              <div key={c.id} className="p-4 bg-white rounded-2xl border border-amber-200 shadow-sm space-y-3">
                <div className="flex items-start justify-between">
                  <div>
                    <h4 className="font-bold text-slate-900 text-sm">{c.foodName || 'Food Donation'}</h4>
                    <div className="flex items-center gap-2 mt-0.5">
                      <p className="text-xs font-semibold text-slate-600">{c.receiverName}</p>
                      <TrustBadge
                        score={c.receiverTrustScore}
                        tier={c.receiverTrustTier}
                        userId={c.receiverId}
                        label={c.receiverName}
                        compact
                      />
                    </div>
                    <p className="text-[11px] text-slate-400">{c.receiverAddress}</p>
                  </div>
                  <span className="text-xs font-bold text-amber-700 bg-amber-100 px-2.5 py-0.5 rounded-full">
                    {c.claimedQuantity} {c.measurementUnit}
                  </span>
                </div>

                <div className="text-xs text-slate-600 bg-slate-50 p-2.5 rounded-xl space-y-1 border border-slate-100">
                  <div className="flex items-center justify-between">
                    <span className="text-slate-500">Proposed Pickup:</span>
                    <strong className="text-slate-800">
                      {c.proposedPickupTime ? new Date(c.proposedPickupTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : 'ASAP'}
                    </strong>
                  </div>
                  {c.notes && (
                    <p className="text-[11px] text-slate-500 italic">"{c.notes}"</p>
                  )}
                </div>

                <div className="flex items-center space-x-2 pt-1">
                  <button
                    onClick={() => handleApproveClaim(c.id)}
                    disabled={actionLoadingId === c.id}
                    className="flex-1 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-bold transition flex items-center justify-center space-x-1 disabled:opacity-50"
                  >
                    {actionLoadingId === c.id ? (
                      <Loader2 className="w-3.5 h-3.5 animate-spin" />
                    ) : (
                      <>
                        <Check className="w-3.5 h-3.5" />
                        <span>Approve & Confirm</span>
                      </>
                    )}
                  </button>
                  <button
                    onClick={() => handleRejectClaim(c.id)}
                    disabled={actionLoadingId === c.id}
                    className="px-3 py-2 bg-slate-100 hover:bg-rose-50 text-slate-600 hover:text-rose-700 rounded-xl text-xs font-medium transition"
                  >
                    <X className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Recent Donations Section */}
      <div className="bg-white p-6 rounded-3xl border border-slate-200 shadow-sm space-y-4">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="font-bold text-slate-900 text-base">Your Active & Recent Listings</h3>
            <p className="text-xs text-slate-400">Real-time status of posted surplus batches</p>
          </div>
          <Link
            to="/donor/my-donations"
            className="text-xs font-semibold text-emerald-600 hover:text-emerald-700 flex items-center space-x-1"
          >
            <span>View all listings</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </Link>
        </div>

        {data?.recentDonations && data.recentDonations.length > 0 ? (
          <div className="divide-y divide-slate-100">
            {data.recentDonations.map((d) => (
              <div key={d.id} className="py-4 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
                <div className="space-y-1">
                  <div className="flex items-center space-x-2">
                    <StatusBadge status={d.status} />
                    <SafetyBadge indicator={d.safetyIndicator} />
                    <span className="text-[11px] text-slate-400 font-mono">
                      {d.foodCategory}
                    </span>
                  </div>
                  <h4 className="font-bold text-slate-900 text-sm">{d.foodName}</h4>
                  <div className="flex flex-wrap items-center gap-4 text-xs text-slate-500">
                    <span>
                      Remaining: <strong className="text-emerald-700">{(d.remainingQuantity ?? d.quantity).toFixed(1)}</strong> / <strong className="text-slate-800">{d.quantity} {d.measurementUnit}</strong>
                    </span>
                    <span className="flex items-center text-amber-600">
                      <Clock className="w-3.5 h-3.5 mr-1" />
                      Pickup before: {new Date(d.pickupDeadline).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </span>
                  </div>
                </div>

                <div className="flex items-center space-x-2 w-full sm:w-auto">
                  <Link
                    to={`/donor/donations/${d.id}/matches`}
                    className="flex-1 sm:flex-none px-3 py-1.5 bg-emerald-50 text-emerald-700 hover:bg-emerald-100 rounded-xl text-xs font-semibold transition flex items-center justify-center space-x-1"
                  >
                    <Sparkles className="w-3.5 h-3.5 text-emerald-600" />
                    <span>View Matches</span>
                  </Link>
                  <Link
                    to={`/donor/donations/${d.id}`}
                    className="flex-1 sm:flex-none px-3 py-1.5 bg-slate-100 text-slate-700 hover:bg-slate-200 rounded-xl text-xs font-semibold transition"
                  >
                    Details
                  </Link>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="text-center py-12 space-y-3">
            <Package className="w-10 h-10 text-slate-300 mx-auto" />
            <h4 className="text-sm font-semibold text-slate-700">No active surplus donations</h4>
            <p className="text-xs text-slate-400 max-w-sm mx-auto">
              Ready to reduce kitchen waste? Post your surplus food in seconds or let our AI parse your description.
            </p>
            <Link
              to="/donor/create-donation"
              className="inline-flex items-center space-x-1.5 px-4 py-2 bg-emerald-600 text-white rounded-xl text-xs font-bold hover:bg-emerald-700 transition"
            >
              <PlusCircle className="w-4 h-4" />
              <span>Create First Listing</span>
            </Link>
          </div>
        )}
      </div>
    </div>
  );
};
