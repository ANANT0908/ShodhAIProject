import { useEffect, useState } from 'react'
import { fetchContests } from '../../lib/api'
import Link from 'next/link'

export default function Contests(){
  const [contests, setContests] = useState([])
  const [loading, setLoading] = useState(true)
  useEffect(()=>{
    fetchContests().then(c=>setContests(c)).catch(e=>console.error(e)).finally(()=>setLoading(false))
  },[])
  return (
    <div>
      <div className="card">
        <h3>Contests</h3>
        <p className="muted">Contests loaded from backend</p>
      </div>
      <div style={{marginTop:12}} className="grid">
        {loading ? <div className="card muted">Loading...</div> :
          contests.length === 0 ? <div className="card muted">No contests found</div> :
          contests.map(c => (
            <div className="card" key={c.id}>
              <div style={{display:'flex',justifyContent:'space-between'}}>
                <div>
                  <div style={{fontWeight:700}}>{c.name}</div>
                  <div className="muted small">id: {c.contestId || c.id}</div>
                </div>
                <div style={{display:'flex',flexDirection:'column',gap:8}}>
                  <Link href={`/contests/${c.contestId || c.id}/problems`}><a className="btn">View Problems</a></Link>
                </div>
              </div>
            </div>
          ))
        }
      </div>
    </div>
  )
}
