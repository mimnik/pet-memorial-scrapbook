import request from '@/utils/request'
import type { AuthPayload, LoginRequest, RegisterRequest } from '@/types/auth'

export const login = (payload: LoginRequest) =>
  request.post<unknown, { data: AuthPayload }>('/auth/login', payload)

export const guestLogin = () =>
  request.post<unknown, { data: AuthPayload }>('/auth/guest')

export const register = (payload: RegisterRequest) =>
  request.post<unknown, { data: AuthPayload }>('/auth/register', payload)

export const me = () => request.get<unknown, { data: AuthPayload }>('/auth/me')
