<template>
  <div class="login-page">
    <div class="login-left">
      <h1>Harmony</h1>
      <p>나만의 SNS 서비스</p>
    </div>
    <div class="login-right">
      <div class="login-container">
        <h2>로그인</h2>
        <form @submit.prevent="handleLogin">
          <input v-model="email" type="email" placeholder="이메일" required />
          <input v-model="password" type="password" placeholder="비밀번호" required />
          <button type="submit" :disabled="loading">
            {{ loading ? '로그인 중...' : '로그인' }}
          </button>
        </form>
        <p v-if="error" class="error-msg">{{ error }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { apiFetch } from '@/utils/apiClient'
import { setTokens } from '@/utils/tokenService'

const router = useRouter()
const email = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

const handleLogin = async () => {
  error.value = ''
  loading.value = true
  try {
    const res = await apiFetch('/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: email.value, password: password.value })
    }, false) // 로그인 시 retry 필요 없음

    if (!res) throw new Error('서버와 연결되지 않았습니다.')
    const resData = await res.json()
    if (!resData.data) throw new Error(resData.message || '로그인에 실패했습니다.')

    // 토큰 저장
    setTokens({
      accessToken: resData.data.accessToken,
      refreshToken: resData.data.refreshToken
    })

    router.push('/home')
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
}
</script>


<style scoped>
.login-page { display: flex; height: 100vh; background: #f0f2f5; }
.login-left { flex:1; background:#007aff; color:white; display:flex; flex-direction:column; justify-content:center; align-items:center; padding:32px; }
.login-left h1 { font-size:48px; margin-bottom:16px; }
.login-left p { font-size:20px; }
.login-right { flex:1; display:flex; justify-content:center; align-items:center; }
.login-container { background:white; padding:32px; border-radius:8px; box-shadow:0 0 10px rgba(0,0,0,0.1); width:320px; display:flex; flex-direction:column; align-items:center; }
.login-container h2 { margin-bottom:24px; }
.login-container input { width:100%; padding:8px; margin-bottom:12px; border:1px solid #ccc; border-radius:4px; }
.login-container button { width:100%; padding:10px; background:#007aff; color:white; border:none; border-radius:4px; cursor:pointer; }
.login-container button:disabled { background:#aaa; cursor:default; }
.error-msg { color:red; margin-top:12px; font-size:0.85rem; }
</style>
