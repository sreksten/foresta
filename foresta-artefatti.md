# foresta-artefatti

Gli **artefatti**: armi, scudi, armature, accessori e ingredienti magici, con i loro modificatori di attributo e i loro incantamenti; il **generatore** che li produce a caso per il bottino e i negozi, il **listino** dei prezzi delle pergamene, le regole della **fusione** dell'incantatore e gli **oggetti leggendari** (con i loro set e i libri tecnici) letti da un file di testo.

È un modulo a sé perché gli artefatti sono *solo oggetti*: sanno come sono fatti, quanto valgono e come si fondono, ma non sanno chi li raccoglie, li indossa o li vende, e non sanno nemmeno che esiste un mondo (il livello a cui nascono è un **parametro**). Questo permette ai personaggi, al bottino, ai negozi e alle missioni di usarli senza che gli artefatti dipendano da loro.

## Dipendenze

**Dichiarate nel pom:** `foresta-testi`, `foresta-strumenti`, `foresta-serializzazione`, `foresta-attributi`, `foresta-danni`.

**Lo dichiarano:** `foresta-personaggi`.

Le dipendenze sono transitive: un modulo vede anche quello che vedono i moduli da cui dipende, quindi il compilatore garantisce la separazione fra i moduli che non si raggiungono.

- `foresta-attributi`: un artefatto porta modificatori di attributo (`ModificatoreAttributoMD`), e ne ha bisogno anche il listino.
- `foresta-danni`: gli incantamenti sono danni elementali (`TipoDanno`) che si sommano al colpo dell'arma, e le armi hanno un tipo di danno.
- `foresta-testi`: i nomi e le descrizioni dei tipi stanno in file per lingua, e i testi a caso degli oggetti vengono dalle grammatiche (`GrammarBean`).
- `foresta-strumenti`: il caso (`Dado`) per generare gli artefatti.
- `foresta-serializzazione`: gli artefatti si salvano con la partita.

## Funzionamento

### I tipi

| Enum | Che cosa dice |
| :--- | :--- |
| `TipoArtefatto` | i 24 tipi: armi (spada, mazza, ascia, spadone, falce, lancia, bastone magico, libro magico), difese (scudo, elmo, armatura, veste, maschera, schinieri), accessori (anello, talismano, ninnolo), **ingredienti magici** (pergamena, gemma, monile, gingillo, sigillo) e due oggetti speciali dei cofani, `AGGIORNAMENTO` e `RIPROGRAMMAZIONE` |
| `CategoriaArtefatto` | raggruppa i tipi che seguono le stesse regole: le armi colpiscono, le armature proteggono, gli ingredienti si fondono |
| `TipoSlotArtefatto` | dove lo porta un personaggio: testa, volto, corpo, mano principale, mano secondaria, entrambe le mani, gambe, accessorio |
| `TipoCardinalitaArtefatto` | si conta per pezzo singolo o per paio (gli schinieri) |
| `TipoRaritaArtefatto` | comune, raro, leggendario: decide quanti effetti (incantamenti più modificatori) può avere — livello −1, livello, livello +1 — fino al tetto di 3, 4 e 5 |
| `GradoIncantamento` | minore, medio, maggiore: bonus fisso (5, 10, 15) e coefficiente (5%, 10%, 20%); il grado dipende dal livello |
| `TipoNegozio` | che cosa tratta ogni negozio: l'armaiolo o il venditore di pergamene |
| `AspettoArtefatto` | l'aspetto speciale con cui un artefatto compare nelle schermate quando non è quello del suo tipo (libro tecnico, spada nascosta…) |
| `StatoPezzoDelSet` | dove sta un pezzo di un set leggendario: indossato, nel gruppo… |
| `TipoMotivoRifiutoIncantatura` | perché l'incantatore non accetta qualcosa sul banco |

### L'artefatto e i suoi dati

`Artefatto` è un **involucro sul suo `ArtefattoMD`**: i dati (tipo, rarità, nome con l'articolo, nome proprio e descrizione, livello, danni base, costo, peso, slot, aspetto, identificativo unico, modificatori, incantamenti, set e pezzo leggendario) stanno nel dato e si salvano con la partita; le regole sono nell'involucro. `Artefatto.di(md)` ricostruisce la sottoclasse giusta (`ArmaFisica` per le armi, `SpadaNormalmenteNascosta` per quella speciale), perché un artefatto riletto dal salvataggio non perda il suo tipo concreto. Un **incantamento** (`IncantamentoMD`) è un danno elementale in più: una parte fissa e una che cresce col coefficiente.

`CostruttoreArtefatto` è uno *step builder*: il compilatore impone l'ordine (tipo, nome, descrizione, livello, danni base se è un'arma, costo, peso) e poi si aggiungono modificatori e incantamenti. `VistaArtefatto`, `VistaScambio`, `VistaBancoDiLavoro` e `VistaPezzoDelSet` sono le **viste in sola lettura** che la UI usa al posto degli oggetti veri; `OggettoConCosto` e `OggettoConPeso` sono i contratti di ciò che ha un prezzo e un peso.

### Il generatore

`GeneratoreArtefatti.istanza()` genera, dato un livello, un artefatto di un tipo (`generaArtefatto`), uno a caso (`generaArtefattoCasuale`, senza gli ingredienti), un ingrediente magico (`generaIngrediente`: tanti effetti quanto il livello, fino a tre, tutti della specialità del tipo e del grado adatto) o un `AGGIORNAMENTO` (`generaAggiornamento`, che fa salire un artefatto di un livello). I danni medi di un'arma crescono con il livello (`danniMediArma`: almeno più delle mani nude). Ogni tanto un artefatto incantabile nasce già incantato, tanto più spesso quanto più è alto il livello. L'implementazione è `GeneratoreArtefattiTabelle` (tabelle interne) con `GrammaticaArtefatti` per i nomi e gli effetti (da `artefatti2.txt` e `ingredienti.txt`); il livello da passare di solito è quello del mondo, ma il modulo non lo sa.

Tutti i valori di bilanciamento — probabilità, tetti, prezzi, soglie dei gradi — sono in `CostantiArtefatti`.

### Il listino e la fusione

- `ListinoPergamene.prezzo(...)` dà il prezzo di un ingrediente come **somma dei prezzi dei suoi effetti**: per un incantamento il danno grezzo che aggiunge (bonus fisso × livello più coefficiente × intelligenza tipica) più un 25% se il tipo di danno ha effetti di stato; per un modificatore di quanto aumenta l'attributo rispetto al suo valore tipico. I valori tipici vengono dalla media delle classi.
- `RegoleIncantatura` sono le regole della **fusione** dell'incantatore: sul banco un solo artefatto incantabile e almeno un ingrediente; fra gli effetti già presenti e quelli degli ingredienti restano i più preziosi, senza superare il massimo di effetti dell'artefatto (un effetto migliore ne scalza uno vecchio; quelli che non entrano si perdono, e `AvvisoDiFusione` lo dice prima). Costo: 10 monete più 5 per effetto trasferito, e gli ingredienti usati si distruggono. Con il libro `RIPROGRAMMAZIONE` sul banco l'artefatto perde tutti gli incantamenti e per ognuno nasce un ingrediente «del Core Dump» (`IngredienteDelCoreDump`); un `AGGIORNAMENTO` fa salire l'artefatto di un livello fino al livello del mondo.
- `BancoDiLavoro` è il banco: un `ScambiatoreArtefatti` che vive solo mentre si è nella bottega e non si salva; uscendo, quel che resta torna nel gruppo. `ScambiatoreArtefatti` è il contratto di chi ha un inventario di artefatti (personaggio, gruppo, negoziante) e può rifiutarne alcuni (`tratta`).

### Gli oggetti leggendari: `artefatti.leggendari`

Sono letti da `leggendari.txt`, una riga per oggetto con campi `CHIAVE=valore` separati da `;` (tipo, nome, chiave, descrizione, livello, costo, peso, danni, modificatori `MOD=`, incantamenti, battute della `LEGGENDA=`, l'eventuale `SET=`). Il formato è spiegato in testa al file.

- `OggettoLeggendario` è un leggendario già pronto: `costruisci(livello)` fa partire dal livello passato, `alLivello(artefatto, livello)` riallinea uno già esistente.
- `SetLeggendario` e `RegoleSetLeggendari`: un personaggio che indossa tutti i pezzi di un set ha i bonus di ognuno moltiplicati per il moltiplicatore del set (solo i modificatori positivi e gli incantamenti scritti sull'artefatto, non i malus né quel che l'artefatto dà di suo). Il moltiplicatore si applica nei conti, mai sull'artefatto: togliendo un pezzo i bonus tornano quelli di prima.
- `LibroTecnico` è un libro con attributi a caso ma nome e immagine propri, riconosciuto dopo il salvataggio per aspetto e nome.
- `CatalogoLeggendari` li raccoglie tutti per chiave. Un leggendario è un oggetto e basta: chi lo custodisce o lo racconta lo decidono le missioni.
- `ProduttoreDiArtefattiCasuale` dà i testi a caso (le battute della leggenda, i nomi degli aggiornamenti) dalle grammatiche `leggendari.txt` e `artefatti2.txt`; `ModificatoreDelTesto` legge un `MOD=` da una riga di testo.

La `SpadaNormalmenteNascosta` è un caso a parte: un'arma a caso che il gruppo ottiene solo con un intermezzo e che, a differenza di ogni altro artefatto, segue un livello che cambia (si ricalcola da quello del mondo ogni volta che lo si chiede, per mezzo di una funzione che le dà chi la usa).

## Osservazioni

- Il dettaglio delle regole (fusione, gradi, prezzi, set) e il piano di lavoro sono in [`artefatti_e_incantamenti.md`](artefatti_e_incantamenti.md).
- Aggiungere un nuovo tipo di artefatto tocca più moduli (`TipoArtefatto` e il suo file dei testi qui, `TipoBottino` e `FabbricaOggetti` nel bottino, le regole di equipaggiamento nei personaggi, le immagini nella UI): la ricetta è in `CLAUDE.md`.
- `GeneratoreArtefattiTabelle` usa la grammatica per i nomi con una certa probabilità (`ARTEFATTO_PROBABILITA_DA_GRAMMATICA`) e le tabelle interne per il resto; il javadoc dell'interfaccia dice ancora che la grammatica «arriverà poi».
