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

    public static final int INCANTESIMO_NEBBIA_COSTO_ACQUISTO = 10;

    public static final int INCANTESIMO_NEBBIA_COSTO_LANCIO = 5;

    /**
     * La nebbia toglie questa percentuale (negativa) di PRECISIONE a ogni avversario, per INCANTESIMO_NEBBIA_DURATA turni
     */
    public static final double INCANTESIMO_NEBBIA_PRECISIONE_PERCENTUALE = -30.0;

    public static final int INCANTESIMO_NEBBIA_DURATA = 3;

    public static final int INCANTESIMO_NEGA_MAGIA_COSTO_ACQUISTO = 12;

    public static final int INCANTESIMO_NEGA_MAGIA_COSTO_LANCIO = 6;

    /**
     * La nega magia toglie questi punti (negativi) di POTERE_MAGICO a ogni avversario, per INCANTESIMO_NEGA_MAGIA_DURATA turni: il potere magico
     * parte da 100, quindi -40 fa il 60% dei danni degli incantesimi
     */
    public static final double INCANTESIMO_NEGA_MAGIA_POTERE_MAGICO = -40.0;

    public static final int INCANTESIMO_NEGA_MAGIA_DURATA = 3;

    public static final int INCANTESIMO_GRIDO_DI_BATTAGLIA_COSTO_ACQUISTO = 12;

    public static final int INCANTESIMO_GRIDO_DI_BATTAGLIA_COSTO_LANCIO = 6;

    /**
     * Il grido di battaglia dà a ogni personaggio vivo del gruppo, per INCANTESIMO_GRIDO_DI_BATTAGLIA_DURATA turni, un numero fisso di punti in più di CORAGGIO e di VALORE
     * (queste basi per la radice quadrata del livello di chi la lancia): sono punti e non una percentuale, perché chi non è un guerriero ne ha
     * pochissimi e una percentuale non cambierebbe niente
     */
    public static final double INCANTESIMO_GRIDO_DI_BATTAGLIA_BASE_CORAGGIO = 2.0;

    public static final double INCANTESIMO_GRIDO_DI_BATTAGLIA_BASE_VALORE = 2.0;

    public static final int INCANTESIMO_GRIDO_DI_BATTAGLIA_DURATA = 4;

    public static final int INCANTESIMO_TERREMOTO_COSTO_ACQUISTO = 12;

    public static final int INCANTESIMO_TERREMOTO_COSTO_LANCIO = 8;

    /**
     * Il danno base del terremoto, che colpisce tutti gli avversari per intero (non è ripartito per posizione): per questo è più basso di quello
     * della Terra
     */
    public static final int INCANTESIMO_TERREMOTO_DANNI = 30;

    /**
     * La probabilità (in percentuale) che chi subisce un danno dal terremoto cada a terra (ATTERRATO)
     */
    public static final int INCANTESIMO_TERREMOTO_PROBABILITA_ATTERRATO = 25;

    public static final int INCANTESIMO_TEMPESTA_COSTO_ACQUISTO = 15;

    public static final int INCANTESIMO_TEMPESTA_COSTO_LANCIO = 10;

    /**
     * Il danno base della tempesta, che colpisce per intero tutti gli avversari e anche tutti i compagni di chi la lancia (tranne lui)
     */
    public static final int INCANTESIMO_TEMPESTA_DANNI = 40;

    public static final int INCANTESIMO_TERRORE_COSTO_ACQUISTO = 12;

    public static final int INCANTESIMO_TERRORE_COSTO_LANCIO = 8;

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
