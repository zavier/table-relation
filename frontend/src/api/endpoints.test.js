import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('element-plus', () => ({
  ElMessage: {
    error: vi.fn()
  }
}))

import request from './request'
import { getAllSchemas, getSchemaTables, getTableColumns } from './meta'
import {
  addDataSource,
  deleteDataSource,
  listDataSources,
  testDataSource,
  updateDataSource
} from './dataSource'
import { generateInsertSql, queryTable } from './query'
import {
  addRelation,
  deleteRelation,
  getErDiagram,
  listRelations,
  updateRelation
} from './relation'

let calls

beforeEach(() => {
  calls = []
  request.defaults.adapter = async (config) => {
    calls.push({
      url: config.url,
      method: config.method,
      params: config.params,
      data: typeof config.data === 'string' ? JSON.parse(config.data) : config.data
    })
    return {
      data: { success: true, data: null },
      status: 200,
      statusText: 'OK',
      headers: {},
      config
    }
  }
})

const lastCall = () => calls[calls.length - 1]

describe('meta 接口', () => {
  it('getAllSchemas', async () => {
    await getAllSchemas()
    expect(lastCall()).toMatchObject({ url: '/table/allSchema', method: 'get' })
  })

  it('getSchemaTables', async () => {
    await getSchemaTables('demo')
    expect(lastCall()).toMatchObject({
      url: '/table/schemaTables',
      method: 'get',
      params: { schema: 'demo' }
    })
  })

  it('getTableColumns', async () => {
    await getTableColumns('demo', 'users')
    expect(lastCall()).toMatchObject({
      url: '/table/tableColumns',
      method: 'get',
      params: { schema: 'demo', tableName: 'users' }
    })
  })
})

describe('数据源接口', () => {
  it('listDataSources', async () => {
    await listDataSources()
    expect(lastCall()).toMatchObject({ url: '/table/datasource/list', method: 'get' })
  })

  it('addDataSource', async () => {
    await addDataSource({ host: 'localhost' })
    expect(lastCall()).toMatchObject({
      url: '/table/datasource/add',
      method: 'post',
      data: { host: 'localhost' }
    })
  })

  it('updateDataSource', async () => {
    await updateDataSource({ id: 1, host: 'localhost' })
    expect(lastCall()).toMatchObject({
      url: '/table/datasource/update',
      method: 'post',
      data: { id: 1, host: 'localhost' }
    })
  })

  it('deleteDataSource', async () => {
    await deleteDataSource(3)
    expect(lastCall()).toMatchObject({ url: '/table/datasource/delete/3', method: 'post' })
  })

  it('testDataSource', async () => {
    await testDataSource({ host: 'localhost' })
    expect(lastCall()).toMatchObject({
      url: '/table/datasource/test',
      method: 'post',
      data: { host: 'localhost' }
    })
  })
})

describe('查询接口', () => {
  it('queryTable', async () => {
    await queryTable({ schema: 'demo', table: 'users', conditions: [] })
    expect(lastCall()).toMatchObject({
      url: '/table/sqlQuery',
      method: 'post',
      data: { schema: 'demo', table: 'users', conditions: [] }
    })
  })

  it('generateInsertSql', async () => {
    await generateInsertSql({ schema: 'demo', table: 'users', conditions: [] })
    expect(lastCall()).toMatchObject({
      url: '/table/generateInsertSql',
      method: 'post',
      data: { schema: 'demo', table: 'users', conditions: [] }
    })
  })
})

describe('表关系接口', () => {
  it('listRelations', async () => {
    await listRelations()
    expect(lastCall()).toMatchObject({ url: '/table/relation/list', method: 'get' })
  })

  it('addRelation', async () => {
    await addRelation({ tableName: 'users' })
    expect(lastCall()).toMatchObject({
      url: '/table/relation/add',
      method: 'post',
      data: { tableName: 'users' }
    })
  })

  it('updateRelation', async () => {
    await updateRelation({ id: 1, tableName: 'users' })
    expect(lastCall()).toMatchObject({
      url: '/table/relation/update',
      method: 'post',
      data: { id: 1, tableName: 'users' }
    })
  })

  it('deleteRelation', async () => {
    await deleteRelation(5)
    expect(lastCall()).toMatchObject({ url: '/table/relation/delete/5', method: 'post' })
  })

  it('getErDiagram', async () => {
    await getErDiagram({ schema: 'demo', tableName: 'users', needTableInfo: true })
    expect(lastCall()).toMatchObject({
      url: '/table/relation/erDiagram',
      method: 'get',
      params: { schema: 'demo', tableName: 'users', needTableInfo: true }
    })
  })
})
