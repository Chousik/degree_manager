import './assets/tailwind.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from "./route.js";

createApp(App).use(router).mount('#app')
