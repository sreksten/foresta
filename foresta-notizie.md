# foresta-notizie

Le **notizie**: lo storico di quel che il giocatore ha visto. Il modulo tiene gli ultimi messaggi del pannello di testo (per ripopolarlo dopo un ricaricamento, con la stessa impaginazione) e le ultime notizie delle locande, quelle che scorrono nel notiziario sotto la mappa.

Esiste a sé perché è una parte con dati propri che non ha bisogno di sapere niente del motore: ascolta il bus per i messaggi, riceve le notizie da chi le produce e prende il suo dato da chi lo custodisce. Con il suo arrivo `foresta-modellodati` resta con la sola classe `ModelloDati`.

## Dipendenze

**Dichiarate nel pom:** `foresta-eventi`, `foresta-serializzazione`.

**Lo dichiarano:** `foresta-modellodati`, `foresta-motore-api`.

Le dipendenze sono transitive: un modulo vede anche quello che vedono i moduli da cui dipende, quindi il compilatore garantisce la separazione fra i moduli che non si raggiungono.

Dipende da `foresta-eventi` per ascoltare le notifiche di testo (`NotificaTestoFrase`, `NotificaTestoParagrafo`) e da `foresta-serializzazione` perché i dati si salvano con la partita (`Serializzabile`, `LettoreCampi`). Non conosce il motore, il gruppo né la mappa.

## Funzionamento

### I dati

Stanno nel pacchetto `modellodati`, come quelli di ogni parte:

- **`NotizieMD`** tiene due liste, dalla più recente alla più vecchia: gli ultimi messaggi (fino a `MASSIMO_MESSAGGI_RICORDATI`, 100) e le ultime notizie (fino a `MASSIMO_NOTIZIE_RICORDATE`, 10). Le collezioni che mostra sono di sola lettura; per cambiarle si passa da `aggiungiMessaggio` e `aggiungiNotizia`.
- **`MessaggioMD`** è un messaggio di testo e ricorda se era l'inizio di un paragrafo o la continuazione di una frase, così il pannello si ripopola con la stessa impaginazione. Nel salvataggio è `P|testo` per un paragrafo e `F|testo` per una continuazione.
- **`NotiziaMD`** è una notizia per la mappa: un identificativo e il corpo (`id|corpo`).

### `Notizie`: la facciata

`Notizie` è una classe di soli metodi statici, nel pacchetto `notizie`. Il dato non lo possiede: lo chiede a ogni uso a un fornitore (`setFornitoreDiDati`), che installa `ModelloDati.installa()` e che punta sempre al modello dati in corso, perché cambia quando si carica un salvataggio. Senza fornitore lancia un `IllegalStateException` esplicito.

- **I messaggi** li prende dal bus: `registrati()` si iscrive a `NotificaTestoFrase` e `NotificaTestoParagrafo`. Sono centinaia i punti del gioco che le pubblicano, e lo storico è proprio quel che il giocatore ha visto, quindi ascoltarle è la scelta giusta. `registrati()` va chiamato una volta sola, prima che il gioco pubblichi notifiche (lo fa `StatoDelMotore.registraComponenti()`).
- **Le notizie** le riceve da chi le produce: `aggiungiNotizia`, chiamata da `Locanda` nel motore. Nessun altro le ascolta. Vanno in testa e le più vecchie oltre il limite si scartano.
- **La lettura**: `getUltimiMessaggi()` serve all'`Automa` per ripopolare il pannello dopo un caricamento (`InternoCaricamentoCompletato`), `getUltimeNotizie()` alla UI, che la legge da `VistaPartita` per il notiziario e per la mappa a tutto schermo.

### Chi produce le notizie

Non questo modulo. `NotizieDelleLocande` sta nel motore (usa il gruppo, i personaggi e la locanda): sceglie la notizia dalla grammatica `NOTIZIE_<identificativo della locanda>`, evita le ultime ricordate e la consegna a `Notizie`. Il quadro è in [`motore_di_gioco.md`](motore_di_gioco.md) («Notizie delle locande»).

## Osservazioni

- Il test della facciata (`NotizieTest`) non ha bisogno di una partita: installa un fornitore con un `NotizieMD` suo e un bus a consegna immediata.
- Il codice di `NotizieDelleLocande` presuppone che ogni alternativa di `NOTIZIE_*` abbia la forma `ID-Titolo - Corpo` senza verificarlo (vedi `motore_di_gioco.md`).
