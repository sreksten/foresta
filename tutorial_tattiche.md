# Il tutorial delle tattiche

Stato: **le quattro lezioni sono scritte** (2026-11-08), in un solo suggerimento di dodici pagine, `SCHERMATA_DELLE_TATTICHE`, che si apre alla prima apertura della schermata (decisione dell'utente: nessuno entra quattro volte in quella schermata); la ricetta guidata (§3.4) resta da fare, e le lezioni vanno viste in Swing e libGDX. Prima era un piano. Il documento dice che cosa c'è già, che cosa nel documento delle tattiche non torna col codice, come si vuole spiegare le tattiche a chi gioca per la prima volta e che cosa serve per farlo. Il disegno generale dei suggerimenti è in [`tutorial.md`](tutorial.md); le tattiche sono descritte in [`tattiche_di_combattimento_automatiche.md`](tattiche_di_combattimento_automatiche.md) (il documento principale, la schermata del giocatore è in §11.15) e in [`tattiche_internals.md`](tattiche_internals.md) (il codice); il modulo è in [`08-foresta-tattiche.md`](08-foresta-tattiche.md).

## 1. Da dove si parte

- All'origine c'era **un solo suggerimento** sulle tattiche, `TATTICHE` (riga 9 di `tutorial.md`, §4): scatta dopo due scontri visti, punta l'icona dell'inventario e dice che «da lì puoi cambiarla per ognuno». Non entrava nella schermata delle tattiche. Oggi ce n'è anche un secondo, `SCHERMATA_DELLE_TATTICHE` (dodici pagine, le quattro lezioni: `Situazione.TATTICHE_APERTE`), che si apre alla prima apertura della schermata (`SchermoDelGioco.schermataDelSuggerimento` conosce anche `StatoDelloSchermo.TATTICHE`).
- La schermata delle tattiche si apre dall'inventario con l'icona `MOSTRA_TATTICHE` (stato `StatoDelloSchermo.TATTICHE`). La logica è in `EditorDelleTattiche` e `MenuADiscesa` (`12-foresta-ui-comune`), i gemelli sono `DisplayableCanvasTattiche` (Swing) e `SchermataTatticheGdx` (libGDX), che disegnano soltanto.
- La schermata ha **a sinistra** la lista delle voci (ognuna con il suo numero, che la accende o la spegne, e il nome, che la ripiega), **al centro** il personaggio con le frecce, **Sì**, **No** e **Predefinita**, **a destra** il riquadro che spiega l'elemento sotto il mouse e come si legge la voce. Il riquadro destro è la parte più utile per chi impara: il tutorial deve insegnare a usarlo.

## 2. Allineamento del documento con il codice

Controllato il 2026-11-08 contro `08-foresta-tattiche`, `06-foresta-combattimento` (`TipoAzione`) e `12-foresta-ui-comune`.

**Allineato:**

- le 7 rose (`TipoRosa`), i 18 filtri (`TipoFiltro`), i 12 ordinamenti (`TipoOrdinamento`), i 7 predicati (`TipoPredicato`) e le 6 portate (`PortataAzione`) coincidono con le tabelle del documento;
- le esclusioni della schermata (`BozzaTattica.predicatiAmmessi`: niente astuzia né intelligenza; `filtriAmmessi`: su «Se stesso» niente classe, categoria, livello rispetto al mio e debole all'azione; `ordinamentiAmmessi`: «chi mi ha attaccato per ultimo» e «l'ultimo scelto» solo fra gli avversari) sono quelle descritte in §11.15;
- i testi del riquadro destro (`testo_tattica_it.properties`, chiavi `spiega.…`) coprono tutti i valori di rose, filtri, ordinamenti, predicati e **tutte e 40** le azioni.

**Corretto il 2026-11-08 (punti 1-4), da fare solo la verifica delle fasce dei mostri:**

1. **Le azioni (§11.9).** *Corretto: la tabella ha ora le 40 azioni e i requisiti delle nuove (incantesimi globali e abilità innate).* Il documento dice «Sono ventiquattro» e ne elenca meno; `TipoAzione` ne ha **40**. Non compare in nessuno dei tre documenti nessuna di queste 15: `NEBBIA`, `NEGA_MAGIA`, `GRIDO_DI_BATTAGLIA`, `TERREMOTO`, `TEMPESTA`, `TERRORE`, `ATTACCO_ALLE_SPALLE`, `MARTELLO_SACRO`, `SCACCIARE_I_NON_MORTI`, `IMPOSIZIONE_DELLE_MANI`, `RADICI`, `SCIAME`, `PELLE_DI_CORTECCIA`, `RIGENERAZIONE`. `LUCE_DIVINA` è citata solo in §11.3, non nella tabella. Per ognuna vanno aggiunti nella tabella il gruppo, la portata, le rose accettate, i requisiti e chi la sa fare (`RegistroTattiche.azioniPossibili`). Le azioni di chierico e druido (`SCACCIARE_I_NON_MORTI`, `IMPOSIZIONE_DELLE_MANI`, `RADICI`, `SCIAME`, `PELLE_DI_CORTECCIA`, `RIGENERAZIONE`) sono le abilità innate di `personaggi_giocabili.md`.
2. **Dove sta la schermata (§11.15).** *Corretto.* Il paragrafo attribuisce la logica a `DisplayableCanvasTattiche` e `CasellaADiscesa` in `12-foresta-swing`. Oggi quelle classi disegnano; la logica è in `EditorDelleTattiche` e `MenuADiscesa` (`12-foresta-ui-comune`) e libGDX ha `SchermataTatticheGdx`. `tattiche_internals.md` lo dice già, il documento principale no.
3. **La portata di Terremoto, Tempesta e Terrore.** *Corretta il 2026-11-08.* Nel codice (`PortataAzione.di`) erano `GRUPPO_ALLEATI`, ma colpiscono gli avversari: nella schermata una voce con queste azioni offriva la rosa «Alleati» invece di «Avversari». Ora sono `GRUPPO_AVVERSARI`, come Nebbia e Nega magia.
4. **Le tattiche predefinite dei giocabili (§11.16).** *Controllate contro `TatticheBase` il 2026-11-08 e corrette.* Mancavano nella tabella il Chierico e il Druido; per il Ladro l'Attacco alle spalle sul più forte; per il Sacerdote la Luce divina sui non morti. **Non controllate** le tattiche dei mostri a fasce di astuzia (stessa sezione).

## 3. Come spiegare le tattiche

### 3.1 L'idea in una frase

Una tattica è una lista di regole, lette dall'alto: **la prima che si può fare vince**. Se nessuna si può fare, il personaggio attacca.

### 3.2 Il vocabolario per chi gioca

Ogni voce risponde a cinque domande, con le parole che la schermata già usa (`su`, `con`, `se`, `fai`):

| Domanda | Termine tecnico | Esempio |
| :--- | :--- | :--- |
| Su **chi**? | rosa | «Alleati», «Avversari» |
| **Quali** fra loro? | filtri | «con salute in percentuale minore di 40» |
| **Quale** di questi? | ordinamento | «con meno salute» |
| **Quando**? | condizioni | «se round maggiore di 2», «nel 30% dei casi» |
| **Che cosa** fa? | azioni | «Pozione di salute, poi attacco» |

L'immagine per spiegarlo è un **imbuto**: si parte da tutti quelli che la rosa propone, i filtri ne tolgono, l'ordinamento ne sceglie uno; se non ne resta nessuno, la voce salta e si passa alla successiva. Le azioni di una stessa voce sono in sequenza: si fa la prima che si può («pozione grande, altrimenti normale, altrimenti attacco»). Costi e scorte (pergamene, magia, pozioni) li controlla il gioco da solo: il giocatore non scrive filtri per quello.

Parole da evitare nei testi del giocatore: «rosa», «predicato», «portata», «ordinamento» (si dice «quale di questi»), «requisiti».

### 3.3 Le quattro lezioni

Una per apertura della schermata, per non sommergere chi non conosce il gioco; ognuna di 2-3 pagine; si danno nell'ordine, ognuna una volta sola.

1. **Leggere una tattica.** (alla prima apertura) Dall'alto in basso, la prima voce che si può fare vince; l'ordine è la priorità; il numero accende e spegne la voce; il nome la ripiega. Si usa la tattica predefinita del ruolo come esempio. Si dice di passare sopra le righe (sul tocco: toccarle) e leggere a destra.
2. **Scegliere il bersaglio.** Su chi, quali, quale: l'imbuto. Perché alcuni menu sono limitati: contro un solo candidato («Se stesso») non serve scegliere, e un'azione accetta solo certe rose (un attacco solo gli avversari).
3. **Quando e che cosa.** Le condizioni (round, probabilità, quanti) e le azioni in sequenza. Una voce senza condizioni vale «sempre, se si può fare».
4. **Provare e confermare.** Sì, No e Predefinita; le modifiche si perdono se si cambia personaggio, e non si fanno in combattimento. Consiglio: le regole specifiche in alto, quelle generiche in basso.

### 3.4 La ricetta guidata (seconda fase)

«Fai curare chi sta male»: aggiungere una voce, scegliere «Alleati», filtrare «salute in percentuale minore di 40», ordinare «con meno salute», scegliere l'azione «Pozione di salute». Il tutorial controlla la bozza (`BozzaTattica`) a ogni passo e va avanti solo quando il giocatore ha fatto quello che chiede. Richiede suggerimenti **interattivi**: oggi il fumetto è una pagina con il pollice, non aspetta un'azione.

## 4. Che cosa serve nel codice

**Stato (2026-11-08):** fatti la schermata per il suggerimento (`TATTICHE_APERTE`), i nuovi bersagli con le aree dell'editor (`LISTA_DELLE_TATTICHE`, `SPIEGAZIONE_DELLE_TATTICHE`, `PULSANTI_DELLE_TATTICHE`) e le lezioni, in **un solo suggerimento** di dodici pagine (la deroga al limite di tre pagine, come `LUOGHI`). Restano la ricetta guidata e provare le lezioni nei due toolkit. Quel che segue è il piano di partenza.

- **Una schermata per suggerimento.** `schermataDelSuggerimento` accetta `StatoDelloSchermo.TATTICHE`, e `SituazioniDellaUI` riceve `Situazione.TATTICHE_APERTE` (e una per lezione, o un contatore di aperture).
- **Nuovi bersagli** (`BersaglioDelSuggerimento`): la lista delle voci, la prima voce, il numero della voce, il riquadro della spiegazione, i pulsanti Sì/No/Predefinita, la riga «aggiungi». Le aree le fornisce `EditorDelleTattiche` (oggi non le espone); `SchermoDelGioco.getAreaDelBersaglio` le chiede a lui. Se una riga non c'è (voce ripiegata, lista scorsa) si ripiega sulla colonna intera, come fa `areaDelleIcone`.
- **Tre suggerimenti separati** per le lezioni 2-4 (invece di una sola lista di pagine) o una deroga al limite di tre pagine come per `LUOGHI`: propongo i suggerimenti separati.
- **Variante per il tocco** (`.TOCCO`) dei testi: sul tocco non si «passa sopra» le righe, quindi il riquadro destro si legge toccando.
- **Il fumetto non deve coprire** la parte a cui punta: per il riquadro destro e i pulsanti centrali `DisposizioneDelFumetto.posizione` mette il fumetto dalla parte con più posto, e va provato nei due toolkit.
- **Tutorial e modifica insieme.** Finché il fumetto è aperto la UI è «occupata» e i tocchi che non cadono sui suoi pulsanti passano allo schermo sotto. Va controllato che per le lezioni 2-4 il giocatore possa aprire un menu e leggere il riquadro a fumetto aperto.

## 5. Ordine di lavoro proposto

1. ~~Sistemare il documento: le 15 azioni in §11.9 e la nota sulla schermata in §11.15 (sezione 2).~~ Fatto il 2026-11-08.
2. Estendere il meccanismo: situazione e schermata per le tattiche, bersagli con le aree dell'editor.
3. Scrivere la lezione 1 (italiano e inglese, con la variante per il tocco), provarla in Swing e libGDX.
4. Le lezioni 2-4, una alla volta.
5. La ricetta guidata, se serve (richiede i suggerimenti interattivi).
6. Aggiornare `tutorial.md` (nuove righe del §4 e dei documenti collegati), `03-foresta-tutorial.md` (il conteggio dei suggerimenti) e questo documento.

Ogni passo compila, si prova e si rivede prima del commit.

## 5 bis. Quel che è stato fatto (2026-11-08)

- `Situazione.TATTICHE_APERTE` e `TipoSuggerimento.SCHERMATA_DELLE_TATTICHE` (dodici pagine, deroga alla regola delle tre come per `LUOGHI`: `PAGINE_MASSIME` sale a 12; le pagine 3 e 4 spiegano le icone per spostare e togliere una voce, il tasto «Nuova voce» e i colori), che `SchermoDelGioco` dà nello stato `TATTICHE`.
- Tre bersagli nuovi, `LISTA_DELLE_TATTICHE`, `SPIEGAZIONE_DELLE_TATTICHE` e `PULSANTI_DELLE_TATTICHE`; le aree le dà `EditorDelleTattiche` (`getAreaDellaLista`, `getAreaDellaSpiegazione`, `getAreaDeiPulsanti`): la lista e la spiegazione sono le due colonne della cornice, i pulsanti l'area che contiene Sì, No e Predefinita. Non c'è ancora un bersaglio per «la prima voce» o per «il numero della voce».
- I testi, in italiano e in inglese, con le parole della schermata («voce», «su», «con», «se», «fai»; niente «rosa»).
- La spiegazione a destra di una voce (`TestoTattica.spiegazione`) è riscritta come un discorso («Guarda tra i nemici. Tiene solo chi rispetta tutti questi requisiti: … Se ne restano più di uno, sceglie quello con meno salute. Poi prova a fare: …») invece del vecchio «su: … fa: …», che ripeteva la riga a sinistra; fuori dalla voce, le spiegazioni degli elementi non parlano più di «rosa».

## 6. Osservazioni

- La difficoltà non è il codice dei suggerimenti, ma il **lessico**: la schermata usa «su», «con», «se», «fai» e le spiegazioni del riquadro destro; i testi del tutorial devono dire le stesse parole, altrimenti il giocatore deve imparare due lingue.
- Le tattiche dei mostri e le fasce di astuzia (§4, §11.16) non vanno spiegate al giocatore: la schermata non le mostra e non le propone (`predicatiAmmessi`).
- Il tutorial delle tattiche non salva su file, come tutto il resto finché si collauda (decisione 10 di `tutorial.md`): le lezioni si ricordano solo in memoria e si rivedono a ogni avvio.

## 7. Da fare / idee aperte

- ~~Decidere se le quattro lezioni si danno una per apertura o tutte alla prima.~~ Deciso (2026-11-08): tutte alla prima apertura, in un solo suggerimento.
- Decidere se la ricetta guidata entra nella prima versione.
- Un riquadro «glossario» sempre a portata di mano (una pagina che elenca rosa, filtri, ordinamenti, condizioni, azioni) oltre al tutorial: forse inutile, il riquadro destro già fa questo lavoro.
- Verificare contro `TatticheBase.deiMostri` le fasce di astuzia dei mostri di §11.16 (sezione 2, punto 4).
- Un trofeo o un cenno quando il giocatore salva la prima tattica personalizzata?
