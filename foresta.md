# La Foresta

Panoramica del progetto: cos'è il gioco, come è fatto, dove trovare il resto della documentazione. Per un giudizio sullo stato del progetto vedi [`assessment.md`](assessment.md).

## Cos'è

**La Foresta** è un **gioco di ruolo single-player in italiano** per desktop, scritto in Java puro (Swing/AWT/Java2D, senza motori di gioco esterni). Si distribuisce come JAR eseguibile (`foresta-0.0.1-SNAPSHOT-jar-with-dependencies.jar`, classe main `com.threeamigos.foresta.Main`). È un progetto personale dell'autore ("La Foresta by Gundam of 3AM" nel `pom.xml`; il `README.md` dice *"My favourite game! (Italian language only)"*).

L'estetica richiama i fantasy a schermate fisse degli anni '80: un font bitmap fatto in casa (`DoomdarkFont`, omaggio a *Doomdark's Revenge* e *Lords of Midnight*), sprite, cornici pixel-perfect. Un gruppo di avventurieri esplora una foresta a caselle, entra nelle locazioni, combatte, raccoglie artefatti e incantesimi e affronta la missione principale, **sconfiggere il Drago**, prima che il tempo finisca. Il Drago ha quattro alleati nei loro castelli (Idra, Lich, Minotauro Gigante, Strega), e intorno alla storia principale ruotano molte missioni secondarie.

## Le meccaniche in breve

- **Il gruppo.** Il giocatore crea il capo scegliendo nome, sesso e classe: Guerriero/Guerriera, Ladro/Ladra, Bardo/Cantastorie, Elfo/Elfa, Mago/Maga. Il gruppo arriva a 5 personaggi permanenti, reclutati in città e locande, più fino a 3 temporanei (aiuti, mercenari, ospiti scortati da una missione). Se muore il capo, la partita è persa.
- **La mappa.** La foresta è una griglia di **dimensione variabile**, in fase di taratura (oggi 20×20, `Foresta.DIMENSIONE_X`/`DIMENSIONE_Y`), generata a ogni partita: quattro città, locande, templi, grotte, paludi, rovine, radure, boschi e i castelli dei boss. Le caselle si scoprono vedendole, sentendone parlare o grazie alle missioni.
- **Il tempo.** Muoversi e riposare fanno passare le ore. A 20, 25, 30 e 35 giorni il Drago distrugge una città; oltre il giorno 40 la partita è persa. Ci si può accampare per la notte.
- **Il combattimento.** A turni, un round al secondo: il personaggio scelto attacca e l'avversario risponde. Le probabilità di colpire e il danno derivano dagli attributi (tiro su 100, non "d20 contro classe armatura"), con 18 effetti di stato che interagiscono con i tipi di danno. Esistono anche duelli, combattimenti fino alla resa e ondate di avversari. In alternativa al combattimento si può corrompere, stringere amicizia, fuggire o passare inosservati.
- **La progressione.** Esperienza con curva quadratica, punti abilità da spendere sugli attributi, livello del mondo che fa crescere mostri, bottino e negozi.
- **Gli oggetti.** Artefatti generati proceduralmente con rarità, slot, incantamenti e set leggendari; ingredienti magici che l'incantatore fonde negli artefatti; dieci incantesimi da comprare come pergamene; pozioni.
- **I negozi.** Nelle città ci sono locanda, alchimista, armaiolo, venditore di pergamene e incantatore; nelle locande e negli incontri amichevoli si trovano offerte (pasti, informazioni, mappe, aiuti).
- **Le missioni.** Oltre alle cinque principali, una grande famiglia di incarichi "a passi" (mandanti, corrieri, scorte, indagini, rituali, leggende...) con domande al giocatore e ricompense.
- **Gli intermezzi.** Scene a pagine con sfondi, animazioni e battute a fumetto, che scattano in momenti precisi del gioco o alla fine di un passo di missione.
- **Notizie e trofei.** Uscendo da una locanda esce una notizia satirica sulle malefatte del gruppo, che compare nel notiziario della mappa. I trofei valgono da una partita all'altra.
- **Persistenza.** Cinque slot di salvataggio, classifica dei punteggi, trofei; tutto in file di testo in `~/.foresta/`.
- **Testi generati.** Nomi di locande, templi, rovine e artefatti, fiabe, oroscopi, notizie e testi delle missioni vengono da grammatiche generative (`GrammarBean`).

## Architettura

Il codice si divide in due parti, che si parlano attraverso un bus eventi publish/subscribe (`BusEventi`):

| Parte | Pacchetti | Documento |
| :--- | :--- | :--- |
| **Motore di gioco** | `motore`, `modellodati`, `tipi`, `eventi`, `personaggi`, `locazioni`, `missioni`, `intermezzi`, `oggetti`, `offerte`, `incantesimi`, `trofei`, `interfacce`, `tools` | [`motore_di_gioco.md`](motore_di_gioco.md) |
| **Motore grafico** | `ui`, `ui.sfx` | [`motore_grafico.md`](motore_grafico.md) |

```
        UI (ui)                                              Motore (Automa e il resto)
  ┌────────────────────────┐   Comando*  ──────────────▶  ┌─────────────────────────────┐
  │ ForestaUI              │                              │ Automa: macchina a stati     │
  │ DisplayableCanvas      │   ◀──────  Notifica*,        │ reattiva, senza game loop    │
  │ thread di ridisegno    │            Richiesta*,       │                              │
  │ a 60 FPS (solo repaint)│            Interno*          │ TemporizzatoreJ2SE: impulsi  │
  └───────────┬────────────┘                              │ TIMER pianificati fuori, ma  │
              │  legge direttamente lo stato di dominio    │ eseguiti sull'EDT            │
              └──────────▶ GruppoGiocatore, Foresta, ...   └─────────────────────────────┘

                 Tutta la logica di gioco e tutto il disegno girano sull'EDT di Swing.
```

- **Un solo thread per la logica.** `BusEventi` consegna gli eventi sull'Event Dispatch Thread, e anche gli impulsi del timer del motore vi arrivano. Il thread della UI si limita a chiedere un `repaint()` 60 volte al secondo quando c'è qualcosa da animare.
- **Il motore non conosce la UI**, salvo l'enum `InterfacciaUtente.Finestra` usato per portare in primo piano un riquadro.
- **La UI invece conosce il dominio.** Il bus separa i comandi, ma la UI legge direttamente `GruppoGiocatore`, `Foresta`, `Notizie` e altri oggetti del motore, e chiama gli `Automa*` dei negozi che le arrivano con gli eventi. Separare il modello dati dal motore è fra le cose da valutare ([`todo.md`](todo.md)).

## Avvio

`Main.main()`:

1. registra sul bus i componenti che ascoltano tutta la partita: `SnifferBusEventi` (log di ogni evento), `Notizie`, `Statistiche`, `RegistroTrofei`, `RegistroArtefatti`, `RegistroMissioni`;
2. legge gli argomenti: `ORIZZONTALE`/`VERTICALE`, `TUTTOSCHERMO`, `SALTALOGO`, `BARRACLASSICA` (barra icone fissa invece del Dock), `MODALITA_DI_PROVA`;
3. installa i gestori su file di classifica, salvataggi e trofei (i trofei si leggono qui, una volta sola);
4. crea l'`Automa` con il suo `Temporizzatore` e si iscrive a `InternoInterfacciaUtentePronta`;
5. crea `ForestaUI`. Quando la finestra è pronta, l'evento arriva e l'automa parte con `inizia()`: mentre la UI traccia il logo, il motore precarica grammatiche e generatore di artefatti.

## Stack e build

- **Java 8** (`maven.compiler.source/target = 1.8`), build **Maven**, sorgenti in UTF-8.
- **Swing/AWT/Java2D** per tutta la grafica, nessuna libreria esterna. Nessuna dipendenza di runtime.
- **JUnit 5** per i test e **JaCoCo** per la copertura. `mvn test` esegue la suite: al 2026-10-05 sono 681 test, tutti verdi (8 saltati). I test di scenario guidano partite intere senza UI (`PartitaDiTest`).
- I salvataggi usano un formato testo proprietario a campi separati da `|` (`Serializzabile`), senza versione: la compatibilità con i salvataggi vecchi non è un obiettivo.
- Circa 520 file e 75.000 righe di codice, più circa 20.000 righe di test.

## Mappa dei documenti

| Documento | Contenuto |
| :--- | :--- |
| [`motore_di_gioco.md`](motore_di_gioco.md) | Macchina a stati, bus eventi, modello dati e salvataggi, mondo, combattimento, personaggi, oggetti, incantesimi, testi generati, strumenti di test |
| [`motore_grafico.md`](motore_grafico.md) | Finestra, canvas e riquadri, barra icone, ciclo di rendering, sprite, font bitmap, schermate, effetti |
| [`gestione_missioni.md`](gestione_missioni.md) | Infrastruttura delle missioni: registro, momenti di controllo, `Passo` e `MissioneAPassi`, claim delle locazioni |
| [`passi_missioni.md`](passi_missioni.md) | Catalogo dei passi e delle missioni concrete, mappatura dei tipi di missione |
| [`intermezzi.md`](intermezzi.md) | Come scrivere, animare e provare un intermezzo |
| [`artefatti_e_incantamenti.md`](artefatti_e_incantamenti.md) | Artefatti, pergamene, incantatore, bilanciamento del combattimento (con il piano di lavoro) |
| [`economia.md`](economia.md) | Bilancio dell'economia del gioco: entrate, uscite, prezzi per livello, cosa non torna. **Il riequilibrio è ancora da affrontare** |
| [`interazioni_effetti_di_stato.md`](interazioni_effetti_di_stato.md) | Interazioni fra effetti di stato e tipi di danno |
| [`GrammarBean.md`](GrammarBean.md) | Manuale e assessment del motore di grammatiche |
| [`assessment.md`](assessment.md) | Valutazione dello stato del progetto e prossimi passi |
| [`todo.md`](todo.md) | Cose da fare generali e bug noti |
| [`revisione_documentazione.md`](revisione_documentazione.md) | Piano e avanzamento della revisione della documentazione |
| `tipiDanno.md`, `tipiPersonaggio.md` | Appunti generici sul genere fantasy (tipi di danno, ruoli dei personaggi), non descrivono il codice |
