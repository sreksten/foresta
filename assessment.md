# Assessment di Foresta

Valutazione del progetto al **2026-11-09**, rifatta da zero dopo la nascita dei moduli del tutorial, dell'interfaccia comune e di libGDX. Le misure sono prese sul working tree dopo `mvn clean install` e `mvn test`:
- righe e file sono contati con `wc -l` su `src/main/java` e `src/test/java` di ogni modulo; senza righe vuote né commenti il codice di produzione è di **85.844 righe**;
- la copertura viene da JaCoCo, sia modulo per modulo sia con tutti i test insieme;
- i moduli e le loro dipendenze sono letti dai `pom.xml` e confrontati con `CLAUDE.md` e con il documento di ogni modulo.

I giudizi sono segnati come **Giudizio**. I difetti ancora aperti stanno nel §5, con i codici della revisione precedente (B35, B41…), che [`todo.md`](todo.md) usa. Il §6 è nuovo: dice quanto i documenti `.md` sono allineati al codice. Le cose da fare per il porting su Android e iOS sono in [`assessment_portabilita.md`](assessment_portabilita.md): qui c'è solo il riassunto. Il §11 (2026-11-13) è il piano della **revisione del codice modulo per modulo** (possibili `NullPointerException`, parti in comune da fattorizzare, leggibilità per un neofita), da fare un passo alla volta, con il prompt da usare.

Questo assessment **non è una nuova lettura integrale dei sorgenti**. Sono rifatti da capo le misure, il grafo dei moduli, la verifica dei difetti aperti e il confronto fra documenti e codice (§6, che non è un controllo riga per riga: vedi le Osservazioni).

## 1. Sintesi

- **Il gioco è completo e giocabile** da inizio a fine, con una novità: ora c'è un tutorial di 31 suggerimenti (modulo `03-foresta-tutorial`), e l'interfaccia esiste in due versioni, Swing e libGDX (con un lanciatore Android, un progetto Gradle che Maven lancia con il profilo `android`).
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
| `testi/GestoreGrammatiche` | testi | 2563 | 2563 |
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
| 13 | `app-swing`, `app-libgdx`, `app-android` (solo con il profilo `android`, nato dopo questa valutazione) |

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
2. **Classi grandi.** `Automa` (2615 righe), `LocazioneBase` (2273) e `GestoreGrammatiche` (2563) sono le tre che superano le 2000, e `Automa` e `LocazioneBase` sono cresciute dall'ultima volta. **Giudizio:** i tagli naturali sono gli stessi di allora: altri sotto-automi in `Automa` (creazione del gruppo, combattimento, fine partita) e il combattimento a round fuori da `LocazioneBase`. Come ha mostrato `PersonaggioBase` (dimezzato), con i moduli il taglio è più facile.
3. **Stato statico.** Lo stato della partita vive in registri e singleton (4 classi con `getIstanza()` e una ventina con un metodo statico `azzera`/`reimposta`/`ripristina`: `GruppoGiocatore`, `RegistroMissioni`, `Cassa`, `Foresta`…). **Giudizio:** accettabile per un gioco single-player, a patto di non aggiungere registri statici.
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
- **Lettura dei documenti, fatta da quattro agenti in parallelo** (uno per gruppo di documenti, in sola lettura), ognuno con il compito di confrontare le parti che descrivono lo stato attuale con il codice. Ho ricontrollato a mano circa venti dei loro rilievi (i più gravi e un campione dei numeri) e si sono confermati (in un conteggio il mio differiva di un'unità, e ho usato il mio); il resto lo riporto sulla base delle loro righe, che citano file e riga. Di sei documenti la lettura è stata parziale: `GestoreGrammatiche.md`, `artefatti_e_incantamenti.md`, `testi_delle_missioni.md`, `personaggi_giocabili.md`, `tattiche_di_combattimento_automatiche.md` e `assessment_portabilita.md` (di quest'ultimo la parte sulla UI e sull'architettura).
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
- i rilievi di dominio: `01-foresta-eventi.md` (134 costanti), `GestoreGrammatiche.md`, `artefatti_e_incantamenti.md` §7, `intermezzi.md`, `gestione_missioni.md`, `passi_missioni.md`, `09-foresta-missioni.md`, `05-foresta-personaggi.md`, `04-foresta-artefatti.md`, `08-foresta-tattiche.md`, `06-foresta-bestiario.md` e `bestiario.md`, `06-foresta-combattimento.md`, `03-foresta-danni.md`, `debolezze_e_resistenze.md`, `nomi.md`, `07-foresta-intermezzi.md`, `strumenti_di_prova.md`, `personaggi_giocabili.md`, `foresta-android.md`, `todo.md`, `missione_a_tappe.md`, `12-foresta-motore.md`, `10-foresta-modellodati.md`, `11-foresta-motore-api.md`, `02-foresta-notizie.md`, `00-foresta-strumenti.md` e `13-foresta-app-swing.md`;
- `assessment_portabilita.md`: ha solo un blocco di aggiornamento in testa (non è riscritto).

Il testo dettagliato dei rilievi (documento per documento, con i nomi e i numeri sbagliati) è nella storia di git, nella versione di questo file del 2026-11-09; qui resta solo quel che è ancora aperto (§6.4). Un'osservazione vale ancora: i **diari nei documenti** (le sezioni «fatto (data)» di `artefatti_e_incantamenti.md`, `interazioni_effetti_di_stato.md`, `modificatori_temporanei.md`, `testi_delle_missioni.md`, `gestione_missioni.md` e `tutorial.md`) citano nomi che non esistono più, ed è per loro che così tanti nomi non si trovavano nel codice.

### 6.4 Cosa resta da correggere

- **`assessment_portabilita.md`**: le sezioni da «La UI Swing» in poi hanno ancora i numeri e le decisioni «da prendere» di prima del porting (c'è solo il blocco di aggiornamento in testa); andrebbe riscritto come analisi dello stato, o ridotto.
- **`tattiche_internals.md` e `tattiche_di_combattimento_automatiche.md`**: le tabelle e le sezioni sulle azioni (§3.1, §5, §7.2, §11.9) non nominano molte azioni di `TipoAzione` (Martello sacro, Attacco alle spalle, le abilità di Chierico e Druido, le sei globali) e i criteri di scelta dell'incantesimo sono cinque, non quattro (`PIU_ECONOMICO_CHE_BASTA`).
- **`testi_delle_missioni.md`**: i passi §6 descrivono ancora i `.properties` come esistenti; è il materiale storico del porting.
- **Omissioni minori**: `01-foresta-testi.md` (`Elenchi`, `Preposizione`, `RegoleConEccezioni`, `Testi.serie`), `02-foresta-attributi.md` (`potenzialeDiCarico`, `CRITICO_MASSIMO`), `bestiario.md` (`RegistroBestiario.sbloccaInSilenzio`), `intermezzi.md` (alcuni metodi del riferimento rapido, l'opzione `--compagni` dell'anteprima), `07-foresta-intermezzi.md` (`GruppoInScena`), `carta_forbici_sasso.md` (i file delle immagini sono nell'atlante `fondi`), `nomi.md` (due conteggi di nomi sono di uno o due fuori), `GestoreGrammatiche.md` (l'esempio d'uso con `fiabe.txt` e `oroscopo.txt`, «~1300 righe»), `artefatti_e_incantamenti.md` (l'intestazione datata 2026-09-25), `modificatori_temporanei.md` §7 (`descrizioneBreve` non c'è: il testo lo dà `TestoDeiModificatori`).
- **I diari**: le sezioni «fatto (data)» di `artefatti_e_incantamenti.md`, `interazioni_effetti_di_stato.md`, `modificatori_temporanei.md`, `testi_delle_missioni.md`, `gestione_missioni.md` e `tutorial.md` non sono state tolte (vedi sopra).

## 7. Portabilità (riassunto)

Il dettaglio è in [`assessment_portabilita.md`](assessment_portabilita.md), che però è fermo al 2026-10-26 (§6.3): va letto come l'analisi di partenza, non come lo stato.

- **Fatto da allora:**
  - esiste `12-foresta-libgdx` (l'interfaccia in libGDX, con la stessa partita di Swing), con `13-foresta-app-libgdx` per il desktop e `foresta-android/` (Gradle) per Android, che dal 2026-11-13 Maven costruisce con `mvn package -Pandroid` (`13-foresta-app-android`);
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
- **Il testo generato da grammatiche** (`GestoreGrammatiche`, `01-foresta-testi`): fiabe, oroscopi, notizie satiriche sulle malefatte del gruppo, nomi, testi delle missioni, in tre lingue.
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

## 11. Revisione del codice modulo per modulo (dal 2026-11-13)

### 11.1 Perché e come

Gli assessment precedenti hanno misurato il progetto ma non hanno riletto i sorgenti. Questa revisione li rilegge tutti, con tre obiettivi:

1. **Robustezza**: trovare dove può nascere una `NullPointerException` (e, già che si legge, gli altri errori che si vedono a occhio: indici fuori dai limiti, divisioni per zero, casi dimenticati di uno `switch`, risorse non chiuse).
2. **Parti in comune**: trovare il codice ripetuto che si potrebbe mettere in un posto solo (dentro un modulo o, se serve a più moduli, nel più basso che li serve tutti).
3. **Leggibilità per un neofita**: il codice deve poterlo leggere anche chi conosce Java da poco e non conosce il gioco. Dove serve si aggiungono commenti; dove il codice è criptico si spiega.

Si procede **un passo alla volta**, dal basso verso l'alto (§11.3): un passo è un modulo o un pezzo di un modulo grande, fra le 2.000 e le 6.000 righe circa, così una sessione basta e non servono decine di agenti insieme. Dal basso perché le parti in comune possono solo scendere: quando si arriva a un modulo alto si sa già che cosa offrono quelli sotto.

**Cosa si fa subito e cosa si propone.** In ogni passo:
- **i commenti si aggiungono subito** (non cambiano il comportamento);
- **le correzioni e le fattorizzazioni si propongono** all'utente, con file e riga, e si applicano solo dopo il suo ok (regole di `CLAUDE.md`: niente iniziative sul disegno, prima di cambiare la forma del codice o di cancellare si chiede);
- **il commit si fa solo su richiesta**, dopo la revisione dell'utente.

**Che cosa resta scritto qui.** Il rapporto completo di un passo (i casi controllati, le proposte una per una) va all'utente, nella conversazione. In questo documento restano solo:
- il **giudizio** su ogni parte (§11.4);
- le **migliorie ancora aperte**, cioè quelle non applicate (§11.4);
- le **decisioni** dell'utente da non riproporre (§11.5);
- le **verifiche rimandate** a un passo successivo, scritte nella riga di quel passo (§11.3).

Quel che è stato sistemato non si scrive: lo racconta il commit «Revisione del codice, passo N».

### 11.2 Il prompt di un passo

Da incollare in una sessione nuova di Claude Code, mettendo al posto di `<PASSO>` il numero del passo della tabella del §11.3:

> Esegui il passo `<PASSO>` della revisione del codice descritta in `assessment.md` §11. Leggi prima §11.1, la riga del passo nel §11.3 (che cosa entra e le verifiche rimandate a questo passo), il §11.4 (le migliorie aperte e i giudizi dei passi prima: non ripeterli) e il §11.5 (le decisioni da non riproporre). Lavora tu, leggendo i file per intero; al massimo un agente di aiuto se il passo è grande, mai più di due.
>
> Per **ogni file** del passo:
>
> 1. **`NullPointerException` possibili.** Per ogni riferimento che si usa (chiamata di metodo, accesso a un campo, `unboxing` di un `Integer`/`Boolean`, `switch` su un'enum, `for` su una collezione) chiediti se può valere `null`, e **non fermarti al file**: risali a chi lo produce (chi chiama il metodo, chi assegna il campo, cosa restituisce il metodo chiamato). Casi tipici in questo progetto: `Map.get` su una chiave che può mancare; metodi che restituiscono `null` per dire «non c'è» (`trova…`, `cerca…`, `get…OrNull`); campi dei `…MD` dopo la rilettura di un salvataggio (un campo che il file non aveva); singleton e registri usati prima di `azzera`/`installa`/`setFornitoreDiDati`; parametri che arrivano dagli eventi del bus; `Testi.get` con una chiave che manca nei `.properties`; personaggi morti o rimossi dal gruppo nel mezzo di un combattimento; liste vuote dove si prende il primo elemento. Segnala **solo i casi che puoi raggiungere davvero** (scrivi il percorso: chi chiama, con quali dati), separando i certi dai plausibili. Non proporre controlli `if (x != null)` che nascondono un errore: se `null` vuol dire che qualcosa è andato storto prima, la correzione è lì. Un sospetto si verifica (con il codice dei chiamanti, o con una prova in `jshell` sulle classi compilate) prima di chiamarlo difetto.
> 2. **Parti in comune.** Cerca il codice ripetuto: metodi quasi uguali in classi sorelle (per esempio le classi dei personaggi e dei mostri, le missioni, le schermate Swing e libGDX), blocchi copiati con piccole differenze, costanti ripetute, cicli che fanno la stessa ricerca. Per ognuno: dove sta (file e righe), che cosa avrebbe di comune, **dove andrebbe** (in quale classe e in quale modulo, rispettando i livelli: un modulo vede solo quelli sotto), quante righe si risparmiano e che cosa si rischia. Non vale la pena di fattorizzare due righe uguali, né di unire cose che si somigliano oggi ma hanno ragioni diverse per cambiare. Riusa quel che i passi prima hanno messo in comune (per esempio `strumenti.Maiuscole`, `GrammaticaPigra.riga`).
> 3. **Leggibilità per un neofita.** Immagina un lettore che conosce le basi di Java ma non le lambda, gli stream, i generici più complicati né il gioco. **Aggiungi i commenti** (in italiano, con le convenzioni di `CLAUDE.md`, virgolette “ ”):
>    - in testa a **ogni classe** che non ce l'ha, un Javadoc che dice che cosa rappresenta nel gioco, chi la usa e perché esiste (non come è fatta);
>    - sui **metodi pubblici** il cui nome non basta (che cosa restituisce, quando restituisce `null` o una lista vuota, che effetti ha sullo stato del gioco);
>    - sulle righe **criptiche**: stream lunghi (che cosa ne esce), espressioni con operatori ternari annidati, calcoli con numeri (da dove vengono e che cosa vogliono dire: per esempio una probabilità, una formula di danno), giochi di bit, ordinamenti con `Comparator` composti, ricorsioni, convenzioni del progetto che un esterno non conosce (il bus degli eventi, i fornitori di dati, il `Dado` con il suo `ContestoLancio`, i `…MD`).
>    Correggi i Javadoc che non dicono più il vero (sono il difetto più frequente trovato finora). Non commentare l'ovvio (`// incrementa i`) e non riscrivere il codice per renderlo più leggibile senza chiedere: se un pezzo andrebbe riscritto (un metodo di 150 righe, un nome che inganna, un numero senza nome), proponilo. Non cambiare nomi, firme né comportamento.
>
> Alla fine:
> - compila e lancia i test con i moduli da cui dipende (`mvn test -o -pl <modulo> -am`: senza `-am` Maven prende i jar vecchi di `~/.m2`); i commenti non devono rompere niente;
> - **non fare commit**;
> - riassumimi le proposte, una per una con il loro peso, e aspetta l'ok prima di applicarle; dopo le mie decisioni applica quelle approvate, lancia tutti i test (`mvn test -o`) e aggiorna i documenti dei moduli toccati;
> - aggiorna `assessment.md`: nel §11.3 la colonna «Stato» del passo e, nella riga di un passo successivo, le verifiche rimandate a lui; nel §11.4 una sottosezione `#### Passo <PASSO>` con il giudizio e le sole migliorie rimaste aperte; nel §11.5 le mie decisioni da non riproporre. Quel che è stato applicato non va nell'assessment ma nel messaggio di commit.

### 11.3 I passi

In ordine di livello. Le righe sono quelle di `wc -l` su `src/main/java` al 2026-11-13. I test non entrano nella revisione (si leggono solo per capire come si usa una classe). Per i moduli grandi il passo dice quali pacchetti o file entrano; «A–M» vuol dire i file il cui nome comincia con una lettera da A a M.

| Passo | Modulo | Che cosa entra (e verifiche rimandate) | Righe | Stato |
| ---: | :--- | :--- | ---: | :--- |
| 1 | `00-foresta-serializzazione`, `00-foresta-strumenti`, `01-foresta-eventi`, `01-foresta-punteggi` | tutto | 2.700 | fatto (2026-11-13) |
| 2 | `01-foresta-testi` | tutto (`GestoreGrammatiche` da solo fa 2.600 righe) | 4.600 | fatto (2026-11-13) |
| 3 | `02-foresta-attributi`, `02-foresta-comandi`, `02-foresta-effettidistato`, `02-foresta-mappa` | tutto | 3.600 | fatto (2026-11-13) |
| 4 | `02-foresta-notizie`, `03-foresta-danni`, `03-foresta-tutorial` | tutto | 1.550 | fatto (2026-11-13) |
| 5 | `04-foresta-artefatti` | tutto | 5.250 | fatto (2026-11-13) |
| 6 | `05-foresta-personaggi` | `tipi` (con `CostantiPersonaggi`), `modellodati`, `interfacce`, `eventi` | 5.700 | fatto (2026-11-13) |
| 7 | `05-foresta-personaggi` | `personaggi`: i meccanismi (`PersonaggioBase`, `Personaggio`, `SaluteEMorte`, `EffettiDiStato`, `ModificatoriTemporanei`, `AttributiSecondari`, `CaricoEInventario`, `GestoreProgressione`, `Progressione`, `RegoleEquipaggiamento`, `CalcolatoreRiposo`, `RisultatoRiposo`, `FabbricaPersonaggi`, `EquipaggiamentoIniziale`, `LanciatoreDeiDadi`, `MoltiplicatoriDiClasse`, `NomeEDescrizione`, `ArmaNaturale`, `PersonaggioNonCombattente`) e `gruppi` | 5.500 | fatto (2026-11-13) |
| 8 | `05-foresta-personaggi` | `personaggi`: le classi dei personaggi e dei mostri (tutti gli altri file, da `Alchimista` a `Viverna`): **qui le parti in comune sono il tema principale** (sono una sessantina di classi corte e simili: se non basta una sessione, A–L e M–Z). **Rimandato qui** (passo 6): ogni classe restituisce da una ventina di metodi le sue costanti di `CostantiPersonaggi` (circa 1.250 righe di blocchi uguali): valutare una tabella, una riga per classe (un oggetto `ValoriDiClasse` o un file di dati come `leggendari.txt`) | 7.550 | fatto (2026-11-13) |
| 9 | `06-foresta-bestiario`, `06-foresta-combattimento`, `06-foresta-statistiche` | tutto. **Rimandati qui:** riscrivere i Javadoc delle costanti di `TipoAttributo` (`02-foresta-attributi`) con quel che ogni attributo fa davvero, leggendone gli usi nel combattimento, e togliere l'avviso in testa all'enum (passo 3); controllare che chi chiama `TipoInterazioneConEffettiDiStato.getDannoAdArea()` sappia che è null per le reazioni senza area (passo 4) | 4.000 | da fare |
| 10 | `07-foresta-intermezzi`, `07-foresta-mondo` | tutto | 4.600 | da fare |
| 11 | `07-foresta-poteri` | tutto | 3.600 | da fare |
| 12 | `08-foresta-bottino`, `08-foresta-economia` | tutto | 3.350 | da fare |
| 13 | `08-foresta-tattiche` | tutto | 5.950 | da fare |
| 14 | `09-foresta-missioni` | `missioni` (l'infrastruttura: `MissioneAPassi`, `Passo`, `RegistroMissioni`…), `tipi`, `modellodati`, `eventi`, `interfacce`, `intermezzi`, `oggetti`. **Rimandato qui:** che nessuno metta un valore null nelle proprietà di `MissioneMD` (`MappaProprieta.salva` lo scriverebbe come il testo "null"; per le caselle è già escluso) (passo 1) | 6.250 | da fare |
| 15 | `09-foresta-missioni` | `missioni/incarichi`. **Rimandato qui** (passo 8): `IlFavore` (in `storie`), `IncaricoDiCombattimento`, `IlColpo`, `LaSorveglianza` e `IlSoccorso` scrivono allo stesso modo i nemici («il goblin», «tre goblin guidati da…», con `NomeDellaClasse`): un metodo comune | 6.250 | da fare |
| 16 | `09-foresta-missioni` | `missioni/richieste` | 4.050 | da fare |
| 17 | `09-foresta-missioni` | `missioni/storie`, `missioni/leggende`, `missioni/principali` | 5.000 | da fare |
| 18 | `10-foresta-modellodati`, `10-foresta-trofei`, `11-foresta-motore-api` | tutto | 2.800 | da fare |
| 19 | `12-foresta-motore` | `motore` (con `Automa`) | 4.750 | da fare |
| 20 | `12-foresta-motore` | `locazioni` (con `LocazioneBase`) | 4.150 | da fare |
| 21 | `12-foresta-motore` | `salvataggi`, `oggetti`, `testi`, `tutorial`, `tipi`, `intermezzi`. **Rimandato qui:** il costruttore di `GestorePunteggiBase` chiama `carica()`, che le sottoclassi ridefiniscono (gira prima che i campi della sottoclasse siano inizializzati: oggi va bene solo perché `nomeFile` non ha un valore iniziale) (passo 1) | 1.950 | da fare |
| 22 | `12-foresta-ui-comune` | `ui` (il pacchetto, senza i sottopacchetti: `SchermoDelGioco`, `ForestaUI`, il router…), `tipi`, `eventi` | 5.250 | da fare |
| 23 | `12-foresta-ui-comune` | `ui/schermate` A–M | 3.850 | da fare |
| 24 | `12-foresta-ui-comune` | `ui/schermate` N–Z | 3.750 | da fare |
| 25 | `12-foresta-ui-comune` | `ui/riquadri`, `ui/tutorial` | 4.450 | da fare |
| 26 | `12-foresta-ui-comune` | `ui/barra`, `ui/componenti`, `ui/testo`. **Rimandato qui:** `BarraIcone` concatena `Comando.getDescrizione()`, che è null per i comandi senza descrizione: vedere se ci arriva solo con comandi che ce l'hanno (passo 3) | 4.250 | da fare |
| 27 | `12-foresta-ui-comune` | `ui/immagini`, `ui/sfx`, `ui/sprite` | 5.250 | da fare |
| 28 | `12-foresta-swing` | `ui` (il pacchetto), `ui/barra`, `ui/componenti`, `ui/immagini`, `ui/testo`, `ui/tutorial`, `tipi`, `eventi` | 3.950 | da fare |
| 29 | `12-foresta-swing` | `ui/riquadri`, `ui/schermate`, `ui/tattiche` | 3.600 | da fare |
| 30 | `12-foresta-swing` | `ui/sprite`, `ui/sfx`, `ui/sviluppo` | 2.450 | da fare |
| 31 | `12-foresta-libgdx` | `libgdx` A–R | 3.000 | da fare |
| 32 | `12-foresta-libgdx` | `libgdx` S–Z, `libgdx/sviluppo`; **confronto con Swing**: quel che le due interfacce fanno uguale e potrebbe scendere in `12-foresta-ui-comune` | 3.850 | da fare |
| 33 | `13-foresta-app-swing`, `13-foresta-app-libgdx`, `13-foresta-app-android`, `foresta-android/` (`AndroidLauncher`, `app/build.gradle`) | tutto; per Android e Gradle solo leggibilità e robustezza | 1.100 | da fare |

### 11.4 Giudizio e migliorie aperte

**Per tutto il progetto** (tornano in ogni passo):
- **Il rientro mescola tab e spazi**, a volte nello stesso modulo (per esempio `LettoreCampi`, `Logger` e `Classifica` con i tab, `Dado`, `BusEventi` e `TrucchiDiSviluppo` con quattro spazi). Non cambia niente nel funzionamento, ma un neofita nota l'incoerenza. Uniformare è un lavoro meccanico, da fare su tutto il progetto in un commit a parte, quando la revisione sarà finita.
- **I Javadoc invecchiano.** Quasi ogni passo ne ha trovati di falsi: dicevano chi crea una classe, quando si salva una cosa, dove sta una regola. Un commento che descrive il «che cosa» resiste; uno che descrive il «chi» o il «quando» va ricontrollato a ogni cambiamento.
- **Lo stato statico** (registri, cataloghi, fornitori di dati) è comodo in un gioco single-player, ma ogni registro va azzerato a mano nei test e al cambio di lingua.

#### Passo 1: serializzazione, strumenti, eventi, punteggi

**Giudizio.** La parte più curata del progetto. Sono moduli piccoli, che fanno una cosa sola, coperti al 100% dai loro test e con Javadoc che spiegano il perché. Alcune scelte sono buone e non banali:
- il `Dado` come unica sorgente di caso, con tre livelli di trucco in un ordine chiaro;
- il bus che isola l'iscritto che fallisce;
- il temporizzatore che rende innocui gli impulsi rimasti in coda dopo un cambio di stato;
- `LettoreCampi`, che trasforma un salvataggio rovinato in un errore che dice la riga.

La concorrenza del temporizzatore e del bus resta la parte più delicata da capire, anche ora che è commentata.

**Aperto:** niente, oltre al rientro (vedi sopra). `TipoEvento` resta, per decisione (§11.5).

#### Passo 2: testi

**Giudizio.** Il modulo ha due metà diverse.
- **Le classi della lingua** (`Testi`, `RegoleDellaLingua` con `LinguaItaliana`, `LinguaInglese` e il decoratore `RegoleConEccezioni`, `Nome`, `Articoli`, `Numeri`) sono il miglior esempio didattico del progetto: piccole, una classe per lingua, testi nei `.properties` con il ripiego sull'italiano.
- **`GestoreGrammatiche`** è un piccolo linguaggio, potente e molto provato. Ora ha i nomi in italiano e una sola `Scansione` per la sintassi, ma resta la classe più lunga e più difficile del modulo.

**Aperto:**
- La guida completa della sintassi sta due volte, nel Javadoc della classe (circa 200 righe) e in `GestoreGrammatiche.md`. Si potrebbe lasciare nel codice solo il riassunto, che c'è già («Come orientarsi»), e mandare al manuale.
- Le sezioni statiche (`#include_static`, circa 250 righe con il loro lucchetto) potrebbero diventare una classe a parte: `GestoreGrammatiche` scenderebbe sotto le 2.300 righe.

#### Passo 3: attributi, comandi, effetti di stato, mappa

**Giudizio.** Moduli piccoli e ben separati. Il calcolo degli attributi non sa chi li possiede; la mappa non sa chi costruisce il mondo (riceve un `PopolatoreDellaForesta`); i dati prendono il fornitore dall'esterno. Il punto debole per chi legge erano i Javadoc di `TipoAttributo`, che promettono meccaniche inesistenti: la loro riscrittura è rimandata al passo 9.

**Aperto:**
- La generazione della mappa conta su presupposti non scritti. Le città sono quattro come i quadranti, e una quinta città non troverebbe un quadrante libero. `Foresta.getCoordinateLibere` e `GeneratoreDellaForesta.getCoordinateLibere` girano all'infinito se la mappa non ha più caselle libere. Con la mappa di oggi (20×20) non succede, ma un controllo esplicito, o un test, lo direbbe a chi cambia le dimensioni.

#### Passo 4: notizie, danni, tutorial

**Giudizio.** Tre moduli piccoli e puliti.
- **Il tutorial** è un esempio di separazione ben riuscita: il registro non sa niente della UI, la UI gli dice che cosa vede con un insieme di `Situazione`, e ogni suggerimento ha accanto il perché del suo posto nell'ordine.
- **I danni** sono tabelle ben documentate, ora leggibili su una riga per reazione.
- **Le notizie** sono semplici.

**Aperto:** niente.

#### Passo 5: artefatti

**Giudizio.** Il modulo più ricco visto finora, e ben progettato:
- gli artefatti non sanno che esiste un mondo, perché ricevono il livello come parametro;
- le regole della fusione sono funzioni pure, che restituiscono un `Piano` senza cambiare niente;
- il costruttore a passi rende impossibile un artefatto senza nome;
- i leggendari e i set sono dati, controllati per intero al caricamento, con messaggi che dicono la riga sbagliata.

Per un neofita le parti dure sono `RegoleIncantatura.piano`, il costruttore a passi e le tabelle del generatore: ora sono commentate.

**Aperto:**
- `GeneratoreArtefattiTabelle` ha una tabella dei pesi (`PESI`) che deve coprire ogni `TipoArtefatto`: un tipo nuovo senza peso darebbe una `NullPointerException` alla prima generazione. Oggi tutti i 24 tipi ce l'hanno. Un test che lo controlli renderebbe esplicito il vincolo.

#### Passo 6: personaggi (tipi, dati, contratti, eventi)

**Giudizio.** Le parti dichiarative del modulo sono ordinate: le debolezze e le immunità sono dati delle enum (`TipoPersonaggio` e `CategoriaPersonaggio`), letti allo stesso modo dal combattimento e dal bestiario; i `…MD` sono semplici e si rileggono in un ordine stabile; gli eventi sono piccoli. I punti deboli per chi legge erano:
- `CostantiPersonaggi`, con i commenti staccati dalle loro costanti, alcuni rimasti dopo che le costanti erano andate in altri moduli;
- i Javadoc che non dicevano più il vero (`TipoRiposo`, `TipoIncantesimo`, `PersonaggioMD`, gli eventi di prelievo).

**Aperto:**
- La causa della morte si salva già tradotta (`PersonaggioMD.causaTrapasso`): dopo un cambio di lingua resta nella lingua di quando il personaggio è morto.

#### Passo 7: personaggi (i meccanismi) e gruppi

**Giudizio.** `PersonaggioBase`, dopo l'estrazione dei sette collaboratori (salute, effetti, modificatori, carico, progressione, nome), è leggibile: ogni collaboratore ha un compito e rilegge i dati dal personaggio. I punti deboli erano:
- i resti dell'estrazione: righe vuote, titoletti senza contenuto, commenti rimasti da sessioni con un'IA;
- le formule di salute e magia scritte due volte;
- i commenti di `LanciatoreDeiDadi`, che smentivano le tabelle;
- i Javadoc dell'interfaccia `Personaggio`, invecchiati.

**Aperto:**
- `PersonaggioBase` pubblica `InternoCreazionePersonaggio` dal costruttore, prima che la sottoclasse abbia finito di costruirsi: oggi lo ascolta solo lo sniffer.
- `Personaggio.getLetteraFinaleAttributo` («o»/«a») vale solo per l'italiano: da vedere al passo 21, con `RegistroNotizie`.

#### Passo 8: personaggi (le classi)

**Giudizio.** Le classi dei personaggi erano una sessantina di file quasi uguali: una ventina di metodi ciascuno che restituivano le costanti della classe. Ora i valori fissi di ogni classe stanno in una tabella, `CostantiPersonaggi.valoriDi` (`ValoriDiClasse`, una riga per classe), che `PersonaggioBase` legge per tutti. Le classi tengono solo il loro comportamento: il repertorio, le offerte, i casi speciali dell'Ombrafiamma. Anche il «personaggio modello», che si costruiva per sapere il nome o i moltiplicatori di una classe (e tirava i dadi), non c'è più: i nomi vengono da `NomeDellaClasse`, i valori dalla tabella.

**Aperto:**
- I repertori di incantesimi dei mostri sono scritti due volte: nelle classi (`getRepertorioIncantesimi`) e negli elenchi `MAGICI`, `CON_SILENZIO` e `CON_MORTE` di `TatticheBase` (c'è già un TODO): da vedere al passo 13.

### 11.5 Decisioni prese (da non riproporre)

- **`TipoEvento` resta** (passo 1): per l'utente è il dizionario centralizzato di tutti i tipi di evento, e potrà servire ancora, anche se oggi lo legge solo il registro.
- **I messaggi d'errore di `GestoreGrammatiche` restano in inglese** (passo 2): li legge chi modifica le grammatiche, che può non sapere l'italiano. È scritto in `GestoreGrammatiche.md` §9.
- **Le produzioni derivate (`NOME = ALTRA`) restano nella sintassi** (passo 2): sono state corrette, non tolte, anche se oggi nessuna grammatica del gioco le usa.
- **`Comando.RUTTOLOMEO` e `STORPSGORBLIN` restano** (passo 3), con le loro icone: sono rimanenze storiche a cui l'utente è affezionato.
- **Una qualsiasi grammatica rotta impedisce al gioco di partire** (passo 5), e `TutteLeGrammaticheTest` le carica tutte, in ogni lingua, prima che si costruisca il jar.
- **`NotificaRifiutoStoccaggioArtefatto` resta** (passo 6), anche se oggi nessuno la pubblica: servirà quando l'inventario generale avrà un limite di peso.
- **Chi è immune a un incantesimo è immune anche al suo tipo di danno** (passo 6): l'immunità della Viverna all'incantesimo Fuoco senza l'immunità al danno di fuoco era un difetto, e così quelle di Arpia, Troll e Strega (che resta debole al fuoco e non è più immune all'incantesimo Fuoco). Lo controlla `CaratteristicheDiClasseTest`; fa eccezione Morte, che non fa danno.
- **I valori fissi delle classi stanno in una tabella in Java** (passo 8), `CostantiPersonaggi.valoriDi`, costruita con una catena di metodi dai nomi parlanti (non un file di dati); ci stanno anche sesso, corrompibile, amichevole e boss.
- **L'Ombrafiamma e l'Ombra nera hanno il budget dei giocanti** in `LanciatoreDeiDadi` (passo 7), come il Necromante: quello dei mostri comuni non era voluto.
- **Chi è immune a un tipo di danno non subisce nemmeno le reazioni elementali di quel danno** (passo 6): un'arpia bagnata colpita dall'aria non si raffredda. Resta com'è scritto in `debolezze_e_resistenze.md`.

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
- Una nuova lettura integrale dei sorgenti, modulo per modulo: è il §11.
- Rendere più sensibile il riconoscitore di `ConvenzioniDiLocalizzazioneTest.leStringheItalianeNelCodiceNonAumentano`: dal passo 5 (D5.1) sappiamo che gli sfuggono i pezzi corti di frase (`"la Gemma"`, `" del Core Dump"`), che si concatenano per fare un testo per il giocatore.
- Un test che produca tutte le alternative delle grammatiche e le disegni con i font, per prendere le produzioni malformate e i caratteri che il font non ha; con lo stesso passo, decidere quali produzioni sono davvero morte (§5.1, B35e).
