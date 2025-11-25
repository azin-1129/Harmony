<template>
  <div class="main-layout">
    <Sidebar />
    <div class="content">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import Sidebar from '@/components/Sidebar.vue'
</script>

<style scoped>
.main-layout {
  display: flex;
  height: 100dvh;
  overflow: hidden;
}

.content {
  flex: 1;
  overflow-y: auto;

  /* ✅ 스크롤바 숨기기 */
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE, Edge */
}

.content::-webkit-scrollbar {
  display: none; /* Chrome, Safari */
}
</style>