#!/bin/bash
# Common utilities for pet-clinic scripts
# Source this file: source "$(dirname "${BASH_SOURCE[0]}")/common.sh"

# =============================================================================
# Path Constants
# =============================================================================
readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[1]}")" && pwd)"
readonly PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
readonly CLIENT_DIR="$PROJECT_ROOT/client"
readonly SERVER_DIR="$PROJECT_ROOT/server"

# =============================================================================
# Colors
# =============================================================================
readonly GREEN='\033[0;32m'
readonly RED='\033[0;31m'
readonly BLUE='\033[0;94m'
readonly CYAN='\033[0;96m'
readonly BOLD='\033[1m'
readonly RESET='\033[0m'

# =============================================================================
# Output Helpers
# =============================================================================
section() {
  echo -e "\n${BOLD}${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
  echo -e "${BOLD}  $1${RESET}"
  echo -e "${BOLD}${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}\n"
}

step() { printf "  ${CYAN}▸${RESET} %-50s" "$1"; }
ok() { echo -e "${GREEN}✓${RESET}"; }
fail() { echo -e "${RED}✗${RESET}"; }
info() { echo -e "  ${CYAN}ℹ${RESET} $1"; }

success_banner() {
  echo -e "${GREEN}${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
  echo -e "${GREEN}${BOLD}  ✅ $1${RESET}"
  echo -e "${GREEN}${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
}

# =============================================================================
# Backend Server Management
# =============================================================================
readonly HEALTH_URL="http://localhost:8080/api/v1/public/actuator/health"
readonly BACKEND_TIMEOUT=120
readonly BACKEND_LOG="/tmp/petclinic-backend.log"

BACKEND_PID=""

setup_backend_trap() {
  trap '[ -n "$BACKEND_PID" ] && kill "$BACKEND_PID" 2>/dev/null' EXIT
}

backend_failed() {
  printf "\r  ${CYAN}▸${RESET} %-50s${RED}✗${RESET}\n" "Starting backend"
  echo -e "  ${RED}✗${RESET} $1"
  echo -e "\n  ${RED}Last 30 lines of backend log:${RESET}"
  tail -30 "$BACKEND_LOG" | sed 's/^/  /'
  exit 1
}

wait_for_backend() {
  local chars="⣾⣽⣻⢿⡿⣟⣯⣷" i=0 elapsed=0
  while ! curl -sf "$HEALTH_URL" >/dev/null 2>&1; do
    kill -0 "$BACKEND_PID" 2>/dev/null || backend_failed "Backend process died unexpectedly"
    ((elapsed >= BACKEND_TIMEOUT)) && backend_failed "Backend failed to start within ${BACKEND_TIMEOUT}s"
    printf "\r  ${CYAN}▸${RESET} %-50s${CYAN}%s${RESET}" "Starting backend (${elapsed}s)" "${chars:i++%8:1}"
    sleep 1 && ((elapsed++))
  done
  printf "\r  ${CYAN}▸${RESET} %-50s${GREEN}✓${RESET}\n" "Backend ready (${elapsed}s)"
}

start_backend() {
  cd "$SERVER_DIR"
  ./gradlew bootRun --console=plain > "$BACKEND_LOG" 2>&1 &
  BACKEND_PID=$!
  wait_for_backend
  info "Backend running (PID: $BACKEND_PID)"
}

stop_gradle_daemons() {
  cd "$SERVER_DIR"
  step "Stopping daemons"
  ./gradlew --stop > /dev/null 2>&1
  ok
}
