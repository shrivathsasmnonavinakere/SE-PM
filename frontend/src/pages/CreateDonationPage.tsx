import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { donationApi, aiApi } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { FoodCategory } from '../types';
import { toLocalDateTimeInputValue } from '../utils/datetime';
import {
  Sparkles,
  Utensils,
  Package,
  Clock,
  MapPin,
  ShieldCheck,
  CheckCircle2,
  AlertCircle,
  Loader2,
  ArrowRight,
  Image as ImageIcon,
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

export const CreateDonationPage: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuth();

  const [aiPrompt, setAiPrompt] = useState('');
  const [aiLoading, setAiLoading] = useState(false);
  const [aiExtracted, setAiExtracted] = useState(false);
  const [aiNotes, setAiNotes] = useState('');

  // Form state
  const [foodName, setFoodName] = useState('');
  const [foodDescription, setFoodDescription] = useState('');
  const [foodCategory, setFoodCategory] = useState<FoodCategory>('Cooked food');
  const [quantity, setQuantity] = useState<number>(10);
  const [measurementUnit, setMeasurementUnit] = useState('kg');
  const [foodImage, setFoodImage] = useState('https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=800&auto=format&fit=crop');

  // Timestamps
  const now = new Date();
  const defaultPrep = toLocalDateTimeInputValue(new Date(now.getTime() - 2 * 60 * 60 * 1000));
  const defaultDeadline = toLocalDateTimeInputValue(new Date(now.getTime() + 4 * 60 * 60 * 1000));

  const [preparationDateTime, setPreparationDateTime] = useState(defaultPrep);
  const [pickupDeadline, setPickupDeadline] = useState(defaultDeadline);

  const [pickupAddress, setPickupAddress] = useState(user?.address || 'Brigade Road, Bangalore');
  const [latitude, setLatitude] = useState<number>(user?.latitude || 12.9716);
  const [longitude, setLongitude] = useState<number>(user?.longitude || 77.5946);

  const [foodHandlingDetails, setFoodHandlingDetails] = useState('Maintained in clean thermal warmers above 65°C');
  const [storageInformation, setStorageInformation] = useState('Sealed in food-grade stainless steel containers');

  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleAiExtraction = async (sampleText?: string) => {
    const text = sampleText || aiPrompt;
    if (!text.trim()) return;

    setAiLoading(true);
    setError(null);

    try {
      const res = await aiApi.extractDonation(text);
      if (res.success && res.data) {
        const d = res.data;
        setFoodName(d.foodName || 'Surplus Food');
        setFoodCategory(d.foodCategory || 'Cooked food');
        setQuantity(d.quantity || 10);
        setMeasurementUnit(d.unit || 'kg');
        if (d.foodDescription) setFoodDescription(d.foodDescription);
        if (d.storageAdvice) setFoodHandlingDetails(d.storageAdvice);

        if (d.preparationDateTime) {
          try {
            setPreparationDateTime(toLocalDateTimeInputValue(d.preparationDateTime));
          } catch {}
        }
        if (d.pickupDeadline) {
          try {
            setPickupDeadline(toLocalDateTimeInputValue(d.pickupDeadline));
          } catch {}
        }

        setAiExtracted(true);
        setAiNotes(d.rawExtractionNotes || 'Extracted via Google Gemini AI');
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'AI extraction failed. Please enter details manually.');
    } finally {
      setAiLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);

    try {
      const payload = {
        foodName,
        foodDescription,
        foodCategory,
        quantity: Number(quantity),
        measurementUnit,
        foodImage,
        preparationDateTime: new Date(preparationDateTime).toISOString(),
        pickupDeadline: new Date(pickupDeadline).toISOString(),
        pickupAddress,
        latitude: Number(latitude),
        longitude: Number(longitude),
        foodHandlingDetails,
        storageInformation,
      };

      const res = await donationApi.create(payload);
      if (res.success && res.data) {
        // Direct to smart matching recommendations page for the new donation!
        navigate(`/donor/donations/${res.data.id}/matches`);
      }
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Failed to create donation listing.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-8">
      {/* Title */}
      <div className="border-b border-slate-200 pb-5">
        <div className="flex items-center space-x-2">
          <div className="p-2 bg-emerald-100 text-emerald-700 rounded-xl">
            <Utensils className="w-5 h-5" />
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">List Surplus Food Donation</h1>
        </div>
        <p className="text-xs sm:text-sm text-slate-500 mt-1">
          Post surplus food for instant matching with verified local shelters and community kitchens.
        </p>
      </div>

      {/* AI Donation Assistant Section */}
      <div className="bg-gradient-to-br from-emerald-500/10 via-teal-500/5 to-white p-6 rounded-3xl border border-emerald-200 shadow-sm space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2 text-emerald-800 font-bold text-sm">
            <Sparkles className="w-5 h-5 text-emerald-600 animate-spin" />
            <span>AI Food Donation Assistant (Powered by Gemini)</span>
          </div>
          <span className="text-[11px] font-semibold bg-emerald-100 text-emerald-800 px-2.5 py-0.5 rounded-full">
            Autofill Form
          </span>
        </div>

        <p className="text-xs text-slate-600 leading-relaxed">
          Type or paste natural kitchen notes. Our Gemini AI model will extract food details, portion volume, and required pickup deadlines for your review.
        </p>

        <div className="relative">
          <textarea
            rows={3}
            value={aiPrompt}
            onChange={(e) => setAiPrompt(e.target.value)}
            placeholder="e.g., We have 25 kg of vegetable biryani prepared at 7 PM. It must be collected before 9 PM."
            className="w-full p-3.5 text-xs bg-white border border-emerald-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 placeholder-slate-400"
          />
          <button
            type="button"
            onClick={() => handleAiExtraction()}
            disabled={aiLoading || !aiPrompt.trim()}
            className="absolute bottom-3 right-3 px-4 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-bold transition flex items-center space-x-1.5 disabled:opacity-50"
          >
            {aiLoading ? (
              <>
                <Loader2 className="w-3.5 h-3.5 animate-spin" />
                <span>Extracting...</span>
              </>
            ) : (
              <>
                <Sparkles className="w-3.5 h-3.5" />
                <span>Extract with AI</span>
              </>
            )}
          </button>
        </div>

        {/* Sample Prompt Chips */}
        <div className="flex items-center flex-wrap gap-2 pt-1 text-[11px]">
          <span className="text-slate-400 font-medium">Try example:</span>
          <button
            type="button"
            onClick={() => {
              const text = 'I have 25 kg of vegetable biryani prepared at 7 PM. It must be collected before 9 PM.';
              setAiPrompt(text);
              handleAiExtraction(text);
            }}
            className="bg-white hover:bg-emerald-50 text-emerald-800 px-2.5 py-1 rounded-lg border border-emerald-200/80 transition"
          >
            "25 kg vegetable biryani before 9 PM"
          </button>
          <button
            type="button"
            onClick={() => {
              const text = 'Surplus 40 packets of fresh bakery dinner rolls and loaves prepared this afternoon. Collect by 10 PM.';
              setAiPrompt(text);
              handleAiExtraction(text);
            }}
            className="bg-white hover:bg-emerald-50 text-emerald-800 px-2.5 py-1 rounded-lg border border-emerald-200/80 transition"
          >
            "40 packets fresh bakery rolls"
          </button>
        </div>

        {aiExtracted && (
          <div className="p-3 bg-emerald-100/70 border border-emerald-200 rounded-2xl text-xs text-emerald-900 flex items-center justify-between">
            <div className="flex items-center space-x-2">
              <CheckCircle2 className="w-4 h-4 text-emerald-700 flex-shrink-0" />
              <span><strong>Details extracted successfully!</strong> Please review and adjust the parameters below before confirming.</span>
            </div>
            <span className="text-[10px] text-emerald-700 font-mono hidden sm:inline">{aiNotes}</span>
          </div>
        )}
      </div>

      {/* Main Review & Edit Form */}
      <form onSubmit={handleSubmit} className="bg-white p-8 rounded-3xl border border-slate-200 shadow-sm space-y-6">
        <div className="border-b border-slate-100 pb-3 flex items-center justify-between">
          <h3 className="font-bold text-slate-800 text-sm">Donation Details Review & Publishing</h3>
          <span className="text-xs text-slate-400">All fields validated before publishing</span>
        </div>

        {error && (
          <div className="p-3 bg-rose-50 border border-rose-200 text-rose-700 rounded-xl text-xs font-semibold">
            {error}
          </div>
        )}

        {/* Row 1: Food Name & Category */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-5 text-xs">
          <div>
            <label className="block font-bold text-slate-700 mb-1">Food Item Name *</label>
            <input
              type="text"
              required
              value={foodName}
              onChange={(e) => setFoodName(e.target.value)}
              placeholder="e.g. Fresh Vegetable Biryani"
              className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-emerald-500 font-medium"
            />
          </div>

          <div>
            <label className="block font-bold text-slate-700 mb-1">Food Category *</label>
            <select
              value={foodCategory}
              onChange={(e) => setFoodCategory(e.target.value as FoodCategory)}
              className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-emerald-500 font-medium"
            >
              {CATEGORIES.map((cat) => (
                <option key={cat} value={cat}>
                  {cat}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* Row 2: Quantity & Unit */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-5 text-xs">
          <div>
            <label className="block font-bold text-slate-700 mb-1">Quantity *</label>
            <input
              type="number"
              step="0.5"
              min="0.5"
              required
              value={quantity}
              onChange={(e) => setQuantity(parseFloat(e.target.value))}
              className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-emerald-500 font-medium"
            />
          </div>

          <div>
            <label className="block font-bold text-slate-700 mb-1">Measurement Unit</label>
            <select
              value={measurementUnit}
              onChange={(e) => setMeasurementUnit(e.target.value)}
              className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-emerald-500 font-medium"
            >
              <option value="kg">kg (Kilograms)</option>
              <option value="packets">packets / boxes</option>
              <option value="portions">meals / portions</option>
              <option value="litres">litres</option>
            </select>
          </div>

          <div>
            <label className="block font-bold text-slate-700 mb-1">Food Image URL</label>
            <input
              type="url"
              value={foodImage}
              onChange={(e) => setFoodImage(e.target.value)}
              placeholder="https://..."
              className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-emerald-500"
            />
          </div>
        </div>

        {/* Row 3: Timestamps */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-5 text-xs">
          <div>
            <label className="block font-bold text-slate-700 mb-1">Preparation Time *</label>
            <input
              type="datetime-local"
              required
              value={preparationDateTime}
              onChange={(e) => setPreparationDateTime(e.target.value)}
              className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-emerald-500"
            />
          </div>

          <div>
            <label className="block font-bold text-slate-700 mb-1">Pickup Deadline (Expiry) *</label>
            <input
              type="datetime-local"
              required
              value={pickupDeadline}
              onChange={(e) => setPickupDeadline(e.target.value)}
              className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-emerald-500"
            />
          </div>
        </div>

        {/* Row 4: Description */}
        <div className="text-xs">
          <label className="block font-bold text-slate-700 mb-1">Food Description & Ingredients</label>
          <textarea
            rows={2}
            value={foodDescription}
            onChange={(e) => setFoodDescription(e.target.value)}
            placeholder="Describe food contents, potential allergens (peanuts, gluten), spice level, or banquet surplus details."
            className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-emerald-500"
          />
        </div>

        {/* Row 5: Safety Handling & Storage */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-5 text-xs">
          <div>
            <label className="block font-bold text-slate-700 mb-1">Food Handling & Temperature Instructions</label>
            <input
              type="text"
              value={foodHandlingDetails}
              onChange={(e) => setFoodHandlingDetails(e.target.value)}
              placeholder="e.g. Kept hot above 65°C / Chilled below 4°C"
              className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-emerald-500"
            />
          </div>

          <div>
            <label className="block font-bold text-slate-700 mb-1">Storage Condition</label>
            <input
              type="text"
              value={storageInformation}
              onChange={(e) => setStorageInformation(e.target.value)}
              placeholder="e.g. Sealed food-grade stainless steel thermoware"
              className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-emerald-500"
            />
          </div>
        </div>

        {/* Row 6: Address & Location */}
        <div className="text-xs space-y-3 pt-2 border-t border-slate-100">
          <div>
            <label className="block font-bold text-slate-700 mb-1">Pickup Address *</label>
            <input
              type="text"
              required
              value={pickupAddress}
              onChange={(e) => setPickupAddress(e.target.value)}
              placeholder="Pickup address for receiving charities"
              className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-emerald-500"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block font-bold text-slate-700 mb-1">Latitude</label>
              <input
                type="number"
                step="0.0001"
                value={latitude}
                onChange={(e) => setLatitude(parseFloat(e.target.value))}
                className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs"
              />
            </div>
            <div>
              <label className="block font-bold text-slate-700 mb-1">Longitude</label>
              <input
                type="number"
                step="0.0001"
                value={longitude}
                onChange={(e) => setLongitude(parseFloat(e.target.value))}
                className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs"
              />
            </div>
          </div>
        </div>

        {/* Submit Button */}
        <div className="pt-4 border-t border-slate-100 flex items-center justify-end space-x-3">
          <button
            type="button"
            onClick={() => navigate('/donor/dashboard')}
            className="px-5 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-semibold rounded-xl transition"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={submitting}
            className="px-6 py-2.5 bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-700 hover:to-teal-700 text-white text-xs font-bold rounded-xl shadow-md shadow-emerald-500/20 transition flex items-center space-x-2 disabled:opacity-50"
          >
            {submitting ? (
              <>
                <Loader2 className="w-4 h-4 animate-spin" />
                <span>Publishing & Matching...</span>
              </>
            ) : (
              <>
                <span>Publish & Find Best Matching NGOs</span>
                <ArrowRight className="w-4 h-4" />
              </>
            )}
          </button>
        </div>
      </form>
    </div>
  );
};
