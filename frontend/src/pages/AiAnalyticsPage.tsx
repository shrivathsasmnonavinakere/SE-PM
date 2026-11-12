import React, { useState, useEffect } from 'react';
import { aiApi } from '../services/api';
import { AiAnalyticsReport } from '../types';
import {
  Sparkles,
  TrendingUp,
  Clock,
  AlertTriangle,
  CheckCircle2,
  Package,
  Layers,
  ArrowRight,
  Loader2,
  FileText,
  Lightbulb,
} from 'lucide-react';

export const AiAnalyticsPage: React.FC = () => {
  const [report, setReport] = useState<AiAnalyticsReport | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadReport();
  }, []);

  const loadReport = async () => {
    try {
      const res = await aiApi.getAnalyticsReport();
      if (res.success && res.data) setReport(res.data);
    } catch (err) {
      console.error('Failed to load AI analytics report:', err);
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
      {/* Title */}
      <div className="border-b border-slate-200 pb-5">
        <div className="flex items-center space-x-2">
          <span className="p-2 bg-gradient-to-tr from-emerald-600 to-teal-500 text-white rounded-xl shadow-sm">
            <Sparkles className="w-5 h-5" />
          </span>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
            AI Ecosystem Analytics Report
          </h1>
        </div>
        <p className="text-xs sm:text-sm text-slate-500 mt-1">
          Algorithmic analysis of real MongoDB donation records, peak disposal hours, and operational recommendations.
        </p>
      </div>

      {report && (
        <>
          {/* Executive Summary Card */}
          <div className="p-8 bg-gradient-to-br from-emerald-950 via-slate-900 to-teal-950 text-white rounded-3xl shadow-xl space-y-4">
            <div className="flex items-center space-x-2 text-emerald-400 text-xs font-bold uppercase tracking-wider">
              <Sparkles className="w-4 h-4" />
              <span>Google Gemini Strategic Evaluation</span>
            </div>
            <h2 className="text-xl sm:text-2xl font-bold leading-relaxed">{report.executiveSummary}</h2>
            <div className="flex flex-wrap gap-4 pt-2 text-xs text-emerald-200">
              <span>Redistribution Success: <strong>{report.redistributionSuccessRate.toFixed(1)}%</strong></span>
              <span>&bull;</span>
              <span>Total Rescued: <strong>{report.totalRedistributedKg.toFixed(1)} kg</strong></span>
              <span>&bull;</span>
              <span>Completed Batches: <strong>{report.totalCompletedDonations}</strong></span>
            </div>
          </div>

          {/* Operational Metrics Grid */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-5">
            <div className="p-5 bg-white rounded-3xl border border-slate-200 shadow-sm space-y-1">
              <span className="text-xs font-bold text-slate-500 uppercase tracking-wider flex items-center">
                <Clock className="w-3.5 h-3.5 mr-1 text-slate-400" />
                Peak Surplus Hours
              </span>
              <div className="text-lg font-extrabold text-slate-900">{report.peakDonationHours}</div>
              <p className="text-[11px] text-slate-400">Post-dinner & banquet kitchen close</p>
            </div>

            <div className="p-5 bg-white rounded-3xl border border-slate-200 shadow-sm space-y-1">
              <span className="text-xs font-bold text-slate-500 uppercase tracking-wider flex items-center">
                <Package className="w-3.5 h-3.5 mr-1 text-emerald-500" />
                Highest Surplus Category
              </span>
              <div className="text-lg font-extrabold text-emerald-600">{report.highestSurplusCategory}</div>
              <p className="text-[11px] text-slate-400">Largest volume of donations</p>
            </div>

            <div className="p-5 bg-white rounded-3xl border border-slate-200 shadow-sm space-y-1">
              <span className="text-xs font-bold text-slate-500 uppercase tracking-wider flex items-center">
                <TrendingUp className="w-3.5 h-3.5 mr-1 text-teal-500" />
                Highest Deficit Category
              </span>
              <div className="text-lg font-extrabold text-teal-600">{report.highestDeficitCategory}</div>
              <p className="text-[11px] text-slate-400">Greatest unmet charity demand</p>
            </div>
          </div>

          {/* Strategic Insights */}
          <div className="p-6 sm:p-8 bg-white rounded-3xl border border-slate-200 shadow-sm space-y-4">
            <div className="flex items-center space-x-2 text-slate-800 font-bold text-sm">
              <Lightbulb className="w-5 h-5 text-amber-500" />
              <span>AI Strategic Insights</span>
            </div>
            <div className="space-y-3">
              {report.strategicInsights.map((insight, idx) => (
                <div key={idx} className="p-4 bg-slate-50 rounded-2xl border border-slate-100 text-xs text-slate-700 leading-relaxed flex items-start space-x-3">
                  <span className="w-5 h-5 rounded-full bg-emerald-100 text-emerald-800 flex items-center justify-center font-bold text-[10px] flex-shrink-0 mt-0.5">
                    {idx + 1}
                  </span>
                  <span>{insight}</span>
                </div>
              ))}
            </div>
          </div>

          {/* Actionable Recommendations */}
          <div className="p-6 sm:p-8 bg-white rounded-3xl border border-slate-200 shadow-sm space-y-4">
            <div className="flex items-center space-x-2 text-slate-800 font-bold text-sm">
              <CheckCircle2 className="w-5 h-5 text-emerald-600" />
              <span>Recommended Interventions for Platform Growth</span>
            </div>
            <div className="space-y-3">
              {report.actionableRecommendations.map((rec, idx) => (
                <div key={idx} className="p-4 bg-emerald-50/50 rounded-2xl border border-emerald-100 text-xs text-emerald-900 leading-relaxed flex items-start space-x-3">
                  <span className="w-5 h-5 rounded-full bg-emerald-600 text-white flex items-center justify-center font-bold text-[10px] flex-shrink-0 mt-0.5">
                    &rarr;
                  </span>
                  <span className="font-medium">{rec}</span>
                </div>
              ))}
            </div>
          </div>
        </>
      )}
    </div>
  );
};
