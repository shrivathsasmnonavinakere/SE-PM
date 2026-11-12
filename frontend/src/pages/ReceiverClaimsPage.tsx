import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { claimApi, volunteerApi } from '../services/api';
import { Claim } from '../types';
import { StatusBadge } from '../components/StatusBadge';
import { QrPassportModal } from '../components/QrPassportModal';
import {
  Package,
  MapPin,
  Clock,
  Phone,
  CheckCircle,
  Truck,
  Loader2,
  AlertCircle,
  Calendar,
  QrCode,
  ShieldAlert,
  UserCheck,
} from 'lucide-react';

export const ReceiverClaimsPage: React.FC = () => {
  const [claims, setClaims] = useState<Claim[]>([]);
  const [filter, setFilter] = useState<string>('ALL');
  const [loading, setLoading] = useState(true);
  const [actionId, setActionId] = useState<string | null>(null);
  const [passportClaimId, setPassportClaimId] = useState<string | null>(null);

  useEffect(() => {
    loadClaims();
  }, []);

  const loadClaims = async () => {
    try {
      const res = await claimApi.getMy();
      if (res.success && res.data) setClaims(res.data);
    } catch (err) {
      console.error('Failed to load receiver claims:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleMarkPickedUp = async (claimId: string) => {
    setActionId(claimId);
    try {
      await claimApi.markPickedUp(claimId);
      loadClaims();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to update pickup status');
    } finally {
      setActionId(null);
    }
  };

  const handleMarkComplete = async (claimId: string) => {
    setActionId(claimId);
    try {
      await claimApi.markCompleted(claimId);
      loadClaims();
      alert('Redistribution completed! Sustainability and hunger impact metrics have been successfully updated.');
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to complete redistribution');
    } finally {
      setActionId(null);
    }
  };

  const handleRequestVolunteer = async (claimId: string) => {
    setActionId(claimId);
    try {
      await volunteerApi.requestSupport(claimId);
      await loadClaims();
      alert('Volunteer courier dispatch requested! Nearby volunteers have been notified.');
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to request volunteer support');
    } finally {
      setActionId(null);
    }
  };

  const filtered = claims.filter((c) => {
    if (filter === 'ALL') return true;
    return c.status === filter;
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
        <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">My Redistribution Claims</h1>
        <p className="text-xs sm:text-sm text-slate-500 mt-1">Track claim approvals, arrange volunteer pickups, and confirm food handovers.</p>
      </div>

      {/* Filter Tabs */}
      <div className="flex items-center space-x-2 overflow-x-auto pb-2 text-xs">
        {['ALL', 'PENDING', 'APPROVED', 'PICKED_UP', 'COMPLETED', 'RELEASED', 'REJECTED'].map((st) => (
          <button
            key={st}
            onClick={() => setFilter(st)}
            className={`px-3 py-1.5 rounded-xl font-bold transition whitespace-nowrap ${
              filter === st
                ? 'bg-emerald-600 text-white shadow-sm'
                : 'bg-white text-slate-600 border border-slate-200 hover:bg-slate-50'
            }`}
          >
            {st.replace(/_/g, ' ')}
          </button>
        ))}
      </div>

      {/* Claims List */}
      {filtered.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
          {filtered.map((c) => (
            <div key={c.id} className="p-6 bg-white rounded-3xl border border-slate-200 shadow-sm space-y-4 flex flex-col justify-between">
              <div className="space-y-3">
                <div className="flex items-start justify-between">
                  <div>
                    <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">Claimed Surplus Batch</span>
                    <h3 className="font-bold text-slate-900 text-base">{c.foodName || 'Food Item'}</h3>
                  </div>
                  <StatusBadge status={c.status} />
                </div>

                <div className="p-4 bg-slate-50 rounded-2xl border border-slate-100 space-y-2 text-xs text-slate-600">
                  <div className="flex items-center justify-between">
                    <span className="text-slate-400">Claimed Quantity:</span>
                    <strong className="text-slate-900">{c.claimedQuantity} {c.measurementUnit}</strong>
                  </div>

                  {c.donorName && (
                    <div className="flex items-center justify-between">
                      <span className="text-slate-400">Donor:</span>
                      <strong className="text-slate-800">{c.donorName}</strong>
                    </div>
                  )}

                  {c.pickupAddress && (
                    <div className="flex items-center space-x-1.5 text-[11px] text-slate-500">
                      <MapPin className="w-3.5 h-3.5 text-emerald-600 shrink-0" />
                      <span className="truncate">{c.pickupAddress}</span>
                    </div>
                  )}

                  <div className="flex items-center justify-between">
                    <span className="text-slate-400">Confirmed Pickup:</span>
                    <strong className="text-purple-700">
                      {c.confirmedPickupTime
                        ? new Date(c.confirmedPickupTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
                        : c.proposedPickupTime
                        ? `${new Date(c.proposedPickupTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} (Proposed)`
                        : 'ASAP'}
                    </strong>
                  </div>

                  {c.assignedVolunteerName && (
                    <div className="p-2.5 bg-indigo-50 border border-indigo-100 rounded-xl text-[11px] text-indigo-900 flex items-center justify-between">
                      <span className="flex items-center space-x-1.5 font-semibold">
                        <UserCheck className="w-3.5 h-3.5 text-indigo-600" />
                        <span>Volunteer: {c.assignedVolunteerName}</span>
                      </span>
                      {c.assignedVolunteerPhone && (
                        <span className="flex items-center space-x-1 text-indigo-700 font-bold">
                          <Phone className="w-3 h-3" />
                          <span>{c.assignedVolunteerPhone}</span>
                        </span>
                      )}
                    </div>
                  )}

                  {c.volunteerRequested && !c.assignedVolunteerId && (c.status === 'APPROVED' || c.status === 'PENDING') && (
                    <div className="p-2 bg-indigo-50/70 text-indigo-800 rounded-lg text-[11px] font-medium">
                      Volunteer dispatch requested &bull; Awaiting nearby courier acceptance
                    </div>
                  )}

                  {c.rescueRelayStatus === 'AWAITING_DONOR_APPROVAL' && (
                    <Link
                      to="/rescue-relay"
                      className="p-2.5 bg-orange-50 border border-orange-200 text-orange-900 rounded-xl text-[11px] font-semibold flex items-center space-x-2 hover:bg-orange-100 transition"
                    >
                      <ShieldAlert className="w-4 h-4 text-orange-600 shrink-0" />
                      <span>Pickup grace window exceeded — Rescue Relay alert active (View details)</span>
                    </Link>
                  )}

                  {c.rejectionReason && (
                    <div className="p-2 bg-rose-50 text-rose-700 rounded-lg text-[11px]">
                      Reason: {c.rejectionReason}
                    </div>
                  )}
                </div>
              </div>

              {/* Status Actions */}
              <div className="pt-2 space-y-2">
                {c.status === 'PENDING' && (
                  <div className="p-3 bg-amber-50 text-amber-800 rounded-2xl border border-amber-200 text-xs flex items-center space-x-2">
                    <Clock className="w-4 h-4 text-amber-600 flex-shrink-0" />
                    <span>Awaiting donor confirmation. You will receive an alert once accepted.</span>
                  </div>
                )}

                {(c.status === 'APPROVED' || c.status === 'PICKED_UP') && (
                  <div className="space-y-2">
                    <button
                      onClick={() => setPassportClaimId(c.id)}
                      className="w-full py-2.5 bg-slate-900 hover:bg-slate-800 text-white rounded-xl text-xs font-bold transition flex items-center justify-center space-x-2 shadow-sm"
                    >
                      <QrCode className="w-4 h-4 text-emerald-400" />
                      <span>Show QR Food Passport {c.qrVerified ? '(Verified)' : ''}</span>
                    </button>

                    {c.status === 'APPROVED' && !c.assignedVolunteerId && !c.volunteerRequested && (
                      <button
                        onClick={() => handleRequestVolunteer(c.id)}
                        disabled={actionId === c.id}
                        className="w-full py-2 bg-indigo-50 hover:bg-indigo-100 text-indigo-700 border border-indigo-200 rounded-xl text-xs font-bold transition flex items-center justify-center space-x-2 disabled:opacity-50"
                      >
                        {actionId === c.id ? <Loader2 className="w-3.5 h-3.5 animate-spin" /> : <Truck className="w-4 h-4" />}
                        <span>Request Volunteer Courier</span>
                      </button>
                    )}

                    {c.status === 'APPROVED' && (
                      <button
                        onClick={() => handleMarkPickedUp(c.id)}
                        disabled={actionId === c.id}
                        className="w-full py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-xs font-bold transition flex items-center justify-center space-x-2 shadow-sm disabled:opacity-50"
                      >
                        {actionId === c.id ? <Loader2 className="w-3.5 h-3.5 animate-spin" /> : <Truck className="w-4 h-4" />}
                        <span>Mark Food Collected / In Transit</span>
                      </button>
                    )}

                    <button
                      onClick={() => handleMarkComplete(c.id)}
                      disabled={actionId === c.id}
                      className="w-full py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-bold transition flex items-center justify-center space-x-2 disabled:opacity-50"
                    >
                      <CheckCircle className="w-4 h-4" />
                      <span>Confirm Complete Redistribution</span>
                    </button>
                  </div>
                )}

                {c.status === 'COMPLETED' && (
                  <div className="p-3 bg-teal-50 text-teal-900 rounded-2xl border border-teal-200 text-xs flex items-center space-x-2">
                    <CheckCircle className="w-4 h-4 text-teal-600 flex-shrink-0" />
                    <span>Redistribution completed. Meals saved & CO2 avoided recorded!</span>
                  </div>
                )}

                {c.status === 'RELEASED' && (
                  <div className="p-3 bg-orange-50 text-orange-900 rounded-2xl border border-orange-200 text-xs flex items-center space-x-2">
                    <AlertCircle className="w-4 h-4 text-orange-600 flex-shrink-0" />
                    <span>Released via Rescue Relay so another nearby NGO could rescue the food in time.</span>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="text-center py-20 bg-white rounded-3xl border border-slate-200 p-8 space-y-3">
          <Package className="w-12 h-12 text-slate-300 mx-auto" />
          <h4 className="text-sm font-semibold text-slate-700">No claims found for '{filter}'</h4>
          <p className="text-xs text-slate-400">Browse available donations to submit new collection claims.</p>
        </div>
      )}

      {passportClaimId && (
        <QrPassportModal
          claimId={passportClaimId}
          canVerify={false}
          onClose={() => setPassportClaimId(null)}
        />
      )}
    </div>
  );
};
