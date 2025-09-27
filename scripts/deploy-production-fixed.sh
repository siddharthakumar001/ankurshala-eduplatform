#!/bin/bash
set -Eeuo pipefail

# ---------- Colors ----------
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'
log() { case "$1" in
  INFO) echo -e "${BLUE}ℹ️  $2${NC}";;
  WARN) echo -e "${YELLOW}⚠️  $2${NC}";;
  FAIL) echo -e "${RED}❌ $2${NC}";;
  PASS) echo -e "${GREEN}✅ $2${NC}";;
esac; }

# ---------- Compose detection ----------
if docker compose version >/dev/null 2>&1; then COMPOSE="docker compose";
elif docker-compose version >/dev/null 2>&1; then COMPOSE="docker-compose";
else log FAIL "Neither 'docker compose' nor 'docker-compose' found in PATH."; exit 1; fi

# ---------- Defaults / CLI parsing ----------
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"; cd "$ROOT_DIR"
ENV_FILE=".env-prod"; COMPOSE_FILE="docker-compose.prod.yml"
SEED_ONCE="false"; NO_BUILD="false"; RELOAD_NGINX="false"; SERVICES=()

usage() {
  cat <<EOF
Usage: $(basename "$0") [options] [services...]

Services: postgres redis zookeeper kafka mailhog backend frontend nginx
If none given: postgres redis zookeeper kafka mailhog backend frontend nginx

Options:
  --seed-once     Enable demo seed just for this deploy (prod default OFF)
  --no-build      Skip image builds (recreate containers only)
  --reload-nginx  Reload nginx config if running (no recreate)
  -h, --help      Show this help
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --seed-once) SEED_ONCE="true";;
    --no-build) NO_BUILD="true";;
    --reload-nginx) RELOAD_NGINX="true";;
    -h|--help) usage; exit 0;;
    postgres|redis|zookeeper|kafka|mailhog|backend|frontend|nginx) SERVICES+=("$1");;
    *) log FAIL "Unknown argument: $1"; usage; exit 1;;
  esac; shift
done
[[ ${#SERVICES[@]} -eq 0 ]] && SERVICES=(postgres redis zookeeper kafka mailhog backend frontend nginx)

log INFO "🚀 Starting AnkurShala Production Deployment (incremental & safe)"
echo "================================================================="

# ---------- Env check ----------
[[ -f "$ENV_FILE" ]] || { log FAIL "$ENV_FILE not found"; exit 1; }
log INFO "Loading environment from $ENV_FILE ..."
set -a; source "$ENV_FILE"; set +a

REQ=(DB_NAME DB_USERNAME DB_PASSWORD JWT_SECRET REDIS_PASSWORD BANK_ENC_KEY)
MISS=(); for v in "${REQ[@]}"; do
  if [[ -z "${!v:-}" ]]; then MISS+=("$v"); else log PASS "$v is set (${#v} chars)"; fi
done
[[ ${#MISS[@]} -gt 0 ]] && { log FAIL "Missing env vars: ${MISS[*]}"; exit 1; }
log PASS "All required environment variables are loaded"

# ---------- Helpers ----------
wait_healthy() {
  local name="$1" timeout="${2:-180}"
  log INFO "Waiting for container '$name' (timeout ${timeout}s)..."
  local start now status running; start=$(date +%s)
  while true; do
    if ! docker inspect "$name" >/dev/null 2>&1; then sleep 1; continue; fi
    status=$(docker inspect --format '{{.State.Health.Status}}' "$name" 2>/dev/null || echo "null")
    running=$(docker inspect --format '{{.State.Status}}' "$name" 2>/dev/null || echo "exited")
    # Pass if healthy OR (no healthcheck and running)
    if [[ "$status" == "healthy" ]] || { [[ "$status" == "null" ]] && [[ "$running" == "running" ]]; }; then
      [[ "$status" == "healthy" ]] && log PASS "'$name' is healthy" || log PASS "'$name' is running"
      return 0
    fi
    now=$(date +%s); (( now - start > timeout )) && { log FAIL "Timeout waiting for '$name' (status: $status / $running)"; return 1; }
    sleep 3
  done
}

recreate_service() {
  local svc="$1"
  if [[ "$NO_BUILD" != "true" ]]; then
    log INFO "Building image for service: $svc"
    $COMPOSE -f "$COMPOSE_FILE" --env-file "$ENV_FILE" build "$svc"
  else
    log INFO "Skipping build for: $svc"
  fi
  log INFO "Recreating: $svc (no deps)"
  $COMPOSE -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d --no-deps "$svc"

  case "$svc" in
    postgres)  wait_healthy "ankurshala_db_prod" 180;;
    redis)     wait_healthy "ankurshala_redis_prod" 120;;
    zookeeper) wait_healthy "ankurshala_zookeeper_prod" 120;;
    kafka)     wait_healthy "ankurshala_kafka_prod" 180;;
    mailhog)   wait_healthy "ankurshala_mailhog_prod" 30;;
    backend)   wait_healthy "ankurshala_backend_prod" 240;;
    frontend)  wait_healthy "ankurshala_frontend_prod" 240;;
    nginx)     wait_healthy "ankurshala_nginx_prod" 90;;
  esac
}

reload_nginx() {
  if docker ps --format '{{.Names}}' | grep -q '^ankurshala_nginx_prod$'; then
    log INFO "Reloading nginx config..."
    $COMPOSE -f "$COMPOSE_FILE" --env-file "$ENV_FILE" exec nginx nginx -t || { log FAIL "nginx config test failed"; exit 1; }
    $COMPOSE -f "$COMPOSE_FILE" --env-file "$ENV_FILE" exec nginx nginx -s reload || true
    log PASS "nginx reloaded"
  else
    log WARN "nginx not running; recreating"
    recreate_service nginx
  fi
}

# Optional one-time seed
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

# ---------- Bring infra up (keeps volumes/data) ----------
log INFO "Bringing base infra up (keeps volumes/data)..."
$COMPOSE -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d postgres redis zookeeper kafka mailhog
wait_healthy "ankurshala_db_prod" 180
wait_healthy "ankurshala_redis_prod" 120
wait_healthy "ankurshala_zookeeper_prod" 120
wait_healthy "ankurshala_kafka_prod" 180
wait_healthy "ankurshala_mailhog_prod" 30

# ---------- Deploy requested services ----------
for svc in "${SERVICES[@]}"; do
  case "$svc" in
    postgres|redis|zookeeper|kafka|mailhog) recreate_service "$svc";;
    backend)
      if [[ "$SEED_ONCE" == "true" ]]; then
        log INFO "Recreating backend WITH seed override (one-time)"
        $COMPOSE -f "$COMPOSE_FILE" -f "$SEED_FILE" --env-file "$ENV_FILE" up -d --no-deps backend
      else
        recreate_service backend
      fi
      wait_healthy "ankurshala_backend_prod" 240
      if [[ "$SEED_ONCE" == "true" ]]; then
        log INFO "Checking backend logs for seed completion..."
        docker logs ankurshala_backend_prod --since 3m 2>&1 | grep -E "Demo data seeding|Skipping demo data seeding" || true
        log INFO "Switching backend back to normal env (no seeding next restarts)"
        $COMPOSE -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d --no-deps backend
        wait_healthy "ankurshala_backend_prod" 180
      fi
      ;;
    frontend) recreate_service frontend;;
    nginx)
      if [[ "$RELOAD_NGINX" == "true" ]]; then reload_nginx; else recreate_service nginx; fi
      ;;
    *) log WARN "Unknown service: $svc (skipping)";;
  esac
done

# ---------- Final status ----------
log INFO "Current service status:"
$COMPOSE -f "$COMPOSE_FILE" --env-file "$ENV_FILE" ps

log PASS "🎉 Deployment completed!"
echo
echo "📋 Tips:"
echo "• Deploy only backend:   $0 backend"
echo "• Deploy only frontend:  $0 frontend"
echo "• Reload nginx config:   $0 --reload-nginx nginx"
echo "• One-time seed in prod: $0 --seed-once backend"
echo "• Skip image build:      $0 --no-build backend frontend"
