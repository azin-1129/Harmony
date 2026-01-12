<template>
  <div class="search-page">
    <div class="search-header">
      <h1>검색</h1>
      <input
        v-model="searchQuery"
        type="text"
        placeholder="사람, 해시태그, 게시물 검색..."
        class="search-input"
        @keyup.enter="handleSearch"
      />
    </div>

    <div class="search-results" v-if="searchQuery">
      <p v-if="loading" class="loading">검색 중...</p>
      <p v-else-if="!results.length" class="no-results">검색 결과가 없습니다.</p>
      <div v-else class="results-list">
        <div v-for="result in results" :key="result.id" class="result-item">
          <h3>{{ result.title }}</h3>
          <p>{{ result.description }}</p>
        </div>
      </div>
    </div>

    <div v-else class="suggestions">
      <h2>인기 검색어</h2>
      <div class="popular-tags">
        <span class="tag">#개발</span>
        <span class="tag">#디자인</span>
        <span class="tag">#여행</span>
        <span class="tag">#음악</span>
        <span class="tag">#사진</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const searchQuery = ref('')
const results = ref([])
const loading = ref(false)

const handleSearch = async () => {
  loading.value = true
  // API 호출 예시 (나중에 실제 API 연결)
  setTimeout(() => {
    results.value = [
      { id: 1, title: '검색 결과 1', description: '설명' },
      { id: 2, title: '검색 결과 2', description: '설명' }
    ]
    loading.value = false
  }, 500)
}
</script>

<style scoped>
.search-page {
  padding: 20px;
  width: 100%;
}

.search-header {
  margin-bottom: 30px;
}

.search-header h1 {
  margin-bottom: 15px;
}

.search-input {
  width: 100%;
  max-width: 500px;
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 20px;
  font-size: 0.95em;
  outline: none;
  transition: border 0.2s;
}

.search-input:focus {
  border-color: #007aff;
}

.search-results {
  margin-top: 20px;
}

.loading,
.no-results {
  text-align: center;
  color: #999;
  padding: 20px;
}

.results-list {
  display: grid;
  gap: 15px;
}

.result-item {
  padding: 15px;
  border: 1px solid #eee;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.result-item:hover {
  background-color: #f5f5f5;
}

.result-item h3 {
  margin: 0 0 8px 0;
  font-size: 1em;
}

.result-item p {
  margin: 0;
  color: #666;
  font-size: 0.9em;
}

.suggestions {
  margin-top: 40px;
}

.suggestions h2 {
  margin-bottom: 20px;
}

.popular-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag {
  background-color: #f0f0f0;
  padding: 8px 15px;
  border-radius: 20px;
  cursor: pointer;
  transition: background 0.2s;
}

.tag:hover {
  background-color: #e0e0e0;
}
</style>
