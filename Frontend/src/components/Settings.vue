<template>
  <div class="settings-page">
    <div class="settings-header">
      <h1>설정</h1>
    </div>

    <div class="settings-container">
      <section class="settings-section">
        <h2>계정</h2>
        <div class="setting-item">
          <span class="label">이메일</span>
          <span class="value">{{ userEmail }}</span>
        </div>
        <button class="setting-btn">비밀번호 변경</button>
      </section>

      <section class="settings-section">
        <h2>알림</h2>
        <div class="setting-toggle">
          <span class="label">푸시 알림</span>
          <input v-model="settings.pushNotification" type="checkbox" class="toggle" />
        </div>
        <div class="setting-toggle">
          <span class="label">이메일 알림</span>
          <input v-model="settings.emailNotification" type="checkbox" class="toggle" />
        </div>
      </section>

      <section class="settings-section">
        <h2>개인정보</h2>
        <div class="setting-toggle">
          <span class="label">프로필 공개</span>
          <input v-model="settings.profilePublic" type="checkbox" class="toggle" />
        </div>
        <div class="setting-toggle">
          <span class="label">활동 상태 표시</span>
          <input v-model="settings.showActiveStatus" type="checkbox" class="toggle" />
        </div>
      </section>

      <section class="settings-section">
        <h2>테마</h2>
        <div class="setting-item">
          <label for="theme">테마 선택</label>
          <select v-model="settings.theme" id="theme" class="theme-select">
            <option value="light">라이트</option>
            <option value="dark">다크</option>
            <option value="auto">자동</option>
          </select>
        </div>
      </section>

      <section class="settings-section danger">
        <h2>위험 영역</h2>
        <button class="delete-btn">계정 삭제</button>
      </section>

      <div class="action-buttons">
        <button @click="saveSettings" class="save-btn">설정 저장</button>
      </div>
    </div>

    <p v-if="message" :class="['message', messageType]">{{ message }}</p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAccessToken } from '@/utils/tokenService'

const userEmail = ref('user@example.com')
const message = ref('')
const messageType = ref('')

const settings = ref({
  pushNotification: true,
  emailNotification: false,
  profilePublic: true,
  showActiveStatus: true,
  theme: 'light'
})

onMounted(() => {
  // 나중에 API에서 사용자 정보와 설정 불러오기
  const token = getAccessToken()
  console.log('Token:', token)
})

const saveSettings = () => {
  // API 호출해서 설정 저장
  console.log('Settings saved:', settings.value)
  message.value = '설정이 저장되었습니다.'
  messageType.value = 'success'
  setTimeout(() => {
    message.value = ''
  }, 3000)
}
</script>

<style scoped>
.settings-page {
  padding: 20px;
  width: 100%;
  max-width: 600px;
}

.settings-header {
  margin-bottom: 30px;
}

.settings-header h1 {
  margin: 0;
}

.settings-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.settings-section {
  padding: 20px;
  border-bottom: 1px solid #eee;
}

.settings-section:last-of-type {
  border-bottom: none;
}

.settings-section.danger {
  background-color: #fff5f5;
}

.settings-section h2 {
  margin: 0 0 15px 0;
  font-size: 1.1em;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.setting-item:last-child {
  border-bottom: none;
}

.setting-item label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
}

.setting-toggle {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.setting-toggle:last-child {
  border-bottom: none;
}

.label {
  font-weight: 500;
}

.value {
  color: #666;
}

.toggle {
  width: 18px;
  height: 18px;
  cursor: pointer;
}

.theme-select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  outline: none;
  cursor: pointer;
}

.setting-btn,
.delete-btn,
.save-btn {
  width: 100%;
  padding: 10px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: background 0.2s;
  margin-top: 10px;
}

.setting-btn {
  background-color: #f0f0f0;
  color: #333;
}

.setting-btn:hover {
  background-color: #e0e0e0;
}

.delete-btn {
  background-color: #d32f2f;
  color: white;
}

.delete-btn:hover {
  background-color: #c62828;
}

.action-buttons {
  padding: 20px;
  background-color: #f9f9f9;
  display: flex;
  gap: 10px;
}

.save-btn {
  background-color: #007aff;
  color: white;
}

.save-btn:hover {
  background-color: #0056d4;
}

.message {
  margin-top: 20px;
  padding: 12px;
  border-radius: 8px;
  text-align: center;
  font-weight: 500;
}

.message.success {
  background-color: #e8f5e9;
  color: #388e3c;
}

.message.error {
  background-color: #ffebee;
  color: #d32f2f;
}
</style>
