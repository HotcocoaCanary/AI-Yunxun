# AI-Yunxun 前端界面

基于 Next.js 构建的智能知识图谱系统前端界面。

## 功能特性

### 🤖 智能问答
- 自然语言查询接口
- 实时聊天体验
- 支持Markdown格式回答
- 自动生成图表和知识图谱

### 🕸️ 知识图谱可视化
- 交互式图谱展示
- 支持节点和边的点击交互
- 多种布局算法
- 实时搜索和筛选

### 📊 数据分析
- 多种图表类型（柱状图、饼图、折线图）
- 基于ECharts的可视化
- 实时数据更新
- 响应式设计

### 📁 数据管理
- 支持JSON、CSV、Excel格式上传
- 拖拽式文件上传
- 文件列表管理
- 数据导入导出

## 技术栈

- **框架**: Next.js 16
- **样式**: Tailwind CSS
- **图表**: ECharts + echarts-for-react
- **图谱**: Cytoscape.js
- **图标**: Lucide React
- **文件上传**: react-dropzone
- **Markdown**: react-markdown

## 快速开始

### 安装依赖

```bash
npm install
```

### 开发模式

```bash
npm run dev
```

访问 [http://localhost:3000](http://localhost:3000) 查看应用。

### 构建生产版本

```bash
npm run build
npm start
```

## 项目结构

```
frontend/
├── app/                    # Next.js App Router
│   ├── globals.css        # 全局样式
│   ├── layout.tsx         # 根布局
│   └── page.tsx           # 主页面
├── components/            # React 组件
│   ├── Sidebar.tsx        # 侧边栏导航
│   ├── ChatInterface.tsx  # 聊天界面
│   ├── KnowledgeGraph.tsx # 知识图谱页面
│   ├── DataManagement.tsx # 数据管理页面
│   ├── Analytics.tsx      # 数据分析页面
│   ├── ChartDisplay.tsx   # 图表显示组件
│   └── GraphDisplay.tsx   # 图谱显示组件
├── public/                # 静态资源
└── package.json          # 依赖配置
```

## 组件说明

### ChatInterface
智能问答聊天界面，支持：
- 自然语言查询
- 实时消息显示
- 图表和图谱展示
- Markdown格式支持

### KnowledgeGraph
知识图谱可视化页面，功能包括：
- 交互式图谱展示
- 搜索和筛选
- 数据导入导出
- 统计信息显示

### DataManagement
数据管理页面，提供：
- 文件上传（JSON/CSV/Excel）
- 文件列表管理
- 数据统计
- 批量操作

### Analytics
数据分析页面，包含：
- 多种图表类型
- 实时数据更新
- 筛选和排序
- 详细数据表格

## 样式主题

采用简洁的白色主题配金色点缀：
- 主色调：琥珀色（#F59E0B）
- 背景色：浅灰色（#F9FAFB）
- 文字色：深灰色（#111827）
- 边框色：浅灰色（#E5E7EB）

## API 集成

前端通过 Next.js 的 API 代理与后端通信：
- 后端地址：http://localhost:8080
- 代理配置：`next.config.ts`
- 主要接口：
  - `/api/intelligent/query` - 智能查询
  - `/api/data/upload` - 数据上传
  - `/api/data/download` - 数据下载
  - `/api/mcp/*` - MCP工具接口

## 开发说明

### 添加新组件
1. 在 `components/` 目录下创建组件文件
2. 使用 TypeScript 和 Tailwind CSS
3. 遵循现有的设计模式

### 修改样式
1. 编辑 `app/globals.css` 添加全局样式
2. 使用 Tailwind CSS 类名进行样式设置
3. 保持与设计系统的一致性

### 集成新图表
1. 在 `ChartDisplay.tsx` 中添加新的图表类型
2. 使用 ECharts 配置
3. 确保响应式设计

## 部署

### 构建
```bash
npm run build
```

### 启动
```bash
npm start
```

### 环境变量
创建 `.env.local` 文件：
```
NEXT_PUBLIC_API_URL=http://localhost:8080
```

## 浏览器支持

- Chrome (推荐)
- Firefox
- Safari
- Edge

## 许可证

MIT License
