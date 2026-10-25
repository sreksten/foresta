package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.modellodati.GruppoGiocatoreMD;

/**
 * I dati del gruppo per le prove di questo modulo: il modello dati della partita (che li mette insieme a quelli delle altre parti) sta
 * sopra, quindi le prove danno al gruppo i suoi, nuovi a ogni partita.
 */
public final class DatiDiProva {

    private DatiDiProva() {
    }

    /**
     * Una partita nuova: il gruppo prende da qui i dati, e il gruppo di prima si dimentica.
     */
    public static void nuovaPartita() {
        GruppoGiocatoreMD dati = new GruppoGiocatoreMD();
        GruppoGiocatore.setFornitoreDiDati(() -> dati);
        GruppoGiocatore.azzeraIstanza();
    }
}
