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
  const [mode, setMode] = useState('login') // 'login' or 'register'
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  async function handleLogin() {
    setError('')
    setMessage('')
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

  async function handleRegister() {
    setError('')
    setMessage('')
    const res = await fetch('/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, email, password })
    })
    if (!res.ok) {
      setError('Registration failed. The email may already be used, or the password is under 6 characters.')
      return
    }
    setMessage('Account created. Please log in.')
    setMode('login')
    setPassword('')
  }

  return (
    <div style={{ maxWidth: 320, margin: '60px auto', display: 'grid', gap: 10 }}>
      <h2>Smart Attendance Tracker</h2>
      <h3>{mode === 'login' ? 'Login' : 'Create account'}</h3>

      {mode === 'register' && (
        <input placeholder="Name" value={name}
               onChange={e => setName(e.target.value)} />
      )}
      <input placeholder="Email" value={email}
             onChange={e => setEmail(e.target.value)} />
      <input placeholder="Password" type="password" value={password}
             onChange={e => setPassword(e.target.value)} />

      <button onClick={mode === 'login' ? handleLogin : handleRegister}>
        {mode === 'login' ? 'Login' : 'Register'}
      </button>

      {error && <p style={{ color: 'red' }}>{error}</p>}
      {message && <p style={{ color: 'green' }}>{message}</p>}

      <button onClick={() => { setMode(mode === 'login' ? 'register' : 'login'); setError(''); setMessage('') }}>
        {mode === 'login' ? 'New here? Create an account' : 'Already have an account? Login'}
      </button>
    </div>
  )
}

function Dashboard({ onLogout }) {
  const [subjects, setSubjects] = useState([])
  const [summaries, setSummaries] = useState({})
  const [newName, setNewName] = useState('')
  const [addError, setAddError] = useState('')

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
  setAddError('')
  if (!newName.trim()) return
  const res = await api('/api/subjects', {
    method: 'POST',
    body: JSON.stringify({ name: newName.trim() })
  })
  if (!res.ok) {
    setAddError('Could not add subject. Status: ' + res.status)
    return
  }
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
      {addError && <p style={{ color: 'red' }}>{addError}</p>}

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