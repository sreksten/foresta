# foresta-poteri

I **poteri** dei personaggi: gli **incantesimi** (le pergamene che il gruppo compra e i mostri lanciano), i **canti** del Bardo e della Cantastorie, le **abilità innate** del Chierico, del Druido, del Sacerdote, del Mago e dell'Elfo. Contiene anche le azioni di uno scontro (`TipoAzione`, `EsitoAzione`) e il canto che il gruppo impara strada facendo.

Sta separato dai personaggi per la direzione delle dipendenze: un personaggio dice *chi è e che cosa ha*; *che cosa sa fare con la magia* lo dice questo modulo, e quindi dipende da lui. Sta anche prima del combattimento: un potere ha bisogno di colpire, di far durare uno stato e di raccontarsi, ma non deve sapere *come* lo scontro lo calcola. Per questo chiede tutto a un'interfaccia che il combattimento poi implementa (vedi sotto, `MeccanicaDeiPoteri`).

## Dipendenze

**Dichiarate nel pom:** `foresta-comandi`, `foresta-personaggi`.

**Solo per i test:** `foresta-personaggi`.

**Lo dichiarano:** `foresta-combattimento`, `foresta-economia`, `foresta-motore`.

Le dipendenze sono transitive: un modulo vede anche quello che vedono i moduli da cui dipende, quindi il compilatore garantisce la separazione fra i moduli che non si raggiungono.

- `foresta-personaggi`: i poteri agiscono su `Personaggio` e `Gruppo`, e i tipi che descrivono il repertorio (`TipoIncantesimo`, `CategoriaIncantesimo`, `PortataIncantesimo`) stanno lì.
- `foresta-comandi`: la richiesta di scelta dell'incantesimo da lanciare (`RichiestaSelezioneIncantesimoDaLanciare`) è una `RichiestaConComandi`.

Non dipende dal combattimento: lo raggiunge **solo** attraverso `MeccanicaDeiPoteri`.

## Funzionamento

### Il filo rovesciato: `MeccanicaDeiPoteri` e `Meccanica`

Per esercitarsi un potere deve poter chiedere: se un colpo va a segno (`colpisce`), quanto dura uno stato alterato (`calcolaDurataStato`), come si ripartisce il danno fra più bersagli (`quotaDelDannoPerPosizione`), come si applica un colpo magico (`colpisciConMagia`: se il bersaglio è immune non succede niente, altrimenti si tira per colpire e, se va a segno, si applica il danno e si registra l'esito per le tattiche), come si racconta (`annuncia`) e come si registra quel che è successo (`registraEsitoAzione`, `registraIncantesimoLanciato`, `registraAlleato`). **Sono i poteri a dire che cosa chiedono**: chi fa i conti — `combattimento.MeccanicaDelloScontro` — implementa l'interfaccia e dipende da loro, non il contrario.

`Meccanica.get()` restituisce l'implementazione: quella che ha installato chi fa partire il gioco (`Meccanica.installa`) oppure, se nessuno l'ha fatto, quella trovata con il `ServiceLoader` (il modulo di combattimento la dichiara in `META-INF/services/com.threeamigos.foresta.poteri.MeccanicaDeiPoteri`). Senza una meccanica lancia un `IllegalStateException` esplicito.

### Gli incantesimi

`Incantesimo` ha una classe (`TipoIncantesimo`), un livello, un costo di lancio in magia (lo dice il tipo) e `formula(formulante, personaggioBersaglio, gruppoBersaglio)`. `FabbricaIncantesimi` costruisce quello di ogni tipo o ne sceglie uno a caso.

- **Malefici** — aria, acqua, terra, fuoco, fulmine, gelo, veleno, morte e silenzio: estendono `IncantesimoMaleficoImpl`, che per ogni bersaglio tira per colpire e calcola il danno come per un'arma (`Silenzio` applica `SILENZIATO` senza far danno, `Morte` è il colpo di grazia).
- **Benefico** — la `Resurrezione` è l'unico.

Le pergamene sono **scorte del gruppo** (il conteggio sta in `Gruppo`), non oggetti: chi le lancia le consuma.

### I canti

`Canto` (costa magia, non consuma pergamene, e per questo non è un incantesimo) ha `canta(cantore, gruppo, gruppoAvversario)` e `cantaSu(…)` per i canti con un bersaglio scelto da chi canta, per esempio da una tattica. `CantoDelBardo` decide chi può cantare e quando; `FabbricaCanti` costruisce il canto di ogni `TipoCanto`: **curativo** (cura il personaggio più ferito, il cantore compreso), **ipnotico** (scuote il morale degli avversari), **di valore** (ispira il gruppo: lo stato `ISPIRATO`) e l'**Urlo della Banshee** (l'ipnotico potenziato, che il bardo della locanda insegna al gruppo). `CantiImparati` (con `CantiImparatiMD`, che `ModelloDati` salva) dice quali canti il gruppo ha imparato strada facendo: il gruppo non sa di averli imparati, lo sanno i canti; i dati li prende da chi li custodisce (`setFornitoreDiDati`). I valori sono in `CostantiCanti`; il quadro è in [`bardo.md`](bardo.md).

### Le abilità innate

`Abilita` (stessa idea dei canti) con `usa`/`usaSu`. `AbilitaInnate` decide chi ha quali abilità, dal `TipoPersonaggio` e non dalla classe Java; `FabbricaAbilita` costruisce quella di ogni `TipoAbilita`; `CostoDelleAbilita` dà il costo per classe (il dardo arcano costa meno al Mago che all'Elfo). Quelle che fanno danno estendono `AbilitaDiDanno`, che è essa stessa l'attacco magico.

| Classe | Abilità |
| :--- | :--- |
| Chierico/Chierica | scacciare i non morti, imposizione delle mani (cura il più ferito) |
| Druido/Druida | radici, sciame, pelle di corteccia (difesa di tutto il gruppo), rigenerazione (`RIGENERAZIONE`) |
| Sacerdote/Sacerdotessa | alba sacra (toglie gli stati negativi), luce divina |
| Mago/Maga, Elfo/Elfa | dardo arcano |

I valori sono in `CostantiAbilitaInnate`; `AbilitaInnateDiClasse` raccoglie nome e descrizione di ogni capacità di una classe per il bestiario. Il quadro è in [`chierico_e_druido.md`](chierico_e_druido.md).

### Le azioni di uno scontro

`TipoAzione` elenca le azioni che una voce di tattica può compiere: l'attacco fisico, ciascun incantesimo, la luce divina e il dardo arcano, i canti, le abilità del chierico e del druido, le pozioni, il cambio d'arma… `EsitoAzione` è com'è andata (`RIUSCITA`, `MANCATA`): la memoria dello scontro la tiene `StatoDelCombattimento`, e le tattiche la usano per non ripetere un'azione che fallisce.

`OggettoConCostoDiLancio` è il contratto comune di ciò che chi lo usa paga con la magia.

## Osservazioni

- La coppia `MeccanicaDeiPoteri`/`Meccanica` risolve l'inversione: combattimento e poteri sono «in ordine inverso» rispetto a quello che la logica suggerirebbe (il combattimento usa i poteri, i poteri hanno bisogno del combattimento). Il piano per toglierla è in `assessment.md`.
- `Meccanica` è uno stato statico: i test che usano i poteri devono avere il modulo di combattimento fra le dipendenze (o installare una meccanica finta).
