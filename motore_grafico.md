# Motore grafico

> Assessment tecnico della parte grafica/UI/rendering: `ui`, `ui.sfx`.
> Per la logica di gioco vedi [`motore_di_gioco.md`](motore_di_gioco.md). Per una visione d'insieme vedi [`foresta.md`](foresta.md).

## 1. Libreria grafica e avvio

Tutto il rendering è **Java2D/Swing/AWT puro** — nessuna libreria grafica esterna (né LWJGL né JavaFX).

`Main.main()` (`Main.java:29-47`) avvia il motore grafico in parallelo al motore di gioco, ciascuno con il proprio `Temporizzatore` indipendente. `ForestaUI` (`ui/ForestaUI.java`, 424 righe) è la classe radice della UI: il suo costruttore chiama `SwingUtilities.invokeLater(this::creaEMostraInterfacciaUtente)` (riga 44) per costruire la finestra sull'Event Dispatch Thread, poi si iscrive a **~30 tipi di evento** sul bus (righe 46-78) prima ancora che la finestra esista.

`creaEMostraInterfacciaUtente()` (righe 81-157):
1. `ImageCache.init()` precarica tutte le risorse grafiche.
2. Calcola le dimensioni della finestra sommando gli ingombri delle cornici in cache (`ImageCache.corniceMappa`, `corniceGrande`, `corniceIncantesimi`, ecc.), con limite alla risoluzione dello schermo e due layout alternativi per `Orientamento.ORIZZONTALE`/`VERTICALE`.
3. Crea un `JFrame` ("La Foresta") con `setLayout(null)` — **posizionamento manuale a coordinate assolute**, niente `LayoutManager` Swing.
4. Vi aggiunge tre componenti: `Prompt` (input testuale overlay, centrato), `DisplayableCanvas` (il canvas di gioco), `PannelloIcone` (barra icone/azioni, orizzontale o verticale a seconda dell'orientamento).
5. A fine setup pubblica `InternoInterfacciaUtentePronta` (riga 156) — è il segnale che sblocca l'avvio del motore di gioco (vedi `Main.java:46`).

## 2. Sistema di "finestre" componibili: `DisplayableCanvas`

`DisplayableCanvas` (`ui/DisplayableCanvas.java`, **967 righe**) è un `JPanel` che implementa `Runnable` ed è il cuore del rendering. Non delega il layout a Swing: gestisce un proprio **stack di elementi grafici** (`ArrayList<InterfacciaUtente.Finestra> stackElementiGrafici`, riga 53) che rappresenta lo z-order di disegno — l'ultimo elemento dello stack è disegnato per ultimo, quindi appare sopra gli altri.

`primoPiano(Finestra)` (riga 373) porta una finestra in cima allo stack rimuovendola e riaggiungendola in coda. Ogni voce dello stack (`MAPPA`, `STATISTICHE`, `GRAFICA`, `STATO`, `INCANTESIMI_E_POZIONI`, `TESTO`, `MISSIONI`, `INFO_COMBATTIMENTO` — enum `InterfacciaUtente.Finestra`) corrisponde a un oggetto "riquadro" dedicato, istanziato nel costruttore di `DisplayableCanvas` con coordinate calcolate sommando manualmente gli ingombri delle cornici (`ImageCache`, righe 104-195) e registrato in `mappaCoordinateElementiGrafici` (`Map<Finestra,Rectangle>`) — usata sia per il rendering sia per instradare gli eventi mouse alla finestra corretta.

Ci sono poi finestre **fuori-stack** che occupano l'intero schermo da sole, selezionate da un enum interno `StatoDisplayableCanvas` (`STATO_INTRO`, `STATO_IN_GIOCO`, `STATO_MAPPA`, `STATO_INVENTARIO`, `STATO_ARMAIOLO`, `STATO_ALCHIMISTA`, `STATO_VINTO`/`STATO_PERSO`, ecc.): `riquadroIntroOutro`, `mappaATuttoSchermo`, `inventario`, `armaiolo`, `alchimista`.

Il metodo `inGioco(Graphics)` (righe 380-433) itera una **copia** dello stack (per evitare `ConcurrentModificationException` se un handler ne modifica il contenuto durante il disegno) e delega a ciascun riquadro (`riquadroMappa.disegnaMappa(graphics)`, `riquadroGruppo.disegnaStatus(graphics)`, ecc. — switch alle righe 386-415), poi disegna gli sprite attivi.

Le classi `DisplayableCanvas*` figlie (`Armaiolo`, `Inventario`, `Scambiatore`/`ScambiatoreArtefatti`/`ScambiatoreConsumabili`, `IntroOutro`, `MappaATuttoSchermo`, `RiquadroCombattimento`, `RiquadroGruppo`, `RiquadroIncantesimiEPozioni`, `RiquadroLocazione`, `RiquadroMappa`, `RiquadroMissioni`, `RiquadroStatistiche`, `RiquadroTesto`) **non estendono `DisplayableCanvas`** né sono `JPanel`: sono oggetti "disegnatori" puri a cui viene delegato il rendering entro un rettangolo di competenza. Implementano l'interfaccia `Finestra` (`ui/Finestra.java`) per ricevere gli eventi mouse tradotti in coordinate locali:

```java
interface Finestra {
    default void processaClick(int x, int y, Tasto tasto) { }
    default void processaPressione(int x, int y, Tasto tasto) { }
    default void processaTrascinamento(int x, int y) { }
    default void processaRotella(int x, int y, int rotazioni, MovimentoRotella m) { }
    // ... entrata/uscita/movimento/doppio-click/rilascio
}
```
(`ui/Finestra.java:1-63`, tutti i metodi hanno default no-op — ogni riquadro sovrascrive solo ciò che gli serve).

`GestoreMouse` (classe interna di `DisplayableCanvas`, riga 754) implementa `MouseListener`/`MouseMotionListener`/`MouseWheelListener` di Swing e fa da **router**: per ogni evento mouse ricevuto, itera lo stack di finestre dall'alto (topmost) verso il basso e consegna l'evento alla prima finestra "sotto" il cursore secondo `mappaCoordinateElementiGrafici` — un pattern classico di hit-testing a z-order per UI custom disegnate a mano.

## 3. Game/render loop

`DisplayableCanvas.addNotify()` (override del hook Swing chiamato quando il componente viene aggiunto alla gerarchia visibile, riga 332) avvia un **thread daemon dedicato**:

```java
private void avviaThreadAnimazione() {
    animatore = new Thread(this);
    animatore.setDaemon(true);
    animatore.start();
}

public void run() {
    animatoreInAzione = true;
    while (animatoreInAzione) {
        if (/* stato richiede animazione (in gioco, mappa, inventario, armaiolo, alchimista) o
               c'è un annuncio globale attivo/in coda */) {
            repaint();
        }
        Thread.sleep(Temporizzatore.DURATA_FRAME_IN_MILLISECONDI);
    }
}
```
(`DisplayableCanvas.java:337-363`)

`Temporizzatore.DURATA_FRAME_IN_MILLISECONDI = 1000 / FRAME_PER_SECONDO` con `FRAME_PER_SECONDO = 30` (`tools/Temporizzatore.java:12-13`) — **30 FPS fissi**, nessun frame-skipping o delta-time adattivo: ogni sprite avanza il proprio stato di un incremento fisso `1f/30` di secondo per chiamata (vedi §5).

Architetturalmente: **il thread daemon si limita a chiamare `repaint()`**; il disegno effettivo avviene sull'EDT tramite `paintComponent(Graphics)` (riga 436, override standard Swing con doppio buffering gestito automaticamente da `JPanel`). Nessuna logica di gioco vive in questo thread — coerente con quanto descritto in [`motore_di_gioco.md`](motore_di_gioco.md): motore e UI comunicano solo via `BusEventi`, e il bus consegna sempre sull'EDT.

## 4. Comunicazione motore → UI: sottoscrizioni al bus eventi

`ForestaUI` si iscrive nel costruttore (`ForestaUI.java:46-78`) a eventi delle tre famiglie rilevanti per la UI: `Notifica*` (fatti compiuti: variazioni statistiche, stato vitale, testo, fine gioco, raccolta oggetti...), `Richiesta*` (input necessario: direzione, sì/no, selezione incantesimo, testo libero...) e `Interno*` (tecnici: `InternoStatoDiGioco`, `InternoAggiornamentoComandiDisponibili`, `InternoPortaInPrimoPiano`, `InternoPreparazioneLocazione`...). Ogni handler traduce l'evento in una chiamata sul `displayableCanvas` corrente:

```java
BusEventi.iscriviti(NotificaVariazioneStatoVitalePersonaggio.class, this::gestisciEventoVariazioneStatoVitalePersonaggio);
// ...
private void gestisciEventoVariazioneStatoVitalePersonaggio(NotificaVariazioneStatoVitalePersonaggio evento) {
    displayableCanvas.notificaVariazioneStatoVitale(evento.getPersonaggio());
}
```

Alcuni handler forzano un repaint sincrono immediato con `rinfresca()` (`jframe.invalidate(); jframe.repaint();`, righe 420-423), usato per dare feedback istantaneo (es. apertura/chiusura della finestra di combattimento) invece di aspettare il prossimo tick del thread di animazione.

`gestisciEventoStatoDiGioco(InternoStatoDiGioco)` (righe 298-343) è l'handler più corposo: uno switch sui valori di `Stato` (l'enum del motore, vedi [`motore_di_gioco.md`](motore_di_gioco.md)) che decide quale schermata/prompt mostrare — è il punto in cui la state machine del motore si riflette in cambi di vista nella UI.

### Eventi "interni" alla sola UI: creazione sprite

Gli eventi `InternoCreazioneSprite*` (`ATempo`, `EffettoDiStato`, `Fumetto`, `InDissolvenza`, `AnnuncioGlobale`) **non arrivano dal motore**: sono pubblicati da un riquadro (es. `DisplayableCanvasRiquadroGruppo`, `RiquadroIncantesimiEPozioni`, `RiquadroMappa`, `RiquadroStatistiche`) in reazione a una notifica *del motore*, e consumati da `DisplayableCanvas` stesso, che li aggiunge alla propria lista `sprites` o alle code dedicate (`codaFumetti`, `codaAnnunciGlobali`). È un disaccoppiamento **interno alla UI**: permette a un riquadro di far comparire uno sprite animato sopra tutto il canvas senza tenere un riferimento diretto al `DisplayableCanvas` padre — lo stesso bus eventi usato per motore↔UI viene riusato come message-bus interno tra i componenti grafici stessi.

## 5. Sistema Sprite

`SpriteInterface` (`ui/SpriteInterface.java`) è minimale:

```java
public interface SpriteInterface {
    void anima(Graphics2D g);
    boolean isAttivo();
}
```

`SpriteBase` (astratta, `ui/SpriteBase.java`, 142 righe) implementa il ciclo di vita comune a tutti gli sprite temporizzati:

- **Interpolazione lineare** di posizione (x,y) e scala tra uno stato iniziale e uno finale, in funzione del progresso temporale (`interpola`, riga 112).
- **Calcolo alpha** delegato al metodo astratto `calcolaAlpha`, con due helper di dissolvenza pronti: `dissolvenzaLineareConSoglia` e `dissolvenzaIperbolicaConSoglia` (righe 116-130).
- `anima(Graphics2D)` è **`final`**: applica composite/alpha e hint di interpolazione bilineare, chiama il metodo astratto `disegna(g, x, y, scala)` delle sottoclassi, poi ripristina lo stato originale di `Graphics2D` — garantendo che uno sprite non "sporchi" lo stato grafico per i disegni successivi nello stesso frame.
- **Avanzamento temporale fisso**: `secondiTrascorsi += 1f / 30` per ogni chiamata (riga 100), coerente con il loop a 30 FPS (§3) — non è delta-time reale, quindi un eventuale frame-drop rallenterebbe percettibilmente le animazioni senza che il codice se ne accorga.
- **Ombra opzionale**: `applicaOmbra()` (righe 41-58) genera una sagoma scura sfalsata in 8 direzioni attorno all'immagine originale, per un effetto contorno/ombra portata senza dover pre-disegnare le risorse con ombra inclusa.
- **Auto-disattivazione per timeout**: quando `secondiTrascorsi >= durataInSecondi`, `attivo = false` (righe 71-74); nessun cleanup esplicito richiesto alle sottoclassi.

`DisplayableCanvas` rimuove gli sprite inattivi a ogni frame dalla propria lista (`inGioco()`, righe 422-432) — un pattern di **garbage collection per timeout**: lo sprite si "spegne" da solo, il contenitore lo scarta al frame successivo.

Sottoclassi concrete: `SpriteATempo` (sprite generico con durata fissa, il più usato — es. variazioni di livello/salute/magia nel riquadro gruppo), `SpriteEffetto` (per effetti di stato sul personaggio), `SpriteFumetto` (fumetti di testo a tempo, con coordinate relative a un personaggio via `CoordinateFumetto`), `SpriteInDissolvenza` (fade puro), `SpriteAnnuncioGlobale` (banner centrato a schermo intero, per notifiche importanti tipo aumento di livello/fine gioco).

`SpriteFumetto` e `SpriteAnnuncioGlobale` hanno inoltre una **coda dedicata con un solo elemento attivo alla volta** (`codaFumetti`/`fumettoAttivo`, `codaAnnunciGlobali`/`annuncioGlobaleAttivo` in `DisplayableCanvas`): se arrivano più notifiche ravvicinate, vengono mostrate in sequenza invece che sovrapposte.

## 6. Font bitmap custom "Doomdark"

Un sistema di rendering testo **completamente proprietario**, che non usa mai `Graphics.drawString`/`Font` di AWT: produce direttamente bitmap pixel-per-pixel, in stile retro (il nome `Doomdark*` è un omaggio esplicito al gioco *Doomdark's Revenge*/*Lords of Midnight*).

`DoomdarkFont` (interfaccia, `ui/DoomdarkFont.java`) espone i glifi come dati binari grezzi:

```java
public interface DoomdarkFont {
    int getHeight();
    int getSpacing();
    int getPadding();
    int getDataWidthInBytes();
    int getGlyphWidth(char c);
    byte[] getGlyphData(char c);   // un bit per pixel
}
```

Implementato da `DoomdarkFontMedium`/`DoomdarkFontSmall` con tabelle di byte incorporate nel codice (bitmap font "a mano").

`DoomdarkTextProducer.buildImageSource()` (`ui/DoomdarkTextProducer.java:12-63`) compone un buffer `int[]` leggendo bit per bit i dati del glifo:

```java
textData[textDataIndex + imageWidth * row] =
    (charData[row * dataWidth + 1 + (bits >> 3)] >> (7 - (bits & 0b111))) & 1;
```

e lo trasforma in un'`Image` AWT tramite `Toolkit.createImage(new MemoryImageSource(...))`, applicando una palette a 1 bit (colore vs trasparente) definita da `DoomdarkColorModel`. Il risultato è che **ogni stringa renderizzata è un'immagine bitmap generata al volo**, non testo vettoriale — coerente con l'estetica pixel-perfect a bassa risoluzione dell'interfaccia. `FontTool` gestisce il word-wrap multi-riga rispettando una larghezza massima; `DoomdarkTextRectangle`/`DoomdarkTextRectangle2x` sono contenitori che posizionano queste immagini-testo in un riquadro (la variante `2x` raddoppia la scala); `DoomdarkColorAlternante` gestisce presumibilmente testo con colori alternati (es. per evidenziare parole chiave in un messaggio).

Per evitare di rigenerare la stessa bitmap-testo a ogni frame, `ImageCache` mantiene una **cache dinamica a riferimenti deboli** (`ui/ImageCache.java:76-80`):

```java
private static final Map<DoomdarkFont, Map<DoomdarkColorModel.Color, Map<String, WeakReference<Image>>>> cacheDinamica =
        new ConcurrentHashMap<>();
```

chiave su font → colore → stringa, con pulizia periodica (schedulata e/o innescata dall'evento `InternoPuliziaCacheDinamicaImmagini`) che permette alla JVM di liberare le immagini di testo non più referenziate altrove.

## 7. Effetti speciali (`ui.sfx`)

- **`CloudManager`/`CloudInstance`/`CloudGenerator`** (`ui/sfx/`): genera un set condiviso di 12 nuvole proceduralmente (`CloudGenerator.generateCloud`, forma casuale), riusato sia dal riquadro mappa in-game sia dalla mappa a tutto schermo, così le nuvole restano **coerenti tra le due viste** quando si passa dall'una all'altra. Ogni nuvola ha una velocità di parallasse proporzionale alla propria larghezza (`speed = 0.2 + (cloudWidth/clipWidth) × 0.8`, `CloudManager.java:104`), e il riposizionamento tra viste diverse è calcolato tramite un sistema di coordinate "canoniche" (delta di traslazione rispetto alla porzione di Foresta effettivamente visibile, righe 63-73) — nessuno scaling, solo traslazione, perché le nuvole devono avere la stessa dimensione ovunque.

- **`LogoTraceEffect`** (`ui/sfx/LogoTraceEffect.java`, **1469 righe** — il file più grande e recente della UI, coerente con l'ultimo commit "LogoTraceEffect"): non è un semplice effetto ma una **pipeline di elaborazione immagine** per tracciare e animare il contorno di un logo. Estesamente documentata in un commento di testa (righe 18-90+) in 7 fasi:
  1. **Maschera** foreground/background, tramite un `BackgroundPredicate` configurabile a lambda (per alpha o per chroma-key a colore), con supporto a una regione di scansione limitata (`--region=x,y,w,h`) per loghi annegati in illustrazioni più grandi.
  2. **Tracciamento del contorno esterno** con l'algoritmo classico **Moore-Neighbor boundary tracing**: scansione riga per riga per trovare pixel di bordo non ancora consumati, poi "cammino" lungo il perimetro esaminando gli 8 vicini in senso orario.
  3. **Rilevamento dei buchi interni** (es. il triangolo dentro una "A"): una seconda maschera isola le regioni di sfondo che non toccano mai il bordo dell'immagine, tracciate con lo stesso algoritmo del punto 2.
  4. **Ricampionamento a distanza costante** lungo ogni contorno per ottenere punti equidistanti, necessari per un'animazione di "disegno" del contorno a velocità uniforme.
  
  Il codice è insolitamente ben documentato per il progetto, con spiegazioni esplicite anche delle proprietà scomode (es. ambiguità topologica su diagonali singole con font/loghi dai tratti sottili).

## 8. Gestione immagini

`ImageCache` (statica, inizializzata via `ImageCache.init()` all'avvio della UI) precarica **decine di `BufferedImage`** e mappe indicizzate per enum (cornici, sprite di stato, `Map<ClassiLocazione, BufferedImage>` per le illustrazioni di ogni tipo di locazione, icone per `ClassePersonaggio`) da risorse in `/com/threeamigos/foresta/img/`.

`BufferedImageBuilder.buildBufferedImage(resource)` (`ui/BufferedImageBuilder.java:17-37`) carica via `ImageIO.read`, poi crea esplicitamente una copia **compatibile con la configurazione grafica dello schermo corrente** (`GraphicsConfiguration.createCompatibleImage`) — un'ottimizzazione classica Java2D che evita conversioni di formato pixel a ogni `drawImage`, a costo di duplicare temporaneamente l'immagine in memoria durante il caricamento. Eventuali errori di caricamento risorsa sono fatali (`System.exit(0)` dopo il log, righe 31-34) — coerente con la filosofia "fail fast all'avvio" per asset mancanti, diversa dalla gestione tollerante via eventi usata altrove nel motore.

## 9. Componenti UI riutilizzabili

- **`ComponenteScorrevole<T>`** (`ui/ComponenteScorrevole.java`): lista ad albero scorrevole generica, con nodi che portano una chiave, una descrizione, un'icona opzionale e un riferimento generico `T` all'oggetto rappresentato (per risalire dall'elemento cliccato all'oggetto di dominio) — usata per le schermate di inventario/negozio.
- **`ImageButton`**/**`PannelloIcone`**: bottoni e barra icone basati su immagini invece che sui componenti Swing standard (`JButton`), con layout orizzontale/verticale gestito a mano in base a `Orientamento`.
- **`Prompt`** (`ui/Prompt.java`): overlay con un `TextField` AWT per l'input testuale libero (es. nome del personaggio); ha un `FocusListener` che **richiama sempre il focus su se stesso** (`MyFocusListener.focusLost` chiama `requestFocus()`, riga 18-22) per impedire che il testo perda il focus mentre la finestra di gioco cattura altri eventi mouse/tastiera. Pubblica `ComandoInvioTesto` sul bus quando il testo è confermato.
- **`CoordinateFumetto`**: calcola il posizionamento di un fumetto di testo relativo a un personaggio/riquadro sullo schermo.
- **`Orientamento`**: enum `ORIZZONTALE`/`VERTICALE` che pilota due layout alternativi dell'intera finestra (vedi §1) — probabilmente pensato per adattarsi a schermi desktop vs. formati più stretti/verticali.

## 10. Osservazioni per un eventuale refactoring

- **`DisplayableCanvas` è un God Object da 967 righe**: possiede sia lo stato dello stack grafico, sia il routing del mouse, sia il thread di animazione, sia i riferimenti a *tutti* i riquadri figli e a tutte le code di sprite. Qualsiasi nuova schermata richiede di toccare questa classe in più punti (costruttore, `inGioco()`, gestori eventi).
- **Layout interamente manuale** (`setLayout(null)`, coordinate calcolate a mano sommando larghezze di cornici): flessibile per un'estetica pixel-perfect, ma fragile — cambiare la dimensione di un'immagine in `ImageCache` richiede di ricontrollare a mano tutte le posizioni derivate.
- **Nessun delta-time reale**: sia il loop di `DisplayableCanvas.run()` sia `SpriteBase.anima()` assumono 30 FPS costanti; su una macchina che non riesce a mantenere il framerate le animazioni rallentano invece di restare temporalmente corrette.
- **`LogoTraceEffect`** è un pezzo di elaborazione immagine generico e ben isolato (algoritmicamente indipendente dal resto della UI): buon candidato per essere estratto in una libreria/modulo a sé se servisse altrove.
