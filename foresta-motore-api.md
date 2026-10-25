# foresta-motore-api

**Il contratto del motore con la UI.** Contiene tutto ciò che l'interfaccia deve poter chiedere o ricevere *senza conoscere le classi del dominio*: la vista d'insieme della partita (`VistaPartita`), il motore visto da chi lo avvia (`ControlloreDiGioco`), i servizi della piattaforma (salvataggi, classifica, strumenti di sviluppo), gli eventi del ciclo di vita e dei dialoghi, la fase di gioco (`FaseDiGioco`) e il comando associato a ogni potere (`ComandiAssociati`).

È il punto in cui il progetto si divide in due mezzi che **non si conoscono**: il motore (le regole) e l'interfaccia grafica. Entrambi dipendono da questo modulo e mai l'uno dall'altro; solo `foresta-app` li mette insieme. Quel che era un modulo unico di «contratti» è oggi diviso: ogni sottoprogetto offre alla UI i *suoi* contratti (`VistaMappa` nella mappa, `VistaPersonaggio` nei personaggi, `VistaMissione` nelle missioni…), e qui rimane solo il contratto del motore stesso.

## Dipendenze

**Dichiarate nel pom:** `foresta-trofei`, `foresta-missioni`, `foresta-bottino`, `foresta-personaggi`, `foresta-notizie`, `foresta-eventi`, `foresta-modellodati`.

**Lo dichiarano:** `foresta-motore`, `foresta-swing`.

Le dipendenze sono transitive: un modulo vede anche quello che vedono i moduli da cui dipende, quindi il compilatore garantisce la separazione fra i moduli che non si raggiungono.

Dipende dai sottoprogetti di cui la UI legge lo stato (trofei, missioni, bottino, personaggi, notizie: `VistaPartita` espone anche le ultime notizie) e da `foresta-modellodati`. Per transitività vede anche gli intermezzi, le tattiche e quasi tutto il resto del dominio, ma **lo mostra alla UI solo come viste in sola lettura e identificativi**.

## Funzionamento

### Le viste: `VistaPartita`

È il punto da cui la UI legge lo stato della partita, in sola lettura, **al posto dei singleton** del motore (`GruppoGiocatore.getIstanza()`, `Foresta`, `RegistroMissioni`…). `Main` passa alla UI l'implementazione del motore, `VistaPartitaMotore`. Espone: il gruppo del giocatore (`VistaGruppoGiocatore`), il gruppo avversario, l'ordine di combattimento, la mappa (`VistaMappa`), la cassa (`VistaCassa`), le missioni attive, completate e fallite (`VistaMissione`), le ultime notizie, il giorno, i punti esperienza e i mostri uccisi, i trofei (vinti, obiettivo e progresso), il bestiario (voci sbloccate, caratteristiche scoperte, moltiplicatori e abilità di classe), i prezzi con la contrattazione, la storia e i finali che racconta il motore, e l'aiuto della barra icone. I gruppi vanno chiesti ogni volta e non tenuti da parte: una partita nuova o riletta li sostituisce.

### I servizi

- **`ControlloreDiGioco`**: l'automa a stati visto da chi lo avvia (l'unica implementazione è `Automa`).
- **`GestoreSalvataggi`**: dove stanno le partite salvate. Gli slot da `NUMERO_1` a `NUMERO_4` (`SLOT_MANUALI`) sono quelli che sceglie il giocatore; `SALVATAGGIO_AUTOMATICO` è lo slot a parte che l'`Automa` scrive quando l'app va in background e non compare nell'elenco; `SLOT_IN_MEMORIA` (`NUMERO_5`) è il salvataggio automatico in memoria, ultima riga dell'elenco da cui si carica, che non si scrive a mano. `TestataSalvataggio` descrive un salvataggio (il capo, il momento) per elencarlo senza leggerlo tutto.
- **`GestorePunteggi`**: la classifica (`Punteggio`: chi l'ha fatto e quanto).
- **`StrumentiDiSviluppo`**: quel che la finestra di prova può fare alla partita oltre a guardarla (cambiare i personaggi del gruppo, far partire le missioni, scegliere gli oggetti dei cofani); li implementa il motore. I trucchi sui dadi passano da `strumenti.TrucchiDiSviluppo`.

I gestori su file di trofei e bestiario (`GestoreTrofei`, `GestoreBestiario`) stanno nei loro moduli, e il motore li implementa.

### Gli eventi

Il bus è in `foresta-eventi`; qui stanno gli eventi del **ciclo di vita** e dei **dialoghi** della macchina a stati, divisi per direzione:

| Pacchetto | Direzione | Esempi |
| :--- | :--- | :--- |
| `comandigiocatore` | UI → motore | `ComandoDiGioco`, `ComandoInvioTesto`, `ComandoScambioArtefatto`, `ComandoCommutazioneElenco`, `ComandoImpostazioneAiuto`, `ComandoSpostaPersonaggio` |
| `richieste` | motore → UI, chiedono un input o aprono una schermata | direzione, sì/no, testo, slot di salvataggio, uscita; apertura dell'inventario, dei trofei, del bestiario |
| `notifiche` | motore → UI, «è successo» | fine gioco, errore di caricamento, raccolta degli oggetti, statistiche e punteggi di fine gioco, `NotificaUscitaDalGioco` |
| `interni` | tecnici | `InternoFaseDiGioco`, `InternoAggiornamentoComandiDisponibili`, logo iniziale e UI pronta, caricamento completato, UI occupata/inattiva |

`InternoFaseDiGioco` porta una `FaseDiGioco` (`LOGO_INIZIALE`, `INTRO`, `NOME_PERSONAGGIO`, `SESSO_PERSONAGGIO`, `CLASSE_PERSONAGGIO`, `SALVATAGGIO_DA_SCRIVERE`): le fasi in cui la UI deve cambiare schermata o animazione, una piccola parte degli stati dell'`Automa`, che la UI non conosce. `NotificaUscitaDalGioco` è il modo in cui **il motore non chiude mai il processo**: la piattaforma si iscrive e decide come terminare (sul desktop `Main` chiama `System.exit`, su Android si chiude l'attività, su iOS si torna alla schermata iniziale); lo controlla `ConvenzioniDiPortabilitaTest`.

Alcuni eventi `Interno…` che portano classi del dominio (le parti di uno scambio, il banco di lavoro) stanno nel modulo del motore, e quelli con sprite e fumetti in quello dell'interfaccia: il pacchetto è lo stesso, il modulo è diverso.

### `ComandiAssociati`

Il comando con cui la UI sceglie un incantesimo, un canto, un'abilità o una mossa di carta, forbici e sasso, e viceversa. Sta qui e non nelle enum perché **i poteri non devono sapere come li chiede la UI**: è il comando che li conosce.

## Osservazioni

- Il contratto di trofei e bestiario aspetta ancora i loro moduli per la parte delle richieste di apertura (`RichiestaAperturaTrofei`, `RichiestaAperturaBestiario` stanno qui).
- Il modulo esiste per lasciare al compilatore il compito di impedire alla UI di usare il motore: `DipendenzeDalToolkitGraficoTest` controlla l'altra direzione (nessuno fuori da `foresta-swing` usa `java.awt` o Swing).
- Il quadro generale delle due metà è in [`motore_di_gioco.md`](motore_di_gioco.md) §1 e in [`didattico.md`](didattico.md).
