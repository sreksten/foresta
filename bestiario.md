# Il bestiario

Un diario dei personaggi, che si apre dall'inventario del gruppo (il bottone "Bestiario", accanto a trofei e tattiche) e **vale da una partita all'altra**, come i
trofei. Ha una voce per ogni personaggio che si può incontrare come avversario o avere nel gruppo, tutte presenti fin dall'inizio: finché una non si è
**sbloccata** se ne vede solo la silhouette e "???" al posto del nome, sia nell'elenco a sinistra sia nella scheda a destra. Poi compaiono il nome, le immagini,
la descrizione, le abilità innate, le forze e le debolezze che si sono scoperte e il carattere della classe (i moltiplicatori con le loro note).

## 1. Le voci

Sono **32**: i 24 mostri più i ruoli di chi si sceglie o si recluta (Guerriero, Ladro, Mago, Elfo, Bardo, Sacerdote, Chierico, Druido). Il ruolo vale per tutte e due
le forme: **Mago e Maga sono una voce sola** (`TipoPersonaggio.getRuolo`), che si ricorda con la classe al maschile.

| Cosa | Dove |
| :--- | :--- |
| Chi compare come avversario: i mostri e i giocanti che le missioni mettono fra i nemici (`NEMICO=`, `ONDATA_n=`, `SFIDANTI=`, `CAMPIONE=` di `missioni.txt`) | `TipoPersonaggio.isAvversario()` |
| Chi può entrare nel gruppo (si sceglie a inizio partita o si recluta in locanda) | `TipoPersonaggio.isCompagno()` |
| Chi ha una voce (l'uno o l'altro) | `TipoPersonaggio.hasVoceNelBestiario()` |
| L'elenco delle voci, una per ruolo, nell'ordine dell'enum | `TipoPersonaggio.getVociDelBestiario()` |
| La classe al femminile di un ruolo (Maga per Mago) e il nome della voce ("Mago e Maga") | `getFormaFemminile()`, `getNomeDellaVoce()` |
| La descrizione, una per ruolo | `tipo_personaggio_it.properties`, voce `DESCRIZIONE` (sul maschile), letta da `getDescrizione()` |

Non hanno una voce i non combattenti (locandiere, armaiolo, alchimista, venditore di pergamene, incantatore, moglie del bardo, bardo delle locande, capitano delle
guardie, `UOMO`, `DONNA`, `BORGOMASTRO`, `VIANDANTE`: non si sconfiggono né si reclutano, quindi la voce resterebbe sempre chiusa) e l'`OMBRAFIAMMA`.

Le descrizioni raccontano il personaggio e accennano a forze e debolezze, senza i numeri: quelli li scrive il bestiario man mano che si scoprono.

## 2. Quando una voce si sblocca

In due modi: **si sconfigge un avversario di quella classe**, oppure **un personaggio di quella classe entra nel gruppo**. `RegistroBestiario` (facciata su
`BestiarioMD`, nel pacchetto `bestiario`) ascolta gli eventi, sblocca la voce, la salva subito e pubblica `InternoVoceDelBestiarioSbloccata`:

| Evento | Quando |
| :--- | :--- |
| `InternoAvversarioSconfitto` | un avversario muore o si arrende (`LocazioneBase.registraMorteAvversario`, che vale anche per le azioni delle tattiche) |
| `InternoAmiciziaStretta`, `InternoCorruzioneRiuscita` | amicizia o corruzione riuscite: contano come un incontro riuscito per tutti gli avversari presenti |
| `InternoSfidaVinta` | si vince una sfida a carta, forbici e sasso (`LocazioneBase.concludiLaSfida`, anche dei campioni): lo sfidante non si uccide, ma conta |
| `InternoPersonaggioEntratoNelGruppo` | un personaggio diventa membro del gruppo (`GruppoGiocatore.diventaMembro`): quello scelto a inizio partita, un compagno reclutato, un aiuto a tempo, chi c'è in un gruppo ricaricato. Non gli ospiti, che viaggiano con il gruppo senza farne parte |

Si sblocca subito, senza aspettare che il gruppo esca vivo dalla locazione (a differenza del progresso dei trofei), e anche in modalità di prova. Una classe senza voce
(un non combattente) non sblocca niente.

## 3. Che cosa si scopre

Le **caratteristiche generali** di una classe stanno in `TipoPersonaggio` (e nelle sue categorie, `CategoriaPersonaggio`): come subisce ogni tipo di danno
(`getMoltiplicatoreDannoSubito`), a quali incantesimi è immune (`isImmuneAIncantesimo`, o a tutti: il gargoyle) e a quali effetti di stato (`isImmuneAEffetto`).
`TipoPersonaggio.getCaratteristiche()` le elenca come `CaratteristicaDiClasse`: un genere (`DANNO`, `INCANTESIMO`, `EFFETTO`), il tipo e, per i danni, il
moltiplicatore; la chiave ("DANNO:FUOCO", "INCANTESIMO:MORTE", "EFFETTO:SANGUINAMENTO", "INCANTESIMO:TUTTI") è quel che il bestiario ricorda e non cambia con la
lingua. I testi ("Debole ai danni di tipo Fuoco (x1,5)", "Immune all'incantesimo di Morte") sono in `caratteristica_di_classe_it.properties`.

**Il personaggio, oltre alla classe.** `PersonaggioBase.isImmuneAIncantesimo` e `isImmuneAEffetto` guardano **sia la classe sia il personaggio**: la prima è
`TipoPersonaggio`, il secondo è `isImmuneAIncantesimoPerIstanza` / `isImmuneAEffettoPerIstanza` (protetti, di norma falsi), che una sottoclasse o un personaggio
particolare ridefinisce: il Guerriero in berserk non si confonde né si spaventa (`Guerriero`, `Guerriera`), e un domani un goblin di una missione potrà essere immune alla
magia senza che lo siano tutti i goblin. Le immunità di un singolo personaggio **non** entrano nel bestiario, che descrive le classi.

**Quando si scopre.** Un evento `InternoCaratteristicaOsservata` parte in tre punti, e il registro lo trasforma in una scoperta solo se la classe ha davvero quella
caratteristica (un colpo che fa il danno normale non scopre niente):

- un colpo di un certo tipo di danno va a segno (`StatoDelCombattimento.registraColpo`, chiamato da `ApplicazioneDelRisultato.applica`);
- un incantesimo o un'azione viene respinta per immunità (`StatoDelCombattimento.registraEsitoAzione` con `EsitoAzione.IMMUNE`); contro un'immunità a tutti gli
  incantesimi ne basta uno;
- un effetto di stato viene rifiutato perché il bersaglio è immune (`PersonaggioBase.addEffettoDiStato`).

Le scoperte valgono anche per una voce ancora chiusa: la scheda le mostra solo da sbloccata.

## 4. La scheda e la schermata

`Comando.MOSTRA_BESTIARIO` (nell'inventario, accanto a `MOSTRA_TROFEI` e `MOSTRA_TATTICHE`: con 5 personaggi i comandi sono 9) porta a `Stato.BESTIARIO`
(`entraInStatoBestiario`), che pubblica `RichiestaAperturaBestiario`; l'unico comando è `ANNULLA`, che torna all'inventario. La UI legge tutto da `VistaPartita`
(`isVoceDelBestiarioSbloccata`, `getCaratteristicheScoperte`, `getMoltiplicatoriDiClasse`, `getAbilitaInnate`), mai dai singoli del motore.

`DisplayableCanvasBestiario` (`ui/schermate`) ha a sinistra l'elenco (un `ScorrimentoVerticale`, una riga per voce con l'icona e il nome, "???" e l'icona in
silhouette se chiusa; l'icona è per tutte l'immagine della classe ridotta a 32 px, anche per chi ha un'icona disegnata a mano, che per ora qui non si usa) e a destra la scheda della voce scelta con un clic; in alto "n/33". L'ombra del drago è
dietro, più tenue che nei trofei. La rotella scorre il pannello sotto il mouse (a passi piccoli e consegnati a uno o due per fotogramma, vedi
`Finestra.passiPerScattoDellaRotella`). Le due immagini (`ImmagineBestiario`) sono composte come quella dei trofei.

La scheda di una voce aperta, dall'alto:

1. il nome nell'alfabeto grande e le **immagini**: per i ruoli con due forme la femmina a sinistra, rivolta a destra, e il maschio a destra, rivolto a sinistra, così
   che si guardino (`ClassePersonaggioImmagine.getImmagine(classe, verso)`); chiusa, le silhouette (`ImageCache.sagomaScura`, `getSagomaDelPersonaggio`);
2. la **descrizione**;
3. le **abilità innate** (`AbilitaInnateDiClasse`, che raccoglie quelle di chi le ha: le sei di Chierico e Druido, i tre canti del Bardo, l'Alba Sacra e la Luce
   Divina del Sacerdote, il dardo arcano di Mago ed Elfo), lette dal codice di chi le definisce;
4. **forze e debolezze**: le caratteristiche scoperte (le debolezze in rosso, resistenze e immunità in verde) e quante ne restano da scoprire, senza dire di che
   tipo (o "Niente di particolare" per chi non ne ha);
5. il **carattere**: i moltiplicatori della classe (carico massimo, critico, precisione... danni magici e fisici, rigenerazioni; `MoltiplicatoriDiClasse`, letti dal
   personaggio modello) con la **nota** che spiega perché ha quel valore (`note_personaggi_it.properties`, `NoteDeiPersonaggi`): verde sopra la media e rosso sotto
   (per la stanchezza, dove un valore alto è un difetto, al contrario). Un moltiplicatore **a zero** non si mostra (l'Arpia non sa contrattare). Se la forma
   maschile e quella femminile hanno **valori o note diversi** si mostrano tutti e due ("Maga x0,75 · Mago x0,8"); se sono uguali, una volta.

## 5. Dati, file e trofeo

- `BestiarioMD` (`modellodati`, sul modello di `TrofeiMD`, fuori da `ModelloDati` e mai reimpostato): le voci sbloccate (per ruolo) e, per ruolo, le chiavi delle
  caratteristiche scoperte. Nel file `bestiario` di `~/.foresta/` una riga per voce che ha qualcosa da dire: `RUOLO|sbloccata|chiave§chiave`. Una riga di una
  classe che non esiste più si salta; un file rovinato si mette da parte (`bestiario.rovinato`).
- `GestoreBestiario` (`foresta-contratti`), `GestoreBestiarioSuFile` (`salvataggi`, passato a `RegistroBestiario.impostaGestoreBestiario` da `Main`); nei test
  `GestoreBestiarioInMemoria`, collegato a `PartitaDiTest` (ogni test riparte con il bestiario vuoto).
- Il trofeo **Enciclopedista** (`TipoTrofeo.ENCICLOPEDISTA`, `TrofeoEnciclopedista`) si vince con tutte le 33 voci sbloccate. Il progresso non è quello del registro dei
  trofei (che si perde se il gruppo muore nella locazione) ma quanto sa il bestiario; si controlla a fine locazione, come gli altri. Il Perdigiorno, che chiede tutti gli
  altri trofei, ora chiede anche questo.
- L'icona del bottone è per ora la copia di quella del goblin (`img/icone/Bestiario.gif`).

## 6. Test

`BestiarioMDTest` (scrittura, lettura, ruoli), `GestoreBestiarioSuFileTest`, `CaratteristicheDiClasseTest` (immunità e voci di `TipoPersonaggio`, testi),
`MoltiplicatoriDiClasseTest`, `AbilitaInnateDiClasseTest`, `VociDelBestiarioTest` (ogni avversario di missioni e locazioni ha una voce, ogni avversario si incontra
davvero, quindi ogni voce si può sbloccare), `ScenarioBestiarioTest` (sblocchi, scoperte reali con colpi, incantesimi e effetti, persistenza, comando, trofeo) e
`DisplayableCanvasBestiarioTest` (la schermata, e che tutti i testi si scrivano con i font del gioco).

## Osservazioni

- **Le due forme possono avere dati diversi**: Mago e Maga hanno oggi un carico massimo diverso, e il bestiario li mostra entrambi. Le **caratteristiche** (danni,
  incantesimi, effetti) e le **scoperte**, invece, sono per ruolo: la scheda legge quelle della classe al maschile e una scoperta vale per tutte e due. Se un giorno le
  due forme avranno resistenze diverse, servirà una chiave per forma.
- Le note delle forme femminili sono oggi copie di quelle maschili (`CANTASTORIE.*` come `BARDO.*`): ora che si leggono a schermo si può dare a ciascuna il suo tono.
- Un incantesimo respinto per immunità si scopre anche se il giocatore non vede un messaggio (`IncantesimoMaleficoImpl` lo scrive solo nel log); un effetto che il
  `CalcolatoreCombattimento` scarta prima di applicarlo (perché il bersaglio è immune) non si scopre: si scopre quando l'immunità viene davvero incontrata.
- I dati di `TipoPersonaggio` per le immunità vengono dagli override che stavano nelle classi dei personaggi (Scheletro, Lich, Idra, Drago...), tolti.

## Da fare / idee aperte

- Un'icona vera per il bottone, e icone piccole disegnate per tutte le voci, da usare al posto dell'immagine ridotta (oggi per tutte è l'immagine ridotta).
- Avvisare il giocatore quando una voce si sblocca (l'evento `InternoVoceDelBestiarioSbloccata` c'è già, la UI non lo usa).
- Mostrare nella scheda quanti ne ha sconfitti e incontrati, e gli incantesimi dei mostri con un repertorio (`getRepertorioIncantesimi`).
- Se i non combattenti o l'`OMBRAFIAMMA` dovranno avere una voce, e come si sbloccano.
- Il nome della voce per i ruoli con due forme è "Mago e Maga": decidere se va bene.
