import Link from 'next/link'

export default function Home(){

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
