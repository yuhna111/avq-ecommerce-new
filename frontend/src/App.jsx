import { useEffect, useState } from 'react'
import { Link, Navigate, NavLink, Route, Routes, useLocation, useNavigate, useSearchParams } from 'react-router-dom'
import { api } from './api'
import { useAuth } from './auth'
import { FREE_SHIP_FROM, SHIPPING_FEE, money } from './format'
import ProductList from './pages/ProductList'
import ProductDetail from './pages/ProductDetail'
import { CustomerLogin, Register, StaffLogin } from './pages/Auth'
import Cart from './pages/Cart'
import Checkout from './pages/Checkout'
import MockPay from './pages/MockPay'
import { MyOrders, MyOrderDetail } from './pages/MyOrders'
import { StaffOrders, StaffOrderDetail } from './pages/StaffOrders'
import StaffProducts from './pages/StaffProducts'

function RequireCustomer({ children }) {
  const { isCustomer } = useAuth()
  const loc = useLocation()
  if (!isCustomer) return <Navigate to={'/dang-nhap?next=' + encodeURIComponent(loc.pathname + loc.search)} replace />
  return children
}

function RequireStaff({ children }) {
  const { isStaff } = useAuth()
  if (!isStaff) return <Navigate to="/nhan-vien/dang-nhap" replace />
  return children
}

// ===== Biểu tượng (SVG nội tuyến, không cần thư viện) =====
const Icon = {
  search: (
    <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" aria-hidden="true">
      <circle cx="11" cy="11" r="7" /><path d="m20 20-3.5-3.5" />
    </svg>
  ),
  user: (
    <svg viewBox="0 0 24 24" width="21" height="21" fill="none" stroke="currentColor" strokeWidth="1.9" strokeLinecap="round" aria-hidden="true">
      <circle cx="12" cy="8" r="4" /><path d="M4 21c1.5-4 4.5-6 8-6s6.5 2 8 6" />
    </svg>
  ),
  bag: (
    <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" strokeWidth="1.9" strokeLinejoin="round" aria-hidden="true">
      <path d="M5 8h14l-1 13H6L5 8Z" /><path d="M9 8V6a3 3 0 0 1 6 0v2" strokeLinecap="round" />
    </svg>
  ),
}

/** Danh mục có ít nhất một sản phẩm đang bán (ẩn danh mục rỗng trên menu). */
function useShopCategories() {
  const [cats, setCats] = useState([])
  useEffect(() => {
    Promise.all([api('/categories'), api('/products')])
      .then(([categories, products]) => {
        const used = new Set(products.map((p) => p.categoryName))
        setCats(categories.filter((c) => used.has(c.name)))
      })
      .catch(() => {})
  }, [])
  return cats
}

function CustomerHeader() {
  const { auth, isCustomer, logout, cartCount } = useAuth()
  const navigate = useNavigate()
  const loc = useLocation()
  const [params] = useSearchParams()
  const cats = useShopCategories()
  const [keyword, setKeyword] = useState(params.get('keyword') || '')
  useEffect(() => setKeyword(params.get('keyword') || ''), [params])

  const onHome = loc.pathname === '/'
  const activeCat = onHome ? params.get('categoryId') : null
  const search = (e) => {
    e.preventDefault()
    const k = keyword.trim()
    navigate(k ? '/?keyword=' + encodeURIComponent(k) : '/')
  }

  return (
    <>
      <div className="promo-bar">
        <div className="container promo-inner">
          <span>Miễn phí giao hàng cho đơn từ <b>{money(FREE_SHIP_FROM)}</b></span>
          <span className="promo-sep" aria-hidden="true">·</span>
          <span className="promo-more">Thanh toán khi nhận hàng hoặc trực tuyến</span>
        </div>
      </div>
      <header className="header">
        <div className="container header-inner">
          <Link to="/" className="logo">AVQ<span>Clothing</span></Link>
          <form className="header-search" role="search" onSubmit={search}>
            <input value={keyword} onChange={(e) => setKeyword(e.target.value)} placeholder="Tìm áo thun, sơ mi, jean..." aria-label="Tìm sản phẩm" />
            <button type="submit" aria-label="Tìm">{Icon.search}</button>
          </form>
          <div className="header-actions">
            {isCustomer ? (
              <>
                <NavLink to="/don-hang" className="icon-link" title="Đơn của tôi">{Icon.user}<span className="icon-label">Đơn của tôi</span></NavLink>
                <span className="user-name">Xin chào, <b>{auth.fullName}</b></span>
                <button className="btn btn-ghost btn-sm" onClick={() => { logout(); navigate('/') }}>Đăng xuất</button>
              </>
            ) : (
              <>
                <Link className="icon-link" to="/dang-nhap">{Icon.user}<span className="icon-label">Đăng nhập</span></Link>
                <Link className="btn btn-primary btn-sm hide-sm" to="/dang-ky">Đăng ký</Link>
              </>
            )}
            <NavLink to="/gio-hang" className="icon-link cart-link" title="Giỏ hàng" aria-label={'Giỏ hàng, ' + cartCount + ' sản phẩm'}>
              {Icon.bag}
              {cartCount > 0 && <span className="cart-count">{cartCount}</span>}
            </NavLink>
          </div>
        </div>
        <div className="container">
          <nav className="cat-nav" aria-label="Danh mục">
            <Link to="/" className={onHome && !activeCat && !params.get('keyword') ? 'active' : ''}>Tất cả</Link>
            {cats.map((c) => (
              <Link key={c.id} to={'/?categoryId=' + c.id} className={activeCat === String(c.id) ? 'active' : ''}>{c.name}</Link>
            ))}
          </nav>
        </div>
      </header>
    </>
  )
}

function StaffHeader() {
  const { auth, logout } = useAuth()
  const navigate = useNavigate()
  return (
    <header className="header header-staff">
      <div className="container header-inner">
        <Link to="/nhan-vien/don-hang" className="logo">AVQ<span>Clothing</span></Link>
        <nav className="nav">
          <NavLink to="/nhan-vien/don-hang">Quản lý đơn hàng</NavLink>
          <NavLink to="/nhan-vien/san-pham">Sản phẩm &amp; tồn kho</NavLink>
        </nav>
        <div className="header-actions">
          <span className="user-name">
            {auth.role === 'OWNER' ? 'Chủ cửa hàng: ' : 'Nhân viên: '}<b>{auth.fullName}</b>
          </span>
          <button className="btn btn-ghost btn-sm" onClick={() => { logout(); navigate('/nhan-vien/dang-nhap') }}>Đăng xuất</button>
        </div>
      </div>
    </header>
  )
}

function Footer({ staff }) {
  if (staff) {
    return (
      <footer className="footer">
        <div className="container muted small">AVQ Clothing · Trang quản lý dành cho nhân viên cửa hàng</div>
      </footer>
    )
  }
  return (
    <footer className="footer">
      <div className="container footer-grid">
        <div>
          <div className="logo">AVQ<span>Clothing</span></div>
          <p className="muted small">Demo tiểu luận Phân tích thiết kế hệ thống thông tin. Thanh toán trực tuyến dùng cổng giả lập, không trừ tiền thật.</p>
        </div>
        <div>
          <h4>Mua sắm</h4>
          <Link to="/">Tất cả sản phẩm</Link>
          <Link to="/gio-hang">Giỏ hàng</Link>
          <Link to="/don-hang">Đơn của tôi</Link>
        </div>
        <div>
          <h4>Giao hàng &amp; thanh toán</h4>
          <span>Phí giao {money(SHIPPING_FEE)}, miễn phí cho đơn từ {money(FREE_SHIP_FROM)}</span>
          <span>Thanh toán khi nhận hàng (COD) hoặc trực tuyến</span>
          <span>Đơn trực tuyến được giữ 15 phút để thanh toán</span>
        </div>
        <div>
          <h4>Cửa hàng</h4>
          <Link to="/nhan-vien/dang-nhap">Dành cho nhân viên</Link>
        </div>
      </div>
    </footer>
  )
}

export default function App() {
  const { isStaff } = useAuth()
  return (
    <>
      {isStaff ? <StaffHeader /> : <CustomerHeader />}
      <main className="container main">
        <Routes>
          <Route path="/" element={<ProductList />} />
          <Route path="/san-pham/:id" element={<ProductDetail />} />
          <Route path="/dang-nhap" element={<CustomerLogin />} />
          <Route path="/dang-ky" element={<Register />} />
          <Route path="/gio-hang" element={<RequireCustomer><Cart /></RequireCustomer>} />
          <Route path="/thanh-toan" element={<RequireCustomer><Checkout /></RequireCustomer>} />
          <Route path="/mock-pay" element={<RequireCustomer><MockPay /></RequireCustomer>} />
          <Route path="/don-hang" element={<RequireCustomer><MyOrders /></RequireCustomer>} />
          <Route path="/don-hang/:code" element={<RequireCustomer><MyOrderDetail /></RequireCustomer>} />
          <Route path="/nhan-vien/dang-nhap" element={<StaffLogin />} />
          <Route path="/nhan-vien/don-hang" element={<RequireStaff><StaffOrders /></RequireStaff>} />
          <Route path="/nhan-vien/don-hang/:code" element={<RequireStaff><StaffOrderDetail /></RequireStaff>} />
          <Route path="/nhan-vien/san-pham" element={<RequireStaff><StaffProducts /></RequireStaff>} />
          <Route path="*" element={<div className="empty">Không tìm thấy trang. <Link to="/">Về trang chủ</Link></div>} />
        </Routes>
      </main>
      <Footer staff={isStaff} />
    </>
  )
}
