# 08-foresta-tattiche

**Come un personaggio sceglie che cosa fare in uno scontro.** Le tattiche sono dati (liste di voci scritte con selettori, condizioni e azioni) che si possono costruire, salvare e modificare dal giocatore; il modulo contiene i dati, la logica che li valuta (il *valutatore*) e quella che esegue la decisione (l'*esecutore*), e le tattiche di partenza dei mostri e dei personaggi giocabili.

È un modulo a sé perché le tattiche fanno due mestieri insieme: sono **scelte dal giocatore** (per i suoi personaggi) e **scritte nel codice** (per i mostri), con lo stesso linguaggio — i mostri e i miei personaggi fanno le stesse cose in una partita vera e in una simulazione. Il personaggio non sa di averne una: la tattica sta qui, **legata al suo `uuid`**.

## Dipendenze

**Dichiarate nel pom:** `06-foresta-combattimento`, `07-foresta-poteri`.

**Solo per i test:** `05-foresta-personaggi`.

**Lo dichiarano:** `09-foresta-missioni`, `10-foresta-modellodati`, `12-foresta-motore`, `12-foresta-swing`.

Le dipendenze sono transitive: un modulo vede anche quello che vedono i moduli da cui dipende, quindi il compilatore garantisce la separazione fra i moduli che non si raggiungono.

Dipende da `06-foresta-combattimento` (e per transitività dai poteri e dai personaggi) perché per decidere deve leggere lo scontro, stimare i danni attesi e sapere quali azioni si possono compiere. Non conosce il motore: gli eventi che pubblica (`InternoDecisioneTattica`, `InternoRichiestaAperturaFinestraCombattimento`) li ascolta chi esegue.

## Funzionamento

### Il linguaggio delle tattiche (i dati)

Tutto è **immutabile** e fatto solo di enumerati, numeri e liste (nel pacchetto `modellodati`), così la stessa classe serve alla UI, che li legge e li modifica, al salvataggio e al motore che li valuta.

- **`TatticaMD`**: la tattica di un personaggio, una lista ordinata di voci (nessun tetto), di cui si esegue la prima eseguibile. Può essere *disabilitata* (una tattica letta da un salvataggio con errori non si esegue ma si conserva perché il giocatore la corregga).
- **`VoceMD`**: un **selettore** (chi), delle **condizioni** in AND (quando) e una sequenza di **azioni** (che cosa: la prima applicabile al bersaglio vince). Si può disattivare senza toglierla; ha un nome dato dal giocatore.
- **`SelettoreMD`**: una **rosa** di candidati (`TipoRosa`), i **filtri** che li restringono (`FiltroMD`, in AND) e un **ordinamento** (`OrdinamentoMD`) che sceglie uno fra i rimasti.
- **`PredicatoMD`**: una condizione sulla situazione e non su un personaggio (`TipoPredicato`: quanti, round, astuzia, intelligenza, probabilità, voce usata, round dall'ultimo uso), con parametro e negazione.
- **`AzioneMD`**: il tipo (`TipoAzione`, che sta in `06-foresta-combattimento`) e i parametri di quelle che li hanno (il criterio di scelta dell'incantesimo, se rinunciare quando l'attacco con le armi rende di più, il tipo di danno dell'arma voluta).
- **`TatticheMD`** tiene le tattiche di tutta la partita, ognuna con l'`uuid` del personaggio; `FormatoTattiche` descrive il formato di salvataggio a righe (`TATTICA`, `VOCE`, `SELETTORE`, `FILTRO`, `PREDICATO`, `AZIONE`). La rilettura usa le stesse fabbriche che controllano gli elementi, quindi un salvataggio incoerente è rifiutato.

Le enum (nel pacchetto `tipi`): `TipoRosa` (se stesso, alleati, alleati caduti, avversari, ospiti e le combinazioni), `TipoFiltro` (salute, magia, livello, classe, categoria, stato, magico, debole o immune a…, il capo, l'ultimo scelto, chi mi ha attaccato per ultimo, l'arma che dà…), `TipoOrdinamento` (primo, a caso, meno/più salute, livello, più debole o resistente a, ultimo attaccante…), `TipoPredicato`, `TipoConfronto` (minore, uguale, maggiore), `TipoParametro`, `CriterioIncantesimo` (a caso, più potente, più adatto, più adatto esperto) e `PortataAzione` (su che cosa agisce un'azione, e quindi da quali rose il suo selettore può pescare).

### La logica (`tattiche`)

- **`ContestoTattico`** è tutto ciò che le tattiche possono *leggere* per decidere. Si crea per ogni decisione e si butta; non modifica niente. Sa di che lato è l'attore, quindi chi sono alleati e avversari: lo stesso vale per un mio personaggio e per un mostro.
- **`Valutatore`** scorre la tattica e decide: la **prima voce eseguibile vince**. Una voce è eseguibile se è attiva, le sue condizioni sono tutte vere e il suo selettore trova un bersaglio su cui almeno un'azione si può fare. Se nessuna lo è, ripiega sull'attacco fisico al primo avversario in campo. Non cambia niente nel gioco: restituisce una `Decisione` (l'azione, il bersaglio, il perché). Tira il dado solo se la tattica lo chiede (`PROBABILITA`, `A_CASO`).
- I meccanismi che il valutatore usa stanno in **`valutazione`**: `Selettori` (applica un selettore), `Filtri`, `Ordinamenti` e `Predicati` (la risposta di ogni tipo), `Requisiti` (quando un'azione si può fare: dopo 2 fallimenti per caso — `CostantiTattiche.MAX_TENTATIVI_FALLITI_PER_AZIONE` — un'azione non si prova più su quel bersaglio), `SceltaIncantesimo` (quale dei sette incantesimi elementali lancia `INCANTESIMO_DI_DANNO`) e `Astuzia` (l'intelligenza massima della classe per una rampa col livello del mondo).
- Le **rose** (`rose`) sono gli insiemi da cui si pescano i candidati: `Rosa` e un'implementazione per ogni `TipoRosa`, scelte da `Rose`.
- **`EsecutoreTattiche`** fa quel che il valutatore ha scelto, sui dati di sempre: un attacco con le armi, un incantesimo, un canto, un'abilità, una pozione, il cambio d'arma. Lo usano il combattimento di una locazione e le simulazioni. Non gestisce il round né la fine dello scontro.
- **`TatticheDeiPersonaggi`** tiene le tattiche della partita in corso (i dati li prende da chi li custodisce, `setFornitoreDiDati`).
- La **modifica** (`modifica`) è quel che serve a una schermata per far cambiare una tattica al giocatore, senza niente di grafico, e quindi uguale per ogni front-end: `BozzaTattica` (la tattica mentre la si modifica: i dati `*MD` sono immutabili, la bozza li tiene in liste modificabili e solo alla conferma costruisce la `TatticaMD` da mandare al motore; sa che cosa si può mettere in una voce: rose, azioni, filtri, ordinamenti e condizioni ammessi, valori predefiniti, passi e massimi dei numeri) e `TestoTattica` (le parole con cui la schermata descrive voci, selettori, condizioni e azioni, e le spiegazioni del riquadro destro). I testi non sono nel codice: stanno in `src/main/resources/…/testi/testo_tattica_it.properties`, letti con `Testi.get` (un file per lingua, in italiano se la lingua scelta non ha la voce), con `{0}`, `{1}`… al posto dei valori; `TestiDiTestoTatticaTest` controlla che ogni valore spiegato di ogni enumerato abbia la sua chiave. La schermata (`DisplayableCanvasTattiche`) e i menu a discesa (`CasellaADiscesa`) restano in `12-foresta-swing`.

### Le tattiche di partenza

- **`TatticheBase`** (500 righe) le scrive come dati: per i mostri una lista per classe divisa in **fasce di astuzia** — bruto (sotto 12: istinto, colpisce chi lo ha colpito o a caso), astuto (da 12: finisce i feriti, cerca un incantatore), esperto (da 20: sceglie l'incantesimo più adatto alle debolezze del bersaglio), stratega (da 31: Colpo di Morte sul quasi caduto, Silenzio sull'incantatore con più magia) — e per i personaggi giocabili un ruolo (il guerriero e il ladro curano se stessi e colpiscono i feriti; maghi ed elfi zittiscono e usano il dardo arcano; il sacerdote cura, risuscita, toglie gli stati; il bardo canta).
- **`RegistroTattiche`** dà la tattica di una classe: quella che qualcuno le ha registrato, altrimenti la «tattica di oggi», la trasposizione voce per voce di come i mostri sceglievano prima delle tattiche.

### I contratti per la UI

`ComandoImpostaTattica` (il giocatore imposta la tattica di un personaggio), `RichiestaAperturaTattiche` (apre la schermata delle tattiche), `InternoDecisioneTattica` (un personaggio ha deciso: porta azione, bersaglio, voce e percorso, per mettere a punto le tattiche con un `grep INTERNO_DECISIONE_TATTICA` sul log, per i test e per spiegare al giocatore perché ha agito così) e `InternoRichiestaAperturaFinestraCombattimento`.

## Osservazioni

- Il manuale d'uso e di progetto è in [`tattiche_di_combattimento_automatiche.md`](tattiche_di_combattimento_automatiche.md) (§11 per il linguaggio), il codice è descritto in [`tattiche_internals.md`](tattiche_internals.md).
- Le costanti sono pochissime (`CostantiTattiche`): la parte di bilanciamento sta nelle voci di `TatticheBase`.
- La schermata che il giocatore usa per modificare le tattiche sta in `12-foresta-swing` (`DisplayableCanvasTattiche`); questo modulo non sa che esiste.
