import { flushPromises, shallowMount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn()
  }
}))

vi.mock('../api/meta', () => ({
  getAllSchemas: vi.fn().mockResolvedValue(['db1']),
  getSchemaTables: vi.fn().mockResolvedValue(['t1']),
  getTableColumns: vi.fn().mockResolvedValue(['id', 'name'])
}))

vi.mock('../api/query', () => ({
  queryTable: vi.fn(),
  generateInsertSql: vi.fn()
}))

import DatabaseQuery from './DatabaseQuery.vue'
import { queryTable } from '../api/query'

const stubs = {
  'el-select': true,
  'el-option': true,
  'el-button': true,
  'el-input': true,
  'el-card': true,
  'el-table': true,
  'el-table-column': true,
  'el-tooltip': true,
  'el-collapse': true,
  'el-collapse-item': true,
  'vue-json-pretty': true
}

const mountView = () => shallowMount(DatabaseQuery, { global: { stubs } })

describe('DatabaseQuery', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('初始化时加载 schema 列表', async () => {
    const wrapper = mountView()
    await flushPromises()

    expect(wrapper.vm.schemas).toEqual([{ value: 'db1', label: 'db1' }])
  })

  it('选择 schema 后加载表，选择表后加载字段', async () => {
    const wrapper = mountView()
    await flushPromises()

    wrapper.vm.selectedSchema = 'db1'
    await wrapper.vm.fetchTables()
    expect(wrapper.vm.tables).toEqual([{ value: 't1', label: 't1' }])

    wrapper.vm.selectedTable = 't1'
    await wrapper.vm.fetchFields()
    expect(wrapper.vm.fields).toEqual([
      { id: 'id', name: 'id' },
      { id: 'name', name: 'name' }
    ])
  })

  it('执行查询后拆分主表与关联表数据', async () => {
    queryTable.mockResolvedValue({
      tableData: {
        t1: [{ id: 1 }],
        rel: [{ id: 2 }]
      },
      comments: {
        t1: { id: '主键' },
        rel: { id: '关联主键' }
      }
    })

    const wrapper = mountView()
    await flushPromises()

    wrapper.vm.selectedSchema = 'db1'
    wrapper.vm.selectedTable = 't1'
    wrapper.vm.queryConditions = [{ field: 'id', operator: '=', value: '1' }]
    await wrapper.vm.executeQuery()

    expect(queryTable).toHaveBeenCalledWith({
      schema: 'db1',
      table: 't1',
      conditions: [{ field: 'id', operator: '=', value: '1' }]
    })
    expect(wrapper.vm.mainTableData).toEqual([{ id: 1 }])
    expect(wrapper.vm.mainTableComments).toEqual({ id: '主键' })
    expect(wrapper.vm.relatedTablesData).toEqual({ rel: [{ id: 2 }] })
    expect(wrapper.vm.relatedTablesComments).toEqual({ rel: { id: '关联主键' } })
  })
})
