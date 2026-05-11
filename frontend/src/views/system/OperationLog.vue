<template>
  <div class="operation-log">
    <el-card>
      <template #header>
        <div class="header-actions">
          <el-form :inline="true" :model="searchForm">
            <el-form-item label="模块">
              <el-select v-model="searchForm.module" clearable style="width: 150px">
                <el-option label="数据集" value="DATASET" />
                <el-option label="报表" value="REPORT" />
                <el-option label="用户" value="USER" />
              </el-select>
            </el-form-item>
            <el-form-item label="操作人">
              <el-input v-model="searchForm.operator" clearable style="width: 150px" />
            </el-form-item>
            <el-form-item label="时间范围">
              <el-date-picker
                v-model="dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">查询</el-button>
            </el-form-item>
          </el-form>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="operation" label="操作" width="120" />
        <el-table-column prop="module" label="模块" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.module === 'DATASET'" type="success">数据集</el-tag>
            <el-tag v-else-if="row.module === 'REPORT'" type="warning">报表</el-tag>
            <el-tag v-else type="info">用户</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operator" label="操作人" width="120" />
        <el-table-column prop="ip" label="IP地址" width="150" />
        <el-table-column prop="method" label="请求方法" width="150" />
        <el-table-column prop="createTime" label="操作时间" width="180" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
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

    <el-dialog v-model="detailVisible" title="日志详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="模块">{{ currentLog.module }}</el-descriptions-item>
        <el-descriptions-item label="操作">{{ currentLog.operation }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ currentLog.operator }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ currentLog.ip }}</el-descriptions-item>
        <el-descriptions-item label="请求方法" :span="2">{{ currentLog.method }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <pre style="margin: 0; max-height: 200px; overflow: auto">{{ currentLog.params }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="执行结果" :span="2">
          <pre style="margin: 0; max-height: 200px; overflow: auto">{{ currentLog.result }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'

const loading = ref(false)
const tableData = ref<any[]>([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const dateRange = ref<string[]>([])

const searchForm = reactive({
  module: '',
  operator: ''
})

const detailVisible = ref(false)
const currentLog = ref<any>({})

const loadData = () => {
  tableData.value = [
    { id: '1', module: 'REPORT', operation: '创建报表', operator: 'admin', ip: '192.168.1.1', method: 'POST /api/report', params: '{"reportName":"测试报表"}', result: '{"code":200}', createTime: '2024-01-15 10:00:00' },
    { id: '2', module: 'DATASET', operation: '测试数据集', operator: 'designer', ip: '192.168.1.2', method: 'POST /api/dataset/1/test', params: '{}', result: '{"success":true}', createTime: '2024-01-15 11:00:00' }
  ]
  total.value = 2
}

const handleSearch = () => {
  page.value = 1
  loadData()
}

const handleDetail = (row: any) => {
  currentLog.value = row
  detailVisible.value = true
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.operation-log {
  .header-actions {
    display: flex;
    align-items: center;
  }
  
  .pagination {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
