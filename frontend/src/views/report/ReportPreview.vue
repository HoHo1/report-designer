<template>
  <div class="report-preview">
    <el-card>
      <template #header>
        <div class="header-actions">
          <div class="header-left">
            <el-button @click="handleBack">
              <el-icon><ArrowLeft /></el-icon>
              返回
            </el-button>
            <span class="report-title">{{ reportName }}</span>
          </div>
          <div class="header-right">
            <el-button @click="handleRefresh">
              <el-icon><Refresh /></el-icon>
              刷新数据
            </el-button>
            <el-button @click="handleExport">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
            <el-button @click="handlePrint">
              <el-icon><Printer /></el-icon>
              打印
            </el-button>
          </div>
        </div>
      </template>

      <div class="preview-content" v-loading="loading">
        <div class="table-wrapper">
          <el-table
            :data="tableData"
            border
            stripe
            :height="600"
            :default-sort="{ prop: 'createTime', order: 'descending' }"
          >
            <el-table-column
              v-for="col in columns"
              :key="col.prop"
              :prop="col.prop"
              :label="col.label"
              :width="col.width"
              sortable
            />
          </el-table>
        </div>

        <div class="pagination">
          <el-pagination
            v-model:current-page="page"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next"
            @change="loadData"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getReport, renderReport } from '@/api/report'

const route = useRoute()
const router = useRouter()

const reportId = route.params.id as string
const reportName = ref('')
const loading = ref(false)
const tableData = ref<any[]>([])
const columns = ref<any[]>([])
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)

const loadData = async () => {
  loading.value = true
  try {
    const res = await renderReport(reportId, {
      page: page.value,
      pageSize: pageSize.value
    })
    const design = res.data.designContent
    const rendered = res.data.renderedData

    if (design.columns) {
      columns.value = design.columns
    }

    if (rendered && Object.keys(rendered).length > 0) {
      const firstKey = Object.keys(rendered)[0]
      tableData.value = rendered[firstKey] || []
      total.value = tableData.value.length
    }
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleBack = () => {
  router.push('/report')
}

const handleRefresh = () => {
  loadData()
  ElMessage.success('刷新成功')
}

const handleExport = () => {
  ElMessage.info('导出功能开发中')
}

const handlePrint = () => {
  window.print()
}

onMounted(async () => {
  if (reportId) {
    const res = await getReport(reportId)
    reportName.value = res.data.reportName
    await loadData()
  }
})
</script>

<style scoped lang="scss">
.report-preview {
  .header-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-left {
      display: flex;
      align-items: center;
      gap: 16px;

      .report-title {
        font-size: 16px;
        font-weight: bold;
      }
    }

    .header-right {
      display: flex;
      gap: 8px;
    }
  }

  .preview-content {
    .pagination {
      margin-top: 16px;
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style>
