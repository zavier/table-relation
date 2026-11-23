import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import './style.css'

// 跟随系统深浅色模式：Element Plus 的暗色主题通过 html.dark 生效
const darkModeQuery = window.matchMedia('(prefers-color-scheme: dark)')
const applyColorScheme = (isDark) => {
  document.documentElement.classList.toggle('dark', isDark)
}
applyColorScheme(darkModeQuery.matches)
darkModeQuery.addEventListener('change', (event) => applyColorScheme(event.matches))

const app = createApp(App)
app.use(router)
app.use(ElementPlus)
app.mount('#app')
