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
- **le correzioni dei possibili `NullPointerException` e le fattorizzazioni si propongono**, con file e riga, e si applicano solo dopo l'ok dell'utente (regole di `CLAUDE.md`: niente iniziative sul disegno, prima di cambiare la forma del codice si chiede). Le correzioni piccole e locali (un controllo, un valore di ritorno) si possono applicare nello stesso passo, dopo l'ok; le fattorizzazioni fra moduli diversi vanno nel §11.5 e si fanno a parte;
- **il commit si fa solo su richiesta**, dopo la revisione dell'utente.

Alla fine di ogni passo si aggiornano la riga della tabella del §11.3 (stato, data) e il §11.4 (che cosa si è trovato).

### 11.2 Il prompt di un passo

Da incollare in una sessione nuova di Claude Code, mettendo al posto di `<PASSO>` il numero del passo della tabella del §11.3:

> Esegui il passo `<PASSO>` della revisione del codice descritta in `assessment.md` §11. Leggi prima §11.1 e §11.3 (che cosa entra nel passo) e §11.4 e §11.5 (che cosa hanno trovato i passi prima: non ripeterlo, e usa le parti comuni già proposte). Lavora tu, leggendo i file per intero; al massimo un agente di aiuto se il passo è grande, mai più di due.
>
> Per **ogni file** del passo:
>
> 1. **`NullPointerException` possibili.** Per ogni riferimento che si usa (chiamata di metodo, accesso a un campo, `unboxing` di un `Integer`/`Boolean`, `switch` su un'enum, `for` su una collezione) chiediti se può valere `null`, e **non fermarti al file**: risali a chi lo produce (chi chiama il metodo, chi assegna il campo, cosa restituisce il metodo chiamato). Casi tipici in questo progetto: `Map.get` su una chiave che può mancare; metodi che restituiscono `null` per dire «non c'è» (`trova…`, `cerca…`, `get…OrNull`); campi dei `…MD` dopo la rilettura di un salvataggio (un campo che il file non aveva); singleton e registri usati prima di `azzera`/`installa`/`setFornitoreDiDati`; parametri che arrivano dagli eventi del bus; `Testi.get` con una chiave che manca nei `.properties`; personaggi morti o rimossi dal gruppo nel mezzo di un combattimento; liste vuote dove si prende il primo elemento. Segnala **solo i casi che puoi raggiungere davvero** (scrivi il percorso: chi chiama, con quali dati), separando i certi dai plausibili; un caso che hai controllato e scartato scrivilo in una riga, così il passo dopo non lo ricontrolla. Non proporre controlli `if (x != null)` che nascondono un errore: se `null` vuol dire che qualcosa è andato storto prima, la correzione è lì.
> 2. **Parti in comune.** Cerca il codice ripetuto: metodi quasi uguali in classi sorelle (per esempio le classi dei personaggi e dei mostri, le missioni, le schermate Swing e libGDX), blocchi copiati con piccole differenze, costanti ripetute, cicli che fanno la stessa ricerca. Per ognuno: dove sta (file e righe), che cosa avrebbe di comune, **dove andrebbe** (in quale classe e in quale modulo, rispettando i livelli: un modulo vede solo quelli sotto), quante righe si risparmiano e che cosa si rischia. Non vale la pena di fattorizzare due righe uguali, né di unire cose che si somigliano oggi ma hanno ragioni diverse per cambiare. Le proposte che toccano più moduli vanno nel §11.5.
> 3. **Leggibilità per un neofita.** Immagina un lettore che conosce le basi di Java ma non le lambda, gli stream, i generici più complicati né il gioco. **Aggiungi i commenti** (in italiano, con le convenzioni di `CLAUDE.md`, virgolette “ ”):
>    - in testa a **ogni classe** che non ce l'ha, un Javadoc che dice che cosa rappresenta nel gioco, chi la usa e perché esiste (non come è fatta);
>    - sui **metodi pubblici** il cui nome non basta (che cosa restituisce, quando restituisce `null` o una lista vuota, che effetti ha sullo stato del gioco);
>    - sulle righe **criptiche**: stream lunghi (che cosa ne esce), espressioni con operatori ternari annidati, calcoli con numeri (da dove vengono e che cosa vogliono dire: per esempio una probabilità, una formula di danno), giochi di bit, ordinamenti con `Comparator` composti, ricorsioni, convenzioni del progetto che un esterno non conosce (il bus degli eventi, i fornitori di dati, il `Dado` con il suo `ContestoLancio`, i `…MD`).
>    Non commentare l'ovvio (`// incrementa i`) e non riscrivere il codice per renderlo più leggibile senza chiedere: se un pezzo andrebbe riscritto (un metodo di 150 righe, un nome che inganna), proponilo nel rapporto. Non cambiare nomi, firme né comportamento.
>
> Alla fine:
> - compila e lancia i test del modulo (`mvn -q test -pl <modulo> -am`, oppure senza `-q` per vedere gli errori); i commenti non devono rompere niente;
> - **non fare commit**;
> - aggiorna `assessment.md`: nella tabella del §11.3 la colonna «Stato» del passo (`fatto il <data>`, o `in corso` se non hai finito, con il punto in cui riprendere), nel §11.4 una sottosezione `#### Passo <PASSO>` con: i possibili `NullPointerException` (certi / plausibili / controllati e scartati), le parti in comune proposte, i pezzi che andrebbero riscritti, i file commentati; nel §11.5 le proposte fra moduli;
> - poi riassumimi le proposte, una per una, e aspetta l'ok prima di applicarle.

### 11.3 I passi

In ordine di livello. Le righe sono quelle di `wc -l` su `src/main/java` al 2026-11-13. I test non entrano nella revisione (si leggono solo per capire come si usa una classe). Per i moduli grandi il passo dice quali pacchetti o file entrano; «A–M» vuol dire i file il cui nome comincia con una lettera da A a M.

| Passo | Modulo | Che cosa entra | Righe | Stato |
| ---: | :--- | :--- | ---: | :--- |
| 1 | `00-foresta-serializzazione`, `00-foresta-strumenti`, `01-foresta-eventi`, `01-foresta-punteggi` | tutto | 2.700 | fatto il 2026-11-13, proposte applicate |
| 2 | `01-foresta-testi` | tutto (`GestoreGrammatiche`, già `GrammarBean`, da solo fa 2.600 righe) | 4.600 | fatto il 2026-11-13, proposte applicate |
| 3 | `02-foresta-attributi`, `02-foresta-comandi`, `02-foresta-effettidistato`, `02-foresta-mappa` | tutto | 3.600 | fatto il 2026-11-13, proposte applicate (D3.2 al passo 9) |
| 4 | `02-foresta-notizie`, `03-foresta-danni`, `03-foresta-tutorial` | tutto | 1.550 | fatto il 2026-11-13, proposte applicate |
| 5 | `04-foresta-artefatti` | tutto | 5.250 | da fare |
| 6 | `05-foresta-personaggi` | `tipi` (con `CostantiPersonaggi`), `modellodati`, `interfacce`, `eventi` | 5.700 | da fare |
| 7 | `05-foresta-personaggi` | `personaggi`: i meccanismi (`PersonaggioBase`, `Personaggio`, `SaluteEMorte`, `EffettiDiStato`, `ModificatoriTemporanei`, `AttributiSecondari`, `CaricoEInventario`, `GestoreProgressione`, `Progressione`, `RegoleEquipaggiamento`, `CalcolatoreRiposo`, `RisultatoRiposo`, `FabbricaPersonaggi`, `EquipaggiamentoIniziale`, `LanciatoreDeiDadi`, `MoltiplicatoriDiClasse`, `NomeEDescrizione`, `ArmaNaturale`, `PersonaggioNonCombattente`) e `gruppi` | 5.500 | da fare |
| 8 | `05-foresta-personaggi` | `personaggi`: le classi dei personaggi e dei mostri (tutti gli altri file, da `Alchimista` a `Viverna`): **qui le parti in comune sono il tema principale** (sono una sessantina di classi corte e simili: se non basta una sessione, A–L e M–Z) | 7.550 | da fare |
| 9 | `06-foresta-bestiario`, `06-foresta-combattimento`, `06-foresta-statistiche` | tutto; **in più (dal passo 3, D3.2): riscrivere i Javadoc delle costanti di `TipoAttributo` (`02-foresta-attributi`) con quello che ogni attributo fa davvero, leggendone gli usi nel combattimento, e togliere l'avviso in testa all'enum** | 4.000 | da fare |
| 10 | `07-foresta-intermezzi`, `07-foresta-mondo` | tutto | 4.600 | da fare |
| 11 | `07-foresta-poteri` | tutto | 3.600 | da fare |
| 12 | `08-foresta-bottino`, `08-foresta-economia` | tutto | 3.350 | da fare |
| 13 | `08-foresta-tattiche` | tutto | 5.950 | da fare |
| 14 | `09-foresta-missioni` | `missioni` (l'infrastruttura: `MissioneAPassi`, `Passo`, `RegistroMissioni`…), `tipi`, `modellodati`, `eventi`, `interfacce`, `intermezzi`, `oggetti` | 6.250 | da fare |
| 15 | `09-foresta-missioni` | `missioni/incarichi` | 6.250 | da fare |
| 16 | `09-foresta-missioni` | `missioni/richieste` | 4.050 | da fare |
| 17 | `09-foresta-missioni` | `missioni/storie`, `missioni/leggende`, `missioni/principali` | 5.000 | da fare |
| 18 | `10-foresta-modellodati`, `10-foresta-trofei`, `11-foresta-motore-api` | tutto | 2.800 | da fare |
| 19 | `12-foresta-motore` | `motore` (con `Automa`) | 4.750 | da fare |
| 20 | `12-foresta-motore` | `locazioni` (con `LocazioneBase`) | 4.150 | da fare |
| 21 | `12-foresta-motore` | `salvataggi`, `oggetti`, `testi`, `tutorial`, `tipi`, `intermezzi` | 1.950 | da fare |
| 22 | `12-foresta-ui-comune` | `ui` (il pacchetto, senza i sottopacchetti: `SchermoDelGioco`, `ForestaUI`, il router…), `tipi`, `eventi` | 5.250 | da fare |
| 23 | `12-foresta-ui-comune` | `ui/schermate` A–M | 3.850 | da fare |
| 24 | `12-foresta-ui-comune` | `ui/schermate` N–Z | 3.750 | da fare |
| 25 | `12-foresta-ui-comune` | `ui/riquadri`, `ui/tutorial` | 4.450 | da fare |
| 26 | `12-foresta-ui-comune` | `ui/barra`, `ui/componenti`, `ui/testo` | 4.250 | da fare |
| 27 | `12-foresta-ui-comune` | `ui/immagini`, `ui/sfx`, `ui/sprite` | 5.250 | da fare |
| 28 | `12-foresta-swing` | `ui` (il pacchetto), `ui/barra`, `ui/componenti`, `ui/immagini`, `ui/testo`, `ui/tutorial`, `tipi`, `eventi` | 3.950 | da fare |
| 29 | `12-foresta-swing` | `ui/riquadri`, `ui/schermate`, `ui/tattiche` | 3.600 | da fare |
| 30 | `12-foresta-swing` | `ui/sprite`, `ui/sfx`, `ui/sviluppo` | 2.450 | da fare |
| 31 | `12-foresta-libgdx` | `libgdx` A–R | 3.000 | da fare |
| 32 | `12-foresta-libgdx` | `libgdx` S–Z, `libgdx/sviluppo`; **confronto con Swing**: quel che le due interfacce fanno uguale e potrebbe scendere in `12-foresta-ui-comune` | 3.850 | da fare |
| 33 | `13-foresta-app-swing`, `13-foresta-app-libgdx`, `13-foresta-app-android`, `foresta-android/` (`AndroidLauncher`, `app/build.gradle`) | tutto; per Android e Gradle solo leggibilità e robustezza | 1.100 | da fare |

### 11.4 Risultati dei passi

Ogni passo aggiunge qui la sua sottosezione, `#### Passo N`.

#### Passo 1: `00-foresta-serializzazione`, `00-foresta-strumenti`, `01-foresta-eventi`, `01-foresta-punteggi` (2026-11-13)

Letti per intero i 39 file (2.700 righe), seguendo i chiamanti nei moduli sopra dove serviva per decidere se un caso è raggiungibile. Test dei quattro moduli verdi dopo i commenti (32 + 51 + 16 + 7).

**Esito (2026-11-13):** l'utente ha approvato tutte le proposte tranne X1, e sono applicate:
- N1.1: `getMessaggio` restituisce null invece di andare in errore;
- D1.1: `booleano` accetta solo `true` e `false`, e il test che verificava il vecchio comportamento è stato adattato;
- D1.2: il registro riceve anche il messaggio;
- D1.3: `volatile`;
- D1.5: la costante è fra le notifiche;
- F1.1: c'è `NotificaDiTesto`;
- F1.2 e F1.3.

In più, su richiesta, i nomi inglesi di `BusEventi` sono diventati italiani (`tipoDiEvento`, `iscritto`, `iscritti`). Dopo le modifiche sono verdi tutti i test del progetto: 3.759, di cui 37 saltati.

**`NullPointerException` possibili**

- *Certi:* nessuno.
- *Plausibili:*
  - **N1.1** `InternoException.getMessaggio()` (`01-foresta-eventi`) fa `exception.getMessage()` se il messaggio è null: con un'eccezione null esce una `NullPointerException`. Oggi tutti quelli che la costruiscono le passano un'eccezione presa in un `catch`, quindi non succede. Però `SegnalatoreDegliErrori.descrizione` (`12-foresta-ui-comune`) controlla `eccezione == null` *dopo* aver chiamato `getMessaggio()`, e quel controllo non può mai servire. Correzione proposta: `getMessaggio()` restituisce null se mancano tutti e due, così il controllo della UI funziona.
  - **N1.2** `MappaProprieta.salva` (`00-foresta-serializzazione`): una chiave null dà una `NullPointerException` in `validaChiave`, e un valore null si salva come il testo `"null"` (e si rilegge come `"null"`). Il controllo c'è in `pulisciValore`, ma solo se chi riempie la mappa lo chiama. Da verificare nei passi 3 (`LocazioneMD`) e 14 (`MissioneMD`), che sono quelli che la riempiono.
- *Controllati e scartati:*
  - `TemporizzatoreJ2SE`: `temporizzabile.tick()` con un `temporizzabile` null. Non succede: `Automa` e `ForestaUI` lo impostano nel costruttore, prima di qualunque `inizia`, e comunque l'errore si prenderebbe e si segnalerebbe.
  - `Classifica.getPunteggio` prima di riempirla, con i nomi null. Non succede: `GestorePunteggiBase` la legge dal file o rimette la predefinita nel costruttore.
  - `BusEventi.pubblica(null)` e `Dado.scegli(contesto, null)`: sarebbero errori di chi chiama, e nessuno lo fa.

**Altri difetti (non `NullPointerException`)**

- **D1.1** `LettoreCampi.booleano()` legge come falso qualunque testo diverso da `true`. `intero()` ed `enumerato()` invece danno un `IOException` su un campo rovinato, mentre un booleano rovinato diventa falso senza dire niente. Proposta: accettare solo `true` e `false`, e dare errore per il resto.
- **D1.2** Il costruttore senza argomenti di `TemporizzatoreJ2SE` butta via il messaggio «Impulsi fermati dopo N errori di fila» (la lambda scrive solo l'eccezione). Lo usano solo i test, quindi è poco grave; ma o si scrive anche il messaggio o si toglie il costruttore (togliere va chiesto).
- **D1.3** `TemporizzatoreJ2SE`: `impulsiFalliti` e `temporizzabile` si leggono e si scrivono sul thread principale fuori dal lucchetto, mentre `pianifica` li tocca dentro. In pratica funziona, perché il passaggio dalla coda del thread principale fa vedere i valori aggiornati, ma non è dichiarato. Se si vuole essere puliti, basta `volatile` su `temporizzabile`.
- **D1.4** (da guardare nel passo 21) Il costruttore di `GestorePunteggiBase` chiama `carica()`, che le sottoclassi ridefiniscono. È una trappola nota di Java: il metodo della sottoclasse gira prima che i suoi campi siano inizializzati. Oggi va bene solo perché il campo `nomeFile` di `GestorePunteggiSuFile` non ha un valore iniziale.
- **D1.5** In `TipoEvento` la costante `NOTIFICA_MESSAGGIO_DEL_TUTORIAL` sta nel blocco degli eventi interni. È solo una questione di ordine, ma confonde chi legge.

**Parti in comune**

- **F1.1** `NotificaTestoFrase` e `NotificaTestoParagrafo` sono uguali, tranne il tipo. Possono estendere una classe astratta `NotificaDiTesto` (stesso pacchetto) con il messaggio, il getter e `conInizialeMaiuscola`; devono comunque restare due classi, perché il bus smista per classe. Si risparmiano una quindicina di righe, e soprattutto si vede che sono parenti.
- **F1.2** `Serializzabile.facoltativo` ripete la regola delle enum di `campo`. Può diventare `valore == null ? "" : campo(valore)`: tre righe in meno e una regola in un posto solo.
- **F1.3** `InternoErrore` ha il campo `messaggio` sia `public final` sia con il getter. Il campo andrebbe reso `private`, come in tutti gli altri eventi.
- *Non vale la pena:* i controlli ripetuti di `Dado` (`tira(N)`, `tira(contesto, N)`, i due `tiraAncheAUnaFaccia`). Sono quattro righe ciascuno, e ognuno ha un messaggio che nomina il suo metodo, che è utile.
- Una proposta che tocca tutti i moduli (`TipoEvento`) è nel §11.5.

**Pezzi che andrebbero riscritti:** nessuno.

**Commenti aggiunti**

Su `Serializzabile` (che cos'è un salvataggio a righe, con un esempio), `LettoreCampi` (`Pattern.quote` e lo `split` con -1), `Dado` (l'ordine delle precedenze fra trucchi dei test, truccatore e caso; il `- 1` della scelta; un Javadoc che diceva «da 1 a N» dove il risultato va da min a max), `TrucchiDiSviluppo`, `ModalitaDiProva`, `DestinazioneConsoleEFile`, `TemporizzatoreJ2SE` (il thread daemon e `scheduleWithFixedDelay`; gli accenti scritti con l'apostrofo), `ThreadPrincipale` (perché un thread solo), `BusEventi` (che cos'è il publish/subscribe, con un esempio; le collezioni concorrenti, il `ThreadLocal`, il cast con `@SuppressWarnings`, lo smistamento per classe esatta), `EventoBase` (diceva «astratta» e non lo è; a che serve il tipo), `InternoException`, `InternoMessaggio` (il Javadoc del getter era sbagliato), `TipoEvento`, `Classifica` (gli array paralleli, i passi di `inserisci`), `GestorePunteggi` (diceva che lo crea `Main`, invece è `AvvioDelMotore`) e `Punteggio`.

**Giudizio**

Questi quattro moduli sono la parte più curata del progetto. Sono piccoli, fanno una cosa sola, sono coperti al 100% dai loro test e hanno quasi tutti un Javadoc che spiega il perché, non solo il cosa. Alcune scelte sono buone e non banali:
- il `Dado` come unica sorgente di caso, con tre livelli di trucco in un ordine chiaro;
- il bus che isola l'iscritto che fallisce senza togliere l'evento agli altri;
- il temporizzatore che rende innocui gli impulsi già in coda dopo un cambio di stato (il numero di «pianificazione»);
- `LettoreCampi`, che trasforma un salvataggio rovinato in un `IOException` con la riga, invece di un errore a caso più avanti.

Da migliorare, in ordine di importanza:
1. Le parti più difficili per un neofita, cioè la concorrenza del temporizzatore, i generici e il `ThreadLocal` del bus, ora sono commentate, ma restano le più delicate del progetto. Chi le tocca deve conoscere il modello dei thread descritto in `ThreadPrincipale`.
2. Lo stile non è uniforme: alcuni file sono rientrati con i tab (`LettoreCampi`, `Logger`, `Classifica`), altri con quattro spazi (`Dado`, `BusEventi`, `TrucchiDiSviluppo`). (`BusEventi` usava anche nomi inglesi, `eventType`, `listener`, `list`; corretti.) Non cambia niente nel funzionamento, ma un neofita nota l'incoerenza. Uniformare è un lavoro meccanico, da fare se si vuole su tutto il progetto e in un commit a parte.
3. Il difetto D1.1 (un booleano rovinato letto in silenzio come falso) è l'unica crepa nella regola, altrimenti rispettata, che un salvataggio rovinato si dichiara.
4. `TipoEvento` oggi serve solo al registro (570 righe che ripetono i nomi delle classi); l'utente lo tiene come dizionario degli eventi (§11.5, X1).

#### Passo 2: `01-foresta-testi` (2026-11-13)

**Esito (2026-11-13):** l'utente ha approvato tutto, e ha chiesto anche di rinominare la classe. Applicate:
- **la rinomina (R2.1).** `GrammarBean` è diventata `GestoreGrammatiche`, con tutti i nomi in italiano: metodi, campi, classi interne, costanti e variabili, compresi quelli che usano gli altri moduli. Per esempio `produce` diventa `produci`, `reset` diventa `ripristina`, `canProduce` diventa `puoProdurre`, `setRootNode` diventa `setRadice`, `addFixedProduction` diventa `fissaProduzione`, `fromResource` diventa `daRisorsa`; `InvalidGrammarException` diventa `GrammaticaMalformataException`, `IncludeResolver` diventa `RisolutoreDelleInclusioni`. Sono stati rinominati anche i test (`GestoreGrammatiche*Test`), il manuale (`GestoreGrammatiche.md`) e i rimandi in tutti i documenti, tranne il diario `revisione_documentazione.md`. **I messaggi d'errore restano in inglese, per decisione dell'utente**: li legge chi modifica le grammatiche, che può non sapere l'italiano. La decisione è scritta in `GestoreGrammatiche.md` §9 e nel Javadoc della classe;
- **D2.1:** una graffa di chiusura senza apertura è un errore di caricamento. Due casi limite in `GestoreGrammaticheCasiLimiteTest`, che si aspettavano il vecchio comportamento, sono stati adattati;
- **D2.2 e D2.3:** le produzioni derivate si risolvono in ordine di dipendenza (`risolviDerivate`), e solo un ciclo vero è un errore. Sono state corrette, non tolte, e il manuale ora le descrive (§4.13);
- **D2.4:** in `Testi`, la lingua e la sua catena di file stanno in un solo oggetto immutabile;
- **D2.5:** un ordinale fuori da 1–41 è un'`IllegalArgumentException` che lo dice;
- **D2.6:** tolte le frasi di storia dai Javadoc;
- **F2.1:** la classe interna `Scansione` sostituisce cinque scanner (`unescapeQuotes`, che trasforma il testo, resta a parte ma segue la stessa regola degli escape);
- **F2.2**, **F2.3**, **F2.4** (`GrammaticaPigra.riga`, 20 chiamate) e **X2** (`strumenti.Maiuscole`, al posto di sei copie).

Ci sono tre test nuovi: la graffa isolata, le derivate a catena nei due ordini, il ciclo. Sono verdi tutti i test del progetto: 3.762, di cui 37 saltati.

Letti per intero i 26 file (4.600 righe, di cui 2.563 di `GestoreGrammatiche`), con le grammatiche dove serviva per decidere se un caso è raggiungibile. I tre difetti di `GestoreGrammatiche` qui sotto li ho confermati con piccole grammatiche lanciate con `jshell` sulle classi compilate. Test del modulo verdi dopo i commenti (341).

**`NullPointerException` possibili**

- *Certi:* nessuno.
- *Plausibili:* nessuno.
- *Controllati e scartati:*
  - `GestoreGrammatiche.produzioneStatica` con una produzione che non è statica (`staticSectionOf` darebbe null): la si chiama solo dopo `isStaticName`.
  - `GestoreGrammatiche.produci()` senza radice: la radice è la prima produzione del file, impostata al caricamento.
  - `CaricatoreDiGrammatiche.perLaLingua` con una risorsa senza punto (darebbe un errore di indice, non una `NullPointerException`): tutti passano un `.txt`.
  - `GrammaticaNonValidaException` con la causa null: tutti la costruiscono in un `catch`.
  - `Lingue.per(null)`: `Testi.getLingua()` non è mai null.
  - Le grammatiche che `ProduttoriDiTesti.resetProduzioni` non azzera (mondo, sfide, templi, rovine, missioni): non hanno produzioni monouso, quindi non si consumano (ho contato le `NOME$` in tutti i file e in tutte le lingue).

**Altri difetti (non `NullPointerException`)**

- **D2.1 (confermato)** In `GestoreGrammatiche.splitTopLevelAlternatives`, una graffa `}` isolata fuori dalle virgolette porta il contatore sotto zero, e da lì in poi i `|` non dividono più. La riga `A } | B` diventa una sola alternativa, `A } | B`, senza errore. Gli altri cinque scanner della classe invece si fermano a zero (`Math.max(0, …)`). Proposta: una graffa di chiusura senza apertura è un errore di caricamento (`GrammaticaMalformataException`), come quella di apertura senza chiusura.
- **D2.2 (confermato)** Con le produzioni derivate a catena (`A = B`, poi `B = C`), `A` resta senza alternative: il caricamento non se ne accorge (`validatePreviousProduction` salta le derivate) e la produzione fallisce dopo, con un errore di indice.
- **D2.3 (confermato)** Nell'ordine inverso (`C`, `B = C`, `A = B`) il caricamento rifiuta la grammatica dicendo che A e B «are mutually derived», e non è vero: è una catena, non un ciclo. **Nessuna grammatica del gioco usa le produzioni derivate**: le usano solo i test di `GestoreGrammaticheTest`. Le strade sono due: correggerle (risolvere le derivazioni in ordine di dipendenza e rifiutare solo i cicli veri), oppure toglierle dalla sintassi. Togliere è una cancellazione: decide l'utente.
- **D2.4** `Testi.impostaLingua` scrive `lingua` e `catena` in due momenti diversi: un thread che legge proprio in mezzo vede la lingua nuova con la catena dei file vecchia. Oggi la lingua cambia dalla schermata delle opzioni, sul thread principale, quindi non succede. Se si vuole chiuderlo, basta tenere le due cose in un solo oggetto immutabile.
- **D2.5** `Numeri.getOrdinaleM(0)` dà «nullo» e `getOrdinaleF(0)` «nulla»: viene da `append(null)` sulla radice che manca, ed è documentato. Da 42 in su c'è un errore di indice senza spiegazione. Proposta: un'`IllegalArgumentException` che dica l'intervallo valido (da 1 a 41).
- **D2.6** Alcuni Javadoc di `GestoreGrammatiche` raccontano la storia del codice («prima che questa funzione esistesse», «una volta era un Random privato», «una semplice HashMap lasciava…»). La convenzione del progetto è che i documenti descrivono il presente: andrebbero tolte quelle frasi, senza toccare il resto.

**Parti in comune**

- **F2.1** In `GestoreGrammatiche` sei scanner scorrono il testo carattere per carattere tenendo il conto di virgolette, escape, graffe e quadre: `splitTopLevelAlternatives`, `findNextUnquotedOpeningBrace`, `unescapeQuotes`, `findMatchingClosingBrace`, `findDefaultSeparator` e `hasTopLevelAssignment`. Ognuno lo rifà a modo suo, con piccole differenze: alcuni riconoscono solo l'escape delle virgolette, altri qualunque escape; alcuni si fermano a zero e altri no, ed è da qui che nasce D2.1. Una piccola classe interna che fa la scansione e dice a ogni carattere «sono fra virgolette, a questa profondità» toglierebbe un centinaio di righe e le differenze. Il rischio è medio, perché è il cuore del parser, ma i 341 test del modulo lo coprono bene. Conviene farla dopo D2.1.
- **F2.2** `produci()` e `produci(String)` hanno lo stesso corpo: il primo può essere `return produci(rootNode);`.
- **F2.3** `Nome.di(Enum)` e `Nome.di(String, Enum)` ripetono la stessa costruzione di dieci righe, cambiando solo dove si cerca una voce. Possono chiamare un metodo privato che riceve «come si legge una voce» (una `Function<String, Optional<String>>`). Resta diverso solo il ripiego del singolare.
- **F2.4** I sette produttori ripetono una ventina di volte `GRAMMATICA.get().produci("PRODUZIONE").get(0).trim()`. Un metodo `GrammaticaPigra.riga(String produzione)` li renderebbe di una riga ciascuno, più facili da leggere.
- **F2.5** La maiuscola iniziale è scritta tre volte in questo modulo (`Articoli.inizialeMaiuscola`, `LinguaInglese.inizialeMaiuscola`, `GestoreGrammatiche.capitalizeFirstLetter`) e altre tre fuori: §11.5, X2.
- *Non vale la pena:* il ciclo che salta i caratteri non lettera all'inizio di `suonoIniziale`, uguale in italiano e in inglese. Sono cinque righe, e il resto dei due metodi è diverso.

**Pezzi che andrebbero riscritti**

- **R2.1** `GestoreGrammatiche` ha tutti i nomi in inglese: campi, metodi, classi interne e messaggi d'errore (`"Production X is empty!"`). I commenti invece sono in italiano, come il resto del progetto. Per un neofita la classe è doppiamente difficile: la sintassi delle grammatiche e due lingue mescolate. Tradurre i nomi privati è meccanico. I nomi pubblici (`produci`, `ripristina`, `setRadice`, `puoProdurre`, `fissaProduzione`) li usano anche gli altri moduli, quindi andrebbero cambiati con loro. I messaggi d'errore li controllano decine di test (`contains("is not defined")`…). È un lavoro grande: decide l'utente se e quando farlo.
- **R2.2** Il Javadoc di `GestoreGrammatiche` è accurato ma prolisso: quello della classe è lungo duecento righe, e molti metodi ripetono le stesse spiegazioni. Ho aggiunto in testa una mappa per orientarsi (le due fasi, caricamento e produzione, con i metodi principali e un esempio). Si potrebbe portare la guida della sintassi in `GestoreGrammatiche.md`, che esiste già, e lasciare nel codice solo il riassunto. Anche questo è da decidere.
- *Idea, non urgente:* le sezioni statiche (`#include_static`, circa 250 righe con il loro lucchetto) potrebbero diventare una classe a parte, e `GestoreGrammatiche` scenderebbe sotto le 2.300 righe.

**Commenti aggiunti**

- `GestoreGrammatiche`: come orientarsi nella classe, con un esempio; il «righello» della scelta pesata; il richiamo alle altre copie della maiuscola.
- `GrammaticaPigra`: perché la mappa è concorrente.
- `RegoleConEccezioni`: il trucco della «x» al posto del nome.
- `Numeri`: perché l'articolo si elide solo per 8 e 11, con un esempio.
- `Nome`: l'indice degli articoli.
- `Elenchi`: il ternario annidato.
- `LinguaInglese`: gli ordinali composti.
- `ProduttoriDiTesti`: quali grammatiche si azzerano, e che cosa fare se se ne aggiunge una con produzioni monouso.
- `Testi`: che cosa fa `impostaLinguaImpl`.
- `Articoli`: 33 Javadoc avevano perso il punto prima di «Vale».

**Giudizio**

È un modulo in due metà diverse.

**Le classi della lingua** (`Testi`, `Lingue`, `RegoleDellaLingua` con `LinguaItaliana`, `LinguaInglese` e `RegoleConEccezioni`, `Nome`, `Articoli`, `Numeri`) sono il miglior esempio didattico del progetto. Hanno un'interfaccia piccola, una classe per lingua, le eccezioni dei nomi messe davanti alla regola con un decoratore (`RegoleConEccezioni`) e i testi nei file `.properties` con il ripiego sull'italiano. Un neofita le legge senza aiuto.

**`GestoreGrammatiche`** è un piccolo linguaggio di programmazione, potente e molto provato (341 test nel modulo, quasi tutti suoi). Però è la classe più difficile del progetto:
- è lunga, ha due lingue mescolate (R2.1) e un Javadoc che spiega tutto ma annega il lettore (R2.2);
- contiene sei scanner simili che non si comportano allo stesso modo (F2.1), e da lì è nato D2.1;
- le produzioni derivate (D2.2, D2.3) sono una funzione rotta, ma che nessuna grammatica usa.

I sette produttori di testi sono semplici ma ripetitivi (F2.4).

In ordine di valore per il lettore: F2.4, F2.2 e F2.3 (piccoli e sicuri), poi D2.1 e la decisione su D2.2/D2.3, poi F2.1. R2.1 è il cambiamento più utile per un neofita, ma anche il più costoso.

#### Passo 3: `02-foresta-attributi`, `02-foresta-comandi`, `02-foresta-effettidistato`, `02-foresta-mappa` (2026-11-13)

Letti per intero i 34 file (3.600 righe), seguendo i chiamanti nei moduli sopra dove serviva (`AttributiSecondari` in `05-foresta-personaggi` per gli attributi, i chiamanti di `Comando` e della mappa). Test dei quattro moduli verdi dopo i commenti.

**Esito (2026-11-13):** applicate:
- **D3.1:** `CalcolatoreAttributiSecondari.radice`, per cui un primario negativo conta come 0, anche nel carico. C'è un test;
- **N3.1:** `ForestaMD.casellaCostruita` dà un `IllegalStateException` che nomina la casella, al posto della `NullPointerException`. C'è un test. Il test «nessuna casella vuota dopo la generazione» c'era già (`GeneratoreDellaForestaTest`);
- **D3.6**;
- **F3.1**, tutte e due le metà: `variazione` e `tempo` in `TestoDeiModificatori`;
- **F3.2:** `Comando.ofPersonaggio` in due righe;
- **F3.3:** le costanti di `Direzioni`;
- **D3.3:** tolti i quattro `package-info` dei pacchetti vuoti. Il loro contenuto è nei documenti dei moduli, e il riferimento in `CLAUDE.md` è corretto.

**D3.4 resta così, per decisione dell'utente:** `RUTTOLOMEO` e `STORPSGORBLIN` sono rimanenze storiche a cui è affezionato. **D3.2 è spostato al passo 9**, scritto nella sua riga della tabella del §11.3. Su richiesta successiva dell'utente, il campo `valore` di `EffettoDiStatoMD` è diventato `durata`. Sono verdi tutti i test del progetto: 3.764, di cui 37 saltati.

**`NullPointerException` possibili**

- *Certi:* nessuno.
- *Plausibili:*
  - **N3.1** `ForestaMD`: `impostaLocazioneVisitata`, `isLocazioneVisitata`, `isLocazioneConosciuta`, l'`impostaLocazioneConosciuta` privata e `salva` leggono la casella senza controllare che esista. Durante la costruzione le caselle non ancora assegnate sono null (lo dicono `ottieniTipoLocazione` e `Foresta.nomeNuovo`, che le controllano). Oggi `GeneratoreDellaForesta` riempie di bosco ogni casella vuota prima di mettere il gruppo sulla mappa, e lo dice in un commento. Chi spostasse quel passo, o chiamasse `Foresta.setPosizioneDelGruppo` durante la costruzione, avrebbe una `NullPointerException`. Una casella rimasta vuota farebbe fallire anche il salvataggio. Proposta: o un controllo con un messaggio chiaro («casella (x, y) non ancora costruita»), o un test che dopo `genera` nessuna casella sia null.
  - **N3.2** `Foresta.getNomeDaMostrare` e `Foresta.nomeDelTempio(CoordinateMD)` usano `getLocazioneMD(…)` senza controllo: hanno lo stesso presupposto di N3.1 (a partita avviata tutte le caselle esistono).
- *Controllati e scartati:*
  - **N1.2 del passo 1** (un valore null in `MappaProprieta.salva`) per le caselle: `LocazioneMD.aggiungiProprieta` passa da `pulisciValore`, che rifiuta il null, e `setNome` toglie la proprietà invece di metterci null. Le chiavi sono costanti. Resta da vedere `MissioneMD` (passo 14).
  - `TestoDeiModificatori.nota(null)`: la nota di un modificatore non è mai null (il costruttore la porta a "").
  - `Comando.getDescrizione()` null: in `BarraIcone` si concatena solo per i comandi in ricarica, che hanno tutti una descrizione (da ricontrollare al passo 26).

**Altri difetti**

- **D3.1 (confermato nel codice)** `CalcolatoreAttributiSecondari` fa la radice quadrata dei primari, e `AttributiSecondari.quantitaModificata` (`05-foresta-personaggi`) li somma ai modificatori senza un limite in basso. Un primario portato sotto zero (Forza 5 con un −10, o una percentuale oltre −100%) dà `NaN`, «non un numero», e il cast `(int)` lo trasforma in 0. Così **l'intero secondario vale 0**, anche se l'altro primario è alto: per esempio la parata con Forza −5 e Destrezza 30. Proposta: in `grezzo` contare un primario negativo come 0, cioè `Math.sqrt(Math.max(0, primario))`. È una riga, ma cambia il comportamento.
- **D3.2** I Javadoc delle costanti di `TipoAttributo` descrivono meccaniche che il gioco non ha: lo scudo di mana di MAGIA, la sopravvivenza a 1 punto vita di FORTUNA, l'attacco furtivo di FURTIVITA, la schivata, il costo in vigore della STANCHEZZA. Ho cercato nel codice e non c'è niente del genere. Per un neofita è la cosa più fuorviante del passo, e ho aggiunto un avviso in testa all'enum. Proposta: riscrivere ogni costante con quello che fa davvero, da fare al passo 9 (combattimento), quando si leggono gli usi. Le descrizioni per il giocatore (`tipo_attributo_it.properties`) sono brevi e corrette.
- **D3.3** Pacchetti che contengono solo il `package-info.java`: `attributi.tipi`, `modificatori.tipi`, `effettidistato.tipi` e `artefatti.tipi` (quest'ultimo del passo 5). Sono quel che resta di quando i tipi stavano in un pacchetto per modulo. Descrivono classi che stanno in `tipi`, e un neofita li apre e li trova vuoti. Proposta: portare il loro testo nel documento del modulo e toglierli. È una cancellazione: decide l'utente.
- **D3.4** `Comando.RUTTOLOMEO` e `STORPSGORBLIN`, «vecchi comandi non più in uso», hanno ancora un'icona in `TipoIcona` e due immagini. `Comando.of(int)` lo usano solo i test. Anche qui togliere è una cancellazione.
- **D3.5** `GeneratoreDellaForesta` mette una città per quadrante: le città sono 4 come i quadranti, e con una quinta città `quadranti.remove(0)` darebbe un errore di indice. `Foresta.getCoordinateLibere` e `GeneratoreDellaForesta.getCoordinateLibere` girano all'infinito se non c'è più una casella libera. Con le dimensioni di oggi (20×20) non succede. Ho messo un commento accanto alle città.
- **D3.6** Il Javadoc di `Foresta.getVarianteDelBosco` parla di un «bosco creato prima che questa proprietà esistesse»: è storia, e i salvataggi non devono restare compatibili. Il ripiego a 1 si può tenere, ma va detto per che cosa serve davvero (una casella costruita a mano dai test).

**Parti in comune**

- **F3.1** `TestoDeiModificatori.durata` e `durataResidua` sono lo stesso `switch` con un prefisso diverso (`DURATA_` / `RESIDUO_`): un metodo privato con il prefisso li riduce a una riga ciascuno. Allo stesso modo `effetto` e `breve` scrivono il valore con lo stesso `switch`.
- **F3.2** `Comando.ofPersonaggio` è una catena di otto `if` che si può scrivere in due righe (`values()[PERSONAGGIO_1.ordinal() + posizione]`, con il controllo dell'intervallo).
- **F3.3** `Direzioni` usa i numeri 6 (caselle al giorno), 36, 50 e 4 senza nome. La regola del progetto (niente valori magici) chiede costanti con nome, per esempio `CASELLE_AL_GIORNO`. Ho aggiunto i commenti, ma le costanti sarebbero meglio.
- *Non vale la pena:* i quattro `getMaxPassiX`/`muoveX` di `Foresta` sono già fattorizzati su `getMaxPassi(dx, dy)` e `muove(dx, dy)`.

**Pezzi che andrebbero riscritti**

- `EffettoDiStatoMD` chiama `valore` la durata (e `getDurata` lo restituisce): rinominare il campo privato in `durata` è sicuro e toglie un inciampo. Intanto l'ho commentato.
- `TipoAttributo` (D3.2).

**Commenti aggiunti**

- `TipoAttributo`: l'avviso sui Javadoc che non descrivono il codice.
- `CalcolatoreAttributiSecondari`: la radice di un negativo.
- `TestoDeiModificatori`: `%+d`.
- `ModificatoreAttributoMD`: la condizione del costruttore.
- `EffettoDiStatoMD`: il campo `valore`.
- `Comando`: l'ordine delle costanti `PERSONAGGIO_n` non si cambia.
- `RichiestaConComandi`: diceva «astratta» e non lo è.
- `ForestaMD`: la griglia in un array, con un esempio.
- `GeneratoreDellaForesta`: `>> 2` e `>> 1` sono divisioni per 4 e per 2; una città per quadrante.
- `Direzioni`: la distanza a piedi, la regola delle direzioni oblique, le 6 caselle al giorno.
- `Foresta`: come si contano i passi; il commento sulla posizione del gruppo, che era finito sopra il metodo sbagliato.
- `Quadrante`: il `- 1`.

**Giudizio**

Moduli piccoli e ordinati, con una buona separazione: il calcolo degli attributi non sa chi li possiede, la mappa non sa chi costruisce il mondo (riceve un `PopolatoreDellaForesta`), i dati prendono il fornitore dall'esterno.

- **Il difetto più serio è D3.1**: un caso limite, ma che dà un risultato assurdo (una parata nulla) senza nessun errore.
- **La cosa più dannosa per chi legge è D3.2**: Javadoc che promettono meccaniche inesistenti. Un neofita che studia `TipoAttributo` impara un gioco che non c'è.
- La mappa è chiara, ma conta su presupposti non scritti nel codice: tutte le caselle esistono (N3.1), quattro città per quattro quadranti (D3.5). Andrebbero resi espliciti con un controllo o un test.
- Restano alcuni residui (D3.3, D3.4) e numeri senza nome (F3.3, e gli spostamenti di bit nel generatore).

#### Passo 4: `02-foresta-notizie`, `03-foresta-danni`, `03-foresta-tutorial` (2026-11-13)

Letti per intero i 17 file (1.550 righe), con i chiamanti dove serviva (`RegistroNotizie` e `StatoDelMotore` nel motore, `AvvioDelMotore` per il tutorial, `SituazioniDellaUI` per le situazioni). Test dei tre moduli verdi dopo i commenti.

**Esito (2026-11-13):** l'utente ha approvato tutto, e tutto è applicato:
- **D4.3:** `SuggerimentiMD.leggi` usa `booleano()`;
- **D4.4:** tolto il `package-info` di `danni.tipi`; corretti `03-foresta-danni.md` e il rimando di `CLAUDE.md`;
- **F4.1:** le 34 reazioni sono su una riga ciascuna, con `stati`, `danni`, `nessuno` e gli import statici delle due enum, e il file è passato da 440 a 291 righe.

Sono verdi tutti i test del progetto: 3.764, di cui 37 saltati.

**`NullPointerException` possibili**

- *Certi:* nessuno.
- *Plausibili:* nessuno.
- *Controllati e scartati:*
  - `NotiziaMD` con id o corpo null, che si salverebbero come il testo "null": l'unico che ne crea una è `RegistroNotizie`, con l'identificativo preso dalla notizia e un corpo già controllato (non vuoto).
  - `RegistroSuggerimenti.impostaGestore(null)`: non lo fa nessuno.
  - `TipoInterazioneConEffettiDiStato.getDannoAdArea()` restituisce null per le reazioni che non colpiscono un'area: è documentato, e i chiamanti lo sanno (da ricontrollare al passo 9).

**Altri difetti**

- **D4.1** I Javadoc di `Notizie` e di `NotizieMD` si contraddicevano: tutti e due dicevano che la regola di rotazione (il nuovo in testa, i più vecchi scartati) stava nell'altra classe. Sta in `NotizieMD`. Corretti.
- **D4.2** I Javadoc di `RegistroSuggerimenti` e di `GestoreSuggerimenti` dicevano che il motore non passa un gestore e che il tutorial non si salva «finché non è finito». Non è più vero: `AvvioDelMotore` passa `GestoreSuggerimentiSuFile`, e il tutorial si salva. Corretti.
- **D4.3** `SuggerimentiMD.leggi` controlla a mano che la prima riga sia `true` o `false`, perché `LettoreCampi.booleano()` leggeva qualunque altro testo come falso. Dal passo 1 (D1.1) `booleano()` fa quel controllo da sé: le otto righe si possono ridurre a `attivo = new LettoreCampi(riga).booleano();`. Il commento, che diceva il contrario, è corretto.
- **D4.4** `03-foresta-danni` ha anch'esso un pacchetto `danni.tipi` con il solo `package-info.java`, sfuggito al passo 3 (D3.3). Va tolto come gli altri quattro, che l'utente ha già approvato.

**Parti in comune**

- **F4.1** `TipoInterazioneConEffettiDiStato`: le 34 reazioni sono scritte con `Collections.singletonList(TipoEffettoDiStato.X)`, `Arrays.asList(...)` e `Collections.emptyList()`, cinque argomenti di quel tipo per costante. Con tre metodi statici di una riga (`stati(...)`, `danni(...)`, `nessuno()`) ogni reazione diventerebbe leggibile in una o due righe, per esempio `ELETTROCUZIONE(stati(BAGNATO), danni(FULMINE), 1.5, nessuno(), nessuno())` (con gli import statici delle due enum). Si risparmiano circa 100 righe, e un neofita vede subito i cinque dati. Ho aggiunto un commento che li elenca.
- *Non vale la pena:* `getDannoAdArea` è uno `switch` su tre costanti. Metterlo fra i dati del costruttore vorrebbe dire aggiungere un sesto argomento a tutte e 34 le costanti.

**Pezzi che andrebbero riscritti:** nessuno.

**Commenti aggiunti**

- `Notizie` e `NotizieMD` (D4.1), `RegistroSuggerimenti` e `GestoreSuggerimenti` (D4.2), `SuggerimentiMD` (D4.3).
- `TipoInterazioneConEffettiDiStato`: i cinque dati di una reazione.
- `TipoDanno`: un refuso, e la precisazione che i commenti delle costanti descrivono le intenzioni.
- `BersaglioDelSuggerimento`: `EnumSet.of`.
- `TipoSuggerimento`: le pagine contate da 1 e il bersaglio che si ripete.
- `MessaggioMD`: un accento.

**Giudizio**

Tre moduli piccoli e puliti, i migliori incontrati finora insieme a quelli del passo 1.

- **Il tutorial** è un esempio di separazione ben riuscita: il registro non sa niente della UI, la UI gli dice che cosa vede con un insieme di `Situazione`, e ogni costante di `TipoSuggerimento` ha accanto un commento che spiega perché sta in quel punto dell'ordine.
- **I danni** sono tabelle ben documentate (ogni reazione ha il suo Javadoc), ma scritte in modo prolisso (F4.1).
- **Le notizie** sono semplici.

I difetti sono tutti di documentazione: Javadoc rimasti indietro rispetto al codice (D4.1, D4.2, D4.3). È lo stesso fenomeno dei documenti `.md` del §6, in piccolo, e ogni passo ne trova qualcuno. Conferma che un commento che descrive il «quando» o il «chi» invecchia più in fretta di uno che descrive il «che cosa».

### 11.5 Proposte che toccano più moduli

Le fattorizzazioni che spostano codice fra moduli si raccolgono qui, con i passi che le hanno trovate, e si decidono a parte.

- **X1 `TipoEvento` (passo 1) — decisa: resta.** Per l'utente è il dizionario centralizzato di tutti i tipi di evento del gioco, e potrà servire ancora; il suo Javadoc ora lo dice. L'enum ha 134 costanti, una per classe di evento, e ogni evento la passa al costruttore di `EventoBase`. Il bus però smista per classe, e il tipo lo legge solo `SnifferBusEventi` (`13-foresta-app-swing`) per scriverlo nel registro, dove basterebbe `evento.getClass().getSimpleName()`. Toglierlo vorrebbe dire eliminare un'enum di 570 righe e un argomento dai costruttori di circa 134 classi, in quasi tutti i moduli. In cambio, aggiungere un evento costerebbe un passo in meno (oggi va aggiunta anche la costante). Si perderebbero i commenti di una riga di ogni costante, che però ripetono il Javadoc delle classi, e il raggruppamento per famiglia, che dicono già i sottopacchetti `comandigiocatore`, `notifiche`, `richieste` e `interni`.
- **X2 La maiuscola iniziale (passo 2) — fatta:** ora c'è `strumenti.Maiuscole.inizialeMaiuscola`. «Il primo carattere in maiuscolo, il resto com'è» è scritto sei volte: `Articoli.inizialeMaiuscola`, `LinguaInglese.inizialeMaiuscola` e `GestoreGrammatiche.capitalizeFirstLetter` (`01-foresta-testi`), `NotificaDiTesto.conInizialeMaiuscola` (`01-foresta-eventi`), `IncaricoInCitta` (`09-foresta-missioni`) e `RivelazioneDellArtefatto` (`12-foresta-ui-comune`). `01-foresta-eventi` non vede `01-foresta-testi`, perché sono allo stesso livello: l'unico posto che vedono tutti è `00-foresta-strumenti`, per esempio una classe `strumenti.Maiuscole` con un metodo statico. Le varianti hanno differenze minime (alcune lasciano com'è un testo che comincia già in maiuscolo, altre accettano null) che si possono unire senza cambiare il comportamento. È una fattorizzazione piccola ma tocca quattro moduli.

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
- Un test che produca tutte le alternative delle grammatiche e le disegni con i font, per prendere le produzioni malformate e i caratteri che il font non ha; con lo stesso passo, decidere quali produzioni sono davvero morte (§5.1, B35e).
