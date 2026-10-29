# Modificatori temporanei

I `ModificatoreAttributoMD` possono durare un tempo e un'arma può metterli sul bersaglio che colpisce. Il lavoro è stato fatto il 2026-10-10 (fasi 1-8 sotto); il documento conserva le decisioni e, dove è rimasto qualcosa da fare, lo dice in "Da fare / idee aperte". Le sezioni 5-7 descrivono com'è il codice adesso.

## 1. Obiettivi

- Un modificatore può **durare un tempo**: poi se ne va da solo, come un effetto di stato.
- Il tempo si misura in **turni di combattimento** oppure in **locazioni**.
- Un modificatore ha un **bersaglio**: chi lo porta o chi viene colpito. Quelli sul bersaglio hanno senso solo sulle **armi**.
- `ALBA_SACRA` toglie anche i modificatori temporanei, così i malus a tempo si curano. Il canto di valore del Bardo non è un modificatore ma un effetto di stato (`ISPIRATO`, vedi `personaggi_giocabili.md`).
- Il `FIXME` della fuga (`PersonaggioBase.fugge`) diventa un malus a tempo di coraggio, valore e carisma.

## 2. Com'è oggi

- `ModificatoreAttributoMD` è immutabile: attributo, tipo, quantità, nota. Sta in `PersonaggioMD.modificatori` (benedizioni, lealtà, `addSalute`/`addMagia`) e in `ArtefattoMD.modificatori`; i salvataggi li scrivono in `PersonaggioMD` (`|` e `§`) e in `ArtefattoMD` (una riga per modificatore).
- `PersonaggioBase.getQuantitaModificata` unisce i modificatori locali con quelli di **tutti** gli artefatti dell'inventario personale: `QUANTITA_ASSOLUTA` (vince il minimo), poi la somma dei fissi, poi la somma delle percentuali.
- Gli effetti di stato scalano in `LocazioneBase.trascorriTurnoEffettiDiStato` (`riduciEffettiDiStato`, per gruppo e avversari). Le armi li mettono in `DannoRisultante.effettiDiStatoDaAggiungere` (`CalcolatoreCombattimento`, sezione 6) e `ApplicazioneDelRisultato.applica` li applica al bersaglio.
- I personaggi a tempo scalano in `Automa.entraInStatoFineLocazione2`, solo se vivi: lo stesso punto serve per le locazioni.
- `AlbaSacra.usa` chiama `rimuoviGliEffettiDiStatoNegativi` su ogni personaggio del gruppo di chi la invoca.

## 3. Decisioni

| Tema | Decisione |
| :--- | :--- |
| Nomi dei tipi | `AUMENTO_FISSO` → `VARIAZIONE_FISSA`, `AUMENTO_PERCENTUALE` → `VARIAZIONE_PERCENTUALE` (fatto il 2026-10-10). `QUANTITA_ASSOLUTA` resta. |
| Unità del tempo | Turni di combattimento o locazioni. Un'enum `UnitaDurata { PERMANENTE, TURNI, LOCAZIONI }` più una `durata` intera (0 solo per `PERMANENTE`). |
| Bersaglio | Un'enum `BersaglioModificatore { PORTATORE, BERSAGLIO }`; il default è `PORTATORE`. |
| Dove vale `BERSAGLIO` | Solo sulle armi. Vale per entrambe le mani: applica il modificatore l'arma che sferra il colpo. |
| Se il modificatore si applica | Stessa formula degli effetti di stato degli incantamenti elementali (vedi §5). |
| Quando scade | Resta finché non scade e passa da una locazione all'altra. I turni scalano nei combattimenti, le locazioni a fine locazione. Finisce anche con la morte di chi lo porta (come gli effetti di stato): non torna con la resurrezione. |
| Alba Sacra | Toglie tutti i modificatori temporanei dai personaggi del gruppo bersaglio, malus e bonus. |
| Sintassi nei file | `MOD=FORTUNA =0 @BERSAGLIO 3TURNI` (`TURNI` o `LOCAZIONI`, con uno spazio facoltativo dopo il numero: `3 TURNI`; tutto in maiuscolo come il resto); senza `@BERSAGLIO` e senza durata è come oggi. |
| Salvataggi | Niente retrocompatibilità. |

## 4. Modello

- `ModificatoreAttributoMD` guadagna `unitaDurata`, `durata` e `bersaglio`, tutti nell'`equals`/`hashCode`. Resta immutabile: chi lo fa scalare ne sostituisce la copia con `conDurata(int)`. I costruttori attuali restano (permanente, sul portatore).
- Sull'**artefatto** la durata è quella con cui il modificatore sarà applicato (un colpo di arma, per esempio "3 turni"); l'artefatto non scala mai. Sul **personaggio** è il tempo che resta, e scala.
- `PersonaggioMD` e `ArtefattoMD` scrivono e rileggono i tre campi a fine riga (il test di andata e ritorno controlla che la durata rimasta torni uguale).
- `RegoleSetLeggendari.applica` copia i nuovi campi e non moltiplica mai un modificatore sul bersaglio (non è un bonus di chi indossa il set).
- Il rinomino dei due tipi (fatto) ha toccato 29 file, fra cui `missioni.txt` (`BENEDIZIONE=`, `LEALTA=`), `ModificatoreDellaRiga`, `OggettoLeggendario`, `ListinoPergamene`, `Anello`, `RegistroArtefatti`, `Automa`, `GeneratoreArtefattiTabelle`, le due schermate che mostrano i modificatori e i test; i salvataggi di prova in `src/test/resources`. Fatto per primo, da solo, con un commit a parte.

## 5. Comportamento

### 5.1 Calcolo

- `getQuantitaModificata` ignora i modificatori `BERSAGLIO` degli artefatti (non contano per chi li porta). I modificatori temporanei sul personaggio passano da lì come gli altri, quindi "FORTUNA porta a 0" è una `QUANTITA_ASSOLUTA` come oggi.
- Più modificatori uguali (stesso attributo, tipo e nota) **non si sommano**: il nuovo rinfresca la durata se è più lunga, come `addEffettoDiStato`.
- Un metodo privato di `PersonaggioBase` ricalcola salute e magia massime e gli attributi secondari (oggi il ricalcolo sta dentro `addModificatore`) e lo usano anche la scadenza e la rimozione; la salute e la magia attuali non vanno sopra i nuovi massimi.

### 5.2 Scorrere del tempo

- **Turni**: nello stesso punto di `riduciEffettiDiStato` (`LocazioneBase.trascorriTurnoEffettiDiStato`, gruppo e avversari) si scalano di 1 i modificatori a turni. Fuori dal combattimento non scalano e restano per il prossimo scontro.
- **Locazioni**: in `Automa.entraInStatoFineLocazione2`, accanto ai personaggi a tempo e solo per i vivi, si scalano di 1 quelli a locazioni.
- A zero il modificatore sparisce e si ricalcolano gli attributi.
- **Notifica.** Una nuova `NotificaVariazioneModificatorePersonaggio`, sul modello di `NotificaVariazioneEffettoDiStatoPersonaggio`, con `TipoVariazione { AGGIUNTA, MODIFICA, RIMOZIONE }`, il modificatore interessato e, per la durata, il valore precedente e quello nuovo. Parte quando il modificatore:
  - viene **aggiunto** (`addModificatore`);
  - viene **modificato**: la durata è rinfrescata da un doppione più lungo, o scende di un turno o di una locazione;
  - viene **rimosso**: perché scade, per `ALBA_SACRA` o per altro.

  Prende il posto di `NotificaAggiuntaModificatorePersonaggio`, che oggi ascolta solo `SnifferBusEventi`: si toglie, con la voce di `TipoEvento`. La notifica ha il suo ramo in `SnifferBusEventi` e un messaggio per il riquadro di testo (`ForestaUI`, come `gestisciEventoVariazioneEffettoDiStato`). Per non riempire il testo di messaggi, la `MODIFICA` per puro scorrere del tempo non si scrive in chiaro, mentre l'aggiunta, il rinfresco e la rimozione sì.
- **Fuga.** La fuga finisce in `FINE_LOCAZIONE` e quindi passa dal decremento: il malus va applicato con una durata più alta di uno (o il decremento saltato per la locazione corrente), perché l'ignominia pesi per tre locazioni *dopo* quella della fuga. Da fissare in un test. Il malus (`VARIAZIONE_PERCENTUALE` negativa su `CORAGGIO`, `VALORE`, `CARISMA`, valori in `CostantiPersonaggi`) si mette a ogni personaggio vivo in `PersonaggioBase.fugge`. L'aggiustamento della salute in base al livello resta un'altra faccenda.

### 5.3 Le armi

- Dopo il blocco degli stati degli incantamenti (sezione 6 di `CalcolatoreCombattimento`), per ogni modificatore `BERSAGLIO` dell'arma che colpisce si fa lo stesso tiro degli incantamenti elementali (`probStatoMagico`: danno del colpo contro la forza del difensore più l'intelligenza pesata dal potere magico). Se riesce, `DannoRisultante.addModificatoreDaAggiungere` mette una copia sul portatore, con la sua durata.
- Il danno del colpo che serve al tiro è quello complessivo di quell'arma (i modificatori non hanno un elemento).
- `ApplicazioneDelRisultato.applica` applica i modificatori con `addModificatore`, tenendo il più lungo se ce ne sono due uguali.
- **Immunità.** Un bersaglio immune al tipo di danno dell'arma (`isImmuneATipoDanno(tipoDanno)`, la stessa `immuneATipoDannoBase` che azzera il danno base) è immune anche ai modificatori che quell'arma applica: per lui il tiro non si fa nemmeno. Vale come per gli effetti di stato dell'arma. L'immunità a un singolo effetto di stato (`isImmuneAEffetto`) non c'entra: i modificatori non sono effetti di stato.
- Le armi secondarie fanno lo stesso: nessun caso a parte.

### 5.4 Alba Sacra

`AlbaSacra.usa` chiama anche `rimuoviIModificatoriTemporaneiRimovibili()` su ogni personaggio del gruppo (`PersonaggioBase`, rimuove i modificatori con unità diversa da `PERMANENTE` e `isRimovibile()`; l'ignominia della fuga è l'unica non rimovibile, ricalcola e notifica). La benedizione, la lealtà e i bonus permanenti restano.

### 5.5 Regole degli oggetti

- **Dove stanno**: un modificatore `BERSAGLIO` si può mettere solo su un'arma, in `ArtefattoMD.addModificatore` e in `GeneratoreArtefattiTabelle`; l'incantatore (`RegoleIncantatura.trasferibili`) non lo trasferisce su altro, con l'avviso degli effetti persi come per il `POTERE_MAGICO`. Un modificatore a tempo sul **portatore** non si limita alle armi, ma nel generatore non serve (vedi §8).
- **Limite degli effetti**: conta come qualsiasi modificatore.
- **Prezzo** (`ListinoPergamene.prezzo(modificatore)`): oggi si paga il valore dell'attributo, come se il modificatore fosse permanente. Per uno a tempo il prezzo base (lo stesso calcolo, sul bersaglio come sul portatore) si moltiplica per `0,5 × durata`, con la durata in turni, fino a un tetto. Il coefficiente 0,5 per turno è deciso (2026-10-10), in `CostantiArtefatti` (per esempio `PERGAMENA_COEFFICIENTE_PREZZO_PER_TURNO`); il tetto va ancora fissato, e va tarato con le altre misure di `economia.md`. Un modificatore permanente non cambia prezzo. Il prezzo permanente di una `QUANTITA_ASSOLUTA` è `5 × q`, e "porta a 0" costa 50 fisso (`PERGAMENA_PREZZO_PORTA_A_ZERO`); poi si applica il moltiplicatore del tempo. Per la durata in locazioni resta da decidere a quanti turni equivale una locazione.

## 6. Sintassi

Nei file di testo (`leggendari.txt`, grammatica degli artefatti, `missioni.txt`):

```
MOD=FORTUNA =0 @BERSAGLIO 3TURNI   porta a 0 la fortuna del bersaglio per 3 turni
MOD=CORAGGIO -20% 3 LOCAZIONI     -20% di coraggio di chi lo porta per 3 locazioni
MOD=FORZA +100%                    come oggi, permanente sul portatore
```

Il numero e l'unità si possono scrivere attaccati o separati da uno spazio (`3TURNI`, `3 TURNI`); il singolare (`1TURNO`, `1 LOCAZIONE`) si accetta come il plurale. L'`=` è `QUANTITA_ASSOLUTA`, `%` la variazione percentuale, il resto la variazione fissa. Il parser di `OggettoLeggendario` e `ModificatoreDellaRiga` si estende con i due campi facoltativi. In grammatica (`<mod:FORZA+1>`) non cambia nulla all'inizio: i modificatori a tempo si scrivono a mano nei leggendari.

## 7. UI

- **Rivelazione artefatto e schermate di scambio** (`SpriteRivelazioneArtefatto`, `DisplayableCanvasScambiatoreArtefatti`): "Fortuna: porta a 0 sul bersaglio per 3 turni".
- **Scheda del personaggio** (il pannello attributi di `DisplayableCanvasInventario`):
  - L'attributo, primario o secondario, è **verde** se i modificatori temporanei lo alzano, **rosso** se lo abbassano; il valore mostrato è quello effettivo. Il colore guarda solo i temporanei, non l'equipaggiamento né le benedizioni, altrimenti con un po' di artefatti mezza scheda sarebbe già colorata. Se su un attributo ci sono bonus e malus conta il saldo. L'attributo sotto il mouse resta bianco: l'evidenziazione vince.
  - **Giallo** se un modificatore **porta l'attributo a un valore fisso** (`QUANTITA_ASSOLUTA`), permanente o temporaneo, da qualunque fonte: un'arma, un artefatto, una benedizione, un colpo subito. È l'unico colore che non guarda solo i temporanei: serve a far capire perché l'attributo ha quel valore e non sale più con i punti abilità o con gli altri bonus. Il giallo vince su verde e rosso (con più modificatori vince il minimo, come nel calcolo). Nel figlio dell'attributo (la descrizione, che si apre con un click) si aggiunge la riga "Fissato a 0 da: la Spada della Sfortuna di RomyJona (ancora 5 turni)", con la nota del modificatore, o il nome dell'artefatto, e la durata se è a tempo.
  - In cima all'albero scorrevole un nodo **"Modificatori temporanei"**, presente solo se ce n'è almeno uno, con un figlio per modificatore ("Ignominia della fuga: Coraggio -20%, 2 locazioni"). Si apre e si chiude con un click, come i "Modificatori:" delle rivelazioni, e lo stato si salva allo stesso modo. Sta nell'albero e non sotto l'immagine perché lì c'è già la colonna dei set completi.
- **Riquadro del gruppo** (`DisplayableCanvasRiquadroGruppo`): solo il colore, senza cartigli; chi è curioso apre la scheda del personaggio. Colora i valori che il riquadro mostra: salute massima (`SALUTE`), magia massima (`MAGIA`), coraggio, valore e carisma, con la stessa regola della scheda (`colorePerAttributo`).
- **`VistaPersonaggio`** guadagna `getModificatoriTemporanei()` (attributo, descrizione, durata, unità), `getVariazioneDaTemporanei(attributo)` e `getFissaggio(attributo)` (chi fissa l'attributo e a che valore, se c'è), tutti calcolati dal motore (`PersonaggioBase`): `getVariazioneDaTemporanei` è la differenza fra l'attributo con e senza i modificatori temporanei, sul solo attributo (vedi Osservazioni). La UI non rifà i conti. `getFissaggio` restituisce un `ModificatoreAttivo` (`interfacce`): il modificatore e la fonte, cioè il nome dell'artefatto o la nota. `DipendenzeUITest` è verde.
- **Sprite sul personaggio**, come per gli effetti di stato (`ForestaUI.gestisciEventoVariazioneModificatore`, `DisplayableCanvasRiquadroLocazione.aggiungiModificatore`): una scritta con la descrizione breve (`ModificatoreAttributoMD.descrizioneBreve`: "Furtività +5", "Coraggio -3", "Coraggio -20%", "Intelligenza a 0"), gialla se porta l'attributo a un valore, verde se lo alza, rossa se lo abbassa, grigia quando l'effetto è finito. Compare all'aggiunta e quando un doppione allunga la durata; non quando la durata scende di un turno. Solo per i modificatori a tempo.
- Le notifiche di aggiunta, rinfresco e rimozione arrivano anche nel testo di gioco (§5.2).

## 8. Fasi

1. [x] **Rinomino** dei due tipi in tutto il repo (codice, risorse, test, `artefatti_e_incantamenti.md`), 2026-10-10. Suite verde.
2. [x] **Modello e salvataggi**: `UnitaDurataModificatore`, `BersaglioModificatore`, i tre campi di `ModificatoreAttributoMD` (con `conDurata`, `suPortatore`, `conQuantita`, `stessoEffetto`, le descrizioni per i testi), scrittura e lettura in `ArtefattoMD` e `PersonaggioMD`.
3. [x] **Calcolo e scadenza**: filtro del bersaglio, nessuna somma di doppioni, ricalcolo, scalare per turni (`LocazioneBase.trascorriTurnoEffettiDiStato`) e per locazioni (`Automa.entraInStatoFineLocazione2`), `NotificaVariazioneModificatorePersonaggio` al posto di `NotificaAggiuntaModificatorePersonaggio`.
4. [x] **Alba Sacra** e **fuga** (`CostantiPersonaggi.FUGA_IGNOMINIA_*`: −20% di coraggio, valore e carisma per 3 locazioni dopo quella della fuga, quindi durata 4). Oltre all'ignominia, fuggire costa a ogni personaggio dal 30% al 50% della sua salute massima (`CostantiPersonaggi.FUGA_DANNO_*_PERCENTUALE`, `PersonaggioBase.fugge`; a fine locazione ognuno recupera dal 16 al 35%, secondo la classe), e non uccide mai: lascia almeno un punto di salute. Il gruppo perde inoltre il 20% delle scorte (monete, preziosi, pergamene e pozioni, arrotondato per difetto: `CostantiPersonaggi.FUGA_PERDITA_CONSUMABILI_PERCENTUALE`, `GruppoGiocatore.fugge`); gli artefatti restano.
5. [x] **Armi**: `Arma.getModificatoriSulBersaglio`, il tiro in `CalcolatoreCombattimento` ("Traguardo 3"), `DannoRisultante.modificatoriDaAggiungere`, applicazione in `ApplicazioneDelRisultato.applica`, immunità.
6. [x] **Sintassi, prezzo, incantatore**: `ModificatoreDelTesto`, `ListinoPergamene.prezzo`, `RegoleIncantatura`; la Spada della Sfortuna di RomyJona in `leggendari.txt` (§10).
7. [x] **UI**: rivelazione e scambio, scheda del personaggio (colori, giallo, nodo dei temporanei), riquadro del gruppo.
8. [x] **Documenti**: questo, `artefatti_e_incantamenti.md`, `motore_di_gioco.md`, `interazioni_effetti_di_stato.md`, `foresta.md`, `CLAUDE.md`.

## 9. Test

Tutti in `ModificatoriTemporaneiTest` (26 test), più i test esistenti che hanno seguito il rinomino:

- Rinomino: tutti i test esistenti verdi senza altre modifiche.
- Salvataggio di personaggio e artefatto con durata, unità e bersaglio.
- Scadenza a turni (non scala fuori combattimento, scala per gruppo e avversari) e a locazioni (scala solo ai vivi, passa fra locazioni).
- `QUANTITA_ASSOLUTA` a tempo che torna al valore normale alla scadenza.
- Doppione: la durata si rinfresca, non si somma.
- Arma con `BERSAGLIO`: il portatore non ne risente, il bersaglio sì (tiro forzato), anche con l'arma della mano secondaria.
- Accessorio o armatura con `BERSAGLIO`: rifiutato. Incantatore: non lo trasferisce, avvisa.
- Set leggendario: nessun moltiplicatore sul bersaglio.
- Alba Sacra: toglie i temporanei, non i permanenti.
- Fuga: malus per le tre locazioni dopo la fuga.

## 10. Artefatto di prova

La **Spada della Sfortuna di RomyJona** (bozza per `leggendari.txt`, nella sezione `SPADA_LEGGENDARIA`; la chiave segue `SPADA_DI_ROMY_JONA`):

```
TIPO=SPADA;NOME=la Spada della Sfortuna di RomyJona;CHIAVE=SPADA_DELLA_SFORTUNA_DI_ROMY_JONA;\
    BREVE=la Spada della Sfortuna;DESCRIZIONE=che porta sfortuna a chi colpisce;LIVELLO=5;DANNI=14;COSTO=60;PESO=2;\
    MOD=FORTUNA =0 @BERSAGLIO 5TURNI;\
    LEGGENDA=...;LEGGENDA=...
```

Serve a provare tutta la catena: il tiro del colpo, la copia sul bersaglio, il calcolo (con la fortuna del bersaglio a 0 i colpi critici avversari diventano più probabili: `probabilitaCritico = 5 + CRITICO attaccante − FORTUNA difensore`), la scheda del nemico e la scadenza.

Siccome la spada continua a colpire, la fortuna del bersaglio resta a zero finché dura il combattimento: un nuovo colpo che riesce rinfresca la durata a 5 turni invece di sommarla (§5.1). È voluto. Test: fortuna a 0 dopo il colpo, durata rinfrescata al secondo, fortuna normale al sesto turno senza colpi, nessun effetto sul portatore.

In modalità di prova (`GruppoGiocatore`, insieme alla Falce del Tristo Mietitore) l'inventario del gruppo ha anche due spade di livello 1 per provare i modificatori a mano: la **Spada dell'Inciampo** (`FORTUNA =0 @BERSAGLIO 2TURNI`) e la **Spada della Debolezza** (`FORZA -2 @BERSAGLIO 3TURNI`).

## Osservazioni

- `ALBA_SACRA` toglie anche i bonus temporanei dei compagni, come gli effetti di stato buoni (`ISPIRATO` del Bardo): è voluto, fa pulito.
- La durata in locazioni conta solo i personaggi vivi, come quelli a tempo: un compagno morto tiene il malus finché non risorge.
- Il colore e la variazione guardano solo i modificatori scritti sull'attributo: un `FORTUNA =0` colora la fortuna, non il critico o la precisione che ne dipendono (il motore li ricalcola, ma la vista non sa quanto sarebbero senza i temporanei). Calcolarlo davvero vorrebbe dire ricalcolare una copia del personaggio senza temporanei a ogni fotogramma.
- Un modificatore sul bersaglio sta su un'arma o su un ingrediente (la pergamena), che lo porta per passarlo a un'arma; `ArtefattoMD.addModificatore` rifiuta gli altri.

## Da fare / idee aperte

- Tarare i valori provvisori: tetto del moltiplicatore di prezzo dei modificatori a tempo (`PERGAMENA_MOLTIPLICATORE_PREZZO_TEMPO_MASSIMO`, 2), equivalenza locazione/turni (`PERGAMENA_TURNI_PER_LOCAZIONE`, 5) e malus della fuga (`FUGA_IGNOMINIA_*`).
- Missioni (`BENEDIZIONE=`, `LEALTA=`) con una durata facoltativa: ora hanno solo modificatori permanenti.
- Grammatica degli artefatti con modificatori a tempo sul bersaglio.
