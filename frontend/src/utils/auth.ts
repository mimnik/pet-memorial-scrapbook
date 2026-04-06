import type { AuthUser } from '@/types/auth'

const TOKEN_KEY = 'pet_memorial_token'
const USER_KEY = 'pet_memorial_user'
const AUTH_EXPIRED_HINT_KEY = 'pet_memorial_auth_expired_hint'
const AUTH_EXPIRED_HINT_TTL_MS = 30_000

export const getToken = () => localStorage.getItem(TOKEN_KEY) || ''

export const setToken = (token: string) => {
  localStorage.setItem(TOKEN_KEY, token)
}

export const clearToken = () => {
  localStorage.removeItem(TOKEN_KEY)
}

export const getCurrentUser = (): AuthUser | null => {
  const value = localStorage.getItem(USER_KEY)
  if (!value) return null
  try {
    return JSON.parse(value) as AuthUser
  } catch {
    return null
  }
}

export const setCurrentUser = (user: AuthUser) => {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export const clearCurrentUser = () => {
  localStorage.removeItem(USER_KEY)
}

export const clearAuth = () => {
  clearToken()
  clearCurrentUser()
}

export const setAuthExpiredHint = (message: string) => {
  sessionStorage.setItem(
    AUTH_EXPIRED_HINT_KEY,
    JSON.stringify({
      message,
      createdAt: Date.now(),
    }),
  )
}

export const popAuthExpiredHint = () => {
  const raw = sessionStorage.getItem(AUTH_EXPIRED_HINT_KEY) || ''
  if (!raw) {
    return ''
  }

  sessionStorage.removeItem(AUTH_EXPIRED_HINT_KEY)

  try {
    const parsed = JSON.parse(raw) as { message?: unknown; createdAt?: unknown }
    if (typeof parsed.message !== 'string' || !parsed.message.trim()) {
      return ''
    }

    if (typeof parsed.createdAt !== 'number') {
      return ''
    }

    if (Date.now() - parsed.createdAt > AUTH_EXPIRED_HINT_TTL_MS) {
      return ''
    }

    return parsed.message.trim()
  } catch {
    return ''
  }
}
