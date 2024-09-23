import http from '@/utils/http';

export async function getAnswerGraph(answer) {
    try {
        // 发送 POST 请求并解构响应数据
        const { data: { nodes, relationships } } = await http.post('/answer', { answer });
        // 返回去重后的节点和关系
        return {
            nodes,
            relationships
        };
    } catch (error) {
        console.error('请求失败', error);
    }
}

export async function getAllGraph() {
    try {
        // 发送 POST 请求并解构响应数据
        const { data: { nodes, relationships } } = await http.post('/all');
        // 返回去重后的节点和关系
        return {
            nodes,
            relationships
        };
    } catch (error) {
        console.error('请求失败', error);
    }
}

export async function getSearchGraph(keyword) {
    try {
        // 发送 POST 请求并解构响应数据
        const { data: { nodes, relationships } } = await http.post('/search', { keyword });
        // 返回去重后的节点和关系
        return {
            nodes,
            relationships
        };
    } catch (error) {
        console.error('请求失败', error);
    }
}
