<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-value">{{ stats.reportCount }}</div>
              <div class="stat-label">报表总数</div>
            </div>
            <div class="stat-icon">
              <el-icon size="40" color="#409EFF"><Document /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-value">{{ stats.datasetCount }}</div>
              <div class="stat-label">数据集总数</div>
            </div>
            <div class="stat-icon">
              <el-icon size="40" color="#67C23A"><Connection /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-value">{{ stats.todayVisits }}</div>
              <div class="stat-label">今日访问</div>
            </div>
            <div class="stat-icon">
              <el-icon size="40" color="#E6A23C"><View /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-value">{{ stats.activeUsers }}</div>
              <div class="stat-label">活跃用户</div>
            </div>
            <div class="stat-icon">
              <el-icon size="40" color="#F56C6C"><User /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近报表</span>
              <el-button type="primary" link @click="$router.push('/report')">查看更多</el-button>
            </div>
          </template>
          <el-table :data="recentReports" style="width: 100%">
            <el-table-column prop="reportName" label="报表名称" />
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'">
                  {{ row.status === 1 ? '已发布' : '草稿' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button type="primary" link @click="$router.push(`/report/preview/${row.id}`)">预览</el-button>
                <el-button type="primary" link @click="$router.push(`/report/designer/${row.id}`)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>快捷操作</span>
            </div>
          </template>
          <div class="quick-actions">
            <div class="action-item" @click="$router.push('/report/designer')">
              <el-icon size="32" color="#409EFF"><Plus /></el-icon>
              <span>新建报表</span>
            </div>
            <div class="action-item" @click="$router.push('/dataset/create/SQL')">
              <el-icon size="32" color="#67C23A"><Connection /></el-icon>
              <span>SQL数据集</span>
            </div>
            <div class="action-item" @click="$router.push('/dataset/create/API')">
              <el-icon size="32" color="#E6A23C"><Link /></el-icon>
              <span>API数据集</span>
            </div>
            <div class="action-item" @click="$router.push('/dataset/create/FILE')">
              <el-icon size="32" color="#909399"><Upload /></el-icon>
              <span>文件数据集</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getReportList } from '@/api/report'

const stats = ref({
  reportCount: 0,
  datasetCount: 0,
  todayVisits: 0,
  activeUsers: 0
})

const recentReports = ref([])

onMounted(async () => {
  try {
    const res = await getReportList({ page: 1, size: 5 })
    recentReports.value = res.data.records
    stats.value.reportCount = res.data.total
  } catch (e) {
    // handle error
  }
})
</script>

<style scoped lang="scss">
.dashboard {
  padding: 0;
}

.stat-card {
  .stat-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .stat-info {
      .stat-value {
        font-size: 32px;
        font-weight: bold;
        color: #333;
      }
      .stat-label {
        font-size: 14px;
        color: #999;
        margin-top: 8px;
      }
    }
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  
  .action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 20px;
    background: #f5f7fa;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      background: #ecf5ff;
      transform: translateY(-2px);
    }
    
    span {
      font-size: 14px;
      color: #666;
    }
  }
}
</style>
