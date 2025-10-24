@echo off
echo 启动学术知识图谱系统...
echo.

echo 请确保以下服务已启动：
echo - MySQL (端口 3306)
echo - MongoDB (端口 27017) 
echo - Neo4j (端口 7687)
echo - Redis (端口 6379)
echo.

echo 启动Spring Boot应用...
mvn spring-boot:run

pause
