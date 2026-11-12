import React, { useState } from 'react';
import { Mail, Phone, MapPin, Send, CheckCircle2 } from 'lucide-react';

export const ContactPage: React.FC = () => {
  const [submitted, setSubmitted] = useState(false);
  const [form, setForm] = useState({ name: '', email: '', subject: '', message: '' });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitted(true);
  };

  return (
    <div className="bg-white py-16">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 space-y-12">
        <div className="text-center space-y-3">
          <span className="text-xs font-bold text-emerald-600 uppercase tracking-wider">Get in Touch</span>
          <h1 className="text-3xl sm:text-4xl font-extrabold text-slate-900">Contact AnnaDhata Support</h1>
          <p className="text-xs sm:text-sm text-slate-600 max-w-xl mx-auto">
            Have questions about integrating your kitchen, verifying your charity, or setting up automated food rescue? We are here to help.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div className="p-6 rounded-2xl bg-slate-50 border border-slate-200 space-y-4">
            <h3 className="font-bold text-slate-800 text-sm">Direct Contacts</h3>
            <div className="space-y-3 text-xs text-slate-600">
              <div className="flex items-center space-x-2.5">
                <Mail className="w-4 h-4 text-emerald-600" />
                <span>support@annadhata.org</span>
              </div>
              <div className="flex items-center space-x-2.5">
                <Phone className="w-4 h-4 text-emerald-600" />
                <span>+91 (080) 2558-8844</span>
              </div>
              <div className="flex items-center space-x-2.5">
                <MapPin className="w-4 h-4 text-emerald-600" />
                <span>MG Road, Bangalore, Karnataka</span>
              </div>
            </div>

            <div className="pt-4 border-t border-slate-200">
              <span className="text-[11px] font-bold text-slate-700 block mb-1">Operational Hours</span>
              <p className="text-xs text-slate-500">24/7 automated matching engine & emergency donation dispatch.</p>
            </div>
          </div>

          <div className="md:col-span-2 bg-white p-8 rounded-3xl border border-slate-200 shadow-sm">
            {submitted ? (
              <div className="text-center py-12 space-y-3">
                <div className="w-12 h-12 bg-emerald-100 text-emerald-600 rounded-full flex items-center justify-center mx-auto">
                  <CheckCircle2 className="w-6 h-6" />
                </div>
                <h3 className="text-lg font-bold text-slate-900">Message Received!</h3>
                <p className="text-xs text-slate-500 max-w-md mx-auto">
                  Thank you for reaching out. An AnnaDhata support coordinator will respond to your inquiry shortly.
                </p>
                <button
                  onClick={() => setSubmitted(false)}
                  className="mt-4 text-xs font-semibold text-emerald-600 hover:text-emerald-700"
                >
                  Send another inquiry &rarr;
                </button>
              </div>
            ) : (
              <form onSubmit={handleSubmit} className="space-y-4 text-xs">
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div>
                    <label className="block font-bold text-slate-700 mb-1">Your Name *</label>
                    <input
                      type="text"
                      required
                      value={form.name}
                      onChange={(e) => setForm({ ...form, name: e.target.value })}
                      className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-emerald-500"
                      placeholder="e.g. Ramesh Patel"
                    />
                  </div>
                  <div>
                    <label className="block font-bold text-slate-700 mb-1">Email Address *</label>
                    <input
                      type="email"
                      required
                      value={form.email}
                      onChange={(e) => setForm({ ...form, email: e.target.value })}
                      className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-emerald-500"
                      placeholder="e.g. ramesh@kitchen.com"
                    />
                  </div>
                </div>

                <div>
                  <label className="block font-bold text-slate-700 mb-1">Subject</label>
                  <input
                    type="text"
                    required
                    value={form.subject}
                    onChange={(e) => setForm({ ...form, subject: e.target.value })}
                    className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-emerald-500"
                    placeholder="e.g. Partnering as institutional food donor"
                  />
                </div>

                <div>
                  <label className="block font-bold text-slate-700 mb-1">Message *</label>
                  <textarea
                    rows={4}
                    required
                    value={form.message}
                    onChange={(e) => setForm({ ...form, message: e.target.value })}
                    className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-emerald-500"
                    placeholder="How can our redistribution network assist your team?"
                  />
                </div>

                <button
                  type="submit"
                  className="w-full py-2.5 px-4 bg-emerald-600 hover:bg-emerald-700 text-white font-bold rounded-xl transition flex items-center justify-center space-x-2"
                >
                  <Send className="w-4 h-4" />
                  <span>Send Message</span>
                </button>
              </form>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
