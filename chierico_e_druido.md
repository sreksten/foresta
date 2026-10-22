# Chierico e druido

Due classi giocanti, ognuna con la forma femminile (`CHIERICO`/`CHIERICA`, `DRUIDO`/`DRUIDA`), e le sei **abilità** innate che le distinguono. Per ora
(la grafica è una copia di quella di guerriero e bardo) **si scelgono solo in modalità di prova**.

## 1. Le classi

| | Chierico / Chierica | Druido / Druida |
| :--- | :--- | :--- |
| Idea | Il sacerdote che combatte in prima linea, fra il Sacerdote e il Guerriero | L'incantatore della natura |
| Partenza dei valori | Copia del Guerriero (`Chierico`, `Chierica`), con valori propri in `Costanti` (`CHIERICO_*`, `CHIERICA_*`) | Copia del Bardo (`Druido`, `Druida`), con valori propri (`DRUIDO_*`, `DRUIDA_*`) |
| Armi | Mazza, spada, scudo, armatura; né bastone né libro; niente doppia arma | Bastone magico, lancia, libro magico; né scudo né armatura (di metallo); niente doppia arma. **Non esistono archi nel gioco**, quindi non li usa |
| Dotazione iniziale | Mazza e scudo | Bastone magico e veste |
| Resistenze (`TipoPersonaggio`) | `NECROTICO` e `MALEDIZIONE` ×0,75 | `ACQUA`, `TERRA`, `VELENO` ×0,75; `FUOCO` ×1,25, `TAGLIENTE` ×1,1 |
| Magia innata | **Alba sacra** e **Luce Divina** come il Sacerdote (`AlbaSacra.conosciutaDa`, `LuceDivina.conosciutaDa`), più deboli solo perché `MAGIA_BASE` (30 contro 45) e `DANNI_MAGICI` (1,0 contro 1,4) sono minori | nessuna dei sacerdoti, né il dardo arcano |
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
| Pelle di corteccia | Druido | A tutto il gruppo, per 4 turni, +30% di `PARATA` e +20% di `RESISTENZA_MAGICA` con due modificatori temporanei (vedi [`modificatori_temporanei.md`](modificatori_temporanei.md)) che l'Alba sacra non toglie; non si raddoppia, si rinfresca | 5 |
| Rigenerazione | Druido | `RIGENERAZIONE` (nuovo effetto di stato benefico) a un alleato per 4 turni: a ogni turno guarisce di (5 + 5% della salute massima) × (1 + `SAGGEZZA`/50), quantità che sta nell'intensità dell'effetto (`PersonaggioBase.applicaDanniDaEffettiDiStato`) | 4 |

I valori stanno in `Costanti` (`ABILITA_*`).

## 3. Solo in modalità di prova

`TipoPersonaggio.isSoloDiProva()` dice quali classi lo sono. `Automa` offre `CHIERICO`/`CHIERICA`/`DRUIDO`/`DRUIDA` fra le classi di inizio partita solo con la modalità di
prova (e rifiuta il comando altrimenti); `LaSfidaDeiCampioni.classiAmichevoli` le esclude dai campioni senza la prova. Nessun mostro e nessun compagno
reclutabile è di queste classi: in prova si possono aggiungere dalla finestra di prova (`FinestraDiProva`), che usa `FabbricaPersonaggi`.

## Osservazioni

- **Immagini.** Le quattro immagini dei personaggi e delle icone di classe sono copie di quelle di Guerriero, Guerriera, Bardo e Cantastorie; le icone delle sei
  abilità (`img/icone/ScacciareINonMorti.gif`, `ImposizioneDelleMani.gif`, `Radici.gif`, `Sciame.gif`, `PelleDiCorteccia.gif`, `Rigenerazione.gif`) sono copie di
  `Pergamena.gif`. Vanno disegnate prima di togliere il vincolo della modalità di prova (vedi [`todo.md`](todo.md)).
- I valori sono un primo giro, mai provati con le simulazioni (`SimulazionePartiteTest`, `TestScontriDiGruppo`).
- Radici e Sciame usano le immunità dell'incantesimo di Terra e di Veleno e registrano l'esito dei colpi come `TERRA` e `VELENO` (`TipoAzione.di`); essendo azioni su
  un gruppo, i requisiti delle tattiche non guardano le immunità scoperte di un bersaglio solo.
- Il Druido è fuori da `CantoDelBardo`: non canta.

## Da fare / idee aperte

- Le immagini vere, poi togliere `isSoloDiProva` e decidere se compaiono come compagni reclutabili o come avversari.
- Provare e ritoccare i numeri (soprattutto la Pelle di corteccia e la Rigenerazione, che durano 4 turni).
- Se la versione "debole" dell'Alba sacra e della Luce Divina del Chierico deve essere più marcata di quella data dalla magia e dai moltiplicatori, servirà un parametro
  negli incantesimi (costo e danno propri del Chierico).
- Le armi del Druido, se un giorno ci saranno gli archi.
