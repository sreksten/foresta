# foresta-app

**L'applicazione**: il punto d'ingresso del gioco (`Main`), il registro di tutto ciò che accade sul bus (`SnifferBusEventi`) e i test che hanno bisogno di motore e interfaccia grafica insieme. È l'**unico posto che conosce insieme il motore e la UI e li collega**: li lascia dialogare solo tramite eventi.

Esiste perché il motore e la UI, per costruzione, non si conoscono (dipendono entrambi da `foresta-motore-api`): ci vuole qualcuno che li assembli. Un altro front-end (Android, iOS) è un'altra applicazione con la stessa funzione, sopra lo stesso motore.

## Dipendenze

**Dichiarate nel pom:** `foresta-motore`, `foresta-swing`.

**Solo per i test:** `foresta-motore`.

**Lo dichiarano:** nessun modulo.

Le dipendenze sono transitive: un modulo vede anche quello che vedono i moduli da cui dipende, quindi il compilatore garantisce la separazione fra i moduli che non si raggiungono.

Dipende da `foresta-motore` e da `foresta-swing`, e per i test anche dal `test-jar` di `foresta-motore`, da cui prende gli helper dei test del motore (`PartitaDiTest` e simili). È in cima alla catena: nessun altro modulo lo dichiara. Il suo `pom` produce nella radice il jar eseguibile con le dipendenze (`foresta-0.0.1-SNAPSHOT-jar-with-dependencies.jar`, classe principale `com.threeamigos.foresta.Main`).

## Funzionamento

### `Main`

Mette insieme le parti nell'ordine giusto:

1. installa il thread principale (`ThreadPrincipale` con l'`EsecutoreSwing`: tutta la logica gira sull'Event Dispatch Thread di Swing);
2. crea lo `SnifferBusEventi`;
3. si iscrive a `NotificaUscitaDalGioco`: sul desktop si esce con `System.exit(codice)` (il motore **non chiude mai il processo**, lo chiede con questo evento e lo decide `Main`), loggando il motivo se è un errore;
4. registra i componenti del motore (`StatoDelMotore.registraComponenti()`) e legge gli argomenti;
5. crea il gestore della classifica e imposta i gestori dei trofei e del bestiario, che valgono da una partita all'altra e si rileggono una volta sola, all'avvio;
6. crea l'`Automa` con un temporizzatore, il gestore dei salvataggi su file e la classifica;
7. si iscrive a `InternoInterfacciaUtentePronta` **prima** di creare la UI (altrimenti l'evento, pubblicato sul thread principale, andrebbe perso lasciando il gioco sulla finestra nera) e fa partire l'automa quando arriva;
8. in modalità di prova installa i trucchi sui dadi, crea `VistaPartitaMotore` e `ForestaUI`, e apre la `FinestraDiProva`.

Argomenti riconosciuti, senza distinguere maiuscole e minuscole: `ORIZZONTALE`, `VERTICALE`, `TUTTOSCHERMO`, `SALTALOGO`, `BARRACLASSICA`, `ZOOM=<numero>`, `MODALITA_DI_PROVA`; quelli sconosciuti sono ignorati con una riga nel log.

### `SnifferBusEventi`

Si iscrive a tutti i tipi di evento del bus e ne scrive una riga nel log, con il tipo in testa, per poterle cercare con `grep` (per esempio `INTERNO_DECISIONE_TATTICA`): serve a seguire dall'esterno cosa accade in una partita, senza influire sul gioco. `SnifferBusEventiTest` controlla che sia iscritto a ogni evento. Il log va su console e, con `-Dforesta.log=cartella`, anche su un file per sessione.

### I test

Stanno qui i test che il compilatore non può far stare più in basso, in due gruppi:

- **le regole sul codice, che leggono i sorgenti dei moduli** (con `SorgentiDelProgetto`, che sale fino alla cartella che contiene i moduli, partendo da quella da cui gira il test): `DipendenzeDalToolkitGraficoTest` (nessuno fuori da `foresta-swing` usa `java.awt`, `javax.swing`, `javax.imageio`, `javax.sound`), `ConvenzioniDiPortabilitaTest` (il motore non chiude il processo, niente chiamate dipendenti dalla lingua del dispositivo senza `Locale`, i testi del log passati come fornitore), `CostantiDegliEventiTest` (ogni evento usa la costante del suo `TipoEvento`), `RiferimentiAlleImmaginiTest`;
- **i test che usano motore e UI insieme**: le schermate (bestiario, commerciante, doppio click dello scambiatore), i riquadri, la barra delle icone, il canvas, le tattiche, la finestra di prova, i font.

## Osservazioni

- Per aggiungere un'altra applicazione (un altro front-end) basta un modulo che dipenda da `foresta-motore` e dalla propria UI e faccia ciò che fa `Main`: installare il proprio `Esecutore` del thread principale, la propria destinazione di log, i gestori su file nella cartella giusta e il proprio modo di uscire.
- I jar nella radice non si committano mai.
