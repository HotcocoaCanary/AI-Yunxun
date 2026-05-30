import type { Message } from "@/types/chat";

export interface ChatSession {
  id: string;
  title: string;
  createdAt: string;
  messages: Message[];
}

export const mockSessions: ChatSession[] = [
  {
    id: "session-1",
    title: "Python 数据分析入门",
    createdAt: "2026-05-27 14:30",
    messages: [
      {
        role: "user",
        content: "我想学习 Python 数据分析，应该从哪里开始？",
      },
      {
        role: "assistant",
        content: "学习 Python 数据分析是一个很好的选择！我建议你按以下路径学习：\n\n1. **Python 基础语法** - 变量、循环、函数、列表推导式等\n2. **NumPy** - 数值计算基础，数组操作\n3. **Pandas** - 数据处理核心库，掌握 DataFrame 操作\n4. **Matplotlib / Seaborn** - 数据可视化\n5. **Scikit-learn** - 机器学习入门\n\n建议从实际项目出发，边做边学。需要我帮你规划一个具体的学习项目吗？",
        status: "完成",
      },
    ],
  },
  {
    id: "session-2",
    title: "Docker 容器部署问题",
    createdAt: "2026-05-26 09:15",
    messages: [
      {
        role: "user",
        content: "我的 Docker 容器启动后马上就退出了，怎么排查？",
      },
      {
        role: "assistant",
        content: "Docker 容器立即退出通常是因为主进程没有在前台运行。常见排查步骤：\n\n1. **查看容器日志**\n```bash\ndocker logs <container_id>\n```\n\n2. **检查启动命令** - 确保命令不会立即返回\n```bash\ndocker run -it <image> /bin/bash\n```\n\n3. **常见原因**\n- CMD 或 ENTRYPOINT 执行完毕后进程退出\n- 程序异常崩溃但没有捕获错误\n- 端口冲突\n\n可以把你的 Dockerfile 和启动命令发给我，我帮你分析具体原因。",
        status: "完成",
      },
      {
        role: "user",
        content: "这是我的 Dockerfile：\nFROM node:18\nWORKDIR /app\nCOPY . .\nRUN npm install\nCMD node index.js",
      },
      {
        role: "assistant",
        content: "你的 Dockerfile 看起来没问题，问题很可能出在 `index.js` 中。让我帮你排查：\n\n1. **检查 index.js 是否真的是一个长驻进程** - 如果它只是一个脚本，执行完就会退出\n2. **检查是否有未捕获的异常**\n```javascript\nprocess.on('uncaughtException', (err) => {\n  console.error('未捕获异常:', err);\n});\n```\n\n3. **确保服务监听了端口**\n```javascript\napp.listen(3000, () => {\n  console.log('服务启动在 3000 端口');\n});\n```\n\n如果上面都正确，试试用 `docker run -it` 交互模式查看实时输出。",
        status: "完成",
      },
    ],
  },
  {
    id: "session-3",
    title: "设计电商数据库表结构",
    createdAt: "2026-05-25 16:45",
    messages: [
      {
        role: "user",
        content: "帮我设计一个电商系统的 MySQL 数据库表结构",
      },
      {
        role: "assistant",
        content: "我来帮你设计一个电商系统核心表结构，包含用户、商品、订单、购物车等模块：\n\n**1. 用户表 (users)**\n```sql\nCREATE TABLE users (\n  id BIGINT PRIMARY KEY AUTO_INCREMENT,\n  username VARCHAR(50) NOT NULL UNIQUE,\n  email VARCHAR(100) NOT NULL UNIQUE,\n  password_hash VARCHAR(255) NOT NULL,\n  phone VARCHAR(20),\n  avatar_url VARCHAR(500),\n  status TINYINT DEFAULT 1,\n  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP\n);\n```\n\n**2. 商品表 (products)**\n```sql\nCREATE TABLE products (\n  id BIGINT PRIMARY KEY AUTO_INCREMENT,\n  name VARCHAR(200) NOT NULL,\n  description TEXT,\n  price DECIMAL(10,2) NOT NULL,\n  stock INT DEFAULT 0,\n  category_id BIGINT,\n  image_urls JSON,\n  status TINYINT DEFAULT 1,\n  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP\n);\n```\n\n需要我继续补充订单表、购物车表和相关索引吗？",
        status: "完成",
      },
      {
        role: "user",
        content: "继续补充订单相关的表结构",
      },
      {
        role: "assistant",
        content: "好的，补充订单相关表结构：\n\n**3. 订单表 (orders)**\n```sql\nCREATE TABLE orders (\n  id BIGINT PRIMARY KEY AUTO_INCREMENT,\n  order_no VARCHAR(32) NOT NULL UNIQUE,\n  user_id BIGINT NOT NULL,\n  total_amount DECIMAL(12,2) NOT NULL,\n  discount_amount DECIMAL(12,2) DEFAULT 0,\n  pay_amount DECIMAL(12,2) NOT NULL,\n  status ENUM('pending','paid','shipped','delivered','cancelled') DEFAULT 'pending',\n  pay_method VARCHAR(20),\n  receiver_name VARCHAR(50),\n  receiver_phone VARCHAR(20),\n  receiver_address TEXT,\n  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n  paid_at TIMESTAMP NULL,\n  shipped_at TIMESTAMP NULL,\n  FOREIGN KEY (user_id) REFERENCES users(id)\n);\n```\n\n**4. 订单明细表 (order_items)**\n```sql\nCREATE TABLE order_items (\n  id BIGINT PRIMARY KEY AUTO_INCREMENT,\n  order_id BIGINT NOT NULL,\n  product_id BIGINT NOT NULL,\n  product_name VARCHAR(200),\n  product_price DECIMAL(10,2),\n  quantity INT NOT NULL DEFAULT 1,\n  FOREIGN KEY (order_id) REFERENCES orders(id),\n  FOREIGN KEY (product_id) REFERENCES products(id)\n);\n```\n\n建议对 `user_id`、`order_no`、`status` 加索引以提升查询性能。",
        status: "完成",
      },
    ],
  },
  {
    id: "session-4",
    title: "React 状态管理选型",
    createdAt: "2026-05-24 11:20",
    messages: [
      {
        role: "user",
        content: "React 项目中用 Context API 还是 Zustand，能帮我对比一下吗？",
      },
      {
        role: "assistant",
        content: "这是一个很好的问题，两个方案各有优劣：\n\n**Context API 优势：**\n- React 内置，零依赖\n- 适合简单到中等复杂度的状态\n- 与 React DevTools 集成良好\n\n**Context API 劣势：**\n- 多个 Context 嵌套会导致 Provider Hell\n- 性能问题：任何值变化都会导致所有消费者重渲染\n- 不提供中间件、持久化等高级功能\n\n**Zustand 优势：**\n- 极简 API，学习成本低\n- 自动优化重渲染（按需订阅）\n- 支持中间件（持久化、immer 集成等）\n- 可在组件外部读写状态\n\n**Zustand 劣势：**\n- 额外依赖（但仅 1KB）\n- 需要理解其订阅模型\n\n**建议：** 小型项目用 Context API 足够，中大型项目推荐 Zustand 或 Jotai。",
        status: "完成",
      },
    ],
  },
  {
    id: "session-5",
    title: "Git 合并冲突处理技巧",
    createdAt: "2026-05-23 15:00",
    messages: [
      {
        role: "user",
        content: "合并分支时出现了很多冲突，有什么高效的处理技巧吗？",
      },
      {
        role: "assistant",
        content: "处理 Git 合并冲突的几个高效技巧：\n\n**1. 使用图形化工具**\n```bash\ngit mergetool\n```\n配置使用 VS Code：\n```bash\ngit config --global merge.tool vscode\ngit config --global mergetool.vscode.cmd 'code --wait $MERGED'\n```\n\n**2. 选择性地合并文件**\n```bash\ngit checkout --ours <file>    # 保留当前分支\ngit checkout --theirs <file>  # 接受合并分支\ngit checkout -p <branch> -- <file>  # 交互式选择\n```\n\n**3. 频繁小步合并**\n- 不要等到分支差异巨大再合并\n- 定期从主分支 rebase：`git rebase main`\n\n**4. 策略性合并**\n- 使用 `git merge --no-ff` 保留分支历史\n- 大型重构建议分阶段合并\n\n需要我详细解释某个技巧吗？",
        status: "完成",
      },
    ],
  },
  {
    id: "session-6",
    title: "MySQL 慢查询优化方案",
    createdAt: "2026-05-22 10:30",
    messages: [
      {
        role: "user",
        content: "数据库查询越来越慢，帮我分析一下优化思路",
      },
      {
        role: "assistant",
        content: "MySQL 慢查询优化可以从以下几个维度入手：\n\n**1. 开启慢查询日志定位问题**\n```sql\nSET GLOBAL slow_query_log = ON;\nSET GLOBAL long_query_time = 1;  -- 超过1秒记录\n```\n\n**2. 使用 EXPLAIN 分析执行计划**\n```sql\nEXPLAIN SELECT * FROM orders WHERE user_id = 123;\n```\n关注：type（ALL 全表扫描最差）、rows、Extra\n\n**3. 索引优化**\n- 为 WHERE、JOIN、ORDER BY 列建索引\n- 使用覆盖索引避免回表\n- 联合索引注意最左前缀原则\n\n**4. SQL 写法优化**\n- 避免 SELECT *\n- 用 LIMIT 分页\n- 避免在 WHERE 中对字段做函数运算\n\n**5. 表结构优化**\n- 大字段垂直拆分\n- 历史数据归档\n\n需要我针对具体查询给出优化建议吗？",
        status: "完成",
      },
    ],
  },
  {
    id: "session-7",
    title: "微服务调用链图谱分析",
    createdAt: "2026-05-21 14:20",
    messages: [
      {
        role: "user",
        content: "帮我分析一下微服务架构中的服务调用关系，绘制服务依赖图谱",
      },
      {
        role: "assistant",
        content: "好的，我来帮你分析微服务调用链并绘制服务依赖关系图。\n\n正在查询 Neo4j 数据库中的服务调用关系…",
        status: "完成",
        tools: [
          {
            callId: "call-neo4j-001",
            name: "echart_graph_generate",
            args: {
              title: "微服务调用链拓扑图",
              layout: "force",
            },
            status: "done",
            result: JSON.stringify({
              title: { text: "微服务调用链拓扑图", left: "center" },
              tooltip: { trigger: "item", formatter: "{b}", confine: true },
              series: [
                {
                  type: "graph",
                  layout: "force",
                  draggable: true,
                  roam: true,
                  symbolSize: 42,
                  force: { repulsion: 280, gravity: 0.08, edgeLength: 120 },
                  label: { show: true, position: "bottom", fontSize: 11, color: "#333", formatter: "{b}", distance: 8 },
                  lineStyle: { color: "source", curveness: 0.15, opacity: 0.5, width: 1.5 },
                  edgeLabel: { show: true, fontSize: 10, formatter: "{c}" },
                  emphasis: { focus: "adjacency" },
                  categories: [
                    { name: "网关层", symbol: "diamond", itemStyle: { color: "#4f46e5" }, symbolSize: 32, label: { fontSize: 11, color: "#333" } },
                    { name: "核心服务", symbol: "circle", itemStyle: { color: "#10b981" }, symbolSize: 32, label: { fontSize: 11, color: "#333" } },
                    { name: "数据层", symbol: "rect", itemStyle: { color: "#f59e0b" }, symbolSize: 32, label: { fontSize: 11, color: "#333" } },
                    { name: "中间件", symbol: "triangle", itemStyle: { color: "#8b5cf6" }, symbolSize: 32, label: { fontSize: 11, color: "#333" } },
                  ],
                  data: [
                    { name: "API Gateway", category: 0, value: "类型: 网关\n端口: 8080\n状态: 运行中" },
                    { name: "用户服务", category: 1, value: "类型: 核心服务\n端口: 8081\n实例数: 3" },
                    { name: "订单服务", category: 1, value: "类型: 核心服务\n端口: 8082\n实例数: 5" },
                    { name: "商品服务", category: 1, value: "类型: 核心服务\n端口: 8083\n实例数: 4" },
                    { name: "支付服务", category: 1, value: "类型: 核心服务\n端口: 8084\n实例数: 3" },
                    { name: "通知服务", category: 1, value: "类型: 核心服务\n端口: 8085\n实例数: 2" },
                    { name: "MySQL 主库", category: 2, value: "类型: 数据库\n版本: 8.0\n连接数: 120" },
                    { name: "Redis 缓存", category: 3, value: "类型: 缓存\n版本: 7.0\n命中率: 96.2%" },
                    { name: "RabbitMQ", category: 3, value: "类型: 消息队列\n版本: 3.12\n队列积压: 0" },
                    { name: "Elasticsearch", category: 2, value: "类型: 搜索引擎\n版本: 8.11\n索引数: 15" },
                  ],
                  links: [
                    { source: "API Gateway", target: "用户服务", value: "HTTP/REST\n调用频率: 高" },
                    { source: "API Gateway", target: "订单服务", value: "HTTP/REST\n调用频率: 高" },
                    { source: "API Gateway", target: "商品服务", value: "HTTP/REST\n调用频率: 中" },
                    { source: "订单服务", target: "用户服务", value: "gRPC\n调用频率: 高" },
                    { source: "订单服务", target: "支付服务", value: "gRPC\n同步调用" },
                    { source: "订单服务", target: "商品服务", value: "gRPC\n库存校验" },
                    { source: "支付服务", target: "通知服务", value: "RabbitMQ\n异步通知" },
                    { source: "用户服务", target: "MySQL 主库", value: "JDBC\n读写操作" },
                    { source: "订单服务", target: "MySQL 主库", value: "JDBC\n读写操作" },
                    { source: "商品服务", target: "Redis 缓存", value: "缓存查询\n商品详情" },
                    { source: "商品服务", target: "Elasticsearch", value: "搜索查询\n全文检索" },
                    { source: "订单服务", target: "RabbitMQ", value: "消息生产\n订单事件" },
                    { source: "通知服务", target: "RabbitMQ", value: "消息消费\n事件处理" },
                  ],
                },
              ],
            }),
          },
        ],
      },
      {
        role: "user",
        content: "订单服务的依赖比较多，帮我重点标注一下它的上下游关系",
      },
      {
        role: "assistant",
        content: "从图谱中可以看到，订单服务是整个系统中依赖最复杂的核心节点：\n\n**上游依赖：**\n- API Gateway → 订单服务（高频入口流量）\n\n**内部调用：**\n- 订单服务 → 用户服务（用户信息校验）\n- 订单服务 → 支付服务（订单支付流程）\n- 订单服务 → 商品服务（库存扣减）\n\n**下游输出：**\n- 订单服务 → RabbitMQ（订单事件消息）\n- 订单服务 → MySQL 主库（数据持久化）\n\n建议对订单服务增加熔断降级机制（如 Sentinel），防止雪崩效应。",
        status: "完成",
      },
    ],
  },
  {
    id: "session-8",
    title: "Q4 季度销售数据分析",
    createdAt: "2026-05-20 10:45",
    messages: [
      {
        role: "user",
        content: "帮我分析一下 Q4 各产品线的销售趋势，用图表展示",
      },
      {
        role: "assistant",
        content: "我来为你生成 Q4 季度销售趋势分析图表。\n\n正在拉取销售数据并生成可视化…",
        status: "完成",
        tools: [
          {
            callId: "call-echart-sales-001",
            name: "echart_line_generate",
            args: {
              title: "Q4 各产品线月度销售趋势",
              categories: ["10月", "11月", "12月"],
            },
            status: "done",
            result: JSON.stringify({
              title: { text: "Q4 各产品线月度销售趋势（万元）", left: "center" },
              tooltip: { trigger: "axis" },
              legend: { bottom: 0, data: ["云计算", "数据服务", "安全产品", "AI平台"] },
              grid: { left: "3%", right: "4%", bottom: "12%", containLabel: true },
              xAxis: { type: "category", data: ["10月", "11月", "12月"], boundaryGap: false },
              yAxis: { type: "value", name: "销售额（万元）" },
              series: [
                { name: "云计算", type: "line", data: [320, 385, 420], smooth: true, itemStyle: { color: "#4f46e5" }, areaStyle: { opacity: 0.1 } },
                { name: "数据服务", type: "line", data: [180, 210, 265], smooth: true, itemStyle: { color: "#10b981" }, areaStyle: { opacity: 0.1 } },
                { name: "安全产品", type: "line", data: [140, 155, 175], smooth: true, itemStyle: { color: "#f59e0b" }, areaStyle: { opacity: 0.1 } },
                { name: "AI平台", type: "line", data: [80, 130, 210], smooth: true, itemStyle: { color: "#8b5cf6" }, areaStyle: { opacity: 0.1 } },
              ],
            }),
          },
        ],
      },
      {
        role: "user",
        content: "各产品线的营收占比呢？",
      },
      {
        role: "assistant",
        content: "来看 Q4 各产品线的营收占比分布：",
        status: "完成",
        tools: [
          {
            callId: "call-echart-pie-002",
            name: "echart_pie_generate",
            args: {
              title: "Q4 产品线营收占比",
            },
            status: "done",
            result: JSON.stringify({
              title: { text: "Q4 各产品线营收占比", left: "center" },
              tooltip: { trigger: "item", formatter: "{b}: {c} 万元 ({d}%)" },
              legend: { bottom: 0 },
              series: [
                {
                  type: "pie",
                  radius: ["45%", "75%"],
                  center: ["50%", "48%"],
                  itemStyle: { borderRadius: 6, borderColor: "#fff", borderWidth: 3 },
                  label: { formatter: "{b}\n{d}%", fontSize: 12 },
                  emphasis: { label: { fontSize: 16, fontWeight: "bold" } },
                  data: [
                    { value: 1125, name: "云计算", itemStyle: { color: "#4f46e5" } },
                    { value: 655, name: "数据服务", itemStyle: { color: "#10b981" } },
                    { value: 470, name: "安全产品", itemStyle: { color: "#f59e0b" } },
                    { value: 420, name: "AI平台", itemStyle: { color: "#8b5cf6" } },
                  ],
                },
              ],
            }),
          },
        ],
      },
      {
        role: "assistant",
        content: "**Q4 分析总结：**\n\n1. **云计算** 仍为营收主力，占比 42.1%，持续增长势头\n2. **AI 平台** 增长最强劲，12月环比增长 61.5%，是明年的重点增长方向\n3. **数据服务** 稳步提升，同比增长 47%\n4. **安全产品** 增长平稳，需关注新产品线拓展\n\n建议：Q1 加大对 AI 平台的销售资源投入，把握增长窗口期。",
        status: "完成",
      },
    ],
  },
  {
    id: "session-9",
    title: "运维告警统计看板",
    createdAt: "2026-05-19 08:30",
    messages: [
      {
        role: "user",
        content: "查看本周各系统的告警分布情况",
      },
      {
        role: "assistant",
        content: "正在查询运维监控数据，生成告警统计图表…",
        status: "完成",
        tools: [
          {
            callId: "call-echart-bar-003",
            name: "echart_bar_generate",
            args: {
              title: "本周各系统告警统计",
            },
            status: "done",
            result: JSON.stringify({
              title: { text: "本周各系统告警统计", left: "center" },
              tooltip: { trigger: "axis", axisPointer: { type: "shadow" } },
              legend: { bottom: 0, data: ["严重告警", "警告", "信息"] },
              grid: { left: "3%", right: "4%", bottom: "12%", containLabel: true },
              xAxis: { type: "category", data: ["订单系统", "支付网关", "用户中心", "商品库存", "消息队列", "缓存服务"] },
              yAxis: { type: "value", name: "告警数量" },
              series: [
                { name: "严重告警", type: "bar", stack: "total", data: [3, 1, 0, 2, 0, 1], itemStyle: { color: "#ef4444", borderRadius: [0, 0, 0, 0] }, label: { show: true, position: "inside", fontSize: 11 } },
                { name: "警告", type: "bar", stack: "total", data: [8, 5, 3, 6, 2, 4], itemStyle: { color: "#f59e0b" }, label: { show: true, position: "inside", fontSize: 11 } },
                { name: "信息", type: "bar", stack: "total", data: [15, 12, 8, 10, 6, 9], itemStyle: { color: "#3b82f6", borderRadius: [4, 4, 0, 0] }, label: { show: true, position: "inside", fontSize: 11 } },
              ],
            }),
          },
        ],
      },
      {
        role: "user",
        content: "订单系统告警有点多，看一下它的告警类型分布",
      },
      {
        role: "assistant",
        content: "这是订单系统本周的告警类型分布：",
        status: "完成",
        tools: [
          {
            callId: "call-echart-pie-004",
            name: "echart_pie_generate",
            args: {
              title: "订单系统告警类型分布",
            },
            status: "done",
            result: JSON.stringify({
              title: { text: "订单系统 - 告警类型分布", left: "center" },
              tooltip: { trigger: "item", formatter: "{b}: {c} 次 ({d}%)" },
              legend: { bottom: 0, orient: "horizontal" },
              series: [
                {
                  type: "pie",
                  radius: "65%",
                  center: ["50%", "48%"],
                  roseType: "area",
                  itemStyle: { borderRadius: 4, borderColor: "#fff", borderWidth: 2 },
                  label: { formatter: "{b}\n{c} 次", fontSize: 11 },
                  emphasis: { label: { fontSize: 14, fontWeight: "bold" } },
                  data: [
                    { value: 12, name: "超时告警", itemStyle: { color: "#ef4444" } },
                    { value: 8, name: "流量异常", itemStyle: { color: "#f97316" } },
                    { value: 4, name: "数据库连接", itemStyle: { color: "#f59e0b" } },
                    { value: 2, name: "内存使用", itemStyle: { color: "#3b82f6" } },
                  ],
                },
              ],
            }),
          },
        ],
      },
      {
        role: "assistant",
        content: "从告警分布来看，订单系统主要问题是**超时告警**（占 46%），建议：\n\n1. 排查下游服务响应时间，重点检查支付服务的 P99 延迟\n2. 检查数据库慢查询，优化未命中索引的 SQL\n3. 考虑对高频接口增加缓存层，减少 RT\n\n需要我帮你调出支付服务的详细监控数据吗？",
        status: "完成",
      },
    ],
  },
];

export function getDefaultSession(): ChatSession {
  return mockSessions[0];
}
