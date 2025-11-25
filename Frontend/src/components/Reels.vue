<template>
  <div class="reels-container">
    <div
      v-for="feed in reelFeeds"
      :key="feed.id"
      class="reel-item"
    >
      <video
        :src="feed.videoUrl"
        controls
        autoplay
        muted
        loop
        playsinline
      ></video>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

// Reels 전용 더미 데이터 (video 타입만)
const reelsFeeds = ref([
  { id: 1, type: 'reels', videoUrl: 'https://www.w3schools.com/html/mov_bbb.mp4' },
  { id: 2, type: 'reels', videoUrl: 'https://www.w3schools.com/html/movie.mp4' },
  { id: 3, type: 'reels', videoUrl: 'https://www.w3schools.com/html/mov_bbb.mp4' },
  { id: 4, type: 'reels', videoUrl: 'https://www.w3schools.com/html/movie.mp4' },
  { id: 5, type: 'reels', videoUrl: 'https://www.w3schools.com/html/mov_bbb.mp4' },
  { id: 6, type: 'reels', videoUrl: 'https://www.w3schools.com/html/movie.mp4' }
])

// 화면에 표시할 Reels만 필터링
const reelFeeds = computed(() => reelsFeeds.value.filter(f => f.type === 'reels'))
</script>

<style scoped>
.reels-container {
  height: 100vh;
  overflow-y: scroll;
  scroll-snap-type: y mandatory;

  /* 스크롤바 숨기기 */
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE 10+ */
}

.reels-container::-webkit-scrollbar {
  display: none; /* Chrome, Safari, Opera */
}

.reel-item {
  height: 100vh;
  scroll-snap-align: start;
  display: flex;
  align-items: center;
  justify-content: center;
  background: black;
}

.reel-item video {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background: black;
}

</style>
