<template>
  <div class="page">
    <h1>홈 피드</h1>
    <div v-if="loading">로딩 중...</div>
    <div v-else>
      <FeedItem 
        v-for="f in feeds" 
        :key="f.id" 
        :feed="f" 
        :friendList="friendList"
        :currentUserIdentifier="currentUserIdentifier"
        :currentUserInfo="currentUserInfo"
        @add-friend="handleAddFriend"
      />
      <div v-if="feeds.length === 0">팔로잉한 사용자의 게시물이 없습니다.</div>
      <div v-if="loadingMore" class="loading-more">피드를 불러오는 중...</div>
      <div ref="loadMoreTrigger" class="load-more-trigger" aria-hidden="true"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import FeedItem from './FeedItem.vue'
import { apiFetch } from '@/utils/apiClient'
import { useFriendship } from '@/composables/useFriendship'
import { useCurrentUser } from '@/composables/useCurrentUser'

const feeds = ref([])
const loading = ref(false)
const loadingMore = ref(false)
const friendList = ref([])
const currentUserIdentifier = ref('')
const currentUserInfo = ref({})
const likedArticleIds = ref([])
const hasNext = ref(false)
const lastContentId = ref(null)
const loadMoreTrigger = ref(null)
const FOLLOWING_PAGE_SIZE = 5
let observer = null

// 전역 상태 관리
const { loadAllFriendshipData } = useFriendship()
const { currentUser, loadCurrentUser } = useCurrentUser()

// 기본 프로필 사진
const DEFAULT_PROFILE_IMAGE = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgdmlld0JveD0iMCAwIDEwMCAxMDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxyZWN0IHdpZHRoPSIxMDAiIGhlaWdodD0iMTAwIiBmaWxsPSIjRTBFMEUwIi8+CjxjaXJjbGUgY3g9IjUwIiBjeT0iMzUiIHI9IjE1IiBmaWxsPSIjOTk5Ii8+CjxwYXRoIGQ9Ik0yMCA2NSBDMjAgNjAgMzAgNTAgNTAgNTAgQzcwIDUwIDgwIDYwIDgwIDY1IiBmaWxsPSIjOTk5Ii8+Cjwvc3ZnPg=='

// 프로필 이미지 URL 정규화
const getProfileImageUrl = (imageUrl) => {
  if (!imageUrl || imageUrl === null || (typeof imageUrl === 'string' && !imageUrl.trim())) {
    return DEFAULT_PROFILE_IMAGE
  }
  return imageUrl
}

const mapArticleToFeed = (item, idx) => {
  return {
    id: item.articleId || item.id || `${item.authorIdentifier}-${item.createdAt}-${idx}`,
    author: {
      name: item.authorIdentifier,
      profileImage: getProfileImageUrl(item.authorProfileImageUrl)
    },
    text: item.articleText,
    imageUrl: item.articleMediaUrl || item.articleThumbnailUrl || '',
    likes: item.articleLikeCount || 0,
    liked: likedArticleIds.value.includes(item.articleId || item.id),
    commentCount: item.articleCommentCount || 0,
    raw: item
  }
}

const parseFollowingResponse = (body) => {
  const payload = body?.data || {}
  const rawContent = Array.isArray(payload.content) ? payload.content : []
  const content = rawContent.slice(0, FOLLOWING_PAGE_SIZE)
  const nextCursor = payload.lastContentId ?? null
  const nextExists = payload.hasNext === true

  return { content, nextCursor, nextExists }
}

const mergeUniqueFeeds = (existingFeeds, incomingArticles) => {
  const existingIds = new Set(existingFeeds.map((feed) => feed.id))
  const uniqueArticles = incomingArticles.filter((article) => {
    const articleId = article.articleId || article.id
    if (articleId == null) return true
    return !existingIds.has(articleId)
  })

  const startIndex = existingFeeds.length
  const mapped = uniqueArticles.map((it, i) => mapArticleToFeed(it, startIndex + i))
  return [...existingFeeds, ...mapped]
}

const fetchFollowingArticles = async ({ articleId } = {}) => {
  const query = articleId != null ? `?articleId=${encodeURIComponent(articleId)}` : ''
  const res = await apiFetch(`/article/following${query}`, { method: 'GET' })
  if (!res || !res.ok) {
    console.error('Failed to load following articles', res?.status)
    return null
  }

  const body = await res.json()
  return parseFollowingResponse(body)
}

const loadInitialFeed = async () => {
  const result = await fetchFollowingArticles()
  if (!result) return

  feeds.value = result.content.map((it, i) => mapArticleToFeed(it, i))
  lastContentId.value = result.nextCursor
  hasNext.value = result.nextExists
}

const loadMoreFeed = async () => {
  if (loading.value || loadingMore.value) return
  if (!hasNext.value) return
  if (lastContentId.value == null) return

  loadingMore.value = true
  try {
    const result = await fetchFollowingArticles({ articleId: lastContentId.value })
    if (!result) return

    feeds.value = mergeUniqueFeeds(feeds.value, result.content)
    lastContentId.value = result.nextCursor
    hasNext.value = result.nextExists
  } catch (err) {
    console.error('Error fetching more feed', err)
  } finally {
    loadingMore.value = false
  }
}

const setupInfiniteScroll = async () => {
  await nextTick()
  if (!loadMoreTrigger.value) return

  observer = new IntersectionObserver(
    (entries) => {
      const [entry] = entries
      if (entry?.isIntersecting) {
        loadMoreFeed()
      }
    },
    { root: null, rootMargin: '200px 0px', threshold: 0 }
  )

  observer.observe(loadMoreTrigger.value)
}

// 현재 사용자 정보 조회 (전역 상태 사용)
const fetchCurrentUser = async () => {
  const user = await loadCurrentUser()
  if (user) {
    currentUserIdentifier.value = user.userIdentifier
    currentUserInfo.value = {
      profileImageUrl: user.profileImageUrl || '',
      nickname: user.nickname || user.userIdentifier
    }
  }
}

// 친구 목록 조회
const fetchFriendList = async () => {
  try {
    const res = await apiFetch('/friendship/friendships', { method: 'GET' })
    if (!res || !res.ok) {
      console.error('Failed to load friend list', res?.status)
      return
    }
    const body = await res.json()
    friendList.value = Array.isArray(body.data) ? body.data : []
    console.log('Friend list loaded:', friendList.value)
  } catch (err) {
    console.error('Error fetching friend list', err)
  }
}

// 좋아요 목록 조회
const fetchLikedArticles = async () => {
  try {
    const res = await apiFetch('/heart', { method: 'GET' })
    if (!res || !res.ok) {
      console.error('Failed to load liked articles', res?.status)
      return
    }
    const body = await res.json()
    const likedList = Array.isArray(body.data) ? body.data : []
    likedArticleIds.value = likedList.map(item => item.articleId)
    console.log('Liked articles loaded:', likedArticleIds.value)
  } catch (err) {
    console.error('Error fetching liked articles', err)
  }
}

// 친구 추가 처리
const handleAddFriend = async (receiverIdentifier) => {
  try {
    const res = await apiFetch('/friendship-request/send', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ receiverIdentifier })
    })
    
    if (!res || !res.ok) {
      console.error('Failed to send friend request', res?.status)
      alert('친구 추가 요청에 실패했습니다.')
      return
    }
    
    const body = await res.json()
    console.log('Friend request sent:', body)
    alert('친구 추가 요청을 보냈습니다.')
  } catch (err) {
    console.error('Error sending friend request', err)
    alert('친구 추가 요청 중 오류가 발생했습니다.')
  }
}

onMounted(async () => {
  loading.value = true
  try {
    // 현재 사용자 정보, 친구 목록, 좋아요 목록을 먼저 로드
    await fetchCurrentUser()
    await fetchFriendList()
    await fetchLikedArticles()
    
    // 전역 친구 관계 데이터 로드 (친구 목록, 보낸/받은 요청)
    await loadAllFriendshipData()

    await loadInitialFeed()
  } catch (err) {
    console.error('Error fetching feed', err)
  } finally {
    loading.value = false
  }

  await setupInfiniteScroll()
})

onUnmounted(() => {
  if (observer) {
    observer.disconnect()
    observer = null
  }
})

</script>

<style scoped>
.page {
  width: 100%;
  max-width: 780px;
  margin: 0 auto;
  padding: 20px;
  box-sizing: border-box;
}

.loading-more {
  text-align: center;
  color: #666;
  margin: 12px 0;
}

.load-more-trigger {
  height: 1px;
}

@media (max-width: 768px) {
  .page {
    max-width: 100%;
    padding: 16px;
  }
}
</style>