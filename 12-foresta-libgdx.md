# 12-foresta-libgdx

L'**interfaccia grafica in libGDX**: la stessa partita di `12-foresta-swing` con un altro toolkit, per girare su Android e sulla famiglia Apple (iMac e Mac, iPhone, iPad) oltre che sul desktop. Swing resta come alternativa: ogni pezzo di UI si porta anche qui, e quel che non dipende dal toolkit sta in `12-foresta-ui-comune`, non si copia. Il piano e l'ordine dei lavori sono in [`porting_libGDX.md`](porting_libGDX.md).

È l'unico modulo (insieme a `13-foresta-app-libgdx`) che usa `com.badlogic` (lo controlla `DipendenzeDalToolkitGraficoTest`), e non usa mai `java.awt`, Swing né `javax.imageio`: deve poter girare su Android.

## Dipendenze

**Dichiarate nel pom:** `12-foresta-ui-comune`, e `com.badlogicgames.gdx:gdx` (versione `libgdx.version` nel pom padre, oggi 1.14.2, con bytecode Java 8).

**Lo dichiarano:** `13-foresta-app-libgdx`.

## Funzionamento

**Le coordinate sono quelle di Swing**, con l'asse y verso il basso e (0, 0) in alto a sinistra: la camera è capovolta (`OrthographicCamera.setToOrtho(true, …)`, dentro un `FitViewport` 1142×656) e le regioni di texture sono già capovolte in verticale, così riquadri e schermate riusano le loro coordinate.

- **`ImmaginiGdx`** dà le immagini come `TextureRegion`, chieste per nome come in Swing (`"icone/Aiuto.png"`): se la cartella ha un atlante (`AtlanteDiImmagini`, nel modulo comune, che legge il descrittore) la regione è presa dalla sua pagina, che si carica una volta sola; altrimenti è un file a sé (loghi, font, scene grandi). Campionamento `NEAREST`; `disegna(batch, nome, x, y, scala)` ingrandisce (le immagini dei file sono a metà risoluzione: la scala è lo zoom, 2). Le texture vanno liberate con `dispose()`.
- **`TestoGdx`** scrive il testo in font Doomdark glifo per glifo, senza immagini di scritte: i glifi di un font (dati del modulo comune, `DoomdarkFont`) si raccolgono all'avvio in una sola texture bianca la cui alfa è il livello del pixel, e una scritta è una fila di sprite tinti del colore (`DoomdarkColorModel.Color`); `disegna`, `disegnaConOmbra`, `disegnaSuPiuRighe` (usa `FontTool.split`). `DisposizioneDeiGlifi` calcola dove stanno i glifi nella pagina (senza libGDX, provata da `DisposizioneDeiGlifiTest`).
- **`InputGdx` e `PianificatoreGdx`** collegano libGDX al router degli input del modulo comune (`RouterDegliInput`, lo stesso di Swing): `InputGdx` traduce mouse e dito nelle chiamate del router (coordinate del mondo virtuale dal viewport, click e doppio click ricavati dal tocco), `PianificatoreGdx` fa scattare i timer del router (click singolo che aspetta, rotella a passi) a ogni fotogramma, da chiamare con `aggiorna()` in `render`.
- **`RiquadroTestoGdx`** è il primo riquadro della schermata di gioco, quello del testo (`DisplayableCanvasRiquadroTesto` in Swing): lo storico, lo scorrimento e i gesti sono quelli del modulo comune (`RiquadroTesto`, `StoricoDelTesto`), qui c'è solo il disegno: sfondo nero con l'ombra del Drago al 30%, le righe che si spengono verso l'alto (`TestoGdx.disegnaConSfumatura`: luminosità sfumata in verticale per riga, un colore per vertice) ritagliate al rettangolo (`RitaglioGdx`, scissor del mondo capovolto) e le frecce su e giù.
- **`ForestaGdx`** è il punto di partenza (`-Dforesta.pagina=N` parte da una pagina; 1 è il riquadro del testo, su cui rotella e trascinamento funzionano): scrive nel log `GL_VERSION`, `GL_RENDERER` e `GL_MAX_TEXTURE_SIZE`, e mostra, una pagina alla volta (spazio o tocco), la pagina di prova (`sviluppo/PaginaDiProva`: testo nei due font e in tutti i colori, accenti, testo a capo, immagini per nome) e le pagine degli atlanti.

## Osservazioni

- Prima misura reale (Mac con M2 Pro, driver Metal): `GL_MAX_TEXTURE_SIZE` = 16384. Su un Realme Pad (Mali-G52) il valore va letto sul dispositivo; il limite che ci si dà è 2048 per lato.
- Il testo è fedele a Swing: la riga è alta quanto l'immagine del font (le righe in più per gli accenti comprese) e l'ombra è una seconda passata in nero spostata di qualche pixel.
- Le texture vivono nella memoria della scheda grafica e non le libera il GC: vanno liberate con `dispose()`.

## Da fare / idee aperte

- Il testo grande (`TestoGrande`, con alone) e l'alfabeto grande a colori (`ImageCache.glifoGrande`).
- Il resto del piano di porting: riquadri, schermate, sprite, input a tocco, logo (`TracciatoreLogo` con `ShapeRenderer`), `FinestraDiProva` (solo desktop).
