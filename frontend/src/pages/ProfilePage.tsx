import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { authApi, trustApi } from '../services/api';
import { TrustScore } from '../types';
import { TrustBadge } from '../components/TrustBadge';
import {
  User as UserIcon,
  Mail,
  Phone,
  MapPin,
  Building2,
  ShieldCheck,
  CheckCircle2,
  Loader2,
  Crosshair,
  Award,
} from 'lucide-react';

export const ProfilePage: React.FC = () => {
  const { user, refreshUser } = useAuth();
  const [trustData, setTrustData] = useState<TrustScore | null>(null);

  const [name, setName] = useState(user?.name || '');
  const [phone, setPhone] = useState(user?.phone || '');
  const [address, setAddress] = useState(user?.address || '');
  const [organizationName, setOrganizationName] = useState(user?.organizationName || '');
  const [organizationType, setOrganizationType] = useState(user?.organizationType || '');
  const [registrationNumber, setRegistrationNumber] = useState(user?.registrationNumber || '');
  const [verificationDocsText, setVerificationDocsText] = useState(
    (user?.verificationDocuments || []).join('\n')
  );
  const [latitude, setLatitude] = useState<number>(user?.latitude || 12.9716);
  const [longitude, setLongitude] = useState<number>(user?.longitude || 77.5946);

  const [saving, setSaving] = useState(false);
  const [savedSuccess, setSavedSuccess] = useState(false);

  useEffect(() => {
    if (user) {
      setName(user.name);
      setPhone(user.phone || '');
      setAddress(user.address || '');
      setOrganizationName(user.organizationName || '');
      setOrganizationType(user.organizationType || '');
      setRegistrationNumber(user.registrationNumber || '');
      setVerificationDocsText((user.verificationDocuments || []).join('\n'));
      setLatitude(user.latitude || 12.9716);
      setLongitude(user.longitude || 77.5946);
      trustApi
        .getMy()
        .then((res) => {
          if (res.success && res.data) setTrustData(res.data);
        })
        .catch(() => {});
    }
  }, [user]);

  const handleDetectGps = () => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition((pos) => {
        setLatitude(pos.coords.latitude);
        setLongitude(pos.coords.longitude);
      });
    }
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      const verificationDocuments = verificationDocsText
        .split('\n')
        .map((s) => s.trim())
        .filter(Boolean);
      await authApi.updateProfile({
        name,
        phone,
        address,
        organizationName,
        organizationType,
        registrationNumber,
        verificationDocuments: user?.role === 'ROLE_RECEIVER' ? verificationDocuments : undefined,
        latitude,
        longitude,
      });
      await refreshUser();
      setSavedSuccess(true);
      setTimeout(() => setSavedSuccess(false), 3000);
    } catch (err) {
      alert('Failed to update profile');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-8">
      {/* Title */}
      <div className="border-b border-slate-200 pb-5">
        <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">Organization Profile</h1>
        <p className="text-xs sm:text-sm text-slate-500 mt-1">Manage contact credentials, verification documents, and pickup coordinates.</p>
      </div>

      <div className="bg-white p-8 rounded-3xl border border-slate-200 shadow-sm space-y-6">
        {/* Verification Status Badge */}
        <div className="p-4 bg-slate-50 rounded-2xl border border-slate-200/80 flex items-center justify-between">
          <div className="space-y-0.5">
            <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">Account Role & Status</span>
            <div className="flex items-center space-x-2">
              <strong className="text-sm text-slate-900 font-bold">{user?.role?.replace('ROLE_', '')}</strong>
              <span className="text-xs text-slate-400">&bull;</span>
              <span
                className={`text-xs font-bold px-2 py-0.5 rounded-full ${
                  user?.verificationStatus === 'VERIFIED'
                    ? 'bg-emerald-100 text-emerald-800'
                    : 'bg-amber-100 text-amber-800'
                }`}
              >
                {user?.verificationStatus}
              </span>
            </div>
          </div>
          <ShieldCheck className="w-6 h-6 text-emerald-600" />
        </div>

        {/* Trust & Reliability Breakdown Card */}
        {trustData && (
          <div className="p-5 bg-gradient-to-br from-emerald-50/70 to-teal-50/60 rounded-2xl border border-emerald-200/80 space-y-4">
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div className="flex items-center space-x-2">
                <Award className="w-5 h-5 text-emerald-600" />
                <div>
                  <h3 className="text-sm font-bold text-slate-900">Trust & Reliability Score</h3>
                  <p className="text-[11px] text-slate-600">{trustData.explanation}</p>
                </div>
              </div>
              <TrustBadge
                score={trustData.overallScore}
                tier={trustData.tier}
                userId={trustData.userId}
                label={trustData.userName}
              />
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              {trustData.factors.map((f) => (
                <div key={f.factorName} className="p-3 bg-white rounded-xl border border-emerald-100 space-y-1 text-xs">
                  <div className="flex items-center justify-between">
                    <span className="font-bold text-slate-800">{f.factorName}</span>
                    <span className="font-black text-emerald-700">
                      {f.score.toFixed(0)}/100 ({f.weightPercent}%)
                    </span>
                  </div>
                  <p className="text-[11px] text-slate-500">{f.detail}</p>
                </div>
              ))}
            </div>
          </div>
        )}

        {savedSuccess && (
          <div className="p-3 bg-emerald-50 text-emerald-800 rounded-xl text-xs font-semibold flex items-center space-x-2 border border-emerald-200">
            <CheckCircle2 className="w-4 h-4 text-emerald-600" />
            <span>Profile settings saved successfully!</span>
          </div>
        )}

        <form onSubmit={handleSave} className="space-y-4 text-xs">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block font-bold text-slate-700 mb-1">Contact Name</label>
              <input
                type="text"
                required
                value={name}
                onChange={(e) => setName(e.target.value)}
                className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl"
              />
            </div>

            <div>
              <label className="block font-bold text-slate-700 mb-1">Email Address</label>
              <input
                type="email"
                disabled
                value={user?.email || ''}
                className="w-full px-3 py-2 bg-slate-100 text-slate-500 border border-slate-200 rounded-xl cursor-not-allowed"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block font-bold text-slate-700 mb-1">Phone Number</label>
              <input
                type="tel"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl"
              />
            </div>

            <div>
              <label className="block font-bold text-slate-700 mb-1">Organization Name</label>
              <input
                type="text"
                value={organizationName}
                onChange={(e) => setOrganizationName(e.target.value)}
                className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl"
              />
            </div>
          </div>

          <div>
            <label className="block font-bold text-slate-700 mb-1">Physical Address</label>
            <input
              type="text"
              value={address}
              onChange={(e) => setAddress(e.target.value)}
              className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl"
            />
          </div>

          {user?.role === 'ROLE_RECEIVER' && (
            <div className="space-y-4">
              <div>
                <label className="block font-bold text-slate-700 mb-1">NGO Trust / Registration Number</label>
                <input
                  type="text"
                  value={registrationNumber}
                  onChange={(e) => setRegistrationNumber(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl"
                />
              </div>
              <div>
                <label className="block font-bold text-slate-700 mb-1">
                  Verification Document URLs (one URL per line — Trust Deed / 80G / NGO Darpan certificate)
                </label>
                <textarea
                  rows={3}
                  value={verificationDocsText}
                  onChange={(e) => setVerificationDocsText(e.target.value)}
                  placeholder="https://example.org/certificates/ngo-darpan-80g.pdf"
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl font-mono text-[11px]"
                />
              </div>
            </div>
          )}

          {/* GPS Coordinates */}
          <div className="p-4 bg-slate-50 rounded-2xl border border-slate-200/80 space-y-2">
            <div className="flex items-center justify-between">
              <span className="font-bold text-slate-700 text-xs">GPS Coordinates for Matching & Maps</span>
              <button
                type="button"
                onClick={handleDetectGps}
                className="text-[11px] text-emerald-600 font-bold hover:text-emerald-700 flex items-center space-x-1"
              >
                <Crosshair className="w-3.5 h-3.5" />
                <span>Auto-detect</span>
              </button>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <input
                type="number"
                step="0.0001"
                value={latitude}
                onChange={(e) => setLatitude(parseFloat(e.target.value))}
                className="w-full px-3 py-2 bg-white border border-slate-200 rounded-xl text-xs"
              />
              <input
                type="number"
                step="0.0001"
                value={longitude}
                onChange={(e) => setLongitude(parseFloat(e.target.value))}
                className="w-full px-3 py-2 bg-white border border-slate-200 rounded-xl text-xs"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={saving}
            className="w-full py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl font-bold transition flex items-center justify-center space-x-2 disabled:opacity-50"
          >
            {saving ? <Loader2 className="w-4 h-4 animate-spin" /> : null}
            <span>Save Profile Updates</span>
          </button>
        </form>
      </div>
    </div>
  );
};
