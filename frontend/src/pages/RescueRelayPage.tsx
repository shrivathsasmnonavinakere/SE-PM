import React, { useState, useEffect } from 'react';
import { rescueRelayApi } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { RescueRelayEvent } from '../types';
import { StatusBadge } from '../components/StatusBadge';
import { formatLocalTime } from '../utils/datetime';
import {
  AlertTriangle,
  RefreshCw,
  CheckCircle,
  Clock,
  Sparkles,
  MapPin,
  Loader2,
  XCircle,
  ShieldAlert,
} from 'lucide-react';

export const RescueRelayPage: React.FC = () => {
  const { role } = useAuth();
  const [events, setEvents] = useState<RescueRelayEvent[]>([]);
  const [loading, setLoading] = useState(true);
  const [scanning, setScanning] = useState(false);
  const [actionId, setActionId] = useState<string | null>(null);
  const [extendMinutes, setExtendMinutes] = useState<{ [claimId: string]: number }>({});

  useEffect(() => {
    loadRelays();
  }, []);

  const loadRelays = async () => {
    try {
      const res = await rescueRelayApi.getMy();
      if (res.success && res.data) {
        setEvents(res.data);
      }
    } catch (err) {
      console.error('Failed to load Rescue Relay alerts:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleScanNow = async () => {
    setScanning(true);
    try {
      const res = await rescueRelayApi.scan(15);
      await loadRelays();
      const count = res.data?.length || 0;
      alert(`Rescue Relay scan completed. ${count} new overdue pickup alert(s) detected.`);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Scan failed');
    } finally {
      setScanning(false);
    }
  };

  const handleRelease = async (claimId: string) => {
    setActionId(claimId);
    try {
      const ext = extendMinutes[claimId] ?? 60;
      const res = await rescueRelayApi.release(claimId, {
        extendDeadlineMinutes: ext,
        notes: 'Released with donor approval via Rescue Relay',
      });
      await loadRelays();
      if (res.success && res.data) {
        const altCount = res.data.alternativeReceivers?.length || 0;
        alert(`Allocation released! ${altCount} alternative NGO(s) notified immediately.`);
      }
    } catch (err: any) {
      alert(err.response?.data?.message || 'Could not release allocation');
    } finally {
      setActionId(null);
    }
  };

  const handleDismiss = async (claimId: string) => {
    setActionId(claimId);
    try {
      await rescueRelayApi.dismiss(claimId, 'Donor elected to wait for scheduled charity.');
      await loadRelays();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Could not dismiss alert');
    } finally {
      setActionId(null);
    }
  };

  const canManage = role === 'ROLE_DONOR' || role === 'ROLE_ADMIN';

  if (loading) {
    return (
      <div className="min-h-[70vh] flex items-center justify-center">
        <Loader2 className="w-8 h-8 animate-spin text-emerald-600" />
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-8">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 border-b border-slate-200 pb-5">
        <div>
          <div className="flex items-center space-x-2">
            <span className="p-2 bg-orange-100 text-orange-700 rounded-xl">
              <AlertTriangle className="w-5 h-5" />
            </span>
            <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
              Rescue Relay Operations
            </h1>
          </div>
          <p className="text-xs sm:text-sm text-slate-500 mt-1">
            Automated detection of missed pickups, donor-approved quantity release, and instant alternative NGO rematching.
          </p>
        </div>

        {canManage && (
          <button
            type="button"
            onClick={handleScanNow}
            disabled={scanning}
            className="inline-flex items-center space-x-2 px-4 py-2.5 bg-orange-600 hover:bg-orange-700 text-white font-bold text-xs rounded-xl shadow-sm transition disabled:opacity-50"
          >
            <RefreshCw className={`w-4 h-4 ${scanning ? 'animate-spin' : ''}`} />
            <span>{scanning ? 'Scanning Overdue Pickups...' : 'Scan Overdue Pickups Now'}</span>
          </button>
        )}
      </div>

      {events.length > 0 ? (
        <div className="space-y-5">
          {events.map((ev) => (
            <div
              key={ev.id}
              className={`p-6 rounded-3xl border bg-white shadow-sm space-y-4 text-xs ${
                ev.status === 'AWAITING_DONOR_APPROVAL' ? 'border-orange-300 ring-2 ring-orange-500/10' : 'border-slate-200'
              }`}
            >
              <div className="flex flex-wrap items-start justify-between gap-3 border-b border-slate-100 pb-3">
                <div className="space-y-1">
                  <div className="flex items-center space-x-2">
                    <StatusBadge status={ev.status} />
                    <span className="text-slate-400 font-medium">
                      Detected at {formatLocalTime(ev.detectedAt)}
                    </span>
                  </div>
                  <h3 className="text-base font-extrabold text-slate-900">{ev.foodName}</h3>
                  <p className="text-slate-600">
                    Missed by: <strong>{ev.originalReceiverName}</strong> &bull; Quantity at Risk:{' '}
                    <strong className="text-orange-700">
                      {ev.quantityAtRisk} {ev.measurementUnit}
                    </strong>
                  </p>
                </div>

                <div className="text-right text-[11px] text-slate-500 space-y-0.5">
                  {ev.confirmedPickupTimeSnapshot && (
                    <p className="flex items-center justify-end">
                      <Clock className="w-3.5 h-3.5 mr-1 text-orange-500" />
                      Scheduled Pickup: <strong>{formatLocalTime(ev.confirmedPickupTimeSnapshot)}</strong>
                    </p>
                  )}
                  {ev.pickupDeadlineSnapshot && (
                    <p>Food Deadline: {formatLocalTime(ev.pickupDeadlineSnapshot)}</p>
                  )}
                </div>
              </div>

              {ev.resolutionNotes && (
                <p className="text-slate-600 bg-slate-50 p-3 rounded-xl border border-slate-100">
                  {ev.resolutionNotes}
                </p>
              )}

              {/* Suggested Alternative NGOs */}
              {ev.alternativeReceivers && ev.alternativeReceivers.length > 0 && (
                <div className="space-y-2 pt-1">
                  <div className="flex items-center space-x-1.5 text-emerald-800 font-bold">
                    <Sparkles className="w-4 h-4 text-emerald-600" />
                    <span>
                      {ev.status === 'RELEASED'
                        ? 'Alternative NGOs Notified via Rescue Relay'
                        : 'Ready Alternative NGOs (Excluding Late Receiver)'}
                    </span>
                  </div>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                    {ev.alternativeReceivers.slice(0, 4).map((alt) => (
                      <div key={alt.receiverId} className="p-3 bg-emerald-50/50 rounded-2xl border border-emerald-100 space-y-1">
                        <div className="flex items-center justify-between">
                          <strong className="text-slate-900">{alt.receiverName}</strong>
                          <span className="font-extrabold text-emerald-700">{Math.round(alt.matchScore)}% Match</span>
                        </div>
                        <p className="text-[11px] text-slate-500 flex items-center">
                          <MapPin className="w-3 h-3 mr-1" />
                          {alt.distanceKm.toFixed(1)} km away &bull; Trust: {Math.round(alt.receiverTrustScore || 80)}/100
                        </p>
                        <p className="text-[11px] text-slate-600 line-clamp-2">{alt.recommendationExplanation}</p>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {canManage && ev.status === 'AWAITING_DONOR_APPROVAL' && (
                <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3 pt-2 border-t border-slate-100">
                  <div className="flex items-center space-x-2">
                    <label className="text-slate-600 font-semibold">Extend Pickup Window (mins):</label>
                    <input
                      type="number"
                      min={0}
                      max={360}
                      value={extendMinutes[ev.claimId] ?? 60}
                      onChange={(e) =>
                        setExtendMinutes({ ...extendMinutes, [ev.claimId]: parseInt(e.target.value || '0', 10) })
                      }
                      className="w-20 px-2.5 py-1.5 bg-slate-50 border border-slate-200 rounded-lg font-bold"
                    />
                  </div>

                  <div className="flex items-center space-x-2">
                    <button
                      type="button"
                      onClick={() => handleRelease(ev.claimId)}
                      disabled={actionId === ev.claimId}
                      className="px-4 py-2 bg-orange-600 hover:bg-orange-700 text-white font-bold rounded-xl transition flex items-center space-x-1.5 disabled:opacity-50"
                    >
                      {actionId === ev.claimId ? <Loader2 className="w-4 h-4 animate-spin" /> : <CheckCircle className="w-4 h-4" />}
                      <span>Approve Release & Match Alternative NGOs</span>
                    </button>
                    <button
                      type="button"
                      onClick={() => handleDismiss(ev.claimId)}
                      disabled={actionId === ev.claimId}
                      className="px-3 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold rounded-xl transition flex items-center space-x-1"
                    >
                      <XCircle className="w-3.5 h-3.5" />
                      <span>Keep Waiting</span>
                    </button>
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      ) : (
        <div className="bg-white rounded-3xl border border-slate-200 p-12 text-center space-y-3">
          <ShieldAlert className="w-12 h-12 text-emerald-300 mx-auto" />
          <h3 className="text-sm font-bold text-slate-800">All Pickups On Track!</h3>
          <p className="text-xs text-slate-500 max-w-md mx-auto">
            Rescue Relay automatically flags approved allocations if confirmed pickup windows pass unverified without QR Food Passport confirmation.
          </p>
        </div>
      )}
    </div>
  );
};

