<template>
  <div class="comments">
    <!-- 댓글 목록 -->
    <div v-if="loading" class="loading">댓글 로딩 중...</div>
    <div v-else class="comments-list">
      <div v-for="c in comments" :key="c.commentId" class="comment">
        <img :src="c.writerProfileImageUrl" :alt="c.nickname" class="comment-profile" />
        <div class="comment-content">
          <div class="comment-header">
            <span class="nickname">{{ c.nickname }}</span>
            <span class="timestamp">{{ formatTime(c.createdAt) }}</span>
          </div>
          <p class="comment-text">{{ c.commentText }}</p>
        </div>
      </div>
    </div>

    <!-- 댓글 작성 폼 -->
    <form @submit.prevent="addComment" class="comment-form">
      <input v-model="newComment" placeholder="댓글 추가..." class="comment-input" />
      <button type="submit" :disabled="isSubmitting">게시</button>
    </form>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { apiFetch } from '@/utils/apiClient'

const props = defineProps({
  feedId: Number,
  currentUserInfo: Object,
  currentUserIdentifier: String
})

const emit = defineEmits(['update-comment-count'])

const comments = ref([])
const newComment = ref('')
const loading = ref(false)
const isSubmitting = ref(false)

// createdAt을 보기 좋은 시간 형식으로 변환
const formatTime = (createdAtString) => {
  try {
    const date = new Date(createdAtString)
    const now = new Date()
    const diffMs = now - date
    const diffSecs = Math.floor(diffMs / 1000)
    const diffMins = Math.floor(diffSecs / 60)
    const diffHours = Math.floor(diffMins / 60)
    const diffDays = Math.floor(diffHours / 24)

    if (diffSecs < 60) {
      return '방금 전'
    } else if (diffMins < 60) {
      return `${diffMins}분 전`
    } else if (diffHours < 24) {
      return `${diffHours}시간 전`
    } else if (diffDays < 7) {
      return `${diffDays}일 전`
    } else {
      // 날짜 포맷팅
      return date.toLocaleDateString('ko-KR', {
        month: 'short',
        day: 'numeric'
      })
    }
  } catch (err) {
    console.error('Error formatting time', err)
    return ''
  }
}

// 댓글 목록 조회
const fetchComments = async () => {
  try {
    loading.value = true
    const res = await apiFetch(`/comment/${props.feedId}`, { method: 'GET' })
    if (!res || !res.ok) {
      console.error('Failed to load comments', res?.status)
      return
    }
    const body = await res.json()
    comments.value = Array.isArray(body.data) ? body.data : []
    console.log('Comments loaded:', comments.value)
    
    // 댓글 카운트 갱신
    emit('update-comment-count', comments.value.length)
  } catch (err) {
    console.error('Error fetching comments', err)
  } finally {
    loading.value = false
  }
}

// 댓글 추가
const addComment = async () => {
  if (!newComment.value.trim()) return

  try {
    isSubmitting.value = true
    const res = await apiFetch('/comment', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        articleId: props.feedId,
        commentText: newComment.value
      })
    })

    if (!res || !res.ok) {
      console.error('Failed to add comment', res?.status)
      alert('댓글 작성에 실패했습니다.')
      return
    }

    const body = await res.json()
    const newCommentData = body.data

    // 현재 사용자 정보를 이용해 댓글 추가
    if (props.currentUserInfo) {
      comments.value.push({
        commentId: Date.now(), // 서버에서 반환하지 않으면 임시 ID 사용
        writerIdentifier: props.currentUserIdentifier || '',
        writerProfileImageUrl: props.currentUserInfo.profileImageUrl || '',
        nickname: props.currentUserInfo.nickname || 'Unknown',
        commentText: newCommentData.commentText,
        createdAt: newCommentData.createdAt
      })
    }

    newComment.value = ''
    
    // 댓글 카운트 증가
    emit('update-comment-count', comments.value.length)
    console.log('Comment added successfully')
  } catch (err) {
    console.error('Error adding comment', err)
    alert('댓글 작성 중 오류가 발생했습니다.')
  } finally {
    isSubmitting.value = false
  }
}

// 컴포넌트 마운트 시 댓글 목록 로드
onMounted(async () => {
  await fetchComments()
})
</script>

<style scoped>
.comments {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #eee;
}

.loading {
  text-align: center;
  padding: 10px;
  color: #999;
  font-size: 0.9em;
}

.comments-list {
  margin-bottom: 10px;
  max-height: 400px;
  overflow-y: auto;
}

.comment {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.comment-profile {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.comment-content {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.nickname {
  font-weight: bold;
  font-size: 0.9em;
}

.timestamp {
  font-size: 0.8em;
  color: #999;
}

.comment-text {
  margin: 0;
  font-size: 0.9em;
  color: #333;
  word-break: break-word;
}

.comment-form {
  display: flex;
  gap: 6px;
}

.comment-input {
  flex: 1;
  padding: 8px 10px;
  border-radius: 6px;
  border: 1px solid #ccc;
  font-size: 0.9em;
}

.comment-input:focus {
  outline: none;
  border-color: #B794F6;
  box-shadow: 0 0 0 2px rgba(183, 148, 246, 0.1);
}

.comment-form button {
  padding: 8px 12px;
  border: none;
  background-color: #B794F6;
  color: white;
  border-radius: 6px;
  font-size: 0.9em;
  cursor: pointer;
  transition: background 0.2s;
}

.comment-form button:hover:not(:disabled) {
  background-color: #9d7ae0;
}

.comment-form button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}
</style>