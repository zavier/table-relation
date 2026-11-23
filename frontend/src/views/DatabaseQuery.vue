<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import VueJsonPretty from 'vue-json-pretty'
import 'vue-json-pretty/lib/styles.css'
import { ElMessage } from 'element-plus'

const schemas = ref([])
const selectedSchema = ref('')
const tables = ref([])
const selectedTable = ref('')
const fields = ref([])
const queryConditions = ref([])
const queryResult = ref(null)
const mainTableData = ref(null)
const relatedTablesData = ref({})
const mainTableDisplayMode = ref('table') // 主表展示模式：'json' 或 'table'
const relatedTablesDisplayMode = ref('table') // 关联表展示模式：'json' 或 'table'
const mainTableComments = ref({}) // 主表字段注释
const relatedTablesComments = ref({}) // 关联表字段注释


// 切换主表展示模式
const toggleMainTableMode = () => {
  mainTableDisplayMode.value = mainTableDisplayMode.value === 'json' ? 'table' : 'json'
}

// 切换关联表展示模式
const toggleRelatedTablesMode = () => {
  relatedTablesDisplayMode.value = relatedTablesDisplayMode.value === 'json' ? 'table' : 'json'
}

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
      schemas.value = []
    }
  } catch (error) {
    console.error('获取schema失败:', error)
    ElMessage.error('获取schema失败')
    schemas.value = []
  }
}

// 获取表列表
const fetchTables = async () => {
  if (!selectedSchema.value) return
  try {
    const response = await axios.get(`/api/table/schemaTables`, {
      params: {
        schema: selectedSchema.value
      }
    })
    if (response.data.success) {
      tables.value = response.data.data.map((tableName) => ({
        value: tableName,
        label: tableName
      }))
    } else {
      ElMessage.error(response.data.message)
      tables.value = []
    }
    selectedTable.value = ''
    fields.value = []
    queryConditions.value = []
  } catch (error) {
    console.error('获取表失败:', error)
    ElMessage.error('获取表失败')
    tables.value = []
  }
}

// 获取字段列表
const fetchFields = async () => {
  if (!selectedTable.value) return
  try {
    const response = await axios.get('/api/table/tableColumns', {
      params: {
        schema: selectedSchema.value,
        tableName: selectedTable.value
      }
    })
    if (response.data.success) {
      // 将返回的字符串数组转换为对象数组格式，以适配现有的组件结构
      fields.value = response.data.data.map(fieldName => ({
        id: fieldName,
        name: fieldName
      }))
    } else {
      ElMessage.error(response.data.message)
      fields.value = []
    }
    queryConditions.value = []
  } catch (error) {
    console.error('获取字段失败:', error)
    ElMessage.error('获取字段失败')
    fields.value = []
  }
}

// 添加查询条件
const addCondition = () => {
  queryConditions.value.push({
    field: '',
    operator: '=',
    value: ''
  })
}

// 移除查询条件
const removeCondition = (index) => {
  queryConditions.value.splice(index, 1)
}

// 复制SQL结果数据
const copySqlResult = async () => {
  try {
    const response = await axios.post('/api/table/generateInsertSql', {
      schema: selectedSchema.value,
      table: selectedTable.value,
      conditions: queryConditions.value
    })
    if (response.data.success) {
      const jsonStr = JSON.stringify(response.data.data, null, 2)
      await navigator.clipboard.writeText(jsonStr)
      ElMessage.success('复制成功')
    } else {
      ElMessage.error(response.data.message || '生成SQL失败')
    }
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败')
  }
}

// 执行查询
const executeQuery = async () => {
  try {
    const response = await axios.post('/api/table/sqlQuery', {
      schema: selectedSchema.value,
      table: selectedTable.value,
      conditions: queryConditions.value
    })
    if (response.data.success) {
      const { tableData, comments } = response.data.data
      queryResult.value = tableData
      // 分离主表数据和关联表数据
      mainTableData.value = tableData[selectedTable.value] || []
      mainTableComments.value = comments[selectedTable.value] || {}
      ElMessage.success('查询成功')
      
      // 处理关联表数据和注释
      relatedTablesData.value = Object.entries(tableData)
        .filter(([key]) => key !== selectedTable.value)
        .reduce((acc, [key, value]) => {
          acc[key] = value
          return acc
        }, {})
      
      relatedTablesComments.value = Object.entries(comments)
        .filter(([key]) => key !== selectedTable.value)
        .reduce((acc, [key, value]) => {
          acc[key] = value
          return acc
        }, {})
    } else {
      ElMessage.error(response.data.message)
      queryResult.value = null
      mainTableData.value = null
      relatedTablesData.value = {}
    }
  } catch (error) {
    console.error('查询执行失败:', error)
  }
}

// 复制查询结果数据
const copyQueryResult = () => {
  try {
    // 只复制tableData部分
    const tableDataOnly = Object.entries(queryResult.value).reduce((acc, [key, value]) => {
      acc[key] = value
      return acc
    }, {})
    const jsonStr = JSON.stringify(tableDataOnly, null, 2)
    navigator.clipboard.writeText(jsonStr)
    ElMessage.success('复制成功')
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败')
  }
}

onMounted(() => {
  fetchSchemas()
})
</script>

<template>
  <div class="database-query">
    <h2>数据库查询工具</h2>
    
    <!-- Schema选择 -->
    <el-select
      v-model="selectedSchema"
      placeholder="选择数据库Schema"
      class="query-select"
      @change="fetchTables"
      filterable
    >
      <el-option
        v-for="schema in schemas"
        :key="schema.value"
        :label="schema.label"
        :value="schema.value"
      />
    </el-select>

    <!-- 表选择 -->
    <el-select
      v-model="selectedTable"
      placeholder="选择表"
      class="query-select"
      :disabled="!selectedSchema"
      @change="fetchFields"
      filterable
    >
      <el-option
        v-for="table in tables"
        :key="table.value"
        :label="table.label"
        :value="table.value"
      />
    </el-select>

    <!-- 查询条件 -->
    <div class="query-conditions" v-if="selectedTable">
      
      <div v-for="(condition, index) in queryConditions" :key="index" class="condition-row">
        <el-select v-model="condition.field" placeholder="选择字段" class="condition-field" filterable>
          <el-option
            v-for="field in fields"
            :key="field.id"
            :label="field.name"
            :value="field.id"
          />
        </el-select>
        
        <el-select v-model="condition.operator" class="condition-operator">
          <el-option label="等于" value="=" />
          <el-option label="不等于" value="!=" />
          <el-option label="大于" value=">" />
          <el-option label="小于" value="<" />
          <el-option label="LIKE" value="LIKE" />
        </el-select>
        
        <el-input v-model="condition.value" placeholder="输入值" class="condition-value" />
        
        <el-button type="danger" @click="removeCondition(index)">删除</el-button>
      </div>

      <div class="query-actions">
        <el-button @click="addCondition">添加条件</el-button>
        <el-button type="primary" @click="executeQuery" :disabled="!queryConditions.length">
          执行查询
        </el-button>
        <el-button type="info" @click="copyQueryResult" plain>复制JSON结果</el-button>
        <el-button type="info" @click="copySqlResult" plain>复制SQL结果</el-button>
      </div>
    </div>

    <!-- 查询结果 -->
    <div v-if="queryResult" class="query-result">
      <div class="result-header">
        <h3>查询结果</h3>
      </div>
      
      <!-- 主表数据 -->
      <div class="main-table-section">
        <div class="section-header">
          <h4>{{ selectedTable }} (主表)</h4>
          <el-button type="primary" size="small" @click="toggleMainTableMode">
            切换为{{ mainTableDisplayMode === 'json' ? '表格' : 'JSON' }}模式
          </el-button>
        </div>
        <el-card class="result-card">
          <template v-if="mainTableDisplayMode === 'json'">
            <vue-json-pretty
              :data="mainTableData"
              :deep="2"
              :show-double-quotes="true"
              :show-length="true"
              :show-line="true"
              class="json-viewer"
            />
          </template>
          <template v-else>
            <el-table :data="mainTableData" border stripe>
              <el-table-column
                v-for="(value, key) in mainTableData?.[0] || {}"
                :key="key"
                :prop="key"
                :label="key"
                sortable
                show-overflow-tooltip
              >
                <template #header>
                  <el-tooltip
                    :content="mainTableComments[key] || '暂无注释'"
                    placement="top"
                    effect="light"
                  >
                    <span>{{ key }}</span>
                  </el-tooltip>
                </template>
                <template #default="scope">
                  <div class="cell-content">
                    {{ scope.row[key] }}
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </template>
        </el-card>
      </div>

      <!-- 关联表数据 -->
      <div v-if="Object.keys(relatedTablesData).length" class="related-tables-section">
        <div class="section-header">
          <h4>关联表数据</h4>
          <el-button type="primary" size="small" @click="toggleRelatedTablesMode">
            切换为{{ relatedTablesDisplayMode === 'json' ? '表格' : 'JSON' }}模式
          </el-button>
        </div>
        <el-collapse>
          <el-collapse-item
            v-for="(data, tableName) in relatedTablesData"
            :key="tableName"
            :title="`${tableName} (${data.length}条)`"
          >
            <el-card class="result-card">
              <template v-if="relatedTablesDisplayMode === 'json'">
                <vue-json-pretty
                  :data="data"
                  :deep="2"
                  :show-double-quotes="true"
                  :show-length="true"
                  :show-line="true"
                  class="json-viewer"
                />
              </template>
              <template v-else>
                <el-table :data="data" border stripe>
                  <el-table-column
                    v-for="(value, key) in data?.[0] || {}"
                    :key="key"
                    :prop="key"
                    :label="key"
                    sortable
                    show-overflow-tooltip
                  >
                    <template #header>
                      <el-tooltip
                        :content="relatedTablesComments[tableName]?.[key] || '暂无注释'"
                        placement="top"
                        effect="light"
                      >
                        <span>{{ key }}</span>
                      </el-tooltip>
                    </template>
                  </el-table-column>
                </el-table>
              </template>
            </el-card>
          </el-collapse-item>
        </el-collapse>
      </div>
    </div>
  </div>
</template>

<style scoped>
.database-query {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.query-select {
  width: 200px;
  margin-right: 20px;
  margin-bottom: 20px;
}

.query-conditions {
  margin-top: 10px;
}

.condition-row {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  gap: 10px;
}

.condition-field {
  width: 200px;
}

.condition-operator {
  width: 100px;
}

.condition-value {
  width: 200px;
}

.query-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}

.query-result {
  margin-top: 20px;
}

.result-card {
  margin-top: 10px;
  background-color: #f8f9fa;
}

.result-card .json-viewer {
  margin: 0;
  background: transparent;
}

.vjs-tree {
  font-size: 14px !important;
}

.vjs-tree .vjs-value {
  color: #409EFF !important;
}

.vjs-tree .vjs-key {
  color: #67C23A !important;
}

.main-table-section {
  margin-bottom: 20px;
}

.main-table-section h4,
.related-tables-section h4 {
  margin-bottom: 10px;
  color: #606266;
}

.related-tables-section .el-collapse {
  border: none;
}

.related-tables-section .el-collapse-item__header {
  font-size: 16px;
  color: #409EFF;
}

.related-tables-section .el-collapse-item__content {
  padding: 10px;
}
</style>