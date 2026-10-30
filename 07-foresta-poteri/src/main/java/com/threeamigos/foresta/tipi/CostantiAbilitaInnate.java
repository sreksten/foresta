package com.threeamigos.foresta.tipi;

/**
 * I valori di bilanciamento delle abilità innate: quelle del Chierico e del Druido e il dardo arcano di Mago ed Elfo (costi di lancio, danni, durate e basi dei loro effetti).
 *
 * @author Stefano Reksten
 */
public final class CostantiAbilitaInnate {

    /**
     * Le abilità innate di Chierico, Druido, Sacerdote, Mago ed Elfo (vedi TipoAbilita): costano MAGIA, non consumano pergamene
     */
    public static final int ABILITA_SCACCIARE_I_NON_MORTI_COSTO_LANCIO = 4;

    /**
     * La ricarica di ogni abilità: i turni (round completi di combattimento, o passi fra una locazione e l'altra) che devono passare prima di
     * poterla riusare. Un'abilità che dà uno stato o un buff per un certo numero di turni si ricarica in uno di più, perché chi lo subisce non
     * resti senza respiro e chi lo dà non lo rinnovi di continuo; con zero non c'è ricarica.
     * Scacciare i non morti: SPAVENTATO dura al più 3 turni (vedi CalcolatoreCombattimento.calcolaDurataStato)
     */
    public static final int ABILITA_SCACCIARE_I_NON_MORTI_RICARICA = 4;

    /**
     * Scacciare i non morti: ognuno cede con la SOGGEZIONE del chierico contro il suo CORAGGIO, e prende SPAVENTATO
     */
    public static final int ABILITA_IMPOSIZIONE_DELLE_MANI_COSTO_LANCIO = 5;

    /** Cura istantanea e discretamente potente */
    public static final int ABILITA_IMPOSIZIONE_DELLE_MANI_RICARICA = 4;

    /**
     * L'imposizione delle mani cura ABILITA_IMPOSIZIONE_DELLE_MANI_BASE × √livello, aumentato della SAGGEZZA divisa per
     * ABILITA_IMPOSIZIONE_DELLE_MANI_DIVISORE_SAGGEZZA (meno del canto curativo: il chierico ha meno magia del bardo)
     */
    public static final double ABILITA_IMPOSIZIONE_DELLE_MANI_BASE = 20.0;

    public static final double ABILITA_IMPOSIZIONE_DELLE_MANI_DIVISORE_SAGGEZZA = 50.0;

    /**
     * Radici e Sciame: danni di Terra e di Veleno su più avversari, un poco meno di una pergamena e per un poco più di MAGIA
     */
    public static final int ABILITA_RADICI_COSTO_LANCIO = 3;

    /** Le radici immobilizzano o accecano per al più 3 turni: i mostri devono poter muoversi di nuovo prima di un altro colpo */
    public static final int ABILITA_RADICI_RICARICA = 4;

    public static final int ABILITA_RADICI_DANNI = 40;

    public static final int ABILITA_SCIAME_COSTO_LANCIO = 3;

    /** Lo sciame avvelena per 4 turni */
    public static final int ABILITA_SCIAME_RICARICA = 5;

    public static final int ABILITA_SCIAME_DANNI = 40;

    /**
     * La pelle di corteccia dà a tutto il gruppo, per ABILITA_PELLE_DI_CORTECCIA_DURATA turni, un numero fisso di punti in più di parata
     * e di resistenza magica (con un modificatore temporaneo): la base per la radice quadrata del livello del druido, come l'imposizione delle
     * mani. Sono punti e non una percentuale perché chi non è un guerriero ha pochissima parata e resistenza magica (da 1 a 18 punti ai livelli
     * da 3 a 8): una percentuale su quei valori non cambia la probabilità di essere colpiti (vedi personaggi_giocabili.md)
     */
    public static final int ABILITA_PELLE_DI_CORTECCIA_COSTO_LANCIO = 5;

    public static final double ABILITA_PELLE_DI_CORTECCIA_BASE_PARATA = 10.0;

    public static final double ABILITA_PELLE_DI_CORTECCIA_BASE_RESISTENZA_MAGICA = 10.0;

    public static final int ABILITA_PELLE_DI_CORTECCIA_DURATA = 4;

    /** Uno in più della durata, per non poterla rinnovare di continuo */
    public static final int ABILITA_PELLE_DI_CORTECCIA_RICARICA = ABILITA_PELLE_DI_CORTECCIA_DURATA + 1;

    /**
     * La rigenerazione dà RIGENERAZIONE a un personaggio per ABILITA_RIGENERAZIONE_DURATA turni: a ogni turno guarisce di
     * ABILITA_RIGENERAZIONE_BASE più questa quota della sua salute massima, aumentata dalla SAGGEZZA del druido
     */
    public static final int ABILITA_RIGENERAZIONE_COSTO_LANCIO = 4;

    public static final int ABILITA_RIGENERAZIONE_DURATA = 4;

    /** Uno in più della durata, per non poterla rinnovare di continuo */
    public static final int ABILITA_RIGENERAZIONE_RICARICA = ABILITA_RIGENERAZIONE_DURATA + 1;

    public static final int ABILITA_RIGENERAZIONE_BASE = 5;

    public static final double ABILITA_RIGENERAZIONE_QUOTA_SALUTE_MASSIMA = 0.05;

    public static final double ABILITA_RIGENERAZIONE_DIVISORE_SAGGEZZA = 50.0;

    /**
     * L'alba sacra del Sacerdote: toglie al gruppo gli stati alterati negativi e i modificatori temporanei.
     */
    public static final int ABILITA_ALBA_SACRA_COSTO_LANCIO = 5;

    public static final int ABILITA_ALBA_SACRA_RICARICA = 4;

    /**
     * La luce divina del Sacerdote: danno sacro su più avversari (il doppio ai non morti), e investe anche i non morti del proprio gruppo.
     */

    /** Il danno sacro acceca per al più 3 turni */
    public static final int ABILITA_LUCE_DIVINA_RICARICA = 4;

    public static final int ABILITA_LUCE_DIVINA_DANNI = 35;

    /**
     * Il costo del Fulmine in proporzione ai danni: il Fulmine costa 10 per 100 di danno, la luce divina ne fa 35 e costa quanto basta per lo stesso rapporto,
     * arrotondato per eccesso (3,5 → 4)
     */
    public static final int ABILITA_LUCE_DIVINA_COSTO_LANCIO = (int) Math.ceil(
            (double) CostantiIncantesimi.INCANTESIMO_FULMINE_COSTO_LANCIO * ABILITA_LUCE_DIVINA_DANNI / CostantiIncantesimi.INCANTESIMO_FULMINE_DANNI);

    /**
     * Il dardo arcano, l'incantesimo innato di Mago ed Elfo: su un solo bersaglio, per poca MAGIA e senza consumare
     * pergamene. Al Mago rende di più (una pergamena di fuoco fa 50) e costa meno che all'Elfo, che è
     * un po' meno bravo con la magia e in cambio sa combattere
     */
    public static final int DARDO_ARCANO_DANNI_MAGO = 20;

    public static final int DARDO_ARCANO_DANNI_ELFO = 15;

    /**
     * Quanto cresce, a ogni livello oltre il primo, il danno base del dardo dell'Elfo: non avendo il bastone né il libro (che dànno al Mago il
     * potere magico) il suo dardo altrimenti non terrebbe il passo con l'attacco con le armi
     */
    public static final int DARDO_ARCANO_DANNI_ELFO_PER_LIVELLO = 4;

    public static final int DARDO_ARCANO_COSTO_LANCIO_MAGO = 4;

    public static final int DARDO_ARCANO_COSTO_LANCIO_ELFO = 6;

    public static final int DARDO_ARCANO_RICARICA = 2;

    private CostantiAbilitaInnate() {
    }
}
