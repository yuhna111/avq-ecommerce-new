import { useEffect, useMemo, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { api } from '../api'
import { useAuth } from '../auth'
import { FIT_PREF, FIT_TYPE, money } from '../format'

// UC06 xem chi tiết, UC07 thêm vào giỏ (DT-02), UC06a gợi ý size (FB-01)
export default function ProductDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { isCustomer, isStaff, refreshCart } = useAuth()
  const [p, setP] = useState(null)
  const [error, setError] = useState('')
  const [img, setImg] = useState(0)
  const [color, setColor] = useState('')
  const [size, setSize] = useState('')
  const [qty, setQty] = useState(1)
  const [msg, setMsg] = useState(null)
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    api('/products/' + id).then(setP).catch((e) => setError(e.message))
  }, [id])

  const colors = useMemo(() => (p ? [...new Set(p.variants.map((v) => v.color))] : []), [p])
  const sizes = useMemo(() => (p ? [...new Set(p.variants.map((v) => v.size))] : []), [p])
  const variant = p?.variants.find((v) => v.color === color && v.size === size)
  const stockOf = (c, s) => p?.variants.find((v) => v.color === c && v.size === s)?.stockQty ?? 0

  if (error) return <div className="alert alert-error">{error} <Link to="/">Quay lại</Link></div>
  if (!p) return <div className="muted">Đang tải...</div>

  const addToCart = async () => {
    setMsg(null)
    if (!isCustomer) {
      // DT-02 R1: chưa đăng nhập -> chuyển trang đăng nhập
      navigate('/dang-nhap?next=' + encodeURIComponent('/san-pham/' + id))
      return
    }
    setBusy(true)
    try {
      // Gửi variantId rỗng khi chưa chọn đủ size+màu để backend trả thông báo R2
      const cart = await api('/cart/items', { method: 'POST', params: { variantId: variant?.id, qty } })
      setMsg({ type: 'success', text: `Đã thêm vào giỏ (${cart.totalQuantity} sản phẩm trong giỏ).` })
      refreshCart()
    } catch (e) {
      setMsg({ type: 'error', text: e.message })
    } finally {
      setBusy(false)
    }
  }

  return (
    <div>
      <Link to="/" className="back">← Tiếp tục mua sắm</Link>
      <div className="detail">
        <div className="gallery card">
          <div className="gallery-main">
            {p.images.length ? <img src={p.images[img]} alt={p.name} /> : <div className="no-img big">AVQ</div>}
          </div>
          <div className="thumbs">
            {p.images.map((u, i) => (
              <button key={u} className={'thumb-btn' + (i === img ? ' active' : '')} onClick={() => setImg(i)}>
                <img src={u} alt="" />
              </button>
            ))}
          </div>
        </div>

        <div className="info">
          <div className="muted">{p.category.name} · Phom {FIT_TYPE[p.fitType]}</div>
          <h1>{p.name}</h1>
          <div className="price-lg">{money(p.price)}</div>
          <p>{p.description}</p>
          {p.material && <p className="muted">Chất liệu: {p.material}</p>}

          <div className="field">
            <span>Màu sắc</span>
            <div className="chips">
              {colors.map((c) => (
                <button key={c} className={'chip' + (color === c ? ' active' : '')} onClick={() => setColor(c)}>{c}</button>
              ))}
            </div>
          </div>
          <div className="field">
            <span>Size</span>
            <div className="chips">
              {sizes.map((s) => {
                const out = color && stockOf(color, s) === 0
                return (
                  <button key={s} className={'chip' + (size === s ? ' active' : '') + (out ? ' disabled' : '')}
                    onClick={() => setSize(s)} title={out ? 'Hết hàng' : ''}>{s}</button>
                )
              })}
            </div>
          </div>
          <div className="stock-line">
            {variant ? (
              variant.stockQty > 0 ? <span>Còn <b>{variant.stockQty}</b> sản phẩm size {variant.size} màu {variant.color}</span>
                : <span className="text-error">Size {variant.size} màu {variant.color} đã hết hàng</span>
            ) : <span className="muted">Chọn màu và size để xem tồn kho</span>}
          </div>

          {!isStaff && (
            <div className="row gap">
              <div className="qty">
                <button onClick={() => setQty(Math.max(1, qty - 1))}>−</button>
                <input type="number" min="1" max="10" value={qty}
                  onChange={(e) => setQty(Math.max(1, Math.min(10, parseInt(e.target.value || '1', 10))))} />
                <button onClick={() => setQty(Math.min(10, qty + 1))}>+</button>
              </div>
              <button className="btn btn-primary btn-lg" disabled={busy} onClick={addToCart}>Thêm vào giỏ</button>
            </div>
          )}
          {msg && (
            <div className={'alert alert-' + msg.type}>
              {msg.text} {msg.type === 'success' && <Link to="/gio-hang">Xem giỏ hàng →</Link>}
            </div>
          )}

          <SizeAdvisor product={p} isCustomer={isCustomer} onPick={setSize} />
        </div>
      </div>
    </div>
  )
}

function SizeAdvisor({ product, isCustomer, onPick }) {
  const [form, setForm] = useState({ heightCm: '', weightKg: '', fitPref: 'VUA', saveProfile: false })
  const [result, setResult] = useState(null)
  const [error, setError] = useState('')

  const submit = async (e) => {
    e.preventDefault()
    setError('')
    setResult(null)
    try {
      const r = await api(`/products/${product.id}/size-advice`, {
        method: 'POST',
        body: { ...form, heightCm: Number(form.heightCm), weightKg: Number(form.weightKg) },
      })
      setResult(r)
      if (r.size) onPick(r.size)
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <form className="advisor card" onSubmit={submit}>
      <h3>Gợi ý size cho bạn</h3>
      <div className="row gap-sm wrap">
        <label className="field grow">
          <span>Chiều cao (cm)</span>
          <input type="number" required min="100" max="220" value={form.heightCm}
            onChange={(e) => setForm({ ...form, heightCm: e.target.value })} />
        </label>
        <label className="field grow">
          <span>Cân nặng (kg)</span>
          <input type="number" required min="25" max="150" value={form.weightKg}
            onChange={(e) => setForm({ ...form, weightKg: e.target.value })} />
        </label>
        <label className="field grow">
          <span>Kiểu mặc</span>
          <select value={form.fitPref} onChange={(e) => setForm({ ...form, fitPref: e.target.value })}>
            {Object.entries(FIT_PREF).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
          </select>
        </label>
      </div>
      {isCustomer && (
        <label className="check">
          <input type="checkbox" checked={form.saveProfile} onChange={(e) => setForm({ ...form, saveProfile: e.target.checked })} />
          Tôi đồng ý lưu số đo cho lần sau
        </label>
      )}
      <button className="btn btn-outline">Gợi ý size</button>
      {error && <div className="alert alert-error">{error}</div>}
      {result && (
        <div className={'alert ' + (result.size ? 'alert-success' : 'alert-info')}>
          {result.size ? <>Size gợi ý: <b className="big-size">{result.size}</b> — {result.message}</> : result.message}
        </div>
      )}
    </form>
  )
}
