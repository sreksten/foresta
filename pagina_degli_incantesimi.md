# Il grimorio (la pagina degli incantesimi)

Stato: **fatto** (2026-11-07), con l'icona provvisoria da sostituire. Una pagina per consultare gli incantesimi: che cosa fanno e con che cosa reagiscono per fare più danno. Il giocatore la apre dall'inventario quando vuole, e il tutorial potrà rimandare a lei.

## Come funziona

- Si apre dall'inventario con l'icona `MOSTRA_GRIMORIO` e si chiude con `ANNULLA` (il solo comando, come per il bestiario e i trofei). Nel motore è lo stato `Stato.GRIMORIO`, che pubblica `RichiestaAperturaGrimorio` (in `07-foresta-poteri`); nella UI è `StatoDelloSchermo.GRIMORIO`, aperto da `ForestaUI` con `SchermoDelGioco.grimorio()`.
- **Mostra tutti** gli incantesimi (`TipoIncantesimo`, nell'ordine dell'enum), anche quelli che il gruppo non ha mai visto: è una pagina di riferimento, non si sblocca giocando come il bestiario. Le voci vengono dalle enum e non dalla `VistaPartita`.
- Ogni incantesimo ha la sua icona (quella del suo comando): com'è, a sinistra del nome nell'elenco, e ingrandita nella scheda. Tutto il testo della scheda è nel font medio, grigio chiaro, alla stessa altezza: le righe piccole si leggevano male.
- Il disegno è quello del bestiario: a sinistra l'elenco, a destra la scheda dell'incantesimo scelto con un clic, due colonne che scorrono a parte (rotella, trascinamento, frecce), ombra del drago tenue, titolo «grimorio» e un sottotitolo scherzoso a caso (quattro) a ogni apertura.
- La **scheda** dice, in quest'ordine: l'icona ingrandita (il doppio) e il nome, centrati insieme; l'`EFFETTO` in bianco; su chi agisce (`PortataIncantesimo` a parole); i danni base, se l'incantesimo ne fa, con sotto in piccolo la nota che i danni veri crescono col livello e con i bonus degli oggetti (`IncantesimoMalefico.getDanni()` a livello 1: il calcolo del combattimento li moltiplica poi per la radice quadrata del livello di chi lancia, `CalcolatoreCombattimento.livelloDiCombattimento`); quanta Magia costa lanciarlo e quanto una pergamena; il tipo di danno e gli stati che quel danno può mettere (`TipoDanno.getEffetti()`); le **reazioni**: per ogni `TipoInterazioneConEffettiDiStato` il cui innescante è il tipo di danno dell'incantesimo, il nome, su chi scatta (gli stati del bersaglio), di quanto cambia il danno, quali stati toglie e quali mette. Resurrezione e Grido di battaglia non fanno danno: non hanno né danni base né reazioni.
- Le reazioni si leggono dai dati e non si ricopiano nei testi, così restano giuste se il bilanciamento cambia.

## Dove sta

| Che cosa | Dove |
| :--- | :--- |
| Comando `MOSTRA_GRIMORIO` | `02-foresta-comandi` (`Comando`, testo in `comandi_<lingua>`) |
| Evento `RICHIESTA_APERTURA_GRIMORIO` | `01-foresta-eventi` (`TipoEvento`) e `RichiestaAperturaGrimorio` in `07-foresta-poteri` |
| Stato e comandi | `12-foresta-motore`: `Stato.GRIMORIO`, `Automa` (`entraInStatoGrimorio`, `gestisciComandoInStatoGrimorio`, il comando fra quelli dell'inventario) |
| Modello e contenuti | `12-foresta-ui-comune`: `SchermataGrimorio`, `ContenutiDelGrimorio`; icona `TipoIcona.MOSTRA_GRIMORIO` (`icone/Grimorio.png`, nell'atlante delle icone) |
| Disegno | Swing: `DisplayableCanvasGrimorio`; libGDX: `SchermataGrimorioGdx` |
| Testi | `ui_<lingua>.properties` (`TITOLO_GRIMORIO`, `GRIMORIO_*`) |
| Test | `GrimorioTest` (ui-comune), `ScenarioGrimorioTest` (motore) |

## Osservazioni

- L'icona è per ora una versione provvisoria in `12-foresta-ui-comune/sorgenti-immagini/icone/Grimorio.png`: per cambiarla si sostituisce il file (32 x 32) e si rifà l'atlante con `risorse_e_documenti_vari/atlanti/costruisci_atlante.py icone`.
- Il moltiplicatore si scrive con la virgola in ogni lingua, come nel bestiario.

## Da fare / idee aperte

- Controllare a occhio, sul tablet e in Swing, che la scheda con le reazioni (ora in font medio) non diventi troppo lunga.
