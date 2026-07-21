#!/bin/bash
set -e

# Build vybranych sluzeb a deploy na Tomcat.
#
# Konfigurace pres promenne prostredi:
#   TOMCAT_HOME           (povinne)  - domovsky adresar Tomcatu, wary se kopiruji do $TOMCAT_HOME/webapps
#   SLEEP_BETWEEN_DEPLOYS (volitelne) - pauza mezi deployi v sekundach, default 5
#
# Sluzby k buildu a deployi lze predat jako argumenty; bez argumentu se nasadi vsechny.
# Priklad: TOMCAT_HOME=~/Software/tomcat9 ./build-and-deploy.sh quota-service user-service
#
# Pozor: build musi bezet pod Javou 8 (viz README).

PROJECT_HOME="$(cd "$(dirname "$0")/../.." && pwd)"

if [ -z "$TOMCAT_HOME" ]; then
  echo "ERROR: promenna TOMCAT_HOME neni nastavena" >&2
  exit 1
fi

SLEEP_BETWEEN_DEPLOYS="${SLEEP_BETWEEN_DEPLOYS:-5}"

all_services=(
"job-execution-service"
"notification-service"
"quota-service"
"result-service"
"upload-service"
"user-service"
"validation-manager-service"
)

if [ "$#" -gt 0 ]; then
  services=("$@")
else
  services=("${all_services[@]}")
fi

# BUILD (jen vybrane moduly)
cd "$PROJECT_HOME"
build_tasks=()
for service in "${services[@]}"; do
  build_tasks+=(":$service:build")
done
echo "building: ${services[*]}"
./gradlew "${build_tasks[@]}"

# TOMCAT
for service in "${services[@]}"; do
  war_built="$PROJECT_HOME/$service/build/libs/kv-$service.war"
  war_deployed="$TOMCAT_HOME/webapps/kv-$service.war"
  cp "$war_built" "$war_deployed"
  echo "Deployed $war_built to $war_deployed"
  sleep "$SLEEP_BETWEEN_DEPLOYS"
done
