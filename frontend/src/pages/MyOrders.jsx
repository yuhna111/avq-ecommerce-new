import { useEffect, useState } from 'react'
import { Link, useParams, useSearchParams } from 'react-router-dom'
import { api } from '../api'
import { dateTime, money, ORDER_STATUS, PAYMENT_METHOD, PAYMENT_STATUS, StatusBadge } from '../format'

// UC11 theo dõi đơn hàng
export function MyOrders() {
  const [orders, setOrders] = useState(null)
  const [error, setError] = useState('')
  useEffect(() => {
    api('/orders').then(setOrders).catch((e) => setError(e.message))
  }, [])
  if (error) return <div className="alert alert-error">{error}</div>
  if (!orders) return <div className="muted">Đang tải...</div>
  return (
    <div>
      <h1>Đơn hàng của tôi</h1>
      {orders.length === 0 ? (
        <div className="empty card">Bạn chưa có đơn hàng nào. <Link to="/">Mua sắm ngay</Link></div>
      ) : (
        <div className="card table-wrap">
          <table className="table">
            <thead>
              <tr><th>Mã đơn</th><th>Ngày đặt</th><th>Số SP</th><th>Thanh toán</th><th>Tổng tiền</th><th>Trạng thái</th></tr>
            </thead>
            <tbody>
              {orders.map((o) => (
                <tr key={o.orderCode}>
                  <td><Link to={'/don-hang/' + o.orderCode}>{o.orderCode}</Link></td>
                  <td>{dateTime(o.orderDate)}</td>
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

// Chi tiết đơn + UC12 hủy đơn (BR13: chỉ ở Chờ thanh toán / Chờ xác nhận)
export function MyOrderDetail() {
  const { code } = useParams()
  const [params] = useSearchParams()
  const [o, setO] = useState(null)
  const [error, setError] = useState('')
  const [msg, setMsg] = useState('')

  const load = () => api('/orders/' + code).then(setO).catch((e) => setError(e.message))
  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [code])

  const cancel = async () => {
    const reason = window.prompt('Lý do hủy đơn (không bắt buộc):', '')
    if (reason === null) return
    setError('')
    try {
      setO(await api(`/orders/${code}/cancel`, { method: 'POST', params: { reason } }))
      setMsg('Đã hủy đơn hàng và hoàn lại tồn kho.')
    } catch (e) {
      setError(e.message)
    }
  }

  if (!o) return <div className="muted">{error || 'Đang tải...'}</div>
  const payAgain = o.status === 'CHO_THANH_TOAN' && o.paymentMethod === 'ONLINE'
  const payUrl = `/mock-pay?vnp_TxnRef=${o.orderCode}&vnp_Amount=${Math.round(Number(o.total) * 100)}&vnp_OrderInfo=${encodeURIComponent('Thanh toan don hang ' + o.orderCode)}`

  return (
    <div>
      <Link to="/don-hang" className="back">← Đơn hàng của tôi</Link>
      {params.get('moi') && <div className="alert alert-success">Đặt hàng thành công! Cửa hàng sẽ sớm xác nhận đơn của bạn.</div>}
      {msg && <div className="alert alert-success">{msg}</div>}
      {error && <div className="alert alert-error">{error}</div>}
      <OrderView o={o} />
      <div className="row gap">
        {payAgain && <Link className="btn btn-primary" to={payUrl}>Thanh toán ngay</Link>}
        {o.cancellableByCustomer ? (
          <button className="btn btn-danger" onClick={cancel}>Hủy đơn hàng</button>
        ) : (
          o.status !== 'DA_HUY' && o.status !== 'DA_GIAO' && (
            <p className="muted">Đơn đang giao nên không thể hủy. Vui lòng liên hệ cửa hàng nếu cần hỗ trợ.</p>
          )
        )}
      </div>
    </div>
  )
}

export function OrderView({ o }) {
  return (
    <div className="order-view">
      <div className="card">
        <div className="row between wrap">
          <h2>Đơn {o.orderCode}</h2>
          <StatusBadge status={o.status} />
        </div>
        <div className="kv">
          <div><span>Ngày đặt</span>{dateTime(o.orderDate)}</div>
          <div><span>Khách hàng</span>{o.customerName}</div>
          <div><span>Người nhận</span>{o.receiverName} · {o.receiverPhone}</div>
          <div><span>Địa chỉ</span>{o.shippingAddress}</div>
          <div><span>Thanh toán</span>{PAYMENT_METHOD[o.paymentMethod]}{o.paymentStatus && ` — ${PAYMENT_STATUS[o.paymentStatus] || o.paymentStatus}`}</div>
          {o.status === 'CHO_THANH_TOAN' && o.expiresAt && <div><span>Hạn thanh toán</span><b className="text-error">{dateTime(o.expiresAt)}</b></div>}
          {o.trackingCode && <div><span>Mã vận đơn</span><b>{o.trackingCode}</b></div>}
        </div>
        <table className="table">
          <thead><tr><th>Sản phẩm</th><th>Size</th><th>Màu</th><th>Đơn giá</th><th>SL</th><th>Thành tiền</th></tr></thead>
          <tbody>
            {o.items.map((i) => (
              <tr key={i.variantId}><td>{i.productName}</td><td>{i.size}</td><td>{i.color}</td><td>{money(i.unitPrice)}</td><td>{i.quantity}</td><td>{money(i.lineTotal)}</td></tr>
            ))}
          </tbody>
        </table>
        <div className="totals">
          <div className="sum-row"><span>Tạm tính</span><span>{money(o.subtotal)}</span></div>
          <div className="sum-row"><span>Phí giao hàng</span><span>{Number(o.shippingFee) === 0 ? 'Miễn phí' : money(o.shippingFee)}</span></div>
          <div className="sum-row total"><span>Tổng cộng</span><b>{money(o.total)}</b></div>
        </div>
      </div>
      <div className="card">
        <h3>Lịch sử trạng thái</h3>
        <ol className="timeline">
          {o.histories.map((h, idx) => (
            <li key={idx}>
              <div className="tl-dot" />
              <div>
                <b>{h.fromStatus ? `${ORDER_STATUS[h.fromStatus]?.label} → ` : ''}{ORDER_STATUS[h.toStatus]?.label}</b>
                <div className="muted small">{dateTime(h.changedAt)}{h.changedBy ? ' · ' + h.changedBy : ''}</div>
                {h.note && <div className="small">{h.note}</div>}
              </div>
            </li>
          ))}
        </ol>
      </div>
    </div>
  )
}
