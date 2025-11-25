<template>
  <nav class="sidebar">
    <div class="top">
      <h2 class="logo">Harmony</h2>
      <ul class="menu">
        <li :class="{ active: isActive('/home') }" @click="go('/home')">홈 피드</li>
        <li :class="{ active: isActive('/reels') }" @click="go('/reels')">릴스</li>
        <li :class="{ active: isActive('/chat') }" @click="go('/chat')">채팅</li>
        <li :class="{ active: isActive('/mypage') }" @click="go('/mypage')">마이페이지</li>
      </ul>
    </div>

    <button class="logout-btn" @click="logout">로그아웃</button>
  </nav>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router'
import { clearTokens } from '@/utils/tokenService'

const router = useRouter()
const route = useRoute()
const go = (path) => router.push(path)
const isActive = (path) => route.path === path

const logout = () => {
  clearTokens()
  router.push('/login')
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
  padding: 12px 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
  margin-bottom: 4px;
}

li:hover {
  background-color: #f2f2f2;
}

li.active {
  background-color: #e5e5ff;
  font-weight: bold;
}

/* ✅ 하단 고정 안 하고도 항상 보이도록 자연스럽게 배치 */
.logout-btn {
  background-color: #6b6bff;
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 10px;
  cursor: pointer;
  font-weight: 600;
  transition: background 0.2s, color 0.2s;
  flex-shrink: 0; /* 👈 버튼이 잘리지 않게 함 */
}

.logout-btn:hover {
  background-color: #5555ff;
}
</style>
