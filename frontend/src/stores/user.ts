import { defineStore } from 'pinia'
import { login as loginApi, me, register as registerApi } from '@/api/auth'
import type { AuthPayload, LoginRequest, RegisterRequest } from '@/types/auth'
import { clearAuth, getCurrentUser, getToken, setCurrentUser, setToken } from '@/utils/auth'

interface UserState {
  token: string
  profile: AuthPayload | null
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    token: getToken(),
    profile: getCurrentUser(),
  }),
  actions: {
    applySession(payload: AuthPayload) {
      if (payload.token) {
        this.token = payload.token
        setToken(payload.token)
      }

      const profile = {
        username: payload.username,
        email: payload.email,
        role: payload.role,
        displayName: payload.displayName,
        avatarUrl: payload.avatarUrl,
      }
      this.profile = profile
      setCurrentUser(profile)
    },
    async login(payload: LoginRequest) {
      const res = await loginApi(payload)
      this.applySession(res.data)
      return res
    },
    async register(payload: RegisterRequest) {
      const res = await registerApi(payload)
      this.applySession(res.data)
      return res
    },
    async refreshProfile() {
      if (!this.token) {
        return
      }
      const res = await me()
      this.applySession(res.data)
    },
    logout() {
      this.token = ''
      this.profile = null
      clearAuth()
    },
  },
})
