import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { claimApi, volunteerApi } from '../services/api';
import { Claim } from '../types';
import { StatusBadge } from '../components/StatusBadge';
import { TrustBadge } from '../components/TrustBadge';
import { QrPassportModal } from '../components/QrPassportModal';
import {
  Check,
  X,
  Phone,
  Clock,
  Package,
  MapPin,
  Loader2,
  Calendar,
  AlertCircle,
  FileCheck,
  QrCode,
  Truck,
  ShieldAlert,
  UserCheck,
} from 'lucide-react';

export const DonorClaimsPage: React.FC = () => {
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
      console.error('Failed to load donor claims:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleApprove = async (claimId: string) => {
    setActionId(claimId);
    try {
      await claimApi.approve(claimId);
      loadClaims();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Approval failed');
    } finally {
      setActionId(null);
    }
  };

  const handleReject = async (claimId: string) => {
    setActionId(claimId);
    try {
      await claimApi.reject(claimId, 'Schedule incompatibility');
      loadClaims();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Rejection failed');
    } finally {
      setActionId(null);
    }
  };

  const handleRequestVolunteer = async (claimId: string) => {
    setActionId(claimId);
    try {
      await volunteerApi.requestSupport(claimId);
      await loadClaims();
      alert('Volunteer courier dispatch requested!');
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to request volunteer');
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
        <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">Claim & Pickup Management</h1>
        <p className="text-xs sm:text-sm text-slate-500 mt-1">Review charity claim requests, confirm pickup schedules, and track collections.</p>
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
                    <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">Donation Listing</span>
                    <h3 className="font-bold text-slate-900 text-base">{c.foodName || 'Surplus Food'}</h3>
                  </div>
                  <StatusBadge status={c.status} />
                </div>

                <div className="p-4 bg-slate-50 rounded-2xl border border-slate-100 space-y-2 text-xs">
                  <div className="flex items-center justify-between gap-2">
                    <span className="text-slate-500 font-medium">Claiming Charity:</span>
                    <div className="flex items-center gap-2">
                      <strong className="text-slate-900">{c.receiverName}</strong>
                      <TrustBadge
                        score={c.receiverTrustScore}
                        tier={c.receiverTrustTier}
                        userId={c.receiverId}
                        label={c.receiverName}
                        compact
                      />
                    </div>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-slate-500 font-medium">Claimed Batch:</span>
                    <strong className="text-slate-900">{c.claimedQuantity} {c.measurementUnit}</strong>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-slate-500 font-medium">Proposed Pickup:</span>
                    <strong className="text-amber-700">
                      {c.proposedPickupTime ? new Date(c.proposedPickupTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : 'ASAP'}
                    </strong>
                  </div>
                  {c.receiverPhone && (
                    <div className="flex items-center justify-between pt-1 border-t border-slate-200/60">
                      <span className="text-slate-500 font-medium">Charity Phone:</span>
                      <strong className="text-emerald-700">{c.receiverPhone}</strong>
                    </div>
                  )}
                  {c.assignedVolunteerName && (
                    <div className="p-2 bg-indigo-50 border border-indigo-100 rounded-xl text-[11px] text-indigo-900 flex items-center justify-between">
                      <span className="flex items-center space-x-1 font-semibold">
                        <UserCheck className="w-3.5 h-3.5 text-indigo-600" />
                        <span>Courier: {c.assignedVolunteerName}</span>
                      </span>
                      {c.assignedVolunteerPhone && <span className="font-bold">{c.assignedVolunteerPhone}</span>}
                    </div>
                  )}
                  {c.volunteerRequested && !c.assignedVolunteerId && c.status === 'APPROVED' && (
                    <div className="p-2 bg-indigo-50/70 text-indigo-800 rounded-lg text-[11px] font-medium">
                      Volunteer pickup requested &bull; Visible on Volunteer Mission Board
                    </div>
                  )}
                  {c.rescueRelayStatus === 'AWAITING_DONOR_APPROVAL' && (
                    <Link
                      to="/rescue-relay"
                      className="p-2.5 bg-orange-50 border border-orange-200 text-orange-900 rounded-xl text-[11px] font-semibold flex items-center space-x-2 hover:bg-orange-100 transition"
                    >
                      <ShieldAlert className="w-4 h-4 text-orange-600 shrink-0" />
                      <span>Pickup Missed — Release via Rescue Relay to re-match NGOs</span>
                    </Link>
                  )}
                  {c.notes && (
                    <p className="text-[11px] text-slate-500 italic pt-1">"{c.notes}"</p>
                  )}
                </div>
              </div>

              {c.status === 'PENDING' && (
                <div className="flex items-center space-x-2 pt-2">
                  <button
                    onClick={() => handleApprove(c.id)}
                    disabled={actionId === c.id}
                    className="flex-1 py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-bold transition flex items-center justify-center space-x-1.5 shadow-sm disabled:opacity-50"
                  >
                    {actionId === c.id ? <Loader2 className="w-3.5 h-3.5 animate-spin" /> : <Check className="w-4 h-4" />}
                    <span>Approve Claim</span>
                  </button>
                  <button
                    onClick={() => handleReject(c.id)}
                    disabled={actionId === c.id}
                    className="px-4 py-2.5 bg-slate-100 hover:bg-rose-50 text-slate-600 hover:text-rose-700 rounded-xl text-xs font-semibold transition"
                  >
                    Reject
                  </button>
                </div>
              )}

              {(c.status === 'APPROVED' || c.status === 'PICKED_UP') && (
                <div className="space-y-2 pt-1">
                  <div className="p-2.5 bg-purple-50 text-purple-900 rounded-xl border border-purple-200 text-[11px] flex items-center space-x-2">
                    <Clock className="w-3.5 h-3.5 text-purple-600 flex-shrink-0" />
                    <span>
                      {c.qrVerified
                        ? 'QR Food Passport verified at pickup.'
                        : 'Scan / verify the NGO or volunteer QR Food Passport on handover.'}
                    </span>
                  </div>
                  <button
                    onClick={() => setPassportClaimId(c.id)}
                    className="w-full py-2.5 bg-slate-900 hover:bg-slate-800 text-white rounded-xl text-xs font-bold transition flex items-center justify-center space-x-2 shadow-sm"
                  >
                    <QrCode className="w-4 h-4 text-emerald-400" />
                    <span>{c.qrVerified ? 'View Verified QR Passport' : 'Verify Pickup via QR Passport'}</span>
                  </button>
                  {c.status === 'APPROVED' && !c.assignedVolunteerId && !c.volunteerRequested && (
                    <button
                      onClick={() => handleRequestVolunteer(c.id)}
                      disabled={actionId === c.id}
                      className="w-full py-2 bg-indigo-50 hover:bg-indigo-100 text-indigo-700 border border-indigo-200 rounded-xl text-xs font-bold transition flex items-center justify-center space-x-1.5 disabled:opacity-50"
                    >
                      <Truck className="w-3.5 h-3.5" />
                      <span>Request Volunteer Courier</span>
                    </button>
                  )}
                </div>
              )}

              {c.status === 'COMPLETED' && (
                <div className="p-3 bg-teal-50 text-teal-900 rounded-2xl border border-teal-200 text-xs flex items-center space-x-2">
                  <FileCheck className="w-4 h-4 text-teal-600 flex-shrink-0" />
                  <span>Redistribution completed. Meals saved & impact recorded in sustainability metrics!</span>
                </div>
              )}

              {c.status === 'RELEASED' && (
                <div className="p-3 bg-orange-50 text-orange-900 rounded-2xl border border-orange-200 text-xs flex items-center space-x-2">
                  <AlertCircle className="w-4 h-4 text-orange-600 flex-shrink-0" />
                  <span>Portion released via Rescue Relay & restored to available inventory.</span>
                </div>
              )}
            </div>
          ))}
        </div>
      ) : (
        <div className="text-center py-20 bg-white rounded-3xl border border-slate-200 p-8 space-y-3">
          <Package className="w-12 h-12 text-slate-300 mx-auto" />
          <h4 className="text-sm font-semibold text-slate-700">No claim requests for '{filter}'</h4>
          <p className="text-xs text-slate-400">Claims submitted by verified NGOs will appear here.</p>
        </div>
      )}

      {passportClaimId && (
        <QrPassportModal
          claimId={passportClaimId}
          canVerify={true}
          onVerified={() => loadClaims()}
          onClose={() => setPassportClaimId(null)}
        />
      )}
    </div>
  );
};
