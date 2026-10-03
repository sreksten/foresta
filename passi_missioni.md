# Catalogo dei passi possibili per `TipoMissione`

Questo documento nasce per preparare il terreno al generatore di missioni
casuali (`GrammarBean` + [[gestione_missioni.md]]): prima verifica se il
`SupertipoMissione` assegnato a ciascun `TipoMissione` è coerente con
l'esempio dato nel Javadoc, poi propone, per ciascun `TipoMissione`, una
sequenza di passi costruita con un piccolo vocabolario comune — in modo da
capire quanti "archetipi meccanici" servono davvero per coprire ~190 tipi di
missione con la struttura a `Passo`/`MissioneAPassi` descritta in
`gestione_missioni.md`.

Non è un piano di implementazione: è un'analisi di contenuto, propedeutica a
decidere quali `Passo` riutilizzabili scrivere per primi (con ogni evidenza,
i più redditizi sono quelli usati da decine di `TipoMissione` — vagabondare
finché non si trova qualcosa, combattere, raccogliere, contare fino a N,
tornare al punto di partenza, chiedere una conferma/scelta).

## 2. Vocabolario dei passi riutilizzabili

Ogni voce è un "tipo di passo" nel senso di `gestione_missioni.md` §3-4:
concretamente uno o più `Passo` costruiti con lo stesso builder fluente, non
una nuova astrazione. Le sigle qui sotto sono usate come notazione compatta
nelle tabelle della sezione 3.

| Sigla | Significato | Note implementative |
|---|---|---|
| `VAI(luogo)` | Muovi il gruppo verso una locazione nota e fissa (città, dungeon, covo...). Condizione: gruppo in quel luogo. Azione: eventualmente crea/popola la locazione se non esiste ancora. | Come oggi `RecuperaIlMedaglione` |
| `VAGABONDA_FINCHE(evento)` | Nessuna destinazione fissa: il passo resta in attesa finché, esplorando liberamente, si verifica un evento casuale (trovato l'ingrediente, incontrato il mostro giusto, rilevata una traccia...) | Condizione = proprietà scritta da un gancio nella logica di incontro/foraggiamento casuale della locazione corrente |
| `COMBATTI(bersaglio)` | Aggancia un combattimento contro un tipo di nemico specifico | Usa il sistema di combattimento esistente, condizione = combattimento vinto |
| `RACCOGLI(oggetto, quantità)` | Raccolta di un oggetto (drop, foraggiamento, pesca, scavo...) | Spesso seguito da `CONTA_FINCHE` |
| `CONTA_FINCHE(contatore, N)` | Ripete un sotto-passo (tipicamente `VAGABONDA_FINCHE`+`COMBATTI`/`RACCOGLI`) finché una proprietà numerica non raggiunge N | Proprietà intera incrementata via `aggiungiProprieta`, stesso spirito di `impostaSequenzaPassi`/`prossimoNellaSequenza` |
| `VAI_INIZIALE` | Torna al punto/PNG che ha assegnato la missione | Caso particolare di `VAI` |
| `CONSEGNA(oggetto)` | Consegna quanto raccolto/recuperato a un PNG o locazione | |
| `RICOMPENSA()` | Assegna oro/oggetto/reputazione e tipicamente chiama `completaMissione()` | |
| `DIALOGO(testo)` | Pura notifica testuale, nessuna interazione (`NotificaTestoParagrafo`) | |
| `CHIEDI_CONFERMA(testo)` | Domanda sì/no al giocatore | Vedi `gestione_missioni.md` §4 |
| `CHIEDI_SCELTA(testo, opzioni)` | Scelta fra 2 e 5 opzioni testuali | Vedi `gestione_missioni.md` §4 |
| `ATTENDI(durata)` | Il passo resta in attesa finché passa un certo tempo di gioco | Letto da `LineaTemporale`, utile per missioni "a tempo" o "torna fra N giorni" |
| `SORVEGLIA(bersaglio, durata)` | Variante di `ATTENDI` con osservazione attiva: il giocatore deve restare/ripassare in un luogo per un periodo | |
| `SCORTA(personaggio, luogo)` | Il gruppo si muove verso un luogo accompagnando/proteggendo un PNG | Spesso abbinato a `FALLISCI_SE(PNG ucciso)` |
| `EVITA_COMBATTIMENTO(luogo)` | Variante furtiva di `VAI`/`VAGABONDA`: farsi scoprire fa fallire la missione o dirotta su un ramo alternativo | |
| `COSTRUISCI(struttura)` | Rappresenta la costruzione/riparazione/creazione di qualcosa | Spesso condizionato da materiali raccolti in passi precedenti |
| `RAMO(stato_di_gioco)` | Il `prossimoPasso` sceglie fra più id in base allo stato di gioco (non a un input del giocatore) | |
| `FALLISCI_SE(condizione)` | Guardia che chiama `fallisciMissione()` quando una condizione avversa si avvera | Come oggi la guardia "città distrutta" di `RecuperaIlMedaglione` |
| `GENERA_PARAMETRI()` | Passo "zero" tipico delle missioni generate da `GrammarBean`: fissa i valori variabili (mandante, bersaglio, quantità...) come proprietà | Vedi `gestione_missioni.md` §3, "Persistenza dei dati generati" |

### Già implementati (2026-10-02)

Metodi di `MissioneAPassi` che restituiscono un `Passo` da completare con
`poi` (e, se serve, altre azioni con `esegui`, che ora si accumulano):

| Sigla | Metodo | Note |
|---|---|---|
| `VAI(luogo)` | `vai(momento, coordinate)`, `vai(momento, locazioneUnica)` | |
| `VAI_INIZIALE` | `tornaAlPuntoDiPartenza(momento)` | il punto di partenza è la casella in cui la missione si è attivata (`getPuntoDiPartenza()`) |
| `DIALOGO(testo)` | `dialogo(momento, testo)` | |
| `RICOMPENSA()` | `ricompensa(momento, monete, testo)` | per ora solo monete |
| `ATTENDI(durata)` | `attendiOre(momento, ore)` | ore di gioco da quando il passo è diventato corrente |
| `CONTA_FINCHE(contatore, N)` | `contaFinche(momento, contatore, N)` | con `incrementaContatore`/`getContatore` |
| `VAGABONDA_FINCHE` + `COMBATTI` + `CONTA_FINCHE` | `sconfiggi(momento, classePersonaggio, N)` | avversari di quella classe sconfitti ovunque |
| `VAGABONDA_FINCHE` + `RACCOGLI` + `CONTA_FINCHE` | `raccogli(momento, classiOggetto, N)` | oggetti di quella classe raccolti |
| `FALLISCI_SE(condizione)` | `Passo.falliscoSe(condizione, testo)` | controllata in tutti e tre i controlli, prima del resto |
| `CHIEDI_CONFERMA`, `CHIEDI_SCELTA`, `RAMO` | `Passo.chiediConferma`, `Passo.chiediScelta`, `poi(Supplier)` | dal punto 4 di gestione_missioni.md |
| (cerca una locazione) | `cercaLocazione(momento, classe)` | dal punto 6 di gestione_missioni.md |

Gli eventi di gioco (`InternoAvversarioSconfitto`, `InternoOggettoRaccolto`)
li ascolta `RegistroMissioni.registrati()` (chiamato da `Main`) e li gira a
tutte le missioni a passi non finite con `registraEvento`; si contano **per il
passo corrente**, così un passo conta solo quel che succede da quando è
corrente. Test: 4 nuovi in `MissioneAPassiTest`, `ScenarioPassiProntiTest`
(una caccia ai goblin con gli eventi veri del bus).

### Oggetti di missione (2026-10-02)

`raccogli(momento, OggettiDaRaccogliere)` è il `RACCOGLI` di oggetti che
esistono solo per la missione, come le radici di mandragola dell'alchimista:

```java
OggettiDaRaccogliere.di("MANDRAGOLA", NomeOggetto.femminile("radice di mandragola", "radici di mandragola"), 4)
    .in(ClassiLocazione.RADURA, ClassiLocazione.BOSCO)
    .conProbabilita(35)
    .alPiuPerLocazione(2);
```

- Il passo li semina (`Passo.semina`) finché è il passo corrente: entrando in
  una locazione, `Automa` chiede a `RegistroMissioni.getOggettoMissione` (prima
  la missione che ha rivendicato la casella, poi le altre in corso) e mette
  l'`OggettoMissione` al posto dell'oggetto della locazione, mai al posto
  dell'artefatto del registro. L'aggancio sta in `Automa` subito dopo `crea`,
  perché molte locazioni ridefiniscono `crea`.
- Compaiono solo nelle locazioni mai visitate, con la probabilità data, mai più
  di quanti ne mancano.
- **Ripiego**: se dopo `OggettiDaRaccogliere.getOreAlRipiego()` ore di gioco
  (72 se non si dice altrimenti, `conRipiegoDopoOre`) da quando il passo è
  corrente il gruppo non li ha ancora trovati tutti, al controllo di inizio
  locazione la missione si procura una locazione della prima classe adatta
  (`RegistroMissioni.cercaOCostruisci`, quindi rivendicata), la segna sulla
  mappa e lo dice ("Un viandante vi segna sulla mappa un posto dove trovare le
  radici di mandragola che vi mancano."). Lì ci sono tutti quelli che mancano,
  anche se la casella è già stata visitata (`MissioneAPassi.getRipiego`). Così
  una raccolta non resta bloccata quando il gruppo ha già esplorato quasi tutto.
  Il ripiego usa il claim della missione: una missione che ne ha già un altro
  in corso lo sovrascriverebbe.
- `OggettoMissione` (classe `ClassiOggetto.OGGETTO_MISSIONE`) si ricorda la
  missione, la chiave e il nome (`NomeOggetto`); raccoglierlo incrementa il
  contatore della missione con quella chiave. Come tutti gli oggetti delle
  locazioni non si salva.
- Immagine provvisoria: `img/oggetti/OggettoMissione.gif`.

Due missioni vere lo usano: `CacciaAiGoblin` e `LAlchimistaELaMandragola`,
entrambe sopra `IncaricoInCitta` (l'incarico preso in una città, con
l'intermezzo del mandante, e la ricompensa al ritorno; fallisce se la città
viene distrutta). L'incarico parte solo a una visita tranquilla, in cui
nessun'altra missione mostra un intermezzo entrando in città
(`MissioneAPassi.haUnIntermezzoInArrivo`): non alla prima visita, quando parte
la missione della città, né quando si torna a concluderne una. Fra due incarichi
pronti nella stessa visita parte il primo controllato. Test:
`ScenarioIncarichiInCittaTest`.

### Combattimenti, scorte e consegne (2026-10-02)

- **`COMBATTI(bersaglio)`**: `combatti(dove, IncontroDiMissione)`. Finché è il
  passo corrente, nella locazione in quelle coordinate ci sono gli avversari
  dell'incontro al posto di quelli che ci sarebbero stati (aggancio in `Automa`
  dopo `crea`, come per gli oggetti: `RegistroMissioni.getIncontroMissione`).
  `IncontroDiMissione.di(HOBGOBLIN, 3).conCapo("Sgranf")`: il capo ha un nome
  proprio e un livello in più. Si conclude a fine locazione, lì, quando il
  gruppo ne ha sconfitti lì quanti ne erano: `RegistroMissioni` registra ogni
  avversario sconfitto anche con la casella in cui è caduto
  (`MissioneAPassi.eventoSconfittoIn`), e quelli sconfitti altrove non contano.
- **Locazioni procurate**: `cercaLocazione` (e la leggenda dell'armaiolo, e i
  castelli delle missioni `Sconfiggi*`) usano `RegistroMissioni.cercaOCostruisci`:
  se non c'è una locazione disponibile della classe richiesta se ne costruisce
  una al posto di un bosco o di una palude disponibile, preferendo quelli già
  visitati, come non ancora visitata. Così un incarico non resta mai fermo.
- **`SCORTA`**: `prendiInScorta(momento, quando, nome)` fa viaggiare con il
  gruppo un `Viandante` (nuova `ClassePersonaggio.VIANDANTE`, con le
  caratteristiche e per ora l'immagine del bardo; non si incontra e non si
  recluta) come **ospite**; `scorta(momento, destinazione)` si conclude quando il
  gruppo arriva a destinazione, e lo scortato si separa dal gruppo. Si separa
  anche se la missione fallisce.
- **Ospiti del gruppo**: `GruppoGiocatore.getOspiti()`, una collezione a parte
  rispetto ai personaggi (anche a quelli a tempo), salvata con il gruppo. Gli
  ospiti non combattono, non si possono attaccare, non contano nei limiti del
  gruppo (né nei fissi della locanda) e non si equipaggiano: possono essere
  quanti si vuole. Nel riquadro del gruppo compaiono dopo i personaggi, con il
  solo nome e "Ospite del gruppo".
- **Ospiti vulnerabili** (ostaggi, feriti da soccorrere):
  `GruppoGiocatore.aggiungiOspite(ospite, true)`, o
  `prendiInScorta(momento, quando, nome, true)`. Non combattono, ma gli
  avversari li possono attaccare: `PersonaggioBase.attacca(Gruppo)` chiede prima
  al gruppo `scegliOspiteBersaglio()`, che sceglie un ospite vulnerabile vivo con
  probabilità proporzionale (con tre personaggi vivi e un ospite vulnerabile, un
  attacco su quattro va all'ospite). Riposando recuperano come i personaggi, e
  nel riquadro del gruppo se ne vede la salute. La vulnerabilità si salva con il
  gruppo. `scorta(momento, destinazione, testoSeMuore)` fa fallire la missione
  se lo scortato muore.
- **`CONSEGNA`**: `consegna(momento, dove, oggetti, testo)`. Gli oggetti di
  missione non stanno nell'inventario: il gruppo li ha se la missione li ha
  contati, e consegnandoli escono dal conteggio. `IncaricoInCitta` la fa al
  ritorno, prima della ricompensa, se `getOggettiDaConsegnare()` non è null
  (la mandragola).
- `Passo.falliscoSe` ora si può chiamare più volte: vale la prima guardia che
  scatta.

Due incarichi in città nuovi li usano: `LaTagliaSuSgranf` (rivendica un bosco,
lo segna sulla mappa e ci mette la banda di Sgranf; 30 monete) e `IlPellegrino`
(rivendica un tempio, Anselmo viaggia con il gruppo come ospite fino al tempio; 25
monete al ritorno dalla sorella). Test: `ScenarioCombattiScortaConsegnaTest`.

### Gli ultimi passi (2026-10-03)

- **`SORVEGLIA(luogo, durata)`**: `sorveglia(dove, volte, oreFraLeVisite)`. Il
  gruppo deve passare da quella casella `volte` volte, a inizio locazione, con
  almeno `oreFraLeVisite` ore di gioco fra una visita che conta e la successiva;
  le visite troppo ravvicinate non contano (`getVisiteNelPassoCorrente`). Usa
  `Passo.aOgniControllo`, un'azione eseguita ogni volta che il passo corrente
  viene valutato nel suo controllo.
- **`EVITA_COMBATTIMENTO(luogo)`**: `evitaCombattimento(momento, dove,
  testoSeScoperti)`. Si conclude arrivando in quella casella; se nel frattempo il
  gruppo ha combattuto (ha attaccato un avversario o ne ha abbattuto uno, anche
  con un incantesimo: `RegistroMissioni` conta l'evento `COMBATTIMENTO`) la
  missione fallisce. Per un ramo alternativo c'è
  `haCombattutoNelPassoCorrente()`.
- **`COSTRUISCI(struttura)`**: `costruisci(momento, dove, Costruzione, testo)`.
  `Costruzione.con(materiali...).conMonete(n).inOre(n)`: consuma gli oggetti di
  missione raccolti prima, paga le monete e fa passare le ore; che cosa si
  costruisce lo fa la missione con un altro `esegui`.
- **`GENERA_PARAMETRI()`**: `generaParametri(momento, parametri)`. Fissa come
  proprietà i valori generati, ciascuno solo se non c'è già, così restano gli
  stessi dopo un caricamento (`getParametro`).
- **`RICOMPENSA` non solo in monete**: `ricompensa(momento, Ricompensa, testo)`,
  con `Ricompensa.inMonete(n).conPreziosi(n).conEsperienza(n).conArtefatto(...)`.
  L'artefatto si crea quando la missione lo consegna, finisce nell'inventario
  del gruppo e si mostra con la rivelazione dei cofani.

Test: `ScenarioPassiAvanzatiTest` e, per gli ospiti vulnerabili,
`ScenarioOspitiVulnerabiliTest`.

L'incarico in città `IlRapimentoDiArmando` li usa: la missione rivendica una
grotta, la segna sulla mappa e ci mette la banda di goblin di Ghignazzo
(`combatti`); sconfitta la banda, Armando si unisce al gruppo come ospite
vulnerabile (`prendiInScorta(..., true)`) e va riportato vivo in città; 35
monete. Il viaggio usa `scortaFinoAllaMeta`, che si conclude sia arrivando con
lo scortato vivo sia, dovunque, quando muore (`isScortatoMorto()` sceglie il
ramo): se Armando muore la missione resta aperta finché il gruppo non torna in
città, dove c'è la scena triste con la moglie, e solo dopo fallisce. Test:
`ScenarioRapimentoDiArmandoTest`.
Il nome dell'ostaggio per ora è fisso; si potrà prendere da una grammatica. Con questi il catalogo del §2 è coperto tutto.

## 3. Mappatura `TipoMissione` → sequenza di passi

I due esempi di partenza dell'utente, per riferimento:

- **RECUPERO** (Recupera il Medaglione): `VAI(bersaglio)` → `COMBATTI` →
  `RACCOGLI(oggetto)` → `VAI_INIZIALE` → `CONSEGNA` → `RICOMPENSA`
- **RACCOLTA_TROFEI**: `VAGABONDA_FINCHE(mostro giusto)` → `COMBATTI` →
  `RACCOGLI(trofeo)` → `CONTA_FINCHE(trofei, N)` [ripete i tre passi
  precedenti] → `VAI_INIZIALE` → `CONSEGNA` → `RICOMPENSA`

Le tabelle seguenti applicano lo stesso principio a tutti gli altri tipi,
raggruppati per supertipo (uso il supertipo **attuale** dell'enum, non
quello corretto proposto al punto 1, per rendere le tabelle confrontabili
con il codice esistente).

### ACQUISIZIONE

| `TipoMissione` | Sequenza di passi | Note |
|---|---|---|
| `RACCOLTA_INGREDIENTI` | `VAGABONDA_FINCHE(ingrediente)` → `CONTA_FINCHE(N)` → `VAI_INIZIALE` → `CONSEGNA` → `RICOMPENSA` | |
| `RACCOLTA_TROFEI` | `VAGABONDA_FINCHE(mostro)` → `COMBATTI` → `RACCOGLI(trofeo)` → `CONTA_FINCHE(N)` → `VAI_INIZIALE` → `CONSEGNA` → `RICOMPENSA` | Esempio di riferimento dell'utente |
| `RACCOLTA_CRISTALLI` | `VAI(miniera/grotta)` → `VAGABONDA_FINCHE(cristallo)` → `CONTA_FINCHE(N)` → `VAI_INIZIALE` → `CONSEGNA` → `RICOMPENSA` | Come `RACCOLTA_INGREDIENTI` ma con luogo di partenza fisso |
| `RACCOLTA_ESSENZA` | `VAGABONDA_FINCHE(mostro)` → `COMBATTI` → `RACCOGLI(essenza)` → `CONTA_FINCHE(N)` → `VAI_INIZIALE` → `CONSEGNA` → `RICOMPENSA` | Come `RACCOLTA_TROFEI` se l'essenza si ottiene da un nemico |
| `MINIERA` | `VAI(miniera)` → `RACCOGLI(minerale)` + `CONTA_FINCHE(N)` [sul posto] → `VAI_INIZIALE` → `CONSEGNA` → `RICOMPENSA` | Nessun vagabondaggio: l'estrazione avviene tutta nello stesso luogo |
| `RECUPERO` | `VAI(bersaglio)` → `COMBATTI` (opz.) → `RACCOGLI(oggetto)` → `VAI_INIZIALE` → `CONSEGNA` → `RICOMPENSA` | Esempio di riferimento dell'utente (Recupera il Medaglione) |
| `TRASPORTO` | `GENERA_PARAMETRI` → `VAI(mittente)` → `RACCOGLI(oggetto sigillato)` → `VAI(destinatario)` → `CONSEGNA` → `RICOMPENSA` | `FALLISCI_SE(oggetto perso/rubato)` come ramo opzionale |
| `CACCIA_AL_TESORO` | `VAI(dungeon)` → `VAGABONDA_FINCHE(indizio)` [eventuali `RAMO` su più indizi] → `COMBATTI` (opz., guardiano) → `RACCOGLI(tesoro)` → `VAI_INIZIALE` → `RICOMPENSA` | |
| `ARTIGIANATO` | `RACCOGLI(materiali)` → `VAI(fucina/laboratorio)` → `COSTRUISCI(oggetto)` → `CONSEGNA`/`RICOMPENSA` | `CHIEDI_SCELTA` se l'oggetto ha varianti |
| `COSTRUZIONE` | `RACCOGLI(materiali)` → `CONTA_FINCHE(N)` → `VAI(cantiere)` → `COSTRUISCI(struttura)` → `DIALOGO(inaugurazione)` → `RICOMPENSA` | |
| `CACCIA_ANIMALI` | `VAGABONDA_FINCHE(animale)` → `COMBATTI` → `RACCOGLI(pelle)` → `CONTA_FINCHE(N)` → `VAI_INIZIALE` → `CONSEGNA` → `RICOMPENSA` | Identico a `RACCOLTA_TROFEI`, cambia solo il flavor |
| `PESCA` | `VAI(riva/porto)` → `VAGABONDA_FINCHE`/`ATTENDI(pesce)` → `RACCOGLI(pesce)` → `CONTA_FINCHE(N)` → `VAI_INIZIALE` → `CONSEGNA` → `RICOMPENSA` | |
| `AGRICOLTURA` | `VAI(campo)` → `DIALOGO(semina)` → `ATTENDI(tempo di crescita)` → `RACCOGLI(raccolto)` → `CONSEGNA` → `RICOMPENSA` | Unico archetipo con un vero passo `ATTENDI` multi-giorno |
| `FORAGGIAMENTO` | `VAGABONDA_FINCHE(pianta/fungo)` → `CONTA_FINCHE(N)` → `VAI_INIZIALE` → `CONSEGNA` → `RICOMPENSA` | Identico a `RACCOLTA_INGREDIENTI` |
| `ALLEVAMENTO` | `VAI(recinto)` → `CHIEDI_CONFERMA`/`RACCOGLI(cattura)` → `ATTENDI(crescita)` → `RICOMPENSA` | Simile ad `AGRICOLTURA` con cattura iniziale al posto della semina |

### NEGOZIAZIONE

| `TipoMissione` | Sequenza di passi | Note |
|---|---|---|
| `DIPLOMAZIA` | `VAI(corte)` → `DIALOGO(richiesta)` → `CHIEDI_SCELTA(argomentazioni)` → `RAMO(esito)` → `RICOMPENSA`/`FALLISCI_SE(rifiuto)` | |
| `MEDIAZIONE` | `VAI(luogo neutro)` → `DIALOGO(ascolto)` → `CHIEDI_SCELTA(proposta)` → `RAMO(fazione A/B/compromesso)` → `RICOMPENSA` | |
| `COMMERCIO` | `VAI(mercato)` → `CHIEDI_SCELTA(vendere/comprare)` → `RAMO(prezzo negoziato)` → `RICOMPENSA` | |
| `ASTA` | `VAI(sala d'asta)` → `CHIEDI_SCELTA(offerta)` [più turni, `CONTA_FINCHE`] → `RAMO(vinta/persa)` → `RICOMPENSA`/`DIALOGO(sconfitta)` | |
| `BORSA` | `GENERA_PARAMETRI(andamento mercato)` → `CHIEDI_SCELTA(investimento)` → `ATTENDI` → `RAMO(guadagno/perdita)` → `RICOMPENSA` | Vedi nota di classificazione §1 |
| `SENSERIA` | `VAI(controparte 1)` → `DIALOGO` → `VAI(controparte 2)` → `CHIEDI_SCELTA(termini)` → `RICOMPENSA` | |
| `CONTRATTO` | `VAI(controparte)` → `CHIEDI_SCELTA(clausole)` → `RAMO(accettato/rifiutato)` → `RICOMPENSA` | |
| `SCAMBIO_OSTAGGI` | `VAI(luogo scambio)` → `CHIEDI_CONFERMA(procedere)` → `RAMO(pulito/imboscata)` → `COMBATTI` (se imboscata) → `RICOMPENSA`/`FALLISCI_SE(ostaggio perso)` | |
| `MERCATO_NERO` | `VAI(mercato nero)` → `CHIEDI_SCELTA(merce)` → `RAMO(scoperti?)` → `COMBATTI`/`EVITA_COMBATTIMENTO` (se scoperti) → `RICOMPENSA` | Vedi nota di classificazione §1 |
| `NEGOZIAZIONE_TREGUA` | `VAI(campo nemico)` → `CHIEDI_SCELTA(condizioni)` → `RAMO(accettata/respinta)` → `RICOMPENSA`/`COMBATTI` (se respinta) | |

### COMBATTIMENTO

| `TipoMissione` | Sequenza di passi | Note |
|---|---|---|
| `CACCIATORE_DI_TAGLIE` | `GENERA_PARAMETRI(bersaglio)` → `VAGABONDA_FINCHE`/`VAI(bersaglio)` → `COMBATTI` → `RACCOGLI(prova)` → `VAI_INIZIALE` → `CONSEGNA` → `RICOMPENSA` | |
| `VENDETTA` | `DIALOGO(motivazione)` → `VAGABONDA_FINCHE(nemico)` → `COMBATTI` → `DIALOGO(epilogo)` → `RICOMPENSA` | Ricompensa spesso solo narrativa |
| `DUELLO` | `VAI(arena)` → `CHIEDI_CONFERMA(accetti)` → `COMBATTI(1v1)` → `RICOMPENSA` | |
| `CONTROLLO_CREATURA` | `VAI(tana)` → `COMBATTI(non fatale)` → `CHIEDI_SCELTA(dominare/liberare)` → `RAMO` → `RICOMPENSA` | Vedi nota di classificazione §1 |
| `BATTAGLIA` | `VAI(campo)` → `CONTA_FINCHE(ondate, COMBATTI)` → `RICOMPENSA` | |
| `ASSALTO` | `VAI(fortezza)` → `COMBATTI(esterno)` → `COMBATTI(interno)` → `RICOMPENSA` | |
| `IMBOSCATA` | `VAGABONDA_FINCHE(bersaglio in transito)` → `COMBATTI(sorpresa)` → `RICOMPENSA` | |
| `GUERRIGLIA` | `CONTA_FINCHE(N, [VAGABONDA_FINCHE+COMBATTI])` → `RICOMPENSA` | |
| `RITIRATA_TATTICA` | `COMBATTI(sfavorevole)` → `CHIEDI_CONFERMA(ritirarsi)` → `VAI(raccolta)` → `RICOMPENSA` | |
| `CARICA` | `VAI(linea nemica)` → `COMBATTI(frontale)` → `RICOMPENSA` | |
| `CIRCONDAMENTO` | `VAI(posizione 1)` → `VAI(posizione 2)` → `COMBATTI(accerchiamento)` → `RICOMPENSA` | |
| `BLOCCO` | `VAI(passaggio)` → `DIALOGO(sbarramento)` → `COMBATTI` [`CONTA_FINCHE`] → `RICOMPENSA` | |
| `SCHERMAGLIA` | `VAGABONDA_FINCHE(pattuglia)` → `COMBATTI(breve)` → `RICOMPENSA` | |
| `ASSEDIO_DIFESA` | `VAI(fortezza)` → `CONTA_FINCHE(ondate, COMBATTI)` → `RICOMPENSA` | Quasi identico a `DIFESA` (PROTEZIONE) e `TRINCEA` |
| `FLOTTA` | `VAI(porto)` → `COMBATTI(navale)` → `RICOMPENSA` | |
| `SORTITA` | `VAI(assediati)` → `CHIEDI_CONFERMA(tentare)` → `COMBATTI(uscita)` → `VAI(rientro)` → `RICOMPENSA` | |
| `CAVALLERIA` | `VAI(linea nemica)` → `COMBATTI(carica montata)` → `RICOMPENSA` | Praticamente identico a `CARICA` |
| `TRINCEA` | `VAI(trincea)` → `CONTA_FINCHE(assalti respinti)` → `RICOMPENSA` | Simile ad `ASSEDIO_DIFESA` |
| `ESPLOSIONE` | `VAI(obiettivo)` → `RACCOGLI`/`COSTRUISCI(ordigno)` → `CHIEDI_CONFERMA(detonare)` → `RAMO(scoperti?)` → `COMBATTI` (se scoperti) → `RICOMPENSA` | Vedi nota di classificazione §1 |
| `PONTE_TATTICO` | `VAI(ponte)` → `COMBATTI(controllo passaggio)` [`CONTA_FINCHE`] → `RICOMPENSA` | |
| `ASSEDIO_OFFENSIVO` | `VAI(fortezza nemica)` → `CONTA_FINCHE(fasi d'assedio)` → `COMBATTI(breccia finale)` → `RICOMPENSA` | |
| `COMBATTIMENTO_RITUALE` | `VAI(luogo sacro)` → `CHIEDI_CONFERMA(accettare il rito)` → `COMBATTI(regole speciali)` → `DIALOGO(esito)` → `RICOMPENSA` | |
| `BATTAGLIA_AEREA` | `VAI(cielo/torre)` → `COMBATTI(aereo)` → `RICOMPENSA` | |
| `DUELLO_MAGICO` | Come `DUELLO` | `COMBATTI` vincolato a un set di incantesimi, nessun passo nuovo |
| `COMBATTIMENTO_BESTIA` | `VAGABONDA_FINCHE(bestia rara)` → `COMBATTI` → `RICOMPENSA` | |
| `DUELLO_ANTICO` | Come `DUELLO` | + `DIALOGO` cerimoniale prima/dopo |
| `PULIZIA_DEI_DUNGEON` | `VAI(dungeon)` → `CONTA_FINCHE(stanze, [VAGABONDA_FINCHE+COMBATTI])` → `RICOMPENSA` | |

### PROTEZIONE

| `TipoMissione` | Sequenza di passi | Note |
|---|---|---|
| `SALVATAGGIO` | `VAI(prigione)` → `COMBATTI` (opz.) → `DIALOGO(liberazione)` → `SCORTA(prigioniero, iniziale)` → `RICOMPENSA` | |
| `SCORTA` | `VAI(partenza PNG)` → `SCORTA(PNG, destinazione)` [`COMBATTI` lungo il tragitto, `FALLISCI_SE(PNG ucciso)`] → `RICOMPENSA` | |
| `DIFESA` | `VAI(villaggio)` → `CONTA_FINCHE(ondate, COMBATTI)` → `RICOMPENSA` | Stesso scheletro di `ASSEDIO_DIFESA`/`TRINCEA`, cambia solo il framing |
| `GUARIGIONE` | `GENERA_PARAMETRI(malattia)` → `VAI(ingrediente)` → `RACCOGLI` → `VAI(paziente)` → `CHIEDI_CONFERMA(somministrare)` → `RICOMPENSA` | |
| `SOCCORSO` | `VAI(zona pericolosa)` → `COMBATTI`/`EVITA_COMBATTIMENTO` → `SCORTA(ferito)` → `VAI_INIZIALE` → `RICOMPENSA` | |
| `EVACUAZIONE` | `VAI(zona di guerra)` → `CONTA_FINCHE(N civili, SCORTA ciascuno)` → `RICOMPENSA` | |
| `EPIDEMIA` | Come `GUARIGIONE` + `CONTA_FINCHE(N pazienti)` | |
| `PURIFICAZIONE` | `VAI(terra corrotta)` → `CHIEDI_CONFERMA(rito)` → `COMBATTI` (opz., guardiano corrotto) → `RICOMPENSA` | Vedi nota di classificazione §1 |
| `POSSESSIONE` | `VAI(posseduto)` → `COMBATTI`/`CHIEDI_SCELTA(metodo esorcismo)` → `RAMO(riuscito/fallito)` → `RICOMPENSA` | |
| `QUARANTENA` | `VAI(area infetta)` → `CHIEDI_CONFERMA(istituire)` → `ATTENDI(N giorni)` → `RICOMPENSA` | |
| `CONFINAMENTO` | `VAI(bersaglio)` → `COMBATTI`/`CHIEDI_CONFERMA(catturare)` → `VAI(reclusione)` → `CONSEGNA` → `RICOMPENSA` | Vedi nota di classificazione §1: sovrapposto a `CARCERE_ILLEGALE` |
| `BARRIERA_MAGICA` | `VAI(luogo da proteggere)` → `RACCOGLI(componenti)` → `COSTRUISCI(barriera)` → `RICOMPENSA` | |
| `PRIMO_SOCCORSO` | `VAI(ferito)` → `CHIEDI_SCELTA(intervento)` → `RAMO(stabilizzato/critico)` → `RICOMPENSA` | |
| `RIFUGIO` | `RACCOGLI(materiali)` → `VAI(sito)` → `COSTRUISCI(rifugio)` → `SCORTA(profughi)` → `RICOMPENSA` | |
| `CURA_MAGICA` | Come `GUARIGIONE` | Ingrediente sostituito da un incantesimo/rituale |
| `ANTI_VELENO` | `VAI(fonte veleno)` → `RACCOGLI(antidoto)` → `VAI(avvelenato)` → `DIALOGO(somministrazione)` → `RICOMPENSA` | |
| `SANTUARIO` | `RACCOGLI(offerte)` → `VAI(sito sacro)` → `COSTRUISCI(santuario)` → `DIALOGO(consacrazione)` → `RICOMPENSA` | |
| `CONTENIMENTO` | `VAI(minaccia)` → `COMBATTI`/`COSTRUISCI(barriera)` → `ATTENDI(tenuta)` → `RICOMPENSA` | |
| `VIGILIA` | `VAI(postazione)` → `SORVEGLIA(durata)` [eventuale `COMBATTI` se qualcosa tenta di passare] → `RICOMPENSA` | |
| `OCCULTAMENTO` | `VAI(bersaglio)` → `SCORTA`/`CHIEDI_CONFERMA(nascondiglio)` → `EVITA_COMBATTIMENTO(pattuglie)` → `RICOMPENSA` | |
| `PROTEZIONE_TEMPORALE` | `CHIEDI_CONFERMA(attivare scudo)` → `COMBATTI(singolo attacco)` → `RICOMPENSA` | Versione ridotta di `DIFESA`/`BARRIERA_MAGICA` a un solo evento |
| `ASILO` | `VAI(rifugiato)` → `SCORTA(luogo d'asilo)` → `CHIEDI_CONFERMA(concedere asilo)` → `RICOMPENSA` | |
| `BLINDATURA` | `RACCOGLI(materiali)` → `VAI(struttura)` → `COSTRUISCI(rinforzo)` → `RICOMPENSA` | |

### INVESTIGAZIONE

| `TipoMissione` | Sequenza di passi | Note |
|---|---|---|
| `INVESTIGAZIONE` | `VAI(scena)` → `RACCOGLI(indizi)` [`CONTA_FINCHE`] → `RAMO(colpevole dedotto)` → `DIALOGO(rivelazione)` → `RICOMPENSA` | |
| `ESPLORAZIONE` | `VAGABONDA_FINCHE(luogo scoperto)` → `DIALOGO(scoperta)` → `VAI_INIZIALE` → `RICOMPENSA` | |
| `RINTRACCIAMENTO` | `GENERA_PARAMETRI(bersaglio)` → `VAGABONDA_FINCHE(traccia)` [`CONTA_FINCHE` su più tracce] → `VAI(destinazione)` → `CHIEDI_SCELTA(confronto/cattura)` → `RICOMPENSA` | |
| `COMUNICAZIONE` | `VAI(luogo del rito)` → `CHIEDI_CONFERMA(tentare il contatto)` → `DIALOGO(messaggio)` → `RICOMPENSA` | Vedi nota di classificazione §1 |
| `RICERCA` | `VAI(biblioteca)` → `CONTA_FINCHE(testi)` → `DIALOGO(informazione trovata)` → `RICOMPENSA` | |
| `CURIOSITA_ACCADEMICA` | Come `RICERCA` | Ricompensa spesso solo narrativa/reputazione |
| `SORVEGLIANZA` | `VAI(osservazione)` → `SORVEGLIA(bersaglio, durata)` → `RACCOGLI(informazione)` → `RICOMPENSA` | |
| `INTERROGATORIO` | `VAI(prigioniero)` → `CHIEDI_SCELTA(approccio)` → `RAMO(ottenuta/rifiutata)` → `RICOMPENSA` | |
| `INCHIESTA` | `CONTA_FINCHE(N testimoni/prove, [VAI+DIALOGO])` → `RAMO(conclusione)` → `RICOMPENSA` | Scala più ampia di `INVESTIGAZIONE` |
| `FORENSICA` | `VAI(scena del crimine)` → `RACCOGLI(prove)` [`CONTA_FINCHE`] → `DIALOGO(analisi)` → `RICOMPENSA` | |
| `INFILTRAZIONE` | `VAI(organizzazione)` → `EVITA_COMBATTIMENTO(copertura)` → `RACCOGLI(informazione)` → `VAI(uscita)` → `RICOMPENSA` | Vedi nota di classificazione §1 |
| `PROFILING` | `VAI(archivio)` → `RACCOGLI(dati)` [`CONTA_FINCHE`] → `DIALOGO(profilo)` → `RICOMPENSA` | |
| `TRACCIA_MAGICA` | `VAGABONDA_FINCHE(traccia)` [`CONTA_FINCHE`] → `VAI(sorgente)` → `RICOMPENSA` | |
| `TESTIMONI` | `CONTA_FINCHE(N testimoni, [VAI+DIALOGO])` → `DIALOGO(sintesi)` → `RICOMPENSA` | |
| `VISIONE_PASSATO` | `VAI(oggetto/luogo)` → `CHIEDI_CONFERMA(attivare visione)` → `DIALOGO(scena rivelata)` → `RICOMPENSA` | Vedi nota di classificazione §1 |
| `LETTURA_MENTE` | `VAI(bersaglio)` → `CHIEDI_CONFERMA(tentare)` → `RAMO(riuscita/resistita)` → `RICOMPENSA` | Vedi nota di classificazione §1 |
| `VISIONE_FUTURO` | `CHIEDI_CONFERMA(consultare)` → `DIALOGO(profezia)` → `RICOMPENSA` | Vedi nota di classificazione §1 |
| `SCOPERTA_SEGRETO` | Come `INVESTIGAZIONE` | Indizi → rivelazione |
| `RICERCA_OGGETTO` | `VAGABONDA_FINCHE`/`VAI(oggetto)` → `RACCOGLI` → `VAI_INIZIALE` → `CONSEGNA` → `RICOMPENSA` | Praticamente identico a `RECUPERO` |
| `SCOPERTA_INGANNO` | `VAI(sospettato)` → `CHIEDI_SCELTA(prove)` → `RAMO(confermato/smentito)` → `RICOMPENSA` | |
| `LETTURA_RUNE` | `VAI(iscrizione)` → `CHIEDI_CONFERMA(decifrare)` → `RAMO(riuscita/indizio mancante)` → `RICOMPENSA` | |
| `DOCUMENTAZIONE` | `VAGABONDA_FINCHE(reperto)` [`CONTA_FINCHE`] → `RACCOGLI(catalogazione)` → `VAI_INIZIALE` → `RICOMPENSA` | |
| `DECIFRAZIONE` | Come `LETTURA_RUNE` + `CONTA_FINCHE` | Più iscrizioni |

### PROGRESSIONE

| `TipoMissione` | Sequenza di passi | Note |
|---|---|---|
| `FAZIONE` | `CONTA_FINCHE(N incarichi)` → `RAMO(grado raggiunto)` → `RICOMPENSA` | |
| `ADDESTRAMENTO` | `VAI(luogo addestramento)` → `CONTA_FINCHE(sessioni)` → `CHIEDI_SCELTA(specializzazione)` (opz.) → `RICOMPENSA` | |
| `COMPETIZIONE` | `VAI(gara)` → `COMBATTI`/`CHIEDI_SCELTA(round)` → `RAMO(vittoria/sconfitta)` → `RICOMPENSA` | |
| `TORNEO` | Come `COMPETIZIONE` + `CONTA_FINCHE(round eliminatori)` | |
| `EREDITA` | `DIALOGO(annuncio)` → `CHIEDI_CONFERMA(accettare)` → `VAI(lascito)` → `RICOMPENSA` | |
| `NOMINA` | `DIALOGO(proposta)` → `CHIEDI_CONFERMA(accettare)` → `RICOMPENSA` | |
| `FRATELLANZA` | `VAI(sede ordine)` → `CHIEDI_CONFERMA(adesione)` → `CONTA_FINCHE(prove)` → `RICOMPENSA` | |
| `CORONAZIONE` | `CONTA_FINCHE(requisiti)` → `VAI(luogo incoronazione)` → `DIALOGO(cerimonia)` → `RICOMPENSA` | |
| `ASCESA_SOCIALE` | `CONTA_FINCHE(imprese pubbliche)` → `RAMO(status raggiunto)` → `RICOMPENSA` | |
| `TITOLO_NOBILIARE` | Come `NOMINA` | + `CONTA_FINCHE(meriti pregressi)` come precondizione |
| `MAESTRIA` | `CONTA_FINCHE(prove di maestria)` → `RICOMPENSA` | Come `ADDESTRAMENTO`, focalizzato su una disciplina |
| `SPECIALIZZAZIONE` | `CHIEDI_SCELTA(ramo)` → `CONTA_FINCHE(prove)` → `RICOMPENSA` | |
| `FAMA` | `CONTA_FINCHE(imprese pubbliche)` → `RICOMPENSA` | Ricompensa = reputazione, non materiale |
| `REPUTAZIONE` | Come `FAMA` | Incremento più graduale/locale |
| `RICCHEZZA` | `CONTA_FINCHE(oro accumulato)` → `RICOMPENSA` | Vedi nota di classificazione §1 |
| `LIGNAGGIO` | `VAI(archivi genealogici)` → `DIALOGO(rivelazione)` → `RICOMPENSA` | |
| `LEGATARIO` | `DIALOGO(designazione)` → `CHIEDI_CONFERMA(accettare)` → `RICOMPENSA` | |
| `EREDE` | Come `EREDITA`/`LEGATARIO` | + `RAMO` su più pretendenti concorrenti |
| `BENEDIZIONE_RICEVERE` | `VAI(luogo sacro)` → `CHIEDI_CONFERMA(chiedere benedizione)` → `DIALOGO(rito)` → `RICOMPENSA` | Vedi nota di classificazione §1 |
| `SUCCESSIONE` | `CONTA_FINCHE(prove idoneità)` → `RAMO(successore designato)` → `RICOMPENSA` | |
| `GESTIONE` | `CONTA_FINCHE(risorse, con ATTENDI fra i cicli)` → `COSTRUISCI(potenziamento)` → `RICOMPENSA` | Vedi nota di classificazione §1 |
| `RICOSTRUZIONE` | `RACCOGLI(materiali)` [`CONTA_FINCHE`] → `VAI(sito)` → `COSTRUISCI(ricostruzione)` → `RICOMPENSA` | Vedi nota di classificazione §1: identico a `COSTRUZIONE` |

### RELAZIONI

| `TipoMissione` | Sequenza di passi | Note |
|---|---|---|
| `LEALTA` | `DIALOGO(problema del compagno)` → `CHIEDI_SCELTA(come aiutarlo)` → sotto-sequenza variabile (spesso composita di altri archetipi) → `RICOMPENSA` | Rapporto rafforzato più che ricompensa materiale |
| `MATRIMONIO` | `RACCOGLI(preparativi)` [`CONTA_FINCHE`] → `VAI(cerimonia)` → `DIALOGO(celebrazione)` → `RICOMPENSA` | |
| `CELEBRAZIONE` | `RACCOGLI(preparativi)` → `VAI(luogo festa)` → `DIALOGO` → `RICOMPENSA` | Come `MATRIMONIO` senza tema nuziale |
| `ALLEANZA_MATRIMONIALE` | `CHIEDI_SCELTA(termini politici)` → `RAMO(accordo)` → sequenza di `MATRIMONIO` → `RICOMPENSA` | Composizione di `DIPLOMAZIA` + `MATRIMONIO` |
| `RISCATTO` | `DIALOGO(colpa)` → `CONTA_FINCHE(atti di redenzione, sotto-passi variabili)` → `DIALOGO(epilogo)` → `RICOMPENSA` | Vedi nota di classificazione §1 |
| `PERDONO` | `VAI(persona offesa)` → `CHIEDI_CONFERMA(chiedere perdono)` → `RAMO(concesso/rifiutato)` → `RICOMPENSA` | |
| `REDENZIONE_PUBBLICA` | Come `RISCATTO` + `DIALOGO` pubblico finale | Vedi nota di classificazione §1 |

### ILLECITO

| `TipoMissione` | Sequenza di passi | Note |
|---|---|---|
| `FURTO` | `VAI(camera del tesoro)` → `EVITA_COMBATTIMENTO` → `RACCOGLI(oggetto)` → `VAI(uscita)` → `RICOMPENSA` | |
| `ASSASSINIO` | `VAGABONDA_FINCHE`/`VAI(bersaglio)` → `EVITA_COMBATTIMENTO(avvicinamento)` → `COMBATTI(colpo furtivo)` → `RICOMPENSA` | |
| `SABOTAGGIO` | `VAI(obiettivo)` → `EVITA_COMBATTIMENTO` → `COSTRUISCI(manomissione)` → `RAMO(scoperti/riusciti)` → `RICOMPENSA` | |
| `SPIONAGGIO` | `VAI(riunione)` → `SORVEGLIA(durata)` → `RACCOGLI(informazione)` → `RICOMPENSA` | |
| `CONTROSPIONAGGIO` | `SORVEGLIA(sospetti)` → `RAMO(spia identificata)` → `CHIEDI_SCELTA(smascherare/seguire)` → `RICOMPENSA` | Vedi nota di classificazione §1 |
| `RICATTO` | `RACCOGLI(prova compromettente)` → `VAI(bersaglio)` → `CHIEDI_SCELTA(richiesta)` → `RAMO(pagato/rifiutato)` → `RICOMPENSA` | |
| `INGANNO` | `VAI(bersaglio)` → `CHIEDI_SCELTA(menzogna)` → `RAMO(creduto/scoperto)` → `RICOMPENSA` | |
| `CONTRABBANDO` | `RACCOGLI(merce)` → `VAI(checkpoint)` → `EVITA_COMBATTIMENTO(controlli)` → `VAI(destinazione)` → `RICOMPENSA` | |
| `TRADIMENTO` | `CHIEDI_CONFERMA(accettare tradimento)` → `RAMO(conseguenze narrative)` → `RICOMPENSA` | |
| `CORRUZIONE` | `VAI(ufficiale)` → `CHIEDI_SCELTA(importo)` → `RAMO(accettata/rifiutata)` → `RICOMPENSA` | |
| `FALSIFICAZIONE` | `RACCOGLI(originale da copiare)` → `COSTRUISCI(falso)` → `CONSEGNA` → `RICOMPENSA` | |
| `RAPIMENTO` | `VAGABONDA_FINCHE`/`VAI(bersaglio)` → `COMBATTI`/`EVITA_COMBATTIMENTO(cattura)` → `SCORTA(nascondiglio)` → `RICOMPENSA` | |
| `PIRATERIA` | `VAI(rotta commerciale)` → `VAGABONDA_FINCHE(nave)` → `COMBATTI(abbordaggio)` → `RACCOGLI(bottino)` → `RICOMPENSA` | |
| `AVVELENAMENTO` | `RACCOGLI(veleno)` → `VAI(bersaglio)` → `CHIEDI_CONFERMA(somministrare)` → `RICOMPENSA` | |
| `VANDALISMO` | `VAI(struttura)` → `EVITA_COMBATTIMENTO` → `DIALOGO(danneggiamento)` → `RICOMPENSA` | |
| `FRODE` | `CHIEDI_SCELTA(schema)` → `RAMO(riuscito/scoperto)` → `RICOMPENSA` | |
| `DIFFAMAZIONE` | `VAI(luogo pubblico)` → `CHIEDI_SCELTA(voce da spargere)` → `RAMO(creduta/smentita)` → `RICOMPENSA` | |
| `SCHIAVITU` | `COMBATTI`/`EVITA_COMBATTIMENTO(cattura)` → `SCORTA(mercato)` → `CONSEGNA` → `RICOMPENSA` | |
| `SEDIZIONE` | `VAI(piazza)` → `CHIEDI_SCELTA(discorso/azione)` → `RAMO(sommossa)` → `RICOMPENSA` | |
| `INCENDIO` | `VAI(obiettivo)` → `EVITA_COMBATTIMENTO` → `DIALOGO(appiccare)` → `RAMO(riuscito/scoperto)` → `RICOMPENSA` | |
| `SICARIO` | `GENERA_PARAMETRI(mandante/compenso)` + sequenza di `ASSASSINIO` | |
| `RICICLAGGIO` | `RACCOGLI(denaro sporco)` → `CHIEDI_SCELTA(canale)` → `RAMO(riuscito/tracciato)` → `RICOMPENSA` | |
| `VIOLAZIONE_DOMICILIO` | `VAI(abitazione)` → `EVITA_COMBATTIMENTO` → `RACCOGLI`/`DIALOGO(perquisizione)` → `RICOMPENSA` | |
| `SACRILEGIO` | `VAI(luogo sacro)` → `CHIEDI_CONFERMA(profanare)` → `RAMO(riuscito/punizione)` → `RICOMPENSA` | Vedi nota su duplicato con `BLASFEMIA` |
| `TRAFFICO` | Come `CONTRABBANDO` + `CONTA_FINCHE(N carichi)` | |
| `FURTO_IDENTITA` | `RACCOGLI(documenti)` → `CHIEDI_SCELTA(uso identità)` → `RAMO(riuscito/scoperto)` → `RICOMPENSA` | |
| `BLASFEMIA` | Come `SACRILEGIO` | Vedi nota su duplicato |
| `BRIGANTAGGIO` | `VAGABONDA_FINCHE`/`VAI(bersaglio)` → `COMBATTI(rapina)` → `RACCOGLI(bottino)` → `RICOMPENSA` | |
| `FALSA_TESTIMONIANZA` | `VAI(tribunale)` → `CHIEDI_SCELTA(versione)` → `RAMO(creduta/scoperta)` → `RICOMPENSA` | |
| `IMBROGLIONE` | `VAI(tavolo da gioco)` → `CHIEDI_SCELTA(metodo)` → `RAMO(vinto/scoperto)` → `RICOMPENSA` | |
| `CARCERE_ILLEGALE` | `COMBATTI`/`EVITA_COMBATTIMENTO(cattura)` → `VAI(prigione clandestina)` → `CONSEGNA` → `RICOMPENSA` | Vedi nota di classificazione §1: sovrapposto a `CONFINAMENTO` |
| `TORTURA` | `VAI(prigioniero)` → `CHIEDI_SCELTA(metodo)` → `RAMO(informazione ottenuta)` → `RICOMPENSA` | |
| `TENTATIVO_OMICIDIO` | Come `ASSASSINIO` + `RAMO(fallito → il bersaglio reagisce)` | |

### SPIRITUALE

| `TipoMissione` | Sequenza di passi | Note |
|---|---|---|
| `SPEZZATURA` | `VAI(posseduto/maledetto)` → `CHIEDI_CONFERMA(tentare il rito)` → `RAMO(riuscito/fallito)` → `RICOMPENSA` | |
| `RITUALE` | `RACCOGLI(componenti)` → `VAI(luogo del rito)` → `CHIEDI_CONFERMA(eseguire)` → `RICOMPENSA`/`RAMO(effetto collaterale)` | |
| `BENEDIZIONE` | `VAI(bersaglio)` → `CHIEDI_CONFERMA(eseguire)` → `RICOMPENSA` | |
| `NECROMANZIA` | `VAI(cimitero)` → `RACCOGLI(componenti)` → `CHIEDI_CONFERMA(rito)` → `COMBATTI` (opz., controllo instabile) → `RICOMPENSA` | |
| `EVOCAZIONE` | `RACCOGLI(componenti)` → `CHIEDI_CONFERMA(evocare)` → `RAMO(amica/ostile)` → `COMBATTI` (se ostile) → `RICOMPENSA` | |
| `MALEDIZIONE` | `VAI(bersaglio)` → `CHIEDI_CONFERMA(lanciare)` → `RICOMPENSA` | |
| `INCANTESIMO` | `RACCOGLI(componenti/pergamena)` → `CHIEDI_CONFERMA(lanciare)` → `RICOMPENSA` | |
| `SIGILLO` | `VAI(portale)` → `RACCOGLI(componenti)` → `CHIEDI_CONFERMA(sigillare)` → `COMBATTI` (opz., guardiano) → `RICOMPENSA` | |
| `TRASMUTAZIONE` | `RACCOGLI(materiale grezzo)` → `CHIEDI_CONFERMA(trasmutare)` → `RICOMPENSA` | |
| `ASTRI` | `VAI(osservatorio)` → `ATTENDI(momento propizio)` → `DIALOGO(lettura)` → `RICOMPENSA` | |
| `DIVINAZIONE` | `CHIEDI_CONFERMA(consultare)` → `DIALOGO(visione)` → `RICOMPENSA` | |
| `ILLUSIONE` | `CHIEDI_CONFERMA(creare)` → `RAMO(inganno riuscito/svelato)` → `RICOMPENSA` | |
| `ANTI_MAGIA` | `VAI(area incantata)` → `CHIEDI_CONFERMA(dispellare)` → `COMBATTI` (opz., resistenza) → `RICOMPENSA` | |
| `INVISIBILITA` | `CHIEDI_CONFERMA(attivare)` → `EVITA_COMBATTIMENTO(attraversamento)` → `RICOMPENSA` | |
| `CHANNELING` | `VAI(fonte energia)` → `CHIEDI_CONFERMA(canalizzare)` → `RICOMPENSA` | |
| `TELEPORTAZIONE` | `RACCOGLI(componenti)` → `CHIEDI_CONFERMA(teletrasportarsi)` → `VAI(destinazione, istantaneo)` → `RICOMPENSA` | |
| `SHAPE_SHIFT` | `CHIEDI_SCELTA(forma)` → `RAMO(uso della forma per un ostacolo)` → `RICOMPENSA` | |
| `CONTROLLO_ELEMENTALE` | `VAI(fonte elementale)` → `CHIEDI_CONFERMA(controllo)` → `COMBATTI` (opz., ribelle) → `RICOMPENSA` | |
| `LEGAME_SPIRITUALE` | `VAI(spirito)` → `CHIEDI_CONFERMA(stringere legame)` → `RICOMPENSA` | |
| `VIAGGIO_ASTRALE` | `CHIEDI_CONFERMA(proiettarsi)` → `VAI(piano astrale)` → `DIALOGO(scoperta)` → `RICOMPENSA` | |
| `COMUNIONE` | `VAI(luogo sacro)` → `CHIEDI_CONFERMA(comunione)` → `DIALOGO(rivelazione)` → `RICOMPENSA` | |
| `TRANCE` | `CHIEDI_CONFERMA(entrare in trance)` → `ATTENDI(durata)` → `DIALOGO(visione)` → `RICOMPENSA` | |
| `FUSIONE` | `CHIEDI_CONFERMA(fondersi)` → `RAMO(esito)` → `RICOMPENSA` | |
| `VIAGGIO_TEMPO` | `RACCOGLI(componenti)` → `CHIEDI_CONFERMA(viaggiare)` → `VAI(epoca diversa)` → `RICOMPENSA` | |
| `REALTA_PARALLELA` | `CHIEDI_CONFERMA(varcare la soglia)` → `VAI(dimensione alternativa)` → `RICOMPENSA` | |
| `POSSESSO_CORPO` | `VAI(bersaglio)` → `CHIEDI_CONFERMA(possedere)` → `RAMO(riuscita/respinta)` → `RICOMPENSA` | |
| `ASSORBIMENTO` | `COMBATTI(fonte di potere)` → `CHIEDI_CONFERMA(assorbire)` → `RICOMPENSA` | |
| `CONTROLLO_MENTE` | `VAI(bersaglio)` → `CHIEDI_CONFERMA(controllo)` → `RAMO(riuscito/resistito)` → `RICOMPENSA` | |
| `SCAMBIO_CORPI` | `CHIEDI_CONFERMA(eseguire lo scambio)` → `RAMO(complicazioni)` → `RICOMPENSA` | |
| `CREAZIONE_GOLEM` | `RACCOGLI(componenti)` → `COSTRUISCI(golem)` → `CHIEDI_CONFERMA(animare)` → `RICOMPENSA` | |
| `FISSIONE` | `CHIEDI_CONFERMA(scissione)` → `RAMO(esito)` → `RICOMPENSA` | |
| `MORTE_TEMPORALE` | `CHIEDI_CONFERMA(ibernazione)` → `ATTENDI(durata)` → `RICOMPENSA` | |
| `ANIMAZIONE_OGGETTI` | `RACCOGLI(componenti)` → `CHIEDI_CONFERMA(animare)` → `RICOMPENSA` | |
| `PATTO_ANIMA` | `VAI(entità)` → `CHIEDI_SCELTA(termini del patto)` → `RAMO(accettato/rifiutato)` → `RICOMPENSA` | |

## 4. Osservazioni conclusive

- **Concentrazione degli archetipi**: la stragrande maggioranza delle ~190
  voci si riduce a variazioni su un numero piccolo di scheletri: (a)
  vagabonda/vai → combatti/raccogli → eventualmente ripeti N volte → torna e
  consegna (ACQUISIZIONE, molto di COMBATTIMENTO, parte di INVESTIGAZIONE);
  (b) vai → chiedi conferma/scelta → ramo sull'esito (NEGOZIAZIONE, buona
  parte di SPIRITUALE, parte di ILLECITO); (c) vai → resisti a N ondate
  (COMBATTIMENTO difensivo, PROTEZIONE); (d) accumula/costruisci nel tempo
  con `ATTENDI` (AGRICOLTURA, ALLEVAMENTO, GESTIONE, QUARANTENA). Conviene
  quindi scrivere per primi i passi `VAI`, `VAGABONDA_FINCHE`, `COMBATTI`,
  `RACCOGLI`, `CONTA_FINCHE`, `CHIEDI_CONFERMA`/`CHIEDI_SCELTA` e `RAMO`: da
  soli coprono la quasi totalità delle sequenze di questo catalogo.
- **`CHIEDI_SCELTA`/`CHIEDI_CONFERMA` sono usati moltissimo**: la richiesta
  dell'utente di trattare l'interazione col giocatore come parte integrante
  del piano ([[gestione_missioni.md]] §4) è confermata dai numeri — decine
  di `TipoMissione`, specie in NEGOZIAZIONE, SPIRITUALE e ILLECITO, non sono
  esprimibili senza un vero punto di scelta del giocatore che determini il
  ramo successivo.
- Le correzioni di supertipo proposte al punto 1 non sono necessarie per
  scrivere i `Passo`: un `TipoMissione` mal classificato ha comunque una
  sequenza di passi coerente con il proprio esempio. Servono piuttosto a
  evitare, quando si scriverà il generatore a grammatica, di pescare per
  errore un `TipoMissione` "spirituale" quando si cercava qualcosa di
  "investigativo" (e viceversa).
