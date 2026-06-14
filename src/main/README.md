# Teoria Spring MVC

## Architettura MVC

MVC è un'architettura a strati che separa le responsabilità in tre componenti:

- **Model** — rappresenta i dati. Nel progetto Spring sono le entità JPA (`User`, `Tournament`, `Team`, ecc.) mappate alle tabelle del database.
- **View** — è l'output mostrato all'utente, di solito un file HTML generato con Thymeleaf.
- **Controller** — riceve le richieste HTTP, interroga il Model per ottenere i dati, e genera la View.

### Flusso di una richiesta

```
Utente → Request HTTP → Controller → Model → Controller → View → Utente
```

1. L'utente fa una richiesta HTTP (GET o POST)
2. Il Controller riceve la richiesta
3. Il Controller interroga il Model per ottenere i dati necessari
4. Il Model restituisce i dati al Controller
5. Il Controller passa i dati alla View tramite `model.addAttribute(...)`
6. La View genera l'HTML che vede l'utente

### Esempio concreto

```java
@GetMapping("/tournaments")
public String showTournaments(Model model) {
    List<Tournament> tournaments = tournamentService.findAll();
    model.addAttribute("tournaments", tournaments);
    return "tournaments/index"; // nome del file HTML
}
```

---

## Strati aggiuntivi di Spring

Spring introduce due strati aggiuntivi rispetto a MVC puro:

- **Service** — intermediario tra Controller e Repository. Contiene la logica di business (es. hashare la password, impostare il ruolo di default).
- **Repository** — esegue le query al database tramite JPA.

### Flusso completo in Spring

```
Request HTTP → Controller → Service → Repository → DB
                   ↑______________|
                   produce la View
```

---

## Model — Le Entità JPA

Il Model contiene le entità che vengono mappate alle tabelle del database PostgreSQL. Ogni classe annotata con `@Entity` diventa una tabella.

### Annotazioni principali

```java
@Entity
public class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private int year;
}
```

- `@Entity` — dichiara la classe come entità JPA, viene creata una tabella corrispondente
- `@Id` — indica la chiave primaria
- `@GeneratedValue(strategy = GenerationType.AUTO)` — delega al DBMS la generazione dell'id
- `@Column(nullable = false, unique = true)` — aggiunge vincoli sulla colonna (opzionale, senza di esso la colonna viene creata con valori di default)

---

## Relazioni tra Entità

Le entità possono avere relazioni tra loro. Ogni relazione ha una **direzione** — il lato owner è quello che gestisce la chiave esterna.

### @OneToOne

Una `Credentials` appartiene a un solo `User` e viceversa.

```java
@OneToOne
private User user;
```

### @OneToMany e @ManyToOne

Una squadra ha tanti giocatori, ma un giocatore appartiene a una sola squadra.

```java
// Team — lato OneToMany (lato inverso)
@OneToMany(mappedBy = "team")
private List<Player> players;

// Player — lato ManyToOne (lato owner, ha la chiave esterna)
@ManyToOne
private Team team;
```

`mappedBy = "team"` indica che la chiave esterna è gestita dalla variabile `team` in `Player`.

### @ManyToMany

Un torneo ha tanti team e un team può partecipare a tanti tornei. JPA crea automaticamente una **tabella di join** per gestire questa relazione.

```java
// Tournament — lato owner
@ManyToMany
@JoinTable(
        name = "tournament_teams",
        joinColumns = @JoinColumn(name = "tournament_id"),
        inverseJoinColumns = @JoinColumn(name = "team_id")
)
private List<Team> teams;

// Team — lato inverso
@ManyToMany(mappedBy = "teams")
private List<Tournament> tournaments;
```

La tabella di join non può essere eliminata — è necessaria perché il database relazionale non può rappresentare direttamente una relazione molti-a-molti.


---

## Strategie di caricamento — EAGER e LAZY

Quando si carica un'entità dal database, JPA deve decidere se caricare subito anche le entità collegate o aspettare.

- **EAGER** — carica subito la relazione insieme all'entità principale.
- **LAZY** — carica la relazione solo quando viene acceduta nel codice. Se la sessione JPA è già chiusa in quel momento, viene lanciata una `LazyInitializationException`.

### Valori di default

| Relazione | Default |
|---|---|
| `@OneToOne` | EAGER |
| `@ManyToOne` | EAGER |
| `@OneToMany` | LAZY |
| `@ManyToMany` | LAZY |

```java
@ManyToMany(fetch = FetchType.EAGER) // override del default
private List<Team> teams;
```

### LazyInitializationException

Con LAZY la sessione JPA deve essere ancora aperta quando si accede alla relazione. La soluzione è annotare il metodo del Service con `@Transactional`, che mantiene la sessione aperta per tutta la durata del metodo.

```java
@Transactional
public Tournament getTournamentWithTeams(Long id) {
    Tournament t = tournamentRepository.findById(id).orElse(null);
    t.getTeams().size(); // funziona perché la sessione è ancora aperta
    return t;
}
```

---

## Cascade

Il Cascade definisce cosa succede alle entità collegate quando si esegue un'operazione sull'entità principale. È utile quando un'entità ha senso solo nel contesto di un'altra.

```java
@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
private Address address;
```

Con `CascadeType.ALL`: se si salva, aggiorna o elimina uno `Studente`, JPA fa automaticamente la stessa operazione sul suo `Address`. In questo caso non serve un `AddressRepository` separato.

### Tipi di Cascade

- `CascadeType.ALL` — propaga tutte le operazioni
- `CascadeType.PERSIST` — propaga solo il salvataggio
- `CascadeType.REMOVE` — propaga solo l'eliminazione
- `CascadeType.MERGE` — propaga solo l'aggiornamento

---

## Repository

Il Repository è un'interfaccia che permette di eseguire operazioni CRUD sul database (Create, Read, Update, Delete). Estende `JpaRepository` che fornisce già i metodi base.

```java
public interface CredentialsRepository extends JpaRepository<Credentials, Long> {
    Credentials findByUsername(String username);
}
```

`JpaRepository<Credentials, Long>` riceve due parametri: il tipo dell'entità (`Credentials`) e il tipo dell'id (`Long`).

### Metodi forniti da JpaRepository

- `save(entity)` — salva o aggiorna
- `findById(id)` — cerca per id, ritorna un `Optional`
- `findAll()` — ritorna tutte le righe
- `delete(entity)` — elimina
- `existsById(id)` — controlla se esiste

### Query derivate dal nome del metodo

Spring Boot interpreta il nome del metodo e genera automaticamente la query SQL corrispondente.

```java
Credentials findByUsername(String username);
// SELECT * FROM credentials WHERE username = ?

List<Team> findByNameContaining(String keyword);
// SELECT * FROM teams WHERE name LIKE %keyword%

boolean existsByUsername(String username);
// SELECT COUNT(*) > 0 FROM credentials WHERE username = ?
```

La parola `find` indica una SELECT, `By` introduce il WHERE, e il nome del campo dopo `By` diventa la condizione.

---

## Service

Il Service è l'intermediario tra il Controller e il Repository. Il Controller non comunica direttamente con il Repository perché spesso una richiesta HTTP richiede più operazioni oltre alla semplice query — ad esempio hashare la password, impostare un ruolo di default, validare dati. Mettere questa logica nel Controller violerebbe la separazione delle responsabilità.

Un altro vantaggio: se più Controller hanno bisogno della stessa operazione, chiamano lo stesso Service — la logica è scritta una volta sola.

```java
@Service
public class CredentialsService {

    private final PasswordEncoder passwordEncoder;
    private final CredentialsRepository credentialsRepository;

    public CredentialsService(PasswordEncoder passwordEncoder,
                              CredentialsRepository credentialsRepository) {
        this.passwordEncoder = passwordEncoder;
        this.credentialsRepository = credentialsRepository;
    }

    @Transactional
    public Credentials saveCredentials(Credentials credentials) {
        credentials.setRole(Credentials.DEFAULT_ROLE);
        credentials.setPassword(passwordEncoder.encode(credentials.getPassword()));
        return credentialsRepository.save(credentials);
    }
}
```

### Responsabilità di ogni strato

- **Controller** — gestisce le richieste HTTP e restituisce la View
- **Service** — contiene la logica di business
- **Repository** — esegue le query al database

---

## @Transactional

`@Transactional` rende un metodo una **transazione ACID**:

- **Atomic** — tutto o niente. Se qualcosa va storto a metà operazione, viene annullato tutto (rollback).
- **Consistent** — il database passa sempre da uno stato valido a un altro stato valido.
- **Isolated** — transazioni concorrenti non si interferiscono tra loro.
- **Durable** — una volta completata, la transazione è permanente anche in caso di crash.

Va messo sul **Service** perché è lì che si definisce cosa costituisce un'operazione logica completa. Sul Repository non serve — JpaRepository gestisce già ogni singola operazione in modo transazionale. Sul Controller non va — non dovrebbe sapere nulla di come vengono gestiti i dati.

### @Transactional(readOnly = true)

Per le operazioni di sola lettura si usa `readOnly = true`. Hibernate ottimizza la query e non tiene il lock sulla risorsa, permettendo a più utenti di leggere contemporaneamente senza bloccarsi.

```java
@Transactional(readOnly = true)
public Tournament findById(Long id) {
    return tournamentRepository.findById(id).orElse(null);
}

@Transactional
public Tournament save(Tournament tournament) {
    return tournamentRepository.save(tournament);
}
```

---

## Controller

Il Controller gestisce le richieste HTTP. Ogni metodo è associato a un path e a un tipo di richiesta (GET o POST).

### @GetMapping

Gestisce le richieste GET — quando l'utente vuole visualizzare una risorsa. Non riceve dati dal client nel body. Restituisce il path del file HTML (la View).

```java
@GetMapping("/tournaments")
public String showTournaments(Model model) {
    List<Tournament> tournaments = tournamentService.findAll();
    model.addAttribute("tournaments", tournaments);
    return "tournaments/index"; // path del file HTML
}
```

### @PostMapping

Gestisce le richieste POST — quando l'utente manda dati al server (es. compilando un form). I dati viaggiano nel body della richiesta, non nell'URL.

```java
@PostMapping("/register")
public String registerUser(...) {
    // elabora i dati e salva
    return "redirect:/";
}
```

### @ModelAttribute

Quando l'utente compila un form e preme submit, i dati arrivano al server come parametri HTTP. `@ModelAttribute` dice a Spring di prendere quei parametri e costruire automaticamente un oggetto.

```java
@PostMapping("/register")
public String registerUser(@ModelAttribute("user") User user) {
    // Spring ha già popolato user.name, user.email ecc. dai campi del form
}
```

### @Valid e BindingResult

`@Valid` attiva la validazione sull'oggetto — controlla le annotazioni come `@NotBlank`, `@Email`, `@Size` ecc. definite nel Model.

`BindingResult` raccoglie tutti gli errori di validazione generati da `@Valid`. Deve stare **sempre subito dopo** l'oggetto che valida — è una regola posizionale.

```java
@PostMapping("/register")
public String registerUser(@Valid @ModelAttribute("user") User user,
                           BindingResult userBindingResult,        // errori di user
                           @Valid @ModelAttribute("credentials") Credentials credentials,
                           BindingResult credentialsBindingResult) { // errori di credentials

    if (!userBindingResult.hasErrors() && !credentialsBindingResult.hasErrors()) {
        credentialsService.saveCredentials(credentials);
        return "redirect:/";
    }
    return "authentication/registerUser"; // torna al form mostrando gli errori
}
```

---

## Thymeleaf

Thymeleaf è il motore di template che permette all'HTML di usare i dati messi nel model dal Controller.

### th:object

Dichiara l'oggetto di riferimento per il form. Da quel momento i campi dell'oggetto si referenziano con `*{...}`.

```html
<form th:action="@{/register}" method="POST" th:object="${user}">
```

### th:field

Collega un input HTML a un campo specifico dell'oggetto dichiarato in `th:object`. Genera automaticamente `id`, `name` e `value`.

```html
<input type="text" th:field="*{name}">    <!-- si riferisce a user.name -->
<input type="email" th:field="*{email}">  <!-- si riferisce a user.email -->
```

### th:action e th:href

Entrambi usano `@{...}` per costruire i path URL, ma su tag diversi:

```html
<form th:action="@{/register}">   <!-- dove mandare i dati del form -->
<a th:href="@{/tournaments}">     <!-- dove porta il link -->
```

### th:if e th:unless

Mostrano o nascondono elementi condizionalmente. `th:unless` è la negazione di `th:if`.

```html
<div th:if="${userDetails}">Sei loggato come <span th:text="${userDetails.username}"></span></div>
<div th:unless="${userDetails}"><a th:href="@{/login}">Login</a></div>
```

### th:each

Itera su una lista — equivalente a un foreach.

```html
<tr th:each="team : ${teams}">
    <td th:text="${team.name}">Nome</td>
</tr>
```

### Mostrare errori di validazione

```html
<input type="text" th:field="*{name}">
<div th:if="${#fields.hasErrors('name')}" th:errors="*{name}" class="error"></div>
```

`#fields.hasErrors('name')` controlla se ci sono errori sul campo `name`. `th:errors` mostra il messaggio di errore.

---

## Spring Security

Spring Security gestisce due aspetti distinti:

- **Autenticazione** — verifica che l'utente sia chi dice di essere (username e password corretti)
- **Autorizzazione** — verifica cosa può fare l'utente in base al suo ruolo (USER, ADMIN ecc.)

La configurazione è centralizzata in una classe annotata con `@Configuration` e `@EnableWebSecurity`.

```java
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    ...
}
```

---

### PasswordEncoder — BCrypt

Le password non si salvano mai in chiaro nel database. Se il database venisse compromesso, tutte le password degli utenti sarebbero esposte — e dato che molti utenti riusano la stessa password su più siti, il danno sarebbe enorme.

BCrypt è l'algoritmo di hashing usato da Spring Security. È one-way — non si può risalire alla password originale dall'hash. Quando l'utente fa login, Spring ricalcola l'hash della password inserita e lo confronta con quello salvato nel database.

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

Nel Service, prima di salvare le credenziali:

```java
credentials.setPassword(passwordEncoder.encode(credentials.getPassword()));
```

---

### UserDetailsService

Spring Security ha bisogno di due informazioni per fare il login: le credenziali (username, password, account abilitato) e il ruolo dell'utente. `JdbcUserDetailsManager` permette di specificare le query per recuperare queste informazioni dalla propria tabella del database.

```java
@Bean
public UserDetailsService userDetailsService() {
    JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);

    // query per recuperare username, password e stato account
    manager.setUsersByUsernameQuery(
        "SELECT username, password, 1 as enabled FROM credentials WHERE username=?");

    // query per recuperare il ruolo dell'utente
    manager.setAuthoritiesByUsernameQuery(
        "SELECT username, role FROM credentials WHERE username=?");

    return manager;
}
```

`1 as enabled` è un valore fisso — Spring Security si aspetta un campo `enabled` ma nella tabella non esiste, quindi si manda sempre `1` (true) per indicare che tutti gli account sono abilitati.

---

### SecurityFilterChain

Definisce le regole di accesso per ogni path dell'applicazione.

```java
@Bean
protected SecurityFilterChain configure(HttpSecurity httpSecurity) throws Exception {
    httpSecurity.authorizeHttpRequests(authorize -> {
        // accessibili a tutti, anche senza login
        authorize.requestMatchers("/", "/register", "/css/**", "/images/**").permitAll();
        // accessibili solo agli utenti con ruolo ADMIN
        authorize.requestMatchers("/admin/**").hasAnyAuthority("ADMIN");
        // tutto il resto richiede autenticazione
        authorize.anyRequest().authenticated();
    });

    httpSecurity.formLogin(form -> {
        form.loginPage("/login").permitAll();
        form.defaultSuccessUrl("/", true);
        form.failureUrl("/login?error=true");
    });

    httpSecurity.logout(logout -> {
        logout.logoutUrl("/logout");
        logout.logoutSuccessUrl("/");
        logout.invalidateHttpSession(true);
        logout.deleteCookies("JSESSIONID");
        logout.permitAll();
    });

    return httpSecurity.build();
}
```

- `permitAll()` — accessibile a tutti, anche senza login
- `hasAnyAuthority("ADMIN")` — accessibile solo agli utenti con quel ruolo
- `anyRequest().authenticated()` — tutto il resto richiede che l'utente sia loggato. Se non lo è, Spring Security reindirizza automaticamente al login.

---

## GlobalController — @ControllerAdvice

`@ControllerAdvice` definisce un comportamento comune a tutti i Controller. In questo caso serve a rendere disponibile l'utente loggato in tutti i template Thymeleaf, senza doverlo aggiungere manualmente in ogni metodo di ogni Controller.

```java
@ControllerAdvice
public class GlobalController {

    @ModelAttribute("userDetails")
    public UserDetails getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            return (UserDetails) authentication.getPrincipal();
        }
        return null;
    }
}
```

### Come funziona

- `SecurityContextHolder` — Spring Security salva qui le informazioni dell'utente loggato per ogni sessione attiva.
- `getAuthentication()` — recupera le informazioni di autenticazione dell'utente che sta facendo la richiesta in quel momento.
- `AnonymousAuthenticationToken` — se nessun utente è loggato, Spring Security non mette `null` nel context ma un oggetto anonimo. Il controllo `instanceof` distingue un utente loggato da uno anonimo.
- `getPrincipal()` — restituisce l'oggetto `UserDetails` dell'utente loggato, con username, password hashata e ruoli.
- `@ModelAttribute("userDetails")` — prima di ogni richiesta, esegue questo metodo e aggiunge il risultato al model con il nome `userDetails`. Così in tutti i template si può usare `${userDetails}`.

### Utilizzo nel template

```html
<div th:if="${userDetails}">
    <span th:text="${userDetails.username}"></span>
    <form th:action="@{/logout}" method="POST">
        <button type="submit">Logout</button>
    </form>
</div>
<div th:unless="${userDetails}">
    <a th:href="@{/login}">Login</a>
</div>
```

---

## Funzionamento di un Form

Un form è il meccanismo con cui l'utente invia dati al server tramite una richiesta HTTP POST.

### Lato HTML — Thymeleaf

```html
<form th:action="@{/register}" method="POST" th:object="${user}">
    <input type="text" th:field="*{name}">
    <div th:if="${#fields.hasErrors('name')}" th:errors="*{name}" class="error"></div>

    <input type="email" th:field="*{email}">
    <div th:if="${#fields.hasErrors('email')}" th:errors="*{email}" class="error"></div>

    <button type="submit">Registra</button>
</form>
```

- `th:action="@{/register}"` — dove mandare i dati quando si preme submit
- `th:object="${user}"` — l'oggetto di riferimento per il form
- `th:field="*{name}"` — collega l'input al campo `name` dell'oggetto
- `#fields.hasErrors('name')` — controlla se ci sono errori di validazione sul campo `name`
- `th:errors="*{name}"` — mostra il messaggio di errore

### CSRF Token

Thymeleaf con `th:action` aggiunge automaticamente un campo hidden con un token segreto:

```html
<input type="hidden" name="_csrf" value="abc123...">
```

Spring Security verifica che il token sia valido ad ogni POST. Se qualcuno tenta di mandare un form da un altro sito (Cross-Site Request Forgery) non avrà il token corretto e la richiesta viene bloccata.

### Lato Server — il flusso completo

1. L'utente compila il form e preme submit
2. Il browser manda una richiesta POST a `/register` con i dati nel body
3. Spring passa la richiesta al Controller con `@PostMapping("/register")`
4. `@ModelAttribute("user")` costruisce automaticamente l'oggetto `User` dai dati ricevuti
5. `@Valid` controlla le annotazioni di validazione (`@NotBlank`, `@Email` ecc.)
6. Se ci sono errori — `BindingResult` li raccoglie e si torna al form mostrando i messaggi
7. Se non ci sono errori — si salva e si fa `redirect:/`

```java
@PostMapping("/register")
public String registerUser(@Valid @ModelAttribute("user") User user,
                           BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
        return "authentication/registerUser"; // torna al form con gli errori
    }
    userService.save(user);
    return "redirect:/";
}
```

### Personalizzare i messaggi di errore

I messaggi di default sono in inglese e generici. Si personalizzano creando un file `messages.properties` in `src/main/resources`:

```properties
NotBlank.user.name=Il nome non può essere vuoto
Email.user.email=Inserisci un indirizzo email valido
Size.credentials.password=La password deve avere almeno 8 caratteri
```

Il formato è `annotazione.nomeOggetto.nomeCampo`.

---

## API REST e @RestController

Quando si usa React, il server non manda più HTML — manda solo dati in JSON. La differenza in Spring è una sola annotazione.

```java
// risponde con HTML via Thymeleaf
@Controller
public class TournamentController {
    @GetMapping("/tournaments")
    public String showTournaments(Model model) {
        model.addAttribute("tournaments", tournamentService.findAll());
        return "tournaments/index";
    }
}

// risponde con JSON per React
@RestController
public class TournamentRestController {
    @GetMapping("/api/tournaments")
    public List<Tournament> getTournaments() {
        return tournamentService.findAll(); // Spring converte automaticamente in JSON
    }
}
```

`@RestController` converte automaticamente l'oggetto Java restituito in JSON. Non si usa `Model` e non si restituisce il path di un file HTML.

### Chi costruisce l'HTML

- **Thymeleaf** — l'HTML lo costruisce il **server** e lo manda già pronto al browser
- **React** — il server manda solo **dati JSON**, e l'HTML lo costruisce il **browser** con JavaScript

### Coesistenza di Thymeleaf e React

I due possono coesistere nello stesso progetto. Thymeleaf gestisce la maggior parte delle pagine, React gestisce pagine specifiche. Il Controller manda una pagina Thymeleaf con un `<div id="root"></div>` vuoto, e React ci costruisce dentro l'interfaccia.

```
Browser chiede /classifica
    → Spring manda HTML con <div id="root"></div>
    → React parte, chiede i dati a /api/classifica
    → Spring risponde con JSON
    → React costruisce la tabella nel browser
```

---

## React — basi

React è una libreria JavaScript per costruire interfacce utente tramite **componenti** — pezzi di UI riutilizzabili, ognuno con la propria logica.

### Componente

Un componente è una funzione che riceve dati (**props**) e restituisce JSX (sintassi simile all'HTML).

```jsx
function PartitaCard({ squadraCasa, squadraOspite, data }) {
    return (
        <div>
            <p>{squadraCasa}</p>
            <p>{squadraOspite}</p>
            <p>{data}</p>
        </div>
    );
}
```

Si usa come un tag HTML:

```jsx
<PartitaCard squadraCasa="Juventus" squadraOspite="Inter" data="02/06/2026" />
```

### Iterare su una lista — map()

Equivalente di `th:each` in Thymeleaf. `.map()` itera sulla lista e per ogni elemento restituisce un componente. `key` è obbligatorio per le liste.

```jsx
{partite.map(p => (
    <PartitaCard
        key={p.id}
        squadraCasa={p.squadraCasa}
        squadraOspite={p.squadraOspite}
        data={p.data}
    />
))}
```

### useState — lo stato del componente

Lo stato è la memoria del componente. Quando cambia, React ridisegna automaticamente l'interfaccia.

```jsx
const [partite, setPartite] = useState([]);
```

`partite` è la variabile, `setPartite` è la funzione per aggiornarla. Non si modifica mai la variabile direttamente — si usa sempre il setter.

### useEffect — effetti al caricamento

Esegue codice quando il componente viene caricato — tipicamente la chiamata API al server.

```jsx
useEffect(() => {
    fetch("/api/partite")
        .then(res => res.json())
        .then(data => setPartite(data));
}, []);
```

### Componente completo con chiamata API

```jsx
function ListaPartite() {
    const [partite, setPartite] = useState([]);

    useEffect(() => {
        fetch("/api/partite")
            .then(res => res.json())
            .then(data => setPartite(data));
    }, []);

    return (
        <div>
            {partite.map(p => (
                <PartitaCard
                    key={p.id}
                    squadraCasa={p.squadraCasa}
                    squadraOspite={p.squadraOspite}
                    data={p.data}
                />
            ))}
        </div>
    );
}
```

Flusso: il componente si carica con lista vuota → chiede i dati a Spring → Spring risponde con JSON → i dati vengono salvati nello stato → React ridisegna l'interfaccia con i dati reali.