#!/bin/bash

IMAGE_NAME="ghcr.io/nextach/moadream-server:latest"
CONTAINER_NAME="moadream-container"

echo "> Starting application deployment..."

if [ ! -z "$GHCR_TOKEN" ]; then
    echo "> Logging in to GitHub Container Registry..."
    echo $GHCR_TOKEN | docker login ghcr.io -u $GHCR_USER --password-stdin
    if [ $? -ne 0 ]; then
        echo "> Failed to login to GHCR"
        exit 1
    fi
fi

echo "> Pulling Docker image: $IMAGE_NAME"
docker pull $IMAGE_NAME
if [ $? -eq 0 ]; then
    echo "> Docker image pulled successfully"
else
    echo "> Failed to pull Docker image"
    exit 1
fi
echo "> Starting Docker container: $CONTAINER_NAME"
docker run -d --name $CONTAINER_NAME -p 8080:8080 $IMAGE_NAME
if [ $? -eq 0 ]; then
    echo "> Container started successfully"
    echo "> Application is running on port 8080"
else
    echo "> Failed to start container"
    exit 1
fi