import http from '@/utils/http';

export async function search() {
    try {
        const response = await http.post('/search');
        console.log(response);
    } catch (error) {
        console.error('There was an error!', error);
    }
}
