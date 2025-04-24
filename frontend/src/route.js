import { createRouter, createWebHistory } from 'vue-router'
import Login from './pages/LoginPage.vue'
import WorkList from './pages/WorkListPage.vue'
import WorkPreview from './pages/WorkPreviewPage.vue'
import AdminPanel from './pages/AdminPanel.vue'
import AuthorizedPage from './pages/AuthorizedPage.vue'
import UploadPage from "@/pages/UploadPage.vue";
import {jwtDecode} from "jwt-decode";

const routes = [
    { path: '/', redirect: '/login' },
    { path: '/login', component: Login },
    { path: '/auth-callback', component: AuthorizedPage },
    { path: '/works', component: WorkList },
    { path: '/preview', component: WorkPreview },
    { path: '/upload', component: UploadPage},
    { path: '/admin', component: AdminPanel }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach((to, from, next) => {
    const publicPages = ['/login', '/auth-callback']
    const accessToken = localStorage.getItem('access_token')

    if (accessToken && typeof accessToken === 'string') {
        try {
            const decoded = jwtDecode(accessToken);

            const isAdmin = decoded.roles.includes('ROLE_ADMIN');

            if (to.path === '/admin' && !isAdmin) {
                next('/works');
            }
            else if (to.path !== '/admin' && isAdmin) {
                next();
            }
            else {
                next();
            }
        } catch (error) {
            console.error('Error decoding JWT:', error);
            next('/login');
        }
    } else {
        if (publicPages.includes(to.path)) {
            next();
        } else {
            next('/login');
        }
    }
})


export default router
