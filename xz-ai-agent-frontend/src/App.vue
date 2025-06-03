<script setup lang="ts">
import { RouterView } from 'vue-router'
</script>

<template>
  <div class="app-wrapper">
    <header>
      <meta
        name="description"
        content="AI智能应用平台 - 提供AI恋爱大师和超级智能体服务，解决各种问题"
      />
      <meta name="keywords" content="AI应用,人工智能,聊天机器人,AI恋爱大师,智能体,AI助手" />
      <meta name="author" content="XZ-AI-Agent" />
      <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    </header>

    <main>
      <RouterView />
    </main>

    <footer class="footer">
      <div class="footer-content">
        <div class="footer-logo">
          <span class="neon-text">XZ-AI</span>
        </div>
        <div class="footer-links">
          <a href="/">首页</a>
          <a href="/love-app">AI恋爱大师</a>
          <a href="/lite-mind">超级智能体</a>
          <a href="/about">关于我们</a>
        </div>
        <div class="footer-copyright">
          <p>© {{ new Date().getFullYear() }} XZ-AI智能应用平台. 保留所有权利.</p>
          <p>使用人工智能技术提供智能对话服务</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<style>
body {
  margin: 0;
  padding: 0;
  font-family: 'Arial', sans-serif;
  width: 100%;
  overflow-x: hidden;
}

.app-wrapper {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  width: 100%;
}

main {
  flex: 1;
  width: 100%;
}

.footer {
  background-color: var(--dark-color);
  color: var(--text-light);
  padding: 2rem 0;
  text-align: center;
  width: 100%;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
  width: 100%;
}

.footer-logo {
  font-size: 1.8rem;
  font-weight: bold;
  margin-bottom: 1rem;
}

.footer-links {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}

.footer-links a {
  color: var(--text-light);
  opacity: 0.8;
  transition: opacity 0.3s;
}

.footer-links a:hover {
  opacity: 1;
  color: var(--text-light);
}

.footer-copyright {
  font-size: 0.9rem;
  opacity: 0.7;
}

@media (max-width: 576px) {
  .footer-links {
    flex-direction: column;
    gap: 0.5rem;
  }
}
</style>
