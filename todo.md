# Cose da fare

Elenco unico delle cose da fare che non appartengono a un sottosistema preciso. Quando una voce è fatta, si toglie (la storia resta in git).

Le idee aperte dei singoli sottosistemi stanno nei loro documenti:

- missioni: [`gestione_missioni.md`](gestione_missioni.md) §11 e [`passi_missioni.md`](passi_missioni.md) §7;
- artefatti, pergamene, incantatore, bilanciamento: la lista di [`artefatti_e_incantamenti.md`](artefatti_e_incantamenti.md);
- economia: [`economia.md`](economia.md), sezioni "Che cosa non torna" e "Proposte".

## Bug noti

- **Non sparate sul pianista.** Ho segnato come conosciuta la città, occorrerebbe metterle anche il segnalino. e la descrizione della missione nel cartiglio.
- **Combattimento.** Personaggi di livello 5 pesantemente armati non riescono nemmeno a scalfire un boss come la Strega o il Lich.
- **Modalità VERTICALE.** `DisplayableCanvasBarraIcone` avanza di 32 px con icone alte 64, e la finestra è larga al massimo 400 px (vedi [`motore_grafico.md`](motore_grafico.md) §12).
- **Schermi alti meno di 804 px.** La barra delle icone copre il fondo del riquadro delle missioni.
- **Scorrimento del riquadro del gruppo.** `DisplayableCanvasRiquadroGruppo` scorre i personaggi di tre righe per volta, diversamente da `DisplayableCanvasRiquadroMissioni`: capire se si può fare come quest'ultimo.

## Architettura (da valutare)

- **Separare motore, modello dati e UI**, a passi (il primo è fatto: il vocabolario comune, `Comando` e gli enum `Tipo*`, sta in `tipi`, che non dipende da nessun altro pacchetto):
  1. dividere gli enum `Classe*` (fatto per le locazioni, `tipi.TipoLocazione` e `locazioni.FabbricaLocazioni`, per i personaggi, `tipi.TipoPersonaggio` e `personaggi.FabbricaPersonaggi`, per le missioni, `tipi.ClasseMissione` e `missioni.FabbricaMissioni`, per gli incantesimi, `tipi.ClasseIncantesimo` e `incantesimi.FabbricaIncantesimi`, per gli oggetti, `tipi.TipoOggetto` e `oggetti.FabbricaOggetti`, per gli intermezzi, `tipi.TipoIntermezzo` e `intermezzi.FabbricaIntermezzi`, e per le offerte, `tipi.TipoOfferta` e `offerte.FabbricaOfferte`: fatto), che oggi sono anche fabbriche (`Arpia::new`...), in un identificativo puro in `tipi` (per esempio `TipoPersonaggio`) e la fabbrica nel pacchetto di dominio; cominciare da quelli che salva il modello dati;
  2. un modello dati fatto solo di dati (fatto: le scelte a caso dei registri e lo scambiatore dei negozi stanno in `RegistroArtefatti`/`RegistroPersonaggi`, `EffettoDiStatoMD` e `IncantamentoMD` sono nel modello dati, che ora dipende solo da `tipi`, ed è stato spostato in `foresta.modellodati`);
  3. viste in sola lettura (interfacce in `interfacce`) per la UI al posto di `GruppoGiocatore.getIstanza()` e simili (fatto: `VistaPartita`, con `VistaGruppoGiocatore`, `VistaGruppo` e `VistaMappa`, implementata da `motore.VistaPartitaMotore` e passata da `Main` alla UI; l'interruttore dell'aiuto manda `ComandoImpostazioneAiuto`; `ComandiPossibili` è passato in `ui`);
  4. i negozi solo a eventi, senza che la UI chiami gli `Automa*` (fatto: gli eventi di apertura portano una `interfacce.VistaScambio`, implementata da `AutomaScambiatoreArtefatti`, e la UI sposta gli artefatti con `ComandoScambioArtefatto` e spende i punti abilità con `ComandoSpesaPuntoAbilita`);
  5. viste in sola lettura anche per personaggi, artefatti e missioni (fatto: `VistaPersonaggio`, `VistaArtefatto`, `VistaMissione`, estese da `Personaggio`, `Artefatto` e `Missione`, più `VistaPezzoDelSet` e `VistaBancoDiLavoro` al posto delle regole statiche dei set e dell'incantatura; gli elenchi aperti o chiusi restano nel dominio e si salvano, e la UI li cambia con `ComandoCommutazioneElenco`);
  6. le ultime dipendenze della UI dal motore (in parte fatto: `Logger` e `Temporizzabile` sono in `strumenti`; trofei e nomi dei personaggi la UI li chiede a `VistaPartita`; `TestataSalvataggio` e `Misc.getDirezione` usano `VistaGruppoGiocatore`; la UI non pesca più dal `Dado` della partita; gli oggetti degli intermezzi, solo dati, restano ammessi; `ui/DipendenzeUITest` impedisce dipendenze nuove). Resta:
     - l'alchimista: `DisplayableCanvasScambiatoreConsumabili` costruisce da sé le offerte con `Costanti.COSTO_*` e `FabbricaIncantesimi.costoAcquisto`, e ricalcola la formula del costo dell'aumento di magia; l'elenco delle offerte, e l'oroscopo che oggi chiede a `ProduttoreDiTestiCasuale` (consumando il caso della partita), dovrebbero arrivare dal motore con l'apertura;
     - `ForestaUI` legge `motore.Stato` dall'evento `InternoStatoDiGioco`: lo `switch` di `gestisciEventoStatoDiGioco` traduce ogni stato dell'`Automa` in una schermata, e `tick()` anima logo o intro in base allo stato memorizzato. La UI ne farebbe a meno con eventi più specifici (che portano già testo e comandi possibili) e un suo enum delle fasi per `tick()`;
     - `DoomdarkTextRectangle2x` legge `Costanti.MASSIMO_MESSAGGI_RICORDATI`;
     - gli eventi del motore portano ancora `Personaggio` e `Artefatto` (la UI li tiene come viste, ma potrebbe chiamarne i metodi che scrivono): è il punto 8. Lo stesso vale per alcuni comandi (`ComandoVendita/Stoccaggio/Prelievo/Spostamento/AcquistoArtefatto`, `ComandoIncantatura`, `ComandoAperturaInventarioGruppo`), da capire se li manda la UI o solo il motore.
  7. i comandi della UI portano identificativi invece delle viste. Oggi `ComandoSpesaPuntoAbilita` e `ComandoAcquistoConsumabile` portano una `VistaPersonaggio` che il motore ritrova nel gruppo per identità, `ComandoScambioArtefatto` e `ComandoCommutazioneElenco` una `VistaArtefatto` da cui il motore torna all'artefatto con un cast (`Artefatto.da`), e `ComandoCommutazioneElenco` una `VistaMissione` di cui il motore usa solo l'id. Si può fare per tutti come per le missioni:
     - **personaggi:** sì. Hanno già un uuid (`PersonaggioMD.getUuid`, lo usano gli ospiti vulnerabili): basta esporlo in `VistaPersonaggio` e far cercare al motore fra personaggi e ospiti del gruppo. È anche più robusto dell'identità, che si perde se un personaggio viene ricreato dal suo modello dati;
     - **artefatti:** sì. Hanno già un uuid (`VistaArtefatto.getUuid`), che è la loro vera identità, perché gli `Artefatto` si ricreano dal modello dati. Il motore però deve sapere dove cercarli: per `ComandoScambioArtefatto` nella parte di partenza dello scambio, che il comando già porta; per `ComandoCommutazioneElenco` gli artefatti cliccabili stanno sempre in una schermata di scambio, quindi il comando porterebbe anche lo scambio e il motore cercherebbe nelle sue due parti. Così sparisce il cast di `Artefatto.da`;
     - **missioni:** il comando porterebbe direttamente l'id invece della vista;
     - **vantaggi:** i comandi diventano solo dati (stringhe e `tipi`), senza riferimenti a oggetti del dominio, e il motore non deve fidarsi di quel che gli arriva: se l'id non corrisponde a niente, il comando si ignora. Lo stesso varrebbe per lo scambio, che si potrebbe identificare con un id invece che con la vista;
     - **costo:** una ricerca per id a ogni comando, trascurabile; in cambio la UI deve tenere l'id insieme alla vista, ma lo ha già dalla vista stessa.
  8. gli eventi del motore portano le viste invece degli oggetti di dominio: `EventoSuPersonaggio.getPersonaggio()` restituisce `VistaPersonaggio`, e così gli altri eventi che portano un artefatto (per esempio `NotificaApprovazioneIncantatura`). Nel motore nessuno legge da questi eventi il personaggio per cambiarlo (gli 11 eventi su `EventoSuPersonaggio` li consuma solo la UI), quindi il cambio è quasi solo di tipi, e chiude il buco del punto 6. Il solo uuid negli eventi non basta, a differenza dei comandi:
     - alla UI servono i dati, non solo l'identità: nome, salute, livello, classe per l'immagine;
     - quando la UI gestisce la notifica il personaggio può non esserci più (un avversario morto esce dal gruppo, un'ondata sostituisce la precedente, un ospite lascia il gruppo), e ritrovarlo per id fallirebbe; per non perderne i dati l'evento dovrebbe portarsene una copia, cioè rifare a mano `VistaPersonaggio`.

     L'asimmetria è voluta: dal motore alla UI le viste, perché la UI legge e mostra; dalla UI al motore gli identificativi (punto 7), perché il motore è il proprietario, ritrova da sé l'oggetto vero e ignora il comando se non lo trova.
- **Separare il modello dati dal motore.** Oggi la UI legge lo stato solo dalle viste di `interfacce` e verso il motore manda solo eventi; quel che resta è nel punto 6 qui sopra. Vedi [`assessment.md`](assessment.md).
- **`TipoLocazione`.** `GROTTA_RECUPERA_IL_MEDAGLIONE` e `ROVINE_RECUPERA_LE_DERRATE_ALIMENTARI` servono ancora? Dovrebbero essere state superate.
- **`Notizie`** si mette in ascolto di una notizia (delle locande) invece di riceverne la pubblicazione: approccio inusuale.
- **Nomi dei modelli dati.** Alcuni non finiscono per `MD` (`ModificatoreArtefatto`).

## Gioco e contenuti

- Quando una città viene distrutta, rimane la "storia" della casella? ("Qui sorgeva la città di ...")
- Carta, forbice e sasso.
- Mostrare in locazione anche i personaggi del gruppo.
- Fumetto che attende la chiusura.
- Come ci sono locande sparse per la foresta, anche qualche negozio (armaiolo, alchimista, venditore di pergamene, incantatore).
- Ricontrollare l'economia partendo da [`economia.md`](economia.md): entrate, uscite, modello per livello e proposte (bottino dei nemici, preziosi che valgono col livello, missioni pagate col livello, prezzi degli ingredienti).
- Dimensione ottimale della mappa: `Foresta.DIMENSIONE_X`/`DIMENSIONE_Y`, da tarare con le prove.

## Il Bardo

Oggi il Bardo non ha nessuna capacità propria ed è la classe più debole (vedi `risorse_e_documenti_vari/analisi_logoramento.md`). Idee, sul modello del dardo arcano di Mago ed Elfo (capacità innata, costa `MAGIA`, non consuma pergamene, è fra le scelte del comando incantesimi, occupa il turno e l'avversario risponde su chi la usa). Ognuna richiede una sua icona.

- **Canto di guarigione** (il gioco non ha un curatore): cura il compagno più ferito (o tutto il gruppo, di meno), circa 30 × √livello aumentato da Saggezza e Carisma, per circa 4 di `MAGIA`.
- **Canto ipnotico o di scherno** (il morale del nemico): `SPAVENTATO` o `CONFUSO` sugli avversari, con la Soggezione del Bardo contro il loro Coraggio. Usa effetti di stato che ci sono già.
- **Ballata di incitamento** (il morale del gruppo): un bonus temporaneo al gruppo (Precisione o danno) per qualche round. Serve un effetto di stato positivo nuovo (per esempio `ISPIRATO`), il suo aggancio in `CalcolatoreCombattimento` e una riga in `interazioni_effetti_di_stato.md`.

## Grafica e immagini

- Immagini degli artefatti che mancano per la rivelazione (`SpriteRivelazioneArtefatto`), da mettere in `img/oggetti`:
  - armi: Mazza, Ascia, Lancia, BastoneMagico;
  - libro magico: LibroMagico;
  - protezioni: Veste;
  - accessori: Talismano, Ninnolo;
  - ingredienti magici (oggi usano l'icona della pergamena): Pergamena, Gemma, Monile, Gingillo, Sigillo.
- Il Viandante (`ClassePersonaggio.VIANDANTE`, chi si fa scortare dalle missioni) usa le immagini del bardo: servono `personaggi/Viandante.gif` e `icone/Viandante-nobordo-piccolo.gif` (`ClassePersonaggioImmagine`).
- `img/oggetti/OggettoMissione.gif` è un sacchetto provvisorio, da ridisegnare: lo usano tutti gli oggetti delle missioni (i materiali delle richieste: erbe, minerali, pesci, trofei; l'oggetto smarrito). In futuro magari un'immagine per ogni oggetto.
- Il sacerdote e la sacerdotessa che offrono una benedizione in locanda (`ScenaInLocanda.conSacerdote`) hanno le immagini del mago e della maga: servono `personaggi/Sacerdote.gif` e `personaggi/Sacerdotessa.gif`.

## TODO e FIXME nel codice

Gli altri `TODO`/`FIXME` restano nel codice, accanto al punto a cui si riferiscono (una ventina, sparsi fra `PersonaggioBase`, `LocazioneBase`, `DisplayableCanvas`, `GruppoGiocatore` e altri). Per ritrovarli:

- in IntelliJ, la finestra **TODO** (View → Tool Windows → TODO, oppure ⌘6) li elenca tutti con file e riga sempre aggiornati, e un click porta al punto esatto;
- da terminale: `grep -rn "TODO\|FIXME" src/main/java`.
