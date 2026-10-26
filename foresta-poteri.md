# foresta-poteri

I **poteri** dei personaggi: gli **incantesimi** (le pergamene che il gruppo compra e i mostri lanciano), i **canti** del Bardo e della Cantastorie, le **abilità innate** del Chierico, del Druido, del Sacerdote, del Mago e dell'Elfo. Contiene anche il canto che il gruppo impara strada facendo. Le azioni di uno scontro (`TipoAzione`, `EsitoAzione`) sono nel combattimento.

Sta separato dai personaggi per la direzione delle dipendenze: un personaggio dice *chi è e che cosa ha*; *che cosa sa fare con la magia* lo dice questo modulo, e quindi dipende da lui. Sta anche dopo il combattimento: un potere ha bisogno di colpire, di far durare uno stato e di raccontarsi, e lo fa chiamando direttamente il combattimento (che non sa che esistano i poteri).

## Dipendenze

**Dichiarate nel pom:** `foresta-combattimento`, `foresta-comandi`, `foresta-personaggi`.

**Solo per i test:** `foresta-personaggi`.

**Lo dichiarano:** `foresta-tattiche`, `foresta-economia`, `foresta-motore`.

Le dipendenze sono transitive: un modulo vede anche quello che vedono i moduli da cui dipende, quindi il compilatore garantisce la separazione fra i moduli che non si raggiungono.

- `foresta-personaggi`: i poteri agiscono su `Personaggio` e `Gruppo`, e i tipi che descrivono il repertorio (`TipoIncantesimo`, `CategoriaIncantesimo`, `PortataIncantesimo`) stanno lì.
- `foresta-comandi`: la richiesta di scelta dell'incantesimo da lanciare (`RichiestaSelezioneIncantesimoDaLanciare`) è una `RichiestaConComandi`.

- `foresta-combattimento`: per esercitarsi un potere chiede al combattimento se un colpo va a segno, quanto dura uno stato alterato e come si ripartisce il danno, e gli affida l'annuncio e la registrazione degli esiti (vedi sotto). Il combattimento non conosce i poteri.

## Funzionamento

### Come si esercitano: chiamano il combattimento

Per esercitarsi un potere chiama direttamente le classi del combattimento: `CalcolatoreCombattimento.colpisce` (se un colpo va a segno), `calcolaDurataStato` (quanto dura uno stato alterato) e `quotaDelDannoPerPosizione` (come si ripartisce il danno fra più bersagli); `ColpoMagico.colpisci` (se il bersaglio è immune non succede niente, altrimenti si tira per colpire e, se va a segno, si applica il danno e si registra l'esito per le tattiche); `AnnuncioDellAzione.azione` (racconta che l'attore agisce); e `StatoDelCombattimento` per registrare quel che è successo (`registraEsitoAzione`, `registraIncantesimoLanciato`, `registraAlleato`). Non c'è nessuna interfaccia in mezzo né un servizio da installare o da cercare: il combattimento sta sotto.

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

### Il ponte con le azioni

`TipoAzione` ed `EsitoAzione` stanno nel combattimento (le usa la memoria dello scontro). Ogni abilità innata e ogni canto sa qual è la sua azione: `TipoAbilita.getTipoAzione()` e `TipoCanto.getTipoAzione()`; per gli incantesimi c'è `TipoAzione.di(TipoIncantesimo)`.

`OggettoConCostoDiLancio` è il contratto comune di ciò che chi lo usa paga con la magia.

## Osservazioni

- I test dei poteri hanno bisogno del combattimento, che sta sotto di loro e si trova da solo.
