# 13-foresta-app-swing

**L'applicazione**: il punto d'ingresso del gioco (`Main`), il registro di tutto ciò che accade sul bus (`SnifferBusEventi`) e i test che hanno bisogno di motore e interfaccia grafica insieme. È l'**unico posto che conosce insieme il motore e la UI e li collega**: li lascia dialogare solo tramite eventi.

Esiste perché il motore e la UI, per costruzione, non si conoscono (dipendono entrambi da `11-foresta-motore-api`): ci vuole qualcuno che li assembli. Un altro front-end (Android, iOS) è un'altra applicazione con la stessa funzione, sopra lo stesso motore.

## Dipendenze

**Dichiarate nel pom:** `12-foresta-motore`, `12-foresta-swing`.

**Solo per i test:** `12-foresta-motore`.

**Lo dichiarano:** nessun modulo.

Le dipendenze sono transitive: un modulo vede anche quello che vedono i moduli da cui dipende, quindi il compilatore garantisce la separazione fra i moduli che non si raggiungono.

Dipende da `12-foresta-motore` e da `12-foresta-swing`, e per i test anche dal `test-jar` di `12-foresta-motore`, da cui prende gli helper dei test del motore (`PartitaDiTest` e simili). È in cima alla catena: nessun altro modulo lo dichiara. Il suo `pom` produce nella radice il jar eseguibile con le dipendenze (`foresta-0.0.1-SNAPSHOT-jar-with-dependencies.jar`, classe principale `com.threeamigos.foresta.Main`).

## Funzionamento

### `Main`

Mette insieme le parti nell'ordine giusto:

1. legge gli argomenti con `OpzioniDiAvvio` (modulo comune, la stessa di libGDX): `ORIZZONTALE`, `VERTICALE`, `TUTTOSCHERMO`, `SALTALOGO`, `BARRACLASSICA`, `JOYSTICK_VIRTUALE` (il joystick a schermo sulla mappa, anche dove c'è il mouse), `LINGUA=<codice>`, `ZOOM=<numero>`, `MODALITA_DI_PROVA`, senza distinguere maiuscole e minuscole; quelli sconosciuti sono ignorati con una riga nel log;
2. crea lo `SnifferBusEventi`;
3. collega il motore con `AvvioDelMotore` (in `12-foresta-motore`, lo stesso di libGDX): modello dati, thread principale (`EsecutoreSwing`: tutta la logica gira sull'Event Dispatch Thread di Swing), uscita (sul desktop `System.exit(codice)`: il motore **non chiude mai il processo**, lo chiede con `NotificaUscitaDalGioco`), componenti del motore, gestori su file, `Automa` che parte con `InternoInterfacciaUtentePronta` (l'iscrizione c'è **prima** che la UI esista, altrimenti l'evento andrebbe perso lasciando il gioco sulla finestra nera), trucchi sui dadi in modalità di prova, `VistaPartitaMotore`;
4. crea `ForestaSwing` e, in modalità di prova, apre la `FinestraDiProva`.

### `SnifferBusEventi`

Si iscrive a tutti i tipi di evento del bus e ne scrive una riga nel log, con il tipo in testa, per poterle cercare con `grep` (per esempio `INTERNO_DECISIONE_TATTICA`): serve a seguire dall'esterno cosa accade in una partita, senza influire sul gioco. `SnifferBusEventiTest` controlla che sia iscritto a ogni evento. Il log va su console e, con `-Dforesta.log=cartella`, anche su un file per sessione.

### I test

Stanno qui i test che il compilatore non può far stare più in basso, in due gruppi:

- **le regole sul codice, che leggono i sorgenti dei moduli** (con `SorgentiDelProgetto`, che sale fino alla cartella che contiene i moduli, partendo da quella da cui gira il test): `DipendenzeDalToolkitGraficoTest` (nessuno fuori da `12-foresta-swing` usa `java.awt`, `javax.swing`, `javax.imageio`, `javax.sound`, e nessuno fuori dai due moduli libGDX usa `com.badlogic`), `ConvenzioniDiLocalizzazioneTest` (le stringhe italiane nel codice si contano contro una base di partenza), `ConvenzioniDiPortabilitaTest` (il motore non chiude il processo, niente chiamate dipendenti dalla lingua del dispositivo senza `Locale`, i testi del log passati come fornitore), `CostantiDegliEventiTest` (ogni evento usa la costante del suo `TipoEvento`), `RiferimentiAlleImmaginiTest`;
- **i test che usano motore e UI insieme**: le schermate (bestiario, commerciante, doppio click dello scambiatore), i riquadri, la barra delle icone, il canvas, le tattiche, la finestra di prova, i font.

## Osservazioni

- Per aggiungere un'altra applicazione (un altro front-end) basta un modulo che dipenda da `12-foresta-motore` e dalla propria UI e faccia ciò che fa `Main`: installare il proprio `Esecutore` del thread principale, la propria destinazione di log, i gestori su file nella cartella giusta e il proprio modo di uscire.
- I jar nella radice non si committano mai.
