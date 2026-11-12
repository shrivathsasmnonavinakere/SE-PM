import React, { useState } from 'react';
import { trustApi } from '../services/api';
import { TrustScore } from '../types';
import { ShieldCheck, Star, Loader2, X, Info } from 'lucide-react';

interface TrustBadgeProps {
  score?: number;
  tier?: string;
  userId?: string;
  label?: string;
  compact?: boolean;
}

export const TrustBadge: React.FC<TrustBadgeProps> = ({
  score = 80,
  tier = 'GOLD',
  userId,
  label,
  compact = false,
}) => {
  const [modalOpen, setModalOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [breakdown, setBreakdown] = useState<TrustScore | null>(null);

  const getTierStyle = (t?: string) => {
    switch (t) {
      case 'PLATINUM':
        return 'bg-emerald-50 text-emerald-800 border-emerald-200';
      case 'GOLD':
        return 'bg-amber-50 text-amber-800 border-amber-200';
      case 'SILVER':
        return 'bg-slate-100 text-slate-700 border-slate-300';
      case 'NEEDS_IMPROVEMENT':
        return 'bg-rose-50 text-rose-700 border-rose-200';
      default:
        return 'bg-teal-50 text-teal-800 border-teal-200';
    }
  };

  const handleOpenBreakdown = async (e: React.MouseEvent) => {
    e.stopPropagation();
    if (!userId) return;
    setModalOpen(true);
    setLoading(true);
    try {
      const res = await trustApi.getByUser(userId);
      if (res.success && res.data) {
        setBreakdown(res.data);
      }
    } catch {
      // Ignore quietly if unavailable
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <button
        type="button"
        onClick={handleOpenBreakdown}
        disabled={!userId}
        title={userId ? 'Click to inspect transparent Trust & Reliability Score breakdown' : undefined}
        className={`inline-flex items-center space-x-1 px-2 py-0.5 rounded-full text-[11px] font-bold border transition ${getTierStyle(
          tier
        )} ${userId ? 'hover:shadow-sm cursor-pointer' : 'cursor-default'}`}
      >
        <ShieldCheck className="w-3 h-3 flex-shrink-0" />
        <span>
          {label ? `${label}: ` : ''}
          {Math.round(score)}
          {!compact && `/100 • ${tier?.replace('_', ' ')}`}
        </span>
      </button>

      {modalOpen && (
        <div
          className="fixed inset-0 z-50 bg-slate-900/50 backdrop-blur-sm flex items-center justify-center p-4"
          onClick={() => setModalOpen(false)}
        >
          <div
            className="bg-white max-w-lg w-full rounded-3xl p-6 sm:p-7 space-y-5 shadow-2xl"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="flex items-start justify-between border-b border-slate-100 pb-3">
              <div>
                <span className="text-[10px] font-bold text-emerald-600 uppercase tracking-wider">
                  Transparent Reliability Audit
                </span>
                <h3 className="text-lg font-extrabold text-slate-900">
                  {breakdown?.userName || 'Organization Trust Profile'}
                </h3>
                {breakdown?.role && (
                  <p className="text-xs text-slate-500">{breakdown.role.replace('ROLE_', '')}</p>
                )}
              </div>
              <button
                type="button"
                onClick={() => setModalOpen(false)}
                className="p-1 text-slate-400 hover:text-slate-700 rounded-lg"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {loading ? (
              <div className="py-12 flex items-center justify-center">
                <Loader2 className="w-6 h-6 animate-spin text-emerald-600" />
              </div>
            ) : breakdown ? (
              <div className="space-y-4 text-xs">
                <div className="flex items-center justify-between p-4 bg-slate-50 rounded-2xl border border-slate-200/80">
                  <div>
                    <span className="text-slate-500 font-medium block">Overall Reliability Score</span>
                    <strong className="text-3xl font-black text-emerald-600">
                      {breakdown.overallScore.toFixed(1)}
                    </strong>
                    <span className="text-slate-400 text-xs font-bold"> / 100</span>
                  </div>
                  <div className="text-right">
                    <span className={`px-3 py-1 rounded-full text-xs font-extrabold border ${getTierStyle(breakdown.tier)}`}>
                      <Star className="w-3.5 h-3.5 inline mr-1" />
                      {breakdown.tier.replace('_', ' ')}
                    </span>
                    <p className="text-[11px] text-slate-400 mt-1.5">
                      {breakdown.completedCount} completed &bull; {breakdown.totalTransactions} total events
                    </p>
                  </div>
                </div>

                <div className="space-y-2.5">
                  <span className="font-bold text-slate-700 block">Scoring Methodology Breakdown</span>
                  {breakdown.factors.map((f, idx) => (
                    <div key={idx} className="p-3 bg-slate-50/70 rounded-2xl border border-slate-100 space-y-1.5">
                      <div className="flex items-center justify-between">
                        <span className="font-bold text-slate-800">
                          {f.factorName} <span className="text-slate-400 font-medium">({f.weightPercent}%)</span>
                        </span>
                        <strong className="text-emerald-700">{f.score.toFixed(1)}/100</strong>
                      </div>
                      <div className="w-full h-1.5 bg-slate-200 rounded-full overflow-hidden">
                        <div
                          className="h-full bg-emerald-500 rounded-full"
                          style={{ width: `${Math.min(100, Math.max(0, f.score))}%` }}
                        />
                      </div>
                      <p className="text-[11px] text-slate-500">{f.detail}</p>
                    </div>
                  ))}
                </div>

                <div className="p-3 bg-emerald-50/70 text-emerald-900 rounded-xl border border-emerald-100 text-[11px] flex items-start space-x-2">
                  <Info className="w-4 h-4 text-emerald-600 flex-shrink-0 mt-0.5" />
                  <span>{breakdown.explanation}</span>
                </div>
              </div>
            ) : (
              <p className="text-xs text-slate-500 py-8 text-center">Trust metrics could not be loaded.</p>
            )}
          </div>
        </div>
      )}
    </>
  );
};

