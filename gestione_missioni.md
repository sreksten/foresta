# Missioni a passi + intermezzi agganciati a una missione

## Stato (2026-10-02)

| Punto | Stato |
| --- | --- |
| 1. Checkpoint `LOCAZIONE_COMPLETATA` | fatto (`MomentoIntermezzo.LOCAZIONE_COMPLETATA`, `Stato.FINE_LOCAZIONE_2`) |
| 2. `RegistroIntermezzi` interroga le missioni | fatto, vedi "Come è stato implementato" in fondo al punto 2 |
| 3. `Passo` e `MissioneAPassi` | fatto, vedi "Come è stato implementato" in fondo al punto 3 |
| 4. Domande al giocatore | fatto, vedi "Come è stato implementato" in fondo al punto 4 |
| 5. Migrazione di Medaglione e Derrate | fatto, vedi "Come è stato implementato" in fondo al punto 5 |
| 6. Claim delle locazioni e `cerca` | da fare |
| 7. `SconfiggiIlDrago` e claim precoce | da fare, dopo il 6 |

## Contesto

Oggi un intermezzo scatta solo a due checkpoint statici e globali dell'automa
(`MomentoIntermezzo.INIZIO_GIOCO`, `INIZIO_LOCAZIONE`), pescando da un elenco
statico (`ClasseIntermezzo`) di intermezzi "singleton per partita": una volta
mostrati, un id finisce in `IntermezziMD` e non scattano più. Le missioni come
`RecuperaIlMedaglione`/`RecuperaLeDerrateAlimentari` codificano invece la loro
sequenza di fasi (attiva → bersaglio recuperato → completata) a mano, con
flag ad hoc su `controllaPreLocazione`/`InLocazione`/`PostLocazione`, e non
hanno alcun modo di agganciare un intermezzo a un proprio passaggio di stato.

Obiettivo: dare alle missioni un **automa a passi** riusabile, in cui ogni
passo può opzionalmente far scattare un intermezzo quando si completa —
usando lo scenario concreto di Recupera il Medaglione (assunzione incarico in
città → "portiamolo in città" a fine locazione dopo aver recuperato il
medaglione → ringraziamento all'arrivo in città) come primo caso reale. In
più, un nuovo checkpoint `MomentoIntermezzo.LOCAZIONE_COMPLETATA` serve sia a
questo scenario sia in generale a locazioni con eventi da segnalare a fine
locazione.

Il framework deve restare abbastanza generico da poter essere pilotato in
futuro da missioni generate con `GrammarBean` (molte istanze della stessa
classe di missione, ciascuna con proprio stato indipendente) — per questo lo
stato "intermezzo del passo già mostrato" vive nelle proprietà della singola
istanza di missione (stesso meccanismo di `ATTIVA`/`COMPLETA`/
`BERSAGLIO_RECUPERATO` in `MissioneBase`), **non** nel registro globale
`IntermezziMD`/`ClasseIntermezzo`, che non è pensato per crescere con istanze
dinamiche.

Per questo primo giro, `RecuperaIlMedaglione` e `RecuperaLeDerrateAlimentari`
vengono portate ciascuna, separatamente, sulla nuova base a passi — restano
due classi distinte. L'eventuale unificazione in un'unica classe parametrica
(propedeutica al generatore a grammatica) è un passo successivo, non parte di
questo piano.

**Vincolo aggiuntivo**: i due esempi concreti sono sequenze lineari, ma la
struttura dati NON deve assumere che un passo abbia un unico successore
fisso. Una missione deve poter, a un certo passo, diramarsi in base allo
stato di gioco, oppure fermarsi ad attendere una scelta del giocatore (sì/no,
o una fra più opzioni) e continuare con passi diversi secondo la risposta —
passi che possono essere già previsti staticamente o costruiti al volo (caso
tipico di una missione generata da `GrammarBean`, dove l'opzione scelta
determina quale variante testuale/parametrica del passo successivo esiste).
Il design del punto 3 tiene conto di questo fin da subito, e il punto 4
copre anche l'aggancio UI vero e proprio: un nuovo stato in `Automa`,
simmetrico a `Stato.ATTESA_SI_NO`, che permette a un passo di missione di
porre al giocatore una domanda sì/no o una scelta fra 2 e 5 opzioni
testuali, riusando l'infrastruttura di richiesta-comandi già esistente
(vedi punto 4) invece di introdurne una nuova da zero.

## 1. Nuovo checkpoint `MomentoIntermezzo.LOCAZIONE_COMPLETATA`

File: `src/main/java/com/threeamigos/foresta/intermezzi/MomentoIntermezzo.java`

Aggiungere il valore con un commento sullo stesso stile degli altri due,
spiegando che scatta dopo `controllaPostLocazione` e prima che il gruppo
riparta verso una nuova direzione.

File: `src/main/java/com/threeamigos/foresta/motore/Automa.java`

- `eseguiFineLocazione(Comando comando)` (righe ~808-866) va spezzato: la
  parte fino a `controllaMissioni(Missione::controllaPostLocazione,
  OrdineVisita.FIGLI_PRIMA)` + `locazioneCorrente.azzeraLocazione(gruppo)`
  resta nel gestore di ingresso/comando esistente, poi invece di procedere
  subito con game-over/stanchezza/`Stato.ATTESA_DIREZIONE` si chiama
  `avviaProssimoIntermezzo(MomentoIntermezzo.LOCAZIONE_COMPLETATA,
  Stato.FINE_LOCAZIONE_2)` (nuovo stato, stesso pattern di
  `INZIO_LOCAZIONE`→`avviaProssimoIntermezzo`→`PREPARAZIONE_LOCAZIONE`).
- Nuovo `Stato.FINE_LOCAZIONE_2` con gestore di ingresso che contiene la coda
  di `eseguiFineLocazione` (game-over check, decremento "a tempo",
  incremento stanchezza, transizione a `Stato.ATTESA_DIREZIONE`). Va
  registrato in `gestoriIngresso` come gli altri stati derivati
  (`INTERMEZZO` già dimostra il pattern di ripartire da uno stato "dopo" una
  volta esaurita la coda di intermezzi — vedi `statoDopoIntermezzi` in
  `avviaProssimoIntermezzo`/`gestisciComandoInStatoIntermezzo`, righe
  ~513-579).
- Nessun'altra modifica ad `avviaProssimoIntermezzo`: il meccanismo di coda
  esistente (mostra tutti gli intermezzi pendenti per quel momento, poi salta
  allo stato indicato) funziona identico per il nuovo momento.

## 2. `RegistroIntermezzi` interroga anche le missioni attive

File: `src/main/java/com/threeamigos/foresta/motore/RegistroIntermezzi.java`

`getProssimoIntermezzo(MomentoIntermezzo momento)` oggi scandisce solo
`ClasseIntermezzo.values()`. Va estesa: se lo scan statico non trova nulla,
scandisce l'albero delle missioni attive (stesso tipo di attraversamento che
`Automa.controllaMissioni`/`controllaMissione` già fa su
`RegistroMissioni.getMissioniNonCompletate()`, righe ~1400-1453 di
`Automa.java` — va estratto/duplicato come piccolo helper statico, es. su
`RegistroMissioni`, per non far dipendere `RegistroIntermezzi` da `Automa`)
cercando la prima missione con un passo pendente il cui intermezzo è per
`momento` e non è ancora stato mostrato.

`segnaScattato(Intermezzo)` diventa polimorfica: se l'intermezzo è
un'istanza del nuovo adattatore `IntermezzoDiPasso` (punto 4), la marcatura
va delegata alla missione (chiama un metodo che aggiorna la sua proprietà
MD), **non** scritta in `IntermezziMD`. Altrimenti comportamento invariato
(scrive in `IntermezziMD` come oggi).

### Come è stato implementato (2026-10-02)

- **Ordine:** `getProssimoIntermezzo(momento)` cerca prima gli intermezzi
  fissi non di ripiego, poi quelli dei passi, poi quelli fissi di ripiego. Un
  intermezzo di passo conta come scattato nel momento, quindi esclude i
  ripieghi come gli altri.
- **Dove cerca:** in `RegistroMissioni.getTutteLeMissioni()`, un helper nuovo
  che restituisce ogni missione dell'albero una volta sola e **in qualunque
  stato**, completate e fallite comprese. Il motivo: l'ultimo passo di una
  missione può avere un intermezzo (un ringraziamento) e insieme completarla,
  e `getMissioniNonCompletate()` lo perderebbe. Fra le missioni vale l'ordine
  dell'albero; dentro una missione, l'ordine in cui i passi si sono conclusi.
- **`IntermezzoDiPasso`** sta in `missioni/` (chiama `costruisciPasso`, che è
  protetto). L'id serve solo ai log (`<id missione>/<id passo>`). Il registro
  lo crea al volo; `segnaScattato` lo riconosce e chiama
  `segnaIntermezzoPassoMostrato` sulla missione invece di scrivere in
  `IntermezziMD`.
- **Quando si vede:** un passo `POST_LOCAZIONE` con intermezzo
  `LOCAZIONE_COMPLETATA` si vede nello stesso turno, perché
  `controllaPostLocazione` corre prima del checkpoint. Un passo `IN_LOCAZIONE`
  con intermezzo `INIZIO_LOCAZIONE` si vede invece all'ingresso nella
  locazione successiva, perché quel checkpoint viene prima dei controlli in
  locazione.
- **Test:** `RegistroIntermezziPassiTest` (2), su una partita vera: un
  intermezzo di passo scatta solo nel suo momento e una volta sola, il già
  mostrato sta nella missione e non in `IntermezziMD`, e l'ultimo passo di una
  missione completata mostra comunque il suo intermezzo.

## 3. `Passo` e `MissioneAPassi`: un automa a passi, non una lista lineare

Nuovo package `com.threeamigos.foresta.missioni.passi` (o direttamente in
`missioni`), due classi nuove. La cosa da evitare è modellare i passi come
`List<Passo>` con un indice: un indice assume una sequenza fissa e non regge
diramazioni né passi generati al volo. Il modello adottato è invece lo stesso
usato da `Automa` per gli stati: **passi identificati da una chiave
(`String id`)**, nessuna lista, nessun ordine implicito — chi decide "cosa
viene dopo" è il passo stesso, non la sua posizione.

**`Passo`** — piccolo value object che descrive un singolo passo. Non è una
`Missione` e non entra nell'albero `aggiungiMissione`/`getMissioniSecondarie`
(quell'albero resta per la composizione di sotto-missioni indipendenti, vedi
`CronacheDiUnFegatoEroico`; qui serve invece la logica di avanzamento dentro
*una* missione). Campi:

- `MomentoControllo momento` — enum con tre valori `PRE_LOCAZIONE`,
  `IN_LOCAZIONE`, `POST_LOCAZIONE`, che rispecchia i tre metodi esistenti di
  `Missione` e dice in quale di essi il passo va valutato.
- `BooleanSupplier condizione` — quando true, il passo è concluso e si può
  avanzare. Per un passo che aspetta una scelta del giocatore, la condizione
  è semplicemente "la risposta è già stata registrata" (vedi più sotto).
- `Runnable azione` — eseguita una sola volta quando la condizione diventa
  vera (attivare la missione, costruire una locazione, dare la ricompensa,
  pubblicare `NotificaTestoParagrafo`, salvare come proprietà l'esito di una
  scelta, eventualmente costruire dinamicamente i parametri di un passo
  successivo).
- `Supplier<String> prossimoPasso` — calcolato **dopo** `azione`, restituisce
  l'id del passo su cui la missione continua. Per una catena lineare è
  semplicemente una costante (`() -> "PASSO_2"`); per una diramazione legge
  lo stato di gioco o la proprietà appena scritta da `azione` e restituisce
  id diversi a seconda del caso. Restituire un sentinel dedicato (es. `null`
  o una costante `Passo.FINE`) segnala che questo era l'ultimo passo — tipicamente
  l'`azione` stessa avrà già chiamato `completaMissione()`.
- opzionale: `MomentoIntermezzo momentoIntermezzo` +
  `Supplier<List<PaginaIntermezzo>> pagine` — se presenti, al completarsi del
  passo viene registrato un intermezzo pendente per quel momento (le pagine
  si costruiscono pigramente, come già oggi per gli `Intermezzo` statici, così
  possono leggere lo stato di gioco al momento in cui scattano).

Costruzione con un piccolo builder fluente (`Passo.quando(momento,
condizione).esegui(azione).poi(prossimoPasso).conIntermezzo(momentoIntermezzo,
pagine)`), sul modello di `PaginaIntermezzo`/`ElementoIntermezzo` già usato in
`intermezzi/`.

**`MissioneAPassi`** — `abstract class extends MissioneBase`:

- costruttore protetto che riceve solo `ClasseMissione` (come oggi); **non**
  riceve una lista di passi.
- unico metodo abstract da implementare nelle sottoclassi:
  `protected abstract Passo costruisciPasso(String id)` — una fabbrica che,
  dato un id, ricostruisce il `Passo` corrispondente (tipicamente uno
  switch/if-chain su costanti `String`). Viene chiamata ogni volta che serve
  valutare il passo corrente, non tenuta in cache: questo è il punto chiave
  che permette ai passi dinamici di funzionare senza dover serializzare
  `BooleanSupplier`/`Runnable` (impossibile con il salvataggio su testo di
  questo progetto) — si persiste solo l'id (una stringa) più, se il passo
  dinamico ne ha bisogno, i parametri che lo caratterizzano come proprietà
  MD ordinarie (stesso meccanismo di `aggiungiProprieta` già usato per tutto
  il resto). Un passo generato da `GrammarBean` con `[!MANDANTE]`/
  `[!BERSAGLIO]` fissati, per esempio, salva quei valori come proprietà
  prima di restituire il proprio id come `prossimoPasso`, e
  `costruisciPasso` li rilegge per rigenerare lo stesso testo in modo
  deterministico — anche dopo un salvataggio/caricamento.
- id del passo corrente persistito come proprietà MD `PASSO_CORRENTE`
  (stringa, non indice: insensibile a riordini o inserimenti futuri).
  L'id iniziale è restituito da un secondo metodo abstract,
  `protected abstract String passoIniziale()`.
- proprietà `INTERMEZZO_MOSTRATO_<id>` (una per id di passo con intermezzo)
  per il bookkeeping "già mostrato" richiesto dal punto 2 — un metodo
  `segnaIntermezzoPassoMostrato(String idPasso)` e
  `isIntermezzoPassoMostrato(String idPasso)` usati da
  `RegistroIntermezzi`/`IntermezzoDiPasso`.
- implementa `controllaPreLocazione()`/`controllaInLocazione()`/
  `controllaPostLocazione()` delegando a un unico
  `avanzaSePronto(MomentoControllo)`: se non fallita/completa, recupera
  `Passo corrente = costruisciPasso(getPassoCorrente())`; se
  `corrente.getMomento() == quello richiesto` e
  `corrente.getCondizione().getAsBoolean()`, esegue `azione.run()`, registra
  l'eventuale intermezzo pendente, calcola `prossimoPasso.get()` e lo
  persiste come nuovo `PASSO_CORRENTE` (o chiama/verifica `completaMissione()`
  se è il sentinel di fine).
- sottoclassi che hanno bisogno di logica extra (es. fallimento per città
  distrutta, come oggi in `RecuperaIlMedaglione.controllaPreLocazione`)
  possono sovrascrivere il metodo corrispondente chiamando
  `super.controllaPreLocazione()` a fine metodo — stesso pattern di override
  già visto in `MuoviALocazione.completaMissione()`.

**Persistenza dei dati generati.** Una missione (soprattutto se generata a
caso con `GrammarBean`) ha in genere due tipi di cose da salvare in modo
permanente, oltre all'id del passo corrente:

1. *Valori singoli fissati alla generazione* (es. il nome del mandante, il
   nome del bersaglio, l'importo della ricompensa). Non serve costruire
   nulla di nuovo: `Missione.aggiungiProprieta(String, String)`/
   `ottieniProprieta(String)` sono già a chiave e valore completamente
   liberi — `MissioneMD` li mantiene in una `Map<String, String>` qualsiasi
   (verificato in `src/main/java/com/threeamigos/foresta/motore/modellodati/MissioneMD.java`,
   serializzata da `MappaProprieta` nello stesso file), senza alcun enum o
   whitelist di chiavi. Una chiave tipo `"MANDANTE"` o `"BERSAGLIO_NOME"`
   scritta da un passo generato funziona già oggi, esattamente come le
   costanti `ATTIVA`/`COMPLETA`/`FALLITA` usate da `MissioneBase`. Il passo
   che genera questi valori (tipicamente il primo, quando la missione viene
   attivata) li scrive una sola volta con `aggiungiProprieta`, e
   `costruisciPasso` li rilegge sempre con `ottieniProprieta` invece di
   richiamare di nuovo `GrammarBean.produce()` — così il testo resta
   identico anche dopo un salvataggio/caricamento, senza dipendere da un
   seed o da alcuna persistenza del generatore stesso.
2. *La sequenza dei passi da fare*, quando non è fissa a compile time ma
   decisa alla generazione (es. "visita N locazioni scelte a caso" con N
   variabile). Per questo caso `MissioneAPassi` offre due metodi protetti,
   `impostaSequenzaPassi(List<String> idPassi)` e
   `List<String> leggiSequenzaPassi()`, che non richiedono alcuna modifica a
   `MissioneMD`/`MappaProprieta`: si appoggiano alla stessa proprietà
   generica del punto 1, salvando la lista come un unico valore stringa con
   gli id separati da un carattere che gli id di passo non useranno mai
   (es. `,`, non `§`/`|` che sono già riservati da `MappaProprieta`). Un
   passo la cui logica è "vai al prossimo elemento della sequenza generata"
   usa il supplier di comodo `prossimoNellaSequenza()` (legge
   `leggiSequenzaPassi()`, trova l'id corrente, restituisce il successivo o
   il sentinel di fine se era l'ultimo) invece di scrivere a mano la
   diramazione; i passi con vera diramazione/scelta del giocatore
   continuano a usare un `Supplier<String>` esplicito come già descritto.
   In questo modo il "piano" generato casualmente (quali passi, in quale
   ordine) è deciso una volta sola dall'azione del passo che lo genera e
   resta stabile per tutta la vita della missione, incluso attraverso
   salvataggio/caricamento — non viene mai ricalcolato o rigenerato.

**Diramazioni e scelte del giocatore.** Con questo modello una diramazione
che dipende solo dallo stato di gioco è già supportata: `prossimoPasso` è un
`Supplier` qualsiasi, può leggere `LineaTemporale`, proprietà della missione,
ecc. Una diramazione che dipende da **una scelta del giocatore** (conferma
sì/no, o una fra più opzioni) si modella con un passo che:
1. resta con `condizione` false finché non esiste ancora una proprietà
   "risposta data" sulla missione;
2. una volta che qualcosa scrive quella proprietà, `condizione` diventa vera
   e `prossimoPasso` la rilegge per decidere il ramo.

Il "qualcosa" che raccoglie la risposta del giocatore e scrive la proprietà è
descritto nel punto 4: un nuovo stato di `Automa` che presenta la domanda,
aspetta il comando del giocatore e chiama
`MissioneAPassi.rispondi(String idOpzioneScelta)` (implementato con
`aggiungiProprieta`, come già previsto qui sopra). La struttura dati di
questo paragrafo (passo che aspetta una proprietà, `prossimoPasso` che la
rilegge, `costruisciPasso` che può generare il ramo scelto anche se non era
previsto in anticipo) non richiede alcuna modifica per supportarlo: il
punto 4 aggiunge solo il meccanismo che scrive quella proprietà a partire da
un input reale del giocatore, invece che a scopo di analisi.

**`IntermezzoDiPasso implements Intermezzo`** (in `intermezzi/` o
`missioni/`, da vedere in base ai package-private necessari) — adattatore
`(MissioneAPassi missione, String idPasso)`: `getId()` non serve per
bookkeeping (delega alla missione), ma utile per log/debug;
`deveScattare(momento)` verifica solo per coerenza (la selezione è già fatta
da chi lo crea); `getPagine()` richiama il supplier del passo.

Registrazione in `ClasseMissione.java`: nessuna voce nuova richiesta per
`Passo`/`MissioneAPassi` di per sé (non sono `Missione` concrete), solo le
sottoclassi concrete (punto 4) vanno registrate come oggi.

### Come è stato implementato (2026-10-02)

Classi in `missioni/` (non in un package a parte, per usare i membri protetti
di `MissioneBase`): `Passo` e `MissioneAPassi`, test in `MissioneAPassiTest`
(8). Rispetto al piano:

- **Builder:** `Passo.quando(momento, condizione).esegui(azione).poi(id)`, con
  `poi(Supplier<String>)` per le diramazioni e `conIntermezzo(momento, pagine)`.
  `MomentoControllo` è un enum annidato in `Passo`. Senza `esegui` l'azione non
  fa niente; senza `poi` il passo successivo è `Passo.FINE`.
- **Passi di fila:** se il passo che diventa corrente è dello stesso controllo
  ed è già concluso, si esegue subito, nello stesso controllo, così un passo di
  solo testo non costa un turno. Oltre 50 passi di fila si lancia
  `IllegalStateException`, perché è quasi certamente un ciclo.
- **Fine:** `Passo.FINE` completa la missione se l'azione non l'ha già fatto.
  Una missione completa o fallita non avanza più; se l'azione fa fallire la
  missione, il passo corrente non cambia.
- **Attivazione:** la fa l'azione di un passo (`attivaMissione()`), come oggi;
  i passi si valutano anche per una missione non ancora attiva, perché il primo
  è spesso proprio quello che la attiva.
- **Intermezzi dei passi:** un passo concluso con un intermezzo lascia il suo id
  nella proprietà `INTERMEZZI_IN_ATTESA` (lista separata da virgole).
  `getPassiConIntermezzoInAttesa()`, `getPassoConIntermezzoInAttesa(momento)`,
  `segnaIntermezzoPassoMostrato(id)` e `isIntermezzoPassoMostrato(id)` sono le
  chiamate per il punto 2. `costruisciPasso` deve quindi saper ricostruire
  anche i passi già superati. L'adattatore `IntermezzoDiPasso` si scriverà con
  il punto 2, che è il primo a usarlo.
- **Sequenze:** `impostaSequenzaPassi(lista)` (id tutti diversi, lista non
  vuota), `leggiSequenzaPassi()` e `prossimoNellaSequenza()`.
- **Id dei passi:** non possono essere vuoti né contenere `,`, `§` o `|`
  (separatore delle liste e caratteri riservati del salvataggio):
  `IllegalArgumentException` altrimenti.

## 4. Il giocatore risponde a una domanda di missione

Un passo che rappresenta una domanda (conferma sì/no, o scelta fra 2 e 5
opzioni testuali) ha bisogno di un aggancio reale con l'utente. Il progetto
ha già tutti i pezzi per questo, usati oggi per casi analoghi (conferma di
fuga, scelta dell'incantesimo, scelta della direzione): non serve inventare
nulla di nuovo lato `Comando`/UI, solo un nuovo stato in `Automa` che li
orchestri per conto di una missione.

**Riuso di infrastruttura esistente** (verificato leggendo il codice, non
per supposizione):
- `Comando.SI`/`Comando.NO` (`src/main/java/com/threeamigos/foresta/motore/Comando.java`)
  e le icone corrispondenti in `ClasseIcona` — già usati da
  `Stato.ATTESA_SI_NO` per la conferma di fuga (`LocazioneBase.chiediConfermaPerLaFuga` +
  `RichiestaSelezioneSiNo`). Riusati tali e quali per una domanda di
  missione a due opzioni.
- `Comando.NUMERO_1`..`NUMERO_5` (stesso file), già con icone dedicate
  (`icone/1.gif`..`5.gif` in `ClasseIcona`) e già riusati per due scopi
  diversi secondo il contesto (numero di passi, slot di salvataggio) — lo
  stesso riuso "per contesto" si applica a una scelta di missione fra 2 e 5
  opzioni testuali: l'opzione N-esima elencata dal passo corrisponde a
  `NUMERO_N`.
- `RichiestaConComandi` (`src/main/java/com/threeamigos/foresta/eventi/RichiestaConComandi.java`),
  la classe base già usata da `RichiestaSelezioneSiNo` e dalla selezione
  incantesimo/direzione per dire alla UI "mostra queste icone e aspetta
  un clic". Nuova sottoclasse `RichiestaSelezioneMissione` nello stesso
  package `eventi/richieste/`, stesso pattern esatto di
  `RichiestaSelezioneSiNo`.
- Il testo della domanda si pubblica separatamente con
  `NotificaTestoParagrafo`, esattamente come `chiediConfermaPerLaFuga`
  pubblica `NotificaTestoFrase` prima di richiedere sì/no — nessuna novità.

**Nuovo stato `Stato.ATTESA_RISPOSTA_MISSIONE`** (in
`src/main/java/com/threeamigos/foresta/motore/Stato.java`), **non** un
riuso di `Stato.ATTESA_SI_NO`: quest'ultimo, alla risposta, rigioca il
comando nello stato precedente aspettandosi che sia proprio quello stato
(es. la gestione fuga di `LocazioneBase`) a intercettare `SI`/`NO` — un
meccanismo cablato sul chiamante che non si vuole toccare né estendere per
un caso d'uso completamente diverso (una missione, non uno stato
dell'automa). Il nuovo stato ha una risoluzione diversa e più semplice:
chiama direttamente la missione, senza bisogno che nessuno "intercetti" il
comando altrove.

In `Automa.java`:
- due nuovi campi privati, es. `MissioneAPassi missioneInAttesaDiRisposta` e
  l'id del passo in attesa (per costruire la chiave della proprietà),
  impostati subito prima della transizione a questo stato.
- `gestoriIngresso.put(Stato.ATTESA_RISPOSTA_MISSIONE, this::entraInStatoAttesaRispostaMissione)`:
  pubblica il testo della domanda (`Passo.getTestoDomanda()`), poi
  `RichiestaSelezioneMissione` con la lista di `Comando` corrispondente al
  numero di opzioni del passo (2 opzioni booleane → `SI`/`NO`; 2-5 opzioni
  generiche → `NUMERO_1..NUMERO_N`), poi `Esito.FERMATI` — stesso schema di
  `entraInStatoAttesaSiNo`.
- `gestoriComando.put(Stato.ATTESA_RISPOSTA_MISSIONE, this::gestisciComandoInStatoAttesaRispostaMissione)`:
  se il comando non è `Comando.TIMER`, traduce il `Comando` ricevuto
  nell'id di opzione del passo (mappa inversa rispetto a quella usata per
  costruire la richiesta), chiama
  `missioneInAttesaDiRisposta.rispondi(idOpzione)`, azzera i due campi e
  torna a `statoPrecedente` **senza rigiocare il comando** (a differenza di
  `ATTESA_SI_NO`: qui nessuno stato a valle deve intercettarlo, la risposta
  è già stata consegnata alla missione) — semplicemente
  `stato = statoPrecedente; return Esito.CONTINUA_CON_INGRESSO` fa
  ripartire da capo la valutazione dei passi (`controllaMissioni`), che
  ora troverà la proprietà risposta già scritta.

**Sul lato `Passo`/`MissioneAPassi`** (estensione del punto 3, stesse
classi, nessuna classe ulteriore):
- il builder fluente si estende con `.chiediConferma(String testoDomanda)`
  (zucchero sintattico per il caso a due opzioni SI/NO) e
  `.chiediScelta(String testoDomanda, List<String> opzioni)` (2 a 5 opzioni
  testuali, mappate in ordine su `NUMERO_1..NUMERO_N`). Entrambi impostano
  automaticamente `condizione` a "la proprietà risposta di questo passo
  esiste" — chi scrive la missione non deve scriverla a mano.
- `MissioneAPassi.rispondi(String idOpzione)`: unico punto di scrittura,
  `aggiungiProprieta("RISPOSTA_" + getPassoCorrente(), idOpzione)` — la
  chiave è qualificata dall'id del passo corrente, quindi risposte di passi
  diversi (anche in istanze diverse della stessa missione generata a caso)
  non collidono mai.
- `prossimoPasso` di un passo-domanda tipicamente legge
  `ottieniProprieta("RISPOSTA_" + idPasso)` per scegliere il ramo — stesso
  meccanismo già descritto nel punto 3, ora effettivamente popolato da un
  input reale.
- una proprietà di bookkeeping `DOMANDA_<id>_PRESENTATA` (stesso schema di
  `INTERMEZZO_MOSTRATO_<id>` nel punto 3) evita di ripresentare la stessa
  domanda a ogni frame mentre l'automa è già in
  `Stato.ATTESA_RISPOSTA_MISSIONE` aspettando la risposta.

**Chi rileva che un passo-domanda è pronto e non ancora presentato**: stesso
punto di aggancio già descritto nel punto 2 per gli intermezzi di passo
(dentro `controllaMissioni`, ai checkpoint pre/in/post-locazione) — un
piccolo helper (stesso spirito di quello per `RegistroIntermezzi`) scandisce
le missioni attive cercando un passo la cui condizione è "domanda non
ancora presentata"; se lo trova, `Automa` imposta i due campi e transita a
`Stato.ATTESA_RISPOSTA_MISSIONE` invece di procedere con il normale
`Esito.CONTINUA_CON_INGRESSO`. Non c'è ambiguità con gli intermezzi di passo
del punto 2: un intermezzo scatta al completarsi di un passo, una domanda
scatta quando il passo successivo (quello con la domanda) diventa corrente
— non possono mai capitare nello stesso istante per lo stesso passo.

### Come è stato implementato (2026-10-02)

Rispetto al piano, due differenze di sostanza:

- **Niente ritorno allo stato precedente.** I controlli delle missioni stanno
  in mezzo al lavoro di tre stati: `INZIO_LOCAZIONE` (prima degli eventi del
  tempo e degli intermezzi), `PREPARAZIONE_LOCAZIONE` (dopo che la locazione è
  stata costruita e descritta) e `FINE_LOCAZIONE` (dopo la raccolta
  dell'oggetto). Rientrare in quegli stati dopo la risposta rifarebbe cose già
  fatte: costruire di nuovo la locazione con i suoi mostri, ritentare la
  raccolta. Ogni controllo passa quindi da `Automa.controllaMissioniEDomande(
  momento, seguito)`: fa il controllo e, se una missione ha una domanda da
  porre, passa a `ATTESA_RISPOSTA_MISSIONE` tenendo da parte una ripresa.
  Consegnata la risposta, il controllo si rifà (il passo con la domanda si
  conclude, e può nascerne un'altra) e solo allora si prosegue con `seguito`,
  il resto del lavoro dello stato (`proseguiInizioLocazione`,
  `proseguiPreparazioneLocazione`, `concludiFineLocazione`). La ripresa è una
  lambda e non si salva: va bene, perché in `ATTESA_RISPOSTA_MISSIONE` i soli
  comandi disponibili sono le risposte, quindi non si può salvare a metà
  domanda.
- **Niente `DOMANDA_<id>_PRESENTATA`.** La domanda si pone una volta per
  controllo, e finché non c'è risposta l'automa resta fermo in
  `ATTESA_RISPOSTA_MISSIONE`: non c'è nulla da ripresentare. Se la risposta
  non arriva mai (per esempio la partita si chiude), la domanda si ripone al
  prossimo controllo del suo momento, che è quel che si vuole.

Il resto come previsto:

- **`Passo`:** `chiediConferma(testo)` (risposte `Passo.SI`/`Passo.NO`) e
  `chiediScelta(testo, opzioni)` (da 2 a 5 opzioni, risposte "1".."N"). La
  condizione del passo dice **quando la domanda si può porre**; il passo si
  conclude quando c'è la risposta, e solo nel controllo del suo momento.
- **`MissioneAPassi`:** `getDomandaDaPorre(momento)`, `rispondi(risposta)`
  (scrive `RISPOSTA_<id passo>`, rifiuta risposte non valide e passi senza
  domanda) e `getRisposta(idPasso)`, da leggere nel `poi` della diramazione.
- **`Automa`:** stato `Stato.ATTESA_RISPOSTA_MISSIONE`. All'ingresso pubblica
  la domanda (`NotificaTestoParagrafo`), le opzioni numerate
  (`NotificaTestoFrase`, "1. …") e `RichiestaSelezioneMissione` con `SI`/`NO` o
  `NUMERO_1..N`; un comando che non è una risposta è un comando non valido, il
  `TIMER` si ignora. Cerca le domande nelle stesse missioni che il controllo
  visita (radici non completate e figlie delle missioni attive).
- **UI:** `ForestaUI` mostra le icone delle risposte e porta in primo piano il
  riquadro del testo.
- **Test:** 3 nuovi in `MissioneAPassiTest` (quando si offre la domanda,
  risposta e ramo, opzioni di una scelta) e `ScenarioDomandeMissioniTest`, su
  una partita vera: entrando in una locazione la missione chiede quale strada
  prendere fra tre, la risposta sceglie il ramo, la missione si completa e la
  locazione non viene costruita di nuovo.

## 5. Migrare `RecuperaIlMedaglione` e `RecuperaLeDerrateAlimentari`

File: `src/main/java/com/threeamigos/foresta/missioni/RecuperaIlMedaglione.java`
(e l'equivalente `RecuperaLeDerrateAlimentari.java`, stessa struttura).

Ciascuna diventa `extends MissioneAPassi` con 3 `Passo`:

1. `IN_LOCAZIONE`, condizione = gruppo in `CITTA_FLEENA` e non attiva, azione
   = testo di incontro + `attivaMissione()` + costruzione della locazione
   grotta — **con** un intermezzo iniziale (`MomentoIntermezzo.
   INIZIO_LOCAZIONE`, essendo valutato durante `controllaInLocazione` che
   corre comunque a valle di quel checkpoint per il primo arrivo in città;
   se si vuole davvero un intermezzo "appena assunto l'incarico" mostrato
   nello stesso frame, si può lasciare senza intermezzo qui e mettere solo il
   testo in `NotificaTestoParagrafo` come oggi, dato che l'utente ha parlato
   esplicitamente di intermezzo iniziale come *esempio* generico, non come
   requisito stretto per questa missione — da confermare nella review).
2. `POST_LOCAZIONE`, condizione = gruppo in `GROTTA_RECUPERA_IL_MEDAGLIONE`
   e locazione completa, azione = `setBersaglioRecuperato()`-equivalente
   (ora solo avanzamento passo, il flag dedicato sparisce), **con**
   intermezzo `LOCAZIONE_COMPLETATA` ("il medaglione è stato recuperato, va
   riportato in città").
3. `IN_LOCAZIONE`, condizione = gruppo in `CITTA_FLEENA`, azione =
   `completaMissione()` + ricompensa, **con** intermezzo
   `INIZIO_LOCAZIONE` (o nessuno, restando sul solo testo — stessa nota del
   passo 1) di ringraziamento.

La guardia "città distrutta" resta un override di `controllaPreLocazione()`
che chiama `fallisciMissione()` e poi delega a `super.controllaPreLocazione()`
(che con la missione già fallita non farà avanzare nulla, dato che
`avanzaSePronto` controlla `isFallita()`).

`MissioneRecuperaBersaglio` (la classe intermedia con `BERSAGLIO_RECUPERATO`)
può essere rimossa se non ha più altri usi dopo la migrazione — verificare
con una ricerca testuale prima di eliminarla.

### Come è stato implementato (2026-10-02)

Le due missioni hanno lo stesso scheletro, quindi `MissioneRecuperaBersaglio`
non è stata tolta ma è diventata la base a passi comune (`extends
MissioneAPassi`): le due classi concrete danno solo città, covo, scene e testi.
Il flag `BERSAGLIO_RECUPERATO` non c'è più: `isBersaglioRecuperato()` guarda il
passo corrente. Cinque passi invece di tre:

1. `INCARICO` (`PRE_LOCAZIONE`, nella città non distrutta): intermezzo a
   `INIZIO_LOCAZIONE`, il mandante che chiede aiuto.
2. `ACCETTAZIONE` (`IN_LOCAZIONE`, nella città): riassunto nel riquadro del
   testo, `attivaMissione()` e costruzione del covo.
3. `RECUPERO` (`POST_LOCAZIONE`, nel covo completato): testo.
4. `RITORNO` (`PRE_LOCAZIONE`, nella città): intermezzo a `INIZIO_LOCAZIONE`,
   il ringraziamento.
5. `RICOMPENSA` (`IN_LOCAZIONE`, nella città): 20 monete e testo, poi `FINE`,
   che completa la missione.

Perché due passi per ogni momento: `attivaMissione()` e `completaMissione()`
pubblicano subito l'avviso globale ("NUOVA MISSIONE", "MISSIONE COMPLETATA"), e
l'automa aspetta che gli avvisi finiscano prima di mostrare un intermezzo.
Se l'avviso partisse insieme all'intermezzo comparirebbe prima della scena.
Il passo con l'intermezzo è quindi a inizio locazione, che corre prima del
checkpoint `INIZIO_LOCAZIONE`; quello con l'avviso è in locazione, che corre
dopo gli intermezzi. La città si riconosce dalle coordinate del gruppo, quindi
il passo a inizio locazione funziona anche se la locazione non è ancora
costruita. La guardia "città distrutta" resta un override di
`controllaPreLocazione()`.

**Scene:** `intermezzi/ScenaInCitta` (pubblica) riusa `ScenaNegozio`, che ora
accetta un primo piano assente: sfondo `locazioni/Citta.gif`, il mandante a
destra (per ora l'immagine del locandiere), il gruppo che arriva in fila come
nei negozi e poi le battute. Le coordinate degli intermezzi sono dello schermo
e lo sfondo della città (390 × 320) è più piccolo di quelli dei negozi
(500 × 348): mandante a x 0,62 e personaggi a y 0,6, nelle stesse proporzioni
dello sfondo.

**Test:** `ScenarioMissioniDiRecuperoTest` (3), su una partita vera: a Fleena e
a Ruuna la pagina con le battute del mandante arriva prima dell'avviso di nuova
missione, e il covo compare; al ritorno il ringraziamento resta in attesa a
inizio locazione e monete e completamento arrivano solo dopo.

## 6. Locazioni assegnate dinamicamente: claim delle missioni e `cerca(ClassiLocazione)`

### Perché

Oggi (verificato leggendo il codice, non per supposizione) le locazioni
"uniche" (città, castelli, `GROTTA_RECUPERA_IL_MEDAGLIONE`,
`ROVINE_RECUPERA_LE_DERRATE_ALIMENTARI`) vivono in
`ForestaMD.locazioniUniche`, un `EnumMap<ClassiLocazione, CoordinateMD>`: **una
sola coordinata per valore dell'enum**. Funziona per un numero fisso di
locazioni scritte a mano (una `GrottaRecuperaIlMedaglione`, quattro castelli
alleati, un castello del Drago), ma non scala ai quasi 190 `TipoMissione` di
`passi_missioni.md`: non si può dare a ciascuno una propria costante
`ClassiLocazione` e relativa sottoclasse.

Due precedenti concreti mostrano già i due estremi:
- `Foresta.costruisciCastelli()` (righe ~178-184) piazza i quattro castelli
  alleati **a tempo di generazione del mondo**, sempre, incondizionatamente —
  le missioni `SconfiggiLaStrega`/`Lich`/`MinotauroGigante`/`LIdra` si
  limitano a controllare se il gruppo è arrivato in un castello già esistente.
- `SconfiggiIlDrago.controllaPostLocazione()` (righe ~61-66) **costruisce da
  sé** `CASTELLO_DRAGO` a runtime, quando serve (`castelliDistrutti()`),
  chiamando `Foresta.costruisciLocazioneUnica(ClassiLocazione.CASTELLO_DRAGO,
  false)` — che internamente usa `getCoordinateLibere()` (righe ~256-264 di
  `Foresta.java`), una ricerca a tentativi casuali finché non trova una
  casella `BOSCO`/`RADURA`/libera.

L'idea è generalizzare il secondo pattern (una missione si procura da sola la
propria locazione, quando ne ha bisogno) e usarlo anche per i quattro castelli
alleati, eliminando il piazzamento incondizionato a tempo di generazione.
Questo **non** richiede eliminare `ClassiLocazione`/`LocazioneUnica`: i
castelli restano sottoclassi concrete come oggi. Cambia solo *quando* e *chi*
decide la coordinata.

### Claim delle missioni in `RegistroMissioni`

Nuovo stato privato in `RegistroMissioni`: `Map<CoordinateMD, String>
locazioniOccupate` (coordinata → id della missione che la occupa). Non serve
toccare `ForestaMD`/`MissioneMD`: è un indice in più, ricostruibile dopo un
caricamento rileggendo le proprietà delle missioni attive (vedi sotto), non
un dato persistito per conto suo.

- `occupaLocazione(CoordinateMD, Missione)`: registra il claim. Chiamato
  dall'`azione` del passo che costruisce/rivendica la locazione (stesso
  momento in cui oggi si chiamerebbe `costruisciLocazioneUnica`).
- La missione stessa memorizza la coordinata come proprietà ordinaria (stesso
  schema di `COORDINATA_X`/`COORDINATA_Y` già usato da `MuoviALocazione`) —
  non serve una mappa inversa nel registro, la missione sa già dove si trova
  la propria locazione.
- Nessun metodo esplicito "libera": una missione conclusa **non** deve
  liberare attivamente la coordinata (romperebbe l'ordine fra
  `completaMissione()` e il resto del turno). La liberazione è **passiva**,
  a carico di `cerca()` (sotto): quando la ricerca incontra una coordinata
  occupata, guarda l'id della missione proprietaria; se quella missione è
  `isCompleta()`/`isFallita()`, rimuove il claim da `locazioniOccupate` e
  tratta la coordinata come libera per questa ricerca. La missione conclusa
  **resta** dov'è già oggi (`elencoMissioniPredefiniteCompletate`/
  `elencoMissioniSecondarieCompletate` — vedi `RegistroMissioni.java` righe
  55-61): si toglie solo il suo claim sulla locazione, non la missione dal
  diario/storico.
- Dopo un caricamento (`aggiornaDopoRilettura`, righe 88-118), il registro dei
  claim va ricostruito scandendo le missioni attive che hanno proprietà
  coordinata (stesso giro che già fa `aggiornaDopoRiletturaImpl`): non serve
  serializzare `locazioniOccupate` a parte.

### `cerca(ClassiLocazione richiesta)`: ricerca a quadrati concentrici

Nuovo metodo (su `RegistroMissioni`, perché deve conoscere sia la mappa sia i
claim; internamente chiama `Foresta.getLocazione(coordinate)` per leggere la
casella). Diverso da `getCoordinateLibere()`: quello cerca una casella
*libera* qualsiasi per piazzarci qualcosa di nuovo; questo cerca una casella
che **esiste già** di un certo `ClassiLocazione` (es. un `BOSCO` da
trasformare in castello, un `TEMPIO` già presente) e non è rivendicata da una
missione ancora attiva.

Nota sui nomi: l'esempio `cerca(TipoLocazione.TEMPIO)` non corrisponde
esattamente ai tipi esistenti — `TipoLocazione` è solo il raggruppamento
(`STANDARD`/`CITTA`/`CASTELLO`/`MISSIONE_SECONDARIA`), mentre `TEMPIO`,
`BOSCO` ecc. sono valori di `ClassiLocazione` (vedi
`src/main/java/com/threeamigos/foresta/locazioni/ClassiLocazione.java` righe
10-16). Il filtro di `cerca()` è quindi su `ClassiLocazione`, non su
`TipoLocazione`.

Algoritmo, a partire da una coordinata casuale `(x0, y0)` (stesso
`Dado.tira` già usato da `getCoordinateLibere`):
1. Controlla `(x0, y0)` stesso (raggio 0).
2. Per raggio `r = 1, 2, 3, ...` crescente, controlla solo il **bordo** del
   quadrato di lato `2r+1` centrato su `(x0, y0)` (cioè le celle con distanza
   di Chebyshev esattamente `r` dal centro — non l'intero quadrato, altrimenti
   si riconterebbero le celle già viste ai raggi precedenti).
3. Per ogni cella del bordo (in un ordine qualsiasi ma deterministico, es. dal
   lato nord in senso orario): se fuori dai limiti della mappa
   (`0 <= x < Foresta.getDimensioneX()`, idem per y) la si scarta; altrimenti
   se `Foresta.getLocazione(coordinate) != richiesta` la si scarta; altrimenti
   controlla il claim come descritto sopra (occupata da missione attiva →
   scarta; occupata da missione conclusa → libera il claim e accetta; libera →
   accetta).
4. La ricerca termina quando trova una cella accettabile (la marca subito con
   `occupaLocazione` per la missione chiamante e ne restituisce la
   coordinata), oppure quando il quadrato di raggio `r` è interamente fuori
   dai limiti della mappa su tutti i lati (raggio massimo utile ≈
   `max(getDimensioneX(), getDimensioneY())`, oggi 20): in quel caso non
   esiste alcuna cella `richiesta` libera su tutta la mappa e il metodo
   restituisce `null`/`Optional.empty()` — la missione che lo ha chiamato
   deve gestire questo caso (tipicamente: non avanza questo turno e riprova al
   turno successivo, non è un fallimento della missione).

### Impatto sulle missioni principali (`Sconfiggi*`)

- `Foresta.costruisciCastelli()` (righe 178-184) perde il piazzamento
  incondizionato dei quattro castelli alleati — resta solo, se serve,
  eventuale terreno "neutro" preesistente (bosco) su cui le missioni
  costruiranno.
- `SconfiggiLaStrega`/`SconfiggiIlLich`/`SconfiggiIlMinotauroGigante`/
  `SconfiggiLIdra`: `controllaPreLocazione()` oggi si limita ad
  `attivaMissione()`. Diventa: se non attiva, `RegistroMissioni.cerca(
  ClassiLocazione.BOSCO)`, trasforma la coordinata trovata nel proprio
  castello (stessa `setLocazione`/registrazione che oggi fa
  `costruisciLocazioneUnica`, ma sulla coordinata già scelta da `cerca()`
  invece che da `getCoordinateLibere()`), poi `attivaMissione()`. Se `cerca()`
  non trova nulla questo turno, la missione resta non attiva e si riprova al
  turno successivo (mappa 20×20, praticamente non dovrebbe mai succedere con
  solo 4 castelli da piazzare).
- `SconfiggiIlDrago` **non cambia comportamento**: già oggi costruisce
  `CASTELLO_DRAGO` da sé a runtime. Per coerenza si può far passare anche lui
  da `RegistroMissioni.cerca(ClassiLocazione.BOSCO)` invece che da
  `getCoordinateLibere()` diretto (così anche il suo claim entra nel
  registro), ma non è necessario per la correttezza: è già lo schema che gli
  altri quattro devono imitare.

### Impatto su `Recupera il Medaglione`/`Le Derrate Alimentari` e passo `CercaLocazione`

Le due missioni del punto 5 hanno già una coordinata fissa
(`GROTTA_RECUPERA_IL_MEDAGLIONE`/`ROVINE_RECUPERA_LE_DERRATE_ALIMENTARI` sono
`ClassiLocazione` dedicate, sempre presenti una sola volta): per loro il claim
è opzionale, non toglie né aggiunge nulla al piano del punto 5 e si può
saltare in questo primo giro. Diventa utile quando una missione **non** ha
una `ClassiLocazione` dedicata e deve trovarne una a runtime: un nuovo tipo di
passo `CercaLocazione(ClassiLocazione richiesta)` (da aggiungere al catalogo
di `passi_missioni.md`) la cui `azione` chiama `cerca()` e salva la coordinata
trovata come proprietà (stesso schema `COORDINATA_X`/`COORDINATA_Y` di
`MuoviALocazione`); `condizione` resta falsa (passo non concluso, si riprova
al turno successivo) se `cerca()` non trova nulla.

### Cosa non serve toccare

A differenza dell'idea più ampia discussa a voce (eventi interni
`InternoSconfittaPersonaggio`, hook di riempimento/descrizione a
`INIZIO_LOCAZIONE` al posto di `LocazioneUnica`), questo pezzo **non tocca
`Automa`**: `occupaLocazione`/`cerca()` sono chiamate dirette dentro
l'`azione` di un passo o dentro `controllaPreLocazione`/`PostLocazione` di una
missione, esattamente come oggi `costruisciLocazioneUnica`. È quindi
indipendente e più piccolo del resto della discussione su "locazione unica",
e può essere costruito prima, senza aspettare quella parte.

## 7. `SconfiggiIlDrago`: claim precoce delle quattro missioni figlie, memoria storica del claim

### Il percorso proposto è già quello che il codice fa oggi, a parte il claim

Verificato riga per riga: `SconfiggiIlDrago()` (costruttore, righe 17-23) crea
già le quattro missioni figlie (`SconfiggiLaStrega`, `SconfiggiIlLich`,
`SconfiggiIlMinotauroGigante`, `SconfiggiLIdra`) appena viene istanziata, cioè
a `RegistroMissioni.reimposta()` — quindi alla generazione della Foresta, non
dopo. `controllaPreLocazione()` (righe 47-52) attiva `SconfiggiIlDrago` al
primissimo controllo, senza condizioni. E la visita dell'albero delle
missioni in `Automa.controllaMissione` (righe 1433-1453) usa
`OrdineVisita.PADRE_PRIMA` per `controllaPreLocazione` — il commento sul posto
lo dice esplicitamente: *"una missione che si attiva adesso porta con sé le
proprie figlie nello stesso giro"* (riga ~1410). Risultato: con la modifica
del punto 6 (`SconfiggiLaStrega` & co. chiamano `RegistroMissioni.cerca(
ClassiLocazione.BOSCO)` dentro il proprio `controllaPreLocazione` invece di
limitarsi ad `attivaMissione()`), le quattro rivendicano ciascuna una propria
locazione **nello stesso giro** in cui parte la partita, senza bisogno di
nessun collegamento nuovo fra `SconfiggiIlDrago` e le figlie: è già tutto
cablato dall'albero `aggiungiMissione`/`getMissioniSecondarie` esistente e
dall'ordine di visita esistente. Sei corretto su questo punto.

### Il claim va tenuto anche dopo il completamento, non evitato — va solo corretta la regola di "disponibilità"

Il punto 6, come scritto, fa evitare da `cerca()` un claim non appena la
missione proprietaria è completa — pensato per liberare la coordinata al
prossimo che ne ha bisogno, ma incompatibile con l'idea di conservare "qui
sorgeva il Castello della Strega" come testo dopo il completamento: se il
claim si cancella, non resta nulla da interrogare per scriverlo.

Correzione: **non cancellare mai un claim**, solo scriverne sopra uno nuovo
quando serve. La disponibilità per `cerca()` diventa "la coordinata non ha
claim, oppure il claim è di una missione non più attiva
(`!missione.isAttiva()`, cioè completata o fallita)" — la stessa condizione
di prima, ma senza la cancellazione: chi rivendica la coordinata sovrascrive
semplicemente la voce con il proprio id. `locazioniOccupate` finisce così a
fare doppio servizio: per `cerca()` è "chi ha la coordinata **adesso**, se
ancora attivo"; per un hook di descrizione è "chi l'ha avuta **per ultimo**",
utile anche dopo che qualcun altro l'ha rivendicata di nuovo (risolve anche
la domanda lasciata aperta due messaggi fa su come arbitrare un conflitto fra
due missioni sulla stessa locazione: vince l'ultima che l'ha rivendicata,
punto e basta, perché è l'unica voce che esiste).

Un hook di descrizione a `INIZIO_LOCAZIONE` (idea del messaggio precedente)
può quindi interrogare `RegistroMissioni` per "chi ha il claim su questa
coordinata" indipendentemente dal `ClassiLocazione` che ci si trova sopra in
quel momento — così "qui sorgeva il Castello della Strega" può comparire
anche quando la casella è già tornata `ROVINE` o `BOSCO`.

**Nota**: oggi nessuna missione trasforma davvero il proprio castello in
rovine dopo la vittoria — `SconfiggiLaStrega.controllaPostLocazione()` chiama
solo `completaMissione()`, la casella resta `CASTELLO_STREGA` con la sua
proprietà `LocazioneMD.COMPLETA`. Per avere davvero "rovine" bisognerebbe
aggiungere, sul modello già esistente di
`GrottaRecuperaIlMedaglione.azzeraLocazione()` (che a fine missione chiama
`Foresta.distruggiLocazioneUnica(..., ClassiLocazione.GROTTA)`), un
`azzeraLocazione` analogo che trasformi il castello sconfitto in `ROVINE`. È
un'aggiunta, non qualcosa che va "preservato" da una funzionalità già
esistente — ma lo schema è lo stesso, collaudato.

### Intermezzi per passo: già coperto, nessuna novità

Confermato: se `SconfiggiLaStrega` & co. venissero riscritte come
`MissioneAPassi` (punto 3), ogni passo — rivendica locazione, combatti,
completa — può già portare un `.conIntermezzo(...)` opzionale esattamente
come progettato al punto 2/3 per `RecuperaIlMedaglione`. Non serve alcun
meccanismo nuovo: è lo stesso framework, applicato a missioni che oggi sono
ancora `MissioneBase` semplici. La migrazione delle cinque missioni
`Sconfiggi*` a `MissioneAPassi` resta però un passo di implementazione
separato (più cinque classi da convertire, con `costruisciCastelli()` da
smontare) — non è incluso nel piano attuale (punto 5), che riguarda solo le
due missioni di recupero.

## Verifica

- `mvn -o compile -q` (workaround offline già in uso in questo progetto) per
  verificare che tutto compili dopo ogni fase.
- Le scene sono state controllate disegnandone i fotogrammi con
  `DisplayableCanvasIntermezzo`. Resta da vedere a mano, nel gioco, l'intero
  percorso qui sotto.
- Avvio manuale del gioco, missione Recupera il Medaglione: assumere
  l'incarico in città, notare la locazione grotta creata; combattere e
  vincere nella grotta, verificare che a fine locazione scatti l'intermezzo
  "portalo in città" (nuovo checkpoint `LOCAZIONE_COMPLETATA`) prima del
  messaggio "in quale direzione ti incammini"; tornare in città e verificare
  il completamento con ricompensa e (se previsto) l'intermezzo finale.
- Ripetere lo stesso percorso per Recupera le Derrate Alimentari.
- Controllare che uscendo e ricaricando un salvataggio a metà missione (es.
  dopo il passo 1) lo stato riprenda dal passo corretto (proprietà
  `PASSO_CORRENTE` persistita correttamente).
- `Stato.ATTESA_RISPOSTA_MISSIONE` (punto 4) è verificato da
  `ScenarioDomandeMissioniTest` su una partita vera. Resta da vedere a mano,
  nel gioco, come appaiono domanda e icone delle risposte.
- Punto 6: avviare una partita, verificare che i quattro castelli alleati
  compaiano solo quando la relativa missione `Sconfiggi*` li rivendica (non
  più tutti fin dall'inizio); sconfiggerne uno, verificare che `cerca()` non
  lo riproponga più come `BOSCO` disponibile finché la missione non è
  completa, e che dopo il completamento la sua coordinata possa essere
  rivendicata di nuovo da un'altra missione (claim liberato passivamente).
  Verificare anche il caso limite "nessun `BOSCO` libero trovato" forzando
  temporaneamente una mappa piena, per controllare che la missione resti
  semplicemente non attiva invece di fallire o lanciare un errore.
