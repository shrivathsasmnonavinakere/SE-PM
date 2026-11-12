import React from 'react';
import { SafetyIndicator } from '../types';
import { ShieldCheck, AlertTriangle, AlertCircle, Clock } from 'lucide-react';

interface SafetyBadgeProps {
  indicator: SafetyIndicator;
  notes?: string;
  className?: string;
}

export const SafetyBadge: React.FC<SafetyBadgeProps> = ({ indicator, notes, className = '' }) => {
  const getStyle = () => {
    switch (indicator) {
      case 'LOW_RISK':
        return {
          bg: 'bg-emerald-50 text-emerald-800 border-emerald-200',
          icon: <ShieldCheck className="w-3.5 h-3.5 text-emerald-600 mr-1" />,
          label: 'Low Risk',
        };
      case 'REVIEW_REQUIRED':
        return {
          bg: 'bg-amber-50 text-amber-800 border-amber-200',
          icon: <AlertCircle className="w-3.5 h-3.5 text-amber-600 mr-1" />,
          label: 'Review Required',
        };
      case 'HIGH_RISK':
        return {
          bg: 'bg-rose-50 text-rose-800 border-rose-200',
          icon: <AlertTriangle className="w-3.5 h-3.5 text-rose-600 mr-1" />,
          label: 'High Risk',
        };
      case 'EXPIRED':
        return {
          bg: 'bg-slate-100 text-slate-700 border-slate-300',
          icon: <Clock className="w-3.5 h-3.5 text-slate-500 mr-1" />,
          label: 'Expired',
        };
      default:
        return {
          bg: 'bg-slate-50 text-slate-700 border-slate-200',
          icon: <ShieldCheck className="w-3.5 h-3.5 text-slate-500 mr-1" />,
          label: 'Screened',
        };
    }
  };

  const style = getStyle();

  return (
    <span
      title={notes || 'AI Food Safety Screening Indicator'}
      className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium border ${style.bg} ${className}`}
    >
      {style.icon}
      {style.label}
    </span>
  );
};
