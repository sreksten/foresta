# 01-foresta-testi

Tutti i testi del gioco che **non stanno nel codice**: i file di testi per lingua (i nomi e le descrizioni delle enum, le note, i comandi), le regole dell'italiano per comporre frasi (articoli, preposizioni articolate, numeri in lettere), il motore di grammatiche `GrammarBean` che produce testi a caso e i produttori che lo usano per fiabe, oroscopi, luoghi, sfide, missioni e locande.

Sta in basso perché ogni modulo che ha un'enum con nomi da mostrare o una frase da comporre ha bisogno di queste funzioni, e perché è qui che si decide **come il gioco potrà essere tradotto**: un testo che sta in un file `_<lingua>.properties` si traduce copiando il file, non toccando il codice.

## Dipendenze

**Dichiarate nel pom:** `00-foresta-strumenti`.

**Lo dichiarano:** `02-foresta-comandi`, `02-foresta-attributi`, `02-foresta-effettidistato`, `03-foresta-danni`, `04-foresta-artefatti`, `02-foresta-mappa`, `05-foresta-personaggi`.

Le dipendenze sono transitive: un modulo vede anche quello che vedono i moduli da cui dipende, quindi il compilatore garantisce la separazione fra i moduli che non si raggiungono.

Dipende da `00-foresta-strumenti` per il caso (`GrammarBean` sceglie le alternative con `Dado`, e quindi segue il seme e i trucchi) e per il log. Non conosce il dominio: i produttori generano testo, non sanno chi lo mostrerà.

## Funzionamento

### `Testi`: i file per lingua

I testi stanno in file `<nome>_<lingua>.properties` (UTF-8: il `ResourceBundle` di Java 8 li leggerebbe in ISO-8859-1) e si leggono una volta sola, poi restano in cache. Due modi d'uso:

- `Testi.get(file, chiave)`: un file nel pacchetto `testi` (qualunque modulo può metterci il suo), con una chiave a piacere. Così funzionano `note_personaggi`, `rifiuti_equipaggiamento`, `caratteristica_di_classe`, `comandi`.
- `Testi.testo(costante, voce)`: i testi di una costante di enum, per esempio `testo(TipoAttributo.FORZA, "NOME")`. **Il file sta accanto all'enum**, nel suo modulo, e si chiama come lei in minuscolo con i trattini bassi (`TipoAttributo` → `tipo_attributo_it.properties`); la chiave è `COSTANTE.VOCE`. Un'enum non ha nel codice nessun testo da tradurre. `testoOCostante` restituisce il nome della costante se il testo manca, così un buco si vede e non rompe niente; `nomeDelFile(classe)` calcola il nome del file.

La lingua è l'italiano (`LINGUA_PREDEFINITA`) finché la piattaforma non sceglie un'altra con `impostaLingua`; **un testo che manca nella lingua scelta si cerca in italiano**, quindi una traduzione incompleta non lascia buchi. `TestiDelleEnumTest` (in `04-foresta-artefatti`) controlla che ogni costante abbia tutte le voci e che nei file non ce ne siano di orfane.

### Le regole dell'italiano: `Articoli` e `Numeri`

- `Articoli` ha gli articoli determinativi e indeterminativi (`il `, `lo `, `la `, `l'`, `i `, `gli `, `le `, `un `, `uno `…) già con lo spazio o l'apostrofo finale, le preposizioni articolate, e due funzioni: fondere una preposizione con l'articolo di un nome e mettere la maiuscola iniziale. Sono testi italiani: la localizzazione completa verrà dopo.
- `Numeri` scrive in lettere i cardinali e gli ordinali (maschile e femminile: `getOrdinaleM/F`, validi per il giorno del calendario).

### `GrammarBean`: testi a caso da una grammatica

Legge un file di grammatica e lo usa per **produrre** testo casuale espandendo ricorsivamente i riferimenti. È la parte più grossa del modulo; il manuale completo è in [`GrammarBean.md`](GrammarBean.md). In breve, il formato:

- una riga alla colonna 0 dichiara una **produzione**; le righe rientrate sono le sue **alternative**, anche più d'una per riga separate da `|`;
- `[Nome]` richiama un'altra produzione; `{a|b|c}` è un gruppo di alternative in linea;
- `Nome$` è una produzione **monouso** (ogni alternativa esce una volta per ciclo), `[*Nome]` e `[!Nome]` fissano il valore globalmente o localmente, `[chiave=valore]` e `[#chiave]` assegnano e rileggono, `[Nome? | ripiego]` dà un valore predefinito;
- `[^N]` dà un peso a un'alternativa; i pesi crescono da soli con la ricchezza del sottoalbero (`SCALE_FACTOR`);
- `"…"` protegge un tratto letterale, `//` è un commento, e le sostituzioni finali (`preposizioni_articolate_pp.txt`) sistemano per esempio «a il» in «al».

`TipoModalitaProduzioneTesti` dice come scegliere fra le alternative (`PRIMO`, `ULTIMO` per i test, `CASUALE`). Il caso viene da `Dado`, quindi con un seme fisso una partita produce sempre gli stessi testi.

`CaricatoreDiGrammatiche` carica un file `.txt` (dalla cartella `testi` o da una risorsa indicata da chi ha grammatiche sue, come gli artefatti) e traduce gli errori in `GrammaticaNonValidaException`; `GrammaticaPigra` carica la grammatica solo al primo uso, così un errore è un'eccezione normale e non un guasto di inizializzazione di classe.

### I produttori

Ogni produttore legge le sue grammatiche e offre testi pronti:

| Produttore | Grammatica | Che cosa produce |
| :--- | :--- | :--- |
| `ProduttoreDiFiabeCasuale` | `fiabe.txt` | le fiabe che il locandiere racconta |
| `ProduttoreDiOroscopiCasuale` | `oroscopo.txt` | gli oroscopi dell'alchimista e il dialogo che ne nasce |
| `ProduttoreDiLuoghiCasuale` | `rovine.txt`, `templi.txt` | i nomi dei luoghi della foresta |
| `ProduttoreDiSfideCasuale` | `sfide.txt` | le frasi della sfida a carta, forbici e sasso |
| `ProduttoreDiMissioniCasuale` | `missioni.txt`, `nomi.txt`, `tappe.txt` | i testi delle missioni e i personaggi del mondo (vedi [`nomi.md`](nomi.md)); `rigaDiTappe(farsa)` dà una riga di missione a tappe (vedi [`missione_a_tappe.md`](missione_a_tappe.md)) da `tappe.txt`, che è a parte perché le sue produzioni a esaurimento si ripristinano a ogni riga |
| `ProduttoreDiLocandeCasuale` | `locande.txt` (e i nomi di `nomi.txt`) | i dati a caso delle locande del mondo: nome, locandiere, dialoghi |

`ProduttoriDiTesti` ne governa il ciclo di vita: `precarica()` li carica tutti subito (per scoprire all'avvio una grammatica sbagliata) e `resetProduzioni()` azzera le liste esauribili, cioè i nomi già estratti, quando comincia una partita nuova. I produttori degli artefatti stanno invece in `04-foresta-artefatti`, con le loro grammatiche.

## Osservazioni

- `Articoli` e i file `.txt` sono ancora scritti in italiano: portare il gioco in un'altra lingua vuol dire anche ripensare queste regole (vedi `assessment_portabilita.md`).
- `GrammarBean` è una classe molto grande (più di 2500 righe) e dalla sintassi ricca: ogni nuova funzione della grammatica va documentata nel manuale `GrammarBean.md`.
- I produttori tengono stato statico (cosa è già stato estratto): `resetProduzioni()` va chiamato a ogni partita nuova.
