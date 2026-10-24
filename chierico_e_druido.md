# Chierico e druido

Due classi giocanti, ognuna con la forma femminile (`CHIERICO`/`CHIERICA`, `DRUIDO`/`DRUIDA`), e le sei **abilità** innate che le distinguono. Si scelgono
come le altre classi a inizio partita e compaiono fra i compagni reclutabili in locanda.

## 1. Le classi

| | Chierico / Chierica | Druido / Druida |
| :--- | :--- | :--- |
| Idea | Il sacerdote che combatte in prima linea, fra il Sacerdote e il Guerriero | L'incantatore della natura |
| Partenza dei valori | Copia del Guerriero (`Chierico`, `Chierica`), con valori propri in `Costanti` (`CHIERICO_*`, `CHIERICA_*`) | Copia del Bardo (`Druido`, `Druida`), con valori propri (`DRUIDO_*`, `DRUIDA_*`) |
| Armi | Mazza, spada, scudo, armatura; né bastone né libro; niente doppia arma | Bastone magico, lancia, libro magico; né scudo né armatura (di metallo); niente doppia arma. **Non esistono archi nel gioco**, quindi non li usa |
| Dotazione iniziale | Mazza e scudo | Bastone magico e veste |
| Resistenze (`TipoPersonaggio`) | `NECROTICO` e `MALEDIZIONE` ×0,75 | `ACQUA`, `TERRA`, `VELENO` ×0,75; `FUOCO` ×1,25, `TAGLIENTE` ×1,1 |
| Magia innata | Nessuna: **Alba sacra** e **Luce Divina** sono del solo Sacerdote (`AlbaSacra.conosciutaDa`, `AbilitaInnate.conosciutaDa(classe, TipoAbilita.LUCE_DIVINA)`), il chierico ha le due abilità (scacciare i non morti, imposizione delle mani) e può comunque lanciare gli incantesimi con le pergamene | nessuna dei sacerdoti, né il dardo arcano |
| Abilità | Scacciare i non morti, Imposizione delle mani | Radici, Sciame, Pelle di corteccia, Rigenerazione |

Il ruolo (`TipoPersonaggio.getRuolo`) è lo stesso nelle due forme: le tattiche non le distinguono. I testi (note dei moltiplicatori) hanno la voce del
maschile e il femminile rimanda a quella (`CHIERICA.COME=CHIERICO`). La distribuzione dei punti di partenza sta in `LanciatoreDeiDadi`.

## 2. Le abilità

Una famiglia nuova, sul modello dei canti del Bardo (vedi [`bardo.md`](bardo.md)): costano `MAGIA`, non consumano pergamene, non si comprano né si trovano,
occupano il turno, compaiono fra le scelte del comando incantesimi e fanno rispondere l'avversario. Non sono in `TipoIncantesimo`, quindi non finiscono in
cofani, negozi o repertorio dei mostri.

- `tipi/TipoAbilita` (con il suo `Comando` e i testi in `tipo_abilita_it.properties`); `abilita/Abilita` (l'interfaccia), `AbilitaInnate` (chi le ha e quando
  servono), `FabbricaAbilita`.
- Ognuna ha un `TipoAzione` con lo stesso nome (`SCACCIARE_I_NON_MORTI`, `IMPOSIZIONE_DELLE_MANI`, `RADICI`, `SCIAME`, `PELLE_DI_CORTECCIA`,
  `RIGENERAZIONE`), quindi funziona anche nel combattimento automatico: `EsecutoreTattiche`, `Requisiti`, `RegistroTattiche.azioniPossibili` (sono solo di chi
  le conosce) e le tattiche predefinite `TatticheBase.chierico()` e `druido()`.
- `LocazioneBase.usaUnaAbilita` e `Automa.entraInStatoSceltaIncantesimoDaLanciare` le portano nel gioco manuale.

| Abilità | Di chi | Effetto | Costo (`MAGIA`) |
| :--- | :--- | :--- | :--- |
| Scacciare i non morti | Chierico | `SPAVENTATO` a fino a `NUMERO_BERSAGLI` non morti avversari, con `SOGGEZIONE` contro `CORAGGIO` come il canto ipnotico. Non serve contro chi è già spaventato o immune (Scheletro e Lich lo sono) | 4 |
| Imposizione delle mani | Chierico | Cura il più ferito: 20 × √livello, aumentata dalla `SAGGEZZA` (meno del canto curativo, che è 30 × √livello) | 5 |
| Radici | Druido | Danno di `TERRA` (40) a più avversari, come un incantesimo: tiro per colpire, immunità e `IMMOBILIZZATO`/`ACCECATO` del tipo di danno | 3 |
| Sciame | Druido | Come le radici, con danno di `VELENO` (40) e `AVVELENATO` | 3 |
| Pelle di corteccia | Druido | A tutto il gruppo, per 4 turni, `10 × √livello` punti (17 a livello 3, 22 a livello 5, 28 a livello 8) di `PARATA` e di `RESISTENZA_MAGICA`, punti fissi e non una percentuale (vedi §4), con due modificatori temporanei (vedi [`modificatori_temporanei.md`](modificatori_temporanei.md)) che l'Alba sacra non toglie; non si raddoppia, si rinfresca | 5 |
| Rigenerazione | Druido | `RIGENERAZIONE` (nuovo effetto di stato benefico) a un alleato per 4 turni: a ogni turno guarisce di (5 + 5% della salute massima) × (1 + `SAGGEZZA`/50), quantità che sta nell'intensità dell'effetto (`PersonaggioBase.applicaDanniDaEffettiDiStato`) | 4 |

I valori stanno in `Costanti` (`ABILITA_*`).

## 3. Chi si sceglie

Chierico, Chierica, Druido e Druida si scelgono fra le classi di inizio partita come le altre (`Automa`), anche senza la modalità di prova, e possono essere campioni della
sfida a carta, forbici e sasso (`LaSfidaDeiCampioni.classiAmichevoli`). Fra i compagni reclutabili in locanda (`RegistroPersonaggi`) ci sono Frederick (Chierico),
Elowen (Chierica), Alder (Druido) e Hazel (Druida). Nessun mostro è di queste classi; in prova si possono aggiungere dalla finestra di prova (`FinestraDiProva`), che
usa `FabbricaPersonaggi`.

## 4. Simulazioni

`AnalisiAbilitaInnateTest` (in `foresta-motore`, test; si attiva con `-Danalisi=true`, quindi `mvn test` normale la salta) fa agire i miei personaggi con la loro
tattica predefinita (`TatticheBase.chierico()` e `druido()`) nel round a coppie di `RoundASimulato`, contro i mostri con le loro tattiche base, senza pozioni né
pergamene e a salute e magia piene. I parametri sono `-Diterazioni=<n>` e, per `sensibilita`, `-Dclassi=CHIERICO,DRUIDO,...`; i metodi sono `analisi` (numeri di
partenza e scontri), `catene` (12 scontri di seguito con il solo recupero di fine locazione), `contributo` (la stessa tattica con le voci di un'abilità disattivate) e
`sensibilita` (una prova stretta da ripetere a ogni variante dei numeri). Per esempio:
`mvn test -pl foresta-motore -am -Dtest='AnalisiAbilitaInnateTest#sensibilita' -Dsurefire.failIfNoSpecifiedTests=false -Danalisi=true -Diterazioni=300`.
Per la pelle di corteccia ci sono `pelleContro` (il druido con tutte le abilità, senza la pelle, con la sola pelle, senza nessuna, in un gruppo di cinque contro un
gruppo di avversari: `-Dnemico=VIVERNA -Dquantita=5 -Dlivelli=3,5,8`) e `pelleCondizionata` (la voce della pelle con condizioni diverse: dal secondo round,
con un ferito, contro almeno quattro avversari).

`RoundASimulato` porta il numero di round dello stato del combattimento al passo del suo giro (nel gioco lo fa la lista dei turni): prima restava a 1, e le condizioni sul
round (`ROUND`, `ROUND_DALL_ULTIMO_USO`, di cui sono fatte anche le tattiche base dei mostri) non scattavano mai. I primi numeri qui sotto sono di prima della correzione.

Risultati dei numeri attuali (200–300 scontri per cella, errore circa ±3 punti), con un gruppo di tre (X, Guerriero, Ladro; X è il capo) contro Idra,
Minotauro gigante, Troll, Strega e Drago:

- **La magia non è il limite.** Ogni personaggio ha una sola azione per round e uno scontro dura 3–6 round, quindi si lanciano 0,2–2 abilità a scontro. In una catena di 12
  scontri il chierico inizia gli scontri con l'85–97% della magia e il druido con il 78–94% (da solo come supporto, il druido spende circa quanto recupera: 10–12 contro
  11–12 a scontro). A limitare il gruppo è la salute.
- **L'Imposizione delle mani è l'unica abilità che pesa molto**, contro nemici fisici e a livello 3–5: a livello 3 il gruppo vince contro l'Idra il 67,5% con l'abilità e il
  20% senza, contro il Minotauro gigante il 91% e il 55,5%, contro il Troll il 100% e il 76%. A livello 8 pesa poco e contro Strega e Lich niente.
- **Il costo e il prezzo in salute non la frenano:** con costo 8 o 12 la media delle vittorie cala di 0–7 punti, con il 25% o il 50% della cura tolto al chierico di 0–3
  (il bersaglio tipico è il capo, cioè il chierico stesso, e in quel caso non scatta). **La cura sì:** con la base a 14 invece di 20 la media a livello 3 e 5 passa da
  67,5% e 63,6% a 60,3% e 55,8%, e a livello 8 non cambia.
- **Scacciare i non morti** pesa poco. **Rigenerazione** e **Radici e sciame** sono forti ma situazionali (Rigenerazione contro il Troll a livello 3: 98% contro 70,5% senza; Radici
  contro il Minotauro gigante a livello 5: 63% contro 25,5% senza).
- **La Pelle di corteccia in percentuale era inutile, e in punti fissi è utile.** La prima versione dava +30% di parata e +20% di resistenza magica: chi non è un guerriero
  ha parata 1–11 e resistenza magica 5–18 ai livelli 3–8, quindi dava 0–3 punti e la probabilità di essere colpiti scendeva di 0–2 punti percentuali (il Guerriero a livello 5,
  dal 32% al 30% contro una viverna): costava l'azione del druido, che altrimenti andava a Radici, Sciame o Rigenerazione (a livello 8 contro cinque viverne 3,4 round
  con la pelle e 2,4 senza), e non migliorava nessun esito, nemmeno al raddoppio delle percentuali o con le condizioni sulla tattica. Con **`10 × √livello` punti fissi**
  (gruppo di cinque con il druido, 400–500 scontri; "senza" è la tattica senza la pelle):

  | Avversari | Livello | Vittorie con / senza | Salute residua con / senza | Caduti con / senza | Round con / senza |
  | :--- | :--- | :--- | :--- | :--- | :--- |
  | 5 viverne | 3 | 96,4% / 95,0% | 68% / 58% | 1,06 / 1,49 | 5,3 / 5,2 |
  | 5 viverne | 5 | 99,8% / 99,6% | 75% / 67% | 0,79 / 1,06 | 4,1 / 3,5 |
  | 5 viverne | 8 | 100% / 100% | 82% / 80% | 0,25 / 0,28 | 3,2 / 2,5 |
  | 3 streghe | 5 | **68,2% / 58,0%** | 63% / 59% | 2,73 / 3,24 | 4,9 / 4,5 |
  | 3 streghe | 8 | **89,4% / 77,8%** | 70% / 63% | 1,65 / 2,41 | 4,4 / 4,1 |
  | 5 hobgoblin | 5 | 100% / 100% | 79% / 80% | 0 / 0 | 3,2 / 2,3 |

  Serve contro avversari pericolosi (le viverne a livello 3–5, le streghe, che colpiscono con la magia) e negli scontri facili non fa danni ma costa circa un round
  (la prima azione del druido). Le condizioni sulla tattica (dal secondo round, con un ferito sotto il 70%, contro almeno quattro avversari) **non migliorano** l'uso
  immediato: contro le streghe il bonus serve fin dal primo round, e solo "dal secondo round" ne conserva quasi tutto (68,0% contro 70,3% a livello 5), quindi la voce
  resta com'è, la prima dopo le cure.
- **Rispetto alle altre classi** (media delle vittorie su quegli avversari, livello 3 / 5 / 8): Chierico 67,5 / 63,6 / 92,1 %, Druido 64,3 / 63,3 / 80,7 %, Bardo 63,2 / 72,3 / 79,3 %,
  Sacerdote 59,1 / 60,4 / 74,3 %, Guerriero 64,4 / 68,3 / 73,4 %. Stanno nella fascia delle classi esistenti; il Bardo cura più dell'Imposizione (30 × √livello contro 20, costo 4
  contro 5). Il Chierico a livello 8 sta sopra le altre, ma lì le abilità contano poco: pesano le sue statistiche di classe.

## Osservazioni

- **Immagini.** Quelle di Chierico, Chierica, Druido e Druida (personaggi, icone di classe e icone delle abilità) sono vere.
- I valori sono un primo giro, provati con le simulazioni della sezione 4 ma non con partite intere (`SimulazionePartiteTest`) né con pozioni e pergamene.
- Radici e Sciame usano le immunità dell'incantesimo di Terra e di Veleno e registrano l'esito dei colpi come `TERRA` e `VELENO` (`TipoAzione.di`); essendo azioni su
  un gruppo, i requisiti delle tattiche non guardano le immunità scoperte di un bersaglio solo.
- Il Druido è fuori da `CantoDelBardo`: non canta.

## Da fare / idee aperte

- Decidere se i chierici e i druidi compaiono anche come avversari (oggi no, sono solo giocabili e reclutabili).
- Ritoccare i numeri: se l'Imposizione delle mani risulta troppo forte a livello 3–5 la leva è la cura (`ABILITA_IMPOSIZIONE_DELLE_MANI_BASE`), non il costo; in alternativa un limite di
  lanci per scontro o una pausa fra due usi (il predicato `ROUND_DALL_ULTIMO_USO` delle tattiche esiste già).
- Rifare le misure principali (Imposizione delle mani, contributo delle abilità, catene) con il `RoundASimulato` corretto: i numeri sopra, a parte quelli della pelle, sono di prima della correzione.
- Provare le abilità in partite intere e con pozioni e pergamene, e capire se il Chierico a livello 8 è forte per le sue statistiche.
- Se il Chierico, senza Alba sacra e Luce Divina, risulta ora troppo debole rispetto al Sacerdote, si può dargli un'abilità in più o ritoccare i valori (vedi i numeri da provare).
- Le armi del Druido, se un giorno ci saranno gli archi.
