# Interazioni tra effetti di stato e tipi di danno: piano di lavoro

> Stato: aggiornato al 2026-09-27. Passo 0 completo (compila senza errori, solo i 3 warning standard su Java 8).
> `TipoInterazioneConEffettiDiStato` ora porta anche `precondizioni` (gli effetti di stato richiesti) e
> `innescanti` (i `TipoDanno` che scatenano l'interazione) come metadati dichiarativi sull'enum stesso.
> Per ora sono solo documentazione: `CalcolatoreCombattimento` continua a decidere quando scatenare
> un'interazione con if espliciti (`hasEffettoDiStato(...)` + `tipoDanno == ...`), duplicando a mano la
> stessa informazione. Le nuove interazioni (§2, §3) vanno aggiunte all'enum già con precondizioni/innescanti
> corretti; whether centralizzare anche il *controllo* (leggendo `getPrecondizioni()`/`getInnescanti()` invece
> di if sparsi) resta una possibile futura pulizia, non ancora approvata.
> La logica delle interazioni vive tutta in `CalcolatoreCombattimento.calcolaDannoRisultante` (righe 226-348).
> `PersonaggioBase.applicaRisultatoCombattimento` si limita a scorrere `DannoRisultante.getInterazioni()` e a
> pubblicare `NotificaInterazionePersonaggio` per la UI, non decide nulla.
> I salvataggi **non** devono restare retrocompatibili: il formato si cambia liberamente.

## 0. Prerequisito: da `TipoInterazioneElementale` a `TipoInterazioneConEffettiDiStato` — ✅ fatto

"Colpo di Grazia" (STORDITO/ATTERRATO + TAGLIENTE/PERFORANTE) è un'interazione fisica, non elementale, e prima era
implementata solo con un flag booleano dedicato, senza passare dal sistema di notifica. Generalizzato l'enum per usare
un solo meccanismo per tutte le interazioni, elementali e fisiche, appoggiandosi al `TipoDanno` che `DannoRisultante`
già porta con sé.

- [x] Rinominato il file/enum `TipoInterazioneElementale` → `TipoInterazioneConEffettiDiStato`
- [x] `DannoRisultante`: rinominati campo e metodi (`interazioni`, `addInterazione`, `getInterazioni`)
- [x] `NotificaInterazioneElementalePersonaggio` → `NotificaInterazionePersonaggio` (classe, campo, `TipoEvento.NOTIFICA_INTERAZIONE_PERSONAGGIO`)
- [x] `CalcolatoreCombattimento`: aggiornate tutte le chiamate esistenti
- [x] `PersonaggioBase.applicaRisultatoCombattimento`: aggiornato il relay dell'evento
- [x] `ForestaUI.gestisciEventoInterazione`, `DisplayableCanvas`/`DisplayableCanvasRiquadroLocazione.aggiungiInterazione`: aggiornato il tipo del parametro
- [x] `SnifferBusEventi`: aggiornati i riferimenti al vecchio naming
- [x] Migrato "Colpo di Grazia" dal flag `colpoDiGrazia` (mantenuto per la logica di danno) a un valore vero di `TipoInterazioneConEffettiDiStato.COLPO_DI_GRAZIA`, così compare anche a video (sprite verde) come le altre interazioni
- [x] Aggiunte a ogni valore dell'enum le `precondizioni` (`TipoEffettoDiStato`) e gli `innescanti` (`TipoDanno`) come metadati dichiarativi — per ora solo documentazione, non ancora letti da `CalcolatoreCombattimento`

## 1. Già a posto — nessun intervento

- [x] BAGNATO: ELETTROCUZIONE (FULMINE), CONGELAMENTO (GELO), VAPORIZZAZIONE (FUOCO) — nota: la descrizione originale di ELETTROCUZIONE ("si propaga automaticamente a tutti i bersagli vicini BAGNATI o nella stessa pozza") non è implementata: oggi applica solo `moltiplicatoreDannoStato * 1.5` sul singolo difensore già colpito. Vedi nota architetturale sotto RIGETTO (§2, MALEDETTO) per come andrebbe fatta la propagazione.
- [x] BRUCIATO: ALIMENTAZIONE_FIAMMA (ARIA), ESTINZIONE (ACQUA), SCIOGLIMENTO_TERMICO (GELO), ESPLOSIONE_DI_GAS (VELENO)
- [x] CONGELATO: FRANTUMAZIONE_DEL_GHIACCIO (CONTUNDENTE), DISGELO_VIOLENTO (FUOCO), SUPERCONDUZIONE (FULMINE)

## 2. Da rifinire (parzialmente implementate)

### STORDITO / ATTERRATO — ✅ chiuso
- [x] Colpo di Grazia: c'è già (righe 314-321) e ora è anche un `TipoInterazioneConEffettiDiStato.COLPO_DI_GRAZIA` (§0)
- [x] Schiacciamento (ATTERRATO + CONTUNDENTE): raddoppia il danno fisico (`moltiplicatoreDannoStato * 2.0`) ed estende la durata di ATTERRATO di un turno (`calcolaDurataStato(...) + 1`, sfruttando il "rinforzo" già gestito da `PersonaggioBase.addEffettoDiStato`); nuovo valore `TipoInterazioneConEffettiDiStato.SCHIACCIAMENTO`, gestito anche in `ForestaUI`

### MALEDETTO — parzialmente chiuso
- [x] Mietitura (NECROTICO): il moltiplicatore resta quello variabile già esistente (`max(1.0, 2.0 - saggezza/100)`, non un fisso x2.0); in più, se il bersaglio è un minion avversario (non-boss) sceso sotto il 20% di vita, muore all'istante e viene sostituito da uno Scheletro alleato aggiunto al gruppo del giocatore; nuovo campo `DannoRisultante.mietituraAttiva` (flag, sul modello di `collassoEntropicoAttivo`), consumato in `PersonaggioBase.applicaRisultatoCombattimento`. Lo Scheletro (e in futuro un eventuale "Controllo Mentale") usa un nuovo ciclo di vita "ospite di locazione" (`TipoAttributo.OSPITE_DI_LOCAZIONE`, `Personaggio.isOspiteDiLocazione`/`setOspiteDiLocazione`), distinto da quello "a tempo" di AiutoGratuito/AiutoMercenario: resta nel gruppo solo per la locazione corrente, rimosso in `Automa.eseguiFineLocazione` (non in `LocazioneBase.azzeraLocazione`, che 7 sottoclassi sovrascrivono senza chiamare `super`). Per ospitarlo il gruppo giocatore passa da 5 a 8 slot totali: 5 permanenti invariati (`Costanti.MAX_PERSONAGGI_GRUPPO_GIOCATORE`) + 3 temporanei nuovi (`Costanti.MAX_PERSONAGGI_GRUPPO_TOTALE`, nuovi `Comando.PERSONAGGIO_6/7/8`); il riquadro del gruppo (`DisplayableCanvasRiquadroGruppo`) ora scorre con la rotella (`saltaPrimi`/`personaggiVisibili`, mirror di `DisplayableCanvasBarraIcone`) invece di tagliare i personaggi oltre il quinto.
- [ ] RIGETTO (SACRO): oggi rimuove solo MALEDETTO; manca il danno ad area Tuono/Sonico ai nemici vicini.
  **Nota architetturale (discussione 2026-09-28, non implementata):**
  - Il gioco non ha un concetto spaziale di "vicinanza": l'unico precedente di effetto ad area (`curaAdArea`, Purificazione) risolve "area" come "tutti i vivi del gruppo del difensore, escluso lui stesso" (`PersonaggioBase.applicaRisultatoCombattimento`, via `GruppoGiocatore`/`GruppoAvversario`). La propagazione di RIGETTO/ELETTROCUZIONE andrebbe risolta allo stesso modo.
  - `DannoRisultante` andrebbe esteso con un `dannoAdArea` che porta un `TipoDanno` **esplicito**, non implicito dal `tipoDanno` del colpo scatenante: RIGETTO scatena SONICO/TUONO da un innesco SACRO, mentre un'eventuale propagazione di ELETTROCUZIONE riuserebbe FULMINE. Serve quindi una coppia `(TipoDanno, quantità)`, non solo un intero come `curaAdArea`.
  - Perché il danno ad area scateni a sua volta altre reazioni sul bersaglio secondario (es. FULMINE ad area su un vicino BAGNATO che fa scattare ELETTROCUZIONE anche su di lui) senza duplicare la logica delle interazioni: aggiungere un nuovo overload `calcolaDannoRisultante(Personaggio attaccante, Personaggio difensore, TipoDanno tipoDanno, int dannoFisso)` che salta il calcolo da statistiche/arma e riusa la sezione delle interazioni esistente con un danno grezzo già dato; `applicaRisultatoCombattimento` richiamerebbe questo overload per ogni altro vivo nel gruppo del difensore e applicherebbe recursivamente il `DannoRisultante` risultante.
  - Decisione ancora aperta: quanto danno SONICO/TUONO infligge l'esplosione di RIGETTO (nessuna formula nel testo originale) — es. una percentuale del danno mitigato del colpo scatenante, come il 50% di `curaAdArea`.
  - Discussa anche l'idea di generalizzare la propagazione a *tutti* gli incantesimi con `PortataIncantesimo.MULTIPLO` (Aria, Acqua, Terra, Fuoco, Fulmine, Gelo, Veleno in `ClasseIncantesimo`): scartata come feature generica sempre attiva, perché MULTIPLO già colpisce fino a N bersagli scelti direttamente in base a `formulante.getBersagli()` (vedi `IncantesimoMaleficoImpl.formula`/`colpisci`, ognuno con il proprio tiro per colpire e la propria risoluzione di reazioni) — una propagazione incondizionata al resto del gruppo, sommata a questo, renderebbe ogni incantesimo elementale MULTIPLO di fatto equivalente a GRUPPO indipendentemente dalla statistica bersagli del lanciatore, ed è quindi troppo forte. La propagazione ad area va tenuta come effetto secondario legato a una reazione specifica (RIGETTO, eventuale ELETTROCUZIONE), non come proprietà generale di MULTIPLO.
- [x] Collasso Entropico (VUOTO): rimuove MALEDETTO; brucia il 25% dei MP massimi del difensore, limitato ai MP realmente disponibili (`Math.min(round(getMagiaMassima() * 0.25), getMagia())`), e infligge un danno alla salute pari agli MP effettivamente persi (non al 25% nominale, se il bersaglio ne aveva meno); nuovo campo `DannoRisultante.collassoEntropicoAttivo` (flag, sul modello di `diluizioneEmaticaAttiva`) gestito in `PersonaggioBase.applicaRisultatoCombattimento` via `subMagia`/`subSalute` diretti sul difensore stesso; nuovo valore `TipoInterazioneConEffettiDiStato.COLLASSO_ENTROPICO`, gestito anche in `ForestaUI`

### INFETTATO — ✅ chiuso
- [x] Purificazione Violenta (SACRO): +50% danno, rimuove INFETTATO e cura gli alleati vivi del difensore (escluso lui stesso) per il 50% del danno applicato (`dannoFinale / 2`, calcolato a fine `calcolaDannoRisultante` tramite il flag `purificazioneAttiva`)
- [x] Sifone Vitale (ARCANO): il 30% del danno grezzo (`dannoOffensivoGrezzo`, pre-mitigazione) va all'attaccante come MP; l'eccedenza oltre `getMagiaMassima()` diventa HP (`DannoRisultante.sifoneVitale`, applicato in `PersonaggioBase.applicaRisultatoCombattimento`)
- [x] Tossicità Settica (VELENO): se il bersaglio infetto viene colpito da danno VELENO, applica/rinforza AVVELENATO con danno periodico raddoppiato (`calcolaDannoPeriodico(...) * 2`)
- [x] Nuovi valori `TipoInterazioneConEffettiDiStato.SIFONE_VITALE`/`TOSSICITA_SETTICA`, gestiti anche in `ForestaUI`; aggiornato il commento di Sifone Vitale in `TipoEffettoDiStato.java` ("danno grezzo" invece di "danno inflitto")

## 3. Del tutto assenti

### RALLENTATO — ✅ chiuso
- [x] Inciampo (TERRA/CONTUNDENTE): +30% danno (`moltiplicatoreDannoStato * 1.3`) e applica ATTERRATO (senza rimuovere RALLENTATO); nuovo valore `TipoInterazioneConEffettiDiStato.INCIAMPO`, gestito anche in `ForestaUI`

### IMMOBILIZZATO — ✅ chiuso
- [x] Incendio Liberatorio (FUOCO): rimuove IMMOBILIZZATO all'istante e +50% danno (`moltiplicatoreDannoStato * 1.5`); scartata la clausola "se l'immobilizzazione è vegetale" (verificato: nel codice IMMOBILIZZATO deriva solo dal proc-rate di `TipoDanno.TERRA`, non esiste una distinzione vegetale/non vegetale da gestire); nuovo valore `TipoInterazioneConEffettiDiStato.INCENDIO_LIBERATORIO`
- [x] Impatto Rigido (CONTUNDENTE/TERRA): +30% danno (`moltiplicatoreDannoStato * 1.3`), senza rimuovere IMMOBILIZZATO; nuovo valore `TipoInterazioneConEffettiDiStato.IMPATTO_RIGIDO` (precondizione singola IMMOBILIZZATO, innescanti CONTUNDENTE/TERRA — stesso pattern di INCIAMPO), gestiti entrambi anche in `ForestaUI`

### SPAVENTATO — ✅ chiuso
- [x] Sovraccarico Mentale (ARCANO): rimuove SPAVENTATO (o CONFUSO, vedi sotto) e applica STORDITO per 1-2 turni (`Dado.tira(2)`); nuovo valore `TipoInterazioneConEffettiDiStato.SOVRACCARICO_MENTALE`
- [x] Shock di Realtà (danno FISICO, qualsiasi tipo): rimuove subito SPAVENTATO (o CONFUSO, vedi sotto) e +20% danno (`moltiplicatoreDannoStato * 1.2`); nuovo valore `TipoInterazioneConEffettiDiStato.SHOCK_DI_REALTA`, gestiti entrambi anche in `ForestaUI`

### SANGUINAMENTO — ✅ chiuso
- [x] Diluizione Ematica (ACQUA): dimezza il danno periodico da sanguinamento (`EffettoDiStatoMD.setDanniNelTempo(danniNelTempo / 2)`, mutato direttamente sull'effetto SANGUINAMENTO già attivo tramite `DannoRisultante.diluizioneEmaticaAttiva`, applicato in `PersonaggioBase.applicaRisultatoCombattimento` — necessario perché il "rinforzo" di `addEffettoDiStato` può solo aumentare durata/danno, mai ridurli) e +5% danno (`moltiplicatoreDannoStato * 1.05`); l'idea della pozza di sangue calpestabile è stata scartata (non gestibile); nuovo valore `TipoInterazioneConEffettiDiStato.DILUIZIONE_EMATICA`
- [x] Coagulazione Forzata (GELO): rimuove SANGUINAMENTO, applica RALLENTATO e +5% danno (`moltiplicatoreDannoStato * 1.05`); nuovo valore `TipoInterazioneConEffettiDiStato.COAGULAZIONE_FORZATA`, gestiti entrambi anche in `ForestaUI`

### AVVELENATO — ✅ chiuso
- [x] Reazione Tossica (ACIDO): scartata la corrosione/raddoppio usura dell'armatura (stessa logica della "pozza di sangue" scartata per SANGUINAMENTO — non gestiamo corrosione/usura delle armi in nessun punto del codice); limitato a +15% danno (`moltiplicatoreDannoStato * 1.15`), senza rimuovere AVVELENATO; nuovo valore `TipoInterazioneConEffettiDiStato.REAZIONE_TOSSICA`, gestito anche in `ForestaUI`
- [x] ~~Neutralizzazione (SACRO): rimuove AVVELENATO, piccola cura~~ Eliminata del tutto: la "piccola cura" non ha senso in `calcolaDannoRisultante`, che è sempre adversariale (attaccante → difensore, tipicamente il nemico) — non esiste un modo per "attaccare con SACRO e curare chi si è colpito" in quella pipeline. Servirebbe un vero incantesimo benefico di gruppo (sul modello di `Resurrezione`, che infatti non passa per `TipoDanno`/`CalcolatoreCombattimento`), non una reazione. Vedi TODO in `Automa.java` per l'incantesimo benefico di gruppo e per SACRO vs non-morti.
- [x] Vampata Tossica (FUOCO): verso opposto di Esplosione di Gas — danno FUOCO su un bersaglio AVVELENATO (senza essere BRUCIATO) rimuove AVVELENATO, applica BRUCIATO e +30% danno (`moltiplicatoreDannoStato * 1.3`); nuovo valore separato `TipoInterazioneConEffettiDiStato.VAMPATA_TOSSICA` (non riusa `ESPLOSIONE_DI_GAS`, per non mischiarne precondizioni/innescanti in un'unica coppia BRUCIATO/AVVELENATO × VELENO/FUOCO che includerebbe anche le combinazioni senza senso BRUCIATO+FUOCO o AVVELENATO+VELENO), gestito anche in `ForestaUI`

### CONFUSO — ✅ chiuso
- [x] Sovraccarico Mentale (ARCANO) e Shock di Realtà (danno FISICO): stessa interazione già usata per SPAVENTATO (§3, sopra) — dato che entrambi gli stati richiedono sempre lo stesso, unico tipo di danno scatenante rispettivamente (ARCANO / FISICO), non si ricade nel caso di cross-product invalido visto con BRUCIATO/AVVELENATO × VELENO/FUOCO: `precondizioni` di `SOVRACCARICO_MENTALE`/`SHOCK_DI_REALTA` estese a `[SPAVENTATO, CONFUSO]`, e nel `CalcolatoreCombattimento` un'unica condizione `hasEffettoDiStato(SPAVENTATO) || hasEffettoDiStato(CONFUSO)` individua lo stato attivo e lo rimuove (a parità, priorità a SPAVENTATO se entrambi presenti)
- [x] Follia Cosmica (VUOTO): rimuove CONFUSO, +50% danno (`moltiplicatoreDannoStato * 1.5`) e applica un nuovo stato `MENTE_FRATTURATA` con durata a turni (rientra nella categoria di default di `calcolaDurataStato`, base 3 turni contrastati da SAGGEZZA). Scartato il "malus permanente" originale: dato che i personaggi giocatore subiscono le interazioni esattamente come i nemici (nessuna asimmetria nel `CalcolatoreCombattimento`), un malus vero e proprio via `ModificatoreAttributoMD` (che oggi non ha né durata né un metodo di rimozione) sarebbe rimasto per sempre anche su di loro. `MENTE_FRATTURATA` è invece un normale `TipoEffettoDiStato` a tempo: mentre è attivo, penalizza del 20% la `statOffensiva` di chi lo ha quando attacca con danno MAGICO/ELEMENTALE (non solo ARCANO/PSICHICO) e del 20% la `statDifensiva` di chi lo ha quando subisce danno MAGICO/ELEMENTALE — due controlli generici in testa a `calcolaDannoRisultante` (dove si calcolano `statOffensiva`/`statDifensiva`), non dentro il blocco delle interazioni, perché il malus deve valere in ogni combattimento successivo finché lo stato dura, non solo nel turno in cui scatta la reazione; nuovo valore `TipoInterazioneConEffettiDiStato.FOLLIA_COSMICA`, gestito anche in `ForestaUI`

### ACCECATO — ✅ chiuso
- [x] Dispersione (ARIA): rimuove ACCECATO e +5% danno (`moltiplicatoreDannoStato * 1.05`); nuovo valore `TipoInterazioneConEffettiDiStato.DISPERSIONE`
- [x] Fango (ACQUA): applica/rinforza un RALLENTATO severo (`calcolaDurataStato(...) + 2` turni) e +5% danno (`moltiplicatoreDannoStato * 1.05`); nuovo valore `TipoInterazioneConEffettiDiStato.FANGO`, gestiti entrambi anche in `ForestaUI`

### ASSORDATO — ✅ chiuso
- [x] Disorientamento (SONICO): trasforma in STORDITO di 1 turno e +5% danno (`moltiplicatoreDannoStato * 1.05`); nuovo valore `TipoInterazioneConEffettiDiStato.DISORIENTAMENTO`, gestito anche in `ForestaUI`

### SILENZIATO — ✅ chiuso
- [x] Risonanza Sigillata (ARCANO): +30% danno Puro che ignora le difese (`dannoPuroBonus = dannoOffensivoGrezzo * 0.3`, sommato a `dannoTotaleCombinato` dopo la mitigazione, non moltiplicato per `fattoreMitigazione`/`moltiplicatoreDannoStato`); nuovo valore `TipoInterazioneConEffettiDiStato.RISONANZA_SIGILLATA`
- [x] Isolamento Sensoriale (PSICHICO): +50% danno (`moltiplicatoreDannoStato * 1.5`) e applica anche ACCECATO (senza rimuovere SILENZIATO); nuovo valore `TipoInterazioneConEffettiDiStato.ISOLAMENTO_SENSORIALE`, gestiti entrambi anche in `ForestaUI`
