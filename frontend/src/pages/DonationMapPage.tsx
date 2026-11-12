import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { donationApi, claimApi } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { Donation } from '../types';
import { LeafletMap } from '../components/LeafletMap';
import { toLocalDateTimeInputValue } from '../utils/datetime';
import { MapPin, Loader2, Package, Sparkles, Truck, ShieldAlert } from 'lucide-react';

export const DonationMapPage: React.FC = () => {
  const { user } = useAuth();
  const [donations, setDonations] = useState<Donation[]>([]);
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
    loadMapDonations();
  }, []);

  const loadMapDonations = async () => {
    try {
      const res = await donationApi.getAvailable({
        lat: user?.latitude || 12.9716,
        lon: user?.longitude || 77.5946,
        maxDistanceKm: 40,
      });
      if (res.success && res.data) setDonations(res.data);
    } catch (err) {
      console.error('Failed to load map donations:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenClaim = (d: Donation) => {
    if (isUnverifiedReceiver) {
      alert('Your NGO account is pending Admin verification. Please upload verification docs in Profile first.');
      return;
    }
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
      await loadMapDonations();
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
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-6">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 border-b border-slate-200 pb-5">
        <div>
          <div className="flex items-center space-x-2">
            <span className="p-2 bg-emerald-100 text-emerald-700 rounded-xl">
              <MapPin className="w-5 h-5" />
            </span>
            <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
              Interactive Live Food Map
            </h1>
          </div>
          <p className="text-xs sm:text-sm text-slate-500 mt-1">
            Browse available food donations across the region with OpenStreetMap markers and live radius distance.
          </p>
        </div>

        <div className="flex items-center space-x-2 text-xs bg-slate-50 px-3 py-1.5 rounded-xl border border-slate-200 text-slate-600 font-medium">
          <span className="w-2.5 h-2.5 rounded-full bg-emerald-500 mr-1 animate-pulse" />
          <span>{donations.length} Active Food Pins</span>
        </div>
      </div>

      {isUnverifiedReceiver && (
        <div className="p-4 bg-amber-50 border border-amber-200 rounded-2xl flex flex-col sm:flex-row sm:items-center justify-between gap-3 text-xs text-amber-900">
          <div className="flex items-center space-x-2">
            <ShieldAlert className="w-5 h-5 text-amber-600 shrink-0" />
            <span>
              Your NGO verification is <strong>{user?.verificationStatus}</strong>. Admin approval is required before claiming food.
            </span>
          </div>
          <Link to="/profile" className="px-3 py-1.5 bg-amber-600 text-white font-bold rounded-xl hover:bg-amber-700 whitespace-nowrap">
            Update Docs
          </Link>
        </div>
      )}

      {/* Full-width Map Container */}
      <LeafletMap
        donations={donations}
        userLat={user?.latitude || 12.9716}
        userLon={user?.longitude || 77.5946}
        radiusKm={25}
        onClaim={handleOpenClaim}
        height="620px"
      />

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
                  placeholder="e.g. Volunteer van arriving with insulated food crates."
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
                  <p className="text-indigo-700 mt-0.5">Open a pickup mission for nearby AnnaDhata volunteer couriers once approved.</p>
                </div>
              </label>

              <div className="p-3 bg-emerald-50 text-emerald-900 rounded-xl border border-emerald-100 text-[11px] space-y-1">
                <strong>Atomic Partial Allocation & QR Passport:</strong>
                <p>Donor approval reserves your requested quantity atomically and issues a QR Food Passport.</p>
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
