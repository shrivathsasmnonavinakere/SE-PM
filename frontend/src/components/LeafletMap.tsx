import React, { useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup, Circle, useMap } from 'react-leaflet';
import L from 'leaflet';
import { Donation } from '../types';
import { StatusBadge } from './StatusBadge';
import { SafetyBadge } from './SafetyBadge';
import { MapPin, Clock, Package } from 'lucide-react';

interface LeafletMapProps {
  donations: Donation[];
  userLat?: number;
  userLon?: number;
  selectedDonationId?: string;
  onSelectDonation?: (donation: Donation) => void;
  onClaim?: (donation: Donation) => void;
  height?: string;
  radiusKm?: number;
}

// Custom Leaflet DivIcons to avoid broken default marker image assets
const createCustomIcon = (category: string, isSelected: boolean = false) => {
  return L.divIcon({
    className: 'custom-leaflet-marker',
    html: `
      <div style="
        background: ${isSelected ? '#047857' : '#10b981'};
        width: ${isSelected ? '36px' : '30px'};
        height: ${isSelected ? '36px' : '30px'};
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        box-shadow: 0 4px 10px rgba(0,0,0,0.25);
        border: 2px solid white;
        cursor: pointer;
        transition: transform 0.2s;
      ">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 17.93c-3.95-.49-7-3.85-7-7.93 0-.62.08-1.21.21-1.79L9 15v1c0 1.1.9 2 2 2v1.93zm6.9-2.54c-.26-.81-1-1.39-1.9-1.39h-1v-3c0-.55-.45-1-1-1H8v-2h2c.55 0 1-.45 1-1V7h2c1.1 0 2-.9 2-2v-.41c2.93 1.19 5 4.06 5 7.41 0 2.08-.8 3.97-2.1 5.39z"/>
        </svg>
      </div>
    `,
    iconSize: [30, 30],
    iconAnchor: [15, 15],
    popupAnchor: [0, -18],
  });
};

const userIcon = L.divIcon({
  className: 'user-location-marker',
  html: `
    <div style="
      background: #2563eb;
      width: 28px;
      height: 28px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      box-shadow: 0 0 0 4px rgba(37,99,235,0.25);
      border: 2px solid white;
    ">
      <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="10"/>
        <circle cx="12" cy="12" r="3"/>
      </svg>
    </div>
  `,
  iconSize: [28, 28],
  iconAnchor: [14, 14],
});

// Component to dynamically re-center map when coordinates change
const MapUpdater: React.FC<{ center: [number, number]; zoom: number }> = ({ center, zoom }) => {
  const map = useMap();
  useEffect(() => {
    map.setView(center, zoom);
  }, [center, zoom, map]);
  return null;
};

export const LeafletMap: React.FC<LeafletMapProps> = ({
  donations,
  userLat = 12.9716,
  userLon = 77.5946,
  selectedDonationId,
  onSelectDonation,
  onClaim,
  height = '500px',
  radiusKm,
}) => {
  const defaultCenter: [number, number] = [userLat, userLon];

  return (
    <div className="w-full rounded-2xl overflow-hidden border border-slate-200 shadow-sm relative" style={{ height }}>
      <MapContainer
        center={defaultCenter}
        zoom={13}
        scrollWheelZoom={true}
        style={{ height: '100%', width: '100%' }}
      >
        <MapUpdater center={defaultCenter} zoom={13} />

        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />

        {/* User Location Marker & Radius */}
        <Marker position={[userLat, userLon]} icon={userIcon}>
          <Popup>
            <div className="text-xs p-1">
              <span className="font-bold text-blue-600 block">Your Organization Location</span>
              <span>Coordinates: {userLat.toFixed(4)}, {userLon.toFixed(4)}</span>
            </div>
          </Popup>
        </Marker>

        {radiusKm && (
          <Circle
            center={[userLat, userLon]}
            radius={radiusKm * 1000}
            pathOptions={{ color: '#10b981', fillColor: '#10b981', fillOpacity: 0.08, weight: 1.5, dashArray: '4, 4' }}
          />
        )}

        {/* Donation Markers */}
        {donations.map((d) => {
          if (!d.latitude || !d.longitude) return null;
          const isSelected = d.id === selectedDonationId;

          return (
            <Marker
              key={d.id}
              position={[d.latitude, d.longitude]}
              icon={createCustomIcon(d.foodCategory, isSelected)}
              eventHandlers={{
                click: () => onSelectDonation && onSelectDonation(d),
              }}
            >
              <Popup>
                <div className="p-2 min-w-[220px] text-xs">
                  <div className="flex items-center justify-between mb-1.5">
                    <StatusBadge status={d.status} />
                    <SafetyBadge indicator={d.safetyIndicator} />
                  </div>

                  <h4 className="font-bold text-slate-900 text-sm">{d.foodName}</h4>
                  <p className="text-slate-500 text-[11px] font-medium">{d.donorName}</p>

                  <div className="mt-2 space-y-1 text-slate-600 border-t border-slate-100 pt-1.5">
                    <div className="flex items-center">
                      <Package className="w-3.5 h-3.5 text-slate-400 mr-1.5" />
                      <span>
                        Remaining: <strong className="text-slate-800">{(d.remainingQuantity ?? d.quantity).toFixed(1)} / {d.quantity} {d.measurementUnit}</strong>
                      </span>
                    </div>

                    <div className="flex items-center">
                      <Clock className="w-3.5 h-3.5 text-amber-500 mr-1.5" />
                      <span>Deadline: {new Date(d.pickupDeadline).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
                    </div>

                    {d.distanceKm !== undefined && (
                      <div className="flex items-center">
                        <MapPin className="w-3.5 h-3.5 text-emerald-600 mr-1.5" />
                        <span>Distance: <strong>{d.distanceKm.toFixed(1)} km away</strong></span>
                      </div>
                    )}
                  </div>

                  {onClaim &&
                    (d.status === 'AVAILABLE' || d.status === 'MATCHED' || d.status === 'PARTIALLY_CLAIMED') &&
                    (d.remainingQuantity ?? d.quantity) > 0 && (
                    <button
                      onClick={() => onClaim(d)}
                      className="mt-3 w-full py-1.5 bg-emerald-600 text-white rounded-lg font-medium hover:bg-emerald-700 transition text-center"
                    >
                      Claim Food Batch
                    </button>
                  )}
                </div>
              </Popup>
            </Marker>
          );
        })}
      </MapContainer>
    </div>
  );
};
