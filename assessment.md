# Assessment di Foresta

Valutazione del codice al **2026-10-19**, con una revisione completa dei sorgenti modulo per modulo, dal basso (`foresta-tipi`) fino a `foresta-app`. I numeri sono misurati sul working tree di quel giorno (`wc -l`, `grep`, JaCoCo da `mvn clean test`); i giudizi sono segnati come **Giudizio**. I difetti trovati sono nel §5, con un codice (A1, M3, B12…) per richiamarli. Le cose da fare per il porting su Android e iOS sono in [`assessment_portabilita.md`](assessment_portabilita.md): qui c'è solo il riassunto.

## 1. Sintesi

- Il gioco è **completo e giocabile** da inizio a fine (creazione del gruppo, esplorazione, combattimento a round con tattiche, negozi, incantatore, missioni, intermezzi, trofei, salvataggi, classifica).
- La **separazione fra le parti è garantita dal compilatore**: sei moduli Maven in fila (tipi, modellodati, contratti, motore, swing, app). La UI non vede il motore, ma soltanto viste in sola lettura ed eventi.
- La **base di test è ampia e veloce**: 1543 test verdi (16 saltati); `tipi` e `modellodati` sono coperti quasi al 100% dai loro test, il motore all'85%.
- La **revisione ha trovato 4 difetti di gravità alta**, tutti nella logica di gioco e non nell'infrastruttura (§5.1):
  - gli avversari uccisi dal danno ad area non si contano;
  - il capo del gruppo cambia quando esce un altro personaggio;
  - La Benedizione può portare il gruppo a 9 personaggi, e il gioco va in crash;
  - un carattere senza glifo (per esempio "ü" in un nome) manda in eccezione il disegno a ogni fotogramma.
- **L'infrastruttura è solida.** Il bus, il `Dado`, i salvataggi di partita, gli scambi di artefatti, i prezzi e le copie difensive sono stati controllati e sono a posto. Non c'è nessuna `ConcurrentModificationException` raggiungibile, e i salvataggi sono simmetrici campo per campo.
- **I difetti medi sono soprattutto regole di gioco incoerenti**: effetti di stato, ospiti, tempo in locanda, testi delle ricompense. Nella UI sono comportamenti sbagliati (§5.2).
- Il **debito tecnico** resta concentrato nelle quattro classi grandi del motore e nello stato statico (§3).
- **Giudizio:** il progetto è in buona salute. I difetti alti si correggono con interventi piccoli e locali; conviene farlo subito, con un test ciascuno. L'orientamento verticale resta rotto (noto) ed è rimandato in fondo.

## 2. Misure

| Modulo | Codice (file / righe) | Test (file / righe) | Test eseguiti | Copertura righe (JaCoCo) |
| :--- | ---: | ---: | ---: | ---: |
| `foresta-tipi` | 57 / 6.600 | 12 / 1.100 | 68 | 100% |
| `foresta-modellodati` | 36 / 5.500 | 27 / 3.700 | 193 | 99,7% |
| `foresta-contratti` | 168 / 7.400 | 9 / 640 | 39 | 40%* |
| `foresta-motore` | 363 / 55.900 | 190 / 31.200 | 1154 (16 saltati) | 85% |
| `foresta-swing` | 95 / 22.100 | 13 / 1.900 | 56 | 27%* |
| `foresta-app` | 3 / 740 | 19 / 1.600 | 33 | 27% |
| **Totale** | **722 / 98.300** | **270 / 40.200** | **1543** | **66%** |

\* JaCoCo conta solo i test del modulo stesso: contratti è esercitato molto di più dai test del motore, e la UI dai test di `foresta-app`. Le percentuali di questi moduli sono quindi più basse del vero.

Nel motore la copertura è alta quasi ovunque (`trofei` 100%, `testi` e `intermezzi` 95%, `combattimento` 94%, `canti` 93%, `tattiche` 89%, `incantesimi` 88%, `missioni` 87%, `motore` 85%). È bassa in `offerte` (24%), `salvataggi` (65%), `personaggi` (69%) e `locazioni` (72%). Non è un caso che molti difetti medi del §5 stiano proprio in `personaggi` e `locazioni`.

Le classi più grandi (escluse le tabelle dei font bitmap `DoomdarkFont*`):

| Classe | Righe | Il 2026-10-17 |
| :--- | ---: | ---: |
| `testi/GrammarBean` | 2533 | 2533 |
| `personaggi/PersonaggioBase` | 2356 | 2342 |
| `motore/Automa` | 2284 | 2288 |
| `locazioni/LocazioneBase` | 1944 | 1944 |
| `ui/DisplayableCanvas` | 1629 | 1631 |
| `tipi/Costanti` | 1625 | 1613 |
| `combattimento/CalcolatoreCombattimento` | 1283 | 1247 |
| `missioni/MissioneAPassi` | 1274 | 1274 |
| `ui/tattiche/DisplayableCanvasTattiche` | 1133 | 1136 |
| `tipi/TipoMissione` | 1012 | 986 |

## 3. Architettura

### Punti di forza

- **Moduli in fila, controllati dal compilatore.** Ogni modulo vede solo quelli sopra di lui. Quello che il compilatore non vede, cioè `java.awt`, `javax.swing`, `javax.imageio` e `javax.sound` fuori da `foresta-swing`, `Locale.ROOT`, `System.exit` e il `Logger`, lo controllano i test che leggono i sorgenti (`DipendenzeDalToolkitGraficoTest`, `ConvenzioniDiPortabilitaTest`).
- **La UI legge da viste e scrive con eventi.** `VistaPartita` e `VistaScambio` sono in sola lettura, e verso il motore vanno solo comandi con identificativi. Le collezioni del modello dati sono esposte in sola lettura, con copie nei costruttori.
- **Un solo thread per la logica.** Il bus consegna sempre sul thread principale (sul desktop l'EDT), dove girano anche i tick del temporizzatore: fra eventi e stato non ci sono corse. L'unico thread estraneo è l'animatore di `DisplayableCanvas` (B40).
- **Salvataggi di partita.** `LettoreCampi` conserva i campi vuoti, i testi liberi passano da `pulisciCampo`, un file troncato dà `IOException`, e la scrittura passa da un file temporaneo. Ogni coppia `salva`/`leggi` è stata confrontata campo per campo; le poche asimmetrie sono nel §5.3.
- **Testabilità.** Ci sono `Dado` con seme e trucchi per contesto, `PartitaDiTest`, il temporizzatore manuale e i simulatori. Tutti i test girano senza UI.
- **Contenuto a basso costo.** Missioni a passi, grammatiche e intermezzi a pagine. Uno script ha verificato che ogni riferimento `[produzione]` nelle grammatiche esiste e che tutte le immagini citate dal codice ci sono.
- **Documentazione.** 27 documenti `.md` nella radice, con la mappa in [`foresta.md`](foresta.md); un `package-info.java` per pacchetto.

### Problemi, in ordine di priorità

1. **Classi grandi.** `PersonaggioBase`, `Automa` e `LocazioneBase` sono stabili dal 2026-10-17, ma restano le zone dove la revisione ha trovato più difetti: il combattimento a round in `LocazioneBase` (M3, M4, M7) e gli effetti di stato in `PersonaggioBase` (M5, M9, B15).
   **Giudizio:** i tagli naturali restano quelli già indicati:
   - per `Automa`, altri sotto-automi (creazione del gruppo, combattimento, fine partita);
   - per `LocazioneBase`, il combattimento a round in una classe sua;
   - per `PersonaggioBase`, attributi e modificatori, equipaggiamento ed effetti di stato separati.
2. **Stato statico.** Lo stato della partita vive in registri e singleton. `StatoDelMotore.azzera()` va bene per i test, ma non per "ripartire pulito" su una piattaforma mobile come suggerisce la sua javadoc. `Automa` e `LocazioneBase` tengono i vecchi gruppi in campi `final`, e il vecchio `GruppoGiocatore` resta iscritto al bus (M20). In più `Foresta.reimposta` dimentica intermezzi e notizie (M2).
   **Giudizio:** accettabile per un gioco single-player, a patto di non aggiungere registri statici e di correggere M2.
3. **Il motore è un modulo solo.** `motore`, `personaggi`, `locazioni`, `missioni` e `oggetti` sono un unico grafo. Il primo taglio possibile resta `foresta-missioni` ([`todo.md`](todo.md)).
4. **Due stili di missione.** Restano missioni che estendono `MissioneBase`. Il controllo dopo la risposta a una domanda rifà i `controlla*` di tutte le missioni, e quindi richiede che quelle scritte a mano siano idempotenti: oggi `DisturbatoreDellaQuietePubblica` non lo è (M15). Il vincolo andrebbe scritto in `gestione_missioni.md`.
5. **Errori degli iscritti.** Il bus cattura le eccezioni e pubblica `InternoException`, ma l'unico iscritto è lo sniffer, che scrive nel log. Un errore a metà di una transizione lascia l'`Automa` in uno stato intermedio, senza nessun segnale per il giocatore (M19).

## 4. Qualità del codice

- **Gestione degli errori.** I `catch` generici stanno quasi tutti nei gestori su file e in `GrammarBean`, ed è una scelta difendibile. Però trofei e classifica si scrivono sul posto, non su un file temporaneo come i salvataggi (M18).
- **`TODO`/`FIXME`:** 18. Due segnalano difetti veri: la fuga a danno fisso (M11) e la "morte per codardia" da veleno (B16). Gli altri riguardano funzioni future.
- **Codice morto.** C'è un po' di codice che non si raggiunge (B35, B41):
  - quattro classi di eventi con sprite mai pubblicate, e due eventi con il `TipoEvento` sbagliato;
  - metodi di `PersonaggioBase` mai chiamati;
  - passi pronti di `MissioneAPassi` usati solo dai test;
  - produzioni delle grammatiche mai usate, fra cui 12 `NOTIZIE_CITTA` che non escono mai.
- **Convenzioni.** `TestoTattica` usa `Locale.ITALIAN` invece di `Locale.ROOT`. `SnifferBusEventi` costruisce una stringa di log fuori dal fornitore, e `ConvenzioniDiPortabilitaTest` non se ne accorge.
- **Test saltati.** I 16 test saltati sono simulazioni da lanciare a mano, non test rotti.
- **Rapporti stantii.** Dopo una rinomina o per misurare serve `mvn clean test`: senza `clean`, `surefire-reports` e `jacoco.exec` tengono classi che non esistono più.

## 5. Difetti trovati dalla revisione del 2026-10-19

Ogni voce è stata verificata sul codice chiamante. **C** sta per *confermato* (lo scenario si innesca), **P** per *plausibile* (dipende da condizioni rare o non ancora raggiungibili). I percorsi sono relativi a `…/` del modulo indicato.

### 5.1 Gravità alta

| Cod. | Dove | Difetto | Scenario | Correzione | |
| :--- | :--- | :--- | :--- | :--- | :---: |

### 5.2 Gravità media

**Combattimento e personaggi**

| Cod. | Dove | Difetto | |
| :--- | :--- | :--- | :---: |
| M4 | `locazioni/LocazioneBase:1408-1427` e QUALE_FORMULA, pozioni | Chi è CONGELATO, ATTERRATO o STORDITO può usare SINGOLO_ATTACCO, incantesimi e pozioni: `isIncapaceDiAgire` si controlla solo nella mischia automatica | C |
| M5 | `LocazioneBase:1376-1392`, `motore/Automa:776`, `PersonaggioBase:606-619` | Gli ospiti vulnerabili ricevono stati che non scadono mai (tick e pulizia scorrono solo i personaggi). In `applicaRisultatoCombattimento` un ospite colpito è trattato come avversario: la Purificazione cura i mostri | C (P per la Purificazione) |
| M6 | `tattiche/TatticheBase:293, 300`, `incantesimi/Morte:61` | Morte: la tattica usa 100·s/m < 25, l'incantesimo s < m/4 intero; con s=2, m=10 si ritorce sul mostro. Morte e Silenzio non registrano l'esito, quindi i tentativi falliti non fermano la ripetizione; la magia "a caso" può pescare Morte su un bersaglio sano | C |
| M8 | `PersonaggioBase:1029-1051` | Il carico massimo non è moltiplicato per `getMoltiplicatoreCarico()`, contro javadoc e `motore_di_gioco.md` §8: Guerriero e Maga, Drago e Goblin portano lo stesso peso. Correggerlo vuol dire ritarare pesi e test | C |
| M10 | `PersonaggioBase:944-953`, `motore/RegistroPersonaggi:99-104` | Un compagno reclutato a livello alto entra con salute e magia del livello 1 (al livello 9, 25-33% della salute massima) | C |
| M11 | `PersonaggioBase:525-527` | La fuga toglie 50-100 punti fissi: al livello 1 un Mago muore nel 41% circa delle fughe (TODO già nel codice) | C |

**Motore, missioni, trofei e testi**

| Cod. | Dove | Difetto | |
| :--- | :--- | :--- | :---: |
| M1 | `motore/GruppoGiocatore:584`, `locazioni/Locanda:284` | Pernottare in locanda fa riposare fino al mattino ma non sposta l'orologio (lo fa solo l'accampamento): il riposo è gratis e aggira la scadenza del drago | C |
| M2 | `motore/Foresta:148-200` | `Foresta.reimposta` non azzera `IntermezziMD` e `NotizieMD`: una nuova partita nella stessa sessione non mostra l'intro né gli intermezzi di prima visita, e il notiziario mostra le notizie vecchie | C |
| M12 | `missioni/incarichi/{CacciaAiGoblin,LaTagliaSullaBanda,CacciatoreDiTaglie,IlPellegrino,IlRapimento,IlTorneo}` | I testi promettono la costante `RICOMPENSA` (la paga base), mentre si paga `getRicompensa()`, cioè la base per il livello del mondo ("15 monete promesse", ne arrivano 45) | C |
| M13 | `missioni/incarichi/IlRituale:94-96, 191-216` | Il ripiego della raccolta rivendica un'altra casella e sposta `getPosto()`: rito e guardiano finiscono nel posto del ripiego | C |
| M14 | `missioni/MissioneAPassi:720-753` | `combatti` a ondata singola somma gli sconfitti di più visite, anche dopo una fuga: la banda risulta sgominata mentre è viva | C |
| M15 | `missioni/principali/DisturbatoreDellaQuietePubblica:55-86` | Non idempotente: dopo una domanda in IN_LOCAZIONE l'`Automa` rifà il controllo e l'eremita conta 2 | C (raro) |
| M16 | `trofei/RegistroTrofei:49` | Il progresso in sospeso si azzera a ogni preparazione di locazione: le missioni completate in PRE_LOCAZIONE (LaBenedizione) non contano per CACCIATORE_DI_TAGLIE | C (P per l'accampamento) |
| M17 | `testi/ProduttoreDiTestiCasuale:505` | `LETTERA_FINALE` si accorda sempre con il capo anche quando il soggetto è il ladro o il bardo (`locande.txt` righe 300, 349, 511): "Gualtiero è stata vista" | C |
| M18 | modellodati: `TrofeiMD:82`; motore: `trofei/RegistroTrofei:41, 104`, `salvataggi/GestoreTrofeiSuFile:64`, `GestorePunteggiSuFile:69` | Un nome di trofeo sconosciuto ferma la lettura, `RegistroTrofei` ignora il fallimento e alla prima locazione riscrive il file: trofei persi per sempre. Trofei e classifica si scrivono sul posto, senza file temporaneo e senza `checkError` | C |
| M19 | contratti: `eventi/BusEventi:57-79` | Gli errori degli iscritti finiscono solo nel log (unico iscritto a `InternoException` è lo sniffer): una transizione a metà lascia il gioco bloccato senza messaggio. Un `Error` invece interrompe la consegna agli altri iscritti | C (P le conseguenze) |
| M20 | motore: `motore/StatoDelMotore:139-144`; contratti: `BusEventi:46-51` | `azzera()` lascia i vecchi gruppi nei campi `final` di `Automa`/`LocazioneBase` e il vecchio `GruppoGiocatore` iscritto al bus (e `cancellati` non può togliere una lambda). Oggi lo chiamano solo i test | P |
| M21 | motore: `motore/Automa:324-347, 1668` | Il salvataggio automatico si cancella solo a fine partita: dopo una partita nuova o un caricamento, una ripresa su mobile può ricaricare la partita vecchia | P (solo mobile) |

**Interfaccia (foresta-swing)**

| Cod. | Dove | Difetto | |
| :--- | :--- | :--- | :---: |
| M22 | `ui/schermate/DisplayableCanvas{ScambiatoreArtefatti:461,ScambiatoreConsumabili:298,Inventario:367}` | I `processaDoppioClick*` non controllano il tasto: un doppio click destro o centrale compra, vende o spende un punto abilità | C |
| M23 | `ui/DisplayableCanvas:443-446` | Ogni variazione di statistiche, anche di un mostro, rimette davanti il riquadro del Gruppo | C |
| M24 | `ui/Prompt:252-264`, `ui/tattiche/DisplayableCanvasTattiche:199-203` | Il campo di rinomina della tattica resta aperto quando si lascia la voce o la schermata; l'Invio scrive su una bozza scartata | C |
| M25 | `ui/riquadri/DisplayableCanvasRiquadroMappa:110` | Il ritaglio delle nuvole della minimappa usa l'offset x anche per y: le nuvole non si vedono mai | C |
| M26 | `ui/riquadri/DisplayableCanvasRiquadroGruppo:511, 532` | Gli sprite di Valore e Carisma usano la riga 1 invece della 2 | C |
| M27 | `ui/sprite/SpriteRivelazioneArtefatto:183` | L'animazione avanza di un passo fisso a ogni repaint: muovendo il mouse la rivelazione accelera | C |
| M28 | `ui/sfx/CloudManager:39-41, 117` | `nextInt(clip.height - cloudHeight)` può ricevere ≤ 0 con una zona conosciuta "a corridoio"; nuvole statiche mai azzerate fra una partita e l'altra | P |
| M29 | `ui/tattiche/CasellaADiscesa:81-94`, `ui/componenti/ComponenteScorrevole:129-132` | Un menu con zero scelte o un componente alto 0 crea una `BufferedImage` alta 0 e lancia a ogni fotogramma | P |

### 5.3 Gravità bassa

**foresta-tipi e foresta-modellodati**

- B1 `ArtefattoMD:346/372`: la descrizione scritta con `facoltativo` è riletta con `testo()`, quindi null torna "". La rivelazione aggiunge una riga vuota. (C)
- B2 `LettoreCampi:115`: un nome di enum non valido dà `IllegalArgumentException` invece di `IOException`; oggi è assorbito dai chiamanti. (C)
- B3 `ModificatoreAttributoMD:52, 158, 175`: una nota null torna "", e il modificatore riletto non è più `equals`. Il confronto fra double usa `==`. (P)
- B4 `MappaProprieta:34`: un valore null si salva come la stringa "null". (P, latente)
- B5 `ResidenteMD:56`: la chiave `CAMPIONE:<nome>` non passa da `pulisciCampo`. (P)
- B6 `GruppoMD:167`, `RegistroPersonaggiMD:130`, `RegistroArtefattiMD:195`: `leggi` aggiunge invece di sostituire, contro la javadoc. Oggi è innocuo grazie a `reimposta()`. (C, latente)
- B7 `GruppoGiocatoreMD:222`: con un file troncato si aggiunge null agli ospiti, e la lettura della sola testata passa senza errore. (C)
- B8 `ForestaMD:48, 59, 234`: `offset()` non controlla i limiti, e gli array restano null prima di `reimposta`. (P)
- B9 `CoordinateMD:27, 35`: è mutabile ma usata come chiave di mappa; i setter non hanno chiamanti. (C)
- B10 `TipoDanno:68`: `getEffetti()` espone l'array interno dell'enum. `ModelloDati:27`: l'istanza non è `volatile`. (P)
- B11 `Costanti:249`: commento fuori posto (POZIONI_SALUTE_INIZIALI). `Comando.SINGOLO`, `RUTTOLOMEO` e `STORPSGORBLIN` sono in uso solo per le icone. (C)

**foresta-contratti**

- B12 `strumenti/Misc:177-231`: a distanza 0 esce "a zero ore di cammino verso ovest", per esempio quando la città distrutta è quella in cui si trova il gruppo. (C)
- B13 `strumenti/Misc:57-63`: `getOrdinaleImpl` dà "nullo" per 0 e un'eccezione da 42 in su. (P)
- B14 Costanti di `TipoEvento` sbagliate: `InternoException` usa INTERNO_ERRORE; in swing, `InternoCreazioneSpriteInDissolvenza` e `…FumettoATempo` usano …A_TEMPO. Altri punti:
  - `ThreadPrincipale.SUBITO` esegue `accoda` subito, senza esecutore installato (P);
  - un `UUID.randomUUID()` per ogni evento;
  - `Dado:101` può andare in overflow su intervalli enormi (teorico).

**foresta-motore**

- B15 `PersonaggioBase:1083-1137`: `subSalute` non controlla `isVivo()`, quindi la morte viene annunciata due volte (collasso entropico, doppio danno periodico, doppia resa in duello). (C)
- B16 `PersonaggioBase:1130-1135`: chi muore per veleno o sanguinamento risulta "morto per troppa codardia" (FIXME). (C)
- B17 `PersonaggioBase:127-131`: il flag PNG dipende dal costruttore (costruttori `(int)` dei residenti), e BARDO e BARDO_DELLE_LOCANDE non sono coerenti. (C l'incoerenza)
- B18 `PersonaggioBase:796`: la preposizione articolata con un nome proprio dà un doppio spazio ("i gusti  di Pippo"). (C)
- B19 `PersonaggioBase:1486-1491`: `subStanchezza` notifica un valore negativo. `:1638`: la variazione temporanea del carico conta i modificatori due volte. `:1136-1146`: la notifica di morte esce prima di `setSalute(0)`. `:881-889`: `limitaEntroMassimi` può restituire un valore negativo. (C/P)
- B20 `LocazioneBase:969-971`: SINGOLO_ATTACCO è offerto in mischia ma ignorato. `:437-440` e `:1528-1533`: rami irraggiungibili, e il secondo manderebbe l'`Automa` in IllegalStateException (IN_COMBATTIMENTO non ha un gestore d'ingresso). (C, latente)
- B21 `CalcolatoreCombattimento:674, 705`: divisione per la FORZA del difensore senza la guardia `> 0` della riga 725. (P)
- B22 `tattiche/valutazione/Requisiti:201-207`: il Canto ipnotico non esclude gli immuni e può ripetersi a vuoto. (P)
- B23 `locazioni/Castello*.azzeraLocazione`: non chiamano `super`. (P, effetto minimo)
- B24 `motore/Automa:1482, 1517, 1531, 1566, 610`: un comando inatteso in MAPPA, INVENTARIO, TROFEI, TATTICHE o nella scelta della classe lancia un'eccezione invece di essere ignorato. (P)
- B25 `motore/LineaTemporale:83-90`: `oreFinoAlMattino` usa 8 scritto a mano invece di `PRIMA_ORA_DEL_MATTINO`. (C, fragilità)
- B26 `salvataggi/GestoreSalvataggiSuFile:91-131`: niente `sync()` prima della rinomina. Su Windows, se la seconda rinomina fallisce, resta solo il `.tmp`. (P)
- B27 `missioni/storie/NonSparateSulPianista:101, 187-208`: la condizione rivela la città più vicina anche quando la missione non parte. (C)
- B28 Missioni con ripieghi:
  - `LOggettoSmarrito:95-97, 177`: NPE con il ripiego in città (P);
  - `IlNegozio:197-199`: IllegalStateException senza rovine né boschi nel quadrante (P);
  - `LaLealta`: fallisce anche se il compagno caduto poi risorge (P, scelta di design);
  - le copie degli incarichi ripetibili non annunciano "NUOVA MISSIONE" (P).
- B29 `intermezzi/IntermezzoLibroTecnico:259, 284`: i libri trovati e non ancora letti sono statici e non si salvano. (P)
- B30 Testi: `SconfiggiLIdra:52` dice "è stato sconfitta". `offerte/Informazioni:396` ha un doppio spazio. (C)
- B31 `TipoMissione`: mancano le note "// Coperto da:" per LaSfidaDeiCampioni, IlCampione, IlGiroDelleSetteChiese, CronacheDiUnFegatoEroico, NessunBoccaleLasciatoIndietro e DisturbatoreDellaQuietePubblica. (C)
- B32 `testi/GrammarBean`:
  - `[chiave]` su una chiave solo assegnata passa la validazione ma fallisce a runtime (`:1630` contro `:2386`);
  - le produzioni derivate dipendono dall'ordine della HashMap (`:1150-1209`);
  - il messaggio di `:1142` stampa la riga invece del numero.

  Oggi nessuna grammatica usa queste forme. (C/P, latente)
- B33 `testi/ProduttoreDiTestiCasuale`:
  - `:427` (`indexOf("-")`), `:343` e `:557` fanno parsing senza controlli;
  - gli stream delle grammatiche non vengono chiusi (`:62-90`).

  Oggi le grammatiche sono conformi. (P)
- B34 `oggetti/GeneratoreArtefattiTabelle:400`: `conGrado` dà per scontato che il nome cominci con il nome comune. `offerte/Informazioni:384`: `tiraAncheAUnaFaccia(0)` senza castelli rimasti (non raggiungibile). `RegistroTrofei:104`: NPE senza un gestore impostato. (P)
- B35 Codice morto nel motore:
  - in `PersonaggioBase`: `calcolaStanchezza`, `subCarisma`, `add<Primario>`, `getQuantitaEffettoDiStato` e `puoPrendere(Artefatto)`;
  - `InternoPersonaggioArreso` non ha nessun iscritto;
  - i lanci di `Dado` sprecati in `impostaValoriDiPartenza`;
  - i passi di `MissioneAPassi` usati solo dai test;
  - le produzioni morte di `fiabe.txt` e `locande.txt`, fra cui 12 `NOTIZIE_CITTA` che non escono mai.

  (C)

**foresta-swing e foresta-app**

- B36 Iniziali e NPE nei testi:
  - `ui/schermate/Notiziario:68, 137, 195`: una notizia vuota o con caratteri non supportati lancia o scrive nel log a ogni fotogramma;
  - `substring(0, 1)` su un nome vuoto (`DisplayableCanvasScambiatoreArtefatti:295, 316`, `DisplayableCanvasIncantatore:52`);
  - `DisplayableCanvasIntroOutro:246-253` non ha il try/catch della selezione da caricare.

  (C/P)
- B37 Riquadri:
  - `DisplayableCanvasRiquadroGruppo:264` e `…IncantesimiEPozioni:192` passano a `setRitaglio` il Rectangle vivo;
  - `:217` e `:135` fanno `setClip(null)` invece di ripristinare il clip;
  - `DisplayableCanvasRiquadroLocazione:322` usa le coordinate degli avversari senza controllo di null;
  - `DisplayableCanvas:1205` fa la dissolvenza di morte anche a un PNG che risorge.

  (C/P)
- B38 Comportamento:
  - gli scorrimenti degli scambiatori non si azzerano alla riapertura;
  - "Nuova voce" nelle tattiche non mostra Sì/No;
  - la bozza delle tattiche si perde cambiando personaggio;
  - i nomi del riquadro di combattimento si accorciano di due caratteri alla volta;
  - il menu a discesa non si apre sulla scelta corrente;
  - `TestoTattica` usa `Locale.ITALIAN`;
  - il nome della classe in tattiche viene da `name()` invece che da `Testi`.

  (C)
- B39 Sprite:
  - `SpriteAnnuncioGlobale:145, 190`: la misura degli spazi (10) non coincide con il disegno (11), e la riga più larga si taglia;
  - `SpriteFumetto:122`: la punta usa la larghezza massima (latente).

  (C/P)
- B40 Thread e avvio:
  - `ForestaUI:87`: l'`invokeLater` di creazione parte prima delle iscrizioni, e `InternoFaseDiGioco` potrebbe perdersi (P);
  - il thread animatore di `DisplayableCanvas:518-536` legge campi non volatili (P);
  - `Prompt:184-189` si riprende il fuoco anche da un'altra finestra (P);
  - `BufferedImageBuilder:36` chiama `System.exit(1)` anche nei caricamenti pigri a partita in corso (C, nessun innesco oggi).
- B41 Codice morto e piccoli difetti:
  - quattro eventi con sprite mai pubblicati;
  - `ImageCache.imageMap`, `set`/`get(String)` e lo shutdown hook inutile;
  - `DoomdarkTextRectangle` (non 2x) e `NegozioAlchimistaPrecedente.gif` inutilizzati;
  - `fermaThreadAnimazione` mai chiamato;
  - `variaLivello` in `DisplayableCanvasRiquadroLocazione`;
  - `ComandiPossibili.add`/`set` usati solo dai test.

  (C)
- B42 `foresta-app`:
  - `SnifferBusEventi:114-115` si iscrive due volte a `InternoVenditaArtefatto`;
  - `:395` costruisce la stringa di log fuori dal fornitore;
  - `:315` ha l'etichetta doppia;
  - `Main:49-67` ignora senza log gli argomenti sconosciuti, e `ZOOM` non valido esce con un'eccezione nuda;
  - `FinestraDiProva.PannelloDadi`: la tendina non mostra il valore del trucco attivo dopo una ricostruzione (solo modalità di prova).

  (C)

### 5.4 Ipotesi controllate e scartate

- **Concorrenza delle collezioni.** Non c'è nessuna `ConcurrentModificationException` raggiungibile: si scorrono sempre copie (`getPersonaggiVivi`, `getTutteLeMissioni`), e il bus usa `CopyOnWriteArrayList`.
- **Ricompense ed esperienza.** Niente doppie ricompense o doppi completamenti di missione. Niente esperienza duplicata (`avversariSconfitti`).
- **Oggetti e prezzi.** Scambi, acquisti, vendite e prelievi non duplicano né perdono artefatti. Vendita al massimo al 75%, acquisto almeno all'80%.
- **`Dado` e trucchi.** I limiti sono coerenti, e un trucco fuori intervallo viene ignorato.
- **Risorse.** I glifi dei font sono coerenti. Le immagini referenziate esistono tutte, e le mappature delle immagini coprono tutti i tipi di personaggio, oggetto e incantesimo.
- **Grammatiche.** Nessun riferimento mancante; i marcatori `<mod:>` e `<danno:>` sono validi.
- **Testi delle enum.** Nessuna chiave mancante nei `.properties`.
- **Combattimento.** Niente round infiniti; la fine del combattimento si intercetta sempre, anche con le ondate.

## 6. Portabilità (riassunto)

Il dettaglio è in [`assessment_portabilita.md`](assessment_portabilita.md).

- **Già pronto:** il motore usa solo `java.io` e `java.util`, non ha dipendenze dal toolkit grafico, ha la cartella dei file iniettabile, il ciclo di vita dell'app e l'uscita affidata alla piattaforma.
- **Emerso dalla revisione:** prima di usare `StatoDelMotore.azzera()` e la ripresa su mobile vanno risolti M20 e M21.
- **Ancora aperto:**
  - `Costanti` come dati;
  - le uscite dal processo della UI Swing (`System.exit` in `BufferedImageBuilder`, `EXIT_ON_CLOSE`, che chiude senza passare dal motore).
- **Il grosso resta la UI.** Va riscritta (libGDX è la scelta più naturale per restare in Java) e va ripensato l'input per il tocco.

## 7. Rischi tecnici

| Rischio | Fatto | Gravità |
| :--- | :--- | :--- |
| Trofei e classifica | M18: un file rovinato o una costante rinominata cancellano trofei o classifica | Media |
| Copertura di `personaggi`, `locazioni`, `offerte`, `salvataggi` | 69%, 72%, 24%, 65% | Media: in `personaggi` e `locazioni` sta metà dei difetti medi |
| Errori silenziosi del bus | M19 | Media |
| Copia delle costanti `static final` | Una compilazione incrementale parziale può tenere valori vecchi | Bassa: basta `mvn clean` |
| Modalità verticale | Rotta: barra icone sovrapposta, area di contenuto fissa a 640 px in una finestra larga al massimo 400 (`todo.md`, `motore_grafico.md` §12) | Bassa sul desktop; rimandata in fondo |

## 8. Prossimi passi consigliati

1. **I difetti alti (A1-A4) sono corretti**, ciascuno con un test.
2. **Correggere i medi certi e a basso rischio:** M1, M2, M4, M10, M12, M16, M17, M18 e tutta la UI (M22-M27).
3. **Decidere le regole di gioco** dove la correzione cambia il bilanciamento: M6 (Morte), M8 (carico), M11 (fuga), M14 (sconfitti fra visite), B28 (LaLealta).
4. **Un vero gestore di `InternoException`** (M19).
5. **Bassi:** pulizia a lotti per modulo (codice morto, testi, javadoc).
6. **Bilanciamento:** come da [`todo.md`](todo.md).
7. **Tagliare le classi grandi** quando si interviene su di loro (§3).
8. **Modalità verticale**, per ultima.

## Osservazioni

- **Come è stata fatta la revisione.** Lettura integrale dei sorgenti, divisi in nove parti:
  - tipi e modellodati;
  - contratti;
  - motore, salvataggi ed eventi;
  - personaggi;
  - combattimento, locazioni, canti, incantesimi e tattiche;
  - missioni e intermezzi;
  - oggetti, offerte, testi e trofei;
  - schermate, riquadri, barra, tattiche e app;
  - sprite, testo, immagini, sfx e sviluppo.

  Ogni ipotesi è stata verificata leggendo i chiamanti; M1 e M8 sono stati ricontrollati a parte.
- **Misure.** Righe e file sono contati con `wc -l` su `src/main` e `src/test`. Test e copertura vengono da `mvn clean test` del 2026-10-19, tutti verdi.
- **Il rischio delle notizie malformate** del 2026-10-17 è ora B33: le grammatiche attuali sono conformi, quindi non si innesca.

## Da fare / idee aperte

- Ripetere le misure dopo ogni grande intervento, e confrontare le righe delle classi della tabella del §2.
- Valutare un report JaCoCo aggregato (`report-aggregate` in `foresta-app`).
- Un test che produca tutte le alternative delle grammatiche e le disegni con i font, per prendere B33 e i caratteri che il font non ha.
- Scrivere in `gestione_missioni.md` il vincolo di idempotenza dei `controlla*` delle missioni scritte a mano (M15).
