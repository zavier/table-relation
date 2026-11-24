import request from './request'

/** 表关系列表 */
export const listRelations = () => request.get('/table/relation/list')

/** 新增表关系 */
export const addRelation = (relation) => request.post('/table/relation/add', relation)

/** 更新表关系 */
export const updateRelation = (relation) => request.post('/table/relation/update', relation)

/** 删除表关系 */
export const deleteRelation = (id) => request.post(`/table/relation/delete/${id}`)

/**
 * 获取 ER 图（mermaid 文本）
 * @param {{ schema: string, tableName: string, needTableInfo: boolean }} params
 */
export const getErDiagram = (params) => request.get('/table/relation/erDiagram', { params })
