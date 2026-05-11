<template>
  <div class="dataset-list">
    <el-card>
      <template #header>
        <div class="header-actions">
          <div class="header-left">
            <el-button type="primary" @click="handleCreate">
              <el-icon><Plus /></el-icon>
              新建数据集
            </el-button>
            <el-radio-group v-model="filterType" style="margin-left: 16px">
              <el-radio-button label="">全部</el-radio-button>
              <el-radio-button label="SQL">SQL</el-radio-button>
              <el-radio-button label="API">API</el-radio-button>
              <el-radio-button label="FILE">文件</el-radio-button>
            </el-radio-group>
          </div>
          <div class="header-right">
            <el-input
              v-model="keyword"
              placeholder="搜索数据集名称"
              style="width: 200px"
              clearable
              @clear="loadData"
              @keyup.enter="loadData"
            >
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="datasetName" label="数据集名称" min-width="150" />
        <el-table-column prop="datasetType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.datasetType === 'SQL'" type="success">SQL</el-tag>
            <el-tag v-else-if="row.datasetType === 'API'" type="warning">API</el-tag>
            <el-tag v-else type="info">文件</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '可用' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createUser" label="创建人" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link @click="handleTest(row)">测试</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @change="loadData"
        />
      </div>
    </el-card>

    <el-dialog v-model="testDialogVisible" title="测试数据集" width="800px">
      <div v-loading="testing">
        <el-table :data="testData" max-height="400" border>
          <el-table-column
            v-for="col in testColumns"
            :key="col"
            :prop="col"
            :label="col"
          />
        </el-table>
        <p style="margin-top: 12px; color: #67C23A" v-if="!testing">
          共 {{ testData.length }} 条数据
        </p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDatasetList, deleteDataset, testDataset } from '@/api/dataset'
import type { Dataset } from '@/api/dataset'

const router = useRouter()

const loading = ref(false)
const tableData = ref<Dataset[]>([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const keyword = ref('')
const filterType = ref('')

const testDialogVisible = ref(false)
const testing = ref(false)
const testData = ref<any[]>([])
const testColumns = ref<string[]>([])

const loadData = async () => {
  loading.value = true
  try {
    const res = await getDatasetList({
      page: page.value,
      size: size.value,
      type: filterType.value,
      keyword: keyword.value
    })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  router.push('/dataset/create/SQL')
}

const handleEdit = (row: Dataset) => {
  router.push(`/dataset/edit/${row.id}`)
}

const handleTest = async (row: Dataset) => {
  testDialogVisible.value = true
  testing.value = true
  testData.value = []
  testColumns.value = []

  try {
    const res = await testDataset(row.id!)
    testData.value = res.data.data || []
    if (testData.value.length > 0) {
      testColumns.value = Object.keys(testData.value[0])
    }
  } catch (e) {
    ElMessage.error('测试失败')
  } finally {
    testing.value = false
  }
}

const handleDelete = async (row: Dataset) => {
  await ElMessageBox.confirm('确定要删除该数据集吗？', '提示', {
    type: 'warning'
  })
  await deleteDataset(row.id!)
  ElMessage.success('删除成功')
  loadData()
}

watch(filterType, () => {
  page.value = 1
  loadData()
})

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.dataset-list {
  .header-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .pagination {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
