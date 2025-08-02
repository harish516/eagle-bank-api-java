# Eagle Bank API Development Setup Script (PowerShell)

Write-Host "🚀 Setting up Eagle Bank API development environment..." -ForegroundColor Green

# Check if Docker is running
try {
    docker info | Out-Null
    Write-Host "✅ Docker is running" -ForegroundColor Green
} catch {
    Write-Host "❌ Docker is not running. Please start Docker and try again." -ForegroundColor Red
    exit 1
}

# Check if Docker Compose is available
if (-not (Get-Command docker-compose -ErrorAction SilentlyContinue)) {
    Write-Host "❌ Docker Compose is not installed. Please install Docker Compose and try again." -ForegroundColor Red
    exit 1
}

Write-Host "📦 Starting development services..." -ForegroundColor Yellow

# Start PostgreSQL and Keycloak
docker-compose up -d postgres keycloak

Write-Host "⏳ Waiting for services to be ready..." -ForegroundColor Yellow

# Wait for PostgreSQL to be ready
Write-Host "Waiting for PostgreSQL..." -ForegroundColor Yellow
do {
    Start-Sleep -Seconds 2
    $postgresReady = docker-compose exec -T postgres pg_isready -U eagle_user -d eagle_bank 2>$null
} while ($LASTEXITCODE -ne 0)
Write-Host "✅ PostgreSQL is ready!" -ForegroundColor Green

# Wait for Keycloak to be ready
Write-Host "Waiting for Keycloak..." -ForegroundColor Yellow
do {
    Start-Sleep -Seconds 5
    try {
        Invoke-WebRequest -Uri "http://localhost:8180/health/ready" -UseBasicParsing | Out-Null
        $keycloakReady = $true
    } catch {
        $keycloakReady = $false
    }
} while (-not $keycloakReady)
Write-Host "✅ Keycloak is ready!" -ForegroundColor Green

Write-Host "🔧 Setting up Keycloak realm and client..." -ForegroundColor Yellow

# Create Keycloak realm and client (this would require Keycloak Admin API calls)
Write-Host "📝 Please manually configure Keycloak:" -ForegroundColor Cyan
Write-Host "1. Open http://localhost:8180" -ForegroundColor White
Write-Host "2. Login with admin/admin" -ForegroundColor White
Write-Host "3. Create realm 'eagle-bank'" -ForegroundColor White
Write-Host "4. Create client 'eagle-bank-api'" -ForegroundColor White
Write-Host "5. Configure client settings" -ForegroundColor White

Write-Host "🧪 Running tests..." -ForegroundColor Yellow
mvn test

Write-Host "🎉 Setup complete!" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Next steps:" -ForegroundColor Cyan
Write-Host "1. Configure Keycloak as described above" -ForegroundColor White
Write-Host "2. Run the application: mvn spring-boot:run" -ForegroundColor White
Write-Host "3. Access the API at http://localhost:8080" -ForegroundColor White
Write-Host "4. Check health at http://localhost:8080/actuator/health" -ForegroundColor White 