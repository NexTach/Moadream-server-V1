#!/bin/bash

echo "> Creating .env file from environment variables..."

if [ -z "$ENV_PROPERTIES" ]; then
    echo "> Error: ENV_PROPERTIES environment variable is not set"
    exit 1
fi

echo "$ENV_PROPERTIES" > .env

if [ $? -eq 0 ]; then
    echo "> .env file created successfully"
    chmod 600 .env
    echo "> .env file permissions set to 600"
else
    echo "> Error: Failed to create .env file"
    exit 1
fi

echo "> .env file creation completed"