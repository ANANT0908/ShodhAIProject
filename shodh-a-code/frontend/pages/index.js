import Link from 'next/link'
import { useEffect } from 'react'

export default function Home(){

  // useEffect(async ()=>{
  //     try {
  //     const res = await fetch('http://localhost:8080/api/contests', {
  //       method: 'POST',
  //       headers: { 'Content-Type': 'application/json' },
  //       body: JSON.stringify({
  //         name: 'Sample Contest',
  //       }),
  //     });

  //     if (res.ok) {
  //       alert('Contest added successfully!');
  //       setName('');
  //       setDescription('');
  //       fetchContests(); // refresh the contest list
  //     } else {
  //       alert('Failed to add contest');
  //     }
  //   } catch (err) {
  //     console.error('Error adding contest:', err);
  //   }
  // }, []);



  return (
    <div className="card">
      <h2>Welcome to ShodhJudge</h2>
      <p className="muted">A lightweight judge UI connected to your backend judge engine.</p>
      <div style={{marginTop:12}}>
        <Link href='/contests'><a className='btn'>View Contests</a></Link>
      </div>
    </div>
  )
}
