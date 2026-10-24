# Assessment di Foresta

Valutazione del codice al **2026-10-19** (revisione), con le misure e i difetti aggiornati al **2026-10-22**, con una revisione completa dei sorgenti modulo per modulo, dal basso (`foresta-tipi`) fino a `foresta-app`. I numeri sono misurati sul working tree di quel giorno (`wc -l`, `grep`, JaCoCo da `mvn clean test`); i giudizi sono segnati come **Giudizio**. I difetti ancora aperti sono nel §5, con un codice (B35, B41…) per richiamarli. Le cose da fare per il porting su Android e iOS sono in [`assessment_portabilita.md`](assessment_portabilita.md): qui c'è solo il riassunto.

## 1. Sintesi

- Il gioco è **completo e giocabile** da inizio a fine (creazione del gruppo, esplorazione, combattimento a round con tattiche, negozi, incantatore, missioni, intermezzi, trofei, salvataggi, classifica).
- La **separazione fra le parti è garantita dal compilatore**: sei moduli Maven in fila (tipi, modellodati, contratti, motore, swing, app). La UI non vede il motore, ma soltanto viste in sola lettura ed eventi.
- La **base di test è ampia e veloce**: 1672 test verdi (16 saltati); `tipi` e `modellodati` sono coperti quasi al 100% dai loro test, il motore all'83%.
- **L'infrastruttura è solida.** Il bus, il `Dado`, i salvataggi di partita, gli scambi di artefatti, i prezzi e le copie difensive sono stati controllati e sono a posto. Non c'è nessuna `ConcurrentModificationException` raggiungibile, e i salvataggi sono simmetrici campo per campo.
- Il **debito tecnico** resta concentrato nelle quattro classi grandi del motore e nello stato statico (§3).
- **Giudizio:** il progetto è in buona salute. Non ci sono difetti aperti di gravità alta o media: restano del codice morto (§5) e il debito tecnico (§3). L'orientamento verticale resta rotto (noto) ed è rimandato in fondo.

## 2. Misure

| Modulo | Codice (file / righe) | Test (file / righe) | Test eseguiti | Copertura righe (JaCoCo) |
| :--- | ---: | ---: | ---: | ---: |
| `foresta-tipi` | 57 / 6.600 | 12 / 1.100 | 69 | 99,9% |
| `foresta-modellodati` | 36 / 5.600 | 28 / 4.000 | 209 | 99,6% |
| `foresta-contratti` | 168 / 7.400 | 9 / 700 | 44 | 41%* |
| `foresta-motore` | 368 / 57.200 | 214 / 33.600 | 1232 (16 saltati) | 83% |
| `foresta-swing` | 96 / 22.400 | 14 / 1.900 | 59 | 27%* |
| `foresta-app` | 3 / 850 | 30 / 2.300 | 59 | 28% |
| **Totale** | **728 / 100.100** | **307 / 43.700** | **1672** | **67%** |

\* JaCoCo conta solo i test del modulo stesso: contratti è esercitato molto di più dai test del motore, e la UI dai test di `foresta-app`. Le percentuali di questi moduli sono quindi più basse del vero.

Nel motore la copertura è alta quasi ovunque (`trofei` 100%, `testi` e `intermezzi` 95%, `missioni` 95%, `combattimento` 94%, `canti` 93%, `incantesimi` 89%, `tattiche` 87%, `motore` 86%). È bassa in `offerte` (25%), `personaggi` (67%) e `locazioni` (73%); `salvataggi` sale all'80%.

Le classi più grandi (escluse le tabelle dei font bitmap `DoomdarkFont*`):

| Classe | Righe | Il 2026-10-19 |
| :--- | ---: | ---: |
| `testi/GrammarBean` | 2536 | 2533 |
| `personaggi/PersonaggioBase` | 2388 | 2356 |
| `motore/Automa` | 2365 | 2284 |
| `locazioni/LocazioneBase` | 2077 | 1944 |
| `ui/DisplayableCanvas` | 1681 | 1629 |
| `tipi/Costanti` | 1642 | 1625 |
| `combattimento/CalcolatoreCombattimento` | 1289 | 1283 |
| `missioni/MissioneAPassi` | 1286 | 1274 |
| `ui/tattiche/DisplayableCanvasTattiche` | 1195 | 1133 |
| `tipi/TipoMissione` | 1012 | 1012 |

## 3. Architettura

### Punti di forza

- **Moduli in fila, controllati dal compilatore.** Ogni modulo vede solo quelli sopra di lui. Quello che il compilatore non vede, cioè `java.awt`, `javax.swing`, `javax.imageio` e `javax.sound` fuori da `foresta-swing`, `Locale.ROOT`, `System.exit` e il `Logger`, lo controllano i test che leggono i sorgenti (`DipendenzeDalToolkitGraficoTest`, `ConvenzioniDiPortabilitaTest`).
- **La UI legge da viste e scrive con eventi.** `VistaPartita` e `VistaScambio` sono in sola lettura, e verso il motore vanno solo comandi con identificativi. Le collezioni del modello dati sono esposte in sola lettura, con copie nei costruttori.
- **Un solo thread per la logica.** Il bus consegna sempre sul thread principale (sul desktop l'EDT), dove girano anche i tick del temporizzatore: fra eventi e stato non ci sono corse. L'unico thread estraneo è l'animatore di `DisplayableCanvas`.
- **Salvataggi di partita.** `LettoreCampi` conserva i campi vuoti, i testi liberi passano da `pulisciCampo`, un file troncato dà `IOException`, e la scrittura passa da un file temporaneo. Ogni coppia `salva`/`leggi` è stata confrontata campo per campo, e sono simmetriche.
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
- **Codice morto.** C'è un po' di codice che non si raggiunge (B35, B41, §5):
  - quattro classi di eventi con sprite mai pubblicate (`InternoCreazioneSprite` + `AnnuncioGlobale`, `EffettoDiStato`, `FumettoATempo`, `InDissolvenza`): sono pronte e funzionanti, con il loro gestore in `DisplayableCanvas` e l'iscrizione nello sniffer, ma oggi gli sprite si creano direttamente. Si tengono come risorsa per usi futuri (chi vuole mostrare uno sprite senza conoscere il canvas pubblica l'evento) e non sono un difetto;
  - metodi di `PersonaggioBase` mai chiamati;
  - passi pronti di `MissioneAPassi` usati solo dai test;
  - produzioni delle grammatiche mai usate, fra cui 12 `NOTIZIE_CITTA` che non escono mai.
- **Test saltati.** I 22 test saltati sono simulazioni da lanciare a mano, non test rotti.
- **Rapporti stantii.** Dopo una rinomina o per misurare serve `mvn clean test`: senza `clean`, `surefire-reports` e `jacoco.exec` tengono classi che non esistono più.

## 5. Difetti aperti

Rimasti dalla revisione del 2026-10-19 e ricontrollati sul codice: tutti di gravità bassa, e tutti confermati (lo scenario si innesca). Il resto è stato corretto. I percorsi sono relativi a `…/` del modulo indicato.

### 5.1 foresta-motore

- B35a Codice morto in `PersonaggioBase`: `calcolaStanchezza`, `subCarisma`, `add<Primario>` (tranne `addCarisma`, che usa `LocazioneBase`), `getQuantitaEffettoDiStato` (la usa solo un test) e `puoPrendere(Artefatto)`.
- B35d Passi pronti di `MissioneAPassi` che nessuna missione usa (solo i test): `evitaCombattimento`, le sequenze di passi e `getPassiConIntermezzoInAttesa`. Gli altri li usano `LaForgia` e `IlViandante`.
- B35e Le produzioni morte di `fiabe.txt` e `locande.txt`, fra cui 12 `NOTIZIE_CITTA` che non escono mai (nessuna locanda ha l'identificativo `CITTA`).

### 5.2 foresta-swing e foresta-app

- B41c `DoomdarkTextRectangle` (non 2x) e `NegozioAlchimistaPrecedente.gif` mai usati.

### 5.3 Ipotesi controllate e scartate

- **Concorrenza delle collezioni.** Non c'è nessuna `ConcurrentModificationException` raggiungibile: si scorrono sempre copie (`getPersonaggiVivi`, `getTutteLeMissioni`), e il bus usa `CopyOnWriteArrayList`.
- **Ricompense ed esperienza.** Niente doppie ricompense o doppi completamenti di missione. Niente esperienza duplicata (`avversariSconfitti`).
- **Oggetti e prezzi.** Scambi, acquisti, vendite e prelievi non duplicano né perdono artefatti. Vendita al massimo al 75%, acquisto almeno all'80%.
- **`Dado` e trucchi.** I limiti sono coerenti, e un trucco fuori intervallo viene ignorato.
- **Risorse.** I glifi dei font sono coerenti. Le immagini referenziate esistono tutte, e le mappature delle immagini coprono tutti i tipi di personaggio, oggetto e incantesimo.
- **Grammatiche.** Nessun riferimento mancante; i marcatori `<mod:>` e `<danno:>` sono validi.
- **Testi delle enum.** Nessuna chiave mancante nei `.properties`.
- **Cache delle scritte (`ImageCache.cacheDinamica`).** Funziona, e le `WeakReference` costano poco. Misurata il 2026-10-21 con un banco di prova esterno (headless, il vero `DoomdarkTextProducer` e il vero `drawImage`): 80 scritte per fotogramma a 60 fps, di cui una parte con un numero che cambia, 15 secondi per prova, spazzatura simulata da 20 a 200 MB/s, heap di default (16 GB di RAM) e `-Xmx96m`. Ricostruire una scritta (costruzione più prima conversione al disegno) costa circa 29 µs.
  - Senza cache si ricostruiscono tutte le scritte a ogni fotogramma: 4.800 al secondo, il 14-16% del tempo (circa 2,5 ms su 16,7).
  - Con `WeakReference` si ricostruiscono lo 0,3-0,5% delle richieste con 20 MB/s di spazzatura, l'1% con 60 MB/s e il 2,6% con 200 MB/s (126 al secondo); con 96 MB di heap si arriva al 6,6% (318 al secondo) a 200 MB/s. In tempo sono 0,3-0,7% (1,6% nel caso peggiore). Le scritte non si perdono a una a una: dopo ogni collezione del garbage collector si rigenerano in blocco, perché l'unico riferimento forte sparisce a fine `paint`.
  - Con `SoftReference` o con un riferimento forte si ricostruiscono sempre e solo i testi che cambiano (9 al secondo, lo 0,19%, 0,17% del tempo), in ogni condizione provata.
  - Limiti della misura: il carico è simulato (non ho misurato quanto alloca il gioco vero) e il costo di ricostruzione vale per questo Mac; su telefono, con una CPU da 3 a 10 volte più lenta, il blocco di ricostruzioni subito dopo un GC potrebbe costare 10-20 ms in un solo fotogramma. Se servirà, il passaggio a `SoftReference` è una modifica da poche righe (il reaper resta utile), da provare sul dispositivo.
- **Ordinali di `Misc` (ex B13).** `getOrdinaleM/F` vale da 1 a 41 (0 dà "nullo", da 42 in su un'eccezione), e nessun chiamante esce da lì: `DisturbatoreDellaQuietePubblica` 1–9, `Cofano` 1–5, i mostri che muoiono 1–6 (l'ordinale parte da 1 in `GruppoAvversario.aggiungiPersonaggio` e riparte a ogni ondata) e il giorno al più 40 (al 41 il gioco è già perso e il testo non si genera). Non è un difetto: l'intervallo è scritto nel javadoc, e non si stimano numeri più alti. La parte in lingua italiana di `Misc` va comunque rivista per una versione inglese (vedi `todo.md`).
- **Combattimento.** Niente round infiniti; la fine del combattimento si intercetta sempre, anche con le ondate.

## 6. Portabilità (riassunto)

Il dettaglio è in [`assessment_portabilita.md`](assessment_portabilita.md).

- **Già pronto:** il motore usa solo `java.io` e `java.util`, non ha dipendenze dal toolkit grafico, ha la cartella dei file iniettabile, il ciclo di vita dell'app e l'uscita affidata alla piattaforma.
- **Ancora aperto:**
  - `Costanti` come dati;
  - l'uscita dal processo della UI Swing (`EXIT_ON_CLOSE` in `ForestaUI`, che chiude senza passare dal motore).
- **Il grosso resta la UI.** Va riscritta (libGDX è la scelta più naturale per restare in Java) e va ripensato l'input per il tocco.

## 7. Rischi tecnici

| Rischio | Fatto | Gravità |
| :--- | :--- | :--- |
| Copertura di `personaggi`, `locazioni`, `offerte`, `salvataggi` | 69%, 72%, 24%, 65% | Media: sono le zone dove la revisione ha trovato più difetti, e hanno meno rete di sicurezza |
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
- **Misure.** Righe e file sono contati con `wc -l` su `src/main` e `src/test`. Test e copertura vengono da `mvn clean test` del 2026-10-22, tutti verdi.

## Da fare / idee aperte

- Ripetere le misure dopo ogni grande intervento, e confrontare le righe delle classi della tabella del §2.
- Valutare un report JaCoCo aggregato (`report-aggregate` in `foresta-app`).
- Un test che produca tutte le alternative delle grammatiche e le disegni con i font, per prendere le produzioni malformate e i caratteri che il font non ha.
