import React, { useState, useEffect } from 'react';
import { adminApi } from '../services/api';
import { User } from '../types';
import {
  Building2,
  CheckCircle,
  XCircle,
  FileText,
  Phone,
  MapPin,
  ExternalLink,
  ShieldCheck,
  Loader2,
} from 'lucide-react';

export const OrganisationVerificationPage: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [actionId, setActionId] = useState<string | null>(null);
  const [reviewNotes, setReviewNotes] = useState<{ [id: string]: string }>({});

  useEffect(() => {
    loadUsers();
  }, []);

  const loadUsers = async () => {
    try {
      const res = await adminApi.getUsers();
      if (res.success && res.data) {
        // Filter receivers
        setUsers(res.data.filter((u) => u.role === 'ROLE_RECEIVER'));
      }
    } catch (err) {
      console.error('Failed to load users for verification:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleVerify = async (userId: string, approve: boolean) => {
    setActionId(userId);
    try {
      const notes = reviewNotes[userId] || (approve ? 'Documentation verified by Administrator.' : 'Insufficient documentation.');
      await adminApi.verifyOrg(userId, approve, notes);
      await loadUsers();
      alert(`Organisation ${approve ? 'Approved' : 'Rejected'} successfully!`);
    } catch (err: any) {
      alert('Verification update failed.');
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

  const pending = users.filter((u) => u.verificationStatus === 'PENDING');
  const processed = users.filter((u) => u.verificationStatus !== 'PENDING');

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-8">
      {/* Title */}
      <div className="border-b border-slate-200 pb-5">
        <div className="flex items-center space-x-2">
          <span className="p-2 bg-purple-100 text-purple-700 rounded-xl">
            <Building2 className="w-5 h-5" />
          </span>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
            Charity & NGO Verification Queue
          </h1>
        </div>
        <p className="text-xs sm:text-sm text-slate-500 mt-1">
          Review charitable trust numbers, NGO registration credentials, and approve organizations before they claim food donations.
        </p>
      </div>

      {/* Pending Queue */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h3 className="font-bold text-slate-900 text-sm">
            Pending Approval ({pending.length})
          </h3>
          <span className="text-xs text-amber-700 bg-amber-50 px-2.5 py-0.5 rounded-full font-semibold">
            Action Required
          </span>
        </div>

        {pending.length > 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
            {pending.map((u) => (
              <div key={u.id} className="p-6 bg-white rounded-3xl border border-amber-200 shadow-sm space-y-4 flex flex-col justify-between">
                <div className="space-y-3 text-xs">
                  <div className="flex items-start justify-between">
                    <div>
                      <h4 className="font-bold text-slate-900 text-base">{u.organizationName || u.name}</h4>
                      <p className="text-slate-500">{u.organizationType}</p>
                    </div>
                    <span className="px-2 py-0.5 rounded-full bg-amber-100 text-amber-800 text-[10px] font-bold">
                      PENDING
                    </span>
                  </div>

                  <div className="p-4 bg-slate-50 rounded-2xl border border-slate-100 space-y-1.5 text-slate-600">
                    <p><strong>Contact:</strong> {u.name} ({u.email})</p>
                    <p><strong>Phone:</strong> {u.phone || 'N/A'}</p>
                    <p><strong>Address:</strong> {u.address}</p>
                    <p><strong>Registration / Trust No:</strong> <span className="font-mono text-emerald-800 font-bold">{u.registrationNumber || 'Not provided'}</span></p>
                  </div>

                  {u.verificationDocuments && u.verificationDocuments.length > 0 && (
                    <div className="space-y-1">
                      <span className="text-[11px] font-bold text-slate-500">Submitted Verification Proof:</span>
                      <div className="flex flex-wrap gap-2">
                        {u.verificationDocuments.map((doc, idx) => (
                          <a
                            key={idx}
                            href={doc}
                            target="_blank"
                            rel="noreferrer"
                            className="inline-flex items-center space-x-1 px-2.5 py-1 bg-slate-100 hover:bg-slate-200 rounded-lg text-emerald-700 font-medium"
                          >
                            <FileText className="w-3.5 h-3.5" />
                            <span>Document {idx + 1}</span>
                            <ExternalLink className="w-3 h-3" />
                          </a>
                        ))}
                      </div>
                    </div>
                  )}

                  <div>
                    <label className="block text-[11px] font-bold text-slate-600 mb-1">Admin Review Notes</label>
                    <input
                      type="text"
                      placeholder="e.g. 80G certificate verified with NGO Darpan portal."
                      value={reviewNotes[u.id] || ''}
                      onChange={(e) => setReviewNotes({ ...reviewNotes, [u.id]: e.target.value })}
                      className="w-full px-3 py-1.5 bg-slate-50 border border-slate-200 rounded-xl text-xs"
                    />
                  </div>
                </div>

                <div className="flex items-center space-x-2 pt-2 border-t border-slate-100">
                  <button
                    onClick={() => handleVerify(u.id, true)}
                    disabled={actionId === u.id}
                    className="flex-1 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-bold transition flex items-center justify-center space-x-1.5 disabled:opacity-50"
                  >
                    {actionId === u.id ? <Loader2 className="w-3.5 h-3.5 animate-spin" /> : <CheckCircle className="w-4 h-4" />}
                    <span>Approve Organisation</span>
                  </button>
                  <button
                    onClick={() => handleVerify(u.id, false)}
                    disabled={actionId === u.id}
                    className="px-4 py-2 bg-slate-100 hover:bg-rose-50 text-slate-600 hover:text-rose-700 rounded-xl text-xs font-semibold transition"
                  >
                    Reject
                  </button>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="text-center py-10 bg-white rounded-3xl border border-slate-200 p-6 text-xs text-slate-400">
            No pending verification requests in the queue.
          </div>
        )}
      </div>

      {/* Processed Organizations */}
      <div className="space-y-4 pt-4">
        <h3 className="font-bold text-slate-900 text-sm">
          Verified Organizations ({processed.length})
        </h3>

        <div className="bg-white rounded-3xl border border-slate-200 overflow-hidden shadow-sm">
          <div className="divide-y divide-slate-100">
            {processed.map((u) => (
              <div key={u.id} className="p-4 sm:p-5 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 text-xs">
                <div>
                  <div className="flex items-center space-x-2">
                    <h4 className="font-bold text-slate-900 text-sm">{u.organizationName || u.name}</h4>
                    <span
                      className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                        u.verificationStatus === 'VERIFIED'
                          ? 'bg-emerald-100 text-emerald-800'
                          : 'bg-rose-100 text-rose-800'
                      }`}
                    >
                      {u.verificationStatus}
                    </span>
                  </div>
                  <p className="text-slate-500 mt-0.5">
                    {u.email} &bull; Reg No: <strong className="font-mono">{u.registrationNumber || 'N/A'}</strong> &bull; {u.address}
                  </p>
                </div>

                <div className="flex items-center space-x-2">
                  <button
                    onClick={() => handleVerify(u.id, u.verificationStatus !== 'VERIFIED')}
                    className="px-3 py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold rounded-xl transition"
                  >
                    {u.verificationStatus === 'VERIFIED' ? 'Revoke Status' : 'Re-Approve'}
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};
