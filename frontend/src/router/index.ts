import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'report',
        name: 'ReportList',
        component: () => import('@/views/report/ReportList.vue'),
        meta: { title: '报表管理' }
      },
      {
        path: 'report/designer/:id?',
        name: 'ReportDesigner',
        component: () => import('@/views/report/ReportDesigner.vue'),
        meta: { title: '报表设计器' }
      },
      {
        path: 'report/preview/:id',
        name: 'ReportPreview',
        component: () => import('@/views/report/ReportPreview.vue'),
        meta: { title: '报表预览' }
      },
      {
        path: 'dataset',
        name: 'DatasetList',
        component: () => import('@/views/dataset/DatasetList.vue'),
        meta: { title: '数据集管理' }
      },
      {
        path: 'dataset/create/:type',
        name: 'DatasetCreate',
        component: () => import('@/views/dataset/DatasetCreate.vue'),
        meta: { title: '创建数据集' }
      },
      {
        path: 'dataset/edit/:id',
        name: 'DatasetEdit',
        component: () => import('@/views/dataset/DatasetCreate.vue'),
        meta: { title: '编辑数据集' }
      },
      {
        path: 'system/user',
        name: 'UserManagement',
        component: () => import('@/views/system/UserManagement.vue'),
        meta: { title: '用户管理', roles: ['ADMIN'] }
      },
      {
        path: 'system/log',
        name: 'OperationLog',
        component: () => import('@/views/system/OperationLog.vue'),
        meta: { title: '操作日志', roles: ['ADMIN'] }
      },
      {
        path: 'system/config',
        name: 'SystemConfig',
        component: () => import('@/views/system/SystemConfig.vue'),
        meta: { title: '系统配置', roles: ['ADMIN'] }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else {
    next()
  }
})

export default router
