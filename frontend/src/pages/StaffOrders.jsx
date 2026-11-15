import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { api } from '../api'
import { dateTime, money, ORDER_STATUS, StatusBadge } from '../format'
import { OrderView } from './MyOrders'

// UC20 nhân viên xem đơn hàng
export function StaffOrders() {
  const [status, setStatus] = useState('')
  const [orders, setOrders] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    setOrders(null)
    api('/admin/orders', { params: { status } }).then(setOrders).catch((e) => setError(e.message))
  }, [status])

  return (
    <div>
      <h1>Quản lý đơn hàng</h1>
      <div className="chips tabs">
        <button className={'chip' + (status === '' ? ' active' : '')} onClick={() => setStatus('')}>Tất cả</button>
        {Object.entries(ORDER_STATUS).map(([k, v]) => (
          <button key={k} className={'chip' + (status === k ? ' active' : '')} onClick={() => setStatus(k)}>{v.label}</button>
        ))}
      </div>
      {error && <div className="alert alert-error">{error}</div>}
      {!orders ? <div className="muted">Đang tải...</div> : orders.length === 0 ? (
        <div className="empty card">Không có đơn hàng.</div>
      ) : (
        <div className="card table-wrap">
          <table className="table">
            <thead>
              <tr><th>Mã đơn</th><th>Ngày đặt</th><th>Khách hàng</th><th>Người nhận</th><th>Số SP</th><th>Thanh toán</th><th>Tổng tiền</th><th>Trạng thái</th></tr>
            </thead>
            <tbody>
              {orders.map((o) => (
                <tr key={o.orderCode}>
                  <td><Link to={'/nhan-vien/don-hang/' + o.orderCode}>{o.orderCode}</Link></td>
                  <td>{dateTime(o.orderDate)}</td>
                  <td>{o.customerName}</td>
                  <td>{o.receiverName}</td>
                  <td>{o.itemCount}</td>
                  <td>{o.paymentMethod === 'COD' ? 'COD' : 'Trực tuyến'}</td>
                  <td>{money(o.total)}</td>
                  <td><StatusBadge status={o.status} /></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}

const ACTION_LABEL = { DANG_GIAO: 'Xác nhận & giao hàng', DA_GIAO: 'Đã giao thành công', DA_HUY: 'Hủy đơn' }

// UC20 cập nhật trạng thái đơn (DT-03, DT-04)
export function StaffOrderDetail() {
  const { code } = useParams()
  const [o, setO] = useState(null)
  const [note, setNote] = useState('')
  const [error, setError] = useState('')
  const [msg, setMsg] = useState('')
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    api('/admin/orders/' + code).then(setO).catch((e) => setError(e.message))
  }, [code])

  const update = async (to) => {
    setError('')
    setMsg('')
    if (to === 'DA_HUY' && !note.trim()) {
      setError('Nhân viên hủy đơn phải nhập lý do vào ô ghi chú.')
      return
    }
    if (to === 'DA_HUY' && !window.confirm('Hủy đơn ' + code + '? Tồn kho sẽ được hoàn lại.')) return
    setBusy(true)
    try {
      setO(await api(`/admin/orders/${code}/status`, { method: 'PUT', params: { to, note } }))
      setNote('')
      setMsg('Đã chuyển đơn sang "' + ORDER_STATUS[to].label + '".')
    } catch (e) {
      setError(e.message)
    } finally {
      setBusy(false)
    }
  }

  if (!o) return <div className="muted">{error || 'Đang tải...'}</div>
  return (
    <div>
      <Link to="/nhan-vien/don-hang" className="back">← Danh sách đơn</Link>
      {msg && <div className="alert alert-success">{msg}</div>}
      {error && <div className="alert alert-error">{error}</div>}
      <div className="card">
        <h3>Cập nhật trạng thái</h3>
        {o.status === 'CHO_THANH_TOAN' ? (
          <p className="muted">Đơn đang chờ khách thanh toán trực tuyến. Hệ thống tự chuyển sang "Chờ xác nhận" khi thanh toán thành công, hoặc tự hủy sau 15 phút.</p>
        ) : o.nextStatuses.length === 0 ? (
          <p className="muted">Đơn đã ở trạng thái kết thúc.</p>
        ) : (
          <>
            <label className="field">
              <span>Ghi chú {o.nextStatuses.includes('DA_HUY') && '(bắt buộc khi hủy đơn)'}</span>
              <input value={note} onChange={(e) => setNote(e.target.value)} placeholder="VD: Giao qua GHN / Khách đổi ý..." />
            </label>
            <div className="row gap wrap">
              {o.nextStatuses.map((s) => (
                <button key={s} className={'btn ' + (s === 'DA_HUY' ? 'btn-danger' : 'btn-primary')} disabled={busy} onClick={() => update(s)}>
                  {ACTION_LABEL[s] || ORDER_STATUS[s].label}
                </button>
              ))}
            </div>
          </>
        )}
      </div>
      <OrderView o={o} />
    </div>
  )
}
