<script setup lang="ts">
import { onMounted } from 'vue'

// SEO元数据
const pageTitle = '关于我们 - XZ-AI智能应用平台'
const pageDescription =
  '了解XZ-AI智能应用平台的愿景、使命和技术细节。我们致力于提供最先进的AI对话体验。'
const pageKeywords = 'XZ-AI,人工智能,AI平台,关于我们,技术愿景'

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
})
</script>

<template>
  <div class="about-page">
    <div class="about-header">
      <div class="container">
        <h1 class="page-title">关于 <span class="highlight">XZ-AI</span></h1>
        <p class="subtitle">探索未来，连接智能</p>
      </div>
    </div>

    <div class="about-content container">
      <section class="about-section">
        <h2>我们的愿景</h2>
        <p>
          XZ-AI智能应用平台致力于打造最智能、最自然的人机交互体验，让人工智能真正成为人类的得力助手。我们相信，通过先进的大语言模型技术，可以为用户提供前所未有的智能服务体验。
        </p>
      </section>

      <section class="about-section">
        <h2>我们的产品</h2>

        <div class="product-cards">
          <div class="product-card">
            <div class="product-icon">❤️</div>
            <h3>AI 恋爱大师</h3>
            <p>
              专注于情感咨询的AI助手，能够理解人类复杂的情感需求，提供贴心的建议和支持。无论是情感困惑还是关系建议，AI恋爱大师都能给予专业且富有同理心的回应。
            </p>
          </div>

          <div class="product-card">
            <div class="product-icon">🧠</div>
            <h3>AI 超级智能体</h3>
            <p>
              强大的通用型AI助手，具备复杂问题的分析和解决能力。通过独特的思维链技术，AI超级智能体能够展示其思考过程，让用户了解AI是如何一步步解决问题的。
            </p>
          </div>
        </div>
      </section>

      <section class="about-section">
        <h2>技术特点</h2>

        <div class="tech-features">
          <div class="tech-feature">
            <div class="feature-icon">⚡</div>
            <div class="feature-content">
              <h3>实时流式响应</h3>
              <p>
                采用先进的SSE（Server-Sent
                Events）技术，实现AI响应的实时流式输出，让对话体验更加自然流畅。
              </p>
            </div>
          </div>

          <div class="tech-feature">
            <div class="feature-icon">🔄</div>
            <div class="feature-content">
              <h3>思维链展示</h3>
              <p>超级智能体采用思维链技术，展示AI的思考过程，提高回答的可解释性和透明度。</p>
            </div>
          </div>

          <div class="tech-feature">
            <div class="feature-icon">📱</div>
            <div class="feature-content">
              <h3>全平台适配</h3>
              <p>响应式设计确保在PC、平板和手机等各种设备上都能获得最佳用户体验。</p>
            </div>
          </div>
        </div>
      </section>

      <section class="about-section contact-section">
        <h2>联系我们</h2>
        <p>如果您对我们的产品有任何问题、建议或合作意向，欢迎通过以下方式联系我们：</p>
        <div class="contact-info">
          <div class="contact-item">
            <div class="contact-icon">📧</div>
            <p>contact@xz-ai.com</p>
          </div>
          <div class="contact-item">
            <div class="contact-icon">🌐</div>
            <p>www.xz-ai.com</p>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.about-page {
  min-height: 100vh;
  padding-bottom: 0;
  width: 100%;
}

.about-header {
  background-color: var(--dark-color);
  color: white;
  padding: 5rem 0;
  text-align: center;
  position: relative;
  overflow: hidden;
  width: 100%;
}

.container {
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 0 2rem;
}

.about-content {
  padding: 3rem 0;
  width: 100%;
}

.about-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(78, 84, 200, 0.2), rgba(255, 107, 107, 0.2));
  z-index: 0;
}

.page-title {
  font-size: 3rem;
  margin-bottom: 1rem;
  position: relative;
  z-index: 1;
}

.highlight {
  color: var(--primary-color);
  font-weight: 700;
  position: relative;
}

.highlight::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, var(--love-color), var(--mind-color));
  border-radius: 2px;
}

.subtitle {
  font-size: 1.5rem;
  opacity: 0.8;
  position: relative;
  z-index: 1;
}

.about-section {
  margin-bottom: 4rem;
}

.about-section h2 {
  font-size: 2rem;
  margin-bottom: 1.5rem;
  color: var(--dark-color);
  position: relative;
  display: inline-block;
}

.about-section h2::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 0;
  width: 60px;
  height: 3px;
  background: linear-gradient(90deg, var(--love-color), var(--mind-color));
  border-radius: 2px;
}

.about-section p {
  font-size: 1.1rem;
  line-height: 1.7;
  color: #555;
}

.product-cards {
  display: flex;
  gap: 2rem;
  margin-top: 2rem;
  flex-wrap: wrap;
}

.product-card {
  flex: 1;
  min-width: 300px;
  background-color: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: var(--shadow-light);
  transition: transform 0.3s, box-shadow 0.3s;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-hover);
}

.product-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.product-card h3 {
  font-size: 1.5rem;
  margin-bottom: 1rem;
  color: var(--dark-color);
}

.tech-features {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  margin-top: 2rem;
}

.tech-feature {
  display: flex;
  gap: 1.5rem;
  background-color: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: var(--shadow-light);
  transition: transform 0.3s;
}

.tech-feature:hover {
  transform: translateX(5px);
}

.feature-icon {
  font-size: 2rem;
  color: var(--primary-color);
}

.feature-content h3 {
  font-size: 1.3rem;
  margin-bottom: 0.5rem;
  color: var(--dark-color);
}

.feature-content p {
  font-size: 1rem;
  margin: 0;
}

.contact-section {
  text-align: center;
}

.contact-info {
  display: flex;
  justify-content: center;
  gap: 3rem;
  margin-top: 2rem;
  flex-wrap: wrap;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 0.8rem;
}

.contact-icon {
  font-size: 1.5rem;
}

.contact-item p {
  margin: 0;
  font-size: 1.1rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-title {
    font-size: 2.5rem;
  }

  .subtitle {
    font-size: 1.2rem;
  }

  .product-cards {
    flex-direction: column;
  }

  .product-card {
    min-width: auto;
  }

  .about-section h2 {
    font-size: 1.8rem;
  }
}

@media (max-width: 576px) {
  .about-header {
    padding: 3rem 0;
  }

  .page-title {
    font-size: 2rem;
  }

  .tech-feature {
    flex-direction: column;
    gap: 1rem;
    align-items: center;
    text-align: center;
  }

  .contact-info {
    flex-direction: column;
    gap: 1.5rem;
  }
}
</style>
