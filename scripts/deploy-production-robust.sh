#!/bin/bash
set -Eeuo pipefail

# Robust Production Deployment Script
# Handles Kafka cluster ID issues, volume management, and edge cases

# Colors for output
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'
log(){ case "$1" in INFO) echo -e "${BLUE}ℹ️  $2${NC}";; WARN) echo -e "${YELLOW}⚠️  $2${NC}";;
FAIL) echo -e "${RED}❌ $2${NC}";; PASS) echo -e "${GREEN}✅ $2${NC}";; esac; }

# Compose detection
if docker compose version >/dev/null 2>&1; then COMPOSE="docker compose";
elif docker-compose version >/dev/null 2>&1; then COMPOSE="docker-compose";
else log FAIL "Neither 'docker compose' nor 'docker-compose' found."; exit 1; fi

# Paths and defaults
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"; cd "$ROOT_DIR"
ENV_FILE=".env-prod"; COMPOSE_FILE="docker-compose.prod.yml"
STATE_FILE=".deploy-state.json"
SEED_ONCE="false"; NO_BUILD="false"; RELOAD_NGINX="false"; SERVICES=()
FORCE_KAFKA_RESET="false"; REMOVE_ORPHANS="false"

usage(){ cat <<EOF
Usage: $(basename "$0") [options] [services...]
Services: postgres redis kafka mailhog backend frontend nginx
Options:
  --seed-once       Enable demo seed just for this deploy
  --no-build        Skip local builds (recreate containers only)
  --reload-nginx    Reload nginx config if running (no recreate)
  --force-kafka-reset Reset Kafka volumes (fixes cluster ID issues)
  --remove-orphans  Remove orphan containers (cleanup old services)
  -h, --help        Show this help
EOF
}

# Parse CLI
while [[ $# -gt 0 ]]; do
  case "$1" in
    --seed-once) SEED_ONCE="true";;
    --no-build) NO_BUILD="true";;
    --reload-nginx) RELOAD_NGINX="true";;
    --force-kafka-reset) FORCE_KAFKA_RESET="true";;
    --remove-orphans) REMOVE_ORPHANS="true";;
    -h|--help) usage; exit 0;;
    postgres|redis|zookeeper|kafka|mailhog|backend|frontend|nginx) SERVICES+=("$1");;
    *) log FAIL "Unknown argument: $1"; usage; exit 1;;
  esac; shift
done
[[ ${#SERVICES[@]} -eq 0 ]] && SERVICES=(postgres redis kafka mailhog backend frontend nginx)

log INFO "🚀 Starting Robust AnkurShala Production Deployment"
echo "======================================================"

# Environment check
[[ -f "$ENV_FILE" ]] || { log FAIL "$ENV_FILE not found"; exit 1; }
log INFO "Loading environment from $ENV_FILE ..."
set -a; source "$ENV_FILE"; set +a
REQ=(DB_NAME DB_USERNAME DB_PASSWORD JWT_SECRET REDIS_PASSWORD BANK_ENC_KEY)
MISS=(); for v in "${REQ[@]}"; do [[ -z "${!v:-}" ]] && MISS+=("$v") || log PASS "$v is set (${#v} chars)"; done
[[ ${#MISS[@]} -gt 0 ]] && { log FAIL "Missing env vars: ${MISS[*]}"; exit 1; }
log PASS "All required environment variables are loaded"

# Compute current SHAs
FE_SHA=$(git rev-parse --short HEAD:frontend 2>/dev/null || git rev-parse --short HEAD)
BE_SHA=$(git rev-parse --short HEAD:backend  2>/dev/null || git rev-parse --short HEAD)
export FRONTEND_TAG="prod-${FE_SHA}"
export BACKEND_TAG="prod-${BE_SHA}"

log INFO "Detected SHAs → frontend:$FE_SHA backend:$BE_SHA"

# Read last deployed SHAs
LAST_FE=""; LAST_BE=""
if [[ -f "$STATE_FILE" ]]; then
  LAST_FE=$(grep -oE '"frontend"\s*:\s*"[^"]*"' "$STATE_FILE" | awk -F\" '{print $4}' || true)
  LAST_BE=$(grep -oE '"backend"\s*:\s*"[^"]*"'  "$STATE_FILE" | awk -F\" '{print $4}' || true)
fi

NEEDS_FE_BUILD="false"; NEEDS_BE_BUILD="false"
[[ "$FE_SHA" != "$LAST_FE" ]] && NEEDS_FE_BUILD="true"
[[ "$BE_SHA" != "$LAST_BE" ]] && NEEDS_BE_BUILD="true"

# If user asked for specific service build, respect that
explicit_frontend=false; explicit_backend=false
for s in "${SERVICES[@]}"; do
  [[ "$s" == "frontend" ]] && explicit_frontend=true
  [[ "$s" == "backend" ]]  && explicit_backend=true
done
$explicit_frontend && NEEDS_FE_BUILD="true"
$explicit_backend  && NEEDS_BE_BUILD="true"

[[ "$NO_BUILD" == "true" ]] && { NEEDS_FE_BUILD="false"; NEEDS_BE_BUILD="false"; }

# Wait for healthy function
wait_healthy() {
  local name="$1" timeout="${2:-180}"
  log INFO "Waiting for container '$name' (timeout ${timeout}s)..."
  local start now status running; start=$(date +%s)
  while true; do
    if ! docker inspect "$name" >/dev/null 2>&1; then sleep 1; continue; fi
    status=$(docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{end}}' "$name" 2>/dev/null || true)
    status="${status:-none}"
    running=$(docker inspect --format '{{.State.Status}}' "$name" 2>/dev/null || echo "exited")
    if [[ "$status" == "healthy" ]] || { [[ "$status" == "none" ]] && [[ "$running" == "running" ]]; }; then
      [[ "$status" == "healthy" ]] && log PASS "'$name' is healthy" || log PASS "'$name' is running (no healthcheck)"
      return 0
    fi
    now=$(date +%s); (( now - start > timeout )) && { log FAIL "Timeout waiting for '$name' (status: $status / $running)"; return 1; }
    sleep 3
  done
}

# Recreate service function
recreate_service() {
  local svc="$1"
  local buildFlags="--pull"
  
  if [[ "$svc" == "frontend" && "$NEEDS_FE_BUILD" == "true" ]]; then buildFlags="--pull --no-cache"; fi
  if [[ "$svc" == "backend"  && "$NEEDS_BE_BUILD" == "true" ]]; then buildFlags="--pull --no-cache"; fi

  if [[ "$NO_BUILD" != "true" && ( "$svc" == "backend" || "$svc" == "frontend" ) ]]; then
    # Check if we have Azure Storage images available
    if [[ -f "backend-image.tar" ]] && [[ "$svc" == "backend" ]]; then
      log INFO "Loading backend image from Azure Storage..."
      docker load < backend-image.tar
      log PASS "Loaded backend image from Azure Storage"
    elif [[ -f "frontend-image.tar" ]] && [[ "$svc" == "frontend" ]]; then
      log INFO "Loading frontend image from Azure Storage..."
      docker load < frontend-image.tar
      log PASS "Loaded frontend image from Azure Storage"
    else
      log INFO "Building image for service: $svc ($buildFlags)"
      $COMPOSE -f "$COMPOSE_FILE" --env-file "$ENV_FILE" build $buildFlags "$svc"
    fi
  else
    log INFO "Skipping local build for: $svc"
  fi

  log INFO "Recreating: $svc (no deps, force new image)"
  $COMPOSE -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d --no-deps --force-recreate "$svc"

  case "$svc" in
    postgres)  wait_healthy "ankurshala_db_prod" 180;;
    redis)     wait_healthy "ankurshala_redis_prod" 120;;
    zookeeper) wait_healthy "ankurshala_zookeeper_prod" 120;;
    kafka)     wait_healthy "ankurshala_kafka_prod" 300 || log WARN "Kafka healthcheck failed during recreate; proceeding";;
    mailhog)   wait_healthy "ankurshala_mailhog_prod" 30;;
    backend)   wait_healthy "ankurshala_backend_prod" 240;;
    frontend)  wait_healthy "ankurshala_frontend_prod" 240;;
    nginx)     wait_healthy "ankurshala_nginx_prod" 90;;
  esac
}

# Kafka KRaft mode management
manage_kafka_kraft() {
  log INFO "🔧 Managing Kafka KRaft cluster..."
  
  # Stop Kafka if running
  $COMPOSE -f "$COMPOSE_FILE" --env-file "$ENV_FILE" stop kafka || true
  
  # Remove Kafka volume to reset cluster metadata
  log INFO "Removing Kafka volume to reset cluster metadata..."
  docker volume rm ankurshala-eduplatform_kafka_data 2>/dev/null || true
  
  # Start Kafka KRaft fresh
  log INFO "Starting Kafka KRaft cluster..."
  $COMPOSE -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d kafka
  
  # Wait for Kafka KRaft with extended timeout
  if wait_healthy "ankurshala_kafka_prod" 300; then
    log PASS "Kafka KRaft cluster started successfully"
  else
    log WARN "Kafka KRaft health check failed, but continuing..."
    if docker ps --format '{{.Names}}' | grep -q '^ankurshala_kafka_prod$'; then
      log WARN "Kafka KRaft is running - deployment will continue"
    else
      log FAIL "Kafka KRaft failed to start completely"
      return 1
    fi
  fi
}

# One-time seed
SEED_FILE=".seed.override.yml"
if [[ "$SEED_ONCE" == "true" ]]; then
  log WARN "One-time seeding ENABLED (DEMO_SEED_ON_START=true, DEMO_ENV=prod, DEMO_FORCE=true)"
  cat > "$SEED_FILE" <<'YAML'
services:
  backend:
    environment:
      DEMO_SEED_ON_START: "true"
      DEMO_ENV: "prod"
      DEMO_FORCE: "true"
YAML
else
  [[ -f "$SEED_FILE" ]] && rm -f "$SEED_FILE"
fi

# Bring base infrastructure up (Kafka KRaft mode - no Zookeeper needed)
log INFO "Bringing base infra up (Kafka KRaft mode)..."
$COMPOSE -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d postgres redis mailhog kafka
wait_healthy "ankurshala_db_prod" 180
wait_healthy "ankurshala_redis_prod" 120
wait_healthy "ankurshala_mailhog_prod" 30

# Clean up old containers and volumes for KRaft migration
if [[ "$REMOVE_ORPHANS" == "true" ]]; then
  log INFO "🧹 Cleaning up old Zookeeper containers and volumes..."
  $COMPOSE -f "$COMPOSE_FILE" --env-file "$ENV_FILE" down --remove-orphans || true
  docker volume rm ankurshala-eduplatform_kafka_data 2>/dev/null || true
  docker volume rm ankurshala-eduplatform_zookeeper_data 2>/dev/null || true
  
  # Force stop and remove any remaining Kafka/Zookeeper containers
  log INFO "🔧 Force stopping old Kafka/Zookeeper containers..."
  docker stop ankurshala_kafka_prod ankurshala_zookeeper_prod 2>/dev/null || true
  docker rm ankurshala_kafka_prod ankurshala_zookeeper_prod 2>/dev/null || true
  
  # Remove all volumes to ensure clean start
  log INFO "🗑️ Removing all Kafka-related volumes..."
  docker volume ls -q | grep -E "(kafka|zookeeper)" | xargs -r docker volume rm 2>/dev/null || true
fi

# Verify Docker Compose configuration
log INFO "🔍 Verifying Docker Compose configuration..."
if grep -q "KAFKA_PROCESS_ROLES" "$COMPOSE_FILE"; then
  log PASS "Docker Compose file contains KRaft configuration"
else
  log FAIL "Docker Compose file missing KRaft configuration!"
  exit 1
fi

# Handle Kafka KRaft mode startup
if [[ "$FORCE_KAFKA_RESET" == "true" ]]; then
  log INFO "Force resetting Kafka KRaft cluster..."
  $COMPOSE -f "$COMPOSE_FILE" --env-file "$ENV_FILE" stop kafka || true
  docker volume rm ankurshala-eduplatform_kafka_data 2>/dev/null || true
  
  # Show Kafka configuration before starting
  log INFO "📋 Kafka KRaft configuration:"
  docker run --rm confluentinc/cp-kafka:7.4.0 env | grep -E "(KAFKA_|CLUSTER_ID)" | head -10
  
  $COMPOSE -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d kafka
fi

# Wait for Kafka KRaft to be ready
log INFO "Waiting for Kafka KRaft cluster to be ready..."
if wait_healthy "ankurshala_kafka_prod" 180; then
  log PASS "Kafka KRaft cluster is healthy"
  
  # Verify KRaft mode is actually running
  log INFO "🔍 Verifying KRaft mode..."
  if docker logs ankurshala_kafka_prod 2>&1 | grep -q "KRaft"; then
    log PASS "Kafka is running in KRaft mode"
  else
    log WARN "Kafka mode verification unclear - checking logs..."
    docker logs ankurshala_kafka_prod 2>&1 | tail -5
  fi
else
  log WARN "Kafka health check failed, checking status..."
  if docker ps --format '{{.Names}}' | grep -q '^ankurshala_kafka_prod$'; then
    log WARN "Kafka is running but health check failed - continuing with deployment"
    docker logs --tail 10 ankurshala_kafka_prod 2>&1 | head -5
  else
    log FAIL "Kafka failed to start - aborting deployment"
    docker logs ankurshala_kafka_prod 2>&1 | tail -20
    exit 1
  fi
fi

# Deploy requested services
for svc in "${SERVICES[@]}"; do
  case "$svc" in
    postgres|redis|mailhog) recreate_service "$svc";;
    zookeeper) log WARN "Zookeeper not needed in KRaft mode - skipping";;
    backend)
      if [[ "$SEED_ONCE" == "true" ]]; then
        log INFO "Recreating backend WITH seed override (one-time)"
        $COMPOSE -f "$COMPOSE_FILE" -f "$SEED_FILE" --env-file "$ENV_FILE" up -d --no-deps --force-recreate backend
      else
        recreate_service backend
      fi
      ;;
    frontend) recreate_service frontend;;
    nginx) [[ "$RELOAD_NGINX" == "true" ]] && reload_nginx || recreate_service nginx;;
    *) log WARN "Unknown service: $svc (skipping)";;
  esac
done

# Prune old images (keep last 2 per service)
log INFO "Pruning old backend/frontend images (keeping last 2)..."
cleanup_images() {
  local repo="$1"
  to_delete=$(docker images --format '{{.Repository}}:{{.Tag}} {{.CreatedAt}}' | grep "^${repo}:" | sort -rk2 | awk 'NR>2{print $1}')
  for img in $to_delete; do
    log INFO "Removing old image: $img"
    docker rmi -f "$img" || true
  done
}
cleanup_images "ankurshala/backend"
cleanup_images "ankurshala/frontend"

# Write new state if builds happened
if [[ "$NEEDS_FE_BUILD" == "true" || "$NEEDS_BE_BUILD" == "true" ]]; then
  cat > "$STATE_FILE" <<JSON
{
  "frontend":"$FE_SHA",
  "backend":"$BE_SHA",
  "updated_at":"$(date -u +%FT%TZ)"
}
JSON
  log PASS "Updated $STATE_FILE (frontend:$FE_SHA backend:$BE_SHA)"
else
  log INFO "No code changes detected for backend/frontend. State unchanged."
fi

# Final status
log INFO "Current service status:"
$COMPOSE -f "$COMPOSE_FILE" --env-file "$ENV_FILE" ps
log PASS "🎉 Robust deployment completed!"
echo
echo "📋 Tips:"
echo "• Deploy only backend:   $0 backend"
echo "• Deploy only frontend:  $0 frontend"
echo "• Fix Kafka issues:      $0 --force-kafka-reset"
echo "• Reload nginx:          $0 --reload-nginx nginx"
echo "• One-time seed in prod: $0 --seed-once backend"
