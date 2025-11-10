#!/bin/bash

COMPOSE_FILE="docker-compose.yml"
PROJECT_NAME="moadream"

echo "> Starting application deployment with Docker Compose..."

if [ ! -z "$GHCR_TOKEN" ]; then
    echo "> Logging in to GitHub Container Registry..."
    echo $GHCR_TOKEN | docker login ghcr.io -u $GHCR_USER --password-stdin
    if [ $? -ne 0 ]; then
        echo "> Failed to login to GHCR"
        exit 1
    fi
fi

if ! command -v docker-compose &> /dev/null; then
    echo "> docker-compose not found, using 'docker compose' plugin"
    DOCKER_COMPOSE="docker compose"
else
    DOCKER_COMPOSE="docker-compose"
fi

echo "> Pulling latest images..."
$DOCKER_COMPOSE -f $COMPOSE_FILE -p $PROJECT_NAME pull

if [ $? -ne 0 ]; then
    echo "> Failed to pull images"
    exit 1
fi

echo "> Starting services with Docker Compose..."
$DOCKER_COMPOSE -f $COMPOSE_FILE -p $PROJECT_NAME up -d

if [ $? -eq 0 ]; then
    echo "> Services started successfully"
    echo "> Application is running on port 8080"
    echo "> Mock API is running on port 9000"
    echo "> Ollama is running on port 11434"

    echo ""
    echo "> Running containers:"
    docker ps --filter "name=${PROJECT_NAME}" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
else
    echo "> Failed to start services"
    exit 1
fi