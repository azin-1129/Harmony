<template>
  <div class="page">
    <h1>홈 피드</h1>
    <div v-if="loading">로딩 중...</div>
    <div v-else>
      <FeedItem v-for="f in feeds" :key="f.id" :feed="f" />
      <div v-if="feeds.length === 0">팔로잉한 사용자의 게시물이 없습니다.</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import FeedItem from './FeedItem.vue'
import { apiFetch } from '@/utils/apiClient'

const feeds = ref([])
const loading = ref(false)

const mapArticleToFeed = (item, idx) => {
  return {
    id: item.id || `${item.authorIdentifier}-${item.createdAt}-${idx}`,
    author: {
      name: item.authorIdentifier,
      profileImage: item.authorProfileImageUrl
    },
    text: item.articleText,
    imageUrl: item.articleMediaUrl || item.articleThumbnailUrl || '',
    likes: item.articleLikeCount || 0,
    liked: false,
    commentCount: item.articleCommentCount || 0,
    raw: item
  }
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await apiFetch('/article/following', { method: 'GET' })
    if (!res) return
    if (!res.ok) {
      console.error('Failed to load following articles', res.status)
      return
    }
    const body = await res.json()
    const list = Array.isArray(body.data) ? body.data : []
    feeds.value = list.map((it, i) => mapArticleToFeed(it, i))
  } catch (err) {
    console.error('Error fetching feed', err)
  } finally {
    loading.value = false
  }
})

</script>

<style scoped>
.page {
  padding: 20px;
}
</style>