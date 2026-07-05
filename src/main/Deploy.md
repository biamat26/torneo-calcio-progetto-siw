# Deploy su Azure con Docker

Questa guida spiega come containerizzare l'applicazione e deployarla su Azure,
passo per passo. È scritta per essere seguita in ordine — non saltare sezioni.

---

## Panoramica di cosa costruiremo

```
[Il tuo PC]                    [Azure]
    │                              │
    ├── Dockerfile                 ├── Azure Container Registry (ACR)
    ├── docker-compose.yml         │     └── immagine app (torneo-calcio)
    │                              │
    └── mvn package                └── Azure Container Apps
                                         ├── container app (Spring Boot)
                                         └── container db  (PostgreSQL)
```

**Flusso di lavoro:**
1. Costruisci l'immagine Docker in locale e la testi
2. Carichi l'immagine su Azure Container Registry (ACR)
3. Azure Container Apps scarica l'immagine da ACR e la esegue

---

## Fase 1 — Dockerfile ✅

Il Dockerfile dice a Docker come costruire l'immagine della tua app.
Usiamo un **multi-stage build**: due fasi separate nello stesso file.

**Perché multi-stage?**
- Stage 1 (`builder`): usa Maven + JDK per compilare il progetto → produce il JAR
- Stage 2 (`runtime`): usa solo JRE (più leggero) per eseguire il JAR

Il risultato finale non contiene Maven né i sorgenti Java — solo il JAR e Java.
L'immagine pesa ~200MB invece di ~600MB.

**Il file da creare:** `Dockerfile` nella root del progetto (stessa cartella di `pom.xml`)

```dockerfile
# ─── Stage 1: build ───────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copia prima solo pom.xml e scarica le dipendenze.
# Docker mette in cache questo layer — se pom.xml non cambia,
# non riscaricare le dipendenze ad ogni build.
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN ./mvnw dependency:go-offline -B

# Ora copia i sorgenti e compila
COPY src ./src
RUN ./mvnw package -DskipTests -B

# ─── Stage 2: runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copia solo il JAR dallo stage precedente
COPY --from=builder /app/target/*.jar app.jar

# Porta su cui gira Spring Boot
EXPOSE 8080

# Comando di avvio — attiva il profilo "prod"
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
```

**⚠️ Attenzione — IntelliJ genera automaticamente un ENTRYPOINT di default:**
Quando crei un Dockerfile con IntelliJ, aggiunge in fondo:
```dockerfile
#FROM ubuntu:latest
#LABEL authors="matteo"
ENTRYPOINT ["top", "-b"]
```
Queste righe vanno eliminate. Docker usa solo l'ultimo ENTRYPOINT — senza rimuoverle,
il container avvia `top` invece della tua app (nessun errore visibile, app non funziona).

**Nota su `-Dspring.profiles.active=prod`:**
Questo attiva `application-prod.properties` invece di `application.properties`.
In produzione vogliamo: cache Thymeleaf attiva, SQL init controllato, log meno verbosi.

---

## Fase 2 — docker-compose.yml (test in locale) ✅

Prima di andare su Azure, testa tutto in locale con docker-compose.
docker-compose avvia più container insieme e li fa comunicare.

**Il file da creare:** `docker-compose.yml` nella root del progetto

```yaml
services:

  # ── Database PostgreSQL ──────────────────────────────────────────────────────
  db:
    image: postgres:16-alpine
    container_name: torneo-db
    environment:
      POSTGRES_DB: torneo_calcio
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5433:5432"           # 5433 sul tuo PC → 5432 nel container
                              # (5433 per non conflitto con Postgres locale)
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

  # ── Applicazione Spring Boot ─────────────────────────────────────────────────
  app:
    build: .
    container_name: torneo-app
    depends_on:
      db:
        condition: service_healthy    # aspetta che il DB sia pronto
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/torneo_calcio
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
      SQL_INIT_MODE: always           # esegue data.sql al primo avvio
    ports:
      - "8080:8080"

volumes:
  postgres_data:
```

**Nota: `version` è obsoleto** — le versioni recenti di docker-compose non richiedono
il campo `version`. Toglierlo elimina il warning "the attribute version is obsolete".

**Nota su `jdbc:postgresql://db:5432/...`:**
Dentro docker-compose i container si vedono usando il nome del servizio come hostname.
`db` è il nome del servizio PostgreSQL — Docker lo risolve automaticamente.

**Nota su `SQL_INIT_MODE`:**
In `application-prod.properties` la riga deve essere:
```properties
spring.sql.init.mode=${SQL_INIT_MODE:never}
```
Il `${SQL_INIT_MODE:never}` significa: usa la variabile d'ambiente se esiste,
altrimenti usa `never` come default. Così:
- In locale Docker → passi `SQL_INIT_MODE=always` → data.sql viene eseguito
- Su Azure → non passi la variabile → default `never` → data.sql non viene rieseguito

**Comandi utili:**
```bash
# Costruisce l'immagine e avvia i container
docker-compose up --build

# Avvia in background
docker-compose up --build -d

# Ferma e rimuove i container (i dati nel volume restano)
docker-compose down

# Ferma, rimuove container E volumi (reset completo del DB)
docker-compose down -v

# Vedi i log dell'app in tempo reale
docker-compose logs -f app
```

**⚠️ Se non vedi i dati dopo `docker-compose up`:**
1. Verifica che `application-prod.properties` abbia `spring.sql.init.mode=${SQL_INIT_MODE:never}`
2. Verifica che `docker-compose.yml` abbia `SQL_INIT_MODE: always`
3. Esegui `docker-compose down -v` per resettare il volume, poi `docker-compose up --build`

---

## Fase 3 — Azure Container Registry (ACR)

ACR è il registro privato dove carichi la tua immagine Docker.
Azure Container Apps la scarica da lì per eseguirla.

**Prerequisiti:**
- Azure CLI installata (`az` da terminale) — [scarica qui](https://learn.microsoft.com/it-it/cli/azure/install-azure-cli)
- Account Azure attivo

```bash
# Login ad Azure
az login

# Crea un Resource Group (contenitore logico per tutte le risorse)
az group create \
  --name torneo-calcio-rg \
  --location westeurope

# Crea il Container Registry
# Il nome deve essere univoco globalmente (es. aggiungi il tuo cognome)
az acr create \
  --resource-group torneo-calcio-rg \
  --name torneocalcioacr \
  --sku Basic \
  --admin-enabled true

# Login al registry
az acr login --name torneocalcioacr
```

**Costruisci e carica l'immagine:**
```bash
# Costruisci l'immagine con il tag del registry
docker build -t torneocalcioacr.azurecr.io/torneo-calcio:latest .

# Carica l'immagine su ACR
docker push torneocalcioacr.azurecr.io/torneo-calcio:latest

# Verifica che sia arrivata
az acr repository list --name torneocalcioacr --output table
```

---

## Fase 4 — PostgreSQL su Azure

Prima di deployare l'app, serve il database.
Usiamo Azure Database for PostgreSQL (Flexible Server).

```bash
# Crea il server PostgreSQL
az postgres flexible-server create \
  --resource-group torneo-calcio-rg \
  --name torneo-calcio-db \
  --location westeurope \
  --admin-user adminuser \
  --admin-password "ScegliunaPasswordForte123!" \
  --sku-name Standard_B1ms \
  --tier Burstable \
  --version 16 \
  --public-access 0.0.0.0

# Crea il database
az postgres flexible-server db create \
  --resource-group torneo-calcio-rg \
  --server-name torneo-calcio-db \
  --database-name torneo_calcio
```

Annota questi valori — ti servono nella Fase 5:
- **host:** `torneo-calcio-db.postgres.database.azure.com`
- **username:** `adminuser`
- **password:** quella che hai scelto
- **database:** `torneo_calcio`

---

## Fase 5 — Azure Container Apps

Container Apps esegue il container della tua app e lo espone su internet.

```bash
# Installa l'estensione Container Apps (solo la prima volta)
az extension add --name containerapp --upgrade

# Registra i provider necessari
az provider register --namespace Microsoft.App
az provider register --namespace Microsoft.OperationalInsights

# Crea l'ambiente Container Apps
az containerapp env create \
  --name torneo-calcio-env \
  --resource-group torneo-calcio-rg \
  --location westeurope

# Recupera le credenziali del registry
ACR_USERNAME=$(az acr credential show --name torneocalcioacr --query username -o tsv)
ACR_PASSWORD=$(az acr credential show --name torneocalcioacr --query passwords[0].value -o tsv)

# Deploya il container
az containerapp create \
  --name torneo-calcio-app \
  --resource-group torneo-calcio-rg \
  --environment torneo-calcio-env \
  --image torneocalcioacr.azurecr.io/torneo-calcio:latest \
  --registry-server torneocalcioacr.azurecr.io \
  --registry-username $ACR_USERNAME \
  --registry-password $ACR_PASSWORD \
  --target-port 8080 \
  --ingress external \
  --min-replicas 1 \
  --max-replicas 1 \
  --env-vars \
    SPRING_PROFILES_ACTIVE=prod \
    SPRING_DATASOURCE_URL="jdbc:postgresql://torneo-calcio-db.postgres.database.azure.com:5432/torneo_calcio?sslmode=require" \
    SPRING_DATASOURCE_USERNAME=adminuser \
    SPRING_DATASOURCE_PASSWORD="ScegliunaPasswordForte123!" \
    SQL_INIT_MODE=always

# Ottieni l'URL pubblico dell'app
az containerapp show \
  --name torneo-calcio-app \
  --resource-group torneo-calcio-rg \
  --query properties.configuration.ingress.fqdn \
  --output tsv
```

L'ultimo comando stampa l'URL tipo:
`torneo-calcio-app.gentlewater-abc123.westeurope.azurecontainerapps.io`

**⚠️ Dopo il primo deploy funzionante:**
Aggiorna `SQL_INIT_MODE` da `always` a `never` per i deploy successivi —
il DB è già popolato e non va rieseguito `data.sql`.

```bash
az containerapp update \
  --name torneo-calcio-app \
  --resource-group torneo-calcio-rg \
  --set-env-vars SQL_INIT_MODE=never
```

---

## Aggiornare l'app dopo modifiche

```bash
# 1. Ricostruisci e carica la nuova immagine
docker build -t torneocalcioacr.azurecr.io/torneo-calcio:latest .
docker push torneocalcioacr.azurecr.io/torneo-calcio:latest

# 2. Forza il riavvio con la nuova immagine
az containerapp update \
  --name torneo-calcio-app \
  --resource-group torneo-calcio-rg \
  --image torneocalcioacr.azurecr.io/torneo-calcio:latest
```

---

## Problemi comuni

**L'app si avvia ma non trova il database**
Controlla che l'URL JDBC contenga `?sslmode=require` — Azure PostgreSQL richiede SSL.

**`docker-compose up` fallisce con "port already in use"**
Hai PostgreSQL già in esecuzione sulla porta 5432. Nel docker-compose usiamo la 5433
per evitarlo — verifica che 5433 sia libera.

**Il sito si apre ma non ci sono dati**
Verifica che `application-prod.properties` usi `${SQL_INIT_MODE:never}` e che
`docker-compose.yml` passi `SQL_INIT_MODE: always`. Poi `docker-compose down -v`
e `docker-compose up --build`.

**Il JAR non viene trovato nel Dockerfile**
Assicurati che `pom.xml` non abbia `<packaging>war</packaging>` — deve essere jar (default).

---

## Ordine delle operazioni (riepilogo)

```
[✅] 1. Crea Dockerfile nella root del progetto
[✅] 2. Crea docker-compose.yml nella root del progetto
[✅] 3. Testa in locale: docker-compose up --build → http://localhost:8080
[ ] 4. Installa Azure CLI
[ ] 5. az login
[ ] 6. Crea Resource Group e ACR
[ ] 7. Push immagine su ACR
[ ] 8. Crea PostgreSQL su Azure
[ ] 9. Crea Container Apps environment
[ ] 10. Deploya il container
[ ] 11. Verifica l'URL pubblico
[ ] 12. Aggiorna SQL_INIT_MODE=never per i deploy successivi
```

---

## Problemi riscontrati durante il deploy reale

**Region westeurope non disponibile con Azure for Students**
La subscription universitaria ha restrizioni sulle region. Usare `italynorth`:
```bash
--location italynorth
```

**Provider Microsoft.ContainerRegistry non registrato**
```bash
az provider register --namespace Microsoft.ContainerRegistry
az provider show --namespace Microsoft.ContainerRegistry --query registrationState
```

**Errore `passwords[0].value` su zsh**
zsh interpreta le parentesi quadre come glob. Usare le virgolette:
```bash
ACR_PASSWORD=$(az acr credential show --name torneocalcio605936 --query "passwords[0].value" -o tsv)
```

**Immagine arm64 non compatibile con Azure (Mac Apple Silicon)**
I Mac con chip M1/M2/M3 costruiscono immagini `linux/arm64` per default.
Azure Container Apps richiede `linux/amd64`. Specificare sempre la piattaforma:
```bash
docker build --platform linux/amd64 -t torneocalcio605936.azurecr.io/torneo-calcio:latest .
```

**Parametro errato per creare il database**
Il parametro corretto è `--name`, non `--database-name`:
```bash
az postgres flexible-server db create \
  --resource-group torneo-calcio-rg \
  --server-name torneo-calcio-db \
  --name torneo_calcio
```

---

## Ordine delle operazioni (riepilogo aggiornato)

```
[✅] 1.  Crea Dockerfile nella root del progetto
[✅] 2.  Crea docker-compose.yml nella root del progetto
[✅] 3.  Testa in locale: docker-compose up --build → http://localhost:8080
[✅] 4.  Installa Azure CLI
[✅] 5.  az login → seleziona Azure for Students
[✅] 6.  Registra provider: Microsoft.ContainerRegistry, Microsoft.App, Microsoft.OperationalInsights
[✅] 7.  Crea Resource Group in italynorth
[✅] 8.  Crea ACR e fai login
[✅] 9.  Build con --platform linux/amd64 e push su ACR
[✅] 10. Crea PostgreSQL Flexible Server in italynorth
[✅] 11. Crea database torneo_calcio
[✅] 12. Crea Container Apps environment
[✅] 13. Deploya il container
[✅] 14. Verifica URL pubblico
[ ] 15. Aggiorna SQL_INIT_MODE=never per i deploy successivi
```

---

## Aggiornare il codice e ridepoyare

Ogni volta che modifichi il codice, ripeti questi tre comandi:

```bash
# 1. Ricostruisci l'immagine (sempre con --platform linux/amd64 su Mac Apple Silicon)
docker build --platform linux/amd64 -t torneocalcio605936.azurecr.io/torneo-calcio:latest .

# 2. Carica su ACR
docker push torneocalcio605936.azurecr.io/torneo-calcio:latest

# 3. Aggiorna il container
az containerapp update \
  --name torneo-calcio-app \
  --resource-group torneo-calcio-rg \
  --image torneocalcio605936.azurecr.io/torneo-calcio:latest
```

---

## Problemi aggiuntivi riscontrati

**Tabelle non create — `relation "match" does not exist`**
Con `ddl-auto=create-drop` Hibernate crea le tabelle all'avvio e le cancella
allo spegnimento. In produzione usare sempre `ddl-auto=update`.
Se il database è in stato inconsistente, ricrearlo da zero:

```bash
az postgres flexible-server db delete \
  --resource-group torneo-calcio-rg \
  --server-name torneo-calcio-db \
  --name torneo_calcio \
  --yes

az postgres flexible-server db create \
  --resource-group torneo-calcio-rg \
  --server-name torneo-calcio-db \
  --name torneo_calcio
```

Poi forzare il riavvio del container aggiornando una variabile d'ambiente:
```bash
az containerapp update \
  --name torneo-calcio-app \
  --resource-group torneo-calcio-rg \
  --set-env-vars RESTART=1
```

**`az containerapp restart` non esiste**
Il comando restart non è disponibile. Per forzare il riavvio usare `update`
con una variabile d'ambiente qualsiasi come sopra.

**Variabili d'ambiente non visibili nell'output JSON**
Azure nasconde i valori delle variabili d'ambiente per sicurezza — è normale.
Non significa che siano vuote.

---

## Ordine delle operazioni (riepilogo finale)

```
[✅] 1.  Crea Dockerfile nella root del progetto
[✅] 2.  Crea docker-compose.yml nella root del progetto
[✅] 3.  Testa in locale: docker-compose up --build → http://localhost:8080
[✅] 4.  Installa Azure CLI
[✅] 5.  az login → seleziona Azure for Students
[✅] 6.  Registra provider: Microsoft.ContainerRegistry, Microsoft.App, Microsoft.OperationalInsights
[✅] 7.  Crea Resource Group in italynorth
[✅] 8.  Crea ACR e fai login
[✅] 9.  Build con --platform linux/amd64 e push su ACR
[✅] 10. Crea PostgreSQL Flexible Server in italynorth
[✅] 11. Crea database torneo_calcio
[✅] 12. Crea Container Apps environment
[✅] 13. Deploya il container
[✅] 14. Verifica URL pubblico
[✅] 15. App funzionante su Azure
[ ] 16. Dopo l'esame: az group delete --name torneo-calcio-rg --yes
```