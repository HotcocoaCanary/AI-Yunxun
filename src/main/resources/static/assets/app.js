(() => {
  const state = {
    sessionId: null,
    streaming: true,
    lastAssistantEl: null,
    abortController: null,
    chart: null,
  };

  const el = {
    status: document.getElementById("status-badge"),
    streamToggle: document.getElementById("stream-toggle"),
    chatList: document.getElementById("chat-list"),
    input: document.getElementById("chat-input"),
    sendBtn: document.getElementById("send-btn"),
    sources: document.getElementById("sources"),
    debug: document.getElementById("debug"),
    chart: document.getElementById("chart"),
    chartHint: document.getElementById("chart-hint"),
  };

  function setStatus(text, kind) {
    el.status.textContent = text;
    const color =
      kind === "ok" ? "var(--ok)" :
      kind === "warn" ? "var(--warn)" :
      kind === "err" ? "var(--err)" : "rgba(255,255,255,0.7)";
    el.status.style.borderColor = color;
    el.status.style.color = color;
  }

  function addMessage(role, content) {
    const wrap = document.createElement("div");
    wrap.className = `msg ${role}`;
    const roleEl = document.createElement("div");
    roleEl.className = "role";
    roleEl.textContent = role;
    const bubble = document.createElement("div");
    bubble.className = "bubble";
    bubble.textContent = content ?? "";
    wrap.appendChild(roleEl);
    wrap.appendChild(bubble);
    el.chatList.appendChild(wrap);
    el.chatList.scrollTop = el.chatList.scrollHeight;
    return bubble;
  }

  function appendToAssistant(token) {
    if (!state.lastAssistantEl) {
      state.lastAssistantEl = addMessage("assistant", "");
    }
    state.lastAssistantEl.textContent += token;
    el.chatList.scrollTop = el.chatList.scrollHeight;
  }

  function clearSide() {
    el.sources.innerHTML = "";
    el.debug.textContent = "";
    el.chartHint.style.display = "block";
    if (state.chart) {
      try { state.chart.dispose(); } catch (_) {}
      state.chart = null;
    }
    el.chart.innerHTML = "";
  }

  function renderSources(sources) {
    el.sources.innerHTML = "";
    if (!Array.isArray(sources) || sources.length === 0) {
      const li = document.createElement("li");
      li.innerHTML = `<span class="muted">无</span>`;
      el.sources.appendChild(li);
      return;
    }
    for (const s of sources) {
      const li = document.createElement("li");
      if (s.type === "web" && s.url) {
        const a = document.createElement("a");
        a.href = s.url;
        a.target = "_blank";
        a.rel = "noreferrer";
        a.textContent = s.title || s.url;
        li.appendChild(a);
      } else if (s.type && s.id) {
        const span = document.createElement("span");
        span.className = "muted";
        span.textContent = `${s.type}: ${s.id}`;
        li.appendChild(span);
      } else {
        const span = document.createElement("span");
        span.className = "muted";
        span.textContent = JSON.stringify(s);
        li.appendChild(span);
      }
      el.sources.appendChild(li);
    }
  }

  function renderChart(charts) {
    el.chartHint.style.display = "none";
    if (!Array.isArray(charts) || charts.length === 0) {
      el.chartHint.textContent = "无图表";
      el.chartHint.style.display = "block";
      return;
    }

    const first = charts.find(c => c && c.type === "echarts" && c.option) || charts[0];
    if (!first || !first.option) {
      el.chartHint.textContent = "图表数据为空";
      el.chartHint.style.display = "block";
      return;
    }

    if (!window.echarts) {
      el.chartHint.textContent = "未加载 ECharts：请放置 /vendor/echarts.min.js";
      el.chartHint.style.display = "block";
      return;
    }

    state.chart = window.echarts.init(el.chart);
    state.chart.setOption(first.option, true);
  }

  function debugAppend(obj) {
    const line = typeof obj === "string" ? obj : JSON.stringify(obj, null, 2);
    el.debug.textContent += (el.debug.textContent ? "\n\n" : "") + line;
    el.debug.scrollTop = el.debug.scrollHeight;
  }

  function disableSend(disabled) {
    el.sendBtn.disabled = disabled;
    el.input.disabled = disabled;
  }

  function parseSseEventBlock(block) {
    const lines = block.split(/\r?\n/);
    let event = "message";
    const dataLines = [];
    for (const line of lines) {
      if (line.startsWith("event:")) {
        event = line.slice(6).trim();
      } else if (line.startsWith("data:")) {
        dataLines.push(line.slice(5).trim());
      }
    }
    const dataText = dataLines.join("\n");
    return { event, dataText };
  }

  async function sendMessage() {
    const message = (el.input.value || "").trim();
    if (!message) return;
    el.input.value = "";
    state.lastAssistantEl = null;

    addMessage("user", message);
    clearSide();

    disableSend(true);
    setStatus("Running", "warn");

    const payload = { sessionId: state.sessionId, message, context: { language: "zh-CN" } };

    try {
      if (!state.streaming) {
        const resp = await fetch("/api/chat", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(payload),
        });
        if (!resp.ok) throw new Error(`HTTP ${resp.status}`);
        const json = await resp.json();
        state.sessionId = json.sessionId || state.sessionId;
        addMessage("assistant", json.answer || "");
        renderSources(json.sources);
        renderChart(json.charts);
        debugAppend({ type: "final", data: json });
        setStatus("Done", "ok");
        return;
      }

      state.abortController = new AbortController();
      const resp = await fetch("/api/chat/stream", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Accept": "text/event-stream",
        },
        body: JSON.stringify(payload),
        signal: state.abortController.signal,
      });
      if (!resp.ok) throw new Error(`HTTP ${resp.status}`);

      const reader = resp.body.getReader();
      const decoder = new TextDecoder("utf-8");
      let buffer = "";

      while (true) {
        const { value, done } = await reader.read();
        if (done) break;
        buffer += decoder.decode(value, { stream: true });

        let idx;
        while ((idx = buffer.search(/\r?\n\r?\n/)) !== -1) {
          const block = buffer.slice(0, idx);
          buffer = buffer.slice(buffer.match(/\r?\n\r?\n/)[0].length + idx);
          const { event, dataText } = parseSseEventBlock(block);
          if (!dataText) continue;

          let payloadObj = null;
          try { payloadObj = JSON.parse(dataText); } catch (_) { payloadObj = { type: event, data: dataText }; }

          if (payloadObj.type === "token") {
            appendToAssistant(payloadObj.data);
          } else if (payloadObj.type === "tool_call" || payloadObj.type === "tool_result") {
            addMessage("tool", `${payloadObj.type}: ${JSON.stringify(payloadObj.data)}`);
            debugAppend(payloadObj);
          } else if (payloadObj.type === "final") {
            const finalResp = payloadObj.data;
            state.sessionId = finalResp.sessionId || state.sessionId;
            renderSources(finalResp.sources);
            renderChart(finalResp.charts);
            debugAppend(payloadObj);
            setStatus("Done", "ok");
          } else if (payloadObj.type === "error") {
            debugAppend(payloadObj);
            setStatus("Error", "err");
          } else {
            debugAppend(payloadObj);
          }
        }
      }
    } catch (err) {
      debugAppend({ error: String(err) });
      addMessage("assistant", `发生错误：${String(err)}`);
      setStatus("Error", "err");
    } finally {
      disableSend(false);
      state.abortController = null;
    }
  }

  el.streamToggle.addEventListener("change", () => {
    state.streaming = el.streamToggle.checked;
  });

  el.sendBtn.addEventListener("click", sendMessage);

  el.input.addEventListener("keydown", (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  });

  setStatus("Idle", "ok");
})();

