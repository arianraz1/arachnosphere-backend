# Arachnosphere Backend

Spring Boot backend for Arachnosphere, a tarantula-keeping community platform. Built as a portfolio project to demonstrate production-grade backend engineering: authentication, security hardening, rate limiting, observability, and a full feature set around species data, personal collections, and messaging.

## Features

### Core Domain
- **Species catalog**: Taxonomy synced from GBIF, photos and common names synced from iNaturalist (with attribution). Species detail is split into a lightweight detail endpoint and a separate paginated owners endpoint to avoid over-fetching.
- **Collections**: Users track owned tarantulas by life stage (sling/juvenile/subadult/adult) and sex, with per-entry image galleries (S3-backed, ordered, max 6 images per entry). Search matches against both scientific and common name, scoped to the requesting user.
- **Messaging**: Threaded messaging between users with soft-delete (per-user thread visibility) and paginated inbox retrieval.
- **User profiles**: Profile and favorite-tarantula images, `/me` endpoint for the frontend session.

### Authentication & Security
- JWT authentication with cookie-based refresh token rotation
- CSRF protection (double-submit cookie pattern)
- Email verification enforcement — mutating requests (POST/PUT/PATCH/DELETE) are blocked for unverified users, with `/auth/**` and `/admin/**` exempt; unverified accounts are auto-removed after 7 days with a warning notification beforehand
- Password reset flow, rate-limited
- Dual-layer rate limiting (per-IP and per-user) via Bucket4j + Caffeine, with configurable per-endpoint rules
- Security response headers, CORS, structured request logging
- Centralized `ApiError` exception format for consistent frontend error handling

### Infrastructure & Observability
- PostgreSQL with Flyway-managed schema migrations
- Dockerized (app + Postgres via Docker Compose)
- Spring Boot Actuator with Prometheus metrics export
- Structured JSON logging in production (logstash-logback-encoder)
- OpenAPI/Swagger documentation
- Graceful shutdown for zero-downtime deploys
- GitHub Actions CI (build/test on push to master)

## Prerequisites

- Java 25
- Maven 3.8+
- PostgreSQL 17
- Docker & Docker Compose (for containerized deployment)

## Local Development

### 1. Clone the repository

```bash
git clone <repository-url>
cd tarantula-project
```

### 2. Set up environment variables

```bash
cp .env.example .env
# Edit .env with your local development values
```

### 3. Start PostgreSQL with Docker Compose

```bash
docker-compose up -d db
```

### 4. Run the application

```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

## Docker Deployment

### Development

```bash
docker-compose up
```

### Production

1. Create `.env` file with production values:

```bash
cp .env.example .env
# Edit .env with production values
```

2. Build and start containers:

```bash
docker-compose up -d
```

3. Check logs:

```bash
docker-compose logs -f app
```

## Environment Variables

See `.env.example` for all required environment variables.

## API Documentation

Access Swagger UI at:
- Development: `http://localhost:8080/swagger-ui.html`
- Production: `https://api.arachnosphere.com/swagger-ui.html`

OpenAPI JSON spec at:
- `http://localhost:8080/v3/api-docs`

## Health Checks

### Actuator Endpoints

- **Health**: `/actuator/health` (public)
- **Info**: `/actuator/info` (public)
- **Metrics**: `/actuator/metrics` (admin only)
- **Prometheus**: `/actuator/prometheus` (admin only)

### Example Health Check

```bash
curl http://localhost:8080/actuator/health
```

## Production Configuration

### Database Connection Pooling

Configured in `application-prod.properties`:
- Maximum pool size: 20
- Minimum idle: 5
- Connection timeout: 30s
- Max lifetime: 30 minutes

### Cache Configuration

Caffeine cache for rate limiting:
- IP rate limit cache: 10,000 entries, 1 hour expiry
- User rate limit cache: 10,000 entries, 1 hour expiry

### Graceful Shutdown

- Grace period: 30 seconds
- In-flight requests complete before shutdown

## Testing

Run all tests:

```bash
./mvnw test
```

Run with coverage:

```bash
./mvnw test jacoco:report
```

Coverage report: `target/site/jacoco/index.html`

## Monitoring & Observability

### Prometheus Metrics

Enable Prometheus scraping in production:

```yaml
scrape_configs:
  - job_name: 'tarantula-backend'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['api.arachnosphere.com:8080']
```

### Log Aggregation

Production logs are in JSON format for easy parsing by:
- ELK Stack
- Datadog
- Cloud logging services (Render, AWS CloudWatch, etc.)

## Deployment Platforms

### Render

1. Connect repository to Render
2. Create PostgreSQL database
3. Create web service with Dockerfile
4. Add environment variables from `.env.example`
5. Deploy

### AWS

1. Build Docker image: `docker build -t tarantula-backend .`
2. Push to ECR: `docker push <ecr-url>/tarantula-backend`
3. Deploy to ECS/EKS with environment variables
4. Configure ALB health check to `/actuator/health`

### DigitalOcean App Platform

1. Connect repository to DigitalOcean
2. Create app with Dockerfile
3. Add PostgreSQL database
4. Configure environment variables
5. Deploy

## Security Best Practices

1. **Never commit `.env` file** — use `.env.example` as template
2. **Use strong JWT secret** — generate with `openssl rand -base64 32`
3. **Enable HTTPS in production** — set `COOKIE_SECURE=true`
4. **Restrict CORS origin** — set to actual frontend domain
5. **Rotate secrets regularly** — JWT secret, database password, API keys
6. **Monitor rate limits** — adjust based on traffic patterns

## Troubleshooting

### Database Connection Issues

```bash
# Check PostgreSQL is running
docker-compose ps db

# View database logs
docker-compose logs db

# Test database connection
docker-compose exec db pg_isready -U postgres
```

### Application Won't Start

```bash
# Check application logs
docker-compose logs app

# Verify environment variables
docker-compose config

# Check for port conflicts
netstat -tuln | grep 8080
```

### Rate Limiting Too Aggressive

Adjust rate limits in `application.properties` or `application-prod.properties`:

```properties
rate-limit.login.capacity=20
rate-limit.login.refill-period=5m
```

## License

Copyright (c) 2026 Arachnosphere. All rights reserved.

This source code is made available for viewing and evaluation purposes only
(e.g., as part of a portfolio or job application review). No permission is
granted to use, copy, modify, merge, publish, distribute, sublicense, or sell
copies of this software, in whole or in part, without prior written
permission from the copyright holder.

## Support

For issues and questions, please open an issue on GitHub.