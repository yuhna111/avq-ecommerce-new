import { useEffect, useMemo, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { api } from '../api'
import { useAuth } from '../auth'
import { FIT_PREF, FIT_TYPE, FREE_SHIP_FROM, SHIPPING_FEE, colorHex, money } from '../format'

// UC06 xem chi tiết, UC07 thêm vào giỏ (DT-02), UC06a gợi ý size (FB-01)
export default function ProductDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { isCustomer, isStaff, refreshCart } = useAuth()
  const [p, setP] = useState(null)
  const [error, setError] = useState('')
  const [color, setColor] = useState('')
  const [size, setSize] = useState('')
  const [qty, setQty] = useState(1)
  const [msg, setMsg] = useState(null)
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    setP(null)
    setSize('')
    setMsg(null)
    api('/products/' + id)
      .then((d) => {
        setP(d)
        // Chọn sẵn màu đầu tiên còn hàng; size khách vẫn phải tự chọn (DT-02 R2)
        const first = d.variants.find((v) => v.stockQty > 0) || d.variants[0]
        setColor(first ? first.color : '')
      })
      .catch((e) => setError(e.message))
  }, [id])

  const colors = useMemo(() => (p ? [...new Set(p.variants.map((v) => v.color))] : []), [p])
  const sizes = useMemo(() => (p ? [...new Set(p.variants.map((v) => v.size))] : []), [p])
  const variant = p?.variants.find((v) => v.color === color && v.size === size)
  const stockOf = (c, s) => p?.variants.find((v) => v.color === c && v.size === s)?.stockQty ?? 0
  const colorInStock = (c) => p?.variants.some((v) => v.color === c && v.stockQty > 0)

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

  const pickColor = (c) => {
    setColor(c)
    if (size && stockOf(c, size) === 0) setSize('')
  }
  const toAdvisor = (e) => {
    e.preventDefault()
    document.getElementById('goi-y-size')?.scrollIntoView({ behavior: 'smooth', block: 'center' })
  }

  return (
    <div className="pdp">
      <nav className="crumbs" aria-label="Vị trí">
        <Link to="/">Trang chủ</Link>
        <span aria-hidden="true">/</span>
        <Link to={'/?categoryId=' + p.category.id}>{p.category.name}</Link>
        <span aria-hidden="true">/</span>
        <span>{p.name}</span>
      </nav>

      <div className="detail">
        <div className={'gallery' + (p.images.length === 1 ? ' single' : '')}>
          {p.images.length
            ? p.images.map((u, i) => (
              <div key={u} className="gallery-pic"><img src={u} alt={i === 0 ? p.name : ''} /></div>
            ))
            : <div className="gallery-pic no-img big">AVQ</div>}
        </div>

        <div className="info">
          <div className="muted small">{p.category.name} · Phom {FIT_TYPE[p.fitType]}</div>
          <h1>{p.name}</h1>
          <div className="price-lg">{money(p.price)}</div>

          <div className="opt">
            <div className="opt-label">Màu sắc: <b>{color || 'chưa chọn'}</b></div>
            <div className="swatch-row">
              {colors.map((c) => (
                <button key={c} type="button" title={c} aria-label={'Màu ' + c} aria-pressed={color === c}
                  className={'swatch-btn' + (color === c ? ' active' : '') + (colorInStock(c) ? '' : ' out')}
                  onClick={() => pickColor(c)}>
                  <span style={{ background: colorHex(c) }} />
                </button>
              ))}
            </div>
          </div>

          <div className="opt">
            <div className="opt-label row between">
              <span>Kích thước: <b>{size || 'chưa chọn'}</b></span>
              <a href="#goi-y-size" className="link-under" onClick={toAdvisor}>Bảng size &amp; gợi ý</a>
            </div>
            <div className="size-row">
              {sizes.map((s) => {
                const out = color && stockOf(color, s) === 0
                return (
                  <button key={s} type="button" className={'size-btn' + (size === s ? ' active' : '') + (out ? ' out' : '')}
                    disabled={!!out} aria-pressed={size === s} onClick={() => setSize(s)}
                    title={out ? `Size ${s} màu ${color} đã hết hàng` : ''}>{s}</button>
                )
              })}
            </div>
            <div className="stock-line">
              {variant ? (
                variant.stockQty > 0 ? <span>Còn <b>{variant.stockQty}</b> sản phẩm size {variant.size} màu {variant.color}</span>
                  : <span className="text-error">Size {variant.size} màu {variant.color} đã hết hàng</span>
              ) : <span className="muted">Chọn màu và size để xem tồn kho</span>}
            </div>
          </div>

          {!isStaff && (
            <div className="buy-row">
              <div className="qty">
                <button type="button" aria-label="Giảm số lượng" onClick={() => setQty(Math.max(1, qty - 1))}>−</button>
                <input type="number" min="1" max="10" value={qty} aria-label="Số lượng"
                  onChange={(e) => setQty(Math.max(1, Math.min(10, parseInt(e.target.value || '1', 10))))} />
                <button type="button" aria-label="Tăng số lượng" onClick={() => setQty(Math.min(10, qty + 1))}>+</button>
              </div>
              <button className="btn btn-primary btn-lg grow" disabled={busy} onClick={addToCart}>Thêm vào giỏ</button>
            </div>
          )}
          {msg && (
            <div className={'alert alert-' + msg.type}>
              {msg.text} {msg.type === 'success' && <Link to="/gio-hang">Xem giỏ hàng →</Link>}
            </div>
          )}

          <ul className="perks">
            <li><b>Giao hàng {money(SHIPPING_FEE)}</b>, miễn phí cho đơn từ {money(FREE_SHIP_FROM)}</li>
            <li>Thanh toán khi nhận hàng (COD) hoặc trực tuyến</li>
          </ul>

          <div className="accordion">
            <details open>
              <summary>Mô tả</summary>
              <p>{p.description || 'Chưa có mô tả.'}</p>
            </details>
            <details>
              <summary>Chất liệu &amp; phom dáng</summary>
              <dl className="specs">
                <dt>Chất liệu</dt><dd>{p.material || '—'}</dd>
                <dt>Phom</dt><dd>{FIT_TYPE[p.fitType]}</dd>
                <dt>Màu</dt><dd>{colors.join(', ')}</dd>
                <dt>Size</dt><dd>{sizes.join(', ')}</dd>
              </dl>
            </details>
            <details>
              <summary>Giao hàng &amp; thanh toán</summary>
              <ul>
                <li>Phí giao {money(SHIPPING_FEE)}; đơn có tạm tính từ {money(FREE_SHIP_FROM)} được miễn phí.</li>
                <li>COD: đơn chuyển ngay sang "Chờ xác nhận".</li>
                <li>Trực tuyến: đơn được giữ 15 phút để thanh toán, quá hạn tự hủy và hoàn kho.</li>
              </ul>
            </details>
          </div>

          <SizeAdvisor product={p} isCustomer={isCustomer} onPick={setSize} />
        </div>
      </div>

      {!isStaff && (
        <div className="sticky-buy" aria-hidden="true">
          <div>
            <div className="sb-name">{p.name}</div>
            <div className="sb-price">{money(p.price)}{size ? ' · Size ' + size : ''}</div>
          </div>
          <button className="btn btn-primary" tabIndex={-1} disabled={busy} onClick={addToCart}>Thêm vào giỏ</button>
        </div>
      )}
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
    <form id="goi-y-size" className="advisor" onSubmit={submit}>
      <h3>Gợi ý size cho bạn</h3>
      <p className="muted small">
        {product.hasSizeChart
          ? 'Nhập số đo, AVQ chọn size theo bảng số đo của mẫu này.'
          : 'Mẫu này chưa có bảng số đo; bạn vẫn có thể gửi số đo để được hướng dẫn.'}
      </p>
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
