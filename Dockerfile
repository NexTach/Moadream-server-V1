FROM openjdk:17-jdk-slim

ARG JAR_FILE=./build/libs/Moadream-server-V1-0.0.1
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java","-Dspring.profiles.active=deploy","-jar","app.jar"]