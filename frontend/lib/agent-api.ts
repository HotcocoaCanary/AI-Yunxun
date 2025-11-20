const BASE = '/api/agent';

async function handle<T>(request: Promise<Response>): Promise<T> {
  const res = await request;
  if (!res.ok) {
    throw new Error(await res.text());
  }
  return res.json();
}

export const fetchTools = <T = unknown>() =>
  handle<T>(fetch(`${BASE}/tools`, { cache: 'no-store' }));

export const fetchConversations = <T = unknown>() =>
  handle<T>(fetch(`${BASE}/conversations`, { cache: 'no-store' }));

export const fetchConversationDetail = <T = unknown>(id: string) =>
  handle<T>(fetch(`${BASE}/conversations/${id}`, { cache: 'no-store' }));

export const postChat = <T = unknown>(body: unknown) =>
  handle<T>(
    fetch(`${BASE}/chat`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    }),
  );
