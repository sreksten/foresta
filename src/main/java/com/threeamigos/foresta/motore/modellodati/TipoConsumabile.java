package com.threeamigos.foresta.motore.modellodati;

/**
 * Il tipo di consumabili che il Gruppo ha a disposizione
 *
 * @author Stefano Reksten
 */
public enum TipoConsumabile {

    GEMME,
    MONETE,
    PUNTI_ESPERIENZA, // In realtà questi non vengono mai "consumati"

    // Tipi acquistabili da un alchimista
    INCANTESIMO,
    POZIONE_SALUTE,
    POZIONE_SALUTE_GRANDE,
    POZIONE_MAGIA,
    POZIONE_MAGIA_GRANDE,
    AUMENTO_MAGIA_SINGOLO,
    AUMENTO_MAGIA_GRUPPO,
    MAPPA_PARZIALE_FORESTA,
    MAPPA_COMPLETA_FORESTA

}
