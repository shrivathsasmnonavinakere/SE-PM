import React, { useState } from 'react';
import { passportApi } from '../services/api';
import { FoodPassport } from '../types';
import { StatusBadge } from '../components/StatusBadge';
import { SafetyBadge } from '../components/SafetyBadge';
import { QrCode, ShieldCheck, CheckCircle2, AlertCircle, Loader2, ScanLine } from 'lucide-react';

export const PassportVerifyPage: React.FC = () => {
  const [passportInput, setPassportInput] = useState('');
  const [notes, setNotes] = useState('');
  const [verifying, setVerifying] = useState(false);
  const [verifiedPassport, setVerifiedPassport] = useState<FoodPassport | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleVerify = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!passportInput.trim()) return;

    setVerifying(true);
    setError(null);
    setVerifiedPassport(null);

    try {
      const res = await passportApi.verify(passportInput.trim(), notes.trim() || undefined);
      if (res.success && res.data) {
        setVerifiedPassport(res.data);
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Verification failed. Check code or signature.');
    } finally {
      setVerifying(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-8">
      <div className="border-b border-slate-200 pb-5">
        <div className="flex items-center space-x-2">
          <span className="p-2 bg-emerald-100 text-emerald-700 rounded-xl">
            <ScanLine className="w-5 h-5" />
          </span>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
            QR Food Passport Verifier
          </h1>
        </div>
        <p className="text-xs sm:text-sm text-slate-500 mt-1">
          Verify HMAC-signed QR Food Passports presented by charity representatives or volunteers at food pickup handover.
        </p>
      </div>

      <div className="bg-white p-6 sm:p-8 rounded-3xl border border-slate-200 shadow-sm space-y-5">
        <form onSubmit={handleVerify} className="space-y-4 text-xs">
          <div>
            <label className="block font-bold text-slate-700 mb-1">
              Passport Code or Scanned QR Payload *
            </label>
            <textarea
              rows={3}
              required
              value={passportInput}
              onChange={(e) => setPassportInput(e.target.value)}
              placeholder='Enter passport code (e.g. AD-A1B2-C3D4-E5F6) or paste scanned QR JSON payload {"passportCode":...}'
              className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl font-mono focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
            />
          </div>

          <div>
            <label className="block font-bold text-slate-700 mb-1">Handover Inspection Notes (Optional)</label>
            <input
              type="text"
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="e.g. Insulated containers sealed at 66°C; clean vehicle verified."
              className="w-full px-3.5 py-2 bg-slate-50 border border-slate-200 rounded-xl"
            />
          </div>

          <button
            type="submit"
            disabled={verifying || !passportInput.trim()}
            className="w-full py-3 bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-700 hover:to-teal-700 text-white font-bold rounded-xl shadow-md shadow-emerald-500/20 transition flex items-center justify-center space-x-2 disabled:opacity-50"
          >
            {verifying ? <Loader2 className="w-4 h-4 animate-spin" /> : <QrCode className="w-4 h-4" />}
            <span>Verify Food Passport & Confirm Pickup</span>
          </button>
        </form>

        {error && (
          <div className="p-4 bg-rose-50 text-rose-800 rounded-2xl border border-rose-200 text-xs flex items-center space-x-2">
            <AlertCircle className="w-4 h-4 text-rose-600 flex-shrink-0" />
            <span className="font-semibold">{error}</span>
          </div>
        )}

        {verifiedPassport && (
          <div className="p-6 bg-emerald-50/70 rounded-2xl border border-emerald-200 space-y-4 text-xs">
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-2 text-emerald-800 font-extrabold text-sm">
                <CheckCircle2 className="w-5 h-5 text-emerald-600" />
                <span>Pickup Handover Cryptographically Verified!</span>
              </div>
              <StatusBadge status={verifiedPassport.status} />
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 bg-white p-4 rounded-xl border border-emerald-100">
              <div>
                <span className="text-slate-400 block">Food Batch</span>
                <strong className="text-slate-900">
                  {verifiedPassport.foodName} ({verifiedPassport.allocatedQuantity} {verifiedPassport.measurementUnit})
                </strong>
              </div>
              <div>
                <span className="text-slate-400 block">Receiving Charity</span>
                <strong className="text-slate-900">{verifiedPassport.receiverName}</strong>
              </div>
              <div>
                <span className="text-slate-400 block">Verified By</span>
                <strong className="text-emerald-700">{verifiedPassport.verifiedByName}</strong>
              </div>
              <div>
                <span className="text-slate-400 block">Safety Screening</span>
                <SafetyBadge indicator={verifiedPassport.safetyIndicator} />
              </div>
            </div>

            <div className="text-[11px] text-emerald-900 flex items-center space-x-1.5">
              <ShieldCheck className="w-4 h-4 text-emerald-600" />
              <span>Claim status automatically updated to PICKED UP and Trust Scores recalculated.</span>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

