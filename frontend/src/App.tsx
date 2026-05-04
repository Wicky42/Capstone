import './App.css'
import { Routes, Route } from "react-router-dom";
import Header from "./components/layout/Header.tsx";
import Footer from "./components/layout/Footer.tsx";
import RegisterPage from "./pages/auth/RegisterPage.tsx";
import AuthCallbackPage from "./pages/auth/AuthCallbackPage.tsx";
import LogoutPage from "./pages/auth/LogoutPage.tsx";
import SellerOnboardingPage from "./pages/seller/SellerOnboardingPage.tsx";
import SellerDashboardPage from "./pages/seller/SellerDashboardPage.tsx";
import CreateProductPage from "./pages/product/CreateProductPage.tsx";
import ProductListPage from "./pages/seller/ProductListPage.tsx";
import InformationBanner from "./components/layout/InformationBanner.tsx";

function App() {
  return (
      <div className={"app"}>
        <Header />
        <InformationBanner />
         <main>
             <Routes>
                 {/*<Route path="/" element={<HomePage />} />*/}
                 <Route path="/seller/onboarding" element={<SellerOnboardingPage />} />
                 <Route path="/seller/products" element={<ProductListPage />} />
                 <Route path="/seller/products/new" element={<CreateProductPage />} />
                 <Route path="/seller/dashboard" element={<SellerDashboardPage />} />
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
