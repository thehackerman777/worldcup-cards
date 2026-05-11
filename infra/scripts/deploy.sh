#!/bin/bash
# ═══════════════════════════════════════════════════════════
# Deploy Script - World Cup Cards
# ═══════════════════════════════════════════════════════════
# Uso: ./deploy.sh [prod|dev]
# ═══════════════════════════════════════════════════════════

set -e

ENV=${1:-dev}
COMPOSE_FILE="../docker-compose.yml"

echo "🚀 Deploying World Cup Cards - Environment: $ENV"

# Load environment file
if [ -f ".env.${ENV}" ]; then
    export $(grep -v '^#' ".env.${ENV}" | xargs)
    echo "✅ Environment loaded from .env.${ENV}"
else
    echo "⚠️  No .env.${ENV} found, using defaults"
fi

# Build and start
echo "🔨 Building and starting containers..."
docker compose -f $COMPOSE_FILE down
docker compose -f $COMPOSE_FILE build --no-cache
docker compose -f $COMPOSE_FILE up -d

# Check health
echo "⏳ Waiting for backend to be healthy..."
sleep 10
if curl -sf http://localhost:8080/api/v1/health > /dev/null 2>&1; then
    echo "✅ Backend is healthy!"
else
    echo "⚠️  Backend health check failed, check logs: docker compose logs backend"
fi

echo "✅ Deployment complete!"
echo "🌐 API: http://localhost:8080"
echo "📊 Health: http://localhost:8080/api/v1/health"
