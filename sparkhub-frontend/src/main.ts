import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate' // 导入插件

// 1. 导入 Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// 导入全局样式
import './styles/global.css'

import App from './App.vue'
import router from './router'

const app = createApp(App)

const pinia = createPinia() // 创建 Pinia 实例
pinia.use(piniaPluginPersistedstate) // 注册插件

app.use(pinia) // 使用 Pinia
app.use(router)

// 2. 注册 Element Plus
app.use(ElementPlus)

app.mount('#app')
