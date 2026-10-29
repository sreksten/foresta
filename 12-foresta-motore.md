# 12-foresta-motore

**Il motore di gioco**: la macchina a stati che guida la partita (`Automa`), le locazioni vive in cui il gruppo si trova, la costruzione del mondo di una partita nuova, la ricostruzione di una partita caricata, la raccolta del bottino, la vista che il motore dà alla UI, i salvataggi su file e quel che serve per avviare tutto il resto. È il «resto» del progetto: quel che non appartiene a nessun sottoprogetto perché *mette insieme* tutti gli altri.

Sta in cima alla catena delle regole (sotto c'è solo il contratto verso la UI) e **non conosce Swing**: il motore gira anche senza interfaccia, e i test giocano partite intere così. Parla alla UI solo con eventi e con la vista in sola lettura `VistaPartita`.

## Dipendenze

**Dichiarate nel pom:** `11-foresta-motore-api`, `05-foresta-personaggi`, `07-foresta-poteri`, `06-foresta-combattimento`, `08-foresta-tattiche`, `02-foresta-mappa`.

**Lo dichiarano:** `13-foresta-app-swing`.

Le dipendenze sono transitive: un modulo vede anche quello che vedono i moduli da cui dipende, quindi il compilatore garantisce la separazione fra i moduli che non si raggiungono.

Dichiara `11-foresta-motore-api` (i contratti con la UI e per transitività il dominio: trofei, missioni, bottino, intermezzi, economia, mondo, bestiario…) e direttamente personaggi, poteri, combattimento, tattiche e mappa, i moduli di cui usa le classi in modo diretto. Nessun modulo di regole lo conosce: **il motore è il solo a vedere tutto**.

## Funzionamento

### L'`Automa`: la macchina a stati

`Automa` (circa 2400 righe) è una macchina a stati finiti **reattiva, senza ciclo di gioco**: avanza solo in risposta a due stimoli, i comandi del giocatore (`processaComando`) e gli impulsi del temporizzatore (`tick`, che arriva come `Comando.TIMER`: per esempio un round di combattimento al secondo). Implementa `ControlloreDiGioco`.

Ogni `Stato` (51) ha due gestori: uno di ingresso, per quando ci si arriva senza un comando, e uno di comando, per quando il giocatore sceglie qualcosa. I gestori impostano il nuovo stato, pubblicano le conseguenze sul bus e dicono se fermarsi in attesa o proseguire da soli con una transizione automatica. **Non esiste una tabella centrale delle transizioni**: le regole «da quale stato a quale» stanno nei metodi `entraInStato…` e `gestisciComandoInStato…`. Gli stati si raggruppano così:

| Gruppo | Stati |
| :--- | :--- |
| avvio | `LOGO_INIZIALE`, `INTRO`, nome, sesso e classe del personaggio, `PRE_GAME_SELEZIONE_SALVATAGGIO_DA_LEGGERE`, `INIZIO_GIOCO` |
| ciclo di una locazione | `INIZIO_LOCAZIONE`, `INTERMEZZO`, `PREPARAZIONE_LOCAZIONE`, `IN_LOCAZIONE`, `IN_COMBATTIMENTO`, `FINE_LOCAZIONE` (e `…_2`), `ATTESA_DIREZIONE`, `SCELTA_DIREZIONE`, `SCELTA_PASSI` |
| scelte | personaggio (automatica, qualsiasi, manuale), destinatario di un oggetto, incantesimo da lanciare, sì/no, risposta a una missione, pozioni, resurrezione (formulante, bersaglio, esecuzione) |
| schermate | `INVENTARIO`, `TROFEI`, `BESTIARIO`, `TATTICHE`, `MAPPA`, `INGRESSO_NEGOZIO`, `ACCAMPAMENTO`, `SELEZIONE_SALVATAGGIO_DA_SCRIVERE`, `CONFERMA_USCITA` |
| fine partita | `GIOCO_PERSO`, `GIOCO_VINTO`, `STATISTICHE`, `ATTESA_NOME_PUNTEGGI` → di nuovo `INTRO` |

**Il ciclo di una locazione** è il filo conduttore: `INIZIO_LOCAZIONE` (controllo delle missioni «prima», evento della linea temporale, intermezzi di inizio) → `PREPARAZIONE_LOCAZIONE` (si costruisce la locazione e la si popola; missioni «dentro») → `IN_LOCAZIONE` ⇄ `IN_COMBATTIMENTO`/negozi/mappa/inventario → `FINE_LOCAZIONE` (raccolta del bottino, missioni «dopo», intermezzi di fine, salvataggio automatico in memoria) → scelta della direzione e dei passi → il tempo avanza → nuova casella.

**Ciclo di vita dell'app**: `inBackground()` sospende il temporizzatore e scrive il salvataggio automatico su file, `inPrimoPiano()` lo fa ripartire, `riprendiPartitaInterrotta()` riprende la partita dalla schermata iniziale.

Gli scambi di artefatti hanno automi propri: `AutomaInventario` (fra il gruppo e un suo personaggio, senza costi), `AutomaAcquistiArtefatti` (con un commerciante, ogni spostamento è un acquisto o una vendita), `AutomaIncantatore` (il banco di lavoro) sopra `AutomaScambiatoreArtefatti`. La UI li vede come `VistaScambio` e comanda con `ComandoScambioArtefatto`.

### Lo stato della partita

`StatoDelMotore` è il punto solo da cui si prepara e si azzera lo stato statico, che vive per tutto il processo. `registraComponenti()` (da chiamare una sola volta, dopo aver installato il thread principale) iscrive al bus tutti i componenti che tengono il conto di ciò che succede: le notizie, le statistiche, i trofei, il bestiario, i magazzini, il registro delle missioni (con il fornitore «la locazione è completa»), gli intermezzi che il loro modulo non conosce, il commercio e la meccanica dello scontro dei poteri. `azzera()` riporta tutto a uno stato vuoto, per i test e per ripartire pulito prima di riprendere. Una partita nuova la prepara l'`Automa`, una caricata la prepara `RiletturaPartita.ricostruisci()`: dopo che un `GestoreSalvataggi` ha letto la partita e installato il nuovo `ModelloDati`, ricalcola lo stato derivato che non si salva (gli attributi secondari, il gruppo collegato al nuovo modello, il registro delle missioni).

### Il mondo e le locazioni

- **`CostruttoreDellaForesta`** fa nascere il mondo di una partita nuova sulla mappa: azzera i registri e popola la Foresta mentre nasce (i residenti delle città, i magazzini dei negozi, i personaggi nelle locande, gli artefatti nei templi). Implementa il `PopolatoreDellaForesta` della mappa.
- **`Locazione`** è l'interfaccia di un luogo; **`LocazioneBase`** (circa 2200 righe) ha il comportamento comune: crea gli incontri e gli oggetti, e gestisce il combattimento a round, gli incantesimi, i canti e le abilità, la corruzione, l'amicizia, la fuga, il passare inosservati, i duelli, la sfida a carta, forbici e sasso. Le sottoclassi: `Bosco`, `Radura`, `Palude`, `Grotta`, `Rovine`, `Tempio`, `Locanda`, le quattro città (`Citta` + `CittaNyena`, `…Malgaard`, `…Ruuna`, `…Fleena`), i cinque castelli (con cofani: Idra, Lich, Minotauro, Strega; il Drago per lo scontro finale) e le botteghe sparse per la Foresta (`Bottega*`, con `GestoreBotteghe` che regge le quattro botteghe come in città). Le locazioni sono di tre generi (`CategoriaLocazione`): ordinarie, città e castelli, questi ultimi `LocazioneUnica`.
- **`FabbricaLocazioni`** costruisce quella di ogni `TipoLocazione`, e **`LocazioneCorrente`** tiene la locazione viva in cui si trova il gruppo: dura quanto la visita e poi si butta. Ciò che deve restare (nome, visitata, completata) sta nel `LocazioneMD` della casella.
- **`RaccoltaBottino`** decide che cosa succede quando il gruppo prende il bottino (monete e pietre nella cassa, il cofano si apre, l'oggetto di una missione la avvisa, un artefatto va a chi può usarlo o all'inventario) e assegna i punti del suo valore. **`BottinoDegliAvversari`** dà ai gruppi avversari le pergamene e le pozioni che portano con sé, che se non usate diventano bottino. **`Pernottamento`** è la notte del gruppo (racconto, orologio, eventi del giorno).

### Verso la UI

`VistaPartitaMotore` è l'implementazione di `VistaPartita`: non tiene stato, a ogni chiamata delega ai gruppi correnti e alle classi statiche, così vale anche dopo una partita nuova o riletta. `StrumentiDiSviluppoMotore` implementa gli strumenti della finestra di prova; `DotazioneDiProva` dà al gruppo in modalità di prova scorte abbondanti, la Falce del Tristo Mietitore, due spade che mettono un modificatore sul bersaglio e la mappa scoperta. `Notizie` è la facciata su `NotizieMD`: gli ultimi messaggi li prende dal bus, le notizie da chi le produce. `Racconto` ha l'introduzione e i finali, che il motore dà alla UI così ogni piattaforma racconta la stessa storia. `TestoAvvisoDiFusione` scrive gli avvisi dell'incantatore.

### I salvataggi su file

Le implementazioni su file delle interfacce del contratto: `GestoreSalvataggiSuFile` (quattro slot a mano più il salvataggio automatico, un file `<slot>.TXT` per slot, scritto su un file temporaneo che prende il posto del vecchio solo se completo), `GestorePunteggiSuFile` (la classifica dei migliori dieci, `forestaHS`; la `Classifica` con le sue regole è in `01-foresta-punteggi`, qui solo quando leggerla e scriverla), `GestoreTrofeiSuFile` e `GestoreBestiarioSuFile`. La base `GestoreSuFile` scrive nella cartella `.foresta` dell'utente, o in quella che indica la piattaforma (su Android e iOS è uno spazio privato dell'app), con `java.io.File` e non con `java.nio.file`. `SalvataggioInMemoria` è la rete di sicurezza: l'ultimo stato a fine locazione, tenuto solo in memoria, come ultima riga dell'elenco da cui si carica.

### I testi e gli intermezzi del motore

`PrecaricatoreDeiTesti` carica all'avvio le grammatiche di tutti i produttori e, se una manca, segnala l'errore sul bus e chiede l'uscita con `NotificaUscitaDalGioco`. `NotizieDelleLocande` sceglie le notizie del locandiere adatte al gruppo. `IntermezziDelMotore` registra nella fabbrica gli epiloghi delle missioni. `CostantiMotore` ha il bilanciamento di locazioni, avversari e locande.

## Osservazioni

- `Automa` e `LocazioneBase` sono le due classi più grandi del progetto e hanno parecchio da dire: il piano è di tagliare il combattimento a round di `LocazioneBase` quando ci si interviene (vedi `assessment.md`).
- Lo stato è in gran parte statico (gruppi, registri, mappa): è lecito per un gioco a giocatore singolo, a patto di non aggiungere altri registri statici.
- Il motore e i moduli sotto non chiudono mai il processo e non usano `java.awt`/Swing: lo controllano `ConvenzioniDiPortabilitaTest` e `DipendenzeDalToolkitGraficoTest`.
- Il quadro completo è in [`motore_di_gioco.md`](motore_di_gioco.md) (§2–3 la macchina a stati e il ciclo di una locazione, §5 salvataggi, §6 mondo).
