# Gestione delle missioni

Come il motore rappresenta, controlla, salva e collega al resto del gioco le missioni: l'infrastruttura. Il vocabolario dei passi e la mappatura dei tipi di missione sono in [`passi_missioni.md`](passi_missioni.md); per l'automa che le controlla vedi [`motore_di_gioco.md`](motore_di_gioco.md) §3; per gli intermezzi [`intermezzi.md`](intermezzi.md).

Tutto il codice è in `missioni/` (modello e missioni concrete), in `motore/RegistroMissioni`, `motore/RegistroIntermezzi` e `motore/modellodati/MissioneMD`, `RegistroMissioniMD`.

## 1. Il modello

### `Missione` e `MissioneBase`

`Missione` è l'interfaccia di una missione: identificatore, nome, descrizione (con il flag "visibile" per il riquadro delle missioni), i **tre controlli** dell'automa (`controllaPreLocazione`, `controllaInLocazione`, `controllaPostLocazione`) più `controllaAccampamento` (di default non fa niente), lo stato (`isAttiva`, `isCompleta`, `isFallita`, `isPrimaria`), le **proprietà** (`ottieniProprieta`, `aggiungiProprieta`, `rimuoviProprieta`) e le **missioni secondarie** (figlie). In più cinque metodi di default, con cui una missione influisce sul mondo quando il gruppo entra in una locazione:

| Metodo | A che cosa serve |
| :--- | :--- |
| `getIncontroInLocazione(coordinate)` | gli avversari che la missione vuole in quella locazione, al posto di quelli normali |
| `getOndateSuccessiveInLocazione(coordinate)` | le ondate che arrivano dopo, a combattimento vinto |
| `getOggettoInLocazione(coordinate, classe, visitata)` | l'oggetto che la missione vuole al posto di quello della locazione |
| `getRicordoDellaLocazione()` | la frase per chi entra, a missione finita, nel posto che aveva rivendicato ("Qui sorgeva il castello della Strega.") |
| `controllaAccampamento()` | il controllo all'accampamento (vedi [`motore_di_gioco.md`](motore_di_gioco.md) §3) |

`MissioneBase` realizza il comune. Lo **stato è nelle proprietà** (`ATTIVA`, `COMPLETA`, `FALLITA`: presenza della chiave), non in campi Java, perché le proprietà si salvano. `completaMissione()` segna la missione completata, la sposta nel registro, pubblica `NotificaAggiornamentoStatoMissione` ("MISSIONE COMPLETATA") e `InternoMissioneCompletata`, e dà esperienza: il 50% dei punti per il livello successivo per una missione principale, il 20% per le altre (`GestoreProgressione`). `fallisciMissione()` non fa nulla se la missione è già finita; altrimenti la segna fallita, la sposta fra le fallite e non dà esperienza. L'avviso "NUOVA MISSIONE" e quello "MISSIONE FALLITA" arrivano dalla base solo per le missioni predefinite; per quelle a passi se ne occupa `MissioneAPassi`.

### Le missioni formano un albero

Ogni missione può avere **figlie** (`aggiungiMissione`, `getMissioniSecondarie`). L'albero è quello del modello dati: `MissioneMD` tiene l'identificatore (un UUID, o il nome del `TipoMissionePredefinita` per le radici predefinite), la `ClasseMissione`, nome, descrizione, flag di visibilità, la mappa delle proprietà e la lista delle figlie. Una missione non può essere figlia di sé stessa (un ciclo renderebbe infinite le ricorsioni di salvataggio e di ricostruzione). Un esempio è `SconfiggiIlDrago`, che nel costruttore ha come figlie le quattro missioni degli alleati.

### Classi di missione

`ClasseMissione` elenca **ogni classe concreta** di missione, con il suo costruttore: è ciò che permette di ricostruire un albero dopo un caricamento, perché ogni nodo salvato dichiara la propria classe. Ne consegue una regola: **ogni missione che il gioco può creare, anche solo come figlia affidata a metà partita, deve stare in `ClasseMissione`**. Quelle che nascono con la partita stanno anche in `RegistroMissioni.TipoMissionePredefinita` (§2), che elenca solo le radici.

Le missioni si dividono per come sono scritte:
- **a mano**, direttamente su `MissioneBase`: le principali (`SconfiggiIlDrago` e le quattro `Sconfiggi*` figlie), le missioni di prova, e alcune secondarie semplici (`Combatti` e le sue derivate, `VisitaLocanda`, `MuoviALocazione`, `CronacheDiUnFegatoEroico`, `NessunBoccaleLasciatoIndietro`, `DisturbatoreDellaQuietePubblica`, `MissioneSecondaria`);
- **a passi**, su `MissioneAPassi` (§4): tutto il resto, cioè le due missioni di recupero, gli incarichi in città, le leggende, i rituali e le altre. È il modo in cui si scrivono le missioni nuove.

## 2. Il registro (`RegistroMissioni`)

`RegistroMissioni` è la facciata statica sul modello dati (`RegistroMissioniMD`: due mappe, attive e completate) e tiene in memoria gli elenchi di primo livello:

- le **predefinite** (`TipoMissionePredefinita`: la missione principale, le due di recupero, le leggende, gli incarichi e le altre che esistono dall'inizio) in tre elenchi: in corso, completate, fallite. All'inizio della partita (`reimposta`) se ne crea una per ogni valore (le due missioni di prova solo in modalità di prova); nessuna è attiva finché non si attiva da sola (§3). L'identificatore di una predefinita è il nome del suo valore;
- le **secondarie di primo livello** (`aggiungiMissioneSecondaria`): missioni nate durante la partita fuori dall'albero della principale, per esempio l'incarico in città che ne ripete uno finito. Anche queste in corso, completate, fallite.

Le figlie non stanno in nessun elenco: una figlia completata o fallita resta dentro la sua madre con il proprio flag. Le interrogazioni principali:

| Metodo | Restituisce |
| :--- | :--- |
| `getMissioniNonCompletate()` | le radici ancora in corso (attive e da attivare); la discesa nell'albero spetta al chiamante |
| `getMissioniAttive()` | le radici attive |
| `getMissioniCompletate()` | le radici completate, e le figlie completate di quelle ancora in corso |
| `getMissioniFallite()` | le radici fallite |
| `getTutteLeMissioni()` | ogni missione dell'albero, una volta sola e in qualunque stato (serve a chi cerca ciò che una missione ha lasciato in sospeso anche dopo la fine: l'intermezzo dell'ultimo passo, i claim) |
| `getMissione(id)` | la missione con quell'identificatore |
| `getMissionePrincipale()` | `SconfiggiIlDrago`, in corso o completata |

**Eventi di gioco.** `registrati()` (chiamato da `Main`) iscrive il registro al bus per contare ciò che le missioni a passi vogliono sapere: l'apertura di un combattimento, gli avversari sconfitti (con la casella), gli oggetti raccolti, i passaggi inosservati. Ognuno è girato a tutte le missioni a passi non finite (`registraEvento`).

**Salvataggio e rilettura.** Si salvano i due elenchi di `RegistroMissioniMD` (attive e completate, ciascuno con tutto il proprio albero). `aggiornaDopoRilettura()` ricostruisce gli oggetti: per una predefinita crea l'istanza dal suo tipo, per le altre dalla `ClasseMissione` dichiarata nel nodo, poi riattacca ricorsivamente le figlie (`setModelloDati` svuota la lista delle figlie costruite dal costruttore, perché la struttura appartiene al modello dati: figlie con identificatori diversi da quelli salvati farebbero sparire i progressi). Poi smista in corso/completate/fallite e ricostruisce i claim delle locazioni (§6).

**Chi sa se la partita è vinta.** `Automa` chiede a `getMissionePrincipale()` se è completa per decidere fra `GIOCO_VINTO` e `GIOCO_PERSO` (vedi [`motore_di_gioco.md`](motore_di_gioco.md) §3).

## 3. Quando una missione viene controllata

L'automa chiama i controlli in quattro momenti (`MomentoControllo`: `PRE_LOCAZIONE`, `IN_LOCAZIONE`, `POST_LOCAZIONE`, `ACCAMPAMENTO`), sempre sull'**albero** delle missioni non completate: dopo una radice si scende nelle figlie, ma solo se la missione è attiva e non conclusa, e saltando le figlie finite. Il `POST_LOCAZIONE` visita **prima le figlie e poi il padre** (così il padre vede lo stato già aggiornato, per esempio le quattro figlie complete); gli altri tre prima il padre. Si lavora su una copia della lista delle figlie, perché un controllo può aggiungerne (le missioni affidate).

Non c'è una "assegnazione" delle missioni: **ognuna decide da sé quando partire**, nel suo controllo. Alcuni esempi:
- `SconfiggiIlDrago` si attiva al primo `PRE_LOCAZIONE` e scrive la sua descrizione;
- ogni missione `Sconfiggi*` degli alleati, a `PRE_LOCAZIONE`, cerca di rivendicare un bosco su cui costruire il suo castello (§6) e si attiva solo se ci riesce, altrimenti riprova al controllo successivo;
- un incarico in città si offre da sé quando il gruppo è nella città giusta (§8).

Dopo ogni controllo, l'automa guarda se una missione a passi ha una **domanda** da porre al giocatore e, in quel caso, va in `ATTESA_RISPOSTA_MISSIONE`; vedi [`motore_di_gioco.md`](motore_di_gioco.md) §3 per il flusso e §4 per i comandi.

## 4. Le missioni a passi

### `Passo`

Un `Passo` è un pezzo di logica di avanzamento dentro **una sola missione**: non è una `Missione` e non entra nell'albero. Si costruisce con un builder fluente, come `Passo.quando(momento, condizione).esegui(azione).poi("PROSSIMO")`:

| Elemento | Significato |
| :--- | :--- |
| `quando(momento, condizione)` | in quale controllo si valuta e quando è concluso |
| `esegui(azione)` | che cosa fare, una volta sola, alla conclusione (si può chiamare più volte, in ordine) |
| `aOgniControllo(azione)` | che cosa fare a ogni valutazione, prima di vedere se è concluso (per tenere conti che dipendono da dove si trova il gruppo) |
| `poi(id)` o `poi(supplier)` | il prossimo passo, fisso o calcolato dopo l'azione (una **diramazione**); `Passo.FINE` chiude la missione |
| `falliscoSe(condizione, testo)` | finché è il passo corrente, se la condizione è vera la missione fallisce con quel testo; vale in tutti i controlli, prima di tutto il resto |
| `conIntermezzo(momento, pagine)` | un intermezzo da mostrare, in quel momento, dopo la conclusione; le pagine si costruiscono quando scatta |
| `chiediConferma(domanda)`, `chiediScelta(domanda, opzioni)` | una domanda al giocatore, sì/no o da 2 a 5 opzioni; la condizione dice quando si può porre |
| `semina(oggetti)`, `affronta(dove, incontro)` | finché è il passo corrente, la missione mette oggetti nelle locazioni, o avversari in una locazione precisa |

I passi **non si salvano**: la missione li ricostruisce dal loro id ogni volta che servono, quindi condizioni e azioni sono lambda qualsiasi. Gli id sono stringhe senza `,`, `§`, `|` (finiscono nelle proprietà e in liste separate da virgole).

### `MissioneAPassi`

È la base delle missioni a passi: un **piccolo automa** i cui stati sono i passi, identificati da una chiave e non da una posizione in una lista, così una missione può diramarsi secondo lo stato di gioco o la risposta del giocatore e può costruire i passi al volo. Una sottoclasse definisce `passoIniziale()` e `costruisciPasso(id)`; quest'ultimo **deve saper ricostruire ogni id che la missione ha mai usato**, anche dopo un salvataggio.

**Avanzamento.** A ogni controllo (`avanzaSePronto`), se la missione non è finita:
1. si costruisce il passo corrente; se una guardia `falliscoSe` è scattata la missione fallisce (con il testo nel pannello);
2. se il passo non è di quel controllo, ci si ferma; altrimenti si eseguono le azioni `aOgniControllo`;
3. se il passo non è concluso (per una domanda: se non c'è la risposta) ci si ferma;
4. si esegue l'azione, si ricorda l'eventuale intermezzo in attesa, si calcola il prossimo passo, lo si salva (`PASSO_CORRENTE`) e se ne segna l'inizio in ore di gioco;
5. se il prossimo è `FINE` la missione si completa (se l'azione non l'ha già fatto), altrimenti si **prosegue subito** con il passo successivo se è dello stesso controllo e già concluso, così un passo di solo testo non costa un turno. Oltre 50 passi di fila nello stesso controllo lancia `IllegalStateException` (un ciclo fra passi).

Si salva solo l'id del passo corrente (`PASSO_CORRENTE`) più le proprietà che le sottoclassi scrivono: dopo un caricamento la missione riprende dal passo giusto (lo verifica `MissioneAPassiTest`).

**Attivazione.** `attivaMissione()` ricorda la casella in cui il gruppo si trovava (`PUNTO_DI_PARTENZA`, a cui si può poi tornare) e l'ora d'inizio del passo corrente.

**Domande.** `getDomandaDaPorre(momento)` restituisce il passo corrente se è una domanda di quel momento, la sua condizione è vera e non c'è ancora risposta; l'automa la pone e consegna la risposta con `rispondi(risposta)` (che la valida e la salva come `RISPOSTA_<id>`). Il passo si conclude al controllo successivo; la diramazione legge la risposta con `getRisposta(idPasso)`. `dimenticaRisposta` fa porre la domanda di nuovo.

**Eventi contati per passo.** `registraEvento(evento, quantità)` somma l'evento al contatore del **passo corrente** (`EVENTO_<passo>_<evento>`): un passo conta solo ciò che succede da quando è corrente. Gli eventi sono nomi costruiti da metodi statici: avversario sconfitto (di una classe, anche in una casella precisa), combattimento, passaggio inosservato, oggetto raccolto. Esistono anche **contatori** per chiave scelta dalla missione (`incrementaContatore`, `getContatore`), usati per gli oggetti di missione.

**Tempo.** `oreDiGioco()` converte la linea temporale in ore; ogni passo ricorda quando è diventato corrente (`INIZIO_<id>`), e così funzionano le attese e i ripieghi.

**Parametri.** Una missione generata fissa i suoi valori variabili (il mandante, il bersaglio, la quantità) come proprietà con `generaParametri` o `parametro(nome, generatore)`: la prima volta si pesca, poi resta quello, anche dopo un salvataggio.

**Sequenze fissate alla generazione.** `impostaSequenzaPassi(ids)` decide una volta sola un ordine di passi (ad esempio "visita N locazioni scelte a caso") e `prossimoNellaSequenza()` lo percorre.

**Missioni ripetibili.** Se `isRipetibile()` è vero, una missione finita (bene o male) ne lascia **una nuova della stessa classe**, aggiunta alle secondarie di primo livello, che si potrà cominciare dopo `ORE_FRA_UNA_MISSIONE_E_L_ALTRA` (48) ore di gioco (`isDisponibile()` va messo nella condizione del suo primo passo). Si ripete una volta sola per missione (`GIA_RIPETUTA`). Le missioni affidate non devono essere ripetibili.

**Missioni affidate.** Un passo può **affidare** missioni secondarie decise in quel momento (`affida(chiave, momento, quando, fornitore)`): il fornitore le crea, diventano figlie della madre (entrano nel suo modello dati e si salvano con lei), vengono attivate e i loro id si ricordano sotto la chiave; si affidano una sola volta. `attendiLeAffidate(chiave, momento)` si conclude quando sono finite tutte; `sonoRiusciteLeAffidate` e `getMissioniAffidate` dicono com'è andata, per diramare.

**Scorta.** Una missione può prendere con sé un `Viandante` (o un personaggio fatto da lei) come **ospite** del gruppo (`prendiInScorta`, anche vulnerabile), che a destinazione si separa; se è vulnerabile e muore la missione può fallire o ramificarsi (`scorta`, `scortaFinoAllaMeta`, `isScortatoMorto`). Quando la missione fallisce o finisce lo scortato si separa.

**Hook sul mondo.** Le sottoclassi realizzano i cinque metodi di default di `Missione` (§1) leggendo il passo corrente: gli avversari dell'incontro (`getIncontroInLocazione`), le ondate, gli oggetti seminati (`getOggettoInLocazione`).

I **passi già pronti** che le missioni compongono (VAI, DIALOGO, RICOMPENSA, ATTENDI, CONTA_FINCHE, SCONFIGGI, RACCOGLI, CONSEGNA, SORVEGLIA, ESPLORA, EVITA_COMBATTIMENTO, COSTRUISCI, COMBATTI, SCORTA, AFFIDA, CERCA_LOCAZIONE...) sono descritti in [`passi_missioni.md`](passi_missioni.md).

## 5. Intermezzi di passo

Un passo con `conIntermezzo` non mostra la scena da sé: quando si conclude, il suo id finisce fra gli **intermezzi in attesa** della missione (`INTERMEZZI_IN_ATTESA`, in ordine di conclusione); chi lo mostra lo segna con `segnaIntermezzoPassoMostrato` (`INTERMEZZO_MOSTRATO_<id>`). Il "già mostrato" **vive nella missione**, non in `IntermezziMD`, perché ogni istanza di una missione generata ha il suo.

- **`RegistroIntermezzi.getProssimoIntermezzo(momento)`** cerca in quest'ordine: gli intermezzi fissi di `ClasseIntermezzo` non di ripiego, poi quelli dei passi delle missioni (`getTutteLeMissioni`, in qualunque stato: l'ultimo passo di una missione può avere un intermezzo e insieme completarla), poi quelli fissi di ripiego solo se nel momento non è già scattato nient'altro (`scattatoNelMomento`, azzerato da `nuovoMomento()` a ogni nuovo momento dell'automa).
- **`IntermezzoDiPasso`** (in `missioni/`) adatta un passo all'interfaccia `Intermezzo`: l'id serve solo ai log (`<missione>/<passo>`) e il "mostrato" è delegato alla missione.
- **Quando si vede.** Un passo `POST_LOCAZIONE` con intermezzo `LOCAZIONE_COMPLETATA` si vede nello stesso turno, perché il controllo corre prima del momento. Un passo `IN_LOCAZIONE` con intermezzo `INIZIO_LOCAZIONE` si vede invece all'ingresso nella locazione successiva, perché quel momento viene prima dei controlli in locazione.
- **Visita tranquilla.** `haUnIntermezzoInArrivo(controllo, momento)` dice se una missione mostrerà un intermezzo, e `isVisitaTranquilla()` se nessun'altra missione ne mostrerà uno entrando nella locazione: serve agli incarichi che si offrono in una città qualsiasi (§8), perché non si sovrappongano alle altre scene. Le missioni che aspettano una visita tranquilla si guardano fra loro solo quando l'intermezzo è già in attesa; se si guardassero l'un l'altra prima di partire non partirebbe nessuna.

## 6. Le locazioni rivendicate (claim)

Alcune missioni hanno bisogno di un **luogo**: un covo, un tempio, un castello. Non lo creano a caso: lo **rivendicano** (claim) in `RegistroMissioni`, che tiene per ogni coordinata l'id della missione che l'ha rivendicata per ultima (`locazioniOccupate`). La missione ricorda la propria coordinata nella proprietà `LOCAZIONE_OCCUPATA` (con un numero d'ordine progressivo) e ne rivendica **una alla volta** (`occupaLocazione`); il registro non salva i claim a parte, li ricostruisce dopo la rilettura dalle proprietà, nell'ordine in cui furono fatti.

**Regola di disponibilità** (`isDisponibile(coordinate, richiedente)`): una casella è libera per una missione se nessuna missione l'ha rivendicata, oppure se chi l'ha fatto è **la stessa missione**, oppure se è **finita** (completa o fallita). Un claim non si cancella mai: si sovrascrive, così la casella ricorda chi l'ha avuta anche dopo la fine e se ne può leggere il **ricordo** (`getRicordo`, dalla frase di `getRicordoDellaLocazione`: "Qui sorgeva il castello della Strega" fra le rovine).

**Ricerca.** `cerca(classe, missione[, quadrante])` esplora la mappa **a quadrati concentrici** attorno a una casella a caso (o a caso nel quadrante) e rivendica la prima locazione già esistente di quella classe che sia disponibile, che non sia la casella del gruppo (non gli si cambia la locazione sotto i piedi) e che non custodisca un artefatto del registro; per un tempio la segna come non ancora visitata. `cercaOCostruisci` aggiunge il caso in cui non ci sia niente: **costruisce** la locazione al posto di un bosco o di una palude disponibili, preferendo quelli già visitati, e la rivendica. Vuoto solo se non c'è nemmeno un bosco da sostituire: la missione riproverà al controllo successivo.

**Locazioni uniche.** `rivendicaPerLocazioneUnica(unica, suCasellaDi, missione)` cerca una casella e ci costruisce una **locazione unica** (un castello) come non ancora visitata. Il castello di un alleato del Drago lo cerca in un quadrante dove non c'è ancora un altro castello (`quadranteSenza`), così i quattro finiscono uno per quadrante; quello del Drago, che arriva dopo, va dovunque. Chi lo cerca resta non attivo finché non lo trova.

**Contenuto soppresso.** Una missione che conclude all'arrivo in un posto che considera sicuro (un tempio per una benedizione) lo segnala con `sopprimiContenutoLocazione`, da un `aOgniControllo` del passo d'arrivo (non dall'azione: il passo potrebbe concludersi e incatenarsi prima che l'automa consumi il segnale); `isDaSopprimere(coordinate)` lo consuma una volta sola e, in `PREPARAZIONE_LOCAZIONE`, toglie avversari e oggetto a caso.

**Segnalazione sulla mappa.** Le caselle rivendicate dalle missioni a passi attive e conosciute dal gruppo lampeggiano sulla mappa (`getLocazioniDaSegnalare`, con il nome della missione da `getNomeMissioneDaSegnalare`); i castelli delle missioni `Sconfiggi*` no.

## 7. La missione principale e i castelli

`SconfiggiIlDrago` è la missione principale (`isPrimaria()`). Le sue quattro figlie sono `SconfiggiLaStrega`, `SconfiggiIlLich`, `SconfiggiIlMinotauroGigante` e `SconfiggiLIdra`, scritte a mano:
- ciascuna, a `PRE_LOCAZIONE`, rivendica un bosco per il suo castello (`CASTELLO_*`) e solo allora si attiva;
- a `POST_LOCAZIONE`, se il gruppo è in quel castello e la locazione è completa, si completa;
- tengono il claim anche dopo il completamento (con il ricordo dei castelli diventati rovine);
- sono "sconfitte" quando sono complete, e `castelliDistrutti()` del Drago lo verifica sulle figlie, che restano sempre nell'albero (regge dopo un caricamento).

`SconfiggiIlDrago` si attiva al primo `PRE_LOCAZIONE` e scrive la descrizione; a `POST_LOCAZIONE`, finché il Drago non è apparso e le quattro figlie sono complete, rivendica per lui un bosco e fa apparire il castello (`DRAGO_APPARSO`) con l'avviso che l'incantesimo che lo nascondeva è svanito; una volta apparso, se il gruppo completa il castello del Drago la missione si completa, annuncia la vittoria (`NotificaGlobale`) e segna il gioco come finito (`LineaTemporale.setGiocoFinito(true)`); poi `FINE_LOCAZIONE_2` vede il gioco finito e la missione principale completa e passa a `GIOCO_VINTO` (vedi [`motore_di_gioco.md`](motore_di_gioco.md) §3).

## 8. Gli incarichi in città (`IncaricoInCitta`)

`IncaricoInCitta` è la base degli incarichi in città: un mandante chiede un servizio, il gruppo lo fa e torna a riscuotere. La struttura è fissa, i passi del compito si inseriscono in mezzo:

1. `INCARICO` (`PRE_LOCAZIONE`, in una città): la missione si ricorda la città (`CITTA`), pesca i nomi dei personaggi (`allIncarico`) e parte l'intermezzo del mandante;
2. `ACCETTAZIONE` (`IN_LOCAZIONE`, nella città): la missione si **attiva**, dopo l'intermezzo, così l'avviso non lo precede;
3. i passi del compito, da `primoPassoDelCompito()`; l'ultimo va a `RITORNO`;
4. `RITORNO` (`PRE_LOCAZIONE`, nella città di ritorno): l'intermezzo del ringraziamento;
5. `CONSEGNA` (se il compito era procurarsi oggetti): il gruppo li consegna;
6. `RICOMPENSA` (`IN_LOCAZIONE`): le monete, e la missione si completa.

Se la città in cui si riscuote viene distrutta, **ogni passo** (anche quelli del compito) ha una guardia `falliscoSe` che fa fallire la missione. Si riscuote in un'altra città se l'incarico è portare qualcosa a qualcuno (`getCittaDelRitorno`).

Due famiglie:
- **città fissa** (`getCittaFissa()`): la storia di una città (il medaglione di Fleena, le derrate di Ruuna, sulla base `MissioneRecuperaBersaglio`); parte alla prima visita e non si ripete;
- **città qualsiasi**: si offre in una città qualsiasi, ma solo a una **visita tranquilla** (§5) e passata la pausa dopo l'incarico precedente; è ripetibile.

`MissioneRecuperaBersaglio` aggiunge due passi al compito: `COVO` (compare il covo, rivendicato dalla missione, subito dopo l'accettazione) e `RECUPERO` (a fine locazione, nel covo completato, il claim passa sulla città di ritorno e si torna a riscuotere).

## 9. Come si scrive una missione nuova

1. Estendere `MissioneAPassi` (o `IncaricoInCitta` per un incarico) e implementare `passoIniziale()` e `costruisciPasso(id)`, componendo i passi già pronti di [`passi_missioni.md`](passi_missioni.md) e collegandoli con `poi`.
2. Aggiungerla a `ClasseMissione` (**obbligatorio**, altrimenti non si ricostruisce dopo un caricamento) e, se deve esistere dall'inizio, a `RegistroMissioni.TipoMissionePredefinita`.
3. Salvare in proprietà (`aggiungiProprieta` o `parametro`) tutto ciò che deve sopravvivere: i passi non si salvano e i campi Java si perdono.
4. Se la condizione di un passo ha effetti (pesca un nome, cambia lo stato), spostarli nell'azione: la condizione può essere valutata più volte e per i passi con intermezzo anche in anticipo (`haUnIntermezzoInArrivo`).
5. Se la missione vuole un luogo, rivendicarlo con `cercaLocazione` (o `RegistroMissioni.cerca`/`cercaOCostruisci`) e dare un `getRicordoDellaLocazione()`.
6. Scriverne il test di scenario con `PartitaDiTest` (vedi [`motore_di_gioco.md`](motore_di_gioco.md) §12): `ScenarioIncarichiInCittaTest` e `ScenarioMissioniDiRecuperoTest` sono i modelli per gli incarichi.

## 10. Test

`MissioneAPassiTest` copre il motore dei passi: avanzamento nel controllo giusto, diramazioni, fine, intermezzi in attesa, ripresa dopo un salvataggio, missione fallita, sequenze, cicli, domande, contatori, eventi per passo, guardie, attese. Sopra stanno una cinquantina di test di scenario (`Scenario*Test` in `src/test/.../missioni`), uno per famiglia di missioni, che giocano una partita vera con `PartitaDiTest`: fra i trasversali, `ScenarioDomandeMissioniTest` (la domanda al giocatore), `ScenarioLocazioniRivendicateTest`, `ScenarioMissioniELocazioniTest` e `ScenarioQuadrantiTest` (claim e castelli), `ScenarioMissioniAffidateTest`, `ScenarioIncarichiRipetutiTest`, `ScenarioOspitiVulnerabiliTest`, `ScenarioOndateTest`, `ScenarioCaselleSegnalateTest`. `RegistroIntermezziPassiTest` verifica gli intermezzi di passo.

## 11. Da fare e idee aperte

- **`FornitoreMissione`.** L'interfaccia esiste (`interfacce/FornitoreMissione.java`, un solo metodo `fornisciProssimaMissione()`), ma **nessun codice la usa**. L'idea era farla usare dai quattro punti in cui il gioco crea "la prossima missione" (`RegistroMissioni.reimposta`, `aggiornaDopoRilettura`, `ricostruisci`, `MissioneAPassi.lasciaUnaMissioneNuova`), così che nei test, o da uno strumento di debug, si possa fornire una missione scelta a mano invece di una generata a caso.
- **Registro dei personaggi incontrati.** Oggi un avversario esiste solo per la locazione in cui combatte; dei personaggi con un nome resta traccia solo nei parametri della missione che li ha creati. Con la resa (vedi [`passi_missioni.md`](passi_missioni.md)) uno sfidante sconfitto può restare vivo, e vale la pena ricordarselo. L'idea: un registro salvato con la partita (nome, classe, livello, dove e in che missione, com'è finita, che rapporto ha col gruppo) da cui le missioni nuove possano **riprendere** un personaggio invece di pescarne uno dalla grammatica (un ladro che ha perso un duello, il campione di un torneo che torna per la rivincita), da usare anche come **aiuto** temporaneo, con i **morti** tolti, e con **nomi unici** (le produzioni dei nomi vanno rese one-shot, e il controllo va fatto contro il registro perché la grammatica non si salva). Non è realizzata: il pacchetto `personaggi` non ha un registro di questo tipo.
- **Verifica a mano nel gioco.** Le note precedenti segnalavano come non ancora provati a mano, nel gioco vero: l'aspetto della domanda di missione con le icone di risposta, e l'intero percorso di un incarico dal primo intermezzo alla ricompensa, con salvataggio e ricarica a metà.

## 12. Osservazioni

- **Due stili di missione.** Le principali, le missioni di prova e le più vecchie secondarie sono scritte direttamente su `MissioneBase` con flag e controlli a mano; tutto il resto è a passi. Non c'è un'unica via, e le prime non hanno gli intermezzi per passo, le domande, i contatori, i ripieghi.
- **Predefinite e `ClasseMissione` da tenere allineate a mano.** Una missione dimenticata in `ClasseMissione` non dà errori in partita: fallisce solo alla rilettura di un salvataggio.
- **Un solo claim per missione.** `occupaLocazione` sovrascrive la coordinata precedente di quella missione (`LOCAZIONE_OCCUPATA` ha un solo valore): una missione che dovesse tenere insieme più luoghi non può.
- **Condizioni con effetti.** `haUnIntermezzoInArrivo` valuta la condizione del passo corrente solo se ha un intermezzo, e dice nel Javadoc che quelle condizioni non devono avere effetti: è una convenzione non controllata.
- **`getTutteLeMissioni()` costruisce una lista a ogni chiamata.** Viene chiamata una volta per evento di gioco (`registraEvento`), a ogni controllo dell'intermezzo e, tramite `isVisitaTranquilla`, una volta per ogni missione che la valuta (quindi O(N²) sul numero di missioni). Con qualche decina di missioni il costo è di microsecondi e scatta una volta per mossa del giocatore: è una scelta consapevole, non un difetto. Una cache dell'elenco richiederebbe di invalidarla in ogni punto che modifica l'albero (anche le sotto-missioni create dalle missioni stesse), col rischio di un elenco vecchio. Se le missioni crescessero molto, la via meno rischiosa è passare uno snapshot dal chiamante a `isVisitaTranquilla`, il che richiede di portare la lista fino alle condizioni dei passi.
