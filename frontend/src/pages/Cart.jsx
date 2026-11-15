import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { api } from '../api'
import { useAuth } from '../auth'
import { money } from '../format'

// UC08 xem và sửa giỏ hàng (BR05: 1–10 và không vượt tồn kho)
export default function Cart() {
  const navigate = useNavigate()
  const { setCartCount } = useAuth()
  const [cart, setCart] = useState(null)
  const [error, setError] = useState('')

  const apply = (c) => {
    setCart(c)
    setCartCount(c.totalQuantity)
  }

  useEffect(() => {
    api('/cart').then(apply).catch((e) => setError(e.message))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const change = async (variantId, qty) => {
    setError('')
    try {
      apply(await api('/cart/items/' + variantId, { method: 'PUT', params: { qty } }))
    } catch (e) {
      setError(e.message)
    }
  }

  const remove = async (variantId) => {
    setError('')
    try {
      apply(await api('/cart/items/' + variantId, { method: 'DELETE' }))
    } catch (e) {
      setError(e.message)
    }
  }

  if (!cart) return <div className="muted">{error || 'Đang tải giỏ hàng...'}</div>
  if (cart.items.length === 0)
    return (
      <div className="empty card">
        <h2>Giỏ hàng đang trống</h2>
        <Link className="btn btn-primary" to="/">Mua sắm ngay</Link>
      </div>
    )

  return (
    <div>
      <h1>Giỏ hàng</h1>
      {error && <div className="alert alert-error">{error}</div>}
      <div className="cart-layout">
        <div className="card">
          {cart.items.map((i) => (
            <div key={i.variantId} className="cart-line">
              <Link to={'/san-pham/' + i.productId} className="cl-img">
                {i.image ? <img src={i.image} alt="" /> : <div className="no-img">AVQ</div>}
              </Link>
              <div className="cl-info">
                <Link to={'/san-pham/' + i.productId} className="cl-name">{i.productName}</Link>
                <div className="muted small">Size {i.size} · Màu {i.color} · Còn {i.stockQty}</div>
                <div>{money(i.unitPrice)}</div>
              </div>
              <div className="qty">
                <button onClick={() => change(i.variantId, i.quantity - 1)} disabled={i.quantity <= 1}>−</button>
                <input readOnly value={i.quantity} />
                <button onClick={() => change(i.variantId, i.quantity + 1)}>+</button>
              </div>
              <div className="cl-total">{money(i.lineTotal)}</div>
              <button className="link-danger" onClick={() => remove(i.variantId)}>Xóa</button>
            </div>
          ))}
        </div>
        <div className="card summary">
          <div className="sum-row"><span>Tạm tính ({cart.totalQuantity} sản phẩm)</span><b>{money(cart.subtotal)}</b></div>
          <p className="muted small">Phí giao hàng 30.000 đ, miễn phí cho đơn từ 500.000 đ. Phí chính xác hiển thị ở bước xác nhận.</p>
          <button className="btn btn-primary btn-block btn-lg" onClick={() => navigate('/thanh-toan')}>Đặt hàng</button>
        </div>
      </div>
    </div>
  )
}
