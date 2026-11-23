<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

// 表关系列表
const relationList = ref([])
const loading = ref(false)

// 数据源选项
const schemas = ref([])
const tables = ref([])
const fields = ref([])
const referencedTables = ref([])
const referencedFields = ref([])

// 筛选条件
const selectedSchema = ref('')

// 筛选后的表关系列表
const filteredRelationList = computed(() => {
  if (!selectedSchema.value) {
    return relationList.value
  }
  return relationList.value.filter(item => item.tableSchema === selectedSchema.value)
})

// 表单数据和规则
const formData = ref({
  tableSchema: '',
  tableName: '',
  columnName: '',
  referencedTableSchema: '',
  referencedTableName: '',
  referencedColumnName: '',
  relationType: 1,
  condition: ''
})

// 获取schema列表
const fetchSchemas = async () => {
  try {
    const response = await axios.get('/api/table/allSchema')
    if (response.data.success) {
      schemas.value = response.data.data.map((schemaName) => ({
        value: schemaName,
        label: schemaName
      }))
    } else {
      ElMessage.error(response.data.message || '获取数据库列表失败')
    }
  } catch (error) {
    ElMessage.error('获取数据库列表失败')
  }
}

// 获取表列表
const fetchTables = async (schema, isReferenced = false) => {
  if (!schema) return
  try {
    const response = await axios.get('/api/table/schemaTables', {
      params: { schema }
    })
    if (response.data.success) {
      const tableList = response.data.data.map((tableName) => ({
        value: tableName,
        label: tableName
      }))
      if (isReferenced) {
        referencedTables.value = tableList
      } else {
        tables.value = tableList
      }
    } else {
      ElMessage.error(response.data.message || '获取表列表失败')
    }
  } catch (error) {
    ElMessage.error('获取表列表失败')
  }
}

// 获取字段列表
const fetchFields = async (schema, table, isReferenced = false) => {
  if (!schema || !table) return
  try {
    const response = await axios.get('/api/table/tableColumns', {
      params: {
        schema,
        tableName: table
      }
    })
    if (response.data.success) {
      const fieldList = response.data.data.map((fieldName) => ({
        value: fieldName,
        label: fieldName
      }))
      if (isReferenced) {
        referencedFields.value = fieldList
      } else {
        fields.value = fieldList
      }
    } else {
      ElMessage.error(response.data.message || '获取字段列表失败')
    }
  } catch (error) {
    ElMessage.error('获取字段列表失败')
  }
}

// 监听主表schema变化
const handleSchemaChange = async () => {
  formData.value.tableName = ''
  formData.value.columnName = ''
  tables.value = []
  fields.value = []
  if (formData.value.tableSchema) {
    await fetchTables(formData.value.tableSchema)
  }
}

// 监听主表表名变化
const handleTableChange = async () => {
  formData.value.columnName = ''
  fields.value = []
  if (formData.value.tableSchema && formData.value.tableName) {
    await fetchFields(formData.value.tableSchema, formData.value.tableName)
  }
}

// 监听关联表schema变化
const handleReferencedSchemaChange = async () => {
  formData.value.referencedTableName = ''
  formData.value.referencedColumnName = ''
  referencedTables.value = []
  referencedFields.value = []
  if (formData.value.referencedTableSchema) {
    await fetchTables(formData.value.referencedTableSchema, true)
  }
}

// 监听关联表表名变化
const handleReferencedTableChange = async () => {
  formData.value.referencedColumnName = ''
  referencedFields.value = []
  if (formData.value.referencedTableSchema && formData.value.referencedTableName) {
    await fetchFields(formData.value.referencedTableSchema, formData.value.referencedTableName, true)
  }
}

const rules = {
  tableSchema: [{ required: true, message: '请输入库名', trigger: 'blur' }],
  tableName: [{ required: true, message: '请输入表名', trigger: 'blur' }],
  columnName: [{ required: true, message: '请输入表字段', trigger: 'blur' }],
  referencedTableSchema: [{ required: true, message: '请输入关联库名', trigger: 'blur' }],
  referencedTableName: [{ required: true, message: '请输入关联表名', trigger: 'blur' }],
  referencedColumnName: [{ required: true, message: '请输入关联表字段', trigger: 'blur' }],
  relationType: [{ required: true, message: '请选择关联类型', trigger: 'change' }],
  condition: [{ required: false, message: '请输入关联条件', trigger: 'blur' }]
}

const formRef = ref(null)
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentId = ref(null)

// 获取表关系列表
const fetchRelations = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/table/relation/list')
    if (response.data.success) {
      relationList.value = response.data.data
    } else {
      ElMessage.error(response.data.message || '获取表关系列表失败')
    }
  } catch (error) {
    ElMessage.error('获取表关系列表失败')
  } finally {
    loading.value = false
  }
}

// 打开添加/编辑对话框
const openDialog = async (row) => {
  if (row) {
    isEdit.value = true
    currentId.value = row.id
    formData.value = { ...row }
    // 加载主表相关数据
    await fetchTables(formData.value.tableSchema)
    await fetchFields(formData.value.tableSchema, formData.value.tableName)
    // 加载关联表相关数据
    await fetchTables(formData.value.referencedTableSchema, true)
    await fetchFields(formData.value.referencedTableSchema, formData.value.referencedTableName, true)
  } else {
    isEdit.value = false
    currentId.value = null
    // 重置表单数据
    formData.value = {
      tableSchema: '',
      tableName: '',
      columnName: '',
      referencedTableSchema: '',
      referencedTableName: '',
      referencedColumnName: '',
      relationType: 1,
      condition: ''
    }
    // 清空相关的下拉选项数据
    tables.value = []
    fields.value = []
    referencedTables.value = []
    referencedFields.value = []
    // 重置表单验证状态
    formRef.value?.resetFields()
  }
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    const url = isEdit.value ? `/api/table/relation/update` : '/api/table/relation/add'
    const method = 'post'
    const response = await axios[method](url, formData.value)
    
    if (response.data.success) {
      ElMessage.success(isEdit.value ? '表关系更新成功' : '表关系添加成功')
      dialogVisible.value = false
      fetchRelations()
    } else {
      ElMessage.error(response.data.message || (isEdit.value ? '更新失败' : '添加失败'))
    }
  } catch (error) {
    if (error.response) {
      ElMessage.error(error.response.data.message || (isEdit.value ? '更新失败' : '添加失败'))
    } else {
      ElMessage.error('网络错误，请稍后重试')
    }
  }
}

// 删除表关系
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该表关系吗？', '提示', {
      type: 'warning'
    })
    
    const response = await axios.post(`/api/table/relation/delete/${id}`)
    if (response.data.success) {
      ElMessage.success('删除成功')
      fetchRelations()
    } else {
      ElMessage.error(response.data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchRelations()
  fetchSchemas()
})
</script>

<template>
  <div class="relation-manage">
    <div class="relation-header">
      <h2>表关系管理</h2>
      <div class="header-right">
        <el-select
          v-model="selectedSchema"
          placeholder="选择库名筛选"
          clearable
          filterable
          class="schema-filter"
        >
          <el-option
            v-for="item in schemas"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <el-button type="primary" @click="openDialog()">添加表关系</el-button>
      </div>
    </div>
    
    <!-- 表关系列表 -->
    <el-table
      :data="filteredRelationList"
      v-loading="loading"
      style="width: 100%"
      border
    >
      <el-table-column prop="tableSchema" label="库名" />
      <el-table-column prop="tableName" label="表名" />
      <el-table-column prop="columnName" label="表字段" />
      <el-table-column prop="condition" label="关联条件" />
      <el-table-column prop="referencedTableSchema" label="关联库名" />
      <el-table-column prop="referencedTableName" label="关联表名" />
      <el-table-column prop="referencedColumnName" label="关联表字段" />
      <el-table-column prop="relationType" label="关联类型" width="100">
        <template #default="{ row }">
          <span>{{ row.relationType === 1 ? '一对一' 
                        : row.relationType === 2 ? '一对多' 
                                : row.relationTsype === 3 ? '多对多' : '' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button-group>
            <el-button type="primary" @click="openDialog(row)" link>
              编辑
            </el-button>
            <el-button type="danger" @click="handleDelete(row.id)" link>
              删除
            </el-button>
          </el-button-group>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加/编辑表关系对话框 -->
    <el-dialog
      :title="isEdit ? '编辑表关系' : '添加表关系'"
      v-model="dialogVisible"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="120px"
        class="relation-form"
      >
        <el-form-item label="库名" prop="tableSchema">
          <el-select v-model="formData.tableSchema" placeholder="请选择库名" @change="handleSchemaChange" filterable>
            <el-option v-for="item in schemas" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="表名" prop="tableName">
          <el-select v-model="formData.tableName" placeholder="请选择表名" :disabled="!formData.tableSchema" @change="handleTableChange" filterable>
            <el-option v-for="item in tables" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="表字段" prop="columnName">
          <el-select v-model="formData.columnName" placeholder="请选择表字段" :disabled="!formData.tableName" filterable>
            <el-option v-for="item in fields" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>

        <el-form-item label="关联条件" prop="condition">
          <el-input v-model="formData.condition" placeholder="请输入关联条件" type="textarea" :rows="3" />
        </el-form-item>
        
        <el-form-item label="关联库名" prop="referencedTableSchema">
          <el-select v-model="formData.referencedTableSchema" placeholder="请选择关联库名" @change="handleReferencedSchemaChange" filterable>
            <el-option v-for="item in schemas" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="关联表名" prop="referencedTableName">
          <el-select v-model="formData.referencedTableName" placeholder="请选择关联表名" :disabled="!formData.referencedTableSchema" @change="handleReferencedTableChange" filterable>
            <el-option v-for="item in referencedTables" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="关联表字段" prop="referencedColumnName">
          <el-select v-model="formData.referencedColumnName" placeholder="请选择关联表字段" :disabled="!formData.referencedTableName" filterable>
            <el-option v-for="item in referencedFields" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="关联类型" prop="relationType">
          <el-select v-model="formData.relationType" placeholder="请选择关联类型">
            <el-option label="一对一" :value="1" />
            <el-option label="一对多" :value="2" />
            <el-option label="多对多" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.relation-manage {
  padding: 20px;
}

.relation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.relation-header h2 {
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.schema-filter {
  width: 200px;
}

.relation-form {
  margin-top: 20px;
}

:deep(.el-dialog__body) {
  padding-top: 10px;
}
</style>