# foresta-attributi

Gli **attributi** dei personaggi e delle cose che li modificano: l'elenco delle statistiche (`TipoAttributo`) con le loro categorie, le formule con cui gli attributi secondari derivano dai primari (`CalcolatoreAttributiSecondari`) e il dato del **modificatore di attributo** (`ModificatoreAttributoMD`) con i tipi che lo descrivono e i testi con cui lo si racconta.

È un modulo a sé perché gli attributi sono il linguaggio comune di personaggi, artefatti e incantamenti: un artefatto «+3 Forza» e un personaggio con la Forza a 17 devono parlare delle stesse cose, ma gli artefatti non devono sapere che esistono i personaggi, né i personaggi come sono fatti gli artefatti. Qui c'è *solo* l'idea di attributo e di modifica.

## Dipendenze

**Dichiarate nel pom:** `foresta-testi`, `foresta-serializzazione`.

**Lo dichiarano:** `foresta-artefatti`, `foresta-personaggi`.

Le dipendenze sono transitive: un modulo vede anche quello che vedono i moduli da cui dipende, quindi il compilatore garantisce la separazione fra i moduli che non si raggiungono.

Dipende da `foresta-testi` perché i nomi e le descrizioni degli attributi stanno in `tipo_attributo_<lingua>.properties` (letti con `Testi`), e da `foresta-serializzazione` perché `ModificatoreAttributoMD` sa scriversi e rileggersi nei salvataggi. Non sa niente di chi possiede gli attributi.

## Funzionamento

### `TipoAttributo` e `CategoriaAttributo`

`TipoAttributo` elenca le statistiche, divise in tre categorie (`CategoriaAttributo`):

| Categoria | Che cosa sono | Attributi |
| :--- | :--- | :--- |
| `PRIMARIO` | le caratteristiche di base | Forza, Destrezza, Costituzione, Intelligenza, Saggezza, Carisma, Fortuna |
| `SECONDARIO` | derivano dai primari o li completano | critico, precisione, velocità, furtività, parata, resistenza magica, percezione, soggezione, furia, coraggio, valore, contrattazione, potere magico, carico massimo, numero di bersagli, rigenerazione di salute e di magia |
| `RISORSA_DINAMICA` | cambiano di continuo durante la partita | livello, punti abilità, punti esperienza, salute e salute massima, magia e magia massima, stanchezza, tempo |

Il javadoc di ogni primario spiega come pesa in attacco e in difesa. I testi (`NOME`, `DESCRIZIONE`) non stanno nell'enum ma nel file dei testi accanto (`COSTANTE.VOCE`, per esempio `FORZA.NOME`), così il gioco si può tradurre.

### `CalcolatoreAttributiSecondari`: le formule

Ogni secondario si ricava da **due primari**, uno che pesa di più e uno di meno, e dal **moltiplicatore della classe** del personaggio. Le formule sommano le **radici quadrate** dei due primari, ciascuna per il suo peso: raddoppiare un primario non raddoppia il secondario (rendimenti decrescenti). Il valore grezzo si moltiplica per il moltiplicatore e si tronca a intero.

| Secondario | Primari (principale, secondario) |
| :--- | :--- |
| `critico` (mai sopra 100) | Destrezza, Fortuna |
| `precisione` | Destrezza, Intelligenza |
| `velocita` | Destrezza, Fortuna |
| `furtivita` | Destrezza, Fortuna |
| `parata` | Forza, Destrezza |
| `resistenzaMagica` | Saggezza, Intelligenza |
| `percezione` | Saggezza, Destrezza |
| `soggezione` | Carisma, Forza |
| `furia` | Forza, Fortuna |
| `coraggio` | Carisma, Costituzione |
| `contrattazione` | Carisma, Fortuna |
| `valore` | Saggezza, Costituzione |
| `numeroBersagli` | principale e secondario a scelta di chi chiama |

Il calcolatore **non legge né scrive niente sul personaggio**: riceve i valori *attuali* dei primari (già modificati da artefatti e incantamenti) e il moltiplicatore, e restituisce il numero. Sta a `PersonaggioBase` (in `foresta-personaggi`) leggere i primari, scegliere il moltiplicatore della classe e salvare il risultato.

### Il modificatore di attributo

`ModificatoreAttributoMD` (nel pacchetto `modellodati` di questo modulo) è il dato di una modifica: **quale** attributo cambia, **come** (`TipoModificatore`), **di quanto**, **per quanto tempo** (`UnitaDurataModificatore`) e **su chi** (`BersaglioModificatore`), più una nota in parole e il flag `rimovibile`. È immutabile: chi lo fa scalare ne sostituisce la copia (`conDurata`, `conQuantita`, `suPortatore`). Sa scriversi in un campo (`comeCampo`) e rileggersi (`daCampo`).

| Tipo | Valori |
| :--- | :--- |
| `TipoModificatore` | `VARIAZIONE_FISSA` (un numero di punti), `VARIAZIONE_PERCENTUALE`, `QUANTITA_ASSOLUTA` (porta l'attributo a un valore) |
| `UnitaDurataModificatore` | `PERMANENTE` (durata 0), `TURNI` (scade con i turni di combattimento), `LOCAZIONI` (scade con le locazioni) |
| `BersaglioModificatore` | `PORTATORE` (chi porta l'artefatto), `BERSAGLIO` (chi viene colpito dall'arma: vale solo per le armi) |

La durata dev'essere coerente con l'unità: un permanente ha durata 0, uno a tempo almeno 1 (altrimenti `IllegalArgumentException`). Su un artefatto la durata è quella con cui il modificatore sarà applicato; su un personaggio è il tempo che resta. `rimovibile` è falso per l'ignominia della fuga, che nemmeno l'Alba sacra può togliere.

`ModificatoreAttivo` (pacchetto `modificatori`) accoppia un modificatore alla sua **fonte** (il nome dell'artefatto o la nota di un modificatore proprio, come una benedizione): serve alla UI per dire da dove viene un valore. `TestoDeiModificatori` racconta il modificatore al giocatore («Coraggio -20%», «Fortuna porta a 0», con la durata); usa sempre cifre latine, qualunque sia la lingua, perché il font bitmap non sa disegnare le altre.

Come i modificatori si sommano e si applicano agli attributi lo decide `PersonaggioBase`; il quadro completo, con le armi che li mettono sul bersaglio, è in [`modificatori_temporanei.md`](modificatori_temporanei.md).

## Osservazioni

- I pacchetti `attributi.tipi` e `modificatori.tipi` contengono solo un `package-info`: i tipi veri stanno nel pacchetto `com.threeamigos.foresta.tipi`, come per tutti i moduli.
- Le formule dei secondari sono costanti dentro i metodi: per ritarare il bilanciamento di un secondario si tocca qui, non nelle costanti dei personaggi (che hanno invece i moltiplicatori di classe).
