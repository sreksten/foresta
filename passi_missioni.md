# Catalogo dei passi e delle missioni

Il vocabolario dei passi con cui si scrivono le missioni a passi, le missioni concrete che li usano e la mappatura dei tipi di missione (`TipoMissione`) sulle missioni che li coprono. L'infrastruttura (`Passo`, `MissioneAPassi`, registro, claim, intermezzi) è in [`gestione_missioni.md`](gestione_missioni.md); qui si descrive **che cosa c'è**, non come funziona il motore.

Il codice è in `missioni/`; le grammatiche in `src/main/resources/com/threeamigos/foresta/motore/missioni.txt` e `leggendari.txt`.

## 1. Regole per chi aggiunge missioni

- **Annotare i `TipoMissione` coperti.** Sopra ogni valore di `missioni/TipoMissione.java` coperto da una missione vera c'è un commento `// Coperto da: NomeClasse` (più classi separate da virgole, con fra parentesi il mandante o il caso). Aggiungendo o togliendo una missione si aggiornano questi commenti.
- **Tipi non sviluppabili.** I tipi impossibili nella Foresta stanno **commentati nell'enum**, con `// Non fattibile nella Foresta: ...` e il motivo; quelli che non si addicono al tono del gioco `// Non adatto al tono del gioco: ...`; non vanno riproposti. I tipi che per ora non vale la pena sviluppare restano nell'enum con `// Da non sviluppare per ora: ...` e il motivo.
- **Le missioni particolari** (andare a bere in tutte le locande, disturbare dieci eremiti) non sono missioni standard di un gioco fantasy e non si annotano.
- **`RICERCA_OGGETTO`** è coperto da `LOggettoSmarrito` (oggetti comuni e ripetibili); la ricerca dei leggendari è un'altra cosa (`CACCIA_AL_TESORO`, §4). **`RECUPERO`** è coperto dal medaglione e dalle derrate, che sono incarichi di combattimento con la città fissa; per recuperare un oggetto qualsiasi basta una riga in più.
- Una riga di grammatica di molti incarichi dichiara il suo tipo nel campo `TIPO=` (un valore di `TipoMissione`, letto con controllo): è il modo in cui il contenuto si collega alla mappatura del §5.

## 2. Il vocabolario dei passi

Sono metodi di `MissioneAPassi` (protetti, `final`) che restituiscono un `Passo` da completare con `poi` e, se serve, altre azioni con `esegui`. `momento` è il `MomentoControllo` in cui si valuta il passo.

| Sigla | Metodo | Si conclude quando… |
| :--- | :--- | :--- |
| `VAI` | `vai(momento, coordinate)`, `vai(momento, locazioneUnica)` | il gruppo è in quella casella o in quella locazione unica (città, castello) |
| `VAI_INIZIALE` | `tornaAlPuntoDiPartenza(momento)` | il gruppo torna nella casella in cui la missione si è attivata (`getPuntoDiPartenza()`) |
| `DIALOGO` | `dialogo(momento, testo)` | subito: scrive il testo nel pannello e passa oltre |
| `RICOMPENSA` | `ricompensa(momento, monete, testo)`, `ricompensa(momento, Ricompensa, testo)` | subito: scrive il testo e dà monete, preziosi, esperienza, un artefatto (creato al livello di quel momento, nell'inventario del gruppo, con la rivelazione dei cofani) |
| `ATTENDI` | `attendiOre(momento, ore)` | sono passate quelle ore di gioco da quando il passo è corrente |
| `CONTA_FINCHE` | `contaFinche(momento, contatore, N)` | il contatore della missione (`incrementaContatore`, `getContatore`) arriva a N |
| `COMBATTI` (a caso) | `sconfiggi(momento, classe, N)` | il gruppo ha sconfitto N avversari di quella classe, **dovunque**, da quando il passo è corrente |
| `RACCOGLI` (a caso) | `raccogli(momento, classeOggetto, N)` | il gruppo ha raccolto N oggetti di quella classe da quando il passo è corrente |
| `RACCOGLI` (oggetti di missione) | `raccogli(momento, OggettiDaRaccogliere)` | il gruppo ha raccolto quanti ne servono (§3) |
| `CONSEGNA` | `consegna(momento, dove, oggetti, testo)` | la condizione `dove` è vera e il gruppo ha gli oggetti: li consegna (escono dal conteggio) e si scrive il testo |
| `SORVEGLIA` | `sorveglia(dove, volte, oreFraLeVisite)` | il gruppo è passato dalla casella `volte` volte, a inizio locazione, con almeno quelle ore fra una visita che conta e la successiva (le troppo ravvicinate non contano) |
| `ESPLORA` | `esplora(caselleNuove)` | il gruppo è entrato in quel numero di caselle mai visitate da quando il passo è corrente (una casella conta una volta sola) |
| `EVITA_COMBATTIMENTO` | `evitaCombattimento(momento, dove, testoSeScoperti)` | il gruppo arriva alla casella; se nel frattempo ha combattuto, la missione fallisce (per un ramo alternativo: `haCombattutoNelPassoCorrente()`) |
| `COSTRUISCI` | `costruisci(momento, dove, Costruzione, testo)` | il gruppo è sul posto e ha materiali e monete: li consuma, passa le ore, scrive il testo (che cosa si costruisce lo fa la missione con un altro `esegui`) |
| `COMBATTI(bersaglio)` | `combatti(dove, IncontroDiMissione)` | a fine locazione, in quella casella, sono stati sconfitti lì tutti gli avversari dell'incontro, in tutte le ondate |
| `COMBATTI(capo)` | `combattiIlCapo(dove, IncontroDiMissione)` | a fine locazione è stato abbattuto il capo dell'incontro (di un'altra classe dalla banda) |
| `SCORTA` | `prendiInScorta(momento, quando, nome[, vulnerabile])` o con un personaggio fatto dalla missione, poi `scorta(momento, destinazione[, testoSeMuore])` o `scortaFinoAllaMeta(...)` | `prendiInScorta`: quando la condizione è vera uno scortato si unisce al gruppo come **ospite**; `scorta`: il gruppo arriva a destinazione con lo scortato vivo, che si separa; con `testoSeMuore` se muore la missione fallisce; `scortaFinoAllaMeta` si conclude anche, dovunque, se muore (`isScortatoMorto()` sceglie il ramo) |
| `AFFIDA` | `affida(chiave, momento, quando, fornitore)`, `attendiLeAffidate(chiave, momento)` | `affida`: crea e attiva missioni secondarie figlie; `attendiLeAffidate`: sono finite tutte (`sonoRiusciteLeAffidate` dice com'è andata) |
| `CERCA_LOCAZIONE` | `cercaLocazione(momento, classe)` | la missione ha rivendicato una locazione di quella classe (o ne ha costruita una); riprova a ogni controllo se non c'è nemmeno un bosco da sostituire (vedi [`gestione_missioni.md`](gestione_missioni.md) §6) |
| `GENERA_PARAMETRI` | `generaParametri(momento, parametri)`, `parametro(nome, generatore)` | subito: fissa come proprietà i valori variabili (nomi, quantità), ciascuno solo se non c'è già, così restano gli stessi dopo un caricamento (`getParametro`) |

Operazioni sul `Passo` stesso, non sulla missione:

| Metodo | Effetto |
| :--- | :--- |
| `chiediConferma(domanda)`, `chiediScelta(domanda, opzioni)` | domanda al giocatore, sì/no o da 2 a 5 opzioni (`CHIEDI_CONFERMA`, `CHIEDI_SCELTA`); la risposta si legge con `getRisposta(id)` |
| `poi(id)` o `poi(supplier)` | passo successivo fisso, o calcolato dopo l'azione: **`RAMO`**, una diramazione sullo stato di gioco o sulla risposta |
| `falliscoSe(condizione, testo)` | **`FALLISCI_SE`**: guardia che fa fallire la missione (la prima che scatta) |
| `aOgniControllo(azione)` | azione a ogni valutazione del passo (la usano `sorveglia` ed `esplora` per i conteggi) |
| `semina(oggetti)`, `affronta(dove, incontro)` | oggetti o avversari di missione nelle locazioni, finché è il passo corrente |
| `segnala(dove)` | la casella lampeggia sulla mappa e il gruppo la conosce, finché è il passo corrente (una città in cui andare o tornare); concluso il passo il segnalino sparisce. Non rivendica la casella |
| `conIntermezzo(momento, pagine)` | intermezzo dopo la conclusione |

## 3. Oggetti di supporto dei passi

**`OggettiDaRaccogliere`** descrive gli oggetti che esistono solo per la missione (`OggettoMissione`, `TipoOggetto.OGGETTO_MISSIONE`: si ricorda missione, chiave e nome, raccoglierlo incrementa il contatore della missione; come gli altri oggetti delle locazioni non si salva). Si costruisce con `OggettiDaRaccogliere.di(chiave, NomeOggetto, quantità)` e:

| Modificatore | Effetto |
| :--- | :--- |
| `in(classiDiLocazione)` | dove possono comparire (radura, bosco, grotta, rovine, palude…) |
| `conProbabilita(%)`, `alPiuPerLocazione(n)` | quanto spesso e quanti per locazione |
| `daiNemici(classi)` | sono **trofei** custoditi dagli avversari di quelle classi: compaiono dovunque ci siano, anche nelle locazioni già visitate, e per prenderli bisogna sconfiggerli |
| `nellaCasella(coordinate)` | stanno tutti in una sola casella scelta dalla missione (un oggetto smarrito), anche se già visitata; senza ripiego |
| `conRipiegoDopoOre(ore)` | dopo quante ore far ripiegare la missione (default 72) |

Compaiono solo nelle locazioni mai visitate (salvo trofei e casella fissa), mai più di quanti ne mancano, e prendono il posto dell'oggetto che la locazione avrebbe avuto (mai di un artefatto del registro). Il **ripiego**: se dopo le ore previste il gruppo non li ha trovati tutti, a inizio locazione la missione si procura una locazione adatta (`cercaOCostruisci`: per i trofei un bosco con i mostri che li portano), la segna sulla mappa con un avviso ("Un viandante vi segna sulla mappa un posto…") e lì mette tutti quelli che mancano. Il ripiego usa il claim della missione: una missione che ne ha già un altro lo sovrascriverebbe.

**`IncontroDiMissione`** descrive gli avversari che una missione mette in una locazione al posto di quelli normali: `IncontroDiMissione.di(classe, numero)` e

| Modificatore | Effetto |
| :--- | :--- |
| `conCapo(nome[, classe])` | il primo avversario (o uno in più, di un'altra classe) ha un nome proprio e un livello sopra gli altri |
| `finoAllaResa()` | gli avversari, sconfitti, si arrendono; se si arrende tutto il gruppo, il gruppo se ne va e torna per la rivincita |
| `aDuello()` | un solo avversario sfida a duello: uno contro uno, gli altri del gruppo in panchina (un rifiuto lascia lo sfidante dov'è) |
| `poi(ondata, arrivo)` | un'ondata che arriva a quelli di prima sconfitti, con il testo d'arrivo; **al massimo 3 ondate** in tutto, mai con un duello; chi fugge ricomincia dalla prima |
| `aggirabile()` | si può evitare passando inosservati (le guardie di un colpo); gli altri avversari vanno affrontati: non si passa inosservati, non si corrompono e non si fa amicizia con loro |

Gli avversari nascono al livello del mondo. Come panchina, resa, duello e passaggio inosservato agiscono in combattimento è descritto in [`motore_di_gioco.md`](motore_di_gioco.md) §7.

**`Ricompensa`**: `Ricompensa.inMonete(n).conPreziosi(n).conEsperienza(n).conArtefatto(supplier)`. **`Costruzione`**: `Costruzione.con(materiali…).conMonete(n).inOre(n)`. **`Mandante`** e **`AspettoDelMandante`**: chi chiede e come compare nella scena in città (un mandante qualsiasi con l'aspetto del locandiere, il capitano delle guardie, l'armaiolo, l'alchimista, il locandiere); `ScenaInCitta` e `ScenaInLocanda` sono le scene degli intermezzi. Se la missione ha portato in città qualcuno (un ostaggio liberato, un bardo da riportare a casa) lo lascia nel gruppo fino alla fine (`scorta(…, false)`, `scortaFinoAllaMeta(…, false)`; `completaMissione` e `fallisciMissione` lo congedano) e la scena lo trova fra gli ospiti vivi: entra subito dopo il capo e prima degli altri, con la sua classe, e guarda il mandante; `parlaLOspite` dà una battuta al primo. Un colpevole che si arrende (`RESA=SI` con capo) in `LIndagine` si unisce al gruppo con `accogliOspite` a fine combattimento (come `Viandante`); senza colpevole catturato non si mostra nessuno. **`TestiDeiLuoghi`**: come i testi nominano i luoghi.

## 4. Le missioni concrete

Quasi tutte nascono sopra `IncaricoInCitta` (vedi [`gestione_missioni.md`](gestione_missioni.md) §8: incarico in città, accettazione, compito, ritorno, consegna, ricompensa, con la guardia "città distrutta" in ogni passo) o direttamente su `MissioneAPassi`. Quelle senza città fissa sono **ripetibili**: finite, bene o male, ne lasciano una uguale che si offre dopo 48 ore di gioco (salvo diverso).

### Le storie delle città (città fissa)

Le ricompense in monete di questo catalogo sono le paghe del primo livello del mondo: un incarico in città paga quella cifra per il livello di quando si è offerto (20 monete al primo livello, 200 al decimo; vedi [`economia.md`](economia.md)).

| Missione | Dove | Compito | Ricompensa |
| :--- | :--- | :--- | :--- |
| `RecuperaIlMedaglione` | Fleena | un uomo chiede il medaglione di famiglia rubato da una banda di quattro ladri: la missione rivendica una grotta e la segna sulla mappa (`COVO`), si sconfiggono i ladri (`CACCIA`) e si torna in città; nemici, testi e descrizione stanno nella produzione `RECUPERA_IL_MEDAGLIONE` di `missioni.txt` | 20 monete |
| `RecuperaLeDerrateAlimentari` | Ruuna | stessa struttura (`IncaricoDiCombattimentoBase`), con sette Troll ladri di derrate fra delle rovine; produzione `RECUPERA_LE_DERRATE_ALIMENTARI` | 20 monete |

### Combattimenti

| Missione | Compito |
| :--- | :--- |
| `CacciaAiGoblin` | un mercante chiede di liberare le strade: 3 goblin sconfitti dovunque (`sconfiggi`); 15 monete |
| `LaTagliaSullaBanda` | una taglia sul capo di una banda di 3 hobgoblin in un bosco rivendicato e segnato sulla mappa (`combatti`); 30 monete; il capo ha un nome dalla grammatica |
| `CacciatoreDiTaglie` | come sopra, ma la banda (3 goblin con un capo hobgoblin) si nasconde fra delle rovine **non segnate**: il mandante dice solo in che direzione; basta abbattere il capo (`combattiIlCapo`); 30 monete |
| `IncaricoDiCombattimento` | la famiglia più ampia: qualcuno vuole sconfitto qualcosa che si nasconde in un posto della classe giusta (`COVO`, poi `CACCIA`), descritto da una riga di `INCARICO_DI_COMBATTIMENTO` (`CombattimentoRichiesto`): vendette, duelli, battaglie a ondate, imboscate, cariche, blocchi, assedi, bestie, pulizie di dungeon, riti di combattimento, titoli nobiliari buffi, la carovana dell'usuraio... Una riga può avere capo con nome, resa, duello e ondate. La logica sta in `IncaricoDiCombattimentoBase`, da cui derivano anche le storie di Fleena e di Ruuna (una produzione con una riga sola, che scrive anche la descrizione con `DESCRIZIONE=` e `DESCRIZIONE_RITORNO=`) |
| `SconfiggiLaStrega` `SconfiggiIlLich` `SconfiggiIlMinotauroGigante` `SconfiggiLIdra` `SconfiggiIlDrago` | le cinque missioni principali (vedi [`gestione_missioni.md`](gestione_missioni.md) §7) |

### Soccorso e scorta

| Missione | Compito |
| :--- | :--- |
| `LaLiberazione` (base astratta) | qualcuno chiede di riportare a casa una persona in mano a dei nemici: `COVO`, `LIBERAZIONE`, `LIBERATO` (la persona si unisce come **ospite vulnerabile**), `VIAGGIO`; se muore, la missione resta aperta finché il gruppo non torna a dare la notizia (`LUTTO`), poi fallisce (`FALLIMENTO`) |
| `IlRapimento` | una donna chiede di liberare il marito, rapito da 4 goblin e tenuto in una grotta; 35 monete; nomi di ostaggio e capobanda dalla grammatica |
| `IlSoccorso` | riportare a casa un ferito o un prigioniero fra i nemici (il taglialegna fra le arpie, il minatore e il troll…), da righe di `SOCCORSO` (`SoccorsoRichiesto`); è ripetibile |
| `IlPellegrino` | accompagnare un pellegrino a un tempio (rivendicato, segnato, **sicuro**: niente avversari né oggetti a caso all'arrivo) e tornare dalla sorella; `PARTENZA`, `VIAGGIO`, `META`; 25 monete |
| `NonSparateSulPianista` | alla **terza visita** a una locanda nel bosco, a una visita tranquilla, il locandiere affida il bardo ubriaco da riportare a casa nella città più vicina, come ospite vulnerabile; `INCARICO`, `ACCETTAZIONE`, `VIAGGIO`, `ARRIVO`; 20 monete; se muore per strada o la città è distrutta, fallisce; è su `MissioneAPassi` e non su `IncaricoInCitta` perché nasce in una locanda |

### Raccolta e consegna

| Missione | Compito |
| :--- | :--- |
| `RichiestaDiMateriali` (quattro mandanti: `dellAlchimista`, `dellArmaiolo`, `delCapitano`, `delLocandiere`) | materiali da raccogliere (erbe, minerali, pesci) o da prendere a certi mostri (trofei), da `RICHIESTA_*` di `missioni.txt` (`MaterialeRichiesto`: genere, nome, provenienza `LUOGHI`/`NEMICI`, quantità, prezzo per pezzo, battute); `RACCOLTA`, poi consegna; paga tanto per pezzo più cinque |
| `IlCorriere` | portare una cosa da una città a un'altra (`Spedizione`, da `TRASPORTO`): `PARTENZA` (la destinazione compare sulla mappa), `VIAGGIO`; paga di più quanto è lontana; le **spedizioni urgenti** hanno una scadenza (il doppio della distanza, più 12 ore di margine) e se il gruppo arriva tardi la missione fallisce |
| `IlContrabbandiere` | come il corriere, ma di nascosto (righe di `CONTRABBANDO`): se per strada il gruppo combatte, la voce si sparge e la missione fallisce |
| `LOggettoSmarrito` | qualcuno ha perso qualcosa vicino a un posto (tempio, rovine, locanda, grotta; da `OGGETTO_SMARRITO`): il posto compare sulla mappa e l'oggetto sta in una casella a non più di 2 passi (`RAGGIO`), anche già visitata (`nellaCasella`) |
| `IlCartografo` | esplorare da 6 a 10 caselle mai visitate (`esplora`) e tornare a raccontarle |

### Investigazione

| Missione | Compito |
| :--- | :--- |
| `LaSorveglianza` | tenere d'occhio un posto passandoci più volte a ore di distanza (`sorveglia`), poi a volte qualcuno salta fuori da sconfiggere lì (`AGGUATO`); righe di `SORVEGLIANZA` (`SorveglianzaRichiesta`): tombe, accampamenti, torri assediate, galline, rape giganti… |
| `LIndagine` | da due a tre indizi in altrettanti posti segnati uno alla volta (`TRACCIA_n`, `INDIZIO_n`, che resta nella descrizione), poi **la scelta del colpevole** fra 2-5 sospetti (`ACCUSA`): se indovina compare il nascondiglio (`NASCONDIGLIO`, `CATTURA`), se accusa un innocente la missione fallisce (`ERRORE`); righe di `INDAGINE` (`IndagineRichiesta`) |
| `LaDocumentazione` | uno studioso chiede di documentare due o tre posti segnati uno alla volta (`TRACCIA_n`, `REPERTO_n`), poi si consegnano le note |

### Furto e colpi di mano

| Missione | Compito |
| :--- | :--- |
| `IlColpo` | rubare, guastare, appiccare un fuoco, entrare dove non si dovrebbe; il posto è segnato e c'è la guardia: il colpo riesce se il gruppo **passa inosservato** (le guardie sono `aggirabile`); se combatte è scoperto e il colpo fallisce, se fugge può riprovare; righe di `COLPO` (`ColpoRichiesto`) |

### Riti e benedizioni

| Missione | Compito |
| :--- | :--- |
| `IlRituale` | celebrare un rito in un posto segnato: `LUOGO`, `RACCOLTA` degli ingredienti, `RITO` (la domanda a inizio locazione, nel posto **sicuro**): una conferma o una scelta fra 2-4 metodi di cui uno solo giusto (`ERRORE` se sbagliato, `RINVIO` se "non ancora", che fa tornare a chiedere), a volte il rito richiama un `GUARDIANO` da sconfiggere; righe di `RITUALE` (`RitualeRichiesto`) |
| `LaBenedizione` | in una locanda, **dalla terza visita**, un sacerdote offre una benedizione in cambio di un favore: `INCONTRO`, `FAVORE` (affida `IlFavore`), `ATTESA`, `TEMPIO` (compare sulla mappa), `ARRIVO` (sicuro), `SCELTA` di chi la riceve (o `DIRETTA` se c'è un solo personaggio in campo): un modificatore permanente di un attributo; si ripete (48 ore); righe di `BENEDIZIONE` |
| `IlFavore` | missione **secondaria affidata** (da `LaBenedizione` e da `LaLealta`): sconfiggere qualcuno in un posto, raccogliere qualcosa o vegliare un posto (`FavoreRichiesto`); si completa sul posto, e a proseguire è la madre |
| `LaLealta` | una sera, **all'accampamento**, un compagno che non è il capo confida un problema e chiede un favore (`IlFavore`): `INCONTRO`, `FAVORE`, `ATTESA`, `RINGRAZIAMENTO` (un modificatore permanente con la nota `LealtaRichiesta.NOTA`, dopo cui quel compagno non chiede più niente); se il compagno muore o se ne va, fallisce; si ripete con un altro compagno (72 ore); righe di `LEALTA` |

### Leggende e tornei

| Missione | Compito |
| :--- | :--- |
| `LaLeggenda` (`LaLeggendaDellArmaiolo`, `LaLeggendaDelLocandiere`) | un narratore racconta la leggenda di un **oggetto leggendario** (da `leggendari.txt`): `INCARICO` (si pesca un leggendario che nessun'altra leggenda ha già pescato), `ACCETTAZIONE` (sorge su un bosco un **tempio** che lo custodisce, con dei guardiani, segnato sulla mappa), `RECUPERO` (il leggendario non c'è più). Il leggendario lo tiene il gruppo. L'armaiolo racconta in una città qualsiasi; il locandiere dalla terza visita alla locanda. Fra due leggende, di chiunque, passano almeno 36 ore |
| `PescaLeggendaria` | decide quale leggendario esce: a caso, ma una leggenda su due (`PROBABILITA_PEZZO_MANCANTE`, 50%) racconta un pezzo mancante del set più vicino a essere completo; dopo 3 leggende di fila senza pezzi mancanti, con un set cominciato, la successiva lo racconta per forza |
| `OggettoLeggendario`, `SetLeggendario`, `ConLeggendario` | un leggendario (riga di `leggendari.txt`: chiave stabile, proprietà, leggende) e i set (con un moltiplicatore dei bonus se indossati tutti); **ogni leggendario esce una volta sola per partita**, in una sola missione fra leggende e tornei |
| `IlTorneo` | un torneo bandito in città, con un leggendario e una borsa in palio: nella lizza (segnata sulla mappa) due turni e la finale, uno per visita, uno contro uno e **fino alla resa** (`LIZZA`, `PRIMO_TURNO`, `SECONDO_TURNO`, `FINALE`); in finale il campione ha un nome e un livello in più; chi perde un turno può tornare per la rivincita; vinta la finale il leggendario va al gruppo e la borsa si riscuote in città; fra due tornei 72 ore; righe di `TORNEO` (`TorneoRichiesto`) |

### Missioni scritte a mano

`CronacheDiUnFegatoEroico` (un boccale in ogni locanda cittadina, una tappa `VisitaLocanda` per città), `NessunBoccaleLasciatoIndietro` (dieci locande nel mezzo della Foresta: tornare dallo stesso oste non conta, la bevuta è annotata sulla casella), `DisturbatoreDellaQuietePubblica` (dieci eremiti disturbati, contati all'incontro) e le missioni di prova (`MissioneDIProva`, `MissioneCheFallisce`, `MissioneDiProvaSecondariaUno/Due`, `MissioneDiProvaTerziariaUno`), oltre alle classi generiche `Combatti`, `MuoviALocazione`, `VisitaLocanda`, `MissioneSecondaria`.

## 5. Le grammatiche delle missioni

I contenuti variabili sono in `missioni.txt` (e `leggendari.txt`), caricate da `ProduttoreDiTestiCasuale` (vedi [`motore_di_gioco.md`](motore_di_gioco.md) §11). Una riga di grammatica è una lista di campi `CHIAVE=valore` separati da `;`, letta da `CampiDiGrammatica`, che **controlla** i campi: uno sconosciuto, ripetuto o mancante si scopre subito. Una riga lunga si spezza con `\`. Classi di lettura: `CombattimentoRichiesto`, `SorveglianzaRichiesta`, `SoccorsoRichiesto`, `IndagineRichiesta`, `RitualeRichiesto`, `BenedizioneRichiesta`, `FavoreRichiesto`, `LealtaRichiesta`, `ColpoRichiesto`, `DocumentazioneRichiesta`, `TorneoRichiesto`, `MaterialeRichiesto`, `Spedizione`, `OggettoSmarrito`.

- **Nomi di personaggi** (produzioni di nomi): ostaggi, bardi, pellegrini, campioni e campionesse (con nome e epiteto), briganti, maghi, capibanda, goblin.
- **Campi comuni**: `CAPO=` (`CapoDellaRiga`: `SI` per un nome da `NOME_CAPOBANDA`, il nome di un'altra produzione di nomi, o il nome stesso quando i testi lo dicono), `NEMICO=`/`NUMERO=` (una `TipoPersonaggio`, anche le classi del gruppo), `LUOGO=` (grotta, rovine, bosco, palude, radura), `MONETE=`, `TIPO=` (il `TipoMissione`), `RESA=`, `DUELLO=`, `ONDATA_2=`/`ARRIVO_2=` e `ONDATA_3=`/`ARRIVO_3=` (`OndateDellaRiga`), un modificatore permanente `ATTRIBUTO TIPO QUANTITA` (`ModificatoreDellaRiga`, solo aumenti). Nei testi `%CAPO%` è il nome del capo; nei testi non si usa il `;` e non vanno usati `[ ] { } | "` (sintassi di `GrammarBean`).
- **Produzioni**: `RICHIESTA_ALCHIMISTA`/`ARMAIOLO`/`CAPITANO`/`LOCANDIERE`, `TRASPORTO`, `CONTRABBANDO`, `OGGETTO_SMARRITO`, `INCARICO_DI_COMBATTIMENTO` (la più grande, con pezzi comuni per i titoli buffi di Sua Maestà e i nemici delle paludi e delle rovine), `SORVEGLIANZA`, `SOCCORSO`, `INDAGINE`, `RITUALE`, `BENEDIZIONE`, `LEALTA`, `TORNEO`, `DOCUMENTAZIONE`, `COLPO`.
- Le missioni **pescano riga e nomi quando si offrono** e li fissano come parametri: una missione ripetuta pesca altro.

## 6. Mappatura `TipoMissione` → missione

`TipoMissione` conta **190 tipi** in nove `SupertipoMissione`: acquisizione, combattimento, protezione, investigazione, progressione, negoziazione, illecito, relazioni, spirituale. L'elenco e i motivi stanno nei commenti dell'enum (§1). Stato:

- **coperti**: 129, da almeno una missione;
- **non fattibili nella Foresta**: 28 (commentati);
- **non adatti al tono**: 9 (commentati);
- **da non sviluppare per ora**: 6;
- **non coperti, non annotati**: 18, candidati per missioni future.

### Acquisizione

- `RichiestaDiMateriali`: `RACCOLTA_INGREDIENTI`, `RACCOLTA_TROFEI`, `RACCOLTA_CRISTALLI`, `RACCOLTA_ESSENZA`, `MINIERA`, `CACCIA_ANIMALI`, `PESCA`, `FORAGGIAMENTO`
- `RecuperaIlMedaglione`, `RecuperaLeDerrateAlimentari`: `RECUPERO`
- `IlCorriere`: `TRASPORTO`
- `LaLeggendaDellArmaiolo`, `LaLeggendaDelLocandiere`: `CACCIA_AL_TESORO` (la leggenda è l'indizio, i guardiani del tempio, il leggendario il tesoro)
- `LaSorveglianza`: `AGRICOLTURA` (le rape giganti), `ALLEVAMENTO` (le oche da guardia)
- non fattibile: `BORSA`. Non coperti: `ARTIGIANATO`, `COSTRUZIONE` (il passo `COSTRUISCI` esiste ma nessuna missione lo usa)

### Combattimento

- `IncaricoDiCombattimento`: `VENDETTA`, `DUELLO`, `BATTAGLIA`, `IMBOSCATA`, `CARICA`, `CIRCONDAMENTO`, `BLOCCO`, `SCHERMAGLIA`, `SORTITA`, `PONTE_TATTICO`, `ASSEDIO_OFFENSIVO`, `COMBATTIMENTO_RITUALE`, `DUELLO_MAGICO`, `COMBATTIMENTO_BESTIA`, `DUELLO_ANTICO`, `PULIZIA_DEI_DUNGEON`
- `CacciatoreDiTaglie`, `LaTagliaSullaBanda`: `CACCIATORE_DI_TAGLIE`
- le cinque `Sconfiggi*`: `ASSALTO`
- `CacciaAiGoblin`: `GUERRIGLIA`
- `LaSorveglianza`: `ASSEDIO_DIFESA` (la torre assediata), `TRINCEA` (il guado da tenere)
- `IlColpo`: `ESPLOSIONE` (la diga dei goblin)
- non fattibili: `FLOTTA`, `CAVALLERIA`, `BATTAGLIA_AEREA`. Non coperto: `RITIRATA_TATTICA`

### Protezione

- `IlRapimento`: `SALVATAGGIO`, `SCORTA`; `IlSoccorso`: `SALVATAGGIO`, `SOCCORSO`, `EVACUAZIONE`, `PRIMO_SOCCORSO`, `ASILO`; `IlPellegrino`, `NonSparateSulPianista`: `SCORTA`
- `LaSorveglianza`: `DIFESA`, `QUARANTENA`, `CONTENIMENTO`, `VIGILIA`
- `IlCorriere`: `GUARIGIONE`, `EPIDEMIA`, `CURA_MAGICA`, `ANTI_VELENO` (rimedi e antidoti, spesso urgenti)
- `IlRituale`: `POSSESSIONE`, `BARRIERA_MAGICA`, `SANTUARIO`, `PURIFICAZIONE`
- `IlContrabbandiere`: `OCCULTAMENTO`; `IncaricoDiCombattimento`: `PROTEZIONE_TEMPORALE` (i fantasmi del pozzo, per una notte)
- non coperti: `RIFUGIO`, `BLINDATURA`

### Investigazione

- `LIndagine`: `INVESTIGAZIONE`, `RINTRACCIAMENTO`, `RICERCA`, `CURIOSITA_ACCADEMICA`, `CONTROSPIONAGGIO`, `INTERROGATORIO`, `INCHIESTA`, `FORENSICA`, `PROFILING`, `TRACCIA_MAGICA`, `TESTIMONI`, `SCOPERTA_SEGRETO`, `SCOPERTA_INGANNO`, `LETTURA_RUNE`, `DECIFRAZIONE`
- `IlCartografo`: `ESPLORAZIONE`; `LaSorveglianza`: `SORVEGLIANZA`; `LOggettoSmarrito`: `RICERCA_OGGETTO`; `LaDocumentazione`: `DOCUMENTAZIONE`
- tutti coperti

### Progressione

- `IncaricoDiCombattimento`: `COMPETIZIONE` (la gara di magia), `TITOLO_NOBILIARE` (i titoli buffi di Sua Maestà); `IlTorneo`: `TORNEO`; `LaBenedizione`: `BENEDIZIONE_RICEVERE`
- non fattibili: `EREDITA`, `CORONAZIONE`, `LIGNAGGIO`, `LEGATARIO`, `EREDE`, `SUCCESSIONE`. Da non sviluppare: `FAMA`, `REPUTAZIONE` (il gioco non tiene un conto), `RICCHEZZA`. Non coperti: `FAZIONE`, `ADDESTRAMENTO`, `NOMINA`, `FRATELLANZA`, `ASCESA_SOCIALE`, `MAESTRIA`, `SPECIALIZZAZIONE`, `GESTIONE`, `RICOSTRUZIONE`

### Negoziazione

- `IlCorriere`: `DIPLOMAZIA` (il trattato d'alleanza), `MEDIAZIONE`, `CONTRATTO`, `NEGOZIAZIONE_TREGUA`; `IlContrabbandiere`: `MERCATO_NERO`
- non adatti al tono: `COMMERCIO`, `SENSERIA`. Da non sviluppare: `ASTA` (servirebbero rilanci e avversari all'asta). Non coperto: `SCAMBIO_OSTAGGI`

### Illecito

- `IlColpo`: `FURTO`, `SABOTAGGIO`, `FALSIFICAZIONE`, `AVVELENAMENTO`, `VANDALISMO`, `INCENDIO`, `VIOLAZIONE_DOMICILIO`, `SACRILEGIO`
- `IlContrabbandiere`: `CONTRABBANDO`, `CORRUZIONE`, `FRODE`, `DIFFAMAZIONE`, `RICICLAGGIO`, `TRAFFICO`
- `IncaricoDiCombattimento`: `SICARIO`, `BRIGANTAGGIO`, `CONFINAMENTO`; `LaSorveglianza`: `SPIONAGGIO`
- non fattibili: `TRADIMENTO`, `PIRATERIA`, `SEDIZIONE`. Non adatti al tono: `ASSASSINIO`, `RICATTO`, `SCHIAVITU`, `FURTO_IDENTITA`, `FALSA_TESTIMONIANZA`, `TORTURA`, `TENTATIVO_OMICIDIO`. Non coperti: `INGANNO`, `RAPIMENTO`, `IMBROGLIONE`

### Relazioni

- `LaLealta`: `LEALTA`; `IlRituale`: `RISCATTO`, `PERDONO`, `REDENZIONE_PUBBLICA`
- non fattibile: `ALLEANZA_MATRIMONIALE`. Da non sviluppare: `MATRIMONIO`, `CELEBRAZIONE` (sarebbero solo una raccolta di materiali, già coperta)

### Spirituale

- `IlRituale` copre ventiquattro tipi: `SPEZZATURA`, `BENEDIZIONE`, `COMUNICAZIONE`, `VISIONE_PASSATO`, `VISIONE_FUTURO`, `RITUALE`, `NECROMANZIA`, `EVOCAZIONE`, `MALEDIZIONE`, `INCANTESIMO`, `SIGILLO`, `TRASMUTAZIONE`, `ASTRI`, `DIVINAZIONE`, `ILLUSIONE`, `ANTI_MAGIA`, `CHANNELING`, `CONTROLLO_ELEMENTALE`, `LEGAME_SPIRITUALE`, `COMUNIONE`, `TRANCE`, `CREAZIONE_GOLEM`, `ANIMAZIONE_OGGETTI`, `PATTO_ANIMA`
- non fattibili (14): `LETTURA_MENTE`, `INVISIBILITA`, `TELEPORTAZIONE`, `SHAPE_SHIFT`, `VIAGGIO_ASTRALE`, `FUSIONE`, `VIAGGIO_TEMPO`, `REALTA_PARALLELA`, `POSSESSO_CORPO`, `ASSORBIMENTO`, `CONTROLLO_MENTE`, `SCAMBIO_CORPI`, `FISSIONE`, `MORTE_TEMPORALE`

## 7. Osservazioni

- **Pochi scheletri, molta varietà.** Quasi tutto il catalogo si riduce a un piccolo numero di forme: (a) arrivare in un posto, combattere o raccogliere, tornare a riscuotere (`IncaricoInCitta` e le sue figlie); (b) arrivare e rispondere a una domanda con un ramo sull'esito (rituali, indagini); (c) resistere a ondate (battaglie, pulizie di dungeon); (d) tenere d'occhio un posto nel tempo (sorveglianze). Il contenuto vero sta nelle **grammatiche**: una missione nuova di una famiglia esistente è quasi sempre una riga nuova di `missioni.txt`, con il suo `TIPO=`.
- **L'annotazione dei tipi è manuale.** Nessun test controlla che i commenti `// Coperto da:` dicano il vero (le classi citate esistono tutte, ma niente verifica che una classe copra davvero quel tipo, né che un tipo con `// Coperto da:` sia raggiungibile da una riga di grammatica).
- **Il passo `COSTRUISCI` e il tipo `ARTIGIANATO`/`COSTRUZIONE`.** Il passo c'è, ma nessuna missione lo usa: il solo uso nel catalogo è nei test (`ScenarioPassiAvanzatiTest`).
- **Diciotto tipi non coperti e non annotati** (§6) sono i candidati naturali per le prossime famiglie: `RITIRATA_TATTICA`, `SCAMBIO_OSTAGGI`, `INGANNO`, `IMBROGLIONE`, `RAPIMENTO`, `RIFUGIO`, `BLINDATURA`, `ARTIGIANATO`, `COSTRUZIONE`, più i nove di progressione.
- **Le missioni ripetibili si accumulano.** Ogni incarico senza città fissa, finito, ne lascia un altro fra le secondarie di primo livello; per non sovrapporsi, gli incarichi che aspettano una visita tranquilla partono a una per volta (vedi [`gestione_missioni.md`](gestione_missioni.md) §5).
