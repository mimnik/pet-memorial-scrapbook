import axios from 'axios'
import { ElMessage } from 'element-plus'
import { clearAuth, getToken } from '@/utils/auth'

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
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status
    if (status === 401) {
      clearAuth()
      window.location.href = '/login'
    }
    return Promise.reject(error)
  },
)

export default request
