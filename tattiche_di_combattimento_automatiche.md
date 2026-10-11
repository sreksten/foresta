# Tattiche di combattimento automatiche

Stato: **idea in studio, niente è ancora nel codice.** Questo documento raccoglie le basi per il piano: l'idea, il nuovo
funzionamento a turni dei due gruppi (round-robin), le azioni possibili, i bersagli e le condizioni che i giochi di riferimento
offrono, con un commento per ognuno su quanto è già leggibile dal modello dati di Foresta. Il piano vero (formato delle tattiche,
motore, scheda del personaggio, salvataggio) viene dopo, a partire da qui.

## 1. L'idea

Oggi in combattimento agisce un solo personaggio, scelto dal giocatore, e l'avversario gli risponde. Si vuole invece che **tutto il
gruppo agisca a turno** (round-robin), e che ogni personaggio decida da solo che cosa fare scorrendo la propria **tattica**: un
elenco ordinato di **voci**, che il giocatore modifica in una scheda secondaria del personaggio (come le *Tactics* di *Dragon Age:
Origins* o i *Gambit* di *Final Fantasy XII*).

Ogni **voce** ha tre campi:

- il **bersaglio**: chi colpire o curare (un selettore: "un avversario magico", "il capo avversario", "il più debole", "l'alleato più
  ferito"...);
- una **condizione** facoltativa ("se ho abbastanza magia", "se il bersaglio non è già silenziato"; vuota vale "sempre");
- l'**azione**: che cosa fare (attacco, un incantesimo, una pozione...).

Il flusso è sempre lo stesso:

1. si scorre l'elenco dall'alto: l'ordine è la **priorità**;
2. la **prima voce che si può davvero eseguire** (c'è un bersaglio che corrisponde, la condizione è vera, c'è abbastanza magia, il
   personaggio ha l'incantesimo scelto...) viene eseguita, e l'azione del personaggio finisce lì;
3. se una voce non si può eseguire, si passa alla successiva;
4. l'ultima voce è sempre un **attacco fisico**, che non fallisce mai.

Per esempio: "bersaglio: un avversario magico; azione: Dardo arcano (se ho la magia)" e subito dopo "bersaglio: lo stesso; azione: attacco".
Dopo l'azione c'è la **risposta del gruppo avversario**, e si passa al personaggio successivo (§3). **Le stesse tattiche valgono per
gli avversari** (§4): ogni mostro ha un set di tattiche base.

L'elenco deve essere **salvabile e ripristinabile**: con la partita, e anche come modello da riapplicare ad altri personaggi.

## 2. Com'è oggi il combattimento

Vedi [`motore_di_gioco.md`](motore_di_gioco.md) §7. Quello che conta per le tattiche:

- Un solo **combattente**, scelto dal giocatore (`PERSONAGGIO_n`), attacca a ogni `TIMER` il primo avversario vivo
  (`GruppoAvversario.getPersonaggioVivo`); poi l'avversario **risponde sul combattente** (`rispondiInMischia`: armi o, per chi sa la
  magia, un incantesimo a caso del suo repertorio, che rinuncia se è `SILENZIATO`). Gli altri personaggi non fanno niente. Se gli
  avversari sono più numerosi dei compagni ne arriva un altro, "si disimpegna e attacca" (`LocazioneBase.eseguiSingoloAttacco`).
- Gli **incantesimi** e le **pozioni** sono del gruppo: le pergamene e le pozioni stanno nell'inventario del gruppo
  (`GruppoGiocatore.getIncantesimi(classe)`, `getPozioniSalute()`...), e chi li usa lo sceglie il giocatore (o il motore: `getFormulante`).
  Gli incantesimi **innati** (dardo arcano, alba sacra, i tre canti) non consumano pergamene.
- La risposta avversaria ha già un embrione di tattica, scritto nel codice (`PersonaggioBase.attacca(Gruppo)` e `scegliIncantesimoContro`):
  chi colpire, se lanciare un incantesimo, quale, e se conviene più la magia o l'attacco fisico. Vedi §4.
- Non c'è **posizione**: né distanza, né mischia contro distanza, né linea di vista. Non c'è un turno di preparazione degli
  incantesimi (sono istantanei) né cooldown. Gli avversari non hanno un "bersaglio" persistente: rispondono a chi ha appena agito.

## 3. Il round-robin dei due gruppi

Il turno a rotazione vale per **tutti e due i gruppi**, e va tracciato in questo piano perché le tattiche ne dipendono.

**Il puntatore.** Ogni gruppo ricorda **chi ha agito per ultimo** e passa la mano al **successivo personaggio vivo** (saltando chi è
caduto o in panchina). Il puntatore di ogni gruppo avanza **sempre**, a ogni azione, e non si azzera fra uno scontro e l'altro dentro la
stessa locazione. Oggi il `Gruppo` non lo ha.

**La risposta.** A ogni azione del giocatore **risponde il prossimo avversario vivo** del gruppo avversario, quello a cui tocca nel suo
round-robin. Se faccio un attacco singolo, c'è una risposta sola; se lancio un incantesimo, c'è una risposta sola; se faccio un
combattimento in cui agisce tutto il gruppo, ogni azione del gruppo ha la sua risposta. **Chi colpire non è una regola fissa**: lo
decide la tattica dell'avversario (§4), come per i miei personaggi.

**Azioni manuali.** Agisce il personaggio scelto dal giocatore (`PERSONAGGIO_n`, come oggi); fa **una** azione, e il puntatore del
gruppo passa **al successivo** vivo rispetto a lui.

**Eventi a metà round.** Chi muore durante il round non agisce più, e il puntatore salta i caduti: se cade un compagno che doveva ancora
agire, la rotazione passa al personaggio successivo. Se cade l'ultimo avversario il round finisce lì (lo scontro è vinto, nessuno
agisce più); se cade il capo del giocatore la partita è persa, come oggi.

**Interruzione.** La **fuga** resta del giocatore: è un'interruzione manuale del combattimento (`INTERRUZIONE_COMBATTIMENTO`, o il
bottone di fuga) che rimette la scelta alle sue mani. Le tattiche non fuggono.

### 3.1 Prima fase: attacchi singoli

Si comincia dagli **attacchi singoli**: ogni azione di un personaggio, scelta dal giocatore o dalla sua tattica, ha **una** risposta, e
basta. È una semplificazione che **penalizza il gruppo più numeroso**: tre compagni contro un avversario solo agiscono tre volte
ognuno con la propria risposta, cioè l'avversario agisce tre volte per giro (e subisce, certo, tre azioni). Va rivista con la fase
seguente. Intanto i test che contano i round e le simulazioni di bilanciamento continuano a fare combattere un personaggio alla volta.

### 3.2 Seconda fase (da rivedere): il round a coppie

Nel combattimento a turni di tutto il gruppo, a inizio round si contano i vivi dei due gruppi (*n* i miei, *m* gli avversari) e si
fanno **max(n, m) coppie**: alla coppia *k* agisce il mio personaggio *k* (se *k* < *n*) e risponde l'avversario *k* (se *k* < *m*).

| Caso | Che cosa succede |
| :--- | :--- |
| 3 miei contro 2 avversari | il mio primo agisce e risponde il primo avversario; il mio secondo agisce e risponde il secondo; il mio terzo agisce **senza risposta** |
| 1 mio contro 3 avversari | il mio agisce e risponde il primo avversario; poi **attaccano anche il secondo e il terzo**, senza una mia azione in mezzo: il mio personaggio ne subisce tre |
| 2 contro 2 | due coppie |

È l'effetto voluto: un avversario solo contro tre compagni agisce una volta e subisce più danni; un compagno solo contro tre avversari
ne subisce tre. **La regola "si disimpegna e attacca" esce dal codice**: è solo il caso in cui il gruppo avversario è più numeroso. Il
numero delle coppie si calcola a inizio round e non si ricalcola quando qualcuno cade: le azioni che non hanno più un attore o una
risposta si saltano. Le risposte "in più" (nessuna mia azione) scelgono il bersaglio con la tattica dell'avversario, come le altre.

## 4. Le tattiche degli avversari

**Lo stesso modello.** Ogni avversario ha una tattica fatta di voci (bersaglio, condizione, azione), valutata come quella dei compagni:
la prima voce eseguibile vince. Le azioni sono quelle del suo repertorio (attacco con l'arma o l'arma naturale, i suoi incantesimi
malefici); i selettori sono gli stessi di §6, letti dal punto di vista dell'avversario (per lui i "nemici" sono i miei personaggi).

**Che cosa c'è già, nel codice.** Una versione molto semplice, che le tattiche renderebbero esplicita e parametrizzabile:

| Decisione | Oggi (`PersonaggioBase`) | Come voce di tattica |
| :--- | :--- | :--- |
| **Chi colpire** (`attacca(Gruppo)`) | il **Mago o la Maga più potenti** (livello più alto, a parità il primo); poi il **Sacerdote o la Sacerdotessa più potenti**; poi l'**Elfo o l'Elfa più potenti**; altrimenti **a caso** fra i personaggi vivi e gli ospiti vulnerabili vivi, tutti con la stessa probabilità (`PersonaggioBase.scegliBersaglio`) | un selettore: "un avversario magico", "il più debole", "il primo", "a caso" |
| **Se lanciare un incantesimo** (`scegliIncantesimoContro`) | no se non è magico (`isMagico`), è `SILENZIATO`, ha 0 `MAGIA` o non può pagarne nessuno; poi **una volta su tre non lancia** | le condizioni "magia sufficiente" e "probabilità del n%" |
| **Quale incantesimo** | `INTELLIGENZA` < 5: **a caso**; < 7: **il più potente**; da 7: **il più conveniente** fra incantesimo e attacco fisico, confrontando probabilità di colpire × danno atteso | un **criterio di scelta** dell'azione: a caso, il più potente, il più conveniente |
| **Magia o attacco fisico** | solo per `INTELLIGENZA` ≥ 7, con quel confronto | idem: l'ultima voce è comunque l'attacco fisico |

**Mostri stupidi e mostri intelligenti.** Un mostro stupido colpisce il primo che trova; uno intelligente il più debole, per ridurre gli
attacchi del giro dopo; uno ancora più furbo cerca di mettere a tacere un mago. Quindi **ogni mostro avrà un set di tattiche base** a
inizio gioco, in funzione della classe e dell'intelligenza: per esempio

- *bruto* (Goblin, Hobgoblin, Scheletro, Troll...): bersaglio il primo; attacco fisico;
- *astuto* (Arpia, Folletto, Chimera...): bersaglio il più debole; incantesimo a caso, altrimenti attacco;
- *stratega* (Strega, Lich, boss): bersaglio un avversario magico non ancora silenziato, con il Silenzio o un attacco arcano se ne ha; poi il più
  debole; incantesimo più conveniente o attacco.

I set sono dati (un file di risorsa per classe, o per fasce di intelligenza), non codice: il comportamento di oggi si riproduce
esprimendolo come tattica base, e si controlla con un test che le decisioni siano le stesse.

**Il silenzio.** `SILENZIATO` lo dà l'**incantesimo del Silenzio** (`incantesimi/Silenzio`: malefico, arcano, pergamena a 5 monete
dall'alchimista, 5 `MAGIA` a lancio, durata di `calcolaDurataStato`), ma anche, con una probabilità, ogni attacco di tipo **arcano**
(il Dardo arcano, l'arma naturale del Lich, gli incantamenti arcani; `TipoDanno.ARCANO`). Il Silenzio ha comunque il tiro per colpire
e l'immunità del bersaglio, quindi non è una garanzia. "Silenziare un mago" è una voce come "bersaglio: un avversario magico non
silenziato; azione: Silenzio" (con il Dardo arcano o un attacco arcano come ripiego). Oggi, senza tattiche, il Silenzio è fra i
malefici del repertorio dei mostri come gli altri, quindi lo lanciano a caso (e silenziano chi stanno attaccando), mentre il giocatore
lo lancia sul gruppo avversario e il codice sceglie il bersaglio: il primo vivo non ancora silenziato, altrimenti quello col
silenzio più corto (`Silenzio.scegliBersaglio`). Lanciato su un personaggio silenzia proprio lui: con i selettori delle tattiche
sarà la voce a sceglierlo.

**Gli ospiti vulnerabili** non hanno più una regola a parte: sono fra i candidati dell'ultima scelta, quella a caso (`Gruppo.getOspitiBersaglio`), quindi un mago o un sacerdote vivo li protegge. Un ospite non vulnerabile non è mai un bersaglio.

## 5. Le azioni possibili

Un personaggio può compiere solo le azioni già disponibili nella barra dei comandi in combattimento (`LocazioneBase.impostaComandiPossibili`).
**Corruzione, amicizia, fuga e passare inosservati sono escluse**: le prime due non sono nemmeno più disponibili appena si combatte
(le loro opzioni si spengono all'inizio dello scontro, `opzioneCorruzioneDisponibile` e `opzioneAmiciziaDisponibile`), la fuga è
una scelta del giocatore (vedi sopra) e passare inosservati non è possibile dopo una vera azione. Per le altre:

| Azione | Chi | Serve | Bersaglio | Note |
| :--- | :--- | :--- | :--- | :--- |
| **Attacco fisico** (`SINGOLO_ATTACCO`) | tutti i combattenti | niente: è l'ultima voce, non fallisce mai (un'arma o l'arma naturale c'è sempre) | un avversario vivo | la seconda fase con la doppia arma (Ladro, Elfo) resta dentro l'attacco |
| **Incantesimo maleficio** (Aria, Acqua, Terra, Fuoco, Fulmine, Gelo, Veleno; Silenzio sotto) | chi `puoFormulare(classe)` | almeno una pergamena della classe nel gruppo, `MAGIA` per il costo, non `SILENZIATO` | un avversario; l'incantesimo `MULTIPLO` colpisce fino a `NUMERO_BERSAGLI` avversari (oggi i primi vivi) | va scelta la classe di incantesimo |
| **Silenzio** | idem | idem; 5 `MAGIA` | un avversario: sul gruppo il primo vivo non silenziato, altrimenti col silenzio più corto; su un personaggio proprio lui | mette `SILENZIATO`, senza danni; con le tattiche: "un avversario magico non silenziato" |
| **Morte** | idem | idem; solo sui vivi | un avversario (`SINGOLO_SOLO_VIVI`) | |
| **Resurrezione** | chi la sa | pergamena e `MAGIA` | un solo bersaglio, anche morto (`SINGOLO_QUALSIASI`), ma oggi solo un compagno del gruppo; il testo dice che contro i non morti fa danno, ma non è implementato (`todo.md`) | su un alleato caduto |
| **Alba sacra** | Sacerdote/Sacerdotessa (innata) e chi ha la pergamena | `MAGIA` | tutto il gruppo (toglie stati e modificatori temporanei) | tipicamente se qualcuno ha uno stato negativo |
| **Dardo arcano** | Mago, Maga, Elfo, Elfa (innato) | `MAGIA` | un avversario | |
| **Canto curativo** | Bardo, Cantastorie, bardo della locanda | `MAGIA` (4); solo se qualcuno è ferito | il più ferito | |
| **Canto ipnotico** | idem | `MAGIA` (3); solo contro avversari vivi | fino a `NUMERO_BERSAGLI` avversari (`SPAVENTATO` o `CONFUSO`) | |
| **Urlo della Banshee** | idem, ma solo dopo che il gruppo l'ha imparato (il bardo della locanda lo sa sempre) | `MAGIA` (6); solo contro avversari vivi | fino a `NUMERO_BERSAGLI` avversari: chi cede prende `SPAVENTATO` e `CONFUSO` insieme | un'alternativa più forte al canto ipnotico |
| **Canto di valore** | idem | `MAGIA` (5) | tutto il gruppo (`ISPIRATO`) | a inizio scontro |
| **Pozione della Salute** (normale, grande) | tutti | almeno una nel gruppo | un personaggio del gruppo (chi beve) | |
| **Pozione della Magia** (normale, grande) | tutti | almeno una nel gruppo | un personaggio del gruppo | |
| **Cambiare arma** | chi può usare quell'arma (`puoEquipaggiare`) | un'arma nell'inventario del gruppo, che il personaggio possa impugnare e che non sia quella che ha già; il controllo preventivo del cambio riesce; non si è già al massimo di cambi | l'arma stessa (non un avversario) | **è l'azione del turno**: il personaggio cambia arma e basta; l'attacco c'è dal turno dopo (§5, "Cambiare arma") |

**Un incantesimo "del repertorio".** Oltre a una classe precisa, la voce può dire "un incantesimo malefico qualsiasi" con un **criterio di
scelta**: *a caso*, *il più potente* o *il più conveniente* (probabilità di colpire × danno atteso, contro l'attacco fisico): sono le tre
logiche che oggi usano i mostri in base all'intelligenza (§4). Vale anche per i personaggi del giocatore.

**Il silenzio è un'azione.** Lo stato `SILENZIATO` lo mette l'incantesimo del Silenzio (riga sopra), o con una probabilità un attacco di
tipo **arcano** (il Dardo arcano di Mago ed Elfo, l'arma naturale del Lich, gli incantamenti arcani). Una voce "silenzia il mago" è
"azione: Silenzio" su un avversario magico non ancora silenziato, e subito dopo, per chi non ha la pergamena o la `MAGIA`, "azione:
Dardo arcano" sullo stesso bersaglio.

**Cambiare arma è un'azione.** Un personaggio può decidere, per esempio, di prendere dall'inventario del gruppo un'arma che dà danno
`SACRO` se tra gli avversari ci sono dei non morti: "condizione: c'è un avversario non morto e non ho già un'arma sacra e nel gruppo
ce n'è una che posso usare; azione: cambia arma, scegliendo una che dà SACRO". Alcune regole, da fissare in questo modo:

- **Il cambio è tutta l'azione del turno**: il personaggio sceglie l'arma, la impugna e il suo turno finisce lì, senza attaccare;
  l'attacco col nuovo equipaggiamento viene dal turno dopo. Vale anche con la doppia arma (Ladro, Elfo): un cambio singolo, della
  principale o della secondaria, è comunque tutto il turno, e per cambiarle entrambe servono due turni di combattimento senza attaccare.
- **Se il cambio non è possibile, l'azione non c'è**: la voce non si esegue e si passa alla successiva dell'elenco, come per un
  incantesimo senza `MAGIA`. Il controllo è **preventivo** (prima di toccare niente) e **riusa i blocchi che ci sono già** per
  l'equipaggiamento, senza regole nuove: `RegoleEquipaggiamento.valuta` dice di no se l'arma è troppo pesante per il carico
  (`TROPPO_CARICO`), se non è utilizzabile dalla classe (`NON_ADATTO_ALLA_CLASSE`), se manca la forza o il livello, se la mano è
  occupata, se è a due mani e non si hanno le mani libere, se non è da mano secondaria... In quel caso l'arma di prima non viene
  nemmeno tolta.
- **Il controllo deve guardare il dopo-cambio.** `valuta` guarda l'equipaggiamento *com'è adesso*, con l'arma di prima ancora in
  mano e nel carico. Per il cambio, a mano, il giocatore prima toglie la vecchia e poi prende la nuova: se la tattica chiamasse
  `puoEquipaggiare` prima di togliere, i risultati sarebbero sbagliati in entrambe le direzioni. Una spada nuova verrebbe respinta
  perché la mano principale è occupata (`ARMA_NON_DA_MANO_SECONDARIA`), un'arma troppo pesante per il carico di adesso entrerebbe
  una volta uscita la vecchia, e un pugnale verrebbe accettato nella mano secondaria, che non è un cambio. Per questo il controllo
  si fa sulla situazione "senza l'arma che esce" (per esempio togliendola, chiamando `valuta` e rimettendola a posto se dice di no,
  che è sicuro perché nel turno agisce uno solo), mentre i blocchi di `valuta` restano gli stessi. Un tentativo che viene
  rifiutato (si toglie la vecchia, la nuova non va, si riprende la vecchia) è un'azione **senza effetto**: non consuma il turno e si
  passa alla voce successiva.
- **Ogni arma si valuta una volta sola.** Chi ha provato un'arma e ha visto che non può impugnarla se lo ricorda, per non rifare le
  prove a ogni turno e a ogni voce. In teoria il personaggio potrebbe valutare tutte le armi dell'inventario del gruppo, ma lo fa
  **una volta sola** e si segna quali può usare (e quali no), per quel combattimento. La valutazione dipende da chi la fa (classe,
  livello, forza, carico) e da quello che ha in mano: se uno di questi cambia (un'arma nuova nell'inventario, un cambio d'arma, un
  livello, un effetto che cambia il carico) le valutazioni vanno rifatte o scartate. Il dato sta nello stato del combattimento (§8).
- **Con la doppia arma, quale esce** (da valutare bene): dovrebbe uscire l'arma **meno conveniente da usare in quel momento**, forse
  quella che fa meno danni o che applica meno effetti di stato o modificatori (a parità, la secondaria). Non c'è ancora un criterio:
  va stabilito, magari con la stessa misura del "più conveniente" fra incantesimo e attacco fisico (probabilità di colpire × danno atteso),
  a cui sommare gli effetti di stato e i modificatori che l'arma applica. Finché non c'è, il cambio vale per l'arma che occupa lo slot
  dell'arma nuova.
- **Un massimo di cambi per combattimento, per personaggio**: **uno, al più due** (una costante in `Costanti`), per fermare i continui
  cambi anche quando le condizioni lo permettono. Si conta nello stato del combattimento (§8) e ricade fra le condizioni della voce
  ("non ho ancora cambiato arma").
- **Come si esegue**: l'azione fa i due passi, uno dopo l'altro e nello stesso turno, **come se il giocatore equipaggiasse a mano**:
  stocca l'arma di prima nell'inventario del gruppo (`InternoStoccaggioArtefatto`) e preleva la nuova (`InternoPrelievoArtefatto`).
  Siccome il cambio avviene nel turno di quel personaggio e agisce uno alla volta, due personaggi non possono cambiare arma insieme: è
  un'**azione atomica**, già validata dal controllo preventivo e dal `GruppoGiocatore`, che non deve temere un rifiuto a metà.
- **Si torna indietro con un'altra voce**, altrimenti l'arma sacra resterebbe in mano anche dopo i non morti: "condizione: non ci sono
  non morti tra gli avversari e ho in mano un'arma sacra e nel gruppo c'è quella di prima; azione: cambia arma". Quel cambio conta nel
  massimo, quindi con un solo cambio possibile non si torna indietro: per questo il limite è uno, al più due. Servono anche condizioni
  che evitino di cambiare a ogni turno ("non ho già un'arma che dà quel danno").
- **Per i mostri non si applica**: usano l'arma naturale e non hanno un inventario.

Nei giochi di riferimento ci sono azioni che qui non esistono: **muoversi, seguire, mantenere la posizione,
parare, rubare, usare un oggetto di scena**. Un'azione **"passa il turno"** non c'è oggi, ma potrebbe servire come ultimo passo
alternativo per chi non deve combattere (per esempio un curatore senza magia).

## 6. Il bersaglio: un campo della voce

Il bersaglio è scelto da un **selettore**, che sta nella voce. Dipende dal tipo di azione: gli attacchi e i malefici vogliono un
avversario, le pozioni, il canto curativo e la resurrezione un alleato, l'alba sacra e il canto di valore tutto il gruppo (nessun
selettore). Per i giochi di riferimento vale la nota in testa a §7.

### 6.1 Selettori per gli avversari

| Selettore | Giochi | In Foresta |
| :--- | :--- | :--- |
| Il primo / un avversario qualsiasi (il "più vicino") | DA:O (*Any, Nearest visible*), FFXII (*nearest*) | **sì**: oggi è il primo vivo (`getPersonaggioVivo`); senza distanza "più vicino" vale "il primo dell'elenco" |
| **A caso** | BG (*random*) | **sì**: `Dado` |
| **Il capo** del gruppo avversario | (specifico) | **sì**: `getCapo()` del gruppo |
| Il **più debole** | DA:O, FFXII (*lowest HP*) | **sì**: per salute attuale più bassa (`getSalute()`); vedi la nota sotto sulle misure di forza |
| Il **più forte** | DA:O, FFXII (*highest*) | **sì**: per livello più alto, salute massima o salute attuale più alta: serve scegliere la misura |
| Quello con **salute sotto una soglia** (%) | DA:O (*Health < n%*), FFXII | **sì**: `getSalute() / getSaluteMassima()` |
| Di livello più alto o più basso del mio | FFXII (*level*) | **sì**: `getLivello()` |
| **Di una classe** (un mago, un ladro...) | DA:O (*class/type*) | **sì**: `getClasse()` |
| **Magico**, che sa lanciare incantesimi (per silenziarlo o colpirlo per primo) | DA:O (*Mage*), FFXII | **sì**: `isMagico()` (livellamento magia > 0) dice che ha la magia, e `getMagia()` quanta gliene resta. Sono magici anche boss, arpie, folletti... non solo i maghi |
| Di una **categoria** (non morto, bestia, umanoide, volante, draconico, gigantesco, ibrido, costrutto) | DA:O (*creature type*), FFXII (*undead, flying*) | **sì**: `TipoPersonaggio.getCategorie()` / `isDellaCategoria` (`tipi/CategoriaPersonaggio`). Una classe ha una o più categorie (Spirito e Ombra Nera sono non morti e volanti, la Chimera bestia e ibrida, il Minotauro Gigante umanoide, ibrido e gigantesco), quindi il selettore "di categoria X" vale per chi ne ha anche solo una |
| Un **boss** / di rango elevato | DA:O (*rank*) | **per ora non serve**: non c'è una distinzione vera, perché i boss (Lich, Minotauro Gigante, Strega...) si incontrano di solito da soli, quindi non c'è un "altro" da scegliere. Se servisse, il modo più semplice è trasformare quegli incontri in **Ondate** (da valutare: è in `todo.md`) |
| Con un certo **effetto di stato** (stordito, avvelenato, congelato...) | DA:O, FFXII (*status*) | **sì**: `hasEffettoDiStato(tipo)`, 19 tipi (`TipoEffettoDiStato`) |
| **Senza** un certo effetto (non ancora silenziato, non ancora avvelenato) | FFXII (*status ≠*) | **sì**: `!hasEffettoDiStato(tipo)` |
| **Debole a un elemento** | DA:O (*vulnerability*), FFXII (*weak to element*) | **parziale**, in tre pezzi che vanno composti (§6.3): la debolezza di classe (`Personaggio.getMoltiplicatoreDannoSubito`: il `SACRO` sui non morti ×2, le immunità), quella momentanea per stato (`BAGNATO` + fulmine...) e la difesa numerica (`difesaContro`); non c'è una lista di debolezze già pronta |
| Non **immune** a quell'incantesimo, tipo di danno o stato (per non sprecare la magia) | DA:O, FFXII | **sì**: `isImmuneAIncantesimo`, `isImmuneATipoDanno`, `isImmuneAEffetto` |
| Con una certa **armatura** (pesante, leggera) | DA:O (*armor*) | **parziale**: i personaggi hanno elmo, scudo, armatura e schinieri con resistenze; i mostri combattono con l'arma naturale |
| **Lo stesso del turno precedente / del compagno** (fuoco concentrato) | FFXII (*last target, leader's target*) | **da aggiungere**: basta uno stato del combattimento con l'ultimo bersaglio di ogni personaggio |
| Quello che **attacca me / un alleato** | DA:O (*targeting*), FFXII | **parziale**: oggi si risponde a chi ha agito; per un "bersaglio dell'avversario" persistente va ricordato l'ultimo bersaglio di ognuno |
| Che sta **lanciando** un incantesimo | DA:O, FFXII | **no**: gli incantesimi sono istantanei |
| A distanza o in mischia | DA:O, FFXII | **no**: non c'è posizione, né armi a distanza (spada, mazza, ascia, spadone, lancia, bastone magico) |

**Le categorie di creatura** (`tipi/CategoriaPersonaggio`, assegnate in `TipoPersonaggio`; `getCategorie()` e `isDellaCategoria`). Una
classe ne ha una o più, e un selettore "di categoria X" prende chi ne ha anche solo una:

| Categoria | Che cosa vuol dire | Classi |
| :--- | :--- | :--- |
| **Umanoide** | forma umana | giocanti, non combattenti, Viandante, Eremita, Strega, Goblin, Hobgoblin, Troll, Folletto, Gigante, Titano, Arpia, Centauro, Minotauro, Minotauro Gigante |
| **Bestia** | animale o bestia | Chimera, Chimera Drago, Drago, Idra, Viverna |
| **Draconico** | un drago o un suo parente | Drago, Chimera Drago, Viverna |
| **Non morto** | morto che continua ad agire (non è mai umanoide) | Fantasma, Spettro, Spirito, Ombra Nera, Lich, Scheletro |
| **Volante** | si muove in volo | Arpia, Drago, Viverna, Folletto, Gargoyle, Fantasma, Spettro, Spirito, Ombra Nera |
| **Gigantesco** | **taglia** esagerata, non la razza dei giganti | Gigante, Titano, Troll, Idra, Drago, Minotauro Gigante |
| **Ibrido** | corpo fatto di più creature | Arpia, Centauro, Minotauro, Minotauro Gigante, Chimera, Chimera Drago |
| **Costrutto** | creatura fabbricata o animata (un giorno i golem) | Gargoyle |

Le classi con più categorie sono la regola per i mostri più strani: Spirito e Ombra Nera sono non morti e volanti, la Chimera Drago è
bestia, ibrida e draconica. La Chimera Drago non è volante, il Gargoyle non è umanoide.

**Le misure di "più forte" e "più debole".** Per un avversario "debole" è naturale la **salute attuale più bassa** (finirlo presto); "forte" può
essere il **livello più alto**, la **salute massima** più alta o la salute attuale più alta. Meglio offrire poche scelte esplicite
("più ferito", "meno ferito", "di livello più alto", "di livello più basso") che una parola ambigua.

### 6.2 Selettori per gli alleati (per cure, resurrezioni, pozioni)

| Selettore | Giochi | In Foresta |
| :--- | :--- | :--- |
| Se stesso | DA:O (*Self*) | **sì** |
| **Il capo** del gruppo (se cade si perde) | (specifico: `GIOCO_PERSO`) | **sì**: `gruppo.getCapo()` |
| L'alleato **più ferito** / con salute sotto una soglia | DA:O (*Ally: health < n%*), FFXII | **sì**: sui personaggi vivi |
| Un alleato **caduto** (per la resurrezione) | DA:O (*dead*), FFXII (*KO'd*) | **sì**: `!isVivo()`; il gruppo dà i vivi, i caduti si ricavano dall'elenco |
| Un alleato con un certo **stato** (avvelenato, confuso...) | DA:O, FFXII | **sì**: `hasEffettoDiStato` |
| Un alleato con **poca magia** (per la pozione della Magia) | FFXII (*MP < n%*) | **sì**: `getMagia() / getMagiaMassima()` |
| Un alleato di una certa **classe** | DA:O | **sì**: `getClasse()` |
| Un **ospite** da proteggere | (specifico: missioni di scorta) | **no**: gli ospiti non combattono e non sono mai bersaglio delle tattiche |

### 6.3 "Debole a un elemento"

"Elemento" qui è il **tipo di danno** (`TipoDanno`: fisico, elementale o magico). Oggi la debolezza di un avversario nasce in tre
punti diversi del codice, che ora si possono interrogare insieme con `CalcolatoreCombattimento.moltiplicatoreAtteso`:

| Origine | Dove sta | Che cosa dà | Limite |
| :--- | :--- | :--- | :--- |
| **1. Classe** | `getMoltiplicatoreDannoSubito(tipoDanno)` (`PersonaggioBase`), che legge la tabella di `TipoPersonaggio` e `CategoriaPersonaggio`; `isImmuneATipoDanno` è lo stesso valore uguale a 0 | **immune** (0) o **debole** (>1: il `SACRO` sui non morti ×2); e le resistenze (tra 0 e 1: danno ridotto e meno effetti), per ora senza nessuna classe che ne abbia | niente: nessuna classe ha ancora una resistenza parziale, e la tabella è pronta |
| **2. Stato (momentanea)** | `TipoInterazioneConEffettiDiStato` (34 interazioni, quasi tutte coppie *stato, tipo di danno*, con il loro moltiplicatore) e `CalcolatoreCombattimento.interazioniAttive` (quando scattano) | moltiplicatori del danno, deboli (×1,5 il fulmine su un `BAGNATO`) o resistenti (×0,5 il fuoco su un `BAGNATO`), che durano quanto lo stato | la debolezza cambia da un turno all'altro: "debole al fulmine" vale finché dura il `BAGNATO` |
| **3. Difesa numerica** | `CalcolatoreCombattimento.difesaContro` | `COSTITUZIONE + PARATA` per il fisico, `RESISTENZA_MAGICA` per tutto il resto, più gli incantamenti dei pezzi | per un mostro tutti gli elementi hanno la stessa difesa |

**Com'era e com'è ora il punto 2.** Prima l'enum diceva solo *quali* coppie esistevano: il moltiplicatore (25 numeri) e *quando* un'interazione
scattava stavano in una catena di `if` dentro `calcolaDannoRisultante` (una quindicina di blocchi, mescolati agli effetti: togliere o
mettere uno stato, danno puro, critico, difesa), e per sapere quanto fa male un fulmine a un `BAGNATO` si poteva solo calcolare un colpo vero.
Dopo il riordino:

- `interazioniAttive(difensore, tipoDanno)` è **l'unico punto che decide quando scatta un'interazione con uno stato**: un ciclo
  sulle interazioni dell'enum, ognuna scatta se il difensore ha uno dei suoi stati (`getPrecondizioni`) e riceve uno dei suoi tipi di danno
  (`getInnescanti`). L'**ordine di dichiarazione dell'enum è l'ordine di applicazione** degli effetti (per stato, come nella vecchia catena);
  vuota se il difensore è immune;
- `moltiplicatoreDelleInterazioni` parte dal moltiplicatore della classe (`getMoltiplicatoreDannoSubito`) e lo moltiplica per quelli dell'enum (più la MIETITURA, che dipende dalla SAGGEZZA), e
  `fattoreDifesaDaStati` dà il cambio di difesa (guscio di ghiaccio ×1,5, superconduzione ×0,7);
- `calcolaDannoRisultante` chiama queste funzioni e **applica solo gli effetti** di ogni interazione (`switch`): così il danno vero e le tattiche
  non possono divergere;
- `moltiplicatoreAtteso(difensore, tipoDanno)` le compone con la difesa, senza dadi e senza cambiare niente.

Il comportamento del danno non è cambiato, con **una sola differenza voluta**: un difensore insieme SPAVENTATO e CONFUSO colpito da danno
`VUOTO` ora subisce la Follia Cosmica (prima no, perché per le interazioni contava solo lo SPAVENTATO). Il test `InterazioniConGliStatiCaratterizzazioneTest` confronta, a seme fisso, l'esito di 8316
colpi (3 difensori, ogni stato da solo e a coppie, ogni tipo di danno) con quello registrato prima del riordino; `MoltiplicatoreAttesoTest`
verifica la funzione nuova.

Altri due limiti, da rivedere insieme:

- **Nessuna resistenza parziale per classe.** Si è immuni, deboli ×2 o niente: un Troll non può essere "resistente al fuoco ×0,5" né
  un Golem "debole al contundente ×1,5" senza codice nuovo.
- **La distinzione fisico / non fisico è troppo grossa.** Sono solo due difese: `COSTITUZIONE + PARATA` e `RESISTENZA_MAGICA`. Fra il fuoco e
  il gelo, o fra il tagliente e il contundente, un mostro non ha differenze se non quelle dei punti 1 e 2.

**Fattore di danno atteso.** Si calcola, per un avversario e un tipo di danno T, **senza tirare i dadi**:

> fattore = 0 se immune a T; altrimenti `100 / (100 + difesa)` × il moltiplicatore di classe × i moltiplicatori degli stati attivi per T
> (la difesa è `difesaContro`, con gli stati che la cambiano) — **`CalcolatoreCombattimento.moltiplicatoreAtteso`, già fatto**.

Da lì: **debole** = moltiplicatore > 1, **resistente** = < 1, **immune** = 0, e "il più debole fra questi elementi" = il fattore più alto.
Serve a scegliere il bersaglio ("il più debole al fuoco"), l'incantesimo o l'arma ("il Gelo contro un `BAGNATO`", "un'arma sacra contro i
non morti": la condizione del cambio d'arma, §5) e il momento del cambio.

**Lavori, in ordine** (i primi tre sono preliminari e utili anche senza le tattiche, e sono fatti):

1. **Il moltiplicatore nell'enum** — **fatto.** Un campo `moltiplicatore` in `TipoInterazioneConEffettiDiStato` (il verso è nel valore: >1
   debolezza, <1 resistenza), al posto dei numeri nella catena; vale 1 per le interazioni che non cambiano il danno. Restano fuori solo la
   MIETITURA (dipende dalla SAGGEZZA del difensore) e i bonus che non sono un moltiplicatore (il danno puro della RISONANZA_SIGILLATA, la
   difesa del guscio di ghiaccio). Il test `MoltiplicatoriDelleInterazioniTest` fissa i valori.
2. **`moltiplicatoreAtteso(difensore, tipoDanno)`** — **fatto.** Estratto da `calcolaDannoRisultante` e **senza effetti collaterali** né dadi, con
   `interazioniAttive` come unico punto che decide quando un'interazione scatta (vedi sopra). Restano nel calcolo del danno gli effetti di
   ogni interazione (stati tolti o messi, danno puro, critico), che non sono moltiplicatori; i bonus degli *incantamenti* dell'arma
   (VAPORIZZAZIONE, MIETITURA) hanno ancora una versione ridotta a parte, in `calcolaDannoRisultante` (la debolezza di classe invece
   vale già anche per loro).
3. **`Arma.daTipoDanno(TipoDanno)`** — **fatto.** Dice se un'arma "dà" quel danno, cioè se è il suo `getTipoDanno()` o quello di uno dei suoi
   `getIncantamenti()` (`getTipoDannoElementale`). Non passa dai **modificatori** (`ModificatoreAttributoMD` cambia un attributo, non il
   tipo di danno). Serve al cambio d'arma (§5). Il test `ArmaDaTipoDannoTest` lo copre (anche con la Spada della Morte, NECROTICO per il suo
   incantamento).
4. **Debolezze e resistenze di classe come dato** — **fatto.** Il danno vero (`DANNO_VERO`, il `SACRO` sui non morti ×2) era l'unico caso
   scritto a mano di una cosa più generale, ed è stato generalizzato (e `DANNO_VERO` è sparito):

   - **Una tabella `tipo di danno → moltiplicatore`**, per categoria (`CategoriaPersonaggio`) e per classe (`TipoPersonaggio`); per un
     personaggio il moltiplicatore (`Personaggio.getMoltiplicatoreDannoSubito`) è il prodotto del valore della classe e di quelli delle sue
     categorie. Il significato dei valori è unico:

     | Valore | Significato |
     | :--- | :--- |
     | 0 | **immune** |
     | tra 0 e 1 | **resistente** |
     | 1 | niente |
     | maggiore di 1 | **debole** |

   - Le regole di prima sono righe: NON_MORTO = SACRO ×2 e VELENO ×0; i fantasmi (classe) ARIA e TERRA ×0; il Drago e la Chimera Drago
     FUOCO ×0; l'Idra TERRA, ACQUA e ARIA ×0; il Gargoyle ogni danno non fisico ×0. Una debolezza o resistenza nuova (il Troll resistente al
     fuoco, un Golem debole al contundente) è una riga in più, non codice. Sono spariti `haInterazioneCon` e gli override di
     `isImmuneATipoDanno` (ora vale `getMoltiplicatoreDannoSubito == 0`). `moltiplicatoreAtteso` e `calcolaDannoRisultante` leggono la
     tabella, anche per gli incantamenti dell'arma.
   - **Il messaggio all'utente**: la UI scrive sopra il bersaglio "Debole a Sacro" (verde), "Resiste a Fuoco" e "Immune a Aria" (rosso)
     (nuovo evento `NotificaReazioneDiClassePersonaggio`, testo e colore in `ui/TestoReazioneDiClasse`), a ogni colpo in cui il
     moltiplicatore non è 1. Prima
     per il danno vero non compariva niente: `ForestaUI.gestisciEventoInterazione` non elencava `DANNO_VERO` e il `default` lanciava
     un'`IllegalArgumentException` (registrata nel log da `BusEventi`, senza fermare il gioco).
   - **Gli altri assi restano a parte**: le immunità a un incantesimo (Morte non ha un tipo di danno) e a uno stato (Sanguinamento,
     Infettato...) non sono tipi di danno.

   **Le resistenze parziali e gli effetti ridotti.** *Immune* (0) vuol dire che quel tipo di danno non lo sente proprio: si salta **tutto**
   quello che riguarda il bersaglio (danno, stati nativi, interazioni), perché è un caso a parte (l'acqua scorre sopra a un immune
   all'acqua, quindi non può esserci elettrocuzione). *Resistente* (tra 0 e 1) vuol dire che regge bene quel danno: il danno si moltiplica
   per il valore, e anche gli **effetti** si riducono con lo stesso moltiplicatore, come **probabilità** (`CalcolatoreCombattimento.fattoreEffetti`:
   il moltiplicatore se è minore di 1, altrimenti 1; un debole ha più danno ma non più effetti):

   - gli **stati nativi** del tipo di danno (e quelli degli incantamenti, ognuno col fattore del suo elemento) e i **modificatori dell'arma
     sul bersaglio** scattano con la loro probabilità di sempre per il fattore;
   - ogni **reazione con uno stato** (`interazioniAttive`) scatta con probabilità pari al fattore, un tiro per reazione: se non scatta
     non ha né i suoi effetti né il suo moltiplicatore del danno;
   - la **durata** degli stati non cambia;
   - senza resistenze (il fattore vale 1) non si tira nessun dado in più, quindi il calcolo di oggi non cambia.

   Test: `RiduzioneDegliEffettiTest`. `moltiplicatoreAtteso` non tiene conto della probabilità ridotta delle reazioni (le conta come se
   scattassero).

5. **Una difesa per elemento** (o per famiglia di elementi) al posto delle due di oggi (`COSTITUZIONE + PARATA` e `RESISTENZA_MAGICA`): da decidere,
   vedi sotto.

**Da decidere**

- **Resistenze parziali per classe e debolezze nuove:** sì, con lo schema del lavoro 4 (una tabella per categoria e per classe, al posto degli
  override nelle classi); restano i punti sopra su effetti e testo.
- **Difesa per elemento:** si resta con fisico / non fisico, o si distingue per famiglia (tagliente, contundente, fuoco, gelo...)?
- **Che cosa vuol dire "debole":** solo moltiplicatore > 1, oppure anche "meno resistente degli altri elementi"?
- **Stati attivi adesso o anche futuri:** il selettore guarda solo gli stati di adesso, o anche quelli che l'azione stessa può causare (il
  Gelo che prima bagna e poi congela)?

## 7. Le condizioni

Gli elenchi dei giochi sono quelli che ricordo, **indicativi**: i nomi esatti cambiano da gioco a gioco. La colonna a destra dice se
la cosa si legge già in Foresta (**sì**), solo in parte o va derivata (**parziale**), richiede un dato nuovo (**da aggiungere**) o non
ha senso qui (**no**).

### 7.1 Su di sé

| Controllo | Giochi | In Foresta |
| :--- | :--- | :--- |
| Sempre (nessuna condizione) | tutti | **sì** |
| Salute sotto/sopra una percentuale | DA:O, FFXII (*Self: HP < n%*) | **sì**: `getSalute() / getSaluteMassima()` |
| Magia sotto/sopra una percentuale o un valore | DA:O (*mana/stamina*), FFXII (*MP*) | **sì**: `getMagia() / getMagiaMassima()` |
| Stanchezza oltre una soglia | (specifico: abbassa la probabilità di colpire) | **sì**: `getStanchezza()` |
| **Magia sufficiente per quell'incantesimo** | DA:O, FFXII (l'abilità non parte se mancano le risorse) | **sì**: `getCostoLancio()` contro `getMagia()`; è ciò che fa passare alla voce successiva |
| **Ha quell'incantesimo** a disposizione | DA:O/FFXII (la licenza, l'abilità) | **sì**: `puoFormulare(classe)` e `getIncantesimi(classe) > 0`, oppure l'innato (`DardoArcano.puoLanciarlo`, `AlbaSacra.puoLanciarlaInnata`, `CantoDelBardo.cantiUtili`) |
| Sotto un effetto di stato (silenziato, stordito, spaventato...) | DA:O, FFXII (*Self: status*) | **sì**: `hasEffettoDiStato(tipo)`; `SILENZIATO` impedisce gli incantesimi |
| Con un modificatore temporaneo attivo | DA:O (buff attivi) | **sì**: `getModificatoriTemporanei()` |
| Della classe X / di livello ≥ N | (selettori di gruppo) | **sì**: `getClasse()`, `getLivello()` |
| Con una certa arma | DA:O (*weapon set*) | **sì**: `getArmaEquipaggiata()`, `getArmaSecondaria()`; `Arma.daTipoDanno(tipo)` dice se "dà SACRO" (tipo di danno dell'arma o di uno dei suoi incantamenti). L'arma si può cambiare con l'azione del turno (§5) |
| In panchina / fuori combattimento | — | **sì**: `isInPanchina()`, `isFuoriCombattimento()` (il personaggio non agisce) |

### 7.2 Sul gruppo e sullo scontro

| Controllo | Giochi | In Foresta |
| :--- | :--- | :--- |
| Numero di avversari vivi ≥ N, o uno solo | DA:O (*enemies in range ≥ n*) | **sì**: `gruppoAvversario.getNumeroPersonaggiVivi()` |
| Numero di compagni vivi, o sono l'unico vivo | FFXII (*alone*) | **sì**: `getNumeroPersonaggiVivi()` |
| Ci sono avversari di una classe o categoria | DA:O (*any enemy of type...*) | **sì** per le classi e per le categorie (§6.1) |
| Il gruppo avversario è più numeroso (o meno) del mio | (specifico) | **sì**: serve a decidere, per esempio, se curarsi o attaccare |
| Il numero del turno (da N in poi, ogni N turni) | BG (*round time*), DA:O (cooldown) | **da aggiungere**: oggi non c'è un contatore del round |
| Una volta per scontro / non più di una volta | DA:O (cooldown), FFXII (*once*) | **da aggiungere**: un insieme "voci già usate in questo scontro" nello stato del combattimento |
| Duello, combattimento fino alla resa, ondata successiva | (specifico) | **sì**: sono flag del personaggio (`isFinoAllaResa`, `isSfidante`) e dello scontro (ondate) |

### 7.3 Risorse e contesto

| Controllo | Giochi | In Foresta |
| :--- | :--- | :--- |
| Ho una pozione (della Salute, della Magia, grande) | DA:O (*item*), FFXII (*item*) | **sì**: `getPozioniSalute()`, `getPozioniSaluteGrande()`, `getPozioniMagia()`, `getPozioniMagiaGrande()` |
| Quante pergamene di un incantesimo restano | (specifico) | **sì**: `getIncantesimi(classe)` |
| **Nell'inventario del gruppo c'è un'arma** che dà un tipo di danno (SACRO contro i non morti), con un certo modificatore, o più forte della mia | DA:O (*weapon set*) | **quasi tutto**: l'inventario del gruppo si legge (`getArtefatti()`), `Arma.daTipoDanno(tipo)` dice se un'arma dà quel danno e `puoEquipaggiare` se il personaggio la può impugnare; manca solo il filtro sull'inventario che li mette insieme (§8) |
| Con una probabilità del n% | BG (*RandomNum*) | **sì**: `Dado` |
| Tipo di locazione, ora del giorno | BG | **sì**: `getTipoLocazioneCorrente()`, `LineaTemporale`; utile solo per tattiche speciali |
| Variabili e contatori definiti dall'utente | BG (*script*) | **no**: va oltre lo scopo |

### 7.4 Modelli già pronti

Alcuni giochi non chiedono di scrivere le regole ma di scegliere uno **stile** (i *Tactics* di *Dragon Quest*: *combatti con giudizio,
niente pietà, risparmia magia*; i *Behavior* di DA:O: aggressivo, difensivo, passivo). In Foresta sarebbero **tattiche precompilate per
classe** (il Mago apre con il suo incantesimo più forte, il Sacerdote cura chi è sotto il 50%, il Guerriero attacca il più debole) che
il giocatore applica e poi ritocca: stesse voci, già scritte.

## 8. Che cosa manca nel modello dati

Quasi tutto è già leggibile (§6 e §7). Quello che manca:

- il **puntatore di rotazione** di ogni gruppo (chi ha agito per ultimo) e il **round** (§3): oggi il `Gruppo` non li ha, e
  `LocazioneBase` agisce su un solo `combattente`;
- uno **stato del combattimento**: il numero del round, l'ultimo bersaglio di ogni personaggio, le voci già usate;
- (il riconoscimento di un **boss** per ora non serve, §6.1; le **categorie di creatura** ci sono già);
- le **debolezze e resistenze di classe** come dato e la **difesa per elemento**, se si vorranno (§6.3, lavoro 4 e "Da decidere"): la composizione
  delle debolezze in un fattore senza effetti collaterali c'è già (`moltiplicatoreAtteso`);
- la **risposta** come azione a sé degli avversari (oggi è `rispondiInMischia` sul combattente, con la logica fissa di §4);
- le **tattiche base dei mostri**: un set per classe o per fascia di intelligenza, in un file di risorse, e il modo di assegnarlo a
  un avversario quando nasce (`FabbricaPersonaggi`);
- il **cambio d'arma** come azione sola e con un controllo preventivo (§5), **tra le prime cose da fare**: oggi il `GruppoGiocatore`
  fa il controllo e lo spostamento insieme, dentro il gestore dell'evento di prelievo (`suEventoRichiestaPrelievoArtefatto`:
  `puoEquipaggiare` e poi `removeArtefatto` / `addArtefatto`), e non sono disponibili in combattimento (`INVENTARIO` non è nei comandi di
  `IN_COMBATTIMENTO`). Va **spezzato in due**: un controllo (`puoEquipaggiare`, che c'è già sul personaggio, con in più la
  variante per il cambio che guarda la situazione senza l'arma che esce) e l'esecuzione (`equipaggia`, che sposta e basta, senza
  rifare il controllo), pubblici sul gruppo e usabili dalla tattica; le schermate dell'inventario usano gli stessi due passi. Serve poi
  il filtro sull'inventario del gruppo che cerca un'arma che dà un tipo di danno (`Arma.daTipoDanno`, già fatto) e che il personaggio
  può impugnare, e, nello stato del combattimento, il **contatore dei cambi** di ogni personaggio e **l'elenco delle armi già valutate** (utilizzabili o no);
- un **criterio di scelta** dell'incantesimo (a caso, il più potente, il più conveniente) come parametro dell'azione, e la
  condizione **"probabilità del n%"** (oggi "una volta su tre non lancia" è una costante).

Non ha senso importare: posizione e distanza, mischia contro distanza, linea di vista, incantesimi con tempo di lancio, movimento.

## 9. Osservazioni

- Il sistema è **di sola lettura sul modello**: le condizioni e i selettori chiedono lo stato attuale e non lo cambiano; le azioni sono
  quelle già eseguibili oggi, scelte dal motore al posto del giocatore. Le regole del danno non cambiano.
- Oggi la scelta del **bersaglio** non esiste (si attacca sempre il primo avversario vivo): le tattiche la introducono per tutte le
  azioni singole.
- Gli incantesimi e le pozioni sono **del gruppo** (scorta condivisa): due personaggi con la voce "lancia Fuoco" consumano la stessa
  scorta. È normale, ma la scheda dovrebbe far vedere che cosa resta.
- Il round-robin cambia il **bilanciamento**: oggi un compagno solo contro più avversari ne subisce uno in più al massimo ("si
  disimpegna e attacca"); nello schema nuovo li subisce tutti, e un gruppo numeroso contro un avversario solo gli dà più azioni. Va
  riverificato con le simulazioni (`SimulazionePartiteTest`, i test di bilanciamento) e con i test di scenario che contano i round.
- I test che oggi fanno combattere un solo personaggio andranno riscritti: con la prima fase (attacchi singoli, §3.1) restano
  quasi tutti validi; si riscrivono con il round a coppie (§3.2).
- Le tattiche dei mostri rendono parametrizzabile ciò che oggi è sparso in `PersonaggioBase` (§4): resta il rischio di cambiare il
  comportamento dei mostri senza accorgersene, da coprire con un test sulle decisioni di oggi.

## 10. Da fare / idee aperte

**Decisioni prese:** il bersaglio è un campo della voce, per i miei personaggi e per i mostri (§4); le azioni manuali le fa il
personaggio scelto dal giocatore e il puntatore passa al successivo; a fine round per la caduta dell'ultimo avversario lo scontro è
vinto, e se cade un compagno che doveva ancora agire si passa al successivo; si comincia dagli attacchi singoli (§3.1), da rivedere
con il round a coppie (§3.2); fuga, corruzione, amicizia e passare inosservati restano fuori.

Domande per il piano:

- **Le tattiche base dei mostri** (§4): per classe o per fascia di intelligenza? In un file di risorse per classe, con quale formato
  (lo stesso delle tattiche del giocatore)? E se qualche boss ha la sua, scritta a mano.
- **Quanto devono poter cambiare** le tattiche dei mostri nel corso del gioco (per livello del mondo, per luogo, per boss) rispetto a
  quelle di partenza.
- **I selettori di forza**: quali misure di "più forte/più debole" offrire nella prima versione (livello, salute attuale, salute
  massima).
- **Quante voci** per personaggio, e se il numero cresce con il livello o con un'abilità, come in DA:O.
- **Il formato** salvabile (testo semplice nel salvataggio e in un file di modelli per classe) e la **scheda** secondaria del
  personaggio (elenco ordinabile con aggiungi, togli, sposta su e giù, attiva/disattiva).
- **Livello di dettaglio dei controlli** per la prima versione (probabilmente: sempre, salute e magia sotto una soglia, stati, classe,
  numero di avversari, risorse, probabilità; poi le categorie di creatura, già nei dati).
- **Cambiare arma** (§5): quale arma esce con la doppia arma, cioè quale è "la meno conveniente da usare in quel momento" (danni, effetti di stato, modificatori; vedi sopra)? Per quanto tempo valgono le valutazioni già fatte delle armi del gruppo? Le voci per tornare all'arma di prima le scrive il giocatore o le propone il sistema in coppia con la prima?
- **Il round a coppie** (§3.2): quando rivederlo, e se i personaggi in più possono agire senza risposta anche dopo il primo giro.
- Il **registro del turno**: dire in una riga perché il personaggio ha scelto quell'azione ("Mira lancia Gelo su Strega: la sola
  maga"), utile al giocatore per mettere a punto le tattiche, e ai test sulle tattiche dei mostri.
