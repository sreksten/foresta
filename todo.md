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
- **Sprite dei danni sul bersaglio sbagliato.** In una partita con Alakazam (`OmbraFiamma`) contro la Viverna, i danni da incantesimo del Veleno sembravano generare lo sprite sopra la Viverna invece che sopra Alakazam. Dal log il danno va giustamente ad Alakazam (`Salute variata`), ma `InternoCreazioneSpriteATempo` porta solo la descrizione e non dove disegnarlo. In `DisplayableCanvasRiquadroLocazione` gli sprite (`variaSalute` e simili) usano le coordinate dei soli avversari, e il gruppo non è disegnato in locazione: da capire cosa succede per un membro del gruppo. Probabile che si risolva da solo quando il gruppo si disegna in locazione (vedi sotto). Per indagare: `-Dforesta.log=/Volumes/ForestaLog` (vedi il javadoc di `Logger`).

## Architettura

- **Dividere il progetto in moduli Maven**, così che la separazione fra motore e UI (vedi [`motore_di_gioco.md`](motore_di_gioco.md) §1) la garantisca il compilatore e non solo i test. L'idea: una **base** (`tipi`, `modellodati`, `interfacce`, `eventi` con `BusEventi`, gli strumenti neutri e i dati degli intermezzi), il **motore** (tutto il dominio, che resta un blocco: i suoi pacchetti si richiamano a vicenda), la **UI** e l'**applicazione** (`Main`, che li assembla); le risorse si dividono fra i moduli (immagini nella UI, grammatiche nel motore). Prima vanno tolti gli ultimi legami, che si possono controllare con test di dipendenza come quelli che già ci sono:
  - `interfacce` importa `missioni.Missione` (`FornitoreMissione`: farle usare `VistaMissione`, o spostare l'interfaccia in `missioni`) e `strumenti.Punteggio` e `strumenti.TestataSalvataggio` (`GestorePunteggi`, `GestoreSalvataggi`: spostare i due tipi nella base);
  - `strumenti` mescola le utilità neutre (`Logger`, `Misc`, `ModalitaDiProva`, i temporizzatori, i gestori su file di classifica e trofei, `Punteggio`, `TestataSalvataggio`) e quel che dipende dal dominio (`CostruttoreArtefatto` e la sua implementazione, che usano `Artefatto`; `GestoreSalvataggiSuFile`, che usa `GruppoGiocatore` e `LineaTemporale`): le seconde vanno nel motore;
  - `intermezzi` mescola i dati che disegna la UI (`PaginaIntermezzo`, `BattutaIntermezzo`, `ImmagineIntermezzo`, `ElementoIntermezzo`, `Animazione`, `StatoElemento`, `TipoStiramento`, `BattutaProgrammata`, che dipendono solo da `tipi`) e la logica degli intermezzi (che dipende da motore, missioni e locazioni): i dati vanno nella base;
  - gli undici `Interno*` che importano il dominio (le parti di uno scambio, il banco di lavoro, i personaggi) e `SnifferBusEventi` vanno nel motore: sono le eccezioni di `eventi/DipendenzeEventiTest`, e `BusEventi` non deve dipendere da loro;
  - i sei eventi `InternoCreazioneSprite*` e `InternoNotificaViaFumettoATempo` portano tipi della UI (sono le eccezioni di `ui/DipendenzeVersoLaUITest`): li crea e li consuma solo la UI, e vanno nel suo modulo, che userà `BusEventi` della base.

## Gioco e contenuti

- Mostrare in locazione anche i personaggi del gruppo, tutti rivolti verso i mostri: da provare con il gruppo a destra che guarda a sinistra (oggi i mostri sono disegnati da sinistra e mai specchiati, vedi `DisplayableCanvasRiquadroLocazione`). Richiede il verso di ogni immagine (vedi "Verso dei personaggi" sotto).
- Idea rimandata: oggi il doppio attacco esiste solo con un'arma equipaggiata nella mano secondaria (Ladro/Ladra, Elfo/Elfa: `PersonaggioBase.getArmaSecondaria`). Si era pensato a un attacco naturale secondario (artigli) per chi ha la mano secondaria vuota, e ad Alakazam di darglielo sempre; per ora Alakazam ripiega solo sugli artigli contro chi è immune al fuoco (`ArmaNaturale.controDifensore`).
- Fumetto che attende la chiusura.
- Come ci sono locande sparse per la foresta, anche qualche negozio (armaiolo, alchimista, venditore di pergamene, incantatore).
- Economia: le entrate sono misurate solo fino al livello 5, perché il giocatore automatico di `SimulazionePartiteTest` muore a livello 2-4 e non fa le missioni; farlo vivere più a lungo e fare le missioni, poi eventualmente ritarare i prezzi degli ingredienti: vedi [`economia.md`](economia.md).
- Dimensione ottimale della mappa: `Foresta.DIMENSIONE_X`/`DIMENSIONE_Y`, da tarare con le prove.

## Il Bardo

Oggi il Bardo non ha nessuna capacità propria ed è la classe più debole (vedi `risorse_e_documenti_vari/analisi_logoramento.md`). Idee, sul modello del dardo arcano di Mago ed Elfo (capacità innata, costa `MAGIA`, non consuma pergamene, è fra le scelte del comando incantesimi, occupa il turno e l'avversario risponde su chi la usa). Ognuna richiede una sua icona.

- **Canto di guarigione** (il gioco non ha un curatore): cura il compagno più ferito (o tutto il gruppo, di meno), circa 30 × √livello aumentato da Saggezza e Carisma, per circa 4 di `MAGIA`.
- **Canto ipnotico o di scherno** (il morale del nemico): `SPAVENTATO` o `CONFUSO` sugli avversari, con la Soggezione del Bardo contro il loro Coraggio. Usa effetti di stato che ci sono già.
- **Ballata di incitamento** (il morale del gruppo): un bonus temporaneo al gruppo (Precisione o danno) per qualche round. Serve un effetto di stato positivo nuovo (per esempio `ISPIRATO`), il suo aggancio in `CalcolatoreCombattimento` e una riga in `interazioni_effetti_di_stato.md`.

## Verso dei personaggi e personaggi non combattenti

Le immagini dei personaggi guardano a destra o a sinistra, e la UI oggi non lo sa: solo gli intermezzi hanno qualcosa (`intermezzi/Verso`, `VersoDiDefault`, che copre le 12 classi giocanti più il Viandante e lancia `IllegalArgumentException` per le altre). Piano, in quest'ordine:

1. **Nuovi `TipoPersonaggio` per i non combattenti** Locandiere, Armaiolo, Alchimista, VenditoreDiPergamene, Incantatore, Sacerdote, Sacerdotessa, MoglieDelBardo (BardoLocanda è un'altra immagine del Bardo, non un tipo). Sul modello di `Viandante` (25 righe che estendono `Bardo`): voce in `TipoPersonaggio` e `FabbricaPersonaggi.COSTRUTTORI`, classe con nomi, articoli, pronome e sesso, le voci nei `switch` di `PersonaggioBase` (circa riga 1862) e `LanciatoreDeiDadi` (due), voce in `ClassePersonaggioImmagine`. I test che scorrono tutti i tipi (`FabbricheTest`, `PersonaggioBaseTest`, `LanciatoreDeiDadiTest`, `TestGenerazionePersonaggi`) segnalano ciò che manca.
   - Da decidere: la classe da cui ereditano le statistiche (contano se un ospite può essere bersaglio), per esempio Locandiere → Guerriero, Alchimista → Mago, Sacerdote/Sacerdotessa → Mago o Bardo.
   - Icone `-nobordo` per gli ospiti: ci sono per Alchimista e Armaiolo, mancano per gli altri (non verificato come reagisce la UI senza: `ClassePersonaggioImmagine.getIcona` restituisce `null`).
   - Da controllare: che non entrino in `LaSfidaDeiCampioni.classiAmichevoli()` e che non compaiano come avversari casuali; `StatisticheMD` salva un contatore per ogni tipo (formato che cambia, nessun problema).
   - Poi servono anche per le missioni di scorta ("salva il locandiere", "salva la moglie del bardo", "salva l'alchimista"), con `MissioneAPassi.prendiInScorta`, al posto del Viandante.
2. **Il verso in `ClassePersonaggioImmagine`**, per tutti i personaggi dell'enum (36 oggi, più i nuovi), non solo per le classi giocanti: anche Centauro, Titano, Goblin ecc. possono essere ospiti temporanei. Decisioni aperte: i personaggi quasi frontali (Arpia, Strega, Lich, Fantasma, Spettro, Spirito, OmbraNera; Chimera, ChimeraDrago e Idra hanno la testa girata) hanno un terzo valore `FRONTALE` che non si specchia mai o sempre sinistra/destra; e i versi dei 24 personaggi che non sono classi giocanti vanno controllati a occhio sulle immagini (da una schermata d'insieme se ne leggono chiaramente solo Drago e Gargoyle a sinistra, Viverna a destra).
3. **Migrazione degli intermezzi**: oggi sono le scene a decidere se specchiare (`StatoElemento.specchiato`, `Tappa.specchiata`, `ElementoIntermezzo.specchiato()`, `orientaNelVersoDelMoto(versoDellImmagine)`, con `VersoDiDefault.serveSpecchiare`), cioè il dominio conosce come sono disegnate le immagini. Meglio che le scene indichino il verso voluto (`StatoElemento` porta un `Verso`, o `null` = così com'è) e che `DisplayableCanvasIntermezzo.disegnaElemento()` lo confronti con quello nativo e specchi solo se serve. `VersoDiDefault` sparisce. Circa 14 file (`Tappa`, `StatoElemento`, `ElementoIntermezzo`, `ScenaNegozio`, `ScenaFraCompagni`, gli intermezzi) e le 13 asserzioni di `ElementoIntermezzoTest`. Dopo il punto 1 non restano immagini di personaggi caricate per percorso (`ImmagineIntermezzo.risorsa("personaggi/Locandiere.gif")` diventa `personaggio(tipo)`).
4. Il disegno del gruppo in locazione (vedi "Gioco e contenuti").

## Grafica e immagini

- Immagini degli artefatti che mancano per la rivelazione (`SpriteRivelazioneArtefatto`), da mettere in `img/oggetti`:
  - armi: Mazza, Ascia, Lancia, BastoneMagico;
  - libro magico: LibroMagico;
  - protezioni: Veste;
  - accessori: Talismano, Ninnolo;
  - ingredienti magici (oggi usano l'icona della pergamena): Pergamena, Gemma, Monile, Gingillo, Sigillo.
- `img/oggetti/OggettoMissione.gif` è un sacchetto provvisorio, da ridisegnare: lo usano tutti gli oggetti delle missioni (i materiali delle richieste: erbe, minerali, pesci, trofei; l'oggetto smarrito). In futuro magari un'immagine per ogni oggetto.
- **Immagini a metà risoluzione con zoom a runtime** (`LivelloDiZoom`, argomento `ZOOM=n`, predefinito 2). Sono già dimezzate nei file e ingrandite al caricamento: locazioni, personaggi, oggetti, mappa, alfabeto, i loghi PNG, le icone con bordo (31×32) e 17 fondi. Restano senza zoom:
  - `icone/*-nobordo` e `Sfondo-icona-*`, `ComponenteScorrevole-*`, `Missione-birra`, `Missione-gallo`: già piccoli e non a blocchi 2×2;
  - **`img/fondinon2x2/`**, 26 fondi non a blocchi 2×2 (`Cartiglio-*`, `CorniceInventario`, `CorniceLarga`, `SferaMagica`, `SfondoStoria`, `Separatore`, `Separatore-Armature/Armi/Elmi/LibriMagici/Schinieri/Scudi`, `Foreground*` e `Interno*` di Alchimista/Armaiolo/Incantatore/Locanda/VenditoreDiPergamene, `Luna`, `Mimas`, `LunaHHGTTG`): vanno riguardati a mano; una volta sistemati ognuno si dimezza, si rimette in `fondi/` e si carica con lo zoom. Per gli sfondi degli intermezzi va anche estesa la regola per prefisso in `DisplayableCanvasIntermezzo.risorsa()`, che oggi ingrandisce solo `locazioni/` e `personaggi/`.
  - Finché non sono sistemati, i cinque separatori già convertiti sono il doppio degli altri sei.
  - `personaggi/Locandiere.gif` aveva un pixel isolato che non rientrava in un blocco 2×2 (circa x=66, y=14): controllare che dimezzandolo non si sia visto.
  - Per salvare i GIF con Pillow: `optimize=False` e niente `convert()` prima di salvare, altrimenti si altera la palette; confrontare palette e trasparenza con l'originale in git.


## TODO e FIXME nel codice

Gli altri `TODO`/`FIXME` restano nel codice, accanto al punto a cui si riferiscono (una ventina, sparsi fra `PersonaggioBase`, `LocazioneBase`, `DisplayableCanvas`, `GruppoGiocatore` e altri). Per ritrovarli:

- in IntelliJ, la finestra **TODO** (View → Tool Windows → TODO, oppure ⌘6) li elenca tutti con file e riga sempre aggiornati, e un click porta al punto esatto;
- da terminale: `grep -rn "TODO\|FIXME" src/main/java`.
