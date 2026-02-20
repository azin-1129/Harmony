<template>
  <div class="feed-item">
    <div class="feed-header">
        <div class="header-left">
          <img :src="getProfileImageUrl(feed.author.profileImage)" class="profile" />
          <span class="username">{{ feed.author.name }}</span>
        </div>
        <button 
          class="add-friend"
          :class="{ disabled: !isFriendAddable }"
          :disabled="!isFriendAddable"
          @click.prevent="onAddFriend"
        >
          {{ isFriendAddable ? '친구 추가' : '친구' }}
        </button>
      </div>

    <div class="feed-content">
      <img v-if="feed.imageUrl" :src="feed.imageUrl" class="feed-img" />
      <video v-else-if="feed.videoUrl" :src="feed.videoUrl" controls class="feed-video"></video>
      <div v-if="feed.text" class="feed-text-section">
        <strong class="author-bold">{{ feed.author.name }}</strong>
        <span class="text-content">{{ feed.text }}</span>
      </div>
    </div>

    <div class="feed-actions">
      <button class="like-btn" @click="toggleLike">
        <span v-if="feed.liked">💖</span>
        <span v-else>🤍</span>
        {{ feed.likes }}
      </button>
      <button class="comment-btn" @click="toggleComments">💬 {{ feed.commentCount || 0 }}</button>
    </div>

    <CommentSection 
      v-if="showComments" 
      :feedId="feed.id" 
      :currentUserInfo="currentUserInfo"
      :currentUserIdentifier="currentUserIdentifier"
      @update-comment-count="updateCommentCount"
    />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import CommentSection from './CommentSection.vue'
import { apiFetch } from '@/utils/apiClient'

const props = defineProps({
  feed: Object,
  friendList: Array,
  currentUserIdentifier: String,
  currentUserInfo: Object
})

const emit = defineEmits(['add-friend'])

// 기본 프로필 사진 - 회색 배경에 사람 아이콘
const DEFAULT_PROFILE_IMAGE = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgdmlld0JveD0iMCAwIDEwMCAxMDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxyZWN0IHdpZHRoPSIxMDAiIGhlaWdodD0iMTAwIiBmaWxsPSIjRTBFMEUwIi8+CjxjaXJjbGUgY3g9IjUwIiBjeT0iMzUiIHI9IjE1IiBmaWxsPSIjOTk5Ii8+CjxwYXRoIGQ9Ik0yMCA2NSBDMjAgNjAgMzAgNTAgNTAgNTAgQzcwIDUwIDgwIDYwIDgwIDY1IiBmaWxsPSIjOTk5Ii8+Cjwvc3ZnPg=='

const showComments = ref(false)

// 프로필 이미지 URL 정규화 - 없으면 기본 이미지 사용
const getProfileImageUrl = (imageUrl) => {
  if (!imageUrl || imageUrl === null || (typeof imageUrl === 'string' && !imageUrl.trim())) {
    return DEFAULT_PROFILE_IMAGE
  }
  return imageUrl
}
const toggleComments = () => (showComments.value = !showComments.value)

const updateCommentCount = (count) => {
  props.feed.commentCount = count
  console.log('Comment count updated:', count)
}

const toggleLike = async () => {
  try {
    const res = await apiFetch(`/heart/${props.feed.id}`, { method: 'POST' })
    if (!res || !res.ok) {
      console.error('Failed to toggle like', res?.status)
      return
    }
    
    // 성공 시 상태 업데이트
    props.feed.likes += props.feed.liked ? -1 : 1
    props.feed.liked = !props.feed.liked
    console.log('Like toggled:', props.feed.id, 'liked:', props.feed.liked)
  } catch (err) {
    console.error('Error toggling like', err)
  }
}

// 친구 추가 가능 여부 판단
// authorIdentifier가 friendList에 없으면 클릭 가능
const isFriendAddable = computed(() => {
  if (!props.friendList || !Array.isArray(props.friendList)) return true
  const authorId = props.feed.author.name
  const isFriend = props.friendList.some(f => f.friendIdentifier === authorId)
  return !isFriend
})

const onAddFriend = () => {
  if (!isFriendAddable.value) {
    console.log('이미 친구입니다:', props.feed.author.name)
    return
  }
  
  const receiverIdentifier = props.feed.author.name
  console.log('친구 추가 요청:', receiverIdentifier)
  emit('add-friend', receiverIdentifier)
}
</script>

<style scoped>
.feed-item {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 20px;
  box-shadow: 0 0 6px rgba(0,0,0,0.08);
}
.feed-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}
.header-left {
  display: flex;
  align-items: center;
}
.profile {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  margin-right: 8px;
}
.username {
  font-weight: bold;
}
.add-friend {
  padding: 6px 12px;
  border: 1px solid #B794F6;
  border-radius: 6px;
  font-size: 0.85em;
  transition: background 0.2s, color 0.2s;
}
.add-friend:not(.disabled) {
  background: #B794F6;
  color: white;
  cursor: pointer;
}
.add-friend:not(.disabled):hover {
  background: #9d7ae0;
  border-color: #9d7ae0;
}
.add-friend.disabled {
  background: white;
  color: #B794F6;
  cursor: default;
}
.feed-img, .feed-video {
  width: 100%;
  border-radius: 10px;
  margin-top: 10px;
}
.feed-text-section {
  margin-top: 10px;
  font-size: 0.95em;
}
.author-bold {
  font-weight: bold;
  margin-right: 6px;
}
.text-content {
  color: #333;
}
.feed-actions {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}
.like-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 0.9em;
  padding: 0;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: opacity 0.2s;
  color: black;
}
.like-btn:hover {
  opacity: 0.8;
}
.comment-btn {
  background: none;
  border: none;
  border-radius: 6px;
  padding: 8px 12px;
  cursor: pointer;
  font-size: 0.9em;
  transition: background 0.2s;
  color: black;
}
.comment-btn:hover {
  background: #E0C3FC;
}
</style>