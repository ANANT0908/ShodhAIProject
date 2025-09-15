import { useState } from 'react';
import { useRouter } from 'next/router';

export default function Home(){
  const [contestId, setContestId] = useState('sample-101');
  const [username, setUsername] = useState('anon');
  const router = useRouter();

  function join(){
    if(!contestId || !username) return alert('enter both');
    router.push(`/contest/${encodeURIComponent(contestId)}?user=${encodeURIComponent(username)}`);
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="p-6 rounded shadow-md w-full max-w-md bg-white">
        <h1 className="text-2xl font-semibold mb-4">Join Contest</h1>
        <input className="w-full p-2 border mb-3" value={contestId} onChange={e=>setContestId(e.target.value)} placeholder="Contest ID" />
        <input className="w-full p-2 border mb-3" value={username} onChange={e=>setUsername(e.target.value)} placeholder="Username" />
        <button onClick={join} className="w-full p-2 bg-indigo-600 text-white rounded">Join</button>
      </div>
    </div>
  );
}
