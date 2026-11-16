import { useEffect, useRef, useState } from 'react'
import { api, uploadImage } from '../api'
import { FIT_TYPE, money } from '../format'

// UC17 thêm sản phẩm, UC18 sửa giá/mô tả & ẩn/hiện, UC19 cập nhật tồn kho
export default function StaffProducts() {
  const [products, setProducts] = useState(null)
  const [categories, setCategories] = useState([])
  const [open, setOpen] = useState(null)
  const [creating, setCreating] = useState(false)
  const [msg, setMsg] = useState(null)

  const load = () => api('/admin/products').then(setProducts).catch((e) => setMsg({ type: 'error', text: e.message }))
  useEffect(() => {
    load()
    api('/categories').then(setCategories)
  }, [])

  const notify = (type, text) => {
    setMsg({ type, text })
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  const toggleVisible = async (p) => {
    try {
      await api('/admin/products/' + p.id, { method: 'PUT', body: { visible: !p.visible } })
      notify('success', (p.visible ? 'Đã ẩn ' : 'Đã hiển thị ') + p.name)
      load()
    } catch (e) {
      notify('error', e.message)
    }
  }

  return (
    <div>
      <div className="row between wrap">
        <h1>Sản phẩm &amp; tồn kho</h1>
        <button className="btn btn-primary" onClick={() => setCreating(!creating)}>{creating ? 'Đóng' : '+ Thêm sản phẩm'}</button>
      </div>
      {msg && <div className={'alert alert-' + msg.type}>{msg.text}</div>}
      {creating && (
        <CreateProduct categories={categories} products={products || []} onDone={(p) => { setCreating(false); notify('success', 'Đã thêm sản phẩm ' + p.name); load() }}
          onError={(t) => notify('error', t)} />
      )}
      {!products ? <div className="muted">Đang tải...</div> : (
        <div className="card table-wrap">
          <table className="table">
            <thead><tr><th></th><th>Sản phẩm</th><th>Danh mục</th><th>Giá</th><th>Tồn kho</th><th>Trạng thái</th><th></th></tr></thead>
            <tbody>
              {products.map((p) => (
                <ProductRow key={p.id} p={p} open={open === p.id} onToggle={() => setOpen(open === p.id ? null : p.id)}
                  onVisible={() => toggleVisible(p)} onSaved={(t) => { notify('success', t); load() }} onError={(t) => notify('error', t)} />
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}

function ProductRow({ p, open, onToggle, onVisible, onSaved, onError }) {
  const [edit, setEdit] = useState({ price: p.price, description: p.description || '', material: p.material || '' })
  const [stocks, setStocks] = useState({})
  const totalStock = p.variants.reduce((s, v) => s + v.stockQty, 0)

  useEffect(() => {
    setEdit({ price: p.price, description: p.description || '', material: p.material || '' })
    setStocks(Object.fromEntries(p.variants.map((v) => [v.id, v.stockQty])))
  }, [p])

  const saveInfo = async () => {
    try {
      await api('/admin/products/' + p.id, { method: 'PUT', body: { price: Number(edit.price), description: edit.description, material: edit.material } })
      onSaved('Đã lưu thông tin ' + p.name)
    } catch (e) {
      onError(e.message)
    }
  }

  const saveStock = async (v) => {
    try {
      await api(`/admin/variants/${v.id}/stock`, { method: 'PUT', params: { stock: stocks[v.id] } })
      onSaved(`Đã cập nhật tồn kho ${v.sku}: ${v.stockQty} → ${stocks[v.id]}`)
    } catch (e) {
      onError(e.message)
    }
  }

  return (
    <>
      <tr className={p.visible ? '' : 'row-muted'}>
        <td>{p.images[0] ? <img className="mini" src={p.images[0]} alt="" /> : null}</td>
        <td><b>{p.name}</b><div className="muted small">{FIT_TYPE[p.fitType]} · {p.variants.length} biến thể</div></td>
        <td>{p.category.name}</td>
        <td>{money(p.price)}</td>
        <td>{totalStock}</td>
        <td>{p.visible ? <span className="badge badge-green">Đang bán</span> : <span className="badge badge-gray">Đang ẩn</span>}</td>
        <td className="nowrap">
          <button className="btn btn-ghost btn-sm" onClick={onToggle}>{open ? 'Thu gọn' : 'Sửa / tồn kho'}</button>
          <button className="btn btn-ghost btn-sm" onClick={onVisible}>{p.visible ? 'Ẩn' : 'Hiện'}</button>
        </td>
      </tr>
      {open && (
        <tr className="row-detail">
          <td colSpan={7}>
            <div className="edit-grid">
              <div>
                <h4>Thông tin bán hàng</h4>
                <label className="field"><span>Giá (đ)</span>
                  <input type="number" min="1" value={edit.price} onChange={(e) => setEdit({ ...edit, price: e.target.value })} /></label>
                <label className="field"><span>Chất liệu</span>
                  <input value={edit.material} onChange={(e) => setEdit({ ...edit, material: e.target.value })} /></label>
                <label className="field"><span>Mô tả</span>
                  <textarea rows={3} value={edit.description} onChange={(e) => setEdit({ ...edit, description: e.target.value })} /></label>
                <button className="btn btn-primary btn-sm" onClick={saveInfo}>Lưu thông tin</button>
                <p className="muted small">Mỗi thay đổi được ghi vào nhật ký product_change_logs.</p>
              </div>
              <div>
                <h4>Tồn kho theo biến thể</h4>
                <table className="table compact">
                  <thead><tr><th>SKU</th><th>Size</th><th>Màu</th><th>Tồn</th><th></th></tr></thead>
                  <tbody>
                    {p.variants.map((v) => (
                      <tr key={v.id}>
                        <td className="small">{v.sku}</td><td>{v.size}</td><td>{v.color}</td>
                        <td><input className="stock-input" type="number" min="0" value={stocks[v.id] ?? v.stockQty}
                          onChange={(e) => setStocks({ ...stocks, [v.id]: e.target.value })} /></td>
                        <td><button className="btn btn-ghost btn-sm" disabled={Number(stocks[v.id]) === v.stockQty} onClick={() => saveStock(v)}>Lưu</button></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </td>
        </tr>
      )}
    </>
  )
}

// ===== Tự sinh SKU theo quy ước dữ liệu demo: AVQ-<mã danh mục><số thứ tự>-<mã màu>-<size>, VD AVQ-TS01-TRG-S =====
const CAT_CODE = { 'ao-thun': 'TS', 'ao-so-mi': 'SM', 'quan-jean': 'QJ', 'ao-khoac': 'AK', 'vay-dam': 'VD' }
const COLOR_CODE = { 'Trắng': 'TRG', 'Đen': 'DEN', 'Xám': 'XAM', 'Xanh nhạt': 'XNH', 'Be': 'BE', 'Xanh đậm': 'XDA', 'Rêu': 'REU', 'Hồng': 'HONG', 'Xanh': 'XANH' }
const plain = (s) => s.normalize('NFD').replace(/[̀-ͯ]/g, '').replace(/đ/gi, 'd').toUpperCase()
const catCode = (c) => (c ? CAT_CODE[c.slug] || plain(c.slug).split('-').map((w) => w[0]).join('').slice(0, 3) : 'SP')
const colorCode = (color) => {
  const c = color.trim()
  if (!c) return ''
  if (COLOR_CODE[c]) return COLOR_CODE[c]
  const words = plain(c).replace(/[^A-Z\s]/g, '').split(/\s+/).filter(Boolean)
  return words.length === 1 ? words[0].slice(0, 4) : words.map((w) => w[0]).join('') + words[words.length - 1].slice(1, 2)
}
/** Số thứ tự kế tiếp trong danh mục, dựa trên các SKU đã có (AVQ-TS01-..., AVQ-TS02-... → 03). */
const nextNumber = (code, products) => {
  const re = new RegExp('^AVQ-' + code + '(\\d+)-')
  const used = products.flatMap((p) => p.variants || []).map((v) => re.exec(v.sku)).filter(Boolean).map((m) => Number(m[1]))
  return String((used.length ? Math.max(...used) : 0) + 1).padStart(2, '0')
}

function CreateProduct({ categories, products, onDone, onError }) {
  const [f, setF] = useState({ name: '', categoryId: '', price: '', fitType: 'REGULAR', material: '', description: '' })
  const [images, setImages] = useState([]) // đường dẫn ảnh; ảnh đầu tiên là ảnh chính
  const [uploading, setUploading] = useState(0)
  const [imgError, setImgError] = useState('')
  const [urlInput, setUrlInput] = useState('')
  const fileRef = useRef(null)

  const onFiles = async (e) => {
    const files = [...e.target.files]
    e.target.value = ''
    setImgError('')
    for (const file of files) {
      if (file.size > 5 * 1024 * 1024) { setImgError(`"${file.name}" vượt quá 5 MB`); continue }
      setUploading((n) => n + 1)
      try {
        const url = await uploadImage(file)
        setImages((list) => [...list, url])
      } catch (err) {
        setImgError(`"${file.name}": ${err.message}`)
      } finally {
        setUploading((n) => n - 1)
      }
    }
  }
  const addUrl = () => {
    const u = urlInput.trim()
    if (u) { setImages([...images, u]); setUrlInput('') }
  }
  const makeMain = (i) => setImages([images[i], ...images.filter((_, j) => j !== i)])
  const [variants, setVariants] = useState([{ size: 'M', color: '', sku: '', manual: false, stock: 10 }])
  const [busy, setBusy] = useState(false)

  const setV = (i, k, val) => setVariants(variants.map((v, j) => (j === i ? { ...v, [k]: val } : v)))
  const category = categories.find((c) => String(c.id) === String(f.categoryId))
  const prefix = category ? `AVQ-${catCode(category)}${nextNumber(catCode(category), products)}` : ''
  const autoSku = (v) => (prefix && v.color.trim() && v.size.trim() ? `${prefix}-${colorCode(v.color)}-${plain(v.size).replace(/\s+/g, '')}` : '')
  const skuOf = (v) => (v.manual ? v.sku : autoSku(v))

  const submit = async (e) => {
    e.preventDefault()
    setBusy(true)
    try {
      const p = await api('/admin/products', {
        method: 'POST',
        body: {
          ...f,
          price: Number(f.price),
          categoryId: Number(f.categoryId),
          imageUrls: images,
          variants: variants.map((v) => ({ size: v.size, color: v.color, sku: skuOf(v), stock: Number(v.stock) })),
        },
      })
      onDone(p)
    } catch (err) {
      onError(err.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <form className="card" onSubmit={submit}>
      <h3>Thêm sản phẩm mới</h3>
      <div className="row gap-sm wrap">
        <label className="field grow"><span>Tên sản phẩm</span><input required value={f.name} onChange={(e) => setF({ ...f, name: e.target.value })} /></label>
        <label className="field"><span>Danh mục</span>
          <select required value={f.categoryId} onChange={(e) => setF({ ...f, categoryId: e.target.value })}>
            <option value="">-- Chọn --</option>
            {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
          </select></label>
        <label className="field"><span>Giá (đ)</span><input required type="number" min="1" value={f.price} onChange={(e) => setF({ ...f, price: e.target.value })} /></label>
        <label className="field"><span>Phom</span>
          <select value={f.fitType} onChange={(e) => setF({ ...f, fitType: e.target.value })}>
            {Object.entries(FIT_TYPE).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
          </select></label>
      </div>
      <div className="row gap-sm wrap">
        <label className="field grow"><span>Chất liệu</span><input value={f.material} onChange={(e) => setF({ ...f, material: e.target.value })} /></label>
      </div>
      <div className="field">
        <span>Ảnh sản phẩm (JPEG, PNG, WebP, tối đa 5 MB; ảnh đầu tiên là ảnh chính)</span>
        <div className="img-picker">
          {images.map((u, i) => (
            <div key={u + i} className={'img-tile' + (i === 0 ? ' is-main' : '')}>
              <img src={u} alt="" />
              {i === 0 ? <span className="img-badge">Ảnh chính</span>
                : <button type="button" className="img-act" onClick={() => makeMain(i)}>Đặt làm ảnh chính</button>}
              <button type="button" className="img-del" aria-label="Bỏ ảnh" onClick={() => setImages(images.filter((_, j) => j !== i))}>×</button>
            </div>
          ))}
          <button type="button" className="img-add" onClick={() => fileRef.current?.click()} disabled={uploading > 0}>
            <span className="img-add-plus">+</span>
            {uploading > 0 ? 'Đang tải lên...' : 'Tải ảnh từ máy'}
          </button>
          <input ref={fileRef} type="file" accept="image/jpeg,image/png,image/webp" multiple hidden onChange={onFiles} />
        </div>
        {imgError && <div className="alert alert-error">{imgError}</div>}
        <div className="row gap-sm img-url">
          <input placeholder="Hoặc dán đường dẫn ảnh, ví dụ /images/products/p1.jpg" value={urlInput} onChange={(e) => setUrlInput(e.target.value)}
            onKeyDown={(e) => { if (e.key === 'Enter') { e.preventDefault(); addUrl() } }} />
          <button type="button" className="btn btn-ghost btn-sm" onClick={addUrl}>Thêm</button>
        </div>
      </div>
      <label className="field"><span>Mô tả</span><textarea rows={2} value={f.description} onChange={(e) => setF({ ...f, description: e.target.value })} /></label>
      <h4>Biến thể (ít nhất một)</h4>
      <p className="muted small">SKU tự sinh theo danh mục, màu và size (ví dụ AVQ-TS03-TRG-M). Có thể sửa tay nếu cần; SKU trùng sẽ bị từ chối (BR17).</p>
      <datalist id="known-colors">{Object.keys(COLOR_CODE).map((c) => <option key={c} value={c} />)}</datalist>
      {variants.map((v, i) => (
        <div key={i} className="row gap-sm wrap variant-row">
          <input required placeholder="Size" value={v.size} onChange={(e) => setV(i, 'size', e.target.value)} className="w-sm" />
          <input required placeholder="Màu" list="known-colors" value={v.color} onChange={(e) => setV(i, 'color', e.target.value)} />
          <input required placeholder={prefix ? 'SKU (nhập size và màu)' : 'SKU (chọn danh mục trước)'} value={skuOf(v)}
            className={v.manual ? '' : 'sku-auto'} title={v.manual ? 'SKU nhập tay' : 'SKU tự sinh'}
            onChange={(e) => setVariants(variants.map((x, j) => (j === i ? { ...x, sku: e.target.value, manual: true } : x)))} />
          {v.manual && <button type="button" className="link-btn small" onClick={() => setVariants(variants.map((x, j) => (j === i ? { ...x, manual: false } : x)))}>Tự sinh lại</button>}
          <input required type="number" min="0" placeholder="Tồn" value={v.stock} onChange={(e) => setV(i, 'stock', e.target.value)} className="w-sm" />
          {variants.length > 1 && <button type="button" className="link-danger" onClick={() => setVariants(variants.filter((_, j) => j !== i))}>Xóa</button>}
        </div>
      ))}
      <div className="row gap">
        <button type="button" className="btn btn-ghost btn-sm" onClick={() => setVariants([...variants, { size: '', color: variants[variants.length - 1]?.color || '', sku: '', manual: false, stock: 10 }])}>+ Thêm biến thể</button>
        <button className="btn btn-primary" disabled={busy}>Lưu sản phẩm</button>
      </div>
    </form>
  )
}
