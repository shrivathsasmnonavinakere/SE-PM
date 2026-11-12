import React from 'react';
import { DonationStatus, ClaimStatus } from '../types';

interface StatusBadgeProps {
  status: DonationStatus | ClaimStatus | string;
  className?: string;
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({ status, className = '' }) => {
  const getBadgeStyle = (st: string) => {
    switch (st) {
      case 'AVAILABLE':
      case 'ACTIVE':
        return 'bg-emerald-50 text-emerald-700 border-emerald-200 ring-emerald-500/20';
      case 'MATCHED':
        return 'bg-blue-50 text-blue-700 border-blue-200 ring-blue-500/20';
      case 'PARTIALLY_CLAIMED':
        return 'bg-cyan-50 text-cyan-800 border-cyan-200 ring-cyan-500/20';
      case 'CLAIMED':
      case 'PENDING':
      case 'REQUESTED':
        return 'bg-amber-50 text-amber-700 border-amber-200 ring-amber-500/20';
      case 'APPROVED':
      case 'PICKUP_SCHEDULED':
      case 'ASSIGNED':
        return 'bg-purple-50 text-purple-700 border-purple-200 ring-purple-500/20';
      case 'PICKED_UP':
      case 'VERIFIED_PICKUP':
        return 'bg-indigo-50 text-indigo-700 border-indigo-200 ring-indigo-500/20';
      case 'COMPLETED':
      case 'DELIVERED':
        return 'bg-teal-50 text-teal-800 border-teal-200 ring-teal-500/20';
      case 'AWAITING_DONOR_APPROVAL':
      case 'MISSED_DETECTED':
        return 'bg-orange-50 text-orange-800 border-orange-200 ring-orange-500/20';
      case 'EXPIRED':
      case 'REVOKED':
        return 'bg-rose-50 text-rose-700 border-rose-200 ring-rose-500/20';
      case 'REJECTED':
      case 'CANCELLED':
      case 'RELEASED':
        return 'bg-slate-100 text-slate-600 border-slate-200 ring-slate-500/10';
      default:
        return 'bg-slate-50 text-slate-700 border-slate-200';
    }
  };

  const formatText = (st: string) => {
    return st.replace(/_/g, ' ');
  };

  return (
    <span
      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold border ${getBadgeStyle(
        status
      )} ${className}`}
    >
      <span className="w-1.5 h-1.5 rounded-full mr-1.5 bg-current opacity-70"></span>
      {formatText(status)}
    </span>
  );
};
