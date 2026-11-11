# Il layout per telefono

Il piano della Fase 6 di [`porting_libGDX.md`](porting_libGDX.md): come la Foresta si adatta a uno schermo da telefono tenuto in orizzontale, partendo da 800×480, e come si prova dal desktop senza avere i telefoni. Raccoglie le decisioni dell'utente del 2026-11-11 e quello che si è misurato nel codice lo stesso giorno. **Non è ancora fatto niente**: il documento descrive che cosa si farà, e si aggiorna man mano che i passi (§9) si chiudono.

Tutte le misure sono in pixel virtuali a zoom 2 (le immagini sono a metà risoluzione e le ingrandisce lo zoom: vedi `12-foresta-ui-comune.md`), con `SP` = 4 la spaziatura fra i riquadri (`Finestra.SPACING`).

## 1. Le decisioni

1. **Due layout, scelti dalla misura vera dello schermo.** Il layout di oggi (lo chiamiamo «del tablet»: mappa con la sua cornice, riquadro delle statistiche, tre riquadri impilati) resta dove ci sta; altrimenti si usa il layout «del telefono». Su Android la scelta è automatica; sul desktop si chiede con un argomento (§3).
2. **Nel layout del telefono la mappa diventa un riquadro della pila**, come quello delle missioni, **senza la sua cornice** (`CorniceMappa.png`): la cornice è quella grande dei riquadri impilati.
3. **Monete, preziosi e punti vanno in cima alla colonna «Inventario gruppo» della schermata dell'inventario** (gli oggetti del gruppo, che si prendono e si equipaggiano): tre righe fisse, un separatore, e sotto l'elenco scorrevole degli oggetti. Nella schermata di gioco non si vedono: il riquadro delle statistiche nel layout del telefono non c'è (§4.3).
4. **Senza il riquadro delle statistiche non ci sono gli sprite delle variazioni** di monete, preziosi e punti (+3 monete…): le tre righe dell'inventario si aggiornano e basta.
5. **Più spazio vuol dire più testo**: un telefono più largo (o più allungato) dà la larghezza in più al riquadro del testo.
6. **Intro e outro**: lo sfondo si ritaglia (non si deforma); le scritte scorrevoli restano; il logo LaForesta si comporta come oggi; il logo 3AM sta sempre in basso a destra, attaccato agli angoli dello schermo vero.
7. **Trofei**: i nomi vanno a capo su più righe, mai oltre il bordo dello schermo.
8. **Si prova dal desktop** con una serie di misure: quella minima (800×480) e quelle dei telefoni più diffusi (§3).
9. Restano le regole di sempre: tutto in Swing e in libGDX, la logica nel modulo comune, passi che compilano, commit solo su richiesta.

## 2. Come si sceglie il layout

### 2.1 Si conta in dp, non in pixel

La misura che conta è quella fisica, cioè in **dp** (density-independent pixel: i pixel divisi per la densità; un dp è un pixel a 160 punti per pollice). Contare i pixel sbaglierebbe: un telefono di oggi da 2400×1080 ha più pixel di un vecchio 800×480 e anche del Realme Pad (2000×1200), ma è alto solo 411 dp. Con il layout del tablet ci si vedrebbe tutto a una scala minuscola. Su Android la densità la dà il sistema (`Gdx.graphics.getDensity()`, o le `DisplayMetrics` nel lanciatore); sul desktop la dà la misura di prova (§3).

### 2.2 La regola

- **Layout del tablet** se lo schermo in dp contiene quello di oggi a scala 1: con la barra delle icone a destra (`VERTICALE`) servono **1094×732 dp** (`DisposizioneDelloSchermo.dimensioniDelloSchermo`). Si disegna come oggi, con il `FitViewport` e le bande dove avanza.
- **Altrimenti, layout del telefono.** L'area virtuale è grande quanto lo schermo in dp, ma mai sotto il **minimo di 842×505** (§4.1). Se lo schermo in dp è più piccolo del minimo, si riduce tutto della stessa scala finché il minimo ci sta in altezza (e in larghezza), e la larghezza virtuale si allunga fino al rapporto dello schermo, quindi senza bande. La larghezza in più va al testo; l'altezza in più (che sui telefoni non c'è: sono tutti sotto i 505 dp) andrebbe al testo e alla pila.

In formule, con lo schermo di `L`×`A` dp: `scala = min(1, A / 505, L / 842)`, area virtuale `L / scala` × `A / scala`.

### 2.3 Che cosa vuol dire sui dispositivi

Le misure dei dispositivi sono quelle note al 2026-11 e vanno ricontrollate sui modelli veri (le densità soprattutto: alcuni produttori le cambiano con le impostazioni di visualizzazione). «Testo» è la larghezza della colonna di sinistra, dove stanno locazione e testo (§4.1); oggi, sul tablet, il testo è largo 642.

| Profilo (§3) | Pixel | Densità | dp | Layout | Scala | Area virtuale | Testo |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| `MINIMO` | 800×480 | 1 | 800×480 | telefono | 0,95 | 842×505 | 390 |
| `ECONOMICO` (HD+, tipo Galaxy A05) | 1600×720 | 2 | 800×360 | telefono | 0,71 | 1122×505 | 670 |
| `GALAXY_S24` | 2340×1080 | 3 | 780×360 | telefono | 0,71 | 1094×505 | 642 |
| `PIXEL_8` | 2400×1080 | 2,625 | 914×411 | telefono | 0,81 | 1122×505 | 670 |
| `PIXEL_8_PRO` | 2992×1344 | 3 | 997×448 | telefono | 0,89 | 1124×505 | 672 |
| `IPHONE_SE` | 1334×750 | 2 | 667×375 | telefono | 0,74 | 898×505 | 446 |
| `IPHONE_15` | 2556×1179 | 3 | 852×393 | telefono | 0,78 | 1095×505 | 643 |
| `IPHONE_15_PRO_MAX` | 2796×1290 | 3 | 932×430 | telefono | 0,85 | 1095×505 | 643 |
| `REALME_PAD` | 2000×1200 | 1,5 | 1333×800 | tablet | 1,09 | 1094×732 | 642 |
| `IPAD` (Air 10,9″) | 2360×1640 | 2 | 1180×820 | tablet | 1,08 | 1094×732 | 642 |

Due cose si leggono dalla tabella:

- **800×480 a densità 1 non è il caso più stretto.** I telefoni di oggi sono alti 360–430 dp, e lì la scala scende a 0,71–0,85. Il corpo del font medio (16 pixel virtuali) diventa di 11–14 dp: leggibile ma piccolo; il minimo consigliato da Android per il testo è circa 12 sp. Se in prova risultasse troppo piccolo, la leva è abbassare il minimo di 505 (§10).
- In compenso **il testo si allarga quasi ovunque**: sui telefoni comuni la colonna di sinistra passa da 390 a circa 640–670, quanto o più di oggi. Solo il minimo e l'iPhone SE restano stretti.

## 3. Le misure di prova dal desktop

### 3.1 L'argomento `SCHERMO=`

Un'opzione nuova di `OpzioniDiAvvio` (modulo comune, quindi uguale per `Main` e `MainLibgdx`):

- `SCHERMO=<profilo>`: uno dei profili della tabella del §2.3 (`SCHERMO=MINIMO`, `SCHERMO=PIXEL_8`…), senza distinzione fra maiuscole e minuscole;
- `SCHERMO=<larghezza>x<altezza>` o `SCHERMO=<larghezza>x<altezza>@<densità>` per una misura qualunque (`SCHERMO=800x480`, `SCHERMO=2400x1080@2.625`; senza densità vale 1). I due lati si possono scrivere in qualunque ordine: il gioco è sempre in orizzontale e il lato lungo è la larghezza;
- un valore che non si capisce è ignorato con una riga nel log, come `ZOOM=`;
- `SCHERMO=` porta con sé `VERTICALE` (sul telefono la barra delle icone sta a destra, come su Android), e anche `TOCCO`, per provare i gesti del dito; un `ORIZZONTALE` scritto esplicitamente vince;
- senza `SCHERMO=` il desktop resta com'è oggi.

Nel codice, nel modulo comune:

- `ui.MisuraDelloSchermo`: pixel, densità, dp; `leggi("2400x1080@2.625")`. La stessa classe porta la misura vera da Android.
- `tipi.SchermoDiProva` (un'enum: i tipi di un modulo stanno nel pacchetto `tipi`): i profili, ognuno con la sua `MisuraDelloSchermo` e una descrizione di una riga, da stampare nel log quando l'argomento è sbagliato (così si vede l'elenco).
- La scelta del layout del §2.2 sta in un posto solo, accanto a `DisposizioneDelloSchermo` (per esempio `DisposizioneDelloSchermo.perLoSchermo(misura, …)`), e la usano Swing, libGDX desktop e Android.

### 3.2 Che finestra si apre

- **libGDX desktop**: la finestra è grande quanto lo schermo simulato **in dp** (un dp vale circa un punto del desktop), e il gioco vi entra con la scala del §2.2. Così si vede il gioco grande più o meno come sul telefono, scritte piccole comprese.
- **Swing**: oggi non scala niente (la finestra è grande quanto lo schermo di gioco e non si ridimensiona). Per vedere il telefono servirebbe una scala nel disegno e la stessa scala, al contrario, sulle coordinate del mouse. **Deciso**: in Swing la finestra è grande quanto l'**area virtuale** (per `PIXEL_8`, 1122×505), senza scala. Si vede che cosa sta dove, ma non quanto è piccolo: per quello c'è il telefono vero.

### 3.3 Sul telefono vero

`AndroidLauncher` (in `foresta-android`) legge la misura dalle `DisplayMetrics`. Va usata **l'area utile**, cioè senza le barre di sistema e senza il notch (`WindowInsets`), non lo schermo intero. Poi la passa a `ForestaLibGDX`, che sceglie il layout come sul desktop. L'orientamento non cambia mentre il gioco è aperto (decisione 4 di `porting_libGDX.md`), quindi la scelta si fa una volta all'avvio.

## 4. La schermata di gioco del telefono

### 4.1 La disposizione

Con la barra delle icone a destra (72 di spessore), da sinistra a destra:

- **colonna di sinistra**, larga `area − 72 − 368 − 3·SP`, cioè 390 nel minimo: la **locazione** (alta 320, l'altezza di `Foresta.png`) e sotto il **testo**, fino al fondo;
- **colonna di destra**, larga 368 (la cornice grande): la **pila** di quattro riquadri grandi (368×288), cioè gruppo, incantesimi e pozioni, missioni e mappa, uno sopra l'altro come oggi (si clicca un'intestazione per portarlo davanti);
- la **barra delle icone**, verticale, che scorre già quando le icone non ci stanno.

Il minimo viene da qui. In larghezza servono SP + 390 (la larghezza di `Foresta.png`) + SP + 368 + SP + 72 = 842. In altezza, il rapporto 800/480 dà 505, e 505 basta alle due colonne:

- a sinistra restano al testo 505 − SP − 320 − SP − SP = **173** pixel (circa 7 righe del font medio);
- a destra la pila, che comincia a quota SP, ha 505 − SP − 288 = 213 pixel per le intestazioni, cioè **53 per ognuno dei quattro riquadri** (la formula di `passoPila`). Oggi, con tre riquadri, sono 38: si prendono meglio di adesso.

`DisposizioneDelloSchermo` diventa capace delle due disposizioni. Oggi calcola un'unica disposizione dalle cornici; per il telefono riceve l'area virtuale del §2.2 e dà le stesse aree (`getLocazione`, `getTesto`, `getGruppo`, …, `getMappa`), con la mappa nella pila e `getStatistiche` vuota. `RIQUADRI_IMPILATI` (oggi 3) diventa una proprietà della disposizione: 3 sul tablet, 4 sul telefono.

### 4.2 La mappa nella pila

Oggi `RiquadroMappa` disegna 7×7 caselle da 32 pixel (224×224) dentro `CorniceMappa.png` (248×248, bordo interno 12). Nel riquadro grande l'interno, con il bordo delle missioni (`RiquadroMissioni.BORDO` = 20), è **328×248**: la finestra di 7×7 ci sta e resta centrata, con 52 pixel di margine ai lati e 12 sopra e sotto.

- `RiquadroMappa` riceve l'area interna invece di calcolarla dal bordo della sua cornice. Chi disegna (Swing: `DisplayableCanvasRiquadroMappa`; libGDX: `RiquadroMappaGdx`) mette la cornice grande al posto di quella della mappa. Le nuvole e il segnalino restano dentro l'area della mappa.
- La finestra resta **sempre di 7×7 caselle**, come sul tablet (deciso): i 52 pixel per lato restano margine.
- Il riquadro della mappa nella pila ha bisogno di un'**intestazione**, come gli altri (il nome con cui lo si riconosce quando sta dietro).

### 4.3 Monete, preziosi e punti in cima agli oggetti del gruppo

Sul telefono il riquadro delle statistiche non c'è, e monete, preziosi e punti **non si vedono nella schermata di gioco**: stanno in cima alla colonna destra della **schermata dell'inventario** («Inventario gruppo», gli oggetti del gruppo che si prendono e si equipaggiano). Chiarito con l'utente il 2026-11-11: una prima stesura di questo piano li metteva per errore nel riquadro Gruppo della pila.

- In cima alla colonna, **tre righe fisse**: monete, preziosi, punti, con l'etichetta a sinistra e il valore a destra. Sotto, il **separatore** (`Separatore.png`, 336×32, largo quanto l'interno della colonna). In tutto 3 righe del font medio, il separatore e due spazi: **112 pixel**.
- Sotto, l'**elenco scorrevole** degli oggetti del gruppo, che comincia dopo la testata e si accorcia di altrettanto. Le tre righe non scorrono; la rotella, il trascinamento, l'oggetto sotto il mouse e l'aiuto valgono solo dentro l'elenco.
- La logica sta in `SchermataDiScambio` (modulo comune): `setStatisticheInTestaADestra`, `testataADestra()` (le righe e il separatore come `ContenutoDiColonna`), `yElenco(boxX)` e `altezzaElenco(boxX)` (dove comincia e quanto è alto l'elenco di una colonna), che usano `dentroElenco` e `ScambioDegliArtefatti.trovaArtefatto`. Swing e libGDX disegnano l'elenco a quella quota e la testata con il loro `DisegnoDiContenuto`. L'opzione la accendono `DisplayableCanvas` e `SchermoGdx` sulla sola schermata dell'inventario, e solo sul telefono.
- Niente sprite delle variazioni: senza il riquadro delle statistiche non nascono (decisione 4).
- Sul tablet niente cambia: le statistiche stanno nel loro riquadro, con gli sprite, e l'inventario è come oggi.

### 4.4 Gli altri riquadri

- **Combattimento**: la cornice è 384×70 e la colonna di sinistra è larga almeno 390. Ci sta anche nel minimo, sotto la locazione (`getYPreferitaDelCombattimento`), centrato su quella colonna (`getColonnaDestraX`).
- **Sfida a carta, forbici e sasso** (`RiquadroSfida`, `CorniceLarga.png`, 576×288): nel minimo la colonna di sinistra (390) è troppo stretta; sui telefoni comuni (640 e più) ci sta. **Deciso**: se la colonna di sinistra è più stretta della cornice, la sfida sta sopra la pila e la copre (è uno scontro: la pila in quel momento non serve), con la sua finestra **centrata** sull'area di contenuto (lo schermo meno la barra).
- **Testo** (`RiquadroTesto`): si adatta già all'area che riceve, e scorre.
- **Incantesimi e pozioni** e **missioni**: invariati, nella pila.

### 4.5 Il tutorial nella schermata del telefono

- `BersaglioDelSuggerimento.RIQUADRO_DELLA_MAPPA` e gli altri riquadri della pila: quando un suggerimento indica un riquadro che sta dietro, va **portato davanti** prima di disegnargli il bordo intorno, altrimenti il bordo dorato circonda un riquadro coperto. Vale anche per il gruppo e le missioni, ma oggi i riquadri sono tre e la mappa non c'è.
- I suggerimenti che parlano di **monete** o puntano al riquadro delle statistiche non devono puntare a un riquadro che sul telefono non c'è (da controllare in `SituazioniDellaUI`, `TutorDelGioco` e nei testi: dove si vedono le monete è l'inventario).
- **I testi che dicono dove stanno le icone**: per esempio `SCELTA_DELLA_CLASSE.PAGINA_1` e `INVENTARIO.PAGINA_2` dicono «le icone in basso». Con la barra a destra non è vero, e succede già oggi sul tablet con `VERTICALE`. **Deciso**: i testi dicono «la barra delle icone», che vale ovunque stia la barra; niente varianti. Vanno cercati tutti (anche nelle altre lingue e nelle varianti `.TOCCO`).
- Il fumetto (`DisposizioneDelFumetto`) si tiene già dentro lo schermo; va provato sull'area più bassa (505).

## 5. Intro, outro e le altre schermate senza partita

Le misure attuali: `SfondoStoria.png` è 1136×928 (in `fondinon2x2`, letta senza zoom), `LogoForesta.png` 432×128 e `Logo3AM.png` 212×100 a zoom 2, `OmbraDelDrago.png` 454×270, `Trionfo.png` 384×280.

- **Lo sfondo della storia** si disegna già centrato e a misura piena, quindi su uno schermo più piccolo si ritaglia da solo. L'area virtuale più larga della tabella (1124) resta sotto i 1136 della larghezza: non si vede mai il bordo. Va verificato che Swing e libGDX lo ritaglino davvero allo schermo e non alla cornice di un riquadro. Se un giorno l'area superasse 1136, servirebbe una scala che lo copra (ingrandendolo il minimo indispensabile e ritagliando il resto): oggi non serve.
- **Le scritte scorrevoli** (`IntroDelGioco`, `ColonnaScorrevole`): la colonna è già larga quanto lo schermo meno i margini e va a capo da sola; le fasce di dissolvenza (`FASCIA` = 40) restano. Su 505 di altezza la storia si vede per meno righe alla volta, ma la velocità (30 pixel al secondo) resta la stessa.
- **Il logo LaForesta**: segue la storia e si ferma con il centro a un terzo dell'altezza (`height / 3`). Con 505 di altezza si ferma a quota circa 104, e resta lo stesso comportamento, compresa la frenata.
- **Il logo 3AM**: è già messo a `width − 20 − larghezza`, `height − 20 − altezza`. Basta che `width` e `height` siano quelli dell'area virtuale vera (che sul telefono comprende tutto lo schermo, anche le bande che oggi il `FitViewport` lascia nere): così sta sempre nell'angolo in basso a destra. Durante l'intro la barra delle icone non c'è, quindi l'angolo è quello dello schermo.
- **La classifica** (`IntroDelGioco`, fase `CLASSIFICA`): il nome sta a sinistra e il punteggio a destra, ognuno su una riga dell'alfabeto grande (`riga(...)`). In 842 − 100 di margini ci stanno; va verificato con il nome più lungo ammesso e il punteggio più alto.
- **Gli slot di salvataggio** (`SchermateSenzaPartita`): le descrizioni vanno già a capo nella larghezza; da provare a 505 di altezza, dove ci stanno meno slot.

### 5.1 I trofei

`PaginaTrofei` (usata dall'intro e dalla pagina dei trofei che si apre dall'inventario) scrive il nome di ogni trofeo nell'alfabeto grande, a sinistra, con accanto a destra la quantità (`ottenuti/necessari`) e sotto la descrizione nel font medio.

- Il nome passa da `LayoutDelTestoGrande.immagine`, che **va già a capo fra una parola e l'altra**. Una parola più lunga della colonna però non si spezza: esce dal bordo. Il titolo della pagina (`SchermataTrofei.getXDelTitolo`) è su una riga sola e centrato: se è più largo dello schermo, la sua x diventa negativa.
- Da fare:
  - una parola troppo lunga si spezza con un trattino all'ultimo posto che ci sta (in `LayoutDelTestoGrande.righe`, così vale per ogni testo grande);
  - anche il titolo va a capo;
  - un **test** che, per ogni lingua (it, en, en-GB) e per la colonna più stretta (842 − 2·`MARGINE` = 742, e 898 − 100 per l'iPhone SE), controlla che nessuna riga di nessun nome, descrizione o titolo superi la larghezza, e che la quantità non si sovrapponga al nome.
- Lo stesso controllo vale per il **bestiario** e il **grimorio** (`SchermataBestiario`, `SchermataGrimorio`), che usano l'alfabeto grande per i titoli.

## 6. Le schermate a tutto schermo

Sono disegnate per l'area di oggi (1022×732): sul telefono l'altezza è 505. Una per una:

| Schermata | Oggi | Sul telefono |
| :--- | :--- | :--- |
| Inventario, armaiolo e pergamene, alchimista, incantatore (`SchermataDiScambio` e famiglia) | colonne in `CorniceInventario.png`, **368×728** | la cornice non ci sta in altezza: vedi sotto |
| Tattiche (`SchermataTattiche`, `EditorDelleTattiche`) | la stessa cornice | come l'inventario; in più il menu a discesa deve aprirsi dentro lo schermo |
| Mappa a tutto schermo (`MappaATuttoSchermo`) | già relativa a `width`/`height`, con il joystick virtuale e il trascinamento | da provare: dovrebbe adattarsi da sola |
| Bestiario, grimorio | margini e pannelli relativi alla misura, titoli nell'alfabeto grande | da provare (§5.1 per i titoli); i due pannelli affiancati (`SPAZIO_FRA_I_PANNELLI`) nel minimo hanno poco spazio |
| Intermezzi (`ScenaDellIntermezzo`) | lo sfondo della pagina centrato alla sua misura, ma i fumetti messi in proporzione allo **schermo** (`battuta.getPuntoX() * width`) | se lo schermo ha un'altra forma, i fumetti si spostano rispetto all'immagine: **corretto** (2026-11-11): le posizioni sono frazioni di una scena fissa di 1022×732, l'area di oggi, centrata sullo schermo come lo sfondo (`ScenaDellIntermezzo`, `DisposizioneDelloSchermo.dimensioniDellaScena`, `intermezzi.md` §5); sul telefono la scena si ritaglia con lo sfondo |
| Campo di testo (`CampoDiTesto`, `Prompt`, `CampoDiTestoGdx`) | posizionato sulle dimensioni dello schermo | da provare, anche con la tastiera di sistema aperta, che su un telefono copre metà dello schermo |

**La cornice dell'inventario** è un'immagine fissa alta 728. **Deciso**: se l'altezza disponibile `X` è minore di 728, la cornice si **taglia nel mezzo**: si disegnano i primi `X/2` pixel dall'alto dell'immagine e gli ultimi `X/2` dal basso, uno sotto l'altro (con `X` dispari, la metà in più va a quella di sopra). I pixel non si ingrandiscono e non si deformano, e il PNG resta quello di oggi: il taglio si fa al disegno, con due ritagli della stessa immagine. Se `X` ≥ 728 si disegna intera, come oggi.

- Il taglio lo calcola il modulo comune (le due fasce: da quale riga dell'immagine e a quale quota dello schermo), e Swing e libGDX disegnano le due fasce.
- Le liste dentro la cornice (gli oggetti, le intestazioni dei negozianti) prendono l'altezza interna della cornice tagliata e scorrono come oggi: se ne vedono meno alla volta.
- Lo stesso vale per le tattiche, che usano la stessa cornice.

## 7. La barra delle icone

È già verticale e scorre (con freccine, trascinamento, inerzia ed elastico): su 505 di altezza ci stanno circa 7 icone da 64 alla volta. Il Dock si ingrandisce sotto il dito. Lungo la barra il bersaglio del tocco resta di 64 pixel virtuali: a scala 0,71 sono **45 dp**, al limite dei 48 consigliati da Android. Da provare con il dito su un telefono vero.

## 8. Che cosa non cambia

- Il motore e i moduli sotto: la scelta del layout è tutta dell'interfaccia.
- Il layout del tablet e quello del desktop senza `SCHERMO=`: identici a oggi, compresi gli sprite delle statistiche.
- Le coordinate di disegno restano in pixel virtuali con l'asse y verso il basso: cambia solo quanto è grande l'area virtuale.

## 9. I passi

Ogni passo compila e passa i test, e alla fine di ognuno si guarda il risultato prima di andare avanti.

1. **Le misure di prova**: `MisuraDelloSchermo`, `SchermoDiProva`, l'argomento `SCHERMO=` in `OpzioniDiAvvio` (con i test in `OpzioniDiAvvioTest`), la regola del §2.2 con i suoi test (la tabella del §2.3 come casi), e la finestra del §3.2 in `MainLibgdx` e in `Main`. Il layout è ancora quello di oggi: si vede solo come cambia la scala. Aggiornati `13-foresta-app-libgdx.md` e `12-foresta-ui-comune.md`. **Fatto** (2026-11-11).
2. **La disposizione del telefono** in `DisposizioneDelloSchermo` (§4.1), con i test sulle aree (nessuna sovrapposizione fuori dalla pila, tutto dentro l'area, il testo che si allarga con lo schermo), e i due front-end che la usano; la mappa nella pila per ora con la sua cornice. **Fatto** (2026-11-11): sul telefono le statistiche non si creano; `AnteprimaSchermate` accetta `SCHERMO=`. Visto su `MINIMO`: nella locazione larga 390 i personaggi del gruppo si accalcano (cinque in 390 pixel); sui telefoni comuni (642 e più) ci stanno.
3. **La mappa senza cornice** (§4.2) e la sua intestazione. **Fatto** (2026-11-11): cornice grande, intestazione «Mappa», finestra di 7×7 centrata (72 pixel ai lati, 32 sopra e sotto); `AnteprimaSchermate` salva anche `gioco_mappa.png`, con la mappa davanti.
4. **Monete, preziosi e punti in cima agli oggetti del gruppo** nell'inventario (§4.3), e niente sprite delle variazioni nel layout del telefono. **Fatto** (2026-11-11): `RiquadroStatistiche` non è servito dividerlo, sul telefono semplicemente non si crea; `AnteprimaSchermate` apre l'inventario con lo scambio vero; test in `StatisticheInTestaAgliOggettiDelGruppoTest`.
5. **Combattimento e sfida** (§4.4).
6. **Intro, outro, classifica e trofei** (§5), con il test dei testi grandi che non escono dal bordo.
7. **Il tutorial** (§4.5): i riquadri portati davanti, le monete nell'inventario.
8. **Android**: la misura vera con l'area utile (§3.3), e la prova sul Realme Pad, che deve restare sul layout del tablet. Il layout del telefono si prova sul tablet fingendo un'altra misura (`adb shell wm size` e `wm density`).
9. **Le schermate a tutto schermo** (§6), una per una, con la cornice dell'inventario tagliata nel mezzo; gli intermezzi (fumetti in proporzione allo sfondo) si possono correggere anche prima, perché è un difetto a sé.

Dopo ogni passo si aggiornano `porting_libGDX.md` §9 e questo documento.

## 10. Da decidere

Già deciso (2026-11-11): la cornice dell'inventario tagliata nel mezzo (§6), i fumetti e i personaggi degli intermezzi su una scena fissa centrata (§6, fatto), «la barra delle icone» nei testi del tutorial (§4.5, fatto), la mappa sempre 7×7 (§4.2), la sfida sopra la pila e centrata (§4.4), Swing con `SCHERMO=` grande quanto l'area virtuale e senza scala (§3.2: serve a vedere che non ci sia niente fuori posto; la leggibilità si prova su un telefono vero).


- **Il minimo di 505**: tenerlo, o abbassarlo dopo la prova se a scala 0,71 le scritte risultano troppo piccole? Le leve sono la locazione meno alta e un passo della pila più corto; costano testo o intestazioni.
- **I profili**: quelli del §2.3 vanno bene, o ne servono altri (pieghevoli, telefoni più vecchi, iPad mini)?

## Osservazioni

- Le misure dei telefoni sono indicative: i produttori cambiano la densità con le impostazioni di visualizzazione («dimensione dello schermo» su Android), quindi lo stesso telefono può cadere su scale diverse. Il codice non deve dipendere da un modello preciso, solo dai dp.
- Sul telefono il vincolo vero è l'altezza (360–430 dp), non la larghezza: i telefoni sono molto più allungati del tablet (rapporto circa 2,2 contro 1,67), e questo dà la larghezza in più al testo.
- Il difetto dei fumetti degli intermezzi (in proporzione allo schermo e non allo sfondo) c'è già oggi su ogni schermo di forma diversa da quella per cui sono stati scritti.

## Da fare / idee aperte

- Tutti i passi del §9.
- Misurare su un telefono vero la leggibilità del font medio a scala 0,71 e i bersagli del tocco (§7).
- Una volta fatto, la tabella del §2.3 con le schermate di prova di ogni profilo (catturate da libGDX desktop) in `risorse_e_documenti_vari/`.
