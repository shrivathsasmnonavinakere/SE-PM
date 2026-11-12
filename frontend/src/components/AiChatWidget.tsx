import React, { useState, useEffect, useRef } from 'react';
import { aiApi } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { Sparkles, Send, X, Bot, User as UserIcon, Loader2 } from 'lucide-react';

interface ChatMessage {
  id: string;
  sender: 'user' | 'assistant';
  text: string;
  timestamp: string;
  actions?: string[];
}

export const AiChatWidget: React.FC = () => {
  const { user, isAuthenticated } = useAuth();
  const [isOpen, setIsOpen] = useState(false);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const initialQuickPrompts = [
    'How do I donate 25 kg of surplus biryani?',
    'Find food donations near me',
    'How does AnnaDhata AI smart matching work?',
    'How much food have I redistributed?',
  ];

  useEffect(() => {
    if (isOpen && messages.length === 0) {
      const greeting = user
        ? `Namaste ${user.name}! I am AnnaDhata AI Assistant. How can I help you optimize surplus food donation or redistribution today?`
        : 'Welcome to AnnaDhata! I am your AI assistant for reducing food waste and connecting community meals. Ask me anything!';

      setMessages([
        {
          id: 'welcome',
          sender: 'assistant',
          text: greeting,
          timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        },
      ]);
    }
  }, [isOpen, user]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSendMessage = async (textToSend?: string) => {
    const text = (textToSend || input).trim();
    if (!text || loading) return;

    const userMsg: ChatMessage = {
      id: Date.now().toString(),
      sender: 'user',
      text,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    };

    setMessages((prev) => [...prev, userMsg]);
    setInput('');
    setLoading(true);

    try {
      const res = isAuthenticated ? await aiApi.chat(text) : await aiApi.publicChat(text);
      if (res.success && res.data) {
        const botMsg: ChatMessage = {
          id: (Date.now() + 1).toString(),
          sender: 'assistant',
          text: res.data.reply,
          timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
          actions: res.data.relevantActions,
        };
        setMessages((prev) => [...prev, botMsg]);
      } else {
        throw new Error(res.message);
      }
    } catch (err: any) {
      const errorMsg: ChatMessage = {
        id: (Date.now() + 1).toString(),
        sender: 'assistant',
        text: 'Sorry, I encountered an issue processing your request. Please try again or check your connection.',
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      };
      setMessages((prev) => [...prev, errorMsg]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      {/* Floating Launcher Button */}
      <button
        onClick={() => setIsOpen((prev) => !prev)}
        className="fixed bottom-6 right-6 z-40 bg-gradient-to-r from-emerald-600 to-teal-600 text-white p-3.5 rounded-full shadow-lg hover:shadow-emerald-500/30 hover:scale-105 active:scale-95 transition-all duration-200 flex items-center justify-center group"
        title="Chat with AnnaDhata AI"
      >
        <Sparkles className="w-6 h-6 animate-pulse" />
        <span className="max-w-0 overflow-hidden whitespace-nowrap group-hover:max-w-xs transition-all duration-300 ease-in-out pl-0 group-hover:pl-2 text-sm font-medium">
          Ask AI Assistant
        </span>
      </button>

      {/* Chat Window Modal */}
      {isOpen && (
        <div className="fixed bottom-20 right-6 z-50 w-96 max-w-[calc(100vw-2rem)] h-[540px] bg-white rounded-2xl shadow-2xl border border-slate-200 flex flex-col overflow-hidden animate-in fade-in slide-in-from-bottom-5 duration-200">
          {/* Header */}
          <div className="p-3.5 bg-gradient-to-r from-emerald-600 to-teal-600 text-white flex items-center justify-between">
            <div className="flex items-center space-x-2">
              <div className="p-1.5 bg-white/20 rounded-lg">
                <Bot className="w-5 h-5 text-emerald-100" />
              </div>
              <div>
                <h3 className="text-sm font-semibold flex items-center">
                  AnnaDhata AI Assistant
                  <span className="ml-2 w-2 h-2 rounded-full bg-emerald-300 animate-ping" />
                </h3>
                <p className="text-[11px] text-emerald-100 font-light">Powered by Google Gemini</p>
              </div>
            </div>
            <button
              onClick={() => setIsOpen(false)}
              className="p-1 rounded-lg text-white/80 hover:text-white hover:bg-white/10 transition"
            >
              <X className="w-5 h-5" />
            </button>
          </div>

          {/* Messages Container */}
          <div className="flex-1 overflow-y-auto p-3.5 space-y-3 bg-slate-50/60">
            {messages.map((m) => (
              <div
                key={m.id}
                className={`flex items-start space-x-2 ${
                  m.sender === 'user' ? 'flex-row-reverse space-x-reverse' : 'flex-row'
                }`}
              >
                <div
                  className={`w-7 h-7 rounded-full flex items-center justify-center flex-shrink-0 text-xs ${
                    m.sender === 'user' ? 'bg-emerald-600 text-white' : 'bg-teal-100 text-teal-800'
                  }`}
                >
                  {m.sender === 'user' ? <UserIcon className="w-4 h-4" /> : <Bot className="w-4 h-4" />}
                </div>
                <div
                  className={`max-w-[80%] rounded-2xl px-3.5 py-2.5 text-xs leading-relaxed shadow-sm ${
                    m.sender === 'user'
                      ? 'bg-emerald-600 text-white rounded-tr-none'
                      : 'bg-white text-slate-800 border border-slate-100 rounded-tl-none'
                  }`}
                >
                  <p className="whitespace-pre-wrap">{m.text}</p>
                  <div
                    className={`text-[10px] mt-1 text-right ${
                      m.sender === 'user' ? 'text-emerald-100' : 'text-slate-400'
                    }`}
                  >
                    {m.timestamp}
                  </div>

                  {m.actions && m.actions.length > 0 && (
                    <div className="mt-2 pt-2 border-t border-slate-100 flex flex-wrap gap-1">
                      {m.actions.map((act, idx) => (
                        <button
                          key={idx}
                          onClick={() => handleSendMessage(`Tell me about: ${act}`)}
                          className="text-[11px] bg-emerald-50 text-emerald-700 hover:bg-emerald-100 px-2 py-0.5 rounded-full font-medium transition"
                        >
                          {act} &rarr;
                        </button>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            ))}

            {loading && (
              <div className="flex items-center space-x-2 text-slate-400 text-xs py-2">
                <Loader2 className="w-4 h-4 animate-spin text-emerald-600" />
                <span>AnnaDhata AI is thinking...</span>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Quick suggestions pills */}
          {messages.length <= 2 && (
            <div className="p-2 bg-white border-t border-slate-100 flex flex-wrap gap-1.5 overflow-x-auto">
              {initialQuickPrompts.map((q, idx) => (
                <button
                  key={idx}
                  onClick={() => handleSendMessage(q)}
                  className="text-[11px] bg-slate-100 text-slate-700 hover:bg-emerald-50 hover:text-emerald-700 px-2 py-1 rounded-lg transition text-left"
                >
                  {q}
                </button>
              ))}
            </div>
          )}

          {/* Input Box */}
          <form
            onSubmit={(e) => {
              e.preventDefault();
              handleSendMessage();
            }}
            className="p-3 bg-white border-t border-slate-100 flex items-center space-x-2"
          >
            <input
              type="text"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder="Ask anything about food donations..."
              className="flex-1 text-xs px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
            />
            <button
              type="submit"
              disabled={loading || !input.trim()}
              className="p-2 bg-emerald-600 text-white rounded-xl hover:bg-emerald-700 disabled:opacity-40 transition"
            >
              <Send className="w-4 h-4" />
            </button>
          </form>
        </div>
      )}
    </>
  );
};
