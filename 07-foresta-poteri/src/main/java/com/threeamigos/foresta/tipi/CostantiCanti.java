package com.threeamigos.foresta.tipi;

/**
 * I valori di bilanciamento dei canti: costi di lancio, effetti e durate.
 *
 * @author Stefano Reksten
 */
public final class CostantiCanti {

    /**
     * I canti innati del Bardo e della Cantastorie (vedi personaggi_giocabili.md): costano MAGIA, non consumano pergamene
     */
    public static final int CANTO_CURATIVO_COSTO_LANCIO = 6;

    /**
     * La ricarica di ogni canto: i turni (round completi di combattimento, o passi fra una locazione e l'altra) che devono passare prima di
     * poterlo cantare di nuovo; uno in più della durata di quel che dà, per non poterlo rinnovare di continuo.
     * Il canto curativo cura più dell'imposizione delle mani del chierico, e quindi costa di più e ha la stessa ricarica
     */
    public static final int CANTO_CURATIVO_RICARICA = 4;

    public static final int CANTO_IPNOTICO_COSTO_LANCIO = 3;

    /** SPAVENTATO e CONFUSO durano al più 3 turni */
    public static final int CANTO_IPNOTICO_RICARICA = 4;

    public static final int CANTO_DI_VALORE_COSTO_LANCIO = 5;

    /**
     * L'Urlo della Banshee, il canto ipnotico potenziato: costa di più, la SOGGEZIONE del cantore vale di più contro il CORAGGIO
     * di chi ascolta, e chi cede prende SPAVENTATO e CONFUSO insieme
     */
    public static final int CANTO_URLO_DELLA_BANSHEE_COSTO_LANCIO = 6;

    /** Più forte del canto ipnotico, e quindi si ricarica più lentamente */
    public static final int CANTO_URLO_DELLA_BANSHEE_RICARICA = 5;

    public static final double CANTO_URLO_DELLA_BANSHEE_MOLTIPLICATORE_SOGGEZIONE = 1.5;

    /**
     * Il canto curativo guarisce CANTO_CURATIVO_BASE × √livello, aumentato della SAGGEZZA divisa per questo valore
     */
    public static final double CANTO_CURATIVO_BASE = 30.0;

    /**
     * Il canto di valore dà ISPIRATO a tutto il gruppo: coraggio e precisione crescono di CANTO_DI_VALORE_PERCENTUALE_BASE
     * più CANTO_DI_VALORE_PERCENTUALE_PER_LIVELLO per ogni livello del cantore, al più CANTO_DI_VALORE_PERCENTUALE_MASSIMA,
     * per CANTO_DI_VALORE_DURATA turni
     */
    public static final double CANTO_DI_VALORE_PERCENTUALE_BASE = 20.0;

    public static final double CANTO_DI_VALORE_PERCENTUALE_PER_LIVELLO = 2.5;

    public static final double CANTO_DI_VALORE_PERCENTUALE_MASSIMA = 50.0;

    public static final int CANTO_DI_VALORE_DURATA = 4;

    /** Uno in più della durata, per non poterlo rinnovare di continuo */
    public static final int CANTO_DI_VALORE_RICARICA = CANTO_DI_VALORE_DURATA + 1;

    public static final double CANTO_CURATIVO_DIVISORE_SAGGEZZA = 50.0;

    private CostantiCanti() {
    }
}
