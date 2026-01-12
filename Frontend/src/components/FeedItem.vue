<template>
  <div class="feed-item">
    <div class="feed-header">
        <div class="header-left">
          <img :src="feed.author.profileImage" class="profile" />
          <span class="username">{{ feed.author.name }}</span>
        </div>
        <button class="add-friend" @click.prevent="onAddFriend">친구 추가</button>
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

    <CommentSection v-if="showComments" :feedId="feed.id" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import CommentSection from './CommentSection.vue'

const props = defineProps({
  feed: Object
})

const showComments = ref(false)
const toggleComments = () => (showComments.value = !showComments.value)

const toggleLike = () => {
  feed.likes += feed.liked ? -1 : 1
  feed.liked = !feed.liked
}

const onAddFriend = () => {
  // 친구 추가 API 미연동 상태: 버튼만 동작하도록 로그 출력
  console.log('친구 추가 클릭:', feed.author.name)
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
  border: 1px solid #ddd;
  border-radius: 6px;
  background: #E0C3FC;
  cursor: pointer;
  font-size: 0.85em;
  transition: background 0.2s;
}
.add-friend:hover {
  background: #D5B3E8;
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
.like-btn,
.comment-btn {
  background: #E0C3FC;
  border: none;
  border-radius: 6px;
  padding: 8px 12px;
  cursor: pointer;
  font-size: 0.9em;
  transition: background 0.2s;
}
.like-btn:hover,
.comment-btn:hover {
  background: #D5B3E8;
}
</style>