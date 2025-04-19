import { createRouter, createWebHistory } from 'vue-router';
import Login from "@/components/loginform.vue";

const routes = [
    { path: '/login', component: Login }
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('jwtToken');

    if (token && to.path === '/login') {
        return next('/main');
    }

    if (to.meta.requiresAuth && !token) {
        return next('/');
    }
    next();
});


export default router;
