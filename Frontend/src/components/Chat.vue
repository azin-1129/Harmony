<template>
  <div class="chat-page">
    <!-- 좌측 채팅방 목록 -->
    <ChatSidebar
      :key="chatSidebarKey"
      :selectedRoomId="selectedRoom?.chatRoomId"
      @select-room="selectRoom"
    />
    
    <!-- 우측 채팅창 -->
    <ChatWindow
      v-if="selectedRoom"
      :selectedRoom="selectedRoom"
      :stompConnected="stompConnected"
      @message-sent="handleMessageSent"
      @room-left="handleRoomLeft"
    />
    
    <!-- 선택된 채팅방 없을 때 안내 문구 -->
    <div v-else class="chat-placeholder">
      채팅방을 선택하세요
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ChatSidebar from '@/components/chat/ChatSidebar.vue'
import ChatWindow from '@/components/chat/ChatWindow.vue'
import api from '@/services/api'
import { useChatRooms } from '@/composables/useChatRooms'
import stompService from '@/services/stompService'
import { getAccessToken } from '@/utils/tokenService'
import { apiUrl } from '@/utils/config'

const route = useRoute()
const router = useRouter()
const selectedRoom = ref(null)
const stompConnected = ref(false)
const chatSidebarKey = ref(0)

const { chatRooms, loadChatRooms, refreshChatRooms, findPersonalRoomWithUser } = useChatRooms()
const isDirectChatLoading = ref(false)
const lastDirectPartner = ref('')

// 채팅방 선택
const selectRoom = (room) => {
  console.log('[Chat] Selected room:', room.chatRoomId, room)
  selectedRoom.value = room
}

const selectRoomById = (roomId) => {
  if (!roomId) return
  const targetId = Number(roomId)
  if (!targetId) return
  // 다른 방으로 변경될 때만 설정
  if (selectedRoom.value?.chatRoomId === targetId) return
  const room = chatRooms.value.find(r => r.chatRoomId === targetId)
  if (room) {
    console.log('[Chat] selectRoomById:', targetId)
    selectedRoom.value = room
  }
}

// 메시지 전송 처리
const handleMessageSent = (message) => {
  console.log('Message sent:', message)
  // TODO: STOMP를 통한 실제 메시지 전송 구현
}

// 채팅방 나가기 처리
const handleRoomLeft = async (roomId) => {
  console.log('Left room:', roomId)
  selectedRoom.value = null
  await refreshChatRooms()
  chatSidebarKey.value++ // ChatSidebar 새로고침
}

onMounted(async () => {
  stompService.connect({
    url: apiUrl('/ws'),
    useSockJS: true,
    connectHeaders: {
      Authorization: `Bearer ${getAccessToken() || ''}`
    },
    onConnect: () => {
      stompConnected.value = true
      console.log('[Chat] STOMP connected')
    },
    onStompError: (frame) => {
      console.error('[Chat] STOMP error:', frame)
    }
  })
  await loadChatRooms()
  selectRoomById(route.query.roomId)
  await handleDirectChatEntry()
})

onBeforeUnmount(() => {
  stompService.disconnect()
  stompConnected.value = false
})

watch(() => route.query.roomId, (roomId) => {
  selectRoomById(roomId)
})

watch(
  () => [route.query.partnerIdentifier, route.query.partnerUserId],
  async () => {
    await handleDirectChatEntry()
  }
)

watch(chatRooms, () => {
  selectRoomById(route.query.roomId)
})

const handleDirectChatEntry = async () => {
  const partnerIdentifier = route.query.partnerIdentifier
  const partnerUserId = route.query.partnerUserId
  if (!partnerIdentifier) return
  if (isDirectChatLoading.value) return

  const partnerKey = `${partnerIdentifier}-${partnerUserId || ''}`
  if (lastDirectPartner.value === partnerKey) return
  lastDirectPartner.value = partnerKey

  isDirectChatLoading.value = true
  try {
    await loadChatRooms()

    let room = null
    if (partnerUserId) {
      room = findPersonalRoomWithUser(Number(partnerUserId))
    }

    if (!room) {
      const response = await api.createPersonalChatRoom(partnerIdentifier)
      const createdRoomId = response.data.data
      await refreshChatRooms()
      room = chatRooms.value.find(r => r.chatRoomId === createdRoomId) || null
    }

    if (room) {
      // selectedRoom 직접 설정하고 URL 업데이트
      selectedRoom.value = room
      await router.replace({ path: '/chat', query: { roomId: room.chatRoomId } })
    }
  } catch (err) {
    console.error('[Chat] Failed to open direct chat:', err)
  } finally {
    isDirectChatLoading.value = false
  }
}

// TODO: STOMP 연결 초기화
// onMounted(() => {
//   stompClient.value = connectStomp()
// })
</script>

<style scoped>
.chat-page {
  display: flex;
  height: calc(100vh - 32px);
  margin: 16px;
  border-radius: 12px;
  background: #fff;
  overflow: hidden;
  box-shadow: 0 0 6px rgba(0,0,0,0.1);
}

.chat-placeholder {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #888;
}
</style>