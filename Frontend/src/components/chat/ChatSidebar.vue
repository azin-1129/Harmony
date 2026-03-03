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
            <template v-for="(participant, idx) in getDisplayParticipants(room)" :key="participant.userId || participant.userIdentifier || `${room.chatRoomId}-${idx}`">
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

const normalizeParticipant = (participant) => {
  if (!participant) {
    return {
      userId: null,
      userIdentifier: '',
      nickname: '',
      profileImageUrl: ''
    }
  }

  const nestedUser = participant.user || {}

  return {
    ...participant,
    userId:
      participant.userId ??
      participant.id ??
      participant.participantUserId ??
      participant.memberUserId ??
      participant.senderId ??
      nestedUser.userId ??
      nestedUser.id ??
      null,
    userIdentifier:
      participant.userIdentifier ??
      participant.friendIdentifier ??
      participant.participantIdentifier ??
      nestedUser.userIdentifier ??
      '',
    nickname:
      participant.nickname ??
      participant.userNickname ??
      participant.friendNickname ??
      nestedUser.nickname ??
      '',
    profileImageUrl:
      participant.profileImageUrl ??
      participant.profileImage ??
      participant.userProfileImageUrl ??
      participant.friendProfileImageUrl ??
      nestedUser.profileImageUrl ??
      ''
  }
}

const getRoomParticipants = (room) => {
  const participants = Array.isArray(room?.chatRoomParticipants) ? room.chatRoomParticipants : []
  return participants.map(normalizeParticipant)
}

const isSameUser = (participant) => {
  if (!participant || !currentUser.value) return false

  const normalized = normalizeParticipant(participant)

  const currentUserId = currentUser.value.userId
  const currentIdentifier = currentUser.value.userIdentifier
  const currentNickname = currentUser.value.nickname

  const participantUserId = normalized.userId
  const participantIdentifier = normalized.userIdentifier
  const participantNickname = normalized.nickname

  if (currentUserId && participantUserId && Number(participantUserId) === Number(currentUserId)) {
    return true
  }

  if (currentIdentifier && participantIdentifier && participantIdentifier === currentIdentifier) {
    return true
  }

  if (currentNickname && participantNickname && participantNickname.trim() === currentNickname.trim()) {
    return true
  }

  return false
}

const getPersonalOpponent = (room) => {
  const participants = getRoomParticipants(room)
  return participants.find(participant => !isSameUser(participant)) || null
}

// 로그인한 사용자를 제외한 참가자 필터링
const getDisplayParticipants = (room) => {
  const participants = getRoomParticipants(room)
  if (participants.length === 0) return []

  if (room.chatRoomType === 'PERSONAL_CHATROOM') {
    const opponent = getPersonalOpponent(room)
    return opponent ? [opponent] : []
  }

  return participants
    .filter(participant => !isSameUser(participant))
    .slice(0, 4)
}

// 채팅방 이름 생성 (참가자 닉네임을 쉼표로 구분)
const getRoomName = (room) => {
  if (room.chatRoomType === 'PERSONAL_CHATROOM') {
    const opponent = getPersonalOpponent(room)
    return opponent?.nickname || '탈퇴한 사용자'
  }

  const participants = getDisplayParticipants(room)
  
  // 참가자가 나밖에 없을 때 (상대방 탈퇴)
  if (participants.length === 0) {
    return '탈퇴한 사용자'
  }
  
  const names = participants.map(p => p.nickname || p.userIdentifier || '알 수 없는 사용자').join(', ')
  const totalCount = Number(room.chatRoomCount || 0)
  const remaining = Math.max(totalCount - 1 - participants.length, 0)
  
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