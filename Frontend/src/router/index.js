import { createRouter, createWebHistory } from 'vue-router'
import { getAccessToken, getRefreshToken, setTokens, clearTokens } from '@/utils/tokenService'
import { BACKEND_URL } from '@/utils/config'

// JWT 만료 체크 (base64 decode)
const isTokenExpired = (token) => {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
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
    const res = await fetch(`${BACKEND_URL}/auth/reissue`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken })
    })
    if (!res.ok) return false

    const data = await res.json()
    if (data.data?.accessToken) {
      setTokens({ accessToken: data.data.accessToken, refreshToken })
      return true
    }
    return false
  } catch {
    return false
  }
}

import MainLayout from '@/layouts/MainLayout.vue'
import LoginPage from '@/components/LoginPage.vue'
import HomeFeed from '@/components/HomeFeed.vue'
import Reels from '@/components/Reels.vue'
import Chat from '@/components/Chat.vue'
import MyPage from '@/components/MyPage.vue'
import Search from '@/components/Search.vue'
import CreatePost from '@/components/CreatePost.vue'
import Settings from '@/components/Settings.vue'
import ArticleDetail from '@/components/ArticleDetail.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: LoginPage,
    meta: { guestOnly: true }
  },
  {
    path: '/',
    component: MainLayout,
    children: [
      { path: '', redirect: '/home' },
      { path: 'home', component: HomeFeed, meta: { requiresAuth: true } },
      { path: 'reels', component: Reels, meta: { requiresAuth: true } },
      { path: 'search', component: Search, meta: { requiresAuth: true } },
      { path: 'chat', component: Chat, meta: { requiresAuth: true } },
      { path: 'mypage', component: MyPage, meta: { requiresAuth: true } },
      { path: 'create', component: CreatePost, meta: { requiresAuth: true } },
      { path: 'settings', component: Settings, meta: { requiresAuth: true } },
      { path: 'article/:id', component: ArticleDetail, meta: { requiresAuth: true } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 전역 라우트 가드 (async 가능)
router.beforeEach(async (to, from, next) => {
  let token = getAccessToken()

  // 로그인 필요 + 토큰 없음 → 로그인 페이지
  if (to.meta.requiresAuth) {
    if (!token || isTokenExpired(token)) {
      const refreshed = await tryReissue()
      if (refreshed) {
        token = getAccessToken()
        next() // 재발급 성공 → 접근 허용
      } else {
        clearTokens()
        next({ name: 'Login' })
      }
      return
    }
  }

  // 로그인 상태인데 /login 접근 → 홈으로
  if (to.meta.guestOnly && token && !isTokenExpired(token)) {
    next('/home')
    return
  }

  next()
})

export default router
