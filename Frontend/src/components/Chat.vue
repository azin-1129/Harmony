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
      :stompClient="stompClient"
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
import { ref } from 'vue'
import ChatSidebar from '@/components/chat/ChatSidebar.vue'
import ChatWindow from '@/components/chat/ChatWindow.vue'

const selectedRoom = ref(null)
const stompClient = ref(null) // TODO: STOMP 연결 구현
const chatSidebarKey = ref(0)

// 채팅방 선택
const selectRoom = (room) => {
  selectedRoom.value = room
}

// 메시지 전송 처리
const handleMessageSent = (message) => {
  console.log('Message sent:', message)
  // TODO: STOMP를 통한 실제 메시지 전송 구현
}

// 채팅방 나가기 처리
const handleRoomLeft = (roomId) => {
  console.log('Left room:', roomId)
  selectedRoom.value = null
  chatSidebarKey.value++ // ChatSidebar 새로고침
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