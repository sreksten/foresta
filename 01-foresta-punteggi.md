# 01-foresta-punteggi

La **classifica dei migliori punteggi** di tutte le partite: i dieci posti, chi li occupa e le regole per entrarci. Il modulo non sa dove la classifica si conserva: dà la classifica vera (`Classifica`), il contratto con cui il resto del gioco la usa (`GestorePunteggi`) e il dato di una riga (`Punteggio`).

Esiste a sé perché, come i trofei e il bestiario, **non appartiene a una partita**: vale da una partita all'altra e non fa parte di `ModelloDati`. Le sue regole (chi entra, chi esce, la stessa partita che si migliora) sono pure e si provano senza toccare un file; la scrittura su disco è un'altra cosa e la fa il motore.

## Dipendenze

**Dichiarate nel pom:** `00-foresta-serializzazione`.

**Lo dichiarano:** `11-foresta-motore-api`.

Le dipendenze sono transitive: un modulo vede anche quello che vedono i moduli da cui dipende, quindi il compilatore garantisce la separazione fra i moduli che non si raggiungono.

Dipende solo da `00-foresta-serializzazione`, per ripulire i nomi dal separatore dei file (`Serializzabile.pulisciCampo`) e per il nome di chi non ne ha messo uno (`NESSUN_NOME`).

## Funzionamento

### `Classifica`

Tiene tre colonne di `NUMERO_MASSIMO` (10) posti: il nome, il punteggio e la **partita** che l'ha fatto (null per le righe predefinite).

- **`ripristinaLaPredefinita()`** mette la classifica con cui si parte quando non ce n'è una da leggere: dieci nomi, dal primo a diecimila punti all'ultimo a zero.
- **`isPunteggioInClassifica(punteggio, idPartita)`**: se la partita è già in classifica entra solo migliorandosi, perché chi ricarica un salvataggio e rifinisce la partita non deve riempire la classifica di sé; altrimenti entra se supera l'ultimo.
- **`inserisci(nome, punteggio, idPartita)`** mette il punteggio al suo posto e dice se è entrato. Il punteggio precedente della stessa partita se ne va (gli altri risalgono), altrimenti esce l'ultimo; poi il nuovo scorre in su fino al posto che gli spetta.
- **`setPunteggio(posizione, ...)`** serve a chi legge la classifica da dove si conserva.
- **`pulisciNome`** toglie il `|` e gli a capo dai nomi, come per i salvataggi; un nome vuoto diventa «nessun nome».

### Il contratto

- **`Punteggio`** (pacchetto `strumenti`): nome, punteggio e identificativo della partita (che non si mostra).
- **`GestorePunteggi`** (pacchetto `interfacce`): `carica`, `getConteggio`, `getPunteggio(posizione)`, `isPunteggioInClassifica`, `addPunteggio`, `salva`. `Main` crea l'implementazione e la passa all'`Automa`, che vi aggiunge i punteggi a fine partita, e alla UI, che la mostra nell'intro.

### Dove si conserva

Nel motore. `GestorePunteggiBase` tiene una `Classifica` e decide quando leggerla (riparte dalla predefinita se non ci riesce) e quando scriverla (solo se un punteggio entra); `GestorePunteggiSuFile` la legge e la scrive nel file `forestaHS` della cartella `.foresta`, una riga per posto (`idPartita|nome|punteggio`). Un file rovinato si mette da parte con l'estensione `.rovinato`. I test usano `GestorePunteggiInMemoria`.

## Osservazioni

- I punti della partita in corso non sono qui: li tiene `Statistiche`, in `06-foresta-statistiche`.
- Il quadro dei salvataggi e della classifica su file è in [`motore_di_gioco.md`](motore_di_gioco.md) §5.
