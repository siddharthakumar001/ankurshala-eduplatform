# --- GLOBAL ---
COMPOSE ?= docker-compose -f docker-compose.dev.yml
BACKEND_SVC ?= backend
FRONTEND_SVC ?= frontend

# Hard reset Docker dev environment
nuke:
	$(COMPOSE) down -v --remove-orphans
	# remove only images from this project (label via Dockerfiles)
	docker images --filter "label=com.ankurshala=1" -q | xargs -r docker rmi -f
	docker builder prune -af
	docker system prune -af --volumes

# Build EVERYTHING from scratch without cache
fresh: nuke backend.build.fresh frontend.build.fresh up wait

up:
	$(COMPOSE) up -d

down:
	$(COMPOSE) down -v --remove-orphans

# --- BACKEND ---
backend.build.fresh:
	DOCKER_BUILDKIT=1 BUILDKIT_PROGRESS=plain \
	$(COMPOSE) build --no-cache $(BACKEND_SVC)

backend.shell:
	docker exec -it ankur_backend sh || true

# --- FRONTEND ---
frontend.build.fresh:
	DOCKER_BUILDKIT=1 BUILDKIT_PROGRESS=plain \
	$(COMPOSE) build --no-cache $(FRONTEND_SVC)

frontend.shell:
	docker exec -it ankur_frontend sh || true

# --- HEALTH & ASSERTIONS ---
wait:
	@echo "Waiting for backend health..."
	@for i in $$(seq 1 60); do \
	  curl -fsS http://localhost:8080/api/actuator/health >/dev/null && exit 0 || sleep 1; \
	done; \
	echo "Backend did not become healthy in time" && exit 1

# Validate critical mappings exist at runtime
assert:
	curl -fsS http://localhost:8080/api/actuator/mappings \
	| grep -q '/admin/dashboard/metrics' || (echo "❌ Missing /admin/dashboard/metrics"; exit 1)
	curl -fsS http://localhost:8080/api/actuator/mappings \
	| grep -q '/admin/students' || (echo "❌ Missing /admin/students"; exit 1)
	@echo "✅ Controller mappings present"

# Dev-only DB reset (Postgres)
db.reset:
	@echo "Dropping and recreating public schema (dev only)..."
	docker exec -i ankur_db psql -U ankur -d ankurshala -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"

# Bulk seed demo data (15 students, 15 teachers, 3 admins)
seed-bulk:
	@echo "Seeding 15 students, 15 teachers, 3 admins..."
	@curl -s -X POST http://localhost:8080/api/admin/dev-seed/bulk \
	  -H "Authorization: Bearer $${ADMIN_TOKEN}" \
	  -H "Content-Type: application/json" \
	  -d '{}' | jq .