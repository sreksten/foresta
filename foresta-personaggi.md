# foresta-personaggi

I **personaggi** e i **gruppi** di personaggi: chi sono, che cosa possiedono e che cosa sanno fare da soli. Contiene le classi giocabili (otto, ognuna al maschile e al femminile), i mostri, i non combattenti, la base comune `PersonaggioBase` con le regole di salute, magia, esperienza, modificatori, stati alterati ed equipaggiamento, i due gruppi (`GruppoGiocatore` e `GruppoAvversario`), i tipi che li descrivono, le costanti di bilanciamento, i dati che si salvano e i contratti che la UI usa per leggerli.

Sta in mezzo alla catena per una scelta di direzione: **i poteri e il combattimento dipendono dai personaggi, non il contrario**. Un personaggio sa chi è e che cosa ha; *come* lancia un incantesimo, canta o colpisce lo dicono i moduli sopra. E non sa dove sta il gruppo (lo sa la mappa), quanti soldi ha (lo sa la cassa) o che tattica segue (la tengono le tattiche): le informazioni che non gli appartengono stanno altrove, legate al suo `uuid`.

## Dipendenze

**Dichiarate nel pom:** `foresta-artefatti`, `foresta-eventi`, `foresta-danni`, `foresta-effettidistato`, `foresta-attributi`, `foresta-testi`, `foresta-strumenti`, `foresta-serializzazione`.

**Lo dichiarano:** `foresta-poteri`, `foresta-statistiche`, `foresta-intermezzi`, `foresta-bestiario`, `foresta-modellodati`, `foresta-motore-api`, `foresta-motore`.

Le dipendenze sono transitive: un modulo vede anche quello che vedono i moduli da cui dipende, quindi il compilatore garantisce la separazione fra i moduli che non si raggiungono.

- `foresta-artefatti`: un personaggio ha un inventario e un equipaggiamento di artefatti, e il gruppo li scambia (`ScambiatoreArtefatti`).
- `foresta-attributi`, `foresta-danni`, `foresta-effettidistato`: gli attributi e i loro modificatori, i tipi di danno con cui si calcolano debolezze e resistenze di classe, gli stati alterati in corso.
- `foresta-eventi`: i personaggi pubblicano notifiche e eventi interni (livello, ferite, morte, spostamenti di artefatti…).
- `foresta-testi`, `foresta-strumenti`, `foresta-serializzazione`: nomi e note per lingua, il caso per creare i personaggi, i salvataggi.

## Funzionamento

### Il personaggio

`Personaggio` è l'interfaccia che tutti rispettano; estende `VistaPersonaggio` (le letture, che è quel che ne vede la UI), `OggettoConArticoli` (i nomi con l'articolo giusto, per costruire le frasi) e `ScambiatoreArtefatti`. `PersonaggioBase` (circa 2000 righe) la implementa sopra un `PersonaggioMD` e contiene le regole comuni:

- **salute e magia**, con i loro massimi che crescono con il livello, e la **stanchezza**;
- **esperienza e livelli** (`GestoreProgressione` dà la curva: i punti per un livello crescono con il quadrato del livello meno uno) e **punti abilità**, che si spendono sugli attributi primari (`ComandoSpesaPuntoAbilita`);
- **attributi primari e secondari**: i primari sono il dato, i secondari si ricalcolano con `CalcolatoreAttributiSecondari` usando i moltiplicatori della classe; i **modificatori** (degli artefatti e a tempo) e gli **stati alterati** si sommano ai valori base;
- **inventario ed equipaggiamento** con le regole di `RegoleEquipaggiamento` (un artefatto per testa, corpo e ciascuna mano; accessori senza limite; arma in mano secondaria solo per Ladro, Ladra ed Elfi; armi a due mani solo a mani libere; ogni classe usa solo le armi, lo scudo, il libro e l'armatura adatti a lei; il peso e la Forza per l'armatura in `puoEquipaggiare`); il perché di un rifiuto è un `TipoMotivoRifiutoEquipaggiamento`;
- **danni, morte e resa**, con le debolezze, le resistenze e le immunità di classe ai tipi di danno (`getMoltiplicatoreDannoSubito`);
- le notifiche degli eventi (`NotificaVariazioneStatistichePersonaggio`, `…StatoVitale…`, `…EffettoDiStato…`, `…Modificatore…`).

Una **classe concreta** aggiunge solo ciò che la distingue: i valori di partenza degli attributi presi da `CostantiPersonaggi`, i nomi e gli articoli, il repertorio di incantesimi, le immunità e le offerte che può fare. Sono tre famiglie:

| Famiglia | Classi |
| :--- | :--- |
| **giocanti** | Guerriero/Guerriera, Ladro/Ladra, Mago/Maga, Elfo/Elfa, Bardo/Cantastorie, Sacerdote/Sacerdotessa, Chierico/Chierica, Druido/Druida (e l'`OmbraFiamma`, il personaggio segreto per le prove) |
| **mostri** | dal goblin al drago: arpia, centauro, chimera, eremita, folletto, gargoyle, gigante, goblin, hobgoblin, idra, lich, minotauro, necromante, scheletro, strega, titano, troll, viverna…; i non morti volanti incorporei (fantasma, ombra nera, spettro, spirito) condividono la base `NonMortoVolante`; alcuni sono i boss dei castelli |
| **non combattenti** | locandiere, armaiolo, alchimista, venditore di pergamene, incantatore, moglie del bardo, bardo della locanda, borgomastro, capitano delle guardie, e le comparse `Uomo` e `Donna`; più il `Viandante`, l'ospite che una missione deve scortare |

I mostri attaccano con `ArmaNaturale` (artigli, morsi, soffi… al posto dell'arma). `FabbricaPersonaggi` costruisce un personaggio da un `TipoPersonaggio` e dà quel che dipende solo dalla classe (i nomi, i moltiplicatori attraverso un personaggio modello, quanti se ne incontrano al massimo in una locazione). `LanciatoreDeiDadi` distribuisce i punti degli attributi alla creazione (un budget che cresce con il livello, ripartito in percentuali diverse per ogni classe), `EquipaggiamentoIniziale` dà a un giocante un'arma e una protezione leggera comuni, `CalcolatoreRiposo` dice quanto si recupera riposando (`TipoRiposo`: all'aperto senza fuoco, con fuoco, al coperto).

### I gruppi

- **`Gruppo`** (astratto) è un insieme di personaggi con il loro capo e le **scorte** di pergamene (per `TipoIncantesimo`) e di pozioni (salute, salute grande, magia, magia grande, aumento di livello). Le variazioni delle scorte del giocatore si annunciano alla UI con le notifiche; quelle degli avversari no.
- **`GruppoGiocatore`** è un singleton (`getIstanza`) che prende i suoi dati da chi glieli dà (`setFornitoreDiDati`, installato da `ModelloDati`; il fornitore va chiamato a ogni nuovo gruppo perché cambia quando si carica un salvataggio). Gestisce i personaggi (fino a 5 permanenti, più i temporanei), gli **ospiti**, le sfide vinte, il riposo, la fuga, l'esperienza, l'inventario condiviso e gli scambi di artefatti con i personaggi (`controllaPrelievo`, `equipaggia`, `stocca`, `chiPuoEquipaggiare`). `TipoMotivoNelGruppo` dice perché un personaggio è nel gruppo: permanente, aiuto gratuito, aiuto a pagamento, o risvegliato dalla Mietitura.
- **`GruppoAvversario`** è il gruppo dei mostri della locazione: un'istanza sola, che si svuota a ogni locazione.

### I tipi

`TipoPersonaggio` è l'identificativo di una classe (quello che salvano i dati e leggono motore e UI), con le sue `CategoriaPersonaggio` (umanoide, bestia, draconico, non morto, volante, gigantesco, ibrido, costrutto; servono a scegliere chi colpire «per categoria»). `TipoIncantesimo` (10 formule: aria, acqua, terra, fuoco, fulmine, gelo, veleno, morte, resurrezione, silenzio) con `CategoriaIncantesimo` (benefico o malefico) e `PortataIncantesimo` (globale, gruppo, multiplo, singolo qualsiasi): stanno qui perché servono al repertorio, alle immunità e alle scorte del gruppo; *come* si lanciano sta in `foresta-poteri`. `TipoOfferta` elenca i servizi di un incontro amichevole (aiuto gratuito o a pagamento, incantesimi, informazioni, mappe…), `TipoApproccio` come il giocatore si rivolge a un avversario che potrebbe non combattere. `CaratteristicaDiClasse` è ciò che il bestiario scopre a poco a poco (come una classe reagisce a un danno, un incantesimo o uno stato). `MoltiplicatoreDiClasse`, `MoltiplicatoriDiClasse` e `NoteDeiPersonaggi` portano i moltiplicatori di ogni classe con la nota che spiega perché ha quel valore. `PersonaggioNoto` elenca i personaggi non combattenti del mondo che hanno un nome proprio.

### Le costanti

`CostantiPersonaggi` (più di 1500 righe) contiene i valori di partenza di ogni classe, i moltiplicatori e il bilanciamento del combattimento (fuga, mietitura, danno ad area, cambi d'arma, dimensione dei gruppi…); `CostantiIncantesimi` quelli degli incantesimi.

### I dati e i contratti

- **Dati**: `PersonaggioMD` (identificativo, classe, nome, vivo/morto, livello, esperienza, punti abilità, causa del trapasso, motivo nel gruppo, valori minimi, massimi e attuali degli attributi, modificatori, stati alterati, artefatti), `GruppoMD` (scorte e personaggi) e `GruppoGiocatoreMD`.
- **Viste per la UI**: `VistaPersonaggio`, `VistaGruppo`, `VistaGruppoGiocatore`.
- **Eventi**: le notifiche sul personaggio e sul gruppo (livello, ferite, reazione di classe a un danno, approvazione o rifiuto degli spostamenti di artefatti, variazione delle scorte di pozioni e incantesimi) e gli interni che portano il dominio (creazione di un personaggio, un avversario sconfitto, un personaggio entrato nel gruppo, punti esperienza assegnati, caratteristica osservata, scheletro risvegliato, sfida vinta…).

## Osservazioni

- `PersonaggioBase` (circa 2000 righe) e `CostantiPersonaggi` sono i punti più grandi del progetto: aggiungere una classe vuol dire una classe concreta, un `TipoPersonaggio`, le sue costanti e un file di note, oltre all'eventuale `Comando` e alla UI.
- `GruppoGiocatore` e `GruppoAvversario` sono singleton statici, e il personaggio ne conosce l'esistenza in pochi punti (da che parte sta, quanti della sua specie ci sono): una dipendenza voluta fra personaggi e gruppi nello stesso modulo.
- Il quadro d'insieme è in [`motore_di_gioco.md`](motore_di_gioco.md) §8; le debolezze di classe in [`debolezze_e_resistenze.md`](debolezze_e_resistenze.md); i moltiplicatori in `modificatori_temporanei.md` e `bardo.md`, `chierico_e_druido.md` per le classi con abilità proprie.
