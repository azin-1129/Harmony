import axios from 'axios'


const api = axios.create({
baseURL: import.meta.env.VITE_API_BASE || '/api',
withCredentials: true // 필요하면 사용
})


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


// Chat REST endpoints (if any)
getChatRooms() { return api.get('/chats/rooms') },
getMessages(roomId, page = 0) { return api.get(`/chats/${roomId}/messages`, { params: { page } }) }
}