import { createRouter, createWebHashHistory } from 'vue-router'
import DatabaseQuery from '../views/DatabaseQuery.vue'
import DataSourceManage from '../views/DataSourceManage.vue'
import TableRelationManage from '../views/TableRelationManage.vue'
import MermaidERViewer from '../views/MermaidERViewer.vue'

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
      // 兼容旧书签（如已下线的 JSON 格式化、SQL 生成页面）
      path: '/:pathMatch(.*)*',
      redirect: '/database-query'
    }
  ]
})

export default router
