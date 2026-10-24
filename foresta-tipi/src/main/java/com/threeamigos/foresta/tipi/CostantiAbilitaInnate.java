package com.threeamigos.foresta.tipi;

/**
 * I valori di bilanciamento delle abilità innate: quelle del Chierico e del Druido e il dardo arcano di Mago ed Elfo (costi di lancio, danni, durate e basi dei loro effetti).
 *
 * @author Stefano Reksten
 */
public final class CostantiAbilitaInnate {

    /**
     * Le abilità innate di Chierico e Druido (vedi TipoAbilita): costano MAGIA, non consumano pergamene
     */
    public static final int ABILITA_SCACCIARE_I_NON_MORTI_COSTO_LANCIO = 4;

    /**
     * Scacciare i non morti: ognuno cede con la SOGGEZIONE del chierico contro il suo CORAGGIO, e prende SPAVENTATO
     */
    public static final int ABILITA_IMPOSIZIONE_DELLE_MANI_COSTO_LANCIO = 5;

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

    public static final int ABILITA_RADICI_DANNI = 40;

    public static final int ABILITA_SCIAME_COSTO_LANCIO = 3;

    public static final int ABILITA_SCIAME_DANNI = 40;

    /**
     * La pelle di corteccia dà a tutto il gruppo, per ABILITA_PELLE_DI_CORTECCIA_DURATA turni, un numero fisso di punti in più di parata
     * e di resistenza magica (con un modificatore temporaneo): la base per la radice quadrata del livello del druido, come l'imposizione delle
     * mani. Sono punti e non una percentuale perché chi non è un guerriero ha pochissima parata e resistenza magica (da 1 a 18 punti ai livelli
     * da 3 a 8): una percentuale su quei valori non cambia la probabilità di essere colpiti (vedi chierico_e_druido.md, §4)
     */
    public static final int ABILITA_PELLE_DI_CORTECCIA_COSTO_LANCIO = 5;

    public static final double ABILITA_PELLE_DI_CORTECCIA_BASE_PARATA = 10.0;

    public static final double ABILITA_PELLE_DI_CORTECCIA_BASE_RESISTENZA_MAGICA = 10.0;

    public static final int ABILITA_PELLE_DI_CORTECCIA_DURATA = 4;

    /**
     * La rigenerazione dà RIGENERAZIONE a un personaggio per ABILITA_RIGENERAZIONE_DURATA turni: a ogni turno guarisce di
     * ABILITA_RIGENERAZIONE_BASE più questa quota della sua salute massima, aumentata dalla SAGGEZZA del druido
     */
    public static final int ABILITA_RIGENERAZIONE_COSTO_LANCIO = 4;

    public static final int ABILITA_RIGENERAZIONE_DURATA = 4;

    public static final int ABILITA_RIGENERAZIONE_BASE = 5;

    public static final double ABILITA_RIGENERAZIONE_QUOTA_SALUTE_MASSIMA = 0.05;

    public static final double ABILITA_RIGENERAZIONE_DIVISORE_SAGGEZZA = 50.0;

    /**
     * L'alba sacra del Sacerdote: toglie al gruppo gli stati alterati negativi e i modificatori temporanei.
     */
    public static final int ABILITA_ALBA_SACRA_COSTO_LANCIO = 5;

    /**
     * La luce divina del Sacerdote: danno sacro su più avversari (il doppio ai non morti), e investe anche i non morti del proprio gruppo.
     */
    public static final int ABILITA_LUCE_DIVINA_COSTO_LANCIO = 10;

    public static final int ABILITA_LUCE_DIVINA_DANNI = 100;

    /**
     * Il dardo arcano, l'incantesimo innato di Mago ed Elfo: su un solo bersaglio, per poca MAGIA e senza consumare
     * pergamene. Al Mago rende di più (una pergamena di fuoco fa 50) e costa meno che all'Elfo, che è
     * un po' meno bravo con la magia e in cambio sa combattere
     */
    public static final int DARDO_ARCANO_DANNI_MAGO = 20;

    public static final int DARDO_ARCANO_DANNI_ELFO = 15;

    public static final int DARDO_ARCANO_COSTO_LANCIO_MAGO = 2;

    public static final int DARDO_ARCANO_COSTO_LANCIO_ELFO = 4;

    private CostantiAbilitaInnate() {
    }
}
