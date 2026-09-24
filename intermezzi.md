# Intermezzi: guida pratica

> Come si scrive un intermezzo (una cutscene), come si anima e come si prova.
> Per come il meccanismo è integrato nel motore e nella UI vedi [`motore_di_gioco.md`](motore_di_gioco.md) §7 e [`motore_grafico.md`](motore_grafico.md) §7.

Un intermezzo è una sequenza di **pagine** mostrate a tutto schermo quando si verificano certe condizioni di gioco. Ogni pagina può avere uno sfondo, delle immagini (anche animate e in movimento), un testo in alto e dei dialoghi a fumetti. Un intermezzo scatta **una sola volta per partita**; lo stato "già visto" viene salvato con la partita.

Ci sono due momenti di innesco (enum `MomentoIntermezzo`):

- **`INIZIO_GIOCO`**: una volta sola, all'inizio di una partita nuova, subito dopo la creazione del personaggio e **prima** dei controlli delle missioni. È il posto degli intermezzi d'apertura: le notifiche delle missioni non si sovrappongono all'intermezzo. Caricando un salvataggio non scatta.
- **`INIZIO_LOCAZIONE`**: all'arrivo in una nuova locazione, dopo i controlli delle missioni e gli eventi del tempo, prima che la locazione venga costruita e descritta. Se la partita è persa, nessun intermezzo parte.

---

## 1. Aggiungere un intermezzo in tre passi

1. **Scrivi la classe** in `intermezzi/`, implementando `Intermezzo`:

   ```java
   public class IncontroConLEremita implements Intermezzo {

       @Override
       public String getId() {
           return ClasseIntermezzo.INCONTRO_CON_L_EREMITA.name();
       }

       @Override
       public boolean deveScattare(MomentoIntermezzo momento) {
           // Il terzo giorno, se il gruppo sta per entrare in una radura
           return momento == MomentoIntermezzo.INIZIO_LOCAZIONE
                   && LineaTemporale.getGiorno() >= 3
                   && GruppoGiocatore.getIstanza().getClasseLocazioneCorrente() == ClassiLocazione.RADURA;
       }

       @Override
       public List<PaginaIntermezzo> getPagine() {
           // ... vedi i paragrafi seguenti
       }
   }
   ```

2. **Registrala** in `intermezzi/ClasseIntermezzo.java`, con una riga nell'enum:

   ```java
   INCONTRO_CON_L_EREMITA(IncontroConLEremita::new);
   ```

   L'ordine dell'enum conta: se due intermezzi scattano nello stesso momento, vengono mostrati uno dopo l'altro in quell'ordine. `getId()` deve restituire il nome della costante: è ciò che viene salvato per ricordare che l'intermezzo è già scattato.

3. **Provala** con l'anteprima (§9), senza giocare fino al punto in cui scatta:

   ```
   mvn -q compile
   java -cp target/classes com.threeamigos.foresta.ui.AnteprimaIntermezzo INCONTRO_CON_L_EREMITA
   ```

Quando non serve più, togli `INTERMEZZO_DI_PROVA` da `ClasseIntermezzo` (è segnato con un `FIXME`): scatta a ogni nuova partita.

### Scrivere l'innesco

`deveScattare` viene chiamato a ogni momento di innesco (all'inizio della partita e a ogni inizio locazione) finché l'intermezzo non è scattato. Può leggere tutto lo stato di gioco. Alcuni esempi:

| Condizione | Codice |
| :--- | :--- |
| Apertura della partita | `momento == MomentoIntermezzo.INIZIO_GIOCO` |
| Dal giorno N in poi | `LineaTemporale.getGiorno() >= N` |
| Missione principale completata | `RegistroMissioni.getMissionePrincipale().isCompleta()` |
| Il gruppo sta per entrare in un tipo di locazione | `GruppoGiocatore.getIstanza().getClasseLocazioneCorrente() == ClassiLocazione.X` |
| Nel gruppo c'è una certa classe | `GruppoGiocatore.getIstanza().getPersonaggiVivi().stream().anyMatch(p -> p.getClasse() == ClassePersonaggio.MAGO)` |

Attenzione: in quel momento il gruppo si è già spostato, ma la **nuova** locazione non è ancora costruita. `getClasseLocazioneCorrente()` legge il tipo dalla casella e quindi dice già dove si sta entrando. `getLocazioneCorrente()`, invece, restituisce ancora l'oggetto della locazione precedente, con i suoi mostri e il suo stato.

---

## 2. Un esempio completo

```java
@Override
public List<PaginaIntermezzo> getPagine() {
    Personaggio capo = GruppoGiocatore.getIstanza().getCapo();
    String nome = capo.getNomeProprio().orElseThrow(Personaggio.PERSONAGGIO_SENZA_NOME);

    return Arrays.asList(
        // Pagina 1: solo testo in alto e il protagonista sotto
        new PaginaIntermezzo("Il sentiero si apre su una radura silenziosa.")
            .conElemento(ElementoIntermezzo.personaggio("eroe", capo.getClasse(), 0.5, 2.0 / 3)),

        // Pagina 2: scena con sfondo, un drago in lontananza, un fuoco e un dialogo
        new PaginaIntermezzo()
            .conSfondo(ImmagineIntermezzo.locazione(ClassiLocazione.RADURA))
            .conElemento(ElementoIntermezzo.personaggio("drago", ClassePersonaggio.DRAGO, 1.1, 0.25)
                .conScala(0.4).conOpacita(0.6)                      // piccolo e semitrasparente
                .poi(Tappa.inSecondi(8).verso(-0.1, 0.15))          // attraversa il cielo
                .ripeti(Ripetizione.AVANTI_E_INDIETRO)              // e torna indietro
                .orientaNelVersoDelMoto(Verso.SINISTRA))            // guardando dove va
            .conElemento(ElementoIntermezzo.di("fuoco",
                ImmagineIntermezzo.animazione(Animazione.FUOCO_DA_CAMPO), 0.5, 0.76))
            .conElemento(ElementoIntermezzo.personaggio("eroe", capo.getClasse(), 0.3, 0.7))
            .conElemento(ElementoIntermezzo.personaggio("eremita", ClassePersonaggio.EREMITA, 1.1, 0.7)
                .specchiato()                                        // guarda verso l'eroe
                .poi(Tappa.inSecondi(2).verso(0.7, 0.7)))           // entra da destra
            .conBattuta(BattutaIntermezzo.di("eremita", "Chi va là?").daSecondo(2))
            .conBattuta(BattutaIntermezzo.di("eroe", "Mi chiamo " + nome + ", e cerco il Drago."))
            .conBattuta(BattutaIntermezzo.di("eremita", "Allora guarda in alto, e prega di non trovarlo.")));
}
```

È la stessa scena dell'`IntermezzoDiProva`: aprila con l'anteprima per vederla girare.

Le pagine vengono costruite **quando l'intermezzo scatta**, quindi possono dipendere dalla partita: il nome e la classe del protagonista, chi c'è nel gruppo, quale città è stata distrutta, eccetera. Se un intermezzo restituisce una lista vuota viene saltato, ma risulta comunque scattato.

---

## 3. Come è fatta una pagina

Una `PaginaIntermezzo` si disegna a strati, dal fondo verso l'alto. Tutti gli strati sono facoltativi:

1. **Sfondo** (`conSfondo`): un'immagine scalata per coprire tutto lo schermo, mantenendo le proporzioni. Se manca c'è l'ombra del drago su fondo nero, come nei messaggi grandi.
2. **Elementi** (`conElemento`), nell'ordine in cui li aggiungi: ogni elemento aggiunto dopo sta sopra i precedenti. Metti prima le cose di sfondo, poi i personaggi.
3. **Testo** in alto (`new PaginaIntermezzo("...")`), centrato e con a capo automatico.
4. **Fumetti** delle battute (`conBattuta`), sempre sopra tutto.

### Coordinate

- Le posizioni sono **frazioni dello schermo**: x da 0 (bordo sinistro) a 1 (bordo destro), y da 0 (bordo superiore) a 1 (bordo inferiore). Valgono a qualunque risoluzione.
- Il punto indicato è il **centro dell'immagine**.
- Valori fuori da 0-1 mettono l'elemento fuori schermo. Si usano per le entrate e le uscite: per esempio si parte da x = 1.1 per entrare da destra.

Qualche misura per orientarsi, con la finestra normale in orizzontale (area di 1022×732 pixel):

| Cosa | Misura |
| :--- | :--- |
| Un personaggio | alto circa 140 px, cioè circa 0.19 dell'altezza; largo 60-125 px, cioè 0.06-0.12 della larghezza |
| Il testo in alto | circa 36 px per riga dall'alto: due righe arrivano a circa y = 0.12 |
| "Personaggi in piedi a terra" | y fra 0.65 e 0.75 lascia spazio sopra per i fumetti |
| Cielo, per elementi di sfondo | y fra 0.1 e 0.3 |

### Due alfabeti diversi

- Il **testo in alto** usa l'alfabeto grande: solo lettere, cifre e `' , . ?`. Le lettere accentate si scrivono con l'apostrofo (`e'`, `perche'`). I due punti, il punto esclamativo e le virgolette non compaiono.
- I **fumetti** usano un font più completo: anche `à è é ì ò ù`, `! : ; " ( ) - / % +`. Lì si scrive italiano normale. Se un fumetto contiene un carattere non supportato non viene disegnato, e l'anteprima stampa l'errore.

---

## 4. Immagini

Un'immagine si indica con `ImmagineIntermezzo`:

| Tipo | Come | Note |
| :--- | :--- | :--- |
| Personaggio | `ImmagineIntermezzo.personaggio(ClassePersonaggio.X)` oppure la scorciatoia `ElementoIntermezzo.personaggio(id, classe, x, y)` | Le stesse immagini delle locazioni |
| Illustrazione di una locazione | `ImmagineIntermezzo.locazione(ClassiLocazione.X)` | Adatta come sfondo |
| Risorsa qualsiasi | `ImmagineIntermezzo.risorsa("intermezzi/Tramonto.png")` | Percorso relativo a `src/main/resources/com/threeamigos/foresta/img/` |
| Sprite sheet | `ImmagineIntermezzo.spriteSheet("intermezzi/Pipistrello.png", colonne, righe, fotogrammiAlSecondo)` | Vedi sotto |
| Animazione via codice | `ImmagineIntermezzo.animazione(Animazione.X)` | Vedi §8 |

Per le risorse nuove consiglio una cartella dedicata, `img/intermezzi/`. Vengono caricate solo quando l'intermezzo parte e dimenticate quando finisce, quindi non pesano sul resto del gioco. Se un file manca, l'elemento viene saltato e l'errore stampato: il gioco non si ferma.

### Sprite sheet

Uno sprite sheet è un'immagine divisa in una griglia di **fotogrammi della stessa dimensione**. I fotogrammi si numerano da 0, riga per riga: da sinistra a destra, poi la riga successiva.

```
+---+---+---+
| 0 | 1 | 2 |
+---+---+---+      spriteSheet("intermezzi/Pipistrello.png", 3, 2, 8)
| 3 | 4 | 5 |      = 3 colonne, 2 righe, 8 fotogrammi al secondo
+---+---+---+
```

- Senza altri parametri si mostrano tutti i fotogrammi in ordine, all'infinito.
- Si può indicare la sequenza da mostrare, per esempio avanti e indietro: `spriteSheet("intermezzi/Pipistrello.png", 3, 2, 8, 0, 1, 2, 1)`.
- Il fotogramma dipende dal tempo della pagina, quindi uno sprite sheet si può muovere con le tappe come qualunque altra immagine.
- Anche lo sfondo può essere uno sprite sheet.

---

## 5. Animare gli elementi

Ogni `ElementoIntermezzo` parte da uno **stato iniziale**: posizione data alla creazione, `conScala(s)`, `conOpacita(o)` (da 0 invisibile a 1 pieno), `specchiato()`. Poi percorre una sequenza di **tappe**, in modo lineare:

```java
ElementoIntermezzo.personaggio("goblin", ClassePersonaggio.GOBLIN, -0.1, 0.7)
    .conOpacita(0)
    .poi(Tappa.inSecondi(1).verso(0.2, 0.7).conOpacita(1))  // entra da sinistra comparendo
    .attendi(3)                                               // resta fermo 3 secondi
    .poi(Tappa.inSecondi(0.5).conScala(1.5))                  // si "gonfia"
    .poi(Tappa.inSecondi(1).verso(-0.2, 0.7).specchiata(true)) // si gira e scappa
```

- Ogni tappa indica la durata e **solo i valori che cambiano**. Gli altri restano quelli della tappa precedente.
- `attendi(secondi)` è una tappa in cui non cambia niente.
- Il tempo parte da 0 all'inizio della pagina.

**Ripetizione**, con `.ripeti(...)`:

| Valore | Effetto |
| :--- | :--- |
| `UNA_VOLTA` (default) | Si ferma sull'ultima tappa |
| `CICLICA` | Ricomincia dalla prima (salto allo stato iniziale) |
| `AVANTI_E_INDIETRO` | Ripercorre le tappe al contrario, all'infinito |

**Verso dell'immagine**, cioè da che parte guarda:

- `specchiato()`: parte rovesciata orizzontalmente.
- `Tappa.specchiata(true/false)`: cambia verso per quella tappa. Vale per tutta la tappa, non si interpola.
- `orientaNelVersoDelMoto(Verso.X)`: guarda sempre dove va, indicando da che parte guarda l'immagine originale. Per esempio l'immagine del drago guarda a sinistra, quindi si usa `Verso.SINISTRA`. Funziona anche nella fase di ritorno di `AVANTI_E_INDIETRO`. Quando l'elemento non si muove in orizzontale vale il verso delle tappe.

---

## 6. Dialoghi a fumetti

```java
.conBattuta(BattutaIntermezzo.di("eremita", "Chi va là?"))         // detta da un elemento
.conBattuta(BattutaIntermezzo.daPunto(0.95, 0.2, "Aiuto!"))         // da fuori scena
```

- **Chi parla**: con `di(id, testo)` il fumetto punta alla bocca dell'elemento con quell'identificativo e lo segue se si muove o si gira. L'elemento deve essere **già stato aggiunto** alla pagina, altrimenti c'è un errore subito. Con `daPunto(x, y, testo)` la punta va a un punto fisso dello schermo.
- **La bocca**: per default è in alto al centro dell'immagine. Si sposta con `conBocca(x, y)` sull'elemento, in frazioni dell'immagine: (0, 0) è l'angolo in alto a sinistra. Per esempio `conBocca(0.4, 0.2)` per un personaggio con la testa spostata.
- **Quando**: senza `daSecondo(s)`, una battuta parte alla fine della precedente più 0,3 secondi di pausa. La prima parte a 0.
- **Quanto dura**: senza `perSecondi(s)`, la durata dipende dalla lunghezza: 1 secondo più 0,06 per carattere, minimo 2 secondi.
- **Dove**: senza `nuvolaA(x, y)`, il fumetto va sopra chi parla, spostato verso il centro dello schermo, e resta sempre dentro lo schermo. Con `nuvolaA` indichi il centro del fumetto, in frazioni dello schermo.
- **Più fumetti insieme**: con `daSecondo` puoi sovrapporre battute nel tempo, per esempio due personaggi che parlano insieme.
- **Larghezza**: un fumetto è largo al massimo un terzo dello schermo e va a capo da solo.

**Dialoghi dinamici**: siccome le pagine si costruiscono al momento, i testi possono venire da variabili, da condizioni o da una grammatica di `GrammarBean`, come fanno già le notizie delle locande:

```java
String saluto = gruppo.getPersonaggiVivi().size() > 1 ? "Siete in tanti." : "Sei da solo?";
pagina.conBattuta(BattutaIntermezzo.di("eremita", saluto));
```

---

## 7. Quando avanzano le pagine

Una pagina avanza **sempre** al click sulla pergamena. Può anche avanzare da sola:

| Situazione | Quando avanza da sola |
| :--- | :--- |
| La pagina ha `perSecondi(s)` | Dopo `s` secondi, qualunque sia l'intermezzo |
| L'intermezzo ha `getSecondiPerPagina()` > 0 (default: `Costanti.SECONDI_PER_PAGINA_INTERMEZZO` = 8) | Dopo quei secondi, ma **non prima** che dialoghi e animazioni non ripetute siano finiti |
| L'intermezzo ridefinisce `getSecondiPerPagina()` restituendo 0 | Mai: solo al click |

Le animazioni ripetute (`CICLICA`, `AVANTI_E_INDIETRO`) non allungano la pagina. L'anteprima mostra nel titolo della finestra quando avanzerà la pagina corrente.

Il click fa avanzare anche a dialogo non finito, quindi il giocatore può saltare. Dopo l'ultima pagina si passa al prossimo intermezzo in coda, se c'è, altrimenti si torna al gioco.

---

## 8. Animazioni disegnate via codice

Per un'animazione che non è una sequenza di fotogrammi già pronti (un fuoco, una pioggia, un personaggio composto da più pezzi), si scrive una classe che fornisce il fotogramma istante per istante. Servono tre passi:

1. Una costante in `intermezzi/Animazione.java`: `PIOGGIA`.
2. Una classe in `ui/` che implementa `AnimazioneImmagine`:

   ```java
   class AnimazionePioggia implements AnimazioneImmagine {

       private final BufferedImage[] fotogrammi = new BufferedImage[6];

       AnimazionePioggia() {
           // Prepara qui tutti i fotogrammi, una volta sola
       }

       @Override
       public BufferedImage getFotogramma(double secondi, StatoElemento stato) {
           return fotogrammi[(int) (secondi * 12) % fotogrammi.length];
       }
   }
   ```

3. Una riga in `ui/AnimazioniIntermezzo.java`: `case PIOGGIA: return new AnimazionePioggia();`.

Poi la usi con `ImmagineIntermezzo.animazione(Animazione.PIOGGIA)`.

Qualche regola:
- **`getFotogramma` è chiamato 30 volte al secondo**: non creare immagini lì dentro, preparale nel costruttore.
- **Viene creata un'istanza per ogni elemento che usa l'animazione**, tenuta finché dura l'intermezzo. Può quindi avere uno stato suo.
- **`stato`** contiene posizione, scala, opacità e verso correnti dell'elemento (è null se l'animazione fa da sfondo). Serve, per esempio, a mostrare la camminata solo mentre l'elemento si sposta.
- **La dimensione** dell'immagine restituita è quella di disegno a scala 1. La scala dell'elemento si applica sopra.

`ui/AnimazioneFuocoDaCampo.java` è un esempio completo, senza nessuna risorsa grafica.

---

## 9. Provare un intermezzo: l'anteprima

`ui/AnteprimaIntermezzo` mostra un intermezzo senza dover giocare fino al punto in cui scatta. Prepara una partita minima, con una foresta nuova e il solo protagonista nel gruppo, e disegna le pagine con la stessa finestra e alla stessa dimensione del gioco. **L'innesco non viene controllato**: l'intermezzo viene mostrato comunque.

```
mvn -q compile
java -cp target/classes com.threeamigos.foresta.ui.AnteprimaIntermezzo [INTERMEZZO] [opzioni]
```

| Opzione | Significato | Default |
| :--- | :--- | :--- |
| `INTERMEZZO` | Nome della costante in `ClasseIntermezzo` | il primo dell'enum |
| `--classe CLASSE` | Classe del protagonista (`MAGA`, `LADRO`, ...) | `GUERRIERO` |
| `--nome NOME` | Nome del protagonista | `Aldric` |
| `--pagina N` | Pagina da cui partire, da 1 | 1 |
| `--png FILE` | Invece della finestra, salva una griglia: una riga per pagina (o solo quella di `--pagina`), una colonna per istante | — |
| `--istanti A,B,...` | Secondi da disegnare con `--png` | `0,1,2,4` |

**Nella finestra** le pagine avanzano da sole con le stesse regole del gioco. Il titolo mostra pagina, tempo trascorso e quando la pagina avanzerà. Tasti:

| Tasto | Azione |
| :--- | :--- |
| Spazio, freccia destra, click | Pagina successiva (dopo l'ultima si ricomincia dalla prima) |
| Freccia sinistra | Pagina precedente |
| R | Ricomincia la pagina |
| P | Pausa / riprendi |
| G | Rigenera le pagine (utile se il contenuto è casuale) |
| Esc | Chiudi |

**Con `--png`** ottieni un'immagine da guardare con calma, per esempio per controllare dove cadono i fumetti in istanti precisi:

```
java -cp target/classes com.threeamigos.foresta.ui.AnteprimaIntermezzo INTERMEZZO_DI_PROVA \
     --classe maga --nome Lyra --pagina 3 --png anteprima.png --istanti 1,3,6,9
```

Gli errori (immagini mancanti, caratteri non supportati nei fumetti) vengono stampati sulla console. Il programma usa la grafica di Java, quindi serve un display anche in modalità `--png`.

**Limite**: la partita è minima. Un intermezzo che nelle pagine legge uno stato particolare (altri personaggi nel gruppo, missioni completate, città distrutte) nell'anteprima troverà la situazione di inizio partita. Se ti serve, `motore/PartitaDiAnteprima.prepara(...)` è il punto dove aggiungere altro stato.

---

## 10. Riferimento rapido

**`PaginaIntermezzo`**

| Metodo | Effetto |
| :--- | :--- |
| `new PaginaIntermezzo()` / `new PaginaIntermezzo(testo)` | Pagina senza testo / con testo in alto |
| `conSfondo(immagine)` | Sfondo a tutto schermo |
| `conElemento(elemento)` | Aggiunge un'immagine; gli identificativi devono essere diversi |
| `conBattuta(battuta)` | Aggiunge un fumetto; chi parla deve essere già nella pagina |
| `perSecondi(s)` | Fissa quando la pagina avanza da sola |

**`ElementoIntermezzo`**

| Metodo | Effetto | Default |
| :--- | :--- | :--- |
| `di(id, immagine, x, y)` / `personaggio(id, classe, x, y)` | Crea l'elemento con il centro in (x, y) | — |
| `conScala(s)` | Scala iniziale | 1 |
| `conOpacita(o)` | Opacità iniziale, da 0 a 1 | 1 |
| `specchiato()` | Parte rovesciato orizzontalmente | no |
| `orientaNelVersoDelMoto(verso)` | Guarda dove va | no |
| `conBocca(x, y)` | Punto dei fumetti, in frazioni dell'immagine | 0.5, 0.15 |
| `poi(tappa)` / `attendi(s)` | Aggiunge un tratto di animazione / una pausa | nessuna tappa |
| `ripeti(ripetizione)` | Cosa fare a fine tappe | `UNA_VOLTA` |

**`Tappa`**

| Metodo | Effetto |
| :--- | :--- |
| `Tappa.inSecondi(s)` | Crea una tappa di `s` secondi |
| `verso(x, y)` | Posizione di arrivo |
| `conScala(s)` / `conOpacita(o)` | Scala / opacità di arrivo |
| `specchiata(true/false)` | Verso durante la tappa |

**`BattutaIntermezzo`**

| Metodo | Effetto | Default |
| :--- | :--- | :--- |
| `di(id, testo)` / `daPunto(x, y, testo)` | Chi parla / punto fisso | — |
| `daSecondo(s)` | Quando compare | fine della precedente + 0,3 s (la prima a 0) |
| `perSecondi(s)` | Quanto dura | 1 s + 0,06 s per carattere, minimo 2 s |
| `nuvolaA(x, y)` | Centro del fumetto | sopra chi parla, verso il centro |

**`ImmagineIntermezzo`**: `personaggio(classe)`, `locazione(classe)`, `risorsa(percorso)`, `spriteSheet(percorso, colonne, righe, fps, sequenza...)`, `animazione(Animazione)`.

**`Intermezzo`**: `getId()`, `deveScattare(momento)`, `getPagine()`, `getSecondiPerPagina()` (default 8; 0 = solo click).

---

## 11. Consigli e limiti

- **Prova sempre con l'anteprima**, anche con classi e nomi diversi (`--classe`, `--nome`): nomi lunghi allungano i fumetti, e classi diverse hanno immagini di larghezza diversa.
- **Lascia spazio sopra i personaggi** che parlano: il fumetto va sopra la loro testa. Con i personaggi a y = 0.7 c'è spazio per 3-4 righe.
- **Tieni le pagine brevi**: il giocatore può saltarle con un click. Le informazioni importanti per il gioco vanno anche nel testo normale o nelle missioni, non solo in un intermezzo.
- **Due momenti di innesco per ora** (`INIZIO_GIOCO` e `INIZIO_LOCAZIONE`). Altri momenti, per esempio dentro una locazione con i mostri già presenti, si aggiungono all'enum `MomentoIntermezzo` e a un punto di controllo nell'automa.
- **Se il timer scatta nello stesso istante di un click** la pagina può avanzare di due. Succede solo se i due eventi cadono a pochi millesimi di secondo l'uno dall'altro.
