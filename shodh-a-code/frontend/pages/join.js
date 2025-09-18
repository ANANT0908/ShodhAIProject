import { useState } from 'react'
import { useRouter } from 'next/router'

export default function JoinPage() {
  const [contestId, setContestId] = useState('')
  const [username, setUsername] = useState('')
  const router = useRouter()

  function handleJoin(e) {
    e.preventDefault()
    if (!contestId || !username) {
      alert('Please enter Contest ID and Username')
      return
    }
    localStorage.setItem('username', username)
    router.push(`/contests/${contestId}/problems`)
  }

  return (
    <div className="card" style={{maxWidth:400, margin:'40px auto'}}>
      <h2>Join Contest</h2>
      <form onSubmit={handleJoin} style={{display:'grid', gap:12}}>
        <input
          type="text"
          placeholder="Contest ID"
          value={contestId}
          onChange={e=>setContestId(e.target.value)}
          className="input"
        />
        <input
          type="text"
          placeholder="Username"
          value={username}
          onChange={e=>setUsername(e.target.value)}
          className="input"
        />
        <button className="btn" type="submit">Join</button>
      </form>
    </div>
  )
}
