import axios from 'axios'
import { ElMessage } from 'element-plus'
import { clearAuth, getToken, setAuthExpiredHint } from '@/utils/auth'

const UPLOADS_PATH_PREFIX = '/uploads/'

const normalizeMediaUrl = (url: string) => {
  const trimmed = url.trim()
  if (!trimmed || trimmed.startsWith('blob:') || trimmed.startsWith('data:')) {
    return trimmed
  }
  if (trimmed.startsWith(UPLOADS_PATH_PREFIX)) {
    return trimmed
  }

  try {
    const parsed = new URL(trimmed)
    const isLocalHost = parsed.hostname === 'localhost' || parsed.hostname === '127.0.0.1'
    if (parsed.pathname.startsWith(UPLOADS_PATH_PREFIX) && isLocalHost) {
      return `${parsed.pathname}${parsed.search}${parsed.hash}`
    }
  } catch {
    return trimmed
  }

  return trimmed
}

const normalizeMediaFields = (value: unknown): unknown => {
  if (Array.isArray(value)) {
    for (let i = 0; i < value.length; i += 1) {
      value[i] = normalizeMediaFields(value[i])
    }
    return value
  }

  if (!value || typeof value !== 'object') {
    return value
  }

  const record = value as Record<string, unknown>
  for (const key of Object.keys(record)) {
    const current = record[key]
    if (typeof current === 'string' && (key === 'url' || key.endsWith('Url'))) {
      record[key] = normalizeMediaUrl(current)
      continue
    }
    if (current && typeof current === 'object') {
      record[key] = normalizeMediaFields(current)
    }
  }

  return record
}

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

const hasAuthorizationHeader = (error: unknown) => {
  const maybeConfig = (error as { config?: { headers?: unknown } } | null)?.config
  const headers = maybeConfig?.headers as
    | {
        Authorization?: unknown
        authorization?: unknown
        get?: (name: string) => unknown
      }
    | undefined

  if (!headers) {
    return false
  }

  if (typeof headers.get === 'function') {
    const headerValue = headers.get('Authorization')
    if (typeof headerValue === 'string' && headerValue.trim()) {
      return true
    }
  }

  const directValue = headers.Authorization ?? headers.authorization
  if (typeof directValue === 'string') {
    return directValue.trim().length > 0
  }

  if (Array.isArray(directValue)) {
    return directValue.some((item) => typeof item === 'string' && item.trim().length > 0)
  }

  return false
}

request.interceptors.response.use(
  (response) => {
    const res = response.data
    normalizeMediaFields(res)
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    const backendMessage = error?.response?.data?.message
    const finalMessage = backendMessage || error.message || '网络错误'
    ElMessage.error(finalMessage)
    return Promise.reject(error)
  },
)

request.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    // 过滤掉非 ASCII 字符（如中文），防止浏览器设置请求头报错
    const safeToken = token.replace(/[^\x20-\x7E]/g, '')
    config.headers.Authorization = `Bearer ${safeToken}`
  }
  return config
})

request.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status
    if (status === 401) {
      const hasLocalToken = getToken().trim().length > 0
      const isAuthenticatedRequest = hasAuthorizationHeader(error) || hasLocalToken
      const expiredMessage =
        typeof error?.response?.data?.message === 'string' && error.response.data.message.trim()
          ? error.response.data.message.trim()
          : '登录状态已失效，请重新登录'

      if (isAuthenticatedRequest) {
        setAuthExpiredHint(expiredMessage)
      }
      clearAuth()
      if (!window.location.pathname.startsWith('/login')) {
        if (isAuthenticatedRequest) {
          const encodedHint = encodeURIComponent(expiredMessage)
          setTimeout(() => {
            window.location.href = `/login?authHint=${encodedHint}`
          }, 1500)
        } else {
          window.location.href = '/login'
        }
      }
    }
    return Promise.reject(error)
  },
)

export default request
