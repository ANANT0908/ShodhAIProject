export default function ResultTable({results}){
  if(!results || results.length === 0) return <div className="muted">No test results yet</div>;
  return (
    <div className="card">
      <table>
        <thead>
          <tr><th>#</th><th>Result</th><th>Details</th></tr>
        </thead>
        <tbody>
          {results.map((r, idx) => (
            <tr key={idx}>
              <td>{idx+1}</td>
              <td className="small">{r.result}</td>
              <td className="small">{r.details}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
