package com.threeamigos.foresta.tipi;

/**
 * I valori di bilanciamento delle missioni e della sfida a carta, forbici e sasso.
 *
 * @author Stefano Reksten
 */
public final class CostantiMissioni {

    /**
     * Quante delle missioni a tappe (vedi MissioneATappe e tappe.txt) sono delle farse: circa una su tre
     */
    public static final int PERCENTUALE_PROBABILITA_FARSA_NELLE_TAPPE = 33;

    // Carta, forbici e sasso (vedi carta_forbici_sasso.md)
    // La probabilità, in percentuale, che chi stringe amicizia venga sfidato invece di ricevere un'offerta, finché la
    // missione La sfida dei campioni non è partita
    // Le mani da vincere per vincere la sfida
    // Le sfide vinte dal gruppo, in tutta la partita, dopo le quali l'avversario lo manda al torneo
    public static final int PERCENTUALE_PROBABILITA_CARTA_FORBICI_SASSO = 33;
    public static final int VITTORIE_PER_VINCERE_LA_SFIDA = 3;
    public static final int SFIDE_VINTE_PER_IL_TORNEO = 3;

    // La forgia (vedi LaForgia): quante monete costa far forgiare l'arma per ogni livello del mondo, quante ore passano a forgiarla
    // (il gruppo le passa sul posto) e quante ore di gioco devono passare perché la lama si raffreddi, e di quanto il suo danno
    // supera quello di un'arma comune del suo livello (tarate il 2026-10-21: vedi economia.md, "La forgia")
    public static final int FORGIA_MONETE_PER_LIVELLO = 10;
    public static final int FORGIA_ORE_DI_LAVORO = 6;
    public static final int FORGIA_ORE_DI_ATTESA = 12;
    public static final double FORGIA_MOLTIPLICATORE_DANNI = 1.4d;

    private CostantiMissioni() {
    }
}
