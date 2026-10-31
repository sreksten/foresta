# Tattiche di combattimento automatiche

Stato: **fatto**. Il documento descrive il combattimento a round e le tattiche automatiche, dei personaggi del giocatore e dei mostri, come stanno nel codice: il round a coppie (§3), le tattiche dei mostri (§4), le azioni (§5), i bersagli e le condizioni (§6, §7) e il modello dati con la schermata del giocatore (§11). Le tabelle di §6 e §7 confrontano ogni voce con i giochi di riferimento (*Dragon Age: Origins*, DA:O; *Final Fantasy XII*, FFXII; *Baldur's Gate*, BG) e dicono se il dato si legge dal modello di Foresta; quali siano diventati filtri, ordinamenti e condizioni lo dice §11.

## 1. L'idea

In combattimento **tutto il gruppo agisce a turno** (round-robin) e ogni personaggio decide da solo che cosa fare scorrendo la propria **tattica**: un elenco ordinato di **voci**, che il giocatore modifica nella schermata delle tattiche (§11.15), come le *Tactics* di *Dragon Age: Origins* o i *Gambit* di *Final Fantasy XII*. Le stesse tattiche valgono per i mostri (§4).

Ogni **voce** ha:

- un **selettore**, che dice su chi agire: una rosa di candidati ("gli avversari", "gli alleati", "se stesso"), dei filtri che la restringono ("magico", "con meno del 50% di salute") e un ordinamento che ne sceglie uno ("il primo", "con meno salute");
- delle **condizioni** facoltative, tutte in AND, su ciò che non riguarda un bersaglio ("al round 1", "con probabilità del 60%"; nessuna vale "sempre");
- una sequenza di **azioni** (attacco, un incantesimo, una pozione...): si esegue la prima che si può fare sul bersaglio scelto.

Il flusso è sempre lo stesso:

1. si scorre l'elenco dall'alto: l'ordine è la **priorità**;
2. la **prima voce che si può davvero eseguire** (c'è un bersaglio che corrisponde, le condizioni sono vere, c'è abbastanza magia, il
   personaggio ha l'incantesimo scelto...) viene eseguita, e l'azione del personaggio finisce lì;
3. se una voce non si può eseguire, si passa alla successiva;
4. se nessuna voce si può eseguire, il valutatore ripiega sull'**attacco fisico** al primo avversario, che non fallisce mai (nella schermata è l'ultima riga, che non si tocca).

Per esempio: "su un avversario magico: Dardo arcano" e subito dopo "sullo stesso: attacco". Dopo l'azione si passa al personaggio successivo (§3): gli avversari non rispondono, agiscono quando tocca a loro.

> **Requisito fondamentale: le tattiche si salvano e si rileggono con il personaggio.** Il giocatore le personalizza per ognuno dei suoi
> personaggi, e questa personalizzazione **è parte del personaggio**: finisce nel salvataggio con lui e, caricando la partita, ogni
> personaggio agisce esattamente come era stato impostato, senza perdere una voce. Lo stesso vale per la tattica diversa che una singola
> istanza di un mostro può portare (§4). Il formato è in §11.12.

> **Prerequisito: le tattiche non cambiano il modello dati, scelgono soltanto.** È l'**automa** (`Automa`, la locazione) a eseguire l'azione e a cambiare il modello.
> Con le tattiche **chi sceglie al posto del giocatore** è la tattica, ma quello che fa è lo stesso: **impartire il comando che avrebbe impartito il giocatore**, e
> l'automa lo esegue come sempre. Una tattica quindi *legge* lo stato (condizioni e selettori chiedono lo stato attuale, senza cambiarlo) e *produce una decisione*; non toglie salute, non consuma pergamene,
> non sposta oggetti, non aggiunge stati. Le azioni sono quelle eseguibili anche a mano (§5) e le regole del danno non cambiano. Vale per i personaggi del giocatore
> e per i mostri, e vale anche per le azioni a più passi: il cambio d'arma (§5) fa le due richieste di stoccaggio e di prelievo come le farebbe il giocatore
> a mano.

## 2. Il combattimento, per le tattiche

Vedi [`motore_di_gioco.md`](motore_di_gioco.md) §7. Quello che conta per le tattiche:

- Il combattimento procede **per round su una lista dei turni** (§3): a ogni `TIMER` agisce una sola volta il prossimo della lista, un mio personaggio o un
  avversario, alternati in proporzione. Chi agisce in automatico, dei miei o avversario, lo fa con la sua tattica (§11.14). I miei senza tattica
  propria usano la predefinita del loro ruolo, i mostri senza una tattica sua quella della classe (§11.16, `RegistroTattiche`). Non c'è una "risposta" a chi ha agito.
- Gli **incantesimi** e le **pozioni** sono del gruppo: le pergamene e le pozioni stanno nell'inventario del gruppo
  (`GruppoGiocatore.getIncantesimi(classe)`, `getPozioniSalute()`...), e chi li usa lo sceglie il giocatore (o il motore: `getFormulante`).
  Gli incantesimi **innati** (dardo arcano, alba sacra, i tre canti) non consumano pergamene.
- Non c'è **posizione**: né distanza, né mischia contro distanza, né linea di vista. Non c'è un turno di preparazione degli
  incantesimi (sono istantanei) né cooldown. Il bersaglio "persistente" è nella memoria dello scontro (l'ultimo scelto e chi ha attaccato per ultimo, §3.3).
- **Gli ospiti vulnerabili sono bersaglio di ogni azione degli avversari**: la scelta spetta alla tattica dell'avversario (§4), e la scelta a caso (`AVVERSARI_E_OSPITI`)
  comprende gli ospiti vulnerabili vivi (prima i maghi, i sacerdoti, gli elfi e i bardi).

## 3. I turni dei due gruppi

I turni seguono una **lista unica** dei personaggi dei due gruppi, fissata a inizio combattimento (§3.1).

**Il cursore.** `StatoDelCombattimento` tiene un cursore sulla lista: tocca a chi sta lì. Chi non può agire, cioè chi è morto o in panchina, un ospite che non combatte o chi ha già agito fuori
turno, **viene saltato**: il suo posto passa senza che succeda niente, e questo vale anche se cade o va in panchina durante il giro. Chi è incapace di agire (atterrato, congelato, stordito)
**perde il turno** ma il suo posto è usato. Il cursore avanza a ogni azione e non si azzera fra uno scontro e l'altro dentro la stessa locazione, tranne con una nuova ondata (§3.1).

**Non c'è una "risposta".** Quando un mio personaggio agisce, gli avversari **non rispondono**: ognuno **agisce quando la lista dei turni tocca a lui** (§3.1), e attacca
a sua volta scegliendo il bersaglio da sé. **Chi colpire non è una regola fissa**: lo decide la tattica dell'avversario (§4), come per i miei personaggi.

**Azioni manuali.** Agisce il personaggio scelto dal giocatore (`PERSONAGGIO_n`); fa **una** azione. Se non gli toccava e non ha ancora agito nel giro, il cursore resta dov'è e il
suo posto, quando arriverà, è saltato: nessuno perde il turno. Se agisce fuori turno **una seconda volta** nello stesso giro, l'azione costa il posto a cui tocca (altrimenti il giocatore
potrebbe agire all'infinito senza risposta).

**Eventi a metà round.** Chi muore durante il round non agisce più, e il cursore salta i caduti: se cade un compagno che doveva ancora
agire, il suo turno passa e si va al successivo. Se cade l'ultimo avversario il round finisce lì (lo scontro è vinto, nessuno
agisce più); se cade il capo del giocatore la partita è persa.

**Interruzione.** La **fuga** resta del giocatore: è un'interruzione manuale del combattimento (`INTERRUZIONE_COMBATTIMENTO`, o il
bottone di fuga) che rimette la scelta alle sue mani. Le tattiche non fuggono.

### 3.1 La lista dei turni

A inizio combattimento si fissa la lista di **tutti** i personaggi dei due gruppi: i miei, gli ospiti vulnerabili, gli avversari, anche chi è in panchina o è caduto
(`StatoDelCombattimento.getOrdineDelRound`, `getPiano` per i lati). I due gruppi si **alternano in proporzione**: il personaggio *i* di un gruppo di *n* ha la posizione *(i + ½)/n*, e la
lista è tutti i personaggi in ordine di posizione; a parità di posizione viene prima il mio gruppo, e la lista (circolare) parte dal mio primo personaggio. A pari numero sono le coppie (un mio, un avversario...); chi è in più si distribuisce
lungo il giro invece di restare in fondo, così non ci sono mai serie di tre dello stesso gruppo se l'altro non ne ha di più.

| Caso | Lista | Che cosa succede |
| :--- | :--- | :--- |
| 1 contro 1, 4 contro 4 | G A G A… | coppie |
| 3 miei contro 2 avversari | G A G A G | due coppie, e il mio terzo agisce **senza avversario** |
| 1 mio contro 3 avversari | G A A A | il mio agisce e poi **attaccano uno dopo l'altro** tutti e tre gli avversari: il mio personaggio ne subisce tre |
| 2 miei contro 5 avversari | G A A A G A A | |
| 4 miei contro 1 avversario | G G A G G | l'avversario sta in mezzo |
| 3 miei contro 5 avversari | G A G A A G A A | |
| 5 miei contro 3 avversari | G A G G A G A G | |

Con il comando `COMBATTIMENTO` il giocatore sceglie chi dei suoi comincia: la lista, che è circolare, riparte da lui, e il primo avversario a muoversi è quello che lo segue nel giro. La lista
**vale per tutto il combattimento**, anche dopo una morte o un passaggio in panchina (chi è fuori resta al suo posto, saltato). Si rifà solo quando arriva una **nuova ondata**
(`nuovaOndata`, senza cambiare il numero del round); chi arriva a combattimento in corso (lo Scheletro della Mietitura) va in fondo. È la stessa lista che mostra il riquadro del combattimento.

Non c'è un tiro di iniziativa: il gruppo del giocatore comincia sempre per primo (vedi [`todo.md`](todo.md)). È l'effetto voluto che un avversario solo contro tre compagni agisca una volta e subisca più azioni, e che un compagno solo contro tre avversari ne subisca tre. La regola "si disimpegna e
attacca" non esiste.

### 3.2 Combattimento automatico e azioni manuali

- **Automatico** (a ogni scatto del `TIMER`): **una sola azione per scatto**, del prossimo della lista, un mio personaggio o un avversario. Agiscono **tutti i miei vivi**, a turno
  (con il comando `COMBATTIMENTO` il giocatore sceglie chi comincia, `faAgirePerPrimo`); la finestra del combattimento mostra **chi sta agendo e il bersaglio** (`InternoAzioneInCombattimento`;
  senza bersaglio se l'azione non lo ha: una pozione, l'Alba Sacra, un canto). Ogni scatto è quindi mezzo scambio, e un round ne fa tanti quanti sono i posti della lista che possono agire.
- **Manuale** (attacco singolo, incantesimo, dardo, canto, pozione): il giocatore sceglie chi e che cosa; chi agisce fuori turno per la prima volta nel giro lo fa subito e il suo posto sarà
  saltato, e subito dopo gli avversari agiscono finché la lista non torna a un mio personaggio o il round non finisce (vedi "Azioni manuali" sopra).
- **Bere una pozione è un'azione del personaggio** e consuma il suo turno, e interrompe la mischia automatica, come l'incantesimo.
- **Gli effetti di stato scorrono alla fine del round** (quando la lista è finita), non a ogni azione: con 3 miei contro 1 durano di più in azioni del giocatore. Fuori dal piano (un
  tentativo di corruzione o di amicizia fallito, senza avversari) il turno passa come prima.
- **Gli avversari che attaccano per primi** (un passaggio inosservato fallito) sono fuori dalla lista: uno solo attacca (`getAvversarioCheAttaccaPerPrimo`).
### 3.3 Il round e lo stato del combattimento

**Che cos'è un round**: finisce quando la **lista dei turni** (§3.1) è finita, cioè quando il cursore ha percorso tutti i posti dei due gruppi (agendo o saltando). Il numero del round **riparte da 1 a ogni
inizio locazione** e **continua** quando arriva la prossima ondata: il combattimento e la locazione non sono cambiati, sono solo apparsi nuovi mostri. Se non ci sono più avversari
il combattimento è finito e il round non avanza. All'inizio di ogni round il motore pubblica un evento **`InternoInizioRound`** (con il numero), per la UI, i log e chi tiene la
memoria del combattimento; a fine round passano gli effetti di stato.

**Lo `StatoDelCombattimento`**: un oggetto che raccoglie in un posto solo tutto ciò che le tattiche devono ricordare di un combattimento. Si crea
(o si azzera) a ogni inizio locazione, insieme alla preparazione del gruppo avversario. Contiene:

- **del combattimento**: il numero del round, il registro di chi ha già agito nel round corrente, la **lista dei turni** e il **cursore** (§3.1). È memoria del combattimento e non modello, e vive
  come lo stato (azzerato a inizio locazione, non fra uno scontro e l'altro nella stessa locazione, tranne la lista con una nuova ondata). La lista tiene i **personaggi** e non indici: chi esce da un gruppo
  si toglie (e il cursore si corregge), chi arriva va in fondo;
- **per ogni personaggio** (una scheda, indicizzata per identità del personaggio): l'ultimo avversario e l'ultimo alleato scelti (F8); chi lo ha attaccato per
  ultimo (F9); se ha già lanciato un incantesimo (la regola del "magico", F3); quanti cambi d'arma ha fatto; le armi del gruppo già valutate; le voci già
  usate (quante volte e in che round); i tentativi falliti per (bersaglio, azione), §11.7;
- **la conoscenza accumulata sulle reazioni** (S4), **di pubblico dominio e non legata a un personaggio**: in combattimento tutti vedono quel che succede, nel bene e nel male, anche di
  sfuggita, quindi la sa chiunque e non ha un osservatore. Per ogni coppia (personaggio colpito, tipo di danno) si ricorda di quanto l'**equipaggiamento difensivo** ha cambiato il
  danno (`DannoRisultante.getFattoreEquipaggiamento`: il rapporto fra la mitigazione con i pezzi e quella con la sola difesa di base, `difesaContro` / `difesaBaseContro`; 1 se
  non conta, meno di 1 se protegge). È la parte che le tattiche non possono prevedere: classe e stati li dà già `moltiplicatoreAtteso`, e il fattore osservato si moltiplica per
  quello. Lo registra ogni colpo andato a segno (`ApplicazioneDelRisultato.applica` → `registraColpo`; le previsioni delle tattiche, che chiamano il calcolo senza
  applicarlo, non lasciano tracce); una nuova osservazione sostituisce la vecchia; `getFattoreEquipaggiamentoNoto` dà 1 se non si è visto niente. Pubbliche sono anche le **immunità scoperte** (`isImmunitaScoperta`, §11.7).

Regole (decise):

- **Che cosa è fatto e dove si aggiorna**: i colpi (ultimo avversario, ultimo attaccante, reazione) in `ApplicazioneDelRisultato.applica`; "ha lanciato" in `formula` degli incantesimi, nel canto e nel dardo arcano; l'ultimo alleato nella Resurrezione. I cambi d'arma e le armi valutate li registra `GruppoGiocatore.cambiaArma` / `puoCambiareArma` (§5); le voci usate le registra l'`EsecutoreTattiche` (`registraUsoVoce`, §11.14), gli esiti delle azioni (`registraEsitoAzione`) gli incantesimi e il dardo.
- **Non si salva**: un combattimento non si interrompe mai per salvare (il comando non è disponibile), quindi lo stato è transitorio e si perde con il
  combattimento. Le tattiche di ogni personaggio, invece, sì (§1).
- **La conoscenza è pubblica**, anche quella sulle immunità scoperte (§11.7): vale per i personaggi del giocatore e per i mostri, e quello che si è imparato sul comportamento di un personaggio lo sa chiunque, in
  entrambi i gruppi.
- **La scheda di un personaggio non si dimentica quando cade**: un compagno potrebbe farlo risorgere, e la conoscenza sulle sue reazioni deve valere ancora. Si
  azzera solo con l'inizio della locazione; i mostri di una nuova ondata partono con una scheda vuota.
- **Non è il modello dati**: è la memoria tattica. La aggiornano il motore e la locazione (o l'oggetto stesso, ascoltando gli eventi come `InternoInizioRound`);
  le tattiche la *leggono* e basta, come vuole il prerequisito del §1.

**Una conseguenza:** il round è lungo quanto la lista (tutti i personaggi dei due gruppi), non un giro di un gruppo solo. Le condizioni sul round e i cooldown contano quindi "giri completi dei due gruppi".

## 4. Le tattiche degli avversari

**Lo stesso modello.** Ogni mostro agisce con una tattica fatta di voci, valutata come quella dei compagni: la prima voce eseguibile vince. Le azioni sono quelle del suo repertorio
(l'attacco con l'arma naturale, i suoi incantesimi malefici: `INCANTESIMO_DEL_REPERTORIO`); le rose sono quelle dei mostri (`AVVERSARI`, `AVVERSARI_E_OSPITI`...), letti dal punto di vista
dell'avversario (per lui gli "avversari" sono i miei personaggi).

**Mostri stupidi e mostri intelligenti.** Un mostro stupido colpisce il primo che trova o chi l'ha colpito; uno intelligente finisce i feriti, per ridurre gli attacchi del giro dopo; uno ancora più furbo mette a tacere un mago.
Ogni mostro ha la sua **tattica base**, **per classe** (`TipoPersonaggio`): la lista è una sola, e le **fasce di astuzia** sono condizioni delle voci (`NON ASTUZIA MINORE soglia`, §11.8), non blocchi. Per esempio:

- *bruto* (Minotauro, Goblin, Scheletro, Troll...): poche voci, "scelgo a caso, attacco fisico, punto";
- *astuto* (Arpia, Hobgoblin, Chimera...): in più "finisco chi è quasi caduto, cerco un magico", e un incantesimo se ha la `MAGIA`;
- *stratega* (Strega, Lich, Drago): "silenzio subito il magico con più `MAGIA`", la Morte sul quasi caduto, poi l'incantesimo più adatto al bersaglio.

Le tattiche base sono dati (voci come quelle del giocatore: il motore che le esegue è lo stesso), scritte in `tattiche/TatticheBase` (§11.16) e non in un file di risorse: in Java si controllano a compilazione e si ripetono fra classi senza copiarle. I boss hanno la loro, perché le classi sono già distinte.

**La tattica generale è quella di classe** (`RegistroTattiche.perClasse`), e non cambia per livello del mondo o per luogo: l'astuzia (intelligenza di classe con una rampa sul livello) e le
condizioni dentro la lista fanno già variare il comportamento, e i boss hanno la loro intelligenza. Una **singola istanza** può però portarne una
diversa (`TatticheDeiPersonaggi.imposta`), per esempio in una missione un goblin più furbo degli altri con una tattica tutta sua; la tattica dell'istanza si salva con la partita (§1, §11.12).

**L'astuzia e le soglie.** Le soglie dei quattro livelli sono: 1 = da 0 a 11, 2 = da 12 a 19, 3 = da 20 a 30, 4 = da 31 (`SOGLIA_ASTUTO`, `SOGLIA_ESPERTO` e `SOGLIA_STRATEGA` in `TatticheBase`). Il **Folletto** (massimo 8)
sta fra il Goblin e l'Hobgoblin e l'**Ombra Nera** (31) nel quarto livello. Il massimo sta in `*_MAX_INTELLIGENZA` di `CostantiPersonaggi`, che serve anche a calcolare l'INTELLIGENZA vera: cambiarlo
cambia anche i lanciatori di incantesimi.

L'**astuzia** (`Astuzia.di`) parte dal **massimo di classe** ed è ridotta da una **rampa sul livello** (quello del personaggio, che nei mostri è del mondo):
`astuzia = massimo × min(1, ASTUZIA_PARTENZA + ASTUZIA_PER_LIVELLO × (livello − 1))`, con `CostantiTattiche.ASTUZIA_PARTENZA` = 0,4 e `ASTUZIA_PER_LIVELLO` = 0,075: il 40% del massimo al livello 1, pieno al livello 9. Un'Ombra Nera
è così tonta all'inizio e diventa più ostica col salire del mondo, e i boss al livello 1 sono un po' meno furbi (Strega e Lich al terzo livello). Il massimo di classe non è un tetto dell'INTELLIGENZA vera: è un valore base che la
distribuzione dei punti di `LanciatoreDeiDadi` (un budget che cresce col livello, ripartito in parte a caso) sostituisce, quindi l'attuale può essere molto sotto (Mago: 55 contro 18 a livello 1) o sopra (Goblin: 4 contro 6–11): per questo
non si parte dall'INTELLIGENZA attuale, che fra i livelli 1 e 9 cresce troppo poco perché l'astuzia si muova.

**Il campo da gioco è il livello 1–9**: difficilmente si arriva al 10. Livello di astuzia (1–4) ai livelli 1 / 3 / 5 / 7 / 9:

| Classe (massimo) | Livello di astuzia |
| :--- | :--- |
| Goblin (4), Folletto (8) | 1 / 1 / 1 / 1 / 1 |
| Hobgoblin (12) | 1 / 1 / 1 / 1 / 2 |
| Guerriero (15), Fantasma (15) | 1 / 1 / 1 / 2 / 2 |
| Idra (25) | 1 / 2 / 2 / 3 / 3 |
| Ladro (30) | 2 / 2 / 3 / 3 / 3 |
| Ombra Nera (31) | 2 / 2 / 3 / 3 / 4 |
| Mago (55) | 3 / 3 / 4 / 4 / 4 |
| Strega (65), Lich (70) | 3 / 4 / 4 / 4 / 4 |
| Drago (80) | 4 / 4 / 4 / 4 / 4 |

Il massimo di classe di chi gioca: Guerriero e Guerriera 15, Ladro e Ladra 30, Bardo e Cantastorie 35, Sacerdote e Sacerdotessa 40, Elfo e Elfa 45, Mago e Maga 55; fra i mostri Troll 10, Fantasma 15, Idra 25.

**Gli ospiti vulnerabili** non hanno una regola a parte: sono fra i candidati dell'ultima scelta, quella a caso (`AVVERSARI_E_OSPITI`, `GruppoGiocatore.getOspitiBersaglio`), quindi un mago o un sacerdote vivo li protegge. Un ospite non vulnerabile non è mai un bersaglio.

## 5. Le azioni possibili

Un personaggio può compiere solo le azioni già disponibili nella barra dei comandi in combattimento (`LocazioneBase.impostaComandiPossibili`).
**Corruzione, amicizia, fuga e passare inosservati sono escluse**: le prime due non sono nemmeno più disponibili appena si combatte
(le loro opzioni si spengono all'inizio dello scontro, `opzioneCorruzioneDisponibile` e `opzioneAmiciziaDisponibile`), la fuga è
una scelta del giocatore (§3) e passare inosservati non è possibile dopo una vera azione. Per le altre:

| Azione | Chi | Serve | Bersaglio | Note |
| :--- | :--- | :--- | :--- | :--- |
| **Attacco fisico** (`SINGOLO_ATTACCO`) | tutti i combattenti | niente: è l'ultima voce, non fallisce mai (un'arma o l'arma naturale c'è sempre) | un avversario vivo | la seconda fase con la doppia arma (Ladro, Elfo) resta dentro l'attacco |
| **Incantesimo maleficio** (Aria, Acqua, Terra, Fuoco, Fulmine, Gelo, Veleno; Silenzio sotto) | chi `puoFormulare(classe)` | almeno una pergamena della classe nel gruppo, `MAGIA` per il costo, non `SILENZIATO` | un avversario; l'incantesimo `MULTIPLO` colpisce fino a `NUMERO_BERSAGLI` avversari (oggi i primi vivi) | va scelta la classe di incantesimo |
| **Silenzio** | idem | idem; 5 `MAGIA` | un avversario: sul gruppo il primo vivo non silenziato, altrimenti col silenzio più corto; su un personaggio proprio lui | mette `SILENZIATO`, senza danni; con le tattiche: "un avversario magico non silenziato" |
| **Morte** | idem | idem; solo sui vivi | un avversario (`SINGOLO_SOLO_VIVI`) | |
| **Resurrezione** | chi la sa | pergamena e `MAGIA` | un solo bersaglio, anche morto (`SINGOLO_QUALSIASI`), ma solo un compagno del gruppo (e mai un non morto del gruppo, vedi `Personaggio.isCurabile`); il danno ai non morti lo fa la Luce Divina, non lei | su un alleato caduto |
| **Alba sacra** | Sacerdote/Sacerdotessa (abilità innata) | `MAGIA` | tutto il gruppo (toglie stati e modificatori temporanei) | tipicamente se qualcuno ha uno stato negativo |
| **Dardo arcano** | Mago, Maga, Elfo, Elfa (innato) | `MAGIA` | un avversario | |
| **Canto curativo** | Bardo, Cantastorie, bardo della locanda | `MAGIA` (4); solo se qualcuno è ferito | il più ferito | |
| **Canto ipnotico** | idem | `MAGIA` (3); solo contro avversari vivi | fino a `NUMERO_BERSAGLI` avversari (`SPAVENTATO` o `CONFUSO`) | |
| **Urlo della Banshee** | idem, ma solo dopo che il gruppo l'ha imparato (il bardo della locanda lo sa sempre) | `MAGIA` (6); solo contro avversari vivi | fino a `NUMERO_BERSAGLI` avversari: chi cede prende `SPAVENTATO` e `CONFUSO` insieme | un'alternativa più forte al canto ipnotico |
| **Canto di valore** | idem | `MAGIA` (5) | tutto il gruppo (`ISPIRATO`) | a inizio scontro |
| **Pozione della Salute** (normale, grande) | tutti | almeno una del tipo nel gruppo; il bersaglio è ferito | un personaggio del gruppo (chi beve) | sono due azioni (`POZIONE_SALUTE`, `POZIONE_SALUTE_GRANDE`) |
| **Pozione della Magia** (normale, grande) | tutti | almeno una del tipo nel gruppo; il bersaglio non ha la `MAGIA` piena | un personaggio del gruppo | sono due azioni (`POZIONE_MAGIA`, `POZIONE_MAGIA_GRANDE`) |
| **Panacea** | tutti | almeno una nel gruppo; il bersaglio ha almeno uno stato negativo | un personaggio del gruppo | `POZIONE_PANACEA`; toglie tutti gli stati negativi. Per ora non è in nessuna tattica automatica: la si usa solo componendo una tattica a mano |
| **Cambiare arma** | chi può usare quell'arma (`puoEquipaggiare`) | un'arma nell'inventario del gruppo, che il personaggio possa impugnare e che non sia quella che ha già; il controllo preventivo del cambio riesce; non si è già al massimo di cambi | l'arma stessa (non un avversario) | **è l'azione del turno**: il personaggio cambia arma e basta; l'attacco c'è dal turno dopo (vedi sotto, "Cambiare arma") |


**Un incantesimo "del repertorio".** Oltre a una classe precisa, la voce può dire "un incantesimo malefico qualsiasi" con un **criterio di
scelta** (`CriterioIncantesimo`, §11.10): *a caso*, *il più potente*, *il più adatto al bersaglio* e così via. Sono le logiche che prima usavano i mostri in base all'intelligenza (§4). L'azione `INCANTESIMO_DI_DANNO`
vale per i personaggi del giocatore (solo i sette elementali), `INCANTESIMO_DEL_REPERTORIO` per i mostri (compresi la Morte e il Silenzio).

**Il criterio di scelta dell'incantesimo** (§11.10) dice quale incantesimo lanciare e **dipende dall'intelligenza di chi sceglie**, in quattro livelli:

1. **poco intelligente**: un incantesimo **a caso**;
2. **mediamente furbo**: quello che potrebbe fare più danni, **senza** tener conto delle resistenze e delle debolezze del bersaglio e di
   eventuali modificatori di armi e armature;
3. **furbo**: quello più adatto a fare più danni **a quel personaggio**, con le resistenze e le debolezze di classe (il conto di
   `moltiplicatoreAtteso`);
4. **molto intelligente**: come il furbo, e in più **impara** dai lanci: se un incantesimo ha avuto un effetto diverso dal previsto contro un
   personaggio equipaggiato con oggetti che aumentano (o riducono) le sue resistenze, se ne ricorda e ne tiene conto (lo scopre solo dopo il
   primo lancio, quindi è ricordato per personaggio e tipo di danno: §3.3).

- **Incantesimi a più bersagli** (`MULTIPLO`): per i mostri poco intelligenti e mediamente furbi conta il **danno massimo a un bersaglio** (il
  resto è danno collaterale); per i furbi conta il **danno totale**; per i molto intelligenti si guarda anche **chi verrebbe portato vicino alla
  fine** (o ucciso): per ogni bersaglio, la quota della salute attuale che l'incantesimo toglie, al massimo 1 se lo uccide, sommata. Un incantesimo che lascia un personaggio a 50 punti e altri due a 35 è peggio di uno
  che lo lascia a 60 e gli altri due a 15, perché con un secondo lancio si finiscono probabilmente gli altri due.
- **Quali incantesimi partecipano**: dipende da cosa ha il gruppo. Resurrezione, Alba Sacra e Silenzio sono **voci
  proprie** della tattica e non partecipano al "più potente" fra gli incantesimi che fanno danno. Il **Silenzio** va usato **subito** su maghi,
  sacerdoti, elfi e bardi (i personaggi magici di cui sopra) quando c'è.
- **Il costo**: si considerano **solo gli incantesimi che si possono lanciare in quel momento** (`MAGIA` sufficiente, non silenziato). I mostri non
  recuperano `MAGIA` **a meno che non siano umanoidi e abbiano nel loro gruppo qualche pozione della Magia a disposizione**; un personaggio può bere una
  pozione della Magia (una voce a sé, "se la `MAGIA` è sotto la soglia") e il turno dopo avere a disposizione quelli più costosi. La stessa
  considerazione vale per le **pozioni della Salute** e per gli **incantesimi curativi** (Alba Sacra, Resurrezione, canto curativo): gli avversari non li usano
  perché non hanno nessuna scorta (né pozioni né pergamene) e il loro repertorio comprende solo incantesimi **malefici**, pagati soltanto con la loro `MAGIA`; i mostri non hanno scorte nel modello dati (§8).

**Il silenzio è un'azione.** Lo stato `SILENZIATO` lo mette l'incantesimo del Silenzio (`incantesimi/Silenzio`: malefico, arcano, pergamena a 5 monete
dall'alchimista, 5 `MAGIA` a lancio, durata di `calcolaDurataStato`) o, con una probabilità, ogni attacco di tipo **arcano** (il Dardo arcano di Mago ed Elfo,
l'arma naturale del Lich, gli incantamenti arcani; `TipoDanno.ARCANO`). Il Silenzio ha comunque il tiro per colpire e l'immunità del bersaglio, quindi non è una garanzia.
Una voce "silenzia il mago" è `SU AVVERSARI [MAGICO] ordine PIU_MAGIA → SILENZIO, DARDO_ARCANO, ATTACCO_FISICO` (§11.7): il Silenzio su un avversario magico non ancora silenziato e, per chi non ha la pergamena o la `MAGIA`,
il Dardo arcano sullo stesso bersaglio. L'azione manuale del giocatore sceglie il bersaglio da sé: il primo vivo non ancora silenziato, altrimenti quello col silenzio più corto (`Silenzio.scegliBersaglio`); lanciato su un personaggio silenzia proprio lui.

**Cambiare arma è un'azione.** Un personaggio può decidere, per esempio, di prendere dall'inventario del gruppo un'arma che dà danno
`SACRO` se tra gli avversari ci sono dei non morti: "condizione: c'è un avversario non morto e non ho già un'arma sacra e nel gruppo
ce n'è una che posso usare; azione: cambia arma, scegliendo una che dà SACRO". Le regole:

- **Il cambio è tutta l'azione del turno**: il personaggio sceglie l'arma, la impugna e il suo turno finisce lì, senza attaccare;
  l'attacco col nuovo equipaggiamento viene dal turno dopo. Vale anche con la doppia arma (Ladro, Elfo): un cambio singolo, della
  principale o della secondaria, è comunque tutto il turno, e per cambiarle entrambe servono due turni di combattimento senza attaccare.
- **Se il cambio non è possibile, l'azione non c'è**: la voce non si esegue e si passa alla successiva dell'elenco, come per un
  incantesimo senza `MAGIA`. Il controllo è **preventivo** (prima di toccare niente) e **riusa i blocchi che ci sono già** per
  l'equipaggiamento, senza regole nuove: `RegoleEquipaggiamento.valuta` dice di no se l'arma è troppo pesante per il carico
  (`TROPPO_CARICO`), se non è utilizzabile dalla classe (`NON_ADATTO_ALLA_CLASSE`), se manca la forza o il livello, se la mano è
  occupata, se è a due mani e non si hanno le mani libere, se non è da mano secondaria... In quel caso l'arma di prima non viene
  nemmeno tolta. Un cambio che non passa è un'azione **senza effetto**: non consuma il turno e si passa alla voce successiva.
- **Il controllo guarda il dopo-cambio** (`RegoleEquipaggiamento.valutaCambioArma`, `GruppoGiocatore.controllaCambioArma`). `valuta` guarda l'equipaggiamento *com'è adesso*, con l'arma di prima ancora in
  mano e nel carico, mentre a mano il giocatore prima toglie la vecchia e poi prende la nuova: chiamare `puoEquipaggiare` prima di togliere darebbe risultati sbagliati in entrambe le direzioni. Una spada nuova verrebbe respinta
  perché la seconda arma non è consentita (o la mano principale è occupata), un'arma troppo pesante per il carico di adesso entrerebbe
  una volta uscita la vecchia, e un pugnale verrebbe accettato nella mano secondaria, che non è un cambio. Per questo il controllo
  si fa sulla situazione "senza l'arma che esce": gli artefatti che escono si tolgono **solo dall'elenco** del personaggio (non con `removeArtefatto`,
  che ricalcola gli attributi e riduce salute e MAGIA se l'arma ne dava il massimo, e pubblica notifiche), si chiama `valuta` e si rimettono nello stesso ordine,
  senza effetti né notifiche.
- **Chi esce** (`RegoleEquipaggiamento.chiEsce`): per un'arma da mano principale, quella che occupa lo slot (o l'arma a due mani che occupa entrambi); per un'arma a due mani, tutto quello che ha in mano.
  **Lo scudo, e ogni cosa che non sia un'arma, non esce mai**: con lo scudo in mano un'arma a due mani non si può prendere (`MANI_OCCUPATE_PER_ARMA_A_DUE_MANI`).
  Con la **doppia arma** (Ladro, Elfo, due armi in mano, non uno scudo) si confrontano le due armi e **esce quella che infligge meno danno**, a parità la secondaria: il danno è `CalcolatoreCombattimento.dannoAttesoDellArma`
  (danni base per il livello di combattimento dell'arma, più per ogni incantamento il bonus fisso e la quota dell'INTELLIGENZA; senza dadi né critici, **contro un bersaglio neutro** e al pieno anche per la secondaria).
  La nuova arma prende lo slot di quella che esce, quindi deve poterci stare: una lancia (solo mano principale) fa uscire la principale anche se è la più forte. Se l'arma più debole è proprio quella che dà
  il tipo di danno voluto, lo evita la condizione della voce ("non ho già un'arma che dà quel danno"), non il controllo. Il tipoConfronto si può affinare (§10).
- **Ogni arma si valuta una volta sola.** Chi ha provato un'arma e ha visto che non può impugnarla se lo ricorda, per non rifare le
  prove a ogni turno e a ogni voce. La valutazione dipende da chi la fa (classe, livello, forza, carico) e da quello che ha in mano: se uno di questi cambia
  (un'arma nuova nell'inventario, un cambio d'arma, un livello, un effetto che cambia il carico) le valutazioni vanno rifatte o scartate. Il dato sta nello stato del combattimento (§3.3): `registraArmaValutata` / `getValutazioneArma`, usati da `GruppoGiocatore.puoCambiareArma`; un cambio d'arma del personaggio scarta le sue valutazioni, e si azzerano a ogni locazione.
- **Un massimo di cambi per combattimento, per personaggio**: `CostantiPersonaggi.MAX_CAMBI_ARMA_PER_COMBATTIMENTO` (2), per fermare i continui
  cambi anche quando le condizioni lo permettono. Si conta nello stato del combattimento (`registraCambioArma`, `getCambiDArma`) e ricade fra le condizioni della voce
  ("non ho ancora cambiato arma").
- **Come si esegue** (`GruppoGiocatore.cambiaArma`): non passa dagli eventi `InternoStoccaggioArtefatto` / `InternoPrelievoArtefatto`, che sono richieste della
  schermata dell'inventario con una notifica di ritorno: è una valutazione del motore, e serve un esito certo nello stesso istante. L'azione chiama direttamente i due passi
  pubblici del gruppo, `stocca` (l'arma di prima nell'inventario del gruppo) ed `equipaggia` (la nuova al personaggio), dopo `puoCambiareArma` (massimo di cambi più controllo
  preventivo); poi conta il cambio. Gli stessi due passi li usano anche gli handler degli eventi della schermata dell'inventario, dopo `controllaPrelievo`.
  Il cambio non consuma il turno da solo: lo fa chi lo chiama (l'`EsecutoreTattiche`). È un'**azione atomica**: già validata dal controllo preventivo, non teme un rifiuto a metà.
- **Il filtro sull'inventario** (`GruppoGiocatore.armiPerCambio(personaggio, tipoDanno)`): le armi da mano principale o a due mani del gruppo che danno quel tipo di danno
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
| **Debole a un elemento** | DA:O (*vulnerability*), FFXII (*weak to element*) | **sì**, in tre pezzi che il calcolatore compone (§6.3): la debolezza di classe (`Personaggio.getMoltiplicatoreDannoSubito`: il `SACRO` sui non morti ×2, le immunità), quella momentanea per stato (`BAGNATO` + fulmine...) e la difesa numerica (`difesaContro`): filtro `DEBOLE_A` e ordinamento `PIU_DEBOLE_A` |
| Non **immune** a quell'incantesimo, tipo di danno o stato (per non sprecare la magia) | DA:O, FFXII | **sì**: `isImmuneAIncantesimo`, `isImmuneATipoDanno`, `isImmuneAEffetto` |
| Con una certa **armatura** (pesante, leggera) | DA:O (*armor*) | **parziale**: i personaggi hanno elmo, scudo, armatura e schinieri con resistenze; i mostri combattono con l'arma naturale |
| **Lo stesso del turno precedente / del compagno** (fuoco concentrato) | FFXII (*last target, leader's target*) | **sì**: lo stato del combattimento ricorda l'ultimo bersaglio di ogni personaggio (ordinamento `ULTIMO_SCELTO`, §3.3) |
| Quello che **attacca me / un alleato** | DA:O (*targeting*), FFXII | **sì**: lo stato del combattimento ricorda chi ha attaccato per ultimo (ordinamento `ULTIMO_ATTACCANTE`, §3.3) |
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

**Le misure: "più forte" e "più debole" non si usano.** Sono parole ambigue (forte per che cosa?): ogni voce dice la misura. Gli ordinamenti che confrontano un valore (più `PIU_MAGIA` e `MENO_MAGIA` per la `MAGIA`, §11.6) scelgono, fra i candidati, quello che ha il valore più alto o più basso:

| Selettore | Misura | Note |
| :--- | :--- | :--- |
| **con meno salute** / **con più salute** | la salute attuale (`getSalute()`), in punti | non la percentuale: un Troll con 40 punti ha "più salute" di un Goblin con 15, anche se il Goblin è intero |
| **di livello più basso** / **di livello più alto** | il livello (`getLivello()`) | |
| **più debole a** `<tipo di danno>` | il fattore di danno atteso contro di lui per quel tipo (`moltiplicatoreAtteso`) più **alto**: debolezza di classe, reazioni con i suoi stati e difesa | un immune ha 0 |
| **più resistente a** `<tipo di danno>` | lo stesso fattore più **basso** | un immune è il più resistente; ha senso soprattutto per gli **alleati** ("chi regge meglio il fuoco del Drago"), e per scartare chi non vale la pena colpire con quel danno |

I selettori che *filtrano* invece di ordinare sono altri: "di livello più alto **del mio**" (un tipoConfronto con chi sceglie), "salute
sotto il n%" (una soglia), "**debole a** `<tipo>`" (moltiplicatore di classe e stati maggiore di 1, senza contare la difesa).

**Regole comuni dei selettori** (decise)

- **Gli ordinamenti** sono: salute attuale (più bassa o più alta), livello (più basso o più alto), più debole o più
  resistente a un tipo di danno, `MAGIA`, chi ha attaccato per ultimo, l'ultimo scelto. **Niente** percentuale di salute né salute massima come ordinamento: la salute assoluta basta (chi ha meno punti ha più
  probabilità di morire al prossimo scontro); la percentuale è un filtro (`SALUTE_PERCENTUALE`). Per un alleato "il più ferito" vuol dire proprio "con meno salute".
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
| Un alleato con **poca magia** (per la pozione della Magia) | FFXII (*MP < n%*) | **sì**: filtro `MAGIA` (in punti) |
| Un alleato di una certa **classe** | DA:O | **sì**: `getClasse()` |
| Un **ospite vulnerabile** da proteggere | (specifico: missioni di scorta) | **sì, come alleato da proteggere**: un ospite vulnerabile vivo (`GruppoGiocatore.getOspitiBersaglio`) è fra i bersagli *possibili* del suo gruppo, e può esserlo di una cura o di una protezione (da decidere quali selettori lo comprendono). Un ospite non vulnerabile non lo è mai. Non combatte e non agisce, quindi non ha una tattica |

### 6.3 "Debole a un elemento"

"Elemento" qui è il **tipo di danno** (`TipoDanno`: fisico, elementale o magico). Oggi la debolezza di un avversario nasce in tre
punti diversi del codice, che ora si possono interrogare insieme con `CalcolatoreCombattimento.moltiplicatoreAtteso`:

| Origine | Dove sta | Che cosa dà | Limite |
| :--- | :--- | :--- | :--- |
| **1. Classe** | `getMoltiplicatoreDannoSubito(tipoDanno)` (`PersonaggioBase`), che legge la tabella di `TipoPersonaggio` e `CategoriaPersonaggio`; `isImmuneATipoDanno` è lo stesso valore uguale a 0 | **immune** (0) o **debole** (>1: il `SACRO` sui non morti ×2); e le resistenze (tra 0 e 1: danno ridotto e meno effetti) | i valori per classe sono in `debolezze_e_resistenze.md` |
| **2. Stato (momentanea)** | `TipoInterazioneConEffettiDiStato` (le interazioni, quasi tutte coppie *stato, tipo di danno*, con il loro moltiplicatore) e `CalcolatoreCombattimento.interazioniAttive` (quando scattano) | moltiplicatori del danno, deboli (×1,5 il fulmine su un `BAGNATO`) o resistenti (×0,5 il fuoco su un `BAGNATO`), che durano quanto lo stato | la debolezza cambia da un turno all'altro: "debole al fulmine" vale finché dura il `BAGNATO` |
| **3. Difesa numerica** | `CalcolatoreCombattimento.difesaContro` | `COSTITUZIONE + PARATA` per il fisico, `RESISTENZA_MAGICA` per tutto il resto, più gli incantamenti dei pezzi | per un mostro tutti gli elementi hanno la stessa difesa |

**Il punto 2** si legge così: `interazioniAttive(difensore, tipoDanno)` è **l'unico punto che decide quando scatta un'interazione con uno stato**: un ciclo
sulle interazioni dell'enum, ognuna scatta se il difensore ha uno dei suoi stati (`getPrecondizioni`) e riceve uno dei suoi tipi di danno
(`getInnescanti`); vuota se il difensore è immune. L'**ordine di dichiarazione dell'enum è l'ordine di applicazione** degli effetti (per stato).
`moltiplicatoreDelleInterazioni` parte dal moltiplicatore della classe (`getMoltiplicatoreDannoSubito`) e lo moltiplica per quelli dell'enum (il critico automatico del colpo di grazia e della frantumazione, `isCriticoAutomatico`, lo aggiunge `moltiplicatoreAtteso` come ×2), e
`fattoreDifesaDaStati` dà il cambio di difesa (guscio di ghiaccio ×1,5, superconduzione ×0,7). `calcolaDannoRisultante` chiama queste funzioni e **applica solo gli effetti** di ogni interazione (`switch`): così il danno vero e le tattiche
non possono divergere. `moltiplicatoreAtteso(difensore, tipoDanno)` le compone con la difesa, senza dadi e senza cambiare niente.
Il test `InterazioniConGliStatiCaratterizzazioneTest` confronta, a seme fisso, l'esito di 8316 colpi (3 difensori, ogni stato da solo e a coppie, ogni tipo di danno) con quello registrato prima del riordino di queste funzioni; `MoltiplicatoreAttesoTest`
verifica `moltiplicatoreAtteso`.

**Limite accettato.** La distinzione di difesa è solo fisico / non fisico (`COSTITUZIONE + PARATA` e `RESISTENZA_MAGICA`): fra il fuoco e il gelo, o fra il
tagliente e il contundente, un mostro non ha differenze di difesa se non quelle dei punti 1 e 2 (tabella di classe e stati). Le resistenze
parziali per classe sono in `debolezze_e_resistenze.md`.

**Fattore di danno atteso.** Si calcola, per un avversario e un tipo di danno T, **senza tirare i dadi**:

> fattore = 0 se immune a T; altrimenti `100 / (100 + difesa)` × il moltiplicatore di classe × i moltiplicatori degli stati attivi per T
> (la difesa è `difesaContro`, con gli stati che la cambiano) — `CalcolatoreCombattimento.moltiplicatoreAtteso`.

Da lì: **debole** = moltiplicatore > 1, **resistente** = < 1, **immune** = 0, e "il più debole fra questi elementi" = il fattore più alto.
Serve a scegliere il bersaglio ("il più debole al fuoco"), l'incantesimo o l'arma ("il Gelo contro un `BAGNATO`", "un'arma sacra contro i
non morti": la condizione del cambio d'arma, §5) e il momento del cambio.

**Decisioni.** "Debole" vuol dire solo moltiplicatore > 1 (non "meno resistente degli altri elementi"). Il selettore e la scelta dell'azione guardano gli stati
di adesso **e anche quelli che l'azione stessa può causare** (il Gelo che prima bagna e poi congela, quindi con i moltiplicatori della reazione che ne seguirebbe).
`CalcolatoreCombattimento.statiCausabili(difensore, arma)` (stati nativi del tipo di danno e degli incantamenti, più quelli che mettono le reazioni con gli stati che il
difensore ha già, come l'ATTERRATO dell'INCIAMPO; esclusi tipi di danno e stati a cui è immune), `statiTolti(difensore, arma)` (quelli che le reazioni gli tolgono) e
`moltiplicatoreAttesoDopo(difensore, tipoDanno, arma | stati)`, il fattore del colpo che segue l'azione: gli stati si contano come se fossero scattati (come le reazioni).
Le conseguenze delle reazioni sono dichiarate in `TipoInterazioneConEffettiDiStato` (vedi `interazioni_effetti_di_stato.md`). **Oggi il valutatore non chiama ancora
queste stime**: guarda solo gli stati di adesso (`moltiplicatoreAtteso`).
## 7. Le condizioni

Gli elenchi dei giochi sono quelli che ricordo, **indicativi**: i nomi esatti cambiano da gioco a gioco. La colonna a destra dice se
la cosa si legge dal modello di Foresta (**sì**), solo in parte o va derivata (**parziale**) o non ha senso qui (**no**). Che cosa è diventato un filtro,
un ordinamento o una condizione delle voci lo dicono §11.5, §11.6 e §11.8.

### 7.1 Su di sé

| Controllo | Giochi | In Foresta |
| :--- | :--- | :--- |
| Sempre (nessuna condizione) | tutti | **sì** |
| Salute sotto/sopra una percentuale | DA:O, FFXII (*Self: HP < n%*) | **sì**: `getSalute() / getSaluteMassima()` |
| Magia sotto/sopra una percentuale o un valore | DA:O (*mana/stamina*), FFXII (*MP*) | **sì**: `getMagia() / getMagiaMassima()` |
| Stanchezza oltre una soglia | (specifico: abbassa la probabilità di colpire) | **sì**: `getStanchezza()`; non è ancora una condizione (§11.8) |
| **Magia sufficiente per quell'incantesimo** | DA:O, FFXII (l'abilità non parte se mancano le risorse) | **sì**: `getCostoLancio()` contro `getMagia()`; è ciò che fa passare alla voce successiva |
| **Ha quell'incantesimo** a disposizione | DA:O/FFXII (la licenza, l'abilità) | **sì**: `puoFormulare(classe)` e `getIncantesimi(classe) > 0`, oppure l'innato (`AbilitaInnate.puoUsare`/`abilitaUtili`, `CantoDelBardo.cantiUtili`) |
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
| Il **numero del round** (è il round `n`, da `n` in poi, ogni `n` round) | BG (*round time*), DA:O (cooldown) | **sì**: il contatore è nello `StatoDelCombattimento` (§3.3), con l'evento `InternoInizioRound`; condizione `ROUND` |
| **Una volta per scontro** / non più di `n` volte / non prima di `n` round dall'ultima | DA:O (cooldown), FFXII (*once*) | **sì**: le voci già usate (e in che round) sono nello `StatoDelCombattimento`; condizioni `VOCE_USATA` e `ROUND_DALL_ULTIMO_USO`; vedi sotto a che cosa serve e a che cosa **no** |
| Duello, combattimento fino alla resa, ondata successiva | (specifico) | **sì**: sono flag del personaggio (`isFinoAllaResa`, `isSfidante`) e dello scontro (ondate) |

**A che cosa serve "una volta per scontro" (e a che cosa no).** Con le azioni di oggi i casi veri sono pochi:

- **Il cambio d'arma** (§5): al massimo uno, al più due per personaggio. È un contatore per personaggio, il caso più chiaro.
- **Le abilità forti dei mostri, che oggi non esistono** e che queste condizioni preparano: il fiato di fuoco del Drago, l'urlo di una banshee, una morte che si ritorce
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
| **Nell'inventario del gruppo c'è un'arma** che dà un tipo di danno (SACRO contro i non morti), con un certo modificatore, o più forte della mia | DA:O (*weapon set*) | **sì**: l'inventario del gruppo si legge (`getArtefatti()`), `Arma.daTipoDanno(tipo)` dice se un'arma dà quel danno e `puoEquipaggiare` se il personaggio la può impugnare; li mette insieme `GruppoGiocatore.armiPerCambio` (§5), che il requisito di `CAMBIA_ARMA` usa |
| Con una probabilità del n% | BG (*RandomNum*) | **sì**: `Dado`; condizione `PROBABILITA` |
| Tipo di locazione, ora del giorno | BG | **sì**: `Foresta.getLocazioneDelGruppo()`, `LineaTemporale`; utile solo per tattiche speciali |
| Variabili e contatori definiti dall'utente | BG (*script*) | **no**: va oltre lo scopo |
### 7.4 Modelli già pronti

Alcuni giochi non chiedono di scrivere le regole ma di scegliere uno **stile** (i *Tactics* di *Dragon Quest*: *combatti con giudizio,
niente pietà, risparmia magia*; i *Behavior* di DA:O: aggressivo, difensivo, passivo). In Foresta sono le **tattiche predefinite per
ruolo** (§11.16): il Mago apre con il suo incantesimo più forte, il Sacerdote cura chi sta male, il Guerriero attacca i feriti; il giocatore le propone con il pulsante "Predefinita" e poi le ritocca: stesse voci, già scritte.

### 7.5 Il catalogo: le parole delle voci

Riassunto con sigle di tutto ciò che una voce può dire (i dettagli sono in §5, §6 e §7, i nomi veri degli enumerati in §11). Una **voce** è: *[bersaglio] + [condizioni] + [azioni
(con un criterio)]*; il personaggio esegue la **prima voce che si può davvero eseguire**. Le voci per personaggio sono **illimitate** (nessun tetto). Ogni mostro ha la sua lista di voci: un bruto
(il Minotauro) una molto semplice, uno più intelligente ne ha di più, con le domande giuste. **L'astuzia stessa è una condizione
della voce** (`ASTUZIA ≥ n`, sigla C1): così la lista di una classe è una sola, e le voci più furbe si accendono quando l'astuzia basta; le
soglie sono un parametro di ogni voce e non un livello globale. La formula dell'astuzia è la rampa del §4.

**Bersagli: i filtri** (restringono i candidati; si possono mettere insieme)

| Sigla | Filtro | Definizione |
| :--- | :--- | :--- |
| F1 | **vivo e in combattimento** | sempre: non morto, non in panchina, non fuori combattimento. Per un gruppo avversario che sceglie nel gruppo del giocatore i **bersagli possibili** sono i personaggi vivi **e gli ospiti vulnerabili vivi** (`getOspitiBersaglio`); i filtri sulla classe e sulla magia non li comprendono mai |
| F2 | **di classe / di categoria** `<X>` | `getClasse()`; `isDellaCategoria` (basta una delle categorie) |
| F3 | **magico** | prima chi ha abilità innate (Dardo arcano: Mago, Maga, Elfo, Elfa; Alba Sacra: Sacerdote, Sacerdotessa; canti: Bardo, Cantastorie) non silenziato e con la `MAGIA` per almeno una; poi i **mostri** di una classe con `isMagico()` (Strega, Lich, Drago...), non silenziati e con `MAGIA` > 0, **fin da subito**; poi gli altri solo se `MAGIA` > 0, non silenziati e che hanno già lanciato in questo combattimento |
| F4 | **con / senza lo stato** `<X>` | `hasEffettoDiStato` |
| F5 | **debole a** `<tipo di danno>` | moltiplicatore di classe e stati maggiore di 1 (senza la difesa); **non immune a** `<tipo>`: moltiplicatore maggiore di 0 |
| F6 | **salute sotto / sopra il** `n%` | salute attuale su massima |
| F7 | **di livello più alto / più basso del mio** | un tipoConfronto con chi sceglie |
| F8 | **lo stesso del turno precedente** | il bersaglio che questo personaggio ha scelto l'ultima volta (uno per avversari e uno per alleati), se è ancora valido (nella schermata è l'ordinamento `ULTIMO_SCELTO`, §11.15) |
| F9 | **che mi ha attaccato per ultimo** | l'avversario che ha agito contro di me per ultimo (nella schermata è l'ordinamento `ULTIMO_ATTACCANTE`) |
| F10 | **il capo** | `getCapo()` del gruppo avversario |

**Bersagli: gli ordinamenti** (scelgono *uno* fra i candidati; a parità vince il primo dell'elenco)

| Sigla | Ordinamento | Misura |
| :--- | :--- | :--- |
| O1 | **il primo** | la posizione nell'elenco dei vivi (è la scelta di base) |
| O2 | **a caso** | tutti con la stessa probabilità |
| O3 | **con meno salute / con più salute** | salute attuale, in punti |
| O4 | **di livello più basso / più alto** | `getLivello()` |
| O5 | **più debole / più resistente a** `<tipo di danno>` | il fattore di danno atteso (`moltiplicatoreAtteso`), più alto o più basso; un immune è 0 |
| O6 | **per priorità di classe** `<lista>` | **non c'è**: si ottiene con più voci, una per classe (§11.6) |
| O7 | **con più / con meno `MAGIA`** | la `MAGIA` attuale: chi ne ha di più è il più pericoloso |

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
| A1 | **attacco fisico** | il ripiego di ogni tattica; non fallisce mai |
| A2 | **un incantesimo che fa danno** (una classe precisa, o "del repertorio") | criterio S: a caso, il più potente, il più adatto, o il più adatto con apprendimento |
| A3 | **Silenzio** | su un bersaglio F3 non silenziato |
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

Il "più conveniente" di oggi (probabilità di colpire × danno atteso, contro l'attacco fisico) è S3 con in più il tipoConfronto con l'attacco (`soloSePiuForteDellAttacco`, §11.10). A questi si aggiunge `PIU_ECONOMICO_CHE_BASTA`, fuori dai quattro livelli.

**Che cosa serve ricordare** durante lo scontro, oltre allo stato dei personaggi: il numero del turno; l'ultimo avversario e l'ultimo alleato scelti da ogni
personaggio (F8); chi mi ha attaccato per ultimo (F9); chi ha già lanciato un incantesimo (F3); le voci già usate e i cambi d'arma (C6); per S4,
per ogni coppia (personaggio, tipo di danno) quanto è andato diverso dal previsto. È tutto nello `StatoDelCombattimento` (§3.3).

Esempi di liste complete, con le fasce di astuzia, sono le tattiche base dei mostri e le predefinite dei giocabili (§11.16).

### 7.6 Niente blocchi

Non ci sono: la tattica è una lista piatta di voci, ciascuna con le sue condizioni in AND, e la fascia di astuzia è una condizione della voce (§11.1). Le condizioni con E, O e parentesi si ottengono con più voci
(§11.8); un pezzo comune a più classi si copia nel codice (`TatticheBase` lo ripete con metodi privati), senza blocchi con nome né file di risorse.

### 7.7 Il registro delle decisioni

Ogni volta che un personaggio sceglie che cosa fare scorrendo la sua tattica, il motore pubblica sul bus l'evento **`InternoDecisioneTattica`** (`eventi/interni`, tipo `INTERNO_DECISIONE_TATTICA`), **prima** che l'azione si esegua. Porta il personaggio e il bersaglio (come
`VistaPersonaggio`), l'azione scelta (per l'incantesimo di danno, quello scelto), la posizione della voce (−1 per il ripiego sull'attacco fisico) e la **spiegazione**, una riga per passo. `SnifferBusEventi` lo scrive nel log con il suo tipo nella riga (`headerEvento`), quindi un
`grep INTERNO_DECISIONE_TATTICA` sul file di `-Dforesta.log=...` tira fuori le sole decisioni tattiche, per mettere a punto le tattiche e per leggere le partite. Lo coprono `CombattimentoConTatticheTest` e `ValutatoreTest`.

Che cosa dice la spiegazione (la costruisce il `Valutatore`, i selettori e `SceltaIncantesimo`):

- **le voci saltate** con il motivo ("voce 1: disattivata", "voce 2: le condizioni non sono tutte vere [...]", "voce 3: nessun bersaglio", "FULMINE scartata: manca la pergamena...");
- **il percorso del selettore** con i candidati che restano dopo ogni passo, con i nomi: "AVVERSARI: 3 [Goblin 1, Goblin 2, Goblin 3]", "azione applicabile: 1 [Goblin 2]", "STATO BAGNATO: 1 [Goblin 2]";
- **le misure dell'ordinamento**: "MENO_SALUTE (Goblin 1 60, Goblin 2 14): Goblin 2";
- **il criterio dell'incantesimo di danno** con i danni attesi confrontati: "PIU_ADATTO: FUOCO 4.2, FULMINE 9.8", con il danno delle armi se l'azione lo confronta; per `PIU_ECONOMICO_CHE_BASTA` se ognuno basta a uccidere;
- **il ripiego**: "nessuna voce eseguibile: attacco fisico al primo avversario".

La spiegazione si costruisce sempre (qualche stringa per decisione): se pesasse, si costruirà solo con un ascoltatore o il log acceso.

## 8. Che cosa manca

Il modello, il motore, il valutatore, il salvataggio, il registro delle decisioni, le tattiche base dei mostri, le predefinite dei giocabili e la schermata del giocatore ci sono. Restano:

- **Tarare le tattiche base** (§11.16): sono una prima versione, scritta dai manuali di tattica e dal codice; vanno provate in partita e con le simulazioni (`TestScontriDiGruppo`), soprattutto la Strega contro gruppi piccoli e il Drago, e per i personaggi giocabili manca una tattica base per fascia di livello.
- **Derivare dal codice i tre elenchi** di `TatticheBase` (chi è magico, chi ha il Silenzio, chi ha la Morte): oggi sono scritti a mano (c'è un `TODO`) e vanno tenuti allineati con `PersonaggioBase.getRepertorioIncantesimi`, o almeno confrontati da un test con le classi vere.
- **Mostri con abilità proprie**: le tattiche usano solo attacco e incantesimi; nessun mostro ha azioni speciali (la rigenerazione del Troll, le teste dell'Idra, il canto dell'Arpia: c'è un `TODO` in `TatticheBase`). Vanno prima create come azioni (`TipoAzione`).
- **Scorte per i mostri**: non hanno pozioni né pergamene nel modello dati, quindi non si curano e non lanciano benefici (§5).
- **L'esito delle azioni con effetti che non si applicano** (un Silenzio a segno che non mette `SILENZIATO`) come "mancata", oggi non registrato (§11.7).
- **Rifinire la schermata del giocatore** (§11.15): l'aiuto con la spiegazione dell'ultima decisione (`InternoDecisioneTattica`, §7.7) e le modifiche dei parametri che oggi si fanno solo con più e meno.
- **Copiare una tattica** da un personaggio a un altro (un "modello" da riapplicare): oggi c'è solo la predefinita del ruolo.
- Il **costo della spiegazione** delle decisioni, se pesasse (§7.7).

Non ha senso importare: posizione e distanza, mischia contro distanza, linea di vista, incantesimi con tempo di lancio, movimento.

## 9. Osservazioni

- Il **bersaglio** lo sceglie la tattica per tutte le azioni singole; le azioni manuali del giocatore scelgono ancora da sole (il primo avversario vivo, il più ferito...: §11.9).
- Il round a coppie cambia il **bilanciamento** (§3.1): un compagno solo contro più avversari li subisce tutti, e un gruppo numeroso contro un avversario solo gli dà più azioni; il
  bersaglio dell'avversario non è più chi ha appena agito. Le simulazioni (`SimulazionePartiteTest` con `GiocatoreAutomatico`, `TestScontriDiGruppo`,
  `TestLogoramentoInizioPartita`) sono state **portate al round a coppie** (che oggi è la lista dei turni: `RoundASimulato` replica ancora il vecchio piano a coppie, da allineare) (`RoundASimulato` lo replica per quelle che lavorano su liste di personaggi) e
  hanno dato questi numeri (ottobre 2026, 30 partite per classe, con i mostri che usavano ancora la replica del vecchio comportamento, §11.13):
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
  - **Da bilanciare.** Mago e Sacerdote sono i bersagli preferiti dei mostri e muoiono presto; l'Eremita resta il killer dei livelli bassi.

## 10. Da fare / idee aperte

Domande aperte:

- **Cambiare arma** (§5): affinare quale arma esce con la doppia arma (oggi la più debole per danno atteso, contro un bersaglio neutro): contro il bersaglio vero, con gli effetti di stato, i modificatori e la probabilità di colpire? Per quanto tempo valgono le valutazioni già fatte delle armi del gruppo? Le voci per tornare all'arma di prima le scrive il giocatore o le propone il sistema in coppia con la prima?
- **Il round a coppie** (§3.1): rimisurare il bilanciamento, e decidere se ridurre l'annuncio "X attacca Y" a ogni azione (oggi sempre, anche per aiutare a leggere il log); la velocità del `TIMER` (ogni scatto è mezzo scambio) è rimasta com'era.
- **`PIU_ECONOMICO_CHE_BASTA`** (§11.10): basta se il danno atteso è almeno la salute attuale del bersaglio, o serve un margine? E l'esatta ripartizione delle fasce di astuzia nelle tattiche dei mostri magici: entrambi da tarare con le simulazioni.
- **Variabili locali** (un valore calcolato una volta e usato più volte nella lista, per esempio "il bersaglio scelto"): non servono finché il selettore è anche una condizione (§11.2); se ne riparla se serviranno.
- **Il danno ad area** delle reazioni (esplosione di gas, rigetto, elettrocuzione: `interazioni_effetti_di_stato.md`, «Danni ad area»): le stime (`dannoAtteso`, `moltiplicatoreAtteso`) guardano solo il bersaglio, quindi non preferiscono chi sta in mezzo alla fila o in una pozza di BAGNATI.

Le altre cose da fare sono in §8.

## 11. Il modello dati delle tattiche

Qui sta il **modello** (come sono fatte una tattica, una voce, un bersaglio) e come lo usano il motore e la schermata. Le classi stanno nel pacchetto `…/tattiche` (i tipi, che servono anche alla UI, in `…/tipi`: `TipoAzione` in `06-foresta-combattimento`, gli altri in `08-foresta-tattiche`).

### 11.1 Principi

- **Tutto a enumerati**: azioni, rosa, filtri, ordinamenti, predicati. Nel file e nel salvataggio una voce usa il **nome dell'enumerato** (`CANTO_DI_VALORE`, `SALUTE_SOTTO`...).
- **Lista piatta, niente blocchi.** Una tattica è una lista ordinata di voci; la prima eseguibile vince. I blocchi `SE / ALTRIMENTI / FINE` e i blocchi con nome sono **tolti**: complicano la scheda
  del giocatore senza aggiungere nulla (un blocco equivale alla sua condizione in AND su ogni voce interna). La fascia di astuzia è una condizione della voce (`ASTUZIA_ALMENO n`);
  se due classi hanno la stessa tattica, si **copia**.
- **La tattica sceglie, non esegue** (§1): il valutatore restituisce una decisione (azione, bersaglio) che l'automa trasforma nei comandi di sempre.
- **Il bersaglio non è un campo fisso della voce**: lo decide l'azione (§11.2).

### 11.2 La voce

```
VoceMD      { SelettoreMD selettore?; List<PredicatoMD> condizioni; List<AzioneMD> azioni; boolean attiva; String nome? }   (la prima azione applicabile vince, §11.7)
AzioneMD    { TipoAzione tipo; CriterioIncantesimo criterio?; boolean soloSePiuForteDellAttacco; TipoDanno armaDi? }   (§11.9, §11.10)
SelettoreMD { TipoRosa rosa; List<FiltroMD> filtri; OrdinamentoMD ordinamento }
FiltroMD    { TipoFiltro tipo; boolean negato; parametro? }                   (§11.5)
PredicatoMD { TipoPredicato tipo; boolean negato; parametri? }                (§11.8; le condizioni sono in AND, lista vuota = sempre)
TatticaMD   { List<VoceMD> voci; boolean disabilitata }                              (§11.12)
```

Ogni `TipoAzione` dichiara la sua **portata**, e quindi se e come usa il selettore:

| Portata | Esempi | Il selettore |
| :--- | :--- | :--- |
| un avversario | `ATTACCO_FISICO`, `INCANTESIMO_DANNO`, `SILENZIO`, `DARDO_ARCANO` | sceglie il bersaglio |
| un alleato | `POZIONE_SALUTE`, `CANTO_CURATIVO` | sceglie il bersaglio |
| un alleato caduto | `RESURREZIONE` | sceglie il bersaglio (rosa `ALLEATI_CADUTI`) |
| tutto il gruppo alleato | `CANTO_DI_VALORE`, `ALBA_SACRA` | **non sceglie nessuno**: è solo un test di esistenza ("c'è almeno un candidato?") |
| tutti gli avversari | `CANTO_IPNOTICO`, `URLO_DELLA_BANSHEE` | idem |
| un'arma | `CAMBIA_ARMA` | assente: l'azione ha il suo parametro (`armaDi`, il tipo di danno voluto) |

**Il selettore è anche una condizione**: se non resta nessun candidato la voce non è eseguibile. Così "c'è un alleato con meno di 50 punti" è il selettore stesso, e non servono variabili locali.
Esempi: `SU ALLEATI [SENZA_STATO ISPIRATO] → CANTO_DI_VALORE` (si salta da sola quando tutti sono ispirati, e riparte quando l'ispirazione scade);
`SU SE_STESSO [SALUTE MINORE 50] → POZIONE_SALUTE`; `SU ALLEATI_E_OSPITI ordine MENO_SALUTE → POZIONE_SALUTE`;
`SU AVVERSARI [MAGICO] ordine PIU_MAGIA → SILENZIO, FULMINE, ATTACCO_FISICO` (§11.7).

Le **condizioni** (§11.8) restano per ciò che non riguarda un bersaglio: `ROUND`, `ASTUZIA`, `QUANTI`... Lo stesso controllo (per esempio la salute) può stare in
un filtro (si applica al candidato) o in una condizione (si applica all'attore o a chi indica).

Il **nome** è dato dal giocatore (nella schermata, §11.15) o dalle tattiche base (§11.16), si salva con la voce e serve a riconoscerla; vale null per una voce scritta senza nome (la schermata le dà "Tattica" e il numero).
Il **selettore** può mancare (`null`): vale allora quello di base della portata delle azioni (`VoceMD.selettoreEffettivo`): gli avversari (il primo) per un attacco, se stesso per una cura o una pozione, i caduti per la Resurrezione, gli alleati o gli avversari tutti per un'azione di gruppo.

### 11.3 La rosa dei candidati

La rosa è l'insieme da cui il selettore pesca, prima dei filtri. Il suo nome è un enumerato (`tipi/TipoRosa` in `08-foresta-tattiche`, che serve anche alla UI), e chi calcola i candidati è la classe
corrispondente (`tattiche/rose/Rosa`, una per valore: `RosaAlleati`, `RosaOspiti`..., si trovano con `Rose.di(tipo)`); il metodo restituisce una **`List<Personaggio>`** (non uno `Stream`: il registro delle decisioni, §7.7, deve
poter dire quanti candidati c'erano e quanti ne sono rimasti dopo ogni filtro; le liste sono piccole).

```java
enum TipoRosa { SE_STESSO, ALLEATI, ALLEATI_CADUTI, AVVERSARI, OSPITI, ALLEATI_E_OSPITI, AVVERSARI_E_OSPITI }

interface Rosa {                                   // una classe per valore, senza stato
    TipoRosa getTipo();
    List<Personaggio> candidati(ContestoTattico contesto);   // lista nuova, non modificabile
}
```
Lo coprono `RosaTest` e `ContestoTatticoTest`; `TipoRosa.valePer(attoreDelGiocatore)` dice per quale lato vale ogni rosa, e `candidati` solleva `IllegalStateException` se usato dal lato sbagliato.

| Rosa | Candidati | Per chi vale |
| :--- | :--- | :--- |
| `SE_STESSO` | l'attore | tutti |
| `ALLEATI` | i compagni vivi e in campo (l'attore compreso) | tutti |
| `ALLEATI_CADUTI` | i compagni caduti (non in panchina) | solo le azioni che lo dichiarano (`RESURREZIONE`) |
| `AVVERSARI` | i vivi e in campo dell'altro gruppo | tutti |
| `OSPITI` | gli **ospiti vulnerabili** vivi del gruppo del giocatore (`getOspitiBersaglio`) | tutti: per i miei sono da proteggere, per i mostri bersagli |
| `ALLEATI_E_OSPITI` | alleati poi ospiti (senza chi non si può curare: `Personaggio.isCurabile`, vedi sotto) | **solo i miei personaggi** |
| `AVVERSARI_E_OSPITI` | avversari poi ospiti | **solo i mostri**: riproduce la scelta a caso di prima |

Regole:

- Una rosa restituisce **solo chi è in grado di essere scelto** (vivo e in campo, il filtro F1 del §7.5 è qui dentro), tranne `ALLEATI_CADUTI`. Nell'ordine del gruppo, così "il primo" e le parità sono ripetibili; nelle
  unioni prima i personaggi, poi gli ospiti.
- **Gli ospiti** non agiscono e non hanno una tattica: sono solo candidati. Solo quelli **vulnerabili** vivi entrano nelle rose; i filtri su classe e magia non li comprendono mai (§7.5, F1), quelli su salute e stati sì.
- Il sacerdote ha una voce per la **Luce Divina** (`TipoAzione.LUCE_DIVINA`, innata, danno SACRO su più avversari) sui non morti avversari: `su AVVERSARI [CATEGORIA NON_MORTO] ordine MENO_SALUTE → LUCE_DIVINA`. `INCANTESIMO_DI_DANNO` non la sceglie (il sacro non è fra i sette elementali); gli altri la lanciano solo con una pergamena, con l'azione `LUCE_DIVINA`.
- Un **non morto entrato nel gruppo del giocatore** (lo Scheletro della Mietitura) non si può curare (`Personaggio.isCurabile`): `ALLEATI_E_OSPITI` e `ALLEATI_CADUTI` non lo propongono e `Requisiti` rifiuta su di lui `CANTO_CURATIVO`, `POZIONE_SALUTE`, `POZIONE_SALUTE_GRANDE` e `RESURREZIONE` ("è un non morto: non si può curare"). Un non morto avversario resta un bersaglio come gli altri: un gruppo di soli non morti ha sempre un "più ferito".
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
- **Parametri tipizzati**: ogni `TipoFiltro` dichiara il tipo del suo parametro (`TipoParametro`: `NESSUNO`, `CONFRONTO`, `CONFRONTO_E_NUMERO`, `STATO`, `CLASSE`, `CATEGORIA`, `TIPO_DANNO`; per le condizioni anche `NUMERO` e `SELEZIONE_CONFRONTO_E_NUMERO`); la lettura controlla che ci sia e sia giusto, e la scheda del giocatore mostra l'editor
  adatto. I filtri numerici prendono un `TipoConfronto` (`MINORE`, `UGUALE` o `MAGGIORE`: con `NON` si ottengono "almeno" e "al massimo") e un numero: `SALUTE MINORE 50`.
- **La classe è il ruolo**: `MAGO` e `MAGA` sono due `TipoPersonaggio`, ma il filtro `CLASSE` ammette entrambe le varianti (Mago/Maga, Elfo/Elfa, Ladro/Ladra, Guerriero/Guerriera, Bardo/Cantastorie,
  Sacerdote/Sacerdotessa): la coppia la dà `TipoPersonaggio.getRuolo()`.
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
| `STATO_NEGATIVO` | nessuno | almeno un effetto che è un male da togliere, cioè non benefico (`TipoEffettoDiStato.isBenefico`: ISPIRATO e BERSERK) |
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
`PIU_SALUTE`, `LIVELLO_PIU_BASSO`, `LIVELLO_PIU_ALTO`, `PIU_DEBOLE_A` e `PIU_RESISTENTE_A` (tipo di danno), `ULTIMO_ATTACCANTE` e `ULTIMO_SCELTO` (la memoria dello scontro, §3.3: scelgono *solo* quel personaggio se è fra i candidati), `PIU_MAGIA`, `MENO_MAGIA`. **Niente `PRIORITA_DI_CLASSE`**: la scelta di oggi dei mostri (il Mago più potente, poi il
Sacerdote, l'Elfo, il Bardo, poi a caso) si ottiene con più voci, una per classe; sono tattiche di mostri, che il giocatore non modifica.

### 11.7 Le azioni: sequenze, requisiti e fallimenti

**Una voce può avere più azioni**, in sequenza: `SU AVVERSARI [MAGICO] ordine PIU_MAGIA → SILENZIO, FULMINE, ATTACCO_FISICO` vuol dire "silenziarlo se posso, altrimenti un fulmine, altrimenti attaccarlo". Il selettore sceglie
*un* bersaglio e si esegue la prima azione della lista **applicabile a lui**; un candidato entra nella rosa solo se almeno una delle azioni si può fare su di lui. È equivalente a più voci con lo stesso selettore, ma il bersaglio
si sceglie una volta sola e la scheda resta corta.

**I requisiti dell'azione.** Ogni `TipoAzione` dichiara quando è applicabile a un candidato, e il valutatore li applica da sé (come filtri automatici): il Silenzio scarta silenziati e immuni, una cura scarta chi ha la
salute piena, la Resurrezione vuole un caduto, un'azione con costo vuole la `MAGIA` o la pergamena. Il giocatore scrive solo i filtri tattici.

**Che cosa si sa delle immunità**:

- **Le immunità e resistenze di classe si conoscono fin da subito**, per tutti (giocatore e mostri): sono nei dati (`getMoltiplicatoreDannoSubito`, `isImmuneAIncantesimo`, `isImmuneAEffetto` di classe), e scartano l'azione prima di
  provarla. Un mostro non lancia il Silenzio sul Lich, che ne è immune.
- **Quelle date da un artefatto non si conoscono**: si **scoprono**. La prima volta che un'azione va a vuoto per immunità o resistenza (la reazione "Immune a X", "Resiste a X" che la UI già mostra) lo `StatoDelCombattimento` se la
  ricorda, e da lì in poi si tiene conto: è conoscenza **pubblica**, come quella sulle reazioni dell'equipaggiamento (§3.3), e vale per tutti i personaggi dei due gruppi. Va per (personaggio colpito, tipo di danno o azione).
  Un'immunità scoperta scarta subito l'azione, senza altri tentativi.

**Quando il tiro fallisce per caso** (un Silenzio mancato) non c'è nessuna scoperta, ma non si riprova all'infinito: lo `StatoDelCombattimento` ricorda i **tentativi falliti** per (attore, bersaglio, azione), perché il tiro dipende da chi
lancia; dopo **2** fallimenti (`CostantiTattiche.MAX_TENTATIVI_FALLITI_PER_AZIONE`, per cominciare) l'azione non è più applicabile a quel bersaglio per quell'attore e la sequenza passa alla successiva. Il conto riparte a ogni locazione, come il resto dello stato. Un'azione
riuscita azzera il conto.

Lo registrano `StatoDelCombattimento.registraEsitoAzione(attore, bersaglio, azione, esito)` (colpito, mancato, immune o resistito; `EsitoAzione`) e le due letture, `getTentativiFalliti` e `isImmunitaScoperta`, che `Requisiti` e `SceltaIncantesimo` consultano. Gli esiti degli incantesimi e del dardo li registrano `IncantesimoMaleficoImpl.colpisci` e `scagliaIlDardo`; l'attacco fisico non ne registra (non si abbandona mai). Manca l'esito delle azioni con effetti che non si applicano (§8).

### 11.8 Le condizioni

Una voce ha una **lista di condizioni** (`Predicato`), tutte in **AND**; ognuna ha il flag `negato` (`NON`), come i filtri. Niente alberi con E, O e parentesi: l'O si ottiene con due voci (per tenere la scheda una lista).
Lista vuota = sempre. Le condizioni servono per ciò che **non è un controllo su un personaggio**: quelli sono filtri (§11.5) di un selettore (`SU SE_STESSO [SALUTE MINORE 50]`), e i requisiti dell'azione (la magia, la pozione, la
pergamena, l'arma da prendere) li porta l'azione (§11.7), quindi non servono né `HO_POZIONE` né `PERGAMENE`. Come per i filtri, ogni `TipoPredicato` dichiara il tipo del parametro e la lettura lo controlla.

`TipoConfronto` ha tre valori, `MINORE`, `UGUALE`, `MAGGIORE`, ed è lo stesso dei filtri numerici: "almeno 3" è `NON MINORE 3`, "al massimo 3" è `NON MAGGIORE 3`.

| Predicato | Parametro | Significato |
| :--- | :--- | :--- |
| `QUANTI` | rosa + filtri + Confronto + numero | quanti candidati ci sono nella rosa dopo i filtri |
| `ROUND` | Confronto + numero | il numero del round (`StatoDelCombattimento.getRound`) |
| `ASTUZIA` | Confronto + numero | l'astuzia di chi decide (§4, rampa sul livello) |
| `INTELLIGENZA` | Confronto + numero | l'intelligenza attuale di chi decide (non l'astuzia: sono le soglie con cui i mostri sceglievano un tempo, usate da `RegistroTattiche.tatticaDiOggi`, §11.13) |
| `PROBABILITA` | percentuale | tira il `Dado`: vero nel n% dei casi |
| `VOCE_USATA` | Confronto + numero | quante volte questa voce è stata usata da questo personaggio nello scontro (`registraUsoVoce`) |
| `ROUND_DALL_ULTIMO_USO` | Confronto + numero | i round passati dall'ultimo uso di questa voce (il *cooldown* delle future abilità forti) |

`QUANTI` è un piccolo selettore dentro una condizione (la scheda riusa l'editor dei filtri) e copre l'esistenza e i conteggi: "c'è un non morto fra gli avversari" è `QUANTI AVVERSARI [CATEGORIA NON_MORTO] MAGGIORE 0`
(con `NON`: "non ce ne sono"); "gli avversari sono almeno 3" è `NON MINORE 3` con `QUANTI AVVERSARI`; "sono l'unico vivo" è `QUANTI ALLEATI UGUALE 1`; un controllo sui propri stati è `QUANTI SE_STESSO [STATO X] UGUALE 1`.
Gli esempi: la mossa d'apertura è `ROUND UGUALE 1`; "una volta per scontro" è `VOCE_USATA UGUALE 0`; "una volta su tre" è `PROBABILITA 33`; la voce di una fascia di astuzia è `NON ASTUZIA MINORE 20`;
per tornare all'arma di prima, `QUANTI AVVERSARI [CATEGORIA NON_MORTO] UGUALE 0` e `SU SE_STESSO [ARMA_CHE_DA SACRO] → CAMBIA_ARMA`.

**Non ci sono** (si aggiungono come valori in più dell'enumerato): la stanchezza, i modificatori temporanei attivi, il tipo di locazione e l'ora, il duello, l'ondata successiva, il tipoConfronto fra il numero
degli avversari e dei miei compagni.

### 11.9 Le azioni: l'elenco

Le azioni sono un enumerato `TipoAzione` con il **nome piatto**, uno per incantesimo (`FULMINE`, non `INCANTESIMO` con un parametro): così una voce si legge come la si pensa, `SU AVVERSARI [STATO BAGNATO] → FULMINE`.
Ogni valore dichiara la sua **portata** (§11.2), le **rose che accetta** e i suoi **requisiti**. Sono ventiquattro:

| Gruppo | `TipoAzione` | Portata | Rose accettate |
| :--- | :--- | :--- | :--- |
| Mischia | `ATTACCO_FISICO` | un avversario | `AVVERSARI`; per i mostri anche `AVVERSARI_E_OSPITI` e `OSPITI` |
| Incantesimi di danno | `ARIA`, `ACQUA`, `TERRA`, `FUOCO`, `FULMINE`, `GELO`, `VELENO` | un avversario | come l'attacco |
| | `MORTE` | un avversario vivo | come l'attacco |
| | `SILENZIO` | un avversario | come l'attacco |
| Incantesimo "del repertorio" | `INCANTESIMO_DI_DANNO` (parametro: criterio, §11.10): i sette elementali, per i miei | un avversario | come l'attacco |
| | `INCANTESIMO_DEL_REPERTORIO`: un malefico qualsiasi, **Morte e Silenzio compresi**, solo per i mostri | un avversario | come l'attacco |
| Innato | `DARDO_ARCANO` | un avversario | come l'attacco |
| Benefici | `RESURREZIONE` | un alleato caduto | `ALLEATI_CADUTI` |
| | `ALBA_SACRA` | tutto il gruppo | il selettore è solo un test di esistenza |
| Canti | `CANTO_CURATIVO` | un alleato | `SE_STESSO`, `ALLEATI`, `OSPITI`, `ALLEATI_E_OSPITI` |
| | `CANTO_IPNOTICO`, `URLO_DELLA_BANSHEE` | tutti gli avversari (fino a `NUMERO_BERSAGLI`) | test di esistenza |
| | `CANTO_DI_VALORE` | tutto il gruppo | test di esistenza |
| Pozioni | `POZIONE_SALUTE`, `POZIONE_SALUTE_GRANDE`, `POZIONE_MAGIA`, `POZIONE_MAGIA_GRANDE`, `POZIONE_PANACEA` | un alleato (di solito sé) | come `CANTO_CURATIVO` |
| Equipaggiamento | `CAMBIA_ARMA` (parametro: tipo di danno voluto) | un'arma | `SE_STESSO` (chi cambia arma) |

Escluse (§5): fuga, corruzione, amicizia, passare inosservati. **Non c'è un'azione "passa il turno"**: se nessuna azione di una lista è applicabile, il personaggio fa un attacco fisico (§1, il ripiego).

**I requisiti** (li porta l'azione, §11.7; il giocatore non li scrive):

- **Incantesimi di danno, `SILENZIO`, `MORTE`:** la pergamena della classe nel gruppo, la `MAGIA` per il costo, non essere `SILENZIATO`. Il bersaglio non deve essere immune a quel tipo di danno (`SILENZIO`: all'effetto `SILENZIATO`, e non già
  silenziato); `MORTE` vuole un bersaglio vivo.
- **`DARDO_ARCANO`, canti:** la `MAGIA`, e la classe che li sa (Mago, Maga, Elfo, Elfa; Bardo, Cantastorie, il bardo della locanda). `CANTO_CURATIVO` vuole qualcuno ferito, `CANTO_IPNOTICO` e `URLO_DELLA_BANSHEE` avversari vivi
  non già `SPAVENTATO` o `CONFUSO`, e l'Urlo che il gruppo l'abbia imparato.
- **`ALBA_SACRA`:** è un'abilità innata (Sacerdote, Sacerdotessa), niente pergamene; vuole almeno un alleato con uno stato diverso da `ISPIRATO` (i modificatori temporanei non si leggono dal personaggio).
- **`RESURREZIONE`:** la pergamena, la `MAGIA`, un compagno caduto (non in panchina).
- **Pozioni:** una del tipo nel gruppo; la Salute vuole un ferito, la Magia chi non ha la `MAGIA` piena.
- **`CAMBIA_ARMA`:** solo per i miei personaggi, se c'è un'arma da prendere (`armiPerCambio`), il controllo preventivo del cambio e il massimo di cambi (§5).
- Tutte: il bersaglio non è un'immunità **nota** (di classe o scoperta, §11.7) e non ha già `N` tentativi falliti.

**Decisioni:**

- **`INCANTESIMO_DI_DANNO`** sceglie fra i sette incantesimi elementali che il personaggio può lanciare adesso; **non comprende `MORTE`** (può ritorcersi contro chi la lancia), `SILENZIO` e i benefici, che restano azioni esplicite.
  Il criterio è un parametro dell'azione. `INCANTESIMO_DEL_REPERTORIO` riproduce invece la scelta dei mostri di un tempo (§11.13).
- **Pozioni:** quattro azioni (Salute e Magia, normale e grande: ognuna beve solo la sua); la scelta fra normale e grande la scrive la voce, con l'ordine delle azioni (§11.16).
- **Incantesimi a più bersagli** (`MULTIPLO`, fino a `NUMERO_BERSAGLI`): il selettore sceglie il **bersaglio principale** e gli altri sono i successivi vivi dell'elenco degli avversari. Il danno si ripartisce per posizione (principale intero, poi 50%, 25%, 12,5%, 6,25%, che è il minimo: `CalcolatoreCombattimento.quotaDelDannoPerPosizione`) e la valutazione somma i danni attesi con le stesse quote.
- **Un test** controlla che ogni valore di `TipoIncantesimo` abbia il suo `TipoAzione` (un nuovo incantesimo senza azione fa fallire il test).

**Il bersaglio scelto, per tutte le azioni.** L'`EsecutoreTattiche` (§11.14) esegue ogni azione sul bersaglio scelto dal valutatore (per i `MULTIPLO` il principale). Le azioni manuali del giocatore (barra dei comandi) continuano a scegliere da sole: l'attacco colpisce il primo avversario vivo (`AttaccoConLeArmi.esegui`),
gli incantesimi i primi vivi, il dardo il primo, il Silenzio il primo non silenziato (`Silenzio.scegliBersaglio`), il canto curativo il più ferito.

### 11.10 Il criterio di scelta dell'incantesimo di danno

Vale per `INCANTESIMO_DI_DANNO` (§11.9), la sola azione che sceglie *quale* incantesimo lanciare. Le sue tre logiche (a caso, il più potente, il più conveniente) sono quelle con cui i mostri sceglievano un tempo in base all'`INTELLIGENZA` (sotto 5, da 5 a 6, da 7: §11.13). Nelle tattiche:

- **Quelli che può lanciare adesso**: per un mostro il suo repertorio di incantesimi elementali che può pagare con la `MAGIA`; per un mio personaggio le pergamene della scorta del gruppo (`getIncantesimi(classe) > 0`) che sa
  formulare (`puoFormulare`) e può pagare. Non `SILENZIATO`. Si scartano gli incantesimi a cui il bersaglio è immune per una conoscenza **nota** (§11.7) e quelli con troppi tentativi falliti. Se non ne resta nessuno l'azione non è applicabile.
- **"Una volta su tre non lancia"** è la condizione `PROBABILITA 67` della voce.
- **Le soglie di `INTELLIGENZA`** (5 e 7) valgono solo per la replica di §11.13: le tattiche base dei mostri usano le fasce di astuzia (§4, 0-11, 12-19, 20-30, da 31).

Il **criterio** (`CriterioIncantesimo`) è un parametro dell'azione:

| Valore | Sigla | Che cosa sceglie |
| :--- | :--- | :--- |
| `A_CASO` | S1 | uno a caso fra quelli lanciabili |
| `PIU_POTENTE` | S2 | quello con più danno base (`getDanni()`, come oggi), senza resistenze né debolezze; con più bersagli conta il danno a uno solo |
| `PIU_ADATTO` | S3 | quello con il **danno atteso** più alto su *quel* bersaglio: probabilità di colpire × `dannoAttesoDellArma` × `moltiplicatoreAtteso` (debolezze e resistenze di classe e stati, §6.3); con più bersagli il danno totale |
| `PIU_ADATTO_ESPERTO` | S4 | come S3, moltiplicato per il fattore dell'equipaggiamento **osservato** (`getFattoreEquipaggiamentoNoto`, §3.3); con più bersagli conta anche chi verrebbe portato vicino alla fine (la quota di salute tolta a ogni bersaglio, al massimo 1, sommata) |
| `PIU_ECONOMICO_CHE_BASTA` | — | per chi ha poche pergamene: fra quelli il cui danno atteso (come S4) basta a **uccidere** il bersaglio, il meno caro (il prezzo della pergamena per i miei, il costo di lancio in `MAGIA` per i mostri); se nessuno basta, il `PIU_ADATTO_ESPERTO` |

**Il tipoConfronto con l'attacco fisico** è un secondo parametro, `soloSePiuForteDellAttacco`: se vero, l'incantesimo scelto è applicabile solo se il suo danno atteso (probabilità di colpire × danno × `moltiplicatoreAtteso`) è maggiore di quello delle fasi di
attacco con le armi sullo stesso bersaglio; altrimenti non lo è, e la sequenza passa all'azione successiva (di solito `ATTACCO_FISICO`). Il danno atteso dell'arma si calcola senza dadi come quello dell'incantesimo.

**Le fasce di astuzia nella tattica base di un mostro magico** (§11.16): la prima fascia (0-11) lancia a caso (`A_CASO`, con una probabilità), la seconda (12-19) il `PIU_POTENTE` solo se rende più dell'attacco, la terza (20-30) il `PIU_ADATTO`, la quarta (da 31) il `PIU_ADATTO_ESPERTO`; sempre seguite da `ATTACCO_FISICO`. I valori sono da tarare con le simulazioni (§8).

Il registro delle decisioni (§7.7) riporta i danni attesi confrontati ("PIU_ADATTO: FUOCO 4.2, FULMINE 9.8"). Lo coprono `ValutatoreTest` e `TatticheBaseTest`.

### 11.11 Dove stanno i dati e la logica

Le classi che descrivono una tattica sono **dati puri**: immutabili, fatti solo di enumerati (`tipi`), numeri e liste. Stanno nel pacchetto **`…/tattiche`** (modulo `08-foresta-tattiche`), accanto alla logica, con il suffisso `MD` come il resto del modello dati
(`TatticaMD`, `VoceMD`, `SelettoreMD`, `FiltroMD`, `OrdinamentoMD`, `PredicatoMD`, `AzioneMD`). Il motivo è che la stessa classe serve a tre cose: la **UI** le legge e le modifica (può importare `modellodati` e `tipi`, non il motore né il dominio), il **salvataggio**
le scrive con il personaggio (`PersonaggioMD`, §11.12) e il **motore** le valuta. Il modulo `10-foresta-modellodati` non vede il motore, e quindi nemmeno `tattiche`: dipendono solo da `tipi` e fra loro.

La **logica** sta in `…/tattiche`: le rose (`Rosa`, `Rose`), `Filtri`, `Ordinamenti`, `Predicati`, `Selettori` (applica un `SelettoreMD`), `Requisiti`, `SceltaIncantesimo`, `Astuzia`, `Valutatore`, il `ContestoTattico` e i risultati (`RisultatoSelezione`, `Decisione`). Non la
può importare la UI: sta in `12-foresta-motore`, che non vede `12-foresta-swing`.

Il valutatore e il contesto: `Valutatore.decidi` (la prima voce eseguibile; ripiego sull'attacco fisico al primo avversario) restituisce una `Decisione` (azione, bersaglio, perché). Il `Selettore` accetta un'`Ammissione`: restano nella rosa solo i candidati su cui un'azione della voce si può fare, e i filtri che dipendono dall'azione la valutano per quella che si farebbe su ciascuno.
`Requisiti` dice quando si può fare ciascuna azione, con il motivo del no; `Astuzia` calcola l'astuzia (§4); `SceltaIncantesimo` e `CriterioIncantesimo` scelgono l'incantesimo (§11.10), con `CalcolatoreCombattimento.dannoAtteso` e `dannoAttesoDellaMischia` (senza dadi).
Un `ROUND_DALL_ULTIMO_USO` di una voce mai usata vale "moltissimi". I test sono `RosaTest`, `ContestoTatticoTest`, `SelezioneTest`, `PredicatiTest` e `ValutatoreTest`.

**Scelte dei requisiti** (da rivedere se non convincono): un incantesimo si può lanciare se il personaggio non è silenziato, ha la `MAGIA` e ha una pergamena nel gruppo, oppure è un mostro magico che lo ha nel repertorio; l'Alba sacra serve se un alleato ha uno
stato diverso da `ISPIRATO`; i canti ipnotico e dell'Urlo servono se c'è un avversario né `SPAVENTATO` né `CONFUSO`; il Canto di valore se c'è un alleato non `ISPIRATO`; le pozioni vogliono una scorta nel
gruppo e un bersaglio ferito (Salute) o senza la `MAGIA` piena; il cambio d'arma solo per i miei, se c'è un'arma da prendere (`armiPerCambio`).

### 11.12 Il salvataggio

La tattica (`SalvataggioTatticheTest`, `CombattimentoConTatticheTest`) sta in `TatticheMD` (`TatticheDeiPersonaggi.di`/`imposta`, legata al personaggio dal suo `uuid`: il personaggio non sa di averne una) e passa dal salvataggio con le altre parti del modello dati: il numero di tattiche e, per ognuna, l'`uuid` e la tattica (i mostri non compaiono). Una
tattica vuota (`TatticaMD.VUOTA`) resta una tattica vuota e non diventa "nessuna tattica": sono due cose diverse (i miei personaggi senza tattica usano la predefinita del loro ruolo, i mostri quella della classe, `RegistroTattiche.perClasse`).

**Una tattica incoerente non fa fallire il caricamento.** I dati malformati (un nome sconosciuto, un numero scritto male, una voce senza azioni) fanno ancora fallire la rilettura; ma una tattica ben formata con errori di coerenza (un attacco sugli alleati, una rosa che le azioni non accettano: `TatticaMD.problemiPerQualsiasiLato`, che non sa se è di un mostro o di un mio personaggio) viene **disabilitata** (`TatticaMD.disabilitata`, il flag si salva) e conservata. Chi agisce la tratta come se non ci fosse (`LocazioneBase.tatticaDi`: la predefinita del ruolo), e la schermata delle tattiche la mostra con un avviso in rosso e gli errori, perché il giocatore li corregga e confermi. Il motore non accetta dalla schermata una tattica con errori (`ComandoImpostaTattica`).

Il formato, nello stile del resto del modello dati (una riga per elemento, campi separati da `|`, enumerati per nome, campi mancanti vuoti, gli elementi che ne contengono altri dicono quanti sono):

```
TATTICA|numero di voci|disabilitata (0 o 1)
VOCE|attiva|ha un selettore|numero di condizioni|numero di azioni|nome dato dal giocatore (vuoto se non ne ha)
SELETTORE|rosa|ordinamento|tipo di danno dell'ordinamento|numero di filtri       (seguono i filtri)
FILTRO|tipo|negato|tipoConfronto|numero|stato|classe|categoria|tipo di danno
PREDICATO|tipo|negato|tipoConfronto|numero|rosa|numero di filtri                     (seguono i filtri)
AZIONE|tipo|criterio|solo se più forte dell'attacco|tipo di danno dell'arma
```

Gli elementi sono immutabili, quindi non implementano `Serializzabile` (che rilegge dentro un oggetto): hanno `salva(PrintWriter)` e un `leggi(BufferedReader)` statico che li costruisce con le stesse fabbriche che li controllano. Un salvataggio incoerente (un'etichetta sbagliata, un enumerato sconosciuto, un parametro mancante, una voce senza azioni,
il file troncato) è rifiutato con una `IOException`. Il test di andata e ritorno scrive una tattica con almeno un esemplare di ogni forma di filtro, predicato e azione e controlla che torni uguale (le classi hanno `equals`) e che riscrivere dia lo stesso testo; un
altro salva e ricarica una partita vera e controlla che il personaggio rilegga la tattica e agisca come impostato.

### 11.13 La replica del vecchio comportamento dei mostri

`RegistroTattiche.tatticaDiOggi()` (`RegistroTatticheTest`, `CombattimentoConTatticheTest`) è la **trasposizione voce per voce** di come i mostri sceglievano prima delle tattiche (il vecchio `PersonaggioBase.attacca(Gruppo)`), con venti voci generate dal codice. Oggi serve da ripiego per le classi di mostri
senza una tattica base in `TatticheBase` (`RegistroTattiche.perClasse`: quella registrata con `registra`, altrimenti `TatticheBase.deiMostri`, altrimenti questa) e da tattica di riferimento per i test; le simulazioni di §9 la usavano. Il registro non copia la tattica nei personaggi (non si salva con ogni mostro): la cerca quando serve (`LocazioneBase.tatticaDi`), e un'istanza ne può avere una sua.

- **Chi colpire**, cinque selettori: il Mago o la Maga di livello più alto (`CLASSE MAGO` con `LIVELLO_PIU_ALTO`, a parità il primo), altrimenti il Sacerdote, l'Elfo, il Bardo (ognuno nel ruolo, con la variante di genere), altrimenti uno a caso fra i miei personaggi vivi e gli ospiti vulnerabili (`AVVERSARI_E_OSPITI`, `A_CASO`).
- **Se e quale incantesimo**, quattro voci per ogni bersaglio, nell'ordine: (1) poco intelligente (`INTELLIGENZA` sotto 5): `INCANTESIMO_DEL_REPERTORIO` a caso; (2) da 5 a 6: il più potente (`PIU_POTENTE`, il danno base più alto); (3) da 7: il più potente solo se rende più dell'attacco con le armi
  (`soloSePiuForteDellAttacco`, con i danni attesi); (4) l'attacco con le armi. Le prime tre hanno le condizioni `QUANTI SE_STESSO [MAGICO] MAGGIORE 0` (non lancia se non è magico, è silenziato o ha 0 `MAGIA`: e non tira nemmeno il dado) e `PROBABILITA 67` (una volta su tre rinuncia), e
  l'attacco con le armi come ripiego nella loro sequenza di azioni.
- **Un mostro colpisce solo il bersaglio scelto**, anche con un incantesimo a più bersagli (`SceltaIncantesimo.bersagli`).
- **Rispetto al codice di prima**, un mostro non lancia un incantesimo a cui il bersaglio è immune per la sua classe né il Silenzio su chi è già silenziato (prima sprecava il turno); il tipoConfronto fra incantesimo e armi usa i danni attesi e non i tiri dei dadi; il dado della rinuncia si tira una volta per voce che ha un bersaglio, non una sola per decisione (la
  probabilità di lanciare resta due su tre).

### 11.14 L'esecutore condiviso

(`tattiche/EsecutoreTattiche`, `EsecutoreTatticheTest`). Il valutatore sceglie, l'esecutore fa: un attacco con le armi sul bersaglio scelto, un incantesimo (con il `GruppoDiBersagli` per i `MULTIPLO`, la pergamena consumata, l'esito registrato), il dardo arcano, i benefici, i canti, le pozioni, il
cambio d'arma. `agisci(attore, tattica)` decide, pubblica l'`InternoDecisioneTattica`, esegue e ricorda l'uso della voce, e restituisce la decisione (null se non c'era niente da fare). Non gestisce il round né la fine dello scontro: chi lo chiama guarda se gli avversari sono finiti o se è
caduto il capo. Gli avversari messi fuori combattimento dai miei li comunica a un `RegistroUccisioni` (nel gioco danno esperienza e punti, `LocazioneBase.registraUccisione`; nelle simulazioni niente).

Lo usano `LocazioneBase` (che gli passa i suoi gruppi e lo stato e tiene per sé il round, le opzioni di corruzione e amicizia, e le azioni manuali del giocatore, che per l'attacco e il dardo passano dalle sue stesse primitive) e `RoundASimulato`, quindi `TestScontriDiGruppo` e `TestLogoramentoInizioPartita`: **i mostri
delle simulazioni agiscono con le stesse tattiche del gioco**, non più con una copia della logica di scelta. `ContestoTattico.per(attore, giocatore, mostri)` lo costruisce da gruppi qualunque.

**Come lo usa `LocazioneBase`**: a ogni scatto del TIMER il personaggio di turno agisce con la sua tattica (`TatticheDeiPersonaggi.di`, salvata in `TatticheMD`, §11.12; `LocazioneBase.tatticaDi` sceglie quella da usare: la sua, la predefinita del ruolo per i miei che non ne hanno o hanno una disabilitata, quella della classe per i mostri). Le azioni manuali del giocatore (barra dei comandi) passano dalle primitive di `LocazioneBase`. Lo coprono `CombattimentoConTatticheTest` e `EsecutoreTatticheTest`.

### 11.15 La schermata del giocatore

(`ui/tattiche/DisplayableCanvasTattiche` e `CasellaADiscesa`, in `12-foresta-swing`; `tattiche/modifica/BozzaTattica` e `TestoTattica`, in `08-foresta-tattiche`; `DisplayableCanvasTatticheTest`, `BozzaTatticaTest`, `ScenarioSchermataTatticheTest`). Si apre dall'inventario con l'icona `MOSTRA_TATTICHE` (stato `Stato.TATTICHE`, come la pagina dei trofei) e si lascia con `ANNULLA`. I comandi della barra sono i personaggi permanenti del gruppo, anche i caduti
(non gli ospiti e chi è a tempo); il capo ha lo sfondo rosso. Due colonne con la cornice dell'inventario e il personaggio al centro:

- **A sinistra la tattica**, un `ComponenteScorrevole` ricostruito a ogni disegno. Ogni voce ha una riga di intestazione (numero, **nome**; a destra su, giù, togli) e sotto il corpo: la riga della rosa e dell'ordinamento, i suoi **filtri** (`con`), le **condizioni** (`se`, con i filtri di `QUANTI` più indentati) e le **azioni** (`fai`); in coda a ogni gruppo una riga "(aggiungi...)" con un
  menu. Il numero della voce la accende e la spegne: verde se è attiva, rosso se è disattivata (e viene saltata); una voce disattivata ha tutta la riga, e le sue righe sotto, in grigio scuro. Il **nome** (`VoceMD.getNome`, salvato con la voce; ogni voce ha sempre un nome: quello predefinito è "Tattica" e il numero della voce al momento in cui nasce o si legge) si clicca per **ripiegare** la voce, che mostra solo l'intestazione (`BozzaTattica.Voce.chiusa`, non si salva), e con un doppio click si **rinomina** con il `Prompt` già compilato (`Prompt.mostra(testo, alInvio)`: l'invio non passa dal motore, Esc rinuncia; un nome vuoto torna al predefinito del posto). Il doppio click lo riconosce la schermata stessa, per non ritardare gli altri click. In fondo "Nuova voce", che aggiunge una voce **completamente vuota** (né rosa né azioni: la rosa arriva con la prima azione, o si sceglie, e le azioni proposte sono quelle che la accettano; finché non ha un'azione la bozza non si conferma), e l'attacco fisico di riserva, che c'è sempre e non si tocca. Le righe lunghe vanno a capo.
- **Ogni riga è fatta di pezzi** (`Token`): testo fisso, testo che apre un menu a discesa, `-` e `+` per i numeri, icone. Le scelte valide dipendono dalla voce: le rose le accettano tutte le sue azioni (`BozzaTattica.roseAmmesse`), le azioni sono della stessa portata (`azioniAmmesse`); aggiungendo un'azione che non accetta la rosa la voce passa alla prima che va bene.
- **Ognuno ha solo le azioni che sa fare** (`RegistroTattiche.azioniPossibili`, che il motore manda con la richiesta di apertura): i canti solo il bardo, il dardo arcano maghi ed elfi, l'Alba sacra i sacerdoti; le pergamene, le pozioni e l'attacco chiunque. Su che cosa agisce una voce dipende dalle sue azioni (un attacco solo sugli avversari): con una sola rosa possibile il nome non è un menu, e passandoci sopra il mouse dice perché.
- **"Quanti" non conta su se stessi**: la rosa di una condizione `QUANTI` non propone "Se stesso" (un candidato solo); se una tattica letta l'ha già, resta e si vede.
- **Niente "non" sui numeri**: i filtri numerici (salute, salute %, magia, livello, livello rispetto al mio) e le condizioni non hanno l'interruttore di negazione; il tipoConfronto offre tre relazioni (minore di, uguale a, maggiore di): con numeri interi "al massimo 50" è "minore di 51", e si va a grandi passi. Una tattica letta che ha un rovescio ("almeno", "al massimo") lo mostra comunque. Per gli altri filtri la parola "con" è l'interruttore: "con" o "senza".
- **Le condizioni `ASTUZIA` e `INTELLIGENZA` non si propongono**: servono a dividere i mostri in fasce di furbizia (§4, §11.16), non hanno senso per i personaggi del gruppo (`BozzaTattica.predicatiAmmessi`).
- **Chi mi ha attaccato per ultimo e l'ultimo scelto sono ordinamenti**, non filtri, e solo fra gli avversari (`ULTIMO_ATTACCANTE`, `ULTIMO_SCELTO`): si scelgono accanto a "il primo", "con meno salute"... Scelgono **solo** quel personaggio: se non è fra i candidati (non c'è, o è caduto) la voce non si esegue e si passa alla successiva. I filtri con lo stesso nome (e "il capo", "me stesso") esistono ancora nei dati ma non si propongono: contando o scegliendo fra gli alleati non hanno senso.
- **Cosa si può scegliere dipende dalla rosa**: su "Se stesso" non compaiono il menu dell'ordinamento né i filtri per classe, categoria, livello rispetto al mio, il capo, me stesso, scelto l'ultima volta, mi ha attaccato per ultimo, debole all'azione (`BozzaTattica.filtriAmmessi`): il candidato è uno solo e già deciso.
- **Il menu a discesa** (`CasellaADiscesa`) è un popup nel font Doomdark con un `ComponenteScorrevole` (rotella e frecce se è lungo), che si apre sotto o sopra la riga e cattura i click finché è aperto.
- **Al centro** il personaggio, le frecce che lo spostano nel gruppo (`ComandoSpostaPersonaggio`, subito, anche il capo: il gruppo ricorda chi è il capo e il suo posto si salva, `GruppoMD.indiceCapo`), **Sì** e **No** (solo se la bozza è diversa dalla tattica del motore; Sì solo se non ci sono problemi) e **Predefinita**, che rimette nella bozza la tattica predefinita del ruolo (`RegistroTattiche.predefinitaDelGiocabile`, §11.16; da confermare).
- **A destra la spiegazione**. Prima dell'elemento sotto il mouse: la rosa, il filtro, la condizione, l'azione, l'ordinamento, il tipoConfronto (`TestoTattica.spiega`), e con un menu aperto quella della scelta su cui si passa, o del pezzo che lo ha aperto; poi come si legge la voce a cui appartiene (`TestoTattica.spiegazione`) e, in rosso, i problemi che impediscono di confermare (`BozzaTattica.problemi`). Il filtro che si chiamava "Magico" ora è "Magie abilitate".

La bozza (`BozzaTattica`) tiene le voci in liste modificabili, perché i dati `*MD` sono immutabili: solo con Sì si costruisce la `TatticaMD` e la si manda con `ComandoImpostaTattica(uuid, tattica)`; il motore la accetta solo con la schermata aperta, quindi **mai in combattimento**, e per un personaggio permanente del gruppo. Cambiando personaggio le modifiche non confermate si perdono. Aprendo una tattica, i confronti rovesciati sui numeri ("almeno 50") diventano quelli equivalenti senza rovescio ("maggiore di 49"): `BozzaTattica.normalizza`.

### 11.16 Le tattiche base dei mostri e le predefinite dei giocabili

(`tattiche/TatticheBase`, `RegistroTattiche.perClasse` e `predefinitaDelGiocabile`, `TatticheBaseTest`). Sono scritte con le voci che il giocatore scrive nella schermata (§11.15) e salvano e rileggono come ogni tattica. Ogni voce ha un nome (`voce(nome, ...)` in `TatticheBase`): quelle dei mostri portano la fascia davanti ("Astuto: finisce i feriti"), quelle dei giocabili dicono che cosa fanno ("Alba sacra", "Pozione di salute grande"). Le fonti: il codice del gioco (che cosa sa fare ogni classe: repertorio, categoria, intelligenza, debolezze) e i
manuali di tattica per il gioco di ruolo, che legano il comportamento dei mostri alla loro intelligenza (sotto una certa soglia agiscono d'istinto, contro il più vicino o chi li ha colpiti; più su scelgono i bersagli deboli, poi gli incantatori, infine coordinano le mosse), per esempio *The Monsters Know What They're Doing* di Keith Ammann.

**Mostri: una lista per classe, a fasce di astuzia** (le soglie di §4: 12, 20, 31), dalla più alta alla più bassa; ogni fascia comincia con `NON ASTUZIA MINORE soglia`, e un mostro ha solo le fasce che la sua astuzia massima raggiunge. La prima voce eseguibile vince, quindi se una voce della fascia alta non si può eseguire
(niente MAGIA, Silenzio già riuscito) si scende alle voci più semplici, fino all'ultima, che attacca sempre.

| Fascia | Come si comporta |
| :--- | :--- |
| **Bruto** (sotto 12) | Se è vendicativo (bestie, Troll, Minotauro, Goblin) nel 50% dei casi colpisce chi l'ha colpito per ultimo; altrimenti uno a caso fra avversari e ospiti. Se sa fare magie ne lancia una a caso (50%, Folletto 80%), altrimenti attacca. |
| **Astuto** (da 12) | Finisce chi è sotto il 30% della salute; poi (Hobgoblin) continua sul bersaglio dell'ultima volta; poi un incantatore a caso (i giganti, non magici, il più forte), poi il più ferito; la magia solo se rende più dell'attacco con le armi. |
| **Esperto** (da 20) | Sui feriti (sotto il 35%) l'incantesimo più economico che basta a ucciderli; il Silenzio sull'incantatore con più magia dove lo ha; poi l'incantesimo più adatto al bersaglio. I giganti vanno sul più forte. |
| **Stratega** (da 31) | La Morte sul quasi caduto (sotto il 25%, `Morte.puoUccidere`, dove uccide: altrimenti si ritorcerebbe contro chi la lancia; Morte e Silenzio registrano l'esito, così i tentativi falliti fermano la ripetizione); il Silenzio sull'incantatore con più magia; il colpo finale; gli incantesimi più adatti conoscendo l'equipaggiamento (`PIU_ADATTO_ESPERTO`). |

Che cosa fa cosa dipende da tre elenchi in `TatticheBase`, da tenere allineati con il codice (`PersonaggioBase.getRepertorioIncantesimi` e le classi): chi è magico, chi ha il Silenzio, chi ha la Morte (i boss). Derivarli dal codice è in §8. Come i mostri salgono di fascia col livello del mondo lo dice la rampa dell'astuzia (§4): il Drago è stratega fin dal primo livello, la Strega e il Lich dal terzo, l'Ombra Nera
dal nono; le bestie e i bruti restano bruti.

**Personaggi giocabili: una tattica predefinita per ruolo** (uguale per i due generi), che la schermata propone col pulsante "Predefinita". Un personaggio **senza tattica propria agisce con la predefinita del suo ruolo** (`LocazioneBase.tatticaDi`, e la schermata mostra quella); una tattica vuota esplicita (`TatticaMD.VUOTA`) vuol dire invece "solo l'attacco di riserva". `RegistroTattiche.predefinitaDelGiocabile` la dà (la vuota per le classi che non ne hanno una).

| Ruolo | Tattica |
| :--- | :--- |
| **Guerriero** | Si cura da solo sotto il 30% (pozione della Salute); finisce i feriti (sotto il 50%); poi i maghi nemici; poi il primo. |
| **Ladro** | Si cura da solo sotto il 30%; i feriti, poi chi ha meno salute. |
| **Mago** | Pozioni della Salute (25%) e della Magia (sotto 20 di MAGIA); Silenzio sui maghi nemici (60%); sui feriti (sotto il 40%) l'incantesimo più economico che basta, il dardo arcano, l'attacco; poi contro il più forte l'incantesimo più adatto (se rende più dell'attacco), il dardo. |
| **Elfo** | Come il guerriero per le cure; dardo arcano e incantesimo solo dove rendono più dell'attacco; i maghi nemici per primi. |
| **Sacerdote** | Cura gli altri (anche gli ospiti) sotto il 40%; risuscita i caduti; Alba sacra se almeno due alleati hanno uno stato da togliere (`STATO_NEGATIVO`, non contano ISPIRATO e BERSERK) e sono passati almeno tre round dall'ultima volta (`ROUND_DALL_ULTIMO_USO`); pozione della Magia; poi colpisce il più ferito. |
| **Bardo** | Pozione (25%); canto curativo sotto il 60%; canto di valore a chi non è ispirato; Urlo della Banshee o canto ipnotico contro più di un avversario; poi colpisce il più ferito. |

**Le pozioni** sono quattro azioni (Salute e Magia, normale e grande, ognuna beve solo la sua): le predefinite hanno due voci per tipo, la prima con la grande quando manca molto (sotto la metà della soglia), la seconda con la normale e, se non ce n'è, la grande.

Le azioni a costo (pergamene, MAGIA, pozioni) le verificano i requisiti (§11.7): una voce che non si può eseguire cede alla successiva, quindi le predefinite funzionano anche con le scorte vuote. Le predefinite non conoscono ancora il livello del personaggio né la scorta rimasta (per esempio "risparmia le pergamene").

**Verifica**: una simulazione manuale (`TestScontriDiGruppo`) con le nuove tattiche dei mostri dà gruppi completi che vincono quasi sempre e il Drago che mette in difficoltà i gruppi di tre; i numeri non si confrontano con quelli delle versioni precedenti.
