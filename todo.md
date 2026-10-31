# Cose da fare

Elenco unico delle cose da fare che non appartengono a un sottosistema preciso. Quando una voce è fatta, si toglie (la storia resta in git).

Le idee aperte dei singoli sottosistemi stanno nei loro documenti:

- missioni: [`gestione_missioni.md`](gestione_missioni.md) §11 e [`passi_missioni.md`](passi_missioni.md) §7;
- artefatti, pergamene, incantatore, bilanciamento: la lista di [`artefatti_e_incantamenti.md`](artefatti_e_incantamenti.md);
- economia: [`economia.md`](economia.md), sezioni "Che cosa non torna" e "Da fare / idee aperte".

## Da fare per prime

- **Più incantesimi e un'economia da rivedere.** A metà partita il gruppo non ha più le monete per comprare incantesimi o artefatti: i soldi non bastano e le fonti sono poche. Servono un modo per recuperare più incantesimi e una revisione dell'economia del mondo (vedi [`economia.md`](economia.md), "Che cosa non torna"). Una delle poche strade praticabili, oltre alle missioni, è il bottino lasciato dai mostri sconfitti: aggiungervi anche le monete. Oggi non ci sono, e `economia.md` lo dichiara scelta voluta (scheletri e spettri non hanno niente da lasciare, un bottino solo per alcuni sarebbe incoerente): la decisione va rivista, per esempio dando le monete solo alle classi che portano una borsa (vedi `BottinoDegliAvversari` per le pergamene). Da stabilire quante, in funzione del livello del mondo e dell'avversario, senza far crollare i prezzi dei negozi.
- **Viaggio rapido.** Un modo per tornare in una locazione già visitata senza rifare la strada (città, locande, templi...), probabilmente a pagamento o a costo di tempo (ore di gioco, con la notte e gli eventi della linea temporale). Da decidere da dove si parte (mappa a tutto schermo?), cosa succede strada facendo e se vale per ogni tipo di locazione.
- **Rileggere i commenti di bilanciamento di `personaggi_giocabili.md`.** Le tabelle degli scontri (§5.1-5.3) sono state rifatte il 2026-10-31 con le ricariche corrette in `RoundASimulato`; restano da rileggere il testo che le commenta (§5.4, §6, §7) e da rifare le tabelle di §3 (schede, per il Ladro senza elmo) e §4.1 (danni delle abilità). Con le ricariche che calano le classi con abilità (Mago, Chierico, Druido, Elfo) valgono molto più di prima: vedi se il Sacerdote, che da solo non vince nemmeno contro i Goblin, e il Ladro senza elmo vanno ritoccati.
- **Ritarare il bilanciamento dopo il danno ripartito.** Gli incantesimi a più bersagli e le abilità di danno di chierico e druido ripartiscono ormai il danno per posizione (principale intero, poi 50%, 25%, 12,5%, 6,25%, che è il minimo: `CalcolatoreCombattimento.quotaDelDannoPerPosizione`, `CostantiIncantesimi.INCANTESIMO_MULTIPLO_*`), ma le simulazioni a mano (`SimulazionePartiteTest`, `TestScontriDiGruppo`, che si lanciano togliendo il `@Disabled`) non sono state rifatte: i numeri di prima erano sul danno pieno. Da rivedere con le simulazioni se la riduzione (50% per posizione) basta a non far più strage, o se serve una curva più ripida, e se i personaggi con molti bersagli (il Mago) restano abbastanza utili.

## Bug noti

- **Bilanciamento col round a coppie.** Le simulazioni (`SimulazionePartiteTest`, `TestScontriDiGruppo`, `TestLogoramentoInizioPartita`) sono portate al round a coppie (oggi lista dei turni con distribuzione in proporzione: `RoundASimulato` replica ancora il vecchio piano, da allineare) e i numeri sono in [`tattiche_di_combattimento_automatiche.md`](tattiche_di_combattimento_automatiche.md) §9: la stima del giocatore automatico è tarata; restano l'Eremita a livello 1-3 e il bersaglio preferito che uccide presto maghi e sacerdoti.
- **Modalità VERTICALE.** `DisplayableCanvasBarraIcone` avanza di 32 px con icone alte 64, e la finestra è larga al massimo 400 px (vedi [`motore_grafico.md`](motore_grafico.md) §12).
- **Schermi alti meno di 804 px.** La barra delle icone copre il fondo del riquadro delle missioni.

## Bugfixing

Sono i difetti trovati dalla revisione completa del 2026-10-19. I codici sono quelli di [`assessment.md`](assessment.md) §5: A vale per gravità alta, M per media, B per bassa. "(P)" segna un difetto plausibile, cioè con un innesco raro o non ancora raggiungibile; gli altri sono confermati. Ogni correzione va con un test. Le voci segnate come **regola** cambiano il bilanciamento, e vanno decise prima di toccarle.

### Gravità bassa: codice e testi non usati

- B35a. Codice morto in `PersonaggioBase` (`05-foresta-personaggi`): `getQuantitaEffettoDiStato` (solo test).
- B35d. Passi pronti di `MissioneAPassi` (`09-foresta-missioni`) che nessuna missione usa, solo i test: `evitaCombattimento`, le sequenze di passi (`impostaSequenzaPassi`, `leggiSequenzaPassi`, `prossimoNellaSequenza`) e `getPassiConIntermezzoInAttesa`. `costruisci`, `attendiOre`, `dialogo` e `contaFinche` li usa `LaForgia`; `generaParametri`, `vai` e `tornaAlPuntoDiPartenza` li usa `IlViandante`. Idee per usare gli altri: una consegna riservata (`evitaCombattimento`), un incarico a più tappe in locazioni scelte a caso (sequenze di passi e `vai`).
- B35e. Le produzioni morte, in `01-foresta-testi` (nessuna è citata da altre produzioni né dal codice; controllato il 2026-10-22): `fiabe.txt` (GIORNO_DELLA_SETTIMANA, OTTENUTO, RACCONTO_BREVE: quest'ultima compare solo in un commento) e `locande.txt` (GESTORE_NOTE e NOTIZIE_CITTA, con le 12 notizie CG1–CG12 che non escono mai). Da togliere, oppure da collegare a un punto del gioco.

### Gravità bassa: 12-foresta-swing e 13-foresta-app-swing

- B41c. `DoomdarkTextRectangle` (non 2x) mai usato.

## Architettura

- **La grammatica italiana per una versione inglese.** `Articoli` e `Numeri` (in `01-foresta-testi`), `mappa/Direzioni` e `motore/Racconto` tengono in codice tutto ciò che dipende dalla lingua italiana: gli articoli e le preposizioni articolate (`Articoli.IL`, `LO`, `DELLA`, `DALL_APOSTROFO`...), `Articoli.conPreposizione`, i cardinali e gli ordinali in lettere (`Numeri.getCardinaleM/F`, `getOrdinaleM/F`, limitati a 1–41 perché non servono numeri più alti), le direzioni (`02-foresta-mappa/…/mappa/Direzioni`) e i testi della storia e dei finali (`Racconto`, che la UI riceve da `VistaPartita`). Per una versione in inglese va riguardato tutto: in inglese gli articoli e gli accordi di genere non ci sono, gli ordinali si formano con un suffisso ("1st", "2nd", "11th") e non servono tabelle, le preposizioni non si fondono con l'articolo. Probabilmente le funzioni diventano un'interfaccia con un'implementazione per lingua (come `Testi` per i `.properties`), e i testi lunghi vanno nei file per lingua. Chi chiama oggi: `PersonaggioBase.getNome` (con gli articoli e gli ordinali dei mostri), `LineaTemporale`, `Cofano`, `DisturbatoreDellaQuietePubblica`, i negozi e le grammatiche. Vedi anche [`assessment_portabilita.md`](assessment_portabilita.md), "Testi italiani nel codice".
- **Salvataggi fuori dal modello dati.** Le classi `…MD` portano ancora `salva` e `leggi` (formato di file). Per toglierli servono getter e setter in quasi tutti i MD (`leggi` scrive direttamente nei campi privati) e un serializzatore per classe, in un solo commit perché `ModelloDati` richiama tutto a catena (29 file); non è necessario per il porting (usano solo `java.io`).
- **Messaggi interni costruiti anche quando nessuno li ascolta (peso sulla mobile).** `Automa.processaComando` pubblica sul bus un `InternoMessaggio("Automa in stato " + ... + "; processo Comando " + ...)` a ogni passo della macchina a stati (e di nuovo a ogni passo della cascata automatica in `prosegui`): la stringa si costruisce prima di sapere se qualcuno ascolta, e nella pratica la ascolta solo `SnifferBusEventi`, uno strumento da desktop. Lo stesso vale per gli altri `InternoMessaggio` costruiti con una concatenazione. Sono 19 in tutto (13 in `Automa`); da vedere come evitare il costo (per esempio un evento che porta il testo come fornitore, o pubblicarlo solo se c'è un iscritto), come si è fatto per il `Logger` con `Logger.log(Supplier<String>)`.

## Gioco e contenuti

- **Punteggio dei cofani.** Oggi un cofano vale sempre 150 punti per ogni cofano trovato (`TipoBottino.COFANO`), qualunque cosa contenga, e quello che c'è dentro (monete, pietre, pozioni, pergamene, artefatti) non dà punti suoi. L'idea è che conti il valore degli oggetti contenuti. Prima va definita una tabella: quanti punti per una moneta, una pietra, una pozione, una pergamena, un artefatto (oggi `MONETA` vale 50 per un mucchietto di 1–2, mentre nel cofano le monete sono 5–10 una per una). Per ora resta 150 a cofano. Il punto in cui calcolarlo è `RaccoltaBottino.apri`.
- Fumetto che attende la chiusura.
- **Tabella delle debolezze e resistenze (provvisoria).** Le voci per classe e categoria in `TipoPersonaggio` e `CategoriaPersonaggio` sono un primo giro dalla tradizione fantasy (vedi [`debolezze_e_resistenze.md`](debolezze_e_resistenze.md)): vanno provate in partita, ritoccate e completate per le classi che non hanno ancora voci (Arpia, Centauro, Minotauro, Gigante, Titano, Goblin, Hobgoblin).
- **Rivedere abilità, canti e incantesimi: costi, danni, durate e ricariche.** Con la ricarica (vedi [`personaggi_giocabili.md`](personaggi_giocabili.md), § "La ricarica") e i nuovi costi di dardo arcano, canto curativo e luce divina i valori non sono ancora misurati: `AnalisiAbilitaInnateTest`, `AnalisiDanniAbilitaInnateTest` e le simulazioni (`SimulazionePartiteTest`, `TestScontriDiGruppo`) vanno rifatte, e a monte va riguardato il rapporto fra danno, costo e ricarica. Il "danno" è la base, poi moltiplicata per √livello, intelligenza, potere magico e bonus del libro; la "durata" è dell'effetto proprio dell'abilità (fra parentesi, lo stato che può causare); le pergamene si consumano e non hanno ricarica. Lo stato al 2026-10-30:

  | Abilità o incantesimo | Danno | Costo | Durata | Ricarica |
  | :--- | ---: | ---: | :--- | ---: |
  | Scacciare i non morti | 0 | 4 | istantaneo (`SPAVENTATO` 3 turni) | 4 |
  | Imposizione delle mani | 0 (cura 20 × √livello) | 5 | istantaneo | 4 |
  | Radici | 40 | 3 | istantaneo (`IMMOBILIZZATO` o `ACCECATO` 3 turni) | 4 |
  | Sciame | 40 | 3 | istantaneo (`AVVELENATO` 4 turni) | 5 |
  | Pelle di corteccia | 0 | 5 | 4 turni | 5 |
  | Rigenerazione | 0 | 4 | 4 turni | 5 |
  | Alba sacra | 0 | 5 | istantaneo | 4 |
  | Luce divina | 35 | 4 | istantaneo (`ACCECATO` 3 turni) | 4 |
  | Attacco alle spalle (Ladro) | arma con critico sicuro (×2) | 0 | istantaneo | 3 |
  | Martello sacro (Chierico) | 30 (sacro, ×2 sui non morti) | 3 | istantaneo | 2 |
  | Dardo arcano (Mago) | 20 | 4 | istantaneo (`SILENZIATO` 3 turni) | 2 |
  | Dardo arcano (Elfo) | 15 + 4 per livello oltre il primo | 6 | istantaneo | 2 |
  | Canto curativo | 0 (cura 30 × √livello) | 6 | istantaneo | 4 |
  | Canto ipnotico | 0 | 3 | istantaneo (`SPAVENTATO` o `CONFUSO` 3 turni) | 4 |
  | Canto di valore | 0 | 5 | 4 turni | 5 |
  | Urlo della Banshee | 0 | 6 | istantaneo (`SPAVENTATO` e `CONFUSO` 3 turni) | 5 |
  | Aria (pergamena) | 35 | 1 | istantaneo | – |
  | Acqua | 45 | 1 | istantaneo | – |
  | Terra | 50 | 2 | istantaneo | – |
  | Fuoco | 50 | 3 | istantaneo | – |
  | Fulmine | 100 | 10 | istantaneo | – |
  | Gelo | 50 | 2 | istantaneo | – |
  | Veleno | 50 | 2 | istantaneo | – |
  | Morte | 0 (uccide chi ha meno del 25% di salute) | 5 | istantaneo | – |
  | Resurrezione | 0 | 15 | istantaneo | – |
  | Silenzio | 0 | 5 | `SILENZIATO` 3 turni | – |
  | Nebbia (globale) | 0 (−30% di precisione a tutti gli avversari) | 5 | 3 turni | – |
  | Nega magia (globale) | 0 (−40 di potere magico a tutti gli avversari) | 6 | 3 turni | – |
  | Grido di battaglia (globale) | 0 (+2 coraggio e +2 valore al gruppo) | 6 | 4 turni | – |
  | Terremoto (globale) | 30 (Terra, a tutti gli avversari, non ripartito) | 8 | istantaneo (`ATTERRATO` 25% per un turno) | – |
  | Tempesta (globale) | 40 (Fulmine, a tutti gli avversari e ai compagni) | 10 | istantaneo | – |
  | Terrore (globale) | 0 (prova di spavento su tutti gli avversari) | 8 | `SPAVENTATO` | – |

  Da riguardare in particolare: il dardo arcano (20 di base per 4 di magia, contro i 35 per 1 dell'Aria: il suo vantaggio è non consumare pergamene, e con ricarica 2 si usa a turni alterni); Radici e Sciame (3 per 40, contro i 2 per 50 di Terra e Veleno); il canto curativo contro l'imposizione delle mani (cura più, costa di più, ma per punto di magia resta più efficiente); e il recupero di salute e magia a fine locazione (circa un quarto della salute e un settimo della magia a ogni passo, vedi `RIGENERAZIONE_*` in `CostantiPersonaggi`), che con la ricarica potrebbe non essere più il problema.
- **Missioni a tappe: contenuti e documenti.** `MissioneATappe` c'è e funziona (persone, leggendari, farse, scelta fra due piste, chiavi con `ChiaveDelCustode`; si assegna come le altre missioni). Restano la Fase 3 di [`missione_a_tappe.md`](missione_a_tappe.md): più ricette in `tappe.txt` (oggi cinque: persona, leggendario, chiavi, due piste, farsa; l'obiettivo era 15-20), le immagini per l'oggetto trovato, scene d'arrivo più varie, e i documenti (`gestione_missioni.md`, `passi_missioni.md`, `intermezzi.md`, `foresta.md`, che oggi non parlano di `MissioneATappe`); poi `missione_a_tappe.md` va riscritto per descrivere il codice, come vuole `CLAUDE.md`.
- **Incantesimi che mancano per i tipi di danno non fisici (da fare fra i primi).** Ogni `TipoDanno` non fisico dovrebbe avere almeno un incantesimo che lo infligge. Oggi (`TipoIncantesimo`, più il Dardo arcano e i canti) la situazione è questa:

  | Tipo di danno | Incantesimo oggi | Manca? | Effetto di stato nativo (`TipoDanno.getEffetti`) |
  | :--- | :--- | :--- | :--- |
  | Aria, Acqua, Terra, Fuoco, Fulmine, Gelo, Veleno | Aria, Acqua, Terra, Fuoco, Fulmine, Gelo, Veleno | no | atterrato, bagnato, immobilizzato o accecato, bruciato, stordito, rallentato o congelato, avvelenato |
  | **Sacro** | Luce Divina (abilità innata di Sacerdote e Sacerdotessa; colpisce anche lo Scheletro alleato) | no | accecato (interagisce con `MALEDETTO`, `INFETTATO`: Rigetto, Purificazione) |
  | **Necrotico** | solo Morte, che non fa un danno vero: uccide chi ha meno di un quarto della salute massima, altrimenti si ritorce contro chi la lancia (gli toglie un quarto della salute) | **sì**, un incantesimo che faccia danno necrotico come gli altri | infettato (interagisce con `MALEDETTO`: Mietitura) |
  | **Acido** | nessuno | **sì** | nessuno (interagisce con `AVVELENATO`: Reazione Tossica) |
  | **Sonico** | nessuno | **sì** | assordato (interagisce con `ASSORDATO`: Disorientamento) |
  | **Psichico** | nessuno | **sì** | confuso o spaventato (interagisce con `SILENZIATO`: Isolamento Sensoriale) |
  | **Vuoto** | nessuno | **sì** | nessuno (interagisce con `CONFUSO`, `MALEDETTO`: Follia Cosmica, Collasso Entropico) |
  | **Maledizione** | nessuno | **sì** | maledetto |
  | **Arcano** | solo il Dardo arcano (innato di Mago ed Elfo) e Silenzio (che mette `SILENZIATO` senza danno) | **sì**, una pergamena che faccia danno arcano | silenziato (interagisce con `INFETTATO`, `SILENZIATO`, `SPAVENTATO`/`CONFUSO`) |

  Quindi mancano **Acido, Sonico, Psichico, Vuoto, Maledizione**, più un incantesimo **necrotico** vero e proprio (la Morte è un'altra cosa) e una pergamena **arcana** che faccia danno. Il Vuoto e la Maledizione sono i più rari e potenti, da tenere per i livelli alti, per boss o per ricompense (non per tutte le alchimie). Un primo ordine possibile: Maledizione (ha già `MALEDETTO`, cardine della Mietitura), Psichico (`CONFUSO`, `SPAVENTATO`, immunità dei Costrutti già presente), Sonico (si lega ai canti del Bardo). Per aggiungerne uno: costi e prezzo in `CostantiIncantesimi`, voce in `TipoIncantesimo`, `Comando`, `TipoAzione`, `TipoIcona` e `ImageCache` con l'icona in `img/icone`, la classe in `incantesimi` e `FabbricaIncantesimi`; vedi come si è aggiunta la Luce Divina (`LuceDivina`, `LuceDivinaTest`). Attenzione che `PersonaggioBase.getRepertorioIncantesimi` dà ai mostri magici tutti i malefici, e `FabbricaIncantesimi.casuale` pesca fra tutte le classi (cofani, offerte): decidere per ognuno se i mostri lo lanciano e se finisce fra le pergamene casuali.

- **Boss come Ondate (da valutare).** I boss (Lich, Minotauro Gigante, Strega...) si incontrano da soli e il gioco non distingue un avversario "di rango": trasformare quegli incontri in Ondate (`Ondata`) darebbe un modo naturale di riconoscerli, per il selettore "boss" delle tattiche (vedi [`tattiche_di_combattimento_automatiche.md`](tattiche_di_combattimento_automatiche.md) §6.1).
- **Incantesimi a più bersagli: l'area d'effetto non sono i primi N del gruppo.** Oggi i bersagli sono i primi N vivi nell'ordine del gruppo (`IncantesimoMaleficoImpl.formula` quando lancia il giocatore, `SceltaIncantesimo.bersagli` quando decide una tattica: il bersaglio principale e poi i successivi dell'elenco). Dovrebbe essere un'area attorno al bersaglio principale: se una tattica sceglie il personaggio più debole, vengono colpiti lui e quelli immediatamente prima e dopo di lui nell'ordine del gruppo, o comunque vicini, fino a N in tutto (all'incirca (N-1)/2 per parte). Da decidere: se il bersaglio è il primo o l'ultimo, l'area si sposta (si colpiscono comunque N personaggi) o si accorcia (se ne colpiscono meno di N)? e come ci si comporta con un N pari, dove (N-1)/2 non è intero? Quando lancia il giocatore a mano non c'è un bersaglio principale (si lancia sull'intero gruppo avversario): serve poterlo scegliere, come per gli incantesimi a bersaglio singolo (vedi il TODO in `LocazioneBase`, "far scegliere al giocatore su quale avversario formularlo"). La stessa regola vale per la valutazione delle tattiche.
- **I mostri colpiscono un solo bersaglio anche con un incantesimo a più bersagli.** Per scelta di oggi (`SceltaIncantesimo.bersagli` esclude i mostri), quindi il `NUMERO_BERSAGLI` di un boss come la Strega o il Lich non ha nessun effetto sui suoi incantesimi. Da valutare se farlo valere, ma senza renderli troppo forti: sono boss, e il gruppo li affronta già con difficoltà (vedi "Combattimento" nei bug noti). Da decidere dopo l'area d'effetto: il danno ripartito per posizione c'è già, e cambia di molto quanto vale un incantesimo a più bersagli.
- **Colpo potente del Guerriero** (idea, da valutare): il Guerriero non ha nessuna abilità innata (ha solo lo stato `BERSERK`, che dipende dalla furia). Un attacco con le armi più forte del normale, per esempio con un moltiplicatore del danno fisico, da usare contro avversari con molta salute. Non può costare `MAGIA` (ne ha 34 al livello 7, con recupero 4): servono un'altra risorsa (la `STANCHEZZA`?) o una pausa fra due usi (la condizione `ROUND_DALL_ULTIMO_USO` delle tattiche esiste già), più una voce in `TatticheBase.guerriero()`. Prima di farlo, misurare con l'equipaggiamento reale: con un'arma incantata il Guerriero da solo regge bene i mostri fisici, e il divario contro i boss quasi sparisce nel gruppo (vedi [`personaggi_giocabili.md`](personaggi_giocabili.md) §5.3 e §7, punto 8); resta debole da solo contro Strega, Idra e Lich (6%, 17%, 1% con le armi incantate).
- **I testi**: le note dei moltiplicatori (`note_personaggi_it`) e le descrizioni (`tipo_abilita_it`) sono un primo giro; nessuna scena di locanda, nome proprio o missione parla ancora di chierici e druidi (`ProduttoreDiMissioniCasuale`, `nomi.txt`).
- **Le armi del druido**: non esistono archi nel gioco, quindi non lo usa (vedi `RegoleEquipaggiamento`); se arriveranno, rivederle.
- Le botteghe nella Foresta (armaiolo, alchimista, venditore di pergamene, incantatore) ci sono. Resta da fare: immagine della locazione (oggi copie della locanda).
- Economia: le entrate sono misurate solo fino al livello 5, perché il giocatore automatico di `SimulazionePartiteTest` muore a livello 2-4 e non fa le missioni; farlo vivere più a lungo e fare le missioni, poi eventualmente ritarare i prezzi degli ingredienti: vedi [`economia.md`](economia.md).
- Dimensione ottimale della mappa: `CostruttoreDellaForesta.DIMENSIONE_X`/`DIMENSIONE_Y`, da tarare con le prove.
- **Spostare anche i personaggi a tempo nell'ordine del gruppo.** Oggi un aiuto (gratuito o mercenario) o lo Scheletro della Mietitura non si sposta: `Gruppo.spostaPersonaggio` rifiuta chi è a tempo e scavalca nei suoi spostamenti chi lo è, e la schermata delle tattiche e l'inventario non li mostrano (`Automa.isDelGruppoPerInventario`, e `DisplayableCanvasTattiche` ha un ramo "Con il gruppo a tempo" che oggi non si raggiunge). Poiché l'ordine del gruppo decide chi è in prima linea e chi riceve i colpi, potrebbe servire poterli mettere avanti o dietro. Da decidere: come si scelgono (la schermata delle tattiche è fatta per i permanenti, e l'inventario non vale per loro), cosa fanno i permanenti vicini nello scambio (oggi si saltano) e se l'ordine dei personaggi a tempo si salva (`GruppoGiocatore.spostaPersonaggio` riordina anche il modello dati).
- **Alzare il limite dei 40 giorni.** Oggi il tempo finisce al giorno 40: dal 20 il drago distrugge una città ogni cinque giorni e dal 41 la partita è persa (`LineaTemporale.eventi`). Se si alza vanno riviste insieme: le soglie delle città distrutte (20, 25, 30, 35), la soglia della sconfitta (`giorno > 40`), `Numeri.getOrdinaleM/F` (valida da 1 a 41: il giorno nel testo "E' il … giorno" si genera con quella, e da 42 in su lancerebbe `ArrayIndexOutOfBoundsException`), la durata che le missioni a tempo si aspettano (spedizioni urgenti, `attendiOre`, la forgia che ruba almeno 18 ore) e le simulazioni (`SimulazionePartiteTest`). Meglio decidere prima se il limite diventa una costante e se la scadenza è un'ora di gioco o un giorno.
- **Iniziativa in combattimento.** Non c'è un "tiro di iniziativa": il gruppo del giocatore comincia sempre per primo (di norma, con `COMBATTIMENTO`, il giocatore sceglie chi), e l'ordine dei turni è quello della lista fissata a inizio combattimento (vedi `tattiche_di_combattimento_automatiche.md` §3). Da valutare un'iniziativa vera, magari legata alla `VELOCITA` dei personaggi, e chi attacca per primo se gli avversari si accorgono del gruppo (oggi `gliAvversariAttaccanoPerPrimi`, un solo attacco fuori dal piano).

## Personaggi non combattenti

I nomi di questi personaggi sono fatti (liste, registro, testi delle missioni, schermate dei negozi): quello che resta e le idee aperte sono in [`nomi.md`](nomi.md) §8.

- **Immagini e tipi propri per la levatrice**, e per i mandanti senza personaggio proprio (mugnaio, mercante, fabbro, notaio, vedove...): oggi sono un `Uomo` o una `Donna` (`AspettoDelMandante`), con l'immagine e il verso del locandiere e della moglie del bardo.
- **La levatrice e le altre comparse nelle scene**: il nome c'è, nessuna scena le chiede.
- **Un punto della UI che elenchi i campioni incontrati** (nome, classe, se sconfitti): i dati sono già nel registro dei personaggi; si può mettere nella schermata delle statistiche o in quella dei trofei.
- Le **missioni di scorta** ("salva il locandiere", "salva l'alchimista"...; "Il viaggio di" la moglie del bardo c'è già: `IlViaggioDellaMoglie`) con `MissioneAPassi.prendiInScorta`, al posto del Viandante: `RegistroPersonaggi.costruisciPersonaggio(residente, livello)` ne fa il `Personaggio` da dare al `Supplier`, come già fa `NonSparateSulPianista` con il `BardoLocanda`. Solo il locandiere non è nel registro: il suo nome sta nella locanda (`Locanda.LOCANDA_NOME_LOCANDIERE`).

## Grafica e immagini

- **Rifiniture grafiche per chierico e druido.** Le immagini ci sono tutte: i personaggi (`img/personaggi/`) e le icone di classe di Chierico, Chierica, Druido e Druida, e le icone delle loro abilità innate (`img/icone/`: `ImposizioneDelleMani.gif`, `ScacciareINonMorti.gif`, `Radici.gif`, `Sciame.gif`, `PelleDiCorteccia.gif`, `Rigenerazione.gif`). Il verso dei personaggi (`VersiDeiPersonaggi`) è già coerente con quello di Guerriero e Guerriera. Restano solo queste cose da decidere o disegnare, nello stile e nella risoluzione di quelle esistenti (icone 32×32 con sfondo trasparente, le compone `TipoIcona`; personaggi dimezzati come gli altri):
  - **Icona dell'effetto di stato `RIGENERAZIONE`**, se se ne vuole una propria: oggi la UI mostra gli effetti di stato nelle schermate delle tattiche e nel riquadro della locazione, non con icone dedicate, e l'effetto non ne ha; controllare come li mostra la UI prima di disegnarla.
  - **Sprite delle abilità nel combattimento** (`TestoAzioneInCombattimento` e gli eventi con sprite): per ogni abilità del chierico e del druido, se si vuole un lampo o un effetto sul bersaglio come per gli incantesimi; da decidere se bastano le icone.
  - **La veste del druido**: oggi parte con la veste da mago (vedi "La veste per il ladro, il sacerdote e il druido").
  - **Arco:** non esistono archi nel gioco, quindi nessuna immagine di arco.

- **La veste per il ladro, il sacerdote e il druido.** `img/oggetti/Veste.gif` è una veste da mago, e la rivelazione (`SpriteRivelazioneArtefatto`) la mostra per ogni veste; servono le immagini delle altre, e il modo di scegliere quella giusta (oggi una veste non sa per chi è).
- **Un'icona sola per la pozione di aumento di livello e per il livello guadagnato.** `ImageCache.spritePozioneAumentoLivello` e `ImageCache.spriteAumentoLivello` sono la stessa immagine, `img/icone/AumentoLivello.gif`, caricata una volta sola (la seconda rinvia alla prima). Il primo campo è l'icona della pozione nella riga "Aumento livello" di `DisplayableCanvasRiquadroIncantesimiEPozioni`; il secondo è l'icona di "livello aumentato" nel riquadro del gruppo (`DisplayableCanvasRiquadroGruppo`). Le altre pozioni hanno ognuna la sua gif (`PozioneSalute`, `PozioneSaluteGrande`, `PozioneMagia`, `PozioneMagiaGrande`): per la pozione di livello non è mai stato fatto un disegno proprio. I due campi restano separati proprio per questo, così il giorno che si disegna un'icona della pozione diversa da quella del livello basta cambiare il caricamento di `spritePozioneAumentoLivello`, senza toccare i punti che li usano. Se invece i due disegni restano uguali, si può tenere un campo solo.
- **Immagini a metà risoluzione con zoom a runtime** (`LivelloDiZoom`, argomento `ZOOM=n`, predefinito 2). Sono già dimezzate nei file e ingrandite al caricamento: locazioni, personaggi, oggetti, mappa, alfabeto, i loghi PNG, 19 fondi e le icone (composte da `TipoIcona`). Restano senza zoom:
  - **`img/icone/`**: le icone dei comandi sono 32×32 con sfondo trasparente e le compone `TipoIcona` (sfondo, ombra, disegno, zoom). Non sono ancora 32×32 `Moneta`, `PietraPreziosa` e `Tempo` (27×28, sprite caricati da `ImageCache`), `Missione-birra-grande` e `ComponenteScorrevole-*` (16×16);
  - **`img/fondinon2x2/`**, 24 fondi non a blocchi 2×2 (`Cartiglio-*`, `SferaMagica`, `SfondoStoria`, `Separatore`, `Separatore-Armature/Armi/Elmi/LibriMagici/Schinieri/Scudi`, `Foreground*` e `Interno*` di Alchimista/Armaiolo/Incantatore/Locanda/VenditoreDiPergamene, `Luna`, `Mimas`, `LunaHHGTTG`): vanno riguardati a mano; una volta sistemati ognuno si dimezza, si rimette in `fondi/` e si carica con lo zoom. Per gli sfondi degli intermezzi va anche estesa la regola per prefisso in `DisplayableCanvasIntermezzo.risorsa()`, che oggi ingrandisce solo `locazioni/` e `personaggi/`.
  - Finché non sono sistemati, i cinque separatori già convertiti sono il doppio degli altri sei.
  - `personaggi/Locandiere.gif` aveva un pixel isolato che non rientrava in un blocco 2×2 (circa x=66, y=14): controllare che dimezzandolo non si sia visto.
  - Per salvare i GIF con Pillow: `optimize=False` e niente `convert()` prima di salvare, altrimenti si altera la palette; confrontare palette e trasparenza con l'originale in git.

## TODO e FIXME nel codice

Gli altri `TODO`/`FIXME` restano nel codice, accanto al punto a cui si riferiscono (sedici, sparsi fra `PersonaggioBase`, `LocazioneBase`, `DisplayableCanvas`, `GruppoGiocatore`, `TatticheBase` e altri; in `TatticheBase` gli elenchi delle classi magiche, con Silenzio e con Morte da tenere allineati con le classi, e le abilità proprie dei mostri: rigenerazione del Troll, teste dell'Idra, canto dell'Arpia). Per ritrovarli:

- in IntelliJ, la finestra **TODO** (View → Tool Windows → TODO, oppure ⌘6) li elenca tutti con file e riga sempre aggiornati, e un click porta al punto esatto;
- da terminale: `grep -rn "TODO\|FIXME" --include=*.java .`.
