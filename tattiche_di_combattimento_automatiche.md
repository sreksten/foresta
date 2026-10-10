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
| **Resurrezione** | chi la sa | pergamena e `MAGIA` | un solo bersaglio, anche morto (`SINGOLO_QUALSIASI`); contro i non morti fa danno | su un alleato caduto o su un non morto |
| **Alba sacra** | Sacerdote/Sacerdotessa (innata) e chi ha la pergamena | `MAGIA` | tutto il gruppo (toglie stati e modificatori temporanei) | tipicamente se qualcuno ha uno stato negativo |
| **Dardo arcano** | Mago, Maga, Elfo, Elfa (innato) | `MAGIA` | un avversario | |
| **Canto curativo** | Bardo, Cantastorie, bardo della locanda | `MAGIA` (4); solo se qualcuno è ferito | il più ferito | |
| **Canto ipnotico** | idem | `MAGIA` (3); solo contro avversari vivi | fino a `NUMERO_BERSAGLI` avversari (`SPAVENTATO` o `CONFUSO`) | |
| **Urlo della Banshee** | idem, ma solo dopo che il gruppo l'ha imparato (il bardo della locanda lo sa sempre) | `MAGIA` (6); solo contro avversari vivi | fino a `NUMERO_BERSAGLI` avversari: chi cede prende `SPAVENTATO` e `CONFUSO` insieme | un'alternativa più forte al canto ipnotico |
| **Canto di valore** | idem | `MAGIA` (5) | tutto il gruppo (`ISPIRATO`) | a inizio scontro |
| **Pozione della Salute** (normale, grande) | tutti | almeno una nel gruppo | un personaggio del gruppo (chi beve) | |
| **Pozione della Magia** (normale, grande) | tutti | almeno una nel gruppo | un personaggio del gruppo | |

**Un incantesimo "del repertorio".** Oltre a una classe precisa, la voce può dire "un incantesimo malefico qualsiasi" con un **criterio di
scelta**: *a caso*, *il più potente* o *il più conveniente* (probabilità di colpire × danno atteso, contro l'attacco fisico): sono le tre
logiche che oggi usano i mostri in base all'intelligenza (§4). Vale anche per i personaggi del giocatore.

**Il silenzio è un'azione.** Lo stato `SILENZIATO` lo mette l'incantesimo del Silenzio (riga sopra), o con una probabilità un attacco di
tipo **arcano** (il Dardo arcano di Mago ed Elfo, l'arma naturale del Lich, gli incantamenti arcani). Una voce "silenzia il mago" è
"azione: Silenzio" su un avversario magico non ancora silenziato, e subito dopo, per chi non ha la pergamena o la `MAGIA`, "azione:
Dardo arcano" sullo stesso bersaglio.

Nei giochi di riferimento ci sono azioni che qui non esistono: **muoversi, seguire, mantenere la posizione, cambiare set di armi,
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
| **Magico**, che sa lanciare incantesimi (per silenziarlo o colpirlo per primo) | DA:O (*Mage*), FFXII | **parziale**: `isMagico()` (livellamento magia > 0) dice che ha la magia; il repertorio (`getRepertorioIncantesimi`) è protetto e va esposto. Sono magici anche boss, arpie, folletti... non solo i maghi |
| Di una **categoria** (non morto, bestia, umanoide, volante, drago) | DA:O (*creature type*), FFXII (*undead, flying*) | **da aggiungere**: le classi non hanno una categoria; i non morti sono noti (Fantasma, Ombra Nera, Spettro, Spirito, Scheletro) ma vanno elencati in una tabella |
| Un **boss** / di rango elevato | DA:O (*rank*) | **da aggiungere**: non c'è un flag; si può derivare da un elenco di classi (Drago, Strega, Lich, Idra, Minotauro...: i guardiani dei castelli) |
| Con un certo **effetto di stato** (stordito, avvelenato, congelato...) | DA:O, FFXII (*status*) | **sì**: `hasEffettoDiStato(tipo)`, 19 tipi (`TipoEffettoDiStato`) |
| **Senza** un certo effetto (non ancora silenziato, non ancora avvelenato) | FFXII (*status ≠*) | **sì**: `!hasEffettoDiStato(tipo)` |
| **Debole a un elemento** | DA:O (*vulnerability*), FFXII (*weak to element*) | **parziale**: `difesaContro(tipoDanno)` e `haInterazioneCon(tipoDanno)` (per esempio `BAGNATO` + fulmine); non c'è una lista di debolezze già pronta |
| Non **immune** a quell'incantesimo, tipo di danno o stato (per non sprecare la magia) | DA:O, FFXII | **sì**: `isImmuneAIncantesimo`, `isImmuneATipoDanno`, `isImmuneAEffetto` |
| Con una certa **armatura** (pesante, leggera) | DA:O (*armor*) | **parziale**: i personaggi hanno elmo, scudo, armatura e schinieri con resistenze; i mostri combattono con l'arma naturale |
| **Lo stesso del turno precedente / del compagno** (fuoco concentrato) | FFXII (*last target, leader's target*) | **da aggiungere**: basta uno stato del combattimento con l'ultimo bersaglio di ogni personaggio |
| Quello che **attacca me / un alleato** | DA:O (*targeting*), FFXII | **parziale**: oggi si risponde a chi ha agito; per un "bersaglio dell'avversario" persistente va ricordato l'ultimo bersaglio di ognuno |
| Che sta **lanciando** un incantesimo | DA:O, FFXII | **no**: gli incantesimi sono istantanei |
| A distanza o in mischia | DA:O, FFXII | **no**: non c'è posizione, né armi a distanza (spada, mazza, ascia, spadone, lancia, bastone magico) |

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
| Con una certa arma | DA:O (*weapon set*) | **sì**: `getArmaEquipaggiata()`, `getArmaSecondaria()`; non si cambia in combattimento |
| In panchina / fuori combattimento | — | **sì**: `isInPanchina()`, `isFuoriCombattimento()` (il personaggio non agisce) |

### 7.2 Sul gruppo e sullo scontro

| Controllo | Giochi | In Foresta |
| :--- | :--- | :--- |
| Numero di avversari vivi ≥ N, o uno solo | DA:O (*enemies in range ≥ n*) | **sì**: `gruppoAvversario.getNumeroPersonaggiVivi()` |
| Numero di compagni vivi, o sono l'unico vivo | FFXII (*alone*) | **sì**: `getNumeroPersonaggiVivi()` |
| Ci sono avversari di una classe o categoria | DA:O (*any enemy of type...*) | **sì** per le classi, **da aggiungere** per le categorie (§6.1) |
| Il gruppo avversario è più numeroso (o meno) del mio | (specifico) | **sì**: serve a decidere, per esempio, se curarsi o attaccare |
| Il numero del turno (da N in poi, ogni N turni) | BG (*round time*), DA:O (cooldown) | **da aggiungere**: oggi non c'è un contatore del round |
| Una volta per scontro / non più di una volta | DA:O (cooldown), FFXII (*once*) | **da aggiungere**: un insieme "voci già usate in questo scontro" nello stato del combattimento |
| Duello, combattimento fino alla resa, ondata successiva | (specifico) | **sì**: sono flag del personaggio (`isFinoAllaResa`, `isSfidante`) e dello scontro (ondate) |

### 7.3 Risorse e contesto

| Controllo | Giochi | In Foresta |
| :--- | :--- | :--- |
| Ho una pozione (della Salute, della Magia, grande) | DA:O (*item*), FFXII (*item*) | **sì**: `getPozioniSalute()`, `getPozioniSaluteGrande()`, `getPozioniMagia()`, `getPozioniMagiaGrande()` |
| Quante pergamene di un incantesimo restano | (specifico) | **sì**: `getIncantesimi(classe)` |
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
- una **categoria di creatura** e il riconoscimento di un **boss**: tabelle per classe;
- l'accesso al **repertorio di incantesimi** di un avversario (oggi protetto) per il selettore "magico";
- le **debolezze elementali** come dato esplicito, se si vuole il selettore "debole al fuoco" (oggi si ricava da `difesaContro` e
  dalle interazioni con gli stati);
- la **risposta** come azione a sé degli avversari (oggi è `rispondiInMischia` sul combattente, con la logica fissa di §4);
- le **tattiche base dei mostri**: un set per classe o per fascia di intelligenza, in un file di risorse, e il modo di assegnarlo a
  un avversario quando nasce (`FabbricaPersonaggi`);
- un **criterio di scelta** dell'incantesimo (a caso, il più potente, il più conveniente) come parametro dell'azione, e la
  condizione **"probabilità del n%"** (oggi "una volta su tre non lancia" è una costante).

Non ha senso importare: posizione e distanza, mischia contro distanza, linea di vista, incantesimi con tempo di lancio, cambio di set di
armi in combattimento, movimento.

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
  numero di avversari, risorse, probabilità; poi categorie di creatura e boss).
- **Il round a coppie** (§3.2): quando rivederlo, e se i personaggi in più possono agire senza risposta anche dopo il primo giro.
- Il **registro del turno**: dire in una riga perché il personaggio ha scelto quell'azione ("Mira lancia Gelo su Strega: la sola
  maga"), utile al giocatore per mettere a punto le tattiche, e ai test sulle tattiche dei mostri.
