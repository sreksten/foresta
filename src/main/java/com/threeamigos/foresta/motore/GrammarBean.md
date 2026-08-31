# GrammarBean — Manuale d'uso

`GrammarBean` legge un file di grammatica testuale e lo usa per **generare testo casuale**
espandendo ricorsivamente dei riferimenti fra produzioni. È il motore dietro `fiabe.txt`,
`oroscopo.txt` e `artefatti.txt`.

Questo documento è sia un **manuale di riferimento** della sintassi e del sistema di pesi,
sia un **assessment** della classe (§10) con le limitazioni e i difetti verificati
sperimentalmente sulla versione corrente.

---

## Indice

1. [Modello concettuale](#1-modello-concettuale)
2. [API pubblica](#2-api-pubblica)
3. [Struttura del file di grammatica](#3-struttura-del-file-di-grammatica)
4. [Riferimento completo della sintassi](#4-riferimento-completo-della-sintassi)
5. [Il sistema dei pesi](#5-il-sistema-dei-pesi)
6. [Il file di post-produzione](#6-il-file-di-post-produzione)
7. [La pipeline completa: cosa avviene e quando](#7-la-pipeline-completa-cosa-avviene-e-quando)
8. [Ricette e pattern d'uso](#8-ricette-e-pattern-duso)
9. [Errori e diagnostica](#9-errori-e-diagnostica)
10. [Assessment: limiti e difetti verificati](#10-assessment-limiti-e-difetti-verificati)
11. [Checklist per scrivere una grammatica](#11-checklist-per-scrivere-una-grammatica)

---

## 1. Modello concettuale

Una grammatica è un insieme di **produzioni**. Ogni produzione ha un nome e una lista di
**alternative** (i "figli"). Generare testo significa:

1. partire dalla produzione radice (`rootNode`);
2. scegliere una delle sue alternative (casualmente, con pesi — §5);
3. sostituire ogni riferimento `[Nome]` trovato nel testo scelto con una alternativa di
   `Nome`, scelta e a sua volta espansa allo stesso modo;
4. ripetere finché non restano più riferimenti;
5. applicare le sostituzioni di post-produzione e la pulizia degli spazi (§6);
6. restituire il risultato spezzato in righe (`List<String>`).

```
ARTEFATTO ──▶ [SPADA] ──▶ [SPADA_LIV_3] ──▶ "{ ... [ATTRIBUTO_PRE_NOME] Spada [ATTRIBUTO_POST_NOME] ... }"
                                                     │                          │
                                                 Terribile              della Vittoria
```

Il risultato di ogni produzione viene sempre **trimmato** prima di essere inserito nel testo
che la referenziava. Questo serve a rendere usabili le alternative volutamente vuote
(§4.5): non lasciano spazi spuri ai bordi della propria espansione.

---

## 2. API pubblica

### Costruttori

```java
// Da stringa, senza post-produzione
new GrammarBean(String grammatica)

// Da stringa, con post-produzione (può essere null)
new GrammarBean(String grammatica, String postProduzione)

// Da stream (l'uso tipico nel progetto); postProduction può essere null
new GrammarBean(InputStream grammatica, InputStream postProduzione)
```

Tutti dichiarano `throws InvalidGrammarException, IOException`. **Tutta la validazione e
tutto il calcolo dei pesi avvengono nel costruttore**: se la grammatica è accettata, i pesi
sono già fissati e non cambiano più per tutta la vita dell'oggetto.

Uso tipico nel progetto — è così che `ProduttoreDiTestiCasuale` carica `fiabe.txt` e
`oroscopo.txt`, le due grammatiche usate dal gioco:

```java
GrammarBean bean = new GrammarBean(
        MiaClasse.class.getResourceAsStream("/com/threeamigos/foresta/motore/artefatti.txt"),
        MiaClasse.class.getResourceAsStream("/com/threeamigos/foresta/motore/preposizioni_articolate_pp.txt"));
```

### Metodi

| Metodo | Descrizione |
| :--- | :--- |
| `List<String> produce()` | Genera partendo da `rootNode`. Al termine **svuota** la cache delle produzioni fissate globali. |
| `List<String> produce(String rootNode)` | Genera partendo dalla produzione indicata, senza cambiare `rootNode`. Come `produce()`, svuota la cache globale al termine. |
| `void reset()` | Ripristina le produzioni one-shot consumate e svuota la cache globale. Segna il confine fra una serie di generazioni e la successiva: **quando** chiamarlo è una scelta, vedi §4.7. |
| `void setRootNode(String)` | Cambia la radice. Lancia `IllegalArgumentException` se il nome non è una produzione definita. |
| `String getRootNode()` | La radice corrente. Di default è **la prima produzione dichiarata nel file**. |
| `void setProductionMode(ProductionModeEnum)` | `RANDOM` (default), `FIRST`, `LAST`. |
| `ProductionModeEnum getProductionMode()` | Il modo corrente. |
| `void addFixedProduction(String nome, String valore)` | Pre-imposta il valore che un riferimento `[*nome]` restituirà, prima di chiamare `produce()`. |

`produceImpl(String)` è package-private: espone l'espansione di un token già racchiuso in
parentesi e l'ordine interno delle fasi, quindi non fa parte della superficie pubblica.

### Un'istanza per thread

`GrammarBean` **non è thread-safe** e va confinato in un solo thread. Il punto che inganna è
che `produce()` sembra una lettura e non lo è: consuma le alternative one-shot, riempie e poi
svuota la cache delle produzioni fissate globali, e pesca da un `Random` condiviso. Due thread
sulla stessa istanza possono perdere i valori fissati l'uno dell'altro, consumare due volte la
stessa alternativa one-shot, o lasciare la mappa delle produzioni potata a metà. Anche
`reset()`, `addFixedProduction`, `setRootNode` e `setProductionMode` scrivono lo stesso stato.

Ciò che invece **è** condivisibile è il sorgente: tutto quello che viene analizzato al
caricamento — le produzioni, i pesi già calcolati, le regole di post-produzione — non viene
più riscritto dopo il costruttore. Quindi per generare testo da più thread si dà a ciascuno il
**proprio** `GrammarBean` costruito dallo stesso file, invece di sincronizzare l'accesso a
un'istanza sola.


### Modi di produzione

`FIRST` e `LAST` rendono la generazione **completamente deterministica**: sono pensati per i
test e per ispezionare una grammatica, non per il gioco. `RANDOM` è l'unico modo che usa i
pesi; con `FIRST`/`LAST` i pesi sono **ignorati** (l'alternativa è scelta per posizione).

---

## 3. Struttura del file di grammatica

Le regole di indentazione sono le uniche cose davvero strutturali:

```
NOME_PRODUZIONE                     ← riga che inizia a colonna 0: dichiara una produzione
	prima alternativa               ← riga che inizia con TAB o spazio: è un figlio
	seconda alternativa
	terza | quarta | quinta         ← più alternative sulla stessa riga, separate da |

# questa è una riga di commento, ignorata
                                    ← le righe vuote sono ignorate

ALTRA_PRODUZIONE
	testo con un [NOME_PRODUZIONE] dentro
```

Regole:

- La **prima** produzione dichiarata diventa la radice di default.
- Ogni produzione deve avere **almeno un'alternativa**; il controllo scatta al caricamento
  per ogni posizione nel file.
- I nomi di produzione **non possono essere duplicati** (errore al caricamento).
- Una riga figlio senza produzione che la precede è un errore.
- Il testo di ogni alternativa viene trimmato.
- Un'alternativa **vuota** è legittima e utilissima: rappresenta "qui non va niente"
  (§4.5). La si ottiene con un `|` finale (`grande|feroce|`) o con una riga contenente solo
  indentazione.

### Codifica dei caratteri

I file di grammatica e di post-produzione vanno scritti in **UTF-8**: è la codifica di tutte
le risorse del progetto, ed è quella che il `pom.xml` dichiara per la build
(`project.build.sourceEncoding`).

`GrammarBean` li decodifica indicando **UTF-8 esplicitamente**, non affidandosi al charset di
default della JVM, quindi le lettere accentate di una grammatica sopravvivono su qualunque
piattaforma e con qualunque versione di Java. Lo stesso vale per una `String` passata ai
costruttori testuali: viene codificata in UTF-8 prima di essere riletta.

---

## 4. Riferimento completo della sintassi

### Tabella riassuntiva

| Costrutto | Dove si mette | Risolto | Significato |
| :--- | :--- | :--- | :--- |
| `NOME` a colonna 0 | riga intera | caricamento | Dichiara una produzione |
| TAB/spazio iniziale | riga intera | caricamento | La riga è un'alternativa della produzione corrente |
| `\|` | fra alternative | caricamento | Separatore di alternative |
| `#` a inizio riga | riga intera | caricamento | Commento |
| `\` a fine riga | fine riga | caricamento | Continuazione: la riga successiva viene accodata |
| `NOME$` | header produzione | caricamento | Produzione **one-shot** |
| `{a\|b\|c}` | in un'alternativa | caricamento | Gruppo di alternanza **inline** |
| `"..."` | in un'alternativa | caricamento | Span **letterale** (protegge `{`, `}`, `\|`) |
| `[^N]` | **inizio** alternativa | caricamento | **Peso** di selezione |
| `[Nome]` | in un'alternativa | generazione | Riferimento semplice |
| `[*Nome]` | in un'alternativa | generazione | Riferimento **fissato globalmente** |
| `[!Nome]` | in un'alternativa | generazione | Riferimento **fissato localmente** |
| `[chiave=valore]` | in un'alternativa | generazione | **Assegnazione** (non produce testo) |
| `[#chiave]` | in un'alternativa | generazione | **Recupero** di un valore assegnato |
| `^[...]` | prima di un riferimento | generazione | **Capitalizza** l'iniziale del valore risolto |

> **Nota sulla colonna "Risolto".** È la distinzione più importante da tenere a mente.
> Tutto ciò che è risolto *al caricamento* è già stato trasformato e non esiste più quando
> si genera; tutto ciò che è risolto *alla generazione* viene rivalutato ad ogni `produce()`.
> Vedi §7.

### 4.1 Riferimenti fra produzioni

```
FRASE
	Il cavaliere incontrò [CREATURA]

CREATURA
	un drago | un troll | una chimera
```

Ogni riferimento è **indipendente**: due `[CREATURA]` nella stessa alternativa producono due
estrazioni distinte.

### 4.2 Produzioni fissate: `[*Nome]` e `[!Nome]`

Servono quando lo stesso valore deve ricomparire identico più volte.

- `[*Nome]` — **fissata globalmente**: la prima espansione dentro una chiamata a `produce()`
  viene messa in cache e riusata da *ogni* altra occorrenza, in qualunque punto del testo.
- `[!Nome]` — **fissata localmente**: idem, ma la cache vale solo dentro lo stesso
  sottoalbero di produzione, quindi rami fratelli generati indipendentemente ottengono
  valori diversi. Il valore **scende** in tutto ciò che è annidato sotto il punto che l'ha
  fissato, a qualunque profondità: un `[#Nome]` o un ulteriore `[!Nome]` in una produzione
  referenziata più in basso trovano lo stesso valore.

```
STORIA
	[*EROE] partì. Lungo la strada [*EROE] incontrò [!NEMICO], e sconfisse [!NEMICO].
```

`[*EROE]` è lo stesso nome in entrambe le occorrenze. `[!NEMICO]` è lo stesso nemico nelle
due occorrenze *di questo ramo*, ma un altro ramo della storia avrà il proprio.

È l'idioma da usare ogni volta che un'entità deve restare coerente all'interno di un
episodio ma cambiare fra un episodio e l'altro: `[!MANDANTE]` e `[!BERSAGLIO]` ripetuti
dentro lo stesso incarico restano gli stessi, mentre due incarichi generati
indipendentemente hanno mandanti diversi.

### 4.3 Variabili: `[chiave=valore]` e `[#chiave]`

`[chiave=valore]` memorizza `valore` sotto `chiave` e **non produce testo**. Il valore può
contenere a sua volta riferimenti, che vengono risolti *prima* di essere memorizzati:

```
INCARICO
	[ARMA=[ARMA_POSSIBILE]] Ti serve [#ARMA]. Senza [#ARMA] non entrare nella grotta.

ARMA_POSSIBILE
	una spada | un'ascia | un arco
```

Dettagli importanti:

- L'assegnazione è **sempre globale**, anche se scritta dentro un sottoalbero.
- Una chiave assegnata è un bersaglio valido anche per `[*chiave]` / `[!chiave]` / `[chiave]`,
  pur non esistendo una produzione con quel nome: la validazione al caricamento fa una
  passata preliminare per raccogliere tutte le chiavi assegnate, quindi l'assegnazione può
  comparire nel file *dopo* il riferimento che la usa.
- L'**ordine di esecuzione** però conta: `[#chiave]` letto prima che l'assegnazione sia
  stata eseguita lancia `IllegalArgumentException` a runtime. Il caricamento non può
  verificarlo.
- `[!Nome]` scritto in un ramo **è** visibile a `[#Nome]` in tutto ciò che sta annidato
  sotto di esso, a qualunque profondità; resta invisibile ai rami fratelli.

### 4.4 Gruppi di alternanza inline: `{a|b|c}`

Un gruppo `{...}` in un'alternativa viene trasformato al caricamento in una produzione
autogenerata (`PROD_0`, `PROD_1`, …) e sostituito con un riferimento ad essa. Da quel
momento si comporta esattamente come un `[Nome]` qualsiasi.

```
PROEMIO
	Il {vecchio|giovane|misterioso} viandante bussò alla porta
```

I gruppi possono essere **annidati**: `{a|{b|c}}` funziona, e produce le proprie produzioni
autogenerate ricorsivamente.

> ⚠️ **Un gruppo deve avere almeno due opzioni, cioè almeno un `|`.** Un gruppo con una
> sola opzione non ha nulla fra cui alternare: l'unico suo effetto sarebbe togliere le
> proprie graffe, quindi è **rifiutato al caricamento**. `TAG { [X] }` produceva
> silenziosamente `TAG valore` invece di `TAG { valore }`; ora è un errore che chiede di
> dichiarare l'intento — togliere le graffe, oppure quotarle per emetterle come testo
> (§4.6 e §8).
>
> `{a|}` va bene: sono due opzioni, di cui una vuota (§4.5). `{}` no: è una sola opzione
> vuota.

### 4.5 Alternative vuote (elementi opzionali)

Un'opzione vuota rende un elemento opzionale:

```
LUPO
	Un {grande |feroce |}lupo
```

produce `Un grande lupo`, `Un feroce lupo` oppure `Un lupo`.

Perché questo funzioni senza lasciare spazi doppi, dopo la post-produzione la classe
**collassa ogni sequenza di 2+ spazi/tab in un singolo spazio** e **rimuove ogni spazio/tab
immediatamente prima di `.` `,` `;` `:` `!` `?`**. Quindi si può scrivere
`sinistra [FORSE_VUOTO] destra.` senza preoccupazioni.

### 4.6 Span letterali: `"..."`

Dentro `"..."` i caratteri `{`, `}` e `|` sono testo normale, non sintassi. `\"` è una
virgoletta letterale che non chiude lo span. Le virgolette delimitatrici **vengono rimosse**
dal testo prodotto.

```
JSON
	"{ \"level\": 1, \"type\": \"SWORD\" }"
```

produce `{ "level": 1, "type": "SWORD" }`. È esattamente la tecnica usata da
`artefatti.txt` per emettere JSON.

Le parentesi quadre sono il caso particolare: uno span quotato **non** le protegge da sé,
perché un `[Nome]` dentro `"..."` deve poter essere espanso — è esattamente ciò su cui si
basa `artefatti.txt`, il cui JSON contiene `[ATTRIBUTO_*]` dentro gli span. Per una
parentesi **letterale** si usa un escape esplicito, `\[` e `\]`, valido dentro uno span
quotato allo stesso modo di `\"`:

```
ARRAY
	"{ \"tags\": \[[NUMERO]\] }"
```

produce `{ "tags": [7] }`: le quadre escapate restano testo, `[NUMERO]` si espande.

> ⚠️ L'escape esiste **solo dentro** uno span quotato, dove `\` è già il carattere di
> escape. Fuori da uno span `\[` non è un escape: la quadra viene comunque interpretata
> come inizio di token.

Uno span aperto e non chiuso entro fine riga (o entro il corpo di un gruppo inline) è un
errore al caricamento.

### 4.7 Produzioni one-shot: `NOME$`

Il `$` in coda al **nome della produzione** (non alle alternative) la rende one-shot: ogni
alternativa usata viene rimossa per il resto del ciclo di generazione.

```
PERSONAGGIO$
	il fabbro | il fruttivendolo | il capo della polizia
```

Serve a non ripetere lo stesso personaggio dentro la stessa sessione. Tipicamente si applica
ai serbatoi di entità che devono risultare uniche: personaggi, oggetti, luoghi bersaglio.

Comportamento all'esaurimento — **da conoscere, perché è aggressivo**:

1. Consumata l'ultima alternativa, la produzione viene rimossa.
2. **A cascata**, ogni alternativa di *qualunque* altra produzione che contenga
   `[NOME]`, `[*NOME]` o `[!NOME]` viene eliminata.
3. Se una produzione resta senza alternative, viene rimossa anch'essa, ricorsivamente.

La cascata può quindi svuotare porzioni ampie della grammatica, e il sintomo finale è un
`IllegalArgumentException: Production X is empty!` a una chiamata successiva — dove `X` può
stare diversi livelli sopra il serbatoio che si è davvero prosciugato.

#### Quando chiamare `reset()`, e quando non chiamarlo

Le produzioni one-shot rendono **stateful** una sequenza di `produce()`, e chi chiama decide
con `reset()` dove finisce una sequenza e dove comincia la prossima. Non esiste una politica
giusta in assoluto, perché i due scopi possibili sono opposti:

| Scopo | `reset()` | Perché |
| :--- | :--- | :--- |
| **Risultati indipendenti** — una fiaba per ogni visita alla locanda | dopo **ogni** `produce()` | Ogni risultato deve pescare dall'insieme completo. Senza reset i serbatoi si svuotano di chiamata in chiamata: i risultati impoveriscono e alla fine la generazione fallisce. |
| **Una serie deliberatamente senza ripetizioni** — cinque personaggi tutti diversi | **solo all'inizio** della serie | È esattamente ciò a cui serve il `$`. Resettare fra un elemento e l'altro lo annullerebbe, e i personaggi potrebbero ripetersi. |

Nel secondo caso la **lunghezza della serie è limitata dal serbatoio one-shot più piccolo** che
attraversa: superarla non dà un risultato scadente, dà l'eccezione di cui sopra. Se serve una
serie più lunga, il serbatoio va allargato.

### 4.8 Capitalizzazione: `^[...]`

Un `^` **immediatamente prima** della parentesi quadra aperta, fuori da essa, capitalizza
l'iniziale del valore risolto. Funziona con tutti i tipi di riferimento
(`^[Nome]`, `^[*Nome]`, `^[!Nome]`, `^[#chiave]`), gestisce le lettere accentate
(`àlbero` → `Àlbero`) ed è un no-op se il valore è vuoto.

```
FRASE
	^[CREATURA] attraversò il ponte.
```

Un `^` non seguito immediatamente da `[` è testo normale (`power^2` resta `power^2`).
Non esiste una sequenza di escape per un `^` letterale prima di un riferimento.

Da non confondere con `[^N]` (§5): stesso carattere, posizione opposta rispetto alla
parentesi, momento di risoluzione diverso.

### 4.9 Continuazione di riga: `\`

Un `\` come **ultimo carattere** della riga la unisce alla successiva. Vale anche per le
righe di header. Un `\` finale sull'ultima riga del file è un errore.

```
LUNGA
	questo è un testo molto lungo \
	che continua sulla riga seguente
```
### 4.10 Riferimenti con valore di default: `[NOME? | ripiego]`

Un riferimento di qualunque tipo può portare un **valore di ripiego**, scritto dopo il nome
come `?` seguito da `|` e dal testo da usare quando il valore non c'è:

```
RACCONTO
	dovette rinunciare al suo [*EQUIPAGGIAMENTO? | fagotto]
```

- Gli spazi intorno a `?` e `|` sono ignorati: `[K?|x]`, `[K ? | x]`, `[K?  |  x]` equivalenti.
- `[NOME?]` da solo è la forma breve per **ripiego vuoto**.
- Il ripiego è testo di grammatica come un altro: un letterale, un `[RIFERIMENTO]`, un gruppo
  `{a|b}`, o un misto. Viene trimmato come ogni espansione.
- Un `|` letterale nel ripiego si protegge con uno span quotato: `[K? | "a|b"]`.
- Funziona su tutte e quattro le forme: `[NOME?|…]`, `[*NOME?|…]`, `[!NOME?|…]`, `[#chiave?|…]`.
- `^[NOME? | …]` capitalizza anche il ripiego.

#### Quando scatta il ripiego

Ogni volta che **in quel punto** non c'è un valore, per qualunque ragione:

| Situazione | Senza `?` | Con `?` |
| :--- | :--- | :--- |
| il nome non è mai stato dichiarato, solo assegnato altrove | `Production X is empty!` | ripiego |
| il `[chiave=valore]` che lo riempirebbe non è ancora stato eseguito | `Production X is empty!` | ripiego |
| una produzione one-shot ha esaurito le alternative (§4.7) | `Production X is empty!` | ripiego |

È questo che rende inutile dichiarare una produzione-segnaposto solo per far passare la
validazione: **il ripiego è la garanzia** che qualcosa uscirà comunque.

#### Cosa il `?` non copre

Il `?` compra tolleranza su **quando** un valore compare, non sul fatto che il nome significhi
qualcosa. Il nome deve comunque essere una produzione dichiarata **oppure** una chiave
assegnata da qualche parte nella grammatica, altrimenti è un errore al caricamento:

```
Production MAI_VISTA is not defined: a '?' default covers the order in which an assignment
runs, not the existence of the name
```

Così un refuso resta un errore, invece di trasformarsi in un ripiego perpetuo e silenzioso.
Conseguenze:

- un nome di produzione **non può finire con `?`** (errore al caricamento);
- il valore di un'assegnazione è testo libero, quindi `?` e `|` dentro `[chiave=valore]`
  restano testo: `[K=a? | b]` assegna letteralmente `a? | b`.


---

## 5. Il sistema dei pesi

È la parte meno intuitiva della classe e quella che più spesso produce distribuzioni
inattese. Il peso effettivo di un'alternativa nasce da **due contributi sommati**.

### 5.1 Peso dichiarato: `[^N]`

Un token `[^N]` all'**inizio** dell'alternativa (dopo il trim) ne fissa il peso base.
`N` è un numero positivo, anche frazionario. Senza token, il peso base è **1**.

```
ROOT
	[^3] comune | raro | rarissimo
```

Il token viene rimosso dal testo al caricamento, quindi non compare mai nell'output.

Vincoli:

- `N` deve essere `> 0` e finito: `0`, `-1`, `abc`, `Infinity`, `NaN` sono errori al
  caricamento.
- Deve stare **all'inizio** dell'alternativa. `ciao [^9] mondo` non è un peso: viene
  interpretato come riferimento a una produzione chiamata `^9` e fallisce al caricamento
  con `Production ^9 is not defined`.
- Funziona anche sulle opzioni di un gruppo inline: `{[^20] x|y}`.
- **`[^N]` è ignorato in modo `FIRST`/`LAST`.**

Misurato su `ROOT → [^3] a|b|c` (200.000 estrazioni):

| Alternativa | Peso | Atteso | Misurato |
| :--- | ---: | ---: | ---: |
| `a` | 3 | 60,00% | 60,16% |
| `b` | 1 | 20,00% | 19,98% |
| `c` | 1 | 20,00% | 19,86% |

### 5.2 Boost automatico dei discendenti

Questo è il meccanismo che sorprende. **Dopo** che la grammatica è stata caricata e
validata, e una sola volta, ogni alternativa riceve un incremento di peso proporzionale
alla "ricchezza strutturale" di ciò che referenzia:

```
peso_effettivo(alternativa) = peso_dichiarato + 0.33 × Σ peso_aggregato(produzioni referenziate)

peso_aggregato(produzione) = Σ peso_effettivo(sue alternative)
```

dove `0.33` è la costante `SCALE_FACTOR`. Il calcolo è ricorsivo dal basso: quando si
somma il peso aggregato di una produzione referenziata, quella produzione ha già ricevuto
il proprio boost.

L'intento è che i rami che si aprono su sottoalberi più ricchi vengano scelti più spesso,
senza dover scrivere `[^N]` a mano. La conseguenza pratica è che **in una grammatica con
riferimenti i pesi non sono quasi mai quelli che sembrano**.

Dettagli:

- Una produzione referenziata **due volte nella stessa alternativa** contribuisce **due
  volte**.
- Un riferimento con ripiego (§4.10) conta **o** la produzione referenziata **o** ciò che
  referenzia il suo ripiego, mai entrambi: a runtime se ne percorre uno solo.
- `[*Nome]` e `[!Nome]` contribuiscono esattamente come `[Nome]`.
- `[chiave=valore]` e `[#chiave]` **non** contribuiscono (non referenziano produzioni), ma
  il *valore* di un'assegnazione sì: `[k=[ALTRA]]` conta `ALTRA`.
- Il boost è **permanente**: calcolato nel costruttore, congelato per tutta la vita
  dell'oggetto. `reset()` non lo ricalcola.

#### Esempio: ricchezza

`ROOT → [RICH]|[POOR]`, con `RICH` = 10 foglie e `POOR` = 1 foglia.

```
peso_aggregato(RICH)  = 10 × 1 = 10
peso_aggregato(POOR)  =  1 × 1 = 1
ROOT alt "[RICH]"     = 1 + 0.33 × 10 = 4.30
ROOT alt "[POOR]"     = 1 + 0.33 ×  1 = 1.33
P(POOR) = 1.33 / 5.63 = 23,6%
```

Misurato su 200.000 estrazioni: `z` (l'unica foglia di `POOR`) esce **23,53%** delle volte,
non il 50% che il file suggerisce a occhio.

#### Esempio: profondità

Il boost si **compone lungo la catena**, quindi la profondità conta:

`ROOT → [L1]|piatto`, `L1 → [L2]`, `L2 → [L3]`, `L3 →` 4 foglie.

```
aggregato(L3) = 4
L2 = 1 + 0.33 × 4      = 2.32
L1 = 1 + 0.33 × 2.32   = 1.7656
ROOT alt "[L1]"        = 1 + 0.33 × 1.7656 = 1.5826
ROOT alt "piatto"      = 1
P(piatto) = 1 / 2.5826 = 38,7%
```

Misurato: **38,60%**.

#### Esempio: riferimento doppio

`ROOT → [R] [R] | solo`, con `R` = 10 foglie:

```
alt "[R] [R]" = 1 + 0.33 × (10 + 10) = 7.60
alt "solo"    = 1
P(solo) = 1 / 8.60 = 11,6%
```

Misurato: **11,61%**.

### 5.3 Caso reale: `artefatti.txt`

Il file, così com'è, sceglie fra i quattro livelli di spada senza pesi:

```
SPADA
    { [SPADA_LIV_1] | [SPADA_LIV_2] | [SPADA_LIV_3] | [SPADA_LIV_4] } \
    "…template JSON…"
```

A occhio: 25% ciascuno. Ma ogni livello assegna un attributo in più del precedente, quindi
referenzia più produzioni e riceve un boost maggiore. Misurato su 300.000 generazioni:

| Livello | Riferimenti | Aggregato referenziato | Peso effettivo | Atteso | Misurato |
| :--- | :--- | ---: | ---: | ---: | ---: |
| 1 | nessuno | — | 1,33 | 6,0% | **6,0%** |
| 2 | pre | 20 | 3,51 | 15,9% | **15,9%** |
| 3 | pre, post | 20 + 26 | 6,34 | 28,7% | **28,8%** |
| 4 | pre, post, post 2, incantesimi | 20 + 26 + 20 + 22,2 | 10,94 | 49,5% | **49,3%** |

Il livello 4 referenzia anche la lista ricorsiva degli incantesimi: è quello che lo fa
schizzare a metà delle estrazioni. E si noti che lo squilibrio **peggiora ogni volta che si
aggiungono alternative agli attributi**, perché ne cresce l'aggregato: passando da 19 a 81
alternative il livello 1 è scivolato dal 12,7% al 6,0% senza che nessuno toccasse un peso.

**Il livello 4 — che dovrebbe essere il leggendario raro — è il più frequente, e il livello
1 il più raro.** Se l'intento era una progressione di rarità, il file va calibrato.

### 5.4 Come calibrare i pesi

Poiché `peso_effettivo = peso_dichiarato + boost`, e il boost è fisso e calcolabile, si
ricava il peso da dichiarare:

```
peso_dichiarato_i = peso_effettivo_desiderato_i − boost_i
```

**Procedura:**

1. Calcola (o stima) `boost_i` per ogni alternativa: `0.33 × Σ` aggregati referenziati.
2. Scegli le proporzioni desiderate e scalale in modo che **ogni** `peso_effettivo_i` sia
   `> boost_i` (altrimenti servirebbe un peso dichiarato ≤ 0, che è rifiutato).
3. Sottrai.
4. **Verifica misurando**: un ciclo di ~100.000 `produce()` con conteggio delle frequenze.
   Il passo 4 non è opzionale — a mano è facile sbagliare un aggregato.

Applicato ad `artefatti.txt` per ottenere 60 / 25 / 10 / 5 — i pesi si mettono sulle opzioni
del gruppo inline, esattamente come su alternative normali:

```
SPADA
    { [^131.67] [SPADA_LIV_1] | [^52.49] [SPADA_LIV_2] | [^16.66] [SPADA_LIV_3] | [^1.06] [SPADA_LIV_4] } \
    "…template JSON…"
```

Risultato misurato su 300.000 generazioni: **59,9% / 25,0% / 10,0% / 5,1%**. Sono numeri da
ricalcolare ogni volta che la grammatica cresce: ecco perché il passo 4, la misura, non è
opzionale.

> **Nota sul limite inferiore.** Facendo tendere il peso dichiarato a 0 il peso effettivo
> di un'alternativa non scende sotto il proprio `boost_i`. Se serve rendere un ramo ricco
> *molto* più raro di uno povero, non basta abbassare il suo `[^N]`: bisogna **alzare**
> quelli degli altri.

### 5.5 Cicli e asimmetria

Le produzioni possono referenziarsi a vicenda. Il calcolo dei pesi non va in loop: quando
incontra una produzione il cui boost è ancora in corso di calcolo, ne usa il peso **base**
non ancora incrementato.

La conseguenza è che **fra membri di un ciclo strutturalmente identici il risultato non è
simmetrico**: chi viene raggiunto primo riceve un boost maggiore. E chi viene raggiunto primo
è **la produzione dichiarata prima nel file**, perché le produzioni sono visitate in ordine di
dichiarazione. L'asimmetria resta, ma è leggibile dal file e la si sposta riordinando le
dichiarazioni.

Su `A → x[B]|stopA` e `B → y[A]|stopB` (perfettamente simmetrici):

```
A dichiarata prima → alternativa ricorsiva di A = 1 + 0.33 × 2.66   = 1.8778
                     alternativa ricorsiva di B = 1 + 0.33 × 2      = 1.66
P(stopA entrando in A) = 1 / 2.8778 = 34,75%
```

Chi è dichiarato prima ha il ramo ricorsivo **più pesante**, quindi esce dalla ricorsione
**meno** spesso. Invertendo le due dichiarazioni il quadro si specchia (misurato su 300.000
generazioni per configurazione):

| ordine nel file | P(stopA) | P(stopB) |
| :--- | ---: | ---: |
| `A` poi `B` | 34,67% | 37,47% |
| `B` poi `A` | 37,70% | 34,70% |

Va inoltre tenuto presente che **il boost rende i rami ricorsivi più probabili**, allungando
le ricorsioni. Non esiste alcun limite di profondità (§10, D5).

C'è di più, e vale la pena saperlo prima di provarci: su una produzione ricorsiva **il peso
della coda non è un controllo efficace della lunghezza**. Alzandolo cresce anche l'aggregato
della produzione stessa, che rientra nel boost del ramo ricorsivo perché il ciclo usa il peso
base non ancora incrementato. Su una lista `[^w]` / `[ELEMENTO] [LISTA]` con `ELEMENTO` di
aggregato 15:

| coda | P(continua) |
| :--- | ---: |
| `[^12]` | 46,0% |
| `[^32]` | 34,5% |
| `[^60]` | 30,3% |
| `[^1000]` | 25,2% |

La probabilità di continuare tende a `SCALE_FACTOR / (1 + SCALE_FACTOR)` = **24,8%**, e sotto
quella soglia non scende per nessun peso. Per limitare davvero la lunghezza di una lista serve
strutturarla senza ricorsione (alternative esplicite per 0, 1, 2 elementi).

---

## 6. Il file di post-produzione

File opzionale, una regola per riga, formato `pre:post`. Ogni occorrenza di `pre` nel testo
finale è sostituita con `post`. Serve a rammendare l'italiano prodotto per concatenazione
meccanica.

`preposizioni_articolate_pp.txt`:

```
 a il : al 
 a lo : allo 
 a l': all'
 ,:,
```

Note pratiche:

- Una riga che inizia con `#` è un **commento** e una riga vuota è ignorata, come nel file di
  grammatica — una regola come `[,:[` ha bisogno di una spiegazione accanto molto più di una
  riga di grammatica. Commenti e righe vuote **contano** comunque nella numerazione riportata
  dai messaggi d'errore, che punta quindi alla riga vera del file. Solo un `#` a inizio riga
  apre un commento: uno dentro una regola è testo. Di conseguenza un `pre` che inizia con `#`
  non è esprimibile, come nel file di grammatica.
- Una riga di soli spazi è considerata vuota. Qui è diverso dal file di grammatica, dove una
  riga indentata e vuota è un'alternativa vuota significativa: in una regola `pre:post` non
  potrebbe esprimere nulla.
- Lo split è sul **primo** `:`, quindi `post` può contenere `:`.
- Gli **spazi sono significativi** e sono lo strumento con cui si delimitano le parole:
  `pre = " a il "` con spazi ai lati evita di colpire "…ta il…".
- `pre` e `post` non possono essere vuoti o solo spazi (errore al caricamento).
- Una riga senza `:` è un errore.
- `post` **non può contenere `pre`** alla lettera: è un errore al caricamento
  (`Post production '…' contains its own pre production '…'`). Es. `a il:a il grande` è
  rifiutato. Il motivo è che ogni regola viene riapplicata finché il suo `pre` compare
  ancora, riscandendo il testo dall'inizio: una regola che riscrive il proprio `pre` non
  terminerebbe mai.
- Ogni regola è applicata **ripetutamente**, quindi anche le occorrenze che si
  **sovrappongono** vengono tutte riscritte. Con `pre = " a il "` (spazio iniziale e finale,
  quindi sovrapponibile con sé stesso), `vai a il a il castello` diventa
  `vai al al castello`.
- Le regole sono applicate **nell'ordine in cui compaiono nel file**. Conta quando due regole
  possono agganciare lo stesso tratto di testo: la prima elencata lo consuma. Con
  `abc:Y` prima di `ab:X`, il testo `abc` diventa `Y`; con l'ordine opposto diventa `Xc`. Un
  `pre` più lungo va quindi elencato prima di un `pre` che ne è un prefisso.

**Dopo** tutte le sostituzioni, sempre e comunque:

1. ogni sequenza di 2+ spazi/tab → un singolo spazio;
2. ogni spazio/tab immediatamente prima di `.` `,` `;` `:` `!` `?` → rimosso.

Il testo risultante è infine spezzato su `\n` in una `List<String>`.

---

## 7. La pipeline completa: cosa avviene e quando

Sapere cosa è già stato deciso al caricamento e cosa viene rideciso ad ogni generazione
spiega la maggior parte dei comportamenti sorprendenti.

### Al caricamento (una volta sola, nel costruttore)

| # | Fase | Effetto |
| :-- | :--- | :--- |
| 1 | Lettura riga per riga | Continuazioni `\` unite, commenti e righe vuote scartati |
| 2 | Header e figli | Produzioni registrate, `$` staccato, prima produzione = radice |
| 3 | Split su `\|` | Consapevole di `{...}`, `[...]` e `"..."` |
| 4 | `parseWeight` | `[^N]` staccato e convertito in numero, rimosso dal testo |
| 5 | `expandInlineAlternations` | `{...}` → produzioni `PROD_n`, virgolette rimosse |
| 6 | `collectAssignedKeys` | Raccolta di tutte le chiavi `[chiave=…]` |
| 7 | `checkProductionsValidity` | Ogni riferimento deve puntare a una produzione o a una chiave assegnata |
| 8 | `adjustWeightsForDescendants` | **Boost dei pesi, congelato per sempre** |
| 9 | Post-produzione | Regole `pre:post` caricate |
| 10 | `reset()` | Copia di lavoro delle produzioni |

### Ad ogni `produce()`

| # | Fase | Effetto |
| :-- | :--- | :--- |
| 1 | Espansione token | Ricerca ripetuta del `[` più a sinistra, risoluzione, sostituzione |
| 2 | Selezione alternativa | `FIRST` / `LAST` / estrazione pesata |
| 3 | Consumo one-shot | Alternativa rimossa, eventuale cascata |
| 4 | Cache fissate | `[*…]` globale, `[!…]` per sottoalbero |
| 5 | `^` | Capitalizzazione del valore risolto |
| 6 | Trim | Ogni produzione restituisce testo trimmato |
| 7 | `postProduce` | Sostituzioni + collasso spazi + spazi prima di punteggiatura |
| 8 | Split righe | `List<String>` |

Corollari utili:

- Un `[^N]` scritto male non arriva mai a runtime: fallisce al caricamento.
- Un `[#chiave]` usato prima dell'assegnazione **non** è intercettabile al caricamento.
- Non serve ottimizzare i pesi "a runtime": sono già calcolati.
- Cambiare `productionMode` non ricalcola nulla, ma disattiva l'uso dei pesi.

---

## 8. Ricette e pattern d'uso

### Elemento opzionale

```
CREATURA
	un {grande |feroce |vecchio |}lupo
```

### Lo stesso valore ripetuto nella frase

```
STORIA
	[*NOME] uscì di casa. Nessuno rivide [*NOME].
```

### Lo stesso valore, ma diverso per ogni ramo

```
INCARICO
	[!RICHIEDENTE] ti manda da [!BERSAGLIO]; riferisci a [!RICHIEDENTE].
```

### Un valore deciso una volta e usato molto dopo

```
INTRO
	[ARMA=[ARMA_POSSIBILE]] Parti con [#ARMA].
FINALE
	Con [#ARMA] in mano, tornasti al villaggio.
```

### Emettere JSON

```
OGGETTO
	"{ \"level\": 3, \"name\": \"[ATTRIBUTO] Spada\", \"tags\": \[1, 2\] }"
```

Le virgolette esterne proteggono `{` `}`; `\"` sono virgolette letterali; `[ATTRIBUTO]` si
espande; `\[` e `\]` sono quadre letterali, quindi anche gli array JSON sono scrivibili.

### Emettere graffe letterali (marcatori strutturali)

```
RECORD
	INCARICO "{" [CORPO_INCARICO] "}"
```

È l'unico modo: senza le virgolette il gruppo a singola opzione è un errore (§4.4).

### Generare N elementi tutti diversi

```
PERSONAGGIO$
	il fabbro | il fruttivendolo | la locandiera
```

```java
bean.reset();                                   // apre la serie
for (int i = 0; i < 3; i++) {                   // al massimo 3: tante quante le alternative
    personaggi.add(bean.produce("PERSONAGGIO").get(0));
}
```

Nessun `reset()` **dentro** il ciclo: è quello che garantisce i tre personaggi distinti (§4.7).

### Accumulare più valori in una lista

Un'assegnazione può **rileggere sé stessa**, perché il suo valore viene risolto prima di essere
memorizzato. `[LISTA="[#LISTA?]…"]` è quindi un *append*, non una sovrascrittura:

```
ATTRIBUTO
	[MODIFICATORI="[#MODIFICATORI?],{ \"attributo\": \"FORZA\" }"] della Vittoria
	[MODIFICATORI="[#MODIFICATORI?],{ \"attributo\": \"FURIA\" }"] della Furia
```

Il `?` serve al primo giro, quando la chiave non esiste ancora. Per una lista di lunghezza
**variabile** basta una produzione ricorsiva con la coda vuota pesata forte, così la ricorsione
termina presto:

```
INCANTESIMI
	[^12]
	[UN_INCANTESIMO] [INCANTESIMI]
```

> ⚠️ **Il separatore fra gli elementi non è esprimibile nella grammatica.** Una grammatica non
> sa se un elemento è il primo della lista, quindi ogni elemento porta la virgola *davanti* a
> sé e ne resta una di troppo all'inizio. La si toglie con una regola di post-produzione — è
> esattamente il lavoro per cui quel file esiste:
>
> ```
> # la virgola di troppo dopo la quadra aperta dell'array
> [,:[
> ```
>
> `artefatti.txt` e `artefatti_pp.txt` sono l'esempio completo: due collezioni JSON di
> lunghezza variabile, rilette da Gson.

### Leggere un valore che potrebbe non essere ancora stato assegnato

```
RACCONTO_P_2
	si mise in cammino, [FORNITO_DI] un [*EQUIPAGGIAMENTO]

RACCONTO_P_5
	dovette rinunciare al suo [*EQUIPAGGIAMENTO? | fagotto]
```

`RACCONTO_P_2` assegna, `RACCONTO_P_5` legge. Il `?` rende la seconda lettura indipendente
dall'ordine: funziona anche invocando `produce("RACCONTO_P_5")` da sola, o riordinando le
produzioni. **Non serve** dichiarare una produzione-segnaposto `EQUIPAGGIAMENTO` per far
passare la validazione: basta che il nome sia assegnato da qualche parte (§4.10).

### Generare risultati indipendenti fra loro

```java
for (int i = 0; i < 100; i++) {
    fiabe.add(bean.produce().get(0));
    bean.reset();                               // ogni fiaba riparte dall'insieme completo
}
```

### Ispezionare una grammatica in modo deterministico

```java
bean.setProductionMode(ProductionModeEnum.FIRST);
System.out.println(bean.produce());
```

### Verificare una distribuzione

```java
Map<String, Integer> conteggio = new TreeMap<>();
for (int i = 0; i < 100_000; i++) {
    conteggio.merge(bean.produce().get(0), 1, Integer::sum);
    bean.reset();   // necessario se la grammatica usa produzioni one-shot
}
```

---

## 9. Errori e diagnostica

### `InvalidGrammarException` (caricamento)

| Messaggio | Causa |
| :--- | :--- |
| `Production X already found.` | Nome di produzione duplicato |
| `Production X does not produce anything.` | Produzione senza figli |
| `Missing parent production.` | Riga indentata senza header che la precede |
| `Line continuation marker '\' at end of file` | `\` finale sull'ultima riga |
| `Missing '}' for inline alternation …` | Graffa non chiusa |
| `Missing closing '"'` | Span letterale non chiuso |
| `Inline alternation group '{…}' has no '\|'` | Gruppo inline con una sola opzione (§4.4) |
| `Reserved placeholder character found` | Il sorgente contiene un carattere `U+E000`/`U+E001`, riservato all'escape `\[`/`\]` |
| `Missing ']' for weight token` | `[^5 A` |
| `Invalid weight 'x': must be a positive number` | `[^0]`, `[^-1]`, `[^abc]`, `[^NaN]`, `[^Infinity]` |
| `Missing ']' element after token …` | Parentesi quadre sbilanciate |
| `Production X is not defined` | Riferimento a produzione inesistente — **anche `Production ^9 is not defined` per un `[^9]` non a inizio alternativa** |
| `Production X is not defined: a '?' default covers the order…` | `[X? \| …]` su un nome che non è né produzione né chiave assegnata (§4.10) |
| `? marker must be preceded by a node name` | `[? \| …]` |
| `Production name X? may not end with '?'` | Header di produzione che termina con `?` (§4.10) |
| `Empty fixed production present` | `[*]` o `[!]` da soli |
| `= symbol must be preceded by a node name` | `[=valore]` |
| `No pre production` / `No post production` / `Empty post production` | Riga malformata nel file di post-produzione (i commenti e le righe vuote sono ignorati, §6) |
| `Post production 'X' contains its own pre production 'Y': the substitution would never terminate` | Regola di post-produzione autoalimentante |

### `IllegalArgumentException` (runtime)

| Messaggio | Causa |
| :--- | :--- |
| `Production X is empty!` | Produzione rimossa dalla cascata one-shot, o riferimento a chiave mai assegnata |
| `Production #k not yet defined!` | `[#k]` letto prima che l'assegnazione o il `[!k]` corrispondente sia stato eseguito, oppure fuori dal sottoalbero che l'ha fissato |
| `X: not a valid production` | `setRootNode` con un nome inesistente |
| `Missing ']' element after token …` | Parentesi sbilanciate emerse a runtime |

### Altri errori

| Sintomo | Causa |
| :--- | :--- |
| `StackOverflowError` | Ricorsione senza alternativa di uscita (D5) |

---

## 10. Assessment: limiti e difetti verificati

Tutto quanto segue è stato **riprodotto sperimentalmente**: i difetti veri e propri sono
indicati con `Dn`, i limiti di progetto con `Qn`.

Le voci già corrette non compaiono più qui — il comportamento risultante è documentato dove
serve, nelle sezioni di riferimento (§4, §5, §6, §9), e il dettaglio della correzione è nella
storia di `git`. Ogni correzione è accompagnata da test di regressione eseguiti anche contro
il codice precedente, dove falliscono: verificano la correzione, non la descrivono.

### Punti di forza

- **Espressività alta per una grammatica di ~1300 righe**: alternanza, annidamento,
  variabili, valori fissati a due livelli di visibilità, one-shot, pesi, capitalizzazione,
  span letterali, continuazioni di riga.
- **Validazione anticipata generosa**: quasi tutti gli errori di sintassi e i riferimenti
  rotti emergono nel costruttore, non a metà partita.
- **Javadoc di qualità inusuale**: quasi ogni metodo privato spiega non solo cosa fa, ma
  perché è fatto così, incluse le proprietà scomode (asimmetria dei cicli, mancanza di
  escape per `^`).
- **Copertura di test ampia**: 161 test che coprono anche i casi limite fastidiosi
  (ricorsione, cicli, span quotati annidati, cascata one-shot, pesi frazionari).
- **Pulizia automatica degli spazi** che rende le alternative vuote realmente usabili senza
  sporcare ogni template.

### Difetti aperti

> La numerazione ha dei buchi: gli identificativi già corretti sono stati rimossi da questo
> elenco per non appesantirlo, ma quelli rimasti conservano il loro numero, così restano
> citabili. Il dettaglio delle correzioni è nella storia di `git`.

**D5 — Nessun limite di profondità di ricorsione.**

```java
new GrammarBean("ROOT\n\tx [ROOT]\n").produce();   // StackOverflowError
```

Aggravante: il boost dei discendenti (§5.2) **aumenta** la probabilità di scegliere il ramo
ricorsivo, perché è quello che referenzia il sottoalbero più ricco. Una grammatica
ricorsiva scritta con leggerezza degrada con l'uso. *Fix:* un contatore di profondità che,
oltre una soglia, forza la scelta fra le alternative non ricorsive.

### Limiti di progetto

**Q3 — Il boost dei discendenti non è disattivabile.** `SCALE_FACTOR` è una costante
privata. Non c'è modo di dire "questa produzione la peso io a mano". Il risultato è che
ogni distribuzione va misurata invece che letta (§5.3). Utile sarebbe un `SCALE_FACTOR`
configurabile e/o un marcatore per-produzione che disattivi il boost.

**Q5 — Un'assegnazione è sempre globale.** `assignFixedProduction` scrive nel campo
`globalFixedProductions` ignorando il parametro `superFixedProductions` che riceve. Il
javadoc lo documenta, ma il parametro non usato è un indizio di intento incompiuto.

### Priorità suggerite

| | Voce | Impatto | Sforzo |
| :-- | :--- | :--- | :--- |
| 1 | D5 nessun limite di ricorsione | crash | medio |
| 2 | Q3 boost non configurabile | pesi difficili da controllare | medio |
| 3 | Q5 assegnazione sempre globale | intento incompiuto | basso |


---

## 11. Checklist per scrivere una grammatica

**Struttura**

- [ ] Il file è salvato in **UTF-8** (§3).
- [ ] La prima produzione del file è la radice desiderata (o si chiama `setRootNode`).
- [ ] Ogni produzione ha almeno un'alternativa (verificato al caricamento in ogni posizione).
- [ ] Nessun nome di produzione duplicato.
- [ ] Nessuna riga indentata prima del primo header.

**Sintassi**

- [ ] Ogni `[` ha il suo `]`; ogni `{` il suo `}`; ogni `"` il suo `"`.
- [ ] I `[^N]` sono a **inizio** alternativa e valgono `> 0`.
- [ ] Ogni gruppo `{…}` ha almeno un `|` (altrimenti è un errore, §4.4).
- [ ] Le graffe destinate a comparire nell'output sono quotate: `"{"` e `"}"`.
- [ ] Ogni `[` letterale atteso nell'output è scritto `\[` dentro uno span quotato (§4.6).
- [ ] Nessun `\` finale sull'ultima riga del file.

**Semantica**

- [ ] Ogni `[#chiave]` è preceduto, **nell'ordine di generazione**, dalla sua assegnazione.
- [ ] Ogni `[#chiave]` sta nel sottoalbero che l'ha fissata, non in un ramo fratello.
- [ ] Ogni lettura che potrebbe precedere la propria assegnazione porta un ripiego
      `[NOME? | …]` (§4.10) — non una produzione-segnaposto.
- [ ] Ogni produzione ricorsiva ha almeno un'alternativa di uscita (D5).
- [ ] Per ogni produzione one-shot è chiaro quale dei due scopi serve — risultati
      indipendenti o serie senza ripetizioni — e `reset()` è chiamato di conseguenza (§4.7).
- [ ] I pool one-shot sono abbastanza capienti per la lunghezza massima che la grammatica può
      chiedere: una produzione ricorsiva che pesca da un serbatoio one-shot lo esaurisce, e la
      cascata di rimozione (§4.7) fa poi fallire la generazione a metà.

**Pesi**

- [ ] La distribuzione dei rami è stata **misurata**, non dedotta a occhio (§5.4).
- [ ] I livelli di rarità (se presenti) hanno l'ordine atteso — la trappola di
      `artefatti.txt` (§5.3).

**Post-produzione**

- [ ] Nessuna regola in cui `post` contiene `pre` (rifiutata al caricamento).
- [ ] Gli spazi delimitatori sono presenti dove servono (`" a il "`, non `"a il"`).
- [ ] Fra regole che possono agganciare lo stesso testo, la più specifica è elencata prima (§6).

**Uso dell'API**

- [ ] `reset()` è chiamato dove segna il confine giusto fra le serie di generazioni (§4.7).
- [ ] Un'istanza per thread (§2).
