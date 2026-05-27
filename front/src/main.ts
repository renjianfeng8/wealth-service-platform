import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import './styles/theme.css'
import './styles/global.css'
import App from './App.vue'
import router from './router'

const app = createApp(App)

// 全局错误处理 — 捕获组件树外的未处理异常
app.config.errorHandler = (err, _instance, info) => {
  console.error('[GlobalError]', err, info)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')
