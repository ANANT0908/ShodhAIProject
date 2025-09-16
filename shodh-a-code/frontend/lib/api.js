// central API functions - adjust baseUrl if needed
const baseUrl = process.env.NEXT_PUBLIC_API_BASE || '';

export async function fetchContests() {
  const res = await fetch(`${baseUrl}/api/contests`);
  if (!res.ok) throw new Error('Failed to load contests');
  return res.json();
}

export async function fetchProblems(contestId) {
  const res = await fetch(`${baseUrl}/api/contests/${contestId}/problems`);
  if (!res.ok) throw new Error('Failed to load problems');
  return res.json();
}

export async function fetchProblem(problemId) {
  const res = await fetch(`${baseUrl}/api/problems/${problemId}`);
  if (!res.ok) throw new Error('Failed to load problem');
  return res.json();
}

export async function submitCode({ contestId, problemId, language, sourceCode }) {
  const res = await fetch(`${baseUrl}/api/submissions`, {
    method: 'POST',
    headers: {'Content-Type':'application/json'},
    body: JSON.stringify({ contestId, problemId, language, sourceCode })
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error('Submit failed: ' + text);
  }
  return res.json(); // expect { submissionUuid }
}

export async function fetchSubmission(uuid) {
  const res = await fetch(`${baseUrl}/api/submissions/${uuid}`);
  if (!res.ok) throw new Error('Failed to load submission');
  return res.json();
}
