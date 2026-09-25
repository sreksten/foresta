# Motore grafico

> Assessment tecnico della parte grafica/UI/rendering: `ui`, `ui.sfx`.
> Per la logica di gioco vedi [`motore_di_gioco.md`](motore_di_gioco.md). Per una visione d'insieme vedi [`foresta.md`](foresta.md).

## 1. Libreria grafica e avvio

Tutto il rendering è **Java2D/Swing/AWT puro** — nessuna libreria grafica esterna (né LWJGL né JavaFX).

`Main.main()` (`Main.java:29-50`) avvia il motore grafico in parallelo al motore di gioco, ciascuno con il proprio `Temporizzatore` indipendente. `ForestaUI` (`ui/ForestaUI.java`, 417 righe) è la classe radice della UI: il suo costruttore chiama `SwingUtilities.invokeLater(this::creaEMostraInterfacciaUtente)` (riga 42) per costruire la finestra sull'Event Dispatch Thread, poi si iscrive a **34 tipi di evento** sul bus (righe 44-78) prima ancora che la finestra esista.

`creaEMostraInterfacciaUtente()` (righe 81-146):
1. `ImageCache.init()` precarica tutte le risorse grafiche.
2. Calcola le dimensioni della finestra sommando gli ingombri delle cornici in cache (`ImageCache.corniceMappa`, `corniceGrande`, `corniceIncantesimi`, ecc.), con limite alla risoluzione dello schermo e due layout alternativi per `Orientamento.ORIZZONTALE`/`VERTICALE`.
3. Crea un `JFrame` ("La Foresta") con `setLayout(null)` — **posizionamento manuale a coordinate assolute**, niente `LayoutManager` Swing.
4. Vi aggiunge **due soli componenti Swing/AWT**: `Prompt` (input testuale overlay, centrato) e `DisplayableCanvas` (il canvas di gioco), a cui passa l'orientamento e lo spessore della barra icone (72 px). Fino al commit `5fb3be5` c'era un terzo componente, `PannelloIcone` (un `JPanel` con dentro dei bottoni `ImageButton`): ora la barra icone è una `Finestra` disegnata dentro il canvas come tutte le altre (vedi §2).
5. A fine setup pubblica `InternoInterfacciaUtentePronta` (riga 145) — è il segnale che sblocca l'avvio del motore di gioco (vedi `Main.java:49`).

## 2. Sistema di "finestre" componibili: `DisplayableCanvas`

`DisplayableCanvas` (`ui/DisplayableCanvas.java`, **1038 righe**) è un `JPanel` che implementa `Runnable` ed è il cuore del rendering. Non delega il layout a Swing: gestisce un proprio **stack di elementi grafici** (`ArrayList<InterfacciaUtente.Finestra> stackElementiGrafici`, riga 56) che rappresenta lo z-order di disegno — l'ultimo elemento dello stack è disegnato per ultimo, quindi appare sopra gli altri.

Il costruttore (`DisplayableCanvas(width, height, orientamento, dimensioneBarraIcone)`, riga 87) riceve le dimensioni dell'intera finestra e ricava da sé l'**area di contenuto** (`larghezzaSchermo`/`altezzaSchermo`): in orizzontale è la finestra meno la fascia di 72 px in basso, in verticale è una larghezza fissa di 640 px per l'intera altezza. Tutti i riquadri e le schermate a tutto schermo sono dimensionati sull'area di contenuto, non sulla finestra.

`primoPiano(Finestra)` (riga 406) porta una finestra in cima allo stack rimuovendola e riaggiungendola in coda. Ogni voce dello stack (`MAPPA`, `STATISTICHE`, `GRAFICA`, `STATO`, `INCANTESIMI_E_POZIONI`, `TESTO`, `MISSIONI`, `INFO_COMBATTIMENTO` — enum `InterfacciaUtente.Finestra`) corrisponde a un oggetto "riquadro" dedicato, istanziato nel costruttore di `DisplayableCanvas` con coordinate calcolate sommando manualmente gli ingombri delle cornici in `ImageCache` (costruttore, righe 113-243) e registrato in `mappaCoordinateElementiGrafici` (`Map<Finestra,Rectangle>`) — usata sia per il rendering sia per instradare gli eventi mouse alla finestra corretta.

Ci sono poi finestre **fuori-stack** che occupano l'intero schermo da sole, selezionate da un enum interno `StatoDisplayableCanvas` (`STATO_INTRO`, `STATO_IN_GIOCO`, `STATO_MAPPA`, `STATO_INVENTARIO`, `STATO_ARMAIOLO`, `STATO_ALCHIMISTA`, `STATO_VINTO`/`STATO_PERSO`, ecc.): `riquadroIntroOutro`, `mappaATuttoSchermo`, `inventario`, `armaiolo`, `alchimista`.

Il metodo `inGioco(Graphics)` (righe 413-466) itera una **copia** dello stack (per evitare `ConcurrentModificationException` se un handler ne modifica il contenuto durante il disegno) e delega a ciascun riquadro (`riquadroMappa.disegnaMappa(graphics)`, `riquadroGruppo.disegnaStatus(graphics)`, ecc. — switch alle righe 420-449), poi disegna gli sprite attivi.

`aggiornaSchermo(Graphics2D)` (righe 519-558), chiamato da `paintComponent`, sceglie cosa disegnare in base a `StatoDisplayableCanvas` e poi, **in qualunque stato**, disegna in coda tre strati sempre sovrapposti al contenuto: l'annuncio globale eventualmente attivo, la **barra icone** (`barraIcone.disegna`) e la **sfera magica** (`disegnaSferaMagica`, riga 513 — immagine decorativa statica ancorata all'angolo in basso a sinistra dell'area di contenuto, visibile solo in `STATO_MAPPA`). Anche la sfera era prima un componente Swing a sé (`PannelloSferaMagica`, un `JPanel` trasparente aggiunto alla `JLayeredPane` del frame proprio per restare sopra al canvas animato, mostrato/nascosto da `ForestaUI` al cambio di schermata): ora è una semplice `drawImage` condizionata allo stato, e non c'è più nessuna visibilità da tenere sincronizzata a mano. Nota: prima era ancorata al fondo della *finestra* (e sovrapposta quindi anche alla barra icone), ora al fondo dell'*area di contenuto*, cioè appena sopra la barra in orientamento orizzontale.

### La barra icone come `Finestra`

`DisplayableCanvasBarraIcone` (`ui/DisplayableCanvasBarraIcone.java`, 294 righe) sostituisce `PannelloIcone` + `ImageButton`. Il Javadoc della classe dichiara il motivo: i veri componenti Swing "convivevano male con l'animazione continua di `DisplayableCanvas`" — due componenti sovrapposti con cicli di repaint indipendenti (quello di Swing per i bottoni, quello del thread di animazione per il canvas). Ora c'è **un solo ciclo di disegno** e un solo router del mouse.

- **Posizione**: calcolata nel costruttore di `DisplayableCanvas` (righe 225-243) — fascia in basso larga quanto la finestra in orizzontale, colonna a destra alta quanto la finestra in verticale — e registrata in `mappaCoordinateElementiGrafici` come ogni altra finestra. A differenza degli altri riquadri, riceve anche il proprio offset (`offsetX`/`offsetY`) perché disegna direttamente in coordinate di canvas.
- **Modello**: `impostaAzioni()` legge `ComandiPossibili.getComandi()` e costruisce una lista di `IconaVisibile` (rettangolo locale + `Comando` + immagine); le icone `PERSONAGGIO_1..5` sono risolte dinamicamente sull'icona della classe del personaggio corrispondente nel gruppo. Se le icone non stanno nella fascia (`larghezza / 66`), `ridistribuisciScelte()` aggiunge in testa/coda le frecce "Precedente"/"Successivo" (`SINISTRA`/`DESTRA` o `SU`/`GIU` a seconda dell'orientamento), gestite localmente con un contatore `saltaPrimi` senza passare dal motore; ogni altro click pubblica `ComandoDiGioco` sul bus.
- **Feedback visivo**: la barra tiene `mouseX`/`mouseY`/`mousePremuto` (aggiornati da `processaMovimento`/`processaPressione`/`processaRilascio`/`processaUscita`) e disegna un bordo grigio chiaro (hover) o bianco (premuto) attorno all'icona sotto il cursore — sostituisce il rollover che prima faceva Swing sui `JButton`.
- **Priorità nel routing**: `GestoreMouse.trovaFinestra` (riga 837) controlla la barra **prima** di tutto il resto, coerentemente col fatto che è disegnata sopra ogni altro strato.
- **Copyright**: le tre righe "La Foresta / copyright 1984-2026 / Stefano Reksten" nell'angolo della barra ora sono posizionate sulle dimensioni reali della fascia. Nella vecchia `PannelloIcone` gli offset erano calcolati nel costruttore con `getWidth()`/`getHeight()` quando il componente non era ancora stato dimensionato (quindi 0), e il testo finiva fuori dall'area visibile: il porting ha corretto il difetto di passaggio.

Conseguenza sul repaint: il thread di animazione chiama `repaint()` solo negli stati animati (§3), quindi negli stati statici (intro, statistiche, selezione slot...) la barra non si aggiornerebbe da sola al passaggio del mouse. Per questo tutti gli handler di `GestoreMouse` ora terminano con un `repaint()` esplicito, e `impostaAzioniIcone()` (chiamato da `ForestaUI` al posto del vecchio `pannelloIcone.impostaAzioni()`) fa lo stesso.

Le classi `DisplayableCanvas*` figlie (`Armaiolo`, `BarraIcone`, `Inventario`, `Scambiatore`/`ScambiatoreArtefatti`/`ScambiatoreConsumabili`, `IntroOutro`, `MappaATuttoSchermo`, `RiquadroCombattimento`, `RiquadroGruppo`, `RiquadroIncantesimiEPozioni`, `RiquadroLocazione`, `RiquadroMappa`, `RiquadroMissioni`, `RiquadroStatistiche`, `RiquadroTesto`) **non estendono `DisplayableCanvas`** né sono `JPanel`: sono oggetti "disegnatori" puri a cui viene delegato il rendering entro un rettangolo di competenza. Implementano l'interfaccia `Finestra` (`ui/Finestra.java`) per ricevere gli eventi mouse tradotti in coordinate locali:

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

`GestoreMouse` (classe interna di `DisplayableCanvas`, riga 807) implementa `MouseListener`/`MouseMotionListener`/`MouseWheelListener` di Swing e fa da **router**: per ogni evento mouse ricevuto, controlla prima la barra icone, poi l'eventuale finestra a tutto schermo dello stato corrente, altrimenti itera lo stack di finestre dall'alto (topmost) verso il basso e consegna l'evento alla prima finestra "sotto" il cursore secondo `mappaCoordinateElementiGrafici` — un pattern classico di hit-testing a z-order per UI custom disegnate a mano. `aggiornaFinestraSottoIlCursore` (riga 882) sintetizza gli eventi di entrata/uscita quando il cursore passa da una finestra all'altra.

## 3. Game/render loop

`DisplayableCanvas.addNotify()` (override del hook Swing chiamato quando il componente viene aggiunto alla gerarchia visibile, riga 365) avvia un **thread daemon dedicato**:

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
(`DisplayableCanvas.java:370-397`)

L'intro è invece guidata dal secondo temporizzatore, quello della UI: `ForestaUI` lo avvia con `iniziaDopo(5_000)` (primo impulso dopo 5 secondi, non subito) così che loghi o classifica restino a schermo per un periodo intero, e a ogni impulso chiama `DisplayableCanvas.avanzaIntro()`. Gli intermezzi del motore (vedi [`motore_di_gioco.md`](motore_di_gioco.md) §7) arrivano come `NotificaPaginaIntermezzo` e sono mostrati da `DisplayableCanvasIntermezzo` (§7).

`Temporizzatore.DURATA_FRAME_IN_MILLISECONDI = 1000 / FRAME_PER_SECONDO` con `FRAME_PER_SECONDO = 30` (`tools/Temporizzatore.java:12-13`) — **30 FPS fissi**, nessun frame-skipping o delta-time adattivo: ogni sprite avanza il proprio stato di un incremento fisso `1f/30` di secondo per chiamata (vedi §5).

Architetturalmente: **il thread daemon si limita a chiamare `repaint()`**; il disegno effettivo avviene sull'EDT tramite `paintComponent(Graphics)` (riga 469, override standard Swing con doppio buffering gestito automaticamente da `JPanel`). Nessuna logica di gioco vive in questo thread — coerente con quanto descritto in [`motore_di_gioco.md`](motore_di_gioco.md): motore e UI comunicano solo via `BusEventi`, e il bus consegna sempre sull'EDT.

## 4. Comunicazione motore → UI: sottoscrizioni al bus eventi

`ForestaUI` si iscrive nel costruttore (`ForestaUI.java:44-78`) a eventi delle tre famiglie rilevanti per la UI: `Notifica*` (fatti compiuti: variazioni statistiche, stato vitale, testo, fine gioco, raccolta oggetti...), `Richiesta*` (input necessario: direzione, sì/no, selezione incantesimo, testo libero...) e `Interno*` (tecnici: `InternoStatoDiGioco`, `InternoAggiornamentoComandiDisponibili`, `InternoPortaInPrimoPiano`, `InternoPreparazioneLocazione`, `InternoCaricamentoCompletato`...). Ogni handler traduce l'evento in una chiamata sul `displayableCanvas` corrente:

```java
BusEventi.iscriviti(NotificaVariazioneStatoVitalePersonaggio.class, this::gestisciEventoVariazioneStatoVitalePersonaggio);
// ...
private void gestisciEventoVariazioneStatoVitalePersonaggio(NotificaVariazioneStatoVitalePersonaggio evento) {
    displayableCanvas.notificaVariazioneStatoVitale(evento.getPersonaggio());
}
```

Alcuni handler forzano un repaint sincrono immediato con `rinfresca()` (`jframe.invalidate(); jframe.repaint();`, righe 413-416), usato per dare feedback istantaneo (es. apertura/chiusura della finestra di combattimento) invece di aspettare il prossimo tick del thread di animazione.

`gestisciEventoStatoDiGioco(InternoStatoDiGioco)` (righe 291-336) è l'handler più corposo: uno switch sui valori di `Stato` (l'enum del motore, vedi [`motore_di_gioco.md`](motore_di_gioco.md)) che decide quale schermata/prompt mostrare — è il punto in cui la state machine del motore si riflette in cambi di vista nella UI.

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

`DisplayableCanvas` rimuove gli sprite inattivi a ogni frame dalla propria lista (`inGioco()`, righe 455-465) — un pattern di **garbage collection per timeout**: lo sprite si "spegne" da solo, il contenitore lo scarta al frame successivo.

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

e lo trasforma in un'`Image` AWT tramite `Toolkit.createImage(new MemoryImageSource(...))`, applicando una palette a 1 bit (colore vs trasparente) definita da `DoomdarkColorModel`. Il risultato è che **ogni stringa renderizzata è un'immagine bitmap generata al volo**, non testo vettoriale — coerente con l'estetica pixel-perfect a bassa risoluzione dell'interfaccia. `FontTool` gestisce il word-wrap multi-riga rispettando una larghezza massima; `DoomdarkTextRectangle` è un contenitore che posiziona queste immagini-testo in un riquadro; `DoomdarkTextRectangle2x` (usato dal riquadro testo del gioco) è invece un **rettangolo scorrevole con storico**: conserva le righe già spezzate alla larghezza del riquadro (fino a `Costanti.MASSIMO_MESSAGGI_RICORDATI` = 100), ridisegna il raster solo quando testo o scorrimento cambiano (`daRidisegnare`), permette di tornare indietro con la rotella (`offsetRighe`) e antepone un "cuscino" di righe vuote perché le più vecchie non restino schiacciate sotto la sfumatura in alto. Dopo un caricamento lo storico viene svuotato e ripopolato da `DisplayableCanvas.ripristinaMessaggi` (vedi §7); il riquadro (`DisplayableCanvasRiquadroTesto`) conserva l'immagine finita, cioè il testo composto sull'ombra del drago con la sfumatura delle righe più vecchie, e la rifà solo quando cambia `DoomdarkTextRectangle2x.getVersione()`, che cresce a ogni testo aggiunto, svuotamento o scorrimento. Prima la ricomponeva a ogni frame (un `MemoryImageSource`, due immagini compatibili e un ciclo `getRGB`/`setRGB` sui pixel, trenta volte al secondo); `DoomdarkColorAlternante` gestisce presumibilmente testo con colori alternati (es. per evidenziare parole chiave in un messaggio).

Per evitare di rigenerare la stessa bitmap-testo a ogni frame, `ImageCache` mantiene una **cache dinamica a riferimenti deboli** (`ui/ImageCache.java:76-80`):

```java
private static final Map<DoomdarkFont, Map<DoomdarkColorModel.Color, Map<String, WeakReference<Image>>>> cacheDinamica =
        new ConcurrentHashMap<>();
```

chiave su font → colore → stringa, con pulizia periodica (schedulata e/o innescata dall'evento `InternoPuliziaCacheDinamicaImmagini`) che permette alla JVM di liberare le immagini di testo non più referenziate altrove.

## 7. Mappa a tutto schermo, notiziario e ripristino dei messaggi

Le ultime modifiche introducono due funzionalità che attraversano motore e UI, entrambe appoggiate a `motore.Notizie`/`NotizieMD` (vedi [`motore_di_gioco.md`](motore_di_gioco.md) §3 e §9).

**Notiziario** (`ui/Notiziario.java`, 187 righe): un ticker a scorrimento orizzontale che `DisplayableCanvasMappaATuttoSchermo` disegna in una fascia blu sotto la mappa, alta quanto un'icona di mappa, **solo quando esistono notizie** (`altezzaMappa()` restituisce l'altezza piena se `Notizie.getUltimeNotizie()` è vuota). Punti notevoli:

- Tutte le notizie correnti sono composte in **un'unica `BufferedImage`** (titolo in giallo + corpo in bianco, separati sul primo `" - "` del testo, più un separatore "-" disegnato come immagine a sé perché gli spazi del font Doomdark sono troppo stretti), ricostruita solo quando la lista cambia; l'immagine è disegnata **affiancata più volte (tiling)** così che lo scorrimento sia continuo senza vuoti.
- Avanza di 3 px per frame (nessun delta-time, come `SpriteBase`), con una sfumatura sul bordo destro per l'ingresso e un'entrata "da destra" solo alla prima costruzione: le ricostruzioni successive proseguono lo scroll già in corso.
- La fascia parte da metà della larghezza della sfera magica (`LARGHEZZA_SFERA_MAGICA_COPERTA`), perché l'altra metà è coperta dalla sfera disegnata sopra (§2).
- Il confronto `notizieAttuali.equals(notizieCostruite)` avviene a ogni frame su uno snapshot della lista; `Notizia` non ridefinisce `equals`, quindi il confronto è per identità degli oggetti — corretto finché ogni notizia nuova è un oggetto nuovo (e dopo un caricamento lo è sempre).

L'immagine delle caselle della mappa a tutto schermo non viene più ricostruita a ogni frame (con la griglia 20×20 da 32 px era una `BufferedImage` da circa 1,6 MB, trenta volte al secondo): `preparaMappa()` la costruisce a ogni apertura della schermata, chiamata da `DisplayableCanvas.mappa()`, perché mentre la mappa è mostrata l'automa attende solo di chiuderla e nessuna casella può cambiare. La casella del gruppo resta vuota nell'immagine, e il segnalino lampeggiante (acceso un secondo sì e uno no) viene disegnato sopra a ogni frame, prima delle nuvole come in precedenza.

La mappa a tutto schermo si adatta di conseguenza: l'offset verticale e il clamping del trascinamento usano `altezzaMappa()` invece dell'altezza piena, l'offset viene **rivalidato a ogni frame** (l'area disponibile può cambiare quando arriva la prima notizia), il disegno di mappa e nuvole è limitato con `clipRect` alla fascia sopra il notiziario, e `processaPressione` ignora i click sulla fascia (non si inizia un trascinamento dal ticker).

**Intermezzi** (guida pratica: [`intermezzi.md`](intermezzi.md)): `DisplayableCanvasIntermezzo` (`ui/DisplayableCanvasIntermezzo.java`) è la finestra a tutto schermo dello stato `STATO_INTERMEZZO`, in cui `DisplayableCanvas.mostraPaginaIntermezzo()` porta il canvas a ogni `NotificaPaginaIntermezzo`. Disegna la pagina a strati in funzione dei secondi reali trascorsi da quando è comparsa (lo stesso metro del timer con cui il motore fa avanzare le pagine, quindi animazioni e dialoghi restano allineati anche se qualche frame va perso):
- **sfondo**: l'immagine della pagina scalata per coprire l'area mantenendo le proporzioni, oppure l'ombra del drago;
- **elementi**: per ciascuno lo stato al secondo corrente, calcolato dal modello, tradotto in pixel (centro, scala, opacità con `AlphaComposite`, verso con larghezza negativa), con interpolazione bilineare. L'immagine può cambiare nel tempo: per uno sprite sheet la UI lo ritaglia una volta sola in sotto-immagini (`getSubimage`, senza duplicare i pixel) e mostra il fotogramma indicato dal modello; per un'`Animazione` crea un'istanza di `AnimazioneImmagine` per ogni elemento, tenuta finché dura l'intermezzo, e le chiede il fotogramma a ogni frame, passandole i secondi e lo stato dell'elemento (per esempio per scegliere un fotogramma di camminata solo mentre si sposta);
- **testo** in alto, delegato a `DisplayableCanvasIntroOutro.scriviTestoCentrato()` che possiede l'alfabeto bitmap grande;
- **fumetti** delle battute visibili: la nuvola e la punta sono quelle di `SpriteFumetto`, estratte nei metodi statici `costruisciNuvola()` e `disegnaPunta()` e riusate qui; la punta va alla bocca di chi parla nella posizione che ha in quel frame, così segue un personaggio in movimento. La nuvola di ogni battuta è costruita una volta sola per pagina.

`AnimazioneImmagine` (`ui/AnimazioneImmagine.java`) è l'interfaccia per chi vuole fornire un'immagine animata via codice: un solo metodo, `getFotogramma(secondi, stato)`, chiamato a ogni frame, che non deve allocare immagini nuove ogni volta (conviene prepararle nel costruttore). `AnimazioniIntermezzo` associa ogni valore dell'enum `Animazione` del modello alla sua classe. L'esempio incluso, `AnimazioneFuocoDaCampo`, disegna nel costruttore otto fotogrammi di un fuoco (ciocchi, tre strati di fiamma che ondeggiano a ritmi diversi, scintille) e li ripete a dieci al secondo.

Le immagini dei personaggi, degli oggetti e delle locazioni vengono da `ClassePersonaggioImmagine`, `ClassiOggettoImmagine` e `ImageCache`: il motore (`ClassePersonaggio`, `ClassiOggetto`) non carica immagini, così si usa anche senza schermo (lo controlla `ClassiOggettoHeadlessTest`, che gira in una JVM headless a parte); quelle di tipo risorsa sono caricate al primo uso con `BufferedImageBuilder.provaACaricare()` (che, a differenza di `buildBufferedImage()`, non chiude il gioco se la risorsa manca) e dimenticate a fine intermezzo (`svuota()`, chiamato da `DisplayableCanvas.iniziaGioco()`). `disegnaAl(graphics, secondi)` permette di disegnare la pagina a un istante preciso: lo usa `AnteprimaIntermezzo` (`src/test/java/.../ui/AnteprimaIntermezzo.java`), lo strumento che mostra un intermezzo fuori dal gioco, in una finestra dal vivo con i tempi del gioco oppure come griglia PNG. L'anteprima usa le stesse dimensioni della finestra di gioco: il calcolo è stato estratto in `ForestaUI.calcolaDimensioniFinestra()` e `DisplayableCanvas.calcolaAreaDiContenuto()`, usati anche dal gioco. Nell'estrarlo è stato corretto un bug: il limite sull'altezza confrontava l'altezza dello schermo con la *larghezza* della finestra, quindi su schermi alti meno di 1022 px (per esempio 900) la finestra prendeva l'altezza dello schermo anche quando la sua bastava.

**Ripristino del pannello testo dopo un caricamento**: al termine di una lettura riuscita il motore pubblica `InternoCaricamentoCompletato` con gli ultimi messaggi salvati; `ForestaUI.gestisciEventoCaricamentoCompletato` → `DisplayableCanvas.ripristinaMessaggi`, che svuota il pannello e li reinserisce dal più vecchio al più recente. Ogni `Messaggio` ricorda se era un paragrafo o una continuazione, e il ripristino usa la stessa regola dei messaggi in diretta: un paragrafo è preceduto da una riga vuota (`DisplayableCanvas.notificaParagrafo`, usato anche da `ForestaUI.gestisciEventoParagrafo`), una frase va semplicemente a capo. Il pannello riappare quindi impaginato come prima del salvataggio.

## 8. Effetti speciali (`ui.sfx`)

- **`CloudManager`/`CloudInstance`/`CloudGenerator`** (`ui/sfx/`): genera un set condiviso di 12 nuvole proceduralmente (`CloudGenerator.generateCloud`, forma casuale), riusato sia dal riquadro mappa in-game sia dalla mappa a tutto schermo, così le nuvole restano **coerenti tra le due viste** quando si passa dall'una all'altra. Ogni nuvola ha una velocità di parallasse proporzionale alla propria larghezza (`speed = 0.2 + (cloudWidth/clipWidth) × 0.8`, `CloudManager.java:104`), e il riposizionamento tra viste diverse è calcolato tramite un sistema di coordinate "canoniche" (delta di traslazione rispetto alla porzione di Foresta effettivamente visibile, righe 63-73) — nessuno scaling, solo traslazione, perché le nuvole devono avere la stessa dimensione ovunque.

- **`TracciatoreLogo`** (`ui/sfx/TracciatoreLogo.java`): non è un semplice effetto ma una **pipeline di elaborazione immagine** per tracciare e animare il contorno di un logo. Si imposta con `TracciatoreLogo.costruttore(logo)…costruisci()` (sfondo per alpha o per colore, regione, corridori espliciti o uno per forma, versi, ritardi, velocità, torcia, dissolvenza, pausa, ripetizione, colore di sfondo), poi chi lo usa lo fa avanzare con `avanza()` e lo disegna con `disegna(g2)`; senza `ripeti(true)` si ferma dopo un ciclo (`isFinito()`). Per provarlo da riga di comando, con finestra dal vivo o PNG, c'è `TracciatoreLogoDaRigaDiComando` tra i sorgenti dei test (stesso pacchetto; uso nel suo javadoc). La pipeline è documentata in un commento di testa in 7 fasi:
  1. **Maschera** primo piano/sfondo, tramite un `PredicatoSfondo` configurabile a lambda (per alpha o per colore), con supporto a una regione di scansione limitata (`Costruttore.regione`) per loghi annegati in illustrazioni più grandi.
  2. **Tracciamento del contorno esterno** con l'algoritmo classico **Moore-Neighbor boundary tracing**: scansione riga per riga per trovare pixel di bordo non ancora consumati, poi "cammino" lungo il perimetro esaminando gli 8 vicini in senso orario.
  3. **Rilevamento dei buchi interni** (es. il triangolo dentro una "A"): una seconda maschera isola le regioni di sfondo che non toccano mai il bordo dell'immagine, tracciate con lo stesso algoritmo del punto 2.
  4. **Ricampionamento a distanza costante** lungo ogni contorno per ottenere punti equidistanti, necessari per un'animazione di "disegno" del contorno a velocità uniforme.
  
  Il codice è insolitamente ben documentato per il progetto, con spiegazioni esplicite anche delle proprietà scomode (es. ambiguità topologica su diagonali singole con font/loghi dai tratti sottili).

## 9. Gestione immagini

`ImageCache` (statica, inizializzata via `ImageCache.init()` all'avvio della UI) precarica **decine di `BufferedImage`** e mappe indicizzate per enum (cornici, sprite di stato, `Map<ClassiLocazione, BufferedImage>` per le illustrazioni di ogni tipo di locazione, icone per `ClassePersonaggio`) da risorse in `/com/threeamigos/foresta/img/`.

`BufferedImageBuilder.buildBufferedImage(resource)` (`ui/BufferedImageBuilder.java:17-37`) carica via `ImageIO.read`, poi crea esplicitamente una copia **compatibile con la configurazione grafica dello schermo corrente** (`GraphicsConfiguration.createCompatibleImage`) — un'ottimizzazione classica Java2D che evita conversioni di formato pixel a ogni `drawImage`, a costo di duplicare temporaneamente l'immagine in memoria durante il caricamento. Eventuali errori di caricamento risorsa sono fatali (`System.exit(0)` dopo il log, righe 31-34) — coerente con la filosofia "fail fast all'avvio" per asset mancanti, diversa dalla gestione tollerante via eventi usata altrove nel motore.

## 10. Componenti UI riutilizzabili

- **`ComponenteScorrevole<T>`** (`ui/ComponenteScorrevole.java`): lista ad albero scorrevole generica, con nodi che portano una chiave, una descrizione, un'icona opzionale e un riferimento generico `T` all'oggetto rappresentato (per risalire dall'elemento cliccato all'oggetto di dominio) — usata per le schermate di inventario/negozio.
- **`DisplayableCanvasBarraIcone`**: la barra dei comandi, ora una `Finestra` disegnata dentro il canvas (vedi §2); ha rimpiazzato `ImageButton`/`PannelloIcone`, che erano componenti Swing veri e propri.
- **`Prompt`** (`ui/Prompt.java`): overlay con un `TextField` AWT per l'input testuale libero (es. nome del personaggio); ha un `FocusListener` che **richiama sempre il focus su se stesso** (`MyFocusListener.focusLost` chiama `requestFocus()`, riga 18-22) per impedire che il testo perda il focus mentre la finestra di gioco cattura altri eventi mouse/tastiera. Pubblica `ComandoInvioTesto` sul bus quando il testo è confermato.
- **`CoordinateFumetto`**: calcola il posizionamento di un fumetto di testo relativo a un personaggio/riquadro sullo schermo.
- **`Orientamento`**: enum `ORIZZONTALE`/`VERTICALE` che pilota due layout alternativi dell'intera finestra (vedi §1) — probabilmente pensato per adattarsi a schermi desktop vs. formati più stretti/verticali. `ForestaUI` lo traduce nelle costanti intere `DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE`/`VERTICALE` (ereditate dalla vecchia `PannelloIcone`) prima di passarlo al canvas.

## 11. Osservazioni per un eventuale refactoring

- **Il passaggio della barra icone a `Finestra` va nella direzione giusta**: un solo ciclo di disegno, un solo router del mouse, niente più componenti Swing sovrapposti al canvas animato (restano solo `Prompt` e il canvas stesso; anche la sfera magica non ha più bisogno della `JLayeredPane`). Il costo è che l'aggiornamento visivo negli stati statici dipende ora dai `repaint()` sparsi in tutti gli handler di `GestoreMouse` — chi aggiunge un nuovo handler deve ricordarsi di fare lo stesso.
- **`DisplayableCanvas` è un God Object da 1038 righe** (erano 967): possiede sia lo stato dello stack grafico, sia il routing del mouse, sia il thread di animazione, sia i riferimenti a *tutti* i riquadri figli e a tutte le code di sprite, e ora anche il calcolo della geometria finestra/area di contenuto/barra. Qualsiasi nuova schermata richiede di toccare questa classe in più punti (costruttore, `aggiornaSchermo()`, `finestraATuttoSchermo()`, `trovaFinestra()`, gestori eventi).
- **Due rappresentazioni dell'orientamento**: l'enum `Orientamento` e le costanti `int` `DisplayableCanvas.ORIENTAMENTO_*`, tradotte in `ForestaUI`. Passare direttamente l'enum al canvas e alla barra eliminerebbe la conversione.
- **Barra verticale con layout incoerente (difetto preesistente, portato tale e quale)**: in `ridistribuisciScelte()` le icone sono alte 64 px ma avanzano di 32 px (`offset += 32`), quindi si sovrappongono per metà e il hit-test (prima icona che contiene il punto) premia sempre quella più in alto; anche in orizzontale lo spazio disponibile è calcolato su un passo di 66 px ma le icone avanzano di 62. Inoltre in orientamento verticale la finestra è larga `min(schermo, 400)` mentre l'area di contenuto è fissata a 640 px: il contenuto viene tagliato dal bordo della finestra e la colonna delle icone (`x = width - 72`) gli si disegna sopra. Nessuno di questi problemi nasce con il refactoring, ma ora è più facile correggerli perché la barra è codice di disegno puro.
- **Layout interamente manuale** (`setLayout(null)`, coordinate calcolate a mano sommando larghezze di cornici): flessibile per un'estetica pixel-perfect, ma fragile — cambiare la dimensione di un'immagine in `ImageCache` richiede di ricontrollare a mano tutte le posizioni derivate. Il notiziario aggiunge un vincolo in più (la fascia dipende dalla larghezza della sfera magica).
- **Nessun delta-time reale**: il loop di `DisplayableCanvas.run()`, `SpriteBase.anima()` e ora anche `Notiziario` (3 px/frame) assumono 30 FPS costanti; su una macchina che non riesce a mantenere il framerate le animazioni rallentano invece di restare temporalmente corrette.
- **Stato di dominio letto direttamente dalla UI**: `Notiziario` e `DisplayableCanvasMappaATuttoSchermo` interrogano `Notizie.getUltimeNotizie()` (la lista viva dentro `ModelloDati`) a ogni frame. Funziona perché tutto gira sull'EDT, ma è un'eccezione al principio "motore e UI si parlano solo via eventi" descritto in §3-4.
- **Risorsa inutilizzata**: `fondi/SferaMagica.png` è stata aggiunta al repository ma `ImageCache` carica ancora `fondi/SferaMagica.gif`; va deciso quale delle due tenere.
- **`TracciatoreLogo`** è un pezzo di elaborazione immagine generico e ben isolato (algoritmicamente indipendente dal resto della UI): buon candidato per essere estratto in una libreria/modulo a sé se servisse altrove.
