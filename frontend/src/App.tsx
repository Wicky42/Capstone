import './App.css'
import { Routes, Route } from "react-router-dom";
import Header from "./components/Header.tsx";
import Footer from "./components/Footer.tsx";
import RegisterPage from "./pages/RegisterPage.tsx";
import AuthCallbackPage from "./pages/AuthCallbackPage.tsx";
import LogoutPage from "./pages/LogoutPage.tsx";

function App() {
  return (
      <div className={"app"}>
        <Header />
         <main>
             <Routes>
                 <Route path="/register" element={<RegisterPage />} />
                 <Route path="/auth/callback" element={<AuthCallbackPage />} />
                 <Route path="/logout" element={<LogoutPage />} />
             </Routes>
        </main>
        <Footer />
      </div>
  )
}

export default App
