export function ToolBox({ tool }: { tool: any }) {
    const isRunning = tool.status === 'running';

    return (
        <div className="border rounded-md bg-gray-50 text-sm font-mono overflow-hidden">
            <div className="flex items-center justify-between px-3 py-2 bg-gray-100 border-b">
                <div className="flex items-center gap-2">
                    {isRunning ? (
                        <div className="animate-spin h-3 w-3 border-2 border-blue-500 border-t-transparent rounded-full" />
                    ) : (
                        <div className="h-3 w-3 bg-green-500 rounded-full" />
                    )}
                    <span className="font-bold text-gray-700">{tool.name}</span>
                </div>
                <span className="text-[10px] text-gray-400">{tool.status.toUpperCase()}</span>
            </div>

            <div className="p-3 text-gray-600">
                <details>
                    <summary className="cursor-pointer hover:text-gray-900">查看参数</summary>
                    <pre className="mt-2 text-[11px] bg-white p-2 rounded border">
            {JSON.stringify(tool.args, null, 2)}
          </pre>
                </details>

                {tool.result && (
                    <div className="mt-2">
                        <div className="font-bold text-gray-800 border-t pt-2 mt-2">执行输出:</div>
                        <pre className="mt-1 text-[11px] whitespace-pre-wrap max-h-40 overflow-y-auto">
              {typeof tool.result === 'string' ? tool.result : JSON.stringify(tool.result, null, 2)}
            </pre>
                    </div>
                )}
            </div>
        </div>
    );
}