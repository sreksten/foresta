# foresta-bottino

Il **bottino**: quello che il gruppo trova nelle locazioni e può prendere — monete, pietre preziose, corone, un cofano, un anello, un artefatto per terra, un oggetto di una missione. Il modulo dice *che cosa c'è* (le classi del bottino, quanti ne sono stati trovati, quale artefatto portano) e lo costruisce (`FabbricaOggetti`); *che cosa succede quando il gruppo lo prende* lo decide `RaccoltaBottino`, che sta nel motore.

È un modulo a sé perché il bottino è un dato, non una regola: non si salva (se il gruppo non lo prende, alla visita successiva la locazione decide di nuovo che cosa c'è), e le missioni devono poter mettere i loro oggetti nelle locazioni senza dipendere dal motore.

## Dipendenze

**Dichiarate nel pom:** `foresta-intermezzi`.

**Lo dichiarano:** `foresta-missioni`, `foresta-motore-api`.

Le dipendenze sono transitive: un modulo vede anche quello che vedono i moduli da cui dipende, quindi il compilatore garantisce la separazione fra i moduli che non si raggiungono.

Dichiara solo `foresta-intermezzi`, ma da lì vede per transitività le statistiche (il livello del mondo, per far nascere gli artefatti), i personaggi, la mappa e gli artefatti. Dipende dagli intermezzi perché il `Cofano` che contiene un libro tecnico lo fa leggere subito al capo con un intermezzo (`IntermezzoLibroTecnico`).

## Funzionamento

### Il bottino e i suoi tipi

- **`Bottino`** è il contratto: ogni oggetto sa che cos'è (`getClasse()`, un `TipoBottino`), come si chiama, quanti ne sono stati trovati (`getQuantita()`) e quale artefatto porta (`getArtefatto()`).
- **`TipoBottino`** elenca gli oggetti che si trovano nelle locazioni, ognuno con quanti se ne trovano al massimo e con il suo valore in punti: anello, cofano, corona, pietra preziosa, moneta, le forme di artefatto per terra (scudo, spada, spadone, falce, mazza, ascia, lancia, bastone magico, libro magico, elmo, maschera, armatura, schinieri), l'artefatto generico e l'oggetto di una missione. Solo i tipi *generabili* li costruisce `FabbricaOggetti`: gli artefatti già fatti e gli oggetti delle missioni nascono altrove.
- **`OggettoBase`** è la base degli oggetti delle locazioni: tiene la quantità (di solito tirata fra uno e il massimo della classe) e, quando l'oggetto viene preso, assegna i punti del suo valore e riapre le finestre di stato e di statistiche.

### Gli oggetti

- `Moneta`, `PietraPreziosa`, `Corona`: monete d'oro, pietre, corone, con quantità e valore.
- `Anello`: una volta su sei è magico, di uno di tre tipi, e allora porta un artefatto con i suoi effetti; altrimenti non ha effetto.
- `Cofano`: il gruppo che lo apre trova qualcosa a caso fra una pergamena, tre pergamene, una pozione (salute, salute grande o magia), monete o pietre preziose. Con una piccola probabilità c'è anche un ingrediente magico o un artefatto, e molto di rado un libro tecnico. Cosa dà e con che probabilità sta in `Cofano.artefattoRaro` e `CostantiBottino.COFANO_PROBABILITA_*`; `Cofano.opzioniOggettoSpeciale` fa vedere e forzare gli oggetti speciali nella finestra di prova (`COFANO_ESITO` e `COFANO_OGGETTO` di `ContestoLancio`).
- `NomeOggetto`: il nome di un oggetto che non ha una classe sua, come gli oggetti delle missioni, con singolare, plurale e articoli, così le descrizioni delle locazioni lo flettono come gli altri.

### Gli artefatti come bottino

Un **artefatto** non è bottino: è il pezzo di equipaggiamento vero, che si conserva e si salva con il suo `ArtefattoMD`. Quando il gruppo ne trova uno lo trova dentro un `BottinoArtefatto`, in due casi: una spada, uno scudo, un elmo… per terra (`nuovo(forma)`: nasce con il suo artefatto, al livello del mondo, e la locazione lo descrive con il nome della forma, «una spada») oppure un artefatto già fatto, come quello custodito in un tempio (`giaFatto(artefatto)`: si descrive con il suo nome completo e, quando lo si prende, si mostra con la rivelazione). `FormaDelBottino` dice, per ogni forma, di quale `TipoArtefatto` è, quale `TipoBottino` ha (e quindi quale sprite nella locazione) e come si chiama al singolare e al plurale con gli articoli.

### Costanti ed eventi

`CostantiBottino` raccoglie le probabilità dei cofani, i valori dell'anello magico e le probabilità del bottino dei gruppi avversari (pergamena, pozione di salute per gli umanoidi). Gli eventi: `InternoOggettoRaccolto` (quale oggetto, in che quantità, l'eventuale artefatto, se era custodito dagli avversari), `InternoArtefattoTrovato` (serve ai trofei dei tesori) e `NotificaArtefattoTrovato` (la UI lo mostra con una rivelazione).

## Osservazioni

- Il bottino è un dato volutamente stupido: la raccolta — a chi va, come si consegna al gruppo, quanti punti dà — sta in `RaccoltaBottino` (motore), per non far dipendere il bottino dal gruppo.
- Per un nuovo tipo di arma o di artefatto da trovare per terra servono un `TipoBottino`, una voce in `FormaDelBottino` e l'immagine in `MappaBottinoImmagine` (nella UI).
