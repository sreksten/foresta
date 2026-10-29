# 06-foresta-combattimento

**Come si scontrano** i personaggi: se un colpo va a segno, quanto danno fa, quali stati alterati provoca e che cosa succede a chi lo riceve; le stime con cui le tattiche scelgono; la memoria di uno scontro (di chi è il turno, chi ha fatto cosa); il cambio d'arma; le ondate di avversari; e le azioni di uno scontro (`TipoAzione`, `EsitoAzione`). I poteri (incantesimi, canti, abilità) si esercitano chiamando questo modulo.

È un modulo a sé perché le regole del colpo non sono né del personaggio (che sa *chi è*) né del motore (che orchestra il flusso della partita): sono calcoli puri che servono a tre parti — il gioco vero, le tattiche che prevedono l'esito, le abilità che colpiscono — e che si possono provare senza avviare un gioco. Per questo fuori dal personaggio stanno anche `ApplicazioneDelRisultato` e `AttaccoConLeArmi`: conoscono il calcolo e i gruppi.

## Dipendenze

**Dichiarate nel pom:** `05-foresta-personaggi`.

**Solo per i test:** `05-foresta-personaggi`.

**Lo dichiarano:** `07-foresta-poteri`, `08-foresta-tattiche`, `12-foresta-motore`.

Le dipendenze sono transitive: un modulo vede anche quello che vedono i moduli da cui dipende, quindi il compilatore garantisce la separazione fra i moduli che non si raggiungono.

Dipende da `05-foresta-personaggi`, e da lì vede gli artefatti, i danni e gli attributi per transitività. **Non conosce i poteri**: sono i poteri a dipendere da lui e a chiamarlo per colpire, per far durare uno stato e per registrare gli esiti. Per questo qui stanno anche `TipoAzione` ed `EsitoAzione`, che la memoria dello scontro (`StatoDelCombattimento`) usa e che le tattiche leggono.

## Funzionamento

### `CalcolatoreCombattimento`: le regole

Una classe di metodi statici (più di 1300 righe) che è il cuore del conto.

- **Probabilità di colpire** (`calcolaProbabilitaDiColpire`, `colpisce`): confronta un attacco e una difesa. Per i danni fisici contano la coordinazione di chi attacca (precisione e destrezza) contro la schivata di chi si difende (velocità, destrezza e parata); per i magici l'intelligenza e la precisione contro la resistenza magica e la saggezza. La stanchezza e gli stati alterati (confuso, accecato, rallentato, spaventato) cambiano i due lati, e il risultato ha un minimo e un massimo.
- **Danno** (`calcolaDannoRisultante`): tiene conto dell'arma e degli incantamenti, del livello, dei critici, delle debolezze e resistenze della classe, delle **interazioni** fra tipo di danno e stati (`interazioniAttive`, vedi `03-foresta-danni`) e dell'equipaggiamento difensivo del bersaglio (`difesaContro`). Il risultato è un `DannoRisultante`.
- **Più bersagli**: `quotaDelDannoPerPosizione` ripartisce il danno per posizione (principale intero, poi 50%, 25%, 12,5%, 6,25%, che è il minimo); `calcolaDannoAdArea` e `viciniInvestiti` gestiscono i colpi che investono anche i vicini nella fila.
- **Durate e danni periodici**: `calcolaDurataStato`, `calcolaDannoPeriodico`.
- **Stime per le tattiche**: `dannoAtteso`, `dannoAttesoDellArma`, `dannoAttesoDellaMischia`, `moltiplicatoreAtteso`, `moltiplicatoreAttesoDopo`, `statiCausabili`, `statiTolti`. Le tattiche le usano per scegliere guardando oltre il colpo di adesso.
- `fasiDiAttacco` / `FaseDiAttacco`: con quali armi e con che quota del danno si colpisce in un turno (la seconda arma fa una quota ridotta).

### Cosa succede a chi è colpito

`DannoRisultante` è il risultato di un colpo prima che il difensore lo subisca: il danno e il suo tipo, le interazioni scattate, le debolezze e resistenze che hanno contato, gli stati da aggiungere o togliere, i modificatori e gli effetti speciali (colpo di grazia, cura e danno ad area, sifone vitale, mietitura…). `ApplicazioneDelRisultato` lo applica: toglie la salute, aggiunge gli stati (per lo stesso stato si tiene quello che dura di più) e i modificatori, toglie gli stati da rimuovere, pubblica le notifiche delle interazioni e delle reazioni di classe, manda la cura ad area agli alleati e i colpi ad area ai vicini. `AttaccoConLeArmi` è l'attacco di un personaggio con le sue armi; `ColpoMagico` quello di un incantesimo o di un'abilità di danno; `GruppoDiBersagli` è un gruppo fatto sul momento con i bersagli scelti di un incantesimo a più bersagli.

### La memoria dello scontro: `StatoDelCombattimento`

Non fa parte del modello dati e **non si salva** (un combattimento non si interrompe mai per salvare); si azzera a ogni inizio locazione. Tiene:

- la **lista dei turni**: a inizio combattimento si fissa la lista di tutti i personaggi dei due gruppi, i miei e gli avversari si alternano distribuendo i posti in proporzione (con 4 contro 1 esce M M T M M); un **cursore** dice a chi tocca, saltando chi non può agire; il giro completo è un **round** (`InternoInizioRound`), e il numero riparte da 1 a ogni locazione e continua con le ondate;
- una **scheda** per personaggio (non dimenticata quando cade, perché un compagno potrebbe farlo risorgere): l'ultimo avversario e l'ultimo alleato scelti, chi lo ha attaccato per ultimo, se ha già lanciato un incantesimo, i cambi d'arma, le voci di tattica già usate;
- la **conoscenza sulle reazioni**, di pubblico dominio: di quanto l'equipaggiamento difensivo ha cambiato il danno per ogni coppia (personaggio colpito, tipo di danno).

Chi lo aggiorna è il motore; le tattiche lo leggono e basta.

### Le altre parti

- `CambioDArma`: quali armi escono dalle mani per far posto alla nuova (si sceglie guardando quanto danno fanno), se il personaggio può ancora cambiare in questo scontro e il cambio stesso sull'inventario del gruppo.
- `Ondata`: un gruppo di avversari che arriva quando quelli di prima sono tutti sconfitti, con ciò che si scrive quando arrivano.
- `AnnuncioDellAzione`: dice alla UI che un personaggio sta per agire (il riquadro evidenzia chi agisce e il bersaglio) e `InternoAzioneInCombattimento` è l'evento corrispondente.
- `NotificaColpoMancato` (in `05-foresta-personaggi`): `AttaccoConLeArmi` e `ColpoMagico` la pubblicano quando il tiro per colpire fallisce, come fanno `Silenzio` e `Morte` nei poteri; la UI ci fa comparire sul bersaglio la scritta «Mancato».

### Le azioni: `TipoAzione` ed `EsitoAzione`

`TipoAzione` elenca le azioni che una voce di tattica può compiere: l'attacco fisico, ciascun incantesimo, la luce divina e il dardo arcano, i canti, le abilità del chierico e del druido, le pozioni, il cambio d'arma… I testi (la descrizione) sono in `tipo_azione_it.properties`. `di(TipoIncantesimo)` e `getClasseIncantesimo()` fanno da ponte fra l'azione e l'incantesimo. L'azione di un'abilità o di un canto la dicono `TipoAbilita` e `TipoCanto` (`getTipoAzione()`, nei poteri). `EsitoAzione` è com'è andata (`RIUSCITA`, `MANCATA`, `IMMUNE`): `StatoDelCombattimento` la ricorda e le tattiche la usano per non ripetere un'azione che fallisce.

## Osservazioni

- Il quadro delle regole e delle interazioni è in [`motore_di_gioco.md`](motore_di_gioco.md) §7 e [`interazioni_effetti_di_stato.md`](interazioni_effetti_di_stato.md); le debolezze di classe in [`debolezze_e_resistenze.md`](debolezze_e_resistenze.md).
- `CalcolatoreCombattimento` è molto lungo e tutto statico: i test ne coprono i casi (11 file di test nel modulo), e ritarare il bilanciamento passa soprattutto dalle costanti di `05-foresta-personaggi`.
