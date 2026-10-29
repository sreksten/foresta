# 01-foresta-eventi

Il **bus degli eventi** con cui motore e interfaccia grafica si parlano senza conoscersi, la classe base di tutti gli eventi, l'elenco dei loro tipi (`TipoEvento`) e le poche notifiche di testo e gli eventi tecnici che non portano dati del dominio.

Esiste come modulo a parte perché *ogni* altro modulo che ha qualcosa da dire al giocatore (o da chiedergli) deve poter definire i propri eventi senza dipendere dal motore né dalla UI. Qui sta solo il meccanismo; le classi dei singoli eventi stanno **dove stanno i dati che portano**: la mappa pubblica `InternoLocazioneVisitata`, l'economia le notifiche di monete, i personaggi quelle sul gruppo, e così via.

## Dipendenze

**Dichiarate nel pom:** `00-foresta-strumenti`.

**Lo dichiarano:** `02-foresta-comandi`, `02-foresta-mappa`, `05-foresta-personaggi`, `02-foresta-notizie`, `11-foresta-motore-api`.

Le dipendenze sono transitive: un modulo vede anche quello che vedono i moduli da cui dipende, quindi il compilatore garantisce la separazione fra i moduli che non si raggiungono.

Dipende da `00-foresta-strumenti` per due cose: `ThreadPrincipale` (la consegna degli eventi avviene sul thread principale, senza conoscere Swing) e `Logger` (gli errori degli ascoltatori finiscono nel registro). Non conosce il dominio: per questo le sue notifiche portano solo testo.

## Funzionamento

### `BusEventi`: il publish/subscribe

È una classe di soli metodi statici, **sincrona e per classe esatta**:

- `iscriviti(Classe, gestore)` registra un ascoltatore per gli eventi *di quella classe* (non delle sue sottoclassi: `evento.getClass()` è la chiave) e restituisce un `Runnable` per togliere l'iscrizione, comodo con le lambda; `cancellati` fa lo stesso a chi tiene il riferimento al gestore.
- `pubblica(evento)` consegna l'evento a tutti gli iscritti al suo tipo. Senza iscritti non succede niente.
- **La consegna è sul thread principale** (`ThreadPrincipale`): subito se si pubblica già da lì, altrimenti accodata. Con Swing è l'Event Dispatch Thread, quindi tutta la logica gira su un thread solo e non servono sincronizzazioni. I test sostituiscono la consegna con `impostaConsegna(Runnable::run)` per riceverla subito sul proprio thread, e ripuliscono con `azzera()`.
- **Un ascoltatore che fallisce non blocca gli altri.** `consegnaA` prende ogni `RuntimeException`/`Error` (non quelli della macchina virtuale, come la memoria finita), li scrive nel log e li ripubblica come `InternoException`, così la UI può dirlo al giocatore. Se a fallire è proprio un ascoltatore di `InternoException` non si ripubblica, per non andare in ciclo.

### `EventoBase` e `TipoEvento`

Ogni evento estende `EventoBase`, che porta un `TipoEvento` e un `uuid` generato solo alla prima richiesta (gli eventi sono centinaia a ogni locazione e quasi nessuno lo chiede).

`TipoEvento` elenca **tutti** gli eventi del gioco, anche quelli definiti in altri moduli (129 costanti), divisi in quattro famiglie:

| Famiglia | Direzione | Significato | Costanti |
| :--- | :--- | :--- | ---: |
| `COMANDO_…` | UI → motore | il giocatore ha scelto qualcosa | 9 |
| `NOTIFICA_…` | motore → UI | è successo qualcosa (un fatto compiuto) | 45 |
| `RICHIESTA_…` | motore → UI | mostra una schermata o chiedi un input | 16 |
| `INTERNO_…` | tecnici | coordinamento fra le parti | 59 |

La regola, controllata da `CostantiDegliEventiTest` in `13-foresta-app-swing`, è che ogni classe di evento usi la costante col suo nome. Gli eventi non portano classi del dominio, ma viste in sola lettura, identificativi e dati: così la UI non può modificare lo stato se non con un comando.

### Gli eventi che stanno qui

Solo quelli che non hanno bisogno di nessun dato del dominio:

- **`notifiche`**: `NotificaTestoFrase` e `NotificaTestoParagrafo` (la narrazione del gioco, una frase o un paragrafo) e `NotificaGlobale` (un messaggio più importante degli altri).
- **`interni`**: `InternoException`, `InternoErrore`, `InternoMessaggio` (errori e messaggi per il log); `InternoMostraFinestraStato` e `InternoMostraFinestraIncantesimiEPozioni` (il motore chiede alla UI di portare in primo piano un riquadro, senza conoscerlo); `InternoRichiestaRefreshUI`; e quelli del ciclo di una locazione, `InternoPreparazioneLocazione`, `InternoFineLocazione` e `InternoPassaggioInosservato`.

Il pacchetto `tipi` di questo modulo contiene solo `TipoEvento`. Il significato di ciascuna famiglia nel flusso del gioco è in [`motore_di_gioco.md`](motore_di_gioco.md) §4.

## Osservazioni

- Il bus è globale e statico: i test devono azzerarlo (`BusEventi.azzera()`) a ogni partita ricostruita.
- Iscriversi a una superclasse non intercetta le sottoclassi. È una scelta voluta (la ricerca è una lettura di mappa), ma va ricordata quando si aggiunge una gerarchia di eventi.
- `TipoEvento` è un punto di contatto comune a tutti i moduli: aggiungere un evento in un modulo qualsiasi vuol dire toccare questo enum.
