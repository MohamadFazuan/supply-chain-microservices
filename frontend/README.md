# Supply Chain Frontend

A minimalist black and white dashboard for the Supply Chain Microservices system.

## 🎨 Design Philosophy

- **Minimalist**: Clean, terminal-inspired interface
- **Functional**: Every element serves a purpose
- **Monospace Typography**: Consistent, readable text
- **High Contrast**: Black background with white text
- **Responsive**: Works on all device sizes

## 🚀 Quick Start

### Development Mode
```bash
# From project root
./start-frontend.sh

# Or manually
cd frontend
npm install
npm run dev
```

### Production Build
```bash
cd frontend
npm run build
npm run preview
```

### Docker Deployment
```bash
# Build and run with Docker Compose
docker-compose up frontend --build

# Access at http://localhost:3000
```

## 📱 Interface Sections

### DASHBOARD
- System overview and health status
- Real-time service monitoring
- Performance metrics
- Alert notifications

### AUTHENTICATION
- User login and registration
- JWT token management
- Role-based access control
- Session monitoring

### PRODUCTS
- Product catalog management
- Inventory tracking
- Pricing configuration
- Category management

### CREDENTIALS
- Secure credential storage
- API key management
- Encryption status
- Access logging

### AI_SERVICES
- AI request submission
- Provider selection (OpenAI, Azure, Hugging Face, Ollama)
- Request history tracking
- Usage analytics

### SYSTEM_STATUS
- Detailed service health checks
- Performance monitoring
- Resource usage metrics
- Links to monitoring tools

## 🔧 Configuration

### Environment Variables
```bash
# API Gateway URL (default: http://localhost:8080)
VITE_API_BASE_URL=http://localhost:8080

# Enable development mode
VITE_DEV_MODE=true
```

### Proxy Configuration
The development server proxies API calls to the backend:
```javascript
// vite.config.js
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true,
    secure: false
  }
}
```

## 🛠️ Technology Stack

- **React 18**: Modern React with hooks
- **Vite**: Fast build tool and dev server
- **Axios**: HTTP client for API calls
- **React Router**: Client-side routing
- **Lucide React**: Minimalist icons
- **CSS3**: Pure CSS with custom properties

## 📦 Project Structure

```
frontend/
├── public/                 # Static assets
├── src/
│   ├── components/        # Reusable UI components
│   │   ├── Header.jsx     # Top navigation bar
│   │   └── Navigation.jsx # Main navigation menu
│   ├── context/          # React context providers
│   │   └── AuthContext.jsx # Authentication state
│   ├── pages/            # Page components
│   │   ├── Dashboard.jsx     # System overview
│   │   ├── AuthPage.jsx      # Login/Register
│   │   ├── ProductsPage.jsx  # Product management
│   │   ├── CredentialsPage.jsx # Credential management
│   │   ├── AIPage.jsx        # AI services
│   │   └── ServicesPage.jsx  # System status
│   ├── App.jsx           # Main app component
│   ├── main.jsx          # Entry point
│   └── index.css         # Global styles
├── Dockerfile            # Production container
├── nginx.conf           # Nginx configuration
├── package.json         # Dependencies
└── vite.config.js      # Build configuration
```

## 🎨 Styling Guide

### CSS Variables
```css
:root {
  --bg-primary: #000000;      /* Main background */
  --bg-secondary: #111111;    /* Card backgrounds */
  --text-primary: #ffffff;    /* Main text */
  --text-secondary: #cccccc;  /* Secondary text */
  --text-muted: #666666;      /* Muted text */
  --border: #333333;          /* Borders */
  --accent: #ffffff;          /* Accent elements */
  --error: #ff4444;           /* Error states */
  --success: #44ff44;         /* Success states */
  --warning: #ffff44;         /* Warning states */
}
```

### Component Classes
- `.card`: Content containers
- `.btn`: Interactive buttons
- `.form`: Form layouts
- `.table`: Data tables
- `.status`: Status indicators
- `.grid`: Layout grids

## 🔐 Authentication Flow

1. User enters credentials on AUTH page
2. Frontend sends request to `/api/auth/login`
3. Backend validates and returns JWT token
4. Token stored in localStorage and axios headers
5. Protected routes check authentication status
6. Auto-logout on token expiration

## 📊 Data Flow

```
User Interaction → Frontend Component → Axios HTTP Request → API Gateway → Microservice → Database
                                                                          ↓
User Interface ← React State Update ← Response Processing ← JSON Response ← Service Response
```

## 🔧 Development Tips

### Hot Reload
Changes to React components automatically reload in development mode.

### Debugging
- Browser DevTools for frontend debugging
- Network tab for API request/response inspection
- Console for error messages and logs

### Adding New Features
1. Create new page component in `/src/pages/`
2. Add route to `App.jsx`
3. Add navigation link to `Navigation.jsx`
4. Implement API calls with error handling
5. Update authentication checks if needed

## 🚀 Deployment

### Production Build
```bash
npm run build
# Creates optimized build in dist/ folder
```

### Docker Production
The production Dockerfile uses multi-stage build:
1. Build stage: Compiles React app
2. Production stage: Serves with Nginx

### Environment-Specific Builds
```bash
# Development
npm run dev

# Production preview
npm run build && npm run preview

# Docker production
docker build -t supply-chain-frontend .
```

## 🔍 Monitoring

The frontend includes built-in monitoring:
- Service health checks every 30 seconds
- Automatic retry on failed requests
- Error boundary for crash recovery
- Performance timing for API calls

## 🤝 Contributing

1. Follow the minimalist design principles
2. Use semantic HTML elements
3. Maintain accessibility standards
4. Test on multiple screen sizes
5. Keep components small and focused

## 📄 License

This frontend is part of the Supply Chain Microservices project under MIT License.
