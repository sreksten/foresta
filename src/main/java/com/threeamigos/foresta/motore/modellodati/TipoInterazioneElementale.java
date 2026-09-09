package com.threeamigos.foresta.motore.modellodati;

/**
 *
 * @author Stefano Reksten
 */
public enum TipoInterazioneElementale {

    // Interazioni con BAGNATO
    // + FULMINE
    ELETTROCUZIONE,
    // + GELO
    CONGELAMENTO,
    // + FUOCO
    VAPORIZZAZIONE,

    // Interazioni con BRUCIATO
    // + ARIA
    ALIMENTAZIONE_FIAMMA,
    // + ACQUA
    ESTINZIONE,
    // + GELO
    SCIOGLIMENTO_TERMICO,
    // + VELENO
    ESPLOSIONE_DI_GAS,

    // Interazioni con CONGELATO
    // + CONTUNDENTE
    FRANTUMAZIONE_DEL_GHIACCO,
    // + FUOCO
    DISGELO_VIOLENTO,
    // + FULMINE
    SUPERCONDUZIONE,

    // Interazioni con MALEDETTO
    // + NECROTICO
    MIETITURA,
    // + SACRO
    RIGETTO,

    // Interazioni con INFETTATO
    // + SACRO
    PURIFICAZIONE

}
