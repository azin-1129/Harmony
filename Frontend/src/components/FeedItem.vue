<template>
  <div class="feed-item">
    <div class="feed-header">
      <img :src="feed.author.profileImage" class="profile" />
      <span class="username">{{ feed.author.name }}</span>
    </div>

    <div class="feed-content">
      <img v-if="feed.imageUrl" :src="feed.imageUrl" class="feed-img" />
      <video v-else-if="feed.videoUrl" :src="feed.videoUrl" controls class="feed-video"></video>
      <p v-if="feed.text">{{ feed.text }}</p>
    </div>

    <div class="feed-actions">
      <button @click="toggleLike">❤️ {{ feed.likes }}</button>
      <button @click="toggleComments">💬 댓글</button>
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
  margin-bottom: 10px;
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
.feed-img, .feed-video {
  width: 100%;
  border-radius: 10px;
  margin-top: 10px;
}
.feed-actions {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}
</style>