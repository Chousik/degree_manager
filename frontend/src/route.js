import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/store/auth'  // Импорт хранилища аутентификации
import Login from './pages/LoginPage.vue'
import WorkList from './pages/WorkListPage.vue'
import WorkPreview from './pages/WorkPreviewPage.vue'
import AdminPanel from "@/pages/AdminPanel.vue";
import AuthorizedPage from './pages/AuthorizedPage.vue'

const routes = [
    { path: '/', redirect: '/login' },
    { path: '/login', component: Login },
    { path: '/works', component: WorkList },
    { path: '/preview', component: WorkPreview },
    { path: '/admin', component: AdminPanel },
    { path: '/auth-callback', component: AuthorizedPage } // ← Добавили сюда
]


const router = createRouter({
    history: createWebHistory(),
    routes,
})

// router.beforeEach((to, from, next) => {
//     const authStore = useAuthStore()
//
//     if (!authStore.isAuthenticated && to.path !== '/login') {
//         next('/login')
//     } else {
//         next()
//     }
// })

export default router
