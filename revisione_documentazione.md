# Revisione della documentazione e assessment di Foresta

Piano di lavoro per verificare sul codice i documenti di progetto e arrivare a un assessment del gioco.

## Situazione di partenza (2026-10-04)

| File | Righe | Natura attuale |
|---|---|---|
| `foresta.md` | 73 | panoramica: cos'è, meccaniche, architettura, bootstrap, stack |
| `motore_di_gioco.md` | 328 | descrizione del motore, con osservazioni per un refactoring |
| `motore_grafico.md` | 238 | descrizione della UI, con osservazioni per un refactoring |
| `gestione_missioni.md` | 1071 | **diario di progettazione**: proposte numerate seguite da "Come è stato implementato (data)" |
| `passi_missioni.md` | 1143 | catalogo dei passi e mappatura `TipoMissione` → passi, seguito da **~40 voci di diario datate** |

Il codice conta circa 520 file Java (~75.000 righe), in `com.threeamigos.foresta`, divisi nei pacchetti `motore` (con `modellodati` e `tipi`), `eventi` (`notifiche`, `richieste`, `interni`, `comandigiocatore`), `missioni`, `intermezzi`, `personaggi`, `locazioni`, `oggetti`, `offerte`, `incantesimi`, `trofei`, `interfacce`, `ui` (con `sfx`) e `tools`.

Il problema principale non riguarda tanto i documenti brevi quanto i due documenti sulle missioni. Sono cresciuti per accumulo: una sezione propone una cosa, una sezione successiva dice com'è stata fatta, e un'altra ancora la corregge. Per sapere come funziona una missione oggi bisogna leggerli tutti e ricostruire l'ultima versione di ogni decisione.

## Decisione presa (2026-10-04): opzione A

Il diario di `gestione_missioni.md` e `passi_missioni.md` non si conserva: i due file si riscrivono da zero come riferimento sullo stato attuale. La storia resta in git.

Opzioni considerate:

- **A (consigliata):** riscriverli come documenti di riferimento sullo stato attuale. La storia resta in git; eventualmente si aggiunge in fondo un breve paragrafo "Decisioni" con le scelte non ovvie e il loro perché, senza date.
- **B:** tenere il diario e aggiungere in testa una sezione "Stato attuale" verificata.
- **C:** spostare il diario in un file separato (`missioni_storia.md`) e riscrivere gli originali come riferimento.

Le idee non ancora realizzate (per esempio "Registro dei personaggi incontrati") vanno raccolte in un'unica sezione "Da fare / idee aperte", qualunque opzione si scelga.

## Divisione dei ruoli tra i due documenti sulle missioni

Prima della riscrittura, i due documenti si sovrappongono. Si propone:

- **`gestione_missioni.md`: l'infrastruttura.** Copre `Passo`, `MissioneAPassi`, `RegistroMissioni` (claim, disponibilità), `cerca(ClassiLocazione)`, `FornitoreMissione`, l'aggancio a `RegistroIntermezzi` e a `MomentoIntermezzo`, le domande di missione al giocatore, le missioni secondarie, le missioni principali `Sconfiggi*` e la loro gerarchia.
- **`passi_missioni.md`: il catalogo.** Copre il vocabolario dei passi riutilizzabili (uno per voce: cosa fa, parametri, quando si completa), la mappatura `TipoMissione` → sequenza di passi, i tipi coperti solo dalla grammatica e i tipi lasciati fuori di proposito, ciascuno con il suo motivo.

## Ordine di lavoro

Si fa una sessione per file, perché il contesto resti pulito. Ogni sessione si chiude con un commit, così ogni riscrittura si può rivedere con `git diff`.

0. **Preparazione.** Committa lo stato attuale, comprese le modifiche pendenti a `gestione_missioni.md` e il nuovo `FornitoreMissione.java`, in modo da partire da una base pulita. Scegli A, B o C.
1. **`motore_di_gioco.md`.** Viene per primo perché è la base: stato, bus eventi, modello dati. Fissa il vocabolario che usano tutti gli altri documenti.
2. **`motore_grafico.md`.** È indipendente dalle missioni e relativamente breve.
3. **`gestione_missioni.md`.** È l'infrastruttura delle missioni e va verificata prima del catalogo.
4. **`passi_missioni.md`.** È il catalogo. È il file più lungo e più "meccanico" da verificare: per ogni passo e per ogni tipo bisogna controllare che esista e che faccia quel che è scritto.
5. **`foresta.md` e assessment.** Si fa per ultimo, perché la panoramica deve riassumere documenti ormai corretti. L'assessment del gioco va in una sezione apposita, oppure in `assessment.md` se diventa lunga.

Gli altri documenti (`economia.md`, `interazioni_effetti_di_stato.md`, `intermezzi.md`, `artefatti_e_incantamenti.md`, `GrammarBean.md`) restano fuori da questo giro. Si annotano però le incongruenze che emergono strada facendo.

## Cosa deve contenere l'assessment (passo 5)

- **Completezza:** quali meccaniche descritte in `foresta.md` esistono davvero e quali sono abbozzate o assenti. Esempi: mostri, oggetti, amicizie, città, locande, gruppo di personaggi.
- **Architettura:** una sintesi delle "osservazioni per un refactoring" dei due documenti sui motori, ricontrollate sul codice, con priorità.
- **Rischi tecnici:** test (`mvn test` è rotto offline, vedi memoria), encoding misto, accoppiamenti tra motore e UI, punti fragili del bus eventi.
- **Coerenza del contenuto:** missioni, passi e tipi dichiarati ma non raggiungibili nel gioco.
- **Prossimi passi consigliati,** in ordine.

## Modello consigliato

- **Passi 1–4 (verifica e riscrittura): Sonnet 5.5.** Il lavoro consiste soprattutto nel leggere il codice e confrontarlo con affermazioni puntuali (nomi di classi, metodi, flussi). Sonnet è adatto a questo compito ed è più economico e più veloce sulle molte letture che servono. Su `passi_missioni.md`, che richiede soprattutto controlli ripetitivi, il vantaggio è massimo.
- **Passo 5 (panoramica e assessment): Opus 5.5.** Qui serve giudizio: pesare l'architettura, dare priorità ai problemi, scrivere una sintesi che regga. Conviene il modello più forte.
- **Eccezione:** se nel passo 3 la decisione sulla struttura del documento si rivela complicata (per esempio claim e disponibilità delle missioni, che hanno avuto più revisioni), si può fare con Opus solo la parte di riorganizzazione.

Il modello si cambia con `/model` all'inizio di ogni sessione.

## Prompt da usare per i passi 1–4

Sostituisci `<FILE>` con il nome del documento e `<PACCHETTI>` con i pacchetti pertinenti (suggerimenti sotto il prompt).

```
Rivedi il documento <FILE> del progetto Foresta confrontandolo con il codice attuale
in src/main/java/com/threeamigos/foresta. Contesto e decisioni sono in
revisione_documentazione.md: leggilo prima. Non modificare codice Java.

Fase 1 — Inventario. Leggi <FILE> per intero ed estrai ogni affermazione verificabile:
nomi di classi, interfacce, enum, metodi, campi, pacchetti, flussi ("A chiama B",
"l'evento X provoca Y"), regole di gioco, numeri. Numerale.

Fase 2 — Verifica. Per ogni affermazione cerca nel codice (grep prima, poi leggi solo
le parti necessarie) e classificala:
  OK | OBSOLETA (era vera, il codice è cambiato) | ERRATA | NON VERIFICABILE.
Per OBSOLETA ed ERRATA annota com'è il codice oggi, con file:riga.
Non dedurre: se non trovi riscontro, è NON VERIFICABILE, non OK.

Fase 3 — Lacune. Scorri i pacchetti <PACCHETTI> e cerca le classi e i meccanismi
rilevanti che il documento non menziona. Elenca solo ciò che serve a capire il
sistema, non ogni classe.

Fase 4 — Rapporto. Mostrami un rapporto sintetico: conteggi per categoria, elenco
di OBSOLETE/ERRATE con correzione, lacune, e la struttura che proponi per il nuovo
documento (indice delle sezioni). Fermati e aspetta la mia approvazione.

Fase 5 — Riscrittura (solo dopo l'approvazione). Riscrivi <FILE> come documento di
riferimento sullo stato attuale, in italiano, secondo l'opzione scelta in
revisione_documentazione.md per il diario. Nomi di codice in `backtick`. Le idee
non realizzate vanno in una sezione finale "Da fare / idee aperte". Le osservazioni
architetturali (refactoring, fragilità) vanno in una sezione finale "Osservazioni",
utile per l'assessment. Infine aggiungi in revisione_documentazione.md, sotto
"Avanzamento", una riga con data, file e principali correzioni.
```

**Pacchetti suggeriti per la fase 3:**
- `motore_di_gioco.md`: `motore`, `motore/modellodati`, `motore/tipi`, `eventi/*`, `personaggi`, `locazioni`, `oggetti`, `offerte`, `incantesimi`, `trofei`.
- `motore_grafico.md`: `ui`, `ui/sfx`, `eventi/interni`.
- `gestione_missioni.md`: `missioni`, `intermezzi`, `interfacce` (per `FornitoreMissione`).
- `passi_missioni.md`: `missioni`, insieme alle grammatiche usate dalle missioni (cercare dove sono le risorse di `GrammarBean`).

## Prompt per il passo 5

```
Con Opus. Leggi revisione_documentazione.md, poi i quattro documenti appena rivisti
(motore_di_gioco.md, motore_grafico.md, gestione_missioni.md, passi_missioni.md) e
foresta.md. Verifica foresta.md sul codice come nei passi precedenti e riscrivilo
come panoramica che rimanda ai quattro documenti. Poi scrivi l'assessment del gioco
secondo la sezione "Cosa deve contenere l'assessment": verifica sul codice ogni
giudizio importante, cita file:riga, distingui fatti da opinioni. Mostrami
l'indice prima di scriverlo per intero.
```

## Avanzamento

_(una riga per sessione completata)_

- 2026-10-04, `motore_di_gioco.md`: riscritto da zero come descrizione dello stato attuale (13 sezioni). Correzioni principali: la pipeline degli artefatti non è più un esempio didattico (`GeneratoreArtefatti` e `GrammaticaArtefatti` generano loot e magazzini); sette grammatiche caricate (non tre); 84 file in `missioni` (non 24); molti stati nuovi (accampamento, negozio, intermezzi, trofei, domande di missione); l'esito dell'automa si chiama `Esito` (il vecchio nome nel documento era un refuso); `BusEventi` isola gli errori degli iscritti; nuovi concetti (panchina, duelli, ondate, ospiti, trofei, incantatore). Aggiunte sezioni su artefatti e negozi, incantesimi, trofei, intermezzi. Eliminati numeri di riga e cronaca delle correzioni.
- 2026-10-04, `motore_grafico.md`: riscritto da zero (12 sezioni). Correzioni principali: il ciclo di rendering gira a **60 FPS** con cadenza fissa (il vecchio testo diceva 30 FPS a passo fisso) e le animazioni usano il **tempo reale** (sprite, notiziario, barra Dock, intro); nuova **barra Dock** attiva per default (argomento `BARRACLASSICA` per la barra fissa); il `Prompt` è un `JTextField` Swing che blocca `|`; `DoomdarkFont` ha colori in più e `UnsupportedCharacterException`; la sfera magica compare solo nella mappa con notizie; doppio click con rinvio di 175 ms; nuove schermate (commerciante, incantatore, trofei, intermezzo); `SpriteRivelazioneArtefatto`; `BufferedImageBuilder` distingue risorse fatali e caricabili al volo. Eliminati numeri di riga e riferimenti a commit. Segnalato: `System.exit(0)` per risorse mancanti nasconde l'errore, `SferaMagica.png` inutilizzata.
- 2026-10-04, `gestione_missioni.md`: riscritto da zero (12 sezioni, da 1071 a circa 200 righe) come descrizione dell'infrastruttura: modello e albero delle missioni, registro, momenti di controllo, `Passo` e `MissioneAPassi`, intermezzi di passo, claim delle locazioni, missione principale, `IncaricoInCitta`, come scrivere una missione. Eliminata la cronaca per punti e le sezioni superate (leggende di Nyena e Malgaard). Segnalato: `FornitoreMissione` esiste ma nessun codice la usa; il registro dei personaggi incontrati è ancora solo un'idea; due stili di missione (a mano e a passi).
- 2026-10-04, `passi_missioni.md`: riscritto da zero (7 sezioni, da 1143 a circa 240 righe). Il catalogo dei passi è ora quello vero (metodi di `MissioneAPassi` e del builder `Passo`, con `OggettiDaRaccogliere`, `IncontroDiMissione`, `Ricompensa`, `Costruzione`); le missioni concrete sono raggruppate per famiglia con struttura dei passi, ricompense e grammatiche; la mappatura dei 190 `TipoMissione` è generata dai commenti dell'enum (129 coperti, 28 non fattibili, 9 non adatti al tono, 6 da non sviluppare, 18 non coperti). Eliminate le tabelle di sequenze ipotetiche per tipo, il diario datato e le proposte di correzione dei supertipi. Segnalato: il passo `COSTRUISCI` non è usato da nessuna missione, solo dai test; nessun test verifica i commenti `// Coperto da:`.
- 2026-10-05, `foresta.md` e assessment: `foresta.md` riscritto come panoramica con la mappa di tutti i documenti. Correzioni principali: mappa di dimensione variabile (oggi 20×20, non 50×50); 60 FPS (non 30); il motore non ha un thread proprio, tutta la logica gira sull'EDT; la UI legge direttamente lo stato del dominio (il bus separa i comandi, non lo stato); avvio completo in `Main`; Gson tolto dal `pom.xml` e `artefatti.txt`/`artefatti_pp.txt` spostati in `risorse_e_documenti_vari/`; `mvn test` funziona (681 test verdi). Nuovi file: `assessment.md` (completezza, architettura, rischi, coerenza, prossimi passi), `todo.md` (i TODO/FIXME che stavano in testa ad `Automa`) e `CLAUDE.md` (mappa argomento → classi per Claude Code). Revisione completata.
