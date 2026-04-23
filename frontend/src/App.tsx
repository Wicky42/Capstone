import './App.css'
import { Routes, Route } from "react-router-dom";
import Header from "./components/Header.tsx";
import Footer from "./components/Footer.tsx";
import RegisterPage from "./pages/RegisterPage.tsx";
import AuthCallbackPage from "./pages/AuthCallbackPage.tsx";

function login() {
    const host: string =
        globalThis.location.host === "localhost:5173"
            ? "http://localhost:8080"
            : globalThis.location.origin

    globalThis.open(host + "/oauth2/authorization/github", "_self")
}

function logout() {
    const host =
        globalThis.location.host === "localhost:5173"
            ? "http://localhost:8080"
            : globalThis.location.origin

    globalThis.open(host + "/logout", "_self")
}

function App() {


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
