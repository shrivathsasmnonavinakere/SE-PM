import React, { useState, useEffect } from 'react';
import { needApi } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { ReceiverNeed, FoodCategory } from '../types';
import {
  Building2,
  PlusCircle,
  Package,
  Users,
  Clock,
  MapPin,
  Trash2,
  CheckCircle2,
  Loader2,
} from 'lucide-react';

const CATEGORIES: FoodCategory[] = [
  'Cooked food',
  'Rice and grains',
  'Vegetables',
  'Fruits',
  'Bread and bakery',
  'Packaged food',
  'Dairy products',
  'Other',
];

export const FoodRequirementsPage: React.FC = () => {
  const { user } = useAuth();
  const [needs, setNeeds] = useState<ReceiverNeed[]>([]);
  const [loading, setLoading] = useState(true);

  // Form state
  const [requiredCategory, setRequiredCategory] = useState<FoodCategory>('Cooked food');
  const [requiredQuantity, setRequiredQuantity] = useState<number>(25);
  const [preferredFoodType, setPreferredFoodType] = useState('Vegetarian cooked meals, rice and dal');
  const [numberOfBeneficiaries, setNumberOfBeneficiaries] = useState<number>(60);
  const [maxReceivingCapacity, setMaxReceivingCapacity] = useState<number>(50);
  const [pickupWindowStart, setPickupWindowStart] = useState('12:00');
  const [pickupWindowEnd, setPickupWindowEnd] = useState('21:00');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadNeeds();
  }, []);

  const loadNeeds = async () => {
    try {
      const res = await needApi.getMy();
      if (res.success && res.data) setNeeds(res.data);
    } catch (err) {
      console.error('Failed to load needs:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateNeed = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);

    try {
      await needApi.create({
        requiredCategory,
        requiredQuantity: Number(requiredQuantity),
        measurementUnit: 'kg',
        preferredFoodType,
        numberOfBeneficiaries: Number(numberOfBeneficiaries),
        maxReceivingCapacity: Number(maxReceivingCapacity),
        pickupAvailable: true,
        pickupWindowStart,
        pickupWindowEnd,
        address: user?.address || 'Bangalore',
        latitude: user?.latitude || 12.9716,
        longitude: user?.longitude || 77.5946,
      });
      await loadNeeds();
      alert('Food requirement published! Smart matching engine will prioritize matching donors for your shelter.');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to save requirement');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeleteNeed = async (needId: string) => {
    if (!window.confirm('Deactivate this requirement?')) return;
    try {
      await needApi.delete(needId);
      loadNeeds();
    } catch (err: any) {
      alert('Failed to deactivate requirement');
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
      {/* Title */}
      <div className="border-b border-slate-200 pb-5">
        <div className="flex items-center space-x-2">
          <span className="p-2 bg-teal-100 text-teal-700 rounded-xl">
            <Building2 className="w-5 h-5" />
          </span>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
            Organization Food Requirements & Demand
          </h1>
        </div>
        <p className="text-xs sm:text-sm text-slate-500 mt-1">
          Declare the daily food quantities and dietary categories your beneficiaries need to receive AI matching recommendations.
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Declare Requirement Form */}
        <div className="lg:col-span-1 bg-white p-6 sm:p-7 rounded-3xl border border-slate-200 shadow-sm space-y-4">
          <h3 className="font-bold text-slate-900 text-sm">Post Food Requirement</h3>
          <p className="text-xs text-slate-500">Informs nearby commercial kitchens of your shelter's daily appetite.</p>

          {error && (
            <div className="p-3 bg-rose-50 text-rose-700 rounded-xl text-xs font-semibold">
              {error}
            </div>
          )}

          <form onSubmit={handleCreateNeed} className="space-y-4 text-xs">
            <div>
              <label className="block font-bold text-slate-700 mb-1">Required Category *</label>
              <select
                value={requiredCategory}
                onChange={(e) => setRequiredCategory(e.target.value as FoodCategory)}
                className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-emerald-500"
              >
                {CATEGORIES.map((cat) => (
                  <option key={cat} value={cat}>
                    {cat}
                  </option>
                ))}
              </select>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block font-bold text-slate-700 mb-1">Target Qty (kg) *</label>
                <input
                  type="number"
                  min="1"
                  required
                  value={requiredQuantity}
                  onChange={(e) => setRequiredQuantity(parseFloat(e.target.value))}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl"
                />
              </div>

              <div>
                <label className="block font-bold text-slate-700 mb-1">Max Capacity (kg) *</label>
                <input
                  type="number"
                  min="1"
                  required
                  value={maxReceivingCapacity}
                  onChange={(e) => setMaxReceivingCapacity(parseFloat(e.target.value))}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl"
                />
              </div>
            </div>

            <div>
              <label className="block font-bold text-slate-700 mb-1">Beneficiary Headcount</label>
              <input
                type="number"
                min="1"
                value={numberOfBeneficiaries}
                onChange={(e) => setNumberOfBeneficiaries(parseInt(e.target.value))}
                placeholder="e.g. 60 residents"
                className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl"
              />
            </div>

            <div>
              <label className="block font-bold text-slate-700 mb-1">Preferred Food Types</label>
              <input
                type="text"
                value={preferredFoodType}
                onChange={(e) => setPreferredFoodType(e.target.value)}
                placeholder="e.g. Rice, biryani, vegetable curry"
                className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl"
              />
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block font-bold text-slate-700 mb-1">Pickup From</label>
                <input
                  type="time"
                  value={pickupWindowStart}
                  onChange={(e) => setPickupWindowStart(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl"
                />
              </div>

              <div>
                <label className="block font-bold text-slate-700 mb-1">Pickup Until</label>
                <input
                  type="time"
                  value={pickupWindowEnd}
                  onChange={(e) => setPickupWindowEnd(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={submitting}
              className="w-full py-2.5 bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-700 hover:to-teal-700 text-white rounded-xl font-bold transition flex items-center justify-center space-x-1.5 shadow-sm disabled:opacity-50"
            >
              {submitting ? <Loader2 className="w-3.5 h-3.5 animate-spin" /> : <PlusCircle className="w-4 h-4" />}
              <span>Save Food Requirement</span>
            </button>
          </form>
        </div>

        {/* Active Requirements List */}
        <div className="lg:col-span-2 space-y-4">
          <h3 className="font-bold text-slate-900 text-sm">Active Declared Requirements ({needs.length})</h3>

          {needs.length > 0 ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              {needs.map((n) => (
                <div key={n.id} className="p-5 bg-white rounded-3xl border border-slate-200 shadow-sm space-y-3 flex flex-col justify-between">
                  <div className="space-y-2">
                    <div className="flex items-center justify-between">
                      <span className="font-bold text-xs px-2.5 py-0.5 rounded-full bg-teal-50 text-teal-800 border border-teal-200">
                        {n.requiredCategory}
                      </span>
                      <button
                        onClick={() => handleDeleteNeed(n.id)}
                        className="text-slate-400 hover:text-rose-600 transition"
                        title="Deactivate requirement"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>

                    <h4 className="font-bold text-slate-900 text-sm">
                      {n.requiredQuantity} {n.measurementUnit} Needed
                    </h4>
                    <p className="text-xs text-slate-500 italic">"{n.preferredFoodType}"</p>

                    <div className="pt-2 border-t border-slate-100 space-y-1 text-xs text-slate-600">
                      <div className="flex items-center justify-between">
                        <span className="text-slate-400">Beneficiaries:</span>
                        <strong className="text-slate-900">{n.numberOfBeneficiaries || 'N/A'} people</strong>
                      </div>
                      <div className="flex items-center justify-between">
                        <span className="text-slate-400">Max Capacity:</span>
                        <strong className="text-slate-900">{n.maxReceivingCapacity} kg</strong>
                      </div>
                      <div className="flex items-center justify-between">
                        <span className="text-slate-400">Pickup Window:</span>
                        <strong className="text-slate-800">{n.pickupWindowStart} - {n.pickupWindowEnd}</strong>
                      </div>
                    </div>
                  </div>

                  <div className="p-2.5 bg-emerald-50/70 text-emerald-900 rounded-xl text-[11px] font-medium flex items-center space-x-1.5 border border-emerald-100">
                    <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" />
                    <span>Active in AI Smart Matching Engine</span>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="text-center py-16 bg-white rounded-3xl border border-slate-200 p-8 space-y-2">
              <Package className="w-10 h-10 text-slate-300 mx-auto" />
              <h4 className="text-sm font-semibold text-slate-700">No active food requirements</h4>
              <p className="text-xs text-slate-400">Add your first requirement using the form to start matching with donors.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
