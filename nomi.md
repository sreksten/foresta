# Nomi dei personaggi: piano

Piano per dare un nome coerente ai personaggi non combattenti e tenerne traccia in un registro. Stato: **le liste sono già
in `nomi.txt`** (condiviso da locande, fiabe e missioni); restano da fare le produzioni one-shot, la generazione dei nomi
alla creazione del mondo, l'enumerato e il registro. `GrammarBean` ha `#include` e `#include_static` (vedi
[`GrammarBean.md`](GrammarBean.md) §4.11, §4.12 e §4.7 per le produzioni one-shot). Il punto di partenza è la voce
"Unificare le grammatiche dei nomi di persona" di [`todo.md`](todo.md).

## 1. Com'è oggi

- **`nomi.txt`** (`src/main/resources/com/threeamigos/foresta/motore`): `locande.txt` e `missioni.txt` lo includono con
  `#include_static` (sezione condivisa), `fiabe.txt` con `#include` normale (le fiabe si azzerano a ogni racconto, quindi la
  loro copia è sempre piena). Contiene:
  - `NOME_MASCHILE` (100, uno per riga, in ordine alfabetico: è la somma dei nomi di locandieri, protagonisti delle fiabe,
    ostaggi, bardi, pellegrini e maghi), `COGNOME` (54), `ATTRIBUTO_POSITIVO` (35), `ATTRIBUTO_NEGATIVO` (35), `TITOLO` (35),
    `DEGLI_LOCANDE` (35, con le note tra parentesi pensate per i locandieri; è l'unica che porta in coda la grammatica di
    origine, per non confondersi con un `DEGLI` generico) e `DEGLI_ALTISONANTE` (35, senza note, ordinata ignorando la
    preposizione: "dei Signori della Luna", "di Altomonte"…), per il borgomastro;
  - dalle fiabe: `COGNOME_STOPPIONI`, `SOPRANNOME_PROTAGONISTA`, `TITOLO_NOBILIARE` e `NOME_SEMPLICE_PRINCIPESSA$` (66 nomi
    femminili, in ordine alfabetico);
  - dalle missioni: `NOME_DI_CAMPIONE`/`EPITETO_DI_CAMPIONE`, `NOME_DI_CAMPIONESSA`/`EPITETO_DI_CAMPIONESSA`,
    `NOME_DI_BRIGANTE`/`SOPRANNOME_DI_BRIGANTE`, `NOME_GOBLIN` e `SOPRANNOME_GOBLIN`.
- **Restano nelle grammatiche** le produzioni che combinano le liste, perché codice e testi le chiamano per nome:
  `NOME_CASUALE` e `ATTRIBUTO` (locande); `NOME_PROTAGONISTA`, `COGNOME_PROTAGONISTA`, `NOME_PRINCIPESSA` e
  `CREA_NOME_PRINCIPESSA` (fiabe); `NOME_OSTAGGIO`, `NOME_BARDO` e `NOME_PELLEGRINO` (tre produzioni dirette di `[NOME_MASCHILE]`, raccolte in un'unica sezione
  di `missioni.txt`; il codice le chiama per nome), `NOME_MAGO` ("Mastro [NOME_MASCHILE]"), `NOME_CAMPIONE`, `NOME_CAMPIONESSA`, `NOME_BRIGANTE` e `NOME_CAPOBANDA` (missioni).
- **Locande**: ognuna delle 35 ha `NOME_LOCANDIERE=[NOME_CASUALE]`, definito prima del dialogo (che ha `MI_CHIAMO`, con 11
  varianti, e usa `[#NOME_LOCANDIERE]`). Il nome è salvato come proprietà della locanda (`Locanda.LOCANDA_NOME_LOCANDIERE`).
  Per partita se ne usano 21: le 4 delle città più `max(compagni disponibili, (DIMENSIONE_X + DIMENSIONE_Y) / 4)` = 17
  (`Foresta.reimposta`, che le pesca tutte insieme con `ProduttoreDiTestiCasuale.getDatiLocanda`). Oggi nessun locandiere ha
  un nome fisso.
- **One-shot**: `NOME_MASCHILE$`, `COGNOME$`, `ATTRIBUTO_POSITIVO$`, `ATTRIBUTO_NEGATIVO$`, `TITOLO$`, `DEGLI_LOCANDE$` e
  `DEGLI_ALTISONANTE$` in `nomi.txt`, più `NOME_SEMPLICE_PRINCIPESSA$` e le locande (`CINGHIALE_VEGANO$`...). Provato: 21
  locandieri tutti diversi, e 1500 nomi di missione di fila senza errori.
- **Guardia contro l'esaurimento**: `ProduttoreDiTestiCasuale.produciDaMissioni` (usata da `nomeOstaggio`, `nomeBardo`,
  `nomePellegrino` e `rigaDiMissioni`, quindi anche da `NOME_MAGO`) controlla `canProduce` e, se i nomi sono finiti, chiama
  `GrammarBean.resetStaticProductions()` prima di produrre.
- **Codice**: i 18 compagni di `RegistroPersonaggi.reimposta()` sono scritti nel codice.
- **Non combattenti**: `TipoPersonaggio` ha già `LOCANDIERE`, `ARMAIOLO`, `ALCHIMISTA`, `VENDITORE_DI_PERGAMENE`,
  `INCANTATORE`, `MOGLIE_DEL_BARDO`, `BARDO_LOCANDA`, `CAPITANO_DELLE_GUARDIE` e `VIANDANTE`, ma nessuno ha un nome: le
  scene (`ScenaInCitta.con…`) usano un modello senza nome, e un personaggio senza nome lancia `IllegalStateException` in
  `getNome`. Il Viandante delle missioni di scorta prende il nome dalla missione. Il borgomastro e la levatrice non
  hanno né tipo né immagine (vedi "Mandanti senza personaggio proprio" in `todo.md`).
- **`RegistroPersonaggiMD`** conosce solo l'elenco iniziale dei compagni e i personaggi in attesa nelle locazioni.
- **`GrammarBean.resetStaticProductions()`** azzera **tutte** le sezioni statiche insieme, e non una sola; ogni grammatica
  che ne include una ricostruisce al prossimo uso anche le proprie produzioni one-shot (non il suo stato fisso). Oggi lo chiama
  `ProduttoreDiTestiCasuale.resetProduzioni()`, da `Foresta.reimposta()`.

## 2. Obiettivo

Ogni personaggio non combattente ha un nome proprio, deciso una volta sola alla creazione del mondo e coerente per
tutta la partita; un registro li raccoglie, e dà la base per le missioni di scorta e per un albo dei campioni
incontrati.

Chi ha bisogno di un nome:

| Chi | Quanti | Nome | Provenienza |
| :--- | :--- | :--- | :--- |
| Borgomastro | uno per città | nome altisonante ("… degli …") | sì |
| Capitano delle guardie | uno per città | nome proprio solo maschile | sì |
| Alchimista, armaiolo, venditore di pergamene, incantatore | uno per città | "Mastro" + nome maschile | sì |
| Bardo della locanda | uno solo, viaggia di locanda in locanda | solo nome, sempre lo stesso | no |
| Moglie del bardo | una sola | solo nome, da `NOME_FEMMINILE` | no |
| Locandiere | uno per locanda | **quello della locanda** (fisso o casuale, vedi §3.7) | no |
| Comparse create al volo (levatrice…) | a richiesta | pool che si ripete | no |
| Campioni delle sfide | quattro | nome ed epiteto scelti dalla missione | no |

La **provenienza** fa parte del "nome completo": "Mastro Ferruccio da Ruuna" (nome proprio "Mastro Ferruccio", provenienza
"Ruuna"). Si mette solo ai personaggi di città, per distinguere per esempio due armaioli.

## 3. Piano

### 3.1 `nomi.txt`: one-shot e radici nuove

- **Sono one-shot** (fatto: `NOME_MASCHILE$`, `COGNOME$`, `ATTRIBUTO_POSITIVO$`, `ATTRIBUTO_NEGATIVO$`, `TITOLO$`,
  `DEGLI_LOCANDE$`, `DEGLI_ALTISONANTE$`): un valore uscito non esce più, in nessuna grammatica che condivide la sezione, fino al prossimo
  `resetStaticProductions()`. Così i nomi dei locandieri sono sempre diversi. Le dimensioni (35 per `ATTRIBUTO_*`, `TITOLO`
  e `DEGLI_LOCANDE`, 54 cognomi, 100 nomi) coprono i 21 locandieri di una partita (ogni locandiere consuma al più un valore
  per lista, due cognomi nella variante col nome e cognome ma una sola volta per nome).
- **`NOME_FEMMINILE`**: è `NOME_SEMPLICE_PRINCIPESSA` rinominata e spostata sotto `NOME_MASCHILE`, e **resta one-shot**
  (`NOME_FEMMINILE$`): le fiabe hanno la loro copia, che si azzera a ogni racconto, e ci evita due nomi uguali in
  `CREA_NOME_PRINCIPESSA`; la moglie del bardo ne pesca una sola dalla sezione statica. Nel racconto `[*NOME_PRINCIPESSA]`
  resta quella prodotta. `fiabe.txt` va aggiornato nei tre punti in cui la cita.
- **Radici nuove**:
  - `NOME_MASTRO`: "Mastro [NOME_MASCHILE]", per i quattro artigiani (sulla falsariga di `NOME_MAGO`);
  - `NOME_CAPITANO`: `[NOME_MASCHILE]`, con un grado se serve;
  - `NOME_ALTISONANTE`: "[NOME_MASCHILE] [DEGLI_ALTISONANTE]", per il borgomastro (`DEGLI_ALTISONANTE` c'è già ed è
    one-shot);
  - `NOME_MOGLIE`: `[NOME_FEMMINILE]`; il bardo: `[NOME_MASCHILE]`;
  - `NOME_COMPARSA`: pool **non one-shot**, che si ripete, per levatrice e simili.
- **Restano nei loro file** le liste con un sapore proprio: epiteti dei campioni, soprannomi dei briganti, `NOME_GOBLIN`,
  `NOME_MAGO`.
- **Caricamento**: `ProduttoreDiTestiCasuale` carica già locande, missioni e fiabe con `GrammarBean.fromResource`;
  `GrammaticaArtefatti` usa ancora il costruttore con gli stream (non include nomi, quindi non serve).

### 3.2 Sequenza di generazione alla creazione del mondo

Tutta in `Foresta.reimposta()`, dopo `resetProduzioni()` (che azzera le sezioni statiche):

1. **Locande**: `getDatiLocanda(21)` come oggi; ogni locandiere pesca i suoi nomi dai pool one-shot, quindi sono tutti diversi.
2. **Città**, una alla volta, **senza reset** fra l'una e l'altra: borgomastro, capitano, alchimista, armaiolo, venditore di pergamene, incantatore, ognuno con
   la provenienza della città. I nomi sono diversi fra loro e da quelli già usciti.
3. **Bardo** (solo nome) e **moglie** (solo nome, da `NOME_FEMMINILE`): come i personaggi delle città, si **costruiscono
   a inizio partita** e si registrano subito, non quando servono. Il bardo è uno solo e viaggia di locanda in locanda.

Quanti ne servono: 21 nomi maschili per i locandieri, 4 × 6 = 24 per le città, 1 per il bardo: **46 su 100**.

### 3.3 Dove il piano non torna del tutto

- **Reset fra una fase e l'altra (deciso: non si fa).** `resetStaticProductions()` è globale: resetta anche i nomi già usati
  dalle locande, quindi il personale di una città potrebbe avere lo stesso nome del **locandiere della locanda di quella
  città**, pescato prima (le locande delle città sono le prime 4 del pool). Poiché 46 nomi su 100 bastano, si resetta **una
  volta sola**, a inizio partita, e tutti i nomi del mondo sono diversi. Il reset per città servirebbe solo se i pool
  diventassero troppo piccoli.
- **Durante la partita.** Ostaggi, pellegrini, bardi e maghi delle missioni pescano da `NOME_MASCHILE` (una volta
  one-shot, si consuma): dopo i 46 del mondo ne restano 54 e finiscono. Esaurito `NOME_MASCHILE`, la potatura a cascata
  (§4.7 di `GrammarBean.md`) svuota `NOME_CASUALE` e `NOME_OSTAGGIO`, e `produce()` lancia `IllegalArgumentException`. Quindi
  la scelta che hai fatto, resettare quando serve un nome, è giusta, e va fatta **dentro** `ProduttoreDiTestiCasuale`
  (`nomeOstaggio`, `nomeBardo`, `nomePellegrino`, e il capo di `rigaDiMissioni`): se `!missioni.canProduce("NOME_MASCHILE")`
  si chiama `resetStaticProductions()` e si riprova.
- **Doppioni con i residenti.** Dopo un reset, un ostaggio può chiamarsi come l'armaiolo di Ruuna (che però è "Mastro
  Ferruccio da Ruuna"), come un locandiere o come il bardo. Conviene che quei punti scartino un nome già in uso nel registro
  dei residenti (qualche tentativo, poi si accetta il doppione).
- **Il reset azzera anche le locande.** Ricostruisce le one-shot delle locande (`CINGHIALE_VEGANO$`…): dopo la creazione del
  mondo non importa (le 21 sono già estratte), ma `getDatiLocanda` non va richiamato a partita avviata.
- **Il bardo delle missioni.** `NonSparateSulPianista` prende il bardo da `NOME_BARDO` (una produzione diretta di
  `NOME_MASCHILE`). Poiché il bardo della locanda è già costruito e registrato a inizio partita, la missione si allinea
  semplicemente recuperando dal registro il suo nome (`getBardo()` smette di pescare) e `nomeBardo()` non serve più.
- **Le note di `DEGLI_LOCANDE`** finiscono oggi nel testo del dialogo (per esempio "Filiberto del Tagliabrodo (Famoso per
  diluire il brodo con l'acqua)"): da decidere se vanno tolte in post-produzione o spostate nella recensione.

### 3.4 L'enumerato `PersonaggioNoto`

Ogni voce dice chi è, con che ambito e da quale produzione prende il nome:

```java
enum PersonaggioNoto {
    //                      tipo                      ambito       produzione            provenienza
    BARDO                  (BARDO_LOCANDA,            GLOBALE,     "NOME_MASCHILE",      no),
    MOGLIE_DEL_BARDO       (MOGLIE_DEL_BARDO,         GLOBALE,     "NOME_MOGLIE",        no),
    CAPITANO               (CAPITANO_DELLE_GUARDIE,   PER_CITTA,   "NOME_CAPITANO",      sì),
    ARMAIOLO               (ARMAIOLO,                 PER_CITTA,   "NOME_MASTRO",        sì),
    ALCHIMISTA             (ALCHIMISTA,               PER_CITTA,   "NOME_MASTRO",        sì),
    VENDITORE_DI_PERGAMENE (VENDITORE_DI_PERGAMENE,   PER_CITTA,   "NOME_MASTRO",        sì),
    INCANTATORE            (INCANTATORE,              PER_CITTA,   "NOME_MASTRO",        sì),
    BORGOMASTRO            (null,                     PER_CITTA,   "NOME_ALTISONANTE",  sì),
    LOCANDIERE             (LOCANDIERE,               PER_LOCANDA, nome deciso dalla locanda, no),
    LEVATRICE              (null,                     COMPARSA,    "NOME_COMPARSA",      no),
    CAMPIONE               (classe variabile,         PER_SFIDA,   nome deciso dalla missione, no)
}
```

- **Ambiti**: `GLOBALE`, `PER_CITTA` (chiave: voce + città), `PER_LOCANDA`, `COMPARSA`, `PER_SFIDA`.
- Il tipo può essere `null` finché borgomastro e levatrice non hanno `TipoPersonaggio` e immagine.
- Le voci si aggiungono man mano che servono (fabbro, mugnaio, notaio…).

### 3.5 Il registro

- In `RegistroPersonaggiMD` una mappa `chiave → residente` (nome proprio, provenienza, `PersonaggioMD`), serializzata come
  il resto. I salvataggi non devono restare compatibili: nessuna migrazione.
- Il **nome completo** si compone dal nome proprio e dalla provenienza con `Misc.conPreposizione("da", città)`: "Mastro
  Ferruccio da Ruuna". Il nome proprio resta quello che `getNomeProprio()` dà oggi.
- **Quando si riempie**: alla creazione del mondo, secondo la sequenza di §3.2; le comparse alla prima richiesta
  (`residente(LEVATRICE, …)`), poi restano registrate.
- **Chi lo usa**: scene e missioni di scorta chiedono il personaggio al registro invece di costruirne uno senza nome.
  `RegistroPersonaggi` (motore) ne espone la lettura; per le scene vedi §5.
- **Campioni**: `LaSfidaDeiCampioni` li registra con nome ed epiteto quando li genera. Restano nel registro anche dopo la
  sfida, con un indicatore "sconfitto": base per un albo dei campioni incontrati.

### 3.6 Provenienza

Il campo sta nel registro e non nel `PersonaggioMD` dei combattenti, che non ne ha bisogno. Per le scene e per le missioni
il nome completo è quello che si legge ("Il capitano Rodolfo da Malgaard vi aspetta"); dove il nome è già detto in città, basta
il nome proprio.

### 3.7 Locandieri

Il nome resta deciso da `locande.txt` e salvato in `LOCANDA_NOME_LOCANDIERE`: un nome fisso rimane fisso, uno casuale
viene dal pool comune. Il registro lo **legge** dalla locanda e non lo pesca, così il Locandiere-personaggio ha sempre il
nome della sua locanda.

### 3.8 Ordine di lavoro

1. `nomi.txt`: `$` sulle liste (`NOME_MASCHILE`, `COGNOME`, `ATTRIBUTO_*`, `TITOLO`, `DEGLI_LOCANDE`, `DEGLI_ALTISONANTE`),
   `NOME_SEMPLICE_PRINCIPESSA$` → `NOME_FEMMINILE$` (sotto `NOME_MASCHILE`, con le fiabe aggiornate), radici nuove
   (`NOME_MASTRO`, `NOME_CAPITANO`, `NOME_ALTISONANTE`, `NOME_MOGLIE`, `NOME_COMPARSA`).
   Test di unicità e di esaurimento del pool.
2. `ProduttoreDiTestiCasuale`: accessori per le radici nuove (la guardia `canProduce` più reset per ostaggio, bardo,
   pellegrino e mago c'è già).
3. Enumerato, mappa in `RegistroPersonaggiMD`, generazione in `Foresta.reimposta()` secondo la sequenza di §3.2 (città,
   bardo e moglie a inizio partita).
4. Scene e missioni di scorta leggono il nome dal registro.
5. Campioni nel registro; poi la levatrice e le altre comparse.

## 4. Test

- Nessun nome esce due volte nella stessa partita, qualunque grammatica lo peschi (con `resetStaticProductions()` a
  inizio e a fine test).
- Una partita completa pesca 46 nomi maschili senza esaurire il pool; i pool bastano per le 21 locande (anche con la
  sequenza peggiore).
- Esaurito `NOME_MASCHILE`, `nomeOstaggio()` resetta e non lancia.
- Dopo salvataggio e ricarica ogni residente ha lo stesso nome e la stessa provenienza.
- I test che pescano molti nomi da `missioni.txt` senza passare da `ProduttoreDiTestiCasuale` (per esempio
  `NomiDelleMissioniTest`) vanno controllati: con le one-shot si esauriscono.
- I test che creano una partita con seme fisso vanno riverificati: la sequenza dei dadi cambia con chi pesca.

## 5. Osservazioni

- `ScenaInCitta` sta in `intermezzi`: i test di dipendenza possono vietare che importi il motore. Il nome deve arrivare
  da `VistaPartita` (sola lettura) oppure essere risolto da chi costruisce la scena. Da decidere.
- Le sezioni statiche sono stato globale: i test che creano una partita devono azzerarle.
- Se un nome è facoltativo conviene un ripiego (`[NOME? | …]`) anziché far lanciare `produce()`.
- Un solo `NOME_MASTRO` per quattro mestieri diversi consuma quattro nomi per città: è voluto, perché sono persone diverse.
- Le fiabe hanno la loro copia dei nomi (`#include` normale): non consumano il pool della partita e viceversa.

## 6. Da fare / idee aperte

- Tipi e immagini per borgomastro e levatrice.
- Decidere cosa fare delle note di `DEGLI_LOCANDE` nel dialogo (tolte in post-produzione o spostate nella recensione).
- Allineare `NonSparateSulPianista` al bardo registrato (vedi §3.3), poi togliere `NOME_BARDO` e `nomeBardo()`.
- Albo dei campioni incontrati (schermata o voce nel registro dei trofei?).
- I 18 compagni di `RegistroPersonaggi` potrebbero prendere i nomi da `nomi.txt`.
