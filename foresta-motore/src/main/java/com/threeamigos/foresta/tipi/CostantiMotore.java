package com.threeamigos.foresta.tipi;

/**
 * I valori di bilanciamento che decide il motore: le locazioni, gli avversari e le locande. Quelli delle altre parti stanno
 * nelle loro costanti ({@code CostantiPersonaggi}, {@code CostantiArtefatti}, {@code CostantiBottino}, {@code CostantiEconomia}...).
 *
 * @author Stefano Reksten
 */
public final class CostantiMotore {

    private CostantiMotore() {
    }

    // Amicizia e corruzione
    // Il tentativo di amicizia riesce se il carisma di chi lo fa è maggiore del tiro di un dado con queste facce: con un
    // carisma di una faccia in più il tentativo riesce sempre
    // Il tentativo di corruzione, se il gruppo ha le monete per pagare, riesce se il tiro di un dado con queste facce supera
    // la soglia: 7 volte su 10 con questi valori
    public static final int FACCE_DEL_DADO_DELLA_CORRUZIONE = 10;
    public static final int SOGLIA_DEL_DADO_DELLA_CORRUZIONE = 3;

    // Inizio morbido (vedi LocazioneBase.crea): finché il capo del gruppo è a questo livello o sotto, si incontrano al
    // massimo tanti mostri quanti i personaggi del gruppo, e mai uno dei mostri di MOSTRI_ESCLUSI_A_INIZIO_PARTITA
    public static final int INIZIO_MORBIDO_FINO_AL_LIVELLO = 3;

    // Il necromante, finché è vivo, evoca uno scheletro ogni volta che ne cadono due; al più tante evocazioni per scontro
    public static final int NECROMANTE_SCHELETRI_PER_EVOCAZIONE = 2;
    public static final int NECROMANTE_EVOCAZIONI_MASSIME = 4;

    // In locanda: quante informazioni si possono ottenere in una stessa locanda, in tutta la partita (i prezzi in CostantiEconomia)
    public static final int LOCANDA_MASSIMO_INFORMAZIONI = 3;

    // Castelli
    public static final int COFANI_IN_CASTELLO_IDRA = 5;
    public static final int COFANI_IN_CASTELLO_LICH = 5;
    public static final int COFANI_IN_CASTELLO_MINOTAURO = 5;
    public static final int COFANI_IN_CASTELLO_STREGA = 5;
}
