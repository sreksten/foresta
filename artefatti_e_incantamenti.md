# Artefatti, pergamene e incantatore: piano di lavoro

> Stato: aggiornato al 2026-09-25. Tutte le fasi (1-7) fatte, bilanciamento delle classi in corso, prima grammatica degli artefatti (solo spade, §7) in prova; resta quel che è in "Da fare", qui sotto. Poi le decisioni prese, quel che è fatto, le fasi di lavoro, i bug trovati e i prezzi delle pergamene.
> I salvataggi **non** devono restare retrocompatibili: il formato si cambia liberamente, ma ogni modifica va coperta da test di salva/rileggi.

## Da fare

### Bilanciamento del combattimento

Si misura con il simulatore (`TestMonteCarloMatrix.testConfrontoEquipaggiamenti` per armi e armature, `testConfrontoClassi` per le classi; vedi `risorse_e_documenti_vari/piano_montecarlo_matrix.md`, §11-§13).

- [ ] **Incantamenti:** uno di grado medio alza il danno del 66%, e la spada di fuoco batte ogni altro equipaggiamento (`piano_montecarlo_matrix.md`, §11.4).
- [ ] **Classi:** capire cosa porterebbe un giocatore a preferire un Guerriero, un Ladro, un Elfo, un Bardo o un Mago (a parte i gusti personali), e bilanciarle fra loro. Oggi il Guerriero è avanti in mischia e Ladro ed Elfa si somigliano (`piano_montecarlo_matrix.md`, §12). L'idea di fondo:
  - **tutti lanciano incantesimi**, perché basta consumare la pergamena su cui sono scritti; ma il **Mago** deve fare con la magia danni di gran lunga superiori a quelli di chiunque altro;
  - l'**Elfo** combatte con più agilità del Mago ed è un po' meno bravo con la magia;
  - il **Guerriero** con gli incantesimi, salvo colpi di fortuna, fa poco, ma in mischia con spada e scudo o spadone è il più forte;
  - il **Ladro** agisce con destrezza: doppia arma, niente armature pesanti;
  - il **Bardo** fa da supporto al gruppo.

  Il gioco è fracassone: non deve essere frustrante, e i mostri stanno al livello del mondo, cioè del capo del gruppo.

  Fatto finora (dettagli in "Fatto" e in `piano_montecarlo_matrix.md`, §12-§13): blocchi di equipaggiamento per classe e `FORZA` minima per l'armatura, moltiplicatori di danno delle classi collegati al combattimento, dardo arcano per Mago ed Elfo, e nel simulatore incantesimi, dotazioni tipiche e scenari più duri. Ultimi ritocchi (2026-09-25), **non ancora misurati**: il dardo costa all'Elfo 4 di `MAGIA` invece di 2, e al Mago fa 40 × livello invece di 30. I moltiplicatori fisici di Ladro e Bardo restano quelli di prima (1,0 e 0,9).

  Da fare:
  - [ ] **Rilanciare `testConfrontoClassi`** con gli ultimi ritocchi (la prova è stata interrotta) e aggiornare `piano_montecarlo_matrix.md`, §13.
  - [ ] **Elfo:** era diventato la classe più forte (dardo arcano più due armi); vedere se il dardo più caro basta.
  - [ ] **Mago:** deve essere "di gran lunga" il migliore con la magia; vedere se basta il dardo più forte.
  - [ ] **Ladro:** senza pergamene regge poco contro i gruppi. Il suo moltiplicatore fisico resta allo standard, 1,0: si sistemerà più avanti, in altro modo. Idem il Bardo, che resta a 0,9.
  - [ ] **Bardo:** il suo ruolo di supporto del gruppo (magia su più bersagli, recupero magico, carisma) nel combattimento non c'è ancora, e il simulatore non lo vede.
  - [ ] **Obiettivi:** fissare per ogni classe, con la sua dotazione, una fascia di vittorie negli scenari alla pari; tre mostri contro un PG solo servono solo a vedere le differenze, perché nel gioco il gruppo ha più personaggi.
  - [ ] **Ombrafiamma:** per ora senza limiti di equipaggiamento; il suo ruolo è da definire.
- [ ] **Guerrieri:** tenerli d'occhio, perché il BERSERK ora funziona davvero (§5.6) e i guerrieri feriti colpiscono più forte.

### Economia

- [ ] **Pergamene:** bilanciare prezzi e gradi (§6).
- [ ] **Tutti i prezzi del gioco** (artefatti, pozioni, incantesimi, pergamene, fusione), alla luce del loot che ora si trova in giro: con spade, scudi, anelli e pergamene raccolti per strada l'economia cambia.
- [ ] **Magazzini dei negozi:** rifornimento periodico (oggi si riempiono una volta sola, alla creazione del mondo). La rivendita a prezzo ridotto c'è: la regola la `CONTRATTAZIONE` (vedi "Prezzi e contrattazione").
- [ ] **Scudi rari** più frequenti (oggi il 10% degli artefatti incantabili è raro), e forse scudi con `RESISTENZA_MAGICA` come modificatore.

### Contenuti nuovi

- [ ] **Artefatti leggendari**, scritti a mano, da mettere nei templi e come premi delle missioni (§2, "Rarità").
- [ ] **Negozi sparsi nella foresta:** un paio per tipo, tra armaiolo, alchimista e incantatore.
- [ ] **Grammatica per `GeneratoreArtefatti`:** formato definito e prima versione per la spada (§7), metà delle spade generate vengono da lì. Da fare:
  - [ ] **Rileggere le liste** di `artefatti2.txt`: alcune parole ereditate da `artefatti.txt` non reggono come prefisso o aggettivo ("la rasoio spada", "la postale spada", "la spada tank").
  - [ ] **Stesso attributo due volte** (es. `SOGGEZIONE` +2 e +1): sommarli in `generaDaGrammatica` o evitarli nella grammatica.
  - [ ] **Bilanciamento:** i modificatori sono intensità × gradino, quindi +6 al livello 10 per un'intensità +2; da misurare insieme alla quota di spade da grammatica (`Costanti.ARTEFATTO_PROBABILITA_DA_GRAMMATICA`, 0,5 per provarla) e ai prezzi.
  - [ ] **Altri tipi:** spadone, mazza, ascia, scudo… (basta aggiungere le radici `<TIPO>_<n>`, §7), e poi le pergamene.
  - [ ] **`artefatti.txt`** resta com'è, come banco di prova di `GrammarBean` (`GrammarBean.md`, §5.3): da togliere quando `artefatti2.txt` l'avrà sostituito.
- [ ] **Nome delle pergamene:** "Pergamena" va generato a caso come i nomi delle locande (runa, sigillo, …).
- [ ] **Accessori:** nuove idee per incantarli (per ora non si incantano).

### Grafica e interfaccia

- [ ] **Immagini degli oggetti per terra:** spada e scudo si trovano nelle locazioni, ma elmo e armatura sono commentati nel `Bosco` finché mancano `oggetti/Elmo.gif` e `oggetti/Armatura.gif`, e lo spadone non ha nemmeno un oggetto (potrebbe riusare l'immagine della spada). Oggi questi pezzi si hanno solo dai cofani (5% di artefatto casuale), dall'armaiolo e dai templi: le dotazioni tipiche del simulatore sono più complete di quel che un giocatore trova davvero.
- [ ] **Icona del dardo arcano:** per ora il comando `DARDO_ARCANO` usa l'icona generica degli incantesimi (`icone/Incantesimo.gif`, TODO in `ClasseIcona`).
- [ ] **Immagine dell'incantatore** (`img/personaggi/Incantatore.gif`): è 31×70, metà delle altre (il venditore è 62×140), va ingrandita.
- [ ] **Nome proprio nell'inventario:** come presentarlo, da riguardare con la resa grafica.
- [ ] **Rarità a video:** come mostrare nell'inventario che un artefatto è raro o leggendario (colore, dicitura…).
- [ ] **Rifiuti sui personaggi:** i personaggi del gruppo non si vedono a video. Quando ci saranno, i rifiuti (peso, slot, livello) andranno mostrati anche lì, per esempio con un fumetto sul personaggio. C'è un TODO in `Artefatto.consegna`.

### Altro

- [ ] **Fuga:** oggi troppo penalizzante (TODO in `GruppoGiocatore.fugge`).
- [ ] **Codice di debug** da togliere: il blocco con 9999 monete in `GruppoGiocatore.reimposta` e gli artefatti "PER TEST" in `Automa.inizializzaGioco` (segnati con FIXME).


## 1. Obiettivo

- Gli **incantamenti** diventano oggetti a sé: si trovano come loot, si comprano e vendono, e con un **incantatore** si trasferiscono in modo permanente su un artefatto.
- Il **loot** della foresta (spade, scudi, elmi, armature, anelli, cofani) produce artefatti veri, che i personaggi usano come armi ed equipaggiamento e che si possono passare da un personaggio all'altro.
- Gli artefatti possono avere un **nome proprio** (es. una spada di fuoco chiamata "Diavolina").

## 2. Decisioni prese

### Modello

- **Pergamena.** Un incantamento-oggetto è un `Artefatto` di tipo `TipoArtefatto.INCANTAMENTO` (supertipo `INCANTAMENTO`, slot `NUCLEO`), chiamato per ora "Pergamena". Come ogni artefatto porta sia una lista di `ModificatoreAttributo` sia una lista di `Incantamento`: sono gli effetti che trasferisce (verificato con `ArtefattoMDTest.salvaERicaricaPergamenaConModificatoriEIncantamenti`). Finché resta nel gruppo i suoi modificatori non si applicano a nessuno, perché contano solo quelli dell'inventario personale. `Incantamento` resta l'oggetto valore che descrive l'effetto: nome, `TipoDanno`, bonus fisso, coefficiente. Così la pergamena riusa inventari, compravendita e salvataggio degli artefatti.
- **Nome proprio.** Facoltativo, su `ArtefattoMD`. Se lo sceglie il giocatore si normalizza con le iniziali maiuscole (es. "lama del drago" → "Lama Del Drago"), nel momento in cui il testo è disponibile. Se lo costruisce il codice resta com'è scritto.
  - È **un solo campo in più** (`nomeProprio`). La forma completa si compone con i campi che ogni artefatto ha già, `nome` (es. "la spada di fuoco", con l'articolo) e `descrizione` (es. "che brucia i nemici"): "Diavolina, la spada di fuoco, che brucia i nemici". Senza nome proprio resta com'è oggi: "la spada di fuoco, che brucia i nemici".
  - **Nei testi** si usa sempre la forma completa:
    - la descrizione dei personaggi del comando `AIUTO` (`PersonaggioBase.getDescrizione`);
    - il "qui c'è …" della locazione;
    - le informazioni sui templi;
    - i messaggi di azione ("Pippo raccoglie Diavolina, la spada di fuoco, che brucia i nemici.").
  - **Nell'inventario** (`DisplayableCanvasScambiatoreArtefatti`) il nodo dell'artefatto mostra in grande il nome proprio, oppure il `nome` se non c'è. La descrizione compare in piccolo, come nodo secondario. Con il nome proprio conviene che il nodo piccolo sia "la spada di fuoco, che brucia i nemici", così il nome generico non si perde. Da riguardare con la resa grafica.
  - La **grammatica** che genera le armi a caso passa anche il nome proprio, facoltativo.
- **Pergamene generate a caso.** Hanno un livello da 1 a 3 e **tanti effetti quanto il loro livello**. Ogni incantamento può avere solo la parte fissa, solo la parte percentuale, o entrambe.
- **Livello di una pergamena.** Non ne limita l'uso: il limite sta sull'oggetto incantato (numero massimo di effetti, cioè incantamenti più modificatori).

### Equipaggiamento

- **Inventario personale.** È la lista degli artefatti che il personaggio porta con sé, cioè il suo equipaggiamento; quelli del gruppo ne sono esclusi. Tutto ciò che è nell'inventario personale applica i suoi modificatori, come oggi.
- **Slot.** Ogni `TipoArtefatto` ha il suo `SlotArtefatto` (`getSlotArtefatto()`).
  - Al massimo 1 artefatto per `TESTA`, `CORPO`, `MANO_PRINCIPALE` e `MANO_SECONDARIA`.
  - Nessun limite su `ACCESSORIO` (anelli, talismani, ninnoli).
  - `NUCLEO` (le pergamene): un personaggio non può prenderlo, resta nel gruppo con un messaggio o fumetto.
  - `ENTRAMBE_LE_MANI` (nuovo valore di `SlotArtefatto`), per le **armi a due mani**: occupa sia la mano principale sia la secondaria.
    - Chi impugna un'arma a due mani non può equipaggiare scudo, libro magico o seconda arma finché non la ripone nell'inventario del gruppo. Viceversa, non può prendere un'arma a due mani se ha qualcosa in una delle due mani.
    - Il rifiuto si avverte con un fumetto.
- **Equipaggiamento secondo la classe.** Armi, scudo e libro seguono una tabella per classe (`RegoleEquipaggiamento.puoUsare`); le versioni femminili seguono le maschili, le classi fuori tabella (i mostri, l'Ombrafiamma) non hanno limiti. Elmo, veste e accessori li portano tutti.

  | Classe | Armi, scudo e libro |
  | :--- | :--- |
  | Guerriero/Guerriera | spada, spadone, mazza, ascia, lancia, scudo |
  | Ladro/Ladra | spada, mazza, ascia (anche due insieme) |
  | Elfo/Elfa | spada, mazza, ascia (anche due insieme), lancia |
  | Bardo/Cantastorie | spada, scudo |
  | Mago/Maga | bastone magico, libro magico |

  Il rifiuto è `MotivoRifiutoEquipaggiamento.NON_ADATTO_ALLA_CLASSE` ("non sa usare questo genere di oggetti").
- **Armatura e FORZA.** L'armatura chiede una `FORZA` di almeno 16 (`Costanti.ARMATURA_FORZA_MINIMA`), invece di un divieto per classe: il Guerriero ce l'ha sempre (17 a livello 1), Ladro, Elfo e Bardo solo se forzuti ai livelli alti (11-14 a livello 1, fino a 19 a livello 10), il Mago mai (5-11). Il rifiuto è `FORZA_INSUFFICIENTE`.
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
- **Anelli non magici.** Restano come oggi, senza effetto. Più avanti si potranno trattare come gemme, da vendere.
- **Elmo e armatura.** Diventano loot a sé, come spada e scudo. Per ora però non si generano, perché mancano le immagini: il codice c'è ma resta commentato.
- **Morti.** Sempre esclusi, come già oggi.
- **Scelta.** Si propongono i candidati più **`GRUPPO`** (comando e icona esistevano già).
  - **Un solo candidato:** l'oggetto va direttamente a lui, senza domanda.
  - **Nessun candidato:** l'oggetto va direttamente nell'inventario del gruppo, con un messaggio. È anche il caso di chi avrebbe già occupato lo slot (spada, scudo, elmo, armatura): niente sostituzioni automatiche, per non dargli robaccia.
- **Troppo carico.** Se il personaggio scelto è troppo carico, l'oggetto va nel gruppo con il messaggio già in uso (fatto).
- **Pergamene.** Vanno sempre nel gruppo (slot `NUCLEO`).
- **Cofano.** Per ogni cofano aperto: 5% una pergamena, 5% un artefatto casuale, quindi il 10% delle volte si trova l'uno o l'altro. Gli altri esiti restano quelli di oggi.
- **Nomi.** Il cofano trova già delle "pergamene" di incantesimi: nessun problema, perché con la grammatica gli incantamenti non si chiameranno più "Pergamena".
- **Monete e gemme.** Restano risorse del gruppo.

### Incantamenti ed effetti

- **Cosa si incanta.** Armi, scudi, elmi, armature.
  - Gli **accessori** (anelli, talismani, ninnoli) non si incantano: tengono solo i loro modificatori di attributo.
  - Il **libro magico** non si incanta: è già una fonte di magia, e dà di suo un bonus simile a un incantamento, un po' più alto (vedi "Combattimento").
- **Sulle armi.** Danno aggiuntivo, come oggi.
- **Sui pezzi difensivi.** Resistenza contro gli attacchi di quel `TipoDanno`, con una parte fissa e una percentuale, come il danno (vedi "Combattimento").

### Combattimento

- **Dardo arcano.** L'incantesimo innato di Mago ed Elfo (`DardoArcano`): danno `ARCANO` su un solo bersaglio, che non consuma pergamene. Al Mago fa 40 × livello di danno base e costa 2 di `MAGIA`; all'Elfo, un po' meno bravo con la magia e in cambio capace di combattere, fa 30 × livello e costa 4 (`Costanti.DARDO_ARCANO_*`). Una pergamena di fuoco fa 60 × livello. Il danno segue le regole degli incantesimi: moltiplicatore magico della classe (il Mago fa più dell'Elfo), bonus del libro magico. Non è fra le `ClasseIncantesimo`, perché non è una pergamena: non si compra, non si trova e non si conta nell'inventario del gruppo. In combattimento si lancia con il comando degli incantesimi: chi lo conosce e ha la `MAGIA` trova `DARDO_ARCANO` fra le scelte, e colpisce il primo avversario vivo.
- **Danno delle pergamene.** `Costanti.INCANTESIMO_FATTORE_DANNI` (oggi 1,0) scala il danno base di tutti gli incantesimi, dardo compreso. Abbassarlo punisce soprattutto il Mago, che vive di pergamene: le pergamene sono poche, quindi restano forti, e il loro peso va rivisto con l'economia.
- **Moltiplicatori di classe.** Il danno base si moltiplica per il `*_MOLTIPLICATORE_DANNI_FISICI` della classe di chi colpisce se l'attacco è fisico, per il `*_MOLTIPLICATORE_DANNI_MAGICI` se è elementale o magico (incantesimi compresi, e gli attacchi naturali di mostri come Spettro o Viverna). Negli incantamenti delle armi il moltiplicatore magico vale solo per la parte percentuale, che scala sull'`INTELLIGENZA`: la parte fissa è dell'arma e vale per tutti. Il bonus del libro magico prende tutto il moltiplicatore magico, perché è danno dell'incantesimo. Valori: Mago 0,5 fisico e 2,0 magico, Guerriero 1,3 e 0,5, Elfo 0,9 e 1,4, Bardo 0,9 e 1,1, Ladro 1,0 e 1,0. Ladro e Bardo reggono poco contro i gruppi, ma i loro moltiplicatori restano questi: si sistemeranno più avanti, in altro modo.
- **Resistenze.** Entrano nella formula a rendimenti decrescenti che c'è già, `danno × 100 / (100 + difesa)`, senza un tetto separato: la difesa non porta mai all'immunità.
  - La somma è su tutti gli incantamenti di tipo T di elmo, scudo e armatura del difensore. La difesa base è quella di oggi: `COSTITUZIONE + PARATA` per il danno fisico, `RESISTENZA_MAGICA` per quello elementale o magico.
  - `difesa_T` si usa nella mitigazione del danno base (se l'arma è di tipo T) e in quella di ogni incantamento di tipo T dell'attaccante.
  - Sui pezzi difensivi la parte percentuale vale la **metà** (`Costanti.RESISTENZA_FATTORE_PERCENTUALE`), perché moltiplica una difesa che cresce già con il livello. Quindi `difesa_T = (difesa base + Σ fisso_T × livello del pezzo) × (1 + Σ percentuale_T / 2)`.
- **Doppia arma** (Ladro/Ladra, Elfo/Elfa con un'arma in `MANO_SECONDARIA`): **due fasi di attacco** per turno.
  - La seconda è con l'arma secondaria, al **40%**: sia il danno base sia i suoi incantamenti (era 60%, abbassato col bilanciamento). Colpisce lo **stesso bersaglio** della prima, o il prossimo vivo se la prima l'ha ucciso.
  - L'arma secondaria occupa lo slot `MANO_SECONDARIA` (`slotEquipaggiamento`), anche se il suo `TipoArtefatto` dice `MANO_PRINCIPALE`.
  - "Guardia aperta": **−25% di `PARATA`** finché si impugnano due armi. Senza scudo, poi, non si ha la parata dello scudo.
- **Scudo.** Oltre ai modificatori scritti sull'artefatto, ha una **`PARATA` intrinseca** di 6 + 2 per livello (`Costanti.SCUDO_PARATA_*`) e una **`RESISTENZA_MAGICA` intrinseca** di +1 per livello, +2 se è raro o leggendario (`Costanti.SCUDO_*RESISTENZA_MAGICA_PER_LIVELLO`). Così la scelta tra scudo e seconda arma conta sempre, anche con scudi "spogli", e anche contro gli attacchi elementali e magici, che la `PARATA` non ferma. Per una protezione magica forte lo scudo è un ottimo candidato per un incantamento.
- **Elmo e armatura.** Anche loro hanno una **`PARATA` intrinseca** con una parte fissa, perché servano anche ai livelli bassi e senza incantamenti: elmo 2 + 1 per livello, armatura 3 + 1 per livello (`Costanti.ELMO_PARATA_*`, `Costanti.ARMATURA_PARATA_*`). La veste, l'armatura di chi usa la magia, dà lo stesso minimo dell'armatura in `RESISTENZA_MAGICA` invece che in `PARATA`. Il +5% di `PARATA` per livello che il generatore scrive sui pezzi difensivi resta, ma su una `PARATA` di base di 2-6 pesa poco.
- **Libro magico.** Bonus al **danno degli incantesimi** di chi lo porta, con parte fissa e parte percentuale come un incantamento del `GradoIncantamento` del livello del libro, **+25%**, del tipo di danno dell'incantesimo. La parte fissa scala con il livello del libro. Non dà effetti di stato in più e non aiuta le armi. Occupa `MANO_SECONDARIA`, quindi il mago sceglie tra libro e scudo.

### Prezzi delle pergamene

Vedi la tabella dei gradi in §6. Formula: `2 × bonus fisso + percentuale`; +25% per i tipi di danno con effetti di stato; rivendita al 50%. I numeri sono di partenza e vanno bilanciati (vedi "Da fare").

### Fusione (incantatore)

- **Cosa si trasferisce.** Tutti gli effetti della pergamena: gli incantamenti **e** i modificatori di attributo (`ModificatoreAttributo`). Nel seguito "effetto" vuol dire l'uno o l'altro.
- **Costo.** 10 monete + 5 per ogni effetto trasferito (incantamento o modificatore). Il costo si sconta con la `CONTRATTAZIONE` del gruppo, come gli acquisti.
- **Limite.** Un artefatto ha un numero massimo di effetti **in totale** che dipende dalla sua rarità (vedi "Rarità"): per un comune `min(3, livello − 1)`. Si contano incantamenti e modificatori, **compresi** quelli che l'artefatto ha già di suo (es. i modificatori degli artefatti dei templi). A livello 1 non se ne hanno. Una spada di livello 3 con già 1 incantamento ne può ricevere al più un altro.
- **Conteggio.** Si contano gli **effetti**, non le pergamene: una pergamena con un incantamento e un modificatore vale due, per il costo e per i limiti.
- **Accessori.** Non si incantano, quindi non ricevono nemmeno i modificatori di una pergamena: la fusione vale solo per gli artefatti incantabili (`isIncantabile()`).
- **Controlli.** La schermata impedisce di mettere sul banco più effetti di quanti l'artefatto ne possa ricevere (rifiuto con fumetto), e il motore ricontrolla tutto alla conferma.
- **Rifiuti.** Nessun artefatto sul banco, più di un artefatto, nessuna pergamena, limite superato, monete insufficienti.
- **Incantamenti uguali.** Restano **distinti** anche se hanno lo stesso `TipoDanno`, così non si aggira il limite; i bonus si sommano comunque. Le pergamene usate vengono distrutte.
- **Nome proprio.** Si chiede a **ogni** fusione con il `Prompt`, proponendo come valore predefinito quello che l'artefatto aveva già, se ce l'aveva.
- **Da dove si prende l'artefatto.** Solo dall'inventario del gruppo: se ce l'ha un personaggio, prima va riposto.
- **Comandi.** In città `Comando.INCANTATORE` (icona `img/icone/Incantatore.gif`) apre la bottega; dentro, `Comando.FUSIONE` (icona `img/icone/Fusione.gif`) conferma la fusione.

### Negozi e generatore

- **Dove.** In città, in quest'ordine nella barra delle icone: locanda, alchimista, armaiolo, venditore di pergamene, incantatore. I negozi sparsi nella foresta vengono dopo.
- **Cosa trattano.** L'armaiolo compra e vende tutto tranne le pergamene; il venditore di pergamene solo quelle (`TipoNegozio.tratta`). Un rifiuto si avverte con un fumetto.
- **Prezzi e contrattazione.** Il gruppo tratta con la `CONTRATTAZIONE` più alta tra i personaggi vivi (attributo secondario: 60% Carisma, 40% Fortuna, per il moltiplicatore di archetipo; il Ladro è il migliore). `RegoleContrattazione` ne ricava `bonus = c / (c + 4)`: gli acquisti (artefatti, consumabili, fusione) si scontano di `0,24 × bonus`, al massimo del 20%; le vendite rendono `0,50 + 0,30 × bonus` del costo, al massimo il 75%. Il peggior acquisto (80%) resta sopra la miglior vendita (75%), quindi comprando e rivendendo non si guadagna. Arrotondamenti a favore del mercante. Lo Scudo Fiscale (leggendario, +16 `CONTRATTAZIONE`) porta un Ladro esattamente ai due limiti.
- **Magazzini.** La chiave è (coordinate, `TipoNegozio`), perché più negozi della stessa città hanno la stessa coordinata.
- **Generatore.** `GeneratoreArtefatti`: nomi ed effetti dalle tabelle di `GeneratoreArtefattiTabelle` e, per i tipi che la conosce (per ora la spada), in parte da una grammatica (§7).
- **Riempimento dei negozi** (venditore di pergamene e armaiolo). Per ora il magazzino si genera **una volta sola, alla creazione del mondo**: 6 artefatti e 6 pergamene per città, con livelli a rotazione da 1 a 3 (`Costanti.MAGAZZINO_*`), perché alla creazione il livello di riferimento è sempre 1. Si potrà passare poi a una rigenerazione periodica in base al livello del gruppo.
- **Gradi per livello.** Livelli 1-3 minore, 4-7 medio, dall'8 in su maggiore (`Costanti.GRADO_INCANTAMENTO_*`, da riaggiustare).
- **Pergamene generate.** Per ora, per le prove, incantamenti e modificatori a caso; poi ci penserà la grammatica. Il livello della pergamena è quello di riferimento limitato a 3, e dà il numero di effetti; il grado dipende dal livello di riferimento.
- **Artefatti che nascono incantati.** Ogni tanto il generatore produce un artefatto incantabile già incantato, tanto più spesso quanto più è alto il livello: 5% per ogni livello oltre il primo, fino al 60% (`Costanti.ARTEFATTO_PROBABILITA_INCANTATO_*`). Gli incantamenti sono del grado del livello e rispettano il limite di effetti, che conta anche i modificatori dell'artefatto. Il prezzo cresce come quello delle pergamene.
- **Artefatti che nascono incantati: tetto.** Al massimo 3 incantamenti, e sempre almeno un posto libero nel limite di effetti, così il giocatore ha modo di migliorare l'artefatto con la fusione (gli incantamenti casuali non si tolgono). Quindi un comune nasce incantato solo dal livello 4 (dal 3 se è raro).

### Rarità

- **`RaritaArtefatto`** (`COMUNE`, `RARO`, `LEGGENDARIO`), campo di `ArtefattoMD`, salvato e riletto. Di default `COMUNE`.
- **Posti per gli effetti** (incantamenti più modificatori, `Artefatto.getEffettiMassimi()`):

  | Rarità | Posti | Tetto |
  | :--- | :--- | ---: |
  | Comune | livello − 1 | 3 |
  | Raro | livello | 4 |
  | Leggendario | livello + 1 | 5 |

  I tetti stanno in `Costanti.ARTEFATTO_MASSIMO_EFFETTI_*`: più di 5 effetti su un solo artefatto sarebbero troppi. Siccome si contano anche i modificatori propri, un comune che nasce con un modificatore può ricevere al più 2 incantamenti.
- **Dove si trovano.** Nel loot e nei negozi i comuni sono i più probabili, e un artefatto incantabile su dieci è raro (`Costanti.ARTEFATTO_PROBABILITA_RARO`). Gli accessori restano comuni, perché la rarità conta solo per i posti. I **leggendari non escono mai nel loot**: si trovano nei templi o come premio di una missione.
- **Leggendari** (da fare). Spade, scudi, elmi e altre armi di livello alto, scritti a mano come quelli dei templi, con un nome del tipo "La Leggendaria Spada del Fulmine con Rinterzo". Nascono con modificatori e incantamenti particolarmente potenti, anche oltre il grado maggiore e oltre i posti della loro rarità. Se hanno ancora posti liberi si possono incantare come gli altri.

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
- [x] **Fase 1 (modello).**
  - `ArtefattoMD.nomeProprio` (null se manca, oggi salvato come campo vuoto), con `normalizzaNomeProprio` per i nomi scelti dal giocatore (iniziali maiuscole, il resto com'è; vuoto → nessun nome). Da chiamare quando arriverà il `Prompt`.
  - Presentazione: `getNomeCompleto()` (forma completa, usata in `LocazioneBase`, `Informazioni`, `PersonaggioBase`, `Artefatto.prendi`/`riponiNelGruppo`/`consegna` e `Anello.prendi`), `getNomeBreve()` e `getDescrizioneBreve()` per l'inventario. Nei messaggi in cui l'artefatto è soggetto l'inciso si chiude con una virgola.
  - Nell'inventario il testo piccolo ora è la descrizione (o "nome, descrizione" con il nome proprio) al posto del tipo ("Spada"): da riguardare con la resa grafica.
  - `Artefatto.isIncantabile()` e `getEffettiMassimi()`, oggi secondo la rarità (vedi §2, "Rarità").
  - `SlotArtefatto.ENTRAMBE_LE_MANI`, `TipoArtefatto.SPADONE`; `TipoArtefatto.INCANTAMENTO` si presenta come "Pergamena".
  - `ArtefattoMD.slotEquipaggiamento` (null se non equipaggiato, oggi salvato come campo vuoto). Per ora nessuno lo imposta: è la fase 2.
  - Test: 10 nuovi in `ArtefattoMDTest` (salva/rileggi di nome proprio, slot, pergamene; forma completa; normalizzazione) e `ArtefattoIncantabileTest` (5). Tutta la suite è verde.
- [x] **Fase 2 (equipaggiamento a slot e livello).**
  - `Personaggio.puoEquipaggiare(Artefatto)` restituisce un `Optional<MotivoRifiutoEquipaggiamento>`: `TROPPO_CARICO`, `SLOT_OCCUPATO`, `PERGAMENA`, `LIVELLO_TROPPO_ALTO`, `SECONDA_ARMA_NON_CONSENTITA`, `MANI_OCCUPATE`, `ARMA_A_DUE_MANI_IMPUGNATA`. Ogni motivo ha la sua spiegazione ("Pippo non sa combattere con due armi.").
  - Le regole a slot stanno in `RegoleEquipaggiamento` (package `personaggi`); il peso lo controlla `PersonaggioBase`.
  - Lo usano il prelievo (`GruppoGiocatore`: la `NotificaRifiutoPrelievoArtefatto` porta il motivo, e il fumetto dell'inventario lo mostra) e `Artefatto.consegna` (messaggio "… resta nell'inventario del gruppo: Pippo …").
  - `PersonaggioBase.addArtefatto` imposta `slotEquipaggiamento`; `removeArtefatto` e `GruppoGiocatore.addArtefatto` lo azzerano. Se si aggiunge un artefatto senza controlli (es. gli artefatti "PER TEST" dell'`Automa`), prende lo slot del suo tipo.
  - `getArmaEquipaggiata()` legge l'arma in `MANO_PRINCIPALE` o `ENTRAMBE_LE_MANI`; nuovo `getArmaSecondaria()`. Riponendo l'arma principale la secondaria resta dov'è: la prossima arma presa va nella principale.
  - Il filtro dei candidati per il loot resta alla fase 4, quando il loot genererà l'artefatto prima della scelta: per ora chi non può prenderlo lo lascia nel gruppo, con il messaggio.
  - Test: `PersonaggioEquipaggiamentoTest` (17) e 2 nuovi in `ArtefattoPrendiTest` (livello troppo alto, seconda spada del guerriero). Tutta la suite è verde (255 test).
- [x] **Fase 3 (scheletro del generatore).**
  - Interfaccia `GeneratoreArtefatti` (`oggetti`), con `istanza()`, `generaArtefatto(tipo, livello)`, `generaArtefattoCasuale(livello)` (pergamene escluse) e `generaPergamena(livello)`. Chi chiama passa il livello di riferimento, `Statistiche.getLivello()`.
  - Implementazione `GeneratoreArtefattiTabelle`, con un `Random` iniettabile per i test. Nomi da tabelle ("la spada d'argento"); nome proprio nel 5% dei casi. Valori tarati sui templi:
    - costo `5 + 5 × livello`;
    - armi: danno `max(4 + 2 × livello, 11 + livello) ± 1` (`GeneratoreArtefatti.danniMediArma`), così ai livelli bassi un'arma fa più delle mani nude di un PG, che fanno 9-10 a ogni livello; dal livello 7 vale `4 + 2 × livello` come prima; lo spadone fa +50% di danno e di prezzo; il bastone fa metà danno e dà +5% di `MAGIA` per livello;
    - pezzi difensivi: +5% di `PARATA` per livello (la veste dà `RESISTENZA_MAGICA`);
    - libro magico: +5% di `MAGIA` per livello;
    - accessori: +livello fisso a un attributo (carisma, coraggio, valore, fortuna, percezione).
  - Pergamene: livello da 1 a 3 (quello di riferimento, limitato a 3) e tanti effetti quanto il livello, del grado del livello di riferimento, ciascuno a caso un incantamento (elementale o magico: solo fisso, solo percentuale o entrambi) o un modificatore di attributo (fisso di 1/2/3 secondo il grado, oppure percentuale come il coefficiente del grado).
  - `GradoIncantamento` (bonus, coefficiente, prezzo base, soglie) e `ListinoPergamene.prezzo(...)`, che applica le regole di §6 a incantamenti e modificatori. I valori stanno in `Costanti`.
  - Artefatti incantabili che nascono già incantati: probabilità `0,05 × (livello − 1)`, al massimo 0,6; da 1 incantamento fino allo spazio libero nel limite di effetti. A livello 2 non succede mai, perché il modificatore che l'artefatto ha di suo occupa già l'unico posto.
  - Nessuno lo chiama ancora: lo useranno il loot (fase 4) e i negozi (fase 5).
  - Test: `GeneratoreArtefattiTest` (11). Tutta la suite è verde (267 test).
- [x] **Fase 4 (loot).**
  - `Spada` e `Scudo` estendono il nuovo `OggettoArtefatto`, che genera l'artefatto nel costruttore, al livello di riferimento: così è già noto quando si sceglie chi lo prende. `Elmo` e `Armatura` ci sono, con le loro voci in `ClassiOggetto` e in `ClassiOggettoImmagine` (immagine `null`), ma restano commentati nell'elenco degli oggetti del `Bosco` finché mancano le immagini.
  - L'anello magico costruisce il suo artefatto nel costruttore; quelli non magici restano senza effetto.
  - Una sola logica di raccolta, `Artefatto.raccogli`, per loot, anelli e artefatti dei templi. I candidati sono i personaggi vivi che possono equipaggiare l'artefatto (`Artefatto.candidati`, con `puoEquipaggiare`). Nessun candidato: va nel gruppo con un messaggio (anche le pergamene); uno solo: va a lui; più di uno: sceglie il giocatore. Il messaggio usa il verbo del tipo ("Pippo impugna la spada d'argento, …").
  - `Oggetto.getArtefatto()` dice quale artefatto porta un oggetto; l'`Automa` lo usa per proporre in `SCELTA_DESTINATARIO_OGGETTO` solo i candidati più `GRUPPO`, e rifiuta un personaggio che non è fra i candidati.
  - `Cofano`: per ogni cofano, 5% una pergamena e 5% un artefatto casuale, che vanno nel gruppo (`Costanti.COFANO_PROBABILITA_*`).
  - Test: `LootTest` (10) e `ArtefattoPrendiTest` aggiornato. Tutta la suite è verde (291 test).
- [x] **Fase 6 (incantatore)**, fatta prima della 5 perché l'icona `Fusione` c'era già.
  - In città il comando `FUSIONE` (icona `img/icone/Fusione.gif`, in `ClasseIcona`) apre la bottega: `DisplayableCanvasIncantatore`, per ora con l'immagine dell'alchimista. A sinistra l'inventario del gruppo, a destra il `BancoDiLavoro`, che non si salva. Nella colonna centrale monete, costo della fusione e posti dell'artefatto ("Effetti 2/3").
  - Dentro, `FUSIONE` fonde e `ANNULLA` esce, rimettendo nel gruppo quel che è rimasto sul banco.
  - `AutomaIncantatore`: con il doppio clic si sposta dal gruppo al banco secondo `RegoleIncantatura.puoMettereSulBanco` (un solo artefatto, incantabile, mai oltre i posti), e un rifiuto è un fumetto (`NotificaRifiutoIncantatura`). Dal banco al gruppo si sposta sempre.
  - `FUSIONE`: `Citta` controlla le regole (`RegoleIncantatura.verifica`); se va, chiede il nome proprio con il `Prompt`, già compilato con quello attuale (`RichiestaTesto` con testo predefinito, `Prompt.mostra`). Il testo arriva alla città con il nuovo `Locazione.riceviTesto`, che pubblica `ComandoIncantatura`.
  - `GruppoGiocatore.incanta` ricontrolla, fa pagare (`Costanti.FUSIONE_COSTO_*`), copia incantamenti e modificatori, distrugge le pergamene, rimette l'artefatto nel gruppo e risponde con `NotificaApprovazioneIncantatura` ("Ecco fatto! …") o `NotificaRifiutoIncantatura`. Un nome vuoto vuol dire nessun nome proprio.
  - Test: `IncantatoreTest` (11). Tutta la suite è verde (301 test). Da provare a mano nel gioco: la schermata e il giro del `Prompt`.
- [x] **Fase 5 (venditore di pergamene e magazzini).**
  - In città due nuovi comandi, `VENDITORE_DI_PERGAMENE` e `INCANTATORE`, con le loro icone. `FUSIONE` resta solo dentro la bottega dell'incantatore. Ordine dei negozi: locanda, alchimista, armaiolo, venditore di pergamene, incantatore.
  - `TipoNegozio` (`ARMAIOLO`, `VENDITORE_DI_PERGAMENE`) in `modellodati`, con `tratta(TipoArtefatto)`. `RegistroArtefattiMD` tiene i magazzini per (coordinate, negozio), salvati come `x|y|NEGOZIO|numero`.
  - `ScambiatoreArtefatti.tratta(Artefatto)` (di default vero); `GruppoGiocatore.vende` rifiuta con `NotificaRifiutoVenditaArtefatto` la vendita di quel che il negozio non tratta.
  - `DisplayableCanvasArmaiolo` è diventato `DisplayableCanvasCommerciante`: una sola schermata per armaiolo e venditore, che cambia nome, immagine e intestazione secondo il `TipoNegozio` portato da `ComandoAperturaInventarioCommerciante`.
  - Immagini vere per il venditore di pergamene e per l'incantatore (`img/personaggi/VenditoreDiPergamene.gif`, `Incantatore.gif`).
  - `RegistroArtefatti.riempiMagazzini`, chiamato da `Foresta` quando costruisce le città.
  - Test: `NegoziTest` (4) e uno nuovo in `RegistroArtefattiMDTest`. Tutta la suite è verde (306 test). Da provare a mano nel gioco.
- [x] **Fase 7 (combattimento).**
  - `CalcolatoreCombattimento.difesaContro(difensore, tipo)`: la difesa contro un tipo di danno, con le resistenze di elmo, scudo e armatura (percentuale dimezzata). La usano la mitigazione del danno base e quella di ogni incantamento dell'arma, che prima usava sempre la `RESISTENZA_MAGICA`.
  - Doppia arma: `CalcolatoreCombattimento.fasiDiAttacco` dà l'arma principale e, per chi ne ha una, la secondaria al 40% (`FaseDiAttacco`). `calcolaDannoRisultante` ha un nuovo parametro `fattore`, che scala danno base e incantamenti. In `LocazioneBase` il turno di mischia fa tutte le fasi, per il giocatore e per l'avversario.
  - `PersonaggioBase.getParata()`: la `PARATA` intrinseca di scudo, elmo e armatura (vedi sotto), e −25% ("guardia aperta") con due armi o un'arma a due mani. Vale sia per la probabilità di essere colpiti sia per la mitigazione. `getResistenzaMagica()`: +1 per livello dello scudo, +2 se è raro.
  - Libro magico: `bonusLibroMagico`, sommato al danno degli `IncantesimoMalefico`.
  - Valori in `Costanti` (sezione "Combattimento").
  - Test: `CalcolatoreCombattimentoEquipaggiamentoTest` (15). Tutta la suite è verde (324 test).
  - Simulazione: `CombatSimulatorMatrix` e `TestMonteCarloMatrix` ora danno ai PG un equipaggiamento (armi, scudo, elmo, armatura, incantamenti) e seguono le fasi di attacco del gioco. Come si usano e i numeri sono in `risorse_e_documenti_vari/piano_montecarlo_matrix.md`, §11.
  - **Bilanciamento di doppia arma e scudo** (2026-09-24). Al primo giro l'attacco rendeva molto più della difesa: il Ladro vinceva contro la Viverna il 39% con spada e scudo e l'82% con due spade. Seconda arma dal 60% al 40%, `PARATA` dello scudo da +1 a +3 per livello, e allo scudo una `RESISTENZA_MAGICA` di +1 per livello (+2 se raro), perché contro gli attacchi elementali la `PARATA` non serve. Ora a livello 5 il Ladro vince contro il Troll il 98% con spada e scudo e il 96% con due spade (in 7,5 e 5,5 turni); contro la Viverna il 59% (73% con lo scudo raro) e il 71%. La guardia aperta resta al −25%, anche se pesa poco perché la `PARATA` di base è piccola.
- [x] **Danno delle armi di livello basso.** Prima a livello 1 una spada faceva 6, meno delle mani nude di un PG (4 + 1,5 × √Forza, cioè 9-10). Ora fa `max(4 + 2 × livello, 11 + livello)` (`GeneratoreArtefatti.danniMediArma`): 12 a livello 1, 16 a livello 5, come prima dal 7 in su. Test in `GeneratoreArtefattiTest`; simulazione in `piano_montecarlo_matrix.md`, §11.5.
- [x] **`PARATA` minima di scudo, elmo e armatura** (2026-09-24). Senza, elmo e armatura spogli non servivano quasi a nulla (il loro +5% di `PARATA` per livello moltiplica una `PARATA` di base di 2-6), e ai livelli bassi lo scudo rendeva poco (+3 a livello 1). Ora: scudo 6 + 2 per livello (era 3 per livello), elmo 2 + 1, armatura 3 + 1; la veste dà lo stesso minimo dell'armatura in `RESISTENZA_MAGICA`. Test in `CalcolatoreCombattimentoEquipaggiamentoTest` (18); tutta la suite è verde (328 test). Simulazione in `piano_montecarlo_matrix.md`, §11.6.
- [x] **Classi: blocchi di equipaggiamento, moltiplicatori di danno, incantesimi nel simulatore** (2026-09-25). I moltiplicatori `*_MOLTIPLICATORE_DANNI_FISICI` e `_MAGICI` c'erano dal 5 settembre (`d48d476`), ma il nuovo motore di combattimento (`3b557cf`, 31 agosto) non li ha mai letti: quello fisico lo usava solo la vecchia `getDanniInCombattimento`, quello magico nessuno. Ora li usa `CalcolatoreCombattimento` (vedi §2, "Combattimento"). Blocchi di equipaggiamento per classe e `FORZA` minima per l'armatura (§2, "Equipaggiamento"). Nel simulatore: `ScortaDiPergamene` e dotazioni tipiche per classe (`piano_montecarlo_matrix.md`, §13). Test: `CalcolatoreCombattimentoClassiTest` (5), 4 nuovi in `PersonaggioEquipaggiamentoTest`, 2 in `TestMonteCarloMatrix`; tutta la suite è verde (340 test).
- [x] **Dardo arcano, scenari più duri e primi ritocchi alle classi** (2026-09-25).
  - `DardoArcano` (§2, "Combattimento"): nel gioco è fra le scelte del comando incantesimi (`Comando.DARDO_ARCANO`, icona provvisoria), nel simulatore Mago ed Elfo lo lanciano quando promette più danno delle armi. Test: `DardoArcanoTest` (5).
  - Corretto il bug della `MAGIA` degli incantesimi di gruppo (§5.7).
  - `Costanti.INCANTESIMO_FATTORE_DANNI` (1,0): abbassarlo puniva soprattutto il Mago, quindi resta così.
  - Nel simulatore: livello dei mostri separato da quello del PG, scenari con 1, 2 e 3 mostri e con un mostro di un livello sopra (`SCENARI_CONFRONTO_CLASSI`).
  - Ritocchi: dardo dell'Elfo a 4 di `MAGIA`, dardo del Mago a 40 × livello. Non ancora misurati (vedi "Da fare"). Provato e tolto +0,1 di danno fisico a Ladro e Bardo: i loro moltiplicatori restano quelli di prima.
  - Tutta la suite è verde (345 test).
- [x] **Grammatica degli artefatti, prima versione** (2026-09-25). `artefatti2.txt` con `artefatti2_pp.txt`, letti da `GrammaticaArtefatti`; formato e scelte in §7. `GeneratoreArtefattiTabelle` la usa per metà delle spade; il costruttore con il solo `Random` resta a sole tabelle, così i test di prima non cambiano. `artefatti.txt` non è stato toccato. Test: `GrammaticaArtefattiTest` (7); `TestArtefatti2` stampa 40 spade come le genera il gioco. Tutta la suite è verde (352 test).
- [x] **Tetti degli effetti a 3/4/5** (comune/raro/leggendario) al posto di 5/6/7.
- [x] **Il `|` sparisce dai testi** alla fonte (§5.5).
- [x] **Rarità degli artefatti.** `RaritaArtefatto` con posti e tetti (§2, "Rarità"), salvata in `ArtefattoMD`. Il generatore fa rari il 10% degli artefatti incantabili e non genera mai leggendari; gli artefatti che nascono incantati hanno al massimo 3 incantamenti e almeno un posto libero. Test in `ArtefattoMDTest`, `ArtefattoIncantabileTest` e `GeneratoreArtefattiTest`; tutta la suite è verde (271 test).

## 4. Fasi

Ogni fase si può provare e committare da sola.

### Fase 1: modello
- `ArtefattoMD.nomeProprio` facoltativo, salvato come campo vuoto quando manca.
- Un metodo di presentazione unico per la forma completa (§2, "Nome proprio"), usato in `LocazioneBase`, `Informazioni`, `PersonaggioBase` e `Artefatto.prendi`/`Anello.prendi`, dove oggi `nome + ", " + descrizione` è ripetuto a mano. Nell'inventario: nome proprio (o nome) in grande, descrizione nel nodo secondario.
- `Artefatto.isIncantabile()` (armi, scudi, elmi, armature) e `getEffettiMassimi()` = `min(5, livello − 1)`.
- `SlotArtefatto.ENTRAMBE_LE_MANI` e `TipoArtefatto.SPADONE`.
- `ArtefattoMD.slotEquipaggiamento`, salvato come campo vuoto quando è `null`.
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
- **Candidati.** `Spada`, `Scudo` e `Anello` (se magico) generano l'artefatto **prima** della scelta e propongono i candidati secondo §2, più `GRUPPO`. I nuovi `Elmo` e `Armatura` si scrivono ma restano commentati finché non ci sono le immagini.
- **Filtro dei candidati.** Lo stato `SCELTA_DESTINATARIO_OGGETTO` esiste già (vivi + `GRUPPO`). Resta da filtrare i personaggi con `puoEquipaggiare(artefatto)`.
- **Un solo candidato:** l'oggetto va a lui senza domanda.
- **Nessun candidato:** l'oggetto va direttamente nel gruppo, anche quando chi c'è ha già occupato lo slot.
- **Cofano:** 5% pergamena, 5% artefatto casuale (10% in tutto).
- **Test:** il loot finisce nel posto giusto; candidati esclusi per slot, livello, morte; nessun candidato → gruppo.

### Fase 5: venditore di pergamene e magazzini
- Nuovo stato di `Citta`, come `DA_ARMAIOLO`, che riusa `AutomaAcquistiArtefatti` e `DisplayableCanvasScambiatoreArtefatti` ma tratta solo pergamene.
- `RegistroArtefattiMD.artefattiPerLocazione` diventa una mappa per (coordinate, tipo di inventario), con salvataggio e test.

### Fase 6: incantatore
- **Schermata** sulla base dello scambiatore:
  - a sinistra l'inventario del gruppo;
  - a destra il banco di lavoro, con al massimo 1 artefatto incantabile e le pergamene, fino al limite dell'artefatto.
  - Uscendo, quello che resta sul banco torna nel gruppo: il banco non si salva.
- **Conferma** → `ComandoIncantatura`. `GruppoGiocatore` ricontrolla le regole (§2), fa pagare, copia incantamenti e modificatori, distrugge le pergamene e risponde con `NotificaApprovazioneIncantatura` o `NotificaRifiutoIncantatura` (con il motivo).
- **Nome proprio** chiesto con il `Prompt`, già compilato con quello attuale.
- **Test:** fusione riuscita; ciascun rifiuto; il caso "livello 3 con 1 incantamento → al più 1 in più"; il tetto di 5.

### Fase 7: combattimento

Le regole sono in §2, "Combattimento". Da fare in `CalcolatoreCombattimento.calcolaDannoRisultante`:
- **Resistenze:** calcolo di `difesa_T` e suo uso nella mitigazione del danno base e degli incantamenti di tipo T.
  - Esempio atteso per il test: difesa base 30; armatura di livello 3 con un incantamento di fuoco "medio" (+10, +10%). Con la percentuale dimezzata `difesa_FUOCO` = (30 + 30) × 1,05 = 63, e il danno di fuoco passa dal 77% al 61%.
- **Doppia arma:** seconda fase di attacco al 40% (danno base e incantamenti) e −25% di `PARATA`.
- **Armi a due mani:** −25% di `PARATA` (il +50% di danno base sta nei valori dell'arma, dati dal generatore).
- **Scudo:** `PARATA` intrinseca in base al livello.
- **Libro magico:** bonus al danno degli incantesimi. Si applica dove gli incantesimi calcolano il danno (`LocazioneBase` → `calcolaDannoRisultante` con `IncantesimoMalefico`).
- **Test** per ciascuno. Poi un giro di `TestMonteCarloMatrix` / `CombatSimulatorMatrix` prima e dopo, per vedere l'effetto sul bilanciamento.

## 5. Bug e problemi trovati

1. ~~**Crash scegliendo `ANNULLA` quando si raccoglie un oggetto.**~~ Corretto con `SCELTA_DESTINATARIO_OGGETTO` (§3).
2. ~~**`addPozioniMagiaGrande` pubblica la notifica delle pozioni normali.**~~ Corretto. Lo sprite +/− compariva sull'icona delle pozioni normali.
3. ~~**Inventario dell'alchimista:** le pozioni di magia grandi dipendevano da quelle normali.~~ Corretto.
4. ~~**La fuga non faceva perdere le pozioni di magia grandi.**~~ Corretto, con un TODO in `GruppoGiocatore.fugge` perché la fuga è troppo penalizzante.
5. ~~**Formato di salvataggio fragile.**~~ Corretto. `StringTokenizer` salta i campi vuoti e usa `|` come separatore: un nome o una descrizione vuoti, o che contengono `|`, spostano tutti i campi seguenti. Correzione proposta, in due pezzi piccoli:
   - **alla fonte (fatto):** il `|` sparisce in silenzio, con `Serializzabile.senzaPipe`. Il `Prompt` non lo lascia scrivere e lo toglie anche dal testo incollato. Lo tolgono anche i setter dei testi che finiscono nei salvataggi: `ArtefattoMD` (nome, nome proprio, descrizione), `PersonaggioMD.setNome`, le note di `ModificatoreAttributo` e il nome di `Incantamento`. Test in `ArtefattoMDTest`.
   - **nomi vuoti (fatto):** un artefatto o un incantamento con il nome vuoto si chiama "nessun nome" (`ArtefattoMD.NESSUN_NOME`); un personaggio con il nome vuoto non ha nome (null) e si chiama con la sua classe.
   - **in lettura (fatto):** `LettoreCampi` in `modellodati` divide la riga con `split` conservando i campi vuoti, e ha preso il posto di `StringTokenizer` in tutti i salvataggi (`ArtefattoMD`, `PersonaggioMD`, `GruppoGiocatoreMD`, `StatisticheMD`, `LineaTemporaleMD`, `ForestaMD`, `RegistroArtefattiMD`, `RegistroPersonaggiMD`, `Notizia`, la testata in `GestoreSalvataggiSuFile`). `LocazioneMD` e `MissioneMD` usavano già `split`. Un campo facoltativo che manca si scrive vuoto (`Serializzabile.facoltativo`) e si rilegge come null: niente più `-` né `"null"`.
   - Non serve l'escape di `|`, perché alla fonte non entra.
   - Test: `LettoreCampiTest`, `ModelloDatiSalvataggioTest` (salva e rilegge in fila gruppo, statistiche, linea temporale, registri e notizie) e un test sui testi vuoti in `ArtefattoMDTest`.
   - **Classifica (fatto):** per coerenza anche `GestorePunteggiSuFile` usa il `|` al posto del `#`, e legge le righe con `LettoreCampi`. `GestorePunteggiBase.pulisciNome` toglie il `|` dai nomi; un nome vuoto diventa "nessun nome" (`Serializzabile.NESSUN_NOME`). Un file dei punteggi vecchio, con il `#`, non si legge più e il gioco riparte dalla classifica di default. Test in `GestorePunteggiTest`.
6. ~~**BERSERK applicato al contrario.**~~ Corretto. In `CalcolatoreCombattimento.calcolaDannoRisultante`, §2.5, la condizione era `dannoNonFisico && …`, mentre `TipoEffettoDiStato.BERSERK` e il commento dicono che scala il danno **fisico**. Ora è `!dannoNonFisico`, coperto da `CalcolatoreCombattimentoBerserkTest`. Siccome i guerrieri usano quasi sempre armi fisiche, finora il BERSERK non si applicava praticamente mai: ora che funziona, il combattimento dei guerrieri feriti diventa più forte, e conviene tenerlo presente nel bilanciamento.
7. ~~**Gli incantesimi di gruppo in combattimento non costavano `MAGIA`.**~~ Corretto. In `LocazioneBase` (`QUALE_FORMULA`) il costo di lancio si controllava ma non si toglieva; lo toglieva solo il ramo degli incantesimi su un solo bersaglio (`IncantesimoMaleficoImpl.formula`). Ora si toglie in tutti e due.

Non sono bug ma scelte da rivedere: l'armaiolo **ricompra a prezzo pieno**, e c'è del codice di debug da togliere (vedi "Da fare").

## 6. Gradi e prezzi delle pergamene

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
- **Modificatori di attributo:** si prezzano come gli incantamenti. Un `AUMENTO_FISSO` di q vale `2 × q`, un `AUMENTO_PERCENTUALE` di q% vale `q`, una `QUANTITA_ASSOLUTA` ("porta a q") vale `5 × q` (valore di partenza, da riaggiustare). Il prezzo della pergamena è la somma dei prezzi dei suoi effetti.
- **Effetti di stato:** +25% di prezzo per i `TipoDanno` che li hanno (`hasEffettiDiStato()`: FUOCO, GELO, VELENO…).
- **Rivendita** (anche per l'armaiolo): 50% del prezzo.
- **Solo fisso o solo percentuale:** una pergamena generata a caso può avere una sola delle due parti, e la formula funziona lo stesso. Per esempio una "media" con solo +10 costa 20, una con solo +10% costa 10.
- **`GradoIncantamento`:** un enum con bonus, coefficiente e prezzo per grado.
  - Il generatore sceglie il grado in base al livello di riferimento, per esempio solo minori ai primi livelli.
  - La grammatica lo usa anche per il **nome**: semplice per i gradi bassi ("una pergamena del fuoco"), altisonante per quelli alti ("il Sigillo della Fiamma Eterna").

Conti di esempio con la fusione:
- una spada di livello 3 con un incantamento medio: 30 (pergamena) + 15 (fusione) = 45 monete;
- una spada comune di livello 6 senza modificatori propri, con tre incantamenti maggiori (il tetto dei comuni): 150 + 25 = 175 monete.

Il bonus fisso scala con il livello dell'arma (`bonus × livello`), il coefficiente con l'Intelligenza di chi colpisce. Per questo il fisso rende di più sulle armi alte e il percentuale sui maghi.

I numeri sono di partenza: il bilanciamento è in "Da fare", e i valori stanno in `Costanti`, quindi si cambiano senza toccare la logica.

## 7. Grammatica degli artefatti

La grammatica sceglie **che cosa** ha un artefatto (nome, soprannome, descrizione, quali modificatori e incantamenti); **quanto** valgono lo decide il generatore, in base al livello. Così la grammatica non contiene numeri da bilanciare.

### File e classi

- `src/main/resources/.../motore/artefatti2.txt`: la grammatica, con il formato spiegato in testa al file.
- `artefatti2_pp.txt`: post-produzione, solo per l'articolo (sotto).
- `oggetti/GrammaticaArtefatti`: carica la grammatica, sceglie la radice, toglie i marcatori e restituisce un `Risultato`. Se il file non si carica lo scrive nel log e il gioco resta alle tabelle.
- `GeneratoreArtefattiTabelle.generaDaGrammatica`: trasforma il `Risultato` in un `ArtefattoMD`.

### Formato del risultato

Ogni radice produce una riga sola: il nome, con dentro dei marcatori `<chiave:valore>` che `GrammaticaArtefatti` toglie dal testo.

| Marcatore | Significato |
| :--- | :--- |
| `<mod:FORZA+1>` | un modificatore: un `TipoAttributo` e un'intensità da −3 a +3 |
| `<danno:FUOCO>` | un incantamento di quel `TipoDanno` |
| `<soprannome:Diavolina>` | il nome proprio; se ce n'è più d'uno vale il primo |
| `<descrizione:che brucia i nemici>` | la descrizione; se ce n'è più d'una vale la prima |

Esempio:

```
@la spada ardente<danno:FUOCO><descrizione:che brucia i nemici><soprannome:Barbecue> del Monaco Distratto<mod:SAGGEZZA-1>
```

diventa "Barbecue, la spada ardente del Monaco Distratto, che brucia i nemici", con un incantamento di fuoco e un malus alla `SAGGEZZA`. Un marcatore sconosciuto, un attributo o un tipo di danno che non esiste sono un errore (`IllegalArgumentException`), così li trovano i test invece di passare in silenzio.

Perché marcatori nel testo e non JSON, come in `artefatti.txt`: il JSON costringeva a escapare ogni virgoletta (e novanta righe di `artefatti.txt` escono malformate proprio per questo), ad accumulare le collezioni in variabili e a togliere con la post-produzione la virgola di troppo. Un marcatore invece sta attaccato alla parola che lo giustifica, e parola ed effetto non possono separarsi.

### Radici e numero di effetti

- Le radici si chiamano `<TIPO>_<n>`: un `TipoArtefatto` e il numero di effetti. Per ora `SPADA_0` … `SPADA_3`. `GrammaticaArtefatti` scopre da sola quali tipi e quanti effetti ci sono: per aggiungere un tipo basta scriverne le radici.
- **Ogni parte del nome porta un solo effetto**, quindi il numero di parti è il numero di effetti, e il limite di `RaritaArtefatto` si rispetta senza tentativi. Dove in `artefatti.txt` una parola dava due effetti (un bonus e un malus) si è tenuto il bonus.
- Il generatore chiede tanti effetti quanti ne ammette l'artefatto **meno uno**, come per gli artefatti che nascono incantati, così resta un posto per la fusione; se la grammatica non ne prevede tanti, prende la radice più grande. Quindi un comune di livello 1-2 esce da `SPADA_0`, che ha solo aggettivi di colore ("la spada di latta").

### Valori e prezzo

- Un modificatore è un `AUMENTO_FISSO` di intensità × gradino del livello (1, 2 o 3, `GradoIncantamento.getGradino`).
- Un incantamento è del grado del livello, con parte fissa e percentuale, e si chiama come quelli generati a caso ("Fuoco medio").
- Il prezzo parte da quello base e sale del prezzo di pergamena (§6) di incantamenti e modificatori positivi, scende di quello dei modificatori negativi, ma non va sotto la metà del prezzo base.
- Le spade da grammatica non ricevono incantamenti a caso (`incantaForse`): contraddirebbero il nome.

### Contenuto

- **Parti con un modificatore:** `PREFISSO` ("la terribile spada"), `AGGETTIVO` ("la spada mozzarella"), `COMPLEMENTO` ("del Monaco Distratto"), `DETTAGLIO` ("(con Camomilla in Omaggio)"). Vengono da `artefatti.txt`; ogni parola compare una volta sola in tutto il file, così dal nome si capisce che cosa fa la spada.
- **Parti elementali:** per ognuno dei quindici tipi di danno non fisici un aggettivo o un complemento, l'incantamento, una descrizione e, due volte su tre, un soprannome (Diavolina, Fiammifero e Barbecue per il fuoco, Ghiacciolo e Sorbetto per il gelo…). Ogni elemento ha produzioni sue, quindi aggettivo, soprannome e descrizione non si contraddicono. Una spada ha al più una parte elementale.
- **Articolo.** Il nome segue lo stile delle tabelle ("la spada di ferro"): la grammatica scrive `@la ` a inizio riga e `artefatti2_pp.txt` lo elide davanti a vocale ("l'immane spada").
- Rispetto a `artefatti.txt`: tolti gli attributi e i danni che nel gioco non esistono (`CARICO` è diventato `CARICO_MASSIMO`; tolti `SONICO` e `NECROTICO` come attributi, `SANGUINAMENTO` e `TENEBRA` come danni), i doppioni e qualche refuso; i nomi comuni usati come aggettivi (Accendino, Frigorifero, Suocera…) sono diventati soprannomi.

### Frequenze misurate

Su 20.000 generazioni per radice (cambiano quando si aggiungono parole, per il boost dei pesi di `GrammarBean`, `GrammarBean.md` §5.2):

| Radice | Spade elementali | Con soprannome | Modificatori positivi |
| :--- | ---: | ---: | ---: |
| `SPADA_1` | 19% | 12% | 71% |
| `SPADA_2` | 40% | 26% | 69% |
| `SPADA_3` | 46% | 29% | 67% |

### Come vederle

- Nel gioco, dall'armaiolo e nei cofani: metà delle spade viene dalla grammatica.
- Senza avviare il gioco: `TestArtefatti2` (nei test) stampa 40 spade dal livello 1 al 10, con prezzo, danno, modificatori e incantamenti.
