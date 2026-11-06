# Il tutorial: i suggerimenti per chi gioca per la prima volta

Stato: **piano** (2026-11-07), niente codice ancora. Chi comincia vede insieme i riquadri, la mappa e tutta la barra delle icone, e non sa da dove cominciare: bisogna spiegargli le cose una alla volta, quando servono, senza un manuale da leggere prima.

## 1. Decisioni prese (dall'utente, 2026-11-07)

1. **Forma**: suggerimenti contestuali, **una volta sola**. Ognuno compare la prima volta che si presenta la situazione (il primo combattimento, la prima volta che si apre la mappa, il primo clic su un riquadro...) e poi non torna. Non c'è un percorso guidato obbligato né un manuale.
2. **Le icone restano come oggi**: nessuna icona si nasconde finché non serve; compaiono quando sono possibili, come ora. Il tutorial si limita a spiegarle.
3. **Si può spegnere e rivedere**: un'opzione per spegnere i suggerimenti e una per rivedere quelli già visti; il primo suggerimento dice come si spegne.

## 2. Che cosa c'è già e si può usare

- La barra delle icone mostra già solo i comandi possibili (`ComandiPossibili`), e l'aiuto (l'icona «?») dà la descrizione di ogni icona (sul tocco con la pressione prolungata).
- Gli eventi sul bus che segnalano le situazioni: `InternoRichiestaAperturaFinestraCombattimento` (si apre il combattimento), `RichiestaAperturaInventarioGruppo`, `RichiestaAperturaTattiche`, `NotificaArtefattoTrovato`, le notifiche di variazione delle scorte, `InternoMostraSchermataGioco`... Molti inneschi non servono nuovo codice del motore: basta ascoltarli. Altri (la prima apertura della mappa, il primo clic su un riquadro) vanno verificati uno per uno.
- Il modello per ricordare qualcosa **da una partita all'altra** esiste: il bestiario (`RegistroBestiario`, `BestiarioMD`, l'interfaccia `GestoreBestiario` implementata su file dal motore) e i trofei. Lo stato dei suggerimenti vissuti si tiene allo stesso modo, nella cartella dell'utente (`~/.foresta`, su Android la cartella privata dell'app), e non nel salvataggio di una partita.
- I testi dei nomi e delle descrizioni delle enum stanno in un file `.properties` accanto all'enum, uno per lingua (`tipo_trofeo_it.properties`, chiavi `COSTANTE.NOME`, `COSTANTE.DESCRIZIONE`): un'enum dei suggerimenti con le sue chiavi `COSTANTE.TITOLO` e `COSTANTE.TESTO` si traduce senza codice.

## 3. Disegno (da confermare)

- **`TipoSuggerimento`**: un'enum con un suggerimento per costante (nome salvato, e testo in `tipo_suggerimento_it/en.properties`).
- **`RegistroSuggerimenti`** (nel modulo adatto, sul modello di `RegistroBestiario`): ascolta gli eventi, decide se un suggerimento va mostrato (non l'hai già visto, i suggerimenti sono accesi), lo pubblica come evento (`NotificaSuggerimento`) e lo segna come visto. Ha l'interruttore acceso/spento e il «rivedi tutto» (azzera i visti). Lo stato su file lo scrive un `GestoreSuggerimenti` del motore, come per il bestiario.
- **Come si mostra**: da decidere (vedi §5). La UI lo traduce con `ForestaUI`, come gli altri eventi, quindi vale per Swing, libGDX e Android.
- **Un suggerimento alla volta**: se ne scattano due insieme, il secondo aspetta (o si perde per quella partita) e non si mostra a metà di una scena.
- **Mai durante un'azione che chiede attenzione**: né in pieno combattimento, né sopra un intermezzo; si aspetta la fine dello scontro o della scena.

## 4. Quali suggerimenti (prima bozza)

| Quando | Che cosa spiega |
| :--- | :--- |
| Prima schermata di gioco | Che cliccando (o toccando) un riquadro lo si porta in primo piano: gruppo, incantesimi e pozioni, missioni, mappa. E come si spegne il tutorial |
| Prima volta che si vede la barra delle icone | Che le icone cambiano con la situazione e che l'icona «?» dà la descrizione di ognuna (sul tocco: tenere premuto) |
| Prima apertura della mappa a tutto schermo | A cosa serve: dove si è, cosa si è visitato, come si trascina; il notiziario sotto; il joystick sul tocco |
| Primo spostamento | Direzioni e numero di passi |
| Primo combattimento | Il riquadro del combattimento (chi agisce, chi è bersaglio, le barre di salute); che i personaggi seguono la tattica |
| Prima volta che si può fuggire o scegliere (singolo attacco, fuga) | Cosa fa ogni comando in combattimento |
| Prima volta in cui compare «Tattiche» | Che si può cambiare come ogni personaggio sceglie cosa fare |
| Primo oggetto trovato | Come si equipaggia dall'inventario e come si passa a un compagno |
| Prima pozione disponibile | Quando serve berla (le piccole solo se qualcuno è ferito; le grandi alzano il massimo) |
| Prima città o bottega | Cosa si può comprare, vendere, incantare |
| Prima notte | L'accampamento, e perché serve qualcuno di guardia |
| Prima missione | Dove si legge, come si completa |
| Prima volta sul tocco | Gesti: tocco, doppio tocco, pressione prolungata, trascinamento |

## 5. Da decidere

- **Come appare un suggerimento**: (a) una riga nel riquadro del testo, con un colore suo (la più semplice, e c'è già `NotificaTestoParagrafo`); (b) un fumetto accanto all'elemento spiegato, come quelli degli intermezzi (più chiaro, ma serve la posizione di ogni elemento e funziona diversamente nelle tre UI); (c) una scheda al centro con «Ho capito» (interrompe, ma non si perde). Una soluzione mista è possibile: la riga nel testo sempre, il fumetto dove la posizione è nota.
- **Se evidenziare l'elemento spiegato** (un bordo che lampeggia sul riquadro o sull'icona) mentre il suggerimento è visibile.
- **Quanto devono essere lunghi**: una frase o due, o un paragrafo.
- **Dove sta il modulo**: un modulo nuovo (livello da calcolare dalle dipendenze: ascolta eventi di motore-api) o dentro uno esistente.
- **Dove si spegne e si rivede**: un'icona della barra, un'opzione all'avvio, o una voce nell'intro.

## Osservazioni

- Lo stesso testo serve a tutte le UI, quindi vanno scritti una volta sola e in tutte le lingue (italiano, inglese, inglese britannico).
- Per chi gioca su Android le istruzioni sono diverse da quelle sul desktop (tocco al posto del clic, pressione prolungata al posto del passaggio del cursore): vanno previste due versioni di alcuni testi, scelte dalla piattaforma.

## Da fare / idee aperte

- Rispondere alle domande del §5.
- Verificare per ognuna delle righe del §4 quale evento già esiste e quale va aggiunto.
- Scrivere i testi (prima in italiano, poi nelle altre lingue).
