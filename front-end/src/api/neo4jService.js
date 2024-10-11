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

export async function getExcel(fileName,page = 1, pageSize = 10) {
    try {
        // 发送 POST 请求并解构响应数据
        const { data: { headers, data, total } } = await http.post('/excel', { fileName, page, pageSize});
        // 返回去重后的节点和关系
        return {
            headers,
            data,
            total
        };
    } catch (error) {
        console.error('请求失败', error);
    }
}

export async function getPermissions(password) {
    try {
        // 发送 POST 请求并解构响应数据
        const { code, data, message } = await http.post('/accredit', {password});
        return {
            code,
            data,
            message
        }
    } catch (error) {
        console.error('请求失败', error);
    }
}

async function sendRequest(url, body, token) {
    try {
        const {code, data, message} = await http.post(url, body, {
            headers: {
                I_am_the_administrator_of_AI_Yun_xun: token
            }
        });
        return {code, data, message};
    } catch (error) {
        console.error('请求失败', error);
        // 返回错误信息
        return {code: -1, message: '请求失败', error};
    }
}

export async function update(token) {
    return sendRequest('/data/update', {}, token);
}

export async function revertAllUploads(token) {
    return sendRequest('/data/revertAll', {}, token);
}

export async function revertUpload(fileName, token) {
    return sendRequest('/data/revert', { fileName }, token);
}

export async function getTemplate(token) {
    return sendRequest('/data/template', {}, token);
}
