import {createRouter, createWebHistory} from 'vue-router'
import Login from "@/views/Login.vue";
import Layout from "@/views/Layout.vue";
import Home from "@/views/home/Home.vue";
import Chat from "@/views/chat/Chat.vue";
import OverAll from "@/views/relation/OverAll.vue";
import Search from "@/views/relation/Search.vue";

//导入组件

//定义路由关系
const routes = [
    {
        path: '/login',
        component:Login
    },
    {
        path: '/',
        component: Layout,
        redirect: '/home',
        children: [
            {
                path: '/home',
                component: Home
            },
            {
                path: '/relation/overall',
                component: OverAll
            },
            {
                path: '/relation/search',
                component: Search
            },
            {
                path: '/chat',
                component: Chat
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
