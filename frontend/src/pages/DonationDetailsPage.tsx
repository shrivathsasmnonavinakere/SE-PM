import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { donationApi, claimApi, volunteerApi } from '../services/api';
import { Donation, Claim } from '../types';
import { StatusBadge } from '../components/StatusBadge';
import { SafetyBadge } from '../components/SafetyBadge';
import { TrustBadge } from '../components/TrustBadge';
import { QrPassportModal } from '../components/QrPassportModal';
import { LeafletMap } from '../components/LeafletMap';
import {
  ArrowLeft,
  Clock,
  Package,
  MapPin,
  ShieldCheck,
  Sparkles,
  Phone,
  Calendar,
  AlertTriangle,
  Check,
  X,
  Loader2,
  QrCode,
  Truck,
  UserCheck,
} from 'lucide-react';

export const DonationDetailsPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [donation, setDonation] = useState<Donation | null>(null);
  const [claims, setClaims] = useState<Claim[]>([]);
  const [loading, setLoading] = useState(true);
  const [passportClaimId, setPassportClaimId] = useState<string | null>(null);
  const [actionId, setActionId] = useState<string | null>(null);

  useEffect(() => {
    if (id) loadData(id);
  }, [id]);

  const loadData = async (donationId: string) => {
    try {
      const [donRes, claimsRes] = await Promise.all([
        donationApi.getById(donationId),
        claimApi.getByDonation(donationId),
      ]);
      if (donRes.success && donRes.data) setDonation(donRes.data);
      if (claimsRes.success && claimsRes.data) setClaims(claimsRes.data);
    } catch (err) {
      console.error('Failed to load donation details:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleApproveClaim = async (claimId: string) => {
    try {
      await claimApi.approve(claimId);
      if (id) loadData(id);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to approve claim');
    }
  };

  const handleRejectClaim = async (claimId: string) => {
    try {
      await claimApi.reject(claimId);
      if (id) loadData(id);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to reject claim');
    }
  };

  const handleRequestVolunteer = async (claimId: string) => {
    setActionId(claimId);
    try {
      await volunteerApi.requestSupport(claimId);
      if (id) await loadData(id);
      alert('Volunteer courier dispatch requested!');
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to request volunteer');
    } finally {
      setActionId(null);
    }
  };

  if (loading) {
    return (
      <div className="min-h-[70vh] flex items-center justify-center">
        <Loader2 className="w-8 h-8 animate-spin text-emerald-600" />
      </div>
    );
  }

  if (!donation) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-16 text-center space-y-4">
        <h2 className="text-xl font-bold text-slate-800">Donation Not Found</h2>
        <Link to="/donor/dashboard" className="text-xs font-semibold text-emerald-600">
          Back to Dashboard
        </Link>
      </div>
    );
  }

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-8">
      {/* Back button */}
      <div>
        <Link
          to="/donor/my-donations"
          className="inline-flex items-center space-x-1.5 text-xs font-semibold text-slate-500 hover:text-emerald-700 transition"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Back to My Listings</span>
        </Link>
      </div>

      {/* Main Details Card */}
      <div className="bg-white rounded-3xl border border-slate-200 overflow-hidden shadow-sm">
        {donation.foodImage && (
          <img src={donation.foodImage} alt={donation.foodName} className="h-64 w-full object-cover" />
        )}

        <div className="p-8 space-y-6">
          <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
            <div>
              <div className="flex items-center space-x-2 mb-2">
                <StatusBadge status={donation.status} />
                <SafetyBadge indicator={donation.safetyIndicator} />
                <span className="text-xs font-semibold text-slate-500">{donation.foodCategory}</span>
              </div>
              <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900">{donation.foodName}</h1>
              <p className="text-xs text-slate-500 mt-1 font-medium">{donation.donorName}</p>
            </div>

            <Link
              to={`/donor/donations/${donation.id}/matches`}
              className="px-4 py-2 bg-emerald-50 text-emerald-700 hover:bg-emerald-100 rounded-xl text-xs font-bold transition flex items-center space-x-1.5"
            >
              <Sparkles className="w-4 h-4 text-emerald-600" />
              <span>View Top 5 AI Matches</span>
            </Link>
          </div>

          <p className="text-xs sm:text-sm text-slate-600 leading-relaxed bg-slate-50/70 p-4 rounded-2xl border border-slate-100">
            {donation.foodDescription || 'No detailed description provided.'}
          </p>

          {/* Key Specs */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 text-xs">
            <div className="p-4 bg-slate-50 rounded-2xl border border-slate-100">
              <span className="text-slate-400 block text-[11px] mb-1">Remaining / Total</span>
              <strong className="text-slate-900 text-sm">
                {(donation.remainingQuantity ?? donation.quantity).toFixed(1)} / {donation.quantity} {donation.measurementUnit}
              </strong>
            </div>

            <div className="p-4 bg-slate-50 rounded-2xl border border-slate-100">
              <span className="text-slate-400 block text-[11px] mb-1">Prepared At</span>
              <strong className="text-slate-900 text-xs">
                {new Date(donation.preparationDateTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
              </strong>
            </div>

            <div className="p-4 bg-slate-50 rounded-2xl border border-slate-100">
              <span className="text-slate-400 block text-[11px] mb-1">Pickup Deadline</span>
              <strong className="text-amber-700 text-xs flex items-center">
                <Clock className="w-3.5 h-3.5 mr-1" />
                {new Date(donation.pickupDeadline).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
              </strong>
            </div>

            <div className="p-4 bg-slate-50 rounded-2xl border border-slate-100">
              <span className="text-slate-400 block text-[11px] mb-1">Handling</span>
              <strong className="text-slate-800 text-xs truncate block">{donation.foodHandlingDetails || 'Standard'}</strong>
            </div>
          </div>

          {/* Partial Food Distribution Progress Bar */}
          {(() => {
            const remaining = donation.remainingQuantity ?? donation.quantity;
            const allocated = donation.allocatedQuantity ?? Math.max(0, donation.quantity - remaining);
            const pct = donation.quantity > 0 ? Math.min(100, Math.round((allocated / donation.quantity) * 100)) : 0;
            return (
              <div className="p-4 bg-emerald-50/50 rounded-2xl border border-emerald-100 space-y-2 text-xs">
                <div className="flex items-center justify-between">
                  <span className="font-bold text-slate-800">Partial Food Distribution Allocation</span>
                  <span className="font-bold text-emerald-700">
                    {allocated.toFixed(1)} {donation.measurementUnit} allocated ({pct}%) &bull; {donation.allocationCount || 0} split(s)
                  </span>
                </div>
                <div className="w-full h-2 bg-slate-200 rounded-full overflow-hidden">
                  <div className="h-full bg-emerald-600 rounded-full transition-all" style={{ width: `${pct}%` }} />
                </div>
              </div>
            );
          })()}

          {/* Location & Map Section */}
          <div className="space-y-3 pt-2">
            <h4 className="text-xs font-bold text-slate-800 flex items-center space-x-1.5">
              <MapPin className="w-4 h-4 text-emerald-600" />
              <span>Pickup Location: {donation.pickupAddress}</span>
            </h4>
            <LeafletMap
              donations={[donation]}
              userLat={donation.latitude || 12.9716}
              userLon={donation.longitude || 77.5946}
              height="280px"
            />
          </div>

          {/* Safety Notes Box */}
          {donation.safetyNotes && (
            <div className="p-4 bg-emerald-50 text-emerald-900 rounded-2xl border border-emerald-100 text-xs space-y-1">
              <span className="font-bold flex items-center space-x-1 text-emerald-800">
                <ShieldCheck className="w-4 h-4" />
                <span>Food Safety Screening Record</span>
              </span>
              <p className="text-[11px] text-emerald-800 leading-relaxed">{donation.safetyNotes}</p>
            </div>
          )}
        </div>
      </div>

      {/* Claims Management for this Donation */}
      <div className="bg-white p-6 sm:p-8 rounded-3xl border border-slate-200 shadow-sm space-y-4">
        <h3 className="text-base font-bold text-slate-900">Incoming Claim Requests ({claims.length})</h3>

        {claims.length > 0 ? (
          <div className="divide-y divide-slate-100">
            {claims.map((c) => (
              <div key={c.id} className="py-4 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
                <div className="space-y-1 text-xs">
                  <div className="flex flex-wrap items-center gap-2">
                    <span className="font-bold text-slate-900 text-sm">{c.receiverName}</span>
                    <StatusBadge status={c.status} />
                    <TrustBadge
                      score={c.receiverTrustScore}
                      tier={c.receiverTrustTier}
                      userId={c.receiverId}
                      label={c.receiverName}
                      compact
                    />
                  </div>
                  <p className="text-slate-500">{c.receiverAddress} &bull; Tel: {c.receiverPhone || 'N/A'}</p>
                  <p className="text-slate-600">
                    Requested: <strong className="text-slate-900">{c.claimedQuantity} {c.measurementUnit}</strong> &bull; Proposed Pickup: <strong>{c.proposedPickupTime ? new Date(c.proposedPickupTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : 'ASAP'}</strong>
                  </p>
                  {c.assignedVolunteerName && (
                    <p className="text-[11px] text-indigo-700 font-semibold flex items-center space-x-1">
                      <UserCheck className="w-3.5 h-3.5" />
                      <span>Volunteer Courier: {c.assignedVolunteerName} ({c.assignedVolunteerPhone || 'N/A'})</span>
                    </p>
                  )}
                  {c.rescueRelayStatus === 'AWAITING_DONOR_APPROVAL' && (
                    <Link
                      to="/rescue-relay"
                      className="inline-flex items-center space-x-1 text-[11px] font-bold text-orange-700 hover:underline"
                    >
                      <AlertTriangle className="w-3.5 h-3.5" />
                      <span>Pickup grace window exceeded — Open Rescue Relay</span>
                    </Link>
                  )}
                  {c.notes && <p className="text-[11px] text-slate-400 italic">Notes: "{c.notes}"</p>}
                </div>

                {c.status === 'PENDING' && (
                  <div className="flex items-center space-x-2 w-full sm:w-auto">
                    <button
                      onClick={() => handleApproveClaim(c.id)}
                      className="flex-1 sm:flex-none px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs rounded-xl transition flex items-center justify-center space-x-1"
                    >
                      <Check className="w-4 h-4" />
                      <span>Approve</span>
                    </button>
                    <button
                      onClick={() => handleRejectClaim(c.id)}
                      className="flex-1 sm:flex-none px-3 py-2 bg-slate-100 hover:bg-rose-50 text-slate-600 hover:text-rose-700 font-semibold text-xs rounded-xl transition"
                    >
                      Reject
                    </button>
                  </div>
                )}

                {(c.status === 'APPROVED' || c.status === 'PICKED_UP') && (
                  <div className="flex flex-wrap items-center gap-2 w-full sm:w-auto">
                    <button
                      onClick={() => setPassportClaimId(c.id)}
                      className="px-3.5 py-2 bg-slate-900 hover:bg-slate-800 text-white font-bold text-xs rounded-xl transition flex items-center space-x-1.5"
                    >
                      <QrCode className="w-3.5 h-3.5 text-emerald-400" />
                      <span>{c.qrVerified ? 'Verified Passport' : 'Verify QR Pickup'}</span>
                    </button>
                    {c.status === 'APPROVED' && !c.assignedVolunteerId && !c.volunteerRequested && (
                      <button
                        onClick={() => handleRequestVolunteer(c.id)}
                        disabled={actionId === c.id}
                        className="px-3 py-2 bg-indigo-50 hover:bg-indigo-100 text-indigo-700 border border-indigo-200 font-bold text-xs rounded-xl transition flex items-center space-x-1 disabled:opacity-50"
                      >
                        <Truck className="w-3.5 h-3.5" />
                        <span>Request Volunteer</span>
                      </button>
                    )}
                  </div>
                )}
              </div>
            ))}
          </div>
        ) : (
          <p className="text-xs text-slate-400 py-4 text-center">No receiver claims submitted for this listing yet.</p>
        )}
      </div>

      {passportClaimId && (
        <QrPassportModal
          claimId={passportClaimId}
          canVerify={true}
          onVerified={() => id && loadData(id)}
          onClose={() => setPassportClaimId(null)}
        />
      )}
    </div>
  );
};
