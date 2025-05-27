import './assets/tailwind.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from "./route.js";
import {createPinia} from "pinia";
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'

const pinia = createPinia().use(piniaPluginPersistedstate)
createApp(App).use(pinia).use(router).mount('#app')
