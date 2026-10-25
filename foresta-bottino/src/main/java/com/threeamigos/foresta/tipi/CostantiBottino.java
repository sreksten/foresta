package com.threeamigos.foresta.tipi;

/**
 * I valori di bilanciamento del bottino.
 *
 * @author Stefano Reksten
 */
public final class CostantiBottino {

    /**
     * Esiti rari di ogni cofano aperto: 10% una pergamena, 10% un artefatto casuale, 3% un oggetto che fa salire di livello un
     * artefatto (vedi TipoArtefatto.AGGIORNAMENTO), 0,5% un libro tecnico non ancora trovato (vedi leggendari.txt): 23,5% in tutto
     */
    public static final double COFANO_PROBABILITA_PERGAMENA = 0.10;
    public static final double COFANO_PROBABILITA_ARTEFATTO = 0.10;
    public static final double COFANO_PROBABILITA_AGGIORNAMENTO = 0.03;
    public static final double COFANO_PROBABILITA_LIBRO_TECNICO = 0.005;

    /**
     * Quanto aggiunge un anello magico ai suoi attributi, e quanti punti vale quando lo si prende.
     */
    public static final int ANELLO_MAGICO_AGGIUNTA_VALORE = 5;
    public static final int ANELLO_MAGICO_AGGIUNTA_CORAGGIO = 5;
    public static final int ANELLO_MAGICO_AGGIUNTA_CARISMA = 1;
    public static final int ANELLO_MAGICO_PUNTEGGIO = 50;

    private CostantiBottino() {
    }
}
