import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/HomePage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/UserLogin.vue'),
  },
  {
    path: '/share/:token',
    name: 'Share',
    component: () => import('@/views/SharePage.vue'),
  },
  {
    path: '/home/:ownerUsername',
    name: 'PublicHome',
    component: () => import('@/views/PublicHomePage.vue'),
  },
  {
    path: '/pets/:id',
    name: 'PetDetail',
    component: () => import('@/views/PetDetailPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/community',
    name: 'Community',
    component: () => import('@/views/CommunityPage.vue'),
    meta: { requiresAuth: true },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  if (to.meta.requiresAuth && !getToken()) {
    return '/login'
  }
  if (to.path === '/login' && getToken()) {
    return '/'
  }
  return true
})

export default router
