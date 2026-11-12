import React from 'react';
import { Link } from 'react-router-dom';
import { HeartHandshake, Shield, Sparkles, MapPin, Globe, Leaf } from 'lucide-react';

export const Footer: React.FC = () => {
  return (
    <footer className="bg-slate-900 text-slate-300 pt-16 pb-12 border-t border-slate-800">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-10 pb-12 border-b border-slate-800">
          {/* Brand Info */}
          <div className="space-y-4 md:col-span-1">
            <div className="flex items-center space-x-2.5">
              <div className="w-9 h-9 rounded-xl bg-emerald-500 flex items-center justify-center text-white font-bold">
                <HeartHandshake className="w-5 h-5" />
              </div>
              <span className="text-xl font-extrabold text-white">
                Anna<span className="text-emerald-400">Dhata</span>
              </span>
            </div>
            <p className="text-xs text-slate-400 leading-relaxed">
              Smart Food Waste Reduction and Sustainable Redistribution Ecosystem. Connecting commercial kitchens and verified NGOs using AI and location intelligence.
            </p>
            <div className="flex items-center space-x-2 text-xs text-emerald-400">
              <Leaf className="w-4 h-4" />
              <span>Aligned with UN SDG 2 & SDG 12</span>
            </div>
          </div>

          {/* Quick Links */}
          <div>
            <h4 className="text-sm font-semibold text-white uppercase tracking-wider mb-3">Platform</h4>
            <ul className="space-y-2 text-xs">
              <li><Link to="/about" className="hover:text-emerald-400 transition">About Us</Link></li>
              <li><Link to="/how-it-works" className="hover:text-emerald-400 transition">How It Works</Link></li>
              <li><Link to="/features" className="hover:text-emerald-400 transition">Key Features</Link></li>
              <li><Link to="/impact" className="hover:text-emerald-400 transition">Impact & Sustainability</Link></li>
              <li><Link to="/contact" className="hover:text-emerald-400 transition">Contact & Help</Link></li>
            </ul>
          </div>

          {/* Roles */}
          <div>
            <h4 className="text-sm font-semibold text-white uppercase tracking-wider mb-3">Participation</h4>
            <ul className="space-y-2 text-xs">
              <li><Link to="/register" className="hover:text-emerald-400 transition">For Restaurants & Hotels</Link></li>
              <li><Link to="/register" className="hover:text-emerald-400 transition">For Hostels & Canteens</Link></li>
              <li><Link to="/register" className="hover:text-emerald-400 transition">For NGOs & Care Homes</Link></li>
              <li><Link to="/register" className="hover:text-emerald-400 transition">For Community Kitchens</Link></li>
              <li><Link to="/login" className="hover:text-emerald-400 transition">Member Login</Link></li>
            </ul>
          </div>

          {/* AI & Trust */}
          <div className="space-y-3">
            <h4 className="text-sm font-semibold text-white uppercase tracking-wider mb-3">AI & Safety</h4>
            <p className="text-xs text-slate-400 leading-relaxed">
              Powered by Google Gemini for natural-language donation parsing, 4-factor smart matching, and automated food safety screening.
            </p>
            <div className="p-3 rounded-xl bg-slate-800/80 border border-slate-700/60 text-[11px] text-slate-300 flex items-center space-x-2">
              <Shield className="w-4 h-4 text-emerald-400 flex-shrink-0" />
              <span>Food safety indicators are software screening aids and not official lab certifications.</span>
            </div>
          </div>
        </div>

        {/* Bottom Bar */}
        <div className="mt-8 flex flex-col sm:flex-row items-center justify-between text-xs text-slate-500">
          <p>&copy; {new Date().getFullYear()} AnnaDhata Ecosystem. All rights reserved.</p>
          <p className="mt-2 sm:mt-0 font-medium text-slate-400">
            Tagline: <em className="text-emerald-400">Turning Surplus Food Into Meals, Not Waste.</em>
          </p>
        </div>
      </div>
    </footer>
  );
};
