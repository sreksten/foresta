# Foresta

Gioco di ruolo a turni in Java 8 + Swing, interamente in italiano (codice, commenti, documentazione).

Il progetto è un insieme di moduli Maven (pom padre nella radice); ogni modulo conosce solo quelli sopra di lui, quindi è il compilatore a garantire la separazione:

| Modulo | Contiene | Dipende da |
| :--- | :--- | :--- |
| `foresta-testi` | `testi.Testi`, il caricatore dei testi per lingua (solo quella classe) | nessuno |
| `foresta-serializzazione` | `serializzazione`: `Serializzabile`, `LettoreCampi`, `MappaProprieta` (scrittura e lettura dei salvataggi a righe) | nessuno |
| `foresta-attributi` | `attributi` (`CalcolatoreAttributiSecondari`, le formule dei secondari), `attributi.tipi` (`TipoAttributo`, `CategoriaAttributo`), `modificatori` (`ModificatoreAttributoMD`) e `modificatori.tipi` (`TipoModificatore`, `UnitaDurataModificatore`, `BersaglioModificatore`): solo gli attributi e le modifiche che si possono fare loro, niente personaggi né oggetti | testi, serializzazione |
| `foresta-effettidistato` | `effettidistato.tipi` (`TipoEffettoDiStato`) e `effettidistato` (`EffettoDiStatoMD`): solo gli effetti di stato | testi, serializzazione |
| `foresta-danni` | `danni.tipi`: `TipoDanno`, `CategoriaDanno`, `TipoInterazioneConEffettiDiStato`: solo i danni e le loro interazioni con gli effetti di stato | testi, effettidistato |
| `foresta-tipi` | `tipi`: enum e costanti di base (anche `Costanti`, i valori di bilanciamento) | testi, attributi, danni |
| `foresta-modellodati` | `modellodati`: le classi `…MD`, solo dati (con la lettura e la scrittura dei salvataggi) | tipi, serializzazione |
| `foresta-contratti` | `interfacce`, `eventi` (bus e classi degli eventi), le utilità di `strumenti` (anche `Dado`, l'unica sorgente di caso) e i dati di `intermezzi` che servono alla UI | modellodati |
| `foresta-motore` | il resto: `motore`, `combattimento`, `personaggi`, `oggetti`, `locazioni`, `missioni`, `incantesimi`, `canti`, `abilita`, `offerte`, `tattiche`, `trofei`, `testi`, `salvataggi`, la logica degli intermezzi, gli eventi interni che portano il dominio, le grammatiche `.txt` | contratti |
| `foresta-swing` | `ui` (e gli eventi con sprite), le immagini | contratti |
| `foresta-app` | `Main`, `SnifferBusEventi` e i test che usano motore e UI insieme | motore, swing |

Un pacchetto può stare in più moduli (`testi`, `strumenti`, `intermezzi`, `eventi.interni`): quel che conta è il modulo, non il pacchetto.
Ogni modulo ha le sue cartelle `src/main/java/com/threeamigos/foresta` (abbreviato `…/` sotto), `src/main/resources/com/threeamigos/foresta` e `src/test/java`. Tutti i file sono UTF-8.

## Dove trovare le cose

| Argomento | Codice | Documento |
| :--- | :--- | :--- |
| Avvio, macchina a stati, ciclo di una locazione, ciclo di vita dell'app (background, ripresa) | `Main`, `…/motore/Automa` (implementa `…/interfacce/ControlloreDiGioco`), `…/motore/Stato`, `…/motore/StatoDelMotore` | `motore_di_gioco.md` §2–3 |
| Bus eventi | `…/eventi` (`notifiche`, `richieste`, `interni`, `comandigiocatore`) | `motore_di_gioco.md` §4 |
| Salvataggi e modello dati | `…/modellodati`, `…/interfacce/GestoreSalvataggi`, `…/salvataggi/GestoreSalvataggiSuFile`, `…/motore/RiletturaPartita` | `motore_di_gioco.md` §5 |
| Mappa, tempo, locazioni | `…/motore/Foresta`, `LineaTemporale`, `…/locazioni` | `motore_di_gioco.md` §6 |
| Combattimento | `…/combattimento/CalcolatoreCombattimento`, `Ondata`, `StatoDelCombattimento`, `…/effettidistato/EffettoDiStatoMD`, `…/danni/tipi/*` | `motore_di_gioco.md` §7, `interazioni_effetti_di_stato.md` |
| Debolezze, resistenze e immunità di classe ai tipi di danno | `…/tipi/CategoriaPersonaggio`, `TipoPersonaggio` (tabelle), `…/personaggi/PersonaggioBase` (`getMoltiplicatoreDannoSubito`), `…/combattimento/CalcolatoreCombattimento`, `…/ui/TestoReazioneDiClasse` | `debolezze_e_resistenze.md` |
| Modificatori di attributo a tempo, armi che li mettono sul bersaglio | `…/modificatori/ModificatoreAttributoMD` (in `foresta-attributi`), `…/oggetti/ModificatoreDelTesto`, `PersonaggioBase`, `CalcolatoreCombattimento` | `modificatori_temporanei.md` |
| Canti del Bardo (curativo, ipnotico, di valore), effetto di stato `ISPIRATO` | `…/canti/Canto*`, `…/tipi/TipoCanto`, `FabbricaCanti`, `…/locazioni/LocazioneBase` (`cantaUnCanto`), `…/motore/Automa` | `bardo.md` |
| Chierico e druido, abilità innate (scacciare i non morti, imposizione delle mani, radici, sciame, pelle di corteccia, rigenerazione), effetto di stato `RIGENERAZIONE` | `…/personaggi/{Chierico,Chierica,Druido,Druida}`, `…/abilita/*`, `…/tipi/TipoAbilita`, `…/locazioni/LocazioneBase` (`usaUnaAbilita`), `…/motore/Automa` | `chierico_e_druido.md` |
| Tattiche di combattimento (rose di candidati, contesto, valutatore, tattiche base dei mostri `TatticheBase`) | `…/tattiche` (logica; sottopacchetti `rose` e `valutazione`), `…/modellodati/tattiche` (dati, suffisso `MD`), `…/tipi/tattiche` | `tattiche_di_combattimento_automatiche.md` §11, `tattiche_internals.md` (il codice) |
| Testi che dipendono dalla lingua (in italiano in file `.properties`; ogni altra lingua è un file accanto con `_<lingua>`): i nomi e le descrizioni delle enum di `foresta-tipi` (un file per enum accanto all'enum, per esempio `tipo_attributo_it.properties` in `foresta-attributi`, chiave `COSTANTE.VOCE`), le note dei moltiplicatori di classe (`note_personaggi_it`), le descrizioni dei comandi (`comandi_it`), i motivi di rifiuto dell'equipaggiamento (`rifiuti_equipaggiamento_it`) | `…/testi/Testi` (in `foresta-testi`; caricatore per lingua; `Testi.testo(costante, voce)` per le enum), `…/tipi/NoteDeiPersonaggi` (mappa tipo di personaggio, attributo → nota); risorse in `foresta-tipi/src/main/resources/com/threeamigos/foresta/tipi/` (e `…/tipi/tattiche/`) per le enum, e in `…/resources/com/threeamigos/foresta/testi/` per i file letti con `Testi.get(file, chiave)` (`note_personaggi`, `comandi`, `rifiuti_equipaggiamento`, `caratteristica_di_classe`) | `assessment_portabilita.md` |
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
| Bestiario (voci, sblocchi, scoperte, scheda; immunità e caratteristiche generali di classe; moltiplicatori con le loro note) | `…/bestiario/RegistroBestiario`, `…/modellodati/BestiarioMD`, `…/tipi/{TipoPersonaggio,CaratteristicaDiClasse,MoltiplicatoreDiClasse,AbilitaInnataDiClasse}`, `…/personaggi/MoltiplicatoriDiClasse`, `…/abilita/AbilitaInnateDiClasse`, `…/trofei/TrofeoEnciclopedista`, `…/ui/schermate/{DisplayableCanvasBestiario,ImmagineBestiario}` | `bestiario.md` |
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
- Trofei: `DisplayableCanvasTrofei`. Bestiario: `DisplayableCanvasBestiario` (con `ImmagineBestiario`). Intermezzi: `DisplayableCanvasIntermezzo`, `AnimazioniIntermezzo`.
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

## Ricette (cosa toccare per aggiungere…)

Sono i punti che si ripetono ogni volta; l'esempio da copiare è indicato. Aggiornare anche il documento `.md` dell'argomento (tabella "Dove trovare le cose") e cercare i test che elencano le costanti dell'enum toccata.

- **Un intermezzo fisso**: costante in `foresta-tipi/…/tipi/TipoIntermezzo` (il nome è l'identificativo salvato in `IntermezziMD`; **l'ordine dell'enum è la priorità** con cui `RegistroIntermezzi` sceglie quale scatta per primo in un `MomentoIntermezzo`); classe in `foresta-motore/…/intermezzi` che implementa `Intermezzo` (`getId`, `deveScattare(momento)`, `getPagine`; `isRipetibile`/`isDiRipiego` se serve); registrazione in `FabbricaIntermezzi`. Nei negozi si costruisce la scena con `NegozioInScena.X.entraIlGruppo().parlaIlNegoziante(..).parlaIlCapo(..).getPagine()` (`ScenaNegozio`), e `NegozioInScena.X.isIngresso(momento)` è l'innesco (momenti `INGRESSO_ALCHIMISTA` ecc.). Se scatta per un fatto avvenuto altrove (un oggetto trovato) lo stato da ricordare è `static` nella classe, e va azzerato in `motore/RiletturaPartita` e in `motore/Foresta` (come `IntermezzoLibroTecnico.reimposta()`). Esempi: `IntermezzoLibroTecnico` (innescato da un cofano), `IntermezzoOroscopo` (alchimista, ripetibile, con battute da grammatica), `IntermezzoBenvenutoNegozio`.
- **Un trofeo**: costante in `foresta-tipi/…/tipi/TipoTrofeo` (con la `CategoriaTrofeo`); `NOME` e `DESCRIZIONE` in `foresta-tipi/…/tipi/tipo_trofeo_it.properties` (chiavi `COSTANTE.NOME`, `COSTANTE.DESCRIZIONE`); voce con lo stesso nome in `foresta-motore/…/trofei/ClasseTrofeo`, di solito un `TrofeoAContatore<>(tipo, ClasseEvento.class, evento -> 1 o 0, obiettivo)` su un evento `Interno…`/`Notifica…` (obiettivo 1 per i trofei "una volta sola"; il contatore conta fra le partite). Il trofeo viene controllato e annunciato solo a **fine locazione** (`RegistroTrofei.fineLocazione`), non nel momento dell'evento. Se manca un evento adatto se ne pubblica uno nuovo (`eventi/interni`).
- **Un oggetto leggendario con nome e proprietà fissi (non a caso)**: riga in `foresta-motore/src/main/resources/…/motore/leggendari.txt`, sotto `LEGGENDARIO_DI_BOTTINO$` se non deve essere pescato come leggenda (il formato dei campi è in testa al file); si ottiene con `CatalogoLeggendari.getLeggendario(chiave)` → `OggettoLeggendario.costruisci()` → `GruppoGiocatore.getIstanza().addArtefatto(..)` + `NotificaArtefattoTrovato`. `costruisci()` fa partire dal livello del mondo; `OggettoLeggendario.alLivelloDelMondo` riallinea un leggendario già esistente. Esempio: `missioni/leggende/LaFalceDelTristoMietitore.consegnaLaFalce`.
- **Un artefatto con attributi a caso ma nome/immagine propri** (come i libri tecnici): `GeneratoreArtefatti.generaArtefatto(tipo, livello)` e poi `ArtefattoMD.setNome/setDescrizione/setRarita/setAspetto` (`oggetti/leggendari/LibroTecnico.costruisci`). L'immagine è l'**aspetto**, un `TipoOggetto` non generabile (`TipoOggetto`, `generabile=false`) mappato in `foresta-swing/…/ui/immagini/ClassiOggettoImmagine` su un gif in `foresta-swing/src/main/resources/com/threeamigos/foresta/img/oggetti/`. Riconoscere l'artefatto dopo il salvataggio: per aspetto + nome (`LibroTecnico.di`).
- **Un nuovo tipo di arma/artefatto** (come la falce): `TipoArtefatto`, `TipoOggetto`, `tipo_artefatto_it.properties`, `oggetti/<Tipo>` + `FabbricaOggetti`, `GeneratoreArtefattiTabelle`, `motore/RegoleEquipaggiamento` (chi lo può usare), `ClassiOggettoImmagine`, `ui/sprite/SpriteRivelazioneArtefatto`, `ui/schermate/ImmagineBestiario`. Per una spada con un'immagine diversa **non serve** un nuovo tipo: basta l'aspetto.
- **Cofani**: cosa danno e con che probabilità sta in `oggetti/Cofano.artefattoRaro` e `Costanti.COFANO_PROBABILITA_*`; `Cofano.opzioniOggettoSpeciale` e `motore/StrumentiDiSviluppoMotore` fanno vedere/forzare gli oggetti speciali nella finestra di prova.

## Build e test

`mvn test` dalla radice esegue i test di tutti i moduli (JUnit 5, in `src/test/java` di ciascuno); al 2026-10-24 sono 1803, tutti verdi (22 saltati, fra cui le analisi manuali di `AnalisiAbilitaInnateTest`).
I test stanno nel modulo più basso che vede tutto quello che usano; i test di motore e UI insieme sono in `foresta-app`, che usa gli helper dei test del motore (`PartitaDiTest` e simili) tramite il suo `test-jar`.
Le dipendenze fra moduli le controlla il compilatore. Il compilatore non vede invece che nessuno fuori da `foresta-swing` usi `java.awt`, `javax.swing`, `javax.imageio` o `javax.sound` (fanno parte del JDK): lo controlla `ui/DipendenzeDalToolkitGraficoTest` in `foresta-app`, che legge i sorgenti (con `SorgentiDelProgetto`). Allo stesso modo `ConvenzioniDiPortabilitaTest` vieta le chiamate che dipendono dalla lingua del dispositivo senza `Locale` (`String.format`, `toLowerCase()`, `toUpperCase()`, `SimpleDateFormat`): si passa sempre `Locale.ROOT`. Il thread principale si raggiunge da `strumenti/ThreadPrincipale` (Swing lo installa con `ui/EsecutoreSwing` in `Main`).
`mvn package` scrive nella radice il jar eseguibile con le dipendenze, `foresta-0.0.1-SNAPSHOT-jar-with-dependencies.jar`.
Le cose da fare generali sono in `todo.md`; i `TODO`/`FIXME` puntuali restano nel codice.
