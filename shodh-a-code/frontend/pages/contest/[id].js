import { useRouter } from 'next/router';
import { useEffect, useState } from 'react';
import dynamic from 'next/dynamic';
import axios from 'axios';

const MonacoEditor = dynamic(() => import('@monaco-editor/react'), { ssr: false });

export default function ContestPage(){
  const router = useRouter();
  const { id } = router.query;
  const [contest, setContest] = useState(null);
  const [problem, setProblem] = useState(null);
  const [code, setCode] = useState(`public class Main {\n  public static void main(String[] args) {\n    System.out.println(\"Hello\");\n  }\n}`);
  const [status, setStatus] = useState(null);
  const [submissionId, setSubmissionId] = useState(null);

  const username = typeof window !== 'undefined' ? new URLSearchParams(window.location.search).get('user') || 'anon' : 'anon';
const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL,
});
  useEffect(() => {
    if(!id) return;
    api.get(`/contests/${id}`)
      .then(r => {
        setContest(r.data);
        // pick first problem (if exists)
        if (r.data && r.data.problems && r.data.problems.length > 0) {
          setProblem(r.data.problems[0]);
        } else {
          setProblem(null);
        }
      })
      .catch(err => console.error(err));
  }, [id]);

  useEffect(() => {
    let iv;
    if (submissionId) {
      iv = setInterval(async () => {
        try {
          const r = await axios.get(`/api/submissions/${submissionId}`);
          setStatus(r.data.status);
          if (r.data.status !== 'Pending' && r.data.status !== 'Running') {
            clearInterval(iv);
          }
        } catch (e) {
          console.error(e);
        }
      }, 2000);
    }
    return () => clearInterval(iv);
  }, [submissionId]);

  function submit(){
    if(!problem) return alert('no problem loaded');
    axios.post('/api/submissions', {
      contestId: id,
      problemId: problem.id,
      username,
      language: 'java',
      sourceCode: code
    }).then(r => {
      setSubmissionId(r.data.submissionId);
      setStatus('Pending');
    }).catch(e => {
      console.error(e);
      alert('Submit failed');
    });
  }

  return (
    <div className="min-h-screen bg-gray-50 p-4">
      <div className="max-w-6xl mx-auto grid grid-cols-3 gap-4">
        <div className="col-span-2 bg-white p-4 rounded shadow">
          <h2 className="text-xl font-semibold mb-2">{problem ? problem.title : 'Loading problem...'}</h2>
          <p className="mb-4">{problem ? problem.statement : ''}</p>
          <div style={{height: '60vh'}} className="border">
            <MonacoEditor
              height="100%"
              defaultLanguage="java"
              value={code}
              onChange={(value) => setCode(value)}
            />
          </div>
          <div className="mt-3 flex items-center gap-3">
            <button onClick={submit} className="px-4 py-2 bg-indigo-600 text-white rounded">Submit</button>
            <div>Status: <strong>{status || 'Idle'}</strong></div>
          </div>
        </div>

        <aside className="col-span-1">
          <div className="bg-white p-4 rounded shadow mb-4">
            <h3 className="font-semibold">Contest</h3>
            <div>{contest ? contest.name : id}</div>
          </div>
          <div className="bg-white p-4 rounded shadow">
            <h3 className="font-semibold">Submission</h3>
            <div>ID: {submissionId}</div>
            <pre className="text-xs whitespace-pre-wrap break-words">{/* Optionally show results */}</pre>
          </div>
        </aside>
      </div>
    </div>
  );
}
