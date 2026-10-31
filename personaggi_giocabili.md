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

Misure con `AnalisiDeiPersonaggiGiocabiliTest#schede` (media di 100 personaggi per classe, equipaggiati come nel gioco: Guerriero spada, scudo, elmo e armatura; Ladro due spade e veste (l'elmo non lo porta, come il Mago e il Sacerdote); Elfo due spade, elmo e veste; Bardo spada, scudo e veste; Mago, Sacerdote e Druido bastone, libro e veste; Chierico mazza, scudo, elmo e armatura; i pezzi sono al livello del personaggio con i valori medi del generatore, senza parte casuale).

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
| Guerriero | 23,4 | 13,4 | 18,6 | 9,4 | 10,3 | 11,4 | 7,5 | 9,0 |
| Ladro | 16,3 | 18,4 | 10,2 | 10,2 | 10,3 | 10,2 | 18,4 | 17,4 |
| Mago | 7,2 | 10,3 | 10,3 | 27,3 | 18,3 | 13,3 | 7,3 | 6,1 |
| Elfo | 16,1 | 18,2 | 10,1 | 13,1 | 13,1 | 13,2 | 10,2 | 13,0 |
| Bardo | 16,2 | 13,1 | 10,1 | 13,2 | 13,1 | 18,2 | 10,2 | 10,1 |
| Sacerdote | 7,3 | 10,2 | 13,3 | 15,3 | 27,4 | 13,2 | 7,3 | 8,0 |
| Chierico | 15,1 | 9,1 | 17,1 | 10,2 | 23,1 | 12,2 | 7,2 | 8,0 |
| Druido | 7,5 | 13,3 | 11,5 | 15,4 | 25,4 | 10,6 | 10,4 | 9,3 |

| Classe | PREC | VEL | FURT | PAR | RES.M | PERC | SOGG | FURIA | CORAG | VAL | CONTR |
| :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| Guerriero | 3,1 | 3,0 | 3,0 | 51,0 | 10,0 | 3,0 | 3,0 | 6,0 | 4,0 | 5,0 | 2,0 |
| Ladro | 5,0 | 5,3 | 6,0 | 3,0 | 15,0 | 4,0 | 3,0 | 4,0 | 3,0 | 3,0 | 4,0 |
| Mago | 3,0 | 2,0 | 2,0 | 1,0 | 15,5 | 4,0 | 3,0 | 2,0 | 3,0 | 3,1 | 3,0 |
| Elfo | 5,0 | 4,2 | 5,0 | 10,0 | 16,0 | 5,0 | 4,0 | 4,0 | 3,0 | 4,0 | 3,0 |
| Bardo | 3,0 | 3,1 | 3,0 | 34,0 | 21,2 | 3,2 | 5,0 | 4,0 | 4,0 | 3,0 | 4,0 |
| Sacerdote | 3,0 | 3,0 | 2,0 | 2,0 | 18,0 | 5,0 | 3,0 | 2,0 | 4,0 | 5,0 | 3,0 |
| Chierico | 3,0 | 2,0 | 2,0 | 8,7 | 5,8 | 4,0 | 3,2 | 3,0 | 4,0 | 5,0 | 3,0 |
| Druido | 4,0 | 3,3 | 3,1 | 2,0 | 16,0 | 6,0 | 3,0 | 2,1 | 3,0 | 4,0 | 3,0 |

Cose che saltano all'occhio: la **parata** del Guerriero (51) e del Bardo (34 con scudo e armatura) contro il 10 di Chierico ed Elfo, il 3 del Ladro (senza elmo) e l'1–2 degli incantatori; la salute del Guerriero (396 al livello 7) quasi doppia di quella del Mago (211); il **potere magico 170** di chi porta bastone e libro (Mago, Sacerdote, Druido), mentre Elfo e Bardo restano a 100 perché non portano il bastone e l'Elfo ha metà del bonus del libro.

## 4. Abilità innate

Le abilità costano `MAGIA`, non consumano pergamene, occupano il turno e compaiono fra le scelte del comando incantesimi; non sono in `TipoIncantesimo`, quindi non finiscono in cofani, negozi o repertorio dei mostri. Ognuna ha un `TipoAzione` con lo stesso nome, quindi funziona anche nel combattimento automatico (`EsecutoreTattiche`, `TatticheBase`). Un personaggio `SILENZIATO` non le usa. Le capacità dipendono dal `TipoPersonaggio`, non dalla classe Java (`AbilitaInnate.conosciutaDa`, `CantoDelBardo.conosciutoDa`).

**Riepilogo per classe.** I lanci sono quelli che reggono a magia piena al livello 7 (magia massima divisa per il costo); "bersagli" è su chi agisce.

| Classe | Abilità innate | Magia al liv. 7 | Lanci a magia piena | Bersagli | Quando le usa la tattica predefinita |
| :--- | :--- | ---: | :--- | :--- | :--- |
| Guerriero | nessuna (lo stato `BERSERK` fa crescere il danno fisico con la salute persa, fino a +50%, e dipende dalla furia ×1,5) | 34 | – | – | – |
| Ladro | Attacco alle spalle (senza Magia, ricarica 3, solo con almeno tre personaggi vivi nel gruppo) | 44 | – | uno | sul più forte, quando c'è e il gruppo è abbastanza grande; altrimenti come prima (finisce i feriti, poi il più ferito) |
| Mago | Dardo arcano | 143 | 71 | uno | sui feriti sotto il 40% (colpo più economico che basta) e sul più forte quando non ha una pergamena adatta; il Silenzio sui maghi avversari ha la precedenza |
| Elfo | Dardo arcano (cresce con il livello) | 98 | 24 | uno | sui maghi avversari e sul più ferito, ma solo se rende più dell'attacco con le armi |
| Bardo | Canto curativo, canto ipnotico, canto di valore, Urlo della Banshee (dopo `IlViaggioDellaMoglie`) | 84 | 21 / 28 / 16 / 14 | alleato più ferito; fino a 4 avversari; tutto il gruppo; fino a 4 avversari | il curativo sotto il 60% di salute, il valore se c'è chi non è ispirato, ipnotico e urlo contro più avversari |
| Sacerdote | Alba sacra, Luce divina | 98 | 19 / 9 | tutto il gruppo; fino a 4 avversari (e i non morti alleati) | l'alba con almeno due alleati con uno stato negativo, dopo il terzo round; la luce solo sui non morti |
| Chierico | Scacciare i non morti, Imposizione delle mani, Martello sacro (3 di magia, ricarica 2) | 64 | 16 / 12 / 21 | fino a 3 non morti; alleato più ferito; un avversario | l'imposizione sul più ferito sotto il 50% di salute (anche gli ospiti); scacciare i non morti sul primo avversario; il martello sacro sul più forte; poi la mazza sul più ferito |
| Druido | Radici, Sciame, Pelle di corteccia, Rigenerazione | 88 | 29 / 29 / 17 / 22 | fino a 4 avversari; fino a 4 avversari; tutto il gruppo; un alleato | la rigenerazione sul più ferito sotto il 70%, la pelle di corteccia subito dopo, radici e sciame sul primo avversario; poi un incantesimo o il bastone sul più ferito |

| Classe | Abilità | Costo (magia) | Ricarica (turni) | Effetto |
| :--- | :--- | ---: | ---: | :--- |
| Mago, Elfo | Dardo arcano | 4 (Mago), 6 (Elfo) | 2 | Danno `ARCANO` a **un solo** bersaglio: base 20 (Mago) o 15 più 4 per ogni livello oltre il primo (Elfo: 31 al livello 5, 39 al livello 7), poi livello, intelligenza, potere magico e bonus del libro |
| Sacerdote | Alba sacra | 5 | 4 | Toglie al gruppo gli effetti di stato negativi e i modificatori temporanei (compreso `ISPIRATO`) |
| Sacerdote | Luce divina | 4 | 4 | Danno `SACRO` (base 35) a più avversari, il doppio ai non morti; investe anche i non morti del proprio gruppo (lo Scheletro della Mietitura), che le cure non guariscono. Il Gargoyle di pietra non la sente |
| Chierico | Martello sacro | 3 | 2 | `SACRO` su un avversario (danno base 30), il doppio sui non morti; accecamento possibile (effetto nativo del SACRO) |
| Chierico | Scacciare i non morti | 4 | 4 | `SPAVENTATO` a fino a `NUMERO_BERSAGLI` non morti, con la soggezione contro il coraggio; non serve contro chi è già spaventato o immune (Scheletro e Lich lo sono) |
| Chierico | Imposizione delle mani | 5 | 4 | Cura il più ferito di 20 × √livello, aumentata dalla saggezza (÷50) |
| Druido | Radici | 3 | 4 | Danno di `TERRA` (base 40) a più avversari; `IMMOBILIZZATO` o `ACCECATO` |
| Druido | Sciame | 3 | 5 | Danno di `VELENO` (base 40) a più avversari; `AVVELENATO` |
| Druido | Pelle di corteccia | 5 | 5 | A tutto il gruppo, per 4 turni, 10 × √livello punti fissi di parata e di resistenza magica (modificatori temporanei, che l'alba sacra non toglie) |
| Druido | Rigenerazione | 4 | 5 | `RIGENERAZIONE` a un alleato per 4 turni: a ogni turno guarisce (5 + 5% della salute massima) × (1 + saggezza/50) |
| Bardo | Canto curativo | 6 | 4 | Cura il più ferito di 30 × √livello, aumentata dalla saggezza (÷50) |
| Bardo | Canto ipnotico | 3 | 4 | `SPAVENTATO` o `CONFUSO` (a caso) a fino a `NUMERO_BERSAGLI` avversari, con la soggezione del bardo contro il coraggio |
| Bardo | Canto di valore | 5 | 5 | `ISPIRATO` a tutto il gruppo per 4 turni: coraggio e precisione +20% + 2,5% per livello del bardo, tetto 50% (non si somma: il secondo rinfresca) |
| Bardo | Urlo della Banshee | 6 | 5 | Il canto ipnotico potenziato (soggezione ×1,5) che dà sia `SPAVENTATO` sia `CONFUSO` con un solo tiro. **Non noto all'inizio**: lo insegna il bardo della locanda dopo `IlViaggioDellaMoglie` (intermezzo `IntermezzoUrloDellaBanshee`, `CantiImparati`), e vale per tutto il gruppo |

**La ricarica.** Dopo l'uso, ogni abilità innata e ogni canto va in ricarica: non si può riusare finché non è passato il numero di turni della tabella (`TipoAbilita.getRicarica`, `TipoCanto.getRicarica`; costanti `…_RICARICA` in `CostantiAbilitaInnate` e `CostantiCanti`). Un turno è un round completo di combattimento, un passo fra una locazione e l'altra o un'ora di sonno (pesata dal tipo di riposo, arrotondata per eccesso): lo stesso orologio degli effetti di stato (§ "Il tempo che passa" in [`motore_di_gioco.md`](motore_di_gioco.md)). Le ricariche non si azzerano a fine scontro e si salvano con il personaggio. La regola: chi dà uno stato o un buff per N turni si ricarica in N + 1 (Pelle di corteccia, Rigenerazione e Canto di valore dipendono dalle loro durate, 4 + 1), e chi può causare uno stato di 3 turni (spavento, immobilizzazione, cecità, confusione) si ricarica in 4, così il bersaglio ha almeno un turno di respiro; il Canto curativo cura più dell'Imposizione delle mani e quindi costa di più, con la stessa ricarica. In UI le abilità in ricarica compaiono, non cliccabili, fra le scelte dell'incantesimo: lo sfondo dell'icona si riempie dal basso, dal rosso scuro al verde scuro passando per il giallo scuro, di (turni passati)/(ricarica + 1) (vedi `SfondoARiempimento` e `TipoIcona.getIconaInRicarica`).

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

**Tabelle rifatte il 2026-10-31**, dopo aver corretto `RoundASimulato` (le ricariche di abilità e canti ora calano a fine round, come nel gioco: prima ogni abilità si usava una volta sola per scontro) e dopo aver tolto l'elmo a Ladro, Mago e Sacerdote. Riletti con i numeri nuovi anche i commenti (§5.2-5.4, §6, §7) e la tabella degli attributi di §3 (la parata del Ladro scende a 3). I danni di §4.1 non dipendono dalle ricariche e non sono cambiati: il Martello sacro e l'attacco alle spalle non ci sono ancora (l'analisi dei danni non li conosce).

Misure con `AnalisiDeiPersonaggiGiocabiliTest#scontri` (100 scontri per cella, `-Diterazioni=<n>` per cambiarli): i miei agiscono con la **tattica predefinita** della loro classe (`RegistroTattiche.predefinitaDelGiocabile`) nel round a coppie di `RoundASimulato`, contro i mostri con le loro tattiche base, **senza pozioni né pergamene**, a salute e magia piene, equipaggiati come in §3, tutti allo stesso livello. Ogni cella è **vittorie % / salute residua dei miei % / round medi delle vittorie**; "0" vuol dire nessuna vittoria. Goblin e Scheletro da soli sono tre alla volta (secondo `LocazioneBase.numeroMassimoDiMostri`, al massimo 3), gli altri uno; nel gruppo di quattro anche Troll, Viverna e Scheletro sono tre.

### 5.1 Ogni classe da sola

Poco realistico (un gruppo ha più personaggi), ma isola quanto vale la classe da sé: danno, resistenza e autonomia. Chi cura o ispira, da solo, non ha chi aiutare.

**Livello 3.**

| Classe | Goblin | Scheletro | Troll | Viverna | Strega | Idra | Lich |
| :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| Guerriero | 100 / 85 / 3,1 | 100 / 81 / 4,7 | 100 / 62 / 7,2 | 55 / 41 / 5,1 | 0 | 3 / 14 / 9,7 | 0 |
| Ladro | 100 / 80 / 2,6 | 100 / 74 / 3,9 | 93 / 45 / 6,0 | 61 / 34 / 4,7 | 0 | 1 / 23 / 10,0 | 0 |
| Mago | 100 / 88 / 1,1 | 100 / 87 / 1,3 | 99 / 57 / 3,1 | 100 / 74 / 3,1 | 26 / 58 / 5,3 | 6 / 18 / 4,0 | 0 |
| Elfo | 100 / 82 / 2,9 | 100 / 80 / 3,0 | 90 / 44 / 6,2 | 62 / 46 / 5,0 | 0 | 7 / 19 / 7,0 | 0 |
| Bardo | 100 / 77 / 6,1 | 100 / 77 / 9,7 | 99 / 65 / 16,1 | 74 / 65 / 13,7 | 0 | 0 | 0 |
| Sacerdote | 100 / 78 / 8,7 | 100 / 86 / 1,5 | 48 / 45 / 23,4 | 76 / 58 / 22,1 | 1 / 73 / 42,0 | 0 | 13 / 88 / 5,1 |
| Chierico | 100 / 76 / 3,9 | 100 / 83 / 2,0 | 88 / 45 / 8,6 | 76 / 51 / 7,6 | 0 | 1 / 8 / 9,0 | 0 |
| Druido | 100 / 84 / 2,2 | 100 / 84 / 2,6 | 100 / 73 / 9,2 | 100 / 88 / 11,0 | 0 | 0 | 0 |

**Livello 5.**

| Classe | Goblin | Scheletro | Troll | Viverna | Strega | Idra | Lich |
| :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| Guerriero | 100 / 79 / 5,4 | 100 / 69 / 8,5 | 100 / 77 / 6,1 | 82 / 44 / 4,6 | 0 | 6 / 29 / 10,5 | 0 |
| Ladro | 100 / 59 / 4,5 | 93 / 37 / 6,6 | 98 / 46 / 5,4 | 89 / 45 / 4,1 | 0 | 1 / 11 / 7,0 | 0 |
| Mago | 100 / 64 / 3,3 | 100 / 62 / 3,1 | 99 / 63 / 2,8 | 100 / 86 / 1,2 | 31 / 70 / 3,1 | 73 / 24 / 3,1 | 1 / 96 / 3,0 |
| Elfo | 100 / 67 / 3,8 | 100 / 60 / 4,6 | 98 / 62 / 4,3 | 99 / 68 / 3,3 | 0 | 19 / 23 / 6,3 | 0 |
| Bardo | 100 / 76 / 12,8 | 96 / 72 / 22,5 | 100 / 75 / 13,5 | 84 / 71 / 11,7 | 0 | 5 / 24 / 25,0 | 0 |
| Sacerdote | 60 / 25 / 15,4 | 99 / 85 / 1,5 | 71 / 51 / 22,1 | 94 / 58 / 20,4 | 0 | 0 | 9 / 76 / 6,7 |
| Chierico | 100 / 62 / 7,6 | 98 / 68 / 4,3 | 95 / 53 / 8,3 | 72 / 58 / 6,3 | 0 | 5 / 33 / 11,6 | 0 |
| Druido | 100 / 80 / 3,0 | 100 / 78 / 5,0 | 100 / 77 / 9,0 | 100 / 86 / 7,3 | 8 / 87 / 9,6 | 0 | 3 / 85 / 10,7 |

**Livello 7.**

| Classe | Goblin | Scheletro | Troll | Viverna | Strega | Idra | Lich |
| :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| Guerriero | 100 / 73 / 8,2 | 100 / 58 / 12,0 | 100 / 82 / 5,4 | 83 / 47 / 4,3 | 0 | 4 / 27 / 14,0 | 0 |
| Ladro | 91 / 36 / 5,8 | 18 / 16 / 8,6 | 93 / 48 / 4,7 | 88 / 55 / 3,8 | 0 | 3 / 20 / 8,0 | 0 |
| Mago | 92 / 27 / 5,1 | 43 / 16 / 5,0 | 98 / 76 / 1,7 | 100 / 87 / 1,1 | 38 / 68 / 3,2 | 79 / 24 / 3,1 | 0 |
| Elfo | 96 / 48 / 5,4 | 94 / 35 / 5,5 | 100 / 71 / 3,3 | 97 / 70 / 3,1 | 0 | 37 / 28 / 5,2 | 0 |
| Bardo | 100 / 76 / 18,1 | 53 / 38 / 31,1 | 100 / 72 / 11,0 | 96 / 75 / 9,2 | 0 | 17 / 44 / 25,9 | 0 |
| Sacerdote | 0 | 100 / 65 / 2,7 | 69 / 42 / 21,2 | 97 / 78 / 19,2 | 1 / 93 / 34,0 | 0 | 19 / 89 / 7,6 |
| Chierico | 87 / 39 / 11,6 | 90 / 47 / 6,7 | 93 / 55 / 7,5 | 93 / 64 / 6,5 | 1 / 88 / 10,0 | 14 / 37 / 14,9 | 0 |
| Druido | 100 / 76 / 4,0 | 100 / 68 / 7,5 | 100 / 78 / 7,5 | 100 / 86 / 6,8 | 23 / 79 / 15,6 | 0 | 3 / 82 / 22,0 |

### 5.2 Come quarto del gruppo Guerriero, Ladro e Mago

Il gruppo di tre che fa da riferimento ("(nessuno)") ha già un fisico, uno scassinatore e un incantatore: aggiungere una classe mostra che cosa porta in più. Contro gli avversari facili tutte le classi fanno il 100% (non si distingue nulla), quindi sono stati scelti avversari che mettono alla prova: branchi di tre Troll o Viverne, la Strega, i boss. Con le ricariche che calano come nel gioco (vedi la nota in testa a §5) il gruppo di riferimento regge quasi tutto, e l'unica cella che distingue davvero le classi è il Drago: al livello 7 il gruppo da solo vince il 36% delle volte, con un quarto il 62% (Elfo), 76% (Mago), 80% (Guerriero), 96% (Bardo, Sacerdote e Chierico), 98% (Ladro) e 100% (Druido); al livello 3 il Lich, che il gruppo da solo vince il 76% delle volte, con quasi ogni quarto va al 100%.

**Livello 3.**

| Classe | Troll | Viverna | Scheletro | Strega | Minotauro gigante | Idra | Drago | Lich |
| :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| (nessuno) | 100 / 64 / 5,4 | 74 / 47 / 5,6 | 100 / 78 / 3,4 | 97 / 82 / 5,1 | 99 / 59 / 4,3 | 93 / 56 / 5,0 | 3 / 46 / 11,7 | 76 / 86 / 8,1 |
| Guerriero | 100 / 74 / 4,3 | 95 / 61 / 4,0 | 100 / 82 / 2,8 | 100 / 87 / 3,4 | 100 / 72 / 3,2 | 100 / 70 / 3,3 | 23 / 54 / 11,1 | 100 / 84 / 4,9 |
| Ladro | 100 / 72 / 4,0 | 100 / 61 / 3,5 | 100 / 81 / 2,7 | 100 / 87 / 2,8 | 100 / 70 / 2,9 | 100 / 73 / 2,5 | 85 / 57 / 10,0 | 100 / 87 / 4,2 |
| Mago | 100 / 71 / 3,5 | 99 / 63 / 3,4 | 100 / 82 / 2,4 | 100 / 86 / 3,2 | 100 / 73 / 2,0 | 100 / 71 / 2,2 | 20 / 58 / 8,5 | 100 / 85 / 4,4 |
| Elfo | 100 / 71 / 4,2 | 99 / 58 / 3,9 | 100 / 82 / 2,7 | 100 / 88 / 3,2 | 100 / 70 / 2,9 | 98 / 72 / 2,7 | 69 / 52 / 11,1 | 100 / 88 / 4,6 |
| Bardo | 100 / 74 / 5,0 | 97 / 65 / 4,7 | 100 / 81 / 3,1 | 100 / 87 / 3,9 | 100 / 73 / 3,4 | 100 / 73 / 3,3 | 51 / 49 / 11,8 | 100 / 88 / 5,6 |
| Sacerdote | 100 / 69 / 4,9 | 100 / 60 / 4,7 | 100 / 84 / 1,6 | 100 / 87 / 3,7 | 100 / 66 / 3,4 | 100 / 71 / 3,3 | 61 / 67 / 10,1 | 100 / 87 / 4,2 |
| Chierico | 100 / 73 / 4,7 | 98 / 63 / 4,1 | 100 / 82 / 2,7 | 100 / 88 / 3,8 | 100 / 71 / 3,3 | 100 / 71 / 3,0 | 50 / 47 / 12,1 | 100 / 88 / 5,3 |
| Druido | 100 / 78 / 4,5 | 100 / 73 / 4,2 | 100 / 83 / 2,9 | 100 / 87 / 3,7 | 100 / 71 / 3,2 | 100 / 73 / 3,5 | 89 / 70 / 10,3 | 100 / 88 / 5,3 |

**Livello 5.**

| Classe | Troll | Viverna | Scheletro | Strega | Minotauro gigante | Idra | Drago | Lich |
| :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| (nessuno) | 100 / 66 / 5,1 | 97 / 54 / 5,2 | 100 / 78 / 3,1 | 100 / 88 / 4,5 | 99 / 74 / 2,9 | 96 / 65 / 3,7 | 15 / 53 / 9,6 | 100 / 87 / 7,2 |
| Guerriero | 100 / 79 / 3,4 | 100 / 71 / 3,4 | 100 / 83 / 2,6 | 100 / 87 / 2,7 | 100 / 79 / 2,4 | 100 / 75 / 2,5 | 65 / 65 / 9,4 | 100 / 88 / 4,0 |
| Ladro | 100 / 77 / 3,1 | 100 / 68 / 3,2 | 100 / 82 / 2,5 | 100 / 87 / 2,4 | 100 / 79 / 2,0 | 100 / 77 / 1,9 | 93 / 70 / 8,7 | 100 / 87 / 3,8 |
| Mago | 100 / 75 / 3,1 | 100 / 72 / 3,0 | 100 / 83 / 1,9 | 100 / 87 / 2,2 | 100 / 82 / 1,5 | 100 / 78 / 1,3 | 61 / 60 / 6,3 | 100 / 88 / 3,4 |
| Elfo | 100 / 75 / 3,3 | 100 / 70 / 3,3 | 100 / 82 / 2,5 | 100 / 87 / 2,8 | 100 / 80 / 1,8 | 100 / 79 / 1,9 | 56 / 60 / 9,3 | 100 / 87 / 4,0 |
| Bardo | 100 / 80 / 4,7 | 100 / 73 / 3,9 | 100 / 82 / 2,9 | 100 / 87 / 3,4 | 100 / 82 / 2,7 | 100 / 77 / 2,4 | 92 / 67 / 10,6 | 100 / 88 / 4,9 |
| Sacerdote | 100 / 71 / 4,4 | 100 / 71 / 3,9 | 100 / 83 / 1,7 | 100 / 88 / 3,1 | 100 / 75 / 3,0 | 100 / 73 / 2,5 | 82 / 71 / 9,2 | 100 / 88 / 2,8 |
| Chierico | 100 / 77 / 3,9 | 100 / 69 / 3,7 | 100 / 83 / 2,3 | 100 / 87 / 3,4 | 100 / 80 / 2,3 | 100 / 76 / 2,3 | 79 / 60 / 10,6 | 100 / 88 / 4,4 |
| Druido | 100 / 81 / 4,0 | 100 / 80 / 3,6 | 100 / 84 / 2,6 | 100 / 87 / 3,4 | 100 / 79 / 2,5 | 100 / 79 / 2,3 | 99 / 84 / 7,9 | 100 / 87 / 4,2 |

**Livello 7.**

| Classe | Troll | Viverna | Scheletro | Strega | Minotauro gigante | Idra | Drago | Lich |
| :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| (nessuno) | 100 / 70 / 4,8 | 100 / 60 / 4,5 | 100 / 78 / 2,9 | 100 / 87 / 3,9 | 100 / 78 / 2,3 | 98 / 78 / 2,5 | 36 / 59 / 9,4 | 100 / 87 / 6,6 |
| Guerriero | 100 / 78 / 3,2 | 99 / 73 / 3,3 | 100 / 82 / 2,3 | 100 / 87 / 2,4 | 100 / 82 / 1,8 | 100 / 83 / 2,0 | 80 / 69 / 8,6 | 100 / 87 / 3,8 |
| Ladro | 100 / 77 / 3,1 | 100 / 72 / 3,0 | 100 / 81 / 2,2 | 100 / 87 / 2,2 | 100 / 80 / 1,8 | 100 / 84 / 1,7 | 98 / 76 / 7,2 | 100 / 87 / 3,4 |
| Mago | 100 / 76 / 3,0 | 100 / 76 / 2,9 | 100 / 83 / 1,5 | 100 / 87 / 1,8 | 100 / 83 / 1,2 | 100 / 84 / 1,1 | 76 / 66 / 5,4 | 100 / 88 / 3,1 |
| Elfo | 100 / 77 / 3,2 | 100 / 73 / 3,1 | 100 / 83 / 2,0 | 100 / 88 / 2,4 | 100 / 81 / 1,6 | 100 / 82 / 1,8 | 62 / 65 / 7,1 | 100 / 87 / 3,8 |
| Bardo | 100 / 81 / 4,2 | 100 / 77 / 3,6 | 100 / 84 / 2,5 | 100 / 88 / 3,2 | 100 / 84 / 1,8 | 100 / 83 / 2,1 | 96 / 74 / 9,4 | 100 / 87 / 4,4 |
| Sacerdote | 100 / 73 / 3,9 | 100 / 76 / 3,6 | 100 / 83 / 1,6 | 100 / 88 / 3,0 | 100 / 82 / 1,9 | 100 / 83 / 2,0 | 96 / 75 / 8,9 | 100 / 87 / 2,2 |
| Chierico | 100 / 77 / 3,5 | 100 / 75 / 3,4 | 100 / 83 / 1,8 | 100 / 88 / 3,1 | 100 / 84 / 1,6 | 100 / 83 / 1,8 | 96 / 70 / 9,1 | 100 / 88 / 4,4 |
| Druido | 100 / 81 / 3,9 | 100 / 83 / 3,3 | 100 / 83 / 2,6 | 100 / 87 / 3,1 | 100 / 84 / 1,7 | 100 / 84 / 2,1 | 100 / 86 / 6,7 | 100 / 87 / 3,8 |

### 5.3 Con armi incantate (e l'Elfo con il libro)

Stesse simulazioni con `-Dequip=incantato`: tutte le armi hanno un incantamento di `FULMINE` del grado adatto al livello (nessuno dei mostri vi è debole o immune), per Guerriero, Ladro, Elfo, Bardo e Chierico; Mago, Sacerdote e Druido restano con bastone e libro. La riga "Elfo+libro" ha la spada incantata e il libro magico al posto della seconda spada (il libro occupa la mano secondaria).

**Livello 7, ognuno da solo.**

| Classe | Goblin | Scheletro | Troll | Viverna | Strega | Idra | Lich |
| :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| Guerriero | 100 / 80 / 3,9 | 100 / 71 / 7,6 | 100 / 87 / 3,3 | 99 / 79 / 2,8 | 13 / 84 / 4,2 | 27 / 39 / 6,5 | 4 / 73 / 8,8 |
| Ladro | 100 / 71 / 3,3 | 100 / 58 / 3,6 | 100 / 88 / 2,1 | 100 / 87 / 2,0 | 64 / 86 / 3,0 | 41 / 46 / 3,8 | 61 / 84 / 6,3 |
| Mago | 89 / 26 / 5,2 | 60 / 16 / 5,1 | 99 / 75 / 1,7 | 100 / 87 / 1,1 | 44 / 69 / 3,3 | 82 / 24 / 3,1 | 2 / 86 / 3,0 |
| Elfo | 100 / 72 / 3,4 | 100 / 61 / 3,8 | 100 / 86 / 2,1 | 100 / 85 / 2,0 | 95 / 86 / 3,8 | 58 / 51 / 4,9 | 90 / 87 / 7,4 |
| Elfo+libro | 100 / 61 / 5,3 | 100 / 52 / 5,1 | 100 / 85 / 3,5 | 98 / 85 / 2,9 | 61 / 87 / 5,5 | 61 / 57 / 3,2 | 43 / 86 / 11,5 |
| Bardo | 100 / 76 / 10,3 | 98 / 75 / 16,9 | 100 / 82 / 5,1 | 100 / 77 / 5,5 | 28 / 85 / 7,0 | 53 / 50 / 12,4 | 8 / 88 / 14,5 |
| Sacerdote | 0 | 98 / 64 / 2,8 | 64 / 50 / 21,6 | 100 / 76 / 20,4 | 0 | 0 | 14 / 81 / 8,2 |
| Chierico | 91 / 43 / 11,1 | 93 / 50 / 5,9 | 95 / 54 / 7,7 | 89 / 62 / 6,5 | 3 / 84 / 10,3 | 16 / 51 / 12,5 | 0 |
| Druido | 100 / 74 / 4,0 | 100 / 72 / 7,3 | 100 / 79 / 7,0 | 100 / 87 / 7,1 | 21 / 82 / 18,7 | 0 | 7 / 62 / 22,7 |

**Livello 7, come quarto del gruppo Guerriero+Ladro+Mago** (anche Guerriero e Ladro del gruppo hanno le armi incantate).

| Classe | Troll | Viverna | Scheletro | Strega | Minotauro gigante | Idra | Drago | Lich |
| :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| (nessuno) | 100 / 78 / 2,5 | 100 / 79 / 2,2 | 100 / 82 / 2,1 | 100 / 88 / 1,5 | 100 / 85 / 1,1 | 100 / 79 / 1,6 | 95 / 80 / 4,1 | 100 / 87 / 2,6 |
| Guerriero | 100 / 84 / 2,1 | 100 / 82 / 2,0 | 100 / 85 / 1,9 | 100 / 87 / 1,0 | 100 / 86 / 1,1 | 100 / 83 / 1,3 | 100 / 82 / 2,8 | 100 / 87 / 1,3 |
| Ladro | 100 / 82 / 1,9 | 100 / 81 / 1,1 | 100 / 84 / 1,0 | 100 / 87 / 1,0 | 100 / 85 / 1,0 | 100 / 84 / 1,0 | 100 / 81 / 2,7 | 100 / 88 / 1,5 |
| Mago | 100 / 81 / 1,8 | 100 / 83 / 1,2 | 100 / 85 / 1,1 | 100 / 87 / 1,0 | 100 / 86 / 1,0 | 100 / 83 / 1,0 | 100 / 79 / 2,7 | 100 / 88 / 1,4 |
| Elfo | 100 / 82 / 2,1 | 100 / 81 / 1,9 | 100 / 84 / 1,3 | 100 / 88 / 1,0 | 100 / 86 / 1,0 | 100 / 84 / 1,1 | 100 / 83 / 2,7 | 100 / 87 / 1,5 |
| Elfo+libro | 100 / 82 / 2,2 | 100 / 81 / 2,0 | 100 / 85 / 1,1 | 100 / 88 / 1,0 | 100 / 86 / 1,1 | 100 / 85 / 1,1 | 100 / 83 / 2,9 | 100 / 88 / 1,5 |
| Bardo | 100 / 85 / 2,4 | 100 / 86 / 2,1 | 100 / 85 / 2,0 | 100 / 87 / 1,5 | 100 / 87 / 1,1 | 100 / 84 / 1,4 | 100 / 86 / 3,3 | 100 / 88 / 2,4 |
| Sacerdote | 100 / 82 / 2,4 | 100 / 82 / 2,2 | 100 / 84 / 1,3 | 100 / 88 / 1,4 | 100 / 85 / 1,2 | 100 / 84 / 1,4 | 100 / 82 / 3,4 | 100 / 87 / 1,0 |
| Chierico | 100 / 83 / 2,4 | 100 / 83 / 2,0 | 100 / 85 / 1,3 | 100 / 88 / 1,5 | 100 / 86 / 1,1 | 100 / 84 / 1,4 | 100 / 84 / 3,6 | 100 / 87 / 1,5 |
| Druido | 100 / 83 / 2,5 | 100 / 84 / 2,1 | 100 / 84 / 2,0 | 100 / 88 / 1,5 | 100 / 86 / 1,1 | 100 / 84 / 1,5 | 100 / 87 / 3,1 | 100 / 87 / 2,1 |

Che cosa dicono:

- **Un solo incantamento cambia tutto per le classi fisiche.** Il gruppo di riferimento (Guerriero, Ladro e Mago) vince il 95% contro il Drago, dove con le armi comuni faceva il 36% (al livello 7), e con un quarto qualunque il 100%: nel gruppo di quattro non c'è più nulla che distingua le classi (tutto 100%). Il divario di §6 contro i boss dipende quindi in buona parte dall'equipaggiamento medio delle simulazioni e non dalle classi.
- **Da soli, Ladro ed Elfo salgono molto**: contro la Strega dallo 0% al 64% (Ladro) e al 95% (Elfo), contro il Lich dallo 0% al 61% e al 90%. Il Guerriero poco: 13% contro la Strega, 27% contro l'Idra, 4% contro il Lich. Ipotesi, non verificata: il danno dell'incantamento passa per il moltiplicatore dei danni magici della classe (Guerriero 0,5, Ladro 1,0, Elfo 1,4).
- **L'Elfo con il libro peggiora**: perdendo la seconda spada fa meno contro la Strega (61% contro 95%) e il Lich (43% contro 90%) e con più round contro i mostri normali. Contro l'Idra è alla pari (61% contro 58%). Al livello 7 le due spade valgono più del libro (che ha metà del bonus).
- **Il Chierico con la mazza incantata e il Martello sacro** tiene contro i mostri normali (Goblin 91%, Scheletro 93%, Troll 95%, Viverna 89%), ma contro i boss magici resta debole: Strega 3%, Idra 16%, Lich 0%. Il Bardo, con la spada incantata, migliora (Troll e Viverna 100% in 5 round, Scheletro 98%, Idra 53%, Strega 28%).

### 5.4 Che cosa fanno davvero le tattiche

`AnalisiDeiPersonaggiGiocabiliTest#azioni` conta le azioni per scontro di ogni classe da sola al livello 7: contro una viverna Guerriero e Ladro fanno solo attacchi fisici (4,3 e 3,8 a scontro; il Ladro da solo non può colpire alle spalle, che chiede almeno tre personaggi nel gruppo); l'Elfo 2,0 dardi arcani e 1,2 attacchi (il dardo ha ricarica 2: lo rilancia appena può); il Mago un solo dardo arcano (1,06: uccide quasi subito); il Druido 2 volte radici, 1,1 sciame, 2 pelle di corteccia e 2,1 attacchi; il Chierico 3,3 martelli sacri, 3,3 attacchi e 0,6 imposizioni delle mani; il Bardo 6,1 attacchi, 2,1 canti di valore e 0,8 curativi; il **Sacerdote 19,0 attacchi fisici e nessuna luce divina**, perché la sua tattica predefinita la usa solo contro i non morti (`TatticheBase.sacerdote`: voce "Luce divina sui non morti"). Da solo contro tre goblin al livello 7 il Sacerdote non vince mai (0%), mentre contro tre scheletri vince sempre (100%).

## 6. Lettura d'insieme

Quel che emerge dalle misure (le spiegazioni marcate come ipotesi non sono verificate):

| Classe | Punti di forza | Punti deboli | Perché sceglierla |
| :--- | :--- | :--- | :--- |
| Mago | Il danno più alto in assoluto (dardo 404 al livello 7, 6 bersagli, 143 di magia, recupero magico ×1,5); da solo vince quasi tutto (al livello 7 Troll 98%, Viverna 100%, Idra 79%) | Salute più bassa (211), parata 1; da solo vince il 38% delle volte contro la Strega, il 43% contro gli scheletri e mai contro il Lich | Chi vuole chiudere gli scontri in fretta; come quarto del gruppo di riferimento lo porta al livello 3 dall'1% al 100% contro il Lich (ma lo fanno anche Elfo, Bardo, Sacerdote, Chierico e Druido) |
| Druido | Il miglior quarto del gruppo contro il Drago (89%, 99% e 100% ai livelli 3, 5 e 7; al livello 7 gli altri vanno dal 62% al 98%), da solo batte Goblin, Scheletro, Troll e Viverna (100%); abilità di gruppo (pelle di corteccia, rigenerazione) e danni di terra e veleno | Le immunità di Idra, Strega e Lich a terra e veleno lo lasciano senza danno (da solo 0% contro l'Idra, 23% contro la Strega e 3% contro il Lich); fuoco ×1,25 | Chi vuole un incantatore che regge il gruppo, non solo lo scontro singolo |
| Guerriero | Salute 396 e parata 51 al livello 7: da solo vince contro Goblin, Scheletro e Troll (100%) e Viverna (83%); come quarto porta il gruppo di riferimento dal 36% all'80% contro il Drago | Danni magici ×0,5: non fa niente contro la Strega e il Lich da solo (0%) e poco contro l'Idra (4%) | Chi vuole il frontale senza pensieri; è la scelta più semplice |
| Ladro | Critico e furtività; da solo vince contro Troll e Viverna; come quarto è la classe che più aiuta contro il Drago dopo il Druido (98% contro il 36% del gruppo da solo e l'80% del Guerriero); l'attacco alle spalle (ricarica 3) lo usa quando il gruppo è di almeno tre | Senza l'elmo ha parata 3: da solo contro gli scheletri 18%, contro Idra, Strega e Lich 3%, 0% e 0% | Chi vuole colpire forte con poca salute (264) e punta sui critici |
| Elfo | Resistenza magica e percezione ×1,4, critico e precisione; due armi; il dardo cresce con il livello (148 al livello 7, 43% di una viverna) e da solo contro l'Idra vince il 37% | Come quarto del gruppo contro il Drago fa il 62%, meno del Mago (76%); da solo con la Strega e il Lich non vince mai | Chi vuole versatilità: spade e dardo, con una resistenza magica che gli altri non hanno |
| Bardo | Supporto: curativo, valore, ipnotico; contrattazione ×1,2; ha la soggezione migliore | Da solo è lento (9–31 round) e vince solo il 53% contro gli scheletri; non porta bastone né libro | Chi gioca il gruppo e non il singolo; come quarto contro il Drago fa il 96% |
| Sacerdote | Magia 98, potere magico 170, alba sacra e luce divina; come quarto contro il Drago 96% | Con la tattica predefinita usa la luce solo sui non morti e il resto del tempo attacca con la mazza (forza 7): da solo 0% contro i goblin | Chi vuole il contrasto ai non morti e un supporto che toglie gli stati |
| Chierico | Salute 343, parata 10 con scudo e armatura; cura (imposizione), scaccia i non morti e il Martello sacro (3 di magia, ricarica 2, il doppio sui non morti) | Da solo contro i boss magici non vince quasi mai (Idra 14%, Strega e Lich 1% e 0%); come quarto contro il Drago 96% | Chi vuole il guaritore robusto in prima linea |

## 7. Squilibri e idee

Sono idee da discutere, non modifiche fatte:

1. **Il Mago ha il danno più alto, ma non domina più.** Il danno magico è la moltiplicazione di molte cose (×2 di classe, potere magico ×1,7 con bastone e libro, bersagli ×1,3, bonus fisso del libro) e la base dell'abilità pesa poco: il Mago ha il danno più alto e il recupero di magia migliore, e i suoi limiti sono la salute, la parata e la ricarica del dardo (2 turni). Con le ricariche corrette nelle simulazioni (vedi la nota in testa a §5) da solo vince il 92% contro i Goblin, il 43% contro gli scheletri e il 38% contro la Strega, e come quarto contro il Drago (76%) fa meno di Ladro (98%), Druido (100%), Bardo, Sacerdote e Chierico (96%). Leve, se serviranno: il moltiplicatore di classe `MAGO_MOLTIPLICATORE_DANNI_MAGICI`, il potere magico di bastone e libro, o il bonus del libro.
2. **Il dardo dell'Elfo** era inutile alle alte dotazioni (base 15 fissa, nessun bastone, libro a metà: la tattica non lo lanciava mai) e ora cresce di 4 di base a ogni livello oltre il primo (`DARDO_ARCANO_DANNI_ELFO_PER_LIVELLO`): da solo l'Elfo al livello 7 contro lo Scheletro passa dal 27% al 94% e contro l'Idra dal 6% al 37% (misure con le ricariche corrette). Resta da vedere se il costo (4 contro 2 del Mago) è quello giusto e se il libro, che l'Elfo può già portare, lo aiuti abbastanza.
3. **Il Sacerdote da solo non fa danno** perché la tattica predefinita limita la luce divina ai non morti: è una scelta di `TatticheBase.sacerdote`, non del personaggio, ma nei test dà risultati che non rispecchiano chi la usa a mano (a mano il giocatore la lancia contro chiunque: nel log di una partita vera, con la luce a base 100, un lancio uccideva una viverna in un colpo).
4. **Il Druido è l'incantatore più forte contro i boss** ma ha tre nemici che ne annullano l'attacco (Idra, Strega, Lich, immuni a terra e veleno): se si vuole che sia meno universale, basta lasciare queste immunità; se si vuole più equilibrio, le sue abilità di gruppo (pelle, rigenerazione) sono la leva.
5. **Il Bardo è lento da solo** (9–31 round) ma nel gruppo regge (come quarto contro il Drago 96%); il Chierico, con il Martello sacro, è molto meno lento (6–15 round). La loro utilità si vede soprattutto con il gruppo, per questo servirebbero analisi con gruppi di quattro o cinque diversi, non solo con quel gruppo di riferimento.
6. **Gli scheletri non sono equivalenti per tutti**: da soli (livello 7) li battono il 100% di Sacerdote, Druido e Guerriero, il 94% dell'Elfo (dopo la modifica al suo dardo; prima il 27%) e il 90% del Chierico, ma solo il 53% del Bardo, il 43% del Mago e il 18% del Ladro (che senza l'elmo ha parata 3). Ipotesi, non verificata: le loro resistenze ai danni fisici o le armi usate.
7. **Strega e Lich** sono ora molto più resistenti (salute base 190 e 300; al livello 7 circa 502 e 793 punti) e di fatto si battono solo con la magia (o con un guaritore): il Guerriero e il Ladro da soli non li battono mai. Va bene finché i gruppi hanno un incantatore; per un gruppo senza è un muro.

8. **Le armi incantate pesano più delle classi.** Con un incantamento di fulmine su ogni arma (§5.3) i divari contro i boss di Guerriero, Ladro e Chierico quasi spariscono nel gruppo, e da soli salgono Ladro, Elfo e Bardo. Prima di dare abilità nuove a Guerriero e Ladro conviene misurare con l'equipaggiamento reale che un giocatore trova, non con quello medio senza incantamenti; il Martello sacro del Chierico (aggiunto il 2026-10-31) lo porta a battere i mostri normali e gli scheletri da solo (90% e oltre), ma non i boss magici (Strega 1%, Idra 14%, Lich 0%), che restano per gli incantatori.

## 8. Come si rifanno le misure

Le analisi sono test manuali (le cifre variano di qualche punto da un'esecuzione all'altra, perché i personaggi hanno attributi a caso) in `12-foresta-motore`, che `mvn test` normale salta e si attivano con `-Danalisi=true`:

```
mvn test -pl 12-foresta-motore -am -Dtest="AnalisiDeiPersonaggiGiocabiliTest#schede" -Danalisi=true -Dsurefire.failIfNoSpecifiedTests=false
mvn test -pl 12-foresta-motore -am -Dtest="AnalisiDeiPersonaggiGiocabiliTest#scontri" -Danalisi=true -Dsurefire.failIfNoSpecifiedTests=false
mvn test -pl 12-foresta-motore -am -Dtest="AnalisiDeiPersonaggiGiocabiliTest#scontri" -Danalisi=true -Dequip=incantato -Dsurefire.failIfNoSpecifiedTests=false
mvn test -pl 12-foresta-motore -am -Dtest="AnalisiDeiPersonaggiGiocabiliTest#azioni" -Danalisi=true -Dsurefire.failIfNoSpecifiedTests=false
mvn test -pl 12-foresta-motore -am -Dtest="AnalisiDanniAbilitaInnateTest" -Danalisi=true -Dsurefire.failIfNoSpecifiedTests=false
```

`AnalisiAbilitaInnateTest` (analisi, catene, contributo, sensibilità, pelle) misura invece quanto pesano singole abilità di Chierico e Druido in gruppi diversi. `RoundASimulato` porta il numero di round al passo di quello del gioco, quindi le condizioni sul round delle tattiche scattano come nella partita vera, e a fine round fa calare le ricariche di abilità e canti come il gioco (`Personaggio.trascorriUnTurno`).

Misure precedenti di Chierico e Druido (con `AnalisiAbilitaInnateTest`, 200–300 scontri per cella, gruppo di tre con Guerriero e Ladro, **prima** di alcuni cambi di valori e di una correzione del round simulato, quindi indicative): la magia non è il limite (si lanciano 0,2–2 abilità a scontro e i personaggi iniziano gli scontri con l'80–95% della magia); l'imposizione delle mani è l'unica abilità che pesa molto, contro nemici fisici e ai livelli 3–5 (contro l'Idra al livello 3 il gruppo vince il 67,5% con l'abilità e il 20% senza); radici, sciame e rigenerazione sono forti ma situazionali; la pelle di corteccia in percentuale era inutile e in punti fissi (10 × √livello) serve contro avversari pericolosi (le streghe: 68% contro 58% al livello 5, 89% contro 78% al livello 8) e negli scontri facili costa solo un round.

## Osservazioni

- I numeri sono di un primo giro: niente pozioni, niente pergamene (che per Mago, Sacerdote e Druido sono una parte grossa del danno nel gioco vero), tattica predefinita e non quella che sceglierebbe un giocatore, personaggi tutti al livello degli avversari, un solo gruppo di riferimento.
- L'equipaggiamento delle simulazioni è quello medio del generatore, non quello che un giocatore trova davvero (più vario, con incantamenti): conta soprattutto per i tre incantatori, per cui bastone e libro triplicano il danno (§4.1).
- Senza equipaggiamento il dardo dell'Elfo (148 al livello 7) supera quello del Mago (130) dal livello 4; con bastone e libro, che un Mago porta sempre, il Mago resta quasi tre volte più forte (404). `DardoArcanoTest` confronta infatti i due con il Mago equipaggiato.
- Le immagini di Chierico, Chierica, Druido e Druida (personaggi, icone di classe e icone delle abilità) sono vere.
- Radici e Sciame usano le immunità dell'incantesimo di Terra e di Veleno e registrano l'esito dei colpi come `TERRA` e `VELENO`; essendo azioni su un gruppo, i requisiti delle tattiche non guardano le immunità scoperte di un solo bersaglio.

## Da fare / idee aperte

- Decidere le leve di §7: con le ricariche corrette il Mago non domina più; restano il Sacerdote (da solo non vince nemmeno contro i Goblin: la tattica predefinita usa la luce divina solo sui non morti) e il Ladro senza elmo (parata 3, scheletri 18%); il dardo dell'Elfo è stato rivisto (§7, punto 2) e va riverificato con il libro.
- Rifare le misure con gruppi di quattro o cinque diversi, con pozioni e pergamene e, per il Sacerdote, anche con una tattica che usi la luce divina contro chiunque.
- Provare le abilità in partite intere (`SimulazionePartiteTest`) e non solo in scontri singoli.
- Insegnare al `GiocatoreAutomatico` dei test a cantare (non conosce i canti).
- Decidere se i chierici e i druidi compaiono anche come avversari (oggi no, sono solo giocabili e reclutabili).
- Il Chierico non ha un'abilità di attacco: se risulta troppo debole rispetto al Sacerdote si può dargliene una o ritoccare i valori.
- Le armi del Druido, se un giorno ci saranno gli archi.
- Un altro canto del Bardo (per esempio uno di scherno che abbassa le difese) è fuori da questo lavoro.
- Tarare insieme la probabilità di cedere del canto ipnotico (stessa coppia soggezione/coraggio con cui si spaventano i nemici nel resto del gioco) e i valori di `VIANDANTE_*` e `NON_COMBATTENTE_*`, uguali a quelli del Bardo.
