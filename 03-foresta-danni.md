# 03-foresta-danni

I **tipi di danno** e le loro **reazioni con gli stati alterati**: `TipoDanno` e `CategoriaDanno` dicono quali danni esistono e quali stati possono causare; `TipoInterazioneConEffettiDiStato` elenca le reazioni che nascono quando un danno colpisce chi ha già un certo stato (il fulmine su un bagnato, il fuoco su uno scudo di ghiaccio…).

Il modulo esiste per tenere in un posto solo la *tabella* di queste regole, che serve a più parti: il calcolo del danno (che le applica), le tattiche (che guardano oltre il colpo di adesso, per scegliere l'azione) e la UI (che ne mostra il testo). Sono dati dichiarati, non logica: il codice che li usa sta più in alto.

## Dipendenze

**Dichiarate nel pom:** `01-foresta-testi`, `02-foresta-effettidistato`.

**Lo dichiarano:** `04-foresta-artefatti`, `05-foresta-personaggi`.

Le dipendenze sono transitive: un modulo vede anche quello che vedono i moduli da cui dipende, quindi il compilatore garantisce la separazione fra i moduli che non si raggiungono.

Dipende da `02-foresta-effettidistato` perché ogni tipo di danno elenca gli stati che può causare e ogni reazione ha per precondizioni e per esiti degli stati; da `01-foresta-testi` per i nomi e le descrizioni. Le debolezze e le resistenze delle classi di personaggio ai tipi di danno non sono qui: stanno in `05-foresta-personaggi`, che dipende da questo modulo.

## Funzionamento

### `TipoDanno` e `CategoriaDanno`

Diciotto tipi, in tre famiglie (`CategoriaDanno`):

| Categoria | Tipi |
| :--- | :--- |
| `FISICO` | contundente, perforante, tagliente |
| `ELEMENTALE` | aria, acqua, terra, fuoco, fulmine, gelo, acido, sonico, veleno |
| `MAGICO` | necrotico, sacro, psichico, arcano, vuoto, maledizione |

Ogni tipo porta gli **stati che può causare** (`getEffetti`, una copia perché l'array è di un'enum): il tagliente il sanguinamento, il fuoco il bruciato, il gelo il rallentato e il congelato, il fulmine lo stordito, e così via; `hasEffettiDiStato()` dice se ne ha almeno uno. Il nome sta in `tipo_danno_<lingua>.properties`. Il javadoc di ogni costante descrive come ci si aspetta che si comporti.

### `TipoInterazioneConEffettiDiStato`

Trentatré **reazioni**. Ognuna è dichiarata con cinque dati:

1. le **precondizioni**: gli stati di cui il difensore deve averne almeno uno;
2. gli **innescanti**: i tipi di danno che la fanno scattare;
3. il **moltiplicatore** del danno (1,5 per l'elettrocuzione su un bagnato, 0,5 per la vaporizzazione del fuoco su un bagnato…);
4. gli **stati rimossi** al difensore;
5. gli **stati applicati** al difensore.

Esempi: `CONGELAMENTO` (gelo su un bagnato: perde `BAGNATO`, riceve `CONGELATO`), `ESTINZIONE` (acqua su un bruciato: perde `BRUCIATO` e `BAGNATO`), `ESPLOSIONE_DI_GAS` (veleno su un bruciato). **L'ordine di dichiarazione è l'ordine in cui le reazioni si applicano**, per stato; il calcolo del danno le prova in sequenza (`CalcolatoreCombattimento.interazioniAttive`). Sono scritte qui, e non solo nel calcolo del danno, perché le usano sia il calcolo vero sia le tattiche, che guardano gli stati causabili (`CalcolatoreCombattimento.statiCausabili`).

Due metodi rendono particolari alcune reazioni:

- `isCriticoAutomatico()`: il `COLPO_DI_GRAZIA` su chi è a terra o stordito e la `FRANTUMAZIONE_DEL_GHIACCIO` fanno un critico automatico (×2, che non si somma a un critico vero);
- `getDannoAdArea()`: l'`ESPLOSIONE_DI_GAS` (fuoco), il `RIGETTO` (sonico) e l'`ELETTROCUZIONE` (fulmine) investono anche i vicini del bersaglio nella fila del suo gruppo; quanto e come lo dice il calcolo del danno, e `getTestoAdArea()` dà la frase («L'esplosione investe…»).

Durate e danni periodici degli stati messi sono quelli normali, salvo eccezioni che il calcolo conosce (l'`ALIMENTAZIONE_FIAMMA` allunga il bruciato che c'è già, la `TOSSICITA_SETTICA` raddoppia il danno del veleno…). Il quadro completo delle reazioni è in [`interazioni_effetti_di_stato.md`](interazioni_effetti_di_stato.md); le debolezze di classe in [`debolezze_e_resistenze.md`](debolezze_e_resistenze.md).

## Osservazioni

- Le reazioni sono tutte un'unica enum lunga (più di 400 righe): aggiungerne una vuol dire una voce qui, un testo nel file `tipo_interazione_con_effetti_di_stato_<lingua>.properties` (e `AREA` se ha un danno ad area) e, se serve, un'eccezione nel calcolo del danno. `TestiDelleEnumTest` controlla che i testi ci siano tutti.
- Il pacchetto `danni.tipi` contiene solo un `package-info`.
