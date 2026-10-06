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

- Carta, forbice e sasso.
- Mostrare in locazione anche i personaggi del gruppo.
- Fumetto che attende la chiusura.
- Come ci sono locande sparse per la foresta, anche qualche negozio (armaiolo, alchimista, venditore di pergamene, incantatore).
- Economia: le entrate sono misurate solo fino al livello 5, perché il giocatore automatico di `SimulazionePartiteTest` muore a livello 2-4 e non fa le missioni; farlo vivere più a lungo e fare le missioni, poi eventualmente ritarare i prezzi degli ingredienti: vedi [`economia.md`](economia.md).
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
- `img/oggetti/OggettoMissione.gif` è un sacchetto provvisorio, da ridisegnare: lo usano tutti gli oggetti delle missioni (i materiali delle richieste: erbe, minerali, pesci, trofei; l'oggetto smarrito). In futuro magari un'immagine per ogni oggetto.

## TODO e FIXME nel codice

Gli altri `TODO`/`FIXME` restano nel codice, accanto al punto a cui si riferiscono (una ventina, sparsi fra `PersonaggioBase`, `LocazioneBase`, `DisplayableCanvas`, `GruppoGiocatore` e altri). Per ritrovarli:

- in IntelliJ, la finestra **TODO** (View → Tool Windows → TODO, oppure ⌘6) li elenca tutti con file e riga sempre aggiornati, e un click porta al punto esatto;
- da terminale: `grep -rn "TODO\|FIXME" src/main/java`.
