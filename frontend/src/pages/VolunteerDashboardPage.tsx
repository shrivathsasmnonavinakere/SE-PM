import React, { useState, useEffect } from 'react';
import { volunteerApi } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { VolunteerDashboard, VolunteerTask } from '../types';
import { StatusBadge } from '../components/StatusBadge';
import { TrustBadge } from '../components/TrustBadge';
import { QrPassportModal } from '../components/QrPassportModal';
import { formatLocalTime } from '../utils/datetime';
import {
  Truck,
  MapPin,
  Clock,
  CheckCircle2,
  QrCode,
  Phone,
  Loader2,
  Sliders,
  Package,
  Navigation,
  XCircle,
} from 'lucide-react';

export const VolunteerDashboardPage: React.FC = () => {
  const { user } = useAuth();
  const [dashboard, setDashboard] = useState<VolunteerDashboard | null>(null);
  const [loading, setLoading] = useState(true);
  const [actionId, setActionId] = useState<string | null>(null);
  const [passportClaimId, setPassportClaimId] = useState<string | null>(null);

  // Dispatch prefs form
  const [vehicleType, setVehicleType] = useState('Cargo Scooter / Van');
  const [maxCapacityKg, setMaxCapacityKg] = useState<number>(30);
  const [serviceRadiusKm, setServiceRadiusKm] = useState<number>(20);
  const [availableForDispatch, setAvailableForDispatch] = useState(true);
  const [savingPrefs, setSavingPrefs] = useState(false);

  useEffect(() => {
    loadDashboard();
  }, []);

  const loadDashboard = async () => {
    try {
      const res = await volunteerApi.getDashboard();
      if (res.success && res.data) {
        setDashboard(res.data);
        const vp = res.data.volunteerProfile;
        if (vp) {
          setVehicleType(vp.vehicleType || 'Cargo Scooter / Van');
          setMaxCapacityKg(vp.maxCapacityKg || 30);
          setServiceRadiusKm(vp.serviceRadiusKm || 20);
          setAvailableForDispatch(vp.availableForDispatch);
        }
      }
    } catch (err) {
      console.error('Failed to load volunteer dashboard:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSavePrefs = async (e: React.FormEvent) => {
    e.preventDefault();
    setSavingPrefs(true);
    try {
      await volunteerApi.updateProfile({
        vehicleType,
        maxCapacityKg: Number(maxCapacityKg),
        serviceRadiusKm: Number(serviceRadiusKm),
        availableForDispatch,
      });
      await loadDashboard();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to update dispatch preferences');
    } finally {
      setSavingPrefs(false);
    }
  };

  const handleAcceptTask = async (claimId: string) => {
    setActionId(claimId);
    try {
      await volunteerApi.acceptTask(claimId);
      await loadDashboard();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Could not accept delivery mission');
    } finally {
      setActionId(null);
    }
  };

  const handleReleaseTask = async (claimId: string) => {
    if (!window.confirm('Step back from this mission before pickup?')) return;
    setActionId(claimId);
    try {
      await volunteerApi.releaseTask(claimId);
      await loadDashboard();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Could not release mission');
    } finally {
      setActionId(null);
    }
  };

  const handleMarkDelivered = async (claimId: string) => {
    setActionId(claimId);
    try {
      await volunteerApi.markDelivered(claimId);
      await loadDashboard();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Could not mark delivery completed');
    } finally {
      setActionId(null);
    }
  };

  if (loading) {
    return (
      <div className="min-h-[70vh] flex items-center justify-center">
        <Loader2 className="w-8 h-8 animate-spin text-emerald-600" />
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 border-b border-slate-200 pb-6">
        <div>
          <div className="flex items-center space-x-2">
            <span className="p-2 bg-indigo-100 text-indigo-700 rounded-xl">
              <Truck className="w-5 h-5" />
            </span>
            <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
              Volunteer Food Rescue Network
            </h1>
          </div>
          <p className="text-xs sm:text-sm text-slate-500 mt-1">
            Bridge commercial donor kitchens and community NGOs by picking up QR-verified surplus meals.
          </p>
        </div>

        <TrustBadge
          score={dashboard?.trustScore}
          tier={dashboard?.trustTier}
          userId={user?.id}
          label="Volunteer Trust"
        />
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 sm:gap-6">
        <div className="p-5 bg-white rounded-3xl border border-slate-200 shadow-sm space-y-1">
          <span className="text-xs font-bold text-slate-500 uppercase">Open Missions</span>
          <div className="text-3xl font-extrabold text-indigo-600">{dashboard?.openTasksCount || 0}</div>
          <p className="text-[11px] text-slate-400">Available in your zone</p>
        </div>
        <div className="p-5 bg-white rounded-3xl border border-slate-200 shadow-sm space-y-1">
          <span className="text-xs font-bold text-slate-500 uppercase">Active Assigned</span>
          <div className="text-3xl font-extrabold text-purple-600">{dashboard?.activeMissionsCount || 0}</div>
          <p className="text-[11px] text-slate-400">In progress</p>
        </div>
        <div className="p-5 bg-white rounded-3xl border border-slate-200 shadow-sm space-y-1">
          <span className="text-xs font-bold text-slate-500 uppercase">Completed Runs</span>
          <div className="text-3xl font-extrabold text-emerald-600">{dashboard?.completedDeliveriesCount || 0}</div>
          <p className="text-[11px] text-slate-400">Successful NGO handovers</p>
        </div>
        <div className="p-5 bg-white rounded-3xl border border-slate-200 shadow-sm space-y-1">
          <span className="text-xs font-bold text-slate-500 uppercase">Food Transported</span>
          <div className="text-3xl font-extrabold text-teal-600">
            {(dashboard?.totalKgTransported || 0).toFixed(1)} <span className="text-base text-slate-500">kg</span>
          </div>
          <p className="text-[11px] text-slate-400">~{dashboard?.estimatedMealsDelivered || 0} meals delivered</p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Left: Dispatch Preferences */}
        <div className="bg-white p-6 rounded-3xl border border-slate-200 shadow-sm space-y-4 h-fit">
          <div className="flex items-center space-x-2">
            <Sliders className="w-4 h-4 text-emerald-600" />
            <h3 className="font-bold text-slate-900 text-sm">Courier Preferences</h3>
          </div>

          <form onSubmit={handleSavePrefs} className="space-y-3 text-xs">
            <label className="flex items-center justify-between p-3 bg-slate-50 rounded-2xl border border-slate-200 cursor-pointer">
              <span className="font-bold text-slate-800">Available for Dispatch Alerts</span>
              <input
                type="checkbox"
                checked={availableForDispatch}
                onChange={(e) => setAvailableForDispatch(e.target.checked)}
                className="w-4 h-4 text-emerald-600 rounded"
              />
            </label>

            <div>
              <label className="block font-bold text-slate-700 mb-1">Vehicle / Transport Type</label>
              <select
                value={vehicleType}
                onChange={(e) => setVehicleType(e.target.value)}
                className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl"
              >
                <option>Two-Wheeler / Scooter</option>
                <option>Cargo Scooter / Van</option>
                <option>Car / Hatchback</option>
                <option>Refrigerated Tempo</option>
                <option>Bicycle / Walking</option>
              </select>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block font-bold text-slate-700 mb-1">Max Capacity (kg)</label>
                <input
                  type="number"
                  min={1}
                  value={maxCapacityKg}
                  onChange={(e) => setMaxCapacityKg(parseFloat(e.target.value))}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl"
                />
              </div>
              <div>
                <label className="block font-bold text-slate-700 mb-1">Radius (km)</label>
                <input
                  type="number"
                  min={1}
                  value={serviceRadiusKm}
                  onChange={(e) => setServiceRadiusKm(parseFloat(e.target.value))}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={savingPrefs}
              className="w-full py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white font-bold rounded-xl transition flex items-center justify-center"
            >
              {savingPrefs ? <Loader2 className="w-4 h-4 animate-spin" /> : 'Save Dispatch Settings'}
            </button>
          </form>
        </div>

        {/* Right: Active & Open Delivery Missions */}
        <div className="lg:col-span-2 space-y-6">
          {/* Active Assigned Missions */}
          <div className="bg-white p-6 rounded-3xl border border-indigo-200 shadow-sm space-y-4">
            <h3 className="font-bold text-slate-900 text-sm">
              My Active Delivery Missions ({dashboard?.activeMissions?.length || 0})
            </h3>
            {dashboard?.activeMissions && dashboard.activeMissions.length > 0 ? (
              <div className="space-y-4">
                {dashboard.activeMissions.map((m) => (
                  <MissionCard
                    key={m.claimId}
                    task={m}
                    isMine
                    loading={actionId === m.claimId}
                    onVerifyPassport={() => setPassportClaimId(m.claimId)}
                    onDeliver={() => handleMarkDelivered(m.claimId)}
                    onRelease={() => handleReleaseTask(m.claimId)}
                  />
                ))}
              </div>
            ) : (
              <p className="text-xs text-slate-400 py-6 text-center">
                No active missions assigned. Pick an open task below to start delivering!
              </p>
            )}
          </div>

          {/* Open Nearby Delivery Requests */}
          <div className="bg-white p-6 rounded-3xl border border-slate-200 shadow-sm space-y-4">
            <h3 className="font-bold text-slate-900 text-sm">
              Open Delivery Board ({dashboard?.openTasks?.length || 0})
            </h3>
            {dashboard?.openTasks && dashboard.openTasks.length > 0 ? (
              <div className="space-y-4">
                {dashboard.openTasks.map((t) => (
                  <MissionCard
                    key={t.claimId}
                    task={t}
                    isMine={false}
                    loading={actionId === t.claimId}
                    onAccept={() => handleAcceptTask(t.claimId)}
                  />
                ))}
              </div>
            ) : (
              <div className="py-10 text-center text-xs text-slate-400 space-y-2">
                <Package className="w-8 h-8 text-slate-300 mx-auto" />
                <p>No unassigned approved food deliveries right now.</p>
              </div>
            )}
          </div>
        </div>
      </div>

      {passportClaimId && (
        <QrPassportModal
          claimId={passportClaimId}
          canVerify={true}
          onVerified={() => loadDashboard()}
          onClose={() => setPassportClaimId(null)}
        />
      )}
    </div>
  );
};

const MissionCard: React.FC<{
  task: VolunteerTask;
  isMine: boolean;
  loading: boolean;
  onAccept?: () => void;
  onVerifyPassport?: () => void;
  onDeliver?: () => void;
  onRelease?: () => void;
}> = ({ task, isMine, loading, onAccept, onVerifyPassport, onDeliver, onRelease }) => {
  return (
    <div className="p-5 bg-slate-50 rounded-2xl border border-slate-200/80 space-y-3 text-xs">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <div className="flex items-center space-x-2">
          <StatusBadge status={task.claimStatus} />
          {task.volunteerRequested && (
            <span className="px-2 py-0.5 bg-amber-100 text-amber-800 rounded-full text-[10px] font-bold">
              Volunteer Priority Request
            </span>
          )}
          {task.qrVerified && (
            <span className="px-2 py-0.5 bg-emerald-100 text-emerald-800 rounded-full text-[10px] font-bold">
              QR Verified
            </span>
          )}
        </div>
        <span className="font-bold text-slate-800">
          {task.claimedQuantity} {task.measurementUnit}
        </span>
      </div>

      <div>
        <h4 className="font-bold text-slate-900 text-sm">{task.foodName}</h4>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 mt-2 text-slate-600">
          <div className="p-2.5 bg-white rounded-xl border border-slate-100 space-y-0.5">
            <span className="text-[10px] font-bold text-emerald-700 uppercase">1. Pickup Kitchen</span>
            <p className="font-bold text-slate-900">{task.donorName}</p>
            <p className="text-[11px] text-slate-500 flex items-center">
              <MapPin className="w-3 h-3 mr-1" />
              {task.pickupAddress}
            </p>
            {task.donorPhone && (
              <p className="text-[11px] text-slate-500 flex items-center">
                <Phone className="w-3 h-3 mr-1" />
                {task.donorPhone}
              </p>
            )}
          </div>

          <div className="p-2.5 bg-white rounded-xl border border-slate-100 space-y-0.5">
            <span className="text-[10px] font-bold text-teal-700 uppercase">2. Dropoff Shelter</span>
            <p className="font-bold text-slate-900">{task.receiverName}</p>
            <p className="text-[11px] text-slate-500 flex items-center">
              <MapPin className="w-3 h-3 mr-1" />
              {task.dropoffAddress || 'Registered Shelter Location'}
            </p>
            {task.receiverPhone && (
              <p className="text-[11px] text-slate-500 flex items-center">
                <Phone className="w-3 h-3 mr-1" />
                {task.receiverPhone}
              </p>
            )}
          </div>
        </div>
      </div>

      <div className="flex flex-wrap items-center justify-between gap-2 text-[11px] text-slate-500 pt-1">
        <div className="flex items-center space-x-3">
          {task.distanceFromVolunteerKm != null && (
            <span className="flex items-center font-semibold text-indigo-700">
              <Navigation className="w-3 h-3 mr-1" />
              {task.distanceFromVolunteerKm.toFixed(1)} km away
            </span>
          )}
          {task.routeDistanceKm != null && <span>Route: {task.routeDistanceKm.toFixed(1)} km</span>}
          {task.pickupDeadline && (
            <span className="flex items-center text-amber-700">
              <Clock className="w-3 h-3 mr-1" />
              Deadline: {formatLocalTime(task.pickupDeadline)}
            </span>
          )}
        </div>
      </div>

      {!isMine && onAccept && (
        <button
          onClick={onAccept}
          disabled={loading}
          className="w-full py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white font-bold rounded-xl transition flex items-center justify-center space-x-1.5"
        >
          {loading ? <Loader2 className="w-4 h-4 animate-spin" /> : <Truck className="w-4 h-4" />}
          <span>Accept Pickup & Delivery Mission</span>
        </button>
      )}

      {isMine && (
        <div className="flex flex-wrap items-center gap-2 pt-1">
          {!task.qrVerified && onVerifyPassport && (
            <button
              onClick={onVerifyPassport}
              className="flex-1 py-2 px-3 bg-indigo-600 hover:bg-indigo-700 text-white font-bold rounded-xl transition flex items-center justify-center space-x-1.5"
            >
              <QrCode className="w-4 h-4" />
              <span>Scan / Verify QR Pickup</span>
            </button>
          )}
          {onDeliver && (
            <button
              onClick={onDeliver}
              disabled={loading}
              className="flex-1 py-2 px-3 bg-emerald-600 hover:bg-emerald-700 text-white font-bold rounded-xl transition flex items-center justify-center space-x-1.5"
            >
              <CheckCircle2 className="w-4 h-4" />
              <span>Confirm Delivered to NGO</span>
            </button>
          )}
          {!task.qrVerified && task.claimStatus === 'APPROVED' && onRelease && (
            <button
              onClick={onRelease}
              disabled={loading}
              className="py-2 px-3 bg-slate-200 hover:bg-rose-50 text-slate-700 hover:text-rose-700 font-semibold rounded-xl transition flex items-center space-x-1"
            >
              <XCircle className="w-3.5 h-3.5" />
              <span>Release</span>
            </button>
          )}
        </div>
      )}
    </div>
  );
};

