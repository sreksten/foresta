# Missioni a tappe (`MissioneATappe`)

**Stato: realizzata, restano i contenuti e la documentazione (Fase 3).** Fatti: la grammatica `tappe.txt` (`01-foresta-testi`), il produttore `ProduttoreDiMissioniCasuale.rigaDiTappe(farsa)` e il lettore `TappeRichieste` con i suoi test (`09-foresta-missioni`); la missione `MissioneATappe` (persone, leggendari, farse, **scelta fra due piste** e **chiavi** con la figlia `ChiaveDelCustode`) con i test di scenario in `12-foresta-motore`; la chiave non è un oggetto dell'inventario (conta averla presa) e un vicolo cieco con guardia va sconfitto prima di poter provare l'altra pista. Restano la Fase 3 (§7): più ricette (oggi cinque), le immagini dell'oggetto trovato, scene più varie e la documentazione degli altri file. Questo documento è il piano di azione per un nuovo tipo di missione, più lungo di quelli di oggi: una **catena di tappe**, in cui ogni tappa è un posto da raggiungere e che, una volta raggiunto, dà un'informazione su dove andare dopo, fino a trovare una persona, un oggetto leggendario o i custodi di alcune chiavi. Quando sarà fatto, il documento si riscrive per descrivere il codice (vedi la convenzione in `CLAUDE.md`).

L'infrastruttura di partenza è in [`gestione_missioni.md`](gestione_missioni.md) (registro, momenti di controllo, `Passo`, `MissioneAPassi`, claim) e [`passi_missioni.md`](passi_missioni.md) (catalogo dei passi e delle missioni); gli intermezzi sono in [`intermezzi.md`](intermezzi.md); la sintassi delle grammatiche in [`GrammarBean.md`](GrammarBean.md).

## 1. L'idea

Oggi la missione della falce (`LaFalceDelTristoMietitore`) è: in città qualcuno racconta, si va nelle rovine, si combatte il necromante, fine. Tre passi. Una missione a tappe è invece:

> Qualcuno chiede di ritrovare **X** (una persona, un oggetto). Le prime tracce stanno in un posto. Arrivati, c'è una scena in cui il gruppo si guarda in giro e trova un'informazione che indica il posto successivo. E così via, fino all'ultimo posto, dove sta ciò che si cerca.

Una **tappa** è quindi un posto di una certa **classe** (città, locanda nel bosco, rovine, tempio, grotta, bosco, palude, radura) e si **conclude quando il gruppo ci arriva e riceve l'informazione**. Non serve sconfiggere nessuno: un avversario può comparire, ma è un'aggiunta (la *guardia* di una tappa), non la regola.

Esempi di catene: *città → locanda nel bosco → rovine → il posto dove si nasconde la persona*; oppure *rovine → tempio → il covo con il leggendario*.

Una variante, **le chiavi**: un'ispezione rivela i posti di due o tre custodi sparsi per la foresta; ognuno custodisce una chiave (un oggetto di missione); con tutte le chiavi si apre l'ultimo posto. Le tre ricerche sono **missioni secondarie obbligatorie**, in qualunque ordine, come i campioni di `LaSfidaDeiCampioni` o le quattro figlie del Drago.

## 2. Quello che c'è già e si riusa

| Ingrediente | Dove | Per che cosa |
| :--- | :--- | :--- |
| Passi con id dinamici | `LIndagine` (`TRACCIA_n`, `INDIZIO_n`), `costruisciPasso(id)` | la catena di tappe: `TAPPA_n` si ricostruisce dall'id dopo un caricamento |
| Posto rivendicato e segnato | `cercaLocazione`, `RegistroMissioni.cerca/cercaOCostruisci`, `Foresta.setLocazioneConosciuta`, `Passo.segnala` | scegliere il posto della tappa e farlo lampeggiare sulla mappa |
| Posto sicuro | `RegistroMissioni.sopprimiContenutoLocazione` (come i templi di `LaBenedizione`) | niente mostri all'arrivo |
| Nomi propri di luogo | `rovine.txt`, `ProduttoreDiLuoghiCasuale` (rovine, templi), nome della locanda | i testi possono dire "le Vestigia dell'Antico Impero": `%PROSSIMO%` |
| Intermezzo di passo | `Passo.conIntermezzo(momento, pagine)`, `ScenaFraCompagni.in(luogo, …)` | la scena d'arrivo |
| Guardie e capi con nome | `IncontroDiMissione`, `affronta`, `combatti` | la guardia facoltativa di una tappa; i custodi delle chiavi |
| Missioni affidate | `affida`, `attendiLeAffidate` (`LaSfidaDeiCampioni`, `LaLealta`) | le chiavi in parallelo |
| Oggetti di missione | `OggettiDaRaccogliere`, `daiNemici` | le chiavi custodite |
| Premio leggendario | `ConLeggendario`, `PescaLeggendaria`, `CatalogoLeggendari`, la leggenda in `leggendari.txt` | il leggendario in palio (una volta sola per partita) |
| Incarico in città | `IncaricoInCitta` (`scenaIncarico`, `scenaRingraziamento`, ricompensa, guardia "città distrutta" in ogni passo) | la richiesta iniziale e il ritorno a riscuotere |

## 3. Quello che manca

1. **Una grammatica per catene di lunghezza variabile.** `IndagineRichiesta` ha `INDIZIO_1..3` scritti nel codice, solo luogo e testo, e finisce sempre nella scelta del colpevole. Servono tappe tipizzate, con scena, guardia facoltativa e segnaposto per il nome del posto successivo (§5).
2. **Una scena d'intermezzo per chi cerca.** `ScenaFraCompagni` lancia un'eccezione con meno di due personaggi: a inizio partita il gruppo ne ha uno. Serve una scena per 1-5 personaggi (nuova, o `ScenaFraCompagni` generalizzata).
3. **Un solo claim per missione** (`LOCAZIONE_OCCUPATA`): va bene per una catena, un posto alla volta; le chiavi in parallelo richiedono figlie, una per custode.
4. **Il tempo.** Una catena di 4 tappe lontane costa giorni (il gioco finisce al giorno 40). `RegistroMissioni.cerca` esplora attorno a una casella a caso: serve poterla ancorare alla tappa precedente, perché la catena resti vicina.
5. **Il diario degli indizi.** Il riquadro delle missioni mostra la descrizione; le informazioni raccolte vanno elencate lì (come fa `LIndagine`), ma con 4-5 tappe il testo si allunga.
6. **Le collisioni fra intermezzi** (visita tranquilla, altre scene dello stesso momento), da gestire come per le altre missioni.

## 4. Progetto: `MissioneATappe`

Una classe astratta su `IncaricoInCitta` (nome definitivo scelto dall'utente: `MissioneATappe`; il tipo di missione che copre è `CACCIA_AL_TESORO`, e per le persone `RINTRACCIAMENTO`), con una sola classe concreta per ogni ricetta se serve, altrimenti una sola e le ricette sono righe di grammatica. Legge una riga `TAPPA_MISSIONE` (§5) e costruisce i passi dinamici:

| Passo | Momento | Che cosa fa | Si conclude quando |
| :--- | :--- | :--- | :--- |
| `ACCETTAZIONE` | in locazione, nella città | scena del mandante; rivendica e segna il posto della tappa 1; la missione si attiva | subito |
| `TAPPA_n` | in locazione | il posto è quello rivendicato; se la tappa ha una guardia la locazione ha quegli avversari, altrimenti è **sicura** | il gruppo è nel posto (e, con la guardia, a fine locazione la guardia è sconfitta) |
| `SCENA_n` (intermezzo del passo) | inizio locazione o locazione completata | la scena d'arrivo (§6) | mostrata |
| `INFORMAZIONE_n` (azione di `TAPPA_n`) | alla conclusione | scrive il testo dell'informazione, rivendica e segna il posto della tappa successiva e ne scrive il nome al posto di `%PROSSIMO%`; l'informazione resta nella descrizione | subito |
| `ARRIVO` | in locazione | l'ultimo posto: con `OGGETTO=LEGGENDARIO` guardiani e consegna (come `LaLeggenda`), con una persona un dialogo | oggetto preso / persona trovata |
| `RITORNO`, `RICOMPENSA` | come `IncaricoInCitta` | per le persone si torna a riscuotere; per il leggendario basta il premio | — |
| `CHIAVI` (solo ricetta a chiavi) | a fine locazione | `affida` N missioni figlie (una per custode: posto segnato, capo con nome, la chiave come oggetto di missione) e `attendiLeAffidate` | tutte le figlie finite |
| `BIVIO` (solo ricetta delle due piste) | in locazione | rivendica e segna i due posti; il primo in cui arriva il gruppo decide: se è quello giusto la catena prosegue, se è il vicolo cieco si scrive il testo e il posto giusto resta l'unico segnato | il gruppo arriva nel posto giusto |
| `SCAMBIO` (figlia di un custode che non combatte) | in locazione | tre passi: il custode chiede l'oggetto; l'oggetto sta in una casella con la sua guardia (`OggettiDaRaccogliere.nellaCasella`, `affronta`); si riporta al custode (`consegna`) e si prende la chiave | consegnato |

Tutto ciò che deve sopravvivere a un salvataggio sta in proprietà: la riga di grammatica (come parametro, pescata una volta), le coordinate di ogni posto (`POSTO_n`), il passo corrente e le informazioni già lette.

**Regole.**
- Una classe di posto può ripetersi nella catena, ma **mai lo stesso posto**: ogni rivendicazione esclude i posti già usati dalla missione.
- Per `CITTA` si sceglie una città in piedi diversa da quella del mandante; per `LOCANDA` una locanda nel bosco.
- Ogni passo ha la guardia "la città del mandante è distrutta" (come `IncaricoInCitta`).
- La catena è breve (1-5 tappe) e i posti si cercano **vicino al precedente**.
- Il leggendario si pesca **all'inizio** (`PescaLeggendaria`), perché non lo peschi un'altra missione.

## 5. La grammatica (prova)

La grammatica è `01-foresta-testi/src/main/resources/com/threeamigos/foresta/testi/tappe.txt`, con due radici: `TAPPA_MISSIONE` (una missione seria) e `TAPPA_MISSIONE_FARSA` (una farsa). Quale chiedere lo decide il gioco, con circa **una farsa su tre**: i pesi `[^N]` di `GrammarBean` non sono proporzionali (ogni ramo riceve un aumento in base alla ricchezza dei suoi sottoalberi, vedi [`GrammarBean.md`](GrammarBean.md) §5.2), e con un peso di 7 su 21 le farse uscivano il 14%. `ProduttoreDiMissioniCasuale.rigaDiTappe(farsa)` la legge e ripristina le produzioni a esaurimento a ogni riga; `TappeRichieste` legge la riga. Ho generato 2000 righe e le ho controllate a macchina: campi senza ripetizioni, classi di posto e di nemici valide, scene con almeno due parti, nessuna parentesi quadra rimasta, solo segnaposto ammessi, ogni informazione che nomina il posto successivo, chiavi e custodi tutti diversi nella stessa riga, e nessuna delle costruzioni che danno per scontato il genere o il numero (participi su una persona, "di il", "c'è gli Schinieri"). Sono tutte diverse fra loro, con 133 sequenze di posti diverse; le farse hanno al più due posti, la scelta fra le due piste ha sempre la falsa pista che rimanda all'altra, e un custode in scambio non compare mai più di una volta per riga.

Una riga è una lista di campi `CHIAVE=valore` separati da `;`, come `missioni.txt`:

| Campo | Significato |
| :--- | :--- |
| `CHIAVE`, `TIPO`, `ASPETTO`, `MANDANTE`, `MONETE`, `TITOLO` | come gli incarichi in città |
| `RICHIESTA`, `BATTUTA`, `RISPOSTA` | la scena del mandante; la `RISPOSTA` finisce con "Cominciate `%DA_PROSSIMO%`" |
| `TAPPA_n=CLASSE [NEMICO NUMERO]:informazione` | il posto della tappa n, chi lo custodisce (facoltativo, come `CUSTODE_n`) e ciò che si scopre arrivandoci |
| `SCENA_n=narrazione/battuta del capo/battuta di un compagno` | la scena d'arrivo; il compagno è facoltativo |
| `OGGETTO=LEGGENDARIO` | ciò che si cerca è un leggendario (lo pesca la missione); senza, si cerca una persona |
| `CHIAVI=n`, `OGGETTO_CHIAVE_n`, `CUSTODE_n=CLASSE NEMICO NUMERO CAPO:testo` | la ricetta a chiavi. Con `NEMICO=SCAMBIO` il custode non combatte: cede la chiave in cambio di un oggetto, e `SCAMBIO_n=CLASSE NEMICO NUMERO:oggetto/testo` dice dove sta (un posto con la sua guardia). Il `CAPO` è il nome di una produzione di nomi (`NOME_CAMPIONE`, `NOME_PELLEGRINO`…) |
| `BIVIO`: `PISTA_A=CLASSE [NEMICO NUMERO]:testo`, `PISTA_B=…`, `GIUSTA=A` o `B`, `SCENA_A`, `SCENA_B` | la scelta fra due piste (ricetta 4): dopo la tappa 1 due posti segnati sulla mappa, uno solo porta avanti; l'altro è un **vicolo cieco** (`%A_ALTRA%`: manda all'altro posto), a volte con una guardia, e non fa fallire la missione |
| `BATTUTA_2`, `RISPOSTA_2` | un secondo scambio nella scena del mandante (le farse); l'ultima `RISPOSTA` finisce con "Cominciate `%DA_PROSSIMO%`" |
| `ARRIVO=CLASSE [NEMICO NUMERO]:testo` | l'ultimo posto; con la guardia, la persona è **prigioniera** e va liberata |
| `RINGRAZIAMENTO`, `RICORDO` | il ritorno e la frase per chi rientra in un posto della missione |

**Segnaposto**, risolti dalla missione mentre si gioca (la grammatica non conosce i nomi dei posti, che nascono con la mappa): `%PROSSIMO%` il nome del posto della tappa successiva, con l'articolo ("le Vestigia dell'Antico Impero", "Malgaard"), e le sue forme con la preposizione, `%A_PROSSIMO%` (alle Vestigia…), `%DI_PROSSIMO%`, `%DA_PROSSIMO%`; per il bivio `%PISTA_A%` e `%PISTA_B%`, `%A_PISTA_A%`, `%DI_PISTA_B%`… (i nomi dei due posti) e `%A_ALTRA%` (l'altro posto, nel vicolo cieco); `%OGGETTO%` e `%DI_OGGETTO%` il leggendario; `%CAPO%` il capo di quel custode; `%POSTO_n%` i posti dei custodi. `Articoli.conPreposizione` fa già le preposizioni articolate. I testi **non dipendono da genere e numero dell'oggetto** (può essere "gli Schinieri del Corridore Codardo"): niente pronomi né participi riferiti a lui. Nei testi non si usano `;`, `/`, `[ ] { } | "`.

**Come è fatta.**
- `ARCO` sceglie, fra le missioni serie, la ricetta: *persona*, *oggetto leggendario*, *chiavi*, *due piste* (le proporzioni sono quelle date dal boost di `GrammarBean`: circa 38% due piste, 24% persone, 21% leggendari, 16% chiavi). Le *farse* hanno una radice a parte.
- **Persona:** otto persone possibili (un cantastorie, una guaritrice, un cartografo, una figlia, un fratello, un'apprendista alchimista, un vecchio maestro d'armi, uno scrivano), ognuna con il suo mandante, il suo oggetto personale (il liuto, il mortaio…) e i participi giusti per il genere (`[PS=seduto]`, `[PG=prigioniero]`); 6 percorsi, da 2 a 4 tappe, con informazioni diverse per ognuna delle otto classi di posto; alla fine la persona è libera (peso 3) o prigioniera di una guardia (peso 2).
- **Oggetto leggendario:** sei narratori (un locandiere che ricorda una canzone, i monaci che lasciarono indizi, un maestro armaiolo, una mappa strappata, il diario di un soldato, una filastrocca); 5 percorsi, da 2 a 4 tappe.
- **Chiavi:** quattro contenitori (una volta, un forziere, una porta di torre, un libro sigillato), quattro modi di scoprire i tre custodi, otto custodi possibili e otto chiavi. Per non moltiplicare le missioni secondarie, **al più uno dei tre custodi** (l'ultimo, una volta su tre) non vuole combattere ma uno **scambio**: il cavaliere vuole il medaglione di sua madre, il taglialegna la sua accetta, l'erudito il libro dei sonetti, l'eremita una bottiglia d'annata. L'oggetto sta in un posto con la sua guardia (scheletri, goblin, un troll, ladri) e basta riportarlo: è una consegna in più, non una quarta ricerca.
- **Due piste:** una persona o un leggendario, con la prima tappa che dà due voci che non si accordano (due testimoni, due registri, due finali di una canzone). I due posti hanno classi diverse (sei coppie, in entrambi gli ordini); chi sbaglia trova un vicolo cieco di sei classi, con tre testi ciascuno, che lo rimanda all'altro: si perde una tappa, ma la missione non fallisce.
- **Farse:** due tipi, *lo sgarbo* ("devi andare a punire un tizio che mi ha fatto uno sgarbo", quasi sempre sognato e quasi sempre su cose che nel mondo di gioco non esistono: una macchina, il wifi, una tessera fedeltà, una password, un ascensore) e *il recupero* (il tizio ha preso un giorno di sole, la pazienza, l'ultima fetta di torta, un'idea); sempre con un secondo scambio ("Che cos'è una macchina?" "Non lo so, una cosa che ho sognato"), poco pagate (15-30 monete), con una tappa o nessuna e poi il tizio, a volte protetto da "avvocati" (ladri), "parenti" (goblin) o folletti che lo chiamano capo. I tizi sono tutti uomini, con un epiteto (Gervasio il Puntiglioso, Baldassarre il Distratto…).
- `LUOGO_<classe>` dà il posto con, a volte, una guardia adatta alla classe (scheletri e spettri nelle rovine, goblin nelle grotte, folletti nei boschi, un troll nella palude…).
- `NOME_CHIAVE$` e `CUSTODE_DATI$` sono one-shot: nella stessa riga non escono due custodi uguali. **Attenzione:** con i one-shot va chiamato `reset()` dopo ogni riga (nel gioco `ProduttoreDiMissioniCasuale` non lo fa, vedi §8).

Una riga di esempio, generata dalla grammatica (la ricetta persona, tre tappe, la persona prigioniera in fondo):

```
CHIAVE=TRACCE_DI_Cleofe;TIPO=RINTRACCIAMENTO;ASPETTO=ALCHIMISTA;MANDANTE=l'alchimista;MONETE=50;
TITOLO=Sulle tracce di Cleofe;
RICHIESTA=La mia apprendista, Cleofe, è sparita da quattro giorni, e nessuno sa dove si trovi. Mi ha lasciato un calderone che bolle da tre giorni e un biglietto con scritto solo: torno presto.;
BATTUTA=Da dove cominciamo?;
RISPOSTA=Cleofe cercava un ingrediente che nessuno vende: per forza è andata a cercarlo da qualche parte. Cominciate %DA_PROSSIMO%: vi segno il posto sulla mappa.;
TAPPA_1=CITTA:In una bottega c'è ancora il mortaio di Cleofe, lasciato in pegno: chi lo ha preso in custodia ricorda che Cleofe diceva di voler andare %A_PROSSIMO%.;
SCENA_1=Il gruppo si ferma in piazza a osservare il viavai: da qualche parte c'è un indizio./Guardiamo bene, nessuno è invisibile del tutto./Io sento le chiacchiere.;
TAPPA_2=BOSCO GOBLIN 3:Su un tronco è incisa una freccia fresca, e sotto i rami ci sono impronte che Cleofe riconoscerebbe. Portano verso %PROSSIMO%.;
SCENA_2=I rami sopra il sentiero formano una galleria verde, e il gruppo avanza guardando i tronchi./Cerchiamo segni sulle cortecce./Qualcuno è passato di qui, l'erba è piegata.;
TAPPA_3=RADURA:Un pastore ricorda Cleofe che mangiava pane e formaggio e chiedeva la strada per %PROSSIMO%. Ha lasciato anche la crosta, per gli uccelli.;
SCENA_3=…;
ARRIVO=GROTTA GOBLIN 4:Cleofe è qui, prigioniera in un angolo, e i goblin di guardia non sembrano disposti a mollare. Prima di parlare, sarà necessario convincerli.;
RINGRAZIAMENTO=Cleofe è tornata con l'ingrediente. Il calderone ha fatto un'esplosione piccola e molto contenta.;
RICORDO=Qui l'apprendista Cleofe cercò un ingrediente che non si può comprare.
```

## 6. Gli intermezzi

Per ogni tappa una scena in due momenti, costruita da `SCENA_n`: la **narrazione** ("Il gruppo si muove fra i muri crollati…"), la **battuta del capo** e quella di un **compagno**, se c'è; poi una pagina con l'**informazione** scoperta. Serve una scena per 1-5 personaggi (§3.2): quella dei compagni (`ScenaFraCompagni.in`) richiede almeno due personaggi. Lo sfondo è la classe del posto; l'oggetto trovato può essere un'immagine esistente (una pergamena) finché non ne esiste una nuova.

Le scene scattano a `INIZIO_LOCAZIONE` (prima di un eventuale combattimento) o a `LOCAZIONE_COMPLETATA` (dopo la guardia), come tutti gli intermezzi di passo (vedi [`gestione_missioni.md`](gestione_missioni.md) §5).

## 7. Piano di azione

**Fase 0 — Prova su una missione esistente (piccola).** Riscrivere `LaFalceDelTristoMietitore` come catena di 3-4 tappe, a mano su `MissioneAPassi`, con un'informazione per tappa e una scena di ricerca. Serve a vedere dal vivo i problemi (scena per un personaggio solo, coda degli intermezzi, tempo di una catena) prima di fissare le astrazioni. Va a finire in un test di scenario con `PartitaDiTest`.

**Fase 1 — La base (media).**
- [x] `09-foresta-missioni`: `TappeRichieste` (lettura della riga, con `CampiDiGrammatica` e i campi numerati) e `tipi.ClasseDiPosto`, con 10 test (righe a mano, righe sbagliate, 2000 righe generate).
- [x] `09-foresta-missioni`: `MissioneATappe`, voce in `MissionePredefinita` e `FabbricaMissioni`.
- [x] `01-foresta-testi`: `tappe.txt` (cinque ricette: persona, leggendario, chiavi, due piste, farse; 2000 righe generate tutte valide e diverse) e `ProduttoreDiMissioniCasuale.rigaDiTappe(farsa)`, con tre test.
- [x] `07-foresta-intermezzi`: la scena per 1-5 personaggi (`ScenaFraCompagni` accetta anche un solo personaggio).
- [x] `RegistroMissioni.cerca…` ancorabile a una casella (`cercaVicino`).
- [x] Test: lettura di 500 righe generate (come `ogniLealtaSiLegge`) e scenario completo della ricetta persona, con salvataggio e ricarica a metà.

**Fase 2 — Le chiavi (media).**
- [x] La figlia del custode (`ChiaveDelCustode`) e l'ordine libero delle tre ricerche.
- [x] Test di scenario con le tre figlie e il salvataggio a metà (`TappeABivioEChiaviGiocateTest`, `ChiaveDelCustodeTest`).

**Fase 3 — Contenuti e rifiniture (piccola/media).**
- [ ] 15-20 ricette; immagini per l'oggetto trovato; scene più varie.
- [ ] Documenti: `gestione_missioni.md`, `passi_missioni.md` (tipi `CACCIA_AL_TESORO` e `RINTRACCIAMENTO` coperti), `intermezzi.md`, `foresta.md`; `todo.md`.

## 8. Rischi e questioni aperte

- **Ripetitività.** Una catena di posti e informazioni stanca presto. Le difese sono: catene brevi, guardie facoltative, tappe di tipo diverso (una scelta, una falsa pista, un PNG che si ricorda), riferimenti al mondo (nomi propri dei luoghi, personaggi già incontrati) e molte ricette.
- **`$` e `reset()`** (risolto): `tappe.txt` ha una grammatica a parte, e `rigaDiTappe` ripristina le produzioni a esaurimento dopo ogni riga, senza toccare i nomi condivisi di `nomi.txt`. Un test genera 1500 righe di fila, tutte diverse.
- **Il tempo.** Il gioco finisce al giorno 40: una catena lunga lontana dalla città è una missione che non si completa.
- **Quando si offre.** Come le altre: a una visita tranquilla, con una pausa dalla precedente.
- **Una sola per partita o ripetibile**, con leggendari diversi?
- **Se il giocatore si blocca**: un suggerimento del mandante dopo N ore (come il ripiego degli oggetti raccolti)?

## Osservazioni

- **Il vocabolario è piccolo.** Tutto il tipo si regge su tre idee che il motore ha già (passi con id dinamici, posti rivendicati e segnati, intermezzo di passo) più una lettura di grammatica nuova: il costo è nel contenuto, non nel codice.
- **Nessun avversario obbligatorio.** Una catena di sole informazioni è un tipo di missione nuovo per il gioco: tutte le altre finiscono in un combattimento, in una raccolta o in una consegna.
- **La prova di grammatica non è una promessa di formato.** I nomi dei campi (`TAPPA_n`, `SCENA_n`, `CHIAVI`, `CUSTODE_n`) sono una proposta da rivedere quando si scrive `TappeRichieste`.

## Da fare / idee aperte

- Decidere la Fase 0: riscrivere la falce o partire da una missione nuova.
- Tappe con una scelta (come l'accusa di `LIndagine`) e false piste.
- Riutilizzare i personaggi già incontrati (il registro dei personaggi di `gestione_missioni.md` §11) come testimoni.
- Una tappa con un personaggio che si unisce al gruppo (come il compagno della benedizione).
