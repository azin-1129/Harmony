<template>
  <div class="login-page">
    <div class="login-left" :style="{ backgroundImage: `url(${backgroundImage})` }">
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { apiFetch } from '@/utils/apiClient'
import { setTokens } from '@/utils/tokenService'

const router = useRouter()
const email = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)
const backgroundImage = ref('') // 이미지를 설정할 수 있는 ref

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

// 배경 이미지 설정
onMounted(() => {
  backgroundImage.value = 'https://images.unsplash.com/photo-1557821552-17105176677c?w=1200&h=1200&fit=crop'
})
</script>


<style scoped>
.login-page { display: flex; height: 100vh; background: #f0f2f5; }
.login-left { flex:1; background-color:#007aff; background-size:cover; background-position:center; background-repeat:no-repeat; color:white; display:flex; flex-direction:column; justify-content:center; align-items:center; padding:32px; }
.login-left h1 { font-size:48px; margin-bottom:16px; text-shadow: 2px 2px 4px rgba(0,0,0,0.5); }
.login-left p { font-size:20px; text-shadow: 1px 1px 2px rgba(0,0,0,0.5); }
.login-right { flex:1; display:flex; justify-content:center; align-items:center; }
.login-container { background:white; padding:32px; border-radius:8px; box-shadow:0 0 10px rgba(0,0,0,0.1); width:280px; display:flex; flex-direction:column; align-items:center; }
.login-container h2 { margin-bottom:24px; }
.login-container form { width:100%; display:flex; flex-direction:column; align-items:center; }
.login-container input { width:100%; max-width:250px; padding:8px; margin-bottom:12px; border:1px solid #ccc; border-radius:4px; }
.login-container button { width:100%; max-width:250px; padding:10px; background:#007aff; color:white; border:none; border-radius:4px; cursor:pointer; margin-top:8px; }
.login-container button:disabled { background:#aaa; cursor:default; }
.error-msg { color:red; margin-top:12px; font-size:0.85rem; }
</style>
