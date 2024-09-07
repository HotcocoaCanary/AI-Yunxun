import http from '@/utils/http';

export async function getGraph(answer) {
    try {
        return await http.post('/answer', {
            answer: answer
        });
    } catch (error) {
        console.error('请求失败', error);
    }
}
