const BACKEND_BASE_URL =
  process.env.BACKEND_BASE_URL || "http://localhost:8080";

type ChatRequestBody = {
  message: string;
};

export async function POST(req: Request) {
  const body = (await req.json()) as ChatRequestBody;

  const res = await fetch(`${BACKEND_BASE_URL}/api/chat`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ message: body.message }),
  });

  if (!res.ok) {
    return new Response(
      JSON.stringify({ error: "Backend chat request failed" }),
      { status: 500 },
    );
  }

  const data = (await res.json()) as { reply: string };

  return Response.json(data);
}

