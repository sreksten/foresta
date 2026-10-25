package com.threeamigos.foresta.tipi;

/**
 * Le costanti di bilanciamento che non appartengono ancora a un sottoprogetto. Quelle dei personaggi (i valori di partenza di ogni classe, i
 * moltiplicatori, il combattimento, l'equipaggiamento) stanno in {@code CostantiPersonaggi}, quelle degli artefatti in
 * {@code CostantiArtefatti}, quelle dei poteri in {@code CostantiIncantesimi}, {@code CostantiCanti} e {@code CostantiAbilitaInnate}.
 *
 * @author Stefano Reksten
 */
public class Costanti {

    private Costanti() {
    }

    // Amicizia e corruzione
    // Il tentativo di amicizia riesce se il carisma di chi lo fa è maggiore del tiro di un dado con queste facce: con un
    // carisma di una faccia in più il tentativo riesce sempre
    // Il tentativo di corruzione, se il gruppo ha le monete per pagare, riesce se il tiro di un dado con queste facce supera
    // la soglia: 7 volte su 10 con questi valori
    public static final int FACCE_DEL_DADO_DELLA_CORRUZIONE = 10;
    public static final int SOGLIA_DEL_DADO_DELLA_CORRUZIONE = 3;


    // Artefatti
    // Il tetto al numero di effetti di un artefatto secondo la rarità sta in TipoRaritaArtefatto

    // Inizio morbido (vedi LocazioneBase.crea): finché il capo del gruppo è a questo livello o sotto, si incontrano al
    // massimo tanti mostri quanti i personaggi del gruppo, e mai uno dei mostri di MOSTRI_ESCLUSI_A_INIZIO_PARTITA
    public static final int INIZIO_MORBIDO_FINO_AL_LIVELLO = 3;

    // Contrattazione (vedi RegoleContrattazione): lo sconto sugli acquisti e il ricavo delle vendite crescono con
    // bonus = contrattazione / (contrattazione + saturazione), fino a un limite che nessun modificatore supera.
    // Con la vendita al massimo al 75% e l'acquisto almeno all'80% non si guadagna comprando e rivendendo.
    public static final int CONTRATTAZIONE_SATURAZIONE = 4;
    public static final double CONTRATTAZIONE_COEFFICIENTE_SCONTO = 0.24;
    public static final double CONTRATTAZIONE_SCONTO_MASSIMO = 0.20;
    public static final double CONTRATTAZIONE_QUOTA_VENDITA_BASE = 0.50;
    public static final double CONTRATTAZIONE_COEFFICIENTE_VENDITA = 0.30;
    public static final double CONTRATTAZIONE_QUOTA_VENDITA_MASSIMA = 0.75;

    // Magazzini dei negozi di città, riforniti alla creazione del mondo e a ogni aumento del suo livello:
    // quanti artefatti per negozio, con livelli a rotazione fra livello del mondo - MAGAZZINO_DIVARIO_LIVELLO
    // e livello del mondo + MAGAZZINO_DIVARIO_LIVELLO (mai sotto 1)
    // A ogni aumento del livello del mondo l'armaiolo scarta gli artefatti di livello inferiore al livello
    // del mondo meno questo scarto
    public static final int MAGAZZINO_ARTEFATTI_ARMAIOLO = 6;
    public static final int MAGAZZINO_PERGAMENE = 6;
    public static final int MAGAZZINO_DIVARIO_LIVELLO = 1;
    public static final int MAGAZZINO_SCARTO_SOTTO_LIVELLO = 2;

    // Il gruppo avversario porta con sé pergamene (vedi BottinoDegliAvversari): la probabilità, per ogni avversario, di averne una...
    public static final int PROBABILITA_PERGAMENA_PER_AVVERSARIO = 20;
    // ...e, se è un umanoide, la probabilità di una pozione della Salute
    public static final int PROBABILITA_POZIONE_SALUTE_PER_AVVERSARIO_UMANOIDE = 20;

    // Il bottino (anelli, cofani): in CostantiBottino

    // Gruppo
    // Oltre ai 5 permanenti, fino a 3 slot aggiuntivi per personaggi temporanei
    // (AiutoGratuito, AiutoMercenario, lo Scheletro da MIETITURA)
    // Il necromante, finché è vivo, evoca uno scheletro ogni volta che ne cadono due; al più tante evocazioni per scontro
    // Per quanti turni uno Scheletro nato dalla MIETITURA accompagna il gruppo
    public static final int NECROMANTE_SCHELETRI_PER_EVOCAZIONE = 2;
    public static final int NECROMANTE_EVOCAZIONI_MASSIME = 4;

    // Dall'alchimista
    public static final int COSTO_POZIONE_SALUTE = 5;
    public static final int COSTO_POZIONE_SALUTE_GRANDE = 15;
    public static final int COSTO_POZIONE_MAGIA = 10;
    public static final int COSTO_POZIONE_MAGIA_GRANDE = 30;
    public static final int COSTO_AUMENTO_MAGIA_GIOCATORE_SINGOLO = 10;

    // In locanda
    // Quante informazioni si possono ottenere in una stessa locanda, in tutta la partita
    public static final int COSTO_PASTO = 5;
    public static final int LOCANDA_MASSIMO_INFORMAZIONI = 3;
    public static final int COSTO_PERNOTTAMENTO = 5;
    public static final int RECUPERO_SALUTE_DA_PASTO = 100;

    // Castelli
    public static final int COFANI_IN_CASTELLO_IDRA = 5;
    public static final int COFANI_IN_CASTELLO_LICH = 5;
    public static final int COFANI_IN_CASTELLO_MINOTAURO = 5;
    public static final int COFANI_IN_CASTELLO_STREGA = 5;


    // Foresta
    public static final int COSTO_MAPPA_DELLA_ZONA = 10;
    public static final int COSTO_MAPPA_DELLA_FORESTA = 20;

    // Notizie e messaggi ricordati: vedi NotizieMD.MASSIMO_MESSAGGI_RICORDATI e MASSIMO_NOTIZIE_RICORDATE

    // Intermezzi: i secondi per pagina in intermezzi.Intermezzo.SECONDI_PER_PAGINA, la rivelazione di un artefatto in
    // PaginaIntermezzo.SECONDI_RIVELAZIONE_ARTEFATTO

    // Statistiche dei vari tipi di personaggio

    // Personaggi giocabili

    // Personaggio giocabile segreto per test

    // Personaggi non giocabili (avversari). Alcuno possono essere amichevoli o corrompibili e offrire supporto

    // Boss

    // Boss finale
}
