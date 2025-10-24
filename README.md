# AI-Yunxun 智能知识图谱系统

基于图数据库和自然语言实现的学术论文知识图谱系统，提供智能问答、知识图谱可视化、数据管理等功能。

## 🚀 快速开始

### 环境要求

- Java 17+
- Node.js 18+
- Maven 3.6+
- Neo4j 5.0+
- MongoDB 6.0+
- MySQL 8.0+
- Redis 6.0+

### 启动步骤

1. **启动后端服务**
   ```bash
   cd backend
   mvn spring-boot:run
   ```

2. **启动前端服务**
   ```bash
   cd frontend
   npm run dev
   ```

3. **访问应用**
   - 前端界面: http://localhost:3000
   - 后端API: http://localhost:8080

### 一键启动（Windows）

运行 `start-dev.bat` 脚本可以同时启动前后端服务。

## 🎯 功能特性

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

## 🏗️ 技术架构

### 后端技术栈
- **框架**: Spring Boot + Spring AI
- **数据库**: 
  - Neo4j (知识图谱存储)
  - MongoDB (文献数据存储)
  - MySQL (用户和日志数据)
  - Redis (缓存)
  - Chroma (向量数据库)
- **大模型**: Spring AI (支持Function Calling与RAG框架)
- **MCP工具**: CrawlerTool、GraphQueryTool、RAGRetrieverTool、DBServiceTool

### 前端技术栈
- **框架**: Next.js 14
- **样式**: Tailwind CSS
- **图表**: ECharts + echarts-for-react
- **图谱**: SVG + 自定义组件
- **图标**: Lucide React
- **文件上传**: react-dropzone
- **Markdown**: react-markdown

## 📁 项目结构

```
AI-Yunxun/
├── backend/                 # 后端服务
│   ├── src/main/java/      # Java源码
│   │   └── yunxun/ai/canary/backend/
│   │       ├── controller/ # 控制器层
│   │       ├── service/    # 服务层
│   │       ├── model/      # 数据模型
│   │       ├── repository/ # 数据访问层
│   │       └── config/     # 配置类
│   └── src/main/resources/ # 配置文件
├── frontend/               # 前端应用
│   ├── app/               # Next.js App Router
│   ├── components/        # React组件
│   └── public/           # 静态资源
└── doc/                  # 文档
```

## 🔧 配置说明

### 后端配置

编辑 `backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_yunxun
    username: your_username
    password: your_password
  
  data:
    neo4j:
      uri: bolt://localhost:7687
      username: neo4j
      password: your_password
    
    mongodb:
      uri: mongodb://localhost:27017/ai_yunxun

  redis:
    host: localhost
    port: 6379
    password: your_password

spring:
  ai:
    openai:
      api-key: your_openai_api_key
```

### 前端配置

编辑 `frontend/next.config.ts` 中的API代理地址：

```typescript
async rewrites() {
  return [
    {
      source: '/api/:path*',
      destination: 'http://localhost:8080/api/:path*',
    },
  ];
}
```

## 📖 使用指南

### 1. 智能问答

1. 在聊天界面输入自然语言问题
2. 系统会自动分析意图并返回结果
3. 支持的问题类型：
   - 图谱查询："查找与机器学习相关的所有论文"
   - 数据分析："分析深度学习领域的发展趋势"
   - 文献综述："总结自然语言处理的最新进展"
   - 合作网络："显示人工智能领域的合作网络"

### 2. 知识图谱

1. 在知识图谱页面输入搜索查询
2. 使用筛选器按类型、关系、时间范围筛选
3. 点击节点查看详细信息
4. 支持数据导入导出

### 3. 数据管理

1. 拖拽文件到上传区域或点击选择文件
2. 支持JSON、CSV、Excel格式
3. 查看文件列表和统计信息
4. 支持批量操作

### 4. 数据分析

1. 查看系统统计信息
2. 分析研究趋势
3. 查看热门作者和机构
4. 生成各种图表

## 🛠️ 开发指南

### 添加新功能

1. **后端**: 在相应的service和controller中添加方法
2. **前端**: 在components目录下创建新组件
3. **API**: 更新API文档和接口定义

### 自定义样式

编辑 `frontend/app/globals.css` 添加自定义样式类。

### 添加新图表

在 `ChartDisplay.tsx` 中添加新的图表类型配置。

## 🐛 故障排除

### 常见问题

1. **端口冲突**: 确保8080和3000端口未被占用
2. **数据库连接**: 检查数据库服务是否启动
3. **依赖安装**: 使用 `npm install --legacy-peer-deps`
4. **API调用**: 检查后端服务是否正常运行

### 日志查看

- 后端日志: 控制台输出
- 前端日志: 浏览器开发者工具

## 📄 许可证

MIT License

## 🤝 贡献

欢迎提交Issue和Pull Request来改进项目。

## 📞 联系方式

如有问题，请通过以下方式联系：

- 项目Issues: [GitHub Issues](https://github.com/your-repo/issues)
- 邮箱: your-email@example.com

---

**AI-Yunxun** - 让知识图谱更智能，让数据更易理解！