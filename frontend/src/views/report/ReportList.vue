<template>
  <div class="report-list">
    <el-card>
      <template #header>
        <div class="header-actions">
          <div class="header-left">
            <el-button type="primary" @click="handleCreate">
              <el-icon><Plus /></el-icon>
              新建报表
            </el-button>
          </div>
          <div class="header-right">
            <el-input
              v-model="keyword"
              placeholder="搜索报表名称"
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
        <el-table-column prop="reportName" label="报表名称" min-width="150" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDesign(row)">设计</el-button>
            <el-button type="primary" link @click="handlePreview(row)">预览</el-button>
            <el-button type="primary" link @click="handleCopy(row)">复制</el-button>
            <el-button type="primary" link @click="handleExport(row)">导出</el-button>
            <el-dropdown trigger="click">
              <el-button type="primary" link>更多</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleEdit(row)">编辑</el-dropdown-item>
                  <el-dropdown-item @click="handleVersion(row)">版本历史</el-dropdown-item>
                  <el-dropdown-item @click="handleDelete(row)" divided>删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @change="loadData"
        />
      </div>
    </el-card>

    <el-dialog v-model="versionDialogVisible" title="版本历史" width="600px">
      <el-table :data="versions" max-height="400">
        <el-table-column prop="versionNum" label="版本号" width="100" />
        <el-table-column prop="createUser" label="操作人" width="120" />
        <el-table-column prop="createTime" label="操作时间" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleRollback(row)">回滚</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="exportDialogVisible" title="导出报表" width="400px">
      <el-form label-width="80px">
        <el-form-item label="导出格式">
          <el-select v-model="exportFormat" style="width: 100%">
            <el-option label="Excel" value="excel" />
            <el-option label="PDF" value="pdf" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="exportDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmExport">导出</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReportList, deleteReport, copyReport, getReportVersions, rollbackReport } from '@/api/report'
import type { Report } from '@/api/report'

const router = useRouter()

const loading = ref(false)
const tableData = ref<Report[]>([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const keyword = ref('')

const versionDialogVisible = ref(false)
const versions = ref<any[]>([])
const currentReportId = ref('')

const exportDialogVisible = ref(false)
const exportFormat = ref('excel')
const exportReportId = ref('')

const loadData = async () => {
  loading.value = true
  try {
    const res = await getReportList({
      page: page.value,
      size: size.value,
      keyword: keyword.value
    })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  router.push('/report/designer')
}

const handleDesign = (row: Report) => {
  router.push(`/report/designer/${row.id}`)
}

const handlePreview = (row: Report) => {
  router.push(`/report/preview/${row.id}`)
}

const handleEdit = (row: Report) => {
  ElMessage.info('编辑功能开发中')
}

const handleCopy = async (row: Report) => {
  await copyReport(row.id!)
  ElMessage.success('复制成功')
  loadData()
}

const handleExport = (row: Report) => {
  exportReportId.value = row.id!
  exportDialogVisible.value = true
}

const confirmExport = async () => {
  ElMessage.success('导出功能开发中')
  exportDialogVisible.value = false
}

const handleVersion = async (row: Report) => {
  currentReportId.value = row.id!
  const res = await getReportVersions(row.id!)
  versions.value = res.data
  versionDialogVisible.value = true
}

const handleRollback = async (row: any) => {
  await ElMessageBox.confirm('确定要回滚到此版本吗？', '提示', {
    type: 'warning'
  })
  await rollbackReport(currentReportId.value, row.id)
  ElMessage.success('回滚成功')
  versionDialogVisible.value = false
  loadData()
}

const handleDelete = async (row: Report) => {
  await ElMessageBox.confirm('确定要删除该报表吗？', '提示', {
    type: 'warning'
  })
  await deleteReport(row.id!)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.report-list {
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
