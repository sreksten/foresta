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

## 3. Disegno (da confermare)

Il tutorial è **un elenco di suggerimenti**, e **la UI decide quando mostrarli** guardando quello che ha davanti. Nessun evento nuovo: dove la UI già sa una cosa (le icone nella barra, l'arrivo in una locazione, l'apertura del combattimento) la usa.

- **Un suggerimento ha tre cose**: *quando scatta*, *a che cosa punta* e *il testo* (le pagine del fumetto).
- **Quando scatta** è sempre qualcosa che la UI vede:
  - **le icone presenti nella barra**: se ci sono le quattro icone di movimento (NORD, EST, SUD, OVEST) scatta, una volta, la spiegazione di come muoversi; se c'è l'icona dell'inventario, quella dell'inventario; se compaiono quelle del combattimento, quelle del combattimento; e così per le tattiche, le pozioni, l'accampamento, il salvataggio, le botteghe;
  - **la prima volta che si arriva in una locazione**: scattano, uno dopo l'altro, i suggerimenti dei riquadri (il gruppo, le missioni, la mappa...);
  - **un evento che la UI già riceve** per le altre situazioni (per esempio `InternoRichiestaAperturaFinestraCombattimento` per il primo combattimento).
- **Come funziona a ogni aggiornamento**: la UI chiede al registro «c'è qualcosa da spiegare adesso?», dicendo che cosa vede (le icone della barra, la schermata, l'evento appena arrivato). Il registro risponde con il primo suggerimento non ancora visto che corrisponde, oppure con niente. Se c'è, la UI disegna il fumetto con la coda che punta al bersaglio e fa lampeggiare il bordo. Alla pressione del pollice la UI dice al registro «visto», e il fumetto passa al successivo.
- **Il registro è solo una memoria** (`RegistroSuggerimenti`, sul modello di `RegistroBestiario`): l'elenco dei suggerimenti, quali sono già stati visti, se il tutorial è acceso o spento. **Non sa niente della UI e non manda eventi.** La UI usa il modulo del tutorial e il modulo non usa la UI: una direzione sola, nessun giro.
- **Il modulo nuovo è piccolo e sta in basso**: contiene l'enum dei suggerimenti (`TipoSuggerimento`, con i suoi testi per lingua), i bersagli (`BersaglioDelSuggerimento`: il riquadro del gruppo, quello delle missioni, le icone di movimento...), il registro e l'interfaccia per salvare cosa si è visto. Dipende solo da cose basse (i comandi, i testi, la serializzazione), quindi il suo livello è 03 e non cambia il livello di nessun modulo esistente: niente da rinominare.
- **Il motore fa due cose sole**: scrive su file cosa si è visto (come per il bestiario, nella cartella dell'utente, non nel salvataggio) e sta fermo mentre il fumetto è aperto (la UI dice già al motore «sono occupata»).
- **Il fumetto del tutorial** (modulo comune per il modello, disegno in Swing e libGDX): coda che punta al bersaglio, le pagine, il pollice (`SI`: avanti sulle pagine intermedie, chiusura sull'ultima), l'annulla (`ANNULLA`: spegne il tutorial). Il bordo che lampeggia sul bersaglio: la UI sa già dove stanno i riquadri (`DisposizioneDelloSchermo`) e le icone (`BarraIcone.rettangoloDisegnato`).
- **Testi diversi sul tocco**: la spiegazione dei gesti cambia (trascinare invece del mouse, tenere premuto per la descrizione): la UI sceglie la variante `.TOCCO` quando c'è il dito (la stessa condizione che già accende i gesti del dito in libGDX).
- **Un suggerimento alla volta**: se la stessa situazione ne fa scattare più d'uno, vanno in ordine fisso, ognuno dopo il pollice del precedente. Mai durante un intermezzo né nel mezzo di un'azione che chiede attenzione.

## 4. Quali suggerimenti (prima bozza)

| Quando | Che cosa spiega |
| :--- | :--- |
| Prima schermata di gioco | Che cliccando (o toccando) un riquadro lo si porta in primo piano: gruppo, incantesimi e pozioni, missioni, mappa. E come si spegne il tutorial |
| Il riquadro delle missioni (la prima volta che compare) | Che lo si porta in primo piano, che si scorrono le missioni (mouse; trascinando sul tablet) e che si «chiude» una missione cliccandoci sopra |
| Prima volta che si vede la barra delle icone | Che le icone cambiano con la situazione e che l'icona «?» dà la descrizione di ognuna (sul tocco: tenere premuto) |
| Prima apertura della mappa a tutto schermo | A cosa serve: dove si è, cosa si è visitato, come si trascina; il notiziario sotto; il joystick sul tocco |
| Primo spostamento | Direzioni e numero di passi |
| Primo combattimento | Il riquadro del combattimento (chi agisce, chi è bersaglio, le barre di salute); che i personaggi seguono la tattica |
| Prima volta che si può fuggire o scegliere (singolo attacco, fuga) | Cosa fa ogni comando in combattimento |
| Prima volta in cui compare «Tattiche» | Che si può cambiare come ogni personaggio sceglie cosa fare |
| Primo oggetto trovato | Come si equipaggia dall'inventario e come si passa a un compagno |
| Prima pozione disponibile | Quando serve berla (le piccole solo se qualcuno è ferito; le grandi alzano il massimo) |
| Prima città o bottega | Cosa si può comprare, vendere, incantare |
| Prima notte | L'accampamento, e perché serve qualcuno di guardia |
| Prima volta sul tocco | Gesti: tocco, doppio tocco, pressione prolungata, trascinamento |

## 4 bis. Pilotare la partita durante il tutorial (idea, 2026-11-07)

A tutorial attivo il gioco si **pilota** perché il giocatore incontri le cose una alla volta. Decisione dell'utente: **il mostro si sceglie truccando il dado, punto** (non con un filtro sui candidati). Quello che ci siamo detti:

- **Cosa guida la scelta è ciò che il giocatore ha già visto**, non il numero del turno. Finché non ha visto i suggerimenti di base (l'attacco singolo e il combattimento) il dado è truccato in modo che **non escano corrompibili né amichevoli** (`Personaggio.isCorrompibile()`, `isAmichevole()`, proprietà della classe), così le icone da spiegare sono poche. Dopo, il dado è truccato per far uscire il mostro adatto al passo successivo: un corrompibile (per esempio il goblin), che fa scattare il suggerimento sulla corruzione quando lo si incontra; poi un amichevole, che fa scattare quello sull'amicizia.
- **Attenzione al giro vizioso:** il trucco non può dipendere dal suggerimento della cosa che sta per comparire (i corrompibili non possono aspettare il suggerimento sulla corruzione, che appare solo quando se ne incontra uno). Conta solo il suggerimento **precedente**.
- **Prima di fissare la sequenza** va guardato quali mostri compaiono nei primi luoghi e a che livello, per sapere quali esiti del dado servono.
- **Trucchi anche sulle scelte:** a tutorial attivo si può truccare il gioco a fondo (anche «in maniera indecente», parole dell'utente) in modo che le scelte del giocatore siano più o meno obbligate: per esempio, nel primo scontro può fare solo un attacco semplice; nel secondo gli arriva l'occasione di corrompere.
- **Interruttore:** tutto questo vale solo con il tutorial attivo e il suggerimento non ancora visto. Se i suggerimenti sono spenti o azzerati, i dadi tornano identici a oggi.

## 5. Da decidere

- **La conferma del disegno** del §3 (la UI decide guardando le icone e gli eventi che già riceve; il registro è solo una memoria).
- **Quanto devono essere lunghi, in pagine**: una pagina per ogni cosa che si può fare, o due-tre frasi per pagina.

## Osservazioni

- Lo stesso testo serve a tutte le UI, quindi vanno scritti una volta sola e in tutte le lingue (italiano, inglese, inglese britannico).
- L'icona di annullamento dentro il fumetto spegne **tutto** il tutorial, non solo il suggerimento in corso: va detto chiaramente nel primo suggerimento, e il ripristino sta solo all'inizio di una partita nuova, quindi non c'è un modo di riaccenderlo a metà.

## Da fare / idee aperte

- Rispondere alle domande del §5.
- Pilotare la partita (§4 bis): guardare quali mostri compaiono nei primi luoghi e decidere la sequenza dei trucchi sui dadi.
- Verificare per ognuna delle righe del §4 quale evento già esiste e quale va aggiunto.
- Scrivere i testi (prima in italiano, poi nelle altre lingue).
- Calcolare il livello esatto del modulo nuovo dalle sue dipendenze, e scegliere il nome (`NN-foresta-<nome>`).
