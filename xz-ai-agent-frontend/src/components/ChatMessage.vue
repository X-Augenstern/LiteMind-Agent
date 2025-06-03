<script setup lang="ts">
const props = defineProps<{
  content: string
  isUser: boolean
}>()

// 使组件可以默认导出
defineExpose({
  content: props.content,
  isUser: props.isUser,
})
</script>

<template>
  <div class="message" :class="{ 'user-message': isUser, 'ai-message': !isUser }">
    <div class="avatar">
      <span v-if="isUser">👤</span>
      <span v-else>🤖</span>
    </div>
    <div class="message-content">
      <div class="message-text">{{ content }}</div>
    </div>
  </div>
</template>

<style scoped>
.message {
  display: flex;
  margin-bottom: 1rem;
  max-width: 80%;
}

.user-message {
  align-self: flex-end;
  flex-direction: row-reverse;
  margin-left: auto;
}

.ai-message {
  align-self: flex-start;
  margin-right: auto;
}

.avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  margin: 0 8px;
  font-size: 1.5rem;
}

.message-content {
  background-color: white;
  padding: 0.8rem 1rem;
  border-radius: 10px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.user-message .message-content {
  background-color: #dcf8c6;
}

.ai-message .message-content {
  background-color: white;
}

.message-text {
  white-space: pre-wrap;
  word-break: break-word;
}
</style> 