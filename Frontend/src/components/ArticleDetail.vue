<template>
  <!-- 모달 오버레이 -->
  <div class="modal-overlay" @click="closeModal">
    <!-- 닫기 버튼 (팝업 밖) -->
    <button class="close-btn" @click="closeModal">✕</button>
    
    <!-- 모달 콘텐츠 (오버레이 클릭 방지) -->
    <div class="modal-content" @click.stop>
      <!-- 로딩 중 -->
      <div v-if="loading" class="loading-message">
        로딩 중...
      </div>

      <!-- 에러 -->
      <div v-else-if="error" class="error-message">
        {{ error }}
      </div>

      <!-- 게시물 표시 -->
      <div v-else-if="article" class="article-container">
        <!-- 상단: 프로필 사진, 닉네임, 메뉴/편집 버튼 -->
        <div class="article-header">
          <img 
            :src="article.authorProfileImageUrl || 'https://via.placeholder.com/50'" 
            alt="프로필" 
            class="profile-image"
          />
          <span class="author-nickname">{{ article.authorNickname }}</span>
          
          <!-- 편집 모드가 아닐 때: 메뉴 버튼 -->
          <template v-if="!isEditMode">
            <button class="menu-btn" @click="toggleMenu">⋮</button>
            <div v-if="showMenu" class="menu-dropdown" @click.stop>
              <button class="menu-item" @click="handleEdit">수정</button>
              <button class="menu-item delete" @click="handleDelete">삭제</button>
            </div>
          </template>
          
          <!-- 편집 모드일 때: 완료/취소 버튼 -->
          <template v-else>
            <button class="edit-action-btn cancel" @click="handleCancelEdit">취소</button>
            <button class="edit-action-btn complete" @click="handleCompleteEdit">완료</button>
          </template>
        </div>

        <!-- 중단: 이미지 (편집 모드) -->
        <div class="article-image-section">
          <div v-if="isEditMode" class="image-edit-overlay" @click="$refs.fileInput.click()">
            <div class="edit-icon">📷 이미지 변경</div>
          </div>
          <img 
            :src="editData.imagePreview || article.articleMediaUrl" 
            :alt="editData.text || article.articleText" 
            class="article-image"
          />
          <input
            ref="fileInput"
            type="file"
            @change="handleImageChange"
            accept="image/*"
            style="display: none;"
          />
        </div>

        <!-- 이미지 아래: 닉네임과 텍스트 (편집 가능) -->
        <div class="article-content">
          <span class="content-nickname">{{ article.authorNickname }}</span>
          <textarea 
            v-if="isEditMode"
            v-model="editData.text"
            class="content-text-edit"
            placeholder="텍스트를 입력하세요..."
          ></textarea>
          <span v-else class="content-text">{{ article.articleText }}</span>
        </div>

        <!-- 하단: 좋아요, 댓글 버튼 (편집 모드가 아닐 때만) -->
        <div v-if="!isEditMode" class="article-actions">
          <button class="action-btn like-btn" @click="toggleLike">
            <span v-if="article.isLiked">❤️</span>
            <span v-else>🤍</span>
            <span class="count">{{ article.articleLikeCount }}</span>
          </button>
          <button class="action-btn comment-btn" @click="toggleComments">
            💬 <span class="count">{{ article.articleCommentCount }}</span>
          </button>
        </div>

        <!-- 댓글 섹션 (편집 모드가 아닐 때만) -->
        <CommentSection 
          v-if="showComments && !isEditMode" 
          :feedId="article.articleId" 
          :currentUserInfo="props.currentUserInfo"
          :currentUserIdentifier="props.currentUserIdentifier"
          :articleAuthorIdentifier="article.authorIdentifier"
          @update-comment-count="updateCommentCount"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { apiFetch } from '../utils/apiClient'
import { useRouter } from 'vue-router'
import CommentSection from './CommentSection.vue'

const router = useRouter()

const props = defineProps({
  article: {
    type: Object,
    required: true
  },
  currentUserInfo: {
    type: Object,
    required: true
  },
  currentUserIdentifier: {
    type: String,
    required: true
  }
})

const emit = defineEmits(['close', 'update-article', 'refresh-articles'])

const article = ref(null)
const loading = ref(false)
const error = ref('')
const showComments = ref(false)
const showMenu = ref(false)
const isEditMode = ref(false)
const editData = ref({
  text: '',
  image: null,
  imagePreview: ''
})
const originalData = ref({
  text: '',
  imageUrl: ''
})
const fileInput = ref(null)

// article prop이 변경될 때마다 업데이트
watch(() => props.article, (newArticle) => {
  if (newArticle) {
    article.value = {
      articleId: newArticle.articleId,
      authorIdentifier: newArticle.authorIdentifier,
      authorNickname: newArticle.authorNickname || newArticle.nickname || newArticle.authorIdentifier,
      authorProfileImageUrl: newArticle.authorProfileImageUrl,
      articleMediaUrl: newArticle.articleMediaUrl,
      articleText: newArticle.articleText,
      articleLikeCount: newArticle.articleLikeCount,
      articleCommentCount: newArticle.articleCommentCount,
      isLiked: newArticle.isLiked === true || newArticle.isLiked === 'true'
    }
    console.log('[ArticleDetail] Article loaded from props:', article.value)
  }
}, { immediate: true })

// 좋아요 토글
const toggleLike = async () => {
  if (!article.value) return
  
  try {
    const response = await apiFetch(`/heart/${article.value.articleId}`, {
      method: 'POST'
    })
    
    if (!response.ok) {
      console.error('Failed to toggle like')
      return
    }
    
    // 좋아요 상태 토글
    article.value.isLiked = !article.value.isLiked
    article.value.articleLikeCount += article.value.isLiked ? 1 : -1
    console.log('[ArticleDetail] Like toggled:', article.value.isLiked)
    
    // 부모 컴포넌트에 업데이트 전달
    emit('update-article', article.value)
  } catch (err) {
    console.error('[ArticleDetail] Like toggle error:', err)
  }
}

// 댓글 토글
const toggleComments = () => {
  showComments.value = !showComments.value
}

// 댓글 수 업데이트
const updateCommentCount = (count) => {
  if (article.value) {
    article.value.articleCommentCount = count
    console.log('[ArticleDetail] Comment count updated:', count)
  }
}

// 모달 닫기
const closeModal = () => {
  emit('close')
}

// 메뉴 토글
const toggleMenu = () => {
  showMenu.value = !showMenu.value
}

// 수정 모드 진입
const handleEdit = () => {
  console.log('수정 버튼 클릭')
  showMenu.value = false
  isEditMode.value = true
  
  // 원본 데이터 백업
  originalData.value = {
    text: article.value.articleText,
    imageUrl: article.value.articleMediaUrl
  }
  
  // 편집 데이터 초기화
  editData.value = {
    text: article.value.articleText,
    image: null,
    imagePreview: ''
  }
}

// 이미지 변경 핸들러
const handleImageChange = (event) => {
  const file = event.target.files?.[0]
  console.log('[ArticleDetail] handleImageChange - file selected:', file)
  
  if (file && file instanceof File) {
    console.log('[ArticleDetail] Valid file - name:', file.name, ', size:', file.size, ', type:', file.type)
    editData.value.image = file
    
    const reader = new FileReader()
    reader.onload = (e) => {
      editData.value.imagePreview = e.target?.result
      console.log('[ArticleDetail] Image preview loaded')
    }
    reader.onerror = (e) => {
      console.error('[ArticleDetail] FileReader error:', e)
    }
    reader.readAsDataURL(file)
  } else {
    console.warn('[ArticleDetail] No valid file selected')
  }
}

// 수정 취소
const handleCancelEdit = () => {
  isEditMode.value = false
  editData.value = {
    text: '',
    image: null,
    imagePreview: ''
  }
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

// 수정 완료
const handleCompleteEdit = async () => {
  if (!article.value) return
  
  const articleId = article.value.articleId
  let hasChanges = false
  let updateSuccess = true
  
  try {
    loading.value = true
    
    // 이미지 변경 확인 및 요청
    if (editData.value.image && editData.value.image instanceof File) {
      hasChanges = true
      console.log('[ArticleDetail] Updating image...')
      console.log('[ArticleDetail] Image file details:')
      console.log('  - Name:', editData.value.image.name)
      console.log('  - Size:', editData.value.image.size, 'bytes')
      console.log('  - Type:', editData.value.image.type)
      console.log('  - LastModified:', editData.value.image.lastModified)
      console.log('  - instanceof File:', editData.value.image instanceof File)
      
      const imageFormData = new FormData()
      imageFormData.append('multipartFile', editData.value.image)
      
      console.log('[ArticleDetail] FormData created - checking contents:')
      let hasFile = false
      for (let pair of imageFormData.entries()) {
        console.log('  Key:', pair[0])
        console.log('  Value:', pair[1])
        if (pair[1] instanceof File) {
          console.log('  Value is File - name:', pair[1].name, ', size:', pair[1].size, ', type:', pair[1].type)
          hasFile = true
        }
      }
      
      if (!hasFile) {
        throw new Error('FormData에 파일이 포함되지 않았습니다.')
      }
      
      console.log('[ArticleDetail] Sending image update request to:', `/article/update/media/${articleId}`)
      
      // FormData 사용 시 Content-Type을 명시하지 않아야 함 (브라우저가 자동으로 boundary 설정)
      const imageResponse = await apiFetch(`/article/update/media/${articleId}`, {
        method: 'POST',
        body: imageFormData,
        // headers를 명시하지 않음 - 브라우저가 자동으로 multipart/form-data 설정
      })
      
      console.log('[ArticleDetail] Image response status:', imageResponse.status)
      
      if (!imageResponse.ok) {
        updateSuccess = false
        const errorData = await imageResponse.json()
        console.error('[ArticleDetail] Image update failed:', errorData)
        throw new Error(errorData.message || '이미지 수정에 실패했습니다.')
      }
      
      // 이미지 업데이트 성공 시 article 업데이트
      article.value.articleMediaUrl = editData.value.imagePreview
      console.log('[ArticleDetail] Image updated successfully')
    } else if (editData.value.image) {
      console.warn('[ArticleDetail] editData.value.image exists but is not a File:', editData.value.image)
    }
    
    // 텍스트 변경 확인 및 요청
    if (editData.value.text !== originalData.value.text) {
      hasChanges = true
      console.log('[ArticleDetail] Updating text...')
      console.log('[ArticleDetail] Text data:', editData.value.text)
      
      console.log('[ArticleDetail] Sending text update request to:', `/article/update/text/${articleId}`)
      
      const textResponse = await apiFetch(`/article/update/text/${articleId}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ articleText: editData.value.text })
      })
      
      if (!textResponse.ok) {
        updateSuccess = false
        const errorData = await textResponse.json()
        throw new Error(errorData.message || '텍스트 수정에 실패했습니다.')
      }
      
      // 텍스트 업데이트 성공 시 article 업데이트
      article.value.articleText = editData.value.text
      console.log('[ArticleDetail] Text updated successfully')
    }
    
    if (!hasChanges) {
      console.log('[ArticleDetail] No changes detected')
    }
    
    if (updateSuccess && hasChanges) {
      // 성공 메시지 표시
      alert('게시물을 수정했습니다.')
      
      // 부모 컴포넌트에 업데이트 전달
      emit('update-article', article.value)
      
      // 게시물 목록 새로고침 요청
      emit('refresh-articles')
      
      // 팝업 닫기
      emit('close')
    }
    
    // 편집 모드 종료
    isEditMode.value = false
    editData.value = {
      text: '',
      image: null,
      imagePreview: ''
    }
    if (fileInput.value) {
      fileInput.value.value = ''
    }
    
  } catch (err) {
    console.error('[ArticleDetail] Update error:', err)
    error.value = err.message || '게시물 수정에 실패했습니다.'
    setTimeout(() => {
      error.value = ''
    }, 3000)
  } finally {
    loading.value = false
  }
}

// 삭제 핸들러
const handleDelete = async () => {
  console.log('삭제 버튼 클릭')
  showMenu.value = false
  
  if (!article.value) return
  
  // 삭제 확인
  if (!confirm('정말 이 게시물을 삭제하시겠습니까?')) {
    return
  }
  
  const articleId = article.value.articleId
  
  try {
    loading.value = true
    console.log('[ArticleDetail] Deleting article:', articleId)
    
    const response = await apiFetch(`/article/${articleId}`, {
      method: 'DELETE'
    })
    
    if (!response.ok) {
      const errorData = await response.json()
      throw new Error(errorData.message || '게시물 삭제에 실패했습니다.')
    }
    
    console.log('[ArticleDetail] Article deleted successfully')
    
    // 삭제 성공 메시지 표시
    alert('게시물 삭제에 성공했습니다.')
    
    // 게시물 목록 새로고침 요청
    emit('refresh-articles')
    
    // 팝업 닫기
    emit('close')
    
  } catch (err) {
    console.error('[ArticleDetail] Delete error:', err)
    alert(err.message || '게시물 삭제에 실패했습니다.')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  // ESC 키로 모달 닫기
  const handleEscape = (e) => {
    if (e.key === 'Escape') {
      closeModal()
    }
  }
  
  // 외부 클릭으로 메뉴 닫기
  const handleClickOutside = (e) => {
    if (showMenu.value && !e.target.closest('.menu-btn') && !e.target.closest('.menu-dropdown')) {
      showMenu.value = false
    }
  }
  
  window.addEventListener('keydown', handleEscape)
  window.addEventListener('click', handleClickOutside)
  
  return () => {
    window.removeEventListener('keydown', handleEscape)
    window.removeEventListener('click', handleClickOutside)
  }
})
</script>

<style scoped>
/* 모달 오버레이 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
  backdrop-filter: blur(2px);
}

/* 닫기 버튼 (팝업 밖) */
.close-btn {
  position: fixed;
  top: 20px;
  right: 20px;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  color: #333;
  border: none;
  font-size: 1.5em;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  z-index: 1001;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.close-btn:hover {
  background: white;
  transform: scale(1.1);
}

/* 모달 콘텐츠 */
.modal-content {
  background: white;
  border-radius: 12px;
  max-width: 600px;
  max-height: 90vh;
  width: 100%;
  position: relative;
  overflow-y: auto;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}

.article-container {
  padding-top: 16px;
}

/* 상단: 프로필 */
.article-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid #eee;
  position: relative;
}

.profile-image {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
}

.author-nickname {
  font-weight: 600;
  font-size: 1.1em;
  flex: 1;
}

/* 메뉴 버튼 */
.menu-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: transparent;
  border: none;
  font-size: 1.5em;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
  color: #666;
}

.menu-btn:hover {
  background: #f5f5f5;
}

/* 메뉴 드롭다운 */
.menu-dropdown {
  position: absolute;
  top: 60px;
  right: 16px;
  background: white;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  z-index: 20;
  min-width: 120px;
}

.menu-item {
  width: 100%;
  padding: 12px 16px;
  background: white;
  border: none;
  text-align: left;
  cursor: pointer;
  font-size: 0.95em;
  transition: background 0.2s;
  color: #333;
}

.menu-item:hover {
  background: #f5f5f5;
}

.menu-item.delete {
  color: #d32f2f;
}

.menu-item.delete:hover {
  background: #ffebee;
}

/* 편집 모드 액션 버튼 */
.edit-action-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  font-size: 0.9em;
  transition: all 0.2s;
}

.edit-action-btn.cancel {
  background: #f0f0f0;
  color: #333;
}

.edit-action-btn.cancel:hover {
  background: #e0e0e0;
}

.edit-action-btn.complete {
  background: #007aff;
  color: white;
  margin-left: 8px;
}

.edit-action-btn.complete:hover {
  background: #0056d4;
}

/* 중단: 이미지 */
.article-image-section {
  width: 100%;
  max-height: 600px;
  overflow: hidden;
  background: #000;
  position: relative;
}

.image-edit-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 5;
  transition: background 0.2s;
}

.image-edit-overlay:hover {
  background: rgba(0, 0, 0, 0.7);
}

.edit-icon {
  color: white;
  font-size: 1.2em;
  font-weight: 600;
  text-align: center;
}

.article-image {
  width: 100%;
  height: auto;
  display: block;
  object-fit: contain;
}

/* 이미지 아래: 텍스트 */
.article-content {
  padding: 16px;
  line-height: 1.5;
}

.content-nickname {
  font-weight: 600;
  margin-right: 8px;
}

.content-text {
  color: #333;
}

.content-text-edit {
  width: 100%;
  min-height: 100px;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-family: inherit;
  font-size: 1em;
  resize: vertical;
  outline: none;
  transition: border 0.2s;
  box-sizing: border-box;
  margin-top: 8px;
}

.content-text-edit:focus {
  border-color: #007aff;
  box-shadow: 0 0 0 3px rgba(0, 122, 255, 0.1);
}

/* 하단: 액션 버튼 */
.article-actions {
  display: flex;
  gap: 16px;
  padding: 12px 16px;
  border-top: 1px solid #eee;
  border-bottom: 1px solid #eee;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: transparent;
  border: none;
  cursor: pointer;
  font-size: 1.1em;
  transition: background 0.2s;
  border-radius: 6px;
}

.action-btn:hover {
  background: #f5f5f5;
}

.action-btn .count {
  font-size: 0.9em;
  color: #666;
}

.like-btn {
  color: #ff4081;
}

.comment-btn {
  color: #333;
}

.loading-message,
.error-message {
  text-align: center;
  padding: 40px 20px;
  font-size: 1.1em;
}

.loading-message {
  color: #666;
}

.error-message {
  color: #d32f2f;
}

/* 반응형 */
@media (max-width: 768px) {
  .modal-overlay {
    padding: 0;
  }

  .modal-content {
    max-width: 100%;
    max-height: 100vh;
    border-radius: 0;
  }

  .close-btn {
    top: 16px;
    right: 16px;
    width: 36px;
    height: 36px;
    font-size: 1.3em;
  }
}
</style>
