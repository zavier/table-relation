<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

// 数据源列表
const dataSourceList = ref([])
const loading = ref(false)

// 表单数据和规则
const formData = ref({
  host: '',
  username: '',
  password: '',
  database: '',
  port: '3306'
})

const rules = {
  host: [{ required: true, message: '请输入主机地址', trigger: 'blur' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  database: [{ required: true, message: '请输入数据库名', trigger: 'blur' }],
  port: [
    { required: true, message: '请输入端口号', trigger: 'blur' },
    { pattern: /^[0-9]+$/, message: '端口号必须为数字', trigger: 'blur' }
  ]
}

const formRef = ref(null)
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentId = ref(null)

// 获取数据源列表
const fetchDataSources = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/table/datasource/list')
    if (response.data.success) {
      dataSourceList.value = response.data.data
    } else {
      ElMessage.error(response.data.message || '获取数据源列表失败')
    }
  } catch (error) {
    ElMessage.error('获取数据源列表失败')
  } finally {
    loading.value = false
  }
}

// 打开添加/编辑对话框
const openDialog = (row) => {
  if (row) {
    isEdit.value = true
    currentId.value = row.id
    formData.value = { ...row }
  } else {
    isEdit.value = false
    currentId.value = null
    formData.value = {
      host: '',
      username: '',
      password: '',
      database: '',
      port: '3306'
    }
    formRef.value?.resetFields()
  }
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    const url = isEdit.value ? `/api/table/datasource/update` : '/api/table/datasource/add'
    const method = 'post'
    const response = await axios[method](url, formData.value)
    
    if (response.data.success) {
      ElMessage.success(isEdit.value ? '数据源更新成功' : '数据源添加成功')
      dialogVisible.value = false
      fetchDataSources()
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

// 删除数据源
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该数据源吗？', '提示', {
      type: 'warning'
    })
    
    const response = await axios.post(`/api/table/datasource/delete/${id}`)
    if (response.data.success) {
      ElMessage.success('删除成功')
      fetchDataSources()
    } else {
      ElMessage.error(response.data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 测试连接
const testConnection = async (row) => {
  try {
    const response = await axios.post('/api/table/datasource/test', row)
    if (response.data.success && response.data.data) {
      ElMessage.success('连接测试成功')
    } else {
      ElMessage.error(response.data.message || '连接测试失败')
    }
  } catch (error) {
    ElMessage.error('连接测试失败')
  }
}

onMounted(() => {
  fetchDataSources()
})
</script>

<template>
  <div class="datasource-manage">
    <div class="datasource-header">
      <h2>数据源管理</h2>
      <el-button type="primary" @click="openDialog()">添加数据源</el-button>
    </div>
    
    <!-- 数据源列表 -->
    <el-table
      :data="dataSourceList"
      v-loading="loading"
      style="width: 100%"
      border
    >
      <el-table-column prop="host" label="主机地址" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="database" label="数据库名" />
      <el-table-column prop="port" label="端口号" width="100" />
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="{ row }">
          <el-button-group>
            <el-button type="primary" @click="testConnection(row)" link>
              测试连接
            </el-button>
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

    <!-- 添加/编辑数据源对话框 -->
    <el-dialog
      :title="isEdit ? '编辑数据源' : '添加数据源'"
      v-model="dialogVisible"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="120px"
        class="datasource-form"
      >
        <el-form-item label="主机地址" prop="host">
          <el-input v-model="formData.host" placeholder="请输入主机地址" />
        </el-form-item>
        
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" placeholder="请输入用户名" />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="formData.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="数据库名" prop="database">
          <el-input v-model="formData.database" placeholder="请输入数据库名" />
        </el-form-item>
        
        <el-form-item label="端口号" prop="port">
          <el-input v-model="formData.port" placeholder="请输入端口号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="testConnection(formData)">测试连接</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.datasource-manage {
  padding: 20px;
}

.datasource-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.datasource-header h2 {
  margin: 0;
}

.datasource-form {
  margin-top: 20px;
}

:deep(.el-dialog__body) {
  padding-top: 10px;
}
</style>