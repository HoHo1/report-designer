import request from '@/utils/request'

export interface LoginParams {
  username: string
  password: string
}

export interface LoginResult {
  token: string
  userId: string
  username: string
  role: string
}

export const login = (data: LoginParams) => {
  return request.post<any, { data: LoginResult }>('/auth/login', data)
}

export const logout = () => {
  return request.post('/auth/logout')
}

export const getCurrentUser = () => {
  return request.get('/auth/user')
}
