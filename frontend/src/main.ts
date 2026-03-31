import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import setupElementPlus from './plugins/element-plus'

const app = createApp(App)
const pinia = createPinia()

setupElementPlus(app)
app.use(pinia)
app.use(router)

app.mount('#app')
