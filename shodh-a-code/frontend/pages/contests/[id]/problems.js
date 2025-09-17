// File: pages/contests/[id]/index.jsx
import { useRouter } from 'next/router'
import { useEffect, useState } from 'react'
import { fetchProblems } from '../../../lib/api'
import CodeEditor from '../../../components/CodeEditor'

export default function ContestProblems(){
  const router = useRouter()
  const { id } = router.query
  const [problems, setProblems] = useState([])
  const [selected, setSelected] = useState(null)
  const [editorCode, setEditorCode] = useState('')

  useEffect(()=>{
    if(!id) return
    fetchProblems(id).then(p=>{
      setProblems(p || [])
      if(p && p.length > 0 && !selected){
        setSelected(p[0])
        setEditorCode(getStarterCodeForProblem(p[0]))
      }
    }).catch(e=>console.error(e))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  },[id])

  function openProblem(pr){
    setSelected(pr)
    setEditorCode(getStarterCodeForProblem(pr))
  }

  function getStarterCodeForProblem(pr){
    // minimal starter template — edit as you wish
    return `// Problem: ${pr?.title || ''}\n// Slug: ${pr?.slug || ''}\n\npublic class Solution {\n  public static void main(String[] args) {\n    // write your solution here\n  }\n}\n`
  }

  return (
    <div style={{display:'flex',gap:16,alignItems:'flex-start'}}>
      <div style={{width:320}}>
        <div className="card">
          <h3>Problems in {id}</h3>
          <p className="muted">Click to open problem</p>
        </div>

        <div style={{marginTop:12, display:'grid', gap:8}}>
          {problems.length === 0 ? (
            <div className="card muted">No problems</div>
          ) : problems.map(pr => (
            <div
              className={`card ${selected?.id === pr.id ? 'selected' : ''}`}
              key={pr.id}
              style={{cursor:'pointer'}}
              onClick={()=>openProblem(pr)}
            >
              <div style={{display:'flex',justifyContent:'space-between',alignItems:'center'}}>
                <div>
                  <div style={{fontWeight:700}}>{pr.title}</div>
                  <div className="muted small">{pr.slug}</div>
                </div>
                <div style={{opacity:0.8,fontSize:12}}>{selected?.id === pr.id ? 'Open' : 'Open'}</div>
              </div>
            </div>
          ))}
        </div>
      </div>

      <div style={{flex:1}}>
        {!selected ? (
          <div className="card muted">Select a problem to open the editor</div>
        ) : (
          <>
            <div className="card">
              <div style={{display:'flex',justifyContent:'space-between',alignItems:'flex-start',gap:12}}>
                <div>
                  <h3 style={{margin:0}}>{selected.title}</h3>
                  <div className="muted small" style={{marginTop:6}}>{selected.slug}</div>
                </div>
                <div style={{minWidth:220, textAlign:'right'}}>
                  <div style={{marginBottom:8}}>
                    <strong>Problem ID:</strong> {selected.id}
                  </div>
                </div>
              </div>
              <div style={{marginTop:12, whiteSpace:'pre-wrap'}} className="muted small">
                {selected.statement}
              </div>
            </div>

            <div style={{marginTop:12}}>
              <CodeEditor
                value={editorCode}
                onChange={setEditorCode}
                language="java"
              />

              <div style={{display:'flex',gap:8,marginTop:8}}>
                <button className="btn" onClick={()=>console.log('Run (stub) — code:', editorCode)}>
                  Run (stub)
                </button>
                <button className="btn" onClick={()=>console.log('Submit (stub) — code:', editorCode)}>
                  Submit (stub)
                </button>
              </div>
            </div>
          </>
        )}
      </div>
    </div>
  )
}
