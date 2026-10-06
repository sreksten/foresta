# Foresta

Gioco di ruolo a turni in Java 8 + Swing, interamente in italiano (codice, commenti, documentazione).
Sorgenti in `src/main/java/com/threeamigos/foresta` (abbreviato `…/` sotto), risorse in
`src/main/resources/com/threeamigos/foresta`. Tutti i file sono UTF-8.

## Dove trovare le cose

| Argomento | Codice | Documento |
| :--- | :--- | :--- |
| Avvio, macchina a stati, ciclo di una locazione | `Main`, `…/motore/Automa` (implementa `…/interfacce/ControlloreDiGioco`), `…/motore/Stato` | `motore_di_gioco.md` §2–3 |
| Bus eventi | `…/eventi` (`notifiche`, `richieste`, `interni`, `comandigiocatore`) | `motore_di_gioco.md` §4 |
| Salvataggi e modello dati | `…/modellodati`, `…/interfacce/GestoreSalvataggi`, `…/strumenti/GestoreSalvataggiSuFile`, `…/motore/RiletturaPartita` | `motore_di_gioco.md` §5 |
| Mappa, tempo, locazioni | `…/motore/Foresta`, `LineaTemporale`, `…/locazioni` | `motore_di_gioco.md` §6 |
| Combattimento | `…/motore/CalcolatoreCombattimento`, `Ondata`, `…/modellodati/EffettoDiStatoMD` | `motore_di_gioco.md` §7, `interazioni_effetti_di_stato.md` |
| Personaggi e gruppi | `…/personaggi`, `…/motore/Gruppo*`, `GestoreProgressione` | `motore_di_gioco.md` §8 |
| Oggetti, artefatti, incantatore | `…/oggetti`, `…/motore/Automa{Inventario,AcquistiArtefatti,Incantatore,ScambiatoreArtefatti}`, `Regole*` | `artefatti_e_incantamenti.md` |
| Negozi ed economia | `…/offerte`, `…/motore/RegoleContrattazione`, `OfferteAlchimista` | `economia.md` |
| Missioni (infrastruttura) | `…/missioni/MissioneAPassi`, `Passo`, `…/motore/RegistroMissioni` | `gestione_missioni.md` |
| Missioni (catalogo) | `…/missioni/*` | `passi_missioni.md` |
| Intermezzi | `…/intermezzi`, `…/motore/RegistroIntermezzi` | `intermezzi.md` |
| Testi generati, grammatiche | `…/motore/GrammarBean`, `ProduttoreDiTestiCasuale`, risorse `motore/*.txt` | `GrammarBean.md` |
| Trofei | `…/trofei`, `…/motore/RegistroTrofei` | `motore_di_gioco.md` §10 |

## Schermate (tutte in `…/ui`)

Il canvas principale è `DisplayableCanvas` (enum interno `StatoDisplayableCanvas` per le schermate a tutto schermo).
Lo stato della partita la UI lo legge da `…/interfacce/VistaPartita` (sola lettura, implementata da `…/motore/VistaPartitaMotore`), mai dai singleton del motore; verso il motore manda solo eventi.
Panoramica in `motore_grafico.md`.

- Inventario del personaggio e del gruppo: `DisplayableCanvasInventario`; logica in `…/motore/AutomaInventario`, che la UI vede come `…/interfacce/VistaScambio` e comanda con `ComandoScambioArtefatto`; apertura con `…/eventi/richieste/RichiestaAperturaInventarioGruppo`.
- Armaiolo e venditore di pergamene: `DisplayableCanvasCommerciante`; alchimista: `DisplayableCanvasScambiatoreConsumabili`.
- Inventario, commerciante e incantatore estendono `DisplayableCanvasScambiatoreArtefatti` (astratta, due liste di oggetti affiancate), che estende `DisplayableCanvasScambiatore`: un comportamento comune alle tre schermate va lì.
- Incantatore: `DisplayableCanvasIncantatore` (+ `…/motore/AutomaIncantatore`, `BancoDiLavoro`).
- Trofei: `DisplayableCanvasTrofei`. Intermezzi: `DisplayableCanvasIntermezzo`, `AnimazioniIntermezzo`.
- Intro, slot di salvataggio, fine partita, statistiche, punteggi: `DisplayableCanvasIntroOutro`.
- Mappa a tutto schermo e notiziario: `DisplayableCanvasMappaATuttoSchermo`, `DisegnatoreMappa`, `Notiziario`.
- Riquadri della schermata di gioco: `DisplayableCanvasRiquadro{Locazione,Gruppo,Combattimento,Mappa,Missioni,Testo,Statistiche,IncantesimiEPozioni}`.
- Barra icone: `DisplayableCanvasBarraIconeDock` (default) e `DisplayableCanvasBarraIcone` (con argomento `BARRACLASSICA`).
- Testo: font bitmap `DoomdarkFont*`, `DoomdarkTextRectangle*`. Prompt di input: `Prompt`.

## Convenzioni

- La documentazione `.md` sta tutta nella radice (panoramica e mappa dei documenti in `foresta.md`) del progetto e descrive lo stato attuale (la storia è in git).
- I documenti finiscono con "Osservazioni" e, dove serve, "Da fare / idee aperte".
- `risorse_e_documenti_vari/` è la cartella di scratch: file non usati dal gioco ed esperimenti.
- I salvataggi non devono restare compatibili con le versioni precedenti.
- `tipiDanno.md` e `tipiPersonaggio.md` sono appunti generici sul genere fantasy, non descrivono il codice.

## Build e test

`mvn test` esegue tutti i test (JUnit 5, in `src/test/java`); al 2026-10-07 sono 729, tutti verdi (16 saltati).
`ui/DipendenzeUITest` controlla che la UI non importi motore e dominio, `eventi/DipendenzeEventiTest` che gli eventi non portino classi del dominio, `ui/DipendenzeVersoLaUITest` che nessuno fuori da `ui` la importi: le dipendenze che restano da togliere sono nelle loro `ECCEZIONI` (per la UI oggi nessuna).
Le cose da fare generali sono in `todo.md`; i `TODO`/`FIXME` puntuali restano nel codice.
