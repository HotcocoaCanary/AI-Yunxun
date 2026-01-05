const els = {
  messages: document.getElementById('messages'),
  toolLogs: document.getElementById('toolLogs'),
  status: document.getElementById('status'),
  form: document.getElementById('chatForm'),
  input: document.getElementById('input'),
  serverList: document.getElementById('serverList'),
  mcpInput: document.getElementById('mcpInput'),
  importBtn: document.getElementById('importBtn'),
  refreshBtn: document.getElementById('refreshBtn'),
  toolStatus: document.getElementById('toolStatus'),
  conversationId: document.getElementById('conversationId'),
  newConversationBtn: document.getElementById('newConversationBtn'),
};

let currentConversationId = null;

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

function generateConversationId() {
  if (window.crypto && window.crypto.randomUUID) {
    return window.crypto.randomUUID();
  }
  return `cid-${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

function setConversationId(id) {
  currentConversationId = id;
  localStorage.setItem('conversationId', id);
  els.conversationId.textContent = id;
}

function ensureConversationId() {
  const saved = localStorage.getItem('conversationId');
  if (saved) {
    setConversationId(saved);
  } else {
    setConversationId(generateConversationId());
  }
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

// --- MCP management ---
async function fetchServers() {
  const res = await fetch('/api/mcp/servers');
  if (!res.ok) {
    throw new Error(`HTTP ${res.status}`);
  }
  const data = await res.json();
  return data.mcpServers || {};
}

function renderServers(servers) {
  els.serverList.innerHTML = '';
  const entries = Object.entries(servers);
  if (entries.length === 0) {
    const empty = document.createElement('div');
    empty.className = 'server-item';
    empty.textContent = '暂无外部 MCP 服务';
    els.serverList.appendChild(empty);
    return;
  }
  for (const [name, def] of entries) {
    const item = document.createElement('div');
    item.className = 'server-item';
    const cmd = [def.command, ...(def.args || [])].join(' ');
    item.innerHTML = `
      <div>
        <div class="name">${name}</div>
        <div class="cmd">${cmd}</div>
      </div>
      <button class="remove" data-name="${name}">移除</button>
    `;
    item.querySelector('.remove').addEventListener('click', async () => {
      await deleteServer(name);
    });
    els.serverList.appendChild(item);
  }
}

async function refreshServers() {
  try {
    const servers = await fetchServers();
    renderServers(servers);
  } catch (e) {
    addLog(`[mcp] ${e.message}`);
  }
}

async function importServersFromText() {
  const raw = (els.mcpInput.value || '').trim();
  if (!raw) {
    addLog('[mcp] empty config');
    return;
  }
  let parsed;
  try {
    parsed = JSON.parse(raw);
  } catch (e) {
    addLog(`[mcp] invalid json: ${e.message}`);
    return;
  }
  const config = parsed.mcpServers ? parsed : { mcpServers: parsed };
  const res = await fetch('/api/mcp/servers', {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(config),
  });
  if (!res.ok) {
    addLog(`[mcp] import failed: HTTP ${res.status}`);
    return;
  }
  addLog('[mcp] imported');
  await refreshServers();
}

async function deleteServer(name) {
  const res = await fetch(`/api/mcp/servers/${encodeURIComponent(name)}`, {
    method: 'DELETE',
  });
  if (!res.ok) {
    addLog(`[mcp] delete failed: HTTP ${res.status}`);
    return;
  }
  addLog(`[mcp] removed ${name}`);
  await refreshServers();
}

// --- Tool status ---
function inferToolName(evt) {
  if (!evt) return 'unknown';
  if (evt.logger === 'echart-tool') {
    const msg = evt.message || '';
    if (msg.includes('柱状图')) return 'echart.bar';
    if (msg.includes('折线图')) return 'echart.line';
    if (msg.includes('饼图')) return 'echart.pie';
    if (msg.includes('关系图')) return 'echart.graph';
    return 'echart';
  }
  if (evt.logger === 'neo4j-tool') {
    const msg = evt.message || '';
    if (msg.includes('架构')) return 'neo4j.schema';
    if (msg.includes('读取')) return 'neo4j.read';
    if (msg.includes('写入')) return 'neo4j.write';
    return 'neo4j';
  }
  return evt.logger || 'tool';
}

function inferToolState(evt) {
  const msg = evt.message || '';
  if ((evt.level || '').toUpperCase() === 'ERROR') {
    return { label: '失败', className: 'failed' };
  }
  if (msg.includes('开始')) {
    return { label: '调用中', className: 'running' };
  }
  if (msg.includes('完成') || msg.includes('成功')) {
    return { label: '成功', className: 'success' };
  }
  return { label: '处理中', className: 'running' };
}

function updateToolStatus(evt) {
  const tool = inferToolName(evt);
  const state = inferToolState(evt);
  els.toolStatus.textContent = `工具：${tool} · ${state.label}`;
  els.toolStatus.className = `tool-status ${state.className}`;
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
          updateToolStatus(evt);
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
    await postSse('/api/chat', { conversationId: currentConversationId, message: text }, (event, data) => {
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
        }
      } catch (_) {
        addLog(`[sse:${event}] ${data}`);
      }
    });
  } catch (err) {
    setStatus('SSE失败');
    addLog(`[sse] ${err.message}`);
  }
});

els.importBtn.addEventListener('click', () => {
  importServersFromText();
});

els.refreshBtn.addEventListener('click', () => {
  refreshServers();
});

els.newConversationBtn.addEventListener('click', () => {
  setConversationId(generateConversationId());
  els.messages.innerHTML = '';
  if (chart) {
    chart.clear();
  }
  addLog(`[chat] new conversation ${currentConversationId}`);
});

ensureConversationId();
refreshServers();
connectToolLogs();
