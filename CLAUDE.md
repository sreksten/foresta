# Foresta

Gioco di ruolo a turni in Java 8 + Swing, interamente in italiano (codice, commenti, documentazione).

Il progetto è un insieme di moduli Maven (pom padre nella radice); ogni modulo conosce solo quelli sopra di lui, quindi è il compilatore a garantire la separazione:

| Modulo | Contiene | Dipende da |
| :--- | :--- | :--- |
| `foresta-tipi` | `tipi`: enum e costanti di base (anche `Costanti`, i valori di bilanciamento) | nessuno |
| `foresta-modellodati` | `modellodati`: le classi `…MD`, solo dati (con la lettura e la scrittura dei salvataggi) | tipi |
| `foresta-contratti` | `interfacce`, `eventi` (bus e classi degli eventi), le utilità di `strumenti` (anche `Dado`, l'unica sorgente di caso) e i dati di `intermezzi` che servono alla UI | modellodati |
| `foresta-motore` | il resto: `motore`, `combattimento`, `personaggi`, `oggetti`, `locazioni`, `missioni`, `incantesimi`, `canti`, `offerte`, `tattiche`, `trofei`, `testi`, `salvataggi`, la logica degli intermezzi, gli eventi interni che portano il dominio, le grammatiche `.txt` | contratti |
| `foresta-swing` | `ui` (e gli eventi con sprite), le immagini | contratti |
| `foresta-app` | `Main`, `SnifferBusEventi` e i test che usano motore e UI insieme | motore, swing |

Un pacchetto può stare in più moduli (`strumenti`, `intermezzi`, `eventi.interni`): quel che conta è il modulo, non il pacchetto.
Ogni modulo ha le sue cartelle `src/main/java/com/threeamigos/foresta` (abbreviato `…/` sotto), `src/main/resources/com/threeamigos/foresta` e `src/test/java`. Tutti i file sono UTF-8.

## Dove trovare le cose

| Argomento | Codice | Documento |
| :--- | :--- | :--- |
| Avvio, macchina a stati, ciclo di una locazione, ciclo di vita dell'app (background, ripresa) | `Main`, `…/motore/Automa` (implementa `…/interfacce/ControlloreDiGioco`), `…/motore/Stato`, `…/motore/StatoDelMotore` | `motore_di_gioco.md` §2–3 |
| Bus eventi | `…/eventi` (`notifiche`, `richieste`, `interni`, `comandigiocatore`) | `motore_di_gioco.md` §4 |
| Salvataggi e modello dati | `…/modellodati`, `…/interfacce/GestoreSalvataggi`, `…/salvataggi/GestoreSalvataggiSuFile`, `…/motore/RiletturaPartita` | `motore_di_gioco.md` §5 |
| Mappa, tempo, locazioni | `…/motore/Foresta`, `LineaTemporale`, `…/locazioni` | `motore_di_gioco.md` §6 |
| Combattimento | `…/combattimento/CalcolatoreCombattimento`, `Ondata`, `StatoDelCombattimento`, `…/modellodati/EffettoDiStatoMD` | `motore_di_gioco.md` §7, `interazioni_effetti_di_stato.md` |
| Debolezze, resistenze e immunità di classe ai tipi di danno | `…/tipi/CategoriaPersonaggio`, `TipoPersonaggio` (tabelle), `…/personaggi/PersonaggioBase` (`getMoltiplicatoreDannoSubito`), `…/combattimento/CalcolatoreCombattimento`, `…/ui/TestoReazioneDiClasse` | `debolezze_e_resistenze.md` |
| Modificatori di attributo a tempo, armi che li mettono sul bersaglio | `…/modellodati/ModificatoreAttributoMD`, `…/oggetti/ModificatoreDelTesto`, `PersonaggioBase`, `CalcolatoreCombattimento` | `modificatori_temporanei.md` |
| Canti del Bardo (curativo, ipnotico, di valore), effetto di stato `ISPIRATO` | `…/canti/Canto*`, `…/tipi/TipoCanto`, `FabbricaCanti`, `…/locazioni/LocazioneBase` (`cantaUnCanto`), `…/motore/Automa` | `bardo.md` |
| Tattiche di combattimento (rose di candidati, contesto, valutatore, tattiche base dei mostri `TatticheBase`) | `…/tattiche` (logica; sottopacchetti `rose` e `valutazione`), `…/modellodati/tattiche` (dati, suffisso `MD`), `…/tipi/tattiche` | `tattiche_di_combattimento_automatiche.md` §11, `tattiche_internals.md` (il codice) |
| Testi che dipendono dalla lingua (in italiano in file `.properties`; ogni altra lingua è un file accanto con `_<lingua>`): i nomi e le descrizioni delle enum di `foresta-tipi` (un file per enum, `tipo_attributo_it.properties`, chiave `COSTANTE.VOCE`), le note dei moltiplicatori di classe (`note_personaggi_it`), le descrizioni dei comandi (`comandi_it`), i motivi di rifiuto dell'equipaggiamento (`rifiuti_equipaggiamento_it`) | `…/tipi/Testi` (caricatore per lingua; `Testi.testo(costante, voce)` per le enum), `…/tipi/NoteDeiPersonaggi` (mappa tipo di personaggio, attributo → nota); risorse in `foresta-tipi/src/main/resources/com/threeamigos/foresta/tipi/` (e `…/tipi/tattiche/`) | `assessment_portabilita.md` |
| Personaggi e gruppi | `…/personaggi`, `…/motore/Gruppo*`, `GestoreProgressione` | `motore_di_gioco.md` §8 |
| Oggetti, artefatti, incantatore | `…/oggetti` (i leggendari e i loro set in `…/oggetti/leggendari`: `OggettoLeggendario`, `SetLeggendario`, `CatalogoLeggendari`, `RegoleSetLeggendari`), `…/motore/Automa{Inventario,AcquistiArtefatti,Incantatore,ScambiatoreArtefatti}`, `Regole*` | `artefatti_e_incantamenti.md` |
| Negozi ed economia | `…/offerte`, `…/motore/RegoleContrattazione`, `OfferteAlchimista` | `economia.md` |
| Missioni (infrastruttura) | `…/missioni/MissioneAPassi`, `Passo`, `RegistroMissioni` | `gestione_missioni.md` |
| Missioni (catalogo) | `…/missioni/{principali,incarichi,richieste,leggende,storie}` | `passi_missioni.md` |
| Finestra di prova (solo in modalità di prova): personaggi, missioni da forzare, trucchi sui dadi per contesto | `…/strumenti/{Dado,ContestoLancio,TruccatoreDiDadi,TrucchiDiSviluppo}`, `…/interfacce/StrumentiDiSviluppo`, `…/motore/StrumentiDiSviluppoMotore`, `…/ui/sviluppo/FinestraDiProva` | `strumenti_di_prova.md` |
| Carta, forbici e sasso (sfida, missione dei campioni) | `…/motore/PartitaCartaForbiciSasso`, `…/locazioni/LocazioneBase`, `…/missioni/storie/LaSfidaDeiCampioni`, `IlCampione`, `…/ui/DisplayableCanvasRiquadroSfida` | `carta_forbici_sasso.md` |
| Intermezzi | `…/intermezzi` (anche `RegistroIntermezzi`) | `intermezzi.md` |
| Testi generati, grammatiche | `…/testi/GrammarBean`, `ProduttoreDiTestiCasuale`, risorse `motore/*.txt` | `GrammarBean.md` |
| Oggetti che fanno salire di livello un artefatto (solo nei cofani, si usano con la fusione) | `…/tipi/TipoArtefatto` (`AGGIORNAMENTO`), `…/oggetti/GeneratoreArtefattiTabelle`, `Cofano`, `…/motore/RegoleIncantatura`; **i nomi sono nella grammatica `foresta-motore/src/main/resources/com/threeamigos/foresta/motore/artefatti2.txt`** (produzione `NOME_AGGIORNAMENTO`) | `artefatti_e_incantamenti.md` |
| Trofei | `…/trofei` (anche `RegistroTrofei`) | `motore_di_gioco.md` §10 |
| Nomi dei personaggi, residenti del mondo (bardo, città, campioni) | risorsa `motore/nomi.txt`, `…/tipi/PersonaggioNoto`, `…/modellodati/ResidenteMD`, `…/motore/RegistroPersonaggi`, `ProduttoreDiTestiCasuale` | `nomi.md` |

## Schermate (tutte in `…/ui`, divise in sottopacchetti)

Sottopacchetti di `ui`: `schermate` (le schermate a tutto schermo e i loro aiuti), `riquadri` (i riquadri della schermata di gioco), `barra` (la barra icone), `sprite`, `testo` (font bitmap Doomdark), `immagini` (`ImageCache` e le immagini), `tattiche` (la schermata delle tattiche), `componenti` (componenti riusabili), `sfx` (logo e nuvole); in `ui` restano `ForestaUI`, `DisplayableCanvas` e poco altro.

Il canvas principale è `DisplayableCanvas` (enum interno `StatoDisplayableCanvas` per le schermate a tutto schermo).
Lo stato della partita la UI lo legge da `…/interfacce/VistaPartita` (sola lettura, implementata da `…/motore/VistaPartitaMotore`), mai dai singleton del motore; verso il motore manda solo eventi.
Panoramica in `motore_grafico.md`.

- Inventario del personaggio e del gruppo: `DisplayableCanvasInventario`; logica in `…/motore/AutomaInventario`, che la UI vede come `…/interfacce/VistaScambio` e comanda con `ComandoScambioArtefatto`; apertura con `…/eventi/richieste/RichiestaAperturaInventarioGruppo`.
- Armaiolo e venditore di pergamene: `DisplayableCanvasCommerciante`; alchimista: `DisplayableCanvasScambiatoreConsumabili`. In tutte e tre e nell'incantatore la colonna centrale porta il nome del negoziante (`disegnaIntestazioneCentrale` in `DisplayableCanvasScambiatore`), spezzato su più righe, con il mestiere in piccolo sotto.
- Inventario, commerciante e incantatore estendono `DisplayableCanvasScambiatoreArtefatti` (astratta, due liste di oggetti affiancate), che estende `DisplayableCanvasScambiatore`: un comportamento comune alle tre schermate va lì.
- Incantatore: `DisplayableCanvasIncantatore` (+ `…/motore/AutomaIncantatore`, `BancoDiLavoro`).
- Tattiche di combattimento del giocatore: `DisplayableCanvasTattiche` (estende `DisplayableCanvasScambiatore`; apertura dall'inventario con `MOSTRA_TATTICHE`; `BozzaTattica` per le modifiche, `CasellaADiscesa` per i menu, `TestoTattica` per i testi); `tattiche_di_combattimento_automatiche.md` §11.15.
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
- Il motore e i moduli sotto di lui non chiudono mai il processo (`System.exit`): per uscire pubblicano `NotificaUscitaDalGioco`, e `Main` (o la piattaforma) decide come. Lo controlla `ConvenzioniDiPortabilitaTest`.
- Un testo costruito per il log (concatenazione, `String.format`) si passa a `Logger.log` come fornitore, `Logger.log(() -> "Colpo a " + nome)`, così non si costruisce se il registro è spento; se una variabile cambia dopo la dichiarazione, si usa `if (Logger.isAttivo()) { ... }`. Lo controlla `ConvenzioniDiPortabilitaTest`.
- `tipiDanno.md` e `tipiPersonaggio.md` sono appunti generici sul genere fantasy, non descrivono il codice.

## Build e test

`mvn test` dalla radice esegue i test di tutti i moduli (JUnit 5, in `src/test/java` di ciascuno); al 2026-10-17 sono 1408, tutti verdi (16 saltati).
I test stanno nel modulo più basso che vede tutto quello che usano; i test di motore e UI insieme sono in `foresta-app`, che usa gli helper dei test del motore (`PartitaDiTest` e simili) tramite il suo `test-jar`.
Le dipendenze fra moduli le controlla il compilatore. Il compilatore non vede invece che nessuno fuori da `foresta-swing` usi `java.awt`, `javax.swing`, `javax.imageio` o `javax.sound` (fanno parte del JDK): lo controlla `ui/DipendenzeDalToolkitGraficoTest` in `foresta-app`, che legge i sorgenti (con `SorgentiDelProgetto`). Allo stesso modo `ConvenzioniDiPortabilitaTest` vieta le chiamate che dipendono dalla lingua del dispositivo senza `Locale` (`String.format`, `toLowerCase()`, `toUpperCase()`, `SimpleDateFormat`): si passa sempre `Locale.ROOT`. Il thread principale si raggiunge da `strumenti/ThreadPrincipale` (Swing lo installa con `ui/EsecutoreSwing` in `Main`).
`mvn package` scrive nella radice il jar eseguibile con le dipendenze, `foresta-0.0.1-SNAPSHOT-jar-with-dependencies.jar`.
Le cose da fare generali sono in `todo.md`; i `TODO`/`FIXME` puntuali restano nel codice.
