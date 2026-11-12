import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { dashboardApi, claimApi } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { ReceiverDashboard, Donation, Claim } from '../types';
import { StatusBadge } from '../components/StatusBadge';
import { SafetyBadge } from '../components/SafetyBadge';
import { TrustBadge } from '../components/TrustBadge';
import { toLocalDateTimeInputValue } from '../utils/datetime';
import {
  Package,
  MapPin,
  Clock,
  Building,
  HeartHandshake,
  CheckCircle2,
  Calendar,
  ArrowRight,
  TrendingUp,
  Loader2,
  AlertCircle,
  PlusCircle,
  Truck,
  ShieldAlert,
} from 'lucide-react';

export const ReceiverDashboardPage: React.FC = () => {
  const { user } = useAuth();
  const [data, setData] = useState<ReceiverDashboard | null>(null);
  const [activeClaims, setActiveClaims] = useState<Claim[]>([]);
  const [loading, setLoading] = useState(true);

  // Claim modal state
  const [selectedDonation, setSelectedDonation] = useState<Donation | null>(null);
  const [claimQty, setClaimQty] = useState<number>(10);
  const [claimTime, setClaimTime] = useState<string>('');
  const [claimNotes, setClaimNotes] = useState('');
  const [volunteerRequested, setVolunteerRequested] = useState(false);
  const [claiming, setClaiming] = useState(false);

  const isUnverifiedReceiver = user?.role === 'ROLE_RECEIVER' && user.verificationStatus !== 'VERIFIED';

  useEffect(() => {
    loadDashboard();
  }, []);

  const loadDashboard = async () => {
    try {
      const [dashRes, claimsRes] = await Promise.all([
        dashboardApi.getReceiver(),
        claimApi.getMy(),
      ]);
      if (dashRes.success && dashRes.data) setData(dashRes.data);
      if (claimsRes.success && claimsRes.data) setActiveClaims(claimsRes.data);
    } catch (err) {
      console.error('Failed to load receiver dashboard:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenClaim = (d: Donation) => {
    setSelectedDonation(d);
    setClaimQty(d.remainingQuantity ?? d.quantity);
    setVolunteerRequested(false);
    const in1Hour = toLocalDateTimeInputValue(new Date(Date.now() + 60 * 60 * 1000));
    setClaimTime(in1Hour);
  };

  const handleConfirmClaim = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedDonation) return;

    setClaiming(true);
    try {
      await claimApi.create({
        donationId: selectedDonation.id,
        claimedQuantity: Number(claimQty),
        proposedPickupTime: new Date(claimTime).toISOString(),
        notes: claimNotes,
        volunteerRequested,
      });
      setSelectedDonation(null);
      setClaimNotes('');
      await loadDashboard();
      alert('Claim submitted successfully! The donor has been notified to confirm pickup.');
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to submit claim.');
    } finally {
      setClaiming(false);
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
      {/* Title */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 border-b border-slate-200 pb-6">
        <div>
          <div className="flex flex-wrap items-center gap-3">
            <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">Receiver NGO Dashboard</h1>
            {data?.trustScore !== undefined && (
              <TrustBadge
                score={data.trustScore}
                tier={data.trustTier}
                userId={user?.id}
                label={user?.organizationName || user?.name}
              />
            )}
          </div>
          <p className="text-xs sm:text-sm text-slate-500 mt-1">Discover available surplus food, claim portions, and arrange pickups.</p>
        </div>

        <div className="flex flex-wrap items-center gap-2.5">
          <Link
            to="/heatmap"
            className="inline-flex items-center space-x-1.5 px-3.5 py-2.5 bg-white text-slate-700 font-semibold text-xs rounded-xl border border-slate-200 shadow-sm hover:bg-slate-50 transition"
          >
            <span>Demand Heatmap</span>
          </Link>
          <Link
            to="/receiver/map"
            className="inline-flex items-center space-x-2 px-4 py-2.5 bg-white text-slate-700 font-semibold text-xs rounded-xl border border-slate-200 shadow-sm hover:bg-slate-50 transition"
          >
            <MapPin className="w-4 h-4 text-emerald-600" />
            <span>View Food Map</span>
          </Link>
          <Link
            to="/receiver/requirements"
            className="inline-flex items-center space-x-2 px-4 py-2.5 bg-gradient-to-r from-emerald-600 to-teal-600 text-white font-bold text-xs rounded-xl shadow-md shadow-emerald-500/20 hover:from-emerald-700 hover:to-teal-700 transition"
          >
            <PlusCircle className="w-4 h-4" />
            <span>Update Food Needs</span>
          </Link>
        </div>
      </div>

      {isUnverifiedReceiver && (
        <div className="p-4 bg-amber-50 border border-amber-200 rounded-2xl flex flex-col sm:flex-row sm:items-center justify-between gap-3 text-xs text-amber-900">
          <div className="flex items-start space-x-2.5">
            <ShieldAlert className="w-5 h-5 text-amber-600 shrink-0 mt-0.5" />
            <div>
              <strong className="block">Organization Verification Required Before Claiming</strong>
              <p className="text-amber-800 mt-0.5">
                Current verification status: <strong>{user?.verificationStatus}</strong>. Upload documents in Profile to unlock claims.
              </p>
            </div>
          </div>
          <Link
            to="/profile"
            className="px-4 py-2 bg-amber-600 text-white font-bold rounded-xl hover:bg-amber-700 transition whitespace-nowrap"
          >
            Upload Documents
          </Link>
        </div>
      )}

      {/* KPI Stats */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 sm:gap-6">
        <div className="p-5 bg-white rounded-3xl border border-slate-200 shadow-sm space-y-1">
          <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">Nearby Available</span>
          <div className="text-3xl font-extrabold text-emerald-600">{data?.availableNearbyDonations || 0}</div>
          <p className="text-[11px] text-slate-400">Within collection range</p>
        </div>

        <div className="p-5 bg-white rounded-3xl border border-slate-200 shadow-sm space-y-1">
          <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">Pending Approvals</span>
          <div className="text-3xl font-extrabold text-amber-600">{data?.activeClaimsCount || 0}</div>
          <p className="text-[11px] text-slate-400">Claims awaiting donor</p>
        </div>

        <div className="p-5 bg-white rounded-3xl border border-slate-200 shadow-sm space-y-1">
          <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">Confirmed Pickups</span>
          <div className="text-3xl font-extrabold text-purple-600">{data?.pendingPickupsCount || 0}</div>
          <p className="text-[11px] text-slate-400">Ready for collection</p>
        </div>

        <div className="p-5 bg-white rounded-3xl border border-slate-200 shadow-sm space-y-1">
          <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">Total Received</span>
          <div className="text-3xl font-extrabold text-teal-600">
            {data ? data.totalQuantityReceivedKg.toFixed(1) : '0.0'} <span className="text-base text-slate-500 font-bold">kg</span>
          </div>
          <p className="text-[11px] text-slate-400">~{data?.estimatedMealsDistributed || 0} meals served</p>
        </div>
      </div>

      {/* Recommended Nearby Surplus Batches */}
      <div className="bg-white p-6 sm:p-8 rounded-3xl border border-slate-200 shadow-sm space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="text-base font-bold text-slate-900">Recommended Surplus Food Available Now</h3>
            <p className="text-xs text-slate-400">Ranked by proximity and your organization's dietary capacity</p>
          </div>
          <Link
            to="/receiver/donations"
            className="text-xs font-semibold text-emerald-600 hover:text-emerald-700 flex items-center space-x-1"
          >
            <span>Browse all food</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </Link>
        </div>

        {data?.recommendedDonations && data.recommendedDonations.length > 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {data.recommendedDonations.map((d) => (
              <div key={d.id} className="bg-slate-50/70 rounded-2xl border border-slate-200 overflow-hidden flex flex-col justify-between hover:shadow-md transition">
                {d.foodImage && (
                  <img src={d.foodImage} alt={d.foodName} className="h-40 w-full object-cover" />
                )}
                <div className="p-5 space-y-3">
                  <div className="flex items-center justify-between">
                    <StatusBadge status={d.status} />
                    <SafetyBadge indicator={d.safetyIndicator} />
                  </div>

                  <div>
                    <h4 className="font-bold text-slate-900 text-sm">{d.foodName}</h4>
                    <div className="flex items-center justify-between gap-2 mt-1">
                      <p className="text-xs text-slate-500">{d.donorName}</p>
                      <TrustBadge
                        score={d.donorTrustScore}
                        tier={d.donorTrustTier}
                        userId={d.donorId}
                        label={d.donorName}
                        compact
                      />
                    </div>
                  </div>

                  <p className="text-xs text-slate-600 line-clamp-2 leading-relaxed">
                    {d.foodDescription || 'Fresh surplus prepared food ready for immediate community redistribution.'}
                  </p>

                  {(() => {
                    const remaining = d.remainingQuantity ?? d.quantity;
                    const allocated = d.allocatedQuantity ?? Math.max(0, d.quantity - remaining);
                    const pct = d.quantity > 0 ? Math.min(100, Math.round((allocated / d.quantity) * 100)) : 0;
                    return (
                      <div className="p-2.5 bg-white rounded-xl border border-slate-200/70 space-y-1 text-[11px]">
                        <div className="flex items-center justify-between">
                          <span className="text-slate-500">Remaining:</span>
                          <strong className="text-emerald-700">
                            {remaining.toFixed(1)} / {d.quantity} {d.measurementUnit}
                          </strong>
                        </div>
                        <div className="w-full h-1.5 bg-slate-200 rounded-full overflow-hidden">
                          <div className="h-full bg-emerald-500 rounded-full" style={{ width: `${Math.max(0, 100 - pct)}%` }} />
                        </div>
                      </div>
                    );
                  })()}

                  <div className="space-y-1.5 text-xs text-slate-600 pt-2 border-t border-slate-200/60">
                    <div className="flex items-center justify-between">
                      <span className="text-slate-400">Deadline:</span>
                      <strong className="text-amber-700 flex items-center">
                        <Clock className="w-3.5 h-3.5 mr-1" />
                        {new Date(d.pickupDeadline).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </strong>
                    </div>

                    {d.distanceKm !== undefined && (
                      <div className="flex items-center justify-between">
                        <span className="text-slate-400">Distance:</span>
                        <strong className="text-emerald-700">{d.distanceKm.toFixed(1)} km away</strong>
                      </div>
                    )}
                  </div>
                </div>

                <div className="p-4 bg-white border-t border-slate-100">
                  <button
                    onClick={() => handleOpenClaim(d)}
                    disabled={isUnverifiedReceiver || (d.remainingQuantity ?? d.quantity) <= 0}
                    className="w-full py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-bold transition shadow-sm disabled:opacity-50"
                  >
                    {isUnverifiedReceiver ? 'Verification Pending' : 'Claim Full or Partial Portion'}
                  </button>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="text-center py-12 space-y-2">
            <Package className="w-10 h-10 text-slate-300 mx-auto" />
            <h4 className="text-sm font-semibold text-slate-700">No active surplus food within radius</h4>
            <p className="text-xs text-slate-400">Check the live map or increase your search distance.</p>
          </div>
        )}
      </div>

      {/* Claim Modal */}
      {selectedDonation && (
        <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/50 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white max-w-lg w-full rounded-3xl p-6 sm:p-8 space-y-6 shadow-2xl animate-in fade-in duration-200">
            <div className="flex items-start justify-between border-b border-slate-100 pb-3">
              <div>
                <span className="text-[10px] font-bold text-emerald-600 uppercase tracking-wider">Submit Redistribution Claim</span>
                <h3 className="text-lg font-bold text-slate-900">{selectedDonation.foodName}</h3>
                <p className="text-xs text-slate-500">{selectedDonation.donorName} &bull; {selectedDonation.pickupAddress}</p>
              </div>
              <button onClick={() => setSelectedDonation(null)} className="p-1 text-slate-400 hover:text-slate-600">
                &times;
              </button>
            </div>

            <form onSubmit={handleConfirmClaim} className="space-y-4 text-xs">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block font-bold text-slate-700 mb-1">
                    Partial / Full Quantity ({selectedDonation.measurementUnit})
                  </label>
                  <input
                    type="number"
                    step="0.5"
                    min="0.5"
                    max={selectedDonation.remainingQuantity ?? selectedDonation.quantity}
                    required
                    value={claimQty}
                    onChange={(e) => setClaimQty(parseFloat(e.target.value))}
                    className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl"
                  />
                  <span className="text-[10px] text-emerald-700 font-semibold mt-0.5 block">
                    Remaining available: {(selectedDonation.remainingQuantity ?? selectedDonation.quantity).toFixed(1)} / {selectedDonation.quantity} {selectedDonation.measurementUnit}
                  </span>
                </div>

                <div>
                  <label className="block font-bold text-slate-700 mb-1">Proposed Pickup Time *</label>
                  <input
                    type="datetime-local"
                    required
                    value={claimTime}
                    onChange={(e) => setClaimTime(e.target.value)}
                    className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl"
                  />
                  <span className="text-[10px] text-amber-600 mt-0.5 block">Deadline: {new Date(selectedDonation.pickupDeadline).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
                </div>
              </div>

              <div>
                <label className="block font-bold text-slate-700 mb-1">Pickup Notes / Transport Details</label>
                <textarea
                  rows={2}
                  value={claimNotes}
                  onChange={(e) => setClaimNotes(e.target.value)}
                  placeholder="e.g. Insulated containers ready for partial pickup."
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl"
                />
              </div>

              <label className="flex items-start space-x-2.5 p-3 bg-indigo-50/70 border border-indigo-100 rounded-xl cursor-pointer">
                <input
                  type="checkbox"
                  checked={volunteerRequested}
                  onChange={(e) => setVolunteerRequested(e.target.checked)}
                  className="mt-0.5 accent-indigo-600 rounded"
                />
                <div className="text-[11px] text-indigo-900">
                  <div className="font-bold flex items-center space-x-1">
                    <Truck className="w-3.5 h-3.5 text-indigo-600" />
                    <span>Request Volunteer Courier Pickup</span>
                  </div>
                  <p className="text-indigo-700 mt-0.5">Open a dispatch task for verified volunteers once the donor approves.</p>
                </div>
              </label>

              <div className="p-3 bg-emerald-50 text-emerald-900 rounded-xl border border-emerald-100 text-[11px] space-y-1">
                <strong>Atomic Partial Allocation & QR Passport:</strong>
                <p>Donor approval atomically locks your portion and generates an HMAC-signed QR Food Passport.</p>
              </div>

              <div className="flex items-center justify-end space-x-2 pt-2">
                <button
                  type="button"
                  onClick={() => setSelectedDonation(null)}
                  className="px-4 py-2 bg-slate-100 text-slate-600 rounded-xl font-semibold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={claiming}
                  className="px-5 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl font-bold flex items-center space-x-1 disabled:opacity-50"
                >
                  {claiming ? <Loader2 className="w-4 h-4 animate-spin" /> : null}
                  <span>Submit Claim</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
