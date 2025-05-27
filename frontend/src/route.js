import { createRouter, createWebHistory } from 'vue-router'
import Login from './pages/LoginPage.vue'
import WorkList from './pages/WorkListPage.vue'
import WorkPreview from './pages/WorkPreviewPage.vue'
import AdminPanel from './pages/AdminPanel.vue'
import AuthorizedPage from './pages/AuthorizedPage.vue'
import UploadPage from "@/pages/UploadPage.vue";
import {jwtDecode} from "jwt-decode";
import { useAuthStore } from '@/store/auth';

const routes = [
    { path: '/', redirect: '/login' },
    { path: '/login', component: Login },
    { path: '/auth-callback', component: AuthorizedPage },
    { path: '/works', component: WorkList },
    {
        path: '/preview/:title',
        component: WorkPreview
    },
    { path: '/upload', component: UploadPage},
    { path: '/admin', component: AdminPanel }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach((to, from, next) => {
    const authStore = useAuthStore();
    const publicPages = ['/login', '/auth-callback'];
    const accessToken = authStore.accessToken;

    if (accessToken) {
        try {
            if (!authStore.userInfo) {
                authStore.userInfo = jwtDecode(accessToken);
            }
            console.log(authStore.userInfo)
            const isAdmin = authStore.userInfo.roles?.includes('ROLE_ADMIN');

            if (to.path === '/admin' && !isAdmin) {
                next('/works');
            } else if (to.path !== '/admin' && isAdmin) {
                next('/admin');
            } else {
                next();
            }
        } catch (e) {
            console.error('Ошибка при декодировании токена', e);
            authStore.logout();
            next('/login');
        }
    } else {
        if (publicPages.includes(to.path)) {
            next();
        } else {
            next('/login');
        }
    }
});



export default router
