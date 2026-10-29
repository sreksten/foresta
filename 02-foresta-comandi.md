# 02-foresta-comandi

Il **vocabolario di tutto ciò che il giocatore può comandare**: l'enum `Comando` (le scelte dei menu, le classi e i sessi del personaggio, le direzioni, le mosse di carta, forbici e sasso, i personaggi del gruppo da selezionare, gli slot dei salvataggi…) con le descrizioni da mostrare, e `RichiestaConComandi`, la richiesta del gioco che offre al giocatore una scelta fra più comandi.

È un modulo a sé perché i comandi sono il punto in cui il giocatore, la UI e *ogni* sottoprogetto si incontrano: un sottoprogetto che vuole fare una domanda al giocatore (le missioni, l'economia, le tattiche) deve poter definire la sua richiesta con le sue risposte possibili senza dipendere dal motore.

## Dipendenze

**Dichiarate nel pom:** `01-foresta-eventi`, `01-foresta-testi`.

**Lo dichiarano:** `07-foresta-poteri`, `06-foresta-bestiario`, `10-foresta-trofei`.

Le dipendenze sono transitive: un modulo vede anche quello che vedono i moduli da cui dipende, quindi il compilatore garantisce la separazione fra i moduli che non si raggiungono.

Dipende da `01-foresta-eventi` perché `RichiestaConComandi` è un evento (estende `EventoBase`) e da `01-foresta-testi` perché le descrizioni dei comandi sono in un file di testi per lingua.

## Funzionamento

### `Comando`

Un'enum con tutto ciò che il giocatore può chiedere al motore: di solito un bottone della UI. La UI non lo manda direttamente al motore: pubblica un evento `ComandoDiGioco` a cui il motore è iscritto. Le costanti sono raggruppate:

- **all'inizio del gioco**: il sesso (`MASCHIO`, `FEMMINA`) e la classe, in versione maschile e femminile (`GUERRIERO`/`GUERRIERA`, `LADRO`/`LADRA`, `BARDO`/`CANTASTORIE`, `ELFO`/`ELFA`, `MAGO`/`MAGA`, `SACERDOTE`/`SACERDOTESSA`, `CHIERICO`/`CHIERICA`, `DRUIDO`/`DRUIDA`);
- **dentro una locazione**: `COMBATTIMENTO`, `SINGOLO_ATTACCO`, `INCANTESIMO`, `CORRUZIONE`, `AMICIZIA`, `FUGA`, `PASSA_INOSSERVATO`…;
- **la scelta di un componente del gruppo**: `PERSONAGGIO_1`…`PERSONAGGIO_8` (`getIndicePersonaggio`, `isPersonaggio`, `ofPersonaggio(n)`);
- **le direzioni, le mosse di carta, forbici e sasso, gli slot di salvataggio** (`NUMERO_1`…`NUMERO_5`), i comandi di incantesimi, canti e abilità, e quelli generati dal sistema come `TIMER`.

`Comando.of(ordinal)` ritrova un comando dall'indice. Il nome di ogni costante è anche la chiave del suo testo.

### Le descrizioni

`getDescrizione()` legge il testo da `comandi_it.properties` con `Testi` (`Testi.get("comandi", NOME)`): è quello che appare nei cartigli della barra delle icone. Un comando senza voce non ha descrizione (gli slot, le frecce di scorrimento, il timer, i comandi segreti). Per una lingua nuova si copia il file in `comandi_<lingua>.properties` e si traduce; quel che manca si legge in italiano.

### `RichiestaConComandi`

La classe base degli eventi del tipo «scegli una di queste cose»: porta la collezione dei comandi possibili (`getPossibilita`). La UI la riceve e mostra i bottoni corrispondenti nella barra icone; la risposta torna come `ComandoDiGioco`. Le richieste concrete (per esempio scegliere la direzione, il personaggio, un incantesimo) la estendono nel modulo che le conosce.

## Osservazioni

- `Comando` mescola, come dice il commento in testa, classi e razze («ce ne faremo una ragione»): è la ragione per cui ci sono le versioni maschile e femminile separate.
- Il comando associato a ogni incantesimo, canto, abilità e mossa non sta qui ma in `ComandiAssociati` (in `11-foresta-motore-api`), perché usa tipi di moduli più in alto.
