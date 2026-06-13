# Account Service - Docker Compose Setup

## Overview
This directory contains Docker and Docker Compose configuration files for running the Account Service application in containers.

## Prerequisites
- Docker (v20.10+)
- Docker Compose (v1.29+)

## Quick Start

### 1. Build and Start the Service
```bash
docker-compose up --build
```

This command will:
- Build the Docker image from the `Dockerfile`
- Start the `accountservice` container on port 8082
- Set up networking and volumes
- Wait for the service to become healthy

### 2. Verify the Service is Running
```bash
# Check health endpoint
curl http://localhost:8082/health

# Or use PowerShell
Invoke-RestMethod http://localhost:8082/health
```

Expected response:
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    ...
  }
}
```

### 3. Use the API

#### Apply a Transaction
```bash
curl -X POST http://localhost:8082/accounts/acc123/transactions \
  -H "Content-Type: application/json" \
  -d '{"eventId":"evt001","amount":100.50}'
```

#### Get Account Balance
```bash
curl http://localhost:8082/accounts/acc123/balance
```

#### Get Account Details with Recent Transactions
```bash
curl http://localhost:8082/accounts/acc123
```

#### Access H2 Database Console
Open in browser: `http://localhost:8082/h2-console`
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: `password`

### 4. View Logs
Logs are available in two ways:

**Live logs (attached to terminal):**
```bash
docker-compose logs -f accountservice
```

**From log volume:**
```bash
# On Linux/Mac
cat ./logs/*.log

# On Windows (PowerShell)
Get-Content ./logs/*.log
```

## Service Configuration

The `docker-compose.yml` file configures:
- **Port**: 8082 (mapped from container port 8082)
- **Database**: H2 in-memory (jdbc:h2:mem:testdb)
- **Health Check**: Every 10 seconds with 3 retries
- **Restart Policy**: Automatic restart on failure
- **Network**: Isolated bridge network for container communication
- **Volumes**: `./logs` directory for application logs

## Environment Variables

All Spring Boot properties are set as environment variables in the compose file:
- `SPRING_APPLICATION_NAME=accountService`
- `SERVER_PORT=8082`
- `SPRING_DATASOURCE_URL=jdbc:h2:mem:testdb`
- `SPRING_H2_CONSOLE_ENABLED=true`
- `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health`
- And others (see `docker-compose.yml`)

To override, either edit `docker-compose.yml` or pass via command line:
```bash
docker-compose run -e SPRING_DATASOURCE_PASSWORD=newpass accountservice
```

## Common Commands

### Start in Background
```bash
docker-compose up -d
```

### Stop the Service
```bash
docker-compose down
```

### Stop and Remove Volumes
```bash
docker-compose down -v
```

### Rebuild Image
```bash
docker-compose build --no-cache
```

### View Container Status
```bash
docker-compose ps
```

### Execute Command in Running Container
```bash
docker-compose exec accountservice sh
```

### Rebuild and Start Fresh
```bash
docker-compose up -d --build --force-recreate
```

## Health Check Details

The container includes a health check that:
- Runs every 10 seconds
- Times out after 5 seconds
- Requires 3 consecutive successes to mark as healthy
- Starts checking after 15 seconds (grace period)

View health status:
```bash
docker-compose ps
```

The `STATUS` column will show:
- `healthy` - Service is running and healthy
- `unhealthy` - Service is running but health check failed
- `starting` - Service starting, grace period active

## Persistent H2 Database (Optional)

To use a file-based H2 database instead of in-memory:

1. Update `docker-compose.yml`:
```yaml
environment:
  - SPRING_DATASOURCE_URL=jdbc:h2:file:/app/data/testdb
```

2. Add volume to service:
```yaml
volumes:
  - ./data:/app/data
  - ./logs:/app/logs
```

3. Rebuild and start:
```bash
docker-compose up --build
```

## Troubleshooting

### Container Won't Start
```bash
# View logs
docker-compose logs accountservice

# Check for port conflict
docker ps
```

### Health Check Failing
```bash
# Check health endpoint directly
docker-compose exec accountservice wget -q -O- http://localhost:8082/health

# View startup logs
docker-compose logs accountservice | tail -50
```

### Permission Issues
```bash
# On Linux/Mac, ensure logs directory is writable
chmod 755 ./logs
```

### Database Connection Issues
Verify environment variables in `docker-compose.yml` match `application.properties`.

## Performance Tuning

For production deployments, consider:

1. **Add Resource Limits** in `docker-compose.yml`:
```yaml
services:
  accountservice:
    deploy:
      resources:
        limits:
          cpus: '1'
          memory: 512M
        reservations:
          cpus: '0.5'
          memory: 256M
```

2. **Use External Persistent Database** instead of H2
3. **Add Load Balancer** for multiple instances
4. **Configure Logging Driver** for centralized logging

## Example Multi-Instance Setup

```yaml
version: '3.9'

services:
  accountservice-1:
    build: .
    ports:
      - "8082:8082"
    # ... rest of config

  accountservice-2:
    build: .
    ports:
      - "8083:8082"
    # ... rest of config

  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
```

## License
Same as parent project.

