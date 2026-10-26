# foresta-trofei

I **trofei**: traguardi che il giocatore conquista e che valgono **da una partita all'altra** (uccidere cento goblin, comprare mille artefatti, sconfiggere il Drago…). Il modulo contiene l'elenco dei trofei (`TipoTrofeo`, con la categoria e i testi), il catalogo che dice quale evento fa avanzare quale trofeo (`RegolaTrofeo`), i trigger (`Trofeo`, `TrofeoAContatore`, `TrofeoEnciclopedista`, `TrofeoPerdigiorno`), la facciata `RegistroTrofei` e il dato `TrofeiMD`.

Come il bestiario, i trofei **non appartengono a una partita**: non fanno parte di `ModelloDati`, non si reimpostano mai e si salvano in un file a parte (`GestoreTrofei`, che il motore implementa su file). È in alto nella catena perché per contare ha bisogno di *ascoltare* un po' di tutto — missioni, bottino, acquisti, uccisioni — e non deve essere ascoltato da nessuno.

## Dipendenze

**Dichiarate nel pom:** `foresta-comandi`, `foresta-missioni`, `foresta-bestiario`.

**Solo per i test:** `foresta-personaggi`.

**Lo dichiarano:** `foresta-motore-api`.

Le dipendenze sono transitive: un modulo vede anche quello che vedono i moduli da cui dipende, quindi il compilatore garantisce la separazione fra i moduli che non si raggiungono.

Dipende da `foresta-missioni` (e per transitività da quasi tutto il dominio: economia, bottino, intermezzi, personaggi, mappa…) perché le regole nominano gli eventi `Interno…` e `Notifica…` che gli altri moduli pubblicano, da `foresta-bestiario` per il trofeo dell'enciclopedista e da `foresta-comandi` perché la richiesta di aprire la pagina (`RichiestaAperturaTrofei`) è una `RichiestaConComandi`.

## Funzionamento

### I trofei

`TipoTrofeo` elenca i trofei che il giocatore può vincere (30), a differenza delle missioni non scadono mai e, una volta vinto, un trofeo non si perde più. Hanno una `CategoriaTrofeo` (speciale, missione, uccisione, bottino, acquisto, diplomazia: è l'ordine in cui compaiono nella pagina dei trofei). Il nome e la descrizione stanno in `tipo_trofeo_<lingua>.properties` (`COSTANTE.NOME`, `COSTANTE.DESCRIZIONE`).

### I trigger

- **`Trofeo`** è il trigger: come una missione ha un innesco, ma non scade mai e, una volta vinto, vale per tutte le partite successive. Si controlla **a fine locazione**, solo finché il trofeo non è stato vinto.
- **`TrofeoAContatore<E>`** vince accumulando, partita dopo partita, quanto vale un certo evento interno fino a una soglia: `new TrofeoAContatore<>(tipo, ClasseEvento.class, evento -> 1 o 0, obiettivo)` (i pasti in locanda, i goblin sconfitti, le amicizie strette…). Un obiettivo 1 è un trofeo «una volta sola».
- **`TrofeoEnciclopedista`**: si vince quando tutte le voci del bestiario sono sbloccate; guarda il bestiario, non il progresso accumulato.
- **`TrofeoPerdigiorno`**: vinto quando sono stati vinti tutti gli altri; non conta nessun evento, guarda solo il registro.
- **`RegolaTrofeo`** elenca i trofei esistenti con il loro trigger: una voce con lo stesso nome della costante di `TipoTrofeo`.

### `RegistroTrofei`

È la facciata su `TrofeiMD`: ricorda i trofei vinti e il progresso verso gli altri (`isVinto`, `getProgresso`, `incrementaProgresso`), si iscrive agli eventi (`registrati()`), controlla i trigger e pubblica `InternoTrofeoAcquisito` (la UI lo annuncia con una notifica globale). Il modello si legge una volta all'avvio (`impostaGestoreTrofei`).

Il progresso guadagnato in una locazione resta **in sospeso** e conta solo se il gruppo ne esce vivo: a fine locazione diventa progresso, si controllano i trofei e si salva. Se il gruppo muore la fine della locazione non arriva e il sospeso si scarta (`scartaIlProgressoInSospeso`) quando la partita finisce, ne comincia una nuova o se ne carica una — lo fa l'`Automa`; **non** quando si prepara una locazione, perché le missioni completate prima (a inizio locazione, o all'accampamento) hanno già contato.

### La richiesta di aprire la pagina

`RichiestaAperturaTrofei` (pacchetto `eventi.richieste`) è l'evento con cui l'`Automa` chiede alla UI di mostrare la pagina dei trofei quando il giocatore la apre dall'inventario; porta il solo comando `ANNULLA`.

## Osservazioni

- Per aggiungere un trofeo: una costante in `TipoTrofeo` (con la `CategoriaTrofeo`), `NOME` e `DESCRIZIONE` nel file dei testi, una voce in `RegolaTrofeo` con lo stesso nome. Se manca un evento adatto se ne pubblica uno nuovo, nel modulo della parte di cui parla (e quindi sotto i trofei). La ricetta è in `CLAUDE.md`.
- Un trofeo viene controllato e annunciato solo a **fine locazione**, non nel momento dell'evento.
- Il quadro è in [`motore_di_gioco.md`](motore_di_gioco.md) §10.
