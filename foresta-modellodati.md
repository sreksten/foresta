# foresta-modellodati

Il **contenitore radice dei dati di una partita**: `ModelloDati`, che mette insieme i dati di tutte le parti per salvarli e rileggerli, e i dati delle notizie (`NotizieMD`, `NotiziaMD`, `MessaggioMD`: gli ultimi messaggi mostrati nel pannello di testo e le notizie per la mappa) che non hanno ancora un modulo loro.

Ogni parte del gioco porta i *suoi* dati (`ArtefattoMD` negli artefatti, `ForestaMD` nella mappa, `PersonaggioMD` nei personaggi, `RegistroMissioniMD` nelle missioni…); questo modulo non ha nessuno di quei dati: li conosce tutti perché è l'unico che deve **scriverli uno dopo l'altro** in un file e **rileggerli**. È anche l'unico che sa dire alle parti dove stanno i loro dati.

## Dipendenze

**Dichiarate nel pom:** `foresta-missioni`, `foresta-mondo`, `foresta-intermezzi`, `foresta-personaggi`, `foresta-tattiche`, `foresta-serializzazione`, `foresta-artefatti`, `foresta-mappa`.

**Lo dichiarano:** `foresta-motore-api`.

Le dipendenze sono transitive: un modulo vede anche quello che vedono i moduli da cui dipende, quindi il compilatore garantisce la separazione fra i moduli che non si raggiungono.

Dichiara le parti che hanno dati nel salvataggio: missioni, mondo, intermezzi, personaggi, tattiche, artefatti, mappa e la serializzazione; l'economia (cassa e magazzini), i poteri (canti imparati) e le statistiche ci arrivano per transitività. Non conosce il motore, gli eventi né l'interfaccia.

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
| le ultime notizie e i messaggi | `NotizieMD` | questo modulo |

più il flag dell'aiuto della barra delle icone. `salva(PrintWriter)` scrive le parti **una dopo l'altra, nell'ordine in cui le rilegge** `leggi(BufferedReader)`, che prima le azzera; `reimposta(dimensioneX, dimensioneY)` le azzera tutte per una partita nuova con una mappa di quelle dimensioni. I salvataggi non devono restare compatibili con le versioni precedenti, quindi cambiare l'ordine o il formato di una parte è lecito.

I **trofei** e il **bestiario** non sono qui: non appartengono a una partita ma passano dall'una all'altra, quindi hanno il loro modello e il loro file.

### Il blocco statico: dire alle parti dove stanno i loro dati

Le parti statiche (`Foresta`, `GruppoGiocatore`, `Statistiche`, `LineaTemporale`, `RegistroPersonaggi`, `RegistroArtefatti`, `RegistroMissioni`, `MagazziniDeiNegozi`, `Cassa`, `RegistroIntermezzi`, `TatticheDeiPersonaggi`, `CantiImparati`) **non conoscono il modello dati**: è lui a installare in ciascuna un fornitore (`setFornitoreDiDati(() -> getIstanza().getXxxMD())`) nel suo blocco statico. Il fornitore va chiamato a ogni uso, perché `ModelloDati.setIstanza(...)` sostituisce l'istanza quando si carica un salvataggio, e i fornitori puntano sempre a quella in corso.

La conseguenza è che il blocco statico gira **quando la classe `ModelloDati` viene caricata**: chi usa una di quelle parti prima di aver toccato `ModelloDati` riceve un `IllegalStateException` esplicito («Nessuno ha detto alla mappa dove sono i suoi dati…»). Nel gioco non succede, perché `StatoDelMotore` carica subito il modello dati; è già successo in un test che girava in una JVM a parte.

### Le notizie

`NotizieMD` tiene gli ultimi messaggi del pannello di testo (per ripopolarlo dopo un ricaricamento, con la stessa impaginazione: `MessaggioMD` ricorda se un messaggio era l'inizio di un paragrafo o la continuazione di una frase) e le ultime notizie per la schermata della mappa (`NotiziaMD`). La logica che le aggiunge e scarta le più vecchie (`Notizie`) sta nel motore; qui solo i dati.

## Osservazioni

- Nel piano delle dipendenze è il modulo che deve ridursi: ogni nuova parte con dati propri aggiunge qui una riga in `ModelloDati`; le notizie sono l'ultimo dato rimasto qui senza un modulo loro. Per `assessment.md` conviene sostituire il blocco statico con un'installazione esplicita (`ModelloDati.installa()`, chiamata da `Main` e dai test).
- Le collezioni che i dati mostrano sono di sola lettura (viste dell'elenco vero); per cambiarle si passa dai metodi che lo dicono (`aggiungi…`, `togli…`, `riordina…`).
- Il quadro dei salvataggi è in [`motore_di_gioco.md`](motore_di_gioco.md) §5.
