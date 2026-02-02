<template>
  <div class="settings-page">
    <div class="settings-header">
      <h1>설정</h1>
    </div>

    <div class="settings-container">
      <div class="settings-list">
        <div class="setting-item" @click="openPasswordModal">
          <span class="label">비밀번호 변경</span>
          <span class="arrow">›</span>
        </div>
        <div class="setting-item" @click="openNicknameModal">
          <span class="label">닉네임 변경</span>
          <span class="arrow">›</span>
        </div>
        <div class="setting-item" @click="openProfileImageModal">
          <span class="label">프로필 사진 변경</span>
          <span class="arrow">›</span>
        </div>
        <div class="setting-item" @click="openDeleteImageModal">
          <span class="label">프로필 사진 삭제</span>
          <span class="arrow">›</span>
        </div>
      </div>
    </div>

    <!-- 비밀번호 변경 모달 -->
    <div v-if="showPasswordModal" class="modal-overlay" @click.self="closePasswordModal">
      <div class="modal-content">
        <div class="modal-header">
          <h2>비밀번호 변경</h2>
          <button class="close-btn" @click="closePasswordModal">✕</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>새 비밀번호</label>
            <div class="password-input-wrapper">
              <input 
                v-model="passwordForm.newPassword" 
                :type="showPassword ? 'text' : 'password'"
                placeholder="새 비밀번호 입력"
                @keyup.enter="changePassword"
                :class="{ 'input-error': passwordForm.newPassword && !isPasswordStrong, 'input-success': isPasswordStrong }"
              />
              <button 
                type="button" 
                class="toggle-password-btn" 
                @click="showPassword = !showPassword"
                tabindex="-1"
              >
                {{ showPassword ? '👁️' : '👁️‍🗨️' }}
              </button>
            </div>
            <p class="info-msg">영문 대소문자, 숫자, 특수기호 포함 8~20자</p>
            <p v-if="passwordForm.newPassword && !isPasswordStrong" class="error-msg">✗ 비밀번호 형식이 올바르지 않습니다</p>
            <p v-if="isPasswordStrong" class="success-msg">✓ 사용 가능한 비밀번호입니다</p>
          </div>
          <div class="form-group">
            <label>새 비밀번호 확인</label>
            <div class="password-input-wrapper">
              <input 
                v-model="passwordForm.newPasswordConfirm" 
                :type="showPasswordConfirm ? 'text' : 'password'"
                placeholder="새 비밀번호 확인"
                @keyup.enter="changePassword"
                :class="{ 'input-error': passwordMismatch, 'input-success': passwordMatch }"
              />
              <button 
                type="button" 
                class="toggle-password-btn" 
                @click="showPasswordConfirm = !showPasswordConfirm"
                tabindex="-1"
              >
                {{ showPasswordConfirm ? '👁️' : '👁️‍🗨️' }}
              </button>
            </div>
            <p v-if="passwordMatch" class="success-msg">✓ 비밀번호가 일치합니다</p>
            <p v-if="passwordMismatch" class="error-msg">✗ 비밀번호가 일치하지 않습니다</p>
          </div>
          <p v-if="passwordError" class="error-msg">{{ passwordError }}</p>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="closePasswordModal">취소</button>
          <button class="submit-btn" @click="changePassword" :disabled="isLoading || !isPasswordValid">
            {{ isLoading ? '변경 중...' : '변경' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 닉네임 변경 모달 -->
    <div v-if="showNicknameModal" class="modal-overlay" @click.self="closeNicknameModal">
      <div class="modal-content">
        <div class="modal-header">
          <h2>닉네임 변경</h2>
          <button class="close-btn" @click="closeNicknameModal">✕</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>새 닉네임</label>
            <input 
              v-model="nicknameForm.newNickname" 
              type="text" 
              placeholder="새 닉네임 입력"
              @keyup.enter="changeNickname"
            />
          </div>
          <p v-if="nicknameError" class="error-msg">{{ nicknameError }}</p>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="closeNicknameModal">취소</button>
          <button class="submit-btn" @click="changeNickname" :disabled="isLoading">
            {{ isLoading ? '변경 중...' : '변경' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 프로필 사진 변경 모달 -->
    <div v-if="showProfileImageModal" class="modal-overlay" @click.self="closeProfileImageModal">
      <div class="modal-content">
        <div class="modal-header">
          <h2>프로필 사진 변경</h2>
          <button class="close-btn" @click="closeProfileImageModal">✕</button>
        </div>
        <div class="modal-body">
          <div class="current-profile-section">
            <label>현재 프로필 사진</label>
            <div class="current-profile-image">
              <img 
                :src="currentProfileImage" 
                alt="현재 프로필"
                @error="handleImageError"
              />
            </div>
          </div>
          <div class="form-group">
            <label>새 이미지 파일 선택</label>
            <input 
              type="file" 
              accept="image/*"
              @change="handleFileChange"
              ref="fileInput"
            />
          </div>
          <div v-if="previewImage" class="image-preview">
            <label>미리보기</label>
            <img :src="previewImage" alt="Preview" />
          </div>
          <p v-if="imageError" class="error-msg">{{ imageError }}</p>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="closeProfileImageModal">취소</button>
          <button class="submit-btn" @click="uploadProfileImage" :disabled="isLoading || !selectedFile">
            {{ isLoading ? '업로드 중...' : '업로드' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 프로필 사진 삭제 확인 모달 -->
    <div v-if="showDeleteImageModal" class="modal-overlay" @click.self="closeDeleteImageModal">
      <div class="modal-content">
        <div class="modal-header">
          <h2>프로필 사진 삭제</h2>
          <button class="close-btn" @click="closeDeleteImageModal">✕</button>
        </div>
        <div class="modal-body">
          <p>정말로 프로필 사진을 삭제하시겠습니까?</p>
          <p v-if="deleteError" class="error-msg">{{ deleteError }}</p>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="closeDeleteImageModal">취소</button>
          <button class="submit-btn danger" @click="deleteProfileImage" :disabled="isLoading">
            {{ isLoading ? '삭제 중...' : '삭제' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 성공 메시지 토스트 -->
    <div v-if="successMessage" class="toast success">{{ successMessage }}</div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import api from '@/services/api'
import { useCurrentUser } from '@/composables/useCurrentUser'

// 현재 사용자 정보
const { currentUser, loadCurrentUser } = useCurrentUser()

// 기본 프로필 사진 - 회색 배경에 사람 아이콘
const DEFAULT_PROFILE_IMAGE = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgdmlld0JveD0iMCAwIDEwMCAxMDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxyZWN0IHdpZHRoPSIxMDAiIGhlaWdodD0iMTAwIiBmaWxsPSIjRTBFMEUwIi8+CjxjaXJjbGUgY3g9IjUwIiBjeT0iMzUiIHI9IjE1IiBmaWxsPSIjOTk5Ii8+CjxwYXRoIGQ9Ik0yMCA2NSBDMjAgNjAgMzAgNTAgNTAgNTAgQzcwIDUwIDgwIDYwIDgwIDY1IiBmaWxsPSIjOTk5Ii8+Cjwvc3ZnPg=='

// 모달 표시 상태
const showPasswordModal = ref(false)
const showNicknameModal = ref(false)
const showProfileImageModal = ref(false)
const showDeleteImageModal = ref(false)

// 폼 데이터
const passwordForm = ref({
  newPassword: '',
  newPasswordConfirm: ''
})

// 비밀번호 표시 상태
const showPassword = ref(false)
const showPasswordConfirm = ref(false)

const nicknameForm = ref({
  newNickname: ''
})

const selectedFile = ref(null)
const previewImage = ref('')
const fileInput = ref(null)

// 현재 프로필 이미지 URL (null이면 기본 이미지)
const currentProfileImage = computed(() => {
  const imageUrl = currentUser.value.profileImageUrl
  if (!imageUrl || imageUrl === null || (typeof imageUrl === 'string' && !imageUrl.trim())) {
    return DEFAULT_PROFILE_IMAGE
  }
  return imageUrl
})

// 에러 메시지
const passwordError = ref('')
const nicknameError = ref('')
const imageError = ref('')
const deleteError = ref('')

// 성공 메시지
const successMessage = ref('')

// 로딩 상태
const isLoading = ref(false)

// 비밀번호 유효성 검사 정규식
// 영문 대문자, 소문자, 숫자, 특수기호 각 1개 이상 포함, 8~20자
const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?])[A-Za-z\d!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]{8,20}$/

// 비밀번호 강도 체크
const isPasswordStrong = computed(() => {
  return passwordRegex.test(passwordForm.value.newPassword)
})

// 비밀번호 일치 여부 확인 (computed)
const passwordMatch = computed(() => {
  return passwordForm.value.newPassword && 
         passwordForm.value.newPasswordConfirm && 
         passwordForm.value.newPassword === passwordForm.value.newPasswordConfirm
})

const passwordMismatch = computed(() => {
  return passwordForm.value.newPasswordConfirm && 
         passwordForm.value.newPassword !== passwordForm.value.newPasswordConfirm
})

const isPasswordValid = computed(() => {
  return isPasswordStrong.value && 
         passwordForm.value.newPasswordConfirm && 
         passwordMatch.value
})

// 모달 열기 함수들
const openPasswordModal = () => {
  showPasswordModal.value = true
  passwordForm.value = { newPassword: '', newPasswordConfirm: '' }
  passwordError.value = ''
  showPassword.value = false
  showPasswordConfirm.value = false
}

const openNicknameModal = () => {
  showNicknameModal.value = true
  nicknameForm.value = { newNickname: '' }
  nicknameError.value = ''
}

const openProfileImageModal = () => {
  showProfileImageModal.value = true
  selectedFile.value = null
  previewImage.value = ''
  imageError.value = ''
}

const openDeleteImageModal = () => {
  showDeleteImageModal.value = true
  deleteError.value = ''
}

// 모달 닫기 함수들
const closePasswordModal = () => {
  showPasswordModal.value = false
  passwordForm.value = { newPassword: '', newPasswordConfirm: '' }
  passwordError.value = ''
  showPassword.value = false
  showPasswordConfirm.value = false
}

const closeNicknameModal = () => {
  showNicknameModal.value = false
  nicknameForm.value = { newNickname: '' }
  nicknameError.value = ''
}

const closeProfileImageModal = () => {
  showProfileImageModal.value = false
  selectedFile.value = null
  previewImage.value = ''
  imageError.value = ''
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

const closeDeleteImageModal = () => {
  showDeleteImageModal.value = false
  deleteError.value = ''
}

// 파일 선택 처리
const handleFileChange = (event) => {
  const file = event.target.files[0]
  if (file) {
    if (!file.type.startsWith('image/')) {
      imageError.value = '이미지 파일만 선택할 수 있습니다.'
      selectedFile.value = null
      previewImage.value = ''
      return
    }
    selectedFile.value = file
    imageError.value = ''
    
    // 미리보기 생성
    const reader = new FileReader()
    reader.onload = (e) => {
      previewImage.value = e.target.result
    }
    reader.readAsDataURL(file)
  }
}

// 이미지 로드 에러 처리 (기본 이미지로 대체)
const handleImageError = (event) => {
  event.target.src = DEFAULT_PROFILE_IMAGE
}

// 성공 메시지 표시
const showSuccess = (message) => {
  successMessage.value = message
  setTimeout(() => {
    successMessage.value = ''
  }, 3000)
}

// 비밀번호 변경
const changePassword = async () => {
  passwordError.value = ''
  
  if (!passwordForm.value.newPassword || !passwordForm.value.newPasswordConfirm) {
    passwordError.value = '모든 필드를 입력해주세요.'
    return
  }
  
  if (passwordForm.value.newPassword !== passwordForm.value.newPasswordConfirm) {
    passwordError.value = '비밀번호가 일치하지 않습니다.'
    return
  }
  
  isLoading.value = true
  
  try {
    const response = await api.updatePassword({
      newPassword: passwordForm.value.newPassword,
      newPasswordConfirm: passwordForm.value.newPasswordConfirm
    })
    
    closePasswordModal()
    showSuccess(response.data.message || '비밀번호를 변경했습니다.')
  } catch (error) {
    console.error('비밀번호 변경 실패:', error)
    passwordError.value = error.response?.data?.message || '비밀번호 변경에 실패했습니다.'
  } finally {
    isLoading.value = false
  }
}

// 닉네임 변경
const changeNickname = async () => {
  nicknameError.value = ''
  
  if (!nicknameForm.value.newNickname.trim()) {
    nicknameError.value = '닉네임을 입력해주세요.'
    return
  }
  
  isLoading.value = true
  
  try {
    const response = await api.updateNickname({
      newNickname: nicknameForm.value.newNickname
    })
    
    closeNicknameModal()
    showSuccess(response.data.message || '닉네임을 변경했습니다.')
    // 사용자 정보 업데이트
    await loadCurrentUser(true)
  } catch (error) {
    console.error('닉네임 변경 실패:', error)
    nicknameError.value = error.response?.data?.message || '닉네임 변경에 실패했습니다.'
  } finally {
    isLoading.value = false
  }
}

// 프로필 사진 업로드
const uploadProfileImage = async () => {
  imageError.value = ''
  
  if (!selectedFile.value) {
    imageError.value = '파일을 선택해주세요.'
    return
  }
  
  isLoading.value = true
  
  try {
    const formData = new FormData()
    formData.append('multipartFile', selectedFile.value)
    
    const response = await api.uploadProfileImage(formData)
    
    closeProfileImageModal()
    showSuccess(response.data.message || '프로필 이미지 업로드에 성공했습니다.')
    // 사용자 정보 업데이트
    await loadCurrentUser(true)
  } catch (error) {
    console.error('프로필 이미지 업로드 실패:', error)
    imageError.value = error.response?.data?.message || '프로필 이미지 업로드에 실패했습니다.'
  } finally {
    isLoading.value = false
  }
}

// 프로필 사진 삭제
const deleteProfileImage = async () => {
  deleteError.value = ''
  isLoading.value = true
  
  try {
    await api.deleteProfileImage()
    
    closeDeleteImageModal()
    showSuccess('프로필 이미지를 삭제했습니다.')
    // 사용자 정보 업데이트
    await loadCurrentUser(true)
  } catch (error) {
    console.error('프로필 이미지 삭제 실패:', error)
    deleteError.value = error.response?.data?.message || '프로필 이미지 삭제에 실패했습니다.'
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.settings-page {
  padding: 20px;
  width: 100%;
  max-width: 600px;
  margin: 0 auto;
}

.settings-header {
  margin-bottom: 20px;
}

.settings-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.settings-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.settings-list {
  padding: 0;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
  cursor: pointer;
  transition: background-color 0.2s;
}

.setting-item:last-child {
  border-bottom: none;
}

.setting-item:hover {
  background-color: #f8f8f8;
}

.setting-item .label {
  font-size: 15px;
  font-weight: 500;
  color: #333;
}

.setting-item .arrow {
  font-size: 20px;
  color: #999;
  font-weight: 300;
}

/* 모달 스타일 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 450px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #eee;
}

.modal-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: #999;
  cursor: pointer;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background-color 0.2s;
}

.close-btn:hover {
  background-color: #f0f0f0;
}

.modal-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.current-profile-section {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.current-profile-section label {
  display: block;
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.current-profile-image {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 16px;
  background-color: #f8f8f8;
  border-radius: 8px;
}

.current-profile-image img {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #ddd;
}

.form-group input[type="text"],
.form-group input[type="password"] {
  width: 100%;
  padding: 10px 40px 10px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  box-sizing: border-box;
  transition: border-color 0.2s;
}

.password-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.password-input-wrapper input {
  flex: 1;
  padding-right: 40px;
}

.toggle-password-btn {
  position: absolute;
  right: 8px;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 18px;
  padding: 4px 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0.6;
  transition: opacity 0.2s;
  user-select: none;
}

.toggle-password-btn:hover {
  opacity: 1;
}

.form-group input[type="text"]:focus,
.form-group input[type="password"]:focus {
  outline: none;
  border-color: #007aff;
}

.form-group input[type="file"] {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  box-sizing: border-box;
}

.image-preview {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #eee;
}

.image-preview label {
  display: block;
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.image-preview img {
  max-width: 100%;
  max-height: 200px;
  border-radius: 8px;
  object-fit: contain;
  display: block;
  margin: 0 auto;
}

.error-msg {
  margin-top: 8px;
  color: #d32f2f;
  font-size: 13px;
}

.success-msg {
  margin-top: 8px;
  color: #4caf50;
  font-size: 13px;
}

.info-msg {
  margin-top: 8px;
  color: #666;
  font-size: 12px;
}

.input-error {
  border-color: #d32f2f !important;
}

.input-success {
  border-color: #4caf50 !important;
}

.modal-footer {
  display: flex;
  gap: 10px;
  padding: 16px 20px;
  border-top: 1px solid #eee;
}

.cancel-btn,
.submit-btn {
  flex: 1;
  padding: 10px 16px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
}

.cancel-btn {
  background-color: #f0f0f0;
  color: #333;
}

.cancel-btn:hover {
  background-color: #e0e0e0;
}

.submit-btn {
  background-color: #007aff;
  color: white;
}

.submit-btn:hover:not(:disabled) {
  background-color: #0056d4;
}

.submit-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.submit-btn.danger {
  background-color: #d32f2f;
}

.submit-btn.danger:hover:not(:disabled) {
  background-color: #c62828;
}

/* 토스트 메시지 */
.toast {
  position: fixed;
  bottom: 30px;
  left: 50%;
  transform: translateX(-50%);
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1001;
  animation: slideUp 0.3s ease-out;
}

.toast.success {
  background-color: #4caf50;
  color: white;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateX(-50%) translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
}
</style>
