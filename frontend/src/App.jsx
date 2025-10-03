import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import Header from './components/Header'
import Navigation from './components/Navigation'
import Dashboard from './pages/Dashboard'
import AuthPage from './pages/AuthPage'
import ProductsPage from './pages/ProductsPage'
import CredentialsPage from './pages/CredentialsPage'
import AIPage from './pages/AIPage'
import ServicesPage from './pages/ServicesPage'
import { AuthProvider } from './context/AuthContext'

function App() {
  return (
    <AuthProvider>
      <Router>
        <div className="app">
          <Header />
          <Navigation />
          <main className="main">
            <div className="container">
              <Routes>
                <Route path="/" element={<Dashboard />} />
                <Route path="/auth" element={<AuthPage />} />
                <Route path="/products" element={<ProductsPage />} />
                <Route path="/credentials" element={<CredentialsPage />} />
                <Route path="/ai" element={<AIPage />} />
                <Route path="/services" element={<ServicesPage />} />
              </Routes>
            </div>
          </main>
        </div>
      </Router>
    </AuthProvider>
  )
}

export default App
