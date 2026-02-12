import { ref } from 'vue'
import api from '@/services/api'

const chatRooms = ref([])
const isLoaded = ref(false)
const isLoading = ref(false)
const error = ref(null)

export function useChatRooms() {
  const loadChatRooms = async (force = false) => {
    if (isLoaded.value && !force) {
      return chatRooms.value
    }

    if (isLoading.value) {
      while (isLoading.value) {
        await new Promise(resolve => setTimeout(resolve, 50))
      }
      return chatRooms.value
    }

    isLoading.value = true
    error.value = null
    try {
      const response = await api.getParticipatedChatRooms()
      chatRooms.value = response.data.data || []
      isLoaded.value = true
      return chatRooms.value
    } catch (err) {
      console.error('[useChatRooms] Failed to load chat rooms:', err)
      chatRooms.value = []
      error.value = '채팅방 목록을 불러오지 못했습니다.'
      return []
    } finally {
      isLoading.value = false
    }
  }

  const refreshChatRooms = async () => {
    isLoaded.value = false
    return loadChatRooms(true)
  }

  const findPersonalRoomWithUser = (userId) => {
    if (!userId) return null
    return chatRooms.value.find(room => {
      if (room.chatRoomType !== 'PERSONAL_CHATROOM') return false
      const participants = room.chatRoomParticipants || []
      return participants.some(p => p.userId === userId)
    }) || null
  }

  return {
    chatRooms,
    isLoaded,
    isLoading,
    error,
    loadChatRooms,
    refreshChatRooms,
    findPersonalRoomWithUser
  }
}
