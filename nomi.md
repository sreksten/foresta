# Nomi dei personaggi: piano

Piano per dare un nome coerente ai personaggi non combattenti e tenerne traccia in un registro. Stato: **fatti** le liste
one-shot in `nomi.txt`, la generazione dei nomi in `Foresta.reimposta`, l'enumerato `PersonaggioNoto`, il registro dei residenti
(`RegistroPersonaggi`, `ResidenteMD`), il salvataggio dei nomi, i campioni delle sfide, i tipi `UOMO` e `DONNA` per borgomastro e
levatrice, il nome del capitano nei testi delle missioni e il nome dei negozianti nelle loro schermate. **Da fare**: il nome
degli altri mandanti nei testi, le immagini proprie di borgomastro e levatrice e le altre voci di §6. `GrammarBean` ha
`#include` e `#include_static` (vedi [`GrammarBean.md`](GrammarBean.md) §4.11, §4.12 e §4.7 per le produzioni one-shot).

## 1. Com'è oggi

- **`nomi.txt`** (`src/main/resources/com/threeamigos/foresta/motore`): `locande.txt` e `missioni.txt` lo includono con
  `#include_static` (sezione condivisa), `fiabe.txt` con `#include` normale (le fiabe si azzerano a ogni racconto, quindi la
  loro copia è sempre piena). Contiene:
  - `NOME_MASCHILE` (98, uno per riga, in ordine alfabetico: è la somma dei nomi di locandieri, protagonisti delle fiabe,
    ostaggi, bardi, pellegrini e maghi), `COGNOME` (54), `ATTRIBUTO_POSITIVO` (35), `ATTRIBUTO_NEGATIVO` (35), `TITOLO` (35),
    `DEGLI_LOCANDE` (35, con le note tra parentesi che descrivono il locandiere: restano così; è l'unica che porta in coda la grammatica di
    origine, per non confondersi con un `DEGLI` generico) e `DEGLI_ALTISONANTE` (35, senza note, ordinata ignorando la
    preposizione: "dei Signori della Luna", "di Altomonte"…), per il borgomastro;
  - dalle fiabe: `COGNOME_STOPPIONI`, `SOPRANNOME_PROTAGONISTA`, `TITOLO_NOBILIARE` e `NOME_FEMMINILE$` (67 nomi
    femminili, in ordine alfabetico: erano i nomi delle principesse, e ora sono anche quelli delle mogli);
  - dalle missioni: `NOME_DI_CAMPIONE`/`EPITETO_DI_CAMPIONE`, `NOME_DI_CAMPIONESSA`/`EPITETO_DI_CAMPIONESSA`,
    `NOME_DI_BRIGANTE`/`SOPRANNOME_DI_BRIGANTE`, `NOME_GOBLIN` e `SOPRANNOME_GOBLIN`.
- **Restano nelle grammatiche** le produzioni che combinano le liste, perché codice e testi le chiamano per nome:
  `NOME_CASUALE` e `ATTRIBUTO` (locande); `NOME_PROTAGONISTA`, `COGNOME_PROTAGONISTA`, `NOME_PRINCIPESSA` e
  `CREA_NOME_PRINCIPESSA` (fiabe); `NOME_OSTAGGIO` e `NOME_PELLEGRINO` (due produzioni dirette di `[NOME_MASCHILE]`, in un'unica sezione
  di `missioni.txt`; il codice le chiama per nome), `NOME_MAGO` ("Mastro [NOME_MASCHILE]"), `NOME_CAMPIONE`, `NOME_CAMPIONESSA`, `NOME_BRIGANTE` e `NOME_CAPOBANDA` (missioni).
- **Locande**: ognuna delle 35 ha `NOME_LOCANDIERE=[NOME_CASUALE]`, definito prima del dialogo (che ha `MI_CHIAMO`, con 11
  varianti, e usa `[#NOME_LOCANDIERE]`). Il nome è salvato come proprietà della locanda (`Locanda.LOCANDA_NOME_LOCANDIERE`).
  Per partita se ne usano 21: le 4 delle città più `max(compagni disponibili, (DIMENSIONE_X + DIMENSIONE_Y) / 4)` = 17
  (`Foresta.reimposta`, che le pesca tutte insieme con `ProduttoreDiTestiCasuale.getDatiLocanda`). Oggi nessun locandiere ha
  un nome fisso.
- **One-shot**: `NOME_MASCHILE$`, `COGNOME$`, `ATTRIBUTO_POSITIVO$`, `ATTRIBUTO_NEGATIVO$`, `TITOLO$`, `DEGLI_LOCANDE$` e
  `DEGLI_ALTISONANTE$` in `nomi.txt`, più `NOME_FEMMINILE$` e le locande (`CINGHIALE_VEGANO$`...). Provato: 21
  locandieri tutti diversi, e 1500 nomi di missione di fila senza errori.
- **Guardia contro l'esaurimento**: `ProduttoreDiTestiCasuale.produciDaMissioni` (usata da `nomeOstaggio`,
  `nomePellegrino`, `nomeDiPersonaggioNoto` e `rigaDiMissioni`, quindi anche da `NOME_MAGO`) controlla `canProduce` e, se i nomi sono finiti, chiama
  `GrammarBean.resetStaticProductions()` prima di produrre.
- **Radici in `nomi.txt`**: `NOME_MASTRO` ("Mastro [NOME_MASCHILE]"), `NOME_CAPITANO`, `NOME_ALTISONANTE`
  ("[NOME_MASCHILE] [DEGLI_ALTISONANTE]"), `NOME_MOGLIE` (`[NOME_FEMMINILE]`) e `NOME_COMPARSA` (20 nomi che si ripetono,
  non one-shot); `NOME_FEMMINILE$` (67 nomi) è l'ex `NOME_SEMPLICE_PRINCIPESSA$`, che le fiabe citano per nome.
- **Residenti**: `tipi.PersonaggioNoto` (voce, classe, ambito, produzione di nomi, provenienza), `modellodati.ResidenteMD`
  (nome, provenienza, classe, sconfitto) e la mappa `chiave → ResidenteMD` in `RegistroPersonaggiMD`, salvata dopo i personaggi
  in locazione. `RegistroPersonaggi.getResidente(...)` dà il residente, creandolo al volo se manca;
  `creaIResidentiDellaCitta(citta)` e `creaIlBardoESuaMoglie()` li creano con il mondo, nell'ordine di §3.2.
  `getNomeCompleto` compone "Mastro Ferruccio da Ruuna"; `costruisciPersonaggio` ne fa il `Personaggio` col suo nome proprio;
  `registraCampione`/`segnaCampioneSconfitto`/`getCampioni` tengono i campioni delle sfide (li registra
  `LaSfidaDeiCampioni`, li segna battuti `IlCampione`).
- **Il bardo delle missioni**: `NonSparateSulPianista.getBardo()` legge il nome dal registro; `NOME_BARDO` e `nomeBardo()` non ci sono più.
- **Codice**: i 18 compagni di `RegistroPersonaggi.reimposta()` sono scritti nel codice.
- **Non combattenti**: `TipoPersonaggio` ha `LOCANDIERE`, `ARMAIOLO`, `ALCHIMISTA`, `VENDITORE_DI_PERGAMENE`, `INCANTATORE`,
  `MOGLIE_DEL_BARDO`, `BARDO_LOCANDA`, `CAPITANO_DELLE_GUARDIE`, `VIANDANTE` e i due generici `UOMO` e `DONNA` (classi `Uomo` e
  `Donna`, con l'immagine e il verso del locandiere e della moglie del bardo, e gli attributi dei non giocanti): sono il
  borgomastro e la levatrice, e i mandanti senza personaggio proprio (`ScenaInCitta.conMandante`/`conMandantessa`). Le scene
  disegnano il personaggio ma non ne scrivono il nome; il Viandante delle missioni di scorta prende il nome dalla missione, mentre `NonSparateSulPianista` scorta il `BardoLocanda` costruito dal registro, con il suo nome.
- **Nei testi e nelle schermate**: il capitano delle guardie si presenta con il nome nei testi delle missioni ("il capitano delle
  guardie, Rodolfo,": `IncaricoInCitta.mandante`/`mandanteDiCitta`, `Mandante.getApertura(nome)`, il mittente di `IlCorriere`);
  le schermate di armaiolo, venditore di pergamene, alchimista e incantatore hanno in cima il nome completo, spezzato su più
  righe, con il mestiere in piccolo sotto (`VistaPartita.getNomeDelPersonaggioDiCitta`, `disegnaIntestazioneCentrale`).
- **`RegistroPersonaggiMD`** conosce l'elenco iniziale dei compagni, i personaggi in attesa nelle locazioni e i residenti.
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
  e `DEGLI_LOCANDE`, 54 cognomi, 98 nomi) coprono i 21 locandieri di una partita (ogni locandiere consuma al più un valore
  per lista, due cognomi nella variante col nome e cognome ma una sola volta per nome).
- **`NOME_FEMMINILE`**: è `NOME_SEMPLICE_PRINCIPESSA` rinominata e spostata sotto `NOME_MASCHILE`, e **resta one-shot**
  (`NOME_FEMMINILE$`): le fiabe hanno la loro copia, che si azzera a ogni racconto, e ci evita due nomi uguali in
  `CREA_NOME_PRINCIPESSA`; la moglie del bardo ne pesca una sola dalla sezione statica. Nel racconto `[*NOME_PRINCIPESSA]`
  resta quella prodotta. `fiabe.txt` la cita ora con il nome nuovo, in `NOME_PRINCIPESSA` e `CREA_NOME_PRINCIPESSA`.
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

Quanti ne servono: 21 nomi maschili per i locandieri, 4 × 6 = 24 per le città, 1 per il bardo: **46 su 98**.

### 3.3 Dove il piano non torna del tutto

- **Reset fra una fase e l'altra (deciso: non si fa).** `resetStaticProductions()` è globale: resetta anche i nomi già usati
  dalle locande, quindi il personale di una città potrebbe avere lo stesso nome del **locandiere della locanda di quella
  città**, pescato prima (le locande delle città sono le prime 4 del pool). Poiché 46 nomi su 98 bastano, si resetta **una
  volta sola**, a inizio partita, e tutti i nomi del mondo sono diversi. Il reset per città servirebbe solo se i pool
  diventassero troppo piccoli.
- **Durante la partita.** Ostaggi, pellegrini e maghi delle missioni pescano da `NOME_MASCHILE` (una volta
  one-shot, si consuma): dopo i 46 del mondo ne restano 52 e finiscono. Esaurito `NOME_MASCHILE`, la potatura a cascata
  (§4.7 di `GrammarBean.md`) svuota `NOME_CASUALE` e `NOME_OSTAGGIO`, e `produce()` lancia `IllegalArgumentException`. Quindi
  la scelta che hai fatto, resettare quando serve un nome, è giusta, e va fatta **dentro** `ProduttoreDiTestiCasuale`
  (`nomeOstaggio`, `nomePellegrino`, e il capo di `rigaDiMissioni`): se `!missioni.canProduce("NOME_MASCHILE")`
  si chiama `resetStaticProductions()` e si riprova.
- **Doppioni con i residenti (aperto).** Dopo un reset, un ostaggio può chiamarsi come l'armaiolo di Ruuna (che però è
  "Mastro Ferruccio da Ruuna"), come un locandiere o come il bardo. Conviene che `produciDaMissioni` scarti un nome già in uso
  nel registro dei residenti (qualche tentativo, poi si accetta il doppione).
- **Il reset azzera anche le locande.** Ricostruisce le one-shot delle locande (`CINGHIALE_VEGANO$`…): dopo la creazione del
  mondo non importa (le 21 sono già estratte), ma `getDatiLocanda` non va richiamato a partita avviata (c'è l'avviso nel suo javadoc).
- **Il bardo delle missioni (fatto).** Il bardo della locanda è registrato a inizio partita, e `NonSparateSulPianista.getBardo()`
  ne legge il nome dal registro; `NOME_BARDO` e `nomeBardo()` sono stati tolti.

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
    BORGOMASTRO            (UOMO,                     PER_CITTA,   "NOME_ALTISONANTE",  sì),
    LOCANDIERE             (LOCANDIERE,               PER_LOCANDA, nome deciso dalla locanda, no),
    LEVATRICE              (DONNA,                    COMPARSA,    "NOME_COMPARSA",      no),
    CAMPIONE               (null /* la dà la missione */, PER_SFIDA, nome deciso dalla missione, no)
}
```

- **Ambiti**: `GLOBALE`, `PER_CITTA` (chiave: voce + città), `PER_LOCANDA`, `COMPARSA`, `PER_SFIDA`.
- Il tipo è `null` solo per il campione (la classe la dà la missione); borgomastro e levatrice sono `UOMO` e `DONNA` finché non avranno una classe e un'immagine proprie.
- Le voci si aggiungono man mano che servono (fabbro, mugnaio, notaio…).

### 3.5 Il registro

- In `RegistroPersonaggiMD` una mappa `chiave → ResidenteMD` (nome proprio, provenienza, classe, sconfitto), serializzata subito
  dopo i personaggi in locazione e provata con un salvataggio e una rilettura dell'intera partita. Il `Personaggio` non si salva: lo
  costruisce `costruisciPersonaggio` quando serve. I salvataggi non devono restare compatibili: nessuna migrazione.
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
il nome completo è quello che si legge nelle schermate dei negozi ("Mastro Ferruccio da Ruuna"); nei testi delle missioni, dove la
città è già nella frase, basta il nome proprio ("il capitano delle guardie di Nyena, Rodolfo,").

### 3.7 Locandieri

Il nome resta deciso da `locande.txt` e salvato in `LOCANDA_NOME_LOCANDIERE`: un nome fisso rimane fisso, uno casuale
viene dal pool comune. Il registro lo **legge** dalla locanda e non lo pesca, così il Locandiere-personaggio ha sempre il
nome della sua locanda.

### 3.8 Ordine di lavoro

1. **Fatto** — `nomi.txt`: `$` sulle liste, `NOME_FEMMINILE$`, radici nuove.
2. **Fatto** — `ProduttoreDiTestiCasuale`: `nomeDiPersonaggioNoto`, e la guardia `canProduce` con reset per ostaggio,
   pellegrino e mago.
3. **Fatto** — enumerato, mappa in `RegistroPersonaggiMD`, generazione in `Foresta.reimposta` (città, poi bardo e moglie, senza
   reset in mezzo).
4. **Fatto** — la missione del bardo legge il nome dal registro; i campioni sono nel registro.
5. **Fatto** — il capitano delle guardie si presenta con il nome nei testi delle missioni; borgomastro e levatrice sono `UOMO` e
   `DONNA`; le schermate di armaiolo, venditore di pergamene, alchimista e incantatore mostrano il nome del negoziante.
6. Da fare — il nome degli altri mandanti nei testi ("l'armaiolo", "l'alchimista", "il borgomastro"...), la levatrice (e le altre
   comparse) nelle scene che le chiedono, immagini proprie per borgomastro e levatrice.

## 4. Test

`NomiDeiPersonaggiDelMondoTest` (14 test) controlla:

- ogni città ha il suo borgomastro, capitano e artigiani, con la provenienza ("… da Ruuna"), e i quattro artigiani sono
  "Mastro …";
- il bardo e la moglie non hanno provenienza e hanno la loro classe; il borgomastro è un `UOMO` e la levatrice una `DONNA`;
- nessun nome del mondo si ripete, in 40 mondi con semi diversi;
- nessun nome di battesimo di un residente è anche di un locandiere;
- il bardo della missione è quello della locanda;
- il personaggio costruito ha il nome del residente;
- la levatrice compare alla prima richiesta e resta;
- i campioni restano nel registro dopo la sconfitta;
- salvataggio e ricarica conservano nome, provenienza, classe e sconfitta: del solo registro, e dell'intera partita (anche dopo
  aver generato un altro mondo); una nuova partita li dimentica.

Altri test: `IntestazioneDelNegozianteTest` (nome su più righe fra le cornici, altezza dell'intestazione, spostamento del fumetto e
della riga delle monete, caratteri dei nomi presenti nei due font Doomdark), `MandanteTest` (la presentazione del capitano),
`ScenarioTrofeiECartografoTest` (il capitano con il nome nella descrizione e nella consegna).

Da aggiungere: esaurito `NOME_MASCHILE`, `nomeOstaggio()` resetta e non lancia (la guardia è provata solo con uno script, 1500
nomi di fila). I test che creano una partita con seme fisso vanno riverificati a ogni modifica ai nomi: la sequenza dei dadi
cambia con chi pesca (è già successo con `BugMappaReimpostataTest` e `BugCacheMappaGeneraleTest`, che ora non dipendono dal seme).

## 5. Osservazioni

- Le scene (`ScenaInCitta` sta in `intermezzi`, che non importa il motore) disegnano il personaggio ma non scrivono il nome: se
  servirà un'etichetta, il nome dovrà arrivare da chi costruisce la scena. Le schermate dei negozi lo leggono da
  `VistaPartita`, come la UI fa con ogni altro stato.
- Nelle schermate dei negozi anche un nome corto può andare su due righe (la colonna fra le cornici è stretta): con la finestra a
  1024 px "Mastro Pippo da Ruuna" ne occupa due.
- Le sezioni statiche sono stato globale: i test che creano una partita devono azzerarle.
- Se un nome è facoltativo conviene un ripiego (`[NOME? | …]`) anziché far lanciare `produce()`.
- Un solo `NOME_MASTRO` per quattro mestieri diversi consuma quattro nomi per città: è voluto, perché sono persone diverse.
- Le fiabe hanno la loro copia dei nomi (`#include` normale): non consumano il pool della partita e viceversa.

## 6. Da fare / idee aperte

- Immagini proprie per borgomastro e levatrice (oggi `UOMO` e `DONNA`, con l'aspetto del locandiere e della moglie del bardo).
- Il nome degli altri mandanti nei testi delle missioni (armaiolo, alchimista, borgomastro...), come già per il capitano.
- Scartare, in `produciDaMissioni`, i nomi già in uso nel registro (vedi §3.3). Per ora un ostaggio può avere il nome di un
  residente, e ce ne facciamo una ragione.
- Albo dei campioni incontrati (schermata o voce nel registro dei trofei?).
- I 18 compagni di `RegistroPersonaggi` potrebbero prendere i nomi da `nomi.txt`.
- La levatrice e le altre comparse nelle scene che le chiedono.
