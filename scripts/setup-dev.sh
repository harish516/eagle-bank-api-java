#!/bin/bash

# Eagle Bank API Development Setup Script

echo "🚀 Setting up Eagle Bank API development environment..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker and try again."
    exit 1
fi

# Check if Docker Compose is available
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose is not installed. Please install Docker Compose and try again."
    exit 1
fi

echo "📦 Starting development services..."

# Start PostgreSQL and Keycloak
docker-compose up -d postgres keycloak

echo "⏳ Waiting for services to be ready..."

# Wait for PostgreSQL to be ready
echo "Waiting for PostgreSQL..."
until docker-compose exec -T postgres pg_isready -U eagle_user -d eagle_bank; do
    echo "PostgreSQL is not ready yet..."
    sleep 2
done
echo "✅ PostgreSQL is ready!"

# Wait for Keycloak to be ready
echo "Waiting for Keycloak..."
until curl -f http://localhost:8180/health/ready > /dev/null 2>&1; do
    echo "Keycloak is not ready yet..."
    sleep 5
done
echo "✅ Keycloak is ready!"

echo "🔧 Setting up Keycloak realm and client..."

# Create Keycloak realm and client (this would require Keycloak Admin API calls)
echo "📝 Please manually configure Keycloak:"
echo "1. Open http://localhost:8180"
echo "2. Login with admin/admin"
echo "3. Create realm 'eagle-bank'"
echo "4. Create client 'eagle-bank-api'"
echo "5. Configure client settings"

echo "🧪 Running tests..."
mvn test

echo "🎉 Setup complete!"
echo ""
echo "📋 Next steps:"
echo "1. Configure Keycloak as described above"
echo "2. Run the application: mvn spring-boot:run"
echo "3. Access the API at http://localhost:8080"
echo "4. Check health at http://localhost:8080/actuator/health" 