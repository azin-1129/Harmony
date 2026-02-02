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
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import FeedItem from './FeedItem.vue'
import { apiFetch } from '@/utils/apiClient'
import { getAccessToken } from '@/utils/tokenService'
import { useFriendship } from '@/composables/useFriendship'
import { useCurrentUser } from '@/composables/useCurrentUser'

const feeds = ref([])
const loading = ref(false)
const friendList = ref([])
const currentUserIdentifier = ref('')
const currentUserInfo = ref({})
const likedArticleIds = ref([])

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
    
    // 피드 로드
    const res = await apiFetch('/article/following', { method: 'GET' })
    if (!res) return
    if (!res.ok) {
      console.error('Failed to load following articles', res.status)
      return
    }
    const body = await res.json()
    const list = Array.isArray(body.data) ? body.data : []
    feeds.value = list.map((it, i) => mapArticleToFeed(it, i))
  } catch (err) {
    console.error('Error fetching feed', err)
  } finally {
    loading.value = false
  }
})

</script>

<style scoped>
.page {
  padding: 20px;
}
</style>