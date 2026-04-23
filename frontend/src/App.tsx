import axios from "axios"
import './App.css'
import {useEffect} from "react";
import { Routes, Route } from "react-router-dom";
import Header from "./components/Header.tsx";
import Footer from "./components/Footer.tsx";
import RegisterPage from "./pages/RegisterPage.tsx";
import AuthCallbackPage from "./pages/AuthCallbackPage.tsx";

function App() {


  function login(){
    const host : string = window.location.host === "localhost:5173" ? "http://localhost:8080" : window.location.origin
    window.open(host + "/oauth2/authorization/github" , "_self")
  }

  function logout() {
    const host = window.location.host === 'localhost:5173' ? 'http://localhost:8080' : window.location.origin

    window.open(host + '/logout', '_self')
  }

  const loadUser = () => {
    axios.get('/api/auth/me')
        .then(response => {
          console.log(response.data)
        })
  }

  useEffect(() => {
    loadUser();
  }, []);

  return (
      <div className={"app"}>
        <Header />
         <main>
             <Routes>
                 <Route path="/register" element={<RegisterPage />} />
                 <Route path="/auth/callback" element={<AuthCallbackPage />} />
             </Routes>
          <button onClick={login}>Login</button>
          <button onClick={logout}>Logout</button>
        </main>
        <Footer />
      </div>
  )
}

export default App
