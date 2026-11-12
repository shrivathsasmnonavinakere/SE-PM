import React, { useState, useEffect } from 'react';
import { donationApi, adminApi } from '../services/api';
import { Donation, DonationStatus } from '../types';
import { StatusBadge } from '../components/StatusBadge';
import { SafetyBadge } from '../components/SafetyBadge';
import {
  Package,
  AlertTriangle,
  Clock,
  Shield,
  Loader2,
  Trash2,
  CheckCircle,
} from 'lucide-react';

export const DonationModerationPage: React.FC = () => {
  const [donations, setDonations] = useState<Donation[]>([]);
  const [filter, setFilter] = useState<string>('ALL');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadDonations();
  }, []);

  const loadDonations = async () => {
    try {
      const res = await adminApi.getAllDonations();
      if (res.success && res.data) setDonations(res.data);
    } catch (err) {
      console.error('Failed to load donations for moderation:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleModerate = async (donationId: string, newStatus: DonationStatus) => {
    const reason = prompt('Please specify moderation reason:', 'Administrative compliance review');
    if (!reason) return;

    try {
      await adminApi.moderateDonation(donationId, newStatus, reason);
      await loadDonations();
      alert('Listing status updated.');
    } catch (err: any) {
      alert('Moderation action failed.');
    }
  };

  const filtered = donations.filter((d) => {
    if (filter === 'ALL') return true;
    if (filter === 'SAFETY_FLAGGED') return d.safetyIndicator === 'HIGH_RISK' || d.safetyIndicator === 'REVIEW_REQUIRED';
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
      {/* Title */}
      <div className="border-b border-slate-200 pb-5">
        <div className="flex items-center space-x-2">
          <span className="p-2 bg-purple-100 text-purple-700 rounded-xl">
            <Shield className="w-5 h-5" />
          </span>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
            Food Donations & Safety Moderation
          </h1>
        </div>
        <p className="text-xs sm:text-sm text-slate-500 mt-1">
          Review live food listings, inspect safety indicators, and moderate policy-violating items.
        </p>
      </div>

      {/* Filter Tabs */}
      <div className="flex items-center space-x-2 overflow-x-auto pb-2 text-xs">
        {['ALL', 'SAFETY_FLAGGED', 'AVAILABLE', 'MATCHED', 'CLAIMED', 'COMPLETED', 'EXPIRED'].map((st) => (
          <button
            key={st}
            onClick={() => setFilter(st)}
            className={`px-3 py-1.5 rounded-xl font-bold transition whitespace-nowrap ${filter === st
                ? 'bg-purple-600 text-white shadow-sm'
                : 'bg-white text-slate-600 border border-slate-200 hover:bg-slate-50'
              }`}
          >
            {st.replace(/_/g, ' ')}
          </button>
        ))}
      </div>

      {/* Donations List */}
      <div className="bg-white rounded-3xl border border-slate-200 overflow-hidden shadow-sm">
        {filtered.length > 0 ? (
          <div className="divide-y divide-slate-100">
            {filtered.map((d) => (
              <div key={d.id} className="p-6 flex flex-col md:flex-row items-start md:items-center justify-between gap-4 text-xs">
                <div className="space-y-2 max-w-2xl">
                  <div className="flex items-center space-x-2">
                    <StatusBadge status={d.status} />
                    <SafetyBadge indicator={d.safetyIndicator} notes={d.safetyNotes} />
                    <span className="text-[11px] font-semibold text-slate-500 bg-slate-100 px-2 py-0.5 rounded-md">
                      {d.foodCategory}
                    </span>
                  </div>

                  <div>
                    <h4 className="font-bold text-slate-900 text-base">{d.foodName}</h4>
                    <p className="text-slate-500">{d.donorName} &bull; {d.pickupAddress}</p>
                  </div>

                  <p className="text-slate-600 leading-relaxed">{d.foodDescription || 'No description provided.'}</p>

                  <div className="flex items-center space-x-4 text-slate-400 text-[11px]">
                    <span>Batch: <strong className="text-slate-700">{d.quantity} {d.measurementUnit}</strong></span>
                    <span>Deadline: <strong className="text-amber-700">{new Date(d.pickupDeadline).toLocaleTimeString()}</strong></span>
                    {d.safetyNotes && <span className="italic text-emerald-800">Note: {d.safetyNotes}</span>}
                  </div>
                </div>

                <div className="flex items-center space-x-2">
                  <button
                    onClick={() => handleModerate(d.id, 'CANCELLED')}
                    className="px-3 py-1.5 bg-rose-50 text-rose-700 hover:bg-rose-100 rounded-xl font-bold transition flex items-center space-x-1"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                    <span>Cancel Listing</span>
                  </button>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="text-center py-16 text-slate-400 text-xs">
            No donations match this moderation filter.
          </div>
        )}
      </div>
    </div>
  );
};
