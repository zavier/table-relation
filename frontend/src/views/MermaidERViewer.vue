<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import mermaid from 'mermaid'

// 拖拽相关状态
const isDragging = ref(false)
const startX = ref(0)
const startY = ref(0)
const translateX = ref(0)
const translateY = ref(0)

const schemas = ref([])
const selectedSchema = ref('')
const tables = ref([])
const selectedTable = ref('')
const erDiagram = ref('')
const loading = ref(false)
const scale = ref(1.2) // 添加缩放比例状态
const MIN_SCALE = 0.4 // 添加最小缩放限制
const needTableInfo = ref(true) // 添加表信息显示选项

// 初始化mermaid
mermaid.initialize({
  startOnLoad: false,  // 修改为false，手动控制渲染时机
  theme: 'default',
  securityLevel: 'loose',
  fontSize: 16,
  flowchart: {
    htmlLabels: true,
    curve: 'basis'
  }
})

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
    erDiagram.value = ''
  } catch (error) {
    console.error('获取表失败:', error)
    ElMessage.error('获取表失败')
    tables.value = []
  }
}

// 生成ER图
const generateERDiagram = async () => {
  if (!selectedSchema.value || !selectedTable.value) {
    ElMessage.warning('请先选择数据库和表')
    return
  }

  // 重置缩放和位置
  scale.value = 1.2
  translateX.value = 0
  translateY.value = 0

  loading.value = true
  try {
    const response = await axios.get('/api/table/relation/erDiagram', {
      params: {
        schema: selectedSchema.value,
        tableName: selectedTable.value,
        needTableInfo: needTableInfo.value
      }
    })

    if (response.data.success) {
      erDiagram.value = response.data.data
      // 清除之前的图表
      const container = document.querySelector('#mermaid-diagram')
      if (!container) {
        throw new Error('找不到图表容器元素')
      }
      container.innerHTML = ''

      // 创建新的图表容器
      const mermaidDiv = document.createElement('div')
      mermaidDiv.className = 'mermaid'
      mermaidDiv.textContent = erDiagram.value
      container.appendChild(mermaidDiv)

      // 使用 try-catch 包裹渲染过程
      try {
        await mermaid.run()
      } catch (renderError) {
        console.error('Mermaid渲染错误:', renderError)
        throw new Error('图表渲染失败')
      }
    } else {
      ElMessage.error(response.data.message || '生成ER图失败')
    }
  } catch (error) {
    console.error('生成ER图失败:', error)
    ElMessage.error(error.message || '生成ER图失败')
  } finally {
    loading.value = false
  }
}

// 增加缩放功能
const zoomIn = () => {
  scale.value += 0.2
}

// 缩小功能
const zoomOut = () => {
  if (scale.value > MIN_SCALE) {
    scale.value -= 0.2
  }
}

// 复制Mermaid代码
const copyMermaidCode = async () => {
  if (!erDiagram.value) {
    ElMessage.warning('请先生成ER图')
    return
  }
  try {
    await navigator.clipboard.writeText(erDiagram.value)
    ElMessage.success('复制成功')
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败')
  }
}

// 拖拽相关方法
const handleMouseDown = (e) => {
  isDragging.value = true
  startX.value = e.clientX - translateX.value
  startY.value = e.clientY - translateY.value
}

const handleMouseMove = (e) => {
  if (!isDragging.value) return
  translateX.value = e.clientX - startX.value
  translateY.value = e.clientY - startY.value
}

const handleMouseUp = () => {
  isDragging.value = false
}

const handleMouseLeave = () => {
  isDragging.value = false
}

onMounted(() => {
  fetchSchemas()
})
</script>

<template>
  <div class="er-viewer">
    <h2>数据库ER图</h2>
    
    <div class="controls">
      <!-- Schema选择 -->
      <el-select
        v-model="selectedSchema"
        placeholder="选择数据库Schema"
        class="control-item"
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
        class="control-item"
        :disabled="!selectedSchema"
        filterable
      >
        <el-option
          v-for="table in tables"
          :key="table.value"
          :label="table.label"
          :value="table.value"
        />
      </el-select>

      <!-- 表信息显示选项 -->
      <el-switch
        v-model="needTableInfo"
        class="control-item"
        active-text="显示表信息"
        inactive-text="隐藏表信息"
      />

      <!-- 生成按钮 -->
      <el-button
        type="primary"
        @click="generateERDiagram"
        :loading="loading"
        :disabled="!selectedSchema || !selectedTable"
      >
        生成ER图
      </el-button>

      <!-- 放大按钮 -->
      <el-button
        type="primary"
        @click="zoomIn"
        :disabled="!erDiagram"
      >
        放大
      </el-button>

      <!-- 缩小按钮 -->
      <el-button
        type="primary"
        @click="zoomOut"
        :disabled="!erDiagram"
      >
        缩小
      </el-button>

      <!-- 复制按钮 -->
      <el-button
        type="primary"
        @click="copyMermaidCode"
        :disabled="!erDiagram"
      >
        复制代码
      </el-button>
    </div>

    <!-- ER图展示区域 -->
    <div class="diagram-container">
      <div
        id="mermaid-diagram"
        class="mermaid-diagram"
        :style="{
          transform: `translate(${translateX}px, ${translateY}px) scale(${scale})`,
          cursor: isDragging ? 'grabbing' : 'grab'
        }"
        @mousedown="handleMouseDown"
        @mousemove="handleMouseMove"
        @mouseup="handleMouseUp"
        @mouseleave="handleMouseLeave"
      ></div>
    </div>
  </div>
</template>

<style scoped>
.er-viewer {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.controls {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
}

.control-item {
  width: 200px;
}

.diagram-container {
  margin-top: 20px;
  padding: 20px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.mermaid-diagram {
  width: 100%;
  min-height: 600px;
  display: flex;
  justify-content: center;
  align-items: center;
  transform-origin: center center;
  user-select: none;
  touch-action: none;
}
</style>