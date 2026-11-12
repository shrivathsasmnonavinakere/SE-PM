import React, { useState, useEffect } from 'react';
import { heatmapApi } from '../services/api';
import { DemandHeatmap, HeatmapZone, FoodCategory } from '../types';
import { MapContainer, TileLayer, Circle, Popup, CircleMarker } from 'react-leaflet';
import { ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Tooltip, Cell } from 'recharts';
import { Flame, Sparkles, MapPin, TrendingUp, AlertTriangle, Loader2, Layers } from 'lucide-react';

const CATEGORIES: string[] = [
  'ALL',
  'Cooked food',
  'Rice and grains',
  'Vegetables',
  'Fruits',
  'Bread and bakery',
  'Packaged food',
  'Dairy products',
  'Other',
];

export const DemandHeatmapPage: React.FC = () => {
  const [heatmap, setHeatmap] = useState<DemandHeatmap | null>(null);
  const [loading, setLoading] = useState(true);
  const [timeRange, setTimeRange] = useState<string>('all');
  const [category, setCategory] = useState<string>('ALL');
  const [mode, setMode] = useState<'DEMAND' | 'SURPLUS' | 'GAP'>('GAP');

  useEffect(() => {
    loadHeatmap();
  }, [timeRange, category]);

  const loadHeatmap = async () => {
    setLoading(true);
    try {
      const catParam = category === 'ALL' ? undefined : (category as FoodCategory);
      const res = await heatmapApi.getDemandHeatmap(timeRange, catParam);
      if (res.success && res.data) {
        setHeatmap(res.data);
      }
    } catch (err) {
      console.error('Failed to load AI Demand Heatmap:', err);
    } finally {
      setLoading(false);
    }
  };

  const getZoneColor = (z: HeatmapZone) => {
    if (mode === 'SURPLUS') return '#10b981'; // emerald
    if (mode === 'DEMAND') return '#f59e0b';  // amber
    return z.deficitKg > 0 ? '#e11d48' : '#0284c7'; // rose for deficit, sky for surplus balance
  };

  const getZoneIntensity = (z: HeatmapZone) => {
    if (mode === 'SURPLUS') return Math.max(0.25, z.surplusIntensity);
    if (mode === 'DEMAND') return Math.max(0.25, z.demandIntensity);
    return Math.max(0.25, Math.max(z.gapIntensity, z.surplusIntensity));
  };

  const getZoneRadius = (z: HeatmapZone) => {
    const val = mode === 'SURPLUS' ? z.totalSurplusKg : mode === 'DEMAND' ? z.totalDemandKg : Math.max(z.deficitKg, z.totalSurplusKg);
    return Math.max(600, Math.min(2800, val * 35));
  };

  const chartData = heatmap?.demandVsSurplusByCategory
    ? Object.entries(heatmap.demandVsSurplusByCategory).map(([name, netKg]) => ({
        name,
        netKg,
      }))
    : [];

  const defaultCenter: [number, number] = heatmap?.zones?.length
    ? [heatmap.zones[0].centerLat, heatmap.zones[0].centerLon]
    : [12.9716, 77.5946];

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-8">
      {/* Header */}
      <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-4 border-b border-slate-200 pb-6">
        <div>
          <div className="flex items-center space-x-2">
            <span className="p-2 bg-amber-100 text-amber-700 rounded-xl">
              <Flame className="w-5 h-5" />
            </span>
            <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
              AI Food Demand & Surplus Heatmap
            </h1>
          </div>
          <p className="text-xs sm:text-sm text-slate-500 mt-1">
            Spatial intelligence aggregated from real MongoDB donations, unmet NGO demands, and expirations.
          </p>
        </div>

        {/* Filter Controls */}
        <div className="flex flex-wrap items-center gap-2 text-xs">
          <select
            value={timeRange}
            onChange={(e) => setTimeRange(e.target.value)}
            className="px-3 py-2 bg-white border border-slate-200 rounded-xl font-semibold text-slate-700"
          >
            <option value="all">All Historical Records</option>
            <option value="30d">Last 30 Days</option>
            <option value="7d">Last 7 Days</option>
          </select>

          <select
            value={category}
            onChange={(e) => setCategory(e.target.value)}
            className="px-3 py-2 bg-white border border-slate-200 rounded-xl font-semibold text-slate-700"
          >
            {CATEGORIES.map((cat) => (
              <option key={cat} value={cat}>
                {cat === 'ALL' ? 'All Food Categories' : cat}
              </option>
            ))}
          </select>
        </div>
      </div>

      {loading ? (
        <div className="min-h-[50vh] flex items-center justify-center">
          <Loader2 className="w-8 h-8 animate-spin text-emerald-600" />
        </div>
      ) : heatmap ? (
        <>
          {/* AI Spatial Briefing */}
          <div className="p-6 bg-gradient-to-br from-slate-900 via-emerald-950 to-slate-900 text-white rounded-3xl shadow-lg space-y-2">
            <div className="flex items-center space-x-2 text-emerald-400 text-xs font-bold uppercase tracking-wider">
              <Sparkles className="w-4 h-4" />
              <span>Gemini Spatial Redistribution Insight</span>
            </div>
            <p className="text-sm leading-relaxed text-slate-100">{heatmap.aiSpatialInsight}</p>
          </div>

          {/* KPI Summary Bar */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div className="p-5 bg-white rounded-3xl border border-slate-200 shadow-sm space-y-1">
              <span className="text-xs font-bold text-slate-400 uppercase">Historical Surplus</span>
              <div className="text-2xl font-extrabold text-emerald-600">{heatmap.totalHistoricalSurplusKg.toFixed(1)} kg</div>
              <p className="text-[11px] text-slate-500">Top hub: {heatmap.highestSurplusZone}</p>
            </div>
            <div className="p-5 bg-white rounded-3xl border border-slate-200 shadow-sm space-y-1">
              <span className="text-xs font-bold text-slate-400 uppercase">Active NGO Demand</span>
              <div className="text-2xl font-extrabold text-amber-600">{heatmap.totalActiveDemandKg.toFixed(1)} kg</div>
              <p className="text-[11px] text-slate-500">Top deficit: {heatmap.highestDemandZone}</p>
            </div>
            <div className="p-5 bg-white rounded-3xl border border-slate-200 shadow-sm space-y-1">
              <span className="text-xs font-bold text-slate-400 uppercase">Fulfilled Volume</span>
              <div className="text-2xl font-extrabold text-teal-600">{heatmap.totalFulfilledKg.toFixed(1)} kg</div>
              <p className="text-[11px] text-slate-500">Peak hours: {heatmap.peakSurplusHours}</p>
            </div>
            <div className="p-5 bg-white rounded-3xl border border-slate-200 shadow-sm space-y-1">
              <span className="text-xs font-bold text-slate-400 uppercase">Expired Bottleneck</span>
              <div className="text-2xl font-extrabold text-rose-600">{heatmap.totalExpiredKg.toFixed(1)} kg</div>
              <p className="text-[11px] text-slate-500">{heatmap.zones.length} clustered urban zones</p>
            </div>
          </div>

          {/* Mode Toggle + Interactive Leaflet Spatial Heatmap */}
          <div className="bg-white p-6 rounded-3xl border border-slate-200 shadow-sm space-y-4">
            <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
              <div>
                <h3 className="font-bold text-slate-900 text-sm flex items-center space-x-2">
                  <Layers className="w-4 h-4 text-emerald-600" />
                  <span>Interactive Geospatial Intensity Overlay</span>
                </h3>
                <p className="text-xs text-slate-500">Click any heat zone marker to inspect exact surplus, demand, and action recommendations.</p>
              </div>

              <div className="flex items-center bg-slate-100 p-1 rounded-2xl border border-slate-200 text-xs">
                {[
                  { key: 'GAP', label: 'Deficit Gap' },
                  { key: 'DEMAND', label: 'NGO Demand' },
                  { key: 'SURPLUS', label: 'Food Surplus' },
                ].map((tab) => (
                  <button
                    key={tab.key}
                    type="button"
                    onClick={() => setMode(tab.key as any)}
                    className={`px-3 py-1.5 rounded-xl font-bold transition ${
                      mode === tab.key ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-600 hover:text-slate-900'
                    }`}
                  >
                    {tab.label}
                  </button>
                ))}
              </div>
            </div>

            <div className="h-[460px] w-full rounded-2xl overflow-hidden border border-slate-200">
              <MapContainer center={defaultCenter} zoom={12} style={{ height: '100%', width: '100%' }}>
                <TileLayer
                  attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
                  url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                />
                {heatmap.zones.map((zone, i) => {
                  const color = getZoneColor(zone);
                  const opacity = getZoneIntensity(zone);
                  const radius = getZoneRadius(zone);
                  return (
                    <React.Fragment key={i}>
                      <Circle
                        center={[zone.centerLat, zone.centerLon]}
                        radius={radius}
                        pathOptions={{
                          color,
                          fillColor: color,
                          fillOpacity: Math.min(0.55, opacity * 0.65),
                          weight: 2,
                        }}
                      />
                      <CircleMarker
                        center={[zone.centerLat, zone.centerLon]}
                        radius={9}
                        pathOptions={{ color: '#ffffff', fillColor: color, fillOpacity: 1, weight: 2 }}
                      >
                        <Popup>
                          <div className="text-xs p-1 space-y-1.5 min-w-[210px]">
                            <h4 className="font-extrabold text-slate-900 text-sm">{zone.zoneName}</h4>
                            <div className="grid grid-cols-2 gap-1 text-[11px]">
                              <span className="text-slate-500">Surplus:</span>
                              <strong className="text-emerald-700">{zone.totalSurplusKg} kg</strong>
                              <span className="text-slate-500">NGO Demand:</span>
                              <strong className="text-amber-700">{zone.totalDemandKg} kg</strong>
                              <span className="text-slate-500">Fulfilled:</span>
                              <strong className="text-teal-700">{zone.fulfilledKg} kg</strong>
                              <span className="text-slate-500">Expired:</span>
                              <strong className="text-rose-700">{zone.expiredKg} kg</strong>
                            </div>
                            <p className="text-[11px] bg-slate-50 p-2 rounded-lg text-slate-700 border border-slate-100">
                              {zone.recommendedAction}
                            </p>
                          </div>
                        </Popup>
                      </CircleMarker>
                    </React.Fragment>
                  );
                })}
              </MapContainer>
            </div>
          </div>

          {/* Chart & Zone Intelligence Table */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            <div className="bg-white p-6 rounded-3xl border border-slate-200 shadow-sm space-y-4">
              <div className="flex items-center space-x-2">
                <TrendingUp className="w-4 h-4 text-emerald-600" />
                <h3 className="font-bold text-slate-900 text-sm">Net Category Balance (Surplus minus Demand kg)</h3>
              </div>
              {chartData.length > 0 ? (
                <div className="h-64 w-full">
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={chartData}>
                      <XAxis dataKey="name" tick={{ fontSize: 11 }} />
                      <YAxis tick={{ fontSize: 11 }} />
                      <Tooltip />
                      <Bar dataKey="netKg" radius={[6, 6, 0, 0]}>
                        {chartData.map((entry, idx) => (
                          <Cell key={idx} fill={entry.netKg >= 0 ? '#10b981' : '#f43f5e'} />
                        ))}
                      </Bar>
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              ) : (
                <p className="text-xs text-slate-400 py-10 text-center">No category balance metrics recorded yet.</p>
              )}
            </div>

            <div className="bg-white p-6 rounded-3xl border border-slate-200 shadow-sm space-y-4">
              <div className="flex items-center space-x-2">
                <MapPin className="w-4 h-4 text-amber-600" />
                <h3 className="font-bold text-slate-900 text-sm">Clustered Urban Zones ({heatmap.zones.length})</h3>
              </div>
              <div className="space-y-3 max-h-64 overflow-y-auto pr-1">
                {heatmap.zones.map((z, idx) => (
                  <div key={idx} className="p-3.5 bg-slate-50 rounded-2xl border border-slate-100 text-xs space-y-1">
                    <div className="flex items-center justify-between">
                      <strong className="text-slate-900 text-sm">{z.zoneName}</strong>
                      {z.deficitKg > 0 ? (
                        <span className="px-2 py-0.5 bg-rose-100 text-rose-800 rounded-full text-[10px] font-bold">
                          Deficit: {z.deficitKg} kg
                        </span>
                      ) : (
                        <span className="px-2 py-0.5 bg-emerald-100 text-emerald-800 rounded-full text-[10px] font-bold">
                          Surplus Hub
                        </span>
                      )}
                    </div>
                    <p className="text-slate-500">
                      Surplus: <strong>{z.totalSurplusKg} kg</strong> ({z.donationCount} donations) &bull; Demand:{' '}
                      <strong>{z.totalDemandKg} kg</strong> ({z.needCount} needs)
                    </p>
                    <p className="text-[11px] text-emerald-800 font-medium">{z.recommendedAction}</p>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </>
      ) : null}
    </div>
  );
};

