<template>
  <!-- 모달 오버레이 -->
  <div class="modal-overlay" @click="closeModal">
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
        <!-- 닫기 버튼 -->
        <button class="close-btn" @click="closeModal">✕</button>

        <!-- 상단: 프로필 사진, 닉네임 -->
        <div class="article-header">
          <img 
            :src="article.authorProfileImageUrl || 'https://via.placeholder.com/50'" 
            alt="프로필" 
            class="profile-image"
          />
          <span class="author-nickname">{{ article.authorNickname }}</span>
        </div>

        <!-- 중단: 이미지 -->
        <div class="article-image-section">
          <img 
            :src="article.articleMediaUrl" 
            :alt="article.articleText" 
            class="article-image"
          />
        </div>

        <!-- 이미지 아래: 닉네임과 텍스트 -->
        <div class="article-content">
          <span class="content-nickname">{{ article.authorNickname }}</span>
          <span class="content-text">{{ article.articleText }}</span>
        </div>

        <!-- 하단: 좋아요, 댓글 버튼 -->
        <div class="article-actions">
          <button class="action-btn like-btn" @click="toggleLike">
            <span v-if="article.isLiked">❤️</span>
            <span v-else>🤍</span>
            <span class="count">{{ article.articleLikeCount }}</span>
          </button>
          <button class="action-btn comment-btn" @click="toggleComments">
            💬 <span class="count">{{ article.articleCommentCount }}</span>
          </button>
        </div>

        <!-- 댓글 섹션 -->
        <CommentSection 
          v-if="showComments" 
          :feedId="article.articleId" 
          :currentUserInfo="currentUserInfo"
          :currentUserIdentifier="props.currentUserIdentifier"
          @update-comment-count="updateCommentCount"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { apiFetch } from '../utils/apiClient'
import CommentSection from './CommentSection.vue'

const props = defineProps({
  article: {
    type: Object,
    required: true
  },
  currentUserIdentifier: {
    type: String,
    required: true
  }
})

const emit = defineEmits(['close', 'update-article'])

const article = ref(null)
const loading = ref(false)
const error = ref('')
const currentUserInfo = ref(null)
const showComments = ref(false)

// 댓글 개수 조회 (watch에서 사용하므로 먼저 정의)
const fetchCommentCount = async (articleId) => {
  if (!articleId) return
  
  try {
    const res = await apiFetch(`/comment/${articleId}`, { method: 'GET' })
    if (!res || !res.ok) {
      console.error('[ArticleDetail] Failed to load comment count', res?.status)
      return
    }
    const body = await res.json()
    const count = Array.isArray(body.data) ? body.data.length : 0
    if (article.value) {
      article.value.articleCommentCount = count
    }
    console.log('[ArticleDetail] Comment count loaded:', count)
  } catch (err) {
    console.error('[ArticleDetail] Error fetching comment count', err)
  }
}

// article prop이 변경될 때마다 업데이트
watch(() => props.article, (newArticle) => {
  if (newArticle) {
    article.value = {
      articleId: newArticle.articleId,
      authorIdentifier: newArticle.authorIdentifier,
      authorNickname: newArticle.authorNickname || newArticle.authorIdentifier,
      authorProfileImageUrl: newArticle.authorProfileImageUrl,
      articleMediaUrl: newArticle.articleMediaUrl,
      articleText: newArticle.articleText,
      articleLikeCount: newArticle.articleLikeCount,
      articleCommentCount: newArticle.articleCommentCount,
      isLiked: newArticle.isLiked === true || newArticle.isLiked === 'true'
    }
    console.log('[ArticleDetail] Article loaded from props:', article.value)
    fetchCommentCount(newArticle.articleId)
  }
}, { immediate: true })

// 현재 사용자 정보 조회
const fetchCurrentUserInfo = async () => {
  try {
    const response = await apiFetch('/user/info')
    if (!response.ok) return
    
    const result = await response.json()
    if (result.data) {
      currentUserInfo.value = result.data
    }
  } catch (err) {
    console.error('[ArticleDetail] User info fetch error:', err)
  }
}

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

onMounted(async () => {
  await fetchCurrentUserInfo()
  
  // ESC 키로 모달 닫기
  const handleEscape = (e) => {
    if (e.key === 'Escape') {
      closeModal()
    }
  }
  window.addEventListener('keydown', handleEscape)
  
  return () => {
    window.removeEventListener('keydown', handleEscape)
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

/* 닫기 버튼 */
.close-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  border: none;
  font-size: 1.5em;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
  z-index: 10;
}

.close-btn:hover {
  background: rgba(0, 0, 0, 0.8);
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
}

/* 중단: 이미지 */
.article-image-section {
  width: 100%;
  max-height: 600px;
  overflow: hidden;
  background: #000;
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
