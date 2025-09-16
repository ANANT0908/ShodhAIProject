import Link from 'next/link';

export default function Navbar(){
  return (
    <nav className="nav card" style={{marginBottom:12}}>
      <div style={{fontWeight:800,fontSize:18}}>ShodhJudge</div>
      <div style={{display:'flex',gap:8,alignItems:'center'}}>
        <Link href="/contests"><a className="muted">Contests</a></Link>
        <a href="#" className="muted">Docs</a>
      </div>
    </nav>
  )
}
