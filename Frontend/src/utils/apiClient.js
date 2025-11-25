// src/utils/apiClient.js
import { getAccessToken, getRefreshToken, setTokens, clearTokens } from './tokenService'

// 서버 URL
const SERVER_URL = import.meta.env.VITE_BACKEND_URL

// JWT 만료 체크 (base64 decode)
const isTokenExpired = (token) => {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    console.log('JWT payload:', payload) // 디버그용
    return Date.now() >= payload.exp * 1000
  } catch {
    return true
  }
}

// refresh token으로 access token 재발급
const tryReissue = async () => {
  const refreshToken = getRefreshToken()
  if (!refreshToken || isTokenExpired(refreshToken)) return false

  try {
    const res = await fetch(`${SERVER_URL}/auth/reissue`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken })
    })
    if (!res.ok) return false

    const data = await res.json()
    if (data.data?.accessToken) {
      // 항상 최신 토큰 localStorage 갱신
      setTokens({ accessToken: data.data.accessToken, refreshToken })
      return true
    }
    return false
  } catch (err) {
    console.error('Reissue failed', err)
    return false
  }
}

// apiFetch
export const apiFetch = async (url, options = {}, retry = true) => {
  let accessToken = getAccessToken()

  // 요청 전 access token 만료 체크
  if (accessToken && isTokenExpired(accessToken)) {
    const refreshed = await tryReissue()
    if (!refreshed) {
      clearTokens()
      window.location.href = '/login'
      return
    }
    accessToken = getAccessToken() // 재발급 후 최신 token
  }

  const headers = { ...(options.headers || {}) }
  if (accessToken) headers['Authorization'] = `Bearer ${accessToken}`

  const response = await fetch(`${SERVER_URL}${url}`, { ...options, headers })

  // 401 발생 시 재발급
  if (response.status === 401 && retry) {
    const refreshed = await tryReissue()
    if (refreshed) return apiFetch(url, options, false)

    clearTokens()
    window.location.href = '/login'
    return
  }

  return response
}
