import { useState, useEffect } from 'react'
import axios from 'axios'
import { useAuth } from '../context/AuthContext'

function AIPage() {
  const { isAuthenticated } = useAuth()
  const [requests, setRequests] = useState([])
  const [loading, setLoading] = useState(true)
  const [showRequestForm, setShowRequestForm] = useState(false)
  const [message, setMessage] = useState('')
  const [formData, setFormData] = useState({
    provider: 'OPENAI',
    requestType: 'DEMAND_FORECASTING',
    prompt: '',
    parameters: ''
  })

  useEffect(() => {
    if (isAuthenticated) {
      fetchRequests()
    }
  }, [isAuthenticated])

  const fetchRequests = async () => {
    try {
      setLoading(true)
      const response = await axios.get('/api/ai/requests')
      setRequests(response.data || [])
    } catch (error) {
      console.error('Failed to fetch AI requests:', error)
      setMessage('ERROR: FAILED TO LOAD AI REQUESTS')
    } finally {
      setLoading(false)
    }
  }

  const handleSubmitRequest = async (e) => {
    e.preventDefault()
    try {
      const requestData = {
        ...formData,
        parameters: formData.parameters ? JSON.parse(formData.parameters) : {}
      }
      
      await axios.post('/api/ai/requests', requestData)
      setMessage('SUCCESS: AI REQUEST SUBMITTED')
      setShowRequestForm(false)
      setFormData({
        provider: 'OPENAI',
        requestType: 'DEMAND_FORECASTING',
        prompt: '',
        parameters: ''
      })
      fetchRequests()
    } catch (error) {
      setMessage(`ERROR: ${error.response?.data?.message || 'FAILED TO SUBMIT REQUEST'}`)
    }
  }

  const getStatusColor = (status) => {
    switch (status) {
      case 'COMPLETED': return 'status-online'
      case 'FAILED': return 'status-offline'
      case 'PROCESSING': return 'status-warning'
      default: return 'status-warning'
    }
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
    return <div className="loading">LOADING AI SERVICES...</div>
  }

  return (
    <div>
      <div className="card">
        <div className="card-header">
          <h2 className="card-title">AI_SERVICES</h2>
          <button 
            className="btn btn-primary"
            onClick={() => setShowRequestForm(!showRequestForm)}
          >
            {showRequestForm ? 'CANCEL' : 'NEW_REQUEST'}
          </button>
        </div>
        <div className="card-content">
          {message && (
            <div className={message.includes('ERROR') ? 'error' : 'success'}>
              {message}
            </div>
          )}

          {showRequestForm && (
            <form className="form" onSubmit={handleSubmitRequest}>
              <div className="grid grid-2">
                <div className="form-group">
                  <label className="form-label">AI_PROVIDER</label>
                  <select
                    value={formData.provider}
                    onChange={(e) => setFormData(prev => ({ ...prev, provider: e.target.value }))}
                    className="form-select"
                  >
                    <option value="OPENAI">OPENAI</option>
                    <option value="AZURE">AZURE_OPENAI</option>
                    <option value="HUGGING_FACE">HUGGING_FACE</option>
                    <option value="OLLAMA">OLLAMA_LOCAL</option>
                  </select>
                </div>

                <div className="form-group">
                  <label className="form-label">REQUEST_TYPE</label>
                  <select
                    value={formData.requestType}
                    onChange={(e) => setFormData(prev => ({ ...prev, requestType: e.target.value }))}
                    className="form-select"
                  >
                    <option value="DEMAND_FORECASTING">DEMAND_FORECASTING</option>
                    <option value="INVENTORY_OPTIMIZATION">INVENTORY_OPTIMIZATION</option>
                    <option value="ROUTE_OPTIMIZATION">ROUTE_OPTIMIZATION</option>
                    <option value="ANOMALY_DETECTION">ANOMALY_DETECTION</option>
                    <option value="TEXT_ANALYSIS">TEXT_ANALYSIS</option>
                    <option value="CLASSIFICATION">CLASSIFICATION</option>
                  </select>
                </div>
              </div>

              <div className="form-group">
                <label className="form-label">PROMPT</label>
                <textarea
                  value={formData.prompt}
                  onChange={(e) => setFormData(prev => ({ ...prev, prompt: e.target.value }))}
                  className="form-textarea"
                  required
                  placeholder="ENTER AI PROMPT OR QUERY"
                />
              </div>

              <div className="form-group">
                <label className="form-label">PARAMETERS (JSON)</label>
                <textarea
                  value={formData.parameters}
                  onChange={(e) => setFormData(prev => ({ ...prev, parameters: e.target.value }))}
                  className="form-textarea"
                  placeholder='{"max_tokens": 1000, "temperature": 0.7}'
                />
              </div>

              <button type="submit" className="btn btn-success">
                SUBMIT_REQUEST
              </button>
            </form>
          )}

          <div className="card">
            <div className="card-header">
              <h3 className="card-title">REQUEST_HISTORY ({requests.length})</h3>
            </div>
            <div className="card-content">
              {requests.length === 0 ? (
                <div>NO AI REQUESTS FOUND</div>
              ) : (
                <table className="table">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>PROVIDER</th>
                      <th>TYPE</th>
                      <th>STATUS</th>
                      <th>TOKENS</th>
                      <th>RESPONSE_TIME</th>
                      <th>CREATED</th>
                    </tr>
                  </thead>
                  <tbody>
                    {requests.map(request => (
                      <tr key={request.id}>
                        <td>{request.id}</td>
                        <td>{request.provider}</td>
                        <td>{request.requestType}</td>
                        <td>
                          <span className={`status ${getStatusColor(request.status)}`}>
                            {request.status}
                          </span>
                        </td>
                        <td>{request.tokensUsed || 'N/A'}</td>
                        <td>{request.responseTimeMs ? `${request.responseTimeMs}ms` : 'N/A'}</td>
                        <td>{new Date(request.createdAt).toLocaleString()}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid-2">
        <div className="card">
          <div className="card-header">
            <h3 className="card-title">AI_CAPABILITIES</h3>
          </div>
          <div className="card-content">
            <div>DEMAND_FORECASTING: Predict future demand patterns</div>
            <div>INVENTORY_OPTIMIZATION: Optimize stock levels</div>
            <div>ROUTE_OPTIMIZATION: Find efficient delivery routes</div>
            <div>ANOMALY_DETECTION: Identify unusual patterns</div>
            <div>TEXT_ANALYSIS: Process documents and messages</div>
            <div>CLASSIFICATION: Categorize products and data</div>
          </div>
        </div>

        <div className="card">
          <div className="card-header">
            <h3 className="card-title">PROVIDER_STATUS</h3>
          </div>
          <div className="card-content">
            <div>OPENAI: <span className="status status-online">ONLINE</span></div>
            <div>AZURE_OPENAI: <span className="status status-online">ONLINE</span></div>
            <div>HUGGING_FACE: <span className="status status-online">ONLINE</span></div>
            <div>OLLAMA_LOCAL: <span className="status status-warning">STANDBY</span></div>
            <br />
            <div>FALLBACK_ENABLED: YES</div>
            <div>LOAD_BALANCING: ACTIVE</div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default AIPage
