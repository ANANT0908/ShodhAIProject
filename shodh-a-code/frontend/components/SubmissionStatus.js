export default function SubmissionStatus({status}){
  const cls = status === 'Accepted' ? 'status-AC' : (status === 'Wrong Answer' ? 'status-WA' : 'status-PEND');
  return (
    <div className={`status-badge ${cls}`}>
      {status || 'Pending'}
    </div>
  )
}
