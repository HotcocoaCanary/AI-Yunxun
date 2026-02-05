export interface Message {
    role: 'user' | 'assistant';
    content: string;
    tools?: ToolInvocation[]; // 存放该条消息关联的工具调用
    thinking?: string;
}

export interface ToolInvocation {
    callId: string;
    name: string;
    args: any;
    result?: any;
    status: 'running' | 'done' | 'error';
}
