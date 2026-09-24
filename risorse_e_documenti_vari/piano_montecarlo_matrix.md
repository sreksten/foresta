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
