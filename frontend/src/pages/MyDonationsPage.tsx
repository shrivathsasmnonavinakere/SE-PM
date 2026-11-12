import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { donationApi } from '../services/api';
import { Donation, DonationStatus } from '../types';
import { StatusBadge } from '../components/StatusBadge';
import { SafetyBadge } from '../components/SafetyBadge';
import {
  Package,
  PlusCircle,
  Clock,
  Sparkles,
  ArrowRight,
  Filter,
  Loader2,
  Trash2,
} from 'lucide-react';

export const MyDonationsPage: React.FC = () => {
  const [donations, setDonations] = useState<Donation[]>([]);
  const [filter, setFilter] = useState<string>('ALL');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadMyDonations();
  }, []);

  const loadMyDonations = async () => {
    try {
      const res = await donationApi.getMy();
      if (res.success && res.data) setDonations(res.data);
    } catch (err) {
      console.error('Failed to load my donations:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = async (id: string) => {
    if (!window.confirm('Are you sure you want to cancel this food donation?')) return;
    try {
      await donationApi.cancel(id);
      loadMyDonations();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to cancel donation');
    }
  };

  const filtered = donations.filter((d) => {
    if (filter === 'ALL') return true;
    return d.status === filter;
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
      {/* Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 border-b border-slate-200 pb-6">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">My Food Listings</h1>
          <p className="text-xs sm:text-sm text-slate-500 mt-1">Review active, claimed, and completed food surplus batches.</p>
        </div>

        <Link
          to="/donor/create-donation"
          className="inline-flex items-center space-x-2 px-4 py-2.5 bg-gradient-to-r from-emerald-600 to-teal-600 text-white font-bold text-xs rounded-xl shadow-md shadow-emerald-500/20 hover:from-emerald-700 hover:to-teal-700 transition"
        >
          <PlusCircle className="w-4 h-4" />
          <span>New Surplus Listing</span>
        </Link>
      </div>

      {/* Filter Tabs */}
      <div className="flex items-center space-x-2 overflow-x-auto pb-2 text-xs">
        {['ALL', 'AVAILABLE', 'PARTIALLY_CLAIMED', 'MATCHED', 'CLAIMED', 'COMPLETED', 'EXPIRED', 'CANCELLED'].map((st) => (
          <button
            key={st}
            onClick={() => setFilter(st)}
            className={`px-3 py-1.5 rounded-xl font-bold transition whitespace-nowrap ${filter === st
                ? 'bg-emerald-600 text-white shadow-sm'
                : 'bg-white text-slate-600 border border-slate-200 hover:bg-slate-50'
              }`}
          >
            {st.replace(/_/g, ' ')}
          </button>
        ))}
      </div>

      {/* Listings Grid */}
      {filtered.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filtered.map((d) => (
            <div
              key={d.id}
              className="bg-white rounded-3xl border border-slate-200 shadow-sm hover:shadow-md transition overflow-hidden flex flex-col justify-between"
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
                    <h3 className="font-bold text-slate-900 text-base">{d.foodName}</h3>
                    <span className="text-[11px] font-semibold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded-md mt-1 inline-block">
                      {d.foodCategory}
                    </span>
                  </div>

                  <p className="text-xs text-slate-600 line-clamp-2 leading-relaxed">
                    {d.foodDescription || 'No description provided.'}
                  </p>

                  <div className="pt-2 border-t border-slate-100 space-y-2 text-xs text-slate-600">
                    {(() => {
                      const remaining = d.remainingQuantity ?? d.quantity;
                      const allocated = d.allocatedQuantity ?? Math.max(0, d.quantity - remaining);
                      const pct = d.quantity > 0 ? Math.min(100, Math.round((allocated / d.quantity) * 100)) : 0;
                      return (
                        <div className="space-y-1">
                          <div className="flex items-center justify-between">
                            <span className="text-slate-400">Allocation:</span>
                            <strong className="text-slate-900">
                              {remaining.toFixed(1)} left of {d.quantity} {d.measurementUnit}
                            </strong>
                          </div>
                          <div className="w-full h-1.5 bg-slate-200 rounded-full overflow-hidden">
                            <div
                              className="h-full bg-emerald-500 rounded-full transition-all"
                              style={{ width: `${pct}%` }}
                            />
                          </div>
                          <div className="flex items-center justify-between text-[10px] text-slate-400">
                            <span>{allocated.toFixed(1)} {d.measurementUnit} claimed ({pct}%)</span>
                            <span>{d.allocationCount || 0} claim(s)</span>
                          </div>
                        </div>
                      );
                    })()}

                    <div className="flex items-center justify-between">
                      <span className="text-slate-400">Pickup Deadline:</span>
                      <strong className="text-amber-700 flex items-center">
                        <Clock className="w-3 h-3 mr-1" />
                        {new Date(d.pickupDeadline).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </strong>
                    </div>

                    {d.claimedByReceiverName && (
                      <div className="flex items-center justify-between pt-1 text-emerald-700">
                        <span>Claimed by:</span>
                        <strong className="truncate max-w-[150px]">{d.claimedByReceiverName}</strong>
                      </div>
                    )}
                  </div>
                </div>
              </div>

              {/* Actions Footer */}
              <div className="p-4 bg-slate-50 border-t border-slate-100 flex items-center justify-between gap-2">
                <Link
                  to={`/donor/donations/${d.id}/matches`}
                  className="flex-1 py-1.5 bg-emerald-50 text-emerald-700 hover:bg-emerald-100 rounded-xl text-xs font-bold transition flex items-center justify-center space-x-1"
                >
                  <Sparkles className="w-3.5 h-3.5 text-emerald-600" />
                  <span>Matches</span>
                </Link>

                <Link
                  to={`/donor/donations/${d.id}`}
                  className="px-3 py-1.5 bg-white text-slate-700 hover:bg-slate-100 border border-slate-200 rounded-xl text-xs font-semibold transition"
                >
                  Details
                </Link>

                {d.status === 'AVAILABLE' && (
                  <button
                    onClick={() => handleCancel(d.id)}
                    title="Cancel Listing"
                    className="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-xl transition"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="text-center py-20 bg-white rounded-3xl border border-slate-200 p-8 space-y-3">
          <Package className="w-12 h-12 text-slate-300 mx-auto" />
          <h4 className="text-sm font-semibold text-slate-700">No food listings found for '{filter}'</h4>
          <p className="text-xs text-slate-400">Adjust the filter tab or create a new food donation.</p>
        </div>
      )}
    </div>
  );
};
