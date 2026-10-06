# Assessment di Foresta

Valutazione dello stato del progetto al **2026-10-05**, scritta dopo la revisione dei documenti di riferimento (vedi [`revisione_documentazione.md`](revisione_documentazione.md)). I fatti sono verificati sul codice e citati con `file:riga` (percorsi relativi a `src/main/java/com/threeamigos/foresta/`, salvo dove indicato). Le opinioni sono segnate come **Giudizio**.

## 1. Sintesi

- Il gioco è **completo e giocabile** da inizio a fine: creazione del gruppo, esplorazione, combattimento, negozi, missioni principali e secondarie, intermezzi, vittoria e sconfitta, salvataggi, classifica, trofei.
- La **base di test è solida**: 681 test verdi con `mvn test`, 61% di copertura delle righe, oltre l'80% su motore, missioni e intermezzi, e test di scenario che giocano partite intere senza UI.
- Il **debito tecnico è concentrato** in poche classi molto grandi (`Automa`, `PersonaggioBase`, `Costanti`, `LocazioneBase`, `DisplayableCanvas`) e nel fatto che la UI legge direttamente lo stato del motore.
- Il **contenuto cresce in fretta** grazie alle grammatiche e alle missioni a passi; i punti deboli sono il **bilanciamento** (combattimento contro i boss, economia) e alcune **immagini mancanti**.
- **Giudizio:** il progetto è in buona salute. Le priorità sono rendere il gioco equilibrato prima di aggiungere altro contenuto, e intanto ridurre le classi più grandi a ogni intervento.

## 2. Completezza

| Meccanica | Stato | Note |
| :--- | :--- | :--- |
| Creazione del capo (nome, sesso, classe) | Fatta | Dieci classi giocabili, cinque per sesso (`motore/Automa.java:474-478`) |
| Gruppo, reclutamento, personaggi temporanei | Fatta | 5 permanenti + 3 temporanei (`motore/Costanti.java:177`); i compagni da reclutare sono in città e locande (`RegistroPersonaggi`) |
| Mappa generata, caselle conosciute | Fatta | Dimensione in taratura: oggi 20×20 (`motore/Foresta.java:34-35`) |
| Tempo, distruzione delle città, fine per tempo | Fatta | Giorni 20/25/30/35 e 40 (`motore_di_gioco.md` §6) |
| Combattimento, effetti di stato, duelli, ondate | Fatta | **Da bilanciare** contro i boss (FIXME in [`todo.md`](todo.md)) |
| Corruzione, amicizia, fuga, passare inosservati | Fatta | La fuga è giudicata troppo penalizzante (`motore/GruppoGiocatore.java:610`) |
| Progressione e punti abilità | Fatta | Nell'inventario i punti abilità disponibili sono in verde |
| Artefatti generati, set leggendari, incantatore | Fatta | Mancano diverse immagini per la rivelazione degli artefatti ([`todo.md`](todo.md)); il piano aperto è in [`artefatti_e_incantamenti.md`](artefatti_e_incantamenti.md) |
| Incantesimi e pozioni | Fatta | Dieci formule più il dardo arcano |
| Negozi di città e offerte | Fatta | I negozi esistono solo in città; negozi sparsi nella foresta sono un'idea aperta |
| Economia | **Riequilibrata, misurata fino al livello 5** | Analisi in [`economia.md`](economia.md): più monete iniziali, preziosi e missioni che valgono col livello; col giocatore automatico le entrate sono salite di circa la metà, ma oltre il livello 5 non si sa ancora |
| Missioni principali (Drago e alleati) | Fatta | Scritte a mano su `MissioneBase` |
| Missioni a passi | Fatta, in crescita | 129 dei 190 `TipoMissione` coperti, 18 non coperti né annotati ([`passi_missioni.md`](passi_missioni.md) §6) |
| Intermezzi | Fatta | Con anteprima fuori dal gioco per gli autori |
| Notizie delle locande e notiziario | Fatta | Con le fragilità del §4 |
| Trofei | Fatta | 100% di copertura dei test |
| Salvataggi, classifica | Fatta | 5 slot (`strumenti/GestoreSalvataggiSuFile.java:32`) |
| Sistema di aiuto | Fatto | Cartigli con la descrizione dei comandi sopra la barra delle icone, con un interruttore salvato con la partita (2026-10-05) |
| Modalità verticale | **Rotta** | Barra icone sovrapposta e contenuto tagliato (`motore_grafico.md` §12) |
| Personaggi del gruppo visibili in locazione | Assente | [`todo.md`](todo.md) |

## 3. Architettura

### Punti di forza

- **Macchina a stati reattiva con un solo thread di logica.** Tutta la logica gira sull'EDT, il timer pianifica solo gli impulsi e scarta quelli superati da un cambio di stato. Non ci sono race condition fra motore e UI (`motore_di_gioco.md` §2).
- **Bus eventi robusto.** Consegna sull'EDT, errori degli iscritti isolati e pubblicati come `InternoException`, consegna sostituibile nei test (`motore_di_gioco.md` §4).
- **Missioni a passi.** Un vocabolario piccolo di passi riutilizzabili e grammatiche per il testo: una missione nuova di una famiglia esistente costa una riga di grammatica (`passi_missioni.md` §7).
- **Testabilità.** `Dado` con seme e lanci truccati, `PartitaDiTest` che guida una partita dal bus, temporizzatore manuale. Ne derivano 681 test che girano in pochi secondi senza UI.
- **Generazione procedurale con un motore unico** (`GrammarBean`), documentato e con un proprio assessment.

### Problemi, in ordine di priorità

1. **La UI legge e chiama il dominio direttamente.** 29 file di `ui` importano classi di `motore` (`GruppoGiocatore` in 12) e `DisplayableCanvas` chiama gli `Automa*` dei negozi ricevuti con gli eventi (`ui/DisplayableCanvas.java:782`, `:810`). Il bus separa i comandi, non lo stato. **Giudizio:** è il limite architetturale più importante, perché rende impossibile cambiare il modello senza toccare la UI. La direzione proposta è separare un modello dati in sola lettura per la UI ([`todo.md`](todo.md)). Non è urgente: conviene farlo un pezzo alla volta, partendo dalle schermate che si toccano comunque.
2. **Classi molto grandi.** `PersonaggioBase` 2072 righe, `Costanti` 2054 (circa 1870 costanti), `Automa` 1976 (circa 70 gestori, una trentina di campi di stato), `LocazioneBase` 1522, `DisplayableCanvas` 1296. Ogni funzione nuova aggiunge a `Automa` uno stato e un gestore, e a `DisplayableCanvas` modifiche in più punti. **Giudizio:** meglio estrarre sotto-automi (come già fatto per negozi e incantatore con `AutomaScambiatoreArtefatti`) quando si interviene su una zona, invece di un grande refactoring.
3. **Stato globale statico.** `Foresta` (28 metodi `public static`), `RegistroMissioni` (32), i singleton dei gruppi. `PartitaDiTest` lo azzera prima di ogni scenario, quindi i test funzionano, ma un solo processo può tenere una sola partita. **Giudizio:** accettabile per un gioco single-player; da non estendere.
3. **Due stili di missione.** Le principali e le secondarie più vecchie (18 classi su `MissioneBase`) non hanno intermezzi per passo, domande, contatori e ripieghi; le altre (23 classi a passi più le figlie) sì (`gestione_missioni.md` §12). **Giudizio:** portare le principali sui passi le renderebbe più ricche, ma solo se si vuole cambiarne il contenuto.
4. **Contratto implicito di `impostaAzioni(..., null)`**, che vuol dire sia "prima entrata" sia "avanza di un passo" (`motore_di_gioco.md` §13).

## 4. Rischi tecnici

| Rischio | Fatto | Gravità |
| :--- | :--- | :--- |
| Notizie malformate | `getNotiziaLocanda` fa `substring` sul primo `-` senza controllarlo (`motore/ProduttoreDiTestiCasuale.java:221-222`) e dopo 20 tentativi lancia `IllegalStateException` (`:217`), possibile con un gruppo piccolo | Media: un'eccezione sull'EDT all'uscita da una locanda |
| Copertura della UI | 22% delle righe di `ui` (5570 righe), 54% di `personaggi`, 23% di `offerte` (report JaCoCo in `target/site/jacoco`) | Media: i bug di layout si scoprono solo giocando |
| Salvataggi senza versione | Formato a `|` senza numero di versione (`motore_di_gioco.md` §5) | Bassa: la compatibilità con i salvataggi vecchi non è un obiettivo |
| Layout manuale | `setLayout(null)` e coordinate sommate a mano (`motore_grafico.md` §12) | Bassa per l'estetica pixel-perfect voluta; cara da cambiare |

## 5. Coerenza del contenuto

- **Tipi di missione non coperti:** 18 `TipoMissione` senza missione né annotazione (`RITIRATA_TATTICA`, `SCAMBIO_OSTAGGI`, `INGANNO`, `IMBROGLIONE`, `RAPIMENTO`, `RIFUGIO`, `BLINDATURA`, `ARTIGIANATO`, `COSTRUZIONE` e nove di progressione). Nessun test verifica i commenti `// Coperto da:` (`passi_missioni.md` §7).
- **Codice senza utilizzatori:** il passo `COSTRUISCI` è usato solo dai test; l'interfaccia `FornitoreMissione` non è usata da nessuna classe (`gestione_missioni.md` §11).
- **Grammatiche:** la produzione `NOTIZIE_CITTA` esiste (`src/main/resources/com/threeamigos/foresta/motore/locande.txt:831`) ma nessuna classe la usa. Le vecchie `artefatti.txt` e `artefatti_pp.txt` sono state tolte dal gioco il 2026-10-05.
- **Immagini provvisorie:** Viandante, sacerdote e sacerdotessa usano immagini di altre classi; gli oggetti di missione condividono un sacchetto provvisorio ([`todo.md`](todo.md)).

## 6. Prossimi passi consigliati

1. **Bilanciare il combattimento contro i boss**: è il problema che più incide sull'esperienza di gioco. Il piano è in [`artefatti_e_incantamenti.md`](artefatti_e_incantamenti.md).
2. **Misurare l'economia ai livelli alti** (oltre il 5 il giocatore automatico non arriva), insieme al punto 1, perché prezzi ed entrate crescono col livello del mondo: vedi [`economia.md`](economia.md).
3. **Rendere robuste le notizie**: controllo del formato al caricamento della grammatica (o un test che produca tutte le alternative) e `null` invece dell'eccezione dopo 20 tentativi.
4. **Decidere sulla modalità verticale**: sistemarla o toglierla. Oggi è rotta.
5. **Separare il modello dati dalla UI**: fatto (viste in sola lettura verso la UI, identificativi verso il motore; vedi [`motore_di_gioco.md`](motore_di_gioco.md) §1). Il passo successivo sarebbe dividere il progetto in moduli.
6. **Un test sui commenti `// Coperto da:`** dei `TipoMissione`, prima di aggiungere nuove famiglie di missioni.
7. **Immagini mancanti** (artefatti, Viandante, sacerdoti, oggetti di missione), quando c'è tempo per la grafica.

## 7. Come è stato fatto

- Fonti: i documenti rivisti fra il 2026-10-04 e il 2026-10-05 (`motore_di_gioco.md`, `motore_grafico.md`, `gestione_missioni.md`, `passi_missioni.md`) e verifiche dirette sul codice per ogni dato numerico e ogni giudizio in §3 e §4.
- Test e copertura: `mvn test` del 2026-10-05 (681 test, 0 falliti, 8 saltati); copertura da `target/site/jacoco/jacoco.csv` generato dalla stessa esecuzione.
- Dimensioni: conteggi con `wc -l` e `grep` sui sorgenti. Sono fotografie: cambiano a ogni commit.
