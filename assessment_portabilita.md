# Portabilità verso Android e iOS: cosa manca

Stato al 2026-10-26. Riguarda `foresta-modellodati`, `foresta-motore-api` e le parti di `foresta-motore` che toccano disco, thread, tempo e testi; da «La UI Swing» in poi anche `foresta-swing`, esaminata file per file (99 file, circa 23.500 righe). I numeri di quella parte vengono da una lettura e da conteggi sul sorgente, non da una misura a runtime: prestazioni e memoria su un dispositivo vero non sono state provate.

In sintesi: **il motore e i moduli di dominio sono già pronti** (nessuno usa il toolkit grafico, né `System.exit`, né `java.nio.file`, reflection, `ServiceLoader`, `java.time`; solo `java.util`, `java.io`, stream, `java.util.function`, `java.util.concurrent`, `regex`, `java.text`), **manca la UI**, da riscrivere, e un punto d'ingresso per la piattaforma (oggi `Main` e `foresta-app` sono Swing e `System.exit`). Non esiste ancora nessun modulo Android o libGDX. Il sorgente è Java 8: su Android gli stream e le lambda chiedono API 24 o il desugaring.

Qui c'è soltanto quello che resta da fare. Quello che è già stato sistemato non è elencato: la motivazione di ogni cambiamento sta nel javadoc o in un commento del codice che lo riguarda, e la storia in git. I punti di aggancio per una piattaforma (ciclo di vita dell'app, uscita dal gioco, cartella dei file, destinazione del log, lingua dei testi) sono descritti nei javadoc di `ControlloreDiGioco`, `NotificaUscitaDalGioco`, `GestoreSuFile`, `DestinazioneDiLog` e `Testi`, e in [`motore_di_gioco.md`](motore_di_gioco.md) (§5, "Ciclo di vita dell'app").

## Portabilità

### Testi italiani nel codice

Il gioco è scritto in italiano. Sono già in un file per lingua, letti da `Testi` (con ripiego sull'italiano): le note dei moltiplicatori di classe, le descrizioni dei comandi, i motivi di rifiuto dell'equipaggiamento e i nomi e le descrizioni delle enum dei vari moduli. Restano nel codice:

- `FUGA_IGNOMINIA_NOTA` in `CostantiPersonaggi`: finisce nel salvataggio come nota di un modificatore, quindi spostarla vuol dire anche decidere come si confronta e come si salva;
- le enum senza ancora un file di testi, ma con testi da mostrare: `NegozioInScena` (singolare e plurale), e le frasi di `Mandante` (mescolate a identificativi: vanno separate) e, nello strumento di sviluppo, `DovePartono` e `ContestoLancio`; `Testi.testo(costante, voce)` funziona per qualunque enum, con il file accanto all'enum nel suo modulo;
- `Articoli` e `Numeri` (in `foresta-testi`): articoli, preposizioni, cardinali e ordinali in lettere; `Direzioni` (in `foresta-mappa`): le direzioni; `Racconto` (nel motore, che li dà alla UI con `VistaPartita`): i testi di storia, sconfitta e vittoria (vedi `todo.md`);
- il resto dei testi del motore e delle schermate (messaggi, fumetti, intermezzi, grammatiche `.txt`).

Prima di spostarli va deciso se il gioco avrà una versione inglese (per gli store). Se non l'avrà, i testi possono restare dove sono.

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

`Main` (in `foresta-app`) collega tutto e sceglie le implementazioni desktop: `EsecutoreSwing` per `ThreadPrincipale`, `TemporizzatoreJ2SE` (un `ScheduledExecutorService`), `GestoreSalvataggiSuFile`, `GestorePunteggiSuFile`, `GestoreTrofeiSuFile` e `GestoreBestiarioSuFile` (con `java.io.File`, non `java.nio.file`, che su Android chiede un'API recente), `System.exit` sulla `NotificaUscitaDalGioco`. `GestoreSuFile` prende la cartella dei file da `user.home` (l'unico `System.getProperty` del motore, più la cartella del log in `DestinazioneConsoleEFile`). Una piattaforma mobile ha bisogno di un suo punto d'ingresso che:

- installi un `Esecutore` per `ThreadPrincipale` sul thread della sua UI;
- dia ai gestori su file la cartella privata dell'app, e una `DestinazioneDiLog` adatta;
- chiami `ModelloDati.installa()`, `StatoDelMotore.registraComponenti()`, `RegistroTrofei.impostaGestoreTrofei`, `RegistroBestiario.impostaGestoreBestiario` come fa `Main`;
- gestisca la pressione sulla memoria e il thread di precaricamento (`Automa.precaricaInBackground` avvia un `Thread` normale, da verificare su Android).

## La UI Swing

`foresta-swing` è un renderer 2D fatto a mano su `BufferedImage` e `Graphics2D`, con quasi nessun widget Swing vero. `DisplayableCanvas` (un `JPanel`) disegna tutta la schermata in `paintComponent` e anima da un thread suo a 60 fotogrammi al secondo; il testo è tutto in font bitmap (solo 3 usi di `Font`). Sono 65 file su 99 a usare `java.awt` o `javax.swing`; gli altri 34 (eventi, tipi, testi, `BozzaTattica`) si riusano quasi così come sono.

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
- **Font Doomdark**: i dati dei glifi (`DoomdarkFontMedium` 1.857 righe, `DoomdarkFontSmall` 1.177) sono tabelle di `byte[]` e si portano tali e quali; `DoomdarkTextProducer` costruisce un `BufferedImage` pixel per pixel con un `IndexColorModel`, e `DoomdarkTextRectangle`/`2x` usano `MemoryImageSource` e `Toolkit.createImage` (via `DisplayableCanvasRiquadroTesto`), tutti AWT. `TestoGrande` usa `ConvolveOp` (l'alone) e `RescaleOp` (scurire). `DoomdarkColorModel` usa `IndexColorModel` solo per la tavolozza.
- **Sprite** (`SpriteBase` e derivati): disegnano in un buffer con `AlphaComposite`. `SpriteInDissolvenza` ha una sfocatura con `ConvolveOp`, `SpriteEffetto` una maschera con `SrcIn`, `SpriteRivelazioneArtefatto` (537 righe) usa `Path2D`, `Ellipse2D`, `RadialGradientPaint` e tratti.
- **`TracciatoreLogo`** (1.443 righe): traccia il logo 3AM con una scia animata, poligoni e tratti sovrapposti. È isolato e si può sostituire con un video, una sequenza di immagini o un'animazione precalcolata. `PannelloLogoIniziale` e le nuvole (`CloudGenerator`, `CloudInstance`, `CloudManager`, con un gradiente radiale) sono più semplici.
- **Riquadri, schermate, barra e componenti**: il contenuto (che cosa mostrare e quando) è portabile, ma le coordinate sono in pixel fissi, pensate per la finestra desktop. Riquadri: Locazione (551 righe), Gruppo (608), Combattimento (302), Incantesimi e pozioni (260), Mappa (146), Missioni (220), Statistiche (129), Testo (127), Sfida (329). Schermate: Intro/Outro (506), Intermezzo (366), Inventario (377), Mappa a tutto schermo (314), Scambiatore e le sue due versioni (381, 536, 456), Commerciante (126), Incantatore (162), Bestiario (173), Trofei (83). Barra: `DisplayableCanvasBarraIconeDock` (210, si ingrandisce sotto il cursore, quindi non ha senso sul tocco) e la classica (453). Componenti: `ComponenteScorrevole` (397) e `ScorrimentoVerticale` (207), da rifare con lo scorrimento tattile.
- **`DisplayableCanvasTattiche`** (1.203 righe): il pezzo più impegnativo per il tocco, con menu a discesa (`CasellaADiscesa`), liste scorrevoli e modifiche multiple. La logica non sta qui: `BozzaTattica` (la tattica in modifica) e `TestoTattica` (i testi) sono in `foresta-tattiche`, pacchetto `tattiche.modifica`, e le userà ogni front-end.

### Portabile quasi così com'è

`TestoAzioneInCombattimento`, `ComandiPossibili`, `CoordinateFumetto`, `Orientamento`, `AnimazioniIntermezzo`, `FontTool`, `TestiDellaUI`, `VersiDeiPersonaggi`, i sei eventi `InternoCreazioneSprite…` e `InternoNotificaViaFumettoATempo`; le tabelle `MappaBottinoImmagine`, `MappaPersonaggioImmagine` e `TipoIcona` (legano un tipo di gioco al nome di un'immagine: cambia il tipo dell'immagine, resta la tabella). `BozzaTattica` e `TestoTattica` non stanno più in questo modulo (vedi «Le classi di modifica delle tattiche»). `FinestraDiProva` (532 righe, `JTabbedPane`, `JSpinner`) è solo per chi sviluppa e non si porta.

### Risorse

327 immagini `gif` e 2 `png` (2,2 MB), a metà risoluzione e ingrandite all'avvio (`LivelloDiZoom`, 2,0 di default) con campionamento `NEAREST`, così il pixel-art resta nitido. Su una GPU 329 texture singole sono troppe: vanno raggruppate in atlanti.

### Cosa non si sistema con la sola riscrittura

- **Input**: oggi mouse, passaggio del cursore, doppio click e rotella. Sul tocco servono gesti (tocca, tieni premuto, trascina). I cartigli con la descrizione dei comandi vivono del passaggio del cursore, che non c'è: serve un altro gesto.
- **Layout**: tutte le coordinate sono in pixel assoluti, per una finestra orizzontale (la disposizione verticale c'è, ma solo in parte). Su un telefono, e su iPad, serve un layout nuovo: è lavoro di progetto della schermata, non di codice.
- **Modello di disegno**: il codice disegna in modo immediato su `Graphics2D`. In libGDX si disegna con uno `SpriteBatch` (e forme e gradienti con `ShapeRenderer` o texture precalcolate).
- **Effetti per pixel** (`ConvolveOp`, `RescaleOp`, `SrcIn`, `setRGB`): su GPU diventano shader o operazioni su `Pixmap` all'avvio.

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
3. **Non cancellare `foresta-swing`** finché libGDX non lo eguaglia: resta come riferimento per confrontare il comportamento. La cancellazione si decide a parte.
4. **Ordine di porting**: disegno e testo, intro e schermata di gioco, schermate a tutto schermo (inventario, mappa, negozi), tattiche per ultime (le più pesanti per il tocco). `TracciatoreLogo` (1.443 righe) conviene sostituirlo con un'animazione precalcolata; `FinestraDiProva` si può lasciare al desktop.

Rischi reali: l'input a tocco (cartigli senza passaggio del cursore, liste da scorrere col dito, tattiche) richiede progettazione e non solo codice; gli effetti per pixel (sfocatura, alone del testo grande, maschere) vanno rifatti con shader o precalcolati; iOS dipende da MobiVM, di cui va controllato lo stato; il costo resta alto (settimane, non giorni), e senza prototipo non si può stimarlo sul serio. Si cambierebbe idea (strato sottile di astrazione, vedi sotto) se le prestazioni del disegno immediato non bastassero o se Swing dovesse restare il front-end ufficiale.

## Convivenza di Swing e libGDX

Si possono tenere entrambi i front-end e decidere dopo quale usare: `foresta-swing` dipende solo da `foresta-motore-api` e `foresta-tattiche` e non vede il motore, quindi un nuovo modulo (per esempio `foresta-libgdx`) con le stesse dipendenze non lo disturba. Il collegamento sta in `Main` (`foresta-app`), l'unico che conosce insieme motore e UI: basta un secondo punto d'ingresso (un modulo `foresta-app-libgdx` o una classe a parte), e il motore non cambia, perché pubblica e legge eventi e viste senza sapere che UI c'è sopra.

Cose a cui fare attenzione:

1. **Il toolkit grafico resta confinato**: `DipendenzeDalToolkitGraficoTest` oggi vieta `java.awt`, `javax.swing`, `javax.imageio` e `javax.sound` fuori da `foresta-swing`; va esteso perché ammetta libGDX solo nel suo modulo e vieti a quel modulo AWT e Swing.
2. **Classi di `foresta-swing` che non sono di Swing** (i dati dei font, le tabelle di immagini; `BozzaTattica` e `TestoTattica` sono già state spostate): il modulo libGDX non può dipendere da `foresta-swing`, perché porterebbe dentro AWT. Le strade: copiarle (semplice, ma due copie da tenere allineate) oppure spostarle in un modulo comune (più pulito, ma cambia la forma del codice e va discusso prima). Via di mezzo: partire copiando e spostare solo quando si vede che cosa si duplica davvero. Per le classi di modifica delle tattiche vedi il paragrafo seguente.
3. **Ogni novità di UI va fatta due volte** finché i due front-end vivono: conviene una regola presto (per esempio le novità si fanno solo nel front-end scelto, l'altro resta com'è).
4. **Build**: il progetto è tutto Maven, ma i moduli Android e iOS di libGDX di solito si costruiscono con Gradle (il desktop si può fare con Maven). Va deciso come far convivere i due sistemi, per esempio un progetto Gradle separato che usa i jar del progetto Maven.
5. **Documentazione**: ogni modulo nuovo ha il suo `foresta-<modulo>.md`, con la sezione delle dipendenze uguale ai `pom.xml`.

Il prototipo dà i dati per scegliere: quando libGDX mostra la schermata di gioco si confrontano prestazioni, aspetto e costo di manutenzione, e si decide se tenere entrambi, passare a libGDX o fermarsi.

### Le classi di modifica delle tattiche

Fatto: `BozzaTattica` e `TestoTattica` (che non toccavano il toolkit grafico) sono passate da `foresta-swing/…/ui/tattiche` a `foresta-tattiche/…/tattiche/modifica`, rese pubbliche (la schermata le usa in circa 66 punti), insieme ai loro test (`BozzaTatticaTest`, `TestoTatticaTest`); `RinominaDellaVoceTest` è rimasto in `foresta-app` e importa la bozza dal nuovo posto. Le parole di `TestoTattica`, che prima erano nel codice («minore di», «a caso», «il più potente», le spiegazioni del riquadro destro) e il nome predefinito di una voce con il motivo per cui una bozza non si conferma (`BozzaTattica`) sono ora in `foresta-tattiche/src/main/resources/…/testi/testo_tattica_it.properties`, letti con `Testi.get` (ripiego sull'italiano, `{0}`, `{1}`… al posto dei valori); `TestiDiTestoTatticaTest` controlla che ogni valore spiegato abbia la sua chiave. Dettagli in `foresta-tattiche.md`.

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
- **Disegnare il layout** per lo schermo verticale e per il tocco (gesti, sostituto dell'hover, tastiera per `Prompt`).
- Scrivere il punto d'ingresso della piattaforma (vedi sopra) e adattare gli `Esecutore`/`Temporizzatore`/cartella dei file.
- Decidere se togliere dal codice il profilo temporaneo di `DisplayableCanvas` e, per iOS, controllare lo stato attuale di MobiVM.
- Il costo dei `InternoMessaggio` costruiti a ogni passo della macchina a stati è in [`todo.md`](todo.md).
