// File: frontend/pages/contests/[id]/problems.js
import { useRouter } from 'next/router'
import { useEffect, useState } from 'react'
import { fetchProblems, submitCode, fetchSubmission, fetchLeaderboard } from '../../../lib/api'
import CodeEditor from '../../../components/CodeEditor'
import SubmissionStatus from '../../../components/SubmissionStatus'

export default function ContestProblems() {
  const router = useRouter()
  const { id } = router.query

  const [problems, setProblems] = useState([])
  const [selected, setSelected] = useState(null)
  const [editorCode, setEditorCode] = useState('')
  const [status, setStatus] = useState(null)
  const [leaderboard, setLeaderboard] = useState([])

  // Load problems
  useEffect(() => {
    if (!id) return
    fetchProblems(id).then(setProblems).catch(console.error)
  }, [id])

  // Leaderboard polling every 20s
  useEffect(() => {
    if (!id) return
    const load = () => fetchLeaderboard(id).then(setLeaderboard).catch(console.error)
    load()
    const interval = setInterval(load, 20000)
    return () => clearInterval(interval)
  }, [id])

  function openProblem(pr) {
    setSelected(pr)
    setEditorCode(getStarterCodeForProblem(pr))
  }

  function getStarterCodeForProblem(pr) {
    return `// Problem: ${pr?.title || ''}\n\npublic class Solution {\n  public static void main(String[] args) {\n    // write your solution here\n  }\n}`
  }

  // Submit code
  async function handleSubmit() {
    if (!selected) return
    const username = localStorage.getItem('username') || 'guest'
    try {
      const res = await submitCode({
        contestId: id,
        problemId: selected.id,
        language: 'java',
        sourceCode: editorCode,
        username
      })
      const subId = res.submissionId || res.submissionUuid
      setStatus('Pending')
      pollStatus(subId)
    } catch (err) {
      console.error(err)
      alert('Submission failed')
    }
  }

  // Poll status until final
  function pollStatus(subId) {
    let tries = 0
    const interval = setInterval(async () => {
      try {
        const s = await fetchSubmission(subId)
        setStatus(s.status)
        if (['Accepted', 'Wrong Answer', 'Error'].includes(s.status)) {
          clearInterval(interval)
        }
      } catch (e) {
        console.error(e)
        clearInterval(interval)
      }
      tries++
      if (tries > 30) clearInterval(interval) // stop after ~1min
    }, 3000)
  }

  return (
    <div style={{display:'flex', gap:16, alignItems:'flex-start'}}>
      {/* Problems List */}
      <div style={{width:320}}>
        <div className="card"><h3>Problems</h3></div>
        <div style={{marginTop:12, display:'grid', gap:8}}>
          {problems.map(pr => (
            <div key={pr.id} className={`card ${selected?.id === pr.id ? 'selected' : ''}`} onClick={() => openProblem(pr)} style={{cursor:'pointer'}}>
              <div style={{fontWeight:700}}>{pr.title}</div>
              <div className="muted small">{pr.slug}</div>
            </div>
          ))}
        </div>
      </div>

      {/* Editor + status */}
      <div style={{flex:1}}>
        {!selected ? <div className="card muted">Select a problem</div> : (
          <>
            <div className="card">
              <h3>{selected.title}</h3>
              <div className="muted small">{selected.statement}</div>
            </div>
            <div style={{marginTop:12}}>
              <CodeEditor value={editorCode} onChange={setEditorCode} language="java"/>
              <div style={{display:'flex', gap:8, marginTop:8}}>
                <button className="btn" onClick={handleSubmit}>Submit</button>
                {status && <SubmissionStatus status={status}/>}
              </div>
            </div>
          </>
        )}
      </div>

      {/* Leaderboard */}
      <div style={{width:280}}>
        <div className="card">
          <h3>Leaderboard</h3>
          {leaderboard.length === 0 ? (
            <p className="muted small">No data</p>
          ) : (
            <ul>
              {leaderboard.map((row, idx) => (
                <li key={idx} style={{marginBottom:6}}>
                  <strong>{row.username}</strong> — {row.score}
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </div>
  )
}
