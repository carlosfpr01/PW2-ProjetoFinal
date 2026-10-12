#!/bin/bash
set -e

SCRIPT_DIR="$(cd -- "$(dirname -- "$0")" && pwd)"
cd "$SCRIPT_DIR"

if ! command -v docker >/dev/null 2>&1; then
    echo "Erro: Docker não foi encontrado. Instale o Docker e tente novamente." >&2
    exit 1
fi

if ! docker compose version >/dev/null 2>&1; then
    echo "Erro: Docker Compose não está disponível. Instale o plugin Docker Compose e tente novamente." >&2
    exit 1
fi

exec docker compose up "$@"
