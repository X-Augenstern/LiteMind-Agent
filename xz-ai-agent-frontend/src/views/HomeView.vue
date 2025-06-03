<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const typingText = ref('')
const fullText = 'Welcome to XZ-AI Platform'
let typingIndex = 0

onMounted(() => {
  startTypingEffect()
})

function startTypingEffect() {
  const typingInterval = setInterval(() => {
    if (typingIndex < fullText.length) {
      typingText.value += fullText.charAt(typingIndex)
      typingIndex++
    } else {
      clearInterval(typingInterval)
      setTimeout(() => {
        typingText.value = ''
        typingIndex = 0
        startTypingEffect()
      }, 2000)
    }
  }, 100)
}
</script>

<template>
  <div class="home-container">
    <div class="hero-section">
      <div class="matrix-bg"></div>
      <div class="hero-content">
        <div class="terminal-header">
          <div class="terminal-circle red"></div>
          <div class="terminal-circle yellow"></div>
          <div class="terminal-circle green"></div>
          <div class="terminal-title">xz-ai-terminal</div>
        </div>
        <div class="terminal-body">
          <div class="terminal-line">
            <span class="prompt">$</span>
            <span class="command">sudo apt install xz-ai-platform</span>
          </div>
          <div class="terminal-line">
            <span class="prompt">$</span>
            <span class="command">xz-ai --version</span>
          </div>
          <div class="terminal-line">
            <span class="output">XZ-AI Platform v1.0.0</span>
          </div>
          <div class="terminal-line">
            <span class="prompt">$</span>
            <span class="typing-command">{{ typingText }}</span>
            <span class="cursor"></span>
          </div>
        </div>
      </div>
      <h1 class="title glitch-effect">AI <span class="neon-text">智能应用平台</span></h1>
      <p class="subtitle">探索未来对话体验，连接智能新世界</p>
    </div>

    <div class="app-section">
      <div class="content-wrapper">
        <h2 class="section-title">选择你的 AI 应用</h2>

        <div class="app-cards">
          <div class="app-card love-card" @click="router.push('/love-app')">
            <div class="card-icon">
              <img
                src="data:image/svg+xml;charset=utf-8,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 width=%22100%22 height=%22100%22 viewBox=%220 0 100 100%22%3E%3Ccircle cx=%2250%22 cy=%2250%22 r=%2245%22 fill=%22%23ff6b6b%22/%3E%3Ctext x=%2250%22 y=%2250%22 font-family=%22Arial%22 font-size=%2240%22 text-anchor=%22middle%22 dominant-baseline=%22middle%22 fill=%22white%22%3E❤️%3C/text%3E%3C/svg%3E"
                alt="AI恋爱大师"
              />
            </div>
            <div class="card-content">
              <h3>AI 恋爱大师</h3>
              <p>与AI恋爱顾问对话，获得情感建议和支持，解决你的感情困惑</p>
              <div class="card-meta">
                <div class="card-tag">情感咨询</div>
                <div class="card-tag">实时对话</div>
              </div>
              <button class="btn btn-love">开始对话</button>
            </div>
            <div class="card-glow"></div>
          </div>

          <div class="app-card mind-card" @click="router.push('/lite-mind')">
            <div class="card-icon">
              <img
                src="data:image/svg+xml;charset=utf-8,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 width=%22100%22 height=%22100%22 viewBox=%220 0 100 100%22%3E%3Ccircle cx=%2250%22 cy=%2250%22 r=%2245%22 fill=%22%234e54c8%22/%3E%3Ctext x=%2250%22 y=%2250%22 font-family=%22Arial%22 font-size=%2240%22 text-anchor=%22middle%22 dominant-baseline=%22middle%22 fill=%22white%22%3E🧠%3C/text%3E%3C/svg%3E"
                alt="AI超级智能体"
              />
            </div>
            <div class="card-content">
              <h3>AI 超级智能体</h3>
              <p>体验最先进的AI思维，解决复杂问题，获取多领域专业知识支持</p>
              <div class="card-meta">
                <div class="card-tag">智能助手</div>
                <div class="card-tag">ReAct</div>
              </div>
              <button class="btn btn-mind">开始对话</button>
            </div>
            <div class="card-glow"></div>
          </div>
        </div>
      </div>
    </div>

    <div class="features-section">
      <div class="content-wrapper">
        <h2 class="section-title">平台特色</h2>
        <div class="features-grid">
          <div class="feature-card">
            <div class="feature-icon">🔒</div>
            <h3>安全对话</h3>
            <p>所有对话内容加密存储，保护您的隐私安全</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">⚡</div>
            <h3>实时响应</h3>
            <p>流式输出技术，实时生成响应，对话更加流畅</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">🧩</div>
            <h3>多场景适用</h3>
            <p>无论是情感咨询还是专业问题，都能获得满意回答</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">📱</div>
            <h3>全平台兼容</h3>
            <p>支持PC、平板和手机，随时随地开启对话</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home-container {
  padding-bottom: 0;
  width: 100%;
}

.content-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
  width: 100%;
}

.app-section {
  padding: 5rem 0;
  text-align: center;
  width: 100%;
}

.features-section {
  background-color: #f9f9f9;
  padding: 5rem 0;
  text-align: center;
  width: 100%;
}

/* 英雄区域 */
.hero-section {
  position: relative;
  height: 100vh;
  min-height: 600px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: var(--text-light);
  background-color: var(--dark-color);
  overflow: hidden;
  padding: 2rem;
  width: 100%;
}

.matrix-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(rgba(26, 26, 46, 0.9), rgba(26, 26, 46, 0.7)),
    url("data:image/svg+xml,%3Csvg width='100' height='100' viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M11 18c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm48 25c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm-43-7c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm63 31c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM34 90c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm56-76c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM12 86c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm28-65c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm23-11c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-6 60c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm29 22c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zM32 63c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm57-13c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-9-21c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM60 91c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM35 41c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM12 60c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2z' fill='%235763eb' fill-opacity='0.1' fill-rule='evenodd'/%3E%3C/svg%3E");
  z-index: 0;
}

.matrix-bg::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: repeating-linear-gradient(
    0deg,
    transparent,
    transparent 2px,
    rgba(87, 99, 235, 0.05) 2px,
    rgba(87, 99, 235, 0.05) 4px
  );
  z-index: 1;
}

.hero-content {
  position: relative;
  z-index: 2;
  margin-bottom: 3rem;
  width: 100%;
  max-width: 700px;
}

.title {
  font-size: 3.5rem;
  margin-bottom: 1rem;
  position: relative;
  z-index: 2;
}

.subtitle {
  font-size: 1.5rem;
  opacity: 0.8;
  margin-bottom: 2rem;
  position: relative;
  z-index: 2;
}

/* 终端样式 */
.terminal-header {
  background-color: #2d2d3a;
  border-radius: 10px 10px 0 0;
  display: flex;
  align-items: center;
  padding: 0.5rem 1rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.terminal-circle {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 6px;
}

.red {
  background-color: #ff5f56;
}
.yellow {
  background-color: #ffbd2e;
}
.green {
  background-color: #27c93f;
}

.terminal-title {
  margin-left: auto;
  margin-right: auto;
  font-family: var(--font-code);
  font-size: 0.8rem;
  opacity: 0.7;
}

.terminal-body {
  background-color: #1e1e2e;
  padding: 1rem;
  border-radius: 0 0 10px 10px;
  font-family: var(--font-code);
  text-align: left;
  min-height: 200px;
}

.terminal-line {
  margin-bottom: 0.5rem;
  line-height: 1.5;
}

.prompt {
  color: #50fa7b;
  margin-right: 0.5rem;
}

.command {
  color: #8be9fd;
}

.output {
  color: #f1fa8c;
}

.typing-command {
  color: #8be9fd;
}

.cursor {
  display: inline-block;
  width: 8px;
  height: 18px;
  background-color: #f8f8f2;
  animation: blink 1s infinite;
  vertical-align: middle;
}

@keyframes blink {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0;
  }
}

/* 应用区域 */
.section-title {
  position: relative;
  display: inline-block;
  font-size: 2.5rem;
  margin-bottom: 3rem;
  color: var(--dark-color);
}

.section-title::after {
  content: '';
  position: absolute;
  left: 50%;
  bottom: -10px;
  transform: translateX(-50%);
  width: 80px;
  height: 4px;
  background: linear-gradient(90deg, var(--love-color), var(--mind-color));
  border-radius: 2px;
}

.app-cards {
  display: flex;
  justify-content: center;
  gap: 2.5rem;
  margin-top: 2rem;
  flex-wrap: wrap;
}

.app-card {
  position: relative;
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 12px;
  width: 350px;
  min-height: 420px;
  box-shadow: var(--shadow-light);
  transition: transform 0.3s, box-shadow 0.3s;
  overflow: hidden;
  z-index: 1;
  cursor: pointer;
}

.app-card:hover {
  transform: translateY(-10px);
  box-shadow: var(--shadow-hover);
}

.love-card:hover .card-glow {
  opacity: 0.1;
}

.mind-card:hover .card-glow {
  opacity: 0.1;
}

.card-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 120px;
  padding: 1.5rem;
}

.card-icon img {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
}

.love-card .card-icon {
  background-color: var(--love-color);
}

.mind-card .card-icon {
  background-color: var(--mind-color);
}

.card-content {
  padding: 1.5rem;
  flex: 1;
  display: flex;
  flex-direction: column;
  z-index: 2;
}

.card-content h3 {
  font-size: 1.6rem;
  margin-bottom: 1rem;
  color: var(--dark-color);
}

.card-content p {
  color: #666;
  line-height: 1.6;
  margin-bottom: 1.5rem;
  flex: 1;
}

.card-meta {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
}

.card-tag {
  background-color: #f0f0f0;
  padding: 0.3rem 0.8rem;
  border-radius: 50px;
  font-size: 0.8rem;
  color: #666;
}

.love-card .btn {
  background-color: var(--love-color);
}

.mind-card .btn {
  background-color: var(--mind-color);
}

.card-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 300px;
  height: 300px;
  border-radius: 50%;
  opacity: 0;
  transition: opacity 0.3s;
  z-index: 1;
}

.love-card .card-glow {
  background: radial-gradient(circle, rgba(255, 107, 107, 0.5) 0%, rgba(255, 107, 107, 0) 70%);
}

.mind-card .card-glow {
  background: radial-gradient(circle, rgba(78, 84, 200, 0.5) 0%, rgba(78, 84, 200, 0) 70%);
}

/* 特性区域 */
.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
  margin-top: 3rem;
}

.feature-card {
  background-color: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: var(--shadow-light);
  transition: transform 0.3s;
}

.feature-card:hover {
  transform: translateY(-5px);
}

.feature-icon {
  font-size: 2.5rem;
  margin-bottom: 1rem;
}

.feature-card h3 {
  margin-bottom: 1rem;
  color: var(--dark-color);
}

.feature-card p {
  color: #666;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .hero-section {
    height: auto;
    padding: 5rem 2rem;
  }

  .title {
    font-size: 3rem;
  }

  .app-card {
    width: 320px;
  }
}

@media (max-width: 768px) {
  .title {
    font-size: 2.5rem;
  }

  .subtitle {
    font-size: 1.2rem;
  }

  .hero-content {
    max-width: 100%;
  }

  .terminal-body {
    min-height: 150px;
  }

  .app-cards {
    gap: 2rem;
  }

  .features-grid {
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  }
}

@media (max-width: 576px) {
  .hero-section {
    padding: 3rem 1rem;
  }

  .title {
    font-size: 2rem;
  }

  .subtitle {
    font-size: 1rem;
  }

  .app-card {
    width: 100%;
    max-width: 320px;
  }

  .terminal-body {
    min-height: 120px;
    font-size: 0.85rem;
  }

  .section-title {
    font-size: 2rem;
  }
}
</style>
