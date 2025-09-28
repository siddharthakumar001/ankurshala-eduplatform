#!/bin/bash
set -Eeuo pipefail

# ---- Colors + logger ----
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'
log(){ case "$1" in INFO) echo -e "${BLUE}ℹ️  $2${NC}";; WARN) echo -e "${YELLOW}⚠️  $2${NC}";;
FAIL) echo -e "${RED}❌ $2${NC}";; PASS) echo -e "${GREEN}✅ $2${NC}";; esac; }

# ---- Compose detection ----
if docker compose version >/dev/null 2>&1; then COMPOSE="docker compose";
elif docker-compose version >/dev/null 2>&1; then COMPOSE="docker-compose";
else log FAIL "Neither 'docker compose' nor 'docker-compose' found."; exit 1; fi

# ---- Paths / defaults ----
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"; cd "$ROOT_DIR"
ENV_FILE=".env-prod"; COMPOSE_FILE="docker-compose.prod.yml"
STATE_FILE=".deploy-state.json"
SEED_ONCE="false"; NO_BUILD="false"; RELOAD_NGINX="false"; SERVICES=()

usage(){ cat <<EOF
Usage: $(basename "$0") [options] [services...]
Services: postgres redis zookeeper kafka mailhog backend frontend nginx
If none given: postgres redis zookeeper kafka mailhog backend frontend nginx
Options:
  --seed-once     Enable demo seed just for this deploy
  --no-build      Skip local builds (recreate containers only)
  --reload-nginx  Reload nginx config if running (no recreate)
  -h, --help      Show this help
EOF
}

# ---- Parse CLI ----
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

# ---- Env check ----
[[ -f "$ENV_FILE" ]] || { log FAIL "$ENV_FILE not found"; exit 1; }
log INFO "Loading environment from $ENV_FILE ..."
set -a; source "$ENV_FILE"; set +a
REQ=(DB_NAME DB_USERNAME DB_PASSWORD JWT_SECRET REDIS_PASSWORD BANK_ENC_KEY)
MISS=(); for v in "${REQ[@]}"; do [[ -z "${!v:-}" ]] && MISS+=("$v") || log PASS "$v is set (${#v} chars)"; done
[[ ${#MISS[@]} -gt 0 ]] && { log FAIL "Missing env vars: ${MISS[*]}"; exit 1; }
log PASS "All required environment variables are loaded"

# ---- Compute current SHAs (only the subtrees matter) ----
# If the path doesn't exist in git (rare), fallback to repo HEAD
FE_SHA=$(git rev-parse --short HEAD:frontend 2>/dev/null || git rev-parse --short HEAD)
BE_SHA=$(git rev-parse --short HEAD:backend  2>/dev/null || git rev-parse --short HEAD)
export FRONTEND_TAG="prod-${FE_SHA}"
export BACKEND_TAG="prod-${BE_SHA}"

log INFO "Detected SHAs → frontend:$FE_SHA backend:$BE_SHA"

# ---- Read last deployed SHAs from state file ----
LAST_FE=""; LAST_BE=""
if [[ -f "$STATE_FILE" ]]; then
  LAST_FE=$(grep -oE '"frontend"\s*:\s*"[^"]*"' "$STATE_FILE" | awk -F\" '{print $4}' || true)
  LAST_BE=$(grep -oE '"backend"\s*:\s*"[^"]*"'  "$STATE_FILE" | awk -F\" '{print $4}' || true)
fi

NEEDS_FE_BUILD="false"; NEEDS_BE_BUILD="false"
[[ "$FE_SHA" != "$LAST_FE" ]] && NEEDS_FE_BUILD="true"
[[ "$BE_SHA" != "$LAST_BE" ]] && NEEDS_BE_BUILD="true"

# If user asked for a specific service build, respect that
explicit_frontend=false; explicit_backend=false
for s in "${SERVICES[@]}"; do
  [[ "$s" == "frontend" ]] && explicit_frontend=true
  [[ "$s" == "backend" ]]  && explicit_backend=true
done
# If user requested them explicitly, mark as needing build regardless of SHA
$explicit_frontend && NEEDS_FE_BUILD="true"
$explicit_backend  && NEEDS_BE_BUILD="true"

[[ "$NO_BUILD" == "true" ]] && { NEEDS_FE_BUILD="false"; NEEDS_BE_BUILD="false"; }

# ---- Helpers ----
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

recreate_service() {
  local svc="$1"
  local buildFlags="--pull"
  # Rebuild with --no-cache for the changed service to guarantee fresh code
  if [[ "$svc" == "frontend" && "$NEEDS_FE_BUILD" == "true" ]]; then buildFlags="--pull --no-cache"; fi
  if [[ "$svc" == "backend"  && "$NEEDS_BE_BUILD" == "true" ]]; then buildFlags="--pull --no-cache"; fi

  if [[ "$NO_BUILD" != "true" && ( "$svc" == "backend" || "$svc" == "frontend" ) ]]; then
    log INFO "Building image for service: $svc ($buildFlags)"
    $COMPOSE -f "$COMPOSE_FILE" --env-file "$ENV_FILE" build $buildFlags "$svc"
  else
    log INFO "Skipping local build for: $svc"
  fi

  log INFO "Recreating: $svc (no deps, force new image)"
  $COMPOSE -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d --no-deps --force-recreate "$svc"

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

# ---- One-time seed (safe) ----
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

# ---- Bring infra (no data wipe) ----
log INFO "Bringing base infra up (keeps volumes/data)..."
$COMPOSE -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d postgres redis zookeeper kafka mailhog
wait_healthy "ankurshala_db_prod" 180
wait_healthy "ankurshala_redis_prod" 120
wait_healthy "ankurshala_zookeeper_prod" 120
wait_healthy "ankurshala_kafka_prod" 180
wait_healthy "ankurshala_mailhog_prod" 30

# ---- Deploy requested services ----
for svc in "${SERVICES[@]}"; do
  case "$svc" in
    postgres|redis|zookeeper|kafka|mailhog) recreate_service "$svc";;
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

# ---- Write new state if builds happened ----
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

# ---- Status ----
log INFO "Current service status:"
$COMPOSE -f "$COMPOSE_FILE" --env-file "$ENV_FILE" ps
log PASS "🎉 Deployment completed!"
echo
echo "📋 Tips:"
echo "• Deploy only backend:   $0 backend"
echo "• Deploy only frontend:  $0 frontend"
echo "• Reload nginx:          $0 --reload-nginx nginx"
echo "• One-time seed in prod: $0 --seed-once backend"
echo "• Skip builds:           $0 --no-build backend frontend"
