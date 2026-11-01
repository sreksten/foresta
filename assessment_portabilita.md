# Portabilità verso Android e iOS: cosa manca

Stato al 2026-10-26. Riguarda `10-foresta-modellodati`, `11-foresta-motore-api` e le parti di `12-foresta-motore` che toccano disco, thread, tempo e testi; da «La UI Swing» in poi anche `12-foresta-swing`, esaminata file per file (99 file, circa 23.500 righe). I numeri di quella parte vengono da una lettura e da conteggi sul sorgente, non da una misura a runtime: prestazioni e memoria su un dispositivo vero non sono state provate.

In sintesi: **il motore e i moduli di dominio sono già pronti** (nessuno usa il toolkit grafico, né `System.exit`, né `java.nio.file`, reflection, `ServiceLoader`, `java.time`; solo `java.util`, `java.io`, stream, `java.util.function`, `java.util.concurrent`, `regex`, `java.text`), **manca la UI**, da riscrivere, e un punto d'ingresso per la piattaforma (oggi `Main` e `13-foresta-app-swing` sono Swing e `System.exit`). Non esiste ancora nessun modulo Android o libGDX. Il sorgente è Java 8: su Android gli stream e le lambda chiedono API 24 o il desugaring.

Qui c'è soltanto quello che resta da fare. Quello che è già stato sistemato non è elencato: la motivazione di ogni cambiamento sta nel javadoc o in un commento del codice che lo riguarda, e la storia in git. I punti di aggancio per una piattaforma (ciclo di vita dell'app, uscita dal gioco, cartella dei file, destinazione del log, lingua dei testi) sono descritti nei javadoc di `ControlloreDiGioco`, `NotificaUscitaDalGioco`, `GestoreSuFile`, `DestinazioneDiLog` e `Testi`, e in [`motore_di_gioco.md`](motore_di_gioco.md) (§5, "Ciclo di vita dell'app").

## Portabilità

### Testi italiani nel codice

Il gioco è scritto in italiano e ora si vuole una **versione inglese** (prima per provare, poi per gli store). Questa sezione è l'analisi di cosa serve e l'elenco dei passi, da smarcare uno per volta (`[ ]` da fare, `[x]` fatto).

#### Com'è oggi

- **Già localizzabile.** `Testi` (in `01-foresta-testi`) legge file `<nome>_<lingua>.properties` in UTF-8 con ripiego sull'italiano. Sono già così 29 file `_it.properties` (circa 2.000 righe): i nomi e le descrizioni delle enum dei vari moduli (`Testi.testo(costante, voce)`), le descrizioni dei comandi, le note dei moltiplicatori di classe, i motivi di rifiuto dell'equipaggiamento, le parole della tattica (`testo_tattica`) e una parte delle scritte della UI (`ui_it`, 25 righe, lette da `TestiDellaUI`).
- **La lingua non si sceglie da nessuna parte.** `Testi.impostaLingua` lo chiamano solo i test: `Main` non ha un argomento e la piattaforma mobile dovrà passare la lingua del dispositivo.
- **Il motore dei testi casuali è neutro, i dati no.** `GrammarBean` non conosce l'italiano (non usa `Articoli`, `Numeri` né `Locale`); sono italiane le sue grammatiche `.txt` (596 KB) e il file delle sostituzioni finali `preposizioni_articolate_pp.txt` ("a il" → "al").
- **Il resto è italiano nel codice.** Scansione dei sorgenti `src/main` (stringhe con almeno due parole italiane o con lettere accentate; esclusi commenti e log), al 2026-11-01: **1.844 stringhe in 311 file**. È una stima per difetto (le frasi spezzate in più pezzi e le parole sole non si contano).

| Modulo | Stringhe | Dove, e di che tipo |
| :--- | ---: | :--- |
| `09-foresta-missioni` | 808 | nomi, descrizioni e dialoghi di ogni missione e passo (`LaForgia` 32, `TappeRichieste` 30, `MissioneATappe` 30, `IlRapimento` 29, …) |
| `12-foresta-motore` | 209 | messaggi di narrazione: `LocazioneBase` 38, `Automa` 36, `Locanda` 19, `RaccoltaBottino` 18, `Pernottamento` 14 |
| `12-foresta-swing` | 207 | `DisplayableCanvasTattiche` 69, `TestoAzioneInCombattimento` 24, `FinestraDiProva` 17 (strumento di sviluppo) |
| `08-foresta-tattiche` | 129 | `Requisiti` 57 (motivi per cui una tattica non vale), `TatticheBase` 34 (nomi delle tattiche) |
| `05-foresta-personaggi` | 101 | `NomeEDescrizione` 17 e gli articoli/nomi nelle classi dei personaggi |
| `04-foresta-artefatti` | 71 | `GeneratoreArtefattiTabelle` 38 (nomi con articolo: "la spada", "lo spadone") |
| `07-foresta-intermezzi` | 59 | battute dei fumetti (`parlaIlCapo`, `BattutaIntermezzo.di`, 72 chiamate in 35 file) |
| `07-foresta-poteri`, `08-foresta-economia`, `06-foresta-statistiche` | 49, 48, 33 | messaggi di incantesimi e canti, offerte, `LineaTemporale` (le ore a parole, 32) |
| altri moduli | circa 130 | `Anello`, `RegistroArtefatti`, `Racconto`, eccezioni di `00-foresta-serializzazione` |

Una parte di queste stringhe sono messaggi per lo sviluppatore (eccezioni, "Stato non gestito", `FinestraDiProva`): è una scelta tenerli in italiano, come i log.

**Risorse.** Le grammatiche `.txt` pesano 596 KB: `missioni.txt` 224, `artefatti2.txt` 123, `locande.txt` 69, `tappe.txt` 61, `fiabe.txt` 43, `leggendari.txt` 29, `oroscopo.txt` 16, `nomi.txt` 13 (quasi neutro), `ingredienti.txt` 9, poi `templi`, `rovine` e `sfide`. Sono la maggior parte del testo del gioco, e in italiano scherzoso: tradurle vuol dire riscriverle.

**Quanto la grammatica italiana è sparsa nel codice.** `Articoli.` si usa in 97 file (582 punti), `Numeri.` in 25, `inizialeMaiuscola` in 26; gli accessori di genere e articolo del personaggio (`getADS`, `getAIS`, `getDeS`, `getPronome`, `getNomeSingolare`, `getNomePlurale`…) in 112 file (446 punti), e **52 classi di personaggi** li hanno scritti a mano (`Goblin`: `getADS() { return Articoli.IL; }`, `getNomeSingolare() { return "Goblin"; }`). La narrazione passa dal bus come stringhe già pronte (`NotificaTestoFrase` e `NotificaTestoParagrafo`: 258 punti in 93 file).

#### Dove la sola traduzione non basta

1. **Frasi montate a pezzi.** `"Il livello di magia " + x + " non permette di formulare questo incantesimo."`, `LaForgia`, `Pernottamento`: l'ordine delle parole è quello italiano, e una traduzione dei pezzi sciolti non dà inglese corretto. Serve una chiave per frase, con i valori al posto di `{0}`, `{1}`.
2. **Grammatica che dipende dal nome.** Articolo ("il/lo/l'/la/i/gli/le", "un/uno/una/un'"), preposizione articolata ("del/dello/dell'/della…"), plurale, pronome e accordo ("sano/sana come un pesce"). In inglese le regole sono altre ("the", "a/an" secondo il suono, "of the"), e vanno dette per lingua, non per messaggio.
3. **Numeri e ordinali, elenchi, maiuscole** ("A, B e C" contro "A, B, and C"; "Viverna 2").
4. **Testo che finisce nel salvataggio.** `MessaggioMD`, `NotiziaMD`, `ArtefattoMD` (nome, nome proprio, descrizione, generati dalle grammatiche), parametri delle missioni: una partita salvata in italiano resta italiana. Non si traduce a caldo.
5. **Il nome di una missione è testo montato** (`LaForgia.getNome()`: "Armaiolo e la forgia"), non una costante.
6. **Test.** 288 file di test aspettano frasi italiane: devono girare con la lingua bloccata sull'italiano.

#### Come organizzarlo: tre strati, e solo uno ha classi per lingua

Non tutto va in file di testo, ma neppure serve una classe per ogni messaggio. Il codice dice **che cosa** succede (soglie, scelte, valori), i file dicono **come si scrive**, e una sola classe per lingua dice **come si coniuga la grammatica**.

- **Strato A, contenuto: `.properties`.** Tutto quello che è una frase o un'etichetta, una chiave per frase, con i valori in `{0}`. Per le varianti di genere o di numero ci sono chiavi con suffisso (`SALUTE_MASSIMA.M`, `SALUTE_MASSIMA.F`; `ORE.UNA`, `ORE.ALTRE`): il codice sceglie la chiave, non il testo. È anche il caso della descrizione del personaggio: la logica a soglie (`salute < 20`…) resta in Java e sceglie un livello (`MOLTO_DEBOLE`, `DEBOLE`…), le parole stanno nel file, e l'ordine della frase intera è un modello che ogni lingua scrive come vuole. Niente classe per lingua.
- **Strato B, grammatica: un'interfaccia `RegoleDellaLingua` in `01-foresta-testi`**, con `LinguaItaliana` e `LinguaInglese` scelte da `Testi.getLingua()`. Fa solo: articolo determinativo e indeterminativo (singolare e plurale) di un nome, preposizione articolata (in inglese "of the"), plurale, cardinali e ordinali, elenco ("A, B e C"), maiuscola iniziale. `Articoli` e `Numeri` diventano la parte italiana. Perché un nome si possa articolare, deve portare i suoi dati (genere, plurale, se comincia con suono di vocale o di "s" impura): vengono dal file dei testi, accanto al nome (`GOBLIN.NOME`, `GOBLIN.GENERE`, `GOBLIN.PLURALE`), e **spariscono dalle 52 classi dei personaggi**.
- **Strato C, testi casuali: una grammatica per lingua** (`missioni_it.txt`, `missioni_en.txt`…), scelta da `CaricatoreDiGrammatiche` con ripiego sull'italiano, e il suo file di sostituzioni finali (in inglese serve "a" → "an"). `GrammarBean` non cambia.

Una partita appartiene alla lingua in cui è cominciata: **la lingua si salva nella testata del salvataggio** (`TestataSalvataggio`) e al caricamento si ripristina; la lingua si sceglie a una nuova partita, non a metà. I salvataggi non devono restare compatibili, quindi si può aggiungere il campo senza migrazioni.

Durante il lavoro la lingua mista è accettata: ciò che manca in inglese ripiega sull'italiano (`Testi` lo fa già), e si vede subito dove manca. Per non perdere il conto, un test conta le stringhe italiane ancora nel codice e **non può mai salire**.

#### Passi

Ogni passo compila e ha i test verdi; la dimensione è S (qualche ora), M (una sessione), L (più sessioni).

0. **Decisioni** (prese il 2026-11-01)
   - [x] **lingue**: inglese sia americano (`en-US`) sia britannico (`en-GB`); poi, in prospettiva, francese, tedesco e spagnolo (e i glifi coprono praticamente tutte le lingue europee). Quindi niente di "inglese" nel codice: tutto passa dall'interfaccia delle regole della lingua (strato B), e ogni scelta di disegno si controlla pensando a una lingua con generi, casi e plurali diversi dall'italiano e dall'inglese (il tedesco ha tre generi, il francese l'elisione, lo spagnolo gli articoli davanti ad "a" tonica);
   - [x] **varianti regionali**: la lingua è un codice con regione facoltativa (`en-GB`); i testi si cercano dal più specifico al più generico (`en-GB` → `en` → `it`), così `en-GB` contiene solo ciò che differisce da `en` ("armour", "colour", "grey"). Chiave del file: `<nome>_en.properties`, `<nome>_en_GB.properties`;
   - [x] **cosa si traduce**: solo ciò che arriva sulla UI (narrazione, nomi, descrizioni, dialoghi, etichette). I messaggi interni (eccezioni, log, `FinestraDiProva`, strumenti di sviluppo) restano in italiano e non si contano nel contatore;
   - [x] chi traduce: Claude scrive la bozza di ogni file, Stefano rivede (elenco in «File da ricontrollare»);
   - [ ] conferma del disegno a tre strati e della lingua legata alla partita.
1. **Infrastruttura** (S/M)
   - [x] `Testi` cerca per catena di lingue (`en-GB` → `en` → `it`; file `_en_GB`, `_en`, `_it`); `impostaLingua` accetta `en-GB`, `en_gb`, `EN`; test in `TestiTest`;
   - [x] argomento `LINGUA=<codice>` in `Main` (sul desktop il predefinito resta l'italiano; sul telefono la piattaforma passerà la lingua del dispositivo). Resta lo statico di `Testi`: la lingua legata alla partita è il passo 11;
   - [x] `Testi.formatta(file, chiave, valori…)` e `Testi.sostituisci(modello, valori…)`: sostituzione di `{0}`, `{1}` senza `MessageFormat` (che dipende dal `Locale` e vuole gli apostrofi raddoppiati), ora usata da `TestiDellaUI` e `TestoTattica`;
   - [ ] estensione di JUnit che blocca la lingua su "it" (e la ripristina) per i test che aspettano frasi italiane — rimandata: oggi i test non cambiano la lingua se non per ripristinarla, serve quando i primi file `_en` esistono e un test lascia la lingua impostata;
   - [x] test di completezza (`ConvenzioniDiLocalizzazioneTest`): un file di un'altra lingua non ha chiavi che l'italiano non ha e porta gli stessi segnaposto. Ancora da fare, a fine lavoro (passo 12): ogni chiave italiana ha la sua traduzione; ogni grammatica si carica e produce un testo;
   - [x] `TestiConSoloCaratteriDelFontTest` legge già ogni `.properties` e `.txt` di ogni modulo, quindi copre da solo i file in qualunque lingua;
   - [x] **test del contatore** (stesso `ConvenzioniDiLocalizzazioneTest`): conta per modulo le stringhe italiane in `src/main` (almeno due parole italiane comuni o una lettera accentata; esclusi commenti, `Logger.log`, eccezioni, `assert` e gli strumenti di sviluppo) e le confronta con `localizzazione_baseline.properties` (13-foresta-app-swing, test). Misura più severa di quella della prima analisi: **circa 900 stringhe** (`09-foresta-missioni` 447, `12-foresta-motore` 104, `12-foresta-swing` 85, `08-foresta-tattiche` 72, `05-foresta-personaggi` 35, `07-foresta-intermezzi` 33, `07-foresta-poteri` 28, …); chi ne sposta nei file abbassa il numero.
2. **Tradurre ciò che è già in file** (M) — il primo risultato visibile
   - [x] i 29 `_it.properties` hanno il loro `_en` (bozza, da rivedere) e 15 hanno anche un `_en_GB` con le sole differenze britanniche; il test `l_inglese_americano_ha_tutte_le_chiavi_dell_italiano` controlla che non manchi nessuna chiave;
   - [x] la UI già in `ui_it` e `testo_tattica`;
   - [ ] (da provare avviando con `LINGUA=en`: i file sono scritti e i test passano, ma il gioco non è stato aperto in inglese) a fine passo il gioco è in inglese nei nomi, nelle descrizioni, nei comandi e nelle scritte della UI, e in italiano nel resto.
3. **Regole della lingua** (L) — il passo che cambia la forma del codice, da concordare prima
   - [x] `RegoleDellaLingua` (articoli, preposizioni articolate, `daAgente`, cardinali e ordinali, elenchi, suono iniziale), `LinguaItaliana` (usa `Articoli` e `Numeri`), `LinguaInglese`, `Lingue.corrente()`; `Nome` (singolare, plurale, genere, suono, pronome, agente determinato), `Genere`, `Suono`, `Preposizione`; test `RegoleDellaLinguaTest`;
   - [x] genere, plurale, pronome, suono e agente determinato dei personaggi nei file `tipo_personaggio_<lingua>` (voci `NOME_SINGOLARE`, `NOME_PLURALE`, `GENERE`, `PRONOME`, `SUONO`, `AGENTE_DETERMINATO`);
   - [x] il bottino: `tipo_bottino_<lingua>` (nome, plurale, genere, articoli propri come «un paio di schinieri»); `OggettoBase`, `FormaDelBottino`, `BottinoArtefatto` e `NomeOggetto` (che ora prende il genere, anche diverso fra singolare e plurale) li leggono dalle regole;
   - [x] i tipi di artefatto: `nomi_artefatti_<lingua>` (nome comune, genere, articolo proprio degli schinieri); `Nome.di(file, costante)` per le enum con un loro schema di voci;
   - [ ] i tipi di locazione (i nomi proprî dei luoghi con articolo: `tipo_locazione` ha già `NOME_PROPRIO`, manca il genere per gli articoli);
   - [x] le 52 classi dei personaggi perdono gli articoli scritti a mano (506 metodi tolti): li dà `PersonaggioBase` con le regole della lingua. Una sola differenza in italiano: «il Uomo» (errore) ora è «l'Uomo»;
   - [x] `NomeEDescrizione` (descrizione della scheda) legge `descrizione_personaggio_<lingua>`; la preposizione «di» davanti al nome proprio passa dalle regole;
   - [x] `Articoli.` e `Numeri.` nei moduli (tranne i test e `01-foresta-testi`): le funzioni (`inizialeMaiuscola`, `conPreposizione`, cardinali, ordinali: 187 punti) passano a `Lingue.corrente()`; le costanti (`Articoli.IL` ecc.) non si usano più fuori da `LinguaItaliana` e dai test. Restano da tradurre i testi che le circondano (passi 4-6, 8, 9). Correzioni di italiano che ne sono venute: «l'Uomo» (era «il Uomo»), «l'orecchio di goblin» (era «il orecchio»), «gli articoli» davanti a vocale nei materiali delle missioni.
4. **Enum e costanti con testo** (S) — fatti `Direzioni` (`direzioni_<lingua>`, con i punti cardinali composti) e le ore del giorno e gli eventi del drago di `LineaTemporale` (`linea_temporale_<lingua>`; corretto «Soo le tre» in «Sono le tre»); `Gruppo.chi()` («il gruppo» / «the party», `gruppo_<lingua>`). Restano:
   - [ ] `FUGA_IGNOMINIA_NOTA` (è nel salvataggio: decidere se confrontarla per chiave), `NegozioInScena`, `Mandante` (frasi mescolate a identificativi: da separare), `DovePartono`, `ContestoLancio`, `Direzioni`, le ore a parole di `LineaTemporale`.
5. **Nomi e descrizioni montati** (M)
   - [x] `NomeEDescrizione` (descrizione del personaggio);
   - [x] `GeneratoreArtefattiTabelle`: nomi («la spada di ferro» / «the iron sword»: il modello `NOME_CON_COMPLEMENTO` dice dove va il complemento), nomi propri, descrizioni e grado nel nome (`conGrado`: «la gemma minore del Drago» / «the minor gem of the Dragon») in `generatore_artefatti_<lingua>`;
   - [x] `Anello` (nomi dal bottino); `TatticheBase` (nomi delle voci, `tattiche_base_<lingua>`); i problemi di una voce (`VoceMD.problemi`, in `testo_tattica`). `Requisiti` e `Valutatore` sono la traccia della decisione per chi sviluppa (come il log) e non si traducono: esclusi dal contatore;
   - [ ] `RegistroArtefatti` e i nomi che vengono dalle grammatiche degli artefatti (`artefatti2.txt`, passo 10).
6. **Messaggi del motore** (L)
   - [x] personaggi e gruppi (`messaggi_personaggi`: pozioni, ospiti, resa, morte, ferite, immunità), combattimento (`messaggi_combattimento`), modificatori (`testo_modificatori`, con la durata residua che prima era un `replaceFirst` italiano), poteri: incantesimi, canti e abilità (`messaggi_poteri`, 42 frasi; le abilità di danno costruiscono ora l'intera frase con `getAnnuncio(utilizzatore, bersagli)`);
   - [x] economia: le offerte (`AiutoGratuito`, `AiutoMercenario`, `Incantesimi`, mappe, `Pasto`, `Informazioni`) hanno una frase per caso (gruppo o singolo, venditore maschio o femmina) in `offerte_<lingua>`, così l'inglese può costruirle come vuole; nomi e descrizioni dell'alchimista in `tipo_consumabile_<lingua>`;
   - [x] motore: `LocazioneBase` (la descrizione di chi si vede, con verbo di guardia a dado: «che protegge/custodisce/sorveglia» / «guarding/protecting/watching over»; corruzione, amicizia, sfide, scheletri), le locazioni, `Locanda`, `Pernottamento`, `RaccoltaBottino` (cofani con ordinale), `Bottega`, `GestoreBotteghe`, `TestoAvvisoDiFusione`, i messaggi di `Automa` visibili al giocatore: `messaggi_motore_<lingua>` (circa 130 chiavi). Restano in italiano, perché per chi sviluppa, i messaggi di `GestoreSalvataggiSuFile`, le tracce `Automa in stato…` e gli oggetti di prova (`DotazioneDiProva`, la modalità di prova di `Automa`);
   - [ ] statistiche (`06-foresta-statistiche` ha ancora 15 frasi, vedi il contatore); la narrazione continua a passare dal bus come stringa, già nella lingua giusta (non si cambia il bus).
7. **UI Swing** (M)
   - [x] `DisplayableCanvasTattiche` (82 chiavi `TATTICHE_*`), `TestoAzioneInCombattimento` (frasi con e senza bersaglio, chiavi `AZIONE_*` e `_SU`), i negozi, l'incantatore, l'inventario, l'introduzione, il bestiario, i titoli dei riquadri e gli annunci: circa 200 chiavi nuove in `ui_it`/`ui_en`/`ui_en_GB`; il contatore di `12-foresta-swing` scende da 85 a 15 (restano: stringhe che dipendono ancora dalle regole della lingua, vedi sotto). `FinestraDiProva` resta italiana (strumento di sviluppo);
   - [ ] restano in `12-foresta-swing` e dipendono dal passo 3: `Numeri.getCardinaleM` nella schermata finale («Avversari uccisi in N giorni»), `Articoli.inizialeMaiuscola` nel fumetto dell'incantatore, `TestoDeiModificatori.durata` (il `replaceFirst("^per ", "ancora ")` del DisplayableCanvasInventario è un trucco italiano), `AIUTO_ROTELLA` in `Cartiglio` (letto una volta sola all'avvio della classe: va bene finché la lingua si sceglie prima di creare la UI).
8. **Intermezzi e racconto** (M)
   - [x] il `Racconto` (storia, sconfitta, vittoria) è in `storia_it.properties` (12-foresta-motore, chiavi `INTRODUZIONE.n`, `FINALE_SCONFITTA.n`, `FINALE_VITTORIA.n`), letto con `Testi.get`; `RaccontoTest`;
   - [x] le battute e le didascalie degli intermezzi del modulo `07-foresta-intermezzi` (introduttivo, fine prima locazione, accampamento con le sue tre scene, notti dei negozi, riprogrammazione, spada nascosta, libro tecnico, saluti dei negozianti) sono in `intermezzi_<lingua>` (chiavi `Classe.n`, `ACCAMPAMENTO.scena.battuta`, `.M`/`.F` per il capo); `NegozioInScena` non porta più il plurale del negoziante, lo prende dal nome della classe. Le battute degli intermezzi delle missioni seguono con le missioni (passo 9); le battute da grammatica (oroscopo, locande) col passo 10.
9. **Missioni** (L, di gran lunga il passo più grosso: 808 stringhe)
   - [x] tutte le missioni (principali, incarichi, richieste, leggende, storie) e il loro `Mandante`: circa 490 chiavi in `missioni_<lingua>` (`Classe.N` per le battute delle scene, `.tN` per i metodi che restituiscono un testo, `.sN` per le altre frasi; `.M`/`.F` e `Uno`/`Molti` dove la frase cambia con il sesso o il numero). Le battute e le frasi sono state estratte con uno strumento e tradotte a mano: le parti `{n}` inserite dal codice (nomi di luoghi e persone, «a Nyena», «dal mercante di Ruuna») vengono ancora da altre classi e restano in italiano finché non sono localizzate a loro volta (vedi sotto);
   - [x] il contatore di `09-foresta-missioni` scende da 447 a 5;
   - [ ] da rifinire: i pezzi che le missioni compongono con preposizioni e articoli italiani (`mandanteSenzaVirgolaFinale`, `getNomeDeiNemici`, `TestiDeiLuoghi`: ora le locuzioni «una grotta» / «la grotta» sono chiavi, ma le preposizioni «a», «da» davanti al nome di città passano ancora da `conPreposizione`); le note dei modificatori («la lealtà» di `LealtaRichiesta`, che è anche un'etichetta salvata); i formati dei leggendari costruiti a stringa (`ForgiaRichiesta.s1`, `LaSfidaDeiCampioni.s1`: la chiave porta tutto il record, la traduzione ne cambia i soli valori); i parametri delle missioni che vengono dalle grammatiche (`missioni.txt`, passo 10).
10. **Grammatiche** (L, più per la scrittura che per il codice)
    - [ ] caricamento per lingua con ripiego sull'italiano, e `_pp` per lingua;
    - [ ] in ordine di visibilità: `nomi`, `artefatti2` e `leggendari` (i nomi degli oggetti), `locande`, `missioni`, `tappe`, `fiabe`, `oroscopo`, e i piccoli `templi`, `rovine`, `sfide`.
11. **Salvataggio e lingua** (S)
    - [ ] la lingua nella testata, ripristinata al caricamento, scelta alla nuova partita; test.
12. **Passata finale** (M)
    - [ ] partita completa in inglese, controllo degli spazi (le righe inglesi sono più corte, ma i nomi delle classi no), ortografia, e togliere il ripiego sull'italiano dal test di completezza.

#### File da ricontrollare

Bozze scritte da Claude il 2026-11-01 (passo 2); spuntare `[x]` quando rivisto. Ogni `_en` sta accanto al suo `_it`. L'inglese britannico (`_en_GB`) è ricavato per regole (`armor`→`armour`, `artifact`→`artefact`, `-ize`→`-ise`, `valor`→`valour`, `zip code`→`postcode`, `savior`→`saviour`…) e ha solo le righe che cambiano.

- [ ] `tipo_attributo_en.properties` (02-foresta-attributi): Soggezione→Awe, Contrattazione→Haggling, Furtività→Stealth
- [ ] `comandi_en.properties` (02-foresta-comandi): descrizioni alla terza persona; le voci GUERRIERA/GUERRIERO… sono uguali
- [ ] `tipo_effetto_di_stato_en.properties` (02-foresta-effettidistato)
- [ ] `tipo_locazione_en.properties` (02-foresta-mappa): «the city of Nyena»: articolo minuscolo come in italiano
- [ ] `tipo_danno_en.properties` (03-foresta-danni): Contundente→Blunt, Tagliente→Slashing
- [ ] `tipo_interazione_con_effetti_di_stato_en.properties` (03-foresta-danni): nomi tecnici adattati (Vampata Tossica→Toxic Flare, Colpo di Grazia→Coup de Grace…)
- [ ] `grado_incantamento_en.properties` (04-foresta-artefatti)
- [ ] `tipo_artefatto_en.properties` (04-foresta-artefatti): UTILIZZO è il verbo di «X wields/wears/carries Y», alla terza persona singolare
- [ ] `tipo_motivo_rifiuto_incantatura_en.properties` (04-foresta-artefatti)
- [ ] `tipo_negozio_en.properties` (04-foresta-artefatti)
- [ ] `tipo_rarita_artefatto_en.properties` (04-foresta-artefatti)
- [ ] `caratteristica_di_classe_en.properties` (05-foresta-personaggi)
- [ ] `note_personaggi_en.properties` (05-foresta-personaggi): 795 note (577 testi diversi), il file lungo; il genere ("he"/"she"/"they") segue quello dell'originale e va uniformato col passo 3
- [ ] `rifiuti_equipaggiamento_en.properties` (05-foresta-personaggi)
- [ ] `categoria_personaggio_en.properties` (05-foresta-personaggi)
- [ ] `tipo_incantesimo_en.properties` (05-foresta-personaggi): NOME_SINGOLARE/NOME_PLURALE senza articolo ("Air spell"): gli articoli arrivano con il passo 3
- [ ] `tipo_personaggio_en.properties` (05-foresta-personaggi): i nomi delle classi non hanno genere (Guerriera/Guerriero→Warrior, Elfa/Elfo→Elf, Maga/Mago→Mage, Chierica/Chierico→Cleric, Druida/Druido→Druid, Ladra/Ladro→Thief); da decidere se la scelta della classe deve distinguere i sessi
- [ ] `tipo_azione_en.properties` (06-foresta-combattimento)
- [ ] `tipo_abilita_en.properties` (07-foresta-poteri)
- [ ] `tipo_canto_en.properties` (07-foresta-poteri)
- [ ] `testo_tattica_en.properties` (08-foresta-tattiche): il linguaggio delle tattiche; `azione.ARMA_DI` è «{0} ({1} damage)»
- [ ] `tipo_filtro_en.properties` (08-foresta-tattiche)
- [ ] `tipo_ordinamento_en.properties` (08-foresta-tattiche)
- [ ] `tipo_predicato_en.properties` (08-foresta-tattiche)
- [ ] `tipo_rosa_en.properties` (08-foresta-tattiche)
- [ ] `tipo_trofeo_en.properties` (10-foresta-trofei): nomi adattati, non tradotti (Perdigiorno→Idler, Ammazzagoblin→Goblin slayer, Trafficone→Tinkerer, Trovarobe→Scavenger, Sbevazzone→Tippler, Vero Rompiscatole→Real Pain in the Neck, Più grosso è, più rumore fa→The Bigger They Are, the Louder They Fall…)
- [ ] `storia_en.properties` (12-foresta-motore): la frase finale dell'introduzione («…a dreadful dive known as») prosegue col nome del gioco
- [ ] `descrizione_personaggio_en.properties` (05-foresta-personaggi): la frase della scheda; `tipo_personaggio_en` ha ora anche `NOME_SINGOLARE`/`NOME_PLURALE`/`PRONOME` (nomi in-game, pronomi He/She/It); `tipo_bottino_en`/`_en_GB` (08-foresta-bottino): nomi e articoli degli oggetti trovati («suit of armor», «a pair of greaves»)
- [ ] `generatore_artefatti_en`/`_en_GB`, `nomi_artefatti_en`/`_en_GB`, `tattiche_base_en`/`_en_GB`, `direzioni_en`, `linea_temporale_en`, `gruppo_en`; le aggiunte a `testo_tattica_en` (problemi di una voce)
- [ ] `messaggi_personaggi_en`, `messaggi_combattimento_en`, `messaggi_poteri_en`, `testo_modificatori_en`: la narrazione. Nelle frasi in cui l'italiano distingue maschio e femmina (`.M`/`.F`) l'inglese ha due righe uguali, salvo «his»/«her» nella resurrezione
- [ ] `offerte_en`/`_en_GB`, `tipo_consumabile_en`, `messaggi_motore_en`/`_en_GB`: la narrazione delle locazioni e le offerte; in `messaggi_motore_en` le frasi di `Locanda.vecchiaAmicizia` e di resa/fuga hanno ancora qualche «him/her/it» da uniformare
- [ ] `intermezzi_en`/`_en_GB` (07-foresta-intermezzi): le battute; i riferimenti informatici (Unix `ls -a`, Core Dump, «The C Programming Language») sono lasciati com'erano, quelli a Kafka/Apuleio e alla stazione da battaglia adattati (golden ass, «That's no moon»)
- [ ] `missioni_en`/`_en_GB` (09-foresta-missioni): circa 490 frasi, tradotte senza vedere il contesto di ogni `{n}`: controllare soprattutto i dialoghi, i nomi delle missioni (`.t1`) e le frasi con più segnaposto (`IlCorriere`, `LaForgia`, `IlTorneo`, `LaSorveglianza`, `RichiestaDiMateriali`), dove in inglese ho riordinato i segnaposto o lasciato cadere quelli per gli accordi di genere dell'italiano
- [ ] `ui_en.properties` (12-foresta-swing): ora circa 225 chiavi (passo 7: tattiche, negozi, inventario, annunci, azioni del combattimento); `ui_en_GB` ha solo due voci. Le frasi con il bersaglio del riquadro del combattimento (`AZIONE_*_SU`) hanno il nome già articolato in italiano finché non arriva il passo 3

Inglese britannico (guardare solo se le differenze sono quelle volute):

- [ ] `tipo_attributo_en_GB.properties` (02-foresta-attributi)
- [ ] `comandi_en_GB.properties` (02-foresta-comandi)
- [ ] `tipo_effetto_di_stato_en_GB.properties` (02-foresta-effettidistato)
- [ ] `tipo_interazione_con_effetti_di_stato_en_GB.properties` (03-foresta-danni)
- [ ] `tipo_artefatto_en_GB.properties` (04-foresta-artefatti)
- [ ] `tipo_motivo_rifiuto_incantatura_en_GB.properties` (04-foresta-artefatti)
- [ ] `tipo_negozio_en_GB.properties` (04-foresta-artefatti)
- [ ] `note_personaggi_en_GB.properties` (05-foresta-personaggi)
- [ ] `tipo_incantesimo_en_GB.properties` (05-foresta-personaggi)
- [ ] `tipo_personaggio_en_GB.properties` (05-foresta-personaggi)
- [ ] `tipo_azione_en_GB.properties` (06-foresta-combattimento)
- [ ] `tipo_canto_en_GB.properties` (07-foresta-poteri)
- [ ] `testo_tattica_en_GB.properties` (08-foresta-tattiche)
- [ ] `tipo_trofeo_en_GB.properties` (10-foresta-trofei)
- [ ] `storia_en_GB.properties` (12-foresta-motore)

**Per il passo 1 e la storia (già committati):** `Testi.java` (catena di lingue, `formatta`, `sostituisci`; con `TestiTest`), `ConvenzioniDiLocalizzazioneTest` e `localizzazione_baseline.properties` (il riconoscitore di stringhe italiane è euristico: elenco di parole in `PAROLE`), `Main` (argomento `LINGUA=`), `TestiDellaUI` e `TestoTattica` (ora delegano a `Testi.formatta`), `Racconto` e `storia_it.properties`.

**Rischi.** L'articolo inglese "a/an" dipende dal suono ("a unicorn", "an hour"): serve una regola con un elenco di eccezioni nel file dei testi. Il passo 3 tocca più di cento file: è meglio farlo a moduli, con l'italiano invariato a ogni commit. Le grammatiche scherzose in italiano ("Il Cinghiale Vegano", i nani che protestano) non si traducono parola per parola: si adattano.

Se non si farà la versione inglese, restano utili comunque il passo 1 e il test del contatore.

### Costanti di bilanciamento come dati

Le costanti di bilanciamento (soprattutto quelle di `CostantiPersonaggi`) sono molte, in gran parte tabelle per classe (`GOBLIN_MAX_FORZA`, `GOBLIN_MOLTIPLICATORE_CRITICO`...). Metterle in una risorsa (properties o JSON) permetterebbe di bilanciare senza ricompilare. C'è un motivo in più: i `static final` primitivi vengono copiati nelle classi che li usano, quindi una compilazione incrementale parziale può tenere valori vecchi. È il lavoro più grande dell'elenco e ha senso solo se serve davvero il bilanciamento senza ricompilare.

### Ciclo di vita dell'app

- I singleton restano statici (`ModelloDati.getIstanza()` è usato in 64 file del motore). Una piattaforma che chiude l'app in background perde lo stato statico: `StatoDelMotore.azzera()` (che chiude l'automa e stacca il vecchio gruppo dal bus: poi se ne crea uno nuovo) e il salvataggio automatico lo coprono, ma non lo eliminano.
- Il salvataggio automatico si scrive solo quando l'app va in background in un punto sicuro (fra una locazione e l'altra). Un'app chiusa dal sistema a metà di una locazione riparte dall'ultimo salvataggio automatico, cioè dall'ultima volta che è andata in background in un punto sicuro (o da niente, e un salvataggio automatico di una sessione precedente resta). Scriverlo a ogni ingresso in `ATTESA_DIREZIONE` ridurrebbe la perdita, al costo di una scrittura per locazione.
- La UI Swing non chiama `inBackground()` e `inPrimoPiano()` (sul desktop non servono). La piattaforma mobile deve chiamarli dal suo ciclo di vita, e sospendere anche il temporizzatore della UI (`temporizzatoreUI` in `Main`).

### Uscite dal processo nella UI desktop

Sono della UI Swing e si sostituiscono con lei:

- `BufferedImageBuilder` pubblica `NotificaUscitaDalGioco(1, …)` e lancia un'eccezione se manca una risorsa grafica di base (fail fast, vedi `motore_grafico.md`); come si esce lo decide la piattaforma;
- la finestra si chiude con la X (`EXIT_ON_CLOSE`) senza passare dalla conferma di uscita né dal salvataggio.

Una UI mobile userà `NotificaUscitaDalGioco` per l'errore fatale e il ciclo di vita dell'app al posto del gancio.

### Punto d'ingresso e adattatori della piattaforma

`Main` (in `13-foresta-app-swing`) collega tutto e sceglie le implementazioni desktop: `EsecutoreSwing` per `ThreadPrincipale`, `TemporizzatoreJ2SE` (un `ScheduledExecutorService`), `GestoreSalvataggiSuFile`, `GestorePunteggiSuFile`, `GestoreTrofeiSuFile` e `GestoreBestiarioSuFile` (con `java.io.File`, non `java.nio.file`, che su Android chiede un'API recente), `System.exit` sulla `NotificaUscitaDalGioco`. `GestoreSuFile` prende la cartella dei file da `user.home` (l'unico `System.getProperty` del motore, più la cartella del log in `DestinazioneConsoleEFile`). Una piattaforma mobile ha bisogno di un suo punto d'ingresso che:

- installi un `Esecutore` per `ThreadPrincipale` sul thread della sua UI;
- dia ai gestori su file la cartella privata dell'app, e una `DestinazioneDiLog` adatta;
- chiami `ModelloDati.installa()`, `StatoDelMotore.registraComponenti()`, `RegistroTrofei.impostaGestoreTrofei`, `RegistroBestiario.impostaGestoreBestiario` come fa `Main`;
- gestisca la pressione sulla memoria e il thread di precaricamento (`Automa.precaricaInBackground` avvia un `Thread` normale, da verificare su Android).

## La UI Swing

`12-foresta-swing` è un renderer 2D fatto a mano su `BufferedImage` e `Graphics2D`, con quasi nessun widget Swing vero. `DisplayableCanvas` (un `JPanel`) disegna tutta la schermata in `paintComponent` e anima da un thread suo a 60 fotogrammi al secondo; il testo è tutto in font bitmap (solo 3 usi di `Font`). Sono 65 file su 99 a usare `java.awt` o `javax.swing`; gli altri 34 (eventi, tipi, testi, `BozzaTattica`) si riusano quasi così come sono.

| API | Occorrenze |
| :--- | ---: |
| `BufferedImage` | 498 |
| `Color` | 440 |
| `Graphics2D` | 185 (in 52 file) |
| `Image` | 156 |
| `Rectangle`, `Point` | 124, 70 |
| `AlphaComposite` | 43 |
| `RenderingHints` | 28 |
| `IndexColorModel` | 26 |
| `BasicStroke`, `RadialGradientPaint`, `ConvolveOp` | 13, 9, 8 |

### Da riscrivere (dipendono dal toolkit)

- **`ForestaUI`** (714 righe): crea il `JFrame` (con `EXIT_ON_CLOSE`) e calcola le dimensioni da `Toolkit.getScreenSize()`. Si tiene la parte che traduce una quarantina di eventi del bus in chiamate sul canvas.
- **`DisplayableCanvas`** (1.788 righe): il pezzo più grosso. È un `JPanel` con un thread di animazione, `paintComponent` e circa 300 righe di input da mouse (click, doppio click e rotella con `javax.swing.Timer`, trascinamento, passaggio del cursore). Si tiene l'idea (pila di `Finestra`, schermata corrente, coda di sprite e fumetti), non il codice. Il profilo temporaneo (`-Dforesta.profilo`) si può togliere.
- **`Prompt`** (un `JTextField` in un `JPanel`) è da rifare con la tastiera di sistema; `EsecutoreSwing` è l'adattatore da sostituire.
- **Immagini**: `BufferedImageBuilder` (`ImageIO.read`, ingrandimento con `NEAREST_NEIGHBOR`), `DimensioniRisorsa` (`ImageReader`), `ImageCache` (532 righe, campi `static BufferedImage` e una cache LRU di immagini composte e di testo: l'elenco dei file e la logica di cache si tengono, il tipo di immagine cambia).
- **Font Doomdark**: i glifi sono due immagini GIF (`img/font/doomdark_small.gif` e `doomdark_medium.gif`), che `ImmagineDelFont` legge una volta in una mappa carattere → `GlifoDelFont` (un livello da 0 a 255 per pixel, che è l'alfa del colore del testo, e la larghezza misurata sui pixel): la lettura usa `BufferedImage` e `ImageIO`, ma i glifi in sé sono dati puri e le immagini si portano tali e quali. `DoomdarkTextProducer` costruisce un `BufferedImage` pixel per pixel, con il colore preso da una tavolozza `IndexColorModel`, e `DoomdarkTextRectangle`/`2x` usano `MemoryImageSource` (via `DisplayableCanvasRiquadroTesto`), tutti AWT. `TestoGrande` usa `ConvolveOp` (l'alone) e `RescaleOp` (scurire). `DoomdarkColorModel` usa `IndexColorModel` solo per la tavolozza.
- **Sprite** (`SpriteBase` e derivati): disegnano in un buffer con `AlphaComposite`. `SpriteInDissolvenza` ha una sfocatura con `ConvolveOp`, `SpriteEffetto` una maschera con `SrcIn`, `SpriteRivelazioneArtefatto` (537 righe) usa `Path2D`, `Ellipse2D`, `RadialGradientPaint` e tratti.
- **`TracciatoreLogo`** (1.443 righe): traccia il logo 3AM con una scia animata, poligoni e tratti sovrapposti. È isolato e si può sostituire con un video, una sequenza di immagini o un'animazione precalcolata. `PannelloLogoIniziale` e le nuvole (`CloudGenerator`, `CloudInstance`, `CloudManager`, con un gradiente radiale) sono più semplici.
- **Riquadri, schermate, barra e componenti**: il contenuto (che cosa mostrare e quando) è portabile, ma le coordinate sono in pixel fissi, pensate per la finestra desktop. Riquadri: Locazione (551 righe), Gruppo (608), Combattimento (302), Incantesimi e pozioni (260), Mappa (146), Missioni (220), Statistiche (129), Testo (127), Sfida (329). Schermate: Intro/Outro (506), Intermezzo (366), Inventario (377), Mappa a tutto schermo (314), Scambiatore e le sue due versioni (381, 536, 456), Commerciante (126), Incantatore (162), Bestiario (173), Trofei (83). Barra: `DisplayableCanvasBarraIconeDock` (210, si ingrandisce sotto il cursore, quindi non ha senso sul tocco) e la classica (453). Componenti: `ComponenteScorrevole` (397) e `ScorrimentoVerticale` (207), da rifare con lo scorrimento tattile.
- **`DisplayableCanvasTattiche`** (1.203 righe): il pezzo più impegnativo per il tocco, con menu a discesa (`CasellaADiscesa`), liste scorrevoli e modifiche multiple. La logica non sta qui: `BozzaTattica` (la tattica in modifica) e `TestoTattica` (i testi) sono in `08-foresta-tattiche`, pacchetto `tattiche.modifica`, e le userà ogni front-end.

### Portabile quasi così com'è

`TestoAzioneInCombattimento`, `ComandiPossibili`, `CoordinateFumetto`, `Orientamento`, `AnimazioniIntermezzo`, `FontTool`, `TestiDellaUI`, `VersiDeiPersonaggi`, i sei eventi `InternoCreazioneSprite…` e `InternoNotificaViaFumettoATempo`; le tabelle `MappaBottinoImmagine`, `MappaPersonaggioImmagine` e `TipoIcona` (legano un tipo di gioco al nome di un'immagine: cambia il tipo dell'immagine, resta la tabella). `BozzaTattica` e `TestoTattica` non stanno più in questo modulo (vedi «Le classi di modifica delle tattiche»). `FinestraDiProva` (532 righe, `JTabbedPane`, `JSpinner`) è solo per chi sviluppa e non si porta.

### Risorse

327 immagini `gif` e 2 `png` (2,2 MB), a metà risoluzione e ingrandite all'avvio (`LivelloDiZoom`, 2,0 di default) con campionamento `NEAREST`, così il pixel-art resta nitido. Su una GPU 329 texture singole sono troppe: vanno raggruppate in atlanti.

### Cosa non si sistema con la sola riscrittura

- **Input**: oggi mouse, passaggio del cursore, doppio click e rotella. Sul tocco servono gesti (tocca, tieni premuto, trascina). I cartigli con la descrizione dei comandi vivono del passaggio del cursore, che non c'è: serve un altro gesto.
- **Layout**: tutte le coordinate sono in pixel assoluti, per una finestra orizzontale (la modalità `VERTICALE` è la stessa disposizione con la barra delle icone sul lato destro, non un layout verticale vero). Su un telefono, e su iPad, serve un layout nuovo: è lavoro di progetto della schermata, non di codice.
- **Modello di disegno**: il codice disegna in modo immediato su `Graphics2D`. In libGDX si disegna con uno `SpriteBatch` (e forme e gradienti con `ShapeRenderer` o texture precalcolate).
- **Effetti per pixel** (`ConvolveOp`, `RescaleOp`, `SrcIn`, `setRGB`): su GPU diventano shader o operazioni su `Pixmap` all'avvio.

### Dimensioni della finestra e dispositivi

**Oggi** (`ForestaUI.calcolaDimensioniFinestra`, orizzontale, zoom 2,0, finestra non ridimensionabile): la finestra è la somma delle cornici dei riquadri, senza superare lo schermo.

| Elemento | Originale | Zoom 2,0 |
| :--- | :---: | :---: |
| `CorniceMappa` | 124×124 | 248×248 |
| `Foresta` (bosco) | 195×160 | 390×320 |
| `CorniceGrande` | 184×144 | 368×288 |
| `CorniceIncantesimi` | 184×72 | 368×144 |
| Sfondo delle icone | 32×32 | 64×64 |

Larghezza 4+248+4+390+4+368+4 = **1022**, altezza 4+288+4+144+4+288+4+64+4 = **804** (rapporto 1,27). In modalità `VERTICALE` la barra delle icone sta sul lato destro e l'area di contenuto è la stessa dell'orizzontale: `calcolaDimensioniFinestra` dà una finestra più larga e più bassa di `SPESSORE_BARRA_ICONE` (72), e `calcolaAreaDiContenuto` sottrae la barra alla larghezza. La barra scorre in pixel fra due frecce fisse, anche col trascinamento e l'inerzia (vedi `motore_grafico.md` §3).

**Ipotesi di layout per i dispositivi** (non ancora nel codice): si riduce il testo a una striscia sotto il bosco (o quasi lo si toglie) e la mappa diventa un tile come gruppo, missioni e incantesimi. Colonna del bosco (390×320 più la striscia di testo) e due colonne di tile larghe 368 (gruppo e missioni da 288; mappa da 288 e incantesimi da 144), barra icone a tutta larghezza. Larghezza 4+390+4+368+4+368+4 = **1142**, altezza 4+288+4+288+4+64+4 = **656**, rapporto 1,74.

Le misure dei dispositivi sono in punti logici, in orizzontale, approssimate (da verificare sui modelli precisi). La scala è quella con cui il gioco entra intero mantenendo le proporzioni.

| Dispositivo | Schermo (pt) | Gioco | Rapporto schermo | Rapporto gioco | Scala | Gioco scalato (pt) | Icona da 64 → |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| Android piccolo | 800×360 | 1142×656 | 2,22 | 1,74 | 0,55 | 627×360 | 35 pt |
| iPhone SE (3ª gen.) | 667×375 | 1142×656 | 1,78 | 1,74 | 0,57 | 653×375 | 37 pt |
| iPhone 15 | 852×393 | 1142×656 | 2,17 | 1,74 | 0,60 | 684×393 | 38 pt |
| Android tipico | 915×412 | 1142×656 | 2,22 | 1,74 | 0,63 | 717×412 | 40 pt |
| iPhone 16 Pro Max | 956×440 | 1142×656 | 2,17 | 1,74 | 0,67 | 766×440 | 43 pt |
| iPad 10,2" | 1080×810 | 1142×656 | 1,33 | 1,74 | 0,95 | 1080×621 | 61 pt |
| iPad mini | 1133×744 | 1142×656 | 1,52 | 1,74 | 0,99 | 1133×651 | 63 pt |
| iPad Air/Pro 11" | 1180×820 | 1142×656 | 1,44 | 1,74 | 1,03 | 1180×678 | 66 pt |
| iPad Pro 13" | 1376×1032 | 1142×656 | 1,33 | 1,74 | 1,21 | 1376×790 | 77 pt |

- **iPad**: il gioco entra a scala circa 1,0, con una fascia nera sopra e sotto.
- **Telefoni**: il vincolo è l'altezza (656 px su 360–440 pt). Sui rapporti attorno a 2,2 restano bande nere ai lati; la scala va da 0,55 a 0,67.
- **Bersagli del tocco**: le icone scendono a 35–43 pt, sotto i 44 pt raccomandati da Apple. Si recupera ingrandendo la sola barra icone, o abbassando i tile (per tenere la scala sopra 0,7 serve un'altezza di circa 560 px: tile del gruppo più basso, mappa a 248).

## Inventario di `Graphics2D`

Chiamate fatte sulle variabili dichiarate `Graphics2D` o `Graphics` (52 file, circa 450 chiamate; per difetto, perché non si vedono quelle su variabili d'altro tipo):

| Metodo | Chiamate | File |
| :--- | ---: | ---: |
| `drawImage` | 208 | 48 |
| `dispose` | 46 | 28 |
| `setColor` | 41 | 17 |
| `setComposite` | 33 | 14 |
| `fillRect` | 26 | 16 |
| `setRenderingHint` | 14 | 10 |
| `setPaint` | 11 | 6 |
| `getComposite` | 10 | 8 |
| `fill` (forma) | 10 | 3 |
| `setClip` / `getClip` / `clipRect` / `clip` | 8 / 5 / 5 / 2 | 9 in tutto |
| `create` | 8 | 7 |
| `drawLine`, `setStroke`, `draw`, `drawRect`, `translate` | 5, 5, 5, 4, 3 | 2–4 |
| `fillRoundRect`, `fillOval`, `fillPolygon`, `drawRoundRect`, `getColor`, `getPaint` | 1–2 | 1–4 |

- **`drawImage` è metà di tutte le chiamate**, quasi sempre nella forma più semplice: 194 volte `(immagine, x, y, null)`, 12 con larghezza e altezza, una con ritaglio di origine e scala (`DisplayableCanvasRiquadroMappa`), una con un filtro (`SpriteInDissolvenza`).
- **Composizione**: `SRC_OVER` con alpha (dissolvenza) per quasi tutti i 33 usi; `DstIn` in `TracciatoreLogo` e `ScorrimentoVerticale`, `SrcIn` in `SpriteEffetto` e `ImageCache`, `Clear` in `ScorrimentoVerticale`: servono a mascherare e sono casi a parte.
- **Qualità**: interpolazione bilineare 6 volte, `NEAREST_NEIGHBOR` 2, antialiasing 5 (solo `CloudGenerator`, `TracciatoreLogo`, `SpriteRivelazioneArtefatto`), `STROKE_PURE` 1. In libGDX l'interpolazione è del filtro della texture, non del contesto di disegno.
- **Gradienti**: nove, in cinque file. I radiali in `TracciatoreLogo`, `SpriteRivelazioneArtefatto` e `CloudGenerator` (da precalcolare in un'immagine), i lineari in `Notiziario` e `ScorrimentoVerticale` (sfumatura dei bordi, ottenibile con un rettangolo e l'alpha).
- **Forme e tratti** (`Shape`, `setStroke`): solo `TracciatoreLogo`, `SpriteRivelazioneArtefatto`, `CloudGenerator` e `SpriteFumetto`.
- **Ritaglio**: nove file (cinque riquadri, tre schermate, `SpriteBase`), sempre rettangolare, per le liste scorrevoli.
- **`BufferedImage`**: `getWidth` 164 volte e `getHeight` 133; **`createGraphics` 38 volte in 25 file**, cioè molte immagini si compongono fuori schermo (immagini composte di `ImageCache`, sprite, icone): un'astrazione deve prevedere un'immagine in cui si può disegnare, non solo lo schermo (in libGDX un `FrameBuffer` o un `Pixmap`); `setRGB`/`getRGB` 6 e 5 volte (`DoomdarkTextProducer`, `TestoGrande`, `TracciatoreLogo`, `TipoIcona`, riquadri Testo e Locazione), quindi serve poter creare un'immagine da un array di pixel; `getSubimage` 5 volte (`TracciatoreLogo`, riquadro Combattimento, Intermezzo); `getScaledInstance` una (riquadro Sfida).
- **`Color`**: 64 `new Color(…)` e circa 170 costanti; dei metodi si usano `getRGB`, `getRed/Green/Blue`, `getAlpha` e una volta `darker`. `Rectangle` e `Point` compaiono in 22 file, con 24 `contains` per gli hit-test: tipi semplici da sostituire.
- **Fuori da `Graphics2D`**: `ConvolveOp` in `SpriteInDissolvenza` (sfocatura) e `TestoGrande` (alone), `RescaleOp` in `TestoGrande`, `MemoryImageSource` nei due `DoomdarkTextRectangle`.

Ne segue che l'uso reale è molto più ristretto della API: un'interfaccia con `drawImage` (con e senza scala, con ritaglio d'origine), `setColor` + `fillRect`/`drawRect`/`drawLine`, alpha con salvataggio e ripristino, ritaglio rettangolare, immagine vuota in cui disegnare, immagine da array di pixel, larghezza, altezza e sotto-immagine, interpolazione per immagine, coprirebbe circa 45 file su 52. Restano da trattare a parte, con una versione per piattaforma: `TracciatoreLogo`, `SpriteRivelazioneArtefatto`, `CloudGenerator`, `SpriteFumetto` (forme), `ScorrimentoVerticale` (maschera), `SpriteEffetto` (`SrcIn`), `SpriteInDissolvenza` e `TestoGrande` (convoluzioni).

### Stima

| Gruppo | Righe circa | Lavoro |
| :--- | ---: | :--- |
| Portabile quasi così com'è | 1.700 | basso |
| Layout e disegno di riquadri e schermate | 9.000 | alto: coordinate e input |
| Sprite, testo, immagini e radice | 5.500 | medio-alto |
| Dati dei font | 3.000 | basso: restano tabelle |
| `TracciatoreLogo` | 1.400 | isolato, sostituibile |
| `FinestraDiProva` | 500 | non si porta |

Le zone più rischiose, perché il resto ne dipende, sono `DisplayableCanvasTattiche`, l'input di `DisplayableCanvas` e il nuovo disegno del testo bitmap.

## iOS (iPhone e iPad)

Con libGDX l'iOS si raggiunge con **MobiVM** (il successore open source di RoboVM), che compila il bytecode in codice nativo prima del tempo, perché Apple vieta di generare codice a runtime. Il progetto libGDX standard ha già il modulo `ios`, e lo stesso codice di `core` serve iPhone e iPad (cambiano dimensioni e rapporto dello schermo, che la UI deve gestire). Costi in più rispetto ad Android: un Mac con Xcode (non si compila da Linux), l'account Apple Developer a pagamento, le regole dell'App Store. MobiVM ha una comunità piccola e segue con ritardo le versioni nuove di iOS e di Xcode: da controllare prima di impegnarsi (non è stato verificato). Il codice del progetto non usa reflection né `ServiceLoader`, che è il tipo di cosa che dà più noie alla compilazione in anticipo.

## Strada consigliata: libGDX al posto di Swing

Valutazione (non una decisione presa): portare l'esistente su libGDX, con il backend desktop (LWJGL3) che sostituisce Swing anche su PC e Mac, **senza strato di astrazione**. Motivi:

- un solo front-end da mantenere, invece di due da tenere allineati a ogni nuovo effetto o schermata;
- Swing non porta valore di gioco: mostra la stessa partita;
- non è una riscrittura da zero: si riusano la logica (`BozzaTattica`, `TestoTattica`, già nel modulo delle tattiche), i dati dei font, le tabelle di immagini, gli eventi, la struttura di `ImageCache` e i contenuti di ogni riquadro e schermata; si riscrivono il disegno, l'input e il collegamento con la piattaforma;
- il disegno è quasi tutto `drawImage` e rettangoli colorati (vedi l'inventario), che con uno `SpriteBatch` hanno un corrispondente quasi uno a uno;
- cambiare il front-end lasciando intatto il motore dimostra che la separazione per contratti funziona.

Come ridurre il rischio:

1. **Prima un prototipo, non la migrazione**: un modulo libGDX che carica le immagini, disegna la schermata iniziale e il testo bitmap, su desktop, emulatore Android e (se c'è un Mac) simulatore iOS. Da misurare: velocità di `ImageCache` con zoom 2,0, atlanti (329 texture singole sono troppe), composizione fuori schermo (`createGraphics` in 25 file).
2. **Prima orizzontale, poi verticale**: con una `FitViewport` a risoluzione virtuale fissa (quella attuale della finestra) le coordinate di riquadri e schermate si riusano quasi tutte, e il gioco sul telefono in orizzontale appare scalato. È meno bello di un layout vero (i bersagli del tocco sono piccoli), ma dà una versione giocabile molto prima; il layout per lo schermo verticale è un secondo passo, necessario in ogni caso.
3. **Non cancellare `12-foresta-swing`** finché libGDX non lo eguaglia: resta come riferimento per confrontare il comportamento. La cancellazione si decide a parte.
4. **Ordine di porting**: disegno e testo, intro e schermata di gioco, schermate a tutto schermo (inventario, mappa, negozi), tattiche per ultime (le più pesanti per il tocco). `TracciatoreLogo` (1.443 righe) conviene sostituirlo con un'animazione precalcolata; `FinestraDiProva` si può lasciare al desktop.

Rischi reali: l'input a tocco (cartigli senza passaggio del cursore, liste da scorrere col dito, tattiche) richiede progettazione e non solo codice; gli effetti per pixel (sfocatura, alone del testo grande, maschere) vanno rifatti con shader o precalcolati; iOS dipende da MobiVM, di cui va controllato lo stato; il costo resta alto (settimane, non giorni), e senza prototipo non si può stimarlo sul serio. Si cambierebbe idea (strato sottile di astrazione, vedi sotto) se le prestazioni del disegno immediato non bastassero o se Swing dovesse restare il front-end ufficiale.

## Convivenza di Swing e libGDX

Si possono tenere entrambi i front-end e decidere dopo quale usare: `12-foresta-swing` dipende solo da `11-foresta-motore-api` e `08-foresta-tattiche` e non vede il motore, quindi un nuovo modulo (per esempio `foresta-libgdx`) con le stesse dipendenze non lo disturba. Il collegamento sta in `Main` (`13-foresta-app-swing`), l'unico che conosce insieme motore e UI: basta un secondo punto d'ingresso (un modulo `foresta-app-libgdx` o una classe a parte), e il motore non cambia, perché pubblica e legge eventi e viste senza sapere che UI c'è sopra.

Cose a cui fare attenzione:

1. **Il toolkit grafico resta confinato**: `DipendenzeDalToolkitGraficoTest` oggi vieta `java.awt`, `javax.swing`, `javax.imageio` e `javax.sound` fuori da `12-foresta-swing`; va esteso perché ammetta libGDX solo nel suo modulo e vieti a quel modulo AWT e Swing.
2. **Classi di `12-foresta-swing` che non sono di Swing** (i dati dei font, le tabelle di immagini; `BozzaTattica` e `TestoTattica` sono già state spostate): il modulo libGDX non può dipendere da `12-foresta-swing`, perché porterebbe dentro AWT. Le strade: copiarle (semplice, ma due copie da tenere allineate) oppure spostarle in un modulo comune (più pulito, ma cambia la forma del codice e va discusso prima). Via di mezzo: partire copiando e spostare solo quando si vede che cosa si duplica davvero. Per le classi di modifica delle tattiche vedi il paragrafo seguente.
3. **Ogni novità di UI va fatta due volte** finché i due front-end vivono: conviene una regola presto (per esempio le novità si fanno solo nel front-end scelto, l'altro resta com'è).
4. **Build**: il progetto è tutto Maven, ma i moduli Android e iOS di libGDX di solito si costruiscono con Gradle (il desktop si può fare con Maven). Va deciso come far convivere i due sistemi, per esempio un progetto Gradle separato che usa i jar del progetto Maven.
5. **Documentazione**: ogni modulo nuovo ha il suo `foresta-<modulo>.md`, con la sezione delle dipendenze uguale ai `pom.xml`.

Il prototipo dà i dati per scegliere: quando libGDX mostra la schermata di gioco si confrontano prestazioni, aspetto e costo di manutenzione, e si decide se tenere entrambi, passare a libGDX o fermarsi.

### Le classi di modifica delle tattiche

Fatto: `BozzaTattica` e `TestoTattica` (che non toccavano il toolkit grafico) sono passate da `12-foresta-swing/…/ui/tattiche` a `08-foresta-tattiche/…/tattiche/modifica`, rese pubbliche (la schermata le usa in circa 66 punti), insieme ai loro test (`BozzaTatticaTest`, `TestoTatticaTest`); `RinominaDellaVoceTest` è rimasto in `13-foresta-app-swing` e importa la bozza dal nuovo posto. Le parole di `TestoTattica`, che prima erano nel codice («minore di», «a caso», «il più potente», le spiegazioni del riquadro destro) e il nome predefinito di una voce con il motivo per cui una bozza non si conferma (`BozzaTattica`) sono ora in `08-foresta-tattiche/src/main/resources/…/testi/testo_tattica_it.properties`, letti con `Testi.get` (ripiego sull'italiano, `{0}`, `{1}`… al posto dei valori); `TestiDiTestoTatticaTest` controlla che ogni valore spiegato abbia la sua chiave. Dettagli in `08-foresta-tattiche.md`.

`CasellaADiscesa` **resta nella UI**: usa `java.awt`, `ComponenteScorrevole`, `ImageCache` e i font Doomdark, quindi è un pezzo di disegno e va riscritta per ogni front-end.

## Osservazioni

- Il grosso del porting resta la UI Swing, da riscrivere, e l'input. I `Comando` astratti aiutano, ma i cartigli al passaggio del mouse e le scorciatoie da tastiera vanno ripensati per il tocco.
- Per un gioco 2D che resta in Java la scelta più naturale è **libGDX**: un solo codice di UI per desktop (LWJGL3), Android e iOS (tramite MobiVM), che può usare direttamente i jar di motore e contratti. L'alternativa è J2ObjC per il motore, con una UI nativa separata per ciascuna piattaforma: vorrebbe dire scrivere la UI due volte.
- **Uno strato fra la UI e il toolkit.** Se Swing deve restare come front-end del desktop, ha senso un modulo di UI comune (riquadri, schermate, sprite, logica di scorrimento e di selezione) che disegna su un'interfaccia sottile, con un backend Swing e uno libGDX; è nello spirito dei sottoprogetti del progetto. Se invece libGDX sostituisce Swing anche sul desktop, lo strato non serve: si porta tutto in una volta. Condizioni perché lo strato valga: tenerlo sottile (un'operazione entra solo se serve in almeno un riquadro o una schermata: gli otto file speciali restano uno per backend), perché il rischio è un'interfaccia che finisce per imitare `Graphics2D`; accettare due backend da mantenere; mettere in conto che il refactor dei circa 9.000 righe di riquadri e schermate costa quasi quanto una riscrittura parziale. Non risolve il layout, l'hover, il `Prompt` e le prestazioni.

## Da fare / idee aperte

- **Decidere** se Swing resta come front-end del desktop (strato di astrazione, backend Swing e libGDX) o se libGDX lo sostituisce; nel frattempo i due possono convivere (vedi «Convivenza di Swing e libGDX»), e il prototipo dà i dati per scegliere.
- Estendere `DipendenzeDalToolkitGraficoTest` quando nascerà il modulo libGDX, e decidere come far convivere Maven e Gradle (moduli Android e iOS).
- Se si fa lo strato: introdurlo prima dentro Swing, senza cambiare comportamento (il gioco resta funzionante a ogni passo), poi aggiungere il backend libGDX partendo da un riquadro e da una schermata.
- Un **prototipo libGDX** (un'immagine e la schermata iniziale, su desktop, emulatore Android e simulatore iOS) per misurare le prestazioni: composizione fuori schermo (`createGraphics` in 25 file), atlanti, memoria di `ImageCache` con lo zoom 2,0, thread di precaricamento su Android.
- **Disegnare il layout** per il telefono (tile al posto del riquadro mappa, testo ridotto: vedi `porting_libGDX.md` §9) e per il tocco (gesti, sostituto dell'hover, tastiera per `Prompt`). La barra icone ha già il trascinamento con inerzia e la modalità con la colonna a destra.
- Scrivere il punto d'ingresso della piattaforma (vedi sopra) e adattare gli `Esecutore`/`Temporizzatore`/cartella dei file.
- **Versione inglese**: i passi sono in «Testi italiani nel codice»; il primo è l'infrastruttura (argomento `LINGUA=`, `Testi.formatta`, test di completezza e contatore delle stringhe italiane).
- Decidere se togliere dal codice il profilo temporaneo di `DisplayableCanvas` e, per iOS, controllare lo stato attuale di MobiVM.
- Il costo dei `InternoMessaggio` costruiti a ogni passo della macchina a stati è in [`todo.md`](todo.md).
