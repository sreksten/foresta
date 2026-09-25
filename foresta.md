# La Foresta

> Assessment tecnico — panoramica del progetto

## Cos'è

**La Foresta** (`com.threeamigos.foresta`, gruppo Maven `com.threeamigos`, `artifactId foresta`) è un **gioco di ruolo single-player in italiano**, sviluppato in Java puro (Swing/AWT/Java2D, nessun motore di gioco esterno). È un'applicazione desktop distribuita come JAR eseguibile (`foresta-0.0.1-SNAPSHOT-jar-with-dependencies.jar`, classe main `com.threeamigos.foresta.Main`).

Esteticamente richiama i fantasy retro a schermate fisse degli anni '80 (font bitmap pixel-based fatti in casa, nome della classe `DoomdarkFont` — un chiaro omaggio a *Doomdark's Revenge*/*Lords of Midnight*): un party di avventurieri esplora una foresta a caselle, entra in locazioni (radure, grotte, città, castelli), combatte mostri, accumula oggetti e incantesimi, e affronta una missione principale (sconfiggere un Drago) più una serie di missioni secondarie e boss opzionali (Idra, Lich, Minotauro Gigante, Strega).

Il file `README.md` del repository lo riassume così: *"My favourite game! (Italian language only)"* — è un progetto personale dell'autore (Stefano Reksten / "Gundam of 3AM", vedi `pom.xml`).

## Genere e meccaniche in breve

- **Party-based**: il giocatore controlla un `GruppoGiocatore` di personaggi (classe scelta all'inizio: Guerriero/a, Ladro/a, Bardo/Cantastorie, Elfo/a, Mago/a), contro un `GruppoAvversario` generato di volta in volta.
- **Combattimento a turni**, con selezione round-robin del personaggio attivo e calcolo di colpito/danno basato su statistiche (non dadi GDR classici: percentuali derivate da attributi + effetti di stato).
- **Esplorazione su mappa**: una Foresta a griglia 50×50 (`Foresta.java`), con locazioni di vario tipo (Bosco, Grotta, Palude, Radura, Rovine, Tempio, Città, Locanda, Castelli dei boss) generate/gestite dinamicamente.
- **Economia**: armaiolo, alchimista/fornitore, scambio di artefatti, offerte nelle locande (aiuto mercenario/gratuito, incantesimi, informazioni, mappe, pasti).
- **Progressione**: esperienza, livelli, incantesimi elementali (Aria/Acqua/Terra/Fuoco...), effetti di stato (stordito, confuso, congelato...).
- **Persistenza**: salvataggi su file locali (5 slot), classifica punteggi.
- **Generazione procedurale di testo**: fiabe, oroscopi, descrizioni di locande e persino JSON di artefatti sono generati da un motore di grammatiche testuali proprietario (`GrammarBean`).

## Architettura in due parti

Il codice si divide nettamente in due sottosistemi, indipendenti a livello di thread e accoppiati **solo** tramite un bus eventi pubblica/sottoscrivi (`BusEventi`):

| Parte | Package principali | Documento |
| :--- | :--- | :--- |
| **Motore di gioco** | `motore`, `motore.modellodati`, `eventi`, `personaggi`, `locazioni`, `missioni`, `oggetti`, `offerte`, `tools` | [`motore_di_gioco.md`](motore_di_gioco.md) |
| **Motore grafico** | `ui`, `ui.sfx` | [`motore_grafico.md`](motore_grafico.md) |

```
                         BusEventi (pub/sub sincrono, sull'EDT Swing)
                    ┌───────────────────────────────────────────────┐
                    │                                                │
   Comando*/Notifica*/Richiesta*/Interno*                Notifica*/Richiesta*/Interno*
                    │                                                │
                    ▼                                                ▼
   ┌────────────────────────────┐               ┌──────────────────────────────────┐
   │   MOTORE DI GIOCO           │               │   MOTORE GRAFICO                 │
   │   Automa (state machine)    │               │   ForestaUI + DisplayableCanvas  │
   │   thread proprio            │               │   JFrame Swing, thread daemon    │
   │   (Temporizzatore + EDT)    │               │   a 30 FPS (repaint)             │
   └────────────────────────────┘               └──────────────────────────────────┘
```

Nessuno dei due sottosistemi ha un riferimento diretto all'altro: il motore non conosce `DisplayableCanvas`, e la UI non conosce `Automa`. Tutta la comunicazione passa per classi di eventi immutabili organizzate in quattro famiglie (`eventi.comandigiocatore`, `eventi.notifiche`, `eventi.richieste`, `eventi.interni`) — dettagliate in `motore_di_gioco.md`.

## Bootstrap (avvio dell'applicazione)

`Main.main()` (`src/main/java/com/threeamigos/foresta/Main.java:29-47`):

1. Registra `SnifferBusEventi`, un logger che si iscrive a *tutti* gli eventi pubblicati (utile per debug).
2. Legge gli argomenti da riga di comando (`ORIZZONTALE`/`VERTICALE`/`TUTTOSCHERMO`, e `SALTALOGO` per non mostrare il logo iniziale nelle partite di prova).
3. Crea `Automa` (il motore, `ControlloreDiGioco`) con il proprio `Temporizzatore`.
4. Crea `ForestaUI` (la UI) con un secondo `Temporizzatore` indipendente.
5. Si iscrive all'evento `InternoInterfacciaUtentePronta`, pubblicato da `ForestaUI` a fine setup della finestra Swing: solo a quel punto il motore riceve `inizia()` e la state machine entra nello stato `INTRO`.

Questo disaccoppiamento all'avvio è lo stesso pattern che regge tutto il resto del gioco: la UI non aspetta il motore, il motore non aspetta la UI, si sincronizzano solo via eventi.

## Stack tecnico

- **Java 8** (`maven.compiler.source/target = 1.8`), build **Maven** (`pom.xml`).
- **Swing/AWT/Java2D** per tutta la UI e il rendering — nessuna libreria grafica esterna (no LWJGL, no JavaFX).
- Unica dipendenza runtime dichiarata: **Gson 2.9.0** — dichiarata nel `pom.xml` ma **non risulta importata in nessun punto del codice sorgente attuale** (grep su `com.google.gson` non produce risultati). La documentazione interna di `GrammarBean.md` §5.3 descrive in dettaglio una pipeline `artefatti.txt` → JSON → Gson → `CostruttoreArtefatto` come caso di studio, ma né `artefatti.txt` né Gson sono mai referenziati da codice `.java`: è un esempio rimasto sulla carta, non una feature attiva (dettagli in [`motore_di_gioco.md`](motore_di_gioco.md) §9) — un punto da chiarire con l'autore in un eventuale refactoring.
- **JUnit 5** per i test (`src/test`).
- **JaCoCo** per la code coverage, integrato nella build Maven.
- Persistenza dei salvataggi in un **formato testo proprietario, pipe-delimited** (non JSON), tramite l'interfaccia `Serializzabile`.

## Per approfondire

- [`motore_di_gioco.md`](motore_di_gioco.md) — state machine, bus eventi, modello dati, combattimento, personaggi, mondo, missioni, economia, persistenza, generazione procedurale di testo.
- [`motore_grafico.md`](motore_grafico.md) — finestra Swing, sistema di canvas componibili, sprite, font bitmap custom, effetti speciali, cache immagini.
