import { useState, useEffect } from 'react'
import axios from 'axios'

function Dashboard() {
  const [services, setServices] = useState([])
  const [stats, setStats] = useState({})
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchDashboardData()
    const interval = setInterval(fetchDashboardData, 30000) // Refresh every 30s
    return () => clearInterval(interval)
  }, [])

  const fetchDashboardData = async () => {
    try {
      // Fetch service health status
      const servicePromises = [
        { name: 'AUTH_SERVICE', url: '/api/auth/health' },
        { name: 'PRODUCT_SERVICE', url: '/api/products/health' },
        { name: 'CREDENTIAL_SERVICE', url: '/api/credentials/health' },
        { name: 'AI_SERVICE', url: '/api/ai/health' }
      ].map(async (service) => {
        try {
          await axios.get(service.url, { timeout: 5000 })
          return { ...service, status: 'ONLINE' }
        } catch {
          return { ...service, status: 'OFFLINE' }
        }
      })

      const serviceStatuses = await Promise.all(servicePromises)
      setServices(serviceStatuses)

      // Fetch basic stats
      try {
        const statsResponse = await axios.get('/api/dashboard/stats')
        setStats(statsResponse.data)
      } catch (error) {
        console.error('Failed to fetch stats:', error)
      }
    } catch (error) {
      console.error('Failed to fetch dashboard data:', error)
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return <div className="loading">LOADING SYSTEM STATUS...</div>
  }

  return (
    <div>
      <div className="card">
        <div className="card-header">
          <h2 className="card-title">SYSTEM_OVERVIEW</h2>
          <span>LAST_UPDATE: {new Date().toLocaleString()}</span>
        </div>
        <div className="card-content">
          <div className="grid grid-2">
            <div>
              <h3>SERVICE_STATUS</h3>
              <table className="table">
                <thead>
                  <tr>
                    <th>SERVICE</th>
                    <th>STATUS</th>
                    <th>ENDPOINT</th>
                  </tr>
                </thead>
                <tbody>
                  {services.map((service) => (
                    <tr key={service.name}>
                      <td>{service.name}</td>
                      <td>
                        <span className={`status ${service.status === 'ONLINE' ? 'status-online' : 'status-offline'}`}>
                          {service.status}
                        </span>
                      </td>
                      <td>{service.url}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            
            <div>
              <h3>SYSTEM_METRICS</h3>
              <div className="grid">
                <div className="card">
                  <div className="card-content">
                    <div>TOTAL_USERS: {stats.totalUsers || 0}</div>
                    <div>ACTIVE_SESSIONS: {stats.activeSessions || 0}</div>
                    <div>PRODUCTS_COUNT: {stats.productsCount || 0}</div>
                    <div>AI_REQUESTS_TODAY: {stats.aiRequestsToday || 0}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid-3">
        <div className="card">
          <div className="card-header">
            <h3 className="card-title">RECENT_ACTIVITIES</h3>
          </div>
          <div className="card-content">
            <div>SYSTEM_STARTUP: {new Date().toLocaleString()}</div>
            <div>SERVICES_INITIALIZED: {services.filter(s => s.status === 'ONLINE').length}/{services.length}</div>
            <div>DATABASE_STATUS: CONNECTED</div>
            <div>CACHE_STATUS: ACTIVE</div>
          </div>
        </div>

        <div className="card">
          <div className="card-header">
            <h3 className="card-title">PERFORMANCE</h3>
          </div>
          <div className="card-content">
            <div>AVG_RESPONSE_TIME: 120ms</div>
            <div>CPU_USAGE: 15%</div>
            <div>MEMORY_USAGE: 2.1GB</div>
            <div>DISK_USAGE: 45%</div>
          </div>
        </div>

        <div className="card">
          <div className="card-header">
            <h3 className="card-title">ALERTS</h3>
          </div>
          <div className="card-content">
            {services.filter(s => s.status === 'OFFLINE').length > 0 ? (
              services.filter(s => s.status === 'OFFLINE').map(service => (
                <div key={service.name} className="error">
                  {service.name} IS OFFLINE
                </div>
              ))
            ) : (
              <div className="success">ALL SYSTEMS OPERATIONAL</div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

export default Dashboard
