<template>
  <nav class="sidebar">
    <div class="top">
      <h2 class="logo">Harmony</h2>
      <ul class="menu">
        <li :class="{ active: isActive('/home') }" @click="go('/home')">
          <span class="icon">🏠</span> 홈
        </li>
        <li :class="{ active: isActive('/reels') }" @click="go('/reels')">
          <span class="icon">🎬</span> 릴스
        </li>
        <li :class="{ active: isActive('/search') }" @click="go('/search')">
          <span class="icon">🔍</span> 검색
        </li>
        <li :class="{ active: isActive('/chat') }" @click="go('/chat')">
          <span class="icon">💬</span> 채팅
        </li>
        <li :class="{ active: isActive('/friends') }" @click="go('/friends')">
          <span class="icon">👥</span> 친구
        </li>
        <li :class="{ active: isActive('/mypage') }" @click="go('/mypage')">
          <span class="icon">👤</span> 마이페이지
        </li>
        <li :class="{ active: isActive('/create') }" @click="go('/create')">
          <span class="icon">✏️</span> 게시물 작성
        </li>
      </ul>
    </div>

    <div class="bottom">
      <button class="settings-btn" @click="go('/settings')">
        <span class="icon">⚙️</span> 설정
      </button>
      <button class="logout-btn" @click="logout">로그아웃</button>
    </div>
  </nav>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router'
import { clearTokens, getAccessToken } from '@/utils/tokenService'
import { apiUrl } from '@/utils/config'

const router = useRouter()
const route = useRoute()
const go = (path) => router.push(path)
const isActive = (path) => route.path === path

const logout = async () => {
  try {
    const token = getAccessToken()
    if (token) {
      await fetch(apiUrl('/logout'), {
        method: 'POST',
        headers: { Authorization: `Bearer ${token}` }
      })
    }
  } catch (err) {
    // 네트워크 오류나 서버 오류는 무시하고 토큰만 제거
    console.error('Logout request failed', err)
  } finally {
    clearTokens()
    router.push('/login')
  }
}
</script>

<style scoped>
.sidebar {
  width: 220px;
  background-color: #fff;
  border-right: 1px solid #ddd;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 100%;
  box-sizing: border-box;
  padding: 20px;
}

.logo {
  font-weight: bold;
  font-size: 1.6em;
  margin-bottom: 40px;
}

.menu {
  list-style: none;
  padding: 0;
  margin: 0;
}

li {
  display: flex;
  align-items: center;
  padding: 12px 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
  margin-bottom: 4px;
  font-size: 0.95em;
}

li:hover {
  background-color: #f2f2f2;
}

li.active {
  background-color: #e5e5ff;
  font-weight: bold;
}

.icon {
  margin-right: 8px;
  font-size: 1.1em;
}

.bottom {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.settings-btn,
.logout-btn {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  border: none;
  border-radius: 8px;
  padding: 10px 12px;
  cursor: pointer;
  font-weight: 600;
  transition: background 0.2s;
  font-size: 0.95em;
  flex-shrink: 0;
}

.settings-btn {
  background-color: #f5f5f5;
  color: #333;
}

.settings-btn:hover {
  background-color: #e8e8e8;
}

.logout-btn {
  background-color: #6b6bff;
  color: #fff;
}

.logout-btn:hover {
  background-color: #5555ff;
}

.settings-btn .icon,
.logout-btn .icon {
  margin-right: 8px;
}
</style>
