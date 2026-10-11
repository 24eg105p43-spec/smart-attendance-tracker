import { useState, useEffect } from 'react'

// Sends a request with the saved token attached
function api(path, options = {}) {
  const token = localStorage.getItem('token')
  return fetch(path, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`
    }
  })
}

function Login({ onLogin }) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')

  async function handleLogin() {
    setError('')
    const res = await fetch('/api/auth/login', {
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
    onLogin()
  }

  return (
    <div style={{ maxWidth: 320, margin: '60px auto', display: 'grid', gap: 10 }}>
      <h2>Smart Attendance Tracker</h2>
      <input placeholder="Email" value={email}
             onChange={e => setEmail(e.target.value)} />
      <input placeholder="Password" type="password" value={password}
             onChange={e => setPassword(e.target.value)} />
      <button onClick={handleLogin}>Login</button>
      {error && <p style={{ color: 'red' }}>{error}</p>}
    </div>
  )
}

function Dashboard({ onLogout }) {
  const [subjects, setSubjects] = useState([])
  const [summaries, setSummaries] = useState({})
  const [newName, setNewName] = useState('')

  async function load() {
    const res = await api('/api/subjects')
    if (res.status === 401) { onLogout(); return }
    const list = await res.json()
    setSubjects(list)

    const result = {}
    for (const s of list) {
      const r = await api(`/api/subjects/${s.id}/summary`)
      if (r.ok) result[s.id] = await r.json()
    }
    setSummaries(result)
  }

  useEffect(() => { load() }, [])

  async function addSubject() {
    if (!newName.trim()) return
    await api('/api/subjects', {
      method: 'POST',
      body: JSON.stringify({ name: newName.trim() })
    })
    setNewName('')
    load()
  }

  async function mark(id, present) {
    await api(`/api/attendance/${id}?present=${present}`, { method: 'POST' })
    load()
  }

  return (
    <div style={{ maxWidth: 560, margin: '30px auto', padding: 10 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between' }}>
        <h2>My Attendance</h2>
        <button onClick={onLogout}>Logout</button>
      </div>

      <div style={{ display: 'flex', gap: 8, marginBottom: 20 }}>
        <input placeholder="New subject name" value={newName}
               onChange={e => setNewName(e.target.value)} />
        <button onClick={addSubject}>Add subject</button>
      </div>

      {subjects.length === 0 && <p>No subjects yet. Add one above.</p>}

      {subjects.map(s => {
        const sum = summaries[s.id]
        return (
          <div key={s.id}
               style={{ border: '1px solid #888', borderRadius: 8, padding: 12, marginBottom: 12 }}>
            <h3>{s.name}</h3>
            {sum && (
              <>
                <p>
                  <b style={{ color: sum.status === 'SAFE' ? 'green' : 'red' }}>
                    {sum.percentage}% ({sum.status})
                  </b>
                  {' '}— attended {sum.attended} of {sum.totalClasses}
                </p>
                <p>You can skip: <b>{sum.canSkip}</b> class(es)</p>
                {sum.needToAttend > 0 && (
                  <p>Attend <b>{sum.needToAttend}</b> in a row to reach {sum.minRequired}%</p>
                )}
              </>
            )}
            <button onClick={() => mark(s.id, true)}>Present</button>{' '}
            <button onClick={() => mark(s.id, false)}>Absent</button>
          </div>
        )
      })}
    </div>
  )
}

export default function App() {
  const [loggedIn, setLoggedIn] = useState(!!localStorage.getItem('token'))

  function logout() {
    localStorage.removeItem('token')
    setLoggedIn(false)
  }

  return loggedIn
    ? <Dashboard onLogout={logout} />
    : <Login onLogin={() => setLoggedIn(true)} />
}