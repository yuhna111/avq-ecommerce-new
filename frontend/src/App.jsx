import { Link, Navigate, NavLink, Route, Routes, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from './auth'
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

function Header() {
  const { auth, isCustomer, isStaff, logout, cartCount } = useAuth()
  const navigate = useNavigate()
  const doLogout = () => {
    logout()
    navigate(isStaff ? '/nhan-vien/dang-nhap' : '/')
  }
  return (
    <header className="header">
      <div className="container header-inner">
        <Link to={isStaff ? '/nhan-vien/don-hang' : '/'} className="logo">
          AVQ<span>Clothing</span>
        </Link>
        <nav className="nav">
          {isStaff ? (
            <>
              <NavLink to="/nhan-vien/don-hang">Quản lý đơn hàng</NavLink>
              <NavLink to="/nhan-vien/san-pham">Sản phẩm &amp; tồn kho</NavLink>
            </>
          ) : (
            <>
              <NavLink to="/" end>Sản phẩm</NavLink>
              {isCustomer && <NavLink to="/don-hang">Đơn của tôi</NavLink>}
              <NavLink to="/gio-hang" className="cart-link">
                Giỏ hàng {cartCount > 0 && <span className="cart-count">{cartCount}</span>}
              </NavLink>
            </>
          )}
        </nav>
        <div className="user-box">
          {auth ? (
            <>
              <span className="user-name">
                {isStaff ? (auth.role === 'OWNER' ? 'Chủ cửa hàng: ' : 'Nhân viên: ') : 'Xin chào, '}
                <b>{auth.fullName}</b>
              </span>
              <button className="btn btn-ghost btn-sm" onClick={doLogout}>Đăng xuất</button>
            </>
          ) : (
            <>
              <Link className="btn btn-ghost btn-sm" to="/dang-nhap">Đăng nhập</Link>
              <Link className="btn btn-primary btn-sm" to="/dang-ky">Đăng ký</Link>
            </>
          )}
        </div>
      </div>
    </header>
  )
}

export default function App() {
  return (
    <>
      <Header />
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
      <footer className="footer">
        <div className="container">
          AVQ Clothing — demo tiểu luận Phân tích thiết kế hệ thống thông tin · Thanh toán trực tuyến là cổng giả lập
          <span className="footer-staff"> · <Link to="/nhan-vien/dang-nhap">Dành cho nhân viên</Link></span>
        </div>
      </footer>
    </>
  )
}
