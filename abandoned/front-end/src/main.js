import './assets/css/main.css';

import { createApp } from 'vue';
import router from "@/router/index.js";
import App from './App.vue';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import VueECharts from 'vue-echarts';

const app = createApp(App);

// 使用 ElementPlus
app.use(ElementPlus);

// 使用路由
app.use(router);

// 注册 VueECharts 为全局组件
app.component('v-chart', VueECharts);

// 挂载应用
app.mount('#app');
