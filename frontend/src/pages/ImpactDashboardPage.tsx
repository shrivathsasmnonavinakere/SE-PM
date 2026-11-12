import React, { useState, useEffect } from 'react';
import { impactApi } from '../services/api';
import { ImpactSummary } from '../types';
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  PieChart,
  Pie,
  Cell,
  Legend,
  LineChart,
  Line,
} from 'recharts';
import {
  Leaf,
  HeartHandshake,
  TrendingUp,
  AlertCircle,
  FileCheck2,
  Calendar,
  Building,
  Utensils,
  Loader2,
  Info,
} from 'lucide-react';

const COLORS = ['#10b981', '#14b8a6', '#06b6d4', '#3b82f6', '#8b5cf6', '#ec4899', '#f59e0b', '#64748b'];

export const ImpactDashboardPage: React.FC = () => {
  const [impact, setImpact] = useState<ImpactSummary | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadImpact();
  }, []);

  const loadImpact = async () => {
    try {
      const res = await impactApi.getPublicSummary();
      if (res.success && res.data) {
        setImpact(res.data);
      }
    } catch (err) {
      console.error('Failed to load impact metrics:', err);
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

  // Format category data for Pie Chart
  const categoryData = impact?.categoryRedistributedKg
    ? Object.entries(impact.categoryRedistributedKg).map(([name, value]) => ({
        name,
        value: Math.round(value * 10.0) / 10.0,
      }))
    : [];

  const trendData = impact?.monthlyTrends || [];

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-8">
      {/* Page Title & Mission */}
      <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-4 border-b border-slate-200 pb-6">
        <div>
          <div className="flex items-center space-x-2">
            <span className="p-2 bg-emerald-100 text-emerald-700 rounded-xl">
              <Leaf className="w-5 h-5" />
            </span>
            <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
              Sustainability & Hunger Impact Dashboard
            </h1>
          </div>
          <p className="text-xs sm:text-sm text-slate-500 mt-1">
            Real-time empirical tracking of diverted surplus food, verified meals saved, and estimated greenhouse gas mitigation.
          </p>
        </div>

        <div className="flex items-center space-x-2 text-xs bg-emerald-50 text-emerald-800 px-3 py-1.5 rounded-xl border border-emerald-200 font-semibold">
          <FileCheck2 className="w-4 h-4 text-emerald-600" />
          <span>MongoDB Persisted Records</span>
        </div>
      </div>

      {/* Primary KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        <div className="p-6 bg-white rounded-3xl border border-slate-200 shadow-sm space-y-2">
          <div className="flex items-center justify-between text-slate-400">
            <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">Meals Delivered</span>
            <HeartHandshake className="w-5 h-5 text-emerald-600" />
          </div>
          <div className="text-3xl font-black text-slate-900">
            {impact ? Math.round(impact.estimatedMealsSaved).toLocaleString() : '0'}
          </div>
          <p className="text-[11px] text-slate-500">
            Calculated at <span className="font-semibold text-slate-700">0.4 kg / meal</span> standard
          </p>
        </div>

        <div className="p-6 bg-white rounded-3xl border border-slate-200 shadow-sm space-y-2">
          <div className="flex items-center justify-between text-slate-400">
            <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">Food Redistributed</span>
            <TrendingUp className="w-5 h-5 text-teal-600" />
          </div>
          <div className="text-3xl font-black text-slate-900">
            {impact ? impact.totalFoodRedistributedKg.toFixed(1) : '0.0'} <span className="text-lg font-bold text-slate-500">kg</span>
          </div>
          <p className="text-[11px] text-slate-500">
            Total completed redistribution volume
          </p>
        </div>

        <div className="p-6 bg-white rounded-3xl border border-slate-200 shadow-sm space-y-2">
          <div className="flex items-center justify-between text-slate-400">
            <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">Est. CO2 Avoided</span>
            <Leaf className="w-5 h-5 text-emerald-600" />
          </div>
          <div className="text-3xl font-black text-slate-900">
            {impact ? impact.estimatedCo2AvoidedKg.toFixed(1) : '0.0'} <span className="text-lg font-bold text-slate-500">kg CO2e</span>
          </div>
          <p className="text-[11px] text-slate-500">
            UNEP/FAO modeled estimate (2.5 kg/kg)
          </p>
        </div>

        <div className="p-6 bg-white rounded-3xl border border-slate-200 shadow-sm space-y-2">
          <div className="flex items-center justify-between text-slate-400">
            <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">Success Rate</span>
            <TrendingUp className="w-5 h-5 text-blue-600" />
          </div>
          <div className="text-3xl font-black text-slate-900">
            {impact ? impact.successRatePercent.toFixed(1) : '0.0'}%
          </div>
          <p className="text-[11px] text-slate-500">
            Completed claims vs expired listings
          </p>
        </div>
      </div>

      {/* Network Participation Bar */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
        <div className="p-5 bg-slate-50 rounded-2xl border border-slate-200 flex items-center space-x-4">
          <div className="p-3 bg-white text-emerald-600 rounded-xl shadow-sm border border-slate-100">
            <Utensils className="w-6 h-6" />
          </div>
          <div>
            <div className="text-xl font-bold text-slate-900">{impact?.activeDonorsCount || 0}</div>
            <p className="text-xs text-slate-500">Active Commercial Food Donors</p>
          </div>
        </div>

        <div className="p-5 bg-slate-50 rounded-2xl border border-slate-200 flex items-center space-x-4">
          <div className="p-3 bg-white text-teal-600 rounded-xl shadow-sm border border-slate-100">
            <Building className="w-6 h-6" />
          </div>
          <div>
            <div className="text-xl font-bold text-slate-900">{impact?.activeReceiversCount || 0}</div>
            <p className="text-xs text-slate-500">Verified Receiver Charities</p>
          </div>
        </div>

        <div className="p-5 bg-slate-50 rounded-2xl border border-slate-200 flex items-center space-x-4">
          <div className="p-3 bg-white text-blue-600 rounded-xl shadow-sm border border-slate-100">
            <Calendar className="w-6 h-6" />
          </div>
          <div>
            <div className="text-xl font-bold text-slate-900">{impact?.totalCompletedDonations || 0}</div>
            <p className="text-xs text-slate-500">Successfully Completed Batches</p>
          </div>
        </div>
      </div>

      {/* Recharts Analytics Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Monthly Trend Chart */}
        <div className="p-6 bg-white rounded-3xl border border-slate-200 shadow-sm space-y-4">
          <div>
            <h3 className="font-bold text-slate-900 text-sm">Monthly Redistribution Trends (kg Food)</h3>
            <p className="text-xs text-slate-400">Total volume rescued and distributed across past months</p>
          </div>

          <div className="h-64 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={trendData}>
                <XAxis dataKey="month" stroke="#94a3b8" fontSize={11} />
                <YAxis stroke="#94a3b8" fontSize={11} />
                <Tooltip
                  contentStyle={{ backgroundColor: '#ffffff', borderRadius: '12px', borderColor: '#e2e8f0', fontSize: '12px' }}
                />
                <Bar dataKey="foodKg" name="Food (kg)" fill="#10b981" radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Category Breakdown Pie Chart */}
        <div className="p-6 bg-white rounded-3xl border border-slate-200 shadow-sm space-y-4">
          <div>
            <h3 className="font-bold text-slate-900 text-sm">Redistribution by Food Category (kg)</h3>
            <p className="text-xs text-slate-400">Distribution proportion of completed surplus items</p>
          </div>

          <div className="h-64 w-full">
            {categoryData.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={categoryData}
                    cx="50%"
                    cy="50%"
                    innerRadius={55}
                    outerRadius={80}
                    paddingAngle={4}
                    dataKey="value"
                  >
                    {categoryData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip
                    contentStyle={{ backgroundColor: '#ffffff', borderRadius: '12px', borderColor: '#e2e8f0', fontSize: '12px' }}
                  />
                  <Legend wrapperStyle={{ fontSize: '11px', paddingTop: '8px' }} />
                </PieChart>
              </ResponsiveContainer>
            ) : (
              <div className="h-full flex items-center justify-center text-xs text-slate-400">
                No categorized completions yet
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Methodology & Legal Notice */}
      <div className="p-6 bg-slate-50 rounded-2xl border border-slate-200 text-xs text-slate-600 space-y-2">
        <div className="flex items-center space-x-2 text-slate-800 font-bold">
          <Info className="w-4 h-4 text-emerald-600" />
          <span>Impact Calculation Methodology & Disclaimer</span>
        </div>
        <p className="leading-relaxed">
          <strong>Meals Saved Factor:</strong> {impact?.mealEstimationFactor}. Modeled on standard nutritional portion sizing for adult meals in social welfare institutions.
        </p>
        <p className="leading-relaxed">
          <strong>Greenhouse Gas Estimation:</strong> {impact?.co2EstimationFactor}. Diverting organic surplus from anaerobic landfill digestion prevents localized methane releases.
        </p>
        <p className="text-slate-400 italic">
          {impact?.disclaimer}
        </p>
      </div>
    </div>
  );
};
