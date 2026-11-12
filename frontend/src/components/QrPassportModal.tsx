import React, { useState, useEffect } from 'react';
import { passportApi } from '../services/api';
import { FoodPassport } from '../types';
import { StatusBadge } from './StatusBadge';
import { SafetyBadge } from './SafetyBadge';
import { QrCode, Copy, Check, ShieldCheck, Loader2, X, Clock } from 'lucide-react';

interface QrPassportModalProps {
  claimId: string;
  canVerify?: boolean;
  onVerified?: () => void;
  onClose: () => void;
}

export const QrPassportModal: React.FC<QrPassportModalProps> = ({
  claimId,
  canVerify = false,
  onVerified,
  onClose,
}) => {
  const [passport, setPassport] = useState<FoodPassport | null>(null);
  const [loading, setLoading] = useState(true);
  const [verifying, setVerifying] = useState(false);
  const [copied, setCopied] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadPassport();
  }, [claimId]);

  const loadPassport = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await passportApi.getByClaim(claimId);
      if (res.success && res.data) {
        setPassport(res.data);
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Unable to load QR Food Passport.');
    } finally {
      setLoading(false);
    }
  };

  const handleCopy = () => {
    if (!passport) return;
    navigator.clipboard?.writeText(passport.passportCode);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleQuickVerify = async () => {
    if (!passport) return;
    setVerifying(true);
    setError(null);
    try {
      const res = await passportApi.verify(passport.passportCode, 'Verified via QR Food Passport modal');
      if (res.success && res.data) {
        setPassport(res.data);
        if (onVerified) onVerified();
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Passport verification failed.');
    } finally {
      setVerifying(false);
    }
  };

  return (
    <div
      className="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-sm flex items-center justify-center p-4"
      onClick={onClose}
    >
      <div
        className="bg-white max-w-md w-full rounded-3xl p-6 sm:p-7 space-y-5 shadow-2xl"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-start justify-between border-b border-slate-100 pb-3">
          <div className="flex items-center space-x-2">
            <span className="p-2 bg-emerald-100 text-emerald-700 rounded-xl">
              <QrCode className="w-5 h-5" />
            </span>
            <div>
              <h3 className="text-base font-extrabold text-slate-900">QR Food Passport</h3>
              <p className="text-[11px] text-slate-500">Tamper-evident pickup authorization token</p>
            </div>
          </div>
          <button onClick={onClose} className="p-1 text-slate-400 hover:text-slate-700 rounded-lg">
            <X className="w-5 h-5" />
          </button>
        </div>

        {loading ? (
          <div className="py-12 flex items-center justify-center">
            <Loader2 className="w-7 h-7 animate-spin text-emerald-600" />
          </div>
        ) : error ? (
          <div className="p-4 bg-rose-50 text-rose-700 rounded-2xl border border-rose-200 text-xs font-medium">
            {error}
          </div>
        ) : passport ? (
          <div className="space-y-4 text-xs">
            <div className="flex items-center justify-between">
              <StatusBadge status={passport.status} />
              <SafetyBadge indicator={passport.safetyIndicator} />
            </div>

            {/* Real Server-Generated QR Image */}
            <div className="bg-slate-50 rounded-2xl p-5 border border-slate-200/80 flex flex-col items-center space-y-3">
              {passport.qrImageBase64 ? (
                <img
                  src={passport.qrImageBase64}
                  alt={`QR Code for ${passport.passportCode}`}
                  className="w-48 h-48 rounded-xl border border-slate-200 bg-white p-2 shadow-sm"
                />
              ) : (
                <div className="w-48 h-48 rounded-xl border border-dashed border-slate-300 flex items-center justify-center text-slate-400">
                  QR Preview Unavailable
                </div>
              )}

              <div className="flex items-center space-x-2">
                <code className="px-3 py-1 bg-white border border-slate-200 rounded-lg font-mono font-bold text-slate-800 text-xs">
                  {passport.passportCode}
                </code>
                <button
                  type="button"
                  onClick={handleCopy}
                  className="p-1.5 bg-white border border-slate-200 rounded-lg text-slate-600 hover:text-emerald-700"
                  title="Copy Passport Code"
                >
                  {copied ? <Check className="w-3.5 h-3.5 text-emerald-600" /> : <Copy className="w-3.5 h-3.5" />}
                </button>
              </div>
              <p className="text-[10px] text-slate-400 font-mono">
                HMAC-SHA256 Sig: {passport.hmacSignature}
              </p>
            </div>

            <div className="p-4 bg-slate-50 rounded-2xl border border-slate-100 space-y-1.5">
              <div className="flex justify-between">
                <span className="text-slate-500">Item & Allocated Batch:</span>
                <strong className="text-slate-900">
                  {passport.foodName} ({passport.allocatedQuantity} {passport.measurementUnit})
                </strong>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-500">Donor Kitchen:</span>
                <strong className="text-slate-800">{passport.donorName}</strong>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-500">Receiver Charity:</span>
                <strong className="text-slate-800">{passport.receiverName}</strong>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-500">Valid Until:</span>
                <strong className="text-amber-700 flex items-center">
                  <Clock className="w-3 h-3 mr-1" />
                  {new Date(passport.expiresAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                </strong>
              </div>
            </div>

            {passport.status === 'VERIFIED_PICKUP' && (
              <div className="p-3 bg-emerald-50 text-emerald-900 rounded-xl border border-emerald-200 text-xs flex items-center space-x-2">
                <ShieldCheck className="w-4 h-4 text-emerald-600 flex-shrink-0" />
                <span>
                  Verified by <strong>{passport.verifiedByName}</strong> at{' '}
                  {passport.verifiedAt ? new Date(passport.verifiedAt).toLocaleTimeString() : 'handover'}.
                </span>
              </div>
            )}

            {canVerify && passport.status === 'ACTIVE' && (
              <button
                type="button"
                onClick={handleQuickVerify}
                disabled={verifying}
                className="w-full py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white font-bold rounded-xl transition flex items-center justify-center space-x-1.5 disabled:opacity-50"
              >
                {verifying ? <Loader2 className="w-4 h-4 animate-spin" /> : <ShieldCheck className="w-4 h-4" />}
                <span>Verify Handover & Mark Picked Up</span>
              </button>
            )}
          </div>
        ) : null}
      </div>
    </div>
  );
};

