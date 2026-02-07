# 甜菜论文知识图谱系统

[![GitHub](https://img.shields.io/badge/GitHub-Repository-blue)](https://github.com/HotcocoaCanary/AI-Vunxun)

一个基于 **Neo4j 图数据库**和**自然语言处理（NLP）**
的学术论文知识图谱系统，旨在帮助研究人员高效管理、检索和分析学术资源。系统提供知识图谱可视化展示与智能问答功能，支持从非结构化论文数据中提取结构化信息，构建语义关联网络。

---

## ✨ 核心功能

- **知识图谱可视化**：通过力导向图展示论文、作者、机构等实体及其复杂关系。
- **智能问答**：结合 NLP 技术解析用户输入，基于知识图谱提供精准答案。
- **数据管理**：支持 Excel 和文本文件的数据导入/导出，批量存储至 Neo4j 数据库。
- **语义分析**：利用 OpenNLP 提取人名、机构、时间等关键实体，生成结构化知识。

---

## 🛠 技术栈

| 模块      | 技术组件                                        |
|---------|---------------------------------------------|
| **后端**  | Spring Boot、Neo4j、Apache OpenNLP、Apache POI |
| **前端**  | Vue 3、Element Plus、ECharts、Axios            |
| **数据库** | Neo4j 图数据库                                  |
| **工具**  | Maven、npm、Vite                              |

---

## 🚀 快速部署

### 环境准备

- **操作系统**：Windows（推荐）/ Linux / macOS
- **JDK 17**：运行 Spring Boot 后端
- **Node.js 16+**：运行 Vue 前端
- **Neo4j 5+**：存储知识图谱数据

### 安装步骤

1. **克隆仓库**：
    ```bash
    git clone https://github.com/HotcocoaCanary/AI-Vunxun.git
    ```
2. **启动 Neo4j**：
    ```bash
    # 进入 Neo4j 安装目录并启动服务
    neo4j console
    ```    
   访问 http://localhost:7474，初始用户名/密码为 neo4j/neo4j，首次登录需修改密码。

3. **启动后端**：
   使用 IDE 导入 backend 模块，运行 CloudHuntChartBackendApplication.java。
4. **启动前端**：
    ```bash
    cd front-end
    npm install    # 安装依赖
    npm run dev    # 启动开发服务器
    ```

访问 http://localhost:5173 使用系统。

### 📂 项目结构

```plaintext
AI-Vunxun/
├── backend/           # Spring Boot 后端
│   ├── src/main/java/com/example/...
│   └── pom.xml
├── front-end/         # Vue 3 前端
│   ├── src/views/...
│   └── package.json
└── docs/              # 部署文档与示例
```

### 📜 许可证

本项目采用 MIT License。

### 🤝 贡献与支持

欢迎提交 Issue 或 Pull Request！如有问题，请联系维护者或通过 GitHub 讨论区提问。
让学术知识触手可及，探索科研新边界！ 🌱