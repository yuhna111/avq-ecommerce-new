// Gọi REST API của backend (qua proxy Vite /api). Lỗi backend có dạng { code, message }.
const AUTH_KEY = 'avq_auth'

export function getAuth() {
  try {
    return JSON.parse(localStorage.getItem(AUTH_KEY))
  } catch {
    return null
  }
}

export function saveAuth(auth) {
  if (auth) localStorage.setItem(AUTH_KEY, JSON.stringify(auth))
  else localStorage.removeItem(AUTH_KEY)
  window.dispatchEvent(new Event('avq-auth'))
}

export class ApiError extends Error {
  constructor(message, code, status) {
    super(message)
    this.code = code
    this.status = status
  }
}

export async function api(path, { method = 'GET', body, params } = {}) {
  const url = new URL('/api' + path, window.location.origin)
  if (params) {
    Object.entries(params).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== '') url.searchParams.set(k, v)
    })
  }
  const headers = {}
  const auth = getAuth()
  if (auth?.token) headers.Authorization = 'Bearer ' + auth.token
  if (body !== undefined) headers['Content-Type'] = 'application/json'

  let res
  try {
    res = await fetch(url, { method, headers, body: body !== undefined ? JSON.stringify(body) : undefined })
  } catch {
    throw new ApiError('Không kết nối được máy chủ. Hãy kiểm tra backend đang chạy ở cổng 8080.', 'NETWORK', 0)
  }
  if (res.status === 401 && auth?.token) {
    saveAuth(null) // token hết hạn (30 phút) -> đăng xuất
  }
  const text = await res.text()
  let data = null
  try {
    data = text ? JSON.parse(text) : null
  } catch {
    data = null
  }
  if (!res.ok) {
    throw new ApiError(data?.message || 'Có lỗi, vui lòng thử lại', data?.code, res.status)
  }
  return data
}

/** Tải một ảnh sản phẩm lên (nhân viên). Trả về đường dẫn ảnh, ví dụ "/uploads/abc.jpg". */
export async function uploadImage(file) {
  const form = new FormData()
  form.append('file', file)
  const headers = {}
  const auth = getAuth()
  if (auth?.token) headers.Authorization = 'Bearer ' + auth.token
  let res
  try {
    res = await fetch('/api/admin/products/images', { method: 'POST', headers, body: form })
  } catch {
    throw new ApiError('Không kết nối được máy chủ. Hãy kiểm tra backend đang chạy ở cổng 8080.', 'NETWORK', 0)
  }
  const text = await res.text()
  if (!res.ok) {
    let data = null
    try { data = JSON.parse(text) } catch { data = null }
    throw new ApiError(data?.message || 'Tải ảnh không thành công', data?.code, res.status)
  }
  return text
}
