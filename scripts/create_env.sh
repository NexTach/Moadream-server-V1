#!/bin/bash

echo "> Renaming .env.prod to .env..."

if [ ! -f ".env.prod" ]; then
    echo "> Error: .env.prod file not found"
    exit 1
fi

mv .env.prod .env

if [ $? -eq 0 ]; then
    echo "> .env.prod renamed to .env successfully"
    chmod 600 .env
    echo "> .env file permissions set to 600"
else
    echo "> Error: Failed to rename .env.prod"
    exit 1
fi

echo "> .env file setup completed"