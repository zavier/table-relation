<template>
  <div class="nl-to-sql-view">
    <div class="control-panel">
      <div class="schema-select">
        <div class="select-row">
          <label for="schemaSelect">选择数据库：</label>
          <select 
            id="schemaSelect" 
            v-model="selectedSchema"
            class="form-select"
          >
            <option value="">请选择数据库</option>
            <option 
              v-for="schema in schemas" 
              :key="schema.value" 
              :value="schema.value"
            >
              {{ schema.label }}
            </option>
          </select>
        </div>
      </div>
      
      <div class="demand-input">
        <label for="demandText">需求描述：</label>
        <textarea
          id="demandText"
          v-model="demand"
          placeholder="请输入您的需求，例如：查询用户表中年龄大于20岁的用户信息"
          class="form-textarea"
        ></textarea>
      </div>
      
      <div class="action-area">
        <button 
          class="generate-btn"
          @click="generateSql"
          :disabled="!selectedSchema || !demand || loading"
        >
          <span v-if="!loading">生成 SQL</span>
          <span v-else class="loading-text">
            <i class="el-icon-loading"></i>
            正在生成中...
          </span>
        </button>
      </div>
    </div>
    
    <div class="result-panel" v-if="sqlResult || loading">
      <h3>
        <span v-if="!loading">生成的 SQL：</span>
        <span v-else class="generating-tip">
          <i class="el-icon-loading"></i>
          正在根据您的需求生成 SQL，这可能需要一些时间...
        </span>
      </h3>
      <div class="sql-display" v-if="sqlResult">
        <textarea
          v-model="sqlResult"
          class="sql-textarea"
          spellcheck="false"
        ></textarea>
        <div class="sql-actions">
          <button 
            class="copy-btn"
            @click="copySql"
            title="复制到剪贴板"
          >
            复制
          </button>
          <button 
            class="execute-btn"
            @click="executeSql"
            :disabled="executing"
          >
            <span v-if="!executing">执行</span>
            <span v-else class="loading-text">
              <i class="el-icon-loading"></i>
              执行中...
            </span>
          </button>
        </div>
      </div>
      <div v-else-if="loading" class="loading-placeholder">
        <div class="loading-animation"></div>
      </div>
    </div>

    <!-- 查询结果表格 -->
    <div class="query-result" v-if="queryResult.length">
      <h3>查询结果：</h3>
      <div class="table-container">
        <el-table 
          :data="queryResult" 
          border 
          stripe
          style="width: 100%"
          max-height="400"
        >
          <el-table-column 
            v-for="col in tableColumns" 
            :key="col"
            :prop="col"
            :label="col"
          />
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const selectedSchema = ref('')
const demand = ref('')
const sqlResult = ref('')
const loading = ref(false)
const executing = ref(false)
const queryResult = ref([])
const tableColumns = ref([])

const schemas = ref([])

// 获取数据库schema列表
const fetchSchemas = async () => {
  try {
    const response = await axios.get('/api/table/allSchema')
    if (response.data.success) {
      schemas.value = response.data.data.map((schemaName) => ({
        value: schemaName,
        label: schemaName
      }))
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    console.error('获取schema失败:', error)
    ElMessage.error('获取schema失败')
  }
}

const generateSql = async () => {
  loading.value = true
  sqlResult.value = '' // 清空之前的结果
  queryResult.value = [] // 清空查询结果
  tableColumns.value = [] // 清空表格列
  
  try {
    const response = await axios.get('/api/table/generateSql', {
      params: {
        schema: selectedSchema.value,
        demand: demand.value
      }
    })

    if (response.data.success) {
      sqlResult.value = response.data.data
    } else {
      ElMessage.error(response.data.message || '生成 SQL 失败')
    }
  } catch (error) {
    ElMessage.error('生成 SQL 失败：' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}

const executeSql = async () => {
  executing.value = true
  try {
    const response = await axios.post('/api/table/executeSql', {
      schema: selectedSchema.value,
      sql: sqlResult.value
    })

    if (response.data.success) {
      const data = response.data.data
      if (data.length) {
        // 从第一条数据获取列名
        tableColumns.value = Object.keys(data[0])
        queryResult.value = data
      } else {
        ElMessage.info('查询结果为空')
      }
    } else {
      ElMessage.error(response.data.message || '执行 SQL 失败')
    }
  } catch (error) {
    ElMessage.error('执行 SQL 失败：' + (error.response?.data?.message || error.message))
  } finally {
    executing.value = false
  }
}

const copySql = async () => {
  try {
    await navigator.clipboard.writeText(sqlResult.value)
    ElMessage.success('SQL 已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败：' + error.message)
  }
}

onMounted(() => {
  fetchSchemas()
})
</script>

<style scoped>
.nl-to-sql-view {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.control-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
  background-color: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #dee2e6;
}

.schema-select {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.select-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.select-row label {
  min-width: 100px;
  margin: 0;
}

.form-select {
  padding: 8px;
  border: 1px solid #ced4da;
  border-radius: 4px;
  font-size: 14px;
  width: 300px;
}

.demand-input {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-textarea {
  padding: 12px;
  border: 1px solid #ced4da;
  border-radius: 4px;
  font-size: 14px;
  min-height: 100px;
  width: 100%;
  resize: vertical;
  font-family: inherit;
}

.action-area {
  display: flex;
  justify-content: flex-start;
}

.generate-btn {
  min-width: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background-color: #007bff;
  color: white;
  padding: 8px 16px;
  border: 1px solid #0056b3;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.generate-btn:hover:not(:disabled) {
  background-color: #0056b3;
  border-color: #004085;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
  transform: translateY(-1px);
}

.generate-btn:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.generate-btn:disabled {
  background-color: #6c757d;
  border-color: #5a6268;
  cursor: not-allowed;
  opacity: 0.65;
  box-shadow: none;
}

.result-panel {
  background-color: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #dee2e6;
}

.result-panel h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  color: #333;
}

.sql-display {
  position: relative;
  background-color: #fff;
  padding: 16px;
  border-radius: 4px;
  border: 1px solid #dee2e6;
}

.sql-display pre {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: monospace;
}

.sql-actions {
  position: absolute;
  top: 8px;
  right: 8px;
  display: flex;
  gap: 8px;
}

.copy-btn,
.execute-btn {
  padding: 4px 12px;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.copy-btn {
  background-color: #6c757d;
}

.execute-btn {
  background-color: #28a745;
}

.copy-btn:hover {
  background-color: #5a6268;
}

.execute-btn:hover {
  background-color: #218838;
}

.execute-btn:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}

.query-result {
  background-color: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #dee2e6;
  margin-top: 20px;
}

.query-result h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  color: #333;
}

.table-container {
  background-color: #fff;
  border-radius: 4px;
  overflow: hidden;
}

/* 自定义 element-plus 表格样式 */
:deep(.el-table) {
  --el-table-border-color: #dee2e6;
  --el-table-header-bg-color: #f8f9fa;
}

:deep(.el-table th) {
  background-color: #f8f9fa;
  font-weight: 600;
}

:deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background-color: #f8f9fa;
}

label {
  font-weight: 500;
  color: #495057;
}

.loading-text {
  display: flex;
  align-items: center;
  gap: 8px;
}

.generating-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 14px;
}

.loading-placeholder {
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fff;
  border-radius: 4px;
  padding: 20px;
}

.loading-animation {
  width: 40px;
  height: 40px;
  border: 3px solid #f3f3f3;
  border-radius: 50%;
  border-top: 3px solid #007bff;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.el-icon-loading {
  animation: spin 1s linear infinite;
}

/* 添加一个渐变动画效果 */
.result-panel {
  transition: all 0.3s ease;
}

.sql-display {
  transition: all 0.3s ease;
}
.sql-textarea {
  width: 100%;
  min-height: 120px;
  padding: 12px;
  margin: 0;
  border: 1px solid #dee2e6;
  border-radius: 4px;
  font-family: monospace;
  font-size: 14px;
  line-height: 1.5;
  resize: vertical;
  background-color: #fff;
  color: #333;
}

.sql-textarea:focus {
  outline: none;
  border-color: #80bdff;
  box-shadow: 0 0 0 0.2rem rgba(0,123,255,.25);
}
</style>