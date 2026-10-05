# Analisi: il logoramento a inizio partita (2026-10-05)

Domanda: perché in una partita normale (non di prova) il protagonista muore dopo pochissimi scontri se non beve pozioni o non trova una locanda? L'ipotesi di partenza era che gli scontri siano "alla pari" finché il personaggio non ha un equipaggiamento migliore.

Strumento: `TestLogoramentoInizioPartita` (nei test, pacchetto `motore.modellodati`, `@Disabled`, da lanciare a mano). Riusa i metodi di `CombatSimulatorMatrix` e riproduce le condizioni reali di inizio partita:

- protagonista di **livello 1**, **da solo**, con la **dotazione di base** di `EquipaggiamentoIniziale` (Guerriero spada e scudo; Ladro, Elfo e Bardo spada e veste; Mago bastone e veste), **senza pozioni e senza usare pergamene**. *Correzione (2026-10-05): il gruppo in realtà parte con 100 monete, 5 preziosi e 3 pergamene ciascuna di Aria, Acqua e Terra (`GruppoGiocatore.reimposta`); il simulatore non le usa, quindi il gioco vero è un po' più facile di quanto misurato qui.*;
- incontri come in `LocazioneBase.crea` per bosco e radura: uno dei 14 mostri a caso, da 1 a `min(2, massimo per locazione)` esemplari fino al livello 5, **al livello del mondo** (qui quello del protagonista); un incontro il 90% delle volte, sempre alla prima locazione, mai l'Eremita alla prima;
- la salute **non si rigenera** fra uno scontro e l'altro (`getRigenerazioneSalute` è mostrata nell'inventario ma nessuno la usa), la stanchezza cresce di 1 per locazione, l'esperienza dei mostri uccisi fa salire di livello.

Il simulatore **non** considera gli effetti di stato (nel gioco rendono gli scontri più duri), la fuga, la corruzione, l'amicizia, le locande e i compagni.

Comandi:

```
mvn -q test -Dtest='TestLogoramentoInizioPartita#analisi' -Dsurefire.failIfNoSpecifiedTests=false \
  '-Djunit.jupiter.conditions.deactivate=org.junit.*DisabledCondition' -Djacoco.skip=true
```

(`#saltoDiLivello` e `#controlloDotazione` sono le altre due prove; l'analisi completa dura circa 2 minuti.)

## 1. Risultati

### Scontri singoli a livello 1, a salute piena

Vittorie / salute che resta quando si vince (1.000 scontri per casella):

| Mostro | Guerriero | Ladro | Elfo | Bardo | Mago |
| :--- | ---: | ---: | ---: | ---: | ---: |
| Goblin ×1 | 100% / 78% | 100% / 71% | 100% / 81% | 100% / 60% | 100% / 87% |
| Goblin ×2 | 100% / 59% | 92% / 38% | 100% / 68% | 57% / 22% | 100% / 79% |
| Scheletro ×1 | 100% / 76% | 100% / 66% | 100% / 82% | 99% / 54% | 100% / 87% |
| Hobgoblin ×2 | 94% / 35% | 37% / 22% | 100% / 61% | 4% / 13% | 100% / 75% |
| Troll ×1 | 94% / 35% | 33% / 25% | 99% / 59% | 5% / 19% | 100% / 70% |
| Minotauro ×1 | 94% / 38% | 38% / 26% | 100% / 60% | 10% / 20% | 100% / 69% |
| Centauro ×1 | 92% / 37% | 36% / 27% | 99% / 60% | 8% / 20% | 100% / 67% |
| Gigante ×1 | 35% / 18% | 2% / 20% | 77% / 39% | 0% | 94% / 46% |
| Titano ×1 | 9% / 7% | 0% | 38% / 16% | 0% | 65% / 16% |
| Chimera Drago ×1 | 12% / 14% | 1% / 11% | 72% / 36% | 0% | 97% / 58% |
| Troll ×2 | 6% / 10% | 0% | 42% / 25% | 0% | 74% / 31% |

### Una locazione dopo l'altra

Locazioni superate prima di morire (2.000 partite per classe). "A 5" è la quota di partite che arriva ad almeno 5 locazioni:

| Classe | Media | Mediana | A 5 |
| :--- | ---: | ---: | ---: |
| Guerriero | 1,1 | 1 | 1,6% |
| Ladro | 0,6 | 0 | 0,1% |
| Elfo | 1,5 | 1 | 2,8% |
| Bardo | 0,4 | 0 | 0,1% |
| Mago | 2,4 | 2 | 13,4% |

Cioè: **metà dei Ladri e dei Bardi muore nella prima locazione**, e quasi nessun protagonista arriva alla quinta.

## 2. Perché

L'ipotesi "scontri alla pari" è vera solo per metà. I mostri del bosco sono di due tipi molto diversi:

1. **I mostri leggeri** (Goblin, Scheletro, Folletto, Arpia, Hobgoblin) si battono quasi sempre, ma **ognuno costa il 20-30% della salute** (il 40-60% al Bardo), e in due il doppio. Senza recupero fra uno scontro e l'altro, bastano tre o quattro incontri leggeri per morire.
   Misura (Ladro con la spada contro un Goblin): colpisce l'84% delle volte per 13,4 di danno, il Goblin ha 60 di salute, quindi servono circa 5 turni, e in 5 turni il Goblin (57% per 7,1) toglie circa 20-25 punti dei 100 del Ladro.
2. **I mostri pesanti** (Troll, Minotauro, Centauro, Gigante, Titano, Chimera Drago) a livello 1 **sono più forti del protagonista**: più salute (170-300 contro 80-150) e più danno (16-27 a colpo contro 13-19). Il Ladro ne batte uno su tre, il Bardo quasi mai. Sono **6 dei 14 mostri del bosco**: quasi un incontro su due.

A questo si aggiungono tre cause strutturali:

3. **Nessun recupero.** Fra uno scontro e l'altro la salute non torna: servono pozioni, una locanda o l'accampamento. `getRigenerazioneSalute` esiste ma non è usata dal motore.
4. **Salire di livello indebolisce.** I mostri nascono al livello del mondo e le loro armi naturali hanno il livello del mostro (`ArmaNaturale.getLivello`), che nel danno fa da moltiplicatore; la spada del protagonista resta di livello 1. La salute massima di tutti cresce di `livellamento × √(livello − 1)`, cioè più che raddoppia dal livello 1 al 2 (Troll da 170 a 380), ma il protagonista **non recupera** salute quando sale di livello. A salute piena, con la spada iniziale, contro un mostro del suo livello:

   | | Livello 1 | Livello 2 | Livello 3 |
   | :--- | ---: | ---: | ---: |
   | Guerriero contro Troll | 93% | 33% | 7% |
   | Ladro contro Troll | 36% | 0,5% | 0% |
   | Ladro contro Goblin (salute che resta) | 71% | 63% | 42% |

   Fino a quando non trova un'arma nuova, ogni livello rende il protagonista più debole rispetto al mondo.
5. **Il danno delle armi è basso rispetto alla magia.** A livello 1 la spada fa 13-19 a colpo, il dardo arcano del Mago 86-89 (abbatte un Goblin al primo colpo). Per questo il Mago, la classe più fragile, è quella che dura di più.

## 3. Le leve provate

Locazioni superate, media (e quota che arriva a 5), per Guerriero / Ladro / Bardo / Mago:

| Variante | Guerriero | Ladro | Bardo | Mago |
| :--- | ---: | ---: | ---: | ---: |
| Oggi | 1,1 (2%) | 0,6 (0%) | 0,4 (0%) | 2,4 (13%) |
| Rigenerazione fra scontri (`getRigenerazioneSalute`) | 1,3 | 0,6 | 0,5 | 2,8 |
| Danno dei mostri al 70% | 1,5 | 0,9 | 0,6 | 3,1 |
| Salute dei mostri al 70% | 1,6 | 1,0 | 0,7 | 3,6 |
| Salute del protagonista +50% | 1,6 | 0,9 | 0,7 | 3,3 |
| Danno delle armi del protagonista ×2 | 1,9 | 1,2 | 0,8 | 2,4 |
| Ripresa del 25% della salute dopo ogni scontro vinto | 1,4 | 0,7 | 0,4 | 3,5 |
| Cura completa al passaggio di livello | 1,2 | 0,6 | 0,4 | 3,2 |
| Mostri un livello sotto il protagonista | 1,1 | 0,6 | 0,4 | 3,2 |
| 3 pozioni di salute | 2,3 | 1,6 | 1,2 | 4,3 |
| 10 pozioni di salute | 3,5 | 2,6 | 2,1 | 5,5 |
| Un mostro solo fino al livello 5 | 1,8 | 1,0 | 0,7 | 4,1 |
| Niente mostri pesanti fino al livello 3 | 2,1 | 1,1 | 0,7 | 4,5 |
| **Inizio morbido**: fino al livello 3 un mostro solo e niente pesanti | 3,5 (29%) | 1,9 (6%) | 1,3 (1%) | 7,5 (90%) |
| Inizio morbido + ripresa del 25% | 5,1 (63%) | 2,8 (23%) | 1,7 (7%) | 14,3 |
| Inizio morbido + ripresa del 25% + 3 pozioni | 6,8 (90%) | 5,4 (71%) | 4,3 (46%) | 15,3 |
| Inizio morbido + ripresa + cura al livello + mostri un livello sotto | 13,1 (72%) | 3,2 (22%) | 1,7 (8%) | 19,8 |
| **Come sopra + armi ×1,5** | **19,6 (93%)** | **8,5 (55%)** | **3,6 (26%)** | **19,8** |

(La simulazione si ferma a 40 locazioni.)

Cosa se ne ricava:

- **Nessuna leva da sola basta.** Ognuna sposta la media di qualche decimo: le prime locazioni uccidono prima che la leva abbia effetto, oppure (pozioni, salute) si consuma in fretta.
- **Le leve si sommano più che linearmente.** Togliere i mostri pesanti e i doppi all'inizio fa sopravvivere abbastanza da salire di livello; da lì contano la cura al passaggio di livello e i mostri un livello sotto, che da soli non servono a nulla.
- **Le 200 monete iniziali**, da sole, aiutano solo quando il protagonista arriva in una città con un alchimista: una pozione costa 5 monete e ridà 100 di salute, quindi 200 monete sono 40 pozioni, cioè molto più di quanto serve. Conviene darne meno, oppure dare **qualche pozione direttamente** (3 pozioni valgono già +1 / +2 locazioni).
- **Ladro e Bardo restano indietro** in ogni variante: il loro problema è il danno per colpo, non solo il logoramento.

## 4. Proposte, in ordine

1. **Inizio morbido** in `LocazioneBase.crea`: fino al livello 3 (o 2) un mostro solo e un elenco di mostri "da inizio partita" senza Troll, Minotauro, Centauro, Gigante, Titano e Chimera Drago. È la leva singola più forte e non tocca i numeri del combattimento.
2. **Recupero fra uno scontro e l'altro**: usare finalmente `getRigenerazioneSalute` (oggi troppo piccola: 10-15 punti) o, meglio, far riprendere fiato al gruppo dopo uno scontro vinto (circa il 25% della salute massima).
3. **Cura (anche parziale) al passaggio di livello**, oppure aumentare insieme salute attuale e massima: oggi salire di livello "svuota" la barra della salute in proporzione.
4. **Il livello dei mostri**: nascere un livello sotto il mondo, o far crescere le armi naturali più lentamente (oggi il livello moltiplica il danno), così che l'equipaggiamento del protagonista non resti indietro a ogni livello.
5. **Danno delle armi a basso livello**: la spada iniziale fa 13-19, il dardo arcano 86. Con le altre proposte, un ×1,5 sulle armi porta il Ladro da 3,2 a 8,5 locazioni medie. Da rivedere insieme al bilanciamento delle classi (§12-13 di `piano_montecarlo_matrix.md`).
6. **Economia**: qualche pozione nella dotazione iniziale (per esempio 3) invece di molte monete; le monete servono solo arrivati in città.

Le proposte 1-3 sono piccole e locali (un elenco di mostri, due righe dopo lo scontro, una al passaggio di livello). La 4 e la 5 toccano il bilanciamento generale e vanno rimisurate con `TestMonteCarloMatrix`.

## 5. Note

- I numeri del §11.5 di `piano_montecarlo_matrix.md` (Ladro con `SPADA` contro un Troll a livello 1: 65%) sono superati: oggi il simulatore dà il 35%, coerente con questa analisi. Il bilanciamento è cambiato dopo quel giro (moltiplicatori di classe, §12-13).
- Il protagonista del simulatore deve avere un nome: un personaggio senza nome è un PNG e `addPuntiEsperienza` lo ignora.

## 6. Modifiche applicate (2026-10-05) e nuovi risultati

Decise con l'autore dopo i §1-5:

- **Salute e danno crescono con la radice quadrata del livello, per tutti** (mostri, boss e personaggi giocanti). Salute massima = salute base × √livello (100, 141, 173, 200...), invece di base + livellamento × √(livello − 1), che dal livello 1 al 2 più che raddoppiava. `getLivellamentoSalute` e le costanti `*_LIVELLAMENTO_SALUTE` sono state tolte. Nel danno (`CalcolatoreCombattimento.livelloDiCombattimento`) il livello dell'arma e quello dell'attaccante valgono la loro radice quadrata. Uno scontro alla pari dura uguale a ogni livello, e l'equipaggiamento migliore resta il vantaggio. (Prima si è provata la radice solo per i mostri: dal livello 5 gli scontri comuni diventavano una formalità di 2-3 turni.)
- **Inizio morbido** (`LocazioneBase.incontriPossibili`): finché il capo è al livello 3 o sotto, mai Centauro, Chimera Drago, Gigante, Minotauro, Titano e Troll.
- **Numero di mostri secondo il gruppo** (`LocazioneBase.numeroMassimoDiMostri`): al massimo tanti quanti i personaggi in campo (gli ospiti non contano), più 1 fino al livello 5 del capo, 2 fino al 15, 4 oltre; durante l'inizio morbido nessuno in più. Per un protagonista solo: 1, 2, 3 e 5, come prima (salvo l'inizio morbido).
- **Recupero a fine locazione** (`Automa`, `FINE_LOCAZIONE_2`): ogni personaggio vivo recupera `getRigenerazioneSalute()`, ora 5 + 15% della salute massima, × (1 + √Costituzione / 10); i modificatori di `RIGENERAZIONE_SALUTE` valgono su tutta la base (prima solo sui 5 punti fissi). A livello 1 fa 29 al Ladro (su 100), 53 al Guerriero (su 150), 16 al Mago (su 80).
- **Nessuna cura al passaggio di livello.**
- **Tolleranza sull'arma**: la parte dell'eroe resta piena con un'arma fino a 2 livelli sotto (`TOLLERANZA_LIVELLO_ARMA`); oltre cala in proporzione.
- **Dardo arcano dimezzato**: 20 × livello al Mago (era 40), 15 all'Elfo (era 30). A livello 1 il Mago fa 46 a colpo invece di 86.
- **Armi basse più forti**: danno medio `max(4 + 2 × livello, 16 + livello)` (era 11 + livello): la spada iniziale fa 16-18 invece di 12-13.

### Locazioni superate a inizio partita, protagonista solo

| Classe | Prima | Ora | A 5 | A 10 | Senza recupero | Senza inizio morbido | Con 3 pozioni |
| :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| Guerriero | 1,1 | 10,1 | 92% | 44% | 5,0 | 1,9 | 15,0 |
| Ladro | 0,6 | 5,4 | 64% | 7% | 3,1 | 1,0 | 9,1 |
| Elfo | 1,5 | 5,3 | 62% | 5% | 3,3 | 1,2 | 8,6 |
| Bardo | 0,4 | 3,5 | 34% | 1% | 2,1 | 0,7 | 6,8 |
| Mago | 2,4 | 10,1 | 97% | 61% | 5,8 | 1,9 | 11,3 |

Recupero e inizio morbido servono entrambi. Il Bardo resta il più debole (il suo ruolo di supporto non c'è ancora).

**Pozioni iniziali** (`Costanti.POZIONI_SALUTE_INIZIALI`, 2 dal 2026-10-05, bevute sotto il 35% della salute): Guerriero 13,5 locazioni in media, Ladro 8,0, Elfo 7,7, Bardo 6,0, Mago 11,1; arriva ad almeno 5 locazioni il 76-100%. Le pergamene iniziali (3 di Aria, Acqua e Terra) non sono ancora simulate.

### La dotazione di livello 1 ai livelli successivi

Salute piena, contro un mostro del suo livello:

| | Livello 1 | 2 | 3 | 5 |
| :--- | ---: | ---: | ---: | ---: |
| Guerriero contro Troll | 98% | 97% | 80% | 49% |
| Ladro contro Troll | 67% | 44% | 23% | 4% |
| Ladro contro Goblin (salute che resta) | 76% | 84% | 79% | 64% |

Il salto di livello non è più un crollo; dal livello 3-5 la dotazione iniziale comincia a pesare, e serve cercare armi nuove.

### Livelli alti

Ogni classe con la sua prima dotazione tipica del suo livello, senza pergamene, da sola, contro avversari del suo livello (`TestLogoramentoInizioPartita#livelliAlti`, vittorie e turni medi):

| Livello | Avversari | Guerriero | Ladro | Elfo | Bardo | Mago |
| ---: | :--- | ---: | ---: | ---: | ---: | ---: |
| 5 | 1 Troll | 100% (6,6) | 98% (5,7) | 98% (6,5) | 92% (9,7) | 100% (1,9) |
| 5 | 3 Troll | 100% (7,7) | 39% | 16% | 17% | 12% |
| 10 | 3 Troll | 100% (7,0) | 75% | 59% | 79% | 83% |
| 15 | 3 Troll | 100% (5,6) | 97% | 89% | 100% | 72% |
| 5-15 | Strega | 1-7% | 0-1% | 0% | 0% | 23-33% |
| 5-15 | Drago, Lich | 0% | 0% | 0% | 0% | 0-7% |

Gli scontri comuni restano veri scontri (6-8 turni per il Guerriero a ogni livello). Contro tre mostri un personaggio solo che non sia un Guerriero perde spesso: nel gioco però i mostri sono tanti quanti i personaggi del gruppo, più uno o due. Gli scontri si fanno un po' più facili salendo di livello (i personaggi giocanti hanno 4 punti di attributi in più per livello, i mostri 3).

### Da fare

- **I boss** uccidono un personaggio solo in 1-2 turni a ogni livello (lo facevano già prima): vanno misurati con un gruppo completo, e il simulatore oggi fa combattere un personaggio solo.
- **Il simulatore con il gruppo**: per verificare il numero di mostri secondo il gruppo serve simulare più personaggi (nel gioco attacca uno alla volta il personaggio scelto, e chi avanza fra gli avversari "si disimpegna e attacca il gruppo").
