package com.example.aiyunxun.db;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Neo4jConnector {

    // 静态变量存储配置参数
    private static String uri;
    private static String user;
    private static String password;

    // 使用静态volatile保证多线程可见性
    private static volatile Driver driver;

    // 通过Setter注入配置到静态变量
    @Value("${spring.neo4j.uri}")
    public void setUri(String uri) {
        Neo4jConnector.uri = uri;
    }

    @Value("${spring.neo4j.username}")
    public void setUser(String user) {
        Neo4jConnector.user = user;
    }

    @Value("${spring.neo4j.password}")
    public void setPassword(String password) {
        Neo4jConnector.password = password;
    }

    // 双重检查锁定确保线程安全
    public static Driver getDriver() {
        if (driver == null) {
            synchronized (Neo4jConnector.class) {
                if (driver == null) {
                    if (uri == null || user == null || password == null) {
                        throw new IllegalStateException("Neo4j配置未正确初始化");
                    }
                    driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
                }
            }
        }
        return driver;
    }

    public static void close() {
        if (driver != null) {
            driver.close();
            driver = null; // 确保关闭后可以重新初始化
        }
    }
}