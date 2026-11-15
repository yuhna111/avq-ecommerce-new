import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { api } from '../api'
import { useAuth } from '../auth'
import { money, PAYMENT_METHOD } from '../format'

// UC09 đặt hàng: khách thấy giá hiện hành + phí giao (BR08, DT-01) trước khi xác nhận
export default function Checkout() {
  const navigate = useNavigate()
  const { refreshCart } = useAuth()
  const [sum, setSum] = useState(null)
  const [error, setError] = useState('')
  const [addrId, setAddrId] = useState('new')
  const [form, setForm] = useState({ receiverName: '', receiverPhone: '', shippingAddress: '', saveAddress: false })
  const [method, setMethod] = useState('COD')
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    api('/orders/checkout')
      .then((s) => {
        setSum(s)
        const def = s.addresses.find((a) => a.isDefault) || s.addresses[0]
        if (def) pickAddress(def)
      })
      .catch((e) => setError(e.message))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const pickAddress = (a) => {
    setAddrId(a ? String(a.id) : 'new')
    setForm(a ? { receiverName: a.receiverName, receiverPhone: a.receiverPhone, shippingAddress: a.addressLine, saveAddress: false }
      : { receiverName: '', receiverPhone: '', shippingAddress: '', saveAddress: false })
  }

  const place = async (e) => {
    e.preventDefault()
    setError('')
    setBusy(true)
    try {
      if (addrId === 'new' && form.saveAddress) {
        await api('/account/addresses', {
          method: 'POST',
          body: { receiverName: form.receiverName, receiverPhone: form.receiverPhone, addressLine: form.shippingAddress, isDefault: false },
        })
      }
      const r = await api('/orders', {
        method: 'POST',
        body: { receiverName: form.receiverName, receiverPhone: form.receiverPhone, shippingAddress: form.shippingAddress, paymentMethod: method },
      })
      refreshCart()
      if (r.paymentUrl) navigate(r.paymentUrl) // BR10: chuyển sang cổng thanh toán (giả lập)
      else navigate('/don-hang/' + r.orderCode + '?moi=1')
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  if (!sum) return error ? <div className="alert alert-error">{error} <Link to="/gio-hang">Về giỏ hàng</Link></div> : <div className="muted">Đang tải...</div>

  return (
    <form onSubmit={place}>
      <h1>Xác nhận đặt hàng</h1>
      <div className="cart-layout">
        <div>
          <div className="card">
            <h3>Thông tin giao hàng</h3>
            {sum.addresses.length > 0 && (
              <div className="addr-list">
                {sum.addresses.map((a) => (
                  <label key={a.id} className={'addr' + (addrId === String(a.id) ? ' active' : '')}>
                    <input type="radio" name="addr" checked={addrId === String(a.id)} onChange={() => pickAddress(a)} />
                    <div><b>{a.receiverName}</b> · {a.receiverPhone}{a.isDefault && <span className="badge badge-blue">Mặc định</span>}<br />
                      <span className="muted">{a.addressLine}</span></div>
                  </label>
                ))}
                <label className={'addr' + (addrId === 'new' ? ' active' : '')}>
                  <input type="radio" name="addr" checked={addrId === 'new'} onChange={() => pickAddress(null)} />
                  <div>Giao đến địa chỉ khác</div>
                </label>
              </div>
            )}
            <div className="row gap-sm">
              <label className="field grow"><span>Người nhận</span>
                <input required value={form.receiverName} onChange={(e) => setForm({ ...form, receiverName: e.target.value })} disabled={addrId !== 'new'} /></label>
              <label className="field grow"><span>Số điện thoại</span>
                <input required value={form.receiverPhone} onChange={(e) => setForm({ ...form, receiverPhone: e.target.value })} disabled={addrId !== 'new'} /></label>
            </div>
            <label className="field"><span>Địa chỉ giao hàng</span>
              <input required value={form.shippingAddress} onChange={(e) => setForm({ ...form, shippingAddress: e.target.value })} disabled={addrId !== 'new'} /></label>
            {addrId === 'new' && (
              <label className="check"><input type="checkbox" checked={form.saveAddress} onChange={(e) => setForm({ ...form, saveAddress: e.target.checked })} /> Lưu địa chỉ này</label>
            )}
          </div>
          <div className="card">
            <h3>Phương thức thanh toán</h3>
            {Object.entries(PAYMENT_METHOD).map(([k, v]) => (
              <label key={k} className={'addr' + (method === k ? ' active' : '')}>
                <input type="radio" name="pm" checked={method === k} onChange={() => setMethod(k)} />
                <div><b>{v}</b><br /><span className="muted small">
                  {k === 'COD' ? 'Đơn chuyển ngay sang "Chờ xác nhận".' : 'Thanh toán qua cổng (giả lập) trong 15 phút, quá hạn đơn tự hủy.'}
                </span></div>
              </label>
            ))}
          </div>
        </div>
        <div className="card summary">
          <h3>Đơn hàng</h3>
          {sum.items.map((i) => (
            <div key={i.variantId} className="sum-item">
              <span>{i.productName} <span className="muted">({i.size}/{i.color}) × {i.quantity}</span></span>
              <span>{money(i.lineTotal)}</span>
            </div>
          ))}
          <hr />
          <div className="sum-row"><span>Tạm tính</span><span>{money(sum.subtotal)}</span></div>
          <div className="sum-row"><span>Phí giao hàng</span><span>{Number(sum.shippingFee) === 0 ? 'Miễn phí' : money(sum.shippingFee)}</span></div>
          <div className="sum-row total"><span>Tổng cộng</span><b>{money(sum.total)}</b></div>
          {error && <div className="alert alert-error">{error}</div>}
          <button className="btn btn-primary btn-block btn-lg" disabled={busy}>{busy ? 'Đang xử lý...' : 'Xác nhận đặt hàng'}</button>
          <Link to="/gio-hang" className="muted small center block">← Sửa giỏ hàng</Link>
        </div>
      </div>
    </form>
  )
}
