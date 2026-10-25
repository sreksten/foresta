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

    // In locanda: quante informazioni si possono ottenere in una stessa locanda, in tutta la partita (i prezzi in CostantiEconomia)
    public static final int LOCANDA_MASSIMO_INFORMAZIONI = 3;

    // Castelli
    public static final int COFANI_IN_CASTELLO_IDRA = 5;
    public static final int COFANI_IN_CASTELLO_LICH = 5;
    public static final int COFANI_IN_CASTELLO_MINOTAURO = 5;
    public static final int COFANI_IN_CASTELLO_STREGA = 5;

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
