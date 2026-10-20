# Cose da fare

Elenco unico delle cose da fare che non appartengono a un sottosistema preciso. Quando una voce è fatta, si toglie (la storia resta in git).

Le idee aperte dei singoli sottosistemi stanno nei loro documenti:

- missioni: [`gestione_missioni.md`](gestione_missioni.md) §11 e [`passi_missioni.md`](passi_missioni.md) §7;
- artefatti, pergamene, incantatore, bilanciamento: la lista di [`artefatti_e_incantamenti.md`](artefatti_e_incantamenti.md);
- economia: [`economia.md`](economia.md), sezioni "Che cosa non torna" e "Da fare / idee aperte".

## Da fare per prime

- Controllare i bug trovati in assessment.md
- **Alba Sacra e Luce Divina** sono anche abilità innate dei Sacerdoti. In effetti dovrebbero essere unicamente abilità innate.
- **Amicizia**: si basa su un tiro sul carisma, ma se il carisma è superiore al numero di facce il tiro riesce sempre; occorrerebbe mettere un cap.
- **Più incantesimi e un'economia da rivedere.** A metà partita il gruppo non ha più le monete per comprare incantesimi o artefatti: i soldi non bastano e le fonti sono poche. Servono un modo per recuperare più incantesimi e una revisione dell'economia del mondo (vedi [`economia.md`](economia.md), "Che cosa non torna"). Una delle poche strade praticabili, oltre alle missioni, è il bottino lasciato dai mostri sconfitti: aggiungervi anche le monete. Oggi non ci sono, e `economia.md` lo dichiara scelta voluta (scheletri e spettri non hanno niente da lasciare, un bottino solo per alcuni sarebbe incoerente): la decisione va rivista, per esempio dando le monete solo alle classi che portano una borsa (vedi `BottinoDegliAvversari` per le pergamene). Da stabilire quante, in funzione del livello del mondo e dell'avversario, senza far crollare i prezzi dei negozi.
- **Viaggio rapido.** Un modo per tornare in una locazione già visitata senza rifare la strada (città, locande, templi...), probabilmente a pagamento o a costo di tempo (ore di gioco, con la notte e gli eventi della linea temporale). Da decidere da dove si parte (mappa a tutto schermo?), cosa succede strada facendo e se vale per ogni tipo di locazione.
- **Incantesimi a più bersagli: il danno va ripartito.** Oggi (`IncantesimoMaleficoImpl.formula`, portata `MULTIPLO`) ogni bersaglio subisce il danno pieno dell'incantesimo, e per questo gli incantesimi fanno strage e rendono troppo forti i personaggi del gruppo. Il danno deve essere ripartito fra i bersagli, in un modo da decidere (per esempio un danno che cala con il numero di bersagli, o con la distanza dal bersaglio principale, vedi la voce sull'area d'effetto). Poi ritarare con le simulazioni (`SimulazionePartiteTest`, `TestScontriDiGruppo`), che oggi sono fatte sul danno pieno, e adeguare la valutazione delle tattiche, che somma i danni attesi su tutti i bersagli (`SceltaIncantesimo.danniTotali` e `misuraEsperta`): deve seguire la stessa regola, altrimenti sceglierebbe per un danno che non è quello vero. Conviene farlo prima delle due voci sui bersagli in "Gioco e contenuti".

## Bug noti

- **Combattimento.** Personaggi di livello 5 pesantemente armati non riescono nemmeno a scalfire un boss come la Strega o il Lich.
- **Bilanciamento col round a coppie.** Le simulazioni (`SimulazionePartiteTest`, `TestScontriDiGruppo`, `TestLogoramentoInizioPartita`) sono portate al round a coppie (oggi lista dei turni con distribuzione in proporzione: `RoundASimulato` replica ancora il vecchio piano, da allineare) e i numeri sono in [`tattiche_di_combattimento_automatiche.md`](tattiche_di_combattimento_automatiche.md) §9: la stima del giocatore automatico è tarata; restano l'Eremita a livello 1-3 e il bersaglio preferito che uccide presto maghi e sacerdoti.
- **Modalità VERTICALE.** `DisplayableCanvasBarraIcone` avanza di 32 px con icone alte 64, e la finestra è larga al massimo 400 px (vedi [`motore_grafico.md`](motore_grafico.md) §12).
- **Schermi alti meno di 804 px.** La barra delle icone copre il fondo del riquadro delle missioni.

## Bugfixing

Sono i difetti trovati dalla revisione completa del 2026-10-19. I codici sono quelli di [`assessment.md`](assessment.md) §5, che ha anche gli scenari: A vale per gravità alta, M per media, B per bassa. "(P)" segna un difetto plausibile, cioè con un innesco raro o non ancora raggiungibile; gli altri sono confermati. Ogni correzione va con un test. Le voci segnate come **regola** cambiano il bilanciamento, e vanno decise prima di toccarle.

### Gravità alta

- **A1, uccisioni ad area non contate.** Gli avversari uccisi da ELETTROCUZIONE, ESPLOSIONE_DI_GAS o RIGETTO sui vicini non passano da `registraMorteAvversario`. Si perdono esperienza, punti e `InternoAvversarioSconfitto`, quindi anche missioni e trofei.
  - Dove: `PersonaggioBase:617-627`, `EsecutoreTattiche:160-219`, `LocazioneBase:1368-1392`.
  - Correzione: a fine azione, `registraMorteAvversario` su tutti gli avversari, non solo sui vivi.
- **A2, il capo cambia senza motivo.** `Gruppo.rimuoviPersonaggio` (riga 438) rimette capo il primo della lista anche quando esce un altro personaggio, e il capo si può spostare con `spostaPersonaggio`.
  - Correzione: riassegnare il capo solo se `p == capo`, e annullarlo se il gruppo resta vuoto.
  - Test: si toglie un non-capo dopo aver spostato il capo.
- **A3, La Benedizione oltre il tetto.** `LaBenedizione.accogliUnSacerdote` (righe 172 e 264) aggiunge il sacerdote anche con 8 personaggi. Con 9, `Comando.ofPersonaggio(8)` lancia un'eccezione in tutti i menu di scelta del personaggio.
  - Correzione: fare la domanda solo se `getNumeroPersonaggi() < MAX_PERSONAGGI_GRUPPO_TOTALE`. Il superamento dei 5 permanenti è voluto.
- **A4, caratteri senza glifo.** `DoomdarkTextProducer` (righe 21 e 52) chiama `getGlyphData` senza protezione, mentre `FontTool.getWidth` salta i caratteri mancanti. Un nome come "Jürgen" o "Bob_" fa lanciare il riquadro del gruppo a ogni fotogramma, e il nome resta nel salvataggio.
  - Correzione: un glifo di riserva, o il salto, in un solo punto e coerente con la misura.
  - Filtrare anche l'input del `Prompt`.

### Gravità media: combattimento e personaggi

- **M3, turno di effetti gratis.** Uscire da CHI_COMBATTE o CHI_DUELLA, o un canto o un dardo che non partono, fanno passare un turno di effetti senza che gli avversari agiscano (`LocazioneBase:456-461, 827-832`).
  - Correzione: `turnoGestitoDalRound = true`, oppure `return` nei rami che non sono azioni.
- **M4, incapaci che agiscono.** Chi è CONGELATO, ATTERRATO o STORDITO può attaccare, formulare e bere con le azioni manuali (`LocazioneBase:1408-1427`, QUALE_FORMULA, CHI_BEVE_*).
  - Correzione: controllare `isIncapaceDiAgire`, oppure escludere gli incapaci dalla scelta.
- **M5, ospiti vulnerabili.** Gli stati non scadono mai, perché tick e pulizia scorrono solo i personaggi (`LocazioneBase:1376-1392`, `Automa:776`).
  - Correzione: includere `getOspitiBersaglio()`.
  - (P) In `PersonaggioBase:606-619` un ospite colpito è trattato come avversario, e la Purificazione cura i mostri.
- **M6, Morte (regola).**
  - La soglia della tattica (`TatticheBase:293, 300`, 100·s/m < 25) non coincide con quella dell'incantesimo (`Morte:61`, s < m/4 con divisione intera), che quindi si ritorce sul mostro.
  - Morte e Silenzio non chiamano `registraEsitoAzione`.
  - La magia "a caso" può pescare Morte su un bersaglio sano.
- **M8, carico senza classe (regola).** `calcolaCaricoMassimo` (`PersonaggioBase:1029-1051`) non moltiplica per `getMoltiplicatoreCarico()`, contro quanto dicono la javadoc e `motore_di_gioco.md` §8. Correggerlo vuol dire ritarare pesi e test.
- **M9, effetti oltre la morte.** Gli effetti di stato restano su chi muore e tornano con la resurrezione (`PersonaggioBase:363-381`).
  - Correzione: `rimuoviTuttiGliEffettiDiStato()` in `muore` o in `resuscita`.
  - Da decidere anche per i modificatori temporanei negativi.
- **M10, reclutati deboli.** `impostaLivelloDopoReclutamento` (`PersonaggioBase:944-953`) alza il livello ma lascia salute e magia del livello 1.
  - Correzione: riscalarle ai nuovi massimi.
- **M11, danno della fuga (regola).** Il danno è fisso, 50-100 punti (`PersonaggioBase:525-527`, c'è già un TODO): al livello 1 è spesso mortale.
  - Proposta: una percentuale della salute massima.

### Gravità media: motore, missioni, trofei e testi

- **M1, notte gratis in locanda.** `GruppoGiocatore.pernotta` dalla Locanda (riga 284) fa riposare ma non chiama `LineaTemporale.mattinoSeguente()` né `eventi(gruppo)`: lo fa solo l'accampamento (`Automa:943-945`).
- **M2, nuova partita sporca.** `Foresta.reimposta` (righe 148-200) non azzera `IntermezziMD` e `NotizieMD`: una nuova partita nella stessa sessione eredita intermezzi già visti e notizie.
- **M12, ricompense promesse sbagliate.** I testi usano la costante `RICOMPENSA` (o `getMonete()`), mentre si paga `getRicompensa()`, che è la base per il livello del mondo.
  - Incarichi: CacciaAiGoblin, LaTagliaSullaBanda, CacciatoreDiTaglie, IlPellegrino, IlRapimento, IlTorneo.
- **M13, il rito cambia posto.** In IlRituale (righe 94-96 e 191-216) il ripiego della raccolta rivendica un'altra casella e sposta `getPosto()`.
  - Correzione: salvare la coordinata del rito in un parametro proprio.
- **M14, sconfitti sommati fra le visite (regola).** In `MissioneAPassi.combatti` a ondata singola (righe 720-753) gli sconfitti di più visite si sommano, anche dopo una fuga.
  - Correzione: azzerare a ogni visita.
- **M15, eremiti contati due volte.** `DisturbatoreDellaQuietePubblica` (righe 55-86) non è idempotente: dopo una domanda in IN_LOCAZIONE l'Automa rifà il controllo (`Automa:1916`) e l'eremita conta due volte.
  - Scrivere il vincolo di idempotenza in `gestione_missioni.md`.
- **M16, trofeo che perde i progressi.** `RegistroTrofei:49` azzera il progresso in sospeso a ogni preparazione di locazione: le missioni completate in PRE_LOCAZIONE (LaBenedizione) non contano per CACCIATORE_DI_TAGLIE.
  - (P) Lo stesso può succedere nell'accampamento.
- **M17, genere delle notizie.** `LETTERA_FINALE` (`ProduttoreDiTestiCasuale:505`) si accorda sempre con il capo, anche quando il soggetto è il ladro o il bardo (`locande.txt`, righe 300, 349 e 511).
- **M18, trofei e classifica fragili.**
  - `TrofeiMD:82`: un nome di trofeo sconosciuto ferma la lettura.
  - `RegistroTrofei:41` ignora il fallimento e alla riga 104 riscrive il file: i trofei si perdono.
  - `GestoreTrofeiSuFile` e `GestorePunteggiSuFile` scrivono sul posto, senza file temporaneo e senza `checkError`.
- **M19, errori del bus silenziosi.** In `BusEventi` (righe 57-79) l'unico iscritto a `InternoException` è lo sniffer, quindi una transizione a metà resta bloccata in silenzio. Un `Error` interrompe la consegna agli altri iscritti.
  - Servono un gestore vero e niente ripetizione dell'errore a ogni tick in `TemporizzatoreJ2SE`.
- **M20, `azzera()` non riparte pulito (P).** `StatoDelMotore.azzera()` lascia i vecchi gruppi nei campi `final` di `Automa` e `LocazioneBase`, e il vecchio `GruppoGiocatore` resta iscritto al bus. `BusEventi.cancellati` non può togliere una lambda.
  - Va risolto prima di usarlo per la ripresa su mobile.
- **M21, salvataggio automatico vecchio (P).** Si cancella solo a fine partita (`Automa:324-347, 1668`).
  - Correzione: cancellarlo anche a partita nuova, dopo un caricamento e all'uscita.

### Gravità media: interfaccia

- **M22, doppio click con qualsiasi tasto.** I `processaDoppioClick*` di ScambiatoreArtefatti (riga 461), ScambiatoreConsumabili (riga 298) e Inventario (riga 367) non controllano il tasto. Un doppio click destro o centrale compra, vende o spende un punto abilità.
- **M23, Gruppo sempre davanti.** In `DisplayableCanvas:443-446` ogni variazione di statistiche, anche di un mostro, rimette davanti il riquadro del Gruppo.
  - Correzione: farlo solo per i personaggi del gruppo.
- **M24, rinomina che resta aperta.** Il `Prompt` per rinominare una tattica resta aperto quando si lascia la schermata, e l'Invio scrive su una bozza ormai scartata (`Prompt:252-264`, `DisplayableCanvasTattiche:199-203`).
- **M25, nuvole della minimappa.** `DisplayableCanvasRiquadroMappa:110` usa l'offset x anche per y nel `clipRect`, così le nuvole non si vedono mai.
- **M26, sprite sulla riga sbagliata.** `DisplayableCanvasRiquadroGruppo:511, 532` disegna gli sprite di Valore e Carisma con `fontHeight * 1` invece di `* 2`.
  - Conviene unificare i nove metodi quasi uguali.
- **M27, rivelazione che accelera.** `SpriteRivelazioneArtefatto:183` avanza di un passo fisso a ogni repaint, quindi muovendo il mouse la rivelazione va più veloce.
  - Correzione: misurare il tempo come fa `SpriteBase`.
- **M28, nuvole della mappa (P).** In `CloudManager:117`, `nextInt` può ricevere un valore ≤ 0. Le nuvole sono statiche e non si azzerano fra una partita e l'altra.
- **M29, menu vuoti (P).** `CasellaADiscesa` e `ComponenteScorrevole` con zero scelte o altezza 0 creano una `BufferedImage` alta 0. Lo stesso in `DisplayableCanvasRiquadroMissioni:72`.

### Gravità bassa: foresta-tipi e foresta-modellodati

- B1. `ArtefattoMD:372`: la descrizione si rilegge con `testo()` invece di `testoFacoltativo()`, quindi null torna "".
- B2. `LettoreCampi.enumerato` (riga 115): un nome di enum non valido deve dare `IOException`.
- B3 (P). `ModificatoreAttributoMD`:
  - null nella nota torna "", e il riletto non è più `equals`;
  - `quantita` si confronta con `==` invece di `Double.compare`;
  - c'è un `import Locale` inutile.
- B4 (P). `MappaProprieta.aggiungiProprieta`: va rifiutato il valore null, che oggi si salva come la stringa "null".
- B5 (P). `ResidenteMD:56`: la chiave `CAMPIONE:<nome>` non passa da `pulisciCampo`.
- B6. `GruppoMD.leggi`, `RegistroPersonaggiMD.leggi` e `RegistroArtefattiMD.leggi` aggiungono invece di sostituire: svuotare, oppure allineare la javadoc.
- B7. `GruppoGiocatoreMD:222`: con un file troncato si aggiunge null agli ospiti; usare `LettoreCampi`.
- B8 (P). `ForestaMD`: `offset()` non controlla i limiti, e gli array restano null prima di `reimposta`.
- B9. `CoordinateMD`: va resa immutabile, perché è usata come chiave di mappa (i setter non hanno chiamanti).
- B10 (P). `TipoDanno.getEffetti()` espone l'array interno, e `ModelloDati.istanza` non è `volatile`.
- B11. `Costanti:249` ha un commento fuori posto. `Comando.SINGOLO`, `RUTTOLOMEO` e `STORPSGORBLIN` sono usati solo dalle icone.

### Gravità bassa: foresta-contratti

- B12. `Misc.getDirezione`: a distanza 0 esce "a zero ore di cammino verso ovest", per esempio quando la città distrutta è proprio quella in cui sta il gruppo.
- B13 (P). `Misc.getOrdinaleImpl`: dà "nullo" per 0 e un'eccezione da 42 in su.
- B14. Costanti di `TipoEvento` sbagliate:
  - `InternoException` usa INTERNO_ERRORE;
  - in swing, `InternoCreazioneSpriteInDissolvenza` e `…FumettoATempo` usano …A_TEMPO.

  Inoltre:
  - (P) `ThreadPrincipale.SUBITO`, senza esecutore installato, non accoda;
  - c'è un `UUID.randomUUID()` per ogni evento;
  - `Dado:101` può andare in overflow su intervalli enormi.

### Gravità bassa: foresta-motore

- B15. `PersonaggioBase.subSalute`: manca il controllo `isVivo()`, quindi la morte viene annunciata due volte (collasso entropico, doppio danno periodico, doppia resa).
- B16. `PersonaggioBase:1130-1135`: chi muore per veleno o sanguinamento risulta "morto per troppa codardia" (FIXME).
- B17. `PersonaggioBase:127-131`: il flag PNG dipende dal costruttore dei residenti (`(int livello)`), e BARDO e BARDO_DELLE_LOCANDE non sono coerenti.
- B18. `PersonaggioBase:796`: `" di " + nome` dà un doppio spazio; deve essere `"di " + nome`.
- B19. Piccoli difetti in `PersonaggioBase`:
  - `subStanchezza` notifica un valore negativo;
  - la variazione temporanea del carico conta i modificatori due volte (riga 1638);
  - la notifica di morte esce prima di `setSalute(0)`, e `resuscita` non notifica la salute;
  - `limitaEntroMassimi` deve restituire almeno 0.
- B20. `LocazioneBase`:
  - SINGOLO_ATTACCO è offerto in mischia ma ignorato (righe 969-971);
  - i rami irraggiungibili delle righe 437-440 e 1528-1533 (MAPPA/INVENTARIO in combattimento, e IN_COMBATTIMENTO non ha un gestore d'ingresso).
- B21 (P). `CalcolatoreCombattimento:674, 705`: manca la guardia `getForza() > 0` della riga 725.
- B22 (P). `Requisiti:201-207`: il Canto ipnotico non esclude gli immuni.
- B23 (P). `Castello*.azzeraLocazione`: non chiamano `super`.
- B24 (P). `Automa:1482, 1517, 1531, 1566, 610`: i comandi inattesi lanciano un'eccezione invece di essere ignorati con `comandoNonValido`.
- B25. `LineaTemporale.oreFinoAlMattino`: usare `PRIMA_ORA_DEL_MATTINO` invece di 8.
- B26 (P). `GestoreSalvataggiSuFile`:
  - manca `sync()` prima della rinomina;
  - su Windows, se la seconda rinomina fallisce, il salvataggio resta solo nel `.tmp`.
- B27. `NonSparateSulPianista:187-208`: la condizione rivela la città più vicina anche quando la missione non parte.
- B28 (P). Missioni:
  - `LOggettoSmarrito`: NPE con il ripiego in città;
  - `IlNegozio.trovaLeRovine`: eccezione senza un ripiego sulla mappa intera;
  - `LaLealta`: fallisce anche se il compagno poi risorge (regola);
  - le copie degli incarichi ripetibili non annunciano "NUOVA MISSIONE";
  - `MissioneBase.completaMissione` non ha una guardia contro la doppia chiamata.
- B29 (P). `IntermezzoLibroTecnico.daLeggere`: è statico e non si salva.
- B30. Testi:
  - `SconfiggiLIdra:52`: "è stato sconfitta";
  - `Informazioni:396`: doppio spazio.
- B31. `TipoMissione`: mancano le note "// Coperto da:" per LaSfidaDeiCampioni, IlCampione, IlGiroDelleSetteChiese, CronacheDiUnFegatoEroico, NessunBoccaleLasciatoIndietro e DisturbatoreDellaQuietePubblica.
- B32. `GrammarBean`:
  - `[chiave]` su una chiave solo assegnata passa la validazione ma fallisce a runtime;
  - le produzioni derivate dipendono dall'ordine della HashMap;
  - il messaggio della riga 1142 stampa la riga invece del numero.
- B33 (P). `ProduttoreDiTestiCasuale`:
  - `indexOf("-")` (riga 427), `lastIndexOf('/')` (riga 343) e `split("/")` (riga 557) senza controlli;
  - gli stream delle grammatiche non vengono chiusi (righe 62-90; usare `fromResource`);
  - `locande.reset()` è inutile, e la javadoc delle notizie è fuorviante.
- B34 (P). Altri:
  - `GeneratoreArtefattiTabelle.conGrado` dà per scontato il nome comune all'inizio;
  - `Informazioni:384` chiama `tiraAncheAUnaFaccia(0)` senza castelli;
  - `RegistroTrofei:104` dà una NPE senza un gestore impostato.
- B35. Codice morto nel motore:
  - in `PersonaggioBase`: `calcolaStanchezza`, `subCarisma`, `add<Primario>`, `getQuantitaEffettoDiStato` e `puoPrendere(Artefatto)`;
  - `InternoPersonaggioArreso` non ha nessun iscritto;
  - i lanci di `Dado` sprecati in `impostaValoriDiPartenza`;
  - i passi di `MissioneAPassi` usati solo dai test;
  - le produzioni morte: `fiabe.txt` (ISTANTE_PRECISO_OOP, GIORNO_DELLA_SETTIMANA, OTTENUTO, RACCONTO_BREVE) e `locande.txt` (GESTORE_NOTE, NOTIZIE_CITTA, 12 notizie che non escono mai).

### Gravità bassa: foresta-swing e foresta-app

- B36. Testi vuoti o sconosciuti:
  - `Notiziario`: una notizia vuota dà un'immagine larga 0, e i caratteri non supportati scrivono nel log a ogni fotogramma;
  - `substring(0, 1)` su un nome vuoto (`DisplayableCanvasScambiatoreArtefatti:295, 316`, `DisplayableCanvasIncantatore:52`);
  - `DisplayableCanvasIntroOutro.selezioneSlotDaSalvare` non ha il try/catch.
- B37. Riquadri:
  - `setRitaglio(areaLista)` passa il Rectangle vivo (`RiquadroGruppo:264`, `RiquadroIncantesimiEPozioni:192`);
  - `setClip(null)` invece di ripristinare il clip (righe 217 e 135);
  - `RiquadroLocazione:322` usa le coordinate degli avversari senza controllo di null;
  - `DisplayableCanvas:1205` fa la dissolvenza di morte anche a un PNG che risorge.
- B38. Comportamento:
  - gli `offsetYZona*` degli scambiatori non si azzerano alla riapertura;
  - gli `spriteLocali` sopravvivono alla chiusura;
  - "Nuova voce" non mostra Sì/No (`isModificata` ignora le voci vuote);
  - la bozza delle tattiche si perde cambiando personaggio;
  - "Posto 1 di N" per chi non è permanente;
  - i nomi del riquadro di combattimento si accorciano di due caratteri alla volta;
  - il menu a discesa non si apre sulla scelta corrente;
  - `TestoTattica` usa `Locale.ITALIAN`;
  - il nome della classe in tattiche viene da `name()` invece che da `Testi`;
  - il javadoc di `riferimentoTitoloAllaQuota` è sbagliato;
  - `DisplayableCanvasBarraIconeDock:189` lascia NEAREST_NEIGHBOR;
  - `RiquadroMissioni:203` non limita `offsetY`.
- B39. Sprite:
  - in `SpriteAnnuncioGlobale` la misura degli spazi (10) non coincide con il disegno (11);
  - (P) in `SpriteFumetto:122` la punta usa la larghezza massima.
- B40 (P). Thread e avvio:
  - `ForestaUI:87`: l'`invokeLater` va messo dopo le iscrizioni;
  - il thread animatore di `DisplayableCanvas` legge campi non volatili;
  - `fermaThreadAnimazione` è morto, e `animatoreInAzione` si imposta tardi;
  - il `Prompt` si riprende il fuoco da altre finestre;
  - `BufferedImageBuilder:36` chiama `System.exit(1)` anche nei caricamenti pigri;
  - il timer del click singolo non si ferma cambiando schermata.
- B41. Codice morto in swing:
  - quattro eventi con sprite mai pubblicati;
  - `ImageCache.imageMap`, `set`/`get(String)` e lo shutdown hook inutile;
  - `DoomdarkTextRectangle` (non 2x) e `NegozioAlchimistaPrecedente.gif`;
  - il doppio caricamento di `spriteAumentoLivello`;
  - `variaLivello` e il parametro `locazione` di `xOggetto` in `RiquadroLocazione`;
  - `ComandiPossibili.add`/`set` usati solo dai test;
  - un javadoc fuori posto in `DisplayableCanvasScambiatore`.
- B42. `foresta-app` e strumenti:
  - `SnifferBusEventi:114-115` si iscrive due volte a `InternoVenditaArtefatto`;
  - `SnifferBusEventi:395` costruisce la stringa di log fuori dal fornitore;
  - `SnifferBusEventi:315` ha l'etichetta doppia;
  - `Main` ignora senza log gli argomenti sconosciuti, e `ZOOM` non valido esce con un'eccezione nuda;
  - in `FinestraDiProva.PannelloDadi` la tendina non mostra il valore del trucco attivo.

## Architettura

- **Rivedere i javadoc di classi e metodi** (in italiano) per la didattica: fatto per `foresta-modellodati`, `foresta-tipi`, `foresta-contratti` e, in `foresta-motore`, per `personaggi`, `oggetti`, `motore`, `combattimento`, `locazioni`, `incantesimi`, `canti`, `offerte`, `trofei`, `testi`, `salvataggi`, `intermezzi` e `missioni` (con i cinque sottopacchetti); e `tattiche` (con i sottopacchetti `rose` e `valutazione`) del motore; fatto anche per `foresta-swing` (con i suoi sottopacchetti) e `foresta-app`: la revisione è completa. La regola: javadoc di classe per tutte, di metodo per quelli non banali (non per i semplici getter e setter), un `package-info.java` per ogni pacchetto.
- `InternoRichiestaAperturaFinestraCombattimento` e `InternoRichiestaChiusuraFinestraCombattimento` usano lo stesso `TipoEvento` (`INTERNO_STATO_FINESTRA_COMBATTIMENTO`): da distinguere, se serve.
- **Salvataggi fuori dal modello dati.** Le classi `…MD` portano ancora `salva` e `leggi` (formato di file). Per toglierli servono getter e setter in quasi tutti i MD (`leggi` scrive direttamente nei campi privati) e un serializzatore per classe, in un solo commit perché `ModelloDati` richiama tutto a catena (circa 870 righe in 24 file); non è necessario per il porting (usano solo `java.io`).
- **Spezzare il motore.** I suoi pacchetti si richiamano a vicenda (`motore`, `personaggi`, `oggetti`, `locazioni`, `missioni`, `incantesimi`...) e per ora restano un modulo solo. Il primo taglio possibile è il modulo `foresta-missioni` (vedi sotto); i leggendari sono già in `oggetti.leggendari`, indipendenti dalle missioni.
- **Modulo `foresta-missioni`.** Nel motore resterebbe il meccanismo (la radice di `missioni`: `Missione`, `MissioneBase`, `MissioneAPassi`, `Passo`, `RegistroMissioni`, `IncontroDiMissione`, `Mandante`, `Ricompensa`, `IntermezzoDiPasso`...); nel nuovo modulo, che dipende dal motore, il catalogo (`missioni.{principali,incarichi,richieste,leggende,storie}`, quasi tutte le 89 classi). Il motore deve prima smettere di nominare il catalogo. Passi, in ordine:
  1. ~~Togliere i riferimenti del motore a missioni concrete~~ (fatto: `ClasseTrofeo` confronta la `MissionePredefinita`; l'intermezzo dell'Urlo della Banshee legge la città da `ProprietaDiMissione`; `LocazioneBase` usa `RegistroMissioni.isPartita/avvia` e `Missione.avviaDaSola`; `getMissionePrincipale` restituisce `Missione`; l'enumerato `TipoMissionePredefinita` è assorbito da `MissionePredefinita`). Resta solo `FabbricaMissioni`, che al passo 2 importa le missioni concrete.
  2. Rendere `FabbricaMissioni` (importa 44 missioni; la usano `RegistroMissioni` due volte e `MissioneAPassi` una) un registro riempito dall'esterno (`ServiceLoader` o una chiamata in `Main`), con `MissioneCheFallisce` dei test registrata allo stesso modo. Il catalogo deve essere registrato prima di ogni rilettura di un salvataggio.
  3. Creare il modulo, spostare i cinque sottopacchetti, sistemare le visibilità (`apri.py`/`apri2.py`) e spostare i circa 40 test che usano missioni concrete (con il `test-jar` del motore); quelli che usano anche la UI restano in `foresta-app`.
  4. `foresta-app` dipende dal nuovo modulo e `Main` (e i test di integrazione) registrano il catalogo; aggiornare `pom`, `CLAUDE.md`, `foresta.md`, `gestione_missioni.md`, `passi_missioni.md`.
  - Facoltativo: dividere `Missione` in un'interfaccia pura in `foresta-contratti` (id, nome, descrizione, stato, `controllaPre/In/PostLocazione`, modello dati e proprietà) e una sottointerfaccia nel motore con i tre metodi sui tipi del dominio (`getOggettoInLocazione`, `getOndateSuccessiveInLocazione`, `getIncontroInLocazione`). Da sola non rompe il ciclo, perché `MissioneAPassi`, `Passo` e `RegistroMissioni` sono classi concrete del motore.
  - Rischio: i passi 1 e 2 toccano salvataggi ed eventi; i passi 3 e 4 sono lunghi ma ripetitivi.
  - Rimane da verificare se `missioni.txt` va con il catalogo (`leggendari.txt` resta nel motore, con gli oggetti).
  - Idea collegata: mettere i file di testo nella cartella del pacchetto che li legge (oggi sono tutti in `…/foresta/motore/`), cambiando i percorsi in `GrammaticaArtefatti` e `ProduttoreDiTestiCasuale`.
- **Messaggi interni costruiti anche quando nessuno li ascolta (peso sulla mobile).** `Automa.processaComando` pubblica sul bus un `InternoMessaggio("Automa in stato " + ... + "; processo Comando " + ...)` a ogni passo della macchina a stati (e di nuovo a ogni passo della cascata automatica in `prosegui`): la stringa si costruisce prima di sapere se qualcuno ascolta, e nella pratica la ascolta solo `SnifferBusEventi`, uno strumento da desktop. Lo stesso vale per gli altri `InternoMessaggio` costruiti con una concatenazione. Sono 18 in tutto (12 in `Automa`); da vedere come evitare il costo (per esempio un evento che porta il testo come fornitore, o pubblicarlo solo se c'è un iscritto), come si è fatto per il `Logger` con `Logger.log(Supplier<String>)`.

## Gioco e contenuti

- Fumetto che attende la chiusura.
- **Tabella delle debolezze e resistenze (provvisoria).** Le voci per classe e categoria in `TipoPersonaggio` e `CategoriaPersonaggio` sono un primo giro dalla tradizione fantasy (vedi [`debolezze_e_resistenze.md`](debolezze_e_resistenze.md)): vanno provate in partita, ritoccate e completate per le classi che non hanno ancora voci (Arpia, Centauro, Minotauro, Gigante, Titano, Goblin, Hobgoblin).
- **Incantesimi che mancano per i tipi di danno non fisici (da fare fra i primi).** Ogni `TipoDanno` non fisico dovrebbe avere almeno un incantesimo che lo infligge. Oggi (`TipoIncantesimo`, più il Dardo arcano e i canti) la situazione è questa:

  | Tipo di danno | Incantesimo oggi | Manca? | Effetto di stato nativo (`TipoDanno.getEffetti`) |
  | :--- | :--- | :--- | :--- |
  | Aria, Acqua, Terra, Fuoco, Fulmine, Gelo, Veleno | Aria, Acqua, Terra, Fuoco, Fulmine, Gelo, Veleno | no | atterrato, bagnato, immobilizzato o accecato, bruciato, stordito, rallentato o congelato, avvelenato |
  | **Sacro** | Luce Divina (innata di Sacerdote e Sacerdotessa, a pergamena per gli altri; colpisce anche lo Scheletro alleato) | no | accecato (interagisce con `MALEDETTO`, `INFETTATO`: Rigetto, Purificazione) |
  | **Necrotico** | solo Morte, che non fa un danno vero: uccide chi ha meno di un quarto della salute massima, altrimenti si ritorce contro chi la lancia (gli toglie un quarto della salute) | **sì**, un incantesimo che faccia danno necrotico come gli altri | infettato (interagisce con `MALEDETTO`: Mietitura) |
  | **Acido** | nessuno | **sì** | nessuno (interagisce con `AVVELENATO`: Reazione Tossica) |
  | **Sonico** | nessuno | **sì** | assordato (interagisce con `ASSORDATO`: Disorientamento) |
  | **Psichico** | nessuno | **sì** | confuso o spaventato (interagisce con `SILENZIATO`: Isolamento Sensoriale) |
  | **Vuoto** | nessuno | **sì** | nessuno (interagisce con `CONFUSO`, `MALEDETTO`: Follia Cosmica, Collasso Entropico) |
  | **Maledizione** | nessuno | **sì** | maledetto |
  | **Arcano** | solo il Dardo arcano (innato di Mago ed Elfo) e Silenzio (che mette `SILENZIATO` senza danno) | **sì**, una pergamena che faccia danno arcano | silenziato (interagisce con `INFETTATO`, `SILENZIATO`, `SPAVENTATO`/`CONFUSO`) |

  Quindi mancano **Acido, Sonico, Psichico, Vuoto, Maledizione**, più un incantesimo **necrotico** vero e proprio (la Morte è un'altra cosa) e una pergamena **arcana** che faccia danno. Il Vuoto e la Maledizione sono i più rari e potenti, da tenere per i livelli alti, per boss o per ricompense (non per tutte le alchimie). Un primo ordine possibile: Maledizione (ha già `MALEDETTO`, cardine della Mietitura), Psichico (`CONFUSO`, `SPAVENTATO`, immunità dei Costrutti già presente), Sonico (si lega ai canti del Bardo). Per aggiungerne uno: costi e prezzo in `Costanti`, voce in `TipoIncantesimo`, `Comando`, `TipoAzione`, `ClasseIcona` e `ImageCache` con l'icona in `img/icone`, la classe in `incantesimi` e `FabbricaIncantesimi`; vedi come si è aggiunta la Luce Divina (`LuceDivina`, `LuceDivinaTest`). Attenzione che `PersonaggioBase.getRepertorioIncantesimi` dà ai mostri magici tutti i malefici, e `FabbricaIncantesimi.casuale` pesca fra tutte le classi (cofani, offerte): decidere per ognuno se i mostri lo lanciano e se finisce fra le pergamene casuali.

- **Boss come Ondate (da valutare).** I boss (Lich, Minotauro Gigante, Strega...) si incontrano da soli e il gioco non distingue un avversario "di rango": trasformare quegli incontri in Ondate (`Ondata`) darebbe un modo naturale di riconoscerli, per il selettore "boss" delle tattiche (vedi [`tattiche_di_combattimento_automatiche.md`](tattiche_di_combattimento_automatiche.md) §6.1).
- **Incantesimi a più bersagli: l'area d'effetto non sono i primi N del gruppo.** Oggi i bersagli sono i primi N vivi nell'ordine del gruppo (`IncantesimoMaleficoImpl.formula` quando lancia il giocatore, `SceltaIncantesimo.bersagli` quando decide una tattica: il bersaglio principale e poi i successivi dell'elenco). Dovrebbe essere un'area attorno al bersaglio principale: se una tattica sceglie il personaggio più debole, vengono colpiti lui e quelli immediatamente prima e dopo di lui nell'ordine del gruppo, o comunque vicini, fino a N in tutto (all'incirca (N-1)/2 per parte). Da decidere: se il bersaglio è il primo o l'ultimo, l'area si sposta (si colpiscono comunque N personaggi) o si accorcia (se ne colpiscono meno di N)? e come ci si comporta con un N pari, dove (N-1)/2 non è intero? Quando lancia il giocatore a mano non c'è un bersaglio principale (si lancia sull'intero gruppo avversario): serve poterlo scegliere, come per gli incantesimi a bersaglio singolo (vedi il TODO in `LocazioneBase`, "far scegliere al giocatore su quale avversario formularlo"). La stessa regola vale per la valutazione delle tattiche.
- **I mostri colpiscono un solo bersaglio anche con un incantesimo a più bersagli.** Per scelta di oggi (`SceltaIncantesimo.bersagli` esclude i mostri), quindi il `NUMERO_BERSAGLI` di un boss come la Strega o il Lich non ha nessun effetto sui suoi incantesimi. Da valutare se farlo valere, ma senza renderli troppo forti: sono boss, e il gruppo li affronta già con difficoltà (vedi "Combattimento" nei bug noti). Da decidere dopo il danno ripartito e l'area d'effetto, che cambiano di molto quanto vale un incantesimo a più bersagli.
- **Chierico e druido.** Mancano due classi classiche. Il **chierico** è il sacerdote che combatte in prima linea: porterebbe l'armatura (cosa che il Sacerdote, per scelta, non fa più) e userebbe mazza e scudo, con gli incantesimi sacri e di cura. Il **druido** è l'incantatore della natura: niente armatura di metallo, bastone, incantesimi di Terra, Acqua, Aria e Veleno. Da pensare insieme a cosa porta oggi a preferire un Sacerdote o un Mago (vedi "Classi" in [`artefatti_e_incantamenti.md`](artefatti_e_incantamenti.md)), con le loro regole in `RegoleEquipaggiamento` e i moltiplicatori in `Costanti`.
- **L'elmo del mago.** Il mago non porta l'armatura, ma l'elmo (come la veste e gli accessori) lo possono portare tutti, e a regola non sta bene nemmeno lui in testa a un mago. Da decidere se anche l'elmo va tolto alle classi che non portano l'armatura (Ladro, Mago e Sacerdote), o se resta per tutti e la differenza la fa il bonus di PARATA (vedi `Costanti.ELMO_PARATA_MINIMA`); nel caso, la regola sta in `RegoleEquipaggiamento.puoUsare`.
- Le botteghe nella Foresta (armaiolo, alchimista, venditore di pergamene, incantatore) ci sono: ognuna nasce dalla missione `IlNegozio` (vedi [`passi_missioni.md`](passi_missioni.md)), una per città. Resta da fare: icone proprie di mappa e di locazione (oggi copie di quelle dei negozi di città e della locanda), e farle nascere anche per altre vie (per esempio incontrandole già fatte).
- Economia: le entrate sono misurate solo fino al livello 5, perché il giocatore automatico di `SimulazionePartiteTest` muore a livello 2-4 e non fa le missioni; farlo vivere più a lungo e fare le missioni, poi eventualmente ritarare i prezzi degli ingredienti: vedi [`economia.md`](economia.md).
- Dimensione ottimale della mappa: `Foresta.DIMENSIONE_X`/`DIMENSIONE_Y`, da tarare con le prove.
- **Iniziativa in combattimento.** Non c'è un "tiro di iniziativa": il gruppo del giocatore comincia sempre per primo (di norma, con `COMBATTIMENTO`, il giocatore sceglie chi), e l'ordine dei turni è quello della lista fissata a inizio combattimento (vedi `tattiche_di_combattimento_automatiche.md` §3). Da valutare un'iniziativa vera, magari legata alla `VELOCITA` dei personaggi, e chi attacca per primo se gli avversari si accorgono del gruppo (oggi `gliAvversariAttaccanoPerPrimi`, un solo attacco fuori dal piano).

## Personaggi non combattenti

I nomi di questi personaggi sono fatti (liste, registro, testi delle missioni, schermate dei negozi): quello che resta e le idee aperte sono in [`nomi.md`](nomi.md) §8.

- **Immagini e tipi propri per borgomastro e levatrice**, e per i mandanti senza personaggio proprio (mugnaio, mercante, fabbro, notaio, vedove...): oggi sono un `Uomo` o una `Donna` (`AspettoDelMandante`), con l'immagine e il verso del locandiere e della moglie del bardo.
- **La levatrice e le altre comparse nelle scene**: il nome c'è, nessuna scena le chiede.
- **Un punto della UI che elenchi i campioni incontrati** (nome, classe, se sconfitti): i dati sono già nel registro dei personaggi; si può mettere nella schermata delle statistiche o in quella dei trofei.
- Le **missioni di scorta** ("salva il locandiere", "salva l'alchimista"...; "Il viaggio di" la moglie del bardo c'è già: `IlViaggioDellaMoglie`) con `MissioneAPassi.prendiInScorta`, al posto del Viandante: `RegistroPersonaggi.costruisciPersonaggio(residente, livello)` ne fa il `Personaggio` da dare al `Supplier`, come già fa `NonSparateSulPianista` con il `BardoLocanda`. Solo il locandiere non è nel registro: il suo nome sta nella locanda (`Locanda.LOCANDA_NOME_LOCANDIERE`).

## Grafica e immagini

- **La veste per il ladro e per il sacerdote.** `img/oggetti/Veste.gif` è una veste da mago, e la rivelazione (`SpriteRivelazioneArtefatto`) la mostra per ogni veste; servono le immagini delle altre due, e il modo di scegliere quella giusta (oggi una veste non sa per chi è).
- **Immagini a metà risoluzione con zoom a runtime** (`LivelloDiZoom`, argomento `ZOOM=n`, predefinito 2). Sono già dimezzate nei file e ingrandite al caricamento: locazioni, personaggi, oggetti, mappa, alfabeto, i loghi PNG, 19 fondi e le icone (composte da `ClasseIcona`). Restano senza zoom:
  - **`img/icone/`**: le icone dei comandi sono 32×32 con sfondo trasparente e le compone `ClasseIcona` (sfondo, ombra, disegno, zoom). Non sono ancora 32×32 `Moneta`, `PietraPreziosa` e `Tempo` (27×28, sprite caricati da `ImageCache`), `Missione-birra-grande` e `ComponenteScorrevole-*` (16×16);
  - **`img/fondinon2x2/`**, 24 fondi non a blocchi 2×2 (`Cartiglio-*`, `SferaMagica`, `SfondoStoria`, `Separatore`, `Separatore-Armature/Armi/Elmi/LibriMagici/Schinieri/Scudi`, `Foreground*` e `Interno*` di Alchimista/Armaiolo/Incantatore/Locanda/VenditoreDiPergamene, `Luna`, `Mimas`, `LunaHHGTTG`): vanno riguardati a mano; una volta sistemati ognuno si dimezza, si rimette in `fondi/` e si carica con lo zoom. Per gli sfondi degli intermezzi va anche estesa la regola per prefisso in `DisplayableCanvasIntermezzo.risorsa()`, che oggi ingrandisce solo `locazioni/` e `personaggi/`.
  - Finché non sono sistemati, i cinque separatori già convertiti sono il doppio degli altri sei.
  - `personaggi/Locandiere.gif` aveva un pixel isolato che non rientrava in un blocco 2×2 (circa x=66, y=14): controllare che dimezzandolo non si sia visto.
  - Per salvare i GIF con Pillow: `optimize=False` e niente `convert()` prima di salvare, altrimenti si altera la palette; confrontare palette e trasparenza con l'originale in git.


## TODO e FIXME nel codice

Gli altri `TODO`/`FIXME` restano nel codice, accanto al punto a cui si riferiscono (venti, sparsi fra `PersonaggioBase`, `LocazioneBase`, `DisplayableCanvas`, `GruppoGiocatore`, `TatticheBase` e altri; in `TatticheBase` gli elenchi delle classi magiche, con Silenzio e con Morte da tenere allineati con le classi, e le abilità proprie dei mostri: rigenerazione del Troll, teste dell'Idra, canto dell'Arpia). Per ritrovarli:

- in IntelliJ, la finestra **TODO** (View → Tool Windows → TODO, oppure ⌘6) li elenca tutti con file e riga sempre aggiornati, e un click porta al punto esatto;
- da terminale: `grep -rn "TODO\|FIXME" --include=*.java .`.
