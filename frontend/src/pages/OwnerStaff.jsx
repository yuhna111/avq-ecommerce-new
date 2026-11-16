import { useEffect, useState } from 'react'
import { api } from '../api'
import { dateTime } from '../format'

// Quản lý nhân viên – chỉ chủ cửa hàng (bổ sung 17/11, BR02). Backend chặn vai trò khác (403).
export default function OwnerStaff() {
  const [list, setList] = useState(null)
  const [msg, setMsg] = useState(null)
  const [form, setForm] = useState({ username: '', fullName: '', password: '' })
  const [busy, setBusy] = useState(false)
  const [resetFor, setResetFor] = useState(null)
  const [newPw, setNewPw] = useState('')

  const load = () => api('/admin/staff').then(setList).catch((e) => setMsg({ type: 'error', text: e.message }))
  useEffect(() => { load() }, [])

  const create = async (e) => {
    e.preventDefault()
    setBusy(true)
    setMsg(null)
    try {
      const s = await api('/admin/staff', { method: 'POST', body: form })
      setMsg({ type: 'success', text: `Đã tạo tài khoản "${s.username}" cho ${s.fullName}. Nhân viên đăng nhập tại trang Dành cho nhân viên.` })
      setForm({ username: '', fullName: '', password: '' })
      load()
    } catch (err) {
      setMsg({ type: 'error', text: err.message })
    } finally {
      setBusy(false)
    }
  }

  const reset = async (s) => {
    setMsg(null)
    try {
      await api(`/admin/staff/${s.id}/password`, { method: 'PUT', body: { password: newPw } })
      setMsg({ type: 'success', text: `Đã đặt lại mật khẩu cho "${s.username}"${s.lockedUntil ? ' và mở khóa tài khoản' : ''}.` })
      setResetFor(null)
      setNewPw('')
      load()
    } catch (err) {
      setMsg({ type: 'error', text: err.message })
    }
  }

  const locked = (s) => s.lockedUntil && new Date(s.lockedUntil) > new Date()

  return (
    <div>
      <h1>Nhân viên</h1>
      <p className="muted">Tài khoản nhân viên do chủ cửa hàng cấp (BR02); không có đăng ký công khai.</p>
      {msg && <div className={'alert alert-' + msg.type}>{msg.text}</div>}

      <div className="staff-layout">
        <section className="card table-wrap">
          {!list ? <p className="muted pad">Đang tải...</p> : (
            <table className="table">
              <thead><tr><th>Tên đăng nhập</th><th>Họ tên</th><th>Vai trò</th><th>Trạng thái</th><th></th></tr></thead>
              <tbody>
                {list.map((s) => (
                  <tr key={s.id}>
                    <td><code>{s.username}</code></td>
                    <td>{s.fullName}</td>
                    <td><span className={'badge ' + (s.role === 'OWNER' ? 'badge-amber' : 'badge-gray')}>{s.role === 'OWNER' ? 'Chủ cửa hàng' : 'Nhân viên'}</span></td>
                    <td>{locked(s)
                      ? <span className="badge badge-violet" title={'Khóa đến ' + dateTime(s.lockedUntil)}>Tạm khóa (nhập sai)</span>
                      : <span className="badge badge-green">Hoạt động</span>}</td>
                    <td className="nowrap">
                      {s.role === 'STAFF' && (resetFor === s.id ? (
                        <span className="row gap-sm">
                          <input type="password" className="pw-input" placeholder="Mật khẩu mới (≥ 8 ký tự)" value={newPw} onChange={(e) => setNewPw(e.target.value)} autoFocus />
                          <button className="btn btn-primary btn-sm" onClick={() => reset(s)}>Lưu</button>
                          <button className="btn btn-ghost btn-sm" onClick={() => { setResetFor(null); setNewPw('') }}>Hủy</button>
                        </span>
                      ) : <button className="btn btn-ghost btn-sm" onClick={() => { setResetFor(s.id); setNewPw('') }}>Đặt lại mật khẩu</button>)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </section>

        <form className="card" onSubmit={create}>
          <h3>Tạo tài khoản nhân viên</h3>
          <label className="field"><span>Tên đăng nhập</span>
            <input required value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value.toLowerCase() })} placeholder="vd: thungan01" /></label>
          <label className="field"><span>Họ và tên</span>
            <input required value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} /></label>
          <label className="field"><span>Mật khẩu tạm (tối thiểu 8 ký tự)</span>
            <input required type="password" minLength={8} value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} /></label>
          <p className="muted small">Tài khoản mới có vai trò Nhân viên: xử lý đơn hàng, sản phẩm và tồn kho; không xem được Tổng quan và trang Nhân viên.</p>
          <button className="btn btn-primary btn-block" disabled={busy}>Tạo tài khoản</button>
        </form>
      </div>
    </div>
  )
}
