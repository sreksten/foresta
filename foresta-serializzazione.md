# foresta-serializzazione

Come si **scrivono e si rileggono i salvataggi**: un testo a righe, una riga per record, con i campi separati da `|`. Tre classi piccole che non sanno *che cosa* si salva, solo *come* si scrive.

Sta in un modulo a parte, senza dipendenze, perché il formato è un'idea indipendente dal gioco e perché lo usano moltissimi moduli: ognuno porta i propri dati (le classi `…MD`) e ha bisogno di scriverli e leggerli nello stesso modo, senza passare dal modulo che li mette insieme (`foresta-modellodati`).

## Dipendenze

**Dichiarate nel pom:** nessuna, solo il JDK.

**Lo dichiarano:** `foresta-attributi`, `foresta-effettidistato`, `foresta-artefatti`, `foresta-mappa`, `foresta-personaggi`, `foresta-notizie`, `foresta-modellodati`.

Le dipendenze sono transitive: un modulo vede anche quello che vedono i moduli da cui dipende, quindi il compilatore garantisce la separazione fra i moduli che non si raggiungono.

Non dipende da niente, nemmeno dal registro di log: solo dal JDK. Il formato dei dati del gioco sta nelle classi `…MD` dei moduli che le possiedono, e il file intero lo compone `ModelloDati`.

## Funzionamento

### `Serializzabile`: scrivere

È l'interfaccia di chi sa scriversi (`salva(PrintWriter)`) e rileggersi (`leggi(BufferedReader)`), e offre i metodi statici per comporre una riga:

- `stampaRiga(stream, valori…)` scrive una riga con i valori separati da `Serializzabile.PIPE` (`|`) e poi l'a capo. Un `enum` si scrive con il suo nome, il resto come lo scriverebbe `print`.
- `campo(valore)` fa la stessa conversione per un campo solo; `facoltativo(valore)` scrive un campo che può mancare come campo vuoto (che il lettore rilegge come `null`).
- `composto(valori…)` mette più valori in un solo campo separati dal carattere `§` (per esempio `FORZA§12`), quello che il lettore apre con `sottocampi`.
- `pulisciCampo(testo)` e `senzaACapo(testo)` ripuliscono in silenzio un testo che finirà in un campo: un nome con dentro un `|` sposterebbe tutti i campi seguenti, e un a capo spezzerebbe la riga.
- `NESSUN_NOME` è il nome di ciò a cui è stato dato un nome vuoto (artefatti, incantamenti, classifica).

### `LettoreCampi`: leggere

Legge i campi di una riga **conservando quelli vuoti** (`a||b` sono tre campi, a differenza di `StringTokenizer`), così un testo vuoto non sposta quelli che lo seguono. Una riga vuota non ha nessun campo: è il caso delle liste vuote scritte su una sola riga.

I metodi leggono il campo successivo con il tipo giusto: `testo`, `testoFacoltativo`, `intero`, `decimale`, `booleano`, `enumerato(Class)` e `enumeratoFacoltativo(Class)` (che ritrovano la costante dal nome, come i salvataggi), `sottocampi(min, max)` per i campi composti, `resto()` per l'ultimo campo (che può contenere `|`), `haAltriCampi()`. Per le righe che contengono un solo valore ci sono le versioni statiche `leggiIntero`, `leggiTesto`, `leggiBooleano` che leggono direttamente da un `BufferedReader`. Una riga mancante o un campo illeggibile sono `IOException` con la riga intera nel messaggio, così si capisce dov'è l'errore.

### `MappaProprieta`: le mappe chiave-valore

Scrive e rilegge in una sola riga una mappa di proprietà stringa-stringa. La condividono le mappe di `LocazioneMD` e `MissioneMD`, dove lo stato (`ATTIVA`, `COMPLETA`…) è la presenza di una chiave. `pulisciValore` toglie dal valore i separatori che romperebbero la riga.

## Osservazioni

- I salvataggi **non devono restare compatibili con le versioni precedenti**: il formato si cambia quando serve.
- Chi scrive e chi legge devono restare allineati a mano, campo per campo: non c'è uno schema. I test (`LettoreCampiTest`, `ModelloDatiSalvataggioTest`, `RileggereSostituisceTest`…, in `foresta-modellodati`) scrivono e rileggono per tenerli allineati.
