# Komplexní validátor – web backend

Backend webové verze Komplexního validátoru PSP balíčků (NKP). Gradle multi-modulový projekt v Javě, skládá se ze sedmi REST služeb (WAR, Jersey/JAX-RS, deploy na Tomcat), sdílené knihovny a dvou spustitelných nástrojů.

## Požadavky

- **Java 8** – projekt je nutné buildit i provozovat pod Javou 8 (viz `.java-version`). Od Javy 9 je nekompatibilita v jsonix-schema-compiler: <https://github.com/highsource/jsonix-schema-compiler/issues/81>
  - Výjimka: samotný externí validační engine (KomplexniValidatorCLI.jar) může běžet pod jinou Javou – cesta k JDK se pro něj nastavuje v configu (`*.validator.javaHome`), takže execution job může používat např. JDK 17.
- Gradle wrapper 5.6.4 (součást repa, není třeba instalovat)
- PostgreSQL (databáze pro quota-service, validation-manager-service a user-service)
- Tomcat pro nasazení WAR souborů
- ClamAV (clamd) pro antivirovou kontrolu nahraných balíčků — instalace viz sekce [ClamAV](#clamav); lze vypnout konfigurací

## Moduly

| Modul | Typ | Účel |
|---|---|---|
| `utils` | knihovna | Sdílený kód: `Config` (načítá `~/.komplexni-validator/config.properties`), HTTP klient, typované API klienty na ostatní služby, JWT/OAuth autentizace |
| `upload-service` | WAR | Příjem nahrávaných PSP balíčků (zip) a samostatných XML, kontrola kvót, uložení na filesystem, založení validace |
| `validation-manager-service` | WAR | Centrální evidence validací a stavový automat (READY_FOR_EXTRACTION → EXTRACTING → … → FINISHED/ERROR → ARCHIVED → DELETED), PostgreSQL |
| `job-execution-service` | WAR | Spouštění jobů nad balíčky: extrakce + ClamAV sken, spuštění externího validátoru, archivace, mazání |
| `result-service` | WAR | Poskytování výstupních logů validace (extraction, clamav, execution, validation-log txt/xml) z filesystému |
| `user-service` | WAR | Evidence uživatelů (Google OAuth), role a ověření, PostgreSQL |
| `quota-service` | WAR | Systémové kvóty (limity uploadu, paralelismus jobů, časy do archivace/mazání), PostgreSQL |
| `notification-service` | WAR | E-mailové notifikace uživatelům přes Postmark |
| `planner` | CLI | Plánovač – periodicky (cron) prochází validace a podle kvót spouští joby přes job-execution-service |
| `cli` | CLI (fat JAR) | Pomocné nástroje: generování JWT klíčů pro mezislužební komunikaci, výroba minifikovaných PSP balíčků |

Služby spolu komunikují přes REST (JSON) s autentizací lokálně podepisovaným JWT (servisní identita `service@komplexni-validator`). Upload-service, job-execution-service a result-service navíc sdílejí pracovní adresář `validation-working-dir` na filesystému.

## Build

```bash
./gradlew build
```

Spouštět v top-level projektu **pod Javou 8**. Každý servisní modul vybuildí `<modul>/build/libs/kv-<modul>.war`.

## Distribuce

Build a distribuci obstarávají skripty v `resources/scripts/`. Oba se konfigurují přes proměnné prostředí, buildí jen vybrané moduly a podmnožinu služeb berou jako argumenty (bez argumentů vezmou všechny):

- **`build-and-deploy.sh`** — build a nasazení WAR do `$TOMCAT_HOME/webapps` (povinně `TOMCAT_HOME`, volitelně `SLEEP_BETWEEN_DEPLOYS`):

  ```bash
  TOMCAT_HOME=~/Software/tomcat9 resources/scripts/build-and-deploy.sh quota-service user-service
  ```

- **`build-and-share.sh`** — build a nakopírování WAR do sdílené složky, typicky veřejného Dropboxu (povinně `DROPBOX_INSTALLERS`). Navíc tam nahraje i souhrnný `komplexni-validator-backend.zip` (struktura `dist/kv-*.war`, jen vybrané služby) — vypne se pomocí `SHARE_ZIP=false`:

  ```bash
  DROPBOX_INSTALLERS=~/Dropbox/Public/installers resources/scripts/build-and-share.sh
  ```

Lokální (gitignored) adresář `.local/` může obsahovat stejnojmenné tenké wrappery, které jen nastaví proměnné prostředí a výběr služeb.

## ClamAV

Extrakční job skenuje rozbalené balíčky přes démona clamd, ke kterému se připojuje na `127.0.0.1:3310` (TCP; adresa a port jsou zatím natvrdo v `ExtractionJob.ClamAvHelper`). Sken lze vypnout klíčem `job-execution-service.clamav.enabled=false` (např. pro lokální vývoj); bez běžícího clamd jinak každá validace skončí ve stavu ERROR.

### Instalace na macOS (Homebrew)

```bash
brew install clamav

# konfigurace (brew --prefix: Apple Silicon /opt/homebrew, Intel /usr/local)
cd "$(brew --prefix)/etc/clamav"
sed 's/^Example/#Example/' freshclam.conf.sample > freshclam.conf
{ sed 's/^Example/#Example/' clamd.conf.sample; echo "TCPSocket 3310"; echo "TCPAddr 127.0.0.1"; } > clamd.conf

freshclam                    # stažení virové databáze (stovky MB, chvíli trvá)
brew services start clamav   # spustí clamd jako službu (jednorázově lze i příkazem clamd)
```

### Instalace na Linux (Debian/Ubuntu)

```bash
sudo apt install clamav clamav-daemon   # virovou databázi stahuje služba clamav-freshclam automaticky
```

Pozor: clamav-daemon se na Debianu/Ubuntu spouští přes **systemd socket aktivaci** (`clamav-daemon.socket`) a v tom režimu clamd ignoruje `TCPSocket` v `/etc/clamav/clamd.conf` — od systemd dostane jen unix socket. TCP port se proto povoluje drop-inem socket jednotky:

```bash
sudo mkdir -p /etc/systemd/system/clamav-daemon.socket.d
printf '[Socket]\nListenStream=127.0.0.1:3310\n' | sudo tee /etc/systemd/system/clamav-daemon.socket.d/tcp.conf
sudo systemctl daemon-reload
sudo systemctl restart clamav-daemon.socket clamav-daemon.service
```

(Kdyby clamd neběžel přes socket aktivaci, stačí klasicky přidat `TCPSocket 3310` a `TCPAddr 127.0.0.1` do `/etc/clamav/clamd.conf` a restartovat `clamav-daemon` — volby tam nesmí být duplicitně.)

### Ověření

```bash
echo PING | nc 127.0.0.1 3310   # očekávaná odpověď: PONG
```

Když spojení selže (`Connection refused` v `clamav.log` validace):

- `ss -ltn | grep 3310` — neposlouchá-li nic, clamd nemá zapnuté TCP (viz socket aktivace výše) nebo neběží.
- `systemctl status clamav-daemon` + `journalctl -u clamav-daemon -n 20` — clamd odmítne nastartovat, dokud freshclam nestáhne virovou databázi (`/var/lib/clamav/main.cvd|cld` a `daily.cvd|cld` musí existovat); jednotka může být „enabled", a proces přesto hned po startu spadl.

Pozn.: clamd běží pod vlastním uživatelem (`clamav`, na macOS podle způsobu spuštění), takže musí mít právo číst soubory ve `validation-working-dir` — extrakční job jim proto po rozbalení nastavuje čtení pro všechny.

## Konfigurace a provoz

- Runtime konfigurace všech modulů se čte z **`~/.komplexni-validator/config.properties`**. Šablony:
  - `resources/config.properties` – produkce (hodnoty `CHANGEME` je nutné doplnit)
  - `resources/config-dev-localhost.properties` – lokální vývoj
- Konfigurují se: URL služeb (`<service>.url`), DB přístupy (`<service>.db.*`), cesty k externímu validátoru (`*.validator.{javaHome,jar,configDir,tmpDir}`), vypínač antivirové kontroly (`job-execution-service.clamav.enabled`, default `true`; vyžaduje běžící clamd na `127.0.0.1:3310`), JWT klíče (`jwt.local.*` – vygeneruj přes `cli` akcí `GENERATE_JWT_KEYS`), Google OAuth (`oauth.google.client-id`, `jwt.google.public-keys-file` – stažení klíčů viz `resources/scripts/fetch-google-oauth2-keys.sh`), Postmark (`notification-service.postmark.*`) a `validation-working-dir`.
- Inicializace a migrace databází: `resources/init_db.sql` (vytvoří uživatele `validator` a databáze `kv_quota_service`, `kv_validation_mgr_service`, `kv_user_service`). Skript je idempotentní — opakované spuštění doplní chybějící databáze, tabulky, sloupce i výchozí řádky kvót a existující data nemaže ani nepřepisuje, takže se používá i pro aktualizaci schématu na starších instalacích: `psql -d postgres -f resources/init_db.sql` (jako postgres superuser). JDBC driver: `resources/postgresql-42.7.10.jar`.
- `planner` je potřeba spouštět periodicky (např. cronem), jinak se joby nespouští.
- Root endpoint každé služby zobrazuje název, verzi, uptime a přehled URL ostatních služeb.
