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

vi.mock('../api/relation', () => ({
  listRelations: vi.fn(),
  addRelation: vi.fn(),
  updateRelation: vi.fn(),
  deleteRelation: vi.fn()
}))

vi.mock('../api/meta', () => ({
  getAllSchemas: vi.fn(),
  getSchemaTables: vi.fn(),
  getTableColumns: vi.fn()
}))

import { ElMessage, ElMessageBox } from 'element-plus'
import TableRelationManage from './TableRelationManage.vue'
import { addRelation, deleteRelation, listRelations, updateRelation } from '../api/relation'
import { getAllSchemas, getSchemaTables, getTableColumns } from '../api/meta'

const stubs = {
  'el-select': true,
  'el-option': true,
  'el-button': true,
  'el-button-group': true,
  'el-dialog': true,
  'el-form': true,
  'el-form-item': true,
  'el-input': true,
  'el-table': true,
  'el-table-column': true
}

const relation = {
  id: 1,
  tableSchema: 'db1',
  tableName: 'users',
  columnName: 'dept_id',
  referencedTableSchema: 'db1',
  referencedTableName: 'departments',
  referencedColumnName: 'id',
  relationType: 1,
  condition: ''
}

const mountView = () =>
  shallowMount(TableRelationManage, {
    global: { stubs }
  })

describe('TableRelationManage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    listRelations.mockResolvedValue([relation])
    getAllSchemas.mockResolvedValue(['db1', 'db2'])
    getSchemaTables.mockResolvedValue(['users', 'departments'])
    getTableColumns.mockResolvedValue(['id', 'dept_id'])
  })

  it('初始化时加载表关系与 schema 列表', async () => {
    const wrapper = mountView()
    await flushPromises()

    expect(wrapper.vm.relationList).toEqual([relation])
    expect(wrapper.vm.schemas).toEqual([
      { value: 'db1', label: 'db1' },
      { value: 'db2', label: 'db2' }
    ])
  })

  it('按 schema 过滤表关系列表', async () => {
    listRelations.mockResolvedValue([relation, { ...relation, id: 2, tableSchema: 'db2' }])
    const wrapper = mountView()
    await flushPromises()

    expect(wrapper.vm.filteredRelationList).toHaveLength(2)

    wrapper.vm.selectedSchema = 'db1'
    expect(wrapper.vm.filteredRelationList).toEqual([relation])
  })

  it('编辑时加载主表与关联表的表/字段选项', async () => {
    const wrapper = mountView()
    await flushPromises()

    await wrapper.vm.openDialog(relation)

    expect(wrapper.vm.isEdit).toBe(true)
    expect(wrapper.vm.currentId).toBe(1)
    expect(wrapper.vm.formData).toEqual(relation)
    expect(getSchemaTables).toHaveBeenCalledWith('db1')
    expect(getTableColumns).toHaveBeenCalledWith('db1', 'users')
    expect(getTableColumns).toHaveBeenCalledWith('db1', 'departments')
    expect(wrapper.vm.tables).toEqual([
      { value: 'users', label: 'users' },
      { value: 'departments', label: 'departments' }
    ])
    expect(wrapper.vm.referencedFields).toEqual([
      { value: 'id', label: 'id' },
      { value: 'dept_id', label: 'dept_id' }
    ])
    expect(wrapper.vm.dialogVisible).toBe(true)
  })

  it('表单校验失败时不提交', async () => {
    const wrapper = mountView()
    await flushPromises()

    wrapper.vm.formRef = { validate: vi.fn().mockRejectedValue(new Error('invalid')) }
    await wrapper.vm.handleSubmit()

    expect(addRelation).not.toHaveBeenCalled()
    expect(updateRelation).not.toHaveBeenCalled()
  })

  it('新增表关系成功后关闭弹窗并刷新列表', async () => {
    const wrapper = mountView()
    await flushPromises()

    wrapper.vm.formRef = { validate: vi.fn().mockResolvedValue(true) }
    wrapper.vm.formData = { ...relation, id: undefined }
    await wrapper.vm.handleSubmit()

    expect(addRelation).toHaveBeenCalledWith({ ...relation, id: undefined })
    expect(ElMessage.success).toHaveBeenCalledWith('表关系添加成功')
    expect(wrapper.vm.dialogVisible).toBe(false)
    expect(listRelations).toHaveBeenCalledTimes(2)
  })

  it('编辑表关系时调用更新接口', async () => {
    const wrapper = mountView()
    await flushPromises()

    wrapper.vm.formRef = { validate: vi.fn().mockResolvedValue(true) }
    wrapper.vm.isEdit = true
    wrapper.vm.formData = { ...relation }
    await wrapper.vm.handleSubmit()

    expect(updateRelation).toHaveBeenCalledWith(relation)
    expect(addRelation).not.toHaveBeenCalled()
    expect(ElMessage.success).toHaveBeenCalledWith('表关系更新成功')
  })

  it('确认后删除表关系并刷新列表', async () => {
    ElMessageBox.confirm.mockResolvedValue('confirm')
    const wrapper = mountView()
    await flushPromises()

    await wrapper.vm.handleDelete(1)

    expect(deleteRelation).toHaveBeenCalledWith(1)
    expect(ElMessage.success).toHaveBeenCalledWith('删除成功')
    expect(listRelations).toHaveBeenCalledTimes(2)
  })

  it('取消删除时不调用删除接口', async () => {
    ElMessageBox.confirm.mockRejectedValue('cancel')
    const wrapper = mountView()
    await flushPromises()

    await wrapper.vm.handleDelete(1)

    expect(deleteRelation).not.toHaveBeenCalled()
  })
})
