import {createRouter, createWebHistory} from 'vue-router'
import Layout from "@/views/Layout.vue";
import Home from "@/views/home/Home.vue";
import Chat from "@/views/chat/Chat.vue";
import OverAll from "@/views/relation/OverAll.vue";
import Maintain from "@/views/relation/Maintain.vue";

//导入组件

//定义路由关系
const routes = [
    {
        path: '/',
        component: Layout,
        redirect: '/home',
        children: [
            {
                path: '/home',
                component: Home,
                meta: { title: '首页' },
            },
            {
                path: '/relation/overall',
                component: OverAll,
                meta: { title: '关系全貌'}
            },
            {
                path: '/relation/maintain',
                component: Maintain,
                meta: { title: '信息维护'}
            },
            {
                path: '/chat',
                component: Chat,
                meta: { title: '问答系统'}
            }
        ]
    }
]

//创建路由器
const router = createRouter({
    history: createWebHistory(),
    routes: routes
})

//导出路由
export default router
