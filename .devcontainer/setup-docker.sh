#!/bin/bash
set -e

# Install Docker CLI if not already installed
if ! command -v docker &> /dev/null; then
    echo "Installing Docker CLI..."
    
    # Download and install Docker CLI
    curl -fsSL https://get.docker.com -o get-docker.sh
    sh get-docker.sh --dry-run
    sh get-docker.sh
    
    # Clean up
    rm get-docker.sh
    
    echo "Docker CLI installed successfully"
else
    echo "Docker CLI already installed"
fi

# Verify Docker installation
docker --version

# Update package index
sudo apt-get update

# Install Maven
sudo apt-get install -y maven

# Download and install Docker Compose
sudo curl -SL https://github.com/docker/compose/releases/download/v2.29.2/docker-compose-linux-x86_64 -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Print Maven version to verify installation
mvn --version

sudo usermod -aG docker "$USER"
echo "User $USER added to the docker group. Please log out and log back in for the changes to take effect."