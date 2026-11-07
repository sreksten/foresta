# 03-foresta-tutorial

Il **tutorial**: i suggerimenti per chi gioca per la prima volta (il piano e le decisioni sono in [`tutorial.md`](tutorial.md)). Il modulo è la *memoria* del tutorial e il catalogo dei suggerimenti: dice quali sono, quando scattano, a che cosa puntano, quali sono già stati visti e se il tutorial è acceso. **Non sa niente della UI e non manda eventi**: la UI gli dice che cosa vede e quando un suggerimento è stato letto, il motore lo legge per pilotare la partita (vedi `12-foresta-motore`, `PilotaggioDelTutorial`). Il modello del fumetto (`TutorDelGioco`) è in `12-foresta-ui-comune`; il disegno del fumetto, il pollice e il bordo che lampeggia sono dei toolkit e non ci sono ancora.

Esiste a sé perché, come il bestiario, **non appartiene a una partita**: non fa parte di `ModelloDati`, non si reimposta mai e si salverebbe in un file a parte.

## Dipendenze

**Dichiarate nel pom:** `00-foresta-serializzazione`, `02-foresta-comandi`.

**Lo dichiarano:** `12-foresta-motore`, `12-foresta-ui-comune`.

Le dipendenze sono transitive: un modulo vede anche quello che vedono i moduli da cui dipende, quindi il compilatore garantisce la separazione fra i moduli che non si raggiungono.

Dipende da `00-foresta-serializzazione` per `SuggerimentiMD` (il dato che si salva), da `02-foresta-comandi` perché le icone a cui punta un suggerimento si identificano con il loro `Comando`, e da `01-foresta-testi` (transitiva, passando dai comandi) per le pagine di testo.

## Funzionamento

### I tipi (pacchetto `tipi`)

- **`TipoSuggerimento`**: i quindici suggerimenti, nell'ordine in cui si danno quando più di uno scatta nella stessa situazione. Ognuno ha la `Situazione` che lo innesca, il `BersaglioDelSuggerimento` a cui punta e le sue pagine di testo (`getNumeroPagine`, `getPagina(n, dito)`): una pagina per ogni cosa che si può fare, al massimo tre (`PAGINE_MASSIME`). I testi sono in `tipo_suggerimento_it.properties` e `_en` (l'inglese britannico ripiega sull'inglese), chiavi `COSTANTE.PAGINA_n`; la variante per il dito è `COSTANTE.PAGINA_n.TOCCO`, e se manca vale la pagina normale.
- **`Situazione`**: che cosa vede la UI, nei termini che al tutorial interessano (la prima schermata di gioco, il combattimento aperto, un oggetto trovato, la notte...). Una situazione innesca un solo suggerimento (lo controlla un test).
- **`BersaglioDelSuggerimento`**: a che cosa punta la coda del fumetto e che cosa lampeggia: un riquadro, la barra o un gruppo di icone (con i loro comandi). Le posizioni le sa la UI.

### Il registro e i dati

- **`RegistroSuggerimenti`** (pacchetto `tutorial`): `isAttivo`/`setAttivo`; `situazione(Set<Situazione>)`, che risponde con il primo suggerimento ancora da vedere che scatta in una di quelle situazioni (niente se il tutorial è spento); `visto(suggerimento)`, `isVisto`; `ripristina()`, che riaccende il tutorial e fa rivedere tutto; `isMostrabile`/`setMostrabile`, se l'interfaccia sa mostrare i suggerimenti (non si salva, non si ripristina). Il modello si legge una volta (`impostaGestore`) e si salva a ogni novità.
- **`SuggerimentiMD`**: se è acceso e quali suggerimenti sono visti. Riga di stato, poi una riga per suggerimento visto; un suggerimento che non esiste più si salta.
- **`GestoreSuggerimenti`** (interfaccia: `carica`, `salva`): dove sta il tutorial. **Per ora il motore non ne passa nessuno**, quindi non si salva niente e il tutorial parte sempre acceso e senza suggerimenti visti (decisione 10 di `tutorial.md`: altrimenti bisognerebbe ripristinarlo a ogni prova). L'implementazione su file, come per il bestiario, si fa quando il tutorial è finito.

### Chi lo usa

- **`LocazioneBase`** (`12-foresta-motore`): al primo turno della partita (`Statistiche.getTurniGiocati() == 0`) a tutorial acceso non offre l'icona della fuga né quella di passare inosservato, e ignora i due comandi se arrivano lo stesso.
- **`PilotaggioDelTutorial`** (`12-foresta-motore`): il truccatore dei dadi che pilota i primi cinque turni; legge solo se il tutorial è acceso.
- **I test** (`PartitaDiTest`) spengono il tutorial all'avvio e lo rimettono com'era alla chiusura: chi vuole provarlo lo riaccende.

## Osservazioni

- Lo stato è statico, come per il bestiario: i test devono rimettere com'era (`RegistroSuggerimenti.ripristina()`).
- Il passo del pilota dei dadi (`12-foresta-motore`, `PilotaggioDelTutorial`) legge da qui quali suggerimenti sono visti (`isVisto`) e se l'interfaccia li mostra (`isMostrabile`, non si salva: lo dice il toolkit con `TutorDelGioco.setAbilitato`).
- **Da fare**: la UI (il fumetto con coda e pagine, il pollice, l'annulla, il bordo che lampeggia, `ForestaUI` che mette insieme le `Situazione`), l'icona di ripristino a inizio partita, gli eventi nuovi dove la UI non può dedurre la situazione (notte, primo ferito, mostro corrompibile o amichevole), il gestore su file e il salvataggio acceso, la verifica dei gesti scritti in `GESTI_SUL_TOCCO` (tocco, doppio tocco, pressione prolungata, trascinamento) con quelli veri dei due toolkit.
