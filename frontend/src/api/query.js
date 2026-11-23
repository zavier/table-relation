import request from './request'

/**
 * 按条件查询表数据
 * @param {{ schema: string, table: string, conditions: Array }} query
 * @returns {Promise<{ tableData: Object, comments: Object }>}
 */
export const queryTable = (query) => request.post('/table/sqlQuery', query)

/**
 * 根据当前查询条件生成 INSERT SQL
 * @param {{ schema: string, table: string, conditions: Array }} query
 */
export const generateInsertSql = (query) =>
  request.post('/table/generateInsertSql', query)
