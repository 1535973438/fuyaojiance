import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/medicines',
    name: 'MedicineList',
    component: () => import('@/views/MedicineList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/medicine/edit/:id?',
    name: 'MedicineEdit',
    component: () => import('@/views/MedicineEdit.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/schedules',
    name: 'ScheduleList',
    component: () => import('@/views/ScheduleList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/schedule/edit/:id?',
    name: 'ScheduleEdit',
    component: () => import('@/views/ScheduleEdit.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/checks',
    name: 'CheckList',
    component: () => import('@/views/CheckList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/check/add',
    name: 'CheckAdd',
    component: () => import('@/views/CheckEdit.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/check/edit/:id',
    name: 'CheckEdit',
    component: () => import('@/views/CheckEdit.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/statistics',
    name: 'Statistics',
    component: () => import('@/views/Statistics.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  
  // 如果访问登录页且已登录，跳转到首页
  if (to.path === '/login' && token) {
    next('/')
    return
  }
  
  // 如果需要认证且未登录，跳转到登录页
  if (to.meta.requiresAuth && !token) {
    next('/login')
    return
  }
  
  next()
})

export default router
