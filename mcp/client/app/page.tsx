"use client";
import { useEffect, useState } from "react";

type Tools = {
    a: { id: string; name: string }[];
    b: { id: string; name: string }[];
};

export default function Page() {
    const [tools, setTools] = useState<Tools | null>(null);
    const [output, setOutput] = useState<string | null>(null);

    useEffect(() => {
        fetch("/api/mcp/tools")
            .then(res => res.json())
            .then(data => setTools(data));
    }, []);

    const handleCall = async (server: "a" | "b", toolId: string) => {
        setOutput("Calling...");
        const res = await fetch("/api/mcp/call", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ server, toolId, input: {} }),
        });
        const data = await res.json();
        setOutput(JSON.stringify(data.result, null, 2));
    };

    if (!tools) return <div>Loading tools...</div>;

    return (
        <div style={{ padding: "2rem" }}>
            <h1>MCP Web Console</h1>

            <h2>Server A</h2>
            <ul>
                {tools.a.map((tool, idx) => (
                    <li key={`${tool.id ?? tool.name ?? "tool"}-${idx}`}>
                        {tool.name}{" "}
                        <button onClick={() => handleCall("a", tool.id)}>Call</button>
                    </li>
                ))}
            </ul>

            <h2>Server B</h2>
            <ul>
                {tools.b.map((tool, idx) => (
                    <li key={`${tool.id ?? tool.name ?? "tool"}-${idx}`}>
                        {tool.name}{" "}
                        <button onClick={() => handleCall("b", tool.id)}>Call</button>
                    </li>
                ))}
            </ul>

            <h3>Output</h3>
            <pre>{output}</pre>
        </div>
    );
}
