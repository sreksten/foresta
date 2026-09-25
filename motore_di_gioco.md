# Motore di gioco

> Assessment tecnico della logica di gioco pura: `motore`, `motore.modellodati`, `eventi`, `personaggi`, `locazioni`, `missioni`, `oggetti`, `offerte`, `tools`.
> Per la parte grafica/rendering vedi [`motore_grafico.md`](motore_grafico.md). Per una visione d'insieme vedi [`foresta.md`](foresta.md).

## 1. Bootstrap e loop principale

Il motore vive interamente nella classe `Automa` (`motore/Automa.java`, ~1250 righe), unica implementazione dell'interfaccia `ControlloreDiGioco`:

```java
public interface ControlloreDiGioco {
    void inizia();
    void processaComando(Comando azione);
}
```

`Automa` non ha un "game loop" nel senso classico (nessun `while(true)` che aggiorna lo stato a ogni frame): è una **macchina a stati finiti reattiva**, che avanza solo in risposta a due tipi di stimolo:

1. **Comandi del giocatore** — eventi `ComandoDiGioco` pubblicati dalla UI quando l'utente clicca un'icona/azione, consegnati a `processaComando(Comando)`.
2. **Tick di un timer** — quando serve un ritardo (es. l'animazione di intro, l'attesa prima di un attacco nemico), `Automa` chiama `temporizzatore.inizia(millisecondi)`; allo scadere il `Temporizzatore` invoca `tick()` (interfaccia `Temporizzabile`), che internamente chiama `processaComando(Comando.TIMER)` (`Automa.java:159-161`).

Il `Temporizzatore` (interfaccia in `tools/Temporizzatore.java`) è implementato da `TemporizzatoreJ2SE` (`tools/TemporizzatoreJ2SE.java`), che usa uno `ScheduledExecutorService` a thread singolo daemon. È importante notare che il tick viene **rimbalzato sull'Event Dispatch Thread di Swing** (`SwingUtilities.invokeLater`, righe 32-43): un commento nel codice spiega perché — `processaComando()` pubblica eventi via `BusEventi`, che se chiamato fuori dall'EDT li accoda con `invokeLater`, aprendo una finestra in cui lo stato di gioco è già cambiato ma la UI non l'ha ancora saputo (causando flicker, es. un mostro morto ma ancora disegnato vivo per un frame). Conclusione architetturale: **tutta la logica di gioco gira sull'EDT**, non su un thread separato — solo il *timing* dei tick è schedulato fuori.

### La state machine (`Stato`)

`motore/Stato.java` enumera ~40 stati, organizzati per fase di gioco:

- **Pre-partita**: `INTRO`, `PRE_GAME_SELEZIONE_SALVATAGGIO_DA_LEGGERE`, `FILE_DI_SALVATAGGIO_NON_VALIDO`, `PRE_GAME_ATTESA_NOME_PERSONAGGIO`, `PRE_GAME_ATTESA_SESSO_PERSONAGGIO`, `PRE_GAME_ATTESA_CLASSE_PERSONAGGIO`, `INIZIO_GIOCO`. Quest'ultimo segue la creazione del personaggio: mostra gli intermezzi di `MomentoIntermezzo.INIZIO_GIOCO` e passa a `INZIO_LOCAZIONE`. Sta prima del ciclo di gioco perché è lì che partono i controlli delle missioni, le cui notifiche altrimenti comparirebbero durante l'intermezzo d'apertura.
- **Ciclo di gioco**: `INZIO_LOCAZIONE` → (`INTERMEZZO`, se ne scatta qualcuno) → `PREPARAZIONE_LOCAZIONE` → `IN_LOCAZIONE` → (combattimento/incantesimi/inventario/mappa) → `FINE_LOCAZIONE` → `ATTESA_DIREZIONE` → `SCELTA_DIREZIONE` → `SCELTA_PASSI` → di nuovo `INZIO_LOCAZIONE`. `ATTESA_DIREZIONE` ha solo un gestore d'ingresso, che pubblica la domanda "in quale direzione?" con i comandi disponibili (direzioni, pozioni, inventario, mappa, accampamento, salvataggio...) e passa subito a `SCELTA_DIREZIONE`, che attende la risposta; `SCELTA_PASSI` attende poi il numero di passi. I due stati si chiamavano `ATTESA_PASSI` e `IN_CAMMINO`, nomi che non corrispondevano a ciò che attendono, e sono stati rinominati insieme ai loro gestori (`gestisciComandoInStatoSceltaDirezione`/`gestisciComandoInStatoSceltaPassi`). Dalla scelta dei passi il giocatore può tornare alla scelta della direzione con `ANNULLA`: `gestisciComandoInStatoSceltaPassi` riporta l'automa in `ATTESA_DIREZIONE`, lo stesso rientro già usato dopo una pozione, `AIUTO` o la chiusura della mappa. Non c'è nulla da disfare, perché scegliere la direzione memorizza solo il campo `direzione`; le operazioni irreversibili di fine locazione (missioni, azzeramento, stanchezza, turni) stanno in `FINE_LOCAZIONE` e non vengono ripetute.
- **Sotto-stati di combattimento e scelte**: `IN_COMBATTIMENTO`, `SCELTA_AUTOMATICA_PERSONAGGIO`, `SCELTA_PERSONAGGIO_QUALSIASI`, `SCELTA_MANUALE_PERSONAGGIO`, `SCELTA_DESTINATARIO_OGGETTO`, `SCELTA_INCANTESIMO_DA_LANCIARE`, `ATTESA_INCANTESIMO_QUALSIASI`, `INCANTESIMO_SCELTO`, `ATTESA_SI_NO`, `ATTESA_POZIONE_*`, `SCELTA_FORMULANTE_RESURREZIONE`, `SCELTA_BERSAGLIO_RESURREZIONE`, `ESEECUZIONE_RESURREZIONE` (sic). `SCELTA_DESTINATARIO_OGGETTO` decide chi raccoglie l'oggetto di fine locazione: propone i personaggi vivi più `GRUPPO` (l'inventario del gruppo), senza `ANNULLA`; con un solo personaggio vivo lo sceglie da sé. Prima si usava `SCELTA_AUTOMATICA_PERSONAGGIO`, che propone `ANNULLA`: scegliendolo, `Oggetto.prendi` riceveva un comando che non indica un personaggio e `Gruppo.getPersonaggio` andava fuori dalla lista. La resurrezione, proponibile alla scelta della direzione, passa da tre stati: prima si sceglie chi la lancia fra i personaggi vivi con magia sufficiente, poi il personaggio morto da risuscitare, infine la si esegue; ciascuna scelta viene saltata se c'è un solo candidato, e `ANNULLA` riporta ad `ATTESA_DIREZIONE`. In precedenza il lanciatore era sempre il primo personaggio in grado di farlo, e con due o più morti la scelta del bersaglio mandava in errore l'automa: `SCELTA_BERSAGLIO_RESURREZIONE` restava in attesa di un comando senza avere un gestore registrato.
- **Fine partita**: `GIOCO_PERSO`/`GIOCO_PERSO_2`, `GIOCO_VINTO`/`GIOCO_VINTO_2`, `STATISTICHE`, `ATTESA_NOME_PUNTEGGI`, `PUNTEGGI`. Dalle statistiche, se il punteggio entra in classifica si chiede il nome (`ATTESA_NOME_PUNTEGGI`), lo si registra e si passa per `PUNTEGGI`, che torna subito all'intro con `inizia()`; la UI, avvisata da `NotificaMostraPunteggiMigliori`, fa partire quell'intro dalla classifica. Altrimenti `STATISTICHE` chiama direttamente `inizia()` e l'intro riparte dai loghi. La sequenza dell'intro (loghi → pagine della storia → classifica, poi di nuovo i loghi) vive nella UI: `DisplayableCanvas.avviaIntro()` la fa ripartire a ogni `InternoStatoDiGioco(INTRO)`, `avanzaIntro()` la fa avanzare a ogni battito da 5 secondi.
- **Meta**: `INVENTARIO`, `MAPPA`, `SELEZIONE_SALVATAGGIO_DA_SCRIVERE`, `CONFERMA_USCITA`.

Nel costruttore di `Automa` (righe 93-157) ogni `Stato` è mappato a uno o due handler dedicati, distribuiti su **due `EnumMap`**:

```java
private final Map<Stato, Supplier<Esito>> gestoriIngresso;       // ingresso automatico, nessun input reale
private final Map<Stato, Function<Comando, Esito>> gestoriComando; // reazione a un comando reale del giocatore

gestoriComando.put(Stato.IN_LOCAZIONE, this::gestisciComandoInStatoInLocazione);
gestoriIngresso.put(Stato.INZIO_LOCAZIONE, this::entraInStatoInizioLocazione);
// ... 20 entry in gestoriIngresso, 27 in gestoriComando (alcuni stati compaiono in entrambe)
```

`processaComando(Comando)` (righe 221-228) non è il singolo dispatcher che invoca un handler e basta: esegue il primo passo e passa il risultato a `prosegui(Esito)` (righe 234-246), un **ciclo** che continua a chiamare `eseguiPasso(...)` finché lo stato risultante non decide di fermarsi e aspettare un input reale. `processaComando` accetta solo comandi reali (lancia `IllegalArgumentException` su `null`): il significato "`null` = ingresso nello stato" è un dettaglio interno del ciclo. `eseguiPasso` (righe 248-264) sceglie quale delle due mappe consultare in base a se il comando corrente è `null` (ingresso automatico) o reale, e ogni handler:

- legge/valida il `Comando` ricevuto rispetto a ciò che è lecito in quello stato (altrimenti chiama `comandoNonValido`, che pubblica un `InternoErrore`, e se manca del tutto un gestore per lo stato corrente lancia `IllegalStateException`),
- muta direttamente il campo `stato` dell'automa (assegnazione diretta),
- restituisce un `Esito`: `Esito.FERMATI` per fermarsi ad aspettare un comando reale, `Esito.CONTINUA_CON_INGRESSO` (costante, prima era un metodo factory) per far scattare subito la logica di ingresso del nuovo stato, o `Esito.continuaCon(comando)` per inoltrare un comando (reale o risolto automaticamente, es. la scelta automatica del personaggio bersaglio) come se fosse appena arrivato per il nuovo stato,
- pubblica sul bus eventi le conseguenze (una `InternoStatoDiGioco` per far sapere alla UI quale schermata mostrare e quali comandi sono ora disponibili, oppure `Notifica*`/`Richiesta*` più specifiche).

Non esiste una funzione di transizione centralizzata che validi "da quale stato a quale stato è lecito andare": le regole restano distribuite nei singoli metodi `entraInStatoX`/`gestisciComandoInStatoX`. È un design pragmatico ma con superficie di manutenzione ampia (già segnalato dagli stessi commenti dell'autore in testa al file, come lista di `FIXME`/`TODO` aperti: fumetti che attendono chiusura, sistema di aiuto; i due TODO sul nome dei gestori delle locande e sulle "ForestaNews" nella mappa sono stati chiusi, vedi §9, così come i FIXME sulla resurrezione senza scelta del lanciatore e sulla perdita della distinzione frase/paragrafo dopo un caricamento, e il TODO sulle cutscene, vedi §7).

**Correzione recente — ingresso in `IN_LOCAZIONE`**: uscendo da `MAPPA` o `INVENTARIO` aperti durante una locazione, l'automa torna in `IN_LOCAZIONE` con `CONTINUA_CON_INGRESSO`, ma per quello stato non esisteva un gestore d'ingresso: i comandi disponibili restavano quelli della mappa e il giocatore doveva cliccare due volte. Una prima correzione aggiungeva `entraInStatoInLocazione()` richiamando `locazioneCorrente.impostaAzioni(..., null)`, con una copia delle transizioni di `gestisciComandoInStatoInLocazione`. Il problema non era la copia in sé ma il significato di `null`: per le locazioni `impostaAzioni(..., null)` vuol dire "fai avanzare di un passo", non "ripresenta i comandi". In `LocazioneBase`, dopo lo `switch`, ogni chiamata fa trascorrere un turno: danni ed estinzione degli effetti di stato per entrambi i gruppi, con possibile `GIOCO_PERSO` o `FINE_LOCAZIONE`. Chiudere la mappa costava quindi un turno di sanguinamento o veleno. In `Locanda`/`Alchimista` lo stesso `null` avrebbe eseguito il passo successivo della loro macchina a stati (es. servire e far pagare il pasto).

La soluzione adottata separa le due operazioni: `Locazione.ripresentaComandi()` ripubblica i comandi dello stato corrente **senza far avanzare nulla**, e `entraInStatoInLocazione()` (righe 403-406) si limita a chiamarlo e a restituire `FERMATI`, come già fa l'ingresso in `ATTESA_DIREZIONE`. Implementazioni: `LocazioneBase` delega a `impostaComandiPossibili()`; `Citta` ripropone i comandi della piazza (e lancia `IllegalStateException` se non è in piazza, l'unico punto da cui mappa e inventario sono raggiungibili); `Locanda` e `Alchimista` lanciano `IllegalStateException`, perché non offrono né mappa né inventario e il comportamento ereditato pubblicherebbe comandi da locazione comune (combattimento, fuga...) privi di senso. Tornare dalla mappa non può più cambiare lo stato, quindi non ci sono transizioni da duplicare.

### Avanzamento automatico: `Esito` invece di ricorsione su `null`

Fino a una versione precedente di questo assessment, l'automa usava una singola mappa `Stato → Consumer<Comando>` e simulava l'"ingresso in uno stato senza input reale" richiamando **ricorsivamente** `processaComando(null)` da dentro gli stessi handler (38 punti di richiamo), distinguendo i due casi con un `if (comando == null)` scritto a mano in ogni handler che ne aveva bisogno. Il difetto: nessun limite alla profondità di ricorsione (rischio di `StackOverflowError` silenzioso su un ipotetico ciclo tra stati), un contratto solo convenzionale (non imposto dal compilatore), e la stessa verifica duplicata in ogni handler.

Il codice attuale sostituisce questo con il meccanismo `Esito` descritto sopra: **nessun handler richiama più `processaComando` o sé stesso** — si limita a restituire un `Esito`, e un solo ciclo in `processaComando` lo interpreta e decide se continuare. Questo elimina completamente la ricorsione per l'avanzamento di stato (non solo per il caso "ingresso automatico", ma anche per l'inoltro di un comando risolto a un altro stato, es. `SCELTA_AUTOMATICA_PERSONAGGIO` → il personaggio scelto viene inoltrato come comando reale allo stato precedente) e aggiunge una rete di sicurezza esplicita: un contatore (`MAX_TRANSIZIONI_AUTOMATICHE = 100`, riga 45) che fa fallire rumorosamente un'eventuale cascata ciclica tra stati invece di un crash silenzioso.

Fino a poco fa restavano due eccezioni: i gestori del **testo libero** (nome del personaggio e nome per la classifica), che arrivano con l'evento `ComandoInvioTesto` invece che con `ComandoDiGioco`, erano metodi `void` fuori dal ciclo e rientravano con `processaComando(null)` per eseguire l'ingresso del nuovo stato (la prima locazione quando il personaggio è casuale o nascosto, `PUNTEGGI` dopo il nome in classifica). Ora anche loro restituiscono un `Esito`, e `onEventoTestoDisponibile` (riga 178) lo passa a `prosegui`: comandi e testo percorrono lo stesso ciclo, con lo stesso limite di transizioni.

Nel farlo è stato anche corretto un bug preesistente in `gestisciComandoInStatoSelezioneSalvataggioDaScrivere` (riga 813): due `if` indipendenti (non un `if/else`) facevano sì che rispondere "no" alla richiesta di slot di salvataggio innescasse comunque anche un salvataggio spurio (in uno slot "NO" inesistente) e una richiesta di conferma-uscita dal gioco non voluta.

## 2. Il bus eventi — spina dorsale architetturale

`eventi/BusEventi.java` (49 righe) implementa un **publish/subscribe sincrono**, dichiaratamente semplice:

```java
private static final Map<Class<?>, List<Consumer<Object>>> sottoscrittori = new ConcurrentHashMap<>();

public static <T> void iscriviti(Class<T> eventType, Consumer<T> listener) { ... }
public static void pubblica(Object evento) {
    List<Consumer<Object>> list = sottoscrittori.get(evento.getClass());
    if (SwingUtilities.isEventDispatchThread()) {
        list.forEach(c -> c.accept(evento));
    } else {
        SwingUtilities.invokeLater(() -> list.forEach(c -> c.accept(evento)));
    }
}
```

Punti chiave:

- **Dispatch per classe esatta** (`evento.getClass()`, non gerarchia/polimorfismo): iscriversi a una superclasse non intercetta le sottoclassi.
- **Consegna sull'EDT**: se `pubblica()` è chiamato già sull'EDT, esegue subito i listener (sincrono, in-place); altrimenti li accoda con `invokeLater`. Questo garantisce che **tutti gli handler di eventi girino sull'Event Dispatch Thread**, l'unico thread su cui sia motore che UI leggono/scrivono stato condiviso — evitando quasi ogni classe di race condition tipica delle UI Swing multi-thread.
- Sottoscrittori mantenuti in `CopyOnWriteArrayList` (sicuro rispetto a iscrizioni/cancellazioni concorrenti durante l'iterazione).
- Nessuna garanzia d'ordine tra sottoscrittori multipli sullo stesso tipo di evento, nessuna gestione di eccezioni nei listener (un'eccezione in un consumer propaga e interrompe gli altri della lista).

### Le quattro famiglie di eventi

Gli eventi sono organizzati per **direzione e intento della comunicazione**, non per dominio (combattimento, mappa, ecc.) — una scelta di naming che rende immediatamente leggibile *chi parla a chi*:

| Package | Conteggio file | Direzione | Esempio |
| :--- | :---: | :--- | :--- |
| `eventi.comandigiocatore` | 12 | UI → motore | `ComandoDiGioco`, `ComandoAcquistoArtefatto`, `ComandoVisualizzazioneMappa` |
| `eventi.notifiche` | 41 | motore → UI, informativi (fatto compiuto) | `NotificaVariazioneStatisticheePersonaggio`, `NotificaFineGioco`, `NotificaNotizia` |
| `eventi.richieste` | 7 | motore → UI, richieste di input | `RichiestaSelezioneDirezione`, `RichiestaSelezioneSiNo`, `RichiestaTesto` |
| `eventi.interni` | 23 | bidirezionale/tecnico, non user-facing | `InternoStatoDiGioco`, `InternoCaricamentoCompletato`, `InternoCreazioneSprite*` |

Classi base comuni:
- `EventoBase` (`eventi/EventoBase.java`) — UUID generato e `TipoEvento`, usata come radice per eventi "che accadono nel motore".
- `EventoSuPersonaggio` — probabile estensione per eventi che portano un riferimento a `Personaggio` (usata da gran parte delle `Notifica*VariazioneXPersonaggio`).

Il flusso tipico di un'azione giocatore:

```
Click su icona (UI)                     →  BusEventi.pubblica(new ComandoDiGioco(Comando.COMBATTIMENTO))
Automa.onEventoComandoDiGioco()          →  processaComando(Comando.COMBATTIMENTO)
Automa.processaComandoInStatoInLocazione →  stato = Stato.IN_COMBATTIMENTO; BusEventi.pubblica(new InternoStatoDiGioco(...))
ForestaUI.gestisciEventoStatoDiGioco()   →  displayableCanvas.primoPiano(...); displayableCanvas.impostaAzioniIcone()
```

`ComandiPossibili`/`Comando.java` (`motore/Comando.java`, 135 righe) è l'enum di tutte le azioni pilotabili: scelte iniziali (sesso/classe), azioni in locazione (`COMBATTIMENTO`, `INCANTESIMO`, `CORRUZIONE`, `AMICIZIA`, `FUGA`), selezione personaggio (`PERSONAGGIO_1..5`), elementi magici (`ARIA`, `ACQUA`, `TERRA`, `FUOCO`, ...), più comandi meta (salvataggio, uscita, timer). Ogni stato dell'automa espone solo un sottoinsieme di `Comando` come "possibili" (`ComandiPossibili.set(...)`), che la UI traduce in icone cliccabili nella barra `DisplayableCanvasBarraIcone` (una `Finestra` disegnata dentro il canvas, non più un pannello Swing: vedi [`motore_grafico.md`](motore_grafico.md) §2).

## 3. Modello dati (`modellodati`) vs logica di comportamento (`motore`)

Il progetto separa nettamente **dati persistibili** da **comportamento**, con una convenzione di naming esplicita: le classi in `motore/modellodati/` hanno suffisso **`MD`** (ModelloDati) e sono essenzialmente bean serializzabili senza logica di gioco, mentre le classi in `motore/` (senza suffisso, es. `Personaggio`, `Foresta`, `Gruppo`) espongono API di comportamento che **operano su** un `*MD` sottostante.

`ModelloDati` (`motore/modellodati/ModelloDati.java`) è il contenitore radice, **singleton statico** (`private static ModelloDati istanza`, righe 22, 44-50), che aggrega:

```java
GruppoGiocatoreMD gruppoGiocatoreMD;     // party del giocatore
StatisticheMD statisticheMD;             // statistiche di partita
LineaTemporaleMD lineaTemporaleMD;       // log storico degli eventi di gioco
ForestaMD forestaMD;                     // la mappa e il suo stato
RegistroPersonaggiMD registroPersonaggiMD;  // NPC/mostri sparsi per la Foresta
RegistroArtefattiMD registroArtefattiMD;    // oggetti sparsi per la Foresta
RegistroMissioniMD registroMissioniMD;      // stato delle missioni
NotizieMD notizieMD;                        // ultimi messaggi e ultime notizie (nuovo)
```

Tutti implementano l'interfaccia `Serializzabile` (`motore/modellodati/Serializzabile.java`):

```java
public interface Serializzabile {
    String PIPE = "|";
    void salva(PrintWriter stream) throws IOException;
    void leggi(BufferedReader stream) throws IOException;
}
```

`ModelloDati.salva()`/`leggi()` delegano in cascata a ciascun componente — è un formato **testuale proprietario, non JSON**, con `|` come separatore di campo dichiarato nella costante `PIPE` (nonostante Gson sia una dipendenza dichiarata nel `pom.xml`, non risulta usata: vedi [`foresta.md`](foresta.md)).

Altre classi `*MD` rilevanti: `PersonaggioMD`, `ArtefattoMD`, `LocazioneMD`, `MissioneMD`, `GruppoMD`/`GruppoGiocatoreMD`, `CoordinateMD` (posizione x/y nella Foresta), `MappaProprieta` (probabile store chiave-valore per proprietà dinamiche). Le classi di logica (`Personaggio`, `Gruppo`, `Foresta`, `Automa`) leggono/scrivono questi bean ma incapsulano le regole: es. `Foresta` (statica, `motore/Foresta.java`) espone `getLocazione(x,y)`, `impostaLocazioneCorrente(...)`, `distruggiLocazioneUnica(...)` delegando sempre a `ModelloDati.getIstanza().getForestaMD()` (righe 30-64).

### Persistenza su file

`tools/GestoreSalvataggiSuFile.java` implementa `InterfacciaGestoreSalvataggi`:

- 5 slot di salvataggio fissi (`Comando.NUMERO_1`..`NUMERO_5`), un file `.TXT` per slot.
- La prima riga del file è un'intestazione leggibile (`id|nome capo del gruppo|giorno N, ora N`), usata da `getSalvataggiDisponibili()` per mostrare l'elenco senza dover leggere tutto il file (`leggiTestataSalvataggio`, righe 90-99).
- Il corpo del file è prodotto da `ModelloDati.getIstanza().salva(writer)` — una cascata di chiamate `Serializzabile.salva()`.
- In lettura, `ModelloDati.setIstanza(new ModelloDati())` sostituisce l'istanza singleton globale — quindi caricare una partita **rimpiazza interamente lo stato di gioco corrente**.
- Errori di I/O e file corrotti sono comunicati alla UI via eventi (`NotificaErroreCaricamento`, `InternoException`), mai eccezioni propagate al chiamante.
- Dopo l'installazione del nuovo `ModelloDati`, `GestoreSalvataggi.ricostruisciModelloDati()` (`tools/GestoreSalvataggi.java:46-54`) è il "punto unico" che ricalcola lo **stato derivato** non salvato esplicitamente: attributi secondari dei personaggi, il riferimento al `ModelloDati` dentro `GruppoGiocatore`, la locazione corrente ricostruita da `Foresta.costruisciIstanza(...)`, lo stato delle missioni — un commento nel codice avverte esplicitamente di non spostare questa chiamata prima dell'installazione, altrimenti le classi di dominio leggerebbero ancora la vecchia istanza.
- Nessuna cifratura: i file `NUMERO_N.TXT` in `~/.foresta/` sono testo semplice leggibile, e nessuna versione/migrazione di schema è prevista nel formato. Conseguenza concreta con l'ultima modifica: `NotizieMD` è stato accodato in fondo alla cascata di `ModelloDati.salva()/leggi()`, quindi **i salvataggi scritti prima di questa modifica non sono più leggibili** (`Integer.parseInt` su una riga assente); l'eccezione è intercettata da `GestoreSalvataggiSuFile` e comunicata come file corrotto, non fa crashare il gioco.
- `NotizieMD` (`motore/modellodati/NotizieMD.java`) salva due liste: gli ultimi messaggi mostrati nel pannello di testo e le ultime `Notizia` (`id|corpo`). Ogni messaggio è un `Messaggio` (`motore/modellodati/Messaggio.java`) con il testo e il tipo, paragrafo (`NotificaTestoParagrafo`) o continuazione (`NotificaTestoFrase`), salvato su una riga come `P|testo` o `F|testo`; il testo viene riletto per intero dopo il separatore, quindi può contenere `|`. Una riga senza quel prefisso, come nei salvataggi scritti prima dell'introduzione del tipo, viene riletta come continuazione. Due fragilità restano: un messaggio contenente un vero a-capo spezzerebbe la lettura (oggi non succede: nei testi gli a-capo sono scritti come `\n` letterale), e il corpo di una notizia viene riletto con `StringTokenizer` su `|`, quindi un `|` nel testo lo troncherebbe. Dopo un caricamento riuscito `Automa` pubblica `InternoCaricamentoCompletato` con una copia dei messaggi salvati, e la UI ripopola il pannello di testo con la stessa impaginazione: righe vuote prima dei paragrafi, a capo semplice per le continuazioni (test: `NotizieMDTest`).

Analogamente `GestorePunteggi`/`GestorePunteggiSuFile`/`GestorePunteggiBase` gestiscono una classifica persistente di punteggi: file di testo `forestaHS` in `~/.foresta/`, una riga per voce nel formato `nome#punteggio` (separatore `#`, diverso dal `|` usato per i salvataggi).

## 4. Sistema di combattimento

Il combattimento **non usa un tiro di dado classico "d20 vs CA"**: è un modello a **probabilità percentuale derivata dagli attributi**, calcolato in `CalcolatoreCombattimento.calcolaProbabilitaDiColpire()` (`motore/CalcolatoreCombattimento.java`, 527 righe):

1. **Controlli automatici sugli effetti di stato**: un attaccante `STORDITO` fallisce sempre (0%); un difensore `ATTERRATO`/`CONGELATO`/`STORDITO` viene colpito sempre (100%) — righe 20-29.
2. **Biforcazione per tipo di danno** (`SupertipoDanno.FISICO` vs magico/elementale):
   - Fisico: attacco = `Precisione + Destrezza`; difesa = `Velocità + Destrezza + 50% Parata`.
   - Magico: attacco = `Precisione + Intelligenza`; difesa = `Resistenza Magica + Saggezza`.
3. **Modificatori da effetti di stato** applicati in cascata: `CONFUSO` riduce l'attacco (mitigato dalla Saggezza), `ACCECATO` penalizza di più il danno fisico che quello magico (mitigato dalla Percezione), la `STANCHEZZA` penalizza linearmente, `RALLENTATO` dimezza la difesa fisica ma solo -10% quella magica.
4. Il risultato finale è un **valore percentuale 0-100** confrontato con un'estrazione `Math.random()`-based (`Dado`, vedi sotto) per determinare colpito/mancato.

Il **danno risultante** (`CalcolatoreCombattimento.calcolaDannoRisultante`, righe 129-393) parte da `danniArma × livelloArma`, scalato dalla statistica offensiva pertinente (Forza per il fisico, Intelligenza per il magico, con bonus Saggezza sul danno SACRO) moltiplicata per il livello dell'attaccante, e mitigato da un "rapporto di efficacia" `livelloArma / livelloAttaccante` (per impedire che un'arma di basso livello resti efficace su un bersaglio di alto livello). Un bonus BERSERK scala con la percentuale di salute persa dall'attaccante. La difesa del bersaglio mitiga il danno con una formula a **rendimenti decrescenti** (`100 / (100 + statisticaDifensiva)`), e un colpo critico (probabilità = 5% base + Critico attaccante − Fortuna difensore) raddoppia il danno.

**Interazioni elementali**: `TipoInterazioneElementale` implementa circa una ventina di combinazioni cross-elementali attivate quando un attacco di un certo tipo colpisce un bersaglio che porta già un certo `TipoEffettoDiStato`, es. BAGNATO+FULMINE → ELETTROCUZIONE (+50% danno), BAGNATO+GELO → CONGELATO, BAGNATO+FUOCO → VAPORIZZAZIONE (−50%), CONGELATO+CONTUNDENTE → FRANTUMAZIONE (danno ×2), MALEDETTO+SACRO → effetto rimosso. Ogni colpo può inoltre "proccare" probabilisticamente un nuovo effetto di stato (sanguinamento, veleno, ecc.), sia dall'arma fisica sia da ciascun `Incantamento` magico indipendentemente. Per gli effetti a danno nel tempo, `calcolaDurataStato`/`calcolaDannoPeriodico` riducono la durata base con una radice quadrata della statistica di resistenza del difensore (di nuovo rendimenti decrescenti) e scalano il danno per-tick sulle statistiche offensive/difensive.

Classi correlate: `DannoRisultante` (esito del danno calcolato), `RisultatoValutazioneAttaccante` (in `modellodati/`), `Arma`/`ArmaNaturale` (armi equipaggiate vs attacchi naturali dei mostri), `SupertipoDanno`/`TipoDanno` (fisico, elementale: fuoco/ghiaccio/aria/ecc.).

Turno e selezione bersaglio: `Gruppo` (astratta, estesa da `GruppoGiocatore` e `GruppoAvversario`, entrambe singleton statici — `getIstanza()`) implementa `getProssimoAttaccante()` (`motore/Gruppo.java:31-56`) con **selezione round-robin circolare** che salta i personaggi morti, e restituisce `null` se il gruppo è interamente sconfitto. Il combattimento avanza a un round al secondo, battuto dal `Comando.TIMER` che `Automa` inietta periodicamente mentre `stato == IN_COMBATTIMENTO` (vedi §1).

### Dadi e RNG

`Dado` (`motore/Dado.java`) è un wrapper statico su `Math.random()` — **non un vero PRNG con seed configurabile**, quindi partite non riproducibili deterministicamente:

- `tira(N)` / `tira(min, max)` — lancio dado classico, con validazione degli argomenti e log di errore fatale su input invalidi.
- `tiraAncheAUnaFaccia(N)` / `tiraAncheSenzaRange(min, max)` — varianti tolleranti per i casi limite (liste con un solo elemento), per evitare eccezioni quando si estrae da collezioni filtrate.
- `selezionaCasualmente(List<T>)` — estrae **e rimuove** un elemento casuale da una lista (usato per pescare senza ripetizione, es. selezione mostri/bottino).

`LanciatoreDeiDadi` (238 righe, non ispezionato in dettaglio) è presumibilmente un livello più alto sopra `Dado`, forse per lanci compositi (es. `3d6`, danno con più dadi).

## 5. Personaggi

`personaggi/Personaggio.java` (interfaccia/contratto, 563 righe) e `PersonaggioBase.java` (implementazione, **1624 righe** — la classe più grande del motore) definiscono il modello del personaggio giocante/NPC. Ogni classe giocabile e ogni tipo di mostro è una sottoclasse concreta: `Guerriero`/`Guerriera`, `Ladro`/`Ladra`, `Bardo`/`Cantastorie`, `Elfo`/`Elfa`, `Mago`/`Maga` per il giocatore; **oltre 25 mostri/NPC** (`Drago`, `Idra`, `Lich`, `Minotauro`/`MinotauroGigante`, `Strega`, `Troll`, `Goblin`, `Hobgoblin`, `Scheletro`, `Spettro`, `Spirito`, `Fantasma`, `Gargoyle`, `Gigante`, `Titano`, `Centauro`, `Chimera`/`ChimeraDrago`, `Arpia`, `Viverna`, `Folletto`, `Eremita`, `OmbraFiamma`, `OmbraNera`, ...) per popolare il mondo.

Il sistema di attributi (`motore/modellodati/TipoAttributo.java`, 278 righe, enum riccamente documentato) definisce almeno 5 attributi primari — **Forza**, **Destrezza**, **Costituzione**, **Intelligenza**, **Saggezza** (dedotta dall'uso in `CalcolatoreCombattimento`) — ciascuno con effetto esplicito sia in attacco sia in difesa, documentato nel Javadoc dell'enum stesso (es. Forza: danno mischia + resistenza a respingimenti; Intelligenza: danno magico + riserva MP + contrasto controincantesimi). Ne derivano attributi secondari calcolati: Precisione, Parata, Velocità, Resistenza Magica, Percezione, Stanchezza.

`Statistiche`/`StatisticheMD`, `TipoAttributo`, `ModificatoreAttributo`, `TipoModificatore` compongono un sistema di **modificatori stackabili** (es. bonus da equipaggiamento/incantesimo che si sommano/moltiplicano agli attributi base).

Effetti di stato (`EffettoDiStato`, `TipoEffettoDiStato`): almeno `STORDITO`, `ATTERRATO`, `CONGELATO`, `CONFUSO`, `ACCECATO`, `RALLENTATO` sono referenziati dal combattimento; il ciclo di vita (aggiunta/variazione/rimozione) è notificato alla UI via `NotificaVariazioneEffettoDiStatoPersonaggio` con un sotto-tipo `AGGIUNTA`/`VARIAZIONE`/`RIMOZIONE`.

Progressione (`GestoreProgressione.java`, 77 righe): curva di esperienza **quadratica** — `xpNecessari(livello) = 100 × (livello-1)²` — con cap a livello 50 e formula inversa per calcolare il livello dagli XP totali (`calcolaLivelloDaXp`, con `Math.sqrt`). Fonti di XP con percentuali fisse rispetto al prossimo livello: artefatto minore 5%, missione secondaria 20%, missione principale 50% (righe 54-73) — un bilanciamento a percentuale-del-gap piuttosto che a valore assoluto, che si autoadatta al livello corrente del personaggio. Le costanti stesse sono commentate esplicitamente dall'autore come la leva "da toccare per rallentare/velocizzare il gioco".

**Riposo** (`CalcolatoreRiposo.calcolaRiposo(Personaggio, ore, TipoRiposo)`): il recupero di salute/magia/stanchezza scala con un fattore tempo **non lineare** (`√ore`, rendimenti decrescenti sul riposare a lungo) moltiplicato per un fattore ambientale che dipende dal `TipoRiposo` (es. locanda più efficace di un accampamento di fortuna). Personaggi non-vivi/spettrali hanno moltiplicatori di recupero fisico/stanchezza pari a 0 nelle loro costanti di razza, quindi saltano di fatto quella componente del riposo.

## 6. Il mondo di gioco

`Foresta` (statica, `motore/Foresta.java`, 355 righe) rappresenta la mappa: griglia fissa **20×20** (`DIMENSIONE_X/Y`, righe 24-25 — ridotta da 50×50 nel commit `ea5ed98` "Prima prova generazione notizie", presumibilmente per velocizzare i test: da verificare prima di un rilascio), con locazioni indicizzate per coordinate (`CoordinateMD`) e classe di locazione (`ClassiLocazione`, enum). Espone concetti come "locazioni uniche" (`isLocazioneUnica()`, `ottieniCoordinateLocazioneUnica`, `distruggiLocazioneUnica`) — presumibilmente i castelli dei boss, che esistono una sola volta sulla mappa e possono essere "consumati"/trasformati dopo l'evento che rappresentano.

`RegistroPersonaggi` (facade statica su `RegistroPersonaggiMD`) popola, a inizio partita, un pool fisso di personaggi giocabili "disponibili" (5 guerrieri, 4 ladri, 2 bardi, 2 elfi, 2 maghi) sparsi casualmente per la mappa dentro locande e città (`Foresta.reimposta()`): è così che il giocatore recluta compagni visitando le locazioni, invece che scegliere l'intero party all'inizio.

### Tempo di gioco (`LineaTemporale`)

Facade statica su `LineaTemporaleMD`. Tiene un'ora del giorno (0-23, con 24 descrizioni testuali associate, es. "è l'alba") e un contatore di giorni; muoversi (`aggiungiOre`) e riposare (`mattinoSeguente`) fanno avanzare il tempo. Oltre il **giorno 40** il gioco termina automaticamente in sconfitta — il Drago vince per esaurimento del tempo a disposizione (`motore/LineaTemporale.java:105-130`). A giorni fissi (20/25/30/35), se il giocatore non l'ha impedito, una città viene "distrutta" (evento narrativo one-shot per città): è il meccanismo che dà urgenza/pressione temporale alla partita, oltre alla progressione lineare per missioni.

`locazioni/` (24 file) definisce i tipi di luogo visitabile: `Bosco`, `Grotta`, `Palude`, `Radura`, `Rovine`, `Tempio` (locazioni generiche, ripetute sulla mappa), `Citta`/`CittaFleena`/`CittaMalgaard`/`CittaNyena`/`CittaRuuna` (città nominate), `Locanda`, `Alchimista` (i due "negozi" visti anche dal lato UI), e locazioni uniche legate a missioni/boss: `CastelloDrago`, `CastelloIdra`, `CastelloLich`, `CastelloMinotauro`, `CastelloStrega`, `GrottaRecuperaIlMedaglione`, `RovineRecuperaLeDerrateAlimentari`. La gerarchia `LocazioneBase`/`Locazione`/`LocazioneUnica` rispecchia questa distinzione locazione-comune vs locazione-narrativa-unica.

## 7. Missioni

`missioni/` (24 file) implementa missioni concrete sopra una gerarchia `MissioneBase`/`Missione`/`SupertipoMissione`/`TipoMissione`/`ClasseMissione`. Pattern osservabili dai nomi:

- Missioni **generiche/parametriche** riusabili: `Combatti`, `MuoviALocazione`, `VisitaLocanda`, `MissioneRecuperaBersaglio` — presumibilmente classi-motore che una missione concreta configura con parametri (bersaglio, locazione).
- Missioni **principali con boss dedicato**: `SconfiggiIlDrago` (l'obiettivo finale del gioco), `SconfiggiLIdra`, `SconfiggiIlLich`, `SconfiggiIlMinotauroGigante`, `SconfiggiLaStrega`, `RecuperaIlMedaglione`, `RecuperaLeDerrateAlimentari`.
- Missioni **secondarie** con nomi narrativi: `CronacheDiUnFegatoEroico`, `DisturbatoreDellaQuietePubblica`, `NessunBoccaleLasciatoIndietro` — il tono scherzoso richiama i "quest" collaterali di gioco RPG comici.
- Missioni di test/sviluppo: `MissioneDIProva`, `MissioneDiProvaSecondariaUno/Due`, `MissioneDiProvaTerziariaUno`.

Il registro (`RegistroMissioni`/`RegistroMissioniMD`) traccia stato di avanzamento, notificato alla UI con `NotificaAggiornamentoStatoMissione`. Dopo un caricamento `aggiornaDopoRilettura` ricostruisce gli elenchi: per le missioni non predefinite i due rami erano invertiti (le completate finivano fra le attive e viceversa), ora corretto. Non aveva effetti visibili perché fra le radici del registro ci sono solo missioni predefinite.

### Intermezzi (cutscene)

> Per scrivere, animare e provare un intermezzo c'è una guida pratica dedicata: [`intermezzi.md`](intermezzi.md). Qui si descrive come il meccanismo è costruito.

Il package `intermezzi/` affianca le missioni con lo stesso schema, ma più semplice: un intermezzo ha un innesco e delle pagine, non ha progressi.

```java
public interface Intermezzo {
    String getId();
    boolean deveScattare(MomentoIntermezzo momento);  // l'innesco
    List<PaginaIntermezzo> getPagine();                // generate al momento in cui scatta
    default int getSecondiPerPagina() { return Costanti.SECONDI_PER_PAGINA_INTERMEZZO; }
}
```

- **Pagine**: una `PaginaIntermezzo` è composta a strati, tutti facoltativi: uno **sfondo** che copre lo schermo (in sua assenza l'ombra del drago), gli **elementi** nell'ordine di inserimento (i successivi sopra i precedenti), il **testo** in alto e le **battute** a fumetti. Le pagine sono costruite con metodi a catena quando l'intermezzo scatta, quindi personaggi e dialoghi possono dipendere dalla partita (il gruppo, i nomi, le missioni...).
  - `ImmagineIntermezzo` è un riferimento a un'immagine senza dipendere dalla UI: `personaggio(ClassePersonaggio)`, `locazione(ClassiLocazione)` (l'illustrazione), `risorsa("intermezzi/...png")` sotto `/com/threeamigos/foresta/img/`, oppure un'immagine **animata**:
    - `spriteSheet(percorso, colonne, righe, fotogrammiAlSecondo, sequenza...)`: la risorsa è una griglia di fotogrammi uguali, numerati da 0 riga per riga, mostrati in ordine o nella sequenza indicata e ripetuti; `getFotogrammaAl(secondi)` sceglie il fotogramma (test: `ImmagineIntermezzoTest`);
    - `animazione(Animazione)`: un fotogramma fornito istante per istante da una classe della UI, per animazioni disegnate via codice o composte (vedi [`motore_grafico.md`](motore_grafico.md) §7). Il modello nomina solo il valore dell'enum `Animazione`; per aggiungerne una servono una costante lì, una classe che implementa `ui.AnimazioneImmagine` e una riga in `ui.AnimazioniIntermezzo`. Per ora c'è `FUOCO_DA_CAMPO`.

    Le risorse sono caricate al volo dalla UI e, se mancano, l'elemento viene saltato con un `InternoErrore`, senza fermare il gioco. Anche lo sfondo può essere animato.
  - `ElementoIntermezzo` ha un identificativo, un'immagine, uno stato iniziale (`StatoElemento`: centro in frazioni dello schermo, scala, opacità, verso), e una sequenza di `Tappa` percorse linearmente (ogni tappa indica durata e i valori che cambiano; `attendi(secondi)` è una tappa ferma), ripetuta secondo `Ripetizione` (`UNA_VOLTA`, `CICLICA`, `AVANTI_E_INDIETRO`). La matematica dell'animazione (`getStatoAl(secondi)`) sta nel modello ed è coperta da `ElementoIntermezzoTest`. Serve sia per i personaggi sia per elementi di sfondo animati.
    - **Verso**: l'immagine può partire rovesciata (`specchiato()`), cambiare verso tappa per tappa (`Tappa.specchiata(true/false)`, che vale per tutta la durata della tappa e non si interpola) oppure guardare sempre nel verso in cui si muove (`orientaNelVersoDelMoto(Verso)`, indicando da che parte guarda l'immagine originale). In quest'ultimo caso l'elemento si gira anche nella fase di ritorno di `AVANTI_E_INDIETRO`, e nei tratti senza spostamento orizzontale vale il verso delle tappe.
  - `BattutaIntermezzo` è un fumetto detto da un elemento (`di(id, testo)`: la punta va alla sua bocca, per default in alto al centro dell'immagine, regolabile con `conBocca`, e lo segue se si muove) o da un punto fisso (`daPunto(x, y, testo)`, per chi è fuori scena). Se non si indica `daSecondo`, parte alla fine della precedente più una pausa di 0,3 s; se non si indica `perSecondi`, dura in base al testo (minimo 2 s); se non si indica `nuvolaA`, la UI colloca il fumetto sopra chi parla, verso il centro dello schermo. Una battuta deve riferirsi a un elemento già aggiunto alla pagina. `PaginaIntermezzo.getBattuteProgrammate()` calcola gli intervalli effettivi (test: `PaginaIntermezzoTest`).
  - **Due alfabeti**: il testo in alto usa l'alfabeto grande, che ha solo lettere, cifre e `' , . ?` (gli accenti si scrivono `e'`); i fumetti usano `DoomdarkFontMedium`, che ha anche lettere accentate, `! : ; " ( ) - / % +`.
- **Registrazione**: `ClasseIntermezzo` elenca le classi concrete, come `ClasseMissione`; l'ordine dell'enum è l'ordine in cui vengono mostrati più intermezzi scattati nello stesso momento. Per ora c'è solo `IntermezzoDiProva`: scatta alla prima locazione della partita; le prime due pagine mostrano il protagonista al centro, a due terzi dell'altezza, la terza prova sfondo (la radura), un drago animato avanti e indietro nel cielo che si gira quando torna, il fuoco da campo animato, un eremita che entra in scena e un dialogo di tre battute (da togliere dopo le prove).
- **"Già scattato"**: lo ricorda il registro, non l'intermezzo. `RegistroIntermezzi.getProssimoIntermezzo(momento)` restituisce il primo non ancora scattato che deve scattare; `IntermezziMD` salva l'insieme degli id scattati, in coda a `ModelloDati`. Un intermezzo viene segnato come scattato quando parte, quindi non si ripete mai; uno che non restituisce pagine viene saltato.
- **Momenti di innesco**: `MomentoIntermezzo.INIZIO_GIOCO`, nello stato omonimo, una volta sola dopo la creazione del personaggio e prima delle missioni; `MomentoIntermezzo.INIZIO_LOCAZIONE`, cioè in `INZIO_LOCAZIONE` dopo i controlli pre-locazione delle missioni e gli eventi della linea temporale (quindi mai a partita persa), prima che la locazione venga costruita. Il resto dell'ex `entraInStatoInizioLocazione` (costruzione, descrizione, `controllaInLocazione`, azioni) è ora nello stato `PREPARAZIONE_LOCAZIONE`. Altri momenti (per esempio dentro la locazione, con i mostri già presenti) si aggiungono all'enum senza cambiare l'interfaccia.
- **Svolgimento**: nello stato `INTERMEZZO` l'automa pubblica `NotificaPaginaIntermezzo` (la UI la mostra a tutto schermo con `DisplayableCanvasIntermezzo`, vedi [`motore_grafico.md`](motore_grafico.md) §7) e offre la pergamena. La pagina avanza al click sulla pergamena e, se `getSecondiPerPagina()` è maggiore di zero, anche da sola allo scadere del timer, riavviato a ogni pagina. Dopo l'ultima pagina l'automa controlla se c'è un altro intermezzo da mostrare; altrimenti pubblica `InternoMostraSchermataGioco` e prosegue con `PREPARAZIONE_LOCAZIONE`.
- **Configurare l'avanzamento**: il default per tutti è `Costanti.SECONDI_PER_PAGINA_INTERMEZZO` (8 secondi, più il click). Una pagina non avanza da sola prima che dialoghi e animazioni non ripetute siano finiti (`getDurataContenuto()`), e può fissare la propria durata con `perSecondi()`. La regola completa è in `PaginaIntermezzo.getSecondiPrimaDiAvanzare(secondiPerPagina)`, usata sia dall'automa sia dall'anteprima, così i due non possono divergere. `perSecondi()` che vale anche per gli intermezzi che altrimenti avanzerebbero solo al click. Un intermezzo che deve avanzare solo al click ridefinisce `getSecondiPerPagina()` restituendo 0; uno con un ritmo diverso restituisce il suo numero di secondi. Mettendo la costante a 0 tutti gli intermezzi avanzano solo al click.
- **Anteprima**: `ui.AnteprimaIntermezzo` mostra un intermezzo fuori dal gioco, in una finestra dal vivo oppure salvando una griglia PNG di pagine e istanti (uso e tasti in [`intermezzi.md`](intermezzi.md) §9). Per farlo prepara una partita minima con `motore.PartitaDiAnteprima.prepara(classe, nome)`: una Foresta nuova e un gruppo con il solo protagonista. Entrambe stanno tra i sorgenti dei test, perché sono strumenti di sviluppo e non entrano nel jar del gioco. `PartitaDiAnteprima` sta nel pacchetto `motore` perché `Foresta.reimposta()` è visibile solo lì.
- **Temporizzatore**: `Temporizzatore.inizia(ms)` fa scattare il primo impulso subito, il che va bene per il combattimento (primo round immediato) ma non per una sequenza di schermate, dove la prima sparirebbe all'istante. Per questo c'è `iniziaDopo(ms)`, con il primo impulso dopo un periodo intero, usato dagli intermezzi e dall'intro della UI. Anche le sequenze di sconfitta e vittoria (`GIOCO_PERSO_2`/`GIOCO_VINTO_2`) usano `iniziaDopo`: con `inizia` saltavano la loro prima pagina.

## 8. Economia: oggetti e offerte

`oggetti/` (13 file): gerarchia `Oggetto`/`OggettoBase`, con `Artefatto` come tipo principale equipaggiabile (`ArmaFisica`, `Spada`, `Scudo`, `Anello`, `Corona`, `Gemma`, `Cofano`, `Moneta`, `Incantamento` — un incantamento è un potenziamento elementale applicabile a un'arma, sommato nel calcolo del danno da `CalcolatoreCombattimento`, vedi §4). `CostruttoreArtefatto`/`CostruttoreArtefattoImpl` (`tools/`) è un **Builder** fluente (`setTipo → setNome → ... → costruisci()`); nel codice attuale è usato solo per istanziare a mano un piccolo set di artefatti "di prova" in `Automa.inizializzaGioco()` (vedi §9 per la discrepanza con la pipeline JSON/Gson documentata ma non attiva).

`RegistroArtefatti`/`RegistroArtefattiMD` gestisce gli artefatti sparsi per la Foresta; `AutomaAcquistiArtefatti` e `ScambiatoreArtefatti`/`AutomaScambiatoreArtefatti` implementano rispettivamente l'automa di acquisto (armaiolo) e lo scambio/inventario tra membri del gruppo — entrambi guidati da comandi giocatore dedicati (`ComandoAcquistoArtefatto`, `ComandoVenditaArtefatto`, `ComandoSpostamentoArtefatto`, `ComandoStoccaggioArtefatto`, `ComandoPrelievoArtefatto`), ciascuno con coppie di eventi `Notifica*Approvazione*`/`Notifica*Rifiuto*` — un pattern **richiesta/verdetto esplicito** invece di eccezioni, per comunicare alla UI se un'operazione economica è stata accettata o respinta (es. fondi insufficienti, inventario pieno).

Architetturalmente `AutomaScambiatoreArtefatti` è una classe astratta (pattern **Strategy/Template Method**) che modella *qualunque* scambio fra una `parteAttiva` (chi decide) e una `parteRemota` (chi riceve/fornisce), entrambe tipizzate dall'interfaccia `ScambiatoreArtefatti` (`getInventario`/`addArtefatto`/`removeArtefatto`, implementata sia da `Personaggio` sia da `Gruppo` sia dai negozianti). Le sottoclassi concrete (`AutomaInventario`, `AutomaAcquistiArtefatti`) differiscono solo su: se mostrare il costo su ciascun lato, e quale evento pubblicare quando l'utente sposta un oggetto in una direzione o nell'altra — lo spostamento fisico avviene solo dopo l'approvazione del motore (round-trip comando → notifica).

`offerte/` (9 file) modella i "servizi" disponibili nelle locazioni sociali (locande, città): `AiutoGratuito`, `AiutoMercenario`, `Incantesimi`, `Informazioni`, `MappaForesta`/`MappaZona`, `Pasto`, tutte sottotipi di `Offerta`/`ClassiOfferta` — presumibilmente un menu di opzioni acquistabili con moneta/reputazione quando si visita una locanda.

`AutomaInventario` (`motore/AutomaInventario.java`) governa lo stato della schermata inventario condiviso dal gruppo, letto dalla UI (`DisplayableCanvasInventario`).

## 9. Generazione procedurale di testo (`GrammarBean`)

Uno dei sottosistemi più sofisticati del motore, con **documentazione dedicata** (`motore/GrammarBean.md`, ~1080 righe — un manuale + un self-assessment con difetti noti numerati `Dn`/limiti `Qn`). `GrammarBean` è un **motore di grammatiche generative** (produzioni con alternative pesate, riferimenti annidati, variabili, span letterali, produzioni one-shot).

A runtime, `ProduttoreDiTestiCasuale` (`motore/ProduttoreDiTestiCasuale.java:27-42`) istanzia **solo tre grammatiche**, ciascuna con lo stesso file di post-produzione condiviso (`preposizioni_articolate_pp.txt`, per correggere preposizioni articolate italiane tipo "a il" → "al"):

- **Fiabe** (`fiabe.txt`) — testo narrativo casuale (`fiaba()`).
- **Oroscopi** (`oroscopo.txt`) — testo casuale (`oroscopo()`).
- **Locande** (`locande.txt`, ~780 righe): la produzione `DATI_LOCANDA` restituisce `nome/identificativo/nome locandiere/RECENSIONE=.../DIALOGO=...` (`getDatiLocanda()`), usati da `Foresta` per assegnare a ogni `Locanda` (anche quelle dentro le città) un'identità stabile tramite `Locanda.impostaDatiLocanda()`. Ogni locanda è una produzione one-shot (`CINGHIALE_VEGANO$`, ...) che fissa `IDENTIFICATIVO`, `NOME_LOCANDA`, `NOME_LOCANDIERE` (pescato da `[NOME_CASUALE]`), recensione e dialogo.

### Notizie delle locande (nuovo)

Uscendo da una locanda (`Locanda.generaNotizia()`, chiamata sui tre rami che portano a `FINE_LOCAZIONE`), il motore genera una "notizia" satirica sulle malefatte del gruppo:

1. `ProduttoreDiTestiCasuale.getNotiziaLocanda(id, nome, locandiere)` produce `NOTIZIE_<IDENTIFICATIVO>` dalla grammatica: 4 notizie specifiche per locanda più un rimando a `[NOTIZIE_LOCANDA]` (49 notizie generiche). Ogni alternativa ha la forma `ID-Titolo - Corpo` (es. `CV2-Lamentele della Locanda - ...`); l'id è tutto ciò che precede il primo `-`.
2. Se l'id compare già fra le ultime 10 notizie (`Costanti.MASSIMO_NOTIZIE_RICORDATE`) o se la notizia non è applicabile, si riprova, fino a `MASSIMO_TENTATIVI_NOTIZIA_LOCANDA = 20`, oltre cui si lancia `IllegalStateException`.
3. `validaNotizia()` gestisce i segnaposto: `BARDO`/`CANTASTORIE`, `LADRO`/`LADRA`, `GUERRIERO`/`GUERRIERA`, `MAGO`/`MAGA`, `ELFO`/`ELFA` rendono la notizia applicabile solo se nel gruppo c'è un personaggio **vivo** di quella classe (di qualunque sesso), e vengono sostituiti con il suo nome proprio; `EROE` diventa il nome del capo, `LETTERA_FINALE` la desinenza di genere, `NOME_LOCANDA`/`NOME_LOCANDIERE` i dati della locanda.
4. La notizia è pubblicata due volte: come `NotificaTestoParagrafo` (compare subito nel pannello di testo) e come `NotificaNotizia` (va nel notiziario della mappa).

`motore.Notizie` (facciata statica registrata in `Main`) si iscrive a `NotificaTestoFrase`, `NotificaTestoParagrafo` e `NotificaNotizia` e tiene aggiornate le liste di `NotizieMD` (più recente in testa, scarto oltre 100 messaggi / 10 notizie). È un caso insolito nel progetto: **un componente del motore che ascolta le proprie notifiche** per costruirsi uno storico, invece di essere chiamato direttamente da chi le pubblica.

**Correzione rispetto a una prima ipotesi**: `artefatti.txt`/`artefatti_pp.txt` (una grammatica che emette JSON di artefatti, con span letterali per proteggere `{`/`}`/`|`) **sono documentati in `GrammarBean.md` §5.3 come caso di studio, ma non sono referenziati da nessuna classe `.java`** — non c'è `getResourceAsStream("...artefatti.txt")` nel codice sorgente, e Gson non è importato in nessun file (`grep -r "gson\|Gson" src/main/java` non produce risultati). Gli artefatti effettivamente creati nel motore sono invece istanziati **a mano** in `Automa.inizializzaGioco()` (`motore/Automa.java:936-1032`) tramite il builder `CostruttoreArtefatto`, con nomi palesemente segnaposto/di test ("cazzabubbolo", "megaspada", "superscudo"...). La pipeline grammatica→JSON→Gson→`CostruttoreArtefatto` è quindi **un esempio didattico nella documentazione, non una feature attiva nel gioco** — un candidato o per essere rimosso (assieme alla dipendenza Gson) o per essere effettivamente completato in un refactoring futuro.

Caratteristiche notevoli documentate: sistema di pesi a due contributi (peso dichiarato `[^N]` + un **boost automatico proporzionale alla ricchezza del sottoalbero referenziato**, `peso_effettivo = peso_dichiarato + 0.33 × Σ peso_aggregato(referenziate)`), produzioni "fissate" globalmente (`[*Nome]`) o per sottoalbero (`[!Nome]`) per mantenere coerenza narrativa (stesso nome di personaggio ripetuto in una storia), produzioni one-shot (`Nome$`) per evitare ripetizioni in una serie. Il documento stesso segnala un difetto aperto non risolto (**D5**: nessun limite di profondità di ricorsione, può causare `StackOverflowError`) e due limiti di progetto (**Q3**: il boost automatico non è disattivabile/configurabile; **Q5**: le assegnazioni `[chiave=valore]` sono sempre globali anche quando dichiarate come locali).

## 10. Osservazioni per un eventuale refactoring

- **FSM monolitica**: `Automa` concentra ~1250 righe e oltre 45 handler di stato in un'unica classe; ogni nuova feature di gioco tende ad aggiungere sia un nuovo `Stato` sia un nuovo handler, con rischio di crescita non lineare della complessità ciclomatica. La ricorsione non limitata sull'avanzamento automatico (descritta più sopra) è stata eliminata a favore di un ciclo esplicito basato su `Esito`, ma la dimensione/il numero di responsabilità della classe restano gli stessi. L'interpretazione dello `Stato` restituito dalla locazione (combattimento → avvio del timer; fine gioco/fine locazione → arresto del timer e prosecuzione) è ora raccolta in `esitoDaStatoLocazione(Stato)`, usato sia da `gestisciComandoInStatoInLocazione()` sia da `entraInStatoInizioLocazione()`. Quest'ultimo prima gestiva solo `FINE_LOCAZIONE`: un eventuale `GIOCO_PERSO` restituito alla prima entrata avrebbe fermato l'automa in attesa di un comando.
- **Doppio significato di `impostaAzioni(..., null)`**: nelle locazioni vuol dire sia "prima entrata" sia "avanza di un passo", e in `LocazioneBase` ogni passo fa trascorrere un turno di effetti di stato. Lo si vedeva anche con `AIUTO`: ridescrivere la locazione passava dallo stesso `break` e costava un turno. Ora il caso restituisce subito `IN_LOCAZIONE`. Nei quattro casi `CHI_BEVE_POZIONE_*` la locazione richiama sé stessa con `impostaAzioni(..., null)` per far trascorrere il turno dopo la pozione (probabilmente voluto: bere è un'azione). Fino a poco fa il risultato di quella chiamata veniva scartato e si restituiva sempre `IN_LOCAZIONE`, quindi un `GIOCO_PERSO` o `FINE_LOCAZIONE` prodotto da quel turno (capo o ultimo avversario uccisi da un effetto di stato) andava perso e il gioco proseguiva. Ora il risultato viene restituito all'automa, che lo gestisce tramite `esitoDaStatoLocazione`. Rinunciare alla pozione (`ANNULLA` nella scelta di chi beve) ripresenta invece i comandi della locazione senza far trascorrere alcun turno. Bere una pozione durante un combattimento, quando il gruppo ha più di un personaggio vivo (e quindi si passa dalla scelta di chi beve), riporta la locazione a `IN_LOCAZIONE` e interrompe il combattimento: è un comportamento voluto (con un solo personaggio vivo la pozione viene bevuta senza scelta e il combattimento continua).
- **Nessun test automatico su `Automa`**: `src/test` copre `GestoreProgressione`, `GrammarBean`, il modello dati e `PersonaggioBase`, ma non la macchina a stati. Qualunque intervento su questa classe (incluso il refactoring del ciclo di dispatch sopra descritto) è verificabile solo per lettura attenta del codice e compilazione/smoke-test manuale, non da una suite che ne certifichi il comportamento — un investimento a cui pensare prima del prossimo intervento strutturale su `Automa`.
- **RNG non seedabile** (`Dado` usa `Math.random()` direttamente): preclude repliche deterministiche di partite per debug/test automatizzati del bilanciamento.
- **Dipendenza Gson dichiarata ma non usata**: zero import in tutto `src/main/java`. La pipeline che la richiederebbe (`artefatti.txt` → JSON → Gson → `CostruttoreArtefatto`) è documentata in dettaglio in `GrammarBean.md` §5.3 ma non è mai invocata da `ProduttoreDiTestiCasuale` né da altre classi: è un esempio didattico rimasto isolato dal codice di gioco, non una feature attiva. Da chiarire se rimuovere la dipendenza o completare l'integrazione.
- **Persistenza in formato proprietario pipe-delimited** anziché un formato standard (JSON/altro): funziona, ma rende più fragile l'evoluzione dello schema di salvataggio (nessuna versione/migrazione esplicita visibile nei file letti).
- **Generazione delle notizie: robustezza dei dati di grammatica**. Il codice presuppone che ogni alternativa di `NOTIZIE_*` abbia la forma `ID-Titolo - Corpo`, ma non lo verifica. Nel `locande.txt` c'erano già quattro alternative fuori formato, ora corrette: `CG9` senza il `-` dopo l'id, e `LG14`, `LG25`, `LG26` con `:` al posto di `" - "` fra titolo e corpo. Un'alternativa senza nessun `-` farebbe fallire `substring(0, -1)` con un'eccezione sull'EDT. Inoltre la produzione `NOTIZIE_CITTA` (12 notizie) è definita ma nessuna classe la usa, e `Locanda.generaNotizia()` lascia un log di debug (`"NOTIZIONA!!! -> "`). Un controllo di formato al caricamento della grammatica (o un test che produca tutte le alternative) proteggerebbe da questi casi.
- **`getNotiziaLocanda` può far fallire il gioco**: dopo 20 tentativi lancia `IllegalStateException`, non intercettata, sull'EDT. Con 4 + 49 alternative per locanda e 10 id recenti da evitare è molto improbabile, ma diventa possibile con un gruppo piccolo (molte notizie richiedono un `MAGO`, un `LADRO`, ecc. vivo). Tornare `null` e saltare la notizia sarebbe più tollerante. Anche il retry è ricorsivo: con un limite di 20 non è un problema, ma un ciclo sarebbe coerente con quanto fatto per `Automa`.
- **Mappa ridotta a 20×20** (`Foresta.DIMENSIONE_X/Y`), cambio di bilanciamento non dichiarato nel commit: verificare se è voluto o se è rimasto dai test.
- **Salvataggi incompatibili**: l'aggiunta di `NotizieMD` in coda al formato rende illeggibili i salvataggi precedenti. Nessun numero di versione permette di riconoscerli e leggerli con valori di default.
- **`GrammarBean.md` è già un self-assessment** dell'autore con difetti verificati sperimentalmente (D5, Q3, Q5): è probabilmente il documento di riferimento più maturo del repository e un buon modello di cosa intendere per "assessment" andando avanti sugli altri moduli.
