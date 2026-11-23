import axios from 'axios'
import { ElMessage } from 'element-plus'

/**
 * 后端统一返回结构：{ success, message, data }
 *
 * 拦截器负责：
 * 1. success=true 时直接把 data 返回给调用方
 * 2. success=false 或 HTTP 异常时统一提示，并 reject ApiError
 *
 * 页面因此只需要处理成功分支，catch 里一般只记录日志 / 做状态清理，
 * 只有需要区分「接口错误」与「本地错误」（如剪贴板失败）时才判断 ApiError。
 */
export class ApiError extends Error {
  constructor(message, payload) {
    super(message)
    this.name = 'ApiError'
    this.payload = payload
  }
}

const request = axios.create({
  baseURL: '/api',
  // SQL 查询、大模型生成 SQL 可能较慢，超时设置得宽一些
  timeout: 120000
})

request.interceptors.response.use(
  (response) => {
    const body = response.data
    // 兼容非 Result 结构的响应（如静态资源/第三方接口）
    if (body && typeof body === 'object' && 'success' in body) {
      if (body.success) {
        return body.data
      }
      const message = body.message || '请求失败'
      ElMessage.error(message)
      return Promise.reject(new ApiError(message, body))
    }
    return body
  },
  (error) => {
    if (axios.isCancel(error)) {
      return Promise.reject(error)
    }
    const message = error.response?.data?.message || '网络错误，请稍后重试'
    ElMessage.error(message)
    return Promise.reject(new ApiError(message, error.response?.data))
  }
)

export default request
