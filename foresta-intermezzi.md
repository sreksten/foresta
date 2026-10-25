# foresta-intermezzi

Gli **intermezzi**: le brevi scene a fumetti (sfondi, personaggi che entrano e si muovono, dialoghi) che il gioco mostra a tutto schermo in certi momenti — l'introduzione, l'ingresso in un negozio o in una locanda, la notte, l'accampamento, il ritrovamento di un libro tecnico. Il modulo contiene il linguaggio con cui si descrivono (pagine, elementi, tappe, battute), il registro che decide quale intermezzo scatta e ricorda quali sono già scattati, gli strumenti per comporre le scene e gli intermezzi che **non hanno bisogno delle missioni**.

È un modulo a sé perché gli intermezzi sono *dati e regole di innesco*: la UI sa disegnarli senza conoscere il motore, e le missioni possono darne di propri senza che gli intermezzi sappiano che le missioni esistono. Per questo l'ordine delle dipendenze è «le missioni danno i loro intermezzi come `FonteDiIntermezzi`, il motore registra nella fabbrica gli epiloghi», e non il contrario.

## Dipendenze

**Dichiarate nel pom:** `foresta-statistiche`, `foresta-personaggi`, `foresta-mappa`.

**Lo dichiarano:** `foresta-bottino`, `foresta-modellodati`.

Le dipendenze sono transitive: un modulo vede anche quello che vedono i moduli da cui dipende, quindi il compilatore garantisce la separazione fra i moduli che non si raggiungono.

- `foresta-statistiche`: alcune scene dipendono dall'ora del giorno e dal livello del mondo (i negozi di notte, le locande).
- `foresta-personaggi`: le scene mostrano le classi del gruppo e dei negozianti.
- `foresta-mappa`: gli ingressi nelle locande dipendono dalla locazione (`TipoLocazione`) e dalla posizione.

Non conosce le missioni né il motore.

## Funzionamento

### Il linguaggio delle scene

Si descrive con un piccolo linguaggio a catena, tutto immutabile:

- `PaginaIntermezzo`: una pagina composta a strati — lo sfondo (un'immagine a tutto schermo, o in sua assenza l'ombra del drago), gli **elementi** nell'ordine in cui sono aggiunti (i successivi sopra), il testo in alto, i fumetti delle battute. Tutto è facoltativo. Le pagine si costruiscono **al momento in cui l'intermezzo scatta**, quindi personaggi e dialoghi dipendono dalla partita (`.conSfondo(...)`, `.conElemento(...)`, `.conBattuta(...)`, `.perSecondi(...)`).
- `ElementoIntermezzo`: un'immagine in una pagina (un personaggio, un oggetto, un elemento di sfondo animato). Parte da uno stato iniziale (posizione del centro in frazioni dello schermo, scala, opacità) e percorre una sequenza di **`Tappa`** — un tratto d'animazione in cui l'elemento passa in modo lineare allo stato della tappa — ripetuta come dice `TipoRipetizione`. `Verso` dice da che parte guarda; `StatoElemento` com'è in un certo istante.
- `ImmagineIntermezzo`: un riferimento a un'immagine **senza dipendere dalla UI** (il motore dice quale, la UI la carica): l'immagine di una classe di personaggio, l'illustrazione di una locazione, una risorsa, uno *sprite sheet* a fotogrammi, o un'`Animazione` fornita da una classe della UI.
- `BattutaIntermezzo` e `BattutaProgrammata`: una battuta di dialogo, mostrata come fumetto che punta alla bocca dell'elemento che parla e lo segue se si muove.
- `TipoStiramento` dice come lo sfondo si adatta all'area disponibile.

### Le scene già pronte

Per non riscrivere ogni volta ingressi e dialoghi ci sono gli strumenti di scena:

- `ScenaNegozio`: il negoziante è al suo posto, il gruppo entra camminando (capo in testa, gli altri in fila) e, appena arrivato, parlano negoziante e capo. Si usa con `NegozioInScena.X.entraIlGruppo().parlaIlNegoziante(..).parlaIlCapo(..).getPagine()`.
- `ScenaInCitta`: una strada di città, con il mandante di una missione (un uomo, una donna, il locandiere, l'armaiolo, l'alchimista o il capitano delle guardie). Se la missione ha portato fin qui qualcuno che è ancora ospite del gruppo, la scena lo mostra.
- `ScenaInLocanda` e `ScenaFraCompagni` (il gruppo che parla fra sé, fuori dalle città). `Locande` dice se il gruppo sta davvero entrando in una locanda e quale (le chiavi `LOCANDA_*` delle caselle).

### Quando scatta un intermezzo

`Intermezzo` è un'interfaccia: `getId`, `deveScattare(momento)` (l'innesco), `getPagine()`, e per i casi particolari `isRipetibile` (scatta ogni volta che l'innesco lo chiede), `isDiRipiego` (scatta solo se non scatta nessun altro), `getSecondiPerPagina` (le pagine avanzano al click e, di norma, da sole dopo 8 secondi) e `segnaScattato`. A differenza di una missione non ha progressi: una volta mostrato non si ripete (salvo i ripetibili).

- `MomentoIntermezzo` elenca i punti in cui l'automa controlla: inizio gioco, inizio locazione, locazione completata, ingresso in locanda in città, ingresso da alchimista, armaiolo, venditore di pergamene o incantatore.
- `TipoIntermezzo` elenca gli intermezzi fissi (introduttivo, fine della prima locazione, locanda prima visita e visite successive, Urlo della Banshee, spada nascosta, riprogrammazione, libro tecnico, accampamento, eremita rompiscatole, gli ingressi nei negozi e le notti nei negozi e nella locanda). **L'ordine dell'enum è la priorità** con cui `RegistroIntermezzi` sceglie quale scatta per primo in un momento.
- **`RegistroIntermezzi`** è la facciata su `IntermezziMD`: sceglie il prossimo intermezzo da mostrare e ricorda gli identificativi di quelli già scattati (`IntermezziMD`, che `ModelloDati` salva; il dato lo prende da chi lo custodisce, `setFornitoreDiDati`).
- **`FabbricaIntermezzi`** costruisce l'intermezzo di ogni `TipoIntermezzo`. Quelli di questo modulo si registrano da soli; quelli che hanno bisogno delle missioni li registra chi li conosce con `FabbricaIntermezzi.registra`.
- **`FonteDiIntermezzi`**: chi ha intermezzi suoi oltre a quelli fissi — le missioni, con gli intermezzi dei loro passi. Gli intermezzi non sanno che esistono: è chi sta sopra a dirlo al registro (`RegistroIntermezzi.aggiungiFonte`).

Gli intermezzi concreti qui sono quelli che non hanno bisogno delle missioni: `IntermezzoIntroduttivo`, `IntermezzoFinePrimaLocazione`, `IntermezzoBenvenutoNegozio`, `IntermezzoOroscopo` (l'alchimista), `IntermezzoLocandaPrimaVisita` e `…VisitaSuccessiva`, `IntermezzoNonChiudeteMai` (chi entra a ore impossibili), `IntermezzoSpadaNascosta`, `IntermezzoLibroTecnico`, `IntermezzoRiprogrammazione`, `IntermezzoAccampamento`.

### I contratti per la UI

`NotificaPaginaIntermezzo` (una pagina da mostrare a tutto schermo finché non arriva la successiva), `InternoFumettoSuccessivo` (salta al fumetto successivo) e `InternoCreazioneFumetto` (per mettere a punto come si spezza il testo delle battute). `Animazione` nomina le animazioni fornite dalla UI; l'associazione con la classe che le disegna sta in `ui.AnimazioniIntermezzo`.

## Osservazioni

- Per aggiungere un intermezzo fisso: una costante in `TipoIntermezzo`, una classe che implementa `Intermezzo`, la registrazione in `FabbricaIntermezzi`, e (se serve) il momento e la sua priorità; la ricetta completa, con l'esempio di `IntermezzoLibroTecnico`, è in `CLAUDE.md`. Un intermezzo che ricorda un fatto avvenuto altrove tiene lo stato `static` e lo azzera in `RiletturaPartita` e `CostruttoreDellaForesta`.
- Il quadro è in [`intermezzi.md`](intermezzi.md).
