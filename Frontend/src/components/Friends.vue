<template>
  <div class="friends-container">
    <h1>친구 관리</h1>

    <!-- 친구 목록 섹션 -->
    <section class="section">
      <h2>친구 목록</h2>
      <div v-if="loadingFriends" class="loading">로딩 중...</div>
      <div v-else-if="friends.length === 0" class="empty">친구가 없습니다.</div>
      <div v-else class="friends-list">
        <div 
          v-for="friend in friends" 
          :key="friend.friendIdentifier"
          class="friend-item"
          @click="goToProfile(friend.friendIdentifier)"
          @mouseenter="hoveredFriend = friend.friendIdentifier"
          @mouseleave="hoveredFriend = null"
        >
          <div class="friend-info">
            <div v-if="!friend.profileImageUrl" class="profile-image default-profile">
              👤
            </div>
            <img 
              v-else
              :src="friend.profileImageUrl" 
              :alt="friend.nickname"
              class="profile-image"
              @error="(e) => e.target.style.display = 'none'"
            />
            <span class="nickname">{{ friend.nickname }}</span>
          </div>
          <button 
            v-if="hoveredFriend === friend.friendIdentifier"
            class="delete-btn"
            @click.stop="confirmDeleteFriend(friend)"
          >
            ✕
          </button>
        </div>
      </div>
    </section>

    <!-- 친구 요청 섹션 -->
    <section class="section">
      <h2>친구 요청</h2>
      
      <!-- 받은 친구 요청 -->
      <div class="subsection">
        <h3>받은 요청</h3>
        <div v-if="loadingReceivedRequests" class="loading">로딩 중...</div>
        <div v-else-if="receivedRequests.length === 0" class="empty">받은 요청이 없습니다.</div>
        <div v-else class="request-list">
          <div 
            v-for="request in receivedRequests" 
            :key="request.friendshipRequestSenderIdentifier"
            class="request-item"
          >
            <span class="identifier">{{ request.friendshipRequestSenderIdentifier }}</span>
            <div class="button-group">
              <button 
                class="accept-btn"
                @click="acceptRequest(request.friendshipRequestSenderIdentifier)"
              >
                수락
              </button>
              <button 
                class="reject-btn"
                @click="rejectRequest(request.friendshipRequestSenderIdentifier)"
              >
                거절
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 보낸 친구 요청 -->
      <div class="subsection">
        <h3>보낸 요청</h3>
        <div v-if="loadingSentRequests" class="loading">로딩 중...</div>
        <div v-else-if="sentRequests.length === 0" class="empty">보낸 요청이 없습니다.</div>
        <div v-else class="request-list">
          <div 
            v-for="request in sentRequests" 
            :key="request.friendshipRequestReceiverIdentifier"
            class="request-item"
          >
            <span class="identifier">{{ request.friendshipRequestReceiverIdentifier }}</span>
            <button 
              class="cancel-btn"
              @click="cancelRequest(request.friendshipRequestReceiverIdentifier)"
            >
              취소
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- 삭제 확인 팝업 -->
    <div v-if="showDeleteConfirm" class="modal-overlay" @click="showDeleteConfirm = false">
      <div class="modal" @click.stop>
        <p>이 친구를 삭제할까요?</p>
        <div class="modal-buttons">
          <button class="confirm-btn" @click="deleteFriend">예</button>
          <button class="cancel-btn-modal" @click="showDeleteConfirm = false">아니오</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/services/api'

const router = useRouter()

// 친구 목록 관련
const friends = ref([])
const loadingFriends = ref(true)
const hoveredFriend = ref(null)

// 친구 요청 관련
const sentRequests = ref([])
const receivedRequests = ref([])
const loadingSentRequests = ref(true)
const loadingReceivedRequests = ref(true)

// 삭제 확인 팝업
const showDeleteConfirm = ref(false)
const friendToDelete = ref(null)

// 친구 목록 조회
const loadFriends = async () => {
  try {
    loadingFriends.value = true
    const response = await api.getFriendsList()
    if (response.data.code === 'FRIENDSHIP_READ_SUCCESS') {
      friends.value = response.data.data || []
    }
  } catch (error) {
    console.error('친구 목록 조회 실패:', error)
    friends.value = []
  } finally {
    loadingFriends.value = false
  }
}

// 보낸 친구 요청 조회
const loadSentRequests = async () => {
  try {
    loadingSentRequests.value = true
    const response = await api.getSentFriendRequests()
    if (response.data.code === 'SENT_FRIENDSHIP_REQUESTS_READ_SUCCESS') {
      sentRequests.value = response.data.data || []
    }
  } catch (error) {
    console.error('보낸 친구 요청 조회 실패:', error)
    sentRequests.value = []
  } finally {
    loadingSentRequests.value = false
  }
}

// 받은 친구 요청 조회
const loadReceivedRequests = async () => {
  try {
    loadingReceivedRequests.value = true
    const response = await api.getReceivedFriendRequests()
    if (response.data.code === 'RECEIVED_FRIENDSHIP_REQUESTS_READ_SUCCESS') {
      receivedRequests.value = response.data.data || []
    }
  } catch (error) {
    console.error('받은 친구 요청 조회 실패:', error)
    receivedRequests.value = []
  } finally {
    loadingReceivedRequests.value = false
  }
}

// 프로필 페이지로 이동
const goToProfile = (identifier) => {
  router.push(`/profile/${identifier}`)
}

// 친구 삭제 확인
const confirmDeleteFriend = (friend) => {
  friendToDelete.value = friend
  showDeleteConfirm.value = true
}

// 친구 삭제
const deleteFriend = async () => {
  try {
    const response = await api.deleteFriend(friendToDelete.value.friendIdentifier)
    if (response.data.code === 'FRIENDSHIP_DELETE_SUCCESS') {
      alert('친구를 삭제했습니다.')
      friends.value = friends.value.filter(
        f => f.friendIdentifier !== friendToDelete.value.friendIdentifier
      )
    }
  } catch (error) {
    console.error('친구 삭제 실패:', error)
    if (error.response?.data?.code === 'FRIENDSHIP_NOT_FOUND') {
      alert('존재하지 않는 친구입니다.')
    } else {
      alert('친구 삭제에 실패했습니다.')
    }
  } finally {
    showDeleteConfirm.value = false
    friendToDelete.value = null
  }
}

// 보낸 친구 요청 취소
const cancelRequest = async (receiverIdentifier) => {
  try {
    const response = await api.cancelFriendRequest(receiverIdentifier)
    if (response.data.code === 'FRIENDSHIP_REQUEST_CANCEL_SUCCESS') {
      alert('친구 요청을 취소했습니다.')
      sentRequests.value = sentRequests.value.filter(
        r => r.friendshipRequestReceiverIdentifier !== receiverIdentifier
      )
    }
  } catch (error) {
    console.error('친구 요청 취소 실패:', error)
    alert('친구 요청 취소에 실패했습니다.')
  }
}

// 받은 친구 요청 수락
const acceptRequest = async (senderIdentifier) => {
  try {
    const response = await api.acceptFriendRequest(senderIdentifier)
    if (response.data.code === 'FRIENDSHIP_REQUEST_ACCEPT_SUCCESS') {
      alert('친구 요청을 수락했습니다.')
      receivedRequests.value = receivedRequests.value.filter(
        r => r.friendshipRequestSenderIdentifier !== senderIdentifier
      )
      // 친구 목록 다시 로드
      loadFriends()
    }
  } catch (error) {
    console.error('친구 요청 수락 실패:', error)
    alert('친구 요청 수락에 실패했습니다.')
  }
}

// 받은 친구 요청 거절
const rejectRequest = async (senderIdentifier) => {
  try {
    const response = await api.rejectFriendRequest(senderIdentifier)
    if (response.data.code === 'FRIENDSHIP_REQUEST_REJECT_SUCCESS') {
      alert('친구 요청을 거절했습니다.')
      receivedRequests.value = receivedRequests.value.filter(
        r => r.friendshipRequestSenderIdentifier !== senderIdentifier
      )
    }
  } catch (error) {
    console.error('친구 요청 거절 실패:', error)
    alert('친구 요청 거절에 실패했습니다.')
  }
}

onMounted(() => {
  loadFriends()
  loadSentRequests()
  loadReceivedRequests()
})
</script>

<style scoped>
.friends-container {
  padding: 24px;
  max-width: 800px;
  margin: 0 auto;
}

h1 {
  margin-bottom: 32px;
  font-size: 1.8em;
}

.section {
  margin-bottom: 48px;
}

.section h2 {
  font-size: 1.4em;
  margin-bottom: 16px;
  color: #333;
}

.subsection {
  margin-top: 24px;
}

.subsection h3 {
  font-size: 1.1em;
  margin-bottom: 12px;
  color: #555;
}

.loading,
.empty {
  padding: 20px;
  text-align: center;
  color: #999;
}

/* 친구 목록 스타일 */
.friends-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.friend-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s, border-color 0.2s;
}

.friend-item:hover {
  background-color: #f5f5f5;
  border-color: #ccc;
}

.friend-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.profile-image {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  background-color: #e0e0e0;
}

.default-profile {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5em;
  color: #999;
  flex-shrink: 0;
}

.nickname {
  font-weight: 500;
  font-size: 1em;
}

.delete-btn {
  background: none;
  border: none;
  font-size: 1.5em;
  color: #999;
  cursor: pointer;
  padding: 4px 8px;
  transition: color 0.2s;
}

.delete-btn:hover {
  color: #ff4444;
}

/* 친구 요청 목록 스타일 */
.request-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.request-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background-color: #fafafa;
}

.identifier {
  font-weight: 500;
  color: #333;
}

.button-group {
  display: flex;
  gap: 8px;
}

.accept-btn,
.reject-btn,
.cancel-btn {
  padding: 6px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.2s;
}

.accept-btn {
  background-color: #4caf50;
  color: white;
}

.accept-btn:hover {
  background-color: #45a049;
}

.reject-btn {
  background-color: #ff4444;
  color: white;
}

.reject-btn:hover {
  background-color: #cc0000;
}

.cancel-btn {
  background-color: #888;
  color: white;
}

.cancel-btn:hover {
  background-color: #666;
}

/* 모달 스타일 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  min-width: 300px;
}

.modal p {
  margin-bottom: 20px;
  font-size: 1.1em;
  text-align: center;
}

.modal-buttons {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.confirm-btn,
.cancel-btn-modal {
  padding: 8px 24px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: background-color 0.2s;
}

.confirm-btn {
  background-color: #6b6bff;
  color: white;
}

.confirm-btn:hover {
  background-color: #5555ff;
}

.cancel-btn-modal {
  background-color: #e0e0e0;
  color: #333;
}

.cancel-btn-modal:hover {
  background-color: #d0d0d0;
}
</style>
