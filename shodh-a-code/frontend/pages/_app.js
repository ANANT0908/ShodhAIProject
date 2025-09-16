import '../styles/global.css'
import Navbar from '../components/Navbar'

export default function App({ Component, pageProps }){
  return (
    <div>
      <div className="container">
        <Navbar />
        <Component {...pageProps} />
      </div>
    </div>
  )
}
