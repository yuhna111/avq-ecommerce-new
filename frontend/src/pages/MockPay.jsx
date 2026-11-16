import { useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { api } from '../api'
import { money } from '../format'

// Trang cổng thanh toán GIẢ LẬP (thay cho VNPay). Kết quả đi qua PaymentService.handleCallback (DT-05).
export default function MockPay() {
  const [params] = useSearchParams()
  const code = params.get('vnp_TxnRef')
  const amount = Number(params.get('vnp_Amount') || 0) / 100
  const [result, setResult] = useState(null)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  const pay = async (success) => {
    setBusy(true)
    setError('')
    try {
      // Gửi kèm link đã ký để cổng nhận lại giao dịch nếu backend vừa khởi động lại
      const link = Object.fromEntries([...params.entries()].filter(([k]) => k.startsWith('vnp_')))
      setResult(await api('/payments/mock/' + code, { method: 'POST', params: { ...link, success } }))
    } catch (e) {
      setError(e.message)
    } finally {
      setBusy(false)
    }
  }

  if (!code) return <div className="alert alert-error">Thiếu mã đơn hàng.</div>

  return (
    <div className="gateway card">
      <div className="gw-head">
        <div className="gw-logo">AVQ<b>Pay</b></div>
        <span className="badge badge-amber">Cổng thanh toán giả lập</span>
      </div>
      <div className="gw-body">
        <div className="sum-row"><span>Đơn hàng</span><b>{code}</b></div>
        <div className="sum-row"><span>Số tiền</span><b className="price-lg">{money(amount)}</b></div>
        <div className="sum-row"><span>Nội dung</span><span>{params.get('vnp_OrderInfo')}</span></div>
        <p className="muted small">Bản demo không gọi VNPay thật. Chọn kết quả giao dịch để cổng gửi thông báo (IPN đã ký HMAC-SHA512) về hệ thống.</p>
        {error && <div className="alert alert-error">{error}</div>}
        {!result ? (
          <div className="row gap">
            <button className="btn btn-primary btn-lg grow" disabled={busy} onClick={() => pay(true)}>Thanh toán thành công</button>
            <button className="btn btn-danger btn-lg grow" disabled={busy} onClick={() => pay(false)}>Hủy / thất bại</button>
          </div>
        ) : (
          <div className={'alert ' + (result.RspCode === '00' ? 'alert-success' : 'alert-error')}>
            Mã phản hồi {result.RspCode}: {result.Message}
            {result.RspCode === '00' && result.Message !== 'Thanh toán thành công' && (
              <div>Đơn vẫn ở trạng thái "Chờ thanh toán", bạn có thể thanh toán lại trong 15 phút.</div>
            )}
          </div>
        )}
        <Link className="btn btn-ghost btn-block" to={'/don-hang/' + code}>Xem đơn hàng</Link>
      </div>
    </div>
  )
}
