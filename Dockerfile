FROM eclipse-temurin:17-jre

# 타임존 설정 및 시스템 심볼릭 링크 생성
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

ARG JAR_FILE=app.jar
COPY ${JAR_FILE} app.jar

# JVM 타임존 강제 지정 옵션 추가
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-Dspring.profiles.active=prod", "-jar", "/app.jar"]