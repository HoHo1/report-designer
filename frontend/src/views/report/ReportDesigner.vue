<template>
  <div class="report-designer">
    <el-container>
      <el-header class="designer-header">
        <div class="header-left">
          <el-button @click="handleBack">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <el-input v-model="reportName" placeholder="请输入报表名称" style="width: 200px; margin-left: 16px" />
        </div>
        <div class="header-right">
          <el-button @click="handleSaveDraft">保存草稿</el-button>
          <el-button type="primary" @click="handlePublish">发布</el-button>
        </div>
      </el-header>

      <el-container class="designer-body">
        <el-aside width="60px" class="toolbar">
          <el-tooltip content="字体" placement="right">
            <el-button><el-icon><Font /></el-icon></el-button>
          </el-tooltip>
          <el-tooltip content="加粗" placement="right">
            <el-button><el-icon><Bolder /></el-icon></el-button>
          </el-tooltip>
          <el-tooltip content="斜体" placement="right">
            <el-button><el-icon><Italic /></el-icon></el-button>
          </el-tooltip>
          <el-divider />
          <el-tooltip content="边框" placement="right">
            <el-button><el-icon><Grid /></el-icon></el-button>
          </el-tooltip>
          <el-tooltip content="背景色" placement="right">
            <el-button><el-icon><Brush /></el-icon></el-button>
          </el-tooltip>
          <el-tooltip content="对齐" placement="right">
            <el-button><el-icon><AlignLeft /></el-icon></el-button>
          </el-tooltip>
          <el-divider />
          <el-tooltip content="合并单元格" placement="right">
            <el-button><el-icon><Tickets /></el-icon></el-button>
          </el-tooltip>
          <el-tooltip content="插入行" placement="right">
            <el-button @click="insertRow"><el-icon><Plus /></el-icon></el-button>
          </el-tooltip>
          <el-tooltip content="插入列" placement="right">
            <el-button @click="insertCol"><el-icon><Plus /></el-icon></el-button>
          </el-tooltip>
        </el-aside>

        <el-main class="designer-main">
          <div class="table-container">
            <hot-table
              ref="hotTableRef"
              :data="tableData"
              :settings="hotSettings"
              :columns="columns"
              :colHeaders="colHeaders"
            />
          </div>
        </el-main>

        <el-aside width="280px" class="dataset-panel">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="数据集" name="dataset">
              <div class="dataset-list">
                <el-input
                  v-model="datasetSearch"
                  placeholder="搜索数据集"
                  prefix-icon="Search"
                  clearable
                />
                <el-tree
                  :data="datasetTree"
                  :props="{ label: 'label', children: 'children' }"
                  node-key="id"
                  @node-click="handleDatasetClick"
                >
                  <template #default="{ node, data }">
                    <span>
                      <el-icon v-if="data.type === 'SQL'"><Connection /></el-icon>
                      <el-icon v-else-if="data.type === 'API'"><Link /></el-icon>
                      <el-icon v-else><Document /></el-icon>
                      {{ node.label }}
                    </span>
                  </template>
                </el-tree>
              </div>
            </el-tab-pane>
            <el-tab-pane label="绑定字段" name="binding">
              <div class="binding-panel">
                <p v-if="!selectedDataset">请先选择数据集</p>
                <div v-else>
                  <p class="selected-dataset">当前数据集：{{ selectedDataset.datasetName }}</p>
                  <el-table :data="datasetFields" size="small" max-height="300">
                    <el-table-column prop="name" label="字段名" />
                    <el-table-column label="操作" width="80">
                      <template #default="{ row }">
                        <el-button type="primary" size="small" link @click="bindField(row)">绑定</el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-aside>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { HotTable } from '@handsontable/vue3'
import 'handsontable/dist/handsontable.full.css'
import { ElMessage } from 'element-plus'
import { getReport, createReport, updateReport } from '@/api/report'
import { getDatasetList, getDatasetFields } from '@/api/dataset'

const route = useRoute()
const router = useRouter()

const reportId = ref(route.params.id as string)
const reportName = ref('')
const tableData = ref<any[]>([
  ['', '', '', '', ''],
  ['', '', '', '', ''],
  ['', '', '', '', '']
])
const colHeaders = ref(['A', 'B', 'C', 'D', 'E'])

const hotTableRef = ref()
const activeTab = ref('dataset')
const datasetSearch = ref('')
const datasetTree = ref<any[]>([])
const selectedDataset = ref<any>(null)
const datasetFields = ref<any[]>([])

const hotSettings = ref({
  width: '100%',
  height: 'auto',
  minRows: 50,
  minCols: 20,
  rowHeaders: true,
  colHeaders: true,
  manualRowResize: true,
  manualColResize: true,
  mergeCells: true,
  contextMenu: true,
  licenseKey: 'non-commercial-and-evaluation'
})

const columns = ref([
  { type: 'text' },
  { type: 'text' },
  { type: 'text' },
  { type: 'text' },
  { type: 'text' }
])

const handleBack = () => {
  router.push('/report')
}

const handleSaveDraft = async () => {
  const designContent = JSON.stringify({
    tableData: tableData.value,
    colHeaders: colHeaders.value
  })

  if (reportId.value) {
    await updateReport(reportId.value, {
      reportName: reportName.value,
      designContent,
      status: 0
    } as any)
  } else {
    const res = await createReport({
      reportName: reportName.value,
      designContent,
      status: 0
    } as any)
    reportId.value = res.data.id!
  }
  ElMessage.success('保存成功')
}

const handlePublish = async () => {
  if (!reportName.value) {
    ElMessage.warning('请输入报表名称')
    return
  }
  await handleSaveDraft()
  if (reportId.value) {
    await updateReport(reportId.value, { status: 1 } as any)
  }
  ElMessage.success('发布成功')
}

const insertRow = () => {
  tableData.value.push(['', '', '', '', ''])
}

const insertCol = () => {
  tableData.value.forEach(row => row.push(''))
  colHeaders.value.push(String.fromCharCode(65 + colHeaders.value.length))
  columns.value.push({ type: 'text' })
}

const loadDatasets = async () => {
  const res = await getDatasetList({ page: 1, size: 100 })
  datasetTree.value = res.data.records.map(ds => ({
    id: ds.id,
    label: ds.datasetName,
    type: ds.datasetType,
    ...ds
  }))
}

const handleDatasetClick = async (data: any) => {
  if (data.type) {
    selectedDataset.value = data
    const res = await getDatasetFields(data.id)
    datasetFields.value = res.data
  }
}

const bindField = (field: any) => {
  ElMessage.success(`字段 ${field.name} 已绑定`)
}

onMounted(async () => {
  await loadDatasets()

  if (reportId.value) {
    const res = await getReport(reportId.value)
    reportName.value = res.data.reportName
    const design = JSON.parse(res.data.designContent || '{}')
    if (design.tableData) {
      tableData.value = design.tableData
    }
    if (design.colHeaders) {
      colHeaders.value = design.colHeaders
    }
  }
})
</script>

<style scoped lang="scss">
.report-designer {
  height: 100vh;

  .designer-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: #fff;
    border-bottom: 1px solid #e8e8e8;
    padding: 0 16px;

    .header-left, .header-right {
      display: flex;
      align-items: center;
    }
  }

  .designer-body {
    height: calc(100vh - 60px);

    .toolbar {
      background: #fff;
      padding: 8px;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 4px;
      border-right: 1px solid #e8e8e8;

      .el-button {
        width: 40px;
        height: 40px;
      }
    }

    .designer-main {
      padding: 16px;
      background: #f5f7fa;

      .table-container {
        background: #fff;
        padding: 16px;
        border-radius: 4px;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
      }
    }

    .dataset-panel {
      background: #fff;
      padding: 16px;
      border-left: 1px solid #e8e8e8;

      .dataset-list {
        .el-input {
          margin-bottom: 16px;
        }
      }

      .binding-panel {
        .selected-dataset {
          font-weight: bold;
          margin-bottom: 12px;
        }
      }
    }
  }
}
</style>
