<template>
  <div class="register-page">
    <div class="register-left" :style="{ backgroundImage: `url(${backgroundImage})` }">
      <h1>Harmony</h1>
      <p>나만의 SNS 서비스</p>
    </div>

    <div class="register-right">
      <div class="register-container">
        <h2>회원가입</h2>

        <form @submit.prevent="handleRegister">
          <div class="field-block">
            <label class="field-label">이메일</label>
            <div class="field-row">
              <input v-model="form.email" type="email" placeholder="이메일" required />
              <button type="button" class="check-btn" @click="checkDuplicate('email')" :disabled="!canCheckDuplicate('email')">
                중복확인
              </button>
            </div>
            <p v-if="validation.email" class="status-msg error-msg">
              {{ validation.email }}
            </p>
            <p v-if="duplicate.email.message" :class="['status-msg', duplicate.email.ok ? 'ok-msg' : 'error-msg']">
              {{ duplicate.email.message }}
            </p>
          </div>

          <div class="field-block">
            <label class="field-label">식별자</label>
            <div class="field-row">
              <input
                v-model="form.userIdentifier"
                type="text"
                placeholder="식별자"
                required
              />
              <button
                type="button"
                class="check-btn"
                @click="checkDuplicate('userIdentifier')"
                :disabled="!canCheckDuplicate('userIdentifier')"
              >
                중복확인
              </button>
            </div>
            <p v-if="validation.userIdentifier" class="status-msg error-msg">
              {{ validation.userIdentifier }}
            </p>
            <p
              v-if="duplicate.userIdentifier.message"
              :class="['status-msg', duplicate.userIdentifier.ok ? 'ok-msg' : 'error-msg']"
            >
              {{ duplicate.userIdentifier.message }}
            </p>
          </div>

          <div class="field-block">
            <label class="field-label">닉네임</label>
            <div class="field-row">
              <input v-model="form.nickname" type="text" placeholder="닉네임" required />
              <button type="button" class="check-btn" @click="checkDuplicate('nickname')" :disabled="!canCheckDuplicate('nickname')">
                중복확인
              </button>
            </div>
            <p v-if="validation.nickname" class="status-msg error-msg">
              {{ validation.nickname }}
            </p>
            <p v-if="duplicate.nickname.message" :class="['status-msg', duplicate.nickname.ok ? 'ok-msg' : 'error-msg']">
              {{ duplicate.nickname.message }}
            </p>
          </div>

          <div class="field-block">
            <label class="field-label">비밀번호</label>
            <input v-model="form.password" type="password" placeholder="비밀번호" required />
            <p v-if="validation.password" class="status-msg error-msg">
              {{ validation.password }}
            </p>
          </div>
          <div class="field-block">
            <label class="field-label">비밀번호 확인</label>
            <input v-model="form.passwordConfirm" type="password" placeholder="비밀번호 확인" required />
            <p v-if="validation.passwordConfirm" class="status-msg error-msg">
              {{ validation.passwordConfirm }}
            </p>
            <p v-if="validation.passwordConfirmOk" class="status-msg ok-msg">
              {{ validation.passwordConfirmOk }}
            </p>
          </div>

          <button type="submit" :disabled="loading">
            {{ loading ? '가입 중...' : '회원가입' }}
          </button>
        </form>

        <p v-if="error" class="error-msg">{{ error }}</p>
        <p v-if="success" class="ok-msg">{{ success }}</p>

        <button class="secondary-btn" @click="goToLogin">로그인으로 돌아가기</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { apiFetch } from '@/utils/apiClient'

const router = useRouter()

const backgroundImage = ref('')
const loading = ref(false)
const error = ref('')
const success = ref('')

const form = reactive({
  email: '',
  userIdentifier: '',
  nickname: '',
  password: '',
  passwordConfirm: ''
})

const duplicate = reactive({
  email: { ok: false, checkedValue: '', message: '' },
  userIdentifier: { ok: false, checkedValue: '', message: '' },
  nickname: { ok: false, checkedValue: '', message: '' }
})

const validation = reactive({
  email: '',
  userIdentifier: '',
  nickname: '',
  password: '',
  passwordConfirm: '',
  passwordConfirmOk: ''
})

const getLiveFormatError = (field, value) => {
  if (!value) return ''
  return getFormatError(field, value)
}

const getFormatError = (field, value) => {
  if (!value) return '값을 먼저 입력해주세요.'

  if (field === 'email') {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)
      ? ''
      : '이메일 형식이 올바르지 않습니다.'
  }

  if (field === 'userIdentifier') {
    return /^[a-z0-9]{4,20}$/.test(value)
      ? ''
      : '식별자는 영문 소문자/숫자 4~20자리여야 합니다.'
  }

  if (field === 'nickname') {
    return /^[가-힣]{2,10}$/.test(value)
      ? ''
      : '닉네임은 한글 2~10자리여야 합니다.'
  }

  if (field === 'password') {
    return /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,20}$/.test(value)
      ? ''
      : '비밀번호는 영문 대/소문자, 숫자, 특수기호를 포함한 8~20자리여야 합니다.'
  }

  return ''
}

const resetDuplicateState = (field) => {
  duplicate[field].ok = false
  duplicate[field].checkedValue = ''
  duplicate[field].message = ''
}

watch(() => form.email, () => {
  resetDuplicateState('email')
  validation.email = getLiveFormatError('email', form.email.trim())
})
watch(() => form.userIdentifier, () => {
  resetDuplicateState('userIdentifier')
  validation.userIdentifier = getLiveFormatError('userIdentifier', form.userIdentifier.trim())
})
watch(() => form.nickname, () => {
  resetDuplicateState('nickname')
  validation.nickname = getLiveFormatError('nickname', form.nickname.trim())
})

watch(() => form.password, () => {
  validation.password = getFormatError('password', form.password)
  if (!form.passwordConfirm) {
    validation.passwordConfirm = ''
    validation.passwordConfirmOk = ''
    return
  }

  if (form.password !== form.passwordConfirm) {
    validation.passwordConfirm = '비밀번호와 비밀번호 확인이 일치하지 않습니다.'
    validation.passwordConfirmOk = ''
    return
  }

  validation.passwordConfirm = ''
  validation.passwordConfirmOk = '비밀번호가 일치합니다.'
})

watch(() => form.passwordConfirm, () => {
  if (!form.passwordConfirm) {
    validation.passwordConfirm = ''
    validation.passwordConfirmOk = ''
    return
  }

  if (form.password !== form.passwordConfirm) {
    validation.passwordConfirm = '비밀번호와 비밀번호 확인이 일치하지 않습니다.'
    validation.passwordConfirmOk = ''
    return
  }

  validation.passwordConfirm = ''
  validation.passwordConfirmOk = '비밀번호가 일치합니다.'
})

const checkDuplicate = async (field) => {
  const value = form[field]?.trim()

  const formatError = getFormatError(field, value)
  if (formatError) {
    duplicate[field].ok = false
    duplicate[field].message = formatError
    return
  }

  error.value = ''
  success.value = ''

  try {
    const encoded = encodeURIComponent(value)
    const res = await apiFetch(`/user/validate/${field}?${field}=${encoded}`, {
      method: 'GET'
    }, false)

    const data = await res.json().catch(() => ({}))
    duplicate[field].ok = res.ok
    duplicate[field].checkedValue = value
    duplicate[field].message = data.message || (res.ok ? '사용 가능한 값입니다.' : '이미 사용중인 값입니다.')
  } catch (err) {
    duplicate[field].ok = false
    duplicate[field].message = err.message || '중복 확인 중 오류가 발생했습니다.'
  }
}

const canCheckDuplicate = (field) => {
  if (loading.value) return false
  const value = form[field]?.trim()
  return !getFormatError(field, value)
}

const isDuplicateChecked = (field) => {
  const value = form[field]?.trim()
  return duplicate[field].ok && duplicate[field].checkedValue === value
}

const handleRegister = async () => {
  error.value = ''
  success.value = ''

  const emailError = getFormatError('email', form.email.trim())
  const identifierError = getFormatError('userIdentifier', form.userIdentifier.trim())
  const nicknameError = getFormatError('nickname', form.nickname.trim())
  const passwordError = getFormatError('password', form.password)
  const passwordConfirmError = form.password !== form.passwordConfirm
    ? '비밀번호와 비밀번호 확인이 일치하지 않습니다.'
    : ''

  if (emailError || identifierError || nicknameError || passwordError || passwordConfirmError) {
    if (emailError) {
      duplicate.email.ok = false
      duplicate.email.message = emailError
    }
    if (identifierError) {
      duplicate.userIdentifier.ok = false
      duplicate.userIdentifier.message = identifierError
    }
    if (nicknameError) {
      duplicate.nickname.ok = false
      duplicate.nickname.message = nicknameError
    }

    validation.password = passwordError
    validation.passwordConfirm = passwordConfirmError
    validation.passwordConfirmOk = passwordConfirmError ? '' : '비밀번호가 일치합니다.'
    error.value = emailError || identifierError || nicknameError || passwordError || passwordConfirmError
    return
  }

  if (!isDuplicateChecked('email') || !isDuplicateChecked('userIdentifier') || !isDuplicateChecked('nickname')) {
    error.value = '이메일, 식별자, 닉네임 중복 확인을 완료해주세요.'
    return
  }

  loading.value = true

  try {
    const res = await apiFetch('/user/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        email: form.email.trim(),
        userIdentifier: form.userIdentifier.trim(),
        nickname: form.nickname.trim(),
        password: form.password,
        passwordConfirm: form.passwordConfirm
      })
    }, false)

    const data = await res.json().catch(() => ({}))

    if (!res.ok) {
      throw new Error(data.message || '회원가입에 실패했습니다.')
    }

    success.value = data.message || '회원가입에 성공했습니다.'
    setTimeout(() => {
      router.push('/login')
    }, 800)
  } catch (err) {
    error.value = err.message || '회원가입 중 오류가 발생했습니다.'
  } finally {
    loading.value = false
  }
}

const goToLogin = () => {
  router.push('/login')
}

onMounted(() => {
  backgroundImage.value = 'https://images.unsplash.com/photo-1557821552-17105176677c?w=1200&h=1200&fit=crop'
})
</script>

<style scoped>
.register-page { display: flex; height: 100vh; background: #f0f2f5; }
.register-left { flex:1; background-color:#007aff; background-size:cover; background-position:center; background-repeat:no-repeat; color:white; display:flex; flex-direction:column; justify-content:center; align-items:center; padding:32px; }
.register-left h1 { font-size:48px; margin-bottom:16px; text-shadow: 2px 2px 4px rgba(0,0,0,0.5); }
.register-left p { font-size:20px; text-shadow: 1px 1px 2px rgba(0,0,0,0.5); }
.register-right { flex:1; display:flex; justify-content:center; align-items:center; }
.register-container { background:white; padding:28px; border-radius:8px; box-shadow:0 0 10px rgba(0,0,0,0.1); width:460px; display:flex; flex-direction:column; align-items:center; }
.register-container h2 { margin-bottom:20px; }
.register-container form { width:100%; display:flex; flex-direction:column; align-items:center; }
.field-block { width:100%; max-width:410px; margin-bottom:10px; }
.field-label { display:block; font-size:0.85rem; color:#374151; margin:0 0 6px 2px; font-weight:600; }
.field-row { width:100%; max-width:410px; display:flex; gap:6px; margin-bottom:0; }
.register-container input { width:100%; padding:8px; border:1px solid #ccc; border-radius:4px; margin-bottom:10px; }
.field-row input { margin-bottom:0; flex:1; min-width:0; }
.check-btn { flex:0 0 84px; width:84px; min-width:84px; padding:8px 6px; background:#e8eefc; color:#1d4ed8; border:1px solid #c7d2fe; border-radius:4px; cursor:pointer; margin-top:0; }
.check-btn:disabled { background:#d1d5db; color:#6b7280; cursor:default; }
.register-container form > button { width:100%; max-width:410px; padding:10px; background:#007aff; color:white; border:none; border-radius:4px; cursor:pointer; margin-top:8px; }
.register-container form > button:disabled { background:#aaa; cursor:default; }
.secondary-btn { width:100%; max-width:410px; background:#f3f4f6 !important; color:#374151 !important; margin-top:10px; }
.status-msg { width:100%; max-width:410px; margin:4px 0 0; font-size:0.82rem; line-height:1.35; }
.status-msg + .status-msg { margin-top:2px; }
.error-msg { color:#dc2626; margin-top:8px; font-size:0.85rem; }
.ok-msg { color:#16a34a; margin-top:8px; font-size:0.85rem; }
</style>
