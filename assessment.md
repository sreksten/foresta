# Assessment di Foresta

Valutazione del progetto al **2026-11-09**, rifatta da zero dopo la nascita dei moduli del tutorial, dell'interfaccia comune e di libGDX. Le misure sono prese sul working tree dopo `mvn clean install` e `mvn test`:
- righe e file sono contati con `wc -l` su `src/main/java` e `src/test/java` di ogni modulo; senza righe vuote né commenti il codice di produzione è di **85.844 righe**;
- la copertura viene da JaCoCo, sia modulo per modulo sia con tutti i test insieme;
- i moduli e le loro dipendenze sono letti dai `pom.xml` e confrontati con `CLAUDE.md` e con il documento di ogni modulo.

I giudizi sono segnati come **Giudizio**. I difetti ancora aperti stanno nel §5, con i codici della revisione precedente (B35, B41…), che [`todo.md`](todo.md) usa. Il §6 è nuovo: dice quanto i documenti `.md` sono allineati al codice. Le cose da fare per il porting su Android e iOS sono in [`assessment_portabilita.md`](assessment_portabilita.md): qui c'è solo il riassunto.

Questo assessment **non è una nuova lettura integrale dei sorgenti**. Sono rifatti da capo le misure, il grafo dei moduli, la verifica dei difetti aperti e il confronto fra documenti e codice (§6, che non è un controllo riga per riga: vedi le Osservazioni).

## 1. Sintesi

- **Il gioco è completo e giocabile** da inizio a fine, con una novità: ora c'è un tutorial di 31 suggerimenti (modulo `03-foresta-tutorial`), e l'interfaccia esiste in due versioni, Swing e libGDX (con un lanciatore Android fuori da Maven).
- **Il progetto è diviso in 33 moduli Maven, in fila.** Ognuno vede solo quelli sotto di lui, e il compilatore lo garantisce. I moduli nuovi dall'ultima volta sono `03-foresta-tutorial`, `12-foresta-ui-comune` (la logica di interfaccia che non disegna), `12-foresta-libgdx` e `13-foresta-app-libgdx`.
- **3633 test, tutti verdi** (31 saltati, simulazioni da lanciare a mano), in 5 minuti e 25 secondi. La copertura delle righe con tutti i test è dell'**84%** (era 77%). Fuori dalla UI quasi ogni modulo è coperto al 95-100% dai test suoi.
- **Giudizio sul codice:** in buona salute. Non ci sono difetti aperti di gravità alta o media. Il confine fra logica e disegno è nuovo e funziona: `DisplayableCanvas` è passato da 1781 a 676 righe, `PersonaggioBase` da 2054 a 1162. `Automa` però è cresciuto (da 2415 a 2615 righe).
- **Giudizio sui documenti:** è il punto debole. I 33 documenti di modulo descrivono bene il loro modulo, con la sezione delle dipendenze uguale ai `pom.xml` quasi dappertutto (§6.2). Ma i documenti trasversali e gli appunti di progetto (`motore_di_gioco.md`, `motore_grafico.md`, `tutorial.md`, `assessment_portabilita.md`, `didattico.md`, `foresta.md`…) raccontano ancora il progetto prima della divisione in moduli, dell'interfaccia comune e di libGDX. `CLAUDE.md` aveva diversi percorsi vecchi (§6.3). **Dopo la valutazione il 2026-11-09 sono stati corretti `CLAUDE.md`, `foresta.md`, `didattico.md` e la maggior parte degli altri documenti**; resta aperto quanto elencato nel §6.4 (soprattutto `assessment_portabilita.md` e le sezioni sulle azioni delle tattiche).

## 2. Misure

| Modulo | Codice (file / righe) | Test (file / righe) | Test eseguiti | Copertura dei suoi test | Copertura con tutti i test |
| :--- | ---: | ---: | ---: | ---: | ---: |
| `00-foresta-serializzazione` | 4 / 373 | 4 / 346 | 32 | 100% | 100% |
| `00-foresta-strumenti` | 15 / 1.003 | 9 / 911 | 51 | 100% | 100% |
| `01-foresta-eventi` | 16 / 1.069 | 3 / 245 | 16 | 100% | 100% |
| `01-foresta-punteggi` | 4 / 221 | 1 / 98 | 7 | 100% | 100% |
| `01-foresta-testi` | 26 / 4.611 | 15 / 3.739 | 341 | 95% | 99% |
| `02-foresta-attributi` | 13 / 1.024 | 6 / 409 | 27 | 96% | 96% |
| `02-foresta-comandi` | 2 / 287 | 2 / 120 | 7 | 100% | 100% |
| `02-foresta-effettidistato` | 4 / 312 | 3 / 88 | 5 | 100% | 100% |
| `02-foresta-mappa` | 15 / 1.999 | 9 / 1.277 | 76 | 100% | 100% |
| `02-foresta-notizie` | 5 / 368 | 4 / 278 | 15 | 100% | 100% |
| `03-foresta-danni` | 4 / 572 | 4 / 181 | 8 | 100% | 100% |
| `03-foresta-tutorial` | 7 / 589 | 3 / 317 | 21 | 95% | 99% |
| `04-foresta-artefatti` | 44 / 5.169 | 25 / 3.435 | 196 | 100% | 100% |
| `05-foresta-personaggi` | 143 / 18.724 | 38 / 4.876 | 249 | 99% | 100% |
| `06-foresta-bestiario` | 5 / 353 | 3 / 399 | 22 | 100% | 100% |
| `06-foresta-combattimento` | 15 / 2.886 | 18 / 3.569 | 151 | 99% | 100% |
| `06-foresta-statistiche` | 8 / 729 | 5 / 667 | 37 | 100% | 100% |
| `07-foresta-intermezzi` | 41 / 3.549 | 7 / 1.104 | 43 | 98% | 100% |
| `07-foresta-mondo` | 5 / 1.037 | 4 / 551 | 30 | 100% | 100% |
| `07-foresta-poteri` | 59 / 3.509 | 8 / 1.691 | 57 | 98% | 100% |
| `08-foresta-bottino` | 17 / 1.032 | 3 / 328 | 13 | 100% | 100% |
| `08-foresta-economia` | 42 / 2.292 | 9 / 1.430 | 66 | 100% | 100% |
| `08-foresta-tattiche` | 52 / 5.949 | 31 / 6.066 | 291 | 100% | 100% |
| `09-foresta-missioni` | 117 / 20.842 | 31 / 5.835 | 194 | 100% | 100% |
| `10-foresta-modellodati` | 2 / 306 | 10 / 1.012 | 43 | 97% | 100% |
| `10-foresta-trofei` | 13 / 754 | 5 / 709 | 32 | 100% | 100% |
| `11-foresta-motore-api` | 46 / 1.578 | 5 / 335 | 15 | 100% | 100% |
| `12-foresta-motore` | 64 / 10.487 | 234 / 38.001 | 1091 (31 saltati) | 97% | 97% |
| `12-foresta-ui-comune` | 129 / 24.481 | 30 / 3.813 | 242 | 35% | 72% |
| `12-foresta-swing` | 85 / 9.362 | 15 / 2.164 | 68 | 21% | 61% |
| `12-foresta-libgdx` | 53 / 6.558 | 11 / 1.361 | 60 | 6% | 6% |
| `13-foresta-app-swing` | 3 / 838 | 41 / 4.210 | 127 | 30% | 30% |
| `13-foresta-app-libgdx` | 1 / 105 | 0 / 0 | 0 | - | 0% |
| **Totale** | **1059 / 132.968** | **596 / 89.565** | **3633 (31 saltati)** | | **84%** |

- **"Copertura dei suoi test"** è quella del rapporto di JaCoCo del modulo: conta solo i test che stanno nel modulo.
- **"Copertura con tutti i test"** unisce i `jacoco.exec` di tutti i moduli e misura ogni modulo con i dati di tutti (il rapporto aggregato di JaCoCo non serve: vede solo le dipendenze dirette di `13-foresta-app-swing`). L'ho calcolata con un piccolo programma sopra `org.jacoco.core`, che non è nel progetto. Il totale è di 35.707 righe coperte su 42.682.
- Le righe qui sopra sono quelle di `wc -l`, vuote e commenti compresi. A parte, il progetto ha 135 file `.properties` (6.249 righe), 47 grammatiche e altre risorse `.txt` e 68 documenti `.md` (8.055 righe non vuote).

**Rispetto all'assessment del 2026-10-25**

| | Allora | Adesso |
| :--- | ---: | ---: |
| Moduli | 29 | 33 |
| Codice (file / righe) | 838 / 109.546 | 1059 / 132.968 |
| Test eseguiti | 2291 | 3633 |
| Copertura con tutti i test | 77% | 84% |
| `12-foresta-swing` (righe di codice) | 23.545 | 9.362 |
| `12-foresta-ui-comune` (righe di codice) | non c'era | 24.481 |

La differenza più grande non è la crescita ma lo spostamento: quasi tutta la logica di interfaccia che stava in Swing è passata a `12-foresta-ui-comune`, e libGDX ne riusa la parte che non disegna.

Le classi più grandi (escluse le tabelle dei font bitmap `DoomdarkFont*`):

| Classe | Modulo | Righe | Allora |
| :--- | :--- | ---: | ---: |
| `motore/Automa` | motore | 2615 | 2415 |
| `testi/GrammarBean` | testi | 2563 | 2563 |
| `locazioni/LocazioneBase` | motore | 2273 | 2188 |
| `tipi/CostantiPersonaggi` | personaggi | 1577 | 1559 |
| `ui/SchermoDelGioco` | ui-comune | 1434 | era in `DisplayableCanvas` (1781) |
| `missioni/MissioneAPassi` | missioni | 1361 | 1301 |
| `ui/schermate/EditorDelleTattiche` | ui-comune | 1310 | era in `DisplayableCanvasTattiche` (1203) |
| `combattimento/CalcolatoreCombattimento` | combattimento | 1302 | 1326 |
| `personaggi/PersonaggioBase` | personaggi | 1162 | 2054 |
| `tipi/TipoMissione` | missioni | 1013 | 1013 |

## 3. Architettura

### La catena dei moduli

Dal basso in alto, con il livello che porta il nome del modulo. Le dipendenze sono quelle dichiarate nei `pom.xml`:

| Livello | Moduli |
| ---: | :--- |
| 0 | `serializzazione`, `strumenti` |
| 1 | `eventi`, `punteggi`, `testi` |
| 2 | `attributi`, `comandi`, `effettidistato`, `mappa`, `notizie` |
| 3 | `danni`, `tutorial` |
| 4 | `artefatti` |
| 5 | `personaggi` (con i gruppi) |
| 6 | `bestiario`, `combattimento`, `statistiche` |
| 7 | `intermezzi`, `mondo`, `poteri` |
| 8 | `bottino`, `economia`, `tattiche` |
| 9 | `missioni` |
| 10 | `modellodati`, `trofei` |
| 11 | `motore-api` |
| 12 | `motore`, `ui-comune`, `swing`, `libgdx` |
| 13 | `app-swing`, `app-libgdx` |

**Giudizio:** il livello di `swing` e `libgdx` non rispetta la regola di `CLAUDE.md` (0 se non dipendono da nessuno, altrimenti 1 più il livello più alto fra le dipendenze dirette). Dipendono solo da `ui-comune`, che è al 12: per la regola sarebbero al 13, e le due applicazioni al 14. Gli altri 31 moduli la rispettano. O si rinominano quattro moduli (cartelle, `artifactId`, documenti, riferimenti), o si scrive nella regola che la UI sta tutta al 12. Decide l'utente.

### Punti di forza

- **Le dipendenze le controlla il compilatore.** Ogni parte del gioco è un modulo con i suoi tipi (`tipi`), i suoi dati (`modellodati`) e i suoi contratti (eventi, viste). Quello che il compilatore non vede, cioè `java.awt`, Swing e `com.badlogic` fuori dai moduli che li possono usare, `Locale.ROOT`, `System.exit` e il `Logger`, lo controllano i test che leggono i sorgenti (`DipendenzeDalToolkitGraficoTest`, `ConvenzioniDiPortabilitaTest`). `System.exit` compare solo in `Main`.
- **Le dipendenze vanno dove devono.** Quando una parte deve sapere qualcosa di una parte che sta sopra, la riceve senza conoscerla: `FonteDiIntermezzi`, `FabbricaIntermezzi.registra`, `RegistroMissioni.setFornitoreLocazioneCompleta`, e i `setFornitoreDiDati` che `ModelloDati.installa()` mette al loro posto.
- **La UI legge da viste e scrive con eventi, e ora la logica di interfaccia è una sola.** `12-foresta-ui-comune` ha lo schermo di gioco (`SchermoDelGioco`), il router degli input, i modelli della barra delle icone (compreso il Dock, ora in entrambi gli orientamenti), i riquadri, le schermate e il tutorial (`TutorDelGioco`, il fumetto e il bordo `ContornoDiRettangoli`); Swing e libGDX misurano e disegnano soltanto.
- **Un solo thread per la logica.** Il bus consegna sul thread principale, dove girano anche i tick del temporizzatore.
- **Salvataggi di partita.** `LettoreCampi` conserva i campi vuoti, un file troncato dà `IOException`, e la scrittura passa da un file temporaneo. Ogni parte salva i suoi dati con il suo `…MD`. Il registro del tutorial non si salva su file (il gestore è commentato in `AvvioDelMotore`).
- **Testabilità.** C'è `Dado` con seme e trucchi per contesto, e c'è `PartitaDiTest`; ci sono 76 classi di test di scenario. Tutti i test girano senza UI.
- **Ogni parte si prova nel suo modulo.** Nell'assessment precedente le missioni avevano il 9% di copertura dai propri test, l'economia il 9%, i poteri il 16%: oggi sono tutte al 98-100%.

### Problemi, in ordine di priorità

1. **Documentazione che invecchia.** Vedi il §6: la divisione in moduli, il tutorial e libGDX non sono arrivati nei documenti trasversali, e `CLAUDE.md` ha percorsi che non esistono più. Chi segue un documento per trovare dove sta una cosa finisce spesso nel modulo sbagliato.
2. **Classi grandi.** `Automa` (2615 righe), `LocazioneBase` (2273) e `GrammarBean` (2563) sono le tre che superano le 2000, e `Automa` e `LocazioneBase` sono cresciute dall'ultima volta. **Giudizio:** i tagli naturali sono gli stessi di allora: altri sotto-automi in `Automa` (creazione del gruppo, combattimento, fine partita) e il combattimento a round fuori da `LocazioneBase`. Come ha mostrato `PersonaggioBase` (dimezzato), con i moduli il taglio è più facile.
3. **Stato statico.** Lo stato della partita vive in registri e singleton (4 classi con `getIstanza()` e una ventina con un metodo statico `azzera`/`reimposta`/`reset`: `GruppoGiocatore`, `RegistroMissioni`, `Cassa`, `Foresta`…). **Giudizio:** accettabile per un gioco single-player, a patto di non aggiungere registri statici.
4. **Residui della divisione.**
   - `10-foresta-modellodati` contiene solo `ModelloDati`.
   - `11-foresta-motore-api` dichiara `01-foresta-punteggi` e `10-foresta-modellodati` ma non ne usa le classi (lo dice `dependency:analyze`): servono solo a farle arrivare in transitiva a `motore` e `ui-comune`. Con le dipendenze "in vista" dovrebbero dichiararle loro.
   - Il livello di `swing` e `libgdx` (§3).
5. **Pacchetti divisi fra moduli.** `tipi` sta in 22 moduli, `eventi` in 18, `modellodati` in 17, `eventi.interni` in 14, `interfacce` in 9. È una convenzione voluta (CLAUDE.md), e con Java 8 e su Android funziona. Ma non è compatibile con i moduli di Java 9 (JPMS), se un giorno servissero.
6. **libGDX non è provata.** Compila e ha test sul modello, ma `12-foresta-libgdx` ha il 6% di copertura e `foresta-android.md` dice che il tocco non è stato provato sistematicamente.

## 4. Qualità del codice

- **Convenzioni controllate dai test**:
  - niente toolkit grafico fuori dai moduli della UI, e niente `com.badlogic` fuori dai due moduli libGDX;
  - `Locale.ROOT` nelle chiamate che dipendono dalla lingua;
  - niente `System.exit` sotto `13-foresta-app-swing`;
  - il `Logger` con il fornitore del testo;
  - le chiavi dei `.properties` delle enum e la localizzazione delle stringhe (`ConvenzioniDiLocalizzazioneTest`).
- **`TODO`/`FIXME`:** sono 4, tutti nel codice di produzione e su funzioni future o rifiniture (`LineaTemporale`, due in `TatticheBase`, uno in `LocazioneBase`). Nell'assessment precedente erano 14; il conto di allora includeva ogni parola che contiene «todo», come «METODO».
- **Test saltati:** i 31 saltati sono simulazioni da lanciare a mano (per esempio `AnalisiAbilitaInnateTest`), non test rotti.
- **Rapporti stantii: successo oggi.** Un test (`DisplayableCanvasBestiarioTest`) falliva perché in `12-foresta-swing/target/classes` c'erano ancora i `ui_*.properties` di quando i testi della UI stavano in Swing (la copia vecchia, senza i sottotitoli del bestiario, oscurava quella nuova di `12-foresta-ui-comune`). Un `mvn clean` l'ha risolto. Inoltre nella radice ci sono **30 cartelle** con i nomi vecchi dei moduli (`foresta-artefatti`, `foresta-swing`…) che contengono solo `target/` e non sono tracciate da git: sono scarti di prima della numerazione dei livelli.
- **Codice usato solo dai test:** pochi pezzi, elencati nel §5.

## 5. Difetti aperti

### 5.1 Ricontrollati sul codice del 2026-11-09 (tutti ancora aperti)

Sono tutti di gravità bassa. I percorsi sono relativi a `…/` del modulo indicato.

- **B35a (`05-foresta-personaggi`)** `PersonaggioBase.getQuantitaEffettoDiStato` (e quella di `Personaggio`) la usano solo i test.
- **B35d (`09-foresta-missioni`)** Due passi pronti di `MissioneAPassi` li usano solo i test: `evitaCombattimento` e `getPassiConIntermezzoInAttesa`.
- **B35e (`01-foresta-testi`)** In `locande.txt` la produzione `GESTORE_NOTE` non è referenziata da nessuna parte. Le produzioni di `fiabe.txt` (`FIABA`, `GIORNO_DELLA_SETTIMANA`, `OTTENUTO`…) non le ho ricontrollate: il codice le chiede anche con nomi costruiti a runtime, e uno script che cerca i nomi dà troppi falsi positivi.
- **B41c (`12-foresta-swing`)** `ui/testo/DoomdarkTextRectangle` (quello non 2x) lo usa solo il suo test.

### 5.2 Nuovi

- **`ImmagineBestiario` (`12-foresta-swing`)** è diventata un alias sottile di `ContenutiDelBestiario` (in `ui-comune`) e la usano solo i test. Ma `CLAUDE.md`, `bestiario.md` e `motore_grafico.md` dicono ancora che compone le due immagini del bestiario.
- **`CalcolatoreCombattimento.statiCausabili`, `statiTolti` e `moltiplicatoreAtteso…`** (`06-foresta-combattimento`) li usa solo la classe stessa: le tattiche ancora non li usano, come dice `interazioni_effetti_di_stato.md`.
- **Le dipendenze di `11-foresta-motore-api`** sopra (§3, problema 4).
- **Le 30 cartelle con i nomi vecchi** (§4).

### 5.3 Ipotesi controllate e scartate nella revisione del 2026-10-19 (non ricontrollate)

- **Concorrenza delle collezioni.** Non c'è nessuna `ConcurrentModificationException` raggiungibile: si scorrono sempre copie, e il bus usa `CopyOnWriteArrayList`.
- **Ricompense ed esperienza.** Niente doppie ricompense o doppi completamenti di missione, niente esperienza duplicata.
- **Oggetti e prezzi.** Scambi, acquisti, vendite e prelievi non duplicano né perdono artefatti. La vendita arriva al massimo al 75%, l'acquisto parte almeno dall'80%.
- **`Dado` e trucchi.** I limiti sono coerenti, e un trucco fuori intervallo viene ignorato.
- **Cache delle scritte** (`ImageCache.cacheDinamica`, `WeakReference`). Misurata: ricostruisce dallo 0,3 al 2,6% delle scritte. Su telefono potrebbe convenire passare a `SoftReference`.
- **Ordinali di `Numeri`.** Valgono da 1 a 41, e nessun chiamante esce da lì.
- **Combattimento.** Niente round infiniti, e la fine del combattimento si intercetta sempre.

## 6. I documenti sono allineati al codice?

### 6.1 Come è stato fatto

- **Controlli meccanici, fatti da me su tutto il progetto:** i moduli e le loro dipendenze nei `pom.xml` contro la sezione «Dipendenze» di ogni documento di modulo e contro la tabella di `CLAUDE.md`; i nomi di classi, metodi, costanti e file citati fra apici nei `.md`, cercati nei sorgenti e nelle risorse; i percorsi di `CLAUDE.md`, `foresta.md` e `didattico.md`; i conteggi (test, moduli, righe, costanti delle enum più citate).
- **Lettura dei documenti, fatta da quattro agenti in parallelo** (uno per gruppo di documenti, in sola lettura), ognuno con il compito di confrontare le parti che descrivono lo stato attuale con il codice. Ho ricontrollato a mano circa venti dei loro rilievi (i più gravi e un campione dei numeri) e si sono confermati (in un conteggio il mio differiva di un'unità, e ho usato il mio); il resto lo riporto sulla base delle loro righe, che citano file e riga. Di sei documenti la lettura è stata parziale: `GrammarBean.md`, `artefatti_e_incantamenti.md`, `testi_delle_missioni.md`, `personaggi_giocabili.md`, `tattiche_di_combattimento_automatiche.md` e `assessment_portabilita.md` (di quest'ultimo la parte sulla UI e sull'architettura).
- **Fuori dal controllo:** `tipiDanno.md` e `tipiPersonaggio.md` (appunti generici sul genere fantasy, non descrivono il codice) e `revisione_documentazione.md` (diario).

### 6.2 Cosa è allineato

- **I 33 documenti di modulo esistono, uno per modulo, e nessun documento è senza modulo.** Il livello nel nome e l'`artifactId` coincidono con la cartella.
- **La sezione «Dipendenze» è uguale ai `pom.xml`** (righe «Dichiarate nel pom» e «Solo per i test») in 32 documenti su 33; sbaglia `12-foresta-motore.md` (§6.3). Le righe «Lo dichiarano» hanno invece sette scarti in sette documenti: mancano i moduli nati di recente (`03-foresta-tutorial` in `00-foresta-serializzazione` e `02-foresta-comandi`; `12-foresta-libgdx` in `12-foresta-ui-comune`; `13-foresta-app-libgdx` in `12-foresta-motore`), `08-foresta-tattiche` e `11-foresta-motore-api` danno `12-foresta-swing` al posto di `12-foresta-ui-comune`, e a `05-foresta-personaggi` mancano quattro dichiaranti (`07-foresta-mondo`, `08-foresta-economia`, `08-foresta-tattiche`, `10-foresta-trofei`, forse alcuni solo come `test-jar`).
- **Letti e trovati allineati**: `00-foresta-serializzazione`, `00-foresta-strumenti` (salvo una nota), `01-foresta-punteggi`, `01-foresta-testi`, `02-foresta-attributi`, `02-foresta-comandi`, `02-foresta-effettidistato`, `02-foresta-mappa`, `02-foresta-notizie`, `06-foresta-statistiche`, `07-foresta-mondo`, `07-foresta-poteri`, `08-foresta-bottino`, `08-foresta-economia`, `10-foresta-trofei`, `10-foresta-modellodati`, `11-foresta-motore-api` (salvo una voce di `FaseDiGioco`), `13-foresta-app-libgdx`, `README.md`, `economia.md`, `carta_forbici_sasso.md`, `pagina_degli_incantesimi.md` (solo i riferimenti), `missione_a_tappe.md` (nella parte di grammatica). Nei documenti diario (`interazioni_effetti_di_stato.md`, `modificatori_temporanei.md`, `artefatti_e_incantamenti.md`) le sezioni sullo stato attuale tornano: 34 reazioni, il calcolo dei danni, le costanti dei prezzi e della fusione.
- **Falsi allarmi del controllo sui nomi**, che non sono errori: segnaposto e pseudocodice (`CLASSE_RUOLO`, `PARAM_X`, `getXxxMD`, `ClasseEvento`), nomi di librerie esterne (`CloudGenerator` di libGDX, `FrameBuffer`, `ShapeRenderer`…), esempi di sintassi (`AnimazionePioggia`), prefissi con «…» (`entraInStato…`, `RichiestaSelezione…`), storia dichiarata come tale (`bardo.md`, «ha sostituito»), e un comando JUnit (`DisabledCondition` in `economia.md`).

### 6.3 Cosa non tornava (e che cosa è stato corretto)

Questo è l'elenco dei rilievi della valutazione. **Il 2026-11-09, subito dopo, sono stati corretti** (verificando ogni nome e numero nel codice, non sulla fiducia degli agenti: dove un rilievo era sbagliato, per esempio «`impostaAzioni` non esiste più», non l'ho applicato):
- `CLAUDE.md`, `didattico.md` e `foresta.md` (la struttura, i diagrammi, le tabelle dei moduli con le dipendenze uguali ai `pom.xml`, i percorsi, le ricette, i numeri), e le righe delle dipendenze dei documenti di modulo;
- i documenti trasversali: `motore_di_gioco.md` (mappa dei pacchetti e dei moduli, separazione dalla UI, stati, Foresta, compagni, effetti di stato, incantesimi), `motore_grafico.md`, `12-foresta-swing.md`, `12-foresta-ui-comune.md`, `12-foresta-libgdx.md`, `porting_libGDX.md`, `tutorial.md`, `tutorial_tattiche.md`, `03-foresta-tutorial.md`, `tattiche_internals.md` e `tattiche_di_combattimento_automatiche.md` (dove stanno i dati e la logica);
- i rilievi di dominio: `01-foresta-eventi.md` (134 costanti), `GrammarBean.md`, `artefatti_e_incantamenti.md` §7, `intermezzi.md`, `gestione_missioni.md`, `passi_missioni.md`, `09-foresta-missioni.md`, `05-foresta-personaggi.md`, `04-foresta-artefatti.md`, `08-foresta-tattiche.md`, `06-foresta-bestiario.md` e `bestiario.md`, `06-foresta-combattimento.md`, `03-foresta-danni.md`, `debolezze_e_resistenze.md`, `nomi.md`, `07-foresta-intermezzi.md`, `strumenti_di_prova.md`, `personaggi_giocabili.md`, `foresta-android.md`, `todo.md`, `missione_a_tappe.md`, `12-foresta-motore.md`, `10-foresta-modellodati.md`, `11-foresta-motore-api.md`, `02-foresta-notizie.md`, `00-foresta-strumenti.md` e `13-foresta-app-swing.md`;
- `assessment_portabilita.md`: ha solo un blocco di aggiornamento in testa (non è riscritto).

Qui sotto il testo originale dei rilievi, per memoria; nel §6.4 quel che resta aperto.

**`CLAUDE.md`** (il documento che si legge per primo):
- la tabella dei moduli ha la riga di `12-foresta-ui-comune` e quella di `12-foresta-swing` sulla stessa riga (`||`): `swing` non risulta in tabella;
- la colonna «Dipende da» mescola dipendenze dirette e transitive, e non registra `comandi` per `bestiario` e `poteri`, né `tutorial` per `motore` e `ui-comune`;
- `RegistroMissioni.setRifornitoreDelleBotteghe` non esiste più (c'è `setFornitoreLocazioneCompleta`); lo cita anche `09-foresta-missioni.md` e `foresta.md`;
- percorsi vecchi: `…/artefatti/tipi/TipoArtefatto` (ora `…/tipi/`), `…/effettidistato/EffettoDiStatoMD` e `…/modificatori/ModificatoreAttributoMD` (ora `…/modellodati/`), `motore/RegoleEquipaggiamento` (ora `personaggi/`, in `05-foresta-personaggi`), `…/ui/DisplayableCanvasRiquadroSfida` (ora `…/ui/riquadri/`), `…/ui/immagini/MappaBottinoImmagine` (sta in `…/tipi/` di `12-foresta-swing`);
- la ricetta dell'artefatto con nome e immagine propri manda a «un gif in `12-foresta-swing/…/img/oggetti/`»: le immagini sono PNG in atlanti, in `12-foresta-ui-comune/…/img/oggetti/` (`atlante_oggetti.png` e `.txt`);
- `ImmagineBestiario` compare come la classe che compone le immagini del bestiario (§5.2);
- il tutorial non ha una riga nella tabella «Dove trovare le cose» e il fumetto (`DisplayableCanvasTutorial`, `TutorialGdx`) non è nella sezione «Schermate».

**I documenti che raccontano ancora un'altra epoca** (gravità alta: chi li segue sbaglia modulo o crede che manchi cose che ci sono):

| Documento | Cosa non torna |
| :--- | :--- |
| `assessment_portabilita.md` | «Stato al 2026-10-26… non esiste ancora nessun modulo Android o libGDX»; la sezione sulla UI Swing ha numeri vecchi (`DisplayableCanvas` 1788 righe, oggi 676; `TracciatoreLogo` 1443, oggi 454; 99 file, oggi 85; «327 immagini gif», oggi PNG in atlanti) e le decisioni già prese (modulo libGDX, modulo comune, prototipo) sono ancora presentate come aperte |
| `tutorial.md` | riga 3 «piano, niente codice ancora»; i suggerimenti sono contati 15, 17, 20, 21 in punti diversi (sono 31); fra i «Da fare» ci sono cose fatte (dotazione all'eremita, nome del modulo, testi); i punti operativi parlano ancora di scheletro e chimera drago, tolti il 2026-11-09 |
| `motore_di_gioco.md` | la mappa dei pacchetti mette tutto nel motore (oggi `Foresta` è in `02-foresta-mappa`, `LineaTemporale` in `06-foresta-statistiche`, i gruppi in `05-foresta-personaggi`, `Dado` in `00-foresta-strumenti`, `trofei` nel modulo 10); la foresta ha 3 categorie di locazione (non 4, con `MISSIONE_SECONDARIA`) e la costruisce `CostruttoreDellaForesta`, non `Foresta.reimposta()`; il popolamento descrive «5 guerrieri, 4 ladri… Fra' Stornato e Sorella Intronata» mentre `RegistroPersonaggi` crea 4 guerrieri, 4 ladri e due per ogni altra classe, compresi Chierico e Druido, che il documento non nomina; 18 effetti di stato (sono 21); `tipi.ClasseIncantesimo` (è `TipoIncantesimo`); `FornitoreMissione` e `InternoStatoDiGioco` non esistono più |
| `motore_grafico.md` | «tutto il rendering è Java2D/Swing/AWT»: c'è anche libGDX; dice che `GestoreMouse` fa da router (è `RouterDegliInput`, nel comune; `GestoreMouse` traduce solo gli eventi AWT); la barra icone è descritta come classe di Swing, ma il modello è `BarraIcone` in `ui-comune`; `TipoIcona.getIcona`/`componi` e `impostaAzioni` non esistono più; il tutorial è citato solo di passaggio; mancano bestiario, grimorio, tattiche e scelta della classe fra le schermate |
| `12-foresta-swing.md` | dice che `TipoFinestra`, `TipoIcona` e `Orientamento` stanno nel pacchetto `tipi` di Swing (sono in `ui-comune`); la tabella dei pacchetti non ha `tutorial`; `JoystickVirtuale` è nel comune (in Swing c'è `JoystickVirtualeAwt`); «`DisplayableCanvas` 1800 righe» (sono 676) |
| `tattiche_internals.md`, `tattiche_di_combattimento_automatiche.md` | dicono che la logica delle tattiche sta in `12-foresta-motore` e i dati in `10-foresta-modellodati`: sta tutta in `08-foresta-tattiche` (nel motore restano `LocazioneBase` e `Automa`, e il modulo 10 dipende da `08`); mancano molte azioni (Martello sacro, Attacco alle spalle, le abilità di Chierico e Druido, le sei globali) e le pozioni sono cinque, non quattro; i criteri di scelta dell'incantesimo sono cinque |
| `03-foresta-tutorial.md` | «Da fare» che sono fatti (la UI col fumetto, il pollice, l'annulla, `ForestaUI` che mette insieme le situazioni); `PAGINE_MASSIME` è 12 (il documento dice 6) e la lista dei luoghi ha 12 pagine; non cita `NotificaMessaggioDelTutorial`, l'unico evento del modulo (la pubblica l'`Automa`) |
| `tutorial_tattiche.md` | §1 dice che nessun suggerimento si apre nella schermata delle tattiche (c'è) e §4-5 propongono tre suggerimenti separati: è uno solo, di dodici pagine |
| `intermezzi.md` | «due momenti di innesco» (`MomentoIntermezzo` ne ha 8 o 9); le risorse sarebbero in `12-foresta-swing/…/img/` (stanno in `12-foresta-ui-comune`); `IntermezzoDiProva` non esiste |
| `GrammarBean.md` | `ProductionModeEnum` con `RANDOM`/`FIRST`/`LAST` (è `TipoModalitaProduzioneTesti` con `CASUALE`/`PRIMO`/`ULTIMO`); due messaggi d'errore che non esistono più; l'esempio d'uso cita `artefatti.txt` fuori dal progetto; «161 test e una grammatica di ~1300 righe» (258 test, 2563 righe) |
| `artefatti_e_incantamenti.md` | §7 mette `artefatti2.txt` e `GrammaticaArtefatti` in `12-foresta-motore`: stanno in `04-foresta-artefatti` |
| `01-foresta-eventi.md` | «129 costanti» di `TipoEvento` (sono 134: 9 comandi, 48 notifiche, 17 richieste, 60 interni; corretto) |
| `foresta.md`, `didattico.md` | 30 e 29 moduli (sono 33); 19 e 18 effetti di stato (21); «1705 test» e «2145 test» (3633); «715 file e 96.500 righe, il motore 55.000» (1059 file e 132.968 righe, il motore 10.487); classi giocabili senza Chierico e Druido; UI solo Swing; la tabella dell'architettura di `foresta.md` è malformata; `oggetti.leggendari` (è `artefatti.leggendari`); `foresta.md` non elenca `foresta-android.md` e `porting_libGDX.md` |
| `gestione_missioni.md`, `passi_missioni.md`, `09-foresta-missioni.md` | `TipoMissionePredefinita`, `FornitoreMissione` e `tipi.ClasseMissione` non esistono più (è `MissionePredefinita`); le «due missioni di recupero» sono una riga `TIPO=RECUPERO` di `IncaricoDiCombattimento`; «i testi sono in corso di spostamento»: il porting è finito; `leggendari.txt` non è in `12-foresta-motore` (sta in `04-foresta-artefatti`); `MissionePredefinita` ha 52 costanti, non 49 |
| `12-foresta-motore.md` | `Stato` ha 54 costanti, non 51; dice che `AvvioDelMotore` passa `GestoreSuggerimentiSuFile` al registro (la riga è commentata); non nomina il tutorial fra le dipendenze dichiarate nel pom |

**Gravità media o bassa** (un nome, un numero, una riga):
- `05-foresta-personaggi.md`: `TipoIncantesimo` ha 16 formule (il documento ne conta 10, senza le sei globali), `PortataIncantesimo` 5 valori, le famiglie sono quattro e non tre, a `TipoMotivoNelGruppo` manca `MISSIONE`.
- `08-foresta-tattiche.md`: `CriterioIncantesimo` ha 5 valori (manca `PIU_ECONOMICO_CHE_BASTA`); `TatticheBase` ha 554 righe.
- `06-foresta-bestiario.md` e `bestiario.md`: le voci sono 33 (25 mostri più 8 ruoli), non 32; `bestiario.md` mette `GestoreBestiario` in `11-foresta-motore-api` (sta in `06-foresta-bestiario`).
- `debolezze_e_resistenze.md`: la tabella non ha Chierico e Druido (che hanno regole) né l'immunità dell'Arpia all'aria.
- `04-foresta-artefatti.md`: `TipoSlotArtefatto` ha 9 valori, con `NUCLEO`.
- `03-foresta-danni.md`: dice che le tattiche guardano `statiCausabili`; non è così (§5.2).
- `nomi.md`: la generazione dei residenti sta in `CostruttoreDellaForesta`/`PopolatoreDelMondo`, non in `Foresta.reimposta()`; il conteggio di due liste di nomi è di uno o due fuori.
- `12-foresta-ui-comune.md` e `12-foresta-libgdx.md`: «Da fare» già fatti (estrarre da Swing la barra, `TipoIcona`, gli sprite; l'input a tocco), «gli sprite arriveranno» (arrivati), FitViewport 1142×656 (è quello delle pagine di prova).
- `porting_libGDX.md`: la riga 98 dice che il Dock non ha senso sul tocco, e il §9 dice il contrario; la tabella 12bis elenca come «da estrarre» classi già nel comune (con «1203 righe» per `DisplayableCanvasTattiche`, oggi 212).
- `todo.md`: le voci sul tutorial sono già in gran parte fatte; «sedici TODO sparsi» (sono 4); parla di gif per le immagini.
- `foresta-android.md`: tra le cose da fare c'è «schermo sempre acceso», ma `AndroidLauncher` imposta `useWakelock`; «945 file» non è riproducibile.
- `strumenti_di_prova.md`: la tabella dei contesti di lancio non ha `EFFETTO_DI_STATO_DELL_ARMA`; `TrucchiDiSviluppo` sta in `00-foresta-strumenti` e non nel «modulo contratti».
- `personaggi_giocabili.md`: `TipoIcona.getIconaInRicarica` non esiste (è `IconeAwt.iconaInRicarica`).
- `10-foresta-modellodati.md` e `11-foresta-motore-api.md`: una voce di troppo o di meno in un elenco (`Notizie.setFornitoreDiDati`, `FaseDiGioco.LINGUA`).
- `13-foresta-app-swing.md`: non cita `ConvenzioniDiLocalizzazioneTest`, `AnteprimaSchermate` e `AnteprimaIntermezzo`.

**Diari nei documenti.** `artefatti_e_incantamenti.md`, `interazioni_effetti_di_stato.md`, `modificatori_temporanei.md`, `testi_delle_missioni.md`, `gestione_missioni.md` e `tutorial.md` portano lunghe sezioni «fatto (data)», che sono la storia del lavoro e citano nomi sparito. `CLAUDE.md` dice che i documenti descrivono lo stato attuale e che la storia è in git: queste sezioni sono la ragione per cui così tanti nomi non si trovano più nel codice.

### 6.4 Cosa resta da correggere

- **`assessment_portabilita.md`**: le sezioni da «La UI Swing» in poi hanno ancora i numeri e le decisioni «da prendere» di prima del porting (c'è solo il blocco di aggiornamento in testa); andrebbe riscritto come analisi dello stato, o ridotto.
- **`tattiche_internals.md` e `tattiche_di_combattimento_automatiche.md`**: le tabelle e le sezioni sulle azioni (§3.1, §5, §7.2, §11.9) non nominano molte azioni di `TipoAzione` (Martello sacro, Attacco alle spalle, le abilità di Chierico e Druido, le sei globali) e i criteri di scelta dell'incantesimo sono cinque, non quattro (`PIU_ECONOMICO_CHE_BASTA`).
- **`testi_delle_missioni.md`**: i passi §6 descrivono ancora i `.properties` come esistenti; è il materiale storico del porting.
- **Omissioni minori**: `01-foresta-testi.md` (`Elenchi`, `Preposizione`, `RegoleConEccezioni`, `Testi.serie`), `02-foresta-attributi.md` (`potenzialeDiCarico`, `CRITICO_MASSIMO`), `bestiario.md` (`RegistroBestiario.sbloccaInSilenzio`), `intermezzi.md` (alcuni metodi del riferimento rapido, l'opzione `--compagni` dell'anteprima), `07-foresta-intermezzi.md` (`GruppoInScena`), `carta_forbici_sasso.md` (i file delle immagini sono nell'atlante `fondi`), `nomi.md` (due conteggi di nomi sono di uno o due fuori), `GrammarBean.md` (l'esempio d'uso con `fiabe.txt` e `oroscopo.txt`, «~1300 righe»), `artefatti_e_incantamenti.md` (l'intestazione datata 2026-09-25), `modificatori_temporanei.md` §7 (`descrizioneBreve` non c'è: il testo lo dà `TestoDeiModificatori`).
- **I diari**: le sezioni «fatto (data)» di `artefatti_e_incantamenti.md`, `interazioni_effetti_di_stato.md`, `modificatori_temporanei.md`, `testi_delle_missioni.md`, `gestione_missioni.md` e `tutorial.md` non sono state tolte (vedi sopra).

## 7. Portabilità (riassunto)

Il dettaglio è in [`assessment_portabilita.md`](assessment_portabilita.md), che però è fermo al 2026-10-26 (§6.3): va letto come l'analisi di partenza, non come lo stato.

- **Fatto da allora:**
  - esiste `12-foresta-libgdx` (l'interfaccia in libGDX, con la stessa partita di Swing), con `13-foresta-app-libgdx` per il desktop e `foresta-android/` (Gradle, fuori da Maven) per Android;
  - la logica d'interfaccia che non disegna è in `12-foresta-ui-comune`, senza `java.awt`, Swing né libGDX (lo controlla `DipendenzeDalToolkitGraficoTest`);
  - la barra delle icone, il Dock (anche col dito e in verticale), il tutorial e i gesti sul tocco sono nel comune.
- **Già pronto, come prima:** nessun modulo sotto la UI usa il toolkit grafico; la cartella dei file si può iniettare; l'uscita dal gioco è affidata alla piattaforma (`NotificaUscitaDalGioco`); i contenuti (storia, finali, testi delle enum, grammatiche) stanno sotto la UI.
- **Ancora aperto:**
  - le costanti di bilanciamento sono codice (`Costanti*`), non dati;
  - libGDX e Android non sono provati a schermo da script (`12-foresta-libgdx` ha il 6% di copertura), e il tocco non è stato provato sistematicamente;
  - iOS e la famiglia Apple non hanno ancora un lanciatore.

## 8. Rischi tecnici

| Rischio | Fatto | Gravità |
| :--- | :--- | :--- |
| Documenti che mandano nel posto sbagliato | `CLAUDE.md` e una ventina di documenti avevano percorsi, numeri e descrizioni di prima della divisione (§6): corretti il 2026-11-09 quasi tutti; restano i punti del §6.4 | Bassa dopo le correzioni, ma senza un controllo automatico tornerà a salire |
| Copertura bassa dei toolkit | `12-foresta-libgdx` 6%, `13-foresta-app-swing` 30%, `12-foresta-swing` 61%, `12-foresta-ui-comune` 72% con tutti i test | Media: la parte che disegna si prova poco, e libGDX non si prova senza GL |
| Risorse e classi stantie in `target/` | Oggi un test falliva per una copia vecchia dei testi della UI; nella radice ci sono 30 cartelle `target/` dei nomi vecchi | Bassa: basta `mvn clean` |
| `Automa` e `LocazioneBase` | 2615 e 2273 righe, in crescita | Bassa oggi, media se continuano a crescere |
| Il livello di `swing` e `libgdx` | Non segue la regola di `CLAUDE.md` (§3) | Bassa: è una scelta da prendere |

## 9. Prossimi passi consigliati

1. **Finire di allineare i documenti** (§6.4): `assessment_portabilita.md`, le sezioni sulle azioni delle tattiche e le omissioni minori; poi tenerli allineati con un controllo automatico (vedi «Da fare»). Le sezioni «fatto (data)» che sono ormai storia vanno tolte dai documenti (CLAUDE.md dice che la storia è in git).
2. **Decidere il livello di `swing` e `libgdx`:** rinominarli (e di conseguenza le due applicazioni) o cambiare la regola.
3. **Pulizia piccola:**
   - togliere le 30 cartelle con i nomi vecchi;
   - il codice morto del §5 (B35a, B35d, B35e, B41c, `ImmagineBestiario`);
   - le dipendenze di `11-foresta-motore-api` che non usa.
4. **Tagliare le classi grandi** quando si interviene su di loro (§3, problema 2). Il candidato più naturale è il combattimento a round di `LocazioneBase`.
5. **Provare libGDX e Android** su un dispositivo e portare la copertura della parte che disegna dove serve.
6. **Bilanciamento**, come da [`todo.md`](todo.md).

## 10. Confronto con i giochi moderni dello stesso tipo (base per gli sviluppi futuri)

Appunti del 2026-11-09, ricavati dalla documentazione e dal codice, **non da partite giocate**. Il confronto è con i giochi di ruolo a turni a gruppo di oggi in generale (le tattiche programmabili di Final Fantasy XII e Dragon Age, Darkest Dungeon, Battle Brothers, Wartales, il testo generato di Caves of Qud). Serve a due cose: sapere che cosa non rovinare e scegliere dove investire.

### 10.1 Cosa ha in più (da proteggere)

- **Il sistema di tattiche.** Regole programmabili per ogni personaggio e per ogni mostro: 7 rose di bersagli, 18 filtri, 12 ordinamenti, 7 condizioni, 40 azioni (`08-foresta-tattiche`), con la spiegazione di ogni decisione. È più ricco di quanto si trovi di solito nei giochi indie.
- **Il testo generato da grammatiche** (`GrammarBean`, `01-foresta-testi`): fiabe, oroscopi, notizie satiriche sulle malefatte del gruppo, nomi, testi delle missioni, in tre lingue.
- **Danni e stati che si combinano:** 18 tipi di danno, 21 effetti di stato, 34 reazioni, debolezze e resistenze per classe.
- **Una pressione di tempo vera:** il calendario del Drago (20, 25, 30 e 35 giorni, perdita oltre il 40).
- **Un tutorial che pilota la partita** (`PilotaggioDelTutorial`): i dadi dei primi turni sono truccati perché si incontri un mostro alla volta.
- **Meta-progressione leggera:** bestiario, trofei, classifica.
- **La base tecnica:** 33 moduli, 3633 test, un giocatore automatico, partite ripetibili con il seme dei dadi, la stessa partita in Swing, libGDX e Android.

### 10.2 Cosa ha in meno (candidati allo sviluppo)

| Area | Com'è oggi | Cosa fanno i giochi moderni | Dove si aggancerebbe | Costo |
| :--- | :--- | :--- | :--- | :--- |
| **Costruzione del personaggio** | 8 classi fisse; i punti abilità vanno solo ai 7 attributi primari (`ComandoSpesaPuntoAbilita`); le abilità innate sono fisse per classe e con ricarica; chi può usare cosa lo dice `RegoleEquipaggiamento`; curva quadratica fino al livello 50 | Alberi di abilità, multiclasse, specializzazioni: il personaggio si distingue dagli altri della stessa classe | `TipoAbilita` e `AbilitaInnataDiClasse` (`07-foresta-poteri`), `GestoreProgressione`, `PersonaggioMD` (salvataggio), `TipoAzione` e `Requisiti` (perché le tattiche vedano le nuove abilità), la schermata dell'inventario | Alto: dati, UI, tattiche, bilanciamento e test; vedi il §10.4 |
| **Combattimento tattico** | Round a coppie. L'unica posizione è l'ordine del gruppo: chi sta davanti riceve i colpi, e un incantesimo a più bersagli colpisce i primi N vivi con quota 100%, 50%, 25%… (`quotaDelDannoPerPosizione`; `todo.md` ne nota il limite) | Griglie o file, copertura, movimento, bersagli d'area veri | Ordine del gruppo (`Gruppo`), `CalcolatoreCombattimento`, le rose delle tattiche | Medio: due file (fronte e retro) sono molto più semplici di una griglia |
| **Esplorazione** | Ogni casella della mappa 20×20 è un singolo incontro | Luoghi da esplorare, scelte lungo il percorso | `LocazioneBase`, `Foresta`, le missioni a tappe (`MissioneATappe`) | Medio-alto |
| **Narrazione e contenuto** | Una storia principale (il Drago e i suoi quattro alleati) e incarichi generati | Compagni con una storia, scelte con conseguenze, dialoghi ramificati | Gli intermezzi (`FonteDiIntermezzi`), `MissioneAPassi.chiediScelta` (domande a 2-5 opzioni, già esistenti), `Racconto` | Soprattutto contenuto da scrivere, non codice |
| **Presentazione** | Grafica volutamente retro, sprite di testo. **Non risultano file audio né codice di riproduzione.** | Animazioni di combattimento, musica ed effetti | `ModelloDelloSprite` e gli sprite; un'astrazione dell'audio per i due toolkit | Medio |
| **Piattaforme e accessibilità** | Niente iOS, layout per telefono e controller; lingua originale solo l'italiano (l'inglese c'è); libGDX e Android non provati in modo sistematico (copertura 6% per `12-foresta-libgdx`) | Più piattaforme, controller, opzioni di accessibilità | `porting_libGDX.md`, `assessment_portabilita.md` | Medio |
| **Estendibilità** | Le grammatiche sono già dati; il bilanciamento (`Costanti*`) è codice | Mod, dati editabili | Costanti come dati (`assessment_portabilita.md`) | Medio |
| **Rigiocabilità** | Bestiario, trofei e classifica; il seme dei dadi esiste (`Dado.impostaSeme`) ma non è un'opzione per il giocatore; dai documenti non risulta una scelta di difficoltà | Difficoltà, partite a seme condiviso, sfide giornaliere | `Dado`, `Statistiche`, le opzioni di avvio (`OpzioniDiAvvio`) | Basso-medio |

### 10.3 Un'imprecisione da non ripetere

Dire che il combattimento è «senza posizioni» è troppo: la posizione c'è, ma è una sola, l'ordine del gruppo, e decide chi riceve i colpi e chi è colpito dagli incantesimi a più bersagli. È un buon punto di partenza per un sistema a file.

### 10.4 Alberi di abilità, multiclasse e specializzazioni (discussione del 2026-11-09)

**Punto di partenza.** Una classe è un'identità fissa in tutto il codice: valori di partenza (`CostantiPersonaggi`), moltiplicatori di danno (`MoltiplicatoriDiClasse`, `TipoPersonaggio`), immunità, equipaggiamento (`RegoleEquipaggiamento`), tattica di base, voce del bestiario, immagini e forma al maschile e al femminile (`getRuolo()` le unisce). Due personaggi della stessa classe e dello stesso livello si distinguono solo per come hanno speso i punti abilità (sui 7 attributi primari) e per quel che portano addosso. Le abilità innate sono fisse per classe. Gli incantesimi sono scorte di pergamene del gruppo, non del personaggio.

**Che cosa c'è già da riusare:**
- `ModificatoreAttributoMD`: modificatori permanenti o a tempo (variazione fissa, percentuale, «porta a»). Un nodo passivo di un albero è già esprimibile così.
- `TipoAbilita` con ricarica e `TipoAzione`: un nodo attivo è una nuova abilità, che le tattiche possono usare se si aggiorna `Requisiti`.
- `CantiImparati` (`07-foresta-poteri`): un precedente di poteri acquisiti durante la partita, anche se a livello di gruppo.

**Le tre idee, dalla più economica:**
1. **Specializzazioni.** A un livello (per esempio 5 o 10) il personaggio sceglie una fra 2-3 varianti della propria classe: un campo in `PersonaggioMD`, un piccolo insieme di modificatori e una abilità in più. Si appoggia a quasi tutto ciò che c'è; va fatta sul ruolo e non sulla forma maschile o femminile della classe. È la cosa con il miglior rapporto fra valore e costo.
2. **Alberi di abilità.** Servono una seconda valuta di punti (o una divisione di quelli attuali), il formato di salvataggio, una schermata nuova nei tre moduli della UI (comune, Swing, libGDX), i testi in tre lingue, il tutorial e un bilanciamento combinatorio. Le azioni nuove allungano l'elenco di 40 azioni del `EditorDelleTattiche`. Ha senso solo se una partita dà abbastanza livelli. **Dato dell'autore (2026-11-09): una partita media arriva a circa il livello 16**, e ogni livello dà un solo punto abilità (`Progressione`): sono una quindicina di punti in tutto, pochi per un albero vero (si riempirebbe quasi tutto, senza scelte). Il massimo è 50 (`economia.md` ha le misure solo fino al livello 5).
3. **Multiclasse.** È la più costosa e la meno adatta: rompe l'ipotesi «un personaggio, una classe» in ogni modulo (immagini, immunità, equipaggiamento, bestiario, tattica di base). Lo stesso sapore si ottiene senza toccarla con abilità imparate da altre classi tramite missioni o maestri, sul modello di `CantiImparati`, e con classi ibride come l'Elfo, il Chierico e il Druido.

**Bozza delle specializzazioni** (idee del 2026-11-09, da discutere; ognuna riusa meccaniche che esistono già). Con una partita media di 16 livelli bastano due punti di scelta (per esempio ai livelli 5 e 12).

| Classe | Specializzazioni possibili | Meccaniche esistenti su cui poggiano |
| :--- | :--- | :--- |
| Mago | **Necromante** (danno necrotico, Morte, Maledizione, Scheletri); Elementalista (reazioni fra stati e tipi di danno); Controllore (Silenzio, Confuso, Spaventato, danno psichico) | `NECROMANTE` (mostro con necrotico ×0,5 e sacro ×1,25), la Mietitura (necrotico ×2; un minion non boss sotto il 20% muore e diventa uno Scheletro nel gruppo, `TipoMotivoNelGruppo.MIETITURA`), le 34 reazioni |
| Guerriero | Difensore (**serve una Provocazione, che non esiste**: vedi `todo.md`); Berserker; Duellista | l'effetto `BERSERK`, i duelli; per il Difensore c'è solo l'ordine del gruppo, che da solo non attira i colpi dei mostri |
| Ladro | Assassino (attacco alle spalle più frequente); Furfante (passare inosservato, fuga, corruzione) | `AttaccoAlleSpalle` (ricarica 3, almeno tre personaggi), passare inosservato, corruzione |
| Sacerdote, Chierico | Guaritore (alba sacra, imposizione delle mani); Inquisitore (luce divina, martello sacro, scacciare i non morti) | `AbilitaInnate`, `MartelloSacro`, danno `SACRO` |
| Druido | Protettore (pelle di corteccia, rigenerazione); Evocatore (radici, sciame) | le abilità del Druido |
| Bardo | Cantore di battaglia (canto di valore); Ipnotizzatore (canto ipnotico) | i tre canti, lo stato `ISPIRATO` |

`AbilitaInnate` decide chi ha quali abilità dal `TipoPersonaggio`, `CostoDelleAbilita` dà il costo per classe e `RegistroTattiche.azioniPossibili` offre le azioni alle tattiche per classe: sono i punti da portare a «per personaggio» per agganciare una specializzazione. Le specializzazioni non costano tutte uguale: il Necromante riusa meccaniche che ci sono, il Difensore ne chiede una nuova. Attenzione al Necromante: la luce divina del Sacerdote colpisce anche i non morti del proprio gruppo, e gli Scheletri non si curano.

**Vincolo da non perdere:** ogni abilità nuova deve poter essere una `TipoAzione`, altrimenti le tattiche, che sono il punto di forza del gioco, ne vedrebbero meno.

### 10.5 Ordine suggerito

Dal meno rischioso: (1) opzioni per la rigiocabilità (seme a scelta, difficoltà), perché toccano poco; (2) l'audio, perché è un'aggiunta isolata; (3) le due file, che si appoggiano all'ordine del gruppo; (4) una scelta di abilità a ogni livello, come primo passo verso gli alberi; (5) il resto, che chiede contenuto o più piattaforme. I punti di forza del §10.1 non vanno toccati senza un motivo: le tattiche, in particolare, dovranno poter usare ogni nuova abilità.

## Osservazioni

- **Come è stato fatto.** Rispetto all'assessment del 2026-10-25 sono rifatti da capo:
  - le misure: righe, test, copertura modulo per modulo e con tutti i test;
  - il grafo dei moduli, dai `pom.xml` (con `dependency:analyze` per le dipendenze dichiarate e non usate) e il confronto con i documenti;
  - le classi grandi, i pacchetti divisi e i difetti che restavano aperti;
  - il confronto dei documenti con il codice (§6).

  Non è stata rifatta la lettura integrale dei sorgenti.
- **Il confronto dei documenti** è un controllo di plausibilità, non una prova: i controlli meccanici sono completi (nomi, percorsi, dipendenze, conteggi), ma la lettura dei documenti è stata fatta da agenti che hanno verificato le affermazioni che si potevano trovare con una ricerca nel codice. Descrizioni di funzionamento più sottili (per esempio le percentuali di esperienza di `motore_di_gioco.md` §8, o le tabelle numeriche di `personaggi_giocabili.md`) non sono state verificate una per una. Mancano all'appello anche i testi interni delle grammatiche.
- **La copertura con tutti i test** viene da un programma di poche righe sopra `org.jacoco.core`, che unisce i `jacoco.exec` di tutti i moduli e analizza le `target/classes` di ciascuno. Il programma non è nel progetto.
- **Rispetto all'assessment precedente.** Allora i moduli erano 29, i test 2291 e la copertura complessiva 77%; adesso sono 33, 3633 e 84%. La copertura propria dei moduli sotto la UI è passata dall'intervallo 9-100% al 95-100%.

## Da fare / idee aperte

- Un test sui documenti, come quelli che già controllano le convenzioni del codice: la sezione «Dipendenze» di ogni documento di modulo contro i `pom.xml` (righe «Dichiarate nel pom», «Solo per i test», «Lo dichiarano») e l'esistenza delle classi e dei file citati fra apici (con un elenco di eccezioni per i segnaposto e per i documenti diario). Avrebbe preso quasi tutto il §6.3.
- Mettere nel progetto la misura della copertura con tutti i test, per esempio come profilo Maven o come piccolo strumento in `risorse_e_documenti_vari/`.
- Una nuova lettura integrale dei sorgenti, modulo per modulo, quando la divisione sarà finita.
- Un test che produca tutte le alternative delle grammatiche e le disegni con i font, per prendere le produzioni malformate e i caratteri che il font non ha; con lo stesso passo, decidere quali produzioni sono davvero morte (§5.1, B35e).
