FROM python:3.11-slim AS python-deps
WORKDIR /mock-api
COPY mock-api/requirements.txt .
RUN pip3 install --no-cache-dir --target=/python-packages -r requirements.txt

FROM eclipse-temurin:17-jdk-jammy

RUN sed -i 's/archive.ubuntu.com/mirror.kakao.com/g' /etc/apt/sources.list && \
    apt-get update && \
    apt-get install -y python3 && \
    rm -rf /var/lib/apt/lists/*

COPY --from=python-deps /python-packages /python-packages
ENV PYTHONPATH=/python-packages

COPY mock-api /mock-api

WORKDIR /
ARG JAR_FILE=build/libs/Moadream-server-V1-0.0.1.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8080

ENTRYPOINT ["java","-Dspring.profiles.active=deploy","-jar","app.jar"]