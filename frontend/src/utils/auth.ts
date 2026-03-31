import type { AuthUser } from '@/types/auth'

const TOKEN_KEY = 'pet_memorial_token'
const USER_KEY = 'pet_memorial_user'

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
