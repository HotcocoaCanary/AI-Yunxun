### 架构设计
```shell
C:.
├─app                    # 应用入口与编排层：将 HTTP/SSE 请求组织成一次完整的智能问答流程
├─common                 # 通用工具层：无业务语义的公共工具、常量与辅助类
├─config                 # 全局配置层：Spring Boot、WebFlux 及外部依赖的配置与装配
├─repository             # 数据访问层：封装所有存储系统的访问细节
│  ├─mongo               # MongoDB 数据访问实现
│  └─neo4j               # Neo4j 图数据库访问实现
└─service                # 核心能力层：承载具体业务能力与 MCP 相关逻辑
    ├─chart              # 图表能力服务：将数据转换为 ECharts 可用的 option
    ├─llm                # 大模型服务：负责 LLM 调用、流式输出及 Tool Call 解析
    └─mcp                # MCP 能力层：实现 MCP 客户端与服务端相关逻辑
        ├─client         # MCP 客户端：用于调用或测试 MCP Server 的客户端实现
        │  ├─api         # MCP 客户端对外接口定义
        │  └─config      # MCP 客户端配置
        └─server         # MCP 服务端：负责工具注册、协议管理与调度
            ├─config     # MCP Server 配置与初始化逻辑
            ├─prompt     # Prompt 管理：系统提示词与工具说明模板
            └─tool       # 工具协议层：Tool 接口、描述规范及注册/分发机制（不含实现）
```