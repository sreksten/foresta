# 07-foresta-mondo

**Chi e che cosa c'è nella Foresta** oltre al gruppo del giocatore: il `RegistroPersonaggi` (i residenti delle città, i campioni delle sfide, i compagni da reclutare, i personaggi sparsi per le caselle, il negoziante di una bottega) e il `RegistroArtefatti` (gli artefatti da distribuire, quelli sparsi per le caselle e quelli custoditi nei templi), con i loro dati.

È un modulo a sé perché la mappa sa solo *dove* sono le caselle e il gruppo sa solo *chi* ne fa parte: l'elenco di chi abita il mondo e di che cosa ci è sparso è una responsabilità sua. Il mondo viene popolato mentre la mappa si costruisce (la mappa chiede al popolatore di metterci qualcosa quando nasce una casella) e poi le missioni, l'economia e i negozi lo interrogano.

## Dipendenze

**Dichiarate nel pom:** `06-foresta-statistiche`.

**Solo per i test:** `05-foresta-personaggi`.

**Lo dichiarano:** `08-foresta-economia`, `09-foresta-missioni`, `10-foresta-modellodati`.

Le dipendenze sono transitive: un modulo vede anche quello che vedono i moduli da cui dipende, quindi il compilatore garantisce la separazione fra i moduli che non si raggiungono.

Dichiara solo `06-foresta-statistiche`, e da lì vede per transitività i personaggi, la mappa, gli artefatti e gli intermezzi. Le statistiche gli servono per il livello del mondo: i personaggi e gli artefatti che costruisce nascono a quel livello.

## Funzionamento

### `RegistroPersonaggi`

Tiene i personaggi del mondo che non stanno nel gruppo del giocatore. Il dato è `RegistroPersonaggiMD`, che il registro riceve da chi lo custodisce (`setFornitoreDiDati`, installato da `ModelloDati`). Tre insiemi:

- i personaggi **da distribuire** nelle locazioni (si prendono con `getPersonaggioDisponibile` o `getPersonaggioCasuale`; l'elenco non si salva, serve solo quando si costruisce la foresta);
- i personaggi **già posati nelle caselle** (uno per casella, riconosciuti dalle coordinate: `addPersonaggioInLocazione`, `getPersonaggioInLocazione`, `rimuoviPersonaggioInLocazione`);
- i **residenti** (`ResidenteMD`): chi ha un nome proprio nel mondo senza stare in un gruppo o in una locazione — il bardo e sua moglie, gli artigiani delle città (armaiolo, alchimista, venditore di pergamene, incantatore, locandiere…), il borgomastro, il capitano delle guardie, i campioni delle sfide. Si creano con `creaIResidentiDellaCitta`, `creaIlBardoESuaMoglie`, `registraCampione` e si interrogano con `getResidente(PersonaggioNoto[, città])`, `getNomeCompleto`, `getCampioni`, `segnaCampioneSconfitto`; `costruisciPersonaggio(residente[, livello])` ne fa un personaggio vero.

`preparaCompagno` prepara un compagno da reclutare, e `getNegoziante(casella)` dice chi tiene una bottega (la chiave `NEGOZIANTE` nelle proprietà della casella). I nomi vengono dalle liste a esaurimento di `nomi.txt` (vedi [`nomi.md`](nomi.md)).

### `RegistroArtefatti`

Tiene gli artefatti del mondo che non sono nell'inventario del gruppo, dal dato `RegistroArtefattiMD`: quelli **da distribuire** (`getArtefattoDisponibile`, `getArtefattoCasuale`), quelli **smarriti nelle locazioni** (uno per casella: `addArtefattoInLocazione`, `getArtefattoInLocazione`, `rimuoviArtefattoInLocazione`), quelli custoditi nei **templi** (`custodisciInUnTempioNuovo`) e **le caselle in cui il gruppo sa** che c'è un artefatto smarrito (`segnaLocalizzazioneConosciuta`, `isLocalizzazioneConosciuta`). I dati sono `ArtefattoMD`: il registro li trasforma in `Artefatto` e li distribuisce a caso. **I magazzini dei negozi** non sono qui: li governa `MagazziniDeiNegozi` in `08-foresta-economia`, con i suoi dati (`MagazziniMD`).

### I dati

`RegistroPersonaggiMD`, `RegistroArtefattiMD` e `ResidenteMD` stanno nel pacchetto `modellodati` di questo modulo e vengono messi insieme e salvati da `ModelloDati`. L'elenco dei personaggi e degli artefatti da distribuire non si salva.

## Osservazioni

- I registri sono statici con dati iniettati, come la mappa e le statistiche: i test installano un fornitore o passano da `ModelloDati`.
- Quando si crea una partita nuova si chiamano `reimposta` dei due registri insieme alla costruzione della foresta (`CostruttoreDellaForesta`, nel motore), che implementa il `PopolatoreDellaForesta` della mappa.
