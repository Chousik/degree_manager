import './assets/tailwind.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from "./route.js";
import {createPinia} from "pinia";

const pinia = createPinia()
createApp(App).use(pinia).use(router).mount('#app')
