import { useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { api } from '../api'
import { useAuth } from '../auth'

function useSubmit(fn) {
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const run = async (e) => {
    e.preventDefault()
    setError('')
    setBusy(true)
    try {
      await fn()
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }
  return { error, busy, run }
}

// UC02 đăng nhập khách (BR16: sai 5 lần khóa 15 phút)
export function CustomerLogin() {
  const [params] = useSearchParams()
  const navigate = useNavigate()
  const { login } = useAuth()
  const [form, setForm] = useState({ identifier: '', password: '' })
  const { error, busy, run } = useSubmit(async () => {
    login(await api('/auth/login', { method: 'POST', body: form }))
    navigate(params.get('next') || '/')
  })
  return (
    <div className="auth-box card">
      <h2>Đăng nhập</h2>
      <form onSubmit={run}>
        <label className="field">
          <span>Email hoặc số điện thoại</span>
          <input required value={form.identifier} onChange={(e) => setForm({ ...form, identifier: e.target.value })} autoFocus />
        </label>
        <label className="field">
          <span>Mật khẩu</span>
          <input required type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />
        </label>
        {error && <div className="alert alert-error">{error}</div>}
        <button className="btn btn-primary btn-block" disabled={busy}>Đăng nhập</button>
      </form>
      <p className="muted center">Chưa có tài khoản? <Link to="/dang-ky">Đăng ký</Link></p>
      <p className="muted small center">Tài khoản demo: khach@avq.vn / khach12345</p>
    </div>
  )
}

// UC01 đăng ký bằng email HOẶC số điện thoại (BR01)
export function Register() {
  const navigate = useNavigate()
  const { login } = useAuth()
  const [form, setForm] = useState({ fullName: '', email: '', phone: '', password: '', confirm: '' })
  const { error, busy, run } = useSubmit(async () => {
    if (form.password !== form.confirm) throw new Error('Mật khẩu nhập lại không khớp')
    if (!form.email.trim() && !form.phone.trim()) throw new Error('Vui lòng nhập email hoặc số điện thoại')
    const { confirm, ...body } = form
    login(await api('/auth/register', { method: 'POST', body }))
    navigate('/')
  })
  const f = (k) => ({ value: form[k], onChange: (e) => setForm({ ...form, [k]: e.target.value }) })
  return (
    <div className="auth-box card">
      <h2>Tạo tài khoản</h2>
      <form onSubmit={run}>
        <label className="field"><span>Họ và tên</span><input required {...f('fullName')} /></label>
        <div className="row gap-sm">
          <label className="field grow"><span>Email</span><input type="email" {...f('email')} /></label>
          <label className="field grow"><span>Số điện thoại</span><input {...f('phone')} placeholder="0901234567" /></label>
        </div>
        <p className="muted small">Nhập ít nhất một trong hai: email hoặc số điện thoại.</p>
        <label className="field"><span>Mật khẩu (tối thiểu 8 ký tự)</span><input required type="password" minLength={8} {...f('password')} /></label>
        <label className="field"><span>Nhập lại mật khẩu</span><input required type="password" {...f('confirm')} /></label>
        {error && <div className="alert alert-error">{error}</div>}
        <button className="btn btn-primary btn-block" disabled={busy}>Đăng ký</button>
      </form>
      <p className="muted center">Đã có tài khoản? <Link to="/dang-nhap">Đăng nhập</Link></p>
    </div>
  )
}

// UC13 đăng nhập nhân viên (BR02: tài khoản do chủ cửa hàng cấp)
export function StaffLogin() {
  const navigate = useNavigate()
  const { login } = useAuth()
  const [form, setForm] = useState({ identifier: '', password: '' })
  const { error, busy, run } = useSubmit(async () => {
    const t = await api('/auth/staff/login', { method: 'POST', body: form })
    login(t)
    navigate(t.role === 'OWNER' ? '/nhan-vien/tong-quan' : '/nhan-vien/don-hang')
  })
  return (
    <div className="auth-box card">
      <h2>Đăng nhập nhân viên</h2>
      <form onSubmit={run}>
        <label className="field">
          <span>Tên đăng nhập</span>
          <input required value={form.identifier} onChange={(e) => setForm({ ...form, identifier: e.target.value })} autoFocus />
        </label>
        <label className="field">
          <span>Mật khẩu</span>
          <input required type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />
        </label>
        {error && <div className="alert alert-error">{error}</div>}
        <button className="btn btn-primary btn-block" disabled={busy}>Đăng nhập</button>
      </form>
      <p className="muted small center">Tài khoản demo: nhanvien / nhanvien123 · chucuahang / chucuahang123</p>
    </div>
  )
}
