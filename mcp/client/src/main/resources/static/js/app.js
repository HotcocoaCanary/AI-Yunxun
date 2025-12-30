const els = {
  messages: document.getElementById('messages'),
  toolLogs: document.getElementById('toolLogs'),
  status: document.getElementById('status'),
  form: document.getElementById('chatForm'),
  input: document.getElementById('input'),
};

function nowTime() {
  const d = new Date();
  return d.toLocaleTimeString();
}

function addMsg(role, content) {
  const div = document.createElement('div');
  div.className = `msg ${role}`;
  div.innerHTML = `
    <div class="meta">${role === 'user' ? '你' : '助手'} · ${nowTime()}</div>
    <div class="content"></div>
  `;
  div.querySelector('.content').textContent = content || '';
  els.messages.appendChild(div);
  els.messages.scrollTop = els.messages.scrollHeight;
  return div.querySelector('.content');
}

function addLog(line) {
  const div = document.createElement('div');
  div.className = 'log';
  div.innerHTML = `<div class="line">${line}</div>`;
  els.toolLogs.appendChild(div);
  els.toolLogs.scrollTop = els.toolLogs.scrollHeight;
}

function setStatus(text) {
  els.status.textContent = text;
}

// --- ECharts ---
let chart = null;
function ensureChart() {
  const el = document.getElementById('chart');
  if (!chart) {
    chart = echarts.init(el);
    window.addEventListener('resize', () => chart.resize());
  }
  return chart;
}

function renderChart(optionJson) {
  try {
    const opt = JSON.parse(optionJson);
    ensureChart().setOption(opt, true);
  } catch (e) {
    addLog(`[chart] invalid option: ${e.message}`);
  }
}

// --- STOMP over WebSocket ---
function connectToolLogs() {
  const wsUrl = `${location.protocol === 'https:' ? 'wss' : 'ws'}://${location.host}/ws`;
  try {
    const socket = new WebSocket(wsUrl);
    const client = Stomp.over(socket);
    client.debug = () => {};
    client.connect({}, () => {
      setStatus('WebSocket已连接');
      client.subscribe('/topic/tool-logs', (frame) => {
        try {
          const evt = JSON.parse(frame.body);
          addLog(`[${evt.server}] ${evt.level} ${evt.logger}: ${evt.message}`);
        } catch (e) {
          addLog(`[tool-logs] ${frame.body}`);
        }
      });
    }, (err) => {
      setStatus('WebSocket连接失败');
      addLog(`[ws] ${JSON.stringify(err)}`);
    });
  } catch (e) {
    setStatus('WebSocket不可用');
    addLog(`[ws] ${e.message}`);
  }
}

// --- SSE over fetch (POST) ---
async function postSse(url, body, onEvent) {
  const res = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });

  if (!res.ok || !res.body) {
    throw new Error(`HTTP ${res.status}`);
  }

  const reader = res.body.getReader();
  const decoder = new TextDecoder('utf-8');

  let buf = '';
  while (true) {
    const { done, value } = await reader.read();
    if (done) break;
    buf += decoder.decode(value, { stream: true });

    // SSE event separator
    let idx;
    while ((idx = buf.indexOf('\n\n')) !== -1) {
      const raw = buf.slice(0, idx);
      buf = buf.slice(idx + 2);
      handleSseBlock(raw, onEvent);
    }
  }
}

function handleSseBlock(raw, onEvent) {
  const lines = raw.split('\n').map((l) => l.trimEnd());
  let event = 'message';
  let dataLines = [];
  for (const l of lines) {
    if (l.startsWith('event:')) event = l.slice('event:'.length).trim();
    if (l.startsWith('data:')) dataLines.push(l.slice('data:'.length).trim());
  }
  const data = dataLines.join('\n');
  if (data) onEvent(event, data);
}

// --- UI wiring ---
let assistantContentEl = null;

els.form.addEventListener('submit', async (e) => {
  e.preventDefault();
  const text = (els.input.value || '').trim();
  if (!text) return;

  addMsg('user', text);
  assistantContentEl = addMsg('assistant', '');
  els.input.value = '';

  try {
    setStatus('SSE对话中…');
    await postSse('/api/chat', { message: text }, (event, data) => {
      try {
        const payload = JSON.parse(data);
        if (payload.type === 'status') {
          setStatus(payload.content === 'thinking' ? '思考中…' : '完成');
          return;
        }
        if (payload.type === 'text') {
          assistantContentEl.textContent += payload.content;
          els.messages.scrollTop = els.messages.scrollHeight;
          return;
        }
        if (payload.type === 'chart') {
          renderChart(payload.content);
          return;
        }
      } catch (_) {
        // fallback
        addLog(`[sse:${event}] ${data}`);
      }
    });
  } catch (err) {
    setStatus('SSE失败');
    addLog(`[sse] ${err.message}`);
  }
});

connectToolLogs();



