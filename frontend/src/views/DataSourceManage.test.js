import { flushPromises, shallowMount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn()
  },
  ElMessageBox: {
    confirm: vi.fn()
  }
}))

vi.mock('../api/dataSource', () => ({
  listDataSources: vi.fn(),
  addDataSource: vi.fn(),
  updateDataSource: vi.fn(),
  deleteDataSource: vi.fn(),
  testDataSource: vi.fn()
}))

import { addDataSource, listDataSources } from '../api/dataSource'
import DataSourceManage from './DataSourceManage.vue'

const stubs = {
  'el-table': true,
  'el-table-column': true,
  'el-button': true,
  'el-button-group': true,
  'el-dialog': true,
  'el-form': true,
  'el-form-item': true,
  'el-input': true,
  'el-select': true,
  'el-option': true,
  'el-tag': true
}

const mountView = () => shallowMount(DataSourceManage, { global: { stubs } })

const postgresRow = {
  id: 1,
  databaseType: 'POSTGRESQL',
  host: '127.0.0.1',
  username: 'postgres',
  password: 'postgres',
  database: 'shop',
  schemaName: 'sales',
  port: 5433
}

describe('DataSourceManage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    listDataSources.mockResolvedValue([])
  })

  it('默认使用 MySQL 与 3306 端口', async () => {
    const wrapper = mountView()
    await flushPromises()

    expect(wrapper.vm.formData.databaseType).toBe('MYSQL')
    expect(wrapper.vm.formData.port).toBe('3306')
    expect(wrapper.vm.formData.schemaName).toBe('public')
  })

  it('切换到 PostgreSQL 时自动切换默认端口', async () => {
    const wrapper = mountView()
    await flushPromises()

    wrapper.vm.formData.databaseType = 'POSTGRESQL'
    wrapper.vm.handleTypeChange('POSTGRESQL')
    expect(wrapper.vm.formData.port).toBe('5432')

    wrapper.vm.formData.databaseType = 'MYSQL'
    wrapper.vm.handleTypeChange('MYSQL')
    expect(wrapper.vm.formData.port).toBe('3306')
  })

  it('用户手动填写的端口不会被类型切换覆盖', async () => {
    const wrapper = mountView()
    await flushPromises()

    wrapper.vm.formData.port = '15432'
    wrapper.vm.handleTypeChange('POSTGRESQL')
    expect(wrapper.vm.formData.port).toBe('15432')
  })

  it('编辑 PostgreSQL 数据源时回填类型与 schema', async () => {
    const wrapper = mountView()
    await flushPromises()

    wrapper.vm.openDialog(postgresRow)

    expect(wrapper.vm.isEdit).toBe(true)
    expect(wrapper.vm.currentId).toBe(1)
    expect(wrapper.vm.formData.databaseType).toBe('POSTGRESQL')
    expect(wrapper.vm.formData.schemaName).toBe('sales')
    expect(wrapper.vm.dialogVisible).toBe(true)
  })

  it('历史数据缺少类型时按 MySQL 处理', async () => {
    const wrapper = mountView()
    await flushPromises()

    wrapper.vm.openDialog({ ...postgresRow, databaseType: undefined, schemaName: undefined })

    expect(wrapper.vm.formData.databaseType).toBe('MYSQL')
    expect(wrapper.vm.formData.schemaName).toBe('public')
  })

  it('提交时带上数据库类型与 schema', async () => {
    const wrapper = mountView()
    await flushPromises()

    wrapper.vm.formRef = { validate: vi.fn().mockResolvedValue(true) }
    wrapper.vm.formData = { ...postgresRow }
    await wrapper.vm.handleSubmit()

    expect(addDataSource).toHaveBeenCalledWith(
      expect.objectContaining({
        databaseType: 'POSTGRESQL',
        schemaName: 'sales',
        database: 'shop'
      })
    )
  })
})
