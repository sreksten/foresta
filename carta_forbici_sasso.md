# Carta, forbici e sasso

La sfida a carta, forbici e sasso e la missione che ne nasce, **La sfida dei campioni**. Stato attuale; la storia è in git.

## 1. Panoramica

Quando il gruppo stringe amicizia con degli avversari (`LocazioneBase`, stato `CHI_FA_AMICIZIA`), nel 33% dei casi
(`Costanti.PERCENTUALE_PROBABILITA_CARTA_FORBICI_SASSO`) invece di un'offerta l'avversario sfida il capo del gruppo a carta,
forbici e sasso. Si gioca **a tre mani vinte** (`Costanti.VITTORIE_PER_VINCERE_LA_SFIDA`), con i soli comandi `CARTA`,
`FORBICE` e `SASSO`; un pareggio non conta e si rigioca. Ogni sfida vinta dal giocatore si conta per tutta la partita
(`GruppoGiocatoreMD.sfideVinte`, salvato): alla **terza** (`Costanti.SFIDE_VINTE_PER_IL_TORNEO`) l'avversario lo manda
al torneo e parte la missione `LaSfidaDeiCampioni`. **Da quel momento nessuno sfida più**: la sfida per amicizia serve
solo da innesco, e `LaSfidaDeiCampioni.isPartita()` (la missione è una sola per partita, in corso, finita o fallita)
la spegne, senza nemmeno tirare il dado.

| Cosa | Dove |
| :--- | :--- |
| Le tre mosse, chi batte chi | `tipi/MossaCartaForbiciSasso` |
| La partita a tre mani vinte | `motore/PartitaCartaForbiciSasso` |
| Innesco, stato, esito | `locazioni/LocazioneBase` (`StatoLocazione.SFIDA_CARTA_FORBICI_SASSO`, `iniziaLaSfida`, `giocaUnaMano`, `concludiLaSfida`) |
| Il riquadro | `ui/DisplayableCanvasRiquadroSfida`, evento `eventi/interni/InternoSfidaCartaForbiciSasso` |
| Le frasi | `motore/sfide.txt`, `ProduttoreDiTestiCasuale.fraseDiSfida` e le altre |
| La missione | `missioni/LaSfidaDeiCampioni`, `missioni/IlCampione` |
| Il trofeo | `TipoTrofeo.RE_DI_CARTA_FORBICI_E_SASSO`, `ClasseTrofeo` |

## 2. La sfida in locazione

La sfida **non è un intermezzo** (gli intermezzi non sono interattivi e scattano una volta sola): è un sottostato della
locazione, come `CHI_DUELLA`. Sfondo e avversari sono quelli della locazione; l'avversario parla con una
`NotificaTestoFrase` ("L'Idra dice: \"…\"").

- **Innesco.** Nel ramo di successo di `CHI_FA_AMICIZIA`, dopo `InternoAmiciziaStretta` e prima di `getOfferta`: se
  `LaSfidaDeiCampioni.isPartita()` è falso, `Dado.tira(100) <= 33` → `iniziaLaSfida()`. Il conto di `AMICO_DI_TUTTI` quindi
  avanza lo stesso.
- **Chi non gioca.** L'ombrafiamma è un personaggio segreto, per ora escluso (`MossaCartaForbiciSasso.puoGiocare`): non sfida mai e non è mai un campione.
- **I comandi.** In `SFIDA_CARTA_FORBICI_SASSO` `impostaComandiPossibili` offre solo `CARTA`, `FORBICE`, `SASSO`: non c'è
  `ANNULLA`, né fuga, né mappa, né pozioni. Un altro comando riceve le stesse tre mosse. La barra icone non cambia disegno.
- **Una mano.** `PartitaCartaForbiciSasso.gioca` tira la mossa dell'avversario con `Dado.tira(3)` (il dado della partita:
  1 carta, 2 forbici, 3 sasso) e pubblica `InternoSfidaCartaForbiciSasso.mano(...)`.
- **La fine** (`concludiLaSfida`): l'avversario commenta ("Poffarre! Non avevo mai incontrato un giocatore forte come te!" se
  vince il giocatore, "Heh! Sono sempre il più forte a questo gioco." se perde) e la locazione **finisce**, vinta o persa,
  come dopo un'offerta accettata: `setCompleta(true)`, `haStrettoAmicizia` vero, l'oggetto non si prende. L'oggetto non preso sparisce **subito**, appena l'amicizia riesce (`setOggetto(null)` in `CHI_FA_AMICIZIA`, come già per la corruzione), quindi non resta disegnato in una locazione che si svuota; vale per ogni amicizia, non solo per la sfida. Se è la terza
  vittoria, l'avversario aggiunge la frase del torneo e `LaSfidaDeiCampioni.avvia()` fa partire la missione.
- **Il salvataggio.** Lo stato della sfida non si salva: si gioca fino a tre mani vinte e solo dopo si può salvare (durante
  la sfida non ci sono i comandi per farlo). Si salva solo il conto delle sfide vinte.

### La UI

`DisplayableCanvasRiquadroSfida` (`TipoFinestra.SFIDA`) sta al centro dell'area di contenuto: la cornice larga
(`ImageCache.corniceLarga`, `fondinon2x2/CorniceLarga.gif`) è lo **sfondo** e le mani (`img/fondi/<Carta|Forbici|Sasso>-sx.gif`
per il giocatore, a sinistra, e `…-dx.gif` per l'avversario, a destra, con la trasparenza) si disegnano **sopra**; in alto
il punteggio di ognuno, con `TestoGrande` e il suo alone (`conAlone`). Le immagini si caricano e si scalano al primo uso,
qualunque siano le loro dimensioni: il riquadro occupa **al massimo metà della larghezza e metà dell'altezza** dell'area di
contenuto (`fattoreDiScala`, mai un ingrandimento). Si disegna dopo tutte le altre finestre (nessuna lo copre).

- **Mentre il giocatore sceglie** le mani, due sassi, si muovono "bim bum bam": su e giù e avanti e indietro, con due
  sinusoidi piccole (`spostamentoOrizzontale` a `FREQUENZA_ORIZZONTALE`, 1 al secondo, e `spostamentoVerticale` al
  doppio, ampiezza del 4% dello spazio dentro la cornice), la sinistra e la destra in senso opposto. Si muovono attorno a
  una posizione **spostata verso il bordo** (`SPOSTAMENTO_VERSO_IL_BORDO`: ognuna verso il suo lato, fino al bordo e un po' oltre
  (il 2% dello spazio)): ci arrivano con una salita dolce (`avvicinamentoAlBordo`, 0,8 s) a ogni ripartenza, partendo da
  ferme al centro. Il movimento è ritagliato allo spazio dentro il bordo nero della cornice (`BORDO_DELLA_CORNICE`, 20 px
  per lato): le mani possono quindi sovrapporsi al bordo, ma non si vedono sopra la cornice. Il canvas ridisegna a ogni
  fotogramma finché si è in gioco: il tempo si legge da `System.nanoTime()`.
- **Dopo la scelta** (`InternoSfidaCartaForbiciSasso`, non `isInizio`) le mani si fermano al centro e mostrano le due
  mosse per `SECONDI_DI_PAUSA_DOPO_UNA_MANO` (mezzo secondo), poi tornano due sassi che si muovono, con il punteggio nuovo.
  Se si sceglie di nuovo durante la pausa, la mano nuova prende il posto.
- **All'ultima mano** (`isFinale`) `DisplayableCanvas.dissolviLaSfida` lo trasforma in uno `SpriteInDissolvenza` (fermo
  e intero per 1,5 s, poi sfuma in 1 s) e lo nasconde, perché non copra la mappa mentre si sceglie la direzione;
  `InternoPreparazioneLocazione` lo nasconde comunque.

## 3. La missione La sfida dei campioni

`LaSfidaDeiCampioni` (`ClasseMissione.LA_SFIDA_DEI_CAMPIONI`) è una missione secondaria senza mandante in città: la
fa partire la terza sfida vinta (`LaSfidaDeiCampioni.avvia()`), che fissa il livello del mondo di quel momento e il
leggendario in palio, pubblica la notifica "NUOVA MISSIONE" e **subito**, senza aspettare il prossimo controllo, affida
quattro missioni `IlCampione` (`affida` di `MissioneAPassi`) e fa trovare a ognuna il suo posto: i quattro segnalini
sono già sulla mappa quando la sfida finisce. Poi ne aspetta la fine:

1. `CAMPIONI`, a fine locazione, subito: i quattro campioni sono affidati;
2. `ATTESA`, a fine locazione: i quattro sono battuti (i figli si controllano prima della madre, `OrdineVisita.FIGLI_PRIMA`:
   la ricompensa arriva **sul posto del quarto campione**);
3. `RICOMPENSA`, a fine locazione, subito: `Ricompensa.inMonete(100 × livello del mondo)` più il leggendario.

**I campioni.** Ognuno è un `IlCampione` (`ClasseMissione.IL_CAMPIONE`): un avversario scelto a caso fra le classi con cui
si può stringere amicizia (`LaSfidaDeiCampioni.classiAmichevoli`, cioè `Personaggio.isAmichevole`), con un nome di
`missioni.txt` (`NOME_CAMPIONESSA` se è una femmina, altrimenti `NOME_CAMPIONE`, gli stessi dei duelli), in un tipo di posto
diverso dagli altri tre fra grotta, rovine, bosco, palude e radura (`CombattimentoRichiesto.LUOGHI`). Ognuno rivendica la sua
locazione (`cercaLocazione`), quindi i **quattro posti compaiono sulla mappa**, ciascuno come il segnalino di una missione
sola: nessun cambio all'infrastruttura dei segnalini. I passi:

- `POSTO`, in locazione, subito: il posto è segnato;
- `SFIDA`, a fine locazione, nel posto: la locazione è completa. Il passo mette lì l'avversario con
  `IncontroDiMissione.aCartaForbiciSasso()` (come `aDuello`: un avversario solo).

Quando il gruppo entra nel posto, `LocazioneBase.gestisciNuovaLocazione` vede `Gruppo.isSfidaACartaForbiciSasso()` (il flag
`Personaggio.isSfidanteACartaForbiciSasso`, che non si salva) e comincia **subito** la sfida, senza offerte né altre
azioni; `Automa.entraInStatoPreparazioneLocazione` toglie anche ogni oggetto dalla locazione (suo o di altre missioni). Vinta, la locazione è completa e si va via normalmente (nessun oggetto); **persa, la locazione non è completa**:
resta non visitata, il campione resta lì, e si può tornare a riprovare. La missione non fallisce mai. Questa sfida non
conta fra quelle del torneo.

**Il leggendario.** Quello del catalogo che nessuna leggenda e nessun torneo ha già messo in palio (`PescaLeggendaria`, la
missione è una `ConLeggendario`); se il catalogo è finito, uno costruito al volo, un anello di livello del mondo + 2
(`LaSfidaDeiCampioni.costruisciUnLeggendario`, con un nome di `sfide.txt`, `NOME_LEGGENDARIO_DEI_CAMPIONI`).

**Il trofeo.** `RE_DI_CARTA_FORBICI_E_SASSO` (supertipo `MISSIONE`) si vince completando la missione.

## 4. I testi

`motore/sfide.txt` (formato di `GrammarBean`) ha le produzioni `SFIDA`, `GIOCATORE_VINCE`, `GIOCATORE_PERDE`, `TORNEO` e
`NOME_LEGGENDARIO_DEI_CAMPIONI`; `ProduttoreDiTestiCasuale` ha un metodo per ognuna.

## 5. I test

`PartitaCartaForbiciSassoTest` (regole), `ScenarioCartaForbiciSassoTest` (l'innesco, le mani, il pareggio, il torneo e lo spegnersi
dell'innesco), `ScenarioSfidaDeiCampioniTest` (i quattro posti, l'arrivo dal campione con le sole tre mosse, la vittoria,
la sconfitta, la ricompensa, il salvataggio), `RiquadroSfidaTest` (nomi delle immagini). `GiocatoreAutomatico` risponde
alla sfida con una mossa a caso. Il test sull'amicizia di `TrofeiTest` trucca anche il dado della sfida (`Dado.trucca(1, 100)`).

## Osservazioni

- La sfida del torneo e `IlTorneo` (il torneo di combattimento in città) sono cose diverse: la frase dell'avversario parla di
  "un torneo che sta per cominciare" ma non c'entra con quello.
- La missione non ha un tetto di tempo né un ordine: i quattro campioni si battono quando e come si vuole.
- I campioni non hanno aspetto proprio: sono avversari come gli altri, con il loro nome.

## Da fare / idee aperte

- Un'animazione delle mani (le immagini non ci sono): oggi il riquadro mostra solo la mano scelta.
- Un'immagine o un'icona per il trofeo, se l'elenco dei trofei le userà.
- Misurare con `SimulazionePartiteTest` l'effetto della ricompensa (100 × livello del mondo e un leggendario) sull'economia: vedi
  [`economia.md`](economia.md).
- Una frase di sfida con il nome dell'avversario o del campione, se la grammatica lo permetterà.
