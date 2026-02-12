import axios from 'axios'
import { getAccessToken } from '@/utils/tokenService'
import { BACKEND_URL } from '@/utils/config'

const api = axios.create({
  baseURL: BACKEND_URL,
  withCredentials: true
})

// 요청 인터셉터: 모든 요청에 토큰 추가
api.interceptors.request.use(
  (config) => {
    const token = getAccessToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

export default {
// Auth
login(payload) { return api.post('/auth/login', payload) },
logout() { return api.post('/auth/logout') },
me() { return api.get('/auth/me') },


// Feed
getFeed(page = 0, size = 20) { return api.get('/posts', { params: { page, size } }) },
createPost(payload) { return api.post('/posts', payload) },


// Likes / comments skeleton
likePost(postId) { return api.post(`/posts/${postId}/like`) },
commentPost(postId, payload) { return api.post(`/posts/${postId}/comments`, payload) },


// Chat REST endpoints
getParticipatedChatRooms() { return api.get('/participate/participated') },
getChatMessages(chatRoomId, chatMessageId = null, size = 15) { 
  const params = { size, sort: 'chatMessageId,desc' }
  if (chatMessageId) {
    params.chatMessageId = chatMessageId
  }
  return api.get(`/chat/${chatRoomId}`, { params }) 
},
leaveChatRoom(chatRoomId) { 
  return api.delete('/participate/leave', { data: { chatRoomId } }) 
},

// Friendship (친구)
getFriendsList() { return api.get('/friendship/friendships') },
deleteFriend(friendIdentifier) { 
  return api.delete('/friendship', { 
    data: { friendIdentifier } 
  }) 
},

// Friendship Requests (친구 요청)
getSentFriendRequests() { return api.get('/friendship-request/sent') },
getReceivedFriendRequests() { return api.get('/friendship-request/received') },
acceptFriendRequest(senderIdentifier) { 
  return api.post('/friendship-request/accept', { senderIdentifier }) 
},
cancelFriendRequest(receiverIdentifier) { 
  return api.post('/friendship-request/cancel', { receiverIdentifier }) 
},
rejectFriendRequest(senderIdentifier) { 
  return api.post('/friendship-request/reject', { senderIdentifier }) 
},
sendFriendRequest(receiverIdentifier) {
  return api.post('/friendship-request/send', { receiverIdentifier })
},

// Personal Chatroom
createPersonalChatRoom(partnerIdentifier) {
  return api.post('/participate/init/personal', { partnerIdentifier })
},

// User Settings (사용자 설정)
updatePassword(payload) {
  return api.post('/user/update/password', payload)
},
updateNickname(payload) {
  return api.post('/user/update/nickname', payload)
},
uploadProfileImage(formData) {
  return api.post('/media', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
},
deleteProfileImage() {
  return api.delete('/media')
}
}