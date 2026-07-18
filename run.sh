#!/usr/bin/env bash
# Loads .env then starts the backend. Usage: ./run.sh
set -euo pipefail

cd "$(dirname "$0")"

if [ -f .env ]; then
  set -a
  # shellcheck disable=SC1091
  . ./.env
  set +a
else
  echo "No .env found — copy .env.example to .env and fill in BREVO_API_KEY." >&2
fi

exec ./mvnw spring-boot:run
