# Portabilità verso Android e iOS: cosa manca

Stato al 2026-10-17. Riguarda `foresta-tipi`, `foresta-modellodati`, `foresta-contratti` e le parti di `foresta-motore` che toccano disco, thread, tempo e testi; `foresta-swing` è stata guardata solo dove il motore la incontra.

Qui c'è soltanto quello che resta da fare. Quello che è già stato sistemato non è elencato: la motivazione di ogni cambiamento sta nel javadoc o in un commento del codice che lo riguarda, e la storia in git. I punti di aggancio per una piattaforma (ciclo di vita dell'app, uscita dal gioco, cartella dei file, destinazione del log, lingua dei testi) sono descritti nei javadoc di `ControlloreDiGioco`, `NotificaUscitaDalGioco`, `GestoreSuFile`, `DestinazioneDiLog` e `Testi`, e in [`motore_di_gioco.md`](motore_di_gioco.md) (§5, "Ciclo di vita dell'app").

## Portabilità

### Testi italiani nel codice

Il gioco è scritto in italiano. Sono già in un file per lingua, letti da `Testi` (con ripiego sull'italiano): le note dei moltiplicatori di classe, le descrizioni dei comandi, i motivi di rifiuto dell'equipaggiamento e i nomi e le descrizioni di tutte le enum di `foresta-tipi`. Restano nel codice:

- `FUGA_IGNOMINIA_NOTA` in `Costanti`: finisce nel salvataggio come nota di un modificatore, quindi spostarla vuol dire anche decidere come si confronta e come si salva;
- le enum fuori da `foresta-tipi` con testi da mostrare: `NegozioInScena` (singolare e plurale), e le frasi di `Mandante` (mescolate a identificativi: vanno separate) e, nello strumento di sviluppo, `DovePartono` e `ContestoLancio`; `Testi.testo(costante, voce)` funziona per qualunque enum, con il file accanto all'enum nel suo modulo;
- `Articoli` e `Numeri` (in `foresta-testi`): articoli, preposizioni, cardinali e ordinali in lettere; `Misc` (in `foresta-contratti`): le direzioni e i testi di storia, sconfitta e vittoria (vedi `todo.md`);
- il resto dei testi del motore e delle schermate (messaggi, fumetti, intermezzi, grammatiche `.txt`).

Prima di spostarli va deciso se il gioco avrà una versione inglese (per gli store). Se non l'avrà, i testi possono restare dove sono.

### Costanti di bilanciamento come dati

`Costanti` ha ancora circa 1350 costanti, in gran parte tabelle per classe (`GOBLIN_MAX_FORZA`, `GOBLIN_MOLTIPLICATORE_CRITICO`...). Metterle in una risorsa (properties o JSON) permetterebbe di bilanciare senza ricompilare. C'è un motivo in più: i `static final` primitivi vengono copiati nelle classi che li usano, quindi una compilazione incrementale parziale può tenere valori vecchi. È il lavoro più grande dell'elenco e ha senso solo se serve davvero il bilanciamento senza ricompilare.

### Ciclo di vita dell'app

- I singleton restano statici (`ModelloDati.getIstanza()` è usato in 64 file del motore). Una piattaforma che chiude l'app in background perde lo stato statico: `StatoDelMotore.azzera()` (che chiude l'automa e stacca il vecchio gruppo dal bus: poi se ne crea uno nuovo) e il salvataggio automatico lo coprono, ma non lo eliminano.
- Il salvataggio automatico si scrive solo quando l'app va in background in un punto sicuro (fra una locazione e l'altra). Un'app chiusa dal sistema a metà di una locazione riparte dall'ultimo salvataggio automatico, cioè dall'ultima volta che è andata in background in un punto sicuro (o da niente, e un salvataggio automatico di una sessione precedente resta). Scriverlo a ogni ingresso in `ATTESA_DIREZIONE` ridurrebbe la perdita, al costo di una scrittura per locazione.
- La UI Swing non chiama `inBackground()` e `inPrimoPiano()` (sul desktop non servono). La piattaforma mobile deve chiamarli dal suo ciclo di vita, e sospendere anche il temporizzatore della UI (`temporizzatoreUI` in `Main`).

### Uscite dal processo nella UI desktop

Sono della UI Swing e si sostituiscono con lei:

- `BufferedImageBuilder` pubblica `NotificaUscitaDalGioco(1, …)` e lancia un'eccezione se manca una risorsa grafica di base (fail fast, vedi `motore_grafico.md`); come si esce lo decide la piattaforma;
- la finestra si chiude con la X (`EXIT_ON_CLOSE`) senza passare dalla conferma di uscita né dal salvataggio.

Una UI mobile userà `NotificaUscitaDalGioco` per l'errore fatale e il ciclo di vita dell'app al posto del gancio.

## Osservazioni

- Il grosso del porting resta la UI Swing, da riscrivere, e l'input. I `Comando` astratti aiutano, ma i cartigli al passaggio del mouse e le scorciatoie da tastiera vanno ripensati per il tocco.
- Per un gioco 2D che resta in Java la scelta più naturale è **libGDX**: un solo codice di UI per desktop, Android e iOS (tramite MobiVM), che può usare direttamente i jar di motore e contratti. L'alternativa è J2ObjC per il motore, con una UI nativa separata per ciascuna piattaforma.

## Da fare / idee aperte

- Esaminare `foresta-swing` (dimensioni dello schermo, input, immagini) e la logica del motore con lo stesso criterio di questo documento.
- Il costo dei `InternoMessaggio` costruiti a ogni passo della macchina a stati è in [`todo.md`](todo.md).
