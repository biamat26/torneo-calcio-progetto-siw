# Pagina Giocatori con React — Guida per l'orale

## Cosa abbiamo fatto e perché

La pagina `/players` è stata riscritta per usare React invece di Thymeleaf per la visualizzazione dei dati. Il resto del sito (gestione admin, form, partite) rimane in Thymeleaf.

---

## Architettura della soluzione

```
Browser
  │
  ├── GET /players
  │     └── PlayerController (Thymeleaf)
  │           └── restituisce list.html (pagina "guscio" con <div id="root">)
  │
  └── GET /api/players        ← chiamata fetch() di React
        └── PlayerRestController (@RestController)
              └── PlayerService.findAll()
                    └── restituisce List<PlayerDTO> come JSON
```

La pagina si carica in due fasi:
1. Thymeleaf serve l'HTML statico con la navbar e un `<div id="root">` vuoto
2. React monta su quel div, chiama `/api/players`, e costruisce la lista dinamicamente

---

## I file modificati/creati

### 1. `PlayerDTO.java` — il Data Transfer Object

**Perché serve?**

Se esponessimo direttamente l'entità `Player` come JSON, Jackson (la libreria che serializza gli oggetti Java in JSON) andrebbe in loop infinito:

```
Player → Team → List<Player> → Player → Team → ...
```

Questo perché `Player` ha una relazione `@ManyToOne` verso `Team`, e `Team` ha una relazione `@OneToMany` verso `List<Player>`.

Il DTO "appiattisce" la relazione: invece di mandare l'oggetto `Team` completo, espone solo `teamName` e `teamId` — i due campi che servono al frontend.

**Perché `role` viene convertito con `.name()`?**

`role` è un enum in Java (es. `Role.GOALKEEPER`). Se lo passassimo direttamente, Jackson lo serializzerebbe come oggetto con campi interni. Con `.name()` diventa la stringa `"GOALKEEPER"`, che React può usare direttamente.

**Dove si trova:** `src/main/java/.../dto/PlayerDTO.java`

Il package `dto` è separato da `controller` — i DTO non sono controller, hanno una responsabilità diversa.

---

### 2. `PlayerRestController.java` — l'endpoint REST

```java
@RestController
public class PlayerRestController {

    @GetMapping("/api/players")
    public List<PlayerDTO> getPlayers() {
        return playerService.findAll()
                .stream()
                .map(PlayerDTO::new)
                .collect(Collectors.toList());
    }
}
```

**`@RestController` vs `@Controller`**

- `@Controller` → il metodo restituisce il nome di un template Thymeleaf
- `@RestController` → il metodo restituisce dati serializzati come JSON (equivale a `@Controller` + `@ResponseBody` su ogni metodo)

**Perché il filtraggio avviene lato client e non lato server?**

Con pochi dati (decine/centinaia di giocatori) non ha senso aggiungere endpoint separati come `/api/players?role=STRIKER&name=Mario` per ogni combinazione di filtri. React riceve tutta la lista una volta e la filtra in memoria ad ogni carattere digitato. È più semplice e sufficientemente efficiente per questo caso d'uso.

Se i dati fossero migliaia, la scelta corretta sarebbe filtrare lato server con query JPA.

**Nota su Security:** l'endpoint `/api/players` deve essere nelle route pubbliche in `SecurityConfiguration` (dentro `permitAll()`), altrimenti Spring Security risponde 403 a React.

---

### 3. `PlayerController.java` — modifica al controller esistente

Il metodo `list()` è stato semplificato:

```java
// PRIMA
@GetMapping("/players")
public String list(Model model) {
    model.addAttribute("players", playerService.findAll());
    return "players/list";
}

// DOPO
@GetMapping("/players")
public String list() {
    return "players/list";
}
```

I dati non vengono più passati da Thymeleaf al template perché ora li carica React tramite l'API. Il template Thymeleaf è diventato una pagina "guscio".

**Perché non usare `playerService.findAll().size()` per il contatore?**

Chiamare `findAll()` solo per contare esegue una query completa che carica tutti gli oggetti in memoria. La soluzione corretta è `playerRepository.count()` che esegue `SELECT COUNT(*) FROM player`. Il contatore nella nostra implementazione viene calcolato da React direttamente sulla lista già ricevuta (`filtered.length`) — zero chiamate aggiuntive al server.

---

### 4. `players/list.html` — il template Thymeleaf + React

Il template fa due cose distinte:

**Parte Thymeleaf** (righe 1-50 circa): gestisce la navbar con login/logout, il pulsante admin, e il `<div id="root">` vuoto. Thymeleaf viene eseguito lato server e produce HTML statico.

**Parte React** (il tag `<script type="text/babel">`): viene eseguita lato client nel browser. Babel standalone transpila il JSX in JavaScript puro al momento del caricamento della pagina.

**Problema tecnico che abbiamo risolto: Thymeleaf + JSX**

Thymeleaf usa un parser HTML chiamato attoparser che analizza l'intero file — incluso il contenuto dei tag `<script>`. Questo causa errori quando il JSX contiene:

- Virgolette doppie dentro attributi: `alt={player.name + " " + player.surname}` → Thymeleaf vede `"` come delimitatore di attributo HTML
- Oggetti stile inline: `style={{gap: '0.75rem'}}` → Thymeleaf interpreta `{{` come la sua sintassi di inline expression

**Soluzione adottata:**
1. `th:inline="none"` sul tag script — disabilita il preprocessing di Thymeleaf per le espressioni `[[...]]`
2. Nessun `style={{...}}` inline nel JSX — tutte le regole di stile sono state spostate in classi CSS nel file `style.css` (`.player-search`, `.player-role-select`)
3. Arrow functions `=>` sostituite con `function()` dove necessario

---

## La struttura del componente React

```
PlayerList                  ← componente principale
  │  stato: players, loading, error, search, role
  │  useEffect: chiama /api/players una volta al mount
  │  filtra: calcola `filtered` ad ogni render
  │
  ├── FilterBar (input + select)
  │
  └── PlayerCard × N        ← un componente per ogni giocatore
```

**Separazione dei componenti:** ogni componente fa una cosa sola. `PlayerCard` sa solo come visualizzare un giocatore. `PlayerList` sa solo come gestire lo stato e il filtraggio. È il principio di singola responsabilità applicato ai componenti React.

**`filtered` non è uno stato:** è una variabile calcolata dentro il componente. React ricalcola tutto ad ogni modifica di `search` o `role`. Non serve `useState` per i risultati filtrati — aggiungere uno stato in più sarebbe un errore.

---

## Domande probabili all'orale

**"Perché hai usato un DTO invece di esporre direttamente l'entità?"**
Per evitare loop di serializzazione JSON causati dalle relazioni bidirezionali JPA, e per non esporre al client dati non necessari (come l'intera struttura delle partite associate alla squadra).

**"Perché il filtraggio avviene lato client?"**
Perché il numero di giocatori è limitato e non giustifica la complessità di endpoint con parametri multipli. React filtra in memoria la lista già ricevuta, senza chiamate aggiuntive al server.

**"Cosa fa `@RestController`?"**
Combina `@Controller` e `@ResponseBody`. Indica a Spring che i metodi del controller restituiscono dati (serializzati da Jackson come JSON) invece di nomi di template.

**"Come funziona `useEffect` con array vuoto `[]`?"**
L'array vuoto come secondo parametro significa che l'effetto viene eseguito una sola volta, al primo render del componente (equivalente a `componentDidMount` nelle classi). Senza quell'array, l'effetto si eseguirebbe ad ogni render.

**"Perché `PlayerController` non passa più i dati al template?"**
Perché il template non li usa più. I dati vengono caricati da React tramite `fetch('/api/players')`. Passarli anche via Thymeleaf sarebbe una query inutile al database.