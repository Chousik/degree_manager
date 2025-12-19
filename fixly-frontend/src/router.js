import { createRouter, createWebHistory } from 'vue-router';
import AuthPage from './pages/AuthPage.vue';
import ListingsPage from './pages/ListingsPage.vue';
import PublicListingsPage from './pages/PublicListingsPage.vue';
import AuthCallback from './pages/AuthCallback.vue';
import RentalsPage from './pages/RentalsPage.vue';
import FavoritesPage from './pages/FavoritesPage.vue';
import ListingDetailPage from './pages/ListingDetailPage.vue';
import AccountPage from './pages/AccountPage.vue';
import CommunicationPage from './pages/CommunicationPage.vue';
import ModerationPage from './pages/ModerationPage.vue';
import SupportPage from './pages/SupportPage.vue';
import { useSession } from './state/session';

const routes = [
  { path: '/', redirect: '/catalog' },
  { path: '/catalog', component: PublicListingsPage },
  { path: '/catalog/:id', component: ListingDetailPage },
  { path: '/auth', component: AuthPage, meta: { guestOnly: true } },
  { path: '/auth-callback', component: AuthCallback, meta: { guestOnly: true } },
  { path: '/listings', component: ListingsPage, meta: { requiresAuth: true } },
  { path: '/rentals', component: RentalsPage, meta: { requiresAuth: true } },
  { path: '/favorites', component: FavoritesPage, meta: { requiresAuth: true } },
  { path: '/account', component: AccountPage, meta: { requiresAuth: true } },
  { path: '/communication', component: CommunicationPage, meta: { requiresAuth: true } },
  { path: '/moderation', component: ModerationPage, meta: { requiresAuth: true } },
  { path: '/support', component: SupportPage, meta: { requiresAuth: true } },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to) => {
  const { isLoggedIn } = useSession();
  if (to.meta.requiresAuth && !isLoggedIn.value) {
    return '/auth';
  }
  if (to.meta.guestOnly && isLoggedIn.value) {
    return '/listings';
  }
  return true;
});

export default router;
