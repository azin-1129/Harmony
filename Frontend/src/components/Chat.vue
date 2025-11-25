<template>
  <div class="chat-page">
    <!-- 좌측 친구 목록 -->
    <ChatSidebar
      :friends="friends"
      :selectedFriend="selectedFriend"
      @select-friend="selectFriend"
    />
    
    <!-- 우측 채팅창 -->
    <ChatWindow
      v-if="selectedFriend"
      :selectedFriend="selectedFriend"
      :stompClient="stompClient"
      :myId="myId"
      :messages="messages"
      @message-sent="addMessage"
    />
    
    <!-- 선택된 친구 없을 때 안내 문구 -->
    <div v-else class="chat-placeholder">
      채팅할 친구를 선택하세요
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import ChatSidebar from '@/components/chat/ChatSidebar.vue'
import ChatWindow from '@/components/chat/ChatWindow.vue'

const friends = ref([
  { id: 1, name: 'hana', profileImage: 'https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e?ixlib=rb-4.0.3&auto=format&fit=crop&w=36&q=80' },
  { id: 2, name: 'minho', profileImage: 'https://images.unsplash.com/photo-1531123897727-8f129e1688ce?ixlib=rb-4.0.3&auto=format&fit=crop&w=36&q=80' }
])

const selectedFriend = ref(null)
const myId = ref(99)
const messages = ref([])

// 선택된 친구 변경
const selectFriend = (friend) => {
  selectedFriend.value = friend
  messages.value = [
    { id: 1, senderId: friend.id, content: "안녕하세요!", timestamp: new Date().toISOString(), senderProfileImage: friend.profileImage },
    { id: 2, senderId: myId.value, content: "안녕하세요 반가워요!", timestamp: new Date().toISOString(), senderProfileImage: '' }
  ]
}

// 새 메시지 추가
const addMessage = (msg) => {
  messages.value.push(msg)
}

// STOMP는 아직 더미 상태
const stompClient = null
</script>

<style scoped>
.chat-page {
  display: flex;
  height: 80vh;          /* 충분히 높이 확보 */
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