# Piano di porting su libGDX (Android, iPhone, iPad)

Stato al 2026-10-31. È un piano, non una decisione presa: dice in che ordine fare le cose se si porta la Foresta su Android e iOS. Il «cosa manca» e i numeri sul codice Swing stanno in [`assessment_portabilita.md`](assessment_portabilita.md); qui c'è solo l'ordine dei lavori.

## 1. Perché questo piano, e che cosa non è in discussione

- **La riscrittura della UI è inevitabile.** Swing e AWT non esistono né su Android né su iOS. Qualunque strada porti la Foresta su un telefono passa dal riscrivere `12-foresta-swing` (disegno, input, immagini, testo) e `13-foresta-app-swing` (punto d'ingresso). Motore e moduli di dominio non si toccano: pubblicano eventi e viste senza sapere che UI c'è sopra.
- **libGDX è il mezzo consigliato, non l'unico.** Un'UI nativa solo per Android (`Canvas`/`View`) esiste, ma coprirebbe Android e basta, e per iOS andrebbe riscritta ancora. libGDX dà un solo codice di UI per desktop, Android e iOS (tramite MobiVM) e usa direttamente i jar del progetto.
- **Il disegno si presta.** È quasi tutto `drawImage` (208 chiamate su circa 450) e rettangoli colorati, che con uno `SpriteBatch` hanno un corrispondente quasi uno a uno.
- **Che cosa si riusa**: logica (`BozzaTattica`, `TestoTattica`), immagini dei font Doomdark (`doomdark_small.gif` e `doomdark_medium.gif`) con la loro lettura in `ImmagineDelFont`, tabelle di immagini (`MappaBottinoImmagine`, `MappaPersonaggioImmagine`, `TipoIcona`), eventi, struttura di `ImageCache`, contenuto di ogni riquadro e schermata. Si riscrivono disegno, input e collegamento con la piattaforma.

## 2. Decisioni prese e da prendere

Prese (dall'utente, 2026-10-31):

1. **Build**: si resta con Maven. Quando servirà, la versione Gradle (per i moduli Android e iOS) si costruirà **in più**, partendo da quella Maven, che resta la principale.
2. **Moduli**: `12-foresta-libgdx` per il front-end, `13-foresta-app-libgdx` per il punto d'ingresso desktop. I moduli Android e iOS, se stanno fuori da Maven, non entrano nella numerazione.
3. **Classi Swing**: restano dove sono (`12-foresta-swing`); il modulo libGDX non dipende da lui e quel che gli serve (dati dei font, tabelle di immagini) lo copia, senza spostare nulla. Swing resta come riferimento.
4. **Orientamento**: il gioco resta **orizzontale**. Niente layout verticale vero.
5. **Layout per il telefono**: prima prova, la sola barra icone piazzata in verticale sul lato destro, per stringere l'altezza e allargare la lunghezza (vedi la sezione 9). **Fatta in Swing** (2026-10-31): la modalità `VERTICALE` funziona, con la barra che scorre in pixel fra due frecce fisse, col trascinamento e l'inerzia.
6. **Android**: `minSdk` 24 (Android 7.0), dove lambda e stream di Java 8 funzionano senza desugaring particolare.
7. **Logo iniziale**: `TracciatoreLogo` si riscrive con `ShapeRenderer` (forme, tratti e gradienti), fedele all'originale e senza fotogrammi precalcolati.
8. **Due front-end**: finché vivono entrambi, ogni novità di UI si fa in **tutti e due** (Swing resta alla pari).
9. **Cartigli sul tocco**: la descrizione dei comandi compare con la **pressione prolungata**; il tocco normale esegue il comando.

Da prendere: l'altezza target del layout (dopo la Fase 3), e lo spostamento in un modulo comune delle classi non-Swing solo se la duplicazione diventa un peso.

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

   Si fa invece come un `BitmapFont`: all'avvio si costruisce **un atlante dei glifi** (uno per font, e per colore se serve) e ogni scritta si disegna come una sequenza di sprite, uno per lettera, nello stesso lotto dello `SpriteBatch`. Niente cache dinamica, riferimenti deboli o reaper per i testi, niente da liberare. Si riusano la divisione in righe (`FontTool.split`) e le larghezze dei glifi, che sono logica pura. I glifi li dà già l'**immagine del font**: `img/font/doomdark_small.gif` e `doomdark_medium.gif`, con ogni glifo in un riquadro di una griglia fissa (i bordi rossi, che servono solo a chi disegna e a chi legge, e l'elenco dei caratteri nell'ordine in `CaratteriDelFont`). Il livello di grigio di ogni pixel è l'alfa del colore del testo (nero puro trasparente, bianco il colore pieno), e la larghezza di ogni glifo è misurata sui pixel quando si apre l'immagine (`ImmagineDelFont`). In libGDX la lettura della griglia si riscrive sul `Pixmap` (è pochissima logica: il calcolo dei riquadri e delle larghezze) oppure si fa in fase di build, scrivendo l'atlante e un file con le larghezze. `DoomdarkTextProducer` e `DoomdarkTextRectangle` (che usano `BufferedImage`, `IndexColorModel`, `MemoryImageSource`) non si portano: li sostituisce il disegno per glifi.
   - **Colori**: il glifo è bianco con alfa, e il colore del testo (`DoomdarkColorModel`) si applica come tinta: basta un solo atlante per font con `batch.setColor(...)`, perché l'alfa dei pixel sfumati si moltiplica con quella del colore, come fa oggi `DoomdarkTextProducer.pixel`. Le scritte con ombra sono due passaggi (nero spostato di 2 pixel e colore sopra), uguali a oggi.
   - **Righe in più**: la riga di testo è alta quanto l'immagine intera (corpo più le righe per accenti e cediglie: 13 nel piccolo, 24 nel medio); chi posiziona un testo in un punto fisso toglie `getRigheInCima()` dalla quota. Valgono le stesse regole di impaginazione di Swing.
4. **Testo grande**: `TestoGrande` usa `ConvolveOp` (alone) e `RescaleOp` (scurire): si precalcola in un `Pixmap` o si fa con uno shader. Sono pochi testi, quasi sempre gli stessi. Dove una scritta composta è troppo pesante da ridisegnare a ogni fotogramma resta una piccola cache con un limite di voci e `dispose()` esplicito quando una voce esce (mai riferimenti deboli).
   - **Personaggi specchiati**: non servono immagini a parte (`immaginiPersonaggiPerVerso`); si disegna la stessa regione con larghezza negativa o con `TextureRegion.flip`.
5. **Composizione fuori schermo**: un'immagine in cui si può disegnare (`FrameBuffer` per ciò che si ricompone a ogni fotogramma, `Pixmap` per ciò che si calcola una volta).

## 6. Fase 3: schermata di gioco in orizzontale

1. **`FitViewport` a risoluzione virtuale fissa** (quella del layout scelto). Le coordinate di riquadri e schermate si riusano quasi tutte; sul telefono il gioco appare scalato.
2. **Riquadri** nell'ordine in cui servono per vedere una partita: Locazione, Gruppo, Testo (ridotto), Mappa (come tile), Missioni, Incantesimi e pozioni, Statistiche; poi Combattimento e Sfida.
3. **Barra icone**: la classica (`DisplayableCanvasBarraIcone`). La versione dock si ingrandisce sotto il cursore e non ha senso sul tocco.
4. **Sprite e fumetti**: prima quelli che usano solo `drawImage` e alpha; poi `SpriteEffetto` (`SrcIn`) e `SpriteInDissolvenza` (sfocatura), da rifare con shader o precalcolati; `SpriteRivelazioneArtefatto` (forme e gradiente radiale) per ultimo.
5. **Traduzione degli eventi** di `ForestaUI` (una quarantina) in chiamate sul nuovo canvas: la parte che si tiene.

Traguardo: una partita giocabile in orizzontale, sul desktop con il mouse e sull'emulatore Android.

## 7. Fase 4: input a tocco

È progettazione, non solo codice.

- **Gesti**: tocco al posto del click; pressione prolungata al posto del passaggio del cursore; trascinamento e fling per le liste (`ComponenteScorrevole`, `ScorrimentoVerticale` da rifare).
- **Cartigli** con la descrizione dei comandi: vivono del passaggio del cursore. Decisione: pressione prolungata per la descrizione, tocco normale per eseguire.
- **Hit-test**: `Rectangle`/`Point` e i 24 `contains` si portano su tipi propri o su quelli di libGDX.
- **Bersagli del tocco**: almeno 44 pt; le icone da 64 scendono a 35–43 pt sui telefoni (vedi la tabella in `assessment_portabilita.md`), quindi vanno ingrandite o ridisegnate.
- **`Prompt`** (oggi un `JTextField`): tastiera di sistema.
- **Tasto «indietro»** di Android e gesto di chiusura di iOS: mappati sulla stessa uscita della finestra, passando dalla conferma e dal salvataggio.

## 8. Fase 5: schermate a tutto schermo

Nell'ordine, dalla più semplice alla più pesante:

1. Intro/Outro, slot di salvataggio, punteggi (`DisplayableCanvasIntroOutro`); Trofei; Bestiario.
2. Mappa a tutto schermo e Notiziario.
3. Intermezzo (fumetti e animazioni).
4. Inventario, Commerciante, Incantatore, Scambiatore consumabili (famiglia `DisplayableCanvasScambiatore`: un comportamento comune va nella classe base).
5. **Tattiche** (`DisplayableCanvasTattiche`, 1.203 righe) per ultime: menu a discesa (`CasellaADiscesa`, da riscrivere), liste scorrevoli, modifiche multiple. La logica sta già in `08-foresta-tattiche` (`BozzaTattica`, `TestoTattica`).
6. **Logo** (`TracciatoreLogo`, 1.443 righe): decisione presa, si riscrive con `ShapeRenderer` (poligoni, tratti sovrapposti, scia animata, gradienti radiali delle nuvole in `CloudGenerator`, da precalcolare in una texture). Isolato dal resto: si fa per ultimo.

La finestra di prova (`FinestraDiProva`) non si porta: resta del desktop.

## 9. Fase 6: layout per telefono

Il gioco resta orizzontale. Prima prova, **fatta in Swing** (argomento `VERTICALE`): la barra icone passa da orizzontale in fondo a verticale sul lato destro, e scorre in pixel su tutta la lunghezza (con rotella, trascinamento, inerzia ed elastico), con due freccine blu sovrapposte che segnalano dove c'è altro. Restano da fare il layout a tile (mappa come tile, testo ridotto) e la scelta dell'altezza target. Con le misure di lavoro (zoom 2,0) la barra tiene 64 + 4 px di spaziatura:

| | Barra in fondo | Barra a destra |
| :--- | :---: | :---: |
| Larghezza | 1142 | 1142 + 64 + 4 = **1210** |
| Altezza | 656 | 656 − 64 − 4 = **588** |
| Rapporto | 1,74 | **2,06** |

Il rapporto 2,06 è vicino a quello dei telefoni (circa 2,2), quindi le bande nere ai lati quasi spariscono, e il vincolo dell'altezza passa da 656 a 588 px: la scala su un iPhone 15 (852×393) sale da 0,60 a circa 0,67 e le icone da 64 diventano circa 43 pt. Con il layout attuale, non ancora a tile, la prova Swing dà una finestra di 1094×732 (1022+72 per 804−72). Il Dock resta solo orizzontale e non ha senso sul tocco.

- Per tenere la scala sopra 0,7 resta l'idea di abbassare i tile (gruppo più basso, mappa a 248).
- **iPad**: entra a scala circa 1,0 con qualunque delle due disposizioni.
- Il layout reattivo (una scelta per rapporto dello schermo) si decide dopo la Fase 3, con il gioco davanti.

## 10. Fase 7: Android

1. Modulo Android (Gradle) con il lancio dell'attività, il ciclo di vita e la cartella privata dell'app.
2. Compatibilità del sorgente: `minSdk` 24, dove lambda e stream di Java 8 funzionano; i gestori su file sono già in `java.io`, non `java.nio.file`.
3. Prestazioni e memoria su un dispositivo di fascia bassa; pressione sulla memoria e thread di precaricamento.
4. Icona, nome, orientamento forzato, schermo sempre acceso durante la partita, notch e barre di sistema.
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

## Osservazioni

- Il piano non stima i tempi per fase: senza il prototipo sarebbero numeri inventati. La stima di `assessment_portabilita.md` (circa 9.000 righe di layout e disegno dei riquadri, 5.500 di sprite, testo e immagini) dice dove sta il peso, non quanto dura.
- Le zone più rischiose sono `DisplayableCanvasTattiche`, l'input di `DisplayableCanvas` e il nuovo disegno del testo bitmap, perché il resto ne dipende.
- Le misure dei dispositivi sono in punti logici e approssimate: vanno riverificate sui modelli veri prima di fissare il layout.

## Da fare / idee aperte

- Nel prototipo, sul testo: quanti glifi si disegnano per fotogramma in una schermata di gioco piena, e se i colori del font sono tinte semplici o hanno più toni (decide se serve un atlante dei glifi per colore).
- Decidere se l'atlante dei glifi di libGDX si ricava all'avvio dalle GIF del font (con la lettura della griglia riscritta sul `Pixmap`) o in fase di build, con un file delle larghezze accanto.
- Quando serviranno i moduli Android e iOS, costruire ANCHE la versione Gradle partendo da quella Maven (che resta la principale).
- Costruire il prototipo della Fase 0 e riempire qui la tabella delle misure.
- Decidere il layout definitivo (altezza target, tile, striscia di testo) dopo la Fase 3.
- Verificare MobiVM con l'Xcode in uso sul simulatore iOS.
- Decidere se togliere dal codice Swing il profilo temporaneo (`-Dforesta.profilo`) prima di prendere `DisplayableCanvas` come riferimento.
