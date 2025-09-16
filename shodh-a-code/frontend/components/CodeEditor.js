import { useState } from 'react';

export default function CodeEditor({value, onChange, language}){
  const [wrap, setWrap] = useState(true);
  return (
    <div>
      <div style={{display:'flex',justifyContent:'space-between',marginBottom:8}}>
        <div className="muted">Language: <strong style={{color:'#fff'}}>{language}</strong></div>
        <div style={{display:'flex',gap:8}}>
          <button className="btn secondary small" onClick={()=>setWrap(!wrap)}>{wrap ? 'Wrap' : 'No wrap'}</button>
        </div>
      </div>
      <textarea
        className="code-editor"
        style={{whiteSpace: wrap ? 'pre-wrap' : 'pre'}}
        value={value}
        onChange={e=>onChange(e.target.value)}
        spellCheck={false}
      />
    </div>
  )
}
