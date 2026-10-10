# Modificatori temporanei

Piano di lavoro (2026-10-10) per dare un tempo ai `ModificatoreAttributoMD` e per far sì che un'arma possa applicarli al bersaglio. Nulla di quanto segue è ancora nel codice, salvo dove dice il contrario. Quando il lavoro sarà fatto, il documento va riscritto come descrizione dello stato attuale (la storia è in git).

## 1. Obiettivi

- Un modificatore può **durare un tempo**: poi se ne va da solo, come un effetto di stato.
- Il tempo si misura in **turni di combattimento** oppure in **locazioni**.
- Un modificatore ha un **bersaglio**: chi lo porta o chi viene colpito. Quelli sul bersaglio hanno senso solo sulle **armi**.
- `ALBA_SACRA` toglie anche i modificatori temporanei, così i malus a tempo si curano e il Bardo potrà dare al gruppo bonus a tempo (per esempio un `CANTO_DI_VALORE`).
- Il `FIXME` della fuga (`PersonaggioBase.fugge`) diventa un malus a tempo di coraggio, valore e carisma.

## 2. Com'è oggi

- `ModificatoreAttributoMD` è immutabile: attributo, tipo, quantità, nota. Sta in `PersonaggioMD.modificatori` (benedizioni, lealtà, `addSalute`/`addMagia`) e in `ArtefattoMD.modificatori`; i salvataggi li scrivono in `PersonaggioMD` (`|` e `§`) e in `ArtefattoMD` (una riga per modificatore).
- `PersonaggioBase.getQuantitaModificata` unisce i modificatori locali con quelli di **tutti** gli artefatti dell'inventario personale: `QUANTITA_ASSOLUTA` (vince il minimo), poi la somma dei fissi, poi la somma delle percentuali.
- Gli effetti di stato scalano in `LocazioneBase.trascorriTurnoEffettiDiStato` (`riduciEffettiDiStato`, per gruppo e avversari). Le armi li mettono in `DannoRisultante.effettiDiStatoDaAggiungere` (`CalcolatoreCombattimento`, sezione 6) e `PersonaggioBase.applicaRisultatoCombattimento` li applica al bersaglio.
- I personaggi a tempo scalano in `Automa.entraInStatoFineLocazione2`, solo se vivi: lo stesso punto serve per le locazioni.
- `AlbaSacra.formula` chiama `rimuoviTuttiGliEffettiDiStato` su ogni personaggio di `gruppoBersaglio`.

## 3. Decisioni

| Tema | Decisione |
| :--- | :--- |
| Nomi dei tipi | `AUMENTO_FISSO` → `VARIAZIONE_FISSA`, `AUMENTO_PERCENTUALE` → `VARIAZIONE_PERCENTUALE` (fatto il 2026-10-10). `QUANTITA_ASSOLUTA` resta. |
| Unità del tempo | Turni di combattimento o locazioni. Un'enum `UnitaDurata { PERMANENTE, TURNI, LOCAZIONI }` più una `durata` intera (0 solo per `PERMANENTE`). |
| Bersaglio | Un'enum `BersaglioModificatore { PORTATORE, BERSAGLIO }`; il default è `PORTATORE`. |
| Dove vale `BERSAGLIO` | Solo sulle armi. Vale per entrambe le mani: applica il modificatore l'arma che sferra il colpo. |
| Se il modificatore si applica | Stessa formula degli effetti di stato degli incantamenti elementali (vedi §5). |
| Quando scade | Resta finché non scade e passa da una locazione all'altra. I turni scalano nei combattimenti, le locazioni a fine locazione. |
| Alba Sacra | Toglie tutti i modificatori temporanei dai personaggi del gruppo bersaglio, malus e bonus. |
| Sintassi nei file | `MOD=FORTUNA =0 @bersaglio 3t` (`t` turni, `l` locazioni); senza `@bersaglio` e senza durata è come oggi. |
| Salvataggi | Niente retrocompatibilità. |

## 4. Modello

- `ModificatoreAttributoMD` guadagna `unitaDurata`, `durata` e `bersaglio`, tutti nell'`equals`/`hashCode`. Resta immutabile: chi lo fa scalare ne sostituisce la copia con `conDurata(int)`. I costruttori attuali restano (permanente, sul portatore).
- Sull'**artefatto** la durata è quella con cui il modificatore sarà applicato (un colpo di arma, per esempio "3 turni"); l'artefatto non scala mai. Sul **personaggio** è il tempo che resta, e scala.
- `PersonaggioMD` e `ArtefattoMD` scrivono e rileggono i tre campi a fine riga (il test di andata e ritorno controlla che la durata rimasta torni uguale).
- `RegoleSetLeggendari.applica` copia i nuovi campi e non moltiplica mai un modificatore sul bersaglio (non è un bonus di chi indossa il set).
- Il rinomino dei due tipi (fatto) ha toccato 29 file, fra cui `missioni.txt` (`BENEDIZIONE=`, `LEALTA=`), `ModificatoreDellaRiga`, `OggettoLeggendario`, `ListinoPergamene`, `Anello`, `RegistroArtefatti`, `Automa`, `GeneratoreArtefattiTabelle`, le due schermate che mostrano i modificatori e i test; i salvataggi di prova in `src/test/resources`. Fatto per primo, da solo, con un commit a parte.

## 5. Comportamento

### 5.1 Calcolo

- `getQuantitaModificata` ignora i modificatori `BERSAGLIO` degli artefatti (non contano per chi li porta). I modificatori temporanei sul personaggio passano da lì come gli altri, quindi "FORTUNA porta a 0" è una `QUANTITA_ASSOLUTA` come oggi.
- Più modificatori uguali (stesso attributo, tipo e nota) **non si sommano**: il nuovo rinfresca la durata se è più lunga, come `addEffettoDiStato`.
- Un metodo privato di `PersonaggioBase` ricalcola salute e magia massime e gli attributi secondari (oggi il ricalcolo sta dentro `addModificatore`) e lo usano anche la scadenza e la rimozione; la salute e la magia attuali non vanno sopra i nuovi massimi.

### 5.2 Scorrere del tempo

- **Turni**: nello stesso punto di `riduciEffettiDiStato` (`LocazioneBase.trascorriTurnoEffettiDiStato`, gruppo e avversari) si scalano di 1 i modificatori a turni. Fuori dal combattimento non scalano e restano per il prossimo scontro.
- **Locazioni**: in `Automa.entraInStatoFineLocazione2`, accanto ai personaggi a tempo e solo per i vivi, si scalano di 1 quelli a locazioni.
- A zero il modificatore sparisce, si ricalcolano gli attributi e parte una notifica (generalizzare `NotificaVariazioneEffettoDiStatoPersonaggio` o aggiungerne una nuova) che `SnifferBusEventi` e il riquadro di testo conoscono.
- **Fuga.** La fuga finisce in `FINE_LOCAZIONE` e quindi passa dal decremento: il malus va applicato con una durata più alta di uno (o il decremento saltato per la locazione corrente), perché l'ignominia pesi per tre locazioni *dopo* quella della fuga. Da fissare in un test. Il malus (`VARIAZIONE_PERCENTUALE` negativa su `CORAGGIO`, `VALORE`, `CARISMA`, valori in `Costanti`) si mette a ogni personaggio vivo in `PersonaggioBase.fugge`. L'aggiustamento della salute in base al livello resta un'altra faccenda.

### 5.3 Le armi

- Dopo il blocco degli stati degli incantamenti (sezione 6 di `CalcolatoreCombattimento`), per ogni modificatore `BERSAGLIO` dell'arma che colpisce si fa lo stesso tiro degli incantamenti elementali (`probStatoMagico`: danno del colpo contro la forza del difensore più l'intelligenza pesata dal potere magico). Se riesce, `DannoRisultante.addModificatoreDaAggiungere` mette una copia sul portatore, con la sua durata.
- Il danno del colpo che serve al tiro è quello complessivo di quell'arma (i modificatori non hanno un elemento).
- `PersonaggioBase.applicaRisultatoCombattimento` applica i modificatori con `addModificatore`, tenendo il più lungo se ce ne sono due uguali.
- **Immunità.** Gli effetti di stato hanno `isImmuneAEffetto`; per i modificatori serve una regola a parte (da decidere: un bersaglio immune al tipo di danno dell'arma lo è anche ai suoi modificatori, oppure non c'è immunità).
- Le armi secondarie fanno lo stesso: nessun caso a parte.

### 5.4 Alba Sacra

`AlbaSacra.formula` chiama anche `rimuoviTuttiIModificatoriTemporanei()` su ogni personaggio del gruppo bersaglio (`PersonaggioBase`, rimuove i modificatori con unità diversa da `PERMANENTE`, ricalcola e notifica). La benedizione, la lealtà e i bonus permanenti restano.

### 5.5 Regole degli oggetti

- **Dove stanno**: un modificatore `BERSAGLIO` si può mettere solo su un'arma, in `ArtefattoMD.addModificatore` e in `GeneratoreArtefattiTabelle`; l'incantatore (`RegoleIncantatura.trasferibili`) non lo trasferisce su altro, con l'avviso degli effetti persi come per il `POTERE_MAGICO`. Un modificatore a tempo sul **portatore** non si limita alle armi, ma nel generatore non serve (vedi §8).
- **Limite degli effetti**: conta come qualsiasi modificatore.
- **Prezzo** (`ListinoPergamene.prezzo(modificatore)`): oggi si paga il valore dell'attributo; un modificatore sul bersaglio vale il suo effetto sul bersaglio ma è soggetto al tiro e dura poco, quindi un coefficiente (per esempio 0,5 × durata in turni fino a un tetto) da tarare con le altre misure. Da fissare in `Costanti`.

## 6. Sintassi

Nei file di testo (`leggendari.txt`, grammatica degli artefatti, `missioni.txt`):

```
MOD=FORTUNA =0 @bersaglio 3t       porta a 0 la fortuna del bersaglio per 3 turni
MOD=CORAGGIO -20% 3l               -20% di coraggio di chi lo porta per 3 locazioni
MOD=FORZA +100%                    come oggi, permanente sul portatore
```

L'`=` è `QUANTITA_ASSOLUTA`, `%` la variazione percentuale, il resto la variazione fissa. Il parser di `OggettoLeggendario` e `ModificatoreDellaRiga` si estende con i due campi facoltativi. In grammatica (`<mod:FORZA+1>`) non cambia nulla all'inizio: i modificatori a tempo si scrivono a mano nei leggendari.

## 7. UI

- Rivelazione artefatto e schermate di scambio (`SpriteRivelazioneArtefatto`, `DisplayableCanvasScambiatoreArtefatti`): "Fortuna: porta a 0 sul bersaglio per 3 turni".
- Scheda del personaggio e riquadro dello stato: i modificatori temporanei attivi con il tempo che resta, accanto agli effetti di stato, passando da `VistaPersonaggio` (la UI non importa `modellodati` oltre a quanto già fa; da controllare con `DipendenzeUITest`).
- Le notifiche di aggiunta e scadenza arrivano nel testo di gioco.

## 8. Fasi

1. [x] **Rinomino** dei due tipi in tutto il repo (codice, risorse, test, `artefatti_e_incantamenti.md`), 2026-10-10. Suite verde.
2. **Modello e salvataggi**: i tre campi, `equals`, `conDurata`, scrittura e lettura; test di andata e ritorno.
3. **Calcolo e scadenza**: filtro del bersaglio, nessuna somma di doppioni, ricalcolo, scalare per turni e per locazioni, notifiche, test.
4. **Alba Sacra** e **fuga** (con il test sulla durata di tre locazioni).
5. **Armi**: tiro, `DannoRisultante`, applicazione, immunità; regole di dove può stare un modificatore sul bersaglio.
6. **Sintassi, prezzo, incantatore**; un paio di leggendari di prova (per esempio una Spada del Tristo Raccolto che porta a 0 la fortuna).
7. **UI**.
8. **Documenti**: questo (da piano a descrizione), `artefatti_e_incantamenti.md`, `motore_di_gioco.md` §5 e §7, `interazioni_effetti_di_stato.md`, voce in `foresta.md` e `CLAUDE.md`. Aggiornare il conteggio dei test.

Le fasi 1-4 sono indipendenti dalle armi e possono andare da sole.

## 9. Test da scrivere

- Rinomino: tutti i test esistenti verdi senza altre modifiche.
- Salvataggio di personaggio e artefatto con durata, unità e bersaglio.
- Scadenza a turni (non scala fuori combattimento, scala per gruppo e avversari) e a locazioni (scala solo ai vivi, passa fra locazioni).
- `QUANTITA_ASSOLUTA` a tempo che torna al valore normale alla scadenza.
- Doppione: la durata si rinfresca, non si somma.
- Arma con `BERSAGLIO`: il portatore non ne risente, il bersaglio sì (tiro forzato), anche con l'arma della mano secondaria.
- Accessorio o armatura con `BERSAGLIO`: rifiutato. Incantatore: non lo trasferisce, avvisa.
- Set leggendario: nessun moltiplicatore sul bersaglio.
- Alba Sacra: toglie i temporanei, non i permanenti.
- Fuga: malus per le tre locazioni dopo la fuga.

## Osservazioni

- `ALBA_SACRA` così toglie anche i bonus temporanei dei compagni (un `CANTO_DI_VALORE` finirebbe appena si cura un avvelenamento). Se si vuole evitarlo, basta un campo `benefico` o distinguere per segno della variazione; per ora la decisione è tutto, come detto.
- La durata in locazioni conta solo i personaggi vivi, come quelli a tempo: un compagno morto tiene il malus finché non risorge.
- Un `CANTO_DI_VALORE` sarà un incantesimo o una capacità del Bardo, non un'arma: serve un modo di dare un modificatore a tempo al gruppo da un incantesimo (`PersonaggioBase.addModificatore` già lo permette).

## Da fare / idee aperte

- Immunità ai modificatori sul bersaglio (§5.3).
- Coefficiente di prezzo dei modificatori a tempo (§5.5) e valori della fuga (§5.2).
- Il Bardo e il `CANTO_DI_VALORE`: è un capitolo a parte.
- Missioni (`BENEDIZIONE=`, `LEALTA=`) con una durata facoltativa: ora hanno solo modificatori permanenti.
- Grammatica degli artefatti con modificatori a tempo sul bersaglio.
