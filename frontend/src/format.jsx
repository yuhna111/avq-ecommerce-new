export const money = (v) => (v === null || v === undefined ? '' : new Intl.NumberFormat('vi-VN').format(v) + ' đ')

export const dateTime = (s) => {
  if (!s) return ''
  const d = new Date(s)
  return d.toLocaleString('vi-VN', { hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit', year: 'numeric' })
}

export const ORDER_STATUS = {
  CHO_THANH_TOAN: { label: 'Chờ thanh toán', tone: 'amber' },
  CHO_XAC_NHAN: { label: 'Chờ xác nhận', tone: 'blue' },
  DANG_GIAO: { label: 'Đang giao', tone: 'violet' },
  DA_GIAO: { label: 'Đã giao', tone: 'green' },
  DA_HUY: { label: 'Đã hủy', tone: 'gray' },
}

export const PAYMENT_STATUS = {
  DANG_XU_LY: 'Đang xử lý',
  THANH_CONG: 'Thành công',
  THAT_BAI: 'Thất bại',
  SAI_LECH: 'Sai lệch số tiền',
}

export const PAYMENT_METHOD = { COD: 'Thanh toán khi nhận hàng (COD)', ONLINE: 'Thanh toán trực tuyến' }

export const FIT_TYPE = { SLIM: 'Ôm (Slim)', REGULAR: 'Vừa (Regular)', OVERSIZE: 'Rộng (Oversize)' }

export const FIT_PREF = { OM: 'Mặc ôm', VUA: 'Mặc vừa', RONG: 'Mặc rộng' }

// Mã màu hiển thị cho ô mẫu màu (tên màu lấy từ product_variants.color)
const COLOR_HEX = {
  'Đen': '#1c1d21',
  'Trắng': '#ffffff',
  'Xám': '#9aa0a8',
  'Xanh nhạt': '#a9c7e8',
  'Be': '#e3d3b8',
  'Xanh đậm': '#24395c',
  'Rêu': '#6b7045',
  'Hồng': '#eab0bd',
  'Xanh': '#5b8fd6',
}
export const colorHex = (name) => COLOR_HEX[name] || '#c9ced6'

export function Swatch({ color, size = 16 }) {
  return (
    <span className="swatch" title={color} aria-label={color}
      style={{ width: size, height: size, background: colorHex(color) }} />
  )
}

// Cam kết dịch vụ: lấy đúng từ quy tắc nghiệp vụ (DT-01, BR10), không hứa chính sách không có trong thiết kế
export const FREE_SHIP_FROM = 500000
export const SHIPPING_FEE = 30000

export function StatusBadge({ status }) {
  const s = ORDER_STATUS[status] || { label: status, tone: 'gray' }
  return <span className={'badge badge-' + s.tone}>{s.label}</span>
}
