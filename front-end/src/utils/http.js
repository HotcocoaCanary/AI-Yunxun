// src/utils/http.js
import axios from 'axios';

// 创建一个axios实例
const http = axios.create({
    baseURL: '/api', // 设置API的基础地址
    timeout: 10000, // 设置请求超时时间
    headers: {
        'Content-Type': 'application/json;charset=UTF-8'
    }
});

// 请求拦截器
http.interceptors.request.use(
    config => {
        return config;
    },
    error => {
        return Promise.reject(error);
    }
);

// 响应拦截器
http.interceptors.response.use(
    response => {
        // 对响应数据处理
        const res = response.data;
        // 根据返回的状态码进行一些操作，例如登录过期、错误提示等。
        if (res.code !== 200) {
            // 业务错误处理，可以根据实际情况进行调整
            return Promise.reject(new Error(res.message || 'Error'));
        } else {
            return res;
        }
    },
    error => {
        // 对响应错误处理
        return Promise.reject(error);
    }
);

export default http;
