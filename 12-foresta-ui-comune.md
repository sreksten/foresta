# 12-foresta-ui-comune

Quel che le **interfacce grafiche hanno in comune**, senza dipendere da nessun toolkit: le **immagini del gioco** (tutte le risorse sotto `img/`, in PNG), i testi della UI (`ui_<lingua>.properties`) e la logica d'interfaccia che non disegna (le finestre, lo scorrimento, i font Doomdark come dati, i comandi possibili), che prima stava in `12-foresta-swing`. Serve perché il gioco abbia due front-end, Swing e libGDX (per Android e per la famiglia Apple: iMac, iPhone, iPad), senza copiare il codice da uno all'altro.

Non usa `java.awt`, Swing, `javax.imageio` né libGDX: lo controlla `DipendenzeDalToolkitGraficoTest`, che legge i sorgenti.

## Dipendenze

**Dichiarate nel pom:** `11-foresta-motore-api`, `08-foresta-tattiche`.

**Lo dichiarano:** `12-foresta-swing`.

## Funzionamento

### Le immagini

Stanno in `src/main/resources/com/threeamigos/foresta/img/` (quelle già raggruppate in un atlante, vedi sotto, nei jar non ci sono più come file singoli), in cartelle per tipo: `fondi` e `fondinon2x2` (cornici e sfondi), `icone`, `locazioni`, `mappa`, `oggetti`, `personaggi`, `font` (le immagini dei font Doomdark), più i loghi. Sono tutte **PNG**: erano GIF, convertite una volta (nessuna era animata) con i pixel identici (RGBA confrontato file per file). Le due interfacce le leggono per nome (`/com/threeamigos/foresta/img/<cartella>/<nome>.png`). Sono a metà risoluzione: ogni interfaccia le ingrandisce a runtime con lo zoom (`LivelloDiZoom`).

### Gli atlanti e gli originali

Le immagini piccole (icone, tessere della mappa, oggetti) stanno nei jar solo **raggruppate in un atlante**, un PNG per cartella con il suo descrittore: `img/<cartella>/atlante_<cartella>.png` e `.txt`. **Le immagini originali non si buttano via**: stanno in `sorgenti-immagini/<cartella>/` (nel modulo, fuori da `src/main/resources`, quindi **non entrano nei jar**) e servono a rifare l'atlante quando se ne aggiunge o se ne cambia una. Vale per ogni cartella per cui si fa un atlante, e varrà anche per gli sfondi delle locazioni, che potrebbero essere ricostruiti uno per uno.

Come si rifà un atlante: si aggiunge o si cambia il file in `sorgenti-immagini/<cartella>/`, si aggiunge il nome al descrittore (a griglia, a mano; libero, con `impagina_atlante.py <cartella> [larghezza] [--in-cima=…] [--fuori=…]`, che **sovrascrive** il descrittore) e si lancia `python3 risorse_e_documenti_vari/atlanti/costruisci_atlante.py <cartella>`. Il test `AtlantiDelleImmaginiTest` fallisce se un originale non sta nell'atlante con gli stessi pixel, o se nell'atlante c'è un nome senza originale.

`AtlanteDiImmagini` (nel modulo comune) legge il descrittore e ritaglia un'immagine per nome (`AtlanteDiImmagini.cerca("icone/Aiuto.png")`); Swing lo usa in `BufferedImageBuilder` e `DimensioniRisorsa`, quindi il codice continua a chiedere le immagini per nome come prima. Un file a sé nelle risorse (per esempio `ManoPuntatore`, 60×64, o i fondi) si legge direttamente.

**Alias.** `ALIAS nome altro` dice che l'immagine `nome` è identica ad `altro` e ne usa la cella: lo script controlla che i pixel siano davvero gli stessi e `AtlantiDelleImmaginiTest` lo ricontrolla. Servono per i segnaposto uguali; finché sono uguali si risparmia lo spazio, e quando si disegna quella vera si toglie la riga ALIAS e si aggiunge l'immagine all'atlante.

**Formati del descrittore.** *A griglia* (celle tutte della stessa misura, quadrate o rettangolari; la prima riga e la prima colonna del PNG sono rosse, e a destra e in basso di ogni cella c'è la sua linea rossa, come nell'immagine dei font): `CELLA n` o `CELLA LxA`, `COLONNE n`, `FUORI nome…`, `ALIAS nome altro`, una riga di nomi per riga di celle, `nome@LxA` per le immagini più piccole della cella, `-` per una cella vuota. *Libero* (immagini di misure diverse, ognuna col suo riquadro rosso di un pixel): `LIBERO`, `LARGHEZZA`, `ALTEZZA` e una riga `nome x y larghezza altezza` per immagine.

- `img/icone/atlante_icone.png` (727×265, celle da 32×32, 22 colonne e 8 righe: le celle vuote costano memoria, quindi la larghezza è quella della riga più lunga) raggruppa le icone (139 più 3 alias: `Amicizia` = `Carta`, `MoglieDelBardo` = `Donna`, `Locandiere` = `Uomo`): personaggi giocanti, mostri e comparse, incantesimi, abilità e canti, direzioni, frecce e azioni di gioco, scelte e conferme, negozi e schermate di contorno (con a destra le due icone scherzose), consumabili e valori (con a destra gli sfondi delle icone). Fuori dall'atlante c'è solo `ManoPuntatore` (60×64).
- `img/mappa/atlante_mappa.png` (154×35, celle da 16×16, 9 colonne e 2 righe): terreni e castello (le quattro foreste sono le varianti del bosco), luoghi, botteghe e segni sulla mappa.
- `img/locazioni/atlante_locazioni.png` (589×484, celle da 195×160, 3 colonne e 3 righe, nessuna cella vuota): gli sfondi delle locazioni. Le cinque botteghe (locanda, alchimista, armaiolo, incantatore, venditore di pergamene) per ora hanno lo stesso disegno: sono **alias** della locanda (`ALIAS Alchimista Locanda`…), cioè usano la sua cella, ma ognuna ha il suo originale; quando se ne disegna una, si toglie la riga ALIAS e la si aggiunge all'atlante.
- `img/fondi/atlante_fondi.png` (452×1353, impaginazione libera, 90% dei pixel occupati, 20 immagini): le cornici dei riquadri, i separatori, le mani di carta, forbici e sasso (sinistra e destra), il trionfo e l'ombra del Drago.
- `img/fondinon2x2/atlante_fondinon2x2.png` (338×558, impaginazione libera, 13 immagini): i separatori degli elenchi, i capi dei cartigli, la sfera magica e le lune (le immagini di questa cartella non sono a metà risoluzione, vedi `todo.md`). **Restano file a sé** (in `FUORI`) le scene grandi degli intermezzi, che si usano una alla volta e pesano molto: gli interni e i primi piani dei cinque negozi (500×348) e `SfondoStoria` (1136×928). Per queste gli originali sono i file stessi, nelle risorse.
- `img/personaggi/atlante_personaggi.png` (327×786, impaginazione libera, 90% dei pixel occupati, 53 immagini più 2 alias, `MoglieDelBardo` = `Donna` e `Locandiere` = `Uomo`): i personaggi giocanti stanno nella fascia in alto (`impagina_atlante.py personaggi 1024 --in-cima=Guerriero,… --alias=MoglieDelBardo:Donna,Locandiere:Uomo`), gli altri sotto e negli spazi che restano.
- `img/oggetti/atlante_oggetti.png` (280×534, 29 oggetti, impaginazione libera, 92% dei pixel occupati), perché le misure vanno da 16×18 a 160×160 e 53×183.

### Leggere i pixel senza toolkit

`ImmagineDiPixel` è un'immagine come griglia di pixel ARGB, e `LetturaPng` la legge da un PNG usando solo il JDK (`java.util.zip`), senza `javax.imageio` (che su Android non c'è): capisce grigi, RGB, palette a 1, 2, 4 e 8 bit, alfa e trasparenza `tRNS`, non interlacciati. Un test di `12-foresta-swing` (`LetturaPngTest`) controlla che per ogni PNG del gioco i pixel siano gli stessi di ImageIO. Chi ha un'immagine del toolkit la converte in `ImmagineDiPixel` col suo ponte (`ImmaginiAwt.pixel` in Swing).

### Il testo bitmap Doomdark come dati

`DoomdarkFont` (piccolo e medio), `AtlanteDiGlifi`, `ImmagineDelFont` e `ImmagineDelFontAColori` leggono la griglia di glifi dell'immagine del font (`img/font/`) e ne ricavano, per ogni carattere, un `GlifoDelFont` (livelli di grigio che fanno da alfa) o un `GlifoAColori`, con le larghezze misurate sui pixel; `CaratteriDelFont` è l'elenco dei caratteri, `FontTool` divide il testo in righe, `DoomdarkColorModel.Color` sono i colori del testo (con il loro RGB pieno). Come un glifo diventa un'immagine lo decide il toolkit: Swing con un modello a due indici (`ModelliDiColoreAwt`), libGDX con una tinta.

### Il router degli input

`RouterDegliInput` instrada mouse e tocco alle finestre, senza toolkit: trova a chi spetta un evento (la barra delle icone per prima, poi l'eventuale finestra a tutto schermo, altrimenti la più in primo piano della pila che contiene il punto), dà a ogni finestra le coordinate sue, sintetizza entrata e uscita, fa catturare il trascinamento a chi lo chiede (`Finestra.catturaIlTrascinamento`), porta davanti un riquadro impilato cliccato e non ultimo, ritarda di 175 ms il click singolo di chi distingue il doppio click (e lo scarta se arriva il secondo o se la schermata cambia) e divide la rotella in passi per fotogramma (`Finestra.passiPerScattoDellaRotella`). Da chi lo ospita vuole un `RouterDegliInput.Contesto` (la barra, la finestra a tutto schermo, la pila, le aree `Area`, lo stato, il ridisegno) e un `Pianificatore` (timer: `PianificatoreSwing` in Swing con un `javax.swing.Timer`, `PianificatoreGdx` in libGDX, a ogni fotogramma). Gli eventi arrivano in coordinate dello schermo: `mosso`, `premuto`, `trascinato`, `rilasciato`, `clic` (con il conteggio, 2 per il doppio), `rotella`, `mouseEntrato`, `mouseUscito`. In Swing `DisplayableCanvas.GestoreMouse` traduce gli eventi di AWT; in libGDX `InputGdx` traduce quelli di `InputProcessor` e ricava i click (un tocco rilasciato quasi dove è cominciato) e il doppio click. `RouterDegliInputTest` lo prova con finestre finte.

### Il riquadro del testo

`StoricoDelTesto` (le righe già spezzate alla larghezza, il cuscino di righe vuote, il limite di `NotizieMD`, l'offset di scorrimento a pixel e `perOgniRigaVisibile`, che dice a chi disegna dove sta ogni riga) e `RiquadroTesto` (la finestra: `addString`, `clear`, rotella, trascinamento con inerzia ed elastico, ritorno alla riga più recente) sono il modello del riquadro dei messaggi; `DisplayableCanvasRiquadroTesto` in Swing e `RiquadroTestoGdx` in libGDX lo estendono e ci mettono sopra il disegno.

### Il resto

Le finestre e l'input (`Finestra`, `TipoFinestra`, `Orientamento`), lo scorrimento su un asse (`ScorrimentoSuUnAsse`), lo zoom (`LivelloDiZoom`), i comandi possibili (`ComandiPossibili`), i testi della UI (`TestiDellaUI`), il segnalatore degli errori, le coordinate dei fumetti e il testo delle azioni in combattimento.

## Osservazioni

- Non usa `java.awt`, Swing, `javax.imageio` né libGDX: lo controlla `DipendenzeDalToolkitGraficoTest` (i moduli diversi da `12-foresta-swing` e dai moduli libGDX non possono usarli).
- Il piano di porting, con l'ordine dei lavori, è in [`porting_libGDX.md`](porting_libGDX.md). Man mano che si porta un componente se ne estrae il modello (quel che non disegna) qui.
- L'atlante per libGDX (le immagini piccole raggruppate in pagine) si ricava da questi stessi PNG: non si tengono due copie a mano.

## Da fare / idee aperte

- Estrarre da Swing la logica che non disegna di `JoystickVirtuale`, `ScorrimentoVerticale`, `ComponenteScorrevole`, `SfondoARiempimento`, le tabelle di `TipoIcona`, `MappaBottinoImmagine`, `MappaPersonaggioImmagine` e `VersiDeiPersonaggi`, dei riquadri, della barra icone, del canvas e dei sprite (vedi l'elenco in `porting_libGDX.md`).
- I loghi nella radice di `img/` e i font restano file a sé (il logo si traccia, i font hanno già la loro griglia).
