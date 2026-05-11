<template>
  <div class="dataset-create">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-button @click="handleBack">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <span>{{ isEdit ? '编辑数据集' : '创建数据集' }}</span>
        </div>
      </template>

      <el-steps :active="activeStep" finish-status="success" align-center>
        <el-step title="基本信息" />
        <el-step title="配置参数" />
        <el-step title="测试验证" />
      </el-steps>

      <div class="step-content">
        <el-form
          v-show="activeStep === 0"
          ref="basicFormRef"
          :model="form"
          :rules="rules"
          label-width="120px"
        >
          <el-form-item label="数据集名称" prop="datasetName">
            <el-input v-model="form.datasetName" placeholder="请输入数据集名称" />
          </el-form-item>
          <el-form-item label="数据集类型" prop="datasetType">
            <el-radio-group v-model="form.datasetType" :disabled="isEdit">
              <el-radio label="SQL">SQL 查询</el-radio>
              <el-radio label="API">API 接口</el-radio>
              <el-radio label="FILE">文件上传</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>

        <div v-show="activeStep === 1">
          <el-form v-if="form.datasetType === 'SQL'" :model="sqlConfig" label-width="120px">
            <el-form-item label="数据库类型">
              <el-select v-model="sqlConfig.dbType" style="width: 100%">
                <el-option label="MySQL" value="MYSQL" />
                <el-option label="PostgreSQL" value="POSTGRESQL" />
                <el-option label="Oracle" value="ORACLE" />
                <el-option label="SQL Server" value="SQLSERVER" />
              </el-select>
            </el-form-item>
            <el-form-item label="主机地址">
              <el-input v-model="sqlConfig.host" placeholder="例如: localhost" />
            </el-form-item>
            <el-form-item label="端口">
              <el-input-number v-model="sqlConfig.port" :min="1" :max="65535" />
            </el-form-item>
            <el-form-item label="数据库名">
              <el-input v-model="sqlConfig.database" placeholder="请输入数据库名" />
            </el-form-item>
            <el-form-item label="用户名">
              <el-input v-model="sqlConfig.username" placeholder="请输入用户名" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="sqlConfig.password" type="password" show-password placeholder="请输入密码" />
            </el-form-item>
            <el-form-item label="SQL 查询">
              <el-input
                v-model="sqlConfig.sql"
                type="textarea"
                :rows="6"
                placeholder="SELECT * FROM table_name WHERE id = #{id}"
              />
            </el-form-item>
            <el-form-item label="超时时间(秒)">
              <el-input-number v-model="sqlConfig.timeout" :min="1" :max="60" />
            </el-form-item>
          </el-form>

          <el-form v-else-if="form.datasetType === 'API'" :model="apiConfig" label-width="120px">
            <el-form-item label="请求地址">
              <el-input v-model="apiConfig.url" placeholder="https://api.example.com/data" />
            </el-form-item>
            <el-form-item label="请求方式">
              <el-radio-group v-model="apiConfig.method">
                <el-radio label="GET">GET</el-radio>
                <el-radio label="POST">POST</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="请求头">
              <el-input
                v-model="apiConfig.headersText"
                type="textarea"
                :rows="3"
                placeholder='{"Content-Type": "application/json"}'
              />
            </el-form-item>
            <el-form-item label="请求参数">
              <el-input
                v-model="apiConfig.paramsText"
                type="textarea"
                :rows="3"
                placeholder='{"key": "value"}'
              />
            </el-form-item>
            <el-form-item label="数据路径">
              <el-input v-model="apiConfig.dataPath" placeholder="data.items (用于解析嵌套的数组)" />
            </el-form-item>
            <el-form-item label="超时时间(秒)">
              <el-input-number v-model="apiConfig.timeout" :min="1" :max="60" />
            </el-form-item>
          </el-form>

          <el-form v-else :model="fileConfig" label-width="120px">
            <el-form-item label="上传文件">
              <el-upload
                drag
                action="#"
                :auto-upload="false"
                :on-change="handleFileChange"
                accept=".xlsx,.xls,.csv"
              >
                <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
                <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
                <template #tip>
                  <div class="el-upload__tip">支持 .xlsx、.xls、.csv 格式，单文件不超过 20MB</div>
                </template>
              </el-upload>
            </el-form-item>
          </el-form>
        </div>

        <div v-show="activeStep === 2" v-loading="testing">
          <el-result
            v-if="testResult !== null"
            :icon="testResult ? 'success' : 'error'"
            :title="testResult ? '测试成功' : '测试失败'"
          >
            <template #sub-title>
              <p v-if="testResult">数据集配置正确，可以正常获取数据</p>
              <p v-else>{{ testError }}</p>
            </template>
          </el-result>
        </div>
      </div>

      <div class="step-actions">
        <el-button v-if="activeStep > 0" @click="activeStep--">上一步</el-button>
        <el-button v-if="activeStep < 2" type="primary" @click="nextStep">下一步</el-button>
        <el-button v-if="activeStep === 1" @click="handleTest">测试</el-button>
        <el-button v-if="activeStep === 2" type="primary" @click="handleSave">保存</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getDataset, createDataset, updateDataset, testDataset } from '@/api/dataset'

const route = useRoute()
const router = useRouter()

const datasetId = computed(() => route.params.id as string)
const isEdit = computed(() => !!datasetId.value)

const activeStep = ref(0)
const basicFormRef = ref()
const testing = ref(false)
const testResult = ref<boolean | null>(null)
const testError = ref('')

const form = reactive({
  datasetName: '',
  datasetType: 'SQL',
  config: '',
  status: 0
})

const sqlConfig = reactive({
  dbType: 'MYSQL',
  host: 'localhost',
  port: 3306,
  database: '',
  username: '',
  password: '',
  sql: '',
  timeout: 10
})

const apiConfig = reactive({
  url: '',
  method: 'GET',
  headersText: '',
  paramsText: '',
  dataPath: '',
  timeout: 10
})

const fileConfig = reactive({
  file: null as any
})

const rules = {
  datasetName: [{ required: true, message: '请输入数据集名称', trigger: 'blur' }],
  datasetType: [{ required: true, message: '请选择数据集类型', trigger: 'change' }]
}

const nextStep = async () => {
  if (activeStep.value === 0) {
    if (!basicFormRef.value) return
    await basicFormRef.value.validate()
  }
  activeStep.value++
}

const handleTest = async () => {
  testing.value = true
  testResult.value = null

  try {
    const config = getConfig()
    const data = {
      ...form,
      config: JSON.stringify(config)
    }

    if (isEdit.value) {
      await updateDataset(datasetId.value, data)
    } else {
      const res = await createDataset(data)
      datasetId.value = res.data.id!
    }

    await testDataset(datasetId.value)
    testResult.value = true
  } catch (e: any) {
    testResult.value = false
    testError.value = e.message || '测试失败'
  } finally {
    testing.value = false
  }
}

const handleSave = async () => {
  const config = getConfig()
  const data = {
    ...form,
    config: JSON.stringify(config),
    status: 1
  }

  if (isEdit.value) {
    await updateDataset(datasetId.value, data)
  } else {
    await createDataset(data)
  }

  ElMessage.success('保存成功')
  router.push('/dataset')
}

const getConfig = () => {
  switch (form.datasetType) {
    case 'SQL':
      return { ...sqlConfig }
    case 'API':
      return {
        ...apiConfig,
        headers: apiConfig.headersText ? JSON.parse(apiConfig.headersText) : {},
        params: apiConfig.paramsText ? JSON.parse(apiConfig.paramsText) : {}
      }
    case 'FILE':
      return { fileId: fileConfig.file?.name }
    default:
      return {}
  }
}

const handleFileChange = (file: any) => {
  fileConfig.file = file.raw
}

const handleBack = () => {
  router.push('/dataset')
}

onMounted(async () => {
  if (isEdit.value) {
    const res = await getDataset(datasetId.value)
    Object.assign(form, res.data)
    const config = JSON.parse(res.data.config || '{}')
    if (form.datasetType === 'SQL') {
      Object.assign(sqlConfig, config)
    } else if (form.datasetType === 'API') {
      Object.assign(apiConfig, config)
      if (config.headers) apiConfig.headersText = JSON.stringify(config.headers)
      if (config.params) apiConfig.paramsText = JSON.stringify(config.params)
    }
  }
})
</script>

<style scoped lang="scss">
.dataset-create {
  .card-header {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .step-content {
    margin: 40px 20px;
    min-height: 400px;
  }

  .step-actions {
    display: flex;
    justify-content: center;
    gap: 16px;
    padding-top: 20px;
    border-top: 1px solid #eee;
  }
}
</style>
