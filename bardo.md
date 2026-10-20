# Il Bardo

I tre **canti** innati del Bardo e della Cantastorie (e del bardo della locanda), sul modello del dardo arcano di Mago
ed Elfo e dell'alba sacra di Sacerdote e Sacerdotessa.

## 1. Obiettivi

- Il Bardo e la Cantastorie hanno tre capacità innate che costano `MAGIA`, non consumano pergamene, sono fra le scelte
  del comando incantesimi, occupano il turno e fanno rispondere l'avversario su chi le usa.
- Il gioco ha finalmente un curatore, un modo di colpire il morale del nemico e un modo di rafforzare il gruppo.

## 2. Com'era prima

- **Dardo arcano** (`incantesimi/DardoArcano`): un'`Arma` che non sta in `TipoIncantesimo`, perché non è una pergamena.
  `conosciutoDa(TipoPersonaggio)` e `puoLanciarlo(Personaggio)` (vivo, conosce, ha la MAGIA, non è `SILENZIATO`)
  decidono chi lo lancia; `Automa.entraInStatoSceltaIncantesimoDaLanciare` lo aggiunge ai comandi;
  `LocazioneBase.lanciaDardoArcano` lo lancia; `LocazioneBase.impostaComandiPossibili` lo conta per abilitare il 
  comando `INCANTESIMO`.
- **Alba sacra** (`incantesimi/AlbaSacra`): invece è un `Incantesimo` di `TipoIncantesimo`, e chi la conosce per natura
  non ne consuma le pergamene.
- Le capacità dipendono dal **`TipoPersonaggio`** (`conosciutoDa(classe)`), non dalla classe Java, e `PersonaggioNonCombattente`,
  `BardoLocanda` e `Viandante`, che estendono `Bardo`, non hanno il dardo né l'alba.
- Il Bardo ha già la MAGIA (`BARDO_MAGIA_BASE` 40, `BARDO_LIVELLAMENTO_MAGIA` 18).
- Gli effetti di stato (`TipoEffettoDiStato`) nascono da `addEffettoDiStato`, che ne rinfresca la durata se ce n'è già
  uno uguale (vedi `PersonaggioBase`), e scalano ogni turno in `LocazioneBase.trascorriTurnoEffettiDiStato`.
- `Viandante` estendeva `Bardo` (fase 1, fatta): è un ospite di missione (scorta) con le caratteristiche del bardo,
  amichevole, e può essere un campione nella sfida a carta, forbici e sasso (`LaSfidaDeiCampioni.classiAmichevoli()`).

## 3. Decisioni

| Tema | Decisione |
| :--- | :--- |
| Chi canta | Bardo, Cantastorie e bardo della locanda (`TipoPersonaggio.BARDO`, `CANTASTORIE`, `BARDO_DELLE_LOCANDE`): il bardo della locanda è un bardo a tutti gli effetti. Nessun altro. |
| Costo | `MAGIA`, con costanti in `Costanti` da tarare: guarigione ~4, ipnotico ~3, valore ~5. |
| Quando | Solo in combattimento, con almeno un avversario vivo. Fuori dagli scontri (mappa, accampamento) i canti non sono offerti. |
| Turno | Ogni canto è l'azione del cantore nel round (`LocazioneBase.dopoAzioneDelGiocatore`: gli avversari del piano agiscono dopo di lui), come il dardo arcano. Vale anche per il canto di valore a inizio scontro. |
| Silenzio | Un Bardo `SILENZIATO` non canta (come per il dardo e l'alba). |
| Più canti | Più Bardi o Cantastorie nello stesso gruppo vanno bene. Due canti di valore non si sommano: il secondo rinfresca la durata (e porta il bonus più alto), come ogni effetto di stato. |
| Canto curativo | Cura il solo personaggio **più ferito** del gruppo (quello con la quota di salute più bassa; un non morto del gruppo, come lo Scheletro della Mietitura, non si può curare e non conta: `Personaggio.isCurabile`). Circa 30 × √livello, aumentato dalla sola Saggezza (fonte di conoscenza; il Carisma non c'entra). |
| Canto ipnotico | Applica a caso `SPAVENTATO` o `CONFUSO` a **fino a `NUMERO_BERSAGLI`** avversari vivi, con la Soggezione del Bardo contro il Coraggio di ciascuno. |
| Canto di valore | **+% di coraggio e di precisione** a tutto il gruppo, per qualche turno; il bonus è **20% + 2,5% per livello** del Bardo, con un tetto del **50%**, e non dipende dal suo Carisma. Solo un bonus, mai un malus. |
| Urlo della Banshee | Il canto ipnotico potenziato, **non noto all'inizio**: lo insegna il bardo della locanda al gruppo (intermezzo `IntermezzoUrloDellaBanshee`, la prima volta in una locanda nel bosco dopo aver **completato con successo** `IlViaggioDellaMoglie`; non è di ripiego, scatta una volta sola e non se la missione fallisce). Costa `CANTO_URLO_DELLA_BANSHEE_COSTO_LANCIO` (6) `MAGIA`, stessi bersagli del canto ipnotico, ma la Soggezione del cantore vale **×1,5** contro il Coraggio (`CANTO_URLO_DELLA_BANSHEE_MOLTIPLICATORE_SOGGEZIONE`) e chi cede, con **un solo tiro**, prende **sia `SPAVENTATO` sia `CONFUSO`**. Il Bardo e la Cantastorie lo conoscono da quando l'intermezzo è scattato (`CantoDelBardo.isUrloDellaBansheeAppreso`, che legge `RegistroIntermezzi`: vale per tutto il gruppo, anche per chi viene reclutato dopo); il `BardoLocanda` lo sa sempre. Classe `UrloDellaBanshee`, `TipoCanto.URLO_DELLA_BANSHEE`, `Comando.URLO_DELLA_BANSHEE`, icona `UrloDellaBanshee.gif`. |
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

## 5. Fasi

1. [x] **Viandante e non combattenti a sé** (2026-10-10): `Viandante` e `PersonaggioNonCombattente` estendono `PersonaggioBase`, con `VIANDANTE_*` e `NON_COMBATTENTE_*` in `Costanti` (i valori di prima: le massime dei non combattenti sono ora i numeri del bardo ×0,85, e `NON_COMBATTENTE_FATTORE_STATISTICHE` non c'è più). Gli articoli e il genere maschili sono ora in `PersonaggioNonCombattente`. Test: `ViandanteENonCombattentiASeTest`; la suite è rimasta verde.
2. [x] **Interfaccia `Canto`** (2026-10-10), in `incantesimi`: `Canto` (`getTipo`, `getCostoLancio`, `boolean canta(cantore, gruppo, gruppoAvversario)`, che scala la MAGIA e dice se il canto c'è stato), `TipoCanto`, `FabbricaCanti`, `CantoDelBardo` (`conosciutoDa`, `puoCantare`, `cantiPossibili`: vivo, sa cantare, non `SILENZIATO`, MAGIA sufficiente) e le tre classi. `CantoCurativo` e `CantoIpnotico` hanno già i loro effetti (passi 5 e 6 sotto, fatti qui; per l'ipnotico `CalcolatoreCombattimento.calcolaDurataStato` è diventato pubblico); `CantoDiValore.canta` aspettava `ISPIRATO` (passi 7 e 8, fatti nella fase 5). Costanti: `CANTO_*` in `Costanti`. Resta da fare il fatto che la sede "in combattimento e con un avversario vivo" la decide chi offre i canti (passo 4), non `puoCantare`. Test: `CantiDelBardoTest`.
3. [x] **Comandi e icone** (2026-10-10): `Comando.CANTO_CURATIVO`, `CANTO_IPNOTICO`, `CANTO_DI_VALORE`; tre voci di `ClasseIcona` con `CantoCurativo.gif`, `CantoIpnotico.gif`, `CantoDiValore.gif`; `TipoCanto.getComando()` e `TipoCanto.ofComando(comando)`. Test: `IconeDeiCantiTest`.
4. [x] **Menu** (2026-10-10): `CantoDelBardo.cantiUtili(cantore, gruppo, gruppoAvversario)` dà i canti che si possono cantare adesso: nessuno se non ci sono avversari vivi, e il curativo solo se qualcuno è ferito. `Automa.entraInStatoSceltaIncantesimoDaLanciare` li aggiunge ai comandi; `LocazioneBase.impostaComandiPossibili` li conta per mostrare `INCANTESIMO` (anche se il gruppo non ha pergamene); `LocazioneBase.QUALE_FORMULA` ha un ramo che riconosce il comando (`TipoCanto.ofComando`) e chiama `cantaUnCanto`: rilegge `cantiUtili`, canta, chiude la locazione se gli avversari sono morti, altrimenti fa rispondere l'avversario sul Bardo; azzera corruzione e amicizia come ogni incantesimo. Un comando dato quando il canto non serve più non fa nulla e non costa. Test: `ScenarioCantiDelBardoTest` (la parte di `LocazioneBase`; la lista dei comandi in `Automa` non ha un test a sé).
5. [x] **Canto curativo** (2026-10-10, fatto con la fase 2): `CantoCurativo`; il personaggio vivo con la quota `salute / saluteMassima` più bassa (a parità il primo), `cura = CANTO_CURATIVO_BASE × √livello × (1 + SAGGEZZA / CANTO_CURATIVO_DIVISORE_SAGGEZZA)` (30, 50), mai oltre il massimo.
6. [x] **Canto ipnotico** (2026-10-10, fatto con la fase 2): `CantoIpnotico`; fino a `NUMERO_BERSAGLI` avversari vivi scelti a caso, `SPAVENTATO` o `CONFUSO` (lo stesso per tutti), cede con probabilità `Soggezione / (Soggezione + Coraggio)` e per `calcolaDurataStato`; chi è immune lo resiste da sé.
7. [x] **Effetto di stato `ISPIRATO`** (2026-10-10): `TipoEffettoDiStato.ISPIRATO`; `EffettoDiStatoMD.getIntensita()` (decimale, salvata come quarto campo dell'effetto in `PersonaggioMD`); `Personaggio.addEffettoDiStato(tipo, durata, danni, intensita)`, che quando c'è già lo stesso effetto tiene durata, danni e intensità più alti (non somma); in `PersonaggioBase.quantitaModificata` un `ISPIRATO` somma la sua intensità alle percentuali di `CORAGGIO` e `PRECISIONE` (e `getVariazioneDaTemporanei` lo conta fra i temporanei, quindi la scheda colora i due attributi di verde). Non agisce se un'`QUANTITA_ASSOLUTA` fissa l'attributo.
8. [x] **Canto di valore** (2026-10-10): `CantoDiValore.canta` scala la MAGIA e dà `ISPIRATO` a tutti i vivi del gruppo con `CANTO_DI_VALORE_DURATA` (4) turni e intensità `min(50, 20 + 2,5 × livello)` (`CANTO_DI_VALORE_PERCENTUALE_*`); `getIntensita()`. Due canti rinfrescano senza sommare, e uno più forte sostituisce uno più debole.
9. [x] **UI** (2026-10-10): lo sprite di `ISPIRATO` c'è già, perché `ForestaUI` ne mostra uno per ogni effetto di stato; la scheda del personaggio colora coraggio e precisione di verde (vedi il passo 7). Nessun codice nuovo.
10. [x] **Costanti e test** (2026-10-10): le costanti `CANTO_*` stanno in `Costanti` (costi 4, 3 e 5; cura 30 e 50; valore 20%, 2,5%, 50%, 4 turni). Test: `CantiDelBardoTest` (chi canta, costi, cura, ipnotico, ispirato, rinfresco, salvataggio dell'intensità), `ScenarioCantiDelBardoTest` (la locazione e il menu dell'`Automa` con una partita vera), `IconeDeiCantiTest`, `ViandanteENonCombattentiASeTest`.
11. [x] **Documenti** (2026-10-10): questo, `motore_di_gioco.md`, `interazioni_effetti_di_stato.md`, `modificatori_temporanei.md`, `foresta.md`, `CLAUDE.md`, e tolta da `todo.md` la sezione "Il Bardo".

## 6. Alba Sacra

Non cambia. `AlbaSacra.formula` toglie tutti gli effetti di stato (`rimuoviTuttiGliEffettiDiStato`) e tutti i modificatori a tempo (`rimuoviIModificatoriTemporaneiRimovibili`, esclusa l'ignominia della fuga, che non è rimovibile), quindi anche `ISPIRATO`: l'Alba fa pulito, come si è deciso, e non si introduce nessuna distinzione fra effetti buoni e cattivi.

## 7. Decisioni prese lungo il lavoro

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
- Un altro canto (per esempio uno di scherno che abbassa le difese) è fuori da questo lavoro; l'Urlo della Banshee è già il quarto, e si impara in gioco.
- Tarare insieme anche la probabilità di cedere del canto ipnotico e il valore dei `VIANDANTE_*` e `NON_COMBATTENTE_*`, ora uguali a quelli del bardo.
