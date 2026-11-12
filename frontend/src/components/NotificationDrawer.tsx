import React from 'react';
import { NotificationItem } from '../types';
import { X, Bell, CheckCheck, Clock, ExternalLink } from 'lucide-react';

interface NotificationDrawerProps {
  isOpen: boolean;
  onClose: () => void;
  notifications: NotificationItem[];
  onMarkRead: (id: string) => void;
  onMarkAllRead: () => void;
}

export const NotificationDrawer: React.FC<NotificationDrawerProps> = ({
  isOpen,
  onClose,
  notifications,
  onMarkRead,
  onMarkAllRead,
}) => {
  if (!isOpen) return null;

  const formatTime = (isoString: string) => {
    try {
      const date = new Date(isoString);
      const diffMinutes = Math.floor((Date.now() - date.getTime()) / (1000 * 60));
      if (diffMinutes < 1) return 'Just now';
      if (diffMinutes < 60) return `${diffMinutes}m ago`;
      const diffHours = Math.floor(diffMinutes / 60);
      if (diffHours < 24) return `${diffHours}h ago`;
      return date.toLocaleDateString();
    } catch {
      return '';
    }
  };

  return (
    <div className="fixed inset-0 z-50 overflow-hidden">
      <div className="absolute inset-0 bg-slate-900/40 backdrop-blur-sm transition-opacity" onClick={onClose} />

      <div className="fixed inset-y-0 right-0 max-w-full flex pl-10">
        <div className="w-screen max-w-md bg-white shadow-2xl flex flex-col">
          {/* Header */}
          <div className="p-4 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
            <div className="flex items-center space-x-2">
              <div className="p-2 bg-emerald-100 text-emerald-700 rounded-lg">
                <Bell className="w-5 h-5" />
              </div>
              <div>
                <h3 className="font-semibold text-slate-800">Notifications</h3>
                <p className="text-xs text-slate-500">Live redistribution alerts & updates</p>
              </div>
            </div>
            <div className="flex items-center space-x-2">
              <button
                onClick={onMarkAllRead}
                title="Mark all as read"
                className="text-xs text-emerald-600 hover:text-emerald-700 font-medium px-2 py-1 rounded hover:bg-emerald-50 transition"
              >
                <CheckCheck className="w-4 h-4 inline mr-1" />
                Read all
              </button>
              <button
                onClick={onClose}
                className="p-1 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition"
              >
                <X className="w-5 h-5" />
              </button>
            </div>
          </div>

          {/* List */}
          <div className="flex-1 overflow-y-auto p-4 space-y-3">
            {notifications.length === 0 ? (
              <div className="text-center py-16">
                <div className="w-12 h-12 bg-slate-100 text-slate-400 rounded-full flex items-center justify-center mx-auto mb-3">
                  <Bell className="w-6 h-6" />
                </div>
                <h4 className="text-sm font-medium text-slate-700">No notifications yet</h4>
                <p className="text-xs text-slate-400 mt-1">You will be alerted when matching donations or claims occur.</p>
              </div>
            ) : (
              notifications.map((n) => (
                <div
                  key={n.id}
                  onClick={() => !n.isRead && onMarkRead(n.id)}
                  className={`p-3.5 rounded-xl border transition cursor-pointer ${
                    n.isRead
                      ? 'bg-white border-slate-100 text-slate-600 opacity-80'
                      : 'bg-emerald-50/40 border-emerald-100 text-slate-800 shadow-sm'
                  }`}
                >
                  <div className="flex items-start justify-between">
                    <h4 className="text-xs font-semibold text-slate-900">{n.title}</h4>
                    {!n.isRead && (
                      <span className="w-2 h-2 rounded-full bg-emerald-500 ring-4 ring-emerald-100" />
                    )}
                  </div>
                  <p className="text-xs text-slate-600 mt-1 leading-relaxed">{n.message}</p>
                  <div className="flex items-center justify-between mt-2 pt-2 border-t border-slate-100/60 text-[11px] text-slate-400">
                    <span className="inline-flex items-center">
                      <Clock className="w-3 h-3 mr-1" />
                      {formatTime(n.createdAt)}
                    </span>
                    <span className="text-[10px] uppercase font-mono px-1.5 py-0.5 rounded bg-slate-100 text-slate-600">
                      {n.type.replace(/_/g, ' ')}
                    </span>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
