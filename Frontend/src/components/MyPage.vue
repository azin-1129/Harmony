<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { apiFetch } from '../utils/apiClient'
import { useRouter, useRoute } from 'vue-router'
import ArticleDetail from './ArticleDetail.vue'

const router = useRouter()
const route = useRoute()

const DEFAULT_PROFILE_IMAGE = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgdmlld0JveD0iMCAwIDEwMCAxMDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxyZWN0IHdpZHRoPSIxMDAiIGhlaWdodD0iMTAwIiBmaWxsPSIjRTBFMEUwIi8+CjxjaXJjbGUgY3g9IjUwIiBjeT0iMzUiIHI9IjE1IiBmaWxsPSIjOTk5Ii8+CjxwYXRoIGQ9Ik0yMCA2NSBDMjAgNjAgMzAgNTAgNTAgNTAgQzcwIDUwIDgwIDYwIDgwIDY1IiBmaWxsPSIjOTk5Ii8+Cjwvc3ZnPg=='

// 내 프로필 정보
const myProfile = ref({
  userId: null,
  nickname: '',
  userIdentifier: '',
  profileImage: '',
  friendCount: 42 // 더미 데이터
})

// 내 게시물 데이터
const myArticles = ref([])
const loading = ref(false)
const error = ref('')
const likedArticleIds = ref([])

// 탭 상태: 'post' or 'reels'
const activeTab = ref('post')

// 선택된 게시물 (상세 팝업용)
const selectedArticle = ref(null)

const profileImageSrc = computed(() => {
  const imageUrl = myProfile.value.profileImage
  if (!imageUrl || (typeof imageUrl === 'string' && !imageUrl.trim())) {
    return DEFAULT_PROFILE_IMAGE
  }
  return imageUrl
})

const handleProfileImageError = (event) => {
  event.target.src = DEFAULT_PROFILE_IMAGE
}

// 좋아요 목록 조회
const fetchLikedArticles = async () => {
  try {
    const res = await apiFetch('/heart', { method: 'GET' })
    if (!res || !res.ok) {
      console.error('[MyPage] Failed to load liked articles', res?.status)
      return
    }
    const body = await res.json()
    const likedList = Array.isArray(body.data) ? body.data : []
    likedArticleIds.value = likedList.map(item => item.articleId)
    console.log('[MyPage] Liked articles loaded:', likedArticleIds.value)
  } catch (err) {
    console.error('[MyPage] Error fetching liked articles', err)
  }
}

// 현재 로그인한 사용자 정보 조회
const fetchMyProfile = async () => {
  loading.value = true
  try {
    const response = await apiFetch('/user/info')
    console.log('[MyPage] User info response status:', response.status)
    if (!response.ok) throw new Error('프로필 조회 실패')
    
    const result = await response.json()
    console.log('[MyPage] User info result:', result)
    
    if (result.data) {
      const userData = result.data
      console.log('[MyPage] Extracted userId:', userData.userId)
      
      myProfile.value = {
        userId: userData.userId,
        nickname: userData.nickname,
        userIdentifier: userData.userIdentifier,
        profileImage: userData.profileImageUrl || '',
        friendCount: 42 // 더미 데이터
      }
      
      console.log('[MyPage] myProfile.value.userId:', myProfile.value.userId)
      console.log('[MyPage] Now fetching articles for userId:', userData.userId)
      
      // userId가 있으면 게시물 조회
      if (userData.userId) {
        await fetchMyArticles(userData.userId)
      } else {
        console.error('[MyPage] userId is undefined or null')
        error.value = 'userId를 가져올 수 없습니다.'
      }
    }
  } catch (err) {
    console.error('[MyPage] Profile fetch error:', err)
    error.value = '프로필을 불러올 수 없습니다.'
  } finally {
    loading.value = false
  }
}

// 내 게시물 목록 조회
const fetchMyArticles = async (userId) => {
  console.log('[MyPage] fetchMyArticles called with userId:', userId, typeof userId)
  
  if (!userId) {
    console.error('[MyPage] userId is empty or null')
    error.value = '사용자 정보를 불러올 수 없습니다.'
    return
  }
  
  loading.value = true
  error.value = ''
  try {
    const url = `/article/${userId}`
    console.log('[MyPage] Fetching articles from URL:', url)
    
    const response = await apiFetch(url)
    console.log('[MyPage] Articles response status:', response.status)
    
    if (!response.ok) {
      const errorText = await response.text()
      console.error('[MyPage] Error response:', errorText)
      throw new Error('게시물 조회 실패')
    }
    
    const result = await response.json()
    console.log('[MyPage] Articles response:', result)
    
    const articles = result.data || []
    // 좋아요 여부 및 nickname 추가
    myArticles.value = articles.map(article => ({
      ...article,
      isLiked: likedArticleIds.value.includes(article.articleId),
      articleCommentCount: article.articleCommentCount || 0,
      authorNickname: article.nickname || article.authorNickname || myProfile.value.nickname,
      nickname: article.nickname || article.authorNickname || myProfile.value.nickname
    }))
    console.log('[MyPage] Articles loaded successfully, count:', myArticles.value.length)
    
    // 각 게시물의 댓글 개수 조회
    await fetchCommentCountsForArticles()
  } catch (err) {
    console.error('[MyPage] Articles fetch error:', err)
    error.value = '게시물을 불러올 수 없습니다.'
  } finally {
    loading.value = false
  }
}

// 모든 게시물의 댓글 개수 조회
const fetchCommentCountsForArticles = async () => {
  for (const article of myArticles.value) {
    try {
      const res = await apiFetch(`/comment/${article.articleId}`, { method: 'GET' })
      if (!res || !res.ok) {
        console.error('[MyPage] Failed to load comment count for article:', article.articleId)
        continue
      }
      const body = await res.json()
      const count = Array.isArray(body.data) ? body.data.length : 0
      article.articleCommentCount = count
    } catch (err) {
      console.error('[MyPage] Error fetching comment count for article:', article.articleId, err)
    }
  }
  console.log('[MyPage] Comment counts updated for all articles')
}

// 게시물 클릭 시 팝업으로 표시
const viewArticleDetail = (articleId) => {
  const article = myArticles.value.find(a => a.articleId === articleId)
  if (article) {
    selectedArticle.value = article
  }
}

// 팝업 닫기
const closeArticleDetail = () => {
  selectedArticle.value = null
}

// 게시물 업데이트 핸들러
const handleArticleUpdate = (updatedArticle) => {
  // 팝업의 selectedArticle 업데이트
  selectedArticle.value = updatedArticle
  
  // myArticles 배열에서 해당 게시물도 업데이트
  const index = myArticles.value.findIndex(a => a.articleId === updatedArticle.articleId)
  if (index !== -1) {
    myArticles.value[index] = { ...myArticles.value[index], ...updatedArticle }
    console.log('[MyPage] Article updated in list:', updatedArticle.articleId)
  }
}

// 게시물 목록 새로고침
const refreshMyArticles = async () => {
  console.log('[MyPage] Refreshing articles after edit...')
  if (myProfile.value.userId) {
    await fetchLikedArticles()
    await fetchMyArticles(myProfile.value.userId)
  }
}

onMounted(async () => {
  // 좋아요 목록을 먼저 조회
  await fetchLikedArticles()
  await fetchMyProfile()
})

// 라우트 변경 감지 - 페이지 재진입 시 데이터 새로고침
watch(() => route.path, async (newPath, oldPath) => {
  if (newPath === '/mypage' && oldPath !== '/mypage') {
    console.log('[MyPage] Route re-entered, refreshing data...')
    await fetchLikedArticles()
    if (myProfile.value.userId) {
      await fetchMyArticles(myProfile.value.userId)
    }
  }
})
</script>

<template>
  <div class="my-page">
    <!-- 프로필 헤더 -->
    <div class="profile-header">
      <img 
        :src="profileImageSrc"
        alt="프로필" 
        class="profile-img" 
        @error="handleProfileImageError"
      />
      <div class="profile-info">
        <h2>{{ myProfile.nickname }}</h2>
        <p class="user-identifier">@{{ myProfile.userIdentifier }}</p>
        <div class="friend-info">
          친구 {{ myProfile.friendCount }}명
        </div>
      </div>
    </div>

    <!-- 게시물/릴스 탭 -->
    <div class="tab-buttons">
      <button
        :class="{ active: activeTab === 'post' }"
        @click="activeTab = 'post'"
      >
        게시물
      </button>
      <button
        :class="{ active: activeTab === 'reels' }"
        @click="activeTab = 'reels'"
      >
        릴스
      </button>
    </div>

    <!-- 로딩 상태 -->
    <div v-if="loading" class="loading-message">
      로딩 중...
    </div>

    <!-- 에러 메시지 -->
    <div v-else-if="error" class="error-message">
      {{ error }}
    </div>

    <!-- 게시물 탭 -->
    <template v-else-if="activeTab === 'post'">
      <!-- 게시물이 없을 때 -->
      <div v-if="myArticles.length === 0" class="empty-message">
        게시물이 없습니다.
      </div>
      
      <!-- 게시물 그리드 -->
      <div v-else class="feed-grid">
        <div 
          v-for="article in myArticles" 
          :key="article.articleId" 
          class="feed-item"
          @click="viewArticleDetail(article.articleId)"
        >
          <img 
            :src="article.articleThumbnailUrl || article.articleMediaUrl" 
            :alt="article.articleText" 
            class="article-thumbnail"
          />
          <div class="article-overlay">
            <span>❤️ {{ article.articleLikeCount }}</span>
            <span>💬 {{ article.articleCommentCount }}</span>
          </div>
        </div>
      </div>
    </template>

    <!-- 릴스 탭 -->
    <div v-else class="empty-message">
      릴스가 없습니다.
    </div>

    <!-- 게시물 상세 팝업 -->
    <ArticleDetail 
      v-if="selectedArticle"
      :article="selectedArticle"
      :currentUserInfo="myProfile"
      :currentUserIdentifier="myProfile.userIdentifier"
      @close="closeArticleDetail"
      @update-article="handleArticleUpdate"
      @refresh-articles="refreshMyArticles"
    />
  </div>
</template>

<style scoped>
.my-page {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.profile-img {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #eee;
}

.profile-info {
  flex: 1;
}

.profile-info h2 {
  margin: 0 0 5px 0;
  font-size: 1.5em;
}

.user-identifier {
  margin: 0 0 8px 0;
  color: #666;
  font-size: 0.95em;
}

.friend-info {
  font-size: 0.95rem;
  color: #666;
  margin-top: 8px;
}

/* 탭 버튼 */
.tab-buttons {
  display: flex;
  gap: 0;
  margin-bottom: 30px;
  border-bottom: 1px solid #eee;
}

.tab-buttons button {
  flex: 1;
  padding: 12px;
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 1em;
  font-weight: 500;
  color: #666;
  position: relative;
  transition: color 0.2s;
}

.tab-buttons button.active {
  color: #007aff;
}

.tab-buttons button.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  right: 0;
  height: 2px;
  background: #007aff;
}

/* 피드 그리드 */
.feed-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.feed-item {
  position: relative;
  cursor: pointer;
  overflow: hidden;
  border-radius: 8px;
  aspect-ratio: 1;
}

.article-thumbnail {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.2s;
}

.feed-item:hover .article-thumbnail {
  transform: scale(1.05);
}

.article-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  opacity: 0;
  transition: opacity 0.2s;
  color: white;
  font-weight: 600;
}

.feed-item:hover .article-overlay {
  opacity: 1;
}

/* 메시지 */
.loading-message,
.error-message,
.empty-message {
  text-align: center;
  padding: 40px 20px;
  color: #666;
  font-size: 1.1em;
}

.error-message {
  color: #d32f2f;
}

/* 반응형 */
@media (max-width: 768px) {
  .feed-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
  }
  
  .profile-header {
    gap: 15px;
  }
  
  .profile-img {
    width: 80px;
    height: 80px;
  }
  
  .profile-info h2 {
    font-size: 1.2em;
  }
}
</style>
