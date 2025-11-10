#!/bin/bash

COMPOSE_FILE="docker-compose.yml"
PROJECT_NAME="moadream"

echo "> Stopping application with Docker Compose..."

if ! command -v docker-compose &> /dev/null; then
    echo "> docker-compose not found, using 'docker compose' plugin"
    DOCKER_COMPOSE="docker compose"
else
    DOCKER_COMPOSE="docker-compose"
fi

if [ ! -f "$COMPOSE_FILE" ]; then
    echo "> docker-compose.yml not found, falling back to legacy stop..."

    CONTAINER_NAME="moadream-container"
    if [ "$(docker ps -q -f name=$CONTAINER_NAME)" ]; then
        echo "> Stopping running container..."
        docker stop $CONTAINER_NAME
        echo "> Container stopped"
    fi
    if [ "$(docker ps -aq -f name=$CONTAINER_NAME)" ]; then
        echo "> Removing container..."
        docker rm $CONTAINER_NAME
        echo "> Container removed"
    fi
else
    echo "> Stopping Docker Compose services..."
    $DOCKER_COMPOSE -f $COMPOSE_FILE -p $PROJECT_NAME down

    if [ $? -eq 0 ]; then
        echo "> Services stopped successfully"
    else
        echo "> Failed to stop services"
        exit 1
    fi
fi