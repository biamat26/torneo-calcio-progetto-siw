# Deploy — Guida e Concetti

## Cos'è il deploy

Il **deploy** (o deployment) è il processo di pubblicare un'applicazione su un server remoto accessibile da internet. Finché l'applicazione gira solo sul tuo computer locale, sei l'unico che può usarla. Con il deploy, chiunque nel mondo può accedervi tramite un URL pubblico.

In sintesi:
- **Locale**: l'app gira su `localhost:8080` — solo tu la vedi
- **Deploy**: l'app gira su `https://torneo-calcio.up.railway.app` — tutti la vedono

---

## Perché fare il deploy

- **Condivisione**: puoi mandare il link al professore senza che installi nulla
- **Dimostrazione**: l'app funziona in un ambiente reale, non solo in sviluppo
- **Valutazione**: per progetti universitari è un bonus che dimostra competenze avanzate
- **Accesso continuo**: l'app è sempre disponibile, anche quando il tuo computer è spento

---

## Come funziona il deploy di una app Spring Boot

Il processo è:

```
1. Il codice viene compilato in un JAR eseguibile (mvn package)
2. Il JAR viene caricato su un server remoto
3. Il server esegue il JAR con Java
4. Il server espone la porta 8080 tramite un URL pubblico
```

Nel nostro caso Railway automatizza tutti questi passi — basta collegare il repository GitHub e Railway fa tutto da solo.

---

## Cos'è Railway

**Railway** è una piattaforma cloud (PaaS — Platform as a Service) che permette di hostare applicazioni web senza gestire manualmente i server.

### Come funziona

1. Colleghi il tuo repository GitHub
2. Railway rileva automaticamente che è un progetto Java/Maven
3. Ad ogni `git push`, Railway rebuilda e rideploya automaticamente
4. Railway genera un URL pubblico per accedere all'app

### Caratteristiche principali

- **Deploy automatico**: ogni push su GitHub triggera un nuovo deploy
- **Zero configurazione server**: non devi installare Java, configurare Nginx, gestire porte
- **Database integrato**: puoi aggiungere PostgreSQL con un click
- **Variabili d'ambiente**: gestione sicura delle credenziali senza hardcodarle nel codice
- **Log in tempo reale**: vedi i log dell'applicazione direttamente dalla dashboard
- **Scaling**: puoi aumentare le risorse con un click

### Piano gratuito

Railway offre **$5 di crediti gratuiti al mese** — sufficiente per un progetto universitario con traffico limitato. Il piano si rinnova ogni mese.

---

## Architettura del nostro deploy

```
GitHub Repository
      │
      │ git push
      ▼
Railway Build Server
  → mvn package → torneo-calcio.jar
      │
      ▼
Railway App Service (Java 21)
  → java -jar torneo-calcio.jar
  → porta 8080 esposta pubblicamente
      │
      │ connessione interna
      ▼
Railway PostgreSQL Service
  → database torneo_calcio
```

I due servizi (App e PostgreSQL) comunicano tramite la rete interna di Railway usando l'hostname `postgres.railway.internal` — più veloce e sicuro di una connessione pubblica.

---

## Variabili d'ambiente

Le credenziali del database non vengono hardcodate nel codice ma passate tramite **variabili d'ambiente**. Questo per due motivi:

1. **Sicurezza**: le credenziali non finiscono su GitHub
2. **Flessibilità**: in locale usi il DB locale, in produzione usi quello di Railway — stesso codice, configurazioni diverse

Nel nostro progetto:

| Variabile | Valore in locale | Valore su Railway |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/torneo_calcio` | `jdbc:postgresql://postgres.railway.internal:5432/railway` |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | `postgres` (Railway) |
| `SPRING_DATASOURCE_PASSWORD` | `postgres` | password generata da Railway |

---

## Profili Spring Boot

Spring Boot supporta **profili** — configurazioni diverse per ambienti diversi.

Nel nostro progetto:

- `application.properties` — configurazione locale, usata di default
- `application-prod.properties` — configurazione produzione, attivata con `SPRING_PROFILES_ACTIVE=prod`

```properties
# application.properties (locale)
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/torneo_calcio}
spring.jpa.hibernate.ddl-auto=create  # ricrea le tabelle ad ogni avvio

# application-prod.properties (Railway)
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.jpa.hibernate.ddl-auto=update  # aggiorna le tabelle senza perdere i dati
```

La sintassi `${VARIABILE:valore_default}` significa: usa la variabile d'ambiente se esiste, altrimenti usa il valore di default.

---

## Differenze tra locale e produzione

| Aspetto | Locale | Produzione |
|---|---|---|
| `ddl-auto` | `create` — ricrea le tabelle | `update` — preserva i dati |
| `sql.init.mode` | `always` — esegue data.sql | `never` — non esegue script |
| `show-sql` | `true` — log delle query | `false` — nessun log SQL |
| `thymeleaf.cache` | `false` — reload immediato | `true` — performance migliore |
| Immagini upload | `src/main/resources/static/` | cartella esterna al JAR |

---

## Comandi usati

```bash
# Login Azure CLI (tentativo iniziale con Azure)
az login
az account show

# Creazione risorse Azure (poi abbandonato per Railway)
az group create --name torneo-calcio-rg --location italynorth
az postgres flexible-server create ...
az appservice plan create ...

# Railway — tutto via interfaccia grafica
# 1. railway.app → New Project → Deploy from GitHub
# 2. Aggiunta PostgreSQL
# 3. Configurazione variabili d'ambiente
# 4. Generazione dominio pubblico
```

---

## URL del progetto deployato

```
https://torneo-calcio-progetto-siw-production.up.railway.app
```

Per riavviarlo dopo una pausa: Railway → Deployments → Redeploy.
EOF