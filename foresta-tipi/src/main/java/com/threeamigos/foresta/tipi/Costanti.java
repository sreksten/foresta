package com.threeamigos.foresta.tipi;

/**
 * Le costanti di bilanciamento del gioco, raccolte in un posto solo.
 * Si possono cambiare qui per variare la difficoltà senza toccare le regole a giro per il codice.
 * <p>
 * Sono divise in sezioni, ciascuna aperta da un commento: amicizia e corruzione, carta forbici e sasso, artefatti, combattimento, e poi, per
 * ogni classe di personaggio, i valori di partenza ({@code GOBLIN_MAX_FORZA}, {@code GOBLIN_SALUTE_BASE}...) e i moltiplicatori con le loro note
 * ({@code GOBLIN_MOLTIPLICATORE_CRITICO}).
 * <p>
 * Le note testuali che spiegano ogni moltiplicatore (perché il goblin ha quel valore di critico) stanno nel file
 * {@code note_personaggi_it.properties} e si leggono con {@link NoteDeiPersonaggi}.
 *
 * @author Stefano Reksten
 */
public class Costanti {

    private Costanti() {
    }

    // Amicizia e corruzione
    // Il tentativo di amicizia riesce se il carisma di chi lo fa è maggiore del tiro di un dado con queste facce: con un
    // carisma di una faccia in più il tentativo riesce sempre
    public static final int FACCE_DEL_DADO_DELL_AMICIZIA = 12;
    // Il tentativo di corruzione, se il gruppo ha le monete per pagare, riesce se il tiro di un dado con queste facce supera
    // la soglia: 7 volte su 10 con questi valori
    public static final int FACCE_DEL_DADO_DELLA_CORRUZIONE = 10;
    public static final int SOGLIA_DEL_DADO_DELLA_CORRUZIONE = 3;

    // Carta, forbici e sasso (vedi carta_forbici_sasso.md)
    // La probabilità, in percentuale, che chi stringe amicizia venga sfidato invece di ricevere un'offerta, finché la
    // missione La sfida dei campioni non è partita
    public static final int PERCENTUALE_PROBABILITA_CARTA_FORBICI_SASSO = 33;
    // Le mani da vincere per vincere la sfida
    public static final int VITTORIE_PER_VINCERE_LA_SFIDA = 3;
    // Le sfide vinte dal gruppo, in tutta la partita, dopo le quali l'avversario lo manda al torneo
    public static final int SFIDE_VINTE_PER_IL_TORNEO = 3;

    // Artefatti
    // Il tetto al numero di effetti di un artefatto secondo la rarità sta in TipoRaritaArtefatto

    // Combattimento (vedi artefatti_e_incantamenti.md, §2 "Combattimento")
    // PARATA intrinseca di scudo, elmo, maschera, armatura e schinieri: una parte fissa, perché servano anche ai
    // livelli bassi, più una per ogni livello del pezzo. La veste dà lo stesso minimo in RESISTENZA_MAGICA.
    public static final int SCUDO_PARATA_MINIMA = 6;
    public static final int SCUDO_PARATA_PER_LIVELLO = 2;
    public static final int ELMO_PARATA_MINIMA = 2;
    public static final int ELMO_PARATA_PER_LIVELLO = 1;
    public static final int MASCHERA_PARATA_MINIMA = 1;
    public static final int MASCHERA_PARATA_PER_LIVELLO = 1;
    public static final int ARMATURA_PARATA_MINIMA = 3;
    public static final int ARMATURA_PARATA_PER_LIVELLO = 1;
    public static final int SCHINIERI_PARATA_MINIMA = 2;
    public static final int SCHINIERI_PARATA_PER_LIVELLO = 1;
    // FORZA minima per indossare un'armatura, per chi la può portare (il Ladro, il Mago e il Sacerdote no: vedi
    // RegoleEquipaggiamento): il Guerriero ce l'ha sempre (17 a livello 1), Elfo e Bardo solo se forzuti ai livelli
    // alti (11-14 a livello 1, fino a 19 a livello 10)
    public static final int ARMATURA_FORZA_MINIMA = 16;
    // RESISTENZA_MAGICA intrinseca dello scudo per ogni suo livello: di più se è raro (o leggendario)
    public static final int SCUDO_RESISTENZA_MAGICA_PER_LIVELLO = 1;
    public static final int SCUDO_RARO_RESISTENZA_MAGICA_PER_LIVELLO = 2;
    // "Guardia aperta": chi impugna due armi o un'arma a due mani perde un quarto della PARATA
    public static final double GUARDIA_APERTA_FATTORE_PARATA = 0.75;
    // La seconda fase di attacco, con l'arma nella mano secondaria, fa il 40% (danno base e incantamenti)
    public static final double DOPPIA_ARMA_FATTORE_SECONDA_ARMA = 0.4;
    // Su elmo, scudo, armatura e schinieri la parte percentuale di un incantamento vale la metà, perché moltiplica
    // una difesa che cresce già con il livello
    public static final double RESISTENZA_FATTORE_PERCENTUALE = 0.5;
    // Il libro magico dà al danno degli incantesimi un bonus come un incantamento del suo grado, più il 25%
    public static final double LIBRO_MAGICO_MAGGIORAZIONE = 0.25;
    // L'elfo sa usare il libro magico, ma non bene come il mago: del bonus agli incantesimi gliene resta questa quota
    public static final double LIBRO_MAGICO_FATTORE_ELFO = 0.5;

    // Un'arma fino a tanti livelli sotto quello di chi la impugna rende ancora tutta la parte dell'eroe; oltre, la parte
    // dell'eroe cala in proporzione (vedi CalcolatoreCombattimento.calcolaDannoRisultante)
    public static final int TOLLERANZA_LIVELLO_ARMA = 2;

    // Inizio morbido (vedi LocazioneBase.crea): finché il capo del gruppo è a questo livello o sotto, si incontrano al
    // massimo tanti mostri quanti i personaggi del gruppo, e mai uno dei mostri di MOSTRI_ESCLUSI_A_INIZIO_PARTITA
    public static final int INIZIO_MORBIDO_FINO_AL_LIVELLO = 3;

    // L'ignominia della fuga (vedi PersonaggioBase.fugge): coraggio, valore e carisma calano di questa percentuale
    // per questo numero di locazioni dopo quella della fuga
    // Fuggire costa a ognuno una quota della sua salute massima, tirata fra queste due percentuali: la fuga non uccide, lascia almeno un punto
    // (a fine locazione ognuno recupera dal 16 al 35% della salute massima, secondo la classe: per questo la fuga deve costare di più)
    public static final int FUGA_DANNO_MINIMO_PERCENTUALE = 30;
    public static final int FUGA_DANNO_MASSIMO_PERCENTUALE = 50;
    // Il gruppo che fugge perde questa percentuale di ognuna delle sue scorte: monete, preziosi, pergamene e pozioni (non gli artefatti)
    public static final int FUGA_PERDITA_CONSUMABILI_PERCENTUALE = 20;
    public static final int FUGA_IGNOMINIA_PERCENTUALE = 20;
    public static final int FUGA_IGNOMINIA_LOCAZIONI = 3;
    public static final String FUGA_IGNOMINIA_NOTA = "Ignominia della fuga";

    // A fine locazione ogni personaggio vivo recupera getRigenerazioneSalute(): 5 punti più questa quota della salute
    // massima, aumentati dalla COSTITUZIONE (circa il 25% della salute massima a inizio partita)
    public static final double RIGENERAZIONE_SALUTE_QUOTA_SALUTE_MASSIMA = 0.15;
    // Allo stesso modo ognuno recupera getRigenerazioneMagia(): 5 punti più questa quota della magia massima, per il
    // moltiplicatore di recupero magico della classe (a inizio partita circa 12 al Mago, 9 al Bardo, 3 al Guerriero)
    public static final double RIGENERAZIONE_MAGIA_QUOTA_MAGIA_MASSIMA = 0.05;

    // Contrattazione (vedi RegoleContrattazione): lo sconto sugli acquisti e il ricavo delle vendite crescono con
    // bonus = contrattazione / (contrattazione + saturazione), fino a un limite che nessun modificatore supera.
    // Con la vendita al massimo al 75% e l'acquisto almeno all'80% non si guadagna comprando e rivendendo.
    public static final int CONTRATTAZIONE_SATURAZIONE = 4;
    public static final double CONTRATTAZIONE_COEFFICIENTE_SCONTO = 0.24;
    public static final double CONTRATTAZIONE_SCONTO_MASSIMO = 0.20;
    public static final double CONTRATTAZIONE_QUOTA_VENDITA_BASE = 0.50;
    public static final double CONTRATTAZIONE_COEFFICIENTE_VENDITA = 0.30;
    public static final double CONTRATTAZIONE_QUOTA_VENDITA_MASSIMA = 0.75;

    // Esiti rari di ogni cofano aperto: 10% una pergamena, 10% un artefatto casuale, 3% un oggetto che fa salire di livello un
    // artefatto (vedi TipoArtefatto.AGGIORNAMENTO), 0,5% un libro tecnico non ancora trovato (vedi leggendari.txt): 23,5% in tutto
    public static final double COFANO_PROBABILITA_PERGAMENA = 0.10;
    public static final double COFANO_PROBABILITA_ARTEFATTO = 0.10;
    public static final double COFANO_PROBABILITA_AGGIORNAMENTO = 0.03;
    public static final double COFANO_PROBABILITA_LIBRO_TECNICO = 0.005;
    // Magazzini dei negozi di città, riforniti alla creazione del mondo e a ogni aumento del suo livello:
    // quanti artefatti per negozio, con livelli a rotazione fra livello del mondo - MAGAZZINO_DIVARIO_LIVELLO
    // e livello del mondo + MAGAZZINO_DIVARIO_LIVELLO (mai sotto 1)
    public static final int MAGAZZINO_ARTEFATTI_ARMAIOLO = 6;
    public static final int MAGAZZINO_PERGAMENE = 6;
    public static final int MAGAZZINO_DIVARIO_LIVELLO = 1;
    // A ogni aumento del livello del mondo l'armaiolo scarta gli artefatti di livello inferiore al livello
    // del mondo meno questo scarto
    public static final int MAGAZZINO_SCARTO_SOTTO_LIVELLO = 2;

    // Pozioni
    // Le pozioni di salute con cui il gruppo parte
    /**
     * L'astuzia di un personaggio per le tattiche (tattiche_di_combattimento_automatiche.md §4): l'intelligenza massima della sua classe per
     * una rampa sul livello, che parte da questa frazione al livello 1 e arriva al pieno al livello 9
     */
    public static final double ASTUZIA_PARTENZA = 0.4d;
    /**
     * Quanto sale la frazione dell'astuzia a ogni livello
     */
    public static final double ASTUZIA_PER_LIVELLO = 0.075d;

    /**
     * Il livello massimo di un personaggio e del mondo: oltre, i punti esperienza non fanno più salire di livello (vedi GestoreProgressione)
     */
    public static final int MASSIMO_LIVELLO = 50;

    // Con cosa comincia una partita: le monete bastano a un'arma nuova per tutti dopo i primi pasti (vedi economia.md)
    public static final int MONETE_INIZIALI = 150;
    public static final int PREZIOSI_INIZIALI = 5;
    public static final int POZIONI_SALUTE_INIZIALI = 2;

    public static final int RECUPERO_DA_POZIONE_SALUTE = 100;
    public static final int RECUPERO_DA_POZIONE_SALUTE_GRANDE = 150;
    public static final int AUMENTO_SALUTE_DA_POZIONE_SALUTE_GRANDE = 10;
    public static final int RECUPERO_DA_POZIONE_MAGIA = 10;
    public static final int AUMENTO_MAGIA_DA_POZIONE_MAGIA_GRANDE = 10;

    // Il gruppo avversario porta con sé pergamene (vedi BottinoDegliAvversari): la probabilità, per ogni avversario, di averne una
    public static final int PROBABILITA_PERGAMENA_PER_AVVERSARIO = 20;
    // Quante volte, al massimo, un personaggio cambia arma in un combattimento (vedi tattiche_di_combattimento_automatiche.md §5)
    public static final int MAX_CAMBI_ARMA_PER_COMBATTIMENTO = 2;
    /**
     * Dopo quanti tentativi falliti per caso (un Silenzio mancato) una tattica smette di provare la stessa azione su quel bersaglio
     * (tattiche_di_combattimento_automatiche.md §11.7)
     */
    public static final int MAX_TENTATIVI_FALLITI_PER_AZIONE = 2;
    // ...e, se è un umanoide, la probabilità di una pozione della Salute
    public static final int PROBABILITA_POZIONE_SALUTE_PER_AVVERSARIO_UMANOIDE = 20;
    public static final int RECUPERO_DA_POZIONE_MAGIA_GRANDE = 20;

    // Oggetti
    public static final int ANELLO_MAGICO_AGGIUNTA_VALORE = 5;
    public static final int ANELLO_MAGICO_AGGIUNTA_CORAGGIO = 5;
    public static final int ANELLO_MAGICO_AGGIUNTA_CARISMA = 1;
    public static final int ANELLO_MAGICO_PUNTEGGIO = 50;

    // Gruppo
    public static final int MAX_PERSONAGGI_GRUPPO_GIOCATORE = 5;
    // Oltre ai 5 permanenti, fino a 3 slot aggiuntivi per personaggi temporanei
    // (AiutoGratuito, AiutoMercenario, lo Scheletro da MIETITURA)
    public static final int MAX_PERSONAGGI_GRUPPO_TOTALE = 8;
    // Il necromante, finché è vivo, evoca uno scheletro ogni volta che ne cadono due; al più tante evocazioni per scontro
    public static final int NECROMANTE_SCHELETRI_PER_EVOCAZIONE = 2;
    public static final int NECROMANTE_EVOCAZIONI_MASSIME = 4;
    // Per quanti turni uno Scheletro nato dalla MIETITURA accompagna il gruppo
    public static final int MIETITURA_DURATA_TURNI = 4;

    // Combattimento
    // La quota del danno grezzo di un colpo che una reazione ad area (esplosione di gas, rigetto, elettrocuzione) porta ai
    // vicini del bersaglio, prima della loro difesa
    public static final double DANNO_AD_AREA_QUOTA = 0.5d;

    // Dall'alchimista
    public static final int COSTO_POZIONE_SALUTE = 5;
    public static final int COSTO_POZIONE_SALUTE_GRANDE = 15;
    public static final int COSTO_POZIONE_MAGIA = 10;
    public static final int COSTO_POZIONE_MAGIA_GRANDE = 30;
    public static final int COSTO_AUMENTO_MAGIA_GIOCATORE_SINGOLO = 10;

    // In locanda
    public static final int COSTO_PASTO = 5;
    // Quante informazioni si possono ottenere in una stessa locanda, in tutta la partita
    public static final int LOCANDA_MASSIMO_INFORMAZIONI = 3;
    public static final int COSTO_PERNOTTAMENTO = 5;
    public static final int RECUPERO_SALUTE_DA_PASTO = 100;

    // Castelli
    public static final int COFANI_IN_CASTELLO_IDRA = 5;
    public static final int COFANI_IN_CASTELLO_LICH = 5;
    public static final int COFANI_IN_CASTELLO_MINOTAURO = 5;
    public static final int COFANI_IN_CASTELLO_STREGA = 5;

    // La forgia (vedi LaForgia): quante monete costa far forgiare l'arma per ogni livello del mondo, quante ore passano a forgiarla
    // (il gruppo le passa sul posto) e quante ore di gioco devono passare perché la lama si raffreddi, e di quanto il suo danno
    // supera quello di un'arma comune del suo livello (tarate il 2026-10-21: vedi economia.md, "La forgia")
    public static final int FORGIA_MONETE_PER_LIVELLO = 10;
    public static final int FORGIA_ORE_DI_LAVORO = 6;
    public static final int FORGIA_ORE_DI_ATTESA = 12;
    public static final double FORGIA_MOLTIPLICATORE_DANNI = 1.4d;

    // Foresta
    public static final int COSTO_MAPPA_DELLA_ZONA = 10;
    public static final int COSTO_MAPPA_DELLA_FORESTA = 20;

    // Notizie e messaggi ricordati: vedi NotizieMD.MASSIMO_MESSAGGI_RICORDATI e MASSIMO_NOTIZIE_RICORDATE

    // Intermezzi (vedi intermezzi.Intermezzo): secondi dopo cui una pagina avanza da sola;
    // con 0 avanza solo al click sulla pergamena
    public static final int SECONDI_PER_PAGINA_INTERMEZZO = 8;
    // Quanto dura la rivelazione di un artefatto trovato, uscita compresa (vedi SpriteRivelazioneArtefatto e PaginaIntermezzo.conRivelazioneDiArtefatto)
    public static final float SECONDI_RIVELAZIONE_ARTEFATTO = 5.0f;

    // Personaggio
    public static final int MAX_STANCHEZZA = 9;

    // Statistiche dei vari tipi di personaggio

    // Personaggi giocabili

    public static final int BARDO_MAX_CARISMA = 8;
    public static final int BARDO_MAX_FORZA = 30;
    public static final int BARDO_MAX_DESTREZZA = 35;
    public static final int BARDO_MAX_COSTITUZIONE = 35;
    public static final int BARDO_MAX_INTELLIGENZA = 35;
    public static final int BARDO_MAX_SAGGEZZA = 35;
    public static final int BARDO_MAX_FORTUNA = 15;
    public static final int BARDO_MAX_NUMERO = 1;
    public static final int BARDO_PUNTI_ESPERIENZA = 25;

    public static final int BARDO_SALUTE_BASE = 105;
    public static final int BARDO_MAGIA_BASE = 40;
    public static final int BARDO_LIVELLAMENTO_MAGIA = 18;
    public static final double BARDO_MOLTIPLICATORE_CARICO = 0.9;
    public static final double BARDO_MOLTIPLICATORE_CRITICO = 1.1;
    public static final double BARDO_MOLTIPLICATORE_PRECISIONE = 1.0;
    public static final double BARDO_MOLTIPLICATORE_VELOCITA = 1.1;
    public static final double BARDO_MOLTIPLICATORE_FURTIVITA = 1.1;
    public static final double BARDO_MOLTIPLICATORE_PARATA = 1.0;
    public static final double BARDO_MOLTIPLICATORE_RESISTENZA_MAGICA = 1.1;
    public static final double BARDO_MOLTIPLICATORE_PERCEZIONE = 1.1;
    public static final double BARDO_MOLTIPLICATORE_SOGGEZIONE = 1.2;
    public static final double BARDO_MOLTIPLICATORE_FURIA = 1.1;
    public static final double BARDO_MOLTIPLICATORE_CORAGGIO = 1.1;
    public static final double BARDO_MOLTIPLICATORE_VALORE = 1.1;
    public static final double BARDO_MOLTIPLICATORE_CONTRATTAZIONE = 1.2;
    public static final double BARDO_MOLTIPLICATORE_NUMERO_BERSAGLI = 1.1;
    public static final double BARDO_MOLTIPLICATORE_STANCHEZZA = 1.0;
    public static final double BARDO_MOLTIPLICATORE_DANNI_MAGICI = 1.1;
    public static final double BARDO_MOLTIPLICATORE_DANNI_FISICI = 0.9;
    public static final double BARDO_MOLTIPLICATORE_RECUPERO_FISICO = 1.1;
    public static final double BARDO_MOLTIPLICATORE_RECUPERO_MAGICO = 1.2;

    public static final int CANTASTORIE_MAX_CARISMA = 8;
    public static final int CANTASTORIE_MAX_FORZA = 30;
    public static final int CANTASTORIE_MAX_DESTREZZA = 35;
    public static final int CANTASTORIE_MAX_COSTITUZIONE = 35;
    public static final int CANTASTORIE_MAX_INTELLIGENZA = 35;
    public static final int CANTASTORIE_MAX_SAGGEZZA = 35;
    public static final int CANTASTORIE_MAX_FORTUNA = 15;
    public static final int CANTASTORIE_MAX_NUMERO = 1;
    public static final int CANTASTORIE_PUNTI_ESPERIENZA = 25;

    public static final int CANTASTORIE_SALUTE_BASE = 105;
    public static final int CANTASTORIE_MAGIA_BASE = 40;
    public static final int CANTASTORIE_LIVELLAMENTO_MAGIA = 18;
    public static final double CANTASTORIE_MOLTIPLICATORE_CARICO = 0.85;
    public static final double CANTASTORIE_MOLTIPLICATORE_CRITICO = 1.15;
    public static final double CANTASTORIE_MOLTIPLICATORE_PRECISIONE = 1.0;
    public static final double CANTASTORIE_MOLTIPLICATORE_VELOCITA = 1.1;
    public static final double CANTASTORIE_MOLTIPLICATORE_FURTIVITA = 1.1;
    public static final double CANTASTORIE_MOLTIPLICATORE_PARATA = 1.0;
    public static final double CANTASTORIE_MOLTIPLICATORE_RESISTENZA_MAGICA = 1.1;
    public static final double CANTASTORIE_MOLTIPLICATORE_PERCEZIONE = 1.1;
    public static final double CANTASTORIE_MOLTIPLICATORE_SOGGEZIONE = 1.2;
    public static final double CANTASTORIE_MOLTIPLICATORE_FURIA = 1.1;
    public static final double CANTASTORIE_MOLTIPLICATORE_CORAGGIO = 1.1;
    public static final double CANTASTORIE_MOLTIPLICATORE_VALORE = 1.1;
    public static final double CANTASTORIE_MOLTIPLICATORE_CONTRATTAZIONE = 1.2;
    public static final double CANTASTORIE_MOLTIPLICATORE_NUMERO_BERSAGLI = 1.1;
    public static final double CANTASTORIE_MOLTIPLICATORE_STANCHEZZA = 1.0;
    public static final double CANTASTORIE_MOLTIPLICATORE_DANNI_MAGICI = 1.1;
    public static final double CANTASTORIE_MOLTIPLICATORE_DANNI_FISICI = 0.9;
    public static final double CANTASTORIE_MOLTIPLICATORE_RECUPERO_FISICO = 1.1;
    public static final double CANTASTORIE_MOLTIPLICATORE_RECUPERO_MAGICO = 1.2;

    public static final int ELFA_MAX_CARISMA = 7;
    public static final int ELFA_MAX_FORZA = 25;
    public static final int ELFA_MAX_DESTREZZA = 40;
    public static final int ELFA_MAX_COSTITUZIONE = 30;
    public static final int ELFA_MAX_INTELLIGENZA = 45;
    public static final int ELFA_MAX_SAGGEZZA = 35;
    public static final int ELFA_MAX_FORTUNA = 12;
    public static final int ELFA_MAX_NUMERO = 1;
    public static final int ELFA_PUNTI_ESPERIENZA = 25;

    public static final int ELFA_SALUTE_BASE = 105;
    public static final int ELFA_MAGIA_BASE = 45;
    public static final int ELFA_LIVELLAMENTO_MAGIA = 22;
    public static final double ELFA_MOLTIPLICATORE_CARICO = 0.85;
    public static final double ELFA_MOLTIPLICATORE_CRITICO = 1.2;
    public static final double ELFA_MOLTIPLICATORE_PRECISIONE = 1.3;
    public static final double ELFA_MOLTIPLICATORE_VELOCITA = 1.2;
    public static final double ELFA_MOLTIPLICATORE_FURTIVITA = 1.3;
    public static final double ELFA_MOLTIPLICATORE_PARATA = 1.1;
    public static final double ELFA_MOLTIPLICATORE_RESISTENZA_MAGICA = 1.4;
    public static final double ELFA_MOLTIPLICATORE_PERCEZIONE = 1.4;
    public static final double ELFA_MOLTIPLICATORE_SOGGEZIONE = 1.1;
    public static final double ELFA_MOLTIPLICATORE_FURIA = 1.2;
    public static final double ELFA_MOLTIPLICATORE_CORAGGIO = 1.1;
    public static final double ELFA_MOLTIPLICATORE_VALORE = 1.2;
    public static final double ELFA_MOLTIPLICATORE_CONTRATTAZIONE = 1.0;
    public static final double ELFA_MOLTIPLICATORE_NUMERO_BERSAGLI = 1.0;
    public static final double ELFA_MOLTIPLICATORE_STANCHEZZA = 0.8;
    public static final double ELFA_MOLTIPLICATORE_DANNI_MAGICI = 1.4;
    public static final double ELFA_MOLTIPLICATORE_DANNI_FISICI = 0.9;
    public static final double ELFA_MOLTIPLICATORE_RECUPERO_FISICO = 1.1;
    public static final double ELFA_MOLTIPLICATORE_RECUPERO_MAGICO = 1.3;

    public static final int ELFO_MAX_CARISMA = 7;
    public static final int ELFO_MAX_FORZA = 25;
    public static final int ELFO_MAX_DESTREZZA = 40;
    public static final int ELFO_MAX_COSTITUZIONE = 30;
    public static final int ELFO_MAX_INTELLIGENZA = 45;
    public static final int ELFO_MAX_SAGGEZZA = 35;
    public static final int ELFO_MAX_FORTUNA = 12;
    public static final int ELFO_MAX_NUMERO = 1;
    public static final int ELFO_PUNTI_ESPERIENZA = 25;

    public static final int ELFO_SALUTE_BASE = 105;
    public static final int ELFO_MAGIA_BASE = 45;
    public static final int ELFO_LIVELLAMENTO_MAGIA = 22;
    public static final double ELFO_MOLTIPLICATORE_CARICO = 0.9;
    public static final double ELFO_MOLTIPLICATORE_CRITICO = 1.2;
    public static final double ELFO_MOLTIPLICATORE_PRECISIONE = 1.3;
    public static final double ELFO_MOLTIPLICATORE_VELOCITA = 1.2;
    public static final double ELFO_MOLTIPLICATORE_FURTIVITA = 1.3;
    public static final double ELFO_MOLTIPLICATORE_PARATA = 1.1;
    public static final double ELFO_MOLTIPLICATORE_RESISTENZA_MAGICA = 1.4;
    public static final double ELFO_MOLTIPLICATORE_PERCEZIONE = 1.4;
    public static final double ELFO_MOLTIPLICATORE_SOGGEZIONE = 1.1;
    public static final double ELFO_MOLTIPLICATORE_FURIA = 1.2;
    public static final double ELFO_MOLTIPLICATORE_CORAGGIO = 1.1;
    public static final double ELFO_MOLTIPLICATORE_VALORE = 1.2;
    public static final double ELFO_MOLTIPLICATORE_CONTRATTAZIONE = 1.0;
    public static final double ELFO_MOLTIPLICATORE_NUMERO_BERSAGLI = 1.0;
    public static final double ELFO_MOLTIPLICATORE_STANCHEZZA = 0.8;
    public static final double ELFO_MOLTIPLICATORE_DANNI_MAGICI = 1.4;
    public static final double ELFO_MOLTIPLICATORE_DANNI_FISICI = 0.9;
    public static final double ELFO_MOLTIPLICATORE_RECUPERO_FISICO = 1.1;
    public static final double ELFO_MOLTIPLICATORE_RECUPERO_MAGICO = 1.3;

    public static final int GUERRIERA_MAX_CARISMA = 5;
    public static final int GUERRIERA_MAX_FORZA = 50;
    public static final int GUERRIERA_MAX_DESTREZZA = 25;
    public static final int GUERRIERA_MAX_COSTITUZIONE = 45;
    public static final int GUERRIERA_MAX_INTELLIGENZA = 15;
    public static final int GUERRIERA_MAX_SAGGEZZA = 20;
    public static final int GUERRIERA_MAX_FORTUNA = 10;
    public static final int GUERRIERA_MAX_NUMERO = 1;
    public static final int GUERRIERA_PUNTI_ESPERIENZA = 25;

    public static final int GUERRIERA_SALUTE_BASE = 150;
    public static final int GUERRIERA_MAGIA_BASE = 20;
    public static final int GUERRIERA_LIVELLAMENTO_MAGIA = 6;
    public static final double GUERRIERA_MOLTIPLICATORE_CARICO = 1.2;
    public static final double GUERRIERA_MOLTIPLICATORE_CRITICO = 1.0;
    public static final double GUERRIERA_MOLTIPLICATORE_PRECISIONE = 1.1;
    public static final double GUERRIERA_MOLTIPLICATORE_VELOCITA = 1.0;
    public static final double GUERRIERA_MOLTIPLICATORE_FURTIVITA = 1.0;
    public static final double GUERRIERA_MOLTIPLICATORE_PARATA = 1.5;
    public static final double GUERRIERA_MOLTIPLICATORE_RESISTENZA_MAGICA = 1.0;
    public static final double GUERRIERA_MOLTIPLICATORE_PERCEZIONE = 1.0;
    public static final double GUERRIERA_MOLTIPLICATORE_SOGGEZIONE = 1.0;
    public static final double GUERRIERA_MOLTIPLICATORE_FURIA = 1.5;
    public static final double GUERRIERA_MOLTIPLICATORE_CORAGGIO = 1.3;
    public static final double GUERRIERA_MOLTIPLICATORE_VALORE = 1.5;
    public static final double GUERRIERA_MOLTIPLICATORE_CONTRATTAZIONE = 0.9;
    public static final double GUERRIERA_MOLTIPLICATORE_NUMERO_BERSAGLI = 1.1;
    public static final double GUERRIERA_MOLTIPLICATORE_STANCHEZZA = 1.0;
    public static final double GUERRIERA_MOLTIPLICATORE_DANNI_MAGICI = 0.5;
    public static final double GUERRIERA_MOLTIPLICATORE_DANNI_FISICI = 1.3;
    public static final double GUERRIERA_MOLTIPLICATORE_RECUPERO_FISICO = 1.4;
    public static final double GUERRIERA_MOLTIPLICATORE_RECUPERO_MAGICO = 0.5;

    public static final int GUERRIERO_MAX_CARISMA = 5;
    public static final int GUERRIERO_MAX_FORZA = 50;
    public static final int GUERRIERO_MAX_DESTREZZA = 25;
    public static final int GUERRIERO_MAX_COSTITUZIONE = 45;
    public static final int GUERRIERO_MAX_INTELLIGENZA = 15;
    public static final int GUERRIERO_MAX_SAGGEZZA = 20;
    public static final int GUERRIERO_MAX_FORTUNA = 10;
    public static final int GUERRIERO_MAX_NUMERO = 1;
    public static final int GUERRIERO_PUNTI_ESPERIENZA = 25;

    public static final int GUERRIERO_SALUTE_BASE = 150;
    public static final int GUERRIERO_MAGIA_BASE = 20;
    public static final int GUERRIERO_LIVELLAMENTO_MAGIA = 6;
    public static final double GUERRIERO_MOLTIPLICATORE_CARICO = 1.25;
    public static final double GUERRIERO_MOLTIPLICATORE_CRITICO = 1.0;
    public static final double GUERRIERO_MOLTIPLICATORE_PRECISIONE = 1.1;
    public static final double GUERRIERO_MOLTIPLICATORE_VELOCITA = 1.0;
    public static final double GUERRIERO_MOLTIPLICATORE_FURTIVITA = 1.0;
    public static final double GUERRIERO_MOLTIPLICATORE_PARATA = 1.5;
    public static final double GUERRIERO_MOLTIPLICATORE_RESISTENZA_MAGICA = 1.0;
    public static final double GUERRIERO_MOLTIPLICATORE_PERCEZIONE = 1.0;
    public static final double GUERRIERO_MOLTIPLICATORE_SOGGEZIONE = 1.0;
    public static final double GUERRIERO_MOLTIPLICATORE_FURIA = 1.5;
    public static final double GUERRIERO_MOLTIPLICATORE_CORAGGIO = 1.3;
    public static final double GUERRIERO_MOLTIPLICATORE_VALORE = 1.5;
    public static final double GUERRIERO_MOLTIPLICATORE_CONTRATTAZIONE = 0.9;
    public static final double GUERRIERO_MOLTIPLICATORE_NUMERO_BERSAGLI = 1.1;
    public static final double GUERRIERO_MOLTIPLICATORE_STANCHEZZA = 1.0;
    public static final double GUERRIERO_MOLTIPLICATORE_DANNI_MAGICI = 0.5;
    public static final double GUERRIERO_MOLTIPLICATORE_DANNI_FISICI = 1.3;
    public static final double GUERRIERO_MOLTIPLICATORE_RECUPERO_FISICO = 1.4;
    public static final double GUERRIERO_MOLTIPLICATORE_RECUPERO_MAGICO = 0.5;

    public static final int LADRA_MAX_CARISMA = 6;
    public static final int LADRA_MAX_FORZA = 30;
    public static final int LADRA_MAX_DESTREZZA = 50;
    public static final int LADRA_MAX_COSTITUZIONE = 30;
    public static final int LADRA_MAX_INTELLIGENZA = 30;
    public static final int LADRA_MAX_SAGGEZZA = 25;
    public static final int LADRA_MAX_FORTUNA = 25;
    public static final int LADRA_MAX_NUMERO = 1;
    public static final int LADRA_PUNTI_ESPERIENZA = 25;

    public static final int LADRA_SALUTE_BASE = 100;
    public static final int LADRA_MAGIA_BASE = 25;
    public static final int LADRA_LIVELLAMENTO_MAGIA = 8;
    public static final double LADRA_MOLTIPLICATORE_CARICO = 0.95;
    public static final double LADRA_MOLTIPLICATORE_CRITICO = 1.5;
    public static final double LADRA_MOLTIPLICATORE_PRECISIONE = 1.3;
    public static final double LADRA_MOLTIPLICATORE_VELOCITA = 1.4;
    public static final double LADRA_MOLTIPLICATORE_FURTIVITA = 1.5;
    public static final double LADRA_MOLTIPLICATORE_PARATA = 1.0;
    public static final double LADRA_MOLTIPLICATORE_RESISTENZA_MAGICA = 1.3;
    public static final double LADRA_MOLTIPLICATORE_PERCEZIONE = 1.3;
    public static final double LADRA_MOLTIPLICATORE_SOGGEZIONE = 0.9;
    public static final double LADRA_MOLTIPLICATORE_FURIA = 1.0;
    public static final double LADRA_MOLTIPLICATORE_CORAGGIO = 1.0;
    public static final double LADRA_MOLTIPLICATORE_VALORE = 1.0;
    public static final double LADRA_MOLTIPLICATORE_CONTRATTAZIONE = 1.3;
    public static final double LADRA_MOLTIPLICATORE_NUMERO_BERSAGLI = 0.9;
    public static final double LADRA_MOLTIPLICATORE_STANCHEZZA = 0.9;
    public static final double LADRA_MOLTIPLICATORE_DANNI_MAGICI = 1.0;
    public static final double LADRA_MOLTIPLICATORE_DANNI_FISICI = 1.0;
    public static final double LADRA_MOLTIPLICATORE_RECUPERO_FISICO = 1.1;
    public static final double LADRA_MOLTIPLICATORE_RECUPERO_MAGICO = 0.9;

    public static final int LADRO_MAX_CARISMA = 6;
    public static final int LADRO_MAX_FORZA = 30;
    public static final int LADRO_MAX_DESTREZZA = 50;
    public static final int LADRO_MAX_COSTITUZIONE = 30;
    public static final int LADRO_MAX_INTELLIGENZA = 30;
    public static final int LADRO_MAX_SAGGEZZA = 25;
    public static final int LADRO_MAX_FORTUNA = 25;
    public static final int LADRO_MAX_NUMERO = 1;
    public static final int LADRO_PUNTI_ESPERIENZA = 25;

    public static final int LADRO_SALUTE_BASE = 100;
    public static final int LADRO_MAGIA_BASE = 25;
    public static final int LADRO_LIVELLAMENTO_MAGIA = 8;
    public static final double LADRO_MOLTIPLICATORE_CARICO = 1.0;
    public static final double LADRO_MOLTIPLICATORE_CRITICO = 1.5;
    public static final double LADRO_MOLTIPLICATORE_PRECISIONE = 1.3;
    public static final double LADRO_MOLTIPLICATORE_VELOCITA = 1.4;
    public static final double LADRO_MOLTIPLICATORE_FURTIVITA = 1.5;
    public static final double LADRO_MOLTIPLICATORE_PARATA = 1.0;
    public static final double LADRO_MOLTIPLICATORE_RESISTENZA_MAGICA = 1.3;
    public static final double LADRO_MOLTIPLICATORE_PERCEZIONE = 1.3;
    public static final double LADRO_MOLTIPLICATORE_SOGGEZIONE = 0.9;
    public static final double LADRO_MOLTIPLICATORE_FURIA = 1.0;
    public static final double LADRO_MOLTIPLICATORE_CORAGGIO = 1.0;
    public static final double LADRO_MOLTIPLICATORE_VALORE = 1.0;
    public static final double LADRO_MOLTIPLICATORE_CONTRATTAZIONE = 1.3;
    public static final double LADRO_MOLTIPLICATORE_NUMERO_BERSAGLI = 0.9;
    public static final double LADRO_MOLTIPLICATORE_STANCHEZZA = 0.9;
    public static final double LADRO_MOLTIPLICATORE_DANNI_MAGICI = 1.0;
    public static final double LADRO_MOLTIPLICATORE_DANNI_FISICI = 1.0;
    public static final double LADRO_MOLTIPLICATORE_RECUPERO_FISICO = 1.1;
    public static final double LADRO_MOLTIPLICATORE_RECUPERO_MAGICO = 0.9;

    public static final int MAGA_MAX_CARISMA = 6;
    public static final int MAGA_MAX_FORZA = 15;
    public static final int MAGA_MAX_DESTREZZA = 20;
    public static final int MAGA_MAX_COSTITUZIONE = 20;
    public static final int MAGA_MAX_INTELLIGENZA = 55;
    public static final int MAGA_MAX_SAGGEZZA = 40;
    public static final int MAGA_MAX_FORTUNA = 10;
    public static final int MAGA_MAX_NUMERO = 1;
    public static final int MAGA_PUNTI_ESPERIENZA = 25;

    public static final int MAGA_SALUTE_BASE = 80;
    public static final int MAGA_MAGIA_BASE = 60;
    public static final int MAGA_LIVELLAMENTO_MAGIA = 34;
    public static final double MAGA_MOLTIPLICATORE_CARICO = 0.75;
    public static final double MAGA_MOLTIPLICATORE_CRITICO = 0.8;
    public static final double MAGA_MOLTIPLICATORE_PRECISIONE = 1.0;
    public static final double MAGA_MOLTIPLICATORE_VELOCITA = 0.9;
    public static final double MAGA_MOLTIPLICATORE_FURTIVITA = 0.9;
    public static final double MAGA_MOLTIPLICATORE_PARATA = 0.6;
    public static final double MAGA_MOLTIPLICATORE_RESISTENZA_MAGICA = 1.1;
    public static final double MAGA_MOLTIPLICATORE_PERCEZIONE = 1.1;
    public static final double MAGA_MOLTIPLICATORE_SOGGEZIONE = 1.0;
    public static final double MAGA_MOLTIPLICATORE_FURIA = 1.0;
    public static final double MAGA_MOLTIPLICATORE_CORAGGIO = 1.0;
    public static final double MAGA_MOLTIPLICATORE_VALORE = 1.0;
    public static final double MAGA_MOLTIPLICATORE_CONTRATTAZIONE = 1.0;
    public static final double MAGA_MOLTIPLICATORE_NUMERO_BERSAGLI = 1.3;
    public static final double MAGA_MOLTIPLICATORE_STANCHEZZA = 1.4;
    public static final double MAGA_MOLTIPLICATORE_DANNI_MAGICI = 2.0;
    public static final double MAGA_MOLTIPLICATORE_DANNI_FISICI = 0.5;
    public static final double MAGA_MOLTIPLICATORE_RECUPERO_FISICO = 0.7;
    public static final double MAGA_MOLTIPLICATORE_RECUPERO_MAGICO = 1.5;

    public static final int MAGO_MAX_CARISMA = 6;
    public static final int MAGO_MAX_FORZA = 15;
    public static final int MAGO_MAX_DESTREZZA = 20;
    public static final int MAGO_MAX_COSTITUZIONE = 20;
    public static final int MAGO_MAX_INTELLIGENZA = 55;
    public static final int MAGO_MAX_SAGGEZZA = 40;
    public static final int MAGO_MAX_FORTUNA = 10;
    public static final int MAGO_MAX_NUMERO = 1;
    public static final int MAGO_PUNTI_ESPERIENZA = 25;

    public static final int MAGO_SALUTE_BASE = 80;
    public static final int MAGO_MAGIA_BASE = 60;
    public static final int MAGO_LIVELLAMENTO_MAGIA = 34;
    public static final double MAGO_MOLTIPLICATORE_CARICO = 0.8;
    public static final double MAGO_MOLTIPLICATORE_CRITICO = 0.8;
    public static final double MAGO_MOLTIPLICATORE_PRECISIONE = 1.0;
    public static final double MAGO_MOLTIPLICATORE_VELOCITA = 0.9;
    public static final double MAGO_MOLTIPLICATORE_FURTIVITA = 0.9;
    public static final double MAGO_MOLTIPLICATORE_PARATA = 0.6;
    public static final double MAGO_MOLTIPLICATORE_RESISTENZA_MAGICA = 1.1;
    public static final double MAGO_MOLTIPLICATORE_PERCEZIONE = 1.1;
    public static final double MAGO_MOLTIPLICATORE_SOGGEZIONE = 1.0;
    public static final double MAGO_MOLTIPLICATORE_FURIA = 1.0;
    public static final double MAGO_MOLTIPLICATORE_CORAGGIO = 1.0;
    public static final double MAGO_MOLTIPLICATORE_VALORE = 1.0;
    public static final double MAGO_MOLTIPLICATORE_CONTRATTAZIONE = 1.0;
    public static final double MAGO_MOLTIPLICATORE_NUMERO_BERSAGLI = 1.3;
    public static final double MAGO_MOLTIPLICATORE_STANCHEZZA = 1.4;
    public static final double MAGO_MOLTIPLICATORE_DANNI_MAGICI = 2.0;
    public static final double MAGO_MOLTIPLICATORE_DANNI_FISICI = 0.5;
    public static final double MAGO_MOLTIPLICATORE_RECUPERO_FISICO = 0.7;
    public static final double MAGO_MOLTIPLICATORE_RECUPERO_MAGICO = 1.5;

    // Sacerdote e Sacerdotessa: la magia dell'elfo con più Saggezza e più robustezza, senza doppia arma e senza dardo arcano
    public static final int SACERDOTE_MAX_CARISMA = 10;
    public static final int SACERDOTE_MAX_FORZA = 25;
    public static final int SACERDOTE_MAX_DESTREZZA = 25;
    public static final int SACERDOTE_MAX_COSTITUZIONE = 30;
    public static final int SACERDOTE_MAX_INTELLIGENZA = 40;
    public static final int SACERDOTE_MAX_SAGGEZZA = 50;
    public static final int SACERDOTE_MAX_FORTUNA = 12;
    public static final int SACERDOTE_MAX_NUMERO = 1;
    public static final int SACERDOTE_PUNTI_ESPERIENZA = 25;
    public static final int SACERDOTE_SALUTE_BASE = 100;
    public static final int SACERDOTE_MAGIA_BASE = 45;
    public static final int SACERDOTE_LIVELLAMENTO_MAGIA = 22;
    public static final double SACERDOTE_MOLTIPLICATORE_CARICO = 0.85;
    public static final double SACERDOTE_MOLTIPLICATORE_CRITICO = 1.0;
    public static final double SACERDOTE_MOLTIPLICATORE_PRECISIONE = 1.1;
    public static final double SACERDOTE_MOLTIPLICATORE_VELOCITA = 1.0;
    public static final double SACERDOTE_MOLTIPLICATORE_FURTIVITA = 0.9;
    public static final double SACERDOTE_MOLTIPLICATORE_PARATA = 0.8;
    public static final double SACERDOTE_MOLTIPLICATORE_RESISTENZA_MAGICA = 1.3;
    public static final double SACERDOTE_MOLTIPLICATORE_PERCEZIONE = 1.2;
    public static final double SACERDOTE_MOLTIPLICATORE_SOGGEZIONE = 1.1;
    public static final double SACERDOTE_MOLTIPLICATORE_FURIA = 1.0;
    public static final double SACERDOTE_MOLTIPLICATORE_CORAGGIO = 1.2;
    public static final double SACERDOTE_MOLTIPLICATORE_VALORE = 1.1;
    public static final double SACERDOTE_MOLTIPLICATORE_CONTRATTAZIONE = 1.0;
    public static final double SACERDOTE_MOLTIPLICATORE_NUMERO_BERSAGLI = 1.0;
    public static final double SACERDOTE_MOLTIPLICATORE_STANCHEZZA = 1.0;
    public static final double SACERDOTE_MOLTIPLICATORE_DANNI_MAGICI = 1.4;
    public static final double SACERDOTE_MOLTIPLICATORE_DANNI_FISICI = 0.8;
    public static final double SACERDOTE_MOLTIPLICATORE_RECUPERO_FISICO = 0.9;
    public static final double SACERDOTE_MOLTIPLICATORE_RECUPERO_MAGICO = 1.3;

    public static final int SACERDOTESSA_MAX_CARISMA = 10;
    public static final int SACERDOTESSA_MAX_FORZA = 25;
    public static final int SACERDOTESSA_MAX_DESTREZZA = 25;
    public static final int SACERDOTESSA_MAX_COSTITUZIONE = 30;
    public static final int SACERDOTESSA_MAX_INTELLIGENZA = 40;
    public static final int SACERDOTESSA_MAX_SAGGEZZA = 50;
    public static final int SACERDOTESSA_MAX_FORTUNA = 12;
    public static final int SACERDOTESSA_MAX_NUMERO = 1;
    public static final int SACERDOTESSA_PUNTI_ESPERIENZA = 25;
    public static final int SACERDOTESSA_SALUTE_BASE = 100;
    public static final int SACERDOTESSA_MAGIA_BASE = 45;
    public static final int SACERDOTESSA_LIVELLAMENTO_MAGIA = 22;
    public static final double SACERDOTESSA_MOLTIPLICATORE_CARICO = 0.8;
    public static final double SACERDOTESSA_MOLTIPLICATORE_CRITICO = 1.0;
    public static final double SACERDOTESSA_MOLTIPLICATORE_PRECISIONE = 1.1;
    public static final double SACERDOTESSA_MOLTIPLICATORE_VELOCITA = 1.0;
    public static final double SACERDOTESSA_MOLTIPLICATORE_FURTIVITA = 0.9;
    public static final double SACERDOTESSA_MOLTIPLICATORE_PARATA = 0.8;
    public static final double SACERDOTESSA_MOLTIPLICATORE_RESISTENZA_MAGICA = 1.3;
    public static final double SACERDOTESSA_MOLTIPLICATORE_PERCEZIONE = 1.2;
    public static final double SACERDOTESSA_MOLTIPLICATORE_SOGGEZIONE = 1.1;
    public static final double SACERDOTESSA_MOLTIPLICATORE_FURIA = 1.0;
    public static final double SACERDOTESSA_MOLTIPLICATORE_CORAGGIO = 1.2;
    public static final double SACERDOTESSA_MOLTIPLICATORE_VALORE = 1.1;
    public static final double SACERDOTESSA_MOLTIPLICATORE_CONTRATTAZIONE = 1.0;
    public static final double SACERDOTESSA_MOLTIPLICATORE_NUMERO_BERSAGLI = 1.0;
    public static final double SACERDOTESSA_MOLTIPLICATORE_STANCHEZZA = 1.0;
    public static final double SACERDOTESSA_MOLTIPLICATORE_DANNI_MAGICI = 1.4;
    public static final double SACERDOTESSA_MOLTIPLICATORE_DANNI_FISICI = 0.8;
    public static final double SACERDOTESSA_MOLTIPLICATORE_RECUPERO_FISICO = 0.9;
    public static final double SACERDOTESSA_MOLTIPLICATORE_RECUPERO_MAGICO = 1.3;

    // Chierico e Chierica: il sacerdote che combatte in prima linea (mazza, scudo, armatura): fra il Sacerdote e il Guerriero,
    // con più salute e parata del primo e più magia del secondo
    public static final int CHIERICO_MAX_CARISMA = 8;
    public static final int CHIERICO_MAX_FORZA = 35;
    public static final int CHIERICO_MAX_DESTREZZA = 25;
    public static final int CHIERICO_MAX_COSTITUZIONE = 40;
    public static final int CHIERICO_MAX_INTELLIGENZA = 25;
    public static final int CHIERICO_MAX_SAGGEZZA = 40;
    public static final int CHIERICO_MAX_FORTUNA = 10;
    public static final int CHIERICO_MAX_NUMERO = 1;
    public static final int CHIERICO_PUNTI_ESPERIENZA = 25;
    public static final int CHIERICO_SALUTE_BASE = 130;
    public static final int CHIERICO_MAGIA_BASE = 30;
    public static final int CHIERICO_LIVELLAMENTO_MAGIA = 14;
    public static final double CHIERICO_MOLTIPLICATORE_CARICO = 1.1;
    public static final double CHIERICO_MOLTIPLICATORE_CRITICO = 1.0;
    public static final double CHIERICO_MOLTIPLICATORE_PRECISIONE = 1.0;
    public static final double CHIERICO_MOLTIPLICATORE_VELOCITA = 0.9;
    public static final double CHIERICO_MOLTIPLICATORE_FURTIVITA = 0.8;
    public static final double CHIERICO_MOLTIPLICATORE_PARATA = 1.3;
    public static final double CHIERICO_MOLTIPLICATORE_RESISTENZA_MAGICA = 1.2;
    public static final double CHIERICO_MOLTIPLICATORE_PERCEZIONE = 1.0;
    public static final double CHIERICO_MOLTIPLICATORE_SOGGEZIONE = 1.1;
    public static final double CHIERICO_MOLTIPLICATORE_FURIA = 1.0;
    public static final double CHIERICO_MOLTIPLICATORE_CORAGGIO = 1.2;
    public static final double CHIERICO_MOLTIPLICATORE_VALORE = 1.2;
    public static final double CHIERICO_MOLTIPLICATORE_CONTRATTAZIONE = 1.0;
    public static final double CHIERICO_MOLTIPLICATORE_NUMERO_BERSAGLI = 1.0;
    public static final double CHIERICO_MOLTIPLICATORE_STANCHEZZA = 1.0;
    public static final double CHIERICO_MOLTIPLICATORE_DANNI_MAGICI = 1.0;
    public static final double CHIERICO_MOLTIPLICATORE_DANNI_FISICI = 1.0;
    public static final double CHIERICO_MOLTIPLICATORE_RECUPERO_FISICO = 1.2;
    public static final double CHIERICO_MOLTIPLICATORE_RECUPERO_MAGICO = 1.0;

    public static final int CHIERICA_MAX_CARISMA = 8;
    public static final int CHIERICA_MAX_FORZA = 35;
    public static final int CHIERICA_MAX_DESTREZZA = 25;
    public static final int CHIERICA_MAX_COSTITUZIONE = 40;
    public static final int CHIERICA_MAX_INTELLIGENZA = 25;
    public static final int CHIERICA_MAX_SAGGEZZA = 40;
    public static final int CHIERICA_MAX_FORTUNA = 10;
    public static final int CHIERICA_MAX_NUMERO = 1;
    public static final int CHIERICA_PUNTI_ESPERIENZA = 25;
    public static final int CHIERICA_SALUTE_BASE = 130;
    public static final int CHIERICA_MAGIA_BASE = 30;
    public static final int CHIERICA_LIVELLAMENTO_MAGIA = 14;
    public static final double CHIERICA_MOLTIPLICATORE_CARICO = 1.1;
    public static final double CHIERICA_MOLTIPLICATORE_CRITICO = 1.0;
    public static final double CHIERICA_MOLTIPLICATORE_PRECISIONE = 1.0;
    public static final double CHIERICA_MOLTIPLICATORE_VELOCITA = 0.9;
    public static final double CHIERICA_MOLTIPLICATORE_FURTIVITA = 0.8;
    public static final double CHIERICA_MOLTIPLICATORE_PARATA = 1.3;
    public static final double CHIERICA_MOLTIPLICATORE_RESISTENZA_MAGICA = 1.2;
    public static final double CHIERICA_MOLTIPLICATORE_PERCEZIONE = 1.0;
    public static final double CHIERICA_MOLTIPLICATORE_SOGGEZIONE = 1.1;
    public static final double CHIERICA_MOLTIPLICATORE_FURIA = 1.0;
    public static final double CHIERICA_MOLTIPLICATORE_CORAGGIO = 1.2;
    public static final double CHIERICA_MOLTIPLICATORE_VALORE = 1.2;
    public static final double CHIERICA_MOLTIPLICATORE_CONTRATTAZIONE = 1.0;
    public static final double CHIERICA_MOLTIPLICATORE_NUMERO_BERSAGLI = 1.0;
    public static final double CHIERICA_MOLTIPLICATORE_STANCHEZZA = 1.0;
    public static final double CHIERICA_MOLTIPLICATORE_DANNI_MAGICI = 1.0;
    public static final double CHIERICA_MOLTIPLICATORE_DANNI_FISICI = 1.0;
    public static final double CHIERICA_MOLTIPLICATORE_RECUPERO_FISICO = 1.2;
    public static final double CHIERICA_MOLTIPLICATORE_RECUPERO_MAGICO = 1.0;

    // Druido e Druida: l'incantatore della natura, con più Percezione, Saggezza e recupero magico del Mago ma meno magia
    // base, e senza armatura né scudo
    public static final int DRUIDO_MAX_CARISMA = 8;
    public static final int DRUIDO_MAX_FORZA = 25;
    public static final int DRUIDO_MAX_DESTREZZA = 30;
    public static final int DRUIDO_MAX_COSTITUZIONE = 35;
    public static final int DRUIDO_MAX_INTELLIGENZA = 40;
    public static final int DRUIDO_MAX_SAGGEZZA = 45;
    public static final int DRUIDO_MAX_FORTUNA = 15;
    public static final int DRUIDO_MAX_NUMERO = 1;
    public static final int DRUIDO_PUNTI_ESPERIENZA = 25;
    public static final int DRUIDO_SALUTE_BASE = 110;
    public static final int DRUIDO_MAGIA_BASE = 40;
    public static final int DRUIDO_LIVELLAMENTO_MAGIA = 20;
    public static final double DRUIDO_MOLTIPLICATORE_CARICO = 0.85;
    public static final double DRUIDO_MOLTIPLICATORE_CRITICO = 1.0;
    public static final double DRUIDO_MOLTIPLICATORE_PRECISIONE = 1.1;
    public static final double DRUIDO_MOLTIPLICATORE_VELOCITA = 1.1;
    public static final double DRUIDO_MOLTIPLICATORE_FURTIVITA = 1.1;
    public static final double DRUIDO_MOLTIPLICATORE_PARATA = 0.9;
    public static final double DRUIDO_MOLTIPLICATORE_RESISTENZA_MAGICA = 1.2;
    public static final double DRUIDO_MOLTIPLICATORE_PERCEZIONE = 1.3;
    public static final double DRUIDO_MOLTIPLICATORE_SOGGEZIONE = 1.1;
    public static final double DRUIDO_MOLTIPLICATORE_FURIA = 1.0;
    public static final double DRUIDO_MOLTIPLICATORE_CORAGGIO = 1.1;
    public static final double DRUIDO_MOLTIPLICATORE_VALORE = 1.0;
    public static final double DRUIDO_MOLTIPLICATORE_CONTRATTAZIONE = 1.0;
    public static final double DRUIDO_MOLTIPLICATORE_NUMERO_BERSAGLI = 1.0;
    public static final double DRUIDO_MOLTIPLICATORE_STANCHEZZA = 0.9;
    public static final double DRUIDO_MOLTIPLICATORE_DANNI_MAGICI = 1.2;
    public static final double DRUIDO_MOLTIPLICATORE_DANNI_FISICI = 0.9;
    public static final double DRUIDO_MOLTIPLICATORE_RECUPERO_FISICO = 1.1;
    public static final double DRUIDO_MOLTIPLICATORE_RECUPERO_MAGICO = 1.2;

    public static final int DRUIDA_MAX_CARISMA = 8;
    public static final int DRUIDA_MAX_FORZA = 25;
    public static final int DRUIDA_MAX_DESTREZZA = 30;
    public static final int DRUIDA_MAX_COSTITUZIONE = 35;
    public static final int DRUIDA_MAX_INTELLIGENZA = 40;
    public static final int DRUIDA_MAX_SAGGEZZA = 45;
    public static final int DRUIDA_MAX_FORTUNA = 15;
    public static final int DRUIDA_MAX_NUMERO = 1;
    public static final int DRUIDA_PUNTI_ESPERIENZA = 25;
    public static final int DRUIDA_SALUTE_BASE = 110;
    public static final int DRUIDA_MAGIA_BASE = 40;
    public static final int DRUIDA_LIVELLAMENTO_MAGIA = 20;
    public static final double DRUIDA_MOLTIPLICATORE_CARICO = 0.85;
    public static final double DRUIDA_MOLTIPLICATORE_CRITICO = 1.0;
    public static final double DRUIDA_MOLTIPLICATORE_PRECISIONE = 1.1;
    public static final double DRUIDA_MOLTIPLICATORE_VELOCITA = 1.1;
    public static final double DRUIDA_MOLTIPLICATORE_FURTIVITA = 1.1;
    public static final double DRUIDA_MOLTIPLICATORE_PARATA = 0.9;
    public static final double DRUIDA_MOLTIPLICATORE_RESISTENZA_MAGICA = 1.2;
    public static final double DRUIDA_MOLTIPLICATORE_PERCEZIONE = 1.3;
    public static final double DRUIDA_MOLTIPLICATORE_SOGGEZIONE = 1.1;
    public static final double DRUIDA_MOLTIPLICATORE_FURIA = 1.0;
    public static final double DRUIDA_MOLTIPLICATORE_CORAGGIO = 1.1;
    public static final double DRUIDA_MOLTIPLICATORE_VALORE = 1.0;
    public static final double DRUIDA_MOLTIPLICATORE_CONTRATTAZIONE = 1.0;
    public static final double DRUIDA_MOLTIPLICATORE_NUMERO_BERSAGLI = 1.0;
    public static final double DRUIDA_MOLTIPLICATORE_STANCHEZZA = 0.9;
    public static final double DRUIDA_MOLTIPLICATORE_DANNI_MAGICI = 1.2;
    public static final double DRUIDA_MOLTIPLICATORE_DANNI_FISICI = 0.9;
    public static final double DRUIDA_MOLTIPLICATORE_RECUPERO_FISICO = 1.1;
    public static final double DRUIDA_MOLTIPLICATORE_RECUPERO_MAGICO = 1.2;

    // I personaggi non combattenti (locandiere, armaiolo, alchimista, venditore di pergamene, incantatore, moglie del
    // bardo): come il bardo ma un poco meno, e senza capacità proprie (vedi PersonaggioNonCombattente)
    public static final int NON_COMBATTENTE_SALUTE_BASE = 90;
    public static final int NON_COMBATTENTE_MAGIA_BASE = 30;
    public static final int NON_COMBATTENTE_LIVELLAMENTO_MAGIA = 14;

    // Le caratteristiche dei non combattenti: erano quelle del bardo (le massime per 0,85); ora sono tutte loro
    public static final double NON_COMBATTENTE_MAX_CARISMA = 6.8;
    public static final double NON_COMBATTENTE_MAX_FORZA = 25.5;
    public static final double NON_COMBATTENTE_MAX_DESTREZZA = 29.75;
    public static final double NON_COMBATTENTE_MAX_COSTITUZIONE = 29.75;
    public static final double NON_COMBATTENTE_MAX_INTELLIGENZA = 29.75;
    public static final double NON_COMBATTENTE_MAX_SAGGEZZA = 29.75;
    public static final double NON_COMBATTENTE_MAX_FORTUNA = 12.75;
    public static final int NON_COMBATTENTE_MAX_NUMERO = 1;
    public static final int NON_COMBATTENTE_PUNTI_ESPERIENZA = 25;
    public static final double NON_COMBATTENTE_MOLTIPLICATORE_CARICO = 0.9;
    public static final double NON_COMBATTENTE_MOLTIPLICATORE_CRITICO = 1.1;
    public static final double NON_COMBATTENTE_MOLTIPLICATORE_PRECISIONE = 1.0;
    public static final double NON_COMBATTENTE_MOLTIPLICATORE_VELOCITA = 1.1;
    public static final double NON_COMBATTENTE_MOLTIPLICATORE_FURTIVITA = 1.1;
    public static final double NON_COMBATTENTE_MOLTIPLICATORE_PARATA = 1.0;
    public static final double NON_COMBATTENTE_MOLTIPLICATORE_RESISTENZA_MAGICA = 1.1;
    public static final double NON_COMBATTENTE_MOLTIPLICATORE_PERCEZIONE = 1.1;
    public static final double NON_COMBATTENTE_MOLTIPLICATORE_SOGGEZIONE = 1.2;
    public static final double NON_COMBATTENTE_MOLTIPLICATORE_FURIA = 1.1;
    public static final double NON_COMBATTENTE_MOLTIPLICATORE_CORAGGIO = 1.1;
    public static final double NON_COMBATTENTE_MOLTIPLICATORE_VALORE = 1.1;
    public static final double NON_COMBATTENTE_MOLTIPLICATORE_CONTRATTAZIONE = 1.2;
    public static final double NON_COMBATTENTE_MOLTIPLICATORE_NUMERO_BERSAGLI = 1.1;
    public static final double NON_COMBATTENTE_MOLTIPLICATORE_STANCHEZZA = 1.0;
    public static final double NON_COMBATTENTE_MOLTIPLICATORE_DANNI_MAGICI = 1.1;
    public static final double NON_COMBATTENTE_MOLTIPLICATORE_DANNI_FISICI = 0.9;
    public static final double NON_COMBATTENTE_MOLTIPLICATORE_RECUPERO_FISICO = 1.1;
    public static final double NON_COMBATTENTE_MOLTIPLICATORE_RECUPERO_MAGICO = 1.2;

    // Il Viandante: un ospite di missione con le caratteristiche del bardo (all'inizio le stesse; ora si tarano da sole)
    public static final int VIANDANTE_MAX_CARISMA = 8;
    public static final int VIANDANTE_MAX_FORZA = 30;
    public static final int VIANDANTE_MAX_DESTREZZA = 35;
    public static final int VIANDANTE_MAX_COSTITUZIONE = 35;
    public static final int VIANDANTE_MAX_INTELLIGENZA = 35;
    public static final int VIANDANTE_MAX_SAGGEZZA = 35;
    public static final int VIANDANTE_MAX_FORTUNA = 15;
    public static final int VIANDANTE_MAX_NUMERO = 1;
    public static final int VIANDANTE_PUNTI_ESPERIENZA = 25;
    public static final int VIANDANTE_SALUTE_BASE = 105;
    public static final int VIANDANTE_MAGIA_BASE = 40;
    public static final int VIANDANTE_LIVELLAMENTO_MAGIA = 18;
    public static final double VIANDANTE_MOLTIPLICATORE_CARICO = 0.9;
    public static final double VIANDANTE_MOLTIPLICATORE_CRITICO = 1.1;
    public static final double VIANDANTE_MOLTIPLICATORE_PRECISIONE = 1.0;
    public static final double VIANDANTE_MOLTIPLICATORE_VELOCITA = 1.1;
    public static final double VIANDANTE_MOLTIPLICATORE_FURTIVITA = 1.1;
    public static final double VIANDANTE_MOLTIPLICATORE_PARATA = 1.0;
    public static final double VIANDANTE_MOLTIPLICATORE_RESISTENZA_MAGICA = 1.1;
    public static final double VIANDANTE_MOLTIPLICATORE_PERCEZIONE = 1.1;
    public static final double VIANDANTE_MOLTIPLICATORE_SOGGEZIONE = 1.2;
    public static final double VIANDANTE_MOLTIPLICATORE_FURIA = 1.1;
    public static final double VIANDANTE_MOLTIPLICATORE_CORAGGIO = 1.1;
    public static final double VIANDANTE_MOLTIPLICATORE_VALORE = 1.1;
    public static final double VIANDANTE_MOLTIPLICATORE_CONTRATTAZIONE = 1.2;
    public static final double VIANDANTE_MOLTIPLICATORE_NUMERO_BERSAGLI = 1.1;
    public static final double VIANDANTE_MOLTIPLICATORE_STANCHEZZA = 1.0;
    public static final double VIANDANTE_MOLTIPLICATORE_DANNI_MAGICI = 1.1;
    public static final double VIANDANTE_MOLTIPLICATORE_DANNI_FISICI = 0.9;
    public static final double VIANDANTE_MOLTIPLICATORE_RECUPERO_FISICO = 1.1;
    public static final double VIANDANTE_MOLTIPLICATORE_RECUPERO_MAGICO = 1.2;

    // Personaggio giocabile segreto per test

    public static final int OMBRAFIAMMA_MAX_CARISMA = 9;
    public static final int OMBRAFIAMMA_MAX_FORZA = 45;
    public static final int OMBRAFIAMMA_MAX_DESTREZZA = 45;
    public static final int OMBRAFIAMMA_MAX_COSTITUZIONE = 40;
    public static final int OMBRAFIAMMA_MAX_INTELLIGENZA = 50;
    public static final int OMBRAFIAMMA_MAX_SAGGEZZA = 45;
    public static final int OMBRAFIAMMA_MAX_FORTUNA = 30;
    public static final int OMBRAFIAMMA_MAX_NUMERO = 1;
    public static final int OMBRAFIAMMA_PUNTI_ESPERIENZA = 100;

    public static final int OMBRAFIAMMA_SALUTE_BASE = 140;
    public static final int OMBRAFIAMMA_MAGIA_BASE = 50;
    public static final int OMBRAFIAMMA_LIVELLAMENTO_MAGIA = 20;
    public static final double OMBRAFIAMMA_MOLTIPLICATORE_CARICO = 90;
    public static final double OMBRAFIAMMA_MOLTIPLICATORE_CRITICO = 1.5;
    public static final double OMBRAFIAMMA_MOLTIPLICATORE_PRECISIONE = 1.5;
    public static final double OMBRAFIAMMA_MOLTIPLICATORE_VELOCITA = 1.5;
    public static final double OMBRAFIAMMA_MOLTIPLICATORE_FURTIVITA = 1.5;
    public static final double OMBRAFIAMMA_MOLTIPLICATORE_PARATA = 1.5;
    public static final double OMBRAFIAMMA_MOLTIPLICATORE_RESISTENZA_MAGICA = 1.5;
    public static final double OMBRAFIAMMA_MOLTIPLICATORE_PERCEZIONE = 1.5;
    public static final double OMBRAFIAMMA_MOLTIPLICATORE_SOGGEZIONE = 1.5;
    public static final double OMBRAFIAMMA_MOLTIPLICATORE_FURIA = 1.5;
    public static final double OMBRAFIAMMA_MOLTIPLICATORE_CORAGGIO = 1.5;
    public static final double OMBRAFIAMMA_MOLTIPLICATORE_VALORE = 1.5;
    public static final double OMBRAFIAMMA_MOLTIPLICATORE_CONTRATTAZIONE = 1.0;
    public static final double OMBRAFIAMMA_MOLTIPLICATORE_NUMERO_BERSAGLI = 3;
    public static final double OMBRAFIAMMA_MOLTIPLICATORE_STANCHEZZA = 0.1;
    public static final double OMBRAFIAMMA_MOLTIPLICATORE_DANNI_MAGICI = 2.0;
    public static final double OMBRAFIAMMA_MOLTIPLICATORE_DANNI_FISICI = 2.0;
    public static final double OMBRAFIAMMA_MOLTIPLICATORE_RECUPERO_FISICO = 1.8;
    public static final double OMBRAFIAMMA_MOLTIPLICATORE_RECUPERO_MAGICO = 1.5;

    // Personaggi non giocabili (avversari). Alcuno possono essere amichevoli o corrompibili e offrire supporto

    public static final int ARPIA_MAX_FORZA = 10;
    public static final int ARPIA_MAX_DESTREZZA = 15;
    public static final int ARPIA_MAX_COSTITUZIONE = 7;
    public static final int ARPIA_MAX_INTELLIGENZA = 8;
    public static final int ARPIA_MAX_SAGGEZZA = 5;
    public static final int ARPIA_MAX_CARISMA = 1;
    public static final int ARPIA_MAX_FORTUNA = 4;
    public static final int ARPIA_MAX_NUMERO = 4;
    public static final int ARPIA_PUNTI_ESPERIENZA = 15;

    public static final int ARPIA_SALUTE_BASE = 95;
    public static final int ARPIA_MAGIA_BASE = 35;
    public static final int ARPIA_LIVELLAMENTO_MAGIA = 12;
    public static final double ARPIA_MOLTIPLICATORE_CARICO = 0.7;
    public static final double ARPIA_MOLTIPLICATORE_CRITICO = 1.3;
    public static final double ARPIA_MOLTIPLICATORE_PRECISIONE = 1.1;
    public static final double ARPIA_MOLTIPLICATORE_VELOCITA = 1.3;
    public static final double ARPIA_MOLTIPLICATORE_FURTIVITA = 0.7;
    public static final double ARPIA_MOLTIPLICATORE_PARATA = 0.7;
    public static final double ARPIA_MOLTIPLICATORE_RESISTENZA_MAGICA = 1.4;
    public static final double ARPIA_MOLTIPLICATORE_PERCEZIONE = 1.4;
    public static final double ARPIA_MOLTIPLICATORE_SOGGEZIONE = 0.9;
    public static final double ARPIA_MOLTIPLICATORE_FURIA = 0.5;
    public static final double ARPIA_MOLTIPLICATORE_CORAGGIO = 0.7;
    public static final double ARPIA_MOLTIPLICATORE_VALORE = 0.5;
    public static final double ARPIA_MOLTIPLICATORE_NUMERO_BERSAGLI = 0.8;
    public static final double ARPIA_MOLTIPLICATORE_STANCHEZZA = 1.1;
    public static final double ARPIA_MOLTIPLICATORE_DANNI_MAGICI = 0.8;
    public static final double ARPIA_MOLTIPLICATORE_DANNI_FISICI = 0.9;
    public static final double ARPIA_MOLTIPLICATORE_RECUPERO_FISICO = 1.0;
    public static final double ARPIA_MOLTIPLICATORE_RECUPERO_MAGICO = 0.8;

    public static final int CENTAURO_MAX_CARISMA = 4;
    public static final int CENTAURO_MAX_NUMERO = 4;
    public static final int CENTAURO_MAX_FORZA = 25;
    public static final int CENTAURO_MAX_DESTREZZA = 22;
    public static final int CENTAURO_MAX_COSTITUZIONE = 25;
    public static final int CENTAURO_MAX_INTELLIGENZA = 12;
    public static final int CENTAURO_MAX_SAGGEZZA = 18;
    public static final int CENTAURO_MAX_FORTUNA = 10;
    public static final int CENTAURO_PUNTI_ESPERIENZA = 40;

    public static final int CENTAURO_SALUTE_BASE = 125;
    public static final int CENTAURO_MAGIA_BASE = 15;
    public static final int CENTAURO_LIVELLAMENTO_MAGIA = 4;
    public static final double CENTAURO_MOLTIPLICATORE_CARICO = 2.0;
    public static final double CENTAURO_MOLTIPLICATORE_CRITICO = 0.8;
    public static final double CENTAURO_MOLTIPLICATORE_PRECISIONE = 1.1;
    public static final double CENTAURO_MOLTIPLICATORE_VELOCITA = 1.4;
    public static final double CENTAURO_MOLTIPLICATORE_FURTIVITA = 0.5;
    public static final double CENTAURO_MOLTIPLICATORE_PARATA = 1.1;
    public static final double CENTAURO_MOLTIPLICATORE_RESISTENZA_MAGICA = 1.1;
    public static final double CENTAURO_MOLTIPLICATORE_PERCEZIONE = 1.1;
    public static final double CENTAURO_MOLTIPLICATORE_SOGGEZIONE = 1.1;
    public static final double CENTAURO_MOLTIPLICATORE_FURIA = 1.3;
    public static final double CENTAURO_MOLTIPLICATORE_CORAGGIO = 1.1;
    public static final double CENTAURO_MOLTIPLICATORE_VALORE = 1.3;
    public static final double CENTAURO_MOLTIPLICATORE_CONTRATTAZIONE = 0.9;
    public static final double CENTAURO_MOLTIPLICATORE_NUMERO_BERSAGLI = 1.2;
    public static final double CENTAURO_MOLTIPLICATORE_STANCHEZZA = 0.8;
    public static final double CENTAURO_MOLTIPLICATORE_DANNI_MAGICI = 0.8;
    public static final double CENTAURO_MOLTIPLICATORE_DANNI_FISICI = 1.3;
    public static final double CENTAURO_MOLTIPLICATORE_RECUPERO_FISICO = 1.3;
    public static final double CENTAURO_MOLTIPLICATORE_RECUPERO_MAGICO = 0.7;

    public static final int CHIMERA_MAX_CARISMA = 0;
    public static final int CHIMERA_MAX_NUMERO = 5;
    public static final int CHIMERA_MAX_FORZA = 20;
    public static final int CHIMERA_MAX_DESTREZZA = 18;
    public static final int CHIMERA_MAX_COSTITUZIONE = 18;
    public static final int CHIMERA_MAX_INTELLIGENZA = 6;
    public static final int CHIMERA_MAX_SAGGEZZA = 7;
    public static final int CHIMERA_MAX_FORTUNA = 6;
    public static final int CHIMERA_PUNTI_ESPERIENZA = 40;

    public static final int CHIMERA_SALUTE_BASE = 120;
    public static final int CHIMERA_MAGIA_BASE = 30;
    public static final int CHIMERA_LIVELLAMENTO_MAGIA = 8;
    public static final double CHIMERA_MOLTIPLICATORE_CARICO = 2.5;
    public static final double CHIMERA_MOLTIPLICATORE_CRITICO = 1.1;
    public static final double CHIMERA_MOLTIPLICATORE_PRECISIONE = 1.0;
    public static final double CHIMERA_MOLTIPLICATORE_VELOCITA = 1.2;
    public static final double CHIMERA_MOLTIPLICATORE_FURTIVITA = 1.1;
    public static final double CHIMERA_MOLTIPLICATORE_PARATA = 0.9;
    public static final double CHIMERA_MOLTIPLICATORE_RESISTENZA_MAGICA = 1.3;
    public static final double CHIMERA_MOLTIPLICATORE_PERCEZIONE = 1.3;
    public static final double CHIMERA_MOLTIPLICATORE_SOGGEZIONE = 1.2;
    public static final double CHIMERA_MOLTIPLICATORE_FURIA = 0.6;
    public static final double CHIMERA_MOLTIPLICATORE_CORAGGIO = 1.0;
    public static final double CHIMERA_MOLTIPLICATORE_VALORE = 0.6;
    public static final double CHIMERA_MOLTIPLICATORE_NUMERO_BERSAGLI = 1.4;
    public static final double CHIMERA_MOLTIPLICATORE_STANCHEZZA = 0.9;
    public static final double CHIMERA_MOLTIPLICATORE_DANNI_MAGICI = 0.9;
    public static final double CHIMERA_MOLTIPLICATORE_DANNI_FISICI = 1.3;
    public static final double CHIMERA_MOLTIPLICATORE_RECUPERO_FISICO = 1.2;
    public static final double CHIMERA_MOLTIPLICATORE_RECUPERO_MAGICO = 0.8;

    public static final int CHIMERADRAGO_MAX_CARISMA = 0;
    public static final int CHIMERADRAGO_MAX_NUMERO = 2;
    public static final int CHIMERADRAGO_MAX_FORZA = 50;
    public static final int CHIMERADRAGO_MAX_DESTREZZA = 35;
    public static final int CHIMERADRAGO_MAX_COSTITUZIONE = 50;
    public static final int CHIMERADRAGO_MAX_INTELLIGENZA = 28;
    public static final int CHIMERADRAGO_MAX_SAGGEZZA = 25;
    public static final int CHIMERADRAGO_MAX_FORTUNA = 18;
    public static final int CHIMERADRAGO_PUNTI_ESPERIENZA = 70;

    public static final int CHIMERADRAGO_SALUTE_BASE = 240;
    public static final int CHIMERADRAGO_MAGIA_BASE = 60;
    public static final int CHIMERADRAGO_LIVELLAMENTO_MAGIA = 16;
    public static final double CHIMERADRAGO_MOLTIPLICATORE_CARICO = 6.0;
    public static final double CHIMERADRAGO_MOLTIPLICATORE_CRITICO = 0.8;
    public static final double CHIMERADRAGO_MOLTIPLICATORE_PRECISIONE = 0.9;
    public static final double CHIMERADRAGO_MOLTIPLICATORE_VELOCITA = 0.9;
    public static final double CHIMERADRAGO_MOLTIPLICATORE_FURTIVITA = 0.6;
    public static final double CHIMERADRAGO_MOLTIPLICATORE_PARATA = 1.2;
    public static final double CHIMERADRAGO_MOLTIPLICATORE_RESISTENZA_MAGICA = 1.4;
    public static final double CHIMERADRAGO_MOLTIPLICATORE_PERCEZIONE = 1.4;
    public static final double CHIMERADRAGO_MOLTIPLICATORE_SOGGEZIONE = 1.4;
    public static final double CHIMERADRAGO_MOLTIPLICATORE_FURIA = 0.7;
    public static final double CHIMERADRAGO_MOLTIPLICATORE_CORAGGIO = 1.2;
    public static final double CHIMERADRAGO_MOLTIPLICATORE_VALORE = 0.7;
    public static final double CHIMERADRAGO_MOLTIPLICATORE_NUMERO_BERSAGLI = 1.6;
    public static final double CHIMERADRAGO_MOLTIPLICATORE_STANCHEZZA = 1.1;
    public static final double CHIMERADRAGO_MOLTIPLICATORE_DANNI_MAGICI = 1.2;
    public static final double CHIMERADRAGO_MOLTIPLICATORE_DANNI_FISICI = 1.6;
    public static final double CHIMERADRAGO_MOLTIPLICATORE_RECUPERO_FISICO = 1.3;
    public static final double CHIMERADRAGO_MOLTIPLICATORE_RECUPERO_MAGICO = 1.2;

    public static final int EREMITA_MAX_CARISMA = 1;
    public static final int EREMITA_MAX_FORZA = 25;
    public static final int EREMITA_MAX_DESTREZZA = 20;
    public static final int EREMITA_MAX_COSTITUZIONE = 25;
    public static final int EREMITA_MAX_INTELLIGENZA = 25;
    public static final int EREMITA_MAX_SAGGEZZA = 55;
    public static final int EREMITA_MAX_FORTUNA = 12;
    public static final int EREMITA_MAX_NUMERO = 1;
    public static final int EREMITA_PUNTI_ESPERIENZA = 15;

    public static final int EREMITA_SALUTE_BASE = 90;
    public static final int EREMITA_MAGIA_BASE = 20;
    public static final int EREMITA_LIVELLAMENTO_MAGIA = 10;
    public static final double EREMITA_MOLTIPLICATORE_CARICO = 0.8;
    public static final double EREMITA_MOLTIPLICATORE_CRITICO = 0.8;
    public static final double EREMITA_MOLTIPLICATORE_PRECISIONE = 0.9;
    public static final double EREMITA_MOLTIPLICATORE_VELOCITA = 0.8;
    public static final double EREMITA_MOLTIPLICATORE_FURTIVITA = 1.1;
    public static final double EREMITA_MOLTIPLICATORE_PARATA = 0.8;
    public static final double EREMITA_MOLTIPLICATORE_RESISTENZA_MAGICA = 1.2;
    public static final double EREMITA_MOLTIPLICATORE_PERCEZIONE = 1.2;
    public static final double EREMITA_MOLTIPLICATORE_SOGGEZIONE = 0.8;
    public static final double EREMITA_MOLTIPLICATORE_FURIA = 1.0;
    public static final double EREMITA_MOLTIPLICATORE_CORAGGIO = 1.0;
    public static final double EREMITA_MOLTIPLICATORE_VALORE = 1.0;
    public static final double EREMITA_MOLTIPLICATORE_CONTRATTAZIONE = 0.8;
    public static final double EREMITA_MOLTIPLICATORE_NUMERO_BERSAGLI = 0.9;
    public static final double EREMITA_MOLTIPLICATORE_STANCHEZZA = 1.2;
    public static final double EREMITA_MOLTIPLICATORE_DANNI_MAGICI = 1.2;
    public static final double EREMITA_MOLTIPLICATORE_DANNI_FISICI = 0.7;
    public static final double EREMITA_MOLTIPLICATORE_RECUPERO_FISICO = 0.9;
    public static final double EREMITA_MOLTIPLICATORE_RECUPERO_MAGICO = 1.4;

    public static final int FANTASMA_MAX_CARISMA = 0;
    public static final int FANTASMA_MAX_NUMERO = 3;
    public static final int FANTASMA_MAX_FORZA = 10;
    public static final int FANTASMA_MAX_DESTREZZA = 20;
    public static final int FANTASMA_MAX_COSTITUZIONE = 12;
    public static final int FANTASMA_MAX_INTELLIGENZA = 15;
    public static final int FANTASMA_MAX_SAGGEZZA = 18;
    public static final int FANTASMA_MAX_FORTUNA = 10;
    public static final int FANTASMA_PUNTI_ESPERIENZA = 15;

    public static final int FANTASMA_SALUTE_BASE = 65;
    public static final int FANTASMA_MAGIA_BASE = 30;
    public static final int FANTASMA_LIVELLAMENTO_MAGIA = 15;
    public static final double FANTASMA_MOLTIPLICATORE_CARICO = 0.0;
    public static final double FANTASMA_MOLTIPLICATORE_CRITICO = 0.5;
    public static final double FANTASMA_MOLTIPLICATORE_PRECISIONE = 0.8;
    public static final double FANTASMA_MOLTIPLICATORE_VELOCITA = 1.1;
    public static final double FANTASMA_MOLTIPLICATORE_FURTIVITA = 1.3;
    public static final double FANTASMA_MOLTIPLICATORE_PARATA = 0.2;
    public static final double FANTASMA_MOLTIPLICATORE_RESISTENZA_MAGICA = 1.0;
    public static final double FANTASMA_MOLTIPLICATORE_PERCEZIONE = 1.0;
    public static final double FANTASMA_MOLTIPLICATORE_SOGGEZIONE = 1.1;
    public static final double FANTASMA_MOLTIPLICATORE_FURIA = 0.4;
    public static final double FANTASMA_MOLTIPLICATORE_CORAGGIO = 1.2;
    public static final double FANTASMA_MOLTIPLICATORE_VALORE = 0.4;
    public static final double FANTASMA_MOLTIPLICATORE_NUMERO_BERSAGLI = 0.9;
    public static final double FANTASMA_MOLTIPLICATORE_STANCHEZZA = 0.0;
    public static final double FANTASMA_MOLTIPLICATORE_DANNI_MAGICI = 1.1;
    public static final double FANTASMA_MOLTIPLICATORE_DANNI_FISICI = 0.2;
    public static final double FANTASMA_MOLTIPLICATORE_RECUPERO_FISICO = 0.0;
    public static final double FANTASMA_MOLTIPLICATORE_RECUPERO_MAGICO = 1.2;

    public static final int FOLLETTO_MAX_CARISMA = 1;
    public static final int FOLLETTO_MAX_NUMERO = 5;
    public static final int FOLLETTO_MAX_FORZA = 5;
    public static final int FOLLETTO_MAX_DESTREZZA = 8;
    public static final int FOLLETTO_MAX_COSTITUZIONE = 5;
    public static final int FOLLETTO_MAX_INTELLIGENZA = 8;
    public static final int FOLLETTO_MAX_SAGGEZZA = 4;
    public static final int FOLLETTO_MAX_FORTUNA = 2;
    public static final int FOLLETTO_PUNTI_ESPERIENZA = 15;

    public static final int FOLLETTO_SALUTE_BASE = 45;
    public static final int FOLLETTO_MAGIA_BASE = 65;
    public static final int FOLLETTO_LIVELLAMENTO_MAGIA = 28;
    public static final double FOLLETTO_MOLTIPLICATORE_CARICO = 0.2;
    public static final double FOLLETTO_MOLTIPLICATORE_CRITICO = 1.2;
    public static final double FOLLETTO_MOLTIPLICATORE_PRECISIONE = 1.1;
    public static final double FOLLETTO_MOLTIPLICATORE_VELOCITA = 1.5;
    public static final double FOLLETTO_MOLTIPLICATORE_FURTIVITA = 1.4;
    public static final double FOLLETTO_MOLTIPLICATORE_PARATA = 0.3;
    public static final double FOLLETTO_MOLTIPLICATORE_RESISTENZA_MAGICA = 1.1;
    public static final double FOLLETTO_MOLTIPLICATORE_PERCEZIONE = 1.1;
    public static final double FOLLETTO_MOLTIPLICATORE_SOGGEZIONE = 0.4;
    public static final double FOLLETTO_MOLTIPLICATORE_FURIA = 0.5;
    public static final double FOLLETTO_MOLTIPLICATORE_CORAGGIO = 0.6;
    public static final double FOLLETTO_MOLTIPLICATORE_VALORE = 0.5;
    public static final double FOLLETTO_MOLTIPLICATORE_NUMERO_BERSAGLI = 0.6;
    public static final double FOLLETTO_MOLTIPLICATORE_STANCHEZZA = 1.3;
    public static final double FOLLETTO_MOLTIPLICATORE_DANNI_MAGICI = 1.2;
    public static final double FOLLETTO_MOLTIPLICATORE_DANNI_FISICI = 0.3;
    public static final double FOLLETTO_MOLTIPLICATORE_RECUPERO_FISICO = 0.8;
    public static final double FOLLETTO_MOLTIPLICATORE_RECUPERO_MAGICO = 1.3;

    public static final int GARGOYLE_MAX_CARISMA = 0;
    public static final int GARGOYLE_MAX_NUMERO = 2;
    public static final int GARGOYLE_MAX_FORZA = 28;
    public static final int GARGOYLE_MAX_DESTREZZA = 20;
    public static final int GARGOYLE_MAX_COSTITUZIONE = 28;
    public static final int GARGOYLE_MAX_INTELLIGENZA = 18;
    public static final int GARGOYLE_MAX_SAGGEZZA = 12;
    public static final int GARGOYLE_MAX_FORTUNA = 10;
    public static final int GARGOYLE_PUNTI_ESPERIENZA = 40;

    public static final int GARGOYLE_SALUTE_BASE = 135;
    public static final int GARGOYLE_MAGIA_BASE = 25;
    public static final int GARGOYLE_LIVELLAMENTO_MAGIA = 6;
    public static final double GARGOYLE_MOLTIPLICATORE_CARICO = 1.4;
    public static final double GARGOYLE_MOLTIPLICATORE_CRITICO = 0.8;
    public static final double GARGOYLE_MOLTIPLICATORE_PRECISIONE = 0.9;
    public static final double GARGOYLE_MOLTIPLICATORE_VELOCITA = 0.7;
    public static final double GARGOYLE_MOLTIPLICATORE_FURTIVITA = 0.4;
    public static final double GARGOYLE_MOLTIPLICATORE_PARATA = 1.4;
    public static final double GARGOYLE_MOLTIPLICATORE_RESISTENZA_MAGICA = 0.9;
    public static final double GARGOYLE_MOLTIPLICATORE_PERCEZIONE = 0.9;
    public static final double GARGOYLE_MOLTIPLICATORE_SOGGEZIONE = 1.1;
    public static final double GARGOYLE_MOLTIPLICATORE_FURIA = 1.2;
    public static final double GARGOYLE_MOLTIPLICATORE_CORAGGIO = 1.3;
    public static final double GARGOYLE_MOLTIPLICATORE_VALORE = 1.2;
    public static final double GARGOYLE_MOLTIPLICATORE_NUMERO_BERSAGLI = 1.1;
    public static final double GARGOYLE_MOLTIPLICATORE_STANCHEZZA = 0.5;
    public static final double GARGOYLE_MOLTIPLICATORE_DANNI_MAGICI = 0.7;
    public static final double GARGOYLE_MOLTIPLICATORE_DANNI_FISICI = 1.2;
    public static final double GARGOYLE_MOLTIPLICATORE_RECUPERO_FISICO = 1.4;
    public static final double GARGOYLE_MOLTIPLICATORE_RECUPERO_MAGICO = 1.0;

    public static final int GIGANTE_MAX_CARISMA = 3;
    public static final int GIGANTE_MAX_NUMERO = 3;
    public static final int GIGANTE_MAX_FORZA = 40;
    public static final int GIGANTE_MAX_DESTREZZA = 18;
    public static final int GIGANTE_MAX_COSTITUZIONE = 35;
    public static final int GIGANTE_MAX_INTELLIGENZA = 15;
    public static final int GIGANTE_MAX_SAGGEZZA = 18;
    public static final int GIGANTE_MAX_FORTUNA = 10;
    public static final int GIGANTE_PUNTI_ESPERIENZA = 70;

    public static final int GIGANTE_SALUTE_BASE = 250;
    public static final int GIGANTE_MAGIA_BASE = 0;
    public static final int GIGANTE_LIVELLAMENTO_MAGIA = 0;
    public static final double GIGANTE_MOLTIPLICATORE_CARICO = 4.5;
    public static final double GIGANTE_MOLTIPLICATORE_CRITICO = 0.6;
    public static final double GIGANTE_MOLTIPLICATORE_PRECISIONE = 0.7;
    public static final double GIGANTE_MOLTIPLICATORE_VELOCITA = 0.6;
    public static final double GIGANTE_MOLTIPLICATORE_FURTIVITA = 0.2;
    public static final double GIGANTE_MOLTIPLICATORE_PARATA = 1.4;
    public static final double GIGANTE_MOLTIPLICATORE_RESISTENZA_MAGICA = 0.8;
    public static final double GIGANTE_MOLTIPLICATORE_PERCEZIONE = 0.8;
    public static final double GIGANTE_MOLTIPLICATORE_SOGGEZIONE = 1.4;
    public static final double GIGANTE_MOLTIPLICATORE_FURIA = 1.1;
    public static final double GIGANTE_MOLTIPLICATORE_CORAGGIO = 1.2;
    public static final double GIGANTE_MOLTIPLICATORE_VALORE = 1.1;
    public static final double GIGANTE_MOLTIPLICATORE_CONTRATTAZIONE = 0.7;
    public static final double GIGANTE_MOLTIPLICATORE_NUMERO_BERSAGLI = 1.6;
    public static final double GIGANTE_MOLTIPLICATORE_STANCHEZZA = 1.3;
    public static final double GIGANTE_MOLTIPLICATORE_DANNI_MAGICI = 0.6;
    public static final double GIGANTE_MOLTIPLICATORE_DANNI_FISICI = 1.6;
    public static final double GIGANTE_MOLTIPLICATORE_RECUPERO_FISICO = 1.3;
    public static final double GIGANTE_MOLTIPLICATORE_RECUPERO_MAGICO = 0.5;

    public static final int GOBLIN_MAX_CARISMA = 0;
    public static final int GOBLIN_MAX_NUMERO = 5;
    public static final int GOBLIN_MAX_FORZA = 12;
    public static final int GOBLIN_MAX_DESTREZZA = 12;
    public static final int GOBLIN_MAX_COSTITUZIONE = 12;
    public static final int GOBLIN_MAX_INTELLIGENZA = 4;
    public static final int GOBLIN_MAX_SAGGEZZA = 5;
    public static final int GOBLIN_MAX_FORTUNA = 5;
    public static final int GOBLIN_PUNTI_ESPERIENZA = 15;

    public static final int GOBLIN_SALUTE_BASE = 60;
    public static final int GOBLIN_MAGIA_BASE = 0;
    public static final int GOBLIN_LIVELLAMENTO_MAGIA = 0;
    public static final double GOBLIN_MOLTIPLICATORE_CARICO = 0.6;
    public static final double GOBLIN_MOLTIPLICATORE_CRITICO = 1.2;
    public static final double GOBLIN_MOLTIPLICATORE_PRECISIONE = 1.0;
    public static final double GOBLIN_MOLTIPLICATORE_VELOCITA = 1.2;
    public static final double GOBLIN_MOLTIPLICATORE_FURTIVITA = 1.3;
    public static final double GOBLIN_MOLTIPLICATORE_PARATA = 0.8;
    public static final double GOBLIN_MOLTIPLICATORE_RESISTENZA_MAGICA = 1.1;
    public static final double GOBLIN_MOLTIPLICATORE_PERCEZIONE = 1.1;
    public static final double GOBLIN_MOLTIPLICATORE_SOGGEZIONE = 0.5;
    public static final double GOBLIN_MOLTIPLICATORE_FURIA = 0.3;
    public static final double GOBLIN_MOLTIPLICATORE_CORAGGIO = 0.4;
    public static final double GOBLIN_MOLTIPLICATORE_VALORE = 0.3;
    public static final double GOBLIN_MOLTIPLICATORE_CONTRATTAZIONE = 1.2;
    public static final double GOBLIN_MOLTIPLICATORE_NUMERO_BERSAGLI = 0.8;
    public static final double GOBLIN_MOLTIPLICATORE_STANCHEZZA = 1.1;
    public static final double GOBLIN_MOLTIPLICATORE_DANNI_MAGICI = 0.8;
    public static final double GOBLIN_MOLTIPLICATORE_DANNI_FISICI = 0.8;
    public static final double GOBLIN_MOLTIPLICATORE_RECUPERO_FISICO = 0.9;
    public static final double GOBLIN_MOLTIPLICATORE_RECUPERO_MAGICO = 0.9;

    public static final int HOBGOBLIN_MAX_CARISMA = 0;
    public static final int HOBGOBLIN_MAX_NUMERO = 3;
    public static final int HOBGOBLIN_MAX_FORZA = 20;
    public static final int HOBGOBLIN_MAX_DESTREZZA = 18;
    public static final int HOBGOBLIN_MAX_COSTITUZIONE = 25;
    public static final int HOBGOBLIN_MAX_INTELLIGENZA = 12;
    public static final int HOBGOBLIN_MAX_SAGGEZZA = 10;
    public static final int HOBGOBLIN_MAX_FORTUNA = 10;
    public static final int HOBGOBLIN_PUNTI_ESPERIENZA = 15;

    public static final int HOBGOBLIN_SALUTE_BASE = 85;
    public static final int HOBGOBLIN_MAGIA_BASE = 0;
    public static final int HOBGOBLIN_LIVELLAMENTO_MAGIA = 0;
    public static final double HOBGOBLIN_MOLTIPLICATORE_CARICO = 1.1;
    public static final double HOBGOBLIN_MOLTIPLICATORE_CRITICO = 1.1;
    public static final double HOBGOBLIN_MOLTIPLICATORE_PRECISIONE = 1.2;
    public static final double HOBGOBLIN_MOLTIPLICATORE_VELOCITA = 0.9;
    public static final double HOBGOBLIN_MOLTIPLICATORE_FURTIVITA = 0.9;
    public static final double HOBGOBLIN_MOLTIPLICATORE_PARATA = 1.4;
    public static final double HOBGOBLIN_MOLTIPLICATORE_RESISTENZA_MAGICA = 1.1;
    public static final double HOBGOBLIN_MOLTIPLICATORE_PERCEZIONE = 1.1;
    public static final double HOBGOBLIN_MOLTIPLICATORE_SOGGEZIONE = 1.1;
    public static final double HOBGOBLIN_MOLTIPLICATORE_FURIA = 1.3;
    public static final double HOBGOBLIN_MOLTIPLICATORE_CORAGGIO = 1.3;
    public static final double HOBGOBLIN_MOLTIPLICATORE_VALORE = 1.3;
    public static final double HOBGOBLIN_MOLTIPLICATORE_CONTRATTAZIONE = 1.1;
    public static final double HOBGOBLIN_MOLTIPLICATORE_NUMERO_BERSAGLI = 1.1;
    public static final double HOBGOBLIN_MOLTIPLICATORE_STANCHEZZA = 1.0;
    public static final double HOBGOBLIN_MOLTIPLICATORE_DANNI_MAGICI = 0.7;
    public static final double HOBGOBLIN_MOLTIPLICATORE_DANNI_FISICI = 1.2;
    public static final double HOBGOBLIN_MOLTIPLICATORE_RECUPERO_FISICO = 1.2;
    public static final double HOBGOBLIN_MOLTIPLICATORE_RECUPERO_MAGICO = 0.7;

    public static final int MINOTAURO_MAX_CARISMA = 0;
    public static final int MINOTAURO_MAX_NUMERO = 3;
    public static final int MINOTAURO_MAX_FORZA = 35;
    public static final int MINOTAURO_MAX_DESTREZZA = 22;
    public static final int MINOTAURO_MAX_COSTITUZIONE = 30;
    public static final int MINOTAURO_MAX_INTELLIGENZA = 10;
    public static final int MINOTAURO_MAX_SAGGEZZA = 12;
    public static final int MINOTAURO_MAX_FORTUNA = 10;
    public static final int MINOTAURO_PUNTI_ESPERIENZA = 40;

    public static final int MINOTAURO_SALUTE_BASE = 160;
    public static final int MINOTAURO_MAGIA_BASE = 0;
    public static final int MINOTAURO_LIVELLAMENTO_MAGIA = 0;
    public static final double MINOTAURO_MOLTIPLICATORE_CARICO = 2.2;
    public static final double MINOTAURO_MOLTIPLICATORE_CRITICO = 0.7;
    public static final double MINOTAURO_MOLTIPLICATORE_PRECISIONE = 0.7;
    public static final double MINOTAURO_MOLTIPLICATORE_VELOCITA = 0.8;
    public static final double MINOTAURO_MOLTIPLICATORE_FURTIVITA = 0.5;
    public static final double MINOTAURO_MOLTIPLICATORE_PARATA = 1.2;
    public static final double MINOTAURO_MOLTIPLICATORE_RESISTENZA_MAGICA = 0.8;
    public static final double MINOTAURO_MOLTIPLICATORE_PERCEZIONE = 0.8;
    public static final double MINOTAURO_MOLTIPLICATORE_SOGGEZIONE = 1.3;
    public static final double MINOTAURO_MOLTIPLICATORE_FURIA = 0.6;
    public static final double MINOTAURO_MOLTIPLICATORE_CORAGGIO = 1.2;
    public static final double MINOTAURO_MOLTIPLICATORE_VALORE = 0.6;
    public static final double MINOTAURO_MOLTIPLICATORE_CONTRATTAZIONE = 0.6;
    public static final double MINOTAURO_MOLTIPLICATORE_NUMERO_BERSAGLI = 1.3;
    public static final double MINOTAURO_MOLTIPLICATORE_STANCHEZZA = 1.2;
    public static final double MINOTAURO_MOLTIPLICATORE_DANNI_MAGICI = 0.5;
    public static final double MINOTAURO_MOLTIPLICATORE_DANNI_FISICI = 1.4;
    public static final double MINOTAURO_MOLTIPLICATORE_RECUPERO_FISICO = 1.3;
    public static final double MINOTAURO_MOLTIPLICATORE_RECUPERO_MAGICO = 0.5;

    public static final int OMBRANERA_MAX_CARISMA = 0;
    public static final int OMBRANERA_MAX_NUMERO = 2;
    public static final int OMBRANERA_MAX_FORZA = 35;
    public static final int OMBRANERA_MAX_DESTREZZA = 35;
    public static final int OMBRANERA_MAX_COSTITUZIONE = 35;
    public static final int OMBRANERA_MAX_INTELLIGENZA = 31;
    public static final int OMBRANERA_MAX_SAGGEZZA = 30;
    public static final int OMBRANERA_MAX_FORTUNA = 20;
    public static final int OMBRANERA_PUNTI_ESPERIENZA = 40;

    public static final int OMBRANERA_SALUTE_BASE = 90;
    public static final int OMBRANERA_MAGIA_BASE = 40;
    public static final int OMBRANERA_LIVELLAMENTO_MAGIA = 20;
    public static final double OMBRANERA_MOLTIPLICATORE_CARICO = 0.0;
    public static final double OMBRANERA_MOLTIPLICATORE_CRITICO = 1.4;
    public static final double OMBRANERA_MOLTIPLICATORE_PRECISIONE = 1.2;
    public static final double OMBRANERA_MOLTIPLICATORE_VELOCITA = 1.3;
    public static final double OMBRANERA_MOLTIPLICATORE_FURTIVITA = 1.5;
    public static final double OMBRANERA_MOLTIPLICATORE_PARATA = 0.5;
    public static final double OMBRANERA_MOLTIPLICATORE_RESISTENZA_MAGICA = 1.2;
    public static final double OMBRANERA_MOLTIPLICATORE_PERCEZIONE = 1.2;
    public static final double OMBRANERA_MOLTIPLICATORE_SOGGEZIONE = 1.4;
    public static final double OMBRANERA_MOLTIPLICATORE_FURIA = 0.3;
    public static final double OMBRANERA_MOLTIPLICATORE_CORAGGIO = 1.4;
    public static final double OMBRANERA_MOLTIPLICATORE_VALORE = 0.3;
    public static final double OMBRANERA_MOLTIPLICATORE_NUMERO_BERSAGLI = 1.1;
    public static final double OMBRANERA_MOLTIPLICATORE_STANCHEZZA = 0.0;
    public static final double OMBRANERA_MOLTIPLICATORE_DANNI_MAGICI = 1.3;
    public static final double OMBRANERA_MOLTIPLICATORE_DANNI_FISICI = 1.0;
    public static final double OMBRANERA_MOLTIPLICATORE_RECUPERO_FISICO = 0.0;
    public static final double OMBRANERA_MOLTIPLICATORE_RECUPERO_MAGICO = 1.8;

    public static final int SCHELETRO_MAX_CARISMA = 0;
    public static final int SCHELETRO_MAX_NUMERO = 4;
    public static final int SCHELETRO_MAX_FORZA = 18;
    public static final int SCHELETRO_MAX_DESTREZZA = 12;
    public static final int SCHELETRO_MAX_COSTITUZIONE = 18;
    public static final int SCHELETRO_MAX_INTELLIGENZA = 6;
    public static final int SCHELETRO_MAX_SAGGEZZA = 4;
    public static final int SCHELETRO_MAX_FORTUNA = 6;
    public static final int SCHELETRO_PUNTI_ESPERIENZA = 15;

    public static final int SCHELETRO_SALUTE_BASE = 70;
    public static final int SCHELETRO_MAGIA_BASE = 0;
    public static final int SCHELETRO_LIVELLAMENTO_MAGIA = 0;
    public static final double SCHELETRO_MOLTIPLICATORE_CARICO = 1.1;
    public static final double SCHELETRO_MOLTIPLICATORE_CRITICO = 1.1;
    public static final double SCHELETRO_MOLTIPLICATORE_PRECISIONE = 1.0;
    public static final double SCHELETRO_MOLTIPLICATORE_VELOCITA = 1.0;
    public static final double SCHELETRO_MOLTIPLICATORE_FURTIVITA = 0.9;
    public static final double SCHELETRO_MOLTIPLICATORE_PARATA = 1.1;
    public static final double SCHELETRO_MOLTIPLICATORE_RESISTENZA_MAGICA = 0.6;
    public static final double SCHELETRO_MOLTIPLICATORE_PERCEZIONE = 0.6;
    public static final double SCHELETRO_MOLTIPLICATORE_SOGGEZIONE = 0.8;
    public static final double SCHELETRO_MOLTIPLICATORE_FURIA = 0.0;
    public static final double SCHELETRO_MOLTIPLICATORE_CORAGGIO = 1.5;
    public static final double SCHELETRO_MOLTIPLICATORE_VALORE = 0.0;
    public static final double SCHELETRO_MOLTIPLICATORE_NUMERO_BERSAGLI = 0.9;
    public static final double SCHELETRO_MOLTIPLICATORE_STANCHEZZA = 0.0;
    public static final double SCHELETRO_MOLTIPLICATORE_DANNI_MAGICI = 0.5;
    public static final double SCHELETRO_MOLTIPLICATORE_DANNI_FISICI = 0.9;
    public static final double SCHELETRO_MOLTIPLICATORE_RECUPERO_FISICO = 0.0;
    public static final double SCHELETRO_MOLTIPLICATORE_RECUPERO_MAGICO = 0.6;

    public static final int SPETTRO_MAX_CARISMA = 0;
    public static final int SPETTRO_MAX_NUMERO = 2;
    public static final int SPETTRO_MAX_FORZA = 8;
    public static final int SPETTRO_MAX_DESTREZZA = 25;
    public static final int SPETTRO_MAX_COSTITUZIONE = 10;
    public static final int SPETTRO_MAX_INTELLIGENZA = 18;
    public static final int SPETTRO_MAX_SAGGEZZA = 35;
    public static final int SPETTRO_MAX_FORTUNA = 12;
    public static final int SPETTRO_PUNTI_ESPERIENZA = 15;

    public static final int SPETTRO_SALUTE_BASE = 70;
    public static final int SPETTRO_MAGIA_BASE = 50;
    public static final int SPETTRO_LIVELLAMENTO_MAGIA = 25;
    public static final double SPETTRO_MOLTIPLICATORE_CARICO = 0.0;
    public static final double SPETTRO_MOLTIPLICATORE_CRITICO = 0.5;
    public static final double SPETTRO_MOLTIPLICATORE_PRECISIONE = 0.8;
    public static final double SPETTRO_MOLTIPLICATORE_VELOCITA = 1.2;
    public static final double SPETTRO_MOLTIPLICATORE_FURTIVITA = 1.4;
    public static final double SPETTRO_MOLTIPLICATORE_PARATA = 0.3;
    public static final double SPETTRO_MOLTIPLICATORE_RESISTENZA_MAGICA = 1.1;
    public static final double SPETTRO_MOLTIPLICATORE_PERCEZIONE = 1.1;
    public static final double SPETTRO_MOLTIPLICATORE_SOGGEZIONE = 1.3;
    public static final double SPETTRO_MOLTIPLICATORE_FURIA = 0.2;
    public static final double SPETTRO_MOLTIPLICATORE_CORAGGIO = 1.3;
    public static final double SPETTRO_MOLTIPLICATORE_VALORE = 0.2;
    public static final double SPETTRO_MOLTIPLICATORE_NUMERO_BERSAGLI = 1.1;
    public static final double SPETTRO_MOLTIPLICATORE_STANCHEZZA = 0.0;
    public static final double SPETTRO_MOLTIPLICATORE_DANNI_MAGICI = 1.3;
    public static final double SPETTRO_MOLTIPLICATORE_DANNI_FISICI = 0.3;
    public static final double SPETTRO_MOLTIPLICATORE_RECUPERO_FISICO = 0.0;
    public static final double SPETTRO_MOLTIPLICATORE_RECUPERO_MAGICO = 1.4;

    public static final int SPIRITO_MAX_CARISMA = 0;
    public static final int SPIRITO_MAX_NUMERO = 2;
    public static final int SPIRITO_MAX_FORZA = 5;
    public static final int SPIRITO_MAX_DESTREZZA = 22;
    public static final int SPIRITO_MAX_COSTITUZIONE = 8;
    public static final int SPIRITO_MAX_INTELLIGENZA = 15;
    public static final int SPIRITO_MAX_SAGGEZZA = 40;
    public static final int SPIRITO_MAX_FORTUNA = 10;
    public static final int SPIRITO_PUNTI_ESPERIENZA = 15;

    public static final int SPIRITO_SALUTE_BASE = 50;
    public static final int SPIRITO_MAGIA_BASE = 45;
    public static final int SPIRITO_LIVELLAMENTO_MAGIA = 20;
    public static final double SPIRITO_MOLTIPLICATORE_CARICO = 0.0;
    public static final double SPIRITO_MOLTIPLICATORE_CRITICO = 0.5;
    public static final double SPIRITO_MOLTIPLICATORE_PRECISIONE = 0.8;
    public static final double SPIRITO_MOLTIPLICATORE_VELOCITA = 1.1;
    public static final double SPIRITO_MOLTIPLICATORE_FURTIVITA = 1.2;
    public static final double SPIRITO_MOLTIPLICATORE_PARATA = 0.3;
    public static final double SPIRITO_MOLTIPLICATORE_RESISTENZA_MAGICA = 1.2;
    public static final double SPIRITO_MOLTIPLICATORE_PERCEZIONE = 1.2;
    public static final double SPIRITO_MOLTIPLICATORE_SOGGEZIONE = 1.0;
    public static final double SPIRITO_MOLTIPLICATORE_FURIA = 0.9;
    public static final double SPIRITO_MOLTIPLICATORE_CORAGGIO = 1.1;
    public static final double SPIRITO_MOLTIPLICATORE_VALORE = 0.9;
    public static final double SPIRITO_MOLTIPLICATORE_NUMERO_BERSAGLI = 1.0;
    public static final double SPIRITO_MOLTIPLICATORE_STANCHEZZA = 0.0;
    public static final double SPIRITO_MOLTIPLICATORE_DANNI_MAGICI = 1.4;
    public static final double SPIRITO_MOLTIPLICATORE_DANNI_FISICI = 0.2;
    public static final double SPIRITO_MOLTIPLICATORE_RECUPERO_FISICO = 0.0;
    public static final double SPIRITO_MOLTIPLICATORE_RECUPERO_MAGICO = 1.4;

    public static final int TITANO_MAX_CARISMA = 3;
    public static final int TITANO_MAX_NUMERO = 3;
    public static final int TITANO_MAX_FORZA = 48;
    public static final int TITANO_MAX_DESTREZZA = 20;
    public static final int TITANO_MAX_COSTITUZIONE = 50;
    public static final int TITANO_MAX_INTELLIGENZA = 22;
    public static final int TITANO_MAX_SAGGEZZA = 25;
    public static final int TITANO_MAX_FORTUNA = 15;
    public static final int TITANO_PUNTI_ESPERIENZA = 100;

    public static final int TITANO_SALUTE_BASE = 300;
    public static final int TITANO_MAGIA_BASE = 0;
    public static final int TITANO_LIVELLAMENTO_MAGIA = 0;
    public static final double TITANO_MOLTIPLICATORE_CARICO = 10.0;
    public static final double TITANO_MOLTIPLICATORE_CRITICO = 0.4;
    public static final double TITANO_MOLTIPLICATORE_PRECISIONE = 0.5;
    public static final double TITANO_MOLTIPLICATORE_VELOCITA = 0.4;
    public static final double TITANO_MOLTIPLICATORE_FURTIVITA = 0.0;
    public static final double TITANO_MOLTIPLICATORE_PARATA = 1.6;
    public static final double TITANO_MOLTIPLICATORE_RESISTENZA_MAGICA = 1.0;
    public static final double TITANO_MOLTIPLICATORE_PERCEZIONE = 1.0;
    public static final double TITANO_MOLTIPLICATORE_SOGGEZIONE = 1.6;
    public static final double TITANO_MOLTIPLICATORE_FURIA = 1.4;
    public static final double TITANO_MOLTIPLICATORE_CORAGGIO = 1.7;
    public static final double TITANO_MOLTIPLICATORE_VALORE = 1.4;
    public static final double TITANO_MOLTIPLICATORE_CONTRATTAZIONE = 0.8;
    public static final double TITANO_MOLTIPLICATORE_NUMERO_BERSAGLI = 2.0;
    public static final double TITANO_MOLTIPLICATORE_STANCHEZZA = 0.5;
    public static final double TITANO_MOLTIPLICATORE_DANNI_MAGICI = 1.5;
    public static final double TITANO_MOLTIPLICATORE_DANNI_FISICI = 2.0;
    public static final double TITANO_MOLTIPLICATORE_RECUPERO_FISICO = 1.5;
    public static final double TITANO_MOLTIPLICATORE_RECUPERO_MAGICO = 1.4;

    public static final int TROLL_MAX_CARISMA = 0;
    public static final int TROLL_MAX_NUMERO = 3;
    public static final int TROLL_MAX_FORZA = 38;
    public static final int TROLL_MAX_DESTREZZA = 15;
    public static final int TROLL_MAX_COSTITUZIONE = 38;
    public static final int TROLL_MAX_INTELLIGENZA = 10;
    public static final int TROLL_MAX_SAGGEZZA = 10;
    public static final int TROLL_MAX_FORTUNA = 8;
    public static final int TROLL_PUNTI_ESPERIENZA = 40;

    public static final int TROLL_SALUTE_BASE = 170;
    public static final int TROLL_MAGIA_BASE = 0;
    public static final int TROLL_LIVELLAMENTO_MAGIA = 0;
    public static final double TROLL_MOLTIPLICATORE_CARICO = 2.8;
    public static final double TROLL_MOLTIPLICATORE_CRITICO = 0.7;
    public static final double TROLL_MOLTIPLICATORE_PRECISIONE = 0.7;
    public static final double TROLL_MOLTIPLICATORE_VELOCITA = 0.7;
    public static final double TROLL_MOLTIPLICATORE_FURTIVITA = 0.5;
    public static final double TROLL_MOLTIPLICATORE_PARATA = 1.2;
    public static final double TROLL_MOLTIPLICATORE_RESISTENZA_MAGICA = 0.7;
    public static final double TROLL_MOLTIPLICATORE_PERCEZIONE = 0.7;
    public static final double TROLL_MOLTIPLICATORE_SOGGEZIONE = 1.1;
    public static final double TROLL_MOLTIPLICATORE_FURIA = 0.7;
    public static final double TROLL_MOLTIPLICATORE_CORAGGIO = 0.9;
    public static final double TROLL_MOLTIPLICATORE_VALORE = 0.7;
    public static final double TROLL_MOLTIPLICATORE_NUMERO_BERSAGLI = 1.2;
    public static final double TROLL_MOLTIPLICATORE_STANCHEZZA = 0.8;
    public static final double TROLL_MOLTIPLICATORE_DANNI_MAGICI = 0.5;
    public static final double TROLL_MOLTIPLICATORE_DANNI_FISICI = 1.4;
    public static final double TROLL_MOLTIPLICATORE_RECUPERO_FISICO = 1.5;
    public static final double TROLL_MOLTIPLICATORE_RECUPERO_MAGICO = 0.5;

    public static final int VIVERNA_MAX_CARISMA = 0;
    public static final int VIVERNA_MAX_NUMERO = 3;
    public static final int VIVERNA_MAX_FORZA = 40;
    public static final int VIVERNA_MAX_DESTREZZA = 35;
    public static final int VIVERNA_MAX_COSTITUZIONE = 38;
    public static final int VIVERNA_MAX_INTELLIGENZA = 30;
    public static final int VIVERNA_MAX_SAGGEZZA = 28;
    public static final int VIVERNA_MAX_FORTUNA = 15;
    public static final int VIVERNA_PUNTI_ESPERIENZA = 40;

    public static final int VIVERNA_SALUTE_BASE = 130;
    public static final int VIVERNA_MAGIA_BASE = 30;
    public static final int VIVERNA_LIVELLAMENTO_MAGIA = 10;
    public static final double VIVERNA_MOLTIPLICATORE_CARICO = 3.5;
    public static final double VIVERNA_MOLTIPLICATORE_CRITICO = 1.2;
    public static final double VIVERNA_MOLTIPLICATORE_PRECISIONE = 1.1;
    public static final double VIVERNA_MOLTIPLICATORE_VELOCITA = 1.2;
    public static final double VIVERNA_MOLTIPLICATORE_FURTIVITA = 0.8;
    public static final double VIVERNA_MOLTIPLICATORE_PARATA = 0.9;
    public static final double VIVERNA_MOLTIPLICATORE_RESISTENZA_MAGICA = 1.3;
    public static final double VIVERNA_MOLTIPLICATORE_PERCEZIONE = 1.3;
    public static final double VIVERNA_MOLTIPLICATORE_SOGGEZIONE = 1.2;
    public static final double VIVERNA_MOLTIPLICATORE_FURIA = 0.8;
    public static final double VIVERNA_MOLTIPLICATORE_CORAGGIO = 1.0;
    public static final double VIVERNA_MOLTIPLICATORE_VALORE = 0.8;
    public static final double VIVERNA_MOLTIPLICATORE_NUMERO_BERSAGLI = 1.2;
    public static final double VIVERNA_MOLTIPLICATORE_STANCHEZZA = 1.0;
    public static final double VIVERNA_MOLTIPLICATORE_DANNI_MAGICI = 0.8;
    public static final double VIVERNA_MOLTIPLICATORE_DANNI_FISICI = 1.2;
    public static final double VIVERNA_MOLTIPLICATORE_RECUPERO_FISICO = 1.1;
    public static final double VIVERNA_MOLTIPLICATORE_RECUPERO_MAGICO = 0.8;

    // Boss

    public static final int IDRA_MAX_CARISMA = 0;
    public static final int IDRA_MAX_FORZA = 60;
    public static final int IDRA_MAX_DESTREZZA = 30;
    public static final int IDRA_MAX_COSTITUZIONE = 60;
    public static final int IDRA_MAX_INTELLIGENZA = 25;
    public static final int IDRA_MAX_SAGGEZZA = 20;
    public static final int IDRA_MAX_FORTUNA = 15;
    public static final int IDRA_MAX_NUMERO = 1;
    public static final int IDRA_PUNTI_ESPERIENZA = 70;

    public static final int IDRA_SALUTE_BASE = 180;
    public static final int IDRA_MAGIA_BASE = 10;
    public static final int IDRA_LIVELLAMENTO_MAGIA = 4;
    public static final double IDRA_MOLTIPLICATORE_CARICO = 5.0;
    public static final double IDRA_MOLTIPLICATORE_CRITICO = 0.6;
    public static final double IDRA_MOLTIPLICATORE_PRECISIONE = 0.8;
    public static final double IDRA_MOLTIPLICATORE_VELOCITA = 0.8;
    public static final double IDRA_MOLTIPLICATORE_FURTIVITA = 0.4;
    public static final double IDRA_MOLTIPLICATORE_PARATA = 1.0;
    public static final double IDRA_MOLTIPLICATORE_RESISTENZA_MAGICA = 1.3;
    public static final double IDRA_MOLTIPLICATORE_PERCEZIONE = 1.3;
    public static final double IDRA_MOLTIPLICATORE_SOGGEZIONE = 1.3;
    public static final double IDRA_MOLTIPLICATORE_FURIA = 0.5;
    public static final double IDRA_MOLTIPLICATORE_CORAGGIO = 1.1;
    public static final double IDRA_MOLTIPLICATORE_VALORE = 0.5;
    public static final double IDRA_MOLTIPLICATORE_NUMERO_BERSAGLI = 1.9;
    public static final double IDRA_MOLTIPLICATORE_STANCHEZZA = 0.9;
    public static final double IDRA_MOLTIPLICATORE_DANNI_MAGICI = 0.9;
    public static final double IDRA_MOLTIPLICATORE_DANNI_FISICI = 1.4;
    public static final double IDRA_MOLTIPLICATORE_RECUPERO_FISICO = 1.5;
    public static final double IDRA_MOLTIPLICATORE_RECUPERO_MAGICO = 0.8;

    public static final int LICH_MAX_CARISMA = 0;
    public static final int LICH_MAX_FORZA = 25;
    public static final int LICH_MAX_DESTREZZA = 35;
    public static final int LICH_MAX_COSTITUZIONE = 38;
    public static final int LICH_MAX_INTELLIGENZA = 70;
    public static final int LICH_MAX_SAGGEZZA = 55;
    public static final int LICH_MAX_FORTUNA = 20;
    public static final int LICH_MAX_NUMERO = 1;
    public static final int LICH_PUNTI_ESPERIENZA = 100;

    public static final int LICH_SALUTE_BASE = 75;
    public static final int LICH_MAGIA_BASE = 80;
    public static final int LICH_LIVELLAMENTO_MAGIA = 45;
    public static final double LICH_MOLTIPLICATORE_CARICO = 1.0;
    public static final double LICH_MOLTIPLICATORE_CRITICO = 1.0;
    public static final double LICH_MOLTIPLICATORE_PRECISIONE = 1.1;
    public static final double LICH_MOLTIPLICATORE_VELOCITA = 0.9;
    public static final double LICH_MOLTIPLICATORE_FURTIVITA = 0.8;
    public static final double LICH_MOLTIPLICATORE_PARATA = 0.7;
    public static final double LICH_MOLTIPLICATORE_RESISTENZA_MAGICA = 1.2;
    public static final double LICH_MOLTIPLICATORE_PERCEZIONE = 1.2;
    public static final double LICH_MOLTIPLICATORE_SOGGEZIONE = 1.5;
    public static final double LICH_MOLTIPLICATORE_FURIA = 0.2;
    public static final double LICH_MOLTIPLICATORE_CORAGGIO = 1.6;
    public static final double LICH_MOLTIPLICATORE_VALORE = 0.2;
    public static final double LICH_MOLTIPLICATORE_NUMERO_BERSAGLI = 5.0;
    public static final double LICH_MOLTIPLICATORE_STANCHEZZA = 0.0;
    public static final double LICH_MOLTIPLICATORE_DANNI_MAGICI = 3;
    public static final double LICH_MOLTIPLICATORE_DANNI_FISICI = 0.5;
    public static final double LICH_MOLTIPLICATORE_RECUPERO_FISICO = 0.0;
    public static final double LICH_MOLTIPLICATORE_RECUPERO_MAGICO = 1.6;

    // Il Necromante: avversario umano oscuro, un mago che ha fatto dei morti il suo esercito. Come il Mago per forza e magia, ma più temibile a vedersi
    public static final int NECROMANTE_MAX_CARISMA = 6;
    public static final int NECROMANTE_MAX_FORZA = 15;
    public static final int NECROMANTE_MAX_DESTREZZA = 20;
    public static final int NECROMANTE_MAX_COSTITUZIONE = 20;
    public static final int NECROMANTE_MAX_INTELLIGENZA = 55;
    public static final int NECROMANTE_MAX_SAGGEZZA = 40;
    public static final int NECROMANTE_MAX_FORTUNA = 10;
    public static final int NECROMANTE_MAX_NUMERO = 1;
    public static final int NECROMANTE_PUNTI_ESPERIENZA = 40;

    public static final int NECROMANTE_SALUTE_BASE = 80;
    public static final int NECROMANTE_MAGIA_BASE = 60;
    public static final int NECROMANTE_LIVELLAMENTO_MAGIA = 34;
    public static final double NECROMANTE_MOLTIPLICATORE_CARICO = 0.8;
    public static final double NECROMANTE_MOLTIPLICATORE_CRITICO = 0.8;
    public static final double NECROMANTE_MOLTIPLICATORE_PRECISIONE = 1.0;
    public static final double NECROMANTE_MOLTIPLICATORE_VELOCITA = 0.9;
    public static final double NECROMANTE_MOLTIPLICATORE_FURTIVITA = 0.9;
    public static final double NECROMANTE_MOLTIPLICATORE_PARATA = 0.6;
    public static final double NECROMANTE_MOLTIPLICATORE_RESISTENZA_MAGICA = 1.1;
    public static final double NECROMANTE_MOLTIPLICATORE_PERCEZIONE = 1.1;
    public static final double NECROMANTE_MOLTIPLICATORE_SOGGEZIONE = 1.3;
    public static final double NECROMANTE_MOLTIPLICATORE_FURIA = 0.8;
    public static final double NECROMANTE_MOLTIPLICATORE_CORAGGIO = 1.2;
    public static final double NECROMANTE_MOLTIPLICATORE_VALORE = 1.0;
    public static final double NECROMANTE_MOLTIPLICATORE_CONTRATTAZIONE = 1.0;
    public static final double NECROMANTE_MOLTIPLICATORE_NUMERO_BERSAGLI = 1.3;
    public static final double NECROMANTE_MOLTIPLICATORE_STANCHEZZA = 1.4;
    public static final double NECROMANTE_MOLTIPLICATORE_DANNI_MAGICI = 2.0;
    public static final double NECROMANTE_MOLTIPLICATORE_DANNI_FISICI = 0.5;
    public static final double NECROMANTE_MOLTIPLICATORE_RECUPERO_FISICO = 0.7;
    public static final double NECROMANTE_MOLTIPLICATORE_RECUPERO_MAGICO = 1.5;

    public static final int MINOTAUROGIGANTE_MAX_CARISMA = 0;
    public static final int MINOTAUROGIGANTE_MAX_FORZA = 70;
    public static final int MINOTAUROGIGANTE_MAX_DESTREZZA = 30;
    public static final int MINOTAUROGIGANTE_MAX_COSTITUZIONE = 65;
    public static final int MINOTAUROGIGANTE_MAX_INTELLIGENZA = 20;
    public static final int MINOTAUROGIGANTE_MAX_SAGGEZZA = 20;
    public static final int MINOTAUROGIGANTE_MAX_FORTUNA = 15;
    public static final int MINOTAUROGIGANTE_MAX_NUMERO = 1;
    public static final int MINOTAUROGIGANTE_PUNTI_ESPERIENZA = 70;

    public static final int MINOTAUROGIGANTE_SALUTE_BASE = 230;
    public static final int MINOTAUROGIGANTE_MAGIA_BASE = 0;
    public static final int MINOTAUROGIGANTE_LIVELLAMENTO_MAGIA = 0;
    public static final double MINOTAUROGIGANTE_MOLTIPLICATORE_CARICO = 4.0;
    public static final double MINOTAUROGIGANTE_MOLTIPLICATORE_CRITICO = 0.5;
    public static final double MINOTAUROGIGANTE_MOLTIPLICATORE_PRECISIONE = 0.6;
    public static final double MINOTAUROGIGANTE_MOLTIPLICATORE_VELOCITA = 0.5;
    public static final double MINOTAUROGIGANTE_MOLTIPLICATORE_FURTIVITA = 0.2;
    public static final double MINOTAUROGIGANTE_MOLTIPLICATORE_PARATA = 1.4;
    public static final double MINOTAUROGIGANTE_MOLTIPLICATORE_RESISTENZA_MAGICA = 0.6;
    public static final double MINOTAUROGIGANTE_MOLTIPLICATORE_PERCEZIONE = 0.6;
    public static final double MINOTAUROGIGANTE_MOLTIPLICATORE_SOGGEZIONE = 1.5;
    public static final double MINOTAUROGIGANTE_MOLTIPLICATORE_FURIA = 0.5;
    public static final double MINOTAUROGIGANTE_MOLTIPLICATORE_CORAGGIO = 1.3;
    public static final double MINOTAUROGIGANTE_MOLTIPLICATORE_VALORE = 0.5;
    public static final double MINOTAUROGIGANTE_MOLTIPLICATORE_NUMERO_BERSAGLI = 1.6;
    public static final double MINOTAUROGIGANTE_MOLTIPLICATORE_STANCHEZZA = 1.4;
    public static final double MINOTAUROGIGANTE_MOLTIPLICATORE_DANNI_MAGICI = 0.4;
    public static final double MINOTAUROGIGANTE_MOLTIPLICATORE_DANNI_FISICI = 1.7;
    public static final double MINOTAUROGIGANTE_MOLTIPLICATORE_RECUPERO_FISICO = 1.4;
    public static final double MINOTAUROGIGANTE_MOLTIPLICATORE_RECUPERO_MAGICO = 0.4;

    public static final int STREGA_MAX_CARISMA = 0;
    public static final int STREGA_MAX_FORZA = 25;
    public static final int STREGA_MAX_DESTREZZA = 30;
    public static final int STREGA_MAX_COSTITUZIONE = 40;
    public static final int STREGA_MAX_INTELLIGENZA = 65;
    public static final int STREGA_MAX_SAGGEZZA = 55;
    public static final int STREGA_MAX_FORTUNA = 20;
    public static final int STREGA_MAX_NUMERO = 1;
    public static final int STREGA_PUNTI_ESPERIENZA = 100;

    public static final int STREGA_SALUTE_BASE = 85;
    public static final int STREGA_MAGIA_BASE = 55;
    public static final int STREGA_LIVELLAMENTO_MAGIA = 32;
    public static final double STREGA_MOLTIPLICATORE_CARICO = 0.95;
    public static final double STREGA_MOLTIPLICATORE_CRITICO = 0.8;
    public static final double STREGA_MOLTIPLICATORE_PRECISIONE = 0.9;
    public static final double STREGA_MOLTIPLICATORE_VELOCITA = 0.9;
    public static final double STREGA_MOLTIPLICATORE_FURTIVITA = 1.0;
    public static final double STREGA_MOLTIPLICATORE_PARATA = 0.6;
    public static final double STREGA_MOLTIPLICATORE_RESISTENZA_MAGICA = 1.4;
    public static final double STREGA_MOLTIPLICATORE_PERCEZIONE = 1.3;
    public static final double STREGA_MOLTIPLICATORE_SOGGEZIONE = 1.2;
    public static final double STREGA_MOLTIPLICATORE_FURIA = 0.8;
    public static final double STREGA_MOLTIPLICATORE_CORAGGIO = 1.1;
    public static final double STREGA_MOLTIPLICATORE_VALORE = 0.8;
    public static final double STREGA_MOLTIPLICATORE_NUMERO_BERSAGLI = 5.0;
    public static final double STREGA_MOLTIPLICATORE_STANCHEZZA = 1.4;
    public static final double STREGA_MOLTIPLICATORE_DANNI_MAGICI = 3.0;
    public static final double STREGA_MOLTIPLICATORE_DANNI_FISICI = 0.6;
    public static final double STREGA_MOLTIPLICATORE_RECUPERO_FISICO = 0.8;
    public static final double STREGA_MOLTIPLICATORE_RECUPERO_MAGICO = 1.4;

    // Boss finale

    public static final int DRAGO_MAX_CARISMA = 8;
    public static final int DRAGO_MAX_FORZA = 80;
    public static final int DRAGO_MAX_DESTREZZA = 50;
    public static final int DRAGO_MAX_COSTITUZIONE = 75;
    public static final int DRAGO_MAX_INTELLIGENZA = 80;
    public static final int DRAGO_MAX_SAGGEZZA = 70;
    public static final int DRAGO_MAX_FORTUNA = 35;
    public static final int DRAGO_MAX_NUMERO = 1;
    public static final int DRAGO_PUNTI_ESPERIENZA = 250;

    public static final int DRAGO_SALUTE_BASE = 200;
    public static final int DRAGO_MAGIA_BASE = 30;
    public static final int DRAGO_LIVELLAMENTO_MAGIA = 8;
    public static final double DRAGO_MOLTIPLICATORE_CARICO = 8.0;
    public static final double DRAGO_MOLTIPLICATORE_CRITICO = 1.0;
    public static final double DRAGO_MOLTIPLICATORE_PRECISIONE = 1.2;
    public static final double DRAGO_MOLTIPLICATORE_VELOCITA = 1.1;
    public static final double DRAGO_MOLTIPLICATORE_FURTIVITA = 0.5;
    public static final double DRAGO_MOLTIPLICATORE_PARATA = 1.4;
    public static final double DRAGO_MOLTIPLICATORE_RESISTENZA_MAGICA = 1.5;
    public static final double DRAGO_MOLTIPLICATORE_PERCEZIONE = 1.5;
    public static final double DRAGO_MOLTIPLICATORE_SOGGEZIONE = 1.6;
    public static final double DRAGO_MOLTIPLICATORE_FURIA = 1.1;
    public static final double DRAGO_MOLTIPLICATORE_CORAGGIO = 1.6;
    public static final double DRAGO_MOLTIPLICATORE_VALORE = 1.1;
    public static final double DRAGO_MOLTIPLICATORE_NUMERO_BERSAGLI = 5.0;
    public static final double DRAGO_MOLTIPLICATORE_STANCHEZZA = 0.7;
    public static final double DRAGO_MOLTIPLICATORE_DANNI_MAGICI = 3.0;
    public static final double DRAGO_MOLTIPLICATORE_DANNI_FISICI = 1.7;
    public static final double DRAGO_MOLTIPLICATORE_RECUPERO_FISICO = 1.4;
    public static final double DRAGO_MOLTIPLICATORE_RECUPERO_MAGICO = 1.5;

    
}
