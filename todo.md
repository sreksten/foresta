# Cose da fare

Elenco unico delle cose da fare che non appartengono a un sottosistema preciso. Quando una voce è fatta, si toglie (la storia resta in git).

Le idee aperte dei singoli sottosistemi stanno nei loro documenti:

- missioni: [`gestione_missioni.md`](gestione_missioni.md) §11 e [`passi_missioni.md`](passi_missioni.md) §7;
- artefatti, pergamene, incantatore, bilanciamento: la lista di [`artefatti_e_incantamenti.md`](artefatti_e_incantamenti.md);
- economia: [`economia.md`](economia.md), sezioni "Che cosa non torna" e "Proposte".

## Bug noti

- **Combattimento.** Personaggi di livello 5 pesantemente armati non riescono nemmeno a scalfire un boss come la Strega o il Lich.
- **Modalità VERTICALE.** `DisplayableCanvasBarraIcone` avanza di 32 px con icone alte 64, e la finestra è larga al massimo 400 px (vedi [`motore_grafico.md`](motore_grafico.md) §12).
- **Schermi alti meno di 804 px.** La barra delle icone copre il fondo del riquadro delle missioni.
- **Scorrimento del riquadro del gruppo.** `DisplayableCanvasRiquadroGruppo` scorre i personaggi di tre righe per volta, diversamente da `DisplayableCanvasRiquadroMissioni`: capire se si può fare come quest'ultimo.

## Architettura (da valutare)

- **Separare il modello dati dal motore.** Oggi la UI importa direttamente classi del motore (`GruppoGiocatore`, `Foresta`, `Notizie`, `RegistroMissioni`...) e riceve dagli eventi gli `Automa*` dei negozi, che poi chiama. L'idea: la UI consulta il modello dati in sola lettura e verso il motore emette solo eventi (`Richiesta*`/`Comando*`). Vedi [`assessment.md`](assessment.md).
- **`ClassiLocazione`.** `GROTTA_RECUPERA_IL_MEDAGLIONE` e `ROVINE_RECUPERA_LE_DERRATE_ALIMENTARI` servono ancora? Dovrebbero essere state superate. Inoltre `ClassiLocazione` dovrebbe chiamarsi `TipoLocazione` e stare in `motore.tipi`.
- **`Notizie`** si mette in ascolto di una notizia (delle locande) invece di riceverne la pubblicazione: approccio inusuale.
- **Nomi dei modelli dati.** Alcuni non finiscono per `MD` (`ModificatoreArtefatto`).

## Gioco e contenuti

- Quando una città viene distrutta, rimane la "storia" della casella? ("Qui sorgeva la città di ...")
- Carta, forbice e sasso.
- Mostrare in locazione anche i personaggi del gruppo.
- Fumetto che attende la chiusura.
- Sistema di aiuto.
- Come ci sono locande sparse per la foresta, anche qualche negozio (armaiolo, alchimista, venditore di pergamene, incantatore).
- Ricontrollare l'economia partendo da [`economia.md`](economia.md): entrate, uscite, modello per livello e proposte (bottino dei nemici, preziosi che valgono col livello, missioni pagate col livello, prezzi degli ingredienti).
- Dimensione ottimale della mappa: `Foresta.DIMENSIONE_X`/`DIMENSIONE_Y`, da tarare con le prove.

## Grafica e immagini

- Segnalare in verde quando uno o più punti abilità sono disponibili, così da farli notare.
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
