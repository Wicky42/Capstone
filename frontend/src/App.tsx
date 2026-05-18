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
import EditProductPage from "./pages/product/EditProductPage.tsx";
import ProductListPage from "./pages/seller/ProductListPage.tsx";
import InformationBanner from "./components/layout/InformationBanner.tsx";
import SellerShopEditPage from "./pages/seller/SellerShopEditPage.tsx";
import PendingShopVerificationPage from "./pages/admin/PendingShopVerificationPage.tsx";
import StorefrontPage from "./pages/StorefrontPage.tsx";
import ProductDetailPage from "./pages/product/ProductDetailPage.tsx";
import ShopDetailPage from "./pages/seller/ShopDetailPage.tsx";

function App() {
  return (
      <div className={"app"}>
        <Header />
        <InformationBanner />
         <main>
             <Routes>
                 <Route path="/" element={<StorefrontPage />} />
                 <Route path="/products/:productId" element={<ProductDetailPage />} />
                 <Route path="/shops/:slug" element={<ShopDetailPage />} />
                 <Route path="/seller/onboarding" element={<SellerOnboardingPage />} />
                 <Route path="/seller/products" element={<ProductListPage />} />
                 <Route path="/seller/products/new" element={<CreateProductPage />} />
                 <Route path="/seller/products/:productId/edit" element={<EditProductPage />} />
                 <Route path="/seller/dashboard" element={<SellerDashboardPage />} />
                 <Route path="/seller/shop/edit" element={<SellerShopEditPage />} />
                 <Route path="/admin" element={<PendingShopVerificationPage />} />
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
