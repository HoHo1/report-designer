import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as apiLogin, logout as apiLogout } from '@/api/auth'
import type { LoginParams, LoginResult } from '@/api/auth'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref<LoginResult | null>(null)

  const login = async (params: LoginParams) => {
    const res = await apiLogin(params)
    token.value = res.data.token
    userInfo.value = res.data
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('userInfo', JSON.stringify(res.data))
  }

  const logout = async () => {
    try {
      await apiLogout()
    } catch (e) {
      // ignore
    }
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/login')
  }

  const initUserInfo = () => {
    const userInfoStr = localStorage.getItem('userInfo')
    if (userInfoStr) {
      userInfo.value = JSON.parse(userInfoStr)
    }
  }

  return {
    token,
    userInfo,
    login,
    logout,
    initUserInfo
  }
})
