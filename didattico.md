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
| **Personaggi** | otto classi giocabili (ognuna al maschile e al femminile), molti mostri, non combattenti (negozianti), progressione con esperienza e punti abilità |
| **Oggetti** | artefatti generati a caso, incantamenti, set leggendari, ingredienti, pozioni, incantesimi |
| **Economia** | negozi in città, offerte nelle locande |
| **Missioni** | cinque principali e una grande famiglia di incarichi «a passi» |
| **Intermezzi** | scene a pagine con sfondi, animazioni e fumetti |
| **Testi generati** | nomi, fiabe, notizie e testi delle missioni escono da grammatiche |
| **Tutorial** | 31 suggerimenti contestuali, uno alla volta, che si spengono e si rivedono: spiegano il gioco a chi comincia |
| **Interfaccia** | la stessa partita in Swing (desktop) e in libGDX (desktop e Android), sopra lo stesso motore |
| **Persistenza** | quattro slot di salvataggio a mano più il salvataggio automatico in memoria, classifica, trofei che valgono fra una partita e l'altra |

## 2. Le due metà e i moduli

Il progetto è diviso in due parti che **non si conoscono**: il **motore** (le regole) e l'**interfaccia grafica**. Si parlano solo tramite eventi su un bus e tramite viste in sola lettura. Il motore gira anche senza la UI: i test giocano partite intere così. L'interfaccia è a sua volta divisa: la **logica che non disegna** (lo schermo di gioco, il router degli input, i modelli della barra, dei riquadri e delle schermate, il tutorial) sta in un modulo comune, `12-foresta-ui-comune`, e **ogni toolkit grafico** (Swing, libGDX) vi aggiunge solo il disegno e la lettura degli input.

```
   ┌─────────────────────────────────────────┐            ┌─────────────────────────────────┐
   │ 12-foresta-swing    12-foresta-libgdx   │  comandi   │ MOTORE (12-foresta-motore)      │
   │ disegnano la partita con un toolkit     │ ─────────▶ │ Automa: macchina a stati        │
   │ ┌─────────────────────────────────────┐ │            │ locazioni, combattimento,       │
   │ │ 12-foresta-ui-comune                │ │ notifiche  │ missioni, oggetti, mondo        │
   │ │ la logica che non disegna: schermo, │ │ ◀───────── │                                 │
   │ │ input, barra, riquadri, tutorial    │ │ richieste  │ non conosce nessun toolkit      │
   │ └─────────────────────────────────────┘ │            └─────────────────────────────────┘
   └────────────────────┬────────────────────┘
                        │ legge (sola lettura)                             ▲
                        └───────── VistaPartita, VistaScambio ─────────────┘
```

I moduli Maven si impilano in **14 livelli** (dal 00 al 13). Il livello di un modulo è 0 se non dipende da nessuno, altrimenti 1 più il livello più alto fra le sue dipendenze dirette: ogni modulo conosce solo moduli di livello più basso, e quindi è il compilatore a garantire la separazione. Moduli dello stesso livello non si conoscono fra loro, con un'eccezione: `12-foresta-swing` e `12-foresta-libgdx` dipendono da `12-foresta-ui-comune`, che ha il loro stesso livello (per la regola starebbero al 13: è una scelta ancora da prendere, vedi [`assessment.md`](assessment.md) §3). Il numero è anche il prefisso del documento del modulo (`NN-foresta-<modulo>.md`): aprendoli in ordine si legge il gioco dalle fondamenta fino alla cima.

```
 13  ┌────────────────────────────────────────────────────────────────────────┐
     │ app-swing      Main, SnifferBusEventi, i test di motore e UI insieme   │
     │ app-libgdx     MainLibgdx: l'applicazione desktop con libGDX           │
     └───────────────────────┬────────────────────┬───────────────────────────┘
 12  ┌───────────────────────┴─────────┐   ┌──────┴───────────────────────────┐
     │ motore                          │   │ swing, libgdx                    │
     │ macchina a stati, locazioni,    │   │ disegnano la partita con un      │
     │ salvataggi su file              │   │ toolkit: non vedono il motore    │
     │                                 │   ├──────────────────────────────────┤
     │                                 │   │ ui-comune                        │
     │                                 │   │ la logica d'interfaccia che non  │
     │                                 │   │ disegna, uguale per ogni toolkit │
     └───────────────────────┬─────────┘   └──────┬───────────────────────────┘
 11  ┌───────────────────────┴────────────────────┴───────────────────────────┐
     │ motore-api     VistaPartita, ControlloreDiGioco, FaseDiGioco           │
     └────────────────────────────────────┬───────────────────────────────────┘
 10  ┌────────────────────────────────────┴───────────────────────────────────┐
     │ modellodati    ModelloDati: i dati di tutte le parti, per salvarli     │
     │ trofei         i trofei che valgono fra una partita e l'altra          │
     └────────────────────────────────────┬───────────────────────────────────┘
  9  ┌────────────────────────────────────┴───────────────────────────────────┐
     │ missioni       principali, incarichi, leggende, storie                 │
     └────────────────────────────────────┬───────────────────────────────────┘
  8  ┌────────────────────────────────────┴───────────────────────────────────┐
     │ bottino        quel che si trova e si raccoglie                        │
     │ economia       cassa, negozi, contrattazione, offerte                  │
     │ tattiche       come un personaggio sceglie che cosa fare               │
     └────────────────────────────────────┬───────────────────────────────────┘
  7  ┌────────────────────────────────────┴───────────────────────────────────┐
     │ intermezzi     le scene a fumetti                                      │
     │ mondo          chi e che cosa c'è nella Foresta                        │
     │ poteri         incantesimi, canti del Bardo, abilità innate            │
     └────────────────────────────────────┬───────────────────────────────────┘
  6  ┌────────────────────────────────────┴───────────────────────────────────┐
     │ bestiario      le voci sbloccate fra una partita e l'altra             │
     │ combattimento  come ci si scontra                                      │
     │ statistiche    punti, esperienza, tempo e calendario del Drago         │
     └────────────────────────────────────┬───────────────────────────────────┘
  5  ┌────────────────────────────────────┴───────────────────────────────────┐
     │ personaggi     classi, gruppi, progressione                            │
     └────────────────────────────────────┬───────────────────────────────────┘
  4  ┌────────────────────────────────────┴───────────────────────────────────┐
     │ artefatti      oggetti generati, incantamenti, leggendari              │
     └────────────────────────────────────┬───────────────────────────────────┘
  3  ┌────────────────────────────────────┴───────────────────────────────────┐
     │ danni          tipi di danno e interazioni con gli effetti di stato    │
     │ tutorial       i suggerimenti per chi comincia: quali, quando, visti   │
     └────────────────────────────────────┬───────────────────────────────────┘
  2  ┌────────────────────────────────────┴───────────────────────────────────┐
     │ attributi      effettidistato    mappa    notizie    comandi           │
     └────────────────────────────────────┬───────────────────────────────────┘
  1  ┌────────────────────────────────────┴───────────────────────────────────┐
     │ eventi (il bus)        testi (per lingua)        punteggi              │
     └────────────────────────────────────┬───────────────────────────────────┘
  0  ┌────────────────────────────────────┴───────────────────────────────────┐
     │ strumenti (dadi, log, timer)     serializzazione (salvataggi a righe)  │
     └────────────────────────────────────────────────────────────────────────┘
```

I collegamenti nel disegno sono indicativi: il livello dice «dipende al più da quelli sotto», non da quale. Le dipendenze esatte, modulo per modulo, sono nella tabella di [`foresta.md`](foresta.md#moduli) e in ogni documento di modulo. A grandi linee i livelli si leggono a gruppi:

- **Fondamenta (00-01):** dadi, log e timer, la serializzazione dei salvataggi, il bus degli eventi, i testi per lingua, la classifica.
- **Mattoni del dominio (02-04):** attributi, effetti di stato, danni, mappa, notizie, comandi, il tutorial e gli artefatti. Ognuno ha i suoi tipi, i suoi dati e i suoi contratti per la UI, e non sa che esistono personaggi né un mondo.
- **Personaggi (05) e quel che ci si costruisce sopra:** le regole (06 combattimento, statistiche e bestiario; 07 poteri, mondo e intermezzi; 08 bottino, economia e tattiche) e i contenuti (09 le missioni).
- **Dati e contratti (10-11):** `ModelloDati` mette insieme i dati di tutte le parti per salvarli, i trofei, e `motore-api` è il contratto del motore con la UI.
- **Motore e interfaccia (12-13):** il motore, la logica d'interfaccia comune, i due toolkit e le applicazioni che li mettono insieme.

Chi sta più in basso non conosce chi sta più in alto: quando una parte ha bisogno di qualcosa che sta sopra, lo riceve da chi la ospita (per esempio `FonteDiIntermezzi` per gli intermezzi delle missioni, `RegistroMissioni.setFornitoreLocazioneCompleta` per sapere se la locazione è completa, e i `setFornitoreDiDati` che `ModelloDati.installa()` mette al loro posto).

| Livello | Moduli | Documenti |
| :--- | :--- | :--- |
| 00 | strumenti, serializzazione | [`00-foresta-strumenti.md`](00-foresta-strumenti.md), [`00-foresta-serializzazione.md`](00-foresta-serializzazione.md) |
| 01 | eventi, testi, punteggi | [`01-foresta-eventi.md`](01-foresta-eventi.md), [`01-foresta-testi.md`](01-foresta-testi.md), [`01-foresta-punteggi.md`](01-foresta-punteggi.md) |
| 02 | attributi, effettidistato, mappa, notizie, comandi | [`02-foresta-attributi.md`](02-foresta-attributi.md), [`02-foresta-effettidistato.md`](02-foresta-effettidistato.md), [`02-foresta-mappa.md`](02-foresta-mappa.md), [`02-foresta-notizie.md`](02-foresta-notizie.md), [`02-foresta-comandi.md`](02-foresta-comandi.md) |
| 03 | danni, tutorial | [`03-foresta-danni.md`](03-foresta-danni.md), [`03-foresta-tutorial.md`](03-foresta-tutorial.md) |
| 04 | artefatti | [`04-foresta-artefatti.md`](04-foresta-artefatti.md) |
| 05 | personaggi | [`05-foresta-personaggi.md`](05-foresta-personaggi.md) |
| 06 | bestiario, combattimento, statistiche | [`06-foresta-bestiario.md`](06-foresta-bestiario.md), [`06-foresta-combattimento.md`](06-foresta-combattimento.md), [`06-foresta-statistiche.md`](06-foresta-statistiche.md) |
| 07 | intermezzi, mondo, poteri | [`07-foresta-intermezzi.md`](07-foresta-intermezzi.md), [`07-foresta-mondo.md`](07-foresta-mondo.md), [`07-foresta-poteri.md`](07-foresta-poteri.md) |
| 08 | bottino, economia, tattiche | [`08-foresta-bottino.md`](08-foresta-bottino.md), [`08-foresta-economia.md`](08-foresta-economia.md), [`08-foresta-tattiche.md`](08-foresta-tattiche.md) |
| 09 | missioni | [`09-foresta-missioni.md`](09-foresta-missioni.md) |
| 10 | modellodati, trofei | [`10-foresta-modellodati.md`](10-foresta-modellodati.md), [`10-foresta-trofei.md`](10-foresta-trofei.md) |
| 11 | motore-api | [`11-foresta-motore-api.md`](11-foresta-motore-api.md) |
| 12 | motore, ui-comune, swing, libgdx | [`12-foresta-motore.md`](12-foresta-motore.md), [`12-foresta-ui-comune.md`](12-foresta-ui-comune.md), [`12-foresta-swing.md`](12-foresta-swing.md), [`12-foresta-libgdx.md`](12-foresta-libgdx.md) |
| 13 | app-swing, app-libgdx | [`13-foresta-app-swing.md`](13-foresta-app-swing.md), [`13-foresta-app-libgdx.md`](13-foresta-app-libgdx.md) |

Il motore e la UI dipendono entrambi da `11-foresta-motore-api` e mai l'uno dall'altro: è lì che stanno le «lingue comuni» (eventi e viste). Solo le applicazioni (`13-foresta-app-swing`, `13-foresta-app-libgdx`) li mettono insieme; su Android lo fa `AndroidLauncher`, in `foresta-android/`, fuori da Maven. Un test (`DipendenzeDalToolkitGraficoTest`) controlla che nessuno fuori da `12-foresta-swing` usi `java.awt`, Swing, `javax.imageio` o `javax.sound`, e che nessuno fuori dai due moduli libGDX usi `com.badlogic`.

## 3. Il bus eventi: come le parti si parlano

`BusEventi` è un publish/subscribe: chi vuole sapere qualcosa si **iscrive** a un tipo di evento, chi ha qualcosa da dire **pubblica**. La consegna avviene sul thread principale, e un errore in un ascoltatore non blocca gli altri.

Il bus e l'elenco di tutti i tipi di evento (`TipoEvento`) stanno in `01-foresta-eventi`; ogni evento sta invece nel modulo della parte di cui parla (per esempio `NotificaApprovazioneAcquistoArtefatto` in `08-foresta-economia`; i comandi e le richieste del ciclo di vita in `11-foresta-motore-api`). Gli eventi si distinguono per direzione e intento:

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

Ogni stato (`Stato`) ha due gestori: cosa fare entrandoci e come reagire a un comando. Un gestore pubblica le conseguenze sul bus e dice come proseguire: aspettare il giocatore, entrare subito nello stato successivo o inoltrare un comando automatico (per esempio la scelta del personaggio quando ce n'è uno solo). Non esiste una tabella centrale delle transizioni: le regole stanno nei metodi `entraInStato…` e `gestisciComandoInStato…`, registrati in due mappe dell'`Automa` (`gestoriIngresso`, `gestoriComando`). Gli stati sono 54.

Gli stati si raggruppano così:

```
 AVVIO              LOGO_INIZIALE → INTRO → (lingua, nome, sesso, classe) → INIZIO_GIOCO
 CICLO LOCAZIONE    INIZIO_LOCAZIONE → PREPARAZIONE_LOCAZIONE → IN_LOCAZIONE ⇄ IN_COMBATTIMENTO
                                                         │ → FINE_LOCAZIONE → FINE_LOCAZIONE_2
 MOVIMENTO          ATTESA_DIREZIONE → SCELTA_DIREZIONE → SCELTA_PASSI → ... → INIZIO_LOCAZIONE
 SCENE E NEGOZI     INTERMEZZO (e ATTESA_UI_PER_INTERMEZZO), INGRESSO_NEGOZIO, ACCAMPAMENTO
 SCELTE             personaggio, incantesimo, pozione, destinatario di un oggetto, resurrezione,
                    sì/no, risposta a una domanda di missione
 SCHERMATE          INVENTARIO → (TROFEI, BESTIARIO, GRIMORIO, TATTICHE), MAPPA, salvataggio
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

- **`Foresta`** (`02-foresta-mappa`): la griglia 20×20, ogni casella ha un tipo di locazione e un modello dati con le sue proprietà (nome, visitata, conosciuta...). A ogni partita si genera un mondo nuovo: lo costruisce il motore (`CostruttoreDellaForesta`) sulla mappa, che non sa chi lo abita, e chi lo abita sta in `07-foresta-mondo` (`RegistroPersonaggi`, `RegistroArtefatti`). Una città per quadrante, locande, templi, grotte, paludi, rovine, radure; il resto è bosco. I castelli dei boss li rivendicano le missioni.
- **Caselle conosciute**: una casella si scopre vedendola, sentendone parlare (le informazioni nelle locande) o grazie a una missione. La mappa a tutto schermo mostra solo quelle.
- **`LineaTemporale`** (`06-foresta-statistiche`): ora del giorno e contatore dei giorni. Muoversi e riposare fanno passare le ore; il Drago distrugge una città a 20, 25, 30 e 35 giorni; oltre il 40 si perde.
- **`Locazione`** (`12-foresta-motore`, pacchetto `locazioni`): l'interfaccia di un luogo (crea gli incontri, descrive, imposta i comandi). `LocazioneBase` ha il comportamento comune (combattimento, incantesimi, corruzione, amicizia, fuga, passare inosservati, duelli, la sfida a carta, forbici e sasso); le sottoclassi sono `Bosco`, `Grotta`, `Palude`, `Radura`, `Rovine`, `Tempio`, le città e le locande.

## 6. Personaggi, gruppi, progressione

- **Personaggio** (`05-foresta-personaggi`): un contratto ricco (identità, attributi, risorse, effetti di stato, equipaggiamento) realizzato da `PersonaggioBase`; ogni `TipoPersonaggio` ha la sua classe. Ci sono le **classi giocabili** (Guerriero, Ladro, Bardo, Elfo, Mago, Sacerdote, Chierico e Druido, ognuna anche in versione femminile: la Bardo è la Cantastorie), i **mostri**, i **non combattenti** (locandiere, armaiolo, alchimista, venditore di pergamene, incantatore) e personaggi speciali (il Viandante che una missione fa scortare).
- **Attributi**: sette primari (Forza, Destrezza, Costituzione, Intelligenza, Saggezza, Carisma, Fortuna), molti secondari che ne derivano (Precisione, Parata, Velocità, Critico, Coraggio...) e risorse dinamiche (salute, magia, stanchezza, esperienza).
- **Modificatori e effetti di stato**: i modificatori cambiano un attributo (in valore, in percentuale o «porta a»), per sempre o a tempo; i 21 effetti di stato (stordito, bruciato, congelato, berserk, ispirato, rigenerazione...) interagiscono con i tipi di danno (`02-foresta-effettidistato`, `03-foresta-danni`).
- **Gruppi**: `GruppoGiocatore` (fino a 5 permanenti, più i temporanei e gli ospiti scortati) e `GruppoAvversario`. I compagni da reclutare nascono con il mondo e stanno in città e locande (`RegistroPersonaggi`, in `07-foresta-mondo`).
- **Progressione** (`GestoreProgressione`): esperienza con curva quadratica, punti abilità da spendere sugli attributi. Il **livello del mondo** fa crescere mostri, bottino e negozi.
- **Debolezze e resistenze**: ogni classe e categoria di personaggio ha moltiplicatori contro i tipi di danno (vedi [`debolezze_e_resistenze.md`](debolezze_e_resistenze.md)).

## 7. Il combattimento

Il combattimento vive in `LocazioneBase` (`12-foresta-motore`), con i calcoli in `CalcolatoreCombattimento` (`06-foresta-combattimento`). Non c'è un «d20 contro classe armatura»: probabilità e danno derivano dagli attributi.

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

- **Tattiche** (`tattiche`, in `08-foresta-tattiche`): ogni personaggio, e ogni mostro, ha un elenco ordinato di *voci*: un selettore (su chi agire: «gli avversari», con filtri e ordinamento), condizioni facoltative («al round 1», «con probabilità del 60%») e una sequenza di azioni (attacco, incantesimo, pozione...). Il giocatore le modifica in una schermata dedicata, come le *Tactics* di Dragon Age o i *Gambit* di Final Fantasy XII.
- **Situazioni particolari**: panchina e resa, duelli, ondate di avversari, ospiti che si possono colpire, passare inosservati, fuggire, corrompere, stringere amicizia (e la sfida a carta, forbici e sasso).
- **Capacità innate** (`07-foresta-poteri`): dardo arcano (Mago, Elfo), i tre **canti del Bardo** (curativo, ipnotico, di valore), l'alba sacra e la luce divina del Sacerdote, scacciare i non morti e imposizione delle mani del Chierico, radici, sciame e pelle di corteccia del Druido, la rigenerazione di Chierico e Druido.
- **Riposo**: recupero di salute, magia e stanchezza in base alle ore e al luogo (all'aperto, col fuoco, al coperto).

## 8. Oggetti, incantesimi, negozi

- **Oggetti e artefatti**: un *oggetto* è ciò che si trova in una locazione (monete, cofani, artefatti, oggetti di missione); un *artefatto* è un pezzo di equipaggiamento vero (arma, scudo, armatura, ingrediente...) con rarità, slot, peso, modificatori e incantamenti, e un modello dati che si salva (`04-foresta-artefatti`; il bottino che si trova nelle locazioni sta in `08-foresta-bottino`).
- **Generazione**: `GeneratoreArtefatti` produce artefatti e pergamene per il bottino e per i negozi, in parte da tabelle e in parte da grammatiche. Gli **ingredienti magici** vengono da `ingredienti.txt`.
- **Leggendari** (`artefatti.leggendari`): oggetti scritti a mano in `leggendari.txt`, ciascuno con una chiave stabile; i **set leggendari** moltiplicano i bonus quando un personaggio li indossa tutti. Esistono indipendentemente dalle missioni, che li possono mettere in palio.
- **Equipaggiamento**: `RegoleEquipaggiamento` (in `05-foresta-personaggi`) fissa slot, chi può usare cosa e il peso.
- **Incantesimi** (`07-foresta-poteri`; i tipi, `TipoIncantesimo`, stanno in `05-foresta-personaggi`): sedici formule, che il gruppo tiene come scorte di pergamene: dieci a bersaglio (acqua, aria, terra, fuoco, fulmine, gelo, veleno, morte, silenzio, resurrezione) e sei globali che agiscono su tutta la locazione (nebbia, nega magia, grido di battaglia, terremoto, tempesta, terrore), più i poteri innati di alcune classi. L'**incantatore** fonde ingredienti negli artefatti nel suo *banco di lavoro*.
- **Negozi** (città; `08-foresta-economia`): locanda, alchimista, armaiolo, venditore di pergamene, incantatore. Armaiolo, venditore e inventario condividono la stessa logica di **scambio** fra due parti (una attiva, una remota): vale il meccanismo richiesta/verdetto del §3.
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

### Tutorial

I **suggerimenti** (31) li conosce `RegistroSuggerimenti` (`03-foresta-tutorial`): quali sono, in quale situazione scattano, a cosa puntano e quali sono già stati visti. `TutorDelGioco` (in `12-foresta-ui-comune`) ne apre uno quando la UI dice che la situazione c'è (`SituazioniDellaUI`), e il fumetto con il pollice, l'annulla e il bordo che lampeggia intorno a ciò a cui punta lo disegna ogni toolkit (`DisplayableCanvasTutorial` in Swing, `TutorialGdx` in libGDX); il bordo segue le icone una per una (`ContornoDiRettangoli`), anche con il Dock ingrandito. Vedi [`tutorial.md`](tutorial.md) e [`03-foresta-tutorial.md`](03-foresta-tutorial.md).

### Trofei e notizie

I **trofei** (`10-foresta-trofei`) sono obiettivi che valgono da una partita all'altra (contatori di goblin uccisi, pasti, amicizie... o imprese singole): una volta vinti restano vinti, e si salvano a parte. Le **notizie** (`02-foresta-notizie`, prodotte da `RegistroNotizie` nel motore) sono satire sulle malefatte del gruppo, generate quando si esce da una locanda e mostrate nel notiziario della mappa.

## 10. Testi generati

Quasi tutto il testo vario viene da **grammatiche** (`GrammarBean`, in `01-foresta-testi`) scritte in file `.txt`: nomi di locande, templi, rovine e artefatti, fiabe, oroscopi, notizie e testi delle missioni. I produttori di testi (`ProduttoreDi…Casuale`) le caricano e le interrogano; le produzioni *one-shot* garantiscono che certi elementi non si ripetano nella stessa partita. Vedi [`GrammarBean.md`](GrammarBean.md).

## 11. Dati e salvataggi

La separazione fra **dati** e **logica** è una scelta di fondo:

```
   i …MD, uno per ogni parte            le parti (facciate statiche)
  ┌────────────────────┐              ┌──────────────────────────────┐
  │ GruppoGiocatoreMD  │ ◀─────────── │ GruppoGiocatore (personaggi) │
  │ ForestaMD          │ ◀─────────── │ Foresta (mappa)              │
  │ RegistroMissioniMD │ ◀─────────── │ RegistroMissioni (missioni)  │
  │ CassaMD ...        │              │ Cassa (economia), Statistiche│
  └─────────┬──────────┘              └──────────────────────────────┘
            │ contenuti nel singleton ModelloDati (10-foresta-modellodati)
            ▼
     salva / leggi  →  file di testo, una riga per record, campi separati da |
```

Le classi `…MD` sono semplici dati serializzabili, e ognuna sta nel pacchetto `modellodati` del modulo della sua parte; le facciate contengono le regole e operano su di esse, e prendono i loro dati da `ModelloDati` con un fornitore (`setFornitoreDiDati`), senza sapere dove sono custoditi. `ModelloDati` è il contenitore radice (un singleton sostituibile nei test), e sta in un modulo suo, `10-foresta-modellodati`. Un salvataggio è un file `<slot>.TXT` nella cartella dei dati scelta dalla piattaforma (sul desktop `~/.foresta/`), con una testata leggibile per elencare gli slot senza leggerli. Dopo la lettura, `RiletturaPartita.ricostruisci()` (nel motore) ricalcola tutto lo stato derivato (attributi secondari, gruppo, locazione corrente, elenchi delle missioni). Gli slot a mano sono quattro; il quinto posto dell'elenco da cui si carica è il salvataggio automatico, che l'`Automa` tiene in memoria e aggiorna a ogni fine di locazione (non si scrive a mano e sparisce con l'app). Quando l'app va in background l'`Automa` scrive anche un salvataggio automatico su file, che non compare nell'elenco. La classifica è un file a parte, come i trofei. Non c'è compatibilità con i salvataggi vecchi.

## 12. L'interfaccia grafica

La UI è divisa in tre strati, in tre moduli:

```
 12-foresta-ui-comune   la logica che non disegna, uguale per ogni toolkit
   ForestaUI            si iscrive al bus e traduce ogni evento in una chiamata sullo schermo
   └─ SchermoDelGioco   lo stato dell'interfaccia: la schermata corrente, la pila dei riquadri,
        │               le code degli effetti, il tutorial
        ├─ riquadri     locazione, gruppo, combattimento, mappa, missioni, testo, statistiche,
        │               incantesimi e pozioni   (la schermata di gioco, in una pila di finestre)
        ├─ schermate    a tutto schermo: intro, mappa, inventario, negozi, incantatore,
        │               tattiche, bestiario, grimorio, trofei, intermezzi, fine partita
        ├─ barra icone  i comandi possibili in quel momento (Dock che si ingrandisce, o classica)
        └─ sprite       numeri dei danni, fumetti, annunci: effetti passeggeri
 12-foresta-swing       ForestaSwing + DisplayableCanvas: disegnano con Java2D
 12-foresta-libgdx      ForestaLibGDX + SchermoGdx: disegnano con libGDX (desktop e Android)
```

I riquadri, le schermate e la barra **non sono componenti di un toolkit**: sono *finestre* (interfaccia `Finestra`) a cui lo schermo manda gli input (il router è `RouterDegliInput`) e che ciascun toolkit disegna in un rettangolo. Lo stato della partita lo leggono da `VistaPartita` (sola lettura) e verso il motore mandano solo eventi. In Swing il disegno è Java2D, in libGDX un batch OpenGL; il testo passa in tutti e due da un font bitmap fatto in casa (`Doomdark`), i cui dati stanno nel modulo comune. Vedi [`12-foresta-ui-comune.md`](12-foresta-ui-comune.md), [`motore_grafico.md`](motore_grafico.md) e [`12-foresta-libgdx.md`](12-foresta-libgdx.md).

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
| come parte il gioco | `Main` (`13-foresta-app-swing`) o `MainLibgdx` (`13-foresta-app-libgdx`), poi `AvvioDelMotore`, `Automa` e `Stato` (`12-foresta-motore`) |
| come si parlano UI e motore | `eventi/*` (`01-foresta-eventi` e i moduli delle parti), `interfacce/Vista*` e `ControlloreDiGioco` (`11-foresta-motore-api`) |
| il mondo | `mappa/Foresta` (`02-foresta-mappa`), `CostruttoreDellaForesta` e `locazioni/*` (`12-foresta-motore`), `statistiche/LineaTemporale` (`06-foresta-statistiche`), `mondo/*` (`07-foresta-mondo`) |
| il combattimento | `combattimento/CalcolatoreCombattimento` (`06-foresta-combattimento`), `tattiche/*` (`08-foresta-tattiche`), `incantesimi/*`, `canti/*`, `abilita/*` (`07-foresta-poteri`) |
| i personaggi | `personaggi/*`, `gruppi/Gruppo*`, `GestoreProgressione` (`05-foresta-personaggi`) |
| gli oggetti | `artefatti/*` e `artefatti/leggendari/*` (`04-foresta-artefatti`), `oggetti/*` (`08-foresta-bottino`), `economia/*` e `offerte/*` (`08-foresta-economia`) |
| le missioni | `missioni/*`, `RegistroMissioni` (`09-foresta-missioni`) |
| i salvataggi | `ModelloDati` (`10-foresta-modellodati`), i `modellodati/*` di ogni modulo, `salvataggi/*` e `motore/RiletturaPartita` (`12-foresta-motore`) |
| il tutorial | `tutorial/RegistroSuggerimenti` (`03-foresta-tutorial`), `ui/tutorial/*` (`12-foresta-ui-comune`), `DisplayableCanvasTutorial` (Swing), `TutorialGdx` (libGDX) |
| il disegno | `ui/*` in `12-foresta-ui-comune` (schermo, riquadri, schermate, barra, sprite, testo) e in `12-foresta-swing` e `12-foresta-libgdx` (il disegno vero) |

## Osservazioni

- Questa guida descrive lo stato al 2026-11-10: 33 moduli Maven, 3655 test, tutti verdi (37 saltati).

## Da fare / idee aperte

- Aggiungere un diagramma delle classi della gerarchia dei personaggi e uno degli eventi di combattimento, se serve per la didattica.
