import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api'
import { ORDER_STATUS, StatusBadge, dateTime, money } from '../format'

const LOW_STOCK = 5
const DAYS = 14

// Tổng quan cho chủ cửa hàng (bổ sung 17/11). Số liệu tính từ API có sẵn: /admin/orders và /admin/products.
export default function OwnerDashboard() {
  const [orders, setOrders] = useState(null)
  const [products, setProducts] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.all([api('/admin/orders'), api('/admin/products')])
      .then(([o, p]) => { setOrders(o); setProducts(p) })
      .catch((e) => setError(e.message))
  }, [])

  const stats = useMemo(() => {
    if (!orders) return null
    const sum = (list) => list.reduce((s, o) => s + Number(o.total), 0)
    const by = (st) => orders.filter((o) => o.status === st)
    const live = orders.filter((o) => o.status !== 'DA_HUY')
    return {
      delivered: sum(by('DA_GIAO')),
      deliveredCount: by('DA_GIAO').length,
      inProgress: sum([...by('CHO_XAC_NHAN'), ...by('DANG_GIAO')]),
      inProgressCount: by('CHO_XAC_NHAN').length + by('DANG_GIAO').length,
      waitingPay: by('CHO_THANH_TOAN').length,
      cancelRate: orders.length ? Math.round((by('DA_HUY').length / orders.length) * 100) : 0,
      avg: live.length ? Math.round(sum(live) / live.length) : 0,
      byStatus: Object.keys(ORDER_STATUS).map((k) => ({ key: k, label: ORDER_STATUS[k].label, count: by(k).length })),
    }
  }, [orders])

  // Doanh thu theo ngày (đơn không bị hủy), 14 ngày gần nhất
  const daily = useMemo(() => {
    if (!orders) return []
    const days = []
    const today = new Date(); today.setHours(0, 0, 0, 0)
    for (let i = DAYS - 1; i >= 0; i--) {
      const d = new Date(today); d.setDate(today.getDate() - i)
      days.push({ date: d, key: d.toDateString(), total: 0, count: 0 })
    }
    orders.filter((o) => o.status !== 'DA_HUY').forEach((o) => {
      const k = new Date(o.orderDate); k.setHours(0, 0, 0, 0)
      const day = days.find((x) => x.key === k.toDateString())
      if (day) { day.total += Number(o.total); day.count += 1 }
    })
    return days
  }, [orders])

  const lowStock = useMemo(() => (products || [])
    .filter((p) => p.visible)
    .flatMap((p) => p.variants.filter((v) => v.stockQty <= LOW_STOCK).map((v) => ({ ...v, product: p.name })))
    .sort((a, b) => a.stockQty - b.stockQty), [products])

  if (error) return <div className="alert alert-error">{error}</div>
  if (!stats || !products) return <div className="muted">Đang tải...</div>

  const recent = [...orders].sort((a, b) => new Date(b.orderDate) - new Date(a.orderDate)).slice(0, 5)

  return (
    <div className="dash">
      <div className="row between wrap">
        <h1>Tổng quan cửa hàng</h1>
        <span className="muted small">Tính từ {orders.length} đơn hàng và {products.length} sản phẩm hiện có</span>
      </div>

      <div className="stat-row">
        <Stat label="Doanh thu đã giao" value={money(stats.delivered)} sub={stats.deliveredCount + ' đơn đã giao'} />
        <Stat label="Đang xử lý" value={money(stats.inProgress)} sub={stats.inProgressCount + ' đơn chờ xác nhận / đang giao'} />
        <Stat label="Giá trị trung bình / đơn" value={money(stats.avg)} sub="Không tính đơn đã hủy" />
        <Stat label="Chờ thanh toán" value={stats.waitingPay} sub={'Tỷ lệ hủy ' + stats.cancelRate + '%'} />
      </div>

      <div className="dash-grid">
        <section className="card">
          <h3>Doanh thu {DAYS} ngày gần nhất</h3>
          <p className="muted small">Tổng tiền các đơn không bị hủy, theo ngày đặt.</p>
          <RevenueChart days={daily} />
          <details className="table-view">
            <summary>Xem dạng bảng</summary>
            <table className="table compact">
              <thead><tr><th>Ngày</th><th>Số đơn</th><th>Doanh thu</th></tr></thead>
              <tbody>{daily.map((d) => (
                <tr key={d.key}><td>{d.date.toLocaleDateString('vi-VN')}</td><td>{d.count}</td><td>{money(d.total)}</td></tr>
              ))}</tbody>
            </table>
          </details>
        </section>

        <section className="card">
          <h3>Đơn hàng theo trạng thái</h3>
          <p className="muted small">Số đơn ở mỗi trạng thái hiện tại.</p>
          <StatusBars rows={stats.byStatus} />
        </section>
      </div>

      <div className="dash-grid">
        <section className="card table-wrap">
          <div className="card-head">
            <h3>Sắp hết hàng</h3>
            <Link to="/nhan-vien/san-pham" className="small">Cập nhật tồn kho →</Link>
          </div>
          {lowStock.length === 0
            ? <p className="muted small pad">Không có biến thể nào còn từ {LOW_STOCK} sản phẩm trở xuống.</p>
            : (
              <table className="table compact">
                <thead><tr><th>Sản phẩm</th><th>Size</th><th>Màu</th><th>Tồn</th></tr></thead>
                <tbody>{lowStock.map((v) => (
                  <tr key={v.id}><td>{v.product}</td><td>{v.size}</td><td>{v.color}</td>
                    <td><b className={v.stockQty === 0 ? 'text-error' : ''}>{v.stockQty === 0 ? 'Hết' : v.stockQty}</b></td></tr>
                ))}</tbody>
              </table>
            )}
        </section>

        <section className="card table-wrap">
          <div className="card-head">
            <h3>Đơn mới nhất</h3>
            <Link to="/nhan-vien/don-hang" className="small">Tất cả đơn →</Link>
          </div>
          {recent.length === 0
            ? <p className="muted small pad">Chưa có đơn hàng nào.</p>
            : (
              <table className="table compact">
                <thead><tr><th>Mã đơn</th><th>Ngày đặt</th><th>Tổng</th><th>Trạng thái</th></tr></thead>
                <tbody>{recent.map((o) => (
                  <tr key={o.orderCode}>
                    <td><Link to={'/nhan-vien/don-hang/' + o.orderCode}>{o.orderCode}</Link></td>
                    <td>{dateTime(o.orderDate)}</td><td>{money(o.total)}</td><td><StatusBadge status={o.status} /></td>
                  </tr>
                ))}</tbody>
              </table>
            )}
        </section>
      </div>
    </div>
  )
}

function Stat({ label, value, sub }) {
  return (
    <div className="stat card">
      <div className="stat-label">{label}</div>
      <div className="stat-value">{value}</div>
      <div className="muted small">{sub}</div>
    </div>
  )
}

/** Cột dọc một chuỗi số liệu: trục y 3 mốc, nhãn ngày, chú thích khi rê chuột. */
function RevenueChart({ days }) {
  const max = Math.max(...days.map((d) => d.total), 0)
  const top = niceCeil(max)
  const ticks = [top, top / 2, 0]
  const short = (v) => (v >= 1e6 ? (v / 1e6).toLocaleString('vi-VN', { maximumFractionDigits: 1 }) + ' tr' : v >= 1e3 ? Math.round(v / 1e3) + 'k' : String(v))
  return (
    <div className="vchart" role="img" aria-label={'Biểu đồ doanh thu ' + days.length + ' ngày gần nhất'}>
      <div className="vchart-y">{ticks.map((t) => <span key={t}>{short(t)}</span>)}</div>
      <div className="vchart-plot">
        {ticks.map((t) => <div key={t} className="vchart-grid" style={{ bottom: (top ? (t / top) * 100 : 0) + '%' }} />)}
        <div className="vchart-bars">
          {days.map((d) => (
            <div key={d.key} className="vchart-col" tabIndex={0}>
              <div className="vchart-bar" style={{ height: (top ? (d.total / top) * 100 : 0) + '%' }} />
              <div className="vchart-tip">
                <b>{d.date.getDate()}/{d.date.getMonth() + 1}</b>
                <span>{money(d.total)}</span><span>{d.count} đơn</span>
              </div>
            </div>
          ))}
        </div>
      </div>
      <div />
      <div className="vchart-x">
        {days.map((d, i) => <span key={d.key}>{i % 2 === (days.length - 1) % 2 ? d.date.getDate() + '/' + (d.date.getMonth() + 1) : ''}</span>)}
      </div>
    </div>
  )
}

/** Thanh ngang một chuỗi số liệu, nhãn trạng thái bằng chữ ở bên trái và số ở bên phải. */
function StatusBars({ rows }) {
  const max = Math.max(...rows.map((r) => r.count), 1)
  return (
    <div className="hbars">
      {rows.map((r) => (
        <div key={r.key} className="hbar-row" title={r.label + ': ' + r.count + ' đơn'}>
          <span className="hbar-label">{r.label}</span>
          <div className="hbar-track"><div className="hbar" style={{ width: (r.count / max) * 100 + '%' }} /></div>
          <span className="hbar-val">{r.count}</span>
        </div>
      ))}
    </div>
  )
}

function niceCeil(v) {
  if (v <= 0) return 0
  const p = Math.pow(10, Math.floor(Math.log10(v)))
  const n = v / p
  return (n <= 1 ? 1 : n <= 2 ? 2 : n <= 5 ? 5 : 10) * p
}
