# Le tattiche di combattimento: gli internals

Documento tecnico: come funzionano **a livello di codice** le tattiche di combattimento automatiche. Per l'idea, le regole di gioco e le scelte di progetto
vedi [`tattiche_di_combattimento_automatiche.md`](tattiche_di_combattimento_automatiche.md) (qui lo si cita come "il documento di progetto"); qui si
spiega *chi chiama chi*, *che cosa tiene in memoria ognuno*, *dove si tira il dado* e *che cosa toccare* per aggiungere un pezzo.

Tutti i riferimenti di classe sono abbreviati: `…/` è `src/main/java/com/threeamigos/foresta` del modulo indicato.

## 1. Dove sta il codice

Le tattiche attraversano più moduli (la separazione la garantisce il compilatore, vedi `CLAUDE.md`):

| Modulo | Pacchetto | Che cosa contiene |
| :--- | :--- | :--- |
| `foresta-combattimento` | `tipi` | Le azioni: `TipoAzione` (che cosa si può fare in uno scontro, poteri compresi) ed `EsitoAzione` |
| `foresta-tattiche` | `tipi` | Gli altri **enumerati**: `TipoRosa`, `PortataAzione` (su che cosa agisce ogni azione e da quali rose pesca: `PortataAzione.di(azione)`), `TipoFiltro`, `TipoOrdinamento`, `TipoPredicato`, `TipoConfronto`, `CriterioIncantesimo`, `TipoParametro`, e `CostantiTattiche`. Solo nomi, descrizioni e regole di compatibilità; nessuna logica di gioco |
| `foresta-tattiche` | `tattiche` | I **dati** (suffisso `MD`): `TatticaMD`, `VoceMD`, `SelettoreMD`, `FiltroMD`, `OrdinamentoMD`, `PredicatoMD`, `AzioneMD`, e `FormatoTattiche` per il salvataggio. Immutabili, con la lettura e la scrittura |
| `foresta-tattiche` | `eventi.*` | `ComandoImpostaTattica` (UI → motore), `RichiestaAperturaTattiche` (motore → UI), `InternoDecisioneTattica` (la spiegazione di una decisione) |
| `foresta-motore` | `tattiche` | La **logica**: `Valutatore`, `ContestoTattico`, `Decisione`, `RisultatoSelezione`, `EsecutoreTattiche`, `RegistroTattiche`, `TatticheBase` |
| | `tattiche.rose` | `Rosa` e una classe per ogni `TipoRosa`; `Rose` le trova |
| | `tattiche.valutazione` | `Selettori`, `Filtri`, `Ordinamenti`, `Predicati`, `Requisiti`, `SceltaIncantesimo`, `Astuzia` |
| | `combattimento` | `StatoDelCombattimento` (la memoria dello scontro) e `CalcolatoreCombattimento` (i danni attesi) |
| | `tattiche.modifica` | `BozzaTattica` (la tattica in modifica), `TestoTattica` (i testi, da `testo_tattica_it.properties`) |
| `foresta-swing` | `ui.tattiche` | `DisplayableCanvasTattiche` (la schermata), `CasellaADiscesa` (i menu) |

La regola che ne discende: **i dati non sanno niente del motore** (`VoceMD` non può chiedere se un'azione si può fare) e **la UI non vede il motore** (lavora sui dati
`*MD` e sugli enumerati, e parla per eventi). Chi valuta è sempre una classe di `foresta-motore`.

## 2. Il quadro d'insieme

Una tattica è una **lista ordinata di voci**; ogni voce dice *chi* (un selettore), *quando* (condizioni, in AND) e *che cosa* (una sequenza di azioni). Il
valutatore scorre le voci in ordine e la prima eseguibile vince; se nessuna lo è, ripiega sull'attacco fisico al primo avversario in campo.

```
   LocazioneBase (round di combattimento: tocca a un personaggio)
        │  tatticaDi(personaggio): la sua, o la base della classe (RegistroTattiche)
        ▼
   EsecutoreTattiche.agisci(attore, tattica)
        │ 1. ContestoTattico.per(attore, miei, avversari)      ← fotografia dei gruppi
        │ 2. Valutatore.decidi(tattica, contesto)              ← sceglie, non cambia niente
        │       per ogni voce, nell'ordine:
        │         attiva? ─ Predicati.valgonoTutti(condizioni) ─ valutaLaVoce:
        │             Selettori.seleziona(rosa → [azione applicabile] → filtri → ordinamento)
        │             Requisiti.motivoDelNo(azione, …)  per ogni azione della voce
        │             SceltaIncantesimo (se l'azione è un incantesimo "a criterio")
        │       nessuna voce → ripiego (attacco fisico sul primo avversario)
        │ 3. BusEventi.pubblica(InternoDecisioneTattica)       ← la spiegazione, prima dell'azione
        │ 4. esegui(attore, decisione)                         ← attacco, incantesimo, canto, pozione…
        │ 5. StatoDelCombattimento.registraUsoVoce(…)          ← la memoria per i predicati
        ▼
   Decisione  (null se non c'era niente da fare: nessun avversario)
```

Due proprietà da tenere presenti, perché spiegano molte scelte:

- **Chi decide non esegue.** `Valutatore`, `Selettori`, `Filtri`, `Ordinamenti`, `Predicati`, `Requisiti` e `SceltaIncantesimo` non modificano niente. Si possono
  quindi chiamare più volte per la stessa situazione (le simulazioni lo fanno) e il solo effetto collaterale possibile è il **dado**, dove la tattica lo chiede (§10).
- **Chi esegue non decide.** `EsecutoreTattiche` fa quello che la `Decisione` dice, sul modello dati di sempre. È condiviso fra la locazione (`LocazioneBase`) e le
  simulazioni, così i mostri e i personaggi del giocatore fanno le stesse cose in tutti e due.

## 3. Gli enumerati (`foresta-tattiche`, pacchetto `tipi`; `TipoAzione` ed `EsitoAzione` in `foresta-combattimento`)

Il vocabolario delle tattiche è fatto di enumerati, e ogni voce nel file di salvataggio usa il **nome** dell'enumerato.

### 3.1 `TipoAzione` e `PortataAzione`

Ogni azione dichiara la sua **portata**, cioè su che cosa agisce, e quindi come usa il selettore della voce:

| Portata | Selettore | Azioni |
| :--- | :--- | :--- |
| `AVVERSARIO` | sceglie il bersaglio fra gli avversari | `ATTACCO_FISICO`, gli incantesimi di classe (`ARIA`…`SILENZIO`) e `LUCE_DIVINA` (un'abilità del Sacerdote), `INCANTESIMO_DI_DANNO`, `INCANTESIMO_DEL_REPERTORIO`, `DARDO_ARCANO` |
| `ALLEATO` | sceglie il bersaglio fra gli alleati (anche chi decide) | `CANTO_CURATIVO`, le quattro pozioni |
| `ALLEATO_CADUTO` | sceglie il bersaglio fra i caduti | `RESURREZIONE` |
| `GRUPPO_ALLEATI` | è solo un test di esistenza | `ALBA_SACRA`, `CANTO_DI_VALORE` |
| `GRUPPO_AVVERSARI` | è solo un test di esistenza | `CANTO_IPNOTICO`, `URLO_DELLA_BANSHEE` |
| `ARMA` | sempre `SE_STESSO`: dice chi cambia arma | `CAMBIA_ARMA` |

Tutte le azioni di una **stessa voce** devono avere la stessa portata (lo controlla `VoceMD.problemi`). `TipoAzione.roseAmmesse(attoreDelGiocatore)` dice da
quali rose può pescare il bersaglio: per esempio un attacco dei miei non colpisce gli ospiti, un attacco di un mostro sì. Le rose sono dati della
portata (`PortataAzione.roseAmmesse`, due insiemi per costante: per i miei e per i mostri).

`TipoAzione.di(ClasseIncantesimo)` e `getClasseIncantesimo()` fanno da ponte fra l'azione e l'incantesimo (`di(null)` dà null). `INCANTESIMO_DI_DANNO` e `INCANTESIMO_DEL_REPERTORIO`
non hanno una classe fissa: sono due azioni "a criterio" che il valutatore trasforma in quella dell'incantesimo scelto (§8).

### 3.2 `TipoRosa`

Sette rose, sempre **relative all'attore**: "alleati" e "avversari" sono i suoi, quindi per un mostro gli avversari sono i miei personaggi.

| Rosa | Contenuto | Vale per |
| :--- | :--- | :--- |
| `SE_STESSO` | il solo attore | tutti |
| `ALLEATI` | i compagni vivi e in campo, attore compreso | tutti |
| `ALLEATI_CADUTI` | i compagni caduti (non in panchina) e curabili: per la Resurrezione | tutti |
| `AVVERSARI` | i vivi e in campo dell'altro gruppo | tutti |
| `OSPITI` | gli ospiti vulnerabili vivi del gruppo del giocatore | tutti |
| `ALLEATI_E_OSPITI` | alleati poi ospiti, tolti i non curabili | solo i miei |
| `AVVERSARI_E_OSPITI` | avversari poi ospiti | solo i mostri |

`TipoRosa.valePer(boolean)` dice per quale lato una rosa ha senso; le rose `ALLEATI_E_OSPITI` e `AVVERSARI_E_OSPITI` chiamano `Rosa.verificaLato` e lanciano
`IllegalStateException` se usate dal lato sbagliato (un errore della tattica, che la lettura avrebbe dovuto rifiutare).

### 3.3 Filtri, ordinamenti, predicati

Ognuno dichiara il **tipo del suo parametro** (`TipoParametro`): nessuno, tipoConfronto, tipoConfronto e numero, stato, classe, categoria, tipo di danno, numero, oppure
la forma composta di `QUANTI` (rosa, filtri, tipoConfronto e numero). Lo stesso `TipoParametro` serve a tre cose: le fabbriche dei dati controllano che il parametro
sia quello giusto, la lettura del salvataggio sa quale campo guardare, la UI sa quale editor mostrare.

`TipoConfronto` ha tre valori (`MINORE`, `UGUALE`, `MAGGIORE`); "almeno" e "al massimo" si ottengono con la **negazione** (`NON MINORE`, `NON MAGGIORE`).

## 4. I dati (`foresta-modellodati`)

```
TatticaMD ── lista di ──► VoceMD ──┬── SelettoreMD ──┬── TipoRosa
 (VUOTA, disabilitata)             │                 ├── lista di FiltroMD
                                   │                 └── OrdinamentoMD
                                   ├── lista di PredicatoMD   (le condizioni, in AND)
                                   └── lista di AzioneMD      (la prima applicabile vince)
```

Tutti i `*MD` sono **immutabili** e hanno gli stessi cinque tratti:

1. **Costruttore privato e fabbriche statiche** che controllano il parametro (`FiltroMD.conConfrontoENumero`, `PredicatoMD.probabilita`, `AzioneMD.cambiaArma`…). Un dato
   incoerente non esiste: `IllegalArgumentException` o `NullPointerException` alla costruzione.
2. **"Modificatori" che restituiscono una copia**: `FiltroMD.negato()`, `PredicatoMD.negato()`, `VoceMD.disattivata()`, `AzioneMD.soloSePiuForteDellAttacco()`,
   `TatticaMD.disabilitata()`. È il motivo per cui la UI non può modificarli e ha una bozza (§16).
3. `salva(PrintWriter)` e `leggi(BufferedReader)`, che passano dalle **stesse fabbriche**: un salvataggio incoerente è rifiutato.
4. `equals`/`hashCode` per valore (la UI li usa per capire se la bozza è cambiata).
5. `toString` leggibile, per il log e per la spiegazione delle decisioni.

Punti non ovvi:

- **`VoceMD` senza selettore.** Il selettore può essere `null`: vale allora quello di base della portata (`VoceMD.selettoreDiBase`): avversari/primo per un attacco o
  un'azione sul gruppo avversario, alleati/primo per un'azione di gruppo alleato, i caduti per la Resurrezione, `SE_STESSO` per il resto. Chi legge un selettore deve
  sempre usare `selettoreEffettivo()`.
- **`VoceMD.problemi(boolean attoreDelGiocatore)`** elenca gli errori di una voce per un lato: azioni di portate diverse, una rosa che non vale per il lato, una rosa che
  un'azione non accetta. Vuota se la voce è a posto. `TatticaMD.problemi` le raccoglie con il numero della voce (da 1).
- **`TatticaMD.VUOTA`** non ha voci: vale "attacca il primo avversario" (il ripiego). È la tattica di un personaggio del giocatore finché non ne scrive una (nel gioco, in
  realtà, riceve la predefinita della sua classe: §13.1).
- **`TatticaMD.disabilitata`** è un flag, non una voce: una tattica letta da un salvataggio con errori non fa fallire il caricamento, si **disabilita** e il giocatore la
  corregge nella schermata. `LocazioneBase.tatticaDi` la tratta come se non ci fosse.
- **`problemiPerQualsiasiLato`**: la lettura non sa se la tattica è di un mio personaggio o di un mostro, quindi la considera a posto se lo è per *almeno uno* dei due lati
  (una rosa `AVVERSARI_E_OSPITI` è un errore per i miei ma non per un mostro).

## 5. Il contesto: `ContestoTattico`

È la **fotografia di una decisione**: tutto ciò che rose, filtri, predicati e ordinamenti devono poter *leggere*. Si crea per ogni decisione di un personaggio (da
`ContestoTattico.per(attore, giocatore, mostri)`) e si butta dopo.

Cosa porta:

| Campo | Significato |
| :--- | :--- |
| `attore`, `attoreDelGiocatore` | chi decide e di che lato è (si ricava cercandolo nel gruppo del giocatore) |
| `alleati`, `avversari` | **tutti** i membri dei due gruppi, anche caduti o in panchina (a filtrare sono le rose); il primo è il capo |
| `ospitiVulnerabili` | gli ospiti vulnerabili vivi del gruppo del giocatore (anche per un mostro, che li può scegliere come bersagli) |
| `capoDegliAlleati`, `capoDegliAvversari` | per il filtro `IL_CAPO` |
| `stato` | la memoria dello scontro (`StatoDelCombattimento`, un singleton: il contesto la porta con sé per poterla sostituire nei test) |
| `gruppoDegliAlleati` | per leggere le scorte (pergamene e pozioni); senza, il gruppo non ha scorte |
| `azioneInValutazione`, `identificativoVoce` | **cambiano mentre si scorre la lista**; `null` fuori da una voce |

Il contesto è **immutabile**: `conAzione`, `conVoce`, `conStato` e `conGruppoDegliAlleati` restituiscono un contesto nuovo con quel solo campo cambiato. Servono perché:

- `conVoce(String)` — i predicati `VOCE_USATA` e `ROUND_DALL_ULTIMO_USO` devono sapere *quale* voce stanno valutando. L'identificativo è la **posizione** della voce nella tattica
  (`String.valueOf(i)`), non il nome: rinominare una voce non azzera la sua storia, ma spostarla o toglierne una sì (la storia è però dello scontro in corso, che non si salva).
- `conAzione(TipoAzione)` — il filtro `DEBOLE_ALL_AZIONE` dipende dall'azione che *si farebbe* su quel candidato (§6.3).

`isMostro(personaggio)` serve al filtro `MAGICO` e a `Requisiti.puoLanciare`/`SceltaIncantesimo` (i mostri hanno un repertorio e non hanno scorte); `isOspite(personaggio)` ai filtri che escludono gli ospiti.

## 6. Scegliere il bersaglio: i selettori

`Selettori.seleziona(selettore, contesto[, ammissione])` restituisce un `RisultatoSelezione` (scelto, rimasti, passi). Quattro fasi (la seconda facoltativa), sempre in questo ordine:

1. **La rosa** (`Rose.di(tipo).candidati(contesto)`): i candidati, nell'ordine del gruppo (così "il primo" è ripetibile). Tutte tranne `ALLEATI_CADUTI` escludono chi è fuori
   combattimento (`Rosa.inCampo`).
2. **L'ammissione** (facoltativa): restano solo i candidati su cui *almeno un'azione della voce si può fare*. Per ognuno si ricorda *quale* azione (una `IdentityHashMap`:
   per identità, non per `equals`, perché due mostri della stessa specie sono due candidati distinti).
3. **I filtri**, in AND, uno dopo l'altro: ognuno vede solo i sopravvissuti del precedente. Il contesto passato a un filtro porta l'azione che si farebbe su *quel* candidato.
4. **L'ordinamento** sceglie uno fra i rimasti.

### 6.1 Perché l'ammissione

Senza di lei, un ordinamento come `MENO_SALUTE` potrebbe cadere su un candidato su cui nessuna azione della voce è possibile (un immune scoperto, una cura su chi è già in
salute piena), e la voce verrebbe scartata *anche se c'era un altro candidato adatto*. Con l'ammissione quel candidato non entra nemmeno. `Valutatore` la passa sempre
(`primaAzioneApplicabile`); `Predicati.QUANTI` no.

### 6.2 `RisultatoSelezione`: "rimasti" e "scelto" sono due cose

`isVuoto()` è vero se `scelto == null`. Ma `scelto` può essere `null` anche con dei rimasti: gli ordinamenti `ULTIMO_ATTACCANTE` e `ULTIMO_SCELTO` vogliono *un personaggio
preciso* e, se non è fra i rimasti, non scelgono nessuno. Per questo:

- una voce **su un bersaglio** guarda `isVuoto()` (nessuno scelto: la voce non è eseguibile);
- una voce **su un gruppo** e il predicato `QUANTI` guardano `getRimasti()` (conta che ci sia qualcuno, non chi sceglierebbe l'ordinamento).

`passi` è la cronologia ("Avversari: 3 [A, B, C]", "SALUTE_PERCENTUALE MINORE 50: 1 [B]", "MENO_SALUTE (B 12): B"), che finisce nella spiegazione.

### 6.3 I filtri (`Filtri`)

Una mappa `EnumMap<TipoFiltro, Verifica>` con una risposta per tipo (un test controlla che non ne manchi nessuna: `haLaSuaRisposta`). `ammette(filtro, candidato, contesto)`
applica la risposta e poi la **negazione** con un `!=` fra booleani (uno XOR). I filtri non cambiano niente e non tirano il dado.

Quelli che meritano attenzione:

- `CLASSE`, `CATEGORIA`, `MAGICO` **non comprendono mai gli ospiti** (non hanno una classe da combattente).
- `CLASSE` confronta il **ruolo** (`stessoRuoloDi`): Mago e Maga sono la stessa cosa.
- `SALUTE_PERCENTUALE` evita la divisione per zero con una salute massima nulla.
- `DEBOLE_A` / `DEBOLE_ALL_AZIONE`: "debole" vuol dire moltiplicatore di classe e di stati **maggiore di 1**, senza contare la difesa dell'equipaggiamento
  (`CalcolatoreCombattimento.moltiplicatoreDelleInterazioni`). `DEBOLE_ALL_AZIONE` legge il tipo di danno dall'azione del contesto, e fuori da una voce o per un'azione
  senza tipo di danno fisso (l'attacco dipende dall'arma) è falso.
- `IMMUNE_A` usa `isImmuneATipoDanno`, che comprende anche gli artefatti.
- `MAGICO` (`Filtri.isMagico`) è il più articolato: *non silenziato* e poi, per una classe con un'abilità innata (Dardo arcano, Alba sacra, Luce divina, canti) conta quella;
  per un mostro conta `isMagico() && getMagia() > 0`; per gli altri conta aver già lanciato un incantesimo nello scontro.
- `SCELTO_L_ULTIMA_VOLTA` guarda sia l'ultimo avversario sia l'ultimo alleato scelti; `MI_HA_ATTACCATO_PER_ULTIMO` solo l'ultimo attaccante.
- `STATO_NEGATIVO`: qualunque stato che non sia benefico (`TipoEffettoDiStato.isBenefico()`: ISPIRATO e BERSERK non contano).

### 6.4 Gli ordinamenti (`Ordinamenti`)

`scegli(candidati, ordinamento, contesto)`:

| Ordinamento | Come sceglie |
| :--- | :--- |
| `PRIMO` | il primo della lista |
| `A_CASO` | `Dado.tiraAncheAUnaFaccia(n)`: l'unico che tira il dado |
| `ULTIMO_ATTACCANTE`, `ULTIMO_SCELTO` | solo quel personaggio, se è fra i candidati; altrimenti nessuno |
| tutti gli altri | quello con la **misura più alta**; a parità il primo (il tipoConfronto è "maggiore" stretto) |

Le misure (`Ordinamenti.misura`): i "meno" sono i "più" col segno cambiato (`MENO_SALUTE` = `-salute`, così si massimizza sempre). `PIU_DEBOLE_A` e `PIU_RESISTENTE_A` usano
`CalcolatoreCombattimento.moltiplicatoreAtteso`, che — a differenza del filtro `DEBOLE_A` — comprende **anche la difesa dell'equipaggiamento**; un immune vale 0, quindi
è il "più resistente". `descriviLeMisure` produce la stringa per la spiegazione ("Mira 12, Orfeo 4"), mostrando il valore vero e non l'opposto.

## 7. Le condizioni e i requisiti

Sono due cose diverse, e confonderle è l'errore più facile:

|  | **Condizioni** (`Predicati`) | **Requisiti** (`Requisiti`) |
| :--- | :--- | :--- |
| Chi li scrive | il giocatore (o `TatticheBase`) | nessuno: li porta l'azione |
| Di che cosa parlano | dello scontro e di chi decide (round, probabilità, quanti candidati) | di se l'azione è *possibile* (pergamena, MAGIA, pozione, immunità) |
| Dove si valutano | prima del selettore, una volta per voce | per ogni azione, su ogni candidato (ammissione) e di nuovo sul bersaglio scelto |
| Cosa danno | vero/falso | `null` se si può, altrimenti il **motivo** del no (per la spiegazione) |

### 7.1 Le condizioni (`Predicati`)

Come i filtri: `EnumMap<TipoPredicato, Verifica>`, negazione con `!=`, una risposta per tipo (controllata da un test). `valgonoTutti` è l'AND; **nessuna condizione vale sempre**.

| Predicato | Come si valuta |
| :--- | :--- |
| `QUANTI` | costruisce al volo un selettore (rosa + filtri, ordinamento `PRIMO`) e confronta con il numero quanti **rimasti** ci sono |
| `ROUND` | `stato.getRound()` |
| `ASTUZIA` | `Astuzia.di(attore)` (§13.3) |
| `INTELLIGENZA` | l'attributo attuale dell'attore: **non è l'astuzia** (le soglie dei mostri di prima delle tattiche, 5 e 7, sono sull'INTELLIGENZA) |
| `PROBABILITA` | `Dado.tira(100) <= numero`: l'unico predicato che tira il dado, e lo ritira a ogni valutazione |
| `VOCE_USATA` | quante volte l'attore ha usato la voce nello scontro (`stato.getUsiDellaVoce`) |
| `ROUND_DALL_ULTIMO_USO` | `round − roundUltimoUso`; se la voce non è mai stata usata, `Integer.MAX_VALUE` (l'attesa di un'abilità mai usata è finita) |

I due predicati sulla voce chiamano `voce(contesto)`, che lancia `IllegalStateException` se il contesto non ha l'identificativo (qualcuno ha dimenticato `conVoce`).

### 7.2 I requisiti (`Requisiti.motivoDelNo`)

Due blocchi, nell'ordine; il primo che fallisce dà il motivo.

**Comuni a tutte le azioni su un singolo bersaglio** (portata `AVVERSARIO` o `ALLEATO`, con bersaglio non nullo):

1. **Immunità scoperta**: `stato.isImmunitaScoperta(bersaglio, tipo)` — conoscenza pubblica, vale per tutti.
2. **Troppi fallimenti**: `getTentativiFalliti(...) >= MAX_TENTATIVI_FALLITI_PER_AZIONE` (oggi 2). Fa eccezione `ATTACCO_FISICO`, che è il ripiego e "non fallisce mai".
3. **Non morto non curabile**: per le azioni di cura (`CANTO_CURATIVO`, le pozioni di salute, `RESURREZIONE`) un bersaglio non `isCurabile()` non è mai valido.

**Propri dell'azione**:

| Azione | Si può fare se… |
| :--- | :--- |
| `ATTACCO_FISICO` | c'è un bersaglio in campo |
| `INCANTESIMO_DI_DANNO`, `…_DEL_REPERTORIO` | `SceltaIncantesimo.scegli(...)` non è `null` |
| `DARDO_ARCANO` | `AbilitaInnate.puoUsare(attore, TipoAbilita.DARDO_ARCANO)` (classe, MAGIA, silenzio) e `contro(...)` |
| `RESURREZIONE` | si può lanciare (pergamena e MAGIA) e il bersaglio non è vivo né in panchina |
| `ALBA_SACRA` | abilità innata del Sacerdote (`AbilitaInnate.puoUsare`) e **qualcuno** degli alleati ha uno stato negativo |
| `CANTO_CURATIVO` | `CantoDelBardo.puoCantare` e il bersaglio è vivo e ferito |
| `CANTO_IPNOTICO`, `URLO_DELLA_BANSHEE` | si può cantare e c'è un avversario in campo né spaventato né confuso |
| `CANTO_DI_VALORE` | si può cantare e c'è un alleato in campo non ancora `ISPIRATO` |
| le quattro pozioni | il gruppo ne ha, e il bersaglio è ferito (salute) o non ha la MAGIA piena (magia) |
| `CAMBIA_ARMA` | il gruppo è un `GruppoGiocatore` e `armiPerCambio(attore, tipoDanno)` non è vuoto |
| gli incantesimi di classe | `puoLanciare` (§7.3), bersaglio in campo, non immune all'incantesimo, `contro(...)`, e per il Silenzio non già silenziato né immune; con `soloSePiuForteDellAttacco`, rende più delle armi |

`contro(...)` aggiunge due controlli: l'immunità di classe al *tipo di danno* e, per il solo dardo con `soloSePiuForteDellAttacco`, il tipoConfronto con le armi.

### 7.3 `puoLanciare`

L'attore può lanciare una classe di incantesimo se `attore.puoFormulare(classe)` (non silenziato, ha la MAGIA) **e** vale una di queste: il gruppo ha una pergamena
(`contesto.getPergamene`); è la Luce divina e l'attore la ha innata; è un mostro magico con quell'incantesimo nel repertorio (`getRepertorioIncantesimi`). I mostri non
hanno scorte, quindi per loro conta solo il repertorio.

### 7.4 La memoria che i requisiti leggono

Le immunità e le resistenze **di classe** si conoscono subito; quelle date da un **artefatto** si conoscono solo se sono state *scoperte* (`registraEsitoAzione` con
`EsitoAzione.IMMUNE`). La scoperta è pubblica: la fa uno e la sanno tutti.

## 8. Scegliere l'incantesimo: `SceltaIncantesimo`

Le azioni `INCANTESIMO_DI_DANNO` e `INCANTESIMO_DEL_REPERTORIO` non nominano un incantesimo: lo sceglie questa classe, con un `CriterioIncantesimo`.

- **Fra quali**: `ELEMENTALI` (aria, acqua, terra, fuoco, fulmine, gelo, veleno) per `INCANTESIMO_DI_DANNO`; tutti i malefici (anche Morte e Silenzio) per
  `INCANTESIMO_DEL_REPERTORIO`, che replica come scelgono i mostri oggi. L'ordine delle liste conta: a parità vince il primo.
- **I lanciabili** (`lanciabiliContro`): quelli che l'attore può lanciare (`Requisiti.puoLanciare`), a cui il bersaglio non è immune (incantesimo, tipo di danno, scoperta),
  non già falliti troppe volte; il Silenzio solo su chi non è già silenziato.
- **Il criterio** (`secondoIlCriterio`):

| Criterio | Misura che massimizza |
| :--- | :--- |
| `A_CASO` | nessuna: `Dado.tiraAncheAUnaFaccia` |
| `PIU_POTENTE` | il danno **base** dell'incantesimo, senza debolezze né resistenze |
| `PIU_ADATTO` | il danno **atteso** su tutti i bersagli colpiti (debolezze e resistenze di classe e di stati) |
| `PIU_ADATTO_ESPERTO` | il danno atteso per il **fattore dell'equipaggiamento osservato**; con più bersagli, la somma della quota di salute tolta a ciascuno (al massimo 1: portare vicino alla fine pesa quanto uccidere) |
| `PIU_ECONOMICO_CHE_BASTA` | fra quelli che bastano a uccidere il bersaglio, il meno caro (pergamena per i miei, costo di lancio in MAGIA per i mostri); se nessuno basta, `PIU_ADATTO_ESPERTO` |

- **`soloSePiuForteDellAttacco`**: dopo la scelta, se il danno atteso dell'incantesimo scelto non supera `CalcolatoreCombattimento.dannoAttesoDellaMischia`, `scegli` restituisce
  `null` e l'azione non è applicabile. Il tipoConfronto è fatto **solo sull'incantesimo scelto**: se il migliore secondo il criterio non batte le armi non si prova con gli altri.
- **`bersagli(...)`**: il bersaglio principale e, per un incantesimo `MULTIPLO` lanciato da un personaggio del giocatore, i successivi avversari vivi fino a `getBersagli()`.
  Un mostro colpisce sempre solo il bersaglio scelto, anche con un incantesimo a più bersagli.
- **`descrivi(...)`** produce la riga di spiegazione ("PIU_ADATTO: FUOCO 4.2, FULMINE 9.8; attacco con le armi 6.0").

Tutti i danni sono **attesi** (probabilità di colpire per danno atteso per fattore di danno), non tirati: la scelta è ripetibile per la stessa situazione, tranne `A_CASO`.

## 9. Il valutatore: `Valutatore.decidi`

```
decidi(tattica, contesto):
    per ogni voce i, nell'ordine:
        se non è attiva                          → spiegazione "disattivata"; avanti
        perLaVoce = contesto.conVoce(i)
        se non valgono tutte le condizioni       → spiegazione "le condizioni non sono tutte vere"; avanti
        d = valutaLaVoce(voce, i, perLaVoce)
        se d != null                             → return d
    return ripiego(contesto)
```

### 9.1 `valutaLaVoce`

Due strade, secondo la portata:

- **Azione su un gruppo** (`GRUPPO_ALLEATI`, `GRUPPO_AVVERSARI`): il selettore è un test di esistenza. Se i **rimasti** sono vuoti la voce è scartata. Poi si prova ogni azione con
  `Requisiti.motivoDelNo(azione, attore, null, contesto)` e la prima che passa vince, con bersaglio `null`.
- **Azione su un bersaglio**: `Selettori.seleziona` con l'ammissione `primaAzioneApplicabile`. Se non scelto nessuno, la voce è scartata. Altrimenti si riprovano le azioni **sul
  bersaglio scelto** e la prima che passa i requisiti diventa la decisione (`decisioneSu`).

`decisioneSu` trasforma le azioni a criterio: per `INCANTESIMO_DI_DANNO`/`…_DEL_REPERTORIO` chiama `SceltaIncantesimo.scegli` e il tipo eseguito diventa quello dell'incantesimo
scelto (`TipoAzione.di(classe)`). Per `CAMBIA_ARMA` il bersaglio della decisione è `null` (il "bersaglio" era l'attore stesso, solo per il selettore).

> **Una sottigliezza.** I requisiti hanno già chiamato `SceltaIncantesimo.scegli` per sapere se c'era qualcosa da lanciare, e `decisioneSu` lo richiama per sceglierlo davvero.
> Con `A_CASO` ogni chiamata ritira il dado, quindi l'incantesimo "controllato" e quello lanciato possono differire (sono entrambi lanciabili). Con `A_CASO` e
> `soloSePiuForteDellAttacco` insieme la seconda chiamata può dare `null`: `decisioneSu` restituisce allora `null`, l'azione è scartata (con il motivo nella spiegazione)
> e `valutaLaVoce` passa all'azione successiva della voce. Senza questo controllo `TipoAzione.di(null)` avrebbe trovato `ATTACCO_FISICO`, la prima azione senza classe di incantesimo.

### 9.2 `Decisione`

Dati: `indiceVoce` (-1 per il ripiego), l'`AzioneMD` scritta nella voce, il `TipoAzione` **eseguito**, la `TipoIncantesimo` (se è un incantesimo), il bersaglio (`null` per i gruppi e il cambio
d'arma) e la spiegazione. `getIdentificativoVoce()` è `String.valueOf(indiceVoce)`, `null` per il ripiego: è la chiave con cui lo stato ricorda gli usi.

### 9.3 Il ripiego

Nessuna voce eseguibile: `Rose.di(AVVERSARI).candidati` e, se ce n'è uno, attacco fisico sul **primo**. Se non c'è nessun avversario in campo il valutatore restituisce `null` e
`EsecutoreTattiche.agisci` non fa niente (chi chiama controlla se lo scontro è finito). Il ripiego non ha identificativo: non conta come uso di una voce.

## 10. Il caso: dove si tira il dado

Tutto il caso passa da `strumenti.dado.Dado` (l'unica sorgente, che in modalità di prova si può truccare). Nelle tattiche si tira in **tre soli posti**:

| Dove | Cosa |
| :--- | :--- |
| `Predicati.PROBABILITA` | `Dado.tira(100)`, a ogni valutazione della condizione |
| `Ordinamenti.A_CASO` | `Dado.tiraAncheAUnaFaccia(n)` sui candidati |
| `SceltaIncantesimo` con `A_CASO` | `Dado.tiraAncheAUnaFaccia(n)` sui lanciabili |

Il resto (per colpire, per il danno) è nell'esecuzione, in `CalcolatoreCombattimento`. Conseguenza pratica: per riprodurre una decisione bastano la tattica, il contesto e i tre
tiri; la spiegazione registra il percorso, non i tiri.

## 11. Eseguire: `EsecutoreTattiche`

`agisci(attore, tattica)`: costruisce il contesto, chiede la decisione, **pubblica `InternoDecisioneTattica`**, esegue, e se la decisione non è un ripiego registra l'uso della voce
(`stato.registraUsoVoce`) **dopo** l'esecuzione, così durante l'azione la voce risulta ancora non usata in questo round. L'evento arriva *prima* dell'azione: la UI scrive l'azione
sotto il personaggio prima che accada (`ForestaUI.gestisciEventoDecisioneTattica`).

`esegui(attore, decisione)` ricava il lato (`miei.contiene(attore)`) e fa uno `switch` sul tipo eseguito:

| Tipo eseguito | Cosa fa | Cosa ricorda nello stato |
| :--- | :--- | :--- |
| `ATTACCO_FISICO` | un mio: `attaccaConLeArmi` di questa classe; un mostro: `AttaccoConLeArmi.esegui(attore, bersaglio)` | i colpi, via `ApplicazioneDelRisultato.applica` → `registraColpo` |
| `DARDO_ARCANO` | `usaUnAbilita` (il dardo è un'abilità di danno, `ColpoMagico`): annuncio, tiro per colpire, danno, costo in MAGIA | esito (`RIUSCITA`/`MANCATA`), incantesimo lanciato, uccisioni |
| `RESURREZIONE` | formula l'incantesimo e consuma la pergamena | — |
| `ALBA_SACRA` | `usaUnAbilita`: toglie stati negativi e modificatori temporanei al gruppo | incantesimo lanciato |
| i quattro canti | `canta(...)` → `FabbricaCanti.crea(tipo, cantore).cantaSu(...)` | incantesimo lanciato, solo se il canto ha avuto luogo |
| le quattro pozioni | `gruppo.consuma…(bersaglio)` | l'alleato scelto (`registraAlleato`) |
| `CAMBIA_ARMA` | prende la prima arma di `armiPerCambio`; solo con un `GruppoGiocatore` | — |
| tutto il resto | `lanciaUnIncantesimoDiDanno`: crea l'incantesimo con `FabbricaIncantesimi`, lo formula (su un bersaglio, o su un `GruppoDiBersagli` se `MULTIPLO`) e consuma la pergamena | l'esito, scritto da `IncantesimoMaleficoImpl.colpisci` |

Note:

- **Gli uccisi** dei miei personaggi vanno a un `RegistroUccisioni` (in gioco, `LocazioneBase.registraUccisione`: esperienza e punti). Quelli dei mostri no.
- **L'attacco dei miei** ripete il bersaglio per ogni arma; se la prima ha ucciso il bersaglio, il colpo successivo passa al **primo avversario ancora in piedi**. Se non ce ne sono, il
  turno finisce.
- **Dove si scrive l'esito di un'azione.** Per gli incantesimi di classe lo scrive `IncantesimoMaleficoImpl.colpisci` (`IMMUNE`, `MANCATA`, `RIUSCITA`); per le abilità di danno (radici, sciame,
  dardo arcano) lo scrive `ColpoMagico.colpisci`, lo stesso metodo che usano gli incantesimi. Gli esiti alimentano i requisiti (§7.2).
- `annuncia(attore, bersaglio)` pubblica `InternoAzioneInCombattimento`, con cui il riquadro del combattimento mostra chi agisce e su chi. Le abilità innate e i
  canti non passano di qui: si annunciano da sole (`AnnuncioDellAzione.azione(attore, bersaglio, azione)`) appena sanno che agiranno, prima dell'effetto, con
  il bersaglio se è uno solo (anche un compagno) e senza se sono più di uno; l'evento porta il `TipoAzione`, così il riquadro scrive "Impone le mani su X".

## 12. La memoria dello scontro: `StatoDelCombattimento`

Non fa parte del modello dati: **non si salva** (un combattimento non si interrompe per salvare) e si azzera a ogni inizio locazione (`reimposta`). È un singleton. Per le tattiche
interessa la **scheda** di ogni personaggio (una `IdentityHashMap`, non dimenticata quando cade: un compagno potrebbe farlo risorgere):

| Scheda | Chi la scrive | Chi la legge |
| :--- | :--- | :--- |
| `ultimoAvversario`, `ultimoAttaccante` | `registraColpo` (da `ApplicazioneDelRisultato.applica`) | `ULTIMO_SCELTO`, `ULTIMO_ATTACCANTE`, `SCELTO_L_ULTIMA_VOLTA`, `MI_HA_ATTACCATO_PER_ULTIMO` |
| `ultimoAlleato` | `registraAlleato` (pozioni, `EsecutoreTattiche`) | `ULTIMO_SCELTO`, `SCELTO_L_ULTIMA_VOLTA` |
| `haLanciato` | `registraIncantesimoLanciato` | `MAGICO` (per chi non ha poteri innati) |
| `vociUsate` / `usiDelleVoci` | `registraUsoVoce` | `ROUND_DALL_ULTIMO_USO`, `VOCE_USATA` |
| `tentativiFalliti` | `registraEsitoAzione` (`MANCATA`) | `Requisiti`, `SceltaIncantesimo` |

Più due mappe di **pubblico dominio**, non legate a nessuno: le **immunità scoperte** (`IMMUNE`) e le **reazioni note** (di quanto l'equipaggiamento di un personaggio ha cambiato il
danno di un tipo: serve a `PIU_ADATTO_ESPERTO` e `PIU_ECONOMICO_CHE_BASTA`).

Attenzione: gli **usi delle voci** sono indicizzati per posizione. Lo stato non sa che le voci sono cambiate, ma la tattica non si cambia in combattimento (§15.2).

## 13. Da dove vengono le tattiche

### 13.1 `RegistroTattiche`

`LocazioneBase.tatticaDi(personaggio)` sceglie la tattica da usare:

1. la tattica del personaggio, se ce l'ha e **non è disabilitata**;
2. altrimenti, per un mio personaggio, `RegistroTattiche.predefinitaDelGiocabile(classe)`; per un mostro `RegistroTattiche.perClasse(classe)`.

`perClasse` cerca, nell'ordine: una tattica registrata a mano (`registra`, per le simulazioni e i test) → `TatticheBase.deiMostri(classe)` (costruita alla prima richiesta e ricordata) →
la **tattica di oggi** (`tatticaDiOggi`), la replica voce per voce di come i mostri sceglievano prima delle tattiche. Non è mai `null`.

`azioniPossibili(classe)` dice quali azioni la schermata offre a una classe: tutte tranne `INCANTESIMO_DEL_REPERTORIO`, i canti (se non è un bardo), il dardo (se non lo conosce), l'Alba
sacra (se non la conosce).

### 13.2 `TatticheBase`

Sono **dati scritti nel codice**, con le stesse voci che il giocatore scrive nella schermata. Per i mostri è una lista sola per classe divisa in **fasce di astuzia**:

| Fascia | Soglia | Comportamento |
| :--- | :--- | :--- |
| Bruto | sotto 12 | istinto: si vendica di chi l'ha colpito (bestie, troll, minotauri e goblin, nel 50% dei casi), altrimenti a caso, ospiti compresi |
| Astuto | da 12 | finisce chi è sotto il 30%, cerca un incantatore, poi il più ferito; magia solo se rende più delle armi |
| Esperto | da 20 | colpo più economico che basta sui feriti, Silenzio sugli incantatori, incantesimo più adatto |
| Stratega | da 31 | Colpo di Morte sul quasi caduto, Silenzio sull'incantatore con più magia, incantesimo più adatto conoscendo l'equipaggiamento |

Le fasce si aggiungono **dalla più alta alla più bassa**, e ognuna (tranne la bruta) comincia con la condizione `NON ASTUZIA MINORE soglia`. Per questo la lista funziona a scalare: un
mostro stratega prova per prime le voci da stratega, e se nessuna si può eseguire scende a quelle da esperto, e così via. Un mostro entra in una fascia solo se la sua **astuzia
massima** (`ASTUZIA_MASSIMA`, l'intelligenza massima della classe) raggiunge la soglia.

I tre elenchi `MAGICI`, `CON_SILENZIO`, `CON_MORTE` sono scritti a mano e vanno tenuti allineati con `PersonaggioBase.getRepertorioIncantesimi` (c'è un `TODO`).

Per i giocabili, `deiGiocabili(classe)` guarda il **ruolo** (Mago e Maga hanno la stessa tattica): guerriero, ladro, mago, elfo, sacerdote, bardo. Le pozioni sono due voci ciascuna
(la grande sotto metà soglia, la normale fra metà e soglia) e ogni voce elenca prima l'azione preferita e poi l'altra come ripiego.

### 13.3 L'astuzia (`Astuzia.di`)

`astuzia = intelligenzaMassimaDiClasse × min(1, ASTUZIA_PARTENZA + ASTUZIA_PER_LIVELLO × (livello − 1))`, con partenza 0,4 e 0,075 per livello: al livello 1 vale il 40% del massimo
e arriva al massimo al livello 9. Un mostro "tonto" all'inizio diventa più ostico col salire del livello del mondo. Non è l'INTELLIGENZA vera del personaggio.

## 14. Il salvataggio

`TatticheMD` salva una riga col numero di tattiche e, per ognuna, l'`uuid` del personaggio e `TatticaMD.salva` (chi non ha una tattica non compare). Ogni tattica si legge dalla sua prima riga
(`TatticaMD.leggi(intestazione, stream)`).

Formato (`FormatoTattiche`): una riga per elemento, campi separati da pipe, la prima parola dice di che elemento si tratta; gli elementi che ne contengono altri **dicono quanti sono**
e questi seguono nelle righe successive.

```
TATTICA|numero di voci|disabilitata (0 o 1)
VOCE|attiva|ha un selettore|numero di condizioni|numero di azioni|nome
SELETTORE|rosa|ordinamento|tipo di danno dell'ordinamento|numero di filtri      ← seguono i filtri
FILTRO|tipo|negato|tipoConfronto|numero|stato|classe|categoria|tipo di danno
PREDICATO|tipo|negato|tipoConfronto|numero|rosa|numero di filtri                    ← seguono i filtri
AZIONE|tipo|criterio|solo se più forte dell'attacco|tipo di danno dell'arma
```

Dopo la riga `VOCE` vengono, nell'ordine: il selettore (se c'è), le condizioni, le azioni. Le fabbriche di ogni `leggi` ricontrollano i parametri.

**Tolleranza**: un nome di enumerato sconosciuto o un numero scritto male → `IOException` (il caricamento fallisce); una tattica *ben formata ma incoerente* (rosa non ammessa, azioni
di portate diverse) → la tattica viene **disabilitata** e il caricamento prosegue. I salvataggi non devono restare compatibili con le versioni precedenti, quindi il formato si cambia
senza migrazioni.

## 15. Il collegamento col gioco

### 15.1 Il combattimento (`LocazioneBase`)

Il combattimento procede per **round**: la lista dei turni è in `StatoDelCombattimento` e dice a chi tocca. Due strade portano a `agisciConLaTattica`:

- `eseguiPassoDellaMischia` — la mischia automatica (a ogni scatto del `TIMER`): agisce una volta il prossimo del piano, un mio personaggio o un avversario. Un mio personaggio che
  non è `isIncapaceDiAgire` agisce con `tatticaDi(attore)`.
- `agisceUnAvversario` — chiamato anche da `dopoAzioneDelGiocatore` dopo un'azione **manuale** del giocatore: gli avversari seguono la lista dei turni finché non torna a un mio personaggio.

Le azioni manuali del giocatore (la barra dei comandi) **non passano dalle tattiche**; passano però da `EsecutoreTattiche.attaccaConLeArmi` per l'attacco.

`agisciConLaTattica(attore, tattica)`: se `agisci` restituisce `null` (nessun bersaglio) e sono i miei con gli avversari finiti → `FINE_LOCAZIONE`. Se ha agito, i miei azzerano le opzioni
di corruzione e amicizia; poi si controlla la fine (avversari tutti caduti → `FINE_LOCAZIONE`; capo caduto → `GIOCO_PERSO`). Il round e il turno degli effetti di stato li gestisce
chi chiama.

### 15.2 La schermata (`Automa`, stato `TATTICHE`)

La schermata si apre dall'inventario con `MOSTRA_TATTICHE`; i comandi possibili sono i personaggi permanenti del gruppo (anche i caduti) e `ANNULLA`.

- `richiediAperturaTattiche` pubblica `RichiestaAperturaTattiche` con: il personaggio scelto, la sua tattica (o la predefinita della classe se non ne ha una — è quella che la
  schermata mostra), la predefinita e le `azioniPossibili`.
- `ComandoImpostaTattica(uuid, tattica)`: il motore **rifiuta** una tattica nulla o con `problemi(true)` non vuoti (log "Tattica rifiutata"), altrimenti `TatticheDeiPersonaggi.imposta` e ripubblica la
  richiesta di apertura per far riallineare la schermata.
- I comandi arrivano solo con la schermata aperta (`personaggioPerLeTattiche` controlla lo stato): **in combattimento le tattiche non si toccano**.

## 16. La schermata del giocatore (`foresta-swing`, `ui.tattiche`; la bozza e i testi in `foresta-tattiche`, `tattiche.modifica`)

Tre classi, con una divisione netta (le prime due non toccano il toolkit grafico e stanno nel modulo delle tattiche, così ogni front-end le usa; i testi sono in `testo_tattica_it.properties`):

| Classe | Ruolo |
| :--- | :--- |
| `BozzaTattica` | **lo stato** della modifica: le voci come liste modificabili (`Voce` con campi pubblici) e le regole di che cosa è ammesso |
| `TestoTattica` | **i testi**: etichette dei menu, la frase di una riga e le spiegazioni del riquadro destro; costruiti solo dai dati e dagli enumerati |
| `DisplayableCanvasTattiche` | **il disegno e i click**: costruisce le righe dalla bozza e le disegna; i menu sono `CasellaADiscesa` |

### 16.1 Perché esiste la bozza

I dati `*MD` sono immutabili, quindi non si possono modificare: la bozza li "scompone" in pezzi modificabili e, **solo quando il giocatore conferma** (pulsante Sì), `toTattica()`
ricostruisce una `TatticaMD` da mandare al motore. Una voce della bozza può non avere ancora azioni (è un lavoro a metà); la bozza lo sa dire (`problemi`) e la conferma resta bloccata.

### 16.2 Cosa fa `BozzaTattica`

- **`da(tattica)`**: scompone una tattica. Toglie l'**attacco di riserva** in coda (`isAttaccoDiRiserva`): la schermata lo disegna sempre come ultima riga, non si sposta e non si toglie,
  ed è il ripiego del valutatore. Assegna un nome predefinito ("Tattica N") alle voci che non ne hanno.
- **`normalizza`**: un tipoConfronto negato su un numero diventa l'equivalente senza rovescio ("NON MINORE 50" → "MAGGIORE 49"), per mostrare "almeno"/"al massimo" in modo leggibile.
- **Cosa è ammesso**: `roseAmmesse(voce)` (quelle valide per i miei **e** accettate da ogni azione della voce), `azioniAmmesse(voce, possibili)` (della stessa portata della prima azione,
  filtrate per la classe), `filtriAmmessi(rosa)` (senza quelli che confrontano con chi decide, e per `SE_STESSO` senza quelli che hanno senso solo su altri), `ordinamentiAmmessi(rosa)`
  (la storia di chi decide solo fra gli avversari), `predicatiAmmessi()` (senza `ASTUZIA` e `INTELLIGENZA`, che servono a dividere i mostri in fasce).
- **`aggiungiAzione` / `sostituisciAzione`** chiamano `correggiLaRosa`: se la rosa non va più bene passa alla prima ammessa e **azzera filtri e ordinamento**.
- **I valori predefiniti** (`azionePredefinita`, `filtroPredefinito`, `predicatoPredefinito`…) garantiscono che ogni elemento nuovo sia già valido: i dati verificano i parametri alla costruzione.
- **Le modifiche** (`conNumero`, `conConfronto`, `conParametro`, `conRosa`…) ricostruiscono un elemento con un campo cambiato; i numeri sono tenuti fra 0 e il massimo del tipo.

### 16.3 `DisplayableCanvasTattiche`

L'elenco di sinistra è un `ComponenteScorrevole<Riga>` che si **ricostruisce a ogni disegno e a ogni click** (`costruisciRighe`). Una `Riga` è fatta di `Token` (testo fisso, testo che apre un menu o
cambia un valore, icona), con un'`Ancora` per sapere dove aprire un menu. `disponi` mette le icone a destra sulla prima linea e gli altri pezzi a capo; `spezzaIPezziTroppoLarghi` divide in parole un pezzo più
largo di una riga.

Il corpo di ogni voce (`aggiungiVoce`) ha: il numero (verde se attiva, rosso se disattivata: click per cambiare), il nome (click ripiega, doppio click rinomina), le icone elimina/giù/su; poi *su* (rosa e
ordinamento), *con* (filtri), *se* (condizioni), *fai* (azioni). `haOrdinamento` nasconde l'ordinamento dove non ha senso (su sé stessi, azioni su gruppo, cambio d'arma).

`impostaTattica` riceve la richiesta del motore: se è lo stesso personaggio e la tattica è invariata la bozza **resta com'è** (la richiesta arriva anche quando si sposta un personaggio nel gruppo),
altrimenti riparte da quella del motore. I pulsanti **Sì**/**No** compaiono solo se `isModificata()`; **Predefinita** riparte da quella della classe. Solo Sì manda un evento che cambia la tattica
(`ComandoImpostaTattica`); le frecce mandano `ComandoSpostaPersonaggio`.

## 17. Aggiungere un pezzo: le ricette

Quando si aggiunge un valore a un enumerato bisogna toccare più punti; dove un test lo controlla è indicato.

**Una nuova azione (`TipoAzione`)**
1. `TipoAzione`: il valore, con descrizione, portata e classe di incantesimo (se c'è). Se ha una portata nuova, anche `PortataAzione`, con le sue rose.
2. `Requisiti.motivoDelNo`: il `case` con i suoi requisiti (altrimenti finisce in `incantesimoSingolo`, che presuppone un'azione con la sua classe di incantesimo).
3. `EsecutoreTattiche.esegui`: il `case` che la esegue (altrimenti finisce in `lanciaUnIncantesimoDiDanno`). Se è un'azione a criterio, anche `Valutatore.decisioneSu`.
4. `RegistroTattiche.azioniPossibili`: se non è per tutte le classi.
5. `TestoTattica.spiega(TipoAzione)` e `TestoAzioneInCombattimento` (il testo sotto il personaggio nel riquadro del combattimento).
6. `BozzaTattica.azionePredefinita`, se ha parametri.
7. Se ha parametri propri: `AzioneMD` (fabbrica, `salva`, `leggi`, `equals`, `hashCode`) e `FormatoTattiche` (la riga `AZIONE`).
8. Se registra un esito: scriverlo con `StatoDelCombattimento.registraEsitoAzione`.

**Un nuovo filtro (`TipoFiltro`)**: il valore con il suo `TipoParametro`; la risposta in `Filtri` (il test `SelezioneTest` fallisce se manca); `TestoTattica.spiega(TipoFiltro)`; se non ha senso
come criterio di ricerca, `BozzaTattica.SOLO_PER_SCEGLIERE` o `SOLO_SU_ALTRI`. Se ha un parametro nuovo: `FiltroMD`, `TipoParametro`, `BozzaTattica.filtro`/`parametroDi`/`parametroPredefinito`
e l'editor in `DisplayableCanvasTattiche.aggiungiParametriFiltro`.

**Un nuovo predicato (`TipoPredicato`)**: il valore; la risposta in `Predicati` (test `PredicatiTest`); `TestoTattica.predicato` e `spiega`; `BozzaTattica.predicatoPredefinito`/`passo`/`massimo`;
se non va offerto al giocatore, toglierlo da `predicatiAmmessi`.

**Un nuovo ordinamento (`TipoOrdinamento`)**: il valore; il `case` in `Ordinamenti.scegli` oppure in `misura` (e in `descriviLeMisure` se non misura); `TestoTattica.spiega`; se guarda la storia di
chi decide, `BozzaTattica.ordinamentiAmmessi`.

**Una nuova rosa (`TipoRosa`)**: il valore con i lati per cui vale; una classe in `tattiche.rose` e la sua `registra` in `Rose`; le rose delle portate che la ammettono (`PortataAzione`); `TestoTattica.spiega(TipoRosa)`; il test
`RosaTest`.

**Una nuova tattica base**: `TatticheBase` (mostri: la classe in `ASTUZIA_MASSIMA` e, se è magica, nei tre elenchi; giocabili: `deiGiocabili`).

## 18. I test

| Test | Cosa copre |
| :--- | :--- |
| `tattiche/ValutatoreTest`, `SelezioneTest` | la scansione delle voci, i selettori, i filtri (compreso "ogni tipo ha la sua risposta") |
| `tattiche/valutazione/PredicatiTest` | i predicati, la negazione, "ogni tipo ha la sua risposta" |
| `tattiche/rose/RosaTest` | le rose per lato |
| `tattiche/EsecutoreTatticheTest`, `motore/CombattimentoConTatticheTest` | l'esecuzione e il combattimento con le tattiche |
| `tattiche/RegistroTatticheTest`, `TatticheBaseTest`, `BersaglioDeiMostriTest` | la replica di oggi, le tattiche base, il bersaglio dei mostri |
| `tattiche/SalvataggioTatticheTest`, `TatticaDisabilitataTest` | il formato e la disabilitazione |
| `motore/ContestoTatticoTest`, `ScenarioSchermataTatticheTest` | il contesto e il flusso della schermata col motore |
| `tattiche/modifica/BozzaTatticaTest`, `TestoTatticaTest`, `TestiDiTestoTatticaTest` | la bozza, i testi, e che ogni valore spiegato abbia il suo testo |
| `ui/tattiche/DisplayableCanvasTatticheTest` (modulo UI) | la schermata |

Per riprodurre un caso, `PartitaDiTest` e gli altri helper dei test del motore costruiscono i gruppi; il dado si trucca con `TruccatoreDiDadi` (vedi `strumenti_di_prova.md`).

## 19. Osservazioni

Cose notate leggendo il codice per questo documento. Due difetti trovati nello stesso giro sono stati corretti: il testo delle pozioni della Salute in `TestoTattica` (diceva che serviva la MAGIA non piena) e la doppia scelta dell'incantesimo (§9.1); è stato tolto anche `SceltaIncantesimo.tipoDanno`, che nessuno usava.

- **`EsecutoreTattiche.attaccaConLeArmi` cerca il "prossimo avversario" in `avversari`** anche se, in teoria, potrebbe essere chiamato da un mostro: oggi lo chiamano solo i miei
  (`esegui` smista), ma il metodo è pubblico e non lo dice.
- **Le voci sono identificate per posizione** nello stato del combattimento: va bene perché la tattica non cambia durante lo scontro, ma sarebbe da rivedere se mai si potesse modificarla in combattimento.
- **Il contesto legge tutto da singleton** (`StatoDelCombattimento.getIstanza()`, `GruppoGiocatore.getIstanza()`): per le simulazioni e i test si passano i gruppi a `ContestoTattico.per`, ma lo stato
  resta il singleton se non si usa `conStato`.

## 20. Da fare / idee aperte

- **Allineare i tre elenchi di `TatticheBase`** (`MAGICI`, `CON_SILENZIO`, `CON_MORTE`) a `PersonaggioBase.getRepertorioIncantesimi`, o confrontarli con un test (c'è un `TODO` nel codice).
- **Azioni proprie dei mostri** (rigenerazione del Troll, teste dell'Idra, canto dell'Arpia): vanno prima create come `TipoAzione` (c'è un `TODO` in `TatticheBase`).
- **Registrare come "mancata" un'azione a segno che non applica l'effetto** (un Silenzio che non mette `SILENZIATO`): oggi non registrato (documento di progetto §11.7).
- **Mostrare la spiegazione delle decisioni lato UI**: oggi `InternoDecisioneTattica` porta le righe di spiegazione, ma la schermata mostra solo il testo dell'azione.
