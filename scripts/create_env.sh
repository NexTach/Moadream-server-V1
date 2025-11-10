#!/bin/bash

echo "> Verifying .env file..."

if [ -f ".env" ]; then
    echo "> .env file found"
else
    echo "> Error: .env file not found. It should be created by deployment process."
    exit 1
fi

echo "> .env file verification completed"