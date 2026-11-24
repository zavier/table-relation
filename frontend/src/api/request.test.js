import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('element-plus', () => ({
  ElMessage: {
    error: vi.fn()
  }
}))

import { ElMessage } from 'element-plus'
import request, { ApiError } from './request'

const responseWith = (body) => ({
  data: body,
  status: 200,
  statusText: 'OK',
  headers: {},
  config: {}
})

const useAdapter = (handler) => {
  request.defaults.adapter = async (config) => handler(config)
}

describe('request 拦截器', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('success=true 时直接返回 data', async () => {
    useAdapter(() => responseWith({ success: true, data: { id: 1 } }))

    await expect(request.get('/test')).resolves.toEqual({ id: 1 })
    expect(ElMessage.error).not.toHaveBeenCalled()
  })

  it('success=false 时提示后端 message 并抛出 ApiError', async () => {
    useAdapter(() => responseWith({ success: false, message: '参数错误' }))

    await expect(request.get('/test')).rejects.toBeInstanceOf(ApiError)
    await expect(request.get('/test')).rejects.toThrow('参数错误')
    expect(ElMessage.error).toHaveBeenCalledWith('参数错误')
  })

  it('HTTP 异常时提示后端 message', async () => {
    useAdapter(() => Promise.reject({ response: { data: { message: '服务异常' } } }))

    await expect(request.get('/test')).rejects.toThrow('服务异常')
    expect(ElMessage.error).toHaveBeenCalledWith('服务异常')
  })

  it('没有后端 message 时使用默认网络错误提示', async () => {
    useAdapter(() => Promise.reject(new Error('Network Error')))

    await expect(request.get('/test')).rejects.toThrow('网络错误，请稍后重试')
    expect(ElMessage.error).toHaveBeenCalledWith('网络错误，请稍后重试')
  })

  it('非 Result 结构的响应原样返回', async () => {
    useAdapter(() => responseWith('plain text'))

    await expect(request.get('/test')).resolves.toBe('plain text')
  })
})
