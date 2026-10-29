# 10-foresta-modellodati

Il **contenitore radice dei dati di una partita**: `ModelloDati`, che mette insieme i dati di tutte le parti per salvarli e rileggerli, e nient'altro: ogni dato ha il suo modulo.

Ogni parte del gioco porta i *suoi* dati (`ArtefattoMD` negli artefatti, `ForestaMD` nella mappa, `PersonaggioMD` nei personaggi, `RegistroMissioniMD` nelle missioni…); questo modulo non ha nessuno di quei dati: li conosce tutti perché è l'unico che deve **scriverli uno dopo l'altro** in un file e **rileggerli**. È anche l'unico che sa dire alle parti dove stanno i loro dati.

## Dipendenze

**Dichiarate nel pom:** `09-foresta-missioni`, `07-foresta-mondo`, `07-foresta-intermezzi`, `05-foresta-personaggi`, `08-foresta-tattiche`, `00-foresta-serializzazione`, `02-foresta-notizie`, `02-foresta-mappa`.

**Lo dichiarano:** `11-foresta-motore-api`.

Le dipendenze sono transitive: un modulo vede anche quello che vedono i moduli da cui dipende, quindi il compilatore garantisce la separazione fra i moduli che non si raggiungono.

Dichiara le parti che hanno dati nel salvataggio: missioni, mondo, intermezzi, personaggi, tattiche, mappa, le notizie e la serializzazione; gli artefatti, l'economia (cassa e magazzini), i poteri (canti imparati) e le statistiche ci arrivano per transitività. Non conosce il motore, gli eventi né l'interfaccia.

## Funzionamento

### `ModelloDati`

Un'istanza è **una partita intera**: contiene i dati di

| Parte | Classe | Di quale modulo |
| :--- | :--- | :--- |
| il gruppo del giocatore con tutti i personaggi | `GruppoGiocatoreMD` | personaggi |
| monete e preziosi | `CassaMD` | economia |
| le tattiche dei personaggi | `TatticheMD` | tattiche |
| i canti imparati strada facendo | `CantiImparatiMD` | poteri |
| punti, esperienza, livello del mondo | `StatisticheMD` | statistiche |
| il tempo e il calendario | `LineaTemporaleMD` | statistiche |
| la mappa della Foresta | `ForestaMD` | mappa |
| i personaggi e gli artefatti sparsi per il mondo | `RegistroPersonaggiMD`, `RegistroArtefattiMD` | mondo |
| i magazzini dei negozi | `MagazziniMD` | economia |
| le missioni | `RegistroMissioniMD` | missioni |
| gli intermezzi già mostrati | `IntermezziMD` | intermezzi |
| le ultime notizie e i messaggi | `NotizieMD` | notizie |

più il flag dell'aiuto della barra delle icone. `salva(PrintWriter)` scrive le parti **una dopo l'altra, nell'ordine in cui le rilegge** `leggi(BufferedReader)`, che prima le azzera; `reimposta(dimensioneX, dimensioneY)` le azzera tutte per una partita nuova con una mappa di quelle dimensioni. I salvataggi non devono restare compatibili con le versioni precedenti, quindi cambiare l'ordine o il formato di una parte è lecito.

I **trofei** e il **bestiario** non sono qui: non appartengono a una partita ma passano dall'una all'altra, quindi hanno il loro modello e il loro file.

### `installa()`: dire alle parti dove stanno i loro dati

Le parti statiche (`Foresta`, `GruppoGiocatore`, `Statistiche`, `LineaTemporale`, `RegistroPersonaggi`, `RegistroArtefatti`, `RegistroMissioni`, `MagazziniDeiNegozi`, `Cassa`, `RegistroIntermezzi`, `TatticheDeiPersonaggi`, `CantiImparati`) **non conoscono il modello dati**: è `ModelloDati.installa()` a installare in ciascuna un fornitore (`setFornitoreDiDati(() -> getIstanza().getXxxMD())`). Il fornitore va chiamato a ogni uso, perché `ModelloDati.setIstanza(...)` sostituisce l'istanza quando si carica un salvataggio, e i fornitori puntano sempre a quella in corso.

L'installazione è **esplicita**: la chiama `Main` come prima cosa, e `PartitaDiTest` a ogni partita di prova; un test che usa una di quelle parti senza passare da lì la chiama lui. Chi usa una parte prima dell'installazione riceve un `IllegalStateException` esplicito («Nessuno ha detto alla mappa dove sono i suoi dati…»). Richiamarla non fa danni.

## Osservazioni

- Nel piano delle dipendenze è il modulo che deve ridursi: ogni nuova parte con dati propri aggiunge qui una riga in `ModelloDati`. Oggi qui non c'è più nessun dato: resta solo `ModelloDati`.
- Le collezioni che i dati mostrano sono di sola lettura (viste dell'elenco vero); per cambiarle si passa dai metodi che lo dicono (`aggiungi…`, `togli…`, `riordina…`).
- Il quadro dei salvataggi è in [`motore_di_gioco.md`](motore_di_gioco.md) §5.
