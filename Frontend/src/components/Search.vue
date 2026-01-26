<template>
  <div class="search-page">
    <!-- 검색 입력 -->
    <div class="search-header">
      <h1>검색</h1>
      <input
        v-model="searchQuery"
        type="text"
        placeholder="닉네임 또는 아이디로 사용자 검색..."
        class="search-input"
        @input="handleSearch"
      />
    </div>

    <!-- 사용자 프로필 상세 뷰 -->
    <div v-if="selectedUser" class="user-profile-view">
      <!-- 뒤로가기 버튼 -->
      <button class="back-button" @click="closeUserProfile">
        ← 검색 결과로 돌아가기
      </button>

      <!-- 프로필 헤더 -->
      <div class="profile-header">
        <img 
          :src="selectedUser.profileImageUrl" 
          alt="프로필" 
          class="profile-img" 
          @error="handleImageError"
        />
        <div class="profile-info">
          <h2>{{ selectedUser.nickname }}</h2>
          <p class="user-identifier">@{{ selectedUser.userIdentifier }}</p>
          <div class="friend-info">
            친구 {{ selectedUser.friendCount || 0 }}명
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
      <div v-if="loadingUserArticles" class="loading-message">
        로딩 중...
      </div>

      <!-- 에러 메시지 -->
      <div v-else-if="userArticlesError" class="error-message">
        {{ userArticlesError }}
      </div>

      <!-- 게시물 탭 -->
      <template v-else-if="activeTab === 'post'">
        <!-- 게시물이 없을 때 -->
        <div v-if="userArticles.length === 0" class="empty-message">
          작성한 게시물이 없습니다.
        </div>
        
        <!-- 게시물 그리드 -->
        <div v-else class="feed-grid">
          <div 
            v-for="article in userArticles" 
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
        :currentUserInfo="currentLoginUser"
        :currentUserIdentifier="currentLoginUser.userIdentifier"
        @close="closeArticleDetail"
        @update-article="handleArticleUpdate"
        @refresh-articles="refreshUserArticles"
      />
    </div>

    <!-- 검색 결과 목록 -->
    <div v-else-if="searchQuery" class="search-results">
      <p v-if="loading" class="loading">검색 중...</p>
      <p v-else-if="!searchResults.length" class="no-results">검색 결과가 없습니다.</p>
      <div v-else class="results-list">
        <div 
          v-for="user in searchResults" 
          :key="user.userIdentifier" 
          class="user-result-item"
          @click="viewUserProfile(user)"
        >
          <img 
            :src="user.profileImageUrl" 
            alt="프로필" 
            class="user-avatar" 
            @error="handleImageError"
          />
          <div class="user-info">
            <p class="user-nickname">{{ user.nickname }}</p>
            <p class="user-id">@{{ user.userIdentifier }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { apiFetch } from '../utils/apiClient'
import ArticleDetail from './ArticleDetail.vue'

const route = useRoute()

// 기본 프로필 사진 - 회색 배경에 사람 아이콘
const DEFAULT_PROFILE_IMAGE = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgdmlld0JveD0iMCAwIDEwMCAxMDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxyZWN0IHdpZHRoPSIxMDAiIGhlaWdodD0iMTAwIiBmaWxsPSIjRTBFMEUwIi8+CjxjaXJjbGUgY3g9IjUwIiBjeT0iMzUiIHI9IjE1IiBmaWxsPSIjOTk5Ii8+CjxwYXRoIGQ9Ik0yMCA2NSBDMjAgNjAgMzAgNTAgNTAgNTAgQzcwIDUwIDgwIDYwIDgwIDY1IiBmaWxsPSIjOTk5Ii8+Cjwvc3ZnPg=='

const searchQuery = ref('')
const searchResults = ref([])
const loading = ref(false)

// 선택된 사용자 프로필
const selectedUser = ref(null)
const userArticles = ref([])
const loadingUserArticles = ref(false)
const userArticlesError = ref('')
const activeTab = ref('post')
const selectedArticle = ref(null)
const likedArticleIds = ref([])

// 현재 로그인한 사용자 정보
const currentLoginUser = ref({
  userId: null,
  nickname: '',
  userIdentifier: '',
  profileImage: ''
})

// 프로필 이미지 URL 정규화 - 없으면 기본 이미지 사용
const getProfileImageUrl = (imageUrl) => {
  if (!imageUrl || imageUrl === null || (typeof imageUrl === 'string' && !imageUrl.trim())) {
    return DEFAULT_PROFILE_IMAGE
  }
  return imageUrl
}

// 이미지 로드 실패 시 기본 이미지로 대체
const handleImageError = (event) => {
  event.target.src = DEFAULT_PROFILE_IMAGE
}

// 현재 로그인한 사용자 정보 조회
const fetchCurrentUserInfo = async () => {
  try {
    const response = await apiFetch('/user/info', { method: 'GET' })
    if (!response.ok) {
      console.error('[Search] Failed to fetch current user info:', response.status)
      return
    }
    
    const result = await response.json()
    const userData = result.data
    
    currentLoginUser.value = {
      userId: userData.userId,
      nickname: userData.nickname,
      userIdentifier: userData.userIdentifier,
      profileImage: userData.profileImageUrl || ''
    }
    
    console.log('[Search] Current user info loaded:', currentLoginUser.value)
  } catch (err) {
    console.error('[Search] Error fetching current user info:', err)
  }
}

// 사용자 검색
const handleSearch = async () => {
  if (!searchQuery.value.trim()) {
    searchResults.value = []
    return
  }

  loading.value = true
  try {
    // POST 방식으로 검색 요청
    const response = await apiFetch('/user/search', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        nicknameOrUserIdentifier: searchQuery.value
      })
    })
    
    if (!response.ok) {
      console.error('[Search] Search failed:', response.status)
      searchResults.value = []
      return
    }

    const result = await response.json()
    const dataList = result.data || []
    
    // data가 빈 배열이면 검색 결과 없음
    if (dataList.length === 0) {
      searchResults.value = []
      console.log('[Search] No search results found')
    } else {
      searchResults.value = dataList.map(user => ({
        ...user,
        profileImageUrl: getProfileImageUrl(user.profileImageUrl)
      }))
      console.log('[Search] Search results:', searchResults.value)
    }
  } catch (err) {
    console.error('[Search] Search error:', err)
    searchResults.value = []
  } finally {
    loading.value = false
  }
}

// 사용자 프로필 보기
const viewUserProfile = async (user) => {
  try {
    // 검색 결과의 userId를 사용하여 프로필 정보 조회 (GET)
    const response = await apiFetch(`/user/profile/${user.userId}`, {
      method: 'GET'
    })
    
    if (!response.ok) {
      console.error('[Search] Failed to fetch user profile:', response.status)
      return
    }
    
    const result = await response.json()
    const userData = result.data
    
    // 프로필 정보 설정 - 검색 결과의 userId를 사용
    selectedUser.value = {
      userId: user.userId, // 검색 결과의 userId 사용
      nickname: userData.nickname || user.nickname,
      userIdentifier: userData.userIdentifier || user.userIdentifier,
      profileImageUrl: getProfileImageUrl(userData.profileImageUrl || user.profileImageUrl),
      friendCount: 0
    }
    
    activeTab.value = 'post'
    await fetchLikedArticles()
    // 검색 결과의 userId를 사용하여 게시물 조회
    await fetchUserArticles(user.userId)
  } catch (err) {
    console.error('[Search] Error fetching user profile:', err)
  }
}

// 사용자 프로필 닫기
const closeUserProfile = () => {
  selectedUser.value = null
  selectedArticle.value = null
  userArticles.value = []
  userArticlesError.value = ''
}

// 좋아요 목록 조회
const fetchLikedArticles = async () => {
  try {
    const res = await apiFetch('/heart', { method: 'GET' })
    if (!res || !res.ok) {
      console.error('[Search] Failed to load liked articles', res?.status)
      return
    }
    const body = await res.json()
    const likedList = Array.isArray(body.data) ? body.data : []
    likedArticleIds.value = likedList.map(item => item.articleId)
  } catch (err) {
    console.error('[Search] Error fetching liked articles', err)
  }
}

// 사용자 게시물 목록 조회
const fetchUserArticles = async (userId) => {
  console.log('[Search] fetchUserArticles called with userId:', userId)
  
  if (!userId) {
    console.error('[Search] userId is empty or null')
    userArticles.value = []
    return
  }
  
  loadingUserArticles.value = true
  userArticlesError.value = ''
  try {
    const url = `/article/${userId}`
    console.log('[Search] Fetching articles from URL:', url)
    
    const response = await apiFetch(url, { method: 'GET' })
    console.log('[Search] Response status:', response.status)
    
    if (!response.ok) {
      console.error('[Search] Failed to fetch articles:', response.status)
      throw new Error('게시물 조회 실패')
    }
    
    const result = await response.json()
    console.log('[Search] Articles response:', result)
    
    const articles = result.data || []
    console.log('[Search] Articles count:', articles.length)
    
    // API 응답 필드명에 맞춰 매핑
    userArticles.value = articles.map(article => ({
      articleId: article.articleId,
      articleMediaUrl: article.articleMediaUrl,
      articleThumbnailUrl: article.articleThumbnailUrl,
      articleText: article.articleText,
      articleLikeCount: article.articleLikeCount || 0,
      articleCommentCount: article.articleCommentCount || 0,
      authorIdentifier: article.authorIdentifier,
      authorProfileImageUrl: getProfileImageUrl(article.authorProfileImageUrl),
      createdAt: article.createdAt,
      isLiked: likedArticleIds.value.includes(article.articleId),
      nickname: selectedUser.value?.nickname || article.authorIdentifier
    }))
    
    console.log('[Search] Articles loaded:', userArticles.value.length)
  } catch (err) {
    console.error('[Search] Articles fetch error:', err)
    userArticlesError.value = '게시물을 불러올 수 없습니다.'
  } finally {
    loadingUserArticles.value = false
  }
}

// 게시물 클릭 시 팝업으로 표시
const viewArticleDetail = (articleId) => {
  const article = userArticles.value.find(a => a.articleId === articleId)
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
  selectedArticle.value = updatedArticle
  
  const index = userArticles.value.findIndex(a => a.articleId === updatedArticle.articleId)
  if (index !== -1) {
    userArticles.value[index] = { ...userArticles.value[index], ...updatedArticle }
  }
}

// 게시물 목록 새로고침
const refreshUserArticles = async () => {
  if (selectedUser.value && selectedUser.value.userId) {
    await fetchLikedArticles()
    await fetchUserArticles(selectedUser.value.userId)
  }
}

// 컴포넌트 마운트 시 현재 사용자 정보 조회
onMounted(async () => {
  await fetchCurrentUserInfo()
})

// 라우트 변경 감지 - 검색 탭 재진입 시 초기화
watch(() => route.path, (newPath, oldPath) => {
  if (newPath === '/search' && oldPath !== '/search') {
    console.log('[Search] Route re-entered, resetting search...')
    searchQuery.value = ''
    searchResults.value = []
    selectedUser.value = null
    selectedArticle.value = null
    userArticles.value = []
  }
})
</script>

<style scoped>
.search-page {
  padding: 20px;
  width: 100%;
  max-width: 1000px;
  margin: 0 auto;
}

.search-header {
  margin-bottom: 30px;
}

.search-header h1 {
  margin-bottom: 15px;
}

.search-input {
  width: 100%;
  max-width: 500px;
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 20px;
  font-size: 0.95em;
  outline: none;
  transition: border 0.2s;
}

.search-input:focus {
  border-color: #007aff;
}

/* 검색 결과 */
.search-results {
  margin-top: 20px;
}

.loading,
.no-results {
  text-align: center;
  color: #999;
  padding: 20px;
}

.results-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.user-result-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 12px 15px;
  border: 1px solid #eee;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.user-result-item:hover {
  background-color: #f5f5f5;
  border-color: #ddd;
}

.user-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #eee;
}

.user-info {
  flex: 1;
}

.user-nickname {
  margin: 0 0 4px 0;
  font-size: 1em;
  font-weight: 600;
  color: #333;
}

.user-id {
  margin: 0;
  font-size: 0.9em;
  color: #666;
}

/* 사용자 프로필 뷰 */
.user-profile-view {
  width: 100%;
}

.back-button {
  background: transparent;
  border: 1px solid #ddd;
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.95em;
  margin-bottom: 20px;
  transition: all 0.2s;
}

.back-button:hover {
  background-color: #f5f5f5;
  border-color: #ccc;
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
  
  .user-result-item {
    padding: 10px 12px;
  }
  
  .user-avatar {
    width: 45px;
    height: 45px;
  }
}
</style>
