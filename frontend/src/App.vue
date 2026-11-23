<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <el-container class="app-container">
    <el-aside width="200px" class="aside">
      <el-menu
        mode="vertical"
        class="menu"
        :default-active="$route.path"
        router
      >
        <el-menu-item index="/database-query">
          <el-icon><Search /></el-icon>
          <span>数据库查询</span>
        </el-menu-item>
        <el-menu-item index="/datasource-manage">
          <el-icon><Setting /></el-icon>
          <span>数据源管理</span>
        </el-menu-item>
        <el-menu-item index="/table-relation-manage">
          <el-icon><Connection /></el-icon>
          <span>表关系管理</span>
        </el-menu-item>
        <el-menu-item index="/er-diagram">
          <el-icon><Share /></el-icon>
          <span>ER图展示</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <el-main class="main">
      <router-view />
    </el-main>
  </el-container>
</template>

<!-- 全局样式 -->
<style>
body {
  color: var(--el-text-color-primary);
  background-color: var(--el-bg-color-page);
}
</style>

<style scoped>
.app-container {
  height: 100vh;
}

.aside {
  background-color: #304156;
  height: 100vh;
}

.menu {
  border-right: none;
  background-color: transparent;
}

:deep(.el-menu-item) {
  color: #bfcbd9;
}

:deep(.el-menu-item.is-active) {
  color: #409EFF;
  background-color: #263445;
}

:deep(.el-menu-item:hover) {
  background-color: #263445;
}

.main {
  background-color: var(--el-bg-color-page);
  padding: 20px;
  height: 100vh;
  overflow-y: auto;
}
</style>
