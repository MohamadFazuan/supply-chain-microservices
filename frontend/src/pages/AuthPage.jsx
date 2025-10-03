import { useState } from 'react'
import { useAuth } from '../context/AuthContext'

function AuthPage() {
  const { login, register, isAuthenticated } = useAuth()
  const [mode, setMode] = useState('login')
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    email: '',
    firstName: '',
    lastName: ''
  })
  const [message, setMessage] = useState('')
  const [loading, setLoading] = useState(false)

  const handleChange = (e) => {
    setFormData(prev => ({
      ...prev,
      [e.target.name]: e.target.value
    }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setMessage('')

    try {
      if (mode === 'login') {
        const result = await login({
          username: formData.username,
          password: formData.password
        })
        
        if (result.success) {
          setMessage('LOGIN SUCCESSFUL')
        } else {
          setMessage(`ERROR: ${result.error}`)
        }
      } else {
        const result = await register({
          username: formData.username,
          password: formData.password,
          email: formData.email,
          firstName: formData.firstName,
          lastName: formData.lastName,
          roles: ['ROLE_USER']
        })
        
        if (result.success) {
          setMessage('REGISTRATION SUCCESSFUL - PLEASE LOGIN')
          setMode('login')
        } else {
          setMessage(`ERROR: ${result.error}`)
        }
      }
    } catch (error) {
      setMessage(`ERROR: ${error.message}`)
    } finally {
      setLoading(false)
    }
  }

  if (isAuthenticated) {
    return (
      <div className="card">
        <div className="card-header">
          <h2 className="card-title">AUTHENTICATION_STATUS</h2>
        </div>
        <div className="card-content">
          <div className="success">AUTHENTICATED SUCCESSFULLY</div>
          <div>STATUS: LOGGED_IN</div>
          <div>SESSION: ACTIVE</div>
        </div>
      </div>
    )
  }

  return (
    <div className="grid grid-2">
      <div className="card">
        <div className="card-header">
          <h2 className="card-title">
            {mode === 'login' ? 'USER_LOGIN' : 'USER_REGISTRATION'}
          </h2>
          <button 
            className="btn btn-small"
            onClick={() => {
              setMode(mode === 'login' ? 'register' : 'login')
              setMessage('')
              setFormData({
                username: '',
                password: '',
                email: '',
                firstName: '',
                lastName: ''
              })
            }}
          >
            {mode === 'login' ? 'REGISTER' : 'LOGIN'}
          </button>
        </div>
        <div className="card-content">
          <form className="form" onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label">USERNAME</label>
              <input
                type="text"
                name="username"
                value={formData.username}
                onChange={handleChange}
                className="form-input"
                required
                placeholder="ENTER_USERNAME"
              />
            </div>

            <div className="form-group">
              <label className="form-label">PASSWORD</label>
              <input
                type="password"
                name="password"
                value={formData.password}
                onChange={handleChange}
                className="form-input"
                required
                placeholder="ENTER_PASSWORD"
              />
            </div>

            {mode === 'register' && (
              <>
                <div className="form-group">
                  <label className="form-label">EMAIL</label>
                  <input
                    type="email"
                    name="email"
                    value={formData.email}
                    onChange={handleChange}
                    className="form-input"
                    required
                    placeholder="ENTER_EMAIL"
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">FIRST_NAME</label>
                  <input
                    type="text"
                    name="firstName"
                    value={formData.firstName}
                    onChange={handleChange}
                    className="form-input"
                    required
                    placeholder="ENTER_FIRST_NAME"
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">LAST_NAME</label>
                  <input
                    type="text"
                    name="lastName"
                    value={formData.lastName}
                    onChange={handleChange}
                    className="form-input"
                    required
                    placeholder="ENTER_LAST_NAME"
                  />
                </div>
              </>
            )}

            <button 
              type="submit" 
              className="btn btn-primary"
              disabled={loading}
            >
              {loading ? 'PROCESSING...' : (mode === 'login' ? 'LOGIN' : 'REGISTER')}
            </button>
          </form>

          {message && (
            <div className={message.includes('ERROR') ? 'error' : 'success'}>
              {message}
            </div>
          )}
        </div>
      </div>

      <div className="card">
        <div className="card-header">
          <h3 className="card-title">AUTHENTICATION_INFO</h3>
        </div>
        <div className="card-content">
          <div>SYSTEM: SUPPLY_CHAIN_MANAGEMENT</div>
          <div>AUTH_METHOD: JWT_TOKEN</div>
          <div>ENCRYPTION: AES_256</div>
          <div>SESSION_TIMEOUT: 24_HOURS</div>
          <br />
          <div>AVAILABLE_ROLES:</div>
          <div>- ADMIN</div>
          <div>- MANAGER</div>
          <div>- USER</div>
          <div>- SUPPLIER</div>
          <div>- CUSTOMER</div>
          <div>- WAREHOUSE_STAFF</div>
          <br />
          <div>DEFAULT_ROLE: USER</div>
          <div>SECURITY_LEVEL: HIGH</div>
        </div>
      </div>
    </div>
  )
}

export default AuthPage
