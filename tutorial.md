# Il tutorial: i suggerimenti per chi gioca per la prima volta

Stato: **piano** (2026-11-07), niente codice ancora. Chi comincia vede insieme i riquadri, la mappa e tutta la barra delle icone, e non sa da dove cominciare: bisogna spiegargli le cose una alla volta, quando servono, senza un manuale da leggere prima.

## 1. Decisioni prese (dall'utente, 2026-11-07)

1. **Forma**: suggerimenti contestuali, **una volta sola**. Ognuno compare la prima volta che si presenta la situazione (il primo combattimento, la prima volta che si apre la mappa, il primo clic su un riquadro...) e poi non torna. Non c'è un percorso guidato obbligato né un manuale.
2. **Le icone restano come oggi**: nessuna icona si nasconde finché non serve; compaiono quando sono possibili, come ora. Il tutorial si limita a spiegarle.
3. **Come appare**: un **fumetto con la coda che punta all'elemento di interesse**. Dentro il fumetto c'è l'icona del **pollice su** per «ho capito» (è la stessa icona di `SI`, `icone/Si.png`: niente icona nuova). Mentre il suggerimento è visibile, l'elemento spiegato ha il **bordo che lampeggia**.
4. **Lunghezza**: un paragrafo, che spieghi bene **come funziona** l'elemento, comprese le cose che si possono fare. Per esempio il riquadro delle missioni: si porta in primo piano, si scorrono le missioni (con il mouse sul desktop, trascinando sul tablet) e una missione si chiude cliccandoci sopra. Quindi il fumetto può avere **più pagine**.
5. **Un modulo nuovo** per il tutorial (piccolo, in basso; la UI lo usa, lui non usa la UI).
6. **Come si spegne**: dal fumetto stesso, con l'icona di annullamento (`ANNULLA`, `icone/Annulla.png`), che spegne il tutorial. **A inizio partita**, insieme alla scelta della lingua, si propone un'icona che **ripristina** il tutorial (lo riaccende e fa rivedere i suggerimenti già visti). L'immagine di quell'icona, per ora, la prepara una versione provvisoria (il pollice con una freccia circolare, dal disegno di `Si.png`); la si sostituisce quando c'è quella vera.
7. **Mentre un suggerimento è aperto il gioco aspetta**, ma l'elemento che lampeggia risponde ai clic: si può portare il riquadro in primo piano, scorrere le missioni, provare quel che il fumetto dice. Il motore resta fermo (UI occupata) finché non si preme il pollice.
8. **Più suggerimenti insieme**: uno dopo l'altro, a ogni pollice, in un ordine fisso; mai due fumetti insieme.
9. **Nelle spiegazioni delle icone** si mette l'icona stessa **a sinistra del testo**, se possibile: chi legge vede subito quale icona si sta spiegando. Vale per le pagine del fumetto che spiegano le icone della barra (e può valere anche per la descrizione dell'aiuto, da decidere).
10. **Mentre si fanno prove, niente salvataggio**: l'infrastruttura per ricordare i suggerimenti visti (il registro, il modello dati, l'interfaccia e la scrittura su file) si prepara, ma **il salvataggio resta spento**, altrimenti bisognerebbe ripristinare il tutorial a ogni prova. Si accende quando il tutorial è finito.
11. **L'icona di ripristino** sta dopo quelle della lingua, a inizio partita, **separata da una spaziatura**, e ripristina esplicitamente il tutorial.

## 2. Che cosa c'è già e si può usare

- La barra delle icone mostra già solo i comandi possibili (`ComandiPossibili`), e l'aiuto (l'icona «?») dà la descrizione di ogni icona (sul tocco con la pressione prolungata). Le icone `Si` (pollice su) e `Annulla` esistono già, con i comandi `SI` e `ANNULLA`.
- Un fumetto con la **coda** esiste: `DescrizioneFumetto` (nel modulo comune) dice com'è fatta la nuvola (rettangolo bianco smussato, righe di testo, il triangolo della punta verso un punto) e `SchermoDelGioco.fumetto(testo, larghezzaMassima, x, y, puntaVersoX, puntaVersoY)` lo mette a schermo; Swing e libGDX lo disegnano (`SpriteFumetto`, `SpriteFumettoGdx`, `NuvolaDiFumettoGdx`). Ma quello di oggi è un **fumetto che sale e svanisce dopo pochi secondi**, di una sola pagina, senza icone dentro: per il tutorial serve un fumetto che **resta finché non si preme il pollice**, con più pagine e le icone cliccabili. Le scene degli intermezzi hanno già fumetti con battute una dopo l'altra (`ScenaDellIntermezzo.Fumetto`, `InternoFumettoSuccessivo`), da cui prendere l'idea del cambio di pagina.
- Gli eventi sul bus che segnalano le situazioni: `InternoRichiestaAperturaFinestraCombattimento` (si apre il combattimento), `RichiestaAperturaInventarioGruppo`, `RichiestaAperturaTattiche`, `NotificaArtefattoTrovato`, le notifiche di variazione delle scorte, `InternoMostraSchermataGioco`... Molti inneschi non servono nuovo codice: basta ascoltarli. Altri (la prima apertura della mappa, il primo clic su un riquadro) vanno verificati uno per uno.
- Il modo di ricordare qualcosa **da una partita all'altra** esiste: il bestiario (`RegistroBestiario`, `BestiarioMD`, l'interfaccia `GestoreBestiario` implementata su file dal motore) e i trofei. Lo stato dei suggerimenti vissuti e l'interruttore acceso/spento si tengono allo stesso modo, nella cartella dell'utente (`~/.foresta`, su Android la cartella privata dell'app), e non nel salvataggio di una partita.
- I testi dei nomi e delle descrizioni delle enum stanno in un file `.properties` accanto all'enum, uno per lingua (`tipo_trofeo_it.properties`, chiavi `COSTANTE.NOME`, `COSTANTE.DESCRIZIONE`): un'enum dei suggerimenti con le sue chiavi si traduce senza codice.

## 3. Disegno (confermato, 2026-11-07)

Il tutorial è **un elenco di suggerimenti**, e **la UI decide quando mostrarli** guardando quello che ha davanti. Niente eventi "per il tutorial": dove la UI già sa una cosa (le icone nella barra, l'arrivo in una locazione, l'apertura del combattimento) la usa; un evento nuovo si aggiunge solo dove la UI non può dedurre la situazione (vedi sotto).

- **Un suggerimento ha tre cose**: *quando scatta*, *a che cosa punta* e *il testo* (le pagine del fumetto).
- **Quando scatta** è sempre qualcosa che la UI vede:
  - **le icone presenti nella barra**: se ci sono le quattro icone di movimento (NORD, EST, SUD, OVEST) scatta, una volta, la spiegazione di come muoversi; se c'è l'icona dell'inventario, quella dell'inventario; se compaiono quelle del combattimento, quelle del combattimento; e così per le tattiche, le pozioni, l'accampamento, il salvataggio, le botteghe;
  - **la prima volta che si arriva in una locazione**: scattano, uno dopo l'altro, i suggerimenti dei riquadri (il gruppo, le missioni, la mappa...);
  - **un evento che la UI già riceve** per le altre situazioni (per esempio `InternoRichiestaAperturaFinestraCombattimento` per il primo combattimento).
- **Come funziona a ogni aggiornamento**: la UI chiede al registro «c'è qualcosa da spiegare adesso?» con `RegistroSuggerimenti.situazione(Set<Situazione>)`. `Situazione` è un'enum del modulo (`ICONE_MOVIMENTO_PRESENTI`, `COMBATTIMENTO_APERTO`, `OGGETTO_TROVATO`, `INGRESSO_NEGOZIO`, `NOTTE`...); `ForestaUI` (modulo comune, dove già traduce gli eventi del bus) la mette insieme da ciò che ha davanti (le icone della barra, la schermata, l'evento appena arrivato), così libGDX e Swing non ripetono la traduzione e disegnano soltanto. Il registro risponde con il primo suggerimento non ancora visto che corrisponde, oppure con niente. Se c'è, la UI disegna il fumetto con la coda che punta al bersaglio e fa lampeggiare il bordo. Alla pressione del pollice la UI dice al registro «visto», e il fumetto passa al successivo.
- **Il registro è solo una memoria** (`RegistroSuggerimenti`, sul modello di `RegistroBestiario`): l'elenco dei suggerimenti, quali sono già stati visti, se il tutorial è acceso o spento. **Non sa niente della UI e non manda eventi.** Non ci sono eventi del tipo «icona X pubblicata»: l'insieme delle icone è già stato della UI (`ComandiPossibili`) e gli altri fatti arrivano già sul bus (`InternoRichiestaAperturaFinestraCombattimento`, `NotificaArtefattoTrovato`, `InternoPreparazioneLocazione`, `InternoFineLocazione`, le fasi di `FaseDiGioco`, i cambi di schermata). La UI usa il modulo del tutorial e il modulo non usa la UI: una direzione sola, nessun giro.
- **Il modulo nuovo è piccolo e sta in basso**: contiene l'enum dei suggerimenti (`TipoSuggerimento`, con i suoi testi per lingua), i bersagli (`BersaglioDelSuggerimento`: il riquadro del gruppo, quello delle missioni, le icone di movimento...), il registro e l'interfaccia per salvare cosa si è visto. È `03-foresta-tutorial` (2026-11-07): dipende da cose basse (i comandi, i testi, la serializzazione) e non cambia il livello di nessun altro modulo. Contiene `TipoSuggerimento` (15 suggerimenti con i loro testi), `Situazione`, `BersaglioDelSuggerimento`, `SuggerimentiMD`, `GestoreSuggerimenti` e `RegistroSuggerimenti`; manca la UI.
- **Eventi nuovi solo dove la UI non può dedurre**: da verificare uno per uno, probabilmente la prima notte, il primo ferito dopo uno scontro, il primo mostro corrompibile o amichevole. Sono notifiche piccole che il motore pubblica comunque, senza sapere del tutorial.
- **Il motore fa tre cose**: scrive su file cosa si è visto (come per il bestiario, nella cartella dell'utente, non nel salvataggio); sta fermo mentre il fumetto è aperto (la UI dice già al motore «sono occupata»); **legge** il registro per pilotare i dadi (§4 bis: sa quali suggerimenti sono già stati visti). Non gli manda nulla: il modulo è basso, quindi il motore può dipendere da lui senza giri.
- **Il fumetto del tutorial** (modulo comune per il modello, disegno in Swing e libGDX): coda che punta al bersaglio, le pagine, il pollice (`SI`: avanti sulle pagine intermedie, chiusura sull'ultima), l'annulla (`ANNULLA`: spegne il tutorial). Il bordo che lampeggia sul bersaglio: la UI sa già dove stanno i riquadri (`DisposizioneDelloSchermo`) e le icone (`BarraIcone.rettangoloDisegnato`).
- **Testi diversi sul tocco**: la spiegazione dei gesti cambia (trascinare invece del mouse, tenere premuto per la descrizione): la UI sceglie la variante `.TOCCO` quando c'è il dito (la stessa condizione che già accende i gesti del dito in libGDX).
- **Un suggerimento alla volta**: se la stessa situazione ne fa scattare più d'uno, vanno in ordine fisso, ognuno dopo il pollice del precedente. Mai durante un intermezzo né nel mezzo di un'azione che chiede attenzione.

## 4. Quali suggerimenti (ordinati per bisogno)

L'ordine va da quello che serve per cominciare a quello che serve per andare avanti: ogni suggerimento scatta quando il giocatore ne ha un motivo, non quando l'elemento compare. Un suggerimento per concetto, al massimo tre pagine (se ne servono di più, il concetto si spezza in due).

| # | Quando | Che cosa spiega |
| :---: | :--- | :--- |
| 1 | Prima schermata di gioco | Solo due cose: che cliccando (o toccando) un riquadro lo si porta in primo piano, e come si spegne il tutorial (l'annulla spegne tutto). Gli altri riquadri (gruppo, incantesimi e pozioni, missioni) aspettano il momento in cui servono |
| 2 | Prima volta che si vede la barra delle icone | Che le icone cambiano con la situazione e che l'icona «?» dà la descrizione di ognuna (sul tocco: tenere premuto) |
| 3 | Primo spostamento, con la prima apertura della mappa a tutto schermo | Direzioni e numero di passi; a cosa serve la mappa (dove si è, cosa si è visitato, come si trascina), il notiziario sotto, il joystick sul tocco |
| 4 | Primo combattimento (solo attacco semplice) | Il riquadro del combattimento: chi agisce, chi è bersaglio, le barre di salute; che i personaggi seguono una tattica, senza aprire ancora le tattiche |
| 5 | Dopo il primo scontro: primo oggetto trovato | Come si equipaggia dall'inventario e come si passa a un compagno |
| 6 | Primo ferito dopo uno scontro, con una pozione disponibile | Quando serve berla (le piccole solo se qualcuno è ferito; le grandi alzano il massimo) |
| 7 | Secondo combattimento: compaiono più comandi (fuga, singolo attacco) | Cosa fa ogni comando in combattimento |
| 8 | Il riquadro delle missioni (la prima volta che si ha una missione) | Che lo si porta in primo piano, che si scorrono le missioni (mouse; trascinando sul tablet) e che si «chiude» una missione cliccandoci sopra |
| 9 | Dopo almeno due scontri visti con la tattica che agisce da sola: compare «Tattiche» | Che si può cambiare come ogni personaggio sceglie cosa fare |
| 10 | Primo mostro corrompibile (dado truccato, §4 bis) | La corruzione |
| 11 | Primo mostro amichevole (dado truccato, §4 bis) | L'amicizia |
| 12 | Prima volta che si può lanciare un incantesimo | Il grimorio, in un solo suggerimento |
| 13 | Prima città o bottega | Cosa si può comprare, vendere, incantare |
| 14 | Prima notte | L'accampamento, e perché serve qualcuno di guardia |
| 15 | Prima volta sul tocco | Gesti: tocco, doppio tocco, pressione prolungata, trascinamento (può anche scattare in anticipo, appena c'è il dito) |

Fuori dal tutorial: trofei, bestiario, intermezzi, il lessico di incantesimi e canti. Sono scoperte, non prerequisiti.

## 4 bis. Pilotare la partita durante il tutorial (idea, 2026-11-07)

A tutorial attivo il gioco si **pilota** perché il giocatore incontri le cose una alla volta. Decisione dell'utente: **il mostro si sceglie truccando il dado, punto** (non con un filtro sui candidati). Quello che ci siamo detti:

- **Cosa guida la scelta è ciò che il giocatore ha già visto**, non il numero del turno. Finché non ha visto i suggerimenti di base (l'attacco singolo e il combattimento) il dado è truccato in modo che **non escano corrompibili né amichevoli** (`Personaggio.isCorrompibile()`, `isAmichevole()`, proprietà della classe), così le icone da spiegare sono poche. Dopo, il dado è truccato per far uscire il mostro adatto al passo successivo: un corrompibile (per esempio il goblin), che fa scattare il suggerimento sulla corruzione quando lo si incontra; poi un amichevole, che fa scattare quello sull'amicizia.
- **Attenzione al giro vizioso:** il trucco non può dipendere dal suggerimento della cosa che sta per comparire (i corrompibili non possono aspettare il suggerimento sulla corruzione, che appare solo quando se ne incontra uno). Conta solo il suggerimento **precedente**.
- **Prima di fissare la sequenza** va guardato quali mostri compaiono nei primi luoghi e a che livello, per sapere quali esiti del dado servono.
- **Trucchi anche sulle scelte:** a tutorial attivo si può truccare il gioco a fondo (anche «in maniera indecente», parole dell'utente) in modo che le scelte del giocatore siano più o meno obbligate: per esempio, nel primo scontro può fare solo un attacco semplice; nel secondo gli arriva l'occasione di corrompere.
- **Interruttore:** tutto questo vale solo con il tutorial attivo e il suggerimento non ancora visto. Se i suggerimenti sono spenti o azzerati, i dadi tornano identici a oggi.

### Il primo scontro (decisioni dell'utente, 2026-11-07)

- **Il truccatore del tutorial si installa sempre**, anche senza la finestra di prova. Oggi `Dado.impostaTruccatore` ne accetta uno solo e `Main` installa `TrucchiDiSviluppo` solo in modalità di prova: serve un `PilotaggioDelTutorial` (nel modulo del tutorial, implementa `TruccatoreDiDadi`) che interpella prima il tutorial e poi, se c'è, la finestra di prova. Con il tutorial spento non decide mai niente.
- **Il giocatore non deve poter perdere il primo scontro.** Più avanti, «via via», perdere uno scontro di prova va bene (e fa parte dell'imparare).
- **Il mostro del primo scontro** è non amichevole e non corrompibile, e non può essere un mostro che fa paura (viverna, chimera drago, minotauro...). L'arpia è la prima scelta; vedi sotto gli altri candidati.
- **L'arpia non lancia incantesimi in quello scontro:** si trucca il dado che sceglie l'azione del mostro (oggi il suo repertorio è `ARIA`), così il primo scontro è davvero solo «attacco semplice».
- **Servono contesti di lancio nuovi per il combattimento.** I tiri di `CalcolatoreCombattimento` (colpire, critico, stati) oggi sono `Dado.tira(100)` senza contesto e il truccatore non li vede: servono contesti come «il mostro colpisce», «il giocatore colpisce», «il mostro tira un incantesimo». Regola del primo scontro: il mostro manca (o fa pochi danni) e il giocatore colpisce quasi sempre.
- **La dotazione iniziale non ha pozioni né incantesimi**, **a prescindere dal tutorial** (decisione dell'utente): così l'elenco delle icone dei primi turni resta corto. Li regala l'eremita a fine prima locazione.
- **L'eremita della fine della prima locazione** (`IntermezzoFinePrimaLocazione`, che scatta sempre a `LOCAZIONE_COMPLETATA` della prima locazione, una volta sola) consegna la dotazione e la annuncia con **un fumetto in più**. La consegna è un cambio di inventario, quindi la fa il motore (`07-foresta-intermezzi` non conosce gli oggetti), e **sempre**, tutorial o no.

### Quali mostri nei primi turni (verificato nel codice)

- **Il primo turno trova sempre un mostro** (`Statistiche.getTurniGiocati() == 0` in `LocazioneBase.crea`) e l'eremita è escluso.
- Finché il capo è di livello 3 o meno (`CostantiMotore.INIZIO_MORBIDO_FINO_AL_LIVELLO`) `MOSTRI_ESCLUSI_A_INIZIO_PARTITA` toglie già centauro, chimera drago, gigante, minotauro, titano e troll, e il numero dei mostri non supera quello dei personaggi del gruppo. Restano, nel bosco e nella radura: arpia, chimera, eremita, folletto, goblin, hobgoblin, scheletro, viverna.
- **Corrompibili:** goblin, hobgoblin, folletto. **Amichevoli:** folletto (e l'eremita). Quindi **non amichevoli e non corrompibili: arpia, chimera, scheletro, viverna.**
- **Candidati al primo scontro:** arpia e scheletro. Chimera e viverna restano fuori (fanno paura e non sono esclusi dall'inizio morbido: nel tutorial vanno esclusi con il dado, perché a caso al primo turno possono ancora uscire).
- **Candidati alla corruzione:** goblin (poi hobgoblin). **All'amicizia:** il folletto, che è anche corrompibile: da verificare quale scena preferiamo (c'è lo spirito, amichevole ma non corrompibile, ma sta nelle rovine).

## 4 ter. La sequenza dei primi cinque turni (decisa dall'utente, 2026-11-07)

Un mostro per turno, sempre **uno solo** (`NUMERO_MOSTRI` truccato a 1), di livello 1. Ogni passo scatta dopo il suggerimento del passo precedente, non dal numero del turno (§4 bis): se il tutorial si spegne, il resto torna al caso.

| Turno | Mostro | Perché lui | Che cosa si spiega |
| :---: | :--- | :--- | :--- |
| 1 | **arpia** | Non amichevole, non corrompibile, senza resistenze alle armi normali: il primo scontro è pulito. Il giocatore non ha né pozioni né incantesimi | I riquadri (clic per portarli in primo piano), la barra delle icone, il movimento e la mappa, il combattimento. **Niente fuga né passare inosservato** (le icone non compaiono), il giocatore **non può perdere**; l'arpia non lancia incantesimi (`ARIA`) |
| fine 1 | **eremita** | `IntermezzoFinePrimaLocazione` scatta sempre a fine locazione | Regala pozioni e incantesimi (la dotazione di oggi, vedi sotto) con un fumetto in più; da qui le opzioni crescono. Grimorio e pozioni |
| 2 | **chimera drago** | Fa paura: è il mostro da cui conviene non farsi vedere | **Passare inosservato riesce sempre**; la **fuga** funziona ma il tutorial la sconsiglia. Fuga e passare inosservato **chiudono la locazione senza prendere l'oggetto** (da verificare nel codice). Se il giocatore decide di combattere e muore, è una sua scelta: la chimera drago **non è protetta** |
| 3 | **scheletro** | Non amichevole, non corrompibile, ma ha **resistenze**: ½ al perforante, ¾ al tagliente, 1,5 al contundente | Gli incantesimi nel combattimento e le debolezze e resistenze dei mostri; l'oggetto trovato (truccato) e l'inventario. Protetto: non lancia il Gelo |
| 4 | **goblin** | Corrompibile | La corruzione (le monete ci sono: 150 nella dotazione iniziale); le tattiche, dopo due scontri visti con la tattica che agisce da sola; il riquadro delle missioni, se ne compare una |
| 5 | **folletto** | Corrompibile e amichevole | L'amicizia. Per quel turno **non scatta la sfida a carta, forbici e sasso** (si spegne dove `LocazioneBase` la decide, finché l'amicizia non è stata spiegata) |

Dopo il quinto turno il gioco torna al caso, e i suggerimenti che restano (pozioni a fine scontro con feriti, botteghe, accampamento...) scattano quando capitano.

Punti da realizzare, in ordine:
1. **Fatto (2026-11-07), a metà.** `PilotaggioDelTutorial` (nel motore, pacchetto `tutorial`) è sempre installato da `AvvioDelMotore` e i trucchi della finestra di prova hanno la precedenza. Fa già: il mostro del turno (`TIPO_MOSTRO`: arpia, chimera drago, scheletro, goblin, folletto) e uno solo (`NUMERO_MOSTRI` = 1); l'incontro c'è sempre (`LocazioneBase.crea` salta il 10% di «niente mostri» e aggiunge il mostro agli ammessi se la locazione o l'inizio morbido lo escludono). Il passo è il turno della partita (`Statistiche.getTurniGiocati()`): è provvisorio, andrà sui suggerimenti visti (§4 bis). **Manca** tutto il resto della tabella: i contesti nuovi (punto 2).
2. **Fatto (2026-11-07)**: contesti di lancio `COLPO_DEI_MOSTRI` (arpia e scheletro non colpiscono mai: tiro di 100, la probabilità arriva a 95), `TATTICA_DEI_MOSTRI` (niente incantesimi: il predicato `PROBABILITA` dei mostri vale 100, quindi nessuna voce sotto il 100% scatta), `PASSAGGIO_INOSSERVATO` (riesce sempre davanti alla chimera drago), `CORRUZIONE` (riesce col goblin), `AMICIZIA` e `SFIDA_CARTA_FORBICI_SASSO` (il folletto diventa amico e non sfida). Li decide `PilotaggioDelTutorial` per turno; `PilotaggioDelTutorialTest`. L'**oggetto trovato** (`TIPO_OGGETTO_TROVATO`, contesto di scelta) allo scheletro è un'arma che il capo può impugnare: la mazza (contro lo scheletro, le ossa si spezzano a bastonate), un bastone magico per mago e druido, una spada per il bardo; `LocazioneBase.crea` lo aggiunge fra gli oggetti possibili se la locazione non lo ha (una radura non ne lascia). **Non verificato in partita**: che l'arpia non lanci davvero (si è letto `TatticheBase`: le voci magiche del bruto hanno una probabilità sotto il 100).
3. La dotazione iniziale senza pozioni né incantesimi (con le monete) e la consegna dell'eremita, sempre.
4. Fuga e passare inosservato (verificato nel codice, 2026-11-07): **chiudono la locazione senza l'oggetto e l'eremita scatta comunque** (vedi sotto); restano da fare: togliere l'icona della fuga e quella di passare inosservato al primo turno, far riuscire sempre il passare inosservato al secondo (oggi il tiro non ha contesto di lancio).

### Fuga e passare inosservato (verificato nel codice, 2026-11-07)

- **Passare inosservato** (`LocazioneBase.passaInosservati`): se riesce, `setCompleta(false)` e `setOggetto(null)` (niente bottino né esperienza), poi `FINE_LOCAZIONE`. Si prova **una volta sola** per locazione e costa un'ora. Il tiro è `Dado.tira(100) <= probabilità` **senza contesto**: per farlo riuscire sempre serve un contesto nuovo (`PASSAGGIO_INOSSERVATO`). Se fallisce, gli avversari attaccano per primi. L'icona compare solo se `isPassaggioPossibile()`: nel tutorial basta una condizione in più per spegnerla al primo turno.
- **Fuga** (`CONFERMA_FUGA`): `setCompleta(false)`, `setOggetto(null)`, `gruppo.fugge()` e `Cassa.perdeNellaFuga()`, poi `FINE_LOCAZIONE`. **Costa**: ogni personaggio vivo subisce ferite e il gruppo perde da 1 a 20% di incantesimi, pozioni, monete e preziosi (arrotondato per difetto). È un buon motivo concreto per il «sconsigliata» del secondo turno, ma anche un rischio per le 150 monete della corruzione del quarto. **Decisione dell'utente:** se il giocatore fugge nel tutorial, le monete devono bastare per la corruzione. La perdita **è ora un tiro di dado** (fatto, 2026-11-07): `Dado.tira(FUGA_PERDITA_DENARO, 1, 20)` per monete e preziosi e `Dado.tira(FUGA_PERDITA_SCORTE, 1, 20)` per incantesimi e pozioni. Il tutorial trucca `FUGA_PERDITA_DENARO` al minimo (1%: con 150 monete se ne perde una, che non cambia la corruzione) finché la corruzione non è spiegata; ferite e perdita di pozioni e incantesimi restano, perché sono il motivo per cui la fuga si sconsiglia. L'icona `FUGA` è aggiunta **sempre** in `impostaComandiPossibili()`: per spegnerla al primo turno serve un hook nuovo.
- **L'eremita scatta comunque**: `Automa.eseguiFineLocazione`, anche per una locazione lasciata a metà, passa da `concludiFineLocazione()`, che pubblica `InternoFineLocazione` e avvia gli intermezzi di `LOCAZIONE_COMPLETATA`. `IntermezzoFinePrimaLocazione.deveScattare` guarda solo il momento e non è ripetibile (il registro lo segna come già scattato), quindi scatta alla fine della prima locazione, qualunque ne sia l'esito.


### La dotazione iniziale (trovata nel codice, 2026-11-07)

- **Incantesimi e pozioni**: `GruppoGiocatore.reimposta()` (`05-foresta-personaggi`) dà 3 incantesimi di ARIA, 3 di ACQUA, 3 di TERRA e `CostantiPersonaggi.POZIONI_SALUTE_INIZIALI` (2) pozioni di salute; le altre pozioni partono da 0.
- **Monete e preziosi**: `Cassa` (`08-foresta-economia`) con `CostantiEconomia.MONETE_INIZIALI` (150) e `PREZIOSI_INIZIALI` (5). Restano come sono.
- **Fatto (2026-11-07)**: `GruppoGiocatore.reimposta()` parte da zero incantesimi e zero pozioni; la consegna la fa il motore all'eremita di fine prima locazione (`DotazioneDellEremita`, chiamata dall'`Automa` quando scatta `INTERMEZZO_FINE_PRIMA_LOCAZIONE`), con 3 incantesimi per ognuno dei tre elementi e 2 pozioni di salute (`CostantiMotore.DOTAZIONE_EREMITA_*`; `POZIONI_SALUTE_INIZIALI` non esiste più). L'eremita ora dice due battute: il dono e l'augurio (`IntermezzoFinePrimaLocazione.1` e `.2`, in italiano e inglese). Sempre, tutorial acceso o spento. Lo prova `IntermezzoLocazioneCompletataTest`. **Resta**: il fumetto non elenca cosa dà (generico, senza numeri).

## 4 quater. La scelta della classe (decisa dall'utente, 2026-11-07)

Chi sceglie la classe la prima volta non sa quale prendere, e la scelta è la più importante della partita. Non è un suggerimento «una volta sola»: l'aiuto sta **dentro la schermata di scelta** e, dove serve, dipende dal tutorial.

- **Un pannello fisso con la scheda della classe** (non un fumetto che si apre al tocco): per la classe sotto il cursore o selezionata mostra la descrizione (`DESCRIZIONE` in `tipo_personaggio_it.properties`, che esiste già), il ruolo in una riga («regge i colpi», «colpisce da lontano»...) e tre o quattro barre (salute, magia, attacco, difesa), da `personaggi_giocabili.md` §2. È sempre visibile, tutorial acceso o spento: chi ha giocato dieci volte vuole comunque confrontare.
- **L'etichetta «consigliato per cominciare»** compare sulle classi più semplici **solo se il tutorial è attivo**: se è spento, il giocatore si considera già esperto e l'etichetta non serve. Quali classi (candidati: Guerriero e Ladro) si decide con le misure del §5 di `personaggi_giocabili.md`: quelle che reggono il primo scontro senza incantesimi.
- **Una frase sola in fumetto, la prima volta** (con il pollice, come gli altri suggerimenti): «Questa scelta decide come giocherai; tocca una classe per leggerne la scheda». Prima di ogni turno, quindi `RegistroSuggerimenti` deve valere già lì (è acceso, non dipende dal turno).
- **Le classi forti negli incantesimi** (Mago, Maga) nei primi turni del tutorial non possono usarli (arrivano dall'eremita, §4 ter): la scheda lo dice, perché la scelta non sembri sbagliata.
- **Il testo** della scheda (ruoli, etichette, la frase) va scritto una volta e in tutte le lingue (italiano, inglese, inglese britannico), come per i suggerimenti. Il disegno del pannello è della UI (Swing e libGDX).

## 5. Da decidere

- ~~**La conferma del disegno** del §3~~ Confermato (2026-11-07), con eventi espliciti solo dove la UI non può dedurre la situazione.
- ~~**Quanto devono essere lunghi, in pagine**~~ Deciso (2026-11-07): una pagina per ogni cosa che si può fare, al massimo tre pagine per suggerimento; oltre, il concetto si spezza in due suggerimenti.

## Osservazioni

- Lo stesso testo serve a tutte le UI, quindi vanno scritti una volta sola e in tutte le lingue (italiano, inglese, inglese britannico).
- L'icona di annullamento dentro il fumetto spegne **tutto** il tutorial, non solo il suggerimento in corso: va detto chiaramente nel primo suggerimento, e il ripristino sta solo all'inizio di una partita nuova, quindi non c'è un modo di riaccenderlo a metà.

## Da fare / idee aperte

- **Fatto (2026-11-07)**: il modulo `03-foresta-tutorial` con i 15 suggerimenti e i loro testi (italiano e inglese), il registro dei visti e il modello dati (senza salvataggio). **Fatto anche (2026-11-07)**: il modello nel modulo comune (`ui.tutorial`: `SituazioniDellaUI`, `TutorDelGioco`, `FumettoDelTutorial`, collegati a `ForestaUI` e `SchermoDelGioco`), non ancora abilitato. **Fatto anche in Swing (2026-11-07)**: `DisplayableCanvasTutorial` (la nuvola con il testo della pagina, il pollice `Si.png` e l'annulla `Annulla.png` in basso, la coda al centro del bersaglio, il bordo giallo che lampeggia), il clic sui due pulsanti nel mouse del canvas, `setAbilitato(true)` nel canvas: in Swing i suggerimenti si aprono. La posizione è in `ui-comune` (`DisposizioneDelFumetto`). **Resta**: il disegno in libGDX (fumetto con coda e pagine, pollice, annulla, bordo che lampeggia, `setAbilitato(true)`, il dito), l'icona di ripristino a inizio partita, il passo del pilota sui suggerimenti visti, il gestore su file, e rileggere i testi (i gesti di `GESTI_SUL_TOCCO` sono da confrontare con quelli veri).
- Pilotare la partita (§4 bis): decidere la sequenza dei trucchi sui dadi (i mostri dei primi luoghi sono già guardati); aggiungere i contesti di lancio del combattimento; il `PilotaggioDelTutorial` sempre installato.
- Spostare la dotazione iniziale (pozioni e incantesimi) all'eremita di fine prima locazione: trovare dove la dà oggi il motore e aggiungere il fumetto in più dell'eremita.
- Verificare per ognuna delle righe del §4 quale evento già esiste e quale va aggiunto.
- La scelta della classe (§4 quater): le barre e i ruoli per ogni classe giocabile, quali classi sono «consigliate», il pannello fisso in Swing e libGDX; l'etichetta dipende da `RegistroSuggerimenti.isAttivo()`.
- Scrivere i testi (prima in italiano, poi nelle altre lingue).
- Calcolare il livello esatto del modulo nuovo dalle sue dipendenze, e scegliere il nome (`NN-foresta-<nome>`).
