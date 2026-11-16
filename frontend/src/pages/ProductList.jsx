import { useEffect, useMemo, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { api } from '../api'
import { FIT_TYPE, FREE_SHIP_FROM, Swatch, money } from '../format'

const SIZES = ['S', 'M', 'L', 'XL', '29', '30', '31', '32']
const PRICE_RANGES = [
  { key: '', label: 'Mọi mức giá' },
  { key: '0-300000', label: 'Dưới 300.000 đ' },
  { key: '300000-500000', label: '300.000 – 500.000 đ' },
  { key: '500000-', label: 'Từ 500.000 đ' },
]
const FILTER_KEYS = ['keyword', 'categoryId', 'size', 'color', 'fitType', 'minPrice', 'maxPrice', 'sort']

// UC04 xem danh sách, UC05 tìm kiếm & lọc (không cần đăng nhập - BR03)
export default function ProductList() {
  const [params, setParams] = useSearchParams()
  const [categories, setCategories] = useState([])
  const [all, setAll] = useState([]) // toàn bộ sản phẩm đang bán: dùng cho ô danh mục và danh sách màu
  const [products, setProducts] = useState(null)
  const [error, setError] = useState('')

  const filter = Object.fromEntries(params.entries())
  const isHome = !FILTER_KEYS.some((k) => filter[k])

  useEffect(() => {
    api('/categories').then(setCategories).catch(() => {})
    api('/products').then(setAll).catch(() => {})
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

  const set = (changes) => {
    const next = new URLSearchParams(params)
    Object.entries(changes).forEach(([key, value]) => {
      if (value === '' || value === null || value === undefined) next.delete(key)
      else next.set(key, value)
    })
    setParams(next)
  }

  // Ô danh mục: chỉ danh mục còn sản phẩm đang bán, ảnh lấy từ sản phẩm đầu tiên
  const tiles = useMemo(() => categories
    .map((c) => {
      const items = all.filter((p) => p.categoryName === c.name)
      return { ...c, count: items.length, image: items.find((p) => p.mainImage)?.mainImage }
    })
    .filter((c) => c.count > 0), [categories, all])
  const colors = useMemo(() => [...new Set(all.flatMap((p) => p.colors))], [all])

  const priceKey = filter.minPrice || filter.maxPrice ? `${filter.minPrice || 0}-${filter.maxPrice || ''}` : ''
  const setPrice = (key) => {
    const [min, max] = key ? key.split('-') : ['', '']
    set({ minPrice: min && min !== '0' ? min : '', maxPrice: max || '' })
  }
  const activeCat = categories.find((c) => String(c.id) === filter.categoryId)
  const title = filter.keyword ? `Kết quả cho "${filter.keyword}"` : activeCat ? activeCat.name : 'Tất cả sản phẩm'
  const hasFilter = ['size', 'color', 'fitType', 'minPrice', 'maxPrice'].some((k) => filter[k])

  return (
    <div className="shop">
      {isHome && (
        <>
          <section className="hero">
            <div className="hero-text">
              <span className="eyebrow">Áo thun · Sơ mi · Jean · Áo khoác · Đầm</span>
              <h1>Đồ mặc hằng ngày, chọn đúng size ngay lần đầu</h1>
              <p>Nhập chiều cao và cân nặng ở trang sản phẩm, AVQ gợi ý size theo bảng số đo của từng mẫu. Miễn phí giao hàng cho đơn từ {money(FREE_SHIP_FROM)}.</p>
              <div className="row gap wrap">
                <a href="#san-pham" className="btn btn-primary btn-lg">Mua sắm ngay</a>
                {tiles[0] && <Link to={'/?categoryId=' + tiles[0].id} className="btn btn-ghost btn-lg">Xem {tiles[0].name.toLowerCase()}</Link>}
              </div>
            </div>
            <div className="hero-art" aria-hidden="true">
              {all.slice(0, 3).map((p, i) => (
                <div key={p.id} className={'hero-pic hero-pic-' + i}>{p.mainImage && <img src={p.mainImage} alt="" />}</div>
              ))}
            </div>
          </section>

          <section className="cat-tiles" aria-label="Danh mục">
            {tiles.map((c) => (
              <Link key={c.id} to={'/?categoryId=' + c.id} className="cat-tile">
                <div className="cat-pic">{c.image && <img src={c.image} alt="" />}</div>
                <div className="cat-name">{c.name}</div>
                <div className="muted small">{c.count} mẫu</div>
              </Link>
            ))}
          </section>
        </>
      )}

      <section id="san-pham" className="listing">
        <div className="listing-head">
          <div>
            <h2>{title}</h2>
            <div className="muted small">{products ? products.length + ' sản phẩm' : 'Đang tải...'}</div>
          </div>
          <label className="sort">
            <span className="muted small">Sắp xếp</span>
            <select value={filter.sort || ''} onChange={(e) => set({ sort: e.target.value })}>
              <option value="">Mới nhất</option>
              <option value="price_asc">Giá tăng dần</option>
              <option value="price_desc">Giá giảm dần</option>
            </select>
          </label>
        </div>

        <div className="pill-row" role="group" aria-label="Lọc theo danh mục">
          <button type="button" className={'pill' + (!filter.categoryId ? ' active' : '')} onClick={() => set({ categoryId: '' })}>Tất cả</button>
          {tiles.map((c) => (
            <button key={c.id} type="button" className={'pill' + (filter.categoryId === String(c.id) ? ' active' : '')}
              onClick={() => set({ categoryId: String(c.id) })}>{c.name}</button>
          ))}
        </div>

        <div className="filter-bar">
          <div className="filter-sizes" role="group" aria-label="Lọc theo size">
            {SIZES.map((s) => (
              <button key={s} type="button" className={'chip' + (filter.size === s ? ' active' : '')}
                onClick={() => set({ size: filter.size === s ? '' : s })}>{s}</button>
            ))}
          </div>
          <select aria-label="Màu" value={filter.color || ''} onChange={(e) => set({ color: e.target.value })}>
            <option value="">Mọi màu</option>
            {colors.map((c) => <option key={c} value={c}>{c}</option>)}
          </select>
          <select aria-label="Phom dáng" value={filter.fitType || ''} onChange={(e) => set({ fitType: e.target.value })}>
            <option value="">Mọi phom</option>
            {Object.entries(FIT_TYPE).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
          </select>
          <select aria-label="Khoảng giá" value={priceKey} onChange={(e) => setPrice(e.target.value)}>
            {PRICE_RANGES.map((r) => <option key={r.key} value={r.key}>{r.label}</option>)}
            {priceKey && !PRICE_RANGES.some((r) => r.key === priceKey) && <option value={priceKey}>Giá đã chọn</option>}
          </select>
          {(hasFilter || filter.keyword) && (
            <button type="button" className="btn btn-ghost btn-sm" onClick={() => setParams(filter.categoryId ? { categoryId: filter.categoryId } : {})}>Xóa bộ lọc</button>
          )}
        </div>

        {error && <div className="alert alert-error">{error}</div>}
        {products && products.length === 0 && (
          <div className="empty">Không có sản phẩm phù hợp. <button className="link-btn" onClick={() => setParams({})}>Xem tất cả sản phẩm</button></div>
        )}
        <div className="grid">
          {products?.map((p) => <ProductCard key={p.id} p={p} />)}
        </div>
      </section>
    </div>
  )
}

function ProductCard({ p }) {
  const shown = p.colors.slice(0, 5)
  return (
    <Link to={'/san-pham/' + p.id} className="product-card">
      <div className="thumb">
        {p.mainImage ? <img src={p.mainImage} alt={p.name} loading="lazy" /> : <div className="no-img">AVQ</div>}
        {!p.inStock && <span className="tag-out">Hết hàng</span>}
      </div>
      <div className="pc-body">
        <div className="pc-name">{p.name}</div>
        <div className="pc-price">{money(p.price)}</div>
        <div className="pc-meta">
          <span className="swatches">
            {shown.map((c) => <Swatch key={c} color={c} size={14} />)}
            {p.colors.length > shown.length && <span className="muted small">+{p.colors.length - shown.length}</span>}
          </span>
          <span className="muted small">{FIT_TYPE[p.fitType]?.split(' ')[0]} · {p.sizes.join(' ')}</span>
        </div>
      </div>
    </Link>
  )
}
