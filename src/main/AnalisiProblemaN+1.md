# Analisi Sperimentale — Problema N+1

## Cos'è il problema N+1

Il problema N+1 si verifica quando per caricare una lista di N entità,
Hibernate esegue 1 query per la lista principale + N query aggiuntive
per caricare le relazioni di ogni elemento.

Caso che ho testato: caricare la lista dei tornei con le squadre associate.

```
1 query  → SELECT * FROM tournament
N query  → SELECT * FROM tournament_teams WHERE tournament_id = ? (una per ogni torneo)
```

Con 2 tornei = 3 query. Con 100 tornei = 101 query.

---


## Strategie a confronto

### Strategia 1 — LAZY (default)

```java
@ManyToMany
private List<Team> teams; // FetchType.LAZY è il default per @ManyToMany
```

Hibernate carica le squadre **solo quando vengono accedute** — una query per ogni torneo.

**Query eseguite:**
```sql
-- 1. Carica tutti i tornei
SELECT * FROM tournament

-- 2. Per ogni torneo, carica le squadre (N volte)
SELECT t FROM tournament_teams JOIN team WHERE tournament_id = ?
SELECT t FROM tournament_teams JOIN team WHERE tournament_id = ?
-- ... una per ogni torneo
```

**Risultato misurato:** ~24ms con 2 tornei

**Problema:** scala male. Con N tornei → N+1 query.

---

### Strategia 2 — EAGER

```java
@ManyToMany(fetch = FetchType.EAGER)
private List<Team> teams;
```

Hibernate carica le squadre **sempre e subito**, anche quando non servono.

**Risultato misurato:** ~66ms con 2 tornei

**Problema:** è il peggio. Carica tutto anche quando non serve. Con relazioni
annidate (Team ha players, homeMatches, awayMatches...) Hibernate finisce
per caricare mezzo database ad ogni richiesta.

---

### Strategia 3 — JOIN FETCH

```java
// TournamentRepository.java
@Query("SELECT DISTINCT t FROM Tournament t LEFT JOIN FETCH t.teams")
List<Tournament> findAllWithTeams();
```

Hibernate carica tornei e squadre in **una sola query SQL** con JOIN.

**Query eseguita:**
```sql
SELECT DISTINCT t.*, team.*
FROM tournament t
LEFT JOIN tournament_teams tt ON t.id = tt.tournaments_id
LEFT JOIN team ON team.id = tt.teams_id
```

**Risultato misurato:** ~23ms con 2 tornei

**Vantaggio:** sempre 1 query, indipendentemente dal numero di tornei.

---

## Risultati

| Strategia | Tempo (2 tornei) | Numero query | Scala con N tornei |
|---|------------------|---|---|
| LAZY | ~24ms            | 1 + N | ❌ N+1 query |
| EAGER | ~66ms            | 1 + N | ❌ carica tutto inutilmente |
| JOIN FETCH | ~23ms            | 1 | ✅ sempre 1 query |

---

## Analisi

Con soli 2 tornei, LAZY sembra comunque veloce (24ms). Questo è dovuto alla semplicità
dei dati utilizzati (solamente 2 tornei) e non all'efficienza della strategia.

Il problema N+1 si manifesta in produzione quando i dati crescono.
La differenza teorica è netta:

- **LAZY con N tornei** → N+1 query al database
- **JOIN FETCH con N tornei** → sempre 1 query al database

Con 100 tornei, LAZY eseguirebbe 101 query contro 1 sola di JOIN FETCH.
Il tempo di risposta crescerebbe linearmente con LAZY, mentre rimarrebbe
costante con JOIN FETCH.

EAGER è sconsigliato perché carica dati non necessari in ogni contesto,
non solo nella lista tornei. Ogni volta che si carica un `Tournament`
(anche per una pagina che non mostra le squadre), Hibernate carica
comunque tutte le squadre associate.

---

## Conclusione

La strategia scelta per la produzione è **JOIN FETCH** tramite query JPQL
dedicata nel repository. Questa scelta:

- elimina il problema N+1
- carica i dati solo quando servono (non globalmente come EAGER)
- scala correttamente con la crescita dei dati
  EOF