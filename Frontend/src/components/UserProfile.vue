<template>
  <div class="user-profile-page">
    <!-- 로딩 상태 -->
    <div v-if="loading" class="loading-message">
      프로필을 불러오는 중...
    </div>

    <!-- 에러 메시지 -->
    <div v-else-if="error" class="error-message">
      {{ error }}
    </div>

    <!-- 프로필 내용 -->
    <div v-else-if="userProfile">
      <!-- 프로필 헤더 -->
      <div class="profile-header">
        <img 
          :src="userProfile.profileImageUrl" 
          alt="프로필" 
          class="profile-img" 
          @error="handleImageError"
        />
        <div class="profile-info">
          <div class="nickname-row">
            <h2>{{ userProfile.nickname }}</h2>
            <!-- 친구 관계 상태 표시 -->
            <span v-if="friendshipStatus === 'friend'" class="status-badge friend">친구</span>
            <span v-else-if="friendshipStatus === 'sent'" class="status-badge sent">요청 보냄</span>
            <span v-else-if="friendshipStatus === 'received'" class="status-badge received">요청 받음</span>
          </div>
          <p class="user-identifier">@{{ userProfile.userIdentifier }}</p>
          <div class="friend-info">
            친구 {{ userProfile.friendCount || 0 }}명
          </div>
          <div v-if="showProfileActions" class="profile-actions">
            <button
              v-if="showAddFriendButton"
              class="action-btn add-friend"
              :disabled="isSendingFriendRequest"
              @click="sendFriendRequest"
            >
              친구 추가
            </button>
            <button
              class="action-btn send-message"
              :disabled="isOpeningChat"
              @click="openChatWithUser"
            >
              메시지 보내기
            </button>
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
      <div v-if="loadingArticles" class="loading-message">
        로딩 중...
      </div>

      <!-- 에러 메시지 -->
      <div v-else-if="articlesError" class="error-message">
        {{ articlesError }}
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
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { apiFetch } from '../utils/apiClient'
import api from '@/services/api'
import ArticleDetail from './ArticleDetail.vue'
import { useFriendship } from '../composables/useFriendship'
import { useCurrentUser } from '../composables/useCurrentUser'
import { useChatRooms } from '../composables/useChatRooms'

const route = useRoute()
const router = useRouter()

// 전역 상태 관리
const { getFriendshipStatus, loadAllFriendshipData } = useFriendship()
const { currentUser, loadCurrentUser } = useCurrentUser()
const { chatRooms, loadChatRooms, refreshChatRooms, findPersonalRoomWithUser } = useChatRooms()

// 기본 프로필 사진
const DEFAULT_PROFILE_IMAGE = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgdmlld0JveD0iMCAwIDEwMCAxMDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxyZWN0IHdpZHRoPSIxMDAiIGhlaWdodD0iMTAwIiBmaWxsPSIjRTBFMEUwIi8+CjxjaXJjbGUgY3g9IjUwIiBjeT0iMzUiIHI9IjE1IiBmaWxsPSIjOTk5Ii8+CjxwYXRoIGQ9Ik0yMCA2NSBDMjAgNjAgMzAgNTAgNTAgNTAgQzcwIDUwIDgwIDYwIDgwIDY1IiBmaWxsPSIjOTk5Ii8+Cjwvc3ZnPg=='

// 상태 관리
const loading = ref(true)
const error = ref('')
const userProfile = ref(null)
const userArticles = ref([])
const loadingArticles = ref(false)
const articlesError = ref('')
const activeTab = ref('post')
const selectedArticle = ref(null)
const likedArticleIds = ref([])
const isSendingFriendRequest = ref(false)
const isOpeningChat = ref(false)

// 현재 로그인한 사용자 정보
const currentLoginUser = ref({
  userId: null,
  nickname: '',
  userIdentifier: '',
  profileImage: ''
})

// 친구 관계 상태 계산
const friendshipStatus = computed(() => {
  if (!userProfile.value) return null
  return getFriendshipStatus(userProfile.value.userId, userProfile.value.userIdentifier)
})

const isMyProfile = computed(() => {
  if (!userProfile.value) return false
  return userProfile.value.userId === currentUser.value.userId
})

const showProfileActions = computed(() => {
  return !isMyProfile.value
})

const showAddFriendButton = computed(() => {
  return showProfileActions.value && friendshipStatus.value === 'none'
})

// 프로필 이미지 URL 정규화
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

// 현재 로그인한 사용자 정보 조회 (전역 상태 사용)
const fetchCurrentUserInfo = async () => {
  const user = await loadCurrentUser()
  if (user) {
    currentLoginUser.value = {
      userId: user.userId,
      nickname: user.nickname,
      userIdentifier: user.userIdentifier,
      profileImage: user.profileImageUrl || ''
    }
    console.log('[UserProfile] Current user info loaded:', currentLoginUser.value)
  }
}

// 좋아요 목록 조회
const fetchLikedArticles = async () => {
  try {
    const res = await apiFetch('/heart', { method: 'GET' })
    if (!res || !res.ok) {
      console.error('[UserProfile] Failed to load liked articles', res?.status)
      return
    }
    const body = await res.json()
    const likedList = Array.isArray(body.data) ? body.data : []
    likedArticleIds.value = likedList.map(item => item.articleId)
  } catch (err) {
    console.error('[UserProfile] Error fetching liked articles', err)
  }
}

// 사용자 프로필 정보 조회
const fetchUserProfile = async (identifier) => {
  try {
    loading.value = true
    error.value = ''
    
    let userId = identifier
    
    // route.state에서 userId가 전달되었는지 확인 (검색 결과에서 온 경우)
    const stateUserId = history.state?.userId
    if (stateUserId) {
      console.log('[UserProfile] Using userId from route state:', stateUserId)
      userId = stateUserId
    }
    // identifier가 숫자가 아니면 friendIdentifier로 간주하고 검색 API로 userId를 찾음
    else if (isNaN(identifier)) {
      console.log('[UserProfile] Searching for user with identifier:', identifier)
      const searchResponse = await apiFetch('/user/search', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          nicknameOrUserIdentifier: identifier
        })
      })
      
      if (!searchResponse.ok) {
        console.error('[UserProfile] Failed to search user:', searchResponse.status)
        error.value = '사용자를 찾을 수 없습니다.'
        return
      }
      
      const searchResult = await searchResponse.json()
      const users = searchResult.data || []
      
      if (users.length === 0) {
        error.value = '사용자를 찾을 수 없습니다.'
        return
      }
      
      // 검색 결과에서 userId 추출
      userId = users[0].userId
      console.log('[UserProfile] Found userId:', userId)
    }
    
    // userId를 사용하여 프로필 정보 조회 (GET)
    const response = await apiFetch(`/user/profile/${userId}`, {
      method: 'GET'
    })
    
    if (!response.ok) {
      console.error('[UserProfile] Failed to fetch user profile:', response.status)
      error.value = '프로필을 불러올 수 없습니다.'
      return
    }
    
    const result = await response.json()
    const userData = result.data
    
    // 프로필 정보 설정
    userProfile.value = {
      userId: userId,
      nickname: userData.nickname,
      userIdentifier: userData.userIdentifier,
      profileImageUrl: getProfileImageUrl(userData.profileImageUrl),
      friendCount: 0
    }
    
    console.log('[UserProfile] Profile loaded:', userProfile.value)
    
    // 게시물 조회
    await fetchUserArticles(userId)
  } catch (err) {
    console.error('[UserProfile] Error fetching user profile:', err)
    error.value = '프로필을 불러오는 중 오류가 발생했습니다.'
  } finally {
    loading.value = false
  }
}

// 사용자 게시물 목록 조회
const fetchUserArticles = async (userId) => {
  console.log('[UserProfile] fetchUserArticles called with userId:', userId)
  
  if (!userId) {
    console.error('[UserProfile] userId is empty or null')
    userArticles.value = []
    return
  }
  
  loadingArticles.value = true
  articlesError.value = ''
  try {
    const url = `/article/${userId}`
    console.log('[UserProfile] Fetching articles from URL:', url)
    
    const response = await apiFetch(url, { method: 'GET' })
    console.log('[UserProfile] Response status:', response.status)
    
    if (!response.ok) {
      console.error('[UserProfile] Failed to fetch articles:', response.status)
      throw new Error('게시물 조회 실패')
    }
    
    const result = await response.json()
    console.log('[UserProfile] Articles response:', result)
    
    const articles = result.data || []
    console.log('[UserProfile] Articles count:', articles.length)
    
    // API 응답 필드명에 맞춰 매핑
    userArticles.value = articles.map(article => ({
      articleId: article.articleId,
      articleMediaUrl: article.articleMediaUrl,
      articleThumbnailUrl: article.articleThumbnailUrl,
      articleText: article.articleText,
      articleLikeCount: article.articleLikeCount || 0,
      articleCommentCount: article.articleCommentCount || 0,
      nickname: article.nickname || userProfile.value?.nickname,
      authorNickname: article.authorNickname || userProfile.value?.nickname,
      authorIdentifier: article.authorIdentifier || userProfile.value?.userIdentifier,
      profileImageUrl: article.authorProfileImageUrl || userProfile.value?.profileImageUrl,
      authorProfileImageUrl: article.authorProfileImageUrl || userProfile.value?.profileImageUrl,
      isLiked: likedArticleIds.value.includes(article.articleId)
    }))
  } catch (err) {
    console.error('[UserProfile] Error fetching articles:', err)
    articlesError.value = '게시물을 불러올 수 없습니다.'
  } finally {
    loadingArticles.value = false
  }
}

// 게시물 상세 보기
const viewArticleDetail = async (articleId) => {
  console.log('[UserProfile] Opening article detail:', articleId)
  try {
    // 먼저 목록에서 해당 게시물을 찾아서 설정
    const article = userArticles.value.find(a => a.articleId === articleId)
    if (article) {
      selectedArticle.value = {
        ...article,
        isLiked: likedArticleIds.value.includes(article.articleId),
        authorIdentifier: article.authorIdentifier || userProfile.value?.userIdentifier,
        profileImageUrl: article.authorProfileImageUrl || article.profileImageUrl || userProfile.value?.profileImageUrl,
        nickname: article.nickname || userProfile.value?.nickname,
        authorNickname: article.authorNickname || userProfile.value?.nickname
      }
      console.log('[UserProfile] Article detail set from list:', selectedArticle.value)
    } else {
      // 목록에 없으면 API로 조회
      const response = await apiFetch(`/article/detail/${articleId}`, { method: 'GET' })
      if (!response.ok) {
        console.error('[UserProfile] Failed to fetch article detail')
        return
      }
      const result = await response.json()
      const articleData = result.data
      
      selectedArticle.value = {
        ...articleData,
        isLiked: likedArticleIds.value.includes(articleData.articleId),
        authorIdentifier: articleData.authorIdentifier || userProfile.value?.userIdentifier,
        profileImageUrl: articleData.authorProfileImageUrl || articleData.profileImageUrl || userProfile.value?.profileImageUrl,
        authorProfileImageUrl: articleData.authorProfileImageUrl || articleData.profileImageUrl || userProfile.value?.profileImageUrl,
        nickname: articleData.nickname || articleData.authorNickname || userProfile.value?.nickname,
        authorNickname: articleData.authorNickname || articleData.nickname || userProfile.value?.nickname
      }
      console.log('[UserProfile] Article detail fetched from API:', selectedArticle.value)
    }
  } catch (err) {
    console.error('[UserProfile] Error fetching article detail:', err)
  }
}

// 게시물 상세 닫기
const closeArticleDetail = () => {
  selectedArticle.value = null
}

// 게시물 업데이트 핸들러
const handleArticleUpdate = (updatedArticle) => {
  const index = userArticles.value.findIndex(a => a.articleId === updatedArticle.articleId)
  if (index !== -1) {
    userArticles.value[index] = {
      ...userArticles.value[index],
      ...updatedArticle
    }
  }
  if (selectedArticle.value && selectedArticle.value.articleId === updatedArticle.articleId) {
    selectedArticle.value = {
      ...selectedArticle.value,
      ...updatedArticle
    }
  }
}

// 게시물 목록 새로고침
const refreshUserArticles = async () => {
  if (userProfile.value?.userId) {
    await fetchUserArticles(userProfile.value.userId)
  }
}

const sendFriendRequest = async () => {
  if (!userProfile.value?.userIdentifier || !userProfile.value?.userId) return
  if (isSendingFriendRequest.value) return

  isSendingFriendRequest.value = true
  try {
    const response = await api.sendFriendRequest(userProfile.value.userIdentifier)
    if (response.data.code === 'FRIENDSHIP_REQUEST_CREATE_SUCCESS') {
      // 로컬 상태 업데이트: sentRequests 배열에 새로운 요청 추가
      const { addLocalSentRequest } = useFriendship()
      addLocalSentRequest(userProfile.value.userId, userProfile.value.userIdentifier)
      alert('친구 요청을 보냈습니다.')
    } else {
      alert('친구 요청에 실패했습니다.')
    }
  } catch (err) {
    console.error('[UserProfile] Failed to send friend request:', err)
    alert('친구 요청에 실패했습니다.')
  } finally {
    isSendingFriendRequest.value = false
  }
}

const openChatWithUser = async () => {
  if (!userProfile.value?.userId || !userProfile.value?.userIdentifier) return
  if (isOpeningChat.value) return

  isOpeningChat.value = true
  try {
    router.push({
      path: '/chat',
      query: {
        partnerIdentifier: userProfile.value.userIdentifier,
        partnerUserId: userProfile.value.userId
      }
    })
  } finally {
    isOpeningChat.value = false
  }
}

// 컴포넌트 마운트 시 실행
onMounted(async () => {
  const identifier = route.params.id
  console.log('[UserProfile] Route param identifier:', identifier)
  
  if (!identifier) {
    error.value = '사용자 정보가 없습니다.'
    loading.value = false
    return
  }
  
  // 현재 로그인한 사용자 정보 조회
  await fetchCurrentUserInfo()

  // 친구 관련 데이터 로드 (항상 최신 상태를 유지하기 위해 강제 새로고침)
  await loadAllFriendshipData(true)
  
  // 좋아요 목록 조회
  await fetchLikedArticles()
  
  // 사용자 프로필 조회 (identifier가 friendIdentifier면 검색 후 userId를 찾아서 조회)
  await fetchUserProfile(identifier)
})
</script>

<style scoped>
.user-profile-page {
  padding: 24px;
  max-width: 935px;
  margin: 0 auto;
}

.loading-message,
.error-message,
.empty-message {
  text-align: center;
  padding: 40px 20px;
  color: #999;
  font-size: 1em;
}

.error-message {
  color: #ff4444;
}

/* 프로필 헤더 */
.profile-header {
  display: flex;
  align-items: center;
  gap: 40px;
  margin-bottom: 32px;
  padding-bottom: 32px;
  border-bottom: 1px solid #e0e0e0;
}

.profile-img {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #e0e0e0;
}

.profile-info h2 {
  font-size: 1.8em;
  margin-bottom: 8px;
  margin: 0;
}

/* 닉네임과 배지를 가로로 배치 */
.nickname-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.user-identifier {
  color: #666;
  font-size: 1em;
  margin-bottom: 16px;
}

.friend-info {
  font-size: 1em;
  color: #333;
}

.profile-actions {
  display: flex;
  gap: 10px;
  margin-top: 14px;
  flex-wrap: wrap;
}

.action-btn {
  padding: 8px 14px;
  border-radius: 8px;
  border: 1px solid #ddd;
  background: #fff;
  cursor: pointer;
  font-size: 0.9em;
  font-weight: 600;
  transition: background-color 0.2s, border-color 0.2s;
}

.action-btn:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.action-btn.add-friend {
  background-color: #1e88e5;
  border-color: #1e88e5;
  color: #fff;
}

.action-btn.add-friend:hover:not(:disabled) {
  background-color: #1976d2;
  border-color: #1976d2;
}

.action-btn.send-message {
  background-color: #6b6bff;
  border-color: #6b6bff;
  color: #fff;
}

.action-btn.send-message:hover:not(:disabled) {
  background-color: #5555ff;
  border-color: #5555ff;
}

/* 친구 관계 상태 */
.status-badge {
  display: inline-block;
  padding: 5px 12px;
  border-radius: 12px;
  font-size: 0.75em;
  font-weight: 600;
  white-space: nowrap;
}

.status-badge.friend {
  background-color: #e3f2fd;
  color: #1976d2;
}

.status-badge.sent {
  background-color: #fff3e0;
  color: #f57c00;
}

.status-badge.received {
  background-color: #e8f5e9;
  color: #388e3c;
}

/* 탭 버튼 */
.tab-buttons {
  display: flex;
  justify-content: center;
  gap: 60px;
  margin-bottom: 32px;
  border-bottom: 1px solid #e0e0e0;
}

.tab-buttons button {
  padding: 12px 0;
  background: none;
  border: none;
  font-size: 0.95em;
  font-weight: 600;
  color: #999;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
  transition: all 0.3s;
}

.tab-buttons button.active {
  color: #333;
  border-bottom-color: #333;
}

/* 게시물 그리드 */
.feed-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 4px;
}

.feed-item {
  position: relative;
  aspect-ratio: 1;
  cursor: pointer;
  overflow: hidden;
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
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  opacity: 0;
  transition: opacity 0.2s;
  color: white;
  font-weight: bold;
}

.feed-item:hover .article-overlay {
  opacity: 1;
}

@media (max-width: 768px) {
  .profile-header {
    flex-direction: column;
    text-align: center;
    gap: 20px;
  }

  .profile-img {
    width: 120px;
    height: 120px;
  }

  .feed-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 2px;
  }
}
</style>
