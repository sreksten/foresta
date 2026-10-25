# foresta-effettidistato

Gli **effetti di stato**: gli stati alterati che possono colpire un personaggio, dal rallentamento al berserk. Il modulo contiene l'enum `TipoEffettoDiStato` e il dato `EffettoDiStatoMD` (uno stato in corso: quale è, quanto dura ancora, i danni a ogni scatto del tempo, l'intensità).

Sta separato dal resto perché è il vocabolario sul quale poi si appoggiano i danni (che dicono quali stati un danno causa), i personaggi (che ne hanno) e le tattiche (che ne tengono conto). Il modulo dice *cosa sono*, non *che cosa fanno*: quello lo decide il combattimento.

## Dipendenze

**Dichiarate nel pom:** `foresta-testi`, `foresta-serializzazione`.

**Lo dichiarano:** `foresta-danni`, `foresta-personaggi`.

Le dipendenze sono transitive: un modulo vede anche quello che vedono i moduli da cui dipende, quindi il compilatore garantisce la separazione fra i moduli che non si raggiungono.

Dipende da `foresta-testi` per le descrizioni (`tipo_effetto_di_stato_<lingua>.properties`) e da `foresta-serializzazione` perché `EffettoDiStatoMD` si salva e si rilegge. Le reazioni fra stati e tipi di danno stanno nel modulo che lo dichiara dopo, `foresta-danni`.

## Funzionamento

### `TipoEffettoDiStato`

Venti stati, raggruppati nel codice per natura:

- **movimento e controllo**: `RALLENTATO`, `IMMOBILIZZATO`, `STORDITO`, `ATTERRATO`, `SPAVENTATO`;
- **danni nel tempo**: `SANGUINAMENTO`, `BRUCIATO`, `AVVELENATO`, `INFETTATO`;
- **mente e sensi**: `CONFUSO`, `MENTE_FRATTURATA`, `ACCECATO`, `ASSORDATO`, `SILENZIATO`, `MALEDETTO`;
- **ambientali**: `BAGNATO`, `CONGELATO`;
- **di classe e benefici**: `BERSERK` (esclusivo del Guerriero, scala il danno fisico col sangue perso e finisce uscendo dalla locazione), `ISPIRATO` (il canto di valore del Bardo, con intensità che dipende dal livello di chi canta) e `RIGENERAZIONE` (la rigenerazione del Druido, che guarisce una quantità fissa a turno).

`isBenefico()` dice quali sono un vantaggio (`ISPIRATO`, `BERSERK`, `RIGENERAZIONE`); gli altri sono un male da togliere, per esempio con l'Alba sacra. Le costanti non portano dati: la descrizione sta nel file dei testi (`getDescrizione`).

### `EffettoDiStatoMD`

Il dato di uno stato in corso su un personaggio: il **tipo** (immutabile), il **valore** (la durata rimasta), i **danni a ogni scatto del tempo** (0 per gli stati che non ne fanno) e l'**intensità** (la forza dell'effetto, per quelli che ne hanno una: ispirazione e rigenerazione). Durata e danni sono modificabili, perché il motore li fa scendere col passare del tempo. Sa scriversi e rileggersi nei salvataggi.

Come gli stati si applicano, durano e scadono in combattimento sta in `foresta-combattimento` e nel personaggio; il quadro completo è in [`interazioni_effetti_di_stato.md`](interazioni_effetti_di_stato.md).

## Osservazioni

- Il modulo è piccolo di proposito: tenerlo separato dai danni evita un ciclo (i danni citano gli stati che possono causare; gli stati non citano i danni, che si ritrovano nelle reazioni in `foresta-danni`).
- Il pacchetto `effettidistato.tipi` contiene solo un `package-info`: l'enum sta, come per ogni modulo, in `com.threeamigos.foresta.tipi`.
