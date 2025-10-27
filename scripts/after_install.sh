#!/usr/bin/env bash

set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
GRADLEW="$PROJECT_ROOT/gradlew"

echo "> project root: $PROJECT_ROOT"

if [ -f "$GRADLEW" ]; then
  echo "> found gradlew at $GRADLEW"
  if [ -x "$GRADLEW" ]; then
    echo "> gradlew is already executable"
  else
    echo "> making gradlew executable"
    chmod +x "$GRADLEW"
    echo "> gradlew is now executable"
  fi
else
  echo "> no gradlew wrapper found at $GRADLEW — skipping chmod"
fi