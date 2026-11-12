import React from 'react';
import {
  Sparkles,
  MapPin,
  ShieldCheck,
  BarChart3,
  Bell,
  Cpu,
  Lock,
  Database,
  ArrowRight,
} from 'lucide-react';
import { Link } from 'react-router-dom';

export const FeaturesPage: React.FC = () => {
  return (
    <div className="bg-white py-16">
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 space-y-16">
        {/* Header */}
        <div className="text-center space-y-4">
          <span className="text-xs font-bold text-emerald-600 uppercase tracking-wider">Engineering Specs</span>
          <h1 className="text-4xl font-extrabold text-slate-900 tracking-tight">System Capabilities & Features</h1>
          <p className="text-base text-slate-600 max-w-2xl mx-auto">
            Discover the technical and algorithmic building blocks that make AnnaDhata the most dependable food redistribution platform.
          </p>
        </div>

        {/* Feature 1: AI Donation Assistant */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8 items-center p-8 rounded-3xl bg-slate-50 border border-slate-200">
          <div className="space-y-4">
            <div className="inline-flex items-center space-x-1.5 px-3 py-1 bg-emerald-100 text-emerald-800 rounded-full text-xs font-semibold">
              <Sparkles className="w-3.5 h-3.5" />
              <span>Feature A</span>
            </div>
            <h3 className="text-2xl font-bold text-slate-900">AI Natural Language Extraction</h3>
            <p className="text-xs text-slate-600 leading-relaxed">
              Donors don't have to fill out complex forms during busy kitchen hours. Simply type or speak: <em>"We have 25 kg of vegetable biryani prepared at 7 PM. Must be picked up before 9 PM."</em>
            </p>
            <ul className="text-xs text-slate-700 space-y-1.5">
              <li>&bull; Extracts food name, category, quantity, and measurement unit</li>
              <li>&bull; Converts relative times into strict ISO-8601 timestamps</li>
              <li>&bull; Never publishes automatically—donor reviews and confirms first</li>
            </ul>
          </div>
          <div className="bg-slate-900 text-emerald-400 p-5 rounded-2xl font-mono text-xs overflow-x-auto shadow-inner">
            <div className="text-slate-500 mb-2">// Gemini AI Structured JSON Output</div>
            <pre>{`{
  "foodName": "Vegetable Biryani",
  "quantity": 25.0,
  "unit": "kg",
  "foodCategory": "Cooked food",
  "preparationTime": "2026-11-06T19:00:00Z",
  "pickupDeadline": "2026-11-06T21:00:00Z",
  "storageAdvice": "Keep warm at 65°C"
}`}</pre>
          </div>
        </div>

        {/* Feature 2: 4-Factor Smart Matching */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8 items-center p-8 rounded-3xl bg-slate-50 border border-slate-200">
          <div className="space-y-4 md:order-2">
            <div className="inline-flex items-center space-x-1.5 px-3 py-1 bg-teal-100 text-teal-800 rounded-full text-xs font-semibold">
              <Cpu className="w-3.5 h-3.5" />
              <span>Feature B</span>
            </div>
            <h3 className="text-2xl font-bold text-slate-900">4-Factor Weighted Matching Engine</h3>
            <p className="text-xs text-slate-600 leading-relaxed">
              Deterministic scoring guarantees mathematically ranked recommendations, while Gemini generates human-readable justifications explaining why the receiver fits best.
            </p>
            <div className="grid grid-cols-2 gap-3 text-xs pt-2">
              <div className="p-3 bg-white rounded-xl border border-slate-200">
                <span className="font-bold text-slate-900 block">Distance (30%)</span>
                <span className="text-slate-500">Haversine GPS calculation</span>
              </div>
              <div className="p-3 bg-white rounded-xl border border-slate-200">
                <span className="font-bold text-slate-900 block">Capacity (25%)</span>
                <span className="text-slate-500">Matches batch to storage volume</span>
              </div>
              <div className="p-3 bg-white rounded-xl border border-slate-200">
                <span className="font-bold text-slate-900 block">Urgency (25%)</span>
                <span className="text-slate-500">Hours remaining before deadline</span>
              </div>
              <div className="p-3 bg-white rounded-xl border border-slate-200">
                <span className="font-bold text-slate-900 block">Category (20%)</span>
                <span className="text-slate-500">Receiver dietary requirements</span>
              </div>
            </div>
          </div>
          <div className="p-6 bg-white rounded-2xl border border-slate-200 shadow-sm md:order-1 space-y-3">
            <h4 className="font-bold text-slate-800 text-xs uppercase tracking-wider">Generated Rationale Preview</h4>
            <div className="p-4 bg-emerald-50 text-emerald-900 rounded-xl text-xs leading-relaxed border border-emerald-100">
              <Sparkles className="w-4 h-4 text-emerald-600 mb-1" />
              <em>"Hope Care Foundation is ranked #1 because it is located only 1.2 km away, requires Cooked Food for 65 residents, and has verified capacity to collect the 25 kg batch within 90 minutes."</em>
            </div>
          </div>
        </div>

        {/* Feature 3: Safety & Expiration */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8 items-center p-8 rounded-3xl bg-slate-50 border border-slate-200">
          <div className="space-y-4">
            <div className="inline-flex items-center space-x-1.5 px-3 py-1 bg-amber-100 text-amber-800 rounded-full text-xs font-semibold">
              <ShieldCheck className="w-3.5 h-3.5" />
              <span>Feature C</span>
            </div>
            <h3 className="text-2xl font-bold text-slate-900">Food Safety & Automatic Expiry</h3>
            <p className="text-xs text-slate-600 leading-relaxed">
              Surplus food safety is paramount. Our dual screening checks both deterministic physical parameters and semantic descriptions.
            </p>
            <ul className="text-xs text-slate-700 space-y-1.5">
              <li>&bull; Automatic scheduled task expires listings when deadline passes</li>
              <li>&bull; Flagged listings require administrative review prior to redistribution</li>
              <li>&bull; Mandatory temperature & storage handling declarations</li>
            </ul>
          </div>
          <div className="space-y-3">
            <div className="p-4 bg-white rounded-xl border border-emerald-200 flex items-center justify-between text-xs">
              <span className="font-semibold text-emerald-800">LOW RISK</span>
              <span className="text-slate-500">Fresh cooked, short window, hot holding</span>
            </div>
            <div className="p-4 bg-white rounded-xl border border-amber-200 flex items-center justify-between text-xs">
              <span className="font-semibold text-amber-800">REVIEW REQUIRED</span>
              <span className="text-slate-500">Missing specific storage/holding details</span>
            </div>
            <div className="p-4 bg-white rounded-xl border border-rose-200 flex items-center justify-between text-xs">
              <span className="font-semibold text-rose-800">EXPIRED</span>
              <span className="text-slate-500">Pickup window elapsed; claiming blocked</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
