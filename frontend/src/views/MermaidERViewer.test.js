import { flushPromises, shallowMount } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    warning: vi.fn(),
    error: vi.fn()
  }
}))

vi.mock('mermaid', () => ({
  default: {
    initialize: vi.fn(),
    run: vi.fn()
  }
}))

vi.mock('../api/meta', () => ({
  getAllSchemas: vi.fn(),
  getSchemaTables: vi.fn()
}))

vi.mock('../api/relation', () => ({
  getErDiagram: vi.fn()
}))

import { ElMessage } from 'element-plus'
import mermaid from 'mermaid'
import MermaidERViewer from './MermaidERViewer.vue'
import { getAllSchemas, getSchemaTables } from '../api/meta'
import { getErDiagram } from '../api/relation'
import { ApiError } from '../api/request'

const stubs = {
  'el-select': true,
  'el-option': true,
  'el-button': true,
  'el-switch': true
}

let wrapper

const mountView = () => {
  wrapper = shallowMount(MermaidERViewer, {
    attachTo: document.body,
    global: { stubs }
  })
  return wrapper
}

describe('MermaidERViewer', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.spyOn(console, 'error').mockImplementation(() => {})
    getAllSchemas.mockResolvedValue(['db1'])
    getSchemaTables.mockResolvedValue(['users', 'departments'])
    mermaid.run.mockResolvedValue(undefined)
  })

  afterEach(() => {
    wrapper?.unmount()
    wrapper = null
    vi.restoreAllMocks()
  })

  it('初始化时加载 schema 列表', async () => {
    mountView()
    await flushPromises()

    expect(wrapper.vm.schemas).toEqual([{ value: 'db1', label: 'db1' }])
  })

  it('选择 schema 后加载表列表并清空已生成图表', async () => {
    mountView()
    await flushPromises()

    wrapper.vm.selectedSchema = 'db1'
    wrapper.vm.erDiagram = 'old diagram'
    await wrapper.vm.fetchTables()

    expect(getSchemaTables).toHaveBeenCalledWith('db1')
    expect(wrapper.vm.tables).toEqual([
      { value: 'users', label: 'users' },
      { value: 'departments', label: 'departments' }
    ])
    expect(wrapper.vm.selectedTable).toBe('')
    expect(wrapper.vm.erDiagram).toBe('')
  })

  it('未选择数据库或表时提示且不请求接口', async () => {
    mountView()
    await flushPromises()

    await wrapper.vm.generateERDiagram()

    expect(ElMessage.warning).toHaveBeenCalledWith('请先选择数据库和表')
    expect(getErDiagram).not.toHaveBeenCalled()
  })

  it('生成 ER 图：请求接口、写入容器并调用 mermaid 渲染', async () => {
    const diagram = 'erDiagram\n  users ||--o{ departments : dept'
    getErDiagram.mockResolvedValue(diagram)

    mountView()
    await flushPromises()

    wrapper.vm.selectedSchema = 'db1'
    wrapper.vm.selectedTable = 'users'
    wrapper.vm.needTableInfo = true
    await wrapper.vm.generateERDiagram()

    expect(getErDiagram).toHaveBeenCalledWith({
      schema: 'db1',
      tableName: 'users',
      needTableInfo: true
    })
    expect(wrapper.vm.erDiagram).toBe(diagram)
    expect(mermaid.run).toHaveBeenCalledTimes(1)
    expect(wrapper.vm.loading).toBe(false)

    const container = document.querySelector('#mermaid-diagram')
    expect(container.querySelector('.mermaid').textContent).toBe(diagram)
  })

  it('接口异常时提示错误并结束 loading', async () => {
    getErDiagram.mockRejectedValue(new Error('生成失败'))

    mountView()
    await flushPromises()

    wrapper.vm.selectedSchema = 'db1'
    wrapper.vm.selectedTable = 'users'
    await wrapper.vm.generateERDiagram()

    expect(ElMessage.error).toHaveBeenCalledWith('生成失败')
    expect(wrapper.vm.loading).toBe(false)
  })

  it('ApiError 已由拦截器提示，不再重复弹错误', async () => {
    getErDiagram.mockRejectedValue(new ApiError('接口错误'))

    mountView()
    await flushPromises()

    wrapper.vm.selectedSchema = 'db1'
    wrapper.vm.selectedTable = 'users'
    await wrapper.vm.generateERDiagram()

    expect(ElMessage.error).not.toHaveBeenCalled()
    expect(wrapper.vm.loading).toBe(false)
  })

  it('mermaid 渲染失败时提示图表渲染失败', async () => {
    getErDiagram.mockResolvedValue('erDiagram')
    mermaid.run.mockRejectedValue(new Error('render error'))

    mountView()
    await flushPromises()

    wrapper.vm.selectedSchema = 'db1'
    wrapper.vm.selectedTable = 'users'
    await wrapper.vm.generateERDiagram()

    expect(ElMessage.error).toHaveBeenCalledWith('图表渲染失败')
    expect(wrapper.vm.loading).toBe(false)
  })
})
