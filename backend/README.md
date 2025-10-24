# 学术知识图谱系统

基于Spring Boot + Spring AI的学术知识图谱构建与查询系统，支持从学术网站爬取论文数据，使用LLM进行实体抽取与关系抽取，构建知识图谱并提供智能查询服务。

## 项目特性

- 🔍 **多源数据爬取**: 支持arXiv、知网等学术网站的数据爬取
- 🤖 **AI驱动**: 使用Spring AI进行实体抽取、关系抽取和自然语言查询
- 🗄️ **多数据库支持**: Neo4j(知识图谱) + MongoDB(论文数据) + MySQL(用户日志) + Redis(缓存) + Chroma(向量数据库)
- 🔧 **MCP工具化**: 提供标准化的工具接口，便于扩展和实验
- 📊 **知识图谱可视化**: 支持Cytoscape.js图谱展示
- 🔐 **用户系统**: 完整的用户认证和权限管理

## 项目状态

✅ **已完成**:
- 项目基础架构搭建
- 数据库配置和实体模型
- MCP工具服务实现
- 用户认证系统
- 数据爬取模块
- 基础API接口

⚠️ **待完善**:
- Spring AI集成（需要配置OpenAI API Key）
- 向量数据库集成（Chroma）
- 完整的LLM功能实现
- 前端界面开发

## 技术栈

### 后端
- **框架**: Spring Boot 3.5.7 + Spring AI 1.0.3
- **数据库**: 
  - Neo4j (知识图谱存储)
  - MongoDB (论文数据存储)
  - MySQL (用户和日志数据)
  - Redis (缓存)
  - Chroma (向量数据库)
- **AI能力**: OpenAI GPT-4o-mini
- **爬虫**: Jsoup + Selenium

### 前端
- Next.js + Cytoscape.js (图谱可视化)

## 项目结构

```
src/main/java/yunxun/ai/canary/backend/
├── config/                 # 配置类
│   ├── DatabaseConfig.java
│   └── SecurityConfig.java
├── controller/             # 控制器
│   ├── AuthController.java
│   ├── QueryController.java
│   └── MCPController.java
├── model/                 # 数据模型
│   ├── User.java
│   ├── Paper.java
│   ├── Entity.java
│   ├── Relationship.java
│   └── QueryLog.java
├── repository/            # 数据访问层
│   ├── UserRepository.java
│   ├── QueryLogRepository.java
│   ├── mongodb/
│   │   └── PaperRepository.java
│   └── neo4j/
│       └── EntityRepository.java
├── service/               # 业务服务
│   ├── UserService.java
│   ├── DataProcessingService.java
│   ├── ScheduledTaskService.java
│   ├── crawler/           # 爬虫服务
│   │   ├── ArxivCrawlerService.java
│   │   └── CnkiCrawlerService.java
│   ├── llm/               # LLM服务
│   │   ├── CypherGenerationService.java
│   │   ├── EntityExtractionService.java
│   │   └── RelationExtractionService.java
│   ├── rag/               # RAG服务
│   │   └── RAGService.java
│   └── mcp/               # MCP工具
│       ├── CrawlerTool.java
│       ├── GraphQueryTool.java
│       ├── RAGRetrieverTool.java
│       └── DBServiceTool.java
└── BackendApplication.java
```

## 核心模块

### 1. MCP工具服务
- **CrawlerTool**: 负责爬取学术数据并存储到MongoDB
- **GraphQueryTool**: 根据自然语言生成Cypher查询并执行
- **RAGRetrieverTool**: 使用向量数据库检索相关论文摘要
- **DBServiceTool**: 管理MySQL日志、用户信息和Redis缓存

### 2. 数据处理流程
1. **数据爬取**: 从arXiv、知网等网站爬取论文数据
2. **实体抽取**: 使用LLM从论文摘要中抽取实体
3. **关系抽取**: 识别实体间的关系
4. **图谱构建**: 将实体和关系存储到Neo4j
5. **向量化**: 将论文摘要向量化存储到Chroma

### 3. 查询服务
- **自然语言查询**: 将用户问题转换为Cypher查询
- **RAG检索**: 基于向量相似度的文档检索
- **增强回答**: 结合图谱和RAG结果生成回答

## 快速开始

### 环境要求
- Java 17+
- Maven 3.6+
- Neo4j 5.0+
- MongoDB 6.0+
- MySQL 8.0+
- Redis 6.0+
- Chroma 0.4+

### 配置说明

1. **数据库配置** (application.yml):
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/academic_kg
    username: root
    password: 123456
  data:
    mongodb:
      uri: mongodb://localhost:27017/academic_kg
    redis:
      host: localhost
      port: 6379
  neo4j:
    uri: bolt://localhost:7687
    authentication:
      username: neo4j
      password: 123456
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
```

2. **启动服务**:
```bash
mvn spring-boot:run
```

### API接口

#### 用户认证
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录

#### 查询服务
- `POST /api/query/natural` - 自然语言查询
- `GET /api/query/history` - 查询历史

#### MCP工具
- `GET /api/mcp/tools` - 获取可用工具
- `POST /api/mcp/crawler` - 执行爬虫工具
- `POST /api/mcp/graph-query` - 执行图谱查询
- `POST /api/mcp/rag-retriever` - 执行RAG检索
- `POST /api/mcp/db-service` - 执行数据库服务

## 使用示例

### 1. 爬取论文数据
```bash
curl -X POST http://localhost:8080/api/mcp/crawler \
  -H "Content-Type: application/json" \
  -d '{
    "source": "arxiv",
    "query": "machine learning",
    "max_papers": 100
  }'
```

### 2. 自然语言查询
```bash
curl -X POST http://localhost:8080/api/query/natural \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "query": "谁是深度学习领域的知名研究者？",
    "type": "entity"
  }'
```

### 3. 图谱查询
```bash
curl -X POST http://localhost:8080/api/mcp/graph-query \
  -H "Content-Type: application/json" \
  -d '{
    "query": "找到与机器学习相关的所有论文",
    "query_type": "general"
  }'
```

## 部署说明

### Docker部署
```bash
# 构建镜像
docker build -t academic-kg-backend .

# 运行容器
docker run -p 8080:8080 academic-kg-backend
```

### 生产环境配置
1. 配置环境变量
2. 设置数据库连接
3. 配置Redis缓存
4. 设置Neo4j集群
5. 配置Chroma向量数据库

## 扩展开发

### 添加新的数据源
1. 实现新的CrawlerService
2. 在CrawlerTool中添加支持
3. 配置相应的解析逻辑

### 添加新的LLM能力
1. 创建新的LLM服务类
2. 实现相应的工具接口
3. 配置Spring AI集成

### 自定义MCP工具
1. 实现MCPTool接口
2. 定义参数模式
3. 注册到MCPController

## 贡献指南

1. Fork项目
2. 创建特性分支
3. 提交更改
4. 推送到分支
5. 创建Pull Request

## 许可证

MIT License

## 联系方式

如有问题或建议，请提交Issue或联系开发团队。
