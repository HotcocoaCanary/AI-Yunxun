import http from '@/utils/http';

export async function getGraph(answer) {
    try {
        // 发送 POST 请求并解构响应数据
        const { data: { nodes, relationships } } = await http.post('/answer', { answer });

        // 使用 Set 和 reduce 方法去重节点
        const uniqueNodes = nodes.reduce((acc, node) => {
            // 如果 Set 中没有当前节点的 name，则添加到结果数组中
            if (!acc.names.has(node.name)) {
                acc.names.add(node.name);
                acc.result.push(node);
            }
            return acc;
        }, { names: new Set(), result: [] }).result;

        // 返回去重后的节点和关系
        return {
            nodes: uniqueNodes,
            relationships
        };
    } catch (error) {
        console.error('请求失败', error);
    }
}
