# RBAC权限管理系统 部署指南

## 1. 环境要求

### 1.1 基础环境

- **Java**: JDK 17 或更高版本
- **数据库**: MySQL 8.0+ / PostgreSQL 12+ / Oracle 12c+
- **缓存**: Redis 6.0+ (可选，用于生产环境)
- **应用服务器**: Tomcat 9.0+ / Jetty 9.4+ (内嵌)
- **操作系统**: Linux / Windows / macOS

### 1.2 开发环境

- **IDE**: IntelliJ IDEA / Eclipse / VS Code
- **构建工具**: Maven 3.6+ / Gradle 7.0+
- **版本控制**: Git 2.20+

## 2. 数据库配置

### 2.1 MySQL 配置

```sql
-- 创建数据库
CREATE DATABASE rbac_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户
CREATE USER 'rbac_user'@'%' IDENTIFIED BY 'rbac_password';
GRANT ALL PRIVILEGES ON rbac_system.* TO 'rbac_user'@'%';
FLUSH PRIVILEGES;

-- 执行初始化脚本
USE rbac_system;
SOURCE /path/to/rbac_init.sql;
```

### 2.2 PostgreSQL 配置

```sql
-- 创建数据库
CREATE DATABASE rbac_system WITH ENCODING 'UTF8';

-- 创建用户
CREATE USER rbac_user WITH PASSWORD 'rbac_password';
GRANT ALL PRIVILEGES ON DATABASE rbac_system TO rbac_user;

-- 连接数据库并执行初始化脚本
\c rbac_system;
\i /path/to/rbac_init.sql;
```

## 3. 应用配置

### 3.1 生产环境配置 (application-prod.yml)

```yaml
spring:
  # 数据源配置
  datasource:
    url: jdbc:mysql://localhost:3306/rbac_system?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: rbac_user
    password: rbac_password
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      
  # Redis配置
  redis:
    host: localhost
    port: 6379
    password: redis_password
    database: 0
    timeout: 3000ms
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5
        max-wait: 3000ms
        
  # 缓存配置
  cache:
    type: redis
    redis:
      time-to-live: 300000 # 5分钟
      cache-null-values: false
      
# MyBatis Plus配置
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.nologging.NoLoggingImpl
  global-config:
    db-config:
      logic-delete-field: deletedTime
      logic-delete-value: 'NOW()'
      logic-not-delete-value: 'NULL'

# RBAC性能配置
rbac:
  performance:
    cache-ttl: 300
    batch-size: 100
    async-core-pool-size: 5
    async-max-pool-size: 20
    async-queue-capacity: 200
  permission:
    check-enabled: true
    super-admin-role: super_admin
    
# 日志配置
logging:
  level:
    com.spark.demo: INFO
    org.springframework.cache: WARN
  file:
    name: logs/rbac-system.log
  logback:
    rollingpolicy:
      max-file-size: 100MB
      max-history: 30
      total-size-cap: 3GB
```

### 3.2 测试环境配置 (application-test.yml)

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password: 
    
  cache:
    type: simple
    
logging:
  level:
    com.spark.demo: DEBUG
```

## 4. 构建和打包

### 4.1 Maven 构建

```bash
# 清理并编译
mvn clean compile

# 运行测试
mvn test

# 打包应用
mvn clean package -Dmaven.test.skip=true

# 构建Docker镜像
mvn spring-boot:build-image
```

### 4.2 Gradle 构建

```bash
# 清理并编译
./gradlew clean build

# 运行测试
./gradlew test

# 打包应用
./gradlew bootJar

# 构建Docker镜像
./gradlew bootBuildImage
```

## 5. Docker 部署

### 5.1 Dockerfile

```dockerfile
FROM openjdk:17-jre-slim

# 设置工作目录
WORKDIR /app

# 复制应用JAR文件
COPY target/my-springboot-service-*.jar app.jar

# 创建日志目录
RUN mkdir -p /app/logs

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 暴露端口
EXPOSE 8080

# 设置JVM参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+PrintGCDetails"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### 5.2 docker-compose.yml

```yaml
version: '3.8'

services:
  # MySQL数据库
  mysql:
    image: mysql:8.0
    container_name: rbac-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root_password
      MYSQL_DATABASE: rbac_system
      MYSQL_USER: rbac_user
      MYSQL_PASSWORD: rbac_password
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./sql/rbac_init.sql:/docker-entrypoint-initdb.d/rbac_init.sql
    networks:
      - rbac-network
    restart: unless-stopped

  # Redis缓存
  redis:
    image: redis:6.2-alpine
    container_name: rbac-redis
    command: redis-server --requirepass redis_password
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - rbac-network
    restart: unless-stopped

  # RBAC应用
  rbac-app:
    build: .
    container_name: rbac-app
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/rbac_system?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
      SPRING_DATASOURCE_USERNAME: rbac_user
      SPRING_DATASOURCE_PASSWORD: rbac_password
      SPRING_REDIS_HOST: redis
      SPRING_REDIS_PASSWORD: redis_password
    ports:
      - "8080:8080"
    volumes:
      - app_logs:/app/logs
    depends_on:
      - mysql
      - redis
    networks:
      - rbac-network
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

volumes:
  mysql_data:
  redis_data:
  app_logs:

networks:
  rbac-network:
    driver: bridge
```

### 5.3 部署命令

```bash
# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看应用日志
docker-compose logs -f rbac-app

# 停止所有服务
docker-compose down

# 重新构建并启动
docker-compose up -d --build
```

## 6. Kubernetes 部署

### 6.1 ConfigMap

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: rbac-config
data:
  application.yml: |
    spring:
      profiles:
        active: k8s
      datasource:
        url: jdbc:mysql://mysql-service:3306/rbac_system
        username: rbac_user
        password: rbac_password
      redis:
        host: redis-service
        port: 6379
        password: redis_password
```

### 6.2 Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: rbac-app
  labels:
    app: rbac-app
spec:
  replicas: 3
  selector:
    matchLabels:
      app: rbac-app
  template:
    metadata:
      labels:
        app: rbac-app
    spec:
      containers:
      - name: rbac-app
        image: rbac-system:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "k8s"
        volumeMounts:
        - name: config-volume
          mountPath: /app/config
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
      volumes:
      - name: config-volume
        configMap:
          name: rbac-config
```

### 6.3 Service

```yaml
apiVersion: v1
kind: Service
metadata:
  name: rbac-service
spec:
  selector:
    app: rbac-app
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  type: LoadBalancer
```

## 7. 监控和运维

### 7.1 健康检查

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  health:
    redis:
      enabled: true
    db:
      enabled: true
```

### 7.2 日志配置

```xml
<!-- logback-spring.xml -->
<configuration>
    <springProfile name="prod">
        <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <file>logs/rbac-system.log</file>
            <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
                <fileNamePattern>logs/rbac-system.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
                <maxFileSize>100MB</maxFileSize>
                <maxHistory>30</maxHistory>
                <totalSizeCap>3GB</totalSizeCap>
            </rollingPolicy>
            <encoder>
                <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
            </encoder>
        </appender>
        
        <root level="INFO">
            <appender-ref ref="FILE"/>
        </root>
    </springProfile>
</configuration>
```

### 7.3 性能监控

```bash
# 使用Prometheus监控
# 添加依赖
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>

# 配置Grafana仪表板
# 导入Spring Boot应用监控模板
```

## 8. 安全配置

### 8.1 HTTPS配置

```yaml
server:
  port: 8443
  ssl:
    key-store: classpath:keystore.p12
    key-store-password: password
    key-store-type: PKCS12
    key-alias: rbac-system
```

### 8.2 防火墙配置

```bash
# 开放必要端口
sudo ufw allow 8080/tcp
sudo ufw allow 8443/tcp
sudo ufw allow 3306/tcp  # MySQL
sudo ufw allow 6379/tcp  # Redis

# 限制访问源
sudo ufw allow from 192.168.1.0/24 to any port 3306
```

## 9. 备份和恢复

### 9.1 数据库备份

```bash
# MySQL备份
mysqldump -u rbac_user -p rbac_system > rbac_backup_$(date +%Y%m%d_%H%M%S).sql

# 自动备份脚本
#!/bin/bash
BACKUP_DIR="/backup/rbac"
DATE=$(date +%Y%m%d_%H%M%S)
mysqldump -u rbac_user -p rbac_system | gzip > $BACKUP_DIR/rbac_$DATE.sql.gz

# 保留最近30天的备份
find $BACKUP_DIR -name "rbac_*.sql.gz" -mtime +30 -delete
```

### 9.2 Redis备份

```bash
# Redis备份
redis-cli --rdb /backup/redis/dump_$(date +%Y%m%d_%H%M%S).rdb
```

## 10. 故障排除

### 10.1 常见问题

**问题1**: 应用启动失败
```bash
# 检查日志
docker logs rbac-app
tail -f logs/rbac-system.log

# 检查端口占用
netstat -tulpn | grep 8080
```

**问题2**: 数据库连接失败
```bash
# 检查数据库状态
docker exec -it rbac-mysql mysql -u rbac_user -p

# 检查网络连通性
docker exec -it rbac-app ping mysql
```

**问题3**: Redis连接失败
```bash
# 检查Redis状态
docker exec -it rbac-redis redis-cli ping

# 检查Redis配置
docker exec -it rbac-redis redis-cli config get requirepass
```

### 10.2 性能调优

```yaml
# JVM参数优化
JAVA_OPTS: >
  -Xms1g -Xmx2g
  -XX:+UseG1GC
  -XX:MaxGCPauseMillis=200
  -XX:+PrintGCDetails
  -XX:+PrintGCTimeStamps
  -XX:+HeapDumpOnOutOfMemoryError
  -XX:HeapDumpPath=/app/logs/

# 数据库连接池优化
spring:
  datasource:
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10
      connection-timeout: 20000
      idle-timeout: 300000
      max-lifetime: 1200000
```

## 11. 升级指南

### 11.1 滚动升级

```bash
# 1. 备份数据
./backup.sh

# 2. 构建新版本
docker build -t rbac-system:v2.0 .

# 3. 更新docker-compose.yml
# 修改镜像版本

# 4. 滚动更新
docker-compose up -d --no-deps rbac-app

# 5. 验证升级
curl http://localhost:8080/actuator/health
```

### 11.2 数据库迁移

```sql
-- 创建迁移脚本
-- V2.0__add_new_features.sql

-- 添加新字段
ALTER TABLE sys_user ADD COLUMN last_login_time DATETIME;

-- 创建新表
CREATE TABLE sys_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    action VARCHAR(100),
    resource VARCHAR(200),
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

这个部署指南涵盖了从开发环境到生产环境的完整部署流程，包括性能优化、监控、安全和运维等各个方面。 