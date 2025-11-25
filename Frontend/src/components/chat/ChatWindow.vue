<template>
  <div class="chat-window">
    <div class="messages">
      <div
        v-for="msg in messages"
        :key="msg.id"
        :class="['message-wrapper', msg.senderId === myId ? 'my-msg' : 'friend-msg']"
      >
        <!-- 친구 프로필 사진 -->
        <img
          v-if="msg.senderId !== myId"
          :src="msg.senderProfileImage"
          alt="프로필"
          class="msg-avatar"
        />

        <!-- 말풍선 + 시간 wrapper -->
        <div class="bubble-time-wrapper">
          <template v-if="msg.senderId === myId">
            <!-- 내 메시지: 시간 왼쪽, 버블 오른쪽 -->
            <span class="message-time">{{ formatTime(msg.timestamp) }}</span>
            <div class="message-bubble">
              <span class="message-text">{{ msg.content }}</span>
            </div>
          </template>
          <template v-else>
            <!-- 친구 메시지: 버블 왼쪽, 시간 오른쪽 -->
            <div class="message-bubble">
              <span class="message-text">{{ msg.content }}</span>
            </div>
            <span class="message-time">{{ formatTime(msg.timestamp) }}</span>
          </template>
        </div>
      </div>
    </div>

    <form @submit.prevent="sendMessage" class="chat-input">
      <input v-model="newMessage" placeholder="메시지를 입력..." />
      <button type="submit">전송</button>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  selectedFriend: Object,
  messages: Array,
  myId: Number,
  stompClient: null
})

const emit = defineEmits(['message-sent'])
const newMessage = ref('')

const sendMessage = () => {
  if (!newMessage.value.trim()) return
  const msg = {
    id: Date.now(),
    senderId: props.myId,
    content: newMessage.value,
    timestamp: new Date().toISOString(),
    senderProfileImage: ''
  }
  emit('message-sent', msg)
  newMessage.value = ''
}

const formatTime = (ts) => {
  if (!ts) return ''
  const d = new Date(ts)
  return `${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}
</script>

<style scoped>
.chat-window {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.messages {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 12px;
  overflow-y: auto;
  gap: 6px;
}

.message-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 6px;
}

.my-msg {
  justify-content: flex-end;
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
  padding: 6px 12px;
  border-radius: 12px;
  background: #eee;
}

.my-msg .message-bubble {
  background: #007aff;
  color: white;
}

.message-text {
  word-break: break-word;
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
}

.chat-input {
  display: flex;
  border-top: 1px solid #ddd;
}

.chat-input input {
  flex: 1;
  padding: 8px;
  border: none;
  outline: none;
}

.chat-input button {
  padding: 8px 12px;
  background: #007aff;
  color: white;
  border: none;
  cursor: pointer;
}
</style>
