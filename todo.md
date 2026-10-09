# Cose da fare

Elenco unico delle cose da fare che non appartengono a un sottosistema preciso. Quando una voce è fatta, si toglie (la storia resta in git).

Le idee aperte dei singoli sottosistemi stanno nei loro documenti:

- missioni: [`gestione_missioni.md`](gestione_missioni.md) §11 e [`passi_missioni.md`](passi_missioni.md) §7;
- artefatti, pergamene, incantatore, bilanciamento: la lista di [`artefatti_e_incantamenti.md`](artefatti_e_incantamenti.md);
- economia: [`economia.md`](economia.md), sezioni "Che cosa non torna" e "Da fare / idee aperte".

## Bug noti

- **Combattimento.** Personaggi di livello 5 pesantemente armati non riescono nemmeno a scalfire un boss come la Strega o il Lich.
- **Modalità VERTICALE.** `DisplayableCanvasBarraIcone` avanza di 32 px con icone alte 64, e la finestra è larga al massimo 400 px (vedi [`motore_grafico.md`](motore_grafico.md) §12).
- **Schermi alti meno di 804 px.** La barra delle icone copre il fondo del riquadro delle missioni.
- **Scorrimento del riquadro del gruppo.** `DisplayableCanvasRiquadroGruppo` scorre i personaggi di tre righe per volta, diversamente da `DisplayableCanvasRiquadroMissioni`: capire se si può fare come quest'ultimo.

## Architettura

- **Dividere il progetto in moduli Maven**, così che la separazione fra motore e UI (vedi [`motore_di_gioco.md`](motore_di_gioco.md) §1) la garantisca il compilatore e non solo i test. L'idea: una **base** (`tipi`, `modellodati`, `interfacce`, `eventi` con `BusEventi`, gli strumenti neutri e i dati degli intermezzi), il **motore** (tutto il dominio, che resta un blocco: i suoi pacchetti si richiamano a vicenda), la **UI** e l'**applicazione** (`Main`, che li assembla); le risorse si dividono fra i moduli (immagini nella UI, grammatiche nel motore). Prima vanno tolti gli ultimi legami, che si possono controllare con test di dipendenza come quelli che già ci sono:
  - `interfacce` importa `missioni.Missione` (`FornitoreMissione`: farle usare `VistaMissione`, o spostare l'interfaccia in `missioni`) e `strumenti.Punteggio` e `strumenti.TestataSalvataggio` (`GestorePunteggi`, `GestoreSalvataggi`: spostare i due tipi nella base);
  - `strumenti` mescola le utilità neutre (`Logger`, `Misc`, `ModalitaDiProva`, i temporizzatori, i gestori su file di classifica e trofei, `Punteggio`, `TestataSalvataggio`) e quel che dipende dal dominio (`CostruttoreArtefatto` e la sua implementazione, che usano `Artefatto`; `GestoreSalvataggiSuFile`, che usa `GruppoGiocatore` e `LineaTemporale`): le seconde vanno nel motore;
  - `intermezzi` mescola i dati che disegna la UI (`PaginaIntermezzo`, `BattutaIntermezzo`, `ImmagineIntermezzo`, `ElementoIntermezzo`, `Animazione`, `StatoElemento`, `TipoStiramento`, `BattutaProgrammata`, che dipendono solo da `tipi`) e la logica degli intermezzi (che dipende da motore, missioni e locazioni): i dati vanno nella base;
  - gli undici `Interno*` che importano il dominio (le parti di uno scambio, il banco di lavoro, i personaggi) e `SnifferBusEventi` vanno nel motore: sono le eccezioni di `eventi/DipendenzeEventiTest`, e `BusEventi` non deve dipendere da loro;
  - i sei eventi `InternoCreazioneSprite*` e `InternoNotificaViaFumettoATempo` portano tipi della UI (sono le eccezioni di `ui/DipendenzeVersoLaUITest`): li crea e li consuma solo la UI, e vanno nel suo modulo, che userà `BusEventi` della base.

## Gioco e contenuti

- **Gruppo in locazione** (esperimento, `DisplayableCanvasRiquadroLocazione`): il gruppo del giocatore e gli ospiti vivi si disegnano a destra e i mostri a sinistra, ognuno specchiato verso l'altro (`ClassePersonaggioImmagine.getImmagine(tipo, verso)`, che usa i versi di `VersiDeiPersonaggi`), con gli sprite dei danni, degli effetti e della morte al posto giusto. Gli sprite a tempo di salute e magia (anche dei mostri) non si sovrappongono più: se ce n'è già uno vicino e attivo, il nuovo parte più in basso (`primoPostoLibero`), e le variazioni nulle non ne creano. Da guardare in partita: i mostri molto larghi (Drago, 454 px su 390) vengono coperti dal gruppo; in `intermezzi/VersoDiDefault` c'è una seconda tabella dei versi, solo per le classi giocanti, i sacerdoti e il Viandante, da unificare con quella (vedi sotto).
- Idea rimandata: oggi il doppio attacco esiste solo con un'arma equipaggiata nella mano secondaria (Ladro/Ladra, Elfo/Elfa: `PersonaggioBase.getArmaSecondaria`). Si era pensato a un attacco naturale secondario (artigli) per chi ha la mano secondaria vuota, e ad Alakazam di darglielo sempre; per ora Alakazam ripiega solo sugli artigli contro chi è immune al fuoco (`ArmaNaturale.controDifensore`).
- **Unificare le grammatiche dei nomi di persona** con `#include_static` (vedi [`GrammarBean.md`](GrammarBean.md) §4.12): un `nomi.txt` condiviso, con liste one-shot il cui consumo è comune a tutte le grammatiche, toglie le copie di oggi e dà i nomi ai nuovi personaggi non combattenti.
  - Oggi: `locande.txt` ha `NOME` (44), `COGNOME` (48) e `NOME_CASUALE` (con `ATTRIBUTO`, `TITOLO`, `DEGLI`); `fiabe.txt` ha `NOME_PROTAGONISTA` (33), `COGNOME_PROTAGONISTA` (41), i soprannomi e 66 nomi di principesse; `missioni.txt` ha liste separate per ostaggi, bardi, pellegrini, campioni, campionesse, briganti, maghi e goblin; i 18 compagni di `RegistroPersonaggi` sono scritti nel codice. 22 dei 44 nomi di `locande.txt` sono anche in `fiabe.txt`, e 35 dei 41 cognomi delle fiabe sono quelli delle locande.
  - Piano: `nomi.txt` con `NOME_MASCHILE` e `NOME_FEMMINILE` (oggi manca una lista femminile generica), `COGNOME` e soprannomi, **one-shot** (`NOME$`) e sezione statica: un nome uscito in una grammatica non esce più in nessun'altra fino a `GrammarBean.resetStaticProductions()`, da chiamare in `ProduttoreDiTestiCasuale.resetProduzioni()` a inizio partita. Restano nei loro file le liste con un sapore proprio: epiteti dei campioni, soprannomi dei briganti, `NOME_GOBLIN`, `NOME_MAGO` ("Mastro…"), le principesse. **I nomi dei locandieri restano nella grammatica delle locande**: alcuni sono legati alla locanda (`NOME_LOCANDIERE`, salvato in `LOCANDA_NOME_LOCANDIERE`), e il Locandiere-personaggio deve usare quello della sua locanda.
  - **Non c'è ricarica automatica**: le liste devono bastare per tutta la partita (nomi per locande, missioni, fiabe e personaggi). Un pool esaurito pota le alternative che lo usano e, se una radice si svuota, `produce()` lancia `IllegalArgumentException`: conviene contare quanti nomi servono nel caso peggiore e abbondare, e valutare un messaggio più chiaro o un ripiego (`[NOME? | …]`) dove un nome è facoltativo.
  - Le grammatiche che includono una sezione statica vanno caricate con `GrammarBean.fromResource` (oggi `ProduttoreDiTestiCasuale` e `GrammaticaArtefatti` usano ancora il costruttore con gli stream). Le sezioni statiche sono stato globale: i test che creano una partita dovrebbero chiamare `resetStaticProductions()` (e la sequenza dei dadi cambia con chi pesca: attenzione ai test con seme fisso).
  - Per i nuovi personaggi: Armaiolo, Alchimista, Venditore di pergamene e Incantatore con un nome maschile (magari "Mastro"), la Moglie del Bardo con uno femminile; un accessore in `ProduttoreDiTestiCasuale`.
- Fumetto che attende la chiusura.
- Come ci sono locande sparse per la foresta, anche qualche negozio (armaiolo, alchimista, venditore di pergamene, incantatore).
- Economia: le entrate sono misurate solo fino al livello 5, perché il giocatore automatico di `SimulazionePartiteTest` muore a livello 2-4 e non fa le missioni; farlo vivere più a lungo e fare le missioni, poi eventualmente ritarare i prezzi degli ingredienti: vedi [`economia.md`](economia.md).
- Dimensione ottimale della mappa: `Foresta.DIMENSIONE_X`/`DIMENSIONE_Y`, da tarare con le prove.

## Il Bardo

Oggi il Bardo non ha nessuna capacità propria ed è la classe più debole (vedi `risorse_e_documenti_vari/analisi_logoramento.md`). Idee, sul modello del dardo arcano di Mago ed Elfo (capacità innata, costa `MAGIA`, non consuma pergamene, è fra le scelte del comando incantesimi, occupa il turno e l'avversario risponde su chi la usa). Ognuna richiede una sua icona.

- **Canto di guarigione** (il gioco non ha un curatore): cura il compagno più ferito (o tutto il gruppo, di meno), circa 30 × √livello aumentato da Saggezza e Carisma, per circa 4 di `MAGIA`.
- **Canto ipnotico o di scherno** (il morale del nemico): `SPAVENTATO` o `CONFUSO` sugli avversari, con la Soggezione del Bardo contro il loro Coraggio. Usa effetti di stato che ci sono già.
- **Ballata di incitamento** (il morale del gruppo): un bonus temporaneo al gruppo (Precisione o danno) per qualche round. Serve un effetto di stato positivo nuovo (per esempio `ISPIRATO`), il suo aggancio in `CalcolatoreCombattimento` e una riga in `interazioni_effetti_di_stato.md`.

## Sacerdote e Sacerdotessa

Giocanti dal 2026-10-08, sul modello di Mago e Maga ma con l'alba sacra innata al posto del dardo arcano (vedi [`motore_di_gioco.md`](motore_di_gioco.md) §7). Da fare:

- **Bilanciamento**: le statistiche sono una prima proposta (Saggezza 50, Intelligenza 40, 100 PS, magia dell'Elfo, danni fisici ×0,8). Nella simulazione (`SimulazionePartiteTest`, 30 partite per classe, giocatore automatico che non lancia l'alba sacra) arrivano a 7,6 giorni e livello 3,6, fra il Mago (10,3 giorni e livello 5,0) e l'Elfo (4,5 e 2,9), il Bardo (3,8 e 2,4) e il Guerriero (3,9 e 2,7): da rivedere con l'uso reale. Il giocatore automatico non sa usare l'alba sacra: se serve, insegnargliela (`GiocatoreAutomatico`).
- **Contenuti**: in `locande.txt` le notizie con `MAGO`/`MAGA` compaiono solo se nel gruppo c'è un Mago o una Maga, e il segnaposto diventa il suo nome (`ProduttoreDiTestiCasuale`, che scambia anche Guerriero/a, Ladro/a ecc.): con un Sacerdote o una Sacerdotessa non scattano, e non c'è ancora una variante per loro (`SACERDOTE`/`SACERDOTESSA`). In `missioni.txt` `MAGO`/`MAGA` sono classi di avversari e sfidanti: controllare se vogliono una variante per i sacerdoti. `leggendari.txt` e `IncontroDiMissione` non c'entrano (c'è solo `ARCIMAGO` e un commento di esempio).

## Verso dei personaggi e personaggi non combattenti

Le immagini dei personaggi guardano a destra o a sinistra. La UI ora lo sa (`ui/VersiDeiPersonaggi`, usata per specchiare il gruppo e i mostri in locazione), mentre gli intermezzi hanno ancora una tabella propria (`intermezzi/Verso`, `VersoDiDefault`, che copre le classi giocanti, i sacerdoti e il Viandante e lancia `IllegalArgumentException` per le altre). Piano, in quest'ordine:

1. **I non combattenti sono fatti** (`TipoPersonaggio`, `PersonaggioNonCombattente` e le sei classi Locandiere, Armaiolo, Alchimista, VenditoreDiPergamene, Incantatore, MoglieDelBardo; BardoLocanda è un'altra immagine del Bardo, non un tipo). Restano da fare:
   - le **icone**: sono copie di quella del Viandante con il nome del personaggio (`icone/Locandiere.gif` ecc.), da ridisegnare;
   - i **versi** di `VersiDeiPersonaggi` e `VersoDiDefault` per i sei: provvisoriamente tutti DESTRA, da controllare sulle immagini;
   - usarli: fatto per `NegozioInScena`, `ScenaInCitta`, `ScenaInLocanda` e gli intermezzi della locanda (ora `ImmagineIntermezzo.personaggio(tipo)`), e `ImageCache` non tiene più le immagini dei commercianti. I mandanti senza personaggio proprio (borgomastro, mugnaio, mercante, fabbro, vedove...) usano l'aspetto del locandiere (`QUALUNQUE`) o della moglie del bardo (`DONNA`): servono immagini loro.
   - le **missioni di scorta** ("salva il locandiere", "salva la moglie del bardo", "salva l'alchimista"...) con `MissioneAPassi.prendiInScorta`, al posto del Viandante: servirà dar loro un nome proprio, perché un personaggio senza nome lancia `IllegalStateException` in `getNome` (come il Viandante);
   - **Viandante**: estende `Bardo` ed è amichevole, e per questo `LaSfidaDeiCampioni.classiAmichevoli()` lo include fra i possibili campioni: valutare se farlo estendere `PersonaggioNonCombattente` (le sue caratteristiche scenderebbero) o escluderlo.
2. **Il verso di ogni personaggio**: la mappa è fatta e i versi sono controllati (`ui/VersiDeiPersonaggi`, tutti i `TipoPersonaggio`, due soli valori `SINISTRA`/`DESTRA`), ed è già usata per specchiare il gruppo e i mostri in locazione. Resta da usarla al posto di `VersoDiDefault` (vedi il punto 3): per le 14 classi che hanno in comune le due tabelle coincidono, quindi l'unificazione è meccanica. Vale per tutti, non solo per le classi giocanti: anche Centauro, Titano, Goblin ecc. possono essere ospiti temporanei.
3. **Migrazione degli intermezzi**: oggi sono le scene a decidere se specchiare (`StatoElemento.specchiato`, `Tappa.specchiata`, `ElementoIntermezzo.specchiato()`, `orientaNelVersoDelMoto(versoDellImmagine)`, con `VersoDiDefault.serveSpecchiare`), cioè il dominio conosce come sono disegnate le immagini. Meglio che le scene indichino il verso voluto (`StatoElemento` porta un `Verso`, o `null` = così com'è) e che `DisplayableCanvasIntermezzo.disegnaElemento()` lo confronti con quello nativo e specchi solo se serve. `VersoDiDefault` sparisce. Circa 14 file (`Tappa`, `StatoElemento`, `ElementoIntermezzo`, `ScenaNegozio`, `ScenaFraCompagni`, gli intermezzi) e le 11 righe di asserzioni di `ElementoIntermezzoTest`. Dopo il punto 1 non restano immagini di personaggi caricate per percorso (`ImmagineIntermezzo.risorsa("personaggi/Locandiere.gif")` diventa `personaggio(tipo)`).

## Grafica e immagini

- Immagini degli artefatti che mancano per la rivelazione (`SpriteRivelazioneArtefatto`), da mettere in `img/oggetti`:
  - armi: Mazza, Ascia, Lancia, BastoneMagico;
  - libro magico: LibroMagico;
  - protezioni: Veste;
  - accessori: Talismano, Ninnolo;
  - ingredienti magici (oggi usano l'icona della pergamena): Pergamena, Gemma, Monile, Gingillo, Sigillo.
- `img/oggetti/OggettoMissione.gif` è un sacchetto provvisorio, da ridisegnare: lo usano tutti gli oggetti delle missioni (i materiali delle richieste: erbe, minerali, pesci, trofei; l'oggetto smarrito). In futuro magari un'immagine per ogni oggetto.
- **Immagini a metà risoluzione con zoom a runtime** (`LivelloDiZoom`, argomento `ZOOM=n`, predefinito 2). Sono già dimezzate nei file e ingrandite al caricamento: locazioni, personaggi, oggetti, mappa, alfabeto, i loghi PNG, 19 fondi e le icone (composte da `ClasseIcona`). Restano senza zoom:
  - **`img/icone/`**: le icone dei comandi sono 32×32 con sfondo trasparente e le compone `ClasseIcona` (sfondo, ombra, disegno, zoom). Non sono ancora 32×32 `Moneta`, `PietraPreziosa` e `Tempo` (27×28, sprite caricati da `ImageCache`), `Missione-birra-grande` e `ComponenteScorrevole-*` (16×16);
  - **`img/fondinon2x2/`**, 24 fondi non a blocchi 2×2 (`Cartiglio-*`, `SferaMagica`, `SfondoStoria`, `Separatore`, `Separatore-Armature/Armi/Elmi/LibriMagici/Schinieri/Scudi`, `Foreground*` e `Interno*` di Alchimista/Armaiolo/Incantatore/Locanda/VenditoreDiPergamene, `Luna`, `Mimas`, `LunaHHGTTG`): vanno riguardati a mano; una volta sistemati ognuno si dimezza, si rimette in `fondi/` e si carica con lo zoom. Per gli sfondi degli intermezzi va anche estesa la regola per prefisso in `DisplayableCanvasIntermezzo.risorsa()`, che oggi ingrandisce solo `locazioni/` e `personaggi/`.
  - Finché non sono sistemati, i cinque separatori già convertiti sono il doppio degli altri sei.
  - `personaggi/Locandiere.gif` aveva un pixel isolato che non rientrava in un blocco 2×2 (circa x=66, y=14): controllare che dimezzandolo non si sia visto.
  - Per salvare i GIF con Pillow: `optimize=False` e niente `convert()` prima di salvare, altrimenti si altera la palette; confrontare palette e trasparenza con l'originale in git.


## TODO e FIXME nel codice

Gli altri `TODO`/`FIXME` restano nel codice, accanto al punto a cui si riferiscono (diciotto, sparsi fra `PersonaggioBase`, `LocazioneBase`, `DisplayableCanvas`, `GruppoGiocatore` e altri). Per ritrovarli:

- in IntelliJ, la finestra **TODO** (View → Tool Windows → TODO, oppure ⌘6) li elenca tutti con file e riga sempre aggiornati, e un click porta al punto esatto;
- da terminale: `grep -rn "TODO\|FIXME" src/main/java`.
