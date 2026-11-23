import { createRouter, createWebHashHistory } from 'vue-router'
import DatabaseQuery from '../views/DatabaseQuery.vue'
import DataSourceManage from '../views/DataSourceManage.vue'
import TableRelationManage from '../views/TableRelationManage.vue'
import JsonFormatter from '../views/JsonFormatter.vue'
import MermaidERViewer from '../views/MermaidERViewer.vue'
import NlToSqlView from '../views/NlToSqlView.vue'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      redirect: '/database-query'
    },
    {
      path: '/database-query',
      name: 'DatabaseQuery',
      component: DatabaseQuery
    },
    {
      path: '/datasource-manage',
      name: 'DataSourceManage',
      component: DataSourceManage
    },
    {
      path: '/table-relation-manage',
      name: 'TableRelationManage',
      component: TableRelationManage
    },
    {
        path: '/er-diagram',
        name: 'ERDiagram',
        component: MermaidERViewer
    },
    {
      path: '/json-formatter',
      name: 'JsonFormatter',
      component: JsonFormatter
    },
    {
      path: '/nl-to-sql',
      name: 'NlToSql',
      component: NlToSqlView
    }
  ]
})

export default router