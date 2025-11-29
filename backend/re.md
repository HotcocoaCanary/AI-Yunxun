首先，这是一个用于图谱分析的智能问答平台，需要使用spring ai框架，加上本地部署的大模型，实现智能问答，这个提示
词要经过设计，因为需要引入mcp服务，从功能上讲，引入mcp服务主要提供两种mcp服务，一种是我们自己做的，这个项目自带的，另一种是用户自己导入
的，这两种我们都需要支持，我们自己做的这个，需要实现以下几个核心的，第一个是neo4j数据库的操作，可以操作本地的neo4j数据库，第二个是数据
输入的处理，包含爬取文本数据，存储到mongo数据库中，提取三元组信息（这里应该用到大模型），将三元组信息保存到neo4j中，第三个是数据的输
出，也是从大模型的输出中，提取需要的信息，返回的是用于绘制图谱，图表的数据，这里图谱是必须的，要求用户的每次问答我们都返回图谱，图表可
以是柱状图，饼图，折线图，可能有的问题中可以提取出来，有的问题场景不需要。然后到这里就可以实现一个比较酷炫的问答，用户提问的时候，我们
可以搜索文档存到文档数据库和图数据库中，然后根据所有相关的历史搜索的文档和图谱做一个RAG，给出的回答不仅有mackdown的回答，还有图谱，还有
可能有统计图表，帮助用户分析。然后第二个需求就是，这是一个平台，所以用户对自己加入的文档和知识应该有权限控制，用户可以设置自己的文档和
加入的数据（包括文档，包括neo4j，包括问题引起的搜索，包括自己上传的）是否是公开的，如果公开，所有登录这个平台的用户都可以使用，如果不公
开，就只有自己的聊天可以使用，mcp工具不需要控制，直接每个用户私有即可，但是用户可以设置启用那些工具，关闭哪些工具，比如用户不希望数据再
增加了，就可以先关掉搜索工具，另外还有一个要求就是mcp的实现使用spring ai框架，java sdk，对于大模型记忆的控制，我希望可以实现树状的类似
文件夹一样的会话管理，每一个会话既是一个会话，内部也可以新建很多子会话，这里关于每个会话的输入输出以及记忆共享范围上还没有太好的想法，
如果你有好的建设性意见可以提出来，这个最后的一个需求优先级并不高，专注前三个。

yunxun.ai.canary.backend

- config
    - ai：Spring AI / 模型 / 向量库配置（现有 AiClientConfig, VectorStoreConfig）。
    - db：Mongo / Neo4j / MySQL / Redis / MyBatis-Plus 等数据源配置。
    - async：线程池、异步任务配置。
    - security：Spring Security + JWT 相关配置与过滤器。
    - mcp：Spring AI MCP server/client 的工具注册、路由配置。
- common
    - constants：全局常量（如默认可见性、角色常量、系统配置 key）。
    - enums：通用枚举（Visibility、DataType、ToolType、MemoryScope 等）。
    - exception：基础业务异常、全局异常处理（比如 BusinessException, GlobalExceptionHandler）。
    - util：工具类（ID 生成、树形结构构建、权限检查帮助方法等）。
- auth
    - api：登录、注册、刷新 token 等接口。
    - service：认证/授权逻辑，调用 JwtService、用户服务等。
    - model：LoginRequest, RegisterRequest, TokenResponse 等 DTO。
- user
    - api：用户信息管理接口（个人资料、偏好设置）。
    - service：用户信息增删改查、密码修改等。
    - model：UserEntity, UserProfileDto。
    - repository：UserMapper / UserRepository。
- access（权限与资源可见性）
    - model：
        - OwnedResource 接口（定义 ownerId, visibility 字段）；
        - 与权限相关的策略对象（如 AccessPolicy）。
    - service：
        - AccessControlService：统一校验用户是否有权限访问某个资源（文档、图节点、搜索结果等）；
        - 对 repository 层提供封装（如 findByIdWithAccessCheck）。
- session（会话树与记忆范围）
    - model：
        - ChatSessionEntity（增加 parentId, memoryScope 等字段）；
        - SessionTreeNodeDto（前端展示用的树节点）。
    - service：
        - SessionTreeService：构建/查询会话树；
        - MemoryScopeService：根据会话和 scope 策略决定从哪些会话收集历史消息/摘要参与 RAG。
- data（文档/数据资源 + 导入）
    - api：
        - 文档上传、URL 导入、资源列表/搜索等接口（整合现有 ResourceController）。
    - service：
        - DataResourceService：统一管理数据资源（Mongo 中的文档/搜索结果），控制 owner/visibility；
        - DataImportService：封装文本/URL 导入流程，触发爬虫、三元组抽取、写图；
        - DataStatsService：数据量/使用情况统计。
    - crawler：
        - WebCrawlerService：网页爬虫实现；
        - 相关 DTO（CrawlTaskRequest, CrawlResult）。
    - triple：
        - TripleExtractionService：调用大模型，从文本中抽取三元组；
        - 抽取结果模型（ExtractedTriple, 内部模型）。
    - model：
        - DataResourceDoc（Mongo 文档）；
        - DataResourceDto, DataResourceQuery, DataImportTextRequest, DataImportUrlRequest。
    - repository：
        - DataResourceRepository（Mongo）。
- graph（图谱域）
    - api：
        - 图谱查询/扩展接口；
        - 也可以作为“图谱专用 API”（独立于问答）。
    - service：
        - GraphService：封装 Neo4j 的读写，基于三元组/实体操作；
        - GraphRagService：面向问答，提供“根据问题和上下文查图”的接口；
        - 提供“问答结果中的图谱构造”能力（从 LLM 输出解析或从 Neo4j 查询）。
    - model：
        - BaseNode, BaseRelationship；
        - GraphNodeDto, GraphEdgeDto, GraphDataDto, GraphExpandRequest, GraphIngestionRequest 等。
    - repository：
        - GraphRepository（Neo4j 操作封装）。
- analytics（统计图表）
    - service：
        - DataAnalysisService：从文档/图谱中生成统计数据；
        - ChartService：把统计数据转换为统一的图表规范（柱状/折线/饼图等），复用现有 ChartSpecDto。
    - model：
        - ChartSpecDto, GraphChartRequest 等。
- qa（智能问答/Agent 编排核心）
    - api：
        - QaController 或沿用当前 AgentController/ChatController（但建议把“用户自然语言问答入口”收拢到一个或少数接口）。
    - service：
        - QuestionOrchestratorService：核心编排服务，负责：
            1. 解析问题和会话上下文；
            2. 查询文档/图谱（RAG）；
            3. 根据用户启用的工具调用 MCP 工具；
            4. 组合 Prompt，调用 LLM；
            5. 从回答中解析出图谱数据和图表数据；
            6. 返回统一的 AnswerPayload。
        - RagService：RAG 抽象，组合文档向量检索 + 图谱检索结果；
        - PromptDesignService：集中管理提示词模板（可复用或包裹现有 LlmPromptTemplates）。
    - model：
        - QuestionRequest（前端发送的问题 + 当前会话/子会话信息）；
        - AnswerPayload（统一返回：markdown, graph, charts）；
        - AgentPlanStep, AgentMessage 等可继续存在于此域。
- chat（对话记录与消息）
    - api：
        - 基础聊天接口可以拆分到 qa 或继续保留独立 ChatController。
    - service：
        - ChatHistoryService：管理历史消息、消息追加等；
        - InMemoryChatService：简单或测试用途实现。
    - model：
        - ChatMessageDoc, ChatMessageDto, ChatMessageAppendRequest 等；
    - repository：
        - ChatMessageMongoRepository, ChatMessageRefMapper, ChatSessionMapper 等。
- mcp（MCP 集成）
    - server：
        - McpClientService：内置 MCP server 端逻辑（被外部 MCP client 调用）；
        - 通过 Spring AI MCP Server WebMVC starter 暴露工具。
    - tool：
        - 内置工具实现：AnalyticsTool, CrawlerTool, GraphTool, LlmTool, RagTool；
        - 每个工具内部再依赖 graph/data/analytics/qa 等 service，自己不直接做复杂业务。
    - client：
        - McpClientAdapter：封装 Spring AI MCP Client，用于调用外部（用户导入的） MCP 服务；
        - 未来可以增加 UserMcpEndpointEntity, UserMcpEndpointRepository，支持用户配置自己的 MCP 服务。
    - registry：
        - PromptRegistry（可以放在这里或 qa），管理所有 Prompt 模板和工具描述。
- setting（用户工具 & 平台设置）
    - api：
        - SettingController, ConfigController, ToolController 等，可以细化到：
            - PlatformConfigController（系统级）；
            - UserToolController（用户工具开关）。
    - service：
        - ConfigService：平台配置项；
        - UserToolService：管理每个用户启用/禁用的 MCP 工具（例如搜索工具 off 时，QuestionOrchestratorService 就不再调用对应工具）。
    - model：
        - ConfigEntryEntity, ConfigEntryDto, ConfigUpsertRequest;
        - UserToolDoc, McpToolStatusDto, McpToolToggleRequest。
    - repository：
        - ConfigEntryMapper, UserToolRepository。
