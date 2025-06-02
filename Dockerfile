# JDK 17을 기반 이미지로 사용
FROM openjdk:21-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app

# Maven Wrapper 파일 복사
COPY mvnw .
COPY .mvn .mvn

# POM 파일 복사
COPY pom.xml .

# 의존성 다운로드 (애플리케이션 코드 변경 없이 의존성만 변경되었을 때 레이어 캐싱 활용)
RUN ./mvnw dependency:go-offline -B

# 애플리케이션 소스 코드 복사
COPY src src

# Maven을 사용하여 애플리케이션 빌드
RUN ./mvnw package -DskipTests

# 최종 애플리케이션 JAR 파일 경로
ARG JAR_FILE=target/*.jar

# JAR 파일 실행
ENTRYPOINT ["sh", "-c", "exec java -jar target/*.jar"]
