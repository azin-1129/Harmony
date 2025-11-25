<template>
  <div class="chat-sidebar">
    <h2>내 친구 목록</h2>
    <ul>
      <li
        v-for="friend in friends"
        :key="friend.id"
        @click="$emit('select-friend', friend)"
        :class="{ active: selectedFriend?.id === friend.id }"
      >
        <img
          v-if="friend.profileImage"
          :src="friend.profileImage"
          alt="프로필"
          class="avatar"
        />
        {{ friend.name }}
      </li>
    </ul>
  </div>
</template>

<script setup>
const props = defineProps({
  friends: Array,
  selectedFriend: Object
})
</script>

<style scoped>
.chat-sidebar {
  width: 30%;
  border-right: 1px solid #ddd;
  overflow-y: auto;
  background: #fafafa;
}

ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

li {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.2s;
}

li:hover {
  background: #f0f0f0;
}

li.active {
  font-weight: bold;
  background: #e0e0ff;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  margin-right: 8px;
  object-fit: cover;
}
</style>
