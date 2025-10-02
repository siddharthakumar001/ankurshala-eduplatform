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
AZURE_MODE="false"; CLEANUP_ONLY="false"

# ---- Resource Management Functions ----
check_disk_space() {
    local required_gb="${1:-5}"
    local available_gb=$(df / | awk 'NR==2 {print int($4/1024/1024)}')
    
    log INFO "Checking disk space: ${available_gb}GB available, ${required_gb}GB required"
    
    if [ "$available_gb" -lt "$required_gb" ]; then
        log WARN "Insufficient disk space. Available: ${available_gb}GB, Required: ${required_gb}GB"
        return 1
    fi
    return 0
}

check_memory() {
    local required_gb="${1:-2}"
    local available_gb=$(free -g | awk 'NR==2{print int($7)}')
    
    log INFO "Checking memory: ${available_gb}GB available, ${required_gb}GB required"
    
    if [ "$available_gb" -lt "$required_gb" ]; then
        log WARN "Insufficient memory. Available: ${available_gb}GB, Required: ${required_gb}GB"
        return 1
    fi
    return 0
}

check_inodes() {
    local available_inodes=$(df -i / | awk 'NR==2 {print $4}')
    local required_inodes=100000
    
    log INFO "Checking inodes: ${available_inodes} available, ${required_inodes} required"
    
    if [ "$available_inodes" -lt "$required_inodes" ]; then
        log WARN "Insufficient inodes. Available: ${available_inodes}, Required: ${required_inodes}"
        return 1
    fi
    return 0
}

cleanup_docker_resources() {
    log INFO "Cleaning up Docker resources..."
    
    # Remove unused images (keep last 3 versions)
    local images_to_remove=$(docker images --format "table {{.Repository}}:{{.Tag}}\t{{.CreatedAt}}" | \
        grep -E "(ankurshala|none)" | \
        tail -n +4 | \
        awk '{print $1}' | \
        head -n -3)
    
    if [ -n "$images_to_remove" ]; then
        echo "$images_to_remove" | xargs -r docker rmi || true
        log PASS "Removed old Docker images"
    fi
    
    # Clean up build cache
    docker builder prune -f || true
    
    # Clean up unused containers and networks
    docker system prune -f || true
    
    # Clean up unused volumes (be careful with this)
    docker volume prune -f || true
    
    log PASS "Docker cleanup completed"
}

ensure_resources() {
    log INFO "Ensuring sufficient resources for deployment..."
    
    # Check resources
    if ! check_disk_space 8; then
        log WARN "Disk space insufficient, cleaning up..."
        cleanup_docker_resources
        
        if ! check_disk_space 5; then
            log FAIL "Still insufficient disk space after cleanup"
            exit 1
        fi
    fi
    
    if ! check_memory 2; then
        log WARN "Memory insufficient, attempting cleanup..."
        cleanup_docker_resources
        
        if ! check_memory 1; then
            log FAIL "Still insufficient memory after cleanup"
            exit 1
        fi
    fi
    
    if ! check_inodes; then
        log WARN "Inodes insufficient, cleaning up..."
        cleanup_docker_resources
        
        if ! check_inodes; then
            log FAIL "Still insufficient inodes after cleanup"
            exit 1
        fi
    fi
    
    log PASS "Resource check passed"
}

# ---- Azure Storage Functions ----
pull_from_azure() {
    local service="$1"
    local image_tag="$2"
    
    log INFO "Pulling ${service} image from Azure: ${image_tag}"
    
    # Pull from Azure Container Registry
    docker pull "ankurshala.azurecr.io/${service}:${image_tag}" || {
        log WARN "Failed to pull from Azure, falling back to local build"
        return 1
    }
    
    # Tag for local use
    docker tag "ankurshala.azurecr.io/${service}:${image_tag}" "ankurshala/${service}:${image_tag}"
    
    log PASS "Successfully pulled ${service} from Azure"
    return 0
}

push_to_azure() {
    local service="$1"
    local image_tag="$2"
    
    log INFO "Pushing ${service} image to Azure: ${image_tag}"
    
    # Tag for Azure
    docker tag "ankurshala/${service}:${image_tag}" "ankurshala.azurecr.io/${service}:${image_tag}"
    
    # Push to Azure
    docker push "ankurshala.azurecr.io/${service}:${image_tag}" || {
        log FAIL "Failed to push to Azure Container Registry"
        exit 1
    }
    
    log PASS "Successfully pushed ${service} to Azure"
}

# ---- Usage ----
usage(){ cat <<EOF
Usage: $(basename "$0") [options] [services...]
Services: postgres redis zookeeper kafka mailhog backend frontend nginx
If none given: postgres redis zookeeper kafka mailhog backend frontend nginx
Options:
  --seed-once     Enable demo seed just for this deploy
  --no-build      Skip local builds (recreate containers only)
  --reload-nginx  Reload nginx config if running (no recreate)
  --azure-mode    Use Azure Container Registry for images
  --cleanup-only  Only perform cleanup, don't deploy
  -h, --help      Show this help
EOF
}

# ---- Parse CLI ----
while [[ $# -gt 0 ]]; do
  case "$1" in
    --seed-once) SEED_ONCE="true";;
    --no-build) NO_BUILD="true";;
    --reload-nginx) RELOAD_NGINX="true";;
    --azure-mode) AZURE_MODE="true";;
    --cleanup-only) CLEANUP_ONLY="true";;
    -h|--help) usage; exit 0;;
    postgres|redis|zookeeper|kafka|mailhog|backend|frontend|nginx) SERVICES+=("$1");;
    *) log FAIL "Unknown argument: $1"; usage; exit 1;;
  esac; shift
done
[[ ${#SERVICES[@]} -eq 0 ]] && SERVICES=(postgres redis zookeeper kafka mailhog backend frontend nginx)

log INFO "🚀 Starting AnkurShala Enhanced Production Deployment"
echo "================================================================="

# ---- Cleanup only mode ----
if [[ "$CLEANUP_ONLY" == "true" ]]; then
    ensure_resources
    log PASS "Cleanup completed successfully"
    exit 0
fi

# ---- Env check ----
[[ -f "$ENV_FILE" ]] || { log FAIL "$ENV_FILE not found"; exit 1; }
log INFO "Loading environment from $ENV_FILE ..."
set -a; source "$ENV_FILE"; set +a
REQ=(DB_NAME DB_USERNAME DB_PASSWORD JWT_SECRET REDIS_PASSWORD BANK_ENC_KEY)
MISS=(); for v in "${REQ[@]}"; do [[ -z "${!v:-}" ]] && MISS+=("$v") || log PASS "$v is set (${#v} chars)"; done
[[ ${#MISS[@]} -gt 0 ]] && { log FAIL "Missing env vars: ${MISS[*]}"; exit 1; }
log PASS "All required environment variables are loaded"

# ---- Resource Management ----
ensure_resources

# ---- Compute current SHAs ----
FE_SHA=$(git rev-parse --short HEAD:frontend 2>/dev/null || git rev-parse --short HEAD)
BE_SHA=$(git rev-parse --short HEAD:backend  2>/dev/null || git rev-parse --short HEAD)
export FRONTEND_TAG="prod-${FE_SHA}"
export BACKEND_TAG="prod-${BE_SHA}"

log INFO "Detected SHAs → frontend:$FE_SHA backend:$BE_SHA"

# ---- Read last deployed SHAs ----
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
$explicit_frontend && NEEDS_FE_BUILD="true"
$explicit_backend  && NEEDS_BE_BUILD="true"

[[ "$NO_BUILD" == "true" ]] && { NEEDS_FE_BUILD="false"; NEEDS_BE_BUILD="false"; }

# ---- Enhanced Service Recreation ----
recreate_service() {
  local svc="$1"
  local buildFlags="--pull"
  
  # Check resources before each build
  ensure_resources
  
  if [[ "$AZURE_MODE" == "true" ]]; then
    # Try to pull from Azure first
    case "$svc" in
      frontend) 
        if [[ "$NEEDS_FE_BUILD" == "true" ]]; then
          if ! pull_from_azure "frontend" "$FRONTEND_TAG"; then
            log INFO "Building frontend locally..."
            buildFlags="--pull --no-cache"
            $COMPOSE -f "$COMPOSE_FILE" --env-file "$ENV_FILE" build $buildFlags "$svc"
            push_to_azure "frontend" "$FRONTEND_TAG"
          fi
        else
          log INFO "Skipping frontend build (no changes)"
        fi
        ;;
      backend)
        if [[ "$NEEDS_BE_BUILD" == "true" ]]; then
          if ! pull_from_azure "backend" "$BACKEND_TAG"; then
            log INFO "Building backend locally..."
            buildFlags="--pull --no-cache"
            $COMPOSE -f "$COMPOSE_FILE" --env-file "$ENV_FILE" build $buildFlags "$svc"
            push_to_azure "backend" "$BACKEND_TAG"
          fi
        else
          log INFO "Skipping backend build (no changes)"
        fi
        ;;
    esac
  else
    # Local build mode
    if [[ "$NO_BUILD" != "true" && ( "$svc" == "backend" || "$svc" == "frontend" ) ]]; then
      if [[ "$svc" == "frontend" && "$NEEDS_FE_BUILD" == "true" ]]; then buildFlags="--pull --no-cache"; fi
      if [[ "$svc" == "backend"  && "$NEEDS_BE_BUILD" == "true" ]]; then buildFlags="--pull --no-cache"; fi
      
      log INFO "Building image for service: $svc ($buildFlags)"
      $COMPOSE -f "$COMPOSE_FILE" --env-file "$ENV_FILE" build $buildFlags "$svc"
    else
      log INFO "Skipping local build for: $svc"
    fi
  fi

  log INFO "Recreating: $svc (no deps, force new image)"
  $COMPOSE -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d --no-deps --force-recreate "$svc"

  # Wait for service health
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

# ---- Wait for healthy function ----
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

# ---- Bring infra up ----
log INFO "Bringing base infra up (keeps volumes/data)..."
$COMPOSE -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d postgres redis zookeeper mailhog
wait_healthy "ankurshala_db_prod" 180
wait_healthy "ankurshala_redis_prod" 120
wait_healthy "ankurshala_zookeeper_prod" 120
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

# ---- Final cleanup ----
cleanup_docker_resources

# ---- Status ----
log INFO "Current service status:"
$COMPOSE -f "$COMPOSE_FILE" --env-file "$ENV_FILE" ps
log PASS "🎉 Enhanced deployment completed!"
echo
echo "📋 Tips:"
echo "• Deploy only backend:   $0 backend"
echo "• Deploy only frontend:  $0 frontend"
echo "• Use Azure mode:        $0 --azure-mode"
echo "• Cleanup only:          $0 --cleanup-only"
echo "• Reload nginx:          $0 --reload-nginx nginx"
echo "• One-time seed in prod: $0 --seed-once backend"
echo "• Skip builds:           $0 --no-build backend frontend"
