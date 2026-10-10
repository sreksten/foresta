# Il Bardo

Piano di lavoro (2026-10-10) per dare al Bardo e alla Cantastorie tre capacità innate, i **canti**, sul modello del dardo arcano di Mago ed Elfo e dell'alba sacra di Sacerdote e Sacerdotessa; e per fare del Viandante una classe a sé. Nulla di quanto segue è ancora nel codice. Quando il lavoro sarà fatto, il documento va riscritto come descrizione dello stato attuale (la storia è in git). Le tre icone (`CantoCurativo.gif`, `CantoIpnotico.gif`, `CantoDiValore.gif`) ci sono già in `img/icone`.

## 1. Obiettivi

- Il Bardo (e la Cantastorie) ha tre capacità che costano `MAGIA`, non consumano pergamene, sono fra le scelte del comando incantesimi, occupano il turno e fanno rispondere l'avversario su chi le usa.
- Il gioco ha finalmente un curatore, un modo di colpire il morale del nemico e un modo di rafforzare il gruppo.
- Il Viandante e i personaggi non combattenti non sono più dei Bardi: hanno le loro caratteristiche, e nessuna capacità del Bardo gli arriva per eredità. Il `BardoLocanda` invece è un bardo a tutti gli effetti.

## 2. Com'è oggi

- **Dardo arcano** (`incantesimi/DardoArcano`): un'`Arma` che non sta in `ClasseIncantesimo`, perché non è una pergamena. `conosciutoDa(TipoPersonaggio)` e `puoLanciarlo(Personaggio)` (vivo, conosce, ha la MAGIA, non è `SILENZIATO`) decidono chi lo lancia; `Automa.entraInStatoSceltaIncantesimoDaLanciare` lo aggiunge ai comandi; `LocazioneBase.lanciaDardoArcano` lo lancia; `LocazioneBase.impostaComandiPossibili` lo conta per abilitare il comando `INCANTESIMO`.
- **Alba sacra** (`incantesimi/AlbaSacra`): invece è un `Incantesimo` di `ClasseIncantesimo`, e chi la conosce per natura non ne consuma le pergamene.
- Le capacità dipendono dal **`TipoPersonaggio`** (`conosciutoDa(classe)`), non dalla classe Java: per questo `instanceof Bardo` non esiste da nessuna parte, e `PersonaggioNonCombattente`, `BardoLocanda` e `Viandante`, che estendono `Bardo`, non hanno il dardo né l'alba.
- Il Bardo ha già la MAGIA (`BARDO_MAGIA_BASE` 40, `BARDO_LIVELLAMENTO_MAGIA` 18).
- Gli effetti di stato (`TipoEffettoDiStato`) nascono da `addEffettoDiStato`, che ne rinfresca la durata se ce n'è già uno uguale (vedi `PersonaggioBase`), e scalano ogni turno in `LocazioneBase.trascorriTurnoEffettiDiStato`.
- `Viandante` estendeva `Bardo` (fase 1, fatta): è un ospite di missione (scorta) con le caratteristiche del bardo, amichevole, e può essere un campione nella sfida a carta, forbici e sasso (`LaSfidaDeiCampioni.classiAmichevoli()`).

## 3. Decisioni

| Tema | Decisione |
| :--- | :--- |
| Chi canta | Bardo, Cantastorie e bardo della locanda (`TipoPersonaggio.BARDO`, `CANTASTORIE`, `BARDO_LOCANDA`): il bardo della locanda è un bardo a tutti gli effetti. Nessun altro. |
| Costo | `MAGIA`, con costanti in `Costanti` da tarare: guarigione ~4, ipnotico ~3, valore ~5. |
| Quando | Solo in combattimento, con almeno un avversario vivo. Fuori dagli scontri (mappa, accampamento) i canti non sono offerti. |
| Turno | Ogni canto occupa il turno e l'avversario risponde (`rispostaAvversaria`), come il dardo arcano. Vale anche per il canto di valore a inizio scontro. |
| Silenzio | Un Bardo `SILENZIATO` non canta (come per il dardo e l'alba). |
| Più canti | Più Bardi o Cantastorie nello stesso gruppo vanno bene. Due canti di valore non si sommano: il secondo rinfresca la durata (e porta il bonus più alto), come ogni effetto di stato. |
| Canto curativo | Cura il solo personaggio **più ferito** del gruppo (quello con la quota di salute più bassa). Circa 30 × √livello, aumentato dalla sola Saggezza (fonte di conoscenza; il Carisma non c'entra). |
| Canto ipnotico | Applica a caso `SPAVENTATO` o `CONFUSO` a **fino a `NUMERO_BERSAGLI`** avversari vivi, con la Soggezione del Bardo contro il Coraggio di ciascuno. |
| Canto di valore | **+% di coraggio e di precisione** a tutto il gruppo, per qualche turno; il bonus è **20% + 2,5% per livello** del Bardo, con un tetto del **50%**, e non dipende dal suo Carisma. Solo un bonus, mai un malus. |
| Il canto di valore è un effetto di stato | Un `TipoEffettoDiStato` nuovo e proprio (nome provvisorio `ISPIRATO`), **non** un modificatore a tempo. |
| Alba Sacra | Non cambia: toglie tutti gli effetti di stato, `ISPIRATO` compreso, e tutti i modificatori a tempo (§6). |
| Viandante | Classe a sé: smette di estendere `Bardo`. |
| Non combattenti | `PersonaggioNonCombattente` (locandiere, armaiolo, alchimista, venditore di pergamene, incantatore, moglie del bardo) smette di estendere `Bardo`, come il Viandante. |
| `BardoLocanda` | Resta un `Bardo` a tutti gli effetti. |

## 4. Viandante e personaggi non combattenti

`Viandante` e `PersonaggioNonCombattente` (la classe astratta dei sei personaggi delle scene) estendono `PersonaggioBase` direttamente, con le loro caratteristiche e i loro moltiplicatori di classe. All'inizio i valori sono **gli stessi di oggi**, così missioni di scorta, scene e sfida dei campioni si comportano come prima; poi si possono tarare da soli.

- **Viandante:** costanti `VIANDANTE_*` uguali a quelle del Bardo. Ripete per conto suo ciò che prendeva da `Bardo` (`setCorrompibile(true)`, `setAmichevole(true)`, un solo esemplare nel gruppo).
- **Non combattenti:** costanti `NON_COMBATTENTE_*` che oggi sono "caratteristiche del bardo × `NON_COMBATTENTE_FATTORE_STATISTICHE`": diventano valori propri con lo stesso risultato. `PersonaggioNonCombattente` tiene il suo `impostaValoriDiPartenza` (non corrompibile, non amichevole) e le sue salute e magia; i sei personaggi (`Locandiere`, `Armaiolo`, `Alchimista`, `VenditoreDiPergamene`, `Incantatore`, `MoglieDelBardo`) non cambiano.
- **`BardoLocanda`** resta sotto `Bardo`: è un bardo con un'altra immagine e un'altra icona. Ha `TipoPersonaggio.BARDO_LOCANDA`, che `CantoDelBardo.conosciutoDa` include: come bardo, canta.
- Nessuna deroga nel codice dei canti: le capacità sono legate al `TipoPersonaggio`, non alla classe Java.
- `FabbricaPersonaggi` costruisce tutti come prima, e tutto ciò che li nomina per tipo (`ClasseIcona`, `ClassePersonaggioImmagine`, `VersiDeiPersonaggi`, `LanciatoreDeiDadi`, `PersonaggioBase`) resta com'è.
- Test da tenere verdi: `ScenarioOspitiVulnerabiliTest`, `ScenaInCittaConOspiteTest`, `ScenarioNonSparateSulPianistaTest`, `ScenarioCombattiScortaConsegnaTest`, `ClassePersonaggioImmagineTest`, `PersonaggiNonCombattentiTest`.
- Il punto sul Viandante in `todo.md` ("valutare se farlo estendere `PersonaggioNonCombattente`... o escluderlo") si può togliere.

## 5. Piano

1. [x] **Viandante e non combattenti a sé** (2026-10-10): `Viandante` e `PersonaggioNonCombattente` estendono `PersonaggioBase`, con `VIANDANTE_*` e `NON_COMBATTENTE_*` in `Costanti` (i valori di prima: le massime dei non combattenti sono ora i numeri del bardo ×0,85, e `NON_COMBATTENTE_FATTORE_STATISTICHE` non c'è più). Gli articoli e il genere maschili sono ora in `PersonaggioNonCombattente`. Test: `ViandanteENonCombattentiASeTest`; la suite è rimasta verde.
2. [x] **Interfaccia `Canto`** (2026-10-10), in `incantesimi`: `Canto` (`getTipo`, `getCostoLancio`, `boolean canta(cantore, gruppo, gruppoAvversario)`, che scala la MAGIA e dice se il canto c'è stato), `TipoCanto`, `FabbricaCanti`, `CantoDelBardo` (`conosciutoDa`, `puoCantare`, `cantiPossibili`: vivo, sa cantare, non `SILENZIATO`, MAGIA sufficiente) e le tre classi. `CantoCurativo` e `CantoIpnotico` hanno già i loro effetti (passi 5 e 6 sotto, fatti qui; per l'ipnotico `CalcolatoreCombattimento.calcolaDurataStato` è diventato pubblico); `CantoDiValore.canta` lancia `UnsupportedOperationException` finché non c'è `ISPIRATO` (passi 7 e 8). Costanti: `CANTO_*` in `Costanti`. Resta da fare il fatto che la sede "in combattimento e con un avversario vivo" la decide chi offre i canti (passo 4), non `puoCantare`. Test: `CantiDelBardoTest`.
3. **Comandi e icone**: tre `Comando` (`CANTO_CURATIVO`, `CANTO_IPNOTICO`, `CANTO_DI_VALORE`) e tre voci di `ClasseIcona` con le icone che ci sono già.
4. **Menu**: `Automa.entraInStatoSceltaIncantesimoDaLanciare` aggiunge i canti che il formulante può cantare; `LocazioneBase.impostaComandiPossibili` li conta per abilitare il comando `INCANTESIMO` (altrimenti il Bardo non lo vedrebbe se il gruppo non ha pergamene); `LocazioneBase.QUALE_FORMULA` ha un ramo `canta` come quello del dardo, con il testo di gioco, il costo in MAGIA, il turno consumato, la risposta avversaria e la fine dello scontro se gli avversari muoiono (per l'ipnotico non succede, ma il percorso è lo stesso).
5. **Canto curativo**: il personaggio vivo del gruppo con la quota `salute / saluteMassima` più bassa (a parità il primo); `addSalute(cura)` con `cura = CANTO_CURATIVO_BASE × √livello × (1 + SAGGEZZA / SAGGEZZA_TIPICA)` (la forma esatta si tara insieme alle costanti); testo "Il Bardo canta per X: guarisce di N".
6. **Canto ipnotico**: per ciascuno degli avversari vivi, fino a `NUMERO_BERSAGLI` (scelti a caso se sono di più), si tira contro il Coraggio con la Soggezione del Bardo; se il tiro riesce, `SPAVENTATO` o `CONFUSO` (uno a caso, lo stesso per tutti i bersagli del canto) con una durata calcolata come per gli altri stati (`calcolaDurataStato`). Un immune (`isImmuneAEffetto`) lo resiste da sé.
7. **Effetto di stato `ISPIRATO`** (nome da confermare): una voce di `TipoEffettoDiStato`, descrizione "Ispirato", e un campo decimale in `EffettoDiStatoMD` per **l'intensità** (la percentuale del bonus, che dipende dal livello di chi canta: 22,5, 25, ...), salvata con la partita. Nel conto degli attributi (`PersonaggioBase.quantitaModificata`) un personaggio con `ISPIRATO` ha, per `CORAGGIO` e `PRECISIONE`, una variazione percentuale in più pari all'intensità, sommata alle altre percentuali. Ricanti: se la durata è più lunga o l'intensità è più alta, rinfresca (`addEffettoDiStato` già lo fa per la durata; va esteso all'intensità), non raddoppia mai.
8. **Canto di valore**: a tutti i personaggi vivi del gruppo `addEffettoDiStato(ISPIRATO, durata, …)` con l'intensità `min(50, 20 + 2,5 × livello)` per cento (22,5% a livello 1, 50% dal livello 12) e una durata di 4 turni (da tarare).
9. **UI**: gli effetti di stato mostrano già lo sprite di aggiunta, di rinforzo e di rimozione, quindi `ISPIRATO` ne ha uno senza altro lavoro; il colore verde e il nome si decidono come per gli altri. Nella scheda del personaggio il coraggio e la precisione dovrebbero risultare verdi: `getVariazioneDaTemporanei` oggi guarda i soli modificatori, va estesa anche a questo effetto (vedi §6).
10. **Costanti e test**: le costanti (costi, cura base, durate, intensità), poi i test: comandi disponibili solo in combattimento e solo con la MAGIA, costo scalato, cura al più ferito e mai oltre il massimo, effetto ipnotico applicato o resistito, `NUMERO_BERSAGLI` rispettato, `ISPIRATO` a tutto il gruppo, rinfresco senza raddoppio con due canti, nessun canto per il Viandante, il Mago o il Locandiere.
11. **Documenti**: questo, `motore_di_gioco.md` (§8, personaggi e capacità innate), `interazioni_effetti_di_stato.md` (`ISPIRATO` e il suo rapporto con `SPAVENTATO`/`CONFUSO`), `modificatori_temporanei.md` (il canto di valore non è un modificatore), `todo.md` (si toglie "Il Bardo" e il punto sul Viandante), `foresta.md` e `CLAUDE.md`.

## 6. Alba Sacra

Non cambia. `AlbaSacra.formula` toglie tutti gli effetti di stato (`rimuoviTuttiGliEffettiDiStato`) e tutti i modificatori a tempo (`rimuoviTuttiIModificatoriTemporanei`), quindi anche `ISPIRATO`: l'Alba fa pulito, come si è deciso, e non si introduce nessuna distinzione fra effetti buoni e cattivi.

## 7. Domande risolte

1. **Alba Sacra e `ISPIRATO`:** l'Alba toglie tutto, `ISPIRATO` compreso (§6).
2. **Cura del canto curativo:** solo la Saggezza.
3. **Scala del canto di valore:** 20% + 2,5% per livello, tetto 50%.
4. **Altre classi sotto `Bardo`:** `BardoLocanda` resta un bardo, `PersonaggioNonCombattente` si stacca come il Viandante.

## Osservazioni

- Dopo i canti il Bardo non è più "la classe più debole" solo sulla carta: ma il suo danno resta quello di prima, e il valore di un canto dipende da quanta MAGIA ha. Con 4-5 di MAGIA per canto e la sua riserva, a livello 1 canta poche volte per scontro: è un bilanciamento da misurare con il giocatore automatico (`risorse_e_documenti_vari/analisi_logoramento.md`).
- Il `GiocatoreAutomatico` dei test non conosce i canti: i risultati dei test di simulazione non cambieranno finché non gli si insegna a usarli.
- La soglia del Coraggio contro la Soggezione (canto ipnotico) è la stessa coppia con cui oggi si spaventano i nemici nel resto del gioco: da riguardare insieme alla probabilità (`SPAVENTATO` ha effetti diversi da `CONFUSO`).

## Da fare / idee aperte

- Tarare costi, cura, durata e intensità. Con il tetto al 50% e il +22,5% già a livello 1 il canto di valore è molto forte nei primi livelli: va misurato.
- Insegnare al `GiocatoreAutomatico` a cantare.
- Un quarto canto (per esempio uno di scherno che abbassa le difese) è fuori da questo piano.
