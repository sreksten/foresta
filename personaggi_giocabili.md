# Personaggi giocabili

Le classi che il giocatore sceglie a inizio partita o recluta in locanda (`TipoPersonaggio.isCompagno`): **Guerriero, Ladro, Mago, Elfo, Bardo, Sacerdote, Chierico e Druido**, ognuna con la sua forma femminile (Guerriera, Ladra, Maga, Elfa, Cantastorie, Sacerdotessa, Chierica, Druida). Questo documento raccoglie le loro caratteristiche innate e le mette a confronto, per capire quali sono più forti, quali più deboli e che cosa porta a sceglierne una invece di un'altra: lo scopo è che siano **tutte più o meno giocabili**. Sostituisce `bardo.md` e `chierico_e_druido.md`.

Il ruolo (`TipoPersonaggio.getRuolo`) è lo stesso nelle due forme, e i valori sono identici: la forma femminile cambia soltanto il moltiplicatore del carico (di 0,05 in meno; per la Cantastorie anche il critico, 1,15 invece di 1,1). Le tabelle sono quindi scritte al maschile. Il codice sta in `05-foresta-personaggi` (le classi e `CostantiPersonaggi`), `07-foresta-poteri` (abilità e canti, `CostantiAbilitaInnate`, `CostantiCanti`) e `08-foresta-tattiche` (le tattiche predefinite, `TatticheBase`).

## 1. Le classi

| Classe | Idea | Compagni da reclutare | Dotazione iniziale | Armi e protezioni |
| :--- | :--- | :--- | :--- | :--- |
| Guerriero / Guerriera | Prima linea fisica: salute, parata e danni delle armi | Reginald, Roderick / Elleran, Wendy | spada e scudo | spada, spadone, falce, mazza, ascia, lancia, scudo, armatura |
| Ladro / Ladra | Colpi critici, destrezza e furtività; due armi | Gabriel, Raven / Filean, Wyan | spada e veste | spada, mazza, ascia (anche in mano secondaria); niente armatura né scudo |
| Mago / Maga | Il cannone di vetro: tutta la magia e il potere magico, poca salute | Merlin / LeFey | bastone magico e veste | bastone magico, libro magico; niente armatura |
| Elfo / Elfa | A metà fra guerriero e mago: precisione, critico e resistenza magica | Lamiel / Yluviel | spada e veste | spada, mazza, ascia, lancia (anche in mano secondaria), armatura, libro magico (con metà del bonus) |
| Bardo / Cantastorie | Supporto: canti che curano, ipnotizzano e ispirano; contratta bene | Samuel / Gwendolyn | spada e veste | spada, scudo, armatura; né bastone né libro |
| Sacerdote / Sacerdotessa | Incantatore sacro: alba sacra e luce divina | nomi a caso (`ProduttoreDiMissioniCasuale`) | bastone magico e veste | bastone magico, mazza, libro magico; niente armatura |
| Chierico / Chierica | Il sacerdote che combatte in prima linea: cura e scaccia i non morti | Frederick / Elowen | mazza e scudo | spada, mazza, scudo, armatura; né bastone né libro |
| Druido / Druida | L'incantatore della natura: danni di terra e veleno, protezione e rigenerazione | Alder / Hazel | bastone magico e veste | bastone magico, lancia, libro magico; niente metallo (né scudo né armatura) |

Resistenze di classe (`TipoPersonaggio`): il Chierico subisce ×0,75 dal `NECROTICO` e dalla `MALEDIZIONE`; il Druido ×0,75 da `ACQUA`, `TERRA` e `VELENO` ma ×1,25 dal `FUOCO` e ×1,1 dal `TAGLIENTE`. Le altre classi non hanno resistenze o debolezze proprie (il quadro generale è in `debolezze_e_resistenze.md`). I pezzi della dotazione iniziale sono comuni, senza incantamenti, al livello del mondo meno 0–2 (`EquipaggiamentoIniziale`). `Viandante` e i personaggi non combattenti (locandiere, armaiolo…) sono classi a sé, non giocabili (`05-foresta-personaggi.md`).

## 2. Caratteristiche di partenza

I valori di classe stanno in `CostantiPersonaggi`. In grassetto i moltiplicatori da 1,3 in su, in corsivo quelli da 0,7 in giù.

**Salute e magia di base.** Contano come punto di partenza: salute e magia massime dipendono poi dal livello e dalle caratteristiche (§3).

| Classe | Salute base | Magia base | Magia per livello |
| :--- | ---: | ---: | ---: |
| Guerriero | 150 | 20 | 6 |
| Ladro | 100 | 25 | 8 |
| Mago | 80 | 60 | 34 |
| Elfo | 105 | 45 | 22 |
| Bardo | 105 | 40 | 18 |
| Sacerdote | 100 | 45 | 22 |
| Chierico | 130 | 30 | 14 |
| Druido | 110 | 40 | 20 |

**Massimi dei sei attributi primari e della fortuna.** I punti si distribuiscono a caso fino a questi tetti (`LanciatoreDeiDadi`).

| Classe | Forza | Destrezza | Costituzione | Intelligenza | Saggezza | Carisma | Fortuna |
| :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| Guerriero | 50 | 25 | 45 | 15 | 20 | 5 | 10 |
| Ladro | 30 | 50 | 30 | 30 | 25 | 6 | 25 |
| Mago | 15 | 20 | 20 | 55 | 40 | 6 | 10 |
| Elfo | 25 | 40 | 30 | 45 | 35 | 7 | 12 |
| Bardo | 30 | 35 | 35 | 35 | 35 | 8 | 15 |
| Sacerdote | 25 | 25 | 30 | 40 | 50 | 10 | 12 |
| Chierico | 35 | 25 | 40 | 25 | 40 | 8 | 10 |
| Druido | 25 | 30 | 35 | 40 | 45 | 8 | 15 |

**Moltiplicatori di classe (1/2).**

| Classe | Carico | Critico | Precisione | Velocità | Furtività | Parata | Resistenza magica | Percezione | Soggezione |
| :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| Guerriero | 1,25 | 1 | 1,1 | 1 | 1 | **1,5** | 1 | 1 | 1 |
| Ladro | 1 | **1,5** | **1,3** | **1,4** | **1,5** | 1 | **1,3** | **1,3** | 0,9 |
| Mago | 0,8 | 0,8 | 1 | 0,9 | 0,9 | _0,6_ | 1,1 | 1,1 | 1 |
| Elfo | 0,9 | 1,2 | **1,3** | 1,2 | **1,3** | 1,1 | **1,4** | **1,4** | 1,1 |
| Bardo | 0,9 | 1,1 | 1 | 1,1 | 1,1 | 1 | 1,1 | 1,1 | 1,2 |
| Sacerdote | 0,85 | 1 | 1,1 | 1 | 0,9 | 0,8 | **1,3** | 1,2 | 1,1 |
| Chierico | 1,1 | 1 | 1 | 0,9 | 0,8 | **1,3** | 1,2 | 1 | 1,1 |
| Druido | 0,85 | 1 | 1,1 | 1,1 | 1,1 | 0,9 | 1,2 | **1,3** | 1,1 |

**Moltiplicatori di classe (2/2).**

| Classe | Furia | Coraggio | Valore | Contrattazione | Numero bersagli | Stanchezza | Danni magici | Danni fisici | Recupero fisico | Recupero magico |
| :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| Guerriero | **1,5** | **1,3** | **1,5** | 0,9 | 1,1 | 1 | _0,5_ | **1,3** | **1,4** | _0,5_ |
| Ladro | 1 | 1 | 1 | **1,3** | 0,9 | 0,9 | 1 | 1 | 1,1 | 0,9 |
| Mago | 1 | 1 | 1 | 1 | **1,3** | **1,4** | **2** | _0,5_ | _0,7_ | **1,5** |
| Elfo | 1,2 | 1,1 | 1,2 | 1 | 1 | 0,8 | **1,4** | 0,9 | 1,1 | **1,3** |
| Bardo | 1,1 | 1,1 | 1,1 | 1,2 | 1,1 | 1 | 1,1 | 0,9 | 1,1 | 1,2 |
| Sacerdote | 1 | 1,2 | 1,1 | 1 | 1 | 1 | **1,4** | 0,8 | 0,9 | **1,3** |
| Chierico | 1 | 1,2 | 1,2 | 1 | 1 | 1 | 1 | 1 | 1,2 | 1 |
| Druido | 1 | 1,1 | 1 | 1 | 1 | 0,9 | 1,2 | 0,9 | 1,1 | 1,2 |

Chi spicca: il Mago per i **danni magici ×2** (e i bersagli ×1,3, il recupero magico ×1,5, la stanchezza ×1,4) ma con la parata a ×0,6 e i danni fisici a ×0,5; il Guerriero per la **parata ×1,5**, la furia ×1,5, il valore ×1,5, i danni fisici ×1,3 e il recupero fisico ×1,4; il Ladro per **critico ×1,5, furtività ×1,5, velocità ×1,4**; l'Elfo per **resistenza magica e percezione ×1,4**, precisione e furtività ×1,3 e danni magici ×1,4; il Bardo per la soggezione ×1,2 e la contrattazione ×1,2 (il resto non supera 1,2); il Sacerdote per i danni magici ×1,4 e il recupero magico ×1,3; il Chierico per la parata ×1,3; il Druido per la percezione ×1,3.

## 3. Quanto valgono ai vari livelli

Misure con `AnalisiDeiPersonaggiGiocabiliTest#schede` (media di 100 personaggi per classe, equipaggiati come nel gioco: Guerriero spada, scudo, elmo e armatura; Ladro due spade e veste; Elfo due spade e veste; Bardo spada, scudo e veste; Mago, Sacerdote e Druido bastone, libro e veste; Chierico mazza, scudo, elmo e armatura; i pezzi sono al livello del personaggio con i valori medi del generatore, senza parte casuale).

**Salute massima / magia massima per livello.**

| Classe | Livello 1 | Livello 3 | Livello 5 | Livello 7 |
| :--- | ---: | ---: | ---: | ---: |
| Guerriero | 150 / 20 | 259 / 28 | 335 / 32 | 396 / 34 |
| Ladro | 100 / 25 | 173 / 36 | 223 / 41 | 264 / 44 |
| Mago | 80 / 60 | 138 / 108 | 178 / 128 | 211 / 143 |
| Elfo | 105 / 45 | 181 / 76 | 234 / 89 | 277 / 98 |
| Bardo | 105 / 40 | 181 / 65 | 234 / 76 | 277 / 84 |
| Sacerdote | 100 / 45 | 173 / 76 | 223 / 89 | 264 / 98 |
| Chierico | 130 / 30 | 225 / 49 | 290 / 58 | 343 / 64 |
| Druido | 110 / 40 | 190 / 68 | 245 / 80 | 291 / 88 |

**Altre risorse al livello 7.** "Rig." è il recupero per turno di salute o di magia; "bersagli" è quanti avversari colpiscono insieme gli attacchi a più bersagli; "potere magico" è il moltiplicatore percentuale del danno degli incantesimi e delle abilità (100 = nessuno).

| Classe | Rig. salute | Rig. magia | Carico massimo | Bersagli | Potere magico |
| :--- | ---: | ---: | ---: | ---: | ---: |
| Guerriero | 129 | 4 | 104 | 5 | 100 |
| Ladro | 65 | 7 | 67 | 3 | 100 |
| Mago | 34 | 19 | 40 | 6 | 170 |
| Elfo | 68 | 13 | 60 | 4 | 100 |
| Bardo | 68 | 12 | 60 | 4 | 100 |
| Sacerdote | 55 | 13 | 45 | 4 | 170 |
| Chierico | 96 | 9 | 78 | 3,2 | 100 |
| Druido | 72 | 12 | 44 | 4 | 170 |

**Attributi al livello 7** (con l'equipaggiamento: la parata comprende scudo e armatura, il potere magico bastone e libro).

| Classe | FOR | DES | COS | INT | SAG | CAR | FORT | CRIT |
| :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| Guerriero | 23,5 | 13,4 | 18,4 | 9,4 | 10,4 | 11,4 | 7,4 | 9,0 |
| Ladro | 16,3 | 18,4 | 10,3 | 10,3 | 10,3 | 10,3 | 18,2 | 17,3 |
| Mago | 7,3 | 10,3 | 10,2 | 27,3 | 18,3 | 13,3 | 7,3 | 6,0 |
| Elfo | 16,1 | 18,2 | 10,2 | 13,2 | 13,1 | 13,2 | 10,1 | 13,0 |
| Bardo | 16,1 | 13,1 | 10,2 | 13,1 | 13,1 | 18,1 | 10,2 | 10,1 |
| Sacerdote | 7,2 | 10,4 | 13,2 | 15,2 | 27,4 | 13,3 | 7,3 | 8,1 |
| Chierico | 15,1 | 9,2 | 17,2 | 10,1 | 23,1 | 12,2 | 7,1 | 8,0 |
| Druido | 7,5 | 13,4 | 11,4 | 15,4 | 25,7 | 10,4 | 10,4 | 9,3 |

| Classe | PREC | VEL | FURT | PAR | RES.M | PERC | SOGG | FURIA | CORAG | VAL | CONTR |
| :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| Guerriero | 3,1 | 3,0 | 3,0 | 51,0 | 10,0 | 3,0 | 3,0 | 6,0 | 4,0 | 5,0 | 2,0 |
| Ladro | 5,0 | 5,3 | 6,0 | 10,0 | 15,0 | 4,0 | 3,0 | 4,0 | 3,0 | 3,0 | 4,0 |
| Mago | 3,0 | 2,0 | 2,0 | 1,0 | 15,5 | 4,0 | 3,0 | 2,0 | 3,0 | 3,1 | 3,0 |
| Elfo | 5,0 | 4,2 | 5,0 | 10,0 | 16,0 | 5,0 | 4,0 | 4,0 | 3,0 | 4,0 | 3,0 |
| Bardo | 3,0 | 3,1 | 3,0 | 34,0 | 21,2 | 3,3 | 5,0 | 4,0 | 4,0 | 3,0 | 4,0 |
| Sacerdote | 3,0 | 3,0 | 2,0 | 2,0 | 18,0 | 5,0 | 3,0 | 2,0 | 4,0 | 5,0 | 3,0 |
| Chierico | 3,0 | 2,0 | 2,0 | 10,0 | 6,0 | 4,0 | 3,2 | 3,0 | 4,0 | 5,0 | 3,0 |
| Druido | 4,0 | 3,3 | 3,0 | 2,0 | 16,0 | 6,0 | 3,0 | 2,1 | 3,0 | 4,0 | 3,0 |

Cose che saltano all'occhio: la **parata** del Guerriero (51) e del Bardo (34 con scudo e armatura) contro il 10 di Chierico, Ladro ed Elfo e l'1–2 degli incantatori; la salute del Guerriero (396 al livello 7) quasi doppia di quella del Mago (211); il **potere magico 170** di chi porta bastone e libro (Mago, Sacerdote, Druido), mentre Elfo e Bardo restano a 100 perché non portano il bastone e l'Elfo ha metà del bonus del libro.

## 4. Abilità innate

Le abilità costano `MAGIA`, non consumano pergamene, occupano il turno e compaiono fra le scelte del comando incantesimi; non sono in `TipoIncantesimo`, quindi non finiscono in cofani, negozi o repertorio dei mostri. Ognuna ha un `TipoAzione` con lo stesso nome, quindi funziona anche nel combattimento automatico (`EsecutoreTattiche`, `TatticheBase`). Un personaggio `SILENZIATO` non le usa. Le capacità dipendono dal `TipoPersonaggio`, non dalla classe Java (`AbilitaInnate.conosciutaDa`, `CantoDelBardo.conosciutoDa`).

**Riepilogo per classe.** I lanci sono quelli che reggono a magia piena al livello 7 (magia massima divisa per il costo); "bersagli" è su chi agisce.

| Classe | Abilità innate | Magia al liv. 7 | Lanci a magia piena | Bersagli | Quando le usa la tattica predefinita |
| :--- | :--- | ---: | :--- | :--- | :--- |
| Guerriero | nessuna (lo stato `BERSERK` fa crescere il danno fisico con la salute persa, fino a +50%, e dipende dalla furia ×1,5) | 34 | – | – | – |
| Ladro | nessuna | 44 | – | – | – |
| Mago | Dardo arcano | 143 | 71 | uno | sui feriti sotto il 40% (colpo più economico che basta) e sul più forte quando non ha una pergamena adatta; il Silenzio sui maghi avversari ha la precedenza |
| Elfo | Dardo arcano (cresce con il livello) | 98 | 24 | uno | sui maghi avversari e sul più ferito, ma solo se rende più dell'attacco con le armi |
| Bardo | Canto curativo, canto ipnotico, canto di valore, Urlo della Banshee (dopo `IlViaggioDellaMoglie`) | 84 | 21 / 28 / 16 / 14 | alleato più ferito; fino a 4 avversari; tutto il gruppo; fino a 4 avversari | il curativo sotto il 60% di salute, il valore se c'è chi non è ispirato, ipnotico e urlo contro più avversari |
| Sacerdote | Alba sacra, Luce divina | 98 | 19 / 9 | tutto il gruppo; fino a 4 avversari (e i non morti alleati) | l'alba con almeno due alleati con uno stato negativo, dopo il terzo round; la luce solo sui non morti |
| Chierico | Scacciare i non morti, Imposizione delle mani | 64 | 16 / 12 | fino a 3 non morti; alleato più ferito | l'imposizione sul più ferito sotto il 50% di salute (anche gli ospiti); scacciare i non morti sul primo avversario; poi la mazza sul più ferito |
| Druido | Radici, Sciame, Pelle di corteccia, Rigenerazione | 88 | 29 / 29 / 17 / 22 | fino a 4 avversari; fino a 4 avversari; tutto il gruppo; un alleato | la rigenerazione sul più ferito sotto il 70%, la pelle di corteccia subito dopo, radici e sciame sul primo avversario; poi un incantesimo o il bastone sul più ferito |

| Classe | Abilità | Costo (magia) | Effetto |
| :--- | :--- | ---: | :--- |
| Mago, Elfo | Dardo arcano | 2 (Mago), 4 (Elfo) | Danno `ARCANO` a **un solo** bersaglio: base 20 (Mago) o 15 più 4 per ogni livello oltre il primo (Elfo: 31 al livello 5, 39 al livello 7), poi livello, intelligenza, potere magico e bonus del libro |
| Sacerdote | Alba sacra | 5 | Toglie al gruppo gli effetti di stato negativi e i modificatori temporanei (compreso `ISPIRATO`) |
| Sacerdote | Luce divina | 10 | Danno `SACRO` (base 35) a più avversari, il doppio ai non morti; investe anche i non morti del proprio gruppo (lo Scheletro della Mietitura), che le cure non guariscono. Il Gargoyle di pietra non la sente |
| Chierico | Scacciare i non morti | 4 | `SPAVENTATO` a fino a `NUMERO_BERSAGLI` non morti, con la soggezione contro il coraggio; non serve contro chi è già spaventato o immune (Scheletro e Lich lo sono) |
| Chierico | Imposizione delle mani | 5 | Cura il più ferito di 20 × √livello, aumentata dalla saggezza (÷50) |
| Druido | Radici | 3 | Danno di `TERRA` (base 40) a più avversari; `IMMOBILIZZATO` o `ACCECATO` |
| Druido | Sciame | 3 | Danno di `VELENO` (base 40) a più avversari; `AVVELENATO` |
| Druido | Pelle di corteccia | 5 | A tutto il gruppo, per 4 turni, 10 × √livello punti fissi di parata e di resistenza magica (modificatori temporanei, che l'alba sacra non toglie) |
| Druido | Rigenerazione | 4 | `RIGENERAZIONE` a un alleato per 4 turni: a ogni turno guarisce (5 + 5% della salute massima) × (1 + saggezza/50) |
| Bardo | Canto curativo | 4 | Cura il più ferito di 30 × √livello, aumentata dalla saggezza (÷50) |
| Bardo | Canto ipnotico | 3 | `SPAVENTATO` o `CONFUSO` (a caso) a fino a `NUMERO_BERSAGLI` avversari, con la soggezione del bardo contro il coraggio |
| Bardo | Canto di valore | 5 | `ISPIRATO` a tutto il gruppo per 4 turni: coraggio e precisione +20% + 2,5% per livello del bardo, tetto 50% (non si somma: il secondo rinfresca) |
| Bardo | Urlo della Banshee | 6 | Il canto ipnotico potenziato (soggezione ×1,5) che dà sia `SPAVENTATO` sia `CONFUSO` con un solo tiro. **Non noto all'inizio**: lo insegna il bardo della locanda dopo `IlViaggioDellaMoglie` (intermezzo `IntermezzoUrloDellaBanshee`, `CantiImparati`), e vale per tutto il gruppo |

Il canto ipnotico e quello di valore si usano solo in combattimento con almeno un avversario vivo, il curativo solo se qualcuno è ferito; il bardo della locanda è un bardo a tutti gli effetti. Il Druido non canta. I danni delle abilità a più bersagli si ripartiscono per posizione (`CalcolatoreCombattimento.quotaDelDannoPerPosizione`): il primo bersaglio subisce tutto, ogni successivo la metà del precedente (100%, 50%, 25%, 12,5%, 6,25%), con il minimo del 6,25% (`CostantiIncantesimi.INCANTESIMO_MULTIPLO_*`).

### 4.1 Quanto fanno le abilità che colpiscono

Misure con `AnalisiDanniAbilitaInnateTest` (media di 200 prove per cella, critico neutralizzato, bersagli allo stesso livello di chi attacca; il danno è quello di un colpo andato a segno sul primo bersaglio, e fra parentesi la percentuale della salute massima media del bersaglio). **Con bastone e libro veri** per Mago, Sacerdote e Druido (l'Elfo e il Bardo senza: non li portano).

**Livello 7.**

| Abilità | Viverna | Chimera-drago | Drago | Idra | Lich | Minotauro gigante |
| :--- | ---: | ---: | ---: | ---: | ---: | ---: |
| Luce divina (Sacerdote) | 374 (109%) | 373 (59%) | 359 (23%) | 370 (78%) | 604 (76%) | 380 (63%) |
| Radici (Druido) | 219 (64%) | 331 (52%) | 211 (13%) | 0 (0%) | 323 (41%) | 338 (56%) |
| Sciame (Druido) | 0 (0%) | 219 (35%) | 320 (20%) | 0 (0%) | 0 (0%) | 338 (56%) |
| Dardo arcano (Mago) | 404 (118%) | 403 (64%) | 388 (24%) | 400 (84%) | 392 (49%) | 411 (68%) |
| Dardo arcano (Elfo) | 148 (43%) | 148 (23%) | 142 (9%) | 146 (31%) | 143 (18%) | 151 (25%) |

**Livello 3.**

| Abilità | Viverna | Chimera-drago | Drago | Idra | Lich | Minotauro gigante |
| :--- | ---: | ---: | ---: | ---: | ---: | ---: |
| Luce divina (Sacerdote) | 145 (65%) | 145 (35%) | 141 (14%) | 144 (46%) | 258 (50%) | 148 (37%) |
| Radici (Druido) | 76 (34%) | 131 (32%) | 74 (7%) | 0 (0%) | 128 (25%) | 134 (34%) |
| Sciame (Druido) | 0 (0%) | 76 (18%) | 128 (12%) | 0 (0%) | 0 (0%) | 134 (34%) |
| Dardo arcano (Mago) | 142 (63%) | 142 (34%) | 138 (13%) | 141 (45%) | 138 (27%) | 145 (36%) |
| Dardo arcano (Elfo) | 58 (26%) | 58 (14%) | 56 (5%) | 58 (18%) | 56 (11%) | 59 (15%) |

**Danno sulla viverna per livello** (con bastone e libro veri).

| Abilità | Livello 1 | Livello 3 | Livello 5 | Livello 7 |
| :--- | ---: | ---: | ---: | ---: |
| Luce divina (Sacerdote) | 66 (51%) | 145 (65%) | 271 (94%) | 374 (109%) |
| Radici (Druido) | 33 (25%) | 76 (34%) | 160 (55%) | 219 (64%) |
| Sciame (Druido) | 0 (0%) | 0 (0%) | 0 (0%) | 0 (0%) |
| Dardo arcano (Mago) | 62 (48%) | 142 (63%) | 290 (100%) | 404 (118%) |
| Dardo arcano (Elfo) | 22 (17%) | 58 (26%) | 100 (35%) | 148 (43%) |

**Gli stessi, senza nessun equipaggiamento, al livello 7** (per misurare quanto pesano bastone e libro).

| Abilità | Viverna | Chimera-drago | Drago | Idra | Lich | Minotauro gigante |
| :--- | ---: | ---: | ---: | ---: | ---: | ---: |
| Luce divina (Sacerdote) | 145 (42%) | 145 (23%) | 140 (9%) | 144 (30%) | 283 (36%) | 148 (24%) |
| Radici (Druido) | 65 (19%) | 131 (21%) | 63 (4%) | 0 (0%) | 128 (16%) | 134 (22%) |
| Sciame (Druido) | 0 (0%) | 65 (10%) | 126 (8%) | 0 (0%) | 0 (0%) | 134 (22%) |
| Dardo arcano (Mago) | 130 (38%) | 130 (20%) | 125 (8%) | 128 (27%) | 126 (16%) | 132 (22%) |
| Dardo arcano (Elfo) | 148 (43%) | 148 (23%) | 142 (9%) | 146 (31%) | 143 (18%) | 151 (25%) |

Che cosa dicono:

- **Bastone e libro valgono più della base dell'abilità.** Con la base della luce divina a 25, 30 e 35 il danno sulla viverna al livello 7 è 312, 341 e 374 (91%, 100% e 109% della sua salute): ogni punto di base ne aggiunge circa 6, il resto viene dall'intelligenza e dalla saggezza del personaggio, dal moltiplicatore di classe, dal potere magico (×1,7 con bastone e libro) e dal bonus fisso del libro. Il dardo arcano del Mago ha base 20 e, equipaggiato, a livello 7 fa 404: più del triplo di quanto fa senza bastone e libro (130).
- **Il dardo del Mago è l'abilità che colpisce più forte**, su un solo bersaglio: 118% di una viverna, 84% dell'Idra, 64% della chimera-drago. La luce divina del Sacerdote (base 35) è al 109% sulla viverna e al 78% sull'Idra, ma su più bersagli; radici e sciame del Druido arrivano al 64% della viverna e al 56% del Minotauro gigante.
- **Il dardo dell'Elfo cresce con il livello** (base 15 + 4 per livello oltre il primo): 22, 58, 100 e 148 ai livelli 1, 3, 5 e 7, cioè 17%, 26%, 35% e 43% di una viverna, contro il 404 del Mago. Non ha bastone né libro (nell'analisi non lo porta; il libro lo può usare con metà del bonus), quindi senza equipaggiamento il suo dardo supera quello del Mago dal livello 4, ma un Mago ha sempre bastone e libro.
- **Immunità:** lo sciame fa 0 a viverna, Idra e Lich (veleno e non morti), le radici fanno 0 all'Idra (immune alla terra); la luce divina fa il doppio al Lich (non morto), ma con il Lich a 793 punti non lo uccide più in un colpo (76%). La Strega è immune a Acqua, Aria, Fuoco e Terra: radici e molti incantesimi non la toccano.
- **Il Drago** (salute base triplicata a 600, circa 1587 al livello 7) richiede da 4 a 8 colpi, a seconda dell'abilità.

## 5. Scontri simulati

Misure con `AnalisiDeiPersonaggiGiocabiliTest#scontri` (100 scontri per cella, `-Diterazioni=<n>` per cambiarli): i miei agiscono con la **tattica predefinita** della loro classe (`RegistroTattiche.predefinitaDelGiocabile`) nel round a coppie di `RoundASimulato`, contro i mostri con le loro tattiche base, **senza pozioni né pergamene**, a salute e magia piene, equipaggiati come in §3, tutti allo stesso livello. Ogni cella è **vittorie % / salute residua dei miei % / round medi delle vittorie**; "0" vuol dire nessuna vittoria. Goblin e Scheletro da soli sono tre alla volta (secondo `LocazioneBase.numeroMassimoDiMostri`, al massimo 3), gli altri uno; nel gruppo di quattro anche Troll, Viverna e Scheletro sono tre.

### 5.1 Ogni classe da sola

Poco realistico (un gruppo ha più personaggi), ma isola quanto vale la classe da sé: danno, resistenza e autonomia. Chi cura o ispira, da solo, non ha chi aiutare.

**Livello 3.**

| Classe | Goblin | Scheletro | Troll | Viverna | Strega | Idra | Lich |
| :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| Guerriero | 100 / 83 / 2,9 | 100 / 80 / 4,7 | 100 / 64 / 7,2 | 66 / 38 / 5,5 | 0 | 2 / 41 / 13,5 | 0 |
| Ladro | 100 / 81 / 2,4 | 100 / 75 / 3,9 | 96 / 46 / 6,1 | 61 / 40 / 4,5 | 0 | 1 / 14 / 7,0 | 0 |
| Mago | 100 / 88 / 1,0 | 100 / 88 / 1,0 | 100 / 71 / 2,1 | 100 / 77 / 2,0 | 42 / 63 / 4,1 | 65 / 41 / 2,7 | 2 / 94 / 8,0 |
| Elfo | 100 / 82 / 2,8 | 100 / 84 / 2,0 | 94 / 44 / 6,0 | 68 / 52 / 4,7 | 0 | 18 / 30 / 5,2 | 0 |
| Bardo | 100 / 78 / 6,5 | 100 / 75 / 10,5 | 100 / 71 / 18,3 | 93 / 72 / 16,4 | 0 | 1 / 66 / 29,0 | 0 |
| Sacerdote | 100 / 74 / 9,2 | 100 / 87 / 1,1 | 45 / 38 / 24,5 | 86 / 55 / 21,3 | 0 | 0 | 21 / 85 / 5,3 |
| Chierico | 100 / 66 / 7,2 | 100 / 61 / 13,2 | 50 / 38 / 23,5 | 69 / 50 / 19,6 | 0 | 0 | 0 |
| Druido | 100 / 84 / 2,2 | 100 / 84 / 2,2 | 74 / 51 / 36,4 | 100 / 86 / 4,8 | 0 | 0 | 4 / 90 / 13,5 |

**Livello 5.**

| Classe | Goblin | Scheletro | Troll | Viverna | Strega | Idra | Lich |
| :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| Guerriero | 100 / 79 / 5,2 | 100 / 73 / 8,4 | 100 / 79 / 5,9 | 84 / 45 / 4,7 | 0 | 8 / 25 / 11,1 | 0 |
| Ladro | 100 / 62 / 4,5 | 98 / 44 / 6,7 | 98 / 51 / 5,5 | 81 / 46 / 4,1 | 0 | 2 / 4 / 8,5 | 0 |
| Mago | 100 / 68 / 2,1 | 100 / 65 / 2,1 | 100 / 72 / 1,9 | 100 / 85 / 1,1 | 58 / 76 / 2,4 | 89 / 48 / 2,2 | 16 / 87 / 5,8 |
| Elfo | 100 / 64 / 4,3 | 100 / 63 / 4,3 | 100 / 71 / 3,4 | 100 / 76 / 3,0 | 1 / 74 / 8,0 | 50 / 31 / 4,6 | 0 |
| Bardo | 100 / 75 / 16,3 | 71 / 30 / 28,9 | 100 / 76 / 14,1 | 98 / 76 / 13,1 | 0 | 6 / 38 / 31,0 | 0 |
| Sacerdote | 51 / 21 / 15,5 | 99 / 83 / 1,4 | 63 / 48 / 22,8 | 86 / 60 / 20,0 | 1 / 79 / 36,0 | 0 | 26 / 86 / 3,8 |
| Chierico | 100 / 59 / 15,2 | 77 / 49 / 26,8 | 85 / 51 / 20,2 | 80 / 56 / 15,7 | 0 | 7 / 34 / 27,1 | 0 |
| Druido | 100 / 81 / 2,7 | 100 / 77 / 3,3 | 88 / 61 / 38,1 | 100 / 86 / 3,1 | 0 | 0 | 25 / 89 / 8,5 |

**Livello 7.**

| Classe | Goblin | Scheletro | Troll | Viverna | Strega | Idra | Lich |
| :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| Guerriero | 100 / 74 / 8,2 | 100 / 59 / 11,8 | 100 / 81 / 5,4 | 91 / 49 / 4,3 | 0 | 11 / 40 / 10,9 | 0 |
| Ladro | 100 / 44 / 5,8 | 41 / 22 / 8,5 | 97 / 59 / 4,8 | 94 / 51 / 3,8 | 0 | 1 / 46 / 9,0 | 0 |
| Mago | 100 / 43 / 3,1 | 89 / 32 / 3,1 | 100 / 82 / 1,4 | 99 / 87 / 1,0 | 67 / 75 / 2,7 | 97 / 53 / 2,1 | 26 / 89 / 3,4 |
| Elfo | 97 / 60 / 4,2 | 96 / 38 / 5,3 | 100 / 73 / 3,0 | 100 / 78 / 2,3 | 1 / 75 / 8,0 | 68 / 46 / 3,8 | 0 |
| Bardo | 99 / 50 / 27,4 | 7 / 11 / 33,9 | 100 / 78 / 11,9 | 100 / 78 / 9,9 | 0 | 41 / 51 / 33,6 | 0 |
| Sacerdote | 0 | 99 / 68 / 2,2 | 66 / 45 / 21,1 | 98 / 79 / 19,4 | 0 | 0 | 14 / 77 / 4,7 |
| Chierico | 88 / 53 / 22,3 | 13 / 65 / 10,1 | 92 / 54 / 19,8 | 93 / 61 / 14,6 | 1 / 96 / 12,0 | 12 / 54 / 30,9 | 0 |
| Druido | 100 / 74 / 3,8 | 100 / 69 / 3,9 | 90 / 58 / 38,5 | 100 / 88 / 3,2 | 5 / 30 / 40,0 | 0 | 39 / 84 / 8,6 |

### 5.2 Come quarto del gruppo Guerriero, Ladro e Mago

Il gruppo di tre che fa da riferimento ("(nessuno)") ha già un fisico, uno scassinatore e un incantatore: aggiungere una classe mostra che cosa porta in più. Contro gli avversari facili tutte le classi fanno il 100% (non si distingue nulla), quindi sono stati scelti avversari che mettono alla prova: branchi di tre Troll o Viverne, la Strega, i boss.

**Livello 3.**

| Classe | Troll | Viverna | Scheletro | Strega | Minotauro gigante | Idra | Drago | Lich |
| :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| (nessuno) | 100 / 62 / 4,9 | 73 / 54 / 5,1 | 100 / 79 / 3,0 | 26 / 57 / 5,0 | 97 / 64 / 3,5 | 88 / 56 / 4,9 | 0 | 1 / 59 / 5,0 |
| Guerriero | 100 / 75 / 3,8 | 99 / 61 / 4,4 | 100 / 82 / 2,7 | 58 / 62 / 3,8 | 100 / 75 / 2,7 | 100 / 67 / 4,0 | 0 | 9 / 54 / 5,0 |
| Ladro | 100 / 73 / 3,5 | 98 / 64 / 3,9 | 100 / 82 / 2,7 | 83 / 56 / 3,9 | 100 / 70 / 2,7 | 100 / 66 / 3,6 | 1 / 51 / 7,0 | 22 / 43 / 5,5 |
| Mago | 100 / 73 / 3,1 | 99 / 67 / 2,9 | 100 / 82 / 2,0 | 100 / 87 / 3,7 | 100 / 75 / 2,0 | 100 / 72 / 1,9 | 17 / 60 / 6,7 | 100 / 87 / 5,7 |
| Elfo | 100 / 72 / 3,6 | 98 / 62 / 3,6 | 100 / 83 / 2,3 | 100 / 87 / 3,8 | 100 / 73 / 2,6 | 100 / 69 / 3,1 | 16 / 46 / 12,1 | 100 / 88 / 5,5 |
| Bardo | 100 / 79 / 4,3 | 99 / 72 / 4,3 | 100 / 82 / 3,0 | 100 / 87 / 4,6 | 100 / 79 / 2,9 | 100 / 73 / 3,8 | 12 / 48 / 13,3 | 100 / 87 / 6,7 |
| Sacerdote | 100 / 69 / 4,2 | 98 / 64 / 4,1 | 100 / 82 / 1,7 | 100 / 87 / 4,8 | 100 / 69 / 3,0 | 99 / 68 / 3,9 | 30 / 76 / 9,0 | 100 / 87 / 4,4 |
| Chierico | 100 / 74 / 4,2 | 100 / 69 / 4,0 | 100 / 81 / 3,0 | 100 / 87 / 5,4 | 100 / 75 / 3,0 | 100 / 70 / 3,7 | 5 / 40 / 13,6 | 100 / 87 / 7,1 |
| Druido | 100 / 81 / 4,5 | 99 / 78 / 3,6 | 100 / 84 / 2,5 | 100 / 86 / 5,1 | 100 / 76 / 2,8 | 100 / 71 / 4,2 | 63 / 65 / 11,0 | 100 / 85 / 5,5 |

**Livello 5.**

| Classe | Troll | Viverna | Scheletro | Strega | Minotauro gigante | Idra | Drago | Lich |
| :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| (nessuno) | 100 / 74 / 3,3 | 90 / 63 / 4,3 | 100 / 78 / 2,9 | 31 / 59 / 4,1 | 100 / 69 / 3,4 | 90 / 60 / 4,5 | 0 | 5 / 58 / 4,4 |
| Guerriero | 100 / 80 / 3,1 | 99 / 71 / 3,5 | 100 / 83 / 2,6 | 82 / 67 / 3,1 | 100 / 77 / 2,7 | 100 / 72 / 3,1 | 2 / 67 / 8,5 | 23 / 52 / 4,4 |
| Ladro | 100 / 78 / 3,1 | 100 / 70 / 3,3 | 100 / 81 / 2,5 | 88 / 62 / 3,3 | 100 / 75 / 2,8 | 100 / 73 / 2,8 | 3 / 65 / 9,3 | 44 / 46 / 4,6 |
| Mago | 100 / 77 / 2,3 | 100 / 73 / 2,4 | 100 / 82 / 2,0 | 100 / 87 / 3,0 | 100 / 80 / 1,4 | 100 / 77 / 1,5 | 34 / 69 / 5,5 | 100 / 87 / 4,1 |
| Elfo | 100 / 75 / 3,0 | 100 / 73 / 3,0 | 100 / 83 / 2,1 | 100 / 88 / 3,2 | 100 / 79 / 2,0 | 100 / 75 / 2,1 | 42 / 55 / 9,6 | 100 / 88 / 4,5 |
| Bardo | 100 / 82 / 3,1 | 100 / 79 / 4,0 | 100 / 82 / 3,0 | 100 / 87 / 3,9 | 100 / 82 / 2,9 | 100 / 78 / 2,9 | 31 / 54 / 12,3 | 100 / 88 / 5,5 |
| Sacerdote | 100 / 75 / 3,2 | 100 / 74 / 3,6 | 100 / 83 / 1,5 | 100 / 87 / 4,0 | 100 / 73 / 3,0 | 100 / 73 / 2,8 | 43 / 73 / 9,4 | 100 / 88 / 3,2 |
| Chierico | 100 / 78 / 3,1 | 100 / 71 / 4,3 | 100 / 81 / 2,9 | 100 / 87 / 4,2 | 100 / 79 / 2,8 | 99 / 74 / 2,8 | 26 / 57 / 10,8 | 100 / 87 / 6,0 |
| Druido | 100 / 82 / 3,2 | 100 / 79 / 3,3 | 100 / 83 / 2,6 | 100 / 87 / 4,2 | 100 / 79 / 2,9 | 100 / 78 / 3,1 | 86 / 73 / 8,0 | 100 / 87 / 4,3 |

**Livello 7.**

| Classe | Troll | Viverna | Scheletro | Strega | Minotauro gigante | Idra | Drago | Lich |
| :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| (nessuno) | 100 / 74 / 3,2 | 96 / 66 / 4,0 | 100 / 77 / 2,9 | 48 / 62 / 3,7 | 100 / 73 / 2,9 | 100 / 70 / 3,3 | 6 / 76 / 4,7 | 7 / 60 / 4,3 |
| Guerriero | 100 / 79 / 3,1 | 100 / 75 / 3,2 | 100 / 83 / 2,5 | 91 / 67 / 3,0 | 100 / 81 / 2,2 | 100 / 80 / 2,3 | 7 / 79 / 5,0 | 36 / 52 / 4,1 |
| Ladro | 100 / 79 / 3,0 | 100 / 74 / 3,0 | 100 / 81 / 2,4 | 98 / 71 / 2,8 | 100 / 81 / 2,0 | 100 / 80 / 2,1 | 6 / 62 / 6,5 | 56 / 46 / 4,3 |
| Mago | 100 / 78 / 2,0 | 100 / 77 / 2,2 | 100 / 81 / 1,9 | 100 / 87 / 2,1 | 100 / 81 / 1,5 | 100 / 84 / 1,3 | 47 / 72 / 5,4 | 100 / 88 / 3,7 |
| Elfo | 100 / 79 / 2,8 | 100 / 77 / 2,7 | 100 / 81 / 2,1 | 100 / 88 / 3,0 | 100 / 81 / 1,6 | 100 / 82 / 1,8 | 48 / 68 / 8,3 | 100 / 88 / 4,2 |
| Bardo | 100 / 83 / 3,1 | 100 / 82 / 3,5 | 100 / 83 / 2,9 | 100 / 87 / 3,5 | 100 / 83 / 2,4 | 100 / 83 / 2,3 | 55 / 61 / 10,2 | 100 / 87 / 5,3 |
| Sacerdote | 100 / 76 / 3,1 | 100 / 77 / 3,3 | 100 / 83 / 1,6 | 100 / 88 / 3,4 | 100 / 79 / 2,5 | 100 / 82 / 2,4 | 61 / 71 / 9,5 | 100 / 87 / 2,7 |
| Chierico | 100 / 78 / 3,1 | 100 / 79 / 3,3 | 100 / 80 / 2,7 | 100 / 87 / 3,8 | 100 / 82 / 2,4 | 100 / 81 / 2,6 | 49 / 65 / 10,1 | 100 / 88 / 5,4 |
| Druido | 100 / 83 / 3,1 | 100 / 80 / 3,0 | 100 / 83 / 2,4 | 100 / 87 / 4,0 | 100 / 81 / 2,4 | 100 / 81 / 2,8 | 99 / 82 / 6,0 | 100 / 87 / 3,6 |

### 5.3 Con armi incantate (e l'Elfo con il libro)

Stesse simulazioni con `-Dequip=incantato`: tutte le armi hanno un incantamento di `FULMINE` del grado adatto al livello (nessuno dei mostri vi è debole o immune), per Guerriero, Ladro, Elfo, Bardo e Chierico; Mago, Sacerdote e Druido restano con bastone e libro. La riga "Elfo+libro" ha la spada incantata e il libro magico al posto della seconda spada (il libro occupa la mano secondaria).

**Livello 7, ognuno da solo.**

| Classe | Goblin | Scheletro | Troll | Viverna | Strega | Idra | Lich |
| :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| Guerriero | 100 / 83 / 4,0 | 100 / 69 / 7,7 | 100 / 86 / 3,5 | 100 / 80 / 2,7 | 6 / 82 / 4,7 | 17 / 30 / 6,1 | 1 / 85 / 8,0 |
| Ladro | 100 / 75 / 3,4 | 100 / 67 / 3,6 | 100 / 88 / 2,1 | 100 / 84 / 2,1 | 74 / 87 / 3,2 | 40 / 50 / 4,2 | 59 / 85 / 6,3 |
| Mago | 100 / 42 / 3,3 | 91 / 30 / 3,1 | 100 / 82 / 1,4 | 99 / 88 / 1,0 | 66 / 72 / 2,9 | 96 / 51 / 2,1 | 28 / 90 / 3,8 |
| Elfo | 100 / 72 / 3,4 | 100 / 56 / 3,7 | 100 / 87 / 2,1 | 100 / 86 / 2,1 | 98 / 86 / 4,0 | 53 / 56 / 4,8 | 94 / 88 / 7,3 |
| Elfo+libro | 99 / 58 / 5,4 | 100 / 63 / 3,2 | 100 / 85 / 3,5 | 100 / 81 / 3,2 | 69 / 88 / 5,1 | 88 / 65 / 2,8 | 43 / 86 / 11,6 |
| Bardo | 100 / 73 / 16,3 | 79 / 30 / 26,4 | 100 / 80 / 5,4 | 100 / 82 / 5,9 | 35 / 86 / 7,5 | 95 / 71 / 19,4 | 9 / 85 / 16,0 |
| Sacerdote | 0 | 100 / 70 / 1,9 | 62 / 48 / 21,1 | 95 / 76 / 19,9 | 0 | 0 | 23 / 88 / 6,5 |
| Chierico | 86 / 53 / 23,1 | 11 / 66 / 8,6 | 95 / 54 / 19,5 | 94 / 64 / 12,4 | 5 / 88 / 7,8 | 9 / 67 / 11,6 | 2 / 93 / 18,0 |
| Druido | 100 / 76 / 3,6 | 100 / 71 / 3,8 | 87 / 53 / 38,6 | 100 / 88 / 3,3 | 4 / 45 / 38,0 | 0 | 31 / 84 / 7,9 |

**Livello 7, come quarto del gruppo Guerriero+Ladro+Mago** (anche Guerriero e Ladro del gruppo hanno le armi incantate).

| Classe | Troll | Viverna | Scheletro | Strega | Minotauro gigante | Idra | Drago | Lich |
| :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| (nessuno) | 100 / 84 / 2,1 | 100 / 82 / 2,1 | 100 / 84 / 2,0 | 100 / 85 / 1,4 | 100 / 84 / 1,2 | 100 / 78 / 1,9 | 93 / 76 / 3,5 | 99 / 85 / 2,6 |
| Guerriero | 100 / 84 / 2,0 | 100 / 82 / 2,0 | 100 / 85 / 2,0 | 100 / 86 / 1,1 | 100 / 85 / 1,1 | 100 / 83 / 1,6 | 98 / 81 / 2,6 | 100 / 85 / 1,5 |
| Ladro | 100 / 85 / 2,0 | 100 / 84 / 1,9 | 100 / 86 / 1,3 | 100 / 86 / 1,2 | 100 / 86 / 1,1 | 100 / 82 / 1,5 | 100 / 78 / 2,7 | 100 / 85 / 1,5 |
| Mago | 100 / 82 / 1,6 | 100 / 82 / 1,3 | 100 / 85 / 1,1 | 100 / 88 / 1,2 | 100 / 86 / 1,0 | 100 / 83 / 1,1 | 99 / 79 / 2,4 | 100 / 87 / 1,6 |
| Elfo | 100 / 84 / 2,0 | 100 / 83 / 1,9 | 100 / 85 / 1,3 | 100 / 88 / 1,1 | 100 / 85 / 1,1 | 100 / 84 / 1,5 | 100 / 84 / 2,6 | 100 / 88 / 1,5 |
| Elfo+libro | 100 / 84 / 2,0 | 100 / 83 / 2,0 | 100 / 85 / 1,1 | 100 / 87 / 1,3 | 100 / 86 / 1,1 | 100 / 84 / 1,3 | 100 / 85 / 2,7 | 100 / 88 / 1,6 |
| Bardo | 100 / 86 / 2,0 | 100 / 85 / 2,1 | 100 / 86 / 2,0 | 100 / 87 / 1,5 | 100 / 87 / 1,2 | 100 / 82 / 1,8 | 100 / 83 / 3,0 | 100 / 88 / 2,5 |
| Sacerdote | 100 / 82 / 2,1 | 100 / 83 / 2,1 | 100 / 86 / 1,2 | 100 / 88 / 1,5 | 100 / 85 / 1,2 | 100 / 82 / 1,8 | 100 / 83 / 3,4 | 100 / 87 / 1,4 |
| Chierico | 100 / 84 / 2,0 | 100 / 85 / 2,1 | 100 / 85 / 1,9 | 100 / 88 / 1,6 | 100 / 86 / 1,1 | 100 / 81 / 1,9 | 100 / 82 / 3,3 | 100 / 88 / 2,1 |
| Druido | 100 / 85 / 2,1 | 100 / 85 / 2,1 | 100 / 86 / 2,0 | 100 / 87 / 1,7 | 100 / 86 / 1,1 | 100 / 85 / 1,7 | 100 / 87 / 2,8 | 100 / 88 / 2,2 |

Che cosa dicono:

- **Un solo incantamento cambia tutto per le classi fisiche.** Il gruppo di riferimento (Guerriero, Ladro e Mago) vince il 93% contro il Drago e il 99% contro il Lich, dove con le armi comuni faceva il 3% e il 7%: nel gruppo di quattro non c'è più nulla che distingua le classi (quasi tutto 100%). Il divario di §6 contro i boss dipende quindi in buona parte dall'equipaggiamento medio delle simulazioni e non dalle classi.
- **Da soli, Ladro ed Elfo salgono molto**: contro la Strega dallo 0% al 74% (Ladro) e al 98% (Elfo), contro il Lich dallo 0% al 59% e al 94%. Il Guerriero no: 6% contro la Strega, 17% contro l'Idra, 1% contro il Lich. Ipotesi, non verificata: il danno dell'incantamento passa per il moltiplicatore dei danni magici della classe (Guerriero 0,5, Ladro 1,0, Elfo 1,4).
- **L'Elfo con il libro peggiora**: perdendo la seconda spada fa meno contro la Strega (69% contro 98%) e il Lich (43% contro 94%) e con più round contro i mostri normali. Fa meglio solo contro l'Idra (88% contro 53%), che è immune a terra, acqua e aria ma non all'arcano del dardo. Al livello 7 le due spade valgono più del libro (che ha metà del bonus).
- **Il Chierico resta il più debole da solo** anche con la mazza incantata: scheletri 11%, Strega 5%, Idra 9%, Lich 2%. Il Bardo, con la spada incantata, migliora molto (Idra 95%, Troll e Viverna 100% in 5–6 round).

### 5.4 Che cosa fanno davvero le tattiche

`AnalisiDeiPersonaggiGiocabiliTest#azioni` conta le azioni per scontro di ogni classe da sola al livello 7: contro una viverna Guerriero e Ladro fanno solo attacchi fisici (3,7–4,1 a scontro); l'Elfo 2,2 dardi arcani (e quasi nessun attacco: con il dardo che cresce con il livello la sua tattica lo preferisce alle armi); il Mago un solo dardo arcano (1,06: uccide quasi subito); il Druido 2 volte radici e una pelle di corteccia; il Chierico 11,4 attacchi e 3,3 imposizioni delle mani; il Bardo 6,3 attacchi, 2,8 canti di valore e 1,2 curativi; il **Sacerdote 20,2 attacchi fisici e nessuna luce divina**, perché la sua tattica predefinita la usa solo contro i non morti (`TatticheBase.sacerdote`: voce "Luce divina sui non morti"). Da solo contro tre goblin al livello 7 il Sacerdote non vince mai (0%), mentre contro tre scheletri vince sempre.

## 6. Lettura d'insieme

Quel che emerge dalle misure (le spiegazioni marcate come ipotesi non sono verificate):

| Classe | Punti di forza | Punti deboli | Perché sceglierla |
| :--- | :--- | :--- | :--- |
| Mago | Il danno più alto in assoluto (dardo 404 al livello 7, 6 bersagli, 143 di magia, recupero magico ×1,5); da solo vince quasi tutto (al livello 7 Troll 100%, Viverna 99%, Idra 97%) | Salute più bassa (211), parata 1; da solo vince due volte su tre contro la Strega (67%) e un quarto delle volte contro il Lich (26%) | Chi vuole chiudere gli scontri in fretta; come quarto del gruppo di riferimento lo porta dal 48% al 100% contro la Strega e dal 7% al 100% contro il Lich (ma lo fanno anche Elfo, Bardo, Sacerdote, Chierico e Druido) |
| Druido | Il miglior quarto del gruppo contro il Drago (63%, 86% e 99% ai livelli 3, 5 e 7; al livello 7 gli altri vanno dal 6% al 61%), da solo batte Troll e Viverna; abilità di gruppo (pelle di corteccia, rigenerazione) e danni di terra e veleno | Le immunità di Idra, Strega e Lich a terra e veleno lo lasciano senza danno (da solo 0% contro l'Idra); fuoco ×1,25 | Chi vuole un incantatore che regge il gruppo, non solo lo scontro singolo |
| Guerriero | Salute 396 e parata 51 al livello 7: da solo vince contro Goblin, Scheletro e Troll (100%) e Viverna (91%); aiuta il gruppo contro Strega (91% invece di 48%) e Lich (36% invece di 7%) | Danni magici ×0,5: non fa niente contro la Strega e il Lich da solo (0%) e poco contro l'Idra (11%) | Chi vuole il frontale senza pensieri; è la scelta più semplice |
| Ladro | Critico e furtività; da solo vince contro Troll e Viverna; come quarto è la classe senza magia che più aiuta contro il Lich (56%, contro il 36% del Guerriero) | Da solo contro gli scheletri 41%, contro Idra e Strega 1% e 0% | Chi vuole colpire forte con poca salute (264) e punta sui critici |
| Elfo | Resistenza magica e percezione ×1,4, critico e precisione; due armi; il dardo cresce con il livello (148 al livello 7, 43% di una viverna) e da solo contro l'Idra vince il 68% | Come quarto del gruppo contro il Drago fa il 48%, come il Mago; da solo con la Strega e il Lich non vince quasi mai | Chi vuole versatilità: spade e dardo, con una resistenza magica che gli altri non hanno |
| Bardo | Supporto: curativo, valore, ipnotico; contrattazione ×1,2; ha la soggezione migliore | Da solo è lento (10–34 round) e perde con gli scheletri (7%); non porta bastone né libro | Chi gioca il gruppo e non il singolo; come quarto contro il Drago fa il 55% |
| Sacerdote | Magia 98, potere magico 170, alba sacra e luce divina; come quarto contro il Drago 61% e sempre 100% su Strega e Lich | Con la tattica predefinita usa la luce solo sui non morti e il resto del tempo attacca con la mazza (forza 7): da solo 0% contro i goblin | Chi vuole il contrasto ai non morti e un supporto che toglie gli stati |
| Chierico | Salute 343, parata 10 con scudo e armatura; cura (imposizione) e scaccia i non morti | Da solo cala contro gli scheletri (13%, e scacciare i non morti non serve contro lo Scheletro che è immune) e contro l'Idra (12%); come quarto contro il Drago 49% | Chi vuole il guaritore robusto in prima linea |

## 7. Squilibri e idee

Sono idee da discutere, non modifiche fatte:

1. **Il Mago domina.** Il danno magico è la moltiplicazione di molte cose (×2 di classe, potere magico ×1,7 con bastone e libro, bersagli ×1,3, bonus fisso del libro) e la base dell'abilità pesa poco: il Mago ha il danno più alto e il recupero di magia migliore, e i suoi limiti sono la salute e la parata. Leve: il moltiplicatore di classe `MAGO_MOLTIPLICATORE_DANNI_MAGICI`, il potere magico di bastone e libro, o il bonus del libro.
2. **Il dardo dell'Elfo** era inutile alle alte dotazioni (base 15 fissa, nessun bastone, libro a metà: la tattica non lo lanciava mai) e ora cresce di 4 di base a ogni livello oltre il primo (`DARDO_ARCANO_DANNI_ELFO_PER_LIVELLO`): da solo l'Elfo al livello 7 contro lo Scheletro passa dal 27% al 96% e contro l'Idra dal 6% al 68%. Resta da vedere se il costo (4 contro 2 del Mago) è quello giusto e se il libro, che l'Elfo può già portare, lo aiuti abbastanza.
3. **Il Sacerdote da solo non fa danno** perché la tattica predefinita limita la luce divina ai non morti: è una scelta di `TatticheBase.sacerdote`, non del personaggio, ma nei test dà risultati che non rispecchiano chi la usa a mano (a mano il giocatore la lancia contro chiunque: nel log di una partita vera, con la luce a base 100, un lancio uccideva una viverna in un colpo).
4. **Il Druido è l'incantatore più forte contro i boss** ma ha tre nemici che ne annullano l'attacco (Idra, Strega, Lich, immuni a terra e veleno): se si vuole che sia meno universale, basta lasciare queste immunità; se si vuole più equilibrio, le sue abilità di gruppo (pelle, rigenerazione) sono la leva.
5. **Il Bardo e il Chierico sono lenti da soli** (10–34 round) ma nel gruppo reggono: la loro utilità si vede solo con il gruppo, per questo servirebbero analisi con gruppi di quattro o cinque diversi, non solo con quel gruppo di riferimento.
6. **Gli scheletri non sono equivalenti per tutti**: da soli (livello 7) li battono il 99% di Sacerdote, il 100% di Druido e Guerriero, l'89% del Mago e il 96% dell'Elfo (dopo la modifica al suo dardo; prima il 27%), ma solo il 41% del Ladro, il 13% del Chierico e il 7% del Bardo. Ipotesi, non verificata: le loro resistenze ai danni fisici o le armi usate.
7. **Strega e Lich** sono ora molto più resistenti (salute base 190 e 300; al livello 7 circa 502 e 793 punti) e di fatto si battono solo con la magia (o con un guaritore): il Guerriero e il Ladro da soli non li battono mai. Va bene finché i gruppi hanno un incantatore; per un gruppo senza è un muro.

8. **Le armi incantate pesano più delle classi.** Con un incantamento di fulmine su ogni arma (§5.3) i divari contro i boss di Guerriero, Ladro e Chierico quasi spariscono nel gruppo, e da soli salgono Ladro, Elfo e Bardo. Prima di dare abilità nuove a Guerriero e Ladro conviene misurare con l'equipaggiamento reale che un giocatore trova, non con quello medio senza incantamenti; il Martello sacro del Chierico (in `todo.md`) resta motivato dai risultati da soli, perché neanche con la mazza incantata batte scheletri e boss magici.

## 8. Come si rifanno le misure

Le analisi sono test manuali (le cifre variano di qualche punto da un'esecuzione all'altra, perché i personaggi hanno attributi a caso) in `12-foresta-motore`, che `mvn test` normale salta e si attivano con `-Danalisi=true`:

```
mvn test -pl 12-foresta-motore -am -Dtest="AnalisiDeiPersonaggiGiocabiliTest#schede" -Danalisi=true -Dsurefire.failIfNoSpecifiedTests=false
mvn test -pl 12-foresta-motore -am -Dtest="AnalisiDeiPersonaggiGiocabiliTest#scontri" -Danalisi=true -Dsurefire.failIfNoSpecifiedTests=false
mvn test -pl 12-foresta-motore -am -Dtest="AnalisiDeiPersonaggiGiocabiliTest#scontri" -Danalisi=true -Dequip=incantato -Dsurefire.failIfNoSpecifiedTests=false
mvn test -pl 12-foresta-motore -am -Dtest="AnalisiDeiPersonaggiGiocabiliTest#azioni" -Danalisi=true -Dsurefire.failIfNoSpecifiedTests=false
mvn test -pl 12-foresta-motore -am -Dtest="AnalisiDanniAbilitaInnateTest" -Danalisi=true -Dsurefire.failIfNoSpecifiedTests=false
```

`AnalisiAbilitaInnateTest` (analisi, catene, contributo, sensibilità, pelle) misura invece quanto pesano singole abilità di Chierico e Druido in gruppi diversi. `RoundASimulato` porta il numero di round al passo di quello del gioco, quindi le condizioni sul round delle tattiche scattano come nella partita vera.

Misure precedenti di Chierico e Druido (con `AnalisiAbilitaInnateTest`, 200–300 scontri per cella, gruppo di tre con Guerriero e Ladro, **prima** di alcuni cambi di valori e di una correzione del round simulato, quindi indicative): la magia non è il limite (si lanciano 0,2–2 abilità a scontro e i personaggi iniziano gli scontri con l'80–95% della magia); l'imposizione delle mani è l'unica abilità che pesa molto, contro nemici fisici e ai livelli 3–5 (contro l'Idra al livello 3 il gruppo vince il 67,5% con l'abilità e il 20% senza); radici, sciame e rigenerazione sono forti ma situazionali; la pelle di corteccia in percentuale era inutile e in punti fissi (10 × √livello) serve contro avversari pericolosi (le streghe: 68% contro 58% al livello 5, 89% contro 78% al livello 8) e negli scontri facili costa solo un round.

## Osservazioni

- I numeri sono di un primo giro: niente pozioni, niente pergamene (che per Mago, Sacerdote e Druido sono una parte grossa del danno nel gioco vero), tattica predefinita e non quella che sceglierebbe un giocatore, personaggi tutti al livello degli avversari, un solo gruppo di riferimento.
- L'equipaggiamento delle simulazioni è quello medio del generatore, non quello che un giocatore trova davvero (più vario, con incantamenti): conta soprattutto per i tre incantatori, per cui bastone e libro triplicano il danno (§4.1).
- Senza equipaggiamento il dardo dell'Elfo (148 al livello 7) supera quello del Mago (130) dal livello 4; con bastone e libro, che un Mago porta sempre, il Mago resta quasi tre volte più forte (404). `DardoArcanoTest` confronta infatti i due con il Mago equipaggiato.
- Le immagini di Chierico, Chierica, Druido e Druida (personaggi, icone di classe e icone delle abilità) sono vere.
- Radici e Sciame usano le immunità dell'incantesimo di Terra e di Veleno e registrano l'esito dei colpi come `TERRA` e `VELENO`; essendo azioni su un gruppo, i requisiti delle tattiche non guardano le immunità scoperte di un solo bersaglio.

## Da fare / idee aperte

- Decidere le leve di §7, a partire dal Mago (moltiplicatore di classe o potere magico di bastone e libro); il dardo dell'Elfo è stato rivisto (§7, punto 2) e va riverificato con il libro.
- Rifare le misure con gruppi di quattro o cinque diversi, con pozioni e pergamene e, per il Sacerdote, anche con una tattica che usi la luce divina contro chiunque.
- Provare le abilità in partite intere (`SimulazionePartiteTest`) e non solo in scontri singoli.
- Insegnare al `GiocatoreAutomatico` dei test a cantare (non conosce i canti).
- Decidere se i chierici e i druidi compaiono anche come avversari (oggi no, sono solo giocabili e reclutabili).
- Il Chierico non ha un'abilità di attacco: se risulta troppo debole rispetto al Sacerdote si può dargliene una o ritoccare i valori.
- Le armi del Druido, se un giorno ci saranno gli archi.
- Un altro canto del Bardo (per esempio uno di scherno che abbassa le difese) è fuori da questo lavoro.
- Tarare insieme la probabilità di cedere del canto ipnotico (stessa coppia soggezione/coraggio con cui si spaventano i nemici nel resto del gioco) e i valori di `VIANDANTE_*` e `NON_COMBATTENTE_*`, uguali a quelli del Bardo.
