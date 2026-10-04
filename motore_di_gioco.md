# Motore di gioco

Descrizione dello stato attuale della logica di gioco: tutto ciò che sta sotto `com.threeamigos.foresta` tranne la parte grafica (`ui`), per la quale vedi [`motore_grafico.md`](motore_grafico.md). Per una visione d'insieme vedi [`foresta.md`](foresta.md).

Documenti di dettaglio su singoli sottosistemi:

| Argomento | Documento |
| :--- | :--- |
| Missioni a passi: infrastruttura, registro, claim delle locazioni | [`gestione_missioni.md`](gestione_missioni.md) |
| Catalogo dei passi e mappatura dei tipi di missione | [`passi_missioni.md`](passi_missioni.md) |
| Artefatti, pergamene, incantatore, bilanciamento del combattimento | [`artefatti_e_incantamenti.md`](artefatti_e_incantamenti.md) |
| Economia (entrate, uscite, prezzi per livello) | [`economia.md`](economia.md) |
| Interazioni fra effetti di stato e tipi di danno | [`interazioni_effetti_di_stato.md`](interazioni_effetti_di_stato.md) |
| Scrivere, animare e provare un intermezzo | [`intermezzi.md`](intermezzi.md) |
| Generazione procedurale di testo | `GrammarBean.md` |

## 1. Mappa dei pacchetti

| Pacchetto | Contenuto |
| :--- | :--- |
| `motore` | `Automa` (la macchina a stati), `Stato`, `Comando`, i gruppi, `Foresta`, `LineaTemporale`, i calcolatori (combattimento, riposo, progressione), i registri, le regole di equipaggiamento e di negoziazione, `Dado`, `GrammarBean` e `ProduttoreDiTestiCasuale` |
| `motore.modellodati` | I bean serializzabili (suffisso `MD`) e `ModelloDati`, il contenitore radice |
| `motore.tipi` | Enum di dominio: attributi, tipi di danno, effetti di stato, slot e rarità degli artefatti, tipi di riposo, di negozio, di trofeo |
| `eventi` | Il bus (`BusEventi`) e le quattro famiglie di eventi: `comandigiocatore`, `notifiche`, `richieste`, `interni` |
| `personaggi` | `Personaggio` (contratto), `PersonaggioBase`, le classi giocabili e i mostri, `ClassePersonaggio`, `EquipaggiamentoIniziale` |
| `locazioni` | `Locazione`, `LocazioneBase`, le locazioni comuni, le città, i castelli, `Locanda`, `ClassiLocazione` |
| `missioni` | `Missione`, `MissioneAPassi`, `Passo`, le missioni concrete (vedi i due documenti sulle missioni) |
| `intermezzi` | Le scene a pagine (vedi §10) |
| `oggetti` | Artefatti, oggetti raccoglibili, generatore di artefatti, listino delle pergamene |
| `offerte` | I servizi che un incontro amichevole o una locanda propongono |
| `incantesimi` | Le dieci formule e il loro catalogo |
| `trofei` | Gli obiettivi che valgono da una partita all'altra |
| `interfacce` | `ControlloreDiGioco`, `Arma`, `FornitoreMissione` e le interfacce di oggetto |
| `tools` | Salvataggi, classifica e trofei su file, temporizzatore, `Misc`, `ModalitaDiProva`, `CostruttoreArtefatto` |

Il motore parla alla UI solo tramite il bus eventi (§4). Le sole dipendenze del motore verso `ui` sono l'enum `InterfacciaUtente.Finestra`, usato da `InternoPortaInPrimoPiano` per dire quale riquadro portare in primo piano, e i due punti in cui usa Swing per il thread (`BusEventi` e `TemporizzatoreJ2SE`, §2 e §4). Nel verso opposto la separazione non c'è: la UI legge direttamente lo stato di dominio (`GruppoGiocatore`, `Foresta`, `Notizie`, `RegistroMissioni`...) e chiama gli `Automa*` dei negozi che le arrivano con gli eventi (vedi §13 e [`todo.md`](todo.md)).

## 2. Avvio e ciclo principale

`Main` registra, nell'ordine, i componenti che ascoltano il bus (`SnifferBusEventi`, `Notizie`, `Statistiche`, `RegistroTrofei`, `RegistroArtefatti`, `RegistroMissioni`), legge gli argomenti, installa i gestori su file di classifica, salvataggi e trofei, crea l'`Automa` con un `TemporizzatoreJ2SE` e si iscrive a `InternoInterfacciaUtentePronta`; solo allora crea `ForestaUI`. Quando la UI è pronta l'evento arriva e l'automa parte con `inizia()`.

### La macchina a stati

`Automa` è l'unica implementazione di `ControlloreDiGioco` (`inizia()`, `processaComando(Comando)`). Non ha un game loop: è una **macchina a stati finiti reattiva**, che avanza solo in risposta a due stimoli:

1. **Comandi del giocatore**: la UI pubblica `ComandoDiGioco` e l'automa lo riceve in `processaComando`. Il testo libero (nome del personaggio, nome per la classifica, nome di un artefatto) arriva con un altro evento, `ComandoInvioTesto`, e segue lo stesso ciclo.
2. **Impulsi di un timer**: `Temporizzatore.inizia(ms)` fa scattare il primo impulso subito, `iniziaDopo(ms)` dopo un periodo intero, `termina()` ferma. Ogni impulso arriva all'automa come `Comando.TIMER`. Il combattimento usa `inizia(1000)` (un round al secondo, primo subito); gli intermezzi, l'intro e le sequenze di sconfitta e vittoria usano `iniziaDopo`, perché la prima schermata non sparisca all'istante.

`TemporizzatoreJ2SE` pianifica gli impulsi su un thread daemon, ma ognuno viene eseguito sull'Event Dispatch Thread, e ricorda la pianificazione da cui viene: se nel frattempo è cambiata (un `inizia`, `iniziaDopo` o `termina`), l'impulso già accodato non fa nulla. Così nessun `TIMER` raggiunge l'automa dopo un cambio di stato. **Tutta la logica di gioco gira sull'EDT**; solo il timing è schedulato fuori.

### Il ciclo di avanzamento

Gli stati sono mappati su due `EnumMap`:

- `gestoriIngresso` (`Supplier<Esito>`): cosa fare entrando in uno stato senza un comando reale;
- `gestoriComando` (`Function<Comando, Esito>`): come reagire a un comando reale (o inoltrato come tale).

Ogni gestore imposta il campo `stato`, pubblica sul bus le conseguenze e restituisce un `Esito` (classe annidata privata):

- `Esito.FERMATI`: si aspetta un comando reale;
- `Esito.CONTINUA_CON_INGRESSO`: si esegue subito la logica di ingresso del nuovo stato;
- `Esito.continuaCon(comando)`: si inoltra un comando al nuovo stato come se fosse appena arrivato (per esempio la scelta automatica di un personaggio).

`processaComando` accetta solo comandi reali (`null` solleva `IllegalArgumentException`: dentro il ciclo indica "ingresso nello stato") ed esegue il primo passo; `prosegui(Esito)` è l'unico ciclo che concatena le transizioni automatiche, con una rete di sicurezza: oltre `MAX_TRANSIZIONI_AUTOMATICHE` (100) passi consecutivi lancia `IllegalStateException`, perché indicano un ciclo fra stati. Un comando non previsto in uno stato pubblica un `InternoErrore`; uno stato senza alcun gestore lancia `IllegalStateException`.

Non esiste una funzione di transizione centralizzata: le regole su "da quale stato a quale stato" stanno nei singoli metodi `entraInStatoX`/`gestisciComandoInStatoX`.

### Gli stati (`Stato`)

- **Avvio**: `LOGO_INIZIALE` (la UI traccia il logo mentre il motore precarica `ProduttoreDiTestiCasuale` e `GeneratoreArtefatti` su un thread a parte; passa a `INTRO` quando ha ricevuto sia `InternoFineLogoIniziale` sia `InternoPrecaricamentoMotoreCompletato`; i comandi si ignorano e non ci si torna più).
- **Pre-partita**: `INTRO`, `PRE_GAME_SELEZIONE_SALVATAGGIO_DA_LEGGERE`, `FILE_DI_SALVATAGGIO_NON_VALIDO`, `PRE_GAME_ATTESA_NOME_PERSONAGGIO`, `PRE_GAME_ATTESA_SESSO_PERSONAGGIO`, `PRE_GAME_ATTESA_CLASSE_PERSONAGGIO`, `INIZIO_GIOCO`. L'ultimo mostra gli intermezzi di apertura e sta prima del ciclo perché da `INIZIO_LOCAZIONE` partono i controlli delle missioni, le cui notifiche altrimenti comparirebbero durante l'intermezzo.
- **Ciclo di locazione**: `INIZIO_LOCAZIONE`, `INTERMEZZO`, `ATTESA_UI_PER_INTERMEZZO`, `PREPARAZIONE_LOCAZIONE`, `IN_LOCAZIONE`, `INGRESSO_NEGOZIO`, `IN_COMBATTIMENTO`, `FINE_LOCAZIONE`, `FINE_LOCAZIONE_2`, `ATTESA_RISPOSTA_MISSIONE`. Il flusso è nel §3.
- **Movimento**: `ATTESA_DIREZIONE`, `SCELTA_DIREZIONE`, `SCELTA_PASSI`, `ACCAMPAMENTO`.
- **Scelte di personaggio e oggetto**: `SCELTA_AUTOMATICA_PERSONAGGIO`, `SCELTA_PERSONAGGIO_QUALSIASI`, `SCELTA_MANUALE_PERSONAGGIO`, `SCELTA_DESTINATARIO_OGGETTO`.
- **Incantesimi, pozioni, resurrezione**: `SCELTA_INCANTESIMO_DA_LANCIARE`, `ATTESA_INCANTESIMO_QUALSIASI`, `INCANTESIMO_SCELTO`, `ATTESA_POZIONE_SALUTE`, `ATTESA_POZIONE_SALUTE_GRANDE`, `ATTESA_POZIONE_MAGIA`, `ATTESA_POZIONE_MAGIA_GRANDE`, `SCELTA_FORMULANTE_RESURREZIONE`, `SCELTA_BERSAGLIO_RESURREZIONE`, `ESECUZIONE_RESURREZIONE`. Ogni scelta con un solo candidato viene saltata; `ANNULLA` riporta ad `ATTESA_DIREZIONE` (o alla locazione).
- **Conferme**: `ATTESA_SI_NO`, `CONFERMA_USCITA`.
- **Schermate**: `INVENTARIO`, `MAPPA`, `TROFEI`, `SELEZIONE_SALVATAGGIO_DA_SCRIVERE`.
- **Fine partita**: `GIOCO_PERSO`/`GIOCO_PERSO_2`, `GIOCO_VINTO`/`GIOCO_VINTO_2`, `STATISTICHE`, `ATTESA_NOME_PUNTEGGI`, `PUNTEGGI`. Dalle statistiche, se il punteggio entra in classifica si chiede il nome, lo si registra e si passa per `PUNTEGGI`, che torna all'intro; altrimenti `STATISTICHE` chiama direttamente `inizia()`. La sequenza dell'intro (loghi, pagine della storia, classifica) vive nella UI.

I comandi che il giocatore può dare (`Comando`, un enum) comprendono scelte di sesso e classe, azioni di locazione (`COMBATTIMENTO`, `INCANTESIMO`, `CORRUZIONE`, `AMICIZIA`, `FUGA`, `PASSA_INOSSERVATO`, `SINGOLO_ATTACCO`), i personaggi (`PERSONAGGIO_1`..`PERSONAGGIO_8`), gli incantesimi, le direzioni, le pozioni, i negozi di città, gli slot di salvataggio e di risposta (`NUMERO_1`..`NUMERO_5`), le frecce di scorrimento e `TIMER`. Ogni stato espone solo un sottoinsieme di comandi (`InternoStatoDiGioco`, `RichiestaSelezione*`, `InternoAggiornamentoComandiDisponibili`), che la UI traduce in icone.

## 3. Il ciclo di una locazione

```
INIZIO_LOCAZIONE ─ controllo missioni PRE_LOCAZIONE ─ evento della linea temporale ─ intermezzi INIZIO_LOCAZIONE
   └─> PREPARAZIONE_LOCAZIONE ─ costruzione, incontri e oggetti, descrizione ─ controllo missioni IN_LOCAZIONE
         └─> IN_LOCAZIONE ⇄ IN_COMBATTIMENTO / schermate / negozi
               └─> FINE_LOCAZIONE ─ raccolta oggetto ─ controllo POST_LOCAZIONE ─ azzeramento ─ intermezzi LOCAZIONE_COMPLETATA
                     └─> FINE_LOCAZIONE_2 ─ fine tempo? personaggi a tempo, stanchezza, turni
                           └─> ATTESA_DIREZIONE → SCELTA_DIREZIONE → SCELTA_PASSI → (movimento, ore, eventi) → INIZIO_LOCAZIONE
```

- **Inizio.** `INIZIO_LOCAZIONE` esegue il controllo delle missioni `PRE_LOCAZIONE`, poi `LineaTemporale.getEvento()` (che consuma l'ultimo evento non ancora mostrato; se il gioco è finito si va a `GIOCO_PERSO`), poi gli intermezzi del momento `INIZIO_LOCAZIONE`.
- **Preparazione.** `PREPARAZIONE_LOCAZIONE` azzera il gruppo avversario e la panchina, toglie gli effetti di stato, costruisce la `Locazione` della casella e la fa popolare (`crea`). Se una missione la considera sicura (`RegistroMissioni.isDaSopprimere`) non c'è nessun avversario né oggetto; altrimenti la missione può sostituire gli avversari (anche con **ondate** successive) e collocare un oggetto di missione. Poi descrive il luogo, aggiunge l'eventuale "ricordo" di una missione conclusa lì, esegue il controllo `IN_LOCAZIONE` e chiama la prima volta `impostaAzioni(..., null)`.
- **Dentro la locazione.** `impostaAzioni(gruppo, avversari, comando)` è l'automa interno di ogni locazione: restituisce `IN_LOCAZIONE` (si aspetta il giocatore), `IN_COMBATTIMENTO` (parte il timer) o un altro stato (`FINE_LOCAZIONE`, `GIOCO_PERSO`...), che l'automa interpreta in `esitoDaStatoLocazione`. Il `null` come comando significa "fai avanzare di un passo" e in `LocazioneBase` fa trascorrere un turno di effetti di stato: per ripresentare solo i comandi (tornando da mappa o inventario) si usa `ripresentaComandi()`, che non fa avanzare nulla.
- **Negozi di città.** Entrando in locanda, alchimista, armaiolo, venditore di pergamene o incantatore (`INGRESSO_NEGOZIO`) l'automa lascia prima scattare l'intermezzo di ingresso (momento `INGRESSO_*`), poi esegue il comando. Armaiolo e simili chiedono alla UI la loro finestra specifica; la locanda mostra il dialogo nella schermata di gioco normale.
- **Fine.** `FINE_LOCAZIONE` ferma il timer, rimette in campo chi era in panchina e, se la locazione è completa e non si è stretta amicizia, prova a far raccogliere l'oggetto (altrimenti passa da `SCELTA_DESTINATARIO_OGGETTO`, che propone i personaggi vivi e `GRUPPO`, e se il personaggio è uno solo lo sceglie da sé). Poi controlla le missioni `POST_LOCAZIONE` (prima di azzerare la locazione, perché un castello sostituito dalle sue rovine deve poter completare la missione), azzera la locazione e lascia scattare gli intermezzi `LOCAZIONE_COMPLETATA`. Una locazione lasciata a metà (fuga, duello perso, passaggio inosservato) fa comunque il controllo delle missioni. `FINE_LOCAZIONE_2` verifica la fine del tempo (vittoria se la missione principale è completa, altrimenti sconfitta), fa scadere i personaggi a tempo, aumenta di 1 la stanchezza e conta il turno.
- **Domande di missione.** Ogni controllo delle missioni passa da `controllaMissioniEDomande`: se una missione a passi ha una domanda per il giocatore, l'automa va in `ATTESA_RISPOSTA_MISSIONE` (conferma `SI`/`NO` o scelta `NUMERO_1..5`), consegna la risposta e rifà il controllo; solo allora prosegue con "il resto del lavoro". Non si torna allo stato di prima, per non rifare cose già fatte. I controlli visitano l'albero delle missioni non completate: `PADRE_PRIMA` per `PRE_LOCAZIONE`, `IN_LOCAZIONE` e `ACCAMPAMENTO`, `FIGLI_PRIMA` per `POST_LOCAZIONE` (così un padre vede le figlie già nello stato di questo giro).
- **Direzione e passi.** `ATTESA_DIREZIONE` pubblica la domanda e i comandi disponibili e passa subito a `SCELTA_DIREZIONE`. Da qui, oltre alle quattro direzioni, si può aprire mappa e inventario, bere pozioni (via `SCELTA_AUTOMATICA_PERSONAGGIO`), lanciare la resurrezione (tre stati: formulante, bersaglio, esecuzione), salvare, chiedere aiuto o **accamparsi**. L'icona dell'accampamento è offerta solo se possibile; l'accampamento incrementa il contatore in `IntermezziMD`, fa il controllo missioni `ACCAMPAMENTO`, lascia scattare gli intermezzi `ACCAMPAMENTO` e solo dopo `ACCAMPAMENTO` fa pernottare il gruppo (`pernotta` con il `TipoRiposo` della casella), avanza al mattino e rifà gli eventi. Scegliere la direzione memorizza solo il campo `direzione`; `SCELTA_PASSI` offre `NUMERO_1..n` (fino a `Comando.MAX_MOVIMENTO` = 5, limitato dalla mappa) più `ANNULLA`, che torna indietro senza nulla da disfare. Muoversi di `n` passi costa `n` ore (`LineaTemporale.aggiungiOre`) e fa scattare gli eventi del tempo.

## 4. Il bus eventi

`BusEventi` è un publish/subscribe **sincrono per classe esatta** (`evento.getClass()`: iscriversi a una superclasse non intercetta le sottoclassi), con `iscriviti`, `cancellati`, `pubblica`.

- **Consegna sull'EDT.** `pubblica` consegna subito se già sull'EDT, altrimenti accoda con `invokeLater`. La consegna è sostituibile con `impostaConsegna` (i test usano `Runnable::run` per riceverla subito sul loro thread) e `azzera()` toglie tutti gli iscritti.
- **Isolamento degli errori.** Ogni iscritto è chiamato in un `try`: se uno solleva un'eccezione gli altri ricevono comunque l'evento, l'errore va nel `Logger` e sul bus come `InternoException`, a meno che a fallire sia proprio chi ascolta quelle. Chi ha pubblicato non si trova l'eccezione a metà di una transizione.
- Sottoscrittori in `CopyOnWriteArrayList`; nessuna garanzia di ordine fra iscritti allo stesso evento.

Gli eventi sono organizzati per **direzione e intento**:

| Pacchetto | Direzione | Esempi |
| :--- | :--- | :--- |
| `eventi.comandigiocatore` | UI → motore | `ComandoDiGioco`, `ComandoInvioTesto`, `ComandoAcquistoArtefatto`, `ComandoVenditaArtefatto`, `ComandoSpostamentoArtefatto`, `ComandoIncantatura`, `ComandoAperturaTrofei`, `ComandoVisualizzazioneMappa` |
| `eventi.notifiche` | motore → UI, fatto compiuto | `NotificaTestoParagrafo`/`NotificaTestoFrase`, `NotificaNotizia`, `NotificaPaginaIntermezzo`, `NotificaFineGioco`, le coppie `Notifica*Approvazione*`/`Notifica*Rifiuto*` |
| `eventi.richieste` | motore → UI, richieste di input | `RichiestaSelezioneDirezione`, `RichiestaSelezioneSiNo`, `RichiestaSelezioneMissione`, `RichiestaTesto` |
| `eventi.interni` | tecnici, non rivolti al giocatore | `InternoStatoDiGioco`, `InternoAggiornamentoComandiDisponibili`, `InternoCaricamentoCompletato`, `InternoUiOccupata`/`InternoUiInattiva`, `InternoErrore`, `InternoException`, gli eventi di sprite |

Classi base: `EventoBase` (identificatore e `TipoEvento`), `EventoSuPersonaggio`, `RichiestaConComandi`. `SnifferBusEventi` si iscrive a tutto per il log.

Le operazioni economiche usano un pattern **richiesta/verdetto**: il comando della UI non sposta nulla, il motore verifica e pubblica un'approvazione o un rifiuto con il motivo (fondi insufficienti, peso, slot, regole di equipaggiamento), e solo dopo l'approvazione l'oggetto si sposta.

Un caso insolito: `motore.Notizie` ascolta le notifiche di testo e di notizia per tenere uno storico in `NotizieMD`, invece di essere chiamato da chi le pubblica (§9).

## 5. Modello dati e persistenza

Le classi in `motore.modellodati` (suffisso `MD`) sono bean serializzabili senza logica di gioco; le classi in `motore` (`Foresta`, `Gruppo*`, `Registro*`, `LineaTemporale`, `Statistiche`, `Notizie`...) sono facciate statiche che operano su di esse. `ModelloDati` è il contenitore radice, un **singleton statico** sostituibile (`setIstanza`):

`GruppoGiocatoreMD`, `StatisticheMD`, `LineaTemporaleMD`, `ForestaMD`, `RegistroPersonaggiMD`, `RegistroArtefattiMD`, `RegistroMissioniMD`, `NotizieMD`, `IntermezziMD`.

Altri `*MD`: `PersonaggioMD`, `ArtefattoMD`, `LocazioneMD` (con le proprietà della casella, `MappaProprieta`), `MissioneMD`, `GruppoMD`, `CoordinateMD`, `MessaggioMD`, `Notizia`, `ModificatoreAttributo`. `TrofeiMD` sta fuori da `ModelloDati` (vedi §11).

### Formato dei file

Tutto implementa `Serializzabile` (`salva(PrintWriter)`, `leggi(BufferedReader)`): un **formato testuale proprietario**, una riga per record, con `|` come separatore (`Serializzabile.PIPE`) e `LettoreCampi` per leggere i campi. `ModelloDati.salva`/`leggi` delegano in cascata nell'ordine dell'elenco sopra; in lettura prima reimposta i componenti e poi li rilegge. Non c'è numero di versione né migrazione.

- **Salvataggi** (`GestoreSalvataggiSuFile`): cinque slot (`NUMERO_1`..`NUMERO_5`), un file `<slot>.TXT` nella cartella `~/.foresta/`; la prima riga è una testata leggibile (`TestataSalvataggio`) usata per elencare gli slot senza leggere tutto. La scrittura passa da un file temporaneo e da uno spostamento atomico. Errori e file corrotti arrivano alla UI come `NotificaErroreCaricamento`/`InternoException`, mai come eccezioni al chiamante.
- **Ricostruzione dopo la lettura**: `GestoreSalvataggi.ricostruisciModelloDati()` è il punto unico che ricalcola lo stato derivato (attributi secondari dei personaggi, collegamento del gruppo al nuovo modello, locazione corrente, elenchi delle missioni). Va eseguito dopo l'installazione del nuovo `ModelloDati`, non prima.
- **Classifica** (`GestorePunteggiSuFile`): file `forestaHS`, una riga `nome#punteggio`.
- Nessuna cifratura. Il banco dell'incantatore e gli oggetti di missione non si salvano.
- **Messaggi e notizie** (`NotizieMD`): gli ultimi messaggi (`P|testo` per un paragrafo, `F|testo` per una continuazione; il testo può contenere `|`) e le ultime notizie (`id|corpo`). Dopo un caricamento `Automa` pubblica `InternoCaricamentoCompletato` con i messaggi, e la UI ripopola il pannello con la stessa impaginazione. Due fragilità: un a-capo reale in un messaggio ne spezzerebbe la lettura (oggi nei testi gli a-capo sono scritti come `\n` letterale) e il corpo di una notizia è letto con un tokenizer su `|`, che ne tronca il testo.

I salvataggi scritti con una versione precedente del formato non sono leggibili e la cosa non interessa (non c'è alcuna retrocompatibilità da mantenere).

## 6. Il mondo

### La Foresta

`Foresta` (statica) è una griglia **20×20** (`DIMENSIONE_X`/`DIMENSIONE_Y`), indicizzata da `CoordinateMD`; la costante `MAX_DIMENSIONE_LATO_FORESTA` (80) serve solo a codificare le coordinate. Ogni casella ha una `ClassiLocazione` e un `LocazioneMD` con le sue proprietà (nome, visitata, conosciuta...). Le classi sono di tre tipi (`TipoLocazione`): `STANDARD` (radura, bosco, palude, locanda, rovine, tempio, grotta), `CITTA` (Nyena, Malgaard, Ruuna, Fleena), `CASTELLO` (Idra, Minotauro, Lich, Strega, Drago) e `MISSIONE_SECONDARIA` (la grotta del Medaglione, il covo dei Troll).

`Foresta.reimposta()` costruisce un mondo nuovo: azzera i registri e le produzioni one-shot delle grammatiche, posiziona le città (una per quadrante, in ordine casuale) con i personaggi reclutabili, poi le locande (con i personaggi e i dati della grammatica), i templi (che custodiscono gli artefatti del registro), grotte, paludi, rovine e radure; il resto è bosco. I castelli degli alleati del Drago non si costruiscono qui: li rivendica ciascuna missione a inizio partita (`RegistroMissioni.rivendicaPerLocazioneUnica`), uno per quadrante senza castello (`Quadrante`); quello del Drago, che arriva dopo, va dovunque. Le missioni possono anche segnare caselle come conosciute, e `Foresta.aggiornaMappaCircostante` rivela quelle intorno al gruppo.

**Nomi.** Le locazioni con un nome lo tengono nella proprietà `NOME` della casella (e perdono il nome se la casella cambia tipo):
- città e castelli: nome fisso in `ClassiLocazione.getNomeProprio()`;
- locande: da `locande.txt` (quella di una città tiene il suo in una proprietà a parte, perché il nome della casella è quello della città);
- rovine: dalla grammatica `rovine.txt`, oppure derivato da quello che c'era ("le Rovine del Maniero del Malefizio");
- templi: dalla grammatica `templi.txt`, diverso da quelli già esistenti se possibile.

Sulla mappa a tutto schermo il nome compare accanto al puntatore su una casella conosciuta (`Foresta.getNomeDaMostrare`). Una casella diventa conosciuta quando il gruppo la vede, ne sente parlare (`offerte.Informazioni`) o una missione la segna.

### Il tempo (`LineaTemporale`)

Ora del giorno (con descrizioni testuali) e contatore di giorni; muoversi e riposare fanno avanzare il tempo. Il motore applica la pressione del Drago: a **20, 25, 30 e 35 giorni** distrugge in ordine Ruuna, Nyena, Fleena e Malgaard (al loro posto restano rovine; le missioni che dovevano concludersi lì falliscono al turno dopo), e **oltre il giorno 40** il gioco finisce in sconfitta. L'ultimo evento non ancora mostrato sta nel modello dati, così un salvataggio non lo perde.

### Locazioni

`Locazione` è l'interfaccia di un luogo: `crea` (popola con avversari e oggetto), `descrivi`, `impostaAzioni` (l'automa interno), `ripresentaComandi`, `riceviTesto` (per le locazioni che chiedono un testo), `isCompleta`, `getModelloDati`. `LocazioneBase` realizza il comportamento comune (combattimento, incantesimi, corruzione, amicizia, fuga, passare inosservati, duelli) ed è estesa da `Bosco`, `Grotta`, `Palude`, `Radura`, `Rovine`, `Tempio`; `LocazioneUnica` copre città e castelli. `Citta` è un automa a stati proprio (piazza, locanda, alchimista, armaiolo, venditore di pergamene, incantatore, nome dell'artefatto da fondere); `Locanda` offre pasto, pernottamento, informazioni e l'incontro con eventuali compagni da reclutare, e genera la notizia all'uscita (§11).

## 7. Combattimento

Il combattimento si svolge dentro `LocazioneBase` (`gestisciCombattimento`, `eseguiSingoloAttacco`), con i calcoli in `CalcolatoreCombattimento`. Non c'è un tiro "d20 contro classe armatura": le probabilità derivano dagli attributi.

**Turno.** Il combattente scelto dal giocatore (`SCELTA_AUTOMATICA_PERSONAGGIO`, `PERSONAGGIO_n`) attacca il bersaglio vivo del gruppo avversario a ogni `TIMER` (o `COMBATTIMENTO`): prima il suo attacco, poi la risposta dell'avversario sul combattente. Chi combatte con due armi (Ladro, Elfo) ha una seconda fase con l'arma secondaria, a una quota del danno (`DOPPIA_ARMA_FATTORE_SECONDA_ARMA`, 0,4). Se gli avversari vivi sono più del gruppo, uno "si disimpegna e attacca" il gruppo. A fine round passa un turno di effetti di stato (danni nel tempo e riduzione delle durate); se un effetto uccide l'ultimo avversario si va a `FINE_LOCAZIONE`, se uccide il capo a `GIOCO_PERSO`. Bere una pozione, lanciare un incantesimo o fuggire interrompe la mischia (chiude la finestra di combattimento); con un solo personaggio vivo la pozione viene bevuta senza scelta e il combattimento continua. `Gruppo.getProssimoAttaccante()` fa un round-robin che salta i personaggi morti o in panchina.

**Situazioni particolari.**
- **Panchina**: in un combattimento fino alla resa chi si arrende finisce in panchina (vivo ma fuori dal combattimento) e rientra alla locazione successiva.
- **Duello** e **combattimento fino alla resa**: se tutto il gruppo è in panchina il duello è perso; il vincitore resta ad aspettare la rivincita e la locazione non è completa.
- **Ondate** (`Ondata`, `GruppoAvversario.setOndateSuccessive`): quando gli avversari sono sconfitti arriva la successiva e la locazione non è più completa.
- **Ospiti** (`GruppoGiocatore.getOspiti`, `Viandante`): personaggi che viaggiano col gruppo perché una missione li scorta; possono essere vulnerabili e bersaglio degli avversari.
- **Passare inosservati**: `probabilitaDiPassareInosservati` confronta le furtività dei due gruppi con l'ora; dopo una vera azione non si può più.
- **Dardo arcano**: l'incantesimo innato di Mago ed Elfo, su un solo bersaglio, senza pergamene.

**Probabilità di colpire.** `calcolaProbabilitaDiColpire`:
1. un attaccante `STORDITO` fallisce sempre; un difensore `ATTERRATO`, `CONGELATO` o `STORDITO` viene sempre colpito;
2. fisico: attacco = Precisione + Destrezza, difesa = Velocità + Destrezza + 50% Parata; magico o elementale: attacco = Precisione + Intelligenza, difesa = Resistenza Magica + Saggezza;
3. modificatori sull'attaccante (`CONFUSO`, `ACCECATO`, più la Stanchezza, 2 punti per livello) e sul difensore (`RALLENTATO`, `SPAVENTATO`, Stanchezza);
4. probabilità = 75 + 2 × (attacco − difesa), limitata a 5–95, confrontata con `Dado.tira(100)`.

**Danno.** `calcolaDannoRisultante(attaccante, difensore, arma, fattore)`:
- danno offensivo = (danno dell'arma × livello dell'arma + parte dell'eroe) × fattore di fase × moltiplicatore di classe; la parte dell'eroe è la statistica pertinente (Forza per il fisico; Intelligenza per il magico e l'elementale, con metà della Saggezza per il sacro) × livello dell'attaccante ÷ 5, scalata dal rapporto fra livello dell'arma e livello dell'attaccante (mai oltre 1: un'arma di basso livello resta poco efficace su un campione);
- il danno degli incantesimi è aumentato dal potere magico (bastoni, libri magici) e dal libro magico;
- `BERSERK` (guerrieri con furia) aumenta il danno fisico fino al 50% della salute persa;
- mitigazione a rendimenti decrescenti: danno × `100 / (100 + difesa)`, dove la difesa (`difesaContro`) include costituzione, parata e le resistenze di elmo, scudo, armatura e schinieri contro quel tipo di danno;
- le **interazioni con gli effetti di stato** (`TipoInterazioneConEffettiDiStato`, per esempio `BAGNATO` + fulmine, `CONGELATO` + contundente) modificano il danno o producono nuovi effetti; il quadro completo è in [`interazioni_effetti_di_stato.md`](interazioni_effetti_di_stato.md);
- le componenti elementali degli incantamenti dell'arma si sommano al danno;
- critico: probabilità = 5% + Critico dell'attaccante − Fortuna del difensore (almeno 0), raddoppia il danno;
- infine ogni colpo può far scattare effetti di stato (dall'arma e da ciascun incantamento, ciascuno con la sua probabilità), con durata e danno periodico che dipendono dalle statistiche delle due parti (`calcolaDurataStato`, `calcolaDannoPeriodico`).

Il risultato è un `DannoRisultante`, applicato con `Personaggio.applicaRisultatoCombattimento`. In modalità di prova l'`OmbraFiamma` colpisce sempre.

**Riposo.** `CalcolatoreRiposo.calcolaRiposo(personaggio, ore, tipoRiposo)`: il recupero di salute, magia e stanchezza scala con `√ore` e con il moltiplicatore del `TipoRiposo` (all'aperto senza fuoco 0,5, con fuoco 1,0, al coperto 1,5); i personaggi non morti o spettrali hanno moltiplicatori di recupero pari a 0 e saltano quelle componenti.

## 8. Personaggi, gruppi, progressione

### Personaggi

`Personaggio` (contratto, ricco: identità e nomi con articoli, attributi, risorse, effetti di stato, equipaggiamento, flag di situazione) è realizzato da `PersonaggioBase`, estesa da una classe concreta per ogni `ClassePersonaggio`:
- **giocabili**: Guerriero/Guerriera, Ladro/Ladra, Bardo/Cantastorie, Elfo/Elfa, Mago/Maga;
- **mostri e non giocanti**: Arpia, Centauro, Chimera, ChimeraDrago, Drago, Eremita, Fantasma, Folletto, Gargoyle, Gigante, Goblin, Hobgoblin, Idra, Lich, Minotauro, MinotauroGigante, OmbraNera, Scheletro, Spettro, Spirito, Strega, Titano, Troll, Viverna;
- speciali: `OmbraFiamma` (la prova) e `Viandante` (chi una missione scorta).

**Attributi** (`TipoAttributo`, con `SupertipoAttributo`): sette **primari** (Forza, Destrezza, Costituzione, Intelligenza, Saggezza, Carisma, Fortuna), numerosi **secondari** derivati (Precisione, Parata, Velocità, Furtività, Critico, Resistenza Magica, Percezione, Soggezione, Furia, Coraggio, Valore, Contrattazione, Carico massimo, Potere magico, rigenerazioni, Numero bersagli) e **risorse dinamiche** (Livello, Esperienza, Punti abilità, Salute e Magia con i massimi, Stanchezza con tetto 9, Tempo, Ospite di locazione). I secondari derivano dai primari con i moltiplicatori della classe (`Costanti.<CLASSE>_MOLTIPLICATORE_*`, ciascuno con la sua nota); le costanti `*_MAX_*` fissano i tetti dei primari. `LanciatoreDeiDadi` ripartisce a livello 1 un budget di punti secondo le percentuali di ogni classe. Dopo un caricamento gli attributi secondari si ricalcolano (`PersonaggioBase.ricalcolaAttributiSecondari`). I **modificatori** (`ModificatoreAttributo`, `TipoModificatore`: fisso, percentuale, quantità assoluta) provengono da equipaggiamento e incantamenti. Ogni livello dà punti abilità da spendere sugli attributi (`spendiPuntoAbilita`).

**Effetti di stato** (`TipoEffettoDiStato`): rallentato, immobilizzato, stordito, atterrato, spaventato, sanguinamento, bruciato, avvelenato, infettato, confuso, mente fratturata, accecato, assordato, silenziato, maledetto, bagnato, congelato, berserk. Aggiunta, variazione e rimozione sono notificate alla UI.

### Gruppi

`Gruppo` (astratta) ha due singleton: `GruppoGiocatore` e `GruppoAvversario`. Il gruppo del giocatore ha al massimo **5 permanenti** (`MAX_PERSONAGGI_GRUPPO_GIOCATORE`) e fino a **8 in tutto** (`MAX_PERSONAGGI_GRUPPO_TOTALE`) con i personaggi a tempo (aiuti gratuiti o mercenari, `isATempo`) e gli ospiti di locazione. Oltre ai personaggi tiene le risorse comuni: monete, preziosi, incantesimi per classe, quattro tipi di pozione, coordinate e locazione corrente, con le regole di movimento (`getMaxPassi*`). La contrattazione del gruppo determina i prezzi (`RegoleContrattazione`: sconto sugli acquisti fino al 20%, ricavo delle vendite fra il 50% e il 75%, così che comprare e rivendere non faccia mai guadagnare).

I compagni da reclutare nascono con il mondo (`RegistroPersonaggi`: 5 guerrieri, 4 ladri, 2 bardi, 2 elfi, 2 maghi, tutti di livello 1) e sono sparsi in città e locande; chi entra nel gruppo (`preparaCompagno`) arriva al livello del mondo meno un numero a caso fra 0 e 2, con l'equipaggiamento di base della classe (`EquipaggiamentoIniziale`).

### Progressione

`GestoreProgressione`: curva **quadratica**, `xpNecessari(livello) = 100 × (livello − 1)²`, con tetto al livello 50 e formula inversa per ricavare il livello dai punti. Le fonti sono percentuali dei punti che separano dal livello successivo: artefatto minore 5%, missione secondaria 20%, missione principale 50%. `Statistiche` tiene i punti, il livello del mondo (di riferimento per mostri, loot e negozi), i mostri uccisi per classe, i turni e l'identificativo della partita.

## 9. Oggetti, artefatti, negozi

**Oggetti.** `Oggetto`/`OggettoBase` (con `ClassiOggetto`) sono ciò che si trova nelle locazioni: monete, pietre preziose, corone, cofani, artefatti, oggetti di missione (`OggettoMissione`, non si salvano: se il gruppo non li prende la missione decide di nuovo alla visita successiva). Gli **artefatti** (`Artefatto`, `ArtefattoMD`, tipi in `TipoArtefatto`: armi (spada, mazza, ascia, spadone, lancia, bastone magico), libro magico, scudo, elmo, armature (armatura, veste, maschera), schinieri, accessori (anello, talismano, ninnolo) e gli ingredienti magici: pergamena, gemma, monile, gingillo, sigillo) hanno livello, rarità (`TipoRaritaArtefatto`: comune, raro, leggendario), slot (`TipoSlotArtefatto`), danni, modificatori e incantamenti (`Incantamento`, `GradoIncantamento`), con un tetto al numero di effetti per rarità.

**Equipaggiamento.** `RegoleEquipaggiamento` impone gli slot (testa, corpo, mani, accessori), chi può usare cosa (l'arma secondaria solo a Ladro ed Elfo, le armi a due mani solo con entrambe le mani libere...) e il peso; la forza minima per le armature sta in `PersonaggioBase.puoEquipaggiare`. I **set leggendari** (`CatalogoLeggendari`, `RegoleSetLeggendari`) moltiplicano i bonus quando un personaggio indossa tutti i pezzi, solo al momento dei calcoli.

**Generazione.** `GeneratoreArtefatti` (con `GeneratoreArtefattiTabelle`) produce artefatti e pergamene per il loot e per i negozi: nomi ed effetti dalle tabelle interne e, per metà (`ARTEFATTO_PROBABILITA_DA_GRAMMATICA`) e per i tipi che la conosce, dalla grammatica `artefatti2.txt` (`GrammaticaArtefatti`); gli **ingredienti magici** vengono tutti da `ingredienti.txt`. `ListinoPergamene` ne calcola il prezzo dal valore degli effetti. Il generatore si precarica durante il logo. `CostruttoreArtefatto` (`tools`) è un builder fluente usato per i pezzi scritti a mano. `RegistroArtefatti` custodisce gli artefatti dei templi (alcuni scritti a mano, altri generati) e i **magazzini dei negozi di città**, che rifornisce alla creazione del mondo e a ogni aumento del livello del mondo (l'armaiolo scarta i pezzi troppo bassi).

**Negozi e scambi.** Armaiolo, venditore di pergamene e alchimista usano `AutomaScambiatoreArtefatti`, una classe astratta che modella uno scambio fra una `parteAttiva` e una `parteRemota` (interfaccia `ScambiatoreArtefatti`, realizzata da personaggi, gruppi e negozi), con le sottoclassi `AutomaInventario` (inventario del gruppo) e `AutomaAcquistiArtefatti`; lo spostamento fisico avviene solo dopo l'approvazione del motore. L'**incantatore** (`AutomaIncantatore`) offre la **fusione**: un banco di lavoro (`BancoDiLavoro`, che non si salva) con un artefatto incantabile e almeno un ingrediente, secondo `RegoleIncantatura` (costo 10 monete più 5 per effetto trasferito, ingredienti distrutti).

**Offerte** (`offerte`): i servizi che un incontro amichevole o una locanda propongono: `AiutoGratuito`, `AiutoMercenario`, `Incantesimi`, `Informazioni`, `MappaForesta`, `MappaZona`, `Pasto`. Ogni offerta dice se è fattibile, se è gratuita, come si descrive e come si accetta.

## 10. Incantesimi, missioni, intermezzi, trofei

### Incantesimi

`ClasseIncantesimo` elenca dieci formule: acqua, aria, terra, fuoco, fulmine, gelo, veleno (malefici, su più bersagli), morte (maleficio su un solo bersaglio vivo), resurrezione (benefica, su un solo bersaglio qualunque) e alba sacra (benefica, sul gruppo). Costi di acquisto, di lancio e danni sono in `Costanti`. Gli incantesimi si acquistano come pergamene e si consumano al lancio (il dardo arcano no); il danno passa dal normale calcolo del combattimento.

### Missioni

`missioni/` contiene la gerarchia `Missione`/`MissioneBase`/`MissioneAPassi` e le missioni concrete: le cinque principali (`SconfiggiIlDrago` e le quattro figlie che dipendono dagli alleati: `SconfiggiLIdra`, `SconfiggiIlLich`, `SconfiggiIlMinotauroGigante`, `SconfiggiLaStrega`), le due di recupero (`RecuperaIlMedaglione`, `RecuperaLeDerrateAlimentari`), le secondarie e una grande famiglia di incarichi a passi (mandanti, corrieri, sorveglianze, leggende...). L'automa le interroga in quattro momenti (`MomentoControllo`: `PRE_LOCAZIONE`, `IN_LOCAZIONE`, `POST_LOCAZIONE`, `ACCAMPAMENTO`) e può passare al giocatore una domanda (§3). `RegistroMissioni` (facciata su `RegistroMissioniMD`) tiene lo stato, rivendica le locazioni uniche, fornisce gli incontri e gli oggetti di missione per una casella, e notifica la UI con `NotificaAggiornamentoStatoMissione`. Missioni di prova (`MissioneDIProva...`) servono ai test. Tutto il resto è in [`gestione_missioni.md`](gestione_missioni.md) e [`passi_missioni.md`](passi_missioni.md).

### Intermezzi

`intermezzi/` contiene le scene a pagine, con lo stesso schema delle missioni ma più semplice: un `Intermezzo` ha un innesco (`deveScattare(MomentoIntermezzo)`) e delle `PaginaIntermezzo`, non ha progressi. Una pagina è composta a strati facoltativi: sfondo, elementi animati (`ElementoIntermezzo` con `Tappa` e `Ripetizione`), testo in alto e battute a fumetto (`BattutaIntermezzo`); `ImmagineIntermezzo` può essere un personaggio, una locazione, una risorsa, uno sprite sheet o un'animazione disegnata da codice (`Animazione`). I momenti di innesco (`MomentoIntermezzo`) comprendono `INIZIO_GIOCO`, `INIZIO_LOCAZIONE`, `LOCAZIONE_COMPLETATA`, `ACCAMPAMENTO` e gli ingressi nei negozi. `ClasseIntermezzo` elenca quelli fissi (introduttivo, fine della prima locazione, locanda alla prima e alla seconda visita, i quattro negozi, accampamento...); `IntermezzoDiPasso` aggiunge quelli dei passi conclusi delle missioni. `RegistroIntermezzi` sceglie il prossimo da mostrare e ricorda quelli scattati (`IntermezziMD`); uno è segnato come scattato quando parte, quindi non si ripete.

Nello stato `INTERMEZZO` l'automa pubblica `NotificaPaginaIntermezzo`, e la UI mostra la pagina a tutto schermo. La pagina avanza al click sulla pergamena o da sola dopo `getSecondiPerPagina()` (default `SECONDI_PER_PAGINA_INTERMEZZO` = 8, con 0 solo al click), e comunque non prima che dialoghi e animazioni non ripetute siano finiti (`PaginaIntermezzo.getSecondiPrimaDiAvanzare`, usato anche dall'anteprima). Se la UI sta ancora animando qualcosa (`InternoUiOccupata`), l'intermezzo pronto attende in `ATTESA_UI_PER_INTERMEZZO` fino a `InternoUiInattiva`. Dopo l'ultima pagina l'automa controlla se ce n'è un altro nello stesso momento (uno alla volta, nell'ordine dell'enum) e altrimenti prosegue. Guida pratica: [`intermezzi.md`](intermezzi.md).

### Trofei

`trofei/` contiene gli obiettivi che **valgono da una partita all'altra**: un `Trofeo` è un trigger che non scade mai e, una volta vinto, resta vinto. `ClasseTrofeo` elenca quelli esistenti: trofei a contatore (`TrofeoAContatore`: pasti in locanda, goblin uccisi, amicizie, corruzioni, scassinamenti, acquisti dall'armaiolo, incantature...), uccisioni di boss, refurtiva, bottino e `Perdigiorno` (vinto quando sono vinti tutti gli altri). `RegistroTrofei` (facciata su `TrofeiMD`) conta quanto si guadagna in una locazione e lo trasforma in progresso solo a fine locazione, se il gruppo ne esce vivo; il modello si legge all'avvio e non si reimposta mai. Sta nel file `trofei` di `~/.foresta/` (`GestoreTrofeiSuFile`) e si consulta dallo stato `TROFEI`, aperto dall'inventario.

## 11. Generazione procedurale di testo

`GrammarBean` è un motore di grammatiche generative (alternative pesate, riferimenti annidati, variabili, produzioni fissate e one-shot) con il suo manuale in `GrammarBean.md`. `ProduttoreDiTestiCasuale` carica a runtime **sette grammatiche**, tutte con lo stesso file di post-produzione `preposizioni_articolate_pp.txt` (per le preposizioni articolate) tranne l'ultima: `fiabe.txt`, `oroscopo.txt`, `locande.txt`, `templi.txt`, `rovine.txt`, `missioni.txt` e `leggendari.txt`. Offre produzioni per i nomi (locande, templi, rovine), per i testi e gli oggetti delle missioni (ostaggi, bardi, pellegrini, richieste di materiali, cose da consegnare, oggetti smarriti) e per i leggendari, e viene precaricato durante il logo. Le produzioni one-shot si azzerano a ogni nuova partita (`resetProduzioni`). Gli artefatti hanno una grammatica a parte (`GrammaticaArtefatti`, §9).

### Notizie delle locande

Uscendo da una locanda, `Locanda.generaNotizia()` produce una notizia satirica sulle malefatte del gruppo:
1. `ProduttoreDiTestiCasuale.getNotiziaLocanda` pesca da `NOTIZIE_<identificativo>` (quattro specifiche della locanda più le generiche); ogni alternativa è `ID-Titolo - Corpo`;
2. si riprova se l'id è fra le ultime `MASSIMO_NOTIZIE_RICORDATE` (10) o se la notizia non è applicabile, fino a 20 tentativi, poi `IllegalStateException`;
3. i segnaposto di classe (`BARDO`/`CANTASTORIE`, `LADRO`/`LADRA`, ...) rendono la notizia applicabile solo se nel gruppo c'è un personaggio vivo di quella classe e vengono sostituiti col suo nome; `EROE` diventa il nome del capo, e `NOME_LOCANDA`, `NOME_LOCANDIERE` e la desinenza di genere si sostituiscono;
4. la notizia esce due volte: come `NotificaTestoParagrafo` (pannello di testo) e come `NotificaNotizia` (notiziario della mappa).

`Notizie` tiene gli ultimi `MASSIMO_MESSAGGI_RICORDATI` (100) messaggi e le ultime 10 notizie, più recente in testa.

## 12. Strumenti per lo sviluppo e i test

- **Generatore di numeri** (`Dado`): l'unica sorgente di caso del motore, un `Random` statico. `impostaSeme(long)` rende ripetibile una partita; `trucca(Number...)` accoda i prossimi risultati (interi per i lanci, `Double` per le probabilità), `ripristina()` svuota la coda, `trucchiRimasti()` dice se il test ha lanciato quanto previsto. Lanci: `tira(N)`, `tira(min, max)`, le varianti tolleranti `tiraAncheAUnaFaccia` e `tiraAncheSenzaRange`, `probabilita()` e `selezionaCasualmente(lista)` (estrae e rimuove). Il generatore di artefatti e le grammatiche usano la sorgente direttamente: seguono il seme ma non consumano i lanci truccati.
- **Modalità di prova** (`ModalitaDiProva`, argomento `MODALITA_DI_PROVA` o `-DtestMode=true`): monete, pergamene e pozioni a volontà, mappa svelata, artefatti potenti e punti abilità al primo personaggio, missioni e intermezzo di prova. Nell'`Automa`, `inizializzaGioco` contiene le dotazioni "per prova".
- **Test di scenario** (`PartitaDiTest`, nei test): guida una partita al posto del giocatore e della UI. `PartitaDiTest.nuova(seme)` azzera il bus, ne rende immediata la consegna, fissa il seme, ricrea `ModelloDati` e i singleton dei gruppi, e usa salvataggi, classifica e trofei in memoria e un `TemporizzatoreManuale` che scatta solo con `scatta()`. Comandi e testi passano dal bus come dalla UI; un `InternoErrore` o `InternoException` fa fallire il test; gli intermezzi scorrono da soli dopo ogni passo (`nonSaltareIntermezzi()` per provarli). Un `Automa` si costruisce nei test con un esecutore di precaricamento sullo stesso thread e un orologio sostituibile. Ci sono oltre cento classi di test, fra cui molti scenari di missione (`Scenario*Test`), i calcoli (`CalcolatoreCombattimento*Test`, `DadoTest`, `GestoreProgressioneTest`), i negozi e l'incantatore.
- **Anteprima degli intermezzi** (`PartitaDiAnteprima`, `ui.AnteprimaIntermezzo`): una partita minima per vedere un intermezzo fuori dal gioco. Stanno fra i sorgenti dei test, perché non entrano nel jar. Vedi [`intermezzi.md`](intermezzi.md).

## 13. Osservazioni

Punti di attenzione verificati sul codice, utili per l'assessment e un eventuale refactoring.

- **`Automa` molto grande.** Quasi 2000 righe, circa 70 gestori e una trentina di campi di stato (intermezzo, domanda di missione, negozio in attesa, formulante...). Ogni nuova funzione aggiunge uno stato e un gestore, e le transizioni sono sparse nei metodi. L'avanzamento automatico però è ordinato (un solo ciclo, con tetto).
- **La UI legge e chiama il dominio direttamente.** 29 file di `ui` importano classi di `motore` (`GruppoGiocatore` in 12, poi `Foresta`, `Notizie`, `Statistiche`, `RegistroMissioni`, `Costanti`...), e `DisplayableCanvas` riceve dagli eventi `AutomaInventario` e `AutomaIncantatore`, che poi chiama. Il bus separa quindi i comandi, ma non lo stato. Da valutare: separare il modello dati dal motore, così che la UI lo consulti in sola lettura e verso il motore emetta solo eventi ([`todo.md`](todo.md)).
- **Doppio significato di `impostaAzioni(..., null)`.** Nelle locazioni vuol dire sia "prima entrata" sia "avanza di un passo", e in `LocazioneBase` ogni passo fa trascorrere un turno di effetti di stato. È stato aggirato con `ripresentaComandi()`, ma il contratto resta implicito. In particolare nei quattro casi `CHI_BEVE_POZIONE_*` la locazione richiama sé stessa con `null` per far trascorrere il turno dopo la pozione (probabilmente voluto: bere è un'azione).
- **Dipendenza Gson e vecchia grammatica degli artefatti: tolte (2026-10-05).** Gson non era usato da nessuna classe ed è stato tolto dal `pom.xml`; `artefatti.txt` e `artefatti_pp.txt`, sostituiti da `artefatti2.txt` e `artefatti2_pp.txt`, sono in `risorse_e_documenti_vari/` come raccolta di spunti.
- **Persistenza proprietaria senza versione.** Il formato a righe con `|` è fragile all'evoluzione dello schema; si accetta, perché la retrocompatibilità non interessa. Restano le due fragilità di `NotizieMD` (a-capo e `|` nelle notizie).
- **Robustezza delle notizie.** Il codice presuppone che ogni alternativa di `NOTIZIE_*` abbia la forma `ID-Titolo - Corpo` senza verificarlo: un'alternativa senza `-` farebbe fallire `substring` con un'eccezione sull'EDT. Un controllo di formato al caricamento della grammatica, o un test che produca tutte le alternative, proteggerebbe da questi casi. La produzione `NOTIZIE_CITTA` esiste in `locande.txt` ma nessuna classe la usa. Dopo 20 tentativi `getNotiziaLocanda` lancia `IllegalStateException` non intercettata: improbabile, ma possibile con un gruppo piccolo (molte notizie richiedono una classe viva). Tornare `null` e saltare la notizia sarebbe più tollerante.
- **Dimensione della mappa variabile.** `DIMENSIONE_X`/`DIMENSIONE_Y` (in `Foresta`) oggi valgono 20 e si stanno tarando con le prove; il limite tecnico per la codifica delle coordinate è 80.
- **Elenchi di cose da fare.** I `TODO`/`FIXME` generali che stavano in testa ad `Automa` sono ora in [`todo.md`](todo.md); quelli legati a un punto preciso del codice restano lì, e `todo.md` spiega come ritrovarli.
- **Combattimento da bilanciare.** Resta aperto un `FIXME` sul fatto che personaggi di livello 5 pesantemente armati non scalfiscano boss come la Strega o il Lich; il piano di bilanciamento è in [`artefatti_e_incantamenti.md`](artefatti_e_incantamenti.md) ed [`economia.md`](economia.md).
- **Punto di forza: `GrammarBean.md`.** È già un manuale e un assessment con difetti verificati sperimentalmente, ed è un buon modello per gli altri documenti.
