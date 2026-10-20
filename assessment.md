# Assessment di Foresta

Valutazione del codice al **2026-10-19**, con una revisione completa dei sorgenti modulo per modulo, dal basso (`foresta-tipi`) fino a `foresta-app`. I numeri sono misurati sul working tree di quel giorno (`wc -l`, `grep`, JaCoCo da `mvn clean test`); i giudizi sono segnati come **Giudizio**. I difetti trovati sono nel §5, con un codice (A1, M3, B12…) per richiamarli. Le cose da fare per il porting su Android e iOS sono in [`assessment_portabilita.md`](assessment_portabilita.md): qui c'è solo il riassunto.

## 1. Sintesi

- Il gioco è **completo e giocabile** da inizio a fine (creazione del gruppo, esplorazione, combattimento a round con tattiche, negozi, incantatore, missioni, intermezzi, trofei, salvataggi, classifica).
- La **separazione fra le parti è garantita dal compilatore**: sei moduli Maven in fila (tipi, modellodati, contratti, motore, swing, app). La UI non vede il motore, ma soltanto viste in sola lettura ed eventi.
- La **base di test è ampia e veloce**: 1543 test verdi (16 saltati; 1559 dopo le correzioni dei difetti); `tipi` e `modellodati` sono coperti quasi al 100% dai loro test, il motore all'85%.
- I **4 difetti di gravità alta** trovati dalla revisione (tutti nella logica di gioco, non nell'infrastruttura) sono stati corretti, ciascuno con un test.
- **L'infrastruttura è solida.** Il bus, il `Dado`, i salvataggi di partita, gli scambi di artefatti, i prezzi e le copie difensive sono stati controllati e sono a posto. Non c'è nessuna `ConcurrentModificationException` raggiungibile, e i salvataggi sono simmetrici campo per campo.
- **I difetti medi sono soprattutto regole di gioco incoerenti**: effetti di stato, ospiti, tempo in locanda, testi delle ricompense. Nella UI sono comportamenti sbagliati (§5.2).
- Il **debito tecnico** resta concentrato nelle quattro classi grandi del motore e nello stato statico (§3).
- **Giudizio:** il progetto è in buona salute. I difetti alti si sono corretti con interventi piccoli e locali, con un test ciascuno; i medi si affrontano a uno a uno. L'orientamento verticale resta rotto (noto) ed è rimandato in fondo.

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

1. **Classi grandi.** `PersonaggioBase`, `Automa` e `LocazioneBase` sono stabili dal 2026-10-17, ma restano le zone dove la revisione ha trovato più difetti: il combattimento a round in `LocazioneBase` e gli effetti di stato in `PersonaggioBase`.
   **Giudizio:** i tagli naturali restano quelli già indicati:
   - per `Automa`, altri sotto-automi (creazione del gruppo, combattimento, fine partita);
   - per `LocazioneBase`, il combattimento a round in una classe sua;
   - per `PersonaggioBase`, attributi e modificatori, equipaggiamento ed effetti di stato separati.
2. **Stato statico.** Lo stato della partita vive in registri e singleton. `StatoDelMotore.azzera()` chiude l'`Automa` in funzione e il vecchio `GruppoGiocatore` esce dal bus: per ripartire si crea un automa nuovo.
   **Giudizio:** accettabile per un gioco single-player, a patto di non aggiungere registri statici.
3. **Il motore è un modulo solo.** `motore`, `personaggi`, `locazioni`, `missioni` e `oggetti` sono un unico grafo. Il primo taglio possibile resta `foresta-missioni` ([`todo.md`](todo.md)).
4. **Due stili di missione.** Restano missioni che estendono `MissioneBase`. Il controllo dopo la risposta a una domanda rifà i `controlla*` di tutte le missioni, e quindi richiede che quelle scritte a mano siano idempotenti (il vincolo è scritto in `gestione_missioni.md`).

## 4. Qualità del codice

- **Gestione degli errori.** I `catch` generici stanno quasi tutti nei gestori su file e in `GrammarBean`, ed è una scelta difendibile.
- **`TODO`/`FIXME`:** Riguardano funzioni future o rifiniture.
- **Codice morto.** C'è un po' di codice che non si raggiunge (B35, B41):
  - quattro classi di eventi con sprite mai pubblicate (`InternoCreazioneSprite` + `AnnuncioGlobale`, `EffettoDiStato`, `FumettoATempo`, `InDissolvenza`): sono pronte e funzionanti, con il loro gestore in `DisplayableCanvas` e l'iscrizione nello sniffer, ma oggi gli sprite si creano direttamente. Si tengono come risorsa per usi futuri (chi vuole mostrare uno sprite senza conoscere il canvas pubblica l'evento) e non sono un difetto;
  - metodi di `PersonaggioBase` mai chiamati;
  - passi pronti di `MissioneAPassi` usati solo dai test;
  - produzioni delle grammatiche mai usate, fra cui 12 `NOTIZIE_CITTA` che non escono mai.
- **Convenzioni.** `TestoTattica` usa `Locale.ITALIAN` invece di `Locale.ROOT`. `SnifferBusEventi` costruisce una stringa di log fuori dal fornitore, e `ConvenzioniDiPortabilitaTest` non se ne accorge.
- **Test saltati.** I 16 test saltati sono simulazioni da lanciare a mano, non test rotti.
- **Rapporti stantii.** Dopo una rinomina o per misurare serve `mvn clean test`: senza `clean`, `surefire-reports` e `jacoco.exec` tengono classi che non esistono più.

## 5. Difetti trovati dalla revisione del 2026-10-19

Ogni voce è stata verificata sul codice chiamante. **C** sta per *confermato* (lo scenario si innesca), **P** per *plausibile* (dipende da condizioni rare o non ancora raggiungibili). I percorsi sono relativi a `…/` del modulo indicato.

### 5.1 Gravità alta

Nessun difetto aperto.

### 5.2 Gravità media

**Combattimento e personaggi**

| Cod. | Dove | Difetto | |
| :--- | :--- | :--- | :---: |

**Motore, missioni, trofei e testi**

Nessun difetto aperto.

**Interfaccia (foresta-swing)**

Nessun difetto aperto.

### 5.3 Gravità bassa

**foresta-tipi e foresta-modellodati**


**foresta-contratti**

- B13 `strumenti/Misc:57-63`: `getOrdinaleImpl` dà "nullo" per 0 e un'eccezione da 42 in su. (P)

**foresta-motore**

- B32 `testi/GrammarBean`:
  - `[chiave]` su una chiave solo assegnata passa la validazione ma fallisce a runtime (`:1630` contro `:2386`);
  - le produzioni derivate dipendono dall'ordine della HashMap (`:1150-1209`);
  - il messaggio di `:1142` stampa la riga invece del numero.

  Oggi nessuna grammatica usa queste forme. (C/P, latente)
- B35a Codice morto in `PersonaggioBase`: `calcolaStanchezza`, `subCarisma`, `add<Primario>`, `getQuantitaEffettoDiStato` e `puoPrendere(Artefatto)`. (C)
- B35c I lanci di `Dado` sprecati in `impostaValoriDiPartenza`. (C)
- B35d I passi di `MissioneAPassi` usati solo dai test. (C)
- B35e Le produzioni morte di `fiabe.txt` e `locande.txt`, fra cui 12 `NOTIZIE_CITTA` che non escono mai. (C)

**foresta-swing e foresta-app**

- B41b `ImageCache.imageMap`, `set`/`get(String)` e lo shutdown hook inutile. (C)
- B41c `DoomdarkTextRectangle` (non 2x) e `NegozioAlchimistaPrecedente.gif` mai usati. (C)
- B41d Il doppio caricamento di `spriteAumentoLivello`. (C)
- B41e `variaLivello` e il parametro `locazione` di `xOggetto` in `RiquadroLocazione` mai usati. (C)
- B41f `ComandiPossibili.add`/`set` usati solo dai test. (C)

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
- **Ancora aperto:**
  - `Costanti` come dati;
  - le uscite dal processo della UI Swing (`System.exit` in `BufferedImageBuilder`, `EXIT_ON_CLOSE`, che chiude senza passare dal motore).
- **Il grosso resta la UI.** Va riscritta (libGDX è la scelta più naturale per restare in Java) e va ripensato l'input per il tocco.

## 7. Rischi tecnici

| Rischio | Fatto | Gravità |
| :--- | :--- | :--- |
| Copertura di `personaggi`, `locazioni`, `offerte`, `salvataggi` | 69%, 72%, 24%, 65% | Media: in `personaggi` e `locazioni` sta metà dei difetti medi |
| Copia delle costanti `static final` | Una compilazione incrementale parziale può tenere valori vecchi | Bassa: basta `mvn clean` |
| Modalità verticale | Rotta: barra icone sovrapposta, area di contenuto fissa a 640 px in una finestra larga al massimo 400 (`todo.md`, `motore_grafico.md` §12) | Bassa sul desktop; rimandata in fondo |

## 8. Prossimi passi consigliati

1. **Bassi:** pulizia a lotti per modulo (codice morto, testi, javadoc).
2. **Bilanciamento:** come da [`todo.md`](todo.md).
3. **Tagliare le classi grandi** quando si interviene su di loro (§3).
4. **Modalità verticale**, per ultima.

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

  Ogni ipotesi è stata verificata leggendo i chiamanti.
- **Misure.** Righe e file sono contati con `wc -l` su `src/main` e `src/test`. Test e copertura vengono da `mvn clean test` del 2026-10-19, tutti verdi.

## Da fare / idee aperte

- Ripetere le misure dopo ogni grande intervento, e confrontare le righe delle classi della tabella del §2.
- Valutare un report JaCoCo aggregato (`report-aggregate` in `foresta-app`).
- Un test che produca tutte le alternative delle grammatiche e le disegni con i font, per prendere le produzioni malformate e i caratteri che il font non ha.
