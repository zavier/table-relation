<script setup>
import { ref } from 'vue'
import VueJsonPretty from 'vue-json-pretty'
import 'vue-json-pretty/lib/styles.css'

const jsonInput = ref('')
const formattedJson = ref(null)
const errorMessage = ref('')

const formatJson = () => {
  try {
    if (!jsonInput.value.trim()) {
      formattedJson.value = null
      errorMessage.value = '请输入JSON文本'
      return
    }
    const parsedJson = JSON.parse(jsonInput.value)
    formattedJson.value = parsedJson
    errorMessage.value = ''
  } catch (error) {
    formattedJson.value = null
    errorMessage.value = '无效的JSON格式：' + error.message
  }
}
</script>

<template>
  <div class="json-formatter">
    <h2>JSON格式化</h2>
    
    <div class="input-section">
      <el-input
        v-model="jsonInput"
        type="textarea"
        :rows="8"
        placeholder="请输入要格式化的JSON文本"
        class="json-input"
      />
      <el-button type="primary" @click="formatJson" class="format-button">
        格式化
      </el-button>
    </div>

    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>

    <div v-if="formattedJson" class="result-section">
      <h3>格式化结果</h3>
      <el-card class="result-card">
        <vue-json-pretty
          :data="formattedJson"
          :deep="2"
          :show-double-quotes="true"
          :show-length="true"
          :show-line="true"
          class="json-viewer"
        />
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.json-formatter {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.input-section {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.json-input {
  width: 100%;
}

.format-button {
  align-self: flex-start;
}

.error-message {
  margin-top: 16px;
  color: #f56c6c;
}

.result-section {
  margin-top: 24px;
}

.result-card {
  margin-top: 16px;
  background-color: #f8f9fa;
}

.json-viewer {
  margin: 0;
  background: transparent;
}

:deep(.vjs-tree) {
  font-size: 14px !important;
}

:deep(.vjs-tree .vjs-value) {
  color: #409EFF !important;
}

:deep(.vjs-tree .vjs-key) {
  color: #67C23A !important;
}
</style>