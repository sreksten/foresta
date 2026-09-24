# Artefatti, pergamene e incantatore: piano di lavoro

> Stato: aggiornato al 2026-09-24. Prossimo passo: fase 1. Raccoglie le decisioni prese, le fasi di lavoro, i TODO e i dubbi ancora aperti.
> I salvataggi **non** devono restare retrocompatibili: il formato si cambia liberamente, ma ogni modifica va coperta da test di salva/rileggi.

## 1. Obiettivo

- Gli **incantamenti** diventano oggetti a sé: si trovano come loot, si comprano e vendono, e con un **incantatore** si trasferiscono in modo permanente su un artefatto.
- Il **loot** della foresta (spade, scudi, elmi, armature, anelli, cofani) produce artefatti veri, che i personaggi usano come armi ed equipaggiamento e che si possono passare da un personaggio all'altro.
- Gli artefatti possono avere un **nome proprio** (es. una spada di fuoco chiamata "Diavolina").

## 2. Decisioni prese

### Modello

- **Pergamena.** Un incantamento-oggetto è un `Artefatto` di tipo `TipoArtefatto.INCANTAMENTO` (supertipo `INCANTAMENTO`, slot `NUCLEO`), chiamato per ora "Pergamena". La sua lista `incantamenti` contiene gli effetti che trasferisce. `Incantamento` resta l'oggetto valore che descrive l'effetto: nome, `TipoDanno`, bonus fisso, coefficiente. Così la pergamena riusa inventari, compravendita e salvataggio degli artefatti.
- **Nome proprio.** Facoltativo, su `ArtefattoMD`. Se lo sceglie il giocatore si normalizza con le iniziali maiuscole (es. "lama del drago" → "Lama Del Drago"), nel momento in cui il testo è disponibile. Se lo costruisce il codice resta com'è scritto.
  - È **un solo campo in più** (`nomeProprio`). La forma completa si compone con i campi che ogni artefatto ha già, `nome` (es. "la spada di fuoco", con l'articolo) e `descrizione` (es. "che brucia i nemici"): "Diavolina, la spada di fuoco, che brucia i nemici". Senza nome proprio resta com'è oggi: "la spada di fuoco, che brucia i nemici".
  - **Nei testi** si usa sempre la forma completa:
    - la descrizione dei personaggi del comando `AIUTO` (`PersonaggioBase.getDescrizione`);
    - il "qui c'è …" della locazione;
    - le informazioni sui templi;
    - i messaggi di azione ("Pippo raccoglie Diavolina, la spada di fuoco, che brucia i nemici.").
  - **Nell'inventario** (`DisplayableCanvasScambiatoreArtefatti`) il nodo dell'artefatto mostra in grande il nome proprio, oppure il `nome` se non c'è. La descrizione compare in piccolo, come nodo secondario. Con il nome proprio conviene che il nodo piccolo sia "la spada di fuoco, che brucia i nemici", così il nome generico non si perde. Da riguardare con la resa grafica.
  - La **grammatica** che genera le armi a caso passa anche il nome proprio, facoltativo.
- **Pergamene generate a caso.** Possono avere solo la parte fissa, solo la parte percentuale, o entrambe.
- **Livello di una pergamena.** Non ne limita l'uso: il limite sta sull'oggetto incantato (numero massimo di incantamenti).

### Equipaggiamento

- **Inventario personale.** È la lista degli artefatti che il personaggio porta con sé, cioè il suo equipaggiamento; quelli del gruppo ne sono esclusi. Tutto ciò che è nell'inventario personale applica i suoi modificatori, come oggi.
- **Slot.** Ogni `TipoArtefatto` ha il suo `SlotArtefatto` (`getSlotArtefatto()`).
  - Al massimo 1 artefatto per `TESTA`, `CORPO`, `MANO_PRINCIPALE` e `MANO_SECONDARIA`.
  - Nessun limite su `ACCESSORIO` (anelli, talismani, ninnoli).
  - `NUCLEO` (le pergamene): un personaggio non può prenderlo, resta nel gruppo con un messaggio o fumetto.
  - `ENTRAMBE_LE_MANI` (nuovo valore di `SlotArtefatto`), per le **armi a due mani**: occupa sia la mano principale sia la secondaria.
    - Chi impugna un'arma a due mani non può equipaggiare scudo, libro magico o seconda arma finché non la ripone nell'inventario del gruppo. Viceversa, non può prendere un'arma a due mani se ha qualcosa in una delle due mani.
    - Il rifiuto si avverte con un fumetto.
- **Arma in `MANO_SECONDARIA`.** Oltre a scudo e libro, anche **spada, mazza o ascia**, ma solo per **Ladro/Ladra ed Elfo/Elfa**. Lancia e bastone magico restano solo nella mano principale.
- **Armi a due mani.** Nuovo `TipoArtefatto.SPADONE` (supertipo `ARMA`, slot `ENTRAMBE_LE_MANI`, danno `TAGLIENTE`, "impugna"), che per ora riusa la grafica della spada.
  - **+50% di danno base** rispetto a un'arma a una mano dello stesso livello.
  - Lo stesso **−25% di `PARATA`** della doppia arma: si rinuncia allo scudo in cambio di potenza.
  - Lancia e bastone magico restano armi a una mano. Il bastone in particolare, perché altrimenti il mago non potrebbe più usare il libro magico.
- **Slot di equipaggiamento sull'artefatto.** Nuovo campo `ArtefattoMD.slotEquipaggiamento` (di tipo `SlotArtefatto`): lo slot che l'artefatto occupa **davvero** mentre un personaggio lo porta.
  - Di solito coincide con lo slot del suo `TipoArtefatto`. Serve per i casi in cui non è così: la spada di Ladro o Elfo nella mano secondaria.
  - Vale `null` quando l'artefatto non è equipaggiato (inventario del gruppo, negozi, templi).
  - Si assegna quando un personaggio prende l'artefatto: se la mano principale è occupata e quella secondaria è libera, la seconda arma va nella secondaria. Si azzera quando l'artefatto viene riposto.
  - **Va salvato e riletto** come gli altri campi di `ArtefattoMD`.
- **Scambio delle armi fra le mani.** Nessun comando apposito per ora: si ripongono le armi nell'inventario del gruppo e si riprendono nell'ordine voluto.
- **Livello.** Un personaggio **non può prendere** un artefatto di livello superiore al suo. Vale al prelievo dall'inventario del gruppo, al loot e agli acquisti per un personaggio. Resta nel gruppo finché il personaggio non sale di livello. Così non serve decidere quali incantamenti "spegnere".

### Loot

- **Spada, elmo, armatura, scudo** (slot con limite). Candidati:
  - i personaggi **vivi** che hanno ancora libero quello slot;
  - e che hanno un livello ≥ a quello dell'artefatto.

  Chi ha già un oggetto in quello slot è escluso: il confronto lo fa il giocatore nell'inventario, così non si rischia di far indossare una ciofeca a chi ha già una megaspada fiammeggiante.
  - Per una spada, un Ladro o un Elfo con la mano principale occupata ma la secondaria libera **è** un candidato: la prende nella secondaria.
  - Chi impugna un'arma a due mani non è candidato per scudo, libro o seconda arma. Per prendere un'arma a due mani servono entrambe le mani libere.
- **Anello e accessori.** Candidati: tutti i personaggi vivi (salvo il vincolo di livello).
- **Morti.** Sempre esclusi, come già oggi.
- **Scelta.** Si propongono i candidati più **`GRUPPO`** (comando e icona esistono già, usati dall'alchimista). Se non c'è nessun candidato, l'oggetto va direttamente nell'inventario del gruppo, con un messaggio.
- **Troppo carico.** Se il personaggio scelto è troppo carico, l'oggetto va nel gruppo con il messaggio già in uso (fatto).
- **Pergamene.** Vanno sempre nel gruppo (slot `NUCLEO`).
- **Cofano.** Raramente produce una pergamena o un artefatto casuale.
- **Monete e gemme.** Restano risorse del gruppo.

### Incantamenti ed effetti

- **Cosa si incanta.** Armi, scudi, elmi, armature.
  - Gli **accessori** (anelli, talismani, ninnoli) non si incantano: tengono solo i loro modificatori di attributo.
  - Il **libro magico** non si incanta: è già una fonte di magia, e dà di suo un bonus simile a un incantamento, un po' più alto (vedi "Combattimento").
- **Sulle armi.** Danno aggiuntivo, come oggi.
- **Sui pezzi difensivi.** Resistenza contro gli attacchi di quel `TipoDanno`, con una parte fissa e una percentuale, come il danno (vedi "Combattimento").

### Combattimento

- **Resistenze.** Entrano nella formula a rendimenti decrescenti che c'è già, `danno × 100 / (100 + difesa)`, senza un tetto separato: la difesa non porta mai all'immunità.
  - Per un attacco di tipo T: `difesa_T = (difesa base + Σ fisso_T × livello del pezzo) × (1 + Σ percentuale_T)`.
  - La somma è su tutti gli incantamenti di tipo T di elmo, scudo e armatura del difensore. La difesa base è quella di oggi: `COSTITUZIONE + PARATA` per il danno fisico, `RESISTENZA_MAGICA` per quello elementale o magico.
  - `difesa_T` si usa nella mitigazione del danno base (se l'arma è di tipo T) e in quella di ogni incantamento di tipo T dell'attaccante.
  - Sulle armature la parte percentuale resta più bassa che sulle armi (es. metà), perché moltiplica una difesa che cresce già con il livello.
- **Doppia arma** (Ladro/Ladra, Elfo/Elfa con un'arma in `MANO_SECONDARIA`): **due fasi di attacco** per turno.
  - La seconda è con l'arma secondaria, al **60%**: sia il danno base sia i suoi incantamenti.
  - L'arma secondaria occupa lo slot `MANO_SECONDARIA`, anche se il suo `TipoArtefatto` dice `MANO_PRINCIPALE`: come assegnarla è un dubbio aperto (§6).
  - "Guardia aperta": **−25% di `PARATA`** finché si impugnano due armi. Senza scudo, poi, non si ha la parata dello scudo.
- **Scudo.** Oltre ai modificatori scritti sull'artefatto, ha una **`PARATA` intrinseca** proporzionale al suo livello. Così la scelta tra scudo e seconda arma conta sempre, anche con scudi "spogli".
- **Libro magico.** Bonus al **danno degli incantesimi** di chi lo porta, con parte fissa e parte percentuale come un incantamento del `GradoIncantamento` corrispondente, **+25%**. Occupa `MANO_SECONDARIA`, quindi il mago sceglie tra libro e scudo.

### Prezzi delle pergamene

Vedi la tabella dei gradi in §8. Formula: `2 × bonus fisso + percentuale`; +25% per i tipi di danno con effetti di stato; rivendita al 50%. I numeri sono di partenza e vanno bilanciati (TODO).

### Fusione (incantatore)

- **Costo.** 10 monete + 5 per incantamento trasferito.
- **Limite.** Un artefatto ha al massimo `min(5, livello − 1)` incantamenti **in totale**. A livello 1 non se ne hanno. Una spada di livello 3 con già 1 incantamento ne può ricevere al più un altro.
- **Conteggio.** Si contano gli **incantamenti**, non le pergamene: una pergamena che ne porta due vale due, per il costo e per i limiti.
- **Controlli.** La schermata impedisce di mettere sul banco più incantamenti di quanti l'artefatto ne possa ricevere (rifiuto con fumetto), e il motore ricontrolla tutto alla conferma.
- **Rifiuti.** Nessun artefatto sul banco, più di un artefatto, nessuna pergamena, limite superato, monete insufficienti.
- **Incantamenti uguali.** Restano **distinti** anche se hanno lo stesso `TipoDanno`, così non si aggira il limite; i bonus si sommano comunque. Le pergamene usate vengono distrutte.
- **Nome proprio.** Si chiede a **ogni** fusione con il `Prompt`, proponendo come valore predefinito quello che l'artefatto aveva già, se ce l'aveva.
- **Da dove si prende l'artefatto.** Solo dall'inventario del gruppo: se ce l'ha un personaggio, prima va riposto.
- **Comando.** Un nuovo `Comando.FUSIONE` di conferma, con l'icona `img/icone/Fusione.gif` (già nelle risorse), registrata in `ClasseIcona` come le altre.

### Negozi e generatore

- **Dove.** In città: armaiolo, alchimista, venditore di pergamene (per ora con l'immagine dell'alchimista) e incantatore. I negozi sparsi nella foresta vengono dopo.
- **Magazzini.** La chiave diventa (coordinate, tipo di inventario), perché più negozi della stessa città hanno la stessa coordinata.
- **Generatore.** `GeneratoreArtefatti`: per ora uno scheletro generale, poi una grammatica sul modello delle locande (formato da definire).

## 3. Fatto

- [x] **`RegistroArtefatti.costruisciArtefatto` usa `Artefatto.di`**: un'arma presa al tempio torna un `ArmaFisica`. Prima `getArmaEquipaggiata()` falliva con `ClassCastException`.
- [x] **L'artefatto del tempio si toglie dal registro** una volta raccolto (`LocazioneBase.rimuoviOggetto`). Corruzione e fuga lo lasciano al suo posto.
- [x] **L'anello "del Valore" aumenta il `VALORE`**, non la `FORZA`.
- [x] **Controllo del peso** in `Artefatto.prendi` e `Anello.prendi` (`Artefatto.consegna`). Se il personaggio è troppo carico, l'oggetto va nell'inventario del gruppo con un messaggio. `Personaggio.puoPrendere` è nell'interfaccia e lo usa anche il prelievo.
- [x] **Note vuote dei modificatori:** `-` si rilegge come `""`, quindi il modificatore riletto è `equals` all'originale.
- [x] **Test** (13, verdi): `ArtefattoMDTest`, `RegistroArtefattiMDTest`, `RegistroArtefattiTest`.
- [x] **`TipoArtefatto.getSlotArtefatto()`** (aggiunto da Stefano).
- [x] **Chi raccoglie l'oggetto di fine locazione:** nuovo stato `SCELTA_DESTINATARIO_OGGETTO`, che propone i personaggi vivi più `GRUPPO` al posto di `ANNULLA`.
  - Scegliendo `ANNULLA` si andava in `IndexOutOfBoundsException` (bug §5.1).
  - `Artefatto.prendi` e `Anello.prendi` gestiscono `GRUPPO` (`Artefatto.riponiNelGruppo`).
  - Nuovo `Comando.isPersonaggio()`.
- [x] **Pozioni di magia grandi** (bug §5.2-§5.4): notifica giusta in `addPozioniMagiaGrande`, voce corretta nell'inventario dell'alchimista, e anche loro si perdono nella fuga. In `GruppoGiocatore.fugge` c'è un TODO per rivedere la fuga, oggi troppo penalizzante.
- [x] **BERSERK sul danno fisico** (bug §5.6), con `CalcolatoreCombattimentoBerserkTest`: il guerriero ferito passa da 58 a 80 danni con un'arma fisica, e il danno resta uguale con un'arma elementale. Il critico è neutralizzato per rendere il test deterministico.
- [x] **Test:** `ArtefattoPrendiTest` (5), che copre `GRUPPO`, personaggio, troppo carico, scelta lasciata all'automa e `isPersonaggio`. In tutto 18 test su artefatti e raccolta, tutti verdi.

## 4. Fasi

Ogni fase si può provare e committare da sola.

### Fase 1: modello
- `ArtefattoMD.nomeProprio` facoltativo, salvato come `-` quando manca.
- Un metodo di presentazione unico per la forma completa (§2, "Nome proprio"), usato in `LocazioneBase`, `Informazioni`, `PersonaggioBase` e `Artefatto.prendi`/`Anello.prendi`, dove oggi `nome + ", " + descrizione` è ripetuto a mano. Nell'inventario: nome proprio (o nome) in grande, descrizione nel nodo secondario.
- `Artefatto.isIncantabile()` (armi, scudi, elmi, armature) e `getIncantamentiMassimi()` = `min(5, livello − 1)`.
- `SlotArtefatto.ENTRAMBE_LE_MANI` e `TipoArtefatto.SPADONE`.
- `ArtefattoMD.slotEquipaggiamento`, salvato come `-` quando è `null`.
- Test di salva/rileggi di `nomeProprio`, `slotEquipaggiamento` e delle pergamene.

### Fase 2: equipaggiamento a slot e livello
- Un unico controllo `Personaggio.puoEquipaggiare(Artefatto)` restituisce un motivo di rifiuto oppure nessuno. I motivi sono:
  - troppo carico;
  - slot occupato;
  - slot `NUCLEO`;
  - livello troppo alto;
  - arma in mano secondaria per una classe che non può;
  - arma a due mani con una mano già occupata, o un oggetto da mano (scudo, libro, seconda arma) con un'arma a due mani impugnata.
- Lo usano il prelievo (`GruppoGiocatore.suEventoRichiestaPrelievoArtefatto`), `Artefatto.consegna` e la scelta dei candidati per il loot. Ogni motivo ha il suo fumetto o messaggio.
- Prendere un artefatto ne imposta `slotEquipaggiamento`; riporlo lo azzera.
- `getArmaEquipaggiata()` legge l'arma con `slotEquipaggiamento` `MANO_PRINCIPALE` o `ENTRAMBE_LE_MANI`, invece del "primo artefatto di supertipo ARMA". Per la doppia arma si aggiunge `getArmaSecondaria()`.
- Test per ogni motivo di rifiuto, per l'arma impugnata e per la seconda arma messa nella mano secondaria.

### Fase 3: generatore di artefatti (scheletro)
- `GeneratoreArtefatti`:
  - `generaArtefatto(TipoArtefatto, livello)`
  - `generaArtefattoCasuale(livello)`
  - `generaPergamena(livello)`
- Dietro un'interfaccia, per poter mettere poi la grammatica senza cambiare chi lo chiama.
- Per ora i nomi vengono da piccole tabelle interne, e il nome proprio compare raramente.
- Livello di riferimento: `Statistiche.getLivello()` (lo stesso dei mostri).

### Fase 4: loot
- **Candidati.** `Spada`, `Scudo`, `Anello` (e i nuovi `Elmo` e `Armatura`, se si vogliono come loot a sé) generano l'artefatto e propongono i candidati secondo §2, più `GRUPPO`.
- **Filtro dei candidati.** Lo stato `SCELTA_DESTINATARIO_OGGETTO` esiste già (vivi + `GRUPPO`). Resta da filtrare i personaggi con `puoEquipaggiare(artefatto)`. Oggi con un solo personaggio vivo lo sceglie da sé: con il filtro, "un solo candidato" non basterà più a saltare la domanda, perché c'è sempre anche `GRUPPO`.
- **Nessun candidato:** l'oggetto va direttamente nel gruppo.
- **Cofano:** nuovi esiti rari, una pergamena o un artefatto.
- **Test:** il loot finisce nel posto giusto; candidati esclusi per slot, livello, morte; nessun candidato → gruppo.

### Fase 5: venditore di pergamene e magazzini
- Nuovo stato di `Citta`, come `DA_ARMAIOLO`, che riusa `AutomaAcquistiArtefatti` e `DisplayableCanvasScambiatoreArtefatti` ma tratta solo pergamene.
- `RegistroArtefattiMD.artefattiPerLocazione` diventa una mappa per (coordinate, tipo di inventario), con salvataggio e test.

### Fase 6: incantatore
- **Schermata** sulla base dello scambiatore:
  - a sinistra l'inventario del gruppo;
  - a destra il banco di lavoro, con al massimo 1 artefatto incantabile e le pergamene, fino al limite dell'artefatto.
  - Uscendo, quello che resta sul banco torna nel gruppo: il banco non si salva.
- **Conferma** → `ComandoIncantatura`. `GruppoGiocatore` ricontrolla le regole (§2), fa pagare, copia gli incantamenti, distrugge le pergamene e risponde con `NotificaApprovazioneIncantatura` o `NotificaRifiutoIncantatura` (con il motivo).
- **Nome proprio** chiesto con il `Prompt`, già compilato con quello attuale.
- **Test:** fusione riuscita; ciascun rifiuto; il caso "livello 3 con 1 incantamento → al più 1 in più"; il tetto di 5.

### Fase 7: combattimento

Le regole sono in §2, "Combattimento". Da fare in `CalcolatoreCombattimento.calcolaDannoRisultante`:
- **Resistenze:** calcolo di `difesa_T` e suo uso nella mitigazione del danno base e degli incantamenti di tipo T.
  - Esempio atteso per il test: difesa base 30; armatura di livello 3 con un incantamento di fuoco "medio" (+10, +10%). Allora `difesa_FUOCO` = (30 + 30) × 1,1 = 66, e il danno di fuoco passa dal 77% al 60%.
- **Doppia arma:** seconda fase di attacco al 60% (danno base e incantamenti) e −25% di `PARATA`.
- **Armi a due mani:** −25% di `PARATA` (il +50% di danno base sta nei valori dell'arma, dati dal generatore).
- **Scudo:** `PARATA` intrinseca in base al livello.
- **Libro magico:** bonus al danno degli incantesimi. Si applica dove gli incantesimi calcolano il danno (`LocazioneBase` → `calcolaDannoRisultante` con `IncantesimoMalefico`).
- **Test** per ciascuno. Poi un giro di `TestMonteCarloMatrix` / `CombatSimulatorMatrix` prima e dopo, per vedere l'effetto sul bilanciamento.

## 5. Bug e problemi trovati

1. ~~**Crash scegliendo `ANNULLA` quando si raccoglie un oggetto.**~~ Corretto con `SCELTA_DESTINATARIO_OGGETTO` (§3).
2. ~~**`addPozioniMagiaGrande` pubblica la notifica delle pozioni normali.**~~ Corretto. Lo sprite +/− compariva sull'icona delle pozioni normali.
3. ~~**Inventario dell'alchimista:** le pozioni di magia grandi dipendevano da quelle normali.~~ Corretto.
4. ~~**La fuga non faceva perdere le pozioni di magia grandi.**~~ Corretto, con un TODO in `GruppoGiocatore.fugge` perché la fuga è troppo penalizzante.
5. **Formato di salvataggio fragile** (rimandato; la correzione qui sotto è quella scelta). `StringTokenizer` salta i campi vuoti e usa `|` come separatore: un nome o una descrizione vuoti, o che contengono `|`, spostano tutti i campi seguenti. Correzione proposta, in due pezzi piccoli:
   - **alla fonte:** il `Prompt` (e poi il generatore) non accetta `|` nei testi, e un nome vuoto diventa "nessun nome". Copre tutto ciò che arriva dal giocatore.
   - **in lettura:** un piccolo helper in `modellodati` (es. `LettoreCampi`) che divide la riga con `split("\\|", -1)`, che conserva i campi vuoti, al posto di `StringTokenizer`; e un `scriviTesto`/`leggiTesto` che rappresenta il testo vuoto con `-`, come già si fa per le note.
   - Non serve l'escape di `|`, se alla fonte non entra.
6. ~~**BERSERK applicato al contrario.**~~ Corretto. In `CalcolatoreCombattimento.calcolaDannoRisultante`, §2.5, la condizione era `dannoNonFisico && …`, mentre `TipoEffettoDiStato.BERSERK` e il commento dicono che scala il danno **fisico**. Ora è `!dannoNonFisico`, coperto da `CalcolatoreCombattimentoBerserkTest`. Siccome i guerrieri usano quasi sempre armi fisiche, finora il BERSERK non si applicava praticamente mai: ora che funziona, il combattimento dei guerrieri feriti diventa più forte, e conviene tenerlo presente nel bilanciamento.

Non sono bug ma scelte da rivedere: l'armaiolo **ricompra a prezzo pieno** (vedi TODO) e ha il magazzino vuoto. Il blocco di debug con 9999 monete in `GruppoGiocatore.reimposta` e gli artefatti "PER TEST" in `Automa.inizializzaGioco` sono già segnati con FIXME.

## 6. Dubbi ancora aperti

- **Riempimento dei negozi** (venditore di pergamene e armaiolo). Oggi un negozio nuovo parte vuoto e vende solo ciò che gli vende il giocatore. Si decide dopo aver scritto `GeneratoreArtefatti` (fase 3). Le possibilità:
  - generare il magazzino una volta sola, alla creazione del mondo;
  - rigenerarlo periodicamente (es. ogni N giorni) in base al livello del gruppo.
- **Valori di partenza da proporre durante l'implementazione**, da scrivere qui e poi correggere in gioco:
  - quanta `PARATA` intrinseca dà lo scudo per ogni livello;
  - la probabilità che un cofano dia una pergamena o un artefatto (es. 5% ciascuno);
  - da quale livello il generatore passa dal grado minore al medio e al maggiore;
  - se la seconda arma colpisce lo stesso bersaglio della prima o uno a caso.
- **Da riguardare con la resa grafica:** la presentazione del nome proprio nell'inventario.

## 7. TODO

- [ ] Rifornimento dei magazzini di armaiolo e venditore di pergamene, dopo `GeneratoreArtefatti` (§6); rivendita a prezzo ridotto invece che pieno.
- [ ] Tenere d'occhio il bilanciamento dei guerrieri: il BERSERK ora funziona davvero (§5.6), e i guerrieri feriti colpiscono più forte.
- [ ] Il nome "Pergamena" va generato a caso come i nomi delle locande (runa, sigillo, …).
- [ ] Grammatica per `GeneratoreArtefatti` (formato da definire), sul modello delle locande.
- [ ] I personaggi del gruppo non si vedono a video: quando ci saranno, i rifiuti (peso, slot, livello) andranno mostrati anche lì, per esempio con un fumetto sul personaggio. C'è un TODO in `Artefatto.consegna`.
- [ ] Immagine del venditore di pergamene (per ora quella dell'alchimista).
- [ ] Negozi sparsi nella foresta: un paio per tipo, tra armaiolo, alchimista e incantatore.
- [ ] Bilanciamento di prezzi e gradi delle pergamene (§8).
- [ ] Rivedere **tutti** i prezzi del gioco (artefatti, pozioni, incantesimi, pergamene, fusione) alla luce del loot che ora si può trovare: con spade, scudi, anelli e pergamene raccolti in giro, l'economia cambia.
- [ ] Rivedere la fuga, oggi troppo penalizzante (TODO in `GruppoGiocatore.fugge`).
- [ ] Nuove idee per incantare gli accessori (per ora non si incantano).

## 8. Gradi e prezzi delle pergamene

Riferimenti dell'economia attuale:
- artefatti dei templi 10-25 monete;
- pozioni 5-30;
- incantesimi 5-15;
- mappa 10-20.

I bonus vanno a gradini, come proposto:

| Grado | Bonus fisso | Coefficiente | Prezzo base |
| :--- | ---: | ---: | ---: |
| Minore | +5 | +5% | 15 |
| Medio | +10 | +10% | 30 |
| Maggiore | +15 | +20% | 50 |

- **Formula del prezzo:** `2 × bonus fisso + coefficiente in punti percentuali`, che dà esattamente i prezzi della tabella (2 × 5 + 5 = 15, 2 × 10 + 10 = 30, 2 × 15 + 20 = 50).
- **Effetti di stato:** +25% di prezzo per i `TipoDanno` che li hanno (`hasEffettiDiStato()`: FUOCO, GELO, VELENO…).
- **Rivendita** (anche per l'armaiolo): 50% del prezzo.
- **Solo fisso o solo percentuale:** una pergamena generata a caso può avere una sola delle due parti, e la formula funziona lo stesso. Per esempio una "media" con solo +10 costa 20, una con solo +10% costa 10.
- **`GradoIncantamento`:** un enum con bonus, coefficiente e prezzo per grado.
  - Il generatore sceglie il grado in base al livello di riferimento, per esempio solo minori ai primi livelli.
  - La grammatica lo usa anche per il **nome**: semplice per i gradi bassi ("una pergamena del fuoco"), altisonante per quelli alti ("il Sigillo della Fiamma Eterna").

Conti di esempio con la fusione:
- una spada di livello 3 con un incantamento medio: 30 (pergamena) + 15 (fusione) = 45 monete;
- una di livello 6 con cinque maggiori: 250 + 35 = 285 monete.

Il bonus fisso scala con il livello dell'arma (`bonus × livello`), il coefficiente con l'Intelligenza di chi colpisce. Per questo il fisso rende di più sulle armi alte e il percentuale sui maghi.

I numeri sono di partenza: il bilanciamento è un TODO (§7), e i valori stanno in `Costanti`, quindi si cambiano senza toccare la logica.
