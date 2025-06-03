<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { loveAppService } from '../services/api'

interface Message {
  id: number
  content: string
  isUser: boolean
}

const router = useRouter()
const messages = ref<Message[]>([])
const inputMessage = ref('')
const chatId = ref('')
const eventSource = ref<EventSource | null>(null)
const isLoading = ref(false)

// SEO元数据
const pageTitle = 'AI恋爱大师 - 专业情感咨询AI助手'
const pageDescription =
  '与AI恋爱大师对话，获得专业情感建议，解决你的感情困惑。流式响应，实时聊天体验。'
const pageKeywords = 'AI恋爱大师,情感咨询,恋爱顾问,人工智能,聊天机器人,情感问题'

// 生成随机的聊天室ID
const generateChatId = () => {
  return 'chat_' + Math.random().toString(36).substring(2, 9)
}

// 初始化聊天
onMounted(() => {
  // 设置页面标题和元数据
  document.title = pageTitle

  // 设置元数据
  const metaDescription = document.querySelector('meta[name="description"]')
  if (metaDescription) {
    metaDescription.setAttribute('content', pageDescription)
  }

  const metaKeywords = document.querySelector('meta[name="keywords"]')
  if (metaKeywords) {
    metaKeywords.setAttribute('content', pageKeywords)
  }

  chatId.value = generateChatId()
  // 添加欢迎消息
  messages.value.push({
    id: Date.now(),
    content: '你好，我是AI恋爱大师，很高兴与你聊天。有什么感情问题需要咨询吗？',
    isUser: false,
  })
})

// 关闭SSE连接
onBeforeUnmount(() => {
  if (eventSource.value) {
    eventSource.value.close()
  }
})

// 发送消息
const sendMessage = () => {
  if (!inputMessage.value.trim()) return

  // 添加用户消息
  const userMessageId = Date.now()
  messages.value.push({
    id: userMessageId,
    content: inputMessage.value,
    isUser: true,
  })

  const userMessage = inputMessage.value
  inputMessage.value = ''

  // 自动滚动到底部
  setTimeout(scrollToBottom, 100)

  // 显示加载状态
  isLoading.value = true

  // 关闭之前的连接
  if (eventSource.value) {
    eventSource.value.close()
  }

  // 建立SSE连接
  eventSource.value = loveAppService.createChatConnection(userMessage, chatId.value)

  let responseMessage = ''
  const aiMessageId = Date.now() + 1 // 确保与用户消息ID不同

  // 先添加一个空的AI消息气泡
  messages.value.push({
    id: aiMessageId,
    content: '',
    isUser: false, // 这是AI消息，不是用户消息
  })

  eventSource.value.onmessage = (event) => {
    const data = event.data
    if (data === '[DONE]') {
      // 消息接收完成
      eventSource.value?.close()
      eventSource.value = null
      isLoading.value = false
      scrollToBottom()
      return
    }

    responseMessage += data

    // 查找并更新AI消息（确保是最后添加的AI消息）
    const aiMessageIndex = messages.value.findIndex((msg) => msg.id === aiMessageId)
    if (aiMessageIndex !== -1) {
      messages.value[aiMessageIndex].content = responseMessage
    }

    // 自动滚动到底部
    scrollToBottom()
  }

  eventSource.value.onerror = () => {
    eventSource.value?.close()
    eventSource.value = null
    isLoading.value = false

    // 如果消息为空，添加错误提示
    if (!responseMessage) {
      const aiMessageIndex = messages.value.findIndex((msg) => msg.id === aiMessageId)
      if (aiMessageIndex !== -1) {
        messages.value[aiMessageIndex].content = '抱歉，连接出现问题，请稍后再试。'
      }
    }

    scrollToBottom()
  }
}

// 滚动到底部
const messageContainer = ref(null)
const scrollToBottom = () => {
  if (messageContainer.value) {
    const container = messageContainer.value as HTMLElement
    container.scrollTop = container.scrollHeight
  }
}
</script>

<template>
  <div class="chat-page">
    <div class="chat-container">
      <div class="chat-header love-header">
        <button class="back-button" @click="router.push('/')">
          <span class="back-icon">←</span>
          <span class="back-text">返回</span>
        </button>
        <div class="header-info">
          <h1>AI 恋爱大师</h1>
          <div class="chat-id">会话ID: {{ chatId }}</div>
        </div>
      </div>

      <div class="chat-messages" ref="messageContainer">
        <div
          v-for="message in messages"
          :key="message.id"
          class="message"
          :class="{ 'user-message': message.isUser, 'ai-message': !message.isUser }"
        >
          <div class="avatar">
            <img
              v-if="!message.isUser"
              src="data:image/svg+xml;charset=utf-8,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 width=%22100%22 height=%22100%22 viewBox=%220 0 100 100%22%3E%3Ccircle cx=%2250%22 cy=%2250%22 r=%2245%22 fill=%22%23ff6b6b%22/%3E%3Ctext x=%2250%22 y=%2250%22 font-family=%22Arial%22 font-size=%2240%22 text-anchor=%22middle%22 dominant-baseline=%22middle%22 fill=%22white%22%3E❤️%3C/text%3E%3C/svg%3E"
              alt="AI恋爱大师"
              class="ai-avatar"
            />
            <img
              v-else
              src="data:image/svg+xml;charset=utf-8,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 width=%22100%22 height=%22100%22 viewBox=%220 0 100 100%22%3E%3Ccircle cx=%2250%22 cy=%2250%22 r=%2245%22 fill=%22%23444%22/%3E%3Ctext x=%2250%22 y=%2250%22 font-family=%22Arial%22 font-size=%2240%22 text-anchor=%22middle%22 dominant-baseline=%22middle%22 fill=%22white%22%3E👤%3C/text%3E%3C/svg%3E"
              alt="用户"
              class="user-avatar"
            />
          </div>
          <div class="message-content">
            <div class="message-sender">{{ message.isUser ? '你' : 'AI恋爱大师' }}</div>
            <div class="message-text">{{ message.content || ' ' }}</div>
            <div class="message-time">{{ new Date(message.id).toLocaleTimeString() }}</div>
          </div>
        </div>
        <div v-if="isLoading" class="typing-indicator">
          <span></span>
          <span></span>
          <span></span>
        </div>
      </div>

      <div class="chat-input">
        <input
          v-model="inputMessage"
          @keyup.enter="sendMessage"
          placeholder="输入消息..."
          :disabled="isLoading"
        />
        <button
          class="send-button"
          @click="sendMessage"
          :disabled="isLoading || !inputMessage.trim()"
        >
          <span class="send-icon">↑</span>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.chat-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
  width: 100%;
}

.chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 100%;
  max-width: 100%;
  margin: 0;
  background-color: white;
  box-shadow: var(--shadow-light);
}

.love-header {
  background-color: var(--love-color);
}

.chat-header {
  display: flex;
  align-items: center;
  padding: 1rem;
  color: white;
  position: relative;
}

.back-button {
  display: flex;
  align-items: center;
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  font-size: 1rem;
  padding: 0.5rem 1rem;
  border-radius: 50px;
  transition: background-color 0.3s;
}

.back-button:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

.back-icon {
  font-size: 1.2rem;
  margin-right: 0.5rem;
}

.header-info {
  flex: 1;
  text-align: center;
}

.chat-header h1 {
  margin: 0;
  font-size: 1.5rem;
}

.chat-id {
  font-size: 0.8rem;
  opacity: 0.8;
  margin-top: 0.25rem;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  background-color: #f9f9f9;
}

.message {
  display: flex;
  max-width: 80%;
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.user-message {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.ai-message {
  align-self: flex-start;
}

.avatar {
  display: flex;
  align-items: flex-start;
  padding-top: 0.5rem;
}

.ai-avatar,
.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  border: 2px solid white;
}

.message-content {
  margin: 0 0.8rem;
  padding: 0.8rem 1rem;
  border-radius: 1rem;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  position: relative;
}

.user-message .message-content {
  background-color: var(--bubble-user);
  border-top-right-radius: 0;
  text-align: right;
}

.ai-message .message-content {
  background-color: var(--bubble-ai);
  border-top-left-radius: 0;
}

.message-sender {
  font-size: 0.85rem;
  font-weight: bold;
  margin-bottom: 0.3rem;
  color: var(--dark-color);
}

.user-message .message-sender {
  color: #3c7d3e;
}

.ai-message .message-sender {
  color: var(--love-color);
}

.message-text {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.5;
}

.message-time {
  font-size: 0.75rem;
  opacity: 0.6;
  margin-top: 0.3rem;
}

.chat-input {
  display: flex;
  padding: 1rem;
  background-color: white;
  border-top: 1px solid #e0e0e0;
}

.chat-input input {
  flex: 1;
  padding: 0.8rem 1.2rem;
  border: 1px solid #ddd;
  border-radius: 50px;
  font-size: 1rem;
  margin-right: 0.5rem;
  outline: none;
  transition: border-color 0.3s;
}

.chat-input input:focus {
  border-color: var(--love-color);
}

.send-button {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background-color: var(--love-color);
  color: white;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
}

.send-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  background-color: #ff5b5b;
}

.send-button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.send-icon {
  font-size: 1.2rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-container {
    height: 100vh;
    max-width: 100%;
    border-radius: 0;
  }

  .message {
    max-width: 90%;
  }

  .back-text {
    display: none;
  }

  .back-icon {
    margin-right: 0;
  }
}

@media (max-width: 576px) {
  .chat-header h1 {
    font-size: 1.3rem;
  }

  .chat-messages {
    padding: 0.8rem;
  }

  .message-content {
    padding: 0.7rem;
  }

  .chat-input {
    padding: 0.8rem;
  }

  .chat-input input {
    padding: 0.7rem 1rem;
  }

  .send-button {
    width: 45px;
    height: 45px;
  }
}
</style> 