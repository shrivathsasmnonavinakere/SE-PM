import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { donationApi } from '../services/api';
import { MatchRecommendation, Donation } from '../types';
import { StatusBadge } from '../components/StatusBadge';
import { SafetyBadge } from '../components/SafetyBadge';
import { TrustBadge } from '../components/TrustBadge';
import {
  Sparkles,
  MapPin,
  Building2,
  Phone,
  Clock,
  Package,
  Award,
  CheckCircle,
  ArrowLeft,
  Loader2,
  TrendingUp,
} from 'lucide-react';

export const MatchingRecommendationsPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [donation, setDonation] = useState<Donation | null>(null);
  const [matches, setMatches] = useState<MatchRecommendation[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (id) loadData(id);
  }, [id]);

  const loadData = async (donationId: string) => {
    try {
      const [donRes, matchRes] = await Promise.all([
        donationApi.getById(donationId),
        donationApi.getMatches(donationId),
      ]);
      if (donRes.success && donRes.data) setDonation(donRes.data);
      if (matchRes.success && matchRes.data) setMatches(matchRes.data);
    } catch (err) {
      console.error('Failed to load matching recommendations:', err);
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

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-8">
      {/* Back button & Title */}
      <div className="space-y-4">
        <Link
          to="/donor/dashboard"
          className="inline-flex items-center space-x-1.5 text-xs font-semibold text-slate-500 hover:text-emerald-700 transition"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Back to Dashboard</span>
        </Link>

        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 border-b border-slate-200 pb-5">
          <div>
            <div className="flex items-center space-x-2">
              <span className="p-2 bg-emerald-100 text-emerald-700 rounded-xl">
                <Sparkles className="w-5 h-5" />
              </span>
              <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
                AI Smart Matching Engine
              </h1>
            </div>
            <p className="text-xs sm:text-sm text-slate-500 mt-1">
              Top 5 verified charities mathematically ranked by our 4-factor deterministic scoring model with Gemini AI explanations.
            </p>
          </div>

          {donation && (
            <div className="flex items-center space-x-2">
              <StatusBadge status={donation.status} />
              <SafetyBadge indicator={donation.safetyIndicator} />
            </div>
          )}
        </div>
      </div>

      {/* Donation Summary Header */}
      {donation && (
        <div className="p-5 bg-white rounded-3xl border border-slate-200 shadow-sm flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
          <div>
            <span className="text-[11px] font-bold text-emerald-600 uppercase tracking-wider">Matching For Listing</span>
            <h3 className="text-lg font-bold text-slate-900">{donation.foodName}</h3>
            <p className="text-xs text-slate-500">
              {donation.quantity} {donation.measurementUnit} &bull; {donation.foodCategory} &bull; {donation.pickupAddress}
            </p>
          </div>

          <div className="text-left sm:text-right text-xs text-slate-500">
            <span className="flex items-center sm:justify-end text-amber-600 font-semibold">
              <Clock className="w-3.5 h-3.5 mr-1" />
              Pickup Deadline: {new Date(donation.pickupDeadline).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
            </span>
            <p className="text-[11px] text-slate-400 mt-0.5">Automated push alerts sent to top receivers</p>
          </div>
        </div>
      )}

      {/* Weighting Algorithm Legend */}
      <div className="p-4 bg-slate-50 rounded-2xl border border-slate-200/80 flex flex-wrap items-center justify-between gap-3 text-xs text-slate-600">
        <span className="font-bold text-slate-800">Scoring Model Weights:</span>
        <span className="bg-white px-2.5 py-1 rounded-lg border border-slate-200 font-semibold">Distance: 30%</span>
        <span className="bg-white px-2.5 py-1 rounded-lg border border-slate-200 font-semibold">Capacity & Qty: 25%</span>
        <span className="bg-white px-2.5 py-1 rounded-lg border border-slate-200 font-semibold">Urgency: 25%</span>
        <span className="bg-white px-2.5 py-1 rounded-lg border border-slate-200 font-semibold">Dietary Compatibility: 20%</span>
      </div>

      {/* Ranked Receivers List */}
      <div className="space-y-4">
        {matches.length > 0 ? (
          matches.map((m, index) => (
            <div
              key={m.receiverId}
              className={`p-6 rounded-3xl border transition shadow-sm bg-white hover:shadow-md ${
                index === 0 ? 'border-emerald-300 ring-2 ring-emerald-500/10' : 'border-slate-200'
              }`}
            >
              <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pb-4 border-b border-slate-100">
                <div className="flex items-center space-x-3">
                  <div
                    className={`w-9 h-9 rounded-2xl flex items-center justify-center font-black text-sm ${
                      index === 0
                        ? 'bg-emerald-600 text-white'
                        : index === 1
                        ? 'bg-teal-600 text-white'
                        : 'bg-slate-100 text-slate-600'
                    }`}
                  >
                    #{index + 1}
                  </div>
                  <div>
                    <div className="flex flex-wrap items-center gap-2">
                      <h3 className="font-bold text-slate-900 text-base flex items-center">
                        {m.receiverName}
                        {index === 0 && (
                          <span className="ml-2 text-[10px] font-bold bg-emerald-100 text-emerald-800 px-2 py-0.5 rounded-full uppercase">
                            Highest Match
                          </span>
                        )}
                      </h3>
                      {m.receiverTrustScore !== undefined && (
                        <TrustBadge
                          score={m.receiverTrustScore}
                          tier={m.receiverTrustTier}
                          userId={m.receiverId}
                          label={m.receiverName}
                          compact
                        />
                      )}
                      {m.trustBonusPoints !== undefined && m.trustBonusPoints > 0 && (
                        <span className="text-[10px] font-bold text-indigo-700 bg-indigo-50 px-2 py-0.5 rounded-full border border-indigo-200">
                          +{m.trustBonusPoints.toFixed(1)} Trust Bonus
                        </span>
                      )}
                    </div>
                    <p className="text-xs text-slate-400 flex items-center mt-0.5">
                      <MapPin className="w-3.5 h-3.5 mr-1 text-slate-400" />
                      {m.receiverAddress} &bull; <strong className="text-slate-700 ml-1">{m.distanceKm.toFixed(1)} km away</strong>
                    </p>
                  </div>
                </div>

                <div className="text-right">
                  <div className="text-2xl font-black text-emerald-600">
                    {m.matchScore.toFixed(0)}<span className="text-xs text-slate-400">/100</span>
                  </div>
                  <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">Overall Match Score</span>
                </div>
              </div>

              {/* Factor Breakdown Bars */}
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 py-3 text-[11px] text-slate-600">
                <div className="p-2 bg-slate-50 rounded-xl">
                  <span className="text-slate-400 block text-[10px]">Distance Score</span>
                  <span className="font-bold text-slate-800">{m.distanceScore.toFixed(0)}%</span>
                </div>
                <div className="p-2 bg-slate-50 rounded-xl">
                  <span className="text-slate-400 block text-[10px]">Capacity Fit</span>
                  <span className="font-bold text-slate-800">{m.quantityScore.toFixed(0)}%</span>
                </div>
                <div className="p-2 bg-slate-50 rounded-xl">
                  <span className="text-slate-400 block text-[10px]">Urgency Window</span>
                  <span className="font-bold text-slate-800">{m.urgencyScore.toFixed(0)}%</span>
                </div>
                <div className="p-2 bg-slate-50 rounded-xl">
                  <span className="text-slate-400 block text-[10px]">Food Compatibility</span>
                  <span className="font-bold text-slate-800">{m.foodCompatibilityScore.toFixed(0)}%</span>
                </div>
              </div>

              {/* Gemini AI Rationale Quote */}
              <div className="mt-2 p-3.5 bg-emerald-50/70 text-emerald-900 rounded-2xl text-xs leading-relaxed border border-emerald-100 flex items-start space-x-2">
                <Sparkles className="w-4 h-4 text-emerald-600 flex-shrink-0 mt-0.5" />
                <div>
                  <span className="font-bold block text-[11px] text-emerald-800 uppercase tracking-wider mb-0.5">
                    Gemini AI Rationale
                  </span>
                  <p className="italic">{m.recommendationExplanation}</p>
                </div>
              </div>
            </div>
          ))
        ) : (
          <div className="text-center py-16 bg-white rounded-3xl border border-slate-200 p-8 space-y-3">
            <Building2 className="w-10 h-10 text-slate-300 mx-auto" />
            <h4 className="text-sm font-semibold text-slate-800">No active receiver requirements found</h4>
            <p className="text-xs text-slate-500 max-w-sm mx-auto">
              Charities in your radius will be matched as soon as they declare food requirements or browse your listing on the map.
            </p>
          </div>
        )}
      </div>
    </div>
  );
};
