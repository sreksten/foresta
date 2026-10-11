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

- Il combattimento procede **per round a coppie** (§3, **fatto**): a ogni `TIMER` agisce una sola volta il prossimo del piano del round, un mio personaggio e poi un
  avversario a turno, e i personaggi in più di un gruppo agiscono da soli. Chi dei miei agisce in automatico attacca con le armi il primo avversario vivo (finché non ci sono le
  tattiche); l'avversario che tocca attacca il mio gruppo scegliendo il bersaglio da sé (`PersonaggioBase.attacca(Gruppo)`: armi o, per chi sa la magia, un incantesimo a caso del suo
  repertorio, che rinuncia se è `SILENZIATO`). Non c'è più una "risposta" a chi ha agito, né il "si disimpegna e attacca".
- Gli **incantesimi** e le **pozioni** sono del gruppo: le pergamene e le pozioni stanno nell'inventario del gruppo
  (`GruppoGiocatore.getIncantesimi(classe)`, `getPozioniSalute()`...), e chi li usa lo sceglie il giocatore (o il motore: `getFormulante`).
  Gli incantesimi **innati** (dardo arcano, alba sacra, i tre canti) non consumano pergamene.
- La risposta avversaria ha già un embrione di tattica, scritto nel codice (`PersonaggioBase.attacca(Gruppo)` e `scegliIncantesimoContro`):
  chi colpire, se lanciare un incantesimo, quale, e se conviene più la magia o l'attacco fisico. Vedi §4.
- Non c'è **posizione**: né distanza, né mischia contro distanza, né linea di vista. Non c'è un turno di preparazione degli
  incantesimi (sono istantanei) né cooldown. Gli avversari non hanno un "bersaglio" persistente: rispondono a chi ha appena agito.
- **Gli ospiti vulnerabili sono bersaglio di ogni azione degli avversari**: l'avversario che agisce sceglie sempre con `PersonaggioBase.attacca(Gruppo)`, dove la scelta a caso comprende
  gli ospiti vulnerabili vivi (prima i maghi, i sacerdoti, gli elfi e i bardi). Con le tattiche la scelta sarà della tattica dell'avversario (§4), e la scelta a caso li comprenderà ancora.

## 3. Il round-robin dei due gruppi

Il turno a rotazione vale per **tutti e due i gruppi**, e va tracciato in questo piano perché le tattiche ne dipendono.

**Il puntatore.** Ogni gruppo ricorda **chi ha agito per ultimo** e passa la mano al **successivo personaggio vivo** (saltando chi è
caduto o in panchina). Il puntatore di ogni gruppo avanza **sempre**, a ogni azione, e non si azzera fra uno scontro e l'altro dentro la
stessa locazione. **Fatto** (`StatoDelCombattimento.getProssimoAttaccante`, §3.3): il puntatore sta nello stato e non più nel `Gruppo`; la risposta avversaria (`rispostaAvversaria`) e la risposta nella mischia (`eseguiSingoloAttacco`)
lo usano per far agire il prossimo avversario vivo, saltando il turno a chi è incapace di agire (atterrato, congelato, stordito). Il gruppo del giocatore ha il suo puntatore: lo usa il combattimento automatico (agiscono tutti i miei, a turno), e le azioni manuali lo spostano su chi ha agito.

**Non c'è una "risposta".** Quando un mio personaggio agisce, gli avversari **non rispondono**: ognuno **agisce quando il piano del round tocca a lui** (§3.1), in round-robin, e attacca
a sua volta scegliendo il bersaglio da sé. **Chi colpire non è una regola fissa**: lo decide la tattica dell'avversario (§4), come per i miei personaggi.

**Azioni manuali.** Agisce il personaggio scelto dal giocatore (`PERSONAGGIO_n`, come oggi); fa **una** azione, e il puntatore del
gruppo passa **al successivo** vivo rispetto a lui.

**Eventi a metà round.** Chi muore durante il round non agisce più, e il puntatore salta i caduti: se cade un compagno che doveva ancora
agire, la rotazione passa al personaggio successivo. Se cade l'ultimo avversario il round finisce lì (lo scontro è vinto, nessuno
agisce più); se cade il capo del giocatore la partita è persa, come oggi.

**Interruzione.** La **fuga** resta del giocatore: è un'interruzione manuale del combattimento (`INTERRUZIONE_COMBATTIMENTO`, o il
bottone di fuga) che rimette la scelta alle sue mani. Le tattiche non fuggono.

### 3.1 Il round a coppie (fatto)

A inizio round si contano i personaggi **in grado di agire** dei due gruppi (*n* i miei, *m* gli avversari: vivi e non in panchina) e si costruisce il **piano del round**
(`StatoDelCombattimento.getPiano`): **max(n, m) coppie**, alla coppia *k* agisce il mio *k*-esimo (se *k* < *n*) e poi l'avversario *k*-esimo (se *k* < *m*). Il piano è la
sequenza dei **lati**, non dei personaggi: chi agisce davvero lo decide, al momento, il round-robin di ogni gruppo.

| Caso | Piano | Che cosa succede |
| :--- | :--- | :--- |
| 3 miei contro 2 avversari | G A G A G | due coppie, e il mio terzo agisce **senza avversario** |
| 1 mio contro 3 avversari | G A A A | il mio agisce e poi **attaccano uno dopo l'altro** tutti e tre gli avversari: il mio personaggio ne subisce tre |
| 2 contro 2 | G A G A | due coppie |

È l'effetto voluto: un avversario solo contro tre compagni agisce una volta e subisce più azioni; un compagno solo contro tre avversari ne subisce tre. **La regola "si disimpegna e
attacca" è uscita dal codice**, così come la "risposta" al combattente. Il piano si calcola a inizio round e **non si ricalcola quando qualcuno cade**: il posto di chi non c'è più
si salta (`registraAzioneSaltata`), e chi è incapace di agire (atterrato, congelato, stordito) salta il turno ma il suo posto è usato. Quando arriva un'**ondata** il piano si rifà con
i nuovi arrivati (`nuovaOndata`), senza cambiare il numero del round.

### 3.2 Combattimento automatico e azioni manuali

- **Automatico** (a ogni scatto del `TIMER`): **una sola azione per scatto**, del prossimo del piano, un mio personaggio o un avversario. Agiscono **tutti i miei vivi**, in rotazione
  (con il comando `COMBATTIMENTO` il giocatore sceglie chi comincia, `faAgirePerPrimo`); la finestra del combattimento mostra **chi sta agendo e il bersaglio** (`InternoAzioneInCombattimento`;
  senza bersaglio se l'azione non lo ha: una pozione, l'Alba Sacra, un canto). Ogni scatto è quindi mezzo scambio, e un round ne fa tanti quanti sono i posti del piano.
- **Manuale** (attacco singolo, incantesimo, dardo, canto, pozione): il giocatore sceglie chi e che cosa; l'azione occupa il **primo posto libero dei miei** nel piano, e subito dopo gli
  avversari agiscono finché il piano non torna a un mio personaggio o il round non finisce. Il puntatore dei miei passa al successivo rispetto a chi ha agito. Con un avversario solo
  contro tre miei, la prima azione ha l'avversario e le altre due no.
- **Bere una pozione è un'azione del personaggio** e consuma il suo turno (prima non faceva agire nessuno), e interrompe ancora la mischia automatica, come l'incantesimo.
- **Gli effetti di stato scorrono alla fine del round** (quando il piano è finito), non a ogni azione: con 3 miei contro 1 durano di più in azioni del giocatore. Fuori dal piano (un
  tentativo di corruzione o di amicizia fallito, senza avversari) il turno passa come prima.
- **Gli avversari che attaccano per primi** (un passaggio inosservato fallito) sono fuori dal piano: uno solo attacca.

### 3.3 Il round e lo stato del combattimento

**Che cos'è un round** (deciso, **fatto**): finisce quando il **piano del round** (§3.1) è finito, cioè quando hanno agito (o saltato) tutti i posti, dei due gruppi. Il numero del round **riparte da 1 a ogni
inizio locazione** e **continua** quando arriva la prossima ondata: il combattimento e la locazione non sono cambiati, sono solo apparsi nuovi mostri. Se non ci sono più avversari
il combattimento è finito e il round non avanza. All'inizio di ogni round il motore pubblica un evento **`InternoInizioRound`** (con il numero), per la UI, i log e chi tiene la
memoria del combattimento; a fine round passano gli effetti di stato.

**Lo `StatoDelCombattimento`** (deciso nel nome; **fatto**: il piano del round con `InternoInizioRound`, chi ha agito nel round, i puntatori dei due gruppi e il round-robin, le schede dei personaggi e la conoscenza pubblica sulle reazioni; resta da collegare solo la fonte che dipende dalle tattiche: le voci usate): un oggetto che raccoglie in un posto solo tutto ciò che le tattiche devono ricordare di un combattimento. Si crea
(o si azzera) a ogni inizio locazione, insieme alla preparazione del gruppo avversario. Contiene:

- **del combattimento**: il numero del round, il registro di chi ha già agito nel round corrente e, per **ogni gruppo**, il **puntatore** all'ultimo personaggio che ha agito
  (§3: passa al successivo vivo, saltando i caduti e la panchina). Il puntatore esce dal `Gruppo` (dove è un indice, `ultimoIndiceSelezionato`) per stare qui: è memoria
  del combattimento e non modello, e vive come lo stato (azzerato a inizio locazione, non fra uno scontro e l'altro nella stessa locazione). Meglio che sia
  il **personaggio** e non l'indice, perché la lista del gruppo cambia (un compagno cade, esce, un nuovo mostro arriva) e un indice slitterebbe;
- **per ogni personaggio** (una scheda, indicizzata per identità del personaggio): l'ultimo avversario e l'ultimo alleato scelti (F8); chi lo ha attaccato per
  ultimo (F9); se ha già lanciato un incantesimo (la regola del "magico", F3); quanti cambi d'arma ha fatto; le armi del gruppo già valutate; le voci già
  usate; e, volendo, le statistiche (danni inflitti e subiti, incantesimi lanciati, colpi a segno e mancati);
- **la conoscenza accumulata sulle reazioni** (S4), **di pubblico dominio e non legata a un personaggio**: in combattimento tutti vedono quel che succede, nel bene e nel male, anche di
  sfuggita, quindi la sa chiunque e non ha un osservatore. Per ogni coppia (personaggio colpito, tipo di danno) si ricorda di quanto l'**equipaggiamento difensivo** ha cambiato il
  danno (`DannoRisultante.getFattoreEquipaggiamento`: il rapporto fra la mitigazione con i pezzi e quella con la sola difesa di base, `difesaContro` / `difesaBaseContro`; 1 se
  non conta, meno di 1 se protegge). È la parte che le tattiche non possono prevedere: classe e stati li dà già `moltiplicatoreAtteso`, e il fattore osservato si moltiplica per
  quello. Lo registra ogni colpo andato a segno (`PersonaggioBase.applicaRisultatoCombattimento` → `registraColpo`; le previsioni delle tattiche, che chiamano il calcolo senza
  applicarlo, non lasciano tracce); una nuova osservazione sostituisce la vecchia; `getFattoreEquipaggiamentoNoto` dà 1 se non si è visto niente.

Regole (decise):

- **Che cosa è fatto e dove si aggiorna**: i colpi (ultimo avversario, ultimo attaccante, reazione) in `applicaRisultatoCombattimento`; "ha lanciato" in `formula` degli incantesimi, nel canto e nel dardo arcano; l'ultimo alleato nella Resurrezione. I cambi d'arma e le armi valutate li registra `GruppoGiocatore.cambiaArma` / `puoCambiareArma` (§5); le voci usate hanno già il metodo (`registraUsoVoce`) ma lo chiamerà il motore delle tattiche.
- **Non si salva**: un combattimento non si interrompe mai per salvare (il comando non è disponibile), quindi lo stato è transitorio e si perde con il
  combattimento. Le tattiche di ogni personaggio, invece, sì (§1).
- **La conoscenza è pubblica**: vale per i personaggi del giocatore e per i mostri, e quello che si è imparato sul comportamento di un personaggio lo sa chiunque, in
  entrambi i gruppi.
- **La scheda di un personaggio non si dimentica quando cade**: un compagno potrebbe farlo risorgere, e la conoscenza sulle sue reazioni deve valere ancora. Si
  azzera solo con l'inizio della locazione; i mostri di una nuova ondata partono con una scheda vuota.
- **Non è il modello dati**: è la memoria tattica. La aggiornano il motore e la locazione (o l'oggetto stesso, ascoltando gli eventi come `InternoInizioRound`);
  le tattiche la *leggono* e basta, come vuole il prerequisito del §1.

**Una conseguenza:** il round è lungo quanto il piano (max(n, m) coppie), non un giro di un gruppo solo. Le condizioni sul round e i cooldown contano quindi "giri completi dei due gruppi".

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
- **Il controllo deve guardare il dopo-cambio** — **fatto** (`RegoleEquipaggiamento.valutaCambioArma`, `GruppoGiocatore.controllaCambioArma`). `valuta` guarda l'equipaggiamento *com'è adesso*, con l'arma di prima ancora in
  mano e nel carico. Per il cambio, a mano, il giocatore prima toglie la vecchia e poi prende la nuova: se la tattica chiamasse
  `puoEquipaggiare` prima di togliere, i risultati sarebbero sbagliati in entrambe le direzioni. Una spada nuova verrebbe respinta
  perché la seconda arma non è consentita (o la mano principale è occupata), un'arma troppo pesante per il carico di adesso entrerebbe
  una volta uscita la vecchia, e un pugnale verrebbe accettato nella mano secondaria, che non è un cambio. Per questo il controllo
  si fa sulla situazione "senza l'arma che esce": gli artefatti che escono si tolgono **solo dall'elenco** del personaggio (non con `removeArtefatto`,
  che ricalcola gli attributi e riduce salute e MAGIA se l'arma ne dava il massimo, e pubblica notifiche), si chiama `valuta` e si rimettono nello stesso ordine,
  senza effetti né notifiche. Un cambio che non passa è un'azione **senza effetto**: non consuma il turno e si passa alla voce successiva.
- **Chi esce** (regola provvisoria, finché non c'è il criterio della doppia arma): per un'arma da mano principale, quella che occupa lo slot (o l'arma a due mani che occupa entrambi); per un'arma a due mani, tutto quello che ha in mano.
  **Lo scudo, e ogni cosa che non sia un'arma, non esce mai**: con lo scudo in mano un'arma a due mani non si può prendere (`MANI_OCCUPATE_PER_ARMA_A_DUE_MANI`).
- **Ogni arma si valuta una volta sola.** Chi ha provato un'arma e ha visto che non può impugnarla se lo ricorda, per non rifare le
  prove a ogni turno e a ogni voce. In teoria il personaggio potrebbe valutare tutte le armi dell'inventario del gruppo, ma lo fa
  **una volta sola** e si segna quali può usare (e quali no), per quel combattimento. La valutazione dipende da chi la fa (classe,
  livello, forza, carico) e da quello che ha in mano: se uno di questi cambia (un'arma nuova nell'inventario, un cambio d'arma, un
  livello, un effetto che cambia il carico) le valutazioni vanno rifatte o scartate. Il dato sta nello stato del combattimento (§8): `registraArmaValutata` / `getValutazioneArma`, usati da `GruppoGiocatore.puoCambiareArma`; un cambio d'arma del personaggio scarta le sue valutazioni, e si azzerano a ogni locazione.
- **Con la doppia arma, quale esce** (**fatto**, versione semplice): si confrontano le due armi che il personaggio ha in mano e **esce quella che infligge meno danno**;
  a parità, la secondaria. Il danno è `CalcolatoreCombattimento.dannoAttesoDellArma`: danni base per il livello di combattimento dell'arma, più per ogni incantamento il bonus fisso
  e la quota dell'INTELLIGENZA; senza dadi né critici, **contro un bersaglio neutro** e al pieno anche per la secondaria. La nuova arma prende lo slot di quella che esce, quindi deve poterci stare:
  una lancia (solo mano principale) fa uscire la principale anche se è la più forte. Vale solo per chi impugna due armi (Ladro, Elfo) e se in mano ci sono due armi (non uno scudo). **Da affinare**: il confronto
  contro il bersaglio vero (`moltiplicatoreAtteso` per il tipo di danno di ciascuna arma), gli effetti di stato e i modificatori che l'arma applica, e la probabilità di colpire (§10).
  Se l'arma più debole è proprio quella che dà il tipo di danno voluto, lo evita la condizione della voce ("non ho già un'arma che dà quel danno"), non il controllo.
- **Un massimo di cambi per combattimento, per personaggio**: **uno, al più due** (una costante in `Costanti`), per fermare i continui
  cambi anche quando le condizioni lo permettono. Il massimo è `Costanti.MAX_CAMBI_ARMA_PER_COMBATTIMENTO` (2). Si conta nello stato del combattimento (§8: `registraCambioArma`, `getCambiDArma`) e ricade fra le condizioni della voce
  ("non ho ancora cambiato arma").
- **Come si esegue** (**fatto**: `GruppoGiocatore.cambiaArma`): non passa dagli eventi `InternoStoccaggioArtefatto` / `InternoPrelievoArtefatto`, che sono richieste della
  schermata dell'inventario con una notifica di ritorno: è una valutazione del motore, e serve un esito certo nello stesso istante. L'azione chiama direttamente i due passi
  pubblici del gruppo, `stocca` (l'arma di prima nell'inventario del gruppo) ed `equipaggia` (la nuova al personaggio), dopo `puoCambiareArma` (massimo di cambi più controllo
  preventivo); poi conta il cambio. Gli stessi due passi li usano ora anche gli handler degli eventi della schermata dell'inventario, dopo `controllaPrelievo`.
  Il cambio non consuma il turno da solo: lo fa chi lo chiama (il motore delle tattiche). È un'**azione atomica**: già validata dal controllo preventivo, non teme un rifiuto a metà.
- **Il filtro sull'inventario** (**fatto**: `GruppoGiocatore.armiPerCambio(personaggio, tipoDanno)`): le armi da mano principale o a due mani del gruppo che danno quel tipo di danno
  (`Arma.daTipoDanno`) e che il personaggio può prendere al posto di quelle che ha in mano.
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

**Limite accettato.** La distinzione di difesa è solo fisico / non fisico (`COSTITUZIONE + PARATA` e `RESISTENZA_MAGICA`): fra il fuoco e il gelo, o fra il
tagliente e il contundente, un mostro non ha differenze di difesa se non quelle dei punti 1 e 2 (tabella di classe e stati). Le resistenze
parziali per classe ci sono (`debolezze_e_resistenze.md`).

**Fattore di danno atteso.** Si calcola, per un avversario e un tipo di danno T, **senza tirare i dadi**:

> fattore = 0 se immune a T; altrimenti `100 / (100 + difesa)` × il moltiplicatore di classe × i moltiplicatori degli stati attivi per T
> (la difesa è `difesaContro`, con gli stati che la cambiano) — **`CalcolatoreCombattimento.moltiplicatoreAtteso`, già fatto**.

Da lì: **debole** = moltiplicatore > 1, **resistente** = < 1, **immune** = 0, e "il più debole fra questi elementi" = il fattore più alto.
Serve a scegliere il bersaglio ("il più debole al fuoco"), l'incantesimo o l'arma ("il Gelo contro un `BAGNATO`", "un'arma sacra contro i
non morti": la condizione del cambio d'arma, §5) e il momento del cambio.

**Decisioni.** "Debole" vuol dire solo moltiplicatore > 1 (non "meno resistente degli altri elementi"). Il selettore e la scelta dell'azione guardano gli stati
di adesso **e anche quelli che l'azione stessa può causare** (il Gelo che prima bagna e poi congela, quindi con i moltiplicatori della reazione che ne seguirebbe).
**Fatto:** `CalcolatoreCombattimento.statiCausabili(difensore, arma)` (stati nativi del tipo di danno e degli incantamenti, esclusi tipi di danno e stati a cui è immune) e
`moltiplicatoreAttesoDopo(difensore, tipoDanno, arma | stati)`, il fattore del colpo che segue l'azione: gli stati si contano come se fossero scattati (come le reazioni).

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

> **Superato dal §11:** i blocchi (e i blocchi con nome) sono stati tolti dal modello; la tattica è una lista piatta di voci, ciascuna con la sua condizione. Resta qui
> la sintassi di esempio, utile solo come idea del formato a campi.

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

Ogni volta che un personaggio sceglie che cosa fare scorrendo la sua tattica, il motore pubblica sul bus l'evento **`InternoDecisioneTattica`** (`eventi/interni`, tipo `INTERNO_DECISIONE_TATTICA`), **prima** che l'azione si esegua. Porta il personaggio e il bersaglio (come
`VistaPersonaggio`), l'azione scelta (per l'incantesimo di danno, quello scelto), la posizione della voce (−1 per il ripiego sull'attacco fisico) e la **spiegazione**, una riga per passo. `SnifferBusEventi` lo scrive nel log con il suo tipo nella riga (`headerEvento`), quindi un
`grep INTERNO_DECISIONE_TATTICA` sul file di `-Dforesta.log=...` tira fuori le sole decisioni tattiche, per mettere a punto le tattiche e per leggere le partite. **Fatto** (`CombattimentoConTatticheTest`, `ValutatoreTest`).

Che cosa dice la spiegazione (la costruisce il `Valutatore`, i selettori e `SceltaIncantesimo`):

- **le voci saltate** con il motivo ("voce 1: disattivata", "voce 2: le condizioni non sono tutte vere [...]", "voce 3: nessun bersaglio", "FULMINE scartata: manca la pergamena...");
- **il percorso del selettore** con i candidati che restano dopo ogni passo, con i nomi: "AVVERSARI: 3 [Goblin 1, Goblin 2, Goblin 3]", "azione applicabile: 1 [Goblin 2]", "STATO BAGNATO: 1 [Goblin 2]";
- **le misure dell'ordinamento**: "MENO_SALUTE (Goblin 1 60, Goblin 2 14): Goblin 2";
- **il criterio dell'incantesimo di danno** con i danni attesi confrontati: "PIU_ADATTO: FUOCO 4.2, FULMINE 9.8", con il danno delle armi se l'azione lo confronta; per `PIU_ECONOMICO_CHE_BASTA` se ognuno basta a uccidere;
- **il ripiego**: "nessuna voce eseguibile: attacco fisico al primo avversario".

Oggi la spiegazione si costruisce sempre (qualche stringa per decisione): se pesasse, si costruirà solo con un ascoltatore o il log acceso. **Non sostituisce ancora** `InternoRisultatoValutazionePersonaggioAttaccante`, che registra la decisione dei mostri senza tattica in
`PersonaggioBase.scegliIncantesimoContro` (otto punti di pubblicazione): quando ci saranno le tattiche base dei mostri quel codice e quell'evento spariscono (con l'enum `TipoRisultatoValutazioneAttaccante` e le sue nove etichette, che diventano frasi della spiegazione), e
`InternoRisultatoValutazionePersonaggioAttaccante` esce dalle eccezioni di `DipendenzeEventiTest`.

La UI potrà mostrare la spiegazione al giocatore per aiutarlo a mettere a punto le sue tattiche (la schermata del giocatore, ancora da fare).

## 8. Che cosa manca

Il modello, il motore, il valutatore, il salvataggio e il registro delle decisioni ci sono (§11). Restano:

- **Le tattiche base dei mostri**: un elemento per classe (§4) nello stesso formato di quelle del giocatore (§11.12), in un file di risorse per classe, e il modo di assegnarle a un avversario quando nasce (`FabbricaPersonaggi`), con la possibilità che
  una singola istanza ne abbia una sua al posto di quella di classe (si salva già con il personaggio). Con le tattiche base spariscono `PersonaggioBase.scegliIncantesimoContro`, `InternoRisultatoValutazionePersonaggioAttaccante` e `TipoRisultatoValutazioneAttaccante` (§7.7).
  Prima, le fasce di astuzia (§4, §11.10) e i ritocchi alle intelligenze massime del Folletto (a 8) e dell'Ombra Nera (a 31).
- **La scheda del giocatore**: la schermata per modificare la tattica di ogni personaggio (elenco di voci, editor di selettore, condizioni e azioni, menu e valori guidati, modelli per classe) e il comando che la manda al motore.
- **L'esito delle azioni con effetti che non si applicano** (un Silenzio a segno che non mette `SILENZIATO`) come "mancata", oggi non registrato.
- Il **costo della spiegazione** delle decisioni, se pesasse (§7.7).

Non ha senso importare: posizione e distanza, mischia contro distanza, linea di vista, incantesimi con tempo di lancio, movimento.

## 9. Osservazioni

- Oggi la scelta del **bersaglio** non esiste (si attacca sempre il primo avversario vivo): le tattiche la introducono per tutte le
  azioni singole.
- Il round a coppie cambia il **bilanciamento** (§3.1): un compagno solo contro più avversari li subisce tutti, e un gruppo numeroso contro un avversario solo gli dà più azioni; il
  bersaglio dell'avversario non è più chi ha appena agito. Le simulazioni (`SimulazionePartiteTest` con `GiocatoreAutomatico`, `TestScontriDiGruppo`,
  `TestLogoramentoInizioPartita`) sono state **portate al piano del round** (`RoundASimulato` lo replica per quelle che lavorano su liste di personaggi) e
  hanno dato questi numeri (ottobre 2026, 30 partite per classe):
  - **Gruppi.** Con tutti che agiscono a ogni round, i gruppi di 4-5 personaggi vincono molto di più di prima (contro il Drago a livello 3: 20% → 62% col gruppo di cinque), e i gruppi
    senza maghi non crollano più; un personaggio **da solo** è invece cambiato poco (`TestLogoramentoInizioPartita`: il piano è "agisce lui, poi tutti i mostri"), tranne contro chi
    atterra o stordisce, perché ora salta il turno (Folletto da solo: 91% → 75%). Reclutare conviene quindi ancora di più.
  - **Stima del giocatore automatico: tarata.** Giocando sempre fino in fondo (`GiocatoreAutomatico.sempreInCombattimento`) e confrontando previsione e realtà
    (tabella "Taratura della stima" di `SimulazionePartiteTest`) la stima aveva buon potere predittivo (correlazione 0,6-0,7) ma prevedeva 2,4 volte i danni veri: ora i danni
    degli avversari sono scalati di 0,45 (`FATTORE_DANNI_AVVERSARI`) e la probabilità che il capo sopravviva è quella misurata (`probabilitaVittoria`). Con la stima
    esatta il giocatore moriva prima (la vecchia pessimismo era una prudenza utile): le soglie (combattere sotto carico 0,1, fuggire se la vittoria è sotto il 95% e la fuga è
    sopportabile, in combattimento fuggire sopra 0,3) sono state cercate con una griglia di partite.
  - **Partite intere (30 per classe, giocatore tarato).** Il Guerriero arriva al limite di passi in 28 partite su 30 (livello 7), Ladro e Elfo in più di metà; il Mago muore
    quasi sempre (27 su 30, livello 4-5), il Sacerdote in 21. Il capo muore soprattutto per l'Eremita (livello 1-3) e, per Mago e Sacerdote, per Chimere e Chimere Drago.
  - **Da bilanciare.** Mago e Sacerdote sono i bersagli preferiti (`PersonaggioBase.scegliBersaglio`) e muoiono presto; l'Eremita resta il killer dei livelli bassi.

## 10. Da fare / idee aperte

Domande aperte:

- **La sintassi vera** del formato delle tattiche (§7.6) e le **variabili locali**, se serviranno.
- **Il formato** salvabile (testo semplice nel salvataggio, con le tattiche di ogni personaggio e le sue personalizzazioni — vedi il requisito in §1 — e in un file di modelli per classe) e la **scheda** secondaria del
  personaggio (elenco ordinabile con aggiungi, togli, sposta su e giù, attiva/disattiva).
- **Livello di dettaglio dei controlli** per la prima versione (probabilmente: sempre, salute e magia sotto una soglia, stati, classe,
  numero di avversari, risorse, probabilità; poi le categorie di creatura, già nei dati).
- **Cambiare arma** (§5): affinare quale arma esce con la doppia arma (oggi la più debole per danno atteso, contro un bersaglio neutro): contro il bersaglio vero, con gli effetti di stato, i modificatori e la probabilità di colpire? Per quanto tempo valgono le valutazioni già fatte delle armi del gruppo? Le voci per tornare all'arma di prima le scrive il giocatore o le propone il sistema in coppia con la prima?
- **Il round a coppie** (§3.1, fatto): rimisurare il bilanciamento, e decidere se ridurre l'annuncio "X attacca Y" a ogni azione (oggi sempre, anche per aiutare a leggere il log); la velocità del `TIMER` (ogni scatto è mezzo scambio) è rimasta com'era.

## 11. Il modello dati delle tattiche

Qui si fissano le decisioni sul **modello** (come sono fatte una tattica, una voce, un bersaglio); la sintassi del file viene di conseguenza. Le classi stanno nel pacchetto `…/tattiche` (i tipi, che servono anche alla UI, in `…/tipi/tattiche`).

### 11.1 Principi (decisi)

- **Tutto a enumerati**: azioni, rosa, filtri, ordinamenti, predicati. Nel file e nel salvataggio una voce usa il **nome dell'enumerato** (`CANTO_DI_VALORE`, `SALUTE_SOTTO`...).
- **Lista piatta, niente blocchi.** Una tattica è una lista ordinata di voci; la prima eseguibile vince. I blocchi `SE / ALTRIMENTI / FINE` e i blocchi con nome sono **tolti**: complicano la scheda
  del giocatore senza aggiungere nulla (un blocco equivale alla sua condizione in AND su ogni voce interna). La fascia di astuzia è una condizione della voce (`ASTUZIA_ALMENO n`);
  se due classi hanno la stessa tattica, si **copia**.
- **La tattica sceglie, non esegue** (§1): il valutatore restituisce una decisione (azione, bersaglio) che l'automa trasforma nei comandi di sempre.
- **Il bersaglio non è un campo fisso della voce**: lo decide l'azione (§11.2).

### 11.2 La voce

```
VoceMD      { SelettoreMD selettore?; List<PredicatoMD> condizioni; List<AzioneMD> azioni; boolean attiva }   (la prima azione applicabile vince, §11.7)
AzioneMD    { TipoAzione tipo; CriterioIncantesimo criterio?; boolean soloSePiuForteDellAttacco; TipoDanno armaDi? }   (§11.9, §11.10)
SelettoreMD { TipoRosa rosa; List<FiltroMD> filtri; OrdinamentoMD ordinamento }
FiltroMD    { TipoFiltro tipo; boolean negato; parametro? }                   (§11.5)
PredicatoMD { TipoPredicato tipo; boolean negato; parametri? }                (§11.8; le condizioni sono in AND, lista vuota = sempre)
TatticaMD   { List<VoceMD> voci }
```

Ogni `TipoAzione` dichiara la sua **portata**, e quindi se e come usa il selettore:

| Portata | Esempi | Il selettore |
| :--- | :--- | :--- |
| un avversario | `ATTACCO_FISICO`, `INCANTESIMO_DANNO`, `SILENZIO`, `DARDO_ARCANO` | sceglie il bersaglio |
| un alleato | `POZIONE_SALUTE`, `CANTO_CURATIVO` | sceglie il bersaglio |
| un alleato caduto | `RESURREZIONE` | sceglie il bersaglio (rosa `ALLEATI_CADUTI`) |
| tutto il gruppo alleato | `CANTO_DI_VALORE`, `ALBA_SACRA` | **non sceglie nessuno**: è solo un test di esistenza ("c'è almeno un candidato?") |
| tutti gli avversari | `CANTO_IPNOTICO`, `URLO_BANSHEE` | idem |
| un'arma | `CAMBIA_ARMA` | assente: l'azione ha il suo parametro (`armaDi`, il tipo di danno voluto) |

**Il selettore è anche una condizione**: se non resta nessun candidato la voce non è eseguibile. Così "c'è un alleato con meno di 50 punti" è il selettore stesso, e non servono variabili locali.
Esempi: `SU ALLEATI [SENZA_STATO ISPIRATO] → CANTO_DI_VALORE` (si salta da sola quando tutti sono ispirati, e riparte quando l'ispirazione scade);
`SU SE_STESSO [SALUTE MINORE 50] → POZIONE_SALUTE`; `SU ALLEATI_E_OSPITI ordine MENO_SALUTE → POZIONE_SALUTE`;
`SU AVVERSARI [MAGICO] ordine PIU_MAGIA → SILENZIO, FULMINE, ATTACCO_FISICO` (§11.7).

Le **condizioni** (§11.8) restano per ciò che non riguarda un bersaglio: `ROUND`, `ASTUZIA`, `QUANTI`... Lo stesso controllo (per esempio la salute) può stare in
un filtro (si applica al candidato) o in una condizione (si applica all'attore o a chi indica).

### 11.3 La rosa dei candidati

La rosa è l'insieme da cui il selettore pesca, prima dei filtri. Il suo nome è un enumerato (`tipi/tattiche/TipoRosa`, che serve anche alla UI), e chi calcola i candidati è la classe
corrispondente (`tattiche/Rosa`, una per valore: `RosaAlleati`, `RosaOspiti`..., si trovano con `Rose.di(tipo)`); il metodo restituisce una **`List<Personaggio>`** (non uno `Stream`: il registro delle decisioni, §7.7, deve
poter dire quanti candidati c'erano e quanti ne sono rimasti dopo ogni filtro; le liste sono piccole).

```java
enum TipoRosa { SE_STESSO, ALLEATI, ALLEATI_CADUTI, AVVERSARI, OSPITI, ALLEATI_E_OSPITI, AVVERSARI_E_OSPITI }

interface Rosa {                                   // una classe per valore, senza stato
    TipoRosa getTipo();
    List<Personaggio> candidati(ContestoTattico contesto);   // lista nuova, non modificabile
}
```
**Fatto** (con `RosaTest` e `ContestoTatticoTest`); `TipoRosa.valePer(attoreDelGiocatore)` dice per quale lato vale ogni rosa, e `candidati` solleva `IllegalStateException` se usato dal lato sbagliato.

| Rosa | Candidati | Per chi vale |
| :--- | :--- | :--- |
| `SE_STESSO` | l'attore | tutti |
| `ALLEATI` | i compagni vivi e in campo (l'attore compreso) | tutti |
| `ALLEATI_CADUTI` | i compagni caduti (non in panchina) | solo le azioni che lo dichiarano (`RESURREZIONE`) |
| `AVVERSARI` | i vivi e in campo dell'altro gruppo | tutti |
| `OSPITI` | gli **ospiti vulnerabili** vivi del gruppo del giocatore (`getOspitiBersaglio`) | tutti: per i miei sono da proteggere, per i mostri bersagli |
| `ALLEATI_E_OSPITI` | alleati poi ospiti | **solo i miei personaggi** |
| `AVVERSARI_E_OSPITI` | avversari poi ospiti | **solo i mostri**: riproduce la scelta a caso di oggi (`PersonaggioBase.scegliBersaglio`) |

Regole:

- Una rosa restituisce **solo chi è in grado di essere scelto** (vivo e in campo, il filtro F1 del §7.5 è qui dentro), tranne `ALLEATI_CADUTI`. Nell'ordine del gruppo, così "il primo" e le parità sono ripetibili; nelle
  unioni prima i personaggi, poi gli ospiti.
- **Gli ospiti** non agiscono e non hanno una tattica: sono solo candidati. Solo quelli **vulnerabili** vivi entrano nelle rose; i filtri su classe e magia non li comprendono mai (§7.5, F1), quelli su salute e stati sì.
- Ogni azione dichiara **quali rose accetta** (un attacco: `AVVERSARI` o `AVVERSARI_E_OSPITI` per i mostri, `OSPITI`; una cura: `SE_STESSO`, `ALLEATI`, `OSPITI`, `ALLEATI_E_OSPITI`); un uso che non torna
  (un mio personaggio che attacca gli ospiti, un mostro con `ALLEATI_E_OSPITI`) si rifiuta alla lettura.
- Ogni voce ha **una sola rosa**; l'unione di più insiemi è una rosa a sé.

### 11.4 Il contesto tattico

Tutto ciò che una rosa, un filtro, un predicato o un ordinamento deve poter *leggere* per decidere, raccolto in un oggetto **di sola lettura** creato per ogni decisione e buttato dopo. Porta: l'**attore**, il suo
gruppo e quello degli avversari (e quindi di che lato è), gli **ospiti vulnerabili**, lo **`StatoDelCombattimento`** (round, schede, conoscenza sulle reazioni, §3.3) e le **scorte del gruppo**
(pozioni, pergamene). È l'unico modo in cui gli enumerati toccano il gioco: così restano senza stato, e lo stesso enumerato serve a un personaggio e a un mostro, perché il contesto sa chi è "alleato" e chi "avversario"
per l'attore. Non modifica niente (§1): chi cambia il modello è l'automa, dopo.

Il contesto espone anche, durante la valutazione di una voce, l'**azione che si sta considerando** (per i filtri che dipendono da essa, `DEBOLE_ALL_AZIONE`).

### 11.5 I filtri

Un filtro è una domanda a cui un candidato risponde sì o no: `ammette(candidato, contesto, parametro)`. Regole (decise):

- **Pipeline.** Parte la rosa, si applicano i filtri uno dopo l'altro (sono in AND, quindi l'ordine non conta), poi l'ordinamento (§11.6) sceglie *uno* fra i rimasti. Se non resta nessuno la voce non è
  eseguibile. Il registro delle decisioni (§7.7) annota quanti candidati restano dopo ogni passo.
- **Senza stato**: enumerati come le rose. Quello che serve ricordare (l'ultimo bersaglio, chi ha già lanciato) lo leggono dalle schede dello `StatoDelCombattimento` attraverso il contesto.
- **Negazione**: ogni filtro ha un flag `negato` (nel file `NON`): "senza lo stato X" è `NON STATO X`, "non immune a Y" è `NON IMMUNE_A Y`. Niente coppie di enumerati.
- **Parametri tipizzati**: ogni `TipoFiltro` dichiara il tipo del suo parametro (`NESSUNO`, `NUMERO`, `STATO`, `CLASSE`, `CATEGORIA`, `TIPO_DANNO`); la lettura controlla che ci sia e sia giusto, e la scheda del giocatore mostra l'editor
  adatto. I filtri numerici prendono un `Confronto` (`MINORE`, `UGUALE` o `MAGGIORE`: con `NON` si ottengono "almeno" e "al massimo") e un numero: `SALUTE MINORE 50`.
- **La classe è il ruolo**: `MAGO` e `MAGA` sono due `TipoPersonaggio`, ma il filtro `CLASSE` ammette entrambe le varianti (Mago/Maga, Elfo/Elfa, Ladro/Ladra, Guerriero/Guerriera, Bardo/Cantastorie,
  Sacerdote/Sacerdotessa): serve una piccola mappa delle coppie in `TipoPersonaggio` (oggi `ProduttoreDiTestiCasuale` le gestisce a mano).
- **Il giocatore sceglie i filtri tattici, l'azione porta i suoi requisiti** (§11.7): per scartare gli immuni o i già silenziati non serve scrivere un filtro.

| Filtro | Parametro | Ammette |
| :--- | :--- | :--- |
| `SALUTE` | Confronto + punti | salute attuale in punti |
| `SALUTE_PERCENTUALE` | Confronto + % | salute su massima |
| `MAGIA` | Confronto + punti | `MAGIA` attuale |
| `LIVELLO` | Confronto + numero | livello assoluto |
| `LIVELLO_RISPETTO_AL_MIO` | Confronto | livello più alto, uguale o più basso di quello dell'attore |
| `ARMA_CHE_DA` | tipo di danno | ha in mano un'arma che dà quel danno (`Arma.daTipoDanno`): "ho un'arma sacra" |
| `CLASSE` | classe (il ruolo) | `getClasse()`, entrambe le varianti |
| `CATEGORIA` | categoria | `isDellaCategoria` (basta una) |
| `STATO` | stato | `hasEffettoDiStato` |
| `MAGICO` | nessuno | F3 (§7.5): abilità innate, mostri magici, chi ha già lanciato |
| `DEBOLE_A` | tipo di danno | `moltiplicatoreAtteso` maggiore di 1 ("debole" è solo questo, §6.3) |
| `DEBOLE_ALL_AZIONE` | nessuno | come `DEBOLE_A` per il tipo di danno dell'azione della voce che si sta valutando (il contesto la espone con `conAzione`); l'attacco fisico non ne ha uno fisso, quindi non passa. Gli stati che l'azione stessa può causare (`moltiplicatoreAttesoDopo`) non contano per il suo stesso colpo: servono a chi sceglie l'azione *seguente* |
| `IMMUNE_A` | tipo di danno | moltiplicatore 0 (con `NON`: "non immune") |
| `IL_CAPO` | nessuno | `getCapo()` del gruppo |
| `ME_STESSO` | nessuno | solo l'attore (con `NON`: "un altro che me") |
| `SCELTO_L_ULTIMA_VOLTA` | nessuno | F8: l'ultimo bersaglio di questo personaggio, se ancora valido |
| `MI_HA_ATTACCATO_PER_ULTIMO` | nessuno | F9 |

### 11.6 Gli ordinamenti

Un enumerato a parte (`TipoOrdinamento`), che sceglie *uno* fra i candidati rimasti; a parità vince il primo della rosa (ripetibile). Valori: `PRIMO`, `A_CASO` (usa il `Dado`, quindi si fissa col seme nei test), `MENO_SALUTE`,
`PIU_SALUTE`, `LIVELLO_PIU_BASSO`, `LIVELLO_PIU_ALTO`, `PIU_DEBOLE_A` e `PIU_RESISTENTE_A` (tipo di danno), `PIU_MAGIA`, `MENO_MAGIA`. **Niente `PRIORITA_DI_CLASSE`**: la scelta di oggi dei mostri (il Mago più potente, poi il
Sacerdote, l'Elfo, il Bardo, poi a caso) si ottiene con più voci, una per classe; sono tattiche di mostri, che il giocatore non modifica.

### 11.7 Le azioni: sequenze, requisiti e fallimenti

**Una voce può avere più azioni**, in sequenza: `SU AVVERSARI [MAGICO] ordine PIU_MAGIA → SILENZIO, FULMINE, ATTACCO_FISICO` vuol dire "silenziarlo se posso, altrimenti un fulmine, altrimenti attaccarlo". Il selettore sceglie
*un* bersaglio e si esegue la prima azione della lista **applicabile a lui**; un candidato entra nella rosa solo se almeno una delle azioni si può fare su di lui. È equivalente a più voci con lo stesso selettore, ma il bersaglio
si sceglie una volta sola e la scheda resta corta.

**I requisiti dell'azione.** Ogni `TipoAzione` dichiara quando è applicabile a un candidato, e il valutatore li applica da sé (come filtri automatici): il Silenzio scarta silenziati e immuni, una cura scarta chi ha la
salute piena, la Resurrezione vuole un caduto, un'azione con costo vuole la `MAGIA` o la pergamena. Il giocatore scrive solo i filtri tattici.

**Che cosa si sa delle immunità** (deciso):

- **Le immunità e resistenze di classe si conoscono fin da subito**, per tutti (giocatore e mostri): sono nei dati (`getMoltiplicatoreDannoSubito`, `isImmuneAIncantesimo`, `isImmuneAEffetto` di classe), e scartano l'azione prima di
  provarla. Un mostro non lancia il Silenzio sul Lich, che ne è immune.
- **Quelle date da un artefatto non si conoscono**: si **scoprono**. La prima volta che un'azione va a vuoto per immunità o resistenza (la reazione "Immune a X", "Resiste a X" che la UI già mostra) lo `StatoDelCombattimento` se la
  ricorda, e da lì in poi si tiene conto: è conoscenza **pubblica**, come quella sulle reazioni dell'equipaggiamento (§3.3), e vale per tutti i personaggi dei due gruppi. Va per (personaggio colpito, tipo di danno o azione).
  Un'immunità scoperta scarta subito l'azione, senza altri tentativi.

**Quando il tiro fallisce per caso** (un Silenzio mancato) non c'è nessuna scoperta, ma non si riprova all'infinito: lo `StatoDelCombattimento` ricorda i **tentativi falliti** per (attore, bersaglio, azione), perché il tiro dipende da chi
lancia; dopo **2** fallimenti (`Costanti`, per cominciare) l'azione non è più applicabile a quel bersaglio per quell'attore e la sequenza passa alla successiva. Il conto riparte a ogni locazione, come il resto dello stato. Un'azione
riuscita azzera il conto.

**Da fare nel motore** per questo: l'azione deve riferire il suo **esito** alla scheda (`colpito`, `mancato`, `immune`/`resistito`), dove oggi si registra solo il colpo andato a segno (`applicaRisultatoCombattimento` →
`registraColpo`); servono `registraEsito(attore, bersaglio, azione, esito)` e le due letture (tentativi falliti, immunità scoperta).

### 11.8 Le condizioni

Una voce ha una **lista di condizioni** (`Predicato`), tutte in **AND**; ognuna ha il flag `negato` (`NON`), come i filtri. Niente alberi con E, O e parentesi: l'O si ottiene con due voci (deciso, per tenere la scheda una lista).
Lista vuota = sempre. Le condizioni servono per ciò che **non è un controllo su un personaggio**: quelli sono filtri (§11.5) di un selettore (`SU SE_STESSO [SALUTE MINORE 50]`), e i requisiti dell'azione (la magia, la pozione, la
pergamena, l'arma da prendere) li porta l'azione (§11.7), quindi non servono né `HO_POZIONE` né `PERGAMENE`. Come per i filtri, ogni `TipoPredicato` dichiara il tipo del parametro e la lettura lo controlla.

`Confronto` ha tre valori, `MINORE`, `UGUALE`, `MAGGIORE`, ed è lo stesso dei filtri numerici: "almeno 3" è `NON MINORE 3`, "al massimo 3" è `NON MAGGIORE 3`.

| Predicato | Parametro | Significato |
| :--- | :--- | :--- |
| `QUANTI` | rosa + filtri + Confronto + numero | quanti candidati ci sono nella rosa dopo i filtri |
| `ROUND` | Confronto + numero | il numero del round (`StatoDelCombattimento.getRound`) |
| `ASTUZIA` | Confronto + numero | l'astuzia di chi decide (§4, rampa sul livello) |
| `PROBABILITA` | percentuale | tira il `Dado`: vero nel n% dei casi |
| `VOCE_USATA` | Confronto + numero | quante volte questa voce è stata usata da questo personaggio nello scontro (`registraUsoVoce`) |
| `ROUND_DALL_ULTIMO_USO` | Confronto + numero | i round passati dall'ultimo uso di questa voce (il *cooldown* delle future abilità forti) |

`QUANTI` è un piccolo selettore dentro una condizione (la scheda riusa l'editor dei filtri) e copre l'esistenza e i conteggi: "c'è un non morto fra gli avversari" è `QUANTI AVVERSARI [CATEGORIA NON_MORTO] MAGGIORE 0`
(con `NON`: "non ce ne sono"); "gli avversari sono almeno 3" è `NON MINORE 3` con `QUANTI AVVERSARI`; "sono l'unico vivo" è `QUANTI ALLEATI UGUALE 1`; un controllo sui propri stati è `QUANTI SE_STESSO [STATO X] UGUALE 1`.
Gli esempi: la mossa d'apertura è `ROUND UGUALE 1`; "una volta per scontro" è `VOCE_USATA UGUALE 0`; "una volta su tre" è `PROBABILITA 33`; la voce di una fascia di astuzia è `NON ASTUZIA MINORE 20`;
per tornare all'arma di prima, `QUANTI AVVERSARI [CATEGORIA NON_MORTO] UGUALE 0` e `SU SE_STESSO [ARMA_CHE_DA SACRO] → CAMBIA_ARMA`.

**Fuori dalla prima versione** (si aggiungono dopo, sono solo valori in più dell'enumerato): la stanchezza, i modificatori temporanei attivi, il tipo di locazione e l'ora, il duello, l'ondata successiva, il confronto fra il numero
degli avversari e dei miei compagni.

### 11.9 Le azioni: l'elenco

Le azioni sono un enumerato `TipoAzione` con il **nome piatto**, uno per incantesimo (`FULMINE`, non `INCANTESIMO` con un parametro): così una voce si legge come la si pensa, `SU AVVERSARI [STATO BAGNATO] → FULMINE`.
Ogni valore dichiara la sua **portata** (§11.2), le **rose che accetta** e i suoi **requisiti**. Sono ventidue:

| Gruppo | `TipoAzione` | Portata | Rose accettate |
| :--- | :--- | :--- | :--- |
| Mischia | `ATTACCO_FISICO` | un avversario | `AVVERSARI`; per i mostri anche `AVVERSARI_E_OSPITI` e `OSPITI` |
| Incantesimi di danno | `ARIA`, `ACQUA`, `TERRA`, `FUOCO`, `FULMINE`, `GELO`, `VELENO` | un avversario | come l'attacco |
| | `MORTE` | un avversario vivo | come l'attacco |
| | `SILENZIO` | un avversario | come l'attacco |
| Incantesimo "del repertorio" | `INCANTESIMO_DI_DANNO` (parametro: criterio S1-S4, §7.5) | un avversario | come l'attacco |
| Innato | `DARDO_ARCANO` | un avversario | come l'attacco |
| Benefici | `RESURREZIONE` | un alleato caduto | `ALLEATI_CADUTI` |
| | `ALBA_SACRA` | tutto il gruppo | il selettore è solo un test di esistenza |
| Canti | `CANTO_CURATIVO` | un alleato | `SE_STESSO`, `ALLEATI`, `OSPITI`, `ALLEATI_E_OSPITI` |
| | `CANTO_IPNOTICO`, `URLO_DELLA_BANSHEE` | tutti gli avversari (fino a `NUMERO_BERSAGLI`) | test di esistenza |
| | `CANTO_DI_VALORE` | tutto il gruppo | test di esistenza |
| Pozioni | `POZIONE_SALUTE`, `POZIONE_MAGIA` | un alleato (di solito sé) | come `CANTO_CURATIVO` |
| Equipaggiamento | `CAMBIA_ARMA` (parametro: tipo di danno voluto) | un'arma | `SE_STESSO` (chi cambia arma) |

Escluse (§5): fuga, corruzione, amicizia, passare inosservati. **Non c'è un'azione "passa il turno"**: se nessuna azione di una lista è applicabile, il personaggio fa un attacco fisico (§7.6, il ripiego).

**I requisiti** (li porta l'azione, §11.7; il giocatore non li scrive):

- **Incantesimi di danno, `SILENZIO`, `MORTE`:** la pergamena della classe nel gruppo, la `MAGIA` per il costo, non essere `SILENZIATO`. Il bersaglio non deve essere immune a quel tipo di danno (`SILENZIO`: all'effetto `SILENZIATO`, e non già
  silenziato); `MORTE` vuole un bersaglio vivo.
- **`DARDO_ARCANO`, canti:** la `MAGIA`, e la classe che li sa (Mago, Maga, Elfo, Elfa; Bardo, Cantastorie, il bardo della locanda). `CANTO_CURATIVO` vuole qualcuno ferito, `CANTO_IPNOTICO` e `URLO_DELLA_BANSHEE` avversari vivi
  non già `SPAVENTATO` o `CONFUSO`, e l'Urlo che il gruppo l'abbia imparato.
- **`ALBA_SACRA`:** usa quella innata se il personaggio ce l'ha (Sacerdote, Sacerdotessa), altrimenti la pergamena; vuole almeno un alleato con uno stato negativo o un modificatore temporaneo da togliere.
- **`RESURREZIONE`:** la pergamena, la `MAGIA`, un compagno caduto (non in panchina).
- **Pozioni:** una nel gruppo; la Salute vuole un ferito, la Magia chi ha poca `MAGIA`.
- **`CAMBIA_ARMA`:** il controllo preventivo del cambio e il massimo di cambi (§5).
- Tutte: il bersaglio non è un'immunità **nota** (di classe o scoperta, §11.7) e non ha già `N` tentativi falliti.

**Decisioni:**

- **`INCANTESIMO_DI_DANNO`** sceglie fra i sette incantesimi elementali che il personaggio può lanciare adesso; **non comprende `MORTE`** (può ritorcersi contro chi la lancia), `SILENZIO` e i benefici, che restano azioni esplicite.
  Il criterio (S1-S4) è un parametro dell'azione.
- **Pozioni:** una sola azione per tipo; la scelta fra normale e grande la fa il valutatore (la grande se la salute mancante è almeno quella che dà la grande, altrimenti la normale).
- **Incantesimi a più bersagli** (`MULTIPLO`, fino a `NUMERO_BERSAGLI`): il selettore sceglie il **bersaglio principale** e gli altri sono i successivi vivi dell'elenco degli avversari.
- **Un test** controlla che ogni valore di `ClasseIncantesimo` abbia il suo `TipoAzione` (un nuovo incantesimo senza azione fa fallire il test).

**Da fare nel motore: il bersaglio scelto, per tutte le azioni.** Oggi ogni azione sceglie da sola il bersaglio: l'attacco colpisce il primo avversario vivo (`LocazioneBase.attaccaConLeArmi`), gli incantesimi i primi vivi, il dardo il primo, il Silenzio
il primo non silenziato (`Silenzio.scegliBersaglio`), il canto curativo il più ferito. Perché la tattica comandi davvero, ognuna deve accettare un **bersaglio scelto dall'esterno** (e, per i `MULTIPLO`, il principale): la modifica va fatta
azione per azione, con i comandi manuali di oggi che continuano a funzionare come ora (senza bersaglio scelto, il comportamento di sempre). Le pozioni accettano già chi beve (`consumaPozioneSalute(Personaggio)`).

### 11.9bis Stato dell'implementazione

**Dove stanno i dati e la logica** (§11.11): i dati in `…/modellodati/tattiche`, la logica in `…/tattiche`, i tipi in `…/tipi/tattiche`.

**Fatto** (`…/tattiche`, `…/tipi/tattiche`; test `RosaTest`, `ContestoTatticoTest`, `SelezioneTest`, `PredicatiTest`, `ValutatoreTest`):

- **Il "SU"**: le rose, i filtri (`Filtro`, `Filtri`), gli ordinamenti (`OrdinamentoMD`, `Ordinamenti`), il `SelettoreMD` applicato da `Selettori` con il suo `RisultatoSelezione` (scelto, rimasti, percorso). Il selettore accetta un'`Ammissione`: restano nella rosa solo i candidati su cui
  un'azione della voce si può fare, e i filtri che dipendono dall'azione la valutano per quella che si farebbe su ciascuno.
- **Le condizioni**: `TipoPredicato`, `PredicatoMD`, `Predicati`, l'astuzia (`Astuzia`, con `Costanti.ASTUZIA_PARTENZA` e `ASTUZIA_PER_LIVELLO`), il conteggio degli usi di una voce. Un `ROUND_DALL_ULTIMO_USO` di una voce mai usata vale "moltissimi".
- **Le azioni**: `TipoAzione`, `PortataAzione`, `AzioneMD` (con i parametri controllati), `Requisiti` (quando si può fare ciascuna, con il motivo del no), `CriterioIncantesimo` e `SceltaIncantesimo` (S1-S4 e `PIU_ECONOMICO_CHE_BASTA`, con il confronto con l'attacco),
  `CalcolatoreCombattimento.dannoAtteso` e `dannoAttesoDellaMischia` (senza dadi).
- **La voce e la tattica**: `VoceMD` (selettore opzionale con quello di base della portata, condizioni, azioni, attiva/disattiva, `problemi(lato)` che la lettura deve rifiutare), `TatticaMD` (lista piatta, `problemi(lato)`), `Decisione` (azione, bersaglio, perché),
  `Valutatore.decidi` (la prima voce eseguibile; ripiego sull'attacco fisico al primo avversario).
- **La memoria dello scontro**: `StatoDelCombattimento.registraEsitoAzione` (riuscita, mancata, immune), `getTentativiFalliti` (si rinuncia dopo `Costanti.MAX_TENTATIVI_FALLITI_PER_AZIONE` = 2) e `isImmunitaScoperta` (pubblica); le immunità di classe si conoscono subito.

**Scelte dei requisiti** (da rivedere se non convincono): un incantesimo si può lanciare se il personaggio non è silenziato, ha la `MAGIA` e ha una pergamena nel gruppo, oppure è un mostro magico che lo ha nel repertorio; l'Alba sacra serve se un alleato ha uno
stato diverso da `ISPIRATO` (i modificatori temporanei, oggi, non si leggono dal personaggio); i canti ipnotico e dell'Urlo servono se c'è un avversario né `SPAVENTATO` né `CONFUSO`; il Canto di valore se c'è un alleato non `ISPIRATO`; le pozioni vogliono una scorta nel
gruppo e un bersaglio ferito (Salute) o con poca `MAGIA`; il cambio d'arma solo per i miei, se c'è un'arma da prendere (`armiPerCambio`).

**Collegato al motore** (`LocazioneBase`, test `CombattimentoConTatticheTest`): a ogni scatto del TIMER il personaggio di turno agisce con la sua tattica (`Personaggio.getTattica`, salvata con il personaggio, §11.12): i miei senza tattica hanno `TatticaMD.VUOTA` (attaccano
il primo avversario, come prima), i mostri senza tattica scelgono ancora con `PersonaggioBase.attacca(Gruppo)` (finché non ci sono le tattiche base). `agisciConLaTattica` decide con il `Valutatore`, esegue la `Decisione` (`eseguiLaDecisione`: attacco con
le armi sul bersaglio scelto, incantesimi di danno con il `GruppoDiBersagli` per i `MULTIPLO`, dardo arcano, benefici, canti — il curativo sul bersaglio scelto, `Canto.cantaSu` —, pozioni con la scelta fra normale e grande, cambio d'arma), registra l'uso della voce
e controlla la fine dello scontro; non gestisce il round. Gli esiti degli incantesimi (colpito, mancato, immune) e del dardo si registrano nello `StatoDelCombattimento` (`IncantesimoMaleficoImpl.colpisci`, `scagliaIlDardo`); l'attacco fisico non ne registra
(non si abbandona mai). Le azioni manuali del giocatore (barra dei comandi) non sono cambiate.

**Da fare**: le tattiche base dei mostri e le fasce di astuzia (compresi i ritocchi alle intelligenze massime del Folletto, a 8, e dell'Ombra Nera, a 31); la scheda del
giocatore; l'esito di un'azione con effetti che non si applicano (un Silenzio a segno che non mette `SILENZIATO`) come "mancata".

### 11.11 Dove stanno i dati e la logica

Le classi che descrivono una tattica sono **dati puri**: immutabili, fatti solo di enumerati (`tipi/tattiche`), numeri e liste. Stanno in **`…/modellodati/tattiche`**, con il suffisso `MD` come il resto del modello dati
(`TatticaMD`, `VoceMD`, `SelettoreMD`, `FiltroMD`, `OrdinamentoMD`, `PredicatoMD`, `AzioneMD`). Il motivo è che la stessa classe serve a tre cose: la **UI** le legge e le modifica (può importare `modellodati` e `tipi`, non il motore né il dominio), il **salvataggio**
le scrive con il personaggio (`PersonaggioMD`, §8) e il **motore** le valuta. Un test (`DipendenzeTatticheTest`) controlla che dipendano solo da `tipi` e fra loro.

La **logica** sta in `…/tattiche`: le rose (`Rosa`, `Rose`), `Filtri`, `Ordinamenti`, `Predicati`, `Selettori` (applica un `SelettoreMD`), `Requisiti`, `SceltaIncantesimo`, `Astuzia`, `Valutatore`, il `ContestoTattico` e i risultati (`RisultatoSelezione`, `Decisione`). Non la
può importare la UI (`DipendenzeUITest`, che ora riconosce anche i sottopacchetti).

### 11.12 Il salvataggio

**Fatto** (`SalvataggioTatticheTest`, `CombattimentoConTatticheTest`). La tattica sta nel `PersonaggioMD` (`getTattica`/`setTattica`, `PersonaggioBase` ne è solo la porta) e passa dal salvataggio con il personaggio: dopo il suo record una riga vuota (nessuna tattica: i mostri, per ora) oppure la tattica. Una
tattica vuota (`TatticaMD.VUOTA`) resta una tattica vuota e non diventa "nessuna tattica": sono due cose diverse (i miei personaggi senza tattica usano la vuota, i mostri senza usano ancora le regole di `PersonaggioBase.attacca(Gruppo)`).

Il formato, nello stile del resto del modello dati (una riga per elemento, campi separati da `|`, enumerati per nome, campi mancanti vuoti, gli elementi che ne contengono altri dicono quanti sono):

```
TATTICA|numero di voci
VOCE|attiva|ha un selettore|numero di condizioni|numero di azioni
SELETTORE|rosa|ordinamento|tipo di danno dell'ordinamento|numero di filtri       (seguono i filtri)
FILTRO|tipo|negato|confronto|numero|stato|classe|categoria|tipo di danno
PREDICATO|tipo|negato|confronto|numero|rosa|numero di filtri                     (seguono i filtri)
AZIONE|tipo|criterio|solo se più forte dell'attacco|tipo di danno dell'arma
```

Gli elementi sono immutabili, quindi non implementano `Serializzabile` (che rilegge dentro un oggetto): hanno `salva(PrintWriter)` e un `leggi(BufferedReader)` statico che li costruisce con le stesse fabbriche che li controllano. Un salvataggio incoerente (un'etichetta sbagliata, un enumerato sconosciuto, un parametro mancante, una voce senza azioni,
il file troncato) è rifiutato con una `IOException`. Il test di andata e ritorno scrive una tattica con almeno un esemplare di ogni forma di filtro, predicato e azione e controlla che torni uguale (le classi hanno `equals`) e che riscrivere dia lo stesso testo; un
altro salva e ricarica una partita vera e controlla che il personaggio rilegga la tattica e agisca come impostato. I salvataggi precedenti non si leggono più, come per ogni cambio di formato.

### 11.10 Il criterio di scelta dell'incantesimo di danno

Vale per `INCANTESIMO_DI_DANNO` (§11.9), la sola azione che sceglie *quale* incantesimo lanciare. Nasce da `PersonaggioBase.scegliIncantesimoContro`, che oggi sceglie così: se non è magico, è `SILENZIATO`, ha 0 `MAGIA` o non può pagare
nessun incantesimo del repertorio, non lancia; una volta su tre rinuncia; poi, con `INTELLIGENZA` sotto 5 sceglie a caso, da 5 a 6 il più potente (il `getDanni()` più alto, senza guardare il bersaglio), da 7 il più potente solo se probabilità di
colpire per danno batte quella dell'arma (con `calcolaDannoRisultante`, che tira i dadi). Nelle tattiche:

- **Quelli che può lanciare adesso**: per un mostro il suo repertorio di incantesimi elementali che può pagare con la `MAGIA`; per un mio personaggio le pergamene della scorta del gruppo (`getIncantesimi(classe) > 0`) che sa
  formulare (`puoFormulare`) e può pagare. Non `SILENZIATO`. Si scartano gli incantesimi a cui il bersaglio è immune per una conoscenza **nota** (§11.7) e quelli con troppi tentativi falliti. Se non ne resta nessuno l'azione non è applicabile.
- **"Una volta su tre non lancia"** è la condizione `PROBABILITA 67` della voce.
- **Le soglie di `INTELLIGENZA`** (5 e 7) non si riproducono: le tattiche base dei mostri usano le fasce di astuzia (§4, 0-11, 12-19, 20-30, da 31) e si **ritoccano le fasce**, accettando che qualche decisione cambi rispetto a oggi.

Il **criterio** (`CriterioIncantesimo`) è un parametro dell'azione:

| Valore | Sigla | Che cosa sceglie |
| :--- | :--- | :--- |
| `A_CASO` | S1 | uno a caso fra quelli lanciabili |
| `PIU_POTENTE` | S2 | quello con più danno base (`getDanni()`, come oggi), senza resistenze né debolezze; con più bersagli conta il danno a uno solo |
| `PIU_ADATTO` | S3 | quello con il **danno atteso** più alto su *quel* bersaglio: probabilità di colpire × `dannoAttesoDellArma` × `moltiplicatoreAtteso` (debolezze e resistenze di classe e stati, §6.3); con più bersagli il danno totale |
| `PIU_ADATTO_ESPERTO` | S4 | come S3, moltiplicato per il fattore dell'equipaggiamento **osservato** (`getFattoreEquipaggiamentoNoto`, §3.3); con più bersagli conta anche chi verrebbe portato vicino alla fine (la quota di salute tolta a ogni bersaglio, al massimo 1, sommata) |
| `PIU_ECONOMICO_CHE_BASTA` | — | per chi ha poche pergamene: fra quelli il cui danno atteso (come S4) basta a **uccidere** il bersaglio, il meno caro (il prezzo della pergamena per i miei, il costo di lancio in `MAGIA` per i mostri); se nessuno basta, il `PIU_ADATTO_ESPERTO` |

**Il confronto con l'attacco fisico** è un secondo parametro, `soloSePiuForteDellAttacco`: se vero, l'incantesimo scelto è applicabile solo se il suo danno atteso (probabilità di colpire × danno × `moltiplicatoreAtteso`) è maggiore di quello delle fasi di
attacco con le armi sullo stesso bersaglio; altrimenti non lo è, e la sequenza passa all'azione successiva (di solito `ATTACCO_FISICO`). Il danno atteso dell'arma si calcola senza dadi come quello dell'incantesimo (oggi il confronto tira i dadi).

**Come ripartire le fasce di astuzia nella tattica base di un mostro magico** (da tarare con le simulazioni): la prima fascia (0-11) `PROBABILITA 67 → INCANTESIMO_DI_DANNO A_CASO`, la seconda (12-19) `PIU_POTENTE`, la terza (20-30) `PIU_ADATTO` con
`soloSePiuForteDellAttacco`, la quarta (da 31) `PIU_ADATTO_ESPERTO` con `soloSePiuForteDellAttacco`; sempre seguite da `ATTACCO_FISICO`.

**Fatto** (§11.9bis): `CalcolatoreCombattimento.dannoAtteso`, l'elenco dei lanciabili e i criteri (`SceltaIncantesimo`). Il registro delle decisioni (§7.7) deve riportare i danni attesi confrontati ("Gelo 14, Fulmine 31: Fulmine"): oggi la spiegazione dice solo
quale incantesimo sceglie.

**Da decidere:** il parametro di `PIU_ECONOMICO_CHE_BASTA` (basta se il danno atteso è almeno la salute attuale del bersaglio? o un margine?) e l'esatta ripartizione delle fasce, entrambi da tarare con le simulazioni.
