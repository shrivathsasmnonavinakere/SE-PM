import React from 'react';
import { HeartHandshake, ShieldCheck, Leaf, Globe, Target, Award, Users } from 'lucide-react';
import { Link } from 'react-router-dom';

export const AboutPage: React.FC = () => {
  return (
    <div className="bg-white py-16">
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 space-y-16">
        {/* Header */}
        <div className="text-center space-y-4">
          <span className="text-xs font-bold text-emerald-600 uppercase tracking-wider">About AnnaDhata</span>
          <h1 className="text-4xl font-extrabold text-slate-900 tracking-tight">
            Engineering a World Where Good Food Feeds People, Not Landfills
          </h1>
          <p className="text-base text-slate-600 max-w-2xl mx-auto leading-relaxed">
            AnnaDhata was created with a clear mission: turning surplus commercial food into dignified meals through artificial intelligence, strict safety screening, and algorithmic location matching.
          </p>
        </div>

        {/* The Problem & Solution Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          <div className="p-8 rounded-3xl bg-rose-50/50 border border-rose-100 space-y-4">
            <span className="text-xs font-bold text-rose-600 uppercase tracking-wider">The Reality</span>
            <h3 className="text-xl font-bold text-slate-900">The Paradox of Food Waste</h3>
            <p className="text-xs text-slate-600 leading-relaxed">
              Globally, over 1.3 billion tonnes of food are wasted annually. In urban centers, restaurants, wedding halls, hostels, and catering companies discard tens of thousands of kilograms of fresh food simply because safe, rapid redistribution channels did not exist.
            </p>
            <p className="text-xs text-slate-600 leading-relaxed">
              Rotting food in municipal dumps produces massive methane emissions, driving climate degradation while neighboring charities struggle to feed orphans, elders, and displaced families.
            </p>
          </div>

          <div className="p-8 rounded-3xl bg-emerald-50/50 border border-emerald-100 space-y-4">
            <span className="text-xs font-bold text-emerald-600 uppercase tracking-wider">The Innovation</span>
            <h3 className="text-xl font-bold text-slate-900">AnnaDhata’s Algorithmic Bridge</h3>
            <p className="text-xs text-slate-600 leading-relaxed">
              AnnaDhata replaces frantic phone calls with an automated intelligent routing engine. Donors can describe food in natural language, while Google Gemini extracts preparation timestamps and deadlines.
            </p>
            <p className="text-xs text-slate-600 leading-relaxed">
              Our 4-factor deterministic scoring algorithm instantly identifies verified receivers within optimal transit distance, ensuring surplus meals are claimed and collected long before deadlines.
            </p>
          </div>
        </div>

        {/* Core Pillars */}
        <div className="space-y-6">
          <h3 className="text-2xl font-bold text-slate-900 text-center">Our Core Operating Pillars</h3>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="p-6 rounded-2xl border border-slate-200 bg-white shadow-sm space-y-3">
              <div className="w-10 h-10 rounded-xl bg-emerald-100 text-emerald-700 flex items-center justify-center">
                <Leaf className="w-5 h-5" />
              </div>
              <h4 className="font-bold text-slate-900 text-sm">Empirical Sustainability</h4>
              <p className="text-xs text-slate-500 leading-relaxed">
                We track real metrics—measuring meals saved (0.4 kg/meal) and CO2 avoided (2.5 kg CO2e/kg waste) based strictly on completed transactions.
              </p>
            </div>

            <div className="p-6 rounded-2xl border border-slate-200 bg-white shadow-sm space-y-3">
              <div className="w-10 h-10 rounded-xl bg-teal-100 text-teal-700 flex items-center justify-center">
                <ShieldCheck className="w-5 h-5" />
              </div>
              <h4 className="font-bold text-slate-900 text-sm">Rigorous Safety First</h4>
              <p className="text-xs text-slate-500 leading-relaxed">
                Deterministic validation enforces strict preparation-to-deadline thresholds, storage checks, and software screening flags to uphold hygiene standards.
              </p>
            </div>

            <div className="p-6 rounded-2xl border border-slate-200 bg-white shadow-sm space-y-3">
              <div className="w-10 h-10 rounded-xl bg-blue-100 text-blue-700 flex items-center justify-center">
                <Users className="w-5 h-5" />
              </div>
              <h4 className="font-bold text-slate-900 text-sm">Dignified Community Support</h4>
              <p className="text-xs text-slate-500 leading-relaxed">
                Surplus food is treated with the same pride as fresh banquet catering, empowering care institutions with predictable, nutritious meals.
              </p>
            </div>
          </div>
        </div>

        {/* CTA */}
        <div className="p-8 rounded-3xl bg-slate-900 text-white text-center space-y-4">
          <h3 className="text-2xl font-bold">Be Part of the Solution</h3>
          <p className="text-xs text-slate-400 max-w-lg mx-auto">
            Whether you operate a restaurant kitchen or manage an orphanage shelter, AnnaDhata is your partner in ending avoidable food waste.
          </p>
          <div className="pt-2">
            <Link
              to="/register"
              className="inline-flex items-center space-x-2 px-6 py-3 bg-emerald-500 text-white rounded-xl font-bold text-xs hover:bg-emerald-400 transition"
            >
              <span>Get Started Now</span>
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};
