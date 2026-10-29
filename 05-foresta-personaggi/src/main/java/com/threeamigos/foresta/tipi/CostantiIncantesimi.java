package com.threeamigos.foresta.tipi;

/**
 * I valori di bilanciamento degli incantesimi: i costi di acquisto e di lancio, i danni di ognuno, e i fattori dei colpi a più bersagli.
 *
 * @author Stefano Reksten
 */
public final class CostantiIncantesimi {

    /**
     * Un incantesimo a più bersagli (MULTIPLO) o un'abilità di danno ripartisce il danno per posizione: il bersaglio principale lo subisce
     * intero, ogni successivo la metà del precedente (100%, 50%, 25%, 12,5%, 6,25%...), mai meno della quota minima
     */
    public static final double INCANTESIMO_MULTIPLO_FATTORE_PER_POSIZIONE = 0.5;

    public static final double INCANTESIMO_MULTIPLO_QUOTA_MINIMA = 0.0625;

    /**
     * Quota del danno base di un incantesimo (INCANTESIMO_*_DANNI × livello) che vale in combattimento: una pergamena
     * deve aiutare chiunque la lanci, ma essere devastante solo in mano al Mago (moltiplicatore magico 2,0)
     */
    public static final double INCANTESIMO_FATTORE_DANNI = 1.0;

    /**
     * Incantesimi
     */
    public static final int INCANTESIMO_ACQUA_COSTO_ACQUISTO = 5;

    public static final int INCANTESIMO_ACQUA_COSTO_LANCIO = 1;

    public static final int INCANTESIMO_ACQUA_DANNI = 45;

    public static final int INCANTESIMO_ARIA_COSTO_ACQUISTO = 5;

    public static final int INCANTESIMO_ARIA_COSTO_LANCIO = 1;

    public static final int INCANTESIMO_ARIA_DANNI = 35;

    public static final int INCANTESIMO_FULMINE_COSTO_ACQUISTO = 15;

    public static final int INCANTESIMO_FULMINE_COSTO_LANCIO = 10;

    public static final int INCANTESIMO_FULMINE_DANNI = 100;

    public static final int INCANTESIMO_FUOCO_COSTO_ACQUISTO = 5;

    public static final int INCANTESIMO_FUOCO_COSTO_LANCIO = 3;

    public static final int INCANTESIMO_FUOCO_DANNI = 50;

    public static final int INCANTESIMO_MORTE_COSTO_ACQUISTO = 5;

    public static final int INCANTESIMO_MORTE_COSTO_LANCIO = 5;

    public static final int INCANTESIMO_MORTE_DANNI = 0;

    /**
     * Morte uccide solo chi ha meno di questa percentuale della salute massima: la usano sia l'incantesimo sia la tattica che lo sceglie
     */
    public static final int INCANTESIMO_MORTE_SOGLIA_SALUTE_PERCENTUALE = 25;

    public static final int INCANTESIMO_RESURREZIONE_COSTO_ACQUISTO = 15;

    public static final int INCANTESIMO_RESURREZIONE_COSTO_LANCIO = 15;

    public static final int INCANTESIMO_SILENZIO_COSTO_ACQUISTO = 5;

    public static final int INCANTESIMO_SILENZIO_COSTO_LANCIO = 5;

    public static final int INCANTESIMO_TERRA_COSTO_ACQUISTO = 5;

    public static final int INCANTESIMO_TERRA_COSTO_LANCIO = 2;

    public static final int INCANTESIMO_TERRA_DANNI = 50;

    public static final int INCANTESIMO_GELO_COSTO_ACQUISTO = 5;

    public static final int INCANTESIMO_GELO_COSTO_LANCIO = 2;

    public static final int INCANTESIMO_GELO_DANNI = 50;

    public static final int INCANTESIMO_VELENO_COSTO_ACQUISTO = 5;

    public static final int INCANTESIMO_VELENO_COSTO_LANCIO = 2;

    public static final int INCANTESIMO_VELENO_DANNI = 50;

    private CostantiIncantesimi() {
    }
}
