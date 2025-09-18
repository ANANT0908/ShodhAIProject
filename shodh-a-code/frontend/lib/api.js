
const baseUrl = process.env.NEXT_PUBLIC_API_BASE || ''

export async function fetchContests() {
  const res = await fetch(`${baseUrl}/api/contests`)
  if (!res.ok) throw new Error('Failed to load contests')
  return res.json()
}

export async function fetchProblems(contestId) {
  const res = await fetch(`${baseUrl}/api/contests/${contestId}/problems`)
  if (!res.ok) throw new Error('Failed to load problems')
  return res.json()
}

export async function submitCode({ contestId, problemId, language, sourceCode, username }) {
  const res = await fetch(`${baseUrl}/api/submissions`, {
    method: 'POST',
    headers: { 'Content-Type':'application/json' },
    body: JSON.stringify({ contestId, problemId, language, sourceCode, username })
  })
  if (!res.ok) throw new Error(await res.text())
  return res.json()
}

export async function fetchSubmission(id) {
  const res = await fetch(`${baseUrl}/api/submissions/${id}`)
  if (!res.ok) throw new Error('Failed to load submission')
  return res.json()
}

export async function fetchLeaderboard(contestId) {
  const res = await fetch(`${baseUrl}/api/contests/${contestId}/leaderboard`)
  if (!res.ok) throw new Error('Failed to load leaderboard')
  return res.json()
}
