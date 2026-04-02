import { createRouter, createWebHistory } from 'vue-router'
import { getCurrentUser, getToken } from '@/utils/auth'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/HomePage.vue'),
    meta: { requiresAuth: true, allowGuest: false },
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
    meta: { requiresAuth: true, allowGuest: false },
  },
  {
    path: '/home/:ownerUsername',
    name: 'PublicHome',
    component: () => import('@/views/PublicHomePage.vue'),
    meta: { requiresAuth: true, allowGuest: false },
  },
  {
    path: '/pets/:id',
    name: 'PetDetail',
    component: () => import('@/views/PetDetailPage.vue'),
    meta: { requiresAuth: true, allowGuest: false },
  },
  {
    path: '/community',
    name: 'Community',
    component: () => import('@/views/CommunityPage.vue'),
    meta: { requiresAuth: true, allowGuest: true },
  },
  {
    path: '/admin',
    name: 'AdminCenter',
    component: () => import('@/views/AdminCenterPage.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const token = getToken()
  const currentUser = getCurrentUser()
  const role = currentUser?.role || ''
  const isAdmin = role === 'ROLE_ADMIN'
  const isGuest = role === 'ROLE_GUEST'
  const requiresAuth = !!to.meta.requiresAuth
  const allowGuest = to.meta.allowGuest === true

  if (requiresAuth && !token) {
    return {
      path: '/login',
      query: { redirect: to.fullPath },
    }
  }

  if (isAdmin && requiresAuth && to.path !== '/admin') {
    return '/admin'
  }

  if (isGuest) {
    if (to.meta.requiresAdmin) {
      return '/community'
    }
    if (requiresAuth && !allowGuest) {
      return '/community'
    }
  }

  if (to.meta.requiresAdmin) {
    if (currentUser?.role !== 'ROLE_ADMIN') {
      return isGuest ? '/community' : '/'
    }
  }

  if (to.path === '/login' && token) {
    if (isAdmin) {
      return '/admin'
    }
    if (isGuest) {
      return true
    }
    const redirect = typeof to.query.redirect === 'string' ? to.query.redirect : ''
    if (redirect.startsWith('/')) {
      return redirect
    }
    return '/'
  }

  return true
})

export default router
