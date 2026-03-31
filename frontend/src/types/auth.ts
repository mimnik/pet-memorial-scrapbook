export interface AuthUser {
  username: string
  email: string
  role: string
  displayName?: string
  avatarUrl?: string
}

export interface AuthPayload extends AuthUser {
  token?: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
}
