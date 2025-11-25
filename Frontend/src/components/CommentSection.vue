<template>
  <div class="comments">
    <div v-for="c in comments" :key="c.id" class="comment">
      <b>{{ c.user }}</b> {{ c.text }}
    </div>

    <form @submit.prevent="addComment">
      <input v-model="newComment" placeholder="댓글 추가..." />
      <button type="submit">게시</button>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  feedId: Number
})

const comments = ref([
  { id: 1, user: 'ajin', text: '멋진 사진이에요!' },
  { id: 2, user: 'hana', text: '와 대박' }
])

const newComment = ref('')

const addComment = () => {
  if (!newComment.value.trim()) return
  comments.value.push({
    id: Date.now(),
    user: 'me',
    text: newComment.value
  })
  newComment.value = ''
}
</script>

<style scoped>
.comments {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #eee;
}
.comment {
  margin-bottom: 6px;
}
form {
  display: flex;
  gap: 6px;
}
input {
  flex: 1;
  padding: 6px 8px;
  border-radius: 6px;
  border: 1px solid #ccc;
}
button {
  padding: 6px 10px;
  border: none;
  background-color: #007aff;
  color: white;
  border-radius: 6px;
}
</style>