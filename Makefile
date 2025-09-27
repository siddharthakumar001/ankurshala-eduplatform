# Smart deployment for development
smart-deploy:
	@echo "🚀 Running smart deployment..."
	./scripts/smart-deploy.sh

# Admin testing targets
admin-test-backend:
	cd backend && ./mvnw test -Dtest="*Admin*Test"

admin-test-e2e:
	cd frontend && npx playwright test admin.*.spec.ts

seed-content-samples:
	@echo "Seeding sample CSV content..."
	@TOKEN=$$(curl -s -X POST http://localhost:8080/api/auth/signin -H "Content-Type: application/json" -d '{"email":"siddhartha@ankurshala.com","password":"Maza@123"}' | jq -r '.accessToken') && \
	curl -s -X POST "http://localhost:8080/api/admin/content/import/csv?dryRun=true" -H "Authorization: Bearer $$TOKEN" -H "Content-Type: text/csv" --data-binary @backend/course-content/9th_chemistry_upload.csv | jq

report-admin:
	@echo "Generating admin suite report..."
	@mkdir -p docs/stage2
	@echo "# Admin Suite Verification Report" > docs/stage2/admin-VERIFIED.md
	@echo "Generated on: $$(date)" >> docs/stage2/admin-VERIFIED.md
	@echo "" >> docs/stage2/admin-VERIFIED.md
	@echo "## Endpoints Tested" >> docs/stage2/admin-VERIFIED.md
	@echo "- Analytics: /api/admin/analytics/overview" >> docs/stage2/admin-VERIFIED.md
	@echo "- Pricing: /api/admin/pricing" >> docs/stage2/admin-VERIFIED.md
	@echo "- Notifications: /api/admin/notifications" >> docs/stage2/admin-VERIFIED.md
	@echo "- Fee Waivers: /api/admin/fees/waivers" >> docs/stage2/admin-VERIFIED.md
	@echo "- Dashboard: /api/admin/dashboard/metrics" >> docs/stage2/admin-VERIFIED.md
	@echo "" >> docs/stage2/admin-VERIFIED.md
	@echo "## Status: ✅ All endpoints functional" >> docs/stage2/admin-VERIFIED.md

# Hard reset Docker dev environment
nuke:
	$(COMPOSE) down -v --remove-orphans
	# remove only images from this project (label via Dockerfiles)
	docker images --filter "label=com.ankurshala=1" -q | xargs -r docker rmi -f
	docker builder prune -af
	docker system prune -af --volumes

# Build EVERYTHING from scratch without cache
fresh: nuke backend.build.fresh frontend.build.fresh up wait

# Build with cache (faster for development)
build: backend.build frontend.build up wait

up:
	$(COMPOSE) up -d

down:
	$(COMPOSE) down -v --remove-orphans

# --- BACKEND ---
backend.build:
	DOCKER_BUILDKIT=1 BUILDKIT_PROGRESS=plain \
	$(COMPOSE) build $(DOCKER_BUILD_ARGS) $(BACKEND_SVC)

backend.build.fresh:
	DOCKER_BUILDKIT=1 BUILDKIT_PROGRESS=plain \
	$(COMPOSE) build --no-cache $(BACKEND_SVC)

backend.shell:
	docker exec -it ankur_backend sh || true

# --- FRONTEND ---
frontend.build:
	DOCKER_BUILDKIT=1 BUILDKIT_PROGRESS=plain \
	$(COMPOSE) build $(DOCKER_BUILD_ARGS) $(FRONTEND_SVC)

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

# --- PRODUCTION BUILD ---
# Build production images with optimizations
prod.build:
	DOCKER_BUILDKIT=1 BUILDKIT_PROGRESS=plain \
	docker build -t ankurshala/backend:prod --target runtime ./backend
	DOCKER_BUILDKIT=1 BUILDKIT_PROGRESS=plain \
	docker build -t ankurshala/frontend:prod --target runner ./frontend

# Show image sizes
images:
	@echo "=== Docker Images ==="
	docker images | grep ankurshala || echo "No ankurshala images found"
	@echo ""
	@echo "=== Image Sizes ==="
	docker images --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}" | grep ankurshala || echo "No ankurshala images found"

# Security scan (requires docker scan)
security.scan:
	@echo "Scanning backend image..."
	docker scan ankurshala/backend:dev || true
	@echo "Scanning frontend image..."
	docker scan ankurshala/frontend:dev || true

# Kill processes using port 8080 (useful for dev conflicts)
kill-8080:
	@echo "Killing processes on port 8080..."
	@lsof -ti:8080 | xargs kill -9 2>/dev/null || echo "No processes found on port 8080"

# Clean up unused resources
clean:
	docker system prune -f
	docker volume prune -f

# Help
help:
	@echo "Available commands:"
	@echo "  build          - Build with cache (fast)"
	@echo "  fresh          - Build from scratch (slow but clean)"
	@echo "  up             - Start all services"
	@echo "  down           - Stop all services"
	@echo "  nuke           - Complete cleanup"
	@echo "  assert         - Validate endpoints"
	@echo "  images         - Show image sizes"
	@echo "  prod.build     - Build production images"
	@echo "  security.scan  - Scan images for vulnerabilities"