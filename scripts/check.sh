#!/usr/bin/env bash
set -euo pipefail

if [[ "${1:-}" == "--docs-only" ]]; then
  ./gradlew :intentflow-generator:run --args="validate .intentflow/login.intentflow.yaml"
  ./gradlew :intentflow-generator:run --args="ai-context .intentflow/login.intentflow.yaml --tool codex" >/tmp/intentflow-android-ai-context.md
  test -f README.md
  test -f docs/wiki/Home.md
  test -f SECURITY.md
  exit 0
fi

./gradlew test
./gradlew dokkaHtml
./gradlew :intentflow-generator:run --args="feature SmokeFeature --mode ai --ui none --output /tmp/intentflow-android-smoke"
./gradlew :intentflow-generator:run --args="validate .intentflow/login.intentflow.yaml"
./gradlew :intentflow-generator:run --args="ai-context .intentflow/login.intentflow.yaml --tool codex" >/tmp/intentflow-android-ai-context.md
