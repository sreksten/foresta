# Assessment di Foresta

Valutazione del codice al **2026-10-17**, rifatta da zero. I numeri sono misurati sul working tree di quel giorno (`wc -l`, `grep`, JaCoCo da `mvn test`); i giudizi sono segnati come **Giudizio**. Le cose da fare per il porting su Android e iOS sono in [`assessment_portabilita.md`](assessment_portabilita.md): qui c'è solo il riassunto.

## 1. Sintesi

- Il gioco è **completo e giocabile** da inizio a fine (creazione del gruppo, esplorazione, combattimento a round con tattiche, negozi, incantatore, missioni, intermezzi, trofei, salvataggi, classifica).
- La **separazione fra le parti è garantita dal compilatore**: sei moduli Maven in fila (tipi, modellodati, contratti, motore, swing, app). La UI non vede il motore, ma soltanto viste in sola lettura ed eventi. Era il limite principale dell'assessment del 2026-10-05, ed è risolto.
- La **base di test è ampia e veloce**: 1355 test verdi (16 saltati), 81% delle righe del motore coperte, e test di scenario che giocano partite intere senza UI.
- Il **debito tecnico è concentrato** in quattro classi del motore che continuano a crescere (`PersonaggioBase`, `Automa`, `LocazioneBase`, `GrammarBean`) e nello stato statico (registri e singleton).
- Il problema che più pesa sul gioco non è nel codice ma nel **bilanciamento**: boss, incantesimi a più bersagli ed economia (vedi [`todo.md`](todo.md)).
- **Giudizio:** il progetto è in buona salute e ben documentato. Il codice è pronto per un porting del motore; la parte cara resta la UI, che va riscritta. Conviene ridurre le classi grandi un pezzo alla volta, quando si interviene su di loro.

## 2. Misure

| Modulo | Codice (file / righe) | Test (file / righe) | Test eseguiti | Copertura righe (JaCoCo) |
| :--- | ---: | ---: | ---: | ---: |
| `foresta-tipi` | 57 / 6.100 | 8 / 570 | 31 | 57% |
| `foresta-modellodati` | 36 / 5.400 | 11 / 1.400 | 70 | 60%* |
| `foresta-contratti` | 168 / 7.200 | 9 / 640 | 39 | 40%* |
| `foresta-motore` | 356 / 55.000 | 185 / 30.500 | 1176 (16 saltati) | 81% |
| `foresta-swing` | 95 / 22.000 | 12 / 1.500 | 52 | 26%* |
| `foresta-app` | 3 / 740 | 19 / 1.600 | 34 | 26% |
| **Totale** | **715 / 96.500** | **244 / 36.200** | **1355** | **62%** |

\* JaCoCo conta solo i test del modulo stesso: modellodati e contratti sono esercitati molto di più dai test del motore, e la UI dai test di `foresta-app`. Le percentuali di questi moduli sono quindi più basse del vero.

Nel motore la copertura è alta quasi ovunque (`trofei` 100%, `testi` 95%, `combattimento` 94%, `intermezzi` 94%, `canti` 93%, `tattiche` 88%, `missioni` 86%, `motore` 84%). È bassa in `offerte` (23%), `salvataggi` (64%), `personaggi` (68%) e `locazioni` (71%).

Le classi più grandi (escluse le tabelle dei font bitmap `DoomdarkFont*`):

| Classe | Righe | Note | Il 2026-10-05 |
| :--- | ---: | :--- | ---: |
| `testi/GrammarBean` | 2533 | Motore di grammatiche, con un suo manuale e assessment ([`GrammarBean.md`](GrammarBean.md)) | — |
| `personaggi/PersonaggioBase` | 2342 | 197 metodi; 5 dei 13 `TODO`/`FIXME` del motore | 2072 |
| `motore/Automa` | 2288 | 32 stati, 70 `case`, 35 campi, 119 metodi | 1976 |
| `locazioni/LocazioneBase` | 1944 | Combattimento a round, azioni, canti, sfida | 1522 |
| `ui/DisplayableCanvas` | 1631 | Il canvas principale | 1296 |
| `tipi/Costanti` | 1613 | 1353 costanti di bilanciamento | 2054 |
| `missioni/MissioneAPassi` | 1274 | | — |
| `combattimento/CalcolatoreCombattimento` | 1247 | 20 metodi statici pubblici | — |
| `ui/tattiche/DisplayableCanvasTattiche` | 1136 | | — |
| `tipi/TipoMissione` | 986 | Spostato in `tipi` dal refactoring in corso | — |

## 3. Architettura

### Punti di forza

- **Moduli in fila, controllati dal compilatore.** Ogni modulo vede solo quelli sopra di lui. Quello che il compilatore non vede, cioè `java.awt`, `javax.swing`, `javax.imageio` e `javax.sound` fuori da `foresta-swing`, `Locale.ROOT`, `System.exit` e il `Logger`, lo controllano i test che leggono i sorgenti (`DipendenzeDalToolkitGraficoTest`, `ConvenzioniDiPortabilitaTest`).
- **La UI legge da viste e scrive con eventi.** `VistaPartita` e `VistaScambio` sono in sola lettura, e verso il motore vanno solo comandi con identificativi. Anche le collezioni del modello dati sono esposte in sola lettura, con metodi espliciti per cambiarle.
- **Un solo thread per la logica.** `ThreadPrincipale` (sul desktop l'EDT) esegue motore e UI. Il temporizzatore pianifica gli impulsi e scarta quelli superati da un cambio di stato. Il bus isola gli errori degli iscritti (`motore_di_gioco.md` §2 e §4).
- **Punti di aggancio per la piattaforma.** Le piattaforme si agganciano a:
  - il ciclo di vita, con `inBackground`/`inPrimoPiano` e il salvataggio automatico;
  - l'uscita, con `NotificaUscitaDalGioco`;
  - la cartella dei file, con `GestoreSuFile(File)`;
  - la destinazione del log;
  - la lingua dei testi, con `Testi`.

  Il motore usa solo `java.io` e `java.util`.
- **Testabilità.** Gli strumenti sono:
  - `Dado`, l'unica sorgente di caso, con seme e lanci truccati per contesto;
  - `PartitaDiTest`, che guida una partita dal bus;
  - il temporizzatore manuale;
  - le simulazioni e il giocatore automatico per il bilanciamento.

  Tutti i test girano senza UI.
- **Contenuto a basso costo.** Le missioni a passi hanno un vocabolario piccolo di passi riutilizzabili. Le grammatiche producono nomi, notizie, testi di missione e artefatti. Gli intermezzi sono a pagine, con un'anteprima per gli autori.
- **Documentazione.** Ci sono 27 documenti `.md` nella radice, con la mappa in [`foresta.md`](foresta.md). La revisione dei javadoc è completa: un `package-info.java` per pacchetto, e javadoc per tutte le classi e per i metodi non banali.

### Problemi, in ordine di priorità

1. **Classi grandi che crescono.** Dal 2026-10-05:
   - `PersonaggioBase` è passata da 2072 a 2342 righe;
   - `Automa` da 1976 a 2288;
   - `LocazioneBase` da 1522 a 1944.

   Ogni funzione nuova aggiunge a `Automa` uno stato, un gestore e qualche campo, e a `LocazioneBase` un ramo del combattimento. `Costanti` invece è scesa da 2054 a 1613 righe, perché i testi sono andati nei file di risorse.
   **Giudizio:** è il debito più concreto. Ci sono tagli naturali da fare quando si tocca la zona:
   - per `Automa`, altri sotto-automi come `AutomaInventario` e `AutomaIncantatore` (per esempio creazione del gruppo, combattimento, fine partita);
   - per `LocazioneBase`, il combattimento a round in una classe sua;
   - per `PersonaggioBase`, attributi e modificatori da una parte, equipaggiamento dall'altra, effetti di stato da un'altra ancora.
2. **Stato statico.** Lo stato della partita vive in registri e singleton:
   - `getIstanza()` compare in 64 file del motore, `ModelloDati.getIstanza()` in 15;
   - alcune classi sono fatte di soli metodi statici: `RegistroMissioni` (36 metodi statici pubblici), `ProduttoreDiTestiCasuale` (30), `Foresta` (28), `CalcolatoreCombattimento` (20), `RegoleIncantatura` (17);
   - nel motore ci sono 23 campi statici non `final`.

   `StatoDelMotore.azzera()` e `PartitaDiTest` riportano tutto a vuoto, quindi i test funzionano. Però un processo tiene una sola partita, e una piattaforma che chiude l'app perde lo stato. Il salvataggio automatico lo copre, ma non lo elimina.
   **Giudizio:** per un gioco single-player è accettabile. Si può tenere così, a patto di non aggiungere altri registri statici.
3. **Il motore è un modulo solo.** I suoi pacchetti si richiamano a vicenda: `motore`, `personaggi`, `locazioni`, `missioni` e `oggetti` sono un unico grafo. Il primo taglio possibile è il modulo `foresta-missioni`, con il piano in [`todo.md`](todo.md). Il refactoring in corso, che porta `TipoMissione`, `CategoriaMissione` e `MissionePredefinita` in `tipi`, va in quella direzione.
4. **Due stili di missione.** 12 classi estendono ancora `MissioneBase`, cioè le principali e le secondarie più vecchie. Altre 10 estendono `MissioneAPassi` direttamente, più le loro figlie. Le prime non hanno intermezzi per passo, domande, contatori e ripieghi (`gestione_missioni.md` §12).
   **Giudizio:** conviene portarle sui passi solo quando se ne cambia il contenuto.
5. **Il salvataggio fa parte del modello dati.** Le classi `…MD` portano `salva` e `leggi`, il formato a `|` non ha numero di versione, e un nome di costante cambiato rompe i salvataggi vecchi: è successo con `BARDO_DELLE_LOCANDE`, sistemato a mano.
   La politica attuale è che i salvataggi non debbano restare compatibili. Se cambiasse, servirebbero:
   - un numero di versione;
   - alias per le costanti rinominate;
   - migrazioni;
   - salvataggi d'esempio da rileggere nei test.

   Il formato (testo o JSON) conta meno. È una decisione ancora aperta.

## 4. Qualità del codice

- **Gestione degli errori.** Ci sono 15 `catch` generici (`Exception`, `Throwable`, `RuntimeException`), quasi tutti nei gestori su file (salvataggi, punteggi, trofei) e in `GrammarBean`. È una scelta difendibile per l'I/O, dove un file rovinato non deve fermare il gioco. `printStackTrace` compare solo nella destinazione di log su console. `System.out` compare solo lì, in `ImageCache` e in righe commentate di `Finestra`.
- **Thread.** I thread si creano solo in quattro punti:
  - nel temporizzatore desktop (`TemporizzatoreJ2SE`);
  - nel precaricamento di `Automa`, che si può sostituire perché il costruttore principale riceve un `Executor`;
  - nella UI: ridisegno, `ImageCache`, `ForestaUI`.
- **`instanceof` nel motore:** 30. Pochi per un dominio con molte classi di personaggi e oggetti; le capacità si decidono per lo più dal `TipoPersonaggio` (vedi [`bardo.md`](bardo.md)).
- **`TODO`/`FIXME`:** 16 in tutto, 13 nel motore (5 in `PersonaggioBase`) e 3 nella UI (2 in `DisplayableCanvas`, sul vecchio flusso dell'oroscopo). Quelli generali stanno in [`todo.md`](todo.md).
- **Test saltati.** I 16 test saltati sono simulazioni e analisi da lanciare a mano: `SimulazionePartiteTest`, `TestScontriDiGruppo`, `AnalisiBossTest`, `TestMonteCarloMatrix`, `TestLogoramentoInizioPartita`, `TestGenerazionePersonaggi`. Non sono test rotti.
- **Rapporti stantii senza `clean`.** `target/surefire-reports` conserva i rapporti di classi di test che non esistono più (`Tmp*`, `Scratch*`): sommandoli si contavano 1402 test invece di 1355. E il `jacoco.exec`, che si accumula da un'esecuzione all'altra, teneva nel rapporto classi già rinominate (`ClasseMissione`, `SupertipoMissione`). Dopo una rinomina o per misurare serve `mvn clean test`.

## 5. Portabilità (riassunto)

Il dettaglio è in [`assessment_portabilita.md`](assessment_portabilita.md).

- **Già pronto:** il motore usa solo `java.io` e `java.util`, non ha dipendenze dal toolkit grafico, ha la cartella dei file iniettabile, il ciclo di vita dell'app e l'uscita affidata alla piattaforma.
- **I testi delle enum di `tipi` sono in file per lingua.** Restano nel codice i testi del motore e delle schermate, alcune enum fuori da `tipi` (`NegozioInScena`, `Mandante`) e `FUGA_IGNOMINIA_NOTA`.
- **Ancora aperto:**
  - `Costanti` come dati;
  - un salvataggio automatico più frequente;
  - il ciclo di vita chiamato da una UI vera;
  - le uscite dal processo della UI Swing (`System.exit` in `BufferedImageBuilder`, `addShutdownHook` in `ImageCache`, `EXIT_ON_CLOSE`).
- **Il grosso resta la UI.** Va riscritta (libGDX è la scelta più naturale per restare in Java) e va ripensato l'input per il tocco.

## 6. Rischi tecnici

| Rischio | Fatto | Gravità |
| :--- | :--- | :--- |
| Notizie malformate | `cercaNotiziaLocanda` cerca il primo `-` con `indexOf` e chiama `substring` senza controllare il risultato (`testi/ProduttoreDiTestiCasuale.java:344-345`). Un'alternativa della grammatica senza `-` dà un'eccezione. Dopo i tentativi la ricerca restituisce `null`, e non lancia più eccezioni | Media: un'eccezione sul thread principale all'uscita da una locanda |
| Copertura di negozi e salvataggi | `offerte` 23%, `salvataggi` 64% | Media: sono le parti in cui un errore costa monete o partite |
| UI poco coperta | `foresta-swing` 26% con i soli test suoi | Media: i bug di layout si scoprono giocando |
| Costanti rinominate e salvataggi | Nel salvataggio finiscono i nomi delle costanti delle enum | Bassa finché la compatibilità non è un obiettivo |
| Copia delle costanti `static final` | I primitivi di `Costanti` sono copiati nelle classi che li usano: una compilazione incrementale parziale può tenere valori vecchi | Bassa: basta `mvn clean` dopo un ritocco di bilanciamento |
| Modalità verticale | Rotta: barra icone sovrapposta e finestra larga al massimo 400 px (`todo.md`) | Bassa sul desktop; da ripensare comunque per i telefoni |

## 7. Prossimi passi consigliati

1. **Bilanciamento.** È la cosa che più incide sul gioco:
   - ripartire il danno degli incantesimi a più bersagli, e adeguare la valutazione delle tattiche;
   - rendere i boss scalfibili;
   - rivedere l'economia di metà partita.

   Il piano è in [`todo.md`](todo.md) e [`artefatti_e_incantamenti.md`](artefatti_e_incantamenti.md).
2. **Notizie robuste.** Controllare il formato `ID-testo` al caricamento della grammatica, oppure con un test che produca tutte le alternative di `locande.txt`, e scartare la riga malformata invece di lanciare.
3. **Test su `offerte` e `salvataggi`.** Servono il giro completo di acquisto e vendita con i prezzi, e una lettura e scrittura di ogni slot, dell'automatico e dei casi di file rovinato.
4. **Tagliare `Automa`, `LocazioneBase` e `PersonaggioBase`** quando si interviene su di loro (vedi §3).
5. **Decidere sulla compatibilità dei salvataggi** (§3, punto 5), prima di altre rinomine di costanti.
6. **Modulo `foresta-missioni`**, seguendo il piano di [`todo.md`](todo.md), dopo aver chiuso il refactoring in corso dei tipi delle missioni.

## Osservazioni

- **Come sono fatte le misure.** Righe e file sono contati con `wc -l` sui sorgenti `src/main` e `src/test`, senza `risorse_e_documenti_vari`. I test e la copertura vengono da `mvn clean test` del 2026-10-17 (i `jacoco.csv` dei moduli).
- **Un refactoring era in corso.** Le misure includono un refactoring non ancora committato, che porta i tipi degli incantesimi e delle missioni in `tipi`: `ClasseIncantesimo` diventa `TipoIncantesimo`, il vecchio `TipoIncantesimo` diventa `CategoriaIncantesimo`, `SupertipoMissione` diventa `CategoriaMissione` e `ClasseMissione` diventa `MissionePredefinita`.
- **Due difetti trovati durante l'assessment.** Facevano fallire `foresta-tipi`, e sono stati corretti:
  - la nota `BARDO_LOCANDA.COME` era rimasta indietro dopo la rinomina in `BARDO_DELLE_LOCANDE`;
  - il file `classe_incantesimo_it.properties` non era stato rinominato insieme all'enum.

  Tutti e due li hanno presi i test sui file dei testi (`NoteDeiPersonaggiTest`, `TestiDelleEnumTest`).
- **Due difetti del 2026-10-05 non ci sono più.** Allora l'interfaccia `FornitoreMissione` non aveva utilizzatori, e la ricerca delle notizie lanciava un'eccezione dopo i tentativi. L'interfaccia non esiste più, e la ricerca ora restituisce `null`.

## Da fare / idee aperte

- Ripetere le misure dopo ogni grande intervento, e confrontare le righe delle classi della tabella del §2 per vedere se il debito scende.
- Valutare un report JaCoCo aggregato (`report-aggregate` in `foresta-app`), che conti la copertura di modellodati, contratti e swing data dai test degli altri moduli.
