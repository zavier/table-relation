<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addRelation, deleteRelation, listRelations, updateRelation } from '../api/relation'
import { getAllSchemas, getSchemaTables, getTableColumns } from '../api/meta'

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
  return relationList.value.filter((item) => item.tableSchema === selectedSchema.value)
})

// 表单数据和规则
const emptyForm = () => ({
  tableSchema: '',
  tableName: '',
  columnName: '',
  referencedTableSchema: '',
  referencedTableName: '',
  referencedColumnName: '',
  relationType: 1,
  conditionField: '',
  conditionOperator: '=',
  conditionValue: ''
})

const formData = ref(emptyForm())

// 获取schema列表
const fetchSchemas = async () => {
  try {
    const schemaNames = await getAllSchemas()
    schemas.value = (schemaNames ?? []).map((schemaName) => ({
      value: schemaName,
      label: schemaName
    }))
  } catch (error) {
    console.error('获取数据库列表失败:', error)
  }
}

// 获取表列表
const fetchTables = async (schema, isReferenced = false) => {
  if (!schema) return
  try {
    const tableNames = await getSchemaTables(schema)
    const tableList = (tableNames ?? []).map((tableName) => ({
      value: tableName,
      label: tableName
    }))
    if (isReferenced) {
      referencedTables.value = tableList
    } else {
      tables.value = tableList
    }
  } catch (error) {
    console.error('获取表列表失败:', error)
  }
}

// 获取字段列表
const fetchFields = async (schema, table, isReferenced = false) => {
  if (!schema || !table) return
  try {
    const columnNames = await getTableColumns(schema, table)
    const fieldList = (columnNames ?? []).map((fieldName) => ({
      value: fieldName,
      label: fieldName
    }))
    if (isReferenced) {
      referencedFields.value = fieldList
    } else {
      fields.value = fieldList
    }
  } catch (error) {
    console.error('获取字段列表失败:', error)
  }
}

// 监听主表schema变化
const handleSchemaChange = async () => {
  formData.value.tableName = ''
  formData.value.columnName = ''
  formData.value.conditionField = ''
  formData.value.conditionValue = ''
  tables.value = []
  fields.value = []
  if (formData.value.tableSchema) {
    await fetchTables(formData.value.tableSchema)
  }
}

// 监听主表表名变化
const handleTableChange = async () => {
  formData.value.columnName = ''
  formData.value.conditionField = ''
  formData.value.conditionValue = ''
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
    await fetchFields(
      formData.value.referencedTableSchema,
      formData.value.referencedTableName,
      true
    )
  }
}

const rules = {
  tableSchema: [{ required: true, message: '请输入库名', trigger: 'blur' }],
  tableName: [{ required: true, message: '请输入表名', trigger: 'blur' }],
  columnName: [{ required: true, message: '请输入表字段', trigger: 'blur' }],
  referencedTableSchema: [{ required: true, message: '请输入关联库名', trigger: 'blur' }],
  referencedTableName: [{ required: true, message: '请输入关联表名', trigger: 'blur' }],
  referencedColumnName: [{ required: true, message: '请输入关联表字段', trigger: 'blur' }],
  relationType: [{ required: true, message: '请选择关联类型', trigger: 'change' }]
}

const formRef = ref(null)
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentId = ref(null)

// 获取表关系列表
const fetchRelations = async () => {
  loading.value = true
  try {
    relationList.value = (await listRelations()) ?? []
  } catch (error) {
    console.error('获取表关系列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 打开添加/编辑对话框
const openDialog = async (row) => {
  if (row) {
    isEdit.value = true
    currentId.value = row.id
    formData.value = {
      ...emptyForm(),
      ...row,
      conditionField: row.conditionField || '',
      conditionOperator: row.conditionOperator || '=',
      conditionValue: row.conditionValue || ''
    }
    // 加载主表相关数据
    await fetchTables(formData.value.tableSchema)
    await fetchFields(formData.value.tableSchema, formData.value.tableName)
    // 加载关联表相关数据
    await fetchTables(formData.value.referencedTableSchema, true)
    await fetchFields(
      formData.value.referencedTableSchema,
      formData.value.referencedTableName,
      true
    )
  } else {
    isEdit.value = false
    currentId.value = null
    // 重置表单数据
    formData.value = emptyForm()
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
  } catch {
    return
  }

  const payload = { ...formData.value }
  if (!payload.conditionField) {
    // 未选择条件字段时，避免把默认运算符提交到后端
    payload.conditionOperator = ''
    payload.conditionValue = ''
  } else if (!payload.conditionOperator || !payload.conditionValue) {
    ElMessage.error('请完整填写关联条件的字段、运算符和值')
    return
  }

  try {
    if (isEdit.value) {
      await updateRelation(payload)
    } else {
      await addRelation(payload)
    }
    ElMessage.success(isEdit.value ? '表关系更新成功' : '表关系添加成功')
    dialogVisible.value = false
    fetchRelations()
  } catch (error) {
    console.error('保存表关系失败:', error)
  }
}

// 删除表关系
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该表关系吗？', '提示', {
      type: 'warning'
    })
  } catch {
    return
  }

  try {
    await deleteRelation(id)
    ElMessage.success('删除成功')
    fetchRelations()
  } catch (error) {
    console.error('删除表关系失败:', error)
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
    <el-table :data="filteredRelationList" v-loading="loading" style="width: 100%" border>
      <el-table-column prop="tableSchema" label="库名" />
      <el-table-column prop="tableName" label="表名" />
      <el-table-column prop="columnName" label="表字段" />
      <el-table-column label="关联条件" width="200">
        <template #default="{ row }">
          <span v-if="row.conditionField">
            {{ row.conditionField }} {{ row.conditionOperator }} {{ row.conditionValue }}
          </span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="referencedTableSchema" label="关联库名" />
      <el-table-column prop="referencedTableName" label="关联表名" />
      <el-table-column prop="referencedColumnName" label="关联表字段" />
      <el-table-column prop="relationType" label="关联类型" width="100">
        <template #default="{ row }">
          <span>{{
            row.relationType === 1
              ? '一对一'
              : row.relationType === 2
                ? '一对多'
                : row.relationTsype === 3
                  ? '多对多'
                  : ''
          }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button-group>
            <el-button type="primary" @click="openDialog(row)" link> 编辑 </el-button>
            <el-button type="danger" @click="handleDelete(row.id)" link> 删除 </el-button>
          </el-button-group>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加/编辑表关系对话框 -->
    <el-dialog :title="isEdit ? '编辑表关系' : '添加表关系'" v-model="dialogVisible" width="600px">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="120px"
        class="relation-form"
      >
        <el-form-item label="库名" prop="tableSchema">
          <el-select
            v-model="formData.tableSchema"
            placeholder="请选择库名"
            @change="handleSchemaChange"
            filterable
          >
            <el-option
              v-for="item in schemas"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="表名" prop="tableName">
          <el-select
            v-model="formData.tableName"
            placeholder="请选择表名"
            :disabled="!formData.tableSchema"
            @change="handleTableChange"
            filterable
          >
            <el-option
              v-for="item in tables"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="表字段" prop="columnName">
          <el-select
            v-model="formData.columnName"
            placeholder="请选择表字段"
            :disabled="!formData.tableName"
            filterable
          >
            <el-option
              v-for="item in fields"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="关联条件">
          <div class="condition-editor">
            <el-select
              v-model="formData.conditionField"
              placeholder="字段（可选）"
              clearable
              filterable
              class="condition-field"
            >
              <el-option
                v-for="item in fields"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
            <el-select
              v-model="formData.conditionOperator"
              class="condition-operator"
              :disabled="!formData.conditionField"
            >
              <el-option label="=" value="=" />
              <el-option label="!=" value="!=" />
              <el-option label=">" value=">" />
              <el-option label=">=" value=">=" />
              <el-option label="<" value="<" />
              <el-option label="<=" value="<=" />
              <el-option label="LIKE" value="LIKE" />
            </el-select>
            <el-input
              v-model="formData.conditionValue"
              placeholder="值"
              :disabled="!formData.conditionField"
              class="condition-value"
            />
          </div>
        </el-form-item>

        <el-form-item label="关联库名" prop="referencedTableSchema">
          <el-select
            v-model="formData.referencedTableSchema"
            placeholder="请选择关联库名"
            @change="handleReferencedSchemaChange"
            filterable
          >
            <el-option
              v-for="item in schemas"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="关联表名" prop="referencedTableName">
          <el-select
            v-model="formData.referencedTableName"
            placeholder="请选择关联表名"
            :disabled="!formData.referencedTableSchema"
            @change="handleReferencedTableChange"
            filterable
          >
            <el-option
              v-for="item in referencedTables"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="关联表字段" prop="referencedColumnName">
          <el-select
            v-model="formData.referencedColumnName"
            placeholder="请选择关联表字段"
            :disabled="!formData.referencedTableName"
            filterable
          >
            <el-option
              v-for="item in referencedFields"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
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

.condition-editor {
  display: flex;
  gap: 8px;
  width: 100%;
}

.condition-field {
  flex: 1;
}

.condition-operator {
  width: 110px;
}

.condition-value {
  flex: 1;
}

.relation-form {
  margin-top: 20px;
}

:deep(.el-dialog__body) {
  padding-top: 10px;
}
</style>
