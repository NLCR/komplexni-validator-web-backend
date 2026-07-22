#!/bin/bash
set -e

# Zaloha vsech databazi Komplexniho validatoru pres pg_dump (komprimovany custom format).
# Hodi se napr. pred aktualizaci schematu pres init_db.sql.
#
# Konfigurace pres promenne prostredi:
#   BACKUP_ROOT (volitelne) - korenovy adresar zaloh, default ~/db-backups
#
# Pripojeni k Postgres: kdyz aktualni uzivatel umi na server sam (typicky lokalni vyvoj),
# pouzije se primo; jinak se skript prepne na 'sudo -u postgres' (typicky produkce pod rootem).
#
# Obnoveni jedne databaze ze zalohy:
#   sudo -u postgres pg_restore --clean --if-exists -d kv_validation_mgr_service <soubor>.dump
# Kontrola citelnosti zalohy:
#   pg_restore --list <soubor>.dump | head

BACKUP_ROOT="${BACKUP_ROOT:-$HOME/db-backups}"
BACKUP_DIR="$BACKUP_ROOT/$(date +%F_%H-%M-%S)"

databases=(
"kv_quota_service"
"kv_validation_mgr_service"
"kv_user_service"
)

if psql -d postgres -c "SELECT 1" >/dev/null 2>&1; then
  PG_DUMP=(pg_dump)
else
  PG_DUMP=(sudo -u postgres pg_dump)
fi

mkdir -p "$BACKUP_DIR"

for db in "${databases[@]}"; do
  out="$BACKUP_DIR/$db.dump"
  "${PG_DUMP[@]}" -Fc "$db" > "$out"
  if [ ! -s "$out" ]; then
    echo "ERROR: zaloha databaze $db je prazdna" >&2
    exit 1
  fi
  echo "Backed up $db to $out"
done

echo "Done, backups stored in $BACKUP_DIR"
