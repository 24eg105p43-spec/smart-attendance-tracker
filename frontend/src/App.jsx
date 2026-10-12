import { useState, useEffect } from 'react'
import './App.css'

const API_BASE = import.meta.env.VITE_API_BASE_URL || ''

function api(path, options = {}) {
  const token = localStorage.getItem('token')
  return fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
      ...(options.headers || {})
    }
  })
}

function Login({ onLogin }) {
  const [mode, setMode] = useState('login')
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  async function handleLogin(e) {
    e?.preventDefault()
    setError('')
    setMessage('')
    try {
      const res = await fetch(`${API_BASE}/api/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
      })
      if (!res.ok) {
        setError('Invalid email or password')
        return
      }
      const data = await res.json()
      localStorage.setItem('token', data.token)
      localStorage.setItem('userName', data.name || email)
      onLogin()
    } catch {
      setError('Could not connect to server.')
    }
  }

  async function handleRegister(e) {
    e?.preventDefault()
    setError('')
    setMessage('')
    try {
      const res = await fetch(`${API_BASE}/api/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name, email, password })
      })
      if (!res.ok) {
        setError('Registration failed. Email may already be in use.')
        return
      }
      setMessage('Account registered successfully! Please log in.')
      setMode('login')
      setPassword('')
    } catch {
      setError('Could not connect to server.')
    }
  }

  return (
    <div style={{ maxWidth: 360, margin: '80px auto', padding: 24, backgroundColor: 'var(--surface)', borderRadius: 12, border: '1px solid var(--border)' }}>
      <h2 style={{ marginBottom: 6, textAlign: 'center', background: 'linear-gradient(135deg, #60a5fa, #a78bfa)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
        Smart Attendance
      </h2>
      <p style={{ textAlign: 'center', color: 'var(--text-muted)', marginBottom: 20, fontSize: '0.85rem' }}>
        {mode === 'login' ? 'Sign in to manage your attendance' : 'Create an account to start tracking'}
      </p>

      <form onSubmit={mode === 'login' ? handleLogin : handleRegister} style={{ display: 'grid', gap: 12 }}>
        {mode === 'register' && (
          <input
            placeholder="Full Name"
            value={name}
            required
            onChange={e => setName(e.target.value)}
          />
        )}
        <input
          placeholder="Email address"
          type="email"
          value={email}
          required
          onChange={e => setEmail(e.target.value)}
        />
        <input
          placeholder="Password"
          type="password"
          value={password}
          required
          onChange={e => setPassword(e.target.value)}
        />

        <button type="submit" className="btn btn-primary" style={{ justifyContent: 'center', marginTop: 4 }}>
          {mode === 'login' ? 'Sign In' : 'Create Account'}
        </button>
      </form>

      {error && <p style={{ color: '#f87171', fontSize: '0.85rem', marginTop: 12, textAlign: 'center' }}>{error}</p>}
      {message && <p style={{ color: '#34d399', fontSize: '0.85rem', marginTop: 12, textAlign: 'center' }}>{message}</p>}

      <div style={{ textAlign: 'center', marginTop: 18 }}>
        <button
          type="button"
          className="btn btn-outline btn-sm"
          onClick={() => { setMode(mode === 'login' ? 'register' : 'login'); setError(''); setMessage('') }}
        >
          {mode === 'login' ? 'New student? Register' : 'Already registered? Login'}
        </button>
      </div>
    </div>
  )
}

function HolidaysModal({ onClose }) {
  const [holidays, setHolidays] = useState([])
  const [hName, setHName] = useState('')
  const [hDate, setHDate] = useState('')
  const [error, setError] = useState('')

  async function loadHolidays() {
    const res = await api('/api/holidays')
    if (res.ok) setHolidays(await res.json())
  }

  useEffect(() => { loadHolidays() }, [])

  async function addHoliday(e) {
    e.preventDefault()
    if (!hName || !hDate) return
    const res = await api('/api/holidays', {
      method: 'POST',
      body: JSON.stringify({ name: hName, date: hDate })
    })
    if (res.ok) {
      setHName('')
      setHDate('')
      loadHolidays()
    } else {
      setError('Failed to add holiday')
    }
  }

  async function deleteHoliday(id) {
    const res = await api(`/api/holidays/${id}`, { method: 'DELETE' })
    if (res.ok) loadHolidays()
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={e => e.stopPropagation()}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
          <h3>🌴 Academic Calendar & Leaves</h3>
          <button className="btn btn-outline btn-sm" onClick={onClose}>✕</button>
        </div>

        <form onSubmit={addHoliday} style={{ display: 'flex', gap: 8, marginBottom: 16 }}>
          <input
            placeholder="Holiday or Leave Name"
            value={hName}
            required
            style={{ flex: 2 }}
            onChange={e => setHName(e.target.value)}
          />
          <input
            type="date"
            value={hDate}
            required
            style={{ flex: 2 }}
            onChange={e => setHDate(e.target.value)}
          />
          <button type="submit" className="btn btn-primary btn-sm">Add</button>
        </form>
        {error && <p style={{ color: '#f87171', fontSize: '0.8rem', marginBottom: 10 }}>{error}</p>}

        <div style={{ maxHeight: 260, overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: 8 }}>
          {holidays.length === 0 ? (
            <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>No holidays added yet.</p>
          ) : (
            holidays.map(h => (
              <div key={h.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '8px 12px', background: 'var(--surface-light)', borderRadius: 6 }}>
                <div>
                  <strong>{h.name}</strong>
                  <span style={{ color: 'var(--text-muted)', fontSize: '0.8rem', marginLeft: 8 }}>{h.date}</span>
                </div>
                <button className="btn btn-danger btn-sm" onClick={() => deleteHoliday(h.id)}>Delete</button>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  )
}

function AiChatDrawer({ onClose }) {
  const [messages, setMessages] = useState([
    { role: 'ai', text: 'Hello! I am your AI Attendance Advisor. How can I help with your classes and bunk buffer today?' }
  ])
  const [input, setInput] = useState('')
  const [loading, setLoading] = useState(false)
  const [suggestions, setSuggestions] = useState(['Can I bunk tomorrow?', 'Show my attendance status', 'What is my semester forecast?'])

  async function sendMessage(text) {
    const query = text || input
    if (!query.trim() || loading) return

    setMessages(prev => [...prev, { role: 'user', text: query }])
    setInput('')
    setLoading(true)

    try {
      const res = await api('/api/ai/chat', {
        method: 'POST',
        body: JSON.stringify({ message: query })
      })
      if (res.ok) {
        const data = await res.json()
        setMessages(prev => [...prev, { role: 'ai', text: data.reply }])
        if (data.suggestedActions?.length) {
          setSuggestions(data.suggestedActions)
        }
      } else {
        setMessages(prev => [...prev, { role: 'ai', text: 'Could not fetch AI advice.' }])
      }
    } catch {
      setMessages(prev => [...prev, { role: 'ai', text: 'Error connecting to AI service.' }])
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="chat-drawer">
      <div className="chat-header">
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <span style={{ fontSize: '1.1rem' }}>🤖</span>
          <strong>AI Attendance Advisor</strong>
        </div>
        <button className="btn btn-outline btn-sm" onClick={onClose}>✕</button>
      </div>

      <div className="chat-messages">
        {messages.map((m, idx) => (
          <div key={idx} className={`chat-bubble ${m.role === 'user' ? 'chat-bubble-user' : 'chat-bubble-ai'}`}>
            {m.text}
          </div>
        ))}
        {loading && <div className="chat-bubble chat-bubble-ai" style={{ fontStyle: 'italic', color: 'var(--text-muted)' }}>Calculating advice...</div>}
      </div>

      <div className="quick-prompts">
        {suggestions.map((s, i) => (
          <span key={i} className="quick-chip" onClick={() => sendMessage(s)}>{s}</span>
        ))}
      </div>

      <form className="chat-input-area" onSubmit={e => { e.preventDefault(); sendMessage() }}>
        <input
          placeholder="Ask advice (e.g. Can I bunk Friday?)"
          value={input}
          style={{ flex: 1 }}
          onChange={e => setInput(e.target.value)}
        />
        <button type="submit" className="btn btn-primary btn-sm" disabled={loading}>Send</button>
      </form>
    </div>
  )
}

function Dashboard({ onLogout }) {
  const [subjects, setSubjects] = useState([])
  const [summaries, setSummaries] = useState({})
  const [semesterSummary, setSemesterSummary] = useState(null)
  const [newName, setNewName] = useState('')
  const [newMin, setNewMin] = useState(75)
  const [addError, setAddError] = useState('')
  const [showHolidays, setShowHolidays] = useState(false)
  const [showAiChat, setShowAiChat] = useState(false)
  const userName = localStorage.getItem('userName') || 'Student'

  async function loadData() {
    try {
      const res = await api('/api/subjects')
      if (res.status === 401) { onLogout(); return }
      const list = await res.json()
      setSubjects(list)

      const sumObj = {}
      for (const s of list) {
        const r = await api(`/api/subjects/${s.id}/summary`)
        if (r.ok) sumObj[s.id] = await r.json()
      }
      setSummaries(sumObj)

      // Fetch semester outlook & alerts
      const semRes = await api('/api/semester/summary')
      if (semRes.ok) {
        setSemesterSummary(await semRes.json())
      }
    } catch {
      // ignore
    }
  }

  useEffect(() => { loadData() }, [])

  async function addSubject(e) {
    e.preventDefault()
    setAddError('')
    if (!newName.trim()) return

    const res = await api('/api/subjects', {
      method: 'POST',
      body: JSON.stringify({ name: newName.trim(), minPercentage: Number(newMin) || 75 })
    })
    if (!res.ok) {
      setAddError('Could not add subject. Error: ' + res.status)
      return
    }
    setNewName('')
    loadData()
  }

  async function deleteSubject(id) {
    if (!window.confirm('Are you sure you want to delete this subject and its attendance records?')) return
    const res = await api(`/api/subjects/${id}`, { method: 'DELETE' })
    if (res.ok) loadData()
  }

  async function markAttendance(id, present) {
    await api(`/api/attendance/${id}?present=${present}`, { method: 'POST' })
    loadData()
  }

  async function exportCsv() {
    try {
      const res = await api('/api/reports/csv')
      if (!res.ok) return
      const blob = await res.blob()
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `attendance_report_${new Date().toISOString().slice(0, 10)}.csv`
      document.body.appendChild(a)
      a.click()
      a.remove()
      window.URL.revokeObjectURL(url)
    } catch {
      alert('Failed to download CSV report')
    }
  }

  return (
    <div className="app-container">
      {/* Top Navbar */}
      <header className="navbar">
        <div className="brand">
          <span style={{ fontSize: '1.5rem' }}>🎓</span>
          <div>
            <h1>Smart Attendance Tracker</h1>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Welcome back, {userName}</p>
          </div>
        </div>

        <div className="nav-actions">
          <button className="btn btn-secondary btn-sm" onClick={() => setShowHolidays(true)}>
            🌴 Holidays ({semesterSummary?.totalHolidays ?? 0})
          </button>
          <button className="btn btn-outline btn-sm" onClick={exportCsv}>
            📥 Export CSV
          </button>
          <button className="btn btn-danger btn-sm" onClick={onLogout}>
            Logout
          </button>
        </div>
      </header>

      <div className="main-grid">
        {/* Semester Forecast Banner */}
        {semesterSummary && (
          <section className="semester-banner">
            <div className="stat-box">
              <span className="stat-label">Semester Closes</span>
              <span className="stat-value">{semesterSummary.semesterEndDate}</span>
            </div>
            <div className="stat-box">
              <span className="stat-label">Remaining Classes</span>
              <span className="stat-value">{semesterSummary.totalRemainingClassDays} Days</span>
            </div>
            <div className="stat-box">
              <span className="stat-label">Tracked Courses</span>
              <span className="stat-value">{subjects.length} Subjects</span>
            </div>
            <div className="stat-box">
              <span className="stat-label">Status</span>
              <span className="stat-value" style={{ color: (semesterSummary.alerts?.length ?? 0) === 0 ? '#34d399' : '#f59e0b' }}>
                {(semesterSummary.alerts?.length ?? 0) === 0 ? 'All Safe' : `${semesterSummary.alerts.length} Warnings`}
              </span>
            </div>
          </section>
        )}

        {/* Dynamic Alerts Banner */}
        {semesterSummary?.alerts && semesterSummary.alerts.length > 0 && (
          <section className="alerts-list">
            {semesterSummary.alerts.map((alert, i) => (
              <div
                key={i}
                className={`alert-card ${
                  alert.severity === 'CRITICAL' ? 'alert-critical' :
                  alert.severity === 'WARNING' ? 'alert-warning' : 'alert-info'
                }`}
              >
                <span style={{ fontSize: '1.2rem' }}>
                  {alert.severity === 'CRITICAL' ? '🚨' : alert.severity === 'WARNING' ? '⚠️' : 'ℹ️'}
                </span>
                <div>
                  <strong>{alert.title}</strong>
                  <p style={{ marginTop: 2 }}>{alert.message}</p>
                </div>
              </div>
            ))}
          </section>
        )}

        {/* Add Subject Section */}
        <section style={{ backgroundColor: 'var(--surface)', padding: 16, borderRadius: 12, border: '1px solid var(--border)' }}>
          <form onSubmit={addSubject} style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
            <input
              placeholder="New Subject Name (e.g. Operating Systems)"
              value={newName}
              style={{ flex: 3, minWidth: 200 }}
              onChange={e => setNewName(e.target.value)}
            />
            <input
              type="number"
              placeholder="Min %"
              value={newMin}
              min="1"
              max="100"
              style={{ flex: 1, minWidth: 90 }}
              onChange={e => setNewMin(e.target.value)}
            />
            <button type="submit" className="btn btn-primary">
              + Add Subject
            </button>
          </form>
          {addError && <p style={{ color: '#f87171', fontSize: '0.85rem', marginTop: 8 }}>{addError}</p>}
        </section>

        {/* Subject Cards Grid */}
        <section>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 14 }}>
            <h3>Your Subjects ({subjects.length})</h3>
          </div>

          {subjects.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '40px 20px', background: 'var(--surface)', borderRadius: 12, border: '1px solid var(--border)' }}>
              <p style={{ color: 'var(--text-muted)' }}>No courses added yet. Add your subjects above to start tracking attendance!</p>
            </div>
          ) : (
            <div className="cards-grid">
              {subjects.map(s => {
                const sum = summaries[s.id]
                const isSafe = sum ? sum.status === 'SAFE' : true
                const pct = sum ? sum.percentage : 0

                return (
                  <div key={s.id} className="subject-card">
                    <div>
                      <div className="card-top">
                        <h4 style={{ fontSize: '1.1rem' }}>{s.name}</h4>
                        <div style={{ display: 'flex', gap: 6 }}>
                          <span className={`badge ${isSafe ? 'badge-safe' : 'badge-low'}`}>
                            {sum ? sum.status : 'N/A'}
                          </span>
                          <button
                            className="btn btn-danger btn-sm"
                            title="Delete subject"
                            style={{ padding: '2px 6px' }}
                            onClick={() => deleteSubject(s.id)}
                          >
                            ✕
                          </button>
                        </div>
                      </div>

                      {sum && (
                        <div className="card-stats">
                          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
                            <span style={{ fontSize: '1.6rem', fontWeight: 700, color: isSafe ? '#34d399' : '#f87171' }}>
                              {pct}%
                            </span>
                            <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                              Req: {sum.minRequired}%
                            </span>
                          </div>

                          <div className="percentage-bar-bg">
                            <div
                              className="percentage-bar-fill"
                              style={{
                                width: `${Math.min(100, Math.max(0, pct))}%`,
                                backgroundColor: isSafe ? '#10b981' : '#ef4444'
                              }}
                            />
                          </div>

                          <div className="card-metrics">
                            <span>Attended: <b>{sum.attended}</b></span>
                            <span>Total Classes: <b>{sum.totalClasses}</b></span>
                          </div>

                          <div className="bunk-advice">
                            {isSafe ? (
                              <p style={{ color: '#34d399' }}>
                                🛡️ Safe to skip: <b>{sum.canSkip}</b> class(es)
                              </p>
                            ) : (
                              <p style={{ color: '#f87171' }}>
                                ⚠️ Attend <b>{sum.needToAttend}</b> in a row to reach {sum.minRequired}%
                              </p>
                            )}
                          </div>
                        </div>
                      )}
                    </div>

                    <div className="card-actions">
                      <button
                        className="btn btn-primary"
                        style={{ flex: 1, backgroundColor: '#059669' }}
                        onClick={() => markAttendance(s.id, true)}
                      >
                        ✓ Present
                      </button>
                      <button
                        className="btn btn-outline"
                        style={{ flex: 1, borderColor: '#dc2626', color: '#f87171' }}
                        onClick={() => markAttendance(s.id, false)}
                      >
                        ✕ Absent
                      </button>
                    </div>
                  </div>
                )
              })}
            </div>
          )}
        </section>
      </div>

      {/* Floating AI Chatbot Button */}
      <button className="floating-ai-btn" onClick={() => setShowAiChat(!showAiChat)}>
        <span>🤖</span>
        <span>AI Advisor</span>
      </button>

      {/* AI Chat Drawer */}
      {showAiChat && <AiChatDrawer onClose={() => setShowAiChat(false)} />}

      {/* Holidays Modal */}
      {showHolidays && <HolidaysModal onClose={() => { setShowHolidays(false); loadData() }} />}
    </div>
  )
}

export default function App() {
  const [loggedIn, setLoggedIn] = useState(!!localStorage.getItem('token'))

  function logout() {
    localStorage.removeItem('token')
    localStorage.removeItem('userName')
    setLoggedIn(false)
  }

  return loggedIn
    ? <Dashboard onLogout={logout} />
    : <Login onLogin={() => setLoggedIn(true)} />
}