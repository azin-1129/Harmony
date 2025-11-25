<script setup>
import { ref, computed } from 'vue'

// 내 프로필 정보
const myProfile = ref({
  nickname: 'azinlee',
  profileImage: 'https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e?ixlib=rb-4.0.3&auto=format&fit=crop&w=80&q=80',
  friendCount: 150
})

// 내 게시물 더미 데이터
const myFeeds = ref([
  { id: 1, type: 'post', content: '첫 번째 게시물', imageUrl: 'https://images.unsplash.com/photo-1503023345310-bd7c1de61c7d?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80' },
  { id: 2, type: 'post', content: '두 번째 게시물', imageUrl: 'https://images.unsplash.com/photo-1525351484163-7529414344d8?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80' },
  { id: 3, type: 'reels', videoUrl: 'https://www.w3schools.com/html/mov_bbb.mp4' },
  { id: 4, type: 'reels', videoUrl: 'https://www.w3schools.com/html/movie.mp4' }
])

// 탭 상태: 'post' or 'reels'
const activeTab = ref('post')

// activeTab 기준으로 필터링된 게시물
const filteredFeeds = computed(() => {
  return myFeeds.value.filter(f => f.type === activeTab.value)
})
</script>

<template>
  <div class="my-page">
    <!-- 프로필 헤더 -->
    <div class="profile-header">
      <img :src="myProfile.profileImage" alt="프로필" class="profile-img" />
      <div class="profile-info">
        <h2>{{ myProfile.nickname }}</h2>
        <div class="friend-info">
          친구 {{ myProfile.friendCount }}명
        </div>
      </div>
    </div>

    <!-- 게시물/릴스 탭 -->
    <div class="tab-buttons">
      <button
        :class="{ active: activeTab === 'post' }"
        @click="activeTab = 'post'"
      >
        게시물
      </button>
      <button
        :class="{ active: activeTab === 'reels' }"
        @click="activeTab = 'reels'"
      >
        릴스
      </button>
    </div>

    <!-- 선택된 게시물 타입 피드 -->
    <div class="feed-grid">
      <div v-for="feed in filteredFeeds" :key="feed.id" class="feed-item">
        <template v-if="feed.type === 'post'">
          <img :src="feed.imageUrl" alt="feed" />
          <p>{{ feed.content }}</p>
        </template>
        <template v-else>
          <video :src="feed.videoUrl" controls></video>
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
.my-page {
  padding: 16px;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.profile-img {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
}

.profile-info h2 {
  margin: 0;
}

.friend-info {
  font-size: 0.9rem;
  color: #666;
}

/* 탭 버튼 */
.tab-buttons {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.tab-buttons button {
  flex: 1;
  padding: 8px;
  border: none;
  background: #eee;
  cursor: pointer;
  border-radius: 4px;
}

.tab-buttons button.active {
  background: #007aff;
  color: white;
}

/* 피드 그리드 */
.feed-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 8px;
}

.feed-item img,
.feed-item video {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  border-radius: 8px;
}

.feed-item p {
  font-size: 0.8rem;
  margin-top: 4px;
}
</style>
