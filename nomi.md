# Nomi dei personaggi

Come si danno i nomi ai personaggi non combattenti del mondo e dove si usano. Le liste di nomi stanno in `nomi.txt`
(condiviso da locande, fiabe e missioni); i nomi dei personaggi del mondo si pescano alla creazione della partita e si
tengono in un registro, che si salva con la partita.

## 1. Le liste: `nomi.txt`

Risorsa `foresta-testi/src/main/resources/com/threeamigos/foresta/testi/nomi.txt`. `locande.txt` e `missioni.txt` la includono con
`#include_static` (una sezione condivisa: un nome uscito in una grammatica non esce più in nessun'altra), `fiabe.txt` con
`#include` normale (le fiabe si azzerano a ogni racconto: hanno una copia tutta loro). Vedi [`GrammarBean.md`](GrammarBean.md)
§4.7 (one-shot), §4.11 e §4.12.

| Lista | Voci | Note |
| :--- | ---: | :--- |
| `NOME_MASCHILE$` | 98 | uno per riga, in ordine alfabetico |
| `NOME_FEMMINILE$` | 67 | erano i nomi delle principesse delle fiabe |
| `COGNOME$` | 54 | |
| `ATTRIBUTO_POSITIVO$`, `ATTRIBUTO_NEGATIVO$` | 35 + 35 | con l'articolo ("il Raffinato"), ordinati ignorandolo |
| `TITOLO$` | 35 | "il Barone"… |
| `DEGLI_LOCANDE$` | 35 | con le note tra parentesi che descrivono il locandiere: restano così |
| `DEGLI_ALTISONANTE$` | 35 | senza note: "dei Signori della Luna", "di Altomonte" |
| `NOME_SACERDOTE$`, `NOME_SACERDOTESSA$` | 5 + 7 | "Fra' …", "Suor …": i due sacerdoti da reclutare e quelli che la benedizione manda nel gruppo (`ProduttoreDiMissioniCasuale.nomeSacerdote`, `nomeSacerdotessa`) |
| `NOME_COMPARSA` | 20 | non one-shot: si ripete |

Le radici per i personaggi del mondo: `NOME_MASTRO` ("Mastro [NOME_MASCHILE]"), `NOME_CAPITANO` (`[NOME_MASCHILE]`),
`NOME_ALTISONANTE` ("[NOME_MASCHILE] [DEGLI_ALTISONANTE]"), `NOME_MOGLIE` (`[NOME_FEMMINILE]`) e `NOME_COMPARSA`; il bardo usa
`NOME_MASCHILE`. Ci sono poi le liste con un sapore proprio, che restano dov'erano (campioni e briganti con i loro epiteti,
goblin, soprannomi e titoli delle fiabe). Le produzioni che **combinano** le liste restano nelle grammatiche, perché codice e
testi le chiamano per nome: `NOME_CASUALE` e `ATTRIBUTO` (locande), `NOME_PROTAGONISTA`, `COGNOME_PROTAGONISTA` e
`NOME_PRINCIPESSA` (fiabe), `NOME_OSTAGGIO`, `NOME_PELLEGRINO`, `NOME_MAGO`, `NOME_CAMPIONE`, `NOME_BRIGANTE`… (missioni).

`GrammarBean.resetStaticProductions()` azzera **tutte** le sezioni statiche insieme, e con esse le one-shot locali delle
grammatiche che le includono (le locande). Non c'è ricarica automatica: un pool esaurito pota a cascata le alternative che lo
usano, e `produce()` finisce per lanciare `IllegalArgumentException`.

## 2. Chi ha un nome

| Chi | Quanti | Nome | Provenienza |
| :--- | :--- | :--- | :--- |
| Borgomastro | uno per città | altisonante ("… degli …"), un `BORGOMASTRO` | sì |
| Capitano delle guardie | uno per città | solo maschile | sì |
| Alchimista, armaiolo, venditore di pergamene, incantatore | uno per città | "Mastro" + nome maschile | sì |
| Bardo della locanda | uno, viaggia di locanda in locanda | solo nome | no |
| Moglie del bardo | una | solo nome, da `NOME_FEMMINILE` | no |
| Levatrice (e altre comparse) | a richiesta, una `DONNA` | da `NOME_COMPARSA` | no |
| Locandiere | uno per locanda | quello della locanda (vedi §4) | no |
| Campioni delle sfide | quattro | nome ed epiteto scelti dalla missione | no |

La **provenienza** fa parte del nome completo: "Mastro Ferruccio da Ruuna", per distinguere per esempio due armaioli. I nomi
fissi dei compagni da reclutare (`RegistroPersonaggi.reimposta()`) restano scritti nel codice, tranne quelli del sacerdote e della
sacerdotessa, che vengono da `NOME_SACERDOTE` e `NOME_SACERDOTESSA`.

## 3. Generazione

In `Foresta.reimposta()`, dopo `resetProduzioni()`, in quest'ordine e **senza altri reset in mezzo**:

1. i 21 locandieri (le 4 locande delle città più `max(compagni disponibili, (DIMENSIONE_X + DIMENSIONE_Y) / 4)` = 17), con
   `ProduttoreDiLocandeCasuale.getDatiLocanda`, che pesca tutto insieme: **non va richiamato a partita avviata** (vedi il suo
   javadoc);
2. per ogni città borgomastro, capitano e i quattro artigiani (`RegistroPersonaggi.creaIResidentiDellaCitta`);
3. il bardo e la moglie (`creaIlBardoESuaMoglie`).

Servono 21 + 24 + 1 = 46 nomi maschili su 98, e i pool delle altre liste (35) coprono i 21 locandieri: così nessun nome del mondo
si ripete.

Durante la partita ostaggi, pellegrini e maghi delle missioni pescano dallo stesso `NOME_MASCHILE`. Quando finisce,
`ProduttoreDiMissioniCasuale.produciDaMissioni` (usata da `nomeOstaggio`, `nomePellegrino`, `nomeDiPersonaggioNoto` e
`rigaDiMissioni`) vede che `canProduce` è falso, azzera le sezioni statiche e riparte. Dopo un reset un ostaggio può avere il
nome di un residente: è accettato.

## 4. Il registro dei residenti

- `tipi.PersonaggioNoto`: l'enumerato dei personaggi del mondo, con classe, ambito (`GLOBALE`, `PER_CITTA`, `PER_NEGOZIO`,
  `PER_LOCANDA`, `COMPARSA`, `PER_SFIDA`), produzione di nomi e se porta la provenienza. Il borgomastro ha la classe sua, `BORGOMASTRO` (classe `Borgomastro`, con l'immagine sua; gli attributi sono
  quelli dei non giocanti); la levatrice è il tipo generico `DONNA` (classe `Donna`, con l'immagine e il verso della moglie del bardo). Il locandiere è nell'enumerato per completezza, ma il suo nome sta nella locanda
  (`Locanda.LOCANDA_NOME_LOCANDIERE`), non nel registro. Chi apre una bottega nella Foresta (`ALCHIMISTA_DELLA_FORESTA`, `ARMAIOLO_DELLA_FORESTA`, `VENDITORE_DI_PERGAMENE_DELLA_FORESTA`, `INCANTATORE_DELLA_FORESTA`, ambito `PER_NEGOZIO`, vedi `IlNegozio`) nasce alla prima richiesta, con la città da cui parte la missione per chiave e provenienza (`RegistroPersonaggi.getResidente(noto, citta)`); la sua classe dà l'immagine, come per gli artigiani delle città.
- `modellodati.ResidenteMD` (nome, provenienza, classe, sconfitto), in una mappa `chiave → ResidenteMD` di
  `RegistroPersonaggiMD`, salvata subito dopo i personaggi in locazione.
- `RegistroPersonaggi`: `getResidente(...)` (lo crea al volo se manca), `getNomeCompleto` ("Mastro Ferruccio da Ruuna"),
  `costruisciPersonaggio(residente[, livello])`, `getNomeDelPersonaggioDiCitta(mestiere)` (per la UI, via `VistaPartita`),
  `registraCampione`, `segnaCampioneSconfitto` e `getCampioni`.
- I **campioni** delle sfide sono già nel registro: `LaSfidaDeiCampioni` li registra con nome ed epiteto quando li crea,
  `IlCampione` li segna sconfitti, e restano anche dopo. Per ora nessuna schermata li mostra.

## 5. Dove si usano

- **`NonSparateSulPianista`** scorta il `BardoLocanda` costruito dal registro, con il suo nome, come ospite **non
  vulnerabile** (è così ubriaco che i mostri non si curano di lui: non viene attaccato e non può morire); la ricompensa la
  paga la moglie, nominata nel testo.
- **Testi delle missioni**: il mandante che ha un nome nel registro (il capitano delle guardie, l'alchimista, l'armaiolo, il
  venditore di pergamene, l'incantatore, il borgomastro, la levatrice) si scrive con il nome fra virgole: "l'armaiolo di
  Nyena, Mastro Ferruccio, ti ha chiesto…". Lo fanno `IncaricoInCitta.mandante`, `mandanteDiCitta` e le varianti senza la
  virgola finale (per quando segue un segno di punteggiatura), in tutte le missioni che lo estendono; `IlCorriere` lo fa per
  mittente e destinatario, `Mandante.getApertura(nome)` per la presentazione del capitano, `LaLeggendaDellArmaiolo` per il
  narratore. Nei testi basta il nome proprio, perché la città è già nella frase. Restano senza nome i mandanti che non sono
  nel registro (il locandiere, il sacerdote, il mugnaio…).
- **Schermate dei negozi** (armaiolo, venditore di pergamene, alchimista, incantatore): in cima alla colonna centrale c'è il
  nome completo, centrato e spezzato su più righe se non sta fra le due cornici, e sotto il mestiere in carattere piccolo
  (`disegnaIntestazioneCentrale` in `DisplayableCanvasScambiatore`); l'altezza dell'intestazione sposta il fumetto e la riga
  delle monete.
- **Mandanti senza personaggio proprio**: sono un `Uomo` o una `Donna` (`AspettoDelMandante`).

## 6. Test

- `NomiDeiPersonaggiDelMondoTest`: personale di ogni città con provenienza e "Mastro", bardo e moglie senza provenienza, nomi
  tutti diversi in 40 mondi con semi diversi e diversi da quelli dei locandieri, il bardo della missione è quello della
  locanda, la levatrice compare alla prima richiesta, campioni nel registro, salvataggio e rilettura (del registro e
  dell'intera partita) e una nuova partita che li dimentica.
- `NomiDelleMissioniTest`: nomi vari, e il reset quando i nomi finiscono (ostaggi e maghi, 500 pescate).
- `IntestazioneDelNegozianteTest` (righe fra le cornici, altezza, fumetto, caratteri nei font Doomdark), `MandanteTest`, e gli
  scenari delle missioni con i nomi nei testi (`ScenarioRichiesteDiMaterialiTest`, `ScenarioTrofeiECartografoTest`,
  `ScenarioCorriereTest`, `ScenarioNonSparateSulPianistaTest`…).

## 7. Osservazioni

- Un solo `NOME_MASTRO` per quattro mestieri consuma quattro nomi per città: è voluto, sono persone diverse.
- Le sezioni statiche sono stato globale: i test che creano una partita devono azzerarle; e la sequenza dei dadi cambia con chi
  pesca, quindi i test con seme fisso vanno riverificati a ogni modifica ai nomi (è già successo con `BugMappaReimpostataTest`
  e `BugCacheMappaGeneraleTest`, che ora non dipendono dal seme).
- Le scene (`ScenaInCitta` sta in `intermezzi`, che non importa il motore) disegnano il personaggio ma non ne scrivono il nome.
- Nelle schermate dei negozi anche un nome corto può andare su due righe: con la finestra a 1024 px "Mastro Pippo da Ruuna" ne
  occupa due.

## 8. Da fare / idee aperte

- Un'immagine propria per la levatrice (oggi usa quella della moglie del bardo).
- La levatrice e le altre comparse nelle scene: il nome c'è (e il mandante "la levatrice" lo porta nei testi), ma nessuna scena
  la chiede.
- Mostrare al giocatore i campioni incontrati: i dati ci sono (nome, classe, sconfitto), manca un punto della UI che li elenchi
  (la schermata delle statistiche o quella dei trofei).
- Altre missioni di scorta ("salva l'alchimista", "salva il locandiere"…; `IlViaggioDellaMoglie` scorta già la moglie del bardo): il personaggio si costruisce con
  `costruisciPersonaggio`; per il locandiere il nome va letto dalla locanda (vedi [`todo.md`](todo.md)).
