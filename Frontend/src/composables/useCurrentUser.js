import { ref } from 'vue'
import { apiFetch } from '../utils/apiClient'

// 전역 상태
const currentUser = ref({
  userId: null,
  nickname: '',
  userIdentifier: '',
  profileImageUrl: ''
})
const isLoaded = ref(false)
const isLoading = ref(false)

export function useCurrentUser() {
  // 현재 사용자 정보 조회
  const loadCurrentUser = async (force = false) => {
    // 이미 로드되었고 강제 새로고침이 아니면 스킵
    if (isLoaded.value && !force) {
      console.log('[useCurrentUser] 이미 로드됨, API 호출 스킵')
      return currentUser.value
    }

    // 이미 로딩 중이면 대기
    if (isLoading.value) {
      console.log('[useCurrentUser] 로딩 중, 대기')
      // 로딩이 완료될 때까지 대기
      while (isLoading.value) {
        await new Promise(resolve => setTimeout(resolve, 50))
      }
      return currentUser.value
    }

    try {
      isLoading.value = true
      console.log('[useCurrentUser] 사용자 정보 로드 시작')
      
      const response = await apiFetch('/user/info', { method: 'GET' })
      if (!response.ok) {
        console.error('[useCurrentUser] Failed to fetch user info:', response.status)
        return null
      }
      
      const result = await response.json()
      const userData = result.data
      
      currentUser.value = {
        userId: userData.userId,
        nickname: userData.nickname,
        userIdentifier: userData.userIdentifier,
        profileImageUrl: userData.profileImageUrl || ''
      }
      
      isLoaded.value = true
      console.log('[useCurrentUser] 사용자 정보 로드 완료:', currentUser.value)
      
      return currentUser.value
    } catch (err) {
      console.error('[useCurrentUser] Error fetching user info:', err)
      return null
    } finally {
      isLoading.value = false
    }
  }

  // 사용자 정보 새로고침
  const refreshCurrentUser = async () => {
    isLoaded.value = false
    await loadCurrentUser(true)
  }

  // 사용자 정보 초기화 (로그아웃 시)
  const clearCurrentUser = () => {
    currentUser.value = {
      userId: null,
      nickname: '',
      userIdentifier: '',
      profileImageUrl: ''
    }
    isLoaded.value = false
    isLoading.value = false
    console.log('[useCurrentUser] 사용자 정보 초기화')
  }

  return {
    currentUser,
    isLoaded,
    isLoading,
    loadCurrentUser,
    refreshCurrentUser,
    clearCurrentUser
  }
}
