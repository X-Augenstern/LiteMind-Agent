# XZ-AI 智能应用平台前端

## 项目简介

XZ-AI 智能应用平台是一个基于Vue3的AI对话应用，包含两个主要功能：AI恋爱大师和AI超级智能体。本项目使用Vue3、TypeScript、Vue Router和现代CSS构建，提供响应式设计，支持在PC、平板和手机上使用。

## 功能特点

### AI恋爱大师

- 情感咨询专用AI助手
- 实时对话，流式响应
- 聊天记录按会话ID保存
- 优雅的UI设计和动画效果

### AI超级智能体

- 展示AI思维链过程
- 解决复杂问题的强大能力
- 每个思考步骤分别显示
- 专业知识支持

## 技术栈

- Vue 3
- TypeScript
- Vue Router
- SSE (Server-Sent Events)
- 响应式CSS

## 开发环境配置

### 安装依赖

```bash
npm install
```

### 本地开发服务

```bash
npm run dev
```

### 生产环境构建

```bash
npm run build
```

## 项目结构

```
xz-ai-agent-frontend/
├── public/          # 静态资源
├── src/
│   ├── assets/      # 样式和资源文件
│   ├── components/  # 可复用组件
│   ├── router/      # 路由配置
│   ├── services/    # API服务
│   ├── views/       # 页面视图
│   ├── App.vue      # 根组件
│   └── main.ts      # 入口文件
└── README.md        # 项目说明
```

## 浏览器兼容性

支持所有现代浏览器，包括：

- Chrome (最新2个版本)
- Firefox (最新2个版本)
- Safari (最新2个版本)
- Edge (最新2个版本)

## 后端API

需要配合后端API服务使用，默认API地址为`http://localhost:8123/api`。
如需修改API地址，请在`src/services/api.ts`文件中更新`API_BASE_URL`常量。

## Recommended IDE Setup

[VSCode](https://code.visualstudio.com/) + [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) (and disable Vetur).

## Type Support for `.vue` Imports in TS

TypeScript cannot handle type information for `.vue` imports by default, so we replace the `tsc` CLI with `vue-tsc` for type checking. In editors, we need [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) to make the TypeScript language service aware of `.vue` types.

## Customize configuration

See [Vite Configuration Reference](https://vite.dev/config/).
