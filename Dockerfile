FROM python:3.11-slim

RUN apt-get update && \
    apt-get install -y --no-install-recommends wget gnupg && \
    mkdir -p /etc/apt/keyrings && \
    wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | gpg --dearmor -o /etc/apt/keyrings/adoptium.gpg && \
    echo "deb [signed-by=/etc/apt/keyrings/adoptium.gpg] https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | tee /etc/apt/sources.list.d/adoptium.list && \
    apt-get update && \
    apt-get install -y --no-install-recommends temurin-17-jdk && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /mock-api
COPY mock-api/requirements.txt .
RUN pip3 install --no-cache-dir -r requirements.txt

COPY mock-api /mock-api

WORKDIR /
ARG JAR_FILE=build/libs/Moadream-server-V1-0.0.1.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8080

ENTRYPOINT ["java","-Dspring.profiles.active=deploy","-jar","app.jar"]