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
- **Statistiche dei personaggi non influenzabili da `Dado`.** `LanciatoreDeiDadi` ridistribuisce il pool di mutazione con un `new java.util.Random()` locale (riga ~219), che ignora il seme di `Dado.impostaSeme` e i trucchi di `Dado.trucca`: due personaggi creati con lo stesso seme hanno statistiche diverse, quindi le partite e i test che le usano non sono ripetibili. Va usato `Dado.sorgente()`. Nei test (`InterazioniConGliStatiCaratterizzazioneTest`, `MoltiplicatoreAttesoTest`) oggi si aggira fissando gli attributi primari.

## Architettura

- **Dividere il progetto in moduli Maven**, così che la separazione fra motore e UI (vedi [`motore_di_gioco.md`](motore_di_gioco.md) §1) la garantisca il compilatore e non solo i test. L'idea: una **base** (`tipi`, `modellodati`, `interfacce`, `eventi` con `BusEventi`, gli strumenti neutri e i dati degli intermezzi), il **motore** (tutto il dominio, che resta un blocco: i suoi pacchetti si richiamano a vicenda), la **UI** e l'**applicazione** (`Main`, che li assembla); le risorse si dividono fra i moduli (immagini nella UI, grammatiche nel motore). Prima vanno tolti gli ultimi legami, che si possono controllare con test di dipendenza come quelli che già ci sono:
  - `interfacce` importa `missioni.Missione` (`FornitoreMissione`: farle usare `VistaMissione`, o spostare l'interfaccia in `missioni`) e `strumenti.Punteggio` e `strumenti.TestataSalvataggio` (`GestorePunteggi`, `GestoreSalvataggi`: spostare i due tipi nella base);
  - `strumenti` mescola le utilità neutre (`Logger`, `Misc`, `ModalitaDiProva`, i temporizzatori, i gestori su file di classifica e trofei, `Punteggio`, `TestataSalvataggio`) e quel che dipende dal dominio (`CostruttoreArtefatto` e la sua implementazione, che usano `Artefatto`; `GestoreSalvataggiSuFile`, che usa `GruppoGiocatore` e `LineaTemporale`): le seconde vanno nel motore;
  - `intermezzi` mescola i dati che disegna la UI (`PaginaIntermezzo`, `BattutaIntermezzo`, `ImmagineIntermezzo`, `ElementoIntermezzo`, `Animazione`, `StatoElemento`, `TipoStiramento`, `BattutaProgrammata`, che dipendono solo da `tipi`) e la logica degli intermezzi (che dipende da motore, missioni e locazioni): i dati vanno nella base;
  - gli undici `Interno*` che importano il dominio (le parti di uno scambio, il banco di lavoro, i personaggi) e `SnifferBusEventi` vanno nel motore: sono le eccezioni di `eventi/DipendenzeEventiTest`, e `BusEventi` non deve dipendere da loro;
  - i sei eventi `InternoCreazioneSprite*` e `InternoNotificaViaFumettoATempo` portano tipi della UI (sono le eccezioni di `ui/DipendenzeVersoLaUITest`): li crea e li consuma solo la UI, e vanno nel suo modulo, che userà `BusEventi` della base.

## Gioco e contenuti

- **Gruppo in locazione**: il gruppo del giocatore e gli ospiti vivi si disegnano a destra e i mostri a sinistra, ognuno specchiato verso l'altro (`ClassePersonaggioImmagine.getImmagine(tipo, verso)`, 
  che usa i versi di `VersiDeiPersonaggi`), con gli sprite dei danni, degli effetti e della morte al posto giusto. Gli sprite a tempo di salute e magia (anche dei mostri) non si sovrappongono più: 
  se ce n'è già uno vicino e attivo, il nuovo parte più in basso (`primoPostoLibero`), e le variazioni nulle non ne creano. 
  Da guardare in partita: i mostri molto larghi (Drago, 454 px su 390) vengono coperti dal gruppo.
- Fumetto che attende la chiusura.
- **Resurrezione e non morti (da sistemare).** Il testo di `ClasseIncantesimo.RESURREZIONE` dice che l'incantesimo fa danno ai non morti ("rigetto rigenerativo alle creature d'ombra e ai non-morti"), e la tabella delle azioni di `tattiche_di_combattimento_automatiche.md` lo ripeteva, ma nel codice non è così: `Resurrezione.formula` non guarda se il bersaglio è un non morto (categoria `NON_MORTO`), e l'incantesimo si lancia solo su un compagno del gruppo (`SINGOLO_QUALSIASI`, benefico), quindi quel caso oggi non esiste. O si corregge il testo, o si implementa l'effetto (avrebbe senso solo con un bersaglio avversario, quindi con la scelta del bersaglio delle tattiche).
- **Boss come Ondate (da valutare).** I boss (Lich, Minotauro Gigante, Strega...) si incontrano da soli e il gioco non distingue un avversario "di rango": trasformare quegli incontri in Ondate (`Ondata`) darebbe un modo naturale di riconoscerli, per il selettore "boss" delle tattiche (vedi [`tattiche_di_combattimento_automatiche.md`](tattiche_di_combattimento_automatiche.md) §6.1).
- Come ci sono locande sparse per la foresta, anche qualche negozio (armaiolo, alchimista, venditore di pergamene, incantatore).
- Economia: le entrate sono misurate solo fino al livello 5, perché il giocatore automatico di `SimulazionePartiteTest` muore a livello 2-4 e non fa le missioni; farlo vivere più a lungo e fare le missioni, poi eventualmente ritarare i prezzi degli ingredienti: vedi [`economia.md`](economia.md).
- Dimensione ottimale della mappa: `Foresta.DIMENSIONE_X`/`DIMENSIONE_Y`, da tarare con le prove.

## Personaggi non combattenti

I nomi di questi personaggi sono fatti (liste, registro, testi delle missioni, schermate dei negozi): quello che resta e le idee aperte sono in [`nomi.md`](nomi.md) §8.

- **Immagini e tipi propri per borgomastro e levatrice**, e per i mandanti senza personaggio proprio (mugnaio, mercante, fabbro, notaio, vedove...): oggi sono un `Uomo` o una `Donna` (`AspettoDelMandante`), con l'immagine e il verso del locandiere e della moglie del bardo.
- **La levatrice e le altre comparse nelle scene**: il nome c'è, nessuna scena le chiede.
- **Un punto della UI che elenchi i campioni incontrati** (nome, classe, se sconfitti): i dati sono già nel registro dei personaggi; si può mettere nella schermata delle statistiche o in quella dei trofei.
- Le **missioni di scorta** ("salva il locandiere", "salva l'alchimista"...; "Il viaggio di" la moglie del bardo c'è già: `IlViaggioDellaMoglie`) con `MissioneAPassi.prendiInScorta`, al posto del Viandante: `RegistroPersonaggi.costruisciPersonaggio(residente, livello)` ne fa il `Personaggio` da dare al `Supplier`, come già fa `NonSparateSulPianista` con il `BardoLocanda`. Solo il locandiere non è nel registro: il suo nome sta nella locanda (`Locanda.LOCANDA_NOME_LOCANDIERE`).

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
