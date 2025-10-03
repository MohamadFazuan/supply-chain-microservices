import { useAuth } from '../context/AuthContext'

function Header() {
  const { user, logout } = useAuth()
  
  return (
    <header className="header">
      <h1>SUPPLY_CHAIN_SYSTEM</h1>
      <div className="header-info">
        {user ? (
          <>
            <span>USER: {user.username}</span>
            <span>ROLE: {user.roles?.[0] || 'UNKNOWN'}</span>
            <button className="btn btn-small" onClick={logout}>
              LOGOUT
            </button>
          </>
        ) : (
          <span>STATUS: UNAUTHORIZED</span>
        )}
        <span>TIME: {new Date().toLocaleTimeString()}</span>
      </div>
    </header>
  )
}

export default Header
