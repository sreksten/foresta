# 06-foresta-bestiario

Il **bestiario**: un diario dei personaggi che si possono incontrare come avversari o avere nel gruppo, e che **vale da una partita all'altra**, come i trofei. Il modulo ricorda quali voci si sono sbloccate e quali caratteristiche di ogni classe si sono scoperte (come reagisce a un tipo di danno, se è immune a un incantesimo o a uno stato), e ascolta gli eventi che le fanno avanzare.

Esiste a sé perché, a differenza di quasi tutto il resto, **non appartiene a una partita**: non fa parte di `ModelloDati`, non si reimposta mai e si salva in un file a parte. Per questo ha un suo modello dati (`BestiarioMD`) e un suo gestore (`GestoreBestiario`), che il motore implementa su file.

## Dipendenze

**Dichiarate nel pom:** `02-foresta-comandi`, `05-foresta-personaggi`.

**Solo per i test:** `05-foresta-personaggi`.

**Lo dichiarano:** `10-foresta-trofei`.

Le dipendenze sono transitive: un modulo vede anche quello che vedono i moduli da cui dipende, quindi il compilatore garantisce la separazione fra i moduli che non si raggiungono.

Dipende da `05-foresta-personaggi`: le voci sono ruoli di `TipoPersonaggio`, e le caratteristiche sono quelle di `CaratteristicaDiClasse`, che escono dalle tabelle delle classi; e da `02-foresta-comandi` perché la richiesta di aprire la pagina (`RichiestaAperturaBestiario`) è una `RichiestaConComandi`. I testi e la scheda li disegna la UI.

## Funzionamento

### Le voci e le caratteristiche

Una **voce** è un ruolo (`TipoPersonaggio.getRuolo()`): Mago e Maga sono la stessa voce. Sono 32: i 24 mostri più i ruoli di chi si sceglie o si recluta (Guerriero, Ladro, Mago, Elfo, Bardo, Sacerdote, Chierico, Druido). Hanno una voce solo le classi per cui `hasVoceNelBestiario()` è vero (i non combattenti, il viandante e l'ombrafiamma no, perché non si sconfiggono né si reclutano).

Le **caratteristiche** scoperte si ricordano per chiave (`DANNO:FUOCO`, `INCANTESIMO:MORTE`, `EFFETTO:SANGUINAMENTO`): non il loro contenuto, che lo sa `TipoPersonaggio`, ma il fatto che il giocatore l'ha vista. Si scoprono **indipendentemente dalla voce** (un colpo di fuoco su una classe dice come reagisce al fuoco anche se non l'hai ancora sconfitta) ma la voce le mostra solo da sbloccata.

### Quando una voce si sblocca

In due modi: si sconfigge un avversario di quella classe (anche facendoci amicizia, corrompendolo o battendolo a carta, forbici e sasso), oppure un personaggio di quella classe entra nel gruppo. `RegistroBestiario.registrati()` si iscrive agli eventi (`InternoAvversarioSconfitto`, `InternoAmiciziaStretta`, `InternoCorruzioneRiuscita`, `InternoSfidaVinta`, `InternoPersonaggioEntratoNelGruppo` e `InternoCaratteristicaOsservata`), sblocca la voce o scopre la caratteristica, **salva subito** (le novità sono rare: ogni scoperta una volta sola in tutte le partite) e pubblica `InternoVoceDelBestiarioSbloccata`.

### La facciata e i dati

- **`RegistroBestiario`** è la facciata su `BestiarioMD`: `isSbloccata(classe)`, `getNumeroVociSbloccate`, `getNumeroVoci`, `getCaratteristicheScoperte(classe)`. Il modello si legge una volta all'avvio (`impostaGestoreBestiario`).
- **`BestiarioMD`** tiene le voci sbloccate e le caratteristiche scoperte per voce.
- **`GestoreBestiario`** (interfaccia: `carica`, `salva`) dice dove sta il bestiario: nel gioco `Main` passa l'implementazione su file (`GestoreBestiarioSuFile`, nel motore), i test un gestore in memoria.

- **`RichiestaAperturaBestiario`** (pacchetto `eventi.richieste`): l'`Automa` la pubblica quando il giocatore chiede di vedere il bestiario dall'inventario, con il solo comando `ANNULLA`; la UI apre la pagina.

La scheda con silhouette, nome, immagini, descrizione, abilità innate, forze e debolezze e moltiplicatori di classe la disegna la UI (`DisplayableCanvasBestiario`).

## Osservazioni

- Il trofeo `Enciclopedista` si vince con tutte le voci sbloccate: è per questo che `10-foresta-trofei` dipende da questo modulo e non il contrario.
- Il quadro completo, con la scheda e le regole delle voci, è in [`bestiario.md`](bestiario.md).
