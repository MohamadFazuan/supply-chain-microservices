import { useState, useEffect } from 'react'
import axios from 'axios'
import { useAuth } from '../context/AuthContext'

function CredentialsPage() {
  const { isAuthenticated } = useAuth()
  const [credentials, setCredentials] = useState([])
  const [loading, setLoading] = useState(true)
  const [showCreateForm, setShowCreateForm] = useState(false)
  const [message, setMessage] = useState('')
  const [formData, setFormData] = useState({
    name: '',
    type: 'API_KEY',
    description: '',
    value: ''
  })

  useEffect(() => {
    if (isAuthenticated) {
      fetchCredentials()
    }
  }, [isAuthenticated])

  const fetchCredentials = async () => {
    try {
      setLoading(true)
      const response = await axios.get('/api/credentials')
      setCredentials(response.data || [])
    } catch (error) {
      console.error('Failed to fetch credentials:', error)
      setMessage('ERROR: FAILED TO LOAD CREDENTIALS')
    } finally {
      setLoading(false)
    }
  }

  const handleCreateCredential = async (e) => {
    e.preventDefault()
    try {
      await axios.post('/api/credentials', formData)
      setMessage('SUCCESS: CREDENTIAL CREATED')
      setShowCreateForm(false)
      setFormData({
        name: '',
        type: 'API_KEY',
        description: '',
        value: ''
      })
      fetchCredentials()
    } catch (error) {
      setMessage(`ERROR: ${error.response?.data?.message || 'FAILED TO CREATE CREDENTIAL'}`)
    }
  }

  const handleDeleteCredential = async (id) => {
    if (window.confirm('CONFIRM DELETE CREDENTIAL?')) {
      try {
        await axios.delete(`/api/credentials/${id}`)
        setMessage('SUCCESS: CREDENTIAL DELETED')
        fetchCredentials()
      } catch (error) {
        setMessage(`ERROR: ${error.response?.data?.message || 'FAILED TO DELETE CREDENTIAL'}`)
      }
    }
  }

  const generateApiKey = () => {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'
    let result = 'sk-'
    for (let i = 0; i < 48; i++) {
      result += chars.charAt(Math.floor(Math.random() * chars.length))
    }
    setFormData(prev => ({ ...prev, value: result }))
  }

  if (!isAuthenticated) {
    return (
      <div className="card">
        <div className="card-content">
          <div className="error">AUTHENTICATION REQUIRED</div>
        </div>
      </div>
    )
  }

  if (loading) {
    return <div className="loading">LOADING CREDENTIALS...</div>
  }

  return (
    <div>
      <div className="card">
        <div className="card-header">
          <h2 className="card-title">CREDENTIAL_MANAGEMENT</h2>
          <button 
            className="btn btn-primary"
            onClick={() => setShowCreateForm(!showCreateForm)}
          >
            {showCreateForm ? 'CANCEL' : 'CREATE_CREDENTIAL'}
          </button>
        </div>
        <div className="card-content">
          {message && (
            <div className={message.includes('ERROR') ? 'error' : 'success'}>
              {message}
            </div>
          )}

          {showCreateForm && (
            <form className="form" onSubmit={handleCreateCredential}>
              <div className="grid grid-2">
                <div className="form-group">
                  <label className="form-label">CREDENTIAL_NAME</label>
                  <input
                    type="text"
                    value={formData.name}
                    onChange={(e) => setFormData(prev => ({ ...prev, name: e.target.value }))}
                    className="form-input"
                    required
                    placeholder="ENTER_CREDENTIAL_NAME"
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">TYPE</label>
                  <select
                    value={formData.type}
                    onChange={(e) => setFormData(prev => ({ ...prev, type: e.target.value }))}
                    className="form-select"
                  >
                    <option value="API_KEY">API_KEY</option>
                    <option value="SECRET">SECRET</option>
                    <option value="PASSWORD">PASSWORD</option>
                    <option value="TOKEN">TOKEN</option>
                    <option value="CERTIFICATE">CERTIFICATE</option>
                  </select>
                </div>
              </div>

              <div className="form-group">
                <label className="form-label">DESCRIPTION</label>
                <input
                  type="text"
                  value={formData.description}
                  onChange={(e) => setFormData(prev => ({ ...prev, description: e.target.value }))}
                  className="form-input"
                  placeholder="ENTER_DESCRIPTION"
                />
              </div>

              <div className="form-group">
                <label className="form-label">VALUE</label>
                <div style={{ display: 'flex', gap: '1rem' }}>
                  <input
                    type="password"
                    value={formData.value}
                    onChange={(e) => setFormData(prev => ({ ...prev, value: e.target.value }))}
                    className="form-input"
                    required
                    placeholder="ENTER_CREDENTIAL_VALUE"
                    style={{ flex: 1 }}
                  />
                  {formData.type === 'API_KEY' && (
                    <button 
                      type="button"
                      className="btn btn-small"
                      onClick={generateApiKey}
                    >
                      GENERATE
                    </button>
                  )}
                </div>
              </div>

              <button type="submit" className="btn btn-success">
                CREATE_CREDENTIAL
              </button>
            </form>
          )}

          <div className="card">
            <div className="card-header">
              <h3 className="card-title">CREDENTIAL_LIST ({credentials.length})</h3>
            </div>
            <div className="card-content">
              {credentials.length === 0 ? (
                <div>NO CREDENTIALS FOUND</div>
              ) : (
                <table className="table">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>NAME</th>
                      <th>TYPE</th>
                      <th>DESCRIPTION</th>
                      <th>CREATED</th>
                      <th>LAST_USED</th>
                      <th>ACTIONS</th>
                    </tr>
                  </thead>
                  <tbody>
                    {credentials.map(credential => (
                      <tr key={credential.id}>
                        <td>{credential.id}</td>
                        <td>{credential.name}</td>
                        <td>{credential.type}</td>
                        <td>{credential.description || 'N/A'}</td>
                        <td>{new Date(credential.createdAt).toLocaleDateString()}</td>
                        <td>
                          {credential.lastAccessedAt 
                            ? new Date(credential.lastAccessedAt).toLocaleDateString()
                            : 'NEVER'
                          }
                        </td>
                        <td>
                          <button 
                            className="btn btn-danger btn-small"
                            onClick={() => handleDeleteCredential(credential.id)}
                          >
                            DELETE
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        </div>
      </div>

      <div className="card">
        <div className="card-header">
          <h3 className="card-title">SECURITY_INFO</h3>
        </div>
        <div className="card-content">
          <div>ENCRYPTION: AES-GCM-256</div>
          <div>KEY_ROTATION: AUTOMATIC</div>
          <div>ACCESS_LOGGING: ENABLED</div>
          <div>AUDIT_TRAIL: COMPLETE</div>
          <div>VAULT_STATUS: SECURE</div>
          <br />
          <div className="success">ALL CREDENTIALS ARE ENCRYPTED AT REST</div>
        </div>
      </div>
    </div>
  )
}

export default CredentialsPage
