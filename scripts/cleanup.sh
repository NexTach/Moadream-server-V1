#!/bin/bash

echo "> Cleaning up Docker system..."
docker compose down -v
docker system prune -af --volumes
echo "> Docker cleanup completed"