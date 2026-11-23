import request from './request'

/** 获取所有 schema */
export const getAllSchemas = () => request.get('/table/allSchema')

/** 获取 schema 下的表列表 */
export const getSchemaTables = (schema) =>
  request.get('/table/schemaTables', { params: { schema } })

/** 获取表字段列表 */
export const getTableColumns = (schema, tableName) =>
  request.get('/table/tableColumns', { params: { schema, tableName } })
