import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Role } from '../types';
import {
  HeartHandshake,
  Utensils,
  Building2,
  Mail,
  Lock,
  User as UserIcon,
  Phone,
  MapPin,
  FileText,
  ArrowRight,
  Loader2,
  Crosshair,
  Truck,
  Link2,
} from 'lucide-react';

export const RegisterPage: React.FC = () => {
  const navigate = useNavigate();
  const { register } = useAuth();

  const [role, setRole] = useState<Role>('ROLE_DONOR');
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [phone, setPhone] = useState('');
  const [address, setAddress] = useState('');
  const [organizationName, setOrganizationName] = useState('');
  const [organizationType, setOrganizationType] = useState('Restaurant');
  const [registrationNumber, setRegistrationNumber] = useState('');
  const [verificationDocsInput, setVerificationDocsInput] = useState('');
  const [vehicleType, setVehicleType] = useState('Cargo Scooter / Van');
  const [maxCapacityKg, setMaxCapacityKg] = useState<number>(30);
  const [serviceRadiusKm, setServiceRadiusKm] = useState<number>(15);
  const [latitude, setLatitude] = useState<number>(12.9716);
  const [longitude, setLongitude] = useState<number>(77.5946);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleRoleChange = (newRole: Role) => {
    setRole(newRole);
    if (newRole === 'ROLE_DONOR') {
      setOrganizationType('Restaurant');
    } else if (newRole === 'ROLE_VOLUNTEER') {
      setOrganizationType('Volunteer Courier');
    } else {
      setOrganizationType('Shelter & Orphanage');
    }
  };

  const handleGetLocation = () => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          setLatitude(pos.coords.latitude);
          setLongitude(pos.coords.longitude);
        },
        (err) => {
          console.warn('GPS location access denied or unavailable, using defaults');
        }
      );
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const verificationDocuments = verificationDocsInput
        .split(/[\n,]/)
        .map((s) => s.trim())
        .filter(Boolean);

      const auth = await register({
        name,
        email,
        password,
        role,
        phone,
        address,
        organizationName: organizationName || name,
        organizationType,
        registrationNumber,
        verificationDocuments: role === 'ROLE_RECEIVER' && verificationDocuments.length > 0 ? verificationDocuments : undefined,
        vehicleType: role === 'ROLE_VOLUNTEER' ? vehicleType : undefined,
        maxCapacityKg: role === 'ROLE_VOLUNTEER' ? maxCapacityKg : undefined,
        serviceRadiusKm: role === 'ROLE_VOLUNTEER' ? serviceRadiusKm : undefined,
        latitude,
        longitude,
      });

      if (auth.role === 'ROLE_DONOR') {
        navigate('/donor/dashboard', { replace: true });
      } else if (auth.role === 'ROLE_RECEIVER') {
        navigate('/receiver/dashboard', { replace: true });
      } else if (auth.role === 'ROLE_VOLUNTEER') {
        navigate('/volunteer/dashboard', { replace: true });
      } else {
        navigate('/admin/dashboard', { replace: true });
      }
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Registration failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[85vh] flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8 bg-slate-50/70">
      <div className="max-w-xl w-full space-y-6">
        <div className="text-center space-y-2">
          <div className="w-12 h-12 rounded-2xl bg-gradient-to-tr from-emerald-600 to-teal-500 text-white flex items-center justify-center mx-auto shadow-md shadow-emerald-500/20">
            <HeartHandshake className="w-7 h-7" />
          </div>
          <h2 className="text-2xl font-extrabold text-slate-900 tracking-tight">Create an AnnaDhata Account</h2>
          <p className="text-xs text-slate-500">Join our sustainable food redistribution ecosystem</p>
        </div>

        {/* Role Toggle Selector */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-2.5 bg-white p-2 rounded-2xl border border-slate-200">
          <button
            type="button"
            onClick={() => handleRoleChange('ROLE_DONOR')}
            className={`flex items-center justify-center space-x-2 py-3 px-3 rounded-xl text-xs font-bold transition ${
              role === 'ROLE_DONOR'
                ? 'bg-emerald-600 text-white shadow-sm'
                : 'text-slate-600 hover:bg-slate-50'
            }`}
          >
            <Utensils className="w-4 h-4 shrink-0" />
            <span>Food Donor</span>
          </button>

          <button
            type="button"
            onClick={() => handleRoleChange('ROLE_RECEIVER')}
            className={`flex items-center justify-center space-x-2 py-3 px-3 rounded-xl text-xs font-bold transition ${
              role === 'ROLE_RECEIVER'
                ? 'bg-teal-600 text-white shadow-sm'
                : 'text-slate-600 hover:bg-slate-50'
            }`}
          >
            <Building2 className="w-4 h-4 shrink-0" />
            <span>Receiver NGO</span>
          </button>

          <button
            type="button"
            onClick={() => handleRoleChange('ROLE_VOLUNTEER')}
            className={`flex items-center justify-center space-x-2 py-3 px-3 rounded-xl text-xs font-bold transition ${
              role === 'ROLE_VOLUNTEER'
                ? 'bg-indigo-600 text-white shadow-sm'
                : 'text-slate-600 hover:bg-slate-50'
            }`}
          >
            <Truck className="w-4 h-4 shrink-0" />
            <span>Volunteer Courier</span>
          </button>
        </div>

        {/* Form Container */}
        <div className="bg-white p-8 rounded-3xl border border-slate-200 shadow-xl shadow-slate-200/40">
          {error && (
            <div className="mb-5 p-3.5 bg-rose-50 border border-rose-200 text-rose-700 rounded-xl text-xs font-semibold">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">Contact Name *</label>
                <div className="relative">
                  <UserIcon className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
                  <input
                    type="text"
                    required
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    placeholder="Full Name"
                    className="w-full pl-9 pr-3 py-2 text-xs bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">Email Address *</label>
                <div className="relative">
                  <Mail className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
                  <input
                    type="email"
                    required
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="name@organization.org"
                    className="w-full pl-9 pr-3 py-2 text-xs bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
                  />
                </div>
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">Password *</label>
                <div className="relative">
                  <Lock className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
                  <input
                    type="password"
                    required
                    minLength={6}
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="Min 6 characters"
                    className="w-full pl-9 pr-3 py-2 text-xs bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">Phone Number *</label>
                <div className="relative">
                  <Phone className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
                  <input
                    type="tel"
                    required
                    value={phone}
                    onChange={(e) => setPhone(e.target.value)}
                    placeholder="+91 98765 43210"
                    className="w-full pl-9 pr-3 py-2 text-xs bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
                  />
                </div>
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">
                  {role === 'ROLE_VOLUNTEER' ? 'Affiliation / Group (Optional)' : 'Organization / Brand Name'}
                </label>
                <input
                  type="text"
                  value={organizationName}
                  onChange={(e) => setOrganizationName(e.target.value)}
                  placeholder={
                    role === 'ROLE_DONOR'
                      ? 'e.g. Spice Garden Restaurant'
                      : role === 'ROLE_VOLUNTEER'
                      ? 'e.g. Indiranagar Relief Riders'
                      : 'e.g. Hope Care Foundation'
                  }
                  className="w-full px-3 py-2 text-xs bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">
                  {role === 'ROLE_VOLUNTEER' ? 'Dispatch Role' : 'Organization Category'}
                </label>
                <select
                  value={organizationType}
                  onChange={(e) => setOrganizationType(e.target.value)}
                  className="w-full px-3 py-2 text-xs bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
                >
                  {role === 'ROLE_DONOR' ? (
                    <>
                      <option value="Restaurant">Restaurant</option>
                      <option value="Hotel & Banquets">Hotel & Banquets</option>
                      <option value="Hostel & College Canteen">Hostel & College Canteen</option>
                      <option value="Institutional Kitchen">Institutional Kitchen</option>
                      <option value="Food Business">Food Manufacturer / Distributor</option>
                    </>
                  ) : role === 'ROLE_VOLUNTEER' ? (
                    <>
                      <option value="Volunteer Courier">Volunteer Courier</option>
                      <option value="Rescue Squad Member">Rescue Squad Member</option>
                      <option value="Civic Driver">Civic Driver</option>
                    </>
                  ) : (
                    <>
                      <option value="Shelter & Orphanage">Shelter & Orphanage</option>
                      <option value="Community Kitchen">Community Kitchen</option>
                      <option value="Charitable NGO">Charitable NGO</option>
                      <option value="Elderly Care Home">Elderly Care Home</option>
                      <option value="Disaster Relief Group">Disaster Relief Group</option>
                    </>
                  )}
                </select>
              </div>
            </div>

            {role === 'ROLE_RECEIVER' && (
              <div className="space-y-3 p-3.5 bg-teal-50/60 border border-teal-100 rounded-2xl">
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    NGO Registration / Trust Number (for Admin Verification)
                  </label>
                  <div className="relative">
                    <FileText className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
                    <input
                      type="text"
                      value={registrationNumber}
                      onChange={(e) => setRegistrationNumber(e.target.value)}
                      placeholder="e.g. NGO-BLR-2024-5541 or 12A/80G Number"
                      className="w-full pl-9 pr-3 py-2 text-xs bg-white border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
                    />
                  </div>
                </div>
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    Verification Document URLs (Optional, comma-separated)
                  </label>
                  <div className="relative">
                    <Link2 className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
                    <input
                      type="text"
                      value={verificationDocsInput}
                      onChange={(e) => setVerificationDocsInput(e.target.value)}
                      placeholder="https://example.org/trust-deed.pdf, https://example.org/80g.pdf"
                      className="w-full pl-9 pr-3 py-2 text-xs bg-white border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
                    />
                  </div>
                </div>
              </div>
            )}

            {role === 'ROLE_VOLUNTEER' && (
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 p-3.5 bg-indigo-50/60 border border-indigo-100 rounded-2xl">
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">Vehicle Type</label>
                  <select
                    value={vehicleType}
                    onChange={(e) => setVehicleType(e.target.value)}
                    className="w-full px-3 py-2 text-xs bg-white border border-slate-200 rounded-xl"
                  >
                    <option value="Two-Wheeler Scooter">Two-Wheeler Scooter</option>
                    <option value="Cargo Scooter / Van">Cargo Scooter / Van</option>
                    <option value="Hatchback / Car">Hatchback / Car</option>
                    <option value="Refrigerated Mini-Van">Refrigerated Mini-Van</option>
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">Max Capacity (kg)</label>
                  <input
                    type="number"
                    min={5}
                    max={500}
                    value={maxCapacityKg}
                    onChange={(e) => setMaxCapacityKg(Number(e.target.value) || 20)}
                    className="w-full px-3 py-2 text-xs bg-white border border-slate-200 rounded-xl"
                  />
                </div>
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">Service Radius (km)</label>
                  <input
                    type="number"
                    min={2}
                    max={100}
                    value={serviceRadiusKm}
                    onChange={(e) => setServiceRadiusKm(Number(e.target.value) || 15)}
                    className="w-full px-3 py-2 text-xs bg-white border border-slate-200 rounded-xl"
                  />
                </div>
              </div>
            )}

            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">Physical Address *</label>
              <div className="relative">
                <MapPin className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
                <input
                  type="text"
                  required
                  value={address}
                  onChange={(e) => setAddress(e.target.value)}
                  placeholder="Street, Area, City (e.g. 12 Brigade Road, Bangalore)"
                  className="w-full pl-9 pr-3 py-2 text-xs bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
                />
              </div>
            </div>

            {/* GPS Coordinates */}
            <div className="p-3 bg-slate-50 rounded-2xl border border-slate-200/80 space-y-2">
              <div className="flex items-center justify-between">
                <span className="text-[11px] font-bold text-slate-700">GPS Coordinates (Used for smart matching)</span>
                <button
                  type="button"
                  onClick={handleGetLocation}
                  className="text-[11px] text-emerald-600 hover:text-emerald-700 font-semibold flex items-center space-x-1"
                >
                  <Crosshair className="w-3 h-3" />
                  <span>Auto-detect GPS</span>
                </button>
              </div>
              <div className="grid grid-cols-2 gap-3 text-xs">
                <input
                  type="number"
                  step="0.0001"
                  value={latitude}
                  onChange={(e) => setLatitude(parseFloat(e.target.value))}
                  placeholder="Latitude"
                  className="px-3 py-1.5 bg-white border border-slate-200 rounded-lg text-xs"
                />
                <input
                  type="number"
                  step="0.0001"
                  value={longitude}
                  onChange={(e) => setLongitude(parseFloat(e.target.value))}
                  placeholder="Longitude"
                  className="px-3 py-1.5 bg-white border border-slate-200 rounded-lg text-xs"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-3 px-4 bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-700 hover:to-teal-700 text-white font-bold text-xs rounded-xl shadow-md shadow-emerald-500/20 transition disabled:opacity-50 flex items-center justify-center space-x-2"
            >
              {loading ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" />
                  <span>Creating Account...</span>
                </>
              ) : (
                <>
                  <span>Complete Registration</span>
                  <ArrowRight className="w-4 h-4" />
                </>
              )}
            </button>
          </form>

          <div className="mt-6 text-center text-xs text-slate-500">
            Already registered?{' '}
            <Link to="/login" className="font-bold text-emerald-600 hover:text-emerald-700">
              Sign In Here
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};
