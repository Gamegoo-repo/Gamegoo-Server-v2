# [1] 빌드 스테이지
FROM gradle:8.11.1-jdk17 AS build

WORKDIR /app

# 빌드 시 전달받은 ARG(환경변수)
ARG JWT_SECRET
ARG RDS_PRIVATE_IP
ARG RDS_PORT
ARG DB_SCHEMA_NAME
ARG DB_USERNAME
ARG DB_PASSWORD
ARG GMAIL_PWD
ARG RIOT_API
ARG SOCKET_SERVER_URL

# Gradle 라이브러리 설치를 위한 초기 세팅
COPY build.gradle settings.gradle ./
RUN gradle dependencies --no-daemon

# 소스코드 복사 후 빌드
COPY . /app
RUN gradle clean build --no-daemon

# [2] 런타임 스테이지
FROM eclipse-temurin:17-jre

WORKDIR /app

# 1) 컨테이너 시간대 KST 설정
ENV TZ=Asia/Seoul
RUN apt-get update && apt-get install -y tzdata \
    && ln -snf /usr/share/zoneinfo/$TZ /etc/localtime \
    && echo $TZ > /etc/timezone \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

# 2) 빌드 결과 JAR 복사
COPY --from=build /app/build/libs/*SNAPSHOT.jar /app/gamegoov2.jar

# 3) 실행
CMD ["java", "-Dspring.profiles.active=dev", "-jar", "gamegoov2.jar"]
