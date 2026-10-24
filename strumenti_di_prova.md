# Strumenti di prova

La finestra per chi sviluppa e i trucchi sui dadi: tutto quello che serve a provare un caso preciso senza aspettare che il caso
lo porti. Esiste solo in **modalità di prova** (argomento `MODALITA_DI_PROVA`, vedi `motore_di_gioco.md` §12); senza, il gioco non
sa che esiste.

## 1. Come è fatto

| Pezzo | Modulo | Che cosa fa |
| :--- | :--- | :--- |
| `strumenti/dado/ContestoLancio` | strumenti | L'elenco dei lanci che si possono truccare, ognuno con la sua descrizione |
| `strumenti/dado/TruccatoreDiDadi` | strumenti | L'interfaccia che `Dado` consulta prima di tirare un lancio con contesto |
| `strumenti/TrucchiDiSviluppo` | contratti | L'implementazione: un trucco permanente per contesto, che resta finché non lo si spegne |
| `strumenti/dado/Dado` | strumenti | `tira(contesto, …)`, `tiraAncheAUnaFaccia(contesto, N)`, `scegli(contesto, elenco)`, `sceltaVoluta(contesto, elenco)` |
| `interfacce/StrumentiDiSviluppo` | contratti | Quello che la finestra può fare alla partita: personaggi, partenza delle missioni, oggetti dei cofani |
| `motore/StrumentiDiSviluppoMotore` | motore | L'implementazione sul gruppo e sul registro delle missioni |
| `ui/sviluppo/FinestraDiProva` | swing | La finestra a tab (personaggi, gruppo, statistiche, missioni, dadi); la crea `Main` solo in modalità di prova |

La finestra vede solo i contratti (`VistaPartita` per leggere, `StrumentiDiSviluppo` e `TrucchiDiSviluppo` per cambiare), come ogni
altra parte della UI. `Main`, se la modalità di prova è accesa, installa `TrucchiDiSviluppo` come truccatore di `Dado` e apre la
finestra sul thread principale.

## 2. I dadi con contesto

I lanci senza contesto (`Dado.tira(6)`) restano al caso, o alla coda di `Dado.trucca` che usano i test. I lanci che interessano a chi
prova hanno un `ContestoLancio`:

| Contesto | Dove | Che cosa si sceglie |
| :--- | :--- | :--- |
| `CARTA_FORBICI_SASSO` | `PartitaCartaForbiciSasso.gioca` | La mossa dell'avversario (1 carta, 2 forbice, 3 sasso) |
| `TIPO_MOSTRO` | `LocazioneBase.crea` | Il tipo di mostro, fra quelli possibili in quella locazione per il livello del capo |
| `NUMERO_MOSTRI` | `LocazioneBase.crea` | Quanti mostri di quel tipo; il valore voluto vale solo se rientra nel limite della locazione (altrimenti va al caso) |
| `COFANO_ESITO` | `RaccoltaBottino` (apertura del cofano) | Che cosa c'è dentro, da 0 (niente) a 7 (pietre preziose); nei castelli lo 0 non esce |
| `COFANO_OGGETTO` | `RaccoltaBottino` (apertura del cofano) | L'oggetto speciale: niente, un ingrediente, un artefatto, un aggiornamento, un leggendario del catalogo o un libro tecnico (anche se già trovato; il suo intermezzo però scatta solo la prima volta) |

**Regole.**

- **La coda dei test vince.** Se `Dado.trucca` ha ancora risultati, i lanci con contesto li consumano come prima e il truccatore non
  viene interpellato: i test non cambiano comportamento.
- **Il trucco è permanente.** Finché la casella è accesa il lancio dà sempre quel valore; per spegnerlo si toglie la spunta (o
  "Spegni tutti i trucchi").
- **Un valore impossibile si ignora.** Un 5 per un lancio da 1 a 3, o un mostro che quella locazione non ammette, non si applica e il
  lancio va al caso. Il trucco non fa mai un errore: nasconderebbe il caso che si sta provando.
- **Con un solo risultato possibile non si trucca.** Un lancio a una faccia sola non interpella il truccatore: non c'è niente da decidere.
- **Il resto del caso non cambia.** Per i mostri, il tiro del 90% di "c'è un incontro" non è truccato: se la locazione non trova
  nessuno (10%) il trucco non ha effetto. Anche la percentuale che fa scattare la sfida a carta, forbici e sasso (in
  `LocazioneBase`) resta al caso.

Perché un'interfaccia sincrona e non un evento sul bus (`InternoLancioDado`): `Dado.tira` si chiama da ovunque e anche da thread che
non sono quello principale, e un evento con risposta ritardata o a più ascoltatori renderebbe il risultato dipendente dall'ordine di
iscrizione. Un solo truccatore, interpellato nel punto del lancio, dà sempre una sola risposta. Se servirà anche un'osservazione dei
lanci (un registro per ricostruire una partita) si può far emettere l'evento a `Dado` senza cambiare chi chiama.

## 3. La finestra

Una finestra a parte con questi tab (in colonna a sinistra, con il contenuto allineato a sinistra), nell'ordine, che si rinfrescano da soli ogni mezzo secondo:

- **Un tab per personaggio del gruppo** (si rifanno quando il gruppo cambia): vivo o morto, livello, gli attributi primari, la salute,
  la magia e la stanchezza (le caselle si possono scrivere; la salute e la magia non superano il massimo), +5 punti abilità.
  I valori sono quelli base, senza i modificatori di artefatti e incantesimi.
- **Gruppo**: monete, pietre preziose, pozioni (salute, salute grande, magia, magia grande, aumento di livello), le pergamene di ogni
  classe di incantesimo (si scrive il valore che si vuole, anche più basso) e un pulsante che dà esperienza al gruppo
  (`GruppoGiocatore.addPuntiEsperienza`: a tutti i personaggi vivi e al mondo, come dopo un combattimento).
- **Statistiche**: il livello del mondo (da cui dipendono mostri, loot e leggendari) e i suoi punti esperienza. Cambiarlo porta gli XP a
  quelli che servono per quel livello (`Statistiche.impostaLivelloDelMondo`), altrimenti il livello risalirebbe al primo punto
  guadagnato. Salendo si annuncia come un aumento vero (`NotificaAumentoLivelloMondo`: artefatti, UI); scendendo no, perché chi ascolta
  conosce solo gli aumenti, e cambia solo quello che si genera da lì in poi.
- **Missioni in locanda**, **Missioni in città** e **Missioni nel bosco**: le missioni predefinite che si offrono lì, con la descrizione (se non ce l'hanno, il nome; il nome è nel suggerimento), lo stato (da cominciare,
  partenza forzata, in corso, completata, fallita) e un pulsante "Forza".
- **Dadi**: una riga per contesto, con la casella per accenderlo e il valore voluto.

### Forzare una missione

"Forza" non avvia la missione sul posto: dice alla missione (`MissioneAPassi.forzaPartenza`) che al prossimo controllo in cui il gruppo
è in una locanda (o in una città, per gli incarichi, o in un bosco, per il viandante) il suo **passo iniziale si consideri concluso**,
senza guardare le condizioni che il passo si dà da sé (visita tranquilla, pausa dopo un altro incarico, numero di visite). Le azioni
del passo si eseguono come sempre, quindi gli intermezzi e il resto partono normalmente. La richiesta non si salva e vale una volta
sola. Finché la missione non è partita davvero (cioè attiva) lo stato resta "partenza forzata".
Per sapere dove si offre una missione c'è un elenco fisso in `StrumentiDiSviluppoMotore`: quelle di locanda sono scritte a mano, quelle
del bosco pure (oggi solo `IlViandante`), le altre sono città se sono un `IncaricoInCitta` (o la leggenda dell'armaiolo e la lealtà).
Una missione il cui primo passo non è la condizione che la fa partire davvero, come `IlViandante` (prima fissa i suoi parametri e poi
aspetta il bosco e il giorno), chiede nella condizione del passo giusto `isInLuogoDellaPartenzaForzata()`: da lì, forzata, parte
al primo bosco anche il primo giorno.

Per provare le missioni nuove: **`LaForgia`** (Missioni in città, l'armaiolo): forzarla in una città, poi raccogliere il metallo e il trofeo
(le monete e il livello del mondo si impostano dai tab Gruppo e Statistiche, e il trucco `TIPO_MOSTRO` dei dadi porta il mostro che serve),
tornare in città per la forgiatura, lasciar passare 12 ore e tornare a ritirare l'arma. **`IlViandante`** (Missioni nel bosco): forzarla,
entrare in un bosco, andare nella città indicata (compare sulla mappa), sconfiggere i mostri chiesti e tornare al bosco dell'incontro. Le missioni scritte a mano su `MissioneBase`
(per esempio `NessunBoccaleLasciatoIndietro`) compaiono nell'elenco ma non si possono forzare.

## 4. Come si aggiunge un trucco

1. Un valore in `ContestoLancio` (con `scelta` vero se sceglie un elemento invece di un numero).
2. Nel punto del lancio, `Dado.tira(contesto, …)`, `Dado.tiraAncheAUnaFaccia(contesto, N)` o `Dado.scegli(contesto, elenco)` al posto
   della chiamata di prima; se il trucco sostituisce un oggetto intero (come nel cofano) `Dado.sceltaVoluta(contesto, elenco)`, che
   restituisce vuoto quando non c'è niente da forzare.
3. Una riga in `FinestraDiProva.PannelloDadi` (`opzioni`, e `numero` se il numero non è la posizione).

## 5. I test

`TrucchiDiSviluppoTest` (contratti): la priorità della coda dei test, il trucco permanente, il valore fuori intervallo, la scelta fra
gli ammessi. `StrumentiDiSviluppoMotoreTest` (motore): attributi, vivo, livello, scorte e incantesimi del gruppo, livello del mondo, le missioni divise per luogo, la partenza forzata in una
città e il suo non partire fuori dal luogo giusto, l'elenco degli oggetti speciali. `FinestraDiProvaTest` (app) costruisce la finestra su una partita vera e controlla i tab; si salta dove non c'è uno schermo.

## Osservazioni

- I trucchi sui dadi non cambiano le probabilità del gioco vero: senza modalità di prova `Dado` non ha nessun truccatore.
- Chi cambia un attributo da qui non passa dalle regole (non costa punti abilità, non lancia le notifiche di variazione): serve a
  mettere il personaggio in una situazione, non a simulare il giocatore.
- Il cofano con un leggendario forzato lo costruisce al livello del mondo (`OggettoLeggendario.costruisci`), come una ricompensa.

## Da fare / idee aperte

- Truccare altri lanci: il tiro del 90% dell'incontro, la corruzione e l'amicizia (`LocazioneBase`), i tiri dei combattimenti.
- Forzare anche le missioni a mano su `MissioneBase`, e quelle che si offrono in altri posti (tempio, castello).
- Un registro dei lanci con contesto per ricostruire cosa è successo in una partita.
- Salvare i trucchi accesi fra una sessione e l'altra.
