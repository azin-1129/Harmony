<template>
  <div class="chat-sidebar">
    <h2>채팅방 목록</h2>
    <div v-if="loading" class="loading">로딩 중...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <ul v-else>
      <li
        v-for="room in chatRooms"
        :key="room.chatRoomId"
        @click="$emit('select-room', room)"
        :class="{ active: selectedRoomId === room.chatRoomId }"
      >
        <div class="room-avatar">
          <template v-if="getDisplayParticipants(room).length > 0">
            <template v-for="(participant, idx) in getDisplayParticipants(room)" :key="participant.userId">
              <div
                v-if="!participant.profileImageUrl"
                class="avatar default-profile"
                :style="getAvatarStyle(idx, getDisplayParticipants(room).length)"
              >
                👤
              </div>
              <img
                v-else
                :src="participant.profileImageUrl"
                :alt="participant.nickname"
                class="avatar"
                :style="getAvatarStyle(idx, getDisplayParticipants(room).length)"
              />
            </template>
          </template>
          <div v-else class="avatar default-profile">
            👤
          </div>
        </div>
        <span class="room-name">{{ getRoomName(room) }}</span>
      </li>
    </ul>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useCurrentUser } from '@/composables/useCurrentUser'
import { useChatRooms } from '@/composables/useChatRooms'

const props = defineProps({
  selectedRoomId: Number
})

const emit = defineEmits(['select-room'])

const { currentUser, loadCurrentUser } = useCurrentUser()
const { chatRooms, isLoading, error: chatRoomsError, loadChatRooms } = useChatRooms()
const loading = isLoading
const error = chatRoomsError

onMounted(async () => {
  await loadCurrentUser()
  await loadChatRooms()
})

// 로그인한 사용자를 제외한 참가자 필터링
const getDisplayParticipants = (room) => {
  if (!room.chatRoomParticipants) return []
  return room.chatRoomParticipants.filter(
    p => p.userId !== currentUser.value.userId
  ).slice(0, 4) // 최대 4명까지만 표시
}

// 채팅방 이름 생성 (참가자 닉네임을 쉼표로 구분)
const getRoomName = (room) => {
  const participants = getDisplayParticipants(room)
  
  // 참가자가 나밖에 없을 때 (상대방 탈퇴)
  if (participants.length === 0) {
    return '탈퇴한 사용자'
  }
  
  const names = participants.map(p => p.nickname).join(', ')
  const remaining = room.chatRoomCount - 1 - participants.length
  
  if (remaining > 0) {
    return `${names} 외 ${remaining}명`
  }
  return names
}

// 아바타 겹치기 스타일
const getAvatarStyle = (index, total) => {
  if (total === 1) return {}
  
  const offset = index * 18 // 18px씩 겹치기
  return {
    position: 'absolute',
    left: `${offset}px`,
    zIndex: total - index
  }
}
</script>

<style scoped>
.chat-sidebar {
  width: 30%;
  border-right: 1px solid #ddd;
  overflow-y: auto;
  background: #fafafa;
  padding: 16px 0;
}

h2 {
  padding: 0 16px;
  margin: 0 0 16px 0;
  font-size: 18px;
}

.loading, .error {
  padding: 16px;
  text-align: center;
  color: #888;
}

.error {
  color: #d32f2f;
}

ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

li {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.2s;
  gap: 12px;
}

li:hover {
  background: #f0f0f0;
}

li.active {
  font-weight: bold;
  background: #e0e0ff;
}

.room-avatar {
  position: relative;
  min-width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #fff;
  background: #fff;
}

.avatar.default-profile {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2em;
  color: #999;
  background-color: #e0e0e0;
}

.room-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>