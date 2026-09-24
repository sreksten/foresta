# Piano di implementazione: Monte Carlo Matrix Test

Piano derivato da `simulazione_combattimenti.md` e dall'analisi di `PersonaggioBase`,
`CalcolatoreCombattimento`, `Arma`/`ArmaNaturale` e `ClassePersonaggio`.

## 1. Obiettivo

Per ogni coppia [Classe Giocabile] vs [Classe Mostro], e per ogni quantità di mostri da 1
al massimo consentito per quella classe in una locazione, eseguire N iterazioni di
combattimento simulato e misurare:

- **Win Rate PG** (% di volte che il PG elimina tutti i mostri restando vivo)
- **Lose Rate PG** (% di volte che il PG muore prima)
- **TTK medio** (numero medio di turni per risolvere lo scontro)
- **Stanchezza media finale** del PG a fine scontro

## 2. Scoperta importante: `attacca()` non è riusabile così com'è

`PersonaggioBase.attacca(Gruppo)` e `attacca(Personaggio)` (linee 313-385) sono legati a
`GruppoAvversario.getIstanza()` / `GruppoGiocatore.getIstanza()` (singleton globali dello
stato di gioco reale) e a `BusEventi` per notifiche testuali. Non possiamo chiamarli
migliaia di volte in un test isolato senza sporcare/dipendere da stato globale del gioco.

**Conseguenza per il piano**: il simulatore NON deve chiamare `attacca()`. Deve invece
orchestrare direttamente le primitive di basso livello già disaccoppiate:
- `CalcolatoreCombattimento.colpisce(attaccante, difensore, SupertipoDanno)`
- `CalcolatoreCombattimento.calcolaDannoFinale(attaccante, difensore, Arma)`
- `difensore.subSalute(danno, attaccante, NotificaFerite.NO, NotificaMorte.NO)`

Usando `NotificaFerite.NO` / `NotificaMorte.NO` evitiamo la costruzione di stringhe e la
pubblicazione di `NotificaTestoFrase` inutili (nessun listener registrato in un test, ma sono
comunque cicli di CPU sprecati su milioni di iterazioni).

## 3. Scoperta importante: `Logger.log` scrive su `System.out` senza interruttore

`CalcolatoreCombattimento` chiama `Logger.log(...)` ad ogni singola valutazione (decine di
righe per attacco). Con migliaia di iterazioni x più turni x più attacchi, la console
esploderebbe (potenzialmente centinaia di milioni di righe).

**Soluzione proposta**: nel test, avvolgere l'intera simulazione reindirizzando
temporaneamente `System.out` verso uno stream nullo (`new PrintStream(OutputStream.nullOutputStream())`),
salvando e ripristinando lo stream originale in un blocco `try/finally`. Non serve toccare
`Logger` in `../src/main`.

## 4. Semplificazione voluta: solo danno fisico via `ArmaNaturale`

Replicare la logica magica (`scegliIncantesimoContro`, privata, con probabilità legate a
intelligenza/inventario/`BusEventi`) è complesso e fuori scopo per una prima versione.

**Fase 1 del simulatore**: sia il PG che i mostri attaccano sempre fisicamente usando
`new ArmaNaturale(personaggio)` come arma, tramite `CalcolatoreCombattimento`. Questo è
consistente con la direzione del codice attuale (il commento "NUOVO MOTORE" dentro
`attacca()` mostra che il gioco sta migrando verso `CalcolatoreCombattimento` per il danno
fisico).

Una eventuale **Fase 2** (fuori da questo piano, da valutare dopo aver visto i primi
risultati) potrebbe aggiungere una probabilità di lancio incantesimo per i PG magici,
riusando `CalcolatoreCombattimento.calcolaDannoFinale` con un "arma" di tipo magico fittizia.

## 5. Determinazione del numero di bersagli (AoE)

`Personaggio.getBersagli()` (implementato in `PersonaggioBase`, riga 187) restituisce
quanti nemici un personaggio può colpire in un turno, derivato da `NUMERO_BERSAGLI`. Il
simulatore userà `Math.min(attaccante.getBersagli(), nemiciVivi.size())` per determinare
su quanti nemici applicare l'attacco nel sub-turno del PG (i mostri, per semplicità,
attaccano sempre 1 bersaglio: il PG).

## 6. Architettura del codice

Tutto in `../src/test/java/com/threeamigos/foresta/motore/modellodati` (è uno strumento di
analisi, non codice di produzione):

### 6.1 `RisultatoMatrice` (classe di supporto, può essere una nested class statica)
```java
public class RisultatoMatrice {
    public final double winRatePg;
    public final double loseRatePg;
    public final double stalloRate; // vedi sezione 9.1: deve tendere a 0
    public final double mediaTurni;
    public final double mediaStanchezzaFinale;
}
```

### 6.2 `CombatSimulatorMatrix` (motore di simulazione)
Metodo principale:
```java
static RisultatoMatrice simulaScontroGruppo(ClassePersonaggio classePg,
                                             ClassePersonaggio classeMostro,
                                             int quantitaMostri,
                                             int livello,
                                             int iterazioni)
```
Logica per singola iterazione:
1. `Personaggio pg = classePg.getIstanza(livello);` con arma `new ArmaNaturale(pg)`
   (fallback `ARTIGLI_LEGGERI`, vedi sezione 9.3 — nessuna arma dedicata da scrivere).
2. Crea `quantitaMostri` istanze fresche di `classeMostro.getIstanza(livello)` in una
   `List<Personaggio>` mutabile, ciascuna con la propria `new ArmaNaturale(mostro)`.
3. Ciclo turni (con **limite di sicurezza di 100 turni**, vedi sezione 9.4):
   - Turno PG: per `min(pg.getBersagli(), mostri.size())` mostri, verifica `colpisce()` e
     applica `calcolaDannoFinale()` + `subSalute()`.
   - Rimuovi dalla lista i mostri con `!isVivo()`.
   - Se lista mostri vuota → vittoria PG, esci dal ciclo.
   - Turno mostri superstiti: ciascuno attacca il PG (stesso schema colpisce/danno/subSalute).
   - Se `!pg.isVivo()` → sconfitta PG, esci dal ciclo.
4. Accumula: vittoria/sconfitta/stallo, turni impiegati, `pg.getStanchezza()` finale.
5. Se si raggiungono i 100 turni senza un vincitore, è uno **stallo**: va contato nella
   sua categoria separata (`stalloRate`), mai come vittoria né come sconfitta. Uno stallo
   non deve mai (o quasi mai) verificarsi: se il report mostra `stalloRate > 0` per una
   combinazione, è un campanello d'allarme di bilanciamento da correggere (danni troppo
   bassi rispetto alle vite, o difese/parate troppo alte), non un risultato accettabile
   da tenere così com'è.

### 6.3 Test/tool che orchestra la matrice
Una classe (es. `TestMonteCarloMatrix`) con:
- Elenco classi giocabili: `BARDO, CANTASTORIE, ELFA, ELFO, GUERRIERA, GUERRIERO, LADRA, LADRO, MAGA, MAGO, OMBRAFIAMMA`
  (sono le classi elencate nel secondo blocco di `ClassePersonaggio`, quelle con
  costruttore `(String nome, ClassePersonaggio, int livello)` disponibile in `PersonaggioBase`).
- Elenco classi mostro: tutte le altre 24 voci di `ClassePersonaggio` (primo blocco
  dell'enum: `ARPIA, CENTAURO, CHIMERA, CHIMERA_DRAGO, DRAGO, EREMITA, FANTASMA, FOLLETTO,
  GARGOYLE, GIGANTE, GOBLIN, HOBGOBLIN, IDRA, LICH, MINOTAURO, MINOTAURO_GIGANTE,
  OMBRA_NERA, SCHELETRO, SPETTRO, SPIRITO, STREGA, TITANO, TROLL, VIVERNA`).
- Per ogni mostro, la quantità massima per locazione si ottiene con
  `classeMostro.getIstanza(1)` seguito da `classeMostro.getQuantitaMassima()` (il valore
  viene impostato dentro il costruttore di `PersonaggioBase` tramite `classe.setQuantitaMassima(...)`,
  quindi va istanziato almeno un mostro prima di leggerlo — oppure, più semplice, si legge
  direttamente la costante `Costanti.<NOME>_MAX_NUMERO` già usata da ciascuna classe mostro).
- Doppio ciclo: per ogni PG, per ogni mostro, per `quantita` da 1 a `maxNumero`, chiama
  `simulaScontroGruppo(...)` e scrive una riga di risultato.

## 7. Output

Come per `PERSONAGGI_VALORI_MEDI.csv`, generare un file CSV nella radice del progetto,
es. `REPORT_BILANCIAMENTO_MATRICE.csv`, con colonne:

```
PG,MOSTRO,QUANTITA_MOSTRI,WIN_RATE,LOSE_RATE,STALLO_RATE,TURNI_MEDI,STANCHEZZA_MEDIA_FINALE
GUERRIERO,GOBLIN,1,98.40,1.60,0.00,2.10,2.50
GUERRIERO,GOBLIN,2,85.10,14.90,0.00,4.30,5.10
...
```
così è consultabile in Excel esattamente come richiesto per l'altra matrice di test.

## 8. Costo computazionale e strategia di esecuzione

11 PG x 24 mostri x ~3 quantità medie x 10.000 iterazioni ≈ diversi milioni di
combattimenti simulati in un singolo metodo di test. Da tenere presente:

- Il metodo NON deve far parte della suite `mvn test` standard (rischia timeout enormi).
  Da marcare con `@Disabled` (JUnit 5) e/o un nome che non matcha i pattern di test
  automatici, così va eseguito manualmente da IntelliJ quando serve un report.
- Parametrizzare `ITERAZIONI` come costante facilmente modificabile (es. 1.000 per una
  prima passata di verifica veloce, 10.000 per il report definitivo).
- Livello fisso a 1 per la prima versione (coerente con la richiesta originale
  "Ladro livello 1 vs Goblin livello 1"); il parametro `livello` nel metodo del
  simulatore resta comunque generico per usi futuri.

## 9. Decisioni prese

### 9.1 Stallo/timeout
Uno stallo (100 turni raggiunti senza un vincitore) va tracciato come categoria separata
(`stalloRate`) accanto a vittoria/sconfitta, mai fuso con l'una o l'altra. Uno stallo NON
deve poter accadere nel gioco reale: se compare nel report, è un difetto di bilanciamento
da correggere (es. danni troppo bassi, parata/resistenza eccessiva), non un esito da
accettare passivamente.

### 9.2 Tipo di danno dei mostri
Confermato: solo danno fisico per questa prima versione del simulatore. La scelta
danno fisico/magico dei mostri (in base a `getMoltiplicatoreDanniMagici()` ecc.) e la
selezione di incantesimi per i PG magici sono rimandate a un test/fase separata, come
già previsto nella sezione 4 ("Fase 2").

### 9.3 Arma del PG — analisi e proposta
Ho verificato se esiste già nel codebase un'arma "reale" con danno definito da usare come
riferimento (`ArmaFisica implements Arma`, in `src/main/java/.../oggetti/`): esiste solo
come classe/struttura (estende `Artefatto`, delega `getDanni()`/`getLivello()` a
`ArtefattoMD`), ma **non ho trovato nessuna istanza concreta con valori di danno definiti
nel codice** — il sistema di inventario/armi non è ancora popolato con dati di gioco reali
utilizzabili come riferimento.

Non serve quindi inventare un numero arbitrario: **`ArmaNaturale` ha già un ramo
`default`** (riga ~78 di `ArmaNaturale.java`) che assegna `TipoAttaccoNaturale.ARTIGLI_LEGGERI`
a qualsiasi classe non esplicitamente mappata — e tutte le 11 classi giocabili cadono in
quel `default`, quindi **oggi `new ArmaNaturale(pg)` già restituisce un'arma "fittizia"
sensata per un PG**, senza bisogno di scrivere altro codice:

- Danno base: `4 + floor(sqrt(Forza) * 1.5)`, minimo 1.
- Tipo danno: `TAGLIENTE`.
- Scala su Forza (non Intelligenza).

Il dato interessante è che questo è **esattamente lo stesso tier usato dai mostri più
deboli del gioco** (Goblin, Folletto, Arpia, Scheletro, Hobgoblin usano tutti
`ARTIGLI_LEGGERI`). Questo rende il confronto "PG livello 1 con equipaggiamento base vs
Goblin livello 1" internamente coerente con l'obiettivo di bilanciamento descritto nel
doc originale (~95%+ win rate contro un singolo mostro base).

**Decisione**: il simulatore userà `new ArmaNaturale(pg)` anche per i personaggi
giocabili, riusando il fallback già presente — nessuna nuova classe arma da scrivere per
la Fase 1.

**Caveat da segnalare nei risultati**: per le classi orientate alla magia (MAGA, MAGO, e
in parte CANTASTORIE/OMBRAFIAMMA) questo fallback scala su Forza, non su Intelligenza —
quindi in Fase 1 questi PG combatteranno "a pugni" in modo non rappresentativo del loro
stile di gioco reale (che userebbe incantesimi). I loro numeri di win rate in Fase 1
vanno letti come limite inferiore, non come verdetto finale sulla classe: la valutazione
completa arriverà solo con la Fase 2 (magia).

### 9.4 Limite turni di sicurezza
Il limite scende da 200 a **100 turni**. Inoltre si aggiunge un obiettivo di design
esplicito: uno scontro ben bilanciato dovrebbe risolversi entro **circa 30 turni** al
massimo per non risultare noioso. Il report dovrebbe quindi evidenziare, oltre allo
`stalloRate` (sezione 9.1), anche i casi in cui `mediaTurni` si avvicina o supera la
soglia dei 30 turni: anche senza arrivare allo stallo tecnico, un combattimento che dura
sistematicamente 40-50+ turni è già un segnale di bilanciamento da rivedere (danni troppo
bassi rispetto alla salute in gioco), da annotare come nota qualitativa nel report finale
oltre ai numeri grezzi.

## 10. Ordine di implementazione consigliato

1. Scrivere `RisultatoMatrice` e `CombatSimulatorMatrix` con il solo metodo
   `simulaScontroGruppo` (1 PG vs 1 tipo di mostro, quantità parametrica).
2. Scrivere un singolo test manuale mirato (es. Ladro liv.1 vs 1 Goblin, 10.000 iterazioni)
   per validare che i numeri siano sensati (win rate ragionevole, nessun loop infinito,
   nessuna eccezione) prima di lanciare la matrice completa.
3. Estendere con il ciclo su quantità mostri (1..max) per la stessa coppia PG/mostro e
   verificare l'andamento del win rate al crescere del numero di nemici.
4. Estendere alla matrice completa (tutti i PG x tutti i mostri) con scrittura CSV.
5. Eseguire con iterazioni basse (es. 100-1.000) per verificare i tempi di esecuzione
   totali, poi eventualmente alzare a 10.000 per il report definitivo.

## 11. Equipaggiamento del PG

Dalla fase 7 di `artefatti_e_incantamenti.md` il combattimento dipende dall'equipaggiamento: resistenze di
elmo, scudo e armatura, doppia arma, spadone, parata dello scudo. Con la sola `ArmaNaturale` il simulatore
non le vedeva, quindi al PG si può dare un equipaggiamento.

### 11.1 `Equipaggiamento`

Classe di supporto in `src/test/.../motore/modellodati`, accanto al simulatore. Un equipaggiamento ha un
nome e una lista di `Pezzo`: un `TipoArtefatto`, facoltativamente con un incantamento di un `TipoDanno`.

- **Costruzione dei pezzi.** Al livello del PG, con i valori medi di `GeneratoreArtefattiTabelle` ma
  senza la parte casuale, così due equipaggiamenti si confrontano senza rumore:
  - armi: danno `GeneratoreArtefatti.danniMediArma(livello)`, cioè `max(4 + 2 × livello, 11 + livello)`; lo spadone +50%, il bastone metà e +5% di `MAGIA` per livello;
  - scudo, elmo, armatura: +5% di `PARATA` per livello (la veste di `RESISTENZA_MAGICA`);
  - libro magico: +5% di `MAGIA` per livello;
  - pesi come nel generatore;
  - incantamento: del `GradoIncantamento` del livello (a livello 5, medio: +10 e +10%). Danno aggiuntivo
    sulle armi, resistenza su elmo, scudo e armatura. Il limite di effetti della rarità non si controlla.
- **Regole vere.** `equipaggia(pg)` fa prendere i pezzi uno alla volta passando da
  `Personaggio.puoEquipaggiare`. Se una classe non può portare l'equipaggiamento (il Guerriero con due
  spade, chi è troppo carico) non prende nessun pezzo e si ha il motivo. `motivoRifiuto(classe, livello)`
  fa lo stesso controllo su un PG nuovo.
- **Equipaggiamenti pronti** (`Equipaggiamento.TUTTI`, in quest'ordine):

  | Nome | Pezzi |
  | :--- | :--- |
  | `NESSUNO` | nessuno: arma naturale, come nella prima versione |
  | `SPADA` | spada |
  | `SPADA_E_SCUDO` | spada, scudo |
  | `SPADA_E_SCUDO_RARO` | spada, scudo raro (più `RESISTENZA_MAGICA`) |
  | `DUE_SPADE` | due spade (solo Ladro/Ladra, Elfo/Elfa) |
  | `SPADONE` | spadone |
  | `SPADA_DI_FUOCO` | spada con un incantamento di fuoco |
  | `CORAZZATO` | spada, scudo, elmo, armatura |
  | `CORAZZATO_CONTRO_VELENO` | come `CORAZZATO`, con l'armatura incantata contro il veleno |

  Per altre prove: `Equipaggiamento.di("NOME", Pezzo.di(TipoArtefatto.SCUDO).incantato(TipoDanno.GELO).raro(), ...)`.

### 11.2 Simulatore

- Nuova variante `simulaScontroGruppo(classePg, equipaggiamento, classeMostro, quantita, livello, iterazioni)`.
  Lancia `IllegalArgumentException` se la classe non può portare l'equipaggiamento. La firma di prima resta
  e usa `Equipaggiamento.NESSUNO`: dà gli stessi risultati di prima.
- Ogni attacco passa da `CalcolatoreCombattimento.fasiDiAttacco`, come nel turno di mischia del gioco: la
  seconda arma fa una seconda fase al 40% (`Costanti.DOPPIA_ARMA_FATTORE_SECONDA_ARMA`), sullo stesso bersaglio o sul prossimo vivo se la prima l'ha
  ucciso. Anche i mostri attaccano per fasi (oggi ne hanno sempre una sola, l'arma naturale).
- Limite: i PG non lanciano incantesimi (la "Fase 2" del §4 non c'è ancora), quindi il libro magico non
  ha effetto e non c'è fra gli equipaggiamenti pronti.

### 11.3 Test in `TestMonteCarloMatrix`

- `testSingoloScontroLadroConDueSpadeVsGoblin` (nella suite normale): nessuno stallo, e con due spade lo
  scontro dura meno che con una spada sola.
- `unaClasseCheNonPuoPortareLEquipaggiamentoVieneRifiutata` (nella suite normale): il Guerriero con
  `DUE_SPADE`.
- `testConfrontoEquipaggiamenti` (`@Disabled`, circa 2 minuti): tutti gli equipaggiamenti per Ladro, Elfa,
  Guerriero e Mago, contro Goblin, Troll, Minotauro e Viverna, 1 contro 1, a livello 5 (`LIVELLO_CONFRONTO`),
  3.000 iterazioni. Stampa una riga per combinazione e scrive `REPORT_BILANCIAMENTO_EQUIPAGGIAMENTI.csv`:

  ```
  PG,EQUIPAGGIAMENTO,MOSTRO,LIVELLO,WIN_RATE,LOSE_RATE,STALLO_RATE,TURNI_MEDI,TASSO_COLPIRE_PG,TASSO_COLPIRE_MOSTRO,DANNO_MEDIO_PG,DANNO_MEDIO_MOSTRO
  ```

  Le combinazioni che una classe non può portare si saltano, con un avviso sulla console.
- `testMatriceCompletaBilanciamento` ha la colonna `EQUIPAGGIAMENTO` dopo `PG` e gira su
  `EQUIPAGGIAMENTI_MATRICE` (di default `Equipaggiamento.TUTTI`, senza le combinazioni impossibili).
  Ogni equipaggiamento moltiplica il tempo: per una passata veloce basta lasciarne uno o due.
- Per lanciare un test `@Disabled` da Maven:
  `mvn test -Dtest='TestMonteCarloMatrix#testConfrontoEquipaggiamenti' -Djunit.jupiter.conditions.deactivate='org.junit.*DisabledCondition'`.
- **Dopo aver cambiato un valore in `Costanti`, prima dei test serve `mvn clean`.** Java copia nel codice che le usa
  le costanti `static final` al momento della compilazione, e Maven ricompila i test solo se cambia il loro
  sorgente: un test può così girare con il valore vecchio (è successo con `CalcolatoreCombattimentoClassiTest`).
  Il codice del gioco invece si ricompila per intero a ogni modifica.

### 11.4 Primo giro e bilanciamento (2026-09-24)

**Primo giro** (seconda arma al 60%, scudo +1 di `PARATA` per livello, niente `RESISTENZA_MAGICA`). Vittorie
del PG a livello 5, 1 contro 1:

| PG | Equipaggiamento | Goblin | Troll | Viverna |
| :--- | :--- | ---: | ---: | ---: |
| Ladro | `SPADA_E_SCUDO` | 100% | 91,7% | 39,2% |
| Ladro | `DUE_SPADE` | 100% | 98,3% | 81,7% |
| Guerriero | `SPADA_E_SCUDO` | 100% | 98,2% | 61,2% |

L'attacco rendeva molto più della difesa. In più, contro la Viverna lo scudo non serviva a nulla: il suo
morso è di veleno, elementale, e contro il danno elementale o magico contano `RESISTENZA_MAGICA` e
`SAGGEZZA`, non la `PARATA`. Il −25% di `PARATA` della guardia aperta non spostava i risultati con nessun
valore, perché la `PARATA` di base è di 2-6.

**Bilanciamento.** Seconda arma al 40%, scudo +3 di `PARATA` per livello e +1 di `RESISTENZA_MAGICA` per
livello (+2 se raro), guardia aperta al −25%. Secondo giro, stesse condizioni (vittorie e turni medi):

| PG | Equipaggiamento | Goblin | Troll | Minotauro | Viverna |
| :--- | :--- | ---: | ---: | ---: | ---: |
| Ladro | `NESSUNO` | 99,5% | 53,7% | 57,1% | 13,1% |
| Ladro | `SPADA` | 99,9% | 84,4% | 86,7% | 38,9% |
| Ladro | `SPADA_E_SCUDO` | 100% | 97,8% (7,5) | 97,7% | 59,0% |
| Ladro | `SPADA_E_SCUDO_RARO` | 100% | 97,2% | 97,9% | 73,4% |
| Ladro | `DUE_SPADE` | 100% | 96,4% (5,5) | 96,8% | 70,5% |
| Ladro | `SPADONE` | 100% | 96,1% | 97,3% | 69,7% |
| Ladro | `SPADA_DI_FUOCO` | 100% | 98,6% | 99,0% | 81,0% |
| Ladro | `CORAZZATO` | 100% | 98,1% | 97,9% | 58,8% |
| Ladro | `CORAZZATO_CONTRO_VELENO` | 100% | 98,3% | 98,6% | 87,2% |
| Guerriero | `SPADA_E_SCUDO` | 100% | 99,5% (8,1) | 99,7% | 76,4% |
| Guerriero | `SPADA_E_SCUDO_RARO` | 100% | 99,7% | 99,6% | 86,6% |
| Guerriero | `SPADONE` | 100% | 99,1% (6,0) | 99,0% | 83,0% |
| Guerriero | `CORAZZATO_CONTRO_VELENO` | 100% | 99,9% | 99,8% | 94,2% |
| Mago | `SPADA_E_SCUDO` | 87,7% | 15,4% | 18,3% | 12,1% |
| Mago | `SPADA_DI_FUOCO` | 92,3% | 31,8% | 33,8% | 30,1% |

Cosa se ne ricava:
- **Scudo e doppia arma ora si equivalgono.** Contro i mostri fisici lo scudo è un po' più sicuro, la
  doppia arma e lo spadone chiudono prima (5,5 turni contro 7,5). Contro i mostri elementali l'attacco resta
  avanti con lo scudo comune e si pareggia con quello raro, oppure con un incantamento sullo scudo.
- **Un incantamento medio vale moltissimo**: la spada di fuoco batte ogni altro equipaggiamento, e porta il
  danno medio del Ladro da 83 a 138 per colpo (+66%).
- **Le resistenze funzionano**: contro la Viverna l'armatura contro il veleno porta le vittorie del Ladro dal
  59% all'87%.
- **Elmo e armatura spogli non servono quasi a nulla**: `CORAZZATO` vince come `SPADA_E_SCUDO`, perché il
  loro +5% di `PARATA` per livello moltiplica una `PARATA` di base piccola (corretto nel §11.6).
- **A livello 1 una spada faceva meno delle mani nude** di un PG (6 contro 4 + 1,5 × √Forza, cioè 9-10).
  Per questo `testSingoloScontroLadroConDueSpadeVsGoblin` confronta le due spade con una spada sola.
  Dopo questo giro il danno delle armi basse è stato alzato (vedi sotto).
- **Il Mago in mischia** resta debole anche armato, come previsto: combatte con gli incantesimi, che il
  simulatore non usa (§9.3).

### 11.5 Danno delle armi di livello basso (2026-09-24)

Le armi generate facevano `4 + 2 × livello` di danno: 6 a livello 1, meno delle mani nude di un PG
(9-10 a ogni livello, 7 per il Mago). Ora fanno `max(4 + 2 × livello, 11 + livello)`: 12 a livello 1,
16 a livello 5, e dal livello 7 come prima. Vittorie contro il Troll, 1 contro 1:

| PG | Equipaggiamento | Livello 1 | Livello 2 | Livello 5 |
| :--- | :--- | ---: | ---: | ---: |
| Ladro | `NESSUNO` | 31,9% | 50,1% | 51,2% |
| Ladro | `SPADA` | 65,0% | 77,6% | 88,4% |
| Ladro | `SPADA_E_SCUDO` | 70,1% | 87,6% | 98,8% |
| Ladro | `DUE_SPADE` | 89,3% | 94,3% | 97,4% |
| Guerriero | `NESSUNO` | 81,2% | 84,2% | 87,9% |
| Guerriero | `SPADA` | 91,4% | 94,9% | 98,0% |
| Guerriero | `SPADA_E_SCUDO` | 93,0% | 97,6% | 99,8% |
| Guerriero | `SPADONE` | 98,6% | 99,5% | 99,4% |

Un'arma ora conta a ogni livello. A livello 5 i rapporti fra scudo e doppia arma restano quelli del §11.4
(contro la Viverna il Ladro vince il 69% con spada e scudo e il 78% con due spade). Ai livelli 1-2 invece
lo scudo resta indietro, perché la sua `PARATA` cresce con il livello (corretto nel §11.6).

### 11.6 Parata minima di scudo, elmo e armatura (2026-09-24)

Scudo, elmo e armatura hanno ora una `PARATA` intrinseca con una parte fissa: scudo 6 + 2 per livello
(era 3 per livello), elmo 2 + 1, armatura 3 + 1; la veste dà lo stesso minimo dell'armatura in
`RESISTENZA_MAGICA`. I valori sono stati scelti provando lo scudo a 4, 6 e 8 di parte fissa: oltre 6 non
cambia quasi nulla.

Il confronto che conta per lo scudo è quello del Guerriero (spada e scudo contro spadone): il Ladro di
solito combatte con due armi, e per lui la scelta è fra una spada e due. Vittorie e turni medi:

| PG | Equipaggiamento | Troll, livello 1 | Troll, livello 5 | Viverna, livello 5 |
| :--- | :--- | ---: | ---: | ---: |
| Guerriero | `SPADA_E_SCUDO` | 97,7% (13,4) | 99,9% (7,3) | 82,7% |
| Guerriero | `SPADONE` | 98,3% (9,9) | 99,6% (5,3) | 89,2% |
| Guerriero | `CORAZZATO` | 98,4% | 100% | 81,5% |
| Ladro | `SPADA` | 63,8% | 89,4% | 51,2% |
| Ladro | `DUE_SPADE` | 89,4% | 98,0% (5,0) | 78,5% |
| Ladro | `CORAZZATO` | 93,6% | 100% | 66,2% |
| Mago | `SPADA_E_SCUDO` | | 25,4% | 18,2% |
| Mago | `CORAZZATO` | | 56,7% | 18,7% |

- **Il Guerriero con lo scudo** ora pareggia lo spadone contro i mostri fisici anche a livello 1: è più
  lento ma altrettanto sicuro. Contro gli elementali lo spadone resta avanti, come deciso: lì conta la
  `RESISTENZA_MAGICA`, e uno scudo va incantato (o raro).
- **Elmo e armatura contano**: il Ladro corazzato a livello 1 vince contro il Troll più che con due spade,
  e il Mago corazzato a livello 5 passa dal 25% al 57%.
- **Contro la Viverna** l'armatura spoglia non aiuta (il suo morso è elementale): serve la resistenza al
  veleno (`CORAZZATO_CONTRO_VELENO`: Guerriero 96,8%, Ladro 92,8%).

## 12. Bilanciamento delle classi: piano

Cosa potrebbe portare un giocatore a preferire un Guerriero, un Ladro, un Elfo, un Bardo o un Mago, a parte i
gusti personali? Ognuno dovrebbe avere un motivo per essere scelto, non solo numeri più alti o più bassi.
L'idea di fondo:
- **tutti lanciano incantesimi**, perché basta consumare la pergamena su cui sono scritti; ma il **Mago** deve
  fare con la magia danni di gran lunga superiori a quelli di chiunque altro;
- l'**Elfo** combatte con più agilità del Mago ed è un po' meno bravo con la magia;
- il **Guerriero** con gli incantesimi, salvo colpi di fortuna, fa poco, ma in mischia con spada e scudo o
  spadone è il più forte;
- il **Ladro** agisce con destrezza: doppia arma, niente armature pesanti;
- il **Bardo** fa da supporto al gruppo.

Passi:
1. **Ruoli** (fatto): la tabella di armi, scudo e libro per classe è in `artefatti_e_incantamenti.md`, §2,
   "Equipaggiamento secondo la classe".
2. **Blocchi di equipaggiamento** (fatto): tabella per classe in `RegoleEquipaggiamento` e `FORZA` minima 16 per
   l'armatura.
3. **Moltiplicatori di danno delle classi** (fatto): `CalcolatoreCombattimento` usa `*_MOLTIPLICATORE_DANNI_FISICI`
   e `_MAGICI`, che c'erano ma non leggeva.
4. **Incantesimi nel simulatore** (fatto): `ScortaDiPergamene` (§13).
5. **Dotazioni tipiche per classe nel simulatore** (fatto): `Equipaggiamento.tipiciPer` (§13).
6. **Obiettivi e ritocchi** (in corso): il dardo arcano per Mago ed Elfo (§13.3) è fatto; i ritocchi del §13.4
   sono da misurare, e gli obiettivi per classe da fissare.

## 13. Classi, pergamene e dotazioni tipiche

### 13.1 Nel simulatore

- **`ScortaDiPergamene`**: quante pergamene di quale incantesimo malefico porta il PG in uno scontro. Finché ne ha
  una e abbastanza `MAGIA` per il costo di lancio, a ogni turno lancia l'incantesimo invece di attaccare con le
  armi; poi combatte con le armi. Come nel gioco, un incantesimo di portata `GRUPPO` colpisce tutti i mostri vivi,
  e ogni bersaglio si colpisce o si manca per conto suo. I mostri combattono solo con le armi.
- **Dardo arcano**: Mago ed Elfo, quando non hanno pergamene (o non hanno la `MAGIA` per lanciarle), lanciano il
  dardo arcano se promette più danno delle armi: probabilità di colpire per danno, come fanno i mostri quando
  scelgono fra magia e armi.
- **Scenari**: `SCENARI_CONFRONTO_CLASSI` dice quanti mostri e di quanti livelli sopra il PG: oggi 1, 2 e 3 mostri
  alla pari, e 1 mostro di un livello sopra. Una quantità oltre il massimo per locazione del mostro si salta.
  Attenzione: nel gioco il gruppo ha più personaggi, mentre nel simulatore un PG combatte da solo contro tutti i
  mostri. Tre mostri contro un PG solo sono quindi più duri del vero: servono a vedere le differenze fra le
  classi, non come obiettivo. I mostri del gioco stanno al livello del mondo, cioè del capo del gruppo, e il
  gioco è fracassone: non deve essere frustrante.
- **`Equipaggiamento.tipiciPer(classe)`**: le dotazioni che ogni classe userebbe davvero:

  | Classe | Dotazioni |
  | :--- | :--- |
  | Guerriero | `CAVALIERE` (spada, scudo, elmo, armatura), `SPADONE_E_ARMATURA` (spadone, elmo, armatura) |
  | Ladro | `DUE_SPADE_E_VESTE` (due spade, elmo, veste) |
  | Elfo | `DUE_SPADE_E_VESTE`, `LANCIA_E_VESTE` (lancia, elmo, veste) |
  | Bardo | `SPADA_SCUDO_E_VESTE` (spada, scudo, elmo, veste) |
  | Mago | `BASTONE_LIBRO_E_VESTE` (bastone, libro, veste) |

- **Test**: `conLePergameneIlMagoVincePiuSpesso` e `ogniClassePuoPortareLeSueDotazioniTipiche` nella suite
  normale; `testConfrontoClassi` (`@Disabled`, circa 2 minuti) prova ogni classe con le sue dotazioni e con
  0, 2 o 5 pergamene di fuoco, contro Goblin, Troll, Minotauro e Viverna, negli scenari qui sopra, ai livelli 1,
  5 e 10, e scrive `REPORT_BILANCIAMENTO_CLASSI.csv`. Dura circa 5 minuti e mezzo.

### 13.2 Primo giro (2026-09-25)

Con i moltiplicatori di classe collegati, i blocchi di equipaggiamento e i valori dei §11.5-§11.6. Vittorie
del PG, 1 contro 1:

| Livello | PG | Dotazione | Pergamene | Troll | Viverna |
| ---: | :--- | :--- | :--- | ---: | ---: |
| 1 | Guerriero | `CAVALIERE` | nessuna | 97,4% | 90,9% |
| 1 | Guerriero | `SPADONE_E_ARMATURA` | nessuna | 97,0% | 97,5% |
| 1 | Ladro | `DUE_SPADE_E_VESTE` | nessuna | 68,8% | 83,2% |
| 1 | Ladro | `DUE_SPADE_E_VESTE` | 2 di fuoco | 94,2% | 96,6% |
| 1 | Elfo | `DUE_SPADE_E_VESTE` | nessuna | 55,2% | 78,9% |
| 1 | Elfo | `LANCIA_E_VESTE` | nessuna | 30,1% | 50,1% |
| 1 | Bardo | `SPADA_SCUDO_E_VESTE` | nessuna | 21,8% | 37,6% |
| 1 | Bardo | `SPADA_SCUDO_E_VESTE` | 2 di fuoco | 86,7% | 90,7% |
| 1 | Mago | `BASTONE_LIBRO_E_VESTE` | nessuna | 0,0% | 0,0% |
| 1 | Mago | `BASTONE_LIBRO_E_VESTE` | 2 di fuoco | 89,2% | 97,8% |
| 5 | Ladro | `DUE_SPADE_E_VESTE` | nessuna | 91,8% | 98,4% |
| 5 | Elfo | `LANCIA_E_VESTE` | nessuna | 71,2% | 86,6% |
| 5 | Bardo | `SPADA_SCUDO_E_VESTE` | nessuna | 76,5% | 84,0% |
| 5 | Mago | `BASTONE_LIBRO_E_VESTE` | nessuna | 0,0% | 0,0% |
| 5 | Mago | `BASTONE_LIBRO_E_VESTE` | 2 di fuoco | 99,3% | 98,0% |

Cosa se ne ricava:
- **Il Guerriero domina la mischia**, come voluto: quasi sempre oltre il 97%, senza bisogno di pergamene.
- **Le pergamene decidono lo scontro per tutti.** Con due pergamene di fuoco quasi ogni classe vince oltre il
  90%, anche il Bardo e il Guerriero, che pure ha il moltiplicatore magico più basso (0,5). Un incantesimo di
  fuoco fa 60 × livello di danno base: oggi una pergamena vale più di un'arma intera.
- **Il Mago senza pergamene non vince mai**: con il bastone fa metà danno (moltiplicatore fisico 0,5) e regge poco.
  Con le pergamene vince, ma non più delle altre classi, che in più sanno anche combattere.
- **La Viverna è più debole di prima**: il suo morso è di veleno, elementale, e il suo moltiplicatore magico
  è 0,8.
- **Dal livello 5 in su, 1 contro 1, quasi tutti vincono oltre il 90%**: per distinguere le classi servono
  scontri più duri (più mostri, o mostri di livello più alto).

### 13.3 Pergamene e dardo arcano (2026-09-25)

**Danno delle pergamene.** Abbassare il danno di tutte le pergamene (`Costanti.INCANTESIMO_FATTORE_DANNI`)
punisce soprattutto il Mago, che vive di pergamene. Con 2 pergamene di fuoco contro un Troll a livello 1:

| Fattore | Mago | Ladro | Guerriero |
| ---: | ---: | ---: | ---: |
| 1,0 | 89% | 94% | 99% |
| 0,5 | 64% | 80% | 97% |
| 0,3 | 5% | 68% | 95% |

Il fattore resta a 1,0: le pergamene sono poche, e il loro peso va rivisto con l'economia.

**Dardo arcano** (30 × livello di danno base, 2 di `MAGIA`, per Mago ed Elfo). Media delle vittorie su Goblin,
Troll, Minotauro e Viverna, a livello 5, mostri alla pari, prima dotazione tipica di ogni classe:

| Classe | Pergamene | 1 mostro | 2 mostri | 3 mostri |
| :--- | :--- | ---: | ---: | ---: |
| Guerriero | nessuna | 99,3% | 79,5% | 57,1% |
| Guerriero | 2 di fuoco | 99,5% | 90,9% | 72,8% |
| Ladro | nessuna | 95,9% | 44,2% | 23,6% |
| Ladro | 2 di fuoco | 98,8% | 95,9% | 90,0% |
| Elfo | nessuna | 99,7% | 80,2% | 41,0% |
| Elfo | 2 di fuoco | 100% | 99,1% | 97,7% |
| Bardo | nessuna | 85,7% | 31,5% | 12,3% |
| Bardo | 2 di fuoco | 98,6% | 95,9% | 91,0% |
| Mago | nessuna | 98,9% | 68,8% | 42,0% |
| Mago | 2 di fuoco | 99,8% | 99,4% | 99,2% |

Senza il dardo, il Mago senza pergamene vinceva lo 0,6% contro un mostro solo.

### 13.4 Cosa resta da sistemare

- **L'Elfo è la classe più forte**: con il dardo arcano e le due armi vince senza pergamene quanto il Guerriero
  (80% contro 2 mostri), più del Mago (69%); a livello 1, uno contro uno, vince il 99,6% (il Guerriero il 96%).
  Doveva essere "un po' meno bravo con la magia" del Mago, e meno forte del Guerriero in mischia.
- **Il Mago non è ancora "di gran lunga" il migliore con la magia**: con le pergamene vince quanto l'Elfo.
- **Ladro e Bardo senza pergamene reggono poco contro i gruppi** (23% e 12% contro 3 mostri). Per il Bardo
  manca nel simulatore il suo ruolo di supporto.
- **Il Guerriero con le pergamene** migliora poco (73% contro 3 mostri, gli altri oltre il 90%), come voluto.

Ritocchi decisi dopo questo giro (2026-09-25), **non ancora misurati**: la prova è stata interrotta, va
rilanciata `testConfrontoClassi` e aggiornata questa sezione.
- **Elfo:** il dardo arcano gli costa 4 di `MAGIA` invece di 2 (`Costanti.DARDO_ARCANO_COSTO_LANCIO_ELFO`).
- **Mago:** il suo dardo fa 40 × livello invece di 30 (`Costanti.DARDO_ARCANO_DANNI_MAGO`).
- **Ladro e Bardo:** i moltiplicatori fisici restano quelli di prima (1,0 e 0,9); si sistemeranno più avanti, in
  altro modo.
- Restano da fissare gli obiettivi per classe, e da dare al Bardo il suo ruolo di supporto.
