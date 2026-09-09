package com.threeamigos.foresta.motore.modellodati;

/**
 *
 * @author Stefano Reksten
 */
public enum TipoInterazioneElementale {

    // Interazioni con BAGNATO
    // + FULMINE
    ELETTROCUZIONE("Elettrocuzione"),
    // + GELO
    CONGELAMENTO("Congelamento"),
    // + FUOCO
    VAPORIZZAZIONE("Vaporizzazione"),

    // Interazioni con BRUCIATO
    // + ARIA
    ALIMENTAZIONE_FIAMMA("Alimentazione fiamma"),
    // + ACQUA
    ESTINZIONE("Estinzione"),
    // + GELO
    SCIOGLIMENTO_TERMICO("Scioglimento termico"),
    // + VELENO
    ESPLOSIONE_DI_GAS("Esplosione di gas"),

    // Interazioni con CONGELATO
    // + CONTUNDENTE
    FRANTUMAZIONE_DEL_GHIACCO("Frantumazione del ghiaccio"),
    // + FUOCO
    DISGELO_VIOLENTO("Disgelo violento"),
    // + FULMINE
    SUPERCONDUZIONE("Superconduzione"),

    // Interazioni con MALEDETTO
    // + NECROTICO
    MIETITURA("Mietitura"),
    // + SACRO
    RIGETTO("Rigetto"),

    // Interazioni con INFETTATO
    // + SACRO
    PURIFICAZIONE("Purificazione");

    private final String descrizione;

    TipoInterazioneElementale(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getDescrizione() {
        return descrizione;
    }
}
