# Assessment di Foresta

Valutazione del progetto al **2026-10-25**, rifatta da zero dopo la divisione in moduli. Le misure sono state prese sul working tree di quel giorno, dopo `mvn clean test`:
- righe e file sono contati con `wc -l`;
- la copertura viene da JaCoCo, sia modulo per modulo sia con tutti i test insieme;
- il grafo dei moduli è ricavato dagli import.

I giudizi sono segnati come **Giudizio**. I difetti ancora aperti stanno nel §5, con i codici della revisione precedente (B35, B41…), che [`todo.md`](todo.md) usa. Le cose da fare per il porting su Android e iOS sono in [`assessment_portabilita.md`](assessment_portabilita.md): qui c'è solo il riassunto.

Questo assessment **non è una nuova lettura integrale dei sorgenti**, come fu la revisione del 2026-10-19. Sono rifatti da capo le misure, l'architettura e la verifica dei difetti aperti. Le ipotesi controllate e scartate allora (§5.2) sono riportate, ma non le ho ricontrollate.

## 1. Sintesi

- **Il gioco è completo e giocabile** da inizio a fine: creazione del gruppo, esplorazione, combattimento a round con tattiche, negozi, incantatore, missioni, intermezzi, trofei, salvataggi, classifica.
- **Il progetto è diviso in 28 moduli Maven, in fila.** Ognuno vede solo quelli sotto di lui, e il compilatore lo garantisce.
  - Le parti del gioco sono sottoprogetti con i loro tipi, i loro dati e i loro contratti per la UI: personaggi, poteri, combattimento, tattiche, mappa, mondo, bottino, economia, statistiche, intermezzi, missioni, bestiario, trofei.
  - Il motore offre alla UI il suo contratto in `foresta-motore-api`, e la UI non vede il motore.
- **I sottoprogetti non sanno chi custodisce i loro dati.** Li ricevono da `ModelloDati` con un fornitore (`setFornitoreDiDati`). Quando non devono sapere che una cosa esiste, la ricevono da chi sta sopra: le fonti di intermezzi, il fornitore della locazione completa.
- **2152 test, tutti verdi** (22 saltati, simulazioni da lanciare a mano). La copertura delle righe con tutti i test è del **77%**.
- **Giudizio:** il progetto è in buona salute, e l'architettura è molto migliorata dalla revisione precedente: il motore è passato da 57.000 a meno di 10.000 righe. Non ci sono difetti aperti di gravità alta o media. Restano:
  - le quattro classi grandi;
  - lo stato statico;
  - qualche residuo della divisione: `foresta-modellodati`, `MeccanicaDeiPoteri`;
  - un po' di codice morto (§5).

## 2. Misure

| Modulo | Codice (file / righe) | Test (file / righe) | Test eseguiti | Copertura dei suoi test | Copertura con tutti i test |
| :--- | ---: | ---: | ---: | ---: | ---: |
| `foresta-strumenti` | 15 / 983 | 8 / 879 | 50 | 99% | 100% |
| `foresta-serializzazione` | 4 / 373 | 4 / 346 | 32 | 100% | 100% |
| `foresta-eventi` | 16 / 1.038 | 2 / 206 | 14 | 92% | 96% |
| `foresta-testi` | 15 / 3.639 | 11 / 3.205 | 299 | 100% | 100% |
| `foresta-comandi` | 2 / 269 | 1 / 97 | 6 | 96% | 100% |
| `foresta-attributi` | 13 / 995 | 6 / 409 | 27 | 100% | 100% |
| `foresta-effettidistato` | 4 / 305 | 3 / 88 | 5 | 100% | 100% |
| `foresta-danni` | 4 / 563 | 4 / 180 | 8 | 100% | 100% |
| `foresta-mappa` | 15 / 2.022 | 7 / 1.101 | 61 | 81% | 97% |
| `foresta-artefatti` | 44 / 5.120 | 19 / 2.860 | 171 | 100% | 100% |
| `foresta-personaggi` | 132 / 19.606 | 24 / 1.933 | 102 | 55% | 79% |
| `foresta-poteri` | 52 / 2.877 | 3 / 193 | 9 | 16% | 94% |
| `foresta-combattimento` | 14 / 2.798 | 11 / 1.642 | 69 | 68% | 93% |
| `foresta-tattiche` | 49 / 4.896 | 17 / 3.169 | 169 | 90% | 92% |
| `foresta-statistiche` | 8 / 757 | 4 / 309 | 14 | 53% | 95% |
| `foresta-intermezzi` | 39 / 3.396 | 5 / 310 | 20 | 28% | 92% |
| `foresta-bottino` | 17 / 1.117 | 1 / 20 | 1 | 11% | 78% |
| `foresta-mondo` | 5 / 1.025 | 3 / 313 | 15 | 32% | 94% |
| `foresta-economia` | 41 / 2.359 | 2 / 152 | 9 | 9% | 64% |
| `foresta-missioni` | 111 / 17.867 | 5 / 363 | 17 | 9% | 89% |
| `foresta-bestiario` | 4 / 309 | 1 / 112 | 7 | 46% | 100% |
| `foresta-trofei` | 12 / 690 | 3 / 206 | 9 | 38% | 100% |
| `foresta-punteggi` | 4 / 221 | 1 / 72 | 4 | n.m. | n.m. |
| `foresta-notizie` | 5 / 368 | 3 / 210 | 10 | n.m. | n.m. |
| `foresta-modellodati` | 2 / 287 | 9 / 916 | 41 | n.m. | n.m. |
| `foresta-motore-api` | 49 / 1.631 | 1 / 32 | 1 | 17% | 91% |
| `foresta-motore` | 60 / 9.686 | 190 / 29.686 | 839 (22 saltati) | 77% | 77% |
| `foresta-swing` | 98 / 23.493 | 14 / 2.028 | 64 | 26% | 52% |
| `foresta-app` | 3 / 902 | 34 / 2.617 | 70 | 28% | 28% |
| **Totale** | **834 / 109.402** | **398 / 53.850** | **2154 (22 saltati)** | | **77%** |

- **n.m.** (non misurata): `foresta-notizie` e `foresta-punteggi` sono nati dopo la misura, separati da `foresta-modellodati`, dal motore e da `foresta-motore-api`; di quei due moduli si sono ricontati classi, righe e test, ma la copertura è da rimisurare.
- **"Copertura dei suoi test"** è quella del rapporto di JaCoCo del modulo: conta solo i test che stanno nel modulo.
- **"Copertura con tutti i test"** unisce i `jacoco.exec` di tutti i moduli e misura ogni modulo con i dati di tutti. Il rapporto aggregato di JaCoCo (`report-aggregate`) non serve: vede solo le dipendenze dirette di `foresta-app`. Per questo l'ho calcolata con un piccolo programma sopra la libreria di JaCoCo.

Fra le due colonne c'è molta distanza, ed è il dato più importante della tabella. Le missioni, per esempio, hanno 17 test loro e sono coperte all'89%: le esercitano gli 843 test di scenario del motore (§3, problema 5).

Le classi più grandi (escluse le tabelle dei font bitmap `DoomdarkFont*`):

| Classe | Modulo | Righe |
| :--- | :--- | ---: |
| `testi/GrammarBean` | testi | 2563 |
| `motore/Automa` | motore | 2415 |
| `locazioni/LocazioneBase` | motore | 2188 |
| `personaggi/PersonaggioBase` | personaggi | 2054 |
| `ui/DisplayableCanvas` | swing | 1781 |
| `tipi/CostantiPersonaggi` | personaggi | 1559 |
| `ui/sfx/TracciatoreLogo` | swing | 1443 |
| `combattimento/CalcolatoreCombattimento` | combattimento | 1326 |
| `missioni/MissioneAPassi` | missioni | 1301 |
| `ui/tattiche/DisplayableCanvasTattiche` | swing | 1203 |
| `tipi/TipoMissione` | missioni | 1013 |

## 3. Architettura

### La catena dei moduli

Dal basso in alto. Ogni livello dipende solo da quelli prima, e le dipendenze sono quelle reali, lette dagli import:

| Livello | Moduli |
| ---: | :--- |
| 0 | `strumenti`, `serializzazione` |
| 1 | `eventi`, `testi` |
| 2 | `comandi`, `attributi`, `effettidistato`, `tipi` |
| 3 | `danni`, `mappa` |
| 4 | `artefatti` |
| 5 | `personaggi` (con i gruppi) |
| 6 | `poteri`, `statistiche`, `bestiario` |
| 7 | `combattimento`, `mondo`, `intermezzi` |
| 8 | `tattiche`, `bottino`, `economia` |
| 9 | `missioni` |
| 10 | `trofei`, `modellodati` |
| 11 | `motore-api` |
| 12 | `motore`, `swing` |
| 13 | `app` |

### Punti di forza

- **Le dipendenze le controlla il compilatore.** Ogni parte del gioco è un modulo con i suoi tipi (`tipi`), i suoi dati (`modellodati`) e i suoi contratti (eventi, viste). Quello che il compilatore non vede, cioè `java.awt` e simili fuori da `foresta-swing`, `Locale.ROOT`, `System.exit` e il `Logger`, lo controllano i test che leggono i sorgenti (`DipendenzeDalToolkitGraficoTest`, `ConvenzioniDiPortabilitaTest`).
- **Le dipendenze vanno dove devono.** Quando una parte deve sapere qualcosa di una parte che sta sopra, la riceve senza conoscerla:
  - `FonteDiIntermezzi` per gli intermezzi delle missioni;
  - `FabbricaIntermezzi.registra` per gli epiloghi delle missioni;
  - `RegistroMissioni.setFornitoreLocazioneCompleta` per il motore.

  Lo stesso schema vale per le cose che una parte possiede ma non sa di avere: `TatticheDeiPersonaggi`, `CantiImparati` e `Cassa` sono legati al gruppo o al personaggio dall'esterno, e la posizione del gruppo la tiene la mappa.
- **La UI legge da viste e scrive con eventi.** `VistaPartita` raccoglie le viste dei sottoprogetti (`VistaMappa`, `VistaCassa`, `VistaGruppoGiocatore`…). Anche il contenuto del gioco, come la storia e i finali, arriva dal motore (`Racconto`): una UI Android non dovrebbe riscriverlo.
- **Un solo thread per la logica.** Il bus consegna sul thread principale, dove girano anche i tick del temporizzatore.
- **Salvataggi di partita.** `LettoreCampi` conserva i campi vuoti, un file troncato dà `IOException`, e la scrittura passa da un file temporaneo. Ogni parte salva i suoi dati con il suo `…MD`.
- **Testabilità.** C'è `Dado` con seme e trucchi per contesto, e c'è `PartitaDiTest`. Tutti i test girano senza UI.
- **Documentazione.** 29 documenti `.md` nella radice, con la mappa in [`foresta.md`](foresta.md). `CLAUDE.md` descrive i moduli e le ricette per aggiungere contenuto.

### Problemi, in ordine di priorità

1. **Classi grandi.** `Automa`, `LocazioneBase` e `PersonaggioBase` superano le 2000 righe. Sono le zone dove la revisione precedente trovò più difetti.
   **Giudizio:** i tagli naturali sono gli stessi di allora:
   - in `Automa`, altri sotto-automi (creazione del gruppo, combattimento, fine partita);
   - in `LocazioneBase`, il combattimento a round in una classe sua;
   - in `PersonaggioBase`, attributi e modificatori, equipaggiamento ed effetti di stato separati.

   Con i moduli il taglio è più facile, perché si vede subito cosa usa cosa.
2. **Stato statico.** Lo stato della partita vive in registri e singleton (`GruppoGiocatore`, `RegistroMissioni`, `Cassa`, `Foresta`…). `StatoDelMotore.azzera()` riparte da un modello dati nuovo e da un automa nuovo.
   **Giudizio:** accettabile per un gioco single-player, a patto di non aggiungere registri statici. I fornitori almeno rendono esplicito dove stanno i dati, e `ModelloDati.installa()` li mette al loro posto all'avvio (`Main`, `PartitaDiTest`).
3. **Residui della divisione.**
   - `foresta-modellodati` contiene solo `ModelloDati`.
   - In `foresta-motore-api` restano `FaseDiGioco`, `ComandiAssociati`, che usa i tipi di personaggi, poteri e missioni, e le richieste di apertura di trofei e bestiario.
   - **`MeccanicaDeiPoteri`:** combattimento e poteri sono ancora in ordine inverso. Il combattimento dipende dai poteri (anche per `TipoAzione` ed `EsitoAzione`), e i poteri arrivano al combattimento solo attraverso l'interfaccia e il `ServiceLoader`.
4. **Personaggi e gruppi nello stesso modulo.** È una scelta voluta. `PersonaggioBase` e `GruppoGiocatore` si conoscono attraverso i singleton, in sette punti: da che parte sta un personaggio, quanti della sua specie ci sono, il gruppo pieno.
5. **I test stanno tutti in alto.** Missioni, economia, poteri e bottino hanno pochi test loro (copertura propria dal 9 al 16%). Li esercitano i test di scenario del motore, che li coprono dal 64 al 94%.
   **Giudizio:** finché si lancia sempre tutta la suite non è un problema. Ma un `mvn test -pl foresta-missioni` dice poco, ed è meglio scrivere i test nuovi di una parte nel suo modulo, quando non serve tutta la partita.
6. **Pacchetti divisi fra moduli.** `tipi` sta in 19 moduli, `modellodati` in 15, `eventi.interni` in 13. È una convenzione voluta (CLAUDE.md), e con Java 8 e su Android funziona. Ma non è compatibile con i moduli di Java 9 (JPMS), se un giorno servissero.

## 4. Qualità del codice

- **Convenzioni controllate dai test**:
  - niente toolkit grafico fuori dalla UI;
  - `Locale.ROOT` nelle chiamate che dipendono dalla lingua;
  - niente `System.exit` sotto `foresta-app`;
  - il `Logger` con il fornitore del testo;
  - le chiavi dei `.properties` delle enum.
- **`TODO`/`FIXME`:** sono 14, tutti su funzioni future o rifiniture.
- **Codice morto:** pochi pezzi, elencati nel §5.
- **Test saltati:** i 22 test saltati sono simulazioni da lanciare a mano, non test rotti.
- **Rapporti stantii.** Dopo uno spostamento serve `mvn clean`. Senza, `target/` tiene classi e risorse che non esistono più. E la compilazione incrementale può non ricompilare i test di un modulo quando cambia un modulo sotto.

## 5. Difetti aperti

### 5.1 Ricontrollati sul codice del 2026-10-25

Sono tutti di gravità bassa. I percorsi sono relativi a `…/` del modulo indicato.

- **B35a (`foresta-personaggi`)** Codice morto in `PersonaggioBase`:
  - `calcolaStanchezza`, `subCarisma`, `addForza`, `addDestrezza`, `addCostituzione`, `addIntelligenza` e `addSaggezza` non li chiama nessuno;
  - `getQuantitaEffettoDiStato` la usa solo un test;
  - `puoPrendere(Artefatto)` non la chiama nessuno: si usa `puoPrendere(double)`.
- **B35d (`foresta-missioni`)** Due passi pronti di `MissioneAPassi` li usano solo i test: `evitaCombattimento` e `getPassiConIntermezzoInAttesa`.
- **B35e (`foresta-testi`)** In `locande.txt` la produzione `NOTIZIE_CITTA` (12 notizie) non esce mai: le notizie si pescano da `NOTIZIE_<identificativo della locanda>`, e nessuna locanda ha l'identificativo `CITTA`. Le produzioni morte di `fiabe.txt` non le ho ricontrollate.
- **B41c (`foresta-swing`)** `ui/testo/DoomdarkTextRectangle` (quello non 2x) e `img/icone/NegozioAlchimistaPrecedente.gif` non li usa nessuno.

### 5.2 Ipotesi controllate e scartate nella revisione del 2026-10-19 (non ricontrollate)

- **Concorrenza delle collezioni.** Non c'è nessuna `ConcurrentModificationException` raggiungibile: si scorrono sempre copie, e il bus usa `CopyOnWriteArrayList`.
- **Ricompense ed esperienza.** Niente doppie ricompense o doppi completamenti di missione, niente esperienza duplicata.
- **Oggetti e prezzi.** Scambi, acquisti, vendite e prelievi non duplicano né perdono artefatti. La vendita arriva al massimo al 75%, l'acquisto parte almeno dall'80%.
- **`Dado` e trucchi.** I limiti sono coerenti, e un trucco fuori intervallo viene ignorato.
- **Risorse, grammatiche e testi delle enum.** Le immagini referenziate esistono tutte e le grammatiche non hanno riferimenti mancanti. Per le chiavi delle enum c'è un test.
- **Cache delle scritte** (`ImageCache.cacheDinamica`, `WeakReference`). È stata misurata: ricostruisce dallo 0,3 al 2,6% delle scritte secondo la spazzatura prodotta. Su telefono potrebbe convenire passare a `SoftReference`.
- **Ordinali di `Numeri`.** Valgono da 1 a 41, e nessun chiamante esce da lì.
- **Combattimento.** Niente round infiniti, e la fine del combattimento si intercetta sempre.

## 6. Portabilità (riassunto)

Il dettaglio è in [`assessment_portabilita.md`](assessment_portabilita.md).

- **Già pronto:**
  - nessun modulo sotto `foresta-swing` usa il toolkit grafico;
  - la cartella dei file si può iniettare;
  - l'uscita dal gioco è affidata alla piattaforma (`NotificaUscitaDalGioco`);
  - i contenuti (storia, finali, testi delle enum, grammatiche) stanno sotto la UI.
- **Ancora aperto:**
  - le costanti di bilanciamento sono codice (`Costanti*`), non dati;
  - la UI Swing chiude il processo da sola con `EXIT_ON_CLOSE` (`ForestaUI`), senza passare dal motore.
- **Il grosso resta la UI**, che va riscritta (libGDX è la scelta più naturale per restare in Java), con l'input ripensato per il tocco.

## 7. Rischi tecnici

| Rischio | Fatto | Gravità |
| :--- | :--- | :--- |
| Copertura bassa della UI e di `foresta-app` | 52% e 28% con tutti i test | Media: la UI si prova poco, e una nuova piattaforma la riscriverà comunque |
| Copertura dell'economia | 64% con tutti i test, 9% con i suoi | Media: `offerte` e i prezzi sono una zona delicata per il bilanciamento |
| Copia delle costanti `static final` e `target/` stantii | Una compilazione incrementale parziale può tenere valori o file vecchi | Bassa: basta `mvn clean` |
| Modalità verticale | Rotta: barra icone sovrapposta, area di contenuto fissa a 640 px (`todo.md`, `motore_grafico.md` §12) | Bassa sul desktop; rimandata |

## 8. Prossimi passi consigliati

1. **Pulizia piccola:**
   - togliere il codice morto del §5.
2. **Finire la divisione:**
   - le richieste di trofei e bestiario nei loro moduli;
   - decidere se invertire combattimento e poteri e togliere `MeccanicaDeiPoteri`.
3. **Tagliare le classi grandi** quando si interviene su di loro (§3, problema 1). Il candidato più naturale è il combattimento a round di `LocazioneBase`.
4. **Test nei moduli:** i test nuovi di una parte vanno nel suo modulo, quando non serve tutta la partita (§3, problema 6).
5. **Bilanciamento**, come da [`todo.md`](todo.md); la **modalità verticale** per ultima.

## Osservazioni

- **Come è stato fatto.** Rispetto alla revisione del 2026-10-19 sono rifatti da capo:
  - le misure: righe, test, copertura modulo per modulo e con tutti i test;
  - il grafo dei moduli, dagli import di ogni file, confrontato con i pom;
  - le classi grandi, i pacchetti divisi e i fornitori;
  - la verifica dei difetti che restavano aperti.

  Non è stata rifatta la lettura integrale dei sorgenti.
- **La copertura con tutti i test** viene da un programma di poche righe sopra `org.jacoco.core`. Unisce i `jacoco.exec` di tutti i moduli e analizza le `target/classes` di ciascuno. Il programma non è nel progetto.
- **Rispetto alla revisione precedente.** Allora i moduli erano 6, e il motore aveva 368 file e 57.200 righe. Ora sono 28, e il motore ha 60 file e 9.700 righe. I test sono passati da 1672 a 2152.

## Da fare / idee aperte

- Una nuova lettura integrale dei sorgenti, modulo per modulo, quando la divisione sarà finita.
- Mettere nel progetto la misura della copertura con tutti i test, per esempio come profilo Maven o come piccolo strumento in `risorse_e_documenti_vari/`.
- Un test che produca tutte le alternative delle grammatiche e le disegni con i font, per prendere le produzioni malformate e i caratteri che il font non ha.
