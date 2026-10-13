# La Foresta: guida didattica

Una guida alla struttura attuale del gioco, pensata per chi vuole ripassarla: quali sono le parti, che cosa fanno e come si parlano. È una mappa, non un manuale: per i dettagli ogni sezione rimanda al documento che approfondisce (la mappa dei documenti è in [`foresta.md`](foresta.md)). I diagrammi sono in ASCII, così si leggono ovunque.

## 1. Il gioco in una pagina

**La Foresta** è un gioco di ruolo a turni, in italiano, per un solo giocatore. Un gruppo di avventurieri (fino a 5 permanenti) esplora una foresta a caselle (20×20, diversa a ogni partita), entra nelle locazioni, combatte, raccoglie oggetti, compra e vende nelle città e porta a termine missioni. L'obiettivo è **sconfiggere il Drago** prima che il tempo finisca: a 20, 25, 30 e 35 giorni il Drago distrugge una città, oltre il giorno 40 la partita è persa. Se muore il capo, la partita è persa.

Le caratteristiche principali, in una riga ciascuna:

| Caratteristica | In breve |
| :--- | :--- |
| **Mappa e tempo** | griglia generata a ogni partita; ogni mossa fa passare le ore |
| **Locazioni** | boschi, grotte, paludi, radure, rovine, templi, città, locande, castelli: ognuna ha incontri e comandi suoi |
| **Combattimento** | a round, tutto il gruppo agisce a turno, ogni personaggio segue una *tattica* |
| **Personaggi** | sei classi giocabili, molti mostri, non combattenti (negozianti), progressione con esperienza e punti abilità |
| **Oggetti** | artefatti generati a caso, incantamenti, set leggendari, ingredienti, pozioni, incantesimi |
| **Economia** | negozi in città, offerte nelle locande |
| **Missioni** | cinque principali e una grande famiglia di incarichi «a passi» |
| **Intermezzi** | scene a pagine con sfondi, animazioni e fumetti |
| **Testi generati** | nomi, fiabe, notizie e testi delle missioni escono da grammatiche |
| **Persistenza** | cinque slot di salvataggio, classifica, trofei che valgono fra una partita e l'altra |

## 2. Le due metà e i moduli

Il progetto è diviso in due parti che **non si conoscono**: il **motore** (le regole) e l'**interfaccia grafica** (Swing). Si parlano solo tramite eventi su un bus e tramite viste in sola lettura. Il motore gira anche senza la UI: i test giocano partite intere così.

```
            INTERFACCIA (foresta-swing)                      MOTORE (foresta-motore)
   ┌───────────────────────────────────┐            ┌───────────────────────────────────┐
   │ ForestaUI, DisplayableCanvas      │  comandi   │ Automa: macchina a stati          │
   │ riquadri, schermate, barra icone  │ ─────────▶ │ locazioni, combattimento,         │
   │ sprite, font bitmap               │            │ missioni, oggetti, mondo          │
   │                                   │  notifiche │                                   │
   │ disegna lo stato della partita    │ ◀───────── │ non conosce Swing                 │
   └─────────────────┬─────────────────┘  richieste └───────────────────────────────────┘
                     │ legge (sola lettura)                        ▲
                     └────────── VistaPartita, VistaScambio ───────┘
```

I moduli Maven si impilano, e ognuno conosce solo quelli sopra di lui, quindi è il compilatore a garantire la separazione:

```
 foresta-tipi          enum e costanti di base (TipoLocazione, TipoPersonaggio, ...)
      ▲
 foresta-modellodati   le classi ...MD: solo dati, con lettura e scrittura dei salvataggi
      ▲
 foresta-contratti     interfacce (Vista*), bus e classi degli eventi, utilità neutre
      ▲                          ▲
 foresta-motore         foresta-swing
 (tutto il dominio)     (ui, immagini)
      ▲                          ▲
      └────────  foresta-app  ───┘     Main, SnifferBusEventi, test che usano entrambi
```

Il motore e la UI dipendono entrambi da `foresta-contratti` e mai l'uno dall'altro: è lì che stanno le «lingue comuni» (eventi e viste). Solo `foresta-app` li mette insieme. Un test (`DipendenzeDalToolkitGraficoTest`) controlla che nessuno fuori da `foresta-swing` usi `java.awt` o Swing.

## 3. Il bus eventi: come le parti si parlano

`BusEventi` è un publish/subscribe: chi vuole sapere qualcosa si **iscrive** a un tipo di evento, chi ha qualcosa da dire **pubblica**. La consegna avviene sul thread principale, e un errore in un ascoltatore non blocca gli altri.

Gli eventi si distinguono per direzione e intento:

| Famiglia | Direzione | Significato | Esempio |
| :--- | :--- | :--- | :--- |
| `Comando*` | UI → motore | «il giocatore ha fatto questo» | `ComandoDiGioco`, `ComandoInvioTesto` |
| `Notifica*` | motore → UI | «è successo questo» (fatto compiuto) | `NotificaTestoParagrafo`, `NotificaFineGioco` |
| `Richiesta*` | motore → UI | «mostra questa schermata / chiedi questo» | `RichiestaSelezioneDirezione`, `RichiestaAperturaInventarioGruppo` |
| `Interno*` | tecnici | coordinamento fra le parti | `InternoFaseDiGioco`, `InternoInterfacciaUtentePronta` |

Un ciclo tipico, il giocatore che preme «attacca»:

```
 Giocatore        UI                       Bus                    Motore (Automa)
     │  click      │                         │                          │
     ├────────────▶│ ComandoDiGioco(...)     │                          │
     │             ├────────────────────────▶├─────────────────────────▶│ processaComando
     │             │                         │                          │ cambia stato, calcola il round
     │             │                         │◀─────────────────────────┤ pubblica NotificaTestoFrase,
     │             │◀────────────────────────┤                          │ variazioni di salute, ...
     │             │ aggiorna riquadri,      │                          │
     │◀────────────┤ sprite dei danni        │                          │
```

**Richiesta e verdetto.** Le operazioni delicate (comprare, vendere, spostare un artefatto) non spostano niente al primo colpo: la UI manda un comando, il motore **verifica** (fondi, peso, slot, regole di equipaggiamento) e pubblica un'**approvazione** o un **rifiuto con il motivo**; solo dopo l'approvazione l'oggetto si sposta. Così la UI non può mai portare la partita in uno stato incoerente.

## 4. L'Automa: il cuore del motore

`Automa` è una **macchina a stati reattiva**: non ha un ciclo di gioco che gira da sé, avanza solo quando riceve uno di due stimoli: un **comando del giocatore** oppure un **impulso del timer** (`TIMER`, per esempio un round di combattimento al secondo).

Ogni stato (`Stato`) ha due gestori: cosa fare entrandoci e come reagire a un comando. Un gestore pubblica le conseguenze sul bus e dice come proseguire: aspettare il giocatore, entrare subito nello stato successivo o inoltrare un comando automatico (per esempio la scelta del personaggio quando ce n'è uno solo). Non esiste una tabella centrale delle transizioni: le regole stanno nei metodi `entraInStato…` e `gestisciComandoInStato…`.

Gli stati si raggruppano così:

```
 AVVIO              LOGO_INIZIALE → INTRO → (nome, sesso, classe)  → INIZIO_GIOCO
 CICLO LOCAZIONE    INIZIO_LOCAZIONE → PREPARAZIONE → IN_LOCAZIONE ⇄ IN_COMBATTIMENTO
                                                         │ → FINE_LOCAZIONE → (movimento)
 MOVIMENTO          ATTESA_DIREZIONE → SCELTA_DIREZIONE → SCELTA_PASSI → ... → INIZIO_LOCAZIONE
 SCELTE             personaggio, incantesimo, pozione, destinatario di un oggetto, resurrezione
 SCHERMATE          INVENTARIO, MAPPA, TROFEI, salvataggio
 FINE PARTITA       GIOCO_PERSO / GIOCO_VINTO → STATISTICHE → PUNTEGGI → INTRO
```

### Il ciclo di una locazione

È il filo conduttore di una partita. A ogni casella raggiunta:

```
 INIZIO_LOCAZIONE   controllo missioni «prima»; evento della linea temporale; intermezzi di inizio
        │
 PREPARAZIONE       si costruisce la locazione e la si popola (incontri, oggetto); controllo missioni «dentro»
        │
 IN_LOCAZIONE  ◀──▶ IN_COMBATTIMENTO / negozi / mappa / inventario
        │
 FINE_LOCAZIONE     raccolta dell'oggetto; controllo missioni «dopo»; intermezzi di fine
        │
 scelta della direzione e dei passi → il tempo avanza → nuova casella
```

Le **missioni** si agganciano a questo ciclo in tre momenti (prima, dentro, dopo la locazione, più l'accampamento): è il punto in cui possono cambiare gli incontri, mettere un oggetto, far partire un intermezzo o fare una domanda al giocatore.

## 5. Il mondo: mappa, tempo, locazioni

- **`Foresta`**: la griglia 20×20, ogni casella ha un tipo di locazione e un modello dati con le sue proprietà (nome, visitata, conosciuta...). A ogni partita si genera un mondo nuovo: una città per quadrante, locande, templi, grotte, paludi, rovine, radure; il resto è bosco. I castelli dei boss li rivendicano le missioni.
- **Caselle conosciute**: una casella si scopre vedendola, sentendone parlare (le informazioni nelle locande) o grazie a una missione. La mappa a tutto schermo mostra solo quelle.
- **`LineaTemporale`**: ora del giorno e contatore dei giorni. Muoversi e riposare fanno passare le ore; il Drago distrugge una città a 20, 25, 30 e 35 giorni; oltre il 40 si perde.
- **`Locazione`**: l'interfaccia di un luogo (crea gli incontri, descrive, imposta i comandi). `LocazioneBase` ha il comportamento comune (combattimento, incantesimi, corruzione, amicizia, fuga, passare inosservati, duelli, la sfida a carta, forbici e sasso); le sottoclassi sono `Bosco`, `Grotta`, `Palude`, `Radura`, `Rovine`, `Tempio`, le città e le locande.

## 6. Personaggi, gruppi, progressione

- **Personaggio**: un contratto ricco (identità, attributi, risorse, effetti di stato, equipaggiamento) realizzato da `PersonaggioBase`; ogni `TipoPersonaggio` ha la sua classe. Ci sono le **classi giocabili** (Guerriero, Ladro, Bardo, Elfo, Mago, Sacerdote, anche in versione femminile), i **mostri**, i **non combattenti** (locandiere, armaiolo, alchimista, venditore di pergamene, incantatore) e personaggi speciali (il Viandante che una missione fa scortare).
- **Attributi**: sette primari (Forza, Destrezza, Costituzione, Intelligenza, Saggezza, Carisma, Fortuna), molti secondari che ne derivano (Precisione, Parata, Velocità, Critico, Coraggio...) e risorse dinamiche (salute, magia, stanchezza, esperienza).
- **Modificatori e effetti di stato**: i modificatori cambiano un attributo (in valore, in percentuale o «porta a»), per sempre o a tempo; i 18 effetti di stato (stordito, bruciato, congelato, berserk...) interagiscono con i tipi di danno.
- **Gruppi**: `GruppoGiocatore` (fino a 5 permanenti, più i temporanei e gli ospiti scortati) e `GruppoAvversario`. I compagni da reclutare nascono con il mondo e stanno in città e locande.
- **Progressione** (`GestoreProgressione`): esperienza con curva quadratica, punti abilità da spendere sugli attributi. Il **livello del mondo** fa crescere mostri, bottino e negozi.
- **Debolezze e resistenze**: ogni classe e categoria di personaggio ha moltiplicatori contro i tipi di danno (vedi [`debolezze_e_resistenze.md`](debolezze_e_resistenze.md)).

## 7. Il combattimento

Il combattimento vive in `LocazioneBase`, con i calcoli in `CalcolatoreCombattimento`. Non c'è un «d20 contro classe armatura»: probabilità e danno derivano dagli attributi.

```
 round = piano di coppie  (mio personaggio, avversario)  × max(n, m)
        ┌──────────────┬─────────────────────────────────────────────┐
        │ chi agisce   │ decide con la sua TATTICA: la prima voce che │
        │              │ si può eseguire (bersaglio + azione)         │
        ├──────────────┼─────────────────────────────────────────────┤
        │ probabilità  │ 75 + 2×(attacco − difesa), limitata a 5–95   │
        │ di colpire   │ confrontata con un tiro su 100               │
        ├──────────────┼─────────────────────────────────────────────┤
        │ danno        │ arma × livello + parte dell'eroe, per il     │
        │              │ moltiplicatore di classe; poi la difesa      │
        │              │ lo riduce (100 / (100 + difesa)); critico    │
        │              │ raddoppia; effetti di stato e interazioni    │
        └──────────────┴─────────────────────────────────────────────┘
```

- **Tattiche** (`tattiche`): ogni personaggio, e ogni mostro, ha un elenco ordinato di *voci*: un selettore (su chi agire: «gli avversari», con filtri e ordinamento), condizioni facoltative («al round 1», «con probabilità del 60%») e una sequenza di azioni (attacco, incantesimo, pozione...). Il giocatore le modifica in una schermata dedicata, come le *Tactics* di Dragon Age o i *Gambit* di Final Fantasy XII.
- **Situazioni particolari**: panchina e resa, duelli, ondate di avversari, ospiti che si possono colpire, passare inosservati, fuggire, corrompere, stringere amicizia (e la sfida a carta, forbici e sasso).
- **Capacità innate**: dardo arcano (Mago, Elfo), i tre **canti del Bardo** (curativo, ipnotico, di valore), l'alba sacra e la luce divina del Sacerdote.
- **Riposo**: recupero di salute, magia e stanchezza in base alle ore e al luogo (all'aperto, col fuoco, al coperto).

## 8. Oggetti, incantesimi, negozi

- **Oggetti e artefatti**: un *oggetto* è ciò che si trova in una locazione (monete, cofani, artefatti, oggetti di missione); un *artefatto* è un pezzo di equipaggiamento vero (arma, scudo, armatura, ingrediente...) con rarità, slot, peso, modificatori e incantamenti, e un modello dati che si salva.
- **Generazione**: `GeneratoreArtefatti` produce artefatti e pergamene per il bottino e per i negozi, in parte da tabelle e in parte da grammatiche. Gli **ingredienti magici** vengono da `ingredienti.txt`.
- **Leggendari** (`oggetti.leggendari`): oggetti scritti a mano in `leggendari.txt`, ciascuno con una chiave stabile; i **set leggendari** moltiplicano i bonus quando un personaggio li indossa tutti. Esistono indipendentemente dalle missioni, che li possono mettere in palio.
- **Equipaggiamento**: `RegoleEquipaggiamento` fissa slot, chi può usare cosa e il peso.
- **Incantesimi**: dieci formule da comprare come pergamene (acqua, aria, terra, fuoco, fulmine, gelo, veleno, morte, resurrezione...), più quelli innati di alcune classi. L'**incantatore** fonde ingredienti negli artefatti nel suo *banco di lavoro*.
- **Negozi** (città): locanda, alchimista, armaiolo, venditore di pergamene, incantatore. Armaiolo, venditore e inventario condividono la stessa logica di **scambio** fra due parti (una attiva, una remota): vale il meccanismo richiesta/verdetto del §3.
- **Offerte** (locande e incontri amichevoli): pasti, informazioni, mappe, aiuti gratuiti o a pagamento, incantesimi.
- **Economia**: prezzi che crescono con il livello del mondo; il bilancio è in [`economia.md`](economia.md).

## 9. Missioni, intermezzi, trofei

### Missioni

Una `Missione` ha identità, descrizione, stato (attiva, completa, fallita), proprietà e i tre controlli agganciati al ciclo di locazione. `RegistroMissioni` le tiene in un albero (le missioni possono avere figlie) e le controlla a ogni momento del ciclo. Quasi tutte sono `MissioneAPassi`: una sequenza di **passi** (`Passo`) scritta in modo dichiarativo, ognuno con una condizione, un'azione e la domanda eventuale al giocatore.

```
 Missione ─▶ MissioneBase ─▶ MissioneAPassi ─▶ le missioni concrete
                                                  ├─ principali  (sconfiggi il Drago e i suoi alleati)
                                                  ├─ incarichi   (corrieri, scorte, indagini, rituali, tornei...)
                                                  ├─ richieste   (materiali, oggetti smarriti...)
                                                  ├─ leggende    (la leggenda di un oggetto leggendario)
                                                  └─ storie      (campioni, benedizione, lealtà...)
```

Una missione può: sostituire gli avversari di una locazione, metterci un oggetto, far scattare un intermezzo, fare una domanda (il motore va in `ATTESA_RISPOSTA_MISSIONE`), dare ricompense. Quando una missione ha bisogno di un luogo (un covo, un tempio, un castello) lo **rivendica** (*claim*) nel registro, che ricorda per ogni casella quale missione l'ha rivendicata per ultima: una casella è libera solo se nessuna l'ha rivendicata, se è la stessa missione o se la precedente è finita. Vedi [`gestione_missioni.md`](gestione_missioni.md) e [`passi_missioni.md`](passi_missioni.md).

### Intermezzi

Scene a pagine con sfondo, elementi animati (che si muovono per tappe), testo in alto e battute a fumetto. Un `Intermezzo` sa quando scattare (`deveScattare(momento)`); il motore pubblica le pagine come notifiche e la UI le mostra a tutto schermo. Possono partire all'inizio del gioco, entrando in una locazione o in un negozio, accampandosi, o alla fine di un passo di missione. Vedi [`intermezzi.md`](intermezzi.md).

### Trofei e notizie

I **trofei** sono obiettivi che valgono da una partita all'altra (contatori di goblin uccisi, pasti, amicizie... o imprese singole): una volta vinti restano vinti, e si salvano a parte. Le **notizie** sono satire sulle malefatte del gruppo, generate quando si esce da una locanda e mostrate nel notiziario della mappa.

## 10. Testi generati

Quasi tutto il testo vario viene da **grammatiche** (`GrammarBean`) scritte in file `.txt`: nomi di locande, templi, rovine e artefatti, fiabe, oroscopi, notizie e testi delle missioni. `ProduttoreDiTestiCasuale` le carica e le interroga; le produzioni *one-shot* garantiscono che certi elementi non si ripetano nella stessa partita. Vedi [`GrammarBean.md`](GrammarBean.md).

## 11. Dati e salvataggi

La separazione fra **dati** e **logica** è una scelta di fondo:

```
   modellodati  (MD)               motore (facciate statiche)
  ┌───────────────────┐          ┌──────────────────────────────┐
  │ GruppoGiocatoreMD │ ◀─────── │ GruppoGiocatore              │
  │ ForestaMD         │ ◀─────── │ Foresta                      │
  │ RegistroMissioniMD│ ◀─────── │ RegistroMissioni             │
  │ PersonaggioMD ... │          │ Statistiche, LineaTemporale  │
  └─────────┬─────────┘          └──────────────────────────────┘
            │ contenuti nel singleton ModelloDati
            ▼
     salva / leggi  →  file di testo, una riga per record, campi separati da |
```

Le classi `…MD` sono semplici dati serializzabili; le facciate nel motore contengono le regole e operano su di esse. `ModelloDati` è il contenitore radice (un singleton sostituibile nei test). Un salvataggio è un file `<slot>.TXT` in `~/.foresta/`, con una testata leggibile per elencare gli slot senza leggerli. Dopo la lettura, `RiletturaPartita.ricostruisci()` ricalcola tutto lo stato derivato (attributi secondari, gruppo, locazione corrente, elenchi delle missioni). La classifica è un file a parte, come i trofei. Non c'è compatibilità con i salvataggi vecchi.

## 12. L'interfaccia grafica

Tutto il disegno è Java2D, e il testo passa da un font bitmap fatto in casa (`Doomdark`). La UI è composta così:

```
 ForestaUI              si iscrive al bus e traduce ogni evento in una chiamata sul canvas
   └─ DisplayableCanvas il pannello Swing che disegna tutto, 60 volte al secondo se serve
        ├─ riquadri     locazione, gruppo, combattimento, mappa, missioni, testo, statistiche,
        │               incantesimi e pozioni   (la schermata di gioco, in una pila di finestre)
        ├─ schermate    a tutto schermo: intro, mappa, inventario, negozi, incantatore,
        │               tattiche, trofei, intermezzi, fine partita
        ├─ barra icone  i comandi possibili in quel momento (stile Dock o classica)
        └─ sprite       numeri dei danni, fumetti, annunci: effetti passeggeri
```

I riquadri e le schermate **non sono componenti Swing**: sono disegnatori a cui il canvas delega il rendering in un rettangolo (interfaccia `Finestra`). Lo stato della partita lo leggono da `VistaPartita` (sola lettura) e verso il motore mandano solo eventi. Vedi [`motore_grafico.md`](motore_grafico.md).

## 13. Due esempi che attraversano tutto

**Entrare in una locazione.**
1. Il giocatore sceglie una direzione e un numero di passi (comandi → `Automa`).
2. L'automa fa avanzare il tempo (`LineaTemporale`) e passa a `INIZIO_LOCAZIONE`.
3. Il `RegistroMissioni` fa il controllo «prima»: una missione può sostituire gli incontri o far partire un intermezzo.
4. `FabbricaLocazioni` costruisce la locazione e la popola con gli avversari e l'oggetto.
5. L'automa pubblica notifiche di testo e il cambio di fase; la UI ridisegna il riquadro della locazione.
6. In locazione il giocatore sceglie un'azione; se è un combattimento parte il timer e ogni impulso è un round.

**Comprare un artefatto dall'armaiolo.**
1. L'automa pubblica `RichiestaAperturaInventarioCommerciante`; la UI mostra la schermata.
2. Il giocatore doppio-clicca un artefatto: la UI pubblica `ComandoScambioArtefatto`.
3. Il motore verifica fondi, peso e regole di equipaggiamento.
4. Se va bene pubblica `NotificaApprovazioneAcquistoArtefatto` e sposta l'oggetto; altrimenti un rifiuto con il motivo, che la UI mostra al giocatore.

## 14. Dove cercare nel codice

| Voglio capire... | Guardo |
| :--- | :--- |
| come parte il gioco | `Main`, `Automa`, `Stato` |
| come si parlano UI e motore | `eventi/*`, `interfacce/Vista*` |
| il mondo | `motore/Foresta`, `LineaTemporale`, `locazioni/*` |
| il combattimento | `combattimento/CalcolatoreCombattimento`, `tattiche/*` |
| i personaggi | `personaggi/*`, `motore/Gruppo*`, `GestoreProgressione` |
| gli oggetti | `oggetti/*`, `oggetti/leggendari/*`, `offerte/*` |
| le missioni | `missioni/*`, `RegistroMissioni` |
| i salvataggi | `modellodati/*`, `salvataggi/*`, `motore/RiletturaPartita` |
| il disegno | `ui/*` (`riquadri`, `schermate`, `barra`, `sprite`, `testo`) |

## Osservazioni

- Questa guida descrive lo stato al 2026-10-13: sei moduli Maven, 1226 test, tutti verdi (16 saltati).
- Il motore è ancora un modulo unico, con pacchetti che si richiamano a vicenda; il possibile taglio del modulo delle missioni è descritto in `todo.md`.

## Da fare / idee aperte

- Aggiungere un diagramma delle classi della gerarchia dei personaggi e uno degli eventi di combattimento, se serve per la didattica.
- Dopo l'eventuale spezzamento del motore, aggiornare i diagrammi dei moduli del §2.
