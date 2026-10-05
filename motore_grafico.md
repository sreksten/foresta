# Motore grafico

Descrizione dello stato attuale della parte grafica: i pacchetti `ui` e `ui.sfx`. Per la logica di gioco vedi [`motore_di_gioco.md`](motore_di_gioco.md); per una visione d'insieme [`foresta.md`](foresta.md).

## 1. Libreria grafica e avvio

Tutto il rendering è **Java2D/Swing/AWT**, senza librerie grafiche esterne. Il testo non usa mai i font di AWT: passa dal sistema di font bitmap `Doomdark` (§7), con l'unica eccezione del campo di input (§11).

`Main` avvia motore e UI ciascuno con il proprio `Temporizzatore`. Gli argomenti che riguardano la UI sono `ORIZZONTALE` (default) o `VERTICALE`, `TUTTOSCHERMO`, `SALTALOGO` (niente logo iniziale: si attende solo il caricamento) e `BARRACLASSICA` (barra icone fissa invece della barra "Dock", §3).

`ForestaUI` è la classe radice. Il costruttore pianifica `creaEMostraInterfacciaUtente()` sull'Event Dispatch Thread e si iscrive subito, prima che la finestra esista, a una quarantina di tipi di evento del bus: ogni handler traduce l'evento in una chiamata sul `DisplayableCanvas` (§5).

Sequenza di avvio:
1. **Finestra.** `creaEMostraInterfacciaUtente()` apre un `JFrame` ("La Foresta", non ridimensionabile, `setLayout(null)`: coordinate assolute, nessun `LayoutManager`) con le dimensioni di `calcolaDimensioniFinestra`. Le dimensioni si ricavano dalle cornici dei riquadri e dall'icona più alta, lette dalle sole intestazioni dei file (`DimensioniRisorsa`) senza decodificarle, con tetto alla risoluzione dello schermo; in tutto schermo si usa lo schermo, in verticale `min(schermo, 400) × min(schermo, 640)`. La finestra contiene solo il `PannelloLogoIniziale`, poi la UI pubblica `InternoInterfacciaUtentePronta`, che sblocca l'avvio del motore.
2. **Logo.** Nello stato `LOGO_INIZIALE` il `TracciatoreLogo` (§9) anima il logo 3AM, centrato, a dimensione naturale: le scie svaniscono mentre il logo compare, il logo resta da solo per due secondi e poi svanisce. Un thread esegue intanto `ImageCache.init()` (cornici, locazioni, personaggi, oggetti, icone) e, a fine caricamento, sull'EDT si costruiscono `Prompt` e `DisplayableCanvas`. Quando l'animazione è finita (subito, con `SALTALOGO`) e l'interfaccia è completa, il canvas prende il posto del logo, il prompt va nel layered pane della finestra e la UI pubblica `InternoFineLogoIniziale`; con quello e col precaricamento del motore l'`Automa` passa a `INTRO`. A `LOGO_INIZIALE` non si torna.

## 2. Il canvas, le finestre e il mouse

`DisplayableCanvas` è un `JPanel` che implementa `Runnable` e disegna da solo tutto il gioco. I **componenti Swing veri** sono due: il canvas e il `Prompt`.

### Area di contenuto

La finestra si divide in un'**area di contenuto** e nella barra icone (§3). In orizzontale l'area è la finestra meno la fascia in basso di 72 px (`SPESSORE_BARRA_ICONE`); in verticale è fissa a 640 px di larghezza per tutta l'altezza (`calcolaAreaDiContenuto`, usata anche dall'anteprima degli intermezzi). Riquadri e schermate a tutto schermo si dimensionano sull'area di contenuto.

### Due modi di comporre lo schermo

- **Stack di riquadri** (stato `STATO_IN_GIOCO`): un elenco ordinato di `InterfacciaUtente.Finestra`, disegnato dal fondo verso l'alto. `primoPiano(finestra)` porta una finestra in cima. I riquadri sono `GRAFICA` (la locazione), `MAPPA`, `STATISTICHE`, `STATO` (il gruppo), `INCANTESIMI_E_POZIONI`, `TESTO`, `MISSIONI` e `INFO_COMBATTIMENTO` (una cornice piccola, visibile solo durante un round). Le coordinate di ognuno sono calcolate nel costruttore sommando a mano gli ingombri delle cornici di `ImageCache` e registrate in una mappa finestra → rettangolo, usata sia per il disegno sia per il mouse. Il disegno itera una **copia** dello stack, per non fallire se un handler lo modifica; poi si disegnano gli sprite (§6).
- **Schermate a tutto schermo**: un solo elemento occupa l'area di contenuto. Sono selezionate dall'enum interno `StatoDisplayableCanvas`: intro, selezione dello slot da caricare e da salvare, messaggio, mappa, conferma di uscita, sconfitta, vittoria, statistiche, punteggi, inventario, trofei, commerciante (armaiolo e venditore di pergamene), alchimista, incantatore, intermezzo. Intro, slot, messaggio, fine partita, statistiche e punteggi sono tutti disegnati da `DisplayableCanvasIntroOutro`.

Le classi `DisplayableCanvas*` figlie **non estendono `DisplayableCanvas`** e non sono componenti Swing: sono disegnatori puri a cui il canvas delega il rendering entro un rettangolo, e implementano l'interfaccia `Finestra`.

### L'interfaccia `Finestra`

`Finestra` ha metodi di default vuoti per ogni evento che un riquadro può voler gestire, tutti in **coordinate locali** al riquadro: `processaEntrata`, `processaUscita`, `processaMovimento`, `processaTrascinamento`, `processaPressione`, `processaRilascio`, `processaClick`, `processaDoppioClick`, `processaRotella`. In più `isVisibile()` (un riquadro nascosto non riceve eventi: lo usa il riquadro del combattimento) e `gestisceDoppioClick()`.

### Il routing del mouse

`GestoreMouse` (classe interna del canvas) realizza `MouseListener`, `MouseMotionListener` e `MouseWheelListener` e fa da router:
1. controlla per prima la **barra icone**, perché è disegnata sopra a tutto: è la barra a decidere quali punti sono suoi, perché nella versione Dock le icone ingrandite escono dal suo rettangolo;
2. in una schermata a tutto schermo consegna l'evento all'unico elemento;
3. altrimenti percorre lo stack **dall'alto verso il basso** e consegna all'elemento visibile più in primo piano che contiene il punto.

Entrata e uscita sono sintetizzate: AWT le segnala solo per il canvas intero, quindi il router ricorda su quale finestra era il cursore e, quando cambia, manda `processaUscita` alla vecchia (anche trascinando) e `processaEntrata` alla nuova. Ogni handler termina con un `repaint()` esplicito, perché negli stati statici il thread di animazione non ridisegna (§4).

**Doppio click.** Una finestra che lo gestisce (`gestisceDoppioClick()`) non riceve subito il click singolo: il router lo rimanda di **175 ms** e lo scarta se arriva il secondo. Per le finestre che non lo gestiscono il click è immediato. Limiti noti: il timer non si ferma al cambio di schermata, e l'intervallo è fisso (non quello del sistema), quindi un doppio click più lento di 175 ms fa scattare sia il click singolo sia il doppio.

## 3. La barra icone

`DisplayableCanvasBarraIcone` è una `Finestra` disegnata dentro il canvas, non un componente Swing: ha rimpiazzato i vecchi bottoni perché due cicli di repaint indipendenti convivevano male. Ha **un solo ciclo di disegno e un solo router**.

- **Posizione**: in orizzontale una fascia in basso larga quanto la finestra; in verticale una colonna a destra alta quanto la finestra. È l'unica finestra che riceve anche il proprio offset, perché disegna direttamente in coordinate di canvas.
- **Modello**: `impostaAzioni()` legge `ComandiPossibili.getComandi()` e costruisce l'elenco delle icone visibili (rettangolo, `Comando`, immagine). Le icone `PERSONAGGIO_n` sono risolte sull'icona della classe del personaggio corrispondente nel gruppo; le altre da `ClasseIcona.ofComando`. `ClasseIcona` associa a ogni comando o classe la sua immagine, caricata al primo uso (e precaricata a inizio partita).
- **Troppe icone**: se non stanno nella fascia (`larghezza / 66`), compaiono in testa e in coda le frecce `SINISTRA`/`DESTRA` (o `SU`/`GIU` in verticale), gestite localmente con un contatore `saltaPrimi` senza passare dal motore. Ogni altro click pubblica `ComandoDiGioco` sul bus.
- **Feedback**: un bordo grigio chiaro sotto il cursore, bianco quando si preme.
- **Aiuto**: in ogni schermata con dei comandi la barra aggiunge da sola in fondo l'interruttore dell'aiuto, che il motore non vede: `AIUTO` quando l'aiuto è spento, `NO_AIUTO` quando è acceso (l'icona mostra cosa fa il click). Lo stato sta in `ModelloDati.isAiutoAbilitato`, acceso in una partita nuova e salvato con la partita. Ad aiuto acceso, passando sopra un'icona compare un cartiglio con `Comando.getDescrizione()` (`disegnaAiuto`, chiamato da `DisplayableCanvas` dopo tutto il resto): con la barra in basso sopra l'icona e centrato su di lei, con la barra a destra alla sua sinistra; spostato quanto serve per restare dentro lo schermo (`posizioneCartiglio`). I comandi senza descrizione (i numeri, le frecce) non hanno cartiglio. Il cartiglio è lo stesso del nome delle caselle sulla mappa a tutto schermo (`Cartiglio`). Anche le schermate di scambio hanno il loro aiuto: passando sopra il nome di un oggetto compaiono accanto al mouse due cartigli, cosa fa il click ("Click: chiudi elenco modificatori", o "apri" se l'elenco è chiuso; per i consumabili dell'alchimista "descrizione") e cosa fa il doppio click da quella parte (`aiutoDoppioClickSinistra`/`Destra`: vendi e compra per armaiolo e venditore di pergamene, sposta nell'inventario del gruppo ed equipaggia per l'inventario, metti sul banco di lavoro e rimuovi per l'incantatore, acquista dall'alchimista, dove i consumabili del gruppo non si vendono); se l'elenco da quella parte è più alto del riquadro, e quindi si scorre (`ComponenteScorrevole.isScorrevole`), un terzo cartiglio "Rotella su o giù: scorri l'elenco", che compare ovunque dentro l'elenco, anche fuori dai nomi. I cartigli impilati sono centrati fra loro. Nella schermata di gioco hanno l'aiuto anche due riquadri, disegnato da `DisplayableCanvas` dopo tutto il resto: quello delle missioni (sul nome di una missione "Click: chiudi descrizione", o "apri"; e la rotella se l'elenco è più alto del riquadro) e quello del gruppo (solo la rotella, quando i personaggi, ospiti compresi, sono più dei 5 che il riquadro mostra). I cartigli impilati accanto al mouse e tenuti dentro l'area (`Cartiglio.disegnaAccantoAlMouse`) sono gli stessi del nome delle caselle sulla mappa.
- **Copyright**: tre righe ("La Foresta", "copyright 1984-2026", "Stefano Reksten") nell'angolo, nel font piccolo grigio scuro.
- **Barra Dock** (`DisplayableCanvasBarraIconeDock`, **attiva per default**, solo in orizzontale): estende la classica, con le stesse scelte, pagine e frecce, ma l'icona sotto il cursore si ingrandisce, le vicine un po' meno (profilo a campana) e la fila si allarga; le icone poggiano sul fondo e crescono verso l'alto, fuori dal rettangolo della barra, che reclama come suoi anche quei punti (`contiene`) perché siano cliccabili. L'effetto cresce all'ingresso e si sgonfia all'uscita in 120 ms (`DURATA_TRANSIZIONE_NANOS`); durante la transizione la barra chiede il ridisegno anche a mouse fermo (`inTransizione`). Con `BARRACLASSICA` si torna alla barra fissa.

## 4. Il ciclo di rendering

`addNotify()` avvia un **thread daemon** che gira a **60 fotogrammi al secondo** (`Temporizzatore.FRAME_PER_SECONDO`). Il thread si limita a chiamare `repaint()`: il disegno vero avviene sull'EDT in `paintComponent`, con il doppio buffering di `JPanel`; non contiene logica di gioco.

La cadenza è fissa e senza deriva: la scadenza di ogni fotogramma si calcola dalla precedente, non da quando finisce il sonno, e se si è in ritardo di oltre un fotogramma (per esempio dopo una sospensione) si riparte da adesso invece di recuperare con una raffica di ridisegni.

Il thread ridisegna solo quando serve: negli stati animati (gioco, mappa, intro, inventario, commerciante, alchimista, incantatore, intermezzo), se c'è un annuncio globale attivo o in coda, o se la barra Dock è in transizione. Negli stati statici (statistiche, slot, trofei...) il ridisegno dipende dagli handler del mouse e dagli eventi.

**Il tempo delle animazioni è quello reale**, misurato con `System.nanoTime()`, non un passo fisso per fotogramma: lo fanno `SpriteBase`, `Notiziario`, la barra Dock, l'intro, l'intermezzo. Il disegno può infatti essere richiesto anche da un `repaint()` estraneo al thread (per l'hover del mouse) e un conteggio dei fotogrammi sarebbe sbagliato.

`aggiornaSchermo` sceglie cosa disegnare in base allo stato e poi, **in qualunque stato**, sovrappone tre strati: l'annuncio globale attivo, la barra icone e la sfera magica (un'immagine decorativa nell'angolo in basso a sinistra, disegnata solo in `STATO_MAPPA` e solo quando ci sono notizie da mostrare). A ogni fotogramma `segnalaSeUiDiventataInattiva` guarda se sprite, annunci e rivelazioni sono tutti esauriti: al passaggio da occupata a inattiva pubblica `InternoUiInattiva`, con cui l'automa sa che può mostrare un intermezzo (vedi [`motore_di_gioco.md`](motore_di_gioco.md) §10).

## 5. Comunicazione motore → UI

`ForestaUI` e `DisplayableCanvas` (che si iscrive a sua volta, in `registratiAEventi`) sono i due punti in cui il bus entra nella UI. Le tre famiglie di eventi rilevanti sono `Notifica*` (fatti compiuti: variazioni di statistiche, stato vitale, testo, fine gioco, raccolta oggetti, trofei...), `Richiesta*` (input necessario: direzione, sì/no, incantesimo, missione, testo, slot) e `Interno*` (tecnici: stato di gioco, comandi disponibili, primo piano, caricamento, preparazione della locazione...). Ogni handler traduce l'evento in una chiamata sul canvas; alcuni forzano un ridisegno immediato (`rinfresca()`) per dare feedback subito, per esempio all'apertura e alla chiusura del riquadro del combattimento.

`gestisciEventoStatoDiGioco(InternoStatoDiGioco)` è il punto in cui gli stati della macchina del motore si riflettono in cambi di vista (logo, intro, schermate di creazione del personaggio, scelta della direzione, slot di salvataggio...). Le richieste portano il riquadro giusto in primo piano: la scelta della direzione porta la mappa, quella dell'incantesimo la finestra incantesimi e pozioni, quella di una risposta di missione il testo. Le richieste di aprire una schermata (`ComandoAperturaInventarioGruppo`, `ComandoAperturaInventarioCommerciante`, `...Fornitore`, `ComandoAperturaIncantatore`, `ComandoAperturaTrofei`, `ComandoVisualizzazioneMappa`) portano il canvas nello stato corrispondente.

**Eventi interni alla UI.** Gli eventi `InternoCreazioneSprite*` (a tempo, effetto di stato, fumetto, dissolvenza, annuncio globale) non vengono dal motore: un riquadro li pubblica in reazione a una notifica del motore e li consuma il canvas, che li aggiunge alle proprie liste di sprite o alle code. Così un riquadro fa comparire uno sprite sopra tutto lo schermo senza un riferimento al canvas padre: il bus serve anche da canale interno.

**Eccezione al principio.** `Notiziario` e `DisplayableCanvasMappaATuttoSchermo` leggono direttamente `Notizie.getUltimeNotizie()` (la lista viva dentro `ModelloDati`) a ogni fotogramma; funziona perché tutto gira sull'EDT.

## 6. Sprite

`SpriteInterface` ha due metodi: `anima(Graphics2D)` e `isAttivo()`. `SpriteBase` (astratta) realizza il ciclo di vita comune degli sprite a tempo:

- **interpolazione lineare** di posizione e scala fra uno stato iniziale e uno finale, in funzione del progresso temporale; l'alpha è delegato a `calcolaAlpha`, con due helper di dissolvenza (`dissolvenzaLineareConSoglia`, `dissolvenzaIperbolicaConSoglia`);
- **tempo reale**: il primo `anima()` non fa avanzare il tempo, i successivi sommano i secondi trascorsi; `resettaTempoTrascorso()` fa ripartire da capo;
- `anima` è `final`: lavora su una **copia** del contesto grafico (`g.create()`), applica composite e interpolazione bilineare, chiama `disegna(g, x, y, scala)` della sottoclasse e poi scarta la copia, così nessuno sprite "sporca" i disegni successivi;
- **ombra opzionale** (`applicaOmbra`): una sagoma scura sfalsata in otto direzioni attorno all'immagine, per un effetto contorno senza risorse pre-ombreggiate;
- **auto-disattivazione**: allo scadere della durata lo sprite si spegne e il canvas lo scarta al fotogramma successivo.

Sottoclassi: `SpriteATempo` (variazioni di livello, salute, magia, monete... con icona e testo; dura 1,6 secondi), `SpriteEffetto` (interazioni elementali e effetti di stato su un personaggio: testo che si ingrandisce e sfuma, con colore pieno o a pattern), `SpriteFumetto` (fumetti di testo a tempo, posizionati con `CoordinateFumetto`; nuvola e punta sono metodi statici riusati dagli intermezzi), `SpriteInDissolvenza`, `SpriteAnnuncioGlobale` (banner centrato che si ingrandisce mentre sfuma: missioni, trofei, aumenti di livello, errori).

Fumetti, annunci globali e rivelazioni hanno **ciascuno una coda con un solo elemento attivo alla volta**, perché più notifiche ravvicinate si mostrino in sequenza invece di sovrapporsi. Un annuncio richiesto mentre è ancora a schermo l'ultima pagina di un intermezzo viene rimandato (`annunciGlobaliRimandati`).

`SpriteRivelazioneArtefatto` mostra un artefatto o un ingrediente trovato in un cofano o in un tempio (`NotificaArtefattoTrovato`): un cerchio luminoso con l'oggetto, raggi che girano in senso antiorario allungandosi e ritraendosi, scintille e, sotto, un pannello scuro con nome, descrizione ed effetti. È centrata sul riquadro della locazione, sopra il testo che resta leggibile, e all'uscita vola verso il riquadro del gruppo. Quanto è vistosa dipende dallo *splendore* (0-3: effetti, rarità, livelli sopra quello del mondo) e i raggi prendono i colori degli elementi degli incantamenti. Non blocca il gioco, ha la sua coda (`codaRivelazioni`) e si disegna solo in `STATO_IN_GIOCO`.

## 7. Font bitmap "Doomdark" e testo

Un sistema di rendering del testo **proprietario**, in stile retro (il nome è un omaggio a *Doomdark's Revenge*): produce bitmap pixel per pixel e non usa `Graphics.drawString`.

- `DoomdarkFont` (interfaccia): altezza, spaziatura, padding, larghezza dei glifi e dati dei glifi, un bit per pixel; un carattere mancante solleva `UnsupportedCharacterException`. È realizzata da `DoomdarkFontMedium` e `DoomdarkFontSmall`, con tabelle di byte incorporate nel codice (il medio ha anche le lettere accentate e `! : ; " ( ) - / % +`).
- `DoomdarkTextProducer` compone un buffer di pixel leggendo i bit dei glifi e lo trasforma in un'`Image` con `MemoryImageSource` e una palette a 1 bit (colore o trasparente) di `DoomdarkColorModel` (bianco, quattro grigi, nero, rosso, giallo, verde, marrone). `FontTool` fa l'a capo automatico entro una larghezza massima.
- **Cache** (`ImageCache`): ogni stringa è un'immagine generata al volo, quindi si tiene una cache a riferimenti deboli chiavata per font, colore e testo; un thread "reaper" la ripulisce periodicamente e, se rimuove qualcosa, pubblica `InternoPuliziaCacheDinamicaImmagini`.
- **Alfabeto grande** (`TestoGrande`, immagini in `ImageCache.lettere`/`cifre`): solo lettere minuscole senza accento, cifre e `' , . ?`; le vocali accentate si scrivono con l'apostrofo ("e'"). Serve per la storia dell'intro, i messaggi grandi, le classifiche e il testo degli intermezzi.
- **Pannello di testo** (`DoomdarkTextRectangle2x`, nel riquadro `DisplayableCanvasRiquadroTesto`): un rettangolo scorrevole con **storico** (fino a `Costanti.MASSIMO_MESSAGGI_RICORDATI`, 100): conserva le righe già spezzate alla larghezza del riquadro, si può tornare indietro con la rotella e antepone un cuscinetto di righe vuote perché le righe più vecchie non restino schiacciate sotto la sfumatura. Il riquadro conserva l'immagine finita (testo composto sull'ombra del drago) e la rifà solo quando cambia `getVersione()`, che cresce a ogni testo aggiunto, svuotamento o scorrimento.
- `DoomdarkColorAlternante` restituisce un colore e poi un altro a ogni richiesta (le liste di missioni e di oggetti).

**Ripristino dopo un caricamento**: `InternoCaricamentoCompletato` porta gli ultimi messaggi salvati; `DisplayableCanvas.ripristinaMessaggi` svuota il pannello e li reinserisce dal più vecchio al più recente con la stessa regola dei messaggi in diretta (un paragrafo è preceduto da una riga vuota, una frase va a capo), così il pannello riappare impaginato come prima.

## 8. Le schermate

### I riquadri di gioco

| Riquadro | Contenuto |
| :--- | :--- |
| `DisplayableCanvasRiquadroLocazione` | L'illustrazione della locazione, con gli avversari e i loro fumetti di effetto |
| `DisplayableCanvasRiquadroMappa` | La mappa in piccolo attorno al gruppo, con le nuvole |
| `DisplayableCanvasRiquadroGruppo` | I personaggi con salute, magia, livello, coraggio, valore, stanchezza, carisma; scorrevole a rotella (un personaggio per scatto) |
| `DisplayableCanvasRiquadroStatistiche` | Monete, preziosi, punti esperienza |
| `DisplayableCanvasRiquadroIncantesimiEPozioni` | Gli incantesimi e le pozioni disponibili |
| `DisplayableCanvasRiquadroMissioni` | Le missioni attive come albero scorrevole (§10), con lo stato di apertura dei nodi tenuto nel modello dati |
| `DisplayableCanvasRiquadroTesto` | Il pannello dei messaggi (§7) |
| `DisplayableCanvasRiquadroCombattimento` | Nomi e barre di salute di combattente e avversario durante un round |

Ogni variazione che arriva dal motore genera qui uno sprite a tempo, vicino al valore che è cambiato.

### Intro e schermate di fine

`DisplayableCanvasIntroOutro` disegna, oltre all'intro, la selezione degli slot (con l'elenco dei personaggi di ogni salvataggio), la conferma di uscita, sconfitta, vittoria, statistiche e classifica. L'**intro** è un'animazione legata al tempo, in tre fasi (`Fase`: `STORIA`, `CLASSIFICA`, `TROFEI`): la **storia** (un unico testo nell'alfabeto grande con un alone scuro che sale dal basso sullo sfondo `SfondoStoria`, in dissolvenza ai bordi) con i **loghi** che compaiono appena la storia è salita oltre di loro, restano un poco e spariscono con lo sfondo; la **classifica**, che sale e si ferma; i **trofei**, in un'unica immagine (`ImmagineTrofei`) che scorre fino a uscire. Poi si ricomincia. Lo scorrimento è `ScorrimentoVerticale`, con due modi: automatico (sale a velocità costante, sfuma ai bordi, si può fermare a una quota) e manuale (rotella e frecce), usato nella pagina dei trofei. Il temporizzatore della UI scatta ogni 5 secondi e chiama `avanzaIntro()`, che si limita a far partire l'intro se non è già in corso.

### Mappa a tutto schermo e notiziario

`DisplayableCanvasMappaATuttoSchermo` disegna la mappa conosciuta (con nuvole condivise col riquadro piccolo, §9), la casella del gruppo con un segnalino lampeggiante e gli indicatori; si trascina con il mouse, e il **doppio click** la centra (lo gestisce). Passando sopra una casella conosciuta che ha un nome compare un **cartiglio** accanto al puntatore (un nastro arancione con i due capi arrotolati; `Foresta.getNomeDaMostrare`); niente compare mentre si trascina o sopra il notiziario. L'immagine delle caselle non si ricostruisce a ogni fotogramma: `preparaMappa()` la costruisce a ogni apertura della schermata, perché mentre è mostrata nessuna casella può cambiare.

Il **notiziario** (`Notiziario`) è un ticker a scorrimento orizzontale in una fascia blu sotto la mappa, alta quanto un'icona di mappa, che esiste **solo quando ci sono notizie** (`altezzaMappa()` torna a tutta l'altezza se non ce ne sono). Tutte le notizie sono composte in un'unica immagine (titolo in giallo, corpo in bianco, separate sul primo " - "; il separatore è un'immagine a sé perché gli spazi del font sono troppo stretti), ricostruita solo quando la lista cambia e disegnata **affiancata più volte** per uno scorrimento continuo. Scorre a **90 pixel al secondo** in tempo reale, con una sfumatura sul bordo destro; a ogni ricostruzione riparte da destra, così la notizia più recente (la prima della striscia) si vede subito. La fascia parte da metà della larghezza della sfera magica, perché l'altra metà è coperta dalla sfera. Il confronto con la lista precedente è per identità degli oggetti `Notizia` (che non ridefinisce `equals`): corretto finché ogni notizia nuova è un oggetto nuovo, e dopo un caricamento lo è sempre. Mappa e nuvole sono ritagliate sulla fascia sopra il notiziario e i click sulla fascia non iniziano un trascinamento.

### Inventario, negozi, trofei

- `DisplayableCanvasScambiatore` è la base per disegnare un'interazione fra due elenchi di oggetti scambiabili (uno a sinistra, uno a destra) con, nella colonna centrale, un personaggio o il commerciante. `DisplayableCanvasScambiatoreArtefatti` serve per personaggio, gruppo e armaiolo; `DisplayableCanvasScambiatoreConsumabili` per gruppo e alchimista. Spostare un oggetto manda un comando al motore (`ComandoSpostamentoArtefatto`, acquisto, vendita...) e l'oggetto si muove solo dopo l'approvazione: la UI reagisce alle notifiche di approvazione e rifiuto, che mostrano un fumetto col motivo.
- `DisplayableCanvasInventario` (personaggio e gruppo, con gli attributi del personaggio in un albero scorrevole che evidenzia quelli che l'oggetto sotto il cursore modifica), `DisplayableCanvasCommerciante` (una sola schermata per armaiolo e venditore di pergamene, che cambia nome e immagine secondo il negozio), `DisplayableCanvasIncantatore` (inventario a sinistra, banco di lavoro a destra, e al centro monete, costo della fusione e posti dell'artefatto). Il **doppio click** è la scorciatoia: su un oggetto lo sposta dall'altra parte (equipaggiare, mettere nel gruppo, vendere, comprare), da un'icona dell'alchimista compra, nell'inventario su un attributo del personaggio ne spende un punto abilità.
- `DisplayableCanvasTrofei`: la pagina dei trofei, che si apre dall'inventario (`MOSTRA_TROFEI`) e si chiude con `ANNULLA`, con rotella e frecce a passi più lunghi.

### Intermezzi

`DisplayableCanvasIntermezzo` è la schermata a tutto schermo dello stato `STATO_INTERMEZZO`, in cui il canvas passa a ogni `NotificaPaginaIntermezzo`. Disegna la pagina a strati in funzione dei **secondi reali** trascorsi da quando è comparsa (lo stesso metro del timer con cui il motore fa avanzare le pagine, quindi animazioni e dialoghi restano allineati anche se qualche fotogramma va perso):
- **sfondo**: l'immagine della pagina, stirata secondo il suo `TipoStiramento`, oppure l'ombra del drago;
- **elementi**: per ciascuno lo stato al secondo corrente calcolato dal modello (centro, scala, opacità con `AlphaComposite`, verso con larghezza negativa), con interpolazione bilineare. Uno sprite sheet si ritaglia una volta sola in sotto-immagini (`getSubimage`); un'`Animazione` crea un'istanza di `AnimazioneImmagine` per elemento, tenuta finché dura l'intermezzo, a cui si chiede il fotogramma a ogni frame;
- **testo** in alto, delegato a `DisplayableCanvasIntroOutro.scriviTestoCentrato()` che possiede l'alfabeto grande;
- **fumetti** delle battute visibili, con la punta alla bocca di chi parla, così segue un personaggio in movimento; la nuvola è costruita una volta sola per pagina.

`AnimazioneImmagine` (un solo metodo, `getFotogramma(secondi, stato)`, che non deve allocare immagini a ogni chiamata) e `AnimazioniIntermezzo` (associa i valori dell'enum `Animazione` alle classi) permettono animazioni disegnate da codice; l'esempio è `AnimazioneFuocoDaCampo` (otto fotogrammi a dieci al secondo). Le immagini di tipo risorsa si caricano al primo uso con `BufferedImageBuilder.provaACaricare()` (se manca, l'elemento si salta con un errore) e si dimenticano a fine intermezzo. `disegnaAl(graphics, secondi)` disegna la pagina a un istante preciso: lo usa `AnteprimaIntermezzo`, tra i sorgenti dei test. Guida pratica: [`intermezzi.md`](intermezzi.md).

## 9. Effetti speciali (`ui.sfx`)

- **`CloudManager`, `CloudInstance`, `CloudGenerator`**: un unico set condiviso di **12 nuvole** generate proceduralmente, usato sia dal riquadro mappa sia dalla mappa a tutto schermo, così le nuvole restano coerenti passando da una vista all'altra. Ogni nuvola ha una velocità di parallasse proporzionale alla propria larghezza; il riposizionamento fra le viste usa coordinate "canoniche" (solo traslazione, nessuno scaling, perché hanno la stessa dimensione ovunque).
- **`TracciatoreLogo`** (circa 1400 righe): una pipeline di elaborazione immagine che traccia il contorno di un logo con uno o più punti luminosi ("corridori") e poi lo fa apparire con una dissolvenza. Si configura con `TracciatoreLogo.costruttore(logo)...costruisci()` (sfondo per alpha o colore, regione, corridori, versi, ritardi, velocità, torcia, dissolvenza, scie, pausa, scomparsa, ripetizione), si fa avanzare con `avanza()` (ogni `INTERVALLO_FOTOGRAMMA_MS` = 16 ms) e si disegna con `disegna(g2)`. L'algoritmo ha una maschera primo piano/sfondo, il tracciamento del contorno esterno con il metodo Moore-Neighbor, il rilevamento dei buchi interni e il ricampionamento a distanza costante per una velocità uniforme; si prova da riga di comando con `TracciatoreLogoDaRigaDiComando`, tra i test. È ben isolato dal resto della UI.

## 10. Immagini e componenti riutilizzabili

**Immagini.** `ImageCache` (statica) tiene le cornici, gli sfondi dei negozi, i separatori, le lettere e le cifre dell'alfabeto grande, le illustrazioni per `ClassiLocazione` e le sue varianti di bosco, gli sprite delle variazioni, le icone, il segnalino e l'indicatore, caricate da `/com/threeamigos/foresta/img/`. Le icone e le immagini dei personaggi e degli oggetti stanno in `ClasseIcona`, `ClassePersonaggioImmagine` e `ClassiOggettoImmagine`, non nel motore, perché questo resti utilizzabile senza schermo (lo controlla un test in una JVM headless). `BufferedImageBuilder` carica via `ImageIO` e crea una copia compatibile con la configurazione grafica dello schermo, per evitare conversioni a ogni `drawImage`. Due livelli di tolleranza: `buildBufferedImage` chiude il gioco se la risorsa manca (le risorse di base, all'avvio), `provaACaricare` registra l'errore e restituisce `null` (le risorse caricate al volo, come gli intermezzi).

**Componenti.**
- `ComponenteScorrevole<T>`: elenco ad albero scorrevole generico; ogni nodo ha chiave, descrizione, icona opzionale e un riferimento a un oggetto `T` per risalire da un click all'oggetto di dominio. È la base del riquadro missioni, delle liste di artefatti e di attributi.
- `Prompt`: un pannello nel layered pane con un `JTextField` per l'input libero (nome del personaggio, nome per la classifica, nome dell'artefatto). Richiama sempre su di sé il focus; non lascia scrivere il carattere `|` (che è il separatore dei salvataggi) e lo toglie dal testo incollato; alla conferma pubblica `ComandoInvioTesto`. Il campo è Swing e non AWT perché un componente AWT nativo ha una visibilità sua e comparirebbe anche a prompt nascosto.
- `CoordinateFumetto`: dove disegnare un fumetto e verso quale punto punta la freccia.
- `Orientamento`: `ORIZZONTALE`/`VERTICALE`, che pilota i due layout della finestra; `ForestaUI` lo traduce nelle costanti intere `DisplayableCanvas.ORIENTAMENTO_*`.

## 11. Test

`src/test/.../ui` contiene test per l'annuncio globale, il commerciante, il nome sotto il mouse sulla mappa, l'ordinamento degli artefatti, lo scorrimento verticale, la rivelazione degli artefatti e i componenti di `sfx`, oltre agli strumenti `AnteprimaIntermezzo` e `TracciatoreLogoDaRigaDiComando`.

## 12. Osservazioni

- **`DisplayableCanvas` è molto grande.** Quasi 1300 righe: possiede lo stack, il routing del mouse, il thread di animazione, i riferimenti a tutti i riquadri, le code di sprite e la geometria. Una nuova schermata richiede di toccarlo in più punti (costruttore, `aggiornaSchermo`, `finestraATuttoSchermo`, il ciclo di animazione, gli handler).
- **Layout interamente manuale.** `setLayout(null)` e coordinate calcolate sommando larghezze di cornici: adatto a un'estetica pixel-perfect, ma cambiare una dimensione in `ImageCache` obbliga a ricontrollare a mano le posizioni derivate. Il notiziario aggiunge un vincolo (la fascia dipende dalla larghezza della sfera magica).
- **Ridisegno negli stati statici.** Dipende dai `repaint()` sparsi negli handler del mouse e negli eventi; chi aggiunge un nuovo handler deve ricordarsi di farlo.
- **Barra verticale con layout incoerente.** Le icone sono alte 64 px ma avanzano di 32 (`offset += 32`): si sovrappongono per metà e il click va sempre a quella più in alto. In orizzontale lo spazio è calcolato su un passo di 66 px ma le icone avanzano di 62. In verticale la finestra è larga `min(schermo, 400)` ma l'area di contenuto è fissata a 640 px: il contenuto viene tagliato e la colonna delle icone gli si disegna sopra. Sono due problemi già segnalati anche come `FIXME` in `Automa`, insieme a quello degli schermi alti meno di 804 px, dove la barra copre il fondo del riquadro delle missioni.
- **Due rappresentazioni dell'orientamento**: l'enum `Orientamento` e le costanti intere `DisplayableCanvas.ORIENTAMENTO_*`, tradotte in `ForestaUI`. Passare direttamente l'enum eliminerebbe la conversione.
- **Doppio click.** Il rinvio di 175 ms ha i due limiti descritti in §2 (timer non fermato al cambio di schermata, intervallo non quello del sistema).
- **Stato di dominio letto dalla UI.** Il notiziario e la mappa a tutto schermo leggono la lista viva delle notizie a ogni fotogramma.
- **Errore fatale per le risorse di base.** `buildBufferedImage` chiude il gioco con `System.exit(1)` se una risorsa manca, dopo averlo scritto nel log: è una scelta "fail fast", senza un messaggio a video per il giocatore.
- **`TracciatoreLogo`** è generico, ben documentato e isolato dal resto: un buon candidato per essere estratto in un modulo a sé.
