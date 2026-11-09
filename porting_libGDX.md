# Piano di porting su libGDX (Android e famiglia Apple: iMac, iPhone, iPad)

Stato al 2026-11-06 (il piano è del 2026-10-31). È un piano, non una decisione presa: dice in che ordine fare le cose se si porta la Foresta su Android e iOS. Il «cosa manca» e i numeri sul codice Swing stanno in [`assessment_portabilita.md`](assessment_portabilita.md); qui c'è solo l'ordine dei lavori.

## 0. A che punto siamo (2026-11-06)

- **Fatto**: Fase 1 (`EsecutoreGdx`, `MainLibgdx` collegato al motore con `AvvioDelMotore`, ciclo di vita con `pause`/`resume`, uscita con `Gdx.app.exit()`; mancano la cartella privata e il log per Android, con la Fase 7), Fase 2, Fase 3 (lo schermo di gioco con tutti i riquadri, la barra e gli sprite, `SchermoGdx`, collegato al motore da `ForestaLibGDX`: si gioca una partita) e Fase 5 tranne il logo. Il campo di testo (`CampoDiTestoGdx` sul `CampoDiTesto` comune) c'è, anche con la tastiera di sistema.
- **Android (2026-11-06)**: il prototipo `foresta-android/` (Gradle, fuori da Maven; vedi [`foresta-android.md`](foresta-android.md)) compila e **gira su un Realme Pad** (tablet, Android 11) a circa 60 fps; la correzione dei bordi rossi sugli atlanti (`SpriteBatchDiPrecisioneAltaGdx`) viene da lì. Il tocco e una partita intera non sono stati ancora provati.
- **Messo in comune** (12bis): la radice e la traduzione degli eventi (`ForestaUI`), lo schermo senza disegno (`SchermoDelGioco`), la disposizione (`DisposizioneDelloSchermo`), il campo di testo (`CampoDiTesto`), gli argomenti (`OpzioniDiAvvio`), il collegamento del motore (`AvvioDelMotore`, in `12-foresta-motore`).
- **Resta**: la finestra di prova è solo per il desktop e non si porta; poi le Fasi 6, 7 e 8 e i punti aperti della Fase 4 (sezione 7). Fatti anche la Fase 4 (input a tocco: gesti, pressione prolungata, doppio tocco, area sensibile della barra, tasto indietro), il logo iniziale (`LogoInizialeGdx`, sulla logica comune `AnimazioneDelLogo`), `TUTTOSCHERMO` e `SALTALOGO`, e i cartigli dell'aiuto di gruppo e missioni, la sfida che si dissolve, l'alfabeto grande a colori, l'ascolto delle rivelazioni degli artefatti e l'orientamento verticale (`VERTICALE`: la barra delle icone sul lato destro).

## 1. Perché questo piano, e che cosa non è in discussione

- **La riscrittura della UI è inevitabile.** Swing e AWT non esistono né su Android né su iOS. Qualunque strada porti la Foresta su un telefono passa dal riscrivere `12-foresta-swing` (disegno, input, immagini, testo) e `13-foresta-app-swing` (punto d'ingresso). Motore e moduli di dominio non si toccano: pubblicano eventi e viste senza sapere che UI c'è sopra.
- **libGDX è il mezzo consigliato, non l'unico.** Un'UI nativa solo per Android (`Canvas`/`View`) esiste, ma coprirebbe Android e basta, e per iOS andrebbe riscritta ancora. libGDX dà un solo codice di UI per desktop, Android e iOS (tramite MobiVM) e usa direttamente i jar del progetto.
- **Il disegno si presta.** È quasi tutto `drawImage` (208 chiamate su circa 450) e rettangoli colorati, che con uno `SpriteBatch` hanno un corrispondente quasi uno a uno.
- **Che cosa si riusa**: logica (`BozzaTattica`, `TestoTattica`), immagini dei font Doomdark (`doomdark_small.png` e `doomdark_medium.png`) con la loro lettura in `ImmagineDelFont`, tabelle di immagini (`MappaBottinoImmagine`, `MappaPersonaggioImmagine`, `TipoIcona`), eventi, struttura di `ImageCache`, contenuto di ogni riquadro e schermata. Si riscrivono disegno, input e collegamento con la piattaforma.

## 2. Decisioni prese e da prendere

Prese (dall'utente, 2026-10-31):

1. **Build**: si resta con Maven. Quando servirà, la versione Gradle (per i moduli Android e iOS) si costruirà **in più**, partendo da quella Maven, che resta la principale.
2. **Moduli**: `12-foresta-libgdx` per il front-end, `13-foresta-app-libgdx` per il punto d'ingresso desktop. I moduli Android e iOS, se stanno fuori da Maven, non entrano nella numerazione.
3. **Codice comune (cambiata il 2026-11-06)**: niente copie. Quel che non dipende dal toolkit sta in `12-foresta-ui-comune` (le immagini, e man mano la logica d'interfaccia che non disegna), usato da `12-foresta-swing` e da `12-foresta-libgdx`. Swing resta come alternativa e ogni pezzo di UI si porta anche in libGDX.
4. **Orientamento** (confermata il 2026-11-06): il gioco resta **orizzontale**, niente layout verticale vero. Sul telefono si usa la disposizione `VERTICALE` (la barra delle icone sul lato destro, che stringe l'altezza), e **l'utente deve ruotare il telefono in orizzontale**: l'orientamento non cambia mentre il gioco è aperto, si sceglie una volta all'avvio (`MainLibgdx VERTICALE`, `ForestaLibGDX(orientamento, …)`, funzionante anche in libGDX). Quindi su Android l'attività si forza in orizzontale (Fase 7) e non serve nessuna gestione del cambio di orientamento.
5. **Layout per il telefono**: prima prova, la sola barra icone piazzata in verticale sul lato destro, per stringere l'altezza e allargare la lunghezza (vedi la sezione 9). **Fatta in Swing** (2026-10-31): la modalità `VERTICALE` funziona, con la barra che scorre in pixel fra due frecce fisse, col trascinamento e l'inerzia.
6. **Android**: `minSdk` 24 (Android 7.0), dove lambda e stream di Java 8 funzionano senza desugaring particolare.
7. **Logo iniziale**: la logica di `TracciatoreLogo` è stata estratta nel modulo comune (`AnimazioneDelLogo`) e libGDX la disegna con `LogoInizialeGdx`: fedele all'originale e senza fotogrammi precalcolati, ma con la torcia e le scie calcolate sui pixel (`TorciaDelLogo`, `ScieDelLogo`, `TestaDellaCometa`) invece che con `ShapeRenderer`, che non ha gradienti radiali né composizione `DstIn`.
8. **Due front-end**: finché vivono entrambi, ogni novità di UI si fa in **tutti e due** (Swing resta alla pari).
9. **Cartigli sul tocco**: la descrizione dei comandi compare con la **pressione prolungata**; il tocco normale esegue il comando.
10. **Fase 4, decisioni del 2026-11-06**:
    - la pressione prolungata **serve solo a leggere**: sollevando il dito si chiude la descrizione, senza eseguire niente;
    - il **doppio click** diventa il **doppio tocco**, com'era già riconosciuto (scambi, mappa, tattiche, colonna del personaggio);
    - i **bersagli piccoli** si risolvono allargando solo l'area sensibile, non il disegno;
    - il tasto **indietro** chiude la finestra in primo piano e, dalla schermata di gioco, apre il salvataggio che propone l'uscita (con la conferma).

Presa il 2026-11-06: l'atlante dei glifi si ricava **all'avvio** dall'immagine del font (è quello che fa già `TestoGdx`: una texture sola per font, costruita dai dati di `DoomdarkFont`; niente passo in fase di build, né file delle larghezze).

Da prendere: l'altezza target del layout (dopo la Fase 3).

Preso il 2026-11-06: le piattaforme sono **Android e tutta la famiglia Apple** (macOS su iMac e Mac, iOS su iPhone e iPad); le immagini sono tutte PNG (convertite dalle GIF, nessuna era animata) e si raggruppano in **atlanti** (icone, mappa, oggetti fatti; poi personaggi, locazioni e fondi) usati da entrambe le interfacce, e gli originali **non si buttano via**: stanno in `12-foresta-ui-comune/sorgenti-immagini/` e non entrano nei jar; la `FinestraDiProva` si porta anche (solo desktop); durante il porting si lanciano solo i test di `risorse_e_documenti_vari/test_porting_ui.txt`.

## 3. Fase 0: prototipo (non la migrazione)

Obiettivo: misurare, non consegnare. Un modulo libGDX che su desktop, emulatore Android e (con un Mac) simulatore iOS:

- carica le immagini con lo zoom 2,0 e disegna la schermata iniziale;
- disegna una riga di testo con il font bitmap Doomdark;
- apre una `FitViewport` 1142×656 e mostra due o tre riquadri finti.

Da misurare e da annotare qui:

| Domanda | Come si misura |
| :--- | :--- |
| 329 texture singole sono troppe? Quanto serve raggrupparle in atlanti? | tempo di caricamento e memoria con e senza atlanti |
| La composizione fuori schermo regge? (`createGraphics` in 25 file) | immagini composte di `ImageCache` con `FrameBuffer` o `Pixmap` |
| Quanta memoria usa `ImageCache` a zoom 2,0 su un telefono vero? | memoria dell'app su un dispositivo di fascia bassa |
| Il thread di precaricamento (`Automa.precaricaInBackground`) regge su Android? | prova sul dispositivo |
| Le lambda e gli stream di Java 8 compilano su Android (API 24 o desugaring) e su MobiVM? | compilazione dei jar del motore nel modulo |
| MobiVM funziona con l'Xcode installato? | «hello world» libGDX sul simulatore iOS |

Esito: si decide se proseguire con libGDX, con uno strato sottile di astrazione, o fermarsi. Senza questo passo il costo (settimane, non giorni) non si stima sul serio.

## 4. Fase 1: infrastruttura della piattaforma

Prima di disegnare schermate serve il punto d'ingresso, perché il motore parta e si salvi.

1. **`ThreadPrincipale`**: un `Esecutore` che porta il lavoro sul thread di rendering di libGDX (`Gdx.app.postRunnable`), al posto di `EsecutoreSwing`.
2. **Temporizzatore**: `TemporizzatoreJ2SE` usa un `ScheduledExecutorService` e dovrebbe andare tale e quale; il temporizzatore della UI invece si sostituisce con il ciclo di `render`, e va sospeso in background.
3. **File e log**: i gestori su file (`GestoreSalvataggiSuFile` e gli altri tre) usano `java.io.File`, e `GestoreSuFile` prende la cartella da `user.home`: va data la cartella privata dell'app. Serve una `DestinazioneDiLog` adatta (Logcat su Android).
4. **Collegamenti come in `Main`**: `ModelloDati.installa()`, `StatoDelMotore.registraComponenti()`, `RegistroTrofei.impostaGestoreTrofei`, `RegistroBestiario.impostaGestoreBestiario`.
5. **Ciclo di vita**: chiamare `inBackground()` e `inPrimoPiano()` dell'automa dagli eventi `pause`/`resume` di libGDX; il salvataggio automatico si scrive solo in un punto sicuro (fra una locazione e l'altra), quindi un'app uccisa a metà locazione riparte dall'ultimo.
6. **Uscita**: `NotificaUscitaDalGioco` per l'errore fatale, niente `System.exit` (lo controlla `ConvenzioniDiPortabilitaTest`).

## 5. Fase 2: immagini, atlanti e testo

È il blocco da cui dipende tutto il resto.

1. **`ImageCache`**: tenere l'elenco dei file e la logica della cache LRU; cambia il tipo (da `BufferedImage` a regione di texture). I campi `static BufferedImage` diventano texture da rilasciare con il ciclo di vita.
2. **Atlanti**: raggruppare le 329 immagini per tipo (locazioni, personaggi, icone, bottino…). Il ridimensionamento con zoom 2,0 e campionamento `NEAREST` resta (`TextureFilter.Nearest`), così il pixel-art resta nitido.
3. **Font Doomdark: testo glifo per glifo, niente cache di scritte.** Oggi `ImageCache` costruisce ogni scritta alla prima richiesta (`get(testo, font, colore)`) e la tiene in `cacheDinamica` con `WeakReference<Image>`; un thread «reaper» toglie le voci svuotate dal GC. Lo stesso per i personaggi specchiati (`immaginiPersonaggiPerVerso`). In libGDX non regge:
   - **Il GC non libera le texture**: vivono nella memoria della scheda grafica, e senza `dispose()` esplicito restano occupate. Con i riferimenti deboli le scritte si accumulerebbero, e non si saprebbe quando liberarle.
   - **Una texture per scritta** costa un cambio di texture nello `SpriteBatch` per ogni scritta e un caricamento sulla GPU, possibile solo sul thread grafico (oggi `ImageCache` costruisce anche da altri thread).
   - Le scritte sono tante e quasi tutte usa e getta (narrazione, nomi, numeri dei danni).

   Si fa invece come un `BitmapFont`: all'avvio si costruisce **un atlante dei glifi** (uno per font, e per colore se serve) e ogni scritta si disegna come una sequenza di sprite, uno per lettera, nello stesso lotto dello `SpriteBatch`. Niente cache dinamica, riferimenti deboli o reaper per i testi, niente da liberare. Si riusano la divisione in righe (`FontTool.split`) e le larghezze dei glifi, che sono logica pura. I glifi li dà già l'**immagine del font**: `img/font/doomdark_small.png` e `doomdark_medium.png`, con ogni glifo in un riquadro di una griglia fissa (i bordi rossi, che servono solo a chi disegna e a chi legge, e l'elenco dei caratteri nell'ordine in `CaratteriDelFont`). Il livello di grigio di ogni pixel è l'alfa del colore del testo (nero puro trasparente, bianco il colore pieno), e la larghezza di ogni glifo è misurata sui pixel quando si apre l'immagine (`ImmagineDelFont`). In libGDX la lettura della griglia si riscrive sul `Pixmap` (è pochissima logica: il calcolo dei riquadri e delle larghezze) oppure si fa in fase di build, scrivendo l'atlante e un file con le larghezze. `DoomdarkTextProducer` e `DoomdarkTextRectangle` (che usano `BufferedImage`, `IndexColorModel`, `MemoryImageSource`) non si portano: li sostituisce il disegno per glifi.
   - **Colori**: il glifo è bianco con alfa, e il colore del testo (`DoomdarkColorModel`) si applica come tinta: basta un solo atlante per font con `batch.setColor(...)`, perché l'alfa dei pixel sfumati si moltiplica con quella del colore, come fa oggi `DoomdarkTextProducer.pixel`. Le scritte con ombra sono due passaggi (nero spostato di 2 pixel e colore sopra), uguali a oggi.
   - **Righe in più**: la riga di testo è alta quanto l'immagine intera (corpo più le righe per accenti e cediglie: 13 nel piccolo, 24 nel medio); chi posiziona un testo in un punto fisso toglie `getRigheInCima()` dalla quota. Valgono le stesse regole di impaginazione di Swing.
4. **Testo grande**: `TestoGrande` usa `ConvolveOp` (alone) e `RescaleOp` (scurire): si precalcola in un `Pixmap` o si fa con uno shader. Sono pochi testi, quasi sempre gli stessi. Dove una scritta composta è troppo pesante da ridisegnare a ogni fotogramma resta una piccola cache con un limite di voci e `dispose()` esplicito quando una voce esce (mai riferimenti deboli).
   - **Personaggi specchiati**: non servono immagini a parte (`immaginiPersonaggiPerVerso`); si disegna la stessa regione con larghezza negativa o con `TextureRegion.flip`.
5. **Composizione fuori schermo**: un'immagine in cui si può disegnare (`FrameBuffer` per ciò che si ricompone a ogni fotogramma, `Pixmap` per ciò che si calcola una volta).

## 6. Fase 3: schermata di gioco in orizzontale

1. **`FitViewport` a risoluzione virtuale fissa** (quella del layout scelto). Le coordinate di riquadri e schermate si riusano quasi tutte; sul telefono il gioco appare scalato.
2. **Riquadri** nell'ordine in cui servono per vedere una partita: Locazione, Gruppo, Testo (ridotto), Mappa (come tile), Missioni, Incantesimi e pozioni, Statistiche; poi Combattimento e Sfida.
3. **Barra icone**: il modello è comune (`BarraIcone`, `BarraIconeDock`) e libGDX la disegna con `BarraIconeGdx`. Il Dock (il default, anche su Android) si ingrandisce sotto il cursore o sotto il dito appoggiato e si sgonfia quando si alza.
4. **Sprite e fumetti**: prima quelli che usano solo `drawImage` e alpha; poi `SpriteEffetto` (`SrcIn`) e `SpriteInDissolvenza` (sfocatura), da rifare con shader o precalcolati; `SpriteRivelazioneArtefatto` (forme e gradiente radiale) per ultimo.
5. **Traduzione degli eventi**: fatta una volta per tutte e due, in `ForestaUI` (modulo comune).

Traguardo: una partita giocabile in orizzontale, sul desktop con il mouse e sull'emulatore Android.

## 7. Fase 4: input a tocco

Le decisioni sono la 9 e la 10 (sezione 2). Stato al 2026-11-06:

- **Gesti** (fatto): `RiconoscitoreDiGesti`, nel modulo comune e testato, ricava da tocchi e movimenti grezzi premuto, trascinato, rilasciato, click e doppio click; col dito consegna la pressione al router solo quando sa che non è una pressione prolungata (la barra delle icone, per esempio, esegue il comando al rilascio: una pressione prolungata seguita dal rilascio lo avrebbe eseguito). Il trascinamento e l'inerzia delle liste c'erano già (`ScorrimentoSuUnAsse`).
- **Cartigli** (fatto): la pressione prolungata (500 ms senza muoversi) fa comparire le descrizioni come se il cursore passasse sul punto, il rilascio le chiude senza click, e spostando il dito le descrizioni lo seguono. Valgono le stesse regole del mouse: le descrizioni compaiono solo se l'aiuto è acceso (l'icona dell'aiuto). Da decidere se sul dito la pressione prolungata debba mostrarle anche con l'aiuto spento.
- **Doppio tocco** (fatto): il router aspetta 300 ms (`INTERVALLO_DOPPIO_TOCCO`) invece di 175 e il riconoscitore conta il secondo tocco solo entro lo stesso intervallo. Prima `InputGdx` accettava il secondo entro 400 ms mentre il router rimandava il click singolo di soli 175: un doppio tocco normale faceva scattare sia il singolo sia il doppio.
- **Hit-test**: i tipi sono già quelli del modulo comune (`Area`, `Punto`), senza `Rectangle`/`Point` di AWT.
- **Bersagli del tocco** (fatto in parte): le icone della barra sono una accanto all'altra, quindi lungo la barra non c'è spazio da prendere; col dito l'area sensibile arriva a tutto lo spessore della barra nell'asse trasversale (`BarraIcone.setAreaSensibileLarga`). Lungo la barra resta quella dell'icona: 64 pixel virtuali, circa 43 punti con la scala di un iPhone 15, appena sotto i 44 consigliati. Per averli davvero servirebbero icone più grandi: da verificare su un telefono vero. Gli altri bersagli (righe delle liste, frecce, riquadri) non sono stati misurati.
- **`Prompt`**: la tastiera di sistema c'è già (`CampoDiTestoGdx`).
- **Tasto «indietro»** (fatto per Android): `TastoIndietro` (comune) dà il comando che fa tornare indietro fra quelli possibili (`ANNULLA`, poi `NO`, poi `FLOPPY_SALVA`); `ForestaLibGDX` lo collega al tasto BACK. Se non c'è niente da fare lo lascia alla piattaforma. Il gesto di chiusura di iOS non esiste in libGDX come tasto: si vedrà con la Fase 8.
- **Provare sul desktop**: l'argomento `TOCCO` fa trattare il mouse come un dito (pressione prolungata, doppio tocco lento, area larga). Sui telefoni è automatico, e porta con sé il joystick a schermo sulla mappa.

## 8. Fase 5: schermate a tutto schermo

Nell'ordine, dalla più semplice alla più pesante:

1. Intro/Outro, slot di salvataggio, punteggi (`DisplayableCanvasIntroOutro`); Trofei; Bestiario.
2. Mappa a tutto schermo e Notiziario.
3. Intermezzo (fumetti e animazioni).
4. Inventario, Commerciante, Incantatore, Scambiatore consumabili (famiglia `DisplayableCanvasScambiatore`: un comportamento comune va nella classe base).
5. **Tattiche** (`DisplayableCanvasTattiche`, 1.203 righe allora, oggi circa 210: la logica è nel comune) per ultime: menu a discesa (`CasellaADiscesa`: la logica è ora `MenuADiscesa` nel comune, il disegno resta per toolkit), liste scorrevoli, modifiche multiple. La logica sta già in `08-foresta-tattiche` (`BozzaTattica`, `TestoTattica`).
6. **Logo** (`TracciatoreLogo`, 1.443 righe allora, oggi circa 450): **fatto**. La logica (contorni, corridori, fasi) è in `AnimazioneDelLogo` nel modulo comune, `TracciatoreLogo` resta la veste Swing e `LogoInizialeGdx` quella libGDX (vedi sopra, decisione 7). I gradienti radiali delle nuvole in `CloudGenerator` si precalcolano in una texture.

La finestra di prova (`FinestraDiProva`) si porta anche lei (decisione del 2026-11-06), ma solo per il desktop.

## 9. Fase 6: layout per telefono

Il gioco resta orizzontale. Prima prova, **fatta in Swing e in libGDX** (argomento `VERTICALE`): la barra icone passa da orizzontale in fondo a verticale sul lato destro, e scorre in pixel su tutta la lunghezza (con rotella, trascinamento, inerzia ed elastico), con due freccine blu sovrapposte che segnalano dove c'è altro. Restano da fare il layout a tile (mappa come tile, testo ridotto) e la scelta dell'altezza target. Con le misure di lavoro (zoom 2,0) la barra tiene 64 + 4 px di spaziatura:

| | Barra in fondo | Barra a destra |
| :--- | :---: | :---: |
| Larghezza | 1142 | 1142 + 64 + 4 = **1210** |
| Altezza | 656 | 656 − 64 − 4 = **588** |
| Rapporto | 1,74 | **2,06** |

Il rapporto 2,06 è vicino a quello dei telefoni (circa 2,2), quindi le bande nere ai lati quasi spariscono, e il vincolo dell'altezza passa da 656 a 588 px: la scala su un iPhone 15 (852×393) sale da 0,60 a circa 0,67 e le icone da 64 diventano circa 43 pt. Con il layout attuale, non ancora a tile, la prova Swing dà una finestra di 1094×732 (1022+72 per 804−72). Il Dock funziona in entrambi gli orientamenti ed è acceso anche su Android: col dito le icone si ingrandiscono mentre il dito è appoggiato sulla barra e si sgonfiano quando si alza.

- Per tenere la scala sopra 0,7 resta l'idea di abbassare i tile (gruppo più basso, mappa a 248).
- **iPad**: entra a scala circa 1,0 con qualunque delle due disposizioni.
- Il layout reattivo (una scelta per rapporto dello schermo) si decide dopo la Fase 3, con il gioco davanti.

## 10. Fase 7: Android

1. Modulo Android (Gradle) con il lancio dell'attività, il ciclo di vita e la cartella privata dell'app.
2. Compatibilità del sorgente: `minSdk` 24, dove lambda e stream di Java 8 funzionano; i gestori su file sono già in `java.io`, non `java.nio.file`.
3. Prestazioni e memoria su un dispositivo di fascia bassa; pressione sulla memoria e thread di precaricamento.
4. Icona, nome, orientamento forzato in orizzontale (con `VERTICALE` per la barra delle icone, vedi decisione 4), schermo sempre acceso durante la partita, notch e barre di sistema.
5. Pubblicazione: firma dell'app e pacchetto per lo store (account a pagamento una tantum).

## 11. Fase 8: iOS (iPhone e iPad)

1. MobiVM compila il bytecode in codice nativo prima del tempo (Apple vieta di generarlo a runtime). Il progetto libGDX standard ha già il modulo `ios`; lo stesso codice serve iPhone e iPad.
2. Servono un Mac con Xcode (non si compila da Linux), l'account Apple Developer a pagamento e il rispetto delle regole dell'App Store.
3. Stato di MobiVM (verificato il 2026-10-31): ultima versione 2.3.26 dell'11 agosto 2026, con binding per iOS 26; la 2.3.25 (23 giugno) aggiunge l'avvio su dispositivi iOS 17+. Nessuna dichiarazione esplicita su Xcode 26. libGDX segue con ritardo (la 1.14.0 usava MobiVM 2.3.22): potrebbe servire forzare la versione nel modulo iOS.
4. Il codice del progetto non usa reflection né `ServiceLoader`, che è ciò che dà più noie alla compilazione in anticipo. Da provare: lambda e stream del Java 8 sotto MobiVM.
5. Dato che iOS dipende da un progetto mantenuto da poche persone, il prototipo (Fase 0) deve includere il simulatore iOS prima di impegnarsi.

## 12. Cose da tenere in ordine lungo tutto il lavoro

- **Test dell'architettura**: `DipendenzeDalToolkitGraficoTest` oggi vieta `java.awt`, `javax.swing`, `javax.imageio` e `javax.sound` fuori da `12-foresta-swing`; va esteso perché ammetta libGDX solo nel suo modulo e vieti AWT e Swing a quel modulo. `ConvenzioniDiPortabilitaTest` (niente `System.exit`, niente chiamate dipendenti dalla lingua senza `Locale`) vale anche qui.
- **Documentazione**: ogni modulo nuovo ha il suo `NN-foresta-<modulo>.md` nella radice, con le dipendenze uguali ai `pom.xml`; la mappa dei documenti in `foresta.md` e la tabella in `didattico.md` §2 si aggiornano.
- **Dipendenze fra moduli**: se una dipendenza cambia il livello di un modulo, si rinominano cartella, `artifactId` e documento.
- **Due front-end insieme**: finché Swing e libGDX vivono entrambi, ogni novità di UI va fatta due volte (decisione: tutto in entrambi, Swing resta alla pari).
- **Commit**: a passi che compilano, solo su richiesta.

## 12bis. La logica che si può estrarre da Swing (controllo del 2026-11-06)

Già nel modulo comune (`12-foresta-ui-comune`): le finestre e l'input (`Finestra`, `TipoFinestra`, `Orientamento`), `ScorrimentoSuUnAsse`, `LivelloDiZoom`, `ComandiPossibili`, i testi della UI, il segnalatore degli errori, il font Doomdark come dati (`DoomdarkFont`, `AtlanteDiGlifi`, `ImmagineDelFont`, `ImmagineDelFontAColori`, i glifi, `DoomdarkColorModel.Color`) e la lettura dei PNG senza toolkit (`LetturaPng`).

Da estrarre **componente per componente, quando lo si porta** (si separa il modello dal disegno, e Swing usa il modello comune; farlo tutto prima, a freddo, rischierebbe di rompere Swing senza vedere il risultato in libGDX):

| Classe | Logica che si può portare in comune | Resta nel toolkit |
| :--- | :--- | :--- |
| `ScorrimentoVerticale`, `ComponenteScorrevole` | il modello dello scorrimento e dell'albero di nodi (altezze, offset, hit-test, stato aperto/chiuso) | disegno dei nodi e delle freccine |
| `DisplayableCanvasBarraIcone` e `…Dock` | **fatto**: `BarraIcone` e `BarraIconeDock` (elenco delle icone, rettangoli, scorrimento e tocco, profilo di ingrandimento del Dock, posizione del cartiglio) | disegno delle icone e dei bordi |
| `Cartiglio` | calcolo della posizione e del testo | disegno |
| `TipoIcona`, `MappaBottinoImmagine`, `MappaPersonaggioImmagine`, `VersiDeiPersonaggi` | **fatto per `TipoIcona` e `VersiDeiPersonaggi`** (nel comune); le due `Mappa…Immagine` restano in Swing: le tabelle comando/classe/oggetto → nome della risorsa e il verso (`serveSpecchiare`) | caricamento dell'immagine nel toolkit e lo specchio |
| `SfondoARiempimento` | **fatto** (nel comune): la maschera dell'interno della cornice e l'interpolazione dei colori, su `ImmagineDiPixel` | la composizione dell'immagine finale |
| `ImageCache` | l'elenco dei file, le chiavi, le regole della cache e delle composizioni | il tipo dell'immagine e la cache viva (libGDX libera le texture a mano) |
| `DoomdarkTextProducer`, `TestoGrande` | la divisione in righe e l'impaginazione (già in `FontTool`) | la produzione dell'immagine (`IndexColorModel`, `ConvolveOp`) |
| `DisplayableCanvas` | **fatto**: `SchermoDelGioco` (stati, pila, code degli effetti, sprite in vita, contesto del router) e `DisposizioneDelloSchermo` (coordinate) | `JPanel`, disegno, eventi di Swing |
| `ForestaSwing` | **fatto**: `ForestaUI` (la traduzione degli eventi del bus, il temporizzatore della UI, le fasi di gioco), `DisposizioneDelloSchermo.dimensioniDelloSchermo` | `JFrame`, il logo, il thread di Swing |
| Riquadri e schermate | il contenuto da mostrare (testi, elenchi, ordinamenti, stato del mouse), le coordinate | `Graphics2D` |
| `DisplayableCanvasTattiche`, `CasellaADiscesa` | **fatto**: `EditorDelleTattiche` e `MenuADiscesa` (stato della modifica, righe, selezione, click) | il disegno |
| Sprite (`SpriteBase`, `SpriteATempo`, fumetti, annunci…) | **fatto**: `ModelloDelloSprite` (tempi, traiettorie, dissolvenza), le `Descrizione…` e `CoordinateFumetto` | disegno e filtri (`SrcIn`, sfocatura) |
| `TracciatoreLogo` | **fatto**: `AnimazioneDelLogo` (geometria e animazione del tracciato), `ContorniDelLogo`, `TorciaDelLogo`, `ScieDelLogo`, `TestaDellaCometa`, `LogoIniziale` | i tratti disegnati (Swing: `Graphics2D`; libGDX: texture riscritte dai pixel) |
| `CloudGenerator` | **fatto**: `GeneratoreDiNuvole`, `Nuvola` e `GestoreNuvole` (comune; geometria e animazione delle nuvole) | il disegno |
| `Prompt` | **fatto**: `CampoDiTesto` (chi aspetta la risposta, invio, Esc, caratteri ammessi) | il campo di testo |

## Osservazioni

- Il piano non stima i tempi per fase: senza il prototipo sarebbero numeri inventati. La stima di `assessment_portabilita.md` (circa 9.000 righe di layout e disegno dei riquadri, 5.500 di sprite, testo e immagini) dice dove sta il peso, non quanto dura.
- Le zone più rischiose sono `DisplayableCanvasTattiche`, l'input di `DisplayableCanvas` e il nuovo disegno del testo bitmap, perché il resto ne dipende.
- Le misure dei dispositivi sono in punti logici e approssimate: vanno riverificate sui modelli veri prima di fissare il layout.

## Da fare / idee aperte

- Nel prototipo, sul testo: quanti glifi si disegnano per fotogramma in una schermata di gioco piena, e se i colori del font sono tinte semplici o hanno più toni (decide se serve un atlante dei glifi per colore).
- Quando serviranno i moduli Android e iOS, costruire ANCHE la versione Gradle partendo da quella Maven (che resta la principale).
- Costruire il prototipo della Fase 0 e riempire qui la tabella delle misure.
- Decidere il layout definitivo (altezza target, tile, striscia di testo) dopo la Fase 3.
- Verificare MobiVM con l'Xcode in uso sul simulatore iOS.
- Decidere se togliere dal codice Swing il profilo temporaneo (`-Dforesta.profilo`) prima di prendere `DisplayableCanvas` come riferimento.
