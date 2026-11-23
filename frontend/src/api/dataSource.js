import request from './request'

/** 数据源列表 */
export const listDataSources = () => request.get('/table/datasource/list')

/** 新增数据源 */
export const addDataSource = (dataSource) =>
  request.post('/table/datasource/add', dataSource)

/** 更新数据源 */
export const updateDataSource = (dataSource) =>
  request.post('/table/datasource/update', dataSource)

/** 删除数据源 */
export const deleteDataSource = (id) =>
  request.post(`/table/datasource/delete/${id}`)

/** 测试数据源连接，返回 boolean */
export const testDataSource = (dataSource) =>
  request.post('/table/datasource/test', dataSource)
