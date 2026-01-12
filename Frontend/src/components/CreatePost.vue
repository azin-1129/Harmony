<template>
  <div class="create-post-page">
    <div class="create-header">
      <h1>게시물 작성</h1>
    </div>

    <div class="create-container">
      <form @submit.prevent="handleCreatePost">
        <div class="form-group">
          <label for="content">내용</label>
          <textarea
            id="content"
            v-model="formData.content"
            placeholder="무엇이 생각나세요?"
            rows="8"
            class="textarea"
          ></textarea>
        </div>

        <div class="form-group">
          <label for="image">이미지</label>
          <input
            id="image"
            type="file"
            @change="handleImageUpload"
            accept="image/*"
            class="file-input"
          />
          <div v-if="formData.imagePreview" class="image-preview">
            <img :src="formData.imagePreview" alt="Preview" />
            <button type="button" @click="removeImage" class="remove-btn">✕</button>
          </div>
        </div>

        <div class="form-actions">
          <button type="submit" :disabled="loading" class="submit-btn">
            {{ loading ? '작성 중...' : '게시' }}
          </button>
          <button type="button" @click="resetForm" class="cancel-btn">취소</button>
        </div>
      </form>
    </div>

    <p v-if="error" class="error-msg">{{ error }}</p>
    <p v-if="success" class="success-msg">게시물이 작성되었습니다!</p>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const error = ref('')
const success = ref(false)

const formData = ref({
  content: '',
  image: null,
  imagePreview: ''
})

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
}

const handleCreatePost = async () => {
  if (!formData.value.content.trim()) {
    error.value = '내용을 입력해주세요.'
    return
  }

  loading.value = true
  error.value = ''
  success.value = false

  try {
    // API 호출 (나중에 실제 API 연결)
    console.log('Post 생성:', formData.value)
    
    success.value = true
    setTimeout(() => {
      resetForm()
      router.push('/home')
    }, 1500)
  } catch (err) {
    error.value = err.message || '게시물 작성에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  formData.value = {
    content: '',
    image: null,
    imagePreview: ''
  }
  error.value = ''
  success.value = false
}
</script>

<style scoped>
.create-post-page {
  padding: 20px;
  width: 100%;
  max-width: 600px;
}

.create-header {
  margin-bottom: 30px;
}

.create-header h1 {
  margin: 0;
}

.create-container {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}

form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-weight: 600;
  font-size: 0.95em;
}

.textarea {
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-family: inherit;
  resize: vertical;
  outline: none;
  transition: border 0.2s;
}

.textarea:focus {
  border-color: #007aff;
}

.file-input {
  padding: 8px;
}

.image-preview {
  position: relative;
  margin-top: 10px;
  border-radius: 8px;
  overflow: hidden;
}

.image-preview img {
  width: 100%;
  max-height: 300px;
  object-fit: cover;
  border-radius: 8px;
}

.remove-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 30px;
  height: 30px;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  font-size: 1.2em;
  transition: background 0.2s;
}

.remove-btn:hover {
  background: rgba(0, 0, 0, 0.8);
}

.form-actions {
  display: flex;
  gap: 10px;
}

.submit-btn,
.cancel-btn {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: background 0.2s;
}

.submit-btn {
  background-color: #007aff;
  color: white;
}

.submit-btn:hover:not(:disabled) {
  background-color: #0056d4;
}

.submit-btn:disabled {
  background-color: #aaa;
  cursor: default;
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
</style>
