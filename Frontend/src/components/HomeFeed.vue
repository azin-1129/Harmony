<template>
  <div class="page">
    <h1>홈 피드</h1>
    <FeedItem v-for="f in feeds" :key="f.id" :feed="f" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import FeedItem from './FeedItem.vue'

// 예제 데이터 (웹 이미지 링크 사용)
const feeds = ref([
  {
    id: 1,
    author: { 
      name: 'ajin', 
      profileImage: 'https://images.unsplash.com/photo-1599566150163-29194dcaad36?ixlib=rb-4.0.3&auto=format&fit=crop&w=36&q=80'
    },
    text: '오늘 날씨 최고!',
    imageUrl: 'https://images.unsplash.com/photo-1503023345310-bd7c1de61c7d?ixlib=rb-4.0.3&auto=format&fit=crop&w=600&q=80',
    likes: 12,
    liked: false
  },
  {
    id: 2,
    author: { 
      name: 'hana', 
      profileImage: 'https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e?ixlib=rb-4.0.3&auto=format&fit=crop&w=36&q=80'
    },
    text: '고양이 자랑',
    imageUrl: 'https://images.unsplash.com/photo-1518791841217-8f162f1e1131?ixlib=rb-4.0.3&auto=format&fit=crop&w=600&q=80',
    likes: 8,
    liked: false
  }
])

</script>

<style scoped>
.page {
  padding: 20px;
}
</style>