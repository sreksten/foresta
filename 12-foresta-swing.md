# 12-foresta-swing

L'**interfaccia grafica in Swing**: la finestra, il canvas che disegna la schermata corrente, i riquadri della schermata di gioco, le schermate a tutto schermo, la barra delle icone, gli sprite animati, il testo in font bitmap, le immagini, e la finestra per chi sviluppa. Contiene anche tutte le risorse grafiche del gioco (le immagini sotto `img/`).

È l'unico modulo che usa `java.awt`, `javax.swing`, `javax.imageio` e `javax.sound` (lo controlla `DipendenzeDalToolkitGraficoTest`), e **non conosce il motore né il dominio**: legge lo stato della partita da `VistaPartita` in sola lettura, e verso il motore manda soltanto eventi. È fatta così perché la stessa partita possa avere un altro front-end (Android, iOS) senza toccare le regole: la UI è una *vista* sostituibile.

## Dipendenze

**Dichiarate nel pom:** `11-foresta-motore-api`, `08-foresta-tattiche`.

**Lo dichiarano:** `13-foresta-app-swing`.

Le dipendenze sono transitive: un modulo vede anche quello che vedono i moduli da cui dipende, quindi il compilatore garantisce la separazione fra i moduli che non si raggiungono.

Dipende da `11-foresta-motore-api` (e per transitività dal resto dei contratti: viste dei sottoprogetti, eventi, tipi) e da `08-foresta-tattiche`, perché la schermata delle tattiche costruisce e mostra i dati di `TatticaMD`. Non vede mai `12-foresta-motore`: lo assembla `13-foresta-app-swing`.

## Funzionamento

### La radice: `ForestaUI` e `DisplayableCanvas`

`ForestaUI` è la classe radice: crea la finestra (sul thread di Swing), il canvas e il prompt, mostra il logo iniziale mentre carica le risorse e poi lascia il posto alla schermata di gioco. Si iscrive subito a una quarantina di tipi di evento del bus e **traduce ciascuno in una chiamata sul canvas**; quando ha finito pubblica `InternoInterfacciaUtentePronta`, e da lì l'`Automa` parte (`Main` lo aspetta). Gli errori interni li dice al giocatore `SegnalatoreDegliErrori`, una volta sola se si ripetono.

`DisplayableCanvas` (circa 1800 righe) è il canvas principale: sa quale schermata è quella corrente (`StatoDisplayableCanvas`, per le schermate a tutto schermo) e la disegna, e fa da facciata per tutto ciò che la UI deve mostrare. Nella schermata di gioco disegna i riquadri e la barra delle icone in **una pila di finestre** (`Finestra`, `TipoFinestra`) che si possono portare in primo piano, e sopra a tutto gli sprite; le animazioni girano a `Temporizzatore.FRAME_PER_SECONDO` (60) fotogrammi al secondo, il disegno vero sul thread di Swing. `EsecutoreSwing` è l'`Esecutore` di `ThreadPrincipale` con l'Event Dispatch Thread: tutta la logica di gioco gira lì. `Prompt` è il campo in cui il giocatore scrive un testo (il nome del personaggio, il suo nome in classifica).

### I sottopacchetti

| Sottopacchetto | Che cosa contiene |
| :--- | :--- |
| `riquadri` | i riquadri della schermata di gioco: locazione, gruppo, combattimento, mappa (minimappa), missioni, testo, statistiche, incantesimi e pozioni, e la sfida a carta, forbici e sasso. Ciascuno disegna la sua parte e ascolta gli eventi che la riguardano |
| `schermate` | le schermate a tutto schermo: introduzione e fine partita (`DisplayableCanvasIntroOutro`: slot di salvataggio, statistiche, punteggi), intermezzi, mappa a tutto schermo con il notiziario, inventario, bestiario, trofei, negozi (armaiolo e venditore di pergamene, alchimista) e incantatore. Quelle che spostano oggetti fra due colonne hanno una base comune (`DisplayableCanvasScambiatore` e `…ScambiatoreArtefatti`) |
| `barra` | la barra delle icone con i comandi possibili in quel momento: la predefinita che si ingrandisce sotto il cursore come il Dock di macOS (`DisplayableCanvasBarraIconeDock`) e quella classica fissa (`DisplayableCanvasBarraIcone`, con l'argomento `BARRACLASSICA`), più i cartigli con la descrizione dei comandi |
| `sprite` | elementi che compaiono per un po' sopra la schermata (numeri che salgono, fumetti, annunci globali, effetti di stato, la rivelazione di un artefatto) con la loro animazione e dissolvenza; `SpriteBase` ne è la base. Gli eventi `InternoCreazioneSprite…` (in questo modulo) decidono come rappresentare un fatto di gioco |
| `testo` | il testo in font bitmap nello stile del gioco originale: i font Doomdark in due misure, i rettangoli di testo con lo storico scorrevole, il testo grande della storia, il testo che sale sopra un personaggio quando è debole o resiste a un danno; `TestiDellaUI` legge da `ui_<lingua>.properties` (in `src/main/resources/…/testi/`) tutte le scritte della UI che non vengono da un'enum: «Mancato», «Immune a {0}», gli annunci, le domande di creazione del personaggio, la riga dei dati di un artefatto |
| `immagini` | `ImageCache` carica le immagini una volta sola dalle risorse (personaggi, oggetti, locazioni, icone, fondali) e tiene una cache dinamica di immagini di testo; `DimensioniRisorsa` e `VersiDeiPersonaggi` dicono dimensioni e verso delle immagini |
| `tattiche` | la schermata con cui il giocatore modifica la tattica di un personaggio, e i menu a discesa; la bozza su cui lavora (`BozzaTattica`) e i testi che descrivono la tattica (`TestoTattica`) stanno in `08-foresta-tattiche`, pacchetto `tattiche.modifica` |
| `componenti` | pezzi riusabili: l'albero scorrevole di nodi e lo scorrimento verticale di un elenco |
| `sfx` | il logo 3AM che si traccia con le sue scie all'inizio e le nuvole che scorrono sopra la mappa |
| `sviluppo` | `FinestraDiProva`, la finestra per chi sviluppa, che si apre solo in modalità di prova: attributi dei personaggi, missioni da forzare e trucchi sui dadi |

I tipi che sono propri della UI stanno nel pacchetto `tipi` di questo modulo: `TipoFinestra` (i riquadri, nell'ordine in cui si impilano), `TipoIcona` (le icone della barra, ognuna lega un comando a un'immagine e a uno sfondo), `Orientamento` (come si dispone la finestra), `MappaPersonaggioImmagine` e `MappaBottinoImmagine` (le immagini di ogni classe di personaggio e di ogni oggetto delle locazioni). Le ultime due stanno qui e non negli enum del dominio **perché il motore non deve caricare immagini**: così si può usare senza schermo, per esempio nei test headless.

### Le immagini e lo zoom

Le immagini del gioco sono a **metà risoluzione** nei file e si ingrandiscono all'avvio (`LivelloDiZoom`, a 2,0 di default, argomento `ZOOM=` di `Main`); `ImageCache` e `DimensioniRisorsa` lo leggono. Le risorse stanno in `img/` (`fondi`, `icone`, `locazioni`, `personaggi`, `mappa`, `alfabeto`, `oggetti`).

### Il rapporto col motore

La UI non importa classi del dominio: riceve **eventi** (notifiche, richieste, interni) e **viste** (`VistaPartita`, `VistaGruppoGiocatore`, `VistaPersonaggio`, `VistaMappa`, `VistaScambio`, `VistaMissione`, `VistaCassa`…), e risponde con **comandi** (`ComandoDiGioco`, `ComandoScambioArtefatto`, `ComandoImpostaTattica`…). Le operazioni delicate seguono il pattern richiesta/verdetto: la UI manda l'intenzione, il motore verifica e risponde con approvazione o rifiuto.

## Osservazioni

- Le scritte nuove della UI non si scrivono nel codice: una chiave in `ui_it.properties` e `TestiDellaUI.get(chiave, valori…)`; per tradurre basta un `ui_<lingua>.properties` accanto. Restano nel codice i nomi interni degli sprite (per esempio «Salute modificata», solo per il log) e i messaggi d'errore degli sviluppatori. Le frasi di narrazione del motore («L'incantesimo non va a segno su…») sono ancora in italiano nel codice dei moduli di dominio.
- Ci sono alcune classi molto grandi e quasi tutte specializzate: `DisplayableCanvas` (1800 righe), `DisplayableCanvasTattiche` (1200), i due font Doomdark (tabelle di glifi) e `TracciatoreLogo`. Che la UI non dipenda dal motore lo garantisce il compilatore (il modulo non lo dichiara); che nessun altro modulo usi il toolkit grafico lo controlla un test in `13-foresta-app-swing`.
- Per aggiungere un'animazione di intermezzo: una costante in `Animazione` (modulo intermezzi), una classe che implementa `AnimazioneImmagine`, una riga in `AnimazioniIntermezzo`.
- La panoramica grafica è in [`motore_grafico.md`](motore_grafico.md); la finestra di prova in [`strumenti_di_prova.md`](strumenti_di_prova.md).
