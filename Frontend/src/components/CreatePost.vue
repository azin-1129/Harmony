<template>
  <div class="create-post-page">
    <div class="create-header">
      <h1>게시물 작성</h1>
    </div>

    <div class="create-container">
      <form @submit.prevent="handleCreatePost">
        <div class="form-content">
          <!-- Left: Image Upload Area -->
          <div class="image-section">
            <div 
              class="image-upload-area"
              @click="$refs.fileInput.click()"
              :style="{ backgroundImage: formData.imagePreview ? `url('${formData.imagePreview}')` : 'none' }"
            >
              <div v-if="!formData.imagePreview" class="upload-placeholder">
                <div class="upload-icon">📷</div>
                <p>이미지를 클릭하여 업로드</p>
              </div>
              <button 
                v-if="formData.imagePreview" 
                type="button" 
                @click.stop="removeImage" 
                class="remove-btn"
              >
                ✕
              </button>
            </div>
            <input
              ref="fileInput"
              id="image"
              type="file"
              @change="handleImageUpload"
              accept="image/*"
              class="file-input"
              style="display: none;"
            />
          </div>

          <!-- Right: Content and Options -->
          <div class="form-section">
            <!-- Top: Content Input -->
            <div class="form-group content-group">
              <textarea
                id="content"
                v-model="formData.content"
                placeholder="텍스트를 입력하세요..."
                class="textarea"
              ></textarea>
            </div>

            <!-- Bottom: Options -->
            <div class="form-group options-group">
              <label class="checkbox-label">
                <input
                  type="checkbox"
                  v-model="formData.disableComments"
                  class="checkbox-input"
                />
                <span>댓글 기능 해제</span>
              </label>
            </div>
          </div>
        </div>

        <!-- Actions -->
        <div class="form-actions">
          <button 
            v-if="formData.imagePreview"
            type="submit" 
            :disabled="loading" 
            class="submit-btn"
          >
            {{ loading ? '작성 중...' : '게시' }}
          </button>
          <button type="button" @click="handleCancel" class="cancel-btn">취소</button>
        </div>
      </form>
    </div>

    <p v-if="error" class="error-msg">{{ error }}</p>
    <p v-if="success" class="success-msg">게시물이 작성되었습니다!</p>

    <!-- Confirm Modal -->
    <div v-if="showConfirmModal" class="modal-overlay" @click="showConfirmModal = false">
      <div class="modal-content" @click.stop>
        <h2>취소하시겠습니까?</h2>
        <p>작성 중인 게시물이 저장되지 않습니다.</p>
        <div class="modal-actions">
          <button @click="confirmCancel" class="modal-confirm-btn">확인</button>
          <button @click="showConfirmModal = false" class="modal-cancel-btn">계속 작성</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { apiFetch } from '../utils/apiClient'

const router = useRouter()
const loading = ref(false)
const error = ref('')
const success = ref(false)
const showConfirmModal = ref(false)

const formData = ref({
  content: '',
  image: null,
  imagePreview: '',
  disableComments: false
})

const fileInput = ref(null)

const handleImageUpload = (event) => {
  const file = event.target.files?.[0]
  if (file) {
    const reader = new FileReader()
    reader.onload = (e) => {
      formData.value.imagePreview = e.target?.result
      formData.value.image = file
    }
    reader.readAsDataURL(file)
  }
}

const removeImage = () => {
  formData.value.image = null
  formData.value.imagePreview = ''
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

const handleCreatePost = async () => {
  if (!formData.value.content.trim()) {
    error.value = '내용을 입력해주세요.'
    return
  }

  if (!formData.value.image) {
    error.value = '이미지를 업로드해주세요.'
    return
  }

  loading.value = true
  error.value = ''
  success.value = false

  try {
    const formDataToSend = new FormData()
    
    // multipartfile: 이미지 파일
    formDataToSend.append('multipartfile', formData.value.image)
    
    // writeArticleRequestDto: JSON 형식
    const requestDto = {
      articleText: formData.value.content
    }
    const dtoBlob = new Blob([JSON.stringify(requestDto)], { type: 'application/json' })
    formDataToSend.append('writeArticleRequestDto', dtoBlob)

    const response = await apiFetch('/article/write', {
      method: 'POST',
      body: formDataToSend
    })

    if (!response.ok) {
      const errorData = await response.json()
      throw new Error(errorData.message || '게시물 작성에 실패했습니다.')
    }

    const result = await response.json()
    console.log('게시물 작성 성공:', result)
    
    success.value = true
    setTimeout(() => {
      resetForm()
      router.push('/home')
    }, 1500)
  } catch (err) {
    console.error('게시물 작성 오류:', err)
    error.value = err.message || '게시물 작성에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  formData.value = {
    content: '',
    image: null,
    imagePreview: '',
    disableComments: false
  }
  if (fileInput.value) {
    fileInput.value.value = ''
  }
  error.value = ''
  success.value = false
}

const handleCancel = () => {
  showConfirmModal.value = true
}

const confirmCancel = () => {
  showConfirmModal.value = false
  resetForm()
  router.back()
}
</script>

<style scoped>
.create-post-page {
  padding: 20px;
  width: 100%;
  max-width: 900px;
  margin: 0 auto;
}

.create-header {
  margin-bottom: 30px;
}

.create-header h1 {
  margin: 0;
  font-size: 1.8em;
}

.create-container {
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
  align-items: stretch;
  min-height: 400px;
}

/* Left: Image Upload Area */
.image-section {
  display: flex;
  flex-direction: column;
}

.image-upload-area {
  flex: 1;
  border: 3px dashed #ccc;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  position: relative;
  transition: all 0.3s ease;
  overflow: hidden;
}

.image-upload-area:hover:not(:has(img)) {
  border-color: #007aff;
  background-color: #f0f8ff;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
  color: #999;
  font-size: 1.1em;
  text-align: center;
}

.upload-icon {
  font-size: 3em;
}

.upload-placeholder p {
  margin: 0;
  font-size: 1em;
}

.remove-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 38px;
  height: 38px;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  font-size: 1.4em;
  transition: background 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.remove-btn:hover {
  background: rgba(0, 0, 0, 0.9);
}

.file-input {
  padding: 8px;
}

/* Right: Form Section */
.form-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
  height: 100%;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.content-group {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.textarea {
  flex: 1;
  min-height: 250px;
  padding: 14px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-family: inherit;
  font-size: 1em;
  resize: none;
  outline: none;
  transition: border 0.2s;
}

.textarea:focus {
  border-color: #007aff;
  box-shadow: 0 0 0 3px rgba(0, 122, 255, 0.1);
}

.options-group {
  display: flex;
  align-items: flex-start;
  flex-shrink: 0;
  width: 100%;
  padding: 12px 0 0 0;
  margin: 0;
  border-top: 1px solid #eee;
  justify-content: flex-start;
  text-align: left;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  font-size: 0.95em;
  user-select: none;
  padding: 0;
  margin: 0;
  text-align: left;
}

.checkbox-input {
  width: 18px;
  height: 18px;
  cursor: pointer;
  margin: 0;
}

/* Actions */
.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.submit-btn,
.cancel-btn {
  padding: 12px 28px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  font-size: 0.95em;
  transition: all 0.2s;
}

.submit-btn {
  background-color: #007aff;
  color: white;
  min-width: 100px;
}

.submit-btn:hover:not(:disabled) {
  background-color: #0056d4;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 122, 255, 0.3);
}

.submit-btn:disabled {
  background-color: #aaa;
  cursor: default;
  transform: none;
}

.cancel-btn {
  background-color: #f0f0f0;
  color: #333;
}

.cancel-btn:hover {
  background-color: #e0e0e0;
}

.error-msg {
  color: #d32f2f;
  text-align: center;
  padding: 12px;
  background-color: #ffebee;
  border-radius: 8px;
  margin-top: 15px;
}

.success-msg {
  color: #388e3c;
  text-align: center;
  padding: 12px;
  background-color: #e8f5e9;
  border-radius: 8px;
  margin-top: 15px;
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  padding: 30px;
  max-width: 400px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.modal-content h2 {
  margin: 0 0 10px 0;
  font-size: 1.3em;
  color: #333;
}

.modal-content p {
  margin: 0 0 25px 0;
  color: #666;
  font-size: 0.95em;
}

.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.modal-confirm-btn,
.modal-cancel-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s;
}

.modal-confirm-btn {
  background-color: #d32f2f;
  color: white;
}

.modal-confirm-btn:hover {
  background-color: #b71c1c;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(211, 47, 47, 0.3);
}

.modal-cancel-btn {
  background-color: #f0f0f0;
  color: #333;
}

.modal-cancel-btn:hover {
  background-color: #e0e0e0;
}

/* Responsive Design */
@media (max-width: 768px) {
  .form-content {
    grid-template-columns: 1fr;
    min-height: auto;
    gap: 20px;
  }

  .form-actions {
    flex-direction: column;
  }

  .submit-btn,
  .cancel-btn {
    width: 100%;
  }
}
</style>
