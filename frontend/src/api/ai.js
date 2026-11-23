import request from './request'

/**
 * 根据自然语言生成 SQL
 * @param {{ schema: string, demand: string }} params
 */
export const generateSql = (params) => request.get('/table/generateSql', { params })

/**
 * 执行 SQL
 * @param {{ schema: string, sql: string }} payload
 * @returns {Promise<Array<Object>>}
 */
export const executeSql = (payload) => request.post('/table/executeSql', payload)
