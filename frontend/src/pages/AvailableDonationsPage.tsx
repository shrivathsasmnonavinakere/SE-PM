import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { donationApi, claimApi } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { Donation, FoodCategory } from '../types';
import { toLocalDateTimeInputValue } from '../utils/datetime';
import { StatusBadge } from '../components/StatusBadge';
import { SafetyBadge } from '../components/SafetyBadge';
import { TrustBadge } from '../components/TrustBadge';
import { LeafletMap } from '../components/LeafletMap';
import {
  Search,
  Filter,
  MapPin,
  Clock,
  Package,
  Layers,
  Map as MapIcon,
  List as ListIcon,
  Loader2,
  Calendar,
  AlertCircle,
  Truck,
  ShieldAlert,
} from 'lucide-react';

const CATEGORIES: string[] = [
  'ALL',
  'Cooked food',
  'Rice and grains',
  'Vegetables',
  'Fruits',
  'Bread and bakery',
  'Packaged food',
  'Dairy products',
  'Other',
];

export const AvailableDonationsPage: React.FC = () => {
  const { user } = useAuth();
  const [donations, setDonations] = useState<Donation[]>([]);
  const [loading, setLoading] = useState(true);

  // Filters
  const [searchTerm, setSearchTerm] = useState('');
  const [category, setCategory] = useState<string>('ALL');
  const [maxDistance, setMaxDistance] = useState<number>(30); // 30 km radius
  const [viewMode, setViewMode] = useState<'list' | 'map'>('list');

  // Claim modal state
  const [selectedDonation, setSelectedDonation] = useState<Donation | null>(null);
  const [claimQty, setClaimQty] = useState<number>(10);
  const [claimTime, setClaimTime] = useState<string>('');
  const [claimNotes, setClaimNotes] = useState('');
  const [volunteerRequested, setVolunteerRequested] = useState(false);
  const [claiming, setClaiming] = useState(false);

  const isUnverifiedReceiver = user?.role === 'ROLE_RECEIVER' && user.verificationStatus !== 'VERIFIED';

  useEffect(() => {
    loadDonations();
  }, [category, maxDistance]);

  const loadDonations = async () => {
    setLoading(true);
    try {
      const params: any = {
        lat: user?.latitude || 12.9716,
        lon: user?.longitude || 77.5946,
        maxDistanceKm: maxDistance,
      };
      if (category !== 'ALL') {
        params.category = category;
      }
      const res = await donationApi.getAvailable(params);
      if (res.success && res.data) setDonations(res.data);
    } catch (err) {
      console.error('Failed to load available donations:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenClaim = (d: Donation) => {
    setSelectedDonation(d);
    const available = d.remainingQuantity ?? d.quantity;
    setClaimQty(available);
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
      await loadDonations();
      alert('Claim submitted successfully! The donor has been notified.');
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to submit claim.');
    } finally {
      setClaiming(false);
    }
  };

  const filtered = donations.filter((d) => {
    if (!searchTerm.trim()) return true;
    const term = searchTerm.toLowerCase();
    return (
      d.foodName.toLowerCase().includes(term) ||
      d.donorName.toLowerCase().includes(term) ||
      d.pickupAddress.toLowerCase().includes(term)
    );
  });

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-8">
      {/* Title & View Mode Toggle */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 border-b border-slate-200 pb-5">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">Available Food Donations</h1>
          <p className="text-xs sm:text-sm text-slate-500 mt-1">Real-time surplus food listings ready for pickup before expiry.</p>
        </div>

        {/* List / Map Toggle */}
        <div className="flex items-center bg-slate-100 p-1 rounded-2xl border border-slate-200">
          <button
            type="button"
            onClick={() => setViewMode('list')}
            className={`flex items-center space-x-1.5 px-3 py-1.5 rounded-xl text-xs font-bold transition ${
              viewMode === 'list' ? 'bg-white text-emerald-800 shadow-sm' : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            <ListIcon className="w-3.5 h-3.5" />
            <span>List View</span>
          </button>
          <button
            type="button"
            onClick={() => setViewMode('map')}
            className={`flex items-center space-x-1.5 px-3 py-1.5 rounded-xl text-xs font-bold transition ${
              viewMode === 'map' ? 'bg-white text-emerald-800 shadow-sm' : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            <MapIcon className="w-3.5 h-3.5" />
            <span>Map View</span>
          </button>
        </div>
      </div>

      {/* Verification Guard Banner */}
      {isUnverifiedReceiver && (
        <div className="p-4 bg-amber-50 border border-amber-200 rounded-2xl flex flex-col sm:flex-row sm:items-center justify-between gap-3 text-xs text-amber-900">
          <div className="flex items-start space-x-2.5">
            <ShieldAlert className="w-5 h-5 text-amber-600 shrink-0 mt-0.5" />
            <div>
              <strong className="block">Organization Verification Required Before Claiming</strong>
              <p className="text-amber-800 mt-0.5">
                Your NGO account status is currently <strong>{user?.verificationStatus}</strong>. Upload verification documents in your profile for Admin approval before reserving surplus food.
              </p>
            </div>
          </div>
          <Link
            to="/profile"
            className="px-4 py-2 bg-amber-600 text-white font-bold rounded-xl hover:bg-amber-700 transition whitespace-nowrap text-center"
          >
            Go to Profile
          </Link>
        </div>
      )}

      {/* Filter Bar */}
      <div className="bg-white p-5 rounded-3xl border border-slate-200 shadow-sm space-y-4">
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 text-xs">
          {/* Keyword Search */}
          <div className="relative">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
            <input
              type="text"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              placeholder="Search food by name, donor, or area..."
              className="w-full pl-9 pr-3 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-emerald-500"
            />
          </div>

          {/* Category Dropdown */}
          <div>
            <select
              value={category}
              onChange={(e) => setCategory(e.target.value)}
              className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-emerald-500 font-medium"
            >
              {CATEGORIES.map((cat) => (
                <option key={cat} value={cat}>
                  Category: {cat}
                </option>
              ))}
            </select>
          </div>

          {/* Distance Slider */}
          <div className="flex items-center space-x-3 bg-slate-50 px-3 py-2 rounded-xl border border-slate-200">
            <span className="text-slate-500 font-medium whitespace-nowrap text-[11px]">Radius: {maxDistance} km</span>
            <input
              type="range"
              min="2"
              max="50"
              step="2"
              value={maxDistance}
              onChange={(e) => setMaxDistance(parseInt(e.target.value))}
              className="w-full accent-emerald-600 cursor-pointer"
            />
          </div>
        </div>
      </div>

      {/* Main Content: Map or List */}
      {loading ? (
        <div className="min-h-[50vh] flex items-center justify-center">
          <Loader2 className="w-8 h-8 animate-spin text-emerald-600" />
        </div>
      ) : viewMode === 'map' ? (
        <div className="space-y-4">
          <div className="flex items-center justify-between text-xs text-slate-500">
            <span>Showing {filtered.length} surplus locations on OpenStreetMap</span>
            <span>Click any marker to inspect food batches and claim</span>
          </div>
          <LeafletMap
            donations={filtered}
            userLat={user?.latitude || 12.9716}
            userLon={user?.longitude || 77.5946}
            radiusKm={maxDistance}
            onClaim={handleOpenClaim}
            height="550px"
          />
        </div>
      ) : filtered.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filtered.map((d) => (
            <div
              key={d.id}
              className="bg-white rounded-3xl border border-slate-200 overflow-hidden shadow-sm hover:shadow-md transition flex flex-col justify-between"
            >
              <div>
                {d.foodImage && (
                  <img src={d.foodImage} alt={d.foodName} className="h-44 w-full object-cover" />
                )}
                <div className="p-6 space-y-3">
                  <div className="flex items-center justify-between">
                    <StatusBadge status={d.status} />
                    <SafetyBadge indicator={d.safetyIndicator} />
                  </div>

                  <div>
                    <div className="flex items-center justify-between gap-2">
                      <h3 className="font-bold text-slate-900 text-base">{d.foodName}</h3>
                    </div>
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
                    <span className="text-[11px] font-semibold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded-md mt-1.5 inline-block">
                      {d.foodCategory}
                    </span>
                  </div>

                  <p className="text-xs text-slate-600 line-clamp-2 leading-relaxed">
                    {d.foodDescription || 'Fresh surplus food available for redistribution.'}
                  </p>

                  {/* Partial Food Distribution Progress Bar */}
                  {(() => {
                    const remaining = d.remainingQuantity ?? d.quantity;
                    const allocated = d.allocatedQuantity ?? Math.max(0, d.quantity - remaining);
                    const pct = d.quantity > 0 ? Math.min(100, Math.round((allocated / d.quantity) * 100)) : 0;
                    return (
                      <div className="p-2.5 bg-slate-50 rounded-xl border border-slate-100 space-y-1.5 text-[11px]">
                        <div className="flex items-center justify-between">
                          <span className="text-slate-500 font-medium">Remaining Available:</span>
                          <strong className="text-emerald-700">
                            {remaining.toFixed(1)} / {d.quantity} {d.measurementUnit}
                          </strong>
                        </div>
                        <div className="w-full h-1.5 bg-slate-200 rounded-full overflow-hidden">
                          <div
                            className="h-full bg-emerald-500 rounded-full transition-all"
                            style={{ width: `${Math.max(0, 100 - pct)}%` }}
                          />
                        </div>
                        <div className="flex items-center justify-between text-[10px] text-slate-400">
                          <span>{allocated.toFixed(1)} {d.measurementUnit} claimed ({pct}%)</span>
                          <span>{d.allocationCount || 0} NGO split(s)</span>
                        </div>
                      </div>
                    );
                  })()}

                  <div className="space-y-1.5 text-xs text-slate-600 pt-2 border-t border-slate-100">
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
              </div>

              <div className="p-4 bg-slate-50 border-t border-slate-100">
                <button
                  onClick={() => handleOpenClaim(d)}
                  disabled={isUnverifiedReceiver || (d.remainingQuantity ?? d.quantity) <= 0}
                  className="w-full py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-bold transition shadow-sm disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {isUnverifiedReceiver ? 'Verification Pending' : 'Claim Full or Partial Portion'}
                </button>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="text-center py-20 bg-white rounded-3xl border border-slate-200 p-8 space-y-3">
          <Package className="w-12 h-12 text-slate-300 mx-auto" />
          <h4 className="text-sm font-semibold text-slate-700">No surplus food listings match your criteria</h4>
          <p className="text-xs text-slate-400">Try broadening the distance slider or selecting 'ALL' categories.</p>
        </div>
      )}

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
                    Remaining available: {(selectedDonation.remainingQuantity ?? selectedDonation.quantity).toFixed(1)} of {selectedDonation.quantity} {selectedDonation.measurementUnit}
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
                  <p className="text-indigo-700 mt-0.5">Dispatch this allocation to nearby verified AnnaDhata volunteers once approved by the donor.</p>
                </div>
              </label>

              <div className="p-3 bg-emerald-50 text-emerald-900 rounded-xl border border-emerald-100 text-[11px] space-y-1">
                <strong>Atomic Partial Allocation & QR Passport:</strong>
                <p>Donor approval atomically reserves your portion and generates an HMAC-signed QR Food Passport for pickup verification.</p>
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
