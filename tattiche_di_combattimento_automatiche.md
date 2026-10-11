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

- il **bersaglio**: chi colpire o curare (un selettore: "un avversario magico", "il capo avversario", "con meno salute", "l'alleato più
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

> **Requisito fondamentale: le tattiche si salvano e si rileggono con il personaggio.** Il giocatore le personalizza per ognuno dei suoi
> personaggi, e questa personalizzazione **è parte del personaggio**: finisce nel salvataggio con lui e, caricando la partita, ogni
> personaggio agisce esattamente come era stato impostato, senza perdere una voce o un blocco. Lo stesso vale per la tattica diversa che una singola
> istanza di un mostro può portare (§4). Ne segue come si costruisce tutto il resto (§7.6 per il formato, §8 per il modello dati).

> **Prerequisito: le tattiche non cambiano il modello dati, scelgono soltanto.** Oggi il giocatore sceglie per conto proprio e impartisce un comando;
> è l'**automa** (`Automa`, la locazione) a eseguire l'azione e a cambiare il modello. Con le tattiche **chi sceglie al posto del giocatore** è la
> tattica, ma quello che fa è lo stesso: **impartire il comando che avrebbe impartito il giocatore**, e l'automa lo esegue come sempre. Una tattica quindi
> *legge* lo stato (condizioni e selettori chiedono lo stato attuale, senza cambiarlo) e *produce un comando*; non toglie salute, non consuma pergamene,
> non sposta oggetti, non aggiunge stati. Le azioni sono quelle già eseguibili oggi (§5) e le regole del danno non cambiano. Vale per i personaggi del giocatore
> e per i mostri, e vale anche per le azioni a più passi: il cambio d'arma (§5) fa le due richieste di stoccaggio e di prelievo come le farebbe il giocatore
> a mano.

L'elenco deve essere salvabile e ripristinabile anche come **modello** da riapplicare ad altri personaggi.

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
- **Gli ospiti vulnerabili sono bersaglio solo in due casi** (un limite del combattimento di oggi): la risposta avversaria colpisce il personaggio che ha appena agito
  (`rispostaAvversaria`) o il combattente scelto (`rispondiInMischia`), non uno a caso; si arriva a `PersonaggioBase.attacca(Gruppo)`, dove la scelta a caso
  comprende gli ospiti vulnerabili vivi, solo quando il gruppo avversario è più numeroso ("si disimpegna e attacca") o quando chi ha agito è già caduto. Con le tattiche la
  risposta sceglie il bersaglio con la tattica dell'avversario (§3, §4), e la scelta a caso li comprende sempre.

## 3. Il round-robin dei due gruppi

Il turno a rotazione vale per **tutti e due i gruppi**, e va tracciato in questo piano perché le tattiche ne dipendono.

**Il puntatore.** Ogni gruppo ricorda **chi ha agito per ultimo** e passa la mano al **successivo personaggio vivo** (saltando chi è
caduto o in panchina). Il puntatore di ogni gruppo avanza **sempre**, a ogni azione, e non si azzera fra uno scontro e l'altro dentro la
stessa locazione. **Fatto** (`StatoDelCombattimento.getProssimoAttaccante`, §3.3): il puntatore sta nello stato e non più nel `Gruppo`; la risposta avversaria (`rispostaAvversaria`) e la risposta nella mischia (`eseguiSingoloAttacco`)
lo usano per far rispondere il prossimo avversario vivo, saltando il turno a chi è incapace di agire (atterrato, congelato, stordito). Il gruppo del giocatore ha il suo puntatore, ma non lo usa ancora: è il giocatore a scegliere chi agisce.

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

### 3.3 Il round e lo stato del combattimento

**Che cos'è un round** (deciso): finisce quando **tutti gli avversari vivi hanno fatto la propria mossa**. Così il round arriva sempre a una fine, anche se il
giocatore continua con azioni manuali facendo agire sempre lo stesso personaggio, e i mostri ruotano comunque in round-robin (una risposta per ogni azione del giocatore). Chi ha
agito nel round si registra per entrambi i gruppi (`haAgitoNelRound`), ma per chiudere il round contano solo gli avversari; se non ce ne sono più il combattimento è
finito e il round non avanza. Il numero del round **riparte da 1 a ogni inizio locazione** e **continua** quando arriva la prossima ondata: il combattimento e la locazione non
sono cambiati, sono solo apparsi nuovi mostri (che non hanno ancora agito: il round si allunga). All'inizio di ogni
round il motore pubblica un evento **`InternoInizioRound`** (con il numero), per la UI, i log e chi tiene la memoria del combattimento.

**Lo `StatoDelCombattimento`** (deciso nel nome; **fatti**: il round con `InternoInizioRound`, chi ha agito nel round, i puntatori dei due gruppi e il round-robin della risposta; il resto arriva con le tattiche): un oggetto che raccoglie in un posto solo tutto ciò che le tattiche devono ricordare di un combattimento. Si crea
(o si azzera) a ogni inizio locazione, insieme alla preparazione del gruppo avversario. Contiene:

- **del combattimento**: il numero del round, il registro di chi ha già agito nel round corrente e, per **ogni gruppo**, il **puntatore** all'ultimo personaggio che ha agito
  (§3: passa al successivo vivo, saltando i caduti e la panchina). Il puntatore esce dal `Gruppo` (dove è un indice, `ultimoIndiceSelezionato`) per stare qui: è memoria
  del combattimento e non modello, e vive come lo stato (azzerato a inizio locazione, non fra uno scontro e l'altro nella stessa locazione). Meglio che sia
  il **personaggio** e non l'indice, perché la lista del gruppo cambia (un compagno cade, esce, un nuovo mostro arriva) e un indice slitterebbe;
- **per ogni personaggio** (una scheda, indicizzata per identità del personaggio): l'ultimo avversario e l'ultimo alleato scelti (F8); chi lo ha attaccato per
  ultimo (F9); se ha già lanciato un incantesimo (la regola del "magico", F3); quanti cambi d'arma ha fatto; le armi del gruppo già valutate; le voci già
  usate; e, volendo, le statistiche (danni inflitti e subiti, incantesimi lanciati, colpi a segno e mancati);
- **la conoscenza accumulata sulle reazioni** (S4): per ogni coppia (personaggio colpito, tipo di danno), quanto il danno vero è stato diverso da quello previsto,
  che fa scoprire le resistenze date dall'equipaggiamento.

Regole (decise):

- **Non si salva**: un combattimento non si interrompe mai per salvare (il comando non è disponibile), quindi lo stato è transitorio e si perde con il
  combattimento. Le tattiche di ogni personaggio, invece, sì (§1).
- **La conoscenza è pubblica**: vale per i personaggi del giocatore e per i mostri, e quello che si è imparato sul comportamento di un personaggio lo sa chiunque, in
  entrambi i gruppi.
- **La scheda di un personaggio non si dimentica quando cade**: un compagno potrebbe farlo risorgere, e la conoscenza sulle sue reazioni deve valere ancora. Si
  azzera solo con l'inizio della locazione; i mostri di una nuova ondata partono con una scheda vuota.
- **Non è il modello dati**: è la memoria tattica. La aggiornano il motore e la locazione (o l'oggetto stesso, ascoltando gli eventi come `InternoInizioRound`);
  le tattiche la *leggono* e basta, come vuole il prerequisito del §1.

**Una conseguenza:** con più compagni che avversari il round è più corto di un giro completo del gruppo del giocatore (un avversario solo: un round a ogni risposta). Le condizioni sul round e i
cooldown dei mostri contano quindi "giri degli avversari", e arrivano a una fine anche in manuale.

## 4. Le tattiche degli avversari

**Lo stesso modello.** Ogni avversario ha una tattica fatta di voci (bersaglio, condizione, azione), valutata come quella dei compagni:
la prima voce eseguibile vince. Le azioni sono quelle del suo repertorio (attacco con l'arma o l'arma naturale, i suoi incantesimi
malefici); i selettori sono gli stessi di §6, letti dal punto di vista dell'avversario (per lui i "nemici" sono i miei personaggi).

**Che cosa c'è già, nel codice.** Una versione molto semplice, che le tattiche renderebbero esplicita e parametrizzabile:

| Decisione | Oggi (`PersonaggioBase`) | Come voce di tattica |
| :--- | :--- | :--- |
| **Chi colpire** (`attacca(Gruppo)`) | il **Mago o la Maga più potenti** (livello più alto, a parità il primo); poi il **Sacerdote o la Sacerdotessa più potenti**; poi l'**Elfo o l'Elfa più potenti**; poi il **Bardo o la Cantastorie più potenti**; altrimenti **a caso** fra i personaggi vivi e gli ospiti vulnerabili vivi, tutti con la stessa probabilità (`PersonaggioBase.scegliBersaglio`) | un selettore: "un avversario magico", "il più debole", "il primo", "a caso" |
| **Se lanciare un incantesimo** (`scegliIncantesimoContro`) | no se non è magico (`isMagico`), è `SILENZIATO`, ha 0 `MAGIA` o non può pagarne nessuno; poi **una volta su tre non lancia** | le condizioni "magia sufficiente" e "probabilità del n%" |
| **Quale incantesimo** | `INTELLIGENZA` < 5: **a caso**; < 7: **il più potente**; da 7: **il più conveniente** fra incantesimo e attacco fisico, confrontando probabilità di colpire × danno atteso | un **criterio di scelta** dell'azione: a caso, il più potente, il più conveniente |
| **Magia o attacco fisico** | solo per `INTELLIGENZA` ≥ 7, con quel confronto | idem: l'ultima voce è comunque l'attacco fisico |

**Mostri stupidi e mostri intelligenti.** Un mostro stupido colpisce il primo che trova; uno intelligente quello con meno salute, per ridurre gli
attacchi del giro dopo; uno ancora più furbo cerca di mettere a tacere un mago. Ogni mostro ha le sue **tattiche base**, **per classe**
(`TipoPersonaggio`) **e per fascia di intelligenza insieme**: la lista è una sola per classe, e le fasce sono dei blocchi `SE la mia astuzia
è maggiore di ... ALLORA ... ALTRIMENTI ... FINE` (§7.6). Per esempio:

- *bruto* (Minotauro, Goblin, Scheletro, Troll...): una voce o poche, "scelgo il primo, attacco fisico, punto";
- *astuto* (Arpia, Hobgoblin, Chimera...): in più "se c'è un magico ne scelgo uno a caso", e un incantesimo a caso se ha la `MAGIA`;
- *stratega* (Strega, Lich, Drago): "se la mia astuzia è almeno 31 silenzio subito il magico con più `MAGIA`", poi il bersaglio quello con meno
  salute e l'incantesimo più adatto.

Le tattiche base sono dati (un file di risorse per classe, nello stesso formato di quelle del giocatore: il motore che le esegue è lo
stesso), non codice: il comportamento di oggi si riproduce esprimendolo come tattica base, e si controlla con un test che le decisioni
siano le stesse. **I boss hanno la loro**, perché le classi sono già distinte (Strega, Lich, Drago, Minotauro Gigante sono ciascuno un
`TipoPersonaggio`).

**La tattica generale è quella di classe**, e non cambia per livello del mondo o per luogo: l'astuzia (intelligenza di classe con una rampa sul livello) e le
condizioni dentro la lista fanno già variare il comportamento, e i boss hanno la loro intelligenza. Una **singola istanza** può però portarne una
diversa, per esempio in una missione un goblin più furbo degli altri con una tattica tutta sua; la tattica dell'istanza si salva con la partita (§1, §8).

**L'astuzia e le soglie** (decise). Le soglie dei quattro livelli sono: 1 = da 0 a 11, 2 = da 12 a 19, 3 = da 20 a 30, 4 = da 31. Il **Folletto** si alza
sopra il Goblin (a 8, fra il Goblin e l'Hobgoblin) e l'**Ombra Nera** passa da 30 a 31, per entrare nel quarto livello ed essere più competitiva (sono due
ritocchi a `*_MAX_INTELLIGENZA` in `Costanti`, da fare insieme alle liste: la costante serve anche a calcolare l'INTELLIGENZA vera dei due, quindi li cambia
anche come lanciatori di incantesimi).

L'**astuzia** parte dal **massimo di classe** (`*_MAX_INTELLIGENZA`) ed è ridotta, per i mostri "tonti" di partenza, da una **rampa sul livello**: l'Ombra Nera
dev'essere tonta all'inizio e diventare più ostica col salire del mondo. Il massimo di classe non è un tetto dell'INTELLIGENZA vera: è un valore base che la
formula del livello fa crescere (`getAttributoAdeguatoALivello`) e che la distribuzione casuale dei punti sposta, quindi l'attuale può essere molto sotto (Mago:
55 contro 18 a livello 1) o sopra (Goblin: 4 contro 6–11).

**Il campo da gioco è il livello 1–9**: difficilmente si arriva al 10, quindi i confronti qui sotto sono sui livelli 1, 3, 5, 7 e 9. Tre formule considerate (**scelta la B**) e i
livelli di astuzia che ne escono:

- **A, dall'INTELLIGENZA attuale:** `astuzia = massimo − k × max(0, massimo − INTELLIGENZA attuale)`. L'INTELLIGENZA cresce molto poco fra i livelli 1 e 9
  (Ombra Nera: 6, 7, 7, 7, 8), quindi **l'astuzia quasi non si muove** (con k = 0,8 l'Ombra Nera resta nel primo livello fino all'ultimo, il Mago passa dal 3 al 4 al 7),
  e anche i mostri con un massimo vicino all'INTELLIGENZA (l'Hobgoblin, massimo 12) sono tonti da subito.
- **B, una rampa sul livello del mondo (scelta):** `astuzia = massimo × min(1, 0,4 + 0,075 × (livello − 1))`: il 40% del massimo al livello 1, pieno al livello 9.
  L'Ombra Nera passa da 12 a 31 (livelli 2, 2, 3, 3, 4), il Mago da 22 a 55, la Strega da 26 a 65 (dal livello 3 è strateghessa).
- **C, nessun malus:** `astuzia = massimo`. Ogni classe ha il suo livello fisso; l'Ombra Nera a 31 è subito del quarto livello. È la più semplice, ma niente
  "tonta all'inizio".

Livello di astuzia (1–4) ai livelli 1 / 3 / 5 / 7 / 9:

| Classe (massimo) | **A** (k = 0,8) | **B** (rampa) | **C** (solo il massimo) |
| :--- | :--- | :--- | :--- |
| Ombra Nera (31) | 1 / 1 / 1 / 1 / 2 | 2 / 2 / 3 / 3 / 4 | 4 |
| Goblin (4), Folletto (8) | 1 | 1 | 1 |
| Hobgoblin (12) | 1 | 1 / 1 / 1 / 1 / 2 | 2 |
| Guerriero (15) | 1 | 1 / 1 / 1 / 2 / 2 | 2 |
| Idra (25) | 2 / 1 / 2 / 2 / 2 | 1 / 2 / 2 / 3 / 3 | 3 |
| Ladro (30) | 2 | 2 / 2 / 3 / 3 / 3 | 3 |
| Mago (55) | 3 / 3 / 3 / 4 / 4 | 3 / 4 / 4 / 4 / 4 | 4 |
| Strega (65), Lich (70) | 4 | 3 / 4 / 4 / 4 / 4 | 4 |
| Drago (80) | 4 | 4 | 4 |

Con **B** gli avversari "salgono" davvero insieme al mondo in questo campo da gioco, ma i boss al livello 1 sono un po' meno furbi (Strega e Lich al terzo livello);
con **A** la classe decide quasi tutto. **Si usa la B**: l'astuzia è `massimo × min(1, 0,4 + 0,075 × (livello − 1))`, con il livello del personaggio (che nei
mostri è quello del mondo), la partenza al 40% (`ASTUZIA_PARTENZA`) e il passo di 0,075 per livello (`ASTUZIA_PER_LIVELLO`) come costanti in `Costanti`, da regolare.

La tabella dell'INTELLIGENZA attuale (media a livello 1 / 5 / 10 / 20, oltre il campo da gioco ma utile per i valori di base):

| Classe | Massimo di classe | INTELLIGENZA attuale, media a livello 1 / 5 / 10 / 20 |
| :--- | :--- | :--- |
| Guerriero, Guerriera | 15 | 7 / 8 / 10 / 13 |
| Ladro, Ladra | 30 | 8 / 9 / 11 / 16 |
| Bardo, Cantastorie | 35 | 9 / 12 / 14 / 20 |
| Sacerdote, Sacerdotessa | 40 | 11 / 14 / 19 / 25 |
| Elfo, Elfa | 45 | 9 / 12 / 15 / 20 |
| Mago, Maga | 55 | 18 / 22 / 27 / 39 |
| Goblin | 4 | 6 / 7 / 8 / 11 |
| Folletto | 3 (da alzare, deve essere più furbo del Goblin) | 6 / 8 / 10 / 15 |
| Troll | 10 | 5 / 6 / 7 / 9 |
| Fantasma | 15 | 6 / 8 / 10 / 15 |
| Ombra Nera | 30 (da portare a 31) | 6 / 7 / 8 / 11 |
| Idra | 25 | 9 / 9 / 10 / 13 |
| Drago | 80 | 19 / 22 / 26 / 34 |
| Strega | 65 | 29 / 33 / 40 / 52 |
| Lich | 70 | 33 / 38 / 47 / 61 |


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

**Il criterio di scelta dell'incantesimo** (deciso; i quattro livelli S1–S4 sono nel §7.5)

- **"Il più potente"** (criterio di scelta dell'incantesimo) — l'incantesimo che farebbe **più danni** a un personaggio, stimati con un **danno atteso**
  (una media, senza dadi), e **dipende dall'intelligenza di chi sceglie**, in quattro livelli:
  1. **poco intelligente**: un incantesimo **a caso**;
  2. **mediamente furbo**: quello che potrebbe fare più danni, **senza** tener conto delle resistenze e delle debolezze del bersaglio e di
     eventuali modificatori di armi e armature;
  3. **furbo**: quello più adatto a fare più danni **a quel personaggio**, con le resistenze e le debolezze di classe (il conto di
     `moltiplicatoreAtteso`);
  4. **molto intelligente**: come il furbo, e in più **impara** dai lanci: se un incantesimo ha avuto un effetto diverso dal previsto contro un
     personaggio equipaggiato con oggetti che aumentano (o riducono) le sue resistenze, se ne ricorda e ne tiene conto (lo scopre solo dopo il
     primo lancio, quindi va ricordato per personaggio e tipo di danno).
- **Incantesimi a più bersagli** (`MULTIPLO`): per i mostri poco intelligenti e mediamente furbi conta il **danno massimo a un bersaglio** (il
  resto è danno collaterale); per i furbi conta il **danno totale**; per i molto intelligenti si guarda anche **chi verrebbe portato vicino alla
  fine** (o ucciso). Esempio: un incantesimo che lascia un personaggio a 50 punti e altri due a 35 è peggio di uno che lo lascia a 60 e gli altri
  due a 15, perché con un secondo lancio si finiscono probabilmente gli altri due. (Una misura possibile: per ogni bersaglio, la quota della salute
  attuale che l'incantesimo toglie, al massimo 1 se lo uccide, sommata; da confermare.)
- **Quali incantesimi partecipano**: dipende da cosa ha il gruppo. Se un gruppo ha Resurrezione, Alba Sacra o Silenzio li usa, ma sono **voci
  proprie** della tattica, non partecipano al "più potente" fra gli incantesimi che fanno danno. Il **Silenzio** va usato **subito** su maghi,
  sacerdoti, elfi e bardi (i personaggi magici di cui sopra) quando c'è.
- **Il costo**: si considerano **solo gli incantesimi che si possono lanciare in quel momento** (`MAGIA` sufficiente, non silenziato). I mostri non
  recuperano `MAGIA` **a meno che non siano umanoidi e abbiano nel loro gruppo qualche pozione della Magia a disposizione**; un personaggio può bere una
  pozione della Magia (una voce a sé, "se la `MAGIA` è sotto la soglia") e il turno dopo avere a disposizione quelli più costosi. La stessa
  considerazione vale per le **pozioni della Salute** e per gli **incantesimi curativi** (Alba Sacra, Resurrezione, canto curativo): oggi gli avversari non li usano
  perché non hanno nessuna scorta (né pozioni né pergamene) e il loro repertorio comprende solo incantesimi **malefici**, pagati soltanto con la loro `MAGIA`; il
  difetto è nel **modello dati** (§8).

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
| **Con meno salute** / **con più salute** | DA:O, FFXII (*lowest / highest HP*) | **sì**: per salute attuale (`getSalute()`), più bassa o più alta; vedi "Le misure" sotto |
| **Di livello più basso** / **più alto** | DA:O, FFXII (*level*) | **sì**: per livello (`getLivello()`) |
| **Più debole** / **più resistente** a un tipo di attacco `<tipo di danno>` | DA:O (*vulnerability*), FFXII (*weak to element*) | **sì**, con `CalcolatoreCombattimento.moltiplicatoreAtteso` (§6.3): il fattore più alto o più basso fra i candidati |
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

**Le misure: "più forte" e "più debole" non si usano.** Sono parole ambigue (forte per che cosa?): ogni voce dice la misura. Sono sei
selettori di ordinamento, che scelgono, fra i candidati, quello che ha il valore più alto o più basso:

| Selettore | Misura | Note |
| :--- | :--- | :--- |
| **con meno salute** / **con più salute** | la salute attuale (`getSalute()`), in punti | non la percentuale: un Troll con 40 punti ha "più salute" di un Goblin con 15, anche se il Goblin è intero |
| **di livello più basso** / **di livello più alto** | il livello (`getLivello()`) | |
| **più debole a** `<tipo di danno>` | il fattore di danno atteso contro di lui per quel tipo (`moltiplicatoreAtteso`) più **alto**: debolezza di classe, reazioni con i suoi stati e difesa | un immune ha 0 |
| **più resistente a** `<tipo di danno>` | lo stesso fattore più **basso** | un immune è il più resistente; ha senso soprattutto per gli **alleati** ("chi regge meglio il fuoco del Drago"), e per scartare chi non vale la pena colpire con quel danno |

I selettori che *filtrano* invece di ordinare sono altri: "di livello più alto **del mio**" (un confronto con chi sceglie), "salute
sotto il n%" (una soglia), "**debole a** `<tipo>`" (moltiplicatore di classe e stati maggiore di 1, senza contare la difesa).

**Regole comuni dei selettori** (decise)

- **I selettori di ordinamento** sono: salute attuale (più bassa o più alta), livello (più basso o più alto), più debole o più
  resistente a un tipo di danno. **Niente** percentuale di salute né salute massima: la salute assoluta basta (chi ha meno punti ha più
  probabilità di morire al prossimo scontro). Per un alleato "il più ferito" vuol dire proprio "con meno salute".
- **A parità di valore** si sceglie il **primo dell'elenco** del gruppo (ripetibile, anche nei test); "a caso" è un selettore a parte.
  *"Il primo"* è la posizione nell'elenco dei vivi, che cambia quando qualcuno cade o va in panchina.
- **"Lo stesso del turno precedente"**: è il bersaglio che *questo* personaggio ha scelto la volta prima, se è ancora vivo e in
  combattimento e l'azione ha ancora senso su di lui (non è diventato immune, non è già silenziato per il Silenzio...); altrimenti la voce non
  c'è e si passa alla successiva. Un nemico che ha attaccato il terzo personaggio continua con lui finché è vivo: non cambia bersaglio solo perché un
  altro è diventato "più debole", il che darebbe al mago il tempo di riprendersi e lanciare. Si ricordano **due bersagli**, l'ultimo avversario e
  l'ultimo alleato (un turno di cura non deve far dimenticare chi si stava colpendo). *"Quello che attacca me o un alleato"* resta un'altra
  cosa: l'avversario che ha agito contro di noi per ultimo.
- **"Magico"**: di un personaggio non si conosce la lista degli incantesimi del gruppo
  (le pergamene), ma si conoscono le **abilità innate della classe**: Dardo arcano (Mago, Maga, Elfo, Elfa), Alba Sacra (Sacerdote,
  Sacerdotessa), canti (Bardo, Cantastorie). Quindi:
  1. **Prima** si cercano i personaggi di una classe con un'abilità innata. Sono magici, e vengono per primi, se **non sono `SILENZIATO`** e
     hanno la `MAGIA` per almeno una delle loro abilità innate; se non l'hanno, o sono già silenziati, si scartano.
  2. **Per i mostri**, fin da subito: una classe di mostro per cui `isMagico()` è vero (`getLivellamentoMagia() > 0`: Strega, Lich, Drago, ma anche
     Idra, Folletto, Fantasma...) conta come magica anche se non ha ancora lanciato nulla, perché si sa che usa la magia (non è silenziata e ha
     `MAGIA` > 0). Non lo sono Goblin, Hobgoblin, Scheletro, Troll, Gigante, Titano, Minotauro e Minotauro Gigante, che non lanciano incantesimi.
     (`isMagico()` è vero anche per tutti i giocanti e i non combattenti, perché hanno una riserva di `MAGIA`: per i *personaggi del giocatore* conta
     quindi il punto 1, non questo.)
  3. **Poi** gli altri personaggi: uno senza abilità innate è magico solo se ha `MAGIA` maggiore di 0, non è silenziato e **ha già lanciato un
     incantesimo in questo combattimento** (in teoria ogni personaggio con una pergamena e abbastanza `MAGIA` lo è, ma non si sa finché non si vede).
  Serve ricordare per ogni personaggio, nello stato del combattimento, se ha lanciato un incantesimo.
  Un mago senza abbastanza `MAGIA` neanche per un Dardo arcano si può saltare; appena ne riacquista abbastanza diventa subito un bersaglio per
  un Silenzio o un attacco. In ogni caso **i potenziali personaggi magici si preferiscono sempre**: meglio prevenire che curare.
  Il **comportamento è lo stesso per i due gruppi**: i personaggi del giocatore si comportano come quelli avversari, e la differenza è solo
  che le condizioni le personalizza il giocatore.

### 6.2 Selettori per gli alleati (per cure, resurrezioni, pozioni)

| Selettore | Giochi | In Foresta |
| :--- | :--- | :--- |
| Se stesso | DA:O (*Self*) | **sì** |
| **Il capo** del gruppo (se cade si perde) | (specifico: `GIOCO_PERSO`) | **sì**: `gruppo.getCapo()` |
| L'alleato **con meno salute** (il "più ferito", per salute attuale) / con salute sotto una soglia | DA:O (*Ally: health < n%*), FFXII | **sì**: sui personaggi vivi |
| Un alleato **caduto** (per la resurrezione) | DA:O (*dead*), FFXII (*KO'd*) | **sì**: `!isVivo()`; il gruppo dà i vivi, i caduti si ricavano dall'elenco |
| Un alleato con un certo **stato** (avvelenato, confuso...) | DA:O, FFXII | **sì**: `hasEffettoDiStato` |
| Un alleato con **poca magia** (per la pozione della Magia) | FFXII (*MP < n%*) | **sì**: `getMagia() / getMagiaMassima()` |
| Un alleato di una certa **classe** | DA:O | **sì**: `getClasse()` |
| Un **ospite vulnerabile** da proteggere | (specifico: missioni di scorta) | **sì, come alleato da proteggere**: un ospite vulnerabile vivo (`GruppoGiocatore.getOspitiBersaglio`) è fra i bersagli *possibili* del suo gruppo, e può esserlo di una cura o di una protezione (da decidere quali selettori lo comprendono). Un ospite non vulnerabile non lo è mai. Non combatte e non agisce, quindi non ha una tattica |

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
   - **La tabella** (le voci per classe e categoria, con le ragioni) è in `debolezze_e_resistenze.md`; il colore del testo ha dieci
     sfumature per parte (dal grigio verso il verde per la debolezza, verso il rosso per la resistenza, rosso acceso per l'immunità).
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
| Il **numero del round** (è il round `n`, da `n` in poi, ogni `n` round) | BG (*round time*), DA:O (cooldown) | **da aggiungere**: il contatore è nello `StatoDelCombattimento` (§3.3), con l'evento `InternoInizioRound` |
| **Una volta per scontro** / non più di `n` volte / non prima di `n` round dall'ultima | DA:O (cooldown), FFXII (*once*) | **da aggiungere**: le "voci già usate" (e in che round) nello `StatoDelCombattimento`; vedi sotto a che cosa serve e a che cosa **no** |
| Duello, combattimento fino alla resa, ondata successiva | (specifico) | **sì**: sono flag del personaggio (`isFinoAllaResa`, `isSfidante`) e dello scontro (ondate) |

**A che cosa serve "una volta per scontro" (e a che cosa no).** Con le azioni di oggi i casi veri sono pochi:

- **Il cambio d'arma** (§5): al massimo uno, al più due per personaggio. È un contatore per personaggio, il caso più chiaro.
- **Le abilità forti dei mostri, che oggi non esistono** e che questa condizione prepara: il fiato di fuoco del Drago, l'urlo di una banshee, una morte che si ritorce
  contro chi la lancia. Vanno usate "una volta per scontro" oppure "non prima di `n` round dall'ultima" (un *cooldown*, come in DA:O), con il round dell'ultimo uso.
- **Una mossa d'apertura** ("a inizio scontro"): più semplice come condizione sul numero del round (`round = 1`), senza un contatore.

**Non** serve, e non va usato, per le azioni che si possono già controllare con una condizione sullo stato, che dicono meglio *perché* non ripeterle:

- il **Canto di valore** non è "una volta per scontro" ma "se nessun compagno è `ISPIRATO`" (dura qualche turno, e a scadenza può avere senso ripeterlo);
- l'**Alba Sacra** innata del Sacerdote non è "una volta" ma "se un compagno ha uno stato negativo o un modificatore temporaneo da togliere" (costa 5 `MAGIA`);
- il **Silenzio** è "se il bersaglio non è `SILENZIATO`", il **canto ipnotico** e l'**Urlo della Banshee** "se il bersaglio non è `SPAVENTATO` o `CONFUSO`";
- una **Resurrezione** è "se c'è un compagno caduto".

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

### 7.5 Il catalogo: le parole delle voci

Riassunto con sigle di tutto ciò che una voce può dire (i dettagli sono in §5, §6 e §7). Una **voce** è: *[bersaglio] + [condizioni] + [azione
(con un criterio)]*; il personaggio esegue la **prima voce che si può davvero eseguire**. Le voci per personaggio sono **illimitate** (nessun tetto). Ogni mostro ha la sua lista di voci: un bruto
(il Minotauro) una molto semplice, uno più intelligente ne ha di più, con le domande giuste. **L'astuzia stessa può essere una condizione
della voce** (`ASTUZIA ≥ n`, sigla C1): così la lista di una classe è una sola, e le voci più furbe si accendono quando l'astuzia basta; le
soglie sono un parametro di ogni voce e non un livello globale. **L'astuzia** per le strategie (**deciso**) parte dall'**intelligenza massima
di classe** (`*_MAX_INTELLIGENZA`) ed è ridotta da una **rampa sul livello**: un'Ombra Nera di livello basso è "tonta", e col salire del livello del mondo
diventa più ostica. La formula è la rampa del §4 (B).

**Bersagli: i filtri** (restringono i candidati; si possono mettere insieme)

| Sigla | Filtro | Definizione |
| :--- | :--- | :--- |
| F1 | **vivo e in combattimento** | sempre: non morto, non in panchina, non fuori combattimento. Per un gruppo avversario che sceglie nel gruppo del giocatore i **bersagli possibili** sono i personaggi vivi **e gli ospiti vulnerabili vivi** (`getOspitiBersaglio`); i filtri sulla classe e sulla magia non li comprendono mai |
| F2 | **di classe / di categoria** `<X>` | `getClasse()`; `isDellaCategoria` (basta una delle categorie) |
| F3 | **magico** | prima chi ha abilità innate (Dardo arcano: Mago, Maga, Elfo, Elfa; Alba Sacra: Sacerdote, Sacerdotessa; canti: Bardo, Cantastorie) non silenziato e con la `MAGIA` per almeno una; poi i **mostri** di una classe con `isMagico()` (Strega, Lich, Drago...), non silenziati e con `MAGIA` > 0, **fin da subito**; poi gli altri solo se `MAGIA` > 0, non silenziati e che hanno già lanciato in questo combattimento |
| F4 | **con / senza lo stato** `<X>` | `hasEffettoDiStato` |
| F5 | **debole a** `<tipo di danno>` | moltiplicatore di classe e stati maggiore di 1 (senza la difesa); **non immune a** `<tipo>`: moltiplicatore maggiore di 0 |
| F6 | **salute sotto / sopra il** `n%` | salute attuale su massima |
| F7 | **di livello più alto / più basso del mio** | un confronto con chi sceglie |
| F8 | **lo stesso del turno precedente** | il bersaglio che questo personaggio ha scelto l'ultima volta (uno per avversari e uno per alleati), se è ancora valido |
| F9 | **che mi ha attaccato per ultimo** | l'avversario che ha agito contro di me per ultimo |
| F10 | **il capo** | `getCapo()` del gruppo avversario |

**Bersagli: gli ordinamenti** (scelgono *uno* fra i candidati; a parità vince il primo dell'elenco)

| Sigla | Ordinamento | Misura |
| :--- | :--- | :--- |
| O1 | **il primo** | la posizione nell'elenco dei vivi (è la scelta di base) |
| O2 | **a caso** | tutti con la stessa probabilità |
| O3 | **con meno salute / con più salute** | salute attuale, in punti |
| O4 | **di livello più basso / più alto** | `getLivello()` |
| O5 | **più debole / più resistente a** `<tipo di danno>` | il fattore di danno atteso (`moltiplicatoreAtteso`), più alto o più basso; un immune è 0 |
| O6 | **per priorità di classe** `<lista>` | il primo che appartiene alla prima classe della lista che ha candidati: per esempio Mago, Maga, Sacerdote, Sacerdotessa, Elfo, Elfa, Bardo, Cantastorie |
| O7 | **con più / con meno `MAGIA`** | la `MAGIA` attuale: chi ne ha di più è il più pericoloso. Un mostro molto furbo può preferirlo a O6 (far fuori chi ha più `MAGIA` anche se è di una classe "di seconda scelta"): basta mettere la voce con O7 prima di quella con O6 |

Per gli **alleati** (cure, resurrezioni, pozioni) valgono gli stessi, più: se stesso, il capo, un caduto, con poca `MAGIA`; "il più ferito" è O3 (con meno salute).

**Condizioni** (si mettono tutte, in AND)

| Sigla | Condizione | Su che cosa |
| :--- | :--- | :--- |
| C1 | la mia **ASTUZIA** è almeno / meno di `n` (`massimo × min(1, 0,4 + 0,075 × (livello − 1))`, §4) | di sé |
| C2 | la mia salute o `MAGIA` è sotto / sopra `n%` o `n` punti; ho `MAGIA` per l'azione | di sé |
| C3 | sono / non sono sotto lo stato `<X>`; sono di classe o livello `<X>` | di sé |
| C4 | gli avversari vivi sono almeno `n`, o uno solo; i compagni vivi; sono l'unico vivo | gruppo |
| C5 | c'è un avversario di classe / categoria / stato `<X>` | gruppo |
| C6 | il numero del turno; una voce già usata (una volta per scontro); i cambi d'arma fatti (al massimo due) | scontro |
| C7 | ho una pozione; ho una pergamena `<X>`; nel gruppo c'è un'arma che dà `<tipo>` | risorse |
| C8 | probabilità del `n%` | caso |

**Azioni**

| Sigla | Azione | Criterio |
| :--- | :--- | :--- |
| A1 | **attacco fisico** | l'ultima voce di ogni lista; non fallisce mai |
| A2 | **un incantesimo che fa danno** (una classe precisa, o "del repertorio") | criterio S: a caso, il più potente, il più adatto, o il più adatto con apprendimento |
| A3 | **Silenzio** | su un bersaglio F3 non silenziato (O6: Mago, Sacerdote, Elfo, Bardo) |
| A4 | **Morte, Resurrezione, Alba Sacra, Dardo arcano, canti** | le voci proprie di ciascuno |
| A5 | **pozione** della Salute o della Magia | chi beve: O3 fra i compagni (di solito sé stessi) |
| A6 | **cambia arma** | un'arma di tipo `<X>` (F5/C7); conta come azione e come cambio |

**Il criterio S di scelta dell'incantesimo** (per A2)

| Livello | Criterio | Bersagli multipli |
| :--- | :--- | :--- |
| S1 | **a caso** (fra quelli lanciabili adesso) | |
| S2 | **il più potente**: danno atteso più alto, senza resistenze né debolezze | il danno massimo a un bersaglio |
| S3 | **il più adatto** a quel bersaglio: danno atteso con `moltiplicatoreAtteso` | il danno totale |
| S4 | come S3, ricordando i lanci fatti: se contro quel personaggio un incantesimo ha avuto un effetto diverso (oggetti che cambiano le resistenze), se ne tiene conto | anche chi verrebbe portato vicino alla fine o ucciso (somma della quota di salute tolta a ogni bersaglio, al massimo 1) |

Il "più conveniente" di oggi (probabilità di colpire × danno atteso, contro l'attacco fisico) è S3 con in più il confronto con l'attacco.

**Che cosa serve ricordare** durante lo scontro, oltre allo stato di oggi: il numero del turno; l'ultimo avversario e l'ultimo alleato scelti da ogni
personaggio (F8); chi mi ha attaccato per ultimo (F9); chi ha già lanciato un incantesimo (F3); le voci già usate e i cambi d'arma (C6); per S4,
per ogni coppia (personaggio, tipo di danno) quanto è andato diverso dal previsto.

**Esempi di liste** (la prima voce eseguibile vince):

- **Minotauro** (bruto): *[O1 il primo] → A1 attacco fisico*.
- **Goblin / Hobgoblin**: come il Minotauro; il Hobgoblin, un po' più furbo, può aggiungere in cima *[F3 magico, O2 a caso] → A1*: sa che maghi, bardi, sacerdoti ed elfi fanno danni magici,
  ne sceglie uno a caso e attacca lui.
- **Un mostro magico di media intelligenza** (per esempio uno Spirito): *[C1 ASTUZIA ≥ 12; F3 magico, O2 a caso] → A2 con S1*; poi *[O1 il primo] → A1*.
- **Un mostro furbo** (per esempio l'Idra, il Folletto più sveglio): *[C1 ASTUZIA ≥ 20; O6 Mago, Sacerdote, Elfo, Bardo; non silenziato; C2 MAGIA sufficiente] → A3 Silenzio*; poi *[O6; C2] → A2 con S2*; poi *[O3 con meno salute] → A1*.
- **Chi si cura da solo** (tipicamente un umanoide che ha trovato o portato pozioni): *[C2 la mia salute è sotto il 30%; C7 ho una pozione della Salute] → A5 Salute, su di me* (se c'è la grande e il danno è molto, la grande).
- **Chi ricarica la magia**: *[C2 la mia `MAGIA` è sotto `n` punti; C7 ho una pozione della Magia; posso ancora lanciare incantesimi, cioè ho pergamene disponibili o un repertorio / abilità innate] → A5 Magia, su di me*. L'ultima condizione evita di sprecare la pozione quando
  non ci sarebbe niente da lanciare; "posso lanciare" si legge da `getIncantesimi(classe)` e da `puoFormulare`/`getRepertorioIncantesimi`. Queste due voci vanno in cima alla lista, prima degli attacchi.
- **Uno stratega** (Strega, Lich, Drago): *[C1 ASTUZIA ≥ 31; F3 magico non silenziato, O7 con più MAGIA, poi O6] → A3*; *[O5 più debole al suo elemento] → A2 con S3 o S4*; se la `MAGIA` è sotto la soglia e ha una pozione → A5; *[O3] → A1*.

I livelli di intelligenza di questi esempi (12, 20, 31) sono le soglie proposte (§10): sono parametri della voce, non categorie del mostro.

### 7.6 I blocchi SE / ALTRIMENTI / FINE

Una voce non è sempre un'azione vera: può essere un'**azione di controllo** che ne contiene altre. Serve per mettere la fascia di intelligenza
dentro la lista di una classe, e anche il giocatore la può usare.

```
SE <condizione>          ← una o più condizioni del catalogo (C1–C8), per esempio C1 ASTUZIA ≥ 31
    <voce>                ← azioni vere, o altri blocchi SE (nidificati)
    <voce>
ALTRIMENTI                ← facoltativo
    <voce>
FINE
```

Regole (proposta):

- Un blocco contiene una **lista ordinata di voci**, che sono azioni vere o altri blocchi, a qualunque profondità. Non c'è un tetto al numero di voci, nidificate comprese.
- Se la condizione è vera si esegue il ramo `SE`, altrimenti il ramo `ALTRIMENTI` (se c'è): è la **condizione** a scegliere il ramo, non il
  fatto che le voci dentro si possano eseguire.
- Dentro il ramo vale la regola di sempre: la **prima voce eseguibile vince** e il turno finisce.
- Un blocco è **trasparente** (deciso): se nel ramo scelto non c'è nessuna voce eseguibile (o manca l'`ALTRIMENTI` e la condizione è falsa), si
  prosegue con la voce che segue il blocco. Se alla fine della lista nessuna condizione o azione è applicabile, il personaggio fa un **attacco
  fisico** e, al peggio, un **incantesimo a caso**: un turno non resta mai senza un'azione.
- Alla lettura si controlla che i `SE` e i `FINE` siano bilanciati e che i nomi esistano.

**Le condizioni del `SE`** (deciso): le condizioni del catalogo (C1–C8, e i filtri sui bersagli) si combinano con **E**, **O** e **NON**, con le **parentesi**.
Per esempio `ASTUZIA>=20 E (MAGICO O NON_SILENZIATO) E NON PRIMO_TURNO`. Come mostrarle graficamente al giocatore è un altro discorso, da fare con la scheda
delle tattiche. Potrebbero servire anche delle **variabili locali** (un valore calcolato una volta e usato più volte nella lista, per esempio "il bersaglio
scelto"); se ne riparla quando saranno chiare le liste, per ora non ci sono.

**Blocchi con nome, per condividere i pezzi** (deciso nella sostanza): un gruppo di voci può avere un **nome** e essere usato da più liste e da più classi,
per esempio "tutti i non morti cominciano così" o "il Silenzio sui magici". Una proposta di sintassi:

```
BLOCCO SilenzioSuiMagici          ← definizione (in un file di blocchi comuni, o nel file della classe)
    FILTRO=MAGICO,NON_SILENZIATO;ORDINE=MAGIA_PIU;AZIONE=SILENZIO
FINE

// nella lista di una classe
SE ASTUZIA>=31
    USA SilenzioSuiMagici         ← vale le voci del blocco, nello stesso punto
FINE
```

Un blocco con nome vale come le sue voci scritte lì (e come ogni blocco è trasparente). Le regole (decise): i blocchi comuni stanno in un **file comune**
da cui le liste attingono (se dovesse diventare ingestibile si ripiega su un file unico). Un blocco **non può usare se stesso, nemmeno
indirettamente**: le **dipendenze circolari** si rilevano alla lettura e il file è rifiutato con un errore (come fa `GrammarBean` con i cicli:
vedi `GrammarBean.md` §5.5). Con questo una lista termina sempre, e non occorre un tetto al numero di voci.

Il **file** (**uno per `TipoPersonaggio`**, per leggibilità; i boss ne hanno uno proprio) usa lo stesso stile di `leggendari.txt` e `missioni.txt`: una
voce per riga, campi `CHIAVE=valore` separati da `;`. Un esempio, solo per dare l'idea (la sintassi vera è da definire):

```
// Strega
SE ASTUZIA>=31
    FILTRO=MAGICO,NON_SILENZIATO;ORDINE=MAGIA_PIU;AZIONE=SILENZIO
    AZIONE=INCANTESIMO;CRITERIO=S3
ALTRIMENTI
    SE ASTUZIA>=20
        FILTRO=MAGICO;ORDINE=CLASSE:MAGO,SACERDOTE,ELFO,BARDO;AZIONE=INCANTESIMO;CRITERIO=S2
    FINE
FINE
ORDINE=MENO_SALUTE;AZIONE=ATTACCO_FISICO
```

Lo stesso formato serve per **salvare** le tattiche del giocatore e per i **modelli per classe** del §7.4.

### 7.7 Il registro delle decisioni

Ogni volta che un personaggio sceglie che cosa fare scorrendo la sua tattica, il motore pubblica sul bus un evento **`InternoDecisioneTattica`**
(`eventi/interni`, tipo `INTERNO_DECISIONE_TATTICA`). Porta il **personaggio** (come `VistaPersonaggio`) e una **sintesi dettagliata del perché**.
Lo registra `SnifferBusEventi` come ogni altro evento, e il suo tipo nella riga di log (`headerEvento`) permette a un filtro sui log (un
`grep INTERNO_DECISIONE_TATTICA` sul file di `-Dforesta.log=...`) di tirare fuori subito le sole decisioni tattiche, per mettere a punto le tattiche e
per leggere le partite.

Che cosa deve dire la sintesi (**dettagliata**):

- **la voce scelta** e il suo percorso nella lista (con i blocchi `SE / ALTRIMENTI` attraversati: "voce 3, dentro il `SE ASTUZIA>=20`");
- **le condizioni valutate** con i valori ("ASTUZIA 24 ≥ 20: vero", "`MAGIA` 3 < 5: falso") e le voci **saltate** con il motivo ("nessun magico non
  silenziato", "`MAGIA` insufficiente per il Silenzio");
- **i candidati considerati** per il bersaglio, con la misura usata ("magici: Mira, Orfeo; ordine per `MAGIA`: Mira 12, Orfeo 4 → Mira");
- **l'azione** e, per un incantesimo, il **criterio** (S1–S4) con i **danni attesi** confrontati ("Gelo 14, Fulmine 31: Fulmine"); per un cambio d'arma, l'arma
  presa e il perché;
- il caso **di ripiego** ("nessuna voce eseguibile: attacco fisico").

**Non è un evento nuovo da aggiungere accanto a uno esistente: è l'evoluzione di `InternoRisultatoValutazionePersonaggioAttaccante`**
(`INTERNO_RISULTATO_VALUTAZIONE_PERSONAGGIO_ATTACCANTE`). Oggi quell'evento registra già la decisione dell'avversario sull'incantesimo da lanciare
(`PersonaggioBase.scegliIncantesimoContro`, otto punti di pubblicazione, registrata da `SnifferBusEventi.onEventoValutazioneAttaccante`): porta il
bersaglio, un `TipoRisultatoValutazioneAttaccante` (`NON_USA_MAGIA`, `SILENZIATO`, `SENZA_MAGIA_A_DISPOSIZIONE`, `SENZA_INCANTESIMI_A_DISPOSIZIONE`,
`CASUALMENTE_NON_LANCIA_INCANTESIMO`, `SCEGLIE_A_CASO`, `SCEGLIE_IL_PIU_POTENTE`, `PREFERISCE_ATTACCO_MAGICO`, `PREFERISCE_ATTACCO_FISICO`), l'incantesimo, le
probabilità di colpire e i possibili danni. Con le tattiche diventerebbe un **doppione** di `InternoDecisioneTattica`, quindi basta **rinominarlo** e
**aggiungere quel che manca**, senza tenere due eventi:

- il **bersaglio**, l'**incantesimo**, le **probabilità di colpire** e i **danni** diventano parte della sintesi (il confronto "attacco fisico contro magico" di oggi è
  il criterio S3 con il confronto con l'attacco);
- il **tipo di risultato** (l'enum) si sostituisce con il testo del perché, con la voce, le condizioni, i candidati e le voci saltate (l'enum e le sue nove
  etichette spariscono: i motivi diventano frasi);
- quel che manca: la voce scelta e il percorso nei blocchi, le condizioni valutate con i valori, i candidati e le misure, le voci saltate;
- l'evento porta un `VistaPersonaggio` e una stringa, non più `Personaggio` e `Incantesimo`, e così esce dalle **eccezioni** di
  `DipendenzeEventiTest` (gli eventi non portano classi del dominio);
- finché il motore delle tattiche non c'è, l'evento resta com'è e le otto pubblicazioni in `scegliIncantesimoContro` continuano; quando entra il motore
  le sostituisce.

Per non pesare sul gioco, la sintesi si costruisce solo se serve (con il log acceso, o in un test), e non a ogni turno di ogni mostro. I test sulle tattiche dei
mostri la leggono per verificare che il mostro abbia deciso per la ragione giusta, e la UI potrebbe mostrarla al giocatore per aiutarlo a mettere a punto le
sue.

## 8. Che cosa manca nel modello dati

**Prerequisito** (§1): tutto quello che segue serve a *leggere* e a *scegliere*; il motore delle tattiche non modifica il modello, impartisce comandi che esegue l'automa.

Quasi tutto è già leggibile (§6 e §7). Quello che manca:

- **La scorta di incantesimi e pozioni anche per il gruppo avversario** — **la struttura è fatta**: le pergamene e le pozioni (della Salute e della Magia, normali e
  grandi) stanno ora in `GruppoMD` e le gestisce `Gruppo` (`getIncantesimi`, `addPozioniSalute`, `consumaPozioneSalute`...); le variazioni del gruppo del giocatore si
  annunciano alla UI, quelle del gruppo avversario no, e le sue scorte si azzerano a ogni locazione. I mostri però conoscono ancora per natura un **repertorio fisso**
  di incantesimi malefici (`getRepertorioIncantesimi`, filtrato per tipo `MALEFICO`) e li pagano con la `MAGIA`, senza pergamene. **Fatto anche il bottino** (`BottinoDegliAvversari`): quando il gruppo avversario si popola (all'inizio della locazione o quando arriva un'ondata), per **ogni avversario** c'è il 20%
  (`Costanti.PROBABILITA_PERGAMENA_PER_AVVERSARIO`) di aggiungere una pergamena: se è un **umanoide** (categoria `UMANOIDE`), un incantesimo qualsiasi (anche se è magico: ha la precedenza); altrimenti, se è magico (`isMagico`),
  una del suo repertorio (uno spirito: Gelo). Per ogni umanoide c'è inoltre il 20% (`Costanti.PROBABILITA_POZIONE_SALUTE_PER_AVVERSARIO_UMANOIDE`) di una pozione della Salute normale. Vinto lo scontro, tutto ciò che resta delle scorte degli avversari passa al giocatore, con un messaggio (le notifiche di variazione delle scorte ci sono già).
  Oggi i mostri non leggono le pergamene, quindi arrivano tutte al giocatore. **Resta da fare**: far usare pozioni e pergamene dalle tattiche (solo agli umanoidi; le voci A5 e C2/C7 del catalogo, §7.5).
- **L'evento `InternoDecisioneTattica`** (§7.7): è `InternoRisultatoValutazionePersonaggioAttaccante` **rinominato e completato** (classe in `eventi/interni`, il valore
  `INTERNO_DECISIONE_TATTICA` in `TipoEvento` al posto di `INTERNO_RISULTATO_VALUTAZIONE_PERSONAGGIO_ATTACCANTE`, la riga di registro in `SnifferBusEventi`), non un
  evento in più; il motore delle tattiche costruisce la sintesi dettagliata mentre sceglie la voce e sostituisce le otto pubblicazioni di `scegliIncantesimoContro`.
- **Il salvataggio delle tattiche con il personaggio** (requisito fondamentale, vedi §1). La tattica di ogni personaggio sta nel suo modello dati
  (`PersonaggioMD`, che si scrive e si legge in testo con `scrivi` / `leggi`) e passa dal salvataggio (`GestoreSalvataggiSuFile`) e dalla rilettura
  (`RiletturaPartita`). Il formato è quello di §7.6, scritto in modo che si rilegga identico: le voci, i blocchi `SE / ALTRIMENTI / FINE` nidificati
  e le condizioni con E, O, NON e parentesi. I **blocchi con nome** usati da una lista vanno **salvati dentro la tattica** (o espansi), e non
  solo richiamati per nome dal file comune: così un cambio del file comune in una versione successiva non altera le tattiche di una partita già salvata.
  I salvataggi non devono restare compatibili con le versioni precedenti, quindi non serve alcuna conversione. Serve un **test di andata e ritorno**:
  una tattica complessa (blocchi nidificati, nomi, condizioni composte) scritta e riletta torna uguale, e un personaggio caricato agisce come prima del
  salvataggio.
- lo **`StatoDelCombattimento`** (§3.3) — **fatti**: il numero del round (con l'evento `InternoInizioRound`), chi ha agito nel round e il puntatore di rotazione di ogni
  gruppo, con il round-robin della risposta avversaria e della risposta nella mischia; il puntatore non è più nel `Gruppo`. **Mancano**, per personaggio: l'ultimo bersaglio
  (avversario e alleato), chi lo ha attaccato per ultimo, chi ha lanciato un incantesimo, le voci già usate (e il round dell'ultimo uso), i cambi d'arma, le armi valutate; e la
  conoscenza sulle reazioni. Il gruppo del giocatore ha il suo puntatore ma non lo usa ancora (`LocazioneBase` fa agire un solo `combattente`, scelto dal giocatore);
- (il riconoscimento di un **boss** per ora non serve, §6.1; le **categorie di creatura** ci sono già);
- la **difesa per elemento**, se si vorrà (§6.3, lavoro 5 e "Da decidere"): le debolezze e resistenze di classe sono già un dato (`getMoltiplicatoreDannoSubito`) e la
  loro composizione in un fattore senza effetti collaterali c'è già (`moltiplicatoreAtteso`);
- la **risposta** come azione a sé degli avversari: oggi chi risponde è già scelto in round-robin, ma l'azione è ancora `rispondiInMischia` sul combattente, con la logica fissa di §4;
- le **tattiche base dei mostri**: un file di risorse per classe (§4), nello stesso formato di quelle del giocatore, e il modo di assegnarle a
  un avversario quando nasce (`FabbricaPersonaggi`), con la possibilità che **una singola istanza abbia una tattica sua** al posto di quella di classe (una
  missione può portare un goblin più furbo degli altri con la sua), e che questa sia salvata con la partita; e il motore che le esegue, con le **azioni di controllo** `SE / ALTRIMENTI / FINE`
  nidificate (§7.6), che oggi non esistono: la lista di voci non è più piatta ma un albero;
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

- Oggi la scelta del **bersaglio** non esiste (si attacca sempre il primo avversario vivo): le tattiche la introducono per tutte le
  azioni singole.
- Il round-robin cambia il **bilanciamento**: oggi un compagno solo contro più avversari ne subisce uno in più al massimo ("si
  disimpegna e attacca"); nello schema nuovo li subisce tutti, e un gruppo numeroso contro un avversario solo gli dà più azioni. Va
  riverificato con le simulazioni (`SimulazionePartiteTest`, i test di bilanciamento) e con i test di scenario che contano i round.
- I test che oggi fanno combattere un solo personaggio andranno riscritti: con la prima fase (attacchi singoli, §3.1) restano
  quasi tutti validi; si riscrivono con il round a coppie (§3.2).
- Le tattiche dei mostri rendono parametrizzabile ciò che oggi è sparso in `PersonaggioBase` (§4): resta il rischio di cambiare il
  comportamento dei mostri senza accorgersene, da coprire con un test sulle decisioni di oggi.

## 10. Da fare / idee aperte

Domande aperte:

- **La sintassi vera** del formato delle tattiche (§7.6) e le **variabili locali**, se serviranno.
- **Il formato** salvabile (testo semplice nel salvataggio, con le tattiche di ogni personaggio e le sue personalizzazioni — vedi il requisito in §1 — e in un file di modelli per classe) e la **scheda** secondaria del
  personaggio (elenco ordinabile con aggiungi, togli, sposta su e giù, attiva/disattiva).
- **Livello di dettaglio dei controlli** per la prima versione (probabilmente: sempre, salute e magia sotto una soglia, stati, classe,
  numero di avversari, risorse, probabilità; poi le categorie di creatura, già nei dati).
- **Cambiare arma** (§5): quale arma esce con la doppia arma, cioè quale è "la meno conveniente da usare in quel momento" (danni, effetti di stato, modificatori; vedi sopra)? Per quanto tempo valgono le valutazioni già fatte delle armi del gruppo? Le voci per tornare all'arma di prima le scrive il giocatore o le propone il sistema in coppia con la prima?
- **Il round a coppie** (§3.2): quando rivederlo, e se i personaggi in più possono agire senza risposta anche dopo il primo giro.
