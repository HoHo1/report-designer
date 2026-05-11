<template>
  <div class="system-config">
    <el-card>
      <template #header>
        <span>系统配置</span>
      </template>

      <el-form ref="formRef" :model="form" label-width="150px">
        <el-divider content-position="left">报表配置</el-divider>
        <el-form-item label="最大数据行数">
          <el-input-number v-model="form.maxRows" :min="1000" :max="100000" />
          <span style="margin-left: 8px; color: #999">行</span>
        </el-form-item>
        <el-form-item label="默认分页大小">
          <el-input-number v-model="form.defaultPageSize" :min="10" :max="100" />
          <span style="margin-left: 8px; color: #999">条/页</span>
        </el-form-item>
        
        <el-divider content-position="left">数据集配置</el-divider>
        <el-form-item label="缓存过期时间">
          <el-input-number v-model="form.cacheExpiration" :min="1" :max="60" />
          <span style="margin-left: 8px; color: #999">分钟</span>
        </el-form-item>
        <el-form-item label="请求超时时间">
          <el-input-number v-model="form.requestTimeout" :min="1" :max="60" />
          <span style="margin-left: 8px; color: #999">秒</span>
        </el-form-item>
        <el-form-item label="文件上传大小限制">
          <el-input-number v-model="form.maxFileSize" :min="1" :max="100" />
          <span style="margin-left: 8px; color: #999">MB</span>
        </el-form-item>
        
        <el-divider content-position="left">样式配置</el-divider>
        <el-form-item label="默认字体">
          <el-input v-model="form.defaultFont" />
        </el-form-item>
        <el-form-item label="默认字体大小">
          <el-input-number v-model="form.defaultFontSize" :min="9" :max="72" />
          <span style="margin-left: 8px; color: #999">px</span>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSave">保存配置</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const formRef = ref()
const form = reactive({
  maxRows: 10000,
  defaultPageSize: 20,
  cacheExpiration: 5,
  requestTimeout: 10,
  maxFileSize: 20,
  defaultFont: 'Microsoft YaHei',
  defaultFontSize: 12
})

const handleSave = () => {
  ElMessage.success('配置保存成功')
}

const handleReset = () => {
  Object.assign(form, {
    maxRows: 10000,
    defaultPageSize: 20,
    cacheExpiration: 5,
    requestTimeout: 10,
    maxFileSize: 20,
    defaultFont: 'Microsoft YaHei',
    defaultFontSize: 12
  })
}

onMounted(() => {
  // 加载配置
})
</script>

<style scoped lang="scss">
.system-config {
  max-width: 800px;
}
</style>
