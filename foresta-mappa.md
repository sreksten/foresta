# foresta-mappa

La **mappa della Foresta**: una griglia di caselle, ciascuna con il suo tipo di locazione, il suo nome e le sue proprietà; quali caselle il gruppo conosce o ha visitato; dove si trova il gruppo del giocatore e come si sposta; le locazioni che esistono una volta sola (le città, i castelli) e le città che il Drago ha distrutto; e la costruzione di una mappa nuova a ogni partita.

È un modulo a sé perché «dove si è» e «che cosa c'è intorno» sono un concetto che serve a molti (le statistiche contano le visite, le missioni cercano un posto libero, la UI disegna la mappa) ma che non ha bisogno di sapere *chi* abita le caselle. Il gruppo, per esempio, **non sa dove sta**: lo sa la mappa. Non sa nemmeno chi costruisce il mondo vivo (le locazioni giocabili, i registri, i personaggi): quello sta nel motore.

## Dipendenze

**Dichiarate nel pom:** `foresta-eventi`, `foresta-testi`, `foresta-serializzazione`, `foresta-strumenti`.

**Lo dichiarano:** `foresta-statistiche`, `foresta-intermezzi`, `foresta-modellodati`, `foresta-motore`.

Le dipendenze sono transitive: un modulo vede anche quello che vedono i moduli da cui dipende, quindi il compilatore garantisce la separazione fra i moduli che non si raggiungono.

- `foresta-eventi`: la mappa pubblica `InternoLocazioneVisitata`, `NotificaVariazioneConoscenzaMappa` e riceve `RichiestaVisualizzazioneMappa`.
- `foresta-testi`: i nomi delle locazioni (file per enum) e le grammatiche dei luoghi e delle locande.
- `foresta-serializzazione`: `ForestaMD`, `LocazioneMD` e `CoordinateMD` si salvano.
- `foresta-strumenti`: il caso per costruire la mappa (`Dado`).

Non dipende dai personaggi né dagli artefatti: quando una casella nasce, la mappa lo dice a chi la abita attraverso un'interfaccia (`PopolatoreDellaForesta`), e quello che ci mette non lo sa.

## Funzionamento

### I tipi

- `TipoLocazione` elenca le caselle: radura, bosco, palude, locanda, rovine, tempio, grotta, le quattro botteghe (alchimista, armaiolo, venditore di pergamene, incantatore), le quattro città (Nyena, Malgaard, Ruuna, Fleena) e i cinque castelli (Idra, Minotauro, Lich, Strega, Drago). Il nome da mostrare sta in un file dei testi.
- `CategoriaLocazione`: `STANDARD`, `CITTA`, `CASTELLO`.
- `Quadrante`: nord-ovest, nord-est, sud-ovest, sud-est. Le città e i castelli si sparpagliano **uno per quadrante** (il nord è dove la y è più piccola).

### I dati

- `ForestaMD` tiene la griglia (una `LocazioneMD` per posizione) e tre informazioni derivate, per non ricavarle ogni volta con un ciclo: dove stanno le locazioni uniche, i limiti dell'area conosciuta (serve alla mappa a tutto schermo) e un **numero di versione** che cambia a ogni modifica visibile della mappa (chi la disegna sa se la sua copia è da rifare). Tiene anche **dove si trova il gruppo** e **quali città sono distrutte**. Il salvataggio scrive le dimensioni, la posizione, le città distrutte e una riga per casella.
- `LocazioneMD` è la singola casella: tipo, nome proprio, e una **mappa di proprietà** dove vive lo stato durevole con la convenzione «la presenza della chiave vale per vero» (visitata, conosciuta, e quel che ci mettono gli altri moduli: la locanda, il tempio…).
- `CoordinateMD` è una posizione.

### `Foresta`: le operazioni sulla mappa

Una classe di soli metodi statici. I dati della partita in corso non li possiede: li chiede a un **fornitore** (`Foresta.setFornitoreDiDati`, installato da `ModelloDati` in `foresta-modellodati`) a ogni uso, perché il fornitore cambia quando si carica un salvataggio. Senza fornitore lancia un `IllegalStateException` esplicito. Le funzioni principali:

- **leggere e cambiare le caselle**: `getLocazione`, `getLocazioneMD`, `impostaLocazione`, `costruisciLocazione`, `getNomeDaMostrare`, `nomeNuovo` (pesca un nome che nessun'altra casella dello stesso tipo ha già);
- **locazioni uniche**: `costruisciLocazioneUnica`, `getCoordinateLocazioneUnica`, `distruggiLocazioneUnica` (il Drago distrugge una città e al suo posto restano delle rovine), `isCittaDistrutta`, `setCittaDistrutta`;
- **conoscenza**: `isLocazioneVisitata`/`setLocazioneVisitata`, `isLocazioneConosciuta`/`setLocazioneConosciuta`, `ottieniMappa`, `ottieniMappaZona`, `ottieniMappaIntorno` (le mappe comprate o ricevute);
- **dove sta il gruppo e come si sposta**: `getPosizioneDelGruppo`, `getLocazioneDelGruppo`, `isGruppoIn`, `posizionaIlGruppoSuUnaCasellaLibera`, `getMaxPassiNord/Est/Sud/Ovest` (quanti passi si possono fare in quella direzione, al massimo `MAX_MOVIMENTO` = 5) e `muoveNord/Est/Sud/Ovest(passi)`;
- **spazio libero**: `getCoordinateLibere`, `isLibera`;
- **mappa a tutto schermo**: i limiti dell'area conosciuta e `getVersioneMappa`;
- il **bosco** ha quattro immagini alternative (`NUMERO_VARIANTI_BOSCO`), scelta una volta per tutte alla costruzione (`VARIANTE_BOSCO`) così la mappa non cambia da una visualizzazione all'altra né dopo un salvataggio.

`Direzioni` dice a parole la direzione e la distanza fra due punti («a poco più di un giorno di cammino verso nordest»), per le informazioni su città, castelli e artefatti.

### La costruzione: `GeneratoreDellaForesta` e `PopolatoreDellaForesta`

`GeneratoreDellaForesta.genera(dimensioneX, dimensioneY, personaggiDisponibili, popolatore)` svuota la mappa e ne costruisce una nuova: le **città** (una per quadrante, in ordine casuale), le **locande** (almeno quante i personaggi da sistemare), i **templi**, le grotte, le paludi, le rovine, le radure, e il bosco per tutto il resto. I **castelli non si costruiscono qui**: li rivendica ciascuno la propria missione.

A chi abita le caselle pensa il `PopolatoreDellaForesta`, un'interfaccia a callback: la mappa costruisce le caselle e i nomi, e a ogni casella importante dice al popolatore che è nata (`cittaCostruita`, `dopoLeCitta`, `locandaCostruita`, `tempioCostruito`, `impostaDatiLocanda`) perché ci metta quel che le serve — i residenti di una città, il magazzino di un negozio, i personaggi nelle locande, gli artefatti nei templi — chiedendo se ce ne sono ancora (`prelevaPersonaggio`, `prelevaArtefatto`). Il popolatore vero è nel motore (`CostruttoreDellaForesta`).

### I contratti per la UI e per gli altri moduli

- `VistaMappa`: la mappa in sola lettura (dimensioni, tipo di una casella, variante del bosco, conosciuta, visitata, i limiti dell'area conosciuta e la versione, il nome da mostrare, le caselle da segnalare e i nomi delle missioni da mostrare su una casella, la posizione e la locazione del gruppo).
- `NotificaVariazioneConoscenzaMappa`: il gruppo ora conosce un'area rettangolare (per esempio ha comprato una mappa).
- `InternoLocazioneVisitata`: il gruppo è arrivato per la prima volta in una casella di quel tipo; tornarci non lo ripubblica. I trofei si iscrivono qui per contare le visite.
- `RichiestaVisualizzazioneMappa`: il giocatore chiede la mappa a tutto schermo.

## Osservazioni

- La mappa è stato globale (statico) con dati iniettati: i test devono installare un fornitore, e lo stesso vale per ogni modulo che usa `setFornitoreDiDati`.
- Il quadro di tempo, locazioni e mappa nel motore è in [`motore_di_gioco.md`](motore_di_gioco.md) §6.
