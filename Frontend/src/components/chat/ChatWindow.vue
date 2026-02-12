<template>
  <div class="chat-window">
    <div class="chat-header">
      <div class="header-avatar">
        <template v-if="displayParticipants.length > 0">
          <template v-for="(participant, idx) in displayParticipants" :key="participant.userId">
            <div
              v-if="!participant.profileImageUrl"
              class="header-avatar-img default-profile"
              :style="getAvatarStyle(idx, displayParticipants.length)"
            >
              👤
            </div>
            <img
              v-else
              :src="participant.profileImageUrl"
              :alt="participant.nickname"
              class="header-avatar-img"
              :style="getAvatarStyle(idx, displayParticipants.length)"
            />
          </template>
        </template>
        <div
          v-else
          class="header-avatar-img default-profile"
        >
          👤
        </div>
      </div>
      <span class="room-title">{{ roomTitle }}</span>
      <button 
        v-if="isUserLeft" 
        class="leave-btn"
        @click="leaveChatRoom"
        title="채팅방 나가기"
      >
        나가기
      </button>
    </div>

    <div class="messages" ref="messagesContainer" @scroll="handleScroll">
      <div v-if="loadingMore" class="loading-more">이전 메시지 로딩 중...</div>
      
      <div v-if="messages.length === 0 && !loading" class="no-messages">
        메시지가 없습니다. 첫 메시지를 보내보세요!
      </div>

      <div
        v-for="msg in messages"
        :key="msg.chatMessageId"
        :class="['message-wrapper', msg.senderId === myId ? 'my-msg' : 'friend-msg']"
      >
        <!-- 친구 프로필 사진 -->
        <template v-if="msg.senderId !== myId">
          <div
            v-if="!getParticipantProfile(msg.senderId)"
            class="msg-avatar default-profile"
          >
            👤
          </div>
          <img
            v-else
            :src="getParticipantProfile(msg.senderId)"
            alt="프로필"
            class="msg-avatar"
          />
        </template>

        <!-- 말풍선 + 시간 wrapper -->
        <div class="bubble-time-wrapper">
          <template v-if="msg.senderId === myId">
            <!-- 내 메시지: 시간 왼쪽, 버블 오른쪽 -->
            <span class="message-time">{{ formatTime(msg.createdAt) }}</span>
            <div class="message-bubble">
              <span class="message-text">{{ msg.chatMessage }}</span>
            </div>
          </template>
          <template v-else>
            <!-- 친구 메시지: 버블 왼쪽, 시간 오른쪽 -->
            <div class="message-bubble">
              <span class="message-text">{{ msg.chatMessage }}</span>
            </div>
            <span class="message-time">{{ formatTime(msg.createdAt) }}</span>
          </template>
        </div>
      </div>
    </div>

    <form @submit.prevent="sendMessage" class="chat-input">
      <input 
        v-model="newMessage" 
        placeholder="메시지를 입력..." 
        :disabled="isUserLeft"
      />
      <button type="submit" :disabled="isUserLeft">전송</button>
    </form>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted, onBeforeUnmount } from 'vue'
import api from '@/services/api'
import { useCurrentUser } from '@/composables/useCurrentUser'
import stompService from '@/services/stompService'

const props = defineProps({
  selectedRoom: Object,
  stompConnected: Boolean
})

const emit = defineEmits(['message-sent', 'room-left'])

const { currentUser, loadCurrentUser } = useCurrentUser()
const messages = ref([])
const newMessage = ref('')
const loading = ref(false)
const loadingMore = ref(false)
const hasNext = ref(false)
const lastContentId = ref(null)
const messagesContainer = ref(null)
const isInitialLoad = ref(true)
const subscriptionRef = ref(null)

const myId = computed(() => currentUser.value.userId)

const displayParticipants = computed(() => {
  if (!props.selectedRoom) return []
  return props.selectedRoom.chatRoomParticipants
    .filter(p => p.userId !== myId.value)
    .slice(0, 4)
})

const isUserLeft = computed(() => {
  if (!props.selectedRoom) return false
  // PERSONAL_CHATROOM 타입이고, 나 혼자만 남은 경우
  if (props.selectedRoom.chatRoomType === 'PERSONAL_CHATROOM') {
    return displayParticipants.value.length === 0
  }
  return false
})

const roomTitle = computed(() => {
  if (!props.selectedRoom) return ''
  
  const participants = props.selectedRoom.chatRoomParticipants.filter(
    p => p.userId !== myId.value
  )
  
  // 참가자가 나밖에 없을 때 (상대방 탈퇴)
  if (participants.length === 0) {
    return '탈퇴한 사용자'
  }
  
  const names = participants.map(p => p.nickname).join(', ')
  const remaining = props.selectedRoom.chatRoomCount - 1 - participants.length
  
  if (remaining > 0) {
    return `${names} 외 ${remaining}명`
  }
  return names
})

onMounted(async () => {
  await loadCurrentUser()
  // ChatWindow 마운트 되면서 selectedRoom이 있으면 메시지 로드
  if (props.selectedRoom?.chatRoomId) {
    console.log('[ChatWindow] onMounted: loading messages for room:', props.selectedRoom.chatRoomId)
    await loadMessages(true)
    await subscribeToRoom(props.selectedRoom.chatRoomId)
  }
})

onBeforeUnmount(() => {
  unsubscribeFromRoom()
})

// 채팅방 변경 시 메시지 로드
watch(
  () => props.selectedRoom?.chatRoomId,
  async (newRoomId, oldRoomId) => {
    // 방이 변경될 때만 로드 (초기값은 onMounted에서 처리)
    if (newRoomId && newRoomId !== oldRoomId) {
      console.log('[ChatWindow] Room changed:', oldRoomId, '->', newRoomId, '- loading messages')
      await loadMessages(true)
      await subscribeToRoom(newRoomId)
    }
  }
)

watch(
  () => props.stompConnected,
  async (connected) => {
    if (connected && props.selectedRoom?.chatRoomId) {
      await subscribeToRoom(props.selectedRoom.chatRoomId)
    }
  }
)

const unsubscribeFromRoom = () => {
  if (subscriptionRef.value) {
    subscriptionRef.value.unsubscribe()
    subscriptionRef.value = null
  }
}

const subscribeToRoom = async (roomId) => {
  if (!roomId) return
  if (!stompService.isConnected()) return

  unsubscribeFromRoom()

  const headers = {}
  const destination = `/sub/${roomId}`

  subscriptionRef.value = stompService.subscribe(destination, async (body) => {
    if (!body) return
    const normalized = {
      ...body,
      createdAt: body.createdAt || new Date().toISOString()
    }
    messages.value.push(normalized)
    await nextTick()
    scrollToBottom()
  }, headers)
}

const loadMessages = async (isInitial = false) => {
  if (!props.selectedRoom) return
  
  if (isInitial) {
    loading.value = true
    messages.value = []
    lastContentId.value = null
    hasNext.value = false
    isInitialLoad.value = true
  } else {
    loadingMore.value = true
  }

  try {
    const response = await api.getChatMessages(
      props.selectedRoom.chatRoomId,
      lastContentId.value
    )
    
    const data = response.data.data
    const newMessages = data.content.reverse() // 시간순으로 정렬 (desc로 왔으므로 reverse)
    
    if (isInitial) {
      messages.value = newMessages
      await nextTick()
      scrollToBottom()
    } else {
      // 이전 메시지 추가 (위에 추가)
      const oldScrollHeight = messagesContainer.value.scrollHeight
      messages.value = [...newMessages, ...messages.value]
      await nextTick()
      // 스크롤 위치 유지
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight - oldScrollHeight
    }
    
    hasNext.value = data.hasNext
    lastContentId.value = data.lastContentId
    isInitialLoad.value = false
  } catch (err) {
    console.error('Failed to load messages:', err)
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

const handleScroll = () => {
  if (!messagesContainer.value) return
  
  // 스크롤이 최상단에 도달하고, 더 로드할 메시지가 있으면
  if (messagesContainer.value.scrollTop === 0 && hasNext.value && !loadingMore.value) {
    loadMessages(false)
  }
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const leaveChatRoom = async () => {
  if (!props.selectedRoom) return
  
  if (!confirm('이 채팅방에서 나가시겠습니까?')) return
  
  try {
    await api.leaveChatRoom(props.selectedRoom.chatRoomId)
    emit('room-left', props.selectedRoom.chatRoomId)
  } catch (err) {
    console.error('Failed to leave chat room:', err)
    alert('채팅방 나가기에 실패했습니다.')
  }
}

const sendMessage = () => {
  if (!newMessage.value.trim() || isUserLeft.value) return

  if (!props.selectedRoom?.chatRoomId) return
  if (!stompService.isConnected()) {
    alert('채팅 서버에 연결되지 않았습니다. 잠시 후 다시 시도해 주세요.')
    return
  }

  const destination = `/pub/${props.selectedRoom.chatRoomId}`
  const payload = {
    chatMessageType: 'SEND',
    chatMessage: newMessage.value.trim()
  }

  stompService.send(destination, payload)
  emit('message-sent', payload)
  newMessage.value = ''
}

const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${hours}:${minutes}`
}

const getParticipantProfile = (userId) => {
  if (!props.selectedRoom) return null
  
  const participant = props.selectedRoom.chatRoomParticipants.find(
    p => p.userId === userId
  )
  
  return participant?.profileImageUrl || null
}

const getAvatarStyle = (index, total) => {
  if (total === 1) return {}
  
  const offset = index * 14
  return {
    position: 'absolute',
    left: `${offset}px`,
    zIndex: total - index
  }
}
</script>

<style scoped>
.chat-window {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid #ddd;
  background: #fafafa;
}

.leave-btn {
  margin-left: auto;
  padding: 6px 16px;
  background-color: #ff4444;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: background-color 0.2s;
}

.leave-btn:hover {
  background-color: #cc0000;
}

.header-avatar {
  position: relative;
  min-width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
}

.header-avatar-img {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #fff;
  background: #fff;
}

.header-avatar-img.default-profile {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2em;
  color: #999;
  background-color: #e0e0e0;
}

.room-title {
  font-weight: bold;
  font-size: 16px;
}

.messages {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 12px;
  overflow-y: auto;
  gap: 8px;
}

.loading-more {
  text-align: center;
  padding: 8px;
  color: #888;
  font-size: 14px;
}

.no-messages {
  text-align: center;
  padding: 40px 20px;
  color: #888;
}

.message-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 6px;
  width: 100%;
}

.my-msg {
  justify-content: flex-end;
}

.my-msg .bubble-time-wrapper {
  justify-content: flex-end;
  width: 100%;
}

.friend-msg {
  justify-content: flex-start;
}

.bubble-time-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 4px;
}

.message-bubble {
  max-width: 70%;
  padding: 8px 12px;
  border-radius: 12px;
  background: #eee;
}

.my-msg .message-time {
  margin-right: 6px;
}

.my-msg .message-bubble {
  background: #007aff;
  color: white;
}

.message-text {
  word-break: break-word;
  line-height: 1.4;
}

.message-time {
  font-size: 0.7rem;
  color: #666;
  flex-shrink: 0;
}

.msg-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.msg-avatar.default-profile {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2em;
  color: #999;
  background-color: #e0e0e0;
}

.chat-input {
  display: flex;
  border-top: 1px solid #ddd;
}

.chat-input input {
  flex: 1;
  padding: 12px;
  border: none;
  outline: none;
  font-size: 14px;
}

.chat-input button {
  padding: 12px 20px;
  background: #007aff;
  color: white;
  border: none;
  cursor: pointer;
  font-weight: bold;
  transition: background 0.2s;
}

.chat-input input:disabled {
  background-color: #f5f5f5;
  color: #999;
  cursor: not-allowed;
}

.chat-input button:hover {
  background: #0056b3;
}

.chat-input button:disabled {
  background: #ccc;
  cursor: not-allowed;
}
</style>
