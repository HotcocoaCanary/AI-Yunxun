# 多阶段构建：构建阶段
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# 复制 pom.xml 并下载依赖（利用 Docker 缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码并构建
COPY src ./src
RUN mvn clean package -DskipTests

# 运行阶段
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 安装 Node.js 和 npm（用于运行 mcp-searxng）
RUN apk add --no-cache \
    curl \
    nodejs \
    npm

# 全局安装 mcp-searxng（无需 API Key 的网络搜索 MCP 服务器）
RUN npm install -g mcp-searxng

# 创建非 root 用户
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# 从构建阶段复制 jar 文件
COPY --from=build /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]

