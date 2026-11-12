import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { impactApi, donationApi } from '../services/api';
import { ImpactSummary, Donation } from '../types';
import { StatusBadge } from '../components/StatusBadge';
import { SafetyBadge } from '../components/SafetyBadge';
import {
  HeartHandshake,
  Sparkles,
  MapPin,
  TrendingUp,
  ShieldCheck,
  Clock,
  ArrowRight,
  Package,
  CheckCircle2,
  Users,
  Building2,
  Utensils,
  Leaf,
  ChevronRight,
} from 'lucide-react';

export const LandingPage: React.FC = () => {
  const [impact, setImpact] = useState<ImpactSummary | null>(null);
  const [recentDonations, setRecentDonations] = useState<Donation[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadPublicData();
  }, []);

  const loadPublicData = async () => {
    try {
      const [impRes, donRes] = await Promise.all([
        impactApi.getPublicSummary(),
        donationApi.getPublicAvailable(),
      ]);
      if (impRes.success && impRes.data) setImpact(impRes.data);
      if (donRes.success && donRes.data) setRecentDonations(donRes.data.slice(0, 3));
    } catch (err) {
      console.error('Failed to load public landing data:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="bg-white">
      {/* Hero Section */}
      <section className="relative overflow-hidden pt-12 pb-20 lg:pt-20 lg:pb-28 bg-gradient-to-b from-brand-50/60 via-white to-white">
        <div className="absolute top-0 right-0 -mt-12 -mr-12 w-96 h-96 bg-brand-200/40 rounded-full blur-3xl pointer-events-none" />
        <div className="absolute top-48 left-0 -ml-12 w-80 h-80 bg-teal-200/30 rounded-full blur-3xl pointer-events-none" />

        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
          <div className="text-center max-w-3xl mx-auto space-y-6">
            <div className="inline-flex items-center space-x-2 px-3 py-1.5 rounded-full bg-emerald-100/80 border border-emerald-200 text-emerald-800 text-xs font-semibold">
              <Sparkles className="w-3.5 h-3.5 text-emerald-600 animate-spin" />
              <span>AI-Powered Food Waste Reduction & Redistribution</span>
            </div>

            <h1 className="text-4xl sm:text-5xl lg:text-6xl font-extrabold text-slate-900 tracking-tight leading-[1.15]">
              Turning Surplus Food Into{' '}
              <span className="text-transparent bg-clip-text bg-gradient-to-r from-emerald-600 to-teal-600">
                Meals, Not Waste.
              </span>
            </h1>

            <p className="text-base sm:text-lg text-slate-600 leading-relaxed font-normal">
              AnnaDhata connects restaurants, hotels, hostels, and institutional kitchens with verified NGOs and community shelters in real-time. Driven by Google Gemini and smart location matching.
            </p>

            {/* CTAs */}
            <div className="flex flex-col sm:flex-row items-center justify-center gap-3 pt-2">
              <Link
                to="/register"
                className="w-full sm:w-auto px-7 py-3.5 rounded-xl font-bold text-white bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-700 hover:to-teal-700 shadow-lg shadow-emerald-500/25 transition transform hover:-translate-y-0.5 flex items-center justify-center space-x-2"
              >
                <span>Join AnnaDhata Today</span>
                <ArrowRight className="w-4 h-4" />
              </Link>
              <Link
                to="/how-it-works"
                className="w-full sm:w-auto px-7 py-3.5 rounded-xl font-semibold text-slate-700 bg-white hover:bg-slate-50 border border-slate-200 transition shadow-sm flex items-center justify-center space-x-2"
              >
                <span>See How It Works</span>
              </Link>
            </div>
          </div>

          {/* Live Impact Counter Bar */}
          <div className="mt-16 grid grid-cols-2 md:grid-cols-4 gap-4 p-6 bg-white rounded-3xl border border-slate-100 shadow-xl shadow-slate-200/50">
            <div className="text-center p-3">
              <div className="text-3xl sm:text-4xl font-extrabold text-emerald-600">
                {impact ? Math.round(impact.estimatedMealsSaved).toLocaleString() : '75+'}
              </div>
              <p className="text-xs font-semibold text-slate-700 mt-1">Meals Saved</p>
              <p className="text-[11px] text-slate-400">Delivered to beneficiaries</p>
            </div>

            <div className="text-center p-3 border-l border-slate-100">
              <div className="text-3xl sm:text-4xl font-extrabold text-teal-600">
                {impact ? Math.round(impact.totalFoodRedistributedKg).toLocaleString() : '30+'} <span className="text-xl">kg</span>
              </div>
              <p className="text-xs font-semibold text-slate-700 mt-1">Surplus Diverted</p>
              <p className="text-[11px] text-slate-400">Rescued before expiry</p>
            </div>

            <div className="text-center p-3 border-l border-slate-100">
              <div className="text-3xl sm:text-4xl font-extrabold text-emerald-700">
                {impact ? impact.estimatedCo2AvoidedKg.toFixed(1) : '75.0'} <span className="text-xl">kg</span>
              </div>
              <p className="text-xs font-semibold text-slate-700 mt-1">Est. CO2e Avoided</p>
              <p className="text-[11px] text-slate-400">UNEP/FAO emission model</p>
            </div>

            <div className="text-center p-3 border-l border-slate-100">
              <div className="text-3xl sm:text-4xl font-extrabold text-slate-900">
                {impact ? impact.successRatePercent.toFixed(0) : '100'}%
              </div>
              <p className="text-xs font-semibold text-slate-700 mt-1">Redistribution Rate</p>
              <p className="text-[11px] text-slate-400">Successful completions</p>
            </div>
          </div>
        </div>
      </section>

      {/* How It Works Section */}
      <section className="py-20 bg-slate-50/70 border-y border-slate-100">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-2xl mx-auto mb-16">
            <span className="text-xs font-bold text-emerald-600 uppercase tracking-wider">Simple 4-Step Process</span>
            <h2 className="text-3xl font-extrabold text-slate-900 mt-2">How AnnaDhata Closes the Waste Loop</h2>
            <p className="text-sm text-slate-500 mt-2">Connecting commercial food donors with community charities in minutes.</p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-4 gap-6 relative">
            {/* Step 1 */}
            <div className="bg-white p-6 rounded-2xl border border-slate-200/80 shadow-sm hover:shadow-md transition">
              <div className="w-10 h-10 rounded-xl bg-emerald-100 text-emerald-700 flex items-center justify-center font-bold text-base mb-4">
                1
              </div>
              <h3 className="font-bold text-slate-900 text-base">Donor Posts Food</h3>
              <p className="text-xs text-slate-600 mt-2 leading-relaxed">
                Describe food manually or type naturally like <em>"25 kg biryani cooked at 7 PM"</em>. Gemini AI extracts quantities and times instantly.
              </p>
            </div>

            {/* Step 2 */}
            <div className="bg-white p-6 rounded-2xl border border-slate-200/80 shadow-sm hover:shadow-md transition">
              <div className="w-10 h-10 rounded-xl bg-teal-100 text-teal-700 flex items-center justify-center font-bold text-base mb-4">
                2
              </div>
              <h3 className="font-bold text-slate-900 text-base">Smart Matching</h3>
              <p className="text-xs text-slate-600 mt-2 leading-relaxed">
                Our 4-factor scoring algorithm evaluates distance, NGO capacity, category compatibility, and urgency, ranking the top 5 receivers.
              </p>
            </div>

            {/* Step 3 */}
            <div className="bg-white p-6 rounded-2xl border border-slate-200/80 shadow-sm hover:shadow-md transition">
              <div className="w-10 h-10 rounded-xl bg-blue-100 text-blue-700 flex items-center justify-center font-bold text-base mb-4">
                3
              </div>
              <h3 className="font-bold text-slate-900 text-base">Claim & Approval</h3>
              <p className="text-xs text-slate-600 mt-2 leading-relaxed">
                Verified receiver submits a pickup claim. Donor approves and confirms pickup window with atomic lock preventing duplicate claims.
              </p>
            </div>

            {/* Step 4 */}
            <div className="bg-white p-6 rounded-2xl border border-slate-200/80 shadow-sm hover:shadow-md transition">
              <div className="w-10 h-10 rounded-xl bg-purple-100 text-purple-700 flex items-center justify-center font-bold text-base mb-4">
                4
              </div>
              <h3 className="font-bold text-slate-900 text-base">Pickup & Impact</h3>
              <p className="text-xs text-slate-600 mt-2 leading-relaxed">
                Receiver collects food before deadline. Completion triggers automated meal estimation, CO2 avoidance logging, and analytics.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Core Features */}
      <section className="py-20 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-2xl mx-auto mb-16">
            <span className="text-xs font-bold text-emerald-600 uppercase tracking-wider">Production Architecture</span>
            <h2 className="text-3xl font-extrabold text-slate-900 mt-2">Built For Reliability & Transparency</h2>
            <p className="text-sm text-slate-500 mt-2">Enterprise-grade capabilities engineered for seamless food rescue.</p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="p-6 rounded-2xl border border-slate-100 bg-slate-50/50 hover:bg-emerald-50/30 transition">
              <div className="w-12 h-12 rounded-xl bg-emerald-100 text-emerald-700 flex items-center justify-center mb-4">
                <Sparkles className="w-6 h-6" />
              </div>
              <h4 className="font-bold text-slate-900 text-base">Gemini AI Assistant</h4>
              <p className="text-xs text-slate-600 mt-2 leading-relaxed">
                Extracts structured listings from conversational prompts, generates transparent human-readable matching rationales, and screens food safety parameters.
              </p>
            </div>

            <div className="p-6 rounded-2xl border border-slate-100 bg-slate-50/50 hover:bg-emerald-50/30 transition">
              <div className="w-12 h-12 rounded-xl bg-teal-100 text-teal-700 flex items-center justify-center mb-4">
                <MapPin className="w-6 h-6" />
              </div>
              <h4 className="font-bold text-slate-900 text-base">Live Leaflet & OSM Maps</h4>
              <p className="text-xs text-slate-600 mt-2 leading-relaxed">
                Interactive OpenStreetMap pins with exact distance calculated via Haversine formula. Filter by radius, food category, and pickup urgency.
              </p>
            </div>

            <div className="p-6 rounded-2xl border border-slate-100 bg-slate-50/50 hover:bg-emerald-50/30 transition">
              <div className="w-12 h-12 rounded-xl bg-blue-100 text-blue-700 flex items-center justify-center mb-4">
                <ShieldCheck className="w-6 h-6" />
              </div>
              <h4 className="font-bold text-slate-900 text-base">Verified NGO Network</h4>
              <p className="text-xs text-slate-600 mt-2 leading-relaxed">
                Strict administrative verification of organization registration numbers and documentation before receivers can claim surplus meals.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Live Available Food Preview */}
      {recentDonations.length > 0 && (
        <section className="py-16 bg-slate-50 border-t border-slate-100">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between mb-8">
              <div>
                <span className="text-xs font-bold text-emerald-600 uppercase tracking-wider">Live Inventory</span>
                <h3 className="text-2xl font-extrabold text-slate-900 mt-1">Available Surplus Food</h3>
              </div>
              <Link
                to="/login"
                className="mt-3 sm:mt-0 text-xs font-semibold text-emerald-600 hover:text-emerald-700 flex items-center space-x-1"
              >
                <span>Log in to claim donations</span>
                <ChevronRight className="w-4 h-4" />
              </Link>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              {recentDonations.map((d) => (
                <div key={d.id} className="bg-white rounded-2xl border border-slate-200 overflow-hidden shadow-sm hover:shadow-md transition flex flex-col">
                  {d.foodImage && (
                    <img src={d.foodImage} alt={d.foodName} className="h-44 w-full object-cover" />
                  )}
                  <div className="p-5 flex-1 flex flex-col justify-between">
                    <div>
                      <div className="flex items-center justify-between mb-2">
                        <StatusBadge status={d.status} />
                        <SafetyBadge indicator={d.safetyIndicator} />
                      </div>
                      <h4 className="font-bold text-slate-900 text-base">{d.foodName}</h4>
                      <p className="text-xs text-slate-500 mt-0.5">{d.donorName}</p>
                      <p className="text-xs text-slate-600 mt-2.5 line-clamp-2">{d.foodDescription}</p>
                    </div>

                    <div className="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between text-xs text-slate-600">
                      <span className="font-bold text-slate-900">{d.quantity} {d.measurementUnit}</span>
                      <span className="flex items-center text-amber-600">
                        <Clock className="w-3.5 h-3.5 mr-1" />
                        Deadline: {new Date(d.pickupDeadline).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </section>
      )}

      {/* Dual CTA Section */}
      <section className="py-20 bg-gradient-to-r from-emerald-800 to-teal-900 text-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-10">
            {/* Donor CTA */}
            <div className="bg-white/10 backdrop-blur-md p-8 rounded-3xl border border-white/20">
              <div className="w-12 h-12 rounded-2xl bg-white/20 flex items-center justify-center text-white mb-4">
                <Utensils className="w-6 h-6" />
              </div>
              <h3 className="text-2xl font-bold">Have Surplus Food?</h3>
              <p className="text-xs text-emerald-100 mt-2 leading-relaxed">
                Restaurants, banquets, hostels, and canteens can list surplus food in 60 seconds. Eliminate disposal fees, reduce landfill waste, and feed people in need.
              </p>
              <Link
                to="/register"
                className="mt-6 inline-flex items-center space-x-2 px-5 py-2.5 bg-white text-emerald-900 rounded-xl font-bold text-xs hover:bg-emerald-50 transition"
              >
                <span>Register as a Food Donor</span>
                <ArrowRight className="w-4 h-4" />
              </Link>
            </div>

            {/* Receiver CTA */}
            <div className="bg-white/10 backdrop-blur-md p-8 rounded-3xl border border-white/20">
              <div className="w-12 h-12 rounded-2xl bg-white/20 flex items-center justify-center text-white mb-4">
                <Building2 className="w-6 h-6" />
              </div>
              <h3 className="text-2xl font-bold">Need Food For Your Shelter?</h3>
              <p className="text-xs text-emerald-100 mt-2 leading-relaxed">
                Verified orphanages, NGOs, care shelters, and community kitchens get instant access to clean, high-quality surplus meals nearby with verified safety screening.
              </p>
              <Link
                to="/register"
                className="mt-6 inline-flex items-center space-x-2 px-5 py-2.5 bg-emerald-500 text-white rounded-xl font-bold text-xs hover:bg-emerald-400 transition"
              >
                <span>Register as a Receiver NGO</span>
                <ArrowRight className="w-4 h-4" />
              </Link>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
};
