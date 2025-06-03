import axios from 'axios'

/**
 * API服务模块 - 提供与后端API的交互功能
 */

// 后端API基础URL
const API_BASE_URL = 'http://localhost:8123/api'

/**
 * 爱情顾问API服务
 */
export const loveAppService = {
  /**
   * 创建SSE连接，获取AI恋爱大师的回复
   * @param message 用户消息
   * @param chatId 聊天ID
   * @returns EventSource实例
   */
  createChatConnection(message: string, chatId: string): EventSource {
    const url = `${API_BASE_URL}/ai/loveApp/chat/sse?message=${encodeURIComponent(
      message,
    )}&chatId=${chatId}`
    return new EventSource(url)
  },
}

/**
 * 超级智能体API服务
 */
export const liteMindService = {
  /**
   * 创建SSE连接，获取AI超级智能体的思维链响应
   * @param message 用户消息
   * @returns EventSource实例
   */
  createChatConnection(message: string): EventSource {
    const url = `${API_BASE_URL}/ai/liteMind/chat?message=${encodeURIComponent(message)}`
    return new EventSource(url)
  },
}

/**
 * 通用API服务
 */
export const apiService = {
  /**
   * 检查API服务器是否在线
   * @returns Promise<boolean>
   */
  async checkServerStatus(): Promise<boolean> {
    try {
      const response = await fetch(`${API_BASE_URL}/health`)
      return response.ok
    } catch (error) {
      console.error('API服务器连接失败:', error)
      return false
    }
  },
}
