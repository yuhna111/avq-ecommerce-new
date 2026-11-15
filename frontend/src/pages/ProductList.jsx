import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { api } from '../api'
import { FIT_TYPE, money } from '../format'

const SIZES = ['S', 'M', 'L', 'XL', '29', '30', '31', '32']

// UC04 xem danh sách, UC05 tìm kiếm & lọc (không cần đăng nhập - BR03)
export default function ProductList() {
  const [params, setParams] = useSearchParams()
  const [categories, setCategories] = useState([])
  const [products, setProducts] = useState(null)
  const [error, setError] = useState('')
  const [keyword, setKeyword] = useState(params.get('keyword') || '')

  const filter = Object.fromEntries(params.entries())

  useEffect(() => {
    api('/categories').then(setCategories).catch(() => {})
  }, [])

  useEffect(() => {
    setProducts(null)
    api('/products', { params: filter })
      .then((d) => {
        setProducts(d)
        setError('')
      })
      .catch((e) => setError(e.message))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [params.toString()])

  const set = (key, value) => {
    const next = new URLSearchParams(params)
    if (value === '' || value === null || value === undefined) next.delete(key)
    else next.set(key, value)
    setParams(next)
  }

  const submitSearch = (e) => {
    e.preventDefault()
    set('keyword', keyword.trim())
  }

  return (
    <div className="shop">
      <aside className="filters card">
        <h3>Bộ lọc</h3>
        <label className="field">
          <span>Danh mục</span>
          <select value={filter.categoryId || ''} onChange={(e) => set('categoryId', e.target.value)}>
            <option value="">Tất cả</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>{c.name}</option>
            ))}
          </select>
        </label>
        <div className="field">
          <span>Khoảng giá (đ)</span>
          <div className="row gap-sm">
            <input type="number" min="0" step="10000" placeholder="Từ" defaultValue={filter.minPrice || ''}
              onBlur={(e) => set('minPrice', e.target.value)} />
            <input type="number" min="0" step="10000" placeholder="Đến" defaultValue={filter.maxPrice || ''}
              onBlur={(e) => set('maxPrice', e.target.value)} />
          </div>
        </div>
        <div className="field">
          <span>Size</span>
          <div className="chips">
            {SIZES.map((s) => (
              <button key={s} type="button" className={'chip' + (filter.size === s ? ' active' : '')}
                onClick={() => set('size', filter.size === s ? '' : s)}>{s}</button>
            ))}
          </div>
        </div>
        <label className="field">
          <span>Màu</span>
          <input placeholder="VD: Đen, Trắng..." defaultValue={filter.color || ''} onBlur={(e) => set('color', e.target.value.trim())} />
        </label>
        <label className="field">
          <span>Phom dáng</span>
          <select value={filter.fitType || ''} onChange={(e) => set('fitType', e.target.value)}>
            <option value="">Tất cả</option>
            {Object.entries(FIT_TYPE).map(([k, v]) => (
              <option key={k} value={k}>{v}</option>
            ))}
          </select>
        </label>
        <button className="btn btn-ghost btn-block" onClick={() => { setKeyword(''); setParams({}) }}>Xóa bộ lọc</button>
      </aside>

      <section>
        <div className="toolbar">
          <form className="search" onSubmit={submitSearch}>
            <input value={keyword} onChange={(e) => setKeyword(e.target.value)} placeholder="Tìm áo thun, sơ mi, jean..." />
            <button className="btn btn-primary">Tìm</button>
          </form>
          <select value={filter.sort || ''} onChange={(e) => set('sort', e.target.value)}>
            <option value="">Mới nhất</option>
            <option value="price_asc">Giá tăng dần</option>
            <option value="price_desc">Giá giảm dần</option>
          </select>
        </div>
        {error && <div className="alert alert-error">{error}</div>}
        {products === null && !error && <div className="muted">Đang tải sản phẩm...</div>}
        {products && products.length === 0 && <div className="empty">Không có sản phẩm phù hợp với bộ lọc.</div>}
        <div className="grid">
          {products?.map((p) => (
            <Link key={p.id} to={'/san-pham/' + p.id} className="product-card card">
              <div className="thumb">
                {p.mainImage ? <img src={p.mainImage} alt={p.name} /> : <div className="no-img">AVQ</div>}
                {!p.inStock && <span className="tag-out">Hết hàng</span>}
              </div>
              <div className="pc-body">
                <div className="muted small">{p.categoryName} · {FIT_TYPE[p.fitType]}</div>
                <div className="pc-name">{p.name}</div>
                <div className="pc-price">{money(p.price)}</div>
                <div className="muted small">Size: {p.sizes.join(', ')}</div>
              </div>
            </Link>
          ))}
        </div>
      </section>
    </div>
  )
}
