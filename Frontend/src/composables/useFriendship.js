import { ref } from 'vue'
import api from '../services/api'

// 전역 상태
const friends = ref([]) // 친구 목록
const sentRequests = ref([]) // 보낸 친구 요청
const receivedRequests = ref([]) // 받은 친구 요청
const isLoaded = ref(false) // 로드 완료 여부

export function useFriendship() {
  // 친구 목록 조회
  const loadFriends = async () => {
    try {
      const response = await api.getFriendsList()
      if (response.data.code === 'FRIENDSHIP_READ_SUCCESS') {
        friends.value = response.data.data || []
        console.log('[useFriendship] 친구 목록 로드:', friends.value.length)
      }
    } catch (error) {
      console.error('[useFriendship] 친구 목록 조회 실패:', error)
      friends.value = []
    }
  }

  // 보낸 친구 요청 조회
  const loadSentRequests = async () => {
    try {
      const response = await api.getSentFriendRequests()
      if (response.data.code === 'SENT_FRIENDSHIP_REQUESTS_READ_SUCCESS') {
        sentRequests.value = response.data.data || []
        console.log('[useFriendship] 보낸 요청 로드:', sentRequests.value.length)
      }
    } catch (error) {
      console.error('[useFriendship] 보낸 친구 요청 조회 실패:', error)
      sentRequests.value = []
    }
  }

  // 받은 친구 요청 조회
  const loadReceivedRequests = async () => {
    try {
      const response = await api.getReceivedFriendRequests()
      if (response.data.code === 'RECEIVED_FRIENDSHIP_REQUESTS_READ_SUCCESS') {
        receivedRequests.value = response.data.data || []
        console.log('[useFriendship] 받은 요청 로드:', receivedRequests.value.length)
      }
    } catch (error) {
      console.error('[useFriendship] 받은 친구 요청 조회 실패:', error)
      receivedRequests.value = []
    }
  }

  // 모든 친구 관련 데이터 로드 (중복 호출 방지)
  const loadAllFriendshipData = async (force = false) => {
    // 이미 로드되었고 강제 새로고침이 아니면 스킵
    if (isLoaded.value && !force) {
      console.log('[useFriendship] 이미 로드됨, API 호출 스킵')
      return
    }
    
    console.log('[useFriendship] 친구 관련 데이터 로드 시작')
    await Promise.all([
      loadFriends(),
      loadSentRequests(),
      loadReceivedRequests()
    ])
    isLoaded.value = true
    console.log('[useFriendship] 친구 관련 데이터 로드 완료')
  }

  // 특정 사용자와의 관계 확인
  const getFriendshipStatus = (userId, userIdentifier) => {
    // 친구인지 확인
    const isFriend = friends.value.some(f => 
      f.friendUserId === userId || f.friendIdentifier === userIdentifier
    )
    if (isFriend) {
      return 'friend'
    }

    // 내가 보낸 요청이 있는지 확인 (대기중)
    const hasSentRequest = sentRequests.value.some(req =>
      req.friendshipRequestReceiverId === userId || 
      req.friendshipRequestReceiverIdentifier === userIdentifier
    )
    if (hasSentRequest) {
      return 'sent'
    }

    // 받은 요청이 있는지 확인
    const hasReceivedRequest = receivedRequests.value.some(req =>
      req.friendshipRequestSenderId === userId ||
      req.friendshipRequestSenderIdentifier === userIdentifier
    )
    if (hasReceivedRequest) {
      return 'received'
    }

    return 'none'
  }

  // 데이터 새로고침 (강제)
  const refreshFriendshipData = async () => {
    isLoaded.value = false // 강제 새로고침을 위해 플래그 리셋
    await loadAllFriendshipData(true)
  }

  return {
    friends,
    sentRequests,
    receivedRequests,
    isLoaded,
    loadAllFriendshipData,
    getFriendshipStatus,
    refreshFriendshipData
  }
}
