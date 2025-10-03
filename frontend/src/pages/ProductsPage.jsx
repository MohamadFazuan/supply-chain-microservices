import { useState, useEffect } from 'react'
import axios from 'axios'
import { useAuth } from '../context/AuthContext'

function ProductsPage() {
  const { isAuthenticated } = useAuth()
  const [products, setProducts] = useState([])
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const [showCreateForm, setShowCreateForm] = useState(false)
  const [message, setMessage] = useState('')
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    categoryId: '',
    basePrice: '',
    currency: 'USD',
    status: 'ACTIVE'
  })

  useEffect(() => {
    if (isAuthenticated) {
      fetchData()
    }
  }, [isAuthenticated])

  const fetchData = async () => {
    try {
      setLoading(true)
      const [productsRes, categoriesRes] = await Promise.all([
        axios.get('/api/products'),
        axios.get('/api/products/categories')
      ])
      setProducts(productsRes.data.content || productsRes.data || [])
      setCategories(categoriesRes.data || [])
    } catch (error) {
      console.error('Failed to fetch data:', error)
      setMessage('ERROR: FAILED TO LOAD PRODUCTS')
    } finally {
      setLoading(false)
    }
  }

  const handleCreateProduct = async (e) => {
    e.preventDefault()
    try {
      await axios.post('/api/products', {
        ...formData,
        basePrice: parseFloat(formData.basePrice)
      })
      setMessage('SUCCESS: PRODUCT CREATED')
      setShowCreateForm(false)
      setFormData({
        name: '',
        description: '',
        categoryId: '',
        basePrice: '',
        currency: 'USD',
        status: 'ACTIVE'
      })
      fetchData()
    } catch (error) {
      setMessage(`ERROR: ${error.response?.data?.message || 'FAILED TO CREATE PRODUCT'}`)
    }
  }

  const handleDeleteProduct = async (id) => {
    if (window.confirm('CONFIRM DELETE?')) {
      try {
        await axios.delete(`/api/products/${id}`)
        setMessage('SUCCESS: PRODUCT DELETED')
        fetchData()
      } catch (error) {
        setMessage(`ERROR: ${error.response?.data?.message || 'FAILED TO DELETE PRODUCT'}`)
      }
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
    return <div className="loading">LOADING PRODUCTS...</div>
  }

  return (
    <div>
      <div className="card">
        <div className="card-header">
          <h2 className="card-title">PRODUCT_MANAGEMENT</h2>
          <button 
            className="btn btn-primary"
            onClick={() => setShowCreateForm(!showCreateForm)}
          >
            {showCreateForm ? 'CANCEL' : 'CREATE_PRODUCT'}
          </button>
        </div>
        <div className="card-content">
          {message && (
            <div className={message.includes('ERROR') ? 'error' : 'success'}>
              {message}
            </div>
          )}

          {showCreateForm && (
            <form className="form" onSubmit={handleCreateProduct}>
              <div className="grid grid-2">
                <div className="form-group">
                  <label className="form-label">PRODUCT_NAME</label>
                  <input
                    type="text"
                    value={formData.name}
                    onChange={(e) => setFormData(prev => ({ ...prev, name: e.target.value }))}
                    className="form-input"
                    required
                    placeholder="ENTER_PRODUCT_NAME"
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">CATEGORY</label>
                  <select
                    value={formData.categoryId}
                    onChange={(e) => setFormData(prev => ({ ...prev, categoryId: e.target.value }))}
                    className="form-select"
                    required
                  >
                    <option value="">SELECT_CATEGORY</option>
                    {categories.map(cat => (
                      <option key={cat.id} value={cat.id}>{cat.name}</option>
                    ))}
                  </select>
                </div>

                <div className="form-group">
                  <label className="form-label">BASE_PRICE</label>
                  <input
                    type="number"
                    step="0.01"
                    value={formData.basePrice}
                    onChange={(e) => setFormData(prev => ({ ...prev, basePrice: e.target.value }))}
                    className="form-input"
                    required
                    placeholder="0.00"
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">CURRENCY</label>
                  <select
                    value={formData.currency}
                    onChange={(e) => setFormData(prev => ({ ...prev, currency: e.target.value }))}
                    className="form-select"
                  >
                    <option value="USD">USD</option>
                    <option value="EUR">EUR</option>
                    <option value="GBP">GBP</option>
                  </select>
                </div>
              </div>

              <div className="form-group">
                <label className="form-label">DESCRIPTION</label>
                <textarea
                  value={formData.description}
                  onChange={(e) => setFormData(prev => ({ ...prev, description: e.target.value }))}
                  className="form-textarea"
                  placeholder="ENTER_PRODUCT_DESCRIPTION"
                />
              </div>

              <button type="submit" className="btn btn-success">
                CREATE_PRODUCT
              </button>
            </form>
          )}

          <div className="card">
            <div className="card-header">
              <h3 className="card-title">PRODUCT_LIST ({products.length})</h3>
            </div>
            <div className="card-content">
              {products.length === 0 ? (
                <div>NO PRODUCTS FOUND</div>
              ) : (
                <table className="table">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>NAME</th>
                      <th>CATEGORY</th>
                      <th>PRICE</th>
                      <th>STATUS</th>
                      <th>CREATED</th>
                      <th>ACTIONS</th>
                    </tr>
                  </thead>
                  <tbody>
                    {products.map(product => (
                      <tr key={product.id}>
                        <td>{product.id}</td>
                        <td>{product.name}</td>
                        <td>{product.category?.name || 'N/A'}</td>
                        <td>{product.basePrice} {product.currency}</td>
                        <td>
                          <span className={`status ${product.status === 'ACTIVE' ? 'status-online' : 'status-offline'}`}>
                            {product.status}
                          </span>
                        </td>
                        <td>{new Date(product.createdAt).toLocaleDateString()}</td>
                        <td>
                          <button 
                            className="btn btn-danger btn-small"
                            onClick={() => handleDeleteProduct(product.id)}
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
    </div>
  )
}

export default ProductsPage
