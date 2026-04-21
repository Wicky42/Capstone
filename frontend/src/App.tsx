import './App.css'

function App() {


  function login(){
    const host : string = window.location.host === "localhost:5173" ? "http://localhost:8080" : window.location.origin
    window.open(host + "/oauth2/authorization/github" , "_self")
  }

  return (
    <>
      <button onClick={login}>Login</button>
    </>
  )
}

export default App
