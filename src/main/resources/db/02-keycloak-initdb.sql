-- Create Keycloak database
CREATE DATABASE keycloak OWNER eagle_user;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE keycloak TO eagle_user;