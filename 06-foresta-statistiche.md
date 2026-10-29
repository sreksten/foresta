# 06-foresta-statistiche

Il **mondo come numeri e come calendario**: le `Statistiche` (punti, esperienza e livello del mondo, mostri uccisi per classe, turni giocati), la `LineaTemporale` (l'ora, il giorno, il Drago che distrugge una città dopo l'altra, la fine della partita per scadenza del tempo) ed `EsperienzaDelGruppo` (quanta esperienza vale un fatto del gioco).

È un modulo a sé perché «a che punto è la partita» è una domanda a cui rispondono molte parti (gli artefatti nascono al livello del mondo, i mostri crescono con lui, le missioni chiedono l'ora, la classifica usa il punteggio) ma che non appartiene a nessuna: non è un attributo del personaggio né una proprietà della mappa. Il **livello del mondo** è anche il modo in cui moduli che «non sanno che esiste un mondo», come gli artefatti, ricevono un livello: da un parametro.

## Dipendenze

**Dichiarate nel pom:** `05-foresta-personaggi`, `02-foresta-mappa`.

**Solo per i test:** `05-foresta-personaggi`.

**Lo dichiarano:** `07-foresta-intermezzi`, `07-foresta-mondo`.

Le dipendenze sono transitive: un modulo vede anche quello che vedono i moduli da cui dipende, quindi il compilatore garantisce la separazione fra i moduli che non si raggiungono.

- `05-foresta-personaggi`: l'esperienza del mondo segue la curva di `GestoreProgressione`, le statistiche contano i mostri uccisi per `TipoPersonaggio` e ascoltano gli eventi sugli avversari sconfitti e i punti esperienza assegnati; la linea temporale parla al `GruppoGiocatore`.
- `02-foresta-mappa`: quando il Drago distrugge una città, la linea temporale la trasforma sulla mappa (`Foresta.distruggiLocazioneUnica`) e racconta in che direzione si vede il fumo (`Direzioni`).

## Funzionamento

### `Statistiche`

Una classe statica sul dato `StatisticheMD` (livello del gioco, punti esperienza, punteggio, mostri uccisi per classe, turni giocati, e l'**identificativo della partita**, che resta lo stesso attraverso salvataggi e caricamenti, così la classifica non registra due volte la stessa partita). I dati li prende da chi li custodisce (`setFornitoreDiDati`, installato da `ModelloDati`).

- `registrati()` iscrive le statistiche agli eventi: `InternoAvversarioSconfitto` conta un mostro ucciso, `InternoPuntiEsperienzaAssegnati` aggiunge esperienza.
- `addPunti` aggiorna il punteggio e lo annuncia (`NotificaVariazionePunteggio`); `addPuntiEsperienza` aggiorna l'esperienza (`NotificaVariazionePuntiEsperienzaPersonaggio`) e, se la nuova esperienza porta al livello successivo secondo la curva, **sale il livello del mondo** (`NotificaAumentoLivelloMondo`).
- `getLivello`, `getPuntiEsperienzaPerProssimoLivello`, `getMostriUccisi(classe)`, `incrementaTurniGiocati`, `getIdPartita`, `reimposta`.
- `impostaLivelloDelMondo(livello)` serve alla modalità di prova: porta livello ed esperienza a quel valore, e annuncia solo gli aumenti.

### `EsperienzaDelGruppo`

Quanto vale un fatto del gioco, **in percentuale dei punti che separano il livello del mondo dal successivo**: completare una missione secondaria dà il 20%, una principale il 50%, acquisire un artefatto minore il 5%. L'esperienza va al gruppo del giocatore (`addPuntiEsperienza`), che la distribuisce ai personaggi.

### `LineaTemporale`

Il tempo ha una **scadenza**. Il dato è `LineaTemporaleMD` (giorno e ora correnti, se il gioco è finito, l'ultimo evento non ancora mostrato). La classe statica:

- fa avanzare il tempo (`aggiungiOre`, anche più di un giorno alla volta, `mattinoSeguente`, `oreFinoAlMattino`) e descrive l'ora del giorno (`getDescrizioneOraDelGiorno`);
- applica gli **eventi del calendario** (`eventi(gruppo)`, e `eventiDiNotte(gruppo)` per l'alba dopo una notte): dal giorno **20** il Drago distrugge una città ogni cinque giorni (Ruuna al 20, Nyena al 25, Fleena al 30, Malgaard al 35) e oltre il giorno **40** il gioco è perduto (`isGiocoFinito`). La distruzione trasforma la città in rovine sulla mappa e viene raccontata con una colonna di fumo nella direzione giusta; se il gruppo è proprio in quella città, o ci ha dormito, il testo cambia (scampa per un pelo, o trova solo le rovine);
- tiene l'ultimo evento da mostrare nel dato, così un salvataggio fatto prima di mostrarlo non lo perde; `getEvento()` lo consuma;
- `setDragoSconfitto` chiude la partita con la vittoria.

### I contratti

`NotificaAumentoLivelloMondo`, `NotificaVariazionePunteggio` e `NotificaVariazionePuntiEsperienzaPersonaggio`: la UI li ascolta per aggiornare riquadro di stato e punteggio.

## Osservazioni

- Per alzare il limite dei 40 giorni vanno riviste insieme le soglie delle città, la soglia della sconfitta e `Numeri.getOrdinaleM/F`, come spiegato in `todo.md`. Il TODO nel codice nota che le città potrebbero essere distrutte a caso invece che in ordine fisso.
- Come `Foresta`, `Statistiche` e `LineaTemporale` sono statici con dati iniettati: chi li usa nei test installa il fornitore (lo fa `ModelloDati.installa()`).
- Il quadro del tempo e delle statistiche è in [`motore_di_gioco.md`](motore_di_gioco.md) §6.
