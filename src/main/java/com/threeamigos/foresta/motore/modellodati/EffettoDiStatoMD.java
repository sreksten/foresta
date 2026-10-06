package com.threeamigos.foresta.motore.modellodati;

import com.threeamigos.foresta.tipi.TipoEffettoDiStato;

/**
 *
 * @author Stefano Reksten
 */
public class EffettoDiStatoMD {

    private final TipoEffettoDiStato tipoEffettoDiStato;
    private int valore;
    private int danniNelTempo;

    public EffettoDiStatoMD(TipoEffettoDiStato tipoEffettoDiStato, int valore, int danniNelTempo) {
        this.tipoEffettoDiStato = tipoEffettoDiStato;
        this.valore = valore;
        this.danniNelTempo = danniNelTempo;
    }

    public TipoEffettoDiStato getTipoEffettoDiStato() {
        return tipoEffettoDiStato;
    }

    public int getDurata() {
        return valore;
    }

    public void setDurata(int valore) {
    	this.valore = valore;
    }

    public int getDanniNelTempo() {
    	return danniNelTempo;
    }

    public void setDanniNelTempo(int dannoNelTempo) {
    	this.danniNelTempo = dannoNelTempo;
    }
}
