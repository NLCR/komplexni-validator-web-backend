# Komplexní validátor – web backend

Backend webové verze Komplexního validátoru PSP balíčků (NKP). Gradle multi-modulový projekt v Javě, skládá se ze sedmi REST služeb (WAR, Jersey/JAX-RS, deploy na Tomcat), sdílené knihovny a dvou spustitelných nástrojů.

## Požadavky

- **Java 8** – projekt je nutné buildit i provozovat pod Javou 8 (viz `.java-version`). Od Javy 9 je nekompatibilita v jsonix-schema-compiler: <https://github.com/highsource/jsonix-schema-compiler/issues/81>
  - Výjimka: samotný externí validační engine (KomplexniValidatorCLI.jar) může běžet pod jinou Javou – cesta k JDK se pro něj nastavuje v configu (`*.validator.javaHome`), takže execution job může používat např. JDK 17.
- Gradle wrapper 5.6.4 (součást repa, není třeba instalovat)
- PostgreSQL (databáze pro quota-service, validation-manager-service a user-service)
- Tomcat pro nasazení WAR souborů
- ClamAV (clamd) pro antivirovou kontrolu nahraných balíčků

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

Aktuální postup po buildu – WAR soubory se nakopírují do `dist/`, zabalí a nahrají do veřejné Dropbox složky:

```bash
cp job-execution-service/build/libs/kv-job-execution-service.war dist
cp user-service/build/libs/kv-user-service.war dist
cp quota-service/build/libs/kv-quota-service.war dist
zip -r komplexni-validator-backend.zip dist

cp komplexni-validator-backend.zip ~/Dropbox/Public/installers
```

(Do zipu se aktuálně balí jen služby, které se právě distribuují; podle potřeby doplň další WAR z `*/build/libs/`.)

Alternativně lze pro nasazení přímo na server použít `resources/scripts/deploy_all.sh`, který provede build a nakopíruje všechny WAR do `$TOMCAT_HOME/webapps`.

## Konfigurace a provoz

- Runtime konfigurace všech modulů se čte z **`~/.komplexni-validator/config.properties`**. Šablony:
  - `resources/config.properties` – produkce (hodnoty `CHANGEME` je nutné doplnit)
  - `resources/config-dev-localhost.properties` – lokální vývoj
- Konfigurují se: URL služeb (`<service>.url`), DB přístupy (`<service>.db.*`), cesty k externímu validátoru (`*.validator.{javaHome,jar,configDir,tmpDir}`), JWT klíče (`jwt.local.*` – vygeneruj přes `cli` akcí `GENERATE_JWT_KEYS`), Google OAuth (`oauth.google.client-id`, `jwt.google.public-keys-file` – stažení klíčů viz `resources/scripts/fetch-google-oauth2-keys.sh`), Postmark (`notification-service.postmark.*`) a `validation-working-dir`.
- Inicializace databází: `resources/init_db.sql` (vytvoří uživatele `validator` a databáze `kv_quota_service`, `kv_validation_mgr_service`, `kv_user_service`). JDBC driver: `resources/postgresql-42.7.10.jar`.
- `planner` je potřeba spouštět periodicky (např. cronem), jinak se joby nespouští.
- Root endpoint každé služby zobrazuje název, verzi, uptime a přehled URL ostatních služeb.
