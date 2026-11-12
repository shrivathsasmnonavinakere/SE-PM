import React from 'react';
import { Utensils, Building2, Sparkles, MapPin, CheckCircle, Clock, ShieldCheck, ArrowRight } from 'lucide-react';
import { Link } from 'react-router-dom';

export const HowItWorksPage: React.FC = () => {
  return (
    <div className="bg-white py-16">
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 space-y-16">
        {/* Header */}
        <div className="text-center space-y-4">
          <span className="text-xs font-bold text-emerald-600 uppercase tracking-wider">Step-by-Step Guide</span>
          <h1 className="text-4xl font-extrabold text-slate-900 tracking-tight">How AnnaDhata Operates</h1>
          <p className="text-base text-slate-600 max-w-2xl mx-auto">
            From kitchen surplus to community dinner table in four transparent, automated steps.
          </p>
        </div>

        {/* Dual Workflow View */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-10">
          {/* Donor Journey */}
          <div className="p-8 rounded-3xl bg-slate-50 border border-slate-200/80 space-y-6">
            <div className="flex items-center space-x-3 text-emerald-700">
              <div className="p-2 bg-emerald-100 rounded-xl">
                <Utensils className="w-6 h-6" />
              </div>
              <h3 className="text-xl font-bold text-slate-900">The Donor Workflow</h3>
            </div>

            <div className="space-y-4 text-xs">
              <div className="p-4 bg-white rounded-2xl border border-slate-200 space-y-1">
                <span className="font-bold text-emerald-600 block">1. Post Surplus Listing</span>
                <p className="text-slate-600">
                  Use standard form or simply enter natural language in our AI Assistant. Food name, quantity, category, and pickup deadline are validated.
                </p>
              </div>

              <div className="p-4 bg-white rounded-2xl border border-slate-200 space-y-1">
                <span className="font-bold text-emerald-600 block">2. Review AI Matching Recommendations</span>
                <p className="text-slate-600">
                  The matching engine scores local charities. View top 5 receiver matches along with Gemini’s explanation of why they are ideal.
                </p>
              </div>

              <div className="p-4 bg-white rounded-2xl border border-slate-200 space-y-1">
                <span className="font-bold text-emerald-600 block">3. Approve Claim Request</span>
                <p className="text-slate-600">
                  When a verified NGO submits a claim, review their proposed pickup window and click Approve. Other claims are safely rejected.
                </p>
              </div>

              <div className="p-4 bg-white rounded-2xl border border-slate-200 space-y-1">
                <span className="font-bold text-emerald-600 block">4. Handover & Impact Confirmation</span>
                <p className="text-slate-600">
                  When NGO staff collects the food, mark collection as Picked Up and Completed. Impact dashboard updates your meals saved!
                </p>
              </div>
            </div>
          </div>

          {/* Receiver Journey */}
          <div className="p-8 rounded-3xl bg-slate-50 border border-slate-200/80 space-y-6">
            <div className="flex items-center space-x-3 text-teal-700">
              <div className="p-2 bg-teal-100 rounded-xl">
                <Building2 className="w-6 h-6" />
              </div>
              <h3 className="text-xl font-bold text-slate-900">The Receiver NGO Workflow</h3>
            </div>

            <div className="space-y-4 text-xs">
              <div className="p-4 bg-white rounded-2xl border border-slate-200 space-y-1">
                <span className="font-bold text-teal-600 block">1. Get Verified & Set Needs</span>
                <p className="text-slate-600">
                  Register your NGO, upload 80G/12A or trust certificate, and specify required food categories, beneficiary count, and receiving capacity.
                </p>
              </div>

              <div className="p-4 bg-white rounded-2xl border border-slate-200 space-y-1">
                <span className="font-bold text-teal-600 block">2. Discover Nearby Food Listings</span>
                <p className="text-slate-600">
                  Browse real-time listings on the interactive Leaflet Map or filter list by distance (km), category, and time left before deadline.
                </p>
              </div>

              <div className="p-4 bg-white rounded-2xl border border-slate-200 space-y-1">
                <span className="font-bold text-teal-600 block">3. Submit Claim with Pickup Time</span>
                <p className="text-slate-600">
                  Select quantity and enter your proposed pickup time. You will receive an instant notification when the donor confirms.
                </p>
              </div>

              <div className="p-4 bg-white rounded-2xl border border-slate-200 space-y-1">
                <span className="font-bold text-teal-600 block">4. Collect & Distribute Meals</span>
                <p className="text-slate-600">
                  Arrive at donor premises with food containers, collect meals, and distribute them to your shelter beneficiaries with verified dignity.
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* AI & Concurrency Guarantee */}
        <div className="p-8 rounded-3xl bg-emerald-950 text-white space-y-4">
          <div className="flex items-center space-x-2 text-emerald-400">
            <Sparkles className="w-5 h-5" />
            <span className="text-xs font-bold uppercase tracking-wider">Zero Double-Claim Concurrency Lock</span>
          </div>
          <h3 className="text-xl font-bold">Guaranteed Reliability for Urgent Perishables</h3>
          <p className="text-xs text-emerald-200 leading-relaxed max-w-3xl">
            Because surplus food has strict pickup deadlines, AnnaDhata utilizes atomic MongoDB conditional updates to prevent race conditions. Only one receiver can successfully claim a donation. Once confirmed, status shifts to CLAIMED and other candidates are notified instantly to prevent wasted travel.
          </p>
        </div>
      </div>
    </div>
  );
};
