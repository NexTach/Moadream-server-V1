FROM eclipse-temurin:17-jdk-jammy

RUN apt-get update && \
    apt-get install -y python3 python3-pip && \
    rm -rf /var/lib/apt/lists/*

COPY mock-api /mock-api
WORKDIR /mock-api
RUN pip3 install --no-cache-dir -r requirements.txt

WORKDIR /
ARG JAR_FILE=build/libs/Moadream-server-V1-0.0.1.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java","-Dspring.profiles.active=deploy","-jar","app.jar"]