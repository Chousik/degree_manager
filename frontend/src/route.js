import { createRouter, createWebHistory } from 'vue-router'
import Login from './pages/LoginPage.vue'
import WorkList from './pages/WorkListPage.vue'
import WorkPreview from './pages/WorkPreviewPage.vue'
import AdminPanel from "@/pages/AdminPanel.vue";

const routes = [
    { path: '/', redirect: '/login' },
    { path: '/login', component: Login },
    { path: '/works', component: WorkList },
    { path: '/preview', component: WorkPreview},
    { path: '/admin', component: AdminPanel}
]

const router = createRouter({
    history: createWebHistory(),
    routes,
})

// router.beforeEach((to, from, next) => {
//     const token = localStorage.getItem('jwtToken')
//     if (to.meta.requiresAuth && !token) {
//         return next('/login')
//     }
//     next()
// })

export default router
