# I testi delle missioni: dai `.properties` alla grammatica

Questo documento dice **dove sono** i testi delle missioni, **dove vogliamo arrivare**, **come sono fatti i due esempi già portati** (`CacciaAiGoblin` e `RichiestaDiMateriali`) e **come si porta una missione nuova, passo per passo**. È scritto perché il lavoro si possa riprendere senza altro contesto. Lo stato del lavoro è nel §9.

Documenti collegati: [`gestione_missioni.md`](gestione_missioni.md) (il meccanismo delle missioni), [`passi_missioni.md`](passi_missioni.md) (il catalogo), [`GrammarBean.md`](GrammarBean.md) (la sintassi della grammatica), [`01-foresta-testi.md`](01-foresta-testi.md) e [`09-foresta-missioni.md`](09-foresta-missioni.md) (i moduli).

## 1. Dove sono i testi (prima del porting)

I testi delle missioni stavano in tre posti:

| Che cosa | Dove | Formato |
| :--- | :--- | :--- |
| titoli, descrizioni, battute delle scene, frasi dei passi: tutto il testo scritto a mano | `09-foresta-missioni/src/main/resources/…/testi/missioni_{it,en,en_GB}.properties` (580 righe per `it` e `en`; `en_GB` ha solo le 27 che differiscono) | chiavi `Classe.N` (battute), `Classe.tN` (ritorni di metodo), `Classe.sN` (altre frasi), più alcune a nome (`.descrizione`, `.uno`/`.molti`, `.M`/`.F`); segnaposto `{0}`, `{1}`… letti con `Testi.formatta("missioni", "Chiave", valori…)` (una ricerca in una mappa e dei `replace`, non `MessageFormat`) |
| le parti a caso: nomi, materiali, oggetti smarriti, incarichi di combattimento, ecc. | `01-foresta-testi/src/main/resources/…/testi/missioni.txt` (+ `_en`, `_en_GB`), letto da `ProduttoreDiMissioniCasuale` | grammatica `GrammarBean` |
| le righe delle missioni a tappe, le sfide, i nomi | `tappe*.txt`, `sfide*.txt`, `nomi*.txt` (anche `04-foresta-artefatti/…/leggendari*.txt` per le leggende) | grammatica |

Le versioni inglesi dei `.txt` hanno le stesse righe di contenuto dell'italiano (1743 in tutti e tre i file di `missioni`); le righe in meno sono commenti. `missioni_en_GB.txt` è una **copia completa** di `missioni_en.txt` con le grafie britanniche, non un elenco di differenze come i `.properties`: ogni correzione fatta nell'inglese americano va ripetuta a mano.

## 2. Dove vogliamo arrivare

**Tutti i testi delle missioni diventano produzioni di `missioni.txt`**, un file per lingua (`missioni.txt`, `missioni_en.txt`, `missioni_en_GB.txt`), e i `missioni_<lingua>.properties` spariscono. Non c'è un file per missione e non c'è un `#include` fra i testi delle missioni: `missioni.txt` contiene tutto, comprese le produzioni a caso che c'erano già. (L'unico `#include_static <nomi.txt>` è l'ultima riga del file, e resta lì.)

Le regole decise, nell'ordine in cui ne abbiamo parlato:

1. **Una produzione per ogni testo, di solito una foglia con una sola alternativa.** Così «va già bene com'è»: un testo fisso è una produzione con una riga. La varietà (più alternative, accordi di genere e numero fatti dalla grammatica) si aggiunge solo dove la si vuole, ed è il vero guadagno della grammatica rispetto ai `.properties`. **Non** si convertono i dialoghi in sequenza a produzioni «a caso»: una scena è una conversazione ordinata.
2. **Il copione.** Le produzioni di una missione stanno **vicine** nel file, **nell'ordine in cui il giocatore le incontra**: titolo, battute del primo intermezzo, testi che possono accadere durante la missione, battute del secondo intermezzo, ricompensa. In una missione a passi in successione basta questo; in una missione a scelte si ordinano per ramo, con un commento di intestazione per ogni ramo. **È una convenzione, non una regola applicata**: `GrammarBean` non dà significato all'ordine in cui le produzioni sono scritte (conta solo la prima, che è la radice di default), l'ordine vero dei testi lo decide il codice. Il copione serve a leggere e rivedere le traduzioni, e può andare fuori sincrono col codice se ci si dimentica di aggiornarlo.
3. **Nomi delle produzioni.** `CLASSE_RUOLO` in maiuscolo con `_`, per esempio `CACCIA_AI_GOBLIN_INCARICO_2`, `RICHIESTA_DI_MATERIALI_DESCRIZIONE_RITORNO`. Sono **globali** a tutta la grammatica: il prefisso con la missione serve a non scontrarsi con le altre. (Verificato: `GrammarBean` accetta anche nomi con il punto o in minuscolo, quindi `CacciaAiGoblin.t1` funzionerebbe; abbiamo scelto la convenzione maiuscola delle altre grammatiche.)
4. **Un solo modo per i segnaposto: `%PARAM_NOME%`.** Il nome comincia **sempre** per `PARAM_`, seguito dal nome del valore: `%PARAM_CITTA%`, `%PARAM_GOBLIN%`, `%PARAM_MONETE%`, `%PARAM_MANDANTE_DI_CITTA_MAIUSC%`. Nel codice la chiave è la stessa senza i `%`: `"PARAM_CITTA"`. Niente più `{0}`, `{1}`, niente virgolette attorno ai segnaposto: i nomi non dipendono dall'ordine (l'inglese poteva usare i valori in un ordine diverso dall'italiano) e si cercano con un `grep`. Il prefisso serve anche a un'altra cosa: una chiave del codice come `"PARAM_DA_DOVE_VIENE"` è fatta di parole italiane e `ConvenzioniDiLocalizzazioneTest` la conterebbe come frase italiana nel codice; quel test **ignora** ogni stringa che comincia per `PARAM_`, e `riempi` rifiuta le chiavi che non cominciano così.
   - Storia: si era pensato di quotare i `{n}` (`"{0}"`) perché la grammatica rifiuta un gruppo `{…}` con una sola opzione; ma avere `{0}` e `%MANDANTE%` insieme era «due modi per fare la stessa cosa», quindi si è unificato sui nomi.
5. **Le forme del mandante.** Lo stesso mandante compare in forme diverse nella frase (con la città o senza, con la virgola finale o senza, con la preposizione, con l'iniziale maiuscola). Ogni forma è un segnaposto a sé: `PARAM_MANDANTE`, `PARAM_MANDANTE_DI_CITTA`, `PARAM_A_MANDANTE_SENZA_VIRGOLA`, `PARAM_A_MANDANTE_DI_CITTA_SENZA_VIRGOLA`…; con il suffisso `_MAIUSC` la stessa forma con l'iniziale maiuscola. In codice le forme sono quelle dei metodi di `IncaricoInCitta` (`mandante`, `mandanteDiCitta`, `mandanteSenzaVirgolaFinale`, `mandanteDiCittaSenzaVirgolaFinale`, `mandanteIn`, `mandanteInSenzaVirgolaFinale`, `mandanteEChiesto`) più `Lingue.corrente().conPreposizione(…)` e `inizialeMaiuscola(…)`.
6. **La lingua è fissa per tutta la partita.** Una partita che comincia in una lingua continua in quella (i nomi dei personaggi e degli artefatti, generati a runtime, restano come sono): quindi salvare nei dati un testo già prodotto non pone problemi di lingua.
7. **Il nome della missione si salva in `MissioneMD.nome`.** Si crea una sola volta, la prima volta che serve, e da lì è quello salvato (`MissioneBase.getNome()` + `creaIlNome()`).
8. **La descrizione è un *modello* per ogni passo.** All'ingresso in un passo si produce il modello della descrizione di quel passo, si **riempiono subito i valori stabili** (la città, il mandante, i numeri che non cambiano) e si **salva il risultato** nella proprietà `DESCRIZIONE_DEL_PASSO`. I valori che cambiano mentre il passo dura (i contatori: "Finora: 2") restano segnaposto e si riempiono a **ogni lettura** di `getDescrizione()`. Così il testo prodotto non cambia da una lettura all'altra, si salva con la partita, e non si riproduce a ogni refresh della UI. **Non** si produce tutto all'inizio: si produce quel che serve quando serve; la coerenza fra testi prodotti in momenti diversi la danno i parametri salvati nella missione (il mandante, il materiale, la città…), ripassati ai segnaposto.

Cose valutate e scartate, per non rifarle:

- **Salvare il testo già formattato** per ogni stato: la descrizione cambia anche dentro lo stesso passo (i contatori), quindi si salva il modello, non il testo finito.
- **Calcolare la descrizione a ogni lettura dalla grammatica** (come oggi dai `.properties`): per le foglie è deterministico, ma con produzioni a caso il titolo cambierebbe a ogni lettura. Il costo del `.properties` non è comunque il problema (una ricerca in una mappa): i guadagni sono la stabilità e un posto solo.
- **Un array di tutte le descrizioni prodotto alla creazione** (come fa `TappeRichieste` con la riga di campi `RICHIESTA=…;BATTUTA=…`): buono per le missioni a passi lineari, ma si preferisce produrre quando serve, perché le missioni a scelta non conoscono alla creazione i passi che verranno. `TappeRichieste` resta com'è.
- **Parametri a `GrammarBean`**: l'unico modo per far entrare un valore dal codice è `addFixedProduction(nome, valore)`, che vale per una sola `produce()` e dà una stringa già pronta (senza varianti né accordi); è pensato per pochi valori ripetuti nella stessa frase, non per tutte le frasi delle missioni. Per questo i valori si riempiono **dopo**, con `riempi`, e non passando dalla grammatica.

## 3. Il meccanismo

### `ProduttoreDiMissioniCasuale` (modulo `01-foresta-testi`)

| Metodo | Che cosa fa |
| :--- | :--- |
| `testoDellaMissione(produzione, coppie…)` | produce la foglia e riempie i segnaposto con le coppie chiave-valore: `testoDellaMissione("CACCIA_AI_GOBLIN_FINE_CACCIA", "PARAM_CITTA", getNomeCitta())` |
| `modelloDellaMissione(produzione)` | il testo della produzione com'è, con i `%PARAM_…%` ancora da riempire |
| `riempi(modello, coppie…)` | sostituisce ogni `%PARAM_X%` con il suo valore; un segnaposto senza coppia **resta com'è**, così il modello si riempie a più riprese; lancia `IllegalArgumentException` se le coppie sono dispari o una chiave non comincia per `PREFISSO_DEI_PARAMETRI` (`"PARAM_"`) |

### `MissioneBase` (modulo `09-foresta-missioni`)

- `getNome()` restituisce `md.getNome()`; se è `null` chiede `creaIlNome()` (default `null`: la missione lo dà da sé, ridefinendo `getNome()` come prima), lo salva con `md.setNome` e lo restituisce.
- Una missione portata **non ridefinisce più `getNome()`**: ridefinisce `creaIlNome()`.

### `MissioneAPassi` (modulo `09-foresta-missioni`)

Quattro ganci, tutti con un default che lascia le altre missioni come sono:

| Gancio | Che cosa deve dare |
| :--- | :--- |
| `produzioneDellaDescrizione(String passo)` | il nome della produzione con il modello della descrizione di quel passo, o `null` se la missione compone la descrizione da sé |
| `valoriStabiliDellaDescrizione()` | coppie chiave-valore dei segnaposto che **non cambiano** finché la missione dura: si riempiono subito, all'ingresso nel passo, prima di salvare |
| `valoriDellaDescrizione()` | coppie chiave-valore dei segnaposto che **cambiano** durante il passo (i contatori): si riempiono a ogni lettura |
| `descrizioneDalModello()` (non si ridefinisce) | la descrizione: legge `DESCRIZIONE_DEL_PASSO`, riempie i valori che cambiano; la missione la chiama dal suo `getDescrizione()` |

Come funziona: `impostaPassoCorrente(id)` (l'unico punto da cui passano tutte le transizioni, chiamato dopo l'azione del passo) chiama `conservaIlModelloDellaDescrizione(id)`: produce il modello, lo riempie dei valori stabili e lo salva in `DESCRIZIONE_DEL_PASSO` (o toglie la proprietà se la produzione è `null`). **Il passo iniziale** non ha un ingresso e i parametri della missione (la città, il materiale…) possono non esserci ancora: per lui `descrizioneDalModello()` produce e riempie ogni volta **senza salvare**. È un'attenzione da ricordare: se il passo iniziale avesse un modello a caso, la sua descrizione potrebbe cambiare finché la missione non avanza (per le foglie non succede).

Una proprietà si salva con la partita e **non** ha vincoli di formato che toccano questi testi: `pulisciCampo` toglie solo `|` e gli a capo (e il testo prodotto dalla grammatica non può avere `|`, che è la sua sintassi).

## 4. Il primo esempio: `CacciaAiGoblin`

La missione più semplice: un mercante in città chiede di sconfiggere 3 goblin, si torna a riscuotere. Passi: `INCARICO` → `ACCETTAZIONE` (da `IncaricoInCitta`) → `CACCIA` → `RITORNO` → `RICOMPENSA`.

**La grammatica** (`missioni.txt`, in fondo, prima di `#include_static <nomi.txt>`; lo stesso blocco, tradotto, in `missioni_en.txt` e `missioni_en_GB.txt`): tutte le righe sono vicine, in ordine di copione.

```
// ---------------------------------------------------------------------------------------------
// Caccia ai goblin (vedi CacciaAiGoblin), in ordine di copione. I segnaposto %PARAM_CHIAVE% li riempie il codice (vedi ProduttoreDiMissioniCasuale.riempi)
// ---------------------------------------------------------------------------------------------

// Il titolo
CACCIA_AI_GOBLIN_TITOLO
    Caccia ai goblin

// La scena dell'incarico, in città: il mercante parla (%PARAM_GOBLIN% da sconfiggere, %PARAM_MONETE% di ricompensa) e il capo risponde
CACCIA_AI_GOBLIN_INCARICO_1
    Ehi, voi! Avete l'aria di gente che sa menare le mani.

CACCIA_AI_GOBLIN_INCARICO_2
    I goblin assaltano le mie carovane appena escono dalla città. Ne sconfiggete %PARAM_GOBLIN% e vi do %PARAM_MONETE% monete.

CACCIA_AI_GOBLIN_INCARICO_3
    Goblin? Affare fatto.

// Il giocatore accetta
CACCIA_AI_GOBLIN_ACCETTAZIONE
    Il mercante pagherà %PARAM_MONETE% monete per %PARAM_GOBLIN% goblin sconfitti.

// Passo della caccia: la descrizione nel diario (%PARAM_CITTA%, %PARAM_GOBLIN% da sconfiggere, %PARAM_SCONFITTI% finora: l'unico che cambia durante il passo)
CACCIA_AI_GOBLIN_DESCRIZIONE_CACCIA
    Un mercante di %PARAM_CITTA% ti ha chiesto di sconfiggere %PARAM_GOBLIN% goblin. Finora: %PARAM_SCONFITTI%.

// Sconfitto l'ultimo goblin
CACCIA_AI_GOBLIN_FINE_CACCIA
    Di goblin, per un po', non se ne vedranno: è ora di tornare dal mercante di %PARAM_CITTA%.

// Passo del ritorno: la descrizione nel diario
CACCIA_AI_GOBLIN_DESCRIZIONE_RITORNO
    Le strade sono libere: torna dal mercante di %PARAM_CITTA% a riscuotere.

// La scena del ringraziamento, in città
CACCIA_AI_GOBLIN_RINGRAZIAMENTO_1
    Le carovane passano di nuovo! Siete stati di parola.

CACCIA_AI_GOBLIN_RINGRAZIAMENTO_2
    E adesso tocca a te.

// La ricompensa
CACCIA_AI_GOBLIN_RICOMPENSA
    Il mercante paga le %PARAM_MONETE% monete promesse.
```

**Il codice** (`09-foresta-missioni/…/incarichi/CacciaAiGoblin.java`):

```java
@Override
protected String creaIlNome() {                       // il titolo: una foglia, salvata in MissioneMD.nome
    return ProduttoreDiMissioniCasuale.testoDellaMissione("CACCIA_AI_GOBLIN_TITOLO");
}

@Override
public String getDescrizione() {
    return descrizioneDalModello();                    // niente più if sul passo: lo fa la produzione scelta
}

@Override
protected String produzioneDellaDescrizione(String passo) {
    return RITORNO.equals(passo) ? "CACCIA_AI_GOBLIN_DESCRIZIONE_RITORNO" : "CACCIA_AI_GOBLIN_DESCRIZIONE_CACCIA";
}

@Override
protected Object[] valoriStabiliDellaDescrizione() {    // si riempiono all'ingresso nel passo, prima di salvare
    return new Object[] {"PARAM_CITTA", getNomeCitta(), "PARAM_GOBLIN", GOBLIN_DA_SCONFIGGERE};
}

@Override
protected Object[] valoriDellaDescrizione() {           // si riempiono a ogni lettura
    return new Object[] {"PARAM_SCONFITTI", getConteggioNelPassoCorrente(eventoSconfitto(TipoPersonaggio.GOBLIN))};
}

// gli altri testi si producono dove servono, con i soli valori che usano:
.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("CACCIA_AI_GOBLIN_INCARICO_2", "PARAM_GOBLIN", GOBLIN_DA_SCONFIGGERE, "PARAM_MONETE", getRicompensa()))
```

Nei dati salvati, durante la caccia, `DESCRIZIONE_DEL_PASSO` vale `Un mercante di Fleena ti ha chiesto di sconfiggere 3 goblin. Finora: %PARAM_SCONFITTI%.`; al ritorno `Le strade sono libere: torna dal mercante di Fleena a riscuotere.`. Il test `ScenarioIncarichiInCittaTest.laDescrizioneDellaCacciaSiConservaNeiDatiConIlContatoreDaRiempire` lo controlla (anche dopo un salvataggio e un caricamento).

## 5. Il secondo esempio: `RichiestaDiMateriali`

Un mandante in città (alchimista, armaiolo, capitano, locandiere) chiede dei materiali da raccogliere, e si torna a consegnarli. Differenze rispetto alla caccia:

- **Il titolo lo compone il codice** (`Lingue.corrente().elenco(mandante, materiale)`): è ancora in `creaIlNome()`, e poi si conserva in `MissioneMD.nome`. Non è una produzione.
- **Le frasi hanno molti valori**, e quasi tutti stabili. `valori()` (privato) costruisce **tutte** le coppie in una volta: i valori del materiale (`PARAM_QUANTI`, `PARAM_TUTTI`, `PARAM_PRONOME`, `PARAM_MATERIALE`, `PARAM_PLURALE`, `PARAM_POSSESSIVO`, `PARAM_DA_DOVE_VIENE`, `PARAM_DA_DOVE_VIENE_FRASE_MAIUSC`, `PARAM_RISPOSTA_DEL_MANDANTE`, `PARAM_ADP_QUANTI_MAIUSC`…), le monete, e le forme del mandante (`PARAM_MANDANTE_MAIUSC`, `PARAM_MANDANTE_DI_CITTA`, `PARAM_MANDANTE_DI_CITTA_MAIUSC`, `PARAM_A_MANDANTE_SENZA_VIRGOLA`, `PARAM_A_MANDANTE_DI_CITTA_SENZA_VIRGOLA`, `PARAM_ALLA_CONSEGNA`). Ogni testo riceve `valori()` intero e ne usa quelli che gli servono: `riempi` ignora le coppie che non servono.
- **L'unico valore che cambia** è `PARAM_RACCOLTI` (il contatore dei materiali), in `valoriDellaDescrizione()`; `valoriStabiliDellaDescrizione()` restituisce `valori()`.
- **Alcune battute non sono sue**: l'apertura del mandante (`mandante.getApertura(…)`), la battuta del capo e la risposta (`materiale.getBattutaDelCapo()`, `getRispostaDelMandante()`) vengono dalle righe a campi di `missioni.txt` (famiglia `RICHIESTA_ALCHIMISTA`…) e restano dove sono.
- **L'ordine delle frasi nei due casi cambia** (l'inglese `RICHIESTA_DI_MATERIALI_INCARICO_2` usa «I'll pay %PARAM_MONETE% coins for %PARAM_PRONOME%»; l'italiano «Ve %PARAM_PRONOME% pago %PARAM_MONETE% monete»): con i nomi non si deve più badare all'ordine.

Il copione del blocco, nell'ordine in cui accade: `INCARICO_1`, (battuta del capo dal materiale), `INCARICO_2`, `INCARICO_3`, `ACCETTAZIONE`, `DESCRIZIONE_RACCOLTA`, `PRONTI` (raccolto tutto), `DESCRIZIONE_RITORNO`, `RINGRAZIAMENTO_1`, `RINGRAZIAMENTO_2`, `CONSEGNA`, `RICOMPENSA`. Il blocco intero è in `missioni.txt` sotto «Richiesta di materiali (vedi RichiestaDiMateriali), in ordine di copione» (e nelle versioni inglesi).

Nota: l'italiano e l'inglese possono avere forme grammaticali diverse (`conPreposizione(Preposizione.A, …)`, `getPronome()`): quel lavoro lo fanno le regole di `Lingue` e il materiale, non la grammatica; la grammatica mette solo il risultato nel segnaposto.

## 6. Come si porta una missione: passo per passo

0. **Prima di cominciare** (come dice `CLAUDE.md`): è un cambio nella forma del codice. Se la missione chiede una scelta nuova di disegno, si dice cosa si vuole fare e si aspetta l'ok. Si procede **una missione alla volta**, per passi che compilano, e si mostra il risultato prima del commit (che si fa solo su richiesta).
1. **Leggere la classe** e annotare ogni chiamata `Testi.formatta("missioni", "Classe.…", …)` (anche nei metodi usati da altre classi: `grep -rn "Classe\." --include=*.java`). Le chiavi della classe sono nei `.properties` `it` e `en`; `en_GB` ne ha poche (controllare con `grep "^Classe" …missioni_en_GB.properties`).
2. **Elencare per ogni testo il suo ruolo e il suo momento** (titolo, scena dell'incarico, accettazione, descrizione del passo X, fine del passo, ringraziamento, ricompensa…) e **ordinarli in copione**. Decidere i nomi `CLASSE_RUOLO`.
3. **Per ogni valore che oggi entra come `{n}`** scegliere il nome `PARAM_…` (e un suffisso `_MAIUSC` se serve l'iniziale maiuscola). Dire se è **stabile** (non cambia finché la missione dura: città, mandante, quantità, monete) o **dinamico** (un contatore). Le forme del mandante: vedi §2, punto 5.
4. **Scrivere il blocco in `missioni.txt`**, in fondo prima di `#include_static <nomi.txt>`, con un'intestazione `// ---` e un commento per scena. Copiare il testo **identico** dal `.properties`, solo con i `{n}` sostituiti. Attenzione alla sintassi: nei testi non vanno `[ ] { } |` e le virgolette dritte `"` (non sono un escape fuori da uno span; si usano `“ ”`, come vuole la convenzione del progetto). Le righe che iniziano con spazi: 4 spazi di indentazione davanti al testo.
5. **Ripetere il blocco in `missioni_en.txt` e `missioni_en_GB.txt`** (stessi nomi di produzione e di segnaposto), prendendo il testo dai `.properties` inglesi; `en_GB` è una copia completa con le grafie britanniche. `ConfrontoGrammaticheTest` e `VariantiBritannicheDelleGrammaticheTest` controllano che le produzioni e le alternative coincidano.
6. **Cambiare la classe**:
   - ogni `Testi.formatta("missioni", "Classe.X", …)` diventa `ProduttoreDiMissioniCasuale.testoDellaMissione("CLASSE_RUOLO", "PARAM_…", valore, …)`;
   - togliere l'override di `getNome()` e metterci `creaIlNome()` (se è una foglia: `testoDellaMissione("CLASSE_TITOLO")`; se lo compone il codice, lo stesso corpo di prima);
   - sostituire il corpo di `getDescrizione()` con `descrizioneDalModello()` e implementare `produzioneDellaDescrizione(passo)`, `valoriStabiliDellaDescrizione()`, `valoriDellaDescrizione()` (gli `if` sul passo spariscono: li fa la produzione scelta);
   - togliere l'`import` di `Testi` se non serve più. I metodi di aiuto condivisi (`mandante…`, `TestiDeiLuoghi`, `MaterialeRichiesto`…) hanno le loro chiavi: si portano con la prima missione che li usa o a parte, e il loro lavoro non deve cambiare.
7. **Togliere le chiavi portate** da `missioni_it.properties` e `missioni_en.properties` (e da `_en_GB` se ci sono). Una chiave ancora usata da un'altra classe **non** si toglie.
8. **Verificare che il testo è identico a prima** (per le parti fisse): confrontare, per `it`, `en` ed `en_GB`, il testo vecchio (dal `.properties` in git: `git show HEAD:…/missioni_it.properties`) con quello nuovo con gli stessi valori. È la prova che la post-produzione delle preposizioni articolate (attiva su `missioni.txt`: `deiTesti("missioni.txt", true)`) non ha cambiato le frasi: se una frase cambia, si decide se è un miglioramento o un problema.
9. **Scrivere o estendere un test di coerenza** (vedi §7).
10. **Eseguire i test**: `mvn -o test` dalla radice. Sempre con la radice (o con `-pl … -am`): con `-pl` da solo Maven usa i jar vecchi di `~/.m2` e dà errori che non c'entrano. **Non** fare `mvn install` per provare.
11. **Aggiornare i documenti**: la riga della missione in `passi_missioni.md` se cambia qualcosa, lo stato nel §9 di questo documento, e il documento del modulo se si tocca un modulo.
12. **Controllare il baseline** di `ConvenzioniDiLocalizzazioneTest` (`localizzazione_baseline.properties`, 09-foresta-missioni = 5 stringhe italiane nel codice): non deve salire; chi sposta testi nei file lo abbassa.

Missioni che **non** sono `IncaricoInCitta` (le principali, le storie, le leggende, le missioni a tappe): il meccanismo è lo stesso (`MissioneAPassi` ha i ganci), ma le scene e i testi sono nel codice della singola classe; il §9 elenca cosa resta. `MissioneATappe` legge già le righe di `tappe.txt` (una per missione, prodotta una volta e salvata): i suoi testi fissi (`MissioneATappe.*`, 28 chiavi) si portano come gli altri.

## 7. I test

- **`CoerenzaDeiTestiDegliIncarichiTest`** (in `12-foresta-motore/src/test/java/…/motore/`): per ogni missione portata percorre i testi in ordine, in **tutte le lingue** (`it`, `en`, `en-GB`: `Testi.impostaLingua`, da ripristinare in `finally`), e controlla: nessun `%…%`, `{}` o `null` rimasto; niente spazi doppi o ai bordi; ogni frase comincia con la maiuscola e finisce con la punteggiatura; gli stessi valori ricorrono dove devono (città, quantità, monete); il titolo è stabile ed è quello salvato in `MissioneMD.nome`. Un metodo per missione: `ITestiDellaCacciaAiGoblinParlanoDelloStessoIncarico`, `ITestiDellaRichiestaDiMaterialiParlanoDelloStessoIncarico`. Sono i modelli da copiare.
- **`TestiDiUnIncarico`** (in `12-foresta-motore/src/test/java/com/threeamigos/foresta/missioni/incarichi/`, stesso package delle classi): un aiuto che espone i testi protetti degli incarichi (le scene, `testoAccettazione`, `testoConsegna`, `testoRicompensa`) senza guidare l'automa. Le frasi di un passo che si pubblicano (`FINE_CACCIA`, `PRONTI`) si leggono con `partita.ultimoTesto()` subito dopo il controllo.
- **Provare che il test sappia fallire**: rompere di proposito un segnaposto di una lingua nel `.txt` (per esempio `%PARAM_MONETE%` → `%PARAM_MONETEX%`) e vedere il test segnalare la frase giusta; poi ripristinare. È stato fatto per il primo test.
- **`ScenarioIncarichiInCittaTest.laDescrizioneDellaCacciaSiConservaNeiDatiConIlContatoreDaRiempire`**: controlla `DESCRIZIONE_DEL_PASSO` (il modello riempito dei valori stabili e col contatore da riempire), il cambio di modello al ritorno e che sopravviva a un salvataggio e a un caricamento.
- Il controllo «le traduzioni hanno solo segnaposto esistenti» (`ConvenzioniDiLocalizzazioneTest.ogniTraduzioneHaSoloChiaviEsistentiInItalianoEGliStessiSegnaposto`) guarda **solo i `.properties`** e solo i `{n}`: per i testi portati non vale più, e lo sostituiscono i test di coerenza qui sopra, che però esistono solo per le missioni portate.

## 8. Le trappole incontrate

- **Un gruppo con una sola opzione è un errore.** `{0}` scritto nudo in una grammatica non si carica (`{a|b}` è l'alternanza). Per questo i `{n}` sono stati sostituiti da `%PARAM_…%`.
- **Una virgoletta dritta fuori da uno span** rompe la grammatica («Missing closing '"'»): nei `.properties` c'era una sola riga così (`LaForgia.t8`). Si usano `“ ”`.
- **Il test sulle stringhe italiane nel codice** (`ConvenzioniDiLocalizzazioneTest`) considera italiana una stringa con due parole comuni (`di`, `da`, `a`, `dove`, `del`…) dopo averla spezzata sulle non-lettere: `"DA_DOVE_VIENE"` lo faceva salire. Da qui la regola del prefisso `PARAM_`, che il test ignora. Per lo stesso motivo il test ignora anche le stringhe tutte maiuscole (`IL_CORRIERE_QUANDO_DI_NASCOSTO`: i nomi delle produzioni delle classi `Il…`/`La…` contengono parole come `IL`, `IN`, `DI`).
- **Sostituzioni di massa**: rinominando i segnaposto con un replace sul `"MATERIALE"` si è cambiato per errore anche la costante `RichiestaDiMateriali.MATERIALE` (la chiave del contatore e del parametro salvato). I replace sui sorgenti vanno limitati ai pezzi giusti e riletti con `git diff`.
- **`Lingue.corrente()`** restituisce `RegoleDellaLingua`, non `Lingue`.
- **`GrammarBean` non è thread-safe**: i produttori leggono dal thread del gioco; non chiamarli da altri thread senza la sincronizzazione che `ProduttoreDiMissioniCasuale` già usa dove serve.
- **I nomi delle produzioni sono globali**: sempre il prefisso con la missione.
- **Il passo iniziale non salva il modello** (§3): se i parametri non ci sono ancora si rischia di congelare un `null`.

## 9. Stato del lavoro e cosa resta

Ultimo aggiornamento: 2026-11-03. **Portate: tutti gli incarichi in città** — `CacciaAiGoblin`, `RichiestaDiMateriali`, `IlCorriere` (con `IlContrabbandiere`), `LaSorveglianza`, `IlSoccorso`, `IlRapimento`, `IlColpo`, `IlCartografo`, `ChiaveDelCustode`, `IlRituale`, `IlTorneo`, `IlPellegrino`, `LOggettoSmarrito`, `LaDocumentazione`, `LIndagine`, `LaTagliaSullaBanda`, `CacciatoreDiTaglie`, `LaForgia`, `IncaricoDiCombattimento` e `IlNegozio` con le sue quattro sottoclassi (tranne il mandante del venditore di pergamene, `IlNegozioDelVenditoreDiPergamene.s2`, che si porta con `Ruolo`). Le frasi condivise che questi incarichi chiamano ancora dai `.properties` sono `Incarico.aspettaA`, `Nemici.guidatiDa`, `TestiDeiLuoghi.*`, `Ruolo.*`, `Mandante.*`, `Genere.*`, `Luogo.*` e quelle di `MaterialeRichiesto`. Tutta la suite è verde (`mvn -o test`). **Il lavoro non è stato committato.**

Chiavi ancora nei `.properties` `it`, per classe:

| Gruppo | Prefissi |
| :--- | :--- |
| Incarichi in città: la base | `IncaricoInCitta` 10 (+ `Incarico` 1) |
| Storie e compagni | `IlCampione`, `IlFavore`, `IlGiroDelleSetteChiese`, `IlViaggioDellaMoglie`, `IlViandante`, `LaBenedizione`, `LaLealta`, `LaSfidaDeiCampioni`, `NonSparateSulPianista` |
| Principali | `SconfiggiIlDrago`, `SconfiggiIlLich`, `SconfiggiIlMinotauroGigante`, `SconfiggiLIdra`, `SconfiggiLaStrega`, `VisitaLocanda`, `DisturbatoreDellaQuietePubblica`, `NessunBoccaleLasciatoIndietro`, `CronacheDiUnFegatoEroico` |
| Leggende | `LaLeggenda`, `LaLeggendaDelLocandiere`, `LaFalceDelTristoMietitore`, `PescaLeggendaria` |
| A tappe | `MissioneATappe` |
| Epiloghi (intermezzi delle missioni) | `IntermezzoEremitaRompiscatole`, `IntermezzoUrloDellaBanshee` |
| Aiuti condivisi (non sono missioni) | `Genere`, `Luogo`, `Mandante`, `MaterialeRichiesto`, `Nemici`, `Ruolo`, `TestiDeiLuoghi`, `Missione`, `MissioneAPassi`, `OffertaDelSacerdote`, `Incarico`, `RitualeRichiesto`, `TorneoRichiesto` |

Per contarle: `grep -o '^[A-Za-z]*\.' 09-foresta-missioni/src/main/resources/com/threeamigos/foresta/testi/missioni_it.properties | sort | uniq -c`.

**Cosa si è imparato portando gli incarichi** (da rileggere prima di riprendere):

- **Mai chiamare la grammatica in un costruttore** (né in un inizializzatore statico che gira all'avvio): `testoDellaMissione` pesca dalla sorgente condivisa `Dado`, e un tiro in più all'avvio sposta la sequenza di tutte le partite con seme (`ScenarioUccisioniTest` si rompeva). I titoli fissi passati dalla sottoclasse (`IlNegozio*`) si passano come *nome della produzione* e si producono alla prima richiesta (`creaIlNome`).
- **Il titolo si salva solo quando è definitivo.** Se dipende da qualcosa che l'incarico sceglie in `allIncarico()` (`IlCorriere`: la destinazione) si ridefinisce `getNome()` perché non salvi il titolo provvisorio. I titoli che vengono dalla riga di grammatica (`testo(getTitolo())`) restano com'erano.
- **I valori stabili si calcolano a ogni ingresso in un passo**, anche prima che l'incarico sia offerto o prima che la missione abbia scelto le sue cose: `valori()` deve essere sicuro (`IlNegozio`, `IlTorneo`, `LaForgia` restituiscono solo i valori che già ci sono) e non deve far pescare parametri in anticipo (cambierebbe la sequenza casuale).
- **Le descrizioni che cambiano dentro un passo** (le visite mancanti della sorveglianza, gli indizi trovati dell'indagine o i reperti della documentazione) o hanno due varianti per lo stesso passo si risolvono così: il valore che cambia è un segnaposto (`valoriDellaDescrizione`), composto dal codice quando è una lista; la variante che cambia dopo l'ingresso (`LaSorveglianza`) si sceglie in `getDescrizione()` senza modello salvato.
- **`en_GB`**: i `.properties` `en_GB` hanno poche chiavi (grafie britanniche: «any more», «armourer»); se `togli(prefisso)` ne toglie, la grafia va riportata in `missioni_en_GB.txt` (l'aiuto di scrittura accetta un quinto testo per `en_GB`).
- **Difetti trovati e corretti per strada**: le frasi di `LaForgia` sul raffreddamento e sul ritiro cominciavano con l'iniziale minuscola («la Lancia … è nella forgia»); il testo inglese di `LaDocumentazione` aveva un « a » italiano nel ritorno; `IlNegozio.isPossibileQui` dava `NullPointerException` su una casella di città mai costruita (`MondiEstremiTest.conPocheCittaCostruite`).
- **I test**: `CoerenzaDeiTestiDegliIncarichiTest.controllaUnIncarico(classe, seme, passi, metodiExtra…)` controlla un incarico in tre lingue (scene, accettazione, ricompensa, descrizione di ogni passo, altri testi): per una nuova missione basta una riga. Se la missione non è un `IncaricoInCitta` (`ChiaveDelCustode`) si usa un aiuto in `TestiDiUnIncarico`. `ConvenzioniDiLocalizzazioneTest` ignora le stringhe tutte maiuscole (nomi di produzioni come `IL_CORRIERE_QUANDO_DI_NASCOSTO`).

Ordine suggerito: la base `IncaricoInCitta` e gli aiuti condivisi che servono, poi le storie, le leggende, le principali e `MissioneATappe`; per ultimo gli epiloghi, e quando i `.properties` sono vuoti si cancellano i tre file e si toglie il controllo che li riguarda da `ConvenzioniDiLocalizzazioneTest`.

**Da decidere o da rivedere** (non sono stati risolti):

- **Le forme del mandante nelle proprietà.** Si era detto che le forme (`A_MANDANTE`, `DEL_MANDANTE`…) si sarebbero salvate nelle proprietà della missione. Per ora **non** si salvano: `valori()` le ricalcola con i metodi di `IncaricoInCitta` a ogni uso, e il modello della descrizione le riceve già riempite. Vanno salvate solo se dipendono da qualcosa che può cambiare dopo la creazione (il nome del mandante da `nomeDelMandante(mandante, citta)`): non è stato verificato.
- **Il vecchio meccanismo dei segnaposto nelle righe a campi.** Le righe di `missioni.txt` e `tappe.txt` (`RICHIESTA=…;BATTUTA=…`) usano già `%NOME%`, `%CITTA%`, `%CAPO%`, riempiti dal codice di ciascuna famiglia (`CapoDellaRiga`, `TappeRichieste`, `IlNegozio` con `SEGNAPOSTO_NOME`…). Sono un meccanismo più vecchio e non seguono la regola `PARAM_`: da unificare quando si portano quelle famiglie (e con loro `TappeRichieste`, che controlla «i soli segnaposto ammessi»). Il documento dei campi è in [`passi_missioni.md`](passi_missioni.md) §5 e [`missione_a_tappe.md`](missione_a_tappe.md).
- **Missioni con scelte.** Il copione di una missione a rami va scritto per ramo; non c'è ancora un esempio portato. Verificare, quando si porta la prima, come i rami si traducono in `produzioneDellaDescrizione(passo)`.
- **`MaterialeRichiesto` e `TestiDeiLuoghi`**: le loro chiavi (`MaterialeRichiesto.t1`, `.t2`, `.s1`, `.s2` e le `.frase`, `TestiDeiLuoghi.*`) sono usate dalle missioni ma non sono di una sola: da portare a parte, prima delle missioni che le usano meglio.
- **Le grammatiche a caso** (`tappe.txt`, `sfide.txt`, `nomi.txt`, `leggendari.txt`) restano file propri, come prima: la regola «tutto in `missioni.txt`» vale per i testi che oggi stanno nei `.properties` delle missioni.
- Documentazione dei moduli: aggiornati `09-foresta-missioni.md`, `01-foresta-testi.md`, `gestione_missioni.md`, `passi_missioni.md`, `foresta.md` e `CLAUDE.md`; da rivedere quando si porta più di una manciata di missioni.

## 10. Osservazioni

- Il guadagno vero non è tecnico ma di lavoro sui testi: un posto solo per lingua, copione leggibile e la possibilità di dare varietà e accordi grammaticali dove servono. Se un testo non ha bisogno di varietà, resta una foglia e il porting è meccanico.
- Il costo è il lavoro sulle traduzioni (`it`, `en`, `en_GB`) e il rischio di regressioni nei testi: da cui il confronto con il testo vecchio (§6, punto 8) e i test di coerenza in tre lingue.
- Per rispondere alla domanda «da dove viene questo testo?» basta cercare la produzione in `missioni.txt`: per le missioni non ancora portate, cercare la chiave nel `.properties`.
