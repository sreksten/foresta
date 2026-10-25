# foresta-economia

Il **denaro e il commercio** del gioco: la **cassa** del gruppo (monete e pietre preziose), i **magazzini** dei negozi, i **prezzi** e la **contrattazione**, il commercio del gruppo con il mondo (comprare e vendere artefatti, comprare dall'alchimista, far fondere dall'incantatore, vendere i preziosi) e le **offerte** che si ricevono facendo amicizia, corrompendo o in locanda (un aiuto, delle pergamene, informazioni, una mappa, un pasto).

Il filo conduttore è «il gruppo non sa di avere i soldi»: **il gruppo sa solo di avere monete, artefatti e scorte**; i prezzi, le botteghe e il livello del mondo sono di questo modulo. Così i personaggi non dipendono dall'economia, e l'economia non è sparsa fra il gruppo e il motore.

## Dipendenze

**Dichiarate nel pom:** `foresta-mondo`, `foresta-poteri`.

**Solo per i test:** `foresta-personaggi`.

**Lo dichiarano:** `foresta-missioni`.

Le dipendenze sono transitive: un modulo vede anche quello che vedono i moduli da cui dipende, quindi il compilatore garantisce la separazione fra i moduli che non si raggiungono.

- `foresta-mondo`: i magazzini si riempiono con gli artefatti del mondo e per le botteghe serve sapere chi sono i negozianti (`RegistroPersonaggi.getNegoziante`).
- `foresta-poteri`: le pergamene sono incantesimi (`TipoIncantesimo`) e le offerte di incantesimi usano `FabbricaIncantesimi`.
- Per transitività: personaggi, artefatti, mappa, statistiche e intermezzi.

## Funzionamento

### La cassa e i suoi dati

`Cassa` governa le monete e le pietre preziose del gruppo con i dati di `CassaMD` (si salva su una riga: monete e preziosi); i dati li prende da chi li custodisce (`setFornitoreDiDati`). Il gruppo inizia con 150 monete e 5 preziosi (`CostantiEconomia`). La UI la legge da `VistaCassa`. Le variazioni si annunciano con `NotificaVariazioneDisponibilitaMonete` e `…Preziosi`.

### Prezzi e contrattazione

`RegoleContrattazione` fissa quanto si paga e quanto si ricava secondo la **contrattazione** del gruppo (un attributo secondario): con `bonus = contrattazione / (contrattazione + saturazione)`, che satura verso 1, lo **sconto sugli acquisti** è 0,24 × bonus (al massimo il 20%) e la **quota ricavata vendendo** è 0,50 + 0,30 × bonus (al massimo il 75%). Il peggior acquisto (80%) resta sopra la miglior vendita (75%): **comprare e rivendere non fa mai guadagnare**. Gli arrotondamenti vanno a favore del mercante.

### Il commercio del gruppo: `CommercioDelGruppo`

Dopo `registrati()` ascolta gli eventi del commercio (`InternoAcquistoArtefatto`, `InternoVenditaArtefatto`, `InternoIncantatura`, `ComandoAcquistoConsumabile`) e li gestisce con il pattern **richiesta e verdetto**: la UI manda l'intenzione, il commercio verifica (fondi, peso, regole del negozio) e pubblica un'**approvazione** o un **rifiuto con il motivo**; solo dopo l'approvazione l'oggetto si sposta. Le funzioni: `prezzoAcquisto`, `prezzoVendita`, `costoFusione` (la fusione è scontata come un acquisto), `vendePreziosi` (un prezioso vale quanto il livello del mondo), `vende` e `incanta`.

### I magazzini: `MagazziniDeiNegozi`

I magazzini dei negozi delle città e delle botteghe della Foresta (armaiolo e venditore di pergamene, per `TipoNegozio`): come si riempiono (`riempiMagazzino` con il generatore di artefatti), come si riforniscono quando il mondo sale di livello e che cosa fanno di quanto il gruppo vende loro. `getScambiatorePerNegozio` dà alla UI lo scambiatore del negozio di una casella. I dati sono `MagazziniMD` (gli artefatti in vendita per tipo di negozio e per città). Il magazzino dell'alchimista non è di artefatti: il suo listino lo costruisce `OfferteAlchimista` con `OffertaConsumabile` (nome, descrizione, costo), e `TipoConsumabile` elenca che cosa vende (pozioni di salute e di magia, aumenti di magia, mappa parziale della Foresta…). Il contratto per la UI è `VistaOffertaConsumabile`.

### Le offerte: `offerte`

`Offerta` dice se un'offerta è fattibile, se è gratuita, come si descrive e che cosa succede quando il gruppo l'accetta; `FabbricaOfferte` la costruisce da un `TipoOfferta`. Quelle del modulo: `AiutoGratuito` e `AiutoMercenario` (un avversario si unisce al gruppo per un periodo limitato, gratis o in cambio di monete), `Incantesimi` (alcune pergamene di un incantesimo a caso a metà prezzo), `Informazioni` (qualche notizia gratuita: dove si trova un artefatto smarrito, una città o un castello), `MappaForesta` (a pagamento), `MappaZona` (gratuita: un quadrato di 15 caselle per lato) e `Pasto` (un pasto caldo che rende salute a tutti, o a volte non incontra i gusti del gruppo). Ogni classe di personaggio sa quali offerte può fare con l'amicizia e con la corruzione.

### Costanti ed eventi

`CostantiEconomia` ha i valori di bilanciamento: monete e preziosi iniziali, la contrattazione, il numero di oggetti dei magazzini, i prezzi delle pozioni (salute 5, salute grande 15, magia 10, magia grande 30), il pasto e il pernottamento (5), le mappe (10 e 20). Gli eventi: comandi, richieste e notifiche per l'acquisto, la vendita e la fusione (approvazione, rifiuto, avviso dell'incantatore), `RichiestaAperturaInventarioCommerciante`, `…Fornitore`, `RichiestaAperturaIncantatore` e `InternoPastoConsumatoInLocanda`.

## Osservazioni

- Il quadro dell'economia, con i listini e le regole, è in [`economia.md`](economia.md).
- La fusione vera sta in `foresta-artefatti` (`RegoleIncantatura`): qui ne sono solo il costo e la conferma.
- La contrattazione e il livello del mondo fanno salire i prezzi: ritarare l'economia vuol dire toccare `CostantiEconomia` e le costanti degli artefatti insieme.
