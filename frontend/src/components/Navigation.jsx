import { NavLink } from 'react-router-dom'

function Navigation() {
  const navItems = [
    { path: '/', label: 'DASHBOARD' },
    { path: '/auth', label: 'AUTHENTICATION' },
    { path: '/products', label: 'PRODUCTS' },
    { path: '/credentials', label: 'CREDENTIALS' },
    { path: '/ai', label: 'AI_SERVICES' },
    { path: '/services', label: 'SYSTEM_STATUS' }
  ]

  return (
    <nav className="nav">
      <ul className="nav-list">
        {navItems.map((item) => (
          <li key={item.path} className="nav-item">
            <NavLink 
              to={item.path} 
              className={({ isActive }) => 
                isActive ? 'nav-link active' : 'nav-link'
              }
            >
              {item.label}
            </NavLink>
          </li>
        ))}
      </ul>
    </nav>
  )
}

export default Navigation
