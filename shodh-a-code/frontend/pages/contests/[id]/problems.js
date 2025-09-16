import { useRouter } from 'next/router'
import { useEffect, useState } from 'react'
import { fetchProblems } from '../../../lib/api'
import Link from 'next/link'

export default function ContestProblems(){
  const router = useRouter()
  const { id } = router.query
  const [problems, setProblems] = useState([])
  useEffect(()=>{
    if(!id) return
    fetchProblems(id).then(p=>setProblems(p)).catch(e=>console.error(e))
  },[id])
  return (
    <div>
      <div className="card">
        <h3>Problems in {id}</h3>
        <p className="muted">Click to open problem</p>
      </div>
      <div style={{marginTop:12}} className="grid">
        {problems.length === 0 ? <div className="card muted">No problems</div> :
          problems.map(pr => (
            <div className="card" key={pr.id}>
              <div style={{display:'flex',justifyContent:'space-between'}}>
                <div>
                  <div style={{fontWeight:700}}>{pr.title}</div>
                  <div className="muted small">{pr.slug}</div>
                </div>
                <div>
                  <Link href={`/problems/${pr.id}`}><a className="btn">Open</a></Link>
                </div>
              </div>
            </div>
          ))
        }
      </div>
    </div>
  )
}
