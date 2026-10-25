package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.modellodati.TatticheMD;
import com.threeamigos.foresta.motore.DatiDiProva;

/**
 * I dati per le prove di questo modulo: il modello dati della partita sta in un modulo che dipende dalle tattiche, quindi le prove
 * danno al gruppo e alle tattiche i loro, nuovi a ogni partita.
 */
public final class DatiDiProvaDelleTattiche {

    private static TatticheMD tattiche;

    private DatiDiProvaDelleTattiche() {
    }

    /**
     * Una partita nuova: il gruppo e le tattiche prendono da qui i dati, e quelli di prima si dimenticano.
     */
    public static void nuovaPartita() {
        DatiDiProva.nuovaPartita();
        tattiche = new TatticheMD();
        TatticheDeiPersonaggi.setFornitoreDiDati(() -> tattiche);
    }

    /**
     * Le tattiche della partita in corso.
     */
    public static TatticheMD tattiche() {
        return tattiche;
    }
}
