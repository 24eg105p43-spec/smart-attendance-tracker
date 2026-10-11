import { useState } from 'react'

export default function App() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [user, setUser] = useState(null)
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
    setUser(data)
  }

  if (user) {
    return <h2>Welcome, {user.name} ({user.role})</h2>
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