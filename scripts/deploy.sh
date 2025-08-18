#!/bin/bash

# Eagle Bank API Kubernetes Deployment Script
# This script deploys the Eagle Bank API to Kubernetes

set -euo pipefail

# Configuration
NAMESPACE="eagle-bank"
IMAGE_TAG="${IMAGE_TAG:-latest}"
ENVIRONMENT="${ENVIRONMENT:-staging}"
KUBECONFIG_FILE="${KUBECONFIG_FILE:-~/.kube/config}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Functions
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

check_prerequisites() {
    log_info "Checking prerequisites..."
    
    # Check if kubectl is installed
    if ! command -v kubectl &> /dev/null; then
        log_error "kubectl is not installed. Please install kubectl first."
        exit 1
    fi
    
    # Check if kustomize is installed
    if ! command -v kustomize &> /dev/null; then
        log_error "kustomize is not installed. Please install kustomize first."
        exit 1
    fi
    
    # Check if kubeconfig is accessible
    if ! kubectl cluster-info &> /dev/null; then
        log_error "Cannot connect to Kubernetes cluster. Check your kubeconfig."
        exit 1
    fi
    
    log_info "Prerequisites check passed"
}

create_namespace() {
    log_info "Creating namespace ${NAMESPACE}..."
    kubectl create namespace ${NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -
}

deploy_secrets() {
    log_info "Deploying secrets..."
    
    # Check if secrets exist, if not create them
    if ! kubectl get secret eagle-bank-secrets -n ${NAMESPACE} &> /dev/null; then
        log_warning "Creating default secrets. Please update them with real values in production!"
        kubectl create secret generic eagle-bank-secrets \
            --from-literal=SPRING_DATASOURCE_USERNAME=eagle_user \
            --from-literal=SPRING_DATASOURCE_PASSWORD=eagle_password \
            --from-literal=SPRING_REDIS_PASSWORD=redis_password \
            --namespace=${NAMESPACE}
    fi
    
    if ! kubectl get secret postgres-secrets -n ${NAMESPACE} &> /dev/null; then
        kubectl create secret generic postgres-secrets \
            --from-literal=username=eagle_user \
            --from-literal=password=eagle_password \
            --namespace=${NAMESPACE}
    fi
    
    if ! kubectl get secret redis-secrets -n ${NAMESPACE} &> /dev/null; then
        kubectl create secret generic redis-secrets \
            --from-literal=password=redis_password \
            --namespace=${NAMESPACE}
    fi
}

deploy_application() {
    log_info "Deploying Eagle Bank API to ${ENVIRONMENT}..."
    
    # Set image tag
    export IMAGE_TAG=${IMAGE_TAG}
    
    # Deploy using kustomize
    case ${ENVIRONMENT} in
        "staging")
            envsubst < k8s/staging/kustomization.yaml > k8s/staging/kustomization-generated.yaml
            kubectl apply -k k8s/staging
            ;;
        "production")
            envsubst < k8s/production/kustomization.yaml > k8s/production/kustomization-generated.yaml
            kubectl apply -k k8s/production
            ;;
        *)
            log_error "Unknown environment: ${ENVIRONMENT}. Use 'staging' or 'production'"
            exit 1
            ;;
    esac
}

wait_for_deployment() {
    log_info "Waiting for deployment to be ready..."
    
    # Wait for deployment rollout
    kubectl rollout status deployment/eagle-bank-api -n ${NAMESPACE} --timeout=600s
    
    # Wait for pods to be ready
    kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=eagle-bank-api -n ${NAMESPACE} --timeout=300s
}

run_health_checks() {
    log_info "Running health checks..."
    
    # Port forward to check health endpoints
    kubectl port-forward service/eagle-bank-api 8080:80 -n ${NAMESPACE} &
    PORT_FORWARD_PID=$!
    
    # Wait for port forward to be ready
    sleep 10
    
    # Check health endpoints
    if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
        log_info "Health check passed"
    else
        log_error "Health check failed"
        kill ${PORT_FORWARD_PID}
        exit 1
    fi
    
    if curl -f http://localhost:8080/actuator/readiness > /dev/null 2>&1; then
        log_info "Readiness check passed"
    else
        log_error "Readiness check failed"
        kill ${PORT_FORWARD_PID}
        exit 1
    fi
    
    # Cleanup
    kill ${PORT_FORWARD_PID}
}

show_deployment_info() {
    log_info "Deployment completed successfully!"
    echo ""
    echo "Deployment Information:"
    echo "======================"
    echo "Environment: ${ENVIRONMENT}"
    echo "Namespace: ${NAMESPACE}"
    echo "Image Tag: ${IMAGE_TAG}"
    echo ""
    echo "Pods:"
    kubectl get pods -n ${NAMESPACE} -l app.kubernetes.io/name=eagle-bank-api
    echo ""
    echo "Services:"
    kubectl get services -n ${NAMESPACE}
    echo ""
    echo "Ingress:"
    kubectl get ingress -n ${NAMESPACE}
}

cleanup_on_error() {
    log_error "Deployment failed. Cleaning up..."
    if [[ -n "${PORT_FORWARD_PID:-}" ]]; then
        kill ${PORT_FORWARD_PID} 2>/dev/null || true
    fi
}

# Main execution
main() {
    trap cleanup_on_error ERR
    
    log_info "Starting Eagle Bank API deployment..."
    log_info "Environment: ${ENVIRONMENT}"
    log_info "Image Tag: ${IMAGE_TAG}"
    log_info "Namespace: ${NAMESPACE}"
    
    check_prerequisites
    create_namespace
    deploy_secrets
    deploy_application
    wait_for_deployment
    run_health_checks
    show_deployment_info
    
    log_info "Deployment completed successfully!"
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -e|--environment)
            ENVIRONMENT="$2"
            shift 2
            ;;
        -t|--tag)
            IMAGE_TAG="$2"
            shift 2
            ;;
        -n|--namespace)
            NAMESPACE="$2"
            shift 2
            ;;
        -h|--help)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  -e, --environment   Deployment environment (staging|production)"
            echo "  -t, --tag          Docker image tag"
            echo "  -n, --namespace    Kubernetes namespace"
            echo "  -h, --help         Show this help message"
            exit 0
            ;;
        *)
            log_error "Unknown option $1"
            exit 1
            ;;
    esac
done

# Run main function
main
