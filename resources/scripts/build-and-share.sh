#!/bin/bash
set -e

# Build vybranych sluzeb a nakopirovani waru do sdilene slozky (Dropbox).
#
# Konfigurace pres promenne prostredi:
#   DROPBOX_INSTALLERS (povinne)  - cilovy adresar, kam se wary nakopiruji
#   SHARE_ZIP          (volitelne) - default true; pri SHARE_ZIP=false se nevytvari a nesdili
#                                    souhrnny komplexni-validator-backend.zip
#
# Sluzby k buildu a sdileni lze predat jako argumenty; bez argumentu se sdili vsechny.
# Priklad: DROPBOX_INSTALLERS=~/Dropbox/Public/installers ./build-and-share.sh quota-service user-service
#
# Pozor: build musi bezet pod Javou 8 (viz README).

PROJECT_HOME="$(cd "$(dirname "$0")/../.." && pwd)"

if [ -z "$DROPBOX_INSTALLERS" ]; then
  echo "ERROR: promenna DROPBOX_INSTALLERS neni nastavena" >&2
  exit 1
fi

SHARE_ZIP="${SHARE_ZIP:-true}"

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

# DROPBOX
for service in "${services[@]}"; do
  cp "$PROJECT_HOME/$service/build/libs/kv-$service.war" "$DROPBOX_INSTALLERS/"
  echo "Copied kv-$service.war to $DROPBOX_INSTALLERS"
done

# ZIP (souhrnny balik se strukturou dist/kv-*.war; obsahuje jen vybrane sluzby)
if [ "$SHARE_ZIP" = "true" ]; then
  zip_workdir="$PROJECT_HOME/build/share"
  zip_name="komplexni-validator-backend.zip"
  rm -rf "$zip_workdir"
  mkdir -p "$zip_workdir/dist"
  for service in "${services[@]}"; do
    cp "$PROJECT_HOME/$service/build/libs/kv-$service.war" "$zip_workdir/dist/"
  done
  (cd "$zip_workdir" && zip -rq "$zip_name" dist)
  cp "$zip_workdir/$zip_name" "$DROPBOX_INSTALLERS/"
  echo "Copied $zip_name (${services[*]}) to $DROPBOX_INSTALLERS"
fi
