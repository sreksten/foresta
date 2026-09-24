# Assessment: `CalcolatoreCombattimento` vs `TipoAttributo`

Nota preliminare: `CalcolatoreCombattimento` non è attualmente richiamata da nessun punto del
motore di gioco (il combattimento "live" passa ancora da `PersonaggioBase.attacca()` /
`getDanniInCombattimento()`). Questo assessment riguarda quindi un motore di combattimento
"nuovo", già scritto ma non collegato, confrontato con le intenzioni di design dichiarate nei
Javadoc di `TipoAttributo`.

Attributi totali modellati in `TipoAttributo`: 22
(SALUTE, FORZA, DESTREZZA, COSTITUZIONE, INTELLIGENZA, SAGGEZZA, CARISMA, FORTUNA, CRITICO,
PRECISIONE, VELOCITA, FURTIVITA, PARATA, RESISTENZA_MAGICA, PERCEZIONE, SOGGEZIONE, FURIA,
CARICO, MAGIA, CORAGGIO, VALORE, STANCHEZZA, NUMERO_BERSAGLI)

Tutti gli attributi sono già esposti tramite `Personaggio` con relativo getter che somma il
valore base al modificatore degli artefatti (`getModificatore(TipoAttributo)`), quindi non
esistono limitazioni tecniche all'uso di uno qualsiasi di essi in `CalcolatoreCombattimento`:
è puramente una questione di formula mancante.

Nota di allineamento (aggiornamento più recente): a valle del cross-check con le convenzioni dei
GDR fantasy moderni, sono stati corretti quattro punti in cui i Javadoc di `TipoAttributo` e la
formula di `CalcolatoreCombattimento` erano incoerenti tra loro. I Javadoc sono stati aggiornati
di conseguenza e questo documento riflette lo stato attuale, non quello precedente alla modifica:
- CARISMA non copre più la resistenza a CONFUSO/SPAVENTATO: quel ruolo è passato a SAGGEZZA.
- FORTUNA, non CRITICO, è ora la contromisura difensiva ai Colpi Critici.
- PARATA mitiga il danno fisico subito (oltre a contribuire alla schivata, come già faceva).
- La DESTREZZA del difensore entra nella formula di schivata in `colpisce()`.

---

## 1) Attributi attualmente usati

### `colpisce(attaccante, difensore)`

| Attributo | Chi lo usa | Ruolo nella formula |
|---|---|---|
| PRECISIONE | attaccante | Componente offensiva: `precisioneTotale = precisione + destrezza` |
| DESTREZZA | attaccante | Componente offensiva (vedi sopra) |
| VELOCITA | difensore | Componente difensiva: `velocitaTotale = velocita + destrezza` |
| DESTREZZA | difensore | Componente difensiva (vedi sopra) |
| SAGGEZZA | attaccante | Riduce la penalità di precisione da CONFUSO |
| SAGGEZZA | difensore | Riduce la penalità di schivata da SPAVENTATO |
| PERCEZIONE | attaccante | Riduce la penalità di precisione da ACCECATO: `(prec/2) * (1 - percezione/100)` |
| CORAGGIO | difensore | Riduce ulteriormente la penalità di schivata da SPAVENTATO: `min(coraggio/100, 1)` |
| STANCHEZZA | attaccante | Penalità di precisione: `precisioneTotale -= stanchezza * 2` |
| STANCHEZZA | difensore | Penalità di schivata: `velocitaTotale -= stanchezza * 2` |

Formula finale: `probabilita = 75 + (precisioneTotale - velocitaTotale) * 2`, clampata tra 5 e 95.
Gli effetti di stato (CONFUSO, ACCECATO, RALLENTATO, SPAVENTATO, STORDITO, ATTERRATO, CONGELATO)
modificano questi due totali o cortocircuitano l'esito, ma non sono attributi.

**Coerenza con i Javadoc:** ora piena per gli attributi coinvolti:
- Il Javadoc di DESTREZZA dichiara un ruolo sia offensivo che difensivo ("in difesa: incrementa
  il valore di Schivata"): la formula legge ora la DESTREZZA di entrambi i contendenti, quella
  dell'attaccante in chiave offensiva e quella del difensore in chiave di schivata.
- Il Javadoc di SAGGEZZA dichiara resistenza mentale a CONFUSO/SPAVENTATO: la formula scala ora
  le rispettive penalità in base alla SAGGEZZA di chi subisce l'effetto (attaccante per CONFUSO,
  difensore per SPAVENTATO), con un tetto massimo (`Math.min`) per non annullare mai del tutto
  la penalità base.
- Il Javadoc di VELOCITA parla di iniziativa nei turni e fuga, non di schivata; nel codice
  viene invece usata come componente difensiva di schivata insieme a DESTREZZA. È una scelta di
  design accettabile (in assenza di uno stat "Schivata" dedicato) ma resta un disallineamento
  narrativo minore, non corretto in questo intervento perché non richiesto e perché VELOCITA non
  era in discussione.
- PARATA non compare più in questa formula: il suo ruolo defensivo è ora esclusivamente la
  mitigazione del danno fisico (vedi sotto), coerentemente con il Javadoc, che non le assegna
  alcun ruolo di schivata.

### `calcolaDannoFinale(attaccante, difensore, tipoDanno, arma)`

| Attributo | Chi lo usa | Ruolo nella formula |
|---|---|---|
| INTELLIGENZA | attaccante | `statOffensiva` per danni MAGICO/ELEMENTALE |
| FORZA | attaccante | `statOffensiva` per danni FISICO |
| SAGGEZZA | attaccante | Bonus su `statOffensiva` quando `tipoDanno == SACRO` (+50% della SAGGEZZA) |
| RESISTENZA_MAGICA | difensore | `statDifensiva` per danni MAGICO/ELEMENTALE |
| COSTITUZIONE | difensore | `statDifensiva` per danni FISICO |
| PARATA | difensore | `statDifensiva` per danni FISICO (insieme a COSTITUZIONE) |
| SAGGEZZA | difensore | Riduce moltiplicatore di danno da NECROTICO su MALEDETTO: `max(1.0, 2.0 - saggezza/100)` |
| CRITICO | attaccante | `probabilitaCritico = 5 + critico - fortuna difensore` |
| FORTUNA | difensore | Contromisura a `probabilitaCritico` (vedi sopra) |
| MAGIA | attaccante | Innesco stati nativi (proc) per danni MAGICO/ELEMENTALE: `statAusiliariaProc` |
| FURIA | attaccante | Innesco stati nativi (proc) per danni FISICO: `statAusiliariaProc` |
| FORZA | difensore | Usata anche come denominatore nella probabilità di proc (`(dannoMitigato*100)/difensore.getForza()`) |

Nota: `arma.getLivello()`/`attaccante.getLivello()` intervengono nella formula ma **non sono
`TipoAttributo`** — sono proprietà di personaggio/oggetto separate, fuori dal perimetro di
questo assessment.

**Coerenza con i Javadoc:**
- FORZA sull'attaccante è coerente ("Incrementa direttamente il danno... colpi in mischia").
  La parte difensiva del Javadoc di FORZA ("permette di resistere agli effetti di spostamento
  fisico... riduce la fatica quando si bloccano attacchi pesanti") non è implementata da
  nessuna parte.
- INTELLIGENZA/RESISTENZA_MAGICA sono coerenti con i rispettivi Javadoc.
- SAGGEZZA è ora coerente: in attacco ("Potenzia direttamente i danni di tipoVariazione Sacro")
  entra come bonus su SACRO; in difesa ("Aumenta la resistenza mentale e riduce la durata
  delle Maledizioni") riduce l'effetto dei danni NECROTICO su bersagli MALEDETTI.
- PARATA è ora coerente col proprio Javadoc ("riduce o azzera il danno fisico in arrivo... statistica
  chiave per i Tank"): entra in `statDifensiva` insieme a COSTITUZIONE per i danni non
  magici/elementali, e beneficia quindi anche del bonus del guscio di ghiaccio su CONGELATO.
- CRITICO/FORTUNA sono ora coerenti con i rispettivi Javadoc: CRITICO aumenta la probabilità di
  colpo critico dell'attaccante, FORTUNA del difensore la riduce (clampata a un minimo di 0 con
  `Math.max`, per evitare probabilità negative).
- FURIA è usata solo per il proc rate degli stati sui danni fisici. Il Javadoc di FURIA
  descrive però un meccanismo ben più specifico e non implementato: "Aumenta esponenzialmente
  il danno fisico man mano che la salute del personaggio diminuisce (meccanica Berserk)". Questo
  richiederebbe SALUTE corrente/massima dell'attaccante, che oggi non entra mai nel calcolo del
  danno.
- Usare la FORZA del **difensore** come termine puramente statistico nel denominatore del proc
  (`difensore.getForza()`) è un uso "di comodo" abbastanza distante dal significato narrativo di
  FORZA ("potenza muscolare"); funzionalmente è più simile a una "resistenza alla contaminazione
  di stato" — vale la pena chiedersi se non sia un residuo di refactoring più che una scelta
  voluta.

---

## 2) Attributi NON usati in nessuno dei due metodi

Elenco completo, con la proposta di integrazione basata **direttamente** sul Javadoc già scritto
in `TipoAttributo` (quindi senza inventare nuova lore, solo implementando quanto già dichiarato):

### SALUTE
Non entra mai nel calcolo del danno o della riuscita. Il proprio Javadoc non promette un ruolo
diretto in combattimento (è più "stato" che "attributo attivo"), ma è il prerequisito tecnico
per implementare la meccanica Berserk già promessa da FURIA (vedi sopra). **Proposta:**
introdurre in `calcolaDannoRisultante` un fattore `1 + (1 - salute/saluteMassima) * k` da applicare
al danno fisico quando l'attaccante ha `getFuria() > 0`, così da far coincidere finalmente
comportamento e Javadoc di FURIA.

### SAGGEZZA (in attacco / su SACRO e MALEDETTO)
La componente difensiva di SAGGEZZA (resistenza a CONFUSO/SPAVENTATO) è implementata in
`colpisce()` (vedi sezione 1). Anche le due parti di Javadoc relative al danno sono ora
implementate in `calcolaDannoRisultante`.

**Implementazione:**
- Quando `tipoDanno == TipoDanno.SACRO`: aggiunto bonus `+saggezza/2` a `statOffensiva`
  dell'attaccante. Questo potenzia direttamente i danni SACRO come promesso dal Javadoc
  ("Potenzia direttamente i danni di tipoVariazione Sacro").
- Quando `difensore.hasEffettoDiStato(MALEDETTO)` e `tipoDanno == TipoDanno.NECROTICO`: il
  moltiplicatore base 2.0x è ridotto dalla SAGGEZZA del difensore secondo la formula
  `max(1.0, 2.0 - saggezza/100)`. La SAGGEZZA mitiga l'effetto della maledizione, coerentemente
  col Javadoc ("Aumenta la resistenza mentale e riduce la durata delle Maledizioni").

### CARISMA
Il Javadoc non gli assegna più alcun ruolo in CONFUSO/SPAVENTATO (spostato a SAGGEZZA, vedi
sopra): il suo ruolo difensivo attuale è resistere a manipolazione/corruzione/intimidazione
sociale. Questo è già implementato, ma **fuori da `CalcolatoreCombattimento`**: `LocazioneBase`
confronta già `getCarisma()` con un tiro di dado per gli eventi di persuasione/corruzione fuori
combattimento, e `isCorrompibile()`/`isAmichevole()` modellano la stessa area. **Nessuna azione
richiesta qui**: CARISMA resta correttamente assente dai due metodi di combattimento, perché il
suo ruolo (sociale, non di battaglia) è già coperto altrove.

### FORTUNA (in attacco)
La componente difensiva di FORTUNA (contromisura ai Colpi Critici) è ora implementata in
`calcolaDannoRisultante` (vedi sezione 1). Restano non implementate le parti offensive/di survival
del Javadoc: precisione imprevedibile/effetti casuali extra sui colpi, e il "sopravvivere con 1
HP" a un colpo letale. **Proposta:** quest'ultima è più una responsabilità di `subSalute()` che
di questi due metodi, ma può essere segnalata tramite `DannoRisultante` (es. flag `evitaMorte`
impostato in base alla FORTUNA del difensore) che il chiamante controlla dopo aver applicato il
danno.

### FURTIVITA
Javadoc: bonus danno enorme se il bersaglio è ignaro, ignorando l'armatura fisica; in difesa
impedisce di essere agganciati a distanza. **Blocco strutturale:** né `colpisce()` né
`calcolaDannoFinale()` ricevono informazione su "è il primo colpo di un'imboscata?" o "il
bersaglio è consapevole dell'attacco?". Per implementarla servirebbe un parametro aggiuntivo
(es. `boolean sorpresa`) propagato dal chiamante (probabilmente da `Automa`), quindi va trattata
come estensione di firma, non solo di formula.

### PERCEZIONE
Javadoc: in attacco trova punti deboli di boss/corazzati e vede invisibili/nascosti; in difesa
evita imboscate e riduce i malus di ACCECATO/ASSORDATO. **Implementazione (difensiva):**
in `colpisce()`, la penalità di ACCECATO è ora scalata sulla PERCEZIONE dell'attaccante secondo
la formula `penalita = (precisioneTotale/2) * (1 - min(percezione/100, 1.0))`. La PERCEZIONE
consente di "vedere attorno" alla cecità, riducendo il malus fino a eliminarlo con PERCEZIONE 100+.
Coerente col Javadoc: "riduce i malus dello stato ACCECATO".

**Non implementato:** il bonus offensivo contro "bersagli corazzati" in `calcolaDannoRisultante` è
stato valutato e scartato perché introdurrebbe un termine ad-hoc (`percezione - statDifensiva`)
che non ha precedenti nel modello e rischia di snaturare il bilanciamento senza aggiungere valore
narrativo chiaro. Il bonus difensivo è sufficiente per rappresentare il ruolo di PERCEZIONE nel
combattimento.

### SOGGEZIONE
Javadoc: in attacco applica automaticamente SPAVENTATO o riduce il morale nemico; in difesa fa
esitare i nemici a scegliere questo bersaglio (quest'ultimo è un comportamento di IA/targeting,
fuori scope per questi due metodi). **Proposta:** nello step 6 di `calcolaDannoRisultante`
(applicazione stati nativi), aggiungere un secondo tiro indipendente per infliggere SPAVENTATO
basato sulla SOGGEZIONE dell'attaccante, analogo al proc esistente ma non legato al `tipoDanno`.

### CARICO
Javadoc: in attacco consente di usare armi pesanti senza malus di precisione; in difesa consente
di indossare armature pesanti senza malus a VELOCITA/schivata. **Blocco strutturale:** non
risulta, da quanto letto in `Artefatto`/`CalcolatoreCombattimento`, un concetto di "peso" delle
armi/armature confrontabile col CARICO del personaggio (solo `getDanni()`/`getLivello()` sono
usati come proprietà dell'arma). Prima di usare CARICO in combattimento serve estendere il
modello dati degli artefatti con un peso, altrimenti non c'è nulla contro cui confrontarlo.

### CORAGGIO
Javadoc: in attacco permette di colpire senza malus di precisione contro nemici spaventosi/boss;
in difesa dà resistenza nativa contro SPAVENTATO. **Implementazione (difensiva):**
in `colpisce()`, il CORAGGIO del difensore fornisce una resistenza aggiuntiva a SPAVENTATO,
complementare a quella della SAGGEZZA. La penalità di schivata da SPAVENTATO è ora:
`velocitaTotale = velocitaTotale * (9 + min(saggezza/20, 1) + min(coraggio/100, 1)) / 10`
Il CORAGGIO consente di resistere mentalmente alla paura fino a eliminarla completamente con
CORAGGIO 100+. Coerente col Javadoc: "dà resistenza nativa contro SPAVENTATO".

**Non implementato:** il bonus offensivo descritto nel Javadoc ("permettere di colpire senza
malus contro nemici spaventosi") richiederebbe un concetto di "nemico spaventoso" inesistente
oggi (nessun flag o effetto di stato è emesso da un personaggio verso chi lo attacca).

### VALORE (`getQuantitaEffettoDiStato`)
Javadoc: in attacco bonus a danni/precisione contro gruppi nemici numericamente superiori o a
difesa di alleati in fin di vita; in difesa aumenta la mitigazione generale e permette di
intercettare colpi per un alleato. **Blocco parziale:** il bonus "contro nemici in superiorità
numerica" o "in difesa di alleati" richiede contesto di gruppo, non disponibile nella firma
attuale (`colpisce`/`calcolaDannoRisultante` lavorano su una coppia 1:1). La parte "aumenta la
mitigazione generale" è invece implementabile subito: sommare un piccolo termine derivato da
VALORE del difensore a `fattoreMitigazione` in `calcolaDannoRisultante`.

### STANCHEZZA
Javadoc: in attacco riduce precisione e aumenta il costo delle azioni; in difesa penalizza la
schivata e allunga il recupero da STORDITO/ATTERRATO. **Implementazione:**
in `colpisce()`, la STANCHEZZA causa una penalità lineare:
- Attaccante: `precisioneTotale -= stanchezza * 2` (in attacco)
- Difensore: `velocitaTotale -= stanchezza * 2` (in difesa)
Con STANCHEZZA che va da 0-9, la penalità massima è 18 punti per attributo. Coerente col Javadoc
("riduce precisione" e "penalizza la schivata") e segue lo stesso pattern degli effetti di stato
permanenti (CONFUSO, ACCECATO, RALLENTATO, SPAVENTATO).

### NUMERO_BERSAGLI
Non pertinente a questi due metodi: è già usata correttamente altrove
(`getBersagliPerIncantesimo`, `getBersagli` in `PersonaggioBase`) per determinare quanti
avversari un'azione può colpire, non per calcolare riuscita/danno su un singolo bersaglio.
Nessuna azione richiesta qui.

---

## 3) Riepilogo e priorità consigliate

Attributi usati: FORZA, DESTREZZA, COSTITUZIONE, INTELLIGENZA, SAGGEZZA, FORTUNA, CRITICO,
PRECISIONE, VELOCITA, PARATA, RESISTENZA_MAGICA, MAGIA, FURIA — **13 su 22**.

Attributi non usati: SALUTE, CARISMA, FURTIVITA, PERCEZIONE, SOGGEZIONE, CARICO, CORAGGIO,
VALORE, STANCHEZZA — **9 su 22** (CARISMA non è una lacuna: il suo ruolo è già coperto fuori da
questi due metodi; NUMERO_BERSAGLI escluso per lo stesso motivo).

### Attributi già implementati

- **SAGGEZZA**: entrambe le componenti ora implementate — resistenza a CONFUSO/SPAVENTATO in
  `colpisce()`, bonus su danni SACRO e mitigazione da MALEDETTO in `calcolaDannoFinale()`.
- **PERCEZIONE**: riduce la penalità di ACCECATO in `colpisce()` in proporzione al valore
  (fino a eliminarla completamente con PERCEZIONE 100+).
- **CORAGGIO**: resistenza aggiuntiva a SPAVENTATO in `colpisce()`, complementare a quella della
  SAGGEZZA (fino a eliminarla completamente con CORAGGIO 100+).
- **STANCHEZZA**: penalità lineare in `colpisce()` sia per attaccante (su precisione) che per
  difensore (su schivata) — penalità di 2 punti per punto di STANCHEZZA (max 18 punti).
- **BERSERK** (stato speciale): meccanica completa per Guerrieri — applicazione automatica quando
  scendono sotto il 33% di vita con FURIA > 0, bonus danno fisico scalato sulla salute persa,
  rimozione automatica al termine della locazione.
- **FORTUNA** (difensiva): contromisura ai Colpi Critici in `calcolaDannoFinale()`.
- **PARATA**: mitigazione del danno fisico in `calcolaDannoFinale()`.

### Attributi ancora da implementare

Suggerimento di ordine di implementazione, dal più semplice/a basso rischio al più invasivo:

1. **STANCHEZZA** in `colpisce()` — stesso pattern degli effetti di stato già presenti, nessuna
   nuova infrastruttura richiesta.
2. **VALORE** come termine di mitigazione in `calcolaDannoRisultante` — una riga in più nel calcolo
   di `fattoreMitigazione`.
3. **SOGGEZIONE** come proc di SPAVENTATO — richiede decidere se convive con il proc esistente
   basato su `tipoDanno` o lo sostituisce in parte.
4. **FURTIVITA** e **CARICO** — richiedono entrambi estensioni di firma/modello dati
   (rispettivamente un flag di "sorpresa" propagato dal chiamante, e un attributo di peso sugli
   `Artefatto`) prima ancora di poter scrivere la formula: sono gli interventi più invasivi e
   vanno pianificati separatamente.

---

## 4) Caveat su visibilità in gioco

Verificato in `PersonaggioMD`: tutti i campi secondari (FORZA, DESTREZZA, SAGGEZZA, FORTUNA,
PARATA, CRITICO, ecc.) sono semplici `int` senza inizializzazione esplicita. Le classi
personaggio (es. `Guerriero.impostaValori()`) impostano solo SALUTE massima, MAGIA massima,
VALORE, CORAGGIO, CARISMA, e i flag `corrompibile`/`amichevole`: non chiamano `setForza`,
`setDestrezza`, `setSaggezza`, `setFortuna`, `setParata`, `setCritico`, ecc. Questi attributi
partono quindi da 0 per la maggior parte dei personaggi e diventano non nulli solo tramite
modificatori da artefatti (`getModificatore(TipoAttributo)`). Le modifiche descritte in questo
documento sono corrette e coerenti con i Javadoc, ma **non avranno effetto visibile in gioco**
finché `CalcolatoreCombattimento` non verrà collegata al motore live e finché i valori base di
questi attributi non verranno popolati per i personaggi (nessuna di queste due cose è stata
richiesta finora: sono segnalate solo come contesto per valutazioni future).

---

## 5) Proposta di ranghe di attributi iniziali per le classi personaggio

La tabella seguente suggerisce valori iniziali (range min/max) per i 13 attributi ora usati in
`CalcolatoreCombattimento`. I valori rispecchiano gli archetipi fantasy standard e il game design
già implicito nei dati esistenti (SALUTE, MAGIA, VALORE, CORAGGIO max per ogni classe).

**Legenda:**
- Range: valori suggeriti come (minimo, massimo) per la classe; la media tra min e max può
  essere usata come default iniziale o come bersaglio di livellamento.
- Attributi per classe raggruppati per rilevanza (i più alti sono quelli che caratterizzano
  l'archetype).
- Classi femminili e maschili della stessa classe hanno lo stesso profilo di attributi (es.
  Guerriero = Guerriera, Mago = Maga, ecc.).

### Personaggi Giocabili

| Classe | FORZA | DESTR. | COST. | INTEL. | SAGG. | FORT. | CRIT. | PREC. | VEL. | PARATA | RES.M. | MAGIA | FURIA |
|--------|-------|--------|-------|--------|-------|-------|-------|-------|------|--------|--------|-------|-------|
| **Guerriero** (GUERRIERA) | 40–50 | 15–25 | 35–45 | 10–15 | 15–20 | 5–10 | 5–10 | 20–30 | 15–20 | 25–35 | 5–10 | 10–20 | 25–35 |
| **Mago** (MAGA) | 10–15 | 15–20 | 15–20 | 45–55 | 30–40 | 5–10 | 5–10 | 30–40 | 20–30 | 5–10 | 35–45 | 50–60 | 5–10 |
| **Elfo** (ELFA) | 15–25 | 30–40 | 20–30 | 35–45 | 25–35 | 8–12 | 5–10 | 35–45 | 30–40 | 10–15 | 20–30 | 40–50 | 10–15 |
| **Ladro** (LADRA) | 20–30 | 40–50 | 20–30 | 20–30 | 15–25 | 15–25 | 15–25 | 40–50 | 35–45 | 10–15 | 10–15 | 15–25 | 15–25 |
| **Bardo** (CANTASTORIE) | 20–30 | 25–35 | 25–35 | 25–35 | 25–35 | 10–15 | 8–12 | 30–40 | 25–35 | 15–20 | 15–25 | 30–40 | 15–20 |
| **OmbraFiamma** | 35–45 | 35–45 | 30–40 | 40–50 | 35–45 | 20–30 | 20–30 | 40–50 | 35–45 | 20–30 | 35–45 | 60–70 | 30–40 |

### Nemici Comuni

| Classe | FORZA | DESTR. | COST. | INTEL. | SAGG. | FORT. | CRIT. | PREC. | VEL. | PARATA | RES.M. | MAGIA | FURIA |
|--------|-------|--------|-------|--------|-------|-------|-------|-------|------|--------|--------|-------|-------|
| **Folletto** | 2–5 | 5–8 | 2–5 | 1–3 | 2–4 | 1–2 | 1–2 | 5–8 | 8–12 | 1–2 | 1–2 | 1–3 | 3–5 |
| **Arpia** | 5–10 | 10–15 | 3–7 | 5–8 | 3–5 | 2–4 | 2–4 | 12–18 | 15–20 | 2–4 | 2–4 | 3–8 | 8–12 |
| **Goblin** | 8–12 | 8–12 | 8–12 | 2–4 | 3–5 | 3–5 | 3–5 | 10–15 | 10–15 | 5–8 | 2–4 | 1–3 | 10–15 |
| **Chimera** | 15–20 | 12–18 | 12–18 | 3–6 | 4–7 | 3–6 | 5–10 | 15–20 | 15–20 | 8–12 | 2–5 | 2–5 | 18–25 |
| **Centauro** | 18–25 | 15–22 | 18–25 | 8–12 | 12–18 | 5–10 | 5–10 | 20–30 | 25–35 | 10–15 | 5–10 | 5–10 | 15–20 |
| **Minotauro** | 25–35 | 15–22 | 20–30 | 5–10 | 8–12 | 5–10 | 8–12 | 18–25 | 15–20 | 15–25 | 5–10 | 2–5 | 25–35 |
| **Hobgoblin** | 15–20 | 12–18 | 18–25 | 8–12 | 5–10 | 5–10 | 5–10 | 15–20 | 15–20 | 12–18 | 8–12 | 5–10 | 15–20 |
| **Fantasma** | 5–10 | 15–20 | 8–12 | 10–15 | 12–18 | 5–10 | 5–10 | 15–20 | 20–30 | 5–8 | 20–30 | 10–15 | 3–8 |
| **Scheletro** | 12–18 | 8–12 | 12–18 | 3–6 | 2–4 | 3–6 | 5–10 | 12–18 | 10–15 | 10–15 | 2–5 | 1–3 | 10–15 |
| **Troll** | 28–38 | 10–15 | 28–38 | 5–10 | 5–10 | 3–8 | 3–8 | 12–18 | 10–15 | 18–28 | 5–10 | 2–5 | 30–40 |
| **Gigante** | 30–40 | 12–18 | 25–35 | 10–15 | 12–18 | 5–10 | 5–10 | 18–25 | 15–20 | 15–25 | 8–12 | 5–10 | 20–30 |
| **Gargoyle** | 20–28 | 15–20 | 20–28 | 12–18 | 8–12 | 5–10 | 5–10 | 18–25 | 15–20 | 18–25 | 15–20 | 12–18 | 15–20 |
| **Viverna** | 30–40 | 25–35 | 28–38 | 20–30 | 18–28 | 8–15 | 8–15 | 25–35 | 20–30 | 15–25 | 25–35 | 30–40 | 25–35 |
| **OmbraNera** | 25–35 | 25–35 | 25–35 | 20–30 | 20–30 | 10–20 | 10–20 | 30–40 | 30–40 | 15–25 | 20–30 | 30–40 | 20–30 |
| **Spettro** | 3–8 | 20–25 | 5–10 | 12–18 | 25–35 | 8–12 | 5–10 | 18–25 | 25–30 | 3–8 | 30–40 | 5–10 | 5–10 |
| **Spirito** | 2–5 | 18–22 | 3–8 | 10–15 | 30–40 | 5–10 | 3–8 | 15–20 | 28–35 | 2–5 | 35–45 | 3–8 | 3–5 |
| **Titano** | 35–48 | 15–20 | 38–50 | 15–22 | 18–25 | 8–15 | 8–15 | 25–35 | 18–25 | 25–40 | 10–18 | 8–12 | 35–50 |

### Boss

| Classe | FORZA | DESTR. | COST. | INTEL. | SAGG. | FORT. | CRIT. | PREC. | VEL. | PARATA | RES.M. | MAGIA | FURIA |
|--------|-------|--------|-------|--------|-------|-------|-------|-------|------|--------|--------|-------|-------|
| **Idra** | 45–60 | 20–30 | 45–60 | 15–25 | 10–20 | 8–15 | 10–20 | 30–40 | 25–35 | 25–40 | 15–25 | 10–20 | 45–60 |
| **ChimeraDrago** | 35–50 | 25–35 | 35–50 | 18–28 | 15–25 | 10–18 | 12–20 | 28–38 | 25–35 | 20–35 | 20–30 | 18–28 | 35–50 |
| **MinotauroGigante** | 55–70 | 20–30 | 50–65 | 12–20 | 12–20 | 8–15 | 10–18 | 25–35 | 20–30 | 30–45 | 15–25 | 5–15 | 50–65 |
| **Lich** | 15–25 | 25–35 | 28–38 | 55–70 | 40–55 | 10–20 | 8–15 | 35–45 | 30–40 | 15–25 | 50–65 | 100–150 | 15–25 |
| **Strega** | 15–25 | 20–30 | 30–40 | 50–65 | 40–55 | 10–20 | 8–15 | 35–45 | 25–35 | 15–25 | 55–70 | 150–200 | 20–30 |
| **Drago** | 60–80 | 35–50 | 55–75 | 60–80 | 50–70 | 20–35 | 25–40 | 45–60 | 35–50 | 35–55 | 60–80 | 200–300 | 60–80 |
| **Eremita** | 15–25 | 12–20 | 15–25 | 15–25 | 40–55 | 5–12 | 3–8 | 20–30 | 15–25 | 10–18 | 15–25 | 5–15 | 10–20 |

### Note sulla tabella

1. **Interpretazione dei range:** il minimo rappresenta le circostanze "ordinarie" di quella classe;
   il massimo rappresenta il livello di quel personaggio al full potenziamento tramite artefatti
   e sviluppo. Non vanno intesi come "immutabili", ma come punti di riferimento di design.

2. **Scaling per livello:** questi valori sono suggeriti come base (livello 1 / non equipaggiato).
   Un sistema di scaling finale potrebbe applicare moltiplicatori per livello, e/o dare bonus
   progressivi da artefatti/buff, in modo che un Drago a livello 20 sia proporzionalmente più
   letale di un Drago a livello 1. Il modello attuale di `CalcolatoreCombattimento` non ha
   fattori di scaling espliciti per questi attributi, ma usa il `livello` di personaggio come
   fattore nel danno base.

3. **Coerenza con i dati esistenti:** le proporzioni relative tra classi (es. Guerriero ha più
   FORZA di un Mago) mantengono coerenza con i profili SALUTE/MAGIA/VALORE/CORAGGIO già definiti
   in `../src/main/java/com/threeamigos/foresta/motore/Costanti.java`. Ad es., Guerriero ha il massimo SALUTE tra i giocabili (550) → FORZA e
   PARATA alte; Mago ha il massimo MAGIA (70) → INTELLIGENZA altissima, RESISTENZA_MAGICA alta,
   COSTITUZIONE bassa.

4. **Mondi aperti / versioni future:** se il gioco un giorno abilitasse altre meccaniche
   (potenziamenti per missione, scambi tra attributi, debuff permanenti), questi range potranno
   essere aggiustati retroattivamente per mantenere il bilanciamento. Per ora sono una "mappa
   stradale" coerente con lo stato attuale del design.
