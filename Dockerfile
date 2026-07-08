# --- Stage 1: Build ứng dụng ---
FROM maven:3.9.6-eclipse-temurin-21-jammy AS build
WORKDIR /app
# Copy tệp cấu hình Maven và tải trước dependencies để tận dụng Docker Cache
COPY pom.xml .
RUN mvn dependency:go-offline -B
# Copy thư mục source code và build file JAR
COPY src ./src
RUN mvn package -DskipTests
# --- Stage 2: Khởi chạy ứng dụng ---
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
# Copy file jar đã build từ Stage 1 sang
COPY --from=build /app/target/*.jar app.jar
# Mở cổng 8081 (cổng Spring Boot của bạn đang chạy)
EXPOSE 8081
# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
