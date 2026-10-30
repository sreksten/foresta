# 09-foresta-missioni

Le **missioni**: gli incarichi e le storie che il gruppo porta avanti durante la partita. Il modulo contiene l'infrastruttura (l'interfaccia `Missione`, le due basi `MissioneBase` e `MissioneAPassi`, il `Passo` con cui si scrive una missione come un piccolo automa, il `RegistroMissioni` che le tiene e le controlla), il catalogo delle missioni vere (principali, incarichi in città, leggende, storie), la sfida a carta, forbici e sasso, gli oggetti di missione e gli intermezzi delle missioni.

È un modulo a sé, e **non conosce il motore**: le missioni sono ospiti del ciclo di una locazione, e per quel che non possono sapere da sole (se la locazione è completa, chi riempie il magazzino di una bottega) lo chiedono a chi le ospita attraverso fornitori che il motore installa. Per questo si possono provare senza avviare un gioco.

## Dipendenze

**Dichiarate nel pom:** `08-foresta-economia`, `08-foresta-tattiche`, `07-foresta-mondo`, `08-foresta-bottino`.

**Lo dichiarano:** `10-foresta-trofei`, `10-foresta-modellodati`, `11-foresta-motore-api`.

Le dipendenze sono transitive: un modulo vede anche quello che vedono i moduli da cui dipende, quindi il compilatore garantisce la separazione fra i moduli che non si raggiungono.

- `08-foresta-economia`, `07-foresta-mondo`: le missioni danno ricompense in monete e preziosi, scelgono i residenti del mondo come mandanti e cercano un posto libero sulla mappa.
- `08-foresta-tattiche` (e per transitività combattimento, poteri, personaggi): le missioni mettono in campo avversari e ne leggono gli esiti.
- `08-foresta-bottino`: le missioni mettono nelle locazioni gli oggetti di missione (`OggettoMissione` è un `Bottino`).
- Per transitività anche gli intermezzi, la mappa, le statistiche e gli artefatti: le missioni danno intermezzi propri come `FonteDiIntermezzi`.

## Funzionamento

### L'infrastruttura

- **`Missione`** è l'interfaccia (stato attivo/completo/fallito, descrizione, i controlli del ciclo, le proprietà); le letture che vede la UI sono in `VistaMissione`. Il ciclo di una locazione offre tre controlli, `controllaPreLocazione`, `controllaInLocazione`, `controllaPostLocazione`, più `controllaAccampamento` per le missioni che nascono intorno al fuoco.
- **`MissioneBase`** è la base delle missioni scritte a mano: tiene i dati in `MissioneMD` (nome, descrizione, **proprietà**, missioni figlie) e implementa lo stato e l'albero delle missioni secondarie. Lo stato è nelle proprietà (la presenza della chiave vale per vero), non in campi Java, perché le proprietà si salvano.
- **`MissioneAPassi`** (1300 righe) è la base delle missioni fatte di `Passo`: **un piccolo automa** in cui ogni passo ha un id, dice in quale controllo si valuta (`MomentoControllo`), quando è concluso, che cosa fa una volta concluso e quale passo viene dopo. Il passo successivo non è una posizione in una lista ma un id calcolato dopo l'azione, quindi la missione può diramarsi secondo lo stato del gioco o una scelta del giocatore. Si salva **solo l'id del passo corrente**, più i parametri come proprietà: i passi si ricostruiscono ogni volta da `costruisciPasso(id)`, che deve saper ricostruire ogni id che la missione abbia mai usato. `Passo.FINE` chiude la missione.
- **`Passo`** si scrive con un builder fluente: `Passo.quando(momento, condizione).esegui(...).poi("ID")`, con in più `conIntermezzo`, `chiediConferma` e `chiediScelta` (una domanda al giocatore, da 2 a 5 opzioni), `affronta(dove, incontro)`, `semina(oggetti)`, `segnala(dove)`, `falliscoSe`.
- **I mattoncini**: `IncontroDiMissione` (gli avversari che una missione mette in una locazione al posto di quelli normali, con il capo dalla propria identità, anche a ondate), `OggettiDaRaccogliere` (cosa chiede di trovare un passo di raccolta, in quali locazioni e con che probabilità), `Costruzione` (il passo `COSTRUISCI`: oggetti consumati, monete, ore), `Ricompensa` (monete, preziosi, esperienza e un artefatto creato solo alla consegna, al livello di quel momento), `TestiDeiLuoghi`, `ProprietaDiMissione`.
- **`RegistroMissioni`** (circa 900 righe) tiene le missioni della partita — quelle *predefinite* (`MissionePredefinita.isPredefinita`), che esistono dall'inizio, e le secondarie nate durante il gioco, ognuna in tre elenchi: in corso, completate, fallite — le controlla a ogni locazione, consegna loro gli eventi di gioco (avversari sconfitti, oggetti raccolti, combattimenti) e tiene i conti delle locazioni che le missioni **rivendicano**, segnano sulla mappa o svuotano, e delle ricerche di una casella adatta (`cerca`, `cercaOCostruisci`, `rivendicaPerLocazioneUnica`). I dati sono `RegistroMissioniMD`. `FabbricaMissioni` ricostruisce l'albero dopo un caricamento, dal nome della `MissionePredefinita`.
- **I ganci col motore**: il registro non conosce il motore, quindi riceve ciò che gli serve da chi lo ospita — `setFornitoreLocazioneCompleta` (se la locazione è completa), `setRifornitoreDelleBotteghe` (chi riempie il magazzino di una bottega), `setFornitoreDiDati` — installati da `StatoDelMotore`.

### Il catalogo

| Sottopacchetto | Che cosa contiene |
| :--- | :--- |
| `principali` | `SconfiggiIlDrago` (la missione principale) e le quattro dei suoi alleati (`SconfiggiLIdra`, `…IlLich`, `…IlMinotauroGigante`, `…LaStrega`), più le missioni fisse a tema (`VisitaLocanda`, `DisturbatoreDellaQuietePubblica` sugli eremiti, `NessunBoccaleLasciatoIndietro`, `CronacheDiUnFegatoEroico`, `IlGiroDelleSetteChiese`) |
| `incarichi` | gli incarichi che si prendono in città (il corriere, il rituale, l'indagine, la liberazione, il rapimento, la sorveglianza, la forgia, il cartografo, i negozi…), tutti figli di `IncaricoInCitta`; l'alchimista, l'armaiolo e gli altri mandanti compaiono nella scena in città |
| `richieste` | i dati di ogni famiglia di incarichi (`RitualeRichiesto` per `IlRituale`…), letti da una riga della grammatica `missioni.txt`, con gli aiuti per leggerle: così si cambiano gli incarichi dai file di testo, senza toccare il codice; `TappeRichieste` legge le righe di `tappe.txt`, le missioni a tappe (vedi [`missione_a_tappe.md`](missione_a_tappe.md)), e controlla tutto ciò che la grammatica promette (tappe senza salti, posti e guardie validi, scene, le due piste con il vicolo cieco, i tre custodi con al più uno scambio, le farse con al più due posti, i soli segnaposto ammessi); la classe del posto di una tappa è `tipi.ClasseDiPosto` |
| `leggende` | le leggende degli oggetti leggendari e dei loro set: qualcuno racconta la leggenda e il gruppo può andare a cercare l'oggetto (i leggendari veri sono in `04-foresta-artefatti`) |
| `storie` | le missioni di trama e dei compagni: la benedizione, il favore, la lealtà, il viaggio della moglie del bardo, il pianista a cui non si spara, il torneo di carta, forbici e sasso coi suoi campioni |

`TipoMissione` (152 tipi di missione secondaria che si ritrovano anche nei giochi commerciali; non tutti sono coperti) con `CategoriaMissione` (acquisizione, combattimento, protezione, investigazione, progressione, negoziazione, illecito, relazioni, spirituale); `MissionePredefinita` (49) è l'identificativo con cui il modello dati dichiara la classe di ogni nodo dell'albero, **l'ordine conta** perché nello stesso controllo le missioni si guardano nell'ordine del registro. `Mandante` e `AspettoDelMandante` dicono chi chiede e chi compare nella scena. Il catalogo completo è in [`passi_missioni.md`](passi_missioni.md), il meccanismo in [`gestione_missioni.md`](gestione_missioni.md).

### Altre parti

- **La sfida a carta, forbici e sasso**: `PartitaCartaForbiciSasso` (vince chi arriva per primo a `CostantiMissioni.VITTORIE_PER_VINCERE_LA_SFIDA`; il pareggio non conta, la mossa dell'avversario è tirata con `ContestoLancio.CARTA_FORBICI_SASSO`), `MossaCartaForbiciSasso`, `InternoSfidaCartaForbiciSasso` e le missioni `IlCampione` e `LaSfidaDeiCampioni`. Vedi [`carta_forbici_sasso.md`](carta_forbici_sasso.md).
- **Oggetti di missione**: `OggettoMissione` è un bottino senza classe sua (nome, missione, chiave sotto cui la missione lo conta): lo mette nelle locazioni la missione stessa e, come ogni oggetto delle locazioni, non si salva.
- **Intermezzi**: `IntermezziDelleMissioni` è la `FonteDiIntermezzi` che dà al registro gli intermezzi dei passi conclusi (`IntermezzoDiPasso`, il cui «già mostrato» è una proprietà della missione) e le scene che una missione tiene per sé (`bloccaIntermezzo`); gli epiloghi `IntermezzoEremitaRompiscatole` e `IntermezzoUrloDellaBanshee` stanno qui, e il motore li registra nella fabbrica. `OffertaDelSacerdote` fa offrire al sacerdote che ha commissionato una missione di unirsi al gruppo.
- **Contratti per la UI**: `VistaMissione`, `NotificaAggiornamentoStatoMissione`, `RichiestaSelezioneMissione` (una conferma o una scelta fra 2 e 5 opzioni) e `InternoMissioneCompletata`.

## Osservazioni

- Aggiungere una missione tocca il catalogo (`MissionePredefinita`, `FabbricaMissioni`, la classe, eventualmente una riga di `missioni.txt`) e la documentazione; le regole per chi aggiunge missioni sono in `passi_missioni.md`.
- `MissioneAPassi` e `RegistroMissioni` sono le classi più grandi del modulo; il piano è di tagliarle quando si interviene su di loro.
- Le missioni sono il motivo per cui `10-foresta-trofei` e `11-foresta-motore-api` dipendono da questo modulo: ascoltano `InternoMissioneCompletata` e leggono `VistaMissione`.
